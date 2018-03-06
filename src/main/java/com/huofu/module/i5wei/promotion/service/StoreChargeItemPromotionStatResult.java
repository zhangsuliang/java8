package com.huofu.module.i5wei.promotion.service;


public class StoreChargeItemPromotionStatResult {

	/**
	 * 收费项目名称
	 */
	private String chargeItemName;

	/**
	 * 收费项目是否被删除
	 */
	private boolean chargeItemDelete;
	/**
	 * 收费项目价格
	 */
	private long price;

	/**
	 * 参与活动用户数量
	 */
	private int countUser;

	/**
	 * 活动产生的订单额度
	 */
	private long countSumOrderPrice;

	/**
	 * 活动优惠金额
	 */
	private long promotionSumPrice;

	/**
	 * 平均每单价格
	 */
	private long averageOrderPrice;

	/**
	 * 平均每人参与次数
	 */
	private double averageUserFrequency;

	/**
	 * 今日参与人数
	 */
	private int todayUser;
	
	/**
	 * 今日所卖金额
	 */
	private long todaySalePrice;
	
	public String getChargeItemName() {
		return chargeItemName;
	}

	public void setChargeItemName(String chargeItemName) {
		this.chargeItemName = chargeItemName;
	}

	public boolean isChargeItemDelete() {
		return chargeItemDelete;
	}

	public void setChargeItemDelete(boolean chargeItemDelete) {
		this.chargeItemDelete = chargeItemDelete;
	}

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
	}

	public int getCountUser() {
		return countUser;
	}

	public void setCountUser(int countUser) {
		this.countUser = countUser;
	}

	public long getCountSumOrderPrice() {
		return countSumOrderPrice;
	}

	public void setCountSumOrderPrice(long countSumOrderPrice) {
		this.countSumOrderPrice = countSumOrderPrice;
	}

	public long getPromotionSumPrice() {
		return promotionSumPrice;
	}

	public void setPromotionSumPrice(long promotionSumPrice) {
		this.promotionSumPrice = promotionSumPrice;
	}

	public long getAverageOrderPrice() {
		return averageOrderPrice;
	}

	public void setAverageOrderPrice(long averageOrderPrice) {
		this.averageOrderPrice = averageOrderPrice;
	}

	public double getAverageUserFrequency() {
		return averageUserFrequency;
	}

	public void setAverageUserFrequency(double averageUserFrequency) {
		this.averageUserFrequency = averageUserFrequency;
	}

	public int getTodayUser() {
		return todayUser;
	}

	public void setTodayUser(int todayUser) {
		this.todayUser = todayUser;
	}

	public long getTodaySalePrice() {
		return todaySalePrice;
	}

	public void setTodaySalePrice(long todaySalePrice) {
		this.todaySalePrice = todaySalePrice;
	}

}
