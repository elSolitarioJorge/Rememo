package com.ggg.rememo.core.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import java.util.List;

@Dao
public interface MemoryPointDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MemoryPoint point);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MemoryPoint> points);

    @Update
    void update(MemoryPoint point);

    @Delete
    void delete(MemoryPoint point);

    @Query("DELETE FROM memory_points WHERE pointId = :pointId")
    void deleteById(String pointId);

    @Query("SELECT * FROM memory_points WHERE pointId = :pointId")
    MemoryPoint getById(String pointId);

    @Query("SELECT * FROM memory_points ORDER BY createdTime DESC")
    List<MemoryPoint> getAll();

    @Query("SELECT * FROM memory_points ORDER BY createdTime DESC LIMIT :limit")
    List<MemoryPoint> getRecent(int limit);

    @Query("SELECT * FROM memory_points WHERE pointName LIKE '%' || :keyword || '%' OR locationAddress LIKE '%' || :keyword || '%'")
    List<MemoryPoint> search(String keyword);

    @Query("SELECT * FROM memory_points WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng")
    List<MemoryPoint> getPointsInRange(double minLat, double maxLat, double minLng, double maxLng);

    @Query("SELECT COUNT(*) FROM memory_points")
    int getCount();

    @Query("UPDATE memory_points SET memoryCount = :count WHERE pointId = :pointId")
    void updateMemoryCount(String pointId, int count);

    @Query("UPDATE memory_points SET coverImageUrl = :coverUrl WHERE pointId = :pointId")
    void updateCoverImage(String pointId, String coverUrl);
}
