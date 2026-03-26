package com.ggg.rememo.core.data.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

/**
 * 本地图片文件转 Base64 工具类。
 * 将图片压缩后转为 data:image/{type};base64,{base64内容} 格式，供后端接口使用。
 */
public class ImageToBase64Util {

    /**
     * 将本地图片文件路径转为 Base64 字符串（带头部）。
     *
     * @param filePath 本地文件绝对路径，如 /data/data/.../files/images/<uuid>.jpg
     * @return 格式如 data:image/jpeg;base64,/9j/4AAQSkZJRg...
     */
    public static String filePathToBase64(String filePath) throws Exception {
        FileInputStream fis = new FileInputStream(filePath);
        Bitmap bitmap = BitmapFactory.decodeStream(fis);
        fis.close();

        String mimeType = getMimeType(filePath);
        Bitmap.CompressFormat format = getCompressFormat(mimeType);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(format, 80, outputStream);
        byte[] bytes = outputStream.toByteArray();

        String base64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
        return "data:" + mimeType + ";base64," + base64;
    }

    private static String getMimeType(String filePath) {
        if (filePath == null) return "image/jpeg";
        int lastDot = filePath.lastIndexOf(".");
        if (lastDot < 0) return "image/jpeg";
        String ext = filePath.substring(lastDot + 1).toLowerCase();
        switch (ext) {
            case "png":
                return "image/png";
            case "webp":
                return "image/webp";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            default:
                return "image/jpeg";
        }
    }

    private static Bitmap.CompressFormat getCompressFormat(String mimeType) {
        if ("image/png".equals(mimeType)) return Bitmap.CompressFormat.PNG;
        if ("image/webp".equals(mimeType)) return Bitmap.CompressFormat.WEBP;
        return Bitmap.CompressFormat.JPEG;
    }
}
