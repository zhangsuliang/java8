package com.huofu.module.i5wei.table.service;

import java.util.List;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTable;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class TransferTableRecordResult {

	private StoreTableRecord originalTableRecord;
	private StoreTable targetTable;
	private List<StoreOrder> subOrderList;
	private StoreTableRecord tableRecord;
	
	public StoreTableRecord getOriginalTableRecord() {
		return originalTableRecord;
	}
	
	public void setOriginalTableRecord(StoreTableRecord originalTableRecord) {
		this.originalTableRecord = originalTableRecord;
	}
	
	public StoreTable getTargetTable() {
		return targetTable;
	}
	
	public void setTargetTable(StoreTable targetTable) {
		this.targetTable = targetTable;
	}
	
	public List<StoreOrder> getSubOrderList() {
		return subOrderList;
	}
	
	public void setSubOrderList(List<StoreOrder> subOrderList) {
		this.subOrderList = subOrderList;
	}
	
	public StoreTableRecord getTableRecord() {
		return tableRecord;
	}
	
	public void setTableRecord(StoreTableRecord tableRecord) {
		this.tableRecord = tableRecord;
	}
}
