package com.ggg.rememo.feature.timeline.model;

import com.ggg.rememo.core.data.model.entity.MemoryPost;

import java.util.ArrayList;
import java.util.List;

/**
 * 季节分组数据模型
 * <p>
 * 用于年份档案页的季节脉络视图，将同一年份的记忆按季节分组。
 * 季节顺序固定：冬 -> 秋 -> 夏 -> 春
 * </p>
 */
public class SeasonSection {

    /** 季节名称：冬、秋、夏、春 */
    public final String seasonName;

    /** 该季节下的记忆列表 */
    public final List<MemoryPost> posts;

    public SeasonSection(String seasonName, List<MemoryPost> posts) {
        this.seasonName = seasonName;
        this.posts = posts != null ? posts : new ArrayList<>();
    }

    public static final String SEASON_WINTER = "冬";
    public static final String SEASON_AUTUMN = "秋";
    public static final String SEASON_SUMMER = "夏";
    public static final String SEASON_SPRING = "春";

    /** 季节显示顺序：冬秋夏春 */
    public static final String[] SEASON_ORDER = {
            SEASON_WINTER, SEASON_AUTUMN, SEASON_SUMMER, SEASON_SPRING
    };
}
