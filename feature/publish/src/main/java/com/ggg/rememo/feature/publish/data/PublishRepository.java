package com.ggg.rememo.feature.publish.data;

import android.content.Context;

import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 发布模块数据仓库（Model 层）
 */
public class PublishRepository {

    private final MemoryPointRepository memoryPointRepository;
    private final MemoryPostRepository memoryPostRepository;

    public PublishRepository(Context context) {
        this.memoryPointRepository = new MemoryPointRepository(context);
        this.memoryPostRepository = new MemoryPostRepository(context);
    }

    /**
     * 保存记忆
     * @param title 标题
     * @param content 文字内容
     * @param images 图片列表
     * @param memoryYear 记忆发生年份
     * @param season 季节
     * @param lat 纬度
     * @param lng 经度
     * @param address 地址
     * @param callback 保存结果回调
     */
    public void saveMemory(String title, String content, List<MemoryPhoto> images,
                          int memoryYear, String season, double lat, double lng, String address,
                          MemoryPostRepository.Callback<Boolean> callback) {
        
        final String pointId = UUID.randomUUID().toString();
        final String postId = UUID.randomUUID().toString();
        
        // 先创建或查找 MemoryPoint
        MemoryPoint memoryPoint = new MemoryPoint();
        memoryPoint.setPointId(pointId);
        memoryPoint.setLatitude(lat);
        memoryPoint.setLongitude(lng);
        memoryPoint.setPointName(address != null ? address : "未命名地点");
        memoryPoint.setLocationAddress(address);
        memoryPoint.setCoverImageUrl(images != null && !images.isEmpty() ? 
            images.get(0).getDisplayUrl() : null);
        memoryPoint.setMemoryCount(1);
        memoryPoint.setMinYear(memoryYear);
        memoryPoint.setMaxYear(memoryYear);
        memoryPoint.setCreatedTime(System.currentTimeMillis());
        memoryPoint.setUpdatedTime(System.currentTimeMillis());

        // 保存 MemoryPoint
        memoryPointRepository.insert(memoryPoint, new MemoryPointRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // 创建 MemoryPost
                MemoryPost memoryPost = new MemoryPost();
                memoryPost.setPostId(postId);
                memoryPost.setPointId(pointId);
                memoryPost.setAuthorId("default_user"); // TODO: 后续从登录模块获取
                memoryPost.setTitle(title);
                memoryPost.setContent(content);
                memoryPost.setMemoryYear(memoryYear);
                memoryPost.setMemorySeason(season);
                
                // 转换图片列表
                if (images != null && !images.isEmpty()) {
                    memoryPost.setImages(new ArrayList<>(images));
                }
                
                memoryPost.setLikeCount(0);
                memoryPost.setCommentCount(0);
                memoryPost.setCollectCount(0);
                memoryPost.setCreatedTime(System.currentTimeMillis());
                memoryPost.setUpdatedTime(System.currentTimeMillis());

                // 保存 MemoryPost
                memoryPostRepository.insert(memoryPost, new MemoryPostRepository.Callback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (callback != null) {
                            callback.onSuccess(true);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        if (callback != null) {
                            callback.onError(e);
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    /**
     * 获取当前位置的地址信息（逆地理编码）
     * @param lat 纬度
     * @param lng 经度
     * @return 地址名称
     */
    public String getAddress(double lat, double lng) {
        // TODO: 调用逆地理编码 API
        return "当前位置";
    }
}
