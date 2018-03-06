package com.huofu.module.i5wei.inventory.service;

import huofuhelper.util.NumberUtil;

public class StoreInventoryCheck {
	private long chargeItemId;
	private String chargeItemName;
	private long productId;
	private String productName;
	private double remain;
	private double orderAmount;
	private double subItemAmount;
	private double chargeItemRemain;
	
	public StoreInventoryCheck(){
	}
	
	public StoreInventoryCheck(long productId, String productName, double remain, double orderAmount) {
		this.productId = productId;
		this.productName = productName;
		this.remain = remain;
		this.orderAmount = orderAmount;
	}
	
	public void setChargeItem(long chargeItemId, String chargeItemName, double subItemAmount){
		this.chargeItemId = chargeItemId;
		this.chargeItemName = chargeItemName;
		this.subItemAmount = subItemAmount;
	}
	
	public long getChargeItemId() {
		return chargeItemId;
	}

	public void setChargeItemId(long chargeItemId) {
		this.chargeItemId = chargeItemId;
	}

	public String getChargeItemName() {
		return chargeItemName;
	}

	public void setChargeItemName(String chargeItemName) {
		this.chargeItemName = chargeItemName;
	}

	public long getProductId() {
		return productId;
	}
	public void setProductId(long productId) {
		this.productId = productId;
	}
	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	public double getRemain() {
		if (remain < 0) {
			remain = 0;
		}
		return remain;
	}
	public void setRemain(double remain) {
		this.remain = remain;
	}
	public double getOrderAmount() {
		return orderAmount;
	}
	public void setOrderAmount(double orderAmount) {
		this.orderAmount = orderAmount;
	}

	public double getSubItemAmount() {
		return subItemAmount;
	}

	public void setSubItemAmount(double subItemAmount) {
		this.subItemAmount = subItemAmount;
	}

	public double getChargeItemRemain() {
		try{
			chargeItemRemain = NumberUtil.div(remain, subItemAmount);
		}catch(Throwable e){
		}
		if (chargeItemRemain < 0) {
			chargeItemRemain = 0;
		}
		return chargeItemRemain;
	}

	public void setChargeItemRemain(double chargeItemRemain) {
		this.chargeItemRemain = chargeItemRemain;
	}
	
}
