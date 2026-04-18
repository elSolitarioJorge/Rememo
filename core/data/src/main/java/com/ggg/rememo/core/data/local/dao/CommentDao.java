package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.ggg.rememo.core.data.model.entity.Comment;
import java.util.List;

@Dao
public interface CommentDao {
    /** 插入评论，如果已存在则替换 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);
    /** 批量插入评论 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Comment> comments);
    /** 根据 ID 删除评论 */
    @Query("DELETE FROM comments WHERE commentId = :commentId")
    void deleteById(String commentId);
    /** 根据 帖子Id 获取评论列表 */
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdTime DESC")
    List<Comment> getByPostId(String postId);
    /** 根据 用户Id 获取评论列表 */
    @Query("SELECT * FROM comments WHERE authorId = :authorId ORDER BY createdTime DESC")
    List<Comment> getByAuthorId(String authorId);
    /** 根据 帖子Id 获取评论数量 */
    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    int getCountByPostId(String postId);
    /** 根据 帖子Id 删除所有评论，当一个记忆/帖子被删除时，需要联动删除它关联的所有评论 */
    @Query("DELETE FROM comments WHERE postId = :postId")
    void deleteByPostId(String postId);
}
