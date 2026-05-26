const API_BASE = '/api';
let currentUser = null;
let currentChatConsultId = null;

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
                alert('登录已过期，请重新登录');
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
    
    // 非医生角色禁止访问
    if (currentUser.role !== 'DOCTOR') {
        alert('无权访问医生工作台');
        window.location.href = '/index.html';
        return;
    }

    // 渲染头部和侧边栏
    document.getElementById('docHeaderName').textContent = currentUser.realName || currentUser.username + ' 医生';
    document.getElementById('docNameDisplay').textContent = currentUser.realName || currentUser.username;
    
    // 初始化默认加载
    loadDoctorInfo();
    loadDoctorSchedule();
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
        loadDoctorSchedule();
    } else if (panelId === 'panelConsult') {
        document.getElementById('nav-consult').classList.add('active');
        backToConsultList();
        loadDoctorConsults();
    }
}

// ==================== 员工信息与排班 ====================

function loadDoctorInfo() {
    // 拉取后端医生详细信息接口（假设按当前系统结构）
    ajaxGet('/doctors/detail/' + currentUser.userId, function(res) {
        if (res.code === 200 && res.data) {
            document.getElementById('docTitleDisplay').textContent = res.data.title || '门诊医生';
            document.getElementById('docDept').textContent = res.data.departmentName || '未分配';
            document.getElementById('docEmpNo').textContent = 'DOC' + res.data.id.toString().padStart(4, '0');
            document.getElementById('docAvatar').textContent = (currentUser.realName || currentUser.username).charAt(0);
        }
    });

    // 顺便拉一下问诊统计
    ajaxGet('/consultations/my-list?userType=DOCTOR&pageNum=1&pageSize=100', function(res) {
        if(res.code === 200) {
            const list = res.data.list || [];
            const pending = list.filter(c => c.status === 'PENDING').length;
            const done = list.filter(c => c.status === 'COMPLETED').length;
            
            document.getElementById('statPending').textContent = pending;
            document.getElementById('statDone').textContent = done;
            
            if (pending > 0) {
                const badge = document.getElementById('pendingConsultCount');
                badge.style.display = 'inline-block';
                badge.textContent = pending > 99 ? '99+' : pending;
            }
        }
    });
}

function loadDoctorSchedule() {
    const dateInput = document.getElementById('scheduleDateFilter').value;
    let url = '/schedules/doctor/' + currentUser.userId;
    
    ajaxGet(url, function(res) {
        const tbody = document.getElementById('scheduleTableBody');
        tbody.innerHTML = '';
        if (res.code === 200 && res.data && res.data.length > 0) {
            // 前端基于选择的日期过滤
            let list = res.data;
            if (dateInput) {
                list = list.filter(item => item.scheduleDate === dateInput);
            }
            if (list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 20px; color:#64748b;">该日期没有您的排班</td></tr>';
                return;
            }
            list.forEach(item => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${item.scheduleDate}</td>
                    <td><span class="badge ${item.timeSlot==='MORNING'?'badge-blue':'badge-green'}">${item.timeSlot === 'MORNING' ? '上午' : '下午'}</span></td>
                    <td>${item.totalLimit}</td>
                    <td>${item.bookedCount}</td>
                    <td><span style="color:${item.status === 'ACTIVE' ? '#10b981' : '#f43f5e'}">${item.status === 'ACTIVE' ? '正常挂号' : '停诊'}</span></td>
                `;
                tbody.appendChild(tr);
            });
        } else {
            tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 20px; color:#64748b;">暂无排班数据</td></tr>';
        }
    });
}

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
                if (c.status === 'PENDING') {
                    statusBadge = '<span class="badge" style="background:#fef08a; color:#854d0e">等待接诊</span>';
                    actionBtn = `<button class="btn-sm btn-reply" onclick="startChat(${c.id}, '${c.patientName}', '${c.symptomDescription}')">开始接诊</button>`;
                } else if (c.status === 'IN_PROGRESS') {
                    statusBadge = '<span class="badge badge-blue">正在问诊</span>';
                    actionBtn = `<button class="btn-sm btn-reply" onclick="startChat(${c.id}, '${c.patientName}', '${c.symptomDescription}')">继续沟通</button>`;
                } else if (c.status === 'COMPLETED') {
                    statusBadge = '<span class="badge badge-green">已完成</span>';
                    actionBtn = `<button class="btn-sm" style="background:#e2e8f0; color:#475569" onclick="startChat(${c.id}, '${c.patientName}', '${c.symptomDescription}')">查看记录</button>`;
                }

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td style="font-weight: 500">${c.patientName}</td>
                    <td style="color:#64748b; max-width: 250px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap" title="${c.symptomDescription}">${c.symptomDescription}</td>
                    <td>${c.createTime}</td>
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

function startChat(consultId, patientName, symptom) {
    currentChatConsultId = consultId;
    document.getElementById('consultListView').style.display = 'none';
    document.getElementById('consultChatView').style.display = 'block';
    
    document.getElementById('chatPatientName').textContent = '与 ' + patientName + ' 沟通中';
    document.getElementById('chatSymptomDesc').textContent = '主诉: ' + symptom;
    
    loadChatMessages();
    // 简易轮询新消息（实际工程应使用WebSocket）
    window.chatTimer = setInterval(loadChatMessages, 2000);
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
            // 此处由于原生DOM重绘，如果做得很精细可以检查增量，为简单起见全量刷新并滚到底部
            const oldScroll = box.scrollTop;
            const isAtBottom = box.scrollHeight - box.scrollTop <= box.clientHeight + 50;

            let html = '<div style="text-align:center; color:#94a3b8; font-size:12px; margin: 20px 0;">---------- 问诊开始 ----------</div>';
            res.data.forEach(msg => {
                if (msg.senderType === 'DOCTOR') {
                    html += `
                        <div class="msg-bubble msg-doctor">
                            ${msg.content}
                            <div style="font-size:10px; opacity:0.7; margin-top:4px; text-align:right">${new Date(msg.createTime).toLocaleTimeString()}</div>
                        </div>`;
                } else {
                    html += `
                        <div class="msg-bubble msg-patient">
                            ${msg.content}
                            <div style="font-size:10px; opacity:0.5; margin-top:4px;">${new Date(msg.createTime).toLocaleTimeString()}</div>
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
    
    ajaxPost('/consultations/' + currentChatConsultId + '/messages', { content: content }, function(res) {
        if(res.code === 200) {
            input.value = '';
            loadChatMessages();
        }
    });
}

function finishConsult() {
    if(confirm('结束问诊后患者将不能再发送消息。是否确认结束？')) {
        ajaxPost('/consultations/' + currentChatConsultId + '/status?status=COMPLETED', {}, function(res) {
            if(res.code === 200) {
                alert('问诊已结束，请前往开具处方（如需）');
                backToConsultList();
            } else {
                alert(res.message);
            }
        });
    }
}
