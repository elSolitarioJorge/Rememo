package com.ggg.rememo.core.data.model.network.request;

import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * 发布记忆请求参数。
 */
public class CreateMemoryPostRequest {

    @SerializedName("pointId")
    private String pointId;

    @SerializedName("pointName")
    private String pointName;

    @SerializedName("lat")
    private double lat;

    @SerializedName("lng")
    private double lng;

    @SerializedName("address")
    private String address;

    @SerializedName("title")
    private String title;

    @SerializedName("content")
    private String content;

    @SerializedName("images")
    private List<MemoryPhotoRequest> images;

    @SerializedName("memoryYear")
    private int memoryYear;

    @SerializedName("memorySeason")
    private String memorySeason;

    public CreateMemoryPostRequest(String pointId, String pointName, double lat, double lng, String address,
                                    String title, String content, List<MemoryPhotoRequest> images,
                                    int memoryYear, String memorySeason) {
        this.pointId = pointId;
        this.pointName = pointName;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.title = title;
        this.content = content;
        this.images = images;
        this.memoryYear = memoryYear;
        this.memorySeason = memorySeason;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<MemoryPhotoRequest> getImages() {
        return images;
    }

    public void setImages(List<MemoryPhotoRequest> images) {
        this.images = images;
    }

    public int getMemoryYear() {
        return memoryYear;
    }

    public void setMemoryYear(int memoryYear) {
        this.memoryYear = memoryYear;
    }

    public String getMemorySeason() {
        return memorySeason;
    }

    public void setMemorySeason(String memorySeason) {
        this.memorySeason = memorySeason;
    }

    /**
     * 图片请求参数内部类。
     */
    public static class MemoryPhotoRequest {

        @SerializedName("photoId")
        private String photoId;

        @SerializedName("originalUrl")
        private String originalUrl;

        @SerializedName("restoredUrl")
        private String restoredUrl;

        @SerializedName("displayState")
        private String displayState;

        public MemoryPhotoRequest(String photoId, String originalUrl, String restoredUrl, String displayState) {
            this.photoId = photoId;
            this.originalUrl = originalUrl;
            this.restoredUrl = restoredUrl;
            this.displayState = displayState;
        }

        public static MemoryPhotoRequest fromEntity(MemoryPhoto photo) {
            if (photo == null) return null;
            return new MemoryPhotoRequest(
                    photo.getPhotoId(),
                    photo.getOriginalUrl(),
                    photo.getRestoredUrl(),
                    photo.getCurrentState() != null ? photo.getCurrentState().name() : null
            );
        }

        public static List<MemoryPhotoRequest> fromEntities(List<MemoryPhoto> photos) {
            if (photos == null || photos.isEmpty()) return null;
            List<MemoryPhotoRequest> requests = new ArrayList<>(photos.size());
            for (MemoryPhoto photo : photos) {
                requests.add(fromEntity(photo));
            }
            return requests;
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

        public String getDisplayState() {
            return displayState;
        }

        public void setDisplayState(String displayState) {
            this.displayState = displayState;
        }
    }
}
