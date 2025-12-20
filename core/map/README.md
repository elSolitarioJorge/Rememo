# core:map

## 目标
统一地图与定位相关能力，屏蔽 SDK 差异，对外提供稳定接口。

## 放什么
- MapSdk 初始化（只在这里初始化）
- 定位能力封装：LocationProvider
- 逆地理编码封装（如 Geocoder/SDK）
- Marker 管理工具（后期可做聚合）

## 不放什么
- 不放业务交互（长按发布、点击跳详情放 feature:here）
- 不放业务数据模型（Place/Memory 等放 feature）

## 依赖规则
- 允许依赖：core:common/core:base
- 禁止依赖：feature