package com.huofu.module.i5wei.heartbeat.service;

import com.huofu.module.i5wei.heartbeat.dao.Store5weiHeartbeatDAO;
import com.huofu.module.i5wei.heartbeat.entity.Store5weiHeartbeat;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;

import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 店铺数据信息状态变更表
 * Created by akwei on 10/16/15.
 */
@Service
public class StoreHeartbeatService {

    @Autowired
    private Store5weiHeartbeatDAO store5weiHeartbeatDAO;
    
    @Autowired
    private Store5weiSettingService store5weiSettingService;

    /**
     * 设置是否有出餐口空闲
     *
     * @param merchantId  商户id
     * @param storeId     店铺id
     * @param hasIdlePort true:有空闲出餐口
     * @return 店铺心跳信息
     */
    public Store5weiHeartbeat changePortIdle(int merchantId, long storeId, boolean hasIdlePort) {
        try {
            return this._changePortIdle(merchantId, storeId, hasIdlePort);
        } catch (DuplicateKeyException e) {
            return this._changePortIdle(merchantId, storeId, hasIdlePort);
        }
    }

    private Store5weiHeartbeat _changePortIdle(int merchantId, long storeId,
                                               boolean hasIdlePort) {
        Store5weiHeartbeat store5weiHeartbeat = this.store5weiHeartbeatDAO
                .getById(merchantId, storeId);
        if (store5weiHeartbeat == null) {
            store5weiHeartbeat = new Store5weiHeartbeat();
            store5weiHeartbeat.setStoreId(storeId);
            store5weiHeartbeat.setMerchantId(merchantId);
            store5weiHeartbeat.setHasIdlePort(hasIdlePort);
            store5weiHeartbeat.setPortLastUpdateTime(0);
            store5weiHeartbeat.create();
        } else {
            store5weiHeartbeat.snapshot();
            store5weiHeartbeat.setHasIdlePort(hasIdlePort);
            store5weiHeartbeat.update();
        }
        return store5weiHeartbeat;
    }

    /**
     * 更改当日最大取餐流水号
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param repastDate   就餐日期
     * @param serialNumber 流水号
     * @return
     */
    public Store5weiHeartbeat changeLastSerialNumber(int merchantId, long
            storeId, long repastDate, int serialNumber) {
        try {
            return this._changeLastSerialNumber(merchantId, storeId,
                    repastDate, serialNumber);
        } catch (DuplicateKeyException e) {
            return this._changeLastSerialNumber(merchantId, storeId,
                    repastDate, serialNumber);
        }
    }

    private Store5weiHeartbeat _changeLastSerialNumber(int merchantId, long
            storeId, long repastDate, int serialNumber) {
        Store5weiHeartbeat store5weiHeartbeat = this.store5weiHeartbeatDAO
                .getById(merchantId, storeId);
        if (store5weiHeartbeat == null) {
            store5weiHeartbeat = new Store5weiHeartbeat();
            store5weiHeartbeat.setStoreId(storeId);
            store5weiHeartbeat.setMerchantId(merchantId);
            store5weiHeartbeat.setRepastDate(repastDate);
            store5weiHeartbeat.setTakeSerialNumber(serialNumber);
            store5weiHeartbeat.create();
        } else {
            store5weiHeartbeat.snapshot();
            store5weiHeartbeat.setRepastDate(repastDate);
            store5weiHeartbeat.setTakeSerialNumber(serialNumber);
            store5weiHeartbeat.update();
        }
        return store5weiHeartbeat;
    }

    /**
     * 获得店铺心跳信息
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @return 店铺心跳信息
     */
    public Store5weiHeartbeat getStore5weiHeartbeat(int merchantId, long storeId) {
        Store5weiHeartbeat store5weiHeartbeat = this.store5weiHeartbeatDAO
                .queryById(merchantId, storeId);
        if (store5weiHeartbeat == null) {
            store5weiHeartbeat = Store5weiHeartbeat.createDefault(merchantId, storeId);
        }
        return store5weiHeartbeat;
    }

    /**
     * 更新备餐完成的通知时间
     *
     * @param merchantId
     * @param storeId
     * @param time
     */
    public void updateDeliveryPreparedNotifyTime(int merchantId, long storeId, long
            time) {

        Store5weiHeartbeat store5weiHeartbeat = this.store5weiHeartbeatDAO
                .getById(merchantId, storeId);
        if (store5weiHeartbeat == null) {
            store5weiHeartbeat = Store5weiHeartbeat.createDefault(merchantId, storeId);
            store5weiHeartbeat.setDeliveryPreparedNotifyTime(time);
            this.createINX(store5weiHeartbeat);
        } else {
            store5weiHeartbeat.snapshot();
            store5weiHeartbeat.setDeliveryPreparedNotifyTime(time);
            store5weiHeartbeat.update();
        }
    }
    
    /**
     * 出餐口最后更新时间
     *
     * @param merchantId
     * @param storeId
     * @param time
     */
    public void updatePortLastUpdateTime(int merchantId, long storeId, long time) {
        Store5weiHeartbeat store5weiHeartbeat = this.store5weiHeartbeatDAO.getById(merchantId, storeId);
        if (store5weiHeartbeat == null) {
            store5weiHeartbeat = Store5weiHeartbeat.createDefault(merchantId, storeId);
            store5weiHeartbeat.setPortLastUpdateTime(time);
            this.createINX(store5weiHeartbeat);
        } else {
            store5weiHeartbeat.snapshot();
            store5weiHeartbeat.setPortLastUpdateTime(time);
            store5weiHeartbeat.update();
        }
    }
    
    /**
     * 划菜时间最后更新时间
     * @param merchantId
     * @param storeId
     * @param time
     */
    public void updateSweepLastUpdateTime(int merchantId, long storeId, long time, boolean isAdvancePrint, Store5weiSetting store5weiSetting){
        if(!isAdvancePrint && store5weiSetting == null){
            store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId , false);
        }
        if(isAdvancePrint || store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
            Store5weiHeartbeat store5weiHeartbeat = this.store5weiHeartbeatDAO.getById(merchantId, storeId);
            if (store5weiHeartbeat == null) {
                store5weiHeartbeat = Store5weiHeartbeat.createDefault(merchantId, storeId);
                store5weiHeartbeat.setSweepLastUpdateTime(time);
                this.createINX(store5weiHeartbeat);
            } else {
                store5weiHeartbeat.snapshot();
                store5weiHeartbeat.setSweepLastUpdateTime(time);
                store5weiHeartbeat.update();
            }
        }
    }

    public void createINX(Store5weiHeartbeat store5weiHeartbeat) {
        try {
            store5weiHeartbeat.create();
        } catch (DuplicateKeyException e) {
            store5weiHeartbeat.update();
        }
    }

}
