# core:base

## 目标
提供最底层的工程骨架与基础抽象，保证所有 feature/core 的基础结构一致。

## 放什么
- BaseActivity / BaseFragment（公共生命周期、通用UI状态位）
- MVP 基类：BaseView / BasePresenter / BaseContract（如果你坚持 MVP）
- 通用结果封装：Result / AppError / ErrorCode（建议轻量）
- 线程/调度基础设施（如需要：MainThread/IOExecutor 的最小封装）

## 不放什么
- 任何三方 SDK 的初始化（网络/地图/媒体都不放）
- 任何具体业务（here/timeline/profile 等字样都不应出现）
- 复杂工具类堆积（工具类放 core:common）

## 依赖规则
- 允许依赖：Java/Kotlin 标准库、AndroidX 基础（少量）
- 禁止依赖：任何 featu
- re 模块