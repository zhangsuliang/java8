package com.huofu.module.i5wei.order.entity;

import huofucore.facade.i5wei.order.StoreOrderPayResultOfDynamicPayMethod;

import java.util.List;

public class StoreOrderActualPayResult {

	/**
	 * 订单ID
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
	 * 订单折扣前的总金额
	 */
	private long orderPrice;
	/**
	 * 订单的实际支付金额，不包含各种折扣，不包含优惠
	 */
	private long actualPrice;
	/**
	 * 外送费
	 */
	private long deliveryFee;
	/**
	 * 台位费
	 */
	private long tableFee;
	/**
	 * 优惠额度（订单优惠+自定义券支付优惠）
	 */
	private long favourableDerate;
	/**
	 * 自助下单折扣
	 */
	private long internetRebatePrice;
	/**
	 * 企业折扣
	 */
	private long enterpriseRebatePrice;
	/**
	 * 会员价折扣
	 */
	private long memberRebatePrice;
	/**
	 * 整单减免
	 */
	private long totalDerate;
	/**
	 * 优惠券抵扣
	 */
	private long couponAmount;
	/**
	 * 自定义券支付名称
	 */
	private String dynamicPayMethodName;
	/**
	 * 自定义券支付优惠
	 */
	private long dynamicPayDerate;
	/**
	 * 现金收款金额
	 */
	private long cashReceivedAmount;
	/**
	 * 现金支付金额
	 */
	private long cashAmount;
	/**
	 * 现金找零
	 */
	private long cashReturnAmount;
	/**
	 * 菜品总价
	 */
	private long chargeItemPrice;
	/**
	 * 单品折扣总共优惠
	 */
	private long promotionPrice;
	
	/**
	 * 整单减免调整
	 */
	private long totalDerate2;
	
	/**
     * 折扣活动减免金额
     */
    private long promotionRebatePrice;
    
    /**
     * 满减活动减免金额
     */
    private long promotionReducePrice;

	/**
	 * 多张自定义券集合,之后自定义券相关信息都从这个集合中获取
	 * 为了兼容旧版本,之前单张的券信息依旧保留
	 */
	private List<StoreOrderPayResultOfDynamicPayMethod> storeOrderPayResultOfDynamicPayMethodList;

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

	public long getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(long orderPrice) {
		this.orderPrice = orderPrice;
	}

	public long getActualPrice() {
		return actualPrice;
	}

	public void setActualPrice(long actualPrice) {
		this.actualPrice = actualPrice;
	}

	public long getDeliveryFee() {
		return deliveryFee;
	}

	public void setDeliveryFee(long deliveryFee) {
		this.deliveryFee = deliveryFee;
	}

	public long getTableFee() {
		return tableFee;
	}

	public void setTableFee(long tableFee) {
		this.tableFee = tableFee;
	}

	public long getFavourableDerate() {
		return favourableDerate;
	}

	public void setFavourableDerate(long favourableDerate) {
		this.favourableDerate = favourableDerate;
	}

	public long getInternetRebatePrice() {
		return internetRebatePrice;
	}

	public void setInternetRebatePrice(long internetRebatePrice) {
		this.internetRebatePrice = internetRebatePrice;
	}

	public long getEnterpriseRebatePrice() {
		return enterpriseRebatePrice;
	}

	public void setEnterpriseRebatePrice(long enterpriseRebatePrice) {
		this.enterpriseRebatePrice = enterpriseRebatePrice;
	}

	public long getMemberRebatePrice() {
		return memberRebatePrice;
	}

	public void setMemberRebatePrice(long memberRebatePrice) {
		this.memberRebatePrice = memberRebatePrice;
	}

	public long getTotalDerate() {
		return totalDerate;
	}

	public void setTotalDerate(long totalDerate) {
		this.totalDerate = totalDerate;
	}

	public long getCouponAmount() {
		return couponAmount;
	}

	public void setCouponAmount(long couponAmount) {
		this.couponAmount = couponAmount;
	}

	public String getDynamicPayMethodName() {
		return dynamicPayMethodName;
	}

	public void setDynamicPayMethodName(String dynamicPayMethodName) {
		this.dynamicPayMethodName = dynamicPayMethodName;
	}

	public long getDynamicPayDerate() {
		return dynamicPayDerate;
	}

	public void setDynamicPayDerate(long dynamicPayDerate) {
		this.dynamicPayDerate = dynamicPayDerate;
	}

	public long getCashReceivedAmount() {
		return cashReceivedAmount;
	}

	public void setCashReceivedAmount(long cashReceivedAmount) {
		this.cashReceivedAmount = cashReceivedAmount;
	}

	public long getCashAmount() {
		return cashAmount;
	}

	public void setCashAmount(long cashAmount) {
		this.cashAmount = cashAmount;
	}

	public long getCashReturnAmount() {
		return cashReturnAmount;
	}

	public void setCashReturnAmount(long cashReturnAmount) {
		this.cashReturnAmount = cashReturnAmount;
	}

	public long getChargeItemPrice() {
		return chargeItemPrice;
	}

	public void setChargeItemPrice(long chargeItemPrice) {
		this.chargeItemPrice = chargeItemPrice;
	}

	public long getPromotionPrice() {
		return promotionPrice;
	}

	public void setPromotionPrice(long promotionPrice) {
		this.promotionPrice = promotionPrice;
	}

	public long getTotalDerate2() {
		return totalDerate2;
	}

	public void setTotalDerate2(long totalDerate2) {
		this.totalDerate2 = totalDerate2;
	}

	public long getPromotionRebatePrice() {
		return promotionRebatePrice;
	}

	public void setPromotionRebatePrice(long promotionRebatePrice) {
		this.promotionRebatePrice = promotionRebatePrice;
	}

	public long getPromotionReducePrice() {
		return promotionReducePrice;
	}

	public void setPromotionReducePrice(long promotionReducePrice) {
		this.promotionReducePrice = promotionReducePrice;
	}

	public List<StoreOrderPayResultOfDynamicPayMethod> getStoreOrderPayResultOfDynamicPayMethodList() {
		return storeOrderPayResultOfDynamicPayMethodList;
	}

	public void setStoreOrderPayResultOfDynamicPayMethodList(List<StoreOrderPayResultOfDynamicPayMethod> storeOrderPayResultOfDynamicPayMethodList) {
		this.storeOrderPayResultOfDynamicPayMethodList = storeOrderPayResultOfDynamicPayMethodList;
	}

	/**
	 * 获取自定义券的折扣总金额
	 */
	public long getDynamicPayAllDerate() {
		if (this.storeOrderPayResultOfDynamicPayMethodList == null || this.storeOrderPayResultOfDynamicPayMethodList.isEmpty()) {
			return 0;
		}
		long dynamicPayDerate = 0;
		for (StoreOrderPayResultOfDynamicPayMethod dynamicPayMethod : this.storeOrderPayResultOfDynamicPayMethodList) {
			dynamicPayDerate += dynamicPayMethod.getAmount() - dynamicPayMethod.getActualAmount();
		}
		return dynamicPayDerate;
	}
}
