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
import com.ggg.rememo.core.map.MapLifecycleHelper;
import com.ggg.rememo.feature.here.contract.HereContract;
import com.ggg.rememo.feature.here.data.HereRepository;
import com.ggg.rememo.feature.here.databinding.FragmentHereHomeBinding;
import com.ggg.rememo.feature.here.presenter.HerePresenter;

import java.util.List;

@Route(path = Routes.Here.HOME_FRAGMENT)
public class HereHomeFragment extends Fragment implements HereContract.View, AMapLocationListener, LocationSource {

    private static final String TAG = "HereHomeFragment";
    private static final float DEFAULT_ZOOM_LEVEL = 16f;

    private FragmentHereHomeBinding binding;
    private AMap aMap;
    private AMapLocationClient locationClient;
    private HerePresenter presenter;

    private OnLocationChangedListener mLocationChangedListener;
    private boolean isLocationInitialized = false;
    private LatLng latestLocation;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHereHomeBinding.inflate(inflater, container, false);
        MapLifecycleHelper.bindTo(this, binding.mapView, savedInstanceState);

        presenter = new HerePresenter(new HereRepository(requireContext()));
        presenter.attachView(this);

        initMap();
        initLocationButton();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        presenter.initLocation();
        presenter.checkLocationPermission();
    }

    private void initMap() {
        aMap = binding.mapView.getMap();

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
        aMap.setMyLocationStyle(myLocationStyle);

        aMap.setLocationSource(this);

        aMap.setOnMapTouchListener(event -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && presenter.isFollowing()) {
                presenter.onMapTouched();
            }
        });

        aMap.setOnMarkerClickListener(marker -> {
            MemoryPoint point = (MemoryPoint) marker.getObject();
            if (point != null) {
                aMap.animateCamera(CameraUpdateFactory.changeLatLng(marker.getPosition()));
                presenter.onMarkerClicked(point);
            }
            return true;
        });
    }

    private void initLocationButton() {
        binding.btnMyLocation.setOnClickListener(v -> {
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
            option.setNeedAddress(false);
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
        if (aMap == null) return;
        aMap.clear();
        for (MemoryPoint point : points) {
            addMarkerForMemoryPoint(point);
        }
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
        if (binding == null) return;
        binding.btnMyLocation.setImageResource(isFollowing ? R.drawable.ic_location_follow : R.drawable.ic_location_unfollow);
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
    public void navigateToTimeline(String pointId) {
        // TODO: 实现跳转到时间线页面
    }

    @Override
    public void showMemoryPointBottomSheet(MemoryPoint point) {
        MemoryPointBottomSheetFragment bottomSheet = MemoryPointBottomSheetFragment.newInstance(point);
        bottomSheet.show(getChildFragmentManager(), "MemoryPointBottomSheet");
    }

    @Override
    public Context getViewContext() {
        return requireContext();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    // ==================== Marker 渲染 ====================

    private void addMarkerForMemoryPoint(MemoryPoint point) {
        if (aMap == null || point == null) return;

        LatLng position = new LatLng(point.getLatitude(), point.getLongitude());

        View markerView = LayoutInflater.from(requireContext()).inflate(R.layout.layout_custom_marker, null);

        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(markerView);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .title(point.getPointName())
                .anchor(0.5f, 1.0f)
                .icon(descriptor)
                .zIndex(1.0f);

        Marker marker = aMap.addMarker(markerOptions);
        if (marker != null) {
            marker.setObject(point);
            boolean existImage = point.getCoverImageUrl() != null && !point.getCoverImageUrl().isEmpty();
            int radiusPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());

            RequestOptions options = new RequestOptions()
                    .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                    .override(200, 200)
                    .disallowHardwareConfig();

            Glide.with(this)
                    .asBitmap()
                    .load(existImage ? point.getCoverImageUrl() : R.drawable.pic_old)
                    .apply(options)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            View finalMarkerView = LayoutInflater.from(getContext()).inflate(R.layout.layout_custom_marker, null);
                            ImageView finalIvPic = finalMarkerView.findViewById(R.id.iv_marker_pic);

                            finalIvPic.setImageBitmap(resource);

                            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(finalMarkerView);
                            marker.setIcon(descriptor);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {}
                    });
        }
    }

    // ==================== AMapLocationListener ====================

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (!isAdded() || aMap == null || aMapLocation == null) return;
        presenter.onLocationChanged(aMapLocation);

        if (mLocationChangedListener != null) {
            mLocationChangedListener.onLocationChanged(aMapLocation);
        }
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

    // ==================== Fragment 生命周期 ====================

    @Override
    public void onResume() {
        super.onResume();
        presenter.resumeLocation();
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
        presenter.pauseLocation();
        if (locationClient != null) {
            locationClient.stopLocation();
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
        if (aMap != null) {
            aMap.setMyLocationEnabled(false);
            aMap.setLocationSource(null);
            aMap.setOnMapTouchListener(null);
        }

        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.unRegisterLocationListener(this);
        }

        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
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

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }
}
