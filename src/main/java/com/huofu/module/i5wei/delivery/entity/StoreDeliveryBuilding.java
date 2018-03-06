package com.huofu.module.i5wei.delivery.entity;

import com.huofu.module.i5wei.delivery.dbrouter.NoDistributeDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

/**
 * Auto created by i5weitools
 * 外送楼宇
 */
@Table(name = "tb_store_delivery_building", dalParser = NoDistributeDbRouter.class)
public class StoreDeliveryBuilding extends BaseEntity {

    /**
     * 楼宇id
     */
    @Id
    @Column("building_id")
    private long buildingId;

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
     * 外送楼宇名称
     */
    @Column("name")
    private String name;

    /**
     * 外送地址
     */
    @Column("address")
    private String address;

    /**
     * 是否删除  0:否 1:是 #bool
     */
    @Column("deleted")
    private boolean deleted;

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

    public long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(long buildingId) {
        this.buildingId = buildingId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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
        this.deleted = false;
    }

    public void makeDeleted() {
        this.snapshot();
        this.setDeleted(true);
        this.setUpdateTime(System.currentTimeMillis());
        this.update();
    }
}