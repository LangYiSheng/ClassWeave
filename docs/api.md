# ClassWeave API 文档

## 1. 文档目标

这份文档定义第一版前后端联调接口，范围覆盖：

- 注册 / 登录
- 重设密码
- 我的课程表列表
- 课程表创建、编辑、删除
- 课表显示配置维护
- 节次时间维护
- 课程维护
- JSON 批量导入
- 分享链接生成与领取
- 实时状态分析

当前默认技术栈：

- 前端：Vue 3
- 后端：Spring Boot
- 数据库：MySQL 8

---

## 2. 通用约定

### 2.1 Base URL

```text
/api/v1
```

### 2.2 认证方式

除注册、登录、分享预览外，其余接口都要求：

```http
Authorization: Bearer <token>
```

第一版建议使用 JWT。

### 2.3 响应格式

成功响应：

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

失败响应：

```json
{
  "code": 40001,
  "message": "用户名已存在",
  "data": null
}
```

### 2.4 时间与星期约定

- `weekday`: `1-7`，分别代表周一到周日
- `time`: 使用 `HH:mm:ss`
- `date`: 使用 `YYYY-MM-DD`
- `datetime`: 使用 `YYYY-MM-DD HH:mm:ss`

### 2.5 权限约定

- `OWNER`: 可编辑、删除、分享自己的课表
- `VIEWER`: 只能查看和叠加展示分享得到的课表

---

## 3. 数据对象

### 3.1 User

```json
{
  "id": 1,
  "username": "lancher",
  "displayName": "Lan"
}
```

### 3.2 ScheduleSummary

```json
{
  "id": 12,
  "name": "大三下课程表",
  "termLabel": "2026 春",
  "description": "主课表",
  "startDate": "2026-02-24",
  "totalWeeks": 18,
  "maxPeriodsPerDay": 12,
  "defaultColor": "#1F6FEB",
  "ownerUserId": 1,
  "ownerDisplayName": "Lan",
  "accessRole": "OWNER",
  "accessSource": "OWNER",
  "displayNameOverride": null,
  "displayColor": "#1F6FEB",
  "displayOpacity": 0.85,
  "isVisibleDefault": true
}
```

### 3.3 TimeSlot

```json
{
  "id": 101,
  "periodIndex": 1,
  "startTime": "08:00:00",
  "endTime": "08:45:00"
}
```

### 3.4 Course

```json
{
  "id": 9001,
  "name": "软件工程",
  "weekday": 3,
  "startWeek": 1,
  "endWeek": 16,
  "startPeriod": 3,
  "endPeriod": 4,
  "weekType": "ALL",
  "teacher": "陈老师",
  "location": "A-302",
  "note": "双周讨论课需要带电脑",
  "isTemporary": false
}
```

### 3.5 ScheduleDetail

```json
{
  "schedule": {
    "id": 12,
    "name": "大三下课程表",
    "termLabel": "2026 春",
    "description": "主课表",
    "startDate": "2026-02-24",
    "totalWeeks": 18,
    "maxPeriodsPerDay": 12,
    "defaultColor": "#1F6FEB",
    "ownerUserId": 1,
    "ownerDisplayName": "Lan",
    "accessRole": "OWNER",
    "accessSource": "OWNER",
    "displayNameOverride": null,
    "displayColor": "#1F6FEB",
    "displayOpacity": 0.85,
    "isVisibleDefault": true
  },
  "timeSlots": [
    {
      "id": 101,
      "periodIndex": 1,
      "startTime": "08:00:00",
      "endTime": "08:45:00"
    }
  ],
  "courses": [
    {
      "id": 9001,
      "name": "软件工程",
      "weekday": 3,
      "startWeek": 1,
      "endWeek": 16,
      "startPeriod": 3,
      "endPeriod": 4,
      "weekType": "ALL",
      "teacher": "陈老师",
      "location": "A-302",
      "note": "双周讨论课需要带电脑",
      "isTemporary": false
    }
  ]
}
```

### 3.6 ShareLink

```json
{
  "id": 55,
  "scheduleId": 12,
  "shareToken": "a5d8f93c0b0a4f7e8db67edb16d6c7f1",
  "permissionType": "VIEW",
  "isActive": true,
  "expiresAt": null,
  "shareUrl": "https://example.com/share/a5d8f93c0b0a4f7e8db67edb16d6c7f1"
}
```

