package com.huofu.module.i5wei.promotion.dao;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
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
 * Auto created by i5weitools
 */
@SuppressWarnings("unchecked")
@Repository
public class StorePromotionRebateDAO extends AbsQueryDAO<StorePromotionRebate> {

    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(StorePromotionRebate storePromotionRebate) {
        this.cacheCleaner.add(StorePromotionRebate.class, storePromotionRebate.getPromotionRebateId());
    }

    @Override
    public void update(StorePromotionRebate storePromotionRebate) {
        this.addCacheCleaner(storePromotionRebate);
        super.update(storePromotionRebate);

    }

    @Override
    public void update(StorePromotionRebate storePromotionRebate, StorePromotionRebate snapshot) {
        this.addCacheCleaner(storePromotionRebate);
        super.update(storePromotionRebate, snapshot);
    }

    public void deleteAll() {
        this.query.delete(StorePromotionRebate.class, null, null);
    }

    /**
     * 查找有效的活动中标题重名的活动，在未结束的数据中查找
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param title      标题
     * @param exceptedId 忽略的活动id
     * @param beginTime  有效期开始时间
     * @return true:有标题重名的活动
     */
    public boolean hasDuplicateNameInavaliable(int merchantId, long storeId, String title, long exceptedId, long beginTime) {
        int val = this.query.count(StorePromotionRebate.class,
                "where merchant_id=? and store_id=? and title=? and promotion_rebate_id!=? and end_time>=? and deleted=?",
                new Object[]{merchantId, storeId, title, exceptedId, beginTime, false});
        return val > 0;
    }

    public StorePromotionRebate loadById(int merchantId, long storeId, long promotionRebateId, boolean enableSlave, boolean enableCache) throws T5weiException {
        if (enableCache) {
            CacheItem item = this.wengerCache.get4Obj(StorePromotionRebate.class, promotionRebateId);
            if (item != null) {
                return item.getObject();
            }
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        StorePromotionRebate obj = this.query.objById(StorePromotionRebate.class, promotionRebateId);
        if (obj == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_INVALID.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] promotionRebateId[" + promotionRebateId + "] invalid");
        }
        if (enableCache) {
            this.wengerCache.set4Obj(promotionRebateId, obj);
        }
        return obj;
    }

    /**
     * 获得在有效期的活动
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param time       发生时间
     * @return 活动list
     */
    public List<StorePromotionRebate> getList4Doing(int merchantId, long storeId, long time, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionRebate> list = this.query.list(StorePromotionRebate.class,
                "where merchant_id=? and store_id=? and paused=? and begin_time<=? and end_time>=? and deleted=? order by promotion_rebate_id desc",
                new Object[]{merchantId, storeId, false, time, time, false});
        return list;
    }

    /**
     * 获得在有效期的活动
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param time       发生时间
     * @return 活动list
     */
    public List<StorePromotionRebate> getList4Avaliable(int merchantId, long storeId, long time, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionRebate> list = this.query.list(StorePromotionRebate.class,
                "where merchant_id=? and store_id=? and end_time>=? and deleted=? order by promotion_rebate_id desc",
                new Object[]{merchantId, storeId, time, false});
        return list;
    }

    public int count4Avaliable(int merchantId, long storeId, long time) {
        return this.query.count(StorePromotionRebate.class,
                "where merchant_id=? and store_id=? and end_time>=? and deleted=?",
                new Object[]{merchantId, storeId, time, false});
    }

    public List<StorePromotionRebate> getList4Status(int merchantId, long storeId, int status, int begin, int size) {
        Object[] objs = this.buildList4Status(merchantId, storeId, status, false, null);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.mysqlList2(StorePromotionRebate.class, sb.toString(), begin, size, params);
    }

    public int count4Status(int merchantId, long storeId, int status) {
        Object[] objs = this.buildList4Status(merchantId, storeId, status, true, null);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.count2(StorePromotionRebate.class, sb.toString(), params);
    }

    public Map<Long, StorePromotionRebate> getMapInIds(int merchantId, long storeId, List<Long> promotionRebateIds) {
        Map<Long, StorePromotionRebate> map = Maps.newHashMap();
        if (promotionRebateIds == null || promotionRebateIds.isEmpty()) {
            return map;
        }
        CacheMapResult<Long, StorePromotionRebate> mapResult = this.wengerCache.getMulti4MapResult4Obj(StorePromotionRebate.class, promotionRebateIds);
        Map<Long, StorePromotionRebate> cachedMap = mapResult.getDataMap();
        map.putAll(cachedMap);
        if (mapResult.getNoCacheDataKeysSize() > 0) {
            DALStatus.setSlaveMode();
            Map<Long, StorePromotionRebate> dataMap = this.query.map2(StorePromotionRebate.class, "where merchant_id=? and store_id=?",
                    "promotion_rebate_id", Lists.newArrayList(merchantId, storeId), mapResult.getNoCacheDataKeys());
            map.putAll(dataMap);
        }
        return map;
    }


    private Object[] buildList4Status(int merchantId, long storeId, int status, boolean forCount, String title) {
        StringBuilder sb = new StringBuilder("where merchant_id=? and store_id=? and deleted=?");
        List<Object> params = Lists.newArrayList();
        params.add(merchantId);
        params.add(storeId);
        params.add(false);
        if (status == StorePromotionStatusEnum.ENDED.getValue()) {
            sb.append(" and end_time<?");
            params.add(System.currentTimeMillis());
        } else if (status == StorePromotionStatusEnum.NOT_BEGIN.getValue()) {
            sb.append(" and begin_time>?");
            params.add(System.currentTimeMillis());
        } else if (status == StorePromotionStatusEnum.DOING.getValue()) {
            sb.append(" and begin_time<? and end_time>? and paused=?");
            params.add(System.currentTimeMillis());
            params.add(System.currentTimeMillis());
            params.add(false);
        } else if (status == StorePromotionStatusEnum.PAUSED.getValue()) {
            sb.append(" and paused=?");
            params.add(true);
        }
        if (DataUtil.isNotEmpty(title)) {
            sb.append(" and title like ?");
            params.add("%" + title + "%");
        }
        if (!forCount) {
            sb.append(" order by promotion_rebate_id desc");
        }
        return new Object[]{sb, params};
    }

    public List<StorePromotionRebate> getListByTitle(int merchantId, long storeId, int status, String title, int size) {
        Object[] objs = this.buildList4Status(merchantId, storeId, status, false, title);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.mysqlList2(StorePromotionRebate.class, sb.toString(), 0, size, params);
    }

	public List<StorePromotionRebate> getAll() {
		return this.query.list(StorePromotionRebate.class, null, null);
	}

	/**
	 * 批量更新折扣活动为指定收费项目
	 * @param merchantId 商户id
	 * @param storeId 店铺id
     * @param rebateIds 折扣活动id集合
	 */
	public void batchUpdatePromotionRebateIsSelectChargeItem(int merchantId, long storeId, List<Long> rebateIds) {
		if (rebateIds == null || rebateIds.isEmpty()){
			return;
		}
		List<Object[]> params = new ArrayList<Object[]>();
		for (Long rebateId : rebateIds) {
			params.add(new Object[]{true, rebateId});
		}
		this.query.batchUpdate(StorePromotionRebate.class, "set select_charge_item=? where promotion_rebate_id=? ", params);
	}

}
