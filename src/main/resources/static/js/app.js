// 全局变量
let currentUser = null;
const API_BASE = '/api';

// ==================== AJAX工具函数 ====================

/**
 * 封装AJAX GET请求
 */
function ajaxGet(url, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open('GET', API_BASE + url, true);

    // 设置请求头（如果有token）
    const token = localStorage.getItem('token');
    if (token) {
        xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    }

    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                callback(response);
            } else {
                showMessage('请求失败: ' + xhr.status, 'error');
            }
        }
    };

    xhr.send();
}

/**
 * 封装AJAX POST请求
 */
function ajaxPost(url, data, callback) {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', API_BASE + url, true);
    xhr.setRequestHeader('Content-Type', 'application/json');

    // 设置请求头（如果有token）
    const token = localStorage.getItem('token');
    if (token) {
        xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    }

    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                callback(response);
            } else {
                showMessage('请求失败: ' + xhr.status, 'error');
            }
        }
    };

    xhr.send(JSON.stringify(data));
}

// ==================== 页面切换 ====================

function showPage(pageId) {
    // 关闭下拉菜单
    document.querySelectorAll('.nav-dropdown.open').forEach(d => d.classList.remove('open'));

    // 隐藏所有页面
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active');
    });

    // 显示目标页面
    document.getElementById(pageId).classList.add('active');

    // 更新导航栏激活状态
    document.querySelectorAll('.nav-menu a').forEach(link => {
        link.classList.remove('active');
    });

    // 加载对应页面数据
    switch(pageId) {
        case 'departments':
            loadDepartments();
            break;
        case 'doctors':
            loadDoctors();
            loadDeptFilter();
            break;
        case 'appointment':
            loadAppointmentDepts();
            break;
    }
}

// ==================== 消息提示 ====================

function showMessage(msg, type) {
    const messageEl = document.getElementById('message');
    messageEl.textContent = msg;
    messageEl.className = 'message show ' + type;

    setTimeout(() => {
        messageEl.classList.remove('show');
    }, 3000);
}

// ==================== 科室相关（AJAX）====================

/**
 * AJAX加载科室列表
 */
function loadDepartments() {
    ajaxGet('/departments/list', function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            displayDepartments(response.data);
        } else {
            showMessage(response.message, 'error');
        }
    });
}

/**
 * AJAX搜索科室
 */
function searchDepartments() {
    const keyword = document.getElementById('deptSearch').value;
    if (keyword.trim() === '') {
        loadDepartments();
        return;
    }

    ajaxGet('/departments/search?keyword=' + encodeURIComponent(keyword), function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            displayDepartments(response.data);
        }
    });
}

/**
 * 显示科室列表
 */
function displayDepartments(departments) {
    const container = document.getElementById('departmentList');
    if (departments.length === 0) {
        container.innerHTML = '<p class="no-data">暂无数据</p>';
        return;
    }

    container.innerHTML = departments.map(dept => `
        <div class="card">
            <h3>${dept.deptName}</h3>
            <p><strong>位置：</strong>${dept.location || '未设置'}</p>
            <p><strong>电话：</strong>${dept.phone || '未设置'}</p>
            <p>${dept.description || ''}</p>
        </div>
    `).join('');
}

// ==================== 医生相关（AJAX）====================

/**
 * AJAX加载医生列表
 */
function loadDoctors() {
    ajaxGet('/doctors/list', function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            displayDoctors(response.data);
        }
    });
}

/**
 * AJAX根据科室加载医生
 */
function loadDoctorsByDept() {
    const deptId = document.getElementById('deptFilter').value;
    if (!deptId) {
        loadDoctors();
        return;
    }

    ajaxGet('/doctors/by-department/' + deptId, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            displayDoctors(response.data);
        }
    });
}

/**
 * AJAX搜索医生
 */
function searchDoctors() {
    const keyword = document.getElementById('doctorSearch').value;
    if (keyword.trim() === '') {
        loadDoctors();
        return;
    }

    ajaxGet('/doctors/search?keyword=' + encodeURIComponent(keyword), function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            displayDoctors(response.data);
        }
    });
}

/**
 * 显示医生列表
 */
function displayDoctors(doctors) {
    const container = document.getElementById('doctorList');
    if (doctors.length === 0) {
        container.innerHTML = '<p class="no-data">暂无数据</p>';
        return;
    }

    container.innerHTML = doctors.map(doctor => `
        <div class="card">
            <h3>${doctor.title || '医生'}</h3>
            <p><strong>擅长：</strong>${doctor.specialty || '未设置'}</p>
            <p><strong>学历：</strong>${doctor.education || '未设置'}</p>
            <p><strong>经验：</strong>${doctor.experience || 0}年</p>
            <p><strong>挂号费：</strong>¥${doctor.consultationFee || 0}</p>
            <p><strong>评分：</strong>${doctor.rating || 5.0}</p>
            <p>${doctor.introduction || ''}</p>
        </div>
    `).join('');
}

/**
 * AJAX加载科室筛选器
 */
function loadDeptFilter() {
    ajaxGet('/departments/list', function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            const select = document.getElementById('deptFilter');
            select.innerHTML = '<option value="">全部科室</option>' +
                response.data.map(dept => `<option value="${dept.id}">${dept.deptName}</option>`).join('');
        }
    });
}

// ==================== 预约挂号（AJAX核心应用）====================

/**
 * AJAX加载预约科室列表
 */
function loadAppointmentDepts() {
    ajaxGet('/departments/list', function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            const select = document.getElementById('appointDept');
            select.innerHTML = '<option value="">请选择科室</option>' +
                response.data.map(dept => `<option value="${dept.id}">${dept.deptName}</option>`).join('');
        }
    });
}

