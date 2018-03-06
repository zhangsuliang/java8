package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemWeekDAO;
import com.huofu.module.i5wei.menu.dao.StoreDateBizSettingDAO;
import com.huofu.module.i5wei.menu.dao.StoreDateTimeBucketSettingDAO;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.*;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreDateBizSettingParam;
import huofucore.facade.i5wei.menu.StoreDateTimeBucketSettingParam;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 店铺菜单相关服务
 * Created by akwei on 8/28/15.
 */
@Service
public class StoreMenuService {

    private static final Log logger = LogFactory.getLog(StoreMenuService.class);

    @Autowired
    private StoreDateBizSettingDAO storeDateBizSettingDAO;

    @Autowired
    private StoreDateTimeBucketSettingDAO
            storeDateTimeBucketSettingDAO;

    @Autowired
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Autowired
    private StoreTimeBucketDAO storeTimeBucketDAO;

    public StoreDateBizSetting saveStoreDateBizSetting(StoreDateBizSettingParam param) throws T5weiException {
        try {
            return this._saveStoreDateBizSetting(param);
        } catch (DuplicateKeyException e) {
            return this._saveStoreDateBizSetting(param);
        }
    }

    private StoreDateBizSetting _saveStoreDateBizSetting
            (StoreDateBizSettingParam param) throws T5weiException {
        final long selectedDate = DateUtil.getBeginTime(param.getSelectedDate
                (), null);
        int weekDay = param.getWeekDay();
        if (weekDay > 0) {
            int selectedWeekDay = DateUtil.getWeekDayByDate(selectedDate, null);
            if (selectedWeekDay != weekDay) {
                MutableDateTime mdt = new MutableDateTime(selectedDate);
                //检测指定的星期[X]对应的日期是否是指定菜单，如果是指定菜单，就不能选择此日期
                mdt.setDayOfWeek(param.getWeekDay());
                StoreDateBizSetting storeDateBizSetting = this
                        .storeDateBizSettingDAO.getForSelectedDate(param
                                .getMerchantId(), param.getStoreId(), mdt
                                .getMillis(), false, false);
                if (StoreDateBizSetting.getWeekDay(storeDateBizSetting) > 0) {
                    throw new T5weiException(T5weiErrorCodeType
                            .SELECTED_WEEK_DAY_MENU_INVALID.getValue(),
                            "weekDay[" + param.getWeekDay() + "]" +
                                    " dayOfWeek[" + mdt + "] no menu");
                }
            } else {
                weekDay = 0;//如果设置的周期=指定日期周期，weekDay=0
            }
        } else {
            weekDay = 0;//如果设置的周期=指定日期周期，weekDay=0
        }
        long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (selectedDate < today) {
            throw new T5weiException(T5weiErrorCodeType
                    .SELECTED_DATE_MUST_START_WITH_TODAY.getValue(),
                    "selectedDate[" + new DateTime(selectedDate) + "] must start " +
                            "with today[" + new DateTime(today) + "]");
        }

        StoreDateBizSetting storeDateBizSetting = this.storeDateBizSettingDAO
                .getById(param.getMerchantId(), param.getStoreId(), selectedDate, false, false);
        if (storeDateBizSetting == null) {
            storeDateBizSetting = new StoreDateBizSetting();
            BeanUtil.copy(param, storeDateBizSetting);
            storeDateBizSetting.setWeekDay(weekDay);
            storeDateBizSetting.init4Create();
            storeDateBizSetting.create();
        } else {
            storeDateBizSetting.snapshot();
            BeanUtil.copy(param, storeDateBizSetting);
            storeDateBizSetting.setWeekDay(weekDay);
            storeDateBizSetting.setDeleted(false);
            storeDateBizSetting.setUpdateTime(System.currentTimeMillis());
            storeDateBizSetting.update();
        }
        return storeDateBizSetting;
    }

