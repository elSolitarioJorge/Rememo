package com.ggg.rememo.core.data.mapper;

import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.network.response.MemoryPhotoResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostDetailResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostListItemResponse;
import com.ggg.rememo.core.data.model.network.response.MemoryPostResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryPost Response → MemoryPost Entity 转换器。
 *
 * <p>提供三种响应对应的转换：</p>
 * <ul>
 *   <li>ListItem   — 发现页 / 时间线 / 地图列表（封面图 + 内容摘要）</li>
 *   <li>Detail      — 详情页（完整正文 + 所有图片）</li>
 *   <li>Post        — 发布成功返回（完整正文 + 所有图片）</li>
 * </ul>
 */
public class MemoryPostMapper {

    private MemoryPostMapper() {}

    // ==================== MemoryPostListItemResponse ====================

    public static List<MemoryPost> fromListItem(List<MemoryPostListItemResponse> responses) {
        if (responses == null) return new ArrayList<>();
        List<MemoryPost> posts = new ArrayList<>(responses.size());
        for (MemoryPostListItemResponse r : responses) {
            posts.add(fromListItem(r));
        }
        return posts;
    }

    public static MemoryPost fromListItem(MemoryPostListItemResponse r) {
        if (r == null) return null;
        MemoryPost post = new MemoryPost();
        post.setPostId(r.getPostId());
        post.setPointId(r.getPointId());
        post.setAuthorId(r.getAuthorId());
        post.setAuthorNickname(r.getAuthorNickname());
        post.setAuthorAvatar(r.getAuthorAvatar());
        post.setTitle(r.getTitle());
        post.setContent(r.getContentPreview());
        post.setMemoryYear(r.getMemoryYear());
        post.setMemorySeason(r.getMemorySeason());
        post.setLikeCount(r.getLikeCount());
        post.setCommentCount(r.getCommentCount());
        post.setCreatedTime(r.getCreatedTime());
        // 列表接口返回封面图 URL，转换为单图实体
        if (r.getCoverImage() != null && !r.getCoverImage().isEmpty()) {
            List<MemoryPhoto> photos = new ArrayList<>();
            photos.add(new MemoryPhoto(null, r.getCoverImage(), null, MemoryPhoto.PhotoState.ORIGINAL));
            post.setImages(photos);
        }
        return post;
    }

    // ==================== MemoryPostDetailResponse ====================

    public static MemoryPost fromDetail(MemoryPostDetailResponse r) {
        if (r == null) return null;
        MemoryPost post = new MemoryPost();
        post.setPostId(r.getPostId());
        post.setPointId(r.getPointId());
        post.setAuthorId(r.getAuthorId());
        post.setAuthorNickname(r.getAuthorNickname());
        post.setAuthorAvatar(r.getAuthorAvatar());
        post.setTitle(r.getTitle());
        post.setContent(r.getContentFull());
        post.setImages(fromPhotoResponseList(r.getImages()));
        post.setMemoryYear(r.getMemoryYear());
        post.setMemorySeason(r.getMemorySeason());
        post.setLikeCount(r.getLikeCount());
        post.setCommentCount(r.getCommentCount());
        post.setCollectCount(r.getCollectCount());
        post.setLiked(r.getIsLiked());
        post.setCollected(r.getIsCollected());
        post.setCreatedTime(r.getCreatedTime());
        return post;
    }

    // ==================== MemoryPostResponse ====================

    public static MemoryPost fromPostResponse(MemoryPostResponse r) {
        if (r == null) return null;
        MemoryPost post = new MemoryPost();
        post.setPostId(r.getPostId());
        post.setPointId(r.getPointId());
        post.setAuthorId(r.getAuthorId());
        post.setAuthorNickname(r.getAuthorNickname());
        post.setAuthorAvatar(r.getAuthorAvatar());
        post.setTitle(r.getTitle());
        post.setContent(r.getContent());
        post.setImages(fromPhotoResponseList(r.getImages()));
        post.setMemoryYear(r.getMemoryYear());
        post.setMemorySeason(r.getMemorySeason());
        post.setLikeCount(r.getLikeCount());
        post.setCommentCount(r.getCommentCount());
        post.setCollectCount(r.getCollectCount());
        post.setCreatedTime(r.getCreatedTime());
        return post;
    }

    // ==================== MemoryPhotoResponse ====================

    public static List<MemoryPhoto> fromPhotoResponseList(List<MemoryPhotoResponse> responses) {
        if (responses == null || responses.isEmpty()) return null;
        List<MemoryPhoto> photos = new ArrayList<>(responses.size());
        for (MemoryPhotoResponse r : responses) {
            photos.add(fromPhotoResponse(r));
        }
        return photos;
    }

    public static MemoryPhoto fromPhotoResponse(MemoryPhotoResponse r) {
        if (r == null) return null;
        MemoryPhoto photo = new MemoryPhoto();
        photo.setPhotoId(r.getPhotoId());
        photo.setOriginalUrl(r.getOriginalUrl());
        photo.setRestoredUrl(r.getRestoredUrl());
        if (r.getDisplayState() != null) {
            try {
                photo.setCurrentState(MemoryPhoto.PhotoState.valueOf(r.getDisplayState()));
            } catch (IllegalArgumentException e) {
                photo.setCurrentState(MemoryPhoto.PhotoState.ORIGINAL);
            }
        } else {
            photo.setCurrentState(MemoryPhoto.PhotoState.ORIGINAL);
        }
        return photo;
    }
}
