package com.ggg.rememo.feature.timeline.view.adapter;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.feature.timeline.databinding.ItemTimelineYearBinding;
import com.ggg.rememo.feature.timeline.data.model.TimelineYearModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

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
    private final Set<YearViewHolder> attachedHolders =
            Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean animationsEnabled;
    private boolean released;

    public interface OnMemoryClickListener {
        void onMemoryClick(MemoryPost post);
        void onGatewayClick(TimelineYearModel yearModel);
    }

    public void setOnMemoryClickListener(OnMemoryClickListener listener) {
        this.listener = listener;
    }

    public void setYears(List<TimelineYearModel> years) {
        if (released) {
            return;
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return TimelineYearAdapter.this.years.size();
            }

            @Override
            public int getNewListSize() {
                return years != null ? years.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                // 按年份 ID 判断是否是同一项
                return TimelineYearAdapter.this.years.get(oldItemPosition).getYear()
                        == years.get(newItemPosition).getYear();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                TimelineYearModel oldModel = TimelineYearAdapter.this.years.get(oldItemPosition);
                TimelineYearModel newModel = years.get(newItemPosition);
                // 比较记忆列表内容是否变化（转发 DiffUtil 到内层 MemoryCardAdapter）
                return oldModel.getPosts().equals(newModel.getPosts());
            }
        }, false);

        this.years.clear();
        if (years != null) {
            this.years.addAll(years);
        }
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * 用新数据刷新时间线（由网络数据到达触发，增量更新）
     */
    public void refreshYears(List<TimelineYearModel> newYears) {
        setYears(newYears);
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
        if (animationsEnabled && holder.itemView.isAttachedToWindow()) {
            holder.startAnimations();
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull YearViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        attachedHolders.add(holder);
        if (animationsEnabled) {
            holder.startAnimations();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull YearViewHolder holder) {
        holder.stopAnimations();
        attachedHolders.remove(holder);
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull YearViewHolder holder) {
        holder.stopAnimations();
        attachedHolders.remove(holder);
        super.onViewRecycled(holder);
    }

    /**
     * 页面失去焦点时停止全部可见节点动画，恢复焦点后再按需启动。
     */
    public void setAnimationsEnabled(boolean enabled) {
        if (released || animationsEnabled == enabled) {
            return;
        }
        animationsEnabled = enabled;
        for (YearViewHolder holder : new ArrayList<>(attachedHolders)) {
            if (enabled) {
                holder.startAnimations();
            } else {
                holder.stopAnimations();
            }
        }
    }

    /**
     * Activity 销毁时的最终释放入口，不依赖 RecyclerView 是否触发回收回调。
     */
    public void release() {
        if (released) {
            return;
        }
        released = true;
        animationsEnabled = false;
        for (YearViewHolder holder : new ArrayList<>(attachedHolders)) {
            holder.release();
        }
        attachedHolders.clear();
        listener = null;
        years.clear();
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
        private Runnable startPulseRunnable;

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

            // "探索 N 个记忆" 按钮（年份维度的大入口，和内嵌传送门功能相同，保留 UI 一致性）
            binding.btnExplore.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGatewayClick(yearModel);
                }
            });

            // 卡片点击：记忆卡片跳详情，传送门卡片也走 gateway 回调
            cardAdapter.setOnCardClickListener(post -> {
                if (listener != null) {
                    listener.onMemoryClick(post);
                }
            });
            cardAdapter.setOnGatewayClickListener((year, remaining) -> {
                if (listener != null) {
                    listener.onGatewayClick(yearModel);
                }
            });

            // 传入当年全部记忆数，由 MemoryCardAdapter 决定是否追加传送门
            List<MemoryPost> allPosts = yearModel.getPosts();
            cardAdapter.setPosts(allPosts, yearModel.getMemoryCount(), yearModel.getYear());

        }

        /**
         * 开启节点和流光动画
         */
        private void startAnimations() {
            if (!itemView.isAttachedToWindow()) {
                return;
            }
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
            startPulseRunnable = () -> {
                startPulseRunnable = null;
                if (!itemView.isAttachedToWindow()) {
                    return;
                }

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
            };
            binding.timelineAxisContainer.post(startPulseRunnable);
        }

        /**
         * 停止所有动画
         */
        public void stopAnimations() {
            if (startPulseRunnable != null) {
                binding.timelineAxisContainer.removeCallbacks(startPulseRunnable);
                startPulseRunnable = null;
            }
            if (nodeAnimatorSet != null) {
                ArrayList<Animator> childAnimations = nodeAnimatorSet.getChildAnimations();
                nodeAnimatorSet.cancel();
                nodeAnimatorSet.removeAllListeners();
                for (Animator animator : childAnimations) {
                    animator.cancel();
                    animator.removeAllListeners();
                    if (animator instanceof ObjectAnimator) {
                        ((ObjectAnimator) animator).setTarget(null);
                    }
                }
                nodeAnimatorSet = null;
            }
            if (pulseAnimator != null) {
                pulseAnimator.removeAllUpdateListeners();
                pulseAnimator.cancel();
                pulseAnimator.removeAllListeners();
                pulseAnimator = null;
            }

            // 恢复视图初始状态，防止复用时状态错乱
            binding.timelineNode.setScaleX(1.0f);
            binding.timelineNode.setScaleY(1.0f);
            binding.timelineNode.setAlpha(1.0f);
            binding.timelineAxis.setTranslationY(0f);
        }

        private void release() {
            stopAnimations();
            binding.btnExplore.setOnClickListener(null);
            cardAdapter.setOnCardClickListener(null);
            cardAdapter.setOnGatewayClickListener(null);
            binding.rvHorizontalCards.setAdapter(null);
        }
    }
}
