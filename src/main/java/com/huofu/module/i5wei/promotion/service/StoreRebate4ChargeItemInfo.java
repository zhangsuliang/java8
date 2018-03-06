package com.huofu.module.i5wei.promotion.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;

import java.util.List;

/**
 * Created by cherie on 2017/1/24.
 */
public class StoreRebate4ChargeItemInfo {

    /**
     * 指定收费项目参与的折扣活动
     */
    private List<StorePromotionRebate> storePromotionRebateList4Use;

    /**
     * 指定收费项目未参与的折扣活动
     */
    private List<StorePromotionRebate> storePromotionRebateList4NoUse;

    public List<StorePromotionRebate> getStorePromotionRebateList4Use() {
        return storePromotionRebateList4Use;
    }

    public void setStorePromotionRebateList4Use(List<StorePromotionRebate> storePromotionRebateList4Use) {
        this.storePromotionRebateList4Use = storePromotionRebateList4Use;
    }

    public List<StorePromotionRebate> getStorePromotionRebateList4NoUse() {
        return storePromotionRebateList4NoUse;
    }

    public void setStorePromotionRebateList4NoUse(List<StorePromotionRebate> storePromotionRebateList4NoUse) {
        this.storePromotionRebateList4NoUse = storePromotionRebateList4NoUse;
    }
}
