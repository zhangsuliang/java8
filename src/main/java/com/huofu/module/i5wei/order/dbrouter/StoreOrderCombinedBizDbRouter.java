package com.huofu.module.i5wei.order.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by wangxiaoyang on 16/8/19
 */
public class StoreOrderCombinedBizDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_store_order_combined_biz";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
