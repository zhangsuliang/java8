package com.huofu.module.i5wei.order.entity;

import huofucore.facade.pay.payment.RefundRecordStatusEnum;

import java.util.List;

public class RefundCallbackResult {
	
    /**
     * 退款记录状态
     */
    private int refundRecordStatus;

    /**
     * 退款订单,退款成功有值
     */
    private StoreOrder storeOrder;

    /**
     * 退款成功有值
     */
    private StoreOrderRefundRecord storeOrderRefundRecord;
    
    /**
     * 退菜信息
     */
    private List<StoreOrderRefundItem> storeOrderRefundItems;
    
	public int getRefundRecordStatus() {
        return refundRecordStatus;
    }

    public void setRefundRecordStatus(int refundRecordStatus) {
        this.refundRecordStatus = refundRecordStatus;
    }

    public StoreOrder getStoreOrder() {
        return storeOrder;
    }

    public void setStoreOrder(StoreOrder storeOrder) {
        this.storeOrder = storeOrder;
    }

    public StoreOrderRefundRecord getStoreOrderRefundRecord() {
        return storeOrderRefundRecord;
    }

    public void setStoreOrderRefundRecord(StoreOrderRefundRecord storeOrderRefundRecord) {
        this.storeOrderRefundRecord = storeOrderRefundRecord;
    }

    public List<StoreOrderRefundItem> getStoreOrderRefundItems() {
		return storeOrderRefundItems;
	}

	public void setStoreOrderRefundItems(List<StoreOrderRefundItem> storeOrderRefundItems) {
		this.storeOrderRefundItems = storeOrderRefundItems;
	}

	public boolean isSuccess() {
        if (this.refundRecordStatus == RefundRecordStatusEnum.SUCCESS.getValue()) {
            return true;
        }
        return false;
    }
}
