package com.huofu.module.i5wei.mealport.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.mealport.StoreMealPortTaskStatusEnum;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.mealport.dbrouter.StoreMealTaskDbRouter;

/**
 * 出餐口与Pad的任务关系表
 */
@Table(name = "tb_store_meal_task", dalParser = StoreMealTaskDbRouter.class)
public class StoreMealTask extends AbsEntity {

    /**
     * 出餐口ID
     */
    @Id
    @Column("port_id")
    private long portId;

    /**
     * 商户ID
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺ID
     */
    @Column("store_id")
    private long storeId;

    /**
     * Pad编号，副本ID
     */
    @Column("appcopy_id")
    private long appcopyId;

    /**
     * 任务关系状态：0=解除任务关系，1=建立任务关系
     */
    @Column("task_status")
    private int taskStatus;

    /**
     * 打印机连接状态：0=未连接，1=正常连接，2=无法打印
     */
    @Column("printer_status")
    private int printerStatus;

    /**
     * 出餐方式：0=手动，1=自动
     */
    @Column("checkout_type")
    private int checkoutType;

    /**
     * 打印机外接设备ID
     */
    @Column("printer_peripheral_id")
    private long printerPeripheralId;

    /**
     * 上次出餐打印时间
     */
    @Column("printer_time")
    private long printerTime;
    
    /**
     * 上次出餐请求时间
     */
    @Column("meal_time")
    private long mealTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

    public long getPortId() {
        return portId;
    }

    public void setPortId(long portId) {
        this.portId = portId;
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

    public long getAppcopyId() {
        return appcopyId;
    }

    public void setAppcopyId(long appcopyId) {
        this.appcopyId = appcopyId;
    }

    public int getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(int taskStatus) {
        this.taskStatus = taskStatus;
    }

    public int getPrinterStatus() {
        return printerStatus;
    }

    public void setPrinterStatus(int printerStatus) {
        this.printerStatus = printerStatus;
    }

    public int getCheckoutType() {
        return checkoutType;
    }

    public void setCheckoutType(int checkoutType) {
        this.checkoutType = checkoutType;
    }

    public long getPrinterPeripheralId() {
        return printerPeripheralId;
    }

    public void setPrinterPeripheralId(long printerPeripheralId) {
        this.printerPeripheralId = printerPeripheralId;
    }

    public long getPrinterTime() {
        return printerTime;
    }

    public void setPrinterTime(long printerTime) {
        this.printerTime = printerTime;
    }

	public long getMealTime() {
        return mealTime;
    }

    public void setMealTime(long mealTime) {
        this.mealTime = mealTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void cancelTask() {
        this.snapshot();
        this.setTaskStatus(StoreMealPortTaskStatusEnum.OFF.getValue());
        this.setUpdateTime(System.currentTimeMillis());
        this.update();
    }

}