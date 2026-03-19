package com.ggg.rememo.feature.message.DataModule;

public class ItemLikeMsg extends ListItem {
    private int AvatarResId;
    private String UserName;
    private int count;
    private String time;
    private String LikeContent;
    private String PostTitle;
    private String PostContent;

    public ItemLikeMsg() {
    }

    public ItemLikeMsg(int avatarResId, String userName, int count, String time, String likeContent, String postTitle, String postContent) {
        AvatarResId = avatarResId;
        UserName = userName;
        this.count = count;
        this.time = time;
        LikeContent = likeContent;
        PostTitle = postTitle;
        PostContent = postContent;
    }

    public int getAvatarResId() {
        return AvatarResId;
    }

    public void setAvatarResId(int avatarResId) {
        AvatarResId = avatarResId;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLikeContent() {
        return LikeContent;
    }

    public void setLikeContent(String likeContent) {
        LikeContent = likeContent;
    }

    public String getPostTitle() {
        return PostTitle;
    }

    public void setPostTitle(String postTitle) {
        PostTitle = postTitle;
    }

    public String getPostContent() {
        return PostContent;
    }

    public void setPostContent(String postContent) {
        PostContent = postContent;
    }

    @Override
    public int getType() {
        return ListItem.TYPE_LIKE;
    }
}
