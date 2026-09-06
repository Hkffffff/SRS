# 考研自习室预约管理系统（SRS）

SRS 是一个前后端分离的考研自习室预约管理系统，用于管理自习室、座位、学生预约、黑名单和预约信用等业务。系统包含学生端和管理员端，适合作为课程设计、毕业设计或前后端分离项目学习示例。

## 项目结构

```text
SRS/
├── frontend/                 # 前端项目：Vue 3 + Vite
├── SRS/                      # 后端项目：Spring Boot 3 + MyBatis-Plus
├── srs.sql                   # MySQL 数据库初始化脚本
├── README.md                 # 项目说明文档
└── .gitignore                # Git 忽略规则
```

## 功能概览

### 学生端

- 学生登录与身份校验
- 查看自习室和座位信息
- 在线预约座位
- 查看个人预约记录
- 查看预约信用与违规相关信息

### 管理员端

- 学生信息管理
- 自习室管理
- 座位管理
- 预约记录管理
- 黑名单管理

## 技术栈

### 前端

- Vue 3
- Vue Router
- Axios
- Vite

### 后端

- Java 17
- Spring Boot 3.2.5
- Spring Web
- Spring JDBC
- MyBatis-Plus 3.5.5
- Lombok
- Hutool

### 数据库

- MySQL 8.x

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 18+
- npm
- MySQL 8.x

## 快速开始

### 1. 初始化数据库

在 MySQL 中创建数据库：

```sql
CREATE DATABASE srs DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

然后导入根目录下的 `srs.sql`。

### 2. 修改后端配置

后端配置文件位于：

```text
SRS/src/main/resources/application.yml
```

默认配置：

- 后端端口：`8080`
- 数据库地址：`jdbc:mysql://localhost:3306/srs`
- 数据库用户名：`root`
- 数据库密码：`2025125`

如果你的 MySQL 用户名、密码或端口不同，请先修改 `application.yml`。

### 3. 启动后端

```bash
cd SRS
mvn spring-boot:run
```

后端默认运行在：

```text
http://localhost:8080
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务默认运行在：

```text
http://localhost:5173
```

## 前后端联调说明

后端默认允许以下前端地址跨域访问：

- `http://localhost:5173`
- `http://127.0.0.1:5173`
- `http://localhost:4173`
- `http://127.0.0.1:4173`

前端接口基础地址可通过环境变量 `VITE_API_BASE_URL` 配置。例如在 `frontend/.env.development` 中添加：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 常用命令

### 后端

```bash
cd SRS
mvn spring-boot:run
mvn test
mvn clean package
```

### 前端

```bash
cd frontend
npm install
npm run dev
npm run build
npm run preview
```

## 主要目录说明

### 后端目录

```text
SRS/src/main/java/com/example/srs/
├── auth/            # 登录用户和鉴权辅助类
├── common/          # 统一返回结果、分页结果、全局异常处理
├── config/          # Web、拦截器、MyBatis-Plus 配置
├── controller/      # 学生端接口控制器
├── controller/admin/# 管理员端接口控制器
├── domain/          # 实体类
├── dto/             # 请求参数对象
├── mapper/          # MyBatis-Plus Mapper
├── service/         # 业务接口
├── service/impl/    # 业务实现
└── task/            # 定时任务
```

### 前端目录

```text
frontend/src/
├── api/         # 接口请求封装
├── assets/      # 静态资源
├── components/  # 通用组件
├── router/      # 路由配置
├── stores/      # 会话状态管理
├── utils/       # 工具函数
└── views/       # 页面视图
```

## 备注

- 项目后端强制使用 JDK 17 构建和运行。
- 后端包含预约超时处理相关定时任务。
- `srs.sql` 用于初始化数据库结构和基础数据。
- 生产部署时请修改数据库连接、跨域白名单和前端接口地址。
