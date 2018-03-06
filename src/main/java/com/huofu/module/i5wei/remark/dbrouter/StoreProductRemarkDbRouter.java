package com.huofu.module.i5wei.remark.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 2/16/15.
 */
public class StoreProductRemarkDbRouter extends BaseStoreDbRouter {

    private static final String tableBaseName = "tb_store_product_remark";

    @Override
    public String getLogicName() {
        return tableBaseName;
    }
}
