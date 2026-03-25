package com.ggg.rememo.feature.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ggg.rememo.feature.profile.databinding.DialogSettingsBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SettingsDialogFragment extends BottomSheetDialogFragment {
    private DialogSettingsBinding binding;

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

        binding.ivCloseSettings.setOnClickListener(v -> dismiss());
        binding.btnLogout.setOnClickListener(v -> {
            // TODO: 退出登录
        });
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
