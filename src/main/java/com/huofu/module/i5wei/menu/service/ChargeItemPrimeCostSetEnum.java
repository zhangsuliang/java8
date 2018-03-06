package com.huofu.module.i5wei.menu.service;

/**
 *
 * Created by lixuwei on 16/3/31.
 */
public enum ChargeItemPrimeCostSetEnum {

    /**
     * 0=未设置
     */
    NO_SET(0),

    /**
     * 1=部分设置
     */
    PART_SET(1),

    /**
     * 2=全部设置
     */
    ALL_SET(2);

    private final int value;

    ChargeItemPrimeCostSetEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
