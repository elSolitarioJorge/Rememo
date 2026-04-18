package com.ggg.rememo.core.network.event;

/**
 * Token 失效事件。
 * 当拦截器检测到 401 时通过 EventBus 发出，
 * 订阅者收到后跳转登录页。
 */
public class TokenExpiredEvent {
    // 空类即可，仅作为事件标记，不携带额外数据
    // 如需携带跳转来源等信息，可在此添加字段
}