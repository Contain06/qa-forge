# QAForge V1 接口文档

## 1. 文档说明

本文档用于指导 QAForge V1 后端接口开发。

QAForge V1 的核心目标是完成接口自动化测试闭环：

```text
项目 -> 环境 -> 接口模板 -> 测试用例 -> 断言规则 -> 测试计划 -> 执行 -> 执行记录/报告
```

接口路径采用简单的“模块/动作”风格，便于按模块逐步开发，例如：

```text
GET  /project/list
POST /project/add
PUT  /project/update
DELETE /project/delete/{id}
```

前后端传参统一使用 camelCase，数据库字段使用 snake_case。

## 2. 通用约定

### 2.1 基础地址

本地开发环境：

```text
http://localhost:8080
```

Knife4j 接口文档地址：

```text
http://localhost:8080/doc.html
```

### 2.2 统一返回结构

建议所有接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

常用状态码：

| code | 含义 |
| --- | --- |
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或登录失效 |
| 403 | 无权限 |
| 404 | 数据不存在 |
| 500 | 服务器内部错误 |

### 2.3 分页返回结构

列表接口建议返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 0,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 2.4 通用字段

多数业务表都包含以下字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Integer | 主键 ID |
| createBy | Integer | 创建人 ID |
| updateBy | Integer | 更新人 ID |
| createTime | String | 创建时间 |
| updateTime | String | 更新时间 |
| deleted | Integer | 逻辑删除标记，0 未删除，1 已删除 |

新增和修改接口通常不需要前端传 `createTime`、`updateTime`、`deleted`。

### 2.5 普通业务状态

适用于 `sys_user`、`project`、`environment`、`api_info`、`api_case`、`api_assertion`、`test_plan`。

| 值 | 含义 |
| --- | --- |
| 0 | 禁用 |
| 1 | 启用 |

新增业务数据时，前端不用传 `status`。后端默认设置为 `1`，表示启用。

### 2.6 执行状态

`run_record.status`：

| 值 | 含义 |
| --- | --- |
| PENDING | 等待执行 |
| RUNNING | 执行中 |
| SUCCESS | 全部成功 |
| FAIL | 全部失败 |
| PARTIAL | 部分成功，部分失败 |
| TIMEOUT | 执行超时 |
| ERROR | 执行异常 |

`run_detail.status`：

| 值 | 含义 |
| --- | --- |
| PASS | 用例通过 |
| FAIL | 用例失败 |
| TIMEOUT | 请求超时 |
| ERROR | 请求异常 |

## 3. 用户与登录接口

### 3.1 用户登录

```text
POST /user/login
```

请求参数：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应数据：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "token": "mock-token"
  }
}
```

V1 可以先返回一个简单 token，后续再接入 JWT 或 Spring Security。

### 3.2 获取当前登录用户

```text
GET /user/current
```

响应数据：

```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员",
  "email": "admin@example.com",
  "phone": "13800000000",
  "status": 1,
  "lastLoginTime": "2026-06-07 14:30:00"
}
```

## 4. 项目管理接口

项目是 QAForge 的顶层业务对象，环境、接口、用例、计划、执行记录都归属于某个项目。

### 4.1 项目分页列表

第一条建议开发的后端接口：

```text
GET /project/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| pageNum | 否 | Integer | 页码，默认 1 |
| pageSize | 否 | Integer | 每页数量，默认 10 |
| projectName | 否 | String | 项目名称，支持模糊查询 |
| projectCode | 否 | String | 项目标识，支持模糊查询 |
| status | 否 | Integer | 状态，0 禁用，1 启用 |

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectName": "QAForge",
      "projectCode": "qa-forge",
      "description": "接口自动化测试平台",
      "ownerId": 1,
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 4.2 项目详情

```text
GET /project/detail/{id}
```

路径参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| id | 是 | Integer | 项目 ID |

响应数据：

```json
{
  "id": 1,
  "projectName": "QAForge",
  "projectCode": "qa-forge",
  "description": "接口自动化测试平台",
  "ownerId": 1,
  "ownerName": "管理员",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

### 4.3 新增项目

```text
POST /project/add
```

请求参数：

```json
{
  "projectName": "QAForge",
  "projectCode": "qa-forge",
  "description": "接口自动化测试平台"
}
```

响应数据：

```json
{
  "id": 1
}
```

### 4.4 修改项目

```text
PUT /project/update
```

请求参数：

```json
{
  "id": 1,
  "projectName": "QAForge",
  "projectCode": "qa-forge",
  "description": "接口自动化测试平台 V1",
  "ownerId": 1,
  "status": 1
}
```

响应数据：

```json
null
```

### 4.5 删除项目

```text
DELETE /project/delete/{id}
```

说明：使用 MyBatis-Plus 逻辑删除，不物理删除数据库记录。

响应数据：

```json
null
```

### 4.6 修改项目状态

```text
PUT /project/status
```

请求参数：

```json
{
  "id": 1,
  "status": 0
}
```

响应数据：

```json
null
```

## 5. 环境管理接口

环境用于保存不同测试环境的基础地址，例如开发环境、测试环境、预发环境。

### 5.1 环境分页列表

```text
GET /environment/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| pageNum | 否 | Integer | 页码 |
| pageSize | 否 | Integer | 每页数量 |
| projectId | 是 | Integer | 项目 ID |
| envName | 否 | String | 环境名称 |
| status | 否 | Integer | 状态 |

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "envName": "测试环境",
      "baseUrl": "https://test.example.com",
      "description": "日常接口测试环境",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 5.2 环境详情

