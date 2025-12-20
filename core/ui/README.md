# core:ui

## 目标
沉淀可复用 UI 组件与统一视觉规范，避免每个 feature 自己造轮子。

## 放什么
- theme/colors/dimens/styles
- 通用 UI 组件：LoadingView、EmptyView、ErrorView、Toolbar
- 通用列表基础：BaseAdapter/BaseViewHolder（如果你有）
- 通用对话框/BottomSheet（轻量）

## 不放什么
- 强业务 UI（只服务某个 feature 的复杂组件不要放）
- 页面级逻辑（Presenter/Repository 不应在这里）

## 依赖规则
- 允许依赖：appcompat/material/constraintlayout
- 禁止依赖：feature
