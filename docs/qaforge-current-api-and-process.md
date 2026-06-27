# QAForge 当前接口文档与项目编写过程

更新时间：2026-06-18

本文档按当前代码状态重新整理，覆盖两部分：

1. 当前已经写出的后端接口文档。
2. 整个项目推荐编写过程、每一步理由，以及你目前写到哪一步。

## 1. 项目定位

QAForge 是一个接口自动化测试平台。

它的核心目标不是只保存接口资料，而是最终能够做到：

```text
维护项目 -> 维护环境 -> 维护接口 -> 维护用例 -> 维护断言 -> 执行用例 -> 保存执行记录
```

当前数据关系可以理解为：

```text
SysUser 用户
  └── Project 项目
        ├── Environment 环境
        └── ApiInfo 接口
              └── ApiCase 测试用例
                    └── ApiAssertion 断言规则

TestPlan 测试计划
RunRecord 执行记录
RunDetail 执行详情
```

目前执行模块还没有正式开始，所以 `TestPlan`、`RunRecord`、`RunDetail` 实体类存在，但对应 Controller 还没有写。

## 2. 通用约定

### 2.1 基础地址

```text
http://localhost:8080
```

### 2.2 登录后的用户标识

除登录接口外，目前大多数接口通过请求头模拟当前登录用户：

```text
User-Id: 1
```

这是前期开发阶段的临时方案。后期接入 JWT 后，应该改成：

```text
Authorization: Bearer token
```

然后由后端从 token 中解析当前用户。

### 2.3 统一响应结构

当前项目使用 `Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

常见业务码：

```text
200 成功
400 请求参数错误
404 数据不存在或无权限
500 业务失败或服务端错误
```

### 2.4 分页响应结构

分页接口统一返回：

```json
{
  "records": [],
  "total": 0,
  "pageNum": 1,
  "pageSize": 10
}
```

说明：

```text
records 当前页数据
total 数据总条数
pageNum 当前页码
pageSize 每页条数
```

`total` 应使用 `result.getTotal()`，前提是 MyBatis-Plus 分页插件已经配置。

### 2.5 逻辑删除与状态

项目实体中多数字段有：

```text
status  启用状态：1 启用，0 禁用
deleted 逻辑删除：0 未删除，1 已删除
```

区别：

```text
status 控制能不能使用
deleted 控制是否已经删除
```

加了 `@TableLogic` 后：

```java
removeById(id)
```

实际会变成逻辑删除：

```sql
UPDATE xxx SET deleted = 1 WHERE id = ?
```

普通查询时 MyBatis-Plus 会自动过滤 `deleted = 1` 的数据。

## 3. 用户模块

### 3.1 登录

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

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "token": "mock-token"
  }
}
```

说明：

当前登录是初级版本，直接按用户名、密码、状态查询用户。

当前 token 是：

```text
mock-token
```

还没有真正实现 JWT。

后续改造方向：

```text
1. 密码加密存储
2. 登录成功生成 JWT
3. 前端后续请求带 Authorization
4. 后端用拦截器解析 token 获取 userId
```

## 4. 项目管理模块

项目是 QAForge 的最高业务容器。接口、环境、用例都要归到项目下。

### 4.1 项目分页列表

```text
GET /project/list?pageNum=1&pageSize=10
Header: User-Id: 1
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "projectName": "QAForge",
        "projectCode": "qa-forge",
        "description": "接口自动化测试平台",
        "ownerName": "管理员",
        "status": 1,
        "createTime": "2026-06-07 14:30:00",
        "updateTime": "2026-06-07 14:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

理由：

项目列表只查当前用户拥有的项目，所以通过 `User-Id` 过滤 `ownerId`。

### 4.2 项目详情

```text
GET /project/detail/{id}
Header: User-Id: 1
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "projectName": "QAForge",
    "projectCode": "qa-forge",
    "description": "接口自动化测试平台",
    "ownerName": "管理员",
    "status": 1,
    "createTime": "2026-06-07 14:30:00",
    "updateTime": "2026-06-07 14:30:00"
  }
}
```

### 4.3 新增项目

```text
POST /project/add
Header: User-Id: 1
```

请求体：

```json
{
  "projectName": "QAForge",
  "projectCode": "qa-forge",
  "description": "接口自动化测试平台"
}
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

