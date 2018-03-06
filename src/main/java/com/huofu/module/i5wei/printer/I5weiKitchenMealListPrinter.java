package com.huofu.module.i5wei.printer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.meal.service.StoreMealMultiHelper;
import com.huofu.module.i5wei.meal.service.StoreMealMultiService;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.printer.JsonMap.StoreMealJsonMap;
import com.huofu.module.i5wei.printer.JsonMap.StoreOrderPrinterJsonMap;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.setting.dao.StoreDefinedPrinterDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreDefinedPrinter;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.merchant.printer.PrintMessageParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofucore.facade.merchant.printer.PrintSrcEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.json.JsonUtil;

/**
 * Created by jiajin.nervous on 16/10/25.
 * 自定打印的后厨清单 + 传菜间的后厨清单（只打印堂食的部分）
 */
@Service
public class I5weiKitchenMealListPrinter {

    private static final Log logger = LogFactory.getLog(I5weiRefundMealPrinter.class);

    @Autowired
    private StoreMealMultiHelper storeMealMultiHelper;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private StoreDefinedPrinterDAO storeDefinedPrinterDAO;

    @Autowired
    private StoreMealPortSendDAO storeMealPortSendDAO;

    @Autowired
    private StorePrinterHelper storePrinterHelper;

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private StoreTableRecordDAO storeTableRecordDAO;

    @Autowired
    private StoreMealPortService storeMealPortService;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @Autowired
    private StoreMealPortDAO storeMealPortdDAO;

    @Autowired
    private StoreMealMultiService storeMealMultiService;
    
    @Autowired
    private StoreOrderDAO storeOrderDAO;

