package com.huofu.module.i5wei.promotion.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebateChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebateChargeItemsCacheData;
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
public class StorePromotionRebateChargeItemDAO extends AbsQueryDAO<StorePromotionRebateChargeItem> {

    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(long promotionRebateId) {
        this.cacheCleaner.add(StorePromotionRebateChargeItemsCacheData.class, promotionRebateId);
    }

    public int deleteByPromotionRebateId(int merchantId, long storeId, long promotionRebateId) {
        this.addCacheCleaner(promotionRebateId);
        return this.query.delete(StorePromotionRebateChargeItem.class,
                "where merchant_id=? and store_id=? and promotion_rebate_id=?",
                new Object[]{merchantId, storeId, promotionRebateId});
    }

    public void deleteAll() {
        this.query.delete(StorePromotionRebateChargeItem.class, null, null);
    }

    public void deleteBatch(List<StorePromotionRebateChargeItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Object[]> olist = Lists.newArrayList();
        Set<Long> rebateIds = Sets.newHashSet();
        for (StorePromotionRebateChargeItem item : items) {
            olist.add(new Object[]{item.getMerchantId(), item.getStoreId(), item.getPromotionRebateId(), item.getChargeItemId()});
            rebateIds.add(item.getPromotionRebateId());
        }
        for (Long rebateId : rebateIds) {
            this.addCacheCleaner(rebateId);
        }
        this.query.batchDelete(StorePromotionRebateChargeItem.class, "where merchant_id=? and store_id=? and promotion_rebate_id=? and charge_item_id=?", olist);
    }

    public Map<Long, List<StorePromotionRebateChargeItem>> getMapInPromotionRebateIds(int merchantId, long storeId, List<Long> promotionRebateIds, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StorePromotionRebateChargeItem>> map = Maps.newHashMap();
        if (promotionRebateIds == null || promotionRebateIds.isEmpty()) {
            return map;
        }
        if (enableCache) {
            CacheMapResult<Long, StorePromotionRebateChargeItemsCacheData> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionRebateChargeItemsCacheData.class, promotionRebateIds);
            for (Map.Entry<Long, StorePromotionRebateChargeItemsCacheData> entry : mapResult.getDataMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getList());
            }
            if (mapResult.getNoCacheDataKeysSize() > 0) {
                Map<Long, List<StorePromotionRebateChargeItem>> map1 = this._getMapInPromotionRebateIdsFromDB(merchantId, storeId, mapResult.getNoCacheDataKeys(), enableSlave);
                if (!map1.isEmpty()) {
                    this._addToCache(map1);
                }
                map.putAll(map1);
            }
            return map;
        }
        return this._getMapInPromotionRebateIdsFromDB(merchantId, storeId, promotionRebateIds, enableSlave);
    }

    private void _addToCache(Map<Long, List<StorePromotionRebateChargeItem>> map) {
        Map<Long, StorePromotionRebateChargeItemsCacheData> cacheDataMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StorePromotionRebateChargeItem>> entry : map.entrySet()) {
            StorePromotionRebateChargeItemsCacheData cacheData = new StorePromotionRebateChargeItemsCacheData();
            cacheData.setList(entry.getValue());
            cacheDataMap.put(entry.getKey(), cacheData);
        }
        this.wengerCache.setMulti4Obj(cacheDataMap);
    }

    private Map<Long, List<StorePromotionRebateChargeItem>> _getMapInPromotionRebateIdsFromDB(int merchantId, long storeId, List<Long> promotionRebateIds, boolean enableSlave) {
        Map<Long, List<StorePromotionRebateChargeItem>> map = Maps.newHashMap();
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionRebateChargeItem> list = this.query.listInValues2(StorePromotionRebateChargeItem.class,
                "where merchant_id=? and store_id=?", "promotion_rebate_id", "order by tid asc", Lists.newArrayList(merchantId, storeId), promotionRebateIds);
        for (StorePromotionRebateChargeItem obj : list) {
            List<StorePromotionRebateChargeItem> sublist = map.get(obj.getPromotionRebateId());
            if (sublist == null) {
                sublist = Lists.newArrayList();
                map.put(obj.getPromotionRebateId(), sublist);
            }
            sublist.add(obj);
        }
        return map;
    }

    /**
     * 根据折扣活动id，获取参与折扣活动的收费项目
     */
    public List<StorePromotionRebateChargeItem> getListByRebateIds(int merchantId, long storeId, List<Long> rebateIds) {
        List<StorePromotionRebateChargeItem> list =
                this.query.listInValues2(StorePromotionRebateChargeItem.class,
                        "where merchant_id=? and store_id=?", "promotion_rebate_id",
                        Lists.newArrayList(merchantId, storeId), rebateIds);
        return list;
    }

    /**
     * 删除指定收费项目下的折扣活动
     * @param merchantId 商户id
     * @param storeId 店铺id
     * @param chargeItemId 收费项目id
     * @param rebateIds 折扣活动的id集合
     * @return null
     */
    public void deleteByChargeItemId(int merchantId, long storeId, long chargeItemId, List<Long> rebateIds) {
        if (CollectionUtils.isEmpty(rebateIds)) {
            return;
        }
        List<Object[]> olist = Lists.newArrayList();
        for (long rebateId : rebateIds) {
            olist.add(new Object[]{merchantId, storeId, chargeItemId, rebateId});
        }
        String sql = "where merchant_id=? and store_id=? and charge_item_id=? and promotion_rebate_id=?";
        this.query.batchDelete(StorePromotionRebateChargeItem.class, sql, olist);
    }

    /**
     * 根据收费项目id和折扣活动id集合，查询符合条件的信息
     * @param merchantId 商户id
     * @param storeId 店铺id
     * @param chargeItemId 收费项目id
     * @param rebateIds 折扣活动的id集合
     * @return null
     */
    public List<StorePromotionRebateChargeItem> getListByChargeItemIdAndRebateIds(int merchantId, long storeId, long chargeItemId, List<Long> rebateIds) {
        List<StorePromotionRebateChargeItem> list = Lists.newArrayList();
        if (CollectionUtils.isEmpty(rebateIds)) {
            return list;
        }
        list = this.query.listInValues2(StorePromotionRebateChargeItem.class,
                " where merchant_id=? and store_id=? and charge_item_id=? ", "promotion_rebate_id",
                Lists.newArrayList(merchantId, storeId, chargeItemId), rebateIds);
        return list;
    }
}
