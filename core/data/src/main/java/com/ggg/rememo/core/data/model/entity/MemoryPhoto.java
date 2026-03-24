package com.ggg.rememo.core.data.model.entity;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class MemoryPhoto implements Parcelable {
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(photoId);
        dest.writeString(originalUrl);
        dest.writeString(restoredUrl);
        dest.writeInt(currentState.ordinal());
    }

    protected MemoryPhoto(Parcel in) {
        photoId = in.readString();
        originalUrl = in.readString();
        restoredUrl = in.readString();
        int stateOrdinal = in.readInt();
        currentState = PhotoState.values()[stateOrdinal];
    }

    public static final Creator<MemoryPhoto> CREATOR = new Creator<MemoryPhoto>() {
        @Override
        public MemoryPhoto createFromParcel(Parcel in) {
            return new MemoryPhoto(in);
        }

        @Override
        public MemoryPhoto[] newArray(int size) {
            return new MemoryPhoto[size];
        }
    };
}