    public List<PrintMessageParam> getPrintMessages(int merchantId, long storeId, long tableRecordId,List<String> orderIds,List<StoreMealTakeup> storeMealTakeups) {
        List<PrintMessageParam> printMessages = new ArrayList<PrintMessageParam>();
        if(orderIds == null || orderIds.isEmpty() || storeMealTakeups == null || storeMealTakeups.isEmpty()){
            return printMessages;
        }
        //获取店铺桌台模式设置
        StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, true);
        StoreTableRecord storeTableRecord = null;
        try {
            //订单所属的桌台记录
            if (storeTableSetting.isEnableTableMode() && tableRecordId != 0) {
                storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, false);
            }
        } catch (T5weiException e) {
            logger.warn("I5weiKitchenMealListPrinter  storeId[" + storeId + "],tableRecordId[" + storeTableRecord + "] is invalid");
        }
        //获取店铺设
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        int printMode = store5weiSetting.getPrintMode();
        List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrdersById(merchantId, storeId, orderIds, false);
        if (storeOrders.isEmpty()) {
            logger.error("I5weiKitchenMealListPrinter, List<StoreOrder> is empty");
            return printMessages;
        }
        Map<String, StoreOrder> storeOrderMap = new HashMap<>();
        for (StoreOrder storeOrder : storeOrders) {
            //快取不打印后厨清单
            if(storeOrder.getTakeMode() != StoreOrderTakeModeEnum.QUICK_TAKE.getValue()){
                storeOrderMap.put(storeOrder.getOrderId(), storeOrder);
            }
        }
        if(storeOrderMap.isEmpty()){
            return printMessages;
        }
        //店铺的传菜间Map
        Map<Long, StoreMealPortSend> storeMealPortSendMap;
        //获取打印后厨清单的自定义打印列表
        List<StoreDefinedPrinter> storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId, storeId, PrintMsgTypeEnum.I5WEI_KITCHEN_MEAL_LIST.getValue(), true);
        //需要统计的产品
        List<StoreProduct> products = storeMealMultiService.getStoreProductByMeals(storeMealTakeups);
        Map<Long, StoreMealPort> storeMealPortMap = new HashMap<>();
        //高级模式
        if (printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
            //店铺的传菜间
            storeMealPortSendMap = storeMealPortSendDAO.getStoreMealPortSendMap(merchantId, storeId, StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue(), true);
            //构造传菜口ID列表(去重)
            Set<Long> storeMealSendPortIds = new HashSet<>();
            Set<Long> storeMealPortIds = new HashSet<>();
            for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
                storeMealPortIds.add(storeMealTakeup.getPortId());
            }
            List<StoreMealPort> storeMealPorts = storeMealPortdDAO.getByIds(merchantId, storeId, new ArrayList<>(storeMealPortIds), true);
            for (StoreMealPort storeMealPort : storeMealPorts) {
                storeMealSendPortIds.add(storeMealPort.getSendPortId());
                storeMealPortMap.put(storeMealPort.getPortId(), storeMealPort);
            }
            //获取订单的分单信息
            List<StoreMealDTO> storeMealDTOs = storeMealMultiHelper.getStoreMealDTOByKitchenTakeups(storeMealTakeups, products, storeMealPortMap, false);
            //按照订单对StoreMealDTO进行分类
            Map<String, List<StoreMealDTO>> storeMealDTOListMap = new HashMap<>();
            for (StoreMealDTO storeMealDTO : storeMealDTOs) {
                List<StoreMealDTO> storeMealDTOList = storeMealDTOListMap.get(storeMealDTO.getOrderId());
                if (storeMealDTOList == null || storeMealDTOList.isEmpty()) {
                    storeMealDTOList = new ArrayList<>();
                }
                storeMealDTOList.add(storeMealDTO);
                storeMealDTOListMap.put(storeMealDTO.getOrderId(), storeMealDTOList);
            }
            for (String orderId : storeMealDTOListMap.keySet()) {
                storeMealDTOs = storeMealDTOListMap.get(orderId);
                StoreOrder storeOrder = storeOrderMap.get(orderId);
                //组装Map<加工档口Id,List<StoreMealDTO>>
                Map<String, List<StoreMealDTO>> storeMealPortPrintMealMap = storePrinterHelper.getStoreMealMap4AdvancePrinter(storeMealDTOs, storeMealPortMap);
                //组装传菜口需要打印的数据<传菜口ID，Map<String,Object>>
                if (storeMealPortPrintMealMap.isEmpty()) {
                    return printMessages;
                }
                //组装传菜口打印信息
                Map<Long, List<Map<String, Object>>> printMessageMap4SendPort = new HashMap<>();
                for (Long sendPortId : storeMealSendPortIds) {
                    for (String key : storeMealPortPrintMealMap.keySet()) {
                        String[] keys = key.split("_");
                        long sendPortIdInKeys = Long.valueOf(keys[1]);
                        if (sendPortId == sendPortIdInKeys) {
                            List<Map<String, Object>> list = printMessageMap4SendPort.get(sendPortId);
                            if (list == null || list.isEmpty()) {
                                list = new ArrayList<>();
                            }
                            List<Map<String, Object>> mapList = StoreMealJsonMap.toStoreMealMapList(storeMealPortPrintMealMap.get(key), storeMealPortMap);
                            if (mapList.isEmpty()) {
                                continue;
                            }
                            list.addAll(mapList);
                            printMessageMap4SendPort.put(sendPortId, list);
                        }
                    }
                }

                //组装每个自定义打印需要打印的数据<自定义打印ID，Map<Long,List<Map<String,Object>>>>  <自定义打印的打印机ID，打印信息>
                Map<Long, List<Map<String, Object>>> printMessageMap4DefinedPrinter = new HashMap<>();
                for (StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters) {
                    long printerPeripheralId = storeDefinedPrinter.getPrinterPeripheralId();
                    String printScope = storeDefinedPrinter.getPrintScope();
                    if (DataUtil.isNotEmpty(printScope)) {
                        List<Integer> scopes = JsonUtil.parse(printScope, List.class);
                        for (Integer scope : scopes) {
                            for (String key : storeMealPortPrintMealMap.keySet()) {
                                String[] keys = key.split("_");
                                long portId = Long.valueOf(keys[0]);
                                if (scope == portId) {
                                    List<Map<String, Object>> list = printMessageMap4DefinedPrinter.get(printerPeripheralId);
                                    if (list == null || list.isEmpty()) {
                                        list = new ArrayList<>();
                                    }
                                    List<Map<String, Object>> mapList = StoreMealJsonMap.toStoreMealMapList(storeMealPortPrintMealMap.get(key), storeMealPortMap);
                                    if (mapList.isEmpty()) {
                                        continue;
                                    }
                                    list.addAll(mapList);
                                    if (printerPeripheralId > 0) {
                                        printMessageMap4DefinedPrinter.put(printerPeripheralId, list);
                                    }
                                }
                            }
                        }
                    }
                }
                //构造传菜口打印信息
                for (Long sendPortId : printMessageMap4SendPort.keySet()) {
                    StoreMealPortSend storeMealPortSend = storeMealPortSendMap.get(sendPortId);
                    PrintMessageParam param = this.getPrintMessageParam(storeOrder, storeTableSetting, storeTableRecord, printMessageMap4SendPort.get(sendPortId),null);
                    if (storeMealPortSend != null) {
                        if (storeMealPortSend.getPrinterPeripheralId() > 0) {
                            param.setPrinterPeripheralId(storeMealPortSend.getPrinterPeripheralId());
                            //高级模式纸划菜,传菜间打印后厨清单
                            if (storeMealPortSend.getSweepType() == StoreMealSweepTypeEnum.PAPER_SWEEP.getValue()) {
                                printMessages.add(param);
                            }
                        }
                    }
                }
                //构造自定义打印的后厨清单打印信息
                this.addPrintMessageDefinedPrinter(printMessageMap4DefinedPrinter, storeOrder, storeTableSetting, storeTableRecord, storeDefinedPrinters, printMessages);
            }
        } else {
            if (printMode == StorePrintModeEnum.NORMAL_PRINT.getValue()) {
                Set<Long> StoreMealportIds = new HashSet<Long>();
                for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
                    StoreMealportIds.add(storeMealTakeup.getPortId());
                }
                storeMealPortMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, new ArrayList<Long>(StoreMealportIds));
                //普通模式分单
                List<StoreMealDTO> storeMealDTOs = storeMealMultiHelper.getStoreMealDTOByKitchenTakeups(storeMealTakeups, products, storeMealPortMap, false);
                if (storeMealDTOs == null || storeMealDTOs.isEmpty()) {
                    //直接返回
                    return printMessages;
                }
                //获取Map<portId，List<StoreMealDTO>>
                Map<Long, List<StoreMealDTO>> storeMealMap = storePrinterHelper.getStoreMealMap4NormalPrinter(storeMealDTOs);
                //组装每个自定义打印需要打印的数据<自定义打印ID，Map<Long,List<Map<String,Object>>>>  <自定义打印的打印机ID，打印信息>
                Map<Long, List<Map<String, Object>>> printMessageMap4DefinedPrinter = new HashMap<>();
                for (StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters) {
                    long printerPeripheralId = storeDefinedPrinter.getPrinterPeripheralId();
                    String printScope = storeDefinedPrinter.getPrintScope();
                    if (DataUtil.isNotEmpty(printScope)) {
                        List<Integer> temps = JsonUtil.parse(printScope, List.class);
                        List<Long> scopes = new ArrayList<>();
                        for (Integer scope : temps) {
                            scopes.add(Long.valueOf(String.valueOf(scope)));
                        }
                        for (Long key : storeMealMap.keySet()) {
                            if (scopes.contains(key)) {
                                this.makePrintMessageMap4DefinedPrinter(printMessageMap4DefinedPrinter, printerPeripheralId, storeMealMap, key, storeMealPortMap);
                            }
                        }
                    }
                }
                //构造自定义打印的后厨清单打印信息
                this.addPrintMessageDefinedPrinter(printMessageMap4DefinedPrinter, storeOrders.get(0), storeTableSetting, storeTableRecord, storeDefinedPrinters ,printMessages);
            }
        }
        return printMessages;
    }

    //构造自定义打印信息
    public void addPrintMessageDefinedPrinter(Map<Long, List<Map<String, Object>>> printMessageMap4DefinedPrinter, StoreOrder storeOrder, StoreTableSetting storeTableSetting, StoreTableRecord storeTableRecord, List<StoreDefinedPrinter> storeDefinedPrinters,List<PrintMessageParam> printMessages) {
        for (StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters) {
            Long printerPeripheralId = storeDefinedPrinter.getPrinterPeripheralId();
            List<Map<String,Object>> mapList = printMessageMap4DefinedPrinter.get(printerPeripheralId);
            if(mapList == null || mapList.isEmpty()){
                return ;
            }
            PrintMessageParam param = this.getPrintMessageParam(storeOrder, storeTableSetting, storeTableRecord, mapList ,storeDefinedPrinter.getPrinterName());
            if (printerPeripheralId > 0) {
                param.setPrinterPeripheralId(printerPeripheralId);
                printMessages.add(param);
            }
        }
    }

    //组装自定义打印信息
    public Map<Long, List<Map<String, Object>>> makePrintMessageMap4DefinedPrinter(Map<Long, List<Map<String, Object>>> printMessageMap4DefinedPrinter, long printerPeripheralId, Map<Long, List<StoreMealDTO>> storeMealMap, Long key, Map<Long, StoreMealPort> storeMealPortMap) {
        List<Map<String, Object>> list = printMessageMap4DefinedPrinter.get(printerPeripheralId);
        if (list == null || list.isEmpty()) {
            list = new ArrayList<>();
        }
        List<Map<String, Object>> mapList = StoreMealJsonMap.toStoreMealMapList(storeMealMap.get(key), storeMealPortMap);
        if (!mapList.isEmpty()) {
            list.addAll(mapList);
            printMessageMap4DefinedPrinter.put(printerPeripheralId, list);
        }
        return printMessageMap4DefinedPrinter;
    }

    //构造打印信息
    private PrintMessageParam getPrintMessageParam(StoreOrder storeOrder, StoreTableSetting storeTableSetting, StoreTableRecord storeTableRecord, List<Map<String, Object>> StoreMealPrinterMap,String definedprinterName) {
        Map<String, Object> printMessage = new HashMap<String, Object>();
        PrintMessageParam param = new PrintMessageParam();
        param.setMerchantId(storeOrder.getMerchantId());
        param.setStoreId(storeOrder.getStoreId());
        param.setPrintSrcId(storeOrder.getOrderId());
        param.setUserId(storeOrder.getUserId());
        param.setStaffId(storeOrder.getStaffId());
        param.setMsgType(PrintMsgTypeEnum.I5WEI_KITCHEN_MEAL_LIST.getValue());
        if (storeTableSetting.isEnableTableMode()) {
            if(storeTableRecord != null){
                Map<String,Object> map = StoreOrderPrinterJsonMap.toMap(storeOrder, storeTableRecord, StoreMealPrinterMap);
                map.put("defined_printer_name",definedprinterName);
                printMessage.put("store_order", map);
            }else{
                Map<String,Object> map = StoreOrderPrinterJsonMap.toMap(storeOrder, StoreMealPrinterMap);
                map.put("defined_printer_name",definedprinterName);
                printMessage.put("store_order", map);
            }
            param.setPrintSrc(PrintSrcEnum.I5WEI_TABLE_RECORD.getValue());
        } else {
            Map<String,Object> map = StoreOrderPrinterJsonMap.toMap(storeOrder, StoreMealPrinterMap);
            map.put("defined_printer_name",definedprinterName);
            printMessage.put("store_order", map);
            param.setPrintSrc(PrintSrcEnum.I5WEI_ORDER.getValue());
        }
        param.setMsgContent(JsonUtil.build(printMessage));
        return param;
    }


    public void sendPrintMessages(int merchantId, long storeOrder, long tableRecordId, List<String> orderIds,List<StoreMealTakeup> storeMealTakeups) {
        if (logger.isDebugEnabled()) {
            logger.debug("####storeOrder=" + JsonUtil.build(storeOrder));
        }
        List<PrintMessageParam> printMessages = this.getPrintMessages(merchantId, storeOrder, tableRecordId, orderIds, storeMealTakeups);
        // 发送消息
        i5weiMessageProducer.sendMultiPrintMessages(printMessages, 0L);
    }
}
