package com.ggg.rememo.feature.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.core.data.model.network.response.UserInfo;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.feature.profile.contract.ProfileContract;
import com.ggg.rememo.feature.profile.databinding.FragmentProfileHomeBinding;
import com.ggg.rememo.feature.profile.presenter.ProfilePresenter;

@Route(path = Routes.Profile.HOME_FRAGMENT)
public class ProfileHomeFragment extends Fragment implements ProfileContract.View {

    private FragmentProfileHomeBinding binding;
    private ProfilePresenter presenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        handleWindowInsets();
        initScrollEffect();
        initClickListeners();

        presenter = new ProfilePresenter();
        presenter.attachView(this);

        // 立即尝试从本地缓存加载，避免闪烁
        loadAvatarFromCache();
    }

    /**
     * 从本地缓存加载头像（同步操作），避免 Glide 异步加载闪烁
     */
    private void loadAvatarFromCache() {
        // 直接读取本地缓存并设置图片
        new com.ggg.rememo.feature.profile.data.ProfileRepository().getLocalUser(
                new ApiCallback<User>() {
                    @Override
                    public void onSuccess(com.ggg.rememo.core.data.model.entity.User user) {
                        if (user != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                            requireActivity().runOnUiThread(() -> {
                                if (binding != null) {
                                    // 直接用 Glide 加载，不使用 placeholder
                                    Glide.with(ProfileHomeFragment.this)
                                            .load(user.getAvatar())
                                            .dontAnimate()
                                            .into(binding.ivAvatar);
                                    Glide.with(ProfileHomeFragment.this)
                                            .load(user.getAvatar())
                                            .dontAnimate()
                                            .into(binding.ivToolbarSmallAvatar);
                                    binding.ivAvatar.setTag(user.getAvatar());
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        // 忽略，本地无缓存时由网络请求处理
                    }
                }
        );
    }

    @Override
    public void onResume() {
        super.onResume();
        if (presenter != null) {
            presenter.loadUserInfo();
        }
    }

    private void initClickListeners() {
        binding.btnEditProfile.setOnClickListener(v -> {
            String avatar = "";
            if (binding.ivAvatar.getDrawable() != null) {
                Object tag = binding.ivAvatar.getTag();
                if (tag instanceof String && !((String) tag).isEmpty()) {
                    avatar = (String) tag;
                }
            }
            EditProfileDialogFragment.newInstance(avatar, updatedUserInfo -> {
                if (updatedUserInfo != null && presenter != null) {
                    // 直接使用更新后的数据刷新 UI，避免重新请求
                    presenter.showUserInfoDirectly(updatedUserInfo);
                }
            }).show(getChildFragmentManager(), "edit_profile");
        });

        binding.ivSettings.setOnClickListener(v -> {
            new SettingsDialogFragment().show(getChildFragmentManager(), "settings");
        });
    }

    /**
     * 设置 AppBarLayout 的滑动监听，实现 Toolbar 背景渐变和头像切换
     */
    private void initScrollEffect() {
        // 获取基础背景色
        final int baseColor = ContextCompat.getColor(requireContext(), R.color.brand_bg);
        final int red = Color.red(baseColor);
        final int green = Color.green(baseColor);
        final int blue = Color.blue(baseColor);

        // 初始状态：Toolbar 设为透明
        binding.toolbar.setBackgroundColor(Color.argb(0, red, green, blue));
        binding.ivToolbarSmallAvatar.setAlpha(0f);

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            // 判空保护，防止在 Fragment 销毁后仍回调
            if (binding == null) return;

            float absOffset = Math.abs(verticalOffset);
            float totalRange = appBarLayout.getTotalScrollRange();

            // Toolbar 背景颜色渐变
            float colorEndThreshold = totalRange * 0.4f;
            float fraction = Math.min(absOffset / colorEndThreshold, 1.0f);

            int alpha = (int) (fraction * 255);
            binding.toolbar.setBackgroundColor(Color.argb(alpha, red, green, blue));

            // 内容区淡出
            float contentAlpha = 1.0f - fraction;
            binding.ivAvatar.setAlpha(contentAlpha);
            binding.tvName.setAlpha(contentAlpha);
            binding.tvBio.setAlpha(contentAlpha);
            binding.llTags.setAlpha(contentAlpha);
            binding.cardStats.setAlpha(contentAlpha);

            // 小头像在中位浮现
            if (fraction > 0.75f) {
                float avatarAlpha = (fraction - 0.75f) / 0.25f;
                binding.ivToolbarSmallAvatar.setAlpha(avatarAlpha);
            } else {
                binding.ivToolbarSmallAvatar.setAlpha(0f);
            }
        });
    }

    /**
     * 处理窗口内边距（状态栏适配）
     */
    private void handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            binding.toolbar.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });
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
    public void showLoading() {
    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void showUserInfo(UserInfo userInfo) {
        if (binding == null || userInfo == null) return;
        binding.tvName.setText(userInfo.getNickname() != null ? userInfo.getNickname() : "");
        binding.tvBio.setText(userInfo.getBio() != null ? userInfo.getBio() : "");
        if (userInfo.getAvatar() != null && !userInfo.getAvatar().isEmpty()) {
            binding.ivAvatar.setTag(userInfo.getAvatar());
            // 不使用 placeholder，避免闪烁
            Glide.with(this)
                    .load(userInfo.getAvatar())
                    .into(binding.ivAvatar);
            Glide.with(this)
                    .load(userInfo.getAvatar())
                    .into(binding.ivToolbarSmallAvatar);
        } else {
            Glide.with(this)
                    .load(com.ggg.rememo.core.ui.R.drawable.avatar_placeholder)
                    .into(binding.ivAvatar);
            Glide.with(this)
                    .load(com.ggg.rememo.core.ui.R.drawable.avatar_placeholder)
                    .into(binding.ivToolbarSmallAvatar);
        }
    }

    @Override
    public void showUpdateSuccess() {
        // 旧方法兼容，不再使用
    }

    @Override
    public void showUpdateSuccessWithData(UserInfo userInfo) {
        if (binding == null || userInfo == null) return;
        showUserInfo(userInfo);
    }

    @Override
    public void navigateToLogin() {
        // 本 Fragment 不处理跳转，由 SettingsDialogFragment 处理
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
