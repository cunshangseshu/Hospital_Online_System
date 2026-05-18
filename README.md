# 芯芯数字化在线医疗服务系统

## 项目简介

基于AJAX技术的多层架构软件开发 - 芯芯数字化在线医疗服务系统

本项目是一个基于 Spring Boot 2.7 + MyBatis Plus + AJAX 的智慧医疗服务平台，涵盖预约挂号、在线问诊、处方管理三大核心业务模块，采用标准三层架构设计，充分展示 AJAX 技术在 Web 开发中的应用。

---

## 技术栈

### 后端技术
| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| MyBatis Plus | 3.5.3.1 | ORM / 持久层 |
| Spring Security | 5.7.x | 安全认证 |
| JWT (jjwt) | 0.11.5 | 无状态 Token 认证 |
| MariaDB | 10.x | 数据库 |
| Lombok | - | 简化代码 |
| Maven | 3.6+ | 构建工具 |
| Java | 21 | 运行环境 |

### 前端技术
- HTML5 + CSS3（渐变设计、毛玻璃效果、聊天气泡）
- JavaScript 原生 XMLHttpRequest（AJAX 异步交互）
- 响应式布局（适配桌面端和移动端）

---

## 项目结构（三层架构）

```
hospital-management-system/
├── pom.xml                          # Maven 配置
├── README.md                        # 项目说明（本文件）
├── ARCHITECTURE.md                  # 架构设计文档
├── docs/
│   ├── 我的预约-功能.md              # 预约模块 API 文档
│   └── 在线问诊-功能.md              # 问诊模块 API 文档
│
├── src/main/java/com/hospital/
│   ├── HospitalApplication.java     # Spring Boot 启动类
│   │
│   ├── common/
│   │   └── Result.java              # 统一响应 {code, message, data}
│   │
│   ├── config/
│   │   ├── SecurityConfig.java      # Spring Security + CORS
│   │   ├── MybatisPlusConfig.java   # MyBatis Plus 分页插件
│   │   └── MyMetaObjectHandler.java # 自动填充 createdAt/updatedAt
│   │
│   ├── security/
│   │   ├── JwtUtil.java             # JWT 生成/解析/验证
│   │   ├── JwtAuthenticationFilter.java  # JWT 认证过滤器
│   │   └── SecurityUtils.java       # 安全上下文工具类
│   │
│   ├── entity/                      # 实体层 (11个实体)
│   │   ├── User.java               # 用户 (sys_user)
│   │   ├── Doctor.java             # 医生信息 (doctor)
│   │   ├── Department.java         # 科室 (department)
│   │   ├── Schedule.java           # 排班 (schedule)
│   │   ├── Appointment.java        # 预约 (appointment)
│   │   ├── MedicalRecord.java      # 电子病历 (medical_record)
│   │   ├── Medicine.java           # 药品 (medicine)
│   │   ├── Consultation.java       # 问诊记录 (consultation)
│   │   ├── ConsultationMessage.java # 问诊消息 (consultation_message)
│   │   ├── Prescription.java       # 处方 (prescription)
│   │   └── PrescriptionItem.java   # 处方明细 (prescription_item)
│   │
│   ├── dto/                         # 数据传输对象 (21个)
│   │   ├── LoginRequest.java / LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   ├── AppointmentRequest.java / AppointmentListItemVO.java
│   │   ├── CancelRequest.java / RescheduleRequest.java
│   │   ├── PageResult.java
│   │   ├── ConsultationRequest.java / ConsultationVO.java
│   │   ├── ConsultationMessageVO.java
│   │   ├── SendMessageRequest.java
│   │   ├── PrescriptionRequest.java / PrescriptionVO.java
│   │   └── ...
│   │
│   ├── mapper/                      # 数据访问层 (11个Mapper)
│   │   ├── UserMapper.java
│   │   ├── DepartmentMapper.java
│   │   ├── DoctorMapper.java
│   │   ├── ScheduleMapper.java
│   │   ├── AppointmentMapper.java
│   │   ├── MedicalRecordMapper.java
│   │   ├── MedicineMapper.java
│   │   ├── ConsultationMapper.java
│   │   ├── ConsultationMessageMapper.java
│   │   ├── PrescriptionMapper.java
│   │   └── PrescriptionItemMapper.java
│   │
│   ├── service/                     # 服务接口层 (7个接口)
│   │   ├── UserService.java
│   │   ├── DepartmentService.java
│   │   ├── DoctorService.java
│   │   ├── ScheduleService.java
│   │   ├── AppointmentService.java
│   │   ├── ConsultationService.java
│   │   ├── PrescriptionService.java
│   │   ├── MedicineService.java
│   │   └── impl/                    # 服务实现层 (8个实现)
│   │       ├── UserServiceImpl.java
│   │       ├── DepartmentServiceImpl.java
│   │       ├── DoctorServiceImpl.java
│   │       ├── ScheduleServiceImpl.java
│   │       ├── AppointmentServiceImpl.java
│   │       ├── ConsultationServiceImpl.java
│   │       ├── PrescriptionServiceImpl.java
│   │       └── MedicineServiceImpl.java
│   │
│   └── controller/                  # 控制器层 (7个Controller)
│       ├── AuthController.java          # /api/auth/*
│       ├── DepartmentController.java    # /api/departments/*
│       ├── DoctorController.java        # /api/doctors/*
│       ├── ScheduleController.java      # /api/schedules/*
│       ├── AppointmentController.java   # /api/appointments/*
│       ├── ConsultationController.java  # /api/consultations/*
│       └── PrescriptionController.java  # /api/prescriptions/*
│
├── src/main/resources/
│   ├── application.yml              # 应用配置（数据库/JWT/MyBatis）
│   ├── init.sql                     # 数据库初始化（11张表 + 测试数据）
│   └── static/
│       ├── index.html               # 单页应用主页面
│       ├── video-consultation.html  # 视频问诊施工中页面
│       ├── css/style.css            # 全局样式（含聊天气泡）
│       ├── js/app.js                # AJAX 核心逻辑（~1100行）
│       └── images/                  # 医院LOGO、背景图
│
└── target/                          # 编译输出
```

