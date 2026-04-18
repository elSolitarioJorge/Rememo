package com.ggg.rememo.feature.explore.view.util;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalItemDecoration extends RecyclerView.ItemDecoration {
    private int space;
    public HorizontalItemDecoration(int space) {
        this.space = space;
    }
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        // 每个Item右侧间距
        if (position != parent.getAdapter().getItemCount() - 1) {
            outRect.right = space;
        }
    }
}
