package com.huofu.module.i5wei.table.service;

import huofucore.facade.i5wei.table.TableRecordStatusEnum;

import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class ClearTableRecordResult {

	private StoreTableRecord storeTableRecord;
	
	private TableRecordStatusEnum preClearTableRecordStatus;
	
	public TableRecordStatusEnum getPreClearTableRecordStatus() {
		return preClearTableRecordStatus;
	}
	
	public void setPreClearTableRecordStatus(
			TableRecordStatusEnum preClearTableRecordStatus) {
		this.preClearTableRecordStatus = preClearTableRecordStatus;
	}

	public StoreTableRecord getStoreTableRecord() {
		return storeTableRecord;
	}

	public void setStoreTableRecord(StoreTableRecord storeTableRecord) {
		this.storeTableRecord = storeTableRecord;
	}
	
}
