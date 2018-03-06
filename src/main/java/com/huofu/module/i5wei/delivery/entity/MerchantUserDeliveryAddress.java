package com.huofu.module.i5wei.delivery.entity;

import com.huofu.module.i5wei.delivery.dbrouter.MerchantUserDeliveryAddressDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

@Table(name = "tb_merchant_user_delivery_address", dalParser = MerchantUserDeliveryAddressDbRouter.class)
public class MerchantUserDeliveryAddress extends BaseEntity{
	/**
	 * 主键
	 */
	@Id
	@Column("address_id")
	private long addressId;
	
	/**
	 * 商户Id
	 */
	@Column("merchant_id")
	private int merchantId;
	
	/**
	 * 用户Id
	 */
	@Column("user_id")
	private long userId;
	
	/**
	 * 用户外送楼宇地址
	 */
	@Column("building_address")
	private String buildingAddress;

	/**
	 * 用户外送楼宇地址
	 */
	@Column("building_name")
	private String buildingName;
	
	/**
	 * 用户外送详细地址
	 */
	@Column("user_address")
	private String userAddress;
	
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
	 * 用户外送地址的经度
	 */
	@Column("user_address_longitude")
	private double userAddressLongitude;
	
	/**
	 * 用户外送地址的纬度
	 */
	@Column("user_address_latitude")
	private double userAddressLatitude;
	
	/**
	 * 创建时间
	 */
	@Column("create_time")
	private long createTime;
	
	/**
	 * 修改时间
	 */
	@Column("update_time")
	private long updateTime;
	
	/**
	 * 删除标识
	 */
	@Column("deleted")
	private int deleted;

	public long getAddressId() {
		return addressId;
	}

	public void setAddressId(long addressId) {
		this.addressId = addressId;
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public String getBuildingName() {
		return buildingName;
	}

	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}

	public String getBuildingAddress() {
		return buildingAddress;
	}

	public void setBuildingAddress(String buildingAddress) {
		this.buildingAddress = buildingAddress;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
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

	public int getDeleted() {
		return deleted;
	}

	public void setDeleted(int deleted) {
		this.deleted = deleted;
	}
}
