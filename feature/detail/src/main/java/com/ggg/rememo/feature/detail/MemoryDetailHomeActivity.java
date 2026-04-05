package com.ggg.rememo.feature.detail;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.bumptech.glide.Glide;
import com.ggg.rememo.feature.detail.adapter.GalleryAdapter;
import com.ggg.rememo.feature.detail.contract.MemoryDetailContract;
import com.ggg.rememo.feature.detail.databinding.ActivityMemoryDetailHomeBinding;
import com.ggg.rememo.feature.detail.presenter.MemoryDetailPresenter;

import java.util.List;

@Route(path = Routes.Detail.HOME)
public class MemoryDetailHomeActivity extends AppCompatActivity implements MemoryDetailContract.View {

    private ActivityMemoryDetailHomeBinding binding;
    private MemoryDetailPresenter presenter;
    private GalleryAdapter galleryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemoryDetailHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        presenter = new MemoryDetailPresenter();
        presenter.attachView(this);

        initClickListeners();

        String postId = getIntent().getStringExtra(Routes.Detail.EXTRA_POST_ID);
        presenter.loadMemory(postId);
    }

    private void initClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnShare.setOnClickListener(v -> presenter.onShareClick());
        binding.iconLike.setOnClickListener(v -> presenter.onLikeClick());
        binding.iconStar.setOnClickListener(v -> presenter.onStarClick());

        binding.nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                float alpha = Math.min(1.0f, (float) scrollY / 300f);
                binding.viewTopNavSolidBg.setAlpha(alpha);
                binding.viewGradientOverlay.setAlpha(1.0f - alpha);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // ==================== MemoryDetailContract.View 实现 ====================

    @Override
    public void showMemory(MemoryPost post) {
        binding.tvDetailTitle.setText(post.getTitle());
        binding.tvContentBody.setText(post.getContent());
        String authorName = post.getAuthorNickname();
        binding.tvAuthorName.setText(authorName != null && !authorName.isEmpty() ? authorName : "匿名用户");

        if (post.getAuthorAvatar() != null && !post.getAuthorAvatar().isEmpty()) {
            Glide.with(this)
                    .load(post.getAuthorAvatar())
                    .circleCrop()
                    .into(binding.ivAuthorAvatar);
        }

        String season = post.getMemorySeason();
        int year = post.getMemoryYear();
        if (season != null && !season.isEmpty()) {
            binding.tvMemoryTime.setText(year + "年  " + season);
        } else {
            binding.tvMemoryTime.setText(String.valueOf(year));
        }

        ConstraintLayout.LayoutParams contentParams = (ConstraintLayout.LayoutParams) binding.contentArea.getLayoutParams();
        List<MemoryPhoto> images = post.getImages();

        if (images != null && !images.isEmpty()) {
            binding.galleryContainer.setVisibility(View.VISIBLE);
            contentParams.topMargin = dpToPx(-20);
            binding.contentArea.setLayoutParams(contentParams);
            setupGallery(images);
        } else {
            binding.galleryContainer.setVisibility(View.GONE);
            contentParams.topMargin = dpToPx(100);
            binding.contentArea.setLayoutParams(contentParams);
        }
        updateLikeState(post.isLiked(), post.getLikeCount());
        updateCollectState(post.isCollected(), post.getCollectCount());
    }

    @Override
    public void notifyImageFixed(int position) {
        if (galleryAdapter != null) {
            galleryAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void updateLikeState(boolean isLiked, int likeCount) {
        binding.iconLike.setImageResource(isLiked
                ? R.drawable.ic_heart_filled
                : R.drawable.ic_heart_outline);
        if (!isLiked) {
            binding.iconLike.setColorFilter(getColor(R.color.icon_default), PorterDuff.Mode.SRC_IN);
        } else {
            binding.iconLike.clearColorFilter();
        }
    }

    @Override
    public void updateCollectState(boolean isCollected, int collectCount) {
        binding.iconStar.setImageResource(isCollected
                ? R.drawable.ic_star_filled
                : R.drawable.ic_star_outline);
        if (!isCollected) {
            binding.iconStar.setColorFilter(getColor(R.color.icon_default), PorterDuff.Mode.SRC_IN);
        } else {
            binding.iconStar.clearColorFilter();
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private void setupGallery(List<MemoryPhoto> images) {
        galleryAdapter = new GalleryAdapter();
        galleryAdapter.setPhotos(images);
        galleryAdapter.setOnAiFixClickListener(position -> presenter.onImageAiFix(position));
        binding.vpImageGallery.setAdapter(galleryAdapter);
        updateGalleryIndicator(1, images.size());

        binding.vpImageGallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateGalleryIndicator(position + 1, images.size());
            }
        });
    }

    private void updateGalleryIndicator(int current, int total) {
        binding.tvGalleryIndicator.setText(current + " / " + total);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
