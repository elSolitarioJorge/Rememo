package com.ggg.rememo.core.data.model.network.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 记忆详情数据（用于详情接口响应）。
 *
 * 对应接口：
 * - GET /api/posts/{postId}（记忆详情）
 *
 * 与 MemoryPostListItem 的区别：
 * - 包含完整正文 contentFull（不截断）
 * - 包含完整图片数组 images（所有图片详情）
 */
public class MemoryPostDetailResponse {

    @SerializedName("postId")
    private String postId;

    @SerializedName("pointId")
    private String pointId;

    @SerializedName("authorId")
    private String authorId;

    @SerializedName("authorNickname")
    private String authorNickname;

    @SerializedName("authorAvatar")
    private String authorAvatar;

    @SerializedName("title")
    private String title;

    @SerializedName("contentFull")
    private String contentFull;

    @SerializedName("images")
    private List<MemoryPhotoResponse> images;

    @SerializedName("memoryYear")
    private int memoryYear;

    @SerializedName("memorySeason")
    private String memorySeason;

    @SerializedName("likeCount")
    private int likeCount;

    @SerializedName("commentCount")
    private int commentCount;

    @SerializedName("collectCount")
    private int collectCount;

    @SerializedName("isLiked")
    private boolean isLiked;

    @SerializedName("isCollected")
    private boolean isCollected;

    @SerializedName("createdTime")
    private long createdTime;

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorNickname() {
        return authorNickname;
    }

    public void setAuthorNickname(String authorNickname) {
        this.authorNickname = authorNickname;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentFull() {
        return contentFull;
    }

    public void setContentFull(String contentFull) {
        this.contentFull = contentFull;
    }

    public List<MemoryPhotoResponse> getImages() {
        return images;
    }

    public void setImages(List<MemoryPhotoResponse> images) {
        this.images = images;
    }

    public int getMemoryYear() {
        return memoryYear;
    }

    public void setMemoryYear(int memoryYear) {
        this.memoryYear = memoryYear;
    }

    public String getMemorySeason() {
        return memorySeason;
    }

    public void setMemorySeason(String memorySeason) {
        this.memorySeason = memorySeason;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(int collectCount) {
        this.collectCount = collectCount;
    }

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public boolean getIsCollected() {
        return isCollected;
    }

    public void setIsCollected(boolean isCollected) {
        this.isCollected = isCollected;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
