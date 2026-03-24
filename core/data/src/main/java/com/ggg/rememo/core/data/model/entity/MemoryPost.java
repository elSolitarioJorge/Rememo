package com.ggg.rememo.core.data.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "memory_posts")
public class MemoryPost implements Parcelable {
    @PrimaryKey
    @NonNull
    private String postId = "";   // 记忆ID

    // 外键关联
    private String pointId;  // 锚点ID
    private String authorId;   // 用户ID

    // 核心内容
    private String title;    // 标题
    private String content;  // 正文
    private List<MemoryPhoto> images; // 图片列表

    // 记忆发生时间
    private int memoryYear;  // 发生年份
    private String memorySeason; // 季节


    // 互动数据
    private int likeCount;       // 点赞数量
    private int commentCount;    // 评论数量
    private int collectCount;    // 收藏数量

    // 记录元数据
    private long createdTime;   // 记忆创建时间戳
    private long updatedTime;   // 记忆更新时间戳

    public MemoryPost() {

    }

    public MemoryPost(@NonNull String authorId, String title, String content, int memoryYear, int likeCount) {
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.memoryYear = memoryYear;
        this.likeCount = likeCount;
    }

    @NonNull
    public String getPostId() {
        return postId;
    }

    public void setPostId(@NonNull String postId) {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<MemoryPhoto> getImages() {
        return images;
    }

    public void setImages(List<MemoryPhoto> images) {
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

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    // 描述内容（一般返回0）
    @Override
    public int describeContents() {
        return 0;
    }

    // 将对象写入Parcel
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(postId);
        dest.writeString(pointId);
        dest.writeString(authorId);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeTypedList(images);
        dest.writeInt(memoryYear);
        dest.writeString(memorySeason);
        dest.writeInt(likeCount);
        dest.writeInt(commentCount);
        dest.writeInt(collectCount);
        dest.writeLong(createdTime);
        dest.writeLong(updatedTime);
    }

    protected MemoryPost(Parcel in) {
        postId = in.readString();
        pointId = in.readString();
        authorId = in.readString();
        title = in.readString();
        content = in.readString();
        images = in.createTypedArrayList(MemoryPhoto.CREATOR);
        memoryYear = in.readInt();
        memorySeason = in.readString();
        likeCount = in.readInt();
        commentCount = in.readInt();
        collectCount = in.readInt();
        createdTime = in.readLong();
        updatedTime = in.readLong();
    }

    // 必须提供 CREATOR 字段（静态、final、名为 CREATOR）
    public static final Creator<MemoryPost> CREATOR = new Creator<MemoryPost>() {
        @Override
        public MemoryPost createFromParcel(Parcel in) {
            return new MemoryPost(in); // // 调用带 Parcel 的构造函数
        }
        @Override
        public MemoryPost[] newArray(int size) {
            return new MemoryPost[size];
        }
    };
}
