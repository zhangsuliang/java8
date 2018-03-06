package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.menu.dao.*;
import com.huofu.module.i5wei.menu.entity.*;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDeliveryParam;
import huofucore.facade.i5wei.menu.StoreTimeBucketIdsParam;
import huofucore.facade.i5wei.menu.StoreTimeBucketParam;
import huofuhelper.util.DateUtil;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Created by akwei on 2/15/15.
 */
@Service
public class StoreTimeBucketService {

    @Autowired
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Autowired
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Autowired
    private StoreMenuDisplayDAO storeMenuDisplayDAO;

    @Autowired
    private StoreMenuDisplayCatDAO storeMenuDisplayCatDAO;

    @Autowired
    private StoreDateTimeBucketSettingDAO storeDateTimeBucketSettingDAO;

    @Autowired
    private StoreChargeItemWeekBackDAO storeChargeItemWeekBackDAO;

    @Autowired
    private StoreDateBizSettingDAO storeDateBizSettingDAO;

    @Autowired
    private StoreTvMenuDAO storeTvMenuDAO;

    private static final int pageSize = 500;

    /**
     * 一天的毫秒数
     */
    private static final long DAY_TIME_MILLIS = 86400000;

    /**
     * 创建营业时段。<br/>
     *
     * @param storeTimeBucket
     * @return
     * @throws T5weiException
     */
    public StoreTimeBucket createStoreTimeBucket(StoreTimeBucket storeTimeBucket) throws T5weiException {
        this.storeTimeBucketDAO.checkForDuplicate(storeTimeBucket.getMerchantId(), storeTimeBucket.getStoreId(), storeTimeBucket.getTimeBucketId(), storeTimeBucket.getStartTime(), storeTimeBucket.getEndTime(), storeTimeBucket.getName());
        storeTimeBucket.initForCreate(System.currentTimeMillis());
        storeTimeBucket.create();
        return storeTimeBucket;
    }


    /**
     * 批量创建营业时段
     */
    @Transactional(rollbackFor = Exception.class)
    public List<StoreTimeBucket> batchCreateStoreTimeBucket(List<StoreTimeBucketParam> params) throws T5weiException {
        List<StoreTimeBucket> storeTimeBuckets = new ArrayList<>();
        for (StoreTimeBucketParam param : params) {
            StoreTimeBucket storeTimeBucket = new StoreTimeBucket();
            BeanUtil.copy(param, storeTimeBucket);
            storeTimeBucket.initForCreate(System.currentTimeMillis());
            storeTimeBuckets.add(storeTimeBucket);
        }
        try {
            List<StoreTimeBucket> timeBuckets = storeTimeBucketDAO.batchCreate(storeTimeBuckets);
            return timeBuckets;
        } catch (DuplicateKeyException e) {
            throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_DUPLICATE.getValue(), "storeTimeBucket duplicate");
        }

    }

    /**
     * 更新营业时段信息
     *
     * @param param
     * @return
     * @throws T5weiException
     */
    public StoreTimeBucketSave updateStoreTimeBucket(StoreTimeBucketParam param) throws T5weiException {

        StoreTimeBucketSave storeTimeBucketSave = new StoreTimeBucketSave();
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getTimeBucketId(), false, true);
//        storeTimeBucket.snapshot();
        checkTableFeeChange(param, storeTimeBucketSave, storeTimeBucket);
        BeanUtil.copy(param, storeTimeBucket, true);
        this.storeTimeBucketDAO.checkForDuplicate(storeTimeBucket.getMerchantId(), storeTimeBucket.getStoreId(), storeTimeBucket.getTimeBucketId(), storeTimeBucket.getStartTime(), storeTimeBucket.getEndTime(), storeTimeBucket.getName());
        storeTimeBucket.setUpdateTime(System.currentTimeMillis());
