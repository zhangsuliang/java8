package com.huofu.module.i5wei.setting.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 店铺桌台模式设置表
 */
@Table(name = "tb_store_table_setting",dalParser = BaseDefaultStoreDbRouter.class)
public class StoreTableSetting extends AbsEntity {

    /**
     * 店铺ID
     */
    @Id
    @Column("store_id")
    private long storeId;

    /**
     * 商户ID
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * #bool 是否开启桌台模式
     */
    @Column("enable_table_mode")
    private boolean enableTableMode;

    /**
     * #bool 是否结账后自动清台
     */
    @Column("enable_table_auto_clear")
    private boolean enableTableAutoClear;

    /**
     * #bool 是否允许顾客自助入座开台
     */
    @Column("enable_table_customer")
    private boolean enableTableCustomer;

    /**
     * #bool 后付费支持
     */
    @Column("enable_pay_after")
    private boolean enablePayAfter;

    /**
     * #bool 是否支持顾客自助下单后付费 (将会考虑废弃)
     */
    @Column("enable_customer_self_pay_after")
    private boolean enableCustomerSelfPayAfter;

    /**
     * #bool 是否支持顾客自助下单后付费
     */
    @Column("enable_customer_self_open_table_pay_first")
    private boolean enableCustomerSelfOpenTablePayFirst;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 新增时间
     */
    @Column("create_time")
    private long createTime;

    /**
    * 顾客自助下单付款类型（0=默认，未设置过；1=先付费；2=后付费；3=先后付费皆可；后续 enableCustomerSelfPayAfter 将会考虑废弃）
    * CustomerPayEnum
    */
    @Column("customer_pay")
    public int customerPay;

    public int getCustomerPay() {
        return customerPay;
    }

    public void setCustomerPay(int customerPay) {
        this.customerPay = customerPay;
    }

    public boolean isEnableCustomerSelfOpenTablePayFirst() {
        return enableCustomerSelfOpenTablePayFirst;
    }

    public void setEnableCustomerSelfOpenTablePayFirst(boolean enableCustomerSelfOpenTablePayFirst) {
        this.enableCustomerSelfOpenTablePayFirst = enableCustomerSelfOpenTablePayFirst;
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

    public boolean isEnableTableMode() {
        return enableTableMode;
    }

    public void setEnableTableMode(boolean enableTableMode) {
        this.enableTableMode = enableTableMode;
    }

    public boolean isEnableTableAutoClear() {
        return enableTableAutoClear;
    }

    public void setEnableTableAutoClear(boolean enableTableAutoClear) {
        this.enableTableAutoClear = enableTableAutoClear;
    }

    public boolean isEnableTableCustomer() {
        return enableTableCustomer;
    }

    public void setEnableTableCustomer(boolean enableTableCustomer) {
        this.enableTableCustomer = enableTableCustomer;
    }

    public boolean isEnablePayAfter() {
        return enablePayAfter;
    }

    public void setEnablePayAfter(boolean enablePayAfter) {
        this.enablePayAfter = enablePayAfter;
    }

    public boolean isEnableCustomerSelfPayAfter() {
        return enableCustomerSelfPayAfter;
    }

    public void setEnableCustomerSelfPayAfter(boolean enableCustomerSelfPayAfter) {
        this.enableCustomerSelfPayAfter = enableCustomerSelfPayAfter;
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

    public static StoreTableSetting createDefault(int merchantId, long storeId){
        StoreTableSetting storeTableSetting = new StoreTableSetting();
        storeTableSetting.setMerchantId(merchantId);
        storeTableSetting.setStoreId(storeId);
        storeTableSetting.setCreateTime(System.currentTimeMillis());
        storeTableSetting.setUpdateTime(System.currentTimeMillis());
        return storeTableSetting;
    }
}