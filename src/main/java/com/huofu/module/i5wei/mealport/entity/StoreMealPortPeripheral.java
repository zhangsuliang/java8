package com.huofu.module.i5wei.mealport.entity;

import com.huofu.module.i5wei.mealport.dbrouter.StoreMealPortPeripheralDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

/**
 * 出餐口与外接设备的设置
 * Created by akwei on 1/4/17.
 */
@Table(name = "tb_store_meal_port_peripheral", dalParser = StoreMealPortPeripheralDbRouter.class)
public class StoreMealPortPeripheral extends BaseEntity {

    @Id
    @Column("port_id")
    private long portId;

    @Id(1)
    @Column("peripheral_id")
    private long peripheralId;

    @Column("merchant_id")
    private int merchantId;

    @Column("store_id")
    private long storeId;

    @Column("create_time")
    private long createTime;

    public long getPortId() {
        return portId;
    }

    public void setPortId(long portId) {
        this.portId = portId;
    }

    public long getPeripheralId() {
        return peripheralId;
    }

    public void setPeripheralId(long peripheralId) {
        this.peripheralId = peripheralId;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
