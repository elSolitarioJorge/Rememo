package com.ggg.rememo.core.data.model.summary;

public class MemoryPointSummary {
    private String pointId;
    private String pointName;
    private String locationAddress;
    private String coverImageUrl;
    private String summaryText;
    private int memoryCount;
    private int minYear;
    private int maxYear;

    public MemoryPointSummary() {

    }

    public String getYearRange() {
        if (minYear < 1900 || maxYear > 2026) return "未知年份";
        if (minYear == maxYear) return String.valueOf(minYear);
        return minYear + " - " + maxYear;
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

    public int getMemoryCount() {
        return memoryCount;
    }

    public void setMemoryCount(int memoryCount) {
        this.memoryCount = memoryCount;
    }
}
