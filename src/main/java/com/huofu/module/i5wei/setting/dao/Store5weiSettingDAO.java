package com.huofu.module.i5wei.setting.dao;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.setting.entity.Store5weiSetting;

/**
 * Auto created by i5weitools
 */
@Repository
public class Store5weiSettingDAO extends AbsQueryDAO<Store5weiSetting> {

    public Store5weiSetting getById(int merchantId, long storeId, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.objById(Store5weiSetting.class, storeId);
    }
    
}
