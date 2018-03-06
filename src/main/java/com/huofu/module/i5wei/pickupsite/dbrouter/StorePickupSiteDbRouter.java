package com.huofu.module.i5wei.pickupsite.dbrouter;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;

/**
 * Created by taoming on 2016/12/2.
 */
public class StorePickupSiteDbRouter extends BaseStoreDbRouter {

	private static final String baseName = "tb_store_pickup_site";

	@Override
	public String getLogicName() {
		return baseName;
	}
}