//        storeTimeBucket.setDeleted(false);
        storeTimeBucket.update();

        storeTimeBucketSave.setStoreTimeBucket(storeTimeBucket);
        return storeTimeBucketSave;
    }

    /**
     * 判断台位费是否有改动
     *
     * @param param
     * @param storeTimeBucketSave
     * @param storeTimeBucket
     */
    public void checkTableFeeChange(StoreTimeBucketParam param, StoreTimeBucketSave storeTimeBucketSave, StoreTimeBucket storeTimeBucket) {
        if (param.isSetTableFee()) {
            if (storeTimeBucket != null && param.getTableFee() != storeTimeBucket.getTableFee()) {
                storeTimeBucketSave.setTableFeeUpdate(true);
            }
        }
    }

    public StoreTimeBucket saveStoreTimeBucketDelivery(StoreTimeBucketDeliveryParam storeTimeBucketDeliveryParam) throws T5weiException {
        int merchantId = storeTimeBucketDeliveryParam.getMerchantId();
        long storeId = storeTimeBucketDeliveryParam.getStoreId();
        long timeBucketId = storeTimeBucketDeliveryParam.getTimeBucketId();
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketDAO.loadById(merchantId, storeId, timeBucketId, false, true);
        BeanUtil.copy(storeTimeBucketDeliveryParam, storeTimeBucket);
        storeTimeBucket.refreshUpdateTime();
        storeTimeBucket.update();
        return storeTimeBucket;
    }

    /**
     * 删除营业时段。<br/>
     * 如果营业时段关联数据有效(周期、指定时间)就不允许删除
     * 如果营业时段以前有过关联，那么就设置删除标志true.如果没有，就直接删除
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param timeBucketId 营业时段id
     * @return true:删除成功,false:存在关联,不能删除
     * @throws T5weiException 无效的营业时段数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteStoreTimeBucket(int merchantId, long storeId, long timeBucketId) throws T5weiException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketDAO.loadById(merchantId, storeId, timeBucketId, false, false);
        //有效收费项目设置相关营业时段的周期数据需要删除
        long now = System.currentTimeMillis();
        MutableDateTime mdt = new MutableDateTime(now);
        mdt.addDays(-1);
        mdt.setHourOfDay(23);
        mdt.setMinuteOfHour(59);
        mdt.setSecondOfMinute(59);
        mdt.setMillisOfSecond(999);
        long endTime = mdt.getMillis();
        long today = DateUtil.getBeginTime(now, null);

        //1.将已经生效的周期设置迁移到周期备份数据表中
        //List<StoreChargeItemWeek> timeBucketForPasts = this.storeChargeItemWeekDAO.getTimeBucketForPast(merchantId, storeId, timeBucketId, now, true);//TODO:考虑使用分页查询
        int count = this.storeChargeItemWeekDAO.getTimeBucketForPastCount(merchantId, storeId, timeBucketId, now, true);
        int pageNums = PageUtil.getPageNum(count, pageSize);
        for (int i = 0; i < pageNums; i++) {
            int begin = i * pageSize;
            List<StoreChargeItemWeek> timeBucketForPasts = this.storeChargeItemWeekDAO.getTimeBucketForPastPage(merchantId, storeId, timeBucketId, now, begin, pageSize, true);
            List<StoreChargeItemWeekBack> chargeItemWeekBacks = BeanUtil.copyList(timeBucketForPasts, StoreChargeItemWeekBack.class);
            this.storeChargeItemWeekBackDAO.batchCreate(chargeItemWeekBacks);//将生效的周期设置插入到备份表中
        }
        //2.修改已经生效的周期设置数据中的结束时间为昨天最后的时间(开始时间早于今天)
        //更新已经生效的周期设置数据中结束时间是昨天最后的时间(开始时间早于今天)
        this.storeChargeItemWeekDAO.updateEndTimeByTimeBucketIdForPast(merchantId, storeId, timeBucketId, endTime, today);
        //3.将未来的与营业时段有关的周期迁移到周期备份数据表中(包括从今天开始设置的数据)
        //List<StoreChargeItemWeek> timeBucketForFeatures = this.storeChargeItemWeekDAO.getTimeBucketForFeature(merchantId, storeId, timeBucketId, now, true);//TODO:考虑使用分页查询
        count = this.storeChargeItemWeekDAO.getTimeBucketForFeatureCount(merchantId, storeId, timeBucketId, now, true);
        pageNums = PageUtil.getPageNum(count, pageSize);
        for (int i = 0; i < pageNums; i++) {
            int begin = i * pageSize;
            List<StoreChargeItemWeek> timeBucketForFeatures = this.storeChargeItemWeekDAO.getTimeBucketForFeaturePage(merchantId, storeId, timeBucketId, now, begin, pageSize, true);
            List<StoreChargeItemWeekBack> chargeItemWeekBacks = BeanUtil.copyList(timeBucketForFeatures, StoreChargeItemWeekBack.class);
            this.storeChargeItemWeekBackDAO.batchCreate(chargeItemWeekBacks);
        }
        //4.删除未来的与营业时段有关的周期设置数据(包括从今天开始设置的数据)
        this.storeChargeItemWeekDAO.deleteByTimeBucketIdForFuture(merchantId, storeId, timeBucketId, today);
        //5.电视菜单停用
        this.storeTvMenuDAO.pausedStoreTvMenu(merchantId, storeId, timeBucketId);
        //6.设置营业时段的删除状态true，即停用营业时段
        storeTimeBucket.makeDeleted(now);

    }

    /**
     * 获得店铺有效的营业时段列表
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @return 数据集合
     */
    public List<StoreTimeBucket> getStoreTimeBucketListForStore(int merchantId, long storeId) {
        return this.storeTimeBucketDAO.getListForStore(merchantId, storeId, true, true);
    }

    /**
     * 获得当前正在营业的跨天营业时段
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public StoreTimeBucket getCurOverDayTimeBucket(int merchantId, long storeId, long time) {
        List<StoreTimeBucket> list = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, true, true);
        for (StoreTimeBucket storeTimeBucket : list) {
            if (storeTimeBucket.isOverDay() && storeTimeBucket.compareTo(time) == 0) {
                return storeTimeBucket;
            }
        }
        return null;
    }

    public List<StoreTimeBucket> getStoreTimeBucketListForStores(int merchantId, List<Long> storeIds) {
        Set<Long> storeIdSet = Sets.newHashSet();
        storeIdSet.addAll(storeIds);
        List<StoreTimeBucket> storeTimeBuckets = Lists.newArrayList();
        for (Long storeId : storeIdSet) {
            List<StoreTimeBucket> list = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, true, true);
            storeTimeBuckets.addAll(list);
        }
        return storeTimeBuckets;
    }

    public StoreTimeBucket getStoreTimeBucket(int merchantId, long storeId, long timeBucketId) throws T5weiException {
        return this.storeTimeBucketDAO.loadByIdForQuery(merchantId, storeId, timeBucketId, false, false);
    }

    public StoreTimeBucket getStoreTimeBucket(int merchantId, long storeId, long timeBucketId, boolean enableSlave) throws T5weiException {
        return this.storeTimeBucketDAO.loadByIdForQuery(merchantId, storeId, timeBucketId, enableSlave, true);
    }

    public Map<Long, StoreTimeBucket> getStoreTimeBucketMapInIds(int merchantId, long storeId, List<Long> timeBucketIds) {
        Map<Long, StoreTimeBucket> storeTimeBucketMap = this
                .storeTimeBucketDAO.getMapInIds(merchantId, storeId, timeBucketIds, true, true);
        Set<Map.Entry<Long, StoreTimeBucket>> set = storeTimeBucketMap.entrySet();
        Iterator<Map.Entry<Long, StoreTimeBucket>> it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<Long, StoreTimeBucket> e = it.next();
            if (e.getValue() == null) {
                it.remove();
            }
        }
        return storeTimeBucketMap;
    }

    public Map<Long, StoreTimeBucket> getStoreTimeBucketMapInIds(int merchantId, Map<Long, Set<Long>> storeTimeBucketMap) {
        boolean enableSlave = true;
        Map<Long, StoreTimeBucket> resultMap = new HashMap<Long, StoreTimeBucket>();
        for (Long storeId : storeTimeBucketMap.keySet()) {
            Set<Long> timeBucketIds = storeTimeBucketMap.get(storeId);
            Map<Long, StoreTimeBucket> timeBucketMap = this.storeTimeBucketDAO.getMapInIds(merchantId, storeId, new ArrayList<Long>(timeBucketIds), enableSlave);
            resultMap.putAll(timeBucketMap);
        }
        return resultMap;
    }

    /**
     * 获得营业时段，如果营业时段id＝0，就获取当前时间可用的营业时段
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param timeBucketId 营业时段id
     * @param time         指定的时间 当天开始时间00:00:00，如果时间不是当天，需要设置有效的timeBucketId
     * @return 营业时段
     * @throws T5weiException
     */
    public StoreTimeBucket getStoreTimeBucketForDate(int merchantId, long storeId, long timeBucketId, long time) throws T5weiException {
        StoreTimeBucket storeTimeBucket = null;
        if (timeBucketId > 0) {
            storeTimeBucket = this.storeTimeBucketDAO.loadByIdForQuery(merchantId, storeId, timeBucketId, true, true);
        } else {
            long now = System.currentTimeMillis();
            long nowDate = DateUtil.getBeginTime(now, null);
            long timeDate = DateUtil.getBeginTime(time, null);
            if (nowDate != timeDate) {
                throw new T5weiException(T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue(), "storeId[" + storeId + "] not timeBucket supported on time [" + time + "]");
            }
            List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, true, true);
            for (StoreTimeBucket obj : storeTimeBuckets) {
                if (obj.isInTime(now)) {
                    storeTimeBucket = obj;
                    break;
                }
            }
        }
        if (storeTimeBucket == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue(), "storeId[" + storeId + "] not timeBucket supported on time [" + time + "]");
        }

        StoreDateTimeBucketSetting storeDateTimeBucketSetting = this
                .storeDateTimeBucketSettingDAO.getForDate(merchantId,
                        storeId, time, timeBucketId, true, true);
        if (storeDateTimeBucketSetting != null && storeDateTimeBucketSetting
                .isPaused()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue(), "storeId[" + storeId + "] timeBucketId[" + storeTimeBucket.getTimeBucketId() + "] is " +
                    "paused on time [" + new DateTime(time) + "]");
        }

        return storeTimeBucket;
    }

    /**
     * 获取当前时间外送的营业时段,timeBucketId为0，获取当前时间当前时间支持外卖的营业时段
     *
     * @param merchantId
     * @param storeId
     * @param timeBucketId
     * @param time
     * @return
     * @throws T5weiException
     */
    public StoreTimeBucket getDeliveryStoreTimeBucketForDate(int merchantId, long storeId, long timeBucketId, long time) throws T5weiException {
        StoreTimeBucket storeTimeBucket = null;
        long currentTime = System.currentTimeMillis();
        if (timeBucketId > 0) {
            storeTimeBucket = this.storeTimeBucketDAO.loadByIdForQuery(merchantId, storeId, timeBucketId, true, true);
            if (!storeTimeBucket.isDeliverySupported() || !storeTimeBucket.isInDeliveryTime(currentTime)) {
                throw new T5weiException(T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue(), "storeId[" + storeId + "] not timeBucket supported on time [" + time + "]");
            }
        } else {
            List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, true, true);
            for (StoreTimeBucket obj : storeTimeBuckets) {
                if (obj.isDeliverySupported() && obj.isInDeliveryTime(currentTime)) {
                    storeTimeBucket = obj;
                    break;
                }
            }
        }

        if (storeTimeBucket == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue(), "storeId[" + storeId + "] not timeBucket supported on time [" + time + "]");
        }

        boolean isdeliveryTimeRes = storeTimeBucket.isDeliveryTimeRes(currentTime);//判断营业时段
        if (isdeliveryTimeRes) {
            MutableDateTime mdt = new MutableDateTime(time);
            mdt.addDays(-1);
            time = mdt.getMillis();
        }

        StoreDateTimeBucketSetting storeDateTimeBucketSetting = this.storeDateTimeBucketSettingDAO.getForDate(merchantId, storeId, time, storeTimeBucket.getTimeBucketId(), true, true);
        if (storeDateTimeBucketSetting != null && storeDateTimeBucketSetting.isPaused()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue(), "storeId[" + storeId + "] timeBucketId[" + storeTimeBucket.getTimeBucketId() + "] is " +
                    "paused on time [" + new DateTime(time) + "]");
        }
        return storeTimeBucket;
    }

    public List<StoreTimeBucket> getStoreItemBucketsInIds(int merchantId, List<StoreTimeBucketIdsParam> storeTimeBucketIdsParams) {
        List<StoreTimeBucket> storeTimeBuckets = Lists.newArrayList();
        for (StoreTimeBucketIdsParam storeTimeBucketIdsParam : storeTimeBucketIdsParams) {
            List<StoreTimeBucket> list = this.storeTimeBucketDAO.getListInIds(merchantId, storeTimeBucketIdsParam.getStoreId(), storeTimeBucketIdsParam.getTimeBucketIds());
            storeTimeBuckets.addAll(list);
        }
        return storeTimeBuckets;
    }

    public StoreTimeBucket changeStoreTimeBucketDeliverySupported(int merchantId, long storeId, long timeBucketId, boolean deliverySupported) throws T5weiException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketDAO.loadById(merchantId, storeId, timeBucketId, false, true);
        storeTimeBucket.changeDeliverySupported(deliverySupported);
        return storeTimeBucket;
    }

    /**
     * 获得指定日期出现的营业时段
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param time       日期
     * @return 营业时段集合
     */
    public List<StoreTimeBucket> getStoreTimeBucketsInStoreForTime(int merchantId, long storeId, long time) {
        //当天日期
        long dayAtNow = DateUtil.getBeginTime(System.currentTimeMillis(), null);

        StoreDateBizSetting storeDateBizSetting = this.storeDateBizSettingDAO
                .getForSelectedDate(merchantId, storeId, time, true, true);
        if (storeDateBizSetting != null && storeDateBizSetting.isPaused()) {
            return new ArrayList<>(0);
        }
        Map<Long, StoreDateTimeBucketSetting> bucketSettingMap = this
                .storeDateTimeBucketSettingDAO.getMapForDate(merchantId,
                        storeId, time, true, true);
        int weekDay = StoreDateBizSetting.getWeekDay(storeDateBizSetting, time);
        List<Long> timeBucketIds = this.storeChargeItemWeekDAO.getTimeBucketIdsForDate(merchantId, storeId, time, weekDay);
        Iterator<Long> timeBucketIdIt = timeBucketIds.iterator();
        while (timeBucketIdIt.hasNext()) {
            long timeBucketId = timeBucketIdIt.next();
            StoreDateTimeBucketSetting bucketSetting = bucketSettingMap.get
                    (timeBucketId);
            if (bucketSetting != null && bucketSetting.isPaused()) {
                timeBucketIdIt.remove();
            }
        }
        Map<Long, StoreTimeBucket> storeTimeBucketMap = this.storeTimeBucketDAO
                .getMapInIds(merchantId, storeId, timeBucketIds, true, true);
        //删除已经无效的营业时段
        Set<Map.Entry<Long, StoreTimeBucket>> set = storeTimeBucketMap.entrySet();
        Iterator<Map.Entry<Long, StoreTimeBucket>> it = set.iterator();
        while (it.hasNext()) {
            Map.Entry<Long, StoreTimeBucket> e = it.next();
            //只过滤掉当天无效的营业时段 add by lixuwei
            if (dayAtNow == time) {
                if (e.getValue().isDeleted()) {
                    it.remove();
                }
            }
        }
        List<StoreTimeBucket> storeTimeBuckets = Lists.newArrayList();
        storeTimeBuckets.addAll(storeTimeBucketMap.values());
        StoreTimeBucketUtil.sortTimeBuckets(storeTimeBuckets);
        return storeTimeBuckets;
    }

    public StoreTimeBucket getStoreTimeBucketByName(int merchantId, long storeId, String name)
            throws T5weiException {
        List<StoreTimeBucket> list = this.storeTimeBucketDAO.getListForStore(merchantId, storeId,
                true, true);
        for (StoreTimeBucket storeTimeBucket : list) {
            if (storeTimeBucket.getName().equals(name)) {
                return storeTimeBucket;
            }
        }
        throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_INVALID.getValue(),
                "storeId[" + storeId + "] name[" + name + "] timeBucket invalid");
    }

    /**
     * 查询一定时间范围内有效的营业时段
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param startTime  开始时间
     * @param endTime    结束时间
     * @throws T5weiException
     * @throws TException
     */
    public Collection<StoreTimeBucket> getStoreTimeBucketsByTimeRange(int merchantId, long storeId, long startTime, long endTime) {
        Map<Long, StoreTimeBucket> timeBucketHashMap = new HashMap<>();
        List<Long> dayList = getDayList(startTime, endTime);

        for (Long day : dayList) {
            List<StoreTimeBucket> storeTimeBuckets = getStoreTimeBucketsInStoreForTime(merchantId, storeId, day);
            for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
                timeBucketHashMap.put(storeTimeBucket.getTimeBucketId(), storeTimeBucket);
            }
        }
        return timeBucketHashMap.values();
    }

    /**
     * 计算得到查询时间范围内的日期集合
     *
     * @param repastDateStart
     * @param repastDateEnd
     * @return
     */
    public List<Long> getDayList(long repastDateStart, long repastDateEnd) {
        long beginTime = DateUtil.getBeginTime(repastDateStart, null);
        long endTime = DateUtil.getBeginTime(repastDateEnd, null);
        List<Long> dayList = Lists.newArrayList();
        dayList.add(beginTime);
        while (beginTime + DAY_TIME_MILLIS <= endTime) {
            dayList.add(beginTime += DAY_TIME_MILLIS);
        }
        return dayList;
    }

    /**
     * 获得店铺里面所有的营业时段 包括删除的
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreTimeBucket> getStoreAllTimeBucket(int merchantId, Long storeId, boolean enableSlave, boolean enableCache) {
        return this.storeTimeBucketDAO.getAllTimeBucket(merchantId, storeId, enableSlave, enableCache);
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreStoreTimeBucket(int merchantId, long storeId, long timeBucketId) {
        long currentTime = System.currentTimeMillis();
        //1.确定指定经营时段需要恢复的周期设置
        //List<StoreChargeItemWeekBack> chargeItemWeekBacks = this.storeChargeItemWeekBackDAO.getStoreChargeItemWeekBacks(merchantId, storeId, timeBucketId, true);//分页
        int count = this.storeChargeItemWeekBackDAO.getStoreChargeItemWeekBackCount(merchantId, storeId, timeBucketId, false);
        int pageNums = PageUtil.getPageNum(count, pageSize);
        for (int i = 0; i < pageNums; i++) {
            int begin = i * pageSize;
            //1.确定指定经营时段需要恢复的周期设置
            List<StoreChargeItemWeekBack> chargeItemWeekBacks = this.storeChargeItemWeekBackDAO.getStoreChargeItemWeekBackPage(merchantId, storeId, timeBucketId, begin, pageSize, false);
            List<Long> chargeItemWeekBackIds = new ArrayList<Long>();
            for (StoreChargeItemWeekBack itemWeekBack : chargeItemWeekBacks) {
                chargeItemWeekBackIds.add(itemWeekBack.getItemWeekId());
            }
            //2.删除旧有的周期设置
            this.storeChargeItemWeekDAO.batchDelete(merchantId, storeId, chargeItemWeekBackIds);
            //3.插入备份表中的周期设置
            List<StoreChargeItemWeek> chargeItemWeeks = BeanUtil.copyList(chargeItemWeekBacks, StoreChargeItemWeek.class);
            this.storeChargeItemWeekDAO.batchCreateHasId(merchantId, storeId, chargeItemWeeks);
        }
        //4.恢复电视菜单
        this.storeTvMenuDAO.onStoreTvMenu(merchantId, storeId, timeBucketId);
        //5.删除备份表中的周期设置
        this.storeChargeItemWeekBackDAO.batchDeleteByTimeBucketId(merchantId, storeId, timeBucketId);
        //6.经营时段deleted标志修改，并且确定经营时段的名称
        List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, false, false);
        if (storeTimeBuckets != null && !storeTimeBuckets.isEmpty()) {
            int nameCount = 0;
            StoreTimeBucket timeBucket = this.storeTimeBucketDAO.getById(merchantId, storeId, timeBucketId, false, false);
            String storeTimeBucketName = timeBucket.getName();
            String storeTimeBucketNamePostfix = timeBucket.getName();
            for (int i = 0; i < storeTimeBuckets.size(); i++) {
                StoreTimeBucket storeTimeBucket = storeTimeBuckets.get(i);
                if (storeTimeBucket.getTimeBucketId() != timeBucketId && storeTimeBucket.getName().equals(storeTimeBucketNamePostfix)) {
                    i = -1;
                    nameCount++;
                    if (nameCount == 1) {
                        storeTimeBucketNamePostfix = "(" + "恢复" + ")";
                    } else {
                        storeTimeBucketNamePostfix = "(" + "恢复" + nameCount + ")";
                    }
                    storeTimeBucketNamePostfix = storeTimeBucketName + storeTimeBucketNamePostfix;
                }
            }
            storeTimeBucketName = storeTimeBucketNamePostfix;
            timeBucket.setName(storeTimeBucketName);
            timeBucket.setDeleted(false);
            timeBucket.setUpdateTime(currentTime);
            this.storeTimeBucketDAO.update(timeBucket);
        }
    }

    public StoreTimeBucket getDeletedTimeBucketByName(int merchantId, long storeId, String name) {
        List<StoreTimeBucket> timeBucketForDeleteds = this.storeTimeBucketDAO.getTimeBucketForDeleted(merchantId, storeId);
        if (timeBucketForDeleteds != null && !timeBucketForDeleteds.isEmpty()) {
            for (StoreTimeBucket storeTimeBucket : timeBucketForDeleteds) {
                if (storeTimeBucket.getName().equals(name)) {
                    return storeTimeBucket;
                }
            }
        }
        return null;
    }

    public StoreTimeBucket getStoreTimeBucket4Waimai(int merchantId, long storeId, long time) throws T5weiException {
        List<StoreTimeBucket> list = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, true, true);
        for (StoreTimeBucket bucket : list) {
            int result = bucket.compareTo(time);
            if (result == 0) {
                return bucket;
            }
            if (result == 1) {
                return bucket;
            }
        }
        throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_INVALID.getValue(),
                "merchantId[" + merchantId + "] storeId[" + storeId + "] time[" + time + "] no timebucket supported");
    }
}
