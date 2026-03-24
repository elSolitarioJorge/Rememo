package com.ggg.rememo.core.network;

/**
 * 网络请求回调接口。
 * 统一处理成功和失败两种结果。
 *
 * @param <T> 成功时 data 的数据类型
 */
public interface ApiCallback<T> {

    /**
     * 请求成功回调。
     *
     * @param data 响应中的 data 字段
     */
    void onSuccess(T data);

    /**
     * 请求失败回调。
     *
     * @param message 错误信息（来自接口返回的 message 或网络异常描述）
     */
    void onError(String message);
}
