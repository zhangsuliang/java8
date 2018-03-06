package com.huofu.module.i5wei.setting.service;

import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.setting.dao.Store5weiSettingDAO;
import com.huofu.module.i5wei.setting.dao.StoreDefinedPrinterDAO;
import com.huofu.module.i5wei.setting.dao.StoreTableSettingDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreDefinedPrinter;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.table.dao.StoreAreaDAO;
import com.huofu.module.i5wei.table.entity.StoreArea;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.store5weisetting.StoreDefinedPrinterDTO;
import huofucore.facade.i5wei.store5weisetting.StoreDefinedPrinterParam;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.i5wei.store5weisetting.StoreTableSettingDTO;
import huofucore.facade.idmaker.IdMakerFacade;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by jiajin.nervous on 16/10/10.
 */
@Service
public class StoreDefinedPrinterService {

    @Autowired
    private StoreDefinedPrinterDAO storeDefinedPrinterDAO;

    @Autowired
    private Store5weiSettingDAO store5weiSettingDAO;

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private StoreAreaDAO storeAreaDAO;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @ThriftClient
    private IdMakerFacade.Iface idMakerFacade;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreDefinedPrinter saveStoreDefinedPrinter(StoreDefinedPrinterParam param) throws T5weiException {
        List<Long> printScope = param.getPrintScope();
        //非桌台模式
        long printerId = param.getPrinterId();
        long storeId = param.getStoreId();
        int merchantId = param.getMerchantId();
        StoreDefinedPrinter storeDefinedPrinter;
        try{
            if(printerId == 0){
                storeDefinedPrinter = new StoreDefinedPrinter();
                printerId = this.nextId(storeId);
                storeDefinedPrinter.setPrinterId(printerId);
                storeDefinedPrinter.setMerchantId(merchantId);
                storeDefinedPrinter.setStoreId(storeId);
                storeDefinedPrinter.setPrinterName(param.getPrinterName());
                storeDefinedPrinter.setPrinterPeripheralId(param.getPrinterPeripheralId());
                storeDefinedPrinter.setPrintMsgType(param.getPrintMsgType());
                storeDefinedPrinter.setPrintScope(JsonUtil.build(printScope));
                storeDefinedPrinter.setCreateTime(System.currentTimeMillis());
                storeDefinedPrinter.setUpdateTime(System.currentTimeMillis());
                storeDefinedPrinter.create();
            }else{
                storeDefinedPrinter = storeDefinedPrinterDAO.loadById(merchantId,storeId,printerId,false);
                storeDefinedPrinter.snapshot();
                storeDefinedPrinter.setPrinterName(param.getPrinterName());
                storeDefinedPrinter.setPrinterPeripheralId(param.getPrinterPeripheralId());
                storeDefinedPrinter.setPrintMsgType(param.getPrintMsgType());
                storeDefinedPrinter.setPrintScope(JsonUtil.build(param.getPrintScope()));
                storeDefinedPrinter.setUpdateTime(System.currentTimeMillis());
                storeDefinedPrinter.update();
            }
        }catch (DuplicateKeyException e){
            throw new T5weiException(T5weiErrorCodeType.STORE_DEFINED_PRINTER_NAME_DUPLICATE.getValue(),"storeId[" + storeId + "],printerId[" + printerId + "],printerName[" + param.getPrinterName() + "] has been exist");
        }
        return storeDefinedPrinter;
    }


    public StoreDefinedPrinter getStoreDefinedPrinterById(int merchantId,long storeId,long printerId) throws T5weiException {
        return storeDefinedPrinterDAO.loadById(merchantId, storeId, printerId,true);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteStoreDefinedPrinter(int merchantId,long storeId,long printerId) throws T5weiException {
        StoreDefinedPrinter storeDefinedPrinter =  storeDefinedPrinterDAO.loadById(merchantId, storeId, printerId,false);
        storeDefinedPrinter.delete();
    }

    /**
     * 过滤自定打印中包含加工档口或者区域的打印范围
     * @param merchantId
     * @param storeId
     * @param scopeId
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void filterStoreDefinedPrinter(int merchantId,long storeId,long scopeId,int printMsgType){
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId,merchantId,false);
        List<StoreDefinedPrinter> storeDefinedPrinters = new ArrayList<>();
        //后厨清单
        if(printMsgType == PrintMsgTypeEnum.I5WEI_KITCHEN_MEAL_LIST.getValue()){
            //高级打印模式
            if(store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
                storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId,storeId,PrintMsgTypeEnum.I5WEI_KITCHEN_MEAL_LIST.getValue(),false);
            }
        }

        if(printMsgType == PrintMsgTypeEnum.I5WEI_ORDER_MEAL.getValue()){
            //桌台模式
            if(storeTableSetting.isEnableTableMode()){
                storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId,storeId,PrintMsgTypeEnum.I5WEI_ORDER_MEAL.getValue(),false);
            }
        }
        List<StoreDefinedPrinter> storeDefinedPrinterList = new ArrayList<>();
        for(StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters){
            String printScope = storeDefinedPrinter.getPrintScope();
            if(DataUtil.isNotEmpty(printScope)){
                List<Integer> printScopes = JsonUtil.parse(printScope,List.class);
                Iterator iterator = printScopes.iterator();
                printScopes = new ArrayList<>();
                while(iterator.hasNext()){
                    int scope = (int)iterator.next();
                    if(Long.valueOf(String.valueOf(scope)) == scopeId){
                        iterator.remove();
                    }else{
                        printScopes.add(scope);
                    }
                }
                printScope = JsonUtil.build(printScopes);
                storeDefinedPrinter.setPrintScope(printScope);
                storeDefinedPrinterList.add(storeDefinedPrinter);
            }
        }
        //批量更新
        storeDefinedPrinterDAO.batchUpdateStoreDefinedPrinter(merchantId,storeId,storeDefinedPrinterList);
    }

    /**
     * 获取idMaker生成的自定义打印ID
     * @param storeId
     * @return
     * @throws T5weiException
     */
    private long nextId(long storeId) throws T5weiException {
        //需要捕获异常，重试机制
        long tableSeq = 0;
        for(int i = 0; i < 4; i++){
            try {
                tableSeq = this.idMakerFacade.getNextId2("tb_store_defined_printer_seq");
                break;
            } catch (Exception e) {
                if(i == 3){
                    throw new T5weiException(T5weiErrorCodeType.STORE_DEFINED_PRINTER_CREATE_ERROR.getValue(),"create storeDefinedPrinter failed，because request IdMaker failed");
                }else{
                    continue;
                }
            }
        }
        return tableSeq;
    }
}
