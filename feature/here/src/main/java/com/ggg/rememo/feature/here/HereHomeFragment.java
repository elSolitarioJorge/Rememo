package com.ggg.rememo.feature.here;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.map.MapLifecycleHelper;
import com.ggg.rememo.feature.here.databinding.FragmentHereHomeBinding;

/**
 * 地图首页 Fragment
 * 功能：
 * 1. 持续定位（2秒间隔）
 * 2. 跟随模式：定位、将视角移动到中心点、定位点依照设备方向旋转、跟随设备移动
 * 3. 拖动/缩放地图退出跟随模式，点击定位按钮恢复
 * 4. 显示蓝点和精度圈
 */
@Route(path = Routes.Here.HOME_FRAGMENT)
public class HereHomeFragment extends Fragment implements AMapLocationListener, LocationSource {
    private static final String TAG = "HereHomeFragment";
    // ========== 常量配置 ==========
    private static final String PREFS_NAME = "here_location_prefs";
    private static final String PREF_LAST_LAT = "last_lat";
    private static final String PREF_LAST_LNG = "last_lng";
    // 防抖阈值：位移小于 2 米时不触发相机动画
    private static final float MIN_MOVE_DISTANCE_METERS = 2.0f;
    // 默认缩放级别
    private static final float DEFAULT_ZOOM_LEVEL = 17f;

    private FragmentHereHomeBinding binding;
    private AMap aMap;
    private AMapLocationClient locationClient;

    // LocationSource 监听器（地图调用 activate() 时注入，用于驱动蓝点渲染）
    private OnLocationChangedListener mLocationChangedListener;

    // 跟随模式：true=镜头跟随用户位置，false=用户手动拖动/缩放地图
    private boolean isFollowing = true;
    // 定位是否已初始化
    private boolean isLocationInitialized = false;
    // 是否已经移动过视角到当前位置（用于区分首次定位和后续定位）
    private boolean hasMovedToCurrentLocation = false;
    // Fragment 是否已销毁
    private boolean isFragmentDestroyed = false;
    // 权限是否被永久拒绝（用户选择了"不再询问"）
    private boolean isPermissionPermanentlyDenied = false;
    // 上一次成功定位的坐标（防抖对比用）
    private LatLng lastLatLng;
    // 最新定位坐标（内存缓存，减少 SharedPreferences 写频率）
    private LatLng latestLocation;

    // 权限请求Launcher
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (isFragmentDestroyed) return;

