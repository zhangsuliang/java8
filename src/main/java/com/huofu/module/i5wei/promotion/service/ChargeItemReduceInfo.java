package com.huofu.module.i5wei.promotion.service;

import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;

import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;

public class ChargeItemReduceInfo {

    /**
     * 当takeMode={@link huofucore.facade.i5wei.order.StoreOrderTakeModeEnum#IN_AND_OUT}时 表示堂食的数据，其他情况下为takeMode对应的数据
     */
    private StorePromotionReduce storePromotionReduce;

    /**
     * 当takeMode={@link huofucore.facade.i5wei.order.StoreOrderTakeModeEnum#IN_AND_OUT}时 表示打包的数据，其他情况下没有数据
     */
    private StorePromotionReduce storePromotionReduce4TakeOut;

    public StorePromotionReduce getStorePromotionReduce() {
        return storePromotionReduce;
    }

    public void setStorePromotionReduce(StorePromotionReduce storePromotionReduce) {
        this.storePromotionReduce = storePromotionReduce;
    }

    public StorePromotionReduce getStorePromotionReduce4TakeOut() {
        return storePromotionReduce4TakeOut;
    }

    public void setStorePromotionReduce4TakeOut(StorePromotionReduce storePromotionReduce4TakeOut) {
        this.storePromotionReduce4TakeOut = storePromotionReduce4TakeOut;
    }

    public StorePromotionReduce getBestStorePromotionReduce(int takeMode) {
        if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
            return storePromotionReduce4TakeOut;
        }
        if(takeMode == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()){
			if (storePromotionReduce4TakeOut != null) {
				return this.storePromotionReduce4TakeOut;
			} else {
				return this.storePromotionReduce;
			}
        }
        return this.storePromotionReduce;
    }

    @Override
    public String toString() {
        return "ChargeItemReduceInfo{" +
                "storePromotionReduce=" + storePromotionReduce +
                ", storePromotionReduce4TakeOut=" + storePromotionReduce4TakeOut +
                '}';
    }
}
