package com.ggg.rememo.core.data.repository;

import android.content.Context;

/**
 * Repository 管理器
 * 统一管理所有 Repository 实例，提供获取 Repository 的入口
 */
public class RepositoryManager {

    private static volatile RepositoryManager INSTANCE;

    private final Context context;

    private volatile MemoryPostRepository memoryPostRepository;
    private volatile MemoryPointRepository memoryPointRepository;
    private volatile UserRepository userRepository;
    private volatile CommentRepository commentRepository;

    private RepositoryManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static RepositoryManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RepositoryManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RepositoryManager(context);
                }
            }
        }
        return INSTANCE;
    }

    public MemoryPostRepository getMemoryPostRepository() {
        if (memoryPostRepository == null) {
            synchronized (this) {
                if (memoryPostRepository == null) {
                    memoryPostRepository = new MemoryPostRepository(context);
                }
            }
        }
        return memoryPostRepository;
    }

    public MemoryPointRepository getMemoryPointRepository() {
        if (memoryPointRepository == null) {
            synchronized (this) {
                if (memoryPointRepository == null) {
                    memoryPointRepository = new MemoryPointRepository(context);
                }
            }
        }
        return memoryPointRepository;
    }

    public UserRepository getUserRepository() {
        if (userRepository == null) {
            synchronized (this) {
                if (userRepository == null) {
                    userRepository = new UserRepository(context);
                }
            }
        }
        return userRepository;
    }

    public CommentRepository getCommentRepository() {
        if (commentRepository == null) {
            synchronized (this) {
                if (commentRepository == null) {
                    commentRepository = new CommentRepository(context);
                }
            }
        }
        return commentRepository;
    }
}
