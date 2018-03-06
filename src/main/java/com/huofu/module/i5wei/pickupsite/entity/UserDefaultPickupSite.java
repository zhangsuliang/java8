package com.huofu.module.i5wei.pickupsite.entity;

import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Table;

/**
 * Created by taoming on 2016/12/6.
 */
@Table(name = "tb_store_pickup_site_user_default", dalParser = BaseDefaultStoreDbRouter.class)
public class UserDefaultPickupSite {

	private static final long serialVersionUID = -1465390725917858230L;

	/**
	 * 用户id
	 */
	@Column("user_id")
	private long userId;


	/**
	 * 时段ID
	 */
	@Column("time_bucket_id")
	private long timeBucketId;


	/**
	 * 自提点ID
	 */
	@Column("pickup_site_id")
	private long pickupSiteId;


	/**
	 * 新增时间
	 */
	@Column("create_time")
	private long createTime;


	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}

	public long getPickupSiteId() {
		return pickupSiteId;
	}

	public void setPickupSiteId(long pickupSiteId) {
		this.pickupSiteId = pickupSiteId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
}
