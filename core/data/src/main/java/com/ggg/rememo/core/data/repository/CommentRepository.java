package com.ggg.rememo.core.data.repository;

import android.content.Context;


import com.ggg.rememo.core.data.local.dao.CommentDao;
import com.ggg.rememo.core.data.local.database.RememoDatabase;
import com.ggg.rememo.core.data.model.entity.Comment;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommentRepository {

    private final CommentDao commentDao;
    private final ExecutorService executorService;

    public CommentRepository(Context context) {
        RememoDatabase database = RememoDatabase.getInstance(context);
        this.commentDao = database.commentDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void insert(Comment comment, Callback<Void> callback) {
        executorService.execute(() -> {
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
        executorService.execute(() -> {
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

    public void update(Comment comment, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                commentDao.update(comment);
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

    public void delete(Comment comment, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                commentDao.delete(comment);
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
        executorService.execute(() -> {
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

    public void getById(String commentId, Callback<Comment> callback) {
        executorService.execute(() -> {
            try {
                Comment result = commentDao.getById(commentId);
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

    public void getAll(Callback<List<Comment>> callback) {
        executorService.execute(() -> {
            try {
                List<Comment> result = commentDao.getAll();
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

    public void getByPostId(String postId, Callback<List<Comment>> callback) {
        executorService.execute(() -> {
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
        executorService.execute(() -> {
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
        executorService.execute(() -> {
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
        executorService.execute(() -> {
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