```text
GET /environment/detail/{id}
```

响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "envName": "测试环境",
  "baseUrl": "https://test.example.com",
  "description": "日常接口测试环境",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

### 5.3 新增环境

```text
POST /environment/add
```

请求参数：

```json
{
  "projectId": 1,
  "envName": "测试环境",
  "baseUrl": "https://test.example.com",
  "description": "日常接口测试环境"
}
```

响应数据：

```json
{
  "id": 1
}
```

### 5.4 修改环境

```text
PUT /environment/update
```

请求参数：

```json
{
  "id": 1,
  "projectId": 1,
  "envName": "测试环境",
  "baseUrl": "https://test.example.com",
  "description": "日常接口测试环境",
  "status": 1
}
```

响应数据：

```json
null
```

### 5.5 删除环境

```text
DELETE /environment/delete/{id}
```

响应数据：

```json
null
```

### 5.6 修改环境状态

```text
PUT /environment/status
```

请求参数：

```json
{
  "id": 1,
  "status": 0
}
```

响应数据：

```json
null
```

## 6. 接口模板管理接口

接口模板保存接口的公共请求信息。测试用例可以在模板基础上覆盖请求头、参数和请求体。

### 6.1 接口分页列表

```text
GET /api-info/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| pageNum | 否 | Integer | 页码 |
| pageSize | 否 | Integer | 每页数量 |
| projectId | 是 | Integer | 项目 ID |
| apiName | 否 | String | 接口名称 |
| requestMethod | 否 | String | 请求方法 |
| status | 否 | Integer | 状态 |

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "apiName": "用户登录",
      "requestMethod": "POST",
      "apiPath": "/api/login",
      "requestHeaders": "{\"Content-Type\":\"application/json\"}",
      "requestParams": "{}",
      "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
      "description": "登录接口模板",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 6.2 接口详情

```text
GET /api-info/detail/{id}
```

响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "apiName": "用户登录",
  "requestMethod": "POST",
  "apiPath": "/api/login",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "description": "登录接口模板",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

### 6.3 新增接口

```text
POST /api-info/add
```

请求参数：

```json
{
  "projectId": 1,
  "apiName": "用户登录",
  "requestMethod": "POST",
  "apiPath": "/api/login",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "description": "登录接口模板"
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| requestMethod | String | GET、POST、PUT、DELETE 等 |
| apiPath | String | 接口路径，不包含环境 baseUrl |
| requestHeaders | String | JSON 字符串 |
| requestParams | String | JSON 字符串，保存 query 参数 |
| requestBody | String | JSON 字符串，保存请求体 |

响应数据：

```json
{
  "id": 1
}
```

### 6.4 修改接口

```text
PUT /api-info/update
```

请求参数：

```json
{
  "id": 1,
  "apiName": "用户登录",
  "requestMethod": "POST",
  "apiPath": "/api/login",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "description": "登录接口模板"
}
```

说明：

```text
修改接口时必须传 id。
不建议前端传 projectId，接口所属项目不应该通过普通修改接口变更。
不建议前端传 status，状态变更统一走 PUT /api-info/status。
```

响应数据：

```json
null
```

### 6.5 删除接口

```text
DELETE /api-info/delete/{id}
```

响应数据：

```json
null
```

### 6.6 修改接口状态

```text
PUT /api-info/status
```

响应数据：

```json
null
```

## 7. 测试用例管理接口

测试用例保存具体测试数据和预期结果，归属于某一个接口模板。

### 7.1 用例分页列表

```text
GET /api-case/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| pageNum | 否 | Integer | 页码 |
| pageSize | 否 | Integer | 每页数量 |
| projectId | 是 | Integer | 项目 ID |
| apiId | 否 | Integer | 接口模板 ID |
| caseName | 否 | String | 用例名称 |
| caseLevel | 否 | String | 用例等级 |
| status | 否 | Integer | 状态 |

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "apiId": 1,
      "apiName": "用户登录",
      "caseName": "登录成功",
      "caseLevel": "P1",
      "requestHeaders": "{\"Content-Type\":\"application/json\"}",
      "requestParams": "{}",
      "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
      "expectedResult": "登录成功",
      "description": "正确账号密码登录",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 7.2 用例详情

```text
GET /api-case/detail/{id}
```

详情接口建议同时返回断言列表，方便前端编辑用例。

响应数据示例：

```json
{
  "id": 1,
  "projectId": 1,
  "apiId": 1,
  "apiName": "用户登录",
  "caseName": "登录成功",
  "caseLevel": "P1",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "expectedResult": "登录成功",
  "description": "正确账号密码登录",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00",
  "assertions": [
    {
      "id": 1,
      "caseId": 1,
      "assertType": "STATUS_CODE",
      "expression": "",
      "assertOperator": "=",
      "expectedValue": "200",
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

### 7.3 新增用例

```text
POST /api-case/add
```

请求参数：

```json
{
  "projectId": 1,
  "apiId": 1,
  "caseName": "登录成功",
  "caseLevel": "P1",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "expectedResult": "登录成功",
  "description": "正确账号密码登录"
}
```

响应数据：

```json
{
  "id": 1
}
```

### 7.4 批量新增用例

```text
POST /api-case/batch-add
```

说明：用于在同一个接口模板下批量新增测试用例。

请求参数：

```json
{
  "projectId": 1,
  "apiId": 1,
  "cases": [
    {
      "caseName": "登录成功",
      "caseLevel": "P1",
      "requestHeaders": "{\"Content-Type\":\"application/json\"}",
      "requestParams": "{}",
      "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
      "expectedResult": "登录成功",
      "description": "正确账号密码登录"
    },
    {
      "caseName": "密码错误",
      "caseLevel": "P1",
      "requestHeaders": "{\"Content-Type\":\"application/json\"}",
      "requestParams": "{}",
      "requestBody": "{\"username\":\"admin\",\"password\":\"wrong\"}",
      "expectedResult": "登录失败",
      "description": "错误密码登录"
    }
  ]
}
```

响应数据：

```json
{
  "ids": [1, 2]
}
```

### 7.5 修改用例

```text
PUT /api-case/update
```

响应数据：

```json
null
```

### 7.6 删除用例

```text
DELETE /api-case/delete/{id}
```

响应数据：

```json
null
```

### 7.7 批量删除用例

```text
POST /api-case/batch-delete
```

说明：用于一次删除多个测试用例。批量删除建议使用 `POST`，避免部分客户端、网关或代理对 `DELETE` 请求体支持不一致。

请求参数：

```json
{
  "ids": [1, 2, 3]
}
```

响应数据：

```json
null
```

### 7.8 修改用例状态

```text
PUT /api-case/status
```

响应数据：

```json
null
```

## 8. 断言规则管理接口

断言规则归属于测试用例，用于判断接口响应是否符合预期。

### 8.1 断言列表

```text
GET /api-assertion/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| caseId | 是 | Integer | 用例 ID |

响应数据：

```json
[
  {
    "id": 1,
    "caseId": 1,
    "assertType": "STATUS_CODE",
    "expression": "",
    "assertOperator": "=",
    "expectedValue": "200",
    "sortOrder": 1,
    "status": 1,
    "createBy": 1,
    "updateBy": 1,
    "createTime": "2026-06-07 14:30:00",
    "updateTime": "2026-06-07 14:30:00"
  }
]
```

### 8.2 断言详情

```text
GET /api-assertion/detail/{id}
```

响应数据：

```json
{
  "id": 1,
  "caseId": 1,
  "assertType": "JSON_PATH",
  "expression": "$.code",
  "assertOperator": "=",
  "expectedValue": "200",
  "sortOrder": 1,
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

### 8.3 新增断言

```text
POST /api-assertion/add
```

请求参数：

```json
{
  "caseId": 1,
  "assertType": "JSON_PATH",
  "expression": "$.code",
  "assertOperator": "=",
  "expectedValue": "200",
  "sortOrder": 1
}
```

断言类型：

| assertType | 含义 | expression 示例 | expectedValue 示例 |
| --- | --- | --- | --- |
| STATUS_CODE | 状态码断言 | 空字符串 | 200 |
| JSON_PATH | JSON 字段断言 | $.code | 200 |
| CONTAINS | 响应文本包含断言 | 空字符串 | success |
| RESPONSE_TIME | 响应时间断言 | 空字符串 | 1000 |

断言操作符：

| assertOperator | 含义 |
| --- | --- |
| = | 等于 |
| != | 不等于 |
| contains | 包含 |
| not_null | 非空 |
| < | 小于 |
| > | 大于 |
| <= | 小于等于 |
| >= | 大于等于 |

响应数据：

```json
{
  "id": 1
}
```

### 8.4 修改断言

```text
PUT /api-assertion/update
```

响应数据：

```json
null
```

### 8.5 删除断言

```text
DELETE /api-assertion/delete/{id}
```

响应数据：

```json
null
```

### 8.6 批量保存断言

```text
POST /api-assertion/batch-save
```

说明：用于用例编辑页一次性保存断言列表。后端可以先删除当前 `caseId` 下旧断言，再插入新断言。

请求参数：

```json
{
  "caseId": 1,
  "assertions": [
    {
      "assertType": "STATUS_CODE",
      "expression": "",
      "assertOperator": "=",
      "expectedValue": "200",
      "sortOrder": 1,
      "status": 1
    },
    {
      "assertType": "JSON_PATH",
      "expression": "$.code",
      "assertOperator": "=",
      "expectedValue": "200",
      "sortOrder": 2,
      "status": 1
    }
  ]
}
```

响应数据：

```json
null
```

## 9. 测试计划管理接口

测试计划用于批量组织测试用例，并在指定环境中执行。

### 9.1 测试计划分页列表

```text
GET /test-plan/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| pageNum | 否 | Integer | 页码 |
| pageSize | 否 | Integer | 每页数量 |
| projectId | 是 | Integer | 项目 ID |
| planName | 否 | String | 计划名称 |
| environmentId | 否 | Integer | 环境 ID |
| status | 否 | Integer | 状态 |

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "environmentId": 1,
      "environmentName": "测试环境",
      "planName": "登录模块回归测试",
      "description": "登录相关接口回归",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 9.2 测试计划详情

```text
GET /test-plan/detail/{id}
```

详情接口建议返回计划基本信息和已关联用例列表。

响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "environmentId": 1,
  "environmentName": "测试环境",
  "planName": "登录模块回归测试",
  "description": "登录相关接口回归",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00",
  "cases": [
    {
      "caseId": 1,
      "caseName": "登录成功",
      "apiId": 1,
      "apiName": "用户登录",
      "caseLevel": "P1",
      "sortOrder": 1
    }
  ]
}
```

### 9.3 新增测试计划

```text
POST /test-plan/add
```

请求参数：

```json
{
  "projectId": 1,
  "environmentId": 1,
  "planName": "登录模块回归测试",
  "description": "登录相关接口回归"
}
```

响应数据：

```json
{
  "id": 1
}
```

### 9.4 修改测试计划

```text
PUT /test-plan/update
```

响应数据：

```json
null
```

### 9.5 删除测试计划

```text
DELETE /test-plan/delete/{id}
```

响应数据：

```json
null
```

### 9.6 修改测试计划状态

```text
PUT /test-plan/status
```

响应数据：

```json
null
```

### 9.7 查询计划关联用例

```text
GET /test-plan/case-list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| planId | 是 | Integer | 测试计划 ID |

响应数据：

```json
[
  {
    "caseId": 1,
    "caseName": "登录成功",
    "apiId": 1,
    "apiName": "用户登录",
    "caseLevel": "P1",
    "sortOrder": 1,
    "status": 1
  }
]
```

### 9.8 保存计划关联用例

```text
POST /test-plan/save-cases
```

请求参数：

```json
{
  "planId": 1,
  "caseIds": [1, 2, 3]
}
```

说明：用于维护 `test_plan_case` 关联关系。建议保存时按 `caseIds` 数组顺序生成 `sortOrder`。

响应数据：

```json
null
```

### 9.9 移除计划中的某个用例

```text
DELETE /test-plan/delete-case
```

请求参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| planId | 是 | Integer | 测试计划 ID |
| caseId | 是 | Integer | 用例 ID |

响应数据：

```json
null
```

## 10. 执行接口

执行接口负责真正发送 HTTP 请求、合并请求头和参数、执行断言、保存运行记录。

### 10.1 执行单个用例

```text
POST /execute/case
```

请求参数：

```json
{
  "caseId": 1,
  "environmentId": 1
}
```

处理流程：

```text
1. 查询 api_case
2. 查询 api_info
3. 查询 environment
4. 查询 api_assertion
5. 拼接请求地址：environment.baseUrl + api_info.apiPath
6. 合并请求头：api_info.requestHeaders + api_case.requestHeaders
7. 合并请求参数和请求体
8. 使用 OkHttp 发送请求
9. 根据断言规则校验响应
10. 写入 run_record 和 run_detail
11. 返回 run_recordId 和执行结果
```

响应数据：

```json
{
  "runRecordId": 1,
  "runDetailId": 1,
  "caseId": 1,
  "status": "PASS",
  "responseStatus": 200,
  "responseTime": 120,
  "assertResult": [
    {
      "assertType": "STATUS_CODE",
      "expression": "",
      "operator": "=",
      "expectedValue": "200",
      "actualValue": "200",
      "passed": true,
      "failReason": ""
    }
  ]
}
```

### 10.2 执行测试计划

```text
POST /execute/plan
```

请求参数：

```json
{
  "planId": 1
}
```

说明：

```text
测试计划本身已经绑定 environmentId，因此执行计划时通常只需要传 planId。
如果后续支持临时切换环境，可以增加 environmentId 参数覆盖计划默认环境。
```

响应数据：

```json
{
  "runRecordId": 1,
  "planId": 1,
  "status": "SUCCESS",
  "totalCount": 3,
  "successCount": 3,
  "failCount": 0,
  "timeoutCount": 0,
  "errorCount": 0,
  "passRate": 100.0,
  "totalTime": 560
}
```

### 10.3 调试执行接口模板

```text
POST /execute/debug
```

说明：该接口可选，用于前端在保存用例前临时调试请求，不一定写入正式执行记录。

请求参数：

```json
{
  "environmentId": 1,
  "requestMethod": "POST",
  "apiPath": "/api/login",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}"
}
```

响应数据：

```json
{
  "requestUrl": "https://test.example.com/api/login",
  "requestMethod": "POST",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "responseStatus": 200,
  "responseHeaders": "{\"Content-Type\":\"application/json\"}",
  "responseBody": "{\"code\":200,\"message\":\"success\"}",
  "responseTime": 120,
  "errorMessage": ""
}
```

## 11. 执行记录与测试报告接口

`run_record` 既是执行记录主表，也是测试报告汇总表。

`run_detail` 既是执行详情表，也是测试报告详情表。

### 11.1 执行记录分页列表

```text
GET /run-record/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| pageNum | 否 | Integer | 页码 |
| pageSize | 否 | Integer | 每页数量 |
| projectId | 否 | Integer | 项目 ID |
| environmentId | 否 | Integer | 环境 ID |
| planId | 否 | Integer | 测试计划 ID |
| caseId | 否 | Integer | 测试用例 ID |
| runType | 否 | String | CASE 或 PLAN |
| status | 否 | String | 执行状态 |
| startTime | 否 | String | 开始时间 |
| endTime | 否 | String | 结束时间 |

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "environmentId": 1,
      "environmentName": "测试环境",
      "planId": 1,
      "planName": "登录模块回归测试",
      "caseId": null,
      "caseName": null,
      "runType": "PLAN",
      "totalCount": 3,
      "successCount": 3,
      "failCount": 0,
      "timeoutCount": 0,
      "errorCount": 0,
      "passRate": 100.0,
      "totalTime": 560,
      "status": "SUCCESS",
      "startTime": "2026-06-07 14:30:00",
      "endTime": "2026-06-07 14:30:01",
      "createBy": 1,
      "createTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

响应数据字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Integer | 执行记录 ID |
| projectId | Integer | 项目 ID |
| environmentId | Integer | 环境 ID |
| planId | Integer | 计划 ID，单用例执行可为空 |
| caseId | Integer | 用例 ID，计划执行可为空 |
| runType | String | CASE 或 PLAN |
| totalCount | Integer | 总用例数 |
| successCount | Integer | 成功数 |
| failCount | Integer | 失败数 |
| timeoutCount | Integer | 超时数 |
| errorCount | Integer | 异常数 |
| passRate | Double | 通过率 |
| totalTime | Integer | 总耗时，单位毫秒 |
| status | String | 执行状态 |
| startTime | String | 开始时间 |
| endTime | String | 结束时间 |

### 11.2 执行记录详情

```text
GET /run-record/detail/{id}
```

详情接口建议返回 `run_record` 汇总信息和 `run_detail` 明细列表。

响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "environmentId": 1,
  "environmentName": "测试环境",
  "planId": 1,
  "planName": "登录模块回归测试",
  "caseId": null,
  "caseName": null,
  "runType": "PLAN",
  "totalCount": 3,
  "successCount": 3,
  "failCount": 0,
  "timeoutCount": 0,
  "errorCount": 0,
  "passRate": 100.0,
  "totalTime": 560,
  "status": "SUCCESS",
  "startTime": "2026-06-07 14:30:00",
  "endTime": "2026-06-07 14:30:01",
  "details": [
    {
      "id": 1,
      "caseId": 1,
      "caseName": "登录成功",
      "apiId": 1,
      "apiName": "用户登录",
      "requestUrl": "https://test.example.com/api/login",
      "requestMethod": "POST",
      "responseStatus": 200,
      "responseTime": 120,
      "status": "PASS"
    }
  ]
}
```

### 11.3 执行详情列表

```text
GET /run-detail/list
```

查询参数：

| 参数 | 必填 | 类型 | 说明 |
| --- | --- | --- | --- |
| runRecordId | 是 | Integer | 执行记录 ID |
| status | 否 | String | PASS、FAIL、TIMEOUT、ERROR |

响应数据：

```json
[
  {
    "id": 1,
    "runRecordId": 1,
    "projectId": 1,
    "environmentId": 1,
    "planId": 1,
    "caseId": 1,
    "caseName": "登录成功",
    "apiId": 1,
    "apiName": "用户登录",
    "requestUrl": "https://test.example.com/api/login",
    "requestMethod": "POST",
    "responseStatus": 200,
    "responseTime": 120,
    "status": "PASS"
  }
]
```

### 11.4 执行详情

```text
GET /run-detail/detail/{id}
```

响应数据：

```json
{
  "id": 1,
  "runRecordId": 1,
  "projectId": 1,
  "environmentId": 1,
  "planId": 1,
  "caseId": 1,
  "caseName": "登录成功",
  "apiId": 1,
  "apiName": "用户登录",
  "requestUrl": "https://test.example.com/api/login",
  "requestMethod": "POST",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "responseStatus": 200,
  "responseHeaders": "{\"Content-Type\":\"application/json\"}",
  "responseBody": "{\"code\":200,\"message\":\"success\"}",
  "responseTime": 120,
  "assertResult": "[{\"assertType\":\"STATUS_CODE\",\"expression\":\"\",\"operator\":\"=\",\"expectedValue\":\"200\",\"actualValue\":\"200\",\"passed\":true,\"failReason\":\"\"}]",
  "errorMessage": "",
  "status": "PASS",
  "createBy": 1,
  "createTime": "2026-06-07 14:30:00"
}
```

响应数据字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | Integer | 执行详情 ID |
| runRecordId | Integer | 执行记录 ID |
| projectId | Integer | 项目 ID |
| environmentId | Integer | 环境 ID |
| planId | Integer | 测试计划 ID |
| caseId | Integer | 测试用例 ID |
| apiId | Integer | 接口模板 ID |
| requestUrl | String | 最终请求地址 |
| requestMethod | String | 请求方法 |
| requestHeaders | String | 最终请求头 JSON |
| requestParams | String | 最终请求参数 JSON |
| requestBody | String | 最终请求体 |
| responseStatus | Integer | HTTP 响应状态码 |
| responseHeaders | String | 响应头 JSON |
| responseBody | String | 响应体 |
| responseTime | Integer | 响应耗时，单位毫秒 |
| assertResult | String | 断言结果 JSON 数组 |
| errorMessage | String | 异常信息 |
| status | String | PASS、FAIL、TIMEOUT、ERROR |

## 12. 推荐开发顺序

建议按下面顺序写接口：

```text
1. Result 统一返回类
2. ProjectMapper / ProjectService / ProjectController
3. GET /project/list
4. POST /project/add
5. PUT /project/update
6. DELETE /project/delete/{id}
7. Environment 模块 CRUD
8. ApiInfo 模块 CRUD
9. ApiCase 模块 CRUD
10. ApiAssertion 模块 CRUD
11. TestPlan + TestPlanCase 关联接口
12. 单用例执行接口
13. 测试计划执行接口
14. RunRecord / RunDetail 查询接口
```

## 13. V1 接口总览

| 模块 | 方法 | 路径 | 说明 |
| --- | --- | --- | --- |
| 用户 | POST | /user/login | 用户登录 |
| 用户 | GET | /user/current | 当前登录用户 |
| 项目 | GET | /project/list | 项目分页列表 |
| 项目 | GET | /project/detail/{id} | 项目详情 |
| 项目 | POST | /project/add | 新增项目 |
| 项目 | PUT | /project/update | 修改项目 |
| 项目 | DELETE | /project/delete/{id} | 删除项目 |
| 项目 | PUT | /project/status | 修改项目状态 |
| 环境 | GET | /environment/list | 环境分页列表 |
| 环境 | GET | /environment/detail/{id} | 环境详情 |
| 环境 | POST | /environment/add | 新增环境 |
| 环境 | PUT | /environment/update | 修改环境 |
| 环境 | DELETE | /environment/delete/{id} | 删除环境 |
| 环境 | PUT | /environment/status | 修改环境状态 |
| 接口模板 | GET | /api-info/list | 接口分页列表 |
| 接口模板 | GET | /api-info/detail/{id} | 接口详情 |
| 接口模板 | POST | /api-info/add | 新增接口 |
| 接口模板 | PUT | /api-info/update | 修改接口 |
| 接口模板 | DELETE | /api-info/delete/{id} | 删除接口 |
| 接口模板 | PUT | /api-info/status | 修改接口状态 |
| 用例 | GET | /api-case/list | 用例分页列表 |
| 用例 | GET | /api-case/detail/{id} | 用例详情 |
| 用例 | POST | /api-case/add | 新增用例 |
| 用例 | POST | /api-case/batch-add | 批量新增用例 |
| 用例 | PUT | /api-case/update | 修改用例 |
| 用例 | DELETE | /api-case/delete/{id} | 删除用例 |
| 用例 | POST | /api-case/batch-delete | 批量删除用例 |
| 用例 | PUT | /api-case/status | 修改用例状态 |
| 断言 | GET | /api-assertion/list | 断言列表 |
| 断言 | GET | /api-assertion/detail/{id} | 断言详情 |
| 断言 | POST | /api-assertion/add | 新增断言 |
| 断言 | PUT | /api-assertion/update | 修改断言 |
| 断言 | DELETE | /api-assertion/delete/{id} | 删除断言 |
| 断言 | POST | /api-assertion/batch-save | 批量保存断言 |
| 测试计划 | GET | /test-plan/list | 测试计划分页列表 |
| 测试计划 | GET | /test-plan/detail/{id} | 测试计划详情 |
| 测试计划 | POST | /test-plan/add | 新增测试计划 |
| 测试计划 | PUT | /test-plan/update | 修改测试计划 |
| 测试计划 | DELETE | /test-plan/delete/{id} | 删除测试计划 |
| 测试计划 | PUT | /test-plan/status | 修改测试计划状态 |
| 测试计划 | GET | /test-plan/case-list | 查询计划关联用例 |
| 测试计划 | POST | /test-plan/save-cases | 保存计划关联用例 |
| 测试计划 | DELETE | /test-plan/delete-case | 移除计划中的某个用例 |
| 执行 | POST | /execute/case | 执行单个用例 |
| 执行 | POST | /execute/plan | 执行测试计划 |
| 执行 | POST | /execute/debug | 调试执行接口模板 |
| 执行记录 | GET | /run-record/list | 执行记录分页列表 |
| 执行记录 | GET | /run-record/detail/{id} | 执行记录详情 |
| 执行详情 | GET | /run-detail/list | 执行详情列表 |
| 执行详情 | GET | /run-detail/detail/{id} | 执行详情 |

## 14. 响应数据补充

说明：本节补充各接口的 `data` 数据结构。实际接口外层仍然统一包一层 `Result`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

对于新增、修改、删除、修改状态这类操作接口，V1 可以先统一返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

如果你希望新增接口返回新建数据 ID，也可以返回：

```json
{
  "id": 1
}
```

### 14.1 项目接口响应

`GET /project/list` 响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectName": "QAForge",
      "projectCode": "qa-forge",
      "description": "接口自动化测试平台",
      "ownerId": 1,
      "ownerName": "管理员",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

`GET /project/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "projectName": "QAForge",
  "projectCode": "qa-forge",
  "description": "接口自动化测试平台",
  "ownerId": 1,
  "ownerName": "管理员",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

`POST /project/add` 响应数据：

```json
{
  "id": 1
}
```

`PUT /project/update`、`DELETE /project/delete/{id}`、`PUT /project/status` 响应数据：

```json
null
```

### 14.2 环境接口响应

`GET /environment/list` 响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "envName": "测试环境",
      "baseUrl": "https://test.example.com",
      "description": "日常接口测试环境",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

`GET /environment/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "envName": "测试环境",
  "baseUrl": "https://test.example.com",
  "description": "日常接口测试环境",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

`POST /environment/add` 响应数据：

```json
{
  "id": 1
}
```

`PUT /environment/update`、`DELETE /environment/delete/{id}`、`PUT /environment/status` 响应数据：

```json
null
```

### 14.3 接口模板接口响应

`GET /api-info/list` 响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "apiName": "用户登录",
      "requestMethod": "POST",
      "apiPath": "/api/login",
      "requestHeaders": "{\"Content-Type\":\"application/json\"}",
      "requestParams": "{}",
      "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
      "description": "登录接口模板",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

`GET /api-info/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "apiName": "用户登录",
  "requestMethod": "POST",
  "apiPath": "/api/login",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "description": "登录接口模板",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

`POST /api-info/add` 响应数据：

```json
{
  "id": 1
}
```

`PUT /api-info/update`、`DELETE /api-info/delete/{id}`、`PUT /api-info/status` 响应数据：

```json
null
```

### 14.4 测试用例接口响应

`GET /api-case/list` 响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "apiId": 1,
      "apiName": "用户登录",
      "caseName": "登录成功",
      "caseLevel": "P1",
      "requestHeaders": "{\"Content-Type\":\"application/json\"}",
      "requestParams": "{}",
      "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
      "expectedResult": "登录成功",
      "description": "正确账号密码登录",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

`GET /api-case/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "apiId": 1,
  "apiName": "用户登录",
  "caseName": "登录成功",
  "caseLevel": "P1",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "expectedResult": "登录成功",
  "description": "正确账号密码登录",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00",
  "assertions": [
    {
      "id": 1,
      "caseId": 1,
      "assertType": "STATUS_CODE",
      "expression": "",
      "assertOperator": "=",
      "expectedValue": "200",
      "sortOrder": 1,
      "status": 1
    }
  ]
}
```

`POST /api-case/add` 响应数据：

```json
{
  "id": 1
}
```

`POST /api-case/batch-add` 响应数据：

```json
{
  "ids": [1, 2]
}
```

`PUT /api-case/update`、`DELETE /api-case/delete/{id}`、`POST /api-case/batch-delete`、`PUT /api-case/status` 响应数据：

```json
null
```

### 14.5 断言接口响应

`GET /api-assertion/list` 响应数据：

```json
[
  {
    "id": 1,
    "caseId": 1,
    "assertType": "STATUS_CODE",
    "expression": "",
    "assertOperator": "=",
    "expectedValue": "200",
    "sortOrder": 1,
    "status": 1,
    "createBy": 1,
    "updateBy": 1,
    "createTime": "2026-06-07 14:30:00",
    "updateTime": "2026-06-07 14:30:00"
  },
  {
    "id": 2,
    "caseId": 1,
    "assertType": "JSON_PATH",
    "expression": "$.code",
    "assertOperator": "=",
    "expectedValue": "200",
    "sortOrder": 2,
    "status": 1,
    "createBy": 1,
    "updateBy": 1,
    "createTime": "2026-06-07 14:30:00",
    "updateTime": "2026-06-07 14:30:00"
  }
]
```

`GET /api-assertion/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "caseId": 1,
  "assertType": "JSON_PATH",
  "expression": "$.code",
  "assertOperator": "=",
  "expectedValue": "200",
  "sortOrder": 1,
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00"
}
```

`POST /api-assertion/add` 响应数据：

```json
{
  "id": 1
}
```

`PUT /api-assertion/update`、`DELETE /api-assertion/delete/{id}`、`POST /api-assertion/batch-save` 响应数据：

```json
null
```

### 14.6 测试计划接口响应

`GET /test-plan/list` 响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "environmentId": 1,
      "environmentName": "测试环境",
      "planName": "登录模块回归测试",
      "description": "登录相关接口回归",
      "status": 1,
      "createBy": 1,
      "updateBy": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

`GET /test-plan/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "environmentId": 1,
  "environmentName": "测试环境",
  "planName": "登录模块回归测试",
  "description": "登录相关接口回归",
  "status": 1,
  "createBy": 1,
  "updateBy": 1,
  "createTime": "2026-06-07 14:30:00",
  "updateTime": "2026-06-07 14:30:00",
  "cases": [
    {
      "caseId": 1,
      "caseName": "登录成功",
      "apiId": 1,
      "apiName": "用户登录",
      "caseLevel": "P1",
      "sortOrder": 1
    }
  ]
}
```

`GET /test-plan/case-list` 响应数据：

```json
[
  {
    "caseId": 1,
    "caseName": "登录成功",
    "apiId": 1,
    "apiName": "用户登录",
    "caseLevel": "P1",
    "sortOrder": 1,
    "status": 1
  }
]
```

`POST /test-plan/add` 响应数据：

```json
{
  "id": 1
}
```

`PUT /test-plan/update`、`DELETE /test-plan/delete/{id}`、`PUT /test-plan/status`、`POST /test-plan/save-cases`、`DELETE /test-plan/delete-case` 响应数据：

```json
null
```

### 14.7 执行接口响应

`POST /execute/case` 响应数据：

```json
{
  "runRecordId": 1,
  "runDetailId": 1,
  "caseId": 1,
  "status": "PASS",
  "responseStatus": 200,
  "responseTime": 120,
  "assertResult": [
    {
      "assertType": "STATUS_CODE",
      "expression": "",
      "operator": "=",
      "expectedValue": "200",
      "actualValue": "200",
      "passed": true,
      "failReason": ""
    }
  ]
}
```

`POST /execute/plan` 响应数据：

```json
{
  "runRecordId": 1,
  "planId": 1,
  "status": "SUCCESS",
  "totalCount": 3,
  "successCount": 3,
  "failCount": 0,
  "timeoutCount": 0,
  "errorCount": 0,
  "passRate": 100.0,
  "totalTime": 560
}
```

`POST /execute/debug` 响应数据：

```json
{
  "requestUrl": "https://test.example.com/api/login",
  "requestMethod": "POST",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "responseStatus": 200,
  "responseHeaders": "{\"Content-Type\":\"application/json\"}",
  "responseBody": "{\"code\":200,\"message\":\"success\"}",
  "responseTime": 120,
  "errorMessage": ""
}
```

### 14.8 执行记录接口响应

`GET /run-record/list` 响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "environmentId": 1,
      "environmentName": "测试环境",
      "planId": 1,
      "planName": "登录模块回归测试",
      "caseId": null,
      "caseName": null,
      "runType": "PLAN",
      "totalCount": 3,
      "successCount": 3,
      "failCount": 0,
      "timeoutCount": 0,
      "errorCount": 0,
      "passRate": 100.0,
      "totalTime": 560,
      "status": "SUCCESS",
      "startTime": "2026-06-07 14:30:00",
      "endTime": "2026-06-07 14:30:01",
      "createBy": 1,
      "createTime": "2026-06-07 14:30:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

`GET /run-record/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "projectId": 1,
  "environmentId": 1,
  "environmentName": "测试环境",
  "planId": 1,
  "planName": "登录模块回归测试",
  "caseId": null,
  "caseName": null,
  "runType": "PLAN",
  "totalCount": 3,
  "successCount": 3,
  "failCount": 0,
  "timeoutCount": 0,
  "errorCount": 0,
  "passRate": 100.0,
  "totalTime": 560,
  "status": "SUCCESS",
  "startTime": "2026-06-07 14:30:00",
  "endTime": "2026-06-07 14:30:01",
  "details": [
    {
      "id": 1,
      "caseId": 1,
      "caseName": "登录成功",
      "apiId": 1,
      "apiName": "用户登录",
      "requestUrl": "https://test.example.com/api/login",
      "requestMethod": "POST",
      "responseStatus": 200,
      "responseTime": 120,
      "status": "PASS"
    }
  ]
}
```

### 14.9 执行详情接口响应

`GET /run-detail/list` 响应数据：

```json
[
  {
    "id": 1,
    "runRecordId": 1,
    "projectId": 1,
    "environmentId": 1,
    "planId": 1,
    "caseId": 1,
    "caseName": "登录成功",
    "apiId": 1,
    "apiName": "用户登录",
    "requestUrl": "https://test.example.com/api/login",
    "requestMethod": "POST",
    "responseStatus": 200,
    "responseTime": 120,
    "status": "PASS"
  }
]
```

`GET /run-detail/detail/{id}` 响应数据：

```json
{
  "id": 1,
  "runRecordId": 1,
  "projectId": 1,
  "environmentId": 1,
  "planId": 1,
  "caseId": 1,
  "caseName": "登录成功",
  "apiId": 1,
  "apiName": "用户登录",
  "requestUrl": "https://test.example.com/api/login",
  "requestMethod": "POST",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "responseStatus": 200,
  "responseHeaders": "{\"Content-Type\":\"application/json\"}",
  "responseBody": "{\"code\":200,\"message\":\"success\"}",
  "responseTime": 120,
  "assertResult": "[{\"assertType\":\"STATUS_CODE\",\"expression\":\"\",\"operator\":\"=\",\"expectedValue\":\"200\",\"actualValue\":\"200\",\"passed\":true,\"failReason\":\"\"}]",
  "errorMessage": "",
  "status": "PASS",
  "createBy": 1,
  "createTime": "2026-06-07 14:30:00"
}
```
