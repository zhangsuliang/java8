package com.huofu.module.i5wei.promotion.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;

import java.util.List;

/**
 * Created by cherie on 2017/1/24.
 */
public class StoreReduce4ChargeItemInfo {

    /**
     * 指定收费项目参与的折扣活动
     */
    private List<StorePromotionReduce> storePromotionReduceList4Use;
    
    /**
     * 指定收费项目未参与的折扣活动
     */
    private List<StorePromotionReduce> storePromotionReduceList4NoUse;

    public List<StorePromotionReduce> getStorePromotionReduceList4Use() {
        return storePromotionReduceList4Use;
    }

    public void setStorePromotionReduceList4Use(List<StorePromotionReduce> storePromotionReduceList4Use) {
        this.storePromotionReduceList4Use = storePromotionReduceList4Use;
    }

    public List<StorePromotionReduce> getStorePromotionReduceList4NoUse() {
        return storePromotionReduceList4NoUse;
    }

    public void setStorePromotionReduceList4NoUse(List<StorePromotionReduce> storePromotionReduceList4NoUse) {
        this.storePromotionReduceList4NoUse = storePromotionReduceList4NoUse;
    }
}
