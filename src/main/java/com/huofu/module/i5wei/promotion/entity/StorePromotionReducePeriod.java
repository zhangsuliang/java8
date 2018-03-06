package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.entity.StoreTimebucketWeek;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.thrift.serialize.ThriftField;

/**
 * 满减活动周期设置表
 */
@Table(name = "tb_store_promotion_reduce_period", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionReducePeriod extends BaseEntity {

    /**
     * 满减活动ID
     */
    @ThriftField(1)
    @Id
    @Column("promotion_reduce_id")
    private long promotionReduceId;

    /**
     * 营业时段ID
     */
    @ThriftField(2)
    @Id(1)
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 星期日期
     */
    @ThriftField(3)
    @Id(2)
    @Column("week_day")
    private int weekDay;

    /**
     * 店铺ID
     */
    @ThriftField(4)
    @Column("store_id")
    private long storeId;

    /**
     * 商户ID
     */
    @ThriftField(5)
    @Column("merchant_id")
    private int merchantId;

    /**
     * 创建时间
     */
    @ThriftField(6)
    @Column("create_time")
    private long createTime;

    private StoreTimeBucket storeTimeBucket;

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }

    public long getPromotionReduceId() {
        return promotionReduceId;
    }

    public void setPromotionReduceId(long promotionReduceId) {
        this.promotionReduceId = promotionReduceId;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean canUse(int weekDay, long timeBucketId) {
        if (this.weekDay == weekDay && this.timeBucketId == timeBucketId) {
            return true;
        }
        return false;
    }

    public boolean equalsData(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorePromotionReducePeriod)) return false;

        StorePromotionReducePeriod that = (StorePromotionReducePeriod) o;

        if (getPromotionReduceId() != that.getPromotionReduceId()) return false;
        if (getTimeBucketId() != that.getTimeBucketId()) return false;
        if (getWeekDay() != that.getWeekDay()) return false;
        if (getStoreId() != that.getStoreId()) return false;
        if (getMerchantId() != that.getMerchantId()) return false;
        return getCreateTime() == that.getCreateTime();

    }

    @Override
    public String toString() {
        return "StorePromotionReducePeriod{" +
                "promotionReduceId=" + promotionReduceId +
                ", timeBucketId=" + timeBucketId +
                ", weekDay=" + weekDay +
                ", storeId=" + storeId +
                ", merchantId=" + merchantId +
                ", createTime=" + createTime +
                ", storeTimeBucket=" + storeTimeBucket +
                '}';
    }
}