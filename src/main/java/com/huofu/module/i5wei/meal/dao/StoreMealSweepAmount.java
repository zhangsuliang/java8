package com.huofu.module.i5wei.meal.dao;

public class StoreMealSweepAmount {
    
    private String orderId;
    
    private long tableRecordId;
    
    private double amountProduct;
    
    private double sweepAmount;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getTableRecordId() {
        return tableRecordId;
    }

    public void setTableRecordId(long tableRecordId) {
        this.tableRecordId = tableRecordId;
    }

    public double getAmountProduct() {
        return amountProduct;
    }

    public void setAmountProduct(double amountProduct) {
        this.amountProduct = amountProduct;
    }

    public double getSweepAmount() {
        return sweepAmount;
    }

    public void setSweepAmount(double sweepAmount) {
        this.sweepAmount = sweepAmount;
    }
    
}
