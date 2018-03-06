package com.huofu.module.i5wei.table.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

import com.huofu.module.i5wei.table.dbrouter.TableRecordBatchRefundRecordDbRouter;

/**
 * Auto created by i5weitools
 * 
 */
@Table(name = "tb_table_record_batch_refund_record", dalParser = TableRecordBatchRefundRecordDbRouter.class)
public class TableRecordBatchRefundRecord extends BaseEntity {

    /**
     * 批量退款批次号
     */
	@Id
    @Column("batch_refund_id")
    private long batchRefundId;

    /**
     * 商编
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺id
     */
    @Column("store_id")
    private long storeId;

    /**
     * 桌台记录id
     */
    @Column("table_record_id")
    private long tableRecordId;

    /**
     * 退款状态
     */
    @Column("status")
    private int status;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;
    
    /**
     * 退款记录id集合
     */
    @Column("refund_record_id")
    private String refundRecordIds;

    /**
     * 退款员工id
     */
    @Column("staff_id")
    private long staffId;
    
    /**
     * 错误码
     */
    @Column("error_code")
    private int errorCode;
    
    /**
     * 异常信息
     */
    @Column("error_msg")
    private String errorMsg;
    
    /**
     * 是否为桌台结账退款：1结账退款；0订单管理退款
     */
    @Column("type")
    private int type;
    
    /**
     * 终端类型
     */
    @Column("client_type")
    private int clientType;
    
    /**
     * 本次退款总金额,包含的是自定义券的面额
     */
    @Column("refund_amount")
    private long refundAmount;

	/**
	 * 本次实际退款总金额,包含的是自定义券的实付金额
	 */
	@Column("actual_refund_amount")
	private long actualRefundAmount;

	public long getBatchRefundId() {
		return batchRefundId;
	}

	public void setBatchRefundId(long batchRefundId) {
		this.batchRefundId = batchRefundId;
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

	public long getTableRecordId() {
		return tableRecordId;
	}

	public void setTableRecordId(long tableRecordId) {
		this.tableRecordId = tableRecordId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public String getRefundRecordIds() {
		return refundRecordIds;
	}

	public void setRefundRecordIds(String refundRecordIds) {
		this.refundRecordIds = refundRecordIds;
	}

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
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

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public long getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(long refundAmount) {
		this.refundAmount = refundAmount;
	}

	public long getActualRefundAmount() {
		return actualRefundAmount;
	}

	public void setActualRefundAmount(long actualRefundAmount) {
		this.actualRefundAmount = actualRefundAmount;
	}
}