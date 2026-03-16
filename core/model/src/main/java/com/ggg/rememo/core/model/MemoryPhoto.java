package com.ggg.rememo.core.model;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MemoryPhoto {
    private String photoId;
    private String originalUrl; // 原图的本地路径或网络链接
    private String restoredUrl; // 修复图的本地路径或网络链接
    private PhotoState currentState = PhotoState.ORIGINAL; // 图片状态

    public enum PhotoState {
        ORIGINAL,
        RESTORED
    }

    public String getDisplayUrl() {
        if (currentState == PhotoState.RESTORED && restoredUrl != null && !restoredUrl.isEmpty()) {
            return restoredUrl;
        }
        return originalUrl; // 默认或者没有修复图时，显示原图
    }

    public void toggleState() {
        if (currentState == PhotoState.ORIGINAL) {
            currentState = PhotoState.RESTORED;
        } else {
            currentState = PhotoState.ORIGINAL;
        }
    }

    public PhotoState getCurrentState() {
        return currentState;
    }

    public MemoryPhoto() {
    }

    public MemoryPhoto(String photoId, String originalUrl, String restoredUrl, PhotoState currentState) {
        this.photoId = photoId;
        this.originalUrl = originalUrl;
        this.restoredUrl = restoredUrl;
        this.currentState = currentState;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public String getRestoredUrl() {
        return restoredUrl;
    }

    public void setRestoredUrl(String restoredUrl) {
        this.restoredUrl = restoredUrl;
    }

    public void setCurrentState(PhotoState currentState) {
        this.currentState = currentState;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MemoryPhoto)) return false;
        MemoryPhoto that = (MemoryPhoto) o;
        return Objects.equals(photoId, that.photoId) && Objects.equals(originalUrl, that.originalUrl) && Objects.equals(restoredUrl, that.restoredUrl) && currentState == that.currentState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(photoId, originalUrl, restoredUrl, currentState);
    }

    @NonNull
    @Override
    public String toString() {
        return "MemoryPhoto{" +
                "photoId='" + photoId + '\'' +
                ", originalUrl='" + originalUrl + '\'' +
                ", restoredUrl='" + restoredUrl + '\'' +
                ", currentState=" + currentState +
                '}';
    }
}
