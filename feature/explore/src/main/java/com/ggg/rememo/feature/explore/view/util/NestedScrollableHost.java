package com.ggg.rememo.feature.explore.view.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.viewpager2.widget.ViewPager2;

/**
 * 解决 ViewPager2 内嵌同向 RecyclerView 的滑动冲突。
 * 将横向 RecyclerView 包裹在此 View 内即可。
 * 参考：https://github.com/android/views-widgets-samples/blob/main/ViewPager2/app/src/main/java/androidx/viewpager2/integration/testapp/NestedScrollableHost.kt
 */
public class NestedScrollableHost extends FrameLayout {

    private int touchSlop;
    private float initialX, initialY;

    public NestedScrollableHost(Context context) {
        this(context, null);
    }

    public NestedScrollableHost(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private View getChild() {
        return getChildCount() > 0 ? getChildAt(0) : null;
    }

    private ViewPager2 getParentViewPager() {
        View v = (View) getParent();
        while (v != null && !(v instanceof ViewPager2)) {
            v = (View) v.getParent();
        }
        return (ViewPager2) v;
    }

    private boolean canChildScroll(int orientation, float delta) {
        int direction = (int) -Math.signum(delta);
        View child = getChild();
        if (child == null) return false;
        if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            return child.canScrollHorizontally(direction);
        } else {
            return child.canScrollVertically(direction);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        handleInterceptTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    private void handleInterceptTouchEvent(MotionEvent e) {
        ViewPager2 vp = getParentViewPager();
        if (vp == null) return;

        int orientation = vp.getOrientation();

        // 只处理与 ViewPager2 同向的冲突
        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            initialX = e.getX();
            initialY = e.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = e.getX() - initialX;
            float dy = e.getY() - initialY;
            if (Math.abs(dx) < touchSlop && Math.abs(dy) < touchSlop) return;

            boolean isVpHorizontal = orientation == ViewPager2.ORIENTATION_HORIZONTAL;
            // 滑动角度是否偏向 ViewPager2 方向（> 45° 则不交给 ViewPager2）
            boolean swipeAlongVp = isVpHorizontal
                    ? Math.abs(dx) > Math.abs(dy)
                    : Math.abs(dy) > Math.abs(dx);

            // 角度不偏向 VP 方向时，始终阻止 VP 拦截（即使子 View 已到边界）
            if (!swipeAlongVp) {
                getParent().requestDisallowInterceptTouchEvent(true);
                return;
            }

            float scaledDelta = isVpHorizontal ? dx : dy;
            if (canChildScroll(orientation, scaledDelta)) {
                getParent().requestDisallowInterceptTouchEvent(true);
            } else {
                getParent().requestDisallowInterceptTouchEvent(false);
            }
        }
    }
}
