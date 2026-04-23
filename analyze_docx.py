import zipfile
import os
import xml.etree.ElementTree as ET

def analyze_docx(docx_path):
    print("=== DOCX文件分析 ===")
    print(f"文件路径: {docx_path}")
    print(f"文件大小: {os.path.getsize(docx_path)} 字节")
    
    # 检查是否能正常解压
    try:
        with zipfile.ZipFile(docx_path, 'r') as zf:
            print("\n文件结构:")
            for file_info in zf.infolist():
                print(f"  {file_info.filename}")
            
            print("\n=== 检查关键文件 ===")
            # 检查核心文件是否存在
            required_files = [
                '[Content_Types].xml', 
                '_rels/.rels', 
                'word/document.xml', 
                'docProps/core.xml', 
                'docProps/app.xml'
            ]
            
            missing_files = []
            for req_file in required_files:
                if req_file not in [f.filename for f in zf.infolist()]:
                    missing_files.append(req_file)
            
            if missing_files:
                print(f"❌ 缺少关键文件: {', '.join(missing_files)}")
            else:
                print("✅ 所有关键文件存在")
            
            # 尝试解析document.xml
            print("\n=== 检查document.xml ===")
            try:
                doc_xml = zf.read('word/document.xml')
                # 简单检查是否是有效的XML
                root = ET.fromstring(doc_xml)
                print("✅ document.xml 格式正确")
                
                # 统计段落数量
                paragraphs = root.findall('.//{http://schemas.openxmlformats.org/wordprocessingml/2006/main}p')
                print(f"   段落数量: {len(paragraphs)}")
                
                # 统计字符数（近似）
                text_content = ET.tostring(root, encoding='utf-8', method='text')
                print(f"   文本长度: {len(text_content.decode('utf-8'))} 字符")
                
            except Exception as e:
                print(f"❌ document.xml 解析失败: {e}")
            
            # 检查是否有宏或可疑内容
            print("\n=== 安全检查 ===")
            suspicious_files = [
                'word/vbaProject.bin',  # 宏
                'word/_rels/document.xml.rels'  # 关系文件
            ]
            
            for s_file in suspicious_files:
                if s_file in [f.filename for f in zf.infolist()]:
                    print(f"⚠️ 发现可能包含宏或外部链接的文件: {s_file}")
                    if s_file == 'word/_rels/document.xml.rels':
                        try:
                            rels_xml = zf.read(s_file)
                            rels_root = ET.fromstring(rels_xml)
                            # 查找外部链接
                            external_rels = rels_root.findall(".//{http://schemas.openxmlformats.org/package/2006/relationships}Relationship[@TargetMode='External']")
                            if external_rels:
                                print(f"   外部链接数量: {len(external_rels)}")
                                for rel in external_rels:
                                    print(f"      - {rel.attrib['Target']}")
                        except Exception as e:
                            print(f"   无法解析关系文件: {e}")
            
            print("\n=== 完整度检查 ===")
            try:
                # 尝试使用python-docx库（如果可用）
                try:
                    from docx import Document
                    doc = Document(docx_path)
                    print("✅ 可以使用python-docx正常打开")
                    
                    # 检查文档属性
                    print(f"   标题: {doc.core_properties.title}")
                    print(f"   作者: {doc.core_properties.author}")
                    print(f"   主题: {doc.core_properties.subject}")
                    print(f"   创建时间: {doc.core_properties.created}")
                    print(f"   最后修改时间: {doc.core_properties.modified}")
                    
                except ImportError:
                    print("ℹ️ python-docx库未安装，跳过高级检查")
            except Exception as e:
                print(f"❌ 使用python-docx打开失败: {e}")
            
    except zipfile.BadZipFile:
        print("\n❌ 不是有效的ZIP文件（DOCX本质是ZIP压缩包）")
    except Exception as e:
        print(f"\n❌ 分析过程中出错: {e}")

if __name__ == "__main__":
    docx_file = "智能物业系统项目合同.docx"
    
    if not os.path.exists(docx_file):
        print(f"❌ 文件不存在: {docx_file}")
    else:
        analyze_docx(docx_file)
