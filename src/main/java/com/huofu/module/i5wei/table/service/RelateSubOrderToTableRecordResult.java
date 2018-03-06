package com.huofu.module.i5wei.table.service;

import java.util.List;

import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class RelateSubOrderToTableRecordResult {

	private StoreTableRecord storeTableRecord;
	private long userId;
	private List<StoreOrder> storeOrders;
	private StoreOrder masterOrder;
	/**
	 * 多个子订单的待出餐列表
	 */
	private List<StoreMealTakeup> storeMealTakeups;

	public List<StoreMealTakeup> getStoreMealTakeups() {
		return storeMealTakeups;
	}

	public void setStoreMealTakeups(List<StoreMealTakeup> storeMealTakeups) {
		this.storeMealTakeups = storeMealTakeups;
	}

	public StoreTableRecord getStoreTableRecord() {
		return storeTableRecord;
	}
	
	public void setStoreTableRecord(StoreTableRecord storeTableRecord) {
		this.storeTableRecord = storeTableRecord;
	}
	
	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}

	public List<StoreOrder> getStoreOrders() {
		return storeOrders;
	}

	public void setStoreOrders(List<StoreOrder> storeOrders) {
		this.storeOrders = storeOrders;
	}

	public StoreOrder getMasterOrder() {
		return masterOrder;
	}

	public void setMasterOrder(StoreOrder masterOrder) {
		this.masterOrder = masterOrder;
	}
	
}
