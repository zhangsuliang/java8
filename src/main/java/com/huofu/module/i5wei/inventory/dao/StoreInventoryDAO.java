package com.huofu.module.i5wei.inventory.dao;


import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.inventory.entity.StoreInventory;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreInventoryDAO extends AbsQueryDAO<StoreInventory> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public List<StoreInventory> batchCreate(List<StoreInventory> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void delete(StoreInventory storeInventory) {
        this.addDbRouteInfo(storeInventory.getMerchantId(), storeInventory.getStoreId());
        super.delete(storeInventory);
    }

    @Override
    public void update(StoreInventory storeInventory) {
        this.addDbRouteInfo(storeInventory.getMerchantId(), storeInventory.getStoreId());
        super.update(storeInventory);
    }

    @Override
    public void create(StoreInventory storeInventory) {
        this.addDbRouteInfo(storeInventory.getMerchantId(), storeInventory.getStoreId());
        super.create(storeInventory);
    }

    public StoreInventory getById(int merchantId, long storeId, long productId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        StoreInventory storeInventory;
        if (forUpdate) {
            storeInventory = this.query.obj(StoreInventory.class, " where merchant_id=? and store_id=? and product_id=? for update ", new Object[]{merchantId, storeId, productId});
        }
        else {
            storeInventory = this.query.obj(StoreInventory.class, " where merchant_id=? and store_id=? and product_id=? ", new Object[]{merchantId, storeId, productId});
        }
        if (storeInventory != null) {
            if (forSnapshot) {
                storeInventory.snapshot();
            }
        }
        return storeInventory;
    }

    public List<StoreInventory> getStoreInventorysByProductIds(int merchantId, long storeId, List<Long> productIds, boolean enableSlave) {
    	if (enableSlave) {
        	DALStatus.setSlaveMode();
        }
    	List<Object> params = new ArrayList<>();
        params.add(merchantId);
        params.add(storeId);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreInventory.class, "where merchant_id=? and store_id=? ", "product_id", params, productIds);
    }

    public Map<Long, StoreInventory> getStoreInventoryMapByProductIds(int merchantId, long storeId, List<Long> productIds, boolean enableSlave) {
        List<StoreInventory> list = this.getStoreInventorysByProductIds(merchantId, storeId, productIds, enableSlave);
        Map<Long, StoreInventory> inventoryMap = new HashMap<>();
        if (list == null || list.isEmpty()) {
            return inventoryMap;
        }
        for (StoreInventory storeInventory : list) {
            inventoryMap.put(storeInventory.getProductId(), storeInventory);
        }
        return inventoryMap;
    }

    public List<StoreInventory> queryStoreInventorys(int merchantId, long storeId) {
    	DALStatus.setSlaveMode();
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreInventory.class, "where merchant_id=? and store_id=? ", new Object[]{merchantId, storeId});
    }

    public Map<Long, StoreInventory> getStoreInventory(int merchantId, long storeId) {
        List<StoreInventory> storeInventorys = this.queryStoreInventorys(merchantId, storeId);
        Map<Long, StoreInventory> inventoryMap = new HashMap<>();
        if (storeInventorys != null && !storeInventorys.isEmpty()) {
            for (StoreInventory inventory : storeInventorys) {
                inventoryMap.put(inventory.getProductId(), inventory);
            }
        }
        return inventoryMap;
    }

}
