package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by akwei on 2/15/15.
 */
@Repository
public class StoreTimeBucketDAO extends AbsQueryDAO<StoreTimeBucket> {

    @Autowired
    private IdMakerUtil idMakerUtil;

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    public void checkForDuplicate(int merchantId, long storeId, long exceptedTimeBucketId, long startTime, long endTime, String name) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        int count = this.query.count2(StoreTimeBucket.class, "where store_id=? and time_bucket_id!=? and deleted=? and ( (start_time=? and end_time=?) or (name=?) )", Lists.newArrayList(storeId, exceptedTimeBucketId, false, startTime, endTime, name));
        if (count > 0) {
            throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_DUPLICATE.getValue(), "storeId[" + storeId + "] timeBucketId[" + exceptedTimeBucketId + "] startTime[" + startTime + "] endTime[" + endTime + "] name[" + name + "] duplicate");
        }
    }

    public Map<Long, StoreTimeBucket> getMapInIds(int merchantId, long storeId, List<Long> timeBucketIds, boolean enableSlave) {
        return this.getMapInIds(merchantId, storeId, timeBucketIds, false, false);
    }

    public Map<Long, StoreTimeBucket> getMapInIds(int merchantId, long storeId, List<Long> timeBucketIds, boolean enableSlave, boolean enableCache) {
        if (timeBucketIds == null || timeBucketIds.isEmpty()) {
            return new HashMap<>(0);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.map2(StoreTimeBucket.class, "where store_id=?", "time_bucket_id", Lists.newArrayList(storeId), timeBucketIds);
    }

    public List<StoreTimeBucket> getListInIds(int merchantId, long storeId, List<Long> timeBucketIds) {
        if (timeBucketIds == null || timeBucketIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        return this.getListInIds(merchantId, storeId, timeBucketIds, false, false);
    }

    public List<StoreTimeBucket> getListInIds(int merchantId, long storeId, List<Long> timeBucketIds, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreTimeBucket.class, "where store_id=?", "time_bucket_id", Lists.newArrayList(storeId), timeBucketIds);
    }

    @Override
    public List<StoreTimeBucket> batchCreate(List<StoreTimeBucket> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        List<Long> ids = this.idMakerUtil.nextIds("store_time_bucket", list.size());
        int i = 0;
        for (StoreTimeBucket storeTimeBucket : list) {
            storeTimeBucket.setTimeBucketId(ids.get(i));
            i++;
        }
        return super.batchCreate(list);
    }

    @Override
    public void create(StoreTimeBucket storeTimeBucket) {
        this.addDbRouteInfo(storeTimeBucket.getMerchantId(), storeTimeBucket.getStoreId());
        storeTimeBucket.setTimeBucketId(this.idMakerUtil.nextId("store_time_bucket"));
        super.create(storeTimeBucket);
    }

    @Override
    public void update(StoreTimeBucket storeTimeBucket, StoreTimeBucket snapshot) {
        this.addDbRouteInfo(storeTimeBucket.getMerchantId(), storeTimeBucket.getStoreId());
        super.update(storeTimeBucket, snapshot);
    }

    @Override
    public void delete(StoreTimeBucket storeTimeBucket) {
        this.addDbRouteInfo(storeTimeBucket.getMerchantId(), storeTimeBucket.getStoreId());
        super.delete(storeTimeBucket);
    }

    public StoreTimeBucket getById(int merchantId, long storeId, long timeBucketId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        StoreTimeBucket storeTimeBucket = this.query.objById(StoreTimeBucket.class, timeBucketId, forUpdate);
        if (storeTimeBucket != null) {
            if (forSnapshot) {
                storeTimeBucket.snapshot();
            }
        }
        return storeTimeBucket;
    }

    public StoreTimeBucket loadById(int merchantId, long storeId, long timeBucketId, boolean forUpdate, boolean forSnapshot) throws T5weiException {
        StoreTimeBucket storeTimeBucket = this.getById(merchantId, storeId, timeBucketId, forUpdate, forSnapshot);
        if (storeTimeBucket == null) {
            throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] timeBucketId[" + timeBucketId + "] invalid");
        }
        return storeTimeBucket;
    }

    public StoreTimeBucket getByIdForQuery(int merchantId, long storeId, long timeBucketId, boolean enableSlave, boolean enableCache) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        StoreTimeBucket storeTimeBucket = this.query.objById(StoreTimeBucket.class, timeBucketId);
        return storeTimeBucket;
    }

    public StoreTimeBucket loadByIdForQuery(int merchantId, long storeId, long timeBucketId, boolean enableSlave, boolean enableCache) throws T5weiException {
        StoreTimeBucket storeTimeBucket = this.getByIdForQuery(merchantId, storeId, timeBucketId, enableSlave, enableCache);
        if (storeTimeBucket == null) {
            throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] timeBucketId[" + timeBucketId + "] invalid");
        }
        return storeTimeBucket;
    }

    /**
     * 获得店铺的有效营业时段列表
     *
     * @param merchantId  商户id
     * @param storeId     店铺id
     * @param enableSlave 是否开启slave数据库读取
     * @return
     */
    public List<StoreTimeBucket> getListForStore(int merchantId, long storeId, boolean enableSlave, boolean enableCache) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreTimeBucket.class, "where store_id=? and deleted=? order by start_time asc,end_time asc", new Object[]{storeId, false});
    }
    
    public List<StoreTimeBucket> getListForStoreId (int merchantId, long storeId, long beginTime, long endTime) {
    	this.addDbRouteInfo(merchantId, storeId);
    	return this.query.list(StoreTimeBucket.class, "where store_id=? and create_time<=? order by start_time asc,end_time asc", new Object[]{storeId, endTime});
    }

    /**
     * 获得跨天有效的营业时段
     */
    public StoreTimeBucket get4OverDay(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreTimeBucket.class, "where store_id=? and deleted=? and end_time>? limit 1", new Object[]{storeId, false, StoreTimeBucket.getMills_in_day()});
    }

    /**
     * 获得店铺里面所有营业时段 包括被删除的
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreTimeBucket> getAllTimeBucket(int merchantId, Long storeId, boolean enableSlave, boolean enableCache) {
        String sql = " where store_id = ? order by start_time asc,end_time asc";
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreTimeBucket.class, sql, new Object[]{storeId});
    }
    
    /**
     * 获取店铺里边停用的营业时段
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreTimeBucket> getTimeBucketForDeleted(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreTimeBucket.class, "where store_id=? and deleted=? ", new Object[]{storeId,true});
    }
}
