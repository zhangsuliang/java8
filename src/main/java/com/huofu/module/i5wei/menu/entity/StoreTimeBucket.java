package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.dbrouter.StoreTimeBucketDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.util.bean.BeanUtil;
import org.joda.time.MutableDateTime;

@Table(name = "tb_store_time_bucket", dalParser = StoreTimeBucketDbRouter.class)
public class StoreTimeBucket extends AbsEntity {

    private static final int mills_in_day = 24 * 60 * 60 * 1000;

    /**
     * id
     */
    @Id
    @Column("time_bucket_id")
    private long timeBucketId;

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
     * 营业时间开始，一天内开始的毫秒数
     */
    @Column("start_time")
    private int startTime;

    /**
     * 营业时间结束，一天内的毫秒数，如果跨天，数据可以大于24*60*60*1000
     */
    @Column("end_time")
    private int endTime;

    /**
     * 名称
     */
    @Column("name")
    private String name;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 数据最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 0:未删除 1:已删除 #bool
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 是否支持外送
     */
    @Column("delivery_supported")
    private boolean deliverySupported;

    /**
     * 外送开始时间。一天内开始的毫秒数
     */
    @Column("delivery_start_time")
    private int deliveryStartTime;

    /**
     * 外送结束时间。如果是当日，就是一天内的结束的毫秒数。如果是次日，就是一天的毫秒数+次日结束的毫秒数
     */
    @Column("delivery_end_time")
    private int deliveryEndTime;

    /**
     * 台位费
     */
    @Column("table_fee")
    private long tableFee;

    /**
     * 点餐提示
     */
    @Column
    private String tips;

    /**
     * 快速收款的默认收费项目id
     */
    @Column("quick_trade_charge_item_id")
    private long quickTradeChargeItemId;

    public long getQuickTradeChargeItemId() {
        return quickTradeChargeItemId;
    }

    public void setQuickTradeChargeItemId(long quickTradeChargeItemId) {
        this.quickTradeChargeItemId = quickTradeChargeItemId;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public static int getMills_in_day() {
        return mills_in_day;
    }

    public int getDeliveryStartTime() {
        return deliveryStartTime;
    }

    public void setDeliveryStartTime(int deliveryStartTime) {
        this.deliveryStartTime = deliveryStartTime;
    }

    public int getDeliveryEndTime() {
        return deliveryEndTime;
    }

    public void setDeliveryEndTime(int deliveryEndTime) {
        this.deliveryEndTime = deliveryEndTime;
    }

    public boolean isDeliverySupported() {
        return deliverySupported;
    }

    public void setDeliverySupported(boolean deliverySupported) {
        this.deliverySupported = deliverySupported;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
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

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public long getTableFee() {
        return tableFee;
    }

    public void setTableFee(long tableFee) {
        this.tableFee = tableFee;
    }

    public void refreshUpdateTime() {
        this.updateTime = System.currentTimeMillis();
    }

    public void makeDeleted(long now) {
        this.snapshot();
        this.updateTime = now;
        this.deleted = true;
        this.update();
    }

    public void initForCreate(long now) {
        this.deleted = false;
        this.setCreateTime(now);
        this.setUpdateTime(now);
    }

    /**
     * 是否需要判断是否是当前可提供的营业时段
     */
    private boolean testInBizTime;

    /**
     * 菜单日期
     */
    private long _menuDate;

    public boolean isTestInBizTime() {
        return testInBizTime;
    }

    public void setTestInBizTime(boolean testInBizTime, long _menuDate) {
        this.testInBizTime = testInBizTime;
        this._menuDate = _menuDate;
    }


    /**
     * 判断是否是当前营业时段，只有在testInBizTime=true是，才会判断<br>
     * 方法会被BeanUtil.copy自动调用
     *
     * @return true:营业中
     */
    public boolean isInBizTime() {
        if (!this.isTestInBizTime()) {
            return false;
        }
        return this.isInTime(this._menuDate, System.currentTimeMillis());
    }

    /**
     * 当前时间是否已经晚于营业时段的时间
     *
     * @return true:已经过了营业时间
     */
    public boolean isAfterBizTime(long menuDate, long now) {
        //跨天
        MutableDateTime menuEnd = new MutableDateTime(menuDate);
        menuEnd.setMillisOfDay(0);
        menuEnd.addMillis(this.endTime);

        long menuEndTime = menuEnd.getMillis();
        if (now > menuEndTime) {
            return true;
        }
        return false;
    }

    /**
     * 按照相对时间判断,只要比开始时间小,就返回true
     */
    public boolean isBeforeBizTime() {
        long now = System.currentTimeMillis();
        MutableDateTime mdt = new MutableDateTime(now);
        mdt.setMillisOfDay(0);
        long res = now - mdt.getMillis();
        if (res < this.startTime) {
            return true;
        }
        return false;
    }

    public boolean isInTime(long menuDate) {
        return this.isInTime(menuDate, System.currentTimeMillis());
    }

    public boolean isInTime(long menuDate, long now) {
        MutableDateTime menuEnd = new MutableDateTime(menuDate);
        menuEnd.setMillisOfDay(0);
        menuEnd.addMillis(this.endTime);
        long menuEndTime = menuEnd.getMillis();

        MutableDateTime menuStart = new MutableDateTime(menuDate);
        menuStart.setMillisOfDay(0);
        menuStart.addMillis(this.startTime);
        long menuStartTime = menuStart.getMillis();

        if (now >= menuStartTime && now <= menuEndTime) {
            return true;
        }
        return false;
    }

    /**
     * 判断给定的时间戳与营业时段的关系
     *
     * @param time 指定的时间
     * @return 1:与指定时间相比较，还没到营业时间 0:在营业时间之内 -1:与指定时间相比较，已经过了营业时间
     */
    public int compareTo(long time) {
        MutableDateTime mdt = new MutableDateTime(time);
        long millisOfDay = mdt.getMillisOfDay();
        //跨天
        if (this.endTime > mills_in_day) {
            //如果当前时间在0点之前，并且大于开始时间，为营业中
            //如果当前时间在0点之后，并且小于结束时间，为营业中
            //如果当前时间在
            if (millisOfDay > this.startTime) {
                return 0;
            }
            if (millisOfDay <= this.endTime - mills_in_day) {
                return 0;
            }
            return 1;
        } else {
            if (millisOfDay < this.startTime) {
                return 1;
            }
            if (millisOfDay >= this.startTime && millisOfDay <= this.endTime) {
                return 0;
            }
            return -1;
        }


//        ==============

//        MutableDateTime mdt = new MutableDateTime(time);
//        mdt.setMillisOfDay(0);
//        //跨天
//        if (this.endTime > mills_in_day) {
//            //如果当前时间在0点之前，并且大于开始时间，为营业中
//            //如果当前时间在0点之后，并且小于结束时间，为营业中
//            //如果当前时间在
//            mdt.getMillisOfDay()
//
//            return 0;
//        } else {
//            long res = time - mdt.getMillis();
//            if (res < this.startTime) {
//                return 1;
//            }
//            if (res >= this.startTime && res <= this.endTime) {
//                return 0;
//            }
//            return -1;
//        }
    }

    /**
     * 外送时间是否跨天
     */
    public boolean isDeliveryTimeRes(long time) {
        MutableDateTime mdt = new MutableDateTime(time);
        mdt.setMillisOfDay(0);
        long res = time - mdt.getMillis();
        if (this.getDeliveryEndTimeForBiz() < mills_in_day) {
            //外送结束时间不跨天
            return false;
        } else {
            if (res > 0 && res <= (this.getDeliveryEndTimeForBiz() - mills_in_day)) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 获得当前时间下营业时段的外送开始时间
     *
     * @return 外送开始时间
     */
    public long getDeliveryStartTimeForDate(long date) {
        MutableDateTime mdt = new MutableDateTime(date);
        mdt.setMillisOfDay(this.getDeliveryStartTimeForBiz());
        return mdt.getMillis();
    }

    /**
     * 获得当前时间下营业时段的外送结束时间
     *
     * @return 外送开始时间
     */
    public long getDeliveryEndTimeForDate(long date) {
        MutableDateTime mdt = new MutableDateTime(date);
        mdt.setMillisOfDay(0);
        mdt.addMillis(this.getDeliveryEndTimeForBiz());
        return mdt.getMillis();
    }

    public void changeDeliverySupported(boolean deliverySupported) {
        this.snapshot();
        this.setDeliverySupported(deliverySupported);
        this.setUpdateTime(System.currentTimeMillis());
        this.update();
    }

    public StoreTimeBucket copySelf() {
        StoreTimeBucket storeTimeBucket = new StoreTimeBucket();
        BeanUtil.copy(this, storeTimeBucket);
        return storeTimeBucket;
    }

    public boolean isDeliveryTimeEmpty() {
        if (this.deliveryStartTime == 0 || this.deliveryEndTime == 0) {
            return true;
        }
        return false;
    }

    /**
     * 业务调用方法，如果没有设置，会使用营业时段的时间
     *
     * @return 外送结束时间
     */
    public int getDeliveryEndTimeForBiz() {
        if (this.isDeliveryTimeEmpty()) {
            return endTime;
        }
        return this.deliveryEndTime;
    }

    /**
     * 业务调用方法，如果没有设置，会使用营业时段的时间
     *
     * @return 外送开始时间
     */
    public int getDeliveryStartTimeForBiz() {
        if (this.isDeliveryTimeEmpty()) {
            return this.startTime;
        }
        return deliveryStartTime;
    }

    /**
     * 获得跨天进行时间判断的分隔点
     */
    public static long getOverDayseparate() {
        MutableDateTime mdt = new MutableDateTime();
        mdt.setMillisOfDay(0);
        mdt.setHourOfDay(5);
        return mdt.getMillis();
    }

    /**
     * 当前时间是否在外送时间内
     */
    public boolean isInDeliveryTime(long time) {
        if (time < this.getDeliveryStartTimeForDate(time)) {
            return false;
        }
        if (time > this.getDeliveryEndTimeForDate(time)) {
            return false;
        }
        return true;
    }

    /**
     * 在跨天的情况下，当前时间是否为0点之后。只有在跨天营业中调用才有效。否则结果不正确
     */
    public boolean isOverDayNextPart(long time) {
        MutableDateTime mdt = new MutableDateTime(time);
        int millisOfDay = mdt.getMillisOfDay();
        if (millisOfDay >= this.startTime) {
            return false;
        }
        return true;
    }

    public boolean isOverDay() {
        //跨天
        if (this.endTime > mills_in_day) {
            return true;
        }
        return false;
    }
}
