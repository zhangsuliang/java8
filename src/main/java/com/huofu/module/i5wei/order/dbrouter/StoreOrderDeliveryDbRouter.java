package com.huofu.module.i5wei.order.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by akwei on 4/4/15.
 */
public class StoreOrderDeliveryDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_order_delivery";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
