package com.huofu.module.i5wei.delivery.service;

import com.huofu.module.i5wei.delivery.dao.StoreDeliveryBuildingDAO;
import com.huofu.module.i5wei.delivery.entity.StoreDeliveryBuilding;
import huofucore.facade.i5wei.delivery.StoreDeliveryBuildingParam;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.bean.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by akwei on 5/6/15.
 */
@Service
public class StoreDeliveryBuildingService {

    @Autowired
    private StoreDeliveryBuildingDAO storeDeliveryBuildingDAO;

    public StoreDeliveryBuilding loadStoreDeliveryBuilding(int merchantId, long storeId, long buildingId) throws T5weiException {
        return this.storeDeliveryBuildingDAO.loadById(merchantId, storeId, buildingId);
    }

    public StoreDeliveryBuilding saveStoreDeliveryBuilding(StoreDeliveryBuildingParam param) throws T5weiException {
        if (param.getBuildingId() <= 0) {
            StoreDeliveryBuilding storeDeliveryBuilding = this.storeDeliveryBuildingDAO.getDuplicate(param.getMerchantId(), param.getStoreId(), param.getName(), param.getBuildingId());
            if (storeDeliveryBuilding == null) {
                storeDeliveryBuilding = new StoreDeliveryBuilding();
                BeanUtil.copy(param, storeDeliveryBuilding);
                storeDeliveryBuilding.init4Create();
                storeDeliveryBuilding.create();
                return storeDeliveryBuilding;
            }
            if (storeDeliveryBuilding.isDeleted()) {
                long buildingId = storeDeliveryBuilding.getBuildingId();
                BeanUtil.copy(param, storeDeliveryBuilding);
                storeDeliveryBuilding.setBuildingId(buildingId);
                storeDeliveryBuilding.init4Create();
                storeDeliveryBuilding.update();
                return storeDeliveryBuilding;
            }
        }
        this.storeDeliveryBuildingDAO.checkDuplicate(param.getMerchantId(), param.getStoreId(), param.getName(), param.getBuildingId());
        StoreDeliveryBuilding storeDeliveryBuilding = this.storeDeliveryBuildingDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getBuildingId());
        BeanUtil.copy(param, storeDeliveryBuilding);
        storeDeliveryBuilding.setUpdateTime(System.currentTimeMillis());
        storeDeliveryBuilding.update();
        return storeDeliveryBuilding;
    }

    public List<StoreDeliveryBuilding> getStoreDeliveryBuildings(int merchantId, long storeId, int size) {
        return this.storeDeliveryBuildingDAO.getListByStoreId(merchantId, storeId, size);
    }

    public void deleteStoreDeliveryBuilding(int merchantId, long storeId, long buildingId) throws T5weiException {
        StoreDeliveryBuilding storeDeliveryBuilding = this.storeDeliveryBuildingDAO.loadById(merchantId, storeId, buildingId);
        storeDeliveryBuilding.makeDeleted();
    }
}
