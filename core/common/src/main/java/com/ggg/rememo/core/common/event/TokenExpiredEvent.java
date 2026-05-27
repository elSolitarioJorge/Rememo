package com.ggg.rememo.core.common.event;

/**
 * Token 失效事件。
 * 网络层检测到已鉴权请求返回 401 时发送，宿主层统一处理退登和登录页跳转。
 */
public class TokenExpiredEvent {
}
