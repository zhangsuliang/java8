package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.order.dbrouter.StoreOrderRefundDetailDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.pay.payment.RefundDetailStatusEnum;
import huofuhelper.module.base.BaseEntity;

/**
 * 退款明细
 */
@Table(name = "tb_store_order_refund_detail", dalParser = StoreOrderRefundDetailDbRouter.class)
public class StoreOrderRefundDetail extends BaseEntity {

    /**
     * 退款明细id
     */
    @Id
    @Column("refund_detail_id")
    private long refundDetailId;

    /**
     * 退款记录id
     */
    @Column("refund_record_id")
    private long refundRecordId;

    /**
     * 退款金额
     */
    @Column("amount")
    private long amount;

    /**
     * 状态 {@link huofucore.facade.pay.payment.RefundDetailStatusEnum}
     */
    @Column("status")
    private int status;

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
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 商户通道id
     */
    @Column("merchant_channel_id")
    private int merchantChannelId;

    public int getMerchantChannelId() {
        return merchantChannelId;
    }

    public void setMerchantChannelId(int merchantChannelId) {
        this.merchantChannelId = merchantChannelId;
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

    public long getRefundDetailId() {
        return refundDetailId;
    }

    public void setRefundDetailId(long refundDetailId) {
        this.refundDetailId = refundDetailId;
    }

    public long getRefundRecordId() {
        return refundRecordId;
    }

    public void setRefundRecordId(long refundRecordId) {
        this.refundRecordId = refundRecordId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
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
}
