package com.ggg.rememo.feature.profile.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProfileTimelineDecoration extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private final Paint glowPaint;
    private final int lineXOffset;

    public ProfileTimelineDecoration(Context context) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#E6F5A623")); // 90% 琥珀金
        paint.setStrokeWidth(dpToPx(context, 2));

        // 核心发光特效
        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setColor(Color.parseColor("#4DF5A623")); // 30% 琥珀金
        glowPaint.setStrokeWidth(dpToPx(context, 6));

        // 线条在屏幕左侧的位置：21dp (16dp margin + 5dp 居中圆点)
        lineXOffset = dpToPx(context, 21);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        if (parent.getChildCount() > 0) {
            View firstChild = parent.getChildAt(0);
            View lastChild = parent.getChildAt(parent.getChildCount() - 1);

            float startY = firstChild.getTop();
            float endY = lastChild.getBottom();

            // 先画发光层（粗）
            c.drawLine(lineXOffset, startY, lineXOffset, endY, glowPaint);
            // 再画实体层（细）
            c.drawLine(lineXOffset, startY, lineXOffset, endY, paint);
        }
    }

    private int dpToPx(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
    }
}
