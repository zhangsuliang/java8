package com.huofu.module.i5wei.order.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;

import huofucore.facade.i5wei.order.StoreOrderPromotionTypeEnum;

/**
 * 订单项目最小折扣计算结果
 * 
 * @author chenkai
 * @since 2016-11-17
 */
public class StoreOrderItemMinRebate {

	/**
	 * 折扣活动ID
	 */
	private long promotionRebateId = 0;
	
	/**
	 * 折扣活动
	 */
	private StorePromotionRebate storePromotionRebate;
	
	/**
	 * 菜品定价折扣减免，例如：20元，打折后18元，那么折扣减免为2元
	 */
	private long chargeItemRebateDerate = 0L;
	/**
	 * 菜品定价的折扣类型
	 */
	private StoreOrderPromotionTypeEnum chargeItemPromotionType = null;
	/**
	 * 菜品定价可参与折扣的数量
	 */
	private double chargeItemRebateAmount = 0D;
	
	/**
	 * 菜品定价是否打包
	 */
	private boolean packed;
	
	/**
	 * 客户端类型
	 */
	private int clientType;
	
	/**
	 * 取餐方式
	 */
	private int takeMode;
	
	public long getPromotionRebateId() {
		return promotionRebateId;
	}

	public void setPromotionRebateId(long promotionRebateId) {
		this.promotionRebateId = promotionRebateId;
	}

	public StorePromotionRebate getStorePromotionRebate() {
		return storePromotionRebate;
	}

	public void setStorePromotionRebate(StorePromotionRebate storePromotionRebate) {
		this.storePromotionRebate = storePromotionRebate;
	}

	public long getChargeItemRebateDerate() {
		return chargeItemRebateDerate;
	}

	public void setChargeItemRebateDerate(long chargeItemRebateDerate) {
		this.chargeItemRebateDerate = chargeItemRebateDerate;
	}

	public StoreOrderPromotionTypeEnum getChargeItemPromotionType() {
		return chargeItemPromotionType;
	}

	public void setChargeItemPromotionType(StoreOrderPromotionTypeEnum chargeItemPromotionType) {
		this.chargeItemPromotionType = chargeItemPromotionType;
	}

	public double getChargeItemRebateAmount() {
		return chargeItemRebateAmount;
	}

	public void setChargeItemRebateAmount(double chargeItemRebateAmount) {
		this.chargeItemRebateAmount = chargeItemRebateAmount;
	}

	public boolean isPacked() {
		return packed;
	}

	public void setPacked(boolean packed) {
		this.packed = packed;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public int getTakeMode() {
		return takeMode;
	}

	public void setTakeMode(int takeMode) {
		this.takeMode = takeMode;
	}

}
