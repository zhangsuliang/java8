package com.huofu.module.i5wei.menu.entity;

public class StoreTimeBucketItem {

    private StoreTimeBucket storeTimeBucket;

    private StoreChargeItem storeChargeItem;

    /**
     * 只有创建数据的时候才存在
     */
    private long productId;

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }

    public StoreChargeItem getStoreChargeItem() {
        return storeChargeItem;
    }

    public void setStoreChargeItem(StoreChargeItem storeChargeItem) {
        this.storeChargeItem = storeChargeItem;
    }
}
