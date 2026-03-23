package com.ggg.rememo.feature.timeline.model;

import com.ggg.rememo.core.data.model.entity.MemoryPost;

import java.util.List;

/**
 * 时间线年份视图模型
 * <p>
 * 用于按年份分组展示记忆列表，每个年份下包含多个记忆卡片。
 * </p>
 */
public class TimelineYearModel {

    private final int year;
    private final String subtitle;
    private final int memoryCount;
    private final List<MemoryPost> posts;

    public TimelineYearModel(int year, String subtitle, List<MemoryPost> posts) {
        this.year = year;
        this.subtitle = subtitle;
        this.posts = posts;
        this.memoryCount = posts != null ? posts.size() : 0;
    }

    public int getYear() {
        return year;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public int getMemoryCount() {
        return memoryCount;
    }

    public List<MemoryPost> getPosts() {
        return posts;
    }

    /**
     * 获取探索按钮文本
     */
    public String getExploreText() {
        return "探索 " + memoryCount + " 个记忆 >";
    }
}
