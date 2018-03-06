package com.huofu.module.i5wei.promotion.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import halo.query.Query;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.promotion.StorePromotionStatusEnum;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.cache.CacheItem;
import huofuhelper.util.cache.CacheMapResult;
import huofuhelper.util.cache.WengerCache;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 买免活动设置DAO
 * @author Suliang
 *
 */
@SuppressWarnings("all")
@Repository
public class StorePromotionGratisDAO extends AbsQueryDAO<StorePromotionGratis> {
	/**
	 * 缓存设置
	 */
	@Resource
	private WengerCache wengerCache;

	public void setWengerCache(WengerCache wengerCache) {
		this.wengerCache = wengerCache;
	}

	private void addCacheCleaner(StorePromotionGratis storePromotionGratis) {
		this.cacheCleaner.add(StorePromotionRebate.class, storePromotionGratis.getPromotionGratisId());
	}

	@Override
	public void update(StorePromotionGratis storePromotionGratis) {
		this.addCacheCleaner(storePromotionGratis);
		super.update(storePromotionGratis);
	}

	@Override
	public void update(StorePromotionGratis storePromotionGratis, StorePromotionGratis snapshot) {
		this.addCacheCleaner(storePromotionGratis);
		super.update(storePromotionGratis, snapshot);
	}

	public void deleteAll(StorePromotionGratis storePromotionGratis) {
		this.query.delete(storePromotionGratis);
	}

	/**
	 * 查询是否含有重复标题名
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param title  标题
	 * @param promotionGratisId  活动的ID
	 * @param time  有效期
	 * @return
	 */
	public boolean hasDuplicateNameInavaliable(int merchantId, long storeId, String title, long promotionGratisId,long time) {
		String sql = " where merchant_id=? and store_id=? and title=? and promotion_gratis_id!=? and end_time>=? and deleted=?";
		Object[] params = new Object[] { merchantId, storeId, title, promotionGratisId, time, false };
		return this.query.count(StorePromotionGratis.class, sql, params) > 0;
	}

