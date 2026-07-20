package com.ggg.rememo.feature.here.marker;

import com.ggg.rememo.core.data.model.entity.MemoryPoint;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MarkerDiffCalculatorTest {

    @Test
    public void firstRender_addsAllFiveHundredPoints() {
        MarkerDiff diff = MarkerDiffCalculator.calculate(
                Collections.emptyMap(), createPoints(500));

        assertEquals(500, diff.getAdded().size());
        assertEquals(0, diff.getChanged().size());
        assertEquals(0, diff.getRemovedIds().size());
        assertEquals(0, diff.getUnchangedCount());
    }

    @Test
    public void identicalRefresh_rebuildsNothing() {
        List<MemoryPoint> points = createPoints(500);
        Map<String, MemoryPointMarkerSnapshot> current =
                MarkerDiffCalculator.snapshotMapOf(points);

        MarkerDiff diff = MarkerDiffCalculator.calculate(current, copyPoints(points));

        assertEquals(0, diff.getAdded().size());
        assertEquals(0, diff.getChanged().size());
        assertEquals(0, diff.getRemovedIds().size());
        assertEquals(500, diff.getUnchangedCount());
    }

    @Test
    public void tenChangedPoints_onlyRebuildsTen() {
        List<MemoryPoint> oldPoints = createPoints(500);
        List<MemoryPoint> newPoints = copyPoints(oldPoints);
        for (int i = 0; i < 10; i++) {
            newPoints.get(i).setPointName("changed-" + i);
        }

        MarkerDiff diff = MarkerDiffCalculator.calculate(
                MarkerDiffCalculator.snapshotMapOf(oldPoints), newPoints);

        assertEquals(10, diff.getChanged().size());
        assertEquals(490, diff.getUnchangedCount());
    }

    @Test
    public void removedPoints_areReportedById() {
        List<MemoryPoint> oldPoints = createPoints(500);
        List<MemoryPoint> newPoints = copyPoints(oldPoints.subList(0, 490));

        MarkerDiff diff = MarkerDiffCalculator.calculate(
                MarkerDiffCalculator.snapshotMapOf(oldPoints), newPoints);

        assertEquals(10, diff.getRemovedIds().size());
        assertTrue(diff.getRemovedIds().contains("point-499"));
        assertEquals(490, diff.getUnchangedCount());
    }

    @Test
    public void addedPoints_areReported() {
        List<MemoryPoint> latest = createPoints(500);
        List<MemoryPoint> oldPoints = copyPoints(latest.subList(0, 490));

        MarkerDiff diff = MarkerDiffCalculator.calculate(
                MarkerDiffCalculator.snapshotMapOf(oldPoints), latest);

        assertEquals(10, diff.getAdded().size());
        assertEquals(490, diff.getUnchangedCount());
    }

    @Test
    public void eachVisualFieldChange_marksPointChanged() {
        MemoryPoint oldPoint = createPoint(1);
        Map<String, MemoryPointMarkerSnapshot> current =
                MarkerDiffCalculator.snapshotMapOf(Collections.singletonList(oldPoint));

        MemoryPoint latitudeChanged = copyPoint(oldPoint);
        latitudeChanged.setLatitude(oldPoint.getLatitude() + 0.001);
        assertEquals(1, MarkerDiffCalculator.calculate(
                current, Collections.singletonList(latitudeChanged)).getChanged().size());

        MemoryPoint longitudeChanged = copyPoint(oldPoint);
        longitudeChanged.setLongitude(oldPoint.getLongitude() + 0.001);
        assertEquals(1, MarkerDiffCalculator.calculate(
                current, Collections.singletonList(longitudeChanged)).getChanged().size());

        MemoryPoint nameChanged = copyPoint(oldPoint);
        nameChanged.setPointName("new-name");
        assertEquals(1, MarkerDiffCalculator.calculate(
                current, Collections.singletonList(nameChanged)).getChanged().size());

        MemoryPoint coverChanged = copyPoint(oldPoint);
        coverChanged.setCoverImageUrl("https://example.com/new.jpg");
        assertEquals(1, MarkerDiffCalculator.calculate(
                current, Collections.singletonList(coverChanged)).getChanged().size());
    }

    @Test
    public void nonVisualFieldChanges_doNotRebuildMarker() {
        MemoryPoint oldPoint = createPoint(1);
        Map<String, MemoryPointMarkerSnapshot> current =
                MarkerDiffCalculator.snapshotMapOf(Collections.singletonList(oldPoint));
        MemoryPoint latest = copyPoint(oldPoint);
        latest.setSummaryText("updated summary");
        latest.setMemoryCount(99);
        latest.setMinYear(1980);
        latest.setMaxYear(2026);

        MarkerDiff diff = MarkerDiffCalculator.calculate(
                current, Collections.singletonList(latest));

        assertEquals(0, diff.getChanged().size());
        assertEquals(1, diff.getUnchangedCount());
    }

    @Test
    public void emptyLatestList_removesAllCurrentPoints() {
        Map<String, MemoryPointMarkerSnapshot> current =
                MarkerDiffCalculator.snapshotMapOf(createPoints(20));

        MarkerDiff emptyDiff = MarkerDiffCalculator.calculate(current, Collections.emptyList());
        MarkerDiff nullDiff = MarkerDiffCalculator.calculate(current, null);

        assertEquals(20, emptyDiff.getRemovedIds().size());
        assertEquals(20, nullDiff.getRemovedIds().size());
    }

    @Test
    public void invalidPoints_areIgnored() {
        List<MemoryPoint> points = new ArrayList<>();
        points.add(null);
        MemoryPoint blank = createPoint(1);
        blank.setPointId("   ");
        points.add(blank);

        MarkerDiff diff = MarkerDiffCalculator.calculate(Collections.emptyMap(), points);

        assertEquals(0, diff.getAdded().size());
        assertEquals(0, diff.getUnchangedCount());
    }

    @Test
    public void duplicatePointId_lastValueWins() {
        MemoryPoint first = createPoint(1);
        first.setPointId("duplicate");
        first.setPointName("first");
        MemoryPoint second = copyPoint(first);
        second.setPointName("second");

        MarkerDiff diff = MarkerDiffCalculator.calculate(
                Collections.emptyMap(), java.util.Arrays.asList(first, second));

        assertEquals(1, diff.getAdded().size());
        assertEquals("second", diff.getAdded().get(0).getPointName());
    }

    private static List<MemoryPoint> createPoints(int count) {
        List<MemoryPoint> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(createPoint(i));
        }
        return result;
    }

    private static MemoryPoint createPoint(int index) {
        MemoryPoint point = new MemoryPoint();
        point.setPointId("point-" + index);
        point.setLatitude(34.0 + (index / 20) * 0.0001);
        point.setLongitude(108.0 + (index % 20) * 0.0001);
        point.setPointName("point-name-" + index);
        point.setCoverImageUrl("");
        point.setSummaryText("summary-" + index);
        point.setMemoryCount(index);
        return point;
    }

    private static List<MemoryPoint> copyPoints(List<MemoryPoint> points) {
        List<MemoryPoint> result = new ArrayList<>(points.size());
        for (MemoryPoint point : points) {
            result.add(copyPoint(point));
        }
        return result;
    }

    private static MemoryPoint copyPoint(MemoryPoint source) {
        MemoryPoint copy = new MemoryPoint();
        copy.setPointId(source.getPointId());
        copy.setLatitude(source.getLatitude());
        copy.setLongitude(source.getLongitude());
        copy.setPointName(source.getPointName());
        copy.setLocationAddress(source.getLocationAddress());
        copy.setCoverImageUrl(source.getCoverImageUrl());
        copy.setMemoryCount(source.getMemoryCount());
        copy.setSummaryText(source.getSummaryText());
        copy.setMinYear(source.getMinYear());
        copy.setMaxYear(source.getMaxYear());
        copy.setCreatedTime(source.getCreatedTime());
        return copy;
    }
}
