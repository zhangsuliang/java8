package com.huofu.module.i5wei.table.service;

import huofucore.facade.i5wei.order.StoreOrderPayResultOfDynamicPayMethod;

import java.util.List;

/**
 * 用于计算桌台记录优惠合计
 *
 * @author licheng7
 *         2016年6月6日 下午6:14:27
 */
public class TableRecordPayDetailResult {
	/**
	 * 交易订单
	 */
	private String orderId;
	/**
	 * 支付订单
	 */
	private String payOrderId;
	/**
	 * 现金支付金额
	 */
	private long cashAmount; // required
	/**
	 * 现金实收金额
	 */
	private long cashReceivedAmount; // required
	/**
	 * 优惠券支付金额
	 */
	private long couponAmount; // required
	/**
	 * 预付费卡支付金额
	 */
	private long prepaidcardAmount; // required
	/**
	 * 用户账户支付金额
	 */
	private long userAccountAmount; // required
	/**
	 * YJPay支付金额
	 */
	private long yjpayAmount; // required
	/**
	 * Wechat支付金额
	 */
	private long wechatAmount; // required
	/**
	 * IPOS支付金额
	 */
	private long iposAmount; // required
	/**
	 * 普通POS支付金额
	 */
	private long posAmount; // required
	/**
	 * 对公转账支付金额
	 */
	private long publicTransferAmount; // required
	/**
	 * 自定义券支付id
	 */
	private int dynamicPayMethodId; // required
	/**
	 * 自定义券支付名称
	 */
	private String dynamicPayMethodName; // required
	/**
	 * 自定义全支付金额
	 */
	private long dynamicPayMethodAmount; // required
	/**
	 * 实际购券金额
	 */
	private long dynamicPayMethodActualAmount; // required
	/**
	 * 支付宝支付金额
	 */
	private long aliPayAmount; // required
	/**
	 * 盒子支付支付金额
	 */
	private long iboxPayAmount; // required
	/**
	 * 赊账金额
	 */
	private long creditAmount;
	/**
	 * 订单原价
	 */
	private long orderPrice = 0L;
	/**
	 * 台位费
	 */
	private long tableFee = 0L;
	/**
	 * 自定义券支付优惠
	 */
	private long dynamicPayDerate = 0L;
	/**
	 * 自助下单折扣
	 */
	private long internetRebatePrice = 0L;
	/**
	 * 企业折扣
	 */
	private long enterpriseRebatePrice = 0L;
	/**
	 * 会员价折扣
	 */
	private long memberRebatePrice = 0L;
	/**
	 * 整单减免+整单折扣
	 */
	private long totalDerate = 0L;
	/**
	 * 折扣活动减免
	 */
	private long promotionRebatePrice = 0L;
	/**
	 * 满减活动减免
	 */
	private long promotionReducePrice = 0L;
	/**
	 * 赠菜免单金额
	 */
	private long gratisPrice = 0L;
	/**
	 * 单品折扣总共优惠
	 */
	private long promotionPrice = 0L;
	
	public long getCashAmount() {
		return cashAmount;
	}
	
	public void setCashAmount(long cashAmount) {
		this.cashAmount = cashAmount;
	}
	
	public long getCashReceivedAmount() {
		return cashReceivedAmount;
	}
	
	public void setCashReceivedAmount(long cashReceivedAmount) {
		this.cashReceivedAmount = cashReceivedAmount;
	}
	
	public long getCouponAmount() {
		return couponAmount;
	}
	
	public void setCouponAmount(long couponAmount) {
		this.couponAmount = couponAmount;
	}
	
	public long getPrepaidcardAmount() {
		return prepaidcardAmount;
	}
	
	public void setPrepaidcardAmount(long prepaidcardAmount) {
		this.prepaidcardAmount = prepaidcardAmount;
	}
	
	public long getUserAccountAmount() {
		return userAccountAmount;
	}
	
	public void setUserAccountAmount(long userAccountAmount) {
		this.userAccountAmount = userAccountAmount;
	}
	
	public long getYjpayAmount() {
		return yjpayAmount;
	}
	
	public void setYjpayAmount(long yjpayAmount) {
		this.yjpayAmount = yjpayAmount;
	}
	
	public long getWechatAmount() {
		return wechatAmount;
	}
	
	public void setWechatAmount(long wechatAmount) {
		this.wechatAmount = wechatAmount;
	}
	
	public long getIposAmount() {
		return iposAmount;
	}
	
	public void setIposAmount(long iposAmount) {
		this.iposAmount = iposAmount;
	}
	
	public long getPosAmount() {
		return posAmount;
	}
	
	public void setPosAmount(long posAmount) {
		this.posAmount = posAmount;
	}
	
	public long getPublicTransferAmount() {
		return publicTransferAmount;
	}
	
	public void setPublicTransferAmount(long publicTransferAmount) {
		this.publicTransferAmount = publicTransferAmount;
	}
	
	public long getAliPayAmount() {
		return aliPayAmount;
	}
	
	public void setAliPayAmount(long aliPayAmount) {
		this.aliPayAmount = aliPayAmount;
	}
	
	public long getIboxPayAmount() {
		return iboxPayAmount;
	}
	
	public void setIboxPayAmount(long iboxPayAmount) {
		this.iboxPayAmount = iboxPayAmount;
	}
	
	public long getOrderPrice() {
		return orderPrice;
	}
	
	public void setOrderPrice(long orderPrice) {
		this.orderPrice = orderPrice;
	}
	
	public long getTableFee() {
		return tableFee;
	}
	
	public void setTableFee(long tableFee) {
		this.tableFee = tableFee;
	}
	
	public long getDynamicPayDerate() {
		return dynamicPayDerate;
	}
	
	public void setDynamicPayDerate(long dynamicPayDerate) {
		this.dynamicPayDerate = dynamicPayDerate;
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

    /**
     * 自定义券集合, 之后获取券相关信息都从该集合获取,
     * 为了兼容旧版本,依旧保留单张券相关信息
     */
    private List<StoreOrderPayResultOfDynamicPayMethod> storeOrderPayResultOfDynamicPayMethodList;

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

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayOrderId() {
		return payOrderId;
	}

	public void setPayOrderId(String payOrderId) {
		this.payOrderId = payOrderId;
	}

	public int getDynamicPayMethodId() {
		return dynamicPayMethodId;
	}

	public void setDynamicPayMethodId(int dynamicPayMethodId) {
		this.dynamicPayMethodId = dynamicPayMethodId;
	}

	public String getDynamicPayMethodName() {
		return dynamicPayMethodName;
	}

	public void setDynamicPayMethodName(String dynamicPayMethodName) {
		this.dynamicPayMethodName = dynamicPayMethodName;
	}

	public long getDynamicPayMethodAmount() {
		return dynamicPayMethodAmount;
	}

	public void setDynamicPayMethodAmount(long dynamicPayMethodAmount) {
		this.dynamicPayMethodAmount = dynamicPayMethodAmount;
	}

	public long getDynamicPayMethodActualAmount() {
		return dynamicPayMethodActualAmount;
	}

	public void setDynamicPayMethodActualAmount(long dynamicPayMethodActualAmount) {
		this.dynamicPayMethodActualAmount = dynamicPayMethodActualAmount;
	}

	public long getCreditAmount() {
		return creditAmount;
	}

	public void setCreditAmount(long creditAmount) {
		this.creditAmount = creditAmount;
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

	public long getGratisPrice() {
		return gratisPrice;
	}

	public void setGratisPrice(long gratisPrice) {
		this.gratisPrice = gratisPrice;
	}

	public long getPromotionPrice() {
		return promotionPrice;
	}

	public void setPromotionPrice(long promotionPrice) {
		this.promotionPrice = promotionPrice;
	}

	public List<StoreOrderPayResultOfDynamicPayMethod> getStoreOrderPayResultOfDynamicPayMethodList() {
		return storeOrderPayResultOfDynamicPayMethodList;
	}

	public void setStoreOrderPayResultOfDynamicPayMethodList(
			List<StoreOrderPayResultOfDynamicPayMethod> storeOrderPayResultOfDynamicPayMethodList) {
		this.storeOrderPayResultOfDynamicPayMethodList = storeOrderPayResultOfDynamicPayMethodList;
	}

}
