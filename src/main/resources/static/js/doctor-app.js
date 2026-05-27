const API_BASE = '/api';
let currentUser = null;
let currentChatConsultId = null;

function escapeHtml(value) {
    return String(value ?? '').replace(/[&<>"']/g, ch => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[ch]));
}

function formatTime(value) {
    return value ? new Date(value).toLocaleTimeString() : '';
}

// ==================== 基础 AJAX 与初始化 ====================

function ajaxGet(url, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', API_BASE + url, true);
    const token = localStorage.getItem('token');
    if (token) xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) callback(JSON.parse(xhr.responseText));
            else if (xhr.status === 401 || xhr.status === 403) {
                alert('登录已过期或无权访问，请重新登录');
                window.location.href = '/index.html';
            }
        }
    };
    xhr.send();
}

function ajaxPost(url, data, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', API_BASE + url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    const token = localStorage.getItem('token');
    if (token) xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4 && xhr.status === 200) {
            callback(JSON.parse(xhr.responseText));
        }
    };
    xhr.send(JSON.stringify(data));
}

window.onload = function() {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
        window.location.href = '/index.html';
        return;
    }
    currentUser = JSON.parse(userStr);
    
    // 【任务2】权限重定向兜底 
    if (currentUser.role !== 'DOCTOR' && currentUser.role !== 'ROLE_DOCTOR') {
        alert('非法访问，仅限医生角色');
        window.location.href = '/index.html';
        return;
    }

    // 渲染头部
    document.getElementById('docHeaderName').textContent = currentUser.realName || currentUser.username + ' 医生';
    document.getElementById('docNameDisplay').textContent = currentUser.realName || currentUser.username;
    
    // 【任务1】一次性聚合渲染 (One-Shot Rendering)
    loadDoctorWorkspaceData('');
};

function doctorLogout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/index.html';
}

// ==================== 视图切换逻辑 ====================

function showPanel(panelId) {
    document.querySelectorAll('.panel').forEach(p => p.style.display = 'none');
    document.getElementById(panelId).style.display = 'block';

    document.querySelectorAll('.doctor-nav-menu a').forEach(a => a.classList.remove('active'));
    
    if(panelId === 'panelSchedule') {
        document.getElementById('nav-schedule').classList.add('active');
        // 切回时保持当前日历日期加载
        const dateInput = document.getElementById('scheduleDateFilter').value;
        loadDoctorWorkspaceData(dateInput);
    } else if (panelId === 'panelConsult') {
        document.getElementById('nav-consult').classList.add('active');
        backToConsultList();
        loadDoctorConsults(); // 问诊大厅依然走独立接口
    }
}

// ==================== 聚合数据获取与日历交互 ====================

/**
 * 聚合拉取工作台数据
 * parameter: filterDate (可选，日历筛选条件)
 */
