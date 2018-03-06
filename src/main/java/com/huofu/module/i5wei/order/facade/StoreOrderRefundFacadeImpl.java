package com.huofu.module.i5wei.order.facade;

import com.huofu.module.i5wei.order.dao.StoreOrderRefundRecordDAO;
import com.huofu.module.i5wei.order.entity.RefundCallbackResult;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundRecord;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderRefundService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.pay.payment.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单退款服务
 * Created by akwei on 6/27/16.
 */
@Component
@ThriftServlet(name = "storeOrderRefundFacadeServlet", serviceClass = StoreOrderRefundFacade.class)
public class StoreOrderRefundFacadeImpl implements StoreOrderRefundFacade.Iface {

    private static final Log log = LogFactory.getLog(StoreOrderRefundFacadeImpl.class);

    @ThriftClient
    private RefundFacade.Iface refundFacade;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderFacadeValidate storeOrderFacadeValidate;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    @Autowired
    private StoreOrderRefundService storeOrderRefundService;

    @Autowired
    private StoreOrderFacade.Iface storeOrderFacade;

    @Autowired
    private StoreOrderRefundRecordDAO storeOrderRefundRecordDAO;

    @Override
    public I5weiCreateRefundRecordResult createRefundRecord(I5weiCreateRefundRecordParam param) throws TException {
        return this.doCreateRefundRecord(param);
    }

    @Override
    public void requestRefund(I5weiRequestRefundParam param) throws TException {
        this.doRequestRefund(param);
    }

    @Override
    public I5weiCheckRefundResult checkRefundResult(int merchantId, long storeId, String orderId, long refundRecordId) throws TException {
        StoreOrderRefundRecord storeOrderRefundRecord = this.storeOrderRefundService.loadStoreOrderRefundRecord(merchantId, storeId, refundRecordId);
        I5weiCheckRefundResult i5weiCheckRefundResult = new I5weiCheckRefundResult();
        i5weiCheckRefundResult.setRefundStatus(storeOrderRefundRecord.getStatus());
        if (storeOrderRefundRecord.isSuccess()) {
            StoreOrderDTO storeOrderDTO = this.storeOrderFacade.getStoreOrderDetailById(merchantId, storeId, orderId);
            i5weiCheckRefundResult.setStoreOrderDTO(storeOrderDTO);
        } else if (storeOrderRefundRecord.isFail()) {
            i5weiCheckRefundResult.setErrorCode(storeOrderRefundRecord.getErrorCode());
            i5weiCheckRefundResult.setErrorMsg(storeOrderRefundRecord.getErrorMsg());
        }
        return i5weiCheckRefundResult;
    }

    @Override
    public void processRefundCallback(I5weiRefundCallbackParam param) throws TException {
        this.doProcessCallBack(param);
    }

    @Override
    public StoreOrderDTO refund4NotPay(huofucore.facade.i5wei.order.I5weiRefund4NotPayParam param) throws TException {
        long staffId = param.getStaffId();
        StoreOrder storeOrder = storeOrderService.getStoreOrderById4Update(param.getMerchantId(), param.getStoreId(), param.getOrderId());
        //退款校验
        storeOrderFacadeValidate.refundPartStoreOrder(storeOrder);
        if (storeOrder.getPayablePrice() == 0) {
            StoreOrder refundOrder = storeOrderService.refundStoreOrder(storeOrder, staffId, StoreOrderRefundStatusEnum.MERCHANT_ALL, 0);
            return storeOrderHelper.getStoreOrderDTOByEntity(refundOrder);
        }
        throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                "storeId[" + storeOrder.getStoreId() + "] orderId[" + storeOrder.getOrderId() + "] can not refund for payablePrice[" + storeOrder.getPayablePrice() + "]>0");
    }

    /**
     * 使用场景:
     * 定时任务调用
     * ipad取消预订订单
     */
    @Override
    public I5weiCreateRefundRecordResult orderRefund(I5weiCreateRefundRecordParam param) throws TException {
        //1.创建退款记录
        I5weiCreateRefundRecordResult i5weiCreateRefundRecordResult = this.doCreateRefundRecord(param);
        I5weiRequestRefundParam i5weiRequestRefundParam = new I5weiRequestRefundParam();
        i5weiRequestRefundParam.setOrderId(param.getOrderId());
        i5weiRequestRefundParam.setMerchantId(param.getMerchantId());
        i5weiRequestRefundParam.setStoreId(param.getStoreId());
        i5weiRequestRefundParam.setRefundRecordId(i5weiCreateRefundRecordResult.getRefundRecordId());
        i5weiRequestRefundParam.setStoreOrderRefundVersion(param.getStoreOrderRefundVersion());
        //2.请求退款
        this.doRequestRefund(i5weiRequestRefundParam);
        return i5weiCreateRefundRecordResult;
    }


    /**
     * 请求m-pay创建退款记录
     *
     * @param param 请求参数
     */
    private I5weiCreateRefundRecordResult doCreateRefundRecord(I5weiCreateRefundRecordParam param) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String orderId = param.getOrderId();
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        if (storeOrder.getPayablePrice() == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                    "storeId[" + storeId + "] orderId[" + orderId + "] payablePrice=0");
        }
        //校验退菜
        storeOrderFacadeValidate.refundStoreOrderItemValidate(param.getRefundOrderItems(), storeOrder);
        //预定订单退款验证
        if (storeOrder.isPreOrder()) {
            if(param.getI5weiRefundParam() != null){
                //目前不允许预定订单自定义退款
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                        "storeId[" + storeId + "] orderId[" + orderId + "] preOrder can not define refund");
            }
            StoreOrderRefundParam storeOrderRefundParam = new StoreOrderRefundParam();
            BeanUtil.copy(param, storeOrderRefundParam);
            //退款校验
            storeOrderFacadeValidate.refundStoreOrderValidate(storeOrderRefundParam, storeOrder);
            long userId = storeOrder.getUserId();
            String payOrderId = storeOrder.getPayOrderId();
            if (payOrderId == null || payOrderId.isEmpty()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                        DataUtil.infoWithParams("storeId=#1, orderId=#2, userId=#3, payOrderId is null ", new Object[]{storeId, orderId, userId}));
            }
        }
        CreateRefundRecordParam createRefundRecordParam = new CreateRefundRecordParam();
        BeanUtil.copy(param, createRefundRecordParam);
        createRefundRecordParam.setPaySrc(PaySrcEnum.M_5WEI.getValue());
        createRefundRecordParam.setRefundBusinId(param.getOrderId());
        createRefundRecordParam.setPreOrder(storeOrder.isPreOrder());
        createRefundRecordParam.setRefundVersion(param.getStoreOrderRefundVersion());
        if (param.getI5weiRefundParam() != null) {
            RefundParam refundParam = new RefundParam();
            BeanUtil.copy(param.getI5weiRefundParam(), refundParam);
            refundParam.setVoucherRefundPayRecordIds(param.getI5weiRefundParam().getVoucherRefundPayRecordIds());
            createRefundRecordParam.setRefundParam(refundParam);
        }
        RefundRecordDBDTO refundRecordDBDTO;
        try {
            refundRecordDBDTO = this.refundFacade.createRefundRecord(createRefundRecordParam);
        } catch (TPayException e) {
            throw new T5weiException(e.getErrorCode(), e.getMessage());
        }

        StoreOrderRefundRecord refundRecord = this.storeOrderRefundService.createRefundRecord(param, refundRecordDBDTO);
        I5weiCreateRefundRecordResult i5weiCreateRefundRecordResult = new I5weiCreateRefundRecordResult();
        i5weiCreateRefundRecordResult.setOrderId(orderId);
        i5weiCreateRefundRecordResult.setMerchantId(merchantId);
        i5weiCreateRefundRecordResult.setStoreId(storeId);
        i5weiCreateRefundRecordResult.setRefundRecordId(refundRecord.getRefundRecordId());
        return i5weiCreateRefundRecordResult;
    }

    private void doRequestRefund(I5weiRequestRefundParam param) throws TException {
        StoreOrder storeOrder = this.storeOrderService.getStoreOrderById(param.getMerchantId(), param.getStoreId(), param.getOrderId());
        this.storeOrderRefundService.makeRefunding(param.getMerchantId(), param.getStoreId(), param.getRefundRecordId());
        this.refundFacade.requestRefund(storeOrder.getPayOrderId(), param.getRefundRecordId(), param.getStoreOrderRefundVersion());

    }

    private void doProcessCallBack(I5weiRefundCallbackParam param) throws TException {
        //1.判断StoreOrderRefundRecord的退款业务是否完成,能防止业务重复请求,重复发退款通知
        int refundVersion = param.getStoreOrderRefundVersion();
        StoreOrderRefundRecord storeOrderRefundRecord = storeOrderRefundRecordDAO.getById(param.getMerchantId(), param.getStoreId(), param.getRefundRecordId(), false);
        //异常状况下,5wei如果没有创建退款记录,会在storeOrderRefundService.confirmRefundSuccess()方法中重新创建
        //这种情况发生过一次:在重新发送退款消息修补数据的出现。
        if (storeOrderRefundRecord != null && storeOrderRefundRecord.isBusinessFinish()) {
            return;
        }

        RefundRecordDBDTO refundRecordV2 = this.refundFacade.getRefundRecordV2(param.getPayOrderId(), param.getRefundRecordId());
        RefundCallbackResult result = this.storeOrderRefundService.processRefundCallback(param, refundRecordV2.getSuccessRefundDetails());
        if (!result.isSuccess()) {
            return;
        }
        //小票划菜，有退菜，加工档口和传菜间需打印退菜通知单
        if (param.isDefineRefund()) {
            this.storeOrderHelper.afterRefundSuccess4DefineRefund(result, refundVersion);
        } else {
            this.storeOrderHelper.afterRefundSuccess4PreOrderOrAllRefund(result, refundVersion);
        }

        //2.设置StoreOrderRefundRecord的退款业务标识为true
        if (storeOrderRefundRecord == null) {
            //重新获取一次就能得到
            storeOrderRefundRecord = storeOrderRefundRecordDAO.getById(param.getMerchantId(), param.getStoreId(), param.getRefundRecordId(), false);
        }
        storeOrderRefundRecord.makeBusinessFinish();
    }
}
