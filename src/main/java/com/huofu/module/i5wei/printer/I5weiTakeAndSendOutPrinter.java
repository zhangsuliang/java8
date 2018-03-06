package com.huofu.module.i5wei.printer;

import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.mealport.service.StoreMealPortSendService;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.printer.JsonMap.StoreOrderPrinterJsonMap;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.setting.dao.StoreDefinedPrinterDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreDefinedPrinter;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.merchant.printer.PrintMessageParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofucore.facade.merchant.printer.PrintSrcEnum;
import huofuhelper.util.json.JsonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiajin.nervous on 16/10/20.
 */

/**
 * 打包（自取） + 外卖（清单）
 */
@Service
public class I5weiTakeAndSendOutPrinter {

    private static final Log logger = LogFactory.getLog(I5weiRefundMealPrinter.class);

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private StoreDefinedPrinterDAO storeDefinedPrinterDAO;

    @Autowired
    private StoreMealPortSendDAO storeMealPortSendDAO;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @Autowired
    private StoreMealPortSendService storeMealPortSendService;

    public List<PrintMessageParam> getPrintMessages(StoreOrder storeOrder){
        String orderId = storeOrder.getOrderId();
        int merchantId = storeOrder.getMerchantId();
        long storeId = storeOrder.getStoreId();
        //打印模式
        int printMode = store5weiSettingService.getStore5weiSettingByStoreId(merchantId,storeId).getPrintMode();
        //打印参数信息
        List<PrintMessageParam> printMessages = new ArrayList<PrintMessageParam>();
        //打包清单
        if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()){
            //获取打印打包清单自定义打印点列表
            List<StoreDefinedPrinter> storeDefinedPrinters = new ArrayList<>();
            storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId,storeId, PrintMsgTypeEnum.I5WEI_TAKE_OUT_MEAL.getValue(),true);
            // 构造打印的通知信息
            for(StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters){
                long printerPeripheralId = storeDefinedPrinter.getPrinterPeripheralId();
                String definedprinterName = storeDefinedPrinter.getPrinterName();
                PrintMessageParam param = this.getPrintMessageParam(storeOrder,definedprinterName);
                if(printerPeripheralId > 0){
                    param.setPrinterPeripheralId(printerPeripheralId);
                    printMessages.add(param);
                }
            }
        }


        //外卖清单
        if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()){
            //获取打印外卖清单自定义打印点列表
            List<StoreDefinedPrinter> storeDefinedPrinters = new ArrayList<>();
            storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId,storeId, PrintMsgTypeEnum.I5WEI_SEND_OUT_MEAL.getValue(),true);
            // 构造打印的通知信息
            for(StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters){
                long printerPeripheralId = storeDefinedPrinter.getPrinterPeripheralId();
                String definedprinterName = storeDefinedPrinter.getPrinterName();
                PrintMessageParam param = this.getPrintMessageParam(storeOrder,definedprinterName);
                if(printerPeripheralId > 0){
                    param.setPrinterPeripheralId(printerPeripheralId);
                    printMessages.add(param);
                }
            }
        }

        //高级打印模式，获取店铺打印打包清单的打包台和打印外卖清单的外卖台
        if(printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
            try {
                StoreMealPortSend packageStoreMealPortSend = storeMealPortSendService.getStoreMealPortSendPackageOrDelivery(merchantId,storeId, StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue(),true);
                StoreMealPortSend deliveryStoreMealPortSend = storeMealPortSendService.getStoreMealPortSendPackageOrDelivery(merchantId,storeId, StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue(),true);
                PrintMessageParam param = this.getPrintMessageParam(storeOrder,null);
                //打包清单
                if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()){
                    if(packageStoreMealPortSend != null && packageStoreMealPortSend.getPrinterPeripheralId() > 0){
                        param.setPrinterPeripheralId(packageStoreMealPortSend.getPrinterPeripheralId());
                        printMessages.add(param);
                    }
                }
                //外卖清单
                if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()){
                    if(deliveryStoreMealPortSend != null && deliveryStoreMealPortSend.getPrinterPeripheralId() > 0){
                        param.setPrinterPeripheralId(deliveryStoreMealPortSend.getPrinterPeripheralId());
                        printMessages.add(param);
                    }
                }
            }catch (Exception e){
                logger.error("storeId[" + storeId + "],packageStoreMealPortSend or deliveryStoreMealPortSend is invalid");
            }

        }
        return printMessages;
    }



    //构造打印信息
    private PrintMessageParam getPrintMessageParam(StoreOrder storeOrder,String definedPrinterName){
        Map<String, Object> printMessage = new HashMap<String, Object>();
        Map<String, Object> map = StoreOrderPrinterJsonMap.toMap(storeOrder);
        map.put("defined_printer_name",definedPrinterName);
        printMessage.put("store_order", map);
        PrintMessageParam param = new PrintMessageParam();
        param.setMerchantId(storeOrder.getMerchantId());
        param.setStoreId(storeOrder.getStoreId());
        param.setPrintSrc(PrintSrcEnum.I5WEI_ORDER.getValue());
        param.setPrintSrcId(storeOrder.getOrderId());
        param.setUserId(storeOrder.getUserId());
        param.setStaffId(storeOrder.getStaffId());
        param.setCreateTime(System.currentTimeMillis());
        if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue() || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()){
            param.setMsgType(PrintMsgTypeEnum.I5WEI_TAKE_OUT_MEAL.getValue());
        }
        if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()){
            param.setMsgType(PrintMsgTypeEnum.I5WEI_SEND_OUT_MEAL.getValue());
        }
        param.setMsgContent(JsonUtil.build(printMessage));
        return param;
    }


    public void sendPrintMessages(StoreOrder storeOrder) {
        if(logger.isDebugEnabled()){
            logger.debug("####storeOrder="+JsonUtil.build(storeOrder));
        }
        List<PrintMessageParam> printMessages = this.getPrintMessages(storeOrder);
        logger.info("printMessages == " + JsonUtil.build(printMessages));
        // 发送消息
        i5weiMessageProducer.sendMultiPrintMessages(printMessages,0L);
    }
}
