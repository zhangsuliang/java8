package com.huofu.module.i5wei.table.service;

import java.util.List;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class MergeTableRecordResult {

	private StoreTableRecord originalTableRecord;
	private StoreTableRecord targetTableRecord;
	private List<StoreOrder> subOrderList;
	
	public StoreTableRecord getOriginalTableRecord() {
		return originalTableRecord;
	}
	
	public void setOriginalTableRecord(StoreTableRecord originalTableRecord) {
		this.originalTableRecord = originalTableRecord;
	}
	
	public StoreTableRecord getTargetTableRecord() {
		return targetTableRecord;
	}
	
	public void setTargetTableRecord(StoreTableRecord targetTableRecord) {
		this.targetTableRecord = targetTableRecord;
	}
	
	public List<StoreOrder> getSubOrderList() {
		return subOrderList;
	}
	
	public void setSubOrderList(List<StoreOrder> subOrderList) {
		this.subOrderList = subOrderList;
	}
}
