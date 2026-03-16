package com.ggg.rememo.feature.publish.data;

/**
 * 发布模块数据仓库（Model 层）
 */
public class PublishRepository {

    /**
     * 保存记忆
     * @param content 文字内容
     * @param imagePath 图片路径（可为空）
     * @param lat 纬度
     * @param lng 经度
     * @return 是否保存成功
     */
    public boolean saveMemory(String content, String imagePath, double lat, double lng) {
        // TODO: 实现实际的存储逻辑
        return true;
    }

    /**
     * 获取当前位置的地址信息
     * @param lat 纬度
     * @param lng 经度
     * @return 地址名称
     */
    public String getAddress(double lat, double lng) {
        // TODO: 调用逆地理编码 API
        return "当前位置";
    }
}
