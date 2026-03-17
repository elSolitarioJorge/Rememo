package com.ggg.rememo.feature.publish;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BaiduAiUtils {
    private static final String API_KEY = "IyEWzbhK6zYcYv2CNBGsrGfv";
    private static final String SECRET_KEY = "8Tm7xFCbhndCdzuceER9KEdgx9wQMyKe";

    private static final OkHttpClient client = new OkHttpClient();
    private static String cachedAccessToken = null;

    // 获取 Access Token
    public static String getAccessToken() throws Exception {
        if (cachedAccessToken != null) return cachedAccessToken;

        String authUrl = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials"
                + "&client_id=" + API_KEY
                + "&client_secret=" + SECRET_KEY;

        Request request = new Request.Builder()
                .url(authUrl)
                .post(RequestBody.create(new byte[0], null)) // 空 body
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonStr = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
                cachedAccessToken = jsonObject.get("access_token").getAsString();
                return cachedAccessToken;
            }
        }
        throw new Exception("获取 Access Token 失败");
    }

    // 2. 调用黑白图片上色接口
    public static String colourize(Context context, Uri imageUri) throws Exception {
        // 获取 Token
        String token = getAccessToken();

        // 将本地 Uri 图片转为 Base64 字符串
        String base64Image = uriToBase64(context, imageUri);

        // 构建请求发送给百度 API
        String url = "https://aip.baidubce.com/rest/2.0/image-process/v1/colourize?access_token=" + token;

        // OkHttp 的 FormBody 会自动帮我们做 UrlEncode
        RequestBody formBody = new FormBody.Builder()
                .add("image", base64Image)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(formBody)
                .build();

        // 解析返回结果
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

                if (jsonObject.has("image")) {
                    String resultBase64 = jsonObject.get("image").getAsString();
                    // 将百度返回的 Base64 图片保存到本地缓存，并返回临时文件的路径
                    return saveBase64ToFile(context, resultBase64);
                } else if (jsonObject.has("error_msg")) {
                    throw new Exception("API 报错: " + jsonObject.get("error_msg").getAsString());
                }
            }
        }
        throw new Exception("上色请求失败");
    }

    // 将 Uri 转为 Base64 (百度要求不带 data:image/jpeg;base64, 头部)
    private static String uriToBase64(Context context, Uri uri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // 压缩图片质量，避免 Base64 过大导致请求失败
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    // 将百度返回的 Base64 存为本地缓存文件，方便 Glide 加载
    private static String saveBase64ToFile(Context context, String base64Str) throws Exception {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.NO_WRAP);

        // 在应用的缓存目录下创建一个临时文件
        File cacheDir = context.getCacheDir();
        File tempFile = new File(cacheDir, "repaired_" + UUID.randomUUID().toString() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(decodedBytes);
            fos.flush();
        }
        // 返回文件的绝对路径
        return tempFile.getAbsolutePath();
    }
}