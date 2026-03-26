package com.ggg.rememo.feature.here;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.feature.here.databinding.ActivityHereHomeBinding;

@Route(path = Routes.Here.HOME)
public class HereHomeActivity extends AppCompatActivity {

    private ActivityHereHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHereHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 加载 HereHomeFragment
        if (savedInstanceState == null) {
            loadFragment();
        }
    }

    private void loadFragment() {
        Fragment fragment = new HereHomeFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(binding.fragmentContainer.getId(), fragment);
        ft.commit();
    }
}