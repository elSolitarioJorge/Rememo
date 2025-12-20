# core:media

## 目标
统一多媒体能力（图片/音频），对外暴露简单接口，内部可替换实现。

## 放什么
- ImageLoader 封装（内部可用 Glide）
- 图片选择/压缩封装（如果你用）
- 录音/播放封装：AudioRecorder、AudioPlayer（建议提供状态回调）

## 不放什么
- 不放发布页/详情页等业务 UI
- 不放具体业务流程（例如“发布时必须先压缩”这种逻辑放 feature）

## 依赖规则
- 允许依赖：core:common/core:base、少量 AndroidX
- 禁止依赖：feature