package com.huofu.module.i5wei.promotion.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;

import java.util.List;
import java.util.Map;

/**
 * Created by cherie on 2017/1/23.
 */
public class ChargeItemPromotionGratises {

    private List<StorePromotionGratis> storePromotionGratisList;

    private Map<Long, List<Long>> promotionGratisIdMap;

    public List<StorePromotionGratis> getStorePromotionGratisList() {
        return storePromotionGratisList;
    }

    public void setStorePromotionGratisList(List<StorePromotionGratis> storePromotionGratisList) {
        this.storePromotionGratisList = storePromotionGratisList;
    }

    public Map<Long, List<Long>> getPromotionGratisIdMap() {
        return promotionGratisIdMap;
    }

    public void setPromotionGratisIdMap(Map<Long, List<Long>> promotionGratisIdMap) {
        this.promotionGratisIdMap = promotionGratisIdMap;
    }
}
