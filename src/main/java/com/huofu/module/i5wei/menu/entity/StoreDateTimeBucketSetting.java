package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.menu.dbrouter.StoreDateTimeBucketSettingDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

import java.util.Map;

/**
 * Auto created by i5weitools
 * 特殊日期指定的营业时段设置
 */
@Table(name = "tb_store_date_time_bucket_setting", dalParser =
        StoreDateTimeBucketSettingDbRouter.class)
public class StoreDateTimeBucketSetting extends BaseEntity {

    /**
     * 店铺id
     */
    @Id(0)
    @Column("store_id")
    private long storeId;

    /**
     * 指定日期
     */
    @Id(1)
    @Column("selected_date")
    private long selectedDate;

    /**
     * 营业时段id
     */
    @Id(2)
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 商户id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 是否暂停 #bool
     */
    @Column("paused")
    private boolean paused;

    /**
     * 删除标识 #bool
     */
    @Column
    private boolean deleted;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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

    public long getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(long selectedDate) {
        this.selectedDate = selectedDate;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
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

    public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        this.deleted = false;
    }

    public static boolean isPaused(StoreDateTimeBucketSetting
                                           storeDateTimeBucketSetting) {
        if (storeDateTimeBucketSetting == null) {
            return false;
        }
        if (storeDateTimeBucketSetting.isDeleted()) {
            return false;
        }
        return storeDateTimeBucketSetting.isPaused();
    }

    public static StoreDateTimeBucketSetting getStoreDateTimeBucketSetting
            (long date, long timeBucketId, Map<String,
                    StoreDateTimeBucketSetting> settingMap) {
        return settingMap.get(date + "_" + timeBucketId);
    }
}