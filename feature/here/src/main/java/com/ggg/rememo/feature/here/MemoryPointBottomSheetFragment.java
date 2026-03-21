package com.ggg.rememo.feature.here;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.feature.here.databinding.FragmentMemoryPointBottomSheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * 地图 Marker 点击后弹出的底部卡片
 *
 * <p>展示单个 MemoryPoint 的概览信息，包括：
 * <ul>
 *   <li>封面图 + 记忆数量标签</li>
 *   <li>地点名称 + 详细地址</li>
 *   <li>AI 时空拾取摘要</li>
 *   <li>「进入时空长河」跳转按钮</li>
 * </ul>
 */
public class MemoryPointBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_POINT = "arg_point";

    private FragmentMemoryPointBottomSheetBinding binding;
    private MemoryPoint memoryPoint;

    public static MemoryPointBottomSheetFragment newInstance(MemoryPoint point) {
        MemoryPointBottomSheetFragment fragment = new MemoryPointBottomSheetFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_POINT, point);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getTheme() {
        return R.style.BottomSheetTheme;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMemoryPointBottomSheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) {
            memoryPoint = getArguments().getParcelable(ARG_POINT);
        }

        if (memoryPoint == null) {
            dismiss();
            return;
        }

        bindData();
        setupListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            // 去掉背后变暗的效果(Dim)
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void bindData() {
        // 封面图
        String coverUrl = memoryPoint.getCoverImageUrl();
        if (coverUrl != null && !coverUrl.isEmpty()) {
            Glide.with(this)
                    .load(coverUrl)
                    .centerCrop()
                    .placeholder(R.drawable.pic_old)
                    .error(R.drawable.pic_old)
                    .into(binding.ivHeroImage);
        } else {
            binding.ivHeroImage.setImageResource(R.drawable.pic_old);
        }

        // 记忆数量
        int count = memoryPoint.getMemoryCount();
        binding.tvMemoryCount.setText(count + " 记忆");

        // 地点名称
        String pointName = memoryPoint.getPointName();
        binding.tvTitle.setText(pointName != null ? pointName : "未命名地点");

        // 详细地址
        String address = memoryPoint.getLocationAddress();
        binding.tvLocation.setText(address != null ? address : "");

        // AI 摘要
        String summary = memoryPoint.getSummaryText();
        if (summary != null && !summary.isEmpty()) {
            binding.tvAiSummary.setText(summary);
        }
    }

    private void setupListeners() {
        // 进入时空长河按钮
        binding.btnEnterTimeline.setOnClickListener(v -> {
            // TODO: 跳转到时间线页面，传入 pointId
            dismiss();
        });

        // 点击卡片背景关闭
        binding.getRoot().setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
