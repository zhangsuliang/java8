package com.huofu.module.i5wei.order.service;

import java.util.List;

public class StoreOrderDeliveryPreparingResult {
	/**
	 * 商户ID
	 */
	private int merchantId;
	/**
	 * 店铺ID
	 */
	private long storeId;
	/**
	 * 外送订单列表
	 */
	private List<String> deliveryOrderIds;
	
	public int getMerchantId() {
		return merchantId;
	}
	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}
	public long getStoreId() {
		return storeId;
	}
	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}
	public List<String> getDeliveryOrderIds() {
		return deliveryOrderIds;
	}
	public void setDeliveryOrderIds(List<String> deliveryOrderIds) {
		this.deliveryOrderIds = deliveryOrderIds;
	}

}
