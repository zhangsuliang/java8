package com.huofu.module.i5wei.setting.dao;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.setting.entity.StorePresellSetting;

/**
 * Auto created by i5weitools
 */
@Repository
public class StorePresellSettingDAO extends AbsQueryDAO<StorePresellSetting> {

    public StorePresellSetting getById(int merchantId, long storeId, boolean forUpdate, boolean forSnapshot) {
        StorePresellSetting storePresellSetting = this.query.objById(StorePresellSetting.class, storeId, forUpdate);
        if (storePresellSetting != null) {
            if (forSnapshot) {
                storePresellSetting.snapshot();
            }
        }
        return storePresellSetting;
    }
    
    public StorePresellSetting getById(int merchantId, long storeId, boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        StorePresellSetting storePresellSetting = this.query.objById(StorePresellSetting.class, storeId);
        return storePresellSetting;
    }

}