说明：

`ownerId`、`createBy`、`updateBy` 不应该由前端传，应该由后端根据当前登录用户设置。

### 4.4 修改项目

```text
PUT /project/update
Header: User-Id: 1
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

### 4.5 删除项目

```text
DELETE /project/delete/{id}
Header: User-Id: 1
```

说明：

项目实体有 `@TableLogic`，所以删除是逻辑删除。

### 4.6 修改项目状态

```text
PUT /project/status
Header: User-Id: 1
```

请求体：

```json
{
  "id": 1,
  "status": 0
}
```

当前代码使用 `ProjectDTO` 接收状态修改。后续建议改成通用 `StatusDTO`，保持和环境、接口、用例一致。

## 5. 环境管理模块

环境用于保存接口执行时的基础地址。

例如：

```text
dev  http://localhost:8080
test https://test.example.com
prod https://api.example.com
```

### 5.1 环境分页列表

```text
GET /environment/list?projectId=1&pageNum=1&pageSize=10
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "projectId": 1,
        "projectName": "QAForge",
        "envName": "测试环境",
        "baseUrl": "https://test.example.com",
        "description": "测试环境地址",
        "status": 1,
        "createTime": "2026-06-07 14:30:00",
        "updateTime": "2026-06-07 14:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

当前代码说明：

`/environment/list` 当前只传 `projectId`，暂时没有要求 `User-Id`。后续建议也加上 `User-Id` 并校验项目归属，和详情、新增、修改保持一致。

### 5.2 环境详情

```text
GET /environment/detail/{id}
Header: User-Id: 1
```

### 5.3 新增环境

```text
POST /environment/add
Header: User-Id: 1
```

请求体：

```json
{
  "projectId": 1,
  "envName": "测试环境",
  "baseUrl": "https://test.example.com",
  "description": "测试环境地址"
}
```

说明：

新增环境前，后端会校验：

```text
projectId 是否存在
project.ownerId 是否等于 User-Id
project.status 是否为 1
```

### 5.4 修改环境

```text
PUT /environment/update
Header: User-Id: 1
```

请求体：

```json
{
  "id": 1,
  "envName": "测试环境",
  "baseUrl": "https://test.example.com",
  "description": "更新后的描述"
}
```

说明：

普通修改环境时不允许前端把环境移动到其他项目下。

### 5.5 删除环境

```text
DELETE /environment/delete/{id}
Header: User-Id: 1
```

### 5.6 修改环境状态

```text
PUT /environment/status
Header: User-Id: 1
```

请求体：

```json
{
  "id": 1,
  "status": 0
}
```

## 6. 接口管理模块

接口管理保存的是接口模板。

例如：

```text
POST /user/login
```

它保存的是：

```text
接口名称
请求方法
接口路径
公共请求头
公共请求参数
公共请求体结构
```

### 6.1 接口分页列表

```text
GET /api-info/list?projectId=1&pageNum=1&pageSize=10
Header: User-Id: 1
```

可选查询参数：

```text
apiName
requestMethod
status
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "projectId": 1,
        "projectName": "QAForge",
        "apiName": "登录接口",
        "requestMethod": "POST",
        "apiPath": "/user/login",
        "requestHeaders": "{\"Content-Type\":\"application/json\"}",
        "requestParams": "{}",
        "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
        "description": "用户登录",
        "status": 1,
        "createTime": "2026-06-07 14:30:00",
        "updateTime": "2026-06-07 14:30:00"
      }
    ],
    "total": 1,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 6.2 接口详情

```text
GET /api-info/detail/{id}
Header: User-Id: 1
```

### 6.3 新增接口

```text
POST /api-info/add
Header: User-Id: 1
```

请求体：

```json
{
  "projectId": 1,
  "apiName": "登录接口",
  "requestMethod": "POST",
  "apiPath": "/user/login",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "description": "用户登录"
}
```

说明：

新增接口必须校验项目归属，不能相信前端随便传来的 `projectId`。

### 6.4 修改接口

```text
PUT /api-info/update
Header: User-Id: 1
```

请求体：

```json
{
  "id": 1,
  "apiName": "登录接口",
  "requestMethod": "POST",
  "apiPath": "/user/login",
  "requestHeaders": "{\"Content-Type\":\"application/json\"}",
  "requestParams": "{}",
  "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
  "description": "用户登录"
}
```

说明：

普通修改接口时不允许前端把接口移动到其他项目下。

### 6.5 删除接口

```text
DELETE /api-info/delete/{id}
Header: User-Id: 1
```

### 6.6 修改接口状态

```text
PUT /api-info/status
Header: User-Id: 1
```

请求体：

```json
{
  "id": 1,
  "status": 0
}
```

## 7. 测试用例管理模块

测试用例表示某个接口的一组具体测试数据。

例如同一个登录接口可以有：

```text
登录成功用例
密码错误用例
账号禁用用例
参数为空用例
```

关系：

```text
ApiInfo 接口
  └── ApiCase 测试用例
