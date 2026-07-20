package com.ggg.rememo.feature.here.marker;

import androidx.annotation.NonNull;

import java.util.List;

/** Marker 快照集合的增删改结果。 */
public final class MarkerDiff {

    private final List<MemoryPointMarkerSnapshot> added;
    private final List<String> removedIds;
    private final List<MemoryPointMarkerSnapshot> changed;
    private final List<String> unchangedIds;

    MarkerDiff(List<MemoryPointMarkerSnapshot> added,
               List<String> removedIds,
               List<MemoryPointMarkerSnapshot> changed,
               List<String> unchangedIds) {
        this.added = immutableCopy(added);
        this.removedIds = immutableCopy(removedIds);
        this.changed = immutableCopy(changed);
        this.unchangedIds = immutableCopy(unchangedIds);
    }

    @NonNull
    public List<MemoryPointMarkerSnapshot> getAdded() {
        return added;
    }

    @NonNull
    public List<String> getRemovedIds() {
        return removedIds;
    }

    @NonNull
    public List<MemoryPointMarkerSnapshot> getChanged() {
        return changed;
    }

    @NonNull
    public List<String> getUnchangedIds() {
        return unchangedIds;
    }

    public int getUnchangedCount() {
        return unchangedIds.size();
    }

    private static <T> List<T> immutableCopy(List<T> source) {
        return List.copyOf(source);
    }
}
