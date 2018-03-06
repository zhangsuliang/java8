package com.huofu.module.i5wei.table.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by jiajin.nervous on 16/4/26.
 */
public class StoreTableDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_table";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
