package com.ggg.rememo.feature.here.marker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 使用 pointId 对 Marker 展示快照进行 O(n) 差量计算。 */
public final class MarkerDiffCalculator {

    private MarkerDiffCalculator() {
    }

    @NonNull
    public static MarkerDiff calculate(
            @Nullable Map<String, MemoryPointMarkerSnapshot> current,
            @Nullable List<MemoryPoint> latestPoints) {
        Map<String, MemoryPointMarkerSnapshot> oldSnapshots = current == null
                ? new LinkedHashMap<>()
                : current;
        Map<String, MemoryPointMarkerSnapshot> latestSnapshots = snapshotMapOf(latestPoints);

        List<MemoryPointMarkerSnapshot> added = new ArrayList<>();
        List<String> removedIds = new ArrayList<>();
        List<MemoryPointMarkerSnapshot> changed = new ArrayList<>();
        List<String> unchangedIds = new ArrayList<>();

        for (String oldId : oldSnapshots.keySet()) {
            if (!latestSnapshots.containsKey(oldId)) {
                removedIds.add(oldId);
            }
        }

        for (Map.Entry<String, MemoryPointMarkerSnapshot> entry : latestSnapshots.entrySet()) {
            MemoryPointMarkerSnapshot oldSnapshot = oldSnapshots.get(entry.getKey());
            MemoryPointMarkerSnapshot newSnapshot = entry.getValue();
            if (oldSnapshot == null) {
                added.add(newSnapshot);
            } else if (!oldSnapshot.equals(newSnapshot)) {
                changed.add(newSnapshot);
            } else {
                unchangedIds.add(entry.getKey());
            }
        }

        return new MarkerDiff(added, removedIds, changed, unchangedIds);
    }

    /**
     * 将业务对象转换为确定顺序的快照 Map。
     * null/空 id 会被忽略；重复 id 以后出现的数据为准。
     */
    @NonNull
    public static Map<String, MemoryPointMarkerSnapshot> snapshotMapOf(
            @Nullable List<MemoryPoint> points) {
        Map<String, MemoryPointMarkerSnapshot> result = new LinkedHashMap<>();
        if (points == null) {
            return result;
        }
        for (MemoryPoint point : points) {
            MemoryPointMarkerSnapshot snapshot = MemoryPointMarkerSnapshot.from(point);
            if (snapshot != null) {
                result.put(snapshot.getPointId(), snapshot);
            }
        }
        return result;
    }
}
