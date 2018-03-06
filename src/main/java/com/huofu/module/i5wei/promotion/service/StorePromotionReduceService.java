package com.huofu.module.i5wei.promotion.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.promotion.dao.StorePromotionReduceChargeItemDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionReduceDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionReducePeriodDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionReduceQuotaDAO;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReducePeriod;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceQuota;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.promotion.StorePromotionPeriodParam;
import huofucore.facade.i5wei.promotion.StorePromotionReduceParam;
import huofucore.facade.i5wei.promotion.StorePromotionReduceQuotaParam;
import huofucore.facade.i5wei.promotion.StorePromotionStatusEnum;
import huofucore.facade.merchant.exception.TMerchantException;
import huofucore.facade.merchant.staff.StaffDTO2;
import huofucore.facade.merchant.staff.query.StaffQueryFacade;
import huofuhelper.util.DateUtil;
import huofuhelper.util.PageResult;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class StorePromotionReduceService {

    @Resource
    private StorePromotionReduceDAO storePromotionReduceDAO;

    @Resource
    private StorePromotionReducePeriodDAO storePromotionReducePeriodDAO;

    @Resource
    private StorePromotionReduceChargeItemDAO storePromotionReduceChargeItemDAO;

    @Resource
    private StoreChargeItemService storeChargeItemService;

    @Resource
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Resource
    private StorePromotionReduceQuotaDAO storePromotionReduceQuotaDAO;

    @ThriftClient
    private StaffQueryFacade.Iface staffQueryFacade;

    public void setStaffQueryFacade(StaffQueryFacade.Iface staffQueryFacade) {
        this.staffQueryFacade = staffQueryFacade;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StorePromotionReduce saveStorePromotionReduce(StorePromotionReduceParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String title = param.getTitle();
        if (param.getPromotionReduceId() <= 0) {
            if (this.storePromotionReduceDAO.hasOtherAvaliable(merchantId, storeId, param.getPromotionReduceId(), param.getBeginTime())) {
                throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_NUM_LIMIT.getValue(),
                        "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" +
                                param.getPromotionReduceId() + "] beginTime[" + new Date(param.getBeginTime()) + "] has other avaliable");
            }
        }
        if (this.storePromotionReduceDAO.hasDuplicateNameInavaliable(merchantId, storeId, title, param.getPromotionReduceId(), System.currentTimeMillis())) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_TITLE_DUPLICATE.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" + param.getPromotionReduceId() + "] title[" + title + "] duplicate");
        }
        StorePromotionReduce obj;
        if (param.getPromotionReduceId() > 0) {
            obj = this.storePromotionReduceDAO.loadById(merchantId, storeId, param.getPromotionReduceId(), false, false);
            if (obj.isEnded()) {
                //已结束的活动不能修改
                throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_ENDED.getValue(),
                        "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" + param.getPromotionReduceId()
                                + "] ended");
            }
            if (obj.isDoing()) {
                //进行中活动不能修改开始时间
                if (obj.getBeginTime() != param.getBeginTime()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_DOING_MODIFY_BEGINTIME_FORBIDDEN.getValue(),
                            "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" + param.getPromotionReduceId()
                                    + "] doing");
                }
            }
            if (obj.isNotBegin()) {
                //未开始的活动开始时间>=当天
                long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
                if (param.getBeginTime() < today) {
                    throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
                            "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" + param.getPromotionReduceId()
                                    + "] beginTime[" + param.getBeginTime() + "] must >= today");
                }
            }
            obj.snapshot();
        } else {
            obj = new StorePromotionReduce();
        }
        BeanUtil.copy(param, obj, true);
        long beginTime = DateUtil.getBeginTime(obj.getBeginTime(), null);
        long endTime = 0;
        if (obj.getEndTime() > 0) {
            if (obj.getEndTime() == Long.MAX_VALUE) {
                endTime = obj.getEndTime();
            } else {
                endTime = DateUtil.getEndTime(obj.getEndTime(), null);
            }
        }
        if (param.isUnlimit()) {
            endTime = Long.MAX_VALUE;
        }
        obj.setBeginTime(beginTime);
        obj.setEndTime(endTime);
        if (param.getPeriodParamsSize() > 0) {
            obj.setSelectPeriod(true);
        } else {
            obj.setSelectPeriod(false);
        }
        if (param.getPromotionReduceId() > 0) {
            obj.setUpdateTime(System.currentTimeMillis());
            obj.update();
        } else {
            obj.init4Create();
            obj.create();
        }
        obj.setChargeItems(this._saveStorePromotionReduceChargeItems(merchantId, storeId, obj.getPromotionReduceId(), param.getChargeItemIds(), param.isSelectChargeItem()));
        obj.setPeriods(this._saveStorePromotionReducePeriods(merchantId, storeId, obj.getPromotionReduceId(), param.getPeriodParams()));
        obj.setQuotas(this._saveStorePromotionReduceQuotas(merchantId, storeId, obj.getPromotionReduceId(), param.getQuotaParams()));
        return obj;
    }

    public StorePromotionReduce getStorePromotionReduce(int merchantId, long storeId, long promotionReduceId, boolean loadDetail, boolean enableSlave, boolean enableCache) throws TException {
        StorePromotionReduce promotionReduce = this.storePromotionReduceDAO.loadById(merchantId, storeId, promotionReduceId, enableSlave, enableCache);
        if (loadDetail) {
            List<StorePromotionReduce> olist = Lists.newArrayList(promotionReduce);
            this.buildReduceInfo(merchantId, storeId, olist, enableSlave, enableCache, true);
            try {
                StaffDTO2 staffDTO2 = this.staffQueryFacade.getStaff(merchantId, promotionReduce.getStaffId());
                promotionReduce.setStaffDTO2(staffDTO2);
            } catch (TMerchantException e) {
                throw new T5weiException(e.getErrorCode(), e.getMessage());
            }
        }
        return promotionReduce;
    }

    /**
     * 获得当前可用的满减活动
     *
     * @param param 参数
     * @return 没有数据时返回空对象
     */
    public ChargeItemReduceInfo getBestStorePromotionReduce(StorePromotionQueryParam param) {
        ChargeItemReduceInfo info = new ChargeItemReduceInfo();
        List<StorePromotionReduce> list = this.getStorePromotionReduces4Match(param.getMerchantId(), param.getStoreId(), param.getRepastDate());
        if (param.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
            StorePromotionReduce promotionReduce = this.getBestStorePromotionReduce(param.getRepastDate(),
                    param.getTimeBucketId(), param.getClientType(), StoreOrderTakeModeEnum.DINE_IN.getValue(), list);
            StorePromotionReduce promotionReduce4TakeOut = this.getBestStorePromotionReduce(param.getRepastDate(),
                    param.getTimeBucketId(), param.getClientType(), StoreOrderTakeModeEnum.TAKE_OUT.getValue(), list);
            info.setStorePromotionReduce(promotionReduce);
            info.setStorePromotionReduce4TakeOut(promotionReduce4TakeOut);
        } else {
            StorePromotionReduce promotionReduce = this.getBestStorePromotionReduce(param.getRepastDate(),
                    param.getTimeBucketId(), param.getClientType(), param.getTakeMode(), list);
            if (param.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
                info.setStorePromotionReduce4TakeOut(promotionReduce);
            }
            info.setStorePromotionReduce(promotionReduce);
        }
        return info;
    }

    public List<StorePromotionReduce> getStorePromotionReduces4Match(int merchantId, long storeId, long repastDate) {
        List<StorePromotionReduce> list = this.storePromotionReduceDAO.getList4Doing(merchantId, storeId, repastDate, true, true);
        this.buildReduceRefInfo(merchantId, storeId, list, true, true);
        return list;
    }

    public StorePromotionReduce getBestStorePromotionReduce(long repastDate, long timeBucketId, int clientType, int takeMode, List<StorePromotionReduce> storePromotionReduces) {
        boolean wechatVisit = StorePromotionHelper.isWechatVisist(clientType);
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            if (storePromotionReduce.canUse(repastDate, takeMode, timeBucketId, wechatVisit)) {
                return storePromotionReduce;
            }
        }
        return null;
    }

    public StorePromotionReduce getBestStorePromotionReduce(int merchantId, long storeId, long repastDate,
                                                            long timeBucketId, List<StorePromotionReduce> storePromotionReduces,
                                                            boolean loadChargeItem) {
        List<StorePromotionReduce> olist = Lists.newArrayList();
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            if (storePromotionReduce.canUse(repastDate, timeBucketId)) {
                olist.add(storePromotionReduce);
                if (loadChargeItem) {
                    this.buildReduceRefInfo(merchantId, storeId, olist, false, false);
                    this.buildChargeItemInfo(olist);
                }
                return olist.get(0);
            }
        }
        return null;
    }

    public StorePromotionReduce getBestStorePromotionReduce(int merchantId, long storeId, long repastDate, long timeBucketId, int clientType, int takeMode, boolean loadChargeItem) {
        List<StorePromotionReduce> list = this.getStorePromotionReduces4Match(merchantId, storeId, repastDate);
        boolean wechatVisit = StorePromotionHelper.isWechatVisist(clientType);
        if (wechatVisit) {
            return this.getBestStorePromotionReduce(repastDate, timeBucketId, clientType, takeMode, list);
        }
        return this.getBestStorePromotionReduce(merchantId, storeId, timeBucketId, repastDate, list, loadChargeItem);
    }

    public Map<Long, Long> getBestStorePromotionReduceMap(Collection<StoreChargeItem> storeChargeItems, StorePromotionReduce storePromotionReduce) {
        List<Long> ids = StoreChargeItem.getIdList(storeChargeItems);
        List<Long> chargeItemIds = storePromotionReduce.getContainChargeItemIds(ids);
        Map<Long, Long> map = Maps.newHashMap();
        for (Long chargeItemId : chargeItemIds) {
            map.put(chargeItemId, storePromotionReduce.getPromotionReduceId());
        }
        return map;
    }

    public StorePromotionSummary<StorePromotionReduce> getStorePromotionReduceSummary(int merchantId, long storeId) throws TException {
        List<StorePromotionReduce> list = this.storePromotionReduceDAO.getList4Avaliable(merchantId, storeId, System.currentTimeMillis(), true, true);
        this.buildReduceInfo(merchantId, storeId, list, true, true, true);
        StorePromotionSummary<StorePromotionReduce> storePromotionSummary = new StorePromotionSummary<>();
        for (StorePromotionReduce promotionReduce : list) {
            storePromotionSummary.build(promotionReduce, promotionReduce.getStatus());
        }
        storePromotionSummary.setCount4Ended(this.storePromotionReduceDAO.count4Status(merchantId, storeId, StorePromotionStatusEnum.ENDED.getValue()));
        return storePromotionSummary;
    }

    public void changePromotionReducePaused(int merchantId, long storeId, long promotionReduceId, boolean paused) throws T5weiException {
        StorePromotionReduce promotionReduce = this.storePromotionReduceDAO.loadById(merchantId, storeId, promotionReduceId, false, false);
        if (promotionReduce.isEnded()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_ENDED.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" + promotionReduceId + "] ended");
        }
        promotionReduce.snapshot();
        promotionReduce.setPaused(paused);
        promotionReduce.update();
    }

    public PageResult getStorePromotionReduces(int merchantId, long storeId, int status, int page, int size) {
        PageResult pr = new PageResult();
        int count = this.storePromotionReduceDAO.count4Status(merchantId, storeId, status);
        pr.setTotal(count);
        if (count == 0) {
            pr.setList(new ArrayList<>(0));
            return pr;
        }
        int begin = PageUtil.getBeginIndex(page, size);
        List<StorePromotionReduce> list = this.storePromotionReduceDAO.getList4Status(merchantId, storeId, status, begin, size);
        pr.setList(list);
        pr.setPage(page);
        pr.setSize(size);
        pr.build();
        return pr;
    }

    public void deleteStorePromotionReduce(int merchantId, long storeId, long promotionReduceId) throws T5weiException {
        StorePromotionReduce obj = this.storePromotionReduceDAO.loadById(merchantId, storeId, promotionReduceId, false, false);
        if (obj.getStatus() == StorePromotionStatusEnum.DOING.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_DOING.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" + promotionReduceId + "] doing.can not delete");
        }
        obj.makeDeleted();
    }

    private List<StorePromotionReducePeriod> _saveStorePromotionReducePeriods(
            int merchantId, long storeId, long promotionReduceId, List<StorePromotionPeriodParam> storePromotionPeriodParams) {
        this.storePromotionReducePeriodDAO.deleteByPromotionReduceId(merchantId, storeId, promotionReduceId);
        if (storePromotionPeriodParams == null || storePromotionPeriodParams.isEmpty()) {
            return null;
        }
        long createTime = System.currentTimeMillis();
        List<StorePromotionReducePeriod> list = Lists.newArrayList();
        for (StorePromotionPeriodParam param : storePromotionPeriodParams) {
            List<Integer> _weekDays = StorePromotionHelper.filterDuplicateWeekDay(param.getWeekDays());
            for (Integer weekDay : _weekDays) {
                StorePromotionReducePeriod reducePeriod = new StorePromotionReducePeriod();
                reducePeriod.setTimeBucketId(param.getTimeBucketId());
                reducePeriod.setWeekDay(weekDay);
                reducePeriod.setMerchantId(merchantId);
                reducePeriod.setStoreId(storeId);
                reducePeriod.setPromotionReduceId(promotionReduceId);
                reducePeriod.setCreateTime(createTime);
                list.add(reducePeriod);
            }
        }
        this.storePromotionReducePeriodDAO.batchCreate(list);
        return list;
    }

    private List<StorePromotionReduceChargeItem> _saveStorePromotionReduceChargeItems(
            int merchantId, long storeId, long promotionReduceId, List<Long> chargeItemIds, boolean isSelectChargeItem) {
        this.storePromotionReduceChargeItemDAO.deleteByPromotionReduceId(merchantId, storeId, promotionReduceId);
        if (chargeItemIds == null || chargeItemIds.isEmpty() || !isSelectChargeItem) {
            return null;
        }
        List<StorePromotionReduceChargeItem> list = Lists.newArrayList();
        List<Long> _list = StorePromotionHelper.filterDuplicateId(chargeItemIds);
        long createTime = System.currentTimeMillis();
        for (Long chargeItemId : _list) {
            StorePromotionReduceChargeItem item = new StorePromotionReduceChargeItem();
            item.setMerchantId(merchantId);
            item.setStoreId(storeId);
            item.setPromotionReduceId(promotionReduceId);
            item.setChargeItemId(chargeItemId);
            item.setCreateTime(createTime);
            list.add(item);
        }
        this.storePromotionReduceChargeItemDAO.batchCreate(list);
        return list;
    }

    /**
     * 保存满减金额设置是，需要进行比对
     */
    private List<StorePromotionReduceQuota> _saveStorePromotionReduceQuotas(int merchantId, long storeId, long promotionReduceId, List<StorePromotionReduceQuotaParam> storePromotionReduceQuotaParams) {
        this.storePromotionReduceQuotaDAO.deleteByPromotionReduceId(merchantId, storeId, promotionReduceId);
        if (storePromotionReduceQuotaParams == null || storePromotionReduceQuotaParams.isEmpty()) {
            return null;
        }
        List<StorePromotionReduceQuota> list = Lists.newArrayList();
        Set<String> keySet = Sets.newHashSet();
        for (StorePromotionReduceQuotaParam param : storePromotionReduceQuotaParams) {
            String key = param.getQuotaPrice() + "_" + param.getReducePrice();
            //为了去重
            if (keySet.contains(key)) {
                continue;
            }
            StorePromotionReduceQuota quota = new StorePromotionReduceQuota();
            BeanUtil.copy(param, quota);
            quota.setMerchantId(merchantId);
            quota.setStoreId(storeId);
            quota.setPromotionReduceId(promotionReduceId);
            quota.init4Create();
            list.add(quota);
            keySet.add(key);
        }
        Collections.sort(list, (o1, o2) -> {
            if (o1.getQuotaPrice() > o2.getQuotaPrice()) {
                return 1;
            }
            if (o1.getQuotaPrice() < o2.getQuotaPrice()) {
                return -1;
            }
            return 0;
        });
        this.storePromotionReduceQuotaDAO.batchCreate(list);
        return list;
    }

    private void buildChargeItemInfo(List<StorePromotionReduce> storePromotionReduces) {
        if (storePromotionReduces == null || storePromotionReduces.isEmpty()) {
            return;
        }
        int merchantId = storePromotionReduces.get(0).getMerchantId();
        long storeId = storePromotionReduces.get(0).getStoreId();
        List<Long> chargeItemIds = Lists.newArrayList();
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            if (storePromotionReduce.getChargeItems() != null) {
                for (StorePromotionReduceChargeItem reduceChargeItem : storePromotionReduce.getChargeItems()) {
                    chargeItemIds.add(reduceChargeItem.getChargeItemId());
                }
            }
        }
        Map<Long, StoreChargeItem> chargeItemMap = this.storeChargeItemService.getStoreChargeItemMapInIds(merchantId, storeId, chargeItemIds, 0, true, true);
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            if (storePromotionReduce.getChargeItems() != null) {
                for (StorePromotionReduceChargeItem reduceChargeItem : storePromotionReduce.getChargeItems()) {
                    reduceChargeItem.setStoreChargeItem(chargeItemMap.get(reduceChargeItem.getChargeItemId()));
                }
            }
        }
    }

    private void buildTimeBucketInfo(List<StorePromotionReduce> storePromotionReduces) {
        if (storePromotionReduces == null || storePromotionReduces.isEmpty()) {
            return;
        }
        int merchantId = storePromotionReduces.get(0).getMerchantId();
        long storeId = storePromotionReduces.get(0).getStoreId();
        Set<Long> bucketIdSet = Sets.newHashSet();
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            if (storePromotionReduce.getPeriods() != null) {
                for (StorePromotionReducePeriod reducePeriod : storePromotionReduce.getPeriods()) {
                    bucketIdSet.add(reducePeriod.getTimeBucketId());
                }
            }
        }
        List<Long> bucketIds = Lists.newArrayList();
        bucketIds.addAll(bucketIdSet);
        Map<Long, StoreTimeBucket> timeBucketMap = this.storeTimeBucketDAO.getMapInIds(merchantId, storeId, bucketIds, true, true);
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            if (storePromotionReduce.getPeriods() != null) {
                for (StorePromotionReducePeriod reducePeriod : storePromotionReduce.getPeriods()) {
                    reducePeriod.setStoreTimeBucket(timeBucketMap.get(reducePeriod.getTimeBucketId()));
                }
            }
        }
    }

    private void buildReduceRefInfo(int merchantId, long storeId, List<StorePromotionReduce> promotionReduces, boolean enableSlave, boolean enableCache) {
        if (promotionReduces == null || promotionReduces.isEmpty()) {
            return;
        }
        List<Long> ids = StorePromotionReduce.getIdList(promotionReduces);
        Map<Long, List<StorePromotionReduceChargeItem>> reduceListMap = this.storePromotionReduceChargeItemDAO.getMapInPromotionReduceIds(merchantId, storeId, ids, enableSlave, enableCache);
        Map<Long, List<StorePromotionReducePeriod>> periodListMap = this.storePromotionReducePeriodDAO.getMapInPromotionReduceIds(merchantId, storeId, ids, enableSlave, enableCache);
        Map<Long, List<StorePromotionReduceQuota>> quotaListMap = this.storePromotionReduceQuotaDAO.getMapInPromotionReduceIds(merchantId, storeId, ids, enableSlave, enableCache);
        for (StorePromotionReduce promotionReduce : promotionReduces) {
            promotionReduce.setChargeItems(reduceListMap.get(promotionReduce.getPromotionReduceId()));
            promotionReduce.setPeriods(periodListMap.get(promotionReduce.getPromotionReduceId()));
            promotionReduce.setQuotas(quotaListMap.get(promotionReduce.getPromotionReduceId()));
        }
    }

    private void buildStaffInfo(List<StorePromotionReduce> storePromotionReduces) throws TException {
        if (storePromotionReduces == null || storePromotionReduces.isEmpty()) {
            return;
        }
        int merchantId = storePromotionReduces.get(0).getMerchantId();
        List<Long> ids = Lists.newArrayList();
        for (StorePromotionReduce promotionReduce : storePromotionReduces) {
            if (promotionReduce.getStaffId() > 0) {
                ids.add(promotionReduce.getStaffId());
            }
        }
        try {
            Map<Long, StaffDTO2> staffDTO2Map = this.staffQueryFacade.getStaffMap(merchantId, ids);
            for (StorePromotionReduce promotionReduce : storePromotionReduces) {
                promotionReduce.setStaffDTO2(staffDTO2Map.get(promotionReduce.getStaffId()));
            }
        } catch (TMerchantException e) {
            throw new T5weiException(e.getErrorCode(), e.getMessage());
        }
    }

    public Map<String, List<StorePromotionReduce>> getStorePromotionReduceMapByTitle(int merchantId, long storeId, String title, int size) throws TException {
        Map<String, List<StorePromotionReduce>> storePromotionReduceMap = new HashMap<>();
        List<StorePromotionReduce> noBeginPromotionList = this.storePromotionReduceDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.NOT_BEGIN.getValue(), title, size);
        List<StorePromotionReduce> doingPromotionList = this.storePromotionReduceDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.DOING.getValue(), title, size);
        List<StorePromotionReduce> pausedPromotionList = this.storePromotionReduceDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.PAUSED.getValue(), title, size);
        List<StorePromotionReduce> endedPromotionList = this.storePromotionReduceDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.ENDED.getValue(), title, size);

        List<StorePromotionReduce> promotionList = new ArrayList<>();
        promotionList.addAll(noBeginPromotionList);
        promotionList.addAll(doingPromotionList);
        promotionList.addAll(pausedPromotionList);
        promotionList.addAll(endedPromotionList);

        this.buildReduceInfo(merchantId, storeId, promotionList, true, true, true);

        storePromotionReduceMap.put("not_begin_list", noBeginPromotionList);
        storePromotionReduceMap.put("doing_list", doingPromotionList);
        storePromotionReduceMap.put("paused_list", pausedPromotionList);
        storePromotionReduceMap.put("ended_list", endedPromotionList);
        return storePromotionReduceMap;
    }

    public List<StorePromotionReduce> buildReduceInfo(int merchantId, long storeId, List<StorePromotionReduce> promotionReduces, boolean enableSlave, boolean enableCache, boolean needBuildReduceRefInfo) throws TException {
        if (needBuildReduceRefInfo) {
            this.buildReduceRefInfo(merchantId, storeId, promotionReduces, enableSlave, enableCache);
        }
        this.buildTimeBucketInfo(promotionReduces);
        this.buildChargeItemInfo(promotionReduces);
        this.buildStaffInfo(promotionReduces);
        this.filterChargeItem4Delete(promotionReduces);
        return promotionReduces;
    }

    private void filterChargeItem4Delete(List<StorePromotionReduce> storePromotionReduces) {
        if (storePromotionReduces == null || storePromotionReduces.isEmpty()) {
            return;
        }
        List<StorePromotionReduceChargeItem> list4Del = Lists.newArrayList();
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            if (storePromotionReduce.getChargeItems() == null || storePromotionReduce.getChargeItems().isEmpty()) {
                continue;
            }
            Iterator<StorePromotionReduceChargeItem> it = storePromotionReduce.getChargeItems().iterator();
            while (it.hasNext()) {
                StorePromotionReduceChargeItem next = it.next();
                if (next.getStoreChargeItem() == null || next.getStoreChargeItem().isDeleted()) {
                    it.remove();
                    list4Del.add(next);
                }
            }
        }
        this.storePromotionReduceChargeItemDAO.deleteBatch(list4Del);
    }

    /**
     * 保存指定收费项目下的折扣活动
     *
     * @param merchantId   商户ID
     * @param storeId      店铺ID
     * @param chargeItemId 收费项目ID
     * @param reduceIds    活动ID列表
     * @param time         当前时间
     */
    public StoreReduce4ChargeItemInfo saveStorePromotionReduce4ChargeItem(int merchantId, long storeId, long chargeItemId, List<Long> reduceIds, long time, List<Long> storeChargeItemIds) {
        List<StorePromotionReduce> promotionReduces = this.storePromotionReduceDAO.getList4Doing(merchantId, storeId,
                time, false, false);
        if (!CollectionUtils.isEmpty(promotionReduces)) {
            List<Long> reduceIds4Delete = StorePromotionReduce.getIdList(promotionReduces);
            this.storePromotionReduceChargeItemDAO.deleteByChargeItemId(merchantId, storeId, chargeItemId, reduceIds4Delete);
        }
        // 将未结束的活动中，没参加的活动拆分出来
        List<StorePromotionReduce> promotionReducesNotUse = Lists.newArrayList();
        Iterator<StorePromotionReduce> reduceIterator = promotionReduces.iterator();
        while (reduceIterator.hasNext()) {
            StorePromotionReduce next = reduceIterator.next();
            if (reduceIds == null || next != null && !reduceIds.contains(next.getPromotionReduceId())) {
                promotionReducesNotUse.add(next);
                reduceIterator.remove();
            }
        }

        List<StorePromotionReduceChargeItem> storePromotionReduceChargeItemList = Lists.newArrayList();
        for (StorePromotionReduce item : promotionReduces) {
            StorePromotionReduceChargeItem reduceChargeItem = this.buildStorePromotionReduceChargeItemInfo(merchantId, storeId, item.getPromotionReduceId(), chargeItemId, time);
            storePromotionReduceChargeItemList.add(reduceChargeItem);
        }

        List<Long> reduceIds4Update = Lists.newArrayList();
        for (StorePromotionReduce item : promotionReducesNotUse) {
            if (!item.isSelectChargeItem()) {
                // 修改收费项目的状态，isSelectChargeItem从0变成1
                reduceIds4Update.add(item.getPromotionReduceId());
                if (!CollectionUtils.isEmpty(storeChargeItemIds)) {
                    storeChargeItemIds.remove(chargeItemId);
                    // 将收费项目与活动的关联添加到数据库中
                    for (Long id : storeChargeItemIds) {
                        StorePromotionReduceChargeItem reduceChargeItem = this.buildStorePromotionReduceChargeItemInfo(merchantId, storeId, item.getPromotionReduceId(), id, time);
                        storePromotionReduceChargeItemList.add(reduceChargeItem);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(reduceIds4Update)) {
            // 批量更新
            this.storePromotionReduceDAO.batchUpdatePromotionReduceIsSelectChargeItem(merchantId, storeId, reduceIds4Update);
        }
        if (!CollectionUtils.isEmpty(storePromotionReduceChargeItemList)) {
            // 批量更新，先删除后更新
            this.storePromotionReduceChargeItemDAO.deleteBatch(storePromotionReduceChargeItemList);
            this.storePromotionReduceChargeItemDAO.batchCreate(storePromotionReduceChargeItemList);
        }

        StoreReduce4ChargeItemInfo storeReduce4ChargeItemInfo = new StoreReduce4ChargeItemInfo();
        storeReduce4ChargeItemInfo.setStorePromotionReduceList4Use(promotionReduces);
        storeReduce4ChargeItemInfo.setStorePromotionReduceList4NoUse(promotionReducesNotUse);
        return storeReduce4ChargeItemInfo;
    }

    public StorePromotionReduceChargeItem buildStorePromotionReduceChargeItemInfo(int merchantId, long storeId, long reduceId, long chargeItemId, long time) {
        StorePromotionReduceChargeItem reduceChargeItem = new StorePromotionReduceChargeItem();
        reduceChargeItem.setPromotionReduceId(reduceId);
        reduceChargeItem.setChargeItemId(chargeItemId);
        reduceChargeItem.setMerchantId(merchantId);
        reduceChargeItem.setStoreId(storeId);
        reduceChargeItem.setCreateTime(time);
        return reduceChargeItem;
    }

    public ChargeItemPromotionReduces getStorePromotionReduce4ChargeItemList(int merchantId, long storeId, long time, List<Long> chargeItemIds) {
        ChargeItemPromotionReduces result = new ChargeItemPromotionReduces();
        List<StorePromotionReduce> storePromotionReduceList = this.storePromotionReduceDAO.getList4Doing(merchantId, storeId, time, false, false);
        ;
        result.setStorePromotionReduceList(storePromotionReduceList);
        if (CollectionUtils.isEmpty(storePromotionReduceList) || CollectionUtils.isEmpty(chargeItemIds)) {
            return result;
        }
        List<Long> reduceIds = StorePromotionReduce.getIdList(storePromotionReduceList);
        List<StorePromotionReduceChargeItem> storePromotionReduceChargeItemList = this.storePromotionReduceChargeItemDAO.getListByReduceIds(merchantId, storeId, reduceIds);

        Map<Long, List<Long>> promotionReduceIdMap = Maps.newHashMap();
        for (StorePromotionReduceChargeItem item : storePromotionReduceChargeItemList) {
            if (!chargeItemIds.contains(item.getChargeItemId())) {
                continue;
            }
            List<Long> promotionReduceIds = promotionReduceIdMap.get(item.getChargeItemId());
            if (promotionReduceIds == null) {
                promotionReduceIds = Lists.newArrayList();
                promotionReduceIdMap.put(item.getChargeItemId(), promotionReduceIds);
            } else if (promotionReduceIds.contains(item.getPromotionReduceId())) {
                continue;
            }
            promotionReduceIds.add(item.getPromotionReduceId());
        }

        for (Long chargeItemId : chargeItemIds) {
            //将没有指定收费项目的折扣活动添加到二者的对应map中
            for (StorePromotionReduce item : storePromotionReduceList) {
                if (!item.isSelectChargeItem()) {
                    List<Long> promotionReduceIds = promotionReduceIdMap.get(chargeItemId);
                    if (promotionReduceIds == null) {
                        promotionReduceIds = Lists.newArrayList();
                        promotionReduceIdMap.put(chargeItemId, promotionReduceIds);
                    } else if (promotionReduceIds.contains(item.getPromotionReduceId())) {
                        continue;
                    }
                    promotionReduceIds.add(item.getPromotionReduceId());
                }
            }
        }
        result.setPromotionReduceIdMap(promotionReduceIdMap);
        return result;
    }

    public StoreReduce4ChargeItemInfo getStorePromotionReduce4ChargeItem(int merchantId, long storeId, long chargeItemId, long time) {
        StoreReduce4ChargeItemInfo result = new StoreReduce4ChargeItemInfo();
        List<StorePromotionReduce> storePromotionReduceList = this.storePromotionReduceDAO.getList4Doing(merchantId, storeId, time, false, false);
        if (CollectionUtils.isEmpty(storePromotionReduceList)) {
            return result;
        }
        List<Long> reduceIds = StorePromotionReduce.getIdList(storePromotionReduceList);
        List<StorePromotionReduceChargeItem> storePromotionReduceChargeItemList = this.storePromotionReduceChargeItemDAO.getListByChargeItemIdAndReduceIds(merchantId, storeId, chargeItemId, reduceIds);

        Set<Long> reduceIds4Use = Sets.newHashSet();
        for (StorePromotionReduceChargeItem item : storePromotionReduceChargeItemList) {
            reduceIds4Use.add(item.getPromotionReduceId());
        }

        for (StorePromotionReduce item : storePromotionReduceList) {
            if (!item.isSelectChargeItem()) {
                reduceIds4Use.add(item.getPromotionReduceId());
            }
        }
        List<StorePromotionReduce> reduceList4Use = Lists.newArrayList();
        Iterator<StorePromotionReduce> reduceIterator = storePromotionReduceList.iterator();
        while (reduceIterator.hasNext()) {
            StorePromotionReduce next = reduceIterator.next();
            if (reduceIds4Use.contains(next.getPromotionReduceId())) {
                reduceList4Use.add(next);
                reduceIterator.remove();
            }
        }
        result.setStorePromotionReduceList4Use(reduceList4Use);
        result.setStorePromotionReduceList4NoUse(storePromotionReduceList);
        return result;
    }
}
