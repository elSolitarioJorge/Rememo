package com.ggg.rememo.core.data.repository;


import com.ggg.rememo.core.common.util.AppContext;
import com.ggg.rememo.core.data.local.dao.MemoryPointDao;
import com.ggg.rememo.core.data.local.database.RememoDatabase;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemoryPointRepository {

    private final MemoryPointDao memoryPointDao;
    private final ExecutorService executorService;

    public MemoryPointRepository() {
        RememoDatabase database = RememoDatabase.getInstance(AppContext.get());
        this.memoryPointDao = database.memoryPointDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void insert(MemoryPoint point, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPointDao.insert(point);
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

    public void insertAll(List<MemoryPoint> points, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPointDao.insertAll(points);
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

    /**
     * 使用完整快照原子替换本地记忆点。
     * 空列表或 null 会清空现有缓存。
     */
    public void replaceAll(List<MemoryPoint> points, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPointDao.replaceAll(points);
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


    public void deleteById(String pointId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                memoryPointDao.deleteById(pointId);
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

    public void getById(String pointId, Callback<MemoryPoint> callback) {
        executorService.execute(() -> {
            try {
                MemoryPoint result = memoryPointDao.getById(pointId);
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

    public void getAll(Callback<List<MemoryPoint>> callback) {
        executorService.execute(() -> {
            try {
                List<MemoryPoint> result = memoryPointDao.getAll();
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

    public void getRecent(int limit, Callback<List<MemoryPoint>> callback) {
        executorService.execute(() -> {
            try {
                List<MemoryPoint> result = memoryPointDao.getRecent(limit);
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
}