```

### 7.1 按接口查询用例列表

```text
GET /api-case/list?apiId=1&pageNum=1&pageSize=10
Header: User-Id: 1
```

可选参数：

```text
caseName
caseLevel
status
```

说明：

这个接口用于查看某个接口下面有多少测试用例。

### 7.2 按项目查询用例列表

```text
GET /api-case/listProjectCase?projectId=1&pageNum=1&pageSize=10
Header: User-Id: 1
```

可选参数：

```text
apiId
caseName
caseLevel
status
```

说明：

这个接口用于查看某个项目下面的全部用例，也可以通过 `apiId` 再筛选某个接口。

### 7.3 用例详情

```text
GET /api-case/detail/{id}
Header: User-Id: 1
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "projectId": 1,
    "projectName": "QAForge",
    "apiId": 1,
    "apiName": "登录接口",
    "caseName": "登录成功用例",
    "caseLevel": "P1",
    "requestHeaders": "{\"Content-Type\":\"application/json\"}",
    "requestParams": "{}",
    "requestBody": "{\"username\":\"admin\",\"password\":\"123456\"}",
    "expectedResult": "{\"code\":200}",
    "description": "测试登录成功",
    "status": 1,
    "createTime": "2026-06-07 14:30:00",
    "updateTime": "2026-06-07 14:30:00"
  }
}
```

### 7.4 新增用例

```text
POST /api-case/add
Header: User-Id: 1
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
  "expectedResult": "{\"code\":200}",
  "description": "测试登录成功"
}
```

最简请求体：

```json
{
  "projectId": 1,
  "apiId": 1,
  "caseName": "登录成功用例",
  "caseLevel": "P1"
}
```

说明：

当前代码会给未传字段补默认值：

```text
requestHeaders -> {}
requestParams  -> {}
requestBody    -> {}
expectedResult -> {}
description    -> ""
```

### 7.5 修改用例

```text
PUT /api-case/update
Header: User-Id: 1
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
  "expectedResult": "{\"code\":200}",
  "description": "测试登录成功"
}
```

说明：

普通修改用例不允许把用例移动到其他接口下，所以 UpdateDTO 不包含 `projectId` 和 `apiId`。

### 7.6 删除用例

```text
DELETE /api-case/delete/{id}
Header: User-Id: 1
```

### 7.7 批量删除用例

```text
POST /api-case/batch-delete
Header: User-Id: 1
```

请求体：

```json
{
  "ids": [1, 2, 3]
}
```

注意：

字段名是 `ids`，不是 `id`。

### 7.8 修改用例状态

```text
PUT /api-case/status
Header: User-Id: 1
```

请求体：

```json
{
  "id": 1,
  "status": 0
}
```

### 7.9 批量新增用例

```text
POST /api-case/batch-add?projectId=1&apiId=1
Header: User-Id: 1
Content-Type: multipart/form-data
```

form-data：

```text
file: 选择 csv 或 json 文件
```

JSON 文件格式：

```json
[
  {
    "caseName": "登录成功用例",
    "caseLevel": "P1",
    "requestHeaders": {
      "Content-Type": "application/json"
    },
    "requestParams": {},
    "requestBody": {
      "username": "admin",
      "password": "123456"
    },
    "expectedResult": "{\"code\":200}",
    "description": "测试登录成功"
  }
]
```

CSV 表头：

```text
caseName,caseLevel,requestHeaders,requestParams,requestBody,expectedResult,description
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "ids": [1, 2]
  }
}
```

## 8. 断言管理模块

断言是用来判断接口响应是否符合预期的规则。

断言不是前端执行的，前端只是配置断言规则。真正执行时，后端会：

```text
1. 发起 HTTP 请求
2. 拿到响应状态码、响应头、响应体
3. 根据断言规则判断是否通过
```

关系：

```text
ApiCase 用例
  └── ApiAssertion 断言
