package com.huofu.module.i5wei.order.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import huofucore.facade.merchant.preferential.EnterpriseRebateType;
import huofuhelper.util.MoneyUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItemPromotion;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceQuota;


/**
 * 订单折扣计算结果，目前整单只能使用一种折扣方式（其中之一：1=网单折扣；2=协议企业折扣；3=整单折扣）
 * 
 * @author chenkai
 * @since 2015-12-29
 */
public class StoreOrderPriceResult {
	
	/**
	 * 菜品总价
	 */
	private long orderItemPrice;
	
	/**
	 * 原价（含打包费、台位费）
	 */
	private long orderPrice;

	/**
	 * 协议企业折扣
	 */
	private double enterpriseRebate;

	/**
	 * 协议企业折扣，订单中多少钱可以按协议企业打折，按订单详情中可以安协议企业打折的部分统计得到
	 */
	private long enterpriseRebateAmount;

	/**
	 * 协议企业折扣打折额度
	 */
	private long enterpriseRebatePrice;

	/**
	 * 网单折扣
	 */
	private double internetRebate;

	/**
	 * 网单折扣限额，订单中多少钱可以按网单打折，按订单详情中可以安网单打折的部分统计得到
	 */
	private long internetRebateAmount;

	/**
	 * 网单打折额度
	 */
	private long internetRebatePrice;
	
    /**
     * 折扣活动参与的金额
     */
    private long promotionRebateAmount;

    /**
     * 折扣活动减免金额
     */
    private long promotionRebatePrice;
    
    /**
     * 满减活动ID
     */
    private long promotionReduceId;
    
    /**
     * 满减活动最低消费金额
     */
    private long promotionReduceQuota;
    
    /**
     * 满减活动参与的金额（所有的）
     */
    private long promotionReduceAmount;
    
    /**
     * 满减活动参与的金额（不共享）
     */
    private long promotionReduceAmountNotShared;
    
    /**
     * 满减活动减免金额
     */
    private long promotionReducePrice;

	/**
	 * 整单折扣比例，可能是活动或者优惠，由店铺单独设置
	 */
	private double totalRebate;

	/**
	 * 整单减免金额，可能是活动或者优惠，由店铺单独设置
	 */
	private long totalDerate;
	
	/**
     * 整单折扣打折额度
     */
    private long totalRebatePrice;

	/**
	 * 总价，按照点餐项目单项打折后，没有任何整单减免与折扣的总价金额（10+20=30）
	 */
	private long totalPrice;
	
	/**
     * 下单用户终端优惠金额，一天只有一单可以优惠
     */
    private long userClientCoupon;

	/**
	 * 在total_price基础上整单减免与折扣的总价金额（30*09=27）
	 */
	private long favorablePrice;

	/**
	 * 折扣类型
	 */
	private int rebateType;
	
	/**
	 * 订单打包费
	 */
	private long packageFee;
	
	/**
	 * 是否会产生打包费
	 */
	private boolean producePackageFee;
	
	/**
	 * 可使用优惠券金额
	 */
	private long orderCouponPrice;
	
	/**
     * 会员价总共优惠
     */
    private long memberRebatePrice;
    
    /**
     * 首份特价总共优惠
     */
    private long promotionPrice;
	
	/**
     * 订单的入客数
     */
    private int customerTraffic;
    
    /**
     * 台位费
     */
    private long tableFee;
	
	/**
	 * 企业折扣类型{@link EnterpriseRebateType}
	 */
	private int enterpriseRebateType;
	
	/**
	 * 订单满减活动
	 */
	private StorePromotionReduce storePromotionReduce;
	
	/**
	 * 订单满减活动限额
	 */
	private StorePromotionReduceQuota storePromotionReduceQuota;
	
	/**
	 * 订单子项促销列表
	 */
	private List<StoreOrderItemPromotion> storeOrderItemPromotions = Lists.newArrayList();
	
	/**
	 * 使用首份特价的菜品定价ID
	 */
	private Set<Long> promotionChargeItemIds = new HashSet<Long>();

	private long gratisPrice;

	public long getGratisPrice() {
		return gratisPrice;
	}

	public void setGratisPrice(long gratisPrice) {
		this.gratisPrice = gratisPrice;
	}