---

## 4. 认证接口

### 4.1 注册

`POST /api/v1/auth/register`

请求体：

```json
{
  "username": "lancher",
  "password": "12345678",
  "displayName": "Lan"
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "username": "lancher",
      "displayName": "Lan"
    }
  }
}
```

说明：

- `username` 全局唯一
- `password` 只传明文到后端，数据库仅保存哈希

### 4.2 登录

`POST /api/v1/auth/login`

请求体：

```json
{
  "username": "lancher",
  "password": "12345678"
}
```

返回同注册。

### 4.3 获取当前用户

`GET /api/v1/users/me`

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "username": "lancher",
    "displayName": "Lan"
  }
}
```

### 4.4 更新当前用户信息

`PUT /api/v1/users/me`

权限：

- 需要登录

请求体：

```json
{
  "displayName": "Lan Chen"
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 1,
    "username": "lancher",
    "displayName": "Lan Chen"
  }
}
```

说明：

- 当前阶段只支持更新 `displayName`
- 传 `null` 或空字符串时，表示清空显示名称

### 4.5 重设密码

`POST /api/v1/auth/reset-password`

权限：

- 需要登录

请求体：

```json
{
  "oldPassword": "12345678",
  "newPassword": "87654321"
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": true
}
```

---

## 5. 课程表接口

### 5.1 获取我可访问的课程表列表

`GET /api/v1/schedules`

查询参数：

- `includeArchived`: 可选，默认 `false`

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": [
    {
      "id": 12,
      "name": "大三下课程表",
      "termLabel": "2026 春",
      "description": "主课表",
      "startDate": "2026-02-24",
      "totalWeeks": 18,
      "maxPeriodsPerDay": 12,
      "defaultColor": "#1F6FEB",
      "ownerUserId": 1,
      "ownerDisplayName": "Lan",
      "accessRole": "OWNER",
      "accessSource": "OWNER",
      "displayNameOverride": null,
      "displayColor": "#1F6FEB",
      "displayOpacity": 0.85,
      "isVisibleDefault": true
    },
    {
      "id": 18,
      "name": "April 大二下",
      "termLabel": "2026 春",
      "description": "分享加入",
      "startDate": "2026-02-24",
      "totalWeeks": 18,
      "maxPeriodsPerDay": 10,
      "defaultColor": "#DF5D43",
      "ownerUserId": 2,
      "ownerDisplayName": "April",
      "accessRole": "VIEWER",
      "accessSource": "SHARE_LINK",
      "displayNameOverride": null,
      "displayColor": "#DF5D43",
      "displayOpacity": 0.75,
      "isVisibleDefault": true
    }
  ]
}
```

### 5.2 创建课程表

`POST /api/v1/schedules`

请求体：

```json
{
  "name": "大三下课程表",
  "termLabel": "2026 春",
  "description": "主课表",
  "startDate": "2026-02-24",
  "totalWeeks": 18,
  "maxPeriodsPerDay": 12,
  "defaultColor": "#1F6FEB"
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 12
  }
}
```

后端要求：

- 创建 `schedules` 后，同时自动插入一条 `schedule_accesses` 的 `OWNER` 记录

### 5.3 获取课程表详情

`GET /api/v1/schedules/{scheduleId}`

返回 `ScheduleDetail`。

### 5.4 更新课程表基础信息

`PUT /api/v1/schedules/{scheduleId}`

权限：

- 仅 `OWNER`

请求体：

```json
{
  "name": "大三下课程表（修订）",
  "termLabel": "2026 春",
  "description": "加上实验课",
  "startDate": "2026-02-24",
  "totalWeeks": 18,
  "maxPeriodsPerDay": 12,
  "defaultColor": "#1F6FEB"
}
```

说明：

- `defaultColor` 更新课表本身默认色

### 5.5 更新当前用户的课表显示设置

`PUT /api/v1/schedules/{scheduleId}/display-settings`

权限：

- 只要当前用户可访问该课表即可
- 自己的课表和共享来的课表都可调用

请求体：

```json
{
  "displayColor": "#DF5D43",
  "displayOpacity": 0.75,
  "isVisibleDefault": true
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": null
}
```

说明：

- 更新的是当前用户在 `schedule_accesses` 里的展示配置
- 不会修改课表本身的 `defaultColor`

