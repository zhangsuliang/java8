package com.huofu.module.i5wei.promotion.entity;

import com.huofu.module.i5wei.base.I5weiCachePrefix;
import huofuhelper.module.base.TObject;
import huofuhelper.util.cache.CacheDataCodecAble;
import huofuhelper.util.thrift.serialize.ThriftField;

import java.util.List;

@CacheDataCodecAble(prefix = I5weiCachePrefix.STORE_PROMOTION_REDUCE_QUOTA)
public class StorePromotionReduceQuotasCacheData extends TObject {

    @ThriftField(1)
    private List<StorePromotionReduceQuota> list;

    public List<StorePromotionReduceQuota> getList() {
        return list;
    }

    public void setList(List<StorePromotionReduceQuota> list) {
        this.list = list;
    }
}
