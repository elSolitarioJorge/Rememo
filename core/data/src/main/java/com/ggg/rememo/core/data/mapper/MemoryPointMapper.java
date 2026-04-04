package com.ggg.rememo.core.data.mapper;

import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.network.response.MemoryPointResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * MemoryPointResponse ↔ MemoryPoint 转换器。
 * 统一网络响应到本地实体的映射逻辑。
 */
public class MemoryPointMapper {

    private MemoryPointMapper() {}

    public static List<MemoryPoint> toEntityList(List<MemoryPointResponse> responses) {
        if (responses == null) return new ArrayList<>();
        List<MemoryPoint> points = new ArrayList<>(responses.size());
        for (MemoryPointResponse r : responses) {
            points.add(toEntity(r));
        }
        return points;
    }

    public static MemoryPoint toEntity(MemoryPointResponse response) {
        if (response == null) return null;
        MemoryPoint point = new MemoryPoint();
        point.setPointId(response.getPointId());
        point.setLatitude(response.getLatitude());
        point.setLongitude(response.getLongitude());
        point.setPointName(response.getPointName());
        point.setLocationAddress(response.getLocationAddress());
        point.setCoverImageUrl(response.getCoverImageUrl());
        point.setMemoryCount(response.getMemoryCount());
        point.setSummaryText(response.getSummaryText());
        point.setMinYear(response.getMinYear() != null ? response.getMinYear() : 0);
        point.setMaxYear(response.getMaxYear() != null ? response.getMaxYear() : 0);
        point.setCreatedTime(response.getCreatedTime());
        return point;
    }
}
