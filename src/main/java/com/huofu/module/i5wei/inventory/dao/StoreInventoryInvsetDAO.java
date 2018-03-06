package com.huofu.module.i5wei.inventory.dao;


import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.inventory.entity.StoreInventoryInvset;

import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreInventoryInvsetDAO extends AbsQueryDAO<StoreInventoryInvset> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreInventoryInvset storeInventoryInvset) {
        this.addDbRouteInfo(storeInventoryInvset.getMerchantId(), storeInventoryInvset.getStoreId());
        super.create(storeInventoryInvset);
    }

    @Override
    public List<StoreInventoryInvset> batchCreate(List<StoreInventoryInvset> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreInventoryInvset storeInventoryInvset) {
        this.addDbRouteInfo(storeInventoryInvset.getMerchantId(), storeInventoryInvset.getStoreId());
        super.update(storeInventoryInvset);
    }

    @Override
    public void delete(StoreInventoryInvset storeInventoryInvset) {
        this.addDbRouteInfo(storeInventoryInvset.getMerchantId(), storeInventoryInvset.getStoreId());
        super.delete(storeInventoryInvset);
    }
}
