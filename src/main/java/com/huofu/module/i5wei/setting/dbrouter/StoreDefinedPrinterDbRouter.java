package com.huofu.module.i5wei.setting.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by jiajin.nervous on 16/10/10.
 */
public class StoreDefinedPrinterDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_defined_printer";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
