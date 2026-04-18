package com.ggg.rememo.feature.publish.contract;

import com.ggg.rememo.core.base.IBaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;

import java.util.List;

/**
 * 发布模块 MVP 契约接口
 */
public interface PublishContract {

    /**
     * View 接口
     */
    interface View extends IBaseView {
        /**
         * 显示发布成功
         */
        void showPublishSuccess();

        /**
         * 显示图片上传进度。
         * @param current 当前已完成的图片数量
         * @param total 图片总数量
         */
        void showUploadProgress(int current, int total);

        /**
         * 获取地址 - 供 Presenter 调用
         * @return 地址字符串
         */
        String getAddress();

        /**
         * 获取时间显示文本 - 供 Presenter 调用
         * @return 时间显示文本，如 "2024 · 冬"
         */
        String getTimeDisplayText();
    }

    /**
     * Presenter 接口
     */
    interface Presenter {
        /**
         * 发布记忆
         * @param title 标题
         * @param content 文字内容
         * @param images 图片列表
         * @param lat 纬度
         * @param lng 经度
         * @param pointId 记忆点ID
         * @param pointName 记忆点名称
         */
        void publish(String title, String content, List<MemoryPhoto> images, double lat, double lng, String pointId, String pointName);
    }
}
