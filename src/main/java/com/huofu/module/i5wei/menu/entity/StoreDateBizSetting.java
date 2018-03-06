package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.menu.dbrouter.StoreDateBizSettingDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.DateUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.MutableDateTime;

/**
 * Auto created by i5weitools
 * 特殊日期菜单设置
 */
@Table(name = "tb_store_date_biz_setting", dalParser =
        StoreDateBizSettingDbRouter.class)
public class StoreDateBizSetting extends BaseEntity {

    private static final Log log = LogFactory.getLog(StoreDateBizSetting.class);

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
     * 商户id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 指定使用星期几的周期菜单
     */
    @Column("week_day")
    private int weekDay;

    /**
     * 是否全天暂停 #bool
     */
    @Column("paused")
    private boolean paused;

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

    /**
     * 删除标识 #bool
     */
    @Column("deleted")
    private boolean deleted;

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

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        this.deleted = false;
    }

    public int getSelectedDateWeekDay() {
        MutableDateTime mdt = new MutableDateTime(this.selectedDate);
        return mdt.getDayOfWeek();
    }

    public static int getWeekDay(StoreDateBizSetting storeDateBizSetting) {
        if (storeDateBizSetting == null) {
            return 0;
        }
        if (storeDateBizSetting.isDeleted()) {
            return 0;
        }
        return storeDateBizSetting.getWeekDay();
    }

    public static int getWeekDay(StoreDateBizSetting storeDateBizSetting, long defTimeOfWeekDay) {
        int weekDay = DateUtil.getWeekDayByDate(defTimeOfWeekDay);
        if (storeDateBizSetting == null) {
            return weekDay;
        }
        if (storeDateBizSetting.isDeleted()) {
            return weekDay;
        }
        if (storeDateBizSetting.getWeekDay() > 0) {
            weekDay = storeDateBizSetting.getWeekDay();
        }
        return weekDay;
    }

    public static boolean isPaused(StoreDateBizSetting storeDateBizSetting) {
        if (storeDateBizSetting == null) {
            return false;
        }
        if (storeDateBizSetting.isDeleted()) {
            return false;
        }
        return storeDateBizSetting.isPaused();
    }
}