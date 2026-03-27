package com.ggg.rememo.feature.message;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.ggg.rememo.core.common.router.Routes;
import com.ggg.rememo.core.ui.Utils.UIUtils;
import com.ggg.rememo.feature.message.Adapter.MessageAdapter;
import com.ggg.rememo.feature.message.DataModule.ItemCommentMsg;
import com.ggg.rememo.feature.message.DataModule.ItemLikeMsg;
import com.ggg.rememo.feature.message.DataModule.ItemSystemMsg;
import com.ggg.rememo.feature.message.DataModule.ItemTimeHeadMsg;
import com.ggg.rememo.feature.message.DataModule.ListItem;
import com.ggg.rememo.feature.message.Utils.VerticalItemDecoration;
import com.ggg.rememo.feature.message.databinding.FragmentMessageHomeDarkBinding;

import java.util.ArrayList;
import java.util.List;

@Route(path = Routes.Message.HOME_FRAGMENT)
public class MessageHomeFragment extends Fragment {
    FragmentMessageHomeDarkBinding binding;
    MessageAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMessageHomeDarkBinding.inflate(inflater, container, false);
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, 0);
            return insets;
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 初始化RecyclerView
        binding.messageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.messageRecyclerView.addItemDecoration(new VerticalItemDecoration(UIUtils.dpToPx(getContext(), 16)));
        binding.messageRecyclerView.setNestedScrollingEnabled(false);

        // 准备数据
        List<ListItem> items = new ArrayList<>();
        items.add(new ItemTimeHeadMsg("今天"));
        items.add(new ItemSystemMsg(R.drawable.ic_robot, "时光修理铺", "10分钟前", "您提交的老照片#892已经通过云端算法深度修复上色完毕，快去看看吧！"));
        items.add(new ItemCommentMsg(com.ggg.rememo.core.ui.R.drawable.pic_my_avatar, "老王头", "那会儿的橘子是不是特别甜？哈哈哈。这AI上色技术真牛，衣服的颜色都还原对了", "2小时前", com.ggg.rememo.core.ui.R.drawable.pic_avatar2, "绿皮车上的离别绿皮车上的离别绿皮车上的离别", "父亲在这里送我，隔着车窗递给我橘子"));
        items.add(new ItemTimeHeadMsg("昨天"));
        items.add(new ItemLikeMsg(com.ggg.rememo.core.ui.R.drawable.pic_my_avatar, "建国叔", 12, "昨天16:30", "我给的橘子被你给收藏了，快去看看吧！", "消失的糖葫芦", "小时候钟楼旁边的那个糖葫芦摊，是我"));

        items.add(new ItemTimeHeadMsg("今天"));
        items.add(new ItemSystemMsg(R.drawable.ic_robot, "时光修理铺(AI)", "10分钟前", "您提交的老照片#892已经通过云端算法深度修复上色完毕，快去看看吧！"));
        items.add(new ItemCommentMsg(com.ggg.rememo.core.ui.R.drawable.pic_my_avatar, "老王头", "那会儿的橘子是不是特别甜？哈哈哈。这AI上色技术真牛，衣服的颜色都还原对了", "2小时前", com.ggg.rememo.core.ui.R.drawable.pic_avatar2, "绿皮车上的离别绿皮车上的离别绿皮车上的离别", "父亲在这里送我，隔着车窗递给我橘子"));
        items.add(new ItemTimeHeadMsg("昨天"));
        items.add(new ItemLikeMsg(com.ggg.rememo.core.ui.R.drawable.pic_my_avatar, "建国叔", 12, "昨天16:30", "我给的橘子被你给收藏了，快去看看吧！", "消失的糖葫芦", "小时候钟楼旁边的那个糖葫芦摊，是我"));

        // 设置Adapter
        adapter = new MessageAdapter(items);
        binding.messageRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
