package com.huofu.module.i5wei.order.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.order.dbrouter.StoreOrderSwitchDbRouter;

/**
 * Auto created by i5weitools
 * 店铺餐饮交易订单变更表
 */
@Table(name = "tb_store_order_switch",dalParser = StoreOrderSwitchDbRouter.class)
public class StoreOrderSwitch extends AbsEntity{

    /**
     * 交易订单变更 ID（D+order_id）
     */
	@Id
    @Column("order_switch_id")
    private String orderSwitchId;

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
     * 交易订单ID
     */
    @Column("order_id")
    private String orderId;

    /**
     * 变更方式：0=未知、1=堂食变打包、2=打包变堂食
     */
    @Column("switch_type")
    private int switchType;

    /**
     * 变更之前的价格
     */
    @Column("pre_price")
    private long prePrice;

    /**
     * 变更差价，支持负数
     */
    @Column("diff_price")
    private long diffPrice;

    /**
     * 处理方式：0=未知、1=退款、2=二次支付
     */
    @Column("process_type")
    private int processType;

    /**
     * 处理状态：0=未处理、1=成功、2=失败
     */
    @Column("process_status")
    private int processStatus;

    /**
     * 支付状态，与tb_store_order的pay_status共用枚举值
     */
    @Column("pay_status")
    private int payStatus;

    /**
     * 退款状态，与tb_store_order的refund_status共用枚举值
     */
    @Column("refund_status")
    private int refundStatus;

    /**
     * 支付订单ID
     */
    @Column("pay_order_id")
    private String payOrderId;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;
    
	public static String getOrderSwitchId(String orderId) {
		return "D" + orderId;
	}

	public String getOrderSwitchId() {
		return orderSwitchId;
	}

	public void setOrderSwitchId(String orderSwitchId) {
		this.orderSwitchId = orderSwitchId;
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

	public int getSwitchType() {
		return switchType;
	}

	public void setSwitchType(int switchType) {
		this.switchType = switchType;
	}

	public long getPrePrice() {
		return prePrice;
	}

	public void setPrePrice(long prePrice) {
		this.prePrice = prePrice;
	}

	public long getDiffPrice() {
		return diffPrice;
	}

	public void setDiffPrice(long diffPrice) {
		this.diffPrice = diffPrice;
	}

	public int getProcessType() {
		return processType;
	}

	public void setProcessType(int processType) {
		this.processType = processType;
	}

	public int getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(int processStatus) {
		this.processStatus = processStatus;
	}

	public int getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(int payStatus) {
		this.payStatus = payStatus;
	}

	public int getRefundStatus() {
		return refundStatus;
	}

	public void setRefundStatus(int refundStatus) {
		this.refundStatus = refundStatus;
	}

	public String getPayOrderId() {
		return payOrderId;
	}

	public void setPayOrderId(String payOrderId) {
		this.payOrderId = payOrderId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}