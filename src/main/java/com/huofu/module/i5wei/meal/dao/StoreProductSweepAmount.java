package com.huofu.module.i5wei.meal.dao;

public class StoreProductSweepAmount {
    /**
     * 产品Id
     */
    private long productId;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 堂食剩余数量
     */
    private double dineInRemainSend;
    /**
     * 打包剩余数量
     */
    private double packagedRemainSend;
    
    public long getProductId() {
        return productId;
    }
    public void setProductId(long productId) {
        this.productId = productId;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public double getDineInRemainSend() {
        return dineInRemainSend;
    }
    public void setDineInRemainSend(double dineInRemainSend) {
        this.dineInRemainSend = dineInRemainSend;
    }
    public double getPackagedRemainSend() {
        return packagedRemainSend;
    }
    public void setPackagedRemainSend(double packagedRemainSend) {
        this.packagedRemainSend = packagedRemainSend;
    }
}
