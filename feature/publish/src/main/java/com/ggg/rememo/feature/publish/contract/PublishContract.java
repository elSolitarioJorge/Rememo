package com.ggg.rememo.feature.publish.contract;

import com.ggg.rememo.core.base.BaseView;

/**
 * 发布模块 MVP 契约接口
 */
public interface PublishContract {

    /**
     * View 接口
     */
    interface View extends BaseView {
        /**
         * 显示发布成功
         */
        void showPublishSuccess();

        /**
         * 显示位置信息
         * @param address 地址名称
         * @param lat 纬度
         * @param lng 经度
         */
        void showLocation(String address, double lat, double lng);
    }

    /**
     * Presenter 接口
     */
    interface Presenter {
        /**
         * 发布记忆
         * @param content 文字内容
         * @param imagePath 图片路径（可为空）
         * @param lat 纬度
         * @param lng 经度
         */
        void publish(String content, String imagePath, double lat, double lng);

        /**
         * 设置当前位置
         * @param lat 纬度
         * @param lng 经度
         */
        void setLocation(double lat, double lng);
    }
}
