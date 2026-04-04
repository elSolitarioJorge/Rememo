package com.ggg.rememo.core.data.model.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * 发布评论请求参数。
 */
public class CreateCommentRequest {

    @SerializedName("postId")
    private String postId;

    @SerializedName("content")
    private String content;

    public CreateCommentRequest(String postId, String content) {
        this.postId = postId;
        this.content = content;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
