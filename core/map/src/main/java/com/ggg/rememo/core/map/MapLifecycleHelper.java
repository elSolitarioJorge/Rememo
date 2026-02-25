package com.ggg.rememo.core.map;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.amap.api.maps.TextureMapView;

/**
 * 将 {@link TextureMapView} 的生命周期方法与 Fragment 自动绑定，
 * 同时处理 Fragment show/hide 模式下的 pause/resume。
 *
 * <p>用法：在 Fragment.onCreateView 中调用 {@link #bindTo(Fragment, TextureMapView, Bundle)}。</p>
 */
public final class MapLifecycleHelper {

    private MapLifecycleHelper() {
        throw new AssertionError("No instances.");
    }

    /**
     * 将 mapView 的生命周期绑定到宿主 Fragment。
     * <ul>
     *   <li>自动转发 onResume / onPause / onDestroy / onSaveInstanceState / onLowMemory</li>
     *   <li>自动处理 Fragment show/hide 切换时的 pause/resume</li>
     * </ul>
     *
     * @param fragment 宿主 Fragment
     * @param mapView  TextureMapView 实例
     * @param savedInstanceState Fragment.onCreateView 收到的 savedInstanceState
     */
    public static void bindTo(@NonNull Fragment fragment,
                              @NonNull TextureMapView mapView,
                              @Nullable Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);

        fragment.getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                if (!fragment.isHidden()) {
                    mapView.onResume();
                }
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                mapView.onPause();
            }

            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {
                mapView.onDestroy();
            }
        });
    }

    /**
     * 在 Fragment.onHiddenChanged 中调用，
     * 当 Fragment 被 hide 时暂停地图渲染，show 时恢复。
     */
    public static void onHiddenChanged(@NonNull TextureMapView mapView, boolean hidden) {
        if (hidden) {
            mapView.onPause();
        } else {
            mapView.onResume();
        }
    }

    /**
     * 在 Fragment.onSaveInstanceState 中调用。
     */
    public static void onSaveInstanceState(@NonNull TextureMapView mapView,
                                           @NonNull Bundle outState) {
        mapView.onSaveInstanceState(outState);
    }
}
