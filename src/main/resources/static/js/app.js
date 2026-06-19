const { createApp, ref, reactive, onMounted, onBeforeUnmount } = Vue;

const API_BASE = '/api';

// Create unified Axios instance
const api = axios.create({
    baseURL: API_BASE,
    timeout: 10000
});

// Configure Axios authorization header interceptor
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

// Vue 3 App Instance
const app = createApp({
    setup() {
        // --- State Definitions ---
        const currentPage = ref('home');
        const isLoggedIn = ref(false);
        const currentUser = reactive({
            username: '',
            realName: '',
            role: ''
        });

        // Toast Messages State
        const toast = reactive({
            show: false,
            message: '',
            type: 'success'
        });

        // Filter / Query Strings
        const filters = reactive({
            deptQuery: '',
            docDeptId: '',
            docQuery: '',
            faqQuery: ''
        });

        // Departments & Doctors lists
        const departments = ref([]);
        const deptList = ref([]); // shared dropdown category list
        const doctors = ref([]);

        // Booking fields
        const booking = reactive({
            deptId: '',
            doctorId: '',
            date: '',
            scheduleId: '',
            symptom: ''
        });
        const bookingDoctors = ref([]);
        const bookingTimeSlots = ref([]);

        // My Appointments lists
        const myAppts = reactive({
            list: [],
            page: 1,
            pageSize: 6,
            total: 0,
            filterStatus: '',
            query: ''
        });

        // Unified view modal
        const modal = reactive({
            show: false,
            content: '', // 'appointment', 'prescription', 'labReport', 'imagingReport'
            data: {}
        });

        // Consultation records
        const consultation = reactive({
            list: [],
            activeChatId: null,
            chatDoctorName: '',
            chatDoctorDept: '',
            chatStatus: 0,
            chatStatusLabel: '',
            messages: [],
            listPage: 1,
            filterStatus: '',
            modalShow: false,
            modalDeptId: '',
            modalDoctorId: '',
            modalSymptom: '',
            inputText: ''
        });
        const consultationDoctors = ref([]);
        let chatPollingTimer = null;
        let lastMessageId = 0;

        // Message Center notifications count
        const notifications = ref([]);
        const unreadCount = ref(0);

        // Help Center FAQs
        const faqs = ref([]);
        const faqFilterCat = ref('');

        // Complaints & suggestions
        const complaint = reactive({
            type: 'SUGGESTION',
            title: '',
            content: '',
            contact: ''
        });

        // Authorization tabs Info
        const authTab = ref('login');
        const loginForm = reactive({ username: '', password: '' });
        const registerForm = reactive({
            username: '',
            password: '',
            passwordConfirm: '',
            realName: '',
            phone: '',
            email: ''
        });
        const registerTip = ref('');
        const registerTipValid = ref(true);

        // Laboratory / Imaging Reports tabs info
        const reportsTab = ref('lab');
        const reports = reactive({
            labs: [],
            imagings: []
        });

        // Health profile record summary statistics
        const healthProfile = reactive({
            info: {
                patientName: '',
                genderText: '',
                phone: '',
                email: '',
                appointmentCount: 0,
                consultationCount: 0,
                prescriptionCount: 0,
                recentAppointments: []
            }
        });

        const todayDate = ref('');

        // --- Core Helpers ---
        const showMessage = (msg, type = 'success') => {
            toast.message = msg;
            toast.type = type;
            toast.show = true;
            setTimeout(() => {
                toast.show = false;
            }, 3000);
        };

        const showPage = (pageName) => {
            currentPage.value = pageName;
            stopChatPolling();

            // Route Guards / Checks
            if (!isLoggedIn.value && ['appointment', 'consultation', 'myAppointments', 'reports', 'healthProfile', 'messages'].includes(pageName)) {
                showMessage('该功能需要登录才能使用！请先登录', 'error');
                currentPage.value = 'login';
                authTab.value = 'login';
                return;
            }

            // Sync loading data
            if (pageName === 'departments') {
                loadDepartments();
            } else if (pageName === 'doctors') {
                loadAllDeptsFilter();
                loadAllDoctors();
            } else if (pageName === 'appointment') {
                loadAllDeptsFilter();
                resetBookingForm();
            } else if (pageName === 'consultation') {
                loadMyConsultations();
            } else if (pageName === 'myAppointments') {
                loadMyAppointments();
            } else if (pageName === 'reports') {
                switchReportsTab('lab');
            } else if (pageName === 'healthProfile') {
                loadHealthProfileData();
            } else if (pageName === 'messages') {
                loadNotificationsData();
            } else if (pageName === 'serviceCenter') {
                loadFaqsData();
            }
        };

        // --- Authentication ---
        const checkLoginStatus = () => {
            const token = localStorage.getItem('token');
            const userStr = localStorage.getItem('user');
            if (token && userStr) {
                try {
                    const user = JSON.parse(userStr);
                    isLoggedIn.value = true;
                    currentUser.username = user.username;
                    currentUser.realName = user.realName || user.username;
                    currentUser.role = user.role;
                    loadUnreadCount();
                } catch(e) {
                    logout();
                }
            }
        };

        const handleLoginSubmit = () => {
            if (!loginForm.username || !loginForm.password) {
                showMessage('请填写用户名与密码', 'error');
                return;
            }
            api.post('/auth/login', loginForm)
                .then(res => {
                    const r = res.data;
                    if (r.code === 200) {
                        localStorage.setItem('token', r.data.token);
                        localStorage.setItem('user', JSON.stringify(r.data));
                        localStorage.setItem('role', r.data.role);

                        isLoggedIn.value = true;
                        currentUser.username = r.data.username;
                        currentUser.realName = r.data.realName || r.data.username;
                        currentUser.role = r.data.role;

                        loginForm.username = '';
                        loginForm.password = '';

                        showMessage('登录成功！欢迎回来', 'success');

                        if (r.data.role === 'DOCTOR' || r.data.role === 'ROLE_DOCTOR') {
                            window.location.href = '/doctor.html';
                        } else {
                            showPage('home');
                        }
                    } else {
                        showMessage(r.message || '登录失败', 'error');
                    }
                })
                .catch(err => {
                    showMessage('登录异常，系统异常错误', 'error');
                });
        };

        const checkUsernameAvailable = () => {
            if (!registerForm.username || registerForm.username.length < 3) {
                registerTip.value = '';
                return;
            }
            api.get('/auth/check-username?username=' + encodeURIComponent(registerForm.username))
                .then(res => {
                    if (res.data.code === 200 && res.data.data) {
                        registerTip.value = '✓ 账号名可以使用';
                        registerTipValid.value = true;
                    } else {
                        registerTip.value = '✗ 该登录账号名已存在';
                        registerTipValid.value = false;
                    }
                });
        };

        const handleRegisterSubmit = () => {
            // Checks
            if (!registerForm.username || !registerForm.password || !registerForm.realName || !registerForm.phone) {
                showMessage('请完整输入全部注册核心信息', 'error');
                return;
            }
            if (!registerTipValid.value) {
                showMessage('账号名称不可用，请重新输入', 'error');
                return;
            }
            if (registerForm.password !== registerForm.passwordConfirm) {
                showMessage('两次确认密码不匹配，请重新检查', 'error');
                return;
            }

            const regData = {
                username: registerForm.username,
                password: registerForm.password,
                realName: registerForm.realName,
                phone: registerForm.phone,
                email: registerForm.email
            };

            api.post('/auth/register', regData)
                .then(res => {
                    if (res.data.code === 200) {
                        showMessage('注册账号成功！现在请切换至登录页面', 'success');
                        authTab.value = 'login';
                        // Reset forms
                        Object.keys(registerForm).forEach(k => registerForm[k] = '');
                        registerTip.value = '';
                    } else {
                        showMessage(res.data.message || '注册不成功', 'error');
                    }
                })
                .catch(err => showMessage('注册系统连接级错误', 'error'));
        };

        const logout = () => {
            localStorage.clear();
            isLoggedIn.value = false;
            currentUser.username = '';
            currentUser.realName = '';
            currentUser.role = '';
            showMessage('已成功注销、退出看病服务系统', 'success');
            showPage('home');
        };

        // --- Departments & Doctors Logic ---
        const loadDepartments = () => {
            api.get('/departments/list')
                .then(res => {
                    if (res.data.code === 200) {
                        departments.value = res.data.data;
                    }
                });
        };

        const searchDepartments = () => {
            if (!filters.deptQuery.trim()) {
                loadDepartments();
                return;
            }
            api.get('/departments/search?keyword=' + encodeURIComponent(filters.deptQuery))
                .then(res => {
                    if (res.data.code === 200) {
                        departments.value = res.data.data;
                    }
                });
        };

        const loadAllDeptsFilter = () => {
            api.get('/departments/list')
                .then(res => {
                    if (res.data.code === 200) {
                        deptList.value = res.data.data;
                    }
                });
        };

        const loadAllDoctors = () => {
            api.get('/doctors/list')
                .then(res => {
                    if (res.data.code === 200) {
                        doctors.value = res.data.data;
                    }
                });
        };

        const loadDoctorsByDept = () => {
            if (!filters.docDeptId) {
                loadAllDoctors();
                return;
            }
            api.get('/doctors/by-department/' + filters.docDeptId)
                .then(res => {
                    if (res.data.code === 200) {
                        doctors.value = res.data.data;
                    }
                });
        };

        const searchDoctors = () => {
            if (!filters.docQuery.trim()) {
                loadDoctorsByDept();
                return;
            }
            api.get('/doctors/search?keyword=' + encodeURIComponent(filters.docQuery))
                .then(res => {
                    if (res.data.code === 200) {
                        doctors.value = res.data.data;
                    }
                });
        };

        // --- Booking / Appointments ---
        const resetBookingForm = () => {
            booking.deptId = '';
            booking.doctorId = '';
            booking.date = todayDate.value;
            booking.scheduleId = '';
            booking.symptom = '';
            bookingDoctors.value = [];
            bookingTimeSlots.value = [];
        };

        const onBookingDeptChange = () => {
            booking.doctorId = '';
            booking.scheduleId = '';
            bookingTimeSlots.value = [];
            if (!booking.deptId) {
                bookingDoctors.value = [];
                return;
            }
            api.get('/doctors/by-department/' + booking.deptId)
                .then(res => {
                    if (res.data.code === 200) {
                        bookingDoctors.value = res.data.data;
                    }
                });
        };

        const onBookingDoctorOrDateChange = () => {
            booking.scheduleId = '';
            if (!booking.doctorId || !booking.date) {
                bookingTimeSlots.value = [];
                return;
            }
            api.get(`/schedules/by-doctor-and-date?doctorId=${booking.doctorId}&date=${booking.date}`)
                .then(res => {
                    if (res.data.code === 200) {
                        bookingTimeSlots.value = res.data.data;
                    }
                });
        };

        const quickBook = (doc) => {
            showPage('appointment');
            booking.deptId = doc.deptId;
            // cascade load doctors
            api.get('/doctors/by-department/' + doc.deptId)
                .then(res => {
                    if (res.data.code === 200) {
                        bookingDoctors.value = res.data.data;
                        booking.doctorId = doc.id;
                        onBookingDoctorOrDateChange();
                    }
                });
        };

        const submitAppointment = () => {
            if (!booking.doctorId || !booking.date || !booking.scheduleId) {
                showMessage('请完整输入出诊日期及挂号时间段号源', 'error');
                return;
            }
            const data = {
                doctorId: parseInt(booking.doctorId),
                scheduleId: parseInt(booking.scheduleId),
                appointmentDate: booking.date,
                timeSlot: bookingTimeSlots.value.find(s => s.id === booking.scheduleId)?.timeSlot || 'MORNING',
                symptom: booking.symptom
            };

            api.post('/appointments/create', data)
                .then(res => {
                    if (res.data.code === 200) {
                        showMessage('预约挂号成功！就诊号：' + res.data.data.appointmentNo, 'success');
                        resetBookingForm();
                        showPage('myAppointments');
                    } else {
                        showMessage(res.data.message || '挂号失败，号源已满', 'error');
                    }
                })
                .catch(err => showMessage('系统响应错误，请检查网络', 'error'));
        };

        // --- My Appointments ---
        const loadMyAppointments = () => {
            let url = `/appointments/my-list?pageNum=${myAppts.page}&pageSize=${myAppts.pageSize}`;
            if (myAppts.filterStatus) url += `&status=${myAppts.filterStatus}`;
            if (myAppts.query.trim()) url += `&keyword=${encodeURIComponent(myAppts.query)}`;

            api.get(url)
                .then(res => {
                    if (res.data.code === 200) {
                        myAppts.list = res.data.data.list || [];
                        myAppts.total = res.data.data.total || 0;
                    }
                });
        };

        const searchMyAppointments = () => {
            myAppts.page = 1;
            loadMyAppointments();
        };

        const changeApptsPage = (p) => {
            myAppts.page = p;
            loadMyAppointments();
        };

        const showAppointmentDetail = (id) => {
            api.get(`/appointments/${id}`)
                .then(res => {
                    if (res.data.code === 200) {
                        modal.content = 'appointment';
                        modal.data = res.data.data;
                        modal.show = true;
                    }
                });
        };

        const cancelAppointment = (id) => {
            const reason = prompt('请务必输入合法的取消诊断挂号预约单原因：');
            if (reason === null) return;
            if (!reason.trim()) {
                showMessage('取消原因不能为空！', 'error');
                return;
            }
            api.post(`/appointments/${id}/cancel`, { cancelReason: reason })
                .then(res => {
                    if (res.data.code === 200) {
                        showMessage('挂号单取消成功。', 'success');
                        closeUnifiedModal();
                        loadMyAppointments();
                    } else {
                        showMessage(res.data.message || '取消失败', 'error');
                    }
                });
        };

        const rescheduleAppointment = (appt) => {
            const newDate = prompt('请输入新的预约就诊意向日期 (YYYY-MM-DD)：', appt.appointmentDate);
            if (!newDate) return;
            const newSlotInput = prompt('请选择挂号时间段 (MORNING -> 上午, AFTERNOON -> 下午, EVENING -> 晚上)：', appt.timeSlot);
            if (!newSlotInput) return;

            // Fetch schedules for that day
            api.get(`/appointments/reschedule-schedules?doctorId=${appt.doctorId}&startDate=${newDate}&endDate=${newDate}`)
                .then(res => {
                    if (res.data.code === 200 && res.data.data.length > 0) {
                        const targetSchedule = res.data.data.find(s => s.timeSlot === newSlotInput);
                        if (!targetSchedule || targetSchedule.availableSlots <= 0) {
                            showMessage('目标时间段已挂满或此排班不正常', 'error');
                            return;
                        }
                        if (confirm(`确认要将就诊改签到 ${newDate} ${targetSchedule.timeSlotText} 吗？`)) {
                            api.post(`/appointments/${appt.id}/reschedule`, {
                                newScheduleId: targetSchedule.id,
                                newAppointmentDate: newDate,
                                newTimeSlot: newSlotInput
                            }).then(resp => {
                                if (resp.data.code === 200) {
                                    showMessage('改签就诊名额提交成功！', 'success');
                                    closeUnifiedModal();
                                    loadMyAppointments();
                                } else {
                                    showMessage(resp.data.message || '改签失败', 'error');
                                }
                            });
                        }
                    } else {
                        showMessage('该目标日期暂无名医排班计划', 'error');
                    }
                });
        };

        const closeUnifiedModal = () => {
            modal.show = false;
            modal.content = '';
            modal.data = {};
        };

        // --- Consultation Logic ---
        const loadMyConsultations = () => {
            let url = `/consultations/my-list?userType=PATIENT&pageNum=${consultation.listPage}&pageSize=10`;
            if (consultation.filterStatus) url += `&status=${consultation.filterStatus}`;
            api.get(url)
                .then(res => {
                    if (res.data.code === 200) {
                        consultation.list = res.data.data.list || [];
                    }
                });
        };

        const openConsultModal = () => {
            consultation.modalDeptId = '';
            consultation.modalDoctorId = '';
            consultation.modalSymptom = '';
            consultationDoctors.value = [];

            // fetch depts
            api.get('/departments/list')
                .then(res => {
                    if (res.data.code === 200) {
                        deptList.value = res.data.data;
                        consultation.modalShow = true;
                    }
                });
        };

        const onConsultDeptChange = () => {
            consultation.modalDoctorId = '';
            if (!consultation.modalDeptId) {
                consultationDoctors.value = [];
                return;
            }
            api.get('/doctors/by-department/' + consultation.modalDeptId)
                .then(res => {
                    if (res.data.code === 200) {
                        consultationDoctors.value = res.data.data;
                    }
                });
        };

        const closeConsultModal = () => {
            consultation.modalShow = false;
        };

        const openVideoConsult = () => {
            window.open('/video-consultation.html', '_blank');
        };

        const submitConsultation = () => {
            if (!consultation.modalDoctorId || !consultation.modalDeptId) {
                showMessage('请选择科室及主诊名医', 'error');
                return;
            }
            const data = {
                doctorId: parseInt(consultation.modalDoctorId),
                deptId: parseInt(consultation.modalDeptId),
                type: 'TEXT',
                symptomDescription: consultation.modalSymptom
            };
            api.post('/consultations/create', data)
                .then(res => {
                    if (res.data.code === 200) {
                        showMessage('问诊申请已异步发起！正在进诊室', 'success');
                        closeConsultModal();
                        enterChat(res.data.data);
                    } else {
                        showMessage(res.data.message || '发起失败', 'error');
                    }
                });
        };

        const enterChat = (cItem) => {
            consultation.activeChatId = cItem.id;
            consultation.chatDoctorName = cItem.doctorName || '待分配医师';
            consultation.chatDoctorDept = cItem.deptName || '';
            consultation.chatStatus = cItem.status;
            consultation.chatStatusLabel = cItem.statusText || '待接诊';
            consultation.messages = [];
            lastMessageId = 0;

            // Fetch details & history
            api.get(`/consultations/${cItem.id}`)
                .then(res => {
                    if (res.data.code === 200) {
                        consultation.messages = res.data.data.messages || [];
                        if (consultation.messages.length > 0) {
                            lastMessageId = consultation.messages[consultation.messages.length - 1].id;
                        }
                        scrollToBottom();
                        startChatPolling();
                    }
                });
        };

        const exitChat = () => {
            stopChatPolling();
            consultation.activeChatId = null;
            loadMyConsultations();
        };

        const startChatPolling = () => {
            stopChatPolling();
            if (consultation.chatStatus < 2) {
                chatPollingTimer = setInterval(pollMessages, 3000);
            }
        };

        const stopChatPolling = () => {
            if (chatPollingTimer) {
                clearInterval(chatPollingTimer);
                chatPollingTimer = null;
            }
        };

        const pollMessages = () => {
            if (!consultation.activeChatId) return;
            api.get(`/consultations/${consultation.activeChatId}/messages?afterMessageId=${lastMessageId}`)
                .then(res => {
                    if (res.data.code === 200 && res.data.data.length > 0) {
                        consultation.messages.push(...res.data.data);
                        lastMessageId = res.data.data[res.data.data.length - 1].id;
                        scrollToBottom();
                    }
                });
        };

        const sendConsultMessage = () => {
            if (!consultation.inputText.trim() || !consultation.activeChatId) return;
            const text = consultation.inputText.trim();
            consultation.inputText = '';

            api.post(`/consultations/${consultation.activeChatId}/message`, {
                content: text,
                messageType: 'TEXT'
            }).then(res => {
                if (res.data.code === 200) {
                    pollMessages();
                } else {
                    showMessage(res.data.message || '发送失败', 'error');
                }
            });
        };

        const scrollToBottom = () => {
            setTimeout(() => {
                const el = document.getElementById('chatMessages');
                if (el) el.scrollTop = el.scrollHeight;
            }, 100);
        };

        const fetchPrescriptionDetail = (rxId) => {
            api.get(`/prescriptions/${rxId}`)
                .then(res => {
                    if (res.data.code === 200) {
                        modal.content = 'prescription';
                        modal.data = res.data.data;
                        modal.show = true;
                    }
                });
        };

        // --- Reports Query ---
        const switchReportsTab = (tab) => {
            reportsTab.value = tab;
            if (tab === 'lab') {
                api.get('/reports/lab/my-list')
                    .then(res => {
                        if (res.data.code === 200) reports.labs = res.data.data || [];
                    });
            } else {
                api.get('/reports/imaging/my-list')
                    .then(res => {
                        if (res.data.code === 200) reports.imagings = res.data.data || [];
                    });
            }
        };

        const fetchLabReportDetail = (id) => {
            api.get('/reports/lab/' + id)
                .then(res => {
                    if (res.data.code === 200) {
                        modal.content = 'labReport';
                        const report = res.data.data;
                        report.parsedResult = [];
                        try {
                            const obj = JSON.parse(report.result || '{}');
                            report.parsedResult = Object.entries(obj).map(([k, v]) => ({
                                key: k,
                                value: v.value,
                                unit: v.unit || '',
                                range: v.range || '',
                                flag: v.flag || ''
                            }));
                        } catch(e) {}
                        modal.data = report;
                        modal.show = true;
                    }
                });
        };

        const fetchImagingReportDetail = (id) => {
            api.get('/reports/imaging/' + id)
                .then(res => {
                    if (res.data.code === 200) {
                        modal.content = 'imagingReport';
                        modal.data = res.data.data;
                        modal.show = true;
                    }
                });
        };

        // --- Health Profile Data ---
        const loadHealthProfileData = () => {
            api.get('/profile/my')
                .then(res => {
                    if (res.data.code === 200) {
                        healthProfile.info = res.data.data;
                    }
                });
        };

        const downloadHealthProfile = () => {
            const role = localStorage.getItem('role');
            if (role !== 'PATIENT') {
                showMessage('越权过滤：仅限患者档案查阅导出', 'error');
                return;
            }
            const token = localStorage.getItem('token');
            if (!token) return;

            const btn = document.getElementById('btnDownloadProfile');
            if (btn) {
                btn.disabled = true;
                btn.textContent = '⌛ 档案加密下载中...';
            }

            fetch('/api/profile/export', {
                method: 'GET',
                headers: {
                    'Authorization': 'Bearer ' + token
                }
            })
            .then(resp => {
                if (resp.status === 401) throw new Error('Unauthenticated');
                if (resp.status === 403) throw new Error('Forbidden role mapping');
                if (!resp.ok) throw new Error('Export service error');
                return resp.blob();
            })
            .then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = `芯芯数字化健康档案_${new Date().toISOString().slice(0,10)}.md`;
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
                showMessage('档案已安全下载至本地', 'success');
            })
            .catch(err => showMessage('导出健康表单错误: ' + err.message, 'error'))
            .finally(() => {
                if (btn) {
                    btn.disabled = false;
                    btn.innerHTML = '<span>⬇️</span> 一键下载健康档案 (MD版)';
                }
            });
        };

        // --- Message Center Notifications ---
        const loadNotificationsData = () => {
            api.get('/notifications/my-list')
                .then(res => {
                    if (res.data.code === 200) {
                        notifications.value = res.data.data || [];
                        loadUnreadCount();
                    }
                });
        };

        const loadUnreadCount = () => {
            api.get('/notifications/unread-count')
                .then(res => {
                    if (res.data.code === 200) {
                        unreadCount.value = res.data.data.count || 0;
                    }
                });
        };

        const markNotifRead = (id) => {
            api.post(`/notifications/${id}/read`)
                .then(() => {
                    loadNotificationsData();
                });
        };

        const markAllNotificationsRead = () => {
            api.post('/notifications/read-all')
                .then(() => {
                    loadNotificationsData();
                    showMessage('所有通知已标为已读', 'success');
                });
        };

        // --- Help Center & FAQs ---
        const loadFaqsData = () => {
            let url = '/service/faqs';
            if (faqFilterCat.value) url += `?category=${faqFilterCat.value}`;
            api.get(url)
                .then(res => {
                    if (res.data.code === 200) {
                        faqs.value = (res.data.data || []).map(f => ({ ...f, _open: false }));
                    }
                });
        };

        const filterFaqs = (cat) => {
            faqFilterCat.value = cat;
            loadFaqsData();
        };

        const searchFaqs = () => {
            if (!filters.faqQuery.trim()) {
                loadFaqsData();
                return;
            }
            api.get('/service/faqs/search?keyword=' + encodeURIComponent(filters.faqQuery))
                .then(res => {
                    if (res.data.code === 200) {
                        faqs.value = (res.data.data || []).map(f => ({ ...f, _open: false }));
                    }
                });
        };

        const submitComplaint = () => {
            if (!complaint.title || !complaint.content) {
                showMessage('请填写反馈标题和具体陈述内容', 'error');
                return;
            }
            api.post('/service/complaints', complaint)
                .then(res => {
                    if (res.data.code === 200) {
                        showMessage('非常感谢！您的反馈已被系统加密收集。', 'success');
                        complaint.title = '';
                        complaint.content = '';
                        complaint.contact = '';
                    } else {
                        showMessage(res.data.message || '提交失败', 'error');
                    }
                });
        };

        // Lifecycle Hook initialization
        onMounted(() => {
            todayDate.value = new Date().toISOString().split('T')[0];
            checkLoginStatus();
        });

        onBeforeUnmount(() => {
            stopChatPolling();
        });

        return {
            currentPage,
            isLoggedIn,
            currentUser,
            toast,
            filters,
            departments,
            deptList,
            doctors,
            booking,
            bookingDoctors,
            bookingTimeSlots,
            myAppts,
            modal,
            consultation,
            consultationDoctors,
            notifications,
            unreadCount,
            faqs,
            faqFilterCat,
            complaint,
            authTab,
            loginForm,
            registerForm,
            registerTip,
            reportsTab,
            reports,
            healthProfile,
            todayDate,

            // Methods
            showMessage,
            showPage,
            handleLoginSubmit,
            checkUsernameAvailable,
            handleRegisterSubmit,
            logout,
            searchDepartments,
            loadDoctorsByDept,
            searchDoctors,
            onBookingDeptChange,
            onBookingDoctorOrDateChange,
            quickBook,
            submitAppointment,
            searchMyAppointments,
            changeApptsPage,
            showAppointmentDetail,
            cancelAppointment,
            rescheduleAppointment,
            closeUnifiedModal,
            openConsultModal,
            onConsultDeptChange,
            closeConsultModal,
            openVideoConsult,
            submitConsultation,
            enterChat,
            exitChat,
            sendConsultMessage,
            fetchPrescriptionDetail,
            switchReportsTab,
            fetchLabReportDetail,
            fetchImagingReportDetail,
            downloadHealthProfile,
            markAllNotificationsRead,
            markNotifRead,
            filterFaqs,
            searchFaqs,
            submitComplaint
        };
    }
}).mount('#app');
