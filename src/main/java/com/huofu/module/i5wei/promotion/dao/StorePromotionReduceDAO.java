package com.huofu.module.i5wei.promotion.dao;


import com.google.common.collect.Lists;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.promotion.StorePromotionStatusEnum;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.cache.CacheItem;
import huofuhelper.util.cache.WengerCache;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class StorePromotionReduceDAO extends AbsQueryDAO<StorePromotionReduce> {

    @Resource
    private WengerCache wengerCache;

    public void setWengerCache(WengerCache wengerCache) {
        this.wengerCache = wengerCache;
    }

    private void addCacheCleaner(StorePromotionReduce storePromotionReduce) {
        this.cacheCleaner.add(StorePromotionReduce.class, storePromotionReduce.getPromotionReduceId());
    }

    public void deleteAll() {
        this.query.delete(StorePromotionReduce.class, null, null);
    }

    @Override
    public void update(StorePromotionReduce storePromotionReduce) {
        this.addCacheCleaner(storePromotionReduce);
        super.update(storePromotionReduce);
    }

    @Override
    public void update(StorePromotionReduce storePromotionReduce, StorePromotionReduce snapshot) {
        this.addCacheCleaner(storePromotionReduce);
        super.update(storePromotionReduce, snapshot);
    }

    @Override
    public void delete(StorePromotionReduce storePromotionReduce) {
        this.addCacheCleaner(storePromotionReduce);
        super.delete(storePromotionReduce);
    }

    /**
     * 在有效期内是否有其他活动
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param exceptedId 忽略的活动id
     * @param beginTime  活动开始时间
     * @return true:有符合条件活动
     */
    public boolean hasOtherAvaliable(int merchantId, long storeId, long exceptedId, long beginTime) {
        if (this.query.count(StorePromotionReduce.class,
                "where merchant_id=? and store_id=? and promotion_reduce_id!=? and begin_time<=? and end_time>=? and deleted=?",
                new Object[]{merchantId, storeId, exceptedId, beginTime, beginTime, false}) > 0) {
            return true;
        }
        return false;
    }

    public boolean hasDuplicateNameInavaliable(int merchantId, long storeId, String title, long exceptedId, long beginTime) {
        int val = this.query.count(StorePromotionReduce.class,
                "where merchant_id=? and store_id=? and title=? and promotion_reduce_id!=? and end_time>=? and deleted=?",
                new Object[]{merchantId, storeId, title, exceptedId, beginTime, false});
        return val > 0;
    }

    public StorePromotionReduce loadById(int merchantId, long storeId, long promotionReduceId, boolean enableSlave, boolean enableCache) throws T5weiException {
        if (enableCache) {
            CacheItem item = this.wengerCache.get4Obj(StorePromotionReduce.class, promotionReduceId);
            if (item != null) {
                return item.getObject();
            }
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        StorePromotionReduce obj = this.query.objById(StorePromotionReduce.class, promotionReduceId);
        if (obj == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_INVALID.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] promotionReduceId[" + promotionReduceId + "] invalid");
        }
        if (enableCache) {
            this.wengerCache.set4Obj(promotionReduceId, obj);
        }
        return obj;
    }

    public List<StorePromotionReduce> getList4Doing(int merchantId, long storeId, long time, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionReduce> list = this.query.list(StorePromotionReduce.class,
                "where merchant_id=? and store_id=? and paused=? and begin_time<=? and end_time>=? and deleted=? order by promotion_reduce_id desc",
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
    public List<StorePromotionReduce> getList4Avaliable(int merchantId, long storeId, long time, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StorePromotionReduce> list = this.query.list(StorePromotionReduce.class,
                "where merchant_id=? and store_id=? and end_time>=? and deleted=? order by promotion_reduce_id desc",
                new Object[]{merchantId, storeId, time, false});
        return list;
    }

    public List<StorePromotionReduce> getList4Status(int merchantId, long storeId, int status, int begin, int size) {
        Object[] objs = this.buildList4Status(merchantId, storeId, status, false, null);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.mysqlList2(StorePromotionReduce.class, sb.toString(), begin, size, params);
    }

    public int count4Status(int merchantId, long storeId, int status) {
        Object[] objs = this.buildList4Status(merchantId, storeId, status, true, null);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.count2(StorePromotionReduce.class, sb.toString(), params);
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
            sb.append(" and paused=? ");
            params.add(true);
        }
        if (DataUtil.isNotEmpty(title)) {
            sb.append(" and title like ?");
            params.add("%" + title + "%");
        }
        if (!forCount) {
            sb.append(" order by promotion_reduce_id desc");
        }
        return new Object[]{sb, params};
    }

    public List<StorePromotionReduce> getListByTitle(int merchantId, long storeId, int status, String title, int size) {
        Object[] objs = this.buildList4Status(merchantId, storeId, status, false, title);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.mysqlList2(StorePromotionReduce.class, sb.toString(), 0, size, params);
    }

    public List<StorePromotionReduce> getAll() {
        return this.query.list(StorePromotionReduce.class, null, null);
    }

    /**
	 * 批量更新满减活动为指定收费项目
	 * @param merchantId 商户id
	 * @param storeId 店铺id
	 * @param reduceIds 满减活动id集合
	 */
	public void batchUpdatePromotionReduceIsSelectChargeItem(int merchantId, long storeId, List<Long> reduceIds) {
		if (reduceIds == null || reduceIds.isEmpty()){
			return;
		}
		List<Object[]> params = new ArrayList<Object[]>();
		for (Long reduceId : reduceIds) {
			params.add(new Object[]{true, reduceId});
		}
		this.query.batchUpdate(StorePromotionReduce.class, "set select_charge_item=? where promotion_reduce_id=? ", params);
	}

}