/**
 * AJAX级联加载医生（选择科室后）
 */
function loadDoctorsForAppointment() {
    const deptId = document.getElementById('appointDept').value;
    const doctorSelect = document.getElementById('appointDoctor');

    if (!deptId) {
        doctorSelect.innerHTML = '<option value="">请先选择科室</option>';
        doctorSelect.disabled = true;
        return;
    }

    // AJAX请求获取该科室的医生
    ajaxGet('/doctors/by-department/' + deptId, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            doctorSelect.innerHTML = '<option value="">请选择医生</option>' +
                response.data.map(doctor => `<option value="${doctor.id}">${doctor.title}</option>`).join('');
            doctorSelect.disabled = false;
        }
    });
}

/**
 * AJAX加载号源信息（选择医生和日期后）
 */
function loadSchedules() {
    const doctorId = document.getElementById('appointDoctor').value;
    const date = document.getElementById('appointDate').value;
    const timeSlotSelect = document.getElementById('appointTimeSlot');
    const slotInfo = document.getElementById('slotInfo');

    if (!doctorId || !date) {
        timeSlotSelect.innerHTML = '<option value="">请先选择医生和日期</option>';
        timeSlotSelect.disabled = true;
        return;
    }

    // AJAX请求获取可用号源
    const startDate = date;
    const endDate = date;
    ajaxGet(`/schedules/by-doctor-and-date?doctorId=${doctorId}&date=${date}`, function(response) {
        if (response.code === 200 && response.data.length > 0) {
            timeSlotSelect.innerHTML = '<option value="">请选择时间段</option>' +
                response.data.map(schedule => {
                    const slotText = schedule.timeSlot === 'MORNING' ? '上午' :
                                   schedule.timeSlot === 'AFTERNOON' ? '下午' : '晚上';
                    return `<option value="${schedule.timeSlot}" data-schedule-id="${schedule.id}">
                        ${slotText} (剩余${schedule.availableSlots}号)
                    </option>`;
                }).join('');
            timeSlotSelect.disabled = false;

            // 监听时间段选择，显示号源信息
            timeSlotSelect.onchange = function() {
                const selected = this.options[this.selectedIndex];
                if (selected.value) {
                    slotInfo.textContent = `(剩余${selected.text.match(/\d+/)[0]}号)`;
                }
            };
        } else {
            timeSlotSelect.innerHTML = '<option value="">该日期无号源</option>';
            timeSlotSelect.disabled = true;
            slotInfo.textContent = '';
        }
    });
}

/**
 * AJAX提交预约
 */
function submitAppointment() {
    const doctorId = document.getElementById('appointDoctor').value;
    const date = document.getElementById('appointDate').value;
    const timeSlot = document.getElementById('appointTimeSlot').value;
    const symptom = document.getElementById('symptom').value;
    const timeSlotSelect = document.getElementById('appointTimeSlot');
    const scheduleId = timeSlotSelect.options[timeSlotSelect.selectedIndex]?.dataset?.scheduleId;

    // 验证
    if (!doctorId || !date || !timeSlot || !scheduleId) {
        showMessage('请完整选择预约信息', 'error');
        return;
    }

    // 检查是否登录
    if (!currentUser) {
        showMessage('请先登录', 'error');
        showPage('login');
        return;
    }

    // AJAX提交预约
    const data = {
        doctorId: parseInt(doctorId),
        scheduleId: parseInt(scheduleId),
        appointmentDate: date,
        timeSlot: timeSlot,
        symptom: symptom
    };

    ajaxPost('/appointments/create', data, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            showMessage('预约成功！预约号：' + response.data.appointmentNo, 'success');
            // 清空表单
            document.getElementById('appointDept').value = '';
            document.getElementById('appointDoctor').value = '';
            document.getElementById('appointDate').value = '';
            document.getElementById('appointTimeSlot').innerHTML = '<option value="">请先选择医生和日期</option>';
            document.getElementById('appointTimeSlot').disabled = true;
            document.getElementById('symptom').value = '';
            document.getElementById('slotInfo').textContent = '';
        } else {
            showMessage(response.message, 'error');
        }
    });
}

// ==================== 用户认证（AJAX）====================

/**
 * 切换登录/注册标签
 */
function switchTab(tab) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));

    event.target.classList.add('active');
    document.getElementById(tab + 'Form').classList.add('active');
}

/**
 * AJAX检查用户名是否可用
 */
function checkUsername() {
    const username = document.getElementById('regUsername').value;
    if (username.length < 3) {
        document.getElementById('usernameTip').textContent = '';
        return;
    }

    ajaxGet('/auth/check-username?username=' + encodeURIComponent(username), function(response) {
        const tip = document.getElementById('usernameTip');
        if (response.code === 200 && response.data) {
            tip.textContent = '✓ 用户名可用';
            tip.className = 'tip success';
        } else {
            tip.textContent = '✗ 用户名已存在';
            tip.className = 'tip error';
        }
    });
}

/**
 * AJAX登录
 */
document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    ajaxPost('/auth/login', { username, password }, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            

            showMessage('登录成功！', 'success');
            updateNavForLogin();
            showPage('home');
        } else {
            showMessage(response.message, 'error');
        }
    });
});

/**
 * AJAX注册
 */
