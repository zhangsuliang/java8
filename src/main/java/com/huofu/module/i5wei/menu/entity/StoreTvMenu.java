package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.menu.dbrouter.StoreTvMenuDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.module.base.WeekDayEnum;
import huofuhelper.util.DateUtil;

import java.util.Date;
import java.util.Map;

/**
 * Auto created by i5weitools
 * 电视菜单
 */
@Table(name = "tb_store_tv_menu", dalParser = StoreTvMenuDbRouter.class)
public class StoreTvMenu extends BaseEntity {

    /**
     * 店铺id
     */
    @Id(0)
    @Column("store_id")
    private long storeId;

    /**
     * 商户id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 营业时段id
     */
    @Id(1)
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 使用日期存储到指定日期的0:00:00.000的时间,参数可以为0,表示不限制时间,可以为1,2,3,4,5,6,7,表示按照星期x进行周期循环
     */
    @Id(2)
    @Column("use_date")
    private long useDate;

    /**
     * 菜单排版信息json存储
     */
    @Column("content")
    private String content;

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
     * 停用状态
     */
    @Column("time_bucket_paused")
    private int timeBucketPaused;

    private StoreTimeBucket storeTimeBucket;

    public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
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

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public long getUseDate() {
        return useDate;
    }

    public void setUseDate(long useDate) {
        this.useDate = useDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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
    
    public int getTimeBucketPaused() {
        return timeBucketPaused;
    }

    public void setTimeBucketPaused(int timeBucketPaused) {
        this.timeBucketPaused = timeBucketPaused;
}

	public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        if (this.useDate == 0) {
            sb.append("默认");
        } else if (this.useDate >= 1 && this.useDate <= 7) {
            sb.append(getWeekDesc(this.useDate));
        } else {
            sb.append(DateUtil.formatDate("yyyy年MM月dd日", new Date(this.useDate)));
        }
        if (this.storeTimeBucket != null) {
            sb.append(storeTimeBucket.getName());
        }
        return sb.toString();
    }

    private String getWeekDesc(long day) {
        if (day == WeekDayEnum.Monday.getValue()) {
            return "周一";
        }
        if (day == WeekDayEnum.Tuesday.getValue()) {
            return "周二";
        }
        if (day == WeekDayEnum.Wednesday.getValue()) {
            return "周三";
        }
        if (day == WeekDayEnum.Thursday.getValue()) {
            return "周四";
        }
        if (day == WeekDayEnum.Friday.getValue()) {
            return "周五";
        }
        if (day == WeekDayEnum.Saturday.getValue()) {
            return "周六";
        }
        if (day == WeekDayEnum.Sunday.getValue()) {
            return "周日";
        }
        return null;
    }

    public boolean isUseDate4Period() {
        if (this.useDate >= 1 && this.useDate <= 7) {
            return true;
        }
        return false;
    }

    public boolean isUseDate4Def() {
        if (this.useDate == 0) {
            return true;
        }
        return false;
    }

    public boolean isUseDate4Date() {
        if (this.useDate > 1000) {
            return true;
        }
        return false;
    }

    /**
     * 在同一天进行顺序比较
     *
     * @param o       比较对象
     * @param sortMap 营业时段排序 key=[营业时段id,0表示无营业时段] value=[从小到大 0,1,2,3....]
     * @return -1,0,1 less thran, equal, greater than 比较对象
     */
    public int compareTo4SameDay(StoreTvMenu o, Map<Long, Integer> sortMap) {
        int sort1 = sortMap.get(this.getTimeBucketId());
        int sort2 = sortMap.get(o.getTimeBucketId());
        if (sort1 < sort2) {
            return -1;
        }
        if (sort1 > sort2) {
            return 1;
        }

        //详细时间>周期>默认
        if (this.isUseDate4Date()) {
            return -1;
        }
        if (this.isUseDate4Period()) {
            if (o.isUseDate4Date()) {
                return 1;
            }
            return -1;
        }
        if (this.isUseDate4Def()) {
            return 1;
        }
        return 0;
    }
}