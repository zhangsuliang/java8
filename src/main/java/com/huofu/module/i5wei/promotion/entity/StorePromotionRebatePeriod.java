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
 * 折扣活动周期表
 */
@Table(name = "tb_store_promotion_rebate_period", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionRebatePeriod extends BaseEntity {

    /**
     * 活动ID
     */
    @ThriftField(1)
    @Id
    @Column("promotion_rebate_id")
    private long promotionRebateId;

    /**
     * 星期
     */
    @ThriftField(2)
    @Id(1)
    @Column("week_day")
    private int weekDay;

    /**
     * 营业时段ID
     */
    @ThriftField(3)
    @Id(2)
    @Column("time_bucket_id")
    private long timeBucketId;

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

    public long getPromotionRebateId() {
        return promotionRebateId;
    }

    public void setPromotionRebateId(long promotionRebateId) {
        this.promotionRebateId = promotionRebateId;
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

    @Override
    public String toString() {
        return "StorePromotionRebatePeriod{" +
                "promotionRebateId=" + promotionRebateId +
                ", weekDay=" + weekDay +
                ", timeBucketId=" + timeBucketId +
                ", storeId=" + storeId +
                ", merchantId=" + merchantId +
                ", createTime=" + createTime +
                ", storeTimeBucket=" + storeTimeBucket +
                '}';
    }

    public boolean equalsData(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorePromotionRebatePeriod)) return false;

        StorePromotionRebatePeriod that = (StorePromotionRebatePeriod) o;

        if (getPromotionRebateId() != that.getPromotionRebateId()) return false;
        if (getWeekDay() != that.getWeekDay()) return false;
        if (getTimeBucketId() != that.getTimeBucketId()) return false;
        if (getStoreId() != that.getStoreId()) return false;
        if (getMerchantId() != that.getMerchantId()) return false;
        return getCreateTime() == that.getCreateTime();

    }
}