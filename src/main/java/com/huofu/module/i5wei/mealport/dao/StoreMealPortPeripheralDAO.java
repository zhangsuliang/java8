package com.huofu.module.i5wei.mealport.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortPeripheral;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealPortPeripheralDAO extends AbsQueryDAO<StoreMealPortPeripheral> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public List<StoreMealPortPeripheral> batchCreate(List<StoreMealPortPeripheral> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StoreMealPortPeripheral obj = list.get(0);
        this.addDbRouteInfo(obj.getMerchantId(), obj.getStoreId());
        return super.batchCreate(list);
    }

    public void deleteByPortId(int merchantId, long storeId, long portId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.delete(StoreMealPortPeripheral.class, "where store_id=? and port_id=?", new Object[]{storeId, portId});
    }

    public List<StoreMealPortPeripheral> getListByPortId(int merchantId, long storeId, long portId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALStatus.setSlaveMode();
        return this.query.list(StoreMealPortPeripheral.class, "where store_id=? and port_id=?", new Object[]{storeId, portId});
    }

    public Map<Long, List<StoreMealPortPeripheral>> getListMapInPortIds(int merchantId, long storeId, List<Long> portIds) {
        this.addDbRouteInfo(merchantId, storeId);
        DALStatus.setSlaveMode();
        List<StoreMealPortPeripheral> list = this.query.listInValues2(StoreMealPortPeripheral.class, "where store_id=? ", "port_id", Lists.newArrayList(storeId), portIds);
        Map<Long, List<StoreMealPortPeripheral>> map = Maps.newHashMap();
        for (StoreMealPortPeripheral obj : list) {
            List<StoreMealPortPeripheral> olist = map.get(obj.getPortId());
            if (olist == null) {
                olist = Lists.newArrayList();
                map.put(obj.getPortId(), olist);
            }
            olist.add(obj);
        }
        return map;
    }
}
