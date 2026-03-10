# ClassWeave 提交信息速查表
```text
推荐格式：

type(scope): 中文说明

例如：

feat(schedule): 支持课程表 JSON 导入
fix(frontend): 修复刷新后页面 404 问题
docs(readme): 补充项目开发初衷
1. 常用前缀
feat

新增功能

feat(auth): 新增用户登录功能
feat(schedule): 支持课程表创建与编辑
feat(share): 支持分享链接领取课表
feat(status): 新增空闲状态面板
fix

修复问题 / Bug

fix(frontend): 修复页面刷新后路由 404
fix(auth): 修复 JWT 解析失败问题
fix(schedule): 修复课程时间冲突判断错误
fix(share): 修复无效分享链接提示异常
docs

文档修改

docs(readme): 重写项目介绍
docs(api): 更新课程表导入接口文档
docs(database): 补充数据库表说明
refactor

重构代码（不算新功能，也不是单纯修 bug）

refactor(backend): 提取统一响应封装类
refactor(schedule): 重构课程表服务层逻辑
refactor(frontend): 拆分课表页面组件
style

格式调整（不改逻辑）

style(frontend): 调整页面代码格式
style(backend): 统一实体类字段排版
chore

杂项修改、配置修改、初始化提交、依赖更新

chore(init): 初始化项目结构
chore(git): 补充 gitignore 配置
chore(deps): 更新前端依赖
chore(build): 调整前端构建输出目录
test

测试相关

test(schedule): 添加课程表接口测试
test(auth): 补充登录逻辑测试
2. scope 怎么写

ClassWeave 里常用这些 scope 就够了：

frontend：前端整体

backend：后端整体

auth：登录注册 / 认证授权

schedule：课程表

share：分享功能

status：状态面板 / 空闲分析

api：接口文档

readme：项目说明

database：数据库脚本

build：构建产物 / 打包配置

deps：依赖

init：初始化

3. 直接可抄的常用模板
项目初始化
chore(init): 初始化项目结构
改 README
docs(readme): 补充项目开发初衷
docs(readme): 更新项目结构说明
docs(readme): 优化项目简介表述
写后端接口
feat(backend): 新增课程表相关接口
feat(auth): 新增用户注册与登录接口
feat(share): 新增课表分享领取接口
修后端问题
fix(backend): 修复接口返回字段异常
fix(auth): 修复登录鉴权失败问题
fix(schedule): 修复课程节次保存错误
写前端页面
feat(frontend): 新增课表周视图页面
feat(frontend): 新增登录注册页面
feat(frontend): 支持多课表叠加显示
修前端问题
fix(frontend): 修复路由跳转异常
fix(frontend): 修复分享页加载失败问题
fix(frontend): 修复课表显示错位问题
数据库改动
feat(database): 新增课程表相关数据表
fix(database): 修正课程时间字段类型
docs(database): 更新初始化脚本说明
重构
refactor(backend): 重构统一异常处理逻辑
refactor(frontend): 拆分课程表页面组件
refactor(schedule): 简化课程导入流程
配置 / 部署 / 构建
chore(build): 调整前端打包输出路径
chore(backend): 更新生产环境配置读取方式
chore(deps): 升级 Spring Boot 版本
4. 推荐写法

推荐：

feat(schedule): 支持课程表 JSON 批量导入
fix(frontend): 修复刷新后页面 404 问题
docs(readme): 增加项目开发背景说明

不推荐：

修改
更新
改一下
终于好了
final
最终版
真最终版
5. 你这个项目最实用的习惯

建议统一成：

英文前缀(模块): 中文说明

也就是：

type(scope): 中文说明

这样兼顾：

看起来规范

自己看得懂

以后放 GitHub 也舒服

后面查提交记录不会想骂自己

6. 一份超短版备忘录
feat: 新功能
fix: 修 bug
docs: 改文档
refactor: 重构
style: 改格式
chore: 杂项
test: 测试

常用写法：

feat(schedule): ...
fix(frontend): ...
docs(readme): ...
chore(init): ...
```