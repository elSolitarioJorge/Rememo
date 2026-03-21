package com.ggg.rememo.feature.here.presenter;

import com.ggg.rememo.core.base.BasePresenter;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;
import com.ggg.rememo.feature.here.contract.MemoryPointBottomSheetContract;

public class MemoryPointBottomSheetPresenter
        extends BasePresenter<MemoryPointBottomSheetContract.View>
        implements MemoryPointBottomSheetContract.Presenter {

    private MemoryPoint memoryPoint;

    @Override
    public void onViewCreated(MemoryPoint point) {
        this.memoryPoint = point;
        if (point != null) {
            ifViewAttached(view -> view.showPointInfo(point));
        } else {
            ifViewAttached(MemoryPointBottomSheetContract.View::dismiss);
        }
    }

    @Override
    public void onEnterTimelineClicked() {
        if (memoryPoint != null) {
            ifViewAttached(view -> view.navigateToTimeline(memoryPoint.getPointId()));
        }
    }
}
