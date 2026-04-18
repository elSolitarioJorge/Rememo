package com.ggg.rememo;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.alibaba.android.arouter.BuildConfig;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.maps.MapsInitializer;
import com.ggg.rememo.core.common.util.AppContext;
import com.tencent.mmkv.MMKV;


public class RememoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 开启全局暗色模式，使状态栏的文字和图标改为浅色（白色）
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        AppContext.init(this);

        // 初始化 MMKV
        MMKV.initialize(this);

        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);
        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
    }
}
