package com.huofu.module.i5wei.table.service;


public enum TableRecordBatchRefundStatusEnum implements org.apache.thrift.TEnum {

	/**
	 * 未退款
	 */
	UNREFUND(0),
	
	/**
	 * 退款中
	 */
	REFUNDING(1),
	
	/**
	 * 退款完成
	 */
	FINISH(2),
	
	/**
	 * 退款失败
	 */
	FAIL(3);

	private final int value;

	private TableRecordBatchRefundStatusEnum (int value) {
		this.value = value;
	}
	
	@Override
	public int getValue() {
		return value;
	}
	
	public static TableRecordBatchRefundStatusEnum findByValue(int value) {
		switch (value) {
		case 0:
			return UNREFUND;
		case 1:
			return REFUNDING;
		case 2:
			return FINISH;
		case 3:
			return FAIL;
		default:
			return null;
		}
	}
	
}
