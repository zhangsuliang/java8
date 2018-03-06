package com.huofu.module.i5wei.promotion.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;

import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebateChargeItem;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisChargeItemsCacheData;

import halo.query.Query;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.cache.CacheMapResult;
import huofuhelper.util.cache.WengerCache;

/**
 * 买赠活动收费项目白名单设置DAO
 *
 * @author Suliang
 */

@SuppressWarnings("all")
@Repository
public class StorePromotionGratisChargeItemDAO extends AbsQueryDAO<StorePromotionGratisChargeItem> {

    /**
     * 缓存设置
     */
    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(long storePromotionGratisId) {
        this.cacheCleaner.add(StorePromotionGratisChargeItemsCacheData.class, storePromotionGratisId);
    }

    /**
     * 删除活动收费项目
     *
     * @param merchantId        商户ID
     * @param storeId           店铺ID
     * @param promotionGratisId 活动ID
     * @return
     */
    public int deleteStorePromotionGratisId(int merchantId, long storeId, long promotionGratisId) {
        this.addCacheCleaner(promotionGratisId);
        String sql = "where merchant_id=? and store_id=? and promotion_gratis_id=?";
        Object[] params = new Object[]{merchantId, storeId, promotionGratisId};
        return this.query.delete(StorePromotionGratisChargeItem.class, sql, params);
    }

    /**
     * 删除所有活动的收费项目
     */
    public void deleteAll() {
        this.query.delete(StorePromotionGratisChargeItem.class, null, null);
    }

    /**
     * 批量删除参加活动的收费项目
     *
     * @param items 删除的收费项目列表
     */
    public void deleteBatch(List<StorePromotionGratisChargeItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Object[]> objList = Lists.newArrayList();
        Set<Long> promotionGratisIds = Sets.newHashSet();
        for (StorePromotionGratisChargeItem item : items) {
            objList.add(new Object[]{item.getMerchantId(), item.getStoreId(), item.getPromotionGratisId(),item.getChargeItemId()});
            promotionGratisIds.add(item.getPromotionGratisId());
        }
        for (Long promotionGratisId : promotionGratisIds) {
            this.addCacheCleaner(promotionGratisId);
        }
        String sql = "where merchant_id=? and store_id=? and promotion_gratis_id=? and charge_item_id=?";
        this.query.batchDelete(StorePromotionGratisChargeItem.class, sql, objList);
    }

    /**
     * 根据活动ID获取对应的收费项目列表，并以活动ID为key，收费项目列表为value，返回Map
     *
     * @param merchantId         商户ID
     * @param storeId            店铺ID
     * @param promotionGratisIds 活动ID列表
     * @return
     */
    public Map<Long, List<StorePromotionGratisChargeItem>> getMapInStorePromotionGratisIds(int merchantId, long storeId,List<Long> promotionGratisIds, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StorePromotionGratisChargeItem>> map = Maps.newHashMap();
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        String sql1 = "where merchant_id=? and store_id=?";
        String sql2 = "promotion_gratis_id";
        String sql3 = "order by t_id desc";
        List params = Lists.newArrayList(merchantId, storeId);
        List<StorePromotionGratisChargeItem> items = this.query.listInValues2(StorePromotionGratisChargeItem.class,sql1, sql2, sql3, params, promotionGratisIds);
        for (StorePromotionGratisChargeItem item : items) {
            long promotionGratisId = item.getPromotionGratisId();
            List<StorePromotionGratisChargeItem> itemList = map.get(promotionGratisId);
            if (itemList == null) {
                itemList = Lists.newArrayList();
                map.put(promotionGratisId, itemList);
            }
            itemList.add(item);
        }
        return map;
    
    }


