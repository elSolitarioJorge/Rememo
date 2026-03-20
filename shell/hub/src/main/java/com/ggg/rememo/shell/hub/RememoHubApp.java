package com.ggg.rememo.shell.hub;

import android.app.Application;

import com.alibaba.android.arouter.BuildConfig;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.maps.MapsInitializer;
import com.tencent.mmkv.MMKV;

public class RememoHubApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化 MMKV
        MMKV.initialize(this);

        // 高德地图隐私合规初始化
        MapsInitializer.updatePrivacyShow(this, true, true);
        MapsInitializer.updatePrivacyAgree(this, true);

        if (BuildConfig.DEBUG) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
    }
}

