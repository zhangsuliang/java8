package com.huofu.module.i5wei.table.service;

import com.huofu.module.i5wei.table.entity.StoreTable;

/**
 * 开台请求参数
 * @author licheng7
 * 2016年6月17日 下午8:18:31
 */
public class CreateStoreTableRecordParam {
	
	/**
	 * 商编
	 */
	private int merchantId;
	/**
	 * 店铺id
	 */
	private long storeId;
	/**
	 * 就餐日期
	 */
	private long repastDate;
	/**
	 * 营业时段
	 */
	private long timeBucketId;
	/**
	 * 桌台
	 */
	private StoreTable storeTable;
	/**
	 * 桌台记录序列号
	 */
	private int tableRecordSeq;
	/**
	 * 就餐人数
	 */
	private int customerTraffic;
	/**
	 * 开台服务员id
	 */
	private long createTableRecordStaffId;
	/**
	 * 自助开台用户id
	 */
	private long createTableRecordUserId;
	/**
	 * 终端类型
	 */
	private int clientType;
	/**
	 * 默认桌台服务员
	 */
	private long defaultStaffId;

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

	public long getRepastDate() {
		return repastDate;
	}

	public void setRepastDate(long repastDate) {
		this.repastDate = repastDate;
	}

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}

	public StoreTable getStoreTable() {
		return storeTable;
	}

	public void setStoreTable(StoreTable storeTable) {
		this.storeTable = storeTable;
	}

	public int getTableRecordSeq() {
		return tableRecordSeq;
	}

	public void setTableRecordSeq(int tableRecordSeq) {
		this.tableRecordSeq = tableRecordSeq;
	}

	public int getCustomerTraffic() {
		return customerTraffic;
	}

	public void setCustomerTraffic(int customerTraffic) {
		this.customerTraffic = customerTraffic;
	}

	public long getCreateTableRecordStaffId() {
		return createTableRecordStaffId;
	}

	public void setCreateTableRecordStaffId(long createTableRecordStaffId) {
		this.createTableRecordStaffId = createTableRecordStaffId;
	}

	public long getCreateTableRecordUserId() {
		return createTableRecordUserId;
	}

	public void setCreateTableRecordUserId(long createTableRecordUserId) {
		this.createTableRecordUserId = createTableRecordUserId;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public long getDefaultStaffId() {
		return defaultStaffId;
	}

	public void setDefaultStaffId(long defaultStaffId) {
		this.defaultStaffId = defaultStaffId;
	}

}
