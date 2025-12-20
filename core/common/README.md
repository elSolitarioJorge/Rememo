# core:common

## 目标
提供跨模块共享的“约定与契约”，减少硬编码与重复实现。

## 放什么
- 路由常量 Routes（路径集中管理）
- 公共常量：Intent Keys、Bundle Keys、时间格式等
- 轻量工具类：DateUtils、FormatUtils、StringUtils（避免膨胀）
- 统一的模块间契约（如果不用 ARouter 时的 Navigator 接口也可放这里）

## 不放什么
- 不要把这里做成“万能工具箱”
- 不要放依赖重的实现（网络/数据库/播放器/地图都不该在这里）

## 依赖规则
- 允许依赖：core:base（可选）、少量 AndroidX
- 禁止依赖：feature