package com.huofu.module.i5wei.table.service;

import huofucore.facade.i5wei.table.TableRecordStatusEnum;

/**
 * 桌台记录状态
 * @author licheng7
 * 2016年5月27日 下午5:36:17
 */
public class TableRecordStatusResult {

	/**
	 * 桌台记录状态
	 */
	private TableRecordStatusEnum tableRecordStatusEnum;
	/**
	 * 总共待出餐数量
	 */
	private long totalStoreMealTakeupNum;
	/**
	 * 总共已出餐数量
	 */
	private long totalStoreMealCheckoutNum;
	
	public TableRecordStatusEnum getTableStatusEnum() {
		return tableRecordStatusEnum;
	}
	public void setTableStatusEnum(TableRecordStatusEnum tableStatusEnum) {
		this.tableRecordStatusEnum = tableStatusEnum;
	}
	public long getTotalStoreMealTakeupNum() {
		return totalStoreMealTakeupNum;
	}
	public void setTotalStoreMealTakeupNum(long totalStoreMealTakeupNum) {
		this.totalStoreMealTakeupNum = totalStoreMealTakeupNum;
	}
	public long getTotalStoreMealCheckoutNum() {
		return totalStoreMealCheckoutNum;
	}
	public void setTotalStoreMealCheckoutNum(long totalStoreMealCheckoutNum) {
		this.totalStoreMealCheckoutNum = totalStoreMealCheckoutNum;
	}
	
}
