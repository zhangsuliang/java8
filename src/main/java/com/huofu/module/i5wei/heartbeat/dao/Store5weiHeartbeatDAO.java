package com.huofu.module.i5wei.heartbeat.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.heartbeat.entity.Store5weiHeartbeat;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

@SuppressWarnings("unchecked")
@Repository
public class Store5weiHeartbeatDAO extends AbsQueryDAO<Store5weiHeartbeat> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(Store5weiHeartbeat store5weiHeartbeat) {
        this.addDbRouteInfo(store5weiHeartbeat.getMerchantId(),
                store5weiHeartbeat.getStoreId());
        super.create(store5weiHeartbeat);
    }

    @Override
    public void replace(Store5weiHeartbeat store5weiHeartbeat) {
        this.addDbRouteInfo(store5weiHeartbeat.getMerchantId(),
                store5weiHeartbeat.getStoreId());
        super.replace(store5weiHeartbeat);
    }

    @Override
    public void update(Store5weiHeartbeat store5weiHeartbeat, Store5weiHeartbeat snapshot) {
        this.addDbRouteInfo(store5weiHeartbeat.getMerchantId(),
                store5weiHeartbeat.getStoreId());
        super.update(store5weiHeartbeat, snapshot);
    }

    @Override
    public void delete(Store5weiHeartbeat store5weiHeartbeat) {
        this.addDbRouteInfo(store5weiHeartbeat.getMerchantId(),
                store5weiHeartbeat.getStoreId());
        super.delete(store5weiHeartbeat);
    }

    public Store5weiHeartbeat getById(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(Store5weiHeartbeat.class,
                storeId);
    }
    
    public Store5weiHeartbeat queryById(int merchantId, long storeId) {
    	DALStatus.setSlaveMode();
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(Store5weiHeartbeat.class,
                storeId);
    }
    
}
