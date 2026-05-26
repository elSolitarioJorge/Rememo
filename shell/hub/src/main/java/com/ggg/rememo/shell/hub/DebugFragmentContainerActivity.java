package com.ggg.rememo.shell.hub;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.shell.hub.databinding.ActivityDebugFragmentContainerBinding;

public class DebugFragmentContainerActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTE = "extra_route";

    private static final String TAG = "DebugFragmentContainer";

    private ActivityDebugFragmentContainerBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebugFragmentContainerBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        String route = getIntent().getStringExtra(EXTRA_ROUTE);
        if (TextUtils.isEmpty(route)) {
            showRouteError("Fragment route is empty");
            return;
        }

        Object target;
        try {
            target = ARouter.getInstance().build(route).navigation();
        } catch (RuntimeException e) {
            showRouteError("Failed to open Fragment route: " + route, e);
            return;
        }

        if (!(target instanceof Fragment)) {
            showRouteError("Route is not a Fragment: " + route);
            return;
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, (Fragment) target)
                    .commit();
        }
    }

    private void showRouteError(String message) {
        Log.w(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showRouteError(String message, Throwable throwable) {
        Log.w(TAG, message, throwable);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }
}
