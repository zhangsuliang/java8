package com.huofu.module.i5wei.order.facade;

import com.huofu.module.i5wei.base.StoreOrderCombinedTaskCons;
import com.huofu.module.i5wei.base.StoreOrderModuleConfig;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderCombinedBiz;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import huofucore.facade.combinedbiz.CombinedBizFacade;
import huofucore.facade.combinedbiz.dto.CombinedBizDTO;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.pay.payment.PayFacade;
import huofucore.facade.pay.payment.PayResultOfPayOrder;
import huofucore.facade.prepaidcard.MerchantPrepaidCardOrderDTO;
import huofucore.facade.prepaidcard.PrepaidCardFacade;
import huofucore.facade.prepaidcard.event.PrepaidCardEventType;
import huofuhelper.module.combinedtask.CombinedTaskData;
import huofuhelper.module.combinedtask.CombinedTaskEvent;
import huofuhelper.module.event.EventData;
import huofuhelper.util.DataUtil;
import huofuhelper.util.MapObject;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by wangxiaoyang on 16/8/20
 */
@Component
@ThriftServlet(name = "i5weiCombinedTaskFacadeServlet", serviceClass = I5weiCombinedTaskFacade.class)
public class I5weiCombinedTaskFacadeImpl implements I5weiCombinedTaskFacade.Iface {

    private static final Log log = LogFactory.getLog(I5weiCombinedTaskFacadeImpl.class);

    @ThriftClient
    private CombinedBizFacade.Iface combinedBizFacade;

    @ThriftClient
    private PrepaidCardFacade.Iface prepaidCardFacade;

    @ThriftClient
    private PayFacade.Iface payFacade;

    @Autowired
    private StoreOrderFacadeImpl storeOrderFacadeImpl;

    @Autowired
    private StoreOrderService storeOrderService;

    @Override
    public void processCombinedTask(String jsonMessage) throws TException {
        CombinedTaskEvent combinedTaskEvent = CombinedTaskEvent.buildFromJson(jsonMessage);
        if (combinedTaskEvent == null) {
            log.error("can not build CombinedTaskEvent.jsonMessage:" + jsonMessage);
            return;
        }
        CombinedBizDTO combinedBizDTO = this.combinedBizFacade.getCombinedBiz(combinedTaskEvent.getCombinedBizId());
        CombinedTaskData combinedTaskData = CombinedTaskData.buildFromJson(combinedBizDTO.getData());
        if (combinedTaskData == null) {
            log.error("can not build CombinedTaskData.json:" + combinedBizDTO.getData());
            return;
        }

        if (combinedTaskData.getSubject().equals(StoreOrderCombinedTaskCons.TASKSUBJECT_BUYPREPAIDCARD_PAYSTOREORDER)) {
            this.processBuyPrepaidCard_payStoreOrder(combinedTaskEvent, combinedTaskData);
        } else {
            log.error("subject[" + combinedTaskData.getSubject() + "] unknown");
        }
    }

    public void processBuyPrepaidCard_payStoreOrder(CombinedTaskEvent combinedTaskEvent, CombinedTaskData combinedTaskData) throws TException {
        EventData eventData = combinedTaskEvent.getEventData();
        if (eventData.getModuleId() == StoreOrderModuleConfig.PREPAIDCARD_MODULE_ID) {
            MapObject mo = new MapObject(combinedTaskData.getDataMap());
            String orderId = mo.getString("orderId");
            String bizId = mo.getString("prepaidCardOrderId"); //充值卡交易订单ID
            int merchantId = mo.getInt("merchantId", 0);
            long storeId = mo.getLong("storeId", 0);

            StoreOrder storeOrder = this.storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
            if (storeOrder.isPayFinish() || storeOrder.isPayFail()) {
                log.warn("order[" + orderId + "],merchant[" + merchantId + "],storeid[" + storeId + "] is pay over,paystatus[" + StoreOrderPayStatusEnum.findByValue(storeOrder.getPayStatus()) + "]");
                return;
            }

            StoreOrderCombinedBiz storeOrderCombinedBiz = new StoreOrderCombinedBiz();
            storeOrderCombinedBiz.setOrderId(orderId);
            storeOrderCombinedBiz.setBizId(bizId);
            storeOrderCombinedBiz.setBizType(StoreOrderCombinedBizType.BUY_PREPAIDCARD_PAY_STORE_ORDER.getValue());
            storeOrderCombinedBiz.setMerchantId(merchantId);
            storeOrderCombinedBiz.setStoreId(storeId);
            storeOrderCombinedBiz.setCreateTime(System.currentTimeMillis());

            if (eventData.getEventType() == PrepaidCardEventType.BUY_FAIL.getValue()) {
                MerchantPrepaidCardOrderDTO merchantPrepaidCardOrder = prepaidCardFacade.getMerchantPrepaidCardOrder(bizId, merchantId);
                log.info("orderId[" + orderId + "] merchantId[" + merchantId + "] storeId[" + storeId + "] merchantPrepaidCardOrderId[" + merchantPrepaidCardOrder.getPrepaidCardOrderId() + "]  buy prepaidcard fail");
                if (DataUtil.isNotEmpty(merchantPrepaidCardOrder.getPayOrderId())) {
                    PayResultOfPayOrder payResultOfPayOrder = payFacade.getPayResultOfPayOrder(merchantPrepaidCardOrder.getPayOrderId());
                    storeOrderCombinedBiz.setErrorCode(payResultOfPayOrder.getErrorCode());
                    storeOrderCombinedBiz.setErrorMsg(payResultOfPayOrder.getErrorMsg());
                }

                this.storeOrderService.processBuyPrepaidcardFail(storeOrderCombinedBiz, storeOrder);
            } else if (eventData.getEventType() == PrepaidCardEventType.BUY_SUCCESS.getValue()) {
                this.storeOrderService.processBuyPrepaidcardSuccess(storeOrderCombinedBiz, merchantId, storeId, orderId);
                StoreOrderPayParam storeOrderPayParam = new StoreOrderPayParam();
                storeOrderPayParam.setMerchantId(merchantId);
                storeOrderPayParam.setStoreId(storeId);
                storeOrderPayParam.setOrderId(orderId);
                storeOrderPayParam.setUserId(mo.getLong("userId", 0));
                storeOrderPayParam.setCouponTypeId(mo.getLong("couponTypeId", 0));
                storeOrderPayParam.setCouponPayAmount(mo.getLong("couponPayAmount", 0));
                storeOrderPayParam.setPrepaidCardPayAmount(mo.getLong("prepaidCardPayAmount", 0));
                storeOrderPayParam.setUserAccountPayAmount(mo.getLong("userAccountPayAmount", 0));
                storeOrderPayParam.setOtherPayAmount(0);
                storeOrderPayParam.setOtherPayMode(StoreOrderPayModeEnum.UNKNOWN);
                storeOrderPayParam.setUserRemark("");
                try {
                    this.storeOrderFacadeImpl.payStoreOrderTradition(storeOrderPayParam);
                } catch (T5weiException e) {
                    if (e.getErrorCode() == T5weiErrorCodeType.STORE_ORDER_PRICE_NOT_EQUAL_INPUT.getValue()) {
                        log.error(e.getMessage());
                    } else {
                        throw e;
                    }
                }
            } else {
                log.error("orderId[" + orderId + "],eventType[" + eventData.getEventType() + "] unknown");
            }
        }
    }
}
