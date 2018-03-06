package com.huofu.module.i5wei.promotion.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemPriceDAO;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPrice;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisChargeItemDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisPeriodDAO;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisPeriod;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisTemp;
import com.huofu.module.i5wei.promotion.facade.StorePromotionGratisFacadeValidator;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.promotion.StorePromotionConflictFacade;
import huofucore.facade.i5wei.promotion.StorePromotionGratisParam;
import huofucore.facade.i5wei.promotion.StorePromotionPeriodParam;
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
import javax.swing.text.StyledEditorKit;
import java.util.*;

/**
 * Create By Suliang on 2016/12/23
 */
@Service
public class StorePromotionGratisService {

    @Resource
    private StorePromotionGratisDAO storePromotionGratisDAO;

    @Resource
    private StorePromotionGratisPeriodDAO storePromotionGratisPeriodDAO;

    @Resource
    private StorePromotionGratisChargeItemDAO storePromotionGratisChargeItemDAO;

    @Resource
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Resource
    private StoreChargeItemService storeChargeItemService;

    @Resource
    private StoreChargeItemPromotionService storeChargeItemPromotionService;

    @Resource
    private StoreChargeItemPriceDAO storeChargeItemPriceDAO;
    
	@Resource
	private StorePromotionGratisFacadeValidator storePromotionGratisFacadeValidator;


    @ThriftClient
    private StaffQueryFacade.Iface staffQueryFacade;

    /**
     * 根据活动ids获取活动列表
     *
     * @param merchantId
     * @param storeId
     * @param promotionIds
     * @return
     */
    public Map<Long, StorePromotionGratis> getStorePromotionGratisMap(int merchantId, long storeId, List<Long> promotionIds) {
        return this.storePromotionGratisDAO.getMapInIds(merchantId, storeId, promotionIds);
    }

