package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.I5weiCachePrefix;
import huofuhelper.module.base.TObject;
import huofuhelper.util.cache.CacheDataCodecAble;
import huofuhelper.util.thrift.serialize.ThriftField;

import java.util.ArrayList;
import java.util.List;

@CacheDataCodecAble(prefix = I5weiCachePrefix.STORE_PROMOTION_REDUCE_CHARGE_ITEMS)
public class StorePromotionReduceChargeItemsCacheData extends TObject {

    @ThriftField(1)
    private List<StorePromotionReduceChargeItem> list;

    public List<StorePromotionReduceChargeItem> getList() {
        if (this.list == null) {
            this.list = new ArrayList<>(0);
        }
        return list;
    }

    public void setList(List<StorePromotionReduceChargeItem> list) {
        this.list = list;
    }
}
