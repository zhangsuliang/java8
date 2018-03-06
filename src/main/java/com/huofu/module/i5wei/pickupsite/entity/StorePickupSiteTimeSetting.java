package com.huofu.module.i5wei.pickupsite.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.pickupsite.dbrouter.StorePickupSiteTimeSettingDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Created by taoming on 2016/12/2.
 */
@Table(name = "tb_store_pickup_site_time_setting", dalParser = StorePickupSiteTimeSettingDbRouter.class)
public class StorePickupSiteTimeSetting extends AbsEntity {

	private static final long serialVersionUID = -1465396725917858230L;


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
	 * 营业时间ID
	 */
	@Id(1)
	@Column("time_bucket_id")
	private long timeBucketId;


	/**
	 * 自提点ID
	 */
	@Id
	@Column("pickup_site_id")
	private long storePickupSiteId;


	/**
	 * 取餐时间
	 */
	@Column("pickup_time")
	private long pickupTime;

	/**
	 * 订餐截止时间
	 */
	@Column("order_cutoff_time")
	private long orderCutOffTime;

	/**
	 * 是否启用
	 */
	@Column("disabled")
	private boolean disabled;

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

	public long getStorePickupSiteId() {
		return storePickupSiteId;
	}

	public void setStorePickupSiteId(long storePickupSiteId) {
		this.storePickupSiteId = storePickupSiteId;
	}

	public long getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(long pickupTime) {
		this.pickupTime = pickupTime;
	}

	public long getOrderCutOffTime() {
		return orderCutOffTime;
	}

	public void setOrderCutOffTime(long orderCutOffTime) {
		this.orderCutOffTime = orderCutOffTime;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
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
}
