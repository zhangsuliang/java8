package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.order.dbrouter.StoreOrderOptlogDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

@Table(name = "tb_store_order_optlog", dalParser = StoreOrderOptlogDbRouter.class)
public class StoreOrderOptlog extends AbsEntity {

    /**
     * 主键，自增ID，没有业务意义
     */
    @Id
    @Column("tid")
    private long tid;

    /**
     * 订单ID
     */
    @Column("order_id")
    private String orderId;

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
     * 员工ID
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 用户ID
     */
    @Column("user_id")
    private long userId;

    /**
     * 客户端类型
     */
    @Column("client_type")
    private int clientType;

    /**
     * 操作类型
     */
    @Column("opt_type")
    private int optType;

    /**
     * 备注
     */
    @Column("remark")
    private String remark;

    /**
     * 新增时间
     */
    @Column("create_time")
    private long createTime;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public int getOptType() {
        return optType;
    }

    public void setOptType(int optType) {
        this.optType = optType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

}
