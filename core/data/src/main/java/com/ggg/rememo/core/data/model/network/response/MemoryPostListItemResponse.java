package com.ggg.rememo.core.data.model.network.response;

import com.google.gson.annotations.SerializedName;

/**
 * 记忆列表项数据（用于列表接口响应）。
 *
 * 对应接口：
 * - GET /api/memory-points/{pointId}/posts（按记忆点）
 * - GET /api/posts/by-year（按年份）
 * - GET /api/users/{userId}/posts（按用户）
 * - GET /api/posts/random（随机）
 *
 * 与 MemoryPostDetail 的区别：
 * - 不含完整正文 contentFull，仅返回 contentPreview（100字符摘要）
 * - 不含完整图片数组，仅返回 coverImage（封面图）和 imageCount（数量）
 */
public class MemoryPostListItemResponse {

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

    @SerializedName("contentPreview")
    private String contentPreview;

    @SerializedName("coverImage")
    private String coverImage;

    @SerializedName("imageCount")
    private int imageCount;

    @SerializedName("coverImageWidth")
    private int coverImageWidth;

    @SerializedName("coverImageHeight")
    private int coverImageHeight;

    @SerializedName("memoryYear")
    private int memoryYear;

    @SerializedName("memorySeason")
    private String memorySeason;

    @SerializedName("likeCount")
    private int likeCount;

    @SerializedName("commentCount")
    private int commentCount;

    @SerializedName("isLiked")
    private boolean isLiked;

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

    public String getContentPreview() {
        return contentPreview;
    }

    public void setContentPreview(String contentPreview) {
        this.contentPreview = contentPreview;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public int getImageCount() {
        return imageCount;
    }

    public void setImageCount(int imageCount) {
        this.imageCount = imageCount;
    }

    public int getCoverImageWidth() {
        return coverImageWidth;
    }

    public void setCoverImageWidth(int coverImageWidth) {
        this.coverImageWidth = coverImageWidth;
    }

    public int getCoverImageHeight() {
        return coverImageHeight;
    }

    public void setCoverImageHeight(int coverImageHeight) {
        this.coverImageHeight = coverImageHeight;
    }

    public float getCoverImageRatio() {
        if (coverImageHeight == 0) return 1.0f;
        return (float) coverImageWidth / coverImageHeight;
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

    public boolean getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
}
