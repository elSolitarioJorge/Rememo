package com.ggg.rememo.feature.here.marker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import java.util.Objects;

/**
 * Marker 外观所依赖的不可变快照。
 * 仅当这些字段变化时，地图 Marker 才需要重建。
 */
public final class MemoryPointMarkerSnapshot {

    @NonNull
    private final String pointId;
    private final double latitude;
    private final double longitude;
    @Nullable
    private final String pointName;
    @Nullable
    private final String coverImageUrl;

    public MemoryPointMarkerSnapshot(@NonNull String pointId,
                                     double latitude,
                                     double longitude,
                                     @Nullable String pointName,
                                     @Nullable String coverImageUrl) {
        this.pointId = pointId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pointName = pointName;
        this.coverImageUrl = coverImageUrl;
    }

    @Nullable
    public static MemoryPointMarkerSnapshot from(@Nullable MemoryPoint point) {
        if (point == null) {
            return null;
        }
        String pointId = point.getPointId();
        if (pointId.trim().isEmpty()) {
            return null;
        }
        return new MemoryPointMarkerSnapshot(
                pointId,
                point.getLatitude(),
                point.getLongitude(),
                point.getPointName(),
                point.getCoverImageUrl()
        );
    }

    @NonNull
    public String getPointId() {
        return pointId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Nullable
    public String getPointName() {
        return pointName;
    }

    @Nullable
    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemoryPointMarkerSnapshot)) return false;
        MemoryPointMarkerSnapshot that = (MemoryPointMarkerSnapshot) o;
        return Double.compare(that.latitude, latitude) == 0
                && Double.compare(that.longitude, longitude) == 0
                && pointId.equals(that.pointId)
                && Objects.equals(pointName, that.pointName)
                && Objects.equals(coverImageUrl, that.coverImageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointId, latitude, longitude, pointName, coverImageUrl);
    }
}
