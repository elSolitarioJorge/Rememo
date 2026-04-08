package com.ggg.rememo.feature.here.contract;

import com.amap.api.location.AMapLocation;
import com.ggg.rememo.core.base.BaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import java.util.List;

public interface HereHomeContract {

    interface View extends BaseView {

        void showMemoryPoints(List<MemoryPoint> points);
        void refreshMemoryPoints(List<MemoryPoint> points);
        void showLocationError(String message);
        void onLocationReceived(AMapLocation location);
        void requestLocationPermission();
        void showPermissionDeniedDialog();
        void updateFollowModeIcon(boolean isFollowing);
        void moveCameraToLocation(double lat, double lng, boolean animate);
        void showMemoryPointBottomSheet(MemoryPoint point);
    }
    interface Presenter {
        void initLocation();
        void pauseLocation();
        void checkLocationPermission();
        void onPermissionResult(boolean granted);
        void onLocationChanged(AMapLocation location);
        void loadMemoryPoints();
        void setFollowingMode(boolean follow);
        void onMapTouched();
        void onLocationButtonClicked();
        void onMarkerClicked(MemoryPoint point);
        boolean isFollowing();
    }
}
