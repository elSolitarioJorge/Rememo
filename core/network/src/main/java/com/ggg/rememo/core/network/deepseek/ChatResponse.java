package com.ggg.rememo.core.network.deepseek;

import java.util.List;

public class ChatResponse {
    public List<Choice> choices;

    public String getContent() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).message.content;
        }
        return null;
    }

    public static class Choice {
        public ChatMessage message;
    }
}
