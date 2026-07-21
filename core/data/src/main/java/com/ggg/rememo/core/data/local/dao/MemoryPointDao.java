package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;


import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import java.util.List;

@Dao
public interface MemoryPointDao {

    /** 插入记忆点 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemoryPoint point);
    /** 批量插入记忆点 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MemoryPoint> points);
    /** 删除全部记忆点 */
    @Query("DELETE FROM memory_points")
    void deleteAll();
    /**
     * 使用服务端完整快照原子替换本地记忆点。
     * 空列表或 null 表示服务端当前没有记忆点。
     */
    @Transaction
    default void replaceAll(List<MemoryPoint> points) {
        deleteAll();
        if (points != null && !points.isEmpty()) {
            insertAll(points);
        }
    }
    /** 根据 ID 删除记忆点 */
    @Query("DELETE FROM memory_points WHERE pointId = :pointId")
    void deleteById(String pointId);
    /** 根据 ID 获取记忆点 */
    @Query("SELECT * FROM memory_points WHERE pointId = :pointId")
    MemoryPoint getById(String pointId);
    /** 获取所有记忆点 */
    @Query("SELECT * FROM memory_points ORDER BY createdTime DESC")
    List<MemoryPoint> getAll();
    /** 获取最近 n 条记忆点 */
    @Query("SELECT * FROM memory_points ORDER BY createdTime DESC LIMIT :limit")
    List<MemoryPoint> getRecent(int limit);
}
