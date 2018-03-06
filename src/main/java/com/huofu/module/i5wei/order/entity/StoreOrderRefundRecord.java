package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.order.dbrouter.StoreOrderRefundRecordDbRouter;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.order.I5weiRefundCallbackParam;
import huofucore.facade.pay.payment.RefundRecordStatusEnum;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.DataUtil;

/**
 * 退款记录
 */
@Table(name = "tb_store_order_refund_record", dalParser = StoreOrderRefundRecordDbRouter.class)
public class StoreOrderRefundRecord extends BaseEntity {

    /**
     * 退款记录id
     */
    @Id
    @Column("refund_record_id")
    private long refundRecordId;

    /**
     * 商户id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺id
     */
    @Column("store_id")
    private long storeId;

    /**
     * 订单id
     */
    @Column("order_id")
    private String orderId;

    /**
     * 退款状态 {@link RefundRecordStatusEnum}
     */
    @Column("status")
    private int status;

    /**
     * 退款员工id
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 退款终端
     */
    @Column("client_type")
    private int clientType;

    /**
     * 错误码
     */
    @Column("error_code")
    private int errorCode;

    /**
     * 错误信息
     */
    @Column("error_msg")
    private String errorMsg;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 结束时间
     */
    @Column("finish_time")
    private long finishTime;
    
    /**
	 * 本次退款金额
	 */
    @Column("refund_amount")
	private long refundAmount;
    
    /**
     * 退款原因
     */
    @Column("refund_reason")
    private String refundReason;
    
    /**
     * 退菜信息
     */
    @Column("refund_item_messages")
    private String refundItemMessages;

    @Column("business_finish")
    private boolean businessFinish;

    public long getRefundRecordId() {
		return refundRecordId;
	}

	public void setRefundRecordId(long refundRecordId) {
		this.refundRecordId = refundRecordId;
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public long getStoreId() {
		return storeId;
	}

	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public long getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(long refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public String getRefundItemMessages() {
		return refundItemMessages;
	}

	public void setRefundItemMessages(String refundItemMessages) {
		this.refundItemMessages = refundItemMessages;
	}

    public boolean isBusinessFinish() {
        return businessFinish;
    }

    public void setBusinessFinish(boolean businessFinish) {
        this.businessFinish = businessFinish;
    }

    public void makeFail(int errorCode, String errorMsg) {
        this.snapshot();
        this.status = RefundRecordStatusEnum.FAIL.getValue();
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.update();
    }

    public void makeSuccess(long refundAmount) {
        this.snapshot();
        this.status = RefundRecordStatusEnum.SUCCESS.getValue();
        this.refundAmount = refundAmount;
        if (this.finishTime == 0) {
            this.finishTime = System.currentTimeMillis();
        }
        this.update();
    }

    public void makeRefunding() {
        this.snapshot();
        this.status = RefundRecordStatusEnum.ING.getValue();
        this.update();
    }

    public void initCreate(I5weiRefundCallbackParam param, long refundAmount){
    	this.merchantId = param.getMerchantId();
    	this.storeId = param.getStoreId();
    	this.staffId = 0;
    	this.orderId = param.getOrderId();
    	this.clientType = ClientTypeEnum.CASHIER.getValue();
    	this.refundRecordId = param.getRefundRecordId();
    	this.status= param.getRefundRecordStatus();
    	this.refundAmount = refundAmount;
    	this.finishTime = System.currentTimeMillis();
    	this.createTime = System.currentTimeMillis();
    	this.create();
    }
    
    public boolean isSuccess() {
        if (this.status == RefundRecordStatusEnum.SUCCESS.getValue()) {
            return true;
        }
        return false;
    }

    public boolean isFail() {
        if (this.status == RefundRecordStatusEnum.FAIL.getValue()) {
            return true;
        }
        return false;
    }

    public void makeBusinessFinish(){
        this.snapshot();
        this.businessFinish = true;
        this.update();
    }

    @Override
    public void create() {
        if (DataUtil.isNotEmpty(this.errorMsg) && errorMsg.length() > 100) {
            this.errorMsg = errorMsg.substring(0, 100);
        }
        super.create();
    }

    @Override
    public void update() {
        if (DataUtil.isNotEmpty(this.errorMsg) && errorMsg.length() > 100) {
            this.errorMsg = errorMsg.substring(0, 100);
        }
        super.update();
    }
}
