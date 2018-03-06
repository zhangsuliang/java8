package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreMenuDisplay;
import com.huofu.module.i5wei.menu.entity.StoreMenuDisplayCat;

import java.util.List;

/**
 * Created by akwei on 3/21/15.
 */
public class SaveStoreMenuDisplayResult {

    private List<StoreMenuDisplayCat> storeMenuDisplayCats;

    private List<StoreMenuDisplay> storeMenuDisplays;

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
}