### 5.6 更新当前用户看到的课表显示名称

`PUT /api/v1/schedules/{scheduleId}/display-name`

权限：

- 只要当前用户可访问该课表即可
- 自己的课表和共享来的课表都可调用

请求体：

```json
{
  "displayNameOverride": "April 的课表"
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": null
}
```

说明：

- 更新的是当前用户在 `schedule_accesses` 里的 `displayNameOverride`
- 传 `null` 或空字符串时，表示取消自定义显示名称

### 5.7 删除课程表

`DELETE /api/v1/schedules/{scheduleId}`

权限：

- 仅 `OWNER`

行为：

- 物理删除
- 后端会先清理课程、节次、分享链接、访问关系，再删除课表主记录

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": true
}
```

### 5.8 从我的列表中移除共享课表

`DELETE /api/v1/schedules/{scheduleId}/access`

权限：

- 仅 `VIEWER`

行为：

- 只删除当前用户对该课表的访问关系
- 不删除原课表本身
- `OWNER` 不能调用该接口移除自己的课表

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": true
}
```

---

## 6. 节次与课程接口

### 6.1 整体替换节次时间

`PUT /api/v1/schedules/{scheduleId}/time-slots`

权限：

- 仅 `OWNER`

请求体：

```json
{
  "timeSlots": [
    {
      "periodIndex": 1,
      "startTime": "08:00:00",
      "endTime": "08:45:00"
    },
    {
      "periodIndex": 2,
      "startTime": "08:55:00",
      "endTime": "09:40:00"
    }
  ]
}
```

建议后端逻辑：

- 先校验 `periodIndex` 不重复
- 再删除旧节次并批量写入新节次

### 6.2 新增课程

`POST /api/v1/schedules/{scheduleId}/courses`

权限：

- 仅 `OWNER`

请求体：

```json
{
  "name": "软件工程",
  "weekday": 3,
  "startWeek": 1,
  "endWeek": 16,
  "startPeriod": 3,
  "endPeriod": 4,
  "weekType": "ALL",
  "teacher": "陈老师",
  "location": "A-302",
  "note": "双周讨论课需要带电脑",
  "isTemporary": false
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 9001
  }
}
```

### 6.3 更新课程

`PUT /api/v1/schedules/{scheduleId}/courses/{courseId}`

权限：

- 仅 `OWNER`

请求体同新增课程。

### 6.4 删除课程

`DELETE /api/v1/schedules/{scheduleId}/courses/{courseId}`

权限：

- 仅 `OWNER`

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": true
}
```

---

## 7. JSON 导入接口

### 7.1 导入整张课程表

`POST /api/v1/schedules/import/json`

权限：

- 仅登录用户

请求体：

```json
{
  "schedule": {
    "name": "大三下课程表",
    "termLabel": "2026 春",
    "description": "从教务系统转换导入",
    "startDate": "2026-02-24",
    "totalWeeks": 18,
    "maxPeriodsPerDay": 12,
    "defaultColor": "#1F6FEB"
  },
  "timeSlots": [
    {
      "periodIndex": 1,
      "startTime": "08:00:00",
      "endTime": "08:45:00"
    },
    {
      "periodIndex": 2,
      "startTime": "08:55:00",
      "endTime": "09:40:00"
    }
  ],
  "courses": [
    {
      "name": "软件工程",
      "weekday": 3,
      "startWeek": 1,
      "endWeek": 16,
      "startPeriod": 3,
      "endPeriod": 4,
      "weekType": "ALL",
      "teacher": "陈老师",
      "location": "A-302",
      "note": null,
      "isTemporary": false
    }
  ]
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "scheduleId": 12
  }
}
```

建议后端实现：

- 整个导入流程放在一个事务中
- 任一节次或课程校验失败则整体回滚

---

## 8. 分享接口

### 8.1 创建分享链接

`POST /api/v1/schedules/{scheduleId}/share-links`

权限：

- 仅 `OWNER`

请求体：

```json
{
  "expiresAt": null
}
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "id": 55,
    "scheduleId": 12,
    "shareToken": "a5d8f93c0b0a4f7e8db67edb16d6c7f1",
    "permissionType": "VIEW",
    "isActive": true,
    "expiresAt": null,
    "shareUrl": "https://example.com/share/a5d8f93c0b0a4f7e8db67edb16d6c7f1"
  }
}
```

### 8.2 查看某张课表的分享链接列表

`GET /api/v1/schedules/{scheduleId}/share-links`

权限：

- 仅 `OWNER`

### 8.3 停用分享链接

`DELETE /api/v1/share-links/{shareLinkId}`

权限：

- 仅 `OWNER`

行为：

- 不删除记录，只把 `isActive` 改成 `false`

### 8.4 分享页预览

`GET /api/v1/shares/{shareToken}`

权限：

- 不需要登录

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "scheduleId": 12,
    "scheduleName": "大三下课程表",
    "ownerDisplayName": "Lan",
    "termLabel": "2026 春",
    "description": "主课表",
    "isExpired": false,
    "requiresLoginToAccept": true
  }
}
```

