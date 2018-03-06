package com.huofu.module.i5wei.request.service;

import com.huofu.module.i5wei.request.entity.Store5weiRequestBizType;

/**
 * 请求唯一性参数
 */
public class Store5weiRequestParam {

	/**
	 * 唯一主键，请求ID
	 */
	private String requestId;

	/**
	 * 商户ID
	 */
	private int merchantId;

	/**
	 * 店铺ID
	 */
	private long storeId;

	/**
	 * 业务类型{@link Store5weiRequestBizType}
	 */
	private int i5weiBizType;

	/**
	 * 业务ID
	 */
	private String i5weiBizId;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

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

	public int getI5weiBizType() {
		return i5weiBizType;
	}

	public void setI5weiBizType(int i5weiBizType) {
		this.i5weiBizType = i5weiBizType;
	}

	public String getI5weiBizId() {
		return i5weiBizId;
	}

	public void setI5weiBizId(String i5weiBizId) {
		this.i5weiBizId = i5weiBizId;
	}

}