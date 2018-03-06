package com.huofu.module.i5wei.printer;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.printer.JsonMap.StoreOrderPrinterJsonMap;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.setting.dao.StoreDefinedPrinterDAO;
import com.huofu.module.i5wei.setting.entity.StoreDefinedPrinter;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import huofucore.facade.merchant.cashier.StoreCashierDTO;
import huofucore.facade.merchant.cashier.StoreCashierFacade;
import huofucore.facade.merchant.printer.PrintMessageParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofucore.facade.merchant.printer.PrintSrcEnum;
import huofucore.facade.merchant.store.StoreAutoPrinterCashierDTO;
import huofucore.facade.merchant.store.StoreAutoPrinterCashierFacade;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 取餐单（非桌台模式）
 */
@Service
public class I5weiOrderTakeCodePrinter {

    private static final Log logger = LogFactory.getLog(I5weiRefundMealPrinter.class);

    @ThriftClient
    private StoreCashierFacade.Iface storeCashierFacade;

    @ThriftClient
    private StoreAutoPrinterCashierFacade.Iface storeAutoPrinterCashierFacade;

    @Autowired
    private StoreDefinedPrinterDAO storeDefinedPrinterDAO;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    public List<PrintMessageParam> getPrintMessages(StoreOrder storeOrder){
        String orderId = storeOrder.getOrderId();
        int merchantId = storeOrder.getMerchantId();
        long storeId = storeOrder.getStoreId();
        int clientType = storeOrder.getClientType();
        StoreAutoPrinterCashierDTO storeAutoPrinterCashierDTO = null;
        StoreCashierDTO storeCashierDTO = null;
        List<PrintMessageParam> printMessages = new ArrayList<PrintMessageParam>();
        //判断桌台模式
        if(storeTableSettingService.getStoreTableSetting(storeId,merchantId,true).isEnableTableMode()){
            return new ArrayList<>();
        }
        //获取打印取餐单的自定义打印点
        List<StoreDefinedPrinter> storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByPrintMsgType(merchantId,storeId, PrintMsgTypeEnum.I5WEI_TAKE_CODE.getValue(),true);

        // 构造打印的通知信息
        for(StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters){
            long printerPeripheralId = storeDefinedPrinter.getPrinterPeripheralId();
            String definedPrinterName = storeDefinedPrinter.getPrinterName();
            //构造打印信息
            PrintMessageParam param = this.getPrintMessageParam(storeOrder,definedPrinterName);
            //自定义打印的通知信息
            if(printerPeripheralId > 0){
                param.setPrinterPeripheralId(printerPeripheralId);
                printMessages.add(param);
            }
        }
        //获取店铺指定自动打印取餐单的收银台
        //storeCashierDTO = storeOrderHelper.getStoreAutoPrintCashiers(merchantId,storeId,true,false);

        //远程自助下单（微信 + 网页 + 点菜宝）构造收银台的打印信息
//        if(clientType == ClientTypeEnum.MOBILEWEB.getValue() || clientType == ClientTypeEnum.WECHAT.getValue() || storeOrder.isDiancaibaoPlaceOrder()){
//            if(storeCashierDTO != null){
//                if(storeCashierDTO.getPrinterPeripheralId() > 0){
//                    //构造打印信息
//                    PrintMessageParam printMessageParam = this.getPrintMessageParam(storeOrder,null);
//                    printMessageParam.setPrinterPeripheralId(storeCashierDTO.getPrinterPeripheralId());
//                    printMessages.add(printMessageParam);
//                }
//            }
//        }
        return printMessages;
    }

    //构造打印信息
    private PrintMessageParam getPrintMessageParam(StoreOrder storeOrder,String definedPrinterName){
        Map<String, Object> printMessage = new HashMap<String, Object>();
        Map<String,Object> map = StoreOrderPrinterJsonMap.toMap(storeOrder);
        map.put("defined_printer_name",definedPrinterName);
        printMessage.put("store_order", map);
        PrintMessageParam param = new PrintMessageParam();
        param.setMerchantId(storeOrder.getMerchantId());
        param.setStoreId(storeOrder.getStoreId());
        param.setPrintSrc(PrintSrcEnum.I5WEI_ORDER.getValue());
        param.setPrintSrcId(storeOrder.getOrderId());
        param.setUserId(storeOrder.getUserId());
        param.setStaffId(storeOrder.getStaffId());
        param.setMsgType(PrintMsgTypeEnum.I5WEI_TAKE_CODE.getValue());
        param.setMsgContent(JsonUtil.build(printMessage));
        param.setCreateTime(System.currentTimeMillis());
        return param;
    }

    public void sendPrintMessages(StoreOrder storeOrder) {
        if(logger.isDebugEnabled()){
            logger.debug("####storeOrder="+JsonUtil.build(storeOrder));
        }
        List<PrintMessageParam> printMessages = this.getPrintMessages(storeOrder);
        // 发送消息
        i5weiMessageProducer.sendMultiPrintMessages(printMessages,0L);
    }
}
