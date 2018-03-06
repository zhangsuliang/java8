package com.huofu.module.i5wei.printer;

import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.printer.JsonMap.StoreOrderPrinterJsonMap;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepTypeEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.merchant.printer.PrintMessageParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofucore.facade.merchant.printer.PrintSrcEnum;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by jiajin.nervous on 16/10/27.
 * 起菜单
 */
@Service
public class I5weiSendPrinter {

    @Autowired
    private StoreMealCheckoutDAO storeMealCheckoutDAO;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private StoreTableRecordDAO storeTableRecordDAO;

    @Autowired
    private StoreMealPortService storeMealPortService;

    @Autowired
    private StoreMealPortSendDAO storeMealPortSendDAO;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @Autowired
    private StoreOrderDAO storeOrderDAO;

    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDAO;

    private static final Log logger = LogFactory.getLog(I5weiRefundMealPrinter.class);

    public List<PrintMessageParam> getPrintMessages(StoreTableRecord storeTableRecord,List<StoreOrder> storeOrders,long sendTime,long staffId){
        int merchantId = storeTableRecord.getMerchantId();
        long storeId = storeTableRecord.getStoreId();
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId,storeId);
        StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId,merchantId,true);
        if(storeTableSetting.isEnableTableMode()){
            try {
                storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId,storeId,storeTableRecord.getTableRecordId(),false);
            } catch (T5weiException e) {
                logger.error("I5weiSendPrinter, storeId[" + storeId + "],storeTableRecordId[" + storeTableRecord.getTableRecordId() + "] is invalid");
                return new ArrayList<>();
            }
        }
        //打印模式
        int printMode = store5weiSetting.getPrintMode();
        List<PrintMessageParam> printMessages = new ArrayList<>();
        //根据桌台记录ID和起菜状态获取桌台记录下的子订单
        List<String> orderIds = new ArrayList<>();
        for(StoreOrder storeOrder : storeOrders){
            orderIds.add(storeOrder.getOrderId());
        }
        //根据订单查询已出餐列表
        List<StoreMealTakeup> storeMealTakeups = storeMealTakeupDAO.getStoreMealTakeupsByOrderIds(merchantId,storeId,orderIds,false,false,true);
        //加工档口（出餐口）ID列表
        Set<Long> storeMealPortIds = new HashSet<>();
        Set<Long> storeMealPortSendIds = new HashSet<>();
        for(StoreMealTakeup storeMealTakeup : storeMealTakeups){
            storeMealPortIds.add(storeMealTakeup.getPortId());
            if(printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
               storeMealPortSendIds.add(storeMealTakeup.getSendPortId());//last update by lizhijun
            }
        }
        Map<Long, StoreMealPort> storeMealPortMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, new ArrayList<>(storeMealPortIds));
        //构造加工档口（出餐口）打印信息
        for(Long portId : storeMealPortIds){
            PrintMessageParam param = this.getPrintMessageParam(storeTableRecord,sendTime,staffId);
            StoreMealPort storeMealPort = storeMealPortMap.get(portId);
            if(storeMealPort != null){
                if(storeMealPort.getPrinterPeripheralId() > 0){
                    param.setPrinterPeripheralId(storeMealPort.getPrinterPeripheralId());
                    printMessages.add(param);
                }
            }
        }

        //高级模式，纸划菜，传菜口打印起菜单
        if(printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
            //高级模式获取传菜口
            Map<Long,StoreMealPortSend> storeMealPortSendMap = storeMealPortSendDAO.getStoreMealPortSendMap(merchantId,storeId, StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue(),true);
            for(Long sendPortId : storeMealPortSendIds){
                PrintMessageParam param = this.getPrintMessageParam(storeTableRecord,sendTime,staffId);
                StoreMealPortSend storeMealPortSend = storeMealPortSendMap.get(sendPortId);
                if(storeMealPortSend == null){
                    continue;
                }
                //划菜类型
                int sweepType = storeMealPortSend.getSweepType();
                if(sweepType == StoreMealSweepTypeEnum.PAPER_SWEEP.getValue()){
                    if(storeMealPortSend.getPrinterPeripheralId() > 0){
                        param.setPrinterPeripheralId(storeMealPortSend.getPrinterPeripheralId());
                        printMessages.add(param);
                    }
                }
            }
        }
        return printMessages;
    }


    //构造打印信息
    private PrintMessageParam getPrintMessageParam(StoreTableRecord storeTableRecord,long sendTime,long staffId){
        Map<String, Object> printMessage = new HashMap<String, Object>();
        PrintMessageParam param = new PrintMessageParam();
        param.setMerchantId(storeTableRecord.getMerchantId());
        param.setStoreId(storeTableRecord.getStoreId());
        param.setPrintSrc(PrintSrcEnum.I5WEI_TABLE_RECORD.getValue());
        printMessage.put("store_table_record", StoreOrderPrinterJsonMap.toMap(storeTableRecord,sendTime,staffId));
        param.setPrintSrcId(storeTableRecord.getOrderId());
        param.setUserId(0L);
        param.setStaffId(staffId);
        param.setMsgType(PrintMsgTypeEnum.I5WEI_SEND_MEAL.getValue());
        param.setMsgContent(JsonUtil.build(printMessage));
        param.setCreateTime(System.currentTimeMillis());
        return param;
    }

    public void sendPrintMessages(StoreTableRecord storeTableRecord,List<StoreOrder> storeOrders ,long sendTime,long staffId) {
        if(logger.isDebugEnabled()){
            logger.debug("####storeTableRecord="+JsonUtil.build(storeTableRecord));
        }
        List<PrintMessageParam> printMessages = this.getPrintMessages(storeTableRecord,storeOrders,sendTime,staffId);
        i5weiMessageProducer.sendMultiPrintMessages(printMessages, staffId);
    }

}
