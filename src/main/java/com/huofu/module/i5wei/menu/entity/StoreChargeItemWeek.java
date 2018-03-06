package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.dbrouter.StoreChargeItemWeekDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;

/**
 * Auto created by i5weitools
 * 店铺收费项目、营业时段、周期关系
 */
@Table(name = "tb_store_charge_item_week", dalParser = StoreChargeItemWeekDbRouter.class)
public class StoreChargeItemWeek extends AbsEntity {


    /**
     * 周期关系id
     */
    @Id
    @Column("item_week_id")
    private long itemWeekId;

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
     * 收费项目id
     */
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 营业时段id
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 周期 (周一,周二, ....,周日)
     */
    @Column("week_day")
    private int weekDay;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 有效期开始时间
     */
    @Column("begin_time")
    private long beginTime;

    /**
     * 有效期结束时间
     */
    @Column("end_time")
    private long endTime;

    /**
     * #bool 0:未删除 1:已删除
     */
    @Column("deleted")
    private boolean deleted;

    private StoreTimeBucket storeTimeBucket;

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }

    public long getItemWeekId() {
        return itemWeekId;
    }

    public void setItemWeekId(long itemWeekId) {
        this.itemWeekId = itemWeekId;
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

    public long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(long chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isCurrentWeek(long time) {
        if (this.beginTime <= time && this.endTime > time) {
            return true;
        }
        return false;
    }

    public boolean isNextWeek(long time) {
        long nextWeekDay = getNextWeekFirstDay(time);
        if (this.beginTime >= nextWeekDay) {
            return true;
        }
        return false;
    }

    private static long getNextWeekFirstDay(long time) {
        MutableDateTime mdt = new MutableDateTime(time);
        mdt.addWeeks(1);
        mdt.setDayOfWeek(1);
        mdt.setMillisOfDay(0);
        return mdt.getMillis();
    }

    /**
     * 数据是否在指定日期出现
     *
     * @param weekDay 指定的星期[X]
     * @param time    指定日期
     * @return true 出现
     */
    public boolean isValidForDate(long time, int weekDay) {
        if (this.beginTime <= time && this.endTime >= time) {
            if (this.weekDay == weekDay) {
                return true;
            }
        }
        return false;
    }

    /**
     * 数据是否在指定日期和营业时段出现
     *
     * @param dateTime     指定日期
     * @param timeBucketId 营业时段id
     * @return true 出现
     */
    public boolean isValidForDateAndTimeBucket(DateTime dateTime, long
            timeBucketId) {
        if (this.beginTime <= dateTime.getMillis() && this.endTime >=
                dateTime.getMillis()) {
            if (this.weekDay == dateTime.getDayOfWeek()) {
                if (this.timeBucketId == timeBucketId) {
                    return true;
                }
            }
        }
        return false;
    }
}