package com.huofu.module.i5wei.promotion.service;

import java.util.List;
import java.util.Map;

import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;

import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;


public class ChargeItemGratisInfo {

    /**
     * 当takeMode={@link huofucore.facade.i5wei.order.StoreOrderTakeModeEnum#IN_AND_OUT}时 表示堂食的数据，其他情况下表示指定takeMode的数据
     */
    private Map<Long, List<StorePromotionGratis>> chargeItemGratisMap;

    /**
     * 当takeMode={@link huofucore.facade.i5wei.order.StoreOrderTakeModeEnum#IN_AND_OUT}时，storePromotionGratis4TakeOut表示打包的数据，其他情况下没有值
     */
    private Map<Long, List<StorePromotionGratis>> chargeItemGratisMap4TakeOut;

    public Map<Long, List<StorePromotionGratis>> getChargeItemGratisMap() {
        return chargeItemGratisMap;
    }


    public void setChargeItemGratisMap(Map<Long, List<StorePromotionGratis>> chargeItemGratisMap) {
        this.chargeItemGratisMap = chargeItemGratisMap;
    }

    public Map<Long, List<StorePromotionGratis>> getChargeItemGratisMap4TakeOut() {
        return chargeItemGratisMap4TakeOut;
    }

    public void setChargeItemGratisMap4TakeOut(Map<Long, List<StorePromotionGratis>> gratisMap) {
        this.chargeItemGratisMap4TakeOut = gratisMap;
    }

    public List<StorePromotionGratis> getStorePromotionGratis(long chargeItemId, int takeMode) {
        if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
            if (this.chargeItemGratisMap4TakeOut == null) {
                return null;
            }
            return this.chargeItemGratisMap4TakeOut.get(chargeItemId);
        }
        if (this.chargeItemGratisMap == null) {
            return null;
        }
        return this.chargeItemGratisMap.get(chargeItemId);
    }

	@Override
	public String toString() {
		return "ChargeItemGratisInfo [chargeItemGratisMap=" + chargeItemGratisMap + ", chargeItemGratisMap4TakeOut="
				+ chargeItemGratisMap4TakeOut + "]";
	}
    
    
}
