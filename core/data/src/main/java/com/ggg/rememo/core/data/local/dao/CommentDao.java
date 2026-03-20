package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.ggg.rememo.core.data.model.entity.Comment;

import java.util.List;

@Dao
public interface CommentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Comment comment);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Comment> comments);

    @Update
    void update(Comment comment);

    @Delete
    void delete(Comment comment);

    @Query("DELETE FROM comments WHERE commentId = :commentId")
    void deleteById(String commentId);

    @Query("SELECT * FROM comments WHERE commentId = :commentId")
    Comment getById(String commentId);

    @Query("SELECT * FROM comments ORDER BY createdTime DESC")
    List<Comment> getAll();

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdTime DESC")
    List<Comment> getByPostId(String postId);

    @Query("SELECT * FROM comments WHERE authorId = :authorId ORDER BY createdTime DESC")
    List<Comment> getByAuthorId(String authorId);

    @Query("SELECT COUNT(*) FROM comments WHERE postId = :postId")
    int getCountByPostId(String postId);

    @Query("DELETE FROM comments WHERE postId = :postId")
    void deleteByPostId(String postId);
}
