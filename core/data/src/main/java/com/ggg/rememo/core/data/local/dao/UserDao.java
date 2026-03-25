package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.ggg.rememo.core.data.model.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<User> users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("DELETE FROM users WHERE userId = :userId")
    void deleteById(String userId);

    @Query("SELECT * FROM users WHERE userId = :userId")
    User getById(String userId);

    @Query("SELECT * FROM users ORDER BY userId DESC")
    List<User> getAll();

    @Query("SELECT * FROM users ORDER BY userId DESC LIMIT :limit")
    List<User> getRecent(int limit);

    @Query("SELECT * FROM users WHERE nickname LIKE '%' || :keyword || '%'")
    List<User> searchByNickName(String keyword);

    @Query("SELECT COUNT(*) FROM users")
    int getCount();
}
