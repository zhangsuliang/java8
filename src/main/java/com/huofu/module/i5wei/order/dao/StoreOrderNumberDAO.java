package com.huofu.module.i5wei.order.dao;


import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DateUtil;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.order.entity.StoreOrderNumber;

import java.util.List;

/**
 * Auto created by i5weitools
 *
 * @author kaichen
 */
@Repository
public class StoreOrderNumberDAO extends AbsQueryDAO<StoreOrderNumber> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreOrderNumber storeOrderNumber) {
        this.addDbRouteInfo(storeOrderNumber.getMerchantId(), storeOrderNumber.getStoreId());
        super.create(storeOrderNumber);
    }

    @Override
    public List<StoreOrderNumber> batchCreate(List<StoreOrderNumber> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreOrderNumber storeOrderNumber) {
        this.addDbRouteInfo(storeOrderNumber.getMerchantId(), storeOrderNumber.getStoreId());
        super.update(storeOrderNumber);
    }

    @Override
    public void delete(StoreOrderNumber storeOrderNumber) {
        this.addDbRouteInfo(storeOrderNumber.getMerchantId(), storeOrderNumber.getStoreId());
        super.delete(storeOrderNumber);
    }

    public StoreOrderNumber getStoreOrderNumberById(int merchantId, long storeId, long repastDate, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        repastDate = DateUtil.getBeginTime(repastDate, null);
        StoreOrderNumber storeOrderNumber;
        if (forUpdate) {
            storeOrderNumber = this.query.obj(StoreOrderNumber.class, "where merchant_id=? and store_id=? and repast_date=? for update ", new Object[]{merchantId, storeId, repastDate});
        }
        else {
            storeOrderNumber = this.query.obj(StoreOrderNumber.class, "where merchant_id=? and store_id=? and repast_date=? ", new Object[]{merchantId, storeId, repastDate});
        }
        if (storeOrderNumber != null) {
            if (forSnapshot) {
                storeOrderNumber.snapshot();
            }
        }
        return storeOrderNumber;
    }

}
