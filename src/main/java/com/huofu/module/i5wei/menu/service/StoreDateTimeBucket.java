package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;

import java.util.List;

/**
 * Created by akwei on 7/8/15.
 */
public class StoreDateTimeBucket {

    private long time;

    private List<StoreTimeBucket> storeTimeBuckets;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getStoreTimeBucketsSize() {
        if (storeTimeBuckets == null || storeTimeBuckets.isEmpty()) {
            return 0;
        }
        return storeTimeBuckets.size();
    }

    public List<StoreTimeBucket> getStoreTimeBuckets() {
        return storeTimeBuckets;
    }

    public void setStoreTimeBuckets(List<StoreTimeBucket> storeTimeBuckets) {
        this.storeTimeBuckets = storeTimeBuckets;
    }
}
