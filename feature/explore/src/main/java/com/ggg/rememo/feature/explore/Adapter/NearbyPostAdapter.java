package com.ggg.rememo.feature.explore.Adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.explore.databinding.ItemNearbyPostBinding;

import java.util.List;

public class NearbyPostAdapter extends RecyclerView.Adapter<NearbyPostAdapter.NearbyPostViewHolder> {
    private List<MemoryPost> items;
    public NearbyPostAdapter(List<MemoryPost> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public NearbyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNearbyPostBinding binding = ItemNearbyPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NearbyPostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyPostViewHolder holder, int position) {
        MemoryPost item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class NearbyPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemNearbyPostBinding binding;
        public NearbyPostViewHolder(ItemNearbyPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPost item) {
            // 封面图设置
            Glide.with(binding.getRoot())
                    .load(com.ggg.rememo.feature.explore.R.drawable.pic_location)
                    // .load(item.getImages().get(0).getDisplayUrl())
                    .placeholder(new ColorDrawable(Color.GRAY))
                    .error(com.ggg.rememo.core.ui.R.drawable.img_error)
                    .centerCrop()
                    .into(binding.imgPostCover);

            // 位置设置
            // binding.textDistance.setText(AMapUtils.calculateLineDistance(new LatLng(), new LatLng()));

            // 标题
            binding.textPostTitle.setText(item.getTitle());
            // 内容
            binding.textPostContent.setText(item.getContent());
        }
    }
}
