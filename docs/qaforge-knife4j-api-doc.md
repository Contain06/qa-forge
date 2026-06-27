# QAForge 接口文档

本文档根据当前后端 Controller、DTO、VO 和 Knife4j/OpenAPI3 依赖整理。

启动项目后可访问：

```text
Knife4j: http://localhost:8080/doc.html
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI JSON: http://localhost:8080/v3/api-docs
```

## 1. 通用约定

### 1.1 基础地址

```text
http://localhost:8080
```

### 1.2 通用请求头

除登录接口外，当前系统暂时使用 `User-Id` 模拟登录用户。

```text
User-Id: 1
Content-Type: application/json
```

文件上传接口使用：

```text
Content-Type: multipart/form-data
```

### 1.3 通用响应结构

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| code | Integer | 业务状态码，`200` 表示成功，`400/401/404/500` 表示失败 |
| message | String | 提示信息 |
| data | Object | 响应数据 |

分页响应结构：

```json
{
  "records": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

## 2. 用户模块

### 2.1 用户登录

```text
POST /user/login
```

请求体：

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应数据：

```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员",
  "token": "mock-token"
}
```

## 3. 项目管理

### 3.1 项目分页列表

```text
GET /project/list?pageNum=1&pageSize=10
```

请求头：

```text
User-Id: 1
```

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectName": "QAForge",
      "projectCode": "qa-forge",
      "description": "接口自动化测试平台",
      "ownerName": "admin",
      "status": 1,
      "createTime": "2026-06-20 16:00:00",
      "updateTime": "2026-06-20 16:00:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 3.2 项目详情

```text
GET /project/detail/{id}
```

### 3.3 新增项目

```text
POST /project/add
```

请求体：

```json
{
  "projectName": "QAForge",
  "projectCode": "qa-forge",
  "description": "接口自动化测试平台"
}
```

### 3.4 修改项目

```text
PUT /project/update
```

请求体：

```json
{
  "id": 1,
  "projectName": "QAForge",
  "projectCode": "qa-forge",
  "description": "接口自动化测试平台"
}
```

### 3.5 删除项目

```text
DELETE /project/delete/{id}
```

### 3.6 修改项目状态

```text
PUT /project/status
```

请求体：

```json
{
  "id": 1,
  "status": 1
}
```

## 4. 环境管理

### 4.1 环境分页列表

```text
GET /environment/list?projectId=1&pageNum=1&pageSize=10
```

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "projectName": "QAForge",
      "envName": "本地环境",
      "baseUrl": "http://localhost:8080",
      "description": "本地测试环境",
      "status": 1
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 4.2 环境详情

```text
GET /environment/detail/{id}
```

### 4.3 新增环境

```text
POST /environment/add
```

请求体：

```json
{
  "projectId": 1,
  "envName": "本地环境",
  "baseUrl": "http://localhost:8080",
  "description": "本地测试环境"
}
```

### 4.4 修改环境

```text
PUT /environment/update
```

请求体：

```json
{
  "id": 1,
  "envName": "本地环境",
  "baseUrl": "http://localhost:8080",
  "description": "本地测试环境"
}
```

### 4.5 删除环境

```text
DELETE /environment/delete/{id}
```

### 4.6 修改环境状态

```text
PUT /environment/status
```

请求体：

```json
{
  "id": 1,
  "status": 1
}
```

## 5. 接口管理

### 5.1 接口分页列表

```text
GET /api-info/list?projectId=1&pageNum=1&pageSize=10
```

可选参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| apiName | String | 接口名称，模糊查询 |
| requestMethod | String | 请求方法，如 GET/POST/PUT/DELETE |
| status | Integer | 状态 |

### 5.2 接口详情

```text
GET /api-info/detail/{id}
```

### 5.3 新增接口

```text
POST /api-info/add
```

请求体：

```json
{
  "projectId": 1,
  "apiName": "用户登录",
  "requestMethod": "POST",
  "apiPath": "/user/login",
  "requestHeaders": "{}",
  "requestParams": "{}",
  "requestBody": "{}",
  "description": "用户登录接口"
}
```

### 5.4 修改接口

```text
PUT /api-info/update
```

请求体：

```json
{
  "id": 1,
  "apiName": "用户登录",
  "requestMethod": "POST",
  "apiPath": "/user/login",
  "requestHeaders": "{}",
  "requestParams": "{}",
  "requestBody": "{}",
  "description": "用户登录接口"
}
```

### 5.5 删除接口

```text
DELETE /api-info/delete/{id}
```

### 5.6 修改接口状态

```text
PUT /api-info/status
```

请求体：

```json
{
  "id": 1,
  "status": 1
}
```

## 6. 测试用例管理

### 6.1 查询某个接口下的用例

```text
GET /api-case/list?apiId=1&pageNum=1&pageSize=10
```

可选参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| caseName | String | 用例名称 |
| caseLevel | String | 用例等级 |
| status | Integer | 状态 |

### 6.2 查询某个项目下的用例

```text
GET /api-case/listProjectCase?projectId=1&pageNum=1&pageSize=10
```

可选参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| apiId | Integer | 接口 ID |
| caseName | String | 用例名称 |
| caseLevel | String | 用例等级 |
| status | Integer | 状态 |

### 6.3 用例详情

```text
GET /api-case/detail/{id}
```

### 6.4 新增用例

```text
POST /api-case/add
```

请求体：

```json
{
  "projectId": 1,
  "apiId": 1,
  "caseName": "登录成功用例",
  "caseLevel": "P1",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "expectedResult": "登录成功",
  "description": "正确账号密码登录"
}
```

### 6.5 修改用例

```text
PUT /api-case/update
```

请求体：

```json
{
  "id": 1,
  "caseName": "登录成功用例",
  "caseLevel": "P1",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "expectedResult": "登录成功",
  "description": "正确账号密码登录"
}
```

### 6.6 删除用例

```text
DELETE /api-case/delete/{id}
```

### 6.7 批量删除用例

```text
POST /api-case/batch-delete
```

请求体：

```json
{
  "ids": [1, 2, 3]
}
```

### 6.8 修改用例状态

```text
PUT /api-case/status
```

请求体：

```json
{
  "id": 1,
  "status": 1
}
```

### 6.9 文件批量导入用例

```text
POST /api-case/batch-add?projectId=1&apiId=1
```

请求类型：

```text
multipart/form-data
```

表单字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| file | File | CSV 或 JSON 文件 |

JSON 文件示例：

```json
[
  {
    "caseName": "登录成功",
    "caseLevel": "P1",
    "requestHeaders": {"Content-Type": "application/json"},
    "requestParams": {},
    "requestBody": {"username": "admin", "password": "123456"},
    "expectedResult": "登录成功",
    "description": "正确账号密码登录"
  }
]
```

CSV 表头：

```csv
caseName,caseLevel,requestHeaders,requestParams,requestBody,expectedResult,description
```

## 7. 断言管理

### 7.1 查询用例断言

```text
GET /api-assertion/list?caseId=1
```

### 7.2 新增断言

```text
POST /api-assertion/add
```

请求体：

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

| 类型 | 说明 |
| --- | --- |
| STATUS_CODE | HTTP 状态码断言 |
| JSON_PATH | JSONPath 字段断言 |
| BODY | 响应体文本断言 |

断言运算符：

```text
=
!=
>
>=
<
<=
contains
exists
notEmpty
```

### 7.3 修改断言

```text
PUT /api-assertion/update
```

请求体：

```json
{
  "id": 1,
  "assertType": "JSON_PATH",
  "expression": "$.data.token",
  "assertOperator": "notEmpty",
  "expectedValue": "",
  "sortOrder": 2
}
```

### 7.4 删除断言

```text
DELETE /api-assertion/delete/{id}
```

### 7.5 修改断言状态

```text
PUT /api-assertion/status
```

请求体：

```json
{
  "id": 1,
  "status": 1
}
```

### 7.6 批量删除断言

```text
POST /api-assertion/batch-delete
```

请求体：

```json
{
  "ids": [1, 2, 3]
}
```

## 8. 单用例执行

### 8.1 执行单个用例

```text
POST /api-case/execute/{caseId}?envId=1
```

响应数据：

```json
{
  "caseId": 1,
  "caseName": "登录成功用例",
  "apiId": 1,
  "apiName": "用户登录",
  "envId": 1,
  "envName": "本地环境",
  "pass": true,
  "statusCode": 200,
  "responseBody": "{\"code\":200,\"message\":\"操作成功\"}",
  "assertionTotal": 3,
  "assertionPass": 3,
  "assertionFail": 0,
  "durationMs": 70,
  "errorMessage": null,
  "runRecordId": 1
}
```

说明：

```text
STATUS_CODE 断言判断 HTTP 状态码。
JSON_PATH $.code 判断响应体里的业务 code。
```

## 9. 执行记录与报告

### 9.1 执行记录列表

```text
GET /run-record/list?projectId=1&pageNum=1&pageSize=10
```

可选参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| status | String | PASS/FAIL/RUNNING |
| runType | String | CASE/PLAN |

响应数据：

```json
{
  "records": [
    {
      "id": 1,
      "projectId": 1,
      "projectName": "QAForge",
      "environmentId": 1,
      "envName": "本地环境",
      "planId": 0,
      "caseId": 1,
      "runType": "CASE",
      "totalCount": 1,
      "successCount": 1,
      "failCount": 0,
      "passRate": 100.0,
      "totalTime": 70,
      "status": "PASS",
      "startTime": "2026-06-20 16:00:00",
      "endTime": "2026-06-20 16:00:00"
    }
  ],
  "total": 1,
  "pageNum": 1,
  "pageSize": 10
}
```

### 9.2 执行报告详情

```text
GET /run-record/detail/{id}
```

响应数据：

```json
{
  "record": {
    "id": 1,
    "projectName": "QAForge",
    "envName": "本地环境",
    "runType": "CASE",
    "totalCount": 1,
    "successCount": 1,
    "failCount": 0,
    "passRate": 100.0,
    "status": "PASS"
  },
  "details": [
    {
      "id": 1,
      "runRecordId": 1,
      "caseId": 1,
      "caseName": "登录成功用例",
      "apiId": 1,
      "apiName": "用户登录",
      "requestUrl": "http://localhost:8080/user/login",
      "requestMethod": "POST",
      "responseStatus": 200,
      "responseTime": 70,
      "assertResult": "PASS",
      "status": "PASS"
    }
  ]
}
```

## 10. 测试计划

### 10.1 新增测试计划

```text
POST /test-plan/add
```

请求体：

```json
{
  "projectId": 1,
  "environmentId": 1,
  "planName": "冒烟测试计划",
  "description": "核心流程冒烟测试"
}
```

### 10.2 测试计划列表

```text
GET /test-plan/list?projectId=1&pageNum=1&pageSize=10
```

可选参数：

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| planName | String | 计划名称 |
| status | Integer | 状态 |

### 10.3 测试计划详情

```text
GET /test-plan/detail/{id}
```

响应数据：

```json
{
  "plan": {
    "id": 1,
    "projectName": "QAForge",
    "envName": "本地环境",
    "planName": "冒烟测试计划",
    "description": "核心流程冒烟测试",
    "status": 1,
    "caseCount": 3
  },
  "cases": []
}
```

### 10.4 修改测试计划

```text
PUT /test-plan/update
```

请求体：

```json
{
  "id": 1,
  "environmentId": 1,
  "planName": "冒烟测试计划",
  "description": "核心流程冒烟测试"
}
```

### 10.5 删除测试计划

```text
DELETE /test-plan/delete/{id}
```

### 10.6 修改测试计划状态

```text
PUT /test-plan/status
```

请求体：

```json
{
  "id": 1,
  "status": 1
}
```

### 10.7 绑定用例到测试计划

```text
POST /test-plan/bind-cases
```

请求体：

```json
{
  "planId": 1,
  "caseIds": [1, 2, 3]
}
```

说明：

```text
bind-cases 表示把多个测试用例加入某个测试计划。
后端会先删除该计划原有绑定关系，再保存新的用例列表。
```

### 10.8 从测试计划移除用例

```text
DELETE /test-plan/remove-case?planId=1&caseId=2
```

### 10.9 查询测试计划下的用例

```text
GET /test-plan/cases/{planId}
```

### 10.10 执行测试计划

```text
POST /test-plan/execute/{planId}?envId=1
```

说明：

```text
envId 可选。
如果不传 envId，则使用测试计划保存的默认 environmentId。
```

执行逻辑：

```text
1. 校验测试计划属于当前用户
2. 查询测试计划绑定的用例
3. 按 sortOrder 依次执行用例
4. 生成 1 条 run_record
5. 每个用例生成 1 条 run_detail
6. 返回 RunReportVO 报告详情
```

响应数据：

```json
{
  "record": {
    "id": 20,
    "projectName": "QAForge",
    "envName": "本地环境",
    "runType": "PLAN",
    "totalCount": 3,
    "successCount": 2,
    "failCount": 1,
    "passRate": 66.67,
    "totalTime": 180,
    "status": "FAIL"
  },
  "details": [
    {
      "caseName": "登录成功用例",
      "apiName": "用户登录",
      "requestUrl": "http://localhost:8080/user/login",
      "responseStatus": 200,
      "responseTime": 70,
      "assertResult": "PASS",
      "status": "PASS"
    }
  ]
}
```

## 11. 推荐测试流程

### 11.1 单用例执行流程

```text
POST /project/add
POST /environment/add
POST /api-info/add
POST /api-case/add
POST /api-assertion/add
POST /api-case/execute/{caseId}?envId=1
GET  /run-record/detail/{runRecordId}
```

### 11.2 测试计划执行流程

```text
POST /test-plan/add
POST /test-plan/bind-cases
GET  /test-plan/detail/{id}
POST /test-plan/execute/{planId}?envId=1
GET  /run-record/detail/{runRecordId}
```

## 12. Knife4j 使用说明

启动后访问：

```text
http://localhost:8080/doc.html
```

如果页面能打开，说明 Knife4j 已生效。当前项目已经配置：

```yaml
knife4j:
  enable: true

springdoc:
  swagger-ui:
    path: /swagger-ui.html
  api-docs:
    path: /v3/api-docs
```

后续如果想让 Knife4j 页面更漂亮，可以在 Controller 上补充：

```java
@Tag(name = "项目管理")
```

在接口方法上补充：

```java
@Operation(summary = "新增项目")
```

在 DTO 字段上补充：

```java
@Schema(description = "项目名称", example = "QAForge")
```

这些注解不是必须的，但能让 Knife4j 页面里的接口说明更清楚。
