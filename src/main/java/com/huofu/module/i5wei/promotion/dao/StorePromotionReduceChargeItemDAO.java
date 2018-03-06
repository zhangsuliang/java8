package com.huofu.module.i5wei.promotion.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.promotion.entity.*;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.cache.CacheMapResult;
import huofuhelper.util.cache.WengerCache;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Auto created by i5weitools
 */
@Repository
public class StorePromotionReduceChargeItemDAO extends AbsQueryDAO<StorePromotionReduceChargeItem> {

    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(long promotionReduceId) {
        this.cacheCleaner.add(StorePromotionReduceChargeItemsCacheData.class, promotionReduceId);
    }

    public int deleteByPromotionReduceId(int merchantId, long storeId, long promotionReduceId) {
        this.addCacheCleaner(promotionReduceId);
        return this.query.delete(StorePromotionReduceChargeItem.class,
                "where merchant_id=? and store_id=? and promotion_reduce_id=?",
                new Object[]{merchantId, storeId, promotionReduceId});
    }

    public void deleteAll() {
        this.query.delete(StorePromotionReduceChargeItem.class, null, null);
    }

    public void deleteBatch(List<StorePromotionReduceChargeItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Object[]> olist = Lists.newArrayList();
        Set<Long> reduceIds = Sets.newHashSet();
        for (StorePromotionReduceChargeItem item : items) {
            olist.add(new Object[]{item.getMerchantId(), item.getStoreId(), item.getPromotionReduceId(), item.getChargeItemId()});
            reduceIds.add(item.getPromotionReduceId());
        }
        for (Long rebateId : reduceIds) {
            this.addCacheCleaner(rebateId);
        }
        this.query.batchDelete(StorePromotionReduceChargeItem.class, "where merchant_id=? and store_id=? and promotion_reduce_id=? and charge_item_id=?", olist);

    }

    public Map<Long, List<StorePromotionReduceChargeItem>> getMapInPromotionReduceIds(int merchantId, long storeId, List<Long> promotionReduceIds, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StorePromotionReduceChargeItem>> map = Maps.newHashMap();
        if (promotionReduceIds == null || promotionReduceIds.isEmpty()) {
            return map;
        }
        if (enableCache) {
            CacheMapResult<Long, StorePromotionReduceChargeItemsCacheData> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionReduceChargeItemsCacheData.class, promotionReduceIds);
            for (Map.Entry<Long, StorePromotionReduceChargeItemsCacheData> entry : mapResult.getDataMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getList());
            }
            if (mapResult.getNoCacheDataKeysSize() > 0) {
                Map<Long, List<StorePromotionReduceChargeItem>> map1 = this._getMapInPromotionReduceIdsFromDB(merchantId, storeId, mapResult.getNoCacheDataKeys(), enableSlave);
                if (!map1.isEmpty()) {
                    this._addToCache(map1);
                }
                map.putAll(map1);
            }
            return map;
        }
        return this._getMapInPromotionReduceIdsFromDB(merchantId, storeId, promotionReduceIds, enableSlave);
    }

    private void _addToCache(Map<Long, List<StorePromotionReduceChargeItem>> map) {
        Map<Long, StorePromotionReduceChargeItemsCacheData> cacheDataMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StorePromotionReduceChargeItem>> entry : map.entrySet()) {
            StorePromotionReduceChargeItemsCacheData cacheData = new StorePromotionReduceChargeItemsCacheData();
            cacheData.setList(entry.getValue());
            cacheDataMap.put(entry.getKey(), cacheData);
        }
        this.wengerCache.setMulti4Obj(cacheDataMap);
    }

    private Map<Long, List<StorePromotionReduceChargeItem>> _getMapInPromotionReduceIdsFromDB(int merchantId, long storeId, List<Long> promotionReduceIds, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionReduceChargeItem> list = this.query.listInValues2(StorePromotionReduceChargeItem.class,
                "where merchant_id=? and store_id=?", "promotion_reduce_id", "order by tid asc", Lists.newArrayList(merchantId, storeId), promotionReduceIds);
        Map<Long, List<StorePromotionReduceChargeItem>> map = Maps.newHashMap();
        for (StorePromotionReduceChargeItem obj : list) {
            List<StorePromotionReduceChargeItem> sublist = map.get(obj.getPromotionReduceId());
            if (sublist == null) {
                sublist = Lists.newArrayList();
                map.put(obj.getPromotionReduceId(), sublist);
            }
            sublist.add(obj);
        }
        return map;
    }

    /**
     * 根据满减活动id，获取参加满减活动的收费项目
     */
    public List<StorePromotionReduceChargeItem> getListByReduceIds(int merchantId, long storeId, List<Long> reduceIds) {
        List<StorePromotionReduceChargeItem> list = this.query.listInValues2(StorePromotionReduceChargeItem.class, "where merchant_id=? and store_id=?", "promotion_reduce_id", Lists.newArrayList(merchantId, storeId), reduceIds);
        return list;
    }

    /**
     * 删除指定收费项目下的满减活动
     */
    public void deleteByChargeItemId(int merchantId, long storeId, long chargeItemId, List<Long> reduceIds) {
        if (reduceIds == null || reduceIds.isEmpty()) {
            return;
        }
        List<Object[]> olist = Lists.newArrayList();
        for (long reduceId : reduceIds) {
            olist.add(new Object[]{merchantId, storeId, chargeItemId, reduceId});
        }
        this.query.batchDelete(StorePromotionReduceChargeItem.class, "where merchant_id=? and store_id=? and charge_item_id=? and promotion_reduce_id=?", olist);
    }

    /**
     * 根据收费项目id和折扣活动id集合，查询符合条件的信息
     * @param merchantId 商户id
     * @param storeId 店铺id
     * @param chargeItemId 收费项目id
     * @param reduceIds 满减活动的id集合
     * @return null
     */
    public List<StorePromotionReduceChargeItem> getListByChargeItemIdAndReduceIds(int merchantId, long storeId, long chargeItemId, List<Long> reduceIds) {
        List<StorePromotionReduceChargeItem> list = Lists.newArrayList();
        if (CollectionUtils.isEmpty(reduceIds)) {
            return list;
        }
        list = this.query.listInValues2(StorePromotionReduceChargeItem.class,
                " where merchant_id=? and store_id=? and charge_item_id=? ", "promotion_reduce_id",
                Lists.newArrayList(merchantId, storeId, chargeItemId), reduceIds);
        return list;
    }
}
