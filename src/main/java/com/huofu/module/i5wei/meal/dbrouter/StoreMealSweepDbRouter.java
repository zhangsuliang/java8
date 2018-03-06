package com.huofu.module.i5wei.meal.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

public class StoreMealSweepDbRouter extends BaseStoreDbRouter {
    private static final String baseName = "tb_store_meal_sweep";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
