package com.huofu.module.i5wei.meal.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 4/4/15.
 */
public class StoreMealCheckoutDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_meal_checkout";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
