package com.ggg.rememo.feature.message.DataModule;

public class ItemCommentMsg extends ListItem {
    private int AvatarResId;
    private String UserName;
    private String Comment;
    private String Time;
    private int CoverResId;
    private String PostTitle;
    private String PostContent;

    public ItemCommentMsg() {
    }

    public ItemCommentMsg(int avatarResId, String userName, String comment, String time, int coverResId, String postTitle, String postContent) {
        AvatarResId = avatarResId;
        UserName = userName;
        Comment = comment;
        Time = time;
        CoverResId = coverResId;
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

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public int getCoverResId() {
        return CoverResId;
    }

    public void setCoverResId(int coverResId) {
        CoverResId = coverResId;
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
        return ListItem.TYPE_COMMENT;
    }
}
