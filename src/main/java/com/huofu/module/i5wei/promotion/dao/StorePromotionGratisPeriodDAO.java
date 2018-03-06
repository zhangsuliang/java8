package com.huofu.module.i5wei.promotion.dao;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisPeriod;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisPeriodsCacheData;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.cache.CacheItem;
import huofuhelper.util.cache.CacheMapResult;
import huofuhelper.util.cache.WengerCache;

/**
 * 
 * @author Suliang
 *
 */
@SuppressWarnings("all")
@Repository
public class StorePromotionGratisPeriodDAO extends AbsQueryDAO<StorePromotionGratisPeriod> {
	@Resource
	private WengerCache wengerCache;

	public void setWengerCache(WengerCache wengerCache) {
		this.wengerCache = wengerCache;
	}

	private void addCacheCleaner(long promotionGratisId) {
		this.cacheCleaner.add(StorePromotionGratisPeriodsCacheData.class, promotionGratisId);
	}

	/**
	 * 删除买赠活动周期表中活动ID为#{promotion_gratis_id}的数据
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param promotionGratisId 活动ID
	 * @return
	 */
	public int deleteByPromotionGratisId(int merchantId, long storeId, long promotionGratisId) {
		this.addCacheCleaner(promotionGratisId);
		String sql = "where merchant_id=? and store_id=? and promotion_gratis_id=?";
		Object[] params = new Object[] { merchantId, storeId, promotionGratisId };
		return this.query.delete(StorePromotionGratisPeriod.class, sql, params);
	}

	/**
	 * 删除活动周期表中所有的活动
	 */
	public void deleteAll() {
		this.query.delete(StorePromotionGratisPeriod.class, null, null);
	}

	/**
	 * 根据活动ID获取其活动周期列表
	 * @param merchantId
	 * @param storeId
	 * @param promotionGratisId
	 * @return
	 */
	public List<StorePromotionGratisPeriod> getListByStorePromotionGratisId(int merchantId, long storeId,long promotionGratisId, boolean enableSlave, boolean enableCache) {
		if (enableCache) {
			CacheItem item = this.wengerCache.get4Obj(StorePromotionGratisPeriodsCacheData.class, promotionGratisId);
			if (item != null) {
				StorePromotionGratisPeriodsCacheData cacheData = item.getObject();
				return cacheData.getList();
			}
		}
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		String sql = "where merchant_id=? and store_id=? and promotion_gratis_id=?";
		Object[] params = new Object[] { merchantId, storeId, promotionGratisId };
		List<StorePromotionGratisPeriod> gratisPeriods = this.query.list(StorePromotionGratisPeriod.class, sql, params);
		if (gratisPeriods.size() > 0 && enableCache) {
			StorePromotionGratisPeriodsCacheData cacheData = new StorePromotionGratisPeriodsCacheData();
			cacheData.setList(gratisPeriods);
			this.wengerCache.set4Obj(promotionGratisId, cacheData);
		}
		return gratisPeriods;
	}
	
	public Map<Long, List<StorePromotionGratisPeriod>> getMapInStorePromotionGratisIds(int merchantId, long storeId, List<Long> promotionGratisIds, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StorePromotionGratisPeriod>> map = Maps.newHashMap();
        if (promotionGratisIds == null || promotionGratisIds.isEmpty()) {
            return map;
        }
        if (enableCache) {
            CacheMapResult<Long, StorePromotionGratisPeriodsCacheData> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionGratisPeriodsCacheData.class, promotionGratisIds);
            for (Map.Entry<Long, StorePromotionGratisPeriodsCacheData> entry : mapResult.getDataMap().entrySet()) {
                map.put(entry.getKey(), entry.getValue().getList());
            }
            if (mapResult.getNoCacheDataKeysSize() > 0) {
                Map<Long, List<StorePromotionGratisPeriod>> map1 = this._getMapInStorePromotionGratisIdsFromDB(merchantId, storeId, mapResult.getNoCacheDataKeys(), enableSlave);
                if (!map1.isEmpty()) {
                    this._addToCache(map1);
                }
                map.putAll(map1);
            }
            return map;
        }
        return this._getMapInStorePromotionGratisIdsFromDB(merchantId, storeId, promotionGratisIds, enableSlave);
    }

	private void _addToCache(Map<Long, List<StorePromotionGratisPeriod>> map) {

        Map<Long, StorePromotionGratisPeriodsCacheData> cacheDataMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StorePromotionGratisPeriod>> entry : map.entrySet()) {
            StorePromotionGratisPeriodsCacheData cacheData = new StorePromotionGratisPeriodsCacheData();
            cacheData.setList(entry.getValue());
            cacheDataMap.put(entry.getKey(), cacheData);
        }
        this.wengerCache.setMulti4Obj(cacheDataMap);
    
		
	}

	private Map<Long, List<StorePromotionGratisPeriod>> _getMapInStorePromotionGratisIdsFromDB(int merchantId, long storeId,List<Long> promotionGratisIds, boolean enableSlave){
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		String sql1 = "where merchant_id=? and store_id=?";
		String sql2 = "promotion_gratis_id";
		String sql3 = "order by tid asc";
		List params = Lists.newArrayList(merchantId, storeId);
		List<StorePromotionGratisPeriod> list = this.query.listInValues2(StorePromotionGratisPeriod.class, sql1, sql2,
				sql3, params, promotionGratisIds);
		Map<Long, List<StorePromotionGratisPeriod>> map = Maps.newHashMap();
		for (StorePromotionGratisPeriod gratisPeriod : list) {
			List<StorePromotionGratisPeriod> sublist = map.get(gratisPeriod.getPromotionGratisId());
			if (sublist == null) {
				sublist = Lists.newArrayList();
				map.put(gratisPeriod.getPromotionGratisId(), sublist);
			}
			sublist.add(gratisPeriod);
		}
		return map;
	}

}
