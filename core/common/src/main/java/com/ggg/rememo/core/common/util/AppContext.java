package com.ggg.rememo.core.common.util;

import android.app.Application;
import android.content.Context;

/**
 * 全局 Application Context 持有类。
 * 在 Application.onCreate() 中初始化后，可在任何位置通过 {@link #get()} 获取 Application 级别的 Context。
 * <p>
 * 使用 Application Context 而非 Activity Context 可以避免内存泄漏风险。
 */
public class AppContext {

    private static Application application;

    private AppContext() {
    }

    /**
     * 初始化全局 Context。应在 Application.onCreate() 中尽早调用。
     *
     * @param app Application 实例
     */
    public static void init(Application app) {
        application = app;
    }

    /**
     * 获取 Application 级别的 Context。
     *
     * @return Application Context，初始化前调用会抛出异常
     */
    public static Context get() {
        if (application == null) {
            throw new IllegalStateException(
                    "AppContext has not been initialized. Call AppContext.init() in Application.onCreate() first.");
        }
        return application.getApplicationContext();
    }

    /**
     * 获取 Application 实例。
     *
     * @return Application 实例，初始化前调用会抛出异常
     */
    public static Application getApp() {
        if (application == null) {
            throw new IllegalStateException(
                    "AppContext has not been initialized. Call AppContext.init() in Application.onCreate() first.");
        }
        return application;
    }
}
