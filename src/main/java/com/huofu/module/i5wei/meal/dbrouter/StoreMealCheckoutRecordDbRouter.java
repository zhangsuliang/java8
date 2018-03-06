package com.huofu.module.i5wei.meal.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by chenkai on 6/19/15.
 */
public class StoreMealCheckoutRecordDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_meal_checkout_record";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
