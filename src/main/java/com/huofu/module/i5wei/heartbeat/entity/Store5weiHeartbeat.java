package com.huofu.module.i5wei.heartbeat.entity;

import com.huofu.module.i5wei.heartbeat.dbrouter.Store5weiHeartbeatDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.DateUtil;

@Table(name = "tb_store_5wei_heartbeat", dalParser = Store5weiHeartbeatDbRouter.class)
public class Store5weiHeartbeat extends BaseEntity {

    @Id
    @Column("store_id")
    private long storeId;

    @Column("merchant_id")
    private int merchantId;

    @Column("has_idle_port")
    private boolean hasIdlePort;

    @Column("port_last_update_time")
    private long portLastUpdateTime;

    /**
     * 就餐日期
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 最后取餐流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;

    /**
     * 外送订单备餐完成通知更新时间
     */
    @Column("delivery_prepared_notify_time")
    public long deliveryPreparedNotifyTime;

    /**
     * 划菜最后更新时间
     */
    @Column("sweep_last_update_time")
    private long sweepLastUpdateTime;
    
    public long getDeliveryPreparedNotifyTime() {
        return deliveryPreparedNotifyTime;
    }

    public void setDeliveryPreparedNotifyTime(long deliveryPreparedNotifyTime) {
        this.deliveryPreparedNotifyTime = deliveryPreparedNotifyTime;
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

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public boolean isHasIdlePort() {
        return hasIdlePort;
    }

    public void setHasIdlePort(boolean hasIdlePort) {
        this.hasIdlePort = hasIdlePort;
    }

    public long getPortLastUpdateTime() {
        return portLastUpdateTime;
    }

    public void setPortLastUpdateTime(long portLastUpdateTime) {
        this.portLastUpdateTime = portLastUpdateTime;
    }

    public long getSweepLastUpdateTime() {
        return sweepLastUpdateTime;
    }

    public void setSweepLastUpdateTime(long sweepLastUpdateTime) {
        this.sweepLastUpdateTime = sweepLastUpdateTime;
    }

    /**
     * 获得当日最后的取餐流水号
     *
     * @return 如果不是当日, 返回0
     */
    public int getTodaySerialNumber() {
        long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (this.repastDate == today) {
            return this.takeSerialNumber;
        }
        return 0;
    }

    public static Store5weiHeartbeat createDefault(int merchantId, long storeId) {
        Store5weiHeartbeat store5weiHeartbeat = new Store5weiHeartbeat();
        store5weiHeartbeat.setStoreId(storeId);
        store5weiHeartbeat.setMerchantId(merchantId);
        store5weiHeartbeat.setHasIdlePort(false);
        store5weiHeartbeat.setPortLastUpdateTime(0);
        store5weiHeartbeat.setSweepLastUpdateTime(0);
        return store5weiHeartbeat;
    }

    @Override
    public String toString() {
        return "Store5weiHeartbeat{" +
                "storeId=" + storeId +
                ", merchantId=" + merchantId +
                ", hasIdlePort=" + hasIdlePort +
                ", portLastUpdateTime=" + portLastUpdateTime +
                ", repastDate=" + repastDate +
                ", takeSerialNumber=" + takeSerialNumber +
                ", deliveryPreparedNotifyTime=" + deliveryPreparedNotifyTime +
                '}';
    }
}
