# core:network

## 目标
统一网络能力与配置，避免 feature 各自 new Retrofit/OkHttp 造成分裂。

## 放什么
- OkHttpClient/Retrofit 的创建与单例管理
- 通用拦截器：日志、token、header
- 统一错误映射：HttpException/IOException -> AppError
- 通用 API 返回结构的解析（如果你后端固定）

## 不放什么
- 不要放具体业务 API 接口（建议放在各 feature:data 下）
- 不要放 UI 逻辑

## 依赖规则
- 允许依赖：core:common/core:base
- 禁止依赖：feature