package com.huofu.module.i5wei.menu.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 2/16/15.
 */
public class StoreTimeBucketDbRouter extends BaseStoreDbRouter {

    private static final String tableBaseName = "tb_store_time_bucket";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
