package com.ggg.rememo.feature.timeline.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.timeline.databinding.ItemTimelineYearBinding;
import com.ggg.rememo.feature.timeline.model.TimelineYearModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间线年份 Adapter
 * <p>
 * 用于纵向 RecyclerView，展示按年份分组的时间线。
 * 每个年份项内嵌一个横向 RecyclerView 展示记忆卡片。
 * </p>
 */
public class TimelineYearAdapter extends RecyclerView.Adapter<TimelineYearAdapter.YearViewHolder> {

    private List<TimelineYearModel> years = new ArrayList<>();
    private OnMemoryClickListener listener;

    public interface OnMemoryClickListener {
        void onMemoryClick(MemoryPost post);
        void onExploreClick(TimelineYearModel yearModel);
    }

    public void setOnMemoryClickListener(OnMemoryClickListener listener) {
        this.listener = listener;
    }

    public void setYears(List<TimelineYearModel> years) {
        this.years = years != null ? years : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<TimelineYearModel> getYears() {
        return years;
    }

    @NonNull
    @Override
    public YearViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTimelineYearBinding binding = ItemTimelineYearBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new YearViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull YearViewHolder holder, int position) {
        TimelineYearModel yearModel = years.get(position);
        boolean isLast = (position == years.size() - 1);
        holder.bind(yearModel, isLast, listener);
    }

    @Override
    public void onViewRecycled(@NonNull YearViewHolder holder) {
        super.onViewRecycled(holder);
        // 当 View 被 RecyclerView 回收时，必须停止动画以防止内存泄漏和动画错乱
        holder.stopAnimations();
    }

    @Override
    public int getItemCount() {
        return years.size();
    }

    /**
     * ViewHolder：处理具体的视图绑定和动画逻辑
     */
    public static class YearViewHolder extends RecyclerView.ViewHolder {

        private final ItemTimelineYearBinding binding;
        private final MemoryCardAdapter cardAdapter;

        // 动画对象保留引用，以便在复用时取消
        private AnimatorSet nodeAnimatorSet;
        private ValueAnimator pulseAnimator;

        YearViewHolder(@NonNull ItemTimelineYearBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            // 初始化横向 RecyclerView 和 Adapter (避免在 bind 中重复创建)
            this.cardAdapter = new MemoryCardAdapter();
            binding.rvHorizontalCards.setLayoutManager(
                    new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            binding.rvHorizontalCards.setNestedScrollingEnabled(false);
            binding.rvHorizontalCards.setAdapter(cardAdapter);
        }

        void bind(TimelineYearModel yearModel, boolean isLast, OnMemoryClickListener listener) {
            // 绑定基础数据
            binding.tvYear.setText(String.valueOf(yearModel.getYear()));
            binding.tvYearSubtitle.setText(yearModel.getSubtitle());
            binding.btnExplore.setText(yearModel.getExploreText());

            // 设置点击事件
            binding.btnExplore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExploreClick(yearModel);
                }
            });

            cardAdapter.setOnCardClickListener(post -> {
                if (listener != null) {
                    listener.onMemoryClick(post);
                }
            });

            // 更新卡片数据
            cardAdapter.setPosts(yearModel.getPosts());

            // 启动特效动画
            startAnimations();
        }

        /**
         * 开启节点和流光动画
         */
        private void startAnimations() {
            stopAnimations(); // 确保之前的动画已停止

            // --- 节点心跳呼吸 ---
            ObjectAnimator nodeScaleX = ObjectAnimator.ofFloat(binding.timelineNode, "scaleX", 0.85f, 1.15f);
            ObjectAnimator nodeScaleY = ObjectAnimator.ofFloat(binding.timelineNode, "scaleY", 0.85f, 1.15f);
            ObjectAnimator nodeAlpha = ObjectAnimator.ofFloat(binding.timelineNode, "alpha", 0.6f, 1f);

            nodeScaleX.setRepeatCount(ValueAnimator.INFINITE);
            nodeScaleX.setRepeatMode(ValueAnimator.REVERSE);
            nodeScaleY.setRepeatCount(ValueAnimator.INFINITE);
            nodeScaleY.setRepeatMode(ValueAnimator.REVERSE);
            nodeAlpha.setRepeatCount(ValueAnimator.INFINITE);
            nodeAlpha.setRepeatMode(ValueAnimator.REVERSE);

            nodeAnimatorSet = new AnimatorSet();
            nodeAnimatorSet.playTogether(nodeScaleX, nodeScaleY, nodeAlpha);
            nodeAnimatorSet.setDuration(1200); // 1.2秒一次心跳
            nodeAnimatorSet.start();

            // --- 能量流光自上而下穿梭 ---
            binding.timelineAxisContainer.post(() -> {
                // 防止 post 延时执行时，View 已经被回收
                if (binding.timelineAxisContainer.getWindowToken() == null) return;

                int containerHeight = binding.timelineAxisContainer.getHeight();
                int pulseHeight = binding.timelineAxis.getHeight();

                pulseAnimator = ValueAnimator.ofFloat(-pulseHeight, containerHeight);
                pulseAnimator.setDuration(2500); // 2.5秒流到底部
                pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
                pulseAnimator.setRepeatMode(ValueAnimator.RESTART);
                pulseAnimator.setInterpolator(new DecelerateInterpolator());

                pulseAnimator.addUpdateListener(animation -> {
                    float translationY = (float) animation.getAnimatedValue();
                    binding.timelineAxis.setTranslationY(translationY);
                });
                pulseAnimator.start();
            });
        }

        /**
         * 停止所有动画
         */
        public void stopAnimations() {
            if (nodeAnimatorSet != null) {
                nodeAnimatorSet.cancel();
                nodeAnimatorSet = null;
            }
            if (pulseAnimator != null) {
                pulseAnimator.cancel();
                pulseAnimator = null;
            }

            // 恢复视图初始状态，防止复用时状态错乱
            binding.timelineNode.setScaleX(1.0f);
            binding.timelineNode.setScaleY(1.0f);
            binding.timelineNode.setAlpha(1.0f);
            binding.timelineAxis.setTranslationY(0f);
        }
    }
}