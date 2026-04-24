package com.ggg.rememo.feature.ar;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.feature.ar.databinding.ActivityTimeLensBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Route(path = Routes.Ar.TIME_LENS)
public class TimeLensActivity extends AppCompatActivity implements SensorEventListener {

    private ActivityTimeLensBinding binding;
    private final int BASE_YEAR = 1900;
    private final Random random = new Random();

    // CameraX 成员变量
    private Preview preview;

    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(this, "需要相机权限来实现 AR 效果", Toast.LENGTH_SHORT).show();
                }
            });

    // 状态管理：是否处于一键穿越的沉浸模式
    private boolean isImmersiveMode = false;
    // 传感器相关
    private SensorManager sensorManager;
    private Sensor rotationVectorSensor;
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationValues = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTimeLensBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化传感器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        }
        if (rotationVectorSensor == null) {
            if (binding != null) {
                binding.tvAzimuth.setText("AZ: N/A");
                binding.tvPitch.setText("PT: N/A");
            }
            Toast.makeText(this, "设备不支持方向传感器，AR 指向功能不可用", Toast.LENGTH_LONG).show();
        }

        // 请求相机权限并启动 CameraX
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            // 用户之前拒绝过，显示原因后再次请求
            new AlertDialog.Builder(this)
                .setTitle("需要相机权限")
                .setMessage("AR 时空透镜需要使用您的相机来呈现增强现实效果")
                .setPositiveButton("授权", (dialog, which) ->
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA))
                .setNegativeButton("取消", null)
                .show();
        } else {
            // 首次请求或用户勾选了"不再询问"——直接请求
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // 基础 UI 和动画设置
        binding.btnExit.setOnClickListener(v -> {
            if (isImmersiveMode) {
                // 如果在沉浸模式，点击返回则是退出沉浸模式
                exitImmersiveMode();
            } else {
                // 否则直接退出 Activity
                finish();
            }
        });
        startRadarAnimation();
        startScanlineAnimation();

        // 右侧时间滑动条：使用 SeekBar 自带的旋转 + 触摸能力
        binding.seekbarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onTimeProgressChanged(progress, fromUser);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.layoutArAnchor.setOnClickListener(v -> enterImmersiveMode());

        binding.seekbarFusion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 将 0-100 的进度转换为 0.0 - 1.0 的透明度
                float alpha = progress / 100f;
                if (binding != null) binding.ivOldPhotoOverlay.setAlpha(alpha);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void startCamera() {
        // 使用 Application Context 避免 CameraX 初始化阶段持有 Activity
        Context appContext = getApplicationContext();
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(appContext);
        cameraProviderFuture.addListener(() -> {
            // 异步回调回来时，务必检查 Activity 状态
            if (isFinishing() || isDestroyed()) {
                return;
            }
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // 预览设置
                preview = new Preview.Builder().build();
                if (binding != null) {
                    preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
                }

                // 选择后置摄像头
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // 绑定生命周期
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(appContext));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rotationVectorSensor != null) {
            // SENSOR_DELAY_UI 足以提供流畅且不耗电的更新
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            // 将旋转矢量转换为旋转矩阵
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);

            // 适配屏幕方向：将设备坐标系映射到屏幕坐标系
            final int rotation = getWindowManager().getDefaultDisplay().getRotation();
            final int worldAxisForDeviceAxis;
            if (rotation == Surface.ROTATION_90) {
                worldAxisForDeviceAxis = SensorManager.AXIS_X;
            } else if (rotation == Surface.ROTATION_180) {
                worldAxisForDeviceAxis = SensorManager.AXIS_Z;
            } else if (rotation == Surface.ROTATION_270) {
                worldAxisForDeviceAxis = SensorManager.AXIS_MINUS_X;
            } else {
                worldAxisForDeviceAxis = SensorManager.AXIS_MINUS_Z;
            }
            SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxis, SensorManager.AXIS_Y, rotationMatrix);

            // 获取方向值
            SensorManager.getOrientation(rotationMatrix, orientationValues);

            // orientationValues[0]: 方位角 Azimuth (-PI to PI)
            // orientationValues[1]: 俯仰角 Pitch (-PI/2 to PI/2)

            float azimuthDeg = (float) Math.toDegrees(orientationValues[0]);
            float pitchDeg = (float) Math.toDegrees(orientationValues[1]);

            // 转换方位角为 0-360，并获取东南西北字母
            if (azimuthDeg < 0) azimuthDeg += 360;
            String direction = getDirectionLetter(azimuthDeg);

            // 更新 UI (例如: "AZ: 184°S", "PT: +12.5°")
            String azStr = String.format(Locale.getDefault(), "AZ: %03d°%s", (int)azimuthDeg, direction);
            String ptStr = String.format(Locale.getDefault(), "PT: %+05.1f°", pitchDeg);

            if (binding != null) {
                binding.tvAzimuth.setText(azStr);
                binding.tvPitch.setText(ptStr);
            }
        }
    }

    private String getDirectionLetter(float angle) {
        if (angle >= 337.5 || angle < 22.5) return "N";
        if (angle >= 22.5 && angle < 67.5) return "NE";
        if (angle >= 67.5 && angle < 112.5) return "E";
        if (angle >= 112.5 && angle < 157.5) return "SE";
        if (angle >= 157.5 && angle < 202.5) return "S";
        if (angle >= 202.5 && angle < 247.5) return "SW";
        if (angle >= 247.5 && angle < 292.5) return "W";
        return "NW"; // 292.5 to 337.5
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 不需要处理
    }

    private void handleTimeTravelLogic(int year) {
        if (year == 2026) {
            showModernEra();

        } else if (year == 2023) {
            showAnchor(R.drawable.pic_2024, "西邮逸夫楼", "2023年 时空记忆", "94%");

        } else {
            // 其他没有锚点的年份，显示搜索中
            showSearchingState(year);
        }
    }

    // 显示命中锚点的状态
    private void showAnchor(int picId, String title, String subtitle, String resonanceStr) {
        if (binding == null) return;
        binding.tvEraStatus.setText("MEMORY_FRAGMENT DETECTED");
        binding.tvEraStatus.setTextColor(Color.parseColor("#F5A623"));

        // 更新卡片内容并显示
        binding.ivAnchorImage.setImageResource(picId);
        binding.tvAnchorTitle.setText(title);
        binding.tvAnchorSubtitle.setText(subtitle);
        binding.layoutArAnchor.setVisibility(View.VISIBLE);

        // 更新底部 HUD
        binding.tvResonance.setText(resonanceStr);
        binding.tvResonance.setTextColor(Color.parseColor("#FF3366")); // 命中时共鸣度变红，更震撼
        binding.tvAiMsg.setText("发现时空锚点：[" + title + "]");
    }

    // 显示现代状态
    private void showModernEra() {
        if (binding == null) return;
        binding.tvEraStatus.setText("CURRENT ERA");
        binding.tvEraStatus.setTextColor(Color.parseColor("#F5A623"));
        binding.layoutArAnchor.setVisibility(View.INVISIBLE);

        binding.tvResonance.setText("12%");
        binding.tvResonance.setTextColor(Color.parseColor("#F5A623")); // 恢复金色
        binding.tvAiMsg.setText("滑动右侧时间轴，检索历史影像共鸣");
    }

    // 显示搜索中状态
    private void showSearchingState(int year) {
        if (binding == null) return;
        binding.tvEraStatus.setText("SEARCHING SPATIAL DATA...");
        binding.tvEraStatus.setTextColor(Color.parseColor("#00E5FF")); // 青色
        binding.layoutArAnchor.setVisibility(View.INVISIBLE);

        // 随机生成一个较低的共鸣度模拟波动
        int randomRes = random.nextInt(36) + 10;
        binding.tvResonance.setText(randomRes + "%");
        binding.tvResonance.setTextColor(Color.parseColor("#F5A623")); // 恢复金色
        binding.tvAiMsg.setText("正在解析 " + year + " 年的环境数据...");
    }

    private void startRadarAnimation() {
        if (binding == null) return;
        // 外圈顺时针缓慢旋转 (12秒一圈)
        RotateAnimation outerAnim = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        outerAnim.setDuration(12000);
        outerAnim.setRepeatCount(Animation.INFINITE);
        outerAnim.setInterpolator(new LinearInterpolator());
        binding.ringOuter.startAnimation(outerAnim);

        // 内圈逆时针快速旋转 (8秒一圈)，营造复杂的机械齿轮感
        RotateAnimation innerAnim = new RotateAnimation(
                0f, -360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        innerAnim.setDuration(8000);
        innerAnim.setRepeatCount(Animation.INFINITE);
        innerAnim.setInterpolator(new LinearInterpolator());
        binding.ringInner.startAnimation(innerAnim);
    }

    private void startScanlineAnimation() {
        if (binding == null) return;
        // 全息扫描线从顶部扫描到底部
        android.view.animation.TranslateAnimation scanAnim = new android.view.animation.TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, -0.1f, // 从屏幕外 10% 开始
                Animation.RELATIVE_TO_PARENT, 1.1f  // 扫到屏幕外 10% 结束
        );
        scanAnim.setDuration(3500); // 3.5 秒扫描一次
        scanAnim.setRepeatCount(Animation.INFINITE);
        scanAnim.setInterpolator(new LinearInterpolator());
        binding.scanline.startAnimation(scanAnim);
    }

    /**
     * 处理时间进度变化：更新年份文字、联动动画、触发 AR 锚点逻辑
     */
    private void onTimeProgressChanged(int progress, boolean animate) {
        if (binding == null) return;
        int currentYear = BASE_YEAR + progress;
        binding.tvYear.setText(String.valueOf(currentYear));

        if (animate) {
            binding.tvYear.setTextColor(Color.parseColor("#00E5FF"));
            binding.tvYear.animate().scaleX(1.1f).scaleY(1.1f).setDuration(80).withEndAction(() -> {
                if (binding != null) {
                    binding.tvYear.setTextColor(Color.WHITE);
                    binding.tvYear.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                }
            }).start();
        }

        handleTimeTravelLogic(currentYear);
    }

    private void enterImmersiveMode() {
        if (binding == null) return;
        isImmersiveMode = true;
        binding.btnExit.setText("← EXIT VISION");

        // 隐藏原有的所有分析工具 UI (淡出)
        binding.centerReticle.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            if (binding != null) binding.centerReticle.setVisibility(View.GONE);
        }).start();
        binding.layoutYearDisplay.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            if (binding != null) binding.layoutYearDisplay.setVisibility(View.GONE);
        }).start();
        binding.seekbarContainer.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            if (binding != null) binding.seekbarContainer.setVisibility(View.GONE);
        }).start();
        binding.bottomHud.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            if (binding != null) binding.bottomHud.setVisibility(View.GONE);
        }).start();
        binding.layoutArAnchor.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            if (binding != null) binding.layoutArAnchor.setVisibility(View.GONE);
        }).start();

        // 显示老照片叠加层和融合滑块 (淡入)
        binding.ivOldPhotoOverlay.setVisibility(View.VISIBLE);
        binding.ivOldPhotoOverlay.animate().alpha(0.5f).setDuration(800).start();

        binding.layoutFusionHud.setVisibility(View.VISIBLE);
        binding.layoutFusionHud.setAlpha(0f);
        binding.layoutFusionHud.animate().alpha(1f).setDuration(800).start();

        binding.seekbarFusion.setProgress(50);
    }

    private void exitImmersiveMode() {
        if (binding == null) return;
        isImmersiveMode = false;
        binding.btnExit.setText("← EXIT");

        binding.ivOldPhotoOverlay.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            if (binding != null) binding.ivOldPhotoOverlay.setVisibility(View.GONE);
        }).start();
        binding.layoutFusionHud.animate().alpha(0f).setDuration(300).withEndAction(() -> {
            if (binding != null) binding.layoutFusionHud.setVisibility(View.GONE);
        }).start();

        binding.centerReticle.setVisibility(View.VISIBLE);
        binding.layoutYearDisplay.setVisibility(View.VISIBLE);
        binding.seekbarContainer.setVisibility(View.VISIBLE);
        binding.bottomHud.setVisibility(View.VISIBLE);
        binding.layoutArAnchor.setVisibility(View.VISIBLE);

        binding.centerReticle.animate().alpha(1f).setDuration(500).start();
        binding.layoutYearDisplay.animate().alpha(1f).setDuration(500).start();
        binding.seekbarContainer.animate().alpha(1f).setDuration(500).start();
        binding.bottomHud.animate().alpha(1f).setDuration(500).start();
        binding.layoutArAnchor.animate().alpha(1f).setDuration(500).start();
    }

    @Override
    protected void onDestroy() {
        // 1. 释放传感器资源
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        // 2. 停止动画并显式取消所有 View 属性动画
        if (binding != null) {
            binding.ringOuter.clearAnimation();
            binding.ringInner.clearAnimation();
            binding.scanline.clearAnimation();
            
            // 显式取消 ViewPropertyAnimator，防止 withEndAction 回调持有 Activity
            binding.centerReticle.animate().cancel();
            binding.layoutYearDisplay.animate().cancel();
            binding.seekbarContainer.animate().cancel();
            binding.bottomHud.animate().cancel();
            binding.layoutArAnchor.animate().cancel();
            binding.ivOldPhotoOverlay.animate().cancel();
            binding.layoutFusionHud.animate().cancel();
            binding.tvYear.animate().cancel();
        }

        // 3. CameraX 清理
        if (preview != null) {
            // 切断 Preview -> PreviewView -> Activity 的引用链
            preview.setSurfaceProvider(null);
            preview = null;
        }
        
        // 注意：不建议在 onDestroy 中阻塞调用 ProcessCameraProvider.getInstance(this).get().unbindAll()，
        // 阻塞主线程可能导致 ActivityThread 状态更新延迟。CameraX 的 bindToLifecycle 能够自动处理销毁逻辑。

        super.onDestroy();
        // 4. 置空 Binding，释放对布局中所有 View 的引用
        binding = null;
    }
}
