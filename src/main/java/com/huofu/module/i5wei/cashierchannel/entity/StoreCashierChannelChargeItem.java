package com.huofu.module.i5wei.cashierchannel.entity;

import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.thrift.serialize.ThriftField;

/**
 * 收银台通道
 */
@Table(name = "tb_store_cashier_channel_charge_item")
public class StoreCashierChannelChargeItem extends BaseEntity {

    /**
     * 收银台通道收费项目ID
     */
    @ThriftField(1)
    @Id
    @Column("tid")
    private long tid;

    /**
     * 商户ID
     */
    @ThriftField(2)
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺ID
     */
    @ThriftField(3)
    @Column("store_id")
    private long storeId;

    /**
     * 通道ID
     */
    @ThriftField(4)
    @Column("channel_id")
    private long channelId;

    /**
     * 收费项目ID
     */
    @ThriftField(5)
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 创建时间
     */
    @ThriftField(6)
    @Column("create_time")
    private long createTime;

    private StoreChargeItem storeChargeItem;

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

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(long chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public StoreChargeItem getStoreChargeItem() {
        return storeChargeItem;
    }

    public void setStoreChargeItem(StoreChargeItem storeChargeItem) {
        this.storeChargeItem = storeChargeItem;
    }
}
