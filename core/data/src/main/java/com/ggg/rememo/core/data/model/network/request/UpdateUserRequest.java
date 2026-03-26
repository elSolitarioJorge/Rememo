package com.ggg.rememo.core.data.model.network.request;

import com.google.gson.annotations.SerializedName;

/**
 * 更新用户信息请求参数。
 * 供 PUT /api/user/info 使用。
 */
public class UpdateUserRequest {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("avatar")
    private String avatar;

    @SerializedName("gender")
    private String gender;

    @SerializedName("bio")
    private String bio;

    public UpdateUserRequest(String nickname, String avatar, String gender, String bio) {
        this.nickname = nickname;
        this.avatar = avatar;
        this.gender = gender;
        this.bio = bio;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
