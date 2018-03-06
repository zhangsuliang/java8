package com.huofu.module.i5wei.pickupsite.service;

import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteOpenTypeEnum;

import java.util.List;

public class StorePickupSiteSaveOrUpdateParam {

	/**
	 * 自提点ID
	 */
	public long storePickupSiteId;
	/**
	 * 自提点名称
	 */
	public String storePickupSiteName;
	/**
	 * 自提点地址
	 */
	public String storePickupSiteAddress;
	/**
	 * 自提点类型
	 *
	 * @see StorePickupSiteOpenTypeEnum
	 */
	public StorePickupSiteOpenTypeEnum storePickupSiteOpenType;
	/**
	 * * 协议企业id集合
	 * *
	 */
	public List<Long> enterpriseIds;
	/**
	 * * 开放时段
	 * *
	 */
	public List<StorePickupSiteTimeSetting> storePickupSiteTimeSetting;
	/**
	 * * 自提点状态 0=启用，1=不启用
	 * *
	 */
	public Boolean disabled;
	/**
	 * 商户ID
	 */
	public int merchantId;
	/**
	 * 店铺ID
	 */
	public long storeId;

	public long getStorePickupSiteId() {
		return storePickupSiteId;
	}

	public void setStorePickupSiteId(long storePickupSiteId) {
		this.storePickupSiteId = storePickupSiteId;
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

	public StorePickupSiteOpenTypeEnum getStorePickupSiteOpenType() {
		return storePickupSiteOpenType;
	}

	public void setStorePickupSiteOpenType(StorePickupSiteOpenTypeEnum storePickupSiteOpenType) {
		this.storePickupSiteOpenType = storePickupSiteOpenType;
	}

	public List<Long> getEnterpriseIds() {
		return enterpriseIds;
	}

	public void setEnterpriseIds(List<Long> enterpriseIds) {
		this.enterpriseIds = enterpriseIds;
	}

	public List<StorePickupSiteTimeSetting> getStorePickupSiteTimeSetting() {
		return storePickupSiteTimeSetting;
	}

	public void setStorePickupSiteTimeSetting(List<StorePickupSiteTimeSetting> storePickupSiteTimeSetting) {
		this.storePickupSiteTimeSetting = storePickupSiteTimeSetting;
	}

	public Boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
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
}
