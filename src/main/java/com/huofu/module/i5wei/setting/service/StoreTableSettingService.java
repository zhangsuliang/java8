package com.huofu.module.i5wei.setting.service;

import com.huofu.module.i5wei.setting.dao.Store5weiSettingDAO;
import com.huofu.module.i5wei.setting.dao.StoreDefinedPrinterDAO;
import com.huofu.module.i5wei.setting.dao.StoreTableSettingDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreDefinedPrinter;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.table.entity.StoreArea;
import com.huofu.module.i5wei.table.service.StoreAreaService;
import huofucore.facade.i5wei.store5weisetting.CustomerPayEnum;
import huofucore.facade.i5wei.store5weisetting.Store5weiSettingParam;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.i5wei.store5weisetting.StoreTableSettingSaveParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by jiajin.nervous on 16/4/26.
 */
@Service
public class StoreTableSettingService {

    private final static Log log = LogFactory.getLog(StoreTableSettingService.class);

    @Autowired
    private StoreAreaService storeAreaService;

    @Autowired
    private StoreTableSettingDAO storeTableSettingDAO;

    @Autowired
    private Store5weiSettingDAO store5weiSettingDAO;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private StoreDefinedPrinterDAO storeDefinedPrinterDAO;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreTableSetting save(StoreTableSettingSaveParam param) throws TException {
    	boolean enableSlave = false;
        StoreTableSetting storeTableSetting = storeTableSettingDAO.getById(param.getStoreId(), enableSlave);
        if(storeTableSetting == null){
            storeTableSetting = this.createDBDefault(param);
        }else{
            storeTableSetting.snapshot();
            //桌台模式发生改变，店铺打印模式发生相应改变
            this.changeStorePrintMode(param,storeTableSetting);
            //处理桌台模式变更造成的取餐单和点菜单的变更
            this.changeStoreI5weiMeal(param,storeTableSetting);
            BeanUtil.copy(param,storeTableSetting,true);
            //处理顾客自助下单后付费字段逻辑
            this.dealCustomerPayAfter(param,storeTableSetting);
            storeTableSetting.setUpdateTime(System.currentTimeMillis());
            storeTableSetting.update();
        }
        return storeTableSetting;
    }

    private void dealCustomerPayAfter(StoreTableSettingSaveParam param,StoreTableSetting storeTableSetting){
        int customer = param.getCustomerPay();
        if(customer == CustomerPayEnum.BEFORE.getValue()){
            storeTableSetting.setEnableCustomerSelfPayAfter(false);
        }else if(customer == CustomerPayEnum.AFTER.getValue() || customer == CustomerPayEnum.BEFORE_AND_AFTER.getValue()){
            storeTableSetting.setEnableCustomerSelfPayAfter(true);
        }
    }

    private void changeStorePrintMode(StoreTableSettingSaveParam param,StoreTableSetting storeTableSetting) throws TException {
        Store5weiSettingParam store5weiSettingParam = new Store5weiSettingParam();
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        store5weiSettingParam.setMerchantId(merchantId);
        store5weiSettingParam.setStoreId(storeId);
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId,storeId,false);
        //桌台模式是否变更 && 更改为桌台模式
        if((param.isEnableTableMode() != storeTableSetting.isEnableTableMode()) && param.isEnableTableMode()){
            if(!store5weiSetting.isPrintModeDefined()){
                store5weiSettingParam.setPrintMode(StorePrintModeEnum.ADVANCE_PRINT.getValue());
            }
        }
        //桌台模式是否变更 && 更改为非桌台模式
        if((param.isEnableTableMode() != storeTableSetting.isEnableTableMode()) && !param.isEnableTableMode()){
            if(!store5weiSetting.isPrintModeDefined()){
                store5weiSettingParam.setPrintMode(StorePrintModeEnum.NORMAL_PRINT.getValue());
            }
        }
        store5weiSettingParam.setPrintModeDefined(store5weiSetting.isPrintModeDefined());
        store5weiSettingService.saveStore5weiSetting(store5weiSettingParam);
    }

    //处理桌台模式变更造成的取餐单和点菜单的变更
    private void changeStoreI5weiMeal(StoreTableSettingSaveParam param,StoreTableSetting storeTableSetting) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        List<StoreArea> storeAreas = storeAreaService.getValidStoreAreas(merchantId,storeId,false);
        List<Long> areaIds = new ArrayList<>();
        for(StoreArea storeArea : storeAreas){
            areaIds.add(storeArea.getAreaId());
        }
        //桌台模式是否变更 && 更改为桌台模式
        if((param.isEnableTableMode() != storeTableSetting.isEnableTableMode()) && param.isEnableTableMode()){
            List<StoreDefinedPrinter> storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId,storeId, PrintMsgTypeEnum.I5WEI_TAKE_CODE.getValue(),false);
            List<StoreDefinedPrinter> storeDefinedPrinterList = new ArrayList<>();
            for(StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters){
                storeDefinedPrinter.setPrintMsgType(PrintMsgTypeEnum.I5WEI_ORDER_MEAL.getValue());
                storeDefinedPrinter.setPrintScope(JsonUtil.build(areaIds));
                storeDefinedPrinterList.add(storeDefinedPrinter);
            }
            storeDefinedPrinterDAO.batchUpdateStoreDefinedPrinter(merchantId,storeId,storeDefinedPrinterList);
        }
        //桌台模式是否变更 && 更改为非桌台模式
        if((param.isEnableTableMode() != storeTableSetting.isEnableTableMode()) && !param.isEnableTableMode()){
            List<StoreDefinedPrinter> storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId,storeId, PrintMsgTypeEnum.I5WEI_ORDER_MEAL.getValue(),false);
            List<StoreDefinedPrinter> storeDefinedPrinterList = new ArrayList<>();
            for(StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters){
                storeDefinedPrinter.setPrintMsgType(PrintMsgTypeEnum.I5WEI_TAKE_CODE.getValue());
                storeDefinedPrinter.setPrintScope(JsonUtil.build(new ArrayList<>()));
                storeDefinedPrinterList.add(storeDefinedPrinter);
            }
            storeDefinedPrinterDAO.batchUpdateStoreDefinedPrinter(merchantId,storeId,storeDefinedPrinterList);
        }
    }

    public StoreTableSetting getStoreTableSetting(long storeId,int merchantId, boolean enableSlave){
        StoreTableSetting storeTableSetting = storeTableSettingDAO.getById(storeId, enableSlave);
        if(storeTableSetting == null){
            storeTableSetting = new StoreTableSetting();
            storeTableSetting.setStoreId(storeId);
            storeTableSetting.setMerchantId(merchantId);
        }
        return storeTableSetting;
    }

    private StoreTableSetting createDBDefault(StoreTableSettingSaveParam param) throws TException {
    	boolean enableSlave = false;
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        StoreTableSetting storeTableSetting = StoreTableSetting.createDefault(merchantId,storeId);
        //第一次开启桌台模式
        try{
            //桌台模式发生改变，店铺打印模式发生相应改变
            this.changeStorePrintMode(param,storeTableSetting);
            //处理桌台模式变更造成的取餐单和点菜单的变更
            this.changeStoreI5weiMeal(param,storeTableSetting);
            BeanUtil.copy(param,storeTableSetting,true);
            storeTableSetting.create();
        }catch (DuplicateKeyException e){
            storeTableSetting = storeTableSettingDAO.getById(storeId, enableSlave);
        }
        //初始化一个默认"大厅"区域
        try{
            storeAreaService.initStoreArea(merchantId,storeId);
        }catch (DuplicateKeyException e){
            log.warn("merchantId["+ merchantId + "] storeId["+ storeId + "] 初始化店铺区域重复!");
        }

        return storeTableSetting;

    }

}
