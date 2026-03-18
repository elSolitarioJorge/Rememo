package com.ggg.rememo.feature.publish;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.map.LocationPickerActivity;
import com.ggg.rememo.core.model.MemoryPhoto;
import com.ggg.rememo.feature.publish.contract.PublishContract;
import com.ggg.rememo.feature.publish.databinding.ActivityPublishHomeBinding;
import com.ggg.rememo.feature.publish.databinding.DialogTimeSelectionBinding;
import com.ggg.rememo.feature.publish.presenter.PublishPresenter;
import java.util.List;

@Route(path = Routes.Publish.HOME)
public class PublishHomeActivity extends AppCompatActivity implements PublishContract.View {
    private static final String TAG = "PublishHomeActivity1";

    private ActivityPublishHomeBinding binding;
    private PublishPresenter presenter;
    private PhotoThumbnailAdapter photoAdapter;
    private MemoryPhoto currentSelectedPhoto;    // 记录当前选中的照片
    private String currentSelectedSeason = "冬"; // 记录当前选中的季节

    // 位置相关
    private AMapLocationClient locationClient;
    private double currentLat = 0.0;
    private double currentLng = 0.0;
    private String currentAddress = "";
    // 超时时间 10 秒
    private static final long LOCATION_TIMEOUT_MS = 10000;
    private long locationStartTime = 0;


    // 定义选点结果接收器
    private final ActivityResultLauncher<Intent> mapSelectLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // 接收从选点页面传回来的数据
                    String selectedAddress = result.getData().getStringExtra("address");
                    currentLat = result.getData().getDoubleExtra("lat", 0.0);
                    currentLng = result.getData().getDoubleExtra("lng", 0.0);

                    // 更新 UI
                    binding.tvPhysicalLocationText.setText(selectedAddress);
                    binding.tvPhysicalLocationText.setTextColor(Color.parseColor("#D97706")); // 变高亮
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(9), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    handleImagesSelected(uris);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPublishHomeBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化 Presenter 并绑定 View
        presenter = new PublishPresenter();
        presenter.attachView(this);
        setupRecyclerView();
        setupListeners();

        // 启动定位，获取当前位置信息
        initLocationAndStart();
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoThumbnailAdapter();
        binding.rvPhotoThumbnails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.rvPhotoThumbnails.setAdapter(photoAdapter);

