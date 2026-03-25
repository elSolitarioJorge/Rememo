package com.ggg.rememo.feature.profile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.feature.profile.databinding.FragmentProfileHomeBinding;

@Route(path = Routes.Profile.HOME_FRAGMENT)
public class ProfileHomeFragment extends Fragment {
    private FragmentProfileHomeBinding binding;

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
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, insets.top, 0, 0);
            return windowInsets;
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
