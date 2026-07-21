package com.ggg.rememo.feature.profile;

import android.graphics.Color;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.base.BaseFragment;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.ui.auth.LoginRequiredPrompt;
import com.ggg.rememo.feature.profile.adapter.ProfileMemoryAdapter;
import com.ggg.rememo.feature.profile.contract.ProfileContract;
import com.ggg.rememo.feature.profile.databinding.FragmentProfileHomeBinding;
import com.ggg.rememo.feature.profile.decoration.ProfileTimelineDecoration;
import com.ggg.rememo.feature.profile.presenter.ProfilePresenter;

import java.util.List;

@Route(path = Routes.Profile.HOME_FRAGMENT)
public class ProfileHomeFragment extends BaseFragment<
        FragmentProfileHomeBinding,
        ProfileContract.View,
        ProfilePresenter> implements ProfileContract.View {
    private ProfileMemoryAdapter memoryAdapter;
    private User currentUserInfo;
    private boolean userDataLoaded;
    private boolean initialResumeHandled;

    @Override
    protected FragmentProfileHomeBinding inflateBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileHomeBinding.inflate(inflater, container, false);
    }

    @Override
    protected ProfilePresenter createPresenter() {
        return new ProfilePresenter();
    }

    @Override
    protected ProfileContract.View getViewContract() {
        return this;
    }

    @Override
    protected void initView() {
        handleWindowInsets();
        initScrollEffect();
        initClickListeners();
        initRecyclerView();
    }

    @Override
    protected void initData() {
        renderLoginState();
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean wasLoaded = userDataLoaded;
        renderLoginState();
        if (initialResumeHandled && wasLoaded && TokenManager.isLoggedIn() && !isHidden()
                && presenter != null) {
            // 从发布页等外部页面返回且个人主页当前可见时，重新读取 Room。
            presenter.loadUserMemories();
        }
        initialResumeHandled = true;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden || !isUIActive()) {
            return;
        }
        boolean wasLoaded = userDataLoaded;
        renderLoginState();
        if (wasLoaded && TokenManager.isLoggedIn() && presenter != null) {
            // MainActivity 通过 hide/show 复用 Fragment，重新显示时必须主动刷新缓存。
            presenter.loadUserMemories();
        }
    }

    private void renderLoginState() {
        if (TokenManager.isLoggedIn()) {
            showLoggedInState();
            if (!userDataLoaded) {
                // 立即尝试从本地缓存加载，避免闪烁
                loadAvatarFromCache();

                // 记忆列表和用户信息只在初始化时加载一次，后续由 Glide 缓存直接命中
                presenter.loadUserInfo();
                presenter.loadUserMemories();
                userDataLoaded = true;
            }
        } else {
            showGuestState();
        }
    }

    private void showGuestState() {
        userDataLoaded = false;
        currentUserInfo = null;
        getBinding().appBarLayout.setVisibility(View.GONE);
        getBinding().rvMemories.setVisibility(View.GONE);
        getBinding().layoutGuestProfile.setVisibility(View.VISIBLE);
        if (memoryAdapter != null) {
            memoryAdapter.clear();
        }
    }

    private void showLoggedInState() {
        getBinding().layoutGuestProfile.setVisibility(View.GONE);
        getBinding().appBarLayout.setVisibility(View.VISIBLE);
        getBinding().rvMemories.setVisibility(View.VISIBLE);
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
                                // 直接用 Glide 加载，不使用 placeholder
                                Glide.with(ProfileHomeFragment.this)
                                        .load(user.getAvatar())
                                        .dontAnimate()
                                        .into(getBinding().ivAvatar);
                                Glide.with(ProfileHomeFragment.this)
                                        .load(user.getAvatar())
                                        .dontAnimate()
                                        .into(getBinding().ivToolbarSmallAvatar);
                                getBinding().ivAvatar.setTag(user.getAvatar());
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

    private void initClickListeners() {
        getChildFragmentManager().setFragmentResultListener(
                SettingsDialogFragment.REQUEST_LOGOUT,
                this,
                (requestKey, result) -> renderLoginState());

        getBinding().btnEditProfile.setOnClickListener(v -> {
            LoginRequiredPrompt.requireLogin(requireActivity(), "编辑资料", () ->
                    EditProfileDialogFragment.newInstance(currentUserInfo, updatedUserInfo -> {
                        if (updatedUserInfo != null && presenter != null) {
                            // 直接使用更新后的数据刷新 UI，避免重新请求
                            presenter.showUserInfoDirectly(updatedUserInfo);
                        }
                    }).show(getChildFragmentManager(), "edit_profile"));
        });

        getBinding().ivSettings.setOnClickListener(v -> {
            new SettingsDialogFragment().show(getChildFragmentManager(), "settings");
        });

        getBinding().btnGuestLogin.setOnClickListener(v ->
                LoginRequiredPrompt.navigateToLogin(requireActivity()));
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
        getBinding().toolbar.setBackgroundColor(Color.argb(0, red, green, blue));
        getBinding().ivToolbarSmallAvatar.setAlpha(0f);

        getBinding().appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float absOffset = Math.abs(verticalOffset);
            float totalRange = appBarLayout.getTotalScrollRange();

            // Toolbar 背景颜色渐变
            float colorEndThreshold = totalRange * 0.4f;
            float fraction = Math.min(absOffset / colorEndThreshold, 1.0f);

            int alpha = (int) (fraction * 255);
            getBinding().toolbar.setBackgroundColor(Color.argb(alpha, red, green, blue));

            // 内容区淡出
            float contentAlpha = 1.0f - fraction;
            getBinding().ivAvatar.setAlpha(contentAlpha);
            getBinding().tvName.setAlpha(contentAlpha);
            getBinding().tvBio.setAlpha(contentAlpha);
            getBinding().llTags.setAlpha(contentAlpha);
            getBinding().cardStats.setAlpha(contentAlpha);

            // 小头像在中位浮现
            if (fraction > 0.75f) {
                float avatarAlpha = (fraction - 0.75f) / 0.25f;
                getBinding().ivToolbarSmallAvatar.setAlpha(avatarAlpha);
            } else {
                getBinding().ivToolbarSmallAvatar.setAlpha(0f);
            }
        });
    }

    /**
     * 处理窗口内边距（状态栏适配）
     */
    private void handleWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            getBinding().toolbar.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });
    }

    /**
     * 初始化"我的时光"RecyclerView：LinearLayoutManager + SpineDecoration + Adapter
     */
    private void initRecyclerView() {
        memoryAdapter = new ProfileMemoryAdapter();
        memoryAdapter.setOnItemClickListener(post -> {
            if (presenter != null) {
                presenter.onMemoryClicked(post.getPostId());
            }
        });
        getBinding().rvMemories.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().rvMemories.addItemDecoration(new ProfileTimelineDecoration(requireContext()));
        getBinding().rvMemories.setAdapter(memoryAdapter);
    }

    @Override
    public void showUserInfo(User user) {
        if (user == null) return;
        currentUserInfo = user;
        getBinding().tvName.setText(user.getNickname() != null ? user.getNickname() : "");
        getBinding().tvBio.setText(user.getBio() != null ? user.getBio() : "");
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            getBinding().ivAvatar.setTag(user.getAvatar());
            Glide.with(this)
                    .load(user.getAvatar())
                    .into(getBinding().ivAvatar);
            Glide.with(this)
                    .load(user.getAvatar())
                    .into(getBinding().ivToolbarSmallAvatar);
        } else {
            Glide.with(this)
                    .load(com.ggg.rememo.core.ui.R.drawable.avatar_placeholder)
                    .into(getBinding().ivAvatar);
            Glide.with(this)
                    .load(com.ggg.rememo.core.ui.R.drawable.avatar_placeholder)
                    .into(getBinding().ivToolbarSmallAvatar);
        }
    }

    @Override
    public void showUpdateSuccess() {
        // 旧方法兼容，不再使用
    }

    @Override
    public void showUpdateSuccessWithData(User user) {
        if (user == null) return;
        currentUserInfo = user;
        showUserInfo(user);
    }

    @Override
    public void navigateToLogin() {
        renderLoginState();
    }

    @Override
    public void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // ========== "我的时光" View 实现 ==========

    @Override
    public void showMemories(List<MemoryPost> posts) {
        if (memoryAdapter == null) return;
        memoryAdapter.setData(posts);
    }

    @Override
    public void showMemoriesEmpty() {
        if (memoryAdapter == null) return;
        memoryAdapter.clear();
    }

    @Override
    public void navigateToMemoryDetail(String postId) {
        ARouter.getInstance()
                .build(Routes.Detail.HOME)
                .withString(Routes.Detail.EXTRA_POST_ID, postId)
                .navigation();
    }
}