        photoAdapter.setOnPhotoClickListener(new PhotoThumbnailAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoSelected(MemoryPhoto photo) {
                currentSelectedPhoto = photo;

                if (photo.getRestoredUrl() != null && !photo.getRestoredUrl().isEmpty()) {
                    binding.btnRunAiRepair.setVisibility(View.GONE);
                    binding.btnSwitch.setVisibility(View.VISIBLE);
                } else {
                    binding.btnRunAiRepair.setVisibility(View.VISIBLE);
                    binding.btnSwitch.setVisibility(View.GONE);
                }

                updateSwitchButtonUI();

                Glide.with(PublishHomeActivity.this)
                        .load(photo.getDisplayUrl())
                        .into(binding.ivDemoImage);
            }

            @Override
            public void onAddMoreClicked() {
                // 点击列表最后的加号，继续添加图片
                launchPhotoPicker();
            }
        });
    }

    private void setupListeners() {
        // 点击上传卡片
        binding.cardRepairContainer.setOnClickListener(v -> {
            if (photoAdapter.getPhotos().isEmpty()) {
                launchPhotoPicker();
            }
        });

        // 启动 AI 修复
        binding.btnRunAiRepair.setOnClickListener(v -> {
            if (currentSelectedPhoto == null) return;

            binding.btnRunAiRepair.setText("AI 引擎处理中...");
            binding.btnRunAiRepair.setEnabled(false);

            // 开启子线程进行网络请求
            new Thread(() -> {
                try {
                    // 调用百度 API 获取上色后的本地文件路径
                    Uri originalUri = Uri.parse(currentSelectedPhoto.getOriginalUrl());
                    String restoredFilePath = BaiduAiUtils.colourize(PublishHomeActivity.this, originalUri);

                    // 切回主线程更新 UI
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 更新 Model 状态
                        currentSelectedPhoto.setRestoredUrl(restoredFilePath);
                        if (currentSelectedPhoto.getCurrentState() == MemoryPhoto.PhotoState.ORIGINAL) {
                            currentSelectedPhoto.toggleState();
                        }

                        // 更新 UI
                        binding.btnRunAiRepair.setText("启动 AI 修复与上色");
                        binding.btnRunAiRepair.setEnabled(true);
                        binding.btnRunAiRepair.setVisibility(View.GONE);

                        binding.btnSwitch.setVisibility(View.VISIBLE);
                        updateSwitchButtonUI();

                        // Glide 能够直接加载本地的 File 路径
                        Glide.with(PublishHomeActivity.this)
                                .load(currentSelectedPhoto.getDisplayUrl())
                                .into(binding.ivDemoImage);

                        Toast.makeText(PublishHomeActivity.this, "记忆重绘完成！", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    // 发生错误时，也要切回主线程恢复按钮状态并提示用户
                    new Handler(Looper.getMainLooper()).post(() -> {
                        binding.btnRunAiRepair.setText("启动 AI 修复与上色");
                        binding.btnRunAiRepair.setEnabled(true);
                        Toast.makeText(PublishHomeActivity.this, "修复失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        // 切换视图按钮
        binding.btnSwitch.setOnClickListener(v -> {
            if (currentSelectedPhoto == null) return;

            currentSelectedPhoto.toggleState();

            Glide.with(PublishHomeActivity.this)
                    .load(currentSelectedPhoto.getDisplayUrl())
                    .into(binding.ivDemoImage);

            updateSwitchButtonUI();
        });

        // 退出按钮
        binding.btnBack.setOnClickListener(v -> finish());

        // 选择时间
        binding.layoutSelectTime.setOnClickListener(v -> showTimeSelectionDialog());

        // 选择位置按钮
        binding.btnSelectLocation.setOnClickListener(v -> {
            // 跳转到选点页面，传入当前已选位置作为初始位置
            Intent intent = new Intent(PublishHomeActivity.this, LocationPickerActivity.class);
            // 可以把当前的经纬度传过去，让地图打开时中心点就是当前位置
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            mapSelectLauncher.launch(intent);
        });
    }

    private void launchPhotoPicker() {
        int maxSelectable = 9 - photoAdapter.getPhotos().size();
        if (maxSelectable > 0) {
            // 注意：部分低版本 Android 的 ActivityResultContracts.PickMultipleVisualMedia 不支持动态设置 maxItems，
            // 传 9 作为全局限制即可，具体过滤逻辑可在拿到结果后判断。
            pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }
    }

    private void handleImagesSelected(List<Uri> uris) {
        // 隐藏占位符，显示大图和底部列表
        binding.layoutUploadPlaceholder.setVisibility(View.GONE);
        binding.ivDemoImage.setVisibility(View.VISIBLE);
        binding.rvPhotoThumbnails.setVisibility(View.VISIBLE);

        photoAdapter.addPhotos(uris);
    }

    private void updateSwitchButtonUI() {
        if (currentSelectedPhoto == null) return;

        if (currentSelectedPhoto.getCurrentState() == MemoryPhoto.PhotoState.RESTORED) {
            binding.btnSwitch.setText("看原图");
        } else {
            binding.btnSwitch.setText("看修复图");
        }
    }

    private void showTimeSelectionDialog() {
        Dialog dialog = new Dialog(this);

        DialogTimeSelectionBinding dialogBinding = DialogTimeSelectionBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        // 设置圆角透明背景和宽度
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.85), ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogBinding.btnYearUp.setOnClickListener(v -> {
            try {
                int year = Integer.parseInt(dialogBinding.etYear.getText().toString());
                if (year < 2026) { // 简单限制最大值
                    dialogBinding.etYear.setText(String.valueOf(year + 1));
                    dialogBinding.etYear.setSelection(dialogBinding.etYear.getText().length());
                }
            } catch (NumberFormatException e) {
                dialogBinding.etYear.setText("1998");
            }
        });

        dialogBinding.btnYearDown.setOnClickListener(v -> {
            try {
                int year = Integer.parseInt(dialogBinding.etYear.getText().toString());
                if (year > 1900) { // 简单限制最小值
                    dialogBinding.etYear.setText(String.valueOf(year - 1));
                    dialogBinding.etYear.setSelection(dialogBinding.etYear.getText().length());
                }
            } catch (NumberFormatException e) {
                dialogBinding.etYear.setText("1998");
            }
        });

        TextView[] seasonViews = new TextView[]{
                dialogBinding.tvSpring,
                dialogBinding.tvSummer,
                dialogBinding.tvAutumn,
                dialogBinding.tvWinter
        };

        // 初始化渲染选中状态
        updateSeasonUI(seasonViews, currentSelectedSeason);

        // 遍历绑定点击事件
        for (TextView seasonView : seasonViews) {
            seasonView.setOnClickListener(v -> {
                currentSelectedSeason = seasonView.getText().toString();
                updateSeasonUI(seasonViews, currentSelectedSeason);
            });
        }

        dialogBinding.btnClose.setOnClickListener(v -> dialog.dismiss());

        dialogBinding.btnConfirm.setOnClickListener(v -> {
            String yearInput = dialogBinding.etYear.getText().toString().trim();
            if (yearInput.isEmpty()) {
                Toast.makeText(this, "请输入发生年份", Toast.LENGTH_SHORT).show();
                return;
            }
            if (Integer.parseInt(yearInput) < 1900 || Integer.parseInt(yearInput) > 2026) {
                Toast.makeText(this, "请输入有效年份（1900 - 2026）", Toast.LENGTH_SHORT).show();
                return;
            }


            // 组装结果并更新外部 Activity UI
            String finalTime = yearInput + " · " + currentSelectedSeason;
            binding.tvTimeText.setText(finalTime);
            binding.tvTimeText.setTextColor(Color.parseColor("#1F2937"));
            binding.tvTimeText.setTypeface(null, Typeface.BOLD);
            binding.tvTimeText.setTextSize(14f);

            dialog.dismiss();
        });

        dialog.show();
    }

    // 根据选中的文本，刷新 4 个按钮的背景和字体颜色
    private void updateSeasonUI(TextView[] seasonViews, String selected) {
        for (TextView view : seasonViews) {
            if (view.getText().toString().equals(selected)) {
                // 选中状态：套用黄色圆角背景，字体变深琥珀色并加粗
                view.setBackgroundResource(R.drawable.bg_season_selected);
                view.setTextColor(Color.parseColor("#D97706"));
                view.setTypeface(null, Typeface.BOLD);
            } else {
                // 未选中状态：套用灰色圆角背景，字体变浅灰色恢复正常
                view.setBackgroundResource(R.drawable.bg_season_normal);
                view.setTextColor(Color.parseColor("#6B7280"));
                view.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    // ========== 位置相关方法 ==========
    private void initLocationAndStart() {
        try {
            locationClient = new AMapLocationClient(getApplicationContext());
            locationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if (aMapLocation != null && aMapLocation.getErrorCode() == AMapLocation.LOCATION_SUCCESS) {
                        // 获取位置成功
                        currentLat = aMapLocation.getLatitude();
                        currentLng = aMapLocation.getLongitude();
                        currentAddress = aMapLocation.getPoiName();
                        if (currentAddress == null || currentAddress.isEmpty()) {
                            currentAddress = aMapLocation.getStreet() + aMapLocation.getStreetNum();
                        }
                        // 更新UI
                        if (currentAddress != null && !currentAddress.isEmpty()) {
                            Log.d(TAG, "onLocationChanged: " + currentAddress);
                            // 有地址，直接显示
                            new Handler(Looper.getMainLooper()).post(() -> {
                                binding.tvPhysicalLocationText.setText(currentAddress);
                                binding.tvPhysicalLocationText.setTextColor(Color.parseColor("#1F2937"));
                            });
                            // 停止定位
                            locationClient.stopLocation();
                        } else {
                            // 没有地址，检查是否超时
                            if (System.currentTimeMillis() - locationStartTime < LOCATION_TIMEOUT_MS) {
                                // 还在超时时间内，尝试重新定位
                                Log.d(TAG, "地址为空，继续尝试定位...");
                                locationClient.startLocation();
                            } else {
                                // 超时了，停止定位
                                locationClient.stopLocation();
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    binding.tvPhysicalLocationText.setText("地址获取失败，请手动选点");
                                    binding.tvPhysicalLocationText.setTextColor(Color.parseColor("#EF4444"));
                                });
                            }
                        }
                    }
                }
            });

            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setOnceLocation(true);
            option.setNeedAddress(true);
            locationClient.setLocationOption(option);

            // 记录开始时间
            locationStartTime = System.currentTimeMillis();

            locationClient.startLocation();

            // 更新UI显示
            binding.tvPhysicalLocationText.setText("正在获取当前位置...");
        } catch (Exception e) {
            e.printStackTrace();
            binding.tvPhysicalLocationText.setText("定位失败");
        }
    }

    @Override
    public void showPublishSuccess() {
        // TODO: 实现发布成功后的 UI 反馈
        Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void showLocation(String address, double lat, double lng) {
        // TODO: 更新 UI 显示位置信息
    }

    // ========== BaseView 默认实现 ==========

    @Override
    public void showError(String message) {
        // TODO: 实现错误信息的 UI 展示
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
        // 解绑 Presenter，防止内存泄漏
        if (presenter != null) {
            presenter.detachView();
        }
    }
}
