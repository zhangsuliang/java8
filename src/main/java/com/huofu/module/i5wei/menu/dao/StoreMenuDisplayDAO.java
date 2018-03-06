package com.huofu.module.i5wei.menu.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreMenuDisplay;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMenuDisplayDAO extends AbsQueryDAO<StoreMenuDisplay> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public List<StoreMenuDisplay> batchCreate(List<StoreMenuDisplay> list) {
        if (list.isEmpty()) {
            return list;
        }
        int merchantId = list.get(0).getMerchantId();
        long storeId = list.get(0).getStoreId();
        this.addDbRouteInfo(merchantId, storeId);
        return super.batchCreate(list);
    }

    public int deleteByChargeItemIdAndTimeBucketId(int merchantId, long storeId, long chargeItemId, long timeBucketId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.delete(StoreMenuDisplay.class, "where store_id=? and charge_item_id=? and time_bucket_id=?", new Object[]{storeId, chargeItemId, timeBucketId});
    }

    public int deleteByTimeBucketId(int merchantId, long storeId, long timeBucketId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.delete(StoreMenuDisplay.class, "where store_id=? and time_bucket_id=?", new Object[]{storeId, timeBucketId});
    }

    public List<StoreMenuDisplay> getListByTimeBucketId(int merchantId, long storeId, long timeBucketId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        if (timeBucketId > 0) {
            return this.query.list(StoreMenuDisplay.class, "where store_id=? and time_bucket_id=?", new Object[]{storeId, timeBucketId});
        }
        return this.query.list(StoreMenuDisplay.class, "where store_id=?", new Object[]{storeId});
    }

    public int deleteByChargeItemId(int merchantId, long storeId, long chargeItemId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.delete(StoreMenuDisplay.class, "where store_id=? and charge_item_id=?",
                new Object[]{storeId, chargeItemId});
    }
}