	/**
	 * 根据活动ID获取活动信息
	 * @param merchantId]  商户Id
	 * @param storeId  店铺Id
	 * @param promotionGratisId  活动Id
	 * @return 活动信息
	 * @throws T5weiException
	 */
	public StorePromotionGratis loadById(int merchantId, long storeId, long promotionGratisId, boolean enableSlave,boolean enableCache) throws T5weiException {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		if (enableCache) {
			CacheItem cacheItem = this.wengerCache.get4Obj(StorePromotionGratis.class, promotionGratisId);
			if (cacheItem != null) {
				return cacheItem.getObject();
			}
		}
		StorePromotionGratis storePromotionGratis = this.query.objById(StorePromotionGratis.class, promotionGratisId);
		if (storePromotionGratis == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_INVALID.getValue(), "merchantId["+ merchantId + "] storeId[" + storeId + "] promotionRebateId[" + promotionGratisId + "] invalid");
		}
		if (enableCache) {
			this.wengerCache.set4Obj(promotionGratisId, storePromotionGratis);
		}
		return storePromotionGratis;

	}

	/**
	 * 获取在有效期的活动
	 * @param merchantId  商户Id
	 * @param storeId  店铺Id
	 * @param time  发生时间
	 * @return  买免活动list
	 */
	public List<StorePromotionGratis> getList4Avaliable(int merchantId, long storeId, long time, boolean enableSlave,boolean enableCache) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		String sql = " where merchant_id=? and store_id=? and end_time>=? and deleted=? order by promotion_gratis_id desc";
		Object[] params = new Object[] { merchantId, storeId, time, false };
		return this.query.list(StorePromotionGratis.class, sql, params);
	}

	/**
	 * 获取在有效时间内的活动数量
	 * @param merchantId  商户Id
	 * @param storeId  店铺Id
	 * @param time  发生时间
	 * @return 活动数量
	 */
	public int count4Avaliable(int merchantId, long storeId, long time, int privilegeWay) {
		String sql = " where merchant_id=? and store_id=? and end_time>? and privilege_way=?";
		Object[] params = new Object[] { merchantId, storeId, time, privilegeWay };
		return this.query.count(StorePromotionGratis.class, sql, params);
	}

	/**
	 * 根据活动状态获取活动集合
	 * @param merchantId  商户Id
	 * @param storeId  店铺Id
	 * @param status  活动状态
	 * @param begin  开始位置
	 * @param size  查询数量
	 * @return  活动list
	 */
	public List<StorePromotionGratis> getList4Status(int merchantId, long storeId, int status, int begin, int size) {
		Object[] objs = this.buildSql4Status(merchantId, storeId, status, false, null);
		StringBuilder sb = (StringBuilder) objs[0];
		List<Object> params = (List<Object>) objs[1];
		List<StorePromotionGratis> promotionGratisList = this.query.mysqlList2(StorePromotionGratis.class, sb.toString(), begin, size, params);
		return promotionGratisList;
	}

	/**
	 * 获取不同状态下的获得数量
	 * @param merchantId 商户ID
	 * @param storeId  店铺ID
	 * @param status  状态
	 * @return  
	 */
	public int count4Status(int merchantId, long storeId, int status) {
		Object[] objs = this.buildSql4Status(merchantId, storeId, status, true, null);
		StringBuilder sb = (StringBuilder) objs[0];
		List<StorePromotionGratis> params = (List<StorePromotionGratis>) objs[1];
		return this.query.count2(StorePromotionGratis.class, sb.toString(), params);
	}

	/**
	 * 根据标题获取活动的列表
	 * @param merchantId 商户ID
	 * @param storeId  店铺ID
	 * @param status  状态
	 * @param size  查询数量
	 * @param title  标题
	 * @return
	 */
	 public List<StorePromotionGratis> getListByTitle(int merchantId,long storeId,int status,int size,String title){
			Object[] objs = this.buildSql4Status(merchantId, storeId, status, false, title);
			StringBuilder sb = (StringBuilder) objs[0];
			List<Object> params = (List<Object>) objs[1];
			return  this.query.mysqlList2(StorePromotionGratis.class, sb.toString(), 0, size, params);
	 }
	
	private Object[] buildSql4Status(int merchantId, long storeId, int status, boolean forCount, String title) {
		StringBuilder sb = new StringBuilder(" where merchant_id=? and store_id=? and deleted=? ");
		List<Object> params = Lists.newArrayList();
		params.add(merchantId);
		params.add(storeId);
		params.add(false);
		// 未开始,进行中,已暂停,已结束,未开启
		if (status == StorePromotionStatusEnum.NOT_BEGIN.getValue()) {
			sb.append(" and begin_time>? and paused=?");
			params.add(System.currentTimeMillis());
            params.add(true);
		} else if (status == StorePromotionStatusEnum.DOING.getValue()) {
			sb.append(" and begin_time<? and end_time>? and paused=?");
			params.add(System.currentTimeMillis());
			params.add(System.currentTimeMillis());
			params.add(false);
		} else if (status == StorePromotionStatusEnum.NOT_OPENED.getValue()) {
			sb.append(" and begin_time>? and paused=?");
		    params.add(System.currentTimeMillis());
			params.add(false);
		} else if (status == StorePromotionStatusEnum.PAUSED.getValue()) {
			sb.append(" and begin_time<? and paused=?");
            params.add(System.currentTimeMillis());
			params.add(true);
		} else if (status == StorePromotionStatusEnum.ENDED.getValue()) {
			sb.append(" and end_time<?");
			params.add(System.currentTimeMillis());
		}
		if (DataUtil.isNotEmpty(title)) {
			sb.append(" and title like ?");
			params.add("%" + title + "%");
		}
	if (!forCount) {
			sb.append(" order by promotion_gratis_id desc");
		}
		return new Object[] { sb, params };
	}
  
	/**
	 * 获取正在进行的活动列表
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param time  当前时间
	 * @return
	 */
	public List<StorePromotionGratis> getList4Doing(int merchantId, long storeId, long time, boolean enableSlave,boolean enableCache) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		String sql = " where merchant_id=? and store_id=? and paused=? and begin_time<=? and end_time>=? and deleted=? order by promotion_gratis_id desc";
		Object[] params = new Object[] { merchantId, storeId, false, time, time, false };
		List<StorePromotionGratis> list = this.query.list(StorePromotionGratis.class, sql, params);
		return list;
	}
    
	/**
	 * 根据活动Ids获取活动列表
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID
	 * @param chargeItemIds  收费项目ID集合
	 * @return
	 */
	public List<StorePromotionGratis> getStorePromotionGratisListByIds(int merchantId, long storeId,List<Long> promotionGratisIds, int privilegeWay) {
		if (promotionGratisIds == null || promotionGratisIds.size()<=0) {
			return Lists.newArrayList();
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" where merchant_id=? and store_id=? and deleted=? and privilege_way=?");
		sql.append(" and ").append(Query.createInSql("promotion_gratis_id", promotionGratisIds.size()));
		List<Object> params = Lists.newArrayList(merchantId, storeId, false, privilegeWay);
		for (int i = 0; i < promotionGratisIds.size(); i++) {
			params.add(promotionGratisIds.get(i));
		}
		return this.query.list(StorePromotionGratis.class, sql.toString(), params.toArray());
	}

	/**
	 * 批量更新买免活动为指定收费项目
	 * @param merchantId 商户id
	 * @param storeId 店铺id
	 * @param gratisIds 买免活动id集合
	 */
	public void batchUpdatePromotionGratisIsSelectChargeItem(int merchantId, long storeId, List<Long> gratisIds) {
		if (gratisIds == null || gratisIds.isEmpty()){
			return;
		}
		List<Object[]> params = new ArrayList<Object[]>();
		for (Long gratisId : gratisIds) {
			params.add(new Object[]{true, gratisId});
		}
		this.query.batchUpdate(StorePromotionGratis.class, "set select_charge_item=? where promotion_gratis_id=? ", params);
	}
	/**
	 * 根据ids获取map
	 *
	 * @param merchantId
	 * @param storeId
	 * @param promotionGratisIds
	 * @return
	 */
	public Map<Long, StorePromotionGratis> getMapInIds(int merchantId, long storeId, List<Long> promotionGratisIds) {
        Map<Long, StorePromotionGratis> map = Maps.newHashMap();
        if (promotionGratisIds == null || promotionGratisIds.isEmpty()) {
            return map;
        }
        CacheMapResult<Long, StorePromotionGratis> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionGratis.class, promotionGratisIds);
        Map<Long, StorePromotionGratis> cachedMap = mapResult.getDataMap();
        map.putAll(cachedMap);
        if (mapResult.getNoCacheDataKeysSize() > 0) {
            DALStatus.setSlaveMode();
            Map<Long, StorePromotionGratis> dataMap = this.query.map2(StorePromotionGratis.class, "where merchant_id=? and store_id=?","promotion_gratis_id", Lists.newArrayList(merchantId, storeId), mapResult.getNoCacheDataKeys());
            map.putAll(dataMap);
        }
        return map;
    }

	/**
	 * 根据活动类型，获取未结束的买免活动
	 * @param merchantId  商户Id
	 * @param storeId  店铺Id
	 * @param privilegeWay 活动类型
	 * @param time  时间
	 * @return 查询结果
	 */
	public List<StorePromotionGratis> getStorePromotionGratisListByPrivilegeWay(int merchantId, long storeId, int privilegeWay, long time) {
		String sql = " where merchant_id=? and store_id=? and privilege_way=? and end_time>? and deleted=? ";
		Object[] params = new Object[] { merchantId, storeId, privilegeWay, time, false};
		return this.query.list(StorePromotionGratis.class, sql, params);
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
	 * 根据收费项目Id的集合，获取map集合
	 * @param merchantId
	 * @param storeId
	 * @param chargeItemIds
	 * @return
	 */
	public Map<Long,List<StorePromotionGratis>>  getAllStorePromotionGratisByChargeItemId(int merchantId, long storeId, List<Long> chargeItemIds){
        String sql= " where merchant_id=? and store_id=? and charge_item_id in ? ";
		Object[] params = new Object[] { merchantId, storeId,chargeItemIds};
		return null;
	}

}
