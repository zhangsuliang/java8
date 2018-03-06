package com.huofu.module.i5wei.menu.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by lixuwei on 2016-03-16.
 */
public class StoreChargeItemCategoryDbRouter extends BaseStoreDbRouter {

    private static final String tableBaseName = "tb_store_charge_item_category";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
