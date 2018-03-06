package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.dbrouter.StoreChargeSubitemDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 收费项目子项目
 */
@Table(name = "tb_store_charge_subitem", dalParser = StoreChargeSubitemDbRouter.class)
public class StoreChargeSubitem extends AbsEntity {

    /**
     * 自增id
     */
    @Id
    @Column("tid")
    private long tid;

    /**
     * 收费项目id
     */
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 商户id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺id
     */
    @Column("store_id")
    private long storeId;

    /**
     * 产品id
     */
    @Column("product_id")
    private long productId;

    /**
     * 数量
     */
    @Column("amount")
    private double amount;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * #bool 0:未删除  1:已删除
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * #bool 是否是主菜 0:不是 1:是
     */
    @Column("main_flag")
    private boolean mainFlag;

    public boolean isMainFlag() {
        return mainFlag;
    }

    public void setMainFlag(boolean mainFlag) {
        this.mainFlag = mainFlag;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(long chargeItemId) {
        this.chargeItemId = chargeItemId;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    private StoreProduct storeProduct;

    public StoreProduct getStoreProduct() {
        return storeProduct;
    }

    public void setStoreProduct(StoreProduct storeProduct) {
        this.storeProduct = storeProduct;
    }

    public void initForCreate(long now) {
        this.deleted = false;
        this.setCreateTime(now);
        this.setUpdateTime(now);
    }

    public void changeAmount(double amout) {
        if (amout <= 0) {
            return;
        }
        this.snapshot();
        this.amount = amout;
        this.updateTime = System.currentTimeMillis();
    }

    public boolean isSame(StoreChargeSubitem storeChargeSubitem) {
        if (this.productId == storeChargeSubitem.getProductId()) {
            return true;
        }
        return false;
    }
}