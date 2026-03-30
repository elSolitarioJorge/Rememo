package com.ggg.rememo.feature.here.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ggg.rememo.feature.here.databinding.ItemChatAiBinding;
import com.ggg.rememo.feature.here.databinding.ItemChatUserBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;

    private final List<ChatItem> items = new ArrayList<>();
    private final Markwon markwon;
    private final Markwon markwonStreaming;
    private final AtomicLong idGenerator = new AtomicLong(0);
    private AiVH streamingVH;

    public ChatAdapter(Context context) {
        // 带表格插件，用于历史消息和流式结束后的最终渲染
        this.markwon = Markwon.builder(context)
                .usePlugin(TablePlugin.create(context))
                .build();
        // 不带表格插件，用于流式过程中实时渲染（避免表格反复重建导致闪烁）
        this.markwonStreaming = Markwon.create(context);
        setHasStableIds(true);
    }

    public void addMessage(String role, String content) {
        items.add(new ChatItem(role, content, idGenerator.getAndIncrement()));
        notifyItemInserted(items.size() - 1);
    }

    /**
     * 流式更新：用不带表格插件的 Markwon 实时渲染，避免表格闪烁
     */
    public void updateLastAiMessageStreaming(String content) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if ("assistant".equals(items.get(i).role)) {
                items.get(i).content = content;
                if (streamingVH != null && streamingVH.getAdapterPosition() == i) {
                    markwonStreaming.setMarkdown(streamingVH.binding.tvAiMsg, content);
                }
                return;
            }
        }
    }

    /**
     * 流式结束，用 Markwon 渲染最终 Markdown
     */
    public void finishStreaming() {
        for (int i = items.size() - 1; i >= 0; i--) {
            if ("assistant".equals(items.get(i).role)) {
                if (streamingVH != null && streamingVH.getAdapterPosition() == i) {
                    markwon.setMarkdown(streamingVH.binding.tvAiMsg, items.get(i).content);
                }
                return;
            }
        }
    }

    /**
     * 非流式更新（错误消息等）
     */
    public void updateLastAiMessage(String content) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if ("assistant".equals(items.get(i).role)) {
                items.get(i).content = content;
                notifyItemChanged(i);
                return;
            }
        }
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public int getItemViewType(int position) {
        return "user".equals(items.get(position).role) ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            return new UserVH(ItemChatUserBinding.inflate(inflater, parent, false));
        } else {
            return new AiVH(ItemChatAiBinding.inflate(inflater, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatItem item = items.get(position);
        if (holder instanceof UserVH) {
            ((UserVH) holder).binding.tvUserMsg.setText(item.content);
        } else if (holder instanceof AiVH) {
            AiVH aiHolder = (AiVH) holder;
            markwon.setMarkdown(aiHolder.binding.tvAiMsg, item.content);
            if (position == items.size() - 1) {
                streamingVH = aiHolder;
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class UserVH extends RecyclerView.ViewHolder {
        final ItemChatUserBinding binding;
        UserVH(ItemChatUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class AiVH extends RecyclerView.ViewHolder {
        final ItemChatAiBinding binding;
        AiVH(ItemChatAiBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ChatItem {
        String role;
        String content;
        final long id;

        ChatItem(String role, String content, long id) {
            this.role = role;
            this.content = content;
            this.id = id;
        }
    }
}
