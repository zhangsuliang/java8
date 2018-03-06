package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;

/**
 * 营业时段保存或更新时的返回对象
 * Created by lixuwei on 16/6/22.
 */
public class StoreTimeBucketSave {

    private StoreTimeBucket storeTimeBucket;

    private boolean tableFeeUpdate;

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }

    public boolean isTableFeeUpdate() {
        return tableFeeUpdate;
    }

    public void setTableFeeUpdate(boolean tableFeeUpdate) {
        this.tableFeeUpdate = tableFeeUpdate;
    }
}
