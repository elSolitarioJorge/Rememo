package com.ggg.rememo.session;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.event.TokenExpiredEvent;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.common.util.TokenManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 宿主层统一处理 token 过期事件。
 */
public final class TokenExpiredHandler {

    private static final AtomicBoolean redirectingToLogin = new AtomicBoolean(false);
    private static TokenExpiredHandler instance;

    private final Application application;

    private TokenExpiredHandler(Application application) {
        this.application = application;
    }

    public static void init(Application application) {
        if (application == null) {
            return;
        }

        synchronized (TokenExpiredHandler.class) {
            if (instance == null) {
                instance = new TokenExpiredHandler(application);
            }

            EventBus eventBus = EventBus.getDefault();
            if (!eventBus.isRegistered(instance)) {
                eventBus.register(instance);
            }
        }
    }

    public static void markLoginRecovered() {
        redirectingToLogin.set(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpired(TokenExpiredEvent event) {
        if (!redirectingToLogin.compareAndSet(false, true)) {
            return;
        }

        TokenManager.logout();
        Toast.makeText(application, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show();
        ARouter.getInstance()
                .build(Routes.Auth.LOGIN)
                .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .navigation(application);
    }
}
