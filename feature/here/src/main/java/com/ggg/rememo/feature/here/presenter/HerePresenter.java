package com.ggg.rememo.feature.here.presenter;

import com.amap.api.location.AMapLocation;
import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.feature.here.contract.HereContract;
import com.ggg.rememo.feature.here.data.HereRepository;

import java.util.List;

public class HerePresenter extends BasePresenter<HereContract.View>
        implements HereContract.Presenter {

    private final HereRepository repository;

    private boolean isFollowing = true;
    private boolean isLocationInitialized = false;

    public HerePresenter(HereRepository repository) {
        this.repository = repository;
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
    public void resumeLocation() {
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
        ifViewAttached(HereContract.View::requestLocationPermission);
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
            ifViewAttached(view -> view.showLocationError(location.getErrorInfo()));
            return;
        }

        ifViewAttached(view -> view.onLocationReceived(location));

        double lat = location.getLatitude();
        double lng = location.getLongitude();
        repository.saveCachedLocation(lat, lng);
    }

    @Override
    public void loadMemoryPoints() {
        repository.loadAllMemoryPoints(new MemoryPointRepository.Callback<List<MemoryPoint>>() {
            @Override
            public void onSuccess(List<MemoryPoint> result) {
                ifViewAttached(view -> view.showMemoryPoints(result));
            }

            @Override
            public void onError(Exception e) {
                ifViewAttached(view -> view.showError("加载记忆点失败: " + e.getMessage()));
            }
        });
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
