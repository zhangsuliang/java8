package com.huofu.module.i5wei.table.facade;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderCreditStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderRefundStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.table.ClearTableRecordParam;
import huofucore.facade.i5wei.table.CreateTableRecordWithOrderIdsParam;
import huofucore.facade.i5wei.table.CreateTableRecordWithTakeCodesParam;
import huofucore.facade.i5wei.table.MergeTableRecordParam;
import huofucore.facade.i5wei.table.RefundTableItemParam;
import huofucore.facade.i5wei.table.StoreTableErrorCodeEnum;
import huofucore.facade.i5wei.table.TableRecordStatusEnum;
import huofucore.facade.i5wei.table.TransferTableRecordParam;

import org.springframework.stereotype.Component;

import com.amazonaws.util.StringUtils;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

/**
 * 桌台后付费参数校验类
 * @author licheng7
 * 2016年5月23日 上午10:08:48
 */
@Component
public class StoreTableRecordFacadeValidate {

	/**
	 * 开台、拼台参数校验
	 * @param createTableRecordParam
	 * @throws T5weiException
	 */
	public void checkCreateTableRecordParam (
			CreateTableRecordWithTakeCodesParam createTableRecordWithTakeCodesParam) throws T5weiException {
		if (createTableRecordWithTakeCodesParam.getTableId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableId["+createTableRecordWithTakeCodesParam.getTableId()+"] invalid");
		}
		if (createTableRecordWithTakeCodesParam.getCustomerTraffic() < 0 || createTableRecordWithTakeCodesParam.getCustomerTraffic() > 999) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "customerTraffic["+createTableRecordWithTakeCodesParam.getCustomerTraffic()+"] invalid");
		}
		if (createTableRecordWithTakeCodesParam.getMerchantId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+createTableRecordWithTakeCodesParam.getMerchantId()+"] invalid");
		}
		if (createTableRecordWithTakeCodesParam.getStoreId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+createTableRecordWithTakeCodesParam.getStoreId()+"] invalid");
		}
		if (createTableRecordWithTakeCodesParam.getRepastDate() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "repastDate["+createTableRecordWithTakeCodesParam.getRepastDate()+"] invalid");
		}
		if (createTableRecordWithTakeCodesParam.getTimeBucketId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "timeBucketId["+createTableRecordWithTakeCodesParam.getTimeBucketId()+"] invalid");
		}
	}
	
	/**
	 * 开台、拼台参数校验
	 * @param createTableRecordParam
	 * @throws T5weiException
	 */
	public void checkCreateTableRecordParam (
			CreateTableRecordWithOrderIdsParam createTableRecordWithOrderIdsParam) throws T5weiException {
		if (createTableRecordWithOrderIdsParam.getTableId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableId["+createTableRecordWithOrderIdsParam.getTableId()+"] invalid");
		}
		if (createTableRecordWithOrderIdsParam.getCustomerTraffic() < 0 || createTableRecordWithOrderIdsParam.getCustomerTraffic() > 999) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "customerTraffic["+createTableRecordWithOrderIdsParam.getCustomerTraffic()+"] invalid");
		}
		if (createTableRecordWithOrderIdsParam.getMerchantId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+createTableRecordWithOrderIdsParam.getMerchantId()+"] invalid");
		}
		if (createTableRecordWithOrderIdsParam.getStoreId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+createTableRecordWithOrderIdsParam.getStoreId()+"] invalid");
		}
		if (createTableRecordWithOrderIdsParam.getRepastDate() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "repastDate["+createTableRecordWithOrderIdsParam.getRepastDate()+"] invalid");
		}
		if (createTableRecordWithOrderIdsParam.getTimeBucketId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "timeBucketId["+createTableRecordWithOrderIdsParam.getTimeBucketId()+"] invalid");
		}
	}
	
	/**
	 * 清台参数校验
	 * @param createTableRecordParam
	 * @throws T5weiException
	 */
	public void checkClearTableRecordParam (
			ClearTableRecordParam clearTableRecordParam) throws T5weiException {
		if (clearTableRecordParam.getMerchantId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+clearTableRecordParam.getMerchantId()+"] invalid");
		}
		if (clearTableRecordParam.getStoreId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+clearTableRecordParam.getStoreId()+"] invalid");
		}
		if (clearTableRecordParam.getStaffId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "staffId["+clearTableRecordParam.getStaffId()+"] invalid");
		}
		if (clearTableRecordParam.getTableRecordId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+clearTableRecordParam.getTableRecordId()+"] invalid");
		}
	}
	
	/**
	 * 合台参数校验
	 * @param mergeTableRecordParam
	 * @throws T5weiException 
	 */
	public void checkMergeTableRecordParam (MergeTableRecordParam mergeTableRecordParam) throws T5weiException {
		if (mergeTableRecordParam.getMerchantId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+mergeTableRecordParam.getMerchantId()+"] invalid");
		}
		if (mergeTableRecordParam.getStoreId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+mergeTableRecordParam.getStoreId()+"] invalid");
		}
		if (mergeTableRecordParam.getOriginalTableRecordId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "originalTableRecordId["+mergeTableRecordParam.getOriginalTableRecordId()+"] invalid");
		}
		if (mergeTableRecordParam.getTargetTableRecordId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "targetTableRecordId["+mergeTableRecordParam.getTargetTableRecordId()+"] invalid");
		}
		if (mergeTableRecordParam.getStaffId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "staffId["+mergeTableRecordParam.getStaffId()+"] invalid");
		}
	}
	
	/**
	 * 转台参数校验
	 * @param transferTableRecordParam
	 * @throws T5weiException
	 */
	public void checkTransferTableRecordParam (TransferTableRecordParam transferTableRecordParam) throws T5weiException {
		if (transferTableRecordParam.getMerchantId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+transferTableRecordParam.getMerchantId()+"] invalid");
		}
		if (transferTableRecordParam.getStoreId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+transferTableRecordParam.getStoreId()+"] invalid");
		}
		if (transferTableRecordParam.getOriginalTableRecordId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "originalTableRecordId["+transferTableRecordParam.getOriginalTableRecordId()+"] invalid");
		}
		if (transferTableRecordParam.getTargetTableId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "targetTableId["+transferTableRecordParam.getTargetTableId()+"] invalid");
		}
		if (transferTableRecordParam.getStaffId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "staffId["+transferTableRecordParam.getStaffId()+"] invalid");
		}
	}
	
	/**
	 * 校验退菜参数
	 * @param refundOrderItemParam
	 * @throws T5weiException
	 */
	public void checkRefundOrderItemParam (RefundTableItemParam refundTableItemParam) throws T5weiException {
		if (refundTableItemParam.getMerchantId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "merchantId["+refundTableItemParam.getMerchantId()+"] invalid");
		}
		if (refundTableItemParam.getStoreId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "storeId["+refundTableItemParam.getStoreId()+"] invalid");
		}
		if (refundTableItemParam.getTableRecordId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+refundTableItemParam.getTableRecordId()+"] invalid");
		}
		if (refundTableItemParam.getStaffId() <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "staffId["+refundTableItemParam.getStaffId()+"] invalid");
		}
		if (refundTableItemParam.getRefundOrderItems() == null || refundTableItemParam.getRefundOrderItems().isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "refundOrderItems invalid");
		}
	}
	
	/**
	 * 校验订单取餐方式（绑定桌台的订单不允许是外送和快取模式）
	 * @param storeOrder
	 * @throws T5weiException 
	 */
	public boolean checkSubOrderTakeMode (StoreOrder storeOrder) throws T5weiException {
		int takeMode = storeOrder.getTakeMode();
		if (takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue() || takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue() || takeMode == StoreOrderTakeModeEnum.UNKNOWN.getValue()) {
			throw new T5weiException(StoreTableErrorCodeEnum.TAKE_MODE_CAN_NOT_BE_SENDOUT_OR_QUICKTAKE.getValue(), "storeOrder["+storeOrder.getOrderId()+"] takeMode can not be SEND_OUT、 QUICK_TAKE or UNKNOWN");
		}
		return true;
	}
	
	/**
	 * 已经关联过桌台记录的订单不允许关联其他桌台记录
	 * @param storeOrder
	 * @throws T5weiException 
	 */
	public boolean checkSubOrderTableRecordId (StoreOrder storeOrder) throws T5weiException {
		long tableRecordId = storeOrder.getTableRecordId();
		if (tableRecordId != 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.REPEAT_BIND_TABLE_RECORD.getValue(), "storeOrder["+storeOrder.getOrderId()+"], tableRecordId["+storeOrder.getTableRecordId()+"] can not createTableRecord");
		}
		return true;
	}
	
	/**
	 * 订单状态校验-桌台记录在结账前，子订单不允许赊账和退款
	 * @param storeTableRecord
	 * @param storeOrder
	 * @throws T5weiException
	 */
	public void checkSubOrderStatus (StoreTableRecord storeTableRecord, StoreOrder storeOrder) throws T5weiException {
		// 忽略已结账和已清台的桌台记录
		int tableRecordStatus = storeTableRecord.getTableRecordStatus();
		if (tableRecordStatus == TableRecordStatusEnum.SETTLEMENT.getValue() || tableRecordStatus == TableRecordStatusEnum.CLEAR_TABLE.getValue()) {
			return;
		}
		// 获取订单退款状态：1＝未退款，2＝用户全额退款：用户在支付完成之后，尚未发生实质交易时，发生的退款记录；3=商户全额退款：可能已发生交易，但由于异常情况需要做退款
		int orderRefundStatus = storeOrder.getRefundStatus(); 
		// 订单赊账状态
		int orderCreditStatus = storeOrder.getCreditStatus();
		// 订单状态校验
		if (orderRefundStatus != StoreOrderRefundStatusEnum.NOT.getValue()) { 
			// 订单有退款时，不允许绑定桌台记录
			throw new T5weiException(StoreTableErrorCodeEnum.SUB_ORDER_CAN_NOT_REFUND.getValue(), "refund order["+storeOrder.getOrderId()+"] can not bind tableRecord["+storeTableRecord.getTableRecordId()+"]");
		}
		if (orderCreditStatus != StoreOrderCreditStatusEnum.NO_CREDIT.getValue()) {
			// 订单为赊账也不允许绑定桌台记录
			throw new T5weiException(StoreTableErrorCodeEnum.SUB_ORDER_CAN_NOT_CREDIT.getValue(), "credit order["+storeOrder.getOrderId()+"] can not bind tableRecord["+storeTableRecord.getTableRecordId()+"]");
		}
	}
	
	// 校验桌台记录主订单id和主订单orderId是否一致
	public void checkMasterOrderId (StoreTableRecord storeTableRecord, StoreOrder masterStoreOrder) throws T5weiException {
		if (!StringUtils.isNullOrEmpty(storeTableRecord.getOrderId()) && !storeTableRecord.getOrderId().equals(masterStoreOrder.getOrderId())) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord["+storeTableRecord.getTableRecordId()+"]’s orderId["+storeTableRecord.getOrderId()+"], masterOrderId["+masterStoreOrder.getOrderId()+"] different");
		}
	}
}
