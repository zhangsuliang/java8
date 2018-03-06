package com.huofu.module.i5wei.order.service;

public enum StoreOrderInvTypeEnum {
	/**
	 * 未知
	 */
	UNKNOWN(0),
	/**
	 * 已预定产品
	 */
	BOOK_ORDER(1),
	/**
	 * 已取号产品
	 */
	TAKED_CODE(2),
	/**
	 * 占用库存
	 */
	TAKEUP_INVENTORY(3);
	
	private final int value;

	private StoreOrderInvTypeEnum(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	public static StoreOrderInvTypeEnum findByValue(int value) {
		switch (value) {
		case 0:
			return UNKNOWN;
		case 1:
			return BOOK_ORDER;
		case 2:
			return TAKED_CODE;
		case 3:
			return TAKEUP_INVENTORY;
		default:
			return null;
		}
	}
}
