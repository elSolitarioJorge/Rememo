package com.ggg.rememo.shell.hub;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.shell.hub.databinding.ActivityHubShellBinding;

import java.util.Arrays;
import java.util.List;

public class HubShellActivity extends AppCompatActivity {
    private ActivityHubShellBinding binding;

    private final List<Item> items = Arrays.asList(
            new Item("在此（地图）", Routes.Here.HOME_FRAGMENT, ItemType.FRAGMENT),
            new Item("探索", Routes.Explore.HOME_FRAGMENT, ItemType.FRAGMENT),
            new Item("时序", Routes.Timeline.HOME, ItemType.ACTIVITY),
            new Item("发布", Routes.Publish.HOME, ItemType.ACTIVITY),
            new Item("我的", Routes.Profile.HOME_FRAGMENT, ItemType.FRAGMENT)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHubShellBinding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.rv.setLayoutManager(new LinearLayoutManager(this));
        binding.rv.setAdapter(new Adapter(items));
        setContentView(binding.rv);

        setTitle("拾忆 · Hub");
    }

    private enum ItemType {
        ACTIVITY,
        FRAGMENT
    }

    static class Item {
        final String title;
        final String path;
        final ItemType type;

        Item(String title, String path, ItemType type) {
            this.title = title;
            this.path = path;
            this.type = type;
        }
    }

    static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        final List<Item> data;

        Adapter(List<Item> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            int pad = (int) (16 * parent.getResources().getDisplayMetrics().density);
            tv.setPadding(pad, pad, pad, pad);
            tv.setTextSize(16);
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Item it = data.get(position);
            h.tv.setText(String.format("%s\n%s", it.title, it.path));
            h.tv.setOnClickListener(v -> {
                if (it.type == ItemType.FRAGMENT) {
                    Intent intent = new Intent(v.getContext(), DebugFragmentContainerActivity.class);
                    intent.putExtra(DebugFragmentContainerActivity.EXTRA_ROUTE, it.path);
                    v.getContext().startActivity(intent);
                } else {
                    ARouter.getInstance().build(it.path).navigation();
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            private final TextView tv;

            VH(@NonNull TextView itemView) {
                super(itemView);
                tv = itemView;
            }
        }
    }
}
