package com.ggg.rememo.core.data.model.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * 图片上传响应
 */
public class ImageUploadResponse {

    @SerializedName("imageId")
    private String imageId;

    @SerializedName("originalUrl")
    private String originalUrl;

    @SerializedName("fileSize")
    private long fileSize;

    @SerializedName("width")
    private int width;

    @SerializedName("height")
    private int height;

    @SerializedName("mimeType")
    private String mimeType;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public String toString() {
        return "ImageUploadResponse{" +
                "imageId='" + imageId + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                ", fileSize=" + fileSize +
                ", width=" + width +
                ", height=" + height +
                ", mimeType='" + mimeType + '\'' +
                '}';
    }
}