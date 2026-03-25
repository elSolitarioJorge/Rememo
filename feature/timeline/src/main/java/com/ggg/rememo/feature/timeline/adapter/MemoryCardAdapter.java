package com.ggg.rememo.feature.timeline.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.timeline.R;
import com.ggg.rememo.feature.timeline.databinding.ItemMemoryCardBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆卡片 Adapter
 */
public class MemoryCardAdapter extends RecyclerView.Adapter<MemoryCardAdapter.ViewHolder> {

    private final List<MemoryPost> posts = new ArrayList<>();
    private OnCardClickListener listener;

    public interface OnCardClickListener {
        void onCardClick(MemoryPost post);
    }

    public void setOnCardClickListener(OnCardClickListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<MemoryPost> newPosts) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return posts.size();
            }

            @Override
            public int getNewListSize() {
                return newPosts != null ? newPosts.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return posts.get(oldItemPosition).getPostId()
                        .equals(newPosts.get(newItemPosition).getPostId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                MemoryPost oldPost = posts.get(oldItemPosition);
                MemoryPost newPost = newPosts.get(newItemPosition);
                return oldPost.getTitle() != null && oldPost.getTitle().equals(newPost.getTitle())
                        && oldPost.getLikeCount() == newPost.getLikeCount()
                        && oldPost.getCommentCount() == newPost.getCommentCount();
            }
        });

        posts.clear();
        if (newPosts != null) {
            posts.addAll(newPosts);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    public List<MemoryPost> getPosts() {
        return new ArrayList<>(posts);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemoryCardBinding binding = ItemMemoryCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMemoryCardBinding binding;

        ViewHolder(@NonNull ItemMemoryCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemoryPost post) {
            binding.tvTitle.setText(post.getTitle());

            // 描述/正文
            String content = post.getContent();
            if (content != null && !content.isEmpty()) {
                binding.tvDesc.setText(content);
                binding.tvDesc.setVisibility(ViewGroup.VISIBLE);
            } else {
                binding.tvDesc.setVisibility(ViewGroup.GONE);
            }

            // 季节标签
            String season = post.getMemorySeason();
            binding.tvSeason.setVisibility(season != null && !season.isEmpty()
                    ? ViewGroup.VISIBLE : ViewGroup.GONE);
            binding.tvSeason.setText(season != null ? season : "");

            // 点赞数 & 评论数
            binding.tvLikes.setText(String.valueOf(post.getLikeCount()));
            binding.tvComments.setText(String.valueOf(post.getCommentCount()));

            // AI 修复标签
            binding.tvAiTag.setVisibility(hasRestoredImage(post) ? ViewGroup.VISIBLE : ViewGroup.GONE);

            // 封面图
            loadCoverImage(post);

            // 用户头像
            Glide.with(binding.getRoot().getContext())
                    .load(com.ggg.rememo.core.ui.R.drawable.avatar_placeholder)
                    .circleCrop()
                    .into(binding.ivAvatar);

            // 点击事件
            binding.getRoot().setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCardClick(posts.get(pos));
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
                binding.ivCover.setImageResource(com.ggg.rememo.core.ui.R.drawable.cover_placeholder);
                return;
            }

            MemoryPhoto photo = post.getImages().get(0);
            String imageUrl = photo.getDisplayUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .placeholder(com.ggg.rememo.core.ui.R.drawable.cover_placeholder)
                        .error(com.ggg.rememo.core.ui.R.drawable.cover_placeholder)
                        .centerCrop()
                        .into(binding.ivCover);
            } else {
                binding.ivCover.setImageResource(com.ggg.rememo.core.ui.R.drawable.cover_placeholder);
            }
        }
    }
}
