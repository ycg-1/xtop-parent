# XTop 微服务架构说明文档

> 面向新加入的同事：读完本文即可理解整个 `xtop-parent` 多模块工程的分层设计、模块职责、依赖方向与构建方式。

---

## 一、项目概述

`xtop-parent` 是整个微服务体系的**根聚合工程**（`packaging=pom`），统一管理所有子模块的版本、依赖与构建顺序。整体采用 **Maven 多模块** 组织方式，并通过继承链实现版本的集中治理。

工程内并存**两套业务架构范式**，便于团队对比评估：

- **传统三层架构**：`xtop-infra`（基础服务），适合逻辑简单、以 CRUD 为主的场景。
- **DDD 四层架构**：`xtop-ddd-order`（订单）、`xtop-ddd-user`（用户），适合业务规则复杂、状态流转多的场景。

公共能力（通用响应体、异常、枚举、Feign 契约）与统一鉴权网关保持独立，不被业务范式绑架。

---

## 二、技术栈

| 类别 | 选型                             |
|------|--------------------------------|
| 语言 / JDK | Java 8                         |
| 微服务框架 | Spring Boot 3.x + Spring Cloud |
| 服务网关 | Spring Cloud Gateway           |
| 服务间调用 | OpenFeign                      |
| 持久层 | MyBatis-Plus                   |
| 数据库 | MySQL                          |
| 构建工具 | Maven（多模块聚合 + 继承）              |

---

## 三、整体目录结构

```text
xtop-parent/                          根父工程（版本/依赖统一管理，packaging=pom）
├── xtop-common/                      公共模块（聚合 pom，所有服务共享）
│   ├── xtop-common-model/            通用响应体 Result、全局异常、通用枚举
│   └── xtop-common-feign/            跨服务 Feign 接口契约
│
├── xtop-infra/                       基础微服务：雪花 ID / Token（传统三层）
│   ├── xtop-infra-model/             服务内部私有实体 / DTO
│   ├── xtop-infra-dao/               Mapper 接口 + XML（私有持久层）
│   ├── xtop-infra-service/           核心业务逻辑
│   ├── xtop-infra-controller/        HTTP 接口
│   └── xtop-infra-bootstrap/         启动类 + application.yml
│
├── xtop-gateway/                     统一鉴权网关（单模块：路由 / 全局过滤器 / 鉴权）
│
├── xtop-ddd-order/                   订单微服务（DDD 四层）
│   ├── xtop-ddd-order-domain/            领域层：聚合根 / 值对象 / 领域事件 / 仓储接口
│   ├── xtop-ddd-order-infrastructure/    基础设施层：PO / Mapper / 仓储实现
│   ├── xtop-ddd-order-application/       应用层：命令编排 / 事务控制
│   ├── xtop-ddd-order-api/               接口层：Controller / DTO
│   └── xtop-ddd-order-bootstrap/         启动类 + application.yml
│
└── xtop-ddd-user/                    用户微服务（DDD 四层，结构同上）
    ├── xtop-ddd-user-domain/
    ├── xtop-ddd-user-infrastructure/
    ├── xtop-ddd-user-application/
    ├── xtop-ddd-user-api/
    └── xtop-ddd-user-bootstrap/
```

---

## 四、继承与聚合关系

### 4.1 继承链（版本集中治理）

```text
xtop-parent
   ├── xtop-common     ──► xtop-common-model / xtop-common-feign
   ├── xtop-infra      ──► xtop-infra-model / dao / service / controller / bootstrap
   ├── xtop-gateway
   ├── xtop-ddd-order  ──► domain / infrastructure / application / api / bootstrap
   └── xtop-ddd-user   ──► domain / infrastructure / application / api / bootstrap
```

- 所有模块只声明 `parent`，**不重复写 `version`**，版本由父工程统一控制。
- 聚合工程（`xtop-common` / `xtop-infra` / `xtop-ddd-order` / `xtop-ddd-user` / `xtop-parent`）`packaging` 均为 `pom`，且需删除自身 `src` 目录，仅作容器。
- 叶子业务模块 `packaging` 为 `jar`。

### 4.2 关键命名约束

**同一父工程下不允许出现重复的 `artifactId`**（例如父级叫 `xtop-infra`，子模块就不能也叫 `xtop-infra`，否则构建直接报错）。因此父级聚合用 `xtop-infra`，核心业务层命名为 `xtop-infra-service`。

---

## 五、模块职责详解

### 5.1 公共层（xtop-common）

| 模块 | 职责 | 红线 |
|------|------|------|
| `xtop-common-model` | 全局统一响应 `Result<T>`、全局异常 `BusinessException`、通用枚举、跨服务返回对象 | **禁止出现任何具体业务实体或数据库表结构** |
| `xtop-common-feign` | 跨服务调用的 Feign 接口契约（`@FeignClient`） | 仅放接口与契约，不含实现 |

> `xtop-common-model` 是「外交官」——所有服务都能依赖它；各微服务私有的实体应放在各自的 model 中，而不是塞进公共 model。

### 5.2 基础服务（xtop-infra，传统三层）

提供雪花 ID、Token 等基础能力。业务简单，采用传统分层即可，无需 DDD。

