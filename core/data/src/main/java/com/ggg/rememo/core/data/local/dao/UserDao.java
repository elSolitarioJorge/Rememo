package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.ggg.rememo.core.data.model.entity.User;

import java.util.List;

@Dao
public interface UserDao {
    /** 插入用户 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);
    /** 批量插入用户 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    /** 根据 ID 删除用户 */
    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteById(String userId);
    /** 根据 ID 获取用户 */
    @Query("SELECT * FROM users WHERE userId = :userId")
    User getById(String userId);
    /** 按昵称搜索用户 */
    @Query("SELECT * FROM users WHERE nickname LIKE '%' || :keyword || '%'")
    List<User> searchByNickName(String keyword);
}
