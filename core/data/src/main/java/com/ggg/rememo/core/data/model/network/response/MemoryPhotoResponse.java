package com.ggg.rememo.core.data.model.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * 记忆图片数据。
 */
public class MemoryPhotoResponse {

    @SerializedName("photoId")
    private String photoId;

    @SerializedName("originalUrl")
    private String originalUrl;

    @SerializedName("restoredUrl")
    private String restoredUrl;

    @SerializedName("displayState")
    private String displayState;

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

    public String getDisplayState() {
        return displayState;
    }

    public void setDisplayState(String displayState) {
        this.displayState = displayState;
    }
}