function loadDoctorWorkspaceData(filterDate) {
    const tbody = document.getElementById('scheduleTableBody');
    tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 40px; color:#94a3b8;">正在为您聚合加载最新数据...</td></tr>';

    let url = '/doctor-workspace/data';
    if (filterDate) {
        url += '?date=' + filterDate;
    }

    ajaxGet(url, function(res) {
        if (res.code === 200 && res.data) {
            const workspaceData = res.data;

            // 1. 渲染 baseInfo
            if (workspaceData.baseInfo) {
                document.getElementById('docTitleDisplay').textContent = workspaceData.baseInfo.title || '门诊医生';
                document.getElementById('docDept').textContent = workspaceData.baseInfo.departmentName || '未分配';
                document.getElementById('docEmpNo').textContent = 'DOC' + workspaceData.baseInfo.id.toString().padStart(4, '0');
                document.getElementById('docAvatar').textContent = (workspaceData.baseInfo.realName || currentUser.username).charAt(0);
                document.getElementById('docPhone').textContent = workspaceData.baseInfo.phone || '未添加';
            }

            // 2. 渲染 todayStats
            if (workspaceData.todayStats) {
                const pending = workspaceData.todayStats.pendingConsults || 0;
                const done = workspaceData.todayStats.completedConsults || 0;
                document.getElementById('statPending').textContent = pending;
                document.getElementById('statDone').textContent = done;

                const badge = document.getElementById('pendingConsultCount');
                if (pending > 0) {
                    badge.style.display = 'inline-block';
                    badge.textContent = pending > 99 ? '99+' : pending;
                } else {
                    badge.style.display = 'none';
                }
            }

            // 3. 渲染 schedules (带有丰富色彩状态的动态表格)
            tbody.innerHTML = '';
            const schedules = workspaceData.schedules || [];
            
            if (schedules.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 40px;"><div class="empty-state" style="color: #64748b; font-size: 15px; display: flex; flex-direction: column; align-items: center; gap: 10px;"><svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>目前您在选定日期内暂无排班记录</div></td></tr>';
                return;
            }

            schedules.forEach(item => {
                const tr = document.createElement('tr');
                
                // 【任务3】日历组件交互拦截与排班状态的色彩区分
                let statusHtml = '';
                if (item.status === 0) {
                    statusHtml = '<span style="color:#ef4444; font-weight:600">停诊</span>';
                } else if ((item.availableSlots || 0) <= 0) {
                    statusHtml = '<span style="color:#ef4444; font-weight:600">已停诊/约满</span>'; // 红色
                } else if (item.status === 1) {
                    statusHtml = '<span style="color:#10b981; font-weight:600">可预约</span>'; // 绿色有号
                } else {
                    statusHtml = '<span style="color:#94a3b8;">' + (item.status || '已过期') + '</span>'; // 置灰
                }

                const timeSlotBadge = item.timeSlot === 'MORNING' ? 'badge-blue' : 'badge-green';
                const timeSlotText = item.timeSlot === 'MORNING' ? '上午 (08:00-12:00)' : '下午 (14:00-18:00)';

                tr.innerHTML = `
                    <td style="font-weight: 500">${item.workDate}</td>
                    <td><span class="badge ${timeSlotBadge}">${timeSlotText}</span></td>
                    <td><span style="color:#64748b">${item.totalSlots || 0}</span></td>
                    <td><span style="color:#0f172a; font-weight: 500">${item.bookedSlots || 0}</span></td>
                    <td>${statusHtml}</td>
                `;
                tbody.appendChild(tr);
            });

        } else {
            // 如果是因为后端还没有实现这个聚合接口等原因，友好提示
            tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; padding: 20px; color:#ef4444;">数据拉取失败: ${res.message || '后端 /api/doctor-workspace/data 接口暂未部署'}</td></tr>`;
        }
    });
}

// 绑定日期选择器的交互事件
function handleScheduleDateChange() {
    const dateInput = document.getElementById('scheduleDateFilter').value;
    // 拦截日历选择动作后，只针对右半部分的 schedule 发起局部重绘
    loadDoctorWorkspaceData(dateInput);
}

// 修改掉之前的旧绑定，指引向我们新的聚合处理逻辑
document.getElementById('scheduleDateFilter').onchange = handleScheduleDateChange;


// ==================== 问诊大厅与聊天 ====================

