package com.huofu.module.i5wei.setting.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_presell_setting", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePresellSetting extends AbsEntity {

    /**
     * 店铺ID（主键）
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
     * 是否启用预售：0=不开启，1=开启
     */
    @Column("enabled")
    private boolean enabled;

    /**
     * 预售开启方式：1=按天开启，2=按周开启
     */
    @Column("pre_mode")
    private int preMode;

    /**
     * pre_mode=1，生效，提前（n＝x天）可预定
     */
    @Column("pre_days")
    private int preDays;

    /**
     * pre_mode=2，生效，每周几（1，2，3.。7）可预定下周
     */
    @Column("pre_week_day")
    private int preWeekDay;

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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPreMode() {
        return preMode;
    }

    public void setPreMode(int preMode) {
        this.preMode = preMode;
    }

    public int getPreDays() {
        return preDays;
    }

    public void setPreDays(int preDays) {
        this.preDays = preDays;
    }

    public int getPreWeekDay() {
        return preWeekDay;
    }

    public void setPreWeekDay(int preWeekDay) {
        this.preWeekDay = preWeekDay;
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

}