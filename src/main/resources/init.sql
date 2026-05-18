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
-- 插入测试数据
-- ========================================

-- 插入默认管理员账户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, phone, email, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lqkkO9QS3TzCjH3rS', '系统管理员', '13800138000', 'admin@hospital.com', 'ADMIN', 1);

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
('doctor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lqkkO9QS3TzCjH3rS', '张医生', '13800138001', 'doctor1@hospital.com', 'DOCTOR', 1),
('doctor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lqkkO9QS3TzCjH3rS', '李医生', '13800138002', 'doctor2@hospital.com', 'DOCTOR', 1);

-- 插入测试医生信息
INSERT INTO doctor (user_id, dept_id, title, specialty, education, experience, introduction, consultation_fee, rating, status) VALUES
(2, 1, '主任医师', '心血管疾病、高血压', '博士研究生', 15, '从事内科临床工作15年，擅长心血管疾病诊治', 50.00, 4.8, 1),
(3, 2, '副主任医师', '普外科手术、微创手术', '硕士研究生', 10, '擅长各类普外科手术，精通腹腔镜微创技术', 40.00, 4.6, 1);

-- 插入测试排班
INSERT INTO schedule (doctor_id, work_date, time_slot, total_slots, available_slots, status) VALUES
(1, CURDATE(), 'MORNING', 20, 20, 1),
(1, CURDATE(), 'AFTERNOON', 20, 20, 1),
(2, CURDATE() + INTERVAL 1 DAY, 'MORNING', 20, 20, 1),
(2, CURDATE() + INTERVAL 1 DAY, 'AFTERNOON', 20, 20, 1);

-- 插入测试药品
INSERT INTO medicine (medicine_name, medicine_code, specification, manufacturer, price, stock, category, status) VALUES
('阿莫西林胶囊', 'AMXL001', '0.5g*24粒', '华北制药', 15.50, 500, '抗生素', 1),
('布洛芬缓释胶囊', 'BLF001', '0.3g*20粒', '中美史克', 22.00, 300, '解热镇痛', 1),
('感冒灵颗粒', 'GML001', '10g*9袋', '华润三九', 18.80, 400, '感冒用药', 1);

SELECT '数据库初始化完成！' AS message;