    public StoreDateTimeBucketSetting saveStoreDateTimeBucketSetting(StoreDateTimeBucketSettingParam param) throws T5weiException {
        try {
            return this._saveStoreDateTimeBucketSetting(param);
        } catch (DuplicateKeyException e) {
            return this._saveStoreDateTimeBucketSetting(param);
        }
    }

    private StoreDateTimeBucketSetting _saveStoreDateTimeBucketSetting
            (StoreDateTimeBucketSettingParam param) throws T5weiException {
        StoreDateTimeBucketSetting storeDateTimeBucketSetting = this
                .storeDateTimeBucketSettingDAO.getById(param
                        .getMerchantId(), param.getStoreId(), param
                        .getSelectedDate(), param.getTimeBucketId());
        if (storeDateTimeBucketSetting == null) {
            storeDateTimeBucketSetting = new StoreDateTimeBucketSetting();
            BeanUtil.copy(param, storeDateTimeBucketSetting);
            storeDateTimeBucketSetting.init4Create();
            storeDateTimeBucketSetting.create();

        } else {
            storeDateTimeBucketSetting.snapshot();
            BeanUtil.copy(param, storeDateTimeBucketSetting);
            storeDateTimeBucketSetting.setUpdateTime(System.currentTimeMillis());
            storeDateTimeBucketSetting.setDeleted(false);
            storeDateTimeBucketSetting.update();
        }
        return storeDateTimeBucketSetting;
    }

    public StoreDateBizSetting getStoreDateBizSettingForSelectedDate
            (int merchantId, long storeId, long selectedDate, boolean enableSlave, boolean enableCache) {
        return this.storeDateBizSettingDAO
                .getForSelectedDate(merchantId, storeId, selectedDate, enableSlave, enableCache);
    }

    public StoreDateBizSetting getStoreDateBizSettingForSelectedDate
            (int merchantId, long storeId, long selectedDate) {
        return this.getStoreDateBizSettingForSelectedDate(merchantId, storeId, selectedDate, false, false);
    }

    public StoreDateBizSetting loadStoreDateBizSettingForSelectedDate
            (int merchantId, long storeId, long selectedDate) throws T5weiException {
        return this.storeDateBizSettingDAO
                .loadForSelectedDate(merchantId, storeId, selectedDate, false, false);
    }

    public StoreDateTimeBucketSetting loadStoreDateTimeBucketSettingForSelecteDate
            (int merchantId, long storeId, long selectedDate, long timeBucketId) throws T5weiException {
        return this.storeDateTimeBucketSettingDAO.loadForDate(merchantId, storeId, selectedDate, timeBucketId, false, false);
    }

    public List<StoreDateBizSetting> getStoreDateBizSettings(int merchantId,
                                                             long storeId,
                                                             long minDate,
                                                             long maxDate) {
        return this.storeDateBizSettingDAO.getListForSelectedDateRange(merchantId, storeId, minDate, maxDate);
    }

    public List<StoreDateTimeBucketSetting>
    getStoreDateTimeBucketSettingsForSelectedDate(int merchantId, long storeId, long selectedDate) {
        return this.storeDateTimeBucketSettingDAO.getListForDate(merchantId, storeId, selectedDate, false, false);
    }

    /**
     * 获得菜单日期实际使用的星期[X]
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param date       菜单日期 00:00:00
     * @return 星期[X]。如果指定日期暂停营业，就返回当前时间的星期[X]
     */
    public int getWeekDayOfMenuDate(int merchantId, long storeId, long date) {
        StoreDateBizSetting setting = this.storeDateBizSettingDAO.getForSelectedDate(merchantId, storeId, date, false, false);
        if (setting == null || StoreDateBizSetting.isPaused(setting)) {
            return DateUtil.getWeekDayByDate(date);
        }
        int weekDay = setting.getWeekDay();
        if (weekDay <= 0) {
            return DateUtil.getWeekDayByDate(date);
        }
        return weekDay;
    }

