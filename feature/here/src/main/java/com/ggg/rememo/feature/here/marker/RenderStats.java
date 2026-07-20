package com.ggg.rememo.feature.here.marker;

import androidx.annotation.NonNull;

import java.util.Locale;

/** 一次 Marker 增量渲染的同步统计数据。 */
public final class RenderStats {

    private final int total;
    private final int added;
    private final int removed;
    private final int changed;
    private final int unchanged;
    private final long costNanos;

    RenderStats(int total, int added, int removed, int changed, int unchanged, long costNanos) {
        this.total = total;
        this.added = added;
        this.removed = removed;
        this.changed = changed;
        this.unchanged = unchanged;
        this.costNanos = costNanos;
    }

    public int getTotal() {
        return total;
    }

    public int getAdded() {
        return added;
    }

    public int getRemoved() {
        return removed;
    }

    public int getChanged() {
        return changed;
    }

    public int getUnchanged() {
        return unchanged;
    }

    public double getCostMillis() {
        return costNanos / 1_000_000.0;
    }

    @NonNull
    public String toLogString() {
        return String.format(Locale.US,
                "MarkerRender total=%d added=%d removed=%d changed=%d unchanged=%d syncSubmitCostMs=%.2f",
                total, added, removed, changed, unchanged, getCostMillis());
    }
}
