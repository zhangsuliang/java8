package com.huofu.module.i5wei.setting.service;

import com.huofu.module.i5wei.setting.entity.Store5weiSetting;

/**
 * 用于店铺设置的save方法返回
 * Created by lixuwei on 16/6/22.
 */
public class Store5weiSettingSaveResult {

    private Store5weiSetting store5weiSetting;

    private boolean tableFeeUpdate;

    public Store5weiSetting getStore5weiSetting() {
        return store5weiSetting;
    }

    public void setStore5weiSetting(Store5weiSetting store5weiSetting) {
        this.store5weiSetting = store5weiSetting;
    }

    public boolean isTableFeeUpdate() {
        return tableFeeUpdate;
    }

    public void setTableFeeUpdate(boolean tableFeeUpdate) {
        this.tableFeeUpdate = tableFeeUpdate;
    }
}
