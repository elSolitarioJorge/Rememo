package com.ggg.rememo.core.data.repository;


import com.ggg.rememo.core.common.util.AppContext;
import com.ggg.rememo.core.data.local.dao.MemoryPostDao;
import com.ggg.rememo.core.data.local.database.RememoDatabase;
import com.ggg.rememo.core.data.model.entity.MemoryPost;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemoryPostRepository {

    private final MemoryPostDao memoryPostDao;
    private final ExecutorService executorService;

    public MemoryPostRepository() {
        RememoDatabase database = RememoDatabase.getInstance(AppContext.get());
        this.memoryPostDao = database.memoryPostDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void insert(MemoryPost post, Callback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                memoryPostDao.insert(post);
                if (callback != null) {
                    callback.onSuccess(true);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void insertAll(List<MemoryPost> posts, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPostDao.insertAll(posts);
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

    public void deleteById(String postId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPostDao.deleteById(postId);
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

    public void getById(String postId, Callback<MemoryPost> callback) {
        executorService.execute(() -> {
            try {
                MemoryPost result = memoryPostDao.getById(postId);
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

    public void getAll(Callback<List<MemoryPost>> callback) {
        executorService.execute(() -> {
            try {
                List<MemoryPost> result = memoryPostDao.getAll();
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

    public void getRecent(int limit, Callback<List<MemoryPost>> callback) {
        executorService.execute(() -> {
            try {
                List<MemoryPost> result = memoryPostDao.getRecent(limit);
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

    public void getByPointId(String pointId, Callback<List<MemoryPost>> callback) {
        executorService.execute(() -> {
            try {
                List<MemoryPost> result = memoryPostDao.getByPointId(pointId);
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

    public void getByAuthorId(String authorId, Callback<List<MemoryPost>> callback) {
        executorService.execute(() -> {
            try {
                List<MemoryPost> result = memoryPostDao.getByAuthorId(authorId);
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

    public void replaceByAuthorId(String authorId, List<MemoryPost> posts, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPostDao.replaceByAuthorId(authorId, posts);
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

    public void updateLikeCount(String postId, int count, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPostDao.updateLikeCount(postId, count);
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

    public void updateCommentCount(String postId, int count, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPostDao.updateCommentCount(postId, count);
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

    public void updateCollectCount(String postId, int count, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPostDao.updateCollectCount(postId, count);
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
