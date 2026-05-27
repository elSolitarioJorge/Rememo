package com.ggg.rememo.core.ui.auth;

import android.app.Activity;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.common.util.TokenManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * 登录守卫：未登录时用轻量弹窗解释功能边界，避免业务模块散落跳登录逻辑。
 */
public final class LoginRequiredPrompt {

    private LoginRequiredPrompt() {
    }

    public static void requireLogin(Activity activity, String scene, Runnable onLoggedIn) {
        requireLogin(activity, scene, onLoggedIn, null);
    }

    public static void requireLogin(Activity activity, String scene, Runnable onLoggedIn, Runnable onCancel) {
        if (TokenManager.isLoggedIn()) {
            if (onLoggedIn != null) {
                onLoggedIn.run();
            }
            return;
        }

        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            if (onCancel != null) {
                onCancel.run();
            }
            return;
        }

        new MaterialAlertDialogBuilder(activity)
                .setTitle("需要登录")
                .setMessage(buildMessage(scene))
                .setPositiveButton("去登录", (dialog, which) ->
                        ARouter.getInstance()
                                .build(Routes.Auth.LOGIN)
                                .navigation(activity))
                .setNegativeButton("暂不", (dialog, which) -> {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .setOnCancelListener(dialog -> {
                    if (onCancel != null) {
                        onCancel.run();
                    }
                })
                .show();
    }

    public static void navigateToLogin(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        ARouter.getInstance()
                .build(Routes.Auth.LOGIN)
                .navigation(activity);
    }

    private static String buildMessage(String scene) {
        if (scene == null || scene.trim().isEmpty()) {
            return "登录后即可继续使用该功能。";
        }
        String trimmed = scene.trim();
        if (trimmed.contains("登录后") || trimmed.contains("需要登录")) {
            return trimmed;
        }
        return trimmed + "需要登录后使用。";
    }
}
