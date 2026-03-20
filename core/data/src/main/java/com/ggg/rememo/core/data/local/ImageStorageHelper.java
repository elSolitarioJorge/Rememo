package com.ggg.rememo.core.data.local;

import android.content.Context;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.UUID;

/**
 * 本地图片持久化存储工具类
 *
 * 将用户选择的图片和 AI 修复后的图片统一存储到应用的内部存储目录 (files/images/)
 * 而非 content:// URI 或 cacheDir，确保图片在发布后能可靠读取。
 *
 * 目录结构:
 *   files/images/         - 用户发布的原始图片（持久化，不会被系统清理）
 *   files/images/restored/ - AI 修复后的图片（持久化，不会被系统清理）
 *
 * 接入后端时，只需在 PublishRepository.saveMemory() 中遍历 images，
 * 对每个图片路径调用 uploadToCloud(path) 上传到云端，
 * 然后将返回的云端 URL 覆盖 originalUrl/restoredUrl 即可。
 */
public class ImageStorageHelper {

    /** 用户发布的原始图片目录 */
    private static final String IMAGES_DIR = "images";
    /** AI 修复后的图片目录 */
    private static final String RESTORED_DIR = "images/restored";

    /**
     * 将 content:// URI 指向的图片复制到内部存储目录 (files/images/)，
     * 返回稳定的文件绝对路径。
     *
     * @param context Android 上下文
     * @param uri     图片的 content:// URI（来自相册选择器）
     * @return 内部存储中的文件绝对路径，如 /data/data/<package>/files/images/<uuid>.jpg
     */
    public static String copyUriToInternalStorage(Context context, Uri uri) {
        File imageDir = getOrCreateDir(context, IMAGES_DIR);
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File destFile = new File(imageDir, fileName);

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destFile)) {
            if (inputStream == null) {
                throw new RuntimeException("无法打开 Uri: " + uri);
            }
            // 直接流复制，不做压缩，避免质量损失
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("复制图片到内部存储失败: " + e.getMessage(), e);
        }

        return destFile.getAbsolutePath();
    }

    /**
     * 将 Base64 字符串解码并保存到内部存储的 restored 目录，
     * 返回稳定的文件绝对路径。
     *
     * @param context Android 上下文
     * @param base64  AI 修复返回的 Base64 字符串（不带 data:image/...;base64, 头）
     * @return 内部存储中的文件绝对路径
     */
    public static String saveRestoredImage(Context context, String base64) {
        File restoredDir = getOrCreateDir(context, RESTORED_DIR);
        String fileName = UUID.randomUUID().toString() + ".jpg";
        File destFile = new File(restoredDir, fileName);

        try {
            byte[] decodedBytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decodedBytes = Base64.getDecoder().decode(base64);
            }
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(decodedBytes);
                fos.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("保存 AI 修复图片失败: " + e.getMessage(), e);
        }

        return destFile.getAbsolutePath();
    }

    /**
     * 生成一个用于保存 AI 修复结果的稳定文件路径。
     * 不实际创建文件，只返回路径字符串。
     *
     * @param context Android 上下文
     * @return files/images/restored/<uuid>.jpg
     */
    public static String generateRestoredImagePath(Context context) {
        File restoredDir = getOrCreateDir(context, RESTORED_DIR);
        String fileName = UUID.randomUUID().toString() + ".jpg";
        return new File(restoredDir, fileName).getAbsolutePath();
    }

    /**
     * 压缩图片并保存到指定路径（供 BaiduAiUtils 调用）。
     *
     * @param context   Android 上下文
     * @param base64    Base64 字符串
     * @param outputPath 目标文件路径
     * @return 输出文件路径
     */
    public static String saveRestoredImageToPath(Context context, String base64, String outputPath) {
        try {
            byte[] decodedBytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decodedBytes = Base64.getDecoder().decode(base64);
            }
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                fos.write(decodedBytes);
                fos.flush();
            }
            return outputPath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("保存 AI 修复图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取或创建指定目录。
     *
     * @param context Android 上下文
     * @param subDir  相对于 filesDir 的子目录路径，如 "images" 或 "images/restored"
     * @return 目录 File 对象
     */
    private static File getOrCreateDir(Context context, String subDir) {
        File dir = new File(context.getFilesDir(), subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 检查文件是否存在（供后续调试/兼容使用）。
     */
    public static boolean fileExists(String path) {
        if (path == null || path.isEmpty()) return false;
        return new File(path).exists();
    }
}
