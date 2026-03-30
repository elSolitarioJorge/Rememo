package com.ggg.rememo.core.network.deepseek;

import java.util.List;

/**
 * 流式响应解析类
 * SSE 格式: data: {"id":"...","choices":[{"delta":{"content":"xxx"}}]}
 */
public class ChatStreamResponse {
    public String id;
    public List<StreamChoice> choices;

    public String getDeltaContent() {
        if (choices != null && !choices.isEmpty()) {
            StreamChoice choice = choices.get(0);
            if (choice.delta != null) {
                return choice.delta.content;
            }
        }
        return null;
    }

    public boolean isFinished() {
        if (choices != null && !choices.isEmpty()) {
            return choices.get(0).finish_reason != null;
        }
        return false;
    }

    public static class StreamChoice {
        public Delta delta;
        public String finish_reason;
    }

    public static class Delta {
        public String content;
        public String role;
    }
}
