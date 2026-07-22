package com.ggg.rememo.core.data.repository;


import com.ggg.rememo.core.common.util.AppContext;
import com.ggg.rememo.core.data.local.dao.CommentDao;
import com.ggg.rememo.core.data.local.database.RememoDatabase;
import com.ggg.rememo.core.data.model.entity.Comment;

import java.util.List;
import java.util.concurrent.Executor;

public class CommentRepository {

    private final CommentDao commentDao;
    private final Executor databaseExecutor;

    public CommentRepository() {
        RememoDatabase database = RememoDatabase.getInstance(AppContext.get());
        this.commentDao = database.commentDao();
        this.databaseExecutor = DataTaskExecutor.get();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void insert(Comment comment, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                commentDao.insert(comment);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void insertAll(List<Comment> comments, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                commentDao.insertAll(comments);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }


    public void deleteById(String commentId, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                commentDao.deleteById(commentId);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }


    public void getByPostId(String postId, Callback<List<Comment>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<Comment> result = commentDao.getByPostId(postId);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getByAuthorId(String authorId, Callback<List<Comment>> callback) {
        databaseExecutor.execute(() -> {
            try {
                List<Comment> result = commentDao.getByAuthorId(authorId);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getCountByPostId(String postId, Callback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int result = commentDao.getCountByPostId(postId);
                if (callback != null) {
                    callback.onSuccess(result);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void deleteByPostId(String postId, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
            try {
                commentDao.deleteByPostId(postId);
                if (callback != null) {
                    callback.onSuccess(null);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }
}
