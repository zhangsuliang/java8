package com.huofu.module.i5wei.base;

public enum ModuleBizType {
    BUY_PREPAIDCARD(1),
    PAY_FOR_STORE_ORDER(3);

    private final int value;

    private ModuleBizType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ModuleBizType findByValue(int value) {
        switch (value) {
            case 1:
                return BUY_PREPAIDCARD;
            case 3:
                return PAY_FOR_STORE_ORDER;
            default:
                return null;
        }
    }

}
