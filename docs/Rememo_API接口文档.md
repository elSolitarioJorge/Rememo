# Rememo API 接口文档

> 文档版本：v2.3
> 接口基础地址：`/api`

---

## 目录

- [概述](#概述)
- [通用说明](#通用说明)
- [认证模块](#认证模块)
- [用户模块](#用户模块)
- [文件模块](#文件模块)
- [评论模块](#评论模块)
- [互动模块](#互动模块)
- [记忆模块](#记忆模块)
- [数据模型](#数据模型)
- [后续模块](#后续模块待补充)

---

## 概述

### 项目简介

Rememo 是一款基于地理位置的记忆类应用，用户可以在地图上标记地点，记录文字、图片、语音等内容，通过时间线回顾自己的足迹与记忆。

### 基础信息

| 项目 | 说明 |
|------|------|
| 协议 | HTTP / HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 认证方式 | Bearer Token (JWT) |

---

## 通用说明

### 请求格式

- `Content-Type: application/json`
- 请求体为 JSON 格式（文件上传接口除外）

### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| `Content-Type` | 是 | `application/json`，文件上传接口为 `multipart/form-data` |
| `Accept` | 否 | `application/json` |
| `Authorization` | 需认证接口必填 | `Bearer {token}` |

### 响应格式

所有接口统一响应格式：

```json
{
    "code": 200,
    "message": "success",
    "data": { ... }
}
```

### 状态码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 / Token 失效 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 错误响应示例

```json
{
    "code": 400,
    "message": "手机号已注册",
    "data": null
}
```

---

## 认证模块

### 接口列表

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| [注册](#1-注册) | POST | `/api/auth/register` | 用户注册 |
| [登录](#2-登录) | POST | `/api/auth/login` | 用户登录 |

---

### 1. 注册

创建新用户账号，注册成功后自动登录，返回 Token。

#### 请求信息

```
POST /api/auth/register
Content-Type: application/json
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 | 约束 |
|--------|------|------|------|------|
| phone | String | 是 | 手机号 | 11位数字，1开头 |
| password | String | 是 | 密码 | 6-20位 |

#### 请求示例

```json
{
    "phone": "13800138000",
    "password": "123456"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| userId | String | 用户ID，UUID |
| token | String | 认证令牌 |
| expiresAt | Long | Token 过期时间戳（毫秒） |

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "expiresAt": 1745316967890
    }
}
```

#### 错误响应

**手机号已注册**

```json
{
    "code": 400,
    "message": "手机号已注册",
    "data": null
}
```

**参数校验失败**

```json
{
    "code": 400,
    "message": "手机号格式不正确",
    "data": null
}
```

```json
{
    "code": 400,
    "message": "密码长度需在6-20位之间",
    "data": null
}
```

---

### 2. 登录

使用手机号和密码登录，返回 Token。

#### 请求信息

```
POST /api/auth/login
Content-Type: application/json
```

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | 密码 |

#### 请求示例

```json
{
    "phone": "13800138000",
    "password": "123456"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| userId | String | 用户ID，UUID |
| token | String | 认证令牌 |
| expiresAt | Long | Token 过期时间戳（毫秒） |

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "expiresAt": 1745316967890
    }
}
```

#### 错误响应

**用户不存在 / 密码错误**

```json
{
    "code": 400,
    "message": "手机号或密码错误",
    "data": null
}
```

---

## 用户模块

### 接口列表

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| [获取用户信息](#3-获取用户信息) | GET | `/api/user/info` | 获取当前用户信息 |
| [更新用户信息](#4-更新用户信息) | PUT | `/api/user/info` | 更新昵称、头像 |
| [修改密码](#5-修改密码) | PUT | `/api/user/password` | 修改登录密码 |
| [注销账号](#6-注销账号) | DELETE | `/api/user/account` | 注销当前用户账号 |

---

### 3. 获取用户信息

获取当前登录用户的详细信息。

#### 请求信息

```
GET /api/user/info
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}`，注册/登录接口返回的 Token |

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| userId | String | 用户ID，UUID |
| phone | String | 手机号 |
| nickname | String | 昵称 |
| avatar | String | 头像 URL，空字符串表示无头像 |
| gender | String | 性别：`male` / `female` / `secret`，默认 "secret" |
| bio | String | 个性签名，0-100 字符，默认空 |
| createdAt | Long | 注册时间戳（毫秒） |

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "phone": "13800138000",
        "nickname": "小明",
        "avatar": "https://cdn.rememo.com/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
        "gender": "male",
        "bio": "记录生活的每一个瞬间",
        "createdAt": 1745316967890
    }
}
```

#### 错误响应

**Token 无效或过期**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

---

### 4. 更新用户信息

更新当前用户的昵称、头像等信息。头像字段填写上传接口返回的 URL。

> **重要**：用户选择本地图片后，应先调用 [图片上传接口](#1-上传图片) 获取公网 URL，再将 URL 填入 avatar 字段。

#### 请求信息

```
PUT /api/user/info
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称，2-20字符，不传则保持原值 |
| avatar | String | 否 | 头像 URL（通过 `/api/upload/image` 上传后返回的 URL），不传则保持原值 |
| gender | String | 否 | 性别：`male`、`female`、`secret`，不传则保持原值 |
| bio | String | 否 | 个性签名，0-100字符，不传则保持原值 |

#### 请求示例

```json
{
    "nickname": "小明",
    "avatar": "https://cdn.rememo.com/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
    "gender": "male",
    "bio": "记录生活的每一个瞬间"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| userId | String | 用户ID |
| phone | String | 手机号 |
| nickname | String | 昵称 |
| avatar | String | 头像 URL |
| gender | String | 性别 |
| bio | String | 个性签名 |
| createdAt | Long | 注册时间戳（毫秒） |

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "userId": "550e8400-e29b-41d4-a716-446655440000",
        "phone": "13800138000",
        "nickname": "小明",
        "avatar": "https://cdn.rememo.com/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
        "gender": "male",
        "bio": "记录生活的每一个瞬间",
        "createdAt": 1745316967890
    }
}
```

#### 错误响应

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

**昵称格式错误**

```json
{
    "code": 400,
    "message": "昵称长度需在2-20字符之间",
    "data": null
}
```

---

### 5. 修改密码

修改当前登录用户的密码。

#### 请求信息

```
PUT /api/user/password
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 当前密码 |
| newPassword | String | 是 | 新密码，6-20位 |

#### 请求示例

```json
{
    "oldPassword": "123456",
    "newPassword": "654321"
}
```

#### 成功响应

```json
{
    "code": 200,
    "message": "密码修改成功",
    "data": null
}
```

#### 错误响应

**旧密码错误**

```json
{
    "code": 400,
    "message": "当前密码错误",
    "data": null
}
```

**新旧密码相同**

```json
{
    "code": 400,
    "message": "新密码不能与当前密码相同",
    "data": null
}
```

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

---

### 6. 注销账号

永久注销当前用户账号，删除所有个人数据。此操作**不可逆**，建议前端二次确认。

#### 请求信息

```
DELETE /api/user/account
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 成功响应

```json
{
    "code": 200,
    "message": "账号注销成功",
    "data": null
}
```

> 注销成功后，Token 即刻失效，前端应跳转至登录页并清除本地缓存。

#### 错误响应

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

---

## 文件模块

### 接口列表

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| [上传图片](#1-上传图片) | POST | `/api/upload/image` | 上传单张图片，返回公网 URL |

---

### 1. 上传图片

上传单张图片文件，后端存储到文件服务，返回可在全球访问的公网 URL。

> **重要**：此接口为通用图片上传接口，可用于用户头像更新、记忆发布、评论附图等场景。推荐先上传图片拿到 URL，再将 URL 填入业务接口（如头像更新、记忆发布）的对应字段。

#### 请求信息

```
POST /api/upload/image
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |
| Content-Type | 是 | `multipart/form-data` |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 图片文件，支持 JPEG、PNG、WebP，最大 10MB |
| type | String | 否 | 图片用途类型，不传默认为 `common`，可选值：<br>`avatar` - 用户头像<br>`memory` - 记忆图片<br>`common` - 通用图片 |

#### 成功响应

| 参数名 | 类型 | 说明 |
|--------|------|------|
| imageId | String | 图片ID，UUID |
| originalUrl | String | 原图公网访问 URL |
| fileSize | Long | 文件大小（字节） |
| width | Integer | 图片宽度（px） |
| height | Integer | 图片高度（px） |
| mimeType | String | MIME 类型，如 `image/jpeg` |

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "imageId": "660e8400-e29b-41d4-a716-446655440001",
        "originalUrl": "https://cdn.rememo.com/images/660e8400-e29b-41d4-a716-446655440001.jpg",
        "fileSize": 204800,
        "width": 1080,
        "height": 1920,
        "mimeType": "image/jpeg"
    }
}
```

#### 错误响应

**文件类型不支持**

```json
{
    "code": 400,
    "message": "仅支持 JPEG、PNG、WebP 格式的图片",
    "data": null
}
```

**文件大小超限**

```json
{
    "code": 400,
    "message": "图片大小不能超过 10MB",
    "data": null
}
```

**未登录**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

---

## 评论模块

### 接口列表

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| [发布评论](#1-发布评论) | POST | `/api/comments` | 对某篇记忆发布评论 |
| [获取评论列表](#2-获取评论列表) | GET | `/api/posts/{postId}/comments` | 分页获取某记忆的所有评论 |

---

### 1. 发布评论

为指定记忆帖子发布一条评论。

#### 请求信息

```
POST /api/comments
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | String | 是 | 记忆帖子ID |
| content | String | 是 | 评论内容，1-500 字符 |

#### 请求示例

```json
{
    "postId": "550e8400-e29b-41d4-a716-446655440000",
    "content": "写得真好！"
}
```

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| commentId | String | 评论ID，UUID |
| postId | String | 关联的记忆ID |
| authorId | String | 评论者用户ID |
| authorNickname | String | 评论者昵称 |
| authorAvatar | String | 评论者头像 URL |
| content | String | 评论内容 |
| createdTime | Long | 发布时间戳（毫秒） |

#### 成功响应

```json
{
    "code": 200,
    "message": "评论发布成功",
    "data": {
        "commentId": "880e8400-e29b-41d4-a716-446655440002",
        "postId": "550e8400-e29b-41d4-a716-446655440000",
        "authorId": "660e8400-e29b-41d4-a716-446655440001",
        "authorNickname": "小明",
        "authorAvatar": "https://cdn.rememo.com/avatar/660e8400-e29b-41d4-a716-446655440001.jpg",
        "content": "写得真好！",
        "createdTime": 1745316967890
    }
}
```

#### 错误响应

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

**评论内容为空**

```json
{
    "code": 400,
    "message": "评论内容不能为空",
    "data": null
}
```

**记忆帖子不存在**

```json
{
    "code": 404,
    "message": "记忆帖子不存在",
    "data": null
}
```

---

### 2. 获取评论列表

分页获取指定记忆帖子的所有评论，按发布时间倒序（最新的在前）。

#### 请求信息

```
GET /api/posts/{postId}/comments?page=1&size=20
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | String | 是 | 记忆帖子ID |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从 1 开始，默认 1 |
| size | Integer | 否 | 每页数量，默认 20，最大 100 |

#### 响应参数

列表中每个元素字段同 [发布评论](#1-发布评论) 的响应参数。

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "commentId": "880e8400-e29b-41d4-a716-446655440002",
            "postId": "550e8400-e29b-41d4-a716-446655440000",
            "authorId": "660e8400-e29b-41d4-a716-446655440001",
            "authorNickname": "小明",
            "authorAvatar": "https://cdn.rememo.com/avatar/660e8400-e29b-41d4-a716-446655440001.jpg",
            "content": "写得真好！",
            "createdTime": 1745316967890
        },
        {
            "commentId": "990e8400-e29b-41d4-a716-446655440003",
            "postId": "550e8400-e29b-41d4-a716-446655440000",
            "authorId": "770e8400-e29b-41d4-a716-446655440002",
            "authorNickname": "小红",
            "authorAvatar": "",
            "content": "同感！",
            "createdTime": 1745316900000
        }
    ]
}
```

**无评论时**

```json
{
    "code": 200,
    "message": "success",
    "data": []
}
```

---

## 互动模块

### 接口列表

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| [点赞/取消点赞](#1-点赞取消点赞) | POST | `/api/posts/{postId}/like` | 切换点赞状态 |
| [获取点赞状态](#2-获取点赞状态) | GET | `/api/posts/{postId}/like` | 获取当前用户点赞状态和点赞数 |
| [收藏/取消收藏](#3-收藏取消收藏) | POST | `/api/posts/{postId}/collect` | 切换收藏状态 |
| [获取收藏状态](#4-获取收藏状态) | GET | `/api/posts/{postId}/collect` | 获取当前用户收藏状态和收藏数 |
| [获取用户点赞列表](#5-获取用户点赞列表) | GET | `/api/user/likes` | 分页获取当前用户的点赞列表 |
| [获取用户收藏列表](#6-获取用户收藏列表) | GET | `/api/user/collects` | 分页获取当前用户的收藏列表 |

---

### 1. 点赞/取消点赞

对指定记忆帖子进行点赞或取消点赞（切换操作）。

#### 请求信息

```
POST /api/posts/{postId}/like
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | String | 是 | 记忆帖子ID |

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| postId | String | 记忆帖子ID |
| liked | Boolean | 当前用户是否已点赞 |
| likeCount | Integer | 当前点赞数 |

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "postId": "330e8400-e29b-41d4-a716-446655440003",
        "liked": true,
        "likeCount": 6
    }
}
```

---

### 2. 获取点赞状态

获取当前用户对指定记忆帖子的点赞状态和点赞数。

#### 请求信息

```
GET /api/posts/{postId}/like
Authorization: Bearer {token}
```

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | String | 是 | 记忆帖子ID |

#### 响应参数

同 [点赞/取消点赞](#1-点赞取消点赞) 响应参数。

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "postId": "330e8400-e29b-41d4-a716-446655440003",
        "liked": true,
        "likeCount": 6
    }
}
```

---

### 3. 收藏/取消收藏

对指定记忆帖子进行收藏或取消收藏（切换操作）。

#### 请求信息

```
POST /api/posts/{postId}/collect
Authorization: Bearer {token}
```

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | String | 是 | 记忆帖子ID |

#### 响应参数

| 参数名 | 类型 | 说明 |
|--------|------|------|
| postId | String | 记忆帖子ID |
| collected | Boolean | 当前用户是否已收藏 |
| collectCount | Integer | 当前收藏数 |

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "postId": "330e8400-e29b-41d4-a716-446655440003",
        "collected": true,
        "collectCount": 3
    }
}
```

---

### 4. 获取收藏状态

获取当前用户对指定记忆帖子的收藏状态和收藏数。

#### 请求信息

```
GET /api/posts/{postId}/collect
Authorization: Bearer {token}
```

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | String | 是 | 记忆帖子ID |

#### 响应参数

同 [收藏/取消收藏](#3-收藏取消收藏) 响应参数。

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "postId": "330e8400-e29b-41d4-a716-446655440003",
        "collected": false,
        "collectCount": 3
    }
}
```

---

### 5. 获取用户点赞列表

分页获取当前用户的点赞列表。

#### 请求信息

```
GET /api/user/likes?page=1&size=20
Authorization: Bearer {token}
```

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从 1 开始，默认 1 |
| size | Integer | 否 | 每页数量，默认 20，最大 100 |

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "postId": "330e8400-e29b-41d4-a716-446655440003",
            "pointId": "110e8400-e29b-41d4-a716-446655440001",
            "authorId": "550e8400-e29b-41d4-a716-446655440000",
            "authorNickname": "小明",
            "authorAvatar": "https://cdn.rememo.com/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
            "title": "难忘的国庆节",
            "contentPreview": "这一天人山人海，红旗飘扬...",
            "coverImage": "https://cdn.rememo.com/images/220e8400.jpg",
            "imageCount": 3,
            "memoryYear": 2019,
            "likeCount": 5,
            "commentCount": 2,
            "isLiked": true,
            "createdTime": 1745316967890
        }
    ]
}
```

---

### 6. 获取用户收藏列表

分页获取当前用户的收藏列表。

#### 请求信息

```
GET /api/user/collects?page=1&size=20
Authorization: Bearer {token}
```

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| page | Integer | 否 | 页码，从 1 开始，默认 1 |
| size | Integer | 否 | 每页数量，默认 20，最大 100 |

#### 成功响应

响应格式同 [获取用户点赞列表](#5-获取用户点赞列表)，返回 MemoryPostListItem 数组。

---

## 记忆模块

### 接口列表

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| [获取所有记忆点](#1-获取所有记忆点) | GET | `/api/memory-points` | 获取当前用户的所有记忆点（首页地图） |
| [发布记忆](#2-发布记忆) | POST | `/api/posts` | 发布记忆（首次发布自动创建关联记忆点） |
| [获取记忆列表（按记忆点）](#3-获取记忆列表按记忆点) | GET | `/api/memory-points/{pointId}/posts` | 获取某记忆点的所有记忆（列表接口） |
| [获取记忆列表（按年份）](#4-获取记忆列表按年份) | GET | `/api/posts/by-year` | 获取某年份的所有记忆（列表接口） |
| [获取记忆列表（按用户）](#5-获取记忆列表按用户) | GET | `/api/users/{userId}/posts` | 获取某用户的所有记忆（列表接口） |
| [获取随机记忆列表](#6-获取随机记忆列表) | GET | `/api/posts/random` | 发现页随机获取记忆列表（列表接口） |
| [获取记忆详情](#7-获取记忆详情) | GET | `/api/posts/{postId}` | 获取单条记忆的完整内容（详情接口） |

> **说明**：不再单独提供"创建记忆点"接口。发布记忆时：
> - 若传入 `pointId`，直接关联到该记忆点
> - 若不传 `pointId`，后端自动创建记忆点并关联（需传入 `pointName`、`lat`、`lng`、`address`）

> **分页策略**：
> - **Timeline（时间线）/ Profile（个人主页）/ Year（按年份）**：一次返回所有数据，前端按年份分组展示
> - **Discover（发现页）**：保留分页（limit 参数），支持上拉加载更多
> - **Comment（评论）**：保留分页，评论数量可能很多

> **重要**：记忆列表接口与详情接口返回的数据结构不同：
> - **列表接口**（如按记忆点/按年份/按用户/随机）返回 [MemoryPostListItem](#memorypostlistitem)，不含完整正文和所有图片，仅含摘要
> - **详情接口**返回 [MemoryPostDetail](#memorypostdetail)，包含完整正文和所有图片

---

### 1. 获取所有记忆点

获取当前登录用户的所有记忆点，用于首页地图显示锚点。

#### 请求信息

```
GET /api/memory-points
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 响应参数

同 [MemoryPoint](#memorypoint) 数据模型列表。

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "pointId": "110e8400-e29b-41d4-a716-446655440001",
            "latitude": 39.9042,
            "longitude": 116.4074,
            "pointName": "天安门广场",
            "locationAddress": "北京市东城区天安门广场",
            "coverImageUrl": "https://cdn.rememo.com/images/220e8400.jpg",
            "memoryCount": 5,
            "summaryText": "难忘的国庆节...",
            "minYear": 2019,
            "maxYear": 2023,
            "createdTime": 1745316967890
        }
    ]
}
```

**无记忆点时**

```json
{
    "code": 200,
    "message": "success",
    "data": []
}
```

#### 错误响应

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

---

### 2. 发布记忆

发布一条记忆。首次发布时（不传 `pointId`），后端自动创建关联的记忆点。

- 若传入 `pointId`，直接关联到该记忆点
- 若不传 `pointId`，后端自动创建记忆点并关联（需传入 `pointName`、`lat`、`lng`、`address`）

#### 请求信息

```
POST /api/posts
Authorization: Bearer {token}
Content-Type: application/json
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pointId | String | 否 | 关联的记忆点ID，不传则新建记忆点 |
| pointName | String | 条件必填 | 记忆点名称，不传则默认为 address；仅在 pointId 不传时需要 |
| lat | Double | 是 | 纬度 |
| lng | Double | 是 | 经度 |
| address | String | 是 | 详细地址 |
| title | String | 是 | 标题，1-50 字符 |
| content | String | 是 | 正文内容，1-2000 字符 |
| images | List | 否 | 图片列表，最多9张 |
| memoryYear | Integer | 是 | 记忆发生年份 |
| memorySeason | String | 是 | 记忆发生季节：`春` / `夏` / `秋` / `冬` |

#### images 子对象字段

| 参数名 | 类型 | 说明 |
|--------|------|------|
| photoId | String | 图片ID，UUID |
| originalUrl | String | 原图公网 URL |
| restoredUrl | String | AI 修复图公网 URL（可空） |
| displayState | String | 当前显示状态：`ORIGINAL` / `RESTORED` |

#### 请求示例

```json
{
    "pointId": "110e8400-e29b-41d4-a716-446655440001",
    "lat": 39.9042,
    "lng": 116.4074,
    "address": "北京市东城区天安门广场",
    "title": "难忘的国庆节",
    "content": "这一天人山人海，红旗飘扬，我的心情无比激动...",
    "images": [
        {
            "photoId": "220e8400-e29b-41d4-a716-446655440002",
            "originalUrl": "https://cdn.rememo.com/images/220e8400-e29b-41d4-a716-446655440002.jpg",
            "restoredUrl": "",
            "displayState": "ORIGINAL"
        }
    ],
    "memoryYear": 2019,
    "memorySeason": "秋"
}
```

> **首次发布示例**（不传 pointId，需传入 pointName）：

```json
{
    "pointName": "天安门广场",
    "lat": 39.9042,
    "lng": 116.4074,
    "address": "北京市东城区天安门广场",
    "title": "难忘的国庆节",
    "content": "...",
    "images": [...],
    "memoryYear": 2019,
    "memorySeason": "秋"
}
```

#### 响应参数

同 [MemoryPost](#memorypost) 数据模型。

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "postId": "330e8400-e29b-41d4-a716-446655440003",
        "pointId": "110e8400-e29b-41d4-a716-446655440001",
        "authorId": "550e8400-e29b-41d4-a716-446655440000",
        "authorNickname": "小明",
        "authorAvatar": "https://cdn.rememo.com/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
        "title": "难忘的国庆节",
        "content": "这一天人山人海，红旗飘扬，我的心情无比激动...",
        "images": [...],
        "memoryYear": 2019,
        "memorySeason": "秋",
        "likeCount": 0,
        "commentCount": 0,
        "collectCount": 0,
        "createdTime": 1745316967890
    }
}
```

#### 错误响应

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

**内容为空**

```json
{
    "code": 400,
    "message": "记忆内容不能为空",
    "data": null
}
```

---

### 3. 获取记忆列表（按记忆点）

获取某记忆点的所有记忆，按记忆年份倒序（由近到远）。一次返回所有数据，前端按年份分组展示。

#### 请求信息

```
GET /api/memory-points/{pointId}/posts
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pointId | String | 是 | 记忆点ID |

#### 响应参数

返回 [MemoryPostListItem](#memorypostlistitem) 数组，列表中每条数据为摘要格式，**不含完整正文和所有图片**。

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "postId": "330e8400-e29b-41d4-a716-446655440003",
            "pointId": "110e8400-e29b-41d4-a716-446655440001",
            "authorId": "550e8400-e29b-41d4-a716-446655440000",
            "authorNickname": "小明",
            "authorAvatar": "https://cdn.rememo.com/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
            "title": "难忘的国庆节",
            "contentPreview": "这一天人山人海，红旗飘扬，我的心情无比激动...",
            "coverImage": "https://cdn.rememo.com/images/220e8400-e29b-41d4-a716-446655440002.jpg",
            "coverImageWidth": 1080,
            "coverImageHeight": 1920,
            "imageCount": 3,
            "memoryYear": 2019,
            "likeCount": 5,
            "commentCount": 2,
            "isLiked": true,
            "createdTime": 1745316967890
        }
    ]
}
```

**无记忆时**

```json
{
    "code": 200,
    "message": "success",
    "data": []
}
```

---

### 4. 获取记忆列表（按年份）

获取某年份的所有记忆。一次返回该年份所有数据。

#### 请求信息

```
GET /api/posts/by-year?year=2024
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| year | Integer | 是 | 年份 |

#### 成功响应

响应格式同 [获取记忆列表（按记忆点）](#4-获取记忆列表按记忆点)，返回 MemoryPostListItem 数组。

---

### 5. 获取记忆列表（按用户）

获取某用户的所有记忆。一次返回所有数据。

#### 请求信息

```
GET /api/users/{userId}/posts
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |

#### 成功响应

响应格式同 [获取记忆列表（按记忆点）](#4-获取记忆列表按记忆点)，返回 MemoryPostListItem 数组。

---

### 7. 获取随机记忆列表

发现页随机获取记忆列表，用于探索和发现功能。

#### 请求信息

```
GET /api/posts/random?limit=20
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 查询参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 获取数量，默认 20，最大 50 |

#### 成功响应

响应格式同 [获取记忆列表（按记忆点）](#4-获取记忆列表按记忆点)，返回 MemoryPostListItem 数组。

**示例**

```json
{
    "code": 200,
    "message": "success",
    "data": [
        {
            "postId": "330e8400-e29b-41d4-a716-446655440003",
            "pointId": "110e8400-e29b-41d4-a716-446655440001",
            "authorId": "550e8400-e29b-41d4-a716-446655440000",
            "authorNickname": "小明",
            "authorAvatar": "https://cdn.rememo.com/avatar/550e8400.jpg",
            "title": "难忘的国庆节",
            "contentPreview": "这一天人山人海，红旗飘扬，我的心情无比激动...",
            "coverImage": "https://cdn.rememo.com/images/220e8400-e29b-41d4-a716-446655440002.jpg",
            "coverImageWidth": 1080,
            "coverImageHeight": 1920,
            "imageCount": 3,
            "memoryYear": 2019,
            "likeCount": 5,
            "commentCount": 2,
            "isLiked": true,
            "createdTime": 1745316967890
        }
    ]
}
```

#### 错误响应

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

---

### 8. 获取记忆详情

获取单条记忆的完整内容，包含正文和所有图片。

#### 请求信息

```
GET /api/posts/{postId}
Authorization: Bearer {token}
```

#### 请求头

| Header | 必填 | 说明 |
|--------|------|------|
| Authorization | 是 | `Bearer {token}` |

#### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| postId | String | 是 | 记忆ID |

#### 响应参数

返回 [MemoryPostDetail](#memorypostdetail) 对象，包含完整正文和所有图片。

#### 成功响应

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "postId": "330e8400-e29b-41d4-a716-446655440003",
        "pointId": "110e8400-e29b-41d4-a716-446655440001",
        "authorId": "550e8400-e29b-41d4-a716-446655440000",
        "authorNickname": "小明",
        "authorAvatar": "https://cdn.rememo.com/avatar/550e8400-e29b-41d4-a716-446655440000.jpg",
        "title": "难忘的国庆节",
        "contentFull": "这一天人山人海，红旗飘扬，我的心情无比激动。清晨五点就起床了，天还没亮就出发。到达天安门广场时，已经是人山人海。...",
        "images": [
            {
                "photoId": "220e8400-e29b-41d4-a716-446655440002",
                "originalUrl": "https://cdn.rememo.com/images/220e8400-e29b-41d4-a716-446655440002.jpg",
                "restoredUrl": "",
                "displayState": "ORIGINAL"
            },
            {
                "photoId": "220e8400-e29b-41d4-a716-446655440003",
                "originalUrl": "https://cdn.rememo.com/images/220e8400-e29b-41d4-a716-446655440003.jpg",
                "restoredUrl": "https://cdn.rememo.com/images/220e8400-e29b-41d4-a716-446655440003_restored.jpg",
                "displayState": "ORIGINAL"
            }
        ],
        "memoryYear": 2019,
        "memorySeason": "秋",
        "likeCount": 5,
        "commentCount": 2,
        "collectCount": 3,
        "isLiked": true,
        "isCollected": false,
        "createdTime": 1745316967890
    }
}
```

#### 错误响应

**记忆不存在**

```json
{
    "code": 404,
    "message": "记忆不存在",
    "data": null
}
```

**Token 无效**

```json
{
    "code": 401,
    "message": "未授权，请重新登录",
    "data": null
}
```

---

## 数据模型

### User

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户ID，UUID，全局唯一 |
| phone | String | 手机号，11位，唯一 |
| nickname | String | 昵称，默认"新用户" |
| avatar | String | 头像 URL，默认空字符串 |
| gender | String | 性别：`male` / `female` / `secret`，默认 "secret" |
| bio | String | 个性签名，0-100 字符，默认空字符串 |
| createdAt | Long | 注册时间戳（毫秒） |

### UploadedImage

| 字段 | 类型 | 说明 |
|------|------|------|
| imageId | String | 图片ID，UUID |
| originalUrl | String | 原图公网 URL |
| fileSize | Long | 文件大小（字节） |
| width | Integer | 图片宽度（px） |
| height | Integer | 图片高度（px） |
| mimeType | String | MIME 类型，如 `image/jpeg` |

### Comment

| 字段 | 类型 | 说明 |
|------|------|------|
| commentId | String | 评论ID，UUID |
| postId | String | 关联的记忆帖子ID |
| authorId | String | 评论者用户ID |
| authorNickname | String | 评论者昵称（冗余存储） |
| authorAvatar | String | 评论者头像 URL（冗余存储） |
| content | String | 评论内容，1-500 字符 |
| createdTime | Long | 发布时间戳（毫秒） |

### MemoryPoint

| 字段 | 类型 | 说明 |
|------|------|------|
| pointId | String | 记忆点ID，UUID |
| latitude | Double | 纬度 |
| longitude | Double | 经度 |
| pointName | String | 记忆点名称 |
| locationAddress | String | 详细地址 |
| coverImageUrl | String | 封面图 URL（取第一张记忆图片） |
| memoryCount | Integer | 记忆数量 |
| summaryText | String | 文字摘要（取第一篇记忆的前100字） |
| minYear | Integer | 最早记忆年份 |
| maxYear | Integer | 最新记忆年份 |
| createdTime | Long | 创建时间戳（毫秒） |

### MemoryPost

> **说明**：[MemoryPost](#memorypost) 仅作为创建接口的请求体模型和内部参考，不用于接口响应。
> - **列表接口**响应请使用 [MemoryPostListItem](#memorypostlistitem)
> - **详情接口**响应请使用 [MemoryPostDetail](#memorypostdetail)

| 字段 | 类型 | 说明 |
|------|------|------|
| postId | String | 记忆ID，UUID |
| pointId | String | 关联的记忆点ID |
| authorId | String | 发布者用户ID |
| authorNickname | String | 发布者昵称（冗余存储） |
| authorAvatar | String | 发布者头像 URL（冗余存储） |
| title | String | 标题，1-50 字符 |
| content | String | 正文内容，1-2000 字符 |
| images | List | 图片列表，最多9张 |
| memoryYear | Integer | 记忆发生年份 |
| memorySeason | String | 记忆发生季节（春夏秋冬） |
| likeCount | Integer | 点赞数，默认0 |
| commentCount | Integer | 评论数，默认0 |
| collectCount | Integer | 收藏数，默认0 |
| createdTime | Long | 发布时间戳（毫秒） |

### MemoryPostListItem

> **用于**：记忆列表接口（按记忆点/按年份/按用户/随机）
> **特点**：轻量级摘要，不含完整正文和所有图片

| 字段 | 类型 | 说明 |
|------|------|------|
| postId | String | 记忆ID，UUID |
| pointId | String | 关联的记忆点ID |
| authorId | String | 发布者用户ID |
| authorNickname | String | 发布者昵称（冗余存储） |
| authorAvatar | String | 发布者头像 URL（冗余存储） |
| title | String | 标题，1-50 字符 |
| contentPreview | String | 正文前 100 字符摘要 |
| coverImage | String | 封面图 URL（第一张图片），无图时为空字符串 |
| coverImageWidth | Integer | 封面图宽度（px），无图时为 0 |
| coverImageHeight | Integer | 封面图高度（px），无图时为 0 |
| imageCount | Integer | 图片数量，无图时为 0 |
| memoryYear | Integer | 记忆发生年份 |
| memorySeason | String | 记忆发生季节（春夏秋冬） |
| likeCount | Integer | 点赞数，默认0 |
| commentCount | Integer | 评论数，默认0 |
| isLiked | Boolean | 当前用户是否已点赞 |
| createdTime | Long | 发布时间戳（毫秒） |

> **说明**：列表接口只返回点赞状态（isLiked），不返回收藏状态。

### MemoryPostDetail

> **用于**：记忆详情接口
> **特点**：完整内容，包含正文和所有图片

| 字段 | 类型 | 说明 |
|------|------|------|
| postId | String | 记忆ID，UUID |
| pointId | String | 关联的记忆点ID |
| authorId | String | 发布者用户ID |
| authorNickname | String | 发布者昵称（冗余存储） |
| authorAvatar | String | 发布者头像 URL（冗余存储） |
| title | String | 标题，1-50 字符 |
| contentFull | String | 完整正文内容，1-2000 字符 |
| images | List | 完整图片列表，最多9张（见 [MemoryPhoto](#memoryphoto)） |
| memoryYear | Integer | 记忆发生年份 |
| memorySeason | String | 记忆发生季节（春夏秋冬） |
| likeCount | Integer | 点赞数，默认0 |
| commentCount | Integer | 评论数，默认0 |
| collectCount | Integer | 收藏数，通过实时查询 user_collect 表获取 |
| isLiked | Boolean | 当前用户是否已点赞 |
| isCollected | Boolean | 当前用户是否已收藏 |
| createdTime | Long | 发布时间戳（毫秒） |

### MemoryPhoto

| 字段 | 类型 | 说明 |
|------|------|------|
| photoId | String | 图片ID，UUID |
| originalUrl | String | 原图公网 URL |
| restoredUrl | String | AI 修复图公网 URL（可空） |
| displayState | String | 当前显示状态：`ORIGINAL` / `RESTORED` |

### LikeResponse

> **用于**：点赞操作和状态查询接口

| 字段 | 类型 | 说明 |
|------|------|------|
| postId | String | 记忆帖子ID |
| liked | Boolean | 当前用户是否已点赞 |
| likeCount | Integer | 当前点赞数 |

### CollectResponse

> **用于**：收藏操作和状态查询接口

| 字段 | 类型 | 说明 |
|------|------|------|
| postId | String | 记忆帖子ID |
| collected | Boolean | 当前用户是否已收藏 |
| collectCount | Integer | 当前收藏数 |

---

## 后续模块（待补充）

- [ ] ~~地图模块~~ - 地点标记、附近记忆
- [ ] ~~时间线模块~~ - 记忆流、时间轴
- [ ] ~~发布模块~~ - 发布、分享记忆
- [ ] ~~文件模块~~ - 图片上传（已完成）
- [ ] ~~评论模块~~ - 评论发布与列表（已完成）
- [x] ~~记忆模块~~ - 记忆点创建、记忆发布与列表（已完成）
- [x] ~~互动模块~~ - 点赞、收藏（已完成）

---

## 更新日志

| 版本 | 日期 | 说明 |
|------|------|------|
| v2.3 | 2026-04-23 | MemoryPostListItem 新增 coverImageWidth、coverImageHeight 字段，用于 Android 端计算封面图宽高比，解决瀑布流图片加载跳动问题 |
| v2.1 | 2026-04-07 | MemoryPostListItem / MemoryPostDetail 新增 isLiked、isCollected 字段（后因后端收藏数存储限制，回退为 v2.2 方案） |
| v2.0 | 2026-04-01 | 移除"创建记忆点"独立接口，合并到发布记忆接口中；发布记忆接口新增 pointName 参数；MemoryPoint 移除 authorId 字段；MemoryPost / 发布记忆响应新增 collectCount 字段 |
| v1.9 | 2026-03-29 | 新增互动模块：点赞/取消点赞、收藏/取消收藏、获取用户点赞/收藏列表；新增 LikeResponse、CollectResponse 数据模型 |
| v1.8 | 2026-03-29 | 接口分页策略调整：Timeline/Profile/Year 列表接口移除分页参数，一次返回所有数据；保留 Discover（limit）和 Comment（page/size）分页 |
| v1.7 | 2026-03-29 | 记忆模块拆分：列表接口返回 MemoryPostListItem（不含完整正文和所有图片），新增记忆详情接口返回 MemoryPostDetail（含完整内容） |
| v1.6 | 2026-03-29 | 新增获取所有记忆点接口、获取随机记忆列表接口；移除 MemoryPoint.updatedTime 字段 |
| v1.5 | 2026-03-28 | 修正发布记忆接口响应示例：首次创建记忆点时 minYear/maxYear 应等于第一条记忆的 memoryYear，不再为 null |
| v1.3 | 2026-03-26 | 新增评论模块（发布评论、获取评论列表）；Comment 模型增加 authorNickname、authorAvatar 字段 |
| v1.2 | 2026-03-26 | 新增文件模块（图片上传接口）、修改密码、注销账号接口；新增 `createdAt` 字段；`avatar` 字段改为仅接收 URL |
| v1.1 | 2026-03-23 | 新增性别(gender)和个性签名(bio)字段，移除时间戳字段 |
| v1.0 | 2026-03-23 | 初始版本，包含用户模块接口 |
