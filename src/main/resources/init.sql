-- ========================================
-- 智慧医院管理系统 - 数据库初始化脚本
-- 数据库: MariaDB
-- ========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS hospital_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hospital_db;

-- ========================================
-- 1. 用户表
-- ========================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    avatar VARCHAR(255) COMMENT '头像URL',
    role VARCHAR(20) NOT NULL DEFAULT 'PATIENT' COMMENT '角色：ADMIN-管理员，DOCTOR-医生，PATIENT-患者',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- ========================================
-- 2. 科室表
-- ========================================
CREATE TABLE IF NOT EXISTS department (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '科室ID',
    dept_name VARCHAR(100) NOT NULL COMMENT '科室名称',
    dept_code VARCHAR(50) UNIQUE COMMENT '科室编码',
    description TEXT COMMENT '科室描述',
    location VARCHAR(200) COMMENT '科室位置',
    phone VARCHAR(20) COMMENT '科室电话',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_dept_name (dept_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科室表';

-- ========================================
-- 3. 医生信息表
-- ========================================
CREATE TABLE IF NOT EXISTS doctor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '医生ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    dept_id BIGINT NOT NULL COMMENT '所属科室ID',
    title VARCHAR(50) COMMENT '职称：主任医师、副主任医师等',
    specialty VARCHAR(100) COMMENT '擅长领域',
    education VARCHAR(100) COMMENT '学历',
    experience INT COMMENT '从业年限',
    introduction TEXT COMMENT '医生简介',
    consultation_fee DECIMAL(10,2) DEFAULT 0 COMMENT '挂号费',
    rating DECIMAL(3,2) DEFAULT 5.0 COMMENT '评分',
    total_appointments INT DEFAULT 0 COMMENT '累计接诊数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停诊，1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (dept_id) REFERENCES department(id),
    INDEX idx_dept_id (dept_id),
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生信息表';

-- ========================================
-- 4. 排班表
-- ========================================
CREATE TABLE IF NOT EXISTS schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '排班ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    work_date DATE NOT NULL COMMENT '工作日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段：MORNING-上午，AFTERNOON-下午，EVENING-晚上',
    total_slots INT DEFAULT 20 COMMENT '总号源数',
    available_slots INT DEFAULT 20 COMMENT '剩余号源数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停诊，1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    UNIQUE KEY uk_doctor_date_slot (doctor_id, work_date, time_slot),
    INDEX idx_work_date (work_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='医生排班表';

-- ========================================
-- 5. 预约记录表
-- ========================================
CREATE TABLE IF NOT EXISTS appointment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '预约ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    dept_id BIGINT NOT NULL COMMENT '科室ID',
    appointment_date DATE NOT NULL COMMENT '预约日期',
    time_slot VARCHAR(20) NOT NULL COMMENT '时间段',
    appointment_no VARCHAR(50) UNIQUE COMMENT '预约号',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待确认，1-已确认，2-已完成，3-已取消，4-已过期',
    symptom TEXT COMMENT '症状描述',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (patient_id) REFERENCES sys_user(id),
    FOREIGN KEY (schedule_id) REFERENCES schedule(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (dept_id) REFERENCES department(id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_appointment_date (appointment_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约记录表';

-- ========================================
-- 6. 电子病历表
-- ========================================
CREATE TABLE IF NOT EXISTS medical_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '病历ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    appointment_id BIGINT COMMENT '关联预约ID',
    visit_date DATE NOT NULL COMMENT '就诊日期',
    diagnosis TEXT COMMENT '诊断结果',
    prescription TEXT COMMENT '处方信息',
    advice TEXT COMMENT '医嘱建议',
    attachments JSON COMMENT '附件（检查报告等）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (patient_id) REFERENCES sys_user(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_visit_date (visit_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电子病历表';

-- ========================================
-- 7. 药品表
-- ========================================
CREATE TABLE IF NOT EXISTS medicine (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '药品ID',
    medicine_name VARCHAR(100) NOT NULL COMMENT '药品名称',
    medicine_code VARCHAR(50) UNIQUE COMMENT '药品编码',
    specification VARCHAR(100) COMMENT '规格',
    manufacturer VARCHAR(200) COMMENT '生产厂家',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    stock INT DEFAULT 0 COMMENT '库存数量',
    category VARCHAR(50) COMMENT '分类',
    description TEXT COMMENT '说明',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_medicine_name (medicine_name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='药品表';

-- ========================================
-- 8. 在线问诊记录表
-- ========================================
CREATE TABLE IF NOT EXISTS consultation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '问诊ID',
    consultation_no VARCHAR(50) UNIQUE COMMENT '问诊编号',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    dept_id BIGINT NOT NULL COMMENT '科室ID',
    type VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '问诊类型：TEXT-文字问诊, VIDEO-视频问诊',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待接诊, 1-问诊中, 2-已完成, 3-已关闭',
    symptom_description TEXT COMMENT '主诉/症状描述',
    doctor_note TEXT COMMENT '医生诊断备注',
    started_at DATETIME COMMENT '接诊时间',
    finished_at DATETIME COMMENT '结束时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (patient_id) REFERENCES sys_user(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (dept_id) REFERENCES department(id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_doctor_id (doctor_id),
    INDEX idx_status (status),
    INDEX idx_consultation_no (consultation_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='在线问诊记录表';

-- ========================================
-- 9. 问诊消息表
-- ========================================
CREATE TABLE IF NOT EXISTS consultation_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    consultation_id BIGINT NOT NULL COMMENT '问诊ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    sender_type VARCHAR(10) NOT NULL COMMENT '发送者类型：PATIENT-患者, DOCTOR-医生, SYSTEM-系统',
    message_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT-文字, IMAGE-图片, PRESCRIPTION-处方卡片',
    content TEXT COMMENT '消息内容',
    ref_prescription_id BIGINT COMMENT '关联处方ID（处方消息时）',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    FOREIGN KEY (consultation_id) REFERENCES consultation(id),
    INDEX idx_consultation_id (consultation_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问诊消息表';

-- ========================================
-- 10. 处方表
-- ========================================
CREATE TABLE IF NOT EXISTS prescription (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '处方ID',
    prescription_no VARCHAR(50) UNIQUE COMMENT '处方编号',
    consultation_id BIGINT NOT NULL COMMENT '问诊ID',
    doctor_id BIGINT NOT NULL COMMENT '开方医生ID',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    diagnosis TEXT COMMENT '诊断结果',
    advice TEXT COMMENT '医嘱建议',
    total_amount DECIMAL(10,2) DEFAULT 0 COMMENT '处方总金额',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待审核, 1-已生效, 2-已作废',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (consultation_id) REFERENCES consultation(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    FOREIGN KEY (patient_id) REFERENCES sys_user(id),
    INDEX idx_consultation_id (consultation_id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_prescription_no (prescription_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方表';

-- ========================================
-- 11. 处方明细表
-- ========================================
CREATE TABLE IF NOT EXISTS prescription_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    prescription_id BIGINT NOT NULL COMMENT '处方ID',
    medicine_id BIGINT NOT NULL COMMENT '药品ID',
    medicine_name VARCHAR(100) COMMENT '药品名称（快照）',
    specification VARCHAR(100) COMMENT '规格（快照）',
    dosage VARCHAR(50) COMMENT '用量（如：1片/次）',
    usage_method VARCHAR(100) COMMENT '用法（如：口服、饭后服用）',
    frequency VARCHAR(50) COMMENT '频次（如：每日3次）',
    days INT COMMENT '用药天数',
    quantity INT COMMENT '数量',
    unit_price DECIMAL(10,2) COMMENT '单价',
    subtotal DECIMAL(10,2) COMMENT '小计',
    remark VARCHAR(200) COMMENT '备注',
    FOREIGN KEY (prescription_id) REFERENCES prescription(id),
    FOREIGN KEY (medicine_id) REFERENCES medicine(id),
    INDEX idx_prescription_id (prescription_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='处方明细表';

-- ========================================
-- 12. 检验报告表
-- ========================================
CREATE TABLE IF NOT EXISTS lab_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    report_no VARCHAR(50) UNIQUE COMMENT '报告编号',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT COMMENT '开单医生ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报告名称（如：血常规、肝功能）',
    category VARCHAR(50) COMMENT '分类：BLOOD-血液, URINE-尿液, BIOCHEM-生化, OTHER-其他',
    sample_type VARCHAR(50) COMMENT '样本类型',
    result TEXT COMMENT '检验结果（JSON格式，含指标名+数值+参考范围）',
    conclusion VARCHAR(500) COMMENT '检验结论',
    normal_flag TINYINT DEFAULT 1 COMMENT '是否正常：0-异常, 1-正常',
    report_date DATE COMMENT '报告日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (patient_id) REFERENCES sys_user(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_report_date (report_date),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验报告表';

-- ========================================
-- 13. 影像报告表
-- ========================================
CREATE TABLE IF NOT EXISTS imaging_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    report_no VARCHAR(50) UNIQUE COMMENT '报告编号',
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT COMMENT '开单医生ID',
    report_name VARCHAR(200) NOT NULL COMMENT '报告名称（如：胸部CT、头颅MRI）',
    modality VARCHAR(50) COMMENT '检查类型：CT, MRI, XRAY, ULTRASOUND',
    body_part VARCHAR(100) COMMENT '检查部位',
    finding TEXT COMMENT '影像所见',
    impression TEXT COMMENT '影像诊断',
    report_date DATE COMMENT '报告日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    FOREIGN KEY (patient_id) REFERENCES sys_user(id),
    FOREIGN KEY (doctor_id) REFERENCES doctor(id),
    INDEX idx_patient_id (patient_id),
    INDEX idx_report_date (report_date),
    INDEX idx_modality (modality)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='影像报告表';

-- ========================================
-- 14. 通知消息表
-- ========================================
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content TEXT COMMENT '通知内容',
    type VARCHAR(30) NOT NULL COMMENT '类型：APPOINTMENT-预约, CONSULTATION-问诊, SYSTEM-系统, REMINDER-提醒',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读：0-未读, 1-已读',
    ref_id BIGINT COMMENT '关联业务ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_is_read (is_read, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知消息表';

-- ========================================
-- 15. 常见问题表
-- ========================================
CREATE TABLE IF NOT EXISTS faq (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '问题ID',
    category VARCHAR(50) NOT NULL COMMENT '分类：APPOINTMENT-预约, PAYMENT-缴费, INSURANCE-医保, GUIDE-导诊, OTHER-其他',
    question VARCHAR(500) NOT NULL COMMENT '问题',
    answer TEXT NOT NULL COMMENT '答案',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_category (category),
    INDEX idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常见问题表';

-- ========================================
-- 16. 投诉建议表
-- ========================================
CREATE TABLE IF NOT EXISTS complaint (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '投诉ID',
    user_id BIGINT NOT NULL COMMENT '提交用户ID',
    type VARCHAR(30) NOT NULL COMMENT '类型：COMPLAINT-投诉, SUGGESTION-建议, PRAISE-表扬',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    contact VARCHAR(100) COMMENT '联系方式',
    status TINYINT DEFAULT 0 COMMENT '状态：0-待处理, 1-处理中, 2-已回复, 3-已关闭',
    reply TEXT COMMENT '回复内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    replied_at DATETIME COMMENT '回复时间',
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉建议表';

-- ========================================
-- 插入测试数据
-- ========================================

-- 插入默认管理员账户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, phone, email, role, status) VALUES
('admin', 'admin123', '系统管理员', '13800138000', 'admin@hospital.com', 'ADMIN', 1);

-- 插入测试科室
INSERT INTO department (dept_name, dept_code, description, location, phone, status, sort_order) VALUES
('内科', 'NK', '内科综合诊疗', '门诊楼2层', '020-12345001', 1, 1),
('外科', 'WK', '外科综合诊疗', '门诊楼3层', '020-12345002', 1, 2),
('儿科', 'EK', '儿科诊疗', '门诊楼4层', '020-12345003', 1, 3),
('妇产科', 'FCK', '妇产科诊疗', '住院楼5层', '020-12345004', 1, 4),
('眼科', 'YK', '眼科诊疗', '门诊楼6层', '020-12345005', 1, 5),
('口腔科', 'KQK', '口腔科诊疗', '门诊楼7层', '020-12345006', 1, 6);

-- 插入测试医生用户
INSERT INTO sys_user (username, password, real_name, phone, email, role, status) VALUES
('doctor1', 'doctor123', '张伟', '13800138001', 'zhangwei@hospital.com', 'DOCTOR', 1),
('doctor2', 'doctor123', '李明', '13800138002', 'liming@hospital.com', 'DOCTOR', 1),
('doctor3', 'doctor123', '王芳', '13800138003', 'wangfang@hospital.com', 'DOCTOR', 1),
('doctor4', 'doctor123', '刘强', '13800138004', 'liuqiang@hospital.com', 'DOCTOR', 1),
('doctor5', 'doctor123', '陈静', '13800138005', 'chenjing@hospital.com', 'DOCTOR', 1),
('doctor6', 'doctor123', '赵丽', '13800138006', 'zhaoli@hospital.com', 'DOCTOR', 1),
('doctor7', 'doctor123', '孙涛', '13800138007', 'suntao@hospital.com', 'DOCTOR', 1),
('doctor8', 'doctor123', '周敏', '13800138008', 'zhoumin@hospital.com', 'DOCTOR', 1),
('doctor9', 'doctor123', '吴军', '13800138009', 'wujun@hospital.com', 'DOCTOR', 1),
('doctor10', 'doctor123', '郑霞', '13800138010', 'zhengxia@hospital.com', 'DOCTOR', 1),
('doctor11', 'doctor123', '黄磊', '13800138011', 'huanglei@hospital.com', 'DOCTOR', 1),
('doctor12', 'doctor123', '林娜', '13800138012', 'linna@hospital.com', 'DOCTOR', 1);

-- 插入测试医生信息（每个科室2名医生）
INSERT INTO doctor (user_id, dept_id, title, specialty, education, experience, introduction, consultation_fee, rating, status) VALUES
-- 内科 (dept_id=1)
(2, 1, '主任医师', '心血管疾病、高血压', '博士研究生', 15, '从事内科临床工作15年，擅长心血管疾病诊治', 50.00, 4.8, 1),
(3, 1, '副主任医师', '糖尿病、内分泌疾病', '硕士研究生', 12, '专注于内分泌系统疾病，尤其在糖尿病治疗方面有丰富经验', 45.00, 4.7, 1),
-- 外科 (dept_id=2)
(4, 2, '主任医师', '普外科手术、微创手术', '博士研究生', 18, '擅长各类普外科手术，精通腹腔镜微创技术', 55.00, 4.9, 1),
(5, 2, '副主任医师', '骨科手术、创伤修复', '硕士研究生', 10, '专业从事骨科手术，擅长关节置换和创伤修复', 45.00, 4.6, 1),
-- 儿科 (dept_id=3)
(6, 3, '主任医师', '小儿呼吸系统疾病', '博士研究生', 20, '从事儿科临床工作20年，擅长小儿呼吸道疾病诊治', 50.00, 4.8, 1),
(7, 3, '主治医师', '小儿消化系统疾病', '硕士研究生', 8, '专注于儿童消化系统疾病，对小儿腹泻、消化不良有独到见解', 35.00, 4.5, 1),
-- 妇产科 (dept_id=4)
(8, 4, '主任医师', '妇科肿瘤、高危妊娠', '博士研究生', 22, '资深妇产科专家，擅长妇科肿瘤手术和高危妊娠管理', 60.00, 4.9, 1),
(9, 4, '副主任医师', '产科、计划生育', '硕士研究生', 13, '专业从事产科诊疗，在产前检查和产后护理方面经验丰富', 45.00, 4.7, 1),
-- 眼科 (dept_id=5)
(10, 5, '主任医师', '白内障、青光眼', '博士研究生', 16, '眼科专家，擅长白内障超声乳化手术和青光眼治疗', 55.00, 4.8, 1),
(11, 5, '副主任医师', '近视矫正、眼底病', '硕士研究生', 11, '专注于屈光手术和眼底疾病诊治，完成数千例近视矫正手术', 45.00, 4.6, 1),
-- 口腔科 (dept_id=6)
(12, 6, '主任医师', '口腔种植、正畸', '博士研究生', 14, '口腔种植专家，擅长复杂病例的种植修复和正畸治疗', 50.00, 4.8, 1),
(13, 6, '主治医师', '牙体牙髓、牙周病', '硕士研究生', 7, '专业从事牙体牙髓治疗和牙周病综合治疗', 35.00, 4.5, 1);

-- 插入测试排班（为所有医生创建排班）
INSERT INTO schedule (doctor_id, work_date, time_slot, total_slots, available_slots, status) VALUES
-- 内科医生排班
(1, CURDATE(), 'MORNING', 20, 20, 1),
(1, CURDATE(), 'AFTERNOON', 20, 20, 1),
(2, CURDATE() + INTERVAL 1 DAY, 'MORNING', 20, 20, 1),
(2, CURDATE() + INTERVAL 1 DAY, 'AFTERNOON', 20, 20, 1),
-- 外科医生排班
(3, CURDATE() + INTERVAL 2 DAY, 'MORNING', 20, 20, 1),
(3, CURDATE() + INTERVAL 2 DAY, 'AFTERNOON', 20, 20, 1),
(4, CURDATE() + INTERVAL 3 DAY, 'MORNING', 20, 20, 1),
(4, CURDATE() + INTERVAL 3 DAY, 'AFTERNOON', 20, 20, 1),
-- 儿科医生排班
(5, CURDATE() + INTERVAL 4 DAY, 'MORNING', 20, 20, 1),
(5, CURDATE() + INTERVAL 4 DAY, 'AFTERNOON', 20, 20, 1),
(6, CURDATE() + INTERVAL 5 DAY, 'MORNING', 20, 20, 1),
(6, CURDATE() + INTERVAL 5 DAY, 'AFTERNOON', 20, 20, 1),
-- 妇产科医生排班
(7, CURDATE() + INTERVAL 6 DAY, 'MORNING', 20, 20, 1),
(7, CURDATE() + INTERVAL 6 DAY, 'AFTERNOON', 20, 20, 1),
(8, CURDATE() + INTERVAL 7 DAY, 'MORNING', 20, 20, 1),
(8, CURDATE() + INTERVAL 7 DAY, 'AFTERNOON', 20, 20, 1),
-- 眼科医生排班
(9, CURDATE() + INTERVAL 8 DAY, 'MORNING', 20, 20, 1),
(9, CURDATE() + INTERVAL 8 DAY, 'AFTERNOON', 20, 20, 1),
(10, CURDATE() + INTERVAL 9 DAY, 'MORNING', 20, 20, 1),
(10, CURDATE() + INTERVAL 9 DAY, 'AFTERNOON', 20, 20, 1),
-- 口腔科医生排班
(11, CURDATE() + INTERVAL 10 DAY, 'MORNING', 20, 20, 1),
(11, CURDATE() + INTERVAL 10 DAY, 'AFTERNOON', 20, 20, 1),
(12, CURDATE() + INTERVAL 11 DAY, 'MORNING', 20, 20, 1),
(12, CURDATE() + INTERVAL 11 DAY, 'AFTERNOON', 20, 20, 1);

-- 插入测试药品
INSERT INTO medicine (medicine_name, medicine_code, specification, manufacturer, price, stock, category, status) VALUES
('阿莫西林胶囊', 'AMXL001', '0.5g*24粒', '华北制药', 15.50, 500, '抗生素', 1),
('布洛芬缓释胶囊', 'BLF001', '0.3g*20粒', '中美史克', 22.00, 300, '解热镇痛', 1),
('感冒灵颗粒', 'GML001', '10g*9袋', '华润三九', 18.80, 400, '感冒用药', 1);

SELECT '数据库初始化完成！' AS message;


INSERT INTO sys_user (username, password, real_name, phone, email, role, status) VALUES
('RedBeanCake', '123456', '患者', '13800138000', 'admin@hospital.com', 'PATIENT', 1);

-- 插入测试检验报告（患者ID=14，即RedBeanCake）
INSERT INTO lab_report (report_no, patient_id, doctor_id, report_name, category, sample_type, result, conclusion, normal_flag, report_date) VALUES
('LAB20260515001', 14, 1, '血常规', 'BLOOD', '静脉血',
 '{"WBC": {"value": 6.8, "unit": "10^9/L", "range": "3.5-9.5", "flag": "N"}, "RBC": {"value": 4.5, "unit": "10^12/L", "range": "4.3-5.8", "flag": "N"}, "HGB": {"value": 138, "unit": "g/L", "range": "130-175", "flag": "N"}, "PLT": {"value": 245, "unit": "10^9/L", "range": "125-350", "flag": "N"}}',
 '各项指标均在正常范围内。', 1, '2026-05-15'),

('LAB20260516001', 14, 2, '肝功能全套', 'BIOCHEM', '静脉血',
 '{"ALT": {"value": 68, "unit": "U/L", "range": "9-50", "flag": "H"}, "AST": {"value": 55, "unit": "U/L", "range": "15-40", "flag": "H"}, "GGT": {"value": 42, "unit": "U/L", "range": "10-60", "flag": "N"}, "TBIL": {"value": 18.5, "unit": "umol/L", "range": "5.1-19.0", "flag": "N"}}',
 'ALT、AST轻度升高，建议复查。注意休息，避免饮酒。', 0, '2026-05-16'),

('LAB20260518001', 14, 3, '尿常规', 'URINE', '中段尿',
 '{"PRO": {"value": "-", "unit": "", "range": "-", "flag": "N"}, "GLU": {"value": "-", "unit": "", "range": "-", "flag": "N"}, "BLD": {"value": "-", "unit": "", "range": "-", "flag": "N"}, "LEU": {"value": "-", "unit": "", "range": "-", "flag": "N"}}',
 '尿常规各项指标均正常。', 1, '2026-05-18');

-- 插入测试影像报告
INSERT INTO imaging_report (report_no, patient_id, doctor_id, report_name, modality, body_part, finding, impression, report_date) VALUES
('IMG20260516001', 14, 2, '胸部正位X线片', 'XRAY', '胸部',
 '双肺纹理清晰，未见明显实质性病变。心影大小形态正常。双侧膈面光滑，肋膈角锐利。',
 '胸部X线未见明显异常。', '2026-05-16'),

('IMG20260517001', 14, 1, '头颅CT平扫', 'CT', '头颅',
 '脑实质未见异常密度影。脑室系统大小形态正常。中线结构居中。颅骨未见骨折征象。',
 '头颅CT平扫未见异常。', '2026-05-17');

-- 插入测试通知消息
INSERT INTO notification (user_id, title, content, type, is_read, ref_id) VALUES
(14, '预约成功通知', '您已成功预约 2026-05-20 张伟 主任医师（内科）上午号源，预约号：APT20260518103000123', 'APPOINTMENT', 0, 1),
(14, '就诊提醒', '您预约的张伟主任医师明天上午（2026-05-20 08:00-12:00），请准时到门诊楼2层内科就诊。', 'REMINDER', 0, 1),
(14, '报告已生成', '您的检验报告"血常规"已于2026-05-15生成，请登录系统查看。', 'SYSTEM', 1, 1),
(14, '问诊已接诊', '您发起的文字问诊（CONS20260519001）已被张伟医生接诊，请进入问诊查看。', 'CONSULTATION', 0, 2);

-- 插入常见问题
INSERT INTO faq (category, question, answer, sort_order, status) VALUES
('APPOINTMENT', '如何预约挂号？', '在首页点击"预约挂号"，依次选择科室、医生、日期和时间段，填写症状描述后提交即可。预约成功后您将收到预约号和短信通知。', 1, 1),
('APPOINTMENT', '如何取消预约？', '登录后进入"我的预约"，找到需要取消的预约记录，点击查看详情后选择"取消预约"，填写取消原因即可。请注意：已完成的预约无法取消。', 2, 1),
('APPOINTMENT', '预约后可以改签吗？', '可以。在"我的预约"中找到预约记录，点击查看详情后选择"改签预约"，重新选择日期和时间段即可。每个预约最多可改签3次。', 3, 1),
('PAYMENT', '挂号费如何支付？', '目前支持现场缴费和线上支付两种方式。线上支付支持微信、支付宝。挂号费根据医生职称不同有所差异，主任医师50元，副主任医师40元。', 4, 1),
('PAYMENT', '医保可以报销吗？', '我院是医保定点医院。门诊挂号费、检查费、药品费等均可按规定比例报销。请就诊时携带医保卡，在缴费窗口直接结算。', 5, 1),
('INSURANCE', '异地医保如何使用？', '异地医保患者需先在参保地办理异地就医备案，备案成功后在我院可直接结算。如未备案，可先自费结算后回参保地报销。', 6, 1),
('GUIDE', '医院怎么走？', '我院位于市中心，可乘坐地铁2号线到"市医院站"A出口，或乘坐公交1路、5路、12路到"市医院"站下车。自驾患者可导航至"芯芯医院"，院内有地下停车场。', 7, 1),
('GUIDE', '门诊时间是什么？', '门诊时间：周一至周五 8:00-12:00、14:00-18:00；周六 8:00-12:00；周日及法定节假日休息。急诊24小时开放。', 8, 1),
('GUIDE', '需要带什么证件？', '初诊患者请携带身份证、医保卡。复诊患者除上述证件外，请携带既往病历资料和检查报告。', 9, 1),
('OTHER', '如何联系医院？', '客服电话：020-12345678（工作时间：8:00-18:00）。您也可以通过系统内的"客服中心"提交投诉或建议，我们会在24小时内回复。', 10, 1);

-- 插入测试投诉建议
INSERT INTO complaint (user_id, type, title, content, contact, status, reply, replied_at) VALUES
(14, 'SUGGESTION', '建议增加夜间门诊', '工作比较忙，白天没时间看病，希望医院能开设夜间门诊（18:00-21:00），方便上班族就诊。', '138****8000', 2, '感谢您的建议！我们已经提交给院办研究，预计下季度将试点开设内科夜间门诊。', '2026-05-18 14:30:00'),
(14, 'PRAISE', '感谢张伟医生', '张伟医生问诊非常耐心细致，详细解答了我的所有问题，态度和蔼可亲，是一位难得的好医生！', '', 2, '感谢您的肯定！我们会将您的表扬转达给张伟医生，激励我们做得更好！', '2026-05-17 10:15:00');



USE hospital_db;
INSERT IGNORE INTO faq (category, question, answer, sort_order, status) VALUES
('APPOINTMENT', '如何预约挂号？', '在首页点击"预约挂号"，依次选择科室、医生、日期和时间段，填写症状描述后提交即可。', 1, 1),
('APPOINTMENT', '如何取消预约？', '进入"我的预约"，找到记录点击查看详情后选择"取消预约"即可。', 2, 1),
('PAYMENT', '挂号费如何支付？', '目前支持现场缴费和线上微信/支付宝支付。', 3, 1),
('GUIDE', '门诊时间是什么？', '周一至周五 8:00-12:00、14:00-18:00；急诊24小时开放。', 4, 1);