package com.huofu.module.i5wei.table.service;

import java.util.List;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class OpenTableRecordResult {

	private StoreTableRecord storeTableRecord;
	private List<StoreOrder> storeOrders;
	private StoreOrder masterOrder;

	public StoreTableRecord getStoreTableRecord() {
		return storeTableRecord;
	}

	public void setStoreTableRecord(StoreTableRecord storeTableRecord) {
		this.storeTableRecord = storeTableRecord;
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
