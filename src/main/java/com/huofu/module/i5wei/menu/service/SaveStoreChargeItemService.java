package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.menu.dao.*;
import com.huofu.module.i5wei.menu.entity.*;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by akwei on 7/19/15.
 */
@Service
public class SaveStoreChargeItemService {

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private StoreChargeSubitemDAO storeChargeSubitemDAO;

    @Autowired
    private StoreChargeItemPriceDAO storeChargeItemPriceDAO;

    @Autowired
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Autowired
    private MenuServiceUtil menuServiceUtil;

    @Autowired
    private StoreMenuDisplayDAO storeMenuDisplayDAO;

    @Autowired
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDAO;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private StoreProductDAO storeProductDAO;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreChargeItem createStoreChargeItem(SaveStoreChargeItemParam param, String headImg, Store5weiSetting store5weiSetting, long now, StoreTimeBucket curOverDayTimeBucket)
            throws T5weiException {
        StoreChargeItem storeChargeItem = new StoreChargeItem();
        BeanUtil.copy(param, storeChargeItem, true);
        if (!store5weiSetting.isEnableUserTake()) {
            storeChargeItem.setQuickTake(false);
        } else if (!store5weiSetting.isQuickTakeSupport()) {
            storeChargeItem.setQuickTake(false);
        }
        this.storeChargeItemDAO
                .checkDuplicate(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(),
                        storeChargeItem.getName());
        storeChargeItem.checkOfflineNotifyTime(now, 0);
        storeChargeItem.initForCreate(now);
        long newDishesEndTime = this.getNewDishesEndTime();// 获取菜品“新品”有效期截止时间
        // 如果“新品”开关 开启了
        if (param.isNewDishesEnable()) {
            storeChargeItem.setNewDishesEndTime(newDishesEndTime);
        } else {
            storeChargeItem.setNewDishesEndTime(0L);
        }

        // 设置收费项目的成本设置情况和分类 add by lixuwei 2016-07-12
        setChargeItemPrimeCostSetAndCategory(param, storeChargeItem);
        if (DataUtil.isNotEmpty(headImg)) {
            storeChargeItem.setHeadImg(headImg);
        }
        if (param.isClearImg()) {
            storeChargeItem.setHeadImg("");
        }
        storeChargeItem.create();

        this._processItemWeeksForCreate(storeChargeItem, param, now, curOverDayTimeBucket);
        this._processItemPricesForCreate(storeChargeItem, param, now, curOverDayTimeBucket);
        this._processSubItemsForCreate(storeChargeItem, param, now);
        this.updateStoreProductUnit(storeChargeItem);
        return storeChargeItem;
    }

    /**
     * 为收费项目设置 '成本设置' 状态 和 分类
     *
     * @param param
     * @return
     */
    private void setChargeItemPrimeCostSetAndCategory(SaveStoreChargeItemParam param, StoreChargeItem storeChargeItem) {

        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();

        List<SaveStoreChargeSubitemParam> subitemParams = param.getSubitemParams();
        if (subitemParams == null || subitemParams.isEmpty()) {
            return;
        }
        List<Long> productIds = Lists.newArrayList();
        for (SaveStoreChargeSubitemParam subitemParam : subitemParams) {
            productIds.add(subitemParam.getProductId());
        }
        List<StoreProduct> products = storeProductDAO.getListInIds(merchantId, storeId, productIds);
        // 如果该定价是个单品 那么就设置与该定价下菜品同样的分类
        if (products.size() == 1) {
            storeChargeItem.setCategoryId(products.get(0).getCategoryId());
        }
        // 设置收费项目的成本设置状态
        storeChargeItem.setPrimeCostSet(updatePrimeCostSet(products));
    }

    private int updatePrimeCostSet(List<StoreProduct> products) {
        int mark = 0;
        for (StoreProduct product : products) {
            if (product.isPrimeCostSet()) {
                mark += 1;
            }
        }
        if (mark == 0) {
            return ChargeItemPrimeCostSetEnum.NO_SET.getValue();
        } else if (products.size() == mark) {
            return ChargeItemPrimeCostSetEnum.ALL_SET.getValue();
        } else if (products.size() > mark) {
            return ChargeItemPrimeCostSetEnum.PART_SET.getValue();
        }
        return ChargeItemPrimeCostSetEnum.NO_SET.getValue();
    }

