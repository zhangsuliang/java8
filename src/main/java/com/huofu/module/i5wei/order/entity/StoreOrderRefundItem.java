package com.huofu.module.i5wei.order.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.MoneyUtil;

import com.huofu.module.i5wei.order.dbrouter.StoreOrderRefundItemDbRouter;

/**
 * 订单退菜详情
 * @author licheng7
 * 2016年4月27日 上午9:32:00
 */
@Table(name = "tb_store_order_refund_item", dalParser = StoreOrderRefundItemDbRouter.class)
public class StoreOrderRefundItem extends BaseEntity{

    /**
     * 订单退菜项目记录ID（自增ID）
     */
	@Id
    @Column("order_refund_item_id")
    private long orderRefundItemId;

    /**
     * 商户ID
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺ID
     */
    @Column("store_id")
    private long storeId;

    /**
     * 员工id
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 订单ID
     */
    @Column("order_id")
    private String orderId;

    /**
     * 就餐日期
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 营业时段
     */
    @Column("time_bucket_id")
    private long timeBucketId;

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
     * 单价
     */
    @Column("price")
    private long price;

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
     * 是否打包：0=不打包，1=打包
     */
    @Column("packed")
    private boolean packed;

    /**
     * 退菜时是否恢复库存：0=不恢复，1=恢复
     */
    @Column("restore_inventory")
    private boolean restoreInventory;

    /**
     * 退款金额
     */
    @Column("refund_price")
    private long refundPrice;

    /**
     * 退菜原因
     */
    @Column("refund_reason")
    private String refundReason;

    /**
     * 开台记录变更ID
     */
    @Column("table_record_refund_id")
    private long tableRecordRefundId;

    /**
     * 开台记录ID
     */
    @Column("table_record_id")
    private long tableRecordId;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;
    
    /**
     * 退款记录id
     */
    @Column("refund_record_id")
    private long refundRecordId;

	/**
	 * 传菜口ID
	 */
	private long sendPortId;
    
    private long originalPrice;

	public long getSendPortId() {
		return sendPortId;
	}

	public void setSendPortId(long sendPortId) {
		this.sendPortId = sendPortId;
	}

	public long getOrderRefundItemId() {
		return orderRefundItemId;
	}

	public void setOrderRefundItemId(long orderRefundItemId) {
		this.orderRefundItemId = orderRefundItemId;
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

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public long getRepastDate() {
		return repastDate;
	}

	public void setRepastDate(long repastDate) {
		this.repastDate = repastDate;
	}

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
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

	public long getPrice() {
		return price;
	}

	public void setPrice(long price) {
		this.price = price;
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

	public boolean isPacked() {
		return packed;
	}
	
	public void setPacked(boolean packed) {
		this.packed = packed;
	}

	public boolean isRestoreInventory() {
		return restoreInventory;
	}

	public void setRestoreInventory(boolean restoreInventory) {
		this.restoreInventory = restoreInventory;
	}

	public long getRefundPrice() {
		return refundPrice;
	}

	public void setRefundPrice(long refundPrice) {
		this.refundPrice = refundPrice;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public long getTableRecordRefundId() {
		return tableRecordRefundId;
	}

	public void setTableRecordRefundId(long tableRecordRefundId) {
		this.tableRecordRefundId = tableRecordRefundId;
	}

	public long getTableRecordId() {
		return tableRecordId;
	}

	public void setTableRecordId(long tableRecordId) {
		this.tableRecordId = tableRecordId;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	
	public long getRefundRecordId() {
		return refundRecordId;
	}

	public void setRefundRecordId(long refundRecordId) {
		this.refundRecordId = refundRecordId;
	}
	
	public long getOriginalPrice(long packagePrice) {
		this.originalPrice = MoneyUtil.mul(this.price, this.amount);
		if (this.packed) {
			this.originalPrice = this.originalPrice + MoneyUtil.mul(packagePrice, amount);
		}
		return this.originalPrice;
	}
	
	public long getOriginalPrice() {
		return this.originalPrice;
	}

}