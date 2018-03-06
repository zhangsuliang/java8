package com.huofu.module.i5wei.order.service;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.I5weiCreateRefundRecordParam;
import huofucore.facade.i5wei.order.I5weiRefundCallbackParam;
import huofucore.facade.i5wei.order.StoreOrderRefundModeParam;
import huofucore.facade.i5wei.order.StoreOrderRefundParam;
import huofucore.facade.i5wei.order.StoreOrderRefundStatusEnum;
import huofucore.facade.pay.payment.PayOrderStatusDTO;
import huofucore.facade.pay.payment.RefundDetailDBDTO;
import huofucore.facade.pay.payment.RefundRecordDBDTO;
import huofucore.facade.pay.payment.RefundRecordStatusEnum;
import huofucore.facade.pay.payment.RefundResultDTO;
import huofuhelper.util.bean.BeanUtil;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.huofu.module.i5wei.meal.service.StoreMealService;
import com.huofu.module.i5wei.order.dao.StoreOrderRefundItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderRefundRecordDAO;
import com.huofu.module.i5wei.order.entity.RefundCallbackResult;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundDetail;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundRecord;
import com.huofu.module.i5wei.printer.I5weiOrderRefundMealPrinter;

/**
 * 退款业务操作
 * Created by akwei on 6/27/16.
 */
@Service
public class StoreOrderRefundService {

    private static Logger logger = Logger.getLogger(StoreOrderRefundService.class);

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderRefundRecordDAO storeOrderRefundRecordDAO;
    
    @Autowired
	private StoreOrderRefundItemDAO storeOrderRefundItemDAO;
    
    @Autowired
	private StoreOrderHelper storeOrderHelper;
    
    @Autowired
	private StoreMealService storeMealService;

	@Autowired
	private I5weiOrderRefundMealPrinter i5weiOrderRefundMealPrinter;

    @Transactional(rollbackFor = Exception.class, isolation = READ_COMMITTED)
    public void makeRefunding(int merchantId, long storeId, long refundRecordId) throws T5weiException {
        StoreOrderRefundRecord storeOrderRefundRecord = this.storeOrderRefundRecordDAO.loadById(merchantId, storeId, refundRecordId, true);
        storeOrderRefundRecord.makeRefunding();
    }

    @Transactional(rollbackFor = Exception.class, isolation = READ_COMMITTED)
    public StoreOrderRefundRecord createRefundRecord(I5weiCreateRefundRecordParam param, RefundRecordDBDTO refundRecordDBDTO) {
        StoreOrderRefundRecord storeOrderRefundRecord = new StoreOrderRefundRecord();
        BeanUtil.copy(param, storeOrderRefundRecord);
        BeanUtil.copy(refundRecordDBDTO, storeOrderRefundRecord);
        String refundItemMessages = storeOrderHelper.getRefundItemMessages(param.getRefundOrderItems());
		storeOrderRefundRecord.setRefundItemMessages(refundItemMessages);
        storeOrderRefundRecord.create();
        return storeOrderRefundRecord;
    }

    public StoreOrderRefundRecord loadStoreOrderRefundRecord(int merchantId, long storeId, long refundRecordId) throws T5weiException {
        return this.storeOrderRefundRecordDAO.loadById(merchantId, storeId, refundRecordId, false);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public RefundCallbackResult processRefundCallback(I5weiRefundCallbackParam param, List<RefundDetailDBDTO> refundDetailDBDTOs) throws TException {
        RefundCallbackResult result = new RefundCallbackResult();
        result.setRefundRecordStatus(param.getRefundRecordStatus());
        if (param.getRefundRecordStatus() == RefundRecordStatusEnum.FAIL.getValue()) {
        	result = this.confirmRefundFail(param);
        } else if (param.getRefundRecordStatus() == RefundRecordStatusEnum.SUCCESS.getValue()) {
        	result = this.confirmRefundSuccess(param, refundDetailDBDTOs);
        	i5weiOrderRefundMealPrinter.sendPrintMessages(result.getStoreOrderRefundRecord().getStaffId(), result.getStoreOrderRefundItems());
        }
        return result;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder refundStoreOrderWrapper(StoreOrderRefundParam storeOrderRefundParam, RefundResultDTO refundResultDTO) throws T5weiException {
        long refundRecordId = refundResultDTO.getRefundRecord().getRefundRecordId();
        StoreOrderRefundRecord storeOrderRefundRecord = new StoreOrderRefundRecord();
        BeanUtil.copy(refundResultDTO.getRefundRecord(), storeOrderRefundRecord);
        BeanUtil.copy(storeOrderRefundParam, storeOrderRefundRecord);
        storeOrderRefundRecord.setFinishTime(System.currentTimeMillis());
        storeOrderRefundRecord.create();
        this.createRefundDetails(storeOrderRefundRecord.getMerchantId(), storeOrderRefundRecord.getStoreId(), storeOrderRefundRecord.getOrderId(), refundResultDTO.getSuccessRefundDetails());
        return this.storeOrderService.refundStoreOrder(storeOrderRefundParam.getStaffId(), storeOrderRefundParam, refundRecordId);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder refundStoreOrderWrapper4Mode(StoreOrderRefundModeParam storeOrderRefundModeParam, StoreOrder storeOrder, StoreOrderRefundStatusEnum merchantRefund, RefundResultDTO refundResultDTO) throws T5weiException {
        long refundRecordId = refundResultDTO.getRefundRecord().getRefundRecordId();
        StoreOrderRefundRecord storeOrderRefundRecord = new StoreOrderRefundRecord();
        BeanUtil.copy(refundResultDTO.getRefundRecord(), storeOrderRefundRecord);
        BeanUtil.copy(storeOrderRefundModeParam, storeOrderRefundRecord);
        storeOrderRefundRecord.setFinishTime(System.currentTimeMillis());
        storeOrderRefundRecord.create();
        this.createRefundDetails(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getOrderId(), refundResultDTO.getSuccessRefundDetails());
        return this.storeOrderService.refundStoreOrder(storeOrder, storeOrderRefundModeParam.getStaffId(), merchantRefund, refundRecordId);
    }

    public long createRefundDetails(int merchantId, long storeId, String orderId, List<RefundDetailDBDTO> refundDetailDBDTOs) {
    	long refundAmount = 0;
		if (refundDetailDBDTOs == null) {
			return refundAmount;
		}
        try {
            for (RefundDetailDBDTO refundDetailDBDTO : refundDetailDBDTOs) {
                StoreOrderRefundDetail detail = new StoreOrderRefundDetail();
                BeanUtil.copy(refundDetailDBDTO, detail);
                detail.setMerchantId(merchantId);
                detail.setStoreId(storeId);
                detail.setOrderId(orderId);
                detail.create();
            }
        } catch (DuplicateKeyException e) {
        }
		for (RefundDetailDBDTO refundDetailDBDTO : refundDetailDBDTOs) {
			refundAmount = refundAmount + refundDetailDBDTO.getAmount();
		}
        return refundAmount;
    }

    public RefundCallbackResult confirmRefundSuccess(I5weiRefundCallbackParam param, List<RefundDetailDBDTO> refundDetailDBDTOs) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long refundRecordId = param.getRefundRecordId();
        StoreOrderRefundRecord storeOrderRefundRecord = this.storeOrderRefundRecordDAO.getById(merchantId, storeId, refundRecordId, true);
        // 保存退款详情
		long refundAmount = this.createRefundDetails(merchantId, storeId, param.getOrderId(), refundDetailDBDTOs);
		// 标记退款记录成功
		if (storeOrderRefundRecord == null) {
			storeOrderRefundRecord = new StoreOrderRefundRecord();
			storeOrderRefundRecord.initCreate(param, refundAmount);
		} else {
			storeOrderRefundRecord.makeSuccess(refundAmount);
		}
     	RefundCallbackResult refundCallbackResult;
     	if (param.isDefineRefund()) {
			refundCallbackResult = this._confirmRefundSuccess4DefineRefund(param, storeOrderRefundRecord);
		} else {
			refundCallbackResult = this._confirmRefundSuccess4PreOrderOrAllOrederRefund(param, storeOrderRefundRecord);
		}
		// 处理退菜信息
		List<StoreOrderRefundItem> storeOrderRefundItems = this.confirmOrderItemRefund(storeOrderRefundRecord, refundCallbackResult.getStoreOrder());
		refundCallbackResult.setStoreOrderRefundItems(storeOrderRefundItems);
		return refundCallbackResult;
    }
    
    /**
	 * 处理退菜信息（仅先付费模式）
	 * 
	 * @param storeOrderRefundRecord
	 * @param refundOrder
	 * @throws T5weiException
	 */
	private List<StoreOrderRefundItem> confirmOrderItemRefund(StoreOrderRefundRecord storeOrderRefundRecord, StoreOrder refundOrder) throws T5weiException {
		if(refundOrder.isTableRecordOrder()){
			return new ArrayList<StoreOrderRefundItem>();
		}
		int merchantId = storeOrderRefundRecord.getMerchantId();
		long storeId = storeOrderRefundRecord.getStoreId();
		List<StoreOrderRefundItem> storeOrderRefundItems = storeOrderHelper.getStoreOrderRefundItemsByRefundRecord(storeOrderRefundRecord, refundOrder);
		if (storeOrderRefundItems.isEmpty()) {
			return storeOrderRefundItems;
		}
		// 创建退菜信息
		storeOrderRefundItemDAO.batchCreate(storeOrderRefundItems);
		// 处理退菜的统计信息
		storeOrderService.updateSubItemQuitAmount(merchantId, storeId, storeOrderRefundItems);
		// 处理后厨退菜
		storeMealService.refundStoreMeal(storeOrderRefundItems);
		// 返回
		return storeOrderRefundItems;
	}

    private RefundCallbackResult _confirmRefundSuccess4PreOrderOrAllOrederRefund(I5weiRefundCallbackParam param, StoreOrderRefundRecord storeOrderRefundRecord) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String orderId = param.getOrderId();
        int refundRecordStatus = param.getRefundRecordStatus();
        long refundRecordId = param.getRefundRecordId();
        RefundCallbackResult result = new RefundCallbackResult();
        result.setRefundRecordStatus(refundRecordStatus);
        long staffId = storeOrderRefundRecord.getStaffId();
        //更新storeOrder信息，创建storeOrder操作日志
        StoreOrderRefundParam storeOrderRefundParam = new StoreOrderRefundParam();
        storeOrderRefundParam.setStaffId(staffId);
        storeOrderRefundParam.setMerchantId(merchantId);
        storeOrderRefundParam.setStoreId(storeId);
        storeOrderRefundParam.setOrderId(orderId);
        storeOrderRefundParam.setClientType(storeOrderRefundRecord.getClientType());
        StoreOrder refundOrder = storeOrderService.refundStoreOrder(staffId, storeOrderRefundParam, refundRecordId);
        result.setStoreOrder(refundOrder);
        result.setStoreOrderRefundRecord(storeOrderRefundRecord);
        return result;
    }

    private RefundCallbackResult _confirmRefundSuccess4DefineRefund(I5weiRefundCallbackParam param, StoreOrderRefundRecord storeOrderRefundRecord) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String orderId = param.getOrderId();
        int payOrderStatus = param.getPayOrderStatus();
        int refundRecordStatus = param.getRefundRecordStatus();
        long refundRecordId = param.getRefundRecordId();
        RefundCallbackResult result = new RefundCallbackResult();
        result.setRefundRecordStatus(refundRecordStatus);
        //默认部分退款
        StoreOrderRefundStatusEnum merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_PART;
        //如果PayOrderDTO是全额退款，修改为商户全额退款
        if (payOrderStatus == PayOrderStatusDTO.FULL_REFUND.getValue()) {
            merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_ALL;
        }
        StoreOrder storeOrder = this.storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        StoreOrder refundOrder = storeOrderService.refundStoreOrder(storeOrder, storeOrderRefundRecord.getStaffId(), merchantRefund, refundRecordId);
        result.setStoreOrderRefundRecord(storeOrderRefundRecord);
        result.setStoreOrder(refundOrder);
        return result;
    }

    public RefundCallbackResult confirmRefundFail(I5weiRefundCallbackParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long refundRecordId = param.getRefundRecordId();
        int refundRecordStatus = param.getRefundRecordStatus();
        int errorCode = param.getErrorCode();
        String errorMsg = param.getErrorMsg();
        RefundCallbackResult result = new RefundCallbackResult();
        result.setRefundRecordStatus(refundRecordStatus);
        StoreOrderRefundRecord storeOrderRefundRecord = this.storeOrderRefundRecordDAO.loadById(merchantId, storeId, refundRecordId, true);
        storeOrderRefundRecord.makeFail(errorCode, errorMsg);
        result.setStoreOrderRefundRecord(storeOrderRefundRecord);
        return result;
    }


    public void finishStoreOrderRefundRecord(int merchantId, long storeId, long refundRecordId, boolean forUpdate){
        StoreOrderRefundRecord storeOrderRefundRecord = this.storeOrderRefundRecordDAO.getById(merchantId, storeId, refundRecordId, forUpdate);
        storeOrderRefundRecord.makeBusinessFinish();
    }
}
