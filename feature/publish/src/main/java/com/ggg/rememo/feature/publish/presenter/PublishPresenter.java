package com.ggg.rememo.feature.publish.presenter;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.feature.publish.contract.PublishContract;
import com.ggg.rememo.feature.publish.data.PublishRepository;

/**
 * 发布模块 Presenter
 */
public class PublishPresenter extends BasePresenter<PublishContract.View>
        implements PublishContract.Presenter {

    private final PublishRepository repository;

    public PublishPresenter() {
        this.repository = new PublishRepository();
    }

    @Override
    public void publish(String content, String imagePath, double lat, double lng) {
        // 参数校验
        if (content == null || content.trim().isEmpty()) {
            ifViewAttached(view -> view.showError("内容不能为空"));
            return;
        }

        if (lat == 0 && lng == 0) {
            ifViewAttached(view -> view.showError("请选择位置"));
            return;
        }

        // 调用 Repository 保存数据
        boolean success = repository.saveMemory(content.trim(), imagePath, lat, lng);

        if (success) {
            ifViewAttached(PublishContract.View::showPublishSuccess);
        } else {
            ifViewAttached(view -> view.showError("保存失败，请重试"));
        }
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
