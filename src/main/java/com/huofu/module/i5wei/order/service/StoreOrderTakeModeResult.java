package com.huofu.module.i5wei.order.service;


public class StoreOrderTakeModeResult {
	
	/**
	 * 取餐模式，订单的取餐模式：1＝堂食；2＝外带；3＝堂食+外带；4＝外送；5＝快取
	 */
	private int takeMode;
	
	/**
     * 是否限制取餐时间
     */
    private boolean limitMealTime = true;
    
    /**
     * 餐牌号，为0则表示没有餐牌号
     */
    private int siteNumber;
    
    /**
     * 是否加菜：0=不是，1=是
     */
    private boolean enableAddDishes;
    
    /**
     * 是否跳过取号环节：0=不是，1=是
     */
    private boolean skipTakeCode;
    
    /**
     * 跳过后厨出餐，默认false
     */
    private boolean disableKitchen;
    
	public int getTakeMode() {
		return takeMode;
	}

	public void setTakeMode(int takeMode) {
		this.takeMode = takeMode;
	}

	public boolean isLimitMealTime() {
		return limitMealTime;
	}

	public void setLimitMealTime(boolean limitMealTime) {
		this.limitMealTime = limitMealTime;
	}

	public int getSiteNumber() {
		return siteNumber;
	}

	public boolean isEnableAddDishes() {
		return enableAddDishes;
	}

	public void setEnableAddDishes(boolean enableAddDishes) {
		this.enableAddDishes = enableAddDishes;
	}

	public void setSiteNumber(int siteNumber) {
		this.siteNumber = siteNumber;
	}

	public boolean isSkipTakeCode() {
		return skipTakeCode;
	}

	public void setSkipTakeCode(boolean skipTakeCode) {
		this.skipTakeCode = skipTakeCode;
	}

	public boolean isDisableKitchen() {
		return disableKitchen;
	}

	public void setDisableKitchen(boolean disableKitchen) {
		this.disableKitchen = disableKitchen;
	}

}