---

## 数据库设计

### 核心数据表（16张）

| 序号 | 表名 | 说明 | 核心字段 |
|------|------|------|----------|
| 1 | sys_user | 用户表 | id, username, password, real_name, role(ADMIN/DOCTOR/PATIENT), status |
| 2 | department | 科室表 | id, dept_name, dept_code, location, phone |
| 3 | doctor | 医生信息表 | id, user_id, dept_id, title, specialty, consultation_fee, rating |
| 4 | schedule | 排班表 | id, doctor_id, work_date, time_slot, available_slots |
| 5 | appointment | 预约记录表 | id, patient_id, doctor_id, appointment_no, status, symptom |
| 6 | medical_record | 电子病历表 | id, patient_id, doctor_id, diagnosis, prescription |
| 7 | medicine | 药品表 | id, medicine_name, medicine_code, price, stock |
| 8 | consultation | 在线问诊表 | id, consultation_no, patient_id, doctor_id, type, status |
| 9 | consultation_message | 问诊消息表 | id, consultation_id, sender_id, sender_type, message_type, content |
| 10 | prescription | 处方表 | id, prescription_no, consultation_id, diagnosis, advice, total_amount |
| 11 | prescription_item | 处方明细表 | id, prescription_id, medicine_id, dosage, usage_method, quantity |
| 12 | lab_report | 检验报告表 ✨ | id, report_no, patient_id, report_name, result, normal_flag |
| 13 | imaging_report | 影像报告表 ✨ | id, report_no, patient_id, report_name, modality, finding, impression |
| 14 | notification | 通知消息表 ✨ | id, user_id, title, content, type, is_read |
| 15 | faq | 常见问题表 ✨ | id, category, question, answer, sort_order |
| 16 | complaint | 投诉建议表 ✨ | id, user_id, type, title, content, status, reply |

