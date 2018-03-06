package com.huofu.module.i5wei.request.entity;

import org.apache.thrift.TEnum;

/**
 * 唯一性请求业务类型
 */
public enum Store5weiRequestBizType implements TEnum {
	/**
	 * 未知
	 */
	NONE(0),
	/**
	 * 下单
	 */
	ORDER(1);

	private final int value;

	private Store5weiRequestBizType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static Store5weiRequestBizType findByValue(int value) {
		switch (value) {
		case 0:
			return NONE;
		case 1:
			return ORDER;
		default:
			return null;
		}
	}
}
