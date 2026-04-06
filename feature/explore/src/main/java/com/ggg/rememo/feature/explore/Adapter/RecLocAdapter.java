package com.ggg.rememo.feature.explore.Adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.ui.R;
import com.ggg.rememo.feature.explore.databinding.ItemLocation1Binding;
import com.ggg.rememo.feature.explore.databinding.ItemLocation2Binding;

import java.util.ArrayList;
import java.util.List;

public class RecLocAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_LOCATION = 0;
    private static final int TYPE_MAP_ENTRY = 1;

    private List<MemoryPoint> memoryPoints;

    public RecLocAdapter() {
        this.memoryPoints = new ArrayList<>();
    }

    public void updateData(List<MemoryPoint> newItems) {
        this.memoryPoints.clear();
        if (newItems != null) {
            for (int i = 0; i < 2 && i < newItems.size(); i++) {
                this.memoryPoints.add(newItems.get(i));
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_MAP_ENTRY) {
            ItemLocation2Binding binding = ItemLocation2Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MapViewHolder(binding);
        } else {
            ItemLocation1Binding binding = ItemLocation1Binding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LocationViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LocationViewHolder && position < memoryPoints.size()) {
            ((LocationViewHolder) holder).bind(memoryPoints.get(position));
        }
        // MapViewHolder 无需数据绑定，布局已是静态样式
    }

    @Override
    public int getItemViewType(int position) {
        return position < 2 ? TYPE_LOCATION : TYPE_MAP_ENTRY;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        private final ItemLocation1Binding binding;
        public LocationViewHolder(ItemLocation1Binding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPoint item) {
            if (item == null) {
                return;
            }

            String coverUrl = item.getCoverImageUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                Glide.with(binding.getRoot())
                        .load(coverUrl)
                        .placeholder(new ColorDrawable(Color.GRAY))
                        .error(R.drawable.img_error)
                        .centerCrop()
                        .into(binding.imgPointCover);
            } else {
                binding.imgPointCover.setImageResource(com.ggg.rememo.core.ui.R.drawable.cover_placeholder);
            }

            binding.textLocationName.setText(item.getPointName() != null ? item.getPointName() : "");
            binding.textMemoryCount.setText(item.getMemoryCount() + "条回忆");
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
