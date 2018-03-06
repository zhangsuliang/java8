package com.huofu.module.i5wei.order.entity;

public class StoreOrderPromotion {

	/**
	 * 订单ID（主键，全库唯一）
	 */
	private String orderId;
	/**
	 * 商户ID
	 */
	private int merchantId;
	/**
	 * 店铺ID
	 */
	private long storeId;
	/**
	 * 活动ID
	 */
	private long promotionId;
	/**
	 * 活动类型
	 */
	private int promotionType;
	/**
	 * 活动标题
	 */
	private String promotionTitle;
	/**
	 * 活动减免金额
	 */
	private long promotionDerate;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public long getPromotionId() {
		return promotionId;
	}

	public void setPromotionId(long promotionId) {
		this.promotionId = promotionId;
	}

	public int getPromotionType() {
		return promotionType;
	}

	public void setPromotionType(int promotionType) {
		this.promotionType = promotionType;
	}

	public String getPromotionTitle() {
		return promotionTitle;
	}

	public void setPromotionTitle(String promotionTitle) {
		this.promotionTitle = promotionTitle;
	}

	public long getPromotionDerate() {
		return promotionDerate;
	}

	public void setPromotionDerate(long promotionDerate) {
		this.promotionDerate = promotionDerate;
	}

}
