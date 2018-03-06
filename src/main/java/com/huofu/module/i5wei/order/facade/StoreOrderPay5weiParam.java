package com.huofu.module.i5wei.order.facade;

import huofucore.facade.pay.payment.PayResultOfPayOrder;

import com.huofu.module.i5wei.setting.entity.Store5weiSetting;


public class StoreOrderPay5weiParam {

    /**
     * 商户ID
     */
    private int merchantId;

    /**
     * 店铺ID
     */
    private long storeId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 用户ID
     */
    private long userId;

    /**
     * 支付订单ID
     */
    private String payOrderId;

    /**
     * 实际支付币种ID
     */
    private int actualCurrencyId;

    /**
     * 实际支付金额
     */
    private long actualPrice;

    /**
     * 用户下单备注
     */
    private String userRemark;
    
    /**
     * 五味店铺设置
     */
    private Store5weiSetting store5weiSetting;
    
    /**
     * 支付结果
     */
    private PayResultOfPayOrder payResult;

    public String getUserRemark() {
        return userRemark;
    }

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
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

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public int getActualCurrencyId() {
        return actualCurrencyId;
    }

    public void setActualCurrencyId(int actualCurrencyId) {
        this.actualCurrencyId = actualCurrencyId;
    }

    public long getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(long actualPrice) {
        this.actualPrice = actualPrice;
    }

	public Store5weiSetting getStore5weiSetting() {
		return store5weiSetting;
	}

	public void setStore5weiSetting(Store5weiSetting store5weiSetting) {
		this.store5weiSetting = store5weiSetting;
	}

	public PayResultOfPayOrder getPayResult() {
		return payResult;
	}

	public void setPayResult(PayResultOfPayOrder payResult) {
		this.payResult = payResult;
	}
	
	public boolean hasCouponPayAmount() {
		if (payResult != null) {
			if (payResult.getCouponAmount() > 0) {
				return true;
			}
		}
		return false;
	}
    
}
