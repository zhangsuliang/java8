package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.thrift.serialize.ThriftField;

/**
 * 满减活动收费项目白名单设置表
 */
@Table(name = "tb_store_promotion_reduce_charge_item", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionReduceChargeItem extends BaseEntity {

    /**
     * 满减活动ID
     */
    @ThriftField(1)
    @Id
    @Column("promotion_reduce_id")
    private long promotionReduceId;

    /**
     * 收费项目ID
     */
    @ThriftField(2)
    @Id(1)
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 店铺ID
     */
    @ThriftField(3)
    @Column("store_id")
    private long storeId;

    /**
     * 商户ID
     */
    @ThriftField(4)
    @Column("merchant_id")
    private int merchantId;


    /**
     * 创建时间
     */
    @ThriftField(5)
    @Column("create_time")
    private long createTime;

    private StoreChargeItem storeChargeItem;

    public String getChargeItemName() {
        if (this.storeChargeItem != null) {
            return this.storeChargeItem.getName();
        }
        return null;
    }

    public StoreChargeItem getStoreChargeItem() {
        return storeChargeItem;
    }

    public void setStoreChargeItem(StoreChargeItem storeChargeItem) {
        this.storeChargeItem = storeChargeItem;
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

    public long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(long chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean equalsData(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorePromotionReduceChargeItem)) return false;

        StorePromotionReduceChargeItem that = (StorePromotionReduceChargeItem) o;

        if (getPromotionReduceId() != that.getPromotionReduceId()) return false;
        if (getChargeItemId() != that.getChargeItemId()) return false;
        if (getStoreId() != that.getStoreId()) return false;
        if (getMerchantId() != that.getMerchantId()) return false;
        return getCreateTime() == that.getCreateTime();
    }

    @Override
    public String toString() {
        return "StorePromotionReduceChargeItem{" +
                "promotionReduceId=" + promotionReduceId +
                ", chargeItemId=" + chargeItemId +
                ", storeId=" + storeId +
                ", merchantId=" + merchantId +
                ", createTime=" + createTime +
                ", storeChargeItem=" + storeChargeItem +
                '}';
    }
}