package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.ggg.rememo.core.data.model.entity.MemoryPost;

import java.util.List;

@Dao
public interface MemoryPostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemoryPost post);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MemoryPost> posts);

    @Update
    void update(MemoryPost post);

    @Delete
    void delete(MemoryPost post);

    @Query("DELETE FROM memory_posts WHERE postId = :postId")
    void deleteById(String postId);

    @Query("SELECT * FROM memory_posts WHERE postId = :postId")
    MemoryPost getById(String postId);

    @Query("SELECT * FROM memory_posts ORDER BY createdTime DESC")
    List<MemoryPost> getAll();

    @Query("SELECT * FROM memory_posts ORDER BY createdTime DESC LIMIT :limit")
    List<MemoryPost> getRecent(int limit);

    @Query("SELECT * FROM memory_posts WHERE pointId = :pointId ORDER BY memoryYear DESC")
    List<MemoryPost> getByPointId(String pointId);

    @Query("SELECT * FROM memory_posts WHERE authorId = :authorId ORDER BY createdTime DESC")
    List<MemoryPost> getByAuthorId(String authorId);

    @Query("SELECT * FROM memory_posts WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%'")
    List<MemoryPost> search(String keyword);

    @Query("SELECT * FROM memory_posts WHERE memoryYear = :year ORDER BY createdTime DESC")
    List<MemoryPost> getByYear(int year);

    @Query("SELECT * FROM memory_posts WHERE memoryYear BETWEEN :startYear AND :endYear ORDER BY memoryYear DESC")
    List<MemoryPost> getByYearRange(int startYear, int endYear);

    @Query("SELECT COUNT(*) FROM memory_posts")
    int getCount();

    @Query("SELECT COUNT(*) FROM memory_posts WHERE pointId = :pointId")
    int getCountByPointId(String pointId);

    @Query("UPDATE memory_posts SET likeCount = :count WHERE postId = :postId")
    void updateLikeCount(String postId, int count);

    @Query("UPDATE memory_posts SET commentCount = :count WHERE postId = :postId")
    void updateCommentCount(String postId, int count);

    @Query("UPDATE memory_posts SET collectCount = :count WHERE postId = :postId")
    void updateCollectCount(String postId, int count);
}
