package com.huofu.module.i5wei.batchproc.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

public class StoreBatchProcDbRouter extends BaseStoreDbRouter {

    private static final String tableBaseName = "tb_store_batch_proc";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
