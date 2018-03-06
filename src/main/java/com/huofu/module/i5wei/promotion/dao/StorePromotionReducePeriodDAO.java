package com.huofu.module.i5wei.promotion.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReducePeriod;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReducePeriodsCacheData;
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
public class StorePromotionReducePeriodDAO extends AbsQueryDAO<StorePromotionReducePeriod> {

    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(long promotionReduceId) {
        this.cacheCleaner.add(StorePromotionReducePeriodsCacheData.class, promotionReduceId);
    }

    public int deleteByPromotionReduceId(int merchantId, long storeId, long promotionReduceId) {
        this.addCacheCleaner(promotionReduceId);
        return this.query.delete(StorePromotionReducePeriod.class,
                "where merchant_id=? and store_id=? and promotion_reduce_id=?",
                new Object[]{merchantId, storeId, promotionReduceId});
    }

    public void deleteAll() {
        this.query.delete(StorePromotionReducePeriod.class, null, null);
    }

    public Map<Long, List<StorePromotionReducePeriod>> getMapInPromotionReduceIds(
            int merchantId, long storeId, List<Long> promotionReduceIds, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StorePromotionReducePeriod>> map = Maps.newHashMap();
        if (promotionReduceIds == null || promotionReduceIds.isEmpty()) {
            return map;
        }
        if (enableCache) {
            CacheMapResult<Long, StorePromotionReducePeriodsCacheData> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionReducePeriodsCacheData.class, promotionReduceIds);
            for (Map.Entry<Long, StorePromotionReducePeriodsCacheData> entry : mapResult.getDataMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getList());
            }
            if (mapResult.getNoCacheDataKeysSize() > 0) {
                Map<Long, List<StorePromotionReducePeriod>> map1 = this._getMapInPromotionRebateIdsFromDB(merchantId, storeId, mapResult.getNoCacheDataKeys(), enableSlave);
                if (!map1.isEmpty()) {
                    this._addToCache(map1);
                }
                map.putAll(map1);
            }
            return map;
        }
        return this._getMapInPromotionRebateIdsFromDB(merchantId, storeId, promotionReduceIds, enableSlave);
    }

    private void _addToCache(Map<Long, List<StorePromotionReducePeriod>> map) {
        Map<Long, StorePromotionReducePeriodsCacheData> cacheDataMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StorePromotionReducePeriod>> entry : map.entrySet()) {
            StorePromotionReducePeriodsCacheData cacheData = new StorePromotionReducePeriodsCacheData();
            cacheData.setList(entry.getValue());
            cacheDataMap.put(entry.getKey(), cacheData);
        }
        this.wengerCache.setMulti4Obj(cacheDataMap);
    }

    private Map<Long, List<StorePromotionReducePeriod>> _getMapInPromotionRebateIdsFromDB(int merchantId, long storeId, List<Long> promotionReduceIds, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionReducePeriod> list = this.query.listInValues2(StorePromotionReducePeriod.class,
                "where merchant_id=? and store_id=?", "promotion_reduce_id", "order by tid asc", Lists.newArrayList(merchantId, storeId), promotionReduceIds);
        Map<Long, List<StorePromotionReducePeriod>> map = Maps.newHashMap();
        for (StorePromotionReducePeriod obj : list) {
            List<StorePromotionReducePeriod> sublist = map.get(obj.getPromotionReduceId());
            if (sublist == null) {
                sublist = Lists.newArrayList();
                map.put(obj.getPromotionReduceId(), sublist);
            }
            sublist.add(obj);
        }
        return map;
    }

}
