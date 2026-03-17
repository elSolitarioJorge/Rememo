package com.ggg.rememo.feature.publish;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.model.MemoryPhoto;
import com.ggg.rememo.feature.publish.contract.PublishContract;
import com.ggg.rememo.feature.publish.databinding.ActivityPublishHomeBinding;
import com.ggg.rememo.feature.publish.presenter.PublishPresenter;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Route(path = Routes.Publish.HOME)
public class PublishHomeActivity extends AppCompatActivity implements PublishContract.View {
    private ActivityPublishHomeBinding binding;
    private PublishPresenter presenter;
    private PhotoThumbnailAdapter photoAdapter;
    private MemoryPhoto currentSelectedPhoto;


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
