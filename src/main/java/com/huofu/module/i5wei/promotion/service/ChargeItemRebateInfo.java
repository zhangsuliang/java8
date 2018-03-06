package com.huofu.module.i5wei.promotion.service;

import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;

import java.util.Map;

public class ChargeItemRebateInfo {

    /**
     * 当takeMode={@link huofucore.facade.i5wei.order.StoreOrderTakeModeEnum#IN_AND_OUT}时 表示堂食的数据，其他情况下表示指定takeMode的数据
     */
    private Map<Long, StorePromotionRebate> chargeItemRebateMap;

    /**
     * 当takeMode={@link huofucore.facade.i5wei.order.StoreOrderTakeModeEnum#IN_AND_OUT}时，storePromotionReduce4TakeOut表示打包的数据，其他情况下没有值
     */
    private Map<Long, StorePromotionRebate> chargeItemRebateMap4TakeOut;

    public Map<Long, StorePromotionRebate> getChargeItemRebateMap() {
        return chargeItemRebateMap;
    }

    public void setChargeItemRebateMap(Map<Long, StorePromotionRebate> chargeItemRebateMap) {
        this.chargeItemRebateMap = chargeItemRebateMap;
    }

    public Map<Long, StorePromotionRebate> getChargeItemRebateMap4TakeOut() {
        return chargeItemRebateMap4TakeOut;
    }

    public void setChargeItemRebateMap4TakeOut(Map<Long, StorePromotionRebate> chargeItemRebateMap4TakeOut) {
        this.chargeItemRebateMap4TakeOut = chargeItemRebateMap4TakeOut;
    }

    public StorePromotionRebate getStorePromotionRebate(long chargeItemId, int takeMode) {
        if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
            if (this.chargeItemRebateMap4TakeOut == null) {
                return null;
            }
            return this.chargeItemRebateMap4TakeOut.get(chargeItemId);
        }
        if (this.chargeItemRebateMap == null) {
            return null;
        }
        return this.chargeItemRebateMap.get(chargeItemId);
    }
}
