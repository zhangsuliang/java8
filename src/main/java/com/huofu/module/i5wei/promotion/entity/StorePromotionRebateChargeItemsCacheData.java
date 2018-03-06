package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.I5weiCachePrefix;
import huofuhelper.module.base.TObject;
import huofuhelper.util.cache.CacheDataCodecAble;
import huofuhelper.util.thrift.serialize.ThriftField;

import java.util.ArrayList;
import java.util.List;

@CacheDataCodecAble(prefix = I5weiCachePrefix.STORE_PROMOTION_REBATE_CHARGE_ITEMS)
public class StorePromotionRebateChargeItemsCacheData extends TObject {

    @ThriftField(1)
    private List<StorePromotionRebateChargeItem> list;

    public List<StorePromotionRebateChargeItem> getList() {
        if (this.list == null) {
            this.list = new ArrayList<>(0);
        }
        return list;
    }

    public void setList(List<StorePromotionRebateChargeItem> list) {
        this.list = list;
    }
}
