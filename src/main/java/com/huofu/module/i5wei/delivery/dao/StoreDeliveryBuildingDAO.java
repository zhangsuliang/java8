package com.huofu.module.i5wei.delivery.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.delivery.entity.StoreDeliveryBuilding;
import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreDeliveryBuildingDAO extends AbsQueryDAO<StoreDeliveryBuilding> {

    public StoreDeliveryBuilding getById(int merchantId, long storeId, long buildingId) {
        return this.query.objById(StoreDeliveryBuilding.class, buildingId);
    }

    public boolean isDuplicate(int merchantId, long storeId, String name, long exceptBuildingId) {
        int count = this.query.count(StoreDeliveryBuilding.class, "where merchant_id=? and store_id=? and name=? and building_id!=? and deleted=?", new Object[]{merchantId, storeId, name, exceptBuildingId, false});
        if (count > 0) {
            return true;
        }
        return false;
    }

    public StoreDeliveryBuilding getDuplicate(int merchantId, long storeId, String name, long exceptBuildingId) {
        List<StoreDeliveryBuilding> storeDeliveryBuildings = this.query.list(StoreDeliveryBuilding.class, "where merchant_id=? and store_id=? and name=? and building_id!=? order by deleted asc limit 1", new Object[]{merchantId, storeId, name, exceptBuildingId});
        if (storeDeliveryBuildings.isEmpty()) {
            return null;
        }
        return storeDeliveryBuildings.get(0);
    }

    public void checkDuplicate(int merchantId, long storeId, String name, long exceptBuildingId) throws T5weiException {
        if (this.isDuplicate(merchantId, storeId, name, exceptBuildingId)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_BUILDING_NAME_DUPLICATE.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] name[" + name + "] duplicate");
        }
    }

    public StoreDeliveryBuilding loadById(int merchantId, long storeId, long buildingId) throws T5weiException {
        StoreDeliveryBuilding storeDeliveryBuilding = this.getById(merchantId, storeId, buildingId);
        if (storeDeliveryBuilding == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_BUILDING_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] buildingId[" + buildingId + "] building invalid");
        }
        return storeDeliveryBuilding;
    }

    public List<StoreDeliveryBuilding> getListByStoreId(int merchantId, long storeId, int size) {
        return this.query.list(StoreDeliveryBuilding.class, "where merchant_id=? and store_id=? and deleted=? limit ?", new Object[]{merchantId, storeId, false, size});
    }

    public Map<Long, StoreDeliveryBuilding> getMapInIds(int merchantId, long storeId, List<Long> buildingIds) {
        return this.query.map2(StoreDeliveryBuilding.class, "where merchant_id=? and store_id=?", "building_id", Lists.newArrayList(merchantId, storeId), buildingIds);
    }
}