---

## 功能模块总览

### 模块一：用户认证
| 功能 | AJAX 应用 | 端点 |
|------|-----------|------|
| 用户注册 | 异步提交 + 实时用户名验证 | POST /api/auth/register, GET /api/auth/check-username |
| 用户登录 | 异步提交 + JWT Token 签发 | POST /api/auth/login |

### 模块二：科室 & 医生管理
| 功能 | AJAX 应用 | 端点 |
|------|-----------|------|
| 科室列表 | 动态加载 + 实时搜索 | GET /api/departments/list, /search |
| 医生团队 | 按科室级联筛选 + 实时搜索 | GET /api/doctors/list, /by-department/{id}, /search |

### 模块三：预约挂号（核心）
| 功能 | AJAX 应用 | 端点 |
|------|-----------|------|
| 四级级联 | 科室→医生→日期→号源 逐级 AJAX 查询 | GET /api/departments, /doctors, /schedules |
| 提交预约 | 异步提交 + 号源事务控制 | POST /api/appointments/create |

### 模块四：我的预约 ✨
| 功能 | AJAX 应用 | 端点 |
|------|-----------|------|
| 预约列表 | 分页加载 + 状态筛选 + 搜索 | GET /api/appointments/my-list |
| 预约详情 | 模态框 AJAX 加载 | GET /api/appointments/{id} |
| 取消预约 | 异步提交（填写取消原因） | POST /api/appointments/{id}/cancel |
| 改签预约 | 新号源查询 + 异步改签 | POST /api/appointments/{id}/reschedule |

### 模块五：在线问诊 ✨✨（新）
| 功能 | AJAX 应用 | 端点 |
|------|-----------|------|
| 发起问诊 | 科室→医生→症状→异步创建 | POST /api/consultations/create |
| 文字聊天 | 发送消息 + 3秒轮询新消息 | POST /api/consultations/{id}/message, GET /.../messages |
| 问诊列表 | 分页 + 状态筛选 | GET /api/consultations/my-list |
| 医生接诊 | 异步状态流转 | POST /api/consultations/{id}/accept |
| 结束问诊 | 异步状态流转 | POST /api/consultations/{id}/finish |

### 模块六：处方管理 ✨✨（新）
| 功能 | AJAX 应用 | 端点 |
|------|-----------|------|
| 开具处方 | 关联药品表 + 异步提交 | POST /api/prescriptions/create |
| 处方详情 | 模态框 AJAX 加载（含明细） | GET /api/prescriptions/{id} |
| 消息内嵌 | 处方卡片嵌入聊天消息 | refPrescriptionId 关联 |

### 其他
| 功能 | 说明 |
|------|------|
| 视频问诊 | 独立施工中页面（暖心设计 + 进度条） |
| 报告查询 ✨ | 检验报告（JSON结果解析）+ 影像报告 + 异常标识 |
| 健康档案 ✨ | 个人信息 + 就诊/问诊/处方统计 + 历史记录 |
| 就医指南 ✨ | 就诊流程图 + 楼层导航表 + 停车指引 |
| 消息中心 ✨ | 预约/就诊/系统通知 + 未读计数 + 批量已读 |
| 客服中心 ✨ | FAQ分类手风琴 + 搜索 + 投诉/建议/表扬提交 |

---

## AJAX 技术应用场景汇总

本项目充分体现 AJAX 技术在 Web 开发中的 20+ 个应用点：

### 实时验证
- ✅ 注册时用户名实时查重（onblur 事件触发）

### 动态加载
- ✅ 科室列表、医生列表、号源信息动态加载
- ✅ 问诊列表、预约列表分页加载
- ✅ 问诊消息 AJAX 轮询（3秒间隔）

### 级联查询
- ✅ 科室 → 医生（预约挂号）
- ✅ 科室 → 医生（发起问诊）
- ✅ 医生 + 日期 → 号源时间段

