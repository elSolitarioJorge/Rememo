package com.ggg.rememo.feature.explore.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.ui.R;
import com.ggg.rememo.feature.explore.databinding.ItemContentPicBinding;
import com.ggg.rememo.feature.explore.databinding.ItemContentTextBinding;

import java.util.List;

public class RecPostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int TYPE_PIC = 0;
    private final int TYPE_TEXT = 1;
    private List<MemoryPost> items;

    public RecPostAdapter(List<MemoryPost> items) {
        this.items = items;
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
            ((PicPostViewHolder) holder).bind(item);
        } else {
            ((TextPostViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        MemoryPost item = items.get(position);
        List<MemoryPhoto> photos = item.getImages();

        if (photos != null && !photos.isEmpty()) {
            // 有图片，返回图片类型
            return TYPE_PIC;
        } else {
            // 无图片
            return TYPE_TEXT;
        }
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class PicPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemContentPicBinding binding;
        public PicPostViewHolder(ItemContentPicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPost item) {

            // 封面图设置
            Glide.with(binding.getRoot())
                    .load(com.ggg.rememo.feature.explore.R.drawable.pic_location)
//                    .load(item.getImages().get(0).getDisplayUrl())
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .error(R.drawable.img_error)
                    .centerCrop()
                    .into(binding.imgCover);

            // 用户头像设置
            /*Glide.with(binding.getRoot())
                    .load(item.getAuthorId())
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .error(R.drawable.img_error)
                    .centerCrop()
                    .into(binding.imgPostAvatar);*/

            // 时间标签
            binding.textTimeLabel.setText(item.getMemoryYear() + "年" + item.getMemorySeason());
            // 标题
            binding.textPostTitle.setText(item.getTitle());
            // 用户名称
            // binding.textPostUserName.setText(item.getAuthorId());

            binding.textLikeCount.setText(item.getLikeCount());
        }
    }

    static class TextPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemContentTextBinding binding;
        public TextPostViewHolder(ItemContentTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPost item) {
            binding.textTitle.setText(item.getTitle());
            binding.textContent.setText(item.getContent());
            binding.textLikeCount.setText(String.valueOf(item.getLikeCount()));
            // binding.textUserName.setText(item.getAuthorId());
        }
    }
}
