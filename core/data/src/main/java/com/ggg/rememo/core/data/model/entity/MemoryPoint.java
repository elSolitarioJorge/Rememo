package com.ggg.rememo.core.data.model.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "memory_points")
public class MemoryPoint {
    @PrimaryKey
    @NonNull
    private String pointId = "";   // 锚点ID

    // 基础地理信息
    private double latitude;  // 纬度
    private double longitude; // 经度
    private String pointName; // 锚点名称
    private String locationAddress;   // 详细地址


    // 聚合统计信息
    private String coverImageUrl; // 封面图
    private int memoryCount;    // 记忆数量
    private String summaryText; // 一段文字摘要

    // 年份区间
    private int minYear;        // 最小年份
    private int maxYear;        // 最大年份

    // 记录元数据
    private long createdTime;   // 锚点创建时间戳
    private long updatedTime;   // 锚点更新时间戳

    public MemoryPoint() {

    }

    @NonNull
    public String getPointId() {
        return pointId;
    }

    public void setPointId(@NonNull String pointId) {
        this.pointId = pointId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public int getMemoryCount() {
        return memoryCount;
    }

    public void setMemoryCount(int memoryCount) {
        this.memoryCount = memoryCount;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }

    public int getMinYear() {
        return minYear;
    }

    public void setMinYear(int minYear) {
        this.minYear = minYear;
    }

    public int getMaxYear() {
        return maxYear;
    }

    public void setMaxYear(int maxYear) {
        this.maxYear = maxYear;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }
}
