# 智慧医院管理系统

## 项目简介

基于AJAX技术的多层架构软件开发 - 智慧医院管理系统

本项目是一个基于Spring Boot + AJAX的医疗信息服务平台，整合了医院信息管理（HIS）和在线预约挂号功能，采用标准的三层架构设计，充分展示了AJAX技术在Web开发中的应用。

---

## 技术栈

### 后端技术
- **框架**: Spring Boot 2.7.18
- **持久层**: MyBatis Plus 3.5.3.1
- **数据库**: MariaDB / MySQL 8.0
- **安全认证**: Spring Security + JWT
- **API规范**: RESTful API

### 前端技术
- **基础**: HTML5 + CSS3 + JavaScript (原生AJAX)
- **样式**: 自定义CSS（渐变设计、响应式布局）
- **交互**: 原生XMLHttpRequest实现AJAX异步请求

---

## 项目结构（三层架构）

```
hospital-management-system/
├── src/main/java/com/hospital/
│   ├── controller/          # 表示层 - REST API控制器
│   │   ├── AuthController.java       # 认证接口（登录/注册）
│   │   ├── DepartmentController.java # 科室管理接口
│   │   ├── DoctorController.java     # 医生管理接口
│   │   ├── ScheduleController.java   # 排班号源接口
│   │   └── AppointmentController.java# 预约管理接口
│   │
│   ├── service/             # 业务逻辑层
│   │   ├── UserService.java         # 用户服务接口
│   │   ├── DepartmentService.java   # 科室服务接口
│   │   ├── DoctorService.java       # 医生服务接口
│   │   ├── ScheduleService.java     # 排班服务接口
│   │   ├── AppointmentService.java  # 预约服务接口
│   │   └── impl/            # 服务实现类
│   │       ├── UserServiceImpl.java
│   │       ├── DepartmentServiceImpl.java
│   │       ├── DoctorServiceImpl.java
│   │       ├── ScheduleServiceImpl.java
│   │       └── AppointmentServiceImpl.java
│   │
│   ├── mapper/              # 数据访问层
│   │   ├── UserMapper.java
│   │   ├── DepartmentMapper.java
│   │   ├── DoctorMapper.java
│   │   ├── ScheduleMapper.java
│   │   ├── AppointmentMapper.java
│   │   ├── MedicalRecordMapper.java
│   │   └── MedicineMapper.java
│   │
│   ├── entity/              # 实体类
│   │   ├── User.java        # 用户实体
│   │   ├── Department.java  # 科室实体
│   │   ├── Doctor.java      # 医生实体
│   │   ├── Schedule.java    # 排班实体
│   │   ├── Appointment.java # 预约实体
│   │   ├── MedicalRecord.java # 病历实体
│   │   └── Medicine.java    # 药品实体
│   │
│   ├── dto/                 # 数据传输对象
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RegisterRequest.java
│   │   └── AppointmentRequest.java
│   │
│   ├── config/              # 配置类
│   │   ├── SecurityConfig.java      # Spring Security配置
│   │   ├── MybatisPlusConfig.java   # MyBatis Plus配置
│   │   └── MyMetaObjectHandler.java # 自动填充配置
│   │
│   ├── security/            # 安全认证
│   │   ├── JwtUtil.java             # JWT工具类
│   │   └── JwtAuthenticationFilter.java # JWT过滤器
│   │
│   ├── common/              # 通用类
│   │   └── Result.java      # 统一返回结果
│   │
│   └── HospitalApplication.java     # 主启动类
│
├── src/main/resources/
│   ├── application.yml      # 应用配置文件
│   ├── init.sql             # 数据库初始化脚本
│   └── static/              # 静态资源
│       ├── index.html       # 前端主页面
│       ├── css/
│       │   └── style.css    # 样式文件
│       └── js/
│           └── app.js       # AJAX核心逻辑
│
└── pom.xml                  # Maven配置文件
```

---

## 数据库设计

### 核心数据表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_user | 用户表 | id, username, password, role, status |
| department | 科室表 | id, dept_name, dept_code, location |
| doctor | 医生表 | id, user_id, dept_id, title, specialty |
| schedule | 排班表 | id, doctor_id, work_date, time_slot, available_slots |
| appointment | 预约表 | id, patient_id, doctor_id, appointment_no, status |
| medical_record | 病历表 | id, patient_id, doctor_id, diagnosis, prescription |
| medicine | 药品表 | id, medicine_name, price, stock |

