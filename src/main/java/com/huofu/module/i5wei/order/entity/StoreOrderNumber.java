package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.order.dbrouter.StoreOrderNumberDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * 店铺每日自增序列号
 */
@Table(name = "tb_store_order_number", dalParser = StoreOrderNumberDbRouter.class)
public class StoreOrderNumber extends AbsEntity {

    /**
     * 自增ID
     */
    @Id
    @Column("tid")
    private long tid;

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
     * 就餐日期，即菜单所在日期
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 自增序列号，每日从1开始计数
     */
    @Column("take_serial_number")
    private int takeSerialNumber;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
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

    public long getRepastDate() {
        return repastDate;
    }

    public void setRepastDate(long repastDate) {
        this.repastDate = repastDate;
    }

    public int getTakeSerialNumber() {
        return takeSerialNumber;
    }

    public void setTakeSerialNumber(int takeSerialNumber) {
        this.takeSerialNumber = takeSerialNumber;
    }

}