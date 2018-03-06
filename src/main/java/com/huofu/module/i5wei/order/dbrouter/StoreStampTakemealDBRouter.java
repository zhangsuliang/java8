package com.huofu.module.i5wei.order.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by chengq on 16/4/14.
 */
public class StoreStampTakemealDBRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_stamp_takemeal";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
