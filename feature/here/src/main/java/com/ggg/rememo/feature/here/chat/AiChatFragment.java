package com.ggg.rememo.feature.here.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ggg.rememo.core.network.deepseek.ChatMessage;
import com.ggg.rememo.core.network.deepseek.DeepSeekClient;
import com.ggg.rememo.core.network.deepseek.StreamCallback;
import com.ggg.rememo.feature.here.databinding.FragmentAiChatBinding;
import com.tencent.mmkv.MMKV;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AiChatFragment extends Fragment {

    private static final String KEY_SYSTEM_PROMPT = "你是 Rememo 时空记忆助手，帮助用户回忆和探索他们的记忆点。请用简洁友好的中文回答。";
    private static final String KEY_CHAT_HISTORY = "ai_chat_memory";

    private FragmentAiChatBinding binding;
    private ChatAdapter adapter;
    private LinearLayoutManager layoutManager;
    private final List<ChatMessage> conversationHistory = new ArrayList<>();
    private boolean isStreaming = false;
    private boolean userScrolledUp = false;
    private MMKV mmkv;

    public static AiChatFragment newInstance() {
        return new AiChatFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAiChatBinding.inflate(inflater, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            int bottomPadding = Math.max(systemBars.bottom, ime.bottom);
            v.setPadding(0, systemBars.top, 0, bottomPadding);
            return insets;
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mmkv = MMKV.defaultMMKV();

        adapter = new ChatAdapter(requireContext());
        layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        binding.rvChat.setLayoutManager(layoutManager);
        binding.rvChat.setAdapter(adapter);

        // 用户主动上滑时停止自动滚动，下滑到底部时恢复
        binding.rvChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {
                    userScrolledUp = false;
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && recyclerView.canScrollVertically(1)) {
                    userScrolledUp = true;
                }
            }
        });

        conversationHistory.add(new ChatMessage("system", KEY_SYSTEM_PROMPT));
        loadChatHistory();

        if (adapter.getItemCount() == 0) {
            adapter.addMessage("assistant", "你好！我是 Rememo 时空记忆助手，有什么可以帮你的吗？");
        }

        binding.btnBack.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.btnSend.setOnClickListener(v -> sendMessage());
        binding.etInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String text = binding.etInput.getText().toString().trim();
        if (text.isEmpty()) return;

        binding.etInput.setText("");

        adapter.addMessage("user", text);
        conversationHistory.add(new ChatMessage("user", text));
        adapter.addMessage("assistant", "...");
        userScrolledUp = false;
        scrollToBottom();

        setInputEnabled(false);
        isStreaming = true;

        DeepSeekClient.getInstance().chatStream(new ArrayList<>(conversationHistory),
                new StreamCallback() {
                    @Override
                    public void onContent(String content) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            adapter.updateLastAiMessageStreaming(content);
                            if (!userScrolledUp) {
                                scrollToBottom();
                            }
                        });
                    }

                    @Override
                    public void onComplete(String fullContent) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            isStreaming = false;
                            setInputEnabled(true);
                            conversationHistory.add(new ChatMessage("assistant", fullContent));
                            saveChatHistory();
                            adapter.finishStreaming();
                            if (!userScrolledUp) {
                                scrollToBottom();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            isStreaming = false;
                            setInputEnabled(true);
                            adapter.updateLastAiMessage(error);
                            scrollToBottom();
                        });
                    }
                });
    }

    private void setInputEnabled(boolean enabled) {
        binding.btnSend.setEnabled(enabled);
        binding.etInput.setEnabled(enabled);
    }

    private void scrollToBottom() {
        if (!isAdded()) return;
        int count = adapter.getItemCount();
        if (count > 0) {
            // 用负的大偏移让 item 底部对齐 RecyclerView 底部
            layoutManager.scrollToPositionWithOffset(count - 1, -Integer.MAX_VALUE / 2);
        }
    }

    // ==================== 缓存相关 ====================

    private void saveChatHistory() {
        try {
            JSONArray jsonArray = new JSONArray();
            // 跳过系统提示词，只保存用户和助手的对话
            for (int i = 1; i < conversationHistory.size(); i++) {
                ChatMessage msg = conversationHistory.get(i);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("role", msg.role);
                jsonObject.put("content", msg.content);
                jsonArray.put(jsonObject);
            }
            mmkv.encode(KEY_CHAT_HISTORY, jsonArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadChatHistory() {
        String json = mmkv.decodeString(KEY_CHAT_HISTORY);
        if (json == null || json.isEmpty()) return;

        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String role = jsonObject.getString("role");
                String content = jsonObject.getString("content");

                // 恢复对话历史
                conversationHistory.add(new ChatMessage(role, content));
                // 显示在 UI 上
                adapter.addMessage(role, content);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 清除对话历史
     */
    public void clearChatHistory() {
        conversationHistory.clear();
        conversationHistory.add(new ChatMessage("system", KEY_SYSTEM_PROMPT));
        mmkv.remove(KEY_CHAT_HISTORY);

        if (adapter != null) {
            adapter.clear();
            adapter.addMessage("assistant", "对话已清空，有什么可以帮你的吗？");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
