package com.ggg.rememo.feature.message.Adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.ggg.rememo.feature.message.DataModule.ItemCommentMsg;
import com.ggg.rememo.feature.message.DataModule.ItemLikeMsg;
import com.ggg.rememo.feature.message.DataModule.ItemSystemMsg;
import com.ggg.rememo.feature.message.DataModule.ItemTimeHeadMsg;
import com.ggg.rememo.feature.message.DataModule.ListItem;
import com.ggg.rememo.feature.message.databinding.MainRecyItemCommentBinding;
import com.ggg.rememo.feature.message.databinding.MainRecyItemLikeBinding;
import com.ggg.rememo.feature.message.databinding.MainRecyItemSystemBinding;
import com.ggg.rememo.feature.message.databinding.MainRecyItemTimeBinding;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ListItem> items;

    public MessageAdapter(List<ListItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_TIME_HEADER) {
            MainRecyItemTimeBinding timeBinding = MainRecyItemTimeBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new HeadTimeViewHolder(timeBinding);
        } else if (viewType == ListItem.TYPE_COMMENT) {
            MainRecyItemCommentBinding commentBinding = MainRecyItemCommentBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new CommentViewHolder(commentBinding);
        } else if (viewType == ListItem.TYPE_LIKE) {
            MainRecyItemLikeBinding likeBinding = MainRecyItemLikeBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new LikeViewHolder(likeBinding);
        } else {
            MainRecyItemSystemBinding systemBinding = MainRecyItemSystemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new SystemViewHolder(systemBinding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ListItem item = items.get(position);
        if (item.getType() == ListItem.TYPE_TIME_HEADER) {
            ((HeadTimeViewHolder) holder).bind((ItemTimeHeadMsg) item);
        } else if (item.getType() == ListItem.TYPE_COMMENT) {
            ((CommentViewHolder) holder).bind((ItemCommentMsg) item);
        } else if (item.getType() == ListItem.TYPE_LIKE) {
            ((LikeViewHolder) holder).bind((ItemLikeMsg) item);
        } else {
            ((SystemViewHolder) holder).bind((ItemSystemMsg) item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeadTimeViewHolder extends RecyclerView.ViewHolder {
        private final MainRecyItemTimeBinding binding;
        public HeadTimeViewHolder(MainRecyItemTimeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(ItemTimeHeadMsg item) {
            binding.timeHead.setText(item.getTime());
        }
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final MainRecyItemCommentBinding binding;
        public CommentViewHolder(MainRecyItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(ItemCommentMsg item) {
            Glide.with(binding.getRoot())
                    .load(item.getAvatarResId())
                    .into(binding.msgCommentAvatar);

            Glide.with(binding.getRoot())
                    .load(item.getCoverResId())
                    .into(binding.commentPostCardCover);

            binding.msgCommentName.setText(item.getUserName());
            binding.commentMsg.setText(item.getComment());
            binding.msgCommentTime.setText(item.getTime());
            binding.commentPostCardTitle.setText(item.getPostTitle());
            binding.commentPostCardContent.setText(item.getPostContent());
        }
    }


    static class LikeViewHolder extends RecyclerView.ViewHolder {
        private final MainRecyItemLikeBinding binding;
        public LikeViewHolder(MainRecyItemLikeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(ItemLikeMsg item) {
            Glide.with(binding.getRoot())
                    .load(item.getAvatarResId())
                    .into(binding.msgLikeAvatar);

            binding.msgLikeName.setText(item.getUserName());
            binding.likePostCardTitle.setText(item.getPostTitle());
            binding.likePostCardContent.setText(item.getPostContent());
        }
    }

    static class SystemViewHolder extends RecyclerView.ViewHolder {
        private final MainRecyItemSystemBinding binding;
        public SystemViewHolder(MainRecyItemSystemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        private void bind(ItemSystemMsg item) {
            Glide.with(binding.getRoot())
                    .load(item.getImageResId())
                    .into(binding.systemAvatar);

            binding.systemTitle.setText(item.getTitle());
            binding.systemMsg.setText(item.getContent());
            binding.systemTime.setText(item.getTime());
        }
    }
}
