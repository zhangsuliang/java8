package com.huofu.module.i5wei.promotion.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;

import java.util.List;
import java.util.Map;

/**
 * Created by cherie on 2017/1/23.
 */
public class ChargeItemPromotionReduces {

    private List<StorePromotionReduce> storePromotionReduceList;

    private Map<Long, List<Long>> promotionReduceIdMap;

    public List<StorePromotionReduce> getStorePromotionReduceList() {
        return storePromotionReduceList;
    }

    public void setStorePromotionReduceList(List<StorePromotionReduce> storePromotionReduceList) {
        this.storePromotionReduceList = storePromotionReduceList;
    }

    public Map<Long, List<Long>> getPromotionReduceIdMap() {
        return promotionReduceIdMap;
    }

    public void setPromotionReduceIdMap(Map<Long, List<Long>> promotionReduceIdMap) {
        this.promotionReduceIdMap = promotionReduceIdMap;
    }
}
