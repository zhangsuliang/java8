package com.huofu.module.i5wei.promotion.entity;

import java.util.ArrayList;
import java.util.List;

import com.huofu.module.i5wei.base.I5weiCachePrefix;

import huofuhelper.module.base.TObject;
import huofuhelper.util.cache.CacheDataCodecAble;
import huofuhelper.util.thrift.serialize.ThriftField;
@SuppressWarnings("all")
@CacheDataCodecAble(prefix=I5weiCachePrefix.STORE_PROMOTION_GRATIS_CHARGE_ITEMS)
public class StorePromotionGratisChargeItemsCacheData  extends TObject{


    @ThriftField(1)
    private List<StorePromotionGratisChargeItem> list;

    public List<StorePromotionGratisChargeItem> getList() {
        if (this.list == null) {
            this.list = new ArrayList<>(0);
        }
        return list;
    }

    public void setList(List<StorePromotionGratisChargeItem> list) {
        this.list = list;
    }

}
