package com.huofu.module.i5wei.table.service;

import java.util.List;

import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class OrderPayFinishResult {

	private StoreTableRecord storeTableRecord;
	private long userId;
	private boolean sendTableRecordAddDishMsg;
	private List<StoreOrder> storeOrders;
	private boolean isSettleMent;

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
	
	public boolean isSendTableRecordAddDishMsg() {
		return sendTableRecordAddDishMsg;
	}
	
	public void setSendTableRecordAddDishMsg(boolean sendTableRecordAddDishMsg) {
		this.sendTableRecordAddDishMsg = sendTableRecordAddDishMsg;
	}

	public List<StoreOrder> getStoreOrders() {
		return storeOrders;
	}

	public void setStoreOrders(List<StoreOrder> storeOrders) {
		this.storeOrders = storeOrders;
	}

	public boolean isSettleMent() {
		return isSettleMent;
	}

	public void setSettleMent(boolean isSettleMent) {
		this.isSettleMent = isSettleMent;
	}
	
}
