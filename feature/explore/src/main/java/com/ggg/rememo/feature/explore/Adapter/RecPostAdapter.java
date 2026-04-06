package com.ggg.rememo.feature.explore.Adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.ui.R;
import com.ggg.rememo.feature.explore.databinding.ItemContentPicBinding;
import com.ggg.rememo.feature.explore.databinding.ItemContentTextBinding;

import java.util.ArrayList;
import java.util.List;

public class RecPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PIC = 0;
    private static final int TYPE_TEXT = 1;

    private List<MemoryPost> items;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MemoryPost post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public RecPostAdapter(List<MemoryPost> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void updateData(List<MemoryPost> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PIC) {
            ItemContentPicBinding binding = ItemContentPicBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new PicPostViewHolder(binding);
        } else {
            ItemContentTextBinding binding = ItemContentTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new TextPostViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MemoryPost item = items.get(position);
        if (holder instanceof PicPostViewHolder) {
            ((PicPostViewHolder) holder).bind(item, this);
        } else {
            ((TextPostViewHolder) holder).bind(item, this);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MemoryPost item = items.get(position);
        List<MemoryPhoto> photos = item.getImages();
        return (photos != null && !photos.isEmpty()) ? TYPE_PIC : TYPE_TEXT;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    private void notifyClick(MemoryPost post) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(post);
        }
    }

    void performClick(MemoryPost post) {
        notifyClick(post);
    }

    static class PicPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemContentPicBinding binding;
        public PicPostViewHolder(ItemContentPicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPost item, RecPostAdapter adapter) {
            binding.getRoot().setOnClickListener(v -> adapter.performClick(item));

            List<MemoryPhoto> photos = item.getImages();
            if (photos != null && !photos.isEmpty()) {
                loadImageWithAspectRatio(photos.get(0).getDisplayUrl(), binding.imgCover);
            } else {
                binding.imgCover.setImageResource(R.drawable.ic_no_image);
            }

            String season = item.getMemorySeason();
            binding.textTimeLabel.setText(item.getMemoryYear() + "年" + (season != null ? season : ""));
            binding.textPostTitle.setText(item.getTitle() != null ? item.getTitle() : "");

            String authorName = item.getAuthorNickname();
            if (authorName != null && !authorName.isEmpty()) {
                binding.textPostUserName.setText(authorName);
                String avatarUrl = item.getAuthorAvatar();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(binding.getRoot())
                            .load(avatarUrl)
                            .placeholder(new ColorDrawable(Color.GRAY))
                            .error(R.drawable.img_error)
                            .circleCrop()
                            .into(binding.imgPostAvatar);
                }
            }

            binding.textLikeCount.setText(String.valueOf(item.getLikeCount()));
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
    }

    static class TextPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemContentTextBinding binding;
        public TextPostViewHolder(ItemContentTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPost item, RecPostAdapter adapter) {
            binding.getRoot().setOnClickListener(v -> adapter.performClick(item));

            binding.textTitle.setText(item.getTitle() != null ? item.getTitle() : "");
            binding.textContent.setText(item.getContent() != null ? item.getContent() : "");
            binding.textLikeCount.setText(String.valueOf(item.getLikeCount()));
            String authorName = item.getAuthorNickname();
            if (authorName != null && !authorName.isEmpty()) {
                binding.textUserName.setText(authorName);
            }
        }
    }
}
