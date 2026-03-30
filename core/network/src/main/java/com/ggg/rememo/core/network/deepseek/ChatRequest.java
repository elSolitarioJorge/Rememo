package com.ggg.rememo.core.network.deepseek;

import java.util.List;

public class ChatRequest {
    public String model = "deepseek-chat";
    public List<ChatMessage> messages;
    public float temperature = 0.7f;
    public boolean stream = false;

    public ChatRequest(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public ChatRequest(List<ChatMessage> messages, boolean stream) {
        this.messages = messages;
        this.stream = stream;
    }
}
