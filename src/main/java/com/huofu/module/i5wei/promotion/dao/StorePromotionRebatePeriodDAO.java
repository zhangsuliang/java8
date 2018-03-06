package com.huofu.module.i5wei.promotion.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.promotion.entity.*;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.cache.CacheItem;
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
public class StorePromotionRebatePeriodDAO extends AbsQueryDAO<StorePromotionRebatePeriod> {

    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(long promotionRebateId) {
        this.cacheCleaner.add(StorePromotionRebatePeriodsCacheData.class, promotionRebateId);
    }

    public int deleteByPromotionRebateId(int merchantId, long storeId, long promotionRebateId) {
        this.addCacheCleaner(promotionRebateId);
        return this.query.delete(StorePromotionRebatePeriod.class,
                "where merchant_id=? and store_id=? and promotion_rebate_id=?",
                new Object[]{merchantId, storeId, promotionRebateId});
    }

    public void deleteAll() {
        this.query.delete(StorePromotionRebatePeriod.class, null, null);
    }

    public List<StorePromotionRebatePeriod> getListByPromotionRebateId(
            int merchantId, long storeId, long promotionRebateId, boolean enableSlave, boolean enableCache) {
        if (enableCache) {
            CacheItem item = this.wengerCache.get4Obj(StorePromotionRebatePeriodsCacheData.class, promotionRebateId);
            if (item != null) {
                StorePromotionRebatePeriodsCacheData cacheData = item.getObject();
                return cacheData.getList();
            }
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionRebatePeriod> list = this.query.list(StorePromotionRebatePeriod.class,
                "where merchant_id=? and store_id=? and promotion_rebate_id=? order by tid asc",
                new Object[]{merchantId, storeId, promotionRebateId});
        if (enableCache && list.size() > 0) {
            StorePromotionRebatePeriodsCacheData cacheData = new StorePromotionRebatePeriodsCacheData();
            cacheData.setList(list);
            this.wengerCache.set4Obj(promotionRebateId, cacheData);
        }
        return list;
    }

    public Map<Long, List<StorePromotionRebatePeriod>> getMapInPromotionRebateIds(
            int merchantId, long storeId, List<Long> promotionRebateIds, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StorePromotionRebatePeriod>> map = Maps.newHashMap();
        if (promotionRebateIds == null || promotionRebateIds.isEmpty()) {
            return map;
        }
        if (enableCache) {
            CacheMapResult<Long, StorePromotionRebatePeriodsCacheData> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionRebatePeriodsCacheData.class, promotionRebateIds);
            for (Map.Entry<Long, StorePromotionRebatePeriodsCacheData> entry : mapResult.getDataMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getList());
            }
            if (mapResult.getNoCacheDataKeysSize() > 0) {
                Map<Long, List<StorePromotionRebatePeriod>> map1 = this._getMapInPromotionRebateIdsFromDB(merchantId, storeId, mapResult.getNoCacheDataKeys(), enableSlave);
                if (!map1.isEmpty()) {
                    this._addToCache(map1);
                }
                map.putAll(map1);
            }
            return map;
        }
        return this._getMapInPromotionRebateIdsFromDB(merchantId, storeId, promotionRebateIds, enableSlave);
    }

    private void _addToCache(Map<Long, List<StorePromotionRebatePeriod>> map) {
        Map<Long, StorePromotionRebatePeriodsCacheData> cacheDataMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StorePromotionRebatePeriod>> entry : map.entrySet()) {
            StorePromotionRebatePeriodsCacheData cacheData = new StorePromotionRebatePeriodsCacheData();
            cacheData.setList(entry.getValue());
            cacheDataMap.put(entry.getKey(), cacheData);
        }
        this.wengerCache.setMulti4Obj(cacheDataMap);
    }

    private Map<Long, List<StorePromotionRebatePeriod>> _getMapInPromotionRebateIdsFromDB(int merchantId, long storeId, List<Long> promotionRebateIds, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionRebatePeriod> list = this.query.listInValues2(StorePromotionRebatePeriod.class,
                "where merchant_id=? and store_id=?", "promotion_rebate_id", "order by tid asc", Lists.newArrayList(merchantId, storeId), promotionRebateIds);
        Map<Long, List<StorePromotionRebatePeriod>> map = Maps.newHashMap();
        for (StorePromotionRebatePeriod obj : list) {
            List<StorePromotionRebatePeriod> sublist = map.get(obj.getPromotionRebateId());
            if (sublist == null) {
                sublist = Lists.newArrayList();
                map.put(obj.getPromotionRebateId(), sublist);
            }
            sublist.add(obj);
        }
        return map;
    }
}