document.getElementById('registerForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const password = document.getElementById('regPassword').value;
    const passwordConfirm = document.getElementById('regPasswordConfirm').value;

    if (password !== passwordConfirm) {
        showMessage('两次密码输入不一致', 'error');
        return;
    }

    const data = {
        username: document.getElementById('regUsername').value,
        password: password,
        realName: document.getElementById('regRealName').value,
        phone: document.getElementById('regPhone').value,
        email: document.getElementById('regEmail').value,
        gender: 0
    };

    ajaxPost('/auth/register', data, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            showMessage('注册成功！请登录', 'success');
            switchTab('login');
            document.getElementById('registerForm').reset();
        } else {
            showMessage(response.message, 'error');
        }
    });
});

/**
 * 更新导航栏（登录后）
 */
function updateNavForLogin() {
    document.getElementById('loginNav').style.display = 'none';
    document.getElementById('userNav').style.display = 'inline-block';
    document.getElementById('userName').textContent = currentUser.realName || currentUser.username;
    // 加载未读消息
    loadNavUnreadCount();
}

/**
 * 退出登录
 */
function logout() {
    currentUser = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    document.getElementById('loginNav').style.display = 'inline-block';
    document.getElementById('userNav').style.display = 'none';

    showMessage('已退出登录', 'success');
    showPage('home');
}

/**
 * 检查登录状态
 */
function checkLoginStatus() {
    const user = localStorage.getItem('user');
    if (user) {
        currentUser = JSON.parse(user);
        updateNavForLogin();
    }
}

// ==================== 初始化 ====================

window.onload = function() {
    checkLoginStatus();

    // 设置默认日期为今天
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('appointDate').value = today;
    document.getElementById('appointDate').min = today;
};

// ==================== 我的预约功能 ====================

let currentAppointmentId = null;
let currentPage = 1;
const pageSize = 10;

/**
 * AJAX加载我的预约列表
 */
