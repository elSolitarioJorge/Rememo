package com.ggg.rememo.feature.here;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.amap.api.maps.AMap;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.map.MapLifecycleHelper;
import com.ggg.rememo.feature.here.databinding.FragmentHereHomeBinding;

@Route(path = Routes.Here.HOME_FRAGMENT)
public class HereHomeFragment extends Fragment {

    private FragmentHereHomeBinding binding;
    private AMap aMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHereHomeBinding.inflate(inflater, container, false);
        MapLifecycleHelper.bindTo(this, binding.mapView, savedInstanceState);
        aMap = binding.mapView.getMap();
        return binding.getRoot();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (binding != null) {
            MapLifecycleHelper.onHiddenChanged(binding.mapView, hidden);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (binding != null) {
            MapLifecycleHelper.onSaveInstanceState(binding.mapView, outState);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
