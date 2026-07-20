package com.ggg.rememo.feature.here;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MyLocationStyle;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.base.BaseFragment;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.map.MapLifecycleHelper;
import com.ggg.rememo.feature.here.chat.AiChatFragment;
import com.ggg.rememo.feature.here.contract.HereHomeContract;
import com.ggg.rememo.feature.here.data.HereRepository;
import com.ggg.rememo.feature.here.databinding.FragmentHereHomeBinding;
import com.ggg.rememo.feature.here.marker.MemoryPointMarkerRenderer;
import com.ggg.rememo.feature.here.marker.RenderStats;
import com.ggg.rememo.feature.here.presenter.HereHomePresenter;

import java.util.List;

@Route(path = Routes.Here.HOME_FRAGMENT)
public class HereHomeFragment extends BaseFragment<
        FragmentHereHomeBinding,
        HereHomeContract.View,
        HereHomePresenter>
        implements HereHomeContract.View, AMapLocationListener, LocationSource {

    private static final String TAG = "HereHomeFragment";
    private static final float DEFAULT_ZOOM_LEVEL = 16f;
    private AMap aMap;
    private AMapLocationClient locationClient;

    private OnLocationChangedListener mLocationChangedListener;
    private boolean isLocationInitialized = false;
    private LatLng latestLocation;

    // 动画管理，防止内存泄漏
    private ObjectAnimator scanAnim;
    private MemoryPointMarkerRenderer markerRenderer;

    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (!isAdded()) return;

                Boolean fineLocation = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocation = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if ((fineLocation != null && fineLocation) || (coarseLocation != null && coarseLocation)) {
                    presenter.onPermissionResult(true);
                    initLocationAndStart();
                    isLocationInitialized = true;
                } else {
                    isLocationInitialized = false;
                    presenter.onPermissionResult(false);

                    boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                            requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

                    if (!shouldShowRationale) {
                        showPermissionDeniedDialog();
                    } else {
                        Toast.makeText(requireContext(), "定位权限被拒绝，点击定位按钮可重新请求", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected FragmentHereHomeBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHereHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected HereHomePresenter createPresenter() {
        return new HereHomePresenter(new HereRepository());
    }

    @Override
    protected HereHomeContract.View getViewContract() {
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        MapLifecycleHelper.bindTo(this, getBinding().mapView, savedInstanceState);
        return view;
    }

    @Override
    protected void initView() {
        initMap();
        initListeners();
        startScanLineAnimation();
        startARGlowAnimation();
    }

    @Override
    protected void initData() {
        presenter.initLocation();
        presenter.checkLocationPermission();
    }

    private void initMap() {
        aMap = getBinding().mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NIGHT);

        aMap.getUiSettings().setScaleControlsEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);
        aMap.getUiSettings().setZoomGesturesEnabled(true);
        aMap.getUiSettings().setTiltGesturesEnabled(false);

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.strokeColor(Color.TRANSPARENT);
        myLocationStyle.radiusFillColor(Color.TRANSPARENT);
        myLocationStyle.strokeWidth(0f);
        myLocationStyle.setZIndex(100);
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.icon_my_location));
        aMap.setMyLocationStyle(myLocationStyle);

        aMap.setLocationSource(this);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM_LEVEL));
        markerRenderer = new MemoryPointMarkerRenderer(
                aMap,
                Glide.with(this),
                LayoutInflater.from(requireContext()),
                getResources()
        );

        aMap.setOnMapTouchListener(event -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && presenter.isFollowing()) {
                presenter.onMapTouched();
            }
        });

        aMap.setOnMarkerClickListener(marker -> {
            Object markerData = marker.getObject();
            if (markerData instanceof MemoryPoint) {
                MemoryPoint point = (MemoryPoint) markerData;
                if (markerRenderer != null) {
                    markerRenderer.select(marker);
                }
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
                presenter.onMarkerClicked(point);
            }
            return true;
        });
    }

    private void initListeners() {
        getBinding().btnMyLocation.setOnClickListener(v -> {
            presenter.onLocationButtonClicked();
            if (latestLocation != null) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latestLocation, DEFAULT_ZOOM_LEVEL));
            }
            if (!isLocationInitialized) {
                presenter.checkLocationPermission();
            } else if (locationClient != null) {
                locationClient.startLocation();
            }
        });

        getBinding().btnAiLab.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            android.R.anim.fade_in, android.R.anim.fade_out,
                            android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(android.R.id.content, AiChatFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        getBinding().btnArLab.setOnClickListener(v ->
                ARouter.getInstance()
                        .build(Routes.Ar.TIME_LENS)
                        .navigation()
        );
    }

    private void initLocationAndStart() {
        if (locationClient != null) {
            aMap.setMyLocationEnabled(true);
            return;
        }
        try {
            Context appContext = requireContext().getApplicationContext();
            locationClient = new AMapLocationClient(appContext);
            locationClient.setLocationListener(this);

            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setNeedAddress(true);
            if (hasFineLocationPermission()) {
                option.setOnceLocation(false);
                option.setInterval(2000);
            } else {
                option.setOnceLocation(true);
            }
            locationClient.setLocationOption(option);

            aMap.setMyLocationEnabled(true);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "定位初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // ==================== HereContract.View 实现 ====================

    @Override
    public void showMemoryPoints(List<MemoryPoint> points) {
        renderMemoryPoints(points);
    }

    @Override
    public void refreshMemoryPoints(List<MemoryPoint> points) {
        renderMemoryPoints(points);
    }

    private void renderMemoryPoints(List<MemoryPoint> points) {
        if (markerRenderer == null) {
            return;
        }
        RenderStats stats = markerRenderer.render(points);
        Log.d(TAG, stats.toLogString());
    }

    @Override
    public void showLocationError(String message) {
        Toast.makeText(requireContext(), "定位失败: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationReceived(AMapLocation location) {
        latestLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void requestLocationPermission() {
        locationPermissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @Override
    public void showPermissionDeniedDialog() {
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("需要定位权限")
                .setMessage("定位权限被永久拒绝，请手动在设置中开启。\n\n点击\"去设置\"后，找到权限 → 位置信息，开启即可。")
                .setPositiveButton("去设置", (dialog, which) -> openAppSettings())
                .setNegativeButton("取消", (dialog, which) ->
                        Toast.makeText(requireContext(), "无法使用定位功能", Toast.LENGTH_SHORT).show())
                .setCancelable(false)
                .show();
    }

    @Override
    public void updateFollowModeIcon(boolean isFollowing) {
        getBinding().btnMyLocation.setImageResource(isFollowing ? R.drawable.ic_location_follow : R.drawable.ic_location_unfollow);
        if (aMap != null && aMap.getMyLocationStyle() != null) {
            MyLocationStyle style = aMap.getMyLocationStyle();
            style.myLocationType(
                    isFollowing ? MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE
                           : MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            aMap.setMyLocationStyle(style);
        }
    }

    @Override
    public void moveCameraToLocation(double lat, double lng, boolean animate) {
        if (aMap == null) return;
        if (animate) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM_LEVEL));
        } else {
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), DEFAULT_ZOOM_LEVEL));
        }
    }

    @Override
    public void showMemoryPointBottomSheet(MemoryPoint point) {
        MemoryPointBottomSheetFragment bottomSheet = MemoryPointBottomSheetFragment.newInstance(point);
        bottomSheet.show(getChildFragmentManager(), "MemoryPointBottomSheet");
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // ==================== AMapLocationListener ====================

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (!isAdded() || aMap == null || aMapLocation == null) return;
        presenter.onLocationChanged(aMapLocation);

        if (mLocationChangedListener != null) {
            mLocationChangedListener.onLocationChanged(aMapLocation);
        }

        updateHudWithRealData(aMapLocation);
    }

    // ==================== LocationSource 接口 ====================

    @Override
    public void activate(OnLocationChangedListener listener) {
        mLocationChangedListener = listener;
        if (locationClient != null) {
            locationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {
        mLocationChangedListener = null;
    }

    // ==================== 动画 ====================

    /**
     * 启动扫描线动画 (从上往下无限扫描)
     */
    private void startScanLineAnimation() {
        int height = getResources().getDisplayMetrics().heightPixels;
        scanAnim = ObjectAnimator.ofFloat(getBinding().viewScanLine, "translationY", -100f, height + 100f);
        scanAnim.setDuration(5000);
        scanAnim.setRepeatCount(ValueAnimator.INFINITE);
        scanAnim.start();
    }

    /**
     * 启动 AR 按钮呼吸光动画
     */
    private void startARGlowAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.3f, 1.0f);
        alphaAnimation.setDuration(1200);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        getBinding().viewArGlow.startAnimation(alphaAnimation);
    }

    /**
     * 更新 HUD
     */
    private void updateHudWithRealData(AMapLocation aMapLocation) {
        if (aMapLocation == null) return;

        getBinding().tvHudLat.setText(String.format("LAT: %.4f° N", aMapLocation.getLatitude()));
        getBinding().tvHudLng.setText(String.format("LNG: %.4f° E", aMapLocation.getLongitude()));

        String address = aMapLocation.getDistrict() + aMapLocation.getStreet() + aMapLocation.getStreetNum();
        if (address.isEmpty()) {
            address = aMapLocation.getAddress();
        }
        getBinding().tvHudAddress.setText("LOC: " + address);
    }

    // ==================== Fragment 生命周期 ====================

    @Override
    public void onResume() {
        super.onResume();
        presenter.loadMemoryPoints();
        if (isLocationInitialized) {
            if (locationClient != null) {
                locationClient.startLocation();
            }
        } else if (hasLocationPermission()) {
            initLocationAndStart();
            isLocationInitialized = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (markerRenderer != null) {
            markerRenderer.onPause();
        }
        presenter.pauseLocation();
        if (locationClient != null) {
            locationClient.stopLocation();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        MapLifecycleHelper.onHiddenChanged(getBinding().mapView, hidden);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        MapLifecycleHelper.onSaveInstanceState(getBinding().mapView, outState);
    }

    @Override
    public void onDestroyView() {
        // 1. 停止扫描线动画
        if (scanAnim != null) {
            scanAnim.cancel();
            scanAnim = null;
        }

        // 2. 停止 AR 呼吸灯动画
        if (getBinding() != null) {
            getBinding().viewArGlow.clearAnimation();
        }

        // 3. 释放本页面创建的 Marker、图片请求和选中动画
        if (markerRenderer != null) {
            markerRenderer.clear();
            markerRenderer = null;
        }

        if (aMap != null) {
            aMap.setMyLocationEnabled(false);
            aMap.setLocationSource(null);
            aMap.setOnMapTouchListener(null);
            aMap.setOnMarkerClickListener(null);
        }

        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.unRegisterLocationListener(this);
        }

        aMap = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationClient != null) {
            locationClient.onDestroy();
            locationClient = null;
        }
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
