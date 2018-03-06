package com.huofu.module.i5wei.order.service;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BeanMapper;
import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemPromotionDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderRefundItemDAO;
import com.huofu.module.i5wei.order.entity.*;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceQuota;
import com.huofu.module.i5wei.promotion.service.ChargeItemGratisInfo;
import com.huofu.module.i5wei.promotion.service.ChargeItemRebateInfo;
import com.huofu.module.i5wei.promotion.service.ChargeItemReduceInfo;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.store5weisetting.StoreCustomerAvgPaymentEnum;
import huofucore.facade.merchant.preferential.MerchantInternetRebateTypeEnum;
import huofucore.facade.merchant.preferential.MerchantPreferentialOfUserDTO;
import huofucore.facade.pay.payment.PayResultOfDynamicPayMethod;
import huofucore.facade.pay.payment.PayResultOfPayOrder;
import huofuhelper.util.MoneyUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 订单折扣计算工具类，计算各种折扣、打包费、外送费、台位费...等等
 *
 * @author chenkai
 * @since 2015-12-29
 */
@Service
public class StoreOrderPriceHelper {

	@Autowired
	private StoreOrderDAO storeOrderDAO;

	@Autowired
	private StoreOrderRefundItemDAO storeOrderRefundItemDAO;

	@Autowired
	private StoreOrderItemDAO storeOrderItemDAO;

	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@Autowired
	private StoreOrderItemPromotionDAO storeOrderItemPromotionDAO;

	@Autowired
	private StoreTimeBucketService storeTimeBucketService;

	@Autowired
	private StoreOrderService storeOrderService;

	@Autowired
	private StoreChargeItemService storeChargeItemService;

	@Autowired
	private StoreTableSettingService storeTableSettingService;


	/**
	 * 计算减免数量
	 *
	 * @param storeOrderPriceResult 价格结算结果对象
	 * @param orderPlaceItemMap     订单项目表
	 * @param orderChargeItems      定价表
	 * @param chargeItemGratisInfo  买赠活动信息
	 * @param takeMode             用餐方式
	 */
	public void computeOrderGratisAmount(StoreOrderPriceResult storeOrderPriceResult,
	                                     Map<Long, StoreOrderPlaceItemParam> orderPlaceItemMap,
	                                     List<StoreChargeItem> orderChargeItems,
	                                     ChargeItemGratisInfo chargeItemGratisInfo, int takeMode) {

		if (CollectionUtils.isEmpty(orderChargeItems) || chargeItemGratisInfo == null) {
			return;
		}
		//定价ID与活动对应map
		Map<Long, List<StorePromotionGratis>> chargeItemGratisMap;

		if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
			chargeItemGratisMap = chargeItemGratisInfo.getChargeItemGratisMap4TakeOut();
		} else {
			chargeItemGratisMap = chargeItemGratisInfo.getChargeItemGratisMap();
		}
		Map<Long, Set<Long>> gratisChargeItemIdsMap = new HashMap<Long, Set<Long>>(); //活动与收费项目列表对应map
		Map<Long, StorePromotionGratis> gratisPromotionMap = new HashMap<Long, StorePromotionGratis>(); //活动表
		Map<Long, StoreChargeItem> storeChargeItemMap = new HashMap<>(); //定价表

		//构建定价表
		for (StoreChargeItem storeChargeItem : orderChargeItems) {
			storeChargeItemMap.put(storeChargeItem.getChargeItemId(), storeChargeItem);
		}

		//活动列表
		List<StorePromotionGratis> storePromotionGratisList = new ArrayList<>();

