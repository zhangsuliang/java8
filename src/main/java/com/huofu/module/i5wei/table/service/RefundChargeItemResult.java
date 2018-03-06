package com.huofu.module.i5wei.table.service;

import java.util.List;

import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class RefundChargeItemResult {

	private StoreTableRecord storeTableRecord;
	private List<StoreOrderRefundItem> storeOrderRefundItems;
	
	public StoreTableRecord getStoreTableRecord() {
		return storeTableRecord;
	}
	
	public void setStoreTableRecord(StoreTableRecord storeTableRecord) {
		this.storeTableRecord = storeTableRecord;
	}
	
	public List<StoreOrderRefundItem> getStoreOrderRefundItems() {
		return storeOrderRefundItems;
	}
	
	public void setStoreOrderRefundItems(
			List<StoreOrderRefundItem> storeOrderRefundItems) {
		this.storeOrderRefundItems = storeOrderRefundItems;
	}
}
