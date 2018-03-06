package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreMenuDisplayCat;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMenuDisplayCatDAO extends AbsQueryDAO<StoreMenuDisplayCat> {

    @Autowired
    private IdMakerUtil idMakerUtil;

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public List<StoreMenuDisplayCat> batchCreate(List<StoreMenuDisplayCat> list) {
        if (list.isEmpty()) {
            return list;
        }
        StoreMenuDisplayCat storeMenuDisplayCat = list.get(0);
        this.addDbRouteInfo(storeMenuDisplayCat.getMerchantId(), storeMenuDisplayCat.getStoreId());
        List<Long> ids = this.idMakerUtil.nextIds("store_menu_display_cat", list.size());
        int i = 0;
        for (StoreMenuDisplayCat cat : list) {
            cat.setDisplayCatId(ids.get(i));
            i++;
        }
        return super.batchCreate(list);
    }

    public List<StoreMenuDisplayCat> getListByTimeBucketId(int merchantId, long storeId, long timeBucketId, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        if (timeBucketId > 0) {
            return this.query.list(StoreMenuDisplayCat.class, "where store_id=? and time_bucket_id=?", new Object[]{storeId, timeBucketId});
        }
        return this.query.list(StoreMenuDisplayCat.class, "where store_id=?", new Object[]{storeId});
    }

    public Map<Long, StoreMenuDisplayCat> getMapInIds(int merchantId, long storeId, List<Long> catIds, boolean enableSlave, boolean enableCache) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.map2(StoreMenuDisplayCat.class, "where store_id=?", "display_cat_id", Lists.newArrayList(storeId), catIds);
    }

    public StoreMenuDisplayCat getByIdForUpdate(int merchantId, long storeId, long catId, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        if (forUpdate) {
            return this.query.objByIdForUpdate(StoreMenuDisplayCat.class, catId);
        }
        return this.query.objById(StoreMenuDisplayCat.class, catId);
    }

    public int deleteByTimeBucketId(int merchantId, long storeId, long timeBucketId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.delete(StoreMenuDisplayCat.class, "where store_id=? and time_bucket_id=?", new Object[]{storeId, timeBucketId});
    }
}