	public long getOrderItemPrice() {
		return orderItemPrice;
	}

	public void setOrderItemPrice(long orderItemPrice) {
		this.orderItemPrice = orderItemPrice;
	}

	public long getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(long orderPrice) {
		this.orderPrice = orderPrice;
	}

	public double getEnterpriseRebate() {
		return enterpriseRebate;
	}

	public void setEnterpriseRebate(double enterpriseRebate) {
		this.enterpriseRebate = enterpriseRebate;
	}

	public long getEnterpriseRebateAmount() {
		return enterpriseRebateAmount;
	}

	public void setEnterpriseRebateAmount(long enterpriseRebateAmount) {
		this.enterpriseRebateAmount = enterpriseRebateAmount;
	}

	public long getEnterpriseRebatePrice() {
		return enterpriseRebatePrice;
	}

	public void setEnterpriseRebatePrice(long enterpriseRebatePrice) {
		this.enterpriseRebatePrice = enterpriseRebatePrice;
	}

	public double getInternetRebate() {
		return internetRebate;
	}

	public void setInternetRebate(double internetRebate) {
		this.internetRebate = internetRebate;
	}

	public long getInternetRebateAmount() {
		return internetRebateAmount;
	}

	public void setInternetRebateAmount(long internetRebateAmount) {
		this.internetRebateAmount = internetRebateAmount;
	}

	public long getInternetRebatePrice() {
		return internetRebatePrice;
	}

	public void setInternetRebatePrice(long internetRebatePrice) {
		this.internetRebatePrice = internetRebatePrice;
	}
	
	public long getPromotionRebateAmount() {
		return promotionRebateAmount;
	}

	public void setPromotionRebateAmount(long promotionRebateAmount) {
		this.promotionRebateAmount = promotionRebateAmount;
	}

	public long getPromotionReduceId() {
		return promotionReduceId;
	}

	public void setPromotionReduceId(long promotionReduceId) {
		this.promotionReduceId = promotionReduceId;
	}

	public long getPromotionRebatePrice() {
		return promotionRebatePrice;
	}

	public void setPromotionRebatePrice(long promotionRebatePrice) {
		this.promotionRebatePrice = promotionRebatePrice;
	}

	public long getPromotionReduceQuota() {
		return promotionReduceQuota;
	}

	public void setPromotionReduceQuota(long promotionReduceQuota) {
		this.promotionReduceQuota = promotionReduceQuota;
	}

	public long getPromotionReduceAmount() {
		return promotionReduceAmount;
	}

	public void setPromotionReduceAmount(long promotionReduceAmount) {
		this.promotionReduceAmount = promotionReduceAmount;
	}

	public long getPromotionReduceAmountNotShared() {
		return promotionReduceAmountNotShared;
	}
	
	public void setPromotionReduceAmountNotShared(long promotionReduceAmountNotShared) {
		this.promotionReduceAmountNotShared = promotionReduceAmountNotShared;
	}

	public long getPromotionReducePrice() {
		return promotionReducePrice;
	}

	public void setPromotionReducePrice(long promotionReducePrice) {
		this.promotionReducePrice = promotionReducePrice;
	}

	public double getTotalRebate() {
		return totalRebate;
	}

	public void setTotalRebate(double totalRebate) {
		if (totalRebate == 0) {
			totalRebate = 100D;
		}
		this.totalRebate = totalRebate;
	}

	public long getTotalDerate() {
		return totalDerate;
	}

	public void setTotalDerate(long totalDerate) {
		this.totalDerate = totalDerate;
	}
	
	public long getTotalRebatePrice() {
		return totalRebatePrice;
	}

	public void setTotalRebatePrice(long totalRebatePrice) {
		this.totalRebatePrice = totalRebatePrice;
	}

	public long getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(long totalPrice) {
		this.totalPrice = totalPrice;
	}
	
	public long getUserClientCoupon() {
		return userClientCoupon;
	}

	public void setUserClientCoupon(long userClientCoupon) {
		this.userClientCoupon = userClientCoupon;
	}

	public long getFavorablePrice() {
		return favorablePrice;
	}

	public void setFavorablePrice(long favorablePrice) {
		this.favorablePrice = favorablePrice;
	}

