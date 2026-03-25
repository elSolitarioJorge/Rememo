package com.ggg.rememo.core.common.util;

import com.tencent.mmkv.MMKV;

/**
 * Token 管理器。
 * 使用 MMKV 存储登录凭证（Token、userId、过期时间）。
 */
public class TokenManager {

    private static final String MMKV_ID = "auth_mmkv";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_EXPIRES_AT = "auth_expires_at";
    private static final String KEY_USER_ID = "auth_user_id";
    private static MMKV mmkv;
    private static MMKV getMMKV() {
        if (mmkv == null) {
            mmkv = MMKV.mmkvWithID(MMKV_ID);
        }
        return mmkv;
    }

    /**
     * 保存登录凭证。
     *
     * @param token    JWT Token
     * @param expiresAt Token 过期时间戳（毫秒）
     * @param userId   用户 ID
     */
    public static void saveToken(String token, long expiresAt, String userId) {
        getMMKV().encode(KEY_TOKEN, token);
        getMMKV().encode(KEY_EXPIRES_AT, expiresAt);
        getMMKV().encode(KEY_USER_ID, userId);
    }

    /**
     * 获取保存的 Token。
     *
     * @return Token 字符串，如果未登录或已过期则返回 null
     */
    public static String getToken() {
        if (!isLoggedIn()) {
            return null;
        }
        return getMMKV().decodeString(KEY_TOKEN, null);
    }

    /**
     * 获取当前登录用户的 ID。
     *
     * @return userId，未登录或已过期返回 null
     */
    public static String getUserId() {
        if (!isLoggedIn()) {
            return null;
        }
        return getMMKV().decodeString(KEY_USER_ID, null);
    }

    /**
     * 判断是否已登录且 Token 未过期。
     *
     * @return true 表示已登录且 Token 有效
     */
    public static boolean isLoggedIn() {
        long expiresAt = getMMKV().decodeLong(KEY_EXPIRES_AT, 0L);
        String token = getMMKV().decodeString(KEY_TOKEN, null);
        return token != null && !token.isEmpty() && expiresAt > System.currentTimeMillis();
    }

    /**
     * 清除登录凭证（退出登录）。
     */
    public static void logout() {
        getMMKV().removeValueForKey(KEY_TOKEN);
        getMMKV().removeValueForKey(KEY_EXPIRES_AT);
        getMMKV().removeValueForKey(KEY_USER_ID);
    }
}
