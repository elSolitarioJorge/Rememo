package com.ggg.rememo;

import android.os.Bundle;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.common.util.TokenManager;
import com.ggg.rememo.core.ui.auth.LoginRequiredPrompt;
import com.ggg.rememo.databinding.ActivityMainBinding;
import com.ggg.rememo.session.TokenExpiredHandler;

@Route(path = Routes.Main.HOME)
public class MainActivity extends AppCompatActivity {

    private static final String STATE_SELECTED_TAB_ID = "state_selected_tab_id";

    private static final TabSpec[] TAB_SPECS = new TabSpec[]{
            new TabSpec(R.id.menu_map, Routes.Here.HOME_FRAGMENT),
            new TabSpec(R.id.menu_discover, Routes.Explore.HOME_FRAGMENT),
            new TabSpec(R.id.menu_message, Routes.Message.HOME_FRAGMENT),
            new TabSpec(R.id.menu_me, Routes.Profile.HOME_FRAGMENT)
    };

    private ActivityMainBinding binding;
    private Fragment activeFragment;
    private int selectedTabId = R.id.menu_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (TokenManager.isLoggedIn()) {
            TokenExpiredHandler.markLoginRecovered();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        selectedTabId = resolveSelectedTabId(savedInstanceState);
        binding.bnvNavigation.setSelectedItemId(selectedTabId);
        selectTab(selectedTabId);
        setupBottomNavigation();
        setupFab();
    }

    private void setupBottomNavigation() {
        binding.bnvNavigation.setOnItemSelectedListener(item -> selectTab(item.getItemId()));
    }

    private void setupFab() {
        binding.fabNavigation.setOnClickListener(v ->
                LoginRequiredPrompt.requireLogin(this, "发布记忆", () ->
                        ARouter.getInstance()
                                .build(Routes.Publish.HOME)
                                .navigation(this))
        );
    }

    private boolean selectTab(@IdRes int menuId) {
        TabSpec targetSpec = findTabSpec(menuId);
        if (targetSpec == null) {
            return false;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        // 极快连续点击时，先落地上一次异步事务，确保可通过稳定 Tag 找回复用实例。
        if (activeFragment != null && !activeFragment.isAdded()) {
            fragmentManager.executePendingTransactions();
        }

        Fragment target = fragmentManager.findFragmentByTag(targetSpec.route);
        if (target == null) {
            Object destination = ARouter.getInstance()
                    .build(targetSpec.route)
                    .navigation();
            if (!(destination instanceof Fragment)) {
                throw new IllegalStateException(
                        "ARouter route did not return a Fragment: " + targetSpec.route);
            }
            target = (Fragment) destination;
        }

        if (target == activeFragment && target.isAdded() && !target.isHidden()) {
            selectedTabId = menuId;
            return true;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction()
                .setReorderingAllowed(true);

        for (TabSpec tabSpec : TAB_SPECS) {
            Fragment fragment = fragmentManager.findFragmentByTag(tabSpec.route);
            if (fragment == null || fragment == target) {
                continue;
            }
            transaction.hide(fragment)
                    .setMaxLifecycle(fragment, Lifecycle.State.STARTED);
        }

        if (!target.isAdded()) {
            transaction.add(R.id.fcv_navigation, target, targetSpec.route);
        }
        transaction.show(target)
                .setMaxLifecycle(target, Lifecycle.State.RESUMED)
                .commit();

        activeFragment = target;
        selectedTabId = menuId;
        return true;
    }

    private int resolveSelectedTabId(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return R.id.menu_map;
        }
        int restoredId = savedInstanceState.getInt(STATE_SELECTED_TAB_ID, R.id.menu_map);
        return findTabSpec(restoredId) == null ? R.id.menu_map : restoredId;
    }

    private static TabSpec findTabSpec(@IdRes int menuId) {
        for (TabSpec tabSpec : TAB_SPECS) {
            if (tabSpec.menuId == menuId) {
                return tabSpec;
            }
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(STATE_SELECTED_TAB_ID, selectedTabId);
        super.onSaveInstanceState(outState);
    }

    private static final class TabSpec {
        @IdRes
        private final int menuId;
        private final String route;

        private TabSpec(@IdRes int menuId, String route) {
            this.menuId = menuId;
            this.route = route;
        }
    }
}
