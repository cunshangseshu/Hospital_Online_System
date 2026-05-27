# 智慧医院管理系统 - 三层架构设计图

> **项目说明**：基于 Spring Boot + MyBatis Plus + AJAX 的智慧医院管理系统，涵盖预约挂号、在线问诊、处方管理三大核心业务。
>
> **最后更新**：2026-05-27

---

## 技术栈

### 后端技术
- **框架**：Spring Boot 2.7.18
- **ORM**：MyBatis Plus 3.5.3.1
- **数据库**：MariaDB (兼容 MySQL)
- **安全**：Spring Security + JWT (jjwt 0.11.5)
- **构建工具**：Maven
- **其他**：Lombok

### 前端技术
- **HTML5/CSS3**：页面结构和样式（含聊天气泡、渐变动画）
- **JavaScript (原生)**：XMLHttpRequest 实现 AJAX
- **DOM 操作**：动态更新页面内容

---

## 系统整体架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                        客户端 (Browser)                              │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │                    前端页面层                                 │  │
│  │  index.html + video-consultation.html + CSS + JS (AJAX)     │  │
│  │                                                               │  │
│  │  • 首页 · 科室介绍 · 医生团队                                  │  │
│  │  • 预约挂号（四级级联 AJAX）                                   │  │
│  │  • 我的预约（列表/取消/改签）                                  │  │
│  │  • 在线问诊（聊天/处方/轮询）  ✨                             │  │
│  │  • 用户登录/注册（实时验证）                                   │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │ AJAX请求 (XMLHttpRequest)
                             │ HTTP/HTTPS + JWT Bearer Token
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     表示层 (Presentation Layer)                      │
│                   Controller - RESTful API (13个)                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  AuthController          - /api/auth/*         (登录/注册)    │  │
│  │  DepartmentController    - /api/departments/*  (科室管理)     │  │
│  │  DoctorController        - /api/doctors/*      (医生管理)     │  │
│  │  ScheduleController      - /api/schedules/*    (排班查询)     │  │
│  │  AppointmentController   - /api/appointments/* (预约管理)     │  │
│  │  ConsultationController  - /api/consultations/*(在线问诊) ✨  │  │
│  │  PrescriptionController  - /api/prescriptions/*(处方管理) ✨  │  │
│  │  DoctorWorkspaceController - /api/doctor-workspace/*(医生端)  │  │
│  │  LabReportController     - /api/reports/lab/*  (检验报告)     │  │
│  │  ImagingReportController - /api/reports/imaging/*(影像报告)   │  │
│  │  HealthProfileController - /api/profile/*      (健康档案)     │  │
│  │  NotificationController  - /api/notifications/*(消息中心)     │  │
│  │  ServiceController       - /api/service/*      (客服中心)     │  │
│  │                                                               │  │
│  │  职责：接收请求 → 参数验证 → 调用Service → 返回JSON            │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │ 方法调用
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   业务逻辑层 (Business Logic Layer)                  │
│                      Service - 业务逻辑 (14个实现)                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  【预约挂号】                                                  │  │
│  │  UserServiceImpl          - register / login / JWT            │  │
│  │  DepartmentServiceImpl    - 科室查询 / 搜索                    │  │
│  │  DoctorServiceImpl        - 医生查询 / 级联 / 搜索             │  │
│  │  ScheduleServiceImpl      - 排班查询 / 号源增减(事务)          │  │
│  │  AppointmentServiceImpl   - 预约CRUD / 分页 / 改签(事务)       │  │
│  │                                                               │  │
│  │  【在线问诊】 ✨                                               │  │
│  │  ConsultationServiceImpl  - 问诊CRUD / 消息收发 / 轮询         │  │
│  │  PrescriptionServiceImpl  - 处方开具 / 明细管理(事务)          │  │
│  │  MedicineServiceImpl      - 药品查询                           │  │
│  │                                                               │  │
│  │  职责：业务逻辑 · 事务管理(@Transactional) · 数据校验           │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │ 数据操作
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   数据访问层 (Data Access Layer)                     │
│                 Mapper - MyBatis Plus (17个)                         │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  User · Department · Doctor · Schedule · Appointment         │  │
│  │  MedicalRecord · Medicine                                     │  │
│  │  Consultation · ConsultationMessage ✨                        │  │
│  │  Prescription · PrescriptionItem ✨                           │  │
│  │                                                               │  │
│  │  职责：SQL执行 · ORM映射 · CRUD · 分页                         │  │
│  └──────────────────────────────────────────────────────────────┘  │
└────────────────────────────┬────────────────────────────────────────┘
                             │ SQL
                             ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        数据库层 (Database)                           │
│                       MariaDB (16张表)                               │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  【基础数据】                                                  │  │
│  │  sys_user · department · doctor · medicine                    │  │
│  │                                                               │  │
│  │  【预约业务】                                                  │  │
│  │  schedule · appointment · medical_record                     │  │
│  │                                                               │  │
│  │  【问诊业务】 ✨                                               │  │
│  │  consultation · consultation_message                          │  │
│  │  prescription · prescription_item                             │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 辅助组件架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                         横切关注点                                   │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  Spring Security + JWT 认证授权                               │  │
│  │  ├─ JwtAuthenticationFilter  - JWT令牌验证（userId→principal）│  │
│  │  ├─ JwtUtil                  - Token生成/解析/校验            │  │
│  │  ├─ SecurityUtils ✨          - 全局获取当前用户ID/用户名/角色  │  │
│  │  └─ SecurityConfig           - CORS + 路由权限 + 无状态会话   │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  MyBatis Plus 增强功能                                        │  │
│  │  ├─ MybatisPlusConfig    - 分页插件 (MariaDB方言)             │  │
│  │  ├─ MyMetaObjectHandler  - 自动填充(createdAt/updatedAt)     │  │
│  │  └─ @TableLogic          - 逻辑删除                           │  │
│  └──────────────────────────────────────────────────────────────┘  │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  通用组件                                                      │  │
│  │  ├─ Result<T>            - 统一返回 {code, message, data}     │  │
│  │  ├─ PageResult<T> ✨     - 分页响应 {total, pageNum, list}    │  │
│  │  ├─ DTO/VO 类 (25个)      - 数据传输与视图对象                │  │
│  │  └─ Entity 类 (16个)      - ORM 实体映射                      │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## AJAX请求流程图

### 流程一：预约挂号（四级级联）

```
用户操作                          AJAX请求序列
─────────                        ────────────

1. 进入页面                       GET /api/departments/list
   [加载科室下拉框]  ←────────────  (动态加载科室)

2. 选择科室                       GET /api/doctors/by-department/{deptId}
   [加载医生下拉框]  ←────────────  (级联查询医生)

3. 选择日期 + 医生                GET /api/schedules/by-doctor-and-date
   [加载时间段列表]  ←────────────  (查询可用号源)
   [显示剩余号源数]

4. 填写症状描述                   (无请求)

5. 点击提交                       POST /api/appointments/create
   [显示预约号]      ←────────────  (事务：减少号源 + 创建预约)
```

### 流程二：在线问诊聊天

```
用户操作                          AJAX请求序列
─────────                        ────────────

1. 发起问诊                       POST /api/consultations/create
   选择科室→医生→症状              (创建问诊记录 + 系统消息)
   [进入聊天界面]    ←────────────

2. 发送消息                       POST /api/consultations/{id}/message
   [消息发出]        ←────────────  (插入消息 + 返回VO)

3. 轮询新消息(每3秒)              GET /api/consultations/{id}/messages?afterMessageId=X
   [收到新消息]      ←────────────  (只返回新增消息，增量更新DOM)

4. 医生开具处方                   POST /api/prescriptions/create
   [处方卡片嵌入]    ←────────────  (关联药品 + 计算金额 + 处方消息)

5. 点击处方卡片                   GET /api/prescriptions/{id}
   [模态框显示详情]  ←────────────  (含药品明细表格)
```

### 流程三：实时搜索

```
用户输入关键字
     ↓
[onkeyup 事件触发]
     ↓
AJAX GET /api/departments/search?keyword=内科
     ↓
[接收 JSON 响应]
     ↓
[动态更新 DOM，无需刷新页面]
```

---

## 数据流向图

```
┌──────────┐   HTTP + JWT    ┌────────────┐   @Autowired   ┌────────────┐
│ Browser  │ ───────────────→ │ Controller │ ─────────────→ │  Service   │
│ (Client) │                  │  (7个)     │                │  (8个Impl) │
└──────────┘                  └────────────┘                └─────┬──────┘
     ↑                             ↑                              │
     │       JSON Response         │      统一返回 Result<T>       │
     │ ◄───────────────────────────┘                              │
     │                                                            ▼
     │                                                     ┌────────────┐
     │                                                     │   Mapper   │
     │                                                     │  (11个)    │
     │                                                     └─────┬──────┘
     │                                                           │ SQL
     │                                                           ▼
     │                                                     ┌────────────┐
     │                                                     │  Database  │
     │                                                     │ (16张表)   │
     │                                                     └────────────┘
     │
     │  完整数据流：
     │  1. 用户操作 → AJAX请求 (XMLHttpRequest + JWT Header)
     │  2. JwtAuthenticationFilter 解析Token → userId存入SecurityContext
     │  3. Controller 通过 SecurityUtils.getCurrentUserId() 获取用户
     │  4. Service 执行业务逻辑 + 事务管理
     │  5. Mapper 执行数据库操作 (MyBatis Plus BaseMapper)
     │  6. 结果逐层返回 → Service 组装 VO → Controller 封装 Result
     │  7. 前端接收 JSON → 动态更新 DOM
```

---

## 新增功能架构（v2.0.0）

### 在线问诊数据模型

```
consultation (问诊记录)
├── id, consultation_no, type(TEXT/VIDEO), status(0-3)
├── patient_id → sys_user
├── doctor_id  → doctor → sys_user (姓名)
├── dept_id    → department (科室名)
└── 1:N → consultation_message (消息)
    ├── sender_id, sender_type(PATIENT/DOCTOR/SYSTEM)
    ├── message_type(TEXT/IMAGE/PRESCRIPTION)
    ├── content, ref_prescription_id → prescription
    └── 1:N → prescription (处方)
        ├── prescription_no, diagnosis, advice, total_amount
        └── 1:N → prescription_item (明细)
            ├── medicine_id → medicine (药品名/规格/单价)
            ├── dosage, usage_method, frequency, days, quantity
            └── subtotal (单价×数量)
```

### SecurityUtils 设计

```
JwtAuthenticationFilter
    │
    │ 解析 JWT Token 得到 userId, username, role
    │ 设置 principal = userId (Long)
    │ 设置 details  = {userId, username, role} (Map)
    ▼
SecurityUtils (静态工具类)
    ├── getCurrentUserId()    → principal (Long)
    ├── getCurrentUsername()  → 从 details Map 获取
    └── getCurrentUserRole()  → 从 details Map 获取
    │
    │ 被以下组件调用：
    ├── AppointmentController.getCurrentUserId()
    ├── ConsultationController (通过 SecurityUtils)
    ├── AppointmentServiceImpl.getCurrentUserId()
    ├── ConsultationServiceImpl.createConsultation()
    ├── PrescriptionServiceImpl.createPrescription()
    └── UserServiceImpl.getCurrentUser()
```

### 医生端身份映射与安全边界（v3.1.0）

```
JWT principal = sys_user.id
        │
        ├─ 医生工作台：doc.user_id = 当前用户ID → doctor.id
        │   ├─ 排班查询：schedule.doctor_id = doctor.id
        │   └─ 问诊统计：consultation.doctor_id = doctor.id
        │
        ├─ 在线问诊医生端
        │   ├─ 列表：userType=DOCTOR 时先映射 doctor.id
        │   ├─ 接诊：仅分配医生可调用 /accept
        │   ├─ 发送消息：患者本人或分配医生可发送
        │   └─ 结束问诊：患者本人或分配医生可结束
        │
        └─ 患者预约端
            ├─ 详情/取消/改签：仅预约所属患者可操作
            └─ 创建预约：以 schedule.doctor_id 为准，校验 doctorId 不可伪造
```

---

## API 端点汇总

### 预约模块
| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| POST | /api/auth/login | 公开 | 登录 |
| POST | /api/auth/register | 公开 | 注册 |
| GET | /api/auth/check-username | 公开 | 用户名查重 |
| GET | /api/departments/list | 公开 | 科室列表 |
| GET | /api/doctors/list | 公开 | 医生列表 |
| GET | /api/schedules/by-doctor-and-date | 公开 | 号源查询 |
| POST | /api/appointments/create | JWT | 创建预约 |
| GET | /api/appointments/my-list | JWT | 我的预约 |
| POST | /api/appointments/{id}/cancel | JWT | 取消预约 |
| POST | /api/appointments/{id}/reschedule | JWT | 改签预约 |

### 问诊模块 ✨
| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| POST | /api/consultations/create | JWT | 发起问诊 |
| GET | /api/consultations/my-list | JWT | 问诊列表 |
| GET | /api/consultations/{id} | JWT | 问诊详情 |
| POST | /api/consultations/{id}/message | JWT | 发送消息 |
| GET | /api/consultations/{id}/messages | JWT | 轮询新消息 |
| POST | /api/consultations/{id}/accept | JWT | 医生接诊 |
| POST | /api/consultations/{id}/finish | JWT | 结束问诊 |
| POST | /api/prescriptions/create | JWT | 开具处方 |
| GET | /api/prescriptions/{id} | JWT | 处方详情 |

### 医生工作台
| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/doctor-workspace/data | JWT(医生) | 聚合获取医生基本信息、今日问诊统计、排班列表 |

### 报告、健康档案、通知与客服
| 方法 | 端点 | 认证 | 说明 |
|------|------|------|------|
| GET | /api/reports/lab/my-list | JWT | 我的检验报告 |
| GET | /api/reports/lab/{id} | JWT | 检验报告详情 |
| GET | /api/reports/imaging/my-list | JWT | 我的影像报告 |
| GET | /api/reports/imaging/{id} | JWT | 影像报告详情 |
| GET | /api/profile/my | JWT | 健康档案聚合数据 |
| GET | /api/profile/export | JWT(患者) | 下载健康档案 Markdown |
| GET | /api/notifications/my-list | JWT | 消息列表 |
| GET | /api/notifications/unread-count | JWT | 未读消息数 |
| POST | /api/notifications/{id}/read | JWT | 标记单条已读 |
| POST | /api/notifications/read-all | JWT | 全部已读 |
| GET | /api/service/faqs | 公开 | FAQ 列表 |
| GET | /api/service/faqs/search | 公开 | FAQ 搜索 |
| POST | /api/service/complaints | JWT | 提交投诉/建议/表扬 |
| GET | /api/service/complaints/my-list | JWT | 我的反馈列表 |

### 静态页面
| 路径 | 说明 |
|------|------|
| / | index.html (患者端 SPA 主页) |
| /doctor.html | 医生工作台 |
| /video-consultation.html | 视频问诊施工中页面 |

---

## 总结

本项目严格按照三层架构设计，充分应用AJAX技术：

✅ **清晰的层次结构**：Controller(13) → Service(14) → Mapper(17)  
✅ **充分的AJAX应用**：20+ 个异步交互场景  
✅ **完善的业务闭环**：挂号 → 问诊 → 处方 → 药品  
✅ **统一的认证体系**：JWT + SecurityUtils 全局可用  
✅ **现代化前端**：聊天气泡、渐变动画、响应式布局  
✅ **规范的代码实现**：注释完整、命名规范、事务控制
