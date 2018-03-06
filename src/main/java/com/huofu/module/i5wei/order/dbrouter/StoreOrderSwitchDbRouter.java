package com.huofu.module.i5wei.order.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by jiangjiajin on 30/12/15.
 */
public class StoreOrderSwitchDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_order_switch";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
