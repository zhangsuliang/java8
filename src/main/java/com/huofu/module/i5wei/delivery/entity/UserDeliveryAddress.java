package com.huofu.module.i5wei.delivery.entity;

import com.huofu.module.i5wei.delivery.dbrouter.NoDistributeDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

/**
 * Auto created by i5weitools
 * 用户外送地址
 */
@Table(name = "tb_user_delivery_address", dalParser = NoDistributeDbRouter.class)
public class UserDeliveryAddress extends BaseEntity {

    /**
     * 地址id
     */
    @Id
    @Column("address_id")
    private long addressId;

    /**
     * 用户id
     */
    @Column("user_id")
    private long userId;

    /**
     * 详细外送地址
     */
    @Column("address")
    private String address;

    /**
     * 联系人名称
     */
    @Column("contact_name")
    private String contactName;

    /**
     * 联系电话
     */
    @Column("contact_phone")
    private String contactPhone;

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
     * 外送楼宇
     */
    @Column("building_id")
    private long buildingId;

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
     * 最后使用时间
     */
    @Column("last_used_time")
    private long lastUsedTime;

    private StoreDeliveryBuilding storeDeliveryBuilding;

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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

    public long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(long buildingId) {
        this.buildingId = buildingId;
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

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public StoreDeliveryBuilding getStoreDeliveryBuilding() {
        return storeDeliveryBuilding;
    }

    public void setStoreDeliveryBuilding(StoreDeliveryBuilding storeDeliveryBuilding) {
        this.storeDeliveryBuilding = storeDeliveryBuilding;
    }

    public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        this.lastUsedTime = this.createTime;
    }
}