```

当前断言操作符使用符号版本：

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

### 8.1 查询用例下的断言列表

```text
GET /api-assertion/list?caseId=1
Header: User-Id: 1
```

响应：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "caseId": 1,
      "assertType": "JSON_PATH",
      "expression": "$.code",
      "assertOperator": "=",
      "expectedValue": "200",
      "sortOrder": 1,
      "status": 1,
      "createTime": "2026-06-07 14:30:00",
      "updateTime": "2026-06-07 14:30:00"
    }
  ]
}
```

### 8.2 新增断言

```text
POST /api-assertion/add
Header: User-Id: 1
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

其他断言示例：

```json
{
  "caseId": 1,
  "assertType": "JSON_PATH",
  "expression": "$.data.token",
  "assertOperator": "notEmpty",
  "expectedValue": "",
  "sortOrder": 2
}
```

```json
{
  "caseId": 1,
  "assertType": "BODY",
  "expression": "",
  "assertOperator": "contains",
  "expectedValue": "操作成功",
  "sortOrder": 3
}
```

### 8.3 断言管理未完成接口

当前代码里断言模块已经完成：

```text
GET  /api-assertion/list
POST /api-assertion/add
```

还没有完成：

```text
PUT    /api-assertion/update
GET    /api-assertion/detail/{id}
DELETE /api-assertion/delete/{id}
POST   /api-assertion/batch-delete
PUT    /api-assertion/status
```

建议你下一步先写：

```text
PUT /api-assertion/update
```

原因：

列表和新增写完后，前端已经可以展示和添加断言。下一步自然是允许用户修改断言规则。

## 9. 全局校验错误返回

当前已经添加全局异常处理器：

```text
GlobalExceptionHandler
```

现在 `@Valid` 校验失败时，不再返回 Spring 默认的：

```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "path": "..."
}
```

而是返回项目统一格式：

```json
{
  "code": 400,
  "message": "Assert operator is invalid",
  "data": null
}
```

## 10. 整个项目推荐编写过程

### 第 1 步：数据库设计和实体类

状态：已完成。

你已经有这些实体类：

```text
SysUser
Project
Environment
ApiInfo
ApiCase
ApiAssertion
TestPlan
RunRecord
RunDetail
```

理由：

后端接口依赖实体关系。先设计表和实体，后面 Controller、Service、Mapper 才知道数据应该怎么流动。

### 第 2 步：统一响应结构 Result

状态：已完成。

理由：

前端不应该每个接口都解析不同格式，所以统一：

```text
code
message
data
```

这样前端处理成功、失败、弹窗提示都会简单很多。

### 第 3 步：登录接口

状态：已完成初级版本。

理由：

所有数据都和用户有关。虽然当前还没有 JWT，但先用 `User-Id` 模拟当前用户，可以让项目继续向后推进。

后续要升级：

```text
mock-token -> JWT
User-Id 请求头 -> Authorization token
```

### 第 4 步：项目管理

状态：已完成基础 CRUD。

理由：

项目是最高层业务容器。没有项目，环境、接口、用例都没有归属。

### 第 5 步：环境管理

状态：已完成基础 CRUD。

理由：

环境决定接口执行时请求发到哪里。后面执行用例时需要：

```text
baseUrl + apiPath
```

组成完整请求地址。

### 第 6 步：接口管理

状态：已完成基础 CRUD。

理由：

接口管理保存接口模板，例如：

```text
POST /user/login
```

用例是基于接口创建的。没有接口模板，就无法知道用例要请求哪个接口。

### 第 7 步：测试用例管理

状态：已完成主要接口。

已完成：

```text
按接口查用例
按项目查用例
详情
新增
修改
删除
批量删除
修改状态
批量上传 CSV/JSON 新增
```

理由：

用例保存的是某个接口的一次具体测试数据。例如登录接口可以有登录成功、密码错误、账号禁用等多个用例。

### 第 8 步：断言管理

状态：正在编写。

你目前写到这里。

已经完成：

```text
GET  /api-assertion/list
POST /api-assertion/add
```

正在继续：

```text
PUT /api-assertion/update
```

理由：

断言管理本质上还是 CRUD，但它保存的是后续执行模块要用的判断规则。

例如：

```text
$.code = 200
$.data.token notEmpty
响应体 contains 操作成功
```

### 第 9 步：断言执行逻辑

状态：未开始。

理由：

断言 CRUD 只是保存规则，真正难的是执行时根据响应结果判断规则是否通过。

后端需要支持：

```text
状态码断言
JSON_PATH 断言
响应体包含断言
响应头断言
```

会用到：

```text
JsonPath 读取响应 JSON 字段
OkHttp 发 HTTP 请求
```

### 第 10 步：单用例执行接口

状态：未开始。

建议接口：

```text
POST /api-case/execute/{caseId}?envId=1
```

执行流程：

```text
1. 查 ApiCase
2. 查 ApiInfo
3. 查 Environment
4. 拼接 URL：environment.baseUrl + apiInfo.apiPath
5. 合并请求头、参数、请求体
6. 用 OkHttp 发请求
7. 拿响应状态码、响应头、响应体
8. 查 ApiAssertion
9. 执行断言
10. 保存 RunRecord / RunDetail
```

### 第 11 步：测试计划管理

状态：未开始。

理由：

单个用例执行完成后，下一步就是批量组织多个用例一起执行，这就是测试计划。

建议接口：

```text
GET    /test-plan/list
POST   /test-plan/add
PUT    /test-plan/update
DELETE /test-plan/delete/{id}
POST   /test-plan/bind-cases
```

### 第 12 步：执行记录和执行详情

状态：未开始。

理由：

接口自动化平台必须能回答：

```text
什么时候执行的？
执行了哪些用例？
通过几个？
失败几个？
失败原因是什么？
响应内容是什么？
```

这部分由：

```text
RunRecord
RunDetail
```

负责。

## 11. 你目前写到哪一步

你目前处于：

```text
第 8 步：断言管理模块
```

具体状态：

```text
登录：已写基础版
项目管理：已写
环境管理：已写
接口管理：已写
测试用例管理：已写
断言管理：已写列表和新增，剩余修改、删除、批量删除、状态
执行模块：未开始
测试计划：未开始
执行记录：未开始
```

最推荐的下一步：

```text
继续写 PUT /api-assertion/update
```

然后按这个顺序补齐断言模块：

```text
1. PUT /api-assertion/update
2. DELETE /api-assertion/delete/{id}
3. POST /api-assertion/batch-delete
4. PUT /api-assertion/status
5. GET /api-assertion/detail/{id}
```

断言管理补齐后，再进入真正的核心逻辑：

```text
执行单个用例
```

## 12. 当前代码中建议后续优化的点

### 12.1 登录需要升级 JWT

当前：

```text
mock-token + User-Id
```

后续：

```text
JWT + Authorization
```

### 12.2 ProjectDTO 建议拆分

当前项目模块新增、修改、状态修改共用 `ProjectDTO`。

后续建议拆为：

```text
ProjectAddDTO
ProjectUpdateDTO
StatusDTO
```

理由：

新增不需要 `id`，修改需要 `id`，状态修改只需要 `id/status`。

### 12.3 Environment list 建议增加 User-Id 校验

当前：

```text
GET /environment/list?projectId=1
```

没有接收 `User-Id`。

后续建议：

```text
GET /environment/list?projectId=1
Header: User-Id: 1
```

并校验项目归属。

### 12.4 断言类型也建议限制

当前你已经限制了 `assertOperator`。

建议也限制 `assertType`：

```text
STATUS_CODE
JSON_PATH
BODY
HEADER
```

### 12.5 执行模块开始前不要急着重构

现在最重要的是把主流程打通。

推荐节奏：

```text
先写得通
再写得好
最后再抽公共方法和优化结构
```

