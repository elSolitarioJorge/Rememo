package com.ggg.rememo.feature.detail.adapter;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.feature.detail.R;
import com.ggg.rememo.feature.detail.databinding.ItemDetailGalleryBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 记忆详情画廊 Adapter。
 */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    private final List<MemoryPhoto> photos = new ArrayList<>();
    private OnAiFixClickListener listener;

    public interface OnAiFixClickListener {
        void onAiFixClick(int position);
    }

    public void setOnAiFixClickListener(OnAiFixClickListener listener) {
        this.listener = listener;
    }

    public void setPhotos(List<MemoryPhoto> newPhotos) {
        photos.clear();
        if (newPhotos != null) {
            photos.addAll(newPhotos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDetailGalleryBinding itemBinding = ItemDetailGalleryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GalleryViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        holder.bind(photos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        private final ItemDetailGalleryBinding binding;

        GalleryViewHolder(@NonNull ItemDetailGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MemoryPhoto photo, int position) {
            Glide.with(binding.getRoot().getContext())
                    .load(photo.getDisplayUrl())
                    .into(binding.ivGalleryImage);

            boolean isRepaired = photo.getCurrentState() == MemoryPhoto.PhotoState.RESTORED;
            applyFilter(binding.ivGalleryImage, isRepaired);
            updateFixButtonUI(binding, isRepaired);

            binding.btnAiFix.setOnClickListener(v -> {
                int currentPos = getAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                photo.toggleState();
                if (listener != null) {
                    listener.onAiFixClick(currentPos);
                }
            });
        }

        private void applyFilter(ImageView imageView, boolean isRepaired) {
            if (isRepaired) {
                imageView.clearColorFilter();
            } else {
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0.2f);
                ColorMatrix sepiaMatrix = new ColorMatrix();
                sepiaMatrix.setScale(1.2f, 1.0f, 0.8f, 1.0f);
                matrix.postConcat(sepiaMatrix);
                imageView.setColorFilter(new ColorMatrixColorFilter(matrix));
            }
        }

        private void updateFixButtonUI(ItemDetailGalleryBinding binding, boolean isRepaired) {
            TextView tvText = (TextView) binding.btnAiFix.getChildAt(1);
            ImageView ivIcon = (ImageView) binding.btnAiFix.getChildAt(0);

            if (isRepaired) {
                tvText.setText("看原图");
                tvText.setTextColor(Color.WHITE);
                ivIcon.setColorFilter(Color.WHITE);
                binding.btnAiFix.setBackgroundResource(R.drawable.bg_glass_pill);
            } else {
                tvText.setText("AI 修复");
                tvText.setTextColor(Color.parseColor("#F5A623"));
                ivIcon.setColorFilter(Color.parseColor("#F5A623"));
                binding.btnAiFix.setBackgroundResource(R.drawable.bg_btn_amber_glass);
            }
        }
    }
}
