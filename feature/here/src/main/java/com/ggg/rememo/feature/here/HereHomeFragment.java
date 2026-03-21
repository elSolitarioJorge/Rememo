package com.ggg.rememo.feature.here;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.core.map.MapLifecycleHelper;
import com.ggg.rememo.feature.here.databinding.FragmentHereHomeBinding;
import com.tencent.mmkv.MMKV;

import java.util.List;


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
    private static final String MMKV_ID = "here_location";  // MMKV 实例 ID
    private static final String PREF_LAST_LAT = "last_lat";
    private static final String PREF_LAST_LNG = "last_lng";
    // 默认缩放级别
    private static final float DEFAULT_ZOOM_LEVEL = 16f;

    private MMKV mmkv;
    private FragmentHereHomeBinding binding;
    private AMap aMap;
    private AMapLocationClient locationClient;
    private MemoryPointRepository memoryPointRepository;

    // LocationSource 监听器（地图调用 activate() 时注入，用于驱动蓝点渲染）
    private OnLocationChangedListener mLocationChangedListener;

    // 跟随模式：true=镜头跟随用户位置，false=用户手动拖动/缩放地图
    private boolean isFollowing = true;
    // 定位是否已初始化
    private boolean isLocationInitialized = false;
    // 权限是否被永久拒绝（用户选择了"不再询问"）
    private boolean isPermissionPermanentlyDenied = false;
    // 最新定位坐标（内存缓存，减少 MMKV 写频率）
    private LatLng latestLocation;

    // 权限请求Launcher
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                // 使用 isAdded() 检查 Fragment 是否仍然附加到 Activity
                if (!isAdded()) return;

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
        binding = FragmentHereHomeBinding.inflate(inflater, container, false);
        MapLifecycleHelper.bindTo(this, binding.mapView, savedInstanceState);

        initMap();
        initLocationButton();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化 Repository
        memoryPointRepository = new MemoryPointRepository(requireContext());
        // 视图创建完成后触发首次权限检查
        checkLocationPermissionAndInit();
    }

    // 获取 MMKV 实例（懒加载）
    private MMKV getMMKV() {
        if (mmkv == null) {
            mmkv = MMKV.mmkvWithID(MMKV_ID);
        }
        return mmkv;
    }

    private void initMap() {
        aMap = binding.mapView.getMap();

        // ========== 地图内置 UI 及手势控制 ==========
        aMap.getUiSettings().setScaleControlsEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setZoomGesturesEnabled(true);
        aMap.getUiSettings().setTiltGesturesEnabled(false);

        // ========== 读取缓存坐标 ==========
        if (getMMKV().containsKey(PREF_LAST_LAT) && getMMKV().containsKey(PREF_LAST_LNG)) {
            double cachedLat = getMMKV().decodeDouble(PREF_LAST_LAT, 34.0);
            double cachedLng = getMMKV().decodeDouble(PREF_LAST_LNG, 108.0);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(cachedLat, cachedLng), DEFAULT_ZOOM_LEVEL));
        } else {
            aMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
        }

        // ========== 蓝点样式配置 ==========
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        myLocationStyle.strokeWidth(0f);
        aMap.setMyLocationStyle(myLocationStyle);

        // ========== 注册自定义 LocationSource ==========
        // 设置定位源，为地图提供定位数据，保证蓝点渲染
        aMap.setLocationSource(this);

        // ========== 地图触摸监听 ==========
        aMap.setOnMapTouchListener(event -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && isFollowing) {  // 避免高频重复调用UI更新
                setFollowingMode(false);
            }
        });

        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                MemoryPoint point = (MemoryPoint) marker.getObject();
                if (point != null) {
                    // 让地图中心平滑移动到点击的 Marker 处
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
                    // 弹出底部卡片
                    showMemoryPointBottomSheet(point);
                }
                return true;
            }
        });
    }

    /**
     * 切换跟随模式
     * @param follow true=开启跟随（SDK内置平滑移动），false=关闭跟随（蓝点旋转但不移动视角）
     */
    private void setFollowingMode(boolean follow) {
        isFollowing = follow;
        // 异步回调中需要判空，防止 onDestroyView 后回调到达
        if (binding != null) {
            binding.btnMyLocation.setImageResource(follow ? R.drawable.ic_location_follow : R.drawable.ic_location_unfollow);
        }
        if (aMap != null && aMap.getMyLocationStyle() != null) {
            MyLocationStyle style = aMap.getMyLocationStyle();
            style.myLocationType(
                    follow ? MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE
                           : MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            aMap.setMyLocationStyle(style);
        }
    }

    private void initLocationButton() {
        binding.btnMyLocation.setOnClickListener(v -> {
            // 点击定位按钮：开启跟随模式（SDK 内置平滑移动）
            setFollowingMode(true);

            // 立即跳转一次到当前位置，确保缩放倍数正确
            if (latestLocation != null) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latestLocation, DEFAULT_ZOOM_LEVEL));
            }

            if (!isLocationInitialized) {
                // 定位未初始化（权限被拒绝过），点击按钮重新触发权限请求
                checkLocationPermissionAndInit();
            } else if (locationClient != null) {
                locationClient.startLocation();
            }
        });
    }

    // ========== 记忆点相关方法 ==========
    private void loadMemoryPoints() {
        if (memoryPointRepository == null || aMap == null) {
            return;
        }

        memoryPointRepository.getAll(new MemoryPointRepository.Callback<List<MemoryPoint>>() {
            @Override
            public void onSuccess(List<MemoryPoint> result) {
                if (!isAdded() || aMap == null) {
                    return;
                }
                // 在主线程更新 UI
                requireActivity().runOnUiThread(() -> {
                    // 清除现有标记
                    aMap.clear();
                    // 添加新的标记
                    for (MemoryPoint point : result) {
                        addMarkerForMemoryPoint(point);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "加载记忆点失败: " + e.getMessage());
            }
        });
    }

    private void addMarkerForMemoryPoint(MemoryPoint point) {
        if (aMap == null || point == null) {
            return;
        }

        LatLng position = new LatLng(point.getLatitude(), point.getLongitude());

        View markerView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_custom_marker, null);

        // 将 View 转化为 BitmapDescriptor 并添加到地图上
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(markerView);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(point.getPointName())
                .anchor(0.5f, 1.0f) // 重要：设置锚点为底部中心，这样缩放地图时 Marker 位置才准
                .icon(descriptor)
                .zIndex(1.0f);

        Marker marker = aMap.addMarker(markerOptions);
        if (marker != null) {
            marker.setObject(point); // 保存对象供点击时使用
            boolean existImage = point.getCoverImageUrl() != null && !point.getCoverImageUrl().isEmpty();
            int radiusPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

            RequestOptions options = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .override(200, 200)       // 限制 Bitmap 大小，防止 OOM 或超过 Canvas 限制
                    .disallowHardwareConfig(); // 禁用硬件加速，确保能在软件 Canvas 上绘制

            Glide.with(this)
                    .asBitmap()
                    .load(existImage ? point.getCoverImageUrl() : R.drawable.pic_old)
                    .apply(options)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            View finalMarkerView = LayoutInflater.from(getContext()).inflate(R.layout.layout_custom_marker, null);
                            ImageView finalIvPic = finalMarkerView.findViewById(R.id.iv_marker_pic);

                            // 将裁切完美的 Bitmap 贴给新的 ImageView
                            finalIvPic.setImageBitmap(resource);

                            // 从 View 生成 Marker 的 Descriptor
                            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(finalMarkerView);
                            marker.setIcon(descriptor);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        }
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
        Log.d(TAG, "initLocationAndStart: hasFineLocation=" + hasFineLocationPermission() + ", locationClient=" + (locationClient != null));

        if (locationClient != null) {
            // 已初始化，重新开启蓝点即可（触发 activate 重启定位）
            aMap.setMyLocationEnabled(true);
            return;
        }
        try {
            Context appContext = requireContext().getApplicationContext();
            Log.d(TAG, "Creating AMapLocationClient...");
            locationClient = new AMapLocationClient(appContext);
            locationClient.setLocationListener(this);

            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setNeedAddress(false);
            if (hasFineLocationPermission()) {
                // 拥有精确定位：开启 2 秒连续定位（实现平滑的实时跟随）
                option.setOnceLocation(false);
                option.setInterval(2000);
            } else {
                // 只有模糊定位时，强制设为单次定位
                option.setOnceLocation(true);
            }
            locationClient.setLocationOption(option);

            // 开启蓝点图层 → 触发 LocationSource.activate() → 启动定位
            aMap.setMyLocationEnabled(true);
            Log.d(TAG, "定位已初始化, hasFineLocation=" + hasFineLocationPermission());
        } catch (Exception e) {
            Log.e(TAG, "初始化定位失败: " + e.getMessage(), e);
            Toast.makeText(requireContext(), "定位初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ==================== AMapLocationListener ====================

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (!isAdded() || aMap == null || aMapLocation == null) return;

        if (aMapLocation.getErrorCode() != AMapLocation.LOCATION_SUCCESS) {
            Log.w(TAG, "定位失败: " + aMapLocation.getErrorCode() + ", " + aMapLocation.getErrorInfo());
            return;
        }

        // 将位置数据喂给地图，驱动蓝点渲染（核心步骤，缺少此步蓝点不会出现）
        // AMapLocation 继承自 android.location.Location，可直接传递
        if (mLocationChangedListener != null) {
            mLocationChangedListener.onLocationChanged(aMapLocation);
        }

        // 内存缓存最新坐标（减少 MMKV 写频率，只在 onPause/onDestroyView 写入）
        latestLocation = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
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
     * 地图调用 setMyLocationEnabled(false) 后触发。
     * 只解绑监听器，定位的停止由 Fragment 生命周期（onPause）管控。
     */
    @Override
    public void deactivate() {
        mLocationChangedListener = null;
    }

    // ==================== 底部卡片弹窗 ====================

    /**
     * 展示 MemoryPoint 的底部详情卡片
     */
    private void showMemoryPointBottomSheet(MemoryPoint point) {
        MemoryPointBottomSheetFragment bottomSheet =
                MemoryPointBottomSheetFragment.newInstance(point);
        bottomSheet.show(getChildFragmentManager(), "MemoryPointBottomSheet");
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

        // 加载记忆点
        loadMemoryPoints();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 将内存中的最新坐标写入 MMKV（只写一次，避免高频 IO）
        saveLocationToPrefs();
        if (locationClient != null) {
            locationClient.stopLocation();
        }
    }

    /**
     * 将缓存的坐标写入 MMKV
     */
    private void saveLocationToPrefs() {
        if (latestLocation != null) {
            getMMKV().encode(PREF_LAST_LAT, latestLocation.latitude);
            getMMKV().encode(PREF_LAST_LNG, latestLocation.longitude);
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
        // 解除所有地图监听器，防止内存泄漏
        if (aMap != null) {
            aMap.setMyLocationEnabled(false);
            aMap.setLocationSource(null);
            aMap.setOnMapTouchListener(null);  // 解除触摸监听器
        }

        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.unRegisterLocationListener(this);
        }

        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.onDestroy();
            locationClient = null;
        }
    }
}
