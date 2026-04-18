package com.ggg.rememo.feature.timeline.contract;

import com.ggg.rememo.core.base.IBaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.feature.timeline.data.model.SeasonSection;

import java.util.List;

/**
 * 年份档案模块 MVP 契约接口
 */
public interface ArchiveContract {

    interface View extends IBaseView {
        /**
         * 显示按季节分组后的记忆列表
         * @param sections 季节分组列表，顺序为：冬、秋、夏、春
         */
        void showData(List<SeasonSection> sections);

        /**
         * 显示记忆点信息到 Header
         * @param point       记忆点详情
         * @param totalCount  该年份的记忆总数
         */
        void showPointInfo(MemoryPoint point, int totalCount);
    }

    interface Presenter {
        /**
         * 加载年份记忆数据
         * @param year    年份
         * @param pointId 地点ID
         */
        void loadData(int year, String pointId);
    }
}