function loadDoctorConsults() {
    const status = document.getElementById('consultStatusFilter').value;
    let url = `/consultations/my-list?userType=DOCTOR&pageNum=1&pageSize=50`;
    if (status) url += `&status=${status}`;

    ajaxGet(url, function(res) {
        const tbody = document.getElementById('consultTableBody');
        tbody.innerHTML = '';
        if (res.code === 200 && res.data && res.data.list.length > 0) {
            res.data.list.forEach(c => {
                let statusBadge = '';
                let actionBtn = '';
                if (c.status === 0) {
                    statusBadge = '<span class="badge" style="background:#fef08a; color:#854d0e">等待接诊</span>';
                    actionBtn = `<button class="btn-sm btn-reply" onclick='startChat(${c.id}, ${JSON.stringify(c.patientName || '')}, ${JSON.stringify(c.symptomDescription || '')}, ${c.status})'>开始接诊</button>`;
                } else if (c.status === 1) {
                    statusBadge = '<span class="badge badge-blue">正在问诊</span>';
                    actionBtn = `<button class="btn-sm btn-reply" onclick='startChat(${c.id}, ${JSON.stringify(c.patientName || '')}, ${JSON.stringify(c.symptomDescription || '')}, ${c.status})'>继续沟通</button>`;
                } else if (c.status === 2) {
                    statusBadge = '<span class="badge badge-green">已完成</span>';
                    actionBtn = `<button class="btn-sm" style="background:#e2e8f0; color:#475569" onclick='startChat(${c.id}, ${JSON.stringify(c.patientName || '')}, ${JSON.stringify(c.symptomDescription || '')}, ${c.status})'>查看记录</button>`;
                }

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 500">${escapeHtml(c.patientName || '')}</td>
                    <td style="color:#64748b; max-width: 250px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap" title="${escapeHtml(c.symptomDescription || '')}">${escapeHtml(c.symptomDescription || '')}</td>
                    <td>${c.createdAt ? c.createdAt.substring(0, 16).replace('T', ' ') : ''}</td>
                    <td>${statusBadge}</td>
                    <td>${actionBtn}</td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 40px; color:#64748b;">暂无对应状态的问诊记录</td></tr>';
        }
    })
}

function startChat(consultId, patientName, symptom, status) {
    currentChatConsultId = consultId;
    document.getElementById('consultListView').style.display = 'none';
    document.getElementById('consultChatView').style.display = 'block';
    
    document.getElementById('chatPatientName').textContent = '与 ' + patientName + ' 沟通中';
    document.getElementById('chatSymptomDesc').textContent = '主诉: ' + symptom;

    const afterAccept = function() {
        loadChatMessages();
        window.chatTimer = setInterval(loadChatMessages, 2000);
    };

    if (status === 0) {
        ajaxPost('/consultations/' + currentChatConsultId + '/accept', {}, function(res) {
            if (res.code !== 200) alert(res.message || '接诊失败');
            afterAccept();
        });
    } else {
        afterAccept();
    }
}

function backToConsultList() {
    currentChatConsultId = null;
    clearInterval(window.chatTimer);
    document.getElementById('consultChatView').style.display = 'none';
    document.getElementById('consultListView').style.display = 'block';
    loadDoctorConsults();
}

function loadChatMessages() {
    if(!currentChatConsultId) return;
    ajaxGet('/consultations/' + currentChatConsultId + '/messages', function(res) {
        if(res.code === 200) {
            const box = document.getElementById('chatMessageArea');
            const oldScroll = box.scrollTop;
            const isAtBottom = box.scrollHeight - box.scrollTop <= box.clientHeight + 50;

            let html = '<div style="text-align:center; color:#94a3b8; font-size:12px; margin: 20px 0;">---------- 问诊开始 ----------</div>';
            res.data.forEach(msg => {
                if (msg.senderType === 'DOCTOR') {
                    html += `
                        <div class="msg-bubble msg-doctor">
                            ${escapeHtml(msg.content)}
                            <div style="font-size:10px; opacity:0.7; margin-top:4px; text-align:right">${formatTime(msg.createdAt)}</div>
                        </div>`;
                } else {
                    html += `
                        <div class="msg-bubble msg-patient">
                            ${escapeHtml(msg.content)}
                            <div style="font-size:10px; opacity:0.5; margin-top:4px;">${formatTime(msg.createdAt)}</div>
                        </div>`;
                }
            });
            box.innerHTML = html;

            if (isAtBottom) box.scrollTop = box.scrollHeight;
            else box.scrollTop = oldScroll;
        }
    });
}

function doctorSendMessage() {
    if (!currentChatConsultId) return;
    const input = document.getElementById('chatDocInput');
    const content = input.value.trim();
    if (!content) return;
    
    ajaxPost('/consultations/' + currentChatConsultId + '/message', { content: content, messageType: 'TEXT' }, function(res) {
        if(res.code === 200) {
            input.value = '';
            loadChatMessages();
        }
    });
}

function finishConsult() {
    if(confirm('结束问诊后患者将不能再发送消息。是否确认结束？')) {
        ajaxPost('/consultations/' + currentChatConsultId + '/finish', {}, function(res) {
            if(res.code === 200) {
                alert('问诊已结束，请前往开具处方（如需）');
                backToConsultList();
            } else {
                alert(res.message);
            }
        });
    }
}
