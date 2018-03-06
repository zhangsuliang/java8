package com.huofu.module.i5wei.table.entity;

import com.huofu.module.i5wei.table.dbrouter.StoreTableRecordRefundDbRouter;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

/**
 * Auto created by i5weitools
 * 
 */
@Table(name = "tb_store_table_record_refund", dalParser = StoreTableRecordRefundDbRouter.class)
public class StoreTableRecordRefund extends BaseEntity{

	/**
     * 桌台退菜记录id
     */
	@Id
    @Column("table_record_refund_id")
    private long tableRecordRefundId;

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
     * 员工id
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 终端类型
     */
    @Column("client_type")
    private int clientType;

    /**
     * 退菜金额
     */
    @Column("refund_price")
    private long refundPrice;

    /**
     * 收费项目ID
     */
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 收费项目名称
     */
    @Column("charge_item_name")
    private String chargeItemName;

    /**
     * 是否打包：0=不打包，1=打包
     */
    @Column("packed")
    private boolean packed;

    /**
     * 退菜数量
     */
    @Column("amount")
    private double amount;

    /**
     * 规格：单位（份、碗、个。。。）
     */
    @Column("unit")
    private String unit;

    /**
     * 退菜原因
     */
    @Column("refund_reason")
    private String refundReason;

    /**
     * 退菜时是否恢复库存：0=不恢复，1=恢复
     */
    @Column("restore_inventory")
    private boolean restoreInventory;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

	public long getTableRecordRefundId() {
		return tableRecordRefundId;
	}

	public void setTableRecordRefundId(long tableRecordRefundId) {
		this.tableRecordRefundId = tableRecordRefundId;
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

	public long getRefundPrice() {
		return refundPrice;
	}

	public void setRefundPrice(long refundPrice) {
		this.refundPrice = refundPrice;
	}

	public long getChargeItemId() {
		return chargeItemId;
	}

	public void setChargeItemId(long chargeItemId) {
		this.chargeItemId = chargeItemId;
	}

	public String getChargeItemName() {
		return chargeItemName;
	}

	public void setChargeItemName(String chargeItemName) {
		this.chargeItemName = chargeItemName;
	}

	public boolean isPacked() {
		return packed;
	}

	public void setPacked(boolean packed) {
		this.packed = packed;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public boolean isRestoreInventory() {
		return restoreInventory;
	}

	public void setRestoreInventory(boolean restoreInventory) {
		this.restoreInventory = restoreInventory;
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
    
}