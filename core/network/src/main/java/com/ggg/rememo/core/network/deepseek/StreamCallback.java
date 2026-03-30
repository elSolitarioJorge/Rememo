package com.ggg.rememo.core.network.deepseek;

/**
 * 流式响应回调接口
 */
public interface StreamCallback {
    /**
     * 收到增量内容
     * @param content 增量文本
     */
    void onContent(String content);

    /**
     * 流式响应完成
     * @param fullContent 完整响应内容
     */
    void onComplete(String fullContent);

    /**
     * 发生错误
     * @param error 错误信息
     */
    void onError(String error);
}
