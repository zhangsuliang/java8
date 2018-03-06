package com.huofu.module.i5wei.pickupsite.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.pickupsite.dbrouter.StorePickupSiteDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteOpenTypeEnum;

/**
 * Created by taoming on 2016/12/2.
 */
@Table(name = "tb_store_pickup_site", dalParser = StorePickupSiteDbRouter.class)
public class StorePickupSite extends AbsEntity {

	private static final long serialVersionUID = -1465396755918858230L;

	/**
	 * 自提点ID（主键，全库唯一）
	 */
	@Id
	@Column("pickup_site_id")
	private long storePickupSiteId;

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
	 * 名称
	 */
	@Column("site_name")
	private String storePickupSiteName;

	/**
	 * 地址
	 */
	@Column("site_address")
	private String storePickupSiteAddress;

	/**
	 * 开放用户类型
	 */
	@Column("site_open_type")
	private int siteOpenType = StorePickupSiteOpenTypeEnum.ALL_ORIENTED.getValue();

	/**
	 * 是否启用
	 */
	@Column("disabled")
	private boolean disabled = true;

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

	public long getStorePickupSiteId() {
		return storePickupSiteId;
	}

	public void setStorePickupSiteId(long storePickupSiteId) {
		this.storePickupSiteId = storePickupSiteId;
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

	public String getStorePickupSiteName() {
		return storePickupSiteName;
	}

	public void setStorePickupSiteName(String storePickupSiteName) {
		this.storePickupSiteName = storePickupSiteName;
	}

	public String getStorePickupSiteAddress() {
		return storePickupSiteAddress;
	}

	public void setStorePickupSiteAddress(String storePickupSiteAddress) {
		this.storePickupSiteAddress = storePickupSiteAddress;
	}

	public int getSiteOpenType() {
		return siteOpenType;
	}

	public void setSiteOpenType(int siteOpenType) {
		this.siteOpenType = siteOpenType;
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