### 异步提交
- ✅ 登录、注册、预约、取消预约、改签预约
- ✅ 发起问诊、发送消息、开具处方

### 实时搜索
- ✅ 科室搜索、医生搜索、预约搜索

---

## 运行步骤

### 1. 环境要求
- JDK 21+
- Maven 3.6+
- MariaDB 10.x 或 MySQL 8.0+

### 2. 数据库初始化
```bash
mysql -u root -p < src/main/resources/init.sql
```

### 3. 修改配置
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/hospital_db
    username: root
    password: your_password
```

### 4. 编译运行
```bash
cd hospital-management-system
mvn clean compile
mvn spring-boot:run
```

### 5. 访问系统
浏览器打开：http://localhost:8080

---

## 默认账户

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理 |
| 患者 | RedBeanCake | 123456 | 测试患者 |
| 医生 | doctor1 ~ doctor12 | doctor123 | 各科室医生 |

---

## 项目亮点

1. **标准三层架构**：Controller → Service → Mapper，职责清晰
2. **AJAX 技术深度应用**：20+ 个异步交互场景，含轮询、级联、实时搜索
3. **JWT 安全认证**：无状态 Token 认证，SecurityUtils 统一获取用户上下文
4. **完整业务闭环**：预约挂号 → 在线问诊 → 开具处方 → 报告查询 → 健康档案
5. **现代化 UI**：渐变色、毛玻璃、聊天气泡、粒子动画、手风琴FAQ、响应式布局
6. **事务控制**：号源增减、处方开具等关键操作使用 @Transactional
7. **消息通知系统**：预约提醒、就诊提醒、系统公告，支持未读计数和批量已读
8. **客服中心**：FAQ分类检索、在线投诉建议提交

---

## 更新日志

### v3.0.0 (2026-05-19) 🎉
- ✨ **新增5大功能模块**
  - 报告查询（检验报告 + 影像报告 + 指标对比）
  - 健康档案（个人信息 + 就诊统计 + 历史记录）
  - 就医指南（就诊流程 + 楼层导航 + 停车指引）
  - 消息中心（预约提醒 + 就诊提醒 + 未读计数 + 批量已读）
  - 客服中心（FAQ分类手风琴 + 搜索 + 投诉建议提交）
- 🗄️ **数据库扩展**：新增 5 张表（lab_report、imaging_report、notification、faq、complaint）+ 模拟数据
- 🎨 **主页视觉优化**：粒子动画、视差滚动、渐变增强
- 🔧 **后端扩展**：新增 5 个实体、5 个 DTO、5 个 Mapper、5 个 Service、6 个 Controller

---

## 更新日志

### v2.0.0 (2026-05-19) 🎉
- ✨ **新增"在线问诊"模块**
  - 文字在线咨询（发起问诊、实时聊天、消息轮询）
  - 在线开具处方（关联药品表、处方卡片嵌入聊天）
  - 远程医疗咨询（问诊记录完整保留、状态流转）
  - 视频问诊施工中页面（暖心设计）
- 🔧 **架构升级**
  - 新增 SecurityUtils 工具类（全局获取当前用户ID）
  - JWT 过滤器重构（userId 作为 principal）
  - 新增 4 张数据库表、4 个实体类、6 个 DTO/VO
  - 新增 2 个 Controller、4 个 Service、4 个 Mapper
- 🎨 **前端升级**
  - 新增问诊列表 + 聊天气泡界面
  - 新增处方详情弹窗（含明细表格）
  - 聊天气泡动画、处方卡片交互

### v1.1.0 (2026-05-18)
- ✨ 新增"我的预约"功能模块（列表、取消、改签）
- 🎨 UI 美化（logo、背景图、动画效果）
- 📄 新增 docs/我的预约-功能.md

### v1.0.0 (2026-05-17)
- 初始版本（用户认证、科室/医生管理、预约挂号）

---

## 课程信息

- 课程：基于AJAX技术的多层架构软件开发
- 学期：2025-2026学年第2学期

---

本项目仅用于教学目的。
