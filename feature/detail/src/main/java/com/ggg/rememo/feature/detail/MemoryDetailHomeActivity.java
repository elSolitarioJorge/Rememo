package com.ggg.rememo.feature.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.base.BaseActivity;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.Comment;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.bumptech.glide.Glide;
import com.ggg.rememo.feature.detail.adapter.CommentAdapter;
import com.ggg.rememo.feature.detail.adapter.GalleryAdapter;
import com.ggg.rememo.feature.detail.contract.MemoryDetailContract;
import com.ggg.rememo.feature.detail.databinding.ActivityMemoryDetailHomeBinding;
import com.ggg.rememo.feature.detail.presenter.MemoryDetailPresenter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Route(path = Routes.Detail.HOME)
public class MemoryDetailHomeActivity extends BaseActivity<
        ActivityMemoryDetailHomeBinding,
        MemoryDetailContract.View,
        MemoryDetailPresenter>
        implements MemoryDetailContract.View {
    private GalleryAdapter galleryAdapter;
    private CommentAdapter commentAdapter;
    private String currentPostId;

    @Override
    protected ActivityMemoryDetailHomeBinding inflateBinding(@NonNull LayoutInflater inflater) {
        return ActivityMemoryDetailHomeBinding.inflate(inflater);
    }

    @Override
    protected MemoryDetailPresenter createPresenter() {
        return new MemoryDetailPresenter();
    }

    @Override
    protected MemoryDetailContract.View getViewContract() {
        return this;
    }

    @Override
    protected void initView() {
        setupKeyboardListener();
        initClickListeners();
        initRecyclerView();
    }

    @Override
    protected void initData() {
        currentPostId = getIntent().getStringExtra(Routes.Detail.EXTRA_POST_ID);
        presenter.loadMemory(currentPostId);
        presenter.loadComments(currentPostId);
    }

    private void setupKeyboardListener() {
        // 在监听器外部，提前获取并记录下原始的 paddingBottom
        final int initialPaddingBottom = getBinding().layoutBottomActionBar.getPaddingBottom();

        ViewCompat.setOnApplyWindowInsetsListener(getBinding().getRoot(), (v, insets) -> {
            // 获取键盘（IME）的高度
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            // 获取系统栏高度
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            // 增加底部视图内边距，适配沉浸式全屏显示
            getBinding().layoutBottomActionBar.setPadding(
                    getBinding().layoutBottomActionBar.getPaddingStart(),
                    getBinding().layoutBottomActionBar.getPaddingTop(),
                    getBinding().layoutBottomActionBar.getPaddingEnd(),
                    initialPaddingBottom + systemBars.bottom);

            // 判断当前键盘是否可见
            boolean isKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            // 动态调整真实输入面板的 BottomMargin
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) getBinding().layoutRealCommentInput.getLayoutParams();
            // 底部边距 = 键盘高度
            layoutParams.bottomMargin = imeInsets.bottom;
            getBinding().layoutRealCommentInput.setLayoutParams(layoutParams);

            if (!isKeyboardVisible && getBinding().layoutRealCommentInput.getVisibility() == View.VISIBLE) {
                getBinding().layoutRealCommentContainer.setVisibility(View.GONE);
                getBinding().etRealComment.clearFocus();
            }
            return insets;
        });
    }

    private void initClickListeners() {
        getBinding().btnBack.setOnClickListener(v -> finish());
        getBinding().btnShare.setOnClickListener(v -> presenter.onShareClick());
        getBinding().iconLike.setOnClickListener(v -> presenter.onLikeClick());
        getBinding().iconStar.setOnClickListener(v -> presenter.onStarClick());

        getBinding().nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                float alpha = Math.min(1.0f, (float) scrollY / 300f);
                getBinding().viewTopNavSolidBg.setAlpha(alpha);
                getBinding().viewGradientOverlay.setAlpha(1.0f - alpha);
            }
        });

        // 点击底部的“假”输入框（TextView），唤起真正的输入面板
        getBinding().tvCommentInput.setOnClickListener(v -> {
            // 显示遮罩层和真正的输入面板
            getBinding().layoutRealCommentContainer.setVisibility(View.VISIBLE);

            // 获取焦点
            getBinding().etRealComment.requestFocus();

            // 弹出软键盘
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(getBinding().etRealComment, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // 点击半透明遮罩层 -> 收起输入面板
        getBinding().viewKeyboardOverlay.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getBinding().etRealComment.getWindowToken(), 0);
            }
        });

        getBinding().etRealComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 输入前调用
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入过程中调用
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 在文本编辑完成后触发
                // 过滤掉首尾空格后的文本内容
                String content = (s != null) ? s.toString().trim() : "";

                // 根据输入内容更新按钮状态
                if (!content.isEmpty()) {
                    // 有输入内容 -> 亮色
                    if (getBinding().btnSendComment.getBackground() != null) {
                        getBinding().btnSendComment.getBackground().setTint(Color.parseColor("#F5A623"));
                    }
                    getBinding().btnSendComment.setTextColor(Color.parseColor("#FFFFFF"));
                } else {
                    // 无输入内容 -> 暗色
                    if (getBinding().btnSendComment.getBackground() != null) {
                        getBinding().btnSendComment.getBackground().setTint(Color.parseColor("#334155"));
                    }
                    getBinding().btnSendComment.setTextColor(Color.parseColor("#94A3B8"));
                }
            }
        });

        getBinding().btnSendComment.setOnClickListener(v -> {
            String content = getBinding().etRealComment.getText().toString().trim();
            if (!content.isEmpty()) {
                presenter.onSendComment(currentPostId, content);
            } else {
                Toast.makeText(this, "有内容才能发送哦", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView() {
        commentAdapter = new CommentAdapter();
        getBinding().rvComments.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvComments.setAdapter(commentAdapter);
        getBinding().rvComments.setNestedScrollingEnabled(false);
    }

    // ==================== MemoryDetailContract.View 实现 ====================

    @Override
    public void showMemory(MemoryPost post) {
        getBinding().tvDetailTitle.setText(post.getTitle());
        getBinding().tvContentBody.setText(post.getContent());
        String authorName = post.getAuthorNickname();
        getBinding().tvAuthorName.setText(authorName != null && !authorName.isEmpty() ? authorName : "匿名用户");
        getBinding().tvPublishTime.setText(formatTime(post.getCreatedTime()));

        if (post.getAuthorAvatar() != null && !post.getAuthorAvatar().isEmpty()) {
            Glide.with(this)
                    .load(post.getAuthorAvatar())
                    .circleCrop()
                    .into(getBinding().ivAuthorAvatar);
        }

        String season = post.getMemorySeason();
        int year = post.getMemoryYear();
        if (season != null && !season.isEmpty()) {
            getBinding().tvMemoryTime.setText(year + "年  " + season);
        } else {
            getBinding().tvMemoryTime.setText(String.valueOf(year));
        }

        ConstraintLayout.LayoutParams contentParams = (ConstraintLayout.LayoutParams) getBinding().contentArea.getLayoutParams();
        List<MemoryPhoto> images = post.getImages();

        if (images != null && !images.isEmpty()) {
            getBinding().galleryContainer.setVisibility(View.VISIBLE);
            contentParams.topMargin = dpToPx(-20);
            getBinding().contentArea.setLayoutParams(contentParams);
            setupGallery(images);
        } else {
            getBinding().galleryContainer.setVisibility(View.GONE);
            contentParams.topMargin = dpToPx(100);
            getBinding().contentArea.setLayoutParams(contentParams);
        }
        updateLikeState(post.isLiked(), post.getLikeCount());
        updateCollectState(post.isCollected(), post.getCollectCount());
        getBinding().tvCommentCount.setText(post.getCommentCount() == 0 ? "评论" : post.getCommentCount() + "");
    }

    @Override
    public void notifyImageFixed(int position) {
        if (galleryAdapter != null) {
            galleryAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void showMemoryPoint(MemoryPoint point) {
        getBinding().tvPointName.setText(point.getPointName() != null ? point.getPointName() : "未知锚点");
        getBinding().tvPointAddress.setText(point.getLocationAddress() != null ? point.getLocationAddress() : "未知地址");
        getBinding().cardMapEntry.setOnClickListener(v -> {
            ARouter.getInstance()
                    .build(Routes.Timeline.HOME)
                    .withString(Routes.Timeline.EXTRA_POINT_ID, point.getPointId())
                    .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .navigation(this);
        });
    }

    @Override
    public void updateLikeState(boolean isLiked, int likeCount) {
        getBinding().iconLike.setImageResource(isLiked
                ? R.drawable.ic_heart_filled
                : R.drawable.ic_heart_outline);
        if (!isLiked) {
            getBinding().iconLike.setColorFilter(getColor(R.color.icon_default), PorterDuff.Mode.SRC_IN);
        } else {
            getBinding().iconLike.clearColorFilter();
        }
        getBinding().tvLikeCount.setText(likeCount == 0 ? "点赞" : likeCount + "");
    }

    @Override
    public void updateCollectState(boolean isCollected, int collectCount) {
        getBinding().iconStar.setImageResource(isCollected
                ? R.drawable.ic_star_filled
                : R.drawable.ic_star_outline);
        if (!isCollected) {
            getBinding().iconStar.setColorFilter(getColor(R.color.icon_default), PorterDuff.Mode.SRC_IN);
        } else {
            getBinding().iconStar.clearColorFilter();
        }
        getBinding().tvStarCount.setText(collectCount == 0 ? "收藏" : collectCount + "");
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showComments(List<Comment> comments) {
        if (comments != null && !comments.isEmpty()) {
            getBinding().layoutEmptyComments.setVisibility(View.GONE);
            getBinding().rvComments.setVisibility(View.VISIBLE);
            commentAdapter.setComments(comments);
            getBinding().tvCommentCount.setText(comments.size() + "");
        } else {
            getBinding().layoutEmptyComments.setVisibility(View.VISIBLE);
            getBinding().rvComments.setVisibility(View.GONE);
        }
    }

    @Override
    public void showCommentSendSuccess() {
        getBinding().etRealComment.setText("");
        getBinding().layoutRealCommentContainer.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getBinding().etRealComment.getWindowToken(), 0);
        }
        if (currentPostId != null) {
            presenter.loadComments(currentPostId);
        }
    }


    private void setupGallery(List<MemoryPhoto> images) {
        galleryAdapter = new GalleryAdapter();
        galleryAdapter.setPhotos(images);
        galleryAdapter.setOnAiFixClickListener(position -> presenter.onImageAiFix(position));
        getBinding().vpImageGallery.setAdapter(galleryAdapter);
        updateGalleryIndicator(1, images.size());

        getBinding().vpImageGallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateGalleryIndicator(position + 1, images.size());
            }
        });
    }

    private void updateGalleryIndicator(int current, int total) {
        getBinding().tvGalleryIndicator.setText(current + " / " + total);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    private String formatTime(long timestamp) {
        if (timestamp <= 0) return "";
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        long minute = 60 * 1000;
        long hour = 60 * minute;
        long day = 24 * hour;

        if (diff < minute) {
            return "刚刚";
        } else if (diff < hour) {
            return (diff / minute) + "分钟前";
        } else if (diff < day) {
            return (diff / hour) + "小时前";
        } else if (diff < 7 * day) {
            return (diff / day) + "天前";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        }
    }
}