                Boolean fineLocation = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocation = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                // 优先要求精确定位权限，如果没有则使用模糊定位
                if ((fineLocation != null && fineLocation) || (coarseLocation != null && coarseLocation)) {
                    // 权限授予成功
                    isPermissionPermanentlyDenied = false;
                    initLocationAndStart();
                    isLocationInitialized = true;
                } else {
                    // 权限被拒绝
                    isLocationInitialized = false;

                    // 检测是否被永久拒绝（用户选择了"不再询问"）
                    boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

                    if (!shouldShowRationale) {
                        // 用户选择了"不再询问"，引导去设置页面
                        isPermissionPermanentlyDenied = true;
                        showPermissionDeniedDialog();
                    } else {
                        // 普通拒绝，提示用户可通过定位按钮重新请求
                        Toast.makeText(requireContext(), "定位权限被拒绝，点击定位按钮可重新请求", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 重置 Fragment 销毁状态（Navigation/ViewPager 回退栈复用时需要）
        isFragmentDestroyed = false;

        binding = FragmentHereHomeBinding.inflate(inflater, container, false);
        MapLifecycleHelper.bindTo(this, binding.mapView, savedInstanceState);

        initMap();
        initLocationButton();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 视图创建完成后触发首次权限检查（放在此处而非 onResume，防止权限弹窗死循环）
        checkLocationPermissionAndInit();
    }

    private void initMap() {
        aMap = binding.mapView.getMap();

        // ========== 地图内置 UI 及手势控制 ==========
        aMap.getUiSettings().setScaleControlsEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setZoomGesturesEnabled(true);
        aMap.getUiSettings().setTiltGesturesEnabled(false);

        // ========== 读取缓存坐标，消除首次加载的"北京闪烁" ==========
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.contains(PREF_LAST_LAT) && prefs.contains(PREF_LAST_LNG)) {
            float cachedLat = prefs.getFloat(PREF_LAST_LAT, 0f);
            float cachedLng = prefs.getFloat(PREF_LAST_LNG, 0f);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cachedLat, cachedLng), DEFAULT_ZOOM_LEVEL));
        }

        // ========== 蓝点样式配置 ==========
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        myLocationStyle.radiusFillColor(Color.argb(50, 0, 122, 255));
        myLocationStyle.strokeWidth(0f);
        aMap.setMyLocationStyle(myLocationStyle);

        // ========== 注册自定义 LocationSource ==========
        // 设置定位源，为地图提供定位数据，保证蓝点渲染
        aMap.setLocationSource(this);

        // ========== 地图触摸监听 ==========
        aMap.setOnMapTouchListener(event -> {
            isFollowing = false;
            binding.btnMyLocation.setImageResource(R.drawable.ic_location_unfollow);
        });
    }

    private void initLocationButton() {
        binding.btnMyLocation.setOnClickListener(v -> {
            // 点击定位按钮：恢复跟随模式
            isFollowing = true;
            binding.btnMyLocation.setImageResource(R.drawable.ic_location_follow);

            // 重置标记，让下次定位时立即移动视角（无动画）
            hasMovedToCurrentLocation = false;

            if (!isLocationInitialized) {
                // 定位未初始化（权限被拒绝过），点击按钮重新触发权限请求
                checkLocationPermissionAndInit();
            } else if (locationClient != null) {
                locationClient.startLocation();
            }
        });
    }

    /**
     * 检查定位权限并初始化定位
     */
    private void checkLocationPermissionAndInit() {
        if (isLocationInitialized) {
            // 定位已经在运行，无需重复启动
            return;
        }

        if (hasLocationPermission()) {
            // 用户在设置中手动开启了权限
            isPermissionPermanentlyDenied = false;
            initLocationAndStart();
            isLocationInitialized = true;
        } else if (isPermissionPermanentlyDenied) {
            // 权限被永久拒绝，不再请求，引导用户去设置
            showPermissionDeniedDialog();
        } else {
            // 首次请求或普通拒绝，正常请求权限
            requestLocationPermission();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 检查是否具有精确定位权限
     */
    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    /**
     * 显示权限被永久拒绝的对话框，引导用户去设置页面
     */
    private void showPermissionDeniedDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("需要定位权限")
                .setMessage("定位权限被永久拒绝，请手动在设置中开启。\n\n点击\"去设置\"后，找到权限 → 位置信息，开启即可。")
                .setPositiveButton("去设置", (dialog, which) -> openAppSettings())
                .setNegativeButton("取消", (dialog, which) ->
                        Toast.makeText(requireContext(), "无法使用定位功能", Toast.LENGTH_SHORT).show())
                .setCancelable(false)
                .show();
    }

    /**
     * 打开应用设置页面，让用户手动开启权限
     */
    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    /**
     * 初始化定位客户端并开启蓝点图层
     * 调用 aMap.setMyLocationEnabled(true) 会触发 LocationSource.activate()，
     * activate() 内部启动 locationClient，形成完整的数据流。
     */
    private void initLocationAndStart() {
        if (locationClient != null) {
            // 已初始化，重新开启蓝点即可（触发 activate 重启定位）
            aMap.setMyLocationEnabled(true);
            return;
        }
        try {
            Context appContext = requireContext().getApplicationContext();
            locationClient = new AMapLocationClient(appContext);
            locationClient.setLocationListener(this);

            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(hasFineLocationPermission()
                    ? AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
                    : AMapLocationClientOption.AMapLocationMode.Battery_Saving);
            option.setNeedAddress(false);
            option.setInterval(2000);
            locationClient.setLocationOption(option);

            // 开启蓝点图层 → 触发 LocationSource.activate() → 启动定位
            aMap.setMyLocationEnabled(true);
            Log.d(TAG, "定位已初始化");
        } catch (Exception e) {
            Log.e(TAG, "初始化定位失败", e);
            Toast.makeText(requireContext(), "定位初始化失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
            Log.d(TAG, "定位已暂停");
        }
    }

    // ==================== AMapLocationListener ====================

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (isFragmentDestroyed || aMap == null) return;

        if (aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {

            // 将位置数据喂给地图，驱动蓝点渲染（核心步骤，缺少此步蓝点不会出现）
            if (mLocationChangedListener != null) {
                android.location.Location loc = new android.location.Location("AMap");
                loc.setLatitude(aMapLocation.getLatitude());
                loc.setLongitude(aMapLocation.getLongitude());
                loc.setAccuracy(aMapLocation.getAccuracy());
                loc.setBearing(aMapLocation.getBearing());
                loc.setSpeed(aMapLocation.getSpeed());
                loc.setTime(aMapLocation.getTime());
                mLocationChangedListener.onLocationChanged(loc);
            }

            // 内存缓存最新坐标（减少 SharedPreferences 写频率，只在 onPause/onDestroyView 写入）
            latestLocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());

            // 跟随模式下移动镜头（防抖：位移 < 2m 不触发动画）
            if (isFollowing) {
                LatLng latLng = latestLocation;
                boolean shouldMove = lastLatLng == null
                        || AMapUtils.calculateLineDistance(lastLatLng, latLng) > MIN_MOVE_DISTANCE_METERS;

                if (shouldMove) {
                    lastLatLng = latLng;
                    if (!hasMovedToCurrentLocation) {
                        // 首次定位：立即跳转（无动画）
                        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL));
                        hasMovedToCurrentLocation = true;
                    } else {
                        // 后续定位：平滑动画
                        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL));
                    }
                }
            }
        } else {
            Log.w(TAG, "定位失败: " + aMapLocation.getErrorCode() + ", " + aMapLocation.getErrorInfo());
        }
    }

    // ==================== LocationSource 接口 ====================

    /**
     * 地图调用 setMyLocationEnabled(true) 后触发。
     * 保存监听器并启动定位，后续通过 mLocationChangedListener 喂数据给蓝点。
     */
    @Override
    public void activate(OnLocationChangedListener listener) {
        mLocationChangedListener = listener;
        if (locationClient != null) {
            locationClient.startLocation();
        }
    }

    /**
     * 地图调用 setMyLocationEnabled(false) 后触发，清理监听器并停止定位。
     */
    @Override
    public void deactivate() {
        mLocationChangedListener = null;
        // 停止定位，防止图层关闭后定位还在后台运行
        if (locationClient != null) {
            locationClient.stopLocation();
        }
    }

    // ==================== Fragment 生命周期 ====================

    @Override
    public void onResume() {
        super.onResume();
        if (isLocationInitialized) {
            // 已初始化，从后台恢复时直接重启定位
            if (locationClient != null) {
                locationClient.startLocation();
            }
        } else if (hasLocationPermission()) {
            // 从设置页返回后权限已开启，立即初始化（不重新弹权限框）
            initLocationAndStart();
            isLocationInitialized = true;
        }
        // 其他情况（未请求 / 已普通拒绝）：等待用户点击定位按钮主动触发
    }

    @Override
    public void onPause() {
        super.onPause();
        // 将内存中的最新坐标写入 SharedPreferences（只写一次，避免高频 IO）
        saveLocationToPrefs();
        pauseLocation();
    }

    /**
     * 将缓存的坐标写入 SharedPreferences
     */
    private void saveLocationToPrefs() {
        if (latestLocation != null) {
            requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putFloat(PREF_LAST_LAT, (float) latestLocation.latitude)
                    .putFloat(PREF_LAST_LNG, (float) latestLocation.longitude)
                    .apply();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (binding != null) {
            MapLifecycleHelper.onHiddenChanged(binding.mapView, hidden);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            MapLifecycleHelper.onSaveInstanceState(binding.mapView, outState);
        }
    }

    @Override
    public void onDestroyView() {
        isFragmentDestroyed = true;

        // 解除所有地图监听器，防止内存泄漏
        if (aMap != null) {
            aMap.setMyLocationEnabled(false);
            aMap.setLocationSource(null);
            aMap.setOnMapTouchListener(null);  // 新增：解除触摸监听器
        }

        if (locationClient != null) {
            locationClient.onDestroy();
            locationClient = null;
        }

        super.onDestroyView();
        binding = null;
    }
}
