package com.ggg.rememo.core.data.repository;

import com.ggg.rememo.core.common.util.AppContext;
import com.ggg.rememo.core.data.local.dao.UserDao;
import com.ggg.rememo.core.data.local.database.RememoDatabase;
import com.ggg.rememo.core.data.model.entity.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executorService;

    public UserRepository() {
        RememoDatabase database = RememoDatabase.getInstance(AppContext.get());
        this.userDao = database.userDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void insert(User user, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.insert(user);
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

    public void insertAll(List<User> users, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.insertAll(users);
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

    public void update(User user, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.update(user);
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

    public void delete(User user, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.delete(user);
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

    public void deleteById(String userId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.deleteById(userId);
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

    public void getById(String userId, Callback<User> callback) {
        executorService.execute(() -> {
            try {
                User result = userDao.getById(userId);
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

    public void getAll(Callback<List<User>> callback) {
        executorService.execute(() -> {
            try {
                List<User> result = userDao.getAll();
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

    public void getRecent(int limit, Callback<List<User>> callback) {
        executorService.execute(() -> {
            try {
                List<User> result = userDao.getRecent(limit);
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

    public void searchByNickName(String keyword, Callback<List<User>> callback) {
        executorService.execute(() -> {
            try {
                List<User> result = userDao.searchByNickName(keyword);
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

    public void getCount(Callback<Integer> callback) {
        executorService.execute(() -> {
            try {
                int result = userDao.getCount();
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
