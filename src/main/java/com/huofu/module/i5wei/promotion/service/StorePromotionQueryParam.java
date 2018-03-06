package com.huofu.module.i5wei.promotion.service;

import huofucore.facade.i5wei.order.StoreOrderPlaceItemParam;
import huofucore.facade.i5wei.order.StoreOrderPlaceParam;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;

import java.util.ArrayList;
import java.util.List;

public class StorePromotionQueryParam {

    /**
     * 商户ID
     */
    private int merchantId;

    /**
     * 店铺ID
     */
    private long storeId;

    /**
     * 就餐日期
     */
    private long repastDate;

    /**
     * 营业时段ID
     */
    private long timeBucketId;

    /**
     * 取餐方式
     */
    private int takeMode;

    private int clientType;
   
    /**
     * 菜品定价ID列表
     */
    private List<Long> chargeItemIds;

    public StorePromotionQueryParam() {

    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public StorePromotionQueryParam(StoreOrderPlaceParam storeOrderPlaceParam) {
        this.merchantId = storeOrderPlaceParam.getMerchantId();
        this.storeId = storeOrderPlaceParam.getStoreId();
        this.repastDate = storeOrderPlaceParam.getRepastDate();
        this.timeBucketId = storeOrderPlaceParam.getTimeBucketId();
        this.takeMode = storeOrderPlaceParam.getTakeMode();
        this.clientType = storeOrderPlaceParam.getClientType();
        List<StoreOrderPlaceItemParam> chargeItems = storeOrderPlaceParam.getChargeItems();
        chargeItemIds = new ArrayList<Long>();
        for (StoreOrderPlaceItemParam itemParam : chargeItems) {
            chargeItemIds.add(itemParam.getChargeItemId());
        }
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

    public long getRepastDate() {
        return repastDate;
    }

    public void setRepastDate(long repastDate) {
        this.repastDate = repastDate;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public int getTakeMode() {
        return takeMode;
    }

    public void setTakeMode(int takeMode) {
        this.takeMode = takeMode;
    }

    public List<Long> getChargeItemIds() {
        return chargeItemIds;
    }

    public void setChargeItemIds(List<Long> chargeItemIds) {
        this.chargeItemIds = chargeItemIds;
    }

}
