package com.huofu.module.i5wei.menu.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by cherie on 2016/12/23.
 */
public class StoreTimeBucketItemDbRouter  extends BaseStoreDbRouter {
    private static final String tableBaseName = "tb_store_time_bucket_item";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
