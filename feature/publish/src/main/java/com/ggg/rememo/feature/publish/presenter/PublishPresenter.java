package com.ggg.rememo.feature.publish.presenter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPhoto;
import com.ggg.rememo.core.data.repository.MemoryPostRepository;
import com.ggg.rememo.feature.publish.contract.PublishContract;
import com.ggg.rememo.feature.publish.data.PublishRepository;

import java.util.List;

/**
 * 发布模块 Presenter
 * 职责：参数校验、数据组装、调用 Repository
 */
public class PublishPresenter extends BasePresenter<PublishContract.View>
        implements PublishContract.Presenter {

    private static final int DEFAULT_YEAR = 2024;
    private static final String DEFAULT_SEASON = "冬";

    private final PublishRepository repository;
    private final Handler mainHandler;

    public PublishPresenter(Context context) {
        this.repository = new PublishRepository(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void publish(String title, String content, List<MemoryPhoto> images,
                       double lat, double lng) {
//        // 参数校验
//        if (title == null || title.trim().isEmpty()) {
//            ifViewAttached(view -> view.showError("标题不能为空"));
//            return;
//        }
//
//        if (content == null || content.trim().isEmpty()) {
//            ifViewAttached(view -> view.showError("内容不能为空"));
//            return;
//        }
//
//        if (lat == 0.0 && lng == 0.0) {
//            ifViewAttached(view -> view.showError("请选择位置"));
//            return;
//        }

        // 调用 View 获取其他必要数据（地址、时间）
        // 这里通过 ifViewAttached 回调获取 View 中的数据
        ifViewAttached(view -> {
            String address = view.getAddress();
            String timeDisplayText = view.getTimeDisplayText();

            // 解析时间
            int memoryYear = DEFAULT_YEAR;
            String season = DEFAULT_SEASON;
            if (timeDisplayText != null && !timeDisplayText.isEmpty()
                    && !timeDisplayText.contains("点击选择")) {
                String[] parts = timeDisplayText.split("·");
                if (parts.length >= 2) {
                    try {
                        memoryYear = Integer.parseInt(parts[0].trim());
                        season = parts[1].trim();
                    } catch (NumberFormatException e) {
                        // 使用默认值
                    }
                }
            }

            // 调用 Repository 保存
            repository.saveMemory(title, content, images, memoryYear, season, lat, lng, address,
                new MemoryPostRepository.Callback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        mainHandler.post(() -> ifViewAttached(PublishContract.View::showPublishSuccess));
                    }

                    @Override
                    public void onError(Exception e) {
                        mainHandler.post(() -> ifViewAttached(view1 -> view1.showError("保存失败: " + e.getMessage())));
                    }
                });
        });
    }

    @Override
    public void setLocation(double lat, double lng) {
        if (lat == 0 && lng == 0) {
            ifViewAttached(view -> view.showError("无效的位置"));
            return;
        }

        // 获取地址名称
        String address = repository.getAddress(lat, lng);
        ifViewAttached(view -> view.showLocation(address, lat, lng));
    }
}
