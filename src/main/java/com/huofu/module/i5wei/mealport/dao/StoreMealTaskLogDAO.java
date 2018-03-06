package com.huofu.module.i5wei.mealport.dao;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DateUtil;

import java.util.List;

import org.joda.time.MutableDateTime;
import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.mealport.entity.StoreMealTaskLog;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealTaskLogDAO extends AbsQueryDAO<StoreMealTaskLog> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreMealTaskLog storeMealTaskLog) {
        this.addDbRouteInfo(storeMealTaskLog.getMerchantId(), storeMealTaskLog.getStoreId());
        super.create(storeMealTaskLog);
    }
    
    @Override
    public void replace(StoreMealTaskLog storeMealTaskLog) {
        this.addDbRouteInfo(storeMealTaskLog.getMerchantId(), storeMealTaskLog.getStoreId());
        super.replace(storeMealTaskLog);
    }

    @Override
    public void update(StoreMealTaskLog storeMealTaskLog) {
        this.addDbRouteInfo(storeMealTaskLog.getMerchantId(), storeMealTaskLog.getStoreId());
        super.update(storeMealTaskLog);
    }

    @Override
    public void update(StoreMealTaskLog storeMealTaskLog, StoreMealTaskLog snapshot) {
        this.addDbRouteInfo(storeMealTaskLog.getMerchantId(), storeMealTaskLog.getStoreId());
        super.update(storeMealTaskLog, snapshot);
    }

    public List<StoreMealTaskLog> getTaskLogs(int merchantId, long storeId, int size) {
    	DALStatus.setSlaveMode();
        this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StoreMealTaskLog.class, " where merchant_id=? and store_id=? order by update_time desc limit 0,? ", new Object[] { merchantId, storeId, size });
    }
    
    public void deleteHistoryTaskLogs(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        long todayStart = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        MutableDateTime mdt = new MutableDateTime(todayStart);
		mdt.addDays(-2);
        this.query.delete(StoreMealTaskLog.class, " where merchant_id=? and store_id=? and update_time<? ", new Object[] { merchantId, storeId, mdt.getMillis() });
    }

}
