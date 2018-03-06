package com.huofu.module.i5wei.delivery.dbrouter;

import com.huofu.module.i5wei.base.BaseMerchantDbRouter;

public class MerchantUserDeliveryAddressDbRouter extends BaseMerchantDbRouter {
    private static final String baseName = "tb_merchant_user_delivery_address";

    @Override
    public String getLogicName() {
        return baseName;
    }
}
