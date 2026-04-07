package com.ggg.rememo.feature.profile;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.common.util.AppContext;
import com.ggg.rememo.core.data.local.ImageStorageHelper;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.response.UserInfo;
import com.ggg.rememo.feature.profile.contract.ProfileContract;
import com.ggg.rememo.feature.profile.databinding.DialogEditProfileBinding;
import com.ggg.rememo.feature.profile.presenter.ProfilePresenter;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;


public class EditProfileDialogFragment extends BottomSheetDialogFragment implements ProfileContract.View {

    private static final String TAG = "EditProfile";
    private static final String ARG_AVATAR = "arg_avatar";

    private DialogEditProfileBinding binding;
    private ProfilePresenter presenter;
    private String currentAvatar = "";
    private String selectedAvatarPath = "";
    private OnProfileUpdateListener updateListener;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == FragmentActivity.RESULT_OK && result.getData() != null) {
                    Uri selectedUri = result.getData().getData();
                    if (selectedUri != null) {
                        handleImageSelected(selectedUri);
                    }
                }
            });

    public interface OnProfileUpdateListener {
        void onProfileUpdated(UserInfo updatedUserInfo);
    }

    public static EditProfileDialogFragment newInstance(String currentAvatar, OnProfileUpdateListener listener) {
        EditProfileDialogFragment fragment = new EditProfileDialogFragment();
        fragment.updateListener = listener;
        Bundle args = new Bundle();
        args.putString(ARG_AVATAR, currentAvatar != null ? currentAvatar : "");
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetTheme);
        if (getArguments() != null) {
            currentAvatar = getArguments().getString(ARG_AVATAR, "");
        }
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

        presenter = new ProfilePresenter();
        presenter.attachView(this);

        loadCurrentAvatar();
        initClickListeners();
    }

    private void loadCurrentAvatar() {
        if (currentAvatar != null && !currentAvatar.isEmpty()) {
            Glide.with(this)
                    .load(currentAvatar)
                    .placeholder(com.ggg.rememo.core.ui.R.drawable.avatar_placeholder)
                    .into(binding.ivEditAvatar);
        }
    }

    private void initClickListeners() {
        binding.ivClose.setOnClickListener(v -> dismiss());
        binding.btnSave.setOnClickListener(v -> saveProfile());
        binding.etGender.setOnClickListener(v -> selectGender());
        binding.flAvatarContainer.setOnClickListener(v -> showImageSourceDialog());
    }

    private void showImageSourceDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#16192B"));
        bg.setCornerRadii(new float[]{60, 60, 60, 60, 0, 0, 0, 0});
        container.setBackground(bg);
        container.setPadding(0, 40, 0, 20);

        TextView title = new TextView(requireContext());
        title.setText("更换头像");
        title.setTextColor(Color.parseColor("#64748B"));
        title.setTextSize(13);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 40);
        container.addView(title);

        String[] options = {"从相册选择"};
        for (String option : options) {
            TextView tv = new TextView(requireContext());
            tv.setText(option);
            tv.setTextSize(16);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 40, 0, 40);
            tv.setTextColor(Color.parseColor("#E2E8F0"));

            TypedValue outValue = new TypedValue();
            requireContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            tv.setBackgroundResource(outValue.resourceId);
            tv.setClickable(true);

            tv.setOnClickListener(v1 -> {
                dialog.dismiss();
                openImagePicker();
            });
            container.addView(tv);
        }

        dialog.setContentView(container);
        View parentView = (View) container.getParent();
        if (parentView != null) {
            parentView.setBackgroundColor(Color.TRANSPARENT);
        }
        dialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void handleImageSelected(Uri uri) {
        try {
            String localPath = ImageStorageHelper.copyUriToInternalStorage(
                    AppContext.get(), uri);
            selectedAvatarPath = localPath;
            Glide.with(this)
                    .load(localPath)
                    .placeholder(com.ggg.rememo.core.ui.R.drawable.avatar_placeholder)
                    .into(binding.ivEditAvatar);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "图片处理失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String nickname = binding.etNickname.getText() != null
                ? binding.etNickname.getText().toString().trim() : "";
        String bio = binding.etBio.getText() != null
                ? binding.etBio.getText().toString().trim() : "";
        String genderText = binding.etGender.getText() != null
                ? binding.etGender.getText().toString().trim() : "保密";

        if (nickname.isEmpty()) {
            Toast.makeText(requireContext(), "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String gender = mapGenderToApi(genderText);

        binding.btnSave.setEnabled(false);
        binding.btnSave.setText("保存中...");

        if (!selectedAvatarPath.isEmpty()) {
            // 先上传头像图片，获取 URL 后再更新用户信息
            presenter.uploadAvatarAndUpdateProfile(
                    selectedAvatarPath,
                    nickname,
                    gender,
                    bio
            );
        } else {
            // 未选择新头像，直接更新其他信息
            presenter.updateProfile(nickname, currentAvatar, gender, bio);
        }
    }

    private String mapGenderToApi(String displayGender) {
        switch (displayGender) {
            case "男":   return "male";
            case "女":   return "female";
            default:     return "secret";
        }
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

            // 选中状态高亮加粗
            if (option.equals(currentChoice)) {
                tv.setTextColor(Color.parseColor("#F5A623"));
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setTextColor(Color.parseColor("#E2E8F0"));
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
        if (presenter != null) {
            presenter.detachView();
        }
        binding = null;
    }

    // ========== ProfileContract.View 实现 ==========

    @Override
    public void showUserInfo(com.ggg.rememo.core.data.model.network.response.UserInfo userInfo) {
        // 由主页 Fragment 调用，编辑弹窗不需要实现
    }

    @Override
    public void showUpdateSuccess() {
        Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
        binding.btnSave.setEnabled(true);
        binding.btnSave.setText("保存");
        if (updateListener != null) {
            updateListener.onProfileUpdated(null);
        }
        dismiss();
    }

    @Override
    public void showUpdateSuccessWithData(UserInfo userInfo) {
        Toast.makeText(requireContext(), "保存成功", Toast.LENGTH_SHORT).show();
        binding.btnSave.setEnabled(true);
        binding.btnSave.setText("保存");
        if (updateListener != null) {
            updateListener.onProfileUpdated(userInfo);
        }
        dismiss();
    }

    @Override
    public void navigateToLogin() {
        // 本弹窗不需要实现
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
            binding.btnSave.setEnabled(true);
            binding.btnSave.setText("保存");
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
