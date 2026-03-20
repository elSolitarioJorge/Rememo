package com.ggg.rememo.core.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.ggg.rememo.core.data.local.dao.CommentDao;
import com.ggg.rememo.core.data.local.dao.MemoryPointDao;
import com.ggg.rememo.core.data.local.dao.MemoryPostDao;
import com.ggg.rememo.core.data.local.dao.UserDao;
import com.ggg.rememo.core.data.local.type.Converters;
import com.ggg.rememo.core.data.model.entity.Comment;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.entity.MemoryPost;
import com.ggg.rememo.core.data.model.entity.User;


@Database(
    entities = {
        MemoryPoint.class,
        MemoryPost.class,
        User.class,
        Comment.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters.class)
public abstract class RememoDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "rememo_db";
    private static volatile RememoDatabase INSTANCE;

    public abstract MemoryPointDao memoryPointDao();
    public abstract MemoryPostDao memoryPostDao();
    public abstract UserDao userDao();
    public abstract CommentDao commentDao();

    public static RememoDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (RememoDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            RememoDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration(true)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
