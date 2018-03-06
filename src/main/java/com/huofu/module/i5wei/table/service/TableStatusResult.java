package com.huofu.module.i5wei.table.service;

import huofucore.facade.i5wei.table.TableRecordStatusEnum;

/**
 * 桌台记录状态&待出餐、已出餐数量
 * @author licheng7
 * 2016年6月4日 上午11:44:34
 */
public class TableStatusResult {
	/**
	 * 桌台记录状态
	 */
	private TableRecordStatusEnum tableRecordStatusEnum;
	/**
	 * 待出餐数量
	 */
	private int totalStoreMealTakeupNum = 0;
	/**
	 * 已出餐数量
	 */
	private int totalStoreMealCheckoutNum = 0;
	
	public TableRecordStatusEnum getTableRecordStatusEnum() {
		return tableRecordStatusEnum;
	}
	
	public void setTableRecordStatusEnum(TableRecordStatusEnum tableRecordStatusEnum) {
		this.tableRecordStatusEnum = tableRecordStatusEnum;
	}
	
	public int getTotalStoreMealCheckoutNum() {
		return totalStoreMealCheckoutNum;
	}
	
	public void setTotalStoreMealCheckoutNum(int totalStoreMealCheckoutNum) {
		this.totalStoreMealCheckoutNum = totalStoreMealCheckoutNum;
	}

	public int getTotalStoreMealTakeupNum() {
		return totalStoreMealTakeupNum;
	}

	public void setTotalStoreMealTakeupNum(int totalStoreMealTakeupNum) {
		this.totalStoreMealTakeupNum = totalStoreMealTakeupNum;
	}
}
