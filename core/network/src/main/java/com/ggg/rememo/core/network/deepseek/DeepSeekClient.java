package com.ggg.rememo.core.network.deepseek;

import android.os.Handler;
import android.os.Looper;

import com.ggg.rememo.core.network.BuildConfig;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DeepSeekClient {

    private static final String BASE_URL = "https://api.deepseek.com/";
    private static volatile DeepSeekClient instance;

    private final DeepSeekApi api;
    private final String apiKey;
    private final Gson gson = new Gson();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private DeepSeekClient() {
        apiKey = "Bearer " + BuildConfig.DEEPSEEK_API_KEY;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(DeepSeekApi.class);
    }

    public static DeepSeekClient getInstance() {
        if (instance == null) {
            synchronized (DeepSeekClient.class) {
                if (instance == null) {
                    instance = new DeepSeekClient();
                }
            }
        }
        return instance;
    }

    /**
     * 发送对话请求
     *
     * @param messages 消息列表
     * @param callback 回调
     */
    public void chat(List<ChatMessage> messages, retrofit2.Callback<ChatResponse> callback) {
        ChatRequest request = new ChatRequest(messages);
        api.chatCompletion(apiKey, request).enqueue(callback);
    }

    /**
     * 发送流式对话请求
     *
     * @param messages 消息列表
     * @param callback 流式回调
     */
    public void chatStream(List<ChatMessage> messages, StreamCallback callback) {
        ChatRequest chatRequest = new ChatRequest(messages, true);
        String json = gson.toJson(chatRequest);
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), json);

        api.chatCompletionStream(apiKey, requestBody).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call,
                                   Response<okhttp3.ResponseBody> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    mainHandler.post(() -> callback.onError("请求失败: " + response.code()));
                    return;
                }

                // 在后台线程读取流数据，避免阻塞主线程
                executor.execute(() -> {
                    StringBuilder fullContent = new StringBuilder();

                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("data: ")) {
                                String data = line.substring(6);
                                if ("[DONE]".equals(data)) {
                                    break;
                                }

                                ChatStreamResponse streamResponse = gson.fromJson(data, ChatStreamResponse.class);
                                if (streamResponse != null) {
                                    String deltaContent = streamResponse.getDeltaContent();
                                    if (deltaContent != null && !deltaContent.isEmpty()) {
                                        fullContent.append(deltaContent);
                                        String content = fullContent.toString();
                                        mainHandler.post(() -> callback.onContent(content));
                                    }

                                    if (streamResponse.isFinished()) {
                                        break;
                                    }
                                }
                            }
                        }

                        String finalContent = fullContent.toString();
                        mainHandler.post(() -> callback.onComplete(finalContent));

                    } catch (IOException e) {
                        mainHandler.post(() -> callback.onError("读取流失败: " + e.getMessage()));
                    }
                });
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                mainHandler.post(() -> callback.onError("网络错误: " + t.getMessage()));
            }
        });
    }
}
