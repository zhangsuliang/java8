package com.huofu.module.i5wei.table.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by lixuwei on 17/1/16.
 */
public class StoreTableRecordOptlogDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_table_record_optlog";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
