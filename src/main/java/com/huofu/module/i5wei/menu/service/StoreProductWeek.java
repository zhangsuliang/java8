package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;

/**
 * Created by akwei on 3/13/15.
 */
public class StoreProductWeek {

    private int weekDay;

    private long timeBucketId;

    private boolean nextWeek;

    private StoreTimeBucket storeTimeBucket;

    public boolean isNextWeek() {
        return nextWeek;
    }

    public void setNextWeek(boolean nextWeek) {
        this.nextWeek = nextWeek;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }
}
