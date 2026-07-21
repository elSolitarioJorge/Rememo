package com.ggg.rememo.core.data.local.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import android.content.Context;

import androidx.room.Room;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ggg.rememo.core.data.local.database.RememoDatabase;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class MemoryPointDaoSnapshotTest {

    private RememoDatabase database;
    private MemoryPointDao dao;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        database = Room.inMemoryDatabaseBuilder(context, RememoDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = database.memoryPointDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    @Test
    public void replaceAll_removesPointsMissingFromLatestSnapshot() {
        dao.insertAll(Arrays.asList(point("A", "old A"), point("B", "old B")));

        dao.replaceAll(Collections.singletonList(point("A", "latest A")));

        List<MemoryPoint> result = dao.getAll();
        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getPointId());
        assertEquals("latest A", result.get(0).getPointName());
    }

    @Test
    public void replaceAll_updatesExistingAndAddsNewPoints() {
        dao.insertAll(Arrays.asList(point("A", "old A"), point("B", "old B")));

        dao.replaceAll(Arrays.asList(point("A", "updated A"), point("C", "new C")));

        List<MemoryPoint> result = dao.getAll();
        assertEquals(2, result.size());
        assertEquals("updated A", findById(result, "A").getPointName());
        assertEquals("new C", findById(result, "C").getPointName());
    }

    @Test
    public void replaceAll_emptySnapshotClearsDatabase() {
        dao.insertAll(Arrays.asList(point("A", "A"), point("B", "B")));

        dao.replaceAll(Collections.emptyList());

        assertEquals(0, dao.getAll().size());
    }

    @Test
    public void replaceAll_nullSnapshotClearsDatabase() {
        dao.insertAll(Arrays.asList(point("A", "A"), point("B", "B")));

        dao.replaceAll(null);

        assertEquals(0, dao.getAll().size());
    }

    @Test
    public void replaceAll_invalidSnapshotRollsBackDeletionAndInsertions() {
        dao.insertAll(Arrays.asList(point("A", "A"), point("B", "B")));
        MemoryPoint invalidPoint = point("invalid", "invalid");
        invalidPoint.setPointId(null);

        try {
            dao.replaceAll(Arrays.asList(point("C", "C"), invalidPoint));
            fail("Expected invalid snapshot insertion to fail");
        } catch (RuntimeException expected) {
            // The transaction must restore the original rows.
        }

        List<MemoryPoint> result = dao.getAll();
        assertEquals(2, result.size());
        assertNotNull(findById(result, "A"));
        assertNotNull(findById(result, "B"));
    }

    private static MemoryPoint point(String pointId, String pointName) {
        MemoryPoint point = new MemoryPoint();
        point.setPointId(pointId);
        point.setPointName(pointName);
        point.setCreatedTime(pointId.charAt(0));
        return point;
    }

    private static MemoryPoint findById(List<MemoryPoint> points, String pointId) {
        for (MemoryPoint point : points) {
            if (pointId.equals(point.getPointId())) {
                return point;
            }
        }
        fail("Missing point " + pointId);
        return null;
    }
}
