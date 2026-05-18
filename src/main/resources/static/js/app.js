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
            currentUser = response.data;
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data));

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
