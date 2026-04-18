package com.ggg.rememo.feature.publish;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.base.BaseActivity;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.local.ImageStorageHelper;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.map.LocationPickerActivity;
import com.ggg.rememo.feature.publish.adapter.PhotoThumbnailAdapter;
import com.ggg.rememo.feature.publish.contract.PublishContract;
import com.ggg.rememo.feature.publish.databinding.ActivityPublishHomeBinding;
import com.ggg.rememo.feature.publish.databinding.DialogTimeSelectionBinding;
import com.ggg.rememo.feature.publish.presenter.PublishPresenter;
import com.ggg.rememo.feature.publish.util.BaiduAiUtils;

import java.util.List;

@Route(path = Routes.Publish.HOME)
public class PublishHomeActivity extends BaseActivity<
        ActivityPublishHomeBinding,
        PublishContract.View,
        PublishPresenter>
        implements PublishContract.View {
    private static final String TAG = "PublishHomeActivity1";
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

    // 接收 ARouter 传递的参数
    private String inputPointId;
    private double inputLat;
    private double inputLng;
    private String inputAddress;
    private String inputPointName;


    // 定义选点结果接收器
    private final ActivityResultLauncher<Intent> mapSelectLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    // 接收从选点页面传回来的数据
                    String selectedAddress = result.getData().getStringExtra("address");
                    currentLat = result.getData().getDoubleExtra("lat", 0.0);
                    currentLng = result.getData().getDoubleExtra("lng", 0.0);

                    // 更新 UI
                    getBinding().tvPhysicalLocationText.setText(selectedAddress);
                    getBinding().tvPhysicalLocationText.setTextColor(Color.parseColor("#D97706")); // 变高亮
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(9), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    handleImagesSelected(uris);
                }
            });


    @Override
    protected ActivityPublishHomeBinding inflateBinding(@NonNull LayoutInflater inflater) {
        return ActivityPublishHomeBinding.inflate(getLayoutInflater());
    }

    @Override
    protected PublishPresenter createPresenter() {
        return new PublishPresenter();
    }

    @Override
    protected PublishContract.View getViewContract() {
        return this;
    }


    @Override
    protected void initView() {
        initRecyclerView();
        initListeners();
    }

    @Override
    protected void initData() {
        // ARouter 参数注入
        ARouter.getInstance().inject(this);

        // 从 ARouter 获取参数
        Bundle args = getIntent().getExtras();
        if (args != null) {
            inputPointId = args.getString(Routes.Publish.EXTRA_POINT_ID, null);
            inputLat = args.getDouble(Routes.Publish.EXTRA_LAT, 0.0);
            inputLng = args.getDouble(Routes.Publish.EXTRA_LNG, 0.0);
            inputAddress = args.getString(Routes.Publish.EXTRA_ADDRESS, null);
            inputPointName = args.getString(Routes.Publish.EXTRA_POINT_NAME, null);
        }
        // 如果有传入位置信息，使用传入的位置；否则启动定位
        if (inputLat != 0.0 && inputLng != 0.0) {
            currentLat = inputLat;
            currentLng = inputLng;
            currentAddress = (inputAddress != null) ? inputAddress : "";
            if (!currentAddress.isEmpty()) {
                getBinding().tvPhysicalLocationText.setText(currentAddress);
                getBinding().tvPhysicalLocationText.setTextColor(Color.parseColor("#D97706"));
                getBinding().btnSelectLocation.setEnabled(false);
            }
            if (inputPointName != null && !inputPointName.isEmpty()) {
                getBinding().etAnchorName.setText(inputPointName);
                getBinding().etAnchorName.setFocusable(false);
                getBinding().etAnchorName.setFocusableInTouchMode(false);
            }
        } else {
            // 启动定位，获取当前位置信息
            initLocationAndStart();
        }
    }

    @Override
    protected void applySystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initRecyclerView() {
        photoAdapter = new PhotoThumbnailAdapter();
        getBinding().rvPhotoThumbnails.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        getBinding().rvPhotoThumbnails.setAdapter(photoAdapter);

        photoAdapter.setOnPhotoClickListener(new PhotoThumbnailAdapter.OnPhotoClickListener() {
            @Override
            public void onPhotoSelected(MemoryPhoto photo) {
                currentSelectedPhoto = photo;

                if (photo.getRestoredUrl() != null && !photo.getRestoredUrl().isEmpty()) {
                    getBinding().btnRunAiRepair.setVisibility(View.GONE);
                    getBinding().btnSwitch.setVisibility(View.VISIBLE);
                } else {
                    getBinding().btnRunAiRepair.setVisibility(View.VISIBLE);
                    getBinding().btnSwitch.setVisibility(View.GONE);
                }

                updateSwitchButtonUI();

                Glide.with(PublishHomeActivity.this)
                        .load(photo.getDisplayUrl())
                        .into(getBinding().ivDemoImage);
            }

            @Override
            public void onAddMoreClicked() {
                launchPhotoPicker();
            }

            @Override
            public void onPhotoDeleted(MemoryPhoto removed, int newSelectedPosition) {
                if (photoAdapter.getPhotos().isEmpty()) {
                    currentSelectedPhoto = null;
                    getBinding().btnRunAiRepair.setVisibility(View.VISIBLE);
                    getBinding().btnSwitch.setVisibility(View.GONE);
                    getBinding().ivDemoImage.setImageDrawable(null);
                } else if (currentSelectedPhoto == removed) {
                    if (!photoAdapter.getPhotos().isEmpty()) {
                        currentSelectedPhoto = photoAdapter.getPhotos()
                                .get(Math.min(newSelectedPosition, photoAdapter.getPhotos().size() - 1));
                        Glide.with(PublishHomeActivity.this)
                                .load(currentSelectedPhoto.getDisplayUrl())
                                .into(getBinding().ivDemoImage);
                        if (currentSelectedPhoto.getRestoredUrl() != null
                                && !currentSelectedPhoto.getRestoredUrl().isEmpty()) {
                            getBinding().btnRunAiRepair.setVisibility(View.GONE);
                            getBinding().btnSwitch.setVisibility(View.VISIBLE);
                        } else {
                            getBinding().btnRunAiRepair.setVisibility(View.VISIBLE);
                            getBinding().btnSwitch.setVisibility(View.GONE);
                        }
                        updateSwitchButtonUI();
                    }
                }
            }
        });
    }

    private void initListeners() {
        // 点击上传卡片
        getBinding().cardRepairContainer.setOnClickListener(v -> {
            if (photoAdapter.getPhotos().isEmpty()) {
                launchPhotoPicker();
            }
        });

        // 开启跑马灯
        getBinding().tvPhysicalLocationText.setSelected(true);

        // 启动 AI 修复
        getBinding().btnRunAiRepair.setOnClickListener(v -> {
            if (currentSelectedPhoto == null) return;

            getBinding().btnRunAiRepair.setText("AI 引擎处理中...");
            getBinding().btnRunAiRepair.setEnabled(false);

            // 开启子线程进行网络请求
            new Thread(() -> {
                try {
                    // 生成稳定的 AI 修复图存储路径（files/images/restored/）
                    String restoredPath = ImageStorageHelper.generateRestoredImagePath(PublishHomeActivity.this);
                    // 直接从内部文件路径读取，调用百度 API 并保存结果
                    String restoredFilePath = BaiduAiUtils.colourizeWithOutputPath(
                            PublishHomeActivity.this, currentSelectedPhoto.getOriginalUrl(), restoredPath);

                    // 切回主线程更新 UI
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 更新 Model 状态
                        currentSelectedPhoto.setRestoredUrl(restoredFilePath);
                        if (currentSelectedPhoto.getCurrentState() == MemoryPhoto.PhotoState.ORIGINAL) {
                            currentSelectedPhoto.toggleState();
                        }

                        // 更新 UI
                        getBinding().btnRunAiRepair.setText("启动 AI 修复与上色");
                        getBinding().btnRunAiRepair.setEnabled(true);
                        getBinding().btnRunAiRepair.setVisibility(View.GONE);

                        getBinding().btnSwitch.setVisibility(View.VISIBLE);
                        updateSwitchButtonUI();

                        // Glide 能够直接加载本地的 File 路径
                        Glide.with(PublishHomeActivity.this)
                                .load(currentSelectedPhoto.getDisplayUrl())
                                .into(getBinding().ivDemoImage);

                        Toast.makeText(PublishHomeActivity.this, "记忆重绘完成！", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    // 发生错误时，也要切回主线程恢复按钮状态并提示用户
                    new Handler(Looper.getMainLooper()).post(() -> {
                        getBinding().btnRunAiRepair.setText("启动 AI 修复与上色");
                        getBinding().btnRunAiRepair.setEnabled(true);
                        Toast.makeText(PublishHomeActivity.this, "修复失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        });

        // 切换视图按钮
        getBinding().btnSwitch.setOnClickListener(v -> {
            if (currentSelectedPhoto == null) return;

            currentSelectedPhoto.toggleState();

            Glide.with(PublishHomeActivity.this)
                    .load(currentSelectedPhoto.getDisplayUrl())
                    .into(getBinding().ivDemoImage);

            updateSwitchButtonUI();
        });

        // 退出按钮
        getBinding().btnBack.setOnClickListener(v -> finish());

        // 选择时间
        getBinding().layoutSelectTime.setOnClickListener(v -> showTimeSelectionDialog());

        // 选择位置按钮
        getBinding().btnSelectLocation.setOnClickListener(v -> {
            // 跳转到选点页面，传入当前已选位置作为初始位置
            Intent intent = new Intent(PublishHomeActivity.this, LocationPickerActivity.class);
            // 把当前的经纬度传过去，让地图打开时中心点就是当前位置
            intent.putExtra("lat", currentLat);
            intent.putExtra("lng", currentLng);
            mapSelectLauncher.launch(intent);
        });

        // 发布按钮
        getBinding().btnPublish.setOnClickListener(v -> handlePublish());
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
        getBinding().layoutUploadPlaceholder.setVisibility(View.GONE);
        getBinding().ivDemoImage.setVisibility(View.VISIBLE);
        getBinding().rvPhotoThumbnails.setVisibility(View.VISIBLE);

        photoAdapter.addPhotos(this, uris);
    }

    private void updateSwitchButtonUI() {
        if (currentSelectedPhoto == null) return;

        if (currentSelectedPhoto.getCurrentState() == MemoryPhoto.PhotoState.RESTORED) {
            getBinding().btnSwitch.setText("看原图");
        } else {
            getBinding().btnSwitch.setText("看修复图");
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
            getBinding().tvTimeText.setText(finalTime);
            getBinding().tvTimeText.setTextColor(Color.parseColor("#F5A623"));
            getBinding().tvTimeText.setTypeface(null, Typeface.BOLD);
            getBinding().tvTimeText.setTextSize(14f);

            dialog.dismiss();
        });

        dialog.show();
    }

    // 根据选中的文本，刷新 4 个按钮的背景和字体颜色
    private void updateSeasonUI(TextView[] seasonViews, String selected) {
        for (TextView view : seasonViews) {
            if (view.getText().toString().equals(selected)) {
                /* 选中状态：套用黄色圆角背景，字体变深琥珀色并加粗
                view.setBackgroundResource(R.drawable.bg_season_selected);
                view.setTextColor(Color.parseColor("#D97706"));*/
                view.setSelected(true);
                view.setTypeface(null, Typeface.BOLD);
            } else {
                /* 未选中状态：套用灰色圆角背景，字体变浅灰色恢复正常
                view.setBackgroundResource(R.drawable.bg_season_normal);
                view.setTextColor(Color.parseColor("#6B7280"));*/
                view.setSelected(false);
                view.setTypeface(null, Typeface.NORMAL);
            }
        }
    }

    // ========== 发布相关方法 ==========
    private void handlePublish() {
        // View 层：只负责收集 UI 数据，调用 Presenter
        String title = getBinding().etMemoryTitle.getText().toString().trim();
        String content = getBinding().etMemoryContent.getText().toString().trim();
        List<MemoryPhoto> photos = photoAdapter.getPhotos();
        String pointName = getBinding().etAnchorName.getText().toString().trim();

        // 调用 Presenter 处理发布
        presenter.publish(title, content, photos, currentLat, currentLng, inputPointId, pointName);
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
                        if (!currentAddress.isEmpty()) {
                            Log.d(TAG, "onLocationChanged: " + currentAddress);
                            // 有地址，直接显示
                            new Handler(Looper.getMainLooper()).post(() -> {
                                getBinding().tvPhysicalLocationText.setText(currentAddress);
                                getBinding().tvPhysicalLocationText.setTextColor(Color.parseColor("#D97706"));
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
                                    getBinding().tvPhysicalLocationText.setText("地址获取失败，请手动选点");
                                    getBinding().tvPhysicalLocationText.setTextColor(Color.parseColor("#EF4444"));
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
            getBinding().tvPhysicalLocationText.setText("正在获取当前位置...");
        } catch (Exception e) {
            e.printStackTrace();
            getBinding().tvPhysicalLocationText.setText("定位失败");
        }
    }

    @Override
    public void showPublishSuccess() {
        getBinding().layoutUploadProgress.setVisibility(View.GONE);
        getBinding().btnPublish.setEnabled(true);
        Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void showUploadProgress(int current, int total) {
        getBinding().layoutUploadProgress.setVisibility(View.VISIBLE);
        getBinding().tvUploadProgress.setText("正在上传第 " + current + "/" + total + " 张...");
        getBinding().progressBarUpload.setMax(total);
        getBinding().progressBarUpload.setProgress(current);
        getBinding().btnPublish.setEnabled(false);
    }

    // ========== View 接口实现 - 供 Presenter 调用 ==========
    @Override
    public String getAddress() {
        return getBinding().tvPhysicalLocationText.getText().toString().trim();
    }

    @Override
    public String getTimeDisplayText() {
        return getBinding().tvTimeText.getText().toString();
    }

    // ========== BaseView 默认实现 ==========

    @Override
    public void showError(String message) {
        getBinding().layoutUploadProgress.setVisibility(View.GONE);
        getBinding().btnPublish.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        // 停止定位并销毁客户端，防止异步回调
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        super.onDestroy();
    }
}
