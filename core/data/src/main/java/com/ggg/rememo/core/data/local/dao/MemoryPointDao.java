package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;


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
