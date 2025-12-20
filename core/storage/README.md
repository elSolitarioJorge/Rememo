# core:storage

## 目标
统一本地存储能力，提供一致的数据读写方式（偏好/缓存/数据库）。

## 放什么
- SharedPreferences 封装（建议提供接口：KeyValueStore）
- 用户/游客相关本地状态（如匿名ID、上次位置、开关配置）
- 如果使用 Room：Database 初始化与通用 DAO 基建（尽量保持通用）

## 不放什么
- 不要放强业务字段/表结构（业务实体尽量在 feature）
- 不要放网络逻辑

## 依赖规则
- 允许依赖：core:common/core:base
- 禁止依赖：feature