	public int getRebateType() {
		return rebateType;
	}

	public void setRebateType(int rebateType) {
		this.rebateType = rebateType;
	}

	public long getPackageFee() {
		return packageFee;
	}

	public void setPackageFee(long packageFee) {
		this.packageFee = packageFee;
	}

	public boolean isProducePackageFee() {
		return producePackageFee;
	}

	public void setProducePackageFee(boolean producePackageFee) {
		this.producePackageFee = producePackageFee;
	}

	public long getOrderCouponPrice() {
		return orderCouponPrice;
	}

	public void setOrderCouponPrice(long orderCouponPrice) {
		this.orderCouponPrice = orderCouponPrice;
	}

	public int getCustomerTraffic() {
		return customerTraffic;
	}

	public void setCustomerTraffic(int customerTraffic) {
		this.customerTraffic = customerTraffic;
	}

	public long getTableFee() {
		return tableFee;
	}

	public void setTableFee(long tableFee) {
		this.tableFee = tableFee;
	}

	public long getMemberRebatePrice() {
		return memberRebatePrice;
	}

	public void setMemberRebatePrice(long memberRebatePrice) {
		this.memberRebatePrice = memberRebatePrice;
	}

	public long getPromotionPrice() {
		return promotionPrice;
	}

	public void setPromotionPrice(long promotionPrice) {
		this.promotionPrice = promotionPrice;
	}

	public int getEnterpriseRebateType() {
		return enterpriseRebateType;
	}

	public void setEnterpriseRebateType(int enterpriseRebateType) {
		this.enterpriseRebateType = enterpriseRebateType;
	}
	
	public StorePromotionReduce getStorePromotionReduce() {
		return storePromotionReduce;
	}
	
	public void setStorePromotionReduce(StorePromotionReduce storePromotionReduce) {
		this.storePromotionReduce = storePromotionReduce;
	}

	public StorePromotionReduceQuota getStorePromotionReduceQuota() {
		return storePromotionReduceQuota;
	}

	public void setStorePromotionReduceQuota(StorePromotionReduceQuota storePromotionReduceQuota) {
		this.storePromotionReduceQuota = storePromotionReduceQuota;
	}

	public List<StoreOrderItemPromotion> getStoreOrderItemPromotions() {
		if (storeOrderItemPromotions == null){
			storeOrderItemPromotions = Lists.newArrayList();
		}
		return storeOrderItemPromotions;
	}
	
	public StoreOrderItemPromotion getStoreOrderItemPromotion(StoreChargeItem storeChargeItem, long promotionId, int promotionType){
		for (StoreOrderItemPromotion storeOrderItemPromotion : storeOrderItemPromotions){
			if (storeOrderItemPromotion.getChargeItemId() == storeChargeItem.getChargeItemId() && storeOrderItemPromotion.getPromotionId() == promotionId && storeOrderItemPromotion.getPromotionType() == promotionType){
				return storeOrderItemPromotion;
			}
		}
		StoreOrderItemPromotion newItem = new StoreOrderItemPromotion(storeChargeItem);
		newItem.setPromotionId(promotionId);
		newItem.setPromotionType(promotionType);
		storeOrderItemPromotions.add(newItem);
		return newItem;
	}
	
	public void setStoreOrderItemPromotionOrderInfo(StoreOrder storeOrder){
		for (StoreOrderItemPromotion storeOrderItemPromotion : storeOrderItemPromotions){
			storeOrderItemPromotion.setStoreOrderInfo(storeOrder);
		}
	}
	
	public void setStoreOrderItemPromotions(List<StoreOrderItemPromotion> storeOrderItemPromotions) {
		this.storeOrderItemPromotions = storeOrderItemPromotions;
	}
	
	public Set<Long> getPromotionChargeItemIds() {
		return promotionChargeItemIds;
	}

	public void setPromotionChargeItemIds(Set<Long> promotionChargeItemIds) {
		this.promotionChargeItemIds = promotionChargeItemIds;
	}
	
	/**
	 * 订单菜品原价累计
	 * @param itemOrderPrice
	 */
	public void addOrderItemPrice(long itemOrderItemPrice){
		orderItemPrice = MoneyUtil.add(orderItemPrice, itemOrderItemPrice);
	}
	
