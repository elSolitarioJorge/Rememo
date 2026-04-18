package com.ggg.rememo.core.data.mapper;

import com.ggg.rememo.core.data.model.entity.User;
import com.ggg.rememo.core.data.model.network.response.UserInfo;

/**
 * UserInfo ↔ User 转换器。
 * 网络响应到本地实体的统一映射逻辑。
 */
public class UserMapper {

    private UserMapper() {}

    public static User toEntity(UserInfo response) {
        if (response == null) return null;
        User user = new User();
        user.setUserId(response.getUserId());
        user.setPhone(response.getPhone());
        user.setNickname(response.getNickname());
        user.setAvatar(response.getAvatar());
        user.setGender(response.getGender());
        user.setBio(response.getBio());
        user.setCreatedAt(response.getCreatedAt());
        return user;
    }
}
