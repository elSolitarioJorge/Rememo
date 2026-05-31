package com.ggg.rememo.core.network;

/**
 * 网络层统一错误类型。
 */
public enum NetworkErrorType {
    TOKEN_EXPIRED,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    SERVER_ERROR,
    TIMEOUT,
    DNS_ERROR,
    CONNECT_ERROR,
    PARSE_ERROR,
    BUSINESS_ERROR,
    UNKNOWN
}
