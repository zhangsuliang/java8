package com.huofu.module.i5wei.order.service;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.promotion.service.ChargeItemGratisInfo;
import com.huofu.module.i5wei.promotion.service.ChargeItemRebateInfo;
import com.huofu.module.i5wei.promotion.service.ChargeItemReduceInfo;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import huofucore.facade.i5wei.order.StoreOrderPlaceParam;
import huofucore.facade.merchant.preferential.MerchantPreferentialOfUserDTO;

import java.util.Map;

/**
 * Created by akwei on 10/16/15.
 */
public class PlaceOrderParam {

	/**
	 * 下单参数
	 */
    private StoreOrderPlaceParam storeOrderPlaceParam;

    /**
	 * 企业折扣&网单折扣
	 */
    private MerchantPreferentialOfUserDTO rebateDto;

    /**
     * 币种ID
     */
    private int currencyId;

    /**
     * 店铺配置
     */
    private Store5weiSetting store5weiSetting;

    /**
     * 店铺外送配置
     */
    private StoreDeliverySetting storeDeliverySetting;
    
    /**
     * 营业时段
     */
	private StoreTimeBucket storeTimeBucket;
	
	/**
     * 快速交易，跳过后厨出餐
     */
    private boolean quickTrade;
	
    /**
     * 首份特价
     */
	private Map<Long, StoreChargeItemPromotion> chargeItemPromotionMap;
	
	/**
     * 折扣活动
     */
	private ChargeItemRebateInfo chargeItemRebateInfo;
	
	/**
     * 满减活动
     */
	private ChargeItemReduceInfo chargeItemReduceInfo;

	private ChargeItemGratisInfo chargeItemGratisInfo;

    public StoreOrderPlaceParam getStoreOrderPlaceParam() {
        return storeOrderPlaceParam;
    }

    public void setStoreOrderPlaceParam(StoreOrderPlaceParam storeOrderPlaceParam) {
        this.storeOrderPlaceParam = storeOrderPlaceParam;
		if (storeOrderPlaceParam.getCashierChannelId() > 0) {
			this.setQuickTrade(true);
		}
    }

    public MerchantPreferentialOfUserDTO getRebateDto() {
        return rebateDto;
    }

    public void setRebateDto(MerchantPreferentialOfUserDTO rebateDto) {
        this.rebateDto = rebateDto;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }

    public Store5weiSetting getStore5weiSetting() {
        return store5weiSetting;
    }

    public void setStore5weiSetting(Store5weiSetting store5weiSetting) {
        this.store5weiSetting = store5weiSetting;
    }

    public StoreDeliverySetting getStoreDeliverySetting() {
        return storeDeliverySetting;
    }

    public void setStoreDeliverySetting(StoreDeliverySetting storeDeliverySetting) {
        this.storeDeliverySetting = storeDeliverySetting;
    }

	public StoreTimeBucket getStoreTimeBucket() {
		return storeTimeBucket;
	}

	public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
		this.storeTimeBucket = storeTimeBucket;
	}
	
	public boolean isQuickTrade() {
		return quickTrade;
	}

	public void setQuickTrade(boolean quickTrade) {
		this.quickTrade = quickTrade;
	}

	public Map<Long, StoreChargeItemPromotion> getChargeItemPromotionMap() {
		if (chargeItemPromotionMap == null) {
			chargeItemPromotionMap = Maps.newHashMap();
		}
		return chargeItemPromotionMap;
	}

	public void setChargeItemPromotionMap(Map<Long, StoreChargeItemPromotion> chargeItemPromotionMap) {
		this.chargeItemPromotionMap = chargeItemPromotionMap;
	}

	public ChargeItemRebateInfo getChargeItemRebateInfo() {
		return chargeItemRebateInfo;
	}
	
	public void setChargeItemRebateInfo(ChargeItemRebateInfo chargeItemRebateInfo) {
		this.chargeItemRebateInfo = chargeItemRebateInfo;
	}

	public ChargeItemReduceInfo getChargeItemReduceInfo() {
		return chargeItemReduceInfo;
	}

	public void setChargeItemReduceInfo(ChargeItemReduceInfo chargeItemReduceInfo) {
		this.chargeItemReduceInfo = chargeItemReduceInfo;
	}

	public ChargeItemGratisInfo getChargeItemGratisInfo() {
		return chargeItemGratisInfo;
	}

	public void setChargeItemGratisInfo(ChargeItemGratisInfo chargeItemGratisInfo) {
		this.chargeItemGratisInfo = chargeItemGratisInfo;
	}
}
