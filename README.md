# ClassWeave

有时候闲着没事，想找朋友一起玩、一起吃饭、一起摸鱼，但是又不知道对方这会儿到底有没有课。
直接去问吧，又怕人家正在上课，消息发过去怪尴尬的。
所以就想着，干脆做一个简单一点的系统，能方便地看到“谁现在没课、谁这周什么时候有空”。
于是就有了这个 ClassWeave。

ClassWeave 是一个课表管理与共享项目，支持用户注册登录、课程表创建与编辑、节次配置、课程导入、分享链接领取，以及多课表叠加查看和状态面板分析。

当前仓库采用前后端分离结构：

- `frontend`：Vue 3 + Vite + Pinia + Vue Router
- `backend`：Spring Boot 3 + Spring Security + JPA
- `docs/api.md`：接口设计文档
- `docs/database.sql`：MySQL 初始化脚本
- `frontendbuild`：前端构建产物输出目录

## 目录结构

```text
ClassWeave/
├─ frontend/         # 前端源码
├─ backend/          # 后端源码
├─ docs/             # 接口与数据库文档
├─ frontendbuild/    # 前端打包输出
└─ prototype/        # 原型稿
```

## 功能概览

- 用户注册、登录、修改密码
- 我的课表列表与课表详情管理
- 节次时间配置与课程增删改
- JSON 导入课程表
- 分享链接生成、预览、领取
- 多课表叠加展示
- 状态面板分析

## 开发环境

建议版本：

- Node.js 20+
- JDK 17
- Maven 3.9+
- MySQL 8

## 本地开发

### 1. 启动数据库

创建数据库：
直接执行 [docs/database.sql](./docs/database.sql)。

### 2. 启动后端

后端默认读取这些环境变量：

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_HOURS`
- `PORT`
- `SHARE_BASE_URL`

默认值定义在 [application.yaml](./backend/src/main/resources/application.yaml) 中；如果你使用 `backend/.env`，需要自己在启动脚本或 IDE 中把这些变量注入到运行环境。

启动命令：

```bash
cd backend
mvn spring-boot:run
```

默认后端端口是 `8080`。

### 3. 启动前端

前端可参考 [frontend/.env.example](./frontend/.env.example)：

```bash
cd frontend
npm install
npm run dev
```

前端主要环境变量：

- `VITE_API_BASE_URL`：后端 API 地址
- `VITE_APP_BASE_PATH`：前端部署子路径

本地联调时，默认可直接使用：

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

## 构建

### 前端构建

```bash
cd frontend
npm install
npm run build
```

构建产物会输出到根目录的 `frontendbuild/`。

### 后端构建

```bash
cd backend
mvn clean package
```

产物默认在 `backend/target/`，可直接使用生成的 Jar 包部署。

## 部署说明

推荐部署方式：

1. 后端 Jar 单独运行在 Linux 服务器上
2. 前端静态文件部署到 Nginx
3. 由 Nginx 提供前端页面，并将 `/api/` 请求转发到后端服务

### 后端部署

示例：

```bash
cd backend
mvn clean package
java -jar target/classweave-0.0.1-SNAPSHOT.jar
```

生产环境建议通过环境变量覆盖数据库、JWT 和分享地址配置，例如：

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/classweave?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
export DB_USERNAME='your_user'
export DB_PASSWORD='your_password'
export JWT_SECRET='replace-with-a-strong-secret'
export PORT='8080'
export SHARE_BASE_URL='https://your-domain.com/share'
java -jar target/classweave-0.0.1-SNAPSHOT.jar
```

### 前端部署

如果前端挂在站点根路径：

```env
VITE_APP_BASE_PATH=/
VITE_API_BASE_URL=https://your-domain.com/api/v1
```

如果前端挂在子路径，比如 `/classweave/`，则构建前设置：

```env
VITE_APP_BASE_PATH=/classweave/
VITE_API_BASE_URL=https://your-domain.com/classweave/api/v1
```

然后执行：

```bash
cd frontend
npm install
npm run build
```

将 `frontendbuild/` 下的文件部署到 Nginx 静态目录即可。

### Nginx 思路

- 静态资源根目录指向前端构建结果
- 前端路由开启 `try_files`
- `/api/v1/` 反向代理到 Spring Boot 服务

如果项目部署在 `/classweave/` 子路径下，需要同时保证：

- `VITE_APP_BASE_PATH=/classweave/`
- Nginx location 与静态目录匹配
- `VITE_API_BASE_URL` 指向正确的后端接口前缀

## 接口与数据文档

- 接口文档：[docs/api.md](./docs/api.md)
- 数据库脚本：[docs/database.sql](./docs/database.sql)

## 补充说明

- 前端构建输出目录是根目录的 `frontendbuild/`，不在 `frontend/dist/`
- 后端默认端口为 `8080`
- 仓库中的本地环境文件、依赖目录和构建产物已在 `.gitignore` 中忽略
