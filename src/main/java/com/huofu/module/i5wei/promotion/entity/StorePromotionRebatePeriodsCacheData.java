package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.I5weiCachePrefix;
import huofuhelper.module.base.TObject;
import huofuhelper.util.cache.CacheDataCodecAble;
import huofuhelper.util.thrift.serialize.ThriftField;

import java.util.ArrayList;
import java.util.List;

@CacheDataCodecAble(prefix = I5weiCachePrefix.STORE_PROMOTION_REBATE_PERIODS)
public class StorePromotionRebatePeriodsCacheData extends TObject {

    @ThriftField(1)
    private List<StorePromotionRebatePeriod> list;

    public List<StorePromotionRebatePeriod> getList() {
        if (this.list == null) {
            this.list = new ArrayList<>(0);
        }
        return list;
    }

    public void setList(List<StorePromotionRebatePeriod> list) {
        this.list = list;
    }
}
