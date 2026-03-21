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
import com.ggg.rememo.feature.here.contract.MemoryPointBottomSheetContract;
import com.ggg.rememo.feature.here.databinding.FragmentMemoryPointBottomSheetBinding;
import com.ggg.rememo.feature.here.presenter.MemoryPointBottomSheetPresenter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MemoryPointBottomSheetFragment extends BottomSheetDialogFragment
        implements MemoryPointBottomSheetContract.View {

    private static final String ARG_POINT = "arg_point";

    private FragmentMemoryPointBottomSheetBinding binding;
    private MemoryPointBottomSheetPresenter presenter;

    public static MemoryPointBottomSheetFragment newInstance(MemoryPoint point) {
        MemoryPointBottomSheetFragment fragment = new MemoryPointBottomSheetFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_POINT, point);
        fragment.setArguments(args);
        return fragment;
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

        presenter = new MemoryPointBottomSheetPresenter();
        presenter.attachView(this);

        MemoryPoint point = null;
        if (getArguments() != null) {
            point = getArguments().getParcelable(ARG_POINT);
        }

        presenter.onViewCreated(point);
        setupListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            Window window = dialog.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    private void setupListeners() {
        binding.btnEnterTimeline.setOnClickListener(v -> presenter.onEnterTimelineClicked());
        binding.getRoot().setOnClickListener(v -> dismiss());
    }

    // ==================== MemoryPointBottomSheetContract.View 实现 ====================

    @Override
    public void showPointInfo(MemoryPoint point) {
        if (point == null) {
            dismiss();
            return;
        }

        String coverUrl = point.getCoverImageUrl();
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

        binding.tvMemoryCount.setText(point.getMemoryCount() + " 记忆");
        binding.tvTitle.setText(point.getPointName() != null ? point.getPointName() : "未命名地点");
        binding.tvLocation.setText(point.getLocationAddress() != null ? point.getLocationAddress() : "");

        String summary = point.getSummaryText();
        if (summary != null && !summary.isEmpty()) {
            binding.tvAiSummary.setText(summary);
        }
    }

    @Override
    public void navigateToTimeline(String pointId) {
        // TODO: 跳转到时间线页面
        dismiss();
    }

    @Override
    public void showError(String message) {
        // BottomSheet 场景下可直接使用 Toast
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
        binding = null;
    }
}