    private void _addToCache(Map<Long, List<StorePromotionGratisChargeItem>> map) {
        Map<Long, StorePromotionGratisChargeItemsCacheData> cacheDataMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StorePromotionGratisChargeItem>> entry : map.entrySet()) {
            StorePromotionGratisChargeItemsCacheData cacheData = new StorePromotionGratisChargeItemsCacheData();
            cacheData.setList(entry.getValue());
            cacheDataMap.put(entry.getKey(), cacheData);
        }
        this.wengerCache.setMulti4Obj(cacheDataMap);
    }

    /**
     * 根据收费项目ID集合获取活动的ID集合
     *
     * @param merchantId    商户ID
     * @param storeId       店铺ID
     * @param chargeItemIds 收费项目ID集合
     * @return
     */
    public List<Long> getStorePromotionGratisIdsByChargeItemIds(int merchantId, long storeId, List<Long> chargeItemIds,boolean enabledSlave) {
        List<Long> promotionGratisIds = Lists.newArrayList();
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return Lists.newArrayList();
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT  promotion_gratis_id from huofu_5wei.tb_store_promotion_gratis_charge_item ");
        sql.append(" where merchant_id=? and store_id=?");
        sql.append(" and ").append(Query.createInSql("charge_item_id", chargeItemIds.size()));
        // 根据chargeItemIds的长度设置对应的参数
        List<Object> params = Lists.newArrayList(merchantId, storeId);
        for (int i = 0; i < chargeItemIds.size(); i++) {
            params.add(chargeItemIds.get(i));
        }
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        for (Map<String, Object> map : mapList) {
            long promotionGratisId = Long.parseLong(map.get("promotion_gratis_id") + "");
            promotionGratisIds.add(promotionGratisId);
        }
        return promotionGratisIds;
    }

    /**
     * 根据买免活动id，获取参加买免活动的收费项目
     */
    public List<StorePromotionGratisChargeItem> getListByGratisIds(int merchantId, long storeId, List<Long> promotionGratisIds) {
    	String sql1="where merchant_id=? and store_id=?";
    	String sql2= "promotion_gratis_id";
        return  this.query.listInValues2(StorePromotionGratisChargeItem.class, sql1,sql2, Lists.newArrayList(merchantId, storeId), promotionGratisIds);
    }

    /**
     * 删除指定收费项目下的买免活动
     */
    public void deleteByChargeItemId(int merchantId, long storeId, long chargeItemId, List<Long> promotionGratisIds) {
        if (promotionGratisIds == null || promotionGratisIds.isEmpty()) {
            return;
        }
        String sql="where merchant_id=? and store_id=? and charge_item_id=? and promotion_gratis_id=?";
        List<Object[]> olist = Lists.newArrayList();
        for (long gratis : promotionGratisIds) {
            olist.add(new Object[]{merchantId, storeId, chargeItemId, gratis});
        }
        this.query.batchDelete(StorePromotionGratisChargeItem.class, sql,olist);
    }

    /**
     * 根据收费项目id和折扣活动id集合，查询符合条件的信息
     * @param merchantId 商户id
     * @param storeId 店铺id
     * @param chargeItemId 收费项目id
     * @param gratisIds 买免活动的id集合
     * @return null
     */
    public List<StorePromotionGratisChargeItem> getListByChargeItemIdAndGratisIds(int merchantId, long storeId, long chargeItemId, List<Long> gratisIds) {
        List<StorePromotionGratisChargeItem> list = Lists.newArrayList();
        if (CollectionUtils.isEmpty(gratisIds)) {
            return list;
        }
        list = this.query.listInValues2(StorePromotionGratisChargeItem.class,
                " where merchant_id=? and store_id=? and charge_item_id=? ", "promotion_gratis_id",Lists.newArrayList(merchantId, storeId, chargeItemId), gratisIds);
        return list;
    }

    /**
     * 根据起始时间获取交集活动列表
     * @param merchantId 商户Id
     * @param storeId 店铺Id
     * @param privilegeWay  优惠方式
     * @param beginTime  开始时间
     * @param endTime 结束时间
     * @return
     */
   public List<StorePromotionGratis> getStorePromotionGratisIntersectTime(int merchantId, long storeId, int privilegeWay, long beginTime, long endTime){
        String sql=" where merchant_id=? and store_id=? and privilege_way=? and paused=? and (begin_time between ? and ? or end_time between ? and ? or (begin_time>=? and end_time<=?) ) and deleted=? ";
		Object[] params=new Object[]{merchantId,storeId,privilegeWay,false,beginTime,endTime,beginTime,endTime,beginTime,endTime,false};
		return this.query.list(StorePromotionGratis.class,sql,params);
	}

    /**
     * 根据收费项目id的集合获取收费新项目列表
     * @param merchantId  商户ID
     * @param storeId  店铺ID
     * @param chargeItemIds 收费项目列表
     * @return
     */
	public List<Map<String, Object>>  getGratisChargeItems(int merchantId, long storeId,List<Long> chargeItemIds){
          List<Long> promotionGratisIds = Lists.newArrayList();
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return Lists.newArrayList();
        }
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT  * from huofu_5wei.tb_store_promotion_gratis_charge_item ");
        sql.append(" where merchant_id=? and store_id=?");
        sql.append(" and ").append(Query.createInSql("charge_item_id", chargeItemIds.size()));
        // 根据chargeItemIds的长度设置对应的参数
        List<Object> params = Lists.newArrayList(merchantId, storeId);
        for (int i = 0; i < chargeItemIds.size(); i++) {
            params.add(chargeItemIds.get(i));
        }
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        return mapList;
    }
}
