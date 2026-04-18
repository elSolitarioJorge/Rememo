package com.ggg.rememo.feature.timeline.view.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.ui.R;
import com.ggg.rememo.feature.timeline.databinding.ItemArchiveMemoryBinding;
import com.ggg.rememo.feature.timeline.data.model.SeasonSection;

import java.util.ArrayList;
import java.util.List;

/**
 * 年份档案 RecyclerView Adapter
 * <p>
 * 支持两种展示模式：
 * <ul>
 *   <li>{@link #MODE_HOT} — 瀑布流双列，隐藏时间节点，传入普通 MemoryPost 列表</li>
 *   <li>{@link #MODE_TIMELINE} — 单列季节脉络，传入 SeasonSection 列表，每个季节开头显示季节标签</li>
 * </ul>
 */
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {

    /** 瀑布流模式（时空热度） */
    public static final int MODE_HOT = 1;

    /** 时间脉络模式（季节脉络） */
    public static final int MODE_TIMELINE = 2;

    /** ViewType：季节标签行 */
    private static final int VIEW_TYPE_SEASON_HEADER = 0;

    /** ViewType：记忆卡片 */
    private static final int VIEW_TYPE_MEMORY_CARD = 1;

    private int currentMode = MODE_HOT;

    // 瀑布流模式数据：MemoryPost 列表
    private final List<MemoryPost> memoryPosts = new ArrayList<>();

    // 季节脉络模式数据：SeasonSection 列表（包含季节标签 + 该季节记忆）
    private final List<Object> timelineItems = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MemoryPost post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setMode(int mode) {
        if (this.currentMode != mode) {
            this.currentMode = mode;
            notifyDataSetChanged();
        }
    }

    /**
     * 设置瀑布流模式数据
     */
    public void setData(List<MemoryPost> posts) {
        this.memoryPosts.clear();
        if (posts != null) {
            this.memoryPosts.addAll(posts);
        }
        notifyDataSetChanged();
    }

    /**
     * 设置季节脉络模式数据（按季节分组）
     */
    public void setTimelineData(List<SeasonSection> sections) {
        this.timelineItems.clear();
        for (SeasonSection section : sections) {
            timelineItems.add(section.seasonName);
            timelineItems.addAll(section.posts);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (currentMode == MODE_TIMELINE) {
            return timelineItems.get(position) instanceof String
                    ? VIEW_TYPE_SEASON_HEADER
                    : VIEW_TYPE_MEMORY_CARD;
        }
        return VIEW_TYPE_MEMORY_CARD;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemArchiveMemoryBinding binding = ItemArchiveMemoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (currentMode == MODE_TIMELINE) {
            Object item = timelineItems.get(position);
            if (item instanceof String) {
                holder.bindSeasonHeader((String) item);
            } else {
                holder.bindMemoryCard((MemoryPost) item, position);
            }
        } else {
            holder.bindMemoryCard(memoryPosts.get(position), position);
        }
    }

    @Override
    public int getItemCount() {
        return currentMode == MODE_TIMELINE ? timelineItems.size() : memoryPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemArchiveMemoryBinding binding;
        private final int viewType;

        ViewHolder(@NonNull ItemArchiveMemoryBinding binding, int viewType) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewType = viewType;
        }

        void bindSeasonHeader(String season) {
            binding.groupTimelineNodes.setVisibility(View.VISIBLE);
            binding.tvSeasonNode.setText(season);
            binding.cardMemoryContainer.setVisibility(View.GONE);
            binding.ivCover.setVisibility(View.GONE);
            binding.ivQuoteIcon.setVisibility(View.GONE);

            resetCardMarginStart(0);

            binding.getRoot().setPadding(
                    binding.getRoot().getPaddingLeft(),
                    dpToPx(12),
                    binding.getRoot().getPaddingRight(),
                    dpToPx(4)
            );
        }

        void bindMemoryCard(MemoryPost post, int position) {
            binding.getRoot().setPadding(
                    binding.getRoot().getPaddingLeft(),
                    0,
                    binding.getRoot().getPaddingRight(),
                    dpToPx(6)
            );

            if (currentMode == MODE_TIMELINE) {
                // 季节脉络模式：隐藏左侧时间节点（每个卡片不重复显示季节标签）
                binding.groupTimelineNodes.setVisibility(View.GONE);
                binding.cardMemoryContainer.setVisibility(View.VISIBLE);
                resetCardMarginStart(dpToPx(44));
            } else {
                // 瀑布流模式：隐藏时间节点，卡片从左侧边缘开始
                binding.groupTimelineNodes.setVisibility(View.GONE);
                binding.cardMemoryContainer.setVisibility(View.VISIBLE);
                resetCardMarginStart(0);
            }

            // 基础数据绑定
            binding.tvTitle.setText(post.getTitle());
            binding.tvAuthor.setText(post.getAuthorNickname());
            binding.tvLikes.setText(String.valueOf(post.getLikeCount()));

            if (post.isLiked()) {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart);
                binding.ivLikeIcon.setColorFilter(Color.parseColor("#EF4444"));
            } else {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_gray_like);
                binding.ivLikeIcon.setColorFilter(Color.parseColor("#9CA3AF"));
            }

            // 用户头像
            String avatarUrl = post.getAuthorAvatar();
            if (!TextUtils.isEmpty(avatarUrl)) {
                binding.ivAvatar.setVisibility(View.VISIBLE);
                Glide.with(binding.ivAvatar.getContext())
                        .load(avatarUrl)
                        .circleCrop()
                        .into(binding.ivAvatar);
            } else {
                binding.ivAvatar.setVisibility(View.INVISIBLE);
            }

            binding.tvContent.setText(post.getContent());

            // 图片处理
            List<MemoryPhoto> images = post.getImages();
            boolean hasImage = images != null && !images.isEmpty();

            if (hasImage) {
                binding.ivCover.setVisibility(View.VISIBLE);
                binding.ivQuoteIcon.setVisibility(View.GONE);
                binding.cardMemoryContainer.setCardBackgroundColor(0xFF16192B);

                String imageUrl = images.get(0).getDisplayUrl();
                binding.ivCover.setImageDrawable(null);
                loadImageWithAspectRatio(imageUrl, binding.ivCover);
            } else {
                binding.ivCover.setVisibility(View.GONE);
                binding.ivQuoteIcon.setVisibility(View.VISIBLE);
                binding.cardMemoryContainer.setCardBackgroundColor(0xFF16192B);
            }

            // Item 点击
            binding.getRoot().setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(post);
                }
            });
        }

        private void resetCardMarginStart(int marginPx) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) binding.cardMemoryContainer.getLayoutParams();
            params.setMarginStart(marginPx);
            binding.cardMemoryContainer.setLayoutParams(params);
        }

        private void loadImageWithAspectRatio(String imageUrl, ImageView imageView) {
            imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int measuredWidth = imageView.getMeasuredWidth();
                    if (measuredWidth > 0) {
                        Glide.with(imageView.getContext())
                                .load(imageUrl)
                                .override(measuredWidth, 0)
                                .fitCenter()
                                .into(imageView);
                    } else {
                        Glide.with(imageView.getContext())
                                .load(imageUrl)
                                .fitCenter()
                                .into(imageView);
                    }
                    return true;
                }
            });
        }

        private int dpToPx(int dp) {
            return (int) (binding.getRoot().getContext().getResources()
                    .getDisplayMetrics().density * dp + 0.5f);
        }
    }
}
