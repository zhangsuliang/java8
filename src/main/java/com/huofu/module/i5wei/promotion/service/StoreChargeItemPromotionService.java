package com.huofu.module.i5wei.promotion.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisDAO;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionLimitTypeEnum;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionParam;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionQueryParam;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionRateTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderPlaceItemParam;
import huofucore.facade.i5wei.order.StoreOrderPlaceParam;
import huofucore.facade.i5wei.order.StoreOrderPromotionTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.promotion.StoreMenuChargeItemPromotionParam;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import huofuhelper.util.cache.WengerCache;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemPromotionDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.dao.StoreOrderItemPromotionDAO;
import com.huofu.module.i5wei.order.entity.StoreOrderItemPromotion;

import javax.annotation.Resource;

@Service
public class StoreChargeItemPromotionService {

	@Autowired
	private StoreChargeItemDAO storeChargeItemDAO;

	@Autowired
	private StoreChargeItemPromotionDAO storeChargeItemPromotionDAO;

	@Autowired
	private StoreOrderItemPromotionDAO storeOrderItemPromotionDAO;

	@Autowired
	private StoreChargeItemService storeChargeItemService;

	@Resource
	private WengerCache wengerCache;

	@Autowired
	private StorePromotionGratisService storePromotionGratisService;

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreChargeItemPromotion saveStoreChargeItemPromotion(StoreChargeItemPromotionParam param) throws T5weiException, TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		long chargeItemId = param.getChargeItemId();
		if (merchantId == 0 || storeId == 0 || chargeItemId == 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId + [ " + merchantId + " ], storeId [ " + storeId + " ], chargeItemId + [ " + chargeItemId + " ]");
		}
		StoreChargeItem storeChargeItem = storeChargeItemDAO.getById(merchantId, storeId, chargeItemId, false, false);
		if (storeChargeItem == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_INVALID.getValue(), "merchantId + [ " + merchantId + " ], storeId [ " + storeId + " ], chargeItemId + [ " + chargeItemId
					+ " ]");
		}
		long currentTime = System.currentTimeMillis();
		long todayTime = DateUtil.getBeginTime(currentTime, null);
		//判断是否参与了未结束的自动买赠活动
        List<StorePromotionGratis> gratisList = this.storePromotionGratisService.getStorePromotionGratisListByChargeItemId(merchantId, storeId, chargeItemId,StorePromotionGratis.PROMOTION_GRATIS_4_AUTO, currentTime);
		if (CollectionUtils.isNotEmpty(gratisList)){
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_GRATIS_CHARGE_ITEM_EXIST.getValue(), "merchantId + [ " + merchantId + " ], storeId [ " + storeId + " ], chargeItemId + [ " + chargeItemId
					+ " ]");
		}

		StoreChargeItemPromotion storeChargeItemPromotion = storeChargeItemPromotionDAO.getById(merchantId, storeId, chargeItemId, true, true);
		if (storeChargeItemPromotion == null) {
			if (param.getStartTime() > param.getEndTime()) {
				throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "startTime not greater than endTime, startTime[ " + param.getStartTime() + "], endTime[" + param.getEndTime()
						+ "]");
			}
			if (param.getStartTime() < todayTime) {
				throw new T5weiException(T5weiErrorCodeType.STORE_PROM0TION_TIME_DAY.getValue(), "startTime not Less than current time, startTime[ " + param.getStartTime());
			}
			storeChargeItemPromotion = new StoreChargeItemPromotion();
			BeanUtil.copy(param, storeChargeItemPromotion);
			storeChargeItemPromotion.setUpdateTime(currentTime);
			storeChargeItemPromotion.setCreateTime(currentTime);
			storeChargeItemPromotion.create();
		} else {
			// 活动有效时间内的已经卖出的数量
			if(param.getLimitType()==StoreChargeItemPromotionLimitTypeEnum.DAY.getValue()){
				if(param.isSetLimitDayNum()){
					storeChargeItemPromotion.setLimitDayNum(param.getLimitDayNum());
				}
			}else{
				if(param.isSetLimitNum()){
					if (param.getLimitNum() >= storeChargeItemPromotion.getSaleNum()) {
						storeChargeItemPromotion.setLimitNum(param.getLimitNum());
					} else {
						throw new T5weiException(T5weiErrorCodeType.STORE_PROM0TION_LIMITNUM_INCO.getValue(), "data format inco limitNum [" + param.getLimitNum() + "] , unable Less than " + storeChargeItemPromotion.getSaleNum());
					}
				}
			}
			// 刷新天销售数量
			if (storeChargeItemPromotion.getLimitType() == StoreChargeItemPromotionLimitTypeEnum.DAY.getValue()) {
				this.setChargeItemSaleDayNum(storeChargeItemPromotion, todayTime);
			}
			// 设置入参
			BeanUtil.copy(param, storeChargeItemPromotion, true);
			storeChargeItemPromotion.setUpdateTime(currentTime);
			try {
				storeChargeItemPromotionDAO.update(storeChargeItemPromotion);
			} catch (DuplicateKeyException e) {
				storeChargeItemPromotionDAO.update(storeChargeItemPromotion);
			}
		}
		return storeChargeItemPromotion;
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void deleteStoreChargeItemPromotion(int merchantId, long storeId, long chargeItemId) throws T5weiException, TException {
		StoreChargeItemPromotion storeChargeItemPromotion = storeChargeItemPromotionDAO.getById(merchantId, storeId, chargeItemId, true, true);
		if (storeChargeItemPromotion == null) {
			return;
		}
		long startTime = 0;
		long endTime = Long.MAX_VALUE;
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue();
		int countOrder = storeOrderItemPromotionDAO.countOrder(merchantId, storeId, chargeItemId, startTime, endTime, promotionType);
		if (countOrder > 0) {
			throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_HAS_BEEN_PROMOTION.getValue(), "merchantId[" + merchantId + "] ,storeId[" + storeId + "], chargeItemId[" + chargeItemId + "]");
		}
		storeChargeItemPromotion.delete();
	}

	public StoreChargeItemPromotion getStoreChargeItemPromotion(int merchantId, long storeId, long chargeItemId) throws T5weiException, TException {
		if (merchantId == 0 || storeId == 0 || chargeItemId == 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] ,storeId[" + storeId + "], chargeItemId[" + chargeItemId + "]");
		}
		StoreChargeItem storeChargeItem = storeChargeItemDAO.getById(merchantId, storeId, chargeItemId, false, false);
		if (storeChargeItem == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_INVALID.getValue(), "merchantId[" + merchantId + "] ,storeId[" + storeId + "], chargeItemId[" + chargeItemId + "]");
		}
		StoreChargeItemPromotion storeChargeItemPromotion = storeChargeItemPromotionDAO.getById(merchantId, storeId, chargeItemId, false, false);
		return storeChargeItemPromotion;
	}

	/**
     * key=chargeItemId value=StoreChargeItemPromotion
     */
    public Map<Long, StoreChargeItemPromotion> getChargeItemPromotionMapInIds(int merchantId, long storeId, List<Long> chargeItemIds) {
		Map<Long, StoreChargeItemPromotion> map = Maps.newHashMap();
		if (chargeItemIds == null || chargeItemIds.isEmpty()) {
			return map;
		}
		List<StoreChargeItemPromotion> list = storeChargeItemPromotionDAO.getListByIds(merchantId, storeId, chargeItemIds);
		if (list == null || list.isEmpty()) {
			return map;
		}
		for (StoreChargeItemPromotion obj : list) {
			// 更新时间不等于当天，刷新销售数量
			long todayTime = DateUtil.getBeginTime(System.currentTimeMillis(), null);
			if (DateUtil.getBeginTime(obj.getUpdateTime(), null) != todayTime) {
				this.setChargeItemSaleDayNum(obj, todayTime);
			}
			map.put(obj.getChargeItemId(), obj);
		}
		return map;
    }

	public List<StoreChargeItemPromotion> getStoreChargeItemPromotions(StoreChargeItemPromotionQueryParam param) throws T5weiException {
		List<StoreChargeItem> itemList = storeChargeItemDAO.getStoreChargeItemList(param);
		if (itemList == null || itemList.size() == 0) {
			return null;
		}
		List<Long> chargeItemIds = new ArrayList<>();
		for (StoreChargeItem item : itemList) {
			chargeItemIds.add(item.getChargeItemId());
		}
		return storeChargeItemPromotionDAO.getStoreChargeItemPromotions(param, chargeItemIds);
	}

	public List<StoreChargeItemPromotion> getStoreChargeItemPromotions(int merchantId, long storeId) throws T5weiException {
		return storeChargeItemPromotionDAO.getStoreChargeItemPromotionlist(merchantId, storeId);
	}

	public int countStoreChargeItemPromotions(StoreChargeItemPromotionQueryParam param) {
		List<StoreChargeItem> itemList = storeChargeItemDAO.getStoreChargeItemList(param);
		if (itemList == null || itemList.size() == 0) {
			return 0;
		}
		List<Long> chargeItemIds = new ArrayList<>();
		for (StoreChargeItem item : itemList) {
			chargeItemIds.add(item.getChargeItemId());
		}
		return storeChargeItemPromotionDAO.countChargeItemPromotions(param, chargeItemIds);
	}

	public StoreChargeItemPromotionStatResult getStoreChargeItemPromotionStat(int merchantId, long storeId, long chargeItemId) throws TException {
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue();
		// 查询收费项目
		StoreChargeItem storeChargeItem = storeChargeItemService.getStoreChargeItem(merchantId, storeId, chargeItemId, false, false, true, false, false, false, false, System.currentTimeMillis());
		boolean chargeItemDelete = storeChargeItem.isDeleted();
		long price = storeChargeItem.getCurPrice();
		String chargeItemName = storeChargeItem.getName();
		// 参与活动用户数量
		int countUser = storeOrderItemPromotionDAO.countUser(merchantId, storeId, chargeItemId, promotionType);
		// 活动产生的订单额度
		long countSumOrderPrice = storeOrderItemPromotionDAO.countSumOrderPrice(merchantId, storeId, chargeItemId, promotionType);
		// 活动优惠金额
		long promotionSumPrice = storeOrderItemPromotionDAO.countPromotionCouponPrice(merchantId, storeId, chargeItemId, promotionType);
		// 平均每单价格
		long averageOrderPrice = 0;
		long countOrder = storeOrderItemPromotionDAO.countOrder(merchantId, storeId, chargeItemId, promotionType);
		if (countSumOrderPrice > 0 || countOrder > 0) {
			averageOrderPrice = countSumOrderPrice / countOrder;
		}
		// 平均每人参数次数
		double averageUserFrequency = 0;
		if (countOrder > 0 && countUser > 0) {
			DecimalFormat df = new DecimalFormat("######0.00");
			averageUserFrequency = Double.parseDouble(df.format((countOrder * 0.1) / (countUser * 0.1)));
		}
		long repastDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		// 今日参与人数
		int todayUser = storeOrderItemPromotionDAO.countUser(merchantId, storeId, chargeItemId, repastDate, promotionType);
		// 今日所卖金额
		long todaySalePrice = storeOrderItemPromotionDAO.countSumOrderPrice(merchantId, storeId, chargeItemId, repastDate, promotionType);
		// 构造返回结果
		StoreChargeItemPromotionStatResult result = new StoreChargeItemPromotionStatResult();
		result.setChargeItemName(chargeItemName);
		result.setPrice(price);
		result.setChargeItemDelete(chargeItemDelete);
		result.setCountUser(countUser);
		result.setCountSumOrderPrice(countSumOrderPrice);
		result.setPromotionSumPrice(promotionSumPrice);
		result.setAverageOrderPrice(averageOrderPrice);
		result.setAverageUserFrequency(averageUserFrequency);
		result.setTodayUser(todayUser);
		result.setTodaySalePrice(todaySalePrice);
		return result;
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void updateSaleNum(int merchantId, long storeId, String orderId) {
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue();
		List<StoreOrderItemPromotion> storeOrderItemPromotions = storeOrderItemPromotionDAO.getByOrderId(merchantId, storeId, orderId,  promotionType);
		if (storeOrderItemPromotions == null || storeOrderItemPromotions.isEmpty()) {
			return;
		}
		long repastDate = storeOrderItemPromotions.get(0).getRepastDate();
		List<Long> chargeItemIds = new ArrayList<Long>();
		for (StoreOrderItemPromotion storeOrderItemPromotion : storeOrderItemPromotions) {
			chargeItemIds.add(storeOrderItemPromotion.getChargeItemId());
		}
		List<StoreChargeItemPromotion> storeChargeItemPromotions = storeChargeItemPromotionDAO.getListByIds(merchantId, storeId, chargeItemIds);
		if (storeChargeItemPromotions == null || storeChargeItemPromotions.isEmpty()) {
			return;
		}
		long currentTime = System.currentTimeMillis();
		for (StoreChargeItemPromotion storeChargeItemPromotion : storeChargeItemPromotions) {
			storeChargeItemPromotion.snapshot();
			this.setChargeItemSaleNum(storeChargeItemPromotion);
			this.setChargeItemSaleDayNum(storeChargeItemPromotion, repastDate);
			storeChargeItemPromotion.setUpdateTime(currentTime);
			storeChargeItemPromotion.update();
		}
	}

	public void updateSaleNum(int merchantId, long storeId, long chargeItemId, long repastDate) {
		StoreChargeItemPromotion storeChargeItemPromotion = storeChargeItemPromotionDAO.getById(merchantId, storeId, chargeItemId, true, true);
		storeChargeItemPromotion.snapshot();
		this.setChargeItemSaleNum(storeChargeItemPromotion);
		this.setChargeItemSaleDayNum(storeChargeItemPromotion, repastDate);
		storeChargeItemPromotion.setUpdateTime(System.currentTimeMillis());
		storeChargeItemPromotion.update();
	}

	private void setChargeItemSaleNum(StoreChargeItemPromotion storeChargeItemPromotion){
		if (storeChargeItemPromotion == null) {
			return;
		}
		int merchantId = storeChargeItemPromotion.getMerchantId();
		long storeId = storeChargeItemPromotion.getStoreId();
		long chargeItemId = storeChargeItemPromotion.getChargeItemId();
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue();
		int saleNum = storeOrderItemPromotionDAO.countByChargeItem(merchantId, storeId, chargeItemId, promotionType);
		storeChargeItemPromotion.setSaleNum(saleNum);
	}

	private void setChargeItemSaleDayNum(StoreChargeItemPromotion storeChargeItemPromotion, long repastDate){
		if (storeChargeItemPromotion == null) {
			return;
		}
		int merchantId = storeChargeItemPromotion.getMerchantId();
		long storeId = storeChargeItemPromotion.getStoreId();
		long chargeItemId = storeChargeItemPromotion.getChargeItemId();
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue();
		int saleDayNum = storeOrderItemPromotionDAO.countByChargeItemDate(merchantId, storeId, chargeItemId, repastDate, promotionType);
		if (repastDate == DateUtil.getBeginTime(System.currentTimeMillis(), null)) {
			storeChargeItemPromotion.setSaleDayNum(saleDayNum); // 当天的更新数据库
		} else {
            this.wengerCache.set(StoreChargeItemPromotion.getCacheKey(chargeItemId, repastDate), saleDayNum, StoreChargeItemPromotion.EXPIRE_SEC, false);
        }
	}

	public Map<Long, StoreChargeItemPromotion> getStoreOrderItemPromotion(StoreOrderPlaceParam storeOrderPlaceParam) {
		int merchantId = storeOrderPlaceParam.getMerchantId();
		long storeId = storeOrderPlaceParam.getStoreId();
		long repastDate = storeOrderPlaceParam.getRepastDate();
		long timeBucketId = storeOrderPlaceParam.getTimeBucketId();
		int takeMode = storeOrderPlaceParam.getTakeMode();
		long userId = storeOrderPlaceParam.getUserId();
		if (userId <= 0) {
			return Maps.newHashMap();
		}
		List<Long> chargeItemIds = new ArrayList<Long>();
		for (StoreOrderPlaceItemParam storeOrderPlaceItemParam : storeOrderPlaceParam.getChargeItems()){
			chargeItemIds.add(storeOrderPlaceItemParam.getChargeItemId());
		}
		// 单品促销设置
		Map<Long, StoreChargeItemPromotion> promotionMap = storeChargeItemPromotionDAO.getMapInIds(merchantId, storeId, chargeItemIds);
		if (promotionMap == null || promotionMap.isEmpty()) {
			return Maps.newHashMap();
		}
		// 过滤无效的收费特价
		this.removeInvalidStoreMenuChargeItemPromotion(promotionMap, chargeItemIds, repastDate, takeMode);
		// 根据消费频次过滤已经消费过的菜品定价
		Map<Long, Integer> promotionNum = this.getStoreOrderItemPromotionNum(merchantId, storeId, userId, chargeItemIds, repastDate, timeBucketId, promotionMap);
		for (long chargeItemId : chargeItemIds){
			StoreChargeItemPromotion storeChargeItemPromotion = promotionMap.get(chargeItemId);
			if (storeChargeItemPromotion == null) {
				continue;
			}
			int userPromotionNum = promotionNum.getOrDefault(chargeItemId, 0);
			if (userPromotionNum > 0){
				promotionMap.remove(chargeItemId);
			}
		}
		return promotionMap;
	}

	public Map<Long, Integer> getStoreMenuChargeItemPromotionNumMap(StoreMenuChargeItemPromotionParam param){
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		long userId = param.getUserId();
		long repastDate = param.getRepastDate();
		long timeBucketId = param.getTimeBucketId();
		int takeMode = param.getTakeMode();
		List<Long> chargeItemIds = param.getChargeItemIds();
		if (merchantId <= 0 || storeId <= 0 || userId <= 0 || repastDate <= 0 || timeBucketId <= 0){
			return Maps.newHashMap();
		}
		if (chargeItemIds == null || chargeItemIds.isEmpty()){
			return Maps.newHashMap();
		}
		Map<Long, StoreChargeItemPromotion> promotionMap = storeChargeItemPromotionDAO.getMapInIds(merchantId, storeId, chargeItemIds);
		if (promotionMap == null || promotionMap.isEmpty()) {
			return Maps.newHashMap();
		}
		// 过滤无效的收费特价
		this.removeInvalidStoreMenuChargeItemPromotion(promotionMap, chargeItemIds, repastDate, takeMode);
		Map<Long, Integer> promotionNumMap = this.getStoreOrderItemPromotionNum(merchantId, storeId, userId, chargeItemIds, repastDate, timeBucketId, promotionMap);
		return promotionNumMap;
	}

	public Map<Long, Integer> getStoreOrderItemPromotionNum(int merchantId, long storeId, long userId, List<Long> chargeItemIds, long repastDate, long timeBucketId) {
		Map<Long, StoreChargeItemPromotion> promotionMap = storeChargeItemPromotionDAO.getMapInIds(merchantId, storeId, chargeItemIds);
		return this.getStoreOrderItemPromotionNum(merchantId, storeId, userId, chargeItemIds, repastDate, timeBucketId, promotionMap);
	}

	/**
	 * 过滤无效的收费特价
	 * @param promotionMap
	 * @param chargeItemIds
	 * @param repastDate
	 * @param takeMode
	 */
	private void removeInvalidStoreMenuChargeItemPromotion(Map<Long, StoreChargeItemPromotion> promotionMap, List<Long> chargeItemIds, long repastDate, int takeMode){
		for (long chargeItemId : chargeItemIds) {
			StoreChargeItemPromotion storeChargeItemPromotion = promotionMap.get(chargeItemId);
			// 没有单品促销设置跳过
			if (storeChargeItemPromotion == null) {
				continue;
			}
			// 仅限堂食的，不为堂食不享受
			if (storeChargeItemPromotion.getSupportType() == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
				if (takeMode != StoreOrderTakeModeEnum.DINE_IN.getValue()) {
					promotionMap.remove(chargeItemId);
				}
			}
			// 不在单品促销设置有效期内不享受
			if (!storeChargeItemPromotion.isInAvailable4Time(repastDate)) {
				promotionMap.remove(chargeItemId);
			}
		}
	}

	private Map<Long, Integer> getStoreOrderItemPromotionNum(int merchantId, long storeId, long userId, List<Long> chargeItemIds, long repastDate, long timeBucketId, Map<Long, StoreChargeItemPromotion> promotionMap) {
		Map<Long, Integer> promotionNumMap = new HashMap<Long, Integer>();
		if (promotionMap == null||promotionMap.isEmpty()) {
			return promotionNumMap;
		}
		Map<Integer, List<Long>> promotionRateMap = new HashMap<Integer, List<Long>>();
		for (long chargeItemId : chargeItemIds){
			StoreChargeItemPromotion promotion = promotionMap.get(chargeItemId);
			if (promotion == null) {
				continue;
			}
			int rateType = promotion.getRateType();
			List<Long> rateChargeItemIds = promotionRateMap.get(rateType);
			if (rateChargeItemIds == null){
				rateChargeItemIds = new ArrayList<Long>();
			}
			rateChargeItemIds.add(chargeItemId);
			promotionRateMap.put(rateType, rateChargeItemIds);
		}
		for(int rateType : promotionRateMap.keySet()){
 			Map<Long, Integer> promotionRates = this.getStoreOrderItemPromotionNum(merchantId, storeId, userId, promotionRateMap.get(rateType), repastDate, timeBucketId, rateType);
			promotionNumMap.putAll(promotionRates);
		}
		return promotionNumMap;
	}

	private Map<Long, Integer> getStoreOrderItemPromotionNum(int merchantId, long storeId, long userId, List<Long> chargeItemIds, long repastDate, long timeBucketId, int rateType) {
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue();
		Map<Long, Integer> promotionNum = new HashMap<Long, Integer>();
		if (chargeItemIds == null || chargeItemIds.isEmpty()){
			return promotionNum;
		}
		if (rateType == StoreChargeItemPromotionRateTypeEnum.TIMEBUCKET_ONCE.getValue()) {
			promotionNum = storeOrderItemPromotionDAO.countByUserTimeBucketId(merchantId, storeId, userId, chargeItemIds, repastDate, timeBucketId, promotionType);
		} else if (rateType == StoreChargeItemPromotionRateTypeEnum.DAY_ONCE.getValue()) {
			promotionNum = storeOrderItemPromotionDAO.countByUserRepastDate(merchantId, storeId, userId, chargeItemIds, repastDate, promotionType);
		} else if (rateType == StoreChargeItemPromotionRateTypeEnum.WEEK_ONCE.getValue()) {
			long weekStart = DateUtil.getWeekDayStart(repastDate);
			long weekEnd = DateUtil.getWeekDayEnd(repastDate);
			promotionNum = storeOrderItemPromotionDAO.countByUserTradeTime(merchantId, storeId, userId, chargeItemIds, weekStart, weekEnd, promotionType);
		} else if (rateType == StoreChargeItemPromotionRateTypeEnum.MONTH_ONCE.getValue()) {
			long monthStart = DateUtil.getMonthDayStart(repastDate);
			long monthEnd = DateUtil.getMonthDayEnd(repastDate);
			promotionNum = storeOrderItemPromotionDAO.countByUserTradeTime(merchantId, storeId, userId, chargeItemIds, monthStart, monthEnd, promotionType);
		} else {
			promotionNum = storeOrderItemPromotionDAO.countByUser(merchantId, storeId, userId, chargeItemIds, promotionType);
		}
		return promotionNum;
	}
	public List<StoreChargeItemPromotion>  getListByIds(int merchantId, long storeId, List<Long> chargeItemIds,long repastDate){
		 if(chargeItemIds!=null){
			 return this.storeChargeItemPromotionDAO.getListByIds(merchantId, storeId, chargeItemIds, repastDate);
		 }
		 return Lists.newArrayList();
	 }

    public StoreChargeItemPromotion getByChargeItemId(int merchantId, long storeId, long chargeItemId, long time) throws T5weiException, TException {
		if (merchantId == 0 || storeId == 0 || chargeItemId == 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] ,storeId[" + storeId + "], chargeItemId[" + chargeItemId + "]");
		}
		StoreChargeItem storeChargeItem = storeChargeItemDAO.getById(merchantId, storeId, chargeItemId, false, false);
		if (storeChargeItem == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_INVALID.getValue(), "merchantId[" + merchantId + "] ,storeId[" + storeId + "], chargeItemId[" + chargeItemId + "]");
		}
		StoreChargeItemPromotion storeChargeItemPromotion = storeChargeItemPromotionDAO.getByChargeItemId(merchantId, storeId, chargeItemId, time);
		return storeChargeItemPromotion;
	}

	public List<StoreChargeItemPromotion> getList4NotEnd(int merchantId, long storeId, long time){
		return this.storeChargeItemPromotionDAO.getList4NotEnd(merchantId, storeId, time);
	}
}
