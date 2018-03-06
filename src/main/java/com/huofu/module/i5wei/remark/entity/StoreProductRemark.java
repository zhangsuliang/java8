package com.huofu.module.i5wei.remark.entity;

import com.huofu.module.i5wei.remark.dbrouter.StoreProductRemarkDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

/**
 * Auto created by i5weitools
 * 店铺菜品常用备注
 */
@Table(name = "tb_store_product_remark", dalParser =
        StoreProductRemarkDbRouter.class)
public class StoreProductRemark extends BaseEntity {

    /**
     * 店铺id
     */
    @Id(0)
    @Column("store_id")
    private long storeId;

    /**
     * 备注
     */
    @Id(1)
    @Column("remark")
    private String remark;

    /**
     * 商户id
     */
    @Column("merchant_id")
    private int merchantId;

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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
    }
}
