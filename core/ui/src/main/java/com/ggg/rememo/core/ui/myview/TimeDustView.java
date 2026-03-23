package com.ggg.rememo.core.ui.myview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;


import androidx.annotation.NonNull;

import java.util.Random;

public class TimeDustView extends View {

    private Paint paint;
    private DustParticle[] particles;
    private ValueAnimator animator;
    private final Random random = new Random();

    // 粒子总数
    private static final int PARTICLE_COUNT = 40;

    // 交互力场参数
    private float touchX = -1000f;
    private float touchY = -1000f;
    private boolean isTouching = false;
    private float REPEL_RADIUS = 350f; // 手指排斥半径 (px)

    public TimeDustView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFFFBBF24); // 琥珀色

        particles = new DustParticle[PARTICLE_COUNT];
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i] = new DustParticle();
        }

        // 60fps 刷新驱动
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            updateParticles();
            invalidate();
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float density = getResources().getDisplayMetrics().density;
        REPEL_RADIUS = 150 * density; // 根据屏幕密度调整力场大小
        for (DustParticle p : particles) {
            p.init(w, h, density, true);
        }
        if (!animator.isRunning()) {
            animator.start();
        }
    }

    // 监听手指滑动，产生排斥力场
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isTouching = true;
                touchX = event.getX();
                touchY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouching = false;
                touchX = -1000f;
                touchY = -1000f;
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateParticles() {
        int w = getWidth();
        int h = getHeight();
        float fadeEdge = h * 0.15f;

        for (DustParticle p : particles) {

            // --- 核心交互：指尖排斥力场 ---
            if (isTouching) {
                float dx = p.x - touchX;
                float dy = p.y - touchY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < REPEL_RADIUS) {
                    // 距离越近，排斥力越强
                    float force = (REPEL_RADIUS - dist) / REPEL_RADIUS;
                    p.velocityX += (dx / dist) * force * 2.0f; // 产生瞬间的加速度
                    p.velocityY += (dy / dist) * force * 2.0f;
                }
            }

            // 应用基础漂浮速度与外力加速度
            p.x += p.velocityX + (float) Math.sin(p.phase) * p.speedX;
            p.y -= p.speedY - p.velocityY; // y 是向上减的，velocityY是向下加的排斥力

            // 阻尼摩擦力：让被推开的粒子慢慢恢复正常的漂浮速度
            p.velocityX *= 0.9f;
            p.velocityY *= 0.9f;

            p.phase += p.phaseSpeed;

            // 呼吸闪烁
            float twinkle = (float) (Math.sin(p.phase * 2) + 1) / 2f;
            float currentAlpha = p.baseAlpha * (0.6f + 0.4f * twinkle);

            // 边缘淡入淡出
            if (p.y > h - fadeEdge) {
                currentAlpha *= (h - p.y) / fadeEdge;
            } else if (p.y < fadeEdge) {
                currentAlpha *= Math.max(0, p.y / fadeEdge);
            }

            p.currentDrawAlpha = Math.max(0f, Math.min(1f, currentAlpha));

            // 越界重置
            if (p.y < -p.radius * 2 || p.x < -p.radius * 2 || p.x > w + p.radius * 2) {
                p.init(w, h, getResources().getDisplayMetrics().density, false);
            }
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (DustParticle p : particles) {
            paint.setAlpha((int) (p.currentDrawAlpha * 255));
            canvas.drawCircle(p.x, p.y, p.radius, paint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) animator.cancel();
    }

    // --- 内部数据类：尘埃粒子 (分为 Bokeh 和 Spark) ---
    private class DustParticle {
        float x, y;
        float radius;
        float speedY, speedX;
        float velocityX = 0, velocityY = 0; // 受外力影响的动态速度
        float phase, phaseSpeed;
        float baseAlpha, currentDrawAlpha;
        boolean isBokeh; // 是否为巨型焦外光斑

        void init(int viewWidth, int viewHeight, float density, boolean randomY) {
            x = random.nextFloat() * viewWidth;
            y = randomY ? random.nextFloat() * viewHeight : viewHeight + random.nextFloat() * 100;

            // 20% 概率生成巨型焦外光斑，80%生成微小火星
            isBokeh = random.nextFloat() < 0.2f;

            if (isBokeh) {
                // 焦外光斑：巨大，极其透明，移动极慢
                radius = (random.nextFloat() * 20f + 15f) * density; // 15dp - 35dp
                baseAlpha = random.nextFloat() * 0.1f + 0.05f;       // 5% - 15% 透明度
                speedY = (random.nextFloat() * 0.5f + 0.2f) * density;
                speedX = (random.nextFloat() * 0.5f + 0.1f) * density;
            } else {
                // 前景火星：细小，较亮，移动快
                radius = (random.nextFloat() * 2.5f + 0.5f) * density; // 0.5dp - 3dp
                baseAlpha = random.nextFloat() * 0.5f + 0.3f;          // 30% - 80% 透明度
                speedY = (random.nextFloat() * 2.5f + 1.0f) * density;
                speedX = (random.nextFloat() * 1.5f + 0.5f) * density;
            }

            phase = random.nextFloat() * (float) Math.PI * 2;
            phaseSpeed = random.nextFloat() * 0.05f + 0.02f;
            velocityX = 0;
            velocityY = 0;
        }
    }
}
