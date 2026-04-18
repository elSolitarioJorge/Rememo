package com.ggg.rememo.feature.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.feature.profile.contract.ProfileContract;
import com.ggg.rememo.feature.profile.databinding.DialogSettingsBinding;
import com.ggg.rememo.feature.profile.presenter.ProfilePresenter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class SettingsDialogFragment extends BottomSheetDialogFragment implements ProfileContract.View {

    private DialogSettingsBinding binding;
    private ProfilePresenter presenter;

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
        binding = DialogSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new ProfilePresenter();
        presenter.attachView(this);

        binding.ivCloseSettings.setOnClickListener(v -> dismiss());
        binding.btnLogout.setOnClickListener(v -> confirmLogout());
        binding.btnAccountSecurity.setOnClickListener(v -> {
            // TODO
        });
        binding.btnPrivacy.setOnClickListener(v -> {
            // TODO
        });
        binding.btnClearCache.setOnClickListener(v -> {
            // TODO
        });
        binding.btnAbout.setOnClickListener(v -> {
            // TODO
        });
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("退出登录")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    if (presenter != null) {
                        presenter.logout();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
        binding = null;
    }

    // ========== ProfileContract.View 实现 ==========

    @Override
    public void showUserInfo(User user) {
        // 设置页不需要展示用户信息
    }

    @Override
    public void showUpdateSuccess() {
        // 设置页不需要实现
    }

    @Override
    public void navigateToLogin() {
        ARouter.getInstance()
                .build(Routes.Auth.LOGIN)
                .withFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .navigation();
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void showMemories(List<MemoryPost> posts) {

    }
    @Override
    public void showMemoriesEmpty() {

    }
    @Override
    public void navigateToMemoryDetail(String postId) {

    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
