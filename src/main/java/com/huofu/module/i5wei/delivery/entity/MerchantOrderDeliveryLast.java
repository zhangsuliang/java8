package com.huofu.module.i5wei.delivery.entity;
import com.huofu.module.i5wei.delivery.dbrouter.MerchantOrderDeliveryLastDbRouter;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 
 */
@Table(name = "tb_merchant_order_delivery_last", dalParser = MerchantOrderDeliveryLastDbRouter.class)
public class MerchantOrderDeliveryLast {

    /**
     * 商户Id
     */
	@Id(0)
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺Id
     */
	@Id(1)
    @Column("store_id")
    private long storeId;

    /**
     * 用户Id
     */
	@Id(2)
    @Column("user_id")
    private long userId;

    /**
     * 外送的模式 1：楼宇模式（非距离模式），2：距离模式
     */
    @Column("delivery_mode")
    private int deliveryMode;

    /**
     * 最后一次外卖下单时间
     */
    @Column("last_delivery_time")
    private long lastDeliveryTime;

    /**
     * 用户外送地址的经度，默认为0
     */
    @Column("user_address_longitude")
    private double userAddressLongitude;

    /**
     * 用户外送地址的纬度，默认为0
     */
    @Column("user_address_latitude")
    private double userAddressLatitude;

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

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getDeliveryMode() {
		return deliveryMode;
	}

	public void setDeliveryMode(int deliveryMode) {
		this.deliveryMode = deliveryMode;
	}

	public long getLastDeliveryTime() {
		return lastDeliveryTime;
	}

	public void setLastDeliveryTime(long lastDeliveryTime) {
		this.lastDeliveryTime = lastDeliveryTime;
	}

	public double getUserAddressLongitude() {
		return userAddressLongitude;
	}

	public void setUserAddressLongitude(double userAddressLongitude) {
		this.userAddressLongitude = userAddressLongitude;
	}

	public double getUserAddressLatitude() {
		return userAddressLatitude;
	}

	public void setUserAddressLatitude(double userAddressLatitude) {
		this.userAddressLatitude = userAddressLatitude;
	}
}