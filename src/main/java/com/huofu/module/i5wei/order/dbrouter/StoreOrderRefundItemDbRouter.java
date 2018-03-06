package com.huofu.module.i5wei.order.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by chenkai on 5/4/16.
 */
public class StoreOrderRefundItemDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_order_refund_item";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
