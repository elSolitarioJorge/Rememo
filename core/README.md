# core 模块职责清单

> **统一规则**

- 允许依赖：`core:base`、其他 core（按需要）
- 不允许依赖：任何 `feature:*`
- 不允许出现：具体业务词（here/timeline/profile…）

------

## `core:base`

**定位**：最底层基建，工程骨架
 **放什么**：

- BaseActivity / BaseFragment / BasePresenter / BaseView
- 通用 Result/Error 模型
- 通用线程/调度工具（轻量）
   **不放什么**：
- 网络/数据库/地图 SDK
- 任何业务常量与页面

------

## `core:common`

**定位**：通用约定与跨模块契约
 **放什么**：

- 路由常量 `Routes`
- 公共常量、通用工具（时间/格式化）
- Navigator 接口（如果你不用 ARouter 也可以放）
   **不放什么**：
- 具体业务页面逻辑
- 大而全工具（避免变成垃圾场）

------

## `core:network`

**定位**：统一网络能力
 **放什么**：

- Retrofit/OkHttp 初始化
- ApiClient、拦截器（token/log）
- 统一错误映射（HttpException -> AppError）
   **不放什么**：
- 具体业务 API（建议业务 API 接口放在对应 feature 的 data 层，但 Retrofit 实例放这里）

------

## `core:storage`

**定位**：本地存储统一能力
 **放什么**：

- SharedPreferences 封装（游客ID、用户偏好）
- Room/数据库封装（如果你用）
   **不放什么**：
- 业务实体的复杂逻辑（实体可以放 feature 的 data/domain）

------

## `core:media`

**定位**：多媒体能力封装
 **放什么**：

- 图片选择/压缩、Glide 封装（ImageLoader）
- 录音/播放（AudioRecorder/AudioPlayer）
   **不放什么**：
- 业务页面（如发布页 UI）

------

## `core:map`

**定位**：地图与定位能力封装
 **放什么**：

- 地图 SDK 初始化、定位、逆地理编码
- Marker 管理/聚类工具（后期）
   **不放什么**：
- 业务交互流程（长按发布、点击详情属于 feature:here）

------

## `core:ui`

**定位**：统一 UI 规范与可复用组件
 **放什么**：

- theme/colors/dimens
- 通用组件：Toolbar、EmptyView、LoadingView
- 通用 Adapter 基类
   **不放什么**：
- 强业务组件（某个 feature 才用的复杂 UI）

# 工具类注释规范

### 1) 什么必须写注释

- **对外 API（别人会调用的类/方法）**：必须写
- **全局单例 / Manager / Provider**：必须写（包含线程安全、生命周期）
- **工具类（Utils/Helper）**：必须写（说明用途、边界、不做什么）
- **封装第三方库的适配层**：必须写（避免以后换库时看不懂）

### 2) 类注释模板

```java
/**
 * <一句话：这个类做什么>
 *
 * <p>适用场景：</p>
 * <ul>
 *   <li>...</li>
 * </ul>
 *
 * <p>不做什么：</p>
 * <ul>
 *   <li>...</li>
 * </ul>
 *
 * <p>线程安全：</p>
 * <ul>
 *   <li>是否线程安全、是否需要在主线程调用</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>
 *   // 示例代码（可选）
 * </pre>
 */
```

### 3) 什么时候写线程安全说明

- 有缓存、单例、静态变量、共享集合、播放器/定位/网络客户端 等：**必须写**
- 纯静态纯函数（format、parse）：可不写

### 4) 什么时候写示例

- 调用姿势容易写错（比如 ARouter、网络、权限、音频录制）：建议写
- 简单 getter/setter：不写

### 5) `@author` 要不要写？

可选。不写也没问题。建议写“维护者”和“模块归属”更重要。