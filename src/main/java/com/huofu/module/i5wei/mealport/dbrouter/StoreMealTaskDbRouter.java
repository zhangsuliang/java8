package com.huofu.module.i5wei.mealport.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 4/4/15.
 */
public class StoreMealTaskDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_meal_task";

    @Override
    public String getLogicName() {
        return baseName;
    }
    
}