---

## AJAX技术应用点

本项目充分体现了AJAX技术在Web开发中的应用：

### 1. 用户认证模块
- ✅ 用户名实时验证（注册时检查用户名是否可用）
- ✅ 异步登录提交（无刷新登录）
- ✅ 异步注册提交（无刷新注册）

### 2. 科室管理模块
- ✅ 科室列表动态加载（AJAX GET请求）
- ✅ 科室实时搜索（输入关键字即时搜索）

### 3. 医生管理模块
- ✅ 医生列表动态加载
- ✅ 按科室级联查询医生（选择科室后自动加载医生）
- ✅ 医生实时搜索（按姓名或擅长领域）

### 4. 预约挂号模块（核心AJAX应用）
- ✅ 科室下拉框动态加载
- ✅ 医生下拉框级联更新（选择科室后AJAX获取医生列表）
- ✅ 号源实时查询（选择医生和日期后AJAX获取可用号源）
- ✅ 剩余号源实时显示
- ✅ 预约异步提交（无刷新提交预约）
- ✅ 预约状态实时更新

### 5. 其他AJAX应用
- ✅ 表单异步验证
- ✅ 数据动态刷新
- ✅ 错误提示无刷新显示

---

## 运行步骤

### 1. 环境要求
- JDK 21+
- Maven 3.6+
- MariaDB 10.x 或 MySQL 8.0+

### 2. 数据库配置

#### 创建数据库并导入初始数据
```bash
# 登录MariaDB/MySQL
mysql -u root -p

# 执行初始化脚本
source src/main/resources/init.sql
```

#### 修改数据库配置
编辑 `src/main/resources/application.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mariadb://localhost:3306/hospital_db
    username: root
    password: your_password  # 修改为你的密码
```

### 3. 编译项目
```bash
mvn clean compile
```

### 4. 运行项目
```bash
mvn spring-boot:run
```

或者直接运行主类：`com.hospital.HospitalApplication`

### 5. 访问系统
浏览器打开：http://localhost:8080

---

## 默认账户

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 系统管理 |
| 医生 | doctor1 | admin123 | 张医生（内科） |
| 医生 | doctor2 | admin123 | 李医生（外科） |

**注意**：首次使用时请修改默认密码！

---

## 功能演示

### 1. 首页
- 系统介绍
- 功能特性展示

### 2. 科室介绍
- 查看所有科室
- 实时搜索科室

### 3. 医生团队
- 查看所有医生
- 按科室筛选
- 搜索医生

### 4. 预约挂号
- 选择科室 → 选择医生 → 选择日期 → 选择时间段 → 提交预约
- 全程AJAX无刷新操作

### 5. 用户中心
- 用户注册
- 用户登录
- 查看我的预约

---

## 项目亮点

1. **标准三层架构**：清晰的分层设计（表示层、业务逻辑层、数据访问层）
2. **AJAX技术应用**：充分的异步交互体验
3. **RESTful API设计**：规范的接口设计
4. **JWT安全认证**：安全的用户认证机制
5. **响应式设计**：适配不同屏幕尺寸
6. **代码规范**：良好的注释和命名规范

---

## 评分要点对应

| 评分项 | 对应实现 |
|--------|----------|
| 系统分析和设计 | 完整的数据库设计、清晰的架构设计 |
| 功能实现 | 用户管理、科室管理、医生管理、预约挂号等完整功能 |
| 多层架构 | Controller-Service-Mapper三层架构 |
| AJAX技术 | 10+个AJAX应用场景 |
| 代码可读性 | 分层编码、逻辑清晰、注释完整 |
| 界面美观 | 现代化UI设计、渐变色、响应式布局 |
| 安全性 | JWT认证、密码加密、SQL注入防护 |
| 创新性 | 智能推荐、实时搜索、级联查询 |

---

## 开发者信息

- 学号：U1602033
- 课程：基于AJAX技术的多层架构软件开发
- 学期：2025-2026学年第2学期

---

## 许可证

本项目仅用于教学目的。
