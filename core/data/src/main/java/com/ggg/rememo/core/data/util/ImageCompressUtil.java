package com.ggg.rememo.core.data.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * 图片压缩工具类，用于上传前压缩图片。
 * 不改变原始文件，仅生成压缩后的临时副本供上传使用。
 */
public class ImageCompressUtil {

    private static final String TAG = "ImageCompressUtil";
    private static final int TARGET_MAX_SIZE = 1920;
    private static final int COMPRESS_QUALITY = 85;

    /**
     * 压缩图片到目标最大边长，默认 1920px，质量 85%。
     * 保持原始格式（JPEG → JPEG，PNG → PNG，WebP → WebP）。
     *
     * @param originalPath 原始图片路径
     * @return 压缩后的临时文件路径
     */
    public static String compressForUpload(String originalPath) {
        Bitmap bitmap = null;
        File temporaryFile = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(originalPath, options);
            int width = options.outWidth;
            int height = options.outHeight;
            int sampleSize = 1;
            while (width / sampleSize > TARGET_MAX_SIZE || height / sampleSize > TARGET_MAX_SIZE) {
                sampleSize *= 2;
            }
            options.inSampleSize = sampleSize;
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeFile(originalPath, options);

            if (bitmap == null) {
                Log.e(TAG, "compressForUpload: 解码图片失败，返回原路径");
                return originalPath;
            }

            String mimeType = options.outMimeType;
            Bitmap.CompressFormat format = getCompressFormat(mimeType);
            String extension = getExtension(mimeType);
            String parentDir = new File(originalPath).getParent();
            String tempPath = parentDir + File.separator + "upload_compressed_"
                    + UUID.randomUUID().toString() + extension;
            temporaryFile = new File(tempPath);

            try (FileOutputStream fos = new FileOutputStream(tempPath)) {
                if (!bitmap.compress(format, COMPRESS_QUALITY, fos)) {
                    if (temporaryFile.exists() && !temporaryFile.delete()) {
                        Log.w(TAG, "compressForUpload: 删除无效临时文件失败 " + tempPath);
                    }
                    return originalPath;
                }
            }

            Log.d(TAG, "compressForUpload: " + originalPath + " -> " + tempPath
                    + " (" + new File(originalPath).length() / 1024 + "KB -> " + new File(tempPath).length() / 1024 + "KB)");
            return tempPath;
        } catch (IOException e) {
            Log.e(TAG, "compressForUpload: 写入临时文件失败", e);
            if (temporaryFile.exists() && !temporaryFile.delete()) {
                Log.w(TAG, "compressForUpload: 删除写入失败的临时文件失败 "
                        + temporaryFile.getAbsolutePath());
            }
            return originalPath;
        } finally {
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * 获取图片的真实 MIME 类型。
     *
     * @param path 图片路径
     * @return MIME 类型字符串，如 "image/jpeg"、"image/png"，默认为 "image/jpeg"
     */
    public static String getMimeType(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        String mimeType = options.outMimeType;
        return mimeType != null ? mimeType : "image/jpeg";
    }

    private static Bitmap.CompressFormat getCompressFormat(String mimeType) {
        switch (mimeType) {
            case "image/png":
                return Bitmap.CompressFormat.PNG;
            case "image/webp":
                return Bitmap.CompressFormat.WEBP;
            default:
                return Bitmap.CompressFormat.JPEG;
        }
    }

    private static String getExtension(String mimeType) {
        switch (mimeType) {
            case "image/png":
                return ".png";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg";
        }
    }
}
