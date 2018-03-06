package com.huofu.module.i5wei.inventory.entity;

import com.huofu.module.i5wei.inventory.dbrouter.StoreInventoryWeekDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_inventory_week", dalParser = StoreInventoryWeekDbRouter.class)
public class StoreInventoryWeek {

    public static final double defaultAmount = Double.valueOf(0);//库存默认为空

    /**
     * 产品周期库存ID，全库唯一主键
     */
    @Id
    @Column("inv_week_id")
    private long invWeekId;

    /**
     * 父节点parent_week_id
     */
    @Column("parent_week_id")
    private long parentWeekId;

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
     * 营业时段id，按周天库存则为0
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 周期 (周一,周二, ....,周日)
     */
    @Column("week_day")
    private int weekDay;

    /**
     * 产品ID
     */
    @Column("product_id")
    private long productId;

    /**
     * 常规预定数量
     */
    @Column("amount")
    private double amount;

    /**
     * 有效期开始时间
     */
    @Column("begin_time")
    private long beginTime;

    /**
     * 有效期结束时间
     */
    @Column("end_time")
    private long endTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 新增时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 营业时段
     */
    private StoreTimeBucket storeTimeBucket;

    public long getInvWeekId() {
        return invWeekId;
    }

    public void setInvWeekId(long invWeekId) {
        this.invWeekId = invWeekId;
    }

    public long getParentWeekId() {
        return parentWeekId;
    }

    public void setParentWeekId(long parentWeekId) {
        this.parentWeekId = parentWeekId;
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

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
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

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }

}