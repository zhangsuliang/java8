package com.huofu.module.i5wei.setting.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.setting.entity.StoreDefinedPrinter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

/**
 * Created by jiajin.nervous on 16/10/10.
 */
@Repository
public class StoreDefinedPrinterDAO extends AbsQueryDAO<StoreDefinedPrinter> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    public StoreDefinedPrinter loadById(int merchantId,long storeId,long printerId,boolean enableSlave) throws T5weiException {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId,storeId);
        StoreDefinedPrinter storeDefinedPrinter = this.query.obj(StoreDefinedPrinter.class,"where store_id=? and printer_id=? ",new Object[]{storeId,printerId});
        if(storeDefinedPrinter == null){
            throw new T5weiException(T5weiErrorCodeType.STORE_DEFINED_PRINTER_NOT_EXIST.getValue(),"storeId[" + storeId + "],printerId[" + printerId + "] not exist" );
        }
        return storeDefinedPrinter;
    }

    public List<StoreDefinedPrinter> getStoreDefinedPrinterListByPrintMsgType(int merchantId,long storeId,int printMsgType,boolean enableSlave){
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId,storeId);
        return this.query.list(StoreDefinedPrinter.class,"where store_id=? and print_msg_type=? ",new Object[]{storeId,printMsgType});
    }

    public List<StoreDefinedPrinter> getStoreDefinedPrinterList(int merchantId,long storeId,boolean enableSlave){
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId,storeId);
        return this.query.list(StoreDefinedPrinter.class,"where store_id=?",new Object[]{storeId});
    }

    public List<StoreDefinedPrinter> getStoreDefinedPrinterListByType(int merchantId,long storeId,int printMsgType,boolean enableSlave){
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId,storeId);
        return this.query.list(StoreDefinedPrinter.class,"where store_id=? and print_msg_type = ?",new Object[]{storeId,printMsgType});
    }

    public void batchUpdateStoreDefinedPrinter(int merchantId,long storeId,List<StoreDefinedPrinter> storeDefinedPrinters){
        if(storeDefinedPrinters.isEmpty()){
            return;
        }
        this.addDbRouteInfo(merchantId,storeId);
        List<Object[]> param = new ArrayList<>();
        for(StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters){
            param.add(new Object[]{storeDefinedPrinter.getPrintScope(),storeDefinedPrinter.getPrintMsgType(),storeDefinedPrinter.getPrinterId()});
        }
        this.query.batchUpdate(StoreDefinedPrinter.class,"set print_scope=?,print_msg_type=? where printer_id=?",param);
    }

    @Override
    public void create(StoreDefinedPrinter storeDefinedPrinter) {
        this.addDbRouteInfo(storeDefinedPrinter.getMerchantId(),storeDefinedPrinter.getStoreId());
        super.create(storeDefinedPrinter);
    }

    @Override
    public void update(StoreDefinedPrinter storeDefinedPrinter) {
        this.addDbRouteInfo(storeDefinedPrinter.getMerchantId(),storeDefinedPrinter.getStoreId());
        super.update(storeDefinedPrinter);
    }

    @Override
    public void delete(StoreDefinedPrinter storeDefinedPrinter) {
        this.addDbRouteInfo(storeDefinedPrinter.getMerchantId(),storeDefinedPrinter.getStoreId());
        super.delete(storeDefinedPrinter);
    }
}
