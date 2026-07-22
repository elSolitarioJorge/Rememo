package com.ggg.rememo.core.data.repository;

import com.ggg.rememo.core.common.util.AppContext;
import com.ggg.rememo.core.data.local.dao.UserDao;
import com.ggg.rememo.core.data.local.database.RememoDatabase;
import com.ggg.rememo.core.data.model.entity.User;

import java.util.List;
import java.util.concurrent.Executor;

public class UserRepository {

    private final UserDao userDao;
    private final Executor databaseExecutor;

    public UserRepository() {
        RememoDatabase database = RememoDatabase.getInstance(AppContext.get());
        this.userDao = database.userDao();
        this.databaseExecutor = DataTaskExecutor.get();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void insert(User user, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
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
        databaseExecutor.execute(() -> {
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

    public void deleteById(String userId, Callback<Void> callback) {
        databaseExecutor.execute(() -> {
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
        databaseExecutor.execute(() -> {
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

    public void searchByNickName(String keyword, Callback<List<User>> callback) {
        databaseExecutor.execute(() -> {
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
}
