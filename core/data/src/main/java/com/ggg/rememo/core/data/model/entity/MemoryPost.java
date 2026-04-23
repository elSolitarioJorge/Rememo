package com.ggg.rememo.core.data.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;
import java.util.Objects;

@Entity(tableName = "memory_posts")
public class MemoryPost implements Parcelable {
    @PrimaryKey
    @NonNull
    private String postId = "";   // 记忆ID

    // 外键关联
    private String pointId;  // 锚点ID
    private String authorId;   // 用户ID
    private String authorNickname; // 作者昵称
    private String authorAvatar;   // 作者头像

    // 核心内容
    private String title;    // 标题
    private String content;  // 正文
    private List<MemoryPhoto> images; // 图片列表

    // 记忆发生时间
    private int memoryYear;  // 发生年份
    private String memorySeason; // 季节
    private String address;      // 详细地址


    // 互动数据
    private int likeCount;       // 点赞数量
    private int commentCount;    // 评论数量
    private int collectCount;    // 收藏数量
    private boolean isLiked;    // 当前用户是否点赞
    private boolean isCollected; // 当前用户是否收藏
    private float coverImageRatio = 1.0f; // 封面图宽高比，默认1.0

    // 记录元数据
    private long createdTime;   // 记忆创建时间戳

    public MemoryPost() {

    }

    @Ignore
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        this.isLiked = liked;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        this.isCollected = collected;
    }

    public float getCoverImageRatio() {
        return coverImageRatio;
    }

    public void setCoverImageRatio(float coverImageRatio) {
        this.coverImageRatio = coverImageRatio;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
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
        dest.writeString(authorNickname);
        dest.writeString(authorAvatar);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeTypedList(images);
        dest.writeInt(memoryYear);
        dest.writeString(memorySeason);
        dest.writeString(address);
        dest.writeInt(likeCount);
        dest.writeInt(commentCount);
        dest.writeInt(collectCount);
        dest.writeInt(isLiked ? 1 : 0);
        dest.writeInt(isCollected ? 1 : 0);
        dest.writeFloat(coverImageRatio);
        dest.writeLong(createdTime);
    }

    protected MemoryPost(Parcel in) {
        postId = Objects.requireNonNull(in.readString());
        pointId = in.readString();
        authorId = in.readString();
        authorNickname = in.readString();
        authorAvatar = in.readString();
        title = in.readString();
        content = in.readString();
        images = in.createTypedArrayList(MemoryPhoto.CREATOR);
        memoryYear = Integer.parseInt(Objects.requireNonNull(in.readString()));
        memorySeason = in.readString();
        address = in.readString();
        likeCount = in.readInt();
        commentCount = in.readInt();
        collectCount = in.readInt();
        isLiked = in.readInt() == 1;
        isCollected = in.readInt() == 1;
        coverImageRatio = in.readFloat();
        createdTime = in.readLong();
    }

    // 必须提供 CREATOR 字段（静态、final、名为 CREATOR）
    public static final Creator<MemoryPost> CREATOR = new Creator<MemoryPost>() {
        @Override
        public MemoryPost createFromParcel(Parcel in) {
            return new MemoryPost(in);
        }
        @Override
        public MemoryPost[] newArray(int size) {
            return new MemoryPost[size];
        }
    };
}
