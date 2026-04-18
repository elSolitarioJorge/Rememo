package com.ggg.rememo.feature.here.contract;

import com.ggg.rememo.core.base.IBaseView;
import com.ggg.rememo.core.data.model.entity.MemoryPoint;

public interface MemoryPointBottomSheetContract {

    interface View extends IBaseView {

        void showPointInfo(MemoryPoint point);

        void navigateToTimeline(String pointId);

        void dismiss();
    }

    interface Presenter {

        void onViewCreated(MemoryPoint point);

        void onEnterTimelineClicked();
    }
}
