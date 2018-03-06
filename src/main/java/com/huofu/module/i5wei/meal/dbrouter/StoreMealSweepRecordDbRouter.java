package com.huofu.module.i5wei.meal.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

public class StoreMealSweepRecordDbRouter extends BaseStoreDbRouter {
    private static final String baseName = "tb_store_meal_sweep_record";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
