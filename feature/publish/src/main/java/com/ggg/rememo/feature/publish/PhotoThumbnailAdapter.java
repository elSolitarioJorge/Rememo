package com.ggg.rememo.feature.publish;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.local.ImageStorageHelper;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.feature.publish.databinding.ItemPhotoThumbnailBinding;

import java.util.ArrayList;
import java.util.List;

public class PhotoThumbnailAdapter extends RecyclerView.Adapter<PhotoThumbnailAdapter.PhotoViewHolder> {
    private static final int MAX_PHOTOS = 9;
    private final List<MemoryPhoto> photos = new ArrayList<>();
    private int selectedPosition = 0; // 默认选中第一张
    private OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoSelected(MemoryPhoto photo);
        void onAddMoreClicked();
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.listener = listener;
    }

    // 更新数据并刷新
    // 注意：图片 URI 会被立即复制到应用的内部存储目录 (files/images/)，
    // 避免依赖不稳定的 content:// URI，确保发布后能可靠读取。
    public void addPhotos(Context context, List<Uri> newUris) {
        int currentSize = photos.size();
        for (Uri uri : newUris) {
            if (photos.size() < MAX_PHOTOS) {
                // 将 content:// URI 复制到内部持久化存储，获取稳定路径
                String stablePath = ImageStorageHelper.copyUriToInternalStorage(context, uri);
                MemoryPhoto photo = new MemoryPhoto();
                photo.setOriginalUrl(stablePath);
                photos.add(photo);
            }
        }
        notifyDataSetChanged();
        // 如果是首次添加，默认选中第一张
        if (currentSize == 0 && !photos.isEmpty() && listener != null) {
            listener.onPhotoSelected(photos.get(0));
        }
    }

    public List<MemoryPhoto> getPhotos() {
        return photos;
    }

    @Override
    public int getItemCount() {
        // 如果还没达到上限，数量 = 图片数 + 1（加号）；否则只有图片数
        return photos.size() < MAX_PHOTOS ? photos.size() + 1 : photos.size();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhotoThumbnailBinding binding = ItemPhotoThumbnailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PhotoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        boolean isAddButton = (position == photos.size());

        if (isAddButton) {
            holder.binding.ivThumbnail.setVisibility(View.GONE);
            holder.binding.ivAddMore.setVisibility(View.VISIBLE);
            holder.binding.viewSelectedMask.setVisibility(View.GONE);
            holder.binding.getRoot().setStrokeWidth(0); // 加号不需要边框

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onAddMoreClicked();
            });
        } else {
            holder.binding.ivThumbnail.setVisibility(View.VISIBLE);
            holder.binding.ivAddMore.setVisibility(View.GONE);
            MemoryPhoto photo = photos.get(position);

            Glide.with(holder.itemView.getContext())
                    .load(photo.getOriginalUrl())
                    .into(holder.binding.ivThumbnail);

            // 处理选中状态
            boolean isSelected = (position == selectedPosition);
            holder.binding.viewSelectedMask.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            holder.binding.getRoot().setStrokeWidth(isSelected ? 4 : 0); // 选中时显示主题色边框

            holder.itemView.setOnClickListener(v -> {
                int previousSelected = selectedPosition;
                selectedPosition = holder.getBindingAdapterPosition();
                notifyItemChanged(previousSelected);
                notifyItemChanged(selectedPosition);
                if (listener != null) listener.onPhotoSelected(photo);
            });
        }
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ItemPhotoThumbnailBinding binding;

        public PhotoViewHolder(ItemPhotoThumbnailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