    /**
     * 保存活动操作
     *
     * @param param 活动参数
     * @return 活动信息
     * @throws TException
     * @throws TMerchantException
     */
    @SuppressWarnings("all")
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StorePromotionGratis saveStorePromotionGratis(StorePromotionGratisParam param) throws TMerchantException, TException {
        List<StorePromotionPeriodParam> periodParams = param.getPeriodParams();
        List<Long> chargeItemIds = param.getChargeItemIds();
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String title = param.getTitle();
        long promotionGratisId = param.getPromotionGratisId();
        int privilegeWay = param.getPrivilegeWay();
        // 判断标题是否重复
        if (storePromotionGratisDAO.hasDuplicateNameInavaliable(merchantId, storeId, title, promotionGratisId, System.currentTimeMillis())) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_TITLE_DUPLICATE.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] promotionGratisId[" + promotionGratisId + "] title[" + title + "] duplicate");
        }
        StorePromotionGratis storePromotionGratis;
        //修改操作
        if (promotionGratisId > 0) {
            storePromotionGratis = this.storePromotionGratisDAO.loadById(merchantId, storeId, promotionGratisId, false, false);
            // 未开启的活动开始的时间不能小于当前时间
            if (storePromotionGratis.isNotBegin()) {
                long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
                if (param.getBeginTime() < today) {
                    throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId [" + merchantId + "] storeId [" + storeId + "] promotionGratisId ["
                            + param.getPromotionGratisId() + "] beginTime [" + param.getBeginTime() + "] must >= today");
                }
            }
            // 正在进行的活动不能修改活动开始时间
            if (storePromotionGratis.isDoing()) {
                if (storePromotionGratis.getBeginTime() != param.getBeginTime()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_DOING_MODIFY_BEGINTIME_FORBIDDE.getValue(), "merchantId [" + merchantId + "] storeId [" + storeId + "] promotionGratisId ["
                            + param.getPromotionGratisId() + "] doing");
                }
            }
            // 已结束的活动不能进行修改
            if (storePromotionGratis.isEnded()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_ENDED.getValue(), "merchantId [" + merchantId + "] storeId[" + storeId + "] promotionGratisId ["
                        + param.getPromotionGratisId() + "] ended");
            }
            storePromotionGratis.setUpdateTime(System.currentTimeMillis());
            storePromotionGratis.snapshot();

        } else {
        	//保存操作
            List<Long> filterChargeItemIds = this._filterStoreChargeItemIds(param.getChargeItemIds());
            // 判断人工买免还是自动买免
            if (StorePromotionGratis.PROMOTION_GRATIS_4_AUTO==privilegeWay) {
                if (this.storePromotionGratisDAO.count4Avaliable(merchantId, storeId, System.currentTimeMillis(),privilegeWay) > 5) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_LIMIT.getValue(),"merchantId ["+ merchantId + "] storeId [" + storeId + "] promotionGratis(auto) num limit");
                }
            } else {
                if (this.storePromotionGratisDAO.count4Avaliable(merchantId, storeId, System.currentTimeMillis(),StorePromotionGratis.PROMOTION_GRATIS_4_ARTIFICIAL) > 20) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_LIMIT.getValue(),"merchantId["+ merchantId + "] storeId [" + storeId + "] promotionGratis(artificial) num limit");
                }

            }
            storePromotionGratis = new StorePromotionGratis();
            if (param.getChargeItemIds() != null) {
                storePromotionGratis.setSelectChargeItem(true);
            }
        }
        // 自动买免中收费项目是否参加首单特价和买免活动
        this.storePromotionGratisFacadeValidator.validate4Activities(privilegeWay, param, storePromotionGratis);

        BeanUtil.copy(param, storePromotionGratis, true);
        //设置开始时间和结束时间
        long beginTime = DateUtil.getBeginTime(storePromotionGratis.getBeginTime(), null);
        long endTime = 0;
        if (storePromotionGratis.getEndTime() > 0) {
            if (storePromotionGratis.getEndTime() == Long.MAX_VALUE) {
                endTime = storePromotionGratis.getEndTime();
            } else {
                endTime = DateUtil.getEndTime(storePromotionGratis.getEndTime(), null);
            }
        }
        if (storePromotionGratis.isUnlimit()) {
            endTime = Long.MAX_VALUE;
        }
        storePromotionGratis.setBeginTime(beginTime);
        storePromotionGratis.setEndTime(endTime);
        //设置周期
        if (param.getPromotionGratisId() > 0) {
            storePromotionGratis.setUpdateTime(System.currentTimeMillis());
            storePromotionGratis.update();
        } else {
            storePromotionGratis.init4Create();
            storePromotionGratis.create();
        }
        // 保存周期
        if (storePromotionGratis.isSelectPeriod()) {
            storePromotionGratis.setPeriods(this._saveStorePromotionGratisPeriods(merchantId, storeId, storePromotionGratis.getPromotionGratisId(), param.getPeriodParams()));
        }
        // 保存收费项目
        if (storePromotionGratis.isSelectChargeItem()) {
            storePromotionGratis.setChargeItems(this._saveStorePromotionGratisChargeItems(merchantId, storeId, storePromotionGratis.getPromotionGratisId(), param.getChargeItemIds(), param.isSelectChargeItem()));
        }
        return storePromotionGratis;

    }


    /**
     * 根据Id获取的活动信息
     *
     * @param merchantId        商户ID
     * @param storeId           店铺ID
     * @param promotionGratisId 活动ID
     * @throws TException
     */
    public StorePromotionGratis getStorePromotionGratisInfo(int merchantId, long storeId, long promotionGratisId, boolean enableSlave, boolean enableCache) throws TException {
        StorePromotionGratis gratis = this.storePromotionGratisDAO.loadById(merchantId, storeId, promotionGratisId, enableSlave, enableCache);
        this.buildStorePromotionGratisRefInfo(merchantId, storeId, Lists.newArrayList(gratis), enableSlave, enableCache, true);
        return gratis;
    }

    /**
     * 构建活动相关信息
     * @param merchantId  商户ID
     * @param storeId  店铺ID
     * @param promotionGratisList  活动列表
     * @return
     * @throws TException
     */
    public List<StorePromotionGratis> buildStorePromotionGratisRefInfo(int merchantId, long storeId, List<StorePromotionGratis> promotionGratisList, boolean enableSlave, boolean enableCache, boolean needBuildGrarisRefInfo) throws TException {
        if (needBuildGrarisRefInfo) {
            this.buildStorePromotionGratisRefInfo(merchantId, storeId, promotionGratisList, enableSlave, enableCache);
        }
        this.buildTimeBucketInfo(promotionGratisList);
        this.buildStoreChargeItemInfo(promotionGratisList);
        this.buildStoreStaffInfo(promotionGratisList);
        this.filterStoreChargeItem4Delete(promotionGratisList);
        return promotionGratisList;
    }

    /**
     * 构建活动相关联的信息
     *
     * @param merchantId          商户ID
     * @param storeId             店铺ID
     * @param promotionGratisList 活动列表
     */
    public void buildStorePromotionGratisRefInfo(int merchantId, long storeId, List<StorePromotionGratis> promotionGratisList, boolean enableSlave, boolean enableCache) {
        List<Long> ids = StorePromotionGratis.getIds(promotionGratisList);
        Map<Long, List<StorePromotionGratisChargeItem>> charmItemListMap = storePromotionGratisChargeItemDAO.getMapInStorePromotionGratisIds(merchantId, storeId, ids, enableSlave, enableCache);
        Map<Long, List<StorePromotionGratisPeriod>> gratisPeriodListMap = storePromotionGratisPeriodDAO.getMapInStorePromotionGratisIds(merchantId, storeId, ids, enableSlave, enableCache);
        for (StorePromotionGratis storePromotionGratis : promotionGratisList) {
            storePromotionGratis.setChargeItems(charmItemListMap.get(storePromotionGratis.getPromotionGratisId()));
            storePromotionGratis.setPeriods(gratisPeriodListMap.get(storePromotionGratis.getPromotionGratisId()));
        }

    }

    /**
     * 删除买免活动
     *
     * @param merchantId        商户ID
     * @param storeId           店铺ID
     * @param promotionGratisId 活动ID
     */
    public void deleteStorePromotionGratis(int merchantId, long storeId, long promotionGratisId) throws T5weiException {
        StorePromotionGratis promotionGratis = this.storePromotionGratisDAO.loadById(merchantId, storeId, promotionGratisId, false, false);
        if (promotionGratis.getStatus() == StorePromotionStatusEnum.DOING.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_DOING.getValue(), "merchantId ["
                    + merchantId + "] storeId [" + storeId + "] promotionGratisId [" + promotionGratisId + "] doing.can not delete");
        }
        promotionGratis.makeDeleted();
    }


    /**
     * 根据标题获取不同状态下的活动列表
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param size       查询数量
     * @param title      标题
     * @return
     * @throws TException
     */
    public StorePromotionGratisTemp getStorePromotionGratisMapByTitle(int merchantId, long storeId, int size, String title) throws TException {
    	StorePromotionGratisTemp temp=new StorePromotionGratisTemp();
        List<StorePromotionGratis> notOpenedList = this.storePromotionGratisDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.NOT_OPENED.getValue(), size, title);
        List<StorePromotionGratis> notBeginList = this.storePromotionGratisDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.NOT_BEGIN.getValue(), size, title);
        List<StorePromotionGratis> doingList = this.storePromotionGratisDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.DOING.getValue(), size, title);
        List<StorePromotionGratis> pausedList = this.storePromotionGratisDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.PAUSED.getValue(), size, title);
        List<StorePromotionGratis> endedList = this.storePromotionGratisDAO.getListByTitle(merchantId, storeId, StorePromotionStatusEnum.ENDED.getValue(), size, title);

        /**
         * 将根据标题查询出不同状态下活动列表，封装成一个集合，便于构建相关联信息
         */
        List<StorePromotionGratis> promotionGratisList = Lists.newArrayList();
        promotionGratisList.addAll(notOpenedList);
        promotionGratisList.addAll(notBeginList);
        promotionGratisList.addAll(doingList);
        promotionGratisList.addAll(pausedList);
        promotionGratisList.addAll(endedList);
        this.buildStorePromotionGratisRefInfo(merchantId, storeId, promotionGratisList, true, true, true);
        temp.setNotOpenedList(notOpenedList);
        temp.setNotBeginList(notBeginList);
        temp.setDoingList(doingList);
        temp.setPausedList(pausedList);
        temp.setEndedList(endedList);
        return temp;
    }

    /**
     * 获取概要信息
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @return
     * @throws TException
     */
    public StorePromotionSummary<StorePromotionGratis> getStorePromotionGratisSummary(int merchantId, long storeId) throws TException {
        List<StorePromotionGratis> list4Avaliable = this.storePromotionGratisDAO.getList4Avaliable(merchantId, storeId, System.currentTimeMillis(), true, true);
        this.buildStorePromotionGratisRefInfo(merchantId, storeId, list4Avaliable, true, true, true);

        StorePromotionSummary<StorePromotionGratis> promotionSummary = new StorePromotionSummary<>();
        for (StorePromotionGratis storePromotionGratis : list4Avaliable) {
            promotionSummary.build(storePromotionGratis, storePromotionGratis.getStatus());
        }
        promotionSummary.setCount4Ended(this.storePromotionGratisDAO.count4Status(merchantId, storeId, StorePromotionStatusEnum.ENDED.getValue()));
        return promotionSummary;
    }

    /**
     * 改变活动的暂停状态
     *
     * @param merchantId        商户ID
     * @param storeId           店铺ID
     * @param promotionGratisId 活动ID
     * @param paused            是否暂停
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void changeStorePromotionGratisPaused(int merchantId, long storeId, long promotionGratisId, boolean paused) throws TException {
        //StorePromotionGratis promotionGratis = this.storePromotionGratisDAO.loadById(merchantId, storeId, promotionGratisId, false, false);
        StorePromotionGratis promotionGratis =  this.getStorePromotionGratisInfo(merchantId,storeId, promotionGratisId, false, false);
        if (promotionGratis.isEnded()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_ENDED.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] promotionGratisId[" + promotionGratisId + "] ended");
        }
        List<Long> chargeitemIds = StorePromotionGratis.getChargeitemIds(promotionGratis.getChargeItems());
        if(!paused){
			//1.判断当前活动下的收费项目是否参加首单特价
            List<StoreChargeItemPromotion> chargeItemPromotionList = this.storeChargeItemPromotionService.getListByIds(merchantId, storeId, chargeitemIds, System.currentTimeMillis());

            //2.判断当活动下的收费项目是否参加买免活动

		}
            promotionGratis.snapshot();
            promotionGratis.setPaused(paused);
            promotionGratis.update();
    }

    /**
     * 根据状态，以分页的形式获取活动列表
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param status     状态
     * @param pageNo     页码
     * @param pageSize   页面大小
     * @return
     * @throws TException
     */
    public PageResult getStorePromotionGratisPage(int merchantId, long storeId, int status, int pageNo, int pageSize) throws TException {
        PageResult page = new PageResult();
        int totalCount = this.storePromotionGratisDAO.count4Status(merchantId, storeId, status);
        page.setTotal(totalCount);
        if (totalCount == 0) {
            page.setList(Lists.newArrayList());
            return page;
        }
        int begin = PageUtil.getBeginIndex(pageNo, pageSize);
        List<StorePromotionGratis> list = this.storePromotionGratisDAO.getList4Status(merchantId, storeId, status, begin, pageSize);
        this.buildStorePromotionGratisRefInfo(merchantId, storeId, list, true, true, true);
        page.setList(list);
        page.setSize(pageSize);
        page.setPage(pageNo);
        page.build();
        return page;
    }

    /**
     * 获得每个收费项目的最优活动
     *
     * @param timeBucketId        营业时段ID
     * @param repastDate          就餐日期
     * @param clientType          请求终端
     * @param takeMode            就餐方式
     * @param chargeItemsList     收费项目集合
     * @param promotionGratisList 在有效期的活动
     * @return key:收费项目id value:收费项目的最优活动。如果没有活动，key中对应的收费项目id不存在
     */
    public Map<Long, List<StorePromotionGratis>> getBestStorePromotionGratisMap(long timeBucketId, long repastDate, int clientType, int takeMode, Collection<StoreChargeItem> chargeItemsList, Collection<StorePromotionGratis> promotionGratisList) {
        Map<Long, List<StorePromotionGratis>> map = Maps.newHashMap();
        if (chargeItemsList == null) {
            return map;
        }
        Collection<StorePromotionGratis> filterPromotionGratisList = this._filterBestStorePromotionGratisList(timeBucketId, repastDate, clientType, takeMode, promotionGratisList);
        List<StorePromotionGratis> promtionGratisListTemp;
        for (StoreChargeItem chargeItem : chargeItemsList) {
            promtionGratisListTemp = Lists.newArrayList();
            for (StorePromotionGratis storePromotionGratis : filterPromotionGratisList) {
                if (storePromotionGratis.containChargeItem(chargeItem.getChargeItemId())) {
                    promtionGratisListTemp.add(storePromotionGratis);
                }
            }
            if (!promtionGratisListTemp.isEmpty()) {
                map.put(chargeItem.getChargeItemId(), promtionGratisListTemp);
            }
        }
        return map;
    }

    /**
     * 获取当时时间正在进行的活动
     *
     * @param merchantId 商户ID
     * @param storeId    店铺ID
     * @param repastDate 就餐日期
     * @return
     * @throws TException
     */
    public List<StorePromotionGratis> getStorePromotionGratisList4Match(int merchantId, long storeId, long repastDate) throws TException {
        List<StorePromotionGratis> promotionGratisList = this.storePromotionGratisDAO.getList4Doing(merchantId, storeId, repastDate, true, true);
        this.buildStorePromotionGratisRefInfo(merchantId, storeId, promotionGratisList, true, true, true);
        return promotionGratisList;
    }

    /**
     * 获取最优活动，不考虑收费项目
     *
     * @param merchantId   商户ID
     * @param storeId      店铺ID
     * @param timeBucketId 营业时段ID
     * @param repastDate   就餐日期
     * @param clientType   终端方式
     * @param takeMode     取餐方式
     * @return 根据就餐相关信息查询可使用的活动
     * @throws TException
     */
    public Collection<StorePromotionGratis> getBestStorePromotionGratis(int merchantId, long storeId, long repastDate, long timeBucketId, int clientType, int takeMode, boolean loadChargeItem) throws TException {
        List<StorePromotionGratis> promotionGratisList = this.getStorePromotionGratisList4Match(merchantId, storeId, repastDate);
        boolean wechatVisit = StorePromotionHelper.isWechatVisist(clientType);
        if (wechatVisit) {
            return this._filterBestStorePromotionGratisList(timeBucketId, repastDate, clientType, takeMode, promotionGratisList);
        }
        return this._filterBestStorePromotionGratisList(merchantId, storeId, timeBucketId, repastDate, promotionGratisList, loadChargeItem);
    }

    /**
     * 根据营业时间段，就餐日期，客户端类型过滤活动
     *
     * @param timeBucketId  营业时段ID
     * @param repastDate  就餐日期
     * @param clientType  客户端类型
     * @param takeMode  取餐方式
     * @param promotionGratisListParam  活动集合
     * @return
     */
    private Collection<StorePromotionGratis> _filterBestStorePromotionGratisList(long timeBucketId, long repastDate, int clientType, int takeMode, Collection<StorePromotionGratis> promotionGratisListParam) {
        List<StorePromotionGratis> promotionGratisList = Lists.newArrayList();
        boolean wechatVisit = StorePromotionHelper.isWechatVisist(clientType);
        for (StorePromotionGratis promotionGratis : promotionGratisListParam) {
            if (promotionGratis.canUse(repastDate, takeMode, timeBucketId, wechatVisit)) {
                promotionGratisList.add(promotionGratis);
            }
        }
        return promotionGratisList;
    }

    /**
     * 根据营业时间段和就餐日期过滤活动
     *
     * @param timeBucketId             营业时间段ID
     * @param repastDate               就餐日期
     * @param promotionGratisListParam 活动集合
     * @return
     */
    private Collection<StorePromotionGratis> _filterBestStorePromotionGratisList(int merchantId, long storeId, long timeBucketId,
                                                                                 long repastDate, Collection<StorePromotionGratis> promotionGratisListParam,
                                                                                 boolean loadChargeItem) {
        List<StorePromotionGratis> promotionGratisList = Lists.newArrayList();
        for (StorePromotionGratis promotionGratis : promotionGratisListParam) {
            if (promotionGratis.canUse(repastDate, timeBucketId)) {
                promotionGratisList.add(promotionGratis);
            }
        }
        if (loadChargeItem){
            this.buildStorePromotionGratisRefInfo(merchantId, storeId, promotionGratisList, false, false);
            this.buildStoreChargeItemInfo(promotionGratisList);
        }
        return promotionGratisList;
    }

    /**
     * 获取最优活动
     *
     * @param repastDate               就餐时间
     * @param timeBucketId             时间段
     * @param clientType               客户端类型
     * @param takeMode                 取餐方式
     * @param storePromotionGratisList 活动列表
     * @return
     */
    public StorePromotionGratis getBestStorePromotionGratis(long repastDate, long timeBucketId, int clientType, int takeMode, List<StorePromotionGratis> storePromotionGratisList) {
        boolean wechatVisit = StorePromotionHelper.isWechatVisist(clientType);
        for (StorePromotionGratis storePromotionGratis : storePromotionGratisList) {
            if (storePromotionGratis.canUse(repastDate, takeMode, timeBucketId, wechatVisit)) {
                return storePromotionGratis;
            }
        }
        return null;
    }

    /**
     * 获取当前买免活动
     *
     * @param param 活动参数
     * @return
     * @throws TException
     */
    public ChargeItemGratisInfo getBestStorePromotionGratis(StorePromotionQueryParam param) throws TException {
        ChargeItemGratisInfo info = new ChargeItemGratisInfo();
        List<StoreChargeItem> chargeItemsList = this.storeChargeItemService.getStoreChargeItemsInIds(param.getMerchantId(), param.getStoreId(), param.getChargeItemIds(), param.getRepastDate(), true, true);
        List<StorePromotionGratis> promotionGratisList = this.getStorePromotionGratisList4Match(param.getMerchantId(), param.getStoreId(), param.getRepastDate());
        if (param.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
            Map<Long, List<StorePromotionGratis>> promotionGratisMap = this.getBestStorePromotionGratisMap(param.getTimeBucketId(), param.getRepastDate(), param.getClientType(), StoreOrderTakeModeEnum.DINE_IN.getValue(), chargeItemsList, promotionGratisList);
            Map<Long, List<StorePromotionGratis>> promotionGratisMap4TakeOut = this.getBestStorePromotionGratisMap(param.getTimeBucketId(), param.getRepastDate(), param.getClientType(), StoreOrderTakeModeEnum.TAKE_OUT.getValue(), chargeItemsList, promotionGratisList);
            info.setChargeItemGratisMap(promotionGratisMap);
            info.setChargeItemGratisMap4TakeOut(promotionGratisMap4TakeOut);
        } else {
            Map<Long, List<StorePromotionGratis>> promotionGratisMap = this.getBestStorePromotionGratisMap(param.getTimeBucketId(), param.getRepastDate(), param.getClientType(), param.getTakeMode(), chargeItemsList, promotionGratisList);
            Map<Long, List<StorePromotionGratis>> filterStorePromtionGratisByPrivilege = this._filterStorePromotionGratisByPrivilege(param.getClientType(), promotionGratisMap);
            if (param.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
                info.setChargeItemGratisMap4TakeOut(filterStorePromtionGratisByPrivilege);
            }
            info.setChargeItemGratisMap(filterStorePromtionGratisByPrivilege);
        }
        return info;
    }

    /**
     * 根据活动类型过滤活动列表
     *
     * @param clientType         客户端类型
     * @param promotionGratisMap 活动集合
     * @return
     */
    private Map<Long, List<StorePromotionGratis>> _filterStorePromotionGratisByPrivilege(int clientType, Map<Long, List<StorePromotionGratis>> promotionGratisMap) {
        Map<Long, List<StorePromotionGratis>> map = Maps.newHashMap();
        List<StorePromotionGratis> promotionGratisList;
        for (Long chargeItemId : promotionGratisMap.keySet()) {
            promotionGratisList = Lists.newArrayList();
            for (StorePromotionGratis storePromotionGratis : promotionGratisMap.get(chargeItemId)) {
                if ((ClientTypeEnum.CASHIER.getValue() == clientType || ClientTypeEnum.DIAN_CAI_BAO.getValue() == clientType) && storePromotionGratis.getPrivilegeWay()==StorePromotionGratis.PROMOTION_GRATIS_4_ARTIFICIAL) {
                    promotionGratisList.add(storePromotionGratis);
                } else {
                    promotionGratisList.add(storePromotionGratis);
                }
            }
            map.put(chargeItemId, promotionGratisList);
        }
        return map;
    }

    /**
     * 保存指定收费项目下的买免活动
     * @param merchantId   商户ID
     * @param storeId      店铺ID
     * @param chargeItemId 收费项目ID
     * @param gratisIds    活动ID列表
     * @param time         当前时间
     */
    public StoreGratis4ChargeItemInfo saveStorePromotionGratis4ChargeItem(int merchantId, long storeId, long chargeItemId, List<Long> gratisIds, long time, List<Long> storeChargeItemIds) throws TException {
        List<StorePromotionGratis> promotionGratises = this.storePromotionGratisDAO.getList4Doing(merchantId, storeId,
                time, false, false);
        if (!CollectionUtils.isEmpty(promotionGratises)) {
            List<Long> gratisIds4Delete = StorePromotionGratis.getIds(promotionGratises);
            this.storePromotionGratisChargeItemDAO.deleteByChargeItemId(merchantId, storeId, chargeItemId, gratisIds4Delete);
        }
        // 将未结束的活动中，没参加的活动拆分出来
        List<StorePromotionGratis> promotionGratisesNotUse = Lists.newArrayList();
        Iterator<StorePromotionGratis> gratisIterator = promotionGratises.iterator();
        while (gratisIterator.hasNext()) {
            StorePromotionGratis next = gratisIterator.next();
            if (gratisIds == null || next != null && !gratisIds.contains(next.getPromotionGratisId())) {
                promotionGratisesNotUse.add(next);
                gratisIterator.remove();
            }
        }

        // 自动买免的
        List<StorePromotionGratis> autoPromotionGratises = Lists.newArrayList();
        for (StorePromotionGratis item : promotionGratises) {
            if (item.getPrivilegeWay()==StorePromotionGratis.PROMOTION_GRATIS_4_AUTO) {
                autoPromotionGratises.add(item);
            }
        }
        if (!CollectionUtils.isEmpty(autoPromotionGratises)) {
            List<Long> autoGratisIds = StorePromotionGratis.getIds(autoPromotionGratises);
            this._validate4SaveAutoGratisIds(merchantId, storeId, autoGratisIds, chargeItemId, time); // 1.验证自动买免的互斥
        }
        List<StorePromotionGratisChargeItem> storePromotionGratisChargeItemList = Lists.newArrayList();
        for (StorePromotionGratis item : autoPromotionGratises) {
            StorePromotionGratisChargeItem storePromotionGratisChargeItem = this._buildStorePromotionGratisChargeItem(merchantId, storeId, item.getPromotionGratisId(), chargeItemId, time);
            storePromotionGratisChargeItemList.add(storePromotionGratisChargeItem);
        }

        List<Long> gratisIds4Update = Lists.newArrayList();
        for (StorePromotionGratis item : promotionGratisesNotUse) {
            if (!item.isSelectChargeItem()) {
                // 修改收费项目的状态，isSelectChargeItem从0变成1
                gratisIds4Update.add(item.getPromotionGratisId());
                if (!CollectionUtils.isEmpty(storeChargeItemIds)) {
                    storeChargeItemIds.remove(chargeItemId);
                    // 将收费项目与活动的关联添加到数据库中
                    for (Long id : storeChargeItemIds) {
                        StorePromotionGratisChargeItem storePromotionGratisChargeItem = this._buildStorePromotionGratisChargeItem(merchantId, storeId, item.getPromotionGratisId(), id, time);
                        storePromotionGratisChargeItemList.add(storePromotionGratisChargeItem);
                    }
                }
            }
        }

        if (!CollectionUtils.isEmpty(gratisIds4Update)) {
            // 批量更新
            this.storePromotionGratisDAO.batchUpdatePromotionGratisIsSelectChargeItem(merchantId, storeId, gratisIds4Update);
        }
        if (!CollectionUtils.isEmpty(storePromotionGratisChargeItemList)) {
            // 批量更新，先删除后更新
            this.storePromotionGratisChargeItemDAO.deleteBatch(storePromotionGratisChargeItemList);
            this.storePromotionGratisChargeItemDAO.batchCreate(storePromotionGratisChargeItemList);
        }

        StoreGratis4ChargeItemInfo storeGratis4ChargeItemInfo = new StoreGratis4ChargeItemInfo();
        storeGratis4ChargeItemInfo.setStorePromotionGratisList4Use(promotionGratises);
        storeGratis4ChargeItemInfo.setStorePromotionGratisList4NoUse(promotionGratisesNotUse);
        return storeGratis4ChargeItemInfo;
    }

    /**
     * 构建活动的收费项目信息
     *
     * @param merchantId  商户ID
     * @param storeId  店铺ID
     * @param promotionGratisId  活动ID
     * @param chargeItemId  收费项目ID
     * @param time  指定日期
     * @return
     */
    private StorePromotionGratisChargeItem _buildStorePromotionGratisChargeItem(int merchantId, long storeId, long promotionGratisId, long chargeItemId, long time) {
        StorePromotionGratisChargeItem storePromotionGratisChargeItem = new StorePromotionGratisChargeItem();
        storePromotionGratisChargeItem.setPromotionGratisId(promotionGratisId);
        storePromotionGratisChargeItem.setChargeItemId(chargeItemId);
        storePromotionGratisChargeItem.setMerchantId(merchantId);
        storePromotionGratisChargeItem.setStoreId(storeId);
        storePromotionGratisChargeItem.setCreateTime(time);
        return storePromotionGratisChargeItem;
    }

    /**
     * 验证一个收费项目保存的自动买免活动是否已参加了有冲突的自动买赠和首份特价
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param autoGratisIds 自动买免活动的id集合
     * @param time          查询时间
     */
    private void _validate4SaveAutoGratisIds(int merchantId, long storeId, List<Long> autoGratisIds, long chargeItemId, long time) throws TException {
        if (autoGratisIds == null || autoGratisIds.isEmpty()) {
            return;
        }
        if (autoGratisIds.size() > 1) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_INVALID.getValue(),
                    "merchantId [" + merchantId + "] storeId [" + storeId + "] chargeItemId [" + chargeItemId
                            + ". A chargeItem can not join 'Auto Gratis Activity' more than 1. gratisIds " + autoGratisIds);
        }
        // 查询自动的买免是否参加了首份特价，如果参加了，则抛错
        if (autoGratisIds.size() == 1) {
            StoreChargeItemPromotion chargeItemPromotion = this.storeChargeItemPromotionService.getByChargeItemId(merchantId, storeId, chargeItemId, time);
            if (chargeItemPromotion != null) {
                throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_INVALID.getValue(),
                        "merchantId [" + merchantId + "] storeId [" + storeId + "] chargeItemId [" + chargeItemId + "]has already joined the 'Privilege of First Order Activity', it can not join the auto gratis promotion ");
            }
        }
    }

    /**
     * 去除重复的活动ID
     *
     * @param chargeItemIds 活动ID列表
     * @return
     */
    private List<Long> _filterStoreChargeItemIds(List<Long> chargeItemIds) {
        Set<Long> set = Sets.newHashSet();
        set.addAll(chargeItemIds);

        List<Long> list = Lists.newArrayList();
        list.addAll(set);
        return list;
    }

    /**
     * 保存参加活动收费项目信息
     *
     * @param merchantId        商户ID
     * @param storeId           店铺ID
     * @param promotionGratisId 活动ID
     * @param chargeItemIds     收费项目ID集合
     * @return
     */
    private List<StorePromotionGratisChargeItem> _saveStorePromotionGratisChargeItems(int merchantId, long storeId, long promotionGratisId, List<Long> chargeItemIds, boolean isSelectChargeItem) {
        this.storePromotionGratisChargeItemDAO.deleteStorePromotionGratisId(merchantId, storeId, promotionGratisId);
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return null;
        }
        if (!isSelectChargeItem) {
            return null;
        }
        long createTime = System.currentTimeMillis();
        List<StorePromotionGratisChargeItem> list = Lists.newArrayList();
        for (Long chargeItemId : chargeItemIds) {
            StorePromotionGratisChargeItem storePromotionGratisChargeItem = new StorePromotionGratisChargeItem();
            storePromotionGratisChargeItem.setMerchantId(merchantId);
            storePromotionGratisChargeItem.setStoreId(storeId);
            storePromotionGratisChargeItem.setPromotionGratisId(promotionGratisId);
            storePromotionGratisChargeItem.setChargeItemId(chargeItemId);
            storePromotionGratisChargeItem.setCreateTime(createTime);
            list.add(storePromotionGratisChargeItem);
        }
        this.storePromotionGratisChargeItemDAO.batchCreate(list);
        return list;
    }

    /**
     * 保存活动周期
     *
     * @param merchantId        商户ID
     * @param storeId           店铺ID
     * @param promotionGratisId 活动ID
     * @param periodParams      活动周期参数
     * @return
     */
    private List<StorePromotionGratisPeriod> _saveStorePromotionGratisPeriods(int merchantId, long storeId, long promotionGratisId, List<StorePromotionPeriodParam> periodParams) {
        this.storePromotionGratisPeriodDAO.deleteByPromotionGratisId(merchantId, storeId, promotionGratisId);
        if (periodParams == null || periodParams.isEmpty()) {
            return null;
        }
        long createTime = System.currentTimeMillis();
        List<StorePromotionGratisPeriod> list = Lists.newArrayList();
        for (StorePromotionPeriodParam storePromotionPeriodParam : periodParams) {
            // 去重
            List<Integer> _weekDays = StorePromotionHelper.filterDuplicateWeekDay(storePromotionPeriodParam.getWeekDays());
            for (Integer weekDay : _weekDays) {
                StorePromotionGratisPeriod storePromotionGratisPeriod = new StorePromotionGratisPeriod();
                storePromotionGratisPeriod.setMerchantId(merchantId);
                storePromotionGratisPeriod.setStoreId(storeId);
                storePromotionGratisPeriod.setTimebucketId(storePromotionPeriodParam.getTimeBucketId());
                storePromotionGratisPeriod.setWeekDay(weekDay);
                storePromotionGratisPeriod.setPromotionGratisId(promotionGratisId);
                storePromotionGratisPeriod.setCreateTime(createTime);
                list.add(storePromotionGratisPeriod);
            }
        }
        this.storePromotionGratisPeriodDAO.batchCreate(list);
        return list;
    }

    /**
     * 构建员工关联信息
     *
     * @param promotionGratisList 活动列表
     * @throws TException
     */
    private void buildStoreStaffInfo(List<StorePromotionGratis> promotionGratisList) throws TException {
        if (promotionGratisList == null || promotionGratisList.isEmpty()) {
            return;
        }
        int merchantId = promotionGratisList.get(0).getMerchantId();
        List<Long> ids = Lists.newArrayList();
        for (StorePromotionGratis promotionGratis : promotionGratisList) {
            if (promotionGratis.getStaffId() > 0) {
                ids.add(promotionGratis.getStaffId());
            }
        }
        try {
            Map<Long, StaffDTO2> staffDTO2Map = this.staffQueryFacade.getStaffMap(merchantId, ids);
            for (StorePromotionGratis promotionGratis : promotionGratisList) {
                promotionGratis.setStaffDTO2(staffDTO2Map.get(promotionGratis.getStaffId()));
            }
        } catch (TMerchantException e) {
            throw new T5weiException(e.getErrorCode(), e.getMessage());
        }
    }

    /**
     * 过滤已经被删除的收费项目
     *
     * @param promotionGratisList 活动集合
     */
    private void filterStoreChargeItem4Delete(List<StorePromotionGratis> promotionGratisList) {
        if (promotionGratisList == null || promotionGratisList.isEmpty()) {
            return;
        }
        List<StorePromotionGratisChargeItem> list4Delete = Lists.newArrayList();
        for (StorePromotionGratis storePromotionGratis : promotionGratisList) {
            if (storePromotionGratis.getChargeItems() == null || storePromotionGratis.getChargeItems().isEmpty()) {
                continue;
            }
            Iterator<StorePromotionGratisChargeItem> iterator = storePromotionGratis.getChargeItems().iterator();
            while (iterator.hasNext()) {
                StorePromotionGratisChargeItem next = iterator.next();
                if (next.getStoreChargeItem() == null || next.getStoreChargeItem().isDeleted()) {
                    iterator.remove();
                    list4Delete.add(next);
                }
            }
        }
        this.storePromotionGratisChargeItemDAO.deleteBatch(list4Delete);
    }

    /**
     * 营业时段相关信息
     *
     * @param promotionList 活动集合
     */
    private void buildTimeBucketInfo(List<StorePromotionGratis> promotionList) {
        if (promotionList == null || promotionList.isEmpty()) {
            return;
        }
        int mechantId = promotionList.get(0).getMerchantId();
        long storeId = promotionList.get(0).getStoreId();
        List<Long> bucketIds = Lists.newArrayList();
        for (StorePromotionGratis storePromotionGratis : promotionList) {
            if (storePromotionGratis.getPeriods() != null) {
                for (StorePromotionGratisPeriod gratisPeriod : storePromotionGratis.getPeriods()) {
                    bucketIds.add(gratisPeriod.getTimeBucketId());
                }
            }

        }
        Map<Long, StoreTimeBucket> timeBucketMap = this.storeTimeBucketDAO.getMapInIds(mechantId, storeId, bucketIds, true, true);
        for (StorePromotionGratis storePromotionGratis : promotionList) {
            if (storePromotionGratis.getPeriods() != null) {
                for (StorePromotionGratisPeriod gratisPeriod : storePromotionGratis.getPeriods()) {
                    gratisPeriod.setStoreTimeBucket(timeBucketMap.get(gratisPeriod.getTimebucketId()));
                }
            }
        }

    }

    private void buildStoreChargeItemInfo(List<StorePromotionGratis> promotionGratisList) {
        if (promotionGratisList == null || promotionGratisList.isEmpty()) {
            return;
        }
        int merchantId = promotionGratisList.get(0).getMerchantId();
        long storeId = promotionGratisList.get(0).getStoreId();

        List<Long> ids = Lists.newArrayList();
        for (StorePromotionGratis storePromotionGratis : promotionGratisList) {
            if (storePromotionGratis.getChargeItems() != null) {
                for (StorePromotionGratisChargeItem gratisChargeItem : storePromotionGratis.getChargeItems()) {
                    System.out.println(gratisChargeItem);
                    ids.add(gratisChargeItem.getChargeItemId());
                }
            }
        }
        Map<Long, StoreChargeItem> chargeItemMap = storeChargeItemService.getStoreChargeItemMapInIds(merchantId, storeId, ids, 0, true, true);
        Map<Long, List<StoreChargeItemPrice>> chargeItemPriceMap = this.storeChargeItemPriceDAO.getMapGroupByChargeItemIdsForCurAndNext(merchantId, storeId, ids, System.currentTimeMillis(), true, true);
        for (StorePromotionGratis storePromotionGratis : promotionGratisList) {
            if (storePromotionGratis.getChargeItems() != null) {
                for (StorePromotionGratisChargeItem chargeItem : storePromotionGratis.getChargeItems()) {
                    chargeItem.setStoreChargeItem(chargeItemMap.get(chargeItem.getChargeItemId()));
                    if (chargeItemPriceMap != null) {
                        List<StoreChargeItemPrice> chargeItemPriceList = chargeItemPriceMap.get(chargeItem.getChargeItemId());
                        if (chargeItemPriceList != null) {
                            chargeItem.getStoreChargeItem().setCurStoreChargeItemPrice(chargeItemPriceList.get(0));
                        }
                    }

                }
            }
        }
    }

    /**
     * 判断两个集合是否相等
     *
     * @param list1
     * @param list2
     * @return
     */
    private boolean _equalList(List<Long> list1, List<Long> list2) {
        return (list1.size() == list2.size()) && list1.containsAll(list2);
    }

    public ChargeItemPromotionGratises getStorePromotionGratis4ChargeItemList(int merchantId, long storeId, long time, List<Long> chargeItemIds) {
        ChargeItemPromotionGratises result = new ChargeItemPromotionGratises();
        List<StorePromotionGratis> storePromotionGratisList = this.storePromotionGratisDAO.getList4Doing(merchantId, storeId, time, false, false);;
        result.setStorePromotionGratisList(storePromotionGratisList);
        if (CollectionUtils.isEmpty(storePromotionGratisList) || CollectionUtils.isEmpty(chargeItemIds)) {
            return result;
        }
        List<Long> gratisIds = StorePromotionGratis.getIds(storePromotionGratisList);
        List<StorePromotionGratisChargeItem> storePromotionGratisChargeItemList = this.storePromotionGratisChargeItemDAO.getListByGratisIds(merchantId, storeId, gratisIds);

        Map<Long, List<Long>> promotionGratisIdMap = Maps.newHashMap();
        for (StorePromotionGratisChargeItem item : storePromotionGratisChargeItemList) {
            if (!chargeItemIds.contains(item.getChargeItemId())) {
                continue;
            }
            List<Long> promotionGratisIds = promotionGratisIdMap.get(item.getChargeItemId());
            if (promotionGratisIds == null) {
                promotionGratisIds = Lists.newArrayList();
                promotionGratisIdMap.put(item.getChargeItemId(), promotionGratisIds);
            } else if (promotionGratisIds.contains(item.getPromotionGratisId())) {
                continue;
            }
            promotionGratisIds.add(item.getPromotionGratisId());
        }

        for (Long chargeItemId : chargeItemIds) {
            //将没有指定收费项目的折扣活动添加到二者的对应map中
            for (StorePromotionGratis item : storePromotionGratisList) {
                if (!item.isSelectChargeItem()) {
                    List<Long> promotionGratisIds = promotionGratisIdMap.get(chargeItemId);
                    if (promotionGratisIds == null) {
                        promotionGratisIds = Lists.newArrayList();
                        promotionGratisIdMap.put(chargeItemId, promotionGratisIds);
                    } else if (promotionGratisIds.contains(item.getPromotionGratisId())) {
                        continue;
                    }
                    promotionGratisIds.add(item.getPromotionGratisId());
                }
            }
        }
        result.setPromotionGratisIdMap(promotionGratisIdMap);
        return result;
    }

    public StoreGratis4ChargeItemInfo getStorePromotionGratis4ChargeItem(int merchantId, long storeId, long chargeItemId, long time){
        StoreGratis4ChargeItemInfo result = new StoreGratis4ChargeItemInfo();
        List<StorePromotionGratis> storePromotionGratisList = this.storePromotionGratisDAO.getList4Doing(merchantId, storeId, time, false, false);
        if (CollectionUtils.isEmpty(storePromotionGratisList)) {
            return result;
        }
        List<Long> gratisIds = StorePromotionGratis.getIds(storePromotionGratisList);
        List<StorePromotionGratisChargeItem> storePromotionGratisChargeItemList = this.storePromotionGratisChargeItemDAO.getListByChargeItemIdAndGratisIds(merchantId, storeId, chargeItemId, gratisIds);

        Set<Long> gratisIds4Use = Sets.newHashSet();
        for (StorePromotionGratisChargeItem item : storePromotionGratisChargeItemList){
            gratisIds4Use.add(item.getPromotionGratisId());
        }

        for (StorePromotionGratis item : storePromotionGratisList){
            if (!item.isSelectChargeItem()){
                gratisIds4Use.add(item.getPromotionGratisId());
            }
        }
        List<StorePromotionGratis> gratisList4Use = Lists.newArrayList();
        Iterator<StorePromotionGratis> gratisIterator = storePromotionGratisList.iterator();
        while (gratisIterator.hasNext()) {
            StorePromotionGratis next = gratisIterator.next();
            if (gratisIds4Use.contains(next.getPromotionGratisId())) {
                gratisList4Use.add(next);
                gratisIterator.remove();
            }
        }
        result.setStorePromotionGratisList4Use(gratisList4Use);
        result.setStorePromotionGratisList4NoUse(storePromotionGratisList);
        return result;
    }

    public List<StorePromotionGratis> getStorePromotionGratisListByPrivilegeWay(int merchantId, long storeId, int privilegeWay, long time){
        List<StorePromotionGratis> gratisList = this.storePromotionGratisDAO.getStorePromotionGratisListByPrivilegeWay(merchantId, storeId, privilegeWay, time);
        this.buildStorePromotionGratisRefInfo(merchantId, storeId, gratisList, false, false);
        this.buildStoreChargeItemInfo(gratisList);
        return gratisList;
    }

    /**
     * 获取指定收费项目参与的指定类型的买赠活动
     */
    public List<StorePromotionGratis> getStorePromotionGratisListByChargeItemId(int merchantId, long storeId, long chargeItemId, int privilegeWay, long currentTime){
        List<StorePromotionGratis> gratisList = this.storePromotionGratisDAO.getStorePromotionGratisListByPrivilegeWay(merchantId, storeId, privilegeWay, currentTime);
        if (CollectionUtils.isEmpty(gratisList)){
            return null;
        }
        List<Long> gratisIds = StorePromotionGratis.getIds(gratisList);
        List<StorePromotionGratisChargeItem> gratisChargeItemList = this.storePromotionGratisChargeItemDAO.getListByChargeItemIdAndGratisIds(merchantId, storeId, chargeItemId, gratisIds);
        if (CollectionUtils.isEmpty(gratisChargeItemList)){
            return null;
        }
        Set<Long> filterGratisIds = Sets.newHashSet();
        for (StorePromotionGratisChargeItem item : gratisChargeItemList){
            filterGratisIds.add(item.getPromotionGratisId());
        }
        if (CollectionUtils.isEmpty(filterGratisIds)){
            return null;
        }
        List<StorePromotionGratis> resultGratisList = Lists.newArrayList();
        for (StorePromotionGratis item : gratisList){
            if (filterGratisIds.contains(item.getPromotionGratisId())){
                resultGratisList.add(item);
            }
        }
        return resultGratisList;
    }

   
}
