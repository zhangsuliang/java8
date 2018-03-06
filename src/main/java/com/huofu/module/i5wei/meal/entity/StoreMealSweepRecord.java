package com.huofu.module.i5wei.meal.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.meal.dbrouter.StoreMealSweepRecordDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 
 */
@Table(name = "tb_store_meal_sweep_record", dalParser = StoreMealSweepRecordDbRouter.class)
public class StoreMealSweepRecord extends AbsEntity {

    /**
     * 划菜主键Id
     */
    @Id
    @Column("tid")
    private long tid;

    /**
     * 
     */
    @Column("order_id")
    private String orderId;

    /**
     * 商户Id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺Id
     */
    @Column("store_id")
    private long storeId;

    /**
     * 就餐日期
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 营业时段Id
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 加工档口Id
     */
    @Column("port_id")
    private long portId;

    /**
     * 传菜口Id
     */
    @Column("send_port_id")
    private long sendPortId;
    
    /**
     * 取餐流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;

    /**
     * 产品总数
     */
    @Column("amount_product")
    private double amountProduct;
    
    /**
     * 桌台记录Id
     */
    @Column("table_record_id")
    private long tableRecordId;

    /**
     * 划菜数量
     */
    @Column("sweep_meal_amount")
    private double sweepMealAmount;

    /**
     * 最后划菜时间
     */
    @Column("last_sweep_meal_time")
    private long lastSweepMealTime;

    /**
     * 是否全部划完
     */
    @Column("all_sweep_status")
    private boolean allSweepStatus;
    /**
     * 修改时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public long getRepastDate() {
        return repastDate;
    }

    public void setRepastDate(long repastDate) {
        this.repastDate = repastDate;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public long getPortId() {
        return portId;
    }

    public void setPortId(long portId) {
        this.portId = portId;
    }

    public long getSendPortId() {
        return sendPortId;
    }

    public void setSendPortId(long sendPortId) {
        this.sendPortId = sendPortId;
    }

    public int getTakeSerialNumber() {
        return takeSerialNumber;
    }

    public void setTakeSerialNumber(int takeSerialNumber) {
        this.takeSerialNumber = takeSerialNumber;
    }

    public double getAmountProduct() {
        return amountProduct;
    }

    public void setAmountProduct(double amountProduct) {
        this.amountProduct = amountProduct;
    }

    public long getTableRecordId() {
        return tableRecordId;
    }

    public void setTableRecordId(long tableRecordId) {
        this.tableRecordId = tableRecordId;
    }

    public double getSweepMealAmount() {
        return sweepMealAmount;
    }

    public void setSweepMealAmount(double sweepMealAmount) {
        this.sweepMealAmount = sweepMealAmount;
    }

    public long getLastSweepMealTime() {
        return lastSweepMealTime;
    }

    public void setLastSweepMealTime(long lastSweepMealTime) {
        this.lastSweepMealTime = lastSweepMealTime;
    }

    public boolean isAllSweepStatus() {
        return allSweepStatus;
    }

    public void setAllSweepStatus(boolean allSweepStatus) {
        this.allSweepStatus = allSweepStatus;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}