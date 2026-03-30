package com.ggg.rememo.core.network.deepseek;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Streaming;

public interface DeepSeekApi {

    @POST("v1/chat/completions")
    Call<ChatResponse> chatCompletion(
            @Header("Authorization") String auth,
            @Body ChatRequest request
    );

    @Streaming
    @POST("v1/chat/completions")
    Call<ResponseBody> chatCompletionStream(
            @Header("Authorization") String auth,
            @Body RequestBody request
    );
}