    /**
     * @param forShow 是否为了获取菜单显示使用
     */
    public List<DateBizCal> getDateBizCalsForDateRange(int merchantId, long
            storeId, long beginDate, long endDate, boolean forShow) {
        //当天的数据
        List<DateBizCal> todayList = this._getDateBizCalsForDateRange(merchantId, storeId, beginDate, endDate);

        List<DateBizCal> dateBizCals = Lists.newArrayList();
        if (forShow) {
            //判断店铺的营业时段是否有跨天的,如果有跨天的,就需要单独获取前一天指定跨天的营业时段
            //判断当前时间是否在跨天分隔时间之前,如果之前就判断是否有跨天时段,如果有跨天时段,就获取跨天时段的数据
            StoreTimeBucket overDayTimeBucket = this.storeTimeBucketDAO.get4OverDay(merchantId, storeId);
            if (overDayTimeBucket != null && !overDayTimeBucket.isDeleted()) {
                MutableDateTime premdt = new MutableDateTime(beginDate);
                premdt.setMillisOfDay(0);
                premdt.addDays(-1);
                long preBeginDate = premdt.getMillis();
                long preEndDate = premdt.getMillis();
                List<DateBizCal> preList = this._getDateBizCalsForDateRange(merchantId, storeId, preBeginDate, preEndDate);
                for (DateBizCal dateBizCal : preList) {
                    if (dateBizCal.getTimeBucketMenuCals() != null) {
                        Iterator<TimeBucketMenuCal> iterator = dateBizCal.getTimeBucketMenuCals().iterator();
                        while (iterator.hasNext()) {
                            TimeBucketMenuCal next = iterator.next();
                            //只获取指定的跨天营业时段
                            if (next.getTimeBucketId() != overDayTimeBucket.getTimeBucketId()) {
                                iterator.remove();
                            }
                        }
                    }
                }
                dateBizCals.addAll(preList);
            }
        }
        dateBizCals.addAll(todayList);
        return dateBizCals;
    }

    public List<DateBizCal> _getDateBizCalsForDateRange(int merchantId, long
            storeId, long beginDate, long endDate) {
        long beginTime = DateUtil.getBeginTime(beginDate, null);
        long endTime = DateUtil.getEndTime(endDate, null);

        Map<Long, StoreDateBizSetting> bizSettingMap = this
                .storeDateBizSettingDAO.getMapForSelectedDateRange
                        (merchantId, storeId, beginDate, endDate);

        Map<String, StoreDateTimeBucketSetting> bucketSettingMap =
                storeDateTimeBucketSettingDAO.getMapForDateRange(merchantId, storeId,
                        beginDate, endDate);

        List<StoreChargeItemWeek> storeChargeItemWeeks = this
                .storeChargeItemWeekDAO.getListForTimeRange(merchantId,
                        storeId, endTime, beginTime);

        DateTime beginDateTime = new DateTime(beginDate);
        DateTime endDateTime = new DateTime(endDate);
        Days days = Days.daysBetween(beginDateTime, endDateTime);
        int distance = days.getDays();
        List<DateBizCal> dateBizCals = new ArrayList<>();
        for (int i = 0; i <= distance; i++) {
            MutableDateTime mdt = new MutableDateTime(beginDate);
            mdt.addDays(i);
            dateBizCals.add(_buildDateMenuCal(mdt, storeChargeItemWeeks,
                    bizSettingMap.get(mdt.getMillis()), bucketSettingMap));
        }
        this._buildDateMenuCalsSpecially(merchantId, storeId, dateBizCals, bucketSettingMap);
        this._buildTimeBuckets(merchantId, storeId, dateBizCals);
        return dateBizCals;
    }

    public DateBizCal getDateBizCalForDate(int merchantId, long
            storeId, long date) {
        List<DateBizCal> dateBizCals = this.getDateBizCalsForDateRange
                (merchantId, storeId, date, date, false);
        if (dateBizCals.isEmpty()) {
            return null;
        }
        return dateBizCals.get(0);
    }

