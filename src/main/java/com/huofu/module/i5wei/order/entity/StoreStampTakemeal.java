package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.order.dbrouter.StoreStampTakemealDBRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Created by chengq on 16/4/8.
 * <p>
 * 店铺需要自动打印取餐单信息
 */
@Table(name = "tb_store_stamp_takemeal", dalParser = StoreStampTakemealDBRouter.class)
public class StoreStampTakemeal extends AbsEntity {

    /**
     * 需要打印取餐单Id
     */
    @Id
    @Column("store_stamp_takemeal_id")
    private long storeStampTakemealId;

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
     * 状态 {@link huofucore.facade.i5wei.order.StatusEnum
     */
    @Column("status")
    private int status;

    /**
     * 营业时间段
     */
    @Column("time_bucket_id")
    private long timeBuchetId;

    /**
     * 就餐时间
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 最后修改时间
     */
    @Column("update_time")
    private long updateTime;

    public long getStoreStampTakemealId() {
        return storeStampTakemealId;
    }

    public void setStoreStampTakemealId(long storeStampTakemealId) {
        this.storeStampTakemealId = storeStampTakemealId;
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

    public long getTimeBuchetId() {
        return timeBuchetId;
    }

    public void setTimeBuchetId(long timeBuchetId) {
        this.timeBuchetId = timeBuchetId;
    }

    public long getRepastDate() {
        return repastDate;
    }

    public void setRepastDate(long repastDate) {
        this.repastDate = repastDate;
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

    @Override
    public String toString() {
        return "StoreStampTakemeal{" +
                "storeStampTakemealId=" + storeStampTakemealId +
                ", merchantId=" + merchantId +
                ", storeId=" + storeId +
                ", orderId='" + orderId + '\'' +
                ", status=" + status +
                ", timeBuchetid=" + timeBuchetId +
                ", repast_date=" + repastDate +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
