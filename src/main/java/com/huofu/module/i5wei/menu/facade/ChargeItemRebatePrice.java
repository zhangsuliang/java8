package com.huofu.module.i5wei.menu.facade;

import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;

class ChargeItemRebatePrice {

    private StoreChargeItem storeChargeItem;

    /**
     * 符合条件参加活动时有值
     */
    private StorePromotionRebate storePromotionRebate;

    /**
     * 可能是折扣后价格、也可能是原价、也可能是企业折扣后价格、会员价
     */
    private long price;

    public StoreChargeItem getStoreChargeItem() {
        return storeChargeItem;
    }

    public void setStoreChargeItem(StoreChargeItem storeChargeItem) {
        this.storeChargeItem = storeChargeItem;
    }

    public StorePromotionRebate getStorePromotionRebate() {
        return storePromotionRebate;
    }

    public void setStorePromotionRebate(StorePromotionRebate storePromotionRebate) {
        this.storePromotionRebate = storePromotionRebate;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }
}
