package com.huofu.module.i5wei.promotion.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.promotion.dao.StorePromotionRebateChargeItemDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionRebateDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionRebatePeriodDAO;
import com.huofu.module.i5wei.promotion.entity.*;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.promotion.*;
import huofucore.facade.merchant.exception.TMerchantException;
import huofucore.facade.merchant.staff.StaffDTO2;
import huofucore.facade.merchant.staff.StaffFacade;
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
public class StorePromotionRebateService {

    @Resource
    private StorePromotionRebateDAO storePromotionRebateDAO;

    @Resource
    private StorePromotionRebateChargeItemDAO storePromotionRebateChargeItemDAO;

    @Resource
    private StorePromotionRebatePeriodDAO storePromotionRebatePeriodDAO;

    @Resource
    private StoreChargeItemService storeChargeItemService;

    @Resource
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @ThriftClient
    private StaffQueryFacade.Iface staffQueryFacade;

    @ThriftClient
    private StaffFacade.Iface staffFacade;

    void setStaffQueryFacade(StaffQueryFacade.Iface staffQueryFacade) {
        this.staffQueryFacade = staffQueryFacade;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StorePromotionRebate saveStorePromotionRebate(StorePromotionRebateParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String title = param.getTitle();
        long rebateId = param.getPromotionRebateId();
        if (param.getPromotionRebateId() <= 0) {
            if (this.storePromotionRebateDAO.count4Avaliable(merchantId, storeId, System.currentTimeMillis()) > 5) {
                throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_NUM_LIMIT.getValue(),
                        "merchantId[" + merchantId + "] storeId[" + storeId + "] promotionRebate num limit");
            }
        }
        if (this.storePromotionRebateDAO.hasDuplicateNameInavaliable(merchantId, storeId, title, rebateId, System.currentTimeMillis())) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_TITLE_DUPLICATE.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] rebateId[" + rebateId + "] title[" + title + "] duplicate");
        }
        StorePromotionRebate obj;
        if (param.getPromotionRebateId() > 0) {
            obj = this.storePromotionRebateDAO.loadById(merchantId, storeId, param.getPromotionRebateId(), false, false);
            if (obj.isEnded()) {
                //已结束的活动不能修改
                throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_ENDED.getValue(),
                        "merchantId[" + merchantId + "] storeId[" + storeId + "] rebateId[" + param.getPromotionRebateId()
                                + "] ended");
            }
            if (obj.isDoing()) {
                //进行中活动不能修改开始时间
                if (obj.getBeginTime() != param.getBeginTime()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_DOING_MODIFY_BEGINTIME_FORBIDDEN.getValue(),
                            "merchantId[" + merchantId + "] storeId[" + storeId + "] rebateId[" + param.getPromotionRebateId()
                                    + "] doing");
                }
            }
            if (obj.isNotBegin()) {
                //未开始的活动开始时间>=当天
                long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
                if (param.getBeginTime() < today) {
                    throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),
                            "merchantId[" + merchantId + "] storeId[" + storeId + "] rebateId[" + param.getPromotionRebateId()
                                    + "] beginTime[" + param.getBeginTime() + "] must >= today");
                }
            }
            obj.snapshot();
        } else {
            obj = new StorePromotionRebate();
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
        if (param.getPromotionRebateId() > 0) {
            obj.setUpdateTime(System.currentTimeMillis());
            obj.update();
        } else {
            obj.init4Create();
            obj.create();
        }
        if (obj.isSelectPeriod()) {
            obj.setPeriods(this._saveStorePromotionRebatePeriods(merchantId, storeId, obj.getPromotionRebateId(), param.getPeriodParams()));
        }
        obj.setChargeItems(this._saveStorePromotionRebateChargeItems(merchantId, storeId, obj.getPromotionRebateId(), param.getChargeItemIds(), param.isSelectChargeItem()));

        return obj;
    }

    public StorePromotionRebate getStorePromotionRebate(int merchantId, long storeId, long promotionRebateId, boolean enableSlave,
                                                        boolean enableCache) throws TException {
        StorePromotionRebate promotionRebate = this.storePromotionRebateDAO.loadById(merchantId, storeId, promotionRebateId, enableSlave, enableCache);
        List<StorePromotionRebate> olist = Lists.newArrayList(promotionRebate);
        this.buildRebateInfo(merchantId, storeId, olist, enableSlave, enableCache, true);
        return promotionRebate;
    }

    public StorePromotionSummary<StorePromotionRebate> getStorePromotionRebateSummary(int merchantId, long storeId) throws TException {
        List<StorePromotionRebate> list = this.storePromotionRebateDAO.getList4Avaliable(merchantId, storeId, System.currentTimeMillis(), true, true);
        this.buildRebateInfo(merchantId, storeId, list, true, true, true);

        StorePromotionSummary<StorePromotionRebate> storePromotionSummary = new StorePromotionSummary<>();
        for (StorePromotionRebate promotionRebate : list) {
            storePromotionSummary.build(promotionRebate, promotionRebate.getStatus());
        }
        storePromotionSummary.setCount4Ended(this.storePromotionRebateDAO.count4Status(merchantId, storeId, StorePromotionStatusEnum.ENDED.getValue()));
        return storePromotionSummary;
    }

    private void buildRebateRefInfo(int merchantId, long storeId, List<StorePromotionRebate> promotionRebates, boolean enableSlave, boolean enableCache) {
        List<Long> ids = StorePromotionRebate.getIdList(promotionRebates);
        Map<Long, List<StorePromotionRebateChargeItem>> rebateListMap = this.storePromotionRebateChargeItemDAO.getMapInPromotionRebateIds(merchantId, storeId, ids, enableSlave, enableCache);
        Map<Long, List<StorePromotionRebatePeriod>> periodListMap = this.storePromotionRebatePeriodDAO.getMapInPromotionRebateIds(merchantId, storeId, ids, enableSlave, enableCache);
        for (StorePromotionRebate promotionRebate : promotionRebates) {
            promotionRebate.setChargeItems(rebateListMap.get(promotionRebate.getPromotionRebateId()));
            promotionRebate.setPeriods(periodListMap.get(promotionRebate.getPromotionRebateId()));
        }
    }

    public ChargeItemRebateInfo getBestStorePromotionRebateMap(StorePromotionQueryParam param) {
        ChargeItemRebateInfo info = new ChargeItemRebateInfo();
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemService.getStoreChargeItemsInIds(param.getMerchantId(), param.getStoreId(), param.getChargeItemIds(), param.getRepastDate(), true, true);
        List<StorePromotionRebate> list = this.getStorePromotionRebates4Match(param.getMerchantId(), param.getStoreId(), param.getRepastDate());
        if (param.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
            Map<Long, StorePromotionRebate> rebateMap = this.getBestStorePromotionRebateMap(param.getTimeBucketId(),
                    param.getRepastDate(), param.getClientType(), StoreOrderTakeModeEnum.DINE_IN.getValue(), storeChargeItems, list);
            Map<Long, StorePromotionRebate> rebateMap4TakeOut = this.getBestStorePromotionRebateMap(param.getTimeBucketId(),
                    param.getRepastDate(), param.getClientType(), StoreOrderTakeModeEnum.TAKE_OUT.getValue(), storeChargeItems, list);
            info.setChargeItemRebateMap(rebateMap);
            info.setChargeItemRebateMap4TakeOut(rebateMap4TakeOut);
        } else {
            Map<Long, StorePromotionRebate> rebateMap = this.getBestStorePromotionRebateMap(param.getTimeBucketId(),
                    param.getRepastDate(), param.getClientType(), param.getTakeMode(), storeChargeItems, list);
            if (param.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
                info.setChargeItemRebateMap4TakeOut(rebateMap);
            }
            info.setChargeItemRebateMap(rebateMap);
        }
        return info;
    }


    /**
     * 获取每个收费项目匹配的最优活动
     *
     * @param merchantId       商户id
     * @param storeId          店铺id
     * @param timeBucketId     营业时段id
     * @param repastDate       就餐日期
     * @param takeMode         取餐方式
     * @param storeChargeItems 收费项目list
     * @return Map key:收费项目ID value:折扣活动
     */
    Map<Long, StorePromotionRebate> getBestStorePromotionRebateMap(int merchantId, long storeId,
                                                                   long timeBucketId, long repastDate,
                                                                   int clientType,
                                                                   int takeMode, Collection<StoreChargeItem> storeChargeItems) {
        List<StorePromotionRebate> list = this.getStorePromotionRebates4Match(merchantId, storeId, repastDate);
        return this.getBestStorePromotionRebateMap(timeBucketId, repastDate, clientType, takeMode, storeChargeItems, list);
    }

    public PageResult getStorePromotionRebates(int merchantId, long storeId, int status, int page, int size) throws TException {
        PageResult pr = new PageResult();
        int count = this.storePromotionRebateDAO.count4Status(merchantId, storeId, status);
        pr.setTotal(count);
        if (count == 0) {
            pr.setList(new ArrayList<>(0));
            return pr;
        }
        int begin = PageUtil.getBeginIndex(page, size);
        List<StorePromotionRebate> list = this.storePromotionRebateDAO.getList4Status(merchantId, storeId, status, begin, size);
        this.buildRebateInfo(merchantId, storeId, list, true, true, true);
        pr.setList(list);
        pr.setPage(page);
        pr.setSize(size);
        pr.build();
        return pr;
    }

    /**
     * 获得在有效期的活动
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param repastDate 就餐日期
     * @return 在有效期的活动
     */
    public List<StorePromotionRebate> getStorePromotionRebates4Match(int merchantId, long storeId, long repastDate) {
        List<StorePromotionRebate> list = this.storePromotionRebateDAO.getList4Doing(merchantId, storeId, repastDate, true, true);
        this.buildRebateRefInfo(merchantId, storeId, list, true, true);
        return list;
    }

    /**
     * 获得每个收费项目的最优活动
     *
     * @param timeBucketId          营业时段id
     * @param repastDate            就餐日期
     * @param clientType            请求终端
     * @param takeMode              就餐方式
     * @param storeChargeItems      收费项目集合
     * @param storePromotionRebates 在有效期的活动
     * @return key:收费项目id value:收费项目的最优活动。如果没有活动，key中对应的收费项目id不存在
     */
    public Map<Long, StorePromotionRebate> getBestStorePromotionRebateMap(long timeBucketId, long repastDate,
                                                                          int clientType, int takeMode,
                                                                          Collection<StoreChargeItem> storeChargeItems,
                                                                          Collection<StorePromotionRebate> storePromotionRebates) {
        Map<Long, StorePromotionRebate> map = Maps.newHashMap();
        if (storeChargeItems == null) {
            return map;
        }
        Collection<StorePromotionRebate> olist = this._filterBestStorePromotionRebates(timeBucketId, repastDate, clientType, takeMode, storePromotionRebates);
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            StorePromotionRebate storePromotionRebate = this.matchChargeItemAndPromotion(storeChargeItem, olist);
            if (storePromotionRebate != null) {
                map.put(storeChargeItem.getChargeItemId(), storePromotionRebate);
            }
        }
        return map;
    }

    /**
     * 获取最优活动，不考虑收费项目
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param timeBucketId 营业时段id
     * @param repastDate   就餐日期
     * @param clientType   请求终端
     * @param takeMode     取餐方式
     * @return 根据就餐相关信息查询可使用的活动
     */
    public Collection<StorePromotionRebate> getBestStorePromotionRebates(
            int merchantId, long storeId, long timeBucketId, long repastDate, int clientType, int takeMode, boolean loadChargeItem) {
        List<StorePromotionRebate> list = this.getStorePromotionRebates4Match(merchantId, storeId, repastDate);
        boolean wechatVisit = StorePromotionHelper.isWechatVisist(clientType);
        if (wechatVisit) {
            return this._filterBestStorePromotionRebates(timeBucketId, repastDate, clientType, takeMode, list);
        }
        return this._filterBestStorePromotionRebates(merchantId, storeId, timeBucketId, repastDate, list, loadChargeItem);
    }

    public void deleteStorePromotionRebate(int merchantId, long storeId, long promotionRebateId) throws T5weiException {
        StorePromotionRebate obj = this.storePromotionRebateDAO.loadById(merchantId, storeId, promotionRebateId, false, false);
        if (obj.getStatus() == StorePromotionStatusEnum.DOING.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_DOING.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] rebateId[" + promotionRebateId + "] doing.can not delete");
        }
        obj.makeDeleted();
    }

    /**
     * 对在有效期的活动进行进一步筛选，把符合条件的数据找出
     *
     * @param timeBucketId 营业时段id
     * @param repastDate   就餐日期
     * @param clientType   请求终端
     * @param takeMode     取餐方式
     * @param list         在有效期的活动列表
     * @return 筛选后的活动列表
     */
    private Collection<StorePromotionRebate> _filterBestStorePromotionRebates(long timeBucketId, long repastDate,
                                                                              int clientType, int takeMode, Collection<StorePromotionRebate> list) {
        List<StorePromotionRebate> olist = Lists.newArrayList();
        boolean wechatVisit = StorePromotionHelper.isWechatVisist(clientType);
        for (StorePromotionRebate promotionRebate : list) {
            if (promotionRebate.canUse(repastDate, takeMode, timeBucketId, wechatVisit)) {
                olist.add(promotionRebate);
            }
        }
        return olist;
    }

    /**
     * 对在有效期的活动进行进一步筛选，把符合条件的数据找出
     *
     * @param timeBucketId 营业时段id
     * @param repastDate   就餐日期
     * @param list         在有效期的活动列表
     * @return 筛选后的活动列表
     */
    private Collection<StorePromotionRebate> _filterBestStorePromotionRebates(int merchantId, long storeId, long timeBucketId, long repastDate,
                                                                              Collection<StorePromotionRebate> list, boolean loadChargeItem) {
        List<StorePromotionRebate> olist = Lists.newArrayList();
        for (StorePromotionRebate promotionRebate : list) {
            if (promotionRebate.canUse(repastDate, timeBucketId)) {
                olist.add(promotionRebate);
            }
        }
        if (loadChargeItem) {
            this.buildRebateRefInfo(merchantId, storeId, olist, false, false);
            this.buildChargeItemInfo(olist);
        }
        return olist;
    }

    private List<StorePromotionRebateChargeItem> _saveStorePromotionRebateChargeItems(
            int merchantId, long storeId, long promotionRebateId, List<Long> chargeItemIds, boolean isSelectChargeItem) {
        this.storePromotionRebateChargeItemDAO.deleteByPromotionRebateId(merchantId, storeId, promotionRebateId);
        if (chargeItemIds == null || chargeItemIds.isEmpty() || !isSelectChargeItem) {
            return null;
        }
        List<StorePromotionRebateChargeItem> list = Lists.newArrayList();
        List<Long> _list = StorePromotionHelper.filterDuplicateId(chargeItemIds);
        long createTime = System.currentTimeMillis();
        for (Long chargeItemId : _list) {
            StorePromotionRebateChargeItem item = new StorePromotionRebateChargeItem();
            item.setMerchantId(merchantId);
            item.setStoreId(storeId);
            item.setPromotionRebateId(promotionRebateId);
            item.setChargeItemId(chargeItemId);
            item.setCreateTime(createTime);
            list.add(item);
        }
        this.storePromotionRebateChargeItemDAO.batchCreate(list);
        return list;
    }

    private List<StorePromotionRebatePeriod> _saveStorePromotionRebatePeriods(
            int merchantId, long storeId, long promotionRebateId, List<StorePromotionPeriodParam> storePromotionPeriodParams) {
        this.storePromotionRebatePeriodDAO.deleteByPromotionRebateId(merchantId, storeId, promotionRebateId);
        if (storePromotionPeriodParams == null || storePromotionPeriodParams.isEmpty()) {
            return null;
        }
        long createTime = System.currentTimeMillis();
        List<StorePromotionRebatePeriod> list = Lists.newArrayList();
        for (StorePromotionPeriodParam param : storePromotionPeriodParams) {
            List<Integer> _weekDays = StorePromotionHelper.filterDuplicateWeekDay(param.getWeekDays());
            for (Integer weekDay : _weekDays) {
                StorePromotionRebatePeriod rebatePeriod = new StorePromotionRebatePeriod();
                rebatePeriod.setWeekDay(weekDay);
                rebatePeriod.setTimeBucketId(param.getTimeBucketId());
                rebatePeriod.setMerchantId(merchantId);
                rebatePeriod.setStoreId(storeId);
                rebatePeriod.setPromotionRebateId(promotionRebateId);
                rebatePeriod.setCreateTime(createTime);
                list.add(rebatePeriod);
            }
        }
        this.storePromotionRebatePeriodDAO.batchCreate(list);
        return list;
    }

    /**
     * 对收费项目和活动进行匹配，选取折扣优惠最多的活动。如果收费项目不开启网单折扣，就不与活动匹配
     *
     * @param storeChargeItem       收费项目
     * @param storePromotionRebates 活动list。每个活动数据必须包含相{@link StorePromotionRebateChargeItem}数据
     * @return 收费项目匹配到的活动，如果没有活动可以匹配返回null
     */
    private StorePromotionRebate matchChargeItemAndPromotion(StoreChargeItem storeChargeItem, Collection<StorePromotionRebate> storePromotionRebates) {
        if (!storeChargeItem.isEnableRebate()) {
            return null;
        }
        StorePromotionRebate minPricePromotionRebate = null;
        long minPrice = Long.MAX_VALUE;
        for (StorePromotionRebate storePromotionRebate : storePromotionRebates) {
            if (!storePromotionRebate.containChargeItem(storeChargeItem.getChargeItemId())) {
                continue;
            }
            long value = storePromotionRebate.getRebatePrice(storeChargeItem.getCurPrice());
            if (minPrice >= value) {
                minPrice = value;
                minPricePromotionRebate = storePromotionRebate;
            }

        }
        return minPricePromotionRebate;
    }

    public Map<Long, StorePromotionRebate> getStorePromotionRebateMap(int merchantId, long storeId, List<Long> promotionRebateIds) {
        return this.storePromotionRebateDAO.getMapInIds(merchantId, storeId, promotionRebateIds);
    }

    public void changePromotionRebatePaused(int merchantId, long storeId, long promotionRebateId, boolean paused) throws T5weiException {
        StorePromotionRebate promotionRebate = this.storePromotionRebateDAO.loadById(merchantId, storeId, promotionRebateId, false, false);
        if (promotionRebate.isEnded()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_ENDED.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] rebateId[" + promotionRebateId + "] ended");
        }
        promotionRebate.snapshot();
        promotionRebate.setPaused(paused);
        promotionRebate.update();
    }

    private void buildChargeItemInfo(List<StorePromotionRebate> storePromotionRebates) {
        if (storePromotionRebates == null || storePromotionRebates.isEmpty()) {
            return;
        }
        int merchantId = storePromotionRebates.get(0).getMerchantId();
        long storeId = storePromotionRebates.get(0).getStoreId();
        List<Long> chargeItemIds = Lists.newArrayList();
        for (StorePromotionRebate storePromotionRebate : storePromotionRebates) {
            if (storePromotionRebate.getChargeItems() != null) {
                for (StorePromotionRebateChargeItem rebateChargeItem : storePromotionRebate.getChargeItems()) {
                    chargeItemIds.add(rebateChargeItem.getChargeItemId());
                }
            }
        }
        Map<Long, StoreChargeItem> chargeItemMap = this.storeChargeItemService.getStoreChargeItemMapInIds(merchantId, storeId, chargeItemIds, 0, true, true);
        for (StorePromotionRebate storePromotionRebate : storePromotionRebates) {
            if (storePromotionRebate.getChargeItems() != null) {
                for (StorePromotionRebateChargeItem rebateChargeItem : storePromotionRebate.getChargeItems()) {
                    rebateChargeItem.setStoreChargeItem(chargeItemMap.get(rebateChargeItem.getChargeItemId()));
                }
            }
        }
    }

    private void buildTimeBucketInfo(List<StorePromotionRebate> storePromotionRebates) {
        if (storePromotionRebates == null || storePromotionRebates.isEmpty()) {
            return;
        }
        int merchantId = storePromotionRebates.get(0).getMerchantId();
        long storeId = storePromotionRebates.get(0).getStoreId();
        List<Long> bucketIds = Lists.newArrayList();
        for (StorePromotionRebate storePromotionRebate : storePromotionRebates) {
            if (storePromotionRebate.getPeriods() != null) {
                for (StorePromotionRebatePeriod rebatePeriod : storePromotionRebate.getPeriods()) {
                    bucketIds.add(rebatePeriod.getTimeBucketId());
                }
            }
        }
        Map<Long, StoreTimeBucket> timeBucketMap = this.storeTimeBucketDAO.getMapInIds(merchantId, storeId, bucketIds, true, true);
        for (StorePromotionRebate storePromotionRebate : storePromotionRebates) {
            if (storePromotionRebate.getPeriods() != null) {
                for (StorePromotionRebatePeriod rebatePeriod : storePromotionRebate.getPeriods()) {
                    rebatePeriod.setStoreTimeBucket(timeBucketMap.get(rebatePeriod.getTimeBucketId()));
                }
            }
        }
    }

    private void buildStaffInfo(List<StorePromotionRebate> storePromotionRebates) throws TException {
        if (storePromotionRebates == null || storePromotionRebates.isEmpty()) {
            return;
        }
        int merchantId = storePromotionRebates.get(0).getMerchantId();
        List<Long> ids = Lists.newArrayList();
        for (StorePromotionRebate promotionRebate : storePromotionRebates) {
            if (promotionRebate.getStaffId() > 0) {
                ids.add(promotionRebate.getStaffId());
            }
        }
        try {
            Map<Long, StaffDTO2> staffDTO2Map = this.staffQueryFacade.getStaffMap(merchantId, ids);
            for (StorePromotionRebate promotionRebate : storePromotionRebates) {
                promotionRebate.setStaffDTO2(staffDTO2Map.get(promotionRebate.getStaffId()));
            }
        } catch (TMerchantException e) {
            throw new T5weiException(e.getErrorCode(), e.getMessage());
        }
    }

    public Map<String, List<StorePromotionRebate>> getStorePromotionRebateMapByTitle(int merchantId, long storeId, String title, int size) throws TException {
        Map<String, List<StorePromotionRebate>> storePromotionRebateMap = new HashMap<>();
        List<StorePromotionRebate> noBeginPromotionList = this.storePromotionRebateDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.NOT_BEGIN.getValue(), title, size);
        List<StorePromotionRebate> doingPromotionList = this.storePromotionRebateDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.DOING.getValue(), title, size);
        List<StorePromotionRebate> pausedPromotionList = this.storePromotionRebateDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.PAUSED.getValue(), title, size);
        List<StorePromotionRebate> endedPromotionList = this.storePromotionRebateDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.ENDED.getValue(), title, size);

        List<StorePromotionRebate> promotionList = new ArrayList<>();
        promotionList.addAll(noBeginPromotionList);
        promotionList.addAll(doingPromotionList);
        promotionList.addAll(pausedPromotionList);
        promotionList.addAll(endedPromotionList);

        this.buildRebateInfo(merchantId, storeId, promotionList, true, true, true);

        storePromotionRebateMap.put("not_begin_list", noBeginPromotionList);
        storePromotionRebateMap.put("doing_list", doingPromotionList);
        storePromotionRebateMap.put("paused_list", pausedPromotionList);
        storePromotionRebateMap.put("ended_list", endedPromotionList);
        return storePromotionRebateMap;
    }

    public List<StorePromotionRebate> buildRebateInfo(int merchantId, long storeId, List<StorePromotionRebate> promotionRebates, boolean enableSlave, boolean enableCache, boolean needBuildRebateRefInfo) throws TException {
        if (needBuildRebateRefInfo) {
            this.buildRebateRefInfo(merchantId, storeId, promotionRebates, enableSlave, enableCache);
        }
        this.buildTimeBucketInfo(promotionRebates);
        this.buildChargeItemInfo(promotionRebates);
        this.buildStaffInfo(promotionRebates);
        this.filterChargeItem4Delete(promotionRebates);
        return promotionRebates;
    }

    private void filterChargeItem4Delete(List<StorePromotionRebate> storePromotionRebates) {
        if (storePromotionRebates == null || storePromotionRebates.isEmpty()) {
            return;
        }
        List<StorePromotionRebateChargeItem> list4Del = Lists.newArrayList();
        for (StorePromotionRebate storePromotionRebate : storePromotionRebates) {
            if (storePromotionRebate.getChargeItems() == null || storePromotionRebate.getChargeItems().isEmpty()) {
                continue;
            }
            Iterator<StorePromotionRebateChargeItem> it = storePromotionRebate.getChargeItems().iterator();
            while (it.hasNext()) {
                StorePromotionRebateChargeItem next = it.next();
                if (next.getStoreChargeItem() == null || next.getStoreChargeItem().isDeleted()) {
                    it.remove();
                    list4Del.add(next);
                }
            }
        }
        this.storePromotionRebateChargeItemDAO.deleteBatch(list4Del);
    }

    /**
     * 保存指定收费项目下的折扣活动
     *
     * @param merchantId   商户ID
     * @param storeId      店铺ID
     * @param chargeItemId 收费项目ID
     * @param rebateIds    活动ID列表
     * @param time         当前时间
     */
    public StoreRebate4ChargeItemInfo saveStorePromotionRebate4ChargeItem(int merchantId, long storeId, long chargeItemId, List<Long> rebateIds, long time, List<Long> storeChargeItemIds) {
        List<StorePromotionRebate> promotionRebates = this.storePromotionRebateDAO.getList4Doing(merchantId, storeId,
                time, false, false);
        if (!CollectionUtils.isEmpty(promotionRebates)) {
            List<Long> rebateIds4Delete = StorePromotionRebate.getIdList(promotionRebates);
            this.storePromotionRebateChargeItemDAO.deleteByChargeItemId(merchantId, storeId, chargeItemId, rebateIds4Delete);
        }
        // 将未结束的活动中，没参加的活动拆分出来
        List<StorePromotionRebate> promotionRebatesNoUse = Lists.newArrayList();
        Iterator<StorePromotionRebate> rebateIterator = promotionRebates.iterator();
        while (rebateIterator.hasNext()) {
            StorePromotionRebate next = rebateIterator.next();
            if (rebateIds == null || (next != null && !rebateIds.contains(next.getPromotionRebateId()))) {
                promotionRebatesNoUse.add(next);
                rebateIterator.remove();
            }
        }
        List<StorePromotionRebateChargeItem> storePromotionRebateChargeItemList = Lists.newArrayList();
        for (StorePromotionRebate item : promotionRebates) {
            StorePromotionRebateChargeItem rebateChargeItem = this.buildStorePromotionRebateChargeItemInfo(merchantId, storeId, item.getPromotionRebateId(), chargeItemId, time);
            storePromotionRebateChargeItemList.add(rebateChargeItem);
        }
        List<Long> rebateIds4Update = Lists.newArrayList();
        for (StorePromotionRebate item : promotionRebatesNoUse) {
            if (!item.isSelectChargeItem()) {
                // 修改收费项目的状态，isSelectChargeItem从0变成1
                rebateIds4Update.add(item.getPromotionRebateId());
                if (!CollectionUtils.isEmpty(storeChargeItemIds)) {
                    // 将收费项目与活动的关联添加到数据库中
                    for (Long id : storeChargeItemIds) {
                        StorePromotionRebateChargeItem rebateChargeItem = this.buildStorePromotionRebateChargeItemInfo(merchantId, storeId, item.getPromotionRebateId(), id, time);
                        storePromotionRebateChargeItemList.add(rebateChargeItem);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(rebateIds4Update)) {
            // 批量更新
            this.storePromotionRebateDAO.batchUpdatePromotionRebateIsSelectChargeItem(merchantId, storeId, rebateIds4Update);
        }
        if (!CollectionUtils.isEmpty(storePromotionRebateChargeItemList)) {
            // 批量更新，先删除后更新
            this.storePromotionRebateChargeItemDAO.deleteBatch(storePromotionRebateChargeItemList);
            this.storePromotionRebateChargeItemDAO.batchCreate(storePromotionRebateChargeItemList);
        }
        StoreRebate4ChargeItemInfo storeRebate4ChargeItemInfo = new StoreRebate4ChargeItemInfo();
        storeRebate4ChargeItemInfo.setStorePromotionRebateList4Use(promotionRebates);
        storeRebate4ChargeItemInfo.setStorePromotionRebateList4NoUse(promotionRebatesNoUse);
        return storeRebate4ChargeItemInfo;
    }

    public StorePromotionRebateChargeItem buildStorePromotionRebateChargeItemInfo(int merchantId, long storeId, long rebateId, long chargeItemId, long time) {
        StorePromotionRebateChargeItem rebateChargeItem = new StorePromotionRebateChargeItem();
        rebateChargeItem.setPromotionRebateId(rebateId);
        rebateChargeItem.setChargeItemId(chargeItemId);
        rebateChargeItem.setMerchantId(merchantId);
        rebateChargeItem.setStoreId(storeId);
        rebateChargeItem.setCreateTime(time);
        return rebateChargeItem;
    }

    public ChargeItemPromotionRebates getStorePromotionRebate4ChargeItemList(int merchantId, long storeId, long time, List<Long> chargeItemIds) {
        ChargeItemPromotionRebates result = new ChargeItemPromotionRebates();
        List<StorePromotionRebate> storePromotionRebateList = this.storePromotionRebateDAO.getList4Doing(merchantId, storeId, time, false, false);
        result.setStorePromotionRebateList(storePromotionRebateList);
        if (CollectionUtils.isEmpty(storePromotionRebateList) || CollectionUtils.isEmpty(chargeItemIds)) {
            return result;
        }
        List<Long> rebateIds = StorePromotionRebate.getIdList(storePromotionRebateList);
        List<StorePromotionRebateChargeItem> storePromotionRebateChargeItemList = this.storePromotionRebateChargeItemDAO.getListByRebateIds(merchantId, storeId, rebateIds);

        Map<Long, List<Long>> promotionRebateIdMap = Maps.newHashMap();
        for (StorePromotionRebateChargeItem item : storePromotionRebateChargeItemList) {
            if (!chargeItemIds.contains(item.getChargeItemId())) {
                continue;
            }
            List<Long> promotionRebateIds = promotionRebateIdMap.get(item.getChargeItemId());
            if (promotionRebateIds == null) {
                promotionRebateIds = Lists.newArrayList();
                promotionRebateIdMap.put(item.getChargeItemId(), promotionRebateIds);
            } else if (promotionRebateIds.contains(item.getPromotionRebateId())) {
                continue;
            }
            promotionRebateIds.add(item.getPromotionRebateId());
        }

        for (Long chargeItemId : chargeItemIds) {
            //将没有指定收费项目的折扣活动添加到二者的对应map中
            for (StorePromotionRebate item : storePromotionRebateList) {
                if (!item.isSelectChargeItem()) {
                    List<Long> promotionRebateIds = promotionRebateIdMap.get(chargeItemId);
                    if (promotionRebateIds == null) {
                        promotionRebateIds = Lists.newArrayList();
                        promotionRebateIdMap.put(chargeItemId, promotionRebateIds);
                    } else if (promotionRebateIds.contains(item.getPromotionRebateId())) {
                        continue;
                    }
                    promotionRebateIds.add(item.getPromotionRebateId());
                }
            }
        }
        result.setPromotionRebateIdMap(promotionRebateIdMap);
        return result;
    }

    public StoreRebate4ChargeItemInfo getStorePromotionRebate4ChargeItem(int merchantId, long storeId, long chargeItemId, long time) {
        StoreRebate4ChargeItemInfo result = new StoreRebate4ChargeItemInfo();
        List<StorePromotionRebate> storePromotionRebateList = this.storePromotionRebateDAO.getList4Doing(merchantId, storeId, time, false, false);
        if (CollectionUtils.isEmpty(storePromotionRebateList)) {
            return result;
        }
        List<Long> rebateIds = StorePromotionRebate.getIdList(storePromotionRebateList);
        List<StorePromotionRebateChargeItem> storePromotionRebateChargeItemList = this.storePromotionRebateChargeItemDAO.getListByChargeItemIdAndRebateIds(merchantId, storeId, chargeItemId, rebateIds);

        Set<Long> rebateIds4Use = Sets.newHashSet();
        for (StorePromotionRebateChargeItem item : storePromotionRebateChargeItemList) {
            rebateIds4Use.add(item.getPromotionRebateId());
        }

        for (StorePromotionRebate item : storePromotionRebateList) {
            if (!item.isSelectChargeItem()) {
                rebateIds4Use.add(item.getPromotionRebateId());
            }
        }
        List<StorePromotionRebate> rebateList4Use = Lists.newArrayList();
        Iterator<StorePromotionRebate> rebateIterator = storePromotionRebateList.iterator();
        while (rebateIterator.hasNext()) {
            StorePromotionRebate next = rebateIterator.next();
            if (rebateIds4Use.contains(next.getPromotionRebateId())) {
                rebateList4Use.add(next);
                rebateIterator.remove();
            }
        }
        result.setStorePromotionRebateList4Use(rebateList4Use);
        result.setStorePromotionRebateList4NoUse(storePromotionRebateList);
        return result;
    }
}
