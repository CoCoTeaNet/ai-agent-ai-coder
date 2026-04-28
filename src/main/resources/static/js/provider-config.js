// 提供商配置管理 JavaScript

const API_BASE = '/api/providers';

// 初始化
document.addEventListener('DOMContentLoaded', function() {
    loadProviders();
    loadProviderTypes();
    setupForm();
});

// 加载提供商列表
function loadProviders() {
    const listDiv = document.getElementById('providerList');

    fetch(`${API_BASE}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                listDiv.innerHTML = renderProviders(data.data);
                updateStats(data.data);
            } else {
                listDiv.innerHTML = `<p style="text-align: center; color: #666; padding: 40px;">${data.message || '加载失败'}</p>`;
            }
        })
        .catch(error => {
            console.error('加载提供商失败:', error);
            listDiv.innerHTML = `<p style="text-align: center; color: #dc2626; padding: 40px;">网络请求失败，请检查连接</p>`;
        });
}

// 渲染提供商列表
function renderProviders(providers) {
    if (!providers || providers.length === 0) {
        return `<p style="text-align: center; color: #666; padding: 40px;">暂无配置</p>`;
    }

    return providers.map(provider => `
        <div class="provider-card ${!provider.enabled ? 'disabled' : ''} ${provider.isDefault ? 'default' : ''}"
             data-id="${provider.id}"
             style="border-left-color: ${getProviderColor(provider.type)};">
            <div class="provider-header">
                <div>
                    <h2 class="provider-name">${provider.name}</h2>
                    <span class="provider-type">${provider.type}</span>
                    ${provider.isDefault ? `
                        <span class="status-badge default">默认配置</span>
                    ` : ''}
                    <span class="status-badge ${provider.enabled ? 'enabled' : 'disabled'}">
                        ${provider.enabled ? '已启用' : '已禁用'}
                    </span>
                </div>
            </div>
            <div class="provider-details">
                <div class="provider-detail">
                    <label>模型名称:</label>
                    <span>${provider.modelName}</span>
                </div>
                <div class="provider-detail">
                    <label>API 地址:</label>
                    <span style="font-family: monospace;">${provider.baseUrl || '-'}</span>
                </div>
                <div class="provider-detail">
                    <label>API Key:</label>
                    <span>${provider.maskedApiKey || '-'}</span>
                </div>
                <div class="provider-detail">
                    <label>温度:</label>
                    <span>${provider.temperature}</span>
                </div>
                <div class="provider-detail">
                    <label>最大 Token:</label>
                    <span>${provider.maxTokens}</span>
                </div>
                <div class="provider-detail">
                    <label>更新时间:</label>
                    <span>${formatDate(provider.updatedAt)}</span>
                </div>
            </div>
            <div class="provider-actions">
                <button class="btn btn-primary btn-small" onclick="editProvider('${provider.id}')">编辑</button>
                ${provider.isDefault ? `
                    <span class="status-badge default">默认配置</span>
                ` : `
                    <button class="btn btn-success btn-small" onclick="setDefault('${provider.id}')">设为默认</button>
                `}
                <button class="btn ${provider.enabled ? 'btn-secondary' : 'btn-primary'} btn-small"
                        onclick="toggleEnable('${provider.id}', ${!provider.enabled})">
                    ${provider.enabled ? '禁用' : '启用'}
                </button>
                <button class="btn btn-secondary btn-small" onclick="testConnection('${provider.id}')">测试连接</button>
                ${!provider.isDefault ? `
                    <button class="btn btn-danger btn-small" onclick="deleteProvider('${provider.id}')">删除</button>
                ` : ''}
            </div>
        </div>
    `).join('');
}

// 获取提供商类型颜色
function getProviderColor(type) {
    const colors = {
        'OLLAMA': '#f59e0b',
        'OPENAI': '#10b981',
        'ANTHROPIC': '#3b82f6',
        'GOOGLE': '#8b5cf6',
        'DEEPSEEK': '#ef4444',
        'VOLCENGINE': '#06b6d4',
        'CUSTOM': '#6b7280'
    };
    return colors[type] || '#667eea';
}

// 更新统计信息
function updateStats(providers) {
    const total = providers.length;
    const enabled = providers.filter(p => p.enabled).length;
    const disabled = total - enabled;

    document.getElementById('totalProviders').textContent = total;
    document.getElementById('enabledProviders').textContent = enabled;
    document.getElementById('disabledProviders').textContent = disabled;
}

// 加载提供商类型
function loadProviderTypes() {
    fetch(`${API_BASE}/types`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const select = document.getElementById('providerType');
                select.innerHTML = data.data.map(type => `
                    <option value="${type}">${type}</option>
                `).join('');
            }
        });
}

// 根据类型更新表单
function updateFormForType() {
    const providerType = document.getElementById('providerType');
    const type = providerType ? providerType.value : 'OLLAMA';

    const baseUrlInput = document.getElementById('baseUrl');
    const modelInput = document.getElementById('modelName');
    const apiKeyInput = document.getElementById('apiKey');

    // 安全设置值的辅助函数
    const setValue = (input, value) => {
        if (input) input.value = value;
    };

    const setDisabled = (input, disabled) => {
        if (input) input.disabled = disabled;
    };

    // 设置默认值
    switch (type) {
        case 'OLLAMA':
            setValue(baseUrlInput, 'http://localhost:11434');
            setValue(modelInput, 'llama2');
            setValue(apiKeyInput, '');
            setDisabled(apiKeyInput, true);
            break;
        case 'OPENAI':
            setValue(baseUrlInput, 'https://api.openai.com');
            setValue(modelInput, 'gpt-4-turbo');
            setValue(apiKeyInput, '');
            setDisabled(apiKeyInput, false);
            break;
        case 'ANTHROPIC':
            setValue(baseUrlInput, 'https://api.anthropic.com');
            setValue(modelInput, 'claude-3-opus-20250219');
            setValue(apiKeyInput, '');
            setDisabled(apiKeyInput, false);
            break;
        case 'GOOGLE':
            setValue(baseUrlInput, 'https://generativelanguage.googleapis.com');
            setValue(modelInput, 'gemini-pro');
            setValue(apiKeyInput, '');
            setDisabled(apiKeyInput, false);
            break;
        case 'DEEPSEEK':
            setValue(baseUrlInput, 'https://api.deepseek.com');
            setValue(modelInput, 'deepseek-chat');
            setValue(apiKeyInput, '');
            setDisabled(apiKeyInput, false);
            break;
        case 'VOLCENGINE':
            setValue(baseUrlInput, 'https://ark.cn-beijing.volces.com/api/coding');
            setValue(modelInput, 'doubao-pro-32k');
            setValue(apiKeyInput, '');
            setDisabled(apiKeyInput, false);
            break;
        case 'CUSTOM':
            setValue(baseUrlInput, '');
            setValue(modelInput, 'custom-model');
            setValue(apiKeyInput, '');
            setDisabled(apiKeyInput, false);
            break;
        default:
            break;
    }
}

// 打开添加模态框
function openAddModal() {
    document.getElementById('providerModal').classList.add('active');
    document.getElementById('modalTitle').textContent = '添加新的模型提供商';
    document.getElementById('providerForm').reset();
    document.getElementById('providerId').value = '';
    document.getElementById('testResult').style.display = 'none';
    updateFormForType();
}

// 打开编辑模态框
function editProvider(id) {
    fetch(`${API_BASE}/${id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const provider = data.data;
                document.getElementById('providerModal').classList.add('active');
                const modalTitle = document.getElementById('modalTitle');
                if (modalTitle) modalTitle.textContent = '编辑提供商配置';

                const providerId = document.getElementById('providerId');
                if (providerId) providerId.value = provider.id || '';

                const providerName = document.getElementById('providerName');
                if (providerName) providerName.value = provider.name || '';

                const providerType = document.getElementById('providerType');
                if (providerType) providerType.value = provider.type || 'OLLAMA';

                const apiKey = document.getElementById('apiKey');
                if (apiKey) apiKey.value = '';

                const baseUrl = document.getElementById('baseUrl');
                if (baseUrl) baseUrl.value = provider.baseUrl || '';

                const modelName = document.getElementById('modelName');
                if (modelName) modelName.value = provider.modelName || '';

                const temperature = document.getElementById('temperature');
                if (temperature) temperature.value = provider.temperature || 0.7;

                const maxTokens = document.getElementById('maxTokens');
                if (maxTokens) maxTokens.value = provider.maxTokens || 2048;

                const topP = document.getElementById('topP');
                if (topP) topP.value = provider.topP || 0.9;

                const isEnabled = document.getElementById('isEnabled');
                if (isEnabled) isEnabled.checked = provider.enabled !== false;

                const setDefault = document.getElementById('setDefault');
                if (setDefault) setDefault.checked = provider.isDefault === true;

                const testResult = document.getElementById('testResult');
                if (testResult) testResult.style.display = 'none';

                updateFormForType();
            } else {
                showToast('加载配置失败', 'error');
            }
        });
}

