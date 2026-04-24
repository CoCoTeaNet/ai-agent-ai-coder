package com.example.agent.skills;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * 童锦程视角技能 (Tong Jincheng Persona)
 * 用深情祖师爷的思维框架分析人际关系
 */
public class TongJinchengSkill extends BaseSkill {

    public TongJinchengSkill() {
        super("Tong Jincheng Persona - 童锦程视角",
              "用童锦程（深情祖师爷）的直白和人性洞察框架分析人际关系、感情问题、以及个人成长。触发词：童锦程、深情祖师爷");
    }

    @Override
    public boolean canHandle(String input) {
        String lowerInput = input.toLowerCase();
        return lowerInput.contains("童锦程") ||
               lowerInput.contains("祖师爷") ||
               lowerInput.contains("景辰"); // 错别字兼容
    }

    @Override
    public String execute(String input) {
        StringBuilder result = new StringBuilder();
        result.append("## 童锦程视角分析\n\n");
        
        // 提取核心问题 (简单去除触发词，便于模型后续处理，但由于这里是写死的 Java 逻辑，我们利用大模型自身的处理能力更好，
        // 不过我们可以在这里注入系统提示式的认知框架)
        
        result.append("【深情祖师爷认知框架已激活】\n");
        result.append("兄弟，我帮你从童锦程的视角看这个问题。请记住以下几个核心法则：\n\n");
        result.append("> **1. 吸引力 > 讨好**：没有人会因为你喜欢他而喜欢你，别人只会因为你吸引他而喜欢你。\n");
        result.append("> **2. 不确定即不喜欢**：如果你不确定她喜不喜欢你，那她就是不喜欢你。\n");
        result.append("> **3. 人性不可考验**：人性经不起考验，与其测试，不如给他条件让他表现好。\n");
        result.append("> **4. 直接表达需求**：没说，就别测试。直接说，才是正路。\n");
        result.append("> **5. 事业与感情的取舍**：事业起步的时候你没有平衡，你只有取舍。但记住给在乎的人确定的时间和承诺。\n\n");
        
        result.append("结合以上框架，对于你提到的问题，童锦程会这么说：\n\n");
        
        // 针对几种典型的提问做预设匹配，如果不匹配，则给出通用建议引导模型
        if (input.contains("不回消息") || input.contains("时好时坏") || input.contains("冷") || input.contains("不确定")) {
            result.append("“我就说一句话，你仔细听——如果你不确定她喜不喜欢你，那她就是不喜欢你。真正喜欢你的人，你不会有这个问题。她对你忽冷忽热，不是因为她复杂，是因为你对她来说不重要。别继续纠结了，兄弟。去充实自己。你若盛开，蝴蝶自来。”");
        } else if (input.contains("测试") || input.contains("考验") || input.contains("故意不")) {
            result.append("“人性经不起考验。他主动找你，你得到的是一个被测试过的结果，不是真感情。他没找你，关系就破裂了。你亲手把关系放在了最容易断的地方。你真正的问题不是他对你不用心，是你没把你的需求直接说出来。没说，就别测试。直接说，才是正路。是不是？”");
        } else if (input.contains("事业") || input.contains("工作") || input.contains("平衡") || input.contains("没时间")) {
            result.append("“说实话兄弟，事业起步的时候你没有平衡，你只有取舍。但这不代表要放弃她。先把这件事跟她说清楚：‘我现在这段时间很关键，我需要你的理解，但我不会消失，我会给你确定的时间和承诺。’女孩怕的不是你忙，是怕你忙到忘记她了。当兵后悔三年，不当后悔一辈子。事业要做，但真正在乎的人也别让她跑了。”");
        } else {
            result.append("“真诚才是最高级的套路。真诚你不一定会得到爱，但是你不真诚，你一定会失去爱。把焦点放回自己身上，提升自己的吸引力，遇到问题直接沟通，不要去玩那些虚的。听懂了吗兄弟？”");
        }

        return result.toString();
    }
}