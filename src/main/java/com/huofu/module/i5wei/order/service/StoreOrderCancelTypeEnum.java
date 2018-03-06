/**
 * Autogenerated by Thrift Compiler (0.9.3)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package com.huofu.module.i5wei.order.service;

/**
 * 取消预定订单类型
 */
public enum StoreOrderCancelTypeEnum implements org.apache.thrift.TEnum {
	/**
	 * 未取消（默认，订单取消时此项表示店员取消
	 */
	DEFAULT_OR_STAFF(0),
	/**
	 * 用户取消
	 */
	USER(1),
	/**
	 * 系统自动取消
	 */
	SYSTEM(2);

	private final int value;

	private StoreOrderCancelTypeEnum(int value) {
		this.value = value;
	}

	/**
	 * Get the integer value of this enum value, as defined in the Thrift IDL.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Find a the enum type by its integer value, as defined in the Thrift IDL.
	 * 
	 * @return null if the value is not found.
	 */
	public static StoreOrderCancelTypeEnum findByValue(int value) {
		switch (value) {
		case 0:
			return DEFAULT_OR_STAFF;
		case 1:
			return USER;
		case 2:
			return SYSTEM;
		default:
			return null;
		}
	}
}
