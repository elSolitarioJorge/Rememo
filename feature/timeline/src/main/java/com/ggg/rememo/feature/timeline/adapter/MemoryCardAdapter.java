package com.ggg.rememo.feature.timeline.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.ui.R;
import com.ggg.rememo.feature.timeline.databinding.ItemMemoryCardBinding;
import com.ggg.rememo.feature.timeline.databinding.ItemTimelineGatewayBinding;

import java.util.ArrayList;
import java.util.List;

public class MemoryCardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** 普通记忆卡片 */
    public static final int ITEM_TYPE_MEMORY = 0;
    /** 时空传送门卡片 */
    public static final int ITEM_TYPE_GATEWAY = 1;

    /** 预览记忆数量上限 */
    private static final int PREVIEW_LIMIT = 3;

    private final List<MemoryPost> posts = new ArrayList<>();
    private OnCardClickListener listener;
    private OnGatewayClickListener gatewayListener;

    // 时空传送门数据（仅当 memoryCount > PREVIEW_LIMIT 时有效）
    private boolean showGateway = false;
    private int gatewayYear = 0;
    private int remainingCount = 0;

    public interface OnCardClickListener {
        void onCardClick(MemoryPost post);
    }

    public interface OnGatewayClickListener {
        void onGatewayClick(int year, int remainingCount);
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.listener = listener;
    }

    public void setOnGatewayClickListener(OnGatewayClickListener gatewayListener) {
        this.gatewayListener = gatewayListener;
    }

    /**
     * 设置记忆列表，同时根据记忆总数决定是否追加时空传送门。
     *
     * @param newPosts     当年所有记忆列表
     * @param memoryCount  当年记忆总数量
     * @param year         年份（用于传送门文案）
     */
    public void setPosts(List<MemoryPost> newPosts, int memoryCount, int year) {
        List<MemoryPost> previewPosts = newPosts != null
                ? new ArrayList<>(newPosts.subList(0, Math.min(PREVIEW_LIMIT, newPosts.size())))
                : new ArrayList<>();

        boolean newShowGateway = memoryCount > PREVIEW_LIMIT;
        int newGatewayYear = year;
        int newRemainingCount = memoryCount - PREVIEW_LIMIT;

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return posts.size() + (showGateway ? 1 : 0);
            }

            @Override
            public int getNewListSize() {
                return previewPosts.size() + (newShowGateway ? 1 : 0);
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                // 【修复 Bug】精准判断当前 position 是否恰好指向传送门
                boolean isOldGateway = showGateway && (oldItemPosition == posts.size());
                boolean isNewGateway = newShowGateway && (newItemPosition == previewPosts.size());

                // 如果都是传送门，必然是同一个 Item
                if (isOldGateway && isNewGateway) return true;
                // 如果一个是一般卡片一个是传送门，必然不同
                if (isOldGateway || isNewGateway) return false;

                // 都是普通记忆卡片，比较 ID
                return posts.get(oldItemPosition).getPostId()
                        .equals(previewPosts.get(newItemPosition).getPostId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                boolean isOldGateway = showGateway && (oldItemPosition == posts.size());
                boolean isNewGateway = newShowGateway && (newItemPosition == previewPosts.size());

                // 如果都是传送门，比较它们展示的文案数据即可
                if (isOldGateway && isNewGateway) {
                    return gatewayYear == newGatewayYear && remainingCount == newRemainingCount;
                }
                if (isOldGateway || isNewGateway) return false;

                // 都是普通卡片，比较具体内容
                MemoryPost oldPost = posts.get(oldItemPosition);
                MemoryPost newPost = previewPosts.get(newItemPosition);
                return oldPost.getTitle() != null && oldPost.getTitle().equals(newPost.getTitle())
                        && oldPost.getLikeCount() == newPost.getLikeCount()
                        && oldPost.getCommentCount() == newPost.getCommentCount();
            }
        }, false);

        // 更新本地数据
        posts.clear();
        posts.addAll(previewPosts);

        showGateway = newShowGateway;
        gatewayYear = newGatewayYear;
        remainingCount = newRemainingCount;

        // 分发动画
        diffResult.dispatchUpdatesTo(this);
    }

    public List<MemoryPost> getPosts() {
        return new ArrayList<>(posts);
    }

    @Override
    public int getItemViewType(int position) {
        // 当索引刚好等于数据列表 size，说明已经遍历完所有记忆，该显示传送门了
        if (showGateway && position == posts.size()) {
            return ITEM_TYPE_GATEWAY;
        }
        return ITEM_TYPE_MEMORY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_TYPE_GATEWAY) {
            ItemTimelineGatewayBinding binding = ItemTimelineGatewayBinding.inflate(inflater, parent, false);
            return new GatewayViewHolder(binding);
        } else {
            ItemMemoryCardBinding binding = ItemMemoryCardBinding.inflate(inflater, parent, false);
            return new MemoryViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MemoryViewHolder) {
            ((MemoryViewHolder) holder).bind(posts.get(position), listener);
        } else if (holder instanceof GatewayViewHolder) {
            ((GatewayViewHolder) holder).bind(gatewayYear, remainingCount, gatewayListener);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size() + (showGateway ? 1 : 0);
    }

    // MemoryViewHolder — 普通记忆卡片
    public static class MemoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemMemoryCardBinding binding;

        MemoryViewHolder(@NonNull ItemMemoryCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemoryPost post, OnCardClickListener listener) {
            binding.tvTitle.setText(post.getTitle());

            String content = post.getContent();
            if (content != null && !content.isEmpty()) {
                binding.tvDesc.setText(content);
                binding.tvDesc.setVisibility(ViewGroup.VISIBLE);
            } else {
                binding.tvDesc.setVisibility(ViewGroup.GONE);
            }

            String season = post.getMemorySeason();
            binding.tvSeason.setVisibility(season != null && !season.isEmpty()
                    ? ViewGroup.VISIBLE : ViewGroup.GONE);
            binding.tvSeason.setText(season != null ? season : "");

            binding.tvLikes.setText(String.valueOf(post.getLikeCount()));
            binding.tvComments.setText(String.valueOf(post.getCommentCount()));

            if (post.isLiked()) {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_heart);
                binding.ivLikeIcon.setColorFilter(Color.parseColor("#EF4444"));
            } else {
                binding.ivLikeIcon.setImageResource(R.drawable.ic_gray_like);
                binding.ivLikeIcon.setColorFilter(Color.parseColor("#9CA3AF"));
            }

            binding.tvAiTag.setVisibility(hasRestoredImage(post) ? ViewGroup.VISIBLE : ViewGroup.GONE);

            loadCoverImage(post);

            // 用户名：优先使用 API 返回的昵称，回退到"匿名用户"
            String authorName = post.getAuthorNickname();
            binding.tvUserName.setText(
                    authorName != null && !authorName.isEmpty() ? authorName : "匿名用户");

            // 用户头像：优先使用 API 返回的头像 URL，回退到占位图
            String avatarUrl = post.getAuthorAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(avatarUrl)
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .circleCrop()
                        .into(binding.ivAvatar);
            } else {
                binding.ivAvatar.setImageResource(R.drawable.avatar_placeholder);
            }

            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCardClick(post);
                }
            });
        }

        private boolean hasRestoredImage(MemoryPost post) {
            if (post.getImages() == null || post.getImages().isEmpty()) {
                return false;
            }
            for (MemoryPhoto photo : post.getImages()) {
                if (photo.getRestoredUrl() != null && !photo.getRestoredUrl().isEmpty()) {
                    return true;
                }
            }
            return false;
        }

        private void loadCoverImage(MemoryPost post) {
            if (post.getImages() == null || post.getImages().isEmpty()) {
                binding.ivCover.setImageResource(R.drawable.cover_placeholder);
                return;
            }

            MemoryPhoto photo = post.getImages().get(0);
            String imageUrl = photo.getDisplayUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.cover_placeholder)
                        .error(R.drawable.cover_placeholder)
                        .centerCrop()
                        .into(binding.ivCover);
            } else {
                binding.ivCover.setImageResource(R.drawable.cover_placeholder);
            }
        }
    }

    // GatewayViewHolder — 时空传送门卡片

    public static class GatewayViewHolder extends RecyclerView.ViewHolder {

        private final ItemTimelineGatewayBinding binding;

        GatewayViewHolder(@NonNull ItemTimelineGatewayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(int year, int remainingCount, OnGatewayClickListener listener) {
            binding.tvGatewayTitle.setText("探索 " + year);
            binding.tvRemainingCount.setText("剩余 " + remainingCount + " 条记忆 >");

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGatewayClick(year, remainingCount);
                }
            });
        }
    }
}