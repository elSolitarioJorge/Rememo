package com.ggg.rememo.feature.profile;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ggg.rememo.feature.profile.databinding.DialogEditProfileBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class EditProfileDialogFragment extends BottomSheetDialogFragment {
    private DialogEditProfileBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme);
    }

    @Override
    public void onStart() {
        super.onStart();
        View bottomSheet = getDialog() != null ?
                getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet) : null;
        if (bottomSheet != null) {
            // 强制展开到最大高度
            BottomSheetBehavior.from(bottomSheet)
                    .setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.ivClose.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> {
            // TODO: 保存逻辑
            dismiss();
        });
        binding.etGender.setOnClickListener(v -> selectGender());
    }

    private void selectGender() {
        String[] genderOptions = {"保密", "男", "女"};
        String currentChoice = binding.etGender.getText().toString();

        // 动态创建一个符合暗色主题的 BottomSheetDialog
        BottomSheetDialog genderDialog = new BottomSheetDialog(requireContext());

        // 创建容器
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);

        // 设置圆角暗色背景
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#16192B"));
        bg.setCornerRadii(new float[]{60, 60, 60, 60, 0, 0, 0, 0});
        container.setBackground(bg);
        container.setPadding(0, 40, 0, 20);

        // 标题
        TextView title = new TextView(requireContext());
        title.setText("选择性别");
        title.setTextColor(Color.parseColor("#64748B")); // brand-text-sub
        title.setTextSize(13);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        container.addView(title);

        // 选项列表
        for (String option : genderOptions) {
            TextView tv = new TextView(requireContext());
            tv.setText(option);
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 40, 0, 40);

            // 选中状态使用品牌色高亮加粗
            if (option.equals(currentChoice)) {
                tv.setTextColor(Color.parseColor("#F5A623")); // brand-accent
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setTextColor(Color.parseColor("#E2E8F0")); // brand-text-main
            }

            // 触摸反馈 (水波纹)
            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            tv.setBackgroundResource(outValue.resourceId);
            tv.setClickable(true);

            // 点击事件
            tv.setOnClickListener(v1 -> {
                binding.etGender.setText(option);
                genderDialog.dismiss();
            });

            container.addView(tv);
        }

        genderDialog.setContentView(container);

        // 让 BottomSheet 自身的背景变透明，以显示我们的圆角
        View parentView = (View) container.getParent();
        if (parentView != null) {
            parentView.setBackgroundColor(Color.TRANSPARENT);
        }

        genderDialog.show();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
