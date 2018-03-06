package com.huofu.module.i5wei.inventory.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 4/4/15.
 */
public class StoreInventoryDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_inventory";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
