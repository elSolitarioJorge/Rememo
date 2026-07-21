package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.ggg.rememo.core.data.model.entity.MemoryPost;

import java.util.List;

@Dao
public interface MemoryPostDao {
    /** 插入记忆帖子 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemoryPost post);
    /** 批量插入记忆 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MemoryPost> posts);
    /** 根据 ID 删除记忆 */
    @Query("DELETE FROM memory_posts WHERE postId = :postId")
    void deleteById(String postId);
    /** 根据 ID 获取记忆 */
    @Query("SELECT * FROM memory_posts WHERE postId = :postId")
    MemoryPost getById(String postId);
    /** 获取所有记忆 */
    @Query("SELECT * FROM memory_posts ORDER BY createdTime DESC")
    List<MemoryPost> getAll();
    /** 获取最近的记忆 */
    @Query("SELECT * FROM memory_posts ORDER BY createdTime DESC LIMIT :limit")
    List<MemoryPost> getRecent(int limit);
    /** 根据 记忆点ID 获取记忆列表 */
    @Query("SELECT * FROM memory_posts WHERE pointId = :pointId ORDER BY memoryYear DESC")
    List<MemoryPost> getByPointId(String pointId);
    /** 根据 用户ID 获取记忆列表 */
    @Query("SELECT * FROM memory_posts WHERE authorId = :authorId ORDER BY createdTime DESC")
    List<MemoryPost> getByAuthorId(String authorId);
    /** 删除某个用户的帖子缓存 */
    @Query("DELETE FROM memory_posts WHERE authorId = :authorId")
    void deleteByAuthorId(String authorId);
    /**
     * 用服务端当前快照替换某个用户的帖子缓存，避免已删除的帖子长期残留。
     */
    @Transaction
    default void replaceByAuthorId(String authorId, List<MemoryPost> posts) {
        deleteByAuthorId(authorId);
        if (posts != null && !posts.isEmpty()) {
            insertAll(posts);
        }
    }
    /** 更新点赞数量 */
    @Query("UPDATE memory_posts SET likeCount = :count WHERE postId = :postId")
    void updateLikeCount(String postId, int count);
    /** 更新评论数量 */
    @Query("UPDATE memory_posts SET commentCount = :count WHERE postId = :postId")
    void updateCommentCount(String postId, int count);
    /** 更新收藏数量 */
    @Query("UPDATE memory_posts SET collectCount = :count WHERE postId = :postId")
    void updateCollectCount(String postId, int count);
    /** 更新点赞状态 */
    @Query("UPDATE memory_posts SET isLiked = :isLiked, likeCount = :likeCount WHERE postId = :postId")
    void updateLikeStatus(String postId, boolean isLiked, int likeCount);
    /** 更新收藏状态 */
    @Query("UPDATE memory_posts SET isCollected = :isCollected, collectCount = :collectCount WHERE postId = :postId")
    void updateCollectStatus(String postId, boolean isCollected, int collectCount);
}