### 8.5 领取分享课表

`POST /api/v1/shares/{shareToken}/accept`

权限：

- 需要登录

行为：

- 为当前用户创建或更新一条 `schedule_accesses`
- `accessRole = VIEWER`
- `accessSource = SHARE_LINK`
- `shareLinkId = 当前分享链接 ID`

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "scheduleId": 12
  }
}
```

幂等要求：

- 同一用户重复领取同一张课表，不应报错
- 已领取时直接返回成功

---

## 9. 状态分析接口

### 9.1 获取状态面板数据

`GET /api/v1/status/panel`

查询参数：

- `scheduleIds`: 必填，逗号分隔，例如 `12,18,25`
- `date`: 必填，例如 `2026-03-09`
- `time`: 必填，例如 `15:30:00`
- `windowMinutes`: 可选，默认 `60`

示例：

```text
GET /api/v1/status/panel?scheduleIds=12,18&date=2026-03-09&time=15:30:00&windowMinutes=60
```

返回：

```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "date": "2026-03-09",
    "time": "15:30:00",
    "windowMinutes": 60,
    "current": [
      {
        "scheduleId": 12,
        "scheduleName": "大三下课程表",
        "ownerDisplayName": "Lan",
        "courseId": 9001,
        "courseName": "软件工程",
        "startTime": "13:30:00",
        "endTime": "15:10:00",
        "location": "A-302"
      }
    ],
    "free": [
      {
        "scheduleId": 18,
        "scheduleName": "April 大二下",
        "ownerDisplayName": "April"
      }
    ],
    "soonStart": [
      {
        "scheduleId": 18,
        "scheduleName": "April 大二下",
        "ownerDisplayName": "April",
        "courseId": 9100,
        "courseName": "市场调研",
        "startTime": "16:00:00",
        "minutesLeft": 30
      }
    ],
    "soonEnd": []
  }
}
```

后端计算建议：

- 根据 `date` 计算当前是第几周、星期几
- 按 `weekType` 过滤单双周
- 用课程的 `startPeriod/endPeriod` 结合 `schedule_time_slots` 推导真实开始结束时间

---

## 10. 推荐前端调用顺序

### 10.1 登录后首页

1. `GET /api/v1/users/me`
2. `GET /api/v1/schedules`

### 10.2 打开课程表编辑页

1. `GET /api/v1/schedules/{scheduleId}`
2. 编辑基础信息后调用 `PUT /api/v1/schedules/{scheduleId}`
3. 修改显示名称时调用 `PUT /api/v1/schedules/{scheduleId}/display-name`
4. 修改显示颜色 / 透明度 / 默认显示状态时调用 `PUT /api/v1/schedules/{scheduleId}/display-settings`
5. 改节次时调用 `PUT /api/v1/schedules/{scheduleId}/time-slots`
6. 改课程时调用课程增删改接口

### 10.3 打开课表叠加页

1. `GET /api/v1/schedules`
2. 用户选中多个课表
3. 分别调用 `GET /api/v1/schedules/{scheduleId}` 获取详情
4. 时间变化时调用 `GET /api/v1/status/panel`

### 10.4 通过分享链接加入课表

1. `GET /api/v1/shares/{shareToken}`
2. 登录后调用 `POST /api/v1/shares/{shareToken}/accept`
3. 回到首页重新请求 `GET /api/v1/schedules`

### 10.5 从我的列表中移除共享课表

1. 在我的课表列表中选中一张共享课表
2. 调用 `DELETE /api/v1/schedules/{scheduleId}/access`
3. 重新请求 `GET /api/v1/schedules`
