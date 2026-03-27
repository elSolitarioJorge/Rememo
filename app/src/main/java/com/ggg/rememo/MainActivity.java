package com.ggg.rememo;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Fragment hereFragment;
    private Fragment exploreFragment;
    private Fragment messageFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        if (!TokenManager.isLoggedIn()) {
//            ARouter.getInstance()
//                    .build(Routes.Auth.LOGIN)
//                    .navigation();
//            finish();
//            return;
//        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        if (savedInstanceState == null) {
            initFragments();
        } else {
            restoreFragments();
        }

        setupBottomNavigation();
        setupFab();
    }

    private void initFragments() {
        hereFragment = (Fragment) ARouter.getInstance()
                .build(Routes.Here.HOME_FRAGMENT).navigation();
        exploreFragment = (Fragment) ARouter.getInstance()
                .build(Routes.Explore.HOME_FRAGMENT).navigation();
        messageFragment = (Fragment) ARouter.getInstance()
                .build(Routes.Message.HOME_FRAGMENT).navigation();
        profileFragment = (Fragment) ARouter.getInstance()
                .build(Routes.Profile.HOME_FRAGMENT).navigation();


        activeFragment = hereFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fcv_navigation, profileFragment, Routes.Profile.HOME_FRAGMENT)
                .hide(profileFragment)
                .add(R.id.fcv_navigation, messageFragment, Routes.Message.HOME_FRAGMENT)
                .hide(messageFragment)
                .add(R.id.fcv_navigation, exploreFragment, Routes.Explore.HOME_FRAGMENT)
                .hide(exploreFragment)
                .add(R.id.fcv_navigation, hereFragment, Routes.Here.HOME_FRAGMENT)
                .commit();
    }

    private void restoreFragments() {
        FragmentManager fm = getSupportFragmentManager();
        hereFragment = fm.findFragmentByTag(Routes.Here.HOME_FRAGMENT);
        exploreFragment = fm.findFragmentByTag(Routes.Explore.HOME_FRAGMENT);
        messageFragment = fm.findFragmentByTag(Routes.Message.HOME_FRAGMENT);
        profileFragment = fm.findFragmentByTag(Routes.Profile.HOME_FRAGMENT);

        int selectedId = binding.bnvNavigation.getSelectedItemId();
        if (selectedId == R.id.menu_discover) {
            activeFragment = exploreFragment;
        } else if (selectedId == R.id.menu_message) {
            activeFragment = messageFragment;
        } else if (selectedId == R.id.menu_me) {
            activeFragment = profileFragment;
        } else {
            activeFragment = hereFragment;
        }
    }

    private void setupBottomNavigation() {
        binding.bnvNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_map) {
                switchFragment(hereFragment);
                return true;
            } else if (id == R.id.menu_discover) {
                switchFragment(exploreFragment);
                return true;
            } else if (id == R.id.menu_message) {
                switchFragment(messageFragment);
                return true;
            } else if (id == R.id.menu_me) {
                switchFragment(profileFragment);
                return true;
            }
            return false;
        });
    }

    private void setupFab() {
        binding.fabNavigation.setOnClickListener(v ->
                ARouter.getInstance()
                        .build(Routes.Publish.HOME)
                        .navigation(this)
        );
    }

    private void switchFragment(Fragment target) {
        if (target == null || target == activeFragment) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(activeFragment).show(target).commit();
        activeFragment = target;
    }
}
