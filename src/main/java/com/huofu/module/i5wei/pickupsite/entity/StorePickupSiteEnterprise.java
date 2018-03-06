package com.huofu.module.i5wei.pickupsite.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.pickupsite.dbrouter.StorePickupSiteEnterpriseDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Table;

/**
 * Created by taoming on 2016/12/2.
 */
@Table(name = "tb_store_pickup_site_enterprise", dalParser = StorePickupSiteEnterpriseDbRouter.class)
public class StorePickupSiteEnterprise extends AbsEntity {

	private static final long serialVersionUID = -1465396725917858330L;

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
	 * 自提点ID
	 */
	@Column("pickup_site_id")
	private long pickupSiteId;


	/**
	 * 协议企业id
	 */
	@Column("enterprise_id")
	private long enterpriseId;


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

	public long getPickupSiteId() {
		return pickupSiteId;
	}

	public void setPickupSiteId(long pickupSiteId) {
		this.pickupSiteId = pickupSiteId;
	}

	public long getEnterpriseId() {
		return enterpriseId;
	}

	public void setEnterpriseId(long enterpriseId) {
		this.enterpriseId = enterpriseId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
}
