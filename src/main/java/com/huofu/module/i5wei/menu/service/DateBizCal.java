package com.huofu.module.i5wei.menu.service;

import java.util.List;

/**
 * Created by akwei on 9/6/15.
 */
public class DateBizCal {

    /**
     * 日历日期
     */
    private long date;

    private boolean paused;

    /**
     * 是否指定使用周几的菜单，如果不特殊指定，值=0
     */
    private int menuWeekDay;

    private List<TimeBucketMenuCal> timeBucketMenuCals;

    public int getMenuWeekDay() {
        return menuWeekDay;
    }

    public void setMenuWeekDay(int menuWeekDay) {
        this.menuWeekDay = menuWeekDay;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public List<TimeBucketMenuCal> getTimeBucketMenuCals() {
        return timeBucketMenuCals;
    }

    public void setTimeBucketMenuCals(List<TimeBucketMenuCal> timeBucketMenuCals) {
        this.timeBucketMenuCals = timeBucketMenuCals;
    }

//    public List<TimeBucketMenuCal> copyTimeBucketMenuCals() {
//        return this.timeBucketMenuCals.stream().map
//                (TimeBucketMenuCal::copySelf).collect(Collectors.toList());
//    }
}
