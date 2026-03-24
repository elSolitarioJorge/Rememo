package com.ggg.rememo.feature.explore.Adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.ui.R;
import com.ggg.rememo.feature.explore.databinding.ItemLocation1Binding;
import com.ggg.rememo.feature.explore.databinding.ItemLocation2Binding;

import java.util.List;

public class RecLocAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LAST = 0;
    private static final int TYPE_LOCATION = 1;

    private List<MemoryPoint> items;

    public RecLocAdapter(List<MemoryPoint> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LOCATION) {
            ItemLocation1Binding binding = ItemLocation1Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LocationViewHolder(binding);
        } else {
            ItemLocation2Binding binding = ItemLocation2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MapViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MemoryPoint item = items.get(position);
        if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).bind(item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == items.size() - 1 ? RecLocAdapter.TYPE_LAST : RecLocAdapter.TYPE_LOCATION;
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        private final ItemLocation1Binding binding;
        public LocationViewHolder(ItemLocation1Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPoint item) {
            Glide.with(binding.getRoot())
                    .asDrawable()
                    .load(com.ggg.rememo.feature.explore.R.drawable.pic_location)
                    // .load(item.getCoverImageUrl())
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .error(R.drawable.img_error)
                    .centerCrop()
                    .into(new CustomTarget<Drawable>() {
                        // 当图片加载完成后调用
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            binding.layoutCover.setBackground(resource);
                        }

                        // 当碎片被销毁时调用
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 创建一个纯灰色背景并设置到layout
                            binding.layoutCover.setBackground(new ColorDrawable(Color.GRAY));
                        }
                    });

            binding.textLocationName.setText(item.getPointName());
            binding.textMemoryCount.setText(item.getMemoryCount() + "条回忆推荐");
        }
    }

    static class MapViewHolder extends RecyclerView.ViewHolder {
        private final ItemLocation2Binding binding;
        public MapViewHolder(ItemLocation2Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
