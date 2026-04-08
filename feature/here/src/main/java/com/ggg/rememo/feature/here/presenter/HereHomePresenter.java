package com.ggg.rememo.feature.here.presenter;

import android.os.Handler;
import android.os.Looper;

import com.amap.api.location.AMapLocation;
import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.feature.here.contract.HereHomeContract;
import com.ggg.rememo.feature.here.data.HereRepository;

import java.util.List;

public class HereHomePresenter extends BasePresenter<HereHomeContract.View>
        implements HereHomeContract.Presenter {

    private static final String TAG = "HerePresenter";

    private final HereRepository repository;
    private final Handler mainHandler;

    private boolean isFollowing = true;
    private boolean isLocationInitialized = false;

    public HereHomePresenter(HereRepository repository) {
        this.repository = repository;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void initLocation() {
        if (isLocationInitialized) {
            return;
        }

        double[] cached = repository.getCachedLocation();
        if (cached != null && cached[0] != 0 && cached[1] != 0) {
            ifViewAttached(view -> view.moveCameraToLocation(cached[0], cached[1], false));
        }
    }

    @Override
    public void pauseLocation() {
        double[] cached = repository.getCachedLocation();
        if (cached != null) {
            repository.saveCachedLocation(cached[0], cached[1]);
        }
    }

    @Override
    public void checkLocationPermission() {
        if (isLocationInitialized) {
            return;
        }
        ifViewAttached(HereHomeContract.View::requestLocationPermission);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        isLocationInitialized = granted;
    }

    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location == null) {
            return;
        }
        if (location.getErrorCode() != AMapLocation.LOCATION_SUCCESS) {
            String errorInfo = "定位失败 errorCode=" + location.getErrorCode()
                    + ", " + location.getErrorInfo();
            ifViewAttached(view -> view.showLocationError(errorInfo));
            return;
        }

        ifViewAttached(view -> view.onLocationReceived(location));

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        repository.saveCachedLocation(lat, lng);
    }

    @Override
    public void loadMemoryPoints() {
        // 缓存优先：先展示本地缓存数据（立即），后台拉取网络最新数据（异步）
        repository.loadWithCacheFirst(
                // 缓存回调：立即展示，无 loading 遮罩
                new MemoryPointRepository.Callback<List<MemoryPoint>>() {
                    @Override
                    public void onSuccess(List<MemoryPoint> cachedData) {
                        mainHandler.post(() -> {
                            ifViewAttached(view -> view.showMemoryPoints(cachedData));
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        // 缓存读取失败，静默处理，等网络回调即可
                    }
                },
                // 网络回调：后台拉取成功后静默更新地图，不显示 loading
                new MemoryPointRepository.Callback<List<MemoryPoint>>() {
                    @Override
                    public void onSuccess(List<MemoryPoint> networkData) {
                        mainHandler.post(() -> {
                            ifViewAttached(view -> view.refreshMemoryPoints(networkData));
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        // 网络失败时静默处理，缓存数据已展示
                    }
                }
        );
    }

    @Override
    public void setFollowingMode(boolean follow) {
        isFollowing = follow;
        ifViewAttached(view -> view.updateFollowModeIcon(follow));
    }

    @Override
    public void onMapTouched() {
        if (isFollowing) {
            setFollowingMode(false);
        }
    }

    @Override
    public void onLocationButtonClicked() {
        setFollowingMode(true);
        if (!isLocationInitialized) {
            checkLocationPermission();
        }
    }

    @Override
    public void onMarkerClicked(MemoryPoint point) {
        if (point == null) {
            return;
        }
        ifViewAttached(view -> view.showMemoryPointBottomSheet(point));
    }

    @Override
    public boolean isFollowing() {
        return isFollowing;
    }
}
