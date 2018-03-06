package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.dbrouter.StoreChargeItemWeekBackDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * 店铺收费项目、营业时段、周期关系
 */
@Table(name = "tb_store_charge_item_week_back", dalParser = StoreChargeItemWeekBackDbRouter.class)
public class StoreChargeItemWeekBack extends AbsEntity {
	/**
	 * 周期关系id
	 */
	@Id
	@Column("item_week_id")
	private long itemWeekId;

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
	 * 收费项目id
	 */
	@Column("charge_item_id")
	private long chargeItemId;

	/**
	 * 营业时段id
	 */
	@Column("time_bucket_id")
	private long timeBucketId;

	/**
	 * 周期 (周一,周二, ....,周日)
	 */
	@Column("week_day")
	private int weekDay;

	/**
	 * 创建时间
	 */
	@Column("create_time")
	private long createTime;

	/**
	 * 有效期开始时间
	 */
	@Column("begin_time")
	private long beginTime;

	/**
	 * 有效期结束时间
	 */
	@Column("end_time")
	private long endTime;

	public long getItemWeekId() {
		return itemWeekId;
	}

	public void setItemWeekId(long itemWeekId) {
		this.itemWeekId = itemWeekId;
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

	public long getChargeItemId() {
		return chargeItemId;
	}

	public void setChargeItemId(long chargeItemId) {
		this.chargeItemId = chargeItemId;
	}

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}

	public int getWeekDay() {
		return weekDay;
	}

	public void setWeekDay(int weekDay) {
		this.weekDay = weekDay;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}