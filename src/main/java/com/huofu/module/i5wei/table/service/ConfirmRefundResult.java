package com.huofu.module.i5wei.table.service;

import java.util.List;
import java.util.Map;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.TableRecordBatchRefundRecord;
import huofucore.facade.pay.payment.RefundRecordDBDTO;

public class ConfirmRefundResult {

	private boolean refundSuccess;
	private TableRecordBatchRefundRecord tableRecordBatchRefundRecord;
	private Map<String, StoreOrder> storeOrderMap;
	private boolean isSettleRefund;
	private List<RefundRecordDBDTO> refundRecordDBDTOs;
	
	public boolean isRefundSuccess() {
		return refundSuccess;
	}
	
	public void setRefundSuccess(boolean refundSuccess) {
		this.refundSuccess = refundSuccess;
	}
	
	public TableRecordBatchRefundRecord getTableRecordBatchRefundRecord() {
		return tableRecordBatchRefundRecord;
	}
	
	public void setTableRecordBatchRefundRecord(
			TableRecordBatchRefundRecord tableRecordBatchRefundRecord) {
		this.tableRecordBatchRefundRecord = tableRecordBatchRefundRecord;
	}

	public Map<String, StoreOrder> getStoreOrderMap() {
		return storeOrderMap;
	}

	public void setStoreOrderMap(Map<String, StoreOrder> storeOrderMap) {
		this.storeOrderMap = storeOrderMap;
	}

	public boolean isSettleRefund() {
		return isSettleRefund;
	}

	public void setSettleRefund(boolean isSettleRefund) {
		this.isSettleRefund = isSettleRefund;
	}

	public List<RefundRecordDBDTO> getRefundRecordDBDTOs() {
		return refundRecordDBDTOs;
	}

	public void setRefundRecordDBDTOs(List<RefundRecordDBDTO> refundRecordDBDTOs) {
		this.refundRecordDBDTOs = refundRecordDBDTOs;
	}
}