| 模块 | 职责 |
|------|------|
| `xtop-infra-model` | infra 服务私有实体（如 TokenPO、雪花 ID 记录实体） |
| `xtop-infra-dao` | 本服务私有数据库的 Mapper 与连接池配置 |
| `xtop-infra-service` | 雪花算法、Token 加解密等核心逻辑 |
| `xtop-infra-controller` | 对外 HTTP 接口 |
| `xtop-infra-bootstrap` | 唯一启动模块，装配上面所有层 |

> **DAO 是私有财产**：每个微服务都有自己的 DAO，绝不下沉到 common。否则订单服务会被迫引入 infra 的数据库连接池，导致启动冲突、内存飙升与数据越权。

### 5.3 网关（xtop-gateway）

独立单模块，负责统一路由、全局过滤器、统一鉴权。**不承载业务逻辑**，不参与 DDD 改造。

### 5.4 DDD 业务服务（xtop-ddd-order / xtop-ddd-user）

经典四层 + 启动层，各层职责与关键约束如下：

| 层 | 职责 | 关键约束 |
|----|------|----------|
| **domain（领域层）** | 聚合根、值对象、领域事件、仓储接口；业务规则内聚在充血模型中 | **纯 Java，零框架依赖**，不依赖 Spring / MyBatis，也不依赖 infrastructure |
| **infrastructure（基础设施层）** | PO、Mapper、仓储接口的实现（`XxxRepositoryImpl`）、技术配置 | 实现 domain 定义的仓储接口（依赖倒置） |
| **application（应用层）** | 应用服务编排、命令 / 查询、事务控制、发布领域事件 | 只编排，不写核心业务规则 |
| **api（接口层）** | Controller、对外 DTO | 对应传统 controller，承担对外契约职责（无需单独 facade） |
| **bootstrap（启动层）** | 启动类、`application.yml`、装配所有模块 | 唯一的可运行入口 |

---

## 六、依赖方向（务必遵守）

### 6.1 DDD 依赖倒置原则

```text
        +----------+
        |   api    |
        +----+-----+
             |
             v
      +--------------+
      | application  |
      +----+---------+
           |         ^ 依赖
   +--------------+  |
   |infrastructure|--+   （实现 domain 的仓储接口）
   +--------------+
           |
           v
      +--------+
      | domain |   <-- 最内核：谁都可以依赖它，它不依赖任何人
      +--------+
```

- **domain 层禁止依赖 infrastructure**（最常见的错误）。仓储接口定义在 domain，实现放在 infrastructure，通过依赖倒置解耦。
- PO（持久化对象，贫血）留在 infrastructure，领域对象（充血）留在 domain，二者物理隔离、通过转换器（convert）互转。

### 6.2 传统三层依赖方向

```text
controller -> service -> dao -> model
```

线性单向，禁止反向依赖。

---

## 七、构建与打包顺序

### 7.1 模块声明顺序（聚合 pom 的 modules）

执行 `mvn clean install` 时，**基础依赖必须排在前面**，否则下游模块编译会报 `Could not resolve dependencies`。

根工程 `xtop-parent/pom.xml` 的模块顺序：

```xml
<modules>
    <!-- 1. 公共模块必须最先 -->
    <module>xtop-common</module>
    <!-- 2. 独立服务与业务服务（Maven 会按依赖关系自动排内部顺序） -->
    <module>xtop-gateway</module>
    <module>xtop-infra</module>
    <module>xtop-ddd-order</module>
    <module>xtop-ddd-user</module>
</modules>
```

DDD 服务内部的模块顺序（底层优先）：

```xml
<modules>
    <module>xtop-ddd-order-domain</module>
    <module>xtop-ddd-order-infrastructure</module>
    <module>xtop-ddd-order-application</module>
    <module>xtop-ddd-order-api</module>
    <module>xtop-ddd-order-bootstrap</module>
</modules>
```

### 7.2 构建命令

在 `xtop-parent` 根目录执行：

```bash
mvn clean install
```

Maven 会沿继承与聚合关系，按依赖拓扑自动完成流水线式打包。

---

## 八、两套架构对比（团队评估参考）

| 评估维度 | 传统三层（xtop-infra） | DDD 四层（xtop-ddd-order / user） |
|---------|----------------------|----------------------------------|
| 模块数 | 5（model/dao/service/controller/bootstrap） | 5（domain/infrastructure/application/api/bootstrap） |
| 业务逻辑位置 | Service 实现类 | 聚合根内部（充血模型） |
| 数据模型 | OrderDO 贯穿各层 | PO / 领域对象分离 |
| 新增规则改动 | 改 Service | 在聚合根加方法，改动集中 |
| 学习成本 | 低 | 中（需理解充血模型与依赖倒置） |
| 可测试性 | 需连库 mock | domain 层纯 Java，可脱离框架单测 |
| 适用场景 | CRUD 为主、逻辑简单 | 复杂业务规则、状态流转多 |

**选型建议**：简单工具型 / CRUD 服务用传统三层；核心复杂业务用 DDD 四层。本项目两套并存，正是为团队提供直观对照。

---

## 九、给新同事的三条铁律

1. **DAO 永不下沉公共模块**——每个微服务的持久层是私有财产。
2. **domain 永不依赖 infrastructure / Spring**——它是纯业务内核，依赖方向永远向内。
3. **版本只写 parent，不写 version**——一切版本变更集中在 `xtop-parent` 统一管理。
