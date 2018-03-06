package com.huofu.module.i5wei.order.service;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import huofucore.facade.i5wei.order.StoreOrderWaimaiPlaceParam;

public class PlaceOrderWaimaiParam {

	private StoreOrderWaimaiPlaceParam storeOrderWaimaiPlaceParam;

	private int currencyId;

	private long repastDate;

	private StoreTimeBucket storeTimeBucket;

	public StoreOrderWaimaiPlaceParam getStoreOrderWaimaiPlaceParam() {
		return storeOrderWaimaiPlaceParam;
	}

	public void setStoreOrderWaimaiPlaceParam(StoreOrderWaimaiPlaceParam storeOrderWaimaiPlaceParam) {
		this.storeOrderWaimaiPlaceParam = storeOrderWaimaiPlaceParam;
	}

	public int getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(int currencyId) {
		this.currencyId = currencyId;
	}

	public long getRepastDate() {
		return repastDate;
	}

	public void setRepastDate(long repastDate) {
		this.repastDate = repastDate;
	}

	public StoreTimeBucket getStoreTimeBucket() {
		return storeTimeBucket;
	}

	public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
		this.storeTimeBucket = storeTimeBucket;
	}

}
