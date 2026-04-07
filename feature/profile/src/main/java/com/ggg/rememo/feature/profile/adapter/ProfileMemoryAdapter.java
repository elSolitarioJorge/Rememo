package com.ggg.rememo.feature.profile.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.feature.profile.databinding.ItemProfileMemoryBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Profile 模块"我的时光" RecyclerView Adapter。
 * <p>
 * 时间脉络模式：每条记忆显示时间节点（xxxx年x月x日 HH:mm 星期x），
 * 图片按宽度等比加载高度，无图时显示引用图标。
 * </p>
 */
public class ProfileMemoryAdapter extends RecyclerView.Adapter<ProfileMemoryAdapter.ViewHolder> {

    private final List<MemoryPost> memoryPosts = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MemoryPost post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setData(List<MemoryPost> posts) {
        this.memoryPosts.clear();
        if (posts != null) {
            // 按 createdTime 倒序排列（从新到旧）
            List<MemoryPost> sorted = new ArrayList<>(posts);
            Collections.sort(sorted, (a, b) -> {
                long diff = b.getCreatedTime() - a.getCreatedTime();
                if (diff > 0) return 1;
                if (diff < 0) return -1;
                return 0;
            });
            this.memoryPosts.addAll(sorted);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.memoryPosts.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProfileMemoryBinding binding = ItemProfileMemoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(memoryPosts.get(position));
    }

    @Override
    public int getItemCount() {
        return memoryPosts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemProfileMemoryBinding binding;

        ViewHolder(@NonNull ItemProfileMemoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemoryPost post) {
            // 时间节点：xxxx年x月x日 HH:mm 星期x
            binding.tvTimeNode.setText(formatTime(post.getCreatedTime()));

            // 标题
            binding.tvTitle.setText(post.getTitle());

            // 内容摘要
            String content = post.getContent();
            binding.tvContent.setText(!TextUtils.isEmpty(content) ? content : "");

            // 图片处理
            List<MemoryPhoto> images = post.getImages();
            boolean hasImage = images != null && !images.isEmpty();
            binding.cardMemoryContainer.setCardBackgroundColor(0xFF16192B);

            if (hasImage) {
                binding.ivCover.setVisibility(View.VISIBLE);
                binding.ivQuoteIcon.setVisibility(View.GONE);

                String imageUrl = images.get(0).getDisplayUrl();
                loadImageWithWidthAspectRatio(imageUrl, binding.ivCover);
            } else {
                binding.ivCover.setVisibility(View.GONE);
                binding.ivQuoteIcon.setVisibility(View.VISIBLE);
            }

            // Item 点击跳转详情
            binding.cardMemoryContainer.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(post);
                }
            });
        }

        /**
         * 按 ImageView 已测量的宽度，加载图片并按宽度等比设置高度。
         */
        private void loadImageWithWidthAspectRatio(String imageUrl, ImageView imageView) {
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

        private String formatTime(long timestamp) {
            if (timestamp <= 0) return "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日 HH:mm 星期", Locale.CHINESE);
            Date date = new Date(timestamp);
            String base = sdf.format(date);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            String[] weekdays = {"日", "一", "二", "三", "四", "五", "六"};
            return base + weekdays[dayOfWeek - 1];
        }
    }
}
