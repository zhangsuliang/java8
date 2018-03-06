package com.huofu.module.i5wei.promotion.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;

import java.util.List;
import java.util.Map;

/**
 * Created by cherie on 2017/1/23.
 */
public class ChargeItemPromotionRebates {

    private List<StorePromotionRebate> storePromotionRebateList;

    private Map<Long, List<Long>> promotionRebateIdMap;

    public List<StorePromotionRebate> getStorePromotionRebateList() {
        return storePromotionRebateList;
    }

    public void setStorePromotionRebateList(List<StorePromotionRebate> storePromotionRebateList) {
        this.storePromotionRebateList = storePromotionRebateList;
    }

    public Map<Long, List<Long>> getPromotionRebateIdMap() {
        return promotionRebateIdMap;
    }

    public void setPromotionRebateIdMap(Map<Long, List<Long>> promotionRebateIdMap) {
        this.promotionRebateIdMap = promotionRebateIdMap;
    }
}
