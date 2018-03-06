package com.huofu.module.i5wei.promotion.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;

import java.util.List;

/**
 * Created by cherie on 2017/1/24.
 */
public class StoreGratis4ChargeItemInfo {
    
    /**
     * 指定收费项目参与的折扣活动
     */
    private List<StorePromotionGratis> storePromotionGratisList4Use;
    
    /**
     * 指定收费项目未参与的折扣活动
     */
    private List<StorePromotionGratis> storePromotionGratisList4NoUse;

    public List<StorePromotionGratis> getStorePromotionGratisList4Use() {
        return storePromotionGratisList4Use;
    }

    public void setStorePromotionGratisList4Use(List<StorePromotionGratis> storePromotionGratisList4Use) {
        this.storePromotionGratisList4Use = storePromotionGratisList4Use;
    }

    public List<StorePromotionGratis> getStorePromotionGratisList4NoUse() {
        return storePromotionGratisList4NoUse;
    }

    public void setStorePromotionGratisList4NoUse(List<StorePromotionGratis> storePromotionGratisList4NoUse) {
        this.storePromotionGratisList4NoUse = storePromotionGratisList4NoUse;
    }
}
