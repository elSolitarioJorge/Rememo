package com.ggg.rememo.feature.here.data;

import android.content.Context;

import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.tencent.mmkv.MMKV;

import java.util.List;

public class HereRepository {

    private static final String MMKV_ID = "here_location";
    private static final String PREF_LAST_LAT = "last_lat";
    private static final String PREF_LAST_LNG = "last_lng";

    private final MMKV mmkv;
    private final MemoryPointRepository memoryPointRepository;

    public HereRepository(Context context) {
        this.mmkv = MMKV.mmkvWithID(MMKV_ID);
        this.memoryPointRepository = new MemoryPointRepository(context);
    }

    /**
     * 加载所有记忆点
     */
    public void loadAllMemoryPoints(MemoryPointRepository.Callback<List<MemoryPoint>> callback) {
        memoryPointRepository.getAll(callback);
    }

    /**
     * 获取缓存的上次定位坐标
     * @return double[] {lat, lng}，如果无缓存则返回 null
     */
    public double[] getCachedLocation() {
        if (!mmkv.containsKey(PREF_LAST_LAT) || !mmkv.containsKey(PREF_LAST_LNG)) {
            return null;
        }
        double lat = mmkv.decodeDouble(PREF_LAST_LAT, 0);
        double lng = mmkv.decodeDouble(PREF_LAST_LNG, 0);
        return new double[]{lat, lng};
    }

    /**
     * 保存上次定位坐标到缓存
     */
    public void saveCachedLocation(double lat, double lng) {
        mmkv.encode(PREF_LAST_LAT, lat);
        mmkv.encode(PREF_LAST_LNG, lng);
    }
}