		Set<Long> chargeItemIds = chargeItemGratisMap.keySet();
		Set<Long> storePromotionGratisIds = new TreeSet<>();
		for (Long changeItemId : chargeItemIds) {
			List<StorePromotionGratis> storePromotionGratises = chargeItemGratisMap.get(changeItemId);
			if (CollectionUtils.isEmpty(storePromotionGratises)) {
				continue;
			}
			for (StorePromotionGratis storePromotionGratis : storePromotionGratises) {
				Set<Long> gratisPromotionChargeItemIds = gratisChargeItemIdsMap.get(storePromotionGratis.getPromotionGratisId());
				if (CollectionUtils.isEmpty(gratisPromotionChargeItemIds)) {
					gratisPromotionChargeItemIds = new TreeSet<>();
				}
				gratisPromotionChargeItemIds.add(changeItemId);
				gratisChargeItemIdsMap.put(storePromotionGratis.getPromotionGratisId(), gratisPromotionChargeItemIds);
				gratisPromotionMap.put(storePromotionGratis.getPromotionGratisId(), storePromotionGratis);
				if(!storePromotionGratisIds.contains(storePromotionGratis.getPromotionGratisId())) {
					storePromotionGratisList.add(storePromotionGratis);
					storePromotionGratisIds.add(storePromotionGratis.getPromotionGratisId());
				}
			}
		}
		this.computeGratisAmount(storeOrderPriceResult, storePromotionGratisList, gratisChargeItemIdsMap, orderPlaceItemMap,
		                         storeChargeItemMap);
	}

	/**
	 * 计算赠送数量
	 *
	 * @param storePromotionGratisList 活动列表
	 * @param gratisChargeItemIdsMap   key=活动id,value=收费项目id列表
	 * @param orderPlaceItemMap        下单收费项目map
	 * @param storeChargeItemMap       菜品定价map
	 */
	private void computeGratisAmount(StoreOrderPriceResult storeOrderPriceResult, List<StorePromotionGratis> storePromotionGratisList,
	                                 Map<Long, Set<Long>> gratisChargeItemIdsMap,
	                                 Map<Long, StoreOrderPlaceItemParam> orderPlaceItemMap,
	                                 Map<Long, StoreChargeItem> storeChargeItemMap) {
		if (CollectionUtils.isEmpty(storePromotionGratisList)) {
			return;
		}
		//对活动一一进行计算
		for (StorePromotionGratis storePromotionGratis : storePromotionGratisList) {
			Set<Long> gratisPromotionChargeItemIds = gratisChargeItemIdsMap.get(storePromotionGratis.getPromotionGratisId());
			if (CollectionUtils.isEmpty(gratisPromotionChargeItemIds)) {
				continue;
			}
			List<StoreChargeItem> storeChargeItems = new ArrayList<>();
			double orderItemAmount = 0; //定价数量
			for (Long gratisPromotionChargeItemId : gratisPromotionChargeItemIds) {
				StoreOrderPlaceItemParam storeOrderPlaceItemParam = orderPlaceItemMap.get(gratisPromotionChargeItemId);
				if (storeOrderPlaceItemParam == null) {
					continue;
				}
				storeChargeItems.add(storeChargeItemMap.get(storeOrderPlaceItemParam.getChargeItemId()));
				if(storePromotionGratis.getTakeModes().size() == 1 &&
				   storePromotionGratis.getTakeModes().get(0) == StoreOrderTakeModeEnum.TAKE_OUT.getValue()){//只是自取
					orderItemAmount = orderItemAmount + storeOrderPlaceItemParam.getPackedAmount();
				}else if(!storePromotionGratis.getTakeModes().contains(StoreOrderTakeModeEnum.TAKE_OUT.getValue())){//不包含自取
					orderItemAmount = orderItemAmount + storeOrderPlaceItemParam.getAmount();
				}else {
					orderItemAmount = orderItemAmount + storeOrderPlaceItemParam.getAmount() + storeOrderPlaceItemParam.getPackedAmount();
				}
			}

			//订单没有命中免赠活动的定价项目集合
			if (CollectionUtils.isEmpty(storeChargeItems)) {
				continue;
			}

			//数量没有达到免赠标准
			if (orderItemAmount < storePromotionGratis.getPurchaseNum()) {
				continue;
			}

			//按照价格排序
			Collections.sort(storeChargeItems, new Comparator() {
				public int compare(Object a, Object b) {
					long aPrice = ((StoreChargeItem) a).getCurPrice();
					long bPrice = ((StoreChargeItem) b).getCurPrice();
					return (int) (aPrice - bPrice);
				}
			});

			//如果达到了减免的标准，先从价格较低的收费项目开始减免
			while (orderItemAmount - storePromotionGratis.getPurchaseNum() >= 0) {
				double gratisNum = storePromotionGratis.getGratisNum(); //活动赠送数量
				for (StoreChargeItem storeChargeItem : storeChargeItems) {
					if (gratisNum <= 0) { //数量减完了
						break;
					}
					StoreOrderPlaceItemParam storeOrderPlaceItemParam = orderPlaceItemMap.get(storeChargeItem.getChargeItemId());
					if(storePromotionGratis.getTakeModes().size() == 1 &&
					   storePromotionGratis.getTakeModes().get(0) == StoreOrderTakeModeEnum.TAKE_OUT.getValue()){//只是自取
						if (storeOrderPlaceItemParam.getPackedAmount() > gratisNum) {
							storeOrderPlaceItemParam.setGratisAmount(gratisNum);
							gratisNum = 0;
						} else {
							storeOrderPlaceItemParam.setGratisAmount(storeOrderPlaceItemParam.getAmount());
							gratisNum = gratisNum - storeOrderPlaceItemParam.getPackedAmount();
						}
						storeOrderPlaceItemParam.setPackedAmount(storeOrderPlaceItemParam.getPackedAmount() - storeOrderPlaceItemParam.getGratisAmount());
					}else if(!storePromotionGratis.getTakeModes().contains(StoreOrderTakeModeEnum.TAKE_OUT.getValue())){//不包含自取
						if (storeOrderPlaceItemParam.getAmount() > gratisNum) {
							storeOrderPlaceItemParam.setGratisAmount(gratisNum);
							gratisNum = 0;
						} else {
							storeOrderPlaceItemParam.setGratisAmount(storeOrderPlaceItemParam.getAmount());
							gratisNum = gratisNum - storeOrderPlaceItemParam.getAmount();
						}
						storeOrderPlaceItemParam.setAmount(storeOrderPlaceItemParam.getAmount() - storeOrderPlaceItemParam.getGratisAmount());
					}else {
						if (storeOrderPlaceItemParam.getAmount() + storeOrderPlaceItemParam.getPackedAmount() > gratisNum) {
							storeOrderPlaceItemParam.setGratisAmount(gratisNum);
							gratisNum = 0;
							double gapAmount = storeOrderPlaceItemParam.getAmount() - gratisNum;
							if(gapAmount > 0) {
								storeOrderPlaceItemParam.setAmount(gapAmount);
							}else{
								storeOrderPlaceItemParam.setAmount(0);
								storeOrderPlaceItemParam.setPackedAmount(storeOrderPlaceItemParam.getPackedAmount() - (gratisNum - storeOrderPlaceItemParam.getAmount()));
							}
						} else {
							storeOrderPlaceItemParam.setGratisAmount(storeOrderPlaceItemParam.getAmount() + storeOrderPlaceItemParam.getPackedAmount());
							gratisNum = gratisNum - storeOrderPlaceItemParam.getAmount() - storeOrderPlaceItemParam.getPackedAmount();
							storeOrderPlaceItemParam.setAmount(0);
							storeOrderPlaceItemParam.setPackedAmount(0);
						}
					}
					StoreOrderItemPromotion storeOrderItemPromotion = storeOrderPriceResult
							.getStoreOrderItemPromotion(storeChargeItem, storePromotionGratis.getPromotionGratisId(),
							                            StoreOrderPromotionTypeEnum.PROMOTION_GRATIS.getValue());
					storeOrderItemPromotion.setAmount(storeOrderPlaceItemParam.getGratisAmount());
					storeOrderItemPromotion.setChargeItemPrice(storeChargeItem.getCurPrice());
					storeOrderItemPromotion.setPromotionPrice(0);
					storeOrderItemPromotion.setPromotionDerate(MoneyUtil.mul(storeChargeItem.getCurPrice(), storeOrderItemPromotion.getAmount()));
					storeOrderPriceResult.addGratisPrice(
							MoneyUtil.mul(storeOrderItemPromotion.getChargeItemPrice(), storeOrderItemPromotion.getAmount()));
				}
				orderItemAmount = orderItemAmount - storePromotionGratis.getPurchaseNum();
			}
		}
	}

	public PlaceOrderParam getPlaceOrderParamSnapshot(PlaceOrderParam placeOrderParam){
		PlaceOrderParam placeOrderParamSnapshot = new PlaceOrderParam();
		StoreOrderPlaceParam storeOrderPlaceParam = new StoreOrderPlaceParam();
		placeOrderParamSnapshot.setStoreOrderPlaceParam(storeOrderPlaceParam);
		List<StoreOrderPlaceItemParam> storeOrderPlaceItemParams = placeOrderParam.getStoreOrderPlaceParam().getChargeItems();
		List<StoreOrderPlaceItemParam> chargeItems = new ArrayList<>();
		storeOrderPlaceParam.setChargeItems(chargeItems);
		StoreOrderPlaceItemParam chargeItem = null;
		for(StoreOrderPlaceItemParam storeOrderPlaceItemParam : storeOrderPlaceItemParams){
			chargeItem = new StoreOrderPlaceItemParam();
			chargeItems.add(chargeItem);
			chargeItem.setPackedAmount(storeOrderPlaceItemParam.getPackedAmount());
			chargeItem.setAmount(storeOrderPlaceItemParam.getAmount());
			chargeItem.setChargeItemId(storeOrderPlaceItemParam.getChargeItemId());
			chargeItem.setGratisAmount(storeOrderPlaceItemParam.getGratisAmount());
		}
		return placeOrderParamSnapshot;
	}


	public StoreOrderPriceResult getStoreOrderRebateResult(PlaceOrderParam placeOrderParam, List<StoreChargeItem> orderChargeItems)
			throws T5weiException {
		StoreOrderPriceResult storeOrderPriceResult = new StoreOrderPriceResult();

		PlaceOrderParam placeOrderParamSnapshot = getPlaceOrderParamSnapshot(placeOrderParam);

		// 入参
		StoreOrderPlaceParam storeOrderPlaceParam = placeOrderParam.getStoreOrderPlaceParam();
		MerchantPreferentialOfUserDTO rebateDto = placeOrderParam.getRebateDto();
		Store5weiSetting store5weiSetting = placeOrderParam.getStore5weiSetting();
		int clientType = storeOrderPlaceParam.getClientType();
		long userId = storeOrderPlaceParam.getUserId();
		// 点餐项目
		Map<Long, StoreOrderPlaceItemParam> orderChargeItemMap = storeOrderHelper.getChargeItemsMapOfStoreOrder(storeOrderPlaceParam);
		// 可以享受首份特价的商品
		Map<Long, StoreChargeItemPromotion> chargeItemPromotionMap = placeOrderParam.getChargeItemPromotionMap();
		// 网单折扣
		storeOrderPriceResult.setInternetRebate(rebateDto.getInternetRebate());
		// 企业折扣
		storeOrderPriceResult.setEnterpriseRebate(rebateDto.getEnterpriseRebate());
		storeOrderPriceResult.setEnterpriseRebateType(rebateDto.getEnterpriseRebateType()); // 协议企业折扣类型：0=未知, 1=普通协议企业类型, 2=火酷折扣类型
		// 满减活动
		ChargeItemReduceInfo chargeItemReduceInfo = placeOrderParam.getChargeItemReduceInfo();
		// 整单折扣&减免
		storeOrderPriceResult.setTotalRebate(storeOrderPlaceParam.getTotalRebate());
		storeOrderPriceResult.setTotalDerate(storeOrderPlaceParam.getTotalDerate());

		//买赠活动
		this.computeOrderGratisAmount(storeOrderPriceResult, orderChargeItemMap, orderChargeItems,
		                              placeOrderParam.getChargeItemGratisInfo(), placeOrderParam.getStoreOrderPlaceParam().getTakeMode());
		// 订单项目价格计算
		for (StoreChargeItem storeChargeItem : orderChargeItems) {

			long chargeItemId = storeChargeItem.getChargeItemId();
			// 点餐订单项目
			StoreOrderPlaceItemParam itemParam = orderChargeItemMap.get(chargeItemId);

			//如果订单项目点餐数目为0，则不参与计算
			if(itemParam.getAmount() == 0 && itemParam.getPackedAmount() == 0){
				continue;
			}

			// 此菜品定价折总共优惠的金额
			long itemRebatePriceDerate = 0;
			// 订单项目最低折扣（打包部分）
			StoreOrderItemMinRebate packedItemMinRebate = null;
			double itemPackedAmount = itemParam.getPackedAmount();
			if (itemPackedAmount > 0) {
				packedItemMinRebate = this.getStoreOrderItemMinPromotionType(storeChargeItem, itemPackedAmount, placeOrderParam, true);
			}
			// 订单项目最低折扣（非打包部分）
			StoreOrderItemMinRebate unpackedItemMinRebate = null;
			double itemUnPackedAmount = NumberUtil.sub(itemParam.getAmount(), itemParam.getPackedAmount());
			if (itemUnPackedAmount > 0) {
				unpackedItemMinRebate = this.getStoreOrderItemMinPromotionType(storeChargeItem, itemUnPackedAmount, placeOrderParam, false);
			}
			// 订单项目最低折扣
			StoreOrderItemMinRebate itemMinRebate = this.minItemRebate(packedItemMinRebate, unpackedItemMinRebate);
			// 首份特价减免
			StoreChargeItemPromotion storeChargeItemPromotion = chargeItemPromotionMap.get(chargeItemId);
			if (this.isInternetClientType(userId, clientType) && storeChargeItemPromotion != null) {
				// 必须是网单
				long itemRebatePartDerate =
						this.calculateOrderPromotionItemDerate(storeChargeItem, storeChargeItemPromotion, storeOrderPriceResult,
						                                       itemMinRebate, chargeItemReduceInfo);
				itemRebatePriceDerate = MoneyUtil.add(itemRebatePriceDerate, itemRebatePartDerate);
			}
			// 计算最低折扣（打包和非打包分别享受不同的折扣）
			long itemRebatePartDerate =
					this.calculateOrderItemRebate(storeChargeItem, storeOrderPriceResult, itemMinRebate, chargeItemReduceInfo);
			// 计入菜品定价的总优惠金额
			itemRebatePriceDerate = MoneyUtil.add(itemRebatePriceDerate, itemRebatePartDerate);
			// 计算最低折扣之外的其他部分最低折扣 
			StoreOrderItemMinRebate anotherItemMinRebate = null;
			if (itemMinRebate.isPacked()) {
				anotherItemMinRebate = unpackedItemMinRebate;
			} else {
				anotherItemMinRebate = packedItemMinRebate;
			}
			itemRebatePartDerate =
					this.calculateOrderItemRebate(storeChargeItem, storeOrderPriceResult, anotherItemMinRebate, chargeItemReduceInfo);
			itemRebatePriceDerate = MoneyUtil.add(itemRebatePriceDerate, itemRebatePartDerate);
			// 菜品定价计入订单价格
			long storeOrderItemPrice = this._getStoreOrderItemPrice(itemParam.getAmount(), storeChargeItem);

			storeOrderPriceResult.addOrderPrice(storeOrderItemPrice);
			storeOrderPriceResult.addOrderItemPrice(storeOrderItemPrice);
			// 菜品定价订单折后价
			storeOrderPriceResult.subFavorablePrice(itemRebatePriceDerate);
		}
		// 订单折后价 
		storeOrderPriceResult.addFavorablePrice(storeOrderPriceResult.getOrderPrice());
		// 计算打包费，由于买免活动修改了订单数量，所以用快照来计算打包费
		StoreOrderPackageFeeResult storeOrderPackageFeeResult = this.getStoreOrderPackageFee(placeOrderParamSnapshot, orderChargeItems);
		storeOrderPriceResult.setProducePackageFee(storeOrderPackageFeeResult.isProducePackageFee());
		storeOrderPriceResult.setPackageFee(storeOrderPackageFeeResult.getPackageFee());
		storeOrderPriceResult.addOrderPrice(storeOrderPackageFeeResult.getPackageFee());
		storeOrderPriceResult.addFavorablePrice(storeOrderPackageFeeResult.getPackageFee());
		// 非桌台的入客数&台位费（桌台的入客数和台位费在TableRecord中记录）
		// 获取店铺桌台设置
		StoreTableSetting
				storeTableSetting = storeTableSettingService
				.getStoreTableSetting(storeOrderPlaceParam.getStoreId(), storeOrderPlaceParam.getMerchantId(), false);
		if (storeOrderPlaceParam.getTableRecordId() == 0 && !storeTableSetting.isEnableTableMode()) {
			int customerTraffic = this.getCustomerTraffic(storeOrderPlaceParam, store5weiSetting, orderChargeItemMap, orderChargeItems);
			storeOrderPriceResult.setCustomerTraffic(customerTraffic);
			if (store5weiSetting.isEnableTableFee()) {
				long tableFee = this.getTableFee(storeOrderPlaceParam, customerTraffic);
				storeOrderPriceResult.setTableFee(tableFee);
				storeOrderPriceResult.addOrderPrice(tableFee);
				storeOrderPriceResult.addFavorablePrice(tableFee);
			}
		}

		// 满减活动减免金额
		StorePromotionReduce storePromotionReduce = storeOrderPriceResult.getStorePromotionReduce();
		if (storePromotionReduce != null && storeOrderPriceResult.getPromotionReduceAmount() > 0 &&
		    this.isClientSupport(storePromotionReduce.isWechatOnly(), clientType)) {
			long promotionReduceAmount = 0L;
			if (this.sharedPromotionReduce(storeOrderPriceResult, storePromotionReduce.isShared())) {
				promotionReduceAmount = storeOrderPriceResult.getPromotionReduceAmount();
			} else {
				promotionReduceAmount = storeOrderPriceResult.getPromotionReduceAmountNotShared();
			}
			StorePromotionReduceQuota storePromotionReduceQuota = storePromotionReduce.getBestReduceQuota(promotionReduceAmount);
			if (storePromotionReduceQuota != null) {
				storeOrderPriceResult.setPromotionReduceAmount(promotionReduceAmount);
				storeOrderPriceResult.setStorePromotionReduceQuota(storePromotionReduceQuota);
				storeOrderPriceResult.setPromotionReduceQuota(storePromotionReduceQuota.getQuotaPrice());
				storeOrderPriceResult.setPromotionReducePrice(storePromotionReduceQuota.getReducePrice());
				storeOrderPriceResult.subFavorablePrice(storePromotionReduceQuota.getReducePrice());
			}
		}
		if (storeOrderPriceResult.getPromotionReducePrice() <= 0) {
			// 不享受满减活动的，满减活动折扣归零
			this._rejectStoreOrderChangeItemPromotions(storeOrderPriceResult, StoreOrderPromotionTypeEnum.PROMOTION_REDUCE);
			storeOrderPriceResult.setPromotionReduceAmount(0);
		}
		// 整单折扣价（整单减免之前的订单折后价）
		storeOrderPriceResult.setTotalPrice(storeOrderPriceResult.getFavorablePrice());
		long _totalRebatePrice =
				this._getTotalRebateResultPrice(storeOrderPriceResult.getTotalPrice(), storeOrderPriceResult.getTotalRebate(),
				                                storeOrderPriceResult.getTotalDerate());
		// 如果整单折扣比已计算的订单折扣大，则采用整单折扣。 (根据目前的业务流程非网单只能是：整单折扣、企业折扣)
		if (!this.isInternetClientType(userId, clientType) && _totalRebatePrice > 0) {
			// 使用整单减免
			storeOrderPriceResult.subFavorablePrice(_totalRebatePrice);
		} else {
			// 不采用整单减免
			storeOrderPriceResult.setTotalRebate(100D);
			storeOrderPriceResult.setTotalDerate(0);
			storeOrderPriceResult.setTotalRebatePrice(0);
		}
		// 已下线字段
		storeOrderPriceResult.setUserClientCoupon(0); // 扣减客户端优惠，已废弃
		storeOrderPriceResult.setRebateType(0); // 整个订单可能享受多种折扣，已废弃
		complementOrderData(placeOrderParamSnapshot,storeOrderPriceResult,placeOrderParam.getStoreOrderPlaceParam().getChargeItems()); //补齐买免订单数据
		return storeOrderPriceResult;
	}

	public void complementOrderData(PlaceOrderParam placeOrderParamSnapshot,StoreOrderPriceResult storeOrderPriceResult, List<StoreOrderPlaceItemParam> storeOrderPlaceItemParams){
		storeOrderPriceResult.setOrderPrice(storeOrderPriceResult.getGratisPrice() + storeOrderPriceResult.getOrderPrice()); //补齐免价额度
		//补齐订单项购买总数
		Map<Long, StoreOrderPlaceItemParam> orderChargeItemMapSnapshot = storeOrderHelper.getChargeItemsMapOfStoreOrder(placeOrderParamSnapshot.getStoreOrderPlaceParam());
		if(CollectionUtils.isNotEmpty(storeOrderPlaceItemParams)){
			for(StoreOrderPlaceItemParam item : storeOrderPlaceItemParams){
				StoreOrderPlaceItemParam storeOrderPlaceItemParamSnapshot = orderChargeItemMapSnapshot.get(item.getChargeItemId());
				if(storeOrderPlaceItemParamSnapshot != null) {
					item.setAmount(storeOrderPlaceItemParamSnapshot.getAmount());
					item.setPackedAmount(storeOrderPlaceItemParamSnapshot.getPackedAmount());
				}
			}
		}
	}

	public boolean isInternetClientType(long userId, int clientType) {
		if (clientType == ClientTypeEnum.CASHIER.getValue() || clientType == ClientTypeEnum.DIAN_CAI_BAO.getValue()) {
			return false;
		}
		if (userId > 0) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isClientSupport(boolean wechatOnly, int clientType) {
		if (wechatOnly) {
			if (clientType == ClientTypeEnum.WECHAT.getValue() || clientType == ClientTypeEnum.MOBILEWEB.getValue()) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * 满减活动是否支持与其他活动共享<br>
	 * 其他活动指：首份特价、折扣活动、企业折扣、微信自助下单折扣等所有优惠活动
	 *
	 * @param storeOrderPriceResult
	 * @param shared
	 * @return
	 */
	public boolean sharedPromotionReduce(StoreOrderPriceResult storeOrderPriceResult, boolean shared) {
		if (shared) {
			return true;
		}
		if (storeOrderPriceResult.getPromotionPrice() > 0) {
			return false;
		}
		if (storeOrderPriceResult.getPromotionRebatePrice() > 0) {
			return false;
		}
		if (storeOrderPriceResult.getMemberRebatePrice() > 0) {
			return false;
		}
		if (storeOrderPriceResult.getInternetRebatePrice() > 0) {
			return false;
		}
		if (storeOrderPriceResult.getEnterpriseRebatePrice() > 0) {
			return false;
		}
		return true;
	}

	/**
	 * 获得订单项目的最低折扣
	 */
	private StoreOrderItemMinRebate getStoreOrderItemMinPromotionType(StoreChargeItem storeChargeItem, double chargeItemRebateAmount,
	                                                                  PlaceOrderParam placeOrderParam, boolean packed) {
		if (chargeItemRebateAmount <= 0) {
			return null;
		}
		// 计算折扣策略（最小折扣，其中折扣活动可能出现混合）
		long promotionRebateId = 0;
		long chargeItemRebateDerate = 0L; // 菜品定价折扣减免，例如：20元，打折后18元，那么折扣减免为2元
		StoreOrderPromotionTypeEnum chargeItemPromotionType = null; //菜品定价的折扣类型
		StoreOrderPlaceParam storeOrderPlaceParam = placeOrderParam.getStoreOrderPlaceParam();
		int clientType = storeOrderPlaceParam.getClientType();
		long userId = storeOrderPlaceParam.getUserId();
		int takeMode = storeOrderPlaceParam.getTakeMode();
		if (clientType == ClientTypeEnum.CASHIER.getValue() || clientType == ClientTypeEnum.DIAN_CAI_BAO.getValue()) {
			if (packed) {
				takeMode = StoreOrderTakeModeEnum.TAKE_OUT.getValue();
			} else {
				takeMode = StoreOrderTakeModeEnum.DINE_IN.getValue();
			}
		}
		MerchantPreferentialOfUserDTO rebateDto = placeOrderParam.getRebateDto();
		// 收费项目
		long chargeItemId = storeChargeItem.getChargeItemId();
		long chargeItemPrice = storeChargeItem.getCurPrice();
		// 网单折扣
		double internetRebate = rebateDto.getInternetRebate();
		// 企业折扣
		double enterpriseRebate = rebateDto.getEnterpriseRebate();
		// 可享受折扣活动的商品
		ChargeItemRebateInfo chargeItemRebateInfo = placeOrderParam.getChargeItemRebateInfo();
		// 收费项目的会员价（单价）
		long memberPriceDerate = 0;
		if (this.isInternetClientType(userId, clientType) &&
		    this.checkMerchantInternetRebateType(rebateDto, MerchantInternetRebateTypeEnum.MEMBER_PRICE)) {
			if (storeChargeItem.getMemberPrice() > 0 && storeChargeItem.getMemberPrice() < chargeItemPrice) {
				memberPriceDerate = MoneyUtil.sub(chargeItemPrice, storeChargeItem.getMemberPrice());
			}
		}
		if (memberPriceDerate > 0) {
			if (chargeItemRebateDerate < memberPriceDerate) {
				chargeItemRebateDerate = memberPriceDerate;
				chargeItemPromotionType = StoreOrderPromotionTypeEnum.MEMBER;
			}
		}
		// 收费项目的网单折扣价格（单价）
		long internetPriceDerate = 0;
		if (storeChargeItem.isEnableRebate() && this.isInternetClientType(userId, clientType) &&
		    this.checkMerchantInternetRebateType(rebateDto, MerchantInternetRebateTypeEnum.REBATE)) {
			// 必须是网单
			internetPriceDerate = this._getRebateResultPrice(chargeItemPrice, internetRebate);
		}
		if (internetPriceDerate > 0) {
			if (chargeItemRebateDerate < internetPriceDerate) {
				chargeItemRebateDerate = internetPriceDerate;
				chargeItemPromotionType = StoreOrderPromotionTypeEnum.INTERNET;
			}
		}
		// 收费项目的企业折扣价格（单价）
		long enterprisePriceDerate = 0;
		if (storeChargeItem.isEnableRebate() && userId > 0) {
			//有用户ID即可享受
			enterprisePriceDerate = this._getRebateResultPrice(chargeItemPrice, enterpriseRebate);
		}
		if (enterprisePriceDerate > 0) {
			if (chargeItemRebateDerate < enterprisePriceDerate) {
				chargeItemRebateDerate = enterprisePriceDerate;
				chargeItemPromotionType = StoreOrderPromotionTypeEnum.ENTERPRISE;
			}
		}
		// 收费项目的折扣活动（单价），目前网单不会有打包外卖混合的情况
		long promotionRebateDerate = 0;
		StorePromotionRebate storePromotionRebate = new StorePromotionRebate();
		if (chargeItemRebateInfo != null) {
			storePromotionRebate = chargeItemRebateInfo.getStorePromotionRebate(chargeItemId, takeMode);
		}
		if (storeChargeItem.isEnableRebate() && storePromotionRebate != null &&
		    this.isClientSupport(storePromotionRebate.isWechatOnly(), clientType)) {
			promotionRebateDerate = this._getRebateResultPrice(chargeItemPrice, storePromotionRebate.getRebate());
		}
		if (promotionRebateDerate > 0) {
			if (chargeItemRebateDerate < promotionRebateDerate) {
				chargeItemRebateDerate = promotionRebateDerate;
				chargeItemPromotionType = StoreOrderPromotionTypeEnum.PROMOTION_REBATE;
				promotionRebateId = storePromotionRebate.getPromotionRebateId();
			}
		}
		// 返回计算结果
		StoreOrderItemMinRebate storeOrderItemMinRebate = new StoreOrderItemMinRebate();
		storeOrderItemMinRebate.setPromotionRebateId(promotionRebateId);
		storeOrderItemMinRebate.setStorePromotionRebate(storePromotionRebate);
		storeOrderItemMinRebate.setChargeItemRebateDerate(chargeItemRebateDerate);
		storeOrderItemMinRebate.setChargeItemPromotionType(chargeItemPromotionType);
		storeOrderItemMinRebate.setChargeItemRebateAmount(chargeItemRebateAmount);
		storeOrderItemMinRebate.setChargeItemRebateDerate(chargeItemRebateDerate);
		storeOrderItemMinRebate.setPacked(packed);
		storeOrderItemMinRebate.setClientType(clientType);
		storeOrderItemMinRebate.setTakeMode(takeMode);
		return storeOrderItemMinRebate;
	}

	private boolean checkMerchantInternetRebateType(MerchantPreferentialOfUserDTO rebateDto,
	                                                MerchantInternetRebateTypeEnum targetInternetRebateType) {
		if (rebateDto == null || targetInternetRebateType == null) {
			return false;
		}
		if (targetInternetRebateType.getValue() == rebateDto.getInternetRebateType()) {
			return true;
		}
		return false;
	}

	/**
	 * 返回订单项目最低的折扣，打包&非打包必然有一个不为空
	 */
	private StoreOrderItemMinRebate minItemRebate(StoreOrderItemMinRebate packedItemMinRebate,
	                                              StoreOrderItemMinRebate unpackedItemMinRebate) {
		if (packedItemMinRebate == null) {
			// 打包为空，则返回非打包
			return unpackedItemMinRebate;
		}
		if (unpackedItemMinRebate == null) {
			// 非打包为空，则返回打包
			return packedItemMinRebate;
		}
		if (unpackedItemMinRebate.getChargeItemRebateDerate() < packedItemMinRebate.getChargeItemRebateDerate()) {
			// 打包的较小，则返回打包
			return packedItemMinRebate;
		} else {
			// 其他则返回非打包
			return unpackedItemMinRebate;
		}
	}

	/**
	 * 计算订单项目的首份特价
	 *
	 * @param storeChargeItem          收费项目
	 * @param storeChargeItemPromotion 首份特价
	 * @param storeOrderPriceResult    价格结果
	 * @param itemPromotionRebate      订单项目最低折扣
	 * @param chargeItemReduceInfo     满减活动信息
	 * @return
	 */
	private long calculateOrderPromotionItemDerate(StoreChargeItem storeChargeItem, StoreChargeItemPromotion storeChargeItemPromotion,
	                                               StoreOrderPriceResult storeOrderPriceResult, StoreOrderItemMinRebate itemPromotionRebate,
	                                               ChargeItemReduceInfo chargeItemReduceInfo) {
		if (itemPromotionRebate == null) {
			return 0;
		}
		long chargeItemPrice = storeChargeItem.getCurPrice();
		long promotionItemPriceDerate = 0;
		long promotionPrice = storeChargeItemPromotion.getPromotionPrice();
		promotionItemPriceDerate = MoneyUtil.sub(chargeItemPrice, promotionPrice);
		if (promotionItemPriceDerate > 0 && itemPromotionRebate.getChargeItemRebateDerate() < promotionItemPriceDerate) {
			long promotionId = storeChargeItemPromotion.getPromotionId();
			int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue();
			// 首份特价默认一份
			double promotionItemAmount = 1D;
			// 进入此订单首份特价减免金额
			storeOrderPriceResult.addPromotionPrice(promotionItemPriceDerate);
			// 首份特价活动促销信息
			StoreOrderItemPromotion promotionChargeItem =
					storeOrderPriceResult.getStoreOrderItemPromotion(storeChargeItem, promotionId, promotionType);
			promotionChargeItem.setAmount(promotionChargeItem.getAmount() + promotionItemAmount);
			promotionChargeItem.setPromotionPrice(promotionPrice);
			promotionChargeItem.setPromotionDerate(promotionItemPriceDerate);
			// 加入所有参与首份特价的商品ID列表
			storeOrderPriceResult.getPromotionChargeItemIds().add(storeChargeItem.getChargeItemId());
			// 加入首份特价则不参与其他折扣
			double chargeItemRebateAmount = itemPromotionRebate.getChargeItemRebateAmount();
			itemPromotionRebate.setChargeItemRebateAmount(NumberUtil.sub(chargeItemRebateAmount, promotionItemAmount));
			// 计入符合满减活动的收费项目价格
			this.calculateStoreOrderCanReduceAndCouponPrice(storeOrderPriceResult, itemPromotionRebate, chargeItemReduceInfo,
			                                                storeChargeItem, promotionItemAmount, promotionPrice);
		}
		// 计入此菜品减免金额
		return promotionItemPriceDerate;
	}

	/**
	 * 运算订单项目折扣价
	 *
	 * @return
	 */
	private long calculateOrderItemRebate(StoreChargeItem storeChargeItem, StoreOrderPriceResult storeOrderPriceResult,
	                                      StoreOrderItemMinRebate itemPromotionRebate, ChargeItemReduceInfo chargeItemReduceInfo) {
		if (itemPromotionRebate == null) {
			return 0;
		}
		// 参数
		long chargeItemPrice = storeChargeItem.getCurPrice();
		long promotionPrice = MoneyUtil.sub(chargeItemPrice, itemPromotionRebate.getChargeItemRebateDerate());
		double itemSaleAmount = itemPromotionRebate.getChargeItemRebateAmount();
		// 菜品定价参与此折扣的金额
		long itemRebatePartAmount = MoneyUtil.mul(chargeItemPrice, itemSaleAmount);
		// 菜品定价参与此折扣的优惠金额
		long itemRebatePartDerate = MoneyUtil.mul(itemPromotionRebate.getChargeItemRebateDerate(), itemSaleAmount);
		// 计算可享受优惠券和满减活动的金额
		long itemSalePrice = MoneyUtil.sub(itemRebatePartAmount, itemRebatePartDerate);
		this.calculateStoreOrderCanReduceAndCouponPrice(storeOrderPriceResult, itemPromotionRebate, chargeItemReduceInfo, storeChargeItem,
		                                                itemSaleAmount, itemSalePrice);
		// 菜品定价折扣额度汇入总的折扣额度
		if (itemSaleAmount <= 0 || itemPromotionRebate.getChargeItemPromotionType() == null ||
		    itemPromotionRebate.getChargeItemRebateDerate() <= 0) {
			return 0;
		}
		// 促销折扣类型
		long promotionId = 0;
		int promotionType = itemPromotionRebate.getChargeItemPromotionType().getValue();
		if (itemPromotionRebate.getChargeItemPromotionType() == StoreOrderPromotionTypeEnum.MEMBER) {
			storeOrderPriceResult.addMemberRebatePrice(itemRebatePartDerate);
		} else if (itemPromotionRebate.getChargeItemPromotionType() == StoreOrderPromotionTypeEnum.INTERNET) {
			storeOrderPriceResult.addInternetRebateAmount(itemRebatePartAmount);
			storeOrderPriceResult.addInternetRebatePrice(itemRebatePartDerate);
		} else if (itemPromotionRebate.getChargeItemPromotionType() == StoreOrderPromotionTypeEnum.ENTERPRISE) {
			storeOrderPriceResult.addEnterpriseRebateAmount(itemRebatePartAmount);
			storeOrderPriceResult.addEnterpriseRebatePrice(itemRebatePartDerate);
		}
		// 折扣促销信息
		StoreOrderItemPromotion storeOrderItemPromotion =
				storeOrderPriceResult.getStoreOrderItemPromotion(storeChargeItem, promotionId, promotionType);
		storeOrderItemPromotion.setAmount(itemSaleAmount);
		storeOrderItemPromotion.setPromotionPrice(promotionPrice);
		storeOrderItemPromotion.setPromotionDerate(itemRebatePartDerate);
		// 计入菜品定价的总优惠金额
		return itemRebatePartDerate;
	}

	/**
	 * 计算可享受优惠券和满减活动的金额
	 */
	private void calculateStoreOrderCanReduceAndCouponPrice(StoreOrderPriceResult storeOrderPriceResult,
	                                                        StoreOrderItemMinRebate itemPromotionRebate,
	                                                        ChargeItemReduceInfo chargeItemReduceInfo, StoreChargeItem storeChargeItem,
	                                                        double itemSaleAmount, long itemSalePrice) {
		int clientType = itemPromotionRebate.getClientType();
		StorePromotionReduce storePromotionReduce = new StorePromotionReduce();
		if (chargeItemReduceInfo != null) {
			storePromotionReduce = chargeItemReduceInfo.getBestStorePromotionReduce(itemPromotionRebate.getTakeMode());
		}
		if (storePromotionReduce != null && this.isClientSupport(storePromotionReduce.isWechatOnly(), clientType) && itemSalePrice > 0) {
			storeOrderPriceResult.setStorePromotionReduce(storePromotionReduce);
			storeOrderPriceResult.setPromotionReduceId(storePromotionReduce.getPromotionReduceId());
			if (storePromotionReduce.containChargeItem(storeChargeItem.getChargeItemId())) {
				long reducePromotionId = storePromotionReduce.getPromotionReduceId();
				int reducePromotionType = StoreOrderPromotionTypeEnum.PROMOTION_REDUCE.getValue();
				// 满减活动促销信息
				StoreOrderItemPromotion promotionReduceChargeItem =
						storeOrderPriceResult.getStoreOrderItemPromotion(storeChargeItem, reducePromotionId, reducePromotionType);
				promotionReduceChargeItem.setPromotionId(reducePromotionId);
				promotionReduceChargeItem.setPromotionType(reducePromotionType);
				if (storePromotionReduce.getTakeModes().contains(itemPromotionRebate.getTakeMode())) {
					promotionReduceChargeItem.setAmount(promotionReduceChargeItem.getAmount() + itemSaleAmount);
					storeOrderPriceResult.addPromotionReduceAmount(itemSalePrice);
					if (itemPromotionRebate.getChargeItemRebateDerate() <= 0 && !storePromotionReduce.isShared()) {
						storeOrderPriceResult.addPromotionReduceAmountNotShared(itemSalePrice);
					}
				}
			}
		}
		// 支持使用优惠券的金额
		if (storeChargeItem.isCouponSupported()) {
			boolean couponSupported = true;
			StorePromotionRebate storePromotionRebate = null;
			if (itemPromotionRebate.getPromotionRebateId() > 0) {
				storePromotionRebate = itemPromotionRebate.getStorePromotionRebate();
			}
			if (storePromotionRebate != null && !storePromotionRebate.isCouponSupport()) {
				couponSupported = false;
			}
			if (storePromotionReduce != null && !storePromotionReduce.isCouponSupport()) {
				couponSupported = false;
			}
			storeChargeItem.setCouponSupported(couponSupported);
			if (couponSupported) {
				storeOrderPriceResult.addOrderCouponPrice(itemSalePrice);
			}
		}
	}

	/**
	 * 剔除指定类型的促销信息
	 */
	private void _rejectStoreOrderChangeItemPromotions(StoreOrderPriceResult storeOrderPriceResult,
	                                                   StoreOrderPromotionTypeEnum promotionType) {
		List<StoreOrderItemPromotion> storeOrderItemPromotions = storeOrderPriceResult.getStoreOrderItemPromotions();
		if (storeOrderItemPromotions == null || storeOrderItemPromotions.isEmpty() || promotionType == null) {
			return;
		}
		Iterator<StoreOrderItemPromotion> it = storeOrderItemPromotions.iterator();
		while (it.hasNext()) {
			StoreOrderItemPromotion storeOrderItemPromotion = it.next();
			if (promotionType.equals(storeOrderItemPromotion.getPromotionType())) {
				it.remove();
			}
		}
	}

	/**
	 * 订单优惠摊销到各个订单项目
	 */
	public void amortizeStoreOrderItemRebatePrice(StoreOrderPriceResult storeOrderPriceResult, StoreOrder storeOrder,
	                                              List<StoreOrderItem> orderItemlist) {
		if (storeOrderPriceResult == null) {
			return;
		}
		// 菜品定价会员价折扣
		Map<Long, Long> memberItemMap = Maps.newHashMap();
		// 菜品定价网单折扣
		Map<Long, Long> internetItemMap = Maps.newHashMap();
		// 菜品定价企业折扣
		Map<Long, Long> enterpriseItemMap = Maps.newHashMap();
		// 菜品定价折扣活动折扣
		Map<Long, Long> promotionRebateItemMap = Maps.newHashMap();
		// 菜品定价首份特价折扣
		Map<Long, Long> promotionItemMap = Maps.newHashMap();
		// 菜品定价满减活动折扣
		Map<Long, Long> promotionReduceItemMap = Maps.newHashMap();
		// 菜品定价整单折扣
		Map<Long, Long> totalRebateItemMap = Maps.newHashMap();
		// 订单子项促销信息中补充订单基本信息
		List<StoreOrderItemPromotion> storeOrderItemPromotions = storeOrderPriceResult.getStoreOrderItemPromotions();
		// 可以享受满减活动的总金额
		long orderCanPromotionReducePrice = 0;
		for (StoreOrderItemPromotion storeOrderItemPromotion : storeOrderItemPromotions) {
			storeOrderItemPromotion.setStoreOrderInfo(storeOrder);
			if (storeOrder.isPayAfter()) {
				storeOrderItemPromotion.setPayOrder(true);
			}
			// 单品促销总原价
			if (storeOrderItemPromotion.getPromotionType() == StoreOrderPromotionTypeEnum.PROMOTION_REDUCE.getValue()) {
				long itemCanPromotionReducePrice =
						MoneyUtil.mul(storeOrderItemPromotion.getChargeItemPrice(), storeOrderItemPromotion.getAmount());
				orderCanPromotionReducePrice = MoneyUtil.add(orderCanPromotionReducePrice, itemCanPromotionReducePrice);
			}
			// 菜品定价促销减免金额
			long itemPromotionDerate = storeOrderItemPromotion.getPromotionDerate();
			long chargeItemId = storeOrderItemPromotion.getChargeItemId();
			if (storeOrderItemPromotion.getPromotionType() == StoreOrderPromotionTypeEnum.MEMBER.getValue()) {
				memberItemMap.put(chargeItemId, memberItemMap.getOrDefault(chargeItemId, 0L) + itemPromotionDerate);
			}
			if (storeOrderItemPromotion.getPromotionType() == StoreOrderPromotionTypeEnum.INTERNET.getValue()) {
				internetItemMap.put(chargeItemId, internetItemMap.getOrDefault(chargeItemId, 0L) + itemPromotionDerate);
			}
			if (storeOrderItemPromotion.getPromotionType() == StoreOrderPromotionTypeEnum.ENTERPRISE.getValue()) {
				enterpriseItemMap.put(chargeItemId, enterpriseItemMap.getOrDefault(chargeItemId, 0L) + itemPromotionDerate);
			}
			if (storeOrderItemPromotion.getPromotionType() == StoreOrderPromotionTypeEnum.PROMOTION_ITEM.getValue()) {
				promotionItemMap.put(chargeItemId, promotionItemMap.getOrDefault(chargeItemId, 0L) + itemPromotionDerate);
			}
			if (storeOrderItemPromotion.getPromotionType() == StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue()) {
				promotionRebateItemMap.put(chargeItemId, promotionRebateItemMap.getOrDefault(chargeItemId, 0L) + itemPromotionDerate);
			}
		}
		// 满减活动减免金额分摊
		long orderPromotionReducePrice = storeOrderPriceResult.getPromotionReducePrice();
		if (orderPromotionReducePrice > 0) {
			StorePromotionReduceQuota storePromotionReduceQuota = storeOrderPriceResult.getStorePromotionReduceQuota();
			for (StoreOrderItemPromotion storeOrderItemPromotion : storeOrderItemPromotions) {
				if (storeOrderItemPromotion.getPromotionType() == StoreOrderPromotionTypeEnum.PROMOTION_REDUCE.getValue()) {
					long itemCanPromotionReducePrice =
							MoneyUtil.mul(storeOrderItemPromotion.getChargeItemPrice(), storeOrderItemPromotion.getAmount());
					// 菜品定价满减活动所占比例
					double itemPromotionReduceRate = MoneyUtil.div(itemCanPromotionReducePrice, orderCanPromotionReducePrice, 4);
					// 菜品定价满减活动分摊价
					long itemPromotionReduceDerate = MoneyUtil.mul(orderPromotionReducePrice, itemPromotionReduceRate);
					// 计入菜品定价的分摊Map
					promotionReduceItemMap.put(storeOrderItemPromotion.getChargeItemId(), itemPromotionReduceDerate);
					// 满减活动促销成本分摊到单价
					storeOrderItemPromotion.setPromotionQuota(storePromotionReduceQuota.getQuotaPrice());
					storeOrderItemPromotion.setPromotionReduce(storePromotionReduceQuota.getReducePrice());
					storeOrderItemPromotion.setPromotionDerate(itemPromotionReduceDerate);
				}
			}
		}
		// 整单折扣减免金额分摊
		if (storeOrderPriceResult.getTotalRebatePrice() > 0) {
			long orderPrice = storeOrder.getOrderPrice();
			for (StoreOrderItem storeOrderItem : orderItemlist) {
				// 菜品定价金额
				long itemPrice = MoneyUtil.mul(storeOrderItem.getPrice(), storeOrderItem.getAmount());
				if (storeOrderItem.getPackedAmount() > 0) {
					long itemPackagePrice = MoneyUtil.mul(storeOrderItem.getPackagePrice(), storeOrderItem.getPackedAmount());
					itemPrice = MoneyUtil.add(itemPrice, itemPackagePrice);
				}
				// 菜品定价整单折扣所占比例
				double itemRate = MoneyUtil.div(itemPrice, orderPrice, 4);
				// 菜品定价整单折扣分摊价
				long itemTotalRebateDerate = MoneyUtil.mul(storeOrderPriceResult.getTotalRebatePrice(), itemRate);
				totalRebateItemMap.put(storeOrderItem.getChargeItemId(), itemTotalRebateDerate);
			}
		}
		// 每种菜品定价的总优惠金额
		for (StoreOrderItem storeOrderItem : orderItemlist) {
			long chargeItemId = storeOrderItem.getChargeItemId();
			// 菜品定价会员价折扣
			long memberDerate = memberItemMap.getOrDefault(chargeItemId, 0L);
			// 菜品定价网单折扣
			long internetDerate = internetItemMap.getOrDefault(chargeItemId, 0L);
			// 菜品定价企业折扣
			long enterpriseDerate = enterpriseItemMap.getOrDefault(chargeItemId, 0L);
			// 菜品定价首份特价折扣
			long promotionItemDerate = promotionItemMap.getOrDefault(chargeItemId, 0L);
			// 菜品定价折扣活动折扣
			long promotionRebateDerate = promotionRebateItemMap.getOrDefault(chargeItemId, 0L);
			// 菜品定价满减活动折扣
			long promotionReduceDerate = promotionReduceItemMap.getOrDefault(chargeItemId, 0L);
			// 菜品定价整单折扣
			long staffRebateDerate = totalRebateItemMap.getOrDefault(chargeItemId, 0L);
			// 总减免金额
			long chargeItemDerate = 0;
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, memberDerate);
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, internetDerate);
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, enterpriseDerate);
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, promotionItemDerate);
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, promotionRebateDerate);
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, promotionReduceDerate);
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, staffRebateDerate);
			// 菜品定价折扣信息
			storeOrderItem.setMemberDerate(memberDerate);
			storeOrderItem.setInternetDerate(internetDerate);
			storeOrderItem.setEnterpriseDerate(enterpriseDerate);
			storeOrderItem.setPromotionItemDerate(promotionItemDerate);
			storeOrderItem.setPromotionRebateDerate(promotionRebateDerate);
			storeOrderItem.setPromotionReduceDerate(promotionReduceDerate);
			storeOrderItem.setStaffRebateDerate(staffRebateDerate);
			storeOrderItem.setChargeItemDerate(chargeItemDerate);
		}
		// 没有具体PromotionId的折扣数据暂不进入订单促销活动记录表
		Iterator<StoreOrderItemPromotion> it = storeOrderItemPromotions.iterator();
		while (it.hasNext()) {
			StoreOrderItemPromotion storeOrderItemPromotion = it.next();
			if (storeOrderItemPromotion.getPromotionId() <= 0) {
				it.remove();
			}
		}
	}

	/**
	 * 订单优惠券减免摊销到各个订单项目
	 *
	 * @param storeOrder
	 */
	public void amortizeStoreOrderItemCouponRebatePrice(StoreOrder storeOrder) {
		long orderCouponDerate = storeOrder.getOrderCouponDerate();
		List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
		if (orderCouponDerate <= 0 || storeOrderItems == null || storeOrderItems.isEmpty()) {
			return;
		}
		// 订单可使用优惠券金额
		long orderCouponPrice = storeOrder.getOrderCouponPrice();
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			// 不支持使用优惠券的跳过
			if (!storeOrderItem.isCouponSupported()) {
				continue;
			}
			// 菜品定价金额
			long itemPrice = MoneyUtil.mul(storeOrderItem.getPrice(), storeOrderItem.getAmount());
			if (storeOrderItem.getPackedAmount() > 0) {
				long itemPackagePrice = MoneyUtil.mul(storeOrderItem.getPackagePrice(), storeOrderItem.getPackedAmount());
				itemPrice = MoneyUtil.add(itemPrice, itemPackagePrice);
			}
			// 菜品定价优惠券减免所占比例
			double itemRate = MoneyUtil.div(itemPrice, orderCouponPrice, 4);
			// 菜品定价优惠券减免分摊价
			long itemCouponDerate = MoneyUtil.mul(orderCouponDerate, itemRate);
			long chargeItemDerate = storeOrderItem.getChargeItemDerate();
			chargeItemDerate = MoneyUtil.add(chargeItemDerate, itemCouponDerate);
			// 菜品定价折扣信息
			storeOrderItem.snapshot();
			storeOrderItem.setCouponDerate(itemCouponDerate);
			storeOrderItem.setChargeItemDerate(chargeItemDerate);
			storeOrderItem.update();
		}
	}

	/**
	 * 用于还需支付的桌台记录，结账时拼装主订单，之后通过主订单进行支付
	 *
	 * @param masterStoreOrder 原始主订单
	 */
	public void buildMasterOrderSettleInfo(StoreOrder masterStoreOrder) {
		boolean enableSlave = false;
		int merchantId = masterStoreOrder.getMerchantId();
		long storeId = masterStoreOrder.getStoreId();
		long tableRecordId = masterStoreOrder.getTableRecordId();
		String parentOrderId = masterStoreOrder.getOrderId();
		long orderPrice = masterStoreOrder.getOrderPrice();
		double totalRebate = masterStoreOrder.getTotalRebate();
		long totalDerate = masterStoreOrder.getTotalDerate();
		long staffId = masterStoreOrder.getStaffId();
		long userId = masterStoreOrder.getUserId();
		// 总可使用优惠券金额
		long orderCouponPrice = 0;
		List<StoreOrder> subStoreOrders =
				storeOrderDAO.getStoreOrderByParentOrderId(merchantId, storeId, tableRecordId, parentOrderId, enableSlave);
		if (subStoreOrders != null) {
			List<String> orderIds = new ArrayList<String>();
			for (StoreOrder storeOrder : subStoreOrders) {
				if (storeOrder.getPayStatus() != StoreOrderPayStatusEnum.FINISH.getValue()) {
					// 加每个未支付订单的可使用优惠券金额
					orderCouponPrice = orderCouponPrice + storeOrder.getOrderCouponPrice();
					orderIds.add(storeOrder.getOrderId());
				}
			}
			if (orderCouponPrice > 0) {
				// 计算退菜的可使用优惠券金额
				long refundOrderCouponPrice = 0L;
				List<StoreOrderRefundItem> storeOrderRefundItems =
						storeOrderRefundItemDAO.getStoreOrderRefundItemsByOrderIds(merchantId, storeId, orderIds, enableSlave);
				if (storeOrderRefundItems != null && !storeOrderRefundItems.isEmpty()) {
					Map<Long, StoreOrderPlaceItemParam> orderChargeItemMap = new HashMap<Long, StoreOrderPlaceItemParam>();
					for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems) {
						long chargeItemId = storeOrderRefundItem.getChargeItemId();
						double amount = storeOrderRefundItem.getAmount();
						StoreOrderPlaceItemParam storeOrderPlaceItemParam = orderChargeItemMap.get(chargeItemId);
						if (storeOrderPlaceItemParam == null) {
							storeOrderPlaceItemParam = new StoreOrderPlaceItemParam();
							storeOrderPlaceItemParam.setChargeItemId(chargeItemId);
							storeOrderPlaceItemParam.setAmount(amount);
						} else {
							storeOrderPlaceItemParam.setAmount(storeOrderPlaceItemParam.getAmount() + amount);
						}
						orderChargeItemMap.put(chargeItemId, storeOrderPlaceItemParam);
					}
					List<Long> chargeItemIds = new ArrayList<Long>(orderChargeItemMap.keySet());
					Map<Long, StoreOrderItem> storeOrderItemMap =
							storeOrderItemDAO.getStoreOrderItemMapByIds(merchantId, storeId, orderIds, chargeItemIds);
					refundOrderCouponPrice = this.getOrderCouponPrice(storeOrderItemMap, orderChargeItemMap);
				}
				// 减去退菜的可使用优惠券金额
				orderCouponPrice = orderCouponPrice - refundOrderCouponPrice;
				if (orderCouponPrice < 0) {
					orderCouponPrice = 0;
				}
			}
		}
		// 整单折扣减免额度
		long totalPrice = orderPrice;
		long totalRebatePrice = this._getTotalRebateResultPrice(orderPrice, totalRebate, totalDerate);
		// 最终折后价格
		long favorablePrice = totalPrice;
		if (totalPrice > totalRebatePrice) {
			favorablePrice = MoneyUtil.sub(totalPrice, totalRebatePrice);// 减去折扣价
		} else {
			totalRebatePrice = 0;
		}
		// 其他相关折扣
		masterStoreOrder.setOrderPrice(orderPrice);
		masterStoreOrder.setTotalPrice(totalPrice);
		masterStoreOrder.setFavorablePrice(favorablePrice);
		masterStoreOrder.setTotalRebate(totalRebate);
		masterStoreOrder.setTotalDerate(totalDerate);
		masterStoreOrder.setTotalRebatePrice(totalRebatePrice);
		masterStoreOrder.setOrderCouponPrice(orderCouponPrice);
		masterStoreOrder.setUserId(userId);
		masterStoreOrder.setStaffId(staffId);
		// 已废弃字段
		masterStoreOrder.setRebateType(0);
	}

	private long _getRebateResultPrice(long itemsCanRebatePrice, double rebate) {
		long rebatePriceResult = MoneyUtil.getRebatePrice(rebate, 0, itemsCanRebatePrice);
		long rebatePrice = MoneyUtil.sub(itemsCanRebatePrice, rebatePriceResult);
		if (rebatePrice < 0) {
			rebatePrice = 0;
		}
		return rebatePrice;
	}

	private long _getTotalRebateResultPrice(long totalPrice, double totalRebate, long totalDerate) {
		long rebatePriceResult = MoneyUtil.getRebatePrice(totalRebate, 0, totalPrice);// 先打折
		rebatePriceResult = MoneyUtil.sub(rebatePriceResult, totalDerate);// 再减免
		if (rebatePriceResult < 0) {
			rebatePriceResult = 0;
		}
		long rebatePrice = MoneyUtil.sub(totalPrice, rebatePriceResult);
		return rebatePrice;
	}

	private long _getStoreOrderItemPrice(double amount, StoreChargeItem storeChargeItem) {
		long chargeItemPrice = storeChargeItem.getCurPrice();
		long chargePrice = MoneyUtil.mul(chargeItemPrice, amount);
		return chargePrice;
	}

	public long getStoreOrderDeliveryFee(PlaceOrderParam placeOrderParam, List<StoreChargeItem> orderChargeItems,
	                                     StoreOrderPriceResult storeOrderPriceResult) throws T5weiException {
		StoreOrderPlaceParam storeOrderPlaceParam = placeOrderParam.getStoreOrderPlaceParam();
		StoreDeliverySetting storeDeliverySetting = placeOrderParam.getStoreDeliverySetting();
		long orderPrice = storeOrderPriceResult.getOrderPrice();
		long deliveryFee = 0;
		boolean delivery = false;
		// 打包费
		if (storeOrderPlaceParam.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
			delivery = true;
		}
		if (delivery) {
			// 外送判断 包括打包费的判断
			if (!storeDeliverySetting.isDeliverySupportedForPrice(orderPrice)) {
				// 订单支付金额不够下单最低标准
				throw new T5weiException(T5weiErrorCodeType.DELIVERY_ORDER_PRICE_LESS_THAN_SUPPORTED.getValue(),
				                         "order price[" + storeOrderPriceResult.getFavorablePrice()
				                         + "] not supported for delivery setting [" + storeDeliverySetting.getMinOrderDeliveryAmount() +
				                         "]");// by_akwei
			}
			deliveryFee = storeDeliverySetting.buildDeliveryFee(orderPrice);
		}
		return deliveryFee;
	}

	/**
	 * 获取订单里支持用优惠券支付的订单金额（包含打包费）
	 */
	private long getOrderCouponPrice(Map<Long, StoreOrderItem> storeOrderItemMap, Map<Long, StoreOrderPlaceItemParam> orderChargeItemMap) {
		long orderCouponPrice = 0;// 可使用优惠券金额
		for (long chargeItemId : orderChargeItemMap.keySet()) {
			StoreOrderPlaceItemParam itemParam = orderChargeItemMap.get(chargeItemId);
			StoreOrderItem storeOrderItem = storeOrderItemMap.get(chargeItemId);
			if (storeOrderItem == null) {
				continue;
			}
			// 是否支持优惠券支付
			if (storeOrderItem.isCouponSupported()) {
				long chargeItemPrice = storeOrderItem.getPrice();
				double amount = itemParam.getAmount();
				long chargePrice = MoneyUtil.mul(chargeItemPrice, amount);
				orderCouponPrice = MoneyUtil.add(orderCouponPrice, chargePrice);
			}
		}
		return orderCouponPrice;
	}

	/**
	 * 计算入客数
	 *
	 * @return
	 */
	private int getCustomerTraffic(StoreOrderPlaceParam storeOrderPlaceParam, Store5weiSetting store5weiSetting,
	                               Map<Long, StoreOrderPlaceItemParam> orderChargeItemMap, List<StoreChargeItem> orderChargeItems) {
		boolean enableManualCustomerTraffic = storeOrderPlaceParam.isEnableManualCustomerTraffic();
		int customerTraffic = storeOrderPlaceParam.getCustomerTraffic();
		// 手动输入的入客数
		if (enableManualCustomerTraffic) {
			return customerTraffic;
		}
		// 客单价的计算方法：0=按订单统计，1=按入客数统计
		if (store5weiSetting.getCustomerAvgPaymentModel() == StoreCustomerAvgPaymentEnum.ORDER.getValue()) {
			return 1;
		}
		// 自动计算的入客数
		int customerNum = 0;
		for (StoreChargeItem storeChargeItem : orderChargeItems) {
			StoreOrderPlaceItemParam itemParam = orderChargeItemMap.get(storeChargeItem.getChargeItemId());
			int customers = storeChargeItem.getCustomerTraffic();
			customers = BigDecimal.valueOf(NumberUtil.mul(customers, itemParam.getAmount())).setScale(0, BigDecimal.ROUND_UP).intValue();
			customerNum += customers;
		}
		return customerNum;
	}

	/**
	 * 计算台位费
	 *
	 * @param storeOrderPlaceParam
	 * @param customerTraffic
	 * @return
	 * @throws T5weiException
	 */
	private long getTableFee(StoreOrderPlaceParam storeOrderPlaceParam, int customerTraffic) throws T5weiException {
		int merchantId = storeOrderPlaceParam.getMerchantId();
		long storeId = storeOrderPlaceParam.getStoreId();
		long timeBucketId = storeOrderPlaceParam.getTimeBucketId();
		int takeMode = storeOrderPlaceParam.getTakeMode();
		boolean enableAddDishes = storeOrderPlaceParam.isEnableAddDishes();
		// 入客数为0不计台位费
		if (customerTraffic <= 0) {
			return 0;
		}
		// 加菜的订单不重复收取台位费
		if (enableAddDishes) {
			return 0;
		}
		// 快取、外送、外带不收台位费
		if (takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue() || takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue() ||
		    takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
			return 0;
		}
		// 营业时段台位费
		StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId);
		long tableFee = storeTimeBucket.getTableFee();
		if (tableFee <= 0) {
			return 0;
		}
		// 台位费
		return customerTraffic * tableFee;
	}

	/**
	 * 计算订单的实际支付信息
	 *
	 * @param storeOrder
	 * @param payResult
	 * @return
	 */
	public StoreOrderActualPayResult getStoreOrderActualPayInfo(StoreOrder storeOrder, PayResultOfPayOrder payResult) {
		StoreOrderActualPayResult storeOrderActualPay = BeanUtil.copy(storeOrder, StoreOrderActualPayResult.class);
		long orderPrice = storeOrder.getOriginalPrice();// 订单原价
		long chargeItemPrice = storeOrder.getChargeItemPrice();// 订单菜品原价
		long deliveryFee = storeOrder.getDeliveryFee();// 外送费
		long tableFee = storeOrder.getTableFee();// 台位费
		long couponAmount = 0L;// 优惠券抵扣
		long cashReceivedAmount = 0L;// 现金收款金额
		long cashAmount = 0L;// 现金支付金额
		String dynamicPayMethodName = "";
		if (payResult != null) {
			couponAmount = payResult.getCouponAmount();
			cashReceivedAmount = payResult.getCashReceivedAmount();
			cashAmount = payResult.getCashAmount();
			List<PayResultOfDynamicPayMethod> payResultOfDynamicPayMethodList = payResult.getPayResultOfDynamicPayMethodList();
			if (payResultOfDynamicPayMethodList != null && !payResultOfDynamicPayMethodList.isEmpty()) {
				dynamicPayMethodName = payResultOfDynamicPayMethodList.get(0).getDynamicPayMethodName();
				// add by wxy 20161111
				List<StoreOrderPayResultOfDynamicPayMethod> storeOrderDynamicPayMethods =
						BeanUtil.copyList(payResultOfDynamicPayMethodList, StoreOrderPayResultOfDynamicPayMethod.class);
				storeOrderActualPay.setStoreOrderPayResultOfDynamicPayMethodList(storeOrderDynamicPayMethods);
			}
		}
		long dynamicPayDerate = storeOrderActualPay.getDynamicPayAllDerate();// 自定义券支付优惠
		long actualPrice = storeOrder.getActualPrice() - couponAmount; // 实付金额
		long favourableDerate = orderPrice - actualPrice; // 优惠额度（订单优惠+自定义券支付优惠）
		long internetRebatePrice = storeOrder.getInternetRebatePrice();// 自助下单折扣
		long enterpriseRebatePrice = storeOrder.getEnterpriseRebatePrice();// 企业折扣
		long memberRebatePrice = storeOrder.getMemberRebatePrice();// 会员价折扣
		long promotionPrice = storeOrder.getPromotionPrice();//单品折扣总共优惠
		long promotionRebatePrice = storeOrder.getPromotionRebatePrice();//折扣活动减免金额
		long promotionReducePrice = storeOrder.getPromotionReducePrice();//满减活动减免金额

		long totalDerate = MoneyUtil.sub(orderPrice, storeOrder.getPayablePrice()); // 整单减免，订单原价-实付金额
		totalDerate = MoneyUtil.sub(totalDerate, internetRebatePrice);// 减去网单减免金额
		totalDerate = MoneyUtil.sub(totalDerate, enterpriseRebatePrice);// 减去网单减免金额
		totalDerate = MoneyUtil.sub(totalDerate, memberRebatePrice);// 减去会员价减免金额
		totalDerate = MoneyUtil.sub(totalDerate, promotionPrice);// 减去首份特价减免金额
		totalDerate = MoneyUtil.sub(totalDerate, promotionRebatePrice);// 减去折扣活动减免金额
		totalDerate = MoneyUtil.sub(totalDerate, promotionReducePrice);// 减去满减活动减免金额

		long totalDerate2 = totalDerate;

		long cashReturnAmount = 0L;// 现金找零
		if (cashReceivedAmount > cashAmount) {
			cashReturnAmount = cashReceivedAmount - cashAmount;
		}
		storeOrderActualPay.setOrderPrice(orderPrice);
		storeOrderActualPay.setChargeItemPrice(chargeItemPrice);
		storeOrderActualPay.setActualPrice(actualPrice);
		storeOrderActualPay.setDeliveryFee(deliveryFee);
		storeOrderActualPay.setTableFee(tableFee);
		storeOrderActualPay.setFavourableDerate(favourableDerate);
		storeOrderActualPay.setInternetRebatePrice(internetRebatePrice);
		storeOrderActualPay.setEnterpriseRebatePrice(enterpriseRebatePrice);
		storeOrderActualPay.setMemberRebatePrice(memberRebatePrice);
		storeOrderActualPay.setPromotionRebatePrice(promotionRebatePrice);
		storeOrderActualPay.setPromotionReducePrice(promotionReducePrice);
		storeOrderActualPay.setTotalDerate(totalDerate);
		storeOrderActualPay.setCouponAmount(couponAmount);
		storeOrderActualPay.setDynamicPayMethodName(dynamicPayMethodName);
		storeOrderActualPay.setDynamicPayDerate(dynamicPayDerate);
		storeOrderActualPay.setCashReceivedAmount(cashReceivedAmount);
		storeOrderActualPay.setCashAmount(cashAmount);
		storeOrderActualPay.setCashReturnAmount(cashReturnAmount);
		storeOrderActualPay.setPromotionPrice(promotionPrice);
		storeOrderActualPay.setTotalDerate2(totalDerate2);
		return storeOrderActualPay;
	}

	public StoreOrderPackageFeeResult getStoreOrderPackageFee(PlaceOrderParam placeOrderParam, List<StoreChargeItem> orderChargeItems) {
		StoreOrderPackageFeeResult storeOrderPackageFeeResult = new StoreOrderPackageFeeResult();
		long packageFee = 0;//订单打包费
		boolean producePackageFee = false; //是否会产生打包费
		//店铺开启打包费
		Store5weiSetting store5weiSetting = placeOrderParam.getStore5weiSetting();
		if (store5weiSetting.isPackageFeeEnable()) {
			StoreOrderPlaceParam storeOrderPlaceParam = placeOrderParam.getStoreOrderPlaceParam();
			Map<Long, StoreOrderPlaceItemParam> orderChargeItemMap = storeOrderHelper.getChargeItemsMapOfStoreOrder(storeOrderPlaceParam);
			for (StoreChargeItem storeChargeItem : orderChargeItems) {
				//收费项目打包费单价
				long packagePrice = storeChargeItem.getPackagePrice();
				if (packagePrice > 0) {
					producePackageFee = true;
				}
				StoreOrderPlaceItemParam itemParam = orderChargeItemMap.get(storeChargeItem.getChargeItemId());
				double packageAmount = 0;
				if (itemParam != null) {
					packageAmount = itemParam.getPackedAmount();
				}
				if (packageAmount <= 0) {
					continue;
				}
				long itemPackagePrice = MoneyUtil.mul(packagePrice, packageAmount);
				packageFee = MoneyUtil.add(packageFee, itemPackagePrice);
			}
		}
		storeOrderPackageFeeResult.setPackageFee(packageFee);
		storeOrderPackageFeeResult.setProducePackageFee(producePackageFee);
		return storeOrderPackageFeeResult;
	}

}
