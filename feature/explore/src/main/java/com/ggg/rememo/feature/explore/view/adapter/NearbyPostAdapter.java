package com.ggg.rememo.feature.explore.view.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.explore.databinding.ItemNearbyPostBinding;

import java.util.ArrayList;
import java.util.List;

public class NearbyPostAdapter extends RecyclerView.Adapter<NearbyPostAdapter.NearbyPostViewHolder> {
    private List<MemoryPost> items;
    private List<String> distances;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(MemoryPost post);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public NearbyPostAdapter(List<MemoryPost> items, List<String> distances) {
        this.items = items != null ? items : new ArrayList<>();
        this.distances = distances != null ? distances : new ArrayList<>();
    }

    public void updateData(List<MemoryPost> newItems, List<String> newDistances) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        this.distances = newDistances != null ? newDistances : new ArrayList<>();
        notifyDataSetChanged();
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
        String distance = position < distances.size() ? distances.get(position) : null;
        holder.bind(item, distance, this);
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    void performClick(MemoryPost post) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(post);
        }
    }

    static class NearbyPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemNearbyPostBinding binding;
        public NearbyPostViewHolder(ItemNearbyPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(MemoryPost item, String distance, NearbyPostAdapter adapter) {
            binding.getRoot().setOnClickListener(v -> adapter.performClick(item));

            List<MemoryPhoto> photos = item.getImages();
            if (photos != null && !photos.isEmpty()) {
                loadImageWithAspectRatio(photos.get(0).getDisplayUrl(), binding.imgPostCover);
            } else {
                binding.imgPostCover.setImageResource(com.ggg.rememo.core.ui.R.drawable.ic_no_image);
            }

            if (distance != null) {
                binding.textDistance.setText("距您" + distance);
            }

            binding.textPostTitle.setText(item.getTitle());
            binding.textPostContent.setText(item.getContent());
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
}
