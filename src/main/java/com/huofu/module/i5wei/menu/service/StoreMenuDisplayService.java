package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.dao.*;
import com.huofu.module.i5wei.menu.entity.*;
import huofucore.facade.config.client.ClientTypeEnum;
import huofuhelper.util.DateUtil;
import huofuhelper.util.MapUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class StoreMenuDisplayService {

    @Resource
    private StoreMenuDisplayDAO storeMenuDisplayDAO;

    @Resource
    private StoreMenuDisplayCatDAO storeMenuDisplayCatDAO;

    @Resource
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Resource
    private MenuServiceUtil menuServiceUtil;

    @Resource
    private StoreChargeItemDAO storeChargeItemDAO;

    @Resource
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Resource
    private StoreDateBizSettingDAO storeDateBizSettingDAO;

    @Resource
    private StoreDateTimeBucketSettingDAO storeDateTimeBucketSettingDAO;

    @Resource
    private StoreChargeItemPromotionDAO storeChargeItemPromotionDAO;

    public List<StoreMenuDisplayCat> getStoreMenuDisplayCatsByTimeBucketId(int merchantId, long storeId, long timeBucketId) {
        return this.storeMenuDisplayCatDAO.getListByTimeBucketId(merchantId, storeId, timeBucketId, true, true);
    }

    public StoreMenuDisplayQueryResult getStoreMenu(StoreMenuQueryParam storeMenuQueryParam) {
        int merchantId = storeMenuQueryParam.getMerchantId();
        long storeId = storeMenuQueryParam.getStoreId();
        long timeBucketId = storeMenuQueryParam.getTimeBucketId();
        StoreDateBizSetting storeDateBizSetting = storeMenuQueryParam.getStoreDateBizSetting();
        long time = storeMenuQueryParam.getDate();
        List<StoreMenuDisplay> storeMenuDisplays = this.storeMenuDisplayDAO.getListByTimeBucketId(merchantId, storeId, timeBucketId, true, true);

        int weekDay = StoreDateBizSetting.getWeekDay(storeDateBizSetting, time);
        List<Long> chargeItemIds = this.storeChargeItemWeekDAO.getChargeItemIdsForWeekDay(merchantId, storeId,
                timeBucketId, weekDay, time, true, true);

        Map<Long, StoreChargeItem> storeChargeItemMap = this.storeChargeItemDAO.getMapInIds(merchantId, storeId, chargeItemIds, true, true);

        this.filterStoreChargeItemMap(storeChargeItemMap, storeMenuQueryParam);

        if (storeMenuQueryParam.isLoadAvailablePromotion()) {
            Map<Long, StoreChargeItemPromotion> map = this.storeChargeItemPromotionDAO.getMapInIds(merchantId, storeId, chargeItemIds);
            for (Map.Entry<Long, StoreChargeItem> entry : storeChargeItemMap.entrySet()) {
                StoreChargeItem item = entry.getValue();
                StoreChargeItemPromotion promotion = map.get(item.getChargeItemId());
                if (promotion != null && promotion.isInAvailable4Time(time)) {
                    item.setStoreChargeItemPromotion(promotion);
                }
            }
        }

        this.filterStoreMenuDisplays(storeMenuDisplays, storeChargeItemMap);
        List<StoreChargeItem> storeChargeItems = new ArrayList<>(storeChargeItemMap.values());
        this.menuServiceUtil.buildSubitems(merchantId, storeId, storeChargeItems, chargeItemIds, true, true, true);
        this.menuServiceUtil.buildPriceForDate(storeChargeItems, merchantId, storeId, chargeItemIds, time, true, true);
        StoreMenuDisplayQueryResult storeMenuDisplayQueryResult = new StoreMenuDisplayQueryResult();
        storeMenuDisplayQueryResult.setStoreChargeItemMap(storeChargeItemMap);
        storeMenuDisplayQueryResult.setStoreMenuDisplays(storeMenuDisplays);
        List<Long> catIds = Lists.newArrayList();
        for (StoreMenuDisplay storeMenuDisplay : storeMenuDisplays) {
            if (catIds.contains(storeMenuDisplay.getDisplayCatId()) || storeMenuDisplay.getDisplayCatId() == 0) {
                continue;
            }
            catIds.add(storeMenuDisplay.getDisplayCatId());
        }
        List<StoreMenuDisplayCat> catList = this.buildStoreMenuDisplayCats(storeMenuDisplays, merchantId, storeId, catIds);
        storeMenuDisplayQueryResult.setStoreMenuDisplayCats(catList);
        return storeMenuDisplayQueryResult;
    }

    private void filterStoreChargeItemMap(Map<Long, StoreChargeItem>
                                                  storeChargeItemMap,
                                          StoreMenuQueryParam
                                                  storeMenuQueryParam) {
        //如果是收银台,就不需要过滤,只需要显示所有
        if (storeMenuQueryParam.getClientType() == ClientTypeEnum.CASHIER.getValue()) {
            return;
        }
        Set<Map.Entry<Long, StoreChargeItem>> set = storeChargeItemMap
                .entrySet();
        Iterator<Map.Entry<Long, StoreChargeItem>> it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<Long, StoreChargeItem> e = it.next();
            StoreChargeItem item = e.getValue();
            //非收银台都是用户下单,需要判断收费项目是否支持用户下单
            if (!item.isEnableUserOrder()) {
                it.remove();
                continue;
            }
            //目前用户下单只能通过微信,因此如果不支持微信,就不显示
            if (storeMenuQueryParam.isForWechat() && !item.isEnableWechat()) {
                it.remove();
                continue;
            }
            if (storeMenuQueryParam.isForDelivery() && !item.isEnableDelivery()) {
                it.remove();
                continue;
            }
            if (storeMenuQueryParam.isForEatIn() && !item.isEnableDineIn()) {
                it.remove();
                continue;
            }
            if (storeMenuQueryParam.isForUserTake() && !item.isEnableUserTake()) {
                it.remove();
            }
        }
    }

    /**
     * 去除没有查询到的收费项目
     */
    private void filterStoreMenuDisplays(List<StoreMenuDisplay> storeMenuDisplays, Map<Long, StoreChargeItem> storeChargeItemMap) {
        Iterator<StoreMenuDisplay> it = storeMenuDisplays.iterator();
        while (it.hasNext()) {
            StoreMenuDisplay storeMenuDisplay = it.next();
            StoreChargeItem storeChargeItem = storeChargeItemMap.get(storeMenuDisplay.getChargeItemId());
            if (storeChargeItem == null) {
                it.remove();
            }
        }
    }

    private List<StoreMenuDisplayCat> buildStoreMenuDisplayCats(List<StoreMenuDisplay> storeMenuDisplays, int merchantId, long storeId, List<Long> catIds) {
        Map<Long, StoreMenuDisplayCat> catMap = this.storeMenuDisplayCatDAO.getMapInIds(merchantId, storeId, catIds, true, true);
        for (StoreMenuDisplay storeMenuDisplay : storeMenuDisplays) {
            StoreMenuDisplayCat cat = catMap.get(storeMenuDisplay.getDisplayCatId());
            if (cat != null) {
                storeMenuDisplay.setStoreMenuDisplayCat(cat);
            }
        }
        MapUtil.removeNullValue(catMap);
        List<StoreMenuDisplayCat> catList = Lists.newArrayList();
        catList.addAll(catMap.values());
        return catList;
    }

    public List<StoreDateTimeBucket> getStoreDateTimeBucketByDate(int merchantId,
                                                                  long storeId,
                                                                  long date,
                                                                  int addDays,
                                                                  boolean
                                                                          enablePreSell)
            throws TException {
        boolean enableSlave = true;
        //表示从当天开始，一共要获取的天数
        int totalDays = addDays + 1;
        int size = 7;//周期为7天，使用周期最大天数，进行周期判断
        MutableDateTime mdt = new MutableDateTime(date);
        mdt.addDays(size);
        long maxTime = mdt.getMillis();
        long endTime = DateUtil.getEndTime(maxTime, null);

        //获得几天之内的所有特殊日期设置、营业时段设置
        Map<Long, StoreDateBizSetting> bizSettingMap = this
                .storeDateBizSettingDAO.getMapForSelectedDateRange(merchantId,
                        storeId, date, maxTime);

        Map<String, StoreDateTimeBucketSetting> bucketSettingMap = this
                .storeDateTimeBucketSettingDAO.getMapForDateRange(merchantId,
                        storeId, date, maxTime);

        //开始时间<=endTime，结束时间>=date
        Set<Integer> weekDaySet = this.storeChargeItemWeekDAO
                .getWeekDaysForTime(merchantId, storeId, endTime, date);

        //添加指定日期的星期[X]
        for (Map.Entry<Long, StoreDateBizSetting> entry : bizSettingMap.entrySet()) {
            StoreDateBizSetting setting = entry.getValue();
            if (!setting.isPaused()) {
                weekDaySet.add(setting.getSelectedDateWeekDay());
            }
        }

        List<Long> menuDates = new ArrayList<>();
        StoreDateBizSetting dateSetting = bizSettingMap.get(date);
        if (!StoreDateBizSetting.isPaused(dateSetting)) {
            menuDates.add(date);
        }
        for (int i = 1; i <= size; i++) {
            //需要获取的后面几天的日期
            MutableDateTime menuDate = new MutableDateTime(date);
            menuDate.addDays(i);
            StoreDateBizSetting setting = bizSettingMap.get(menuDate.getMillis());
            //过滤暂停营业的
            if (StoreDateBizSetting.isPaused(setting)) {
                continue;
            }
            int menuWeekDay = menuDate.getDayOfWeek();
            //判断所选的日期是否符合有效期内的星期[X]
            if (weekDaySet.contains(menuWeekDay)) {
                menuDates.add(menuDate.getMillis());
            }
            if (menuDates.size() >= totalDays) {
                break;
            }
        }

        Set<Long> dateTimeBucketIdSet = new HashSet<>();
        Map<Long, List<Long>> tmap = Maps.newHashMap();
        //获取指定日期存在的营业时段
        for (Long time : menuDates) {
            StoreDateBizSetting setting = bizSettingMap.get(time);
            int weekDay = StoreDateBizSetting.getWeekDay(setting, time);
            List<Long> timeBucketIds = this.storeChargeItemWeekDAO
                    .getTimeBucketIdsForDate(merchantId, storeId, time, weekDay);
            dateTimeBucketIdSet.addAll(timeBucketIds);
            tmap.put(time, timeBucketIds);
        }

        //获取营业时段
        List<Long> dateTimeBucketIds = new ArrayList<>(dateTimeBucketIdSet);
        Map<Long, StoreTimeBucket> storeTimeBucketMap = this.storeTimeBucketDAO
                .getMapInIds(merchantId, storeId, dateTimeBucketIds, enableSlave);

        long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);

        //删除日期中无效的营业时段(已删除、暂停营业)
        Iterator<Long> menuDatesIterator = menuDates.iterator();
        while (menuDatesIterator.hasNext()) {
            long time = menuDatesIterator.next();
            List<Long> timeBucketIds = tmap.get(time);
            Iterator<Long> timeBucketIdIterator = timeBucketIds.iterator();
            while (timeBucketIdIterator.hasNext()) {
                long timeBucketId = timeBucketIdIterator.next();
                StoreTimeBucket storeTimeBucket = storeTimeBucketMap.get
                        (timeBucketId);
                if (storeTimeBucket == null) {
                    timeBucketIdIterator.remove();
                    continue;
                }
                //未来的日期，不显示已经删除的营业时段
                if (time >= today && storeTimeBucket.isDeleted()) {
                    timeBucketIdIterator.remove();
                    continue;
                }
                //已经暂停营业的不显示
                StoreDateTimeBucketSetting storeDateTimeBucketSetting
                        = StoreDateTimeBucketSetting
                        .getStoreDateTimeBucketSetting(time, timeBucketId,
                                bucketSettingMap);
                if (StoreDateTimeBucketSetting.isPaused(storeDateTimeBucketSetting)) {
                    timeBucketIdIterator.remove();
                    continue;
                }
            }
        }

        Map<Long, List<StoreTimeBucket>> dateTimeBucketMap = new HashMap<>();
        for (Long time : menuDates) {
            List<Long> timeBucketIds = tmap.get(time);
            List<StoreTimeBucket> list = Lists.newArrayList();
            for (Long timeBucketId : timeBucketIds) {
                StoreTimeBucket storeTimeBucket = storeTimeBucketMap.get(timeBucketId);
                StoreTimeBucket copy = new StoreTimeBucket();
                BeanUtil.copy(storeTimeBucket, copy);
                if (today == time) {
                    copy.setTestInBizTime(true, today);
                    if (copy.isAfterBizTime(time, System.currentTimeMillis())) {
                        //已经超过了此营业时段
                        continue;
                    }
                }
                list.add(copy);
            }
            dateTimeBucketMap.put(time, list);
        }

        List<StoreDateTimeBucket> storeDateTimeBuckets = new ArrayList<>();
        for (Long time : menuDates) {
            StoreDateTimeBucket storeDateTimeBucket = new StoreDateTimeBucket();
            storeDateTimeBucket.setTime(time);
            storeDateTimeBucket.setStoreTimeBuckets(dateTimeBucketMap.get(time));
            StoreTimeBucketUtil.sortTimeBuckets(storeDateTimeBucket.getStoreTimeBuckets());
            storeDateTimeBuckets.add(storeDateTimeBucket);
        }
        Iterator<StoreDateTimeBucket>
                storeDateTimeBucketIterator = storeDateTimeBuckets.iterator();
        while (storeDateTimeBucketIterator.hasNext()) {
            StoreDateTimeBucket
                    storeDateTimeBucket = storeDateTimeBucketIterator.next();
            if (storeDateTimeBucket.getStoreTimeBucketsSize() == 0) {
                storeDateTimeBucketIterator.remove();
            }
        }
        return storeDateTimeBuckets;
    }

    @Transactional(rollbackFor = Exception.class)
    public void testLock(int merchantId, long storeId, long catId) {
        StoreMenuDisplayCat cat = this.storeMenuDisplayCatDAO.getByIdForUpdate(merchantId, storeId, catId, true);
        cat.setName("akweiwei");
        cat.update();

        StoreMenuDisplayCat cat0 = this.storeMenuDisplayCatDAO.getByIdForUpdate(merchantId, storeId, catId, false);
        cat0.setName("okokok");
        cat0.update();
    }

    public List<StoreMenuDisplay> getStoreMenuDisplays(int merchantId, long storeId, long timeBucketId) {
        return this.storeMenuDisplayDAO.getListByTimeBucketId(merchantId,
                storeId, timeBucketId, true, true);
    }
}
