package com.huofu.module.i5wei.promotion.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebateChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceQuota;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceQuotasCacheData;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.cache.CacheMapResult;
import huofuhelper.util.cache.WengerCache;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StorePromotionReduceQuotaDAO extends AbsQueryDAO<StorePromotionReduceQuota> {

    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(long promotionReduceId) {
        this.cacheCleaner.add(StorePromotionReduceQuotasCacheData.class, promotionReduceId);
    }

    public int deleteByPromotionReduceId(int merchantId, long storeId, long promotionReduceId) {
        this.addCacheCleaner(promotionReduceId);
        return this.query.delete(StorePromotionReduceQuota.class, "where merchant_id=? and store_id=? and promotion_reduce_id=?",
                new Object[]{merchantId, storeId, promotionReduceId});
    }

    public void deleteAll() {
        this.query.delete(StorePromotionReduceQuota.class, null, null);
    }

    public Map<Long, List<StorePromotionReduceQuota>> getMapInPromotionReduceIds(int merchantId, long storeId, List<Long> promotionReduceIds, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StorePromotionReduceQuota>> map = Maps.newHashMap();
        if (promotionReduceIds == null || promotionReduceIds.isEmpty()) {
            return map;
        }
        if (enableCache) {
            CacheMapResult<Long, StorePromotionReduceQuotasCacheData> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionReduceQuotasCacheData.class, promotionReduceIds);
            for (Map.Entry<Long, StorePromotionReduceQuotasCacheData> entry : mapResult.getDataMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getList());
            }
            if (mapResult.getNoCacheDataKeysSize() > 0) {
                Map<Long, List<StorePromotionReduceQuota>> map1 = this._getMapInPromotionReduceIdsFromDB(merchantId, storeId, mapResult.getNoCacheDataKeys(), enableSlave);
                if (!map1.isEmpty()) {
                    this._addToCache(map1);
                }
                map.putAll(map1);
            }
            return map;
        }
        return this._getMapInPromotionReduceIdsFromDB(merchantId, storeId, promotionReduceIds, enableSlave);
    }

    private void _addToCache(Map<Long, List<StorePromotionReduceQuota>> map) {
        Map<Long, StorePromotionReduceQuotasCacheData> cacheDataMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StorePromotionReduceQuota>> entry : map.entrySet()) {
            StorePromotionReduceQuotasCacheData cacheData = new StorePromotionReduceQuotasCacheData();
            cacheData.setList(entry.getValue());
            cacheDataMap.put(entry.getKey(), cacheData);
        }
        this.wengerCache.setMulti4Obj(cacheDataMap);
    }

    private Map<Long, List<StorePromotionReduceQuota>> _getMapInPromotionReduceIdsFromDB(int merchantId, long storeId, List<Long> promotionReduceIds, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionReduceQuota> list = this.query.listInValues2(StorePromotionReduceQuota.class, "where merchant_id=? and store_id=?", "promotion_reduce_id", "order by quota_price asc", Lists.newArrayList(merchantId, storeId), promotionReduceIds);
        Map<Long, List<StorePromotionReduceQuota>> map = Maps.newHashMap();
        for (StorePromotionReduceQuota quota : list) {
            List<StorePromotionReduceQuota> sublist = map.get(quota.getPromotionReduceId());
            if (sublist == null) {
                sublist = Lists.newArrayList();
                map.put(quota.getPromotionReduceId(), sublist);
            }
            sublist.add(quota);
        }
        return map;
    }
}
