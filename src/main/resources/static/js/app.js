/**
 * Agent Web 应用程序
 * 处理聊天功能
 */
class AgentApp {
    constructor() {
        this.sessionId = null;
        this.isLoading = false;
        this.showTrace = true; // 默认显示执行链路
        this.traceExpanded = false; // 默认收起
        this.initialized = false;

        // 立即初始化元素和事件，不等待 DOMContentLoaded
        this.initializeElements();
        this.initializeEventListeners();
        this.init();
    }

    // 初始化 DOM 元素
    initializeElements() {
        this.chatContainer = document.getElementById('chatContainer');
        this.messageInput = document.getElementById('messageInput');
        this.sendBtn = document.getElementById('sendBtn');
        this.toolsBtn = document.getElementById('toolsBtn');
        this.traceToggle = document.getElementById('traceToggle');
        this.sessionInfo = document.getElementById('sessionId');
        this.sessionDisplay = document.getElementById('sessionInfo');
        this.toolsModal = document.getElementById('toolsModal');
        this.toolsModalClose = document.getElementById('toolsModalClose');
        this.toolsList = document.getElementById('toolsList');

        // 文件上传相关元素
        this.uploadBtn = document.getElementById('uploadBtn');
        this.fileInput = document.getElementById('fileInput');
        this.filePreviewArea = document.getElementById('filePreviewArea');
        this.filePreviewList = document.getElementById('filePreviewList');

        // 已上传的文件列表
        this.uploadedFiles = [];
    }

