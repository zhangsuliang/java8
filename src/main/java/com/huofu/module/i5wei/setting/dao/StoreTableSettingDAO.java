package com.huofu.module.i5wei.setting.dao;

import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.store5weisetting.CustomerPayEnum;
import huofuhelper.util.AbsQueryDAO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import java.util.ArrayList;
import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreTableSettingDAO extends AbsQueryDAO<StoreTableSetting> {

    private final static Log log = LogFactory.getLog(StoreTableSettingDAO.class);

    public StoreTableSetting getById(long storeId, boolean enableSlave){
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        StoreTableSetting storeTableSetting = query.objById(StoreTableSetting.class,storeId);
        return storeTableSetting;
    }

    //修复customerPay数据的问题
    public void fixCustomerPay(){
        List<StoreTableSetting> storeTableSettingList = this.query.list(StoreTableSetting.class,null,null);
        log.info("===============修复数据开始===============");
        for(StoreTableSetting storeTableSetting : storeTableSettingList){
            storeTableSetting.snapshot();
            log.info("==============================");
            boolean enableCustomerSelfPayAfter = storeTableSetting.isEnableCustomerSelfPayAfter();
            int customerPay = storeTableSetting.getCustomerPay();
            if(customerPay == CustomerPayEnum.DEFAULT.getValue()){
                log.info("enableCustomerSelfPayAfter  == " + enableCustomerSelfPayAfter);
                if(enableCustomerSelfPayAfter){
                    storeTableSetting.setCustomerPay(CustomerPayEnum.BEFORE_AND_AFTER.getValue());
                    log.info("CustomerPay  == " + storeTableSetting.getCustomerPay());
                }else{
                    storeTableSetting.setCustomerPay(CustomerPayEnum.BEFORE.getValue());
                    log.info("CustomerPay  == " + storeTableSetting.getCustomerPay());
                }
                storeTableSetting.update();
            }
        }
        log.info("===============修复数据结束===============");
    }

}