// 关闭模态框
function closeModal() {
    document.getElementById('providerModal').classList.remove('active');
}

// 测试连接
function testConnection(id = null) {
    const testResultDiv = document.getElementById('testResult');
    testResultDiv.innerHTML = '<div class="loading-spinner" style="display: inline-block; width: 16px; height: 16px; border-width: 2px;"></div> 正在测试...';
    testResultDiv.className = 'test-result';
    testResultDiv.style.display = 'block';

    if (id) {
        // 测试已有的配置
        fetch(`${API_BASE}/${id}/test`)
            .then(response => response.json())
            .then(data => {
                testResultDiv.textContent = data.success ? '✅ 连接成功' : `❌ ${data.message}`;
                testResultDiv.className = `test-result ${data.success ? 'success' : 'error'}`;
            })
            .catch(() => {
                testResultDiv.textContent = '❌ 测试失败，请检查网络';
                testResultDiv.className = 'test-result error';
            });
    } else {
        // 测试当前表单配置
        let formData = collectFormData();
        fetch(`${API_BASE}/test`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
            .then(response => response.json())
            .then(data => {
                testResultDiv.textContent = data.success ? '✅ 配置有效' : `❌ ${data.message}`;
                testResultDiv.className = `test-result ${data.success ? 'success' : 'error'}`;
            })
            .catch(() => {
                testResultDiv.textContent = '❌ 测试失败';
                testResultDiv.className = 'test-result error';
            });
    }
}

// 删除配置
function deleteProvider(id) {
    if (!confirm('确定要删除此配置吗？此操作不可撤销。')) {
        return;
    }

    fetch(`${API_BASE}/${id}`, {
        method: 'DELETE'
    })
        .then(response => response.json())
        .then(data => {
            showToast(data.success ? '删除成功' : data.message, data.success ? 'success' : 'error');
            if (data.success) {
                loadProviders();
            }
        });
}

// 设为默认配置
function setDefault(id) {
    fetch(`${API_BASE}/${id}/default`, {
        method: 'POST'
    })
        .then(response => response.json())
        .then(data => {
            showToast(data.success ? '默认配置已更新' : data.message, data.success ? 'success' : 'error');
            if (data.success) {
                loadProviders();
            }
        });
}

// 切换启用/禁用状态
function toggleEnable(id, enable) {
    fetch(`${API_BASE}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ enabled: enable })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                loadProviders();
                showToast(`配置已${enable ? '启用' : '禁用'}`, 'success');
            } else {
                showToast(data.message, 'error');
            }
        });
}

// 收集表单数据
function collectFormData() {
    // 安全获取 DOM 元素值的辅助函数
    const getElementValue = (id, defaultValue = '') => {
        const element = document.getElementById(id);
        return element ? (element.value || defaultValue) : defaultValue;
    };

    const getElementChecked = (id, defaultValue = false) => {
        const element = document.getElementById(id);
        return element ? element.checked : defaultValue;
    };

    const getElementFloat = (id, defaultValue = 0.0) => {
        const element = document.getElementById(id);
        return element ? parseFloat(element.value) || defaultValue : defaultValue;
    };

    const getElementInt = (id, defaultValue = 0) => {
        const element = document.getElementById(id);
        return element ? parseInt(element.value) || defaultValue : defaultValue;
    };

    return {
        id: getElementValue('providerId', null),
        name: getElementValue('providerName'),
        type: getElementValue('providerType'),
        apiKey: getElementValue('apiKey', null),
        baseUrl: getElementValue('baseUrl', null),
        modelName: getElementValue('modelName'),
        isDefault: getElementChecked('setDefault'),
        enabled: getElementChecked('isEnabled', true),
        temperature: getElementFloat('temperature', 0.7),
        maxTokens: getElementInt('maxTokens', 2048),
        topP: getElementFloat('topP', 0.9)
    };
}

// 设置表单提交
function setupForm() {
    const form = document.getElementById('providerForm');
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        saveProvider();
    });
}

// 保存提供商配置
function saveProvider() {
    const formData = collectFormData();
    const isNew = !formData.id;
    const method = isNew ? 'POST' : 'PUT';
    const url = isNew ? API_BASE : `${API_BASE}/${formData.id}`;

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(formData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                closeModal();
                loadProviders();
                showToast(isNew ? '配置添加成功' : '配置更新成功', 'success');
            } else {
                showToast(data.message, 'error');
            }
        })
        .catch(error => {
            console.error('保存失败:', error);
            showToast('保存失败，请检查网络连接', 'error');
        });
}

// 显示 Toast 通知
function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type}`;
    toast.classList.add('active');

    setTimeout(() => {
        toast.classList.remove('active');
    }, 3000);
}

// 格式化日期
function formatDate(dateString) {
    if (!dateString) return '-';
    try {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now - date;
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));

        if (days === 0) {
            const hours = Math.floor(diff / (1000 * 60 * 60));
            if (hours === 0) {
                const minutes = Math.floor(diff / (1000 * 60));
                return minutes < 5 ? '刚刚' : `${minutes} 分钟前`;
            }
            return `${hours} 小时前`;
        } else if (days === 1) {
            return '昨天';
        } else if (days < 7) {
            return `${days} 天前`;
        } else {
            return date.toLocaleDateString('zh-CN');
        }
    } catch (e) {
        return dateString;
    }
}

// 点击模态框外部关闭
document.getElementById('providerModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeModal();
    }
});

// 键盘快捷键
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeModal();
    }
});
