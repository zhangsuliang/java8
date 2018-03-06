package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.thrift.serialize.ThriftField;

/**
 * 满减活动额度设置表
 */
@Table(name = "tb_store_promotion_reduce_quota", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionReduceQuota extends BaseEntity {

    /**
     * 满减活动ID
     */
    @ThriftField(1)
    @Id
    @Column("promotion_reduce_id")
    private long promotionReduceId;

    /**
     * 最低消费金额
     */
    @ThriftField(2)
    @Id(1)
    @Column("quota_price")
    private long quotaPrice;

    /**
     * 优惠减免金额
     */
    @ThriftField(3)
    @Id(2)
    @Column("reduce_price")
    private long reducePrice;

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
     * 最后更新时间
     */
    @ThriftField(6)
    @Column("update_time")
    private long updateTime;

    /**
     * 创建时间
     */
    @ThriftField(7)
    @Column("create_time")
    private long createTime;

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

    public long getQuotaPrice() {
        return quotaPrice;
    }

    public void setQuotaPrice(long quotaPrice) {
        this.quotaPrice = quotaPrice;
    }

    public long getReducePrice() {
        return reducePrice;
    }

    public void setReducePrice(long reducePrice) {
        this.reducePrice = reducePrice;
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

    public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
    }

    /**
     * 是否可以使用此满减项目
     *
     * @param amount 消费金额
     * @return true:可以使用
     */
    public boolean canUse(long amount) {
        if (amount >= this.quotaPrice) {
            return true;
        }
        return false;
    }

    public long getReduceAmount(long amount) {
        if (amount >= this.quotaPrice) {
            long result = amount - this.reducePrice;
            if (result < 0) {
                result = 0;
            }
            return result;
        }
        return amount;
    }

    public boolean equalsData(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorePromotionReduceQuota)) return false;

        StorePromotionReduceQuota that = (StorePromotionReduceQuota) o;

        if (getPromotionReduceId() != that.getPromotionReduceId()) return false;
        if (getQuotaPrice() != that.getQuotaPrice()) return false;
        if (getReducePrice() != that.getReducePrice()) return false;
        if (getStoreId() != that.getStoreId()) return false;
        if (getMerchantId() != that.getMerchantId()) return false;
        if (getUpdateTime() != that.getUpdateTime()) return false;
        return getCreateTime() == that.getCreateTime();

    }

    @Override
    public String toString() {
        return "StorePromotionReduceQuota{" +
                "promotionReduceId=" + promotionReduceId +
                ", quotaPrice=" + quotaPrice +
                ", reducePrice=" + reducePrice +
                ", storeId=" + storeId +
                ", merchantId=" + merchantId +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}