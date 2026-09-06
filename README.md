# 考研自习室预约管理系统（SRS）

一个用于考研自习室座位预约、管理与统计的前后端分离项目。系统包含学生端与管理员端，支持登录、座位查看、预约管理、黑名单管理和资源维护等功能。

## 项目结构

- `frontend/`：基于 Vue 3 + Vite 的前端项目
- `SRS/`：基于 Spring Boot 3 + MyBatis Plus 的后端项目
- `srs.sql`：数据库初始化脚本
- `DB-课设报告 模板2026.doc`：课程设计报告模板

## 功能概览

### 学生端

- 登录与身份校验
- 查看自习室与座位信息
- 在线预约座位
- 查看我的预约记录
- 预约信用/违规相关页面

### 管理员端

- 学生管理
- 自习室管理
- 座位管理
- 预约管理
- 黑名单管理

## 技术栈

- 前端：Vue 3、Vue Router、Axios、Vite
- 后端：Spring Boot 3、Spring Web、MyBatis Plus、Spring JDBC
- 数据库：MySQL

## 环境要求

- JDK 17
- Node.js 18+（推荐）
- Maven 3.9+
- MySQL 8+

## 本地运行

### 1. 导入数据库

先在 MySQL 中创建数据库 `srs`，然后执行根目录下的 `srs.sql`。

### 2. 启动后端

后端配置文件位于 `SRS/src/main/resources/application.yml`，默认配置如下：

- 端口：`8080`
- 数据库：`jdbc:mysql://localhost:3306/srs`
- 用户名：`root`
- 密码：`2025125`

启动命令：

```bash
cd SRS
mvn spring-boot:run
```

### 3. 启动前端

前端默认使用 Vite，本地开发端口通常为 `5173`。

```bash
cd frontend
npm install
npm run dev
```

## 前后端联调

- 后端已允许来自 `http://localhost:5173`、`http://127.0.0.1:5173`、`http://localhost:4173`、`http://127.0.0.1:4173` 的跨域请求。
- 前端环境变量文件为 `frontend/.env.development`，可通过 `VITE_API_BASE_URL` 指定后端地址。

示例：

```env
VITE_API_BASE_URL=http://localhost:8080
```

## 常用命令

### 前端

```bash
npm run dev
npm run build
npm run preview
```

### 后端

```bash
mvn spring-boot:run
mvn test
```

## 说明

- 后端使用 Java 17 编译和运行。
- 项目中包含自动处理预约超时的定时任务。
- 如需部署到线上环境，请自行修改数据库连接、跨域地址和前端 API 地址。