function loadMyAppointments() {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        showPage('login');
        return;
    }

    const status = document.getElementById('appointmentStatusFilter').value;
    const keyword = document.getElementById('appointmentSearch').value;

    let url = `/appointments/my-list?pageNum=${currentPage}&pageSize=${pageSize}`;
    if (status) url += `&status=${status}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;

    ajaxGet(url, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            displayMyAppointments(response.data);
        } else {
            showMessage(response.message, 'error');
        }
    });
}

/**
 * AJAX搜索我的预约
 */
function searchMyAppointments() {
    currentPage = 1;
    loadMyAppointments();
}

/**
 * 显示我的预约列表
 */
function displayMyAppointments(data) {
    const container = document.getElementById('appointmentList');
    const paginationContainer = document.getElementById('pagination');

    if (!data.list || data.list.length === 0) {
        container.innerHTML = '<p class="no-data" style="text-align: center; padding: 40px; color: hsl(210, 10%, 50%);">暂无预约记录</p>';
        paginationContainer.innerHTML = '';
        return;
    }

    container.innerHTML = data.list.map(appointment => `
        <div class="appointment-card" onclick="showAppointmentDetail(${appointment.id})">
            <div class="appointment-header">
                <div class="appointment-no">${appointment.appointmentNo}</div>
                <span class="appointment-status status-${appointment.statusText.toLowerCase()}">${appointment.statusLabel || appointment.statusText}</span>
            </div>
            <div class="appointment-info">
                <div class="info-item">
                    <span class="info-label">医生</span>
                    <span class="info-value">${appointment.doctorTitle} ${appointment.doctorName}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">科室</span>
                    <span class="info-value">${appointment.departmentName}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">预约日期</span>
                    <span class="info-value">${appointment.appointmentDate} ${appointment.timeSlotText}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">症状描述</span>
                    <span class="info-value">${appointment.symptom || '无'}</span>
                </div>
                <div class="info-item">
                    <span class="info-label">预约时间</span>
                    <span class="info-value">${appointment.createTime}</span>
                </div>
            </div>
        </div>
    `).join('');

    // 渲染分页
    renderPagination(data.total, data.pageNum, data.pageSize);
}

/**
 * 渲染分页
 */
function renderPagination(total, pageNum, pageSize) {
    const totalPages = Math.ceil(total / pageSize);
    const container = document.getElementById('pagination');

    if (totalPages <= 1) {
        container.innerHTML = `<span class="pagination-info">共 ${total} 条记录</span>`;
        return;
    }

    let html = `<span class="pagination-info">共 ${total} 条记录，第 ${pageNum}/${totalPages} 页</span>`;
    html += `<button onclick="changePage(${pageNum - 1})" ${pageNum === 1 ? 'disabled' : ''}>上一页</button>`;

    for (let i = 1; i <= totalPages; i++) {
        if (i === 1 || i === totalPages || (i >= pageNum - 1 && i <= pageNum + 1)) {
            html += `<button onclick="changePage(${i})" class="${i === pageNum ? 'active' : ''}">${i}</button>`;
        } else if (i === pageNum - 2 || i === pageNum + 2) {
            html += `<span>...</span>`;
        }
    }

    html += `<button onclick="changePage(${pageNum + 1})" ${pageNum === totalPages ? 'disabled' : ''}>下一页</button>`;
    container.innerHTML = html;
}

/**
 * 切换页码
 */
function changePage(page) {
    currentPage = page;
    loadMyAppointments();
}

/**
 * 显示预约详情
 */
function showAppointmentDetail(id) {
    ajaxGet(`/appointments/${id}`, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            currentAppointmentId = id;
            const appointment = response.data;
            
            const detailHtml = `
                <div class="appointment-info" style="margin-top: 20px;">
                    <div class="info-item">
                        <span class="info-label">预约号</span>
                        <span class="info-value">${appointment.appointmentNo}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">状态</span>
                        <span class="info-value"><span class="appointment-status status-${appointment.statusText.toLowerCase()}">${appointment.statusLabel || appointment.statusText}</span></span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">患者姓名</span>
                        <span class="info-value">${appointment.patientName}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">医生</span>
                        <span class="info-value">${appointment.doctorTitle} ${appointment.doctorName}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">科室</span>
                        <span class="info-value">${appointment.departmentName}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">预约日期</span>
                        <span class="info-value">${appointment.appointmentDate} ${appointment.timeSlotText}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">症状描述</span>
                        <span class="info-value">${appointment.symptom || '无'}</span>
                    </div>
                    <div class="info-item">
                        <span class="info-label">改签次数</span>
                        <span class="info-value">${appointment.rescheduleCount || 0} 次</span>
                    </div>
                    ${appointment.cancelReason ? `
                    <div class="info-item" style="grid-column: 1 / -1;">
                        <span class="info-label">取消原因</span>
                        <span class="info-value">${appointment.cancelReason}</span>
                    </div>` : ''}
                    <div class="info-item" style="grid-column: 1 / -1;">
                        <span class="info-label">创建时间</span>
                        <span class="info-value">${appointment.createTime}</span>
                    </div>
                </div>
            `;

            document.getElementById('appointmentDetail').innerHTML = detailHtml;

            // 根据状态显示/隐藏按钮
            const cancelBtn = document.getElementById('cancelBtn');
            const rescheduleBtn = document.getElementById('rescheduleBtn');

            if (appointment.statusText === 'PENDING' || appointment.statusText === 'CONFIRMED') {
                cancelBtn.style.display = 'inline-block';
                rescheduleBtn.style.display = 'inline-block';
            } else {
                cancelBtn.style.display = 'none';
                rescheduleBtn.style.display = 'none';
            }

            document.getElementById('appointmentModal').classList.add('show');
        } else {
            showMessage(response.message, 'error');
        }
    });
}

/**
 * 关闭模态框
 */
function closeModal() {
    document.getElementById('appointmentModal').classList.remove('show');
    currentAppointmentId = null;
}

/**
 * 取消预约
 */
function cancelAppointment() {
    if (!currentAppointmentId) return;

    const cancelReason = prompt('请输入取消原因：');
    if (!cancelReason || cancelReason.trim() === '') {
        showMessage('取消原因不能为空', 'error');
        return;
    }

    ajaxPost(`/appointments/${currentAppointmentId}/cancel`, { cancelReason: cancelReason.trim() }, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            showMessage('取消成功', 'success');
            closeModal();
            loadMyAppointments();
        } else {
            showMessage(response.message, 'error');
        }
    });
}

/**
 * 改签预约
 */
function rescheduleAppointment() {
    if (!currentAppointmentId) return;

    // 获取预约详情
    ajaxGet(`/appointments/${currentAppointmentId}`, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            const appointment = response.data;
            
            // 显示改签选择界面
            const newDate = prompt('请输入新的预约日期 (YYYY-MM-DD)：');
            if (!newDate) return;

            const timeSlot = prompt('请选择时间段 (MORNING/AFTERNOON/EVENING)：');
            if (!timeSlot) return;

            // 获取可改签的号源
            ajaxGet(`/appointments/reschedule-schedules?doctorId=${appointment.doctorId}&startDate=${newDate}&endDate=${newDate}`, function(scheduleResponse) {
                if (scheduleResponse.code === 200 && scheduleResponse.data.length > 0) {
                    const schedule = scheduleResponse.data.find(s => s.timeSlot === timeSlot);
                    
                    if (!schedule) {
                        showMessage('该时间段无可用号源', 'error');
                        return;
                    }

                    if (confirm(`确认改签到 ${newDate} ${schedule.timeSlotText}？`)) {
                        ajaxPost(`/appointments/${currentAppointmentId}/reschedule`, {
                            newScheduleId: schedule.id,
                            newAppointmentDate: newDate,
                            newTimeSlot: timeSlot
                        }, function(rescheduleResponse) {
                            if (rescheduleResponse.code === 200) {
                                showMessage('改签成功', 'success');
                                closeModal();
                                loadMyAppointments();
                            } else {
                                showMessage(rescheduleResponse.message, 'error');
                            }
                        });
                    }
                } else {
                    showMessage('该日期无可用号源', 'error');
                }
            });
        }
    });
}

// 修改showPage函数,添加我的预约页面加载
const originalShowPage = showPage;
showPage = function(pageId) {
    originalShowPage(pageId);
    
    if (pageId === 'myAppointments') {
        loadMyAppointments();
    }
};

// 点击模态框外部关闭
window.onclick = function(event) {
    const modal = document.getElementById('appointmentModal');
    if (event.target === modal) {
        closeModal();
    }
    // 点击外部关闭导航下拉
    if (!event.target.closest('.nav-dropdown')) {
        document.querySelectorAll('.nav-dropdown.open').forEach(d => d.classList.remove('open'));
    }
};

// ==================== 在线问诊功能（AJAX核心模块）====================

let currentConsultationId = null;
let currentConsultUserType = 'PATIENT'; // PATIENT or DOCTOR
let consultPage = 1;
let lastMessageId = 0;
let pollingTimer = null;

// 修改showPage，添加问诊页面加载
const showPageWithConsult = showPage;
showPage = function(pageId) {
    showPageWithConsult(pageId);

    if (pageId === 'myAppointments') {
        loadMyAppointments();
    }
    if (pageId === 'consultation') {
        if (currentUser) {
            currentConsultUserType = currentUser.role === 'DOCTOR' ? 'DOCTOR' : 'PATIENT';
        }
        loadMyConsultations();
    }
};

/**
 * 显示问诊列表
 */
function loadMyConsultations() {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        showPage('login');
        return;
    }

    const status = document.getElementById('consultStatusFilter').value;
    let url = `/consultations/my-list?userType=${currentConsultUserType}&pageNum=${consultPage}&pageSize=10`;
    if (status) url += `&status=${status}`;

    ajaxGet(url, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            displayConsultationList(response.data);
        } else {
            showMessage(response.message, 'error');
        }
    });
}

function displayConsultationList(data) {
    const container = document.getElementById('consultationListContainer');
    const pg = document.getElementById('consultPagination');

    if (!data.list || data.list.length === 0) {
        container.innerHTML = '<p style="text-align:center; padding:40px; color:hsl(210,10%,50%);">暂无问诊记录</p>';
        pg.innerHTML = '';
        return;
    }

    container.innerHTML = data.list.map(c => `
        <div class="consult-card" onclick="enterChat(${c.id})">
            <div class="consult-card-header">
                <span class="consult-no">${c.consultationNo}</span>
                <span class="consult-type-badge">${c.typeText}</span>
                <span class="consult-status-badge status-${c.status}">${c.statusText}</span>
            </div>
            <div class="consult-card-body">
                <div class="consult-doctor">
                    <span class="avatar-small"></span>
                    <span>${c.doctorTitle || ''} ${c.doctorName || '待分配'}</span>
                </div>
                <div class="consult-dept">${c.deptName || ''}</div>
                <div class="consult-symptom">${c.symptomDescription || '无描述'}</div>
            </div>
            <div class="consult-card-footer">${c.createdAt ? c.createdAt.substring(0,16) : ''}</div>
        </div>
    `).join('');

    // 简单分页
    const totalPages = Math.ceil(data.total / data.pageSize);
    if (totalPages > 1) {
        let html = '';
        for (let i = 1; i <= totalPages; i++) {
            html += `<button onclick="consultPage=${i};loadMyConsultations()" class="${i===consultPage?'active':''}">${i}</button>`;
        }
        pg.innerHTML = html;
    } else {
        pg.innerHTML = '';
    }
}

/**
 * 发起文字问诊
 */
function startTextConsultation() {
    if (!currentUser) {
        showMessage('请先登录', 'error');
        showPage('login');
        return;
    }

    // 加载科室列表
    ajaxGet('/departments/list', function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            const select = document.getElementById('consultDept');
            select.innerHTML = '<option value="">请选择科室</option>' +
                response.data.map(d => `<option value="${d.id}">${d.deptName}</option>`).join('');
        }
    });

    document.getElementById('startConsultModal').classList.add('show');
}

function loadConsultDoctors() {
    const deptId = document.getElementById('consultDept').value;
    const doctorSelect = document.getElementById('consultDoctor');

    if (!deptId) {
        doctorSelect.innerHTML = '<option value="">请先选择科室</option>';
        doctorSelect.disabled = true;
        return;
    }

    ajaxGet('/doctors/by-department/' + deptId, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            doctorSelect.innerHTML = '<option value="">请选择医生</option>' +
                response.data.map(d => `<option value="${d.id}">${d.title}</option>`).join('');
            doctorSelect.disabled = false;
        }
    });
}

function closeStartConsultModal() {
    document.getElementById('startConsultModal').classList.remove('show');
}

function submitConsultation() {
    const doctorId = document.getElementById('consultDoctor').value;
    const deptId = document.getElementById('consultDept').value;
    const symptom = document.getElementById('consultSymptom').value;

    if (!doctorId || !deptId) {
        showMessage('请完整填写信息', 'error');
        return;
    }

    ajaxPost('/consultations/create', {
        doctorId: parseInt(doctorId),
        deptId: parseInt(deptId),
        type: 'TEXT',
        symptomDescription: symptom
    }, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            showMessage('问诊发起成功！', 'success');
            closeStartConsultModal();
            document.getElementById('consultDept').value = '';
            document.getElementById('consultDoctor').innerHTML = '<option value="">请先选择科室</option>';
            document.getElementById('consultDoctor').disabled = true;
            document.getElementById('consultSymptom').value = '';
            // 直接进入聊天
            enterChat(response.data.id);
        } else {
            showMessage(response.message, 'error');
        }
    });
}

/**
 * 进入聊天界面
 */
function enterChat(consultationId) {
    currentConsultationId = consultationId;

    ajaxGet(`/consultations/${consultationId}`, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            const c = response.data;

            document.getElementById('consultationList').style.display = 'none';
            document.getElementById('consultationChat').style.display = 'block';

            document.getElementById('chatDoctorName').textContent = (c.doctorTitle || '') + ' ' + (c.doctorName || '');
            document.getElementById('chatDoctorDept').textContent = c.deptName || '';
            document.getElementById('chatStatus').textContent = c.statusText;

            // 渲染消息
            renderMessages(c.messages || []);

            // 更新最后消息ID
            if (c.messages && c.messages.length > 0) {
                lastMessageId = c.messages[c.messages.length - 1].id;
            }

            // 控制输入框
            const footer = document.getElementById('chatFooter');
            if (c.status >= 2) {
                footer.style.display = 'none';
            } else {
                footer.style.display = 'flex';
            }

            // 开始轮询
            startPolling();
        } else {
            showMessage(response.message, 'error');
        }
    });
}

/**
 * 返回列表
 */
function backToList() {
    stopPolling();
    currentConsultationId = null;
    document.getElementById('consultationList').style.display = 'block';
    document.getElementById('consultationChat').style.display = 'none';
    loadMyConsultations();
}

/**
 * 渲染消息列表
 */
function renderMessages(messages) {
    const container = document.getElementById('chatMessages');
    if (!messages || messages.length === 0) {
        container.innerHTML = '<div class="chat-empty">暂无消息，开始您的问诊吧</div>';
        return;
    }

    container.innerHTML = messages.map(m => {
        if (m.senderType === 'SYSTEM') {
            return `<div class="chat-msg system"><span>🔔 ${m.content}</span></div>`;
        }
        const isMe = (m.senderType === currentConsultUserType);
        return `
            <div class="chat-msg ${isMe ? 'mine' : 'other'}">
                <div class="msg-bubble">
                    ${m.messageType === 'PRESCRIPTION' ? `<div class="prescription-card-inline" onclick="viewPrescription(${m.refPrescriptionId})">
                        <span class="rx-icon">💊</span> ${m.content}
                    </div>` : `<div class="msg-text">${m.content}</div>`}
                </div>
                <div class="msg-meta">
                    <span class="msg-sender">${m.senderName || ''}</span>
                    <span class="msg-time">${m.time || ''}</span>
                </div>
            </div>
        `;
    }).join('');

    // 滚动到底部
    container.scrollTop = container.scrollHeight;
}

/**
 * 发送消息
 */
function sendConsultMessage() {
    const input = document.getElementById('chatInput');
    const content = input.value.trim();
    if (!content || !currentConsultationId) return;

    ajaxPost(`/consultations/${currentConsultationId}/message`, {
        content: content,
        messageType: 'TEXT'
    }, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            input.value = '';
            // 立即刷新消息
            refreshMessages();
        } else {
            showMessage(response.message, 'error');
        }
    });
}

// 回车发送
document.addEventListener('DOMContentLoaded', function() {
    const chatInput = document.getElementById('chatInput');
    if (chatInput) {
        chatInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                sendConsultMessage();
            }
        });
    }
});

/**
 * AJAX轮询获取新消息
 */
function startPolling() {
    stopPolling();
    pollingTimer = setInterval(refreshMessages, 3000);
}

function stopPolling() {
    if (pollingTimer) {
        clearInterval(pollingTimer);
        pollingTimer = null;
    }
}

function refreshMessages() {
    if (!currentConsultationId) return;

    ajaxGet(`/consultations/${currentConsultationId}/messages?afterMessageId=${lastMessageId}`, function(response) {
        if (response.code === 200 && response.data && response.data.length > 0) {
            const container = document.getElementById('chatMessages');
            const oldScroll = container.scrollTop;
            const wasAtBottom = (container.scrollHeight - container.scrollTop - container.clientHeight) < 50;

            response.data.forEach(m => {
                let html;
                if (m.senderType === 'SYSTEM') {
                    html = `<div class="chat-msg system"><span>🔔 ${m.content}</span></div>`;
                } else {
                    const isMe = (m.senderType === currentConsultUserType);
                    html = `
                        <div class="chat-msg ${isMe ? 'mine' : 'other'}">
                            <div class="msg-bubble">
                                ${m.messageType === 'PRESCRIPTION' ? `<div class="prescription-card-inline" onclick="viewPrescription(${m.refPrescriptionId})">
                                    <span class="rx-icon">💊</span> ${m.content}
                                </div>` : `<div class="msg-text">${m.content}</div>`}
                            </div>
                            <div class="msg-meta">
                                <span class="msg-sender">${m.senderName || ''}</span>
                                <span class="msg-time">${m.time || ''}</span>
                            </div>
                        </div>
                    `;
                }
                container.insertAdjacentHTML('beforeend', html);
            });

            lastMessageId = response.data[response.data.length - 1].id;

            if (wasAtBottom) {
                container.scrollTop = container.scrollHeight;
            }
        }
    });
}

/**
 * 查看处方详情
 */
function viewPrescription(prescriptionId) {
    if (!prescriptionId) return;

    ajaxGet(`/prescriptions/${prescriptionId}`, function(response) {
        if (response.code === 200) {
            currentUser = response.data; localStorage.setItem('token', response.data.token); localStorage.setItem('user', JSON.stringify(response.data));
            if (response.data.role === "DOCTOR") { 
                window.location.href = "/doctor.html"; 
                return; 
            }
            const p = response.data;
            let itemsHtml = (p.items || []).map(i => `
                <tr>
                    <td>${i.medicineName}</td><td>${i.specification || ''}</td>
                    <td>${i.dosage || ''}</td><td>${i.usageMethod || ''}</td>
                    <td>${i.frequency || ''}</td><td>${i.days || ''}天</td>
                    <td>${i.quantity || ''}</td><td>¥${i.subtotal || 0}</td>
                </tr>
            `).join('');

            const html = `
                <div class="rx-detail">
                    <div class="rx-header">
                        <h3>💊 电子处方</h3>
                        <span class="rx-no">${p.prescriptionNo}</span>
                    </div>
                    <div class="rx-info">
                        <div><span>医生：</span>${p.doctorName || ''}</div>
                        <div><span>患者：</span>${p.patientName || ''}</div>
                        <div><span>诊断：</span>${p.diagnosis || ''}</div>
                        <div><span>医嘱：</span>${p.advice || ''}</div>
                    </div>
                    <table class="rx-table"><thead><tr>
                        <th>药品</th><th>规格</th><th>用量</th><th>用法</th><th>频次</th><th>天数</th><th>数量</th><th>金额</th>
                    </tr></thead><tbody>${itemsHtml}</tbody></table>
                    <div class="rx-total">合计：<strong>¥${p.totalAmount || 0}</strong></div>
                </div>
            `;

            document.getElementById('appointmentDetail').innerHTML = html;
            document.getElementById('cancelBtn').style.display = 'none';
            document.getElementById('rescheduleBtn').style.display = 'none';
            document.getElementById('appointmentModal').classList.add('show');
        }
    });
}

// 清理轮询
window.addEventListener('beforeunload', stopPolling);

// ==================== 报告查询模块 ====================
let reportTab = 'lab';

function switchReportTab(tab) {
    reportTab = tab;
    document.querySelectorAll('.report-tab').forEach((t,i) => t.classList.toggle('active', (i===0 && tab==='lab') || (i===1 && tab==='imaging')));
    document.getElementById('labReports').style.display = tab === 'lab' ? 'block' : 'none';
    document.getElementById('imagingReports').style.display = tab === 'imaging' ? 'block' : 'none';
    if (tab === 'lab') loadLabReports(); else loadImagingReports();
}

function loadLabReports() {
    ajaxGet('/reports/lab/my-list', function(res) {
        if (res.code !== 200) { showMessage(res.message,'error'); return; }
        const c = document.getElementById('labReports');
        if (!res.data || !res.data.length) { c.innerHTML = '<p class="no-data">暂无检验报告</p>'; return; }
        c.innerHTML = res.data.map(r => `
            <div class="report-card ${r.normalFlag===1?'':'report-abnormal'}" onclick="viewLabReport(${r.id})">
                <div class="report-card-header">
                    <span class="report-name">${r.reportName}</span>
                    <span class="report-flag">${r.normalFlagText}</span>
                </div>
                <div class="report-card-body">
                    <span>样本：${r.sampleType||'-'}</span>
                    <span>医生：${r.doctorName||'-'}</span>
                    <span>日期：${r.reportDate}</span>
                </div>
            </div>
        `).join('');
    });
}

function loadImagingReports() {
    ajaxGet('/reports/imaging/my-list', function(res) {
        if (res.code !== 200) { showMessage(res.message,'error'); return; }
        const c = document.getElementById('imagingReports');
        if (!res.data || !res.data.length) { c.innerHTML = '<p class="no-data">暂无影像报告</p>'; return; }
        c.innerHTML = res.data.map(r => `
            <div class="report-card" onclick="viewImagingReport(${r.id})">
                <div class="report-card-header">
                    <span class="report-name">${r.reportName}</span>
                    <span class="report-modality">${r.modality}</span>
                </div>
                <div class="report-card-body">
                    <span>部位：${r.bodyPart||'-'}</span>
                    <span>医生：${r.doctorName||'-'}</span>
                    <span>日期：${r.reportDate}</span>
                </div>
            </div>
        `).join('');
    });
}

function viewLabReport(id) {
    ajaxGet('/reports/lab/'+id, function(res) {
        if (res.code!==200) return;
        const r = res.data;
        let itemsHtml = '';
        try { const obj = JSON.parse(r.result||'{}'); itemsHtml = Object.entries(obj).map(([k,v]) => `<tr><td>${k}</td><td>${v.value} ${v.unit||''}</td><td>${v.range||''}</td><td class="${v.flag==='H'?'abnormal-high':v.flag==='L'?'abnormal-low':''}">${v.flag==='H'?'↑':v.flag==='L'?'↓':'正常'}</td></tr>`).join(''); } catch(e){}
        document.getElementById('reportDetail').innerHTML = `<div class="rx-detail"><div class="rx-header"><h3>📊 ${r.reportName}</h3><span class="rx-no">${r.reportNo}</span></div><div class="rx-info"><div><span>样本：</span>${r.sampleType||'-'}</div><div><span>结论：</span><strong style="color:${r.normalFlag===1?'hsl(var(--medical-green))':'hsl(0,70%,50%)'}">${r.normalFlagText}</strong></div></div><table class="rx-table"><thead><tr><th>指标</th><th>结果</th><th>参考范围</th><th>判断</th></tr></thead><tbody>${itemsHtml}</tbody></table><p style="margin-top:12px;color:hsl(210,10%,35%);">${r.conclusion||''}</p></div>`;
        document.getElementById('reportModal').classList.add('show');
    });
}

function viewImagingReport(id) {
    ajaxGet('/reports/imaging/'+id, function(res) {
        if (res.code!==200) return;
        const r = res.data;
        document.getElementById('reportDetail').innerHTML = `<div class="rx-detail"><div class="rx-header"><h3>🩻 ${r.reportName}</h3><span class="rx-no">${r.reportNo}</span></div><div class="rx-info"><div><span>类型：</span>${r.modality}</div><div><span>部位：</span>${r.bodyPart||'-'}</div><div><span>医生：</span>${r.doctorName||'-'}</div><div><span>日期：</span>${r.reportDate}</div></div><div style="margin-top:12px;"><h4 style="color:hsl(var(--medical-blue));margin-bottom:6px;">影像所见</h4><p style="color:hsl(210,10%,30%);line-height:1.7;">${r.finding||'无'}</p></div><div style="margin-top:14px;"><h4 style="color:hsl(var(--medical-blue));margin-bottom:6px;">影像诊断</h4><p style="color:hsl(210,10%,30%);line-height:1.7;font-weight:500;">${r.impression||'无'}</p></div></div>`;
        document.getElementById('reportModal').classList.add('show');
    });
}

function closeReportModal() { document.getElementById('reportModal').classList.remove('show'); }

// ==================== 健康档案模块 ====================
function loadHealthProfile() {
    ajaxGet('/profile/my', function(res) {
        if (res.code!==200) { showMessage(res.message,'error'); return; }
        const p = res.data;
        document.getElementById('profileContent').innerHTML = `
            <div class="profile-header">
                <div class="profile-avatar"></div>
                <div class="profile-info">
                    <h3>${p.patientName||''}</h3>
                    <p>${p.genderText||''} · ${p.phone||''}</p>
                    <p>${p.email||''}</p>
                </div>
            </div>
            <div class="profile-stats">
                <div class="stat-card"><span class="stat-num">${p.appointmentCount||0}</span><span class="stat-label">预约次数</span></div>
                <div class="stat-card"><span class="stat-num">${p.consultationCount||0}</span><span class="stat-label">问诊次数</span></div>
                <div class="stat-card"><span class="stat-num">${p.prescriptionCount||0}</span><span class="stat-label">处方数</span></div>
            </div>
            <h3 style="color:hsl(var(--medical-blue));margin:25px 0 15px;">最近就诊记录</h3>
            <div class="profile-history">${(p.recentAppointments||[]).map(a => `<div class="history-item"><span>📅 ${a.appointmentDate}</span><span>预约号：${a.appointmentNo}</span></div>`).join('')||'<p class="no-data">暂无记录</p>'}</div>
        `;
    });
}

// ==================== 消息中心模块 ====================
function loadNotifications() {
    ajaxGet('/notifications/my-list', function(res) {
        if (res.code!==200) return;
        const c = document.getElementById('notificationList');
        if (!res.data||!res.data.length) { c.innerHTML='<p class="no-data" style="text-align:center;padding:40px;">暂无消息</p>'; return; }
        c.innerHTML = res.data.map(n => `
            <div class="notif-card ${n.isRead===0?'notif-unread':''}" onclick="markNotifRead(${n.id})">
                <div class="notif-header"><span class="notif-type">${n.typeText}</span><span class="notif-time">${n.time}</span></div>
                <div class="notif-title">${n.isRead===0?'<span class="dot"></span>':''}${n.title}</div>
                <div class="notif-content">${n.content}</div>
            </div>
        `).join('');
        loadUnreadCount();
    });
}

function loadUnreadCount() {
    ajaxGet('/notifications/unread-count', function(res) {
        if (res.code===200) {
            document.getElementById('unreadBadge').textContent = res.data.count||0;
            // 同步更新导航徽章
            updateNavBadge(res.data.count||0);
        }
    });
}

function loadNavUnreadCount() {
    ajaxGet('/notifications/unread-count', function(res) {
        if (res.code===200) updateNavBadge(res.data.count||0);
    });
}

function updateNavBadge(count) {
    const badge = document.getElementById('navUnreadBadge');
    if (badge) badge.style.display = count > 0 ? 'inline-block' : 'none';
}

function markNotifRead(id) { ajaxPost('/notifications/'+id+'/read',{},function(){loadNotifications();}); }
function markAllNotificationsRead() { ajaxPost('/notifications/read-all',{},function(){loadNotifications();showMessage('全部已读','success');}); }

// ==================== 客服中心模块 ====================
function filterFaqs(cat) {
    document.querySelectorAll('.faq-cat').forEach(b => b.classList.toggle('active', b.textContent.includes(cat?'':'')));
    // Simple filtering - reload based on category
    const url = cat ? `/service/faqs?category=${cat}` : '/service/faqs';
    ajaxGet(url, function(res) { if(res.code===200) renderFaqs(res.data); });
}

function searchFaqs() {
    const kw = document.getElementById('faqSearch').value.trim();
    if (!kw) { filterFaqs(''); return; }
    ajaxGet('/service/faqs/search?keyword='+encodeURIComponent(kw), function(res) { if(res.code===200) renderFaqs(res.data); });
}

function renderFaqs(list) {
    document.getElementById('faqList').innerHTML = (list||[]).map(f => `
        <div class="faq-item" onclick="this.classList.toggle('open')">
            <div class="faq-q">${f.question}<span class="faq-arrow">▼</span></div>
            <div class="faq-a">${f.answer}</div>
        </div>
    `).join('');
}

function submitComplaint() {
    if (!currentUser) { showMessage('请先登录','error'); showPage('login'); return; }
    const type = document.getElementById('complaintType').value;
    const title = document.getElementById('complaintTitle').value.trim();
    const content = document.getElementById('complaintContent').value.trim();
    if (!title||!content) { showMessage('请填写完整','error'); return; }
    ajaxPost('/service/complaints', {type,title,content,contact:document.getElementById('complaintContact').value}, function(res) {
        if (res.code===200) { showMessage('提交成功！感谢您的反馈','success');
            document.getElementById('complaintTitle').value='';document.getElementById('complaintContent').value='';document.getElementById('complaintContact').value=''; }
        else showMessage(res.message,'error');
    });
}

// ==================== 主页优化 ====================
// 粒子背景动画（home页面专用）
function initHomeParallax() {
    // Add subtle parallax to hero on scroll
    window.addEventListener('scroll', function() {
        const hero = document.querySelector('.hero');
        if (!hero) return;
        const scrolled = window.pageYOffset;
        hero.style.backgroundPositionY = (scrolled * 0.4) + 'px';
    });
}
initHomeParallax();

// 修改showPage以加载各模块数据
const showPageV3 = showPage;
showPage = function(pageId) {
    showPageV3(pageId);
    if (pageId === 'myAppointments') loadMyAppointments();
    if (pageId === 'consultation') { if (currentUser) { currentConsultUserType = currentUser.role==='DOCTOR'?'DOCTOR':'PATIENT'; } loadMyConsultations(); }
    if (pageId === 'reports') { switchReportTab('lab'); }
    if (pageId === 'healthProfile') loadHealthProfile();
    if (pageId === 'messages') loadNotifications();
    if (pageId === 'serviceCenter') filterFaqs('');
};