    @Transactional(rollbackFor = Exception.class)
    public StoreChargeItem updateStoreChargeItem(SaveStoreChargeItemParam param, String headImg, long now, StoreTimeBucket curOverDayTimeBucket) throws TException {
        this.storeChargeItemDAO.checkDuplicate(param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), param.getName());
        StoreChargeItem storeChargeItem = this.storeChargeItemDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), true, true);
        long oldPortId = storeChargeItem.getPortId();
        long oldOfflineNotifyTime = storeChargeItem.getOfflineNotifyTime();
        storeChargeItem.snapshot();
        BeanUtil.copy(param, storeChargeItem, true);
        long newDishesEndTime = this.getNewDishesEndTime();// 获取菜品“新品”有效期截止时间
        // 如果“新品”关闭了，则将“新品结束时间”设置为0
        if (!param.isNewDishesEnable()) {
            storeChargeItem.setNewDishesEndTime(0L);
        } else {
            // 如果当前是“关闭”状态，则需要设置新品结束时间，如果当前是“开启”状态，则不需要再次设置新品结束时间
            if (!storeChargeItem.isNewDishesEnable()) {
                storeChargeItem.setNewDishesEndTime(newDishesEndTime);
            }
        }
        storeChargeItem.checkOfflineNotifyTime(now, oldOfflineNotifyTime);
        storeChargeItem.setUpdateTime(now);
        // 设置收费项目成本的设置状态
        setChargeItemPrimeCostSetAndCategory(param, storeChargeItem);
        if (DataUtil.isNotEmpty(headImg)) {
            storeChargeItem.setHeadImg(headImg);
        }
        if (param.isClearImg()) {
            storeChargeItem.setHeadImg("");
        }
        storeChargeItem.update();
        this.updateStoreChargeSubitems(storeChargeItem, param, now);
        this._processItemWeeksForUpdate(storeChargeItem, param, now, curOverDayTimeBucket);
        this._processPriceForUpdate(storeChargeItem, param, now, curOverDayTimeBucket);
        this.updateMenuDisplayForAllTimeBucket(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId());
        storeChargeItem.sortItemWeeks();
        // update portId
        if (oldPortId != param.getPortId()) {
            boolean hasPackagePort = false;
            StoreMealPort storeMealPort = this.storeMealPortDAO.getParkagePort(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), false);
            if (storeMealPort != null) {
                hasPackagePort = true;
            }
            this.storeMealTakeupDAO.updateProductPort(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(), param.getPortId(), hasPackagePort);
        }
        this.updateStoreProductUnit(storeChargeItem);
        return storeChargeItem;
    }

    public StoreChargeItem saveStoreChargeItemSimple(SaveStoreChargeItemSimpleParam param) throws T5weiException, TException {
        if (DataUtil.isNotEmpty(param.getName())) {
            this.storeChargeItemDAO.checkDuplicate(param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), param.getName());
        }
        StoreChargeItem storeChargeItem = this.storeChargeItemDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), true, true);
        BeanUtil.copy(param, storeChargeItem, true);
        storeChargeItem.update();
        return storeChargeItem;
    }

    private void _processItemWeeksForCreate(StoreChargeItem storeChargeItem, SaveStoreChargeItemParam param, long time, StoreTimeBucket curOverDayTimeBucket) {
        long chargeItemId = storeChargeItem.getChargeItemId();
        int merchantId = storeChargeItem.getMerchantId();
        long storeId = storeChargeItem.getStoreId();
        List<StoreChargeItemWeek> curStoreChargeItemWeeks = Lists.newArrayList();
        if (param.getCurItemWeekParamsSize() > 0) {
            for (StoreChargeItemWeekParam itemWeekParam : param.getCurItemWeekParams()) {
                StoreChargeItemWeek storeChargeItemWeek = new StoreChargeItemWeek();
                storeChargeItemWeek.setChargeItemId(chargeItemId);
                storeChargeItemWeek.setMerchantId(merchantId);
                storeChargeItemWeek.setStoreId(storeId);
                storeChargeItemWeek.setWeekDay(itemWeekParam.getWeekDay());
                storeChargeItemWeek.setTimeBucketId(itemWeekParam.getTimeBucketId());
                curStoreChargeItemWeeks.add(storeChargeItemWeek);
            }
        }

        List<StoreChargeItemWeek> nextWeekStoreChargeItemWeeks = Lists.newArrayList();
        if (param.getNextWeekItemWeekParamsSize() > 0) {
            for (StoreChargeItemWeekParam itemWeekParam : param.getNextWeekItemWeekParams()) {
                StoreChargeItemWeek storeChargeItemWeek = new StoreChargeItemWeek();
                storeChargeItemWeek.setChargeItemId(chargeItemId);
                storeChargeItemWeek.setMerchantId(merchantId);
                storeChargeItemWeek.setStoreId(storeId);
                storeChargeItemWeek.setWeekDay(itemWeekParam.getWeekDay());
                storeChargeItemWeek.setTimeBucketId(itemWeekParam.getTimeBucketId());
                nextWeekStoreChargeItemWeeks.add(storeChargeItemWeek);
            }
        }
        // 设置本周周期设置，生效时间为当天的开始时间
        long today = DateUtil.getBeginTime(time, null);
        if (curOverDayTimeBucket != null && curOverDayTimeBucket.isOverDayNextPart(time)) {
            MutableDateTime mdt1 = new MutableDateTime(today);
            mdt1.addDays(-1);
            today = DateUtil.getBeginTime(mdt1.getMillis(), null);
        }
        long endTime;
        // 如果没有下周的周期设置，有效期结束时间为无限大，否则就是本周周日的23:59:59.000
        if (nextWeekStoreChargeItemWeeks.isEmpty()) {
            endTime = Long.MAX_VALUE;
        } else {
            endTime = getLastDayEndTimeInWeek(time, false);
        }
        List<StoreChargeItemWeek> list = Lists.newArrayList();
        for (StoreChargeItemWeek storeChargeItemWeek : curStoreChargeItemWeeks) {
            storeChargeItemWeek.setDeleted(false);
            storeChargeItemWeek.setBeginTime(today);
            storeChargeItemWeek.setEndTime(endTime);
            storeChargeItemWeek.setCreateTime(time);
            storeChargeItemWeek.setChargeItemId(chargeItemId);
        }
        list.addAll(curStoreChargeItemWeeks);
        if (!nextWeekStoreChargeItemWeeks.isEmpty()) {
            long nextWeekBeginTime = getFirstDayBeginTimeInWeek(time, true);
            long nextWeekEndTime = Long.MAX_VALUE;
            // 设置下周周期设置
            for (StoreChargeItemWeek storeChargeItemWeek : nextWeekStoreChargeItemWeeks) {
                storeChargeItemWeek.setDeleted(false);
                storeChargeItemWeek.setBeginTime(nextWeekBeginTime);
                storeChargeItemWeek.setEndTime(nextWeekEndTime);
                storeChargeItemWeek.setCreateTime(time);
                storeChargeItemWeek.setChargeItemId(chargeItemId);
            }
            list.addAll(nextWeekStoreChargeItemWeeks);
        }
        if (!list.isEmpty()) {
            this.storeChargeItemWeekDAO.batchCreate(list, null);
        }
        storeChargeItem.setCurStoreChargeItemWeeks(curStoreChargeItemWeeks);
        storeChargeItem.setNextWeekStoreChargeItemWeeks(nextWeekStoreChargeItemWeeks);
        storeChargeItem.sortItemWeeks();
    }

    private void _processItemPricesForCreate(StoreChargeItem storeChargeItem, SaveStoreChargeItemParam param, long now, StoreTimeBucket curOverDayTimeBucket) throws T5weiException {
        List<StoreChargeItemPrice> list = Lists.newArrayList();
        long todayBeginTime = DateUtil.getBeginTime(now, null);
        if (curOverDayTimeBucket != null && curOverDayTimeBucket.isOverDayNextPart(now)) {
            MutableDateTime mdt1 = new MutableDateTime(todayBeginTime);
            mdt1.addDays(-1);
            todayBeginTime = DateUtil.getBeginTime(mdt1.getMillis(), null);
        }

        int merchantId = storeChargeItem.getMerchantId();
        long storeId = storeChargeItem.getStoreId();

        StoreChargeItemPrice curStoreChargeItemPrice = new StoreChargeItemPrice();
        curStoreChargeItemPrice.setMerchantId(merchantId);
        curStoreChargeItemPrice.setStoreId(storeId);
        curStoreChargeItemPrice.setBeginTime(todayBeginTime);
        boolean hasNextPrice = false;
        long nextPriceBeginTime = 0;
        if (param.isSetNextPrice() && param.getNextPrice() >= 0) {
            hasNextPrice = true;
        }
        long curEndTime;
        if (hasNextPrice) {
            nextPriceBeginTime = param.getNextPriceBeginTime();
            MutableDateTime mdt = new MutableDateTime(nextPriceBeginTime);
            mdt.setHourOfDay(23);
            mdt.setMinuteOfHour(59);
            mdt.setSecondOfMinute(59);
            mdt.setMillisOfSecond(999);
            mdt.addDays(-1);
            curEndTime = mdt.getMillis();
        } else {
            curEndTime = Long.MAX_VALUE;
        }
        curStoreChargeItemPrice.setEndTime(curEndTime);
        curStoreChargeItemPrice.setPrice(param.getCurPrice());
        curStoreChargeItemPrice.setChargeItemId(storeChargeItem.getChargeItemId());
        curStoreChargeItemPrice.initForCreate(now);
        list.add(curStoreChargeItemPrice);
        StoreChargeItemPrice nextStoreChargeItemPrice = null;
        if (hasNextPrice) {
            nextStoreChargeItemPrice = new StoreChargeItemPrice();
            nextStoreChargeItemPrice.setMerchantId(merchantId);
            nextStoreChargeItemPrice.setStoreId(storeId);
            nextStoreChargeItemPrice.setBeginTime(nextPriceBeginTime);
            nextStoreChargeItemPrice.setEndTime(Long.MAX_VALUE);
            nextStoreChargeItemPrice.setPrice(param.getNextPrice());
            nextStoreChargeItemPrice.setChargeItemId(storeChargeItem.getChargeItemId());
            nextStoreChargeItemPrice.initForCreate(now);
            list.add(nextStoreChargeItemPrice);
        }
        this.storeChargeItemPriceDAO.batchCreate(list, null);
        storeChargeItem.setCurStoreChargeItemPrice(curStoreChargeItemPrice);
        if (nextStoreChargeItemPrice != null) {
            storeChargeItem.setNextStoreChargeItemPrice(nextStoreChargeItemPrice);
        }
    }

    private void _processSubItemsForCreate(StoreChargeItem storeChargeItem, SaveStoreChargeItemParam param, long now) {
        List<StoreChargeSubitem> storeChargeSubitems = Lists.newArrayList();
        for (SaveStoreChargeSubitemParam subitemParam : param.getSubitemParams()) {
            StoreChargeSubitem storeChargeSubitem = new StoreChargeSubitem();
            storeChargeSubitem.setChargeItemId(storeChargeItem.getChargeItemId());
            storeChargeSubitem.setProductId(subitemParam.getProductId());
            storeChargeSubitem.setAmount(subitemParam.getAmount());
            storeChargeSubitem.setMerchantId(storeChargeItem.getMerchantId());
            storeChargeSubitem.setStoreId(storeChargeItem.getStoreId());
            storeChargeSubitem.initForCreate(now);
            storeChargeSubitems.add(storeChargeSubitem);
        }
        this.menuServiceUtil.checkSubitem(storeChargeSubitems);
        this.storeChargeSubitemDAO.batchCreate(storeChargeSubitems);
        storeChargeItem.setStoreChargeSubitems(storeChargeSubitems);
    }

    /**
     * 获得有效期的开始时间,如果是本周，开始时间为当天，如果是下周，开始时间为下周一
     *
     * @param time     当前时间
     * @param nextWeek 是否是下周
     * @return 开始时间，指定日期的00:00:00.000
     */
    private static long getFirstDayBeginTimeInWeek(long time, boolean nextWeek) {
        MutableDateTime mdt = new MutableDateTime(time);
        if (nextWeek) {
            mdt.setDayOfWeek(1);
            mdt.addWeeks(1);
        }
        mdt.setHourOfDay(0);
        mdt.setMinuteOfDay(0);
        mdt.setSecondOfDay(0);
        mdt.setMillisOfDay(0);
        return mdt.getMillis();
    }

    private static long getLastDayEndTimeInWeek(long time, boolean nextWeek) {
        MutableDateTime mdt = new MutableDateTime(time);
        mdt.setDayOfWeek(7);
        if (nextWeek) {
            mdt.addWeeks(1);
        }
        mdt.setHourOfDay(23);
        mdt.setMinuteOfHour(59);
        mdt.setSecondOfMinute(59);
        mdt.setMillisOfSecond(999);
        return mdt.getMillis();
    }

    private void updateStoreChargeSubitems(StoreChargeItem storeChargeItem, SaveStoreChargeItemParam param, long now) {
        long chargeItemId = param.getChargeItemId();
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        List<StoreChargeSubitem> storeChargeSubitems = Lists.newArrayList();
        for (SaveStoreChargeSubitemParam subitemParam : param.getSubitemParams()) {
            StoreChargeSubitem storeChargeSubitem = new StoreChargeSubitem();
            storeChargeSubitem.setChargeItemId(chargeItemId);
            storeChargeSubitem.setProductId(subitemParam.getProductId());
            storeChargeSubitem.setAmount(subitemParam.getAmount());
            storeChargeSubitem.setMerchantId(merchantId);
            storeChargeSubitem.setStoreId(storeId);
            storeChargeSubitems.add(storeChargeSubitem);
        }
        if (storeChargeSubitems.isEmpty()) {
            return;
        }
        for (StoreChargeSubitem storeChargeSubitem : storeChargeSubitems) {
            storeChargeSubitem.initForCreate(now);
        }
        this.menuServiceUtil.checkSubitem(storeChargeSubitems);
        this.storeChargeSubitemDAO.deleteByChargeItemId(merchantId, storeId, chargeItemId);
        this.storeChargeSubitemDAO.batchCreate(storeChargeSubitems);
        storeChargeItem.setStoreChargeSubitems(storeChargeSubitems);
    }

    private void _processItemWeeksForUpdate(StoreChargeItem storeChargeItem, SaveStoreChargeItemParam param, long now, StoreTimeBucket curOverDayTimeBucket) {
        int merchantId = storeChargeItem.getMerchantId();
        long storeId = storeChargeItem.getStoreId();
        long chargeItemId = storeChargeItem.getChargeItemId();
        List<StoreChargeItemWeek> curStoreChargeItemWeeks = Lists.newArrayList();
        if (param.getCurItemWeekParams() != null) {
            for (StoreChargeItemWeekParam itemWeekParam : param.getCurItemWeekParams()) {
                StoreChargeItemWeek storeChargeItemWeek = new StoreChargeItemWeek();
                storeChargeItemWeek.setChargeItemId(chargeItemId);
                storeChargeItemWeek.setMerchantId(merchantId);
                storeChargeItemWeek.setStoreId(storeId);
                storeChargeItemWeek.setWeekDay(itemWeekParam.getWeekDay());
                storeChargeItemWeek.setTimeBucketId(itemWeekParam.getTimeBucketId());
                curStoreChargeItemWeeks.add(storeChargeItemWeek);
            }
        }
        List<StoreChargeItemWeek> nextWeekStoreChargeItemWeeks = Lists.newArrayList();
        if (param.getItemMode() == ChargeItemModeEnum.MODE_SUPER.getValue() && param.getNextWeekItemWeekParams() != null) {
            for (StoreChargeItemWeekParam itemWeekParam : param.getNextWeekItemWeekParams()) {
                StoreChargeItemWeek storeChargeItemWeek = new StoreChargeItemWeek();
                storeChargeItemWeek.setChargeItemId(chargeItemId);
                storeChargeItemWeek.setMerchantId(merchantId);
                storeChargeItemWeek.setStoreId(storeId);
                storeChargeItemWeek.setWeekDay(itemWeekParam.getWeekDay());
                storeChargeItemWeek.setTimeBucketId(itemWeekParam.getTimeBucketId());
                nextWeekStoreChargeItemWeeks.add(storeChargeItemWeek);
            }
        }
        long todayBeginTime = DateUtil.getBeginTime(now, null);
        if (curOverDayTimeBucket != null && curOverDayTimeBucket.isOverDayNextPart(now)) {
            MutableDateTime mdt1 = new MutableDateTime(todayBeginTime);
            mdt1.addDays(-1);
            todayBeginTime = DateUtil.getBeginTime(mdt1.getMillis(), null);
        }
        long todayEndTime = DateUtil.getEndTime(todayBeginTime, null);

        MutableDateTime mdt = new MutableDateTime(todayEndTime);
        mdt.addDays(-1);
        long yesEndTime = mdt.getMillis();

        // 周期设置数据中，以前的生效数据有效期为 yesEndTime
        this.storeChargeItemWeekDAO.updateEndTimeForValid(merchantId, storeId, chargeItemId, todayBeginTime, todayEndTime, yesEndTime);
        // 删除今天创建的有效数据
        this.storeChargeItemWeekDAO.deleteForFuture(merchantId, storeId, chargeItemId, todayBeginTime);
        List<StoreChargeItemWeek> list = Lists.newArrayList();
        if (!curStoreChargeItemWeeks.isEmpty()) {
            long endTime;
            if (param.itemMode == ChargeItemModeEnum.MODE_NORMAL.getValue()) {
                endTime = Long.MAX_VALUE;
            } else {
                endTime = getLastDayEndTimeInWeek(now, false);
            }
            for (StoreChargeItemWeek storeChargeItemWeek : curStoreChargeItemWeeks) {
                storeChargeItemWeek.setDeleted(false);
                storeChargeItemWeek.setBeginTime(todayBeginTime);
                storeChargeItemWeek.setEndTime(endTime);
                storeChargeItemWeek.setCreateTime(now);
                storeChargeItemWeek.setChargeItemId(chargeItemId);
            }
            list.addAll(curStoreChargeItemWeeks);
        }
        if (param.itemMode == ChargeItemModeEnum.MODE_SUPER.getValue() && !nextWeekStoreChargeItemWeeks.isEmpty()) {
            long nextWeekBeginTime = getFirstDayBeginTimeInWeek(now, true);
            long nextWeekEndTime = Long.MAX_VALUE;
            // 设置下周周期设置
            for (StoreChargeItemWeek storeChargeItemWeek : nextWeekStoreChargeItemWeeks) {
                storeChargeItemWeek.setDeleted(false);
                storeChargeItemWeek.setBeginTime(nextWeekBeginTime);
                storeChargeItemWeek.setEndTime(nextWeekEndTime);
                storeChargeItemWeek.setCreateTime(now);
                storeChargeItemWeek.setChargeItemId(chargeItemId);
            }
            list.addAll(nextWeekStoreChargeItemWeeks);
        }
        if (!list.isEmpty()) {
            this.storeChargeItemWeekDAO.batchCreate(list, null);
        }
        storeChargeItem.setCurStoreChargeItemWeeks(curStoreChargeItemWeeks);
        storeChargeItem.setNextWeekStoreChargeItemWeeks(nextWeekStoreChargeItemWeeks);
        storeChargeItem.sortItemWeeks();
    }

    private void _processPriceForUpdate(StoreChargeItem storeChargeItem, SaveStoreChargeItemParam param, long now, StoreTimeBucket curOverDayTimeBucket) {
        int merchantId = storeChargeItem.getMerchantId();
        long storeId = storeChargeItem.getStoreId();
        long chargeItemId = storeChargeItem.getChargeItemId();
        long todayBeginTime = DateUtil.getBeginTime(now, null);
        if (curOverDayTimeBucket != null && curOverDayTimeBucket.isOverDayNextPart(now)) {
            MutableDateTime mdt1 = new MutableDateTime(todayBeginTime);
            mdt1.addDays(-1);
            todayBeginTime = DateUtil.getBeginTime(mdt1.getMillis(), null);
        }

        long curEndTime;
        boolean hasNextPrice = false;
        long nextPriceBeginTime = 0;
        if (param.isSetNextPrice() && param.getNextPrice() >= 0) {
            hasNextPrice = true;
            nextPriceBeginTime = DateUtil.getBeginTime(param.getNextPriceBeginTime(), null);
        }

        if (hasNextPrice) {// 有未来价格，需要设置当前价格有效期为开始时间前一天
            MutableDateTime mdt = new MutableDateTime(nextPriceBeginTime);
            mdt.addDays(-1);
            mdt.setHourOfDay(23);
            mdt.setMinuteOfHour(59);
            mdt.setSecondOfMinute(59);
            mdt.setMillisOfSecond(999);
            curEndTime = mdt.getMillis();
        } else {
            curEndTime = Long.MAX_VALUE;
        }

        // 删除当天生效的
        this.storeChargeItemPriceDAO.deleteByChargeItemIdAndBeginTime(merchantId, storeId, chargeItemId, todayBeginTime);
        // 如果当前价格发生变化,更新生效时间早于今天的价格有效期为昨天最后的时刻
        this.storeChargeItemPriceDAO.updateEndTimeByChargeItemIdAndNotEqPriceInExpiryDate(merchantId, storeId, chargeItemId, param.getCurPrice(), todayBeginTime, todayBeginTime - 1);
        // 创建当天生效的价格
        StoreChargeItemPrice curStoreChargeItemPrice = new StoreChargeItemPrice();
        curStoreChargeItemPrice.setChargeItemId(storeChargeItem.getChargeItemId());
        curStoreChargeItemPrice.setMerchantId(storeChargeItem.getMerchantId());
        curStoreChargeItemPrice.setStoreId(storeChargeItem.getStoreId());
        curStoreChargeItemPrice.setPrice(param.getCurPrice());
        curStoreChargeItemPrice.setBeginTime(todayBeginTime);
        curStoreChargeItemPrice.setEndTime(curEndTime);
        curStoreChargeItemPrice.initForCreate(now);
        curStoreChargeItemPrice.create();
        storeChargeItem.setCurStoreChargeItemPrice(curStoreChargeItemPrice);
        this.storeChargeItemPriceDAO.deleteByChargeItemIdForFuture(merchantId, storeId, chargeItemId, todayBeginTime);
        if (hasNextPrice) {
            long _nextBeginTime = DateUtil.getBeginTime(nextPriceBeginTime, null);
            StoreChargeItemPrice nextStoreChargeItemPrice = new StoreChargeItemPrice();
            nextStoreChargeItemPrice.setChargeItemId(chargeItemId);
            nextStoreChargeItemPrice.setMerchantId(merchantId);
            nextStoreChargeItemPrice.setStoreId(storeId);
            nextStoreChargeItemPrice.setPrice(param.getNextPrice());
            nextStoreChargeItemPrice.setBeginTime(_nextBeginTime);
            nextStoreChargeItemPrice.setEndTime(Long.MAX_VALUE);
            nextStoreChargeItemPrice.initForCreate(now);
            nextStoreChargeItemPrice.create();
            storeChargeItem.setNextStoreChargeItemPrice(nextStoreChargeItemPrice);
        }
    }

    private void updateMenuDisplayForAllTimeBucket(int merchantId, long storeId, long chargeItemId) {
        List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, false, false);
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            this.updateMenuDisplay(merchantId, storeId, chargeItemId, storeTimeBucket.getTimeBucketId());
        }
    }

    private void updateMenuDisplay(int merchantId, long storeId, long chargeItemId, long timeBucketId) {
        int num = this.storeChargeItemWeekDAO.countByChargeItemIdAndTimeBucketId(merchantId, storeId, chargeItemId, timeBucketId);
        if (num == 0) {
            this.storeMenuDisplayDAO.deleteByChargeItemIdAndTimeBucketId(merchantId, storeId, chargeItemId, timeBucketId);
        }
    }

    /**
     * 获取菜品“新品”有效期截止时间
     */
    private long getNewDishesEndTime() {

        long now = System.currentTimeMillis();
        MutableDateTime newDishesEndTime = new MutableDateTime(now);
        newDishesEndTime.addDays(7);
        newDishesEndTime.setHourOfDay(23);
        newDishesEndTime.setMinuteOfHour(59);
        newDishesEndTime.setSecondOfMinute(59);
        newDishesEndTime.setMillisOfSecond(999);

        return newDishesEndTime.getMillis();
    }

    private void updateStoreProductUnit(StoreChargeItem storeChargeItem) {
        if (storeChargeItem.isWeightEnabled()) {
            if (storeChargeItem.getStoreChargeSubitems().size() > 0) {
                long productId = storeChargeItem.getStoreChargeSubitems().get(0).getProductId();
                this.storeProductDAO.updateStoreProductUnit(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), productId, storeChargeItem.getUnit());
            }
        }
    }
}
