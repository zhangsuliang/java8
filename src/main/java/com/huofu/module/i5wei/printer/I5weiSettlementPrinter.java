package com.huofu.module.i5wei.printer;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.printer.JsonMap.StoreOrderPrinterJsonMap;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import huofucore.facade.merchant.cashier.StoreCashierAutoPrintParam;
import huofucore.facade.merchant.cashier.StoreCashierDTO;
import huofucore.facade.merchant.cashier.StoreCashierFacade;
import huofucore.facade.merchant.printer.PrintMessageParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofucore.facade.merchant.printer.PrintSrcEnum;
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
 * Created by jiajin.nervous on 2016/12/21.
 * 结账单
 */
@Service
public class I5weiSettlementPrinter {

    private static final Log logger = LogFactory.getLog(I5weiRefundMealPrinter.class);

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @ThriftClient
    private StoreCashierFacade.Iface storeCashierFacade;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    private List<PrintMessageParam> getPrintMessages(StoreOrder masterOrder,StoreTableRecord storeTableRecord){

        //是微信自助结账
        if(storeTableRecord.getSettleUserId() <= 0){
            return new ArrayList<>();
        }
        int merchantId = masterOrder.getMerchantId();
        long storeId = masterOrder.getStoreId();
        List<PrintMessageParam> printMessages = new ArrayList<>();
        //获取店铺指定自动打印结账单的收银台
        StoreCashierAutoPrintParam param = new StoreCashierAutoPrintParam();
        param.setMerchantId(merchantId);
        param.setStoreId(storeId);
        param.setAutoPrintTake(true);
        StoreCashierDTO storeCashierDTO = storeOrderHelper.getStoreAutoPrintCashiers(merchantId,storeId,false,true);
        if(storeCashierDTO != null){
            if(storeCashierDTO.getPrinterPeripheralId() > 0){
                //构造打印信息
                PrintMessageParam printMessageParam = this.getPrintMessageParam(masterOrder,storeTableRecord);
                printMessageParam.setPrinterPeripheralId(storeCashierDTO.getPrinterPeripheralId());
                printMessages.add(printMessageParam);
            }
        }
        return printMessages;
    }

    //构造打印信息
    private PrintMessageParam getPrintMessageParam(StoreOrder masterStoreOrder,StoreTableRecord storeTableRecord){
        Map<String, Object> printMessage = new HashMap<String, Object>();
        Map<String,Object> map = StoreOrderPrinterJsonMap.toMap(masterStoreOrder,storeTableRecord);
        printMessage.put("store_order", map);
        PrintMessageParam param = new PrintMessageParam();
        param.setMerchantId(masterStoreOrder.getMerchantId());
        param.setStoreId(masterStoreOrder.getStoreId());
        param.setPrintSrc(PrintSrcEnum.I5WEI_TABLE_RECORD.getValue());
        param.setPrintSrcId(masterStoreOrder.getOrderId());
        param.setUserId(storeTableRecord.getSettleUserId());
        param.setStaffId(0L);
        param.setMsgType(PrintMsgTypeEnum.I5WEI_SETTLE_MEAL.getValue());
        param.setMsgContent(JsonUtil.build(printMessage));
        param.setCreateTime(System.currentTimeMillis());
        return param;
    }


    public void sendPrintMessages(StoreOrder masterStoreOrder,StoreTableRecord storeTableRecord) {
        if(logger.isDebugEnabled()){
            logger.debug("####storeTableRecord="+ JsonUtil.build(masterStoreOrder));
        }
        List<PrintMessageParam> printMessages = this.getPrintMessages(masterStoreOrder,storeTableRecord);
        logger.info("printMessages = " + JsonUtil.build(printMessages));
        i5weiMessageProducer.sendPrintMessages(printMessages, storeTableRecord.getSettleUserId());
    }
}

