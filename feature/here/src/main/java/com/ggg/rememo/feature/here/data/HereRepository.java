package com.ggg.rememo.feature.here.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.ggg.rememo.core.data.mapper.MemoryPointMapper;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.core.data.model.network.response.MemoryPointResponse;
import com.ggg.rememo.core.data.repository.MemoryPointRepository;
import com.ggg.rememo.core.network.ApiCallback;
import com.ggg.rememo.core.network.ApiResponse;
import com.ggg.rememo.core.network.ApiService;
import com.ggg.rememo.core.network.NetworkClient;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HereRepository {

    private static final String TAG = "HereRepository";

    private static final String MMKV_ID = "here_location";
    private static final String PREF_LAST_LAT = "last_lat";
    private static final String PREF_LAST_LNG = "last_lng";

    private final MMKV mmkv;
    private final MemoryPointRepository memoryPointRepository;
    private final ApiService apiService;

    public HereRepository() {
        this.mmkv = MMKV.mmkvWithID(MMKV_ID);
        this.memoryPointRepository = new MemoryPointRepository();
        this.apiService = NetworkClient.getInstance().getApiService();
        Log.d(TAG, "HereRepository initialized, BASE_URL=" + NetworkClient.getBaseUrl());
    }

    /**
     * 从网络获取所有记忆点并保存到本地
     */
    public void fetchAllMemoryPoints(ApiCallback<List<MemoryPoint>> callback) {
        Log.d(TAG, "[fetchAllMemoryPoints] 请求网络: GET /api/memory-points");
        apiService.getAllMemoryPoints().enqueue(new Callback<ApiResponse<List<MemoryPointResponse>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<MemoryPointResponse>>> call,
                                   @NonNull Response<ApiResponse<List<MemoryPointResponse>>> response) {
                Log.d(TAG, "[fetchAllMemoryPoints] HTTP response, code=" + response.code()
                        + ", isSuccessful=" + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<MemoryPointResponse>> body = response.body();
                    Log.d(TAG, "[fetchAllMemoryPoints] body.code=" + body.getCode()
                            + ", body.message=" + body.getMessage()
                            + ", isSuccess=" + body.isSuccess()
                            + ", data=" + (body.getData() == null ? "null" : body.getData().size() + " items"));

                    if (body.isSuccess()) {
                        List<MemoryPointResponse> responseList = body.getData();
                        if (responseList != null && !responseList.isEmpty()) {
                            Log.d(TAG, "[fetchAllMemoryPoints] 网络返回 " + responseList.size() + " 条记忆点，开始保存到本地 DB");
                            List<MemoryPoint> points = MemoryPointMapper.toEntityList(responseList);
                            memoryPointRepository.insertAll(points, new MemoryPointRepository.Callback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    Log.d(TAG, "[fetchAllMemoryPoints] DB 保存成功，通知上层");
                                    callback.onSuccess(points);
                                }

                                @Override
                                public void onError(Exception e) {
                                    Log.w(TAG, "[fetchAllMemoryPoints] DB 保存失败但仍返回数据: " + e.getMessage());
                                    callback.onSuccess(points);
                                }
                            });
                        } else {
                            Log.d(TAG, "[fetchAllMemoryPoints] 网络返回空列表 data=[]");
                            callback.onSuccess(new ArrayList<>());
                        }
                    } else {
                        Log.e(TAG, "[fetchAllMemoryPoints] 业务层失败 code=" + body.getCode()
                                + ", message=" + body.getMessage());
                        callback.onError("获取记忆点失败: " + body.getMessage());
                    }
                } else {
                    String errorBody = response.errorBody() != null ? response.errorBody().toString() : "null";
                    Log.e(TAG, "[fetchAllMemoryPoints] HTTP 请求失败 code=" + response.code()
                            + ", errorBody=" + errorBody);
                    callback.onError("HTTP 错误: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<MemoryPointResponse>>> call,
                                  @NonNull Throwable t) {
                Log.e(TAG, "[fetchAllMemoryPoints] 网络请求失败 onFailure: " + t.getClass().getName()
                        + ", message=" + t.getMessage());
                t.printStackTrace();
                callback.onError("网络异常: " + t.getMessage());
            }
        });
    }

    /**
     * 加载本地缓存的所有记忆点
     */
    public void loadLocalMemoryPoints(MemoryPointRepository.Callback<List<MemoryPoint>> callback) {
        memoryPointRepository.getAll(callback);
    }

    /**
     * 缓存优先加载：先返回本地缓存，同时后台拉取网络最新数据并更新本地缓存
     * 适用于 Here 首页，用户进入页面时希望立即看到记忆点，不等待网络
     *
     * @param cacheCallback  缓存数据回调（同步返回，无等待）
     * @param networkCallback 网络最新数据回调（异步返回）
     */
    public void loadWithCacheFirst(
            MemoryPointRepository.Callback<List<MemoryPoint>> cacheCallback,
            MemoryPointRepository.Callback<List<MemoryPoint>> networkCallback) {
        // 1. 立即返回本地缓存
        memoryPointRepository.getAll(new MemoryPointRepository.Callback<List<MemoryPoint>>() {
            @Override
            public void onSuccess(List<MemoryPoint> cachedData) {
                Log.d(TAG, "[loadWithCacheFirst] 本地缓存返回 " + (cachedData == null ? 0 : cachedData.size()) + " 条");
                if (cacheCallback != null) {
                    cacheCallback.onSuccess(cachedData);
                }
                // 2. 后台同步网络最新数据
                fetchAndUpdateCache(new MemoryPointRepository.Callback<List<MemoryPoint>>() {
                    @Override
                    public void onSuccess(List<MemoryPoint> networkData) {
                        Log.d(TAG, "[loadWithCacheFirst] 网络数据同步完成，通知上层刷新");
                        if (networkCallback != null) {
                            networkCallback.onSuccess(networkData);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        // 网络失败时静默处理，缓存已展示过了
                        Log.w(TAG, "[loadWithCacheFirst] 网络同步失败: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.w(TAG, "[loadWithCacheFirst] 读取本地缓存失败: " + e.getMessage());
                if (cacheCallback != null) {
                    cacheCallback.onError(e);
                }
            }
        });
    }

    /**
     * 从网络拉取最新数据并更新本地缓存（内部使用）
     */
    private void fetchAndUpdateCache(MemoryPointRepository.Callback<List<MemoryPoint>> callback) {
        fetchAllMemoryPoints(new ApiCallback<List<MemoryPoint>>() {
            @Override
            public void onSuccess(List<MemoryPoint> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                callback.onError(new Exception(message));
            }
        });
    }

    /**
     * 加载所有记忆点（直接从网络获取，忽略本地缓存）
     */
    public void loadAllMemoryPoints(MemoryPointRepository.Callback<List<MemoryPoint>> callback) {
        Log.d(TAG, "[loadAllMemoryPoints] 直接从网络获取");
        fetchAllMemoryPoints(new ApiCallback<List<MemoryPoint>>() {
            @Override
            public void onSuccess(List<MemoryPoint> data) {
                Log.d(TAG, "[loadAllMemoryPoints] 网络返回 "
                        + (data == null ? 0 : data.size()) + " 条数据");
                callback.onSuccess(data);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "[loadAllMemoryPoints] 网络请求失败: " + message);
                callback.onError(new Exception(message));
            }
        });
    }

    /**
     * 获取缓存的上次定位坐标
     * @return double[] {lat, lng}，如果无缓存则返回 null
     */
    public double[] getCachedLocation() {
        if (!mmkv.containsKey(PREF_LAST_LAT) || !mmkv.containsKey(PREF_LAST_LNG)) {
            Log.d(TAG, "[getCachedLocation] 无缓存坐标");
            return null;
        }
        double lat = mmkv.decodeDouble(PREF_LAST_LAT, 0);
        double lng = mmkv.decodeDouble(PREF_LAST_LNG, 0);
        Log.d(TAG, "[getCachedLocation] 有缓存 lat=" + lat + ", lng=" + lng);
        return new double[]{lat, lng};
    }

    /**
     * 保存上次定位坐标到缓存
     */
    public void saveCachedLocation(double lat, double lng) {
        Log.v(TAG, "[saveCachedLocation] 保存坐标 lat=" + lat + ", lng=" + lng);
        mmkv.encode(PREF_LAST_LAT, lat);
        mmkv.encode(PREF_LAST_LNG, lng);
    }
}
