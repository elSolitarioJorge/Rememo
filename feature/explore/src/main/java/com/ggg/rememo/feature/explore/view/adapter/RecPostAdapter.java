package com.ggg.rememo.feature.explore.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
    private int columnWidth = 0;

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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        calculateColumnWidth(parent.getContext());
        if (viewType == TYPE_PIC) {
            ItemContentPicBinding binding = ItemContentPicBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new PicPostViewHolder(binding, columnWidth);
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

    void performClick(MemoryPost post) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(post);
        }
    }

    static class PicPostViewHolder extends RecyclerView.ViewHolder {
        private final ItemContentPicBinding binding;
        private final int columnWidth;

        public PicPostViewHolder(ItemContentPicBinding binding, int columnWidth) {
            super(binding.getRoot());
            this.binding = binding;
            this.columnWidth = columnWidth;
        }

        private void bind(MemoryPost item, RecPostAdapter adapter) {
            binding.getRoot().setOnClickListener(v -> adapter.performClick(item));

            List<MemoryPhoto> photos = item.getImages();
            if (photos != null && !photos.isEmpty()) {
                float ratio = item.getCoverImageRatio();
                if (ratio <= 0) ratio = 1.0f;
                int targetHeight = (int) (columnWidth / ratio);

                ViewGroup.LayoutParams params = binding.imgCover.getLayoutParams();
                if (params.height != targetHeight) {
                    params.height = targetHeight;
                    binding.imgCover.setLayoutParams(params);
                }

                Glide.with(binding.imgCover.getContext())
                        .load(photos.get(0).getDisplayUrl())
                        .placeholder(R.drawable.ic_no_image)
                        .override(columnWidth, targetHeight)
                        .centerCrop()
                        .into(binding.imgCover);
            } else {
                binding.imgCover.setImageResource(R.drawable.ic_no_image);
                ViewGroup.LayoutParams params = binding.imgCover.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                binding.imgCover.setLayoutParams(params);
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

            if (item.isLiked()) {
                binding.iconLike.setImageResource(R.drawable.ic_heart);
                binding.iconLike.setColorFilter(Color.parseColor("#EF4444"));
            } else {
                binding.iconLike.setImageResource(R.drawable.ic_gray_like);
                binding.iconLike.setColorFilter(Color.parseColor("#64748B"));
            }
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
