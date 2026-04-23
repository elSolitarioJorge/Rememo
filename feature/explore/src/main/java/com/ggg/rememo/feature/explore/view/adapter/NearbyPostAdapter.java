package com.ggg.rememo.feature.explore.view.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
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
    private int columnWidth = 0;

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

    private void calculateColumnWidth(Context context) {
        if (columnWidth > 0) return;
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        // RecyclerView paddingHorizontal="8dp" -> 16dp
        // ItemCardView marginStart/End="3dp" -> 6dp per item -> 12dp for 2 columns
        // Total margin = 28dp
        float totalMarginDp = 28f;
        int totalMarginPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, totalMarginDp, displayMetrics);
        columnWidth = (screenWidth - totalMarginPx) / 2;
    }

    @NonNull
    @Override
    public NearbyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        calculateColumnWidth(parent.getContext());
        ItemNearbyPostBinding binding = ItemNearbyPostBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NearbyPostViewHolder(binding, columnWidth);
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
        private final int columnWidth;

        public NearbyPostViewHolder(ItemNearbyPostBinding binding, int columnWidth) {
            super(binding.getRoot());
            this.binding = binding;
            this.columnWidth = columnWidth;
        }

        private void bind(MemoryPost item, String distance, NearbyPostAdapter adapter) {
            binding.getRoot().setOnClickListener(v -> adapter.performClick(item));

            List<MemoryPhoto> photos = item.getImages();
            if (photos != null && !photos.isEmpty()) {
                float ratio = item.getCoverImageRatio();
                if (ratio <= 0) ratio = 1.0f; 
                
                int targetHeight = (int) (columnWidth / ratio);
                
                ViewGroup.LayoutParams params = binding.imgPostCover.getLayoutParams();
                if (params.height != targetHeight) {
                    params.height = targetHeight;
                    binding.imgPostCover.setLayoutParams(params);
                }

                Glide.with(binding.imgPostCover.getContext())
                        .load(photos.get(0).getDisplayUrl())
                        .placeholder(com.ggg.rememo.core.ui.R.drawable.ic_no_image)
                        .override(columnWidth, targetHeight)
                        .centerCrop()
                        .into(binding.imgPostCover);
            } else {
                binding.imgPostCover.setImageResource(com.ggg.rememo.core.ui.R.drawable.ic_no_image);
                ViewGroup.LayoutParams params = binding.imgPostCover.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.imgPostCover.setLayoutParams(params);
            }

            if (distance != null) {
                binding.textDistance.setText("距您" + distance);
            }

            binding.textPostTitle.setText(item.getTitle());
            binding.textPostContent.setText(item.getContent());
        }
    }
}
