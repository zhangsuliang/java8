package com.huofu.module.i5wei.cashierchannel.entity;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.dbrouter.StoreChargeItemPromotionDbRouter;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.thrift.serialize.ThriftField;

import java.util.List;

/**
 * 收银台通道
 */
@Table(name = "tb_store_cashier_channel")
public class StoreCashierChannel extends BaseEntity {

    /**
     * 活动ID
     */
    @ThriftField(1)
    @Id
    @Column("channel_id")
    private long channelId;

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
     * 收银台通道名称
     */
    @ThriftField(4)
    @Column("name")
    private String name;

    /**
     * 收银台ID
     */
    @ThriftField(5)
    @Column("cashier_id")
    private long cashierId;

    /**
     * 扫码台ID
     */
    @ThriftField(6)
    @Column("scan_peripheral_id")
    private long scanPeripheralId;

    /**
     * 创建时间
     */
    @ThriftField(7)
    @Column("create_time")
    private long createTime;

    /**
     * 更新时间
     */
    @ThriftField(8)
    @Column("update_time")
    private long updateTime;

    /**
     * 收银台通道的收费项目白名单
     */
    private List<StoreCashierChannelChargeItem> chargeItems;

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCashierId() {
        return cashierId;
    }

    public void setCashierId(long cashierId) {
        this.cashierId = cashierId;
    }

    public long getScanPeripheralId() {
        return scanPeripheralId;
    }

    public void setScanPeripheralId(long scanPeripheralId) {
        this.scanPeripheralId = scanPeripheralId;
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

    public List<StoreCashierChannelChargeItem> getChargeItems() {
        return chargeItems;
    }

    public void setChargeItems(List<StoreCashierChannelChargeItem> chargeItems) {
        this.chargeItems = chargeItems;
    }

    public static List<Long> getIdList(List<StoreCashierChannel> storeCashierChannels) {
        List<Long> idList = Lists.newArrayList();
        for (StoreCashierChannel storeCashierChannel : storeCashierChannels) {
            idList.add(storeCashierChannel.getChannelId());
        }
        return idList;
    }
}