    /**
     * 计算每天和每个营业时段出现的收费项目数量
     *
     * @param dateTime             日期
     * @param storeChargeItemWeeks 所有的收费项目周期设置
     * @return {@link DateBizCal}
     */
    private static DateBizCal _buildDateMenuCal(MutableDateTime dateTime,
                                                List<StoreChargeItemWeek>
                                                        storeChargeItemWeeks,
                                                StoreDateBizSetting
                                                        storeDateBizSetting,
                                                Map<String, StoreDateTimeBucketSetting>
                                                        bucketSettingMap) {
        DateBizCal dateBizCal = new DateBizCal();
        dateBizCal.setDate(dateTime.getMillis());
        dateBizCal.setMenuWeekDay(StoreDateBizSetting.getWeekDay(storeDateBizSetting));

        List<TimeBucketMenuCal> timeBucketMenuCals = _buildTimeBucketMenuCals(dateTime.getMillis(), dateTime.getDayOfWeek(), storeChargeItemWeeks);
        dateBizCal.setTimeBucketMenuCals(timeBucketMenuCals);

        _buildTimeBucketPaused(dateBizCal.getDate(), timeBucketMenuCals, bucketSettingMap);

        if (StoreDateBizSetting.isPaused(storeDateBizSetting)) {
            dateBizCal.setPaused(true);
            return dateBizCal;
        }
        return dateBizCal;
    }

    private static void _buildTimeBucketPaused(long date,
                                               List<TimeBucketMenuCal>
                                                       timeBucketMenuCals,
                                               Map<String, StoreDateTimeBucketSetting>
                                                       bucketSettingMap) {
        for (TimeBucketMenuCal timeBucketMenuCal : timeBucketMenuCals) {
            if (StoreDateTimeBucketSetting.isPaused(StoreDateTimeBucketSetting.getStoreDateTimeBucketSetting(date, timeBucketMenuCal.getTimeBucketId(), bucketSettingMap))) {
                timeBucketMenuCal.setPaused(true);
            }
        }
    }

    /**
     * 构造一天当中的所有营业时段的收费项目
     *
     * @param time                 日期时间戳
     * @param weekDay              指定的星期[X]
     * @param storeChargeItemWeeks 收费项目数据
     * @return List<TimeBucketMenuCal>
     */
    private static List<TimeBucketMenuCal> _buildTimeBucketMenuCals(long time, int weekDay, List<StoreChargeItemWeek> storeChargeItemWeeks
    ) {
        Map<Long, List<StoreChargeItemWeek>> itemWeeksMap = new HashMap<>();
        for (StoreChargeItemWeek itemWeek : storeChargeItemWeeks) {
            if (!itemWeek.isValidForDate(time, weekDay)) {
                continue;
            }
            List<StoreChargeItemWeek> itemWeeks = itemWeeksMap.get(itemWeek
                    .getTimeBucketId());
            if (itemWeeks == null) {
                itemWeeks = new ArrayList<>();
                itemWeeksMap.put(itemWeek.getTimeBucketId(), itemWeeks);
            }
            itemWeeks.add(itemWeek);
        }

//        //debug ==================
//        StringBuilder sb = new StringBuilder("\n====== " +
//                "timeBucketMenuCals ======\n");
//        sb.append("date:" + dateTime).append("\n");
//        //debug end ==================

        List<TimeBucketMenuCal> timeBucketMenuCals = new ArrayList<>();
        for (Map.Entry<Long, List<StoreChargeItemWeek>> entry : itemWeeksMap.entrySet()) {
            TimeBucketMenuCal timeBucketMenuCal = new TimeBucketMenuCal();
            timeBucketMenuCal.setTimeBucketId(entry.getKey());
            timeBucketMenuCal.setChargeItemAmount(entry.getValue().size());
            timeBucketMenuCals.add(timeBucketMenuCal);

//            //debug ==================
//            sb.append("timeBucketId:" + timeBucketMenuCal.getTimeBucketId()).append
//                    ("\n");
//            sb.append("chargeItemIds:").append("\n");
//            for (StoreChargeItemWeek obj : entry.getValue()) {
//                sb.append("\t").append(obj.getChargeItemId()).append("\n");
//            }
//            //debug end ==================
        }

//        //debug ==================
//        sb.append("\n====== timeBucketMenuCals end ======\n");
//        logger.info(sb.toString());
//
//        //debug end ==================

        return timeBucketMenuCals;
    }

