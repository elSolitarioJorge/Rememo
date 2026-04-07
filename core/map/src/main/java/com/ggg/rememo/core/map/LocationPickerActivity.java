package com.ggg.rememo.core.map;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.map.databinding.ActivityLocationPickerBinding;

/**
 * 地图选点页面
 */
@Route(path = Routes.Map.LOCATION_PICKER)
public class LocationPickerActivity extends AppCompatActivity implements AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener {

    private static final String TAG = "LocationPickerActivity";
    private ActivityLocationPickerBinding binding;
    private AMap aMap;
    private GeocodeSearch geocodeSearch;
    private double selectedLat = 0.0;
    private double selectedLng = 0.0;
    private String selectedAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationPickerBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            binding.titleContainer.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });

        // 初始化地图
        binding.mapView.onCreate(savedInstanceState);
        initMap();
        // 设置监听器
        setupListeners();

        // 开启跑马灯
        binding.tvSelectedAddress.setSelected(true);
    }

    private void initMap() {
        aMap = binding.mapView.getMap();
        aMap.setMapType(AMap.MAP_TYPE_NIGHT);

        // 隐藏默认UI控件
        aMap.getUiSettings().setScaleControlsEnabled(false);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setRotateGesturesEnabled(false);

        // 获取上个页面传来的默认坐标，将镜头移动过去
        double initLat = getIntent().getDoubleExtra("lat", 34.2655); // 默认西安钟楼
        double initLng = getIntent().getDoubleExtra("lng", 108.9436);
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(initLat, initLng), 15f));

        // 监听地图镜头的移动
        aMap.setOnCameraChangeListener(this);

        // 初始化逆地理编码查询工具
        try {
            geocodeSearch = new GeocodeSearch(this);
            geocodeSearch.setOnGeocodeSearchListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener(v -> finish());

        // 确认按钮
        binding.btnConfirmLocation.setOnClickListener(v -> {
            // 把数据装进 Intent，回传给 PublishActivity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("address", selectedAddress);
            resultIntent.putExtra("lat", selectedLat);
            resultIntent.putExtra("lng", selectedLng);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    // ================= 地图镜头移动监听 =================
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // 地图正在被用户拖动时，提示
        binding.tvSelectedAddress.setText("寻找坐标中...");
    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        // 用户松手，地图停止滑动，获取正中央的经纬度
        selectedLat = cameraPosition.target.latitude;
        selectedLng = cameraPosition.target.longitude;

        // 发起逆地理编码请求 (把经纬度翻译成中文地名)
        LatLonPoint latLonPoint = new LatLonPoint(selectedLat, selectedLng);
        RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query); // 异步查询
    }

    // ================= 逆地理编码回调 =================
    @Override
    public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
        if (rCode == 1000 && result != null && result.getRegeocodeAddress() != null) {
            String poiName = result.getRegeocodeAddress().getFormatAddress();
            if (!result.getRegeocodeAddress().getPois().isEmpty()) {
                poiName = result.getRegeocodeAddress().getPois().get(0).getTitle();
            }

            selectedAddress = poiName;
            binding.tvSelectedAddress.setText(selectedAddress);
        }
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        // 这是地址转经纬度，这里用不到
    }



    // ================= 地图生命周期管理 =================

    @Override
    protected void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
        binding = null;
    }
}
