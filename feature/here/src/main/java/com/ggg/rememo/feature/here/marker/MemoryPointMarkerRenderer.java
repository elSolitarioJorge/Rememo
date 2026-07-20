package com.ggg.rememo.feature.here.marker;

import android.annotation.SuppressLint;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.feature.here.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 维护记忆点 Marker 的增量渲染与资源生命周期。
 */
public final class MemoryPointMarkerRenderer {

    private static final float DEFAULT_ANCHOR_X = 0.5f;
    private static final float DEFAULT_ANCHOR_Y = 1.0f;
    private static final float SELECTED_ANCHOR_Y = 1.15f;

    private final AMap map;
    private final RequestManager requestManager;
    private final LayoutInflater inflater;
    private final RequestOptions imageOptions;
    private final Map<String, MarkerEntry> entries = new LinkedHashMap<>();

    @Nullable
    private String selectedPointId;
    @Nullable
    private Marker selectedMarker;
    @Nullable
    private ValueAnimator selectedAnimator;

    public MemoryPointMarkerRenderer(@NonNull AMap map,
                                     @NonNull RequestManager requestManager,
                                     @NonNull LayoutInflater inflater,
                                     @NonNull Resources resources) {
        this.map = map;
        this.requestManager = requestManager;
        this.inflater = inflater;
        int radiusPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 12, resources.getDisplayMetrics());
        this.imageOptions = new RequestOptions()
                .transform(new CenterCrop(), new RoundedCorners(radiusPx))
                .override(200, 200)
                .disallowHardwareConfig();
    }

    /**
     * 对最新记忆点集合执行同步差量提交。图片加载仍由 Glide 异步完成。
     */
    @MainThread
    @NonNull
    public RenderStats render(@Nullable List<MemoryPoint> points) {
        long startNanos = SystemClock.elapsedRealtimeNanos();
        Map<String, MemoryPoint> latestPoints = pointMapOf(points);
        Map<String, MemoryPointMarkerSnapshot> currentSnapshots = new LinkedHashMap<>();
        for (Map.Entry<String, MarkerEntry> entry : entries.entrySet()) {
            currentSnapshots.put(entry.getKey(), entry.getValue().snapshot);
        }

        MarkerDiff diff = MarkerDiffCalculator.calculate(currentSnapshots, points);

        for (String removedId : diff.getRemovedIds()) {
            removeEntry(removedId, false);
        }

        for (MemoryPointMarkerSnapshot changed : diff.getChanged()) {
            removeEntry(changed.getPointId(), true);
        }

        for (MemoryPointMarkerSnapshot added : diff.getAdded()) {
            addEntry(added, latestPoints.get(added.getPointId()));
        }
        for (MemoryPointMarkerSnapshot changed : diff.getChanged()) {
            addEntry(changed, latestPoints.get(changed.getPointId()));
        }

        // Marker 外观未变化时仍刷新绑定的业务对象，保证点击拿到最新完整数据。
        for (String unchangedId : diff.getUnchangedIds()) {
            MarkerEntry entry = entries.get(unchangedId);
            MemoryPoint latestPoint = latestPoints.get(unchangedId);
            if (entry != null && latestPoint != null && !entry.marker.isRemoved()) {
                entry.marker.setObject(latestPoint);
            }
        }

        long costNanos = SystemClock.elapsedRealtimeNanos() - startNanos;
        return new RenderStats(
                latestPoints.size(),
                diff.getAdded().size(),
                diff.getRemovedIds().size(),
                diff.getChanged().size(),
                diff.getUnchangedCount(),
                costNanos
        );
    }

    /** 只为当前选中的记忆点 Marker 保留浮动动画。 */
    @MainThread
    public void select(@Nullable Marker marker) {
        if (marker == null || marker.isRemoved() || !(marker.getObject() instanceof MemoryPoint)) {
            return;
        }
        MemoryPoint point = (MemoryPoint) marker.getObject();
        MarkerEntry entry = entries.get(point.getPointId());
        if (entry == null || entry.marker != marker) {
            return;
        }

        stopSelectedAnimation(true);
        selectedPointId = point.getPointId();
        startSelectedAnimation(marker);
    }

    /** 页面不可见时停止动画，避免后台持续刷新 Marker anchor。 */
    @MainThread
    public void onPause() {
        stopSelectedAnimation(true);
    }

    /** 释放本渲染器创建的 Marker、Glide Target、Descriptor 和动画。 */
    @MainThread
    public void clear() {
        stopSelectedAnimation(true);
        for (String pointId : new ArrayList<>(entries.keySet())) {
            removeEntry(pointId, false);
        }
        entries.clear();
    }

    private void addEntry(MemoryPointMarkerSnapshot snapshot, @Nullable MemoryPoint point) {
        if (point == null) {
            return;
        }

        View markerView = inflateMarkerView(null);
        BitmapDescriptor placeholderDescriptor = BitmapDescriptorFactory.fromView(markerView);
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(snapshot.getLatitude(), snapshot.getLongitude()))
                .title(snapshot.getPointName())
                .anchor(DEFAULT_ANCHOR_X, DEFAULT_ANCHOR_Y)
                .icon(placeholderDescriptor)
                .zIndex(1.0f);
        Marker marker = map.addMarker(options);
        if (marker == null) {
            recycleDescriptor(placeholderDescriptor);
            return;
        }

        marker.setObject(point);
        MarkerEntry entry = new MarkerEntry(snapshot, marker, placeholderDescriptor);
        entries.put(snapshot.getPointId(), entry);

        String coverUrl = snapshot.getCoverImageUrl();
        Object imageSource = coverUrl == null || coverUrl.isEmpty()
                ? R.drawable.pic_old
                : coverUrl;

        CustomTarget<Bitmap> target = new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource,
                                        @Nullable Transition<? super Bitmap> transition) {
                MarkerEntry current = entries.get(snapshot.getPointId());
                if (current != entry || marker.isRemoved()) {
                    return;
                }

                View loadedMarkerView = inflateMarkerView(resource);
                BitmapDescriptor loadedDescriptor = BitmapDescriptorFactory.fromView(loadedMarkerView);
                BitmapDescriptor oldDescriptor = entry.descriptor;
                marker.setIcon(loadedDescriptor);
                entry.descriptor = loadedDescriptor;
                recycleDescriptor(oldDescriptor);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
                // Descriptor 由 MarkerEntry 统一管理和释放。
            }
        };
        entry.imageTarget = target;
        requestManager.asBitmap()
                .load(imageSource)
                .apply(imageOptions)
                .into(target);

        if (snapshot.getPointId().equals(selectedPointId)) {
            startSelectedAnimation(marker);
        }
    }

    @SuppressLint("InflateParams")
    private View inflateMarkerView(@Nullable Bitmap bitmap) {
        // Marker 图标会被转换为 BitmapDescriptor，不会挂载到普通 ViewGroup。
        View markerView = inflater.inflate(R.layout.layout_custom_marker_2, null);
        if (bitmap != null) {
            ImageView imageView = markerView.findViewById(R.id.iv_marker_image);
            imageView.setImageBitmap(bitmap);
        }
        markerView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        return markerView;
    }

    private void removeEntry(String pointId, boolean keepSelection) {
        MarkerEntry entry = entries.remove(pointId);
        if (entry == null) {
            return;
        }

        if (entry.marker == selectedMarker) {
            stopSelectedAnimation(!keepSelection);
        } else if (!keepSelection && pointId.equals(selectedPointId)) {
            selectedPointId = null;
        }

        if (entry.imageTarget != null) {
            requestManager.clear(entry.imageTarget);
            entry.imageTarget = null;
        }
        if (!entry.marker.isRemoved()) {
            entry.marker.remove();
        }
        recycleDescriptor(entry.descriptor);
        entry.descriptor = null;
    }

    private void startSelectedAnimation(Marker marker) {
        stopSelectedAnimation(false);
        selectedMarker = marker;
        selectedAnimator = ValueAnimator.ofFloat(DEFAULT_ANCHOR_Y, SELECTED_ANCHOR_Y);
        selectedAnimator.setDuration(1500L);
        selectedAnimator.setRepeatCount(ValueAnimator.INFINITE);
        selectedAnimator.setRepeatMode(ValueAnimator.REVERSE);
        selectedAnimator.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        selectedAnimator.addUpdateListener(animation -> {
            Marker current = selectedMarker;
            if (current == null || current.isRemoved()) {
                animation.cancel();
                return;
            }
            current.setAnchor(DEFAULT_ANCHOR_X, (float) animation.getAnimatedValue());
        });
        selectedAnimator.start();
    }

    private void stopSelectedAnimation(boolean clearSelection) {
        if (selectedAnimator != null) {
            selectedAnimator.cancel();
            selectedAnimator.removeAllUpdateListeners();
            selectedAnimator = null;
        }
        if (selectedMarker != null && !selectedMarker.isRemoved()) {
            selectedMarker.setAnchor(DEFAULT_ANCHOR_X, DEFAULT_ANCHOR_Y);
        }
        selectedMarker = null;
        if (clearSelection) {
            selectedPointId = null;
        }
    }

    private static void recycleDescriptor(@Nullable BitmapDescriptor descriptor) {
        if (descriptor != null) {
            descriptor.recycle();
        }
    }

    private static Map<String, MemoryPoint> pointMapOf(@Nullable List<MemoryPoint> points) {
        Map<String, MemoryPoint> result = new LinkedHashMap<>();
        if (points == null) {
            return result;
        }
        for (MemoryPoint point : points) {
            if (point != null) {
                String pointId = point.getPointId();
                if (!pointId.trim().isEmpty()) {
                    result.put(pointId, point);
                }
            }
        }
        return result;
    }

    private static final class MarkerEntry {
        private final MemoryPointMarkerSnapshot snapshot;
        private final Marker marker;
        @Nullable
        private CustomTarget<Bitmap> imageTarget;
        @Nullable
        private BitmapDescriptor descriptor;

        private MarkerEntry(MemoryPointMarkerSnapshot snapshot,
                            Marker marker,
                            @Nullable BitmapDescriptor descriptor) {
            this.snapshot = snapshot;
            this.marker = marker;
            this.descriptor = descriptor;
        }
    }
}
