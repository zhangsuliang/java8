package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPrice;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemWeek;
import com.huofu.module.i5wei.menu.entity.StoreChargeSubitem;

import java.util.List;

/**
 * Created by akwei on 3/6/15.
 */
public class CreateStoreChargeItemResult {

    private StoreChargeItem storeChargeItem;

    private List<StoreChargeSubitem> storeChargeSubitems;

    private StoreChargeItemPrice curStoreChargeItemPrice;

    private StoreChargeItemPrice nextStoreChargeItemPrice;

    private List<StoreChargeItemWeek> curStoreChargeItemWeeks;

    private List<StoreChargeItemWeek> nextWeekStoreChargeItemWeeks;

    public List<StoreChargeItemWeek> getCurStoreChargeItemWeeks() {
        return curStoreChargeItemWeeks;
    }

    public void setCurStoreChargeItemWeeks(List<StoreChargeItemWeek> curStoreChargeItemWeeks) {
        this.curStoreChargeItemWeeks = curStoreChargeItemWeeks;
    }

    public List<StoreChargeItemWeek> getNextWeekStoreChargeItemWeeks() {
        return nextWeekStoreChargeItemWeeks;
    }

    public void setNextWeekStoreChargeItemWeeks(List<StoreChargeItemWeek> nextWeekStoreChargeItemWeeks) {
        this.nextWeekStoreChargeItemWeeks = nextWeekStoreChargeItemWeeks;
    }

    public StoreChargeItem getStoreChargeItem() {
        return storeChargeItem;
    }

    public void setStoreChargeItem(StoreChargeItem storeChargeItem) {
        this.storeChargeItem = storeChargeItem;
    }

    public List<StoreChargeSubitem> getStoreChargeSubitems() {
        return storeChargeSubitems;
    }

    public void setStoreChargeSubitems(List<StoreChargeSubitem> storeChargeSubitems) {
        this.storeChargeSubitems = storeChargeSubitems;
    }

    public StoreChargeItemPrice getCurStoreChargeItemPrice() {
        return curStoreChargeItemPrice;
    }

    public void setCurStoreChargeItemPrice(StoreChargeItemPrice curStoreChargeItemPrice) {
        this.curStoreChargeItemPrice = curStoreChargeItemPrice;
    }

    public StoreChargeItemPrice getNextStoreChargeItemPrice() {
        return nextStoreChargeItemPrice;
    }

    public void setNextStoreChargeItemPrice(StoreChargeItemPrice nextStoreChargeItemPrice) {
        this.nextStoreChargeItemPrice = nextStoreChargeItemPrice;
    }
}
