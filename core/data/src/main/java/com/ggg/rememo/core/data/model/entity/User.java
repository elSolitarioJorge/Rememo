package com.ggg.rememo.core.data.model.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "users")
public class User implements Parcelable {

    @PrimaryKey
    @NonNull
    private String userId = "";
    private String phone;
    private String nickname;
    private String avatar;
    private String gender = "secret";
    private String bio = "";
    private long createdAt = 0;

    public User() {
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // ==================== Parcelable 实现 ====================

    protected User(Parcel in) {
        userId = Objects.requireNonNull(in.readString());
        phone = in.readString();
        nickname = in.readString();
        avatar = in.readString();
        gender = in.readString();
        bio = in.readString();
        createdAt = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(phone);
        dest.writeString(nickname);
        dest.writeString(avatar);
        dest.writeString(gender);
        dest.writeString(bio);
        dest.writeLong(createdAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