    /**
     * 进行经营日历的特殊处理
     *
     * @param merchantId                    商户id
     * @param storeId                       店铺id
     * @param dateBizCals                   未特殊处理的经营日历数据
     * @param storeDateTimeBucketSettingMap 日期中营业时段的特殊设置map
     */
    private void _buildDateMenuCalsSpecially(int merchantId, long storeId,
                                             List<DateBizCal> dateBizCals,
                                             Map<String, StoreDateTimeBucketSetting>
                                                     storeDateTimeBucketSettingMap) {
        //对指定日期的经营特殊处理
        for (DateBizCal dateBizCal : dateBizCals) {
            this._buildDateMenuCalSpecially(merchantId, storeId, dateBizCal, storeDateTimeBucketSettingMap);
        }
    }

    /**
     * 对指定日期的特殊设置进行处理，例如指定使用的周期菜单，是否暂停营业等
     *
     * @param merchantId                    商户id
     * @param storeId                       店铺id
     * @param dateBizCal                    指定日期的经营日历
     * @param storeDateTimeBucketSettingMap 日其中营业时段的特殊设置
     */
    private void _buildDateMenuCalSpecially(int merchantId, long storeId, DateBizCal dateBizCal,
                                            Map<String, StoreDateTimeBucketSetting> storeDateTimeBucketSettingMap) {
        if (dateBizCal.getMenuWeekDay() > 0) {
            List<StoreChargeItemWeek> list = this.storeChargeItemWeekDAO
                    .getListForDate(merchantId, storeId, dateBizCal
                            .getMenuWeekDay(), dateBizCal.getDate());
            List<TimeBucketMenuCal> timeBucketMenuCals = _buildTimeBucketMenuCals(
                    dateBizCal.getDate(), dateBizCal.getMenuWeekDay(), list);

            dateBizCal.setTimeBucketMenuCals(timeBucketMenuCals);

            _buildTimeBucketPaused(dateBizCal.getDate(), timeBucketMenuCals,
                    storeDateTimeBucketSettingMap);
        }
    }

    private void _buildTimeBuckets(int merchantId, long storeId,
                                   List<DateBizCal> dateBizCals) {
        boolean enableSlave = true;
        long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        //设置营业时段
        List<Long> timeBucketIds = new ArrayList<>();
        for (DateBizCal dateBizCal : dateBizCals) {
            timeBucketIds.addAll(dateBizCal.getTimeBucketMenuCals().stream()
                    .map(TimeBucketMenuCal::getTimeBucketId).collect
                            (Collectors.toList()));
        }
        Map<Long, StoreTimeBucket> timeBucketMap = this.storeTimeBucketDAO
                .getMapInIds(merchantId, storeId, timeBucketIds, enableSlave);
        for (DateBizCal dateBizCal : dateBizCals) {
            Iterator<TimeBucketMenuCal> it = dateBizCal.getTimeBucketMenuCals
                    ().iterator();
            while (it.hasNext()) {
                TimeBucketMenuCal timeBucketMenuCal = it.next();
                StoreTimeBucket storeTimeBucket = timeBucketMap.get
                        (timeBucketMenuCal.getTimeBucketId());
                if (storeTimeBucket == null) {
                    it.remove();
                    continue;
                }
                if (storeTimeBucket.isDeleted() && dateBizCal.getDate() >= today) {
                    it.remove();
                    continue;
                }
                StoreTimeBucket copy = storeTimeBucket.copySelf();
//                if (today == dateBizCal.getDate()) {
                copy.setTestInBizTime(true, dateBizCal.getDate());
//                }
                timeBucketMenuCal.setStoreTimeBucket(copy);
            }
            StoreTimeBucketUtil.sortTimeBucketMenuCals(dateBizCal.getTimeBucketMenuCals());
        }
    }
}
