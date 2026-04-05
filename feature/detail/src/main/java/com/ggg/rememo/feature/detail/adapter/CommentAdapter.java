package com.ggg.rememo.feature.detail.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.Comment;
import com.ggg.rememo.feature.detail.databinding.ItemDetailCommentBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 评论列表 Adapter。
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> comments = new ArrayList<>();

    public void setComments(List<Comment> newComments) {
        comments.clear();
        if (newComments != null) {
            comments.addAll(newComments);
        }
        notifyDataSetChanged();
    }

    public void addComment(Comment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDetailCommentBinding binding = ItemDetailCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemDetailCommentBinding binding;

        CommentViewHolder(@NonNull ItemDetailCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Comment comment) {
            binding.tvCommentName.setText(comment.getAuthorNickname() != null
                    ? comment.getAuthorNickname() : "匿名用户");
            binding.tvCommentContent.setText(comment.getContent());
            binding.tvCommentTime.setText(formatTime(comment.getCreatedTime()));

            if (comment.getAuthorAvatar() != null && !comment.getAuthorAvatar().isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(comment.getAuthorAvatar())
                        .circleCrop()
                        .into(binding.ivCommentAvatar);
            }
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
}
