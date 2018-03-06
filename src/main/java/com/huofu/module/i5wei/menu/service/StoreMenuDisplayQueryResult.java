package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.entity.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by akwei on 3/21/15.
 */
public class StoreMenuDisplayQueryResult {

    private List<StoreMenuDisplay> storeMenuDisplays;

    private List<StoreMenuDisplayCat> storeMenuDisplayCats;

    private Map<Long, StoreChargeItem> storeChargeItemMap;

    public List<StoreProduct> getStoreProducts() {
        Map<Long, StoreProduct> storeProductMap = Maps.newHashMap();
        Collection<StoreChargeItem> storeChargeItems = storeChargeItemMap.values();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            if (storeChargeItem.getStoreChargeSubitems() != null) {
                for (StoreChargeSubitem subitem : storeChargeItem.getStoreChargeSubitems()) {
                    storeProductMap.put(subitem.getProductId(), subitem.getStoreProduct());
                }
            }
        }
        return new ArrayList<>(storeProductMap.values());
    }

    public List<StoreMenuDisplayCat> getStoreMenuDisplayCats() {
        return storeMenuDisplayCats;
    }

    public void setStoreMenuDisplayCats(List<StoreMenuDisplayCat> storeMenuDisplayCats) {
        this.storeMenuDisplayCats = storeMenuDisplayCats;
    }

    public List<StoreMenuDisplay> getStoreMenuDisplays() {
        return storeMenuDisplays;
    }

    public void setStoreMenuDisplays(List<StoreMenuDisplay> storeMenuDisplays) {
        this.storeMenuDisplays = storeMenuDisplays;
    }

    public Map<Long, StoreChargeItem> getStoreChargeItemMap() {
        return storeChargeItemMap;
    }

    public void setStoreChargeItemMap(Map<Long, StoreChargeItem> storeChargeItemMap) {
        this.storeChargeItemMap = storeChargeItemMap;
    }
}
