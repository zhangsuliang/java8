package com.huofu.module.i5wei.delivery.dbrouter;

import com.huofu.module.i5wei.base.BaseMerchantDbRouter;

public class MerchantOrderDeliveryLastDbRouter extends BaseMerchantDbRouter {

	private static final String baseName = "tb_merchant_order_delivery_last";
	
	@Override
	public String getLogicName() {
		return baseName;
	}

}