	/**
	 * 订单原价累计
	 * @param itemOrderPrice
	 */
	public void addOrderPrice(long itemOrderPrice){
		orderPrice = MoneyUtil.add(orderPrice, itemOrderPrice);
	}

	public void addGratisPrice(long price){
		gratisPrice = MoneyUtil.add(gratisPrice,price);
	}
	
	/**
	 * 订单折后价累计（加法）
	 * @param itemFavorablePrice
	 */
	public void addFavorablePrice(long itemFavorablePrice){
		favorablePrice = MoneyUtil.add(favorablePrice, itemFavorablePrice);
	}
	
	/**
	 * 订单折后价累计（减法）
	 * @param itemDeratePrice
	 */
	public void subFavorablePrice(long itemDeratePrice){
		favorablePrice = MoneyUtil.sub(favorablePrice, itemDeratePrice);
	}
	
	/**
	 * 可使用优惠券金额累计
	 * @param itemCouponPrice
	 */
	public void addOrderCouponPrice(long itemCouponPrice){
		orderCouponPrice = MoneyUtil.add(orderCouponPrice, itemCouponPrice);
	}
	
	/**
	 * 会员价累计
	 * @param itemMemberRebatePrice
	 */
	public void addMemberRebatePrice(long itemMemberRebatePrice){
		memberRebatePrice = MoneyUtil.add(memberRebatePrice, itemMemberRebatePrice);
	}
	
	/**
	 * 收费特价累计
	 * @param itemPromotionPrice
	 */
	public void addPromotionPrice(long itemPromotionPrice){
		promotionPrice = MoneyUtil.add(promotionPrice, itemPromotionPrice);
	}
	
	/**
	 * 可享受网单折扣累计金额
	 * @param itemInternetRebateAmount
	 */
	public void addInternetRebateAmount(long itemInternetRebateAmount){
		internetRebateAmount = MoneyUtil.add(internetRebateAmount, itemInternetRebateAmount);
	}
	
	/**
	 * 享受网单折扣累计金额
	 * @param itemInternetRebatePrice
	 */
	public void addInternetRebatePrice(long itemInternetRebatePrice){
		internetRebatePrice = MoneyUtil.add(internetRebatePrice, itemInternetRebatePrice);
	}
	
	/**
	 * 可享受企业折扣累计金额
	 * @param itemEnterpriseRebateAmount
	 */
	public void addEnterpriseRebateAmount(long itemEnterpriseRebateAmount){
		enterpriseRebateAmount = MoneyUtil.add(enterpriseRebateAmount, itemEnterpriseRebateAmount);
	}
	
	/**
	 * 享受企业折扣累计金额
	 * @param itemEnterpriseRebatePrice
	 */
	public void addEnterpriseRebatePrice(long itemEnterpriseRebatePrice){
		enterpriseRebatePrice = MoneyUtil.add(enterpriseRebatePrice, itemEnterpriseRebatePrice);
	}
	
	/**
	 * 可享受折扣活动累计金额
	 * @param itemPromotionRebateAmount
	 */
	public void addPromotionRebateAmount(long itemPromotionRebateAmount){
		promotionRebateAmount = MoneyUtil.add(promotionRebateAmount, itemPromotionRebateAmount);
	}
	
	/**
	 * 享受折扣活动累计金额
	 * @param itemPromotionRebatePrice
	 */
	public void addPromotionRebatePrice(long itemPromotionRebatePrice){
		promotionRebatePrice = MoneyUtil.add(promotionRebatePrice, itemPromotionRebatePrice);
	}
	
	/**
	 * 参与满减金额（所有的）
	 * @param itemPromotionReduceAmount
	 */
	public void addPromotionReduceAmount(long itemPromotionReduceAmount){
		promotionReduceAmount = MoneyUtil.add(promotionReduceAmount, itemPromotionReduceAmount);
	}
	
	/**
	 * 参与满减金额（不共享）
	 * @param itemPromotionReduceAmount
	 */
	public void addPromotionReduceAmountNotShared(long itemPromotionReduceAmount){
		promotionReduceAmountNotShared = MoneyUtil.add(promotionReduceAmountNotShared, itemPromotionReduceAmount);
	}
	
}
