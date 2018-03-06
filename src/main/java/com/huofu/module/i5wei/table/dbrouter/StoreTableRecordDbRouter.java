package com.huofu.module.i5wei.table.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * 桌台记录router
 * @author licheng7
 * 2016年5月9日 下午5:20:03
 */
public class StoreTableRecordDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_table_record";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
