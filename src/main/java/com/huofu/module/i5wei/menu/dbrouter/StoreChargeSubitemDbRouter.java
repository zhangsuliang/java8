package com.huofu.module.i5wei.menu.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 2/16/15.
 */
public class StoreChargeSubitemDbRouter extends BaseStoreDbRouter {

    private static final String tableBaseName = "tb_store_charge_subitem";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
