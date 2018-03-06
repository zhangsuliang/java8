package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;

/**
 * Created by akwei on 9/6/15.
 */
public class TimeBucketMenuCal {

    private long timeBucketId;

    private int chargeItemAmount;

    private boolean paused;

    private StoreTimeBucket storeTimeBucket;

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public int getChargeItemAmount() {
        return chargeItemAmount;
    }

    public void setChargeItemAmount(int chargeItemAmount) {
        this.chargeItemAmount = chargeItemAmount;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }

//    public TimeBucketMenuCal copySelf() {
//        TimeBucketMenuCal timeBucketMenuCal = new TimeBucketMenuCal();
//        BeanUtil.copy(this, timeBucketMenuCal);
//        return timeBucketMenuCal;
//    }
}