    // 初始化事件监听器
    initializeEventListeners() {
        console.log('开始初始化事件监听器');
        console.log('当前 DOM 状态:', document.readyState);
        console.log('发送按钮:', this.sendBtn);
        console.log('输入框:', this.messageInput);
        console.log('工具按钮:', this.toolsBtn);
        console.log('链路按钮:', this.traceToggle);

        // 发送按钮点击 - 使用捕获阶段
        if (this.sendBtn) {
            this.sendBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('发送按钮点击事件触发');
                this.sendMessage();
            }, true);
            console.log('发送按钮事件监听器绑定成功');
        } else {
            console.error('发送按钮元素未找到');
        }

        // 输入框按键事件
        if (this.messageInput) {
            this.messageInput.addEventListener('keydown', (e) => {
                console.log('输入框按键事件:', e.key, e.shiftKey, e.ctrlKey);
                this.handleKeyDown(e);
            });
            console.log('输入框按键事件监听器绑定成功');
        } else {
            console.error('输入框元素未找到');
        }

        // 输入框输入事件（自动调整高度）
        if (this.messageInput) {
            this.messageInput.addEventListener('input', () => this.autoResizeTextarea());
            console.log('输入框输入事件监听器绑定成功');
        }

        // 工具按钮 - 使用捕获阶段
        if (this.toolsBtn) {
            this.toolsBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('工具按钮点击事件触发');
                this.showTools();
            }, true);
            console.log('工具按钮事件监听器绑定成功');
        } else {
            console.error('工具按钮元素未找到');
        }

        // 执行链路开关 - 使用捕获阶段
        if (this.traceToggle) {
            this.traceToggle.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('链路按钮点击事件触发');
                this.toggleTrace();
            }, true);
            console.log('链路按钮事件监听器绑定成功');
        } else {
            console.error('链路按钮元素未找到');
        }

        // 工具面板关闭 - 使用捕获阶段
        if (this.toolsModalClose) {
            this.toolsModalClose.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('工具面板关闭点击事件触发');
                this.hideTools();
            }, true);
            console.log('工具面板关闭事件监听器绑定成功');
        } else {
            console.error('工具面板关闭按钮未找到');
        }

        // 点击模态框外部关闭
        if (this.toolsModal) {
            this.toolsModal.addEventListener('click', (e) => {
                if (e.target === this.toolsModal) {
                    console.log('点击模态框外部触发');
                    this.hideTools();
                }
            });
            console.log('模态框外部点击事件监听器绑定成功');
        } else {
            console.error('工具模态框未找到');
        }

        // 文件上传相关事件
        if (this.uploadBtn) {
            this.uploadBtn.addEventListener('click', (e) => {
                e.preventDefault();
                console.log('上传按钮点击');
                this.fileInput.click();
            }, true);
            console.log('上传按钮事件监听器绑定成功');
        }

        if (this.fileInput) {
            this.fileInput.addEventListener('change', (e) => {
                console.log('文件选择变化');
                this.handleFileSelect(e);
            });
            console.log('文件选择事件监听器绑定成功');
        }

        // 拖拽上传支持
        if (this.messageInput) {
            this.messageInput.addEventListener('dragover', (e) => {
                e.preventDefault();
                e.stopPropagation();
                this.messageInput.classList.add('drag-over');
            });

            this.messageInput.addEventListener('dragleave', (e) => {
                e.preventDefault();
                e.stopPropagation();
                this.messageInput.classList.remove('drag-over');
            });

            this.messageInput.addEventListener('drop', (e) => {
                e.preventDefault();
                e.stopPropagation();
                this.messageInput.classList.remove('drag-over');

                const files = e.dataTransfer.files;
                if (files && files.length > 0) {
                    console.log('拖放文件:', files);
                    this.addFiles(files);
                }
            });
            console.log('拖拽上传事件监听器绑定成功');
        }
    }

    // 初始化应用
    async init() {
        try {
            // 检查连接状态
            await this.pingServer();
            this.getPersistentSession();
            // 更新欢迎消息为Markdown
            this.initWelcomeMessage();
            // 加载模型列表
            this.loadModels();
        } catch (error) {
            this.showError('服务器连接失败：' + error.message);
        }
    }

    // 检查服务器连接
    async pingServer() {
        try {
            const response = await fetch('/api/v1/stats');
            const data = await response.json();
            if (data.success) {
                console.log('服务器连接成功，活跃会话数：', data.activeSessions);
            }
        } catch (error) {
            console.warn('服务器不可达，将使用模拟模式');
        }
    }

    // 获取持久化会话
    async getPersistentSession() {
        try {
            const response = await fetch('/api/v1/session', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();
            if (data.success) {
                this.sessionId = data.sessionId;
                this.sessionInfo.textContent = this.sessionId.slice(0, 8);
                console.log('会话已准备好:', this.sessionId);
            } else {
                this.showError('会话准备失败：' + data.error);
            }
        } catch (error) {
            console.warn('无法获取会话，使用临时 ID', error);
            this.sessionId = 'temp-' + Date.now();
            this.sessionInfo.textContent = '本地';
        }
    }

    // 加载并渲染模型和提供商切换器
    async loadModels() {
        const toggleGroup = document.getElementById('modelToggleGroup');
        if (!toggleGroup) return;

        try {
            // 首先尝试加载提供商列表
            const providersResponse = await fetch('/api/providers');
            const providersData = await providersResponse.json();

            if (providersData.success && providersData.data && providersData.data.length > 0) {
                toggleGroup.innerHTML = '';
                let firstEnabledProvider = null;

                providersData.data.forEach(provider => {
                    const btn = document.createElement('button');
                    btn.className = 'model-toggle-btn';
                    btn.dataset.providerId = provider.id;

                    const dot = document.createElement('span');
                    dot.className = 'model-status-dot';

                    const text = document.createElement('span');
                    text.textContent = provider.name;

                    btn.appendChild(dot);
                    btn.appendChild(text);

                    if (!provider.enabled) {
                        btn.classList.add('disabled');
                        btn.title = '该提供商已禁用';
                    } else if (!firstEnabledProvider) {
                        firstEnabledProvider = provider;
                    }

                    if (provider.isDefault) {
                        btn.classList.add('default-provider');
                    }

                    // 点击切换提供商
                    btn.addEventListener('click', async () => {
                        if (btn.classList.contains('disabled') || btn.classList.contains('active')) return;

                        // 乐观更新UI
                        const currentActive = toggleGroup.querySelector('.active');
                        if (currentActive) currentActive.classList.remove('active');
                        btn.classList.add('active');

                        try {
                            const switchResponse = await fetch(`/api/v1/session/${this.sessionId}/provider`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ providerConfigId: provider.id })
                            });

                            const switchData = await switchResponse.json();
                            if (!switchData.success) {
                                this.showToast(switchData.error || '切换提供商失败', 'error');
                                btn.classList.remove('active');
                                if (currentActive) currentActive.classList.add('active');
                            } else {
                                this.showToast(`已切换至: ${provider.name}`, 'success');
                            }
                        } catch (error) {
                            this.showToast('切换提供商失败', 'error');
                            btn.classList.remove('active');
                            if (currentActive) currentActive.classList.add('active');
                        }
                    });

                    toggleGroup.appendChild(btn);
                });

                if (firstEnabledProvider) {
                    const defaultBtn = toggleGroup.querySelector(`[data-provider-id="${firstEnabledProvider.id}"]`);
                    if (defaultBtn) {
                        defaultBtn.classList.add('active');
                    }
                }
                return;
            }
        } catch (error) {
            console.log('加载提供商列表失败，回退到原模型列表:', error);
        }

        try {
            const response = await fetch('/api/v1/models');
            const data = await response.json();

            if (data.success && data.models) {
                toggleGroup.innerHTML = '';
                let firstAvailableModel = null;

                data.models.forEach(model => {
                    const btn = document.createElement('button');
                    btn.className = 'model-toggle-btn';
                    btn.dataset.value = model.id;

                    const dot = document.createElement('span');
                    dot.className = 'model-status-dot';

                    const text = document.createElement('span');
                    text.textContent = model.name;

                    btn.appendChild(dot);
                    btn.appendChild(text);

                    if (!model.available) {
                        btn.classList.add('disabled');
                        btn.title = '未配置该模型或不可用';
                    } else if (!firstAvailableModel) {
                        firstAvailableModel = model;
                    }

                    btn.addEventListener('click', async () => {
                        if (btn.classList.contains('disabled') || btn.classList.contains('active')) return;

                        const currentActive = toggleGroup.querySelector('.active');
                        if (currentActive) currentActive.classList.remove('active');
                        btn.classList.add('active');

                        try {
                            const switchResponse = await fetch('/api/v1/models/switch', {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ modelId: model.id })
                            });

                            const switchData = await switchResponse.json();
                            if (!switchData.success) {
                                this.showToast(switchData.error || '当前模型不可用', 'error');
                                btn.classList.remove('active');
                                if (currentActive) currentActive.classList.add('active');
                            } else {
                                this.currentModelId = model.id;
                                this.showToast(`已切换至: ${model.name}`, 'success');
                            }
                        } catch (error) {
                            this.showToast('切换模型失败', 'error');
                            btn.classList.remove('active');
                            if (currentActive) currentActive.classList.add('active');
                        }
                    });

                    toggleGroup.appendChild(btn);
                });

                if (firstAvailableModel) {
                    this.currentModelId = firstAvailableModel.id;
                    const defaultBtn = toggleGroup.querySelector(`[data-value="${firstAvailableModel.id}"]`);
                    if (defaultBtn) {
                        defaultBtn.classList.add('active');
                    }
                }
            }
        } catch (error) {
            console.error('加载模型列表失败:', error);
            toggleGroup.innerHTML = '<span style="color: var(--text-muted); font-size: 12px; padding: 0 10px;">加载模型失败</span>';
        }
    }

    // 切换执行链路显示
    toggleTrace() {
        this.showTrace = !this.showTrace;
        if (this.traceToggle) {
            this.traceToggle.textContent = this.showTrace ? '🔍 链路' : '⚪ 链路';
            this.traceToggle.title = this.showTrace ? '点击隐藏执行链路' : '点击显示执行链路';
        }

        // 显示或隐藏所有已存在的执行链路
        const traceContainers = document.querySelectorAll('.trace-container');
        traceContainers.forEach(container => {
            container.style.display = this.showTrace ? 'block' : 'none';
        });
    }

    // 发送消息（流式）
    async sendMessage() {
        console.log('sendMessage 方法被调用');
        if (this.isLoading) return;

        const message = this.messageInput.value.trim();
        console.log('用户输入的消息:', message);
        console.log('附件数量:', this.uploadedFiles.length);

        // 如果没有消息也没有附件，不发送
        if (!message && this.uploadedFiles.length === 0) return;

        // 上传所有附件
        const uploadedFileInfos = [];
        for (const fileObj of this.uploadedFiles) {
            try {
                console.log('正在上传文件:', fileObj.name);
                const fileInfo = await this.uploadFile(fileObj);
                uploadedFileInfos.push(fileInfo);
            } catch (error) {
                console.error('文件上传失败:', fileObj.name, error);
                this.showError(`文件 ${fileObj.name} 上传失败: ${error.message}`);
                // 继续上传其他文件
            }
        }

        // 添加用户消息到界面（包含附件）
        this.addMessageToChat(message, 'user', null, uploadedFileInfos);
        this.messageInput.value = '';
        this.autoResizeTextarea();

        // 清空附件列表
        this.uploadedFiles = [];
        this.filePreviewList.innerHTML = '';
        this.updateFilePreviewArea();

        this.isLoading = true;
        this.sendBtn.disabled = true;
        this.sendBtn.innerHTML = '<span class="loading"></span> 发送中...';

        // 创建空的助手消息占位
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message message-assistant';

        const avatar = document.createElement('div');
        avatar.className = 'message-avatar';
        avatar.textContent = '🤖';

        const content = document.createElement('div');
        content.className = 'message-content';

        const textDiv = document.createElement('div');
        textDiv.className = 'message-text';
        textDiv.innerHTML = '<span class="streaming-cursor"></span>';

        content.appendChild(textDiv);

        const footer = this.createMessageFooter('assistant');
        content.appendChild(footer);

        messageDiv.appendChild(avatar);
        messageDiv.appendChild(content);
        this.chatContainer.appendChild(messageDiv);
        this.scrollToBottom();

        let fullResponse = '';
        let trace = null;
        let buffer = '';

        try {
            // 首先尝试使用非流式接口，避免 SSE 解析问题
            try {
                const response = await fetch('/api/v1/chat', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        sessionId: this.sessionId,
                        message: message,
                        attachments: uploadedFileInfos
                    })
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    this.showError(errorData.error || '发送失败');
                    return;
                }

                const result = await response.json();
                if (result.success) {
                    fullResponse = result.response;
                    textDiv.innerHTML = this.parseMarkdown(fullResponse);
                    if (result.trace && result.trace.length > 0) {
                        trace = result.trace;
                        if (this.showTrace) {
                            const traceDiv = this.createTraceDiv(trace);
                            content.insertBefore(traceDiv, footer);
                        }
                    }
                    if (result.sessionId && !this.sessionId) {
                        this.sessionId = result.sessionId;
                        this.sessionInfo.textContent = this.sessionId.slice(0, 8);
                    }
                } else {
                    this.showError(result.error || '发送失败');
                }
            } catch (error) {
                console.error('使用非流式接口失败，尝试使用流式接口:', error);
                // 备用方案：使用流式接口
                const response = await fetch('/api/v1/chat/stream', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        sessionId: this.sessionId,
                        message: message,
                        attachments: uploadedFileInfos
                    })
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    this.showError(errorData.error || '发送失败');
                    return;
                }

                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let inData = false;
                let dataContent = '';

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    buffer += decoder.decode(value, { stream: true });

                    // 循环处理buffer中的数据
                    while (true) {
                        if (!inData) {
                            const dataIndex = buffer.indexOf('data:');
                            if (dataIndex === -1) break; // 没有找到data:标记，等待更多数据

                            // 找到data:标记，开始收集内容
                            inData = true;
                            buffer = buffer.substring(dataIndex + 5); // 跳过data:
                            dataContent = '';
                        } else {
                            // 查找两个连续的换行符作为事件结束
                            const endIndex = buffer.indexOf('\n\n');
                            if (endIndex === -1) {
                                // 没有找到结束标记，但可能有一个换行符
                                const singleNewLine = buffer.indexOf('\n');
                                if (singleNewLine !== -1) {
                                    dataContent += buffer.substring(0, singleNewLine);
                                    buffer = buffer.substring(singleNewLine + 1);
                                }
                                // 否则等待更多数据
                                break;
                            }

                            // 收集到的内容
                            dataContent += buffer.substring(0, endIndex);
                            buffer = buffer.substring(endIndex + 2);

                            // 尝试解析
                            const trimmedData = dataContent.trim();
                            if (trimmedData) {
                                try {
                                    const eventData = JSON.parse(trimmedData);
                                    if (eventData.chunk) {
                                        fullResponse += eventData.chunk;
                                        textDiv.innerHTML = this.parseMarkdown(fullResponse) + '<span class="streaming-cursor"></span>';
                                        this.scrollToBottom();
                                    }
                                    if (eventData.done) {
                                        textDiv.innerHTML = this.parseMarkdown(fullResponse);
                                        if (eventData.trace) {
                                            trace = eventData.trace;
                                            if (this.showTrace && trace.length > 0) {
                                                const traceDiv = this.createTraceDiv(trace);
                                                content.insertBefore(traceDiv, footer);
                                            }
                                        }
                                        if (eventData.sessionId && !this.sessionId) {
                                            this.sessionId = eventData.sessionId;
                                            this.sessionInfo.textContent = this.sessionId.slice(0, 8);
                                        }
                                    }
                                } catch (e) {
                                    console.warn('解析SSE数据失败:', e, dataContent);
                                }
                            }

                            inData = false;
                            dataContent = '';
                        }
                    }
                }
            }
        } catch (error) {
            console.error('请求发送失败:', error);
            this.showError('网络错误：' + error.message);
            if (fullResponse) {
                textDiv.innerHTML = this.parseMarkdown(fullResponse);
            }
        } finally {
            this.isLoading = false;
            this.sendBtn.disabled = false;
            this.sendBtn.innerHTML = '<span>发送</span>';
            this.scrollToBottom();
        }
    }



    // 创建执行链路显示
    createTraceDiv(trace) {
        const traceContainer = document.createElement('div');
        traceContainer.className = 'trace-container';
        traceContainer.style.opacity = '0';
        traceContainer.style.transform = 'translateY(10px)';
        traceContainer.style.transition = 'opacity 0.3s ease, transform 0.3s ease';

        // 延迟显示容器
        setTimeout(() => {
            traceContainer.style.opacity = '1';
            traceContainer.style.transform = 'translateY(0)';
        }, 200);

        // 头部 - 包含开关
        const traceHeader = document.createElement('div');
        traceHeader.className = 'trace-header';

        const toggle = document.createElement('div');
        toggle.className = 'trace-toggle';

        const toggleIcon = document.createElement('span');
        toggleIcon.className = 'trace-toggle-icon';
        toggleIcon.textContent = this.traceExpanded ? '▼' : '▶';

        const title = document.createElement('span');
        title.className = 'trace-title';
        title.textContent = `🔍 执行链路 (${trace.length} 步)`;

        const toggleText = document.createElement('span');
        toggleText.className = 'trace-toggle-text';
        toggleText.textContent = this.traceExpanded ? '点击收起' : '点击展开';

        toggle.appendChild(toggleIcon);
        toggle.appendChild(title);
        toggle.appendChild(toggleText);

        // 步骤容器 - 默认收起
        const traceSteps = document.createElement('div');
        traceSteps.className = 'trace-steps';
        if (!this.traceExpanded) {
            traceSteps.classList.add('collapsed');
        }

        // 预创建所有步骤
        trace.forEach((step, index) => {
            const stepDiv = document.createElement('div');
            stepDiv.className = 'trace-step';
            stepDiv.style.opacity = '0';
            stepDiv.style.transform = 'translateX(-20px)';
            stepDiv.style.transition = `opacity 0.3s ease ${index * 0.15}s, transform 0.3s ease ${index * 0.15}s`;

            const stepIcon = document.createElement('span');
            stepIcon.className = 'trace-icon';
            stepIcon.textContent = step.icon || '⏺';

            const stepContent = document.createElement('div');
            stepContent.className = 'trace-content';

            const stepInfo = document.createElement('div');
            stepInfo.className = 'trace-info';

            const stepType = document.createElement('span');
            stepType.className = 'trace-type';
            stepType.textContent = this.formatStepType(step.type);

            const stepTime = document.createElement('span');
            stepTime.className = 'trace-time';
            stepTime.textContent = this.formatTimestamp(step.timestamp);

            stepInfo.appendChild(stepType);
            stepInfo.appendChild(stepTime);

            const stepText = document.createElement('div');
            stepText.className = 'trace-text';
            stepText.textContent = step.content;

            stepContent.appendChild(stepInfo);
            stepContent.appendChild(stepText);

            stepDiv.appendChild(stepIcon);
            stepDiv.appendChild(stepContent);

            traceSteps.appendChild(stepDiv);
        });

        // 添加点击事件
        toggle.addEventListener('click', () => {
            this.traceExpanded = !this.traceExpanded;
            if (this.traceExpanded) {
                toggleIcon.textContent = '▼';
                toggleText.textContent = '点击收起';
                traceSteps.classList.remove('collapsed');
                // 冒泡式显示动画
                this.showStepsBubbling(traceSteps.querySelectorAll('.trace-step'));
            } else {
                toggleIcon.textContent = '▶';
                toggleText.textContent = '点击展开';
                traceSteps.classList.add('collapsed');
            }
        });

        traceHeader.appendChild(toggle);
        traceContainer.appendChild(traceHeader);
        traceContainer.appendChild(traceSteps);

        // 延迟显示步骤
        if (this.traceExpanded) {
            setTimeout(() => {
                this.showStepsBubbling(traceSteps.querySelectorAll('.trace-step'));
            }, 300);
        }

        return traceContainer;
    }

    // 冒泡式显示步骤
    showStepsBubbling(steps) {
        steps.forEach((step, index) => {
            setTimeout(() => {
                step.style.opacity = '1';
                step.style.transform = 'translateX(0)';
            }, index * 150);
        });
    }

    // 格式化步骤类型
    formatStepType(type) {
        const typeMap = {
            'THOUGHT': '思考',
            'TOOL_CALL': '调用工具',
            'TOOL_RESULT': '工具结果',
            'OBSERVATION': '观察',
            'FINAL_ANSWER': '最终答案'
        };
        return typeMap[type] || type;
    }

    // 处理键盘事件
    handleKeyDown(e) {
        // Enter发送（Shift+Enter换行）
        if (e.key === 'Enter' && !e.shiftKey) {
            console.log('回车键被按下，准备发送消息');
            e.preventDefault();
            e.stopPropagation();
            this.sendMessage();
        }
    }

    // 自动调整文本区域高度
    autoResizeTextarea() {
        const input = this.messageInput;
        input.style.height = 'auto';
        input.style.height = Math.min(input.scrollHeight, 200) + 'px';
    }

    // 滚动到底部
    scrollToBottom() {
        this.chatContainer.scrollTop = this.chatContainer.scrollHeight;
    }

    // 显示工具面板
    async showTools() {
        console.log('showTools 方法被调用');
        try {
            const response = await fetch('/api/v1/tools');
            const data = await response.json();
            console.log('工具列表数据:', data);

            if (data.success) {
                this.toolsList.innerHTML = '';
                data.tools.forEach(tool => {
                    const toolDiv = document.createElement('div');
                    toolDiv.className = 'tool-item';

                    const toolName = document.createElement('div');
                    toolName.className = 'tool-name';
                    toolName.textContent = tool;

                    const toolDesc = document.createElement('div');
                    toolDesc.className = 'tool-description';
                    toolDesc.textContent = this.getToolDescription(tool);

                    toolDiv.appendChild(toolName);
                    toolDiv.appendChild(toolDesc);
                    this.toolsList.appendChild(toolDiv);
                });

                console.log('显示工具面板');
                this.toolsModal.classList.add('show');
            } else {
                this.showError(data.error || '获取工具列表失败');
            }
        } catch (error) {
            console.error('获取工具列表出错:', error);
            this.showError('网络错误：' + error.message);
        }
    }

    // 获取工具描述
    getToolDescription(toolName) {
        const descriptions = {
            'Calculator - 数学计算': '支持加减乘除、括号运算、三角函数等',
            'DateTime - 日期时间处理': '获取当前时间、格式化日期、计算日期差',
            'Search - 搜索功能': '搜索网络信息',
            'RealSearchTool - 真实网络搜索（DuckDuckGo/维基百科）': '使用真实API搜索网络信息',
            'FileTool - 文件操作': '文件读写、目录操作等',
            'NetworkTool - 网络请求': '发送HTTP请求获取网络资源',
            'Chain of Thought - 思维链推理': '展示思考过程，逐步推理得出结论',
            'ReAct - 推理-行动模式': '先思考后行动，使用工具解决问题',
            'Self Reflection - 自我反思': '自动检查和改进回答质量',
            'Code Analysis - 代码分析 - 提供代码分析、代码审查、代码质量评估、代码复杂度计算等功能': '分析代码质量、复杂度，提供代码审查建议',
            'Translation - 翻译 - 提供多语言翻译功能，支持中文、英语、日语、法语、德语、西班牙语、韩语等语言的互译': '提供多语言翻译支持'
        };

        return descriptions[toolName] || '该工具提供特定功能';
    }

    // 隐藏工具面板
    hideTools() {
        this.toolsModal.classList.remove('show');
    }

    // 显示错误
    showError(error) {
        this.addMessageToChat('错误：' + error, 'assistant');
        console.error(error);
    }

    // 显示 Toast 通知
    showToast(message, type = 'success') {
        const toast = document.getElementById('toast');
        if (toast) {
            toast.textContent = message;
            toast.className = `toast ${type}`;
            toast.classList.add('active');

            setTimeout(() => {
                toast.classList.remove('active');
            }, 3000);
        }
    }

    // 格式化时间戳
    formatTimestamp(timestamp) {
        if (!timestamp) return '';
        const date = new Date(timestamp);
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${hours}:${minutes}:${seconds}`;
    }

    // 解析Markdown文本
    parseMarkdown(text) {
        if (!text) return '';

        // 配置marked
        marked.setOptions({
            breaks: true, // 支持换行
            gfm: true, // 支持GitHub Flavored Markdown
            sanitize: false, // 允许HTML（注意：实际使用时需要考虑安全性）
            mangle: false,
            headerIds: false
        });

        // 解析Markdown
        let html = marked.parse(text);

        // 添加一些额外的样式类
        html = html
            .replace(/<h1>/g, '<h1 class="markdown-h1">')
            .replace(/<h2>/g, '<h2 class="markdown-h2">')
            .replace(/<h3>/g, '<h3 class="markdown-h3">')
            .replace(/<p>/g, '<p class="markdown-p">')
            .replace(/<ul>/g, '<ul class="markdown-ul">')
            .replace(/<ol>/g, '<ol class="markdown-ol">')
            .replace(/<li>/g, '<li class="markdown-li">')
            .replace(/<code>/g, '<code class="markdown-code">')
            .replace(/<pre><code>/g, '<pre class="markdown-pre"><code class="markdown-code-block">')
            .replace(/<blockquote>/g, '<blockquote class="markdown-blockquote">')
            .replace(/<a /g, '<a class="markdown-link" target="_blank" ');

        return html;
    }

    // 初始化欢迎消息为Markdown
    initWelcomeMessage() {
        const chatContainer = document.getElementById('chatContainer');
        const welcomeMessage = chatContainer.querySelector('.message-assistant .message-text');
        if (welcomeMessage) {
            const markdownText = `你好！我是 Agent 智能助手（高级版）。我可以帮助你：

- 回答问题和进行对话
- 执行计算任务
- 查询日期和时间
- 搜索信息

**高级功能：**

- 思维链推理 - 展示思考过程
- ReAct模式 - 智能工具选择
- 自我反思 - 自动优化回答
- 长对话摘要 - 保持上下文记忆

试试问我一些问题吧！`;

            welcomeMessage.innerHTML = this.parseMarkdown(markdownText);
        }
    }

    // 处理文件选择
    handleFileSelect(e) {
        const files = e.target.files;
        this.addFiles(files);
        e.target.value = ''; // 清空文件输入，允许重新选择相同文件
    }

    // 添加文件到预览区域
    addFiles(files) {
        if (!files || files.length === 0) return;

        for (const file of files) {
            // 创建文件对象
            const fileObj = {
                id: Date.now() + Math.random().toString(36).substr(2, 9),
                file: file,
                name: file.name,
                size: file.size,
                type: file.type,
                uploading: false
            };

            this.uploadedFiles.push(fileObj);
            this.addFilePreview(fileObj);
        }

        this.updateFilePreviewArea();
    }

    // 添加文件预览项
    addFilePreview(fileObj) {
        const previewItem = document.createElement('div');
        previewItem.className = 'file-preview-item';
        previewItem.dataset.id = fileObj.id;

        if (fileObj.type && fileObj.type.startsWith('image/')) {
            const imgPreview = document.createElement('img');
            imgPreview.src = URL.createObjectURL(fileObj.file);
            imgPreview.style.width = '40px';
            imgPreview.style.height = '40px';
            imgPreview.style.objectFit = 'cover';
            imgPreview.style.borderRadius = '4px';
            previewItem.appendChild(imgPreview);
        } else {
            const fileIcon = document.createElement('span');
            fileIcon.className = 'file-icon';
            fileIcon.textContent = this.getFileIcon(fileObj.name);
            previewItem.appendChild(fileIcon);
        }

        const fileName = document.createElement('span');
        fileName.className = 'file-name';
        fileName.textContent = fileObj.name;
        fileName.title = fileObj.name;

        const fileSize = document.createElement('span');
        fileSize.className = 'file-size';
        fileSize.textContent = this.formatFileSize(fileObj.size);

        const removeBtn = document.createElement('button');
        removeBtn.className = 'file-remove-btn';
        removeBtn.textContent = '×';
        removeBtn.title = '移除';
        removeBtn.addEventListener('click', () => {
            this.removeFile(fileObj.id);
        });

        previewItem.appendChild(fileName);
        previewItem.appendChild(fileSize);
        previewItem.appendChild(removeBtn);

        this.filePreviewList.appendChild(previewItem);
    }

    // 移除文件
    removeFile(fileId) {
        // 从数组中移除
        this.uploadedFiles = this.uploadedFiles.filter(f => f.id !== fileId);

        // 从DOM中移除预览项
        const previewItem = this.filePreviewList.querySelector(`[data-id="${fileId}"]`);
        if (previewItem) {
            previewItem.remove();
        }

        this.updateFilePreviewArea();
    }

    // 更新文件预览区域显示状态
    updateFilePreviewArea() {
        if (this.uploadedFiles.length > 0) {
            this.filePreviewArea.style.display = 'block';
        } else {
            this.filePreviewArea.style.display = 'none';
        }
    }

    // 获取文件图标
    getFileIcon(fileName) {
        const ext = fileName.split('.').pop().toLowerCase();
        const iconMap = {
            // 图片
            'jpg': '🖼️', 'jpeg': '🖼️', 'png': '🖼️', 'gif': '🖼️', 'bmp': '🖼️',
            'webp': '🖼️', 'svg': '🖼️',
            // 文档
            'pdf': '📄', 'doc': '📄', 'docx': '📄', 'txt': '📄', 'rtf': '📄',
            'odt': '📄', 'pages': '📄',
            // 表格
            'xls': '📊', 'xlsx': '📊', 'csv': '📊', 'ods': '📊', 'numbers': '📊',
            // 演示文稿
            'ppt': '📽️', 'pptx': '📽️', 'odp': '📽️', 'key': '📽️',
            // 压缩包
            'zip': '📦', 'rar': '📦', '7z': '📦', 'tar': '📦', 'gz': '📦',
            // 代码
            'js': '💻', 'ts': '💻', 'html': '💻', 'css': '💻', 'java': '💻',
            'py': '💻', 'c': '💻', 'cpp': '💻', 'h': '💻', 'go': '💻',
            'rs': '💻', 'php': '💻', 'rb': '💻', 'json': '💻', 'xml': '💻',
            'yaml': '💻', 'yml': '💻',
            // 音频
            'mp3': '🎵', 'wav': '🎵', 'flac': '🎵', 'aac': '🎵', 'ogg': '🎵',
            'm4a': '🎵',
            // 视频
            'mp4': '🎬', 'avi': '🎬', 'mkv': '🎬', 'mov': '🎬', 'wmv': '🎬',
            'webm': '🎬',
            // 其他
            'default': '📎'
        };

        return iconMap[ext] || iconMap['default'];
    }

    // 格式化文件大小
    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }

    // 上传文件到服务器
    async uploadFile(fileObj) {
        const formData = new FormData();
        formData.append('file', fileObj.file);
        formData.append('sessionId', this.sessionId);

        try {
            const response = await fetch('/api/v1/upload', {
                method: 'POST',
                body: formData
            });

            const result = await response.json();
            if (result.success) {
                return result.fileInfo;
            } else {
                throw new Error(result.error || '上传失败');
            }
        } catch (error) {
            console.error('文件上传失败:', error);
            throw error;
        }
    }

    // 创建消息底部操作区
    createMessageFooter(sender) {
        const footer = document.createElement('div');
        footer.className = 'message-footer';
        
        const timeSpan = document.createElement('span');
        timeSpan.className = 'message-time';
        const now = new Date();
        timeSpan.textContent = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
        footer.appendChild(timeSpan);
        
        const actionsDiv = document.createElement('div');
        actionsDiv.className = 'message-actions';
        
        const copyBtn = document.createElement('button');
        copyBtn.className = 'action-btn';
        copyBtn.title = '复制内容';
        copyBtn.innerHTML = '📋';
        copyBtn.onclick = () => {
            const wrapper = footer.closest('.message-content');
            if (wrapper) {
                const textDiv = wrapper.querySelector('.message-text');
                if (textDiv) {
                    navigator.clipboard.writeText(textDiv.innerText).then(() => {
                        const originalHtml = copyBtn.innerHTML;
                        copyBtn.innerHTML = '✅';
                        setTimeout(() => { copyBtn.innerHTML = originalHtml; }, 2000);
                    });
                }
            }
        };
        actionsDiv.appendChild(copyBtn);
        
        if (sender === 'assistant') {
            const likeBtn = document.createElement('button');
            likeBtn.className = 'action-btn';
            likeBtn.title = '点赞';
            likeBtn.innerHTML = '👍';
            likeBtn.onclick = () => {
                likeBtn.style.color = 'var(--accent-cyan)';
            };
            actionsDiv.appendChild(likeBtn);
            
            const regenBtn = document.createElement('button');
            regenBtn.className = 'action-btn';
            regenBtn.title = '重新生成';
            regenBtn.innerHTML = '🔄';
            regenBtn.onclick = () => {
                // 仅作为演示，实际需调用重试接口
                regenBtn.style.transform = 'rotate(180deg)';
                setTimeout(() => { regenBtn.style.transform = ''; }, 500);
            };
            actionsDiv.appendChild(regenBtn);
        }
        
        footer.appendChild(actionsDiv);
        return footer;
    }

    // 覆盖addMessageToChat方法，添加附件显示支持
    addMessageToChat(text, sender, trace = null, attachments = null) {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'message message-' + sender;

        const avatar = document.createElement('div');
        avatar.className = 'message-avatar';
        avatar.textContent = sender === 'user' ? '👤' : '🤖';

        const content = document.createElement('div');
        content.className = 'message-content';

        const textDiv = document.createElement('div');
        textDiv.className = 'message-text';

        // 如果是助手消息，解析Markdown
        if (sender === 'assistant') {
            textDiv.innerHTML = this.parseMarkdown(text);
        } else {
            // 用户消息保持原样
            textDiv.textContent = text;
        }

        content.appendChild(textDiv);

        // 显示附件
        if (attachments && attachments.length > 0) {
            const attachmentsDiv = document.createElement('div');
            attachmentsDiv.className = 'message-attachments';

            attachments.forEach(attachment => {
                const attachmentDiv = document.createElement('div');
                attachmentDiv.className = 'message-attachment';
                attachmentDiv.style.display = 'flex';
                attachmentDiv.style.alignItems = 'center';
                attachmentDiv.style.gap = '0.5rem';
                attachmentDiv.style.padding = '0.5rem';
                attachmentDiv.style.marginTop = '0.5rem';
                attachmentDiv.style.backgroundColor = 'rgba(0, 0, 0, 0.2)';
                attachmentDiv.style.border = '1px solid var(--border-color)';
                attachmentDiv.style.borderRadius = '8px';

                if (attachment.type && attachment.type.startsWith('image/')) {
                    // 图片展示
                    attachmentDiv.style.flexDirection = 'column';
                    attachmentDiv.style.alignItems = 'flex-start';
                    attachmentDiv.style.cursor = 'pointer';
                    
                    const img = document.createElement('img');
                    img.src = attachment.url;
                    img.alt = attachment.name;
                    img.style.maxWidth = '200px';
                    img.style.maxHeight = '200px';
                    img.style.borderRadius = '4px';
                    img.style.objectFit = 'contain';
                    
                    // 点击放大图片
                    img.onclick = (e) => {
                        e.stopPropagation();
                        this.showImageModal(attachment.url);
                    };

                    const infoDiv = document.createElement('div');
                    infoDiv.style.display = 'flex';
                    infoDiv.style.width = '100%';
                    infoDiv.style.justifyContent = 'space-between';
                    infoDiv.style.fontSize = '0.8rem';
                    infoDiv.style.color = 'var(--text-muted)';
                    
                    const nameSpan = document.createElement('span');
                    nameSpan.textContent = attachment.name;
                    nameSpan.style.overflow = 'hidden';
                    nameSpan.style.textOverflow = 'ellipsis';
                    nameSpan.style.whiteSpace = 'nowrap';
                    nameSpan.style.maxWidth = '150px';
                    
                    const sizeSpan = document.createElement('span');
                    sizeSpan.textContent = this.formatFileSize(attachment.size);
                    
                    infoDiv.appendChild(nameSpan);
                    infoDiv.appendChild(sizeSpan);
                    
                    attachmentDiv.appendChild(img);
                    attachmentDiv.appendChild(infoDiv);
                } else {
                    // 普通文件展示
                    const iconSpan = document.createElement('span');
                    iconSpan.className = 'file-icon';
                    iconSpan.textContent = '📄';

                    const nameSpan = document.createElement('span');
                    nameSpan.className = 'file-name';
                    nameSpan.style.flex = '1';
                    nameSpan.style.overflow = 'hidden';
                    nameSpan.style.textOverflow = 'ellipsis';
                    nameSpan.style.whiteSpace = 'nowrap';
                    nameSpan.textContent = attachment.name;

                    const sizeSpan = document.createElement('span');
                    sizeSpan.className = 'file-size';
                    sizeSpan.style.color = 'var(--text-muted)';
                    sizeSpan.style.fontSize = '0.8rem';
                    sizeSpan.style.marginLeft = 'auto';
                    sizeSpan.textContent = this.formatFileSize(attachment.size);

                    attachmentDiv.appendChild(iconSpan);
                    attachmentDiv.appendChild(nameSpan);
                    attachmentDiv.appendChild(sizeSpan);
                }
                
                attachmentsDiv.appendChild(attachmentDiv);
            });

            content.appendChild(attachmentsDiv);
        }

        // 如果是助手消息且有执行链路，添加执行链路显示
        if (sender === 'assistant' && trace && trace.length > 0 && this.showTrace) {
            const traceDiv = this.createTraceDiv(trace);
            content.appendChild(traceDiv);
        }

        // 添加底部操作区
        const footer = this.createMessageFooter(sender);
        content.appendChild(footer);

        messageDiv.appendChild(avatar);
        messageDiv.appendChild(content);

        this.chatContainer.appendChild(messageDiv);
        this.scrollToBottom();
    }

    // 显示图片模态框
    showImageModal(imgUrl) {
        // 创建模态框
        let modal = document.getElementById('imageModal');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'imageModal';
            modal.className = 'modal';
            modal.style.display = 'none'; // 初始隐藏
            modal.style.alignItems = 'center';
            modal.style.justifyContent = 'center';
            modal.style.position = 'fixed';
            modal.style.top = '0';
            modal.style.left = '0';
            modal.style.width = '100%';
            modal.style.height = '100%';
            modal.style.zIndex = '2000';
            modal.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
            modal.style.opacity = '0';
            modal.style.transition = 'opacity 0.3s ease';
            
            const img = document.createElement('img');
            img.id = 'imageModalImg';
            img.style.maxWidth = '90%';
            img.style.maxHeight = '90%';
            img.style.objectFit = 'contain';
            img.style.boxShadow = '0 0 20px rgba(0,0,0,0.5)';
            img.style.transform = 'scale(0.9)';
            img.style.transition = 'transform 0.3s ease';
            
            const closeBtn = document.createElement('button');
            closeBtn.innerHTML = '×';
            closeBtn.style.position = 'absolute';
            closeBtn.style.top = '1rem';
            closeBtn.style.right = '2rem';
            closeBtn.style.background = 'none';
            closeBtn.style.border = 'none';
            closeBtn.style.color = 'white';
            closeBtn.style.fontSize = '3rem';
            closeBtn.style.cursor = 'pointer';
            
            modal.appendChild(img);
            modal.appendChild(closeBtn);
            
            // 点击关闭
            const closeModal = () => {
                modal.style.opacity = '0';
                img.style.transform = 'scale(0.9)';
                setTimeout(() => modal.style.display = 'none', 300);
            };
            closeBtn.onclick = closeModal;
            modal.onclick = (e) => {
                if (e.target === modal) {
                    closeModal();
                }
            };
            
            document.body.appendChild(modal);
        }
        
        const modalImg = document.getElementById('imageModalImg');
        modalImg.src = imgUrl;
        
        modal.style.display = 'flex';
        // 稍微延迟以触发动画
        setTimeout(() => {
            modal.style.opacity = '1';
            modalImg.style.transform = 'scale(1)';
        }, 10);
    }
}

// 立即初始化应用
console.log('正在初始化 AgentApp');
const app = new AgentApp();

// 添加键盘快捷键
document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + /显示帮助
    if ((e.ctrlKey || e.metaKey) && e.key === '/') {
        e.preventDefault();
        alert('键盘快捷键：\n- Enter：发送消息\n- Shift+Enter：换行\n- Ctrl+/或Cmd+/：显示帮助');
    }

    // Esc关闭模态框
    if (e.key === 'Escape') {
        const modals = document.querySelectorAll('.modal.show');
        modals.forEach(modal => modal.classList.remove('show'));
    }
});
