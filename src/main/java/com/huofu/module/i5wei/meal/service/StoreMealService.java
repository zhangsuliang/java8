package com.huofu.module.i5wei.meal.service;

import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.ChangeStoreOrderSendTypeParam;
import huofucore.facade.i5wei.meal.StoreDivRuleProductParam;
import huofucore.facade.i5wei.meal.StoreMealCheckoutItemParam;
import huofucore.facade.i5wei.meal.StoreMealStatProductParam;
import huofucore.facade.i5wei.meal.StoreMealStatProductsQueryParam;
import huofucore.facade.i5wei.meal.StoreMealsCheckoutParam;
import huofucore.facade.i5wei.meal.StoreSendTypeEnum;
import huofucore.facade.i5wei.mealport.StoreMealPortCheckoutTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderOptlogTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.order.StoreOrderTradeStatusEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutRecordDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckoutRecord;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.service.QueryProductPortParam;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemPromotionDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderOptlogDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderOptlog;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.order.entity.StoreOrderSubitem;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.printer.I5weiSendPrinter;
import com.huofu.module.i5wei.promotion.service.StoreChargeItemPromotionService;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

@Service
public class StoreMealService {
	
	@Autowired
    private StoreOrderHelper storeOrderHelper;

	@Autowired
	private StoreOrderDAO storeOrderDao;

	@Autowired
	private StoreOrderItemDAO storeOrderItemDao;

	@Autowired
	private StoreMealTakeupDAO storeMealTakeupDao;

	@Autowired
	private StoreMealCheckoutDAO storeMealCheckoutDao;

	@Autowired
	private StoreMealCheckoutRecordDAO storeMealCheckoutRecordDao;

	@Autowired
	private StoreProductDAO storeProductDao;

	@Autowired
	private StoreOrderOptlogDAO storeOrderOptlogDao;

	@Autowired
	private StoreMealPortDAO storeMealPortDAO;

	@Autowired
	private StoreChargeItemService storeChargeItemService;

	@Autowired
	private StoreOrderService storeOrderService;

	@Autowired
	private StoreInventoryService storeInventoryService;

	@Autowired
	private StoreMealMultiService storeMealMultiService;

	@Autowired
	private StoreOrderItemPromotionDAO storeOrderItemPromotionDAO;

	@Autowired
	private StoreChargeItemPromotionService storeChargeItemPromotionService;

	@Autowired
	private StoreMealMultiHelper storeMealMultiHelper;

	@Autowired
	private StoreMealPortService storeMealPortService;

	@Autowired
	private StoreTableRecordDAO storeTableRecordDAO;
	
	@Autowired
	private I5weiSendPrinter i5weiSendPrinter;
	
	@Autowired
	private Store5weiSettingService store5weiSettingService;
	
	@Autowired
	private StoreMealSweepService storeMealSweepService;

	public List<StoreMealTakeup> storeOrderTakeCode(StoreOrder storeOrder, int takeSerialNumber) throws TException {
		boolean enableSlave = false;
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		// 取餐方式个更改回写订单
		StoreOrderTakeModeEnum takeMode = StoreOrderTakeModeEnum.findByValue(storeOrder.getTakeMode());
		// 客户订单取餐可能会更高取餐方式，需要回写订单子项
		if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT || takeMode == StoreOrderTakeModeEnum.SEND_OUT) {
			storeOrderItemDao.updatePackagedStatus(merchantId, storeId, orderId, true);
		} else if (takeMode == StoreOrderTakeModeEnum.DINE_IN) {
			storeOrderItemDao.updatePackagedStatus(merchantId, storeId, orderId, false);
		}
		// 进入待出餐
		List<StoreMealTakeup> storeMealTakeUpItems;
		List<StoreMealPort> ports = storeMealPortDAO.getList(merchantId, storeId, enableSlave);
		if (ports == null || ports.isEmpty()) {
			storeMealTakeUpItems = this.storeOrderTakeCodeNoPort(storeOrder, takeSerialNumber);
		} else {
			storeMealTakeUpItems = storeMealMultiService.storeOrderTakeCodeByPort(storeOrder, takeSerialNumber);
		}
		// 扣减固定库存
		storeInventoryService.deductFixInventoryForOrder(storeOrder);
		// 单品促销已交易
		storeOrderItemPromotionDAO.updateTradeOrder(merchantId, storeId, orderId);
		storeChargeItemPromotionService.updateSaleNum(merchantId, storeId, orderId);
		return storeMealTakeUpItems;
	}

	/**
	 * 订单取餐，订单信息进入后厨出餐流程
	 *
	 * @param storeOrder
	 * @param takeSerialNumber
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public List<StoreMealTakeup> storeOrderTakeCodeNoPort(StoreOrder storeOrder, int takeSerialNumber) throws T5weiException {
		boolean enableSlave = false;
		storeOrder.setTakeSerialNumber(takeSerialNumber);
		List<StoreMealTakeup> storeMealTakeUpItems = this.buildStoreMealTakeups(storeOrder, enableSlave);
		storeMealTakeupDao.batchCreate(storeMealTakeUpItems);
		storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.CODE_TAKED.getValue());
		return storeMealTakeUpItems;
	}
	
	/**
	 * 构造待出餐列表（不计算是哪个出餐口，适合快取业务使用）
	 * 
	 * @param storeOrder
	 * @param enableSlave
	 * @return
	 */
	public List<StoreMealTakeup> buildStoreMealTakeups(StoreOrder storeOrder, boolean enableSlave){
		String orderId = storeOrder.getOrderId();
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		long repastDate = storeOrder.getRepastDate();
		long timeBucketId = storeOrder.getTimeBucketId();
		int siteNumber = storeOrder.getSiteNumber();
		int takeSerialNumber = storeOrder.getTakeSerialNumber();
		List<StoreMealTakeup> storeMealTakeUpItems = new ArrayList<StoreMealTakeup>();
		StoreOrderTakeModeEnum takeMode = StoreOrderTakeModeEnum.findByValue(storeOrder.getTakeMode());
		int count = storeMealTakeupDao.countStoreMealTakeupsByOrderId(merchantId, storeId, orderId, enableSlave);
		if (count > 0) {
			return storeMealTakeUpItems;
		}
		List<QueryProductPortParam> queryProductPortParams = storeMealMultiHelper.storeOrderToQueryProductPortParams(storeOrder);
        Map<String, Long> productPortMap = storeChargeItemService.getPortIdMap(merchantId, storeId, queryProductPortParams);
		List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			long chargeItemId = storeOrderItem.getChargeItemId();
			double orderAmount = storeOrderItem.getAmount();
			double packedAmount;
			if (takeMode == StoreOrderTakeModeEnum.DINE_IN) {
				packedAmount = Double.valueOf(0);
			} else if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT || takeMode == StoreOrderTakeModeEnum.SEND_OUT) {
				packedAmount = orderAmount;
			} else {
				packedAmount = storeOrderItem.getPackedAmount();
			}
			storeOrderItem.setPackedAmount(packedAmount);
			String chargeItemName = storeOrderItem.getChargeItemName();
			List<StoreOrderSubitem> storeOrderSubitems = storeOrderItem.getStoreOrderSubitems();
			for (StoreOrderSubitem storeOrderSubitem : storeOrderSubitems) {
				double amount = storeOrderSubitem.getAmount();
				long productId = storeOrderSubitem.getProductId();
				String productName = storeOrderSubitem.getProductName();
				String unit = storeOrderSubitem.getUnit();
				String remark = storeOrderSubitem.getRemark();
				String key = chargeItemId + "_" + productId;
                long portId = productPortMap.getOrDefault(key, 0L);
				StoreMealTakeup storeMealTakeup = new StoreMealTakeup();
				storeMealTakeup.setOrderId(orderId);
				storeMealTakeup.setMerchantId(merchantId);
				storeMealTakeup.setStoreId(storeId);
				storeMealTakeup.setRepastDate(repastDate);
				storeMealTakeup.setTakeMode(takeMode.getValue());
				storeMealTakeup.setTakeSerialNumber(takeSerialNumber);
				storeMealTakeup.setSiteNumber(siteNumber);
				storeMealTakeup.setTimeBucketId(timeBucketId);
				storeMealTakeup.setChargeItemId(chargeItemId);
				storeMealTakeup.setChargeItemName(chargeItemName);
				storeMealTakeup.setProductId(productId);
				storeMealTakeup.setProductName(productName);
				storeMealTakeup.setAmount(amount);
				storeMealTakeup.setUnit(unit);
				storeMealTakeup.setRemark(remark);
				storeMealTakeup.setPortId(portId);
				storeMealTakeup.setUpdateTime(System.currentTimeMillis());
				storeMealTakeup.setCreateTime(System.currentTimeMillis());
				storeMealTakeup.setSpicyLevel(storeOrderItem.getSpicyLevel());
				storeMealTakeup.setPayAfter(storeOrder.isPayAfter());
				storeMealTakeup.setTableRecordId(storeOrder.getTableRecordId());
                storeMealTakeup.setWeightEnabled(storeOrderItem.isWeightEnabled());
				if (packedAmount > 0) {
					// 打包部分
					storeMealTakeup.setPackaged(true);
					storeMealTakeup.setAmountOrderTakeup(packedAmount, StorePrintModeEnum.NORMAL_PRINT.getValue(), false);
					storeMealTakeUpItems.add(storeMealTakeup);
					// 堂食部分
					double inAmount = NumberUtil.sub(orderAmount, packedAmount);
					if (inAmount > 0) {
						StoreMealTakeup notPackaged = BeanUtil.copy(storeMealTakeup, StoreMealTakeup.class);
						notPackaged.setPackaged(false);
						notPackaged.setAmountOrderTakeup(inAmount, StorePrintModeEnum.NORMAL_PRINT.getValue(), false);
						storeMealTakeUpItems.add(notPackaged);
					}
				} else {
					// 全部为堂食
					storeMealTakeup.setPackaged(false);
					storeMealTakeup.setAmountOrderTakeup(orderAmount, StorePrintModeEnum.NORMAL_PRINT.getValue(), false);
					storeMealTakeUpItems.add(storeMealTakeup);
				}
			}
		}
		return storeMealTakeUpItems;
	}

	/**
	 * 得到已取号、未出餐订单产品信息
	 *
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @param timeBucketId
	 * @param refreshTime
	 * @return
	 * @throws T5weiException
	 */
	public List<StoreMealTakeup> getStoreMealTakeup(int merchantId, long storeId, long repastDate, long timeBucketId, long refreshTime) throws T5weiException {
		boolean enableSlave = true;
		long repastDateTime = DateUtil.getBeginTime(repastDate, null);
		return storeMealTakeupDao.getStoreMealTakeups(merchantId, storeId, repastDateTime, timeBucketId, refreshTime, enableSlave);
	}

	/**
	 * 后厨出餐订单产品信息
	 *
	 * @return
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public List<StoreMealCheckout> storeMealCheckout(StoreMealsCheckoutParam storeMealsCheckoutParam, boolean refundMeal) throws T5weiException {
		int merchantId = storeMealsCheckoutParam.getMerchantId();
		long storeId = storeMealsCheckoutParam.getStoreId();
		String orderId = storeMealsCheckoutParam.getOrderId();
		long repastDate = storeMealsCheckoutParam.getRepastDate();
		long staffId = storeMealsCheckoutParam.getStaffId();
		if (merchantId == 0 || storeId == 0) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "store id can not null");
		}
		if (orderId == null || orderId.isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ID_CAN_NOT_NULL.getValue(), "store order_id can not null");
		}
		if (repastDate == 0) {
			throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_CAN_NOT_NULL.getValue(), "store repast date can not null");
		}
		List<StoreMealCheckoutItemParam> storeMealCheckoutItems = storeMealsCheckoutParam.getStoreMealCheckoutItems();
		if (storeMealCheckoutItems == null || storeMealCheckoutItems.isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "store meal checkout items can not null");
		}
		// 计算出餐数量
		Map<Long, Double> storeMealCheckoutItemMap = new HashMap<Long, Double>();
		for (StoreMealCheckoutItemParam storeMealCheckoutItemParam : storeMealCheckoutItems) {
			long chargeItemId = storeMealCheckoutItemParam.getChargeItemId();
			double amount = 0;
			if (storeMealCheckoutItemMap.containsKey(chargeItemId)) {
				amount = storeMealCheckoutItemMap.get(chargeItemId) + storeMealCheckoutItemParam.getAmount();
			} else {
				amount = storeMealCheckoutItemParam.getAmount();
			}
			storeMealCheckoutItemMap.put(chargeItemId, amount);
		}
		repastDate = DateUtil.getBeginTime(repastDate, null);
		List<StoreMealCheckout> resultList = new ArrayList<StoreMealCheckout>();
		boolean packaged = storeMealsCheckoutParam.isPackaged();
		List<StoreMealTakeup> storeMealTakeUpItems = storeMealTakeupDao.getStoreMealTakeupsByOrderPackaged(merchantId, storeId, orderId, packaged, true);
		if (storeMealTakeUpItems == null || storeMealTakeUpItems.isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CHECKOUT_NUM_MORE_THEN_REMAIN_NUM.getValue(), DataUtil.infoWithParams(
					"store checkout num more then order remain num, storeId=#1, orderId=#2, chargeItemId=#3 ", new Object[] { storeId, orderId }));
		}
		StoreOrder storeOrder = storeOrderDao.getById(merchantId, storeId, orderId, false, false);
		// 出餐分单记录
		int takeSerialSeq = this._updateStoreMealCheckoutRecord(storeOrder, staffId, packaged);
		// 出餐记录
		long currentTime = System.currentTimeMillis();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeUpItems) {
			long chargeItemId = storeMealTakeup.getChargeItemId();
			double remainTakeup = storeMealTakeup.getRemainTakeup();
			Double checkoutAmount = storeMealCheckoutItemMap.get(chargeItemId);
			if (checkoutAmount == null) {
				continue;
			}
			if (remainTakeup < checkoutAmount) {
				if (refundMeal) {
					checkoutAmount = remainTakeup;
				} else {
					throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CHECKOUT_NUM_MORE_THEN_REMAIN_NUM.getValue(), DataUtil.infoWithParams(
							"store checkout num more then order remain num, storeId=#1, orderId=#2, chargeItemId=#3 ", new Object[] { storeId, orderId, chargeItemId }));
				}
			}
			// 更新待出餐记录
			double remainAmount = NumberUtil.sub(remainTakeup, checkoutAmount);
			storeMealTakeup.setRemainTakeup(remainAmount);
			storeMealTakeup.setUpdateTime(currentTime);
			storeMealTakeup.setCheckoutTime(currentTime);
			storeMealTakeupDao.update(storeMealTakeup);
			// 添加出餐记录
			StoreMealCheckout storeMealCheckout = BeanUtil.copy(storeMealTakeup, StoreMealCheckout.class);
			storeMealCheckout.setStaffId(staffId);
			storeMealCheckout.setAmountCheckout(checkoutAmount);
			storeMealCheckout.setTakeSerialSeq(takeSerialSeq);
			storeMealCheckout.setPrinted(true);// 手动出餐直接标为已打印
			storeMealCheckout.setRefundMeal(refundMeal);// 设置是否退菜
			storeMealCheckout.setCreateTime(System.currentTimeMillis());
			resultList.add(storeMealCheckout);
		}
		storeMealCheckoutDao.batchCreate(resultList);
		return resultList;
	}

	private int _updateStoreMealCheckoutRecord(StoreOrder storeOrder, long staffId, boolean packaged) {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		List<StoreMealCheckoutRecord> storeMealCheckoutRecords = storeMealCheckoutRecordDao.getStoreMealCheckoutRecordByOrderId(merchantId, storeId, orderId, true);
		int takeSerialSeq = 0;
		StoreMealCheckoutRecord storeMealCheckoutRecord;
		if (storeMealCheckoutRecords != null && !storeMealCheckoutRecords.isEmpty()) {
			storeMealCheckoutRecord = storeMealCheckoutRecords.get(0);
			takeSerialSeq = storeMealCheckoutRecord.getCheckoutSeq();
			takeSerialSeq = takeSerialSeq + 1;
			int packagedSeq = storeMealCheckoutRecord.getPackagedSeq();
			if (packaged) {
				packagedSeq = packagedSeq + 1;
			}
			storeMealCheckoutRecord.setCheckoutSeq(takeSerialSeq);
			storeMealCheckoutRecord.setPackagedSeq(packagedSeq);
			storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
		} else {
			takeSerialSeq = 1;
			int packagedSeq = 0;
			if (packaged) {
				packagedSeq = 1;
			}
			storeMealCheckoutRecord = new StoreMealCheckoutRecord();
			storeMealCheckoutRecord.setMerchantId(merchantId);
			storeMealCheckoutRecord.setStoreId(storeId);
			storeMealCheckoutRecord.setStaffId(staffId);
			storeMealCheckoutRecord.setOrderId(orderId);
			storeMealCheckoutRecord.setUserId(storeOrder.getUserId());
			storeMealCheckoutRecord.setRepastDate(storeOrder.getRepastDate());
			storeMealCheckoutRecord.setTimeBucketId(storeOrder.getTimeBucketId());
			storeMealCheckoutRecord.setTakeSerialNumber(storeOrder.getTakeSerialNumber());
			storeMealCheckoutRecord.setCheckoutSeq(takeSerialSeq);
			storeMealCheckoutRecord.setPackagedSeq(packagedSeq);
			storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
			storeMealCheckoutRecord.setCreateTime(System.currentTimeMillis());
		}
		storeMealCheckoutRecordDao.replace(storeMealCheckoutRecord);
		return takeSerialSeq;
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void storeMealQuickCheckout(StoreOrder storeOrder) throws TException {
		boolean enableSlave = false;
		String orderId = storeOrder.getOrderId();
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		long repastDate = storeOrder.getRepastDate();
		long timeBucketId = storeOrder.getTimeBucketId();
		int takeSerialNumber = storeOrder.getTakeSerialNumber();
		List<StoreMealTakeup> storeMealTakeUpItems = this.buildStoreMealTakeups(storeOrder, enableSlave);
		if (storeMealTakeUpItems == null || storeMealTakeUpItems.isEmpty()) {
			return;
		}
		List<StoreMealCheckout> storeMealCheckoutItems = BeanUtil.copyList(storeMealTakeUpItems, StoreMealCheckout.class);
		// 待出餐剩余数量、待划菜数量置为0
		boolean packaged = false;
		double remainAmount = 0D;
		for (StoreMealTakeup storeMealTakeup : storeMealTakeUpItems){
			storeMealTakeup.setRemainTakeup(remainAmount);
			storeMealTakeup.setRemainSend(remainAmount);
			if(storeMealTakeup.isPackaged()){
				packaged = true;
			}
		}
		int takeSerialSeq = 1;
		int packagedSeq = 0;
		if (packaged) {
			packagedSeq = 1;
		}
		for (StoreMealCheckout storeMealCheckout : storeMealCheckoutItems){
			storeMealCheckout.setTakeSerialSeq(takeSerialSeq);
			storeMealCheckout.setAmountCheckout(storeMealCheckout.getAmountOrder());
			storeMealCheckout.setCheckoutType(StoreMealPortCheckoutTypeEnum.AUTO.getValue());
			storeMealCheckout.setPrinted(true);
		}
		// 出餐记录统计
		Set<Long> portIds = this.getStoreMealTakeUpPortIds(storeMealTakeUpItems);
		for (long portId : portIds) {
			StoreMealCheckoutRecord storeMealCheckoutRecord = new StoreMealCheckoutRecord();
			storeMealCheckoutRecord.setMerchantId(merchantId);
			storeMealCheckoutRecord.setStoreId(storeId);
			storeMealCheckoutRecord.setStaffId(storeOrder.getStaffId());
			storeMealCheckoutRecord.setUserId(storeOrder.getUserId());
			storeMealCheckoutRecord.setOrderId(orderId);
			storeMealCheckoutRecord.setRepastDate(repastDate);
			storeMealCheckoutRecord.setTimeBucketId(timeBucketId);
			storeMealCheckoutRecord.setTakeSerialNumber(takeSerialNumber);
			storeMealCheckoutRecord.setCheckoutSeq(takeSerialSeq);
			storeMealCheckoutRecord.setPackagedSeq(packagedSeq);
			storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
			storeMealCheckoutRecord.setCreateTime(System.currentTimeMillis());
			storeMealCheckoutRecord.setPortId(portId);
			storeMealCheckoutRecordDao.replace(storeMealCheckoutRecord);
		}
		// 待出餐信息保存
		storeMealTakeupDao.batchCreate(storeMealTakeUpItems);
		// 出餐信息保存
		storeMealCheckoutDao.batchCreate(storeMealCheckoutItems);
		// 扣减固定库存
		storeInventoryService.deductFixInventoryForOrder(storeOrder);
		// 单品促销已交易
		storeOrderItemPromotionDAO.updateTradeOrder(merchantId, storeId, orderId);
		storeChargeItemPromotionService.updateSaleNum(merchantId, storeId, orderId);
	}
	
	private Set<Long> getStoreMealTakeUpPortIds(List<StoreMealTakeup> storeMealTakeUpItems) {
		Set<Long> portIds = Sets.newHashSet();
		if (storeMealTakeUpItems == null || storeMealTakeUpItems.isEmpty()) {
			return portIds;
		}
		for (StoreMealTakeup storeMealTakeup : storeMealTakeUpItems) {
			portIds.add(storeMealTakeup.getPortId());
		}
		return portIds;
	}
	
	/**
	 * 过期待出餐清理
	 * @param merchantId
	 * @param storeId
	 * @throws T5weiException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void clearStoreMealsOverdue(int merchantId, long storeId, long expireTime) throws T5weiException{
		if (merchantId <= 0 || storeId <= 0 || expireTime <= 0) {
			return;
		}
		boolean enableSlave = false;
		List<String> orderIds = storeMealTakeupDao.getStoreMealTakeupOrderIdsByExpireTime(merchantId, storeId, expireTime, enableSlave);
		if (orderIds == null || orderIds.isEmpty()) {
			return;
		}
		List<StoreOrder> storeOrders = storeOrderDao.getStoreOrdersById(merchantId, storeId, orderIds, enableSlave);
		for(StoreOrder storeOrder:storeOrders){
			storeOrderOptlogDao.createOptlog(storeOrder, storeOrder.getStaffId(), storeOrder.getClientType(), StoreOrderOptlogTypeEnum.CLEAR_MEALS_OVERDUE.getValue(), " clear meals overdue");
		}
		this.clearStoreMeal(storeOrders);
	}
	
	/**
	 * 出餐完毕，修改订单状态为完成
	 *
	 * @param merchantId
	 * @param storeId
	 * @param orderId
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public int updateOrderPrepareMealFinish(int merchantId, long storeId, String orderId, int checkoutType) {
		boolean enableSlave = false;
		StoreOrder storeOrder = storeOrderDao.getById(merchantId, storeId, orderId, true, true);
		int count = storeMealTakeupDao.countStoreMealTakeupsByOrderId(merchantId, storeId, storeOrder.getOrderId(), enableSlave);
		int clientType = storeOrder.getClientType();
		int optType = StoreOrderOptlogTypeEnum.MEAL_CHECKOUT.getValue();
		if (count == 0) {
			if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
				storeOrderService.updateOrderPrepareMealFinish(merchantId, storeId, storeOrder);
				optType = StoreOrderOptlogTypeEnum.MEAL_CHECKOUT_COMPLETE.getValue();
			} else {
				storeOrderService.updateOrderTradeFinish(storeOrder);
				optType = StoreOrderOptlogTypeEnum.TRADE_FINISH.getValue();
			}
		}
		storeOrderOptlogDao.createOptlog(storeOrder, 0, clientType, optType, "checkoutType=" + checkoutType);
		return count;
	}

	/**
	 * 得到日营业时段销售中的产品
	 *
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @param timeBucketId
	 * @return
	 * @throws T5weiException
	 */
	public List<StoreProduct> getStoreMealStatProducts(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException {
		long repastDateTime = DateUtil.getBeginTime(repastDate, null);
		// 销售中的产品
		List<StoreProduct> products = storeChargeItemService.getStoreProductsForDate(merchantId, storeId, repastDateTime, timeBucketId, true, true);
		if (products == null) {
			return null;
		}
		Collections.sort(products, new ComparatorStoreProduct());
		return products;
	}

	/**
	 * 得到出餐口日营业时段销售中的产品
	 *
	 * @param storeMealStatProductsQueryParam
	 * @return
	 * @throws T5weiException
	 */
	public List<StoreProduct> getStoreMealStatProducts(StoreMealStatProductsQueryParam storeMealStatProductsQueryParam) throws T5weiException {
		if (storeMealStatProductsQueryParam == null) {
			return new ArrayList<StoreProduct>();
		}
		int merchantId = storeMealStatProductsQueryParam.getMerchantId();
		long storeId = storeMealStatProductsQueryParam.getStoreId();
		long repastDate = storeMealStatProductsQueryParam.getRepastDate();
		long timeBucketId = storeMealStatProductsQueryParam.getTimeBucketId();
		long portId = storeMealStatProductsQueryParam.getPortId();
		if (merchantId == 0 || storeId == 0) {
			return new ArrayList<StoreProduct>();
		}
		List<StoreProduct> products4Sell = storeChargeItemService.getStoreProductsForDate(merchantId, storeId, repastDate, timeBucketId, true, true);// 销售中的产品
		List<StoreProduct> products4Port = new ArrayList<StoreProduct>();
		List<Long> portIds = new ArrayList<Long>();// 当前出餐口
		if (portId > 0) {
			portIds.add(portId);
			// 出餐口相关产品
			for (StoreProduct product : products4Sell) {
				if (product.getPortId() == portId || product.getPortId() == 0) {
					products4Port.add(product);
				}
			}
		} else {
			products4Port = products4Sell;
		}
		// 待出餐列表
		List<StoreProduct> products4Meal;
		List<StoreMealTakeup> storeMealTakeups = storeMealTakeupDao.getStoreMealTakeups(merchantId, storeId, portIds, true);
		if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
			// 待出餐列表为空，则直接返回本营业时段产品
			products4Meal = new ArrayList<StoreProduct>();
		} else {
			// 待出餐产品列表
			Set<Long> productIds = new HashSet<Long>();
			for (StoreMealTakeup takeup : storeMealTakeups) {
				productIds.add(takeup.getProductId());
			}
			products4Meal = storeProductDao.getListInIds(merchantId, storeId, new ArrayList<Long>(productIds));
		}
		// List合并去重
		Map<Long, StoreProduct> productMap = new HashMap<Long, StoreProduct>();
		for (StoreProduct product : products4Port) {
			productMap.put(product.getProductId(), product);
		}
		for (StoreProduct product : products4Meal) {
			productMap.put(product.getProductId(), product);
		}
		List<StoreProduct> resultProducts = new ArrayList<StoreProduct>(productMap.values());
		Collections.sort(resultProducts, new ComparatorStoreProduct());
		return resultProducts;
	}

	private class ComparatorStoreProduct implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreProduct obj0 = (StoreProduct) arg0;
			StoreProduct obj1 = (StoreProduct) arg1;
			return obj0.getName().compareTo(obj1.getName());
		}
	}

	public List<StoreProduct> storeDivRuleProductSetup(int merchantId, long storeId, List<StoreDivRuleProductParam> storeDivRuleProducts) throws T5weiException {
		if (storeDivRuleProducts == null || storeDivRuleProducts.isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "storeDivRuleProducts input param incomplete, MealStatProducts is null");
		}
		Map<Long, Integer> storeDivRuleProductMap = new HashMap<>();
		for (StoreDivRuleProductParam storeDivRuleProductParam : storeDivRuleProducts) {
			storeDivRuleProductMap.put(storeDivRuleProductParam.getProductId(), storeDivRuleProductParam.getDivRule());
		}
		List<Long> productIds = new ArrayList<>(storeDivRuleProductMap.keySet());
		List<StoreProduct> products = storeProductDao.getListInIds(merchantId, storeId, productIds);
		if (products != null && !products.isEmpty()) {
			for (StoreProduct storeProduct : products) {
				storeProduct.setDivRule(storeDivRuleProductMap.get(storeProduct.getProductId()));
			}
			storeProductDao.batchUpdateDivRule(merchantId, storeId, products);
		}
		return products;
	}

	/**
	 * 更新产品是否需要后厨出餐统计设置
	 *
	 * @param merchantId
	 * @param storeId
	 * @param storeMealStatProducts
	 * @return
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public List<StoreProduct> storeMealStatProductSetup(int merchantId, long storeId, List<StoreMealStatProductParam> storeMealStatProducts) throws T5weiException {
		if (storeMealStatProducts == null || storeMealStatProducts.isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "storeMealStatProductSetup input param incomplete, MealStatProducts is null");
		}
		Map<Long, Boolean> storeMealStatProductMap = new HashMap<Long, Boolean>();
		for (StoreMealStatProductParam storeMealStatProductParam : storeMealStatProducts) {
			storeMealStatProductMap.put(storeMealStatProductParam.getProductId(), storeMealStatProductParam.isMealStat());
		}
		List<Long> productIds = new ArrayList<Long>(storeMealStatProductMap.keySet());
		List<StoreProduct> products = storeProductDao.getListInIds(merchantId, storeId, productIds);
		if (products != null && !products.isEmpty()) {
			for (StoreProduct storeProduct : products) {
				storeProduct.setMealStat(storeMealStatProductMap.get(storeProduct.getProductId()));
			}
			storeProductDao.batchUpdateMealStat(merchantId, storeId, products);
		}
		return products;
	}

	/**
	 * 日出餐历史
	 *
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @return
	 * @throws T5weiException
	 */
	public List<StoreMealCheckout> getStoreMealCheckoutHistory(int merchantId, long storeId, long repastDate) throws T5weiException {
		boolean enableSlave = true;
		long repastDateTime = DateUtil.getBeginTime(repastDate, null);
		return storeMealCheckoutDao.getStoreMealCheckouts(merchantId, storeId, repastDateTime, enableSlave);
	}

	/**
	 * 根据订单ID查询出餐中的信息
	 * 
	 * @param merchantId
	 * @param storeId
	 * @param orderId
	 * @return
	 * @throws T5weiException
	 */
	public List<StoreMealTakeup> getStoreMealsByOrderId(int merchantId, long storeId, String orderId) throws T5weiException {
		boolean enableSlave = true;
		return storeMealTakeupDao.getStoreMealsByOrderId(merchantId, storeId, orderId, false, enableSlave);
	}

	/**
	 * 根据订单ID查询已出餐信息
	 * 
	 * @param merchantId
	 * @param storeId
	 * @param orderId
	 * @return
	 * @throws T5weiException
	 */
	public List<StoreMealCheckout> getStoreMealsHistoryByOrderId(int merchantId, long storeId, String orderId) throws T5weiException {
		boolean enableSlave = true;
		return storeMealCheckoutDao.getStoreMealsHistoryByOrderId(merchantId, storeId, orderId, false, enableSlave);
	}

	public List<StoreMealTakeup> getStoreMealsByTableRecordId(int merchantId, long storeId, long tableRecordId) throws TException {
		boolean enableSlave = true;
		return storeMealTakeupDao.getStoreMealsByTableRecordId(merchantId, storeId, tableRecordId, false, enableSlave);
	}

	public List<StoreMealCheckout> getStoreMealsHistoryByTableRecordId(int merchantId, long storeId, long tableRecordId) throws TException {
		boolean enableSlave = true;
		return storeMealCheckoutDao.getStoreMealsHistoryByTableRecordId(merchantId, storeId, tableRecordId, false, enableSlave);
	}

	public StoreMealCheckoutRecord getStoreMealCheckoutRecord(int merchantId, long storeId, long repastDate, int takeSerialNumber) throws T5weiException {
		boolean enableSlave = true;
		return storeMealCheckoutRecordDao.getStoreMealCheckoutRecord(merchantId, storeId, repastDate, takeSerialNumber, enableSlave);
	}
	
	/**
     * ＊通过就餐日期、取餐编号和取餐口查询出餐口取餐记录
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param takeSerialNumber
     * @param portId
     * @return
     */
	public StoreMealCheckoutRecord getStoreMealCheckoutRecord(int merchantId, long storeId, long repastDate, int takeSerialNumber, int portId) throws T5weiException {
		boolean enableSlave = true;
		return storeMealCheckoutRecordDao.getStoreMealCheckoutRecord(merchantId, storeId, repastDate, takeSerialNumber, portId, enableSlave);
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public int updateStoreMealNotifyTime(int merchantId, long storeId, String orderId) throws T5weiException, TException {
		List<StoreMealCheckoutRecord> storeMealCheckoutRecords = storeMealCheckoutRecordDao.getStoreMealCheckoutRecordByOrderId(merchantId, storeId, orderId, true);
		if (storeMealCheckoutRecords == null || storeMealCheckoutRecords.isEmpty()) {
			return 0;
		}
		for (StoreMealCheckoutRecord storeMealCheckoutRecord : storeMealCheckoutRecords) {
			storeMealCheckoutRecord.snapshot();
			storeMealCheckoutRecord.setNotifyTime(System.currentTimeMillis());
			storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
			storeMealCheckoutRecord.update();
		}
		StoreOrder storeOrder = new StoreOrder();
		storeOrder.setOrderId(orderId);
		storeOrder.setStoreId(storeId);
		storeOrder.setMerchantId(merchantId);
		storeOrder.setUserId(storeMealCheckoutRecords.get(0).getUserId());
		storeOrderOptlogDao.createOptlog(storeOrder, 0, ClientTypeEnum.CASHIER.getValue(), StoreOrderOptlogTypeEnum.ORDER_MEAL_NOTIFY.getValue(), "store order meal notifyTime");
		return 1;
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public int updateStoreMealsNotifyTime(int merchantId, long storeId, String orderId, int portId) throws T5weiException, TException {
		StoreMealCheckoutRecord storeMealCheckoutRecord = storeMealCheckoutRecordDao.getStoreMealCheckoutRecordByOrderId(merchantId, storeId, orderId, portId, true);
		if (storeMealCheckoutRecord == null) {
			StoreOrder storeOrder = storeOrderDao.getById(merchantId, storeId, orderId, false, false);
			if (storeOrder == null) {
				return 0;
			}
			storeMealCheckoutRecord = BeanUtil.copy(storeOrder, StoreMealCheckoutRecord.class);
			storeMealCheckoutRecord.setPortId(portId);
			storeMealCheckoutRecord.setNotifyTime(System.currentTimeMillis());
			storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
			storeMealCheckoutRecord.setCreateTime(System.currentTimeMillis());
			storeMealCheckoutRecord.create();
		} else {
			storeMealCheckoutRecord.snapshot();
			storeMealCheckoutRecord.setNotifyTime(System.currentTimeMillis());
			storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
			storeMealCheckoutRecord.update();
		}
		return 1;
	}
	
	public Set<Long> getMealStatProductIds(int merchantId, long storeId){
		List<StoreProduct> statProducts = storeProductDao.getMealStatList(merchantId, storeId, true, false);
		if (statProducts == null || statProducts.isEmpty()) {
			return new HashSet<Long>();
		}
		Set<Long> productIds = new HashSet<Long>();
		for (StoreProduct product : statProducts) {
			productIds.add(product.getProductId());
		}
		return productIds;
	}

	/**
	 * 待出餐-退菜
	 * 
	 * @author chenkai
	 * @param storeOrderRefundItems
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void refundStoreMeal(List<StoreOrderRefundItem> storeOrderRefundItems) throws T5weiException {
		if (storeOrderRefundItems == null || storeOrderRefundItems.isEmpty()) {
			return;
		}
		int merchantId = storeOrderRefundItems.get(0).getMerchantId();
		long storeId = storeOrderRefundItems.get(0).getStoreId();
		Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId, false);
		
		Set<String> orderIds = new HashSet<String>();
		// 能后台出餐的直接出，不能出的打印退菜票
		for (StoreOrderRefundItem refundItem : storeOrderRefundItems) {
			orderIds.add(refundItem.getOrderId());
			StoreMealsCheckoutParam storeMealCheckoutParam = new StoreMealsCheckoutParam();
			storeMealCheckoutParam.setMerchantId(merchantId);
			storeMealCheckoutParam.setStoreId(storeId);
			storeMealCheckoutParam.setOrderId(refundItem.getOrderId());
			storeMealCheckoutParam.setRepastDate(refundItem.getRepastDate());
			storeMealCheckoutParam.setTimeBucketId(refundItem.getTimeBucketId());
			storeMealCheckoutParam.setPackaged(refundItem.isPacked());
			List<StoreMealCheckoutItemParam> storeMealCheckoutItems = new ArrayList<StoreMealCheckoutItemParam>();
			StoreMealCheckoutItemParam storeMealCheckoutItemParam = new StoreMealCheckoutItemParam();
			storeMealCheckoutItemParam.setChargeItemId(refundItem.getChargeItemId());
			storeMealCheckoutItemParam.setAmount(refundItem.getAmount());
			storeMealCheckoutItems.add(storeMealCheckoutItemParam);
			storeMealCheckoutParam.setStoreMealCheckoutItems(storeMealCheckoutItems);
			try {
				// 能退的直接从待出餐列表移除，进入已出餐标记为已退菜
				this.storeMealCheckout(storeMealCheckoutParam, true);
			} catch (T5weiException e) {
				// 不能退的需要打印退菜票
			}
			if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
				this.storeMealSweepService.refundMeal4Sweep(merchantId, storeId, refundItem);
			}
		}
		
		// 订单详情
        List<StoreOrder> storeOrders = storeOrderService.getStoreOrders(merchantId, storeId, new ArrayList<String>(orderIds));
        for (StoreOrder storeOrder : storeOrders) {
            if (storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
                storeOrderHelper.setStoreOrderDetail(storeOrder, false);
            }
        }
	    Map<String, StoreOrderRefundItem> refundItemMap = new HashMap<String, StoreOrderRefundItem>();
	    for (StoreOrderRefundItem refundItem : storeOrderRefundItems) {
	        if (refundItem.isRestoreInventory()) {
	            refundItemMap.put(refundItem.getOrderId() + "_" + refundItem.getChargeItemId() + "_" + refundItem.isPacked(), refundItem);
	        }
	    }
	    if (!refundItemMap.isEmpty()) {
	        List<QueryProductPortParam> queryProductPortParams = storeMealMultiHelper.storeOrderToQueryProductPortParams(storeOrders);
	        Map<String, Long> productPortMap = storeChargeItemService.getPortIdMap(merchantId, storeId, queryProductPortParams);
	        StoreMealPort parkagePort = storeMealPortService.getParkagePort(merchantId, storeId);
	        // 已出餐的记录相应的退菜票
	        List<StoreMealCheckout> storeMealCheckoutItems = new ArrayList<StoreMealCheckout>();
	        // 出餐口退菜
	        long currentTime = System.currentTimeMillis();
	        for (StoreOrder storeOrder : storeOrders) {
	            // 构造参数
	            String orderId = storeOrder.getOrderId();
	            long repastDate = storeOrder.getRepastDate();
	            long timeBucketId = storeOrder.getTimeBucketId();
	            int takeMode = storeOrder.getTakeMode();
	            int takeSerialNumber = storeOrder.getTakeSerialNumber();
	            int siteNumber = storeOrder.getSiteNumber();
	            boolean payAfter = storeOrder.isPayAfter();
	            long tableRecordId = storeOrder.getTableRecordId();
	            // 计算出餐口退菜
	            List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
	            for (StoreOrderItem storeOrderItem : storeOrderItems) {
	                long chargeItemId = storeOrderItem.getChargeItemId();
	                String chargeItemName = storeOrderItem.getChargeItemName();
	                // 退菜数量
	                StoreOrderRefundItem refundInItem = refundItemMap.get(orderId + "_" + chargeItemId + "_" + false);
	                StoreOrderRefundItem refundPackedItem = refundItemMap.get(orderId + "_" + chargeItemId + "_" + true);
	                double refundInAmount = 0D;
	                if (refundInItem != null) {
	                    refundInAmount = refundInItem.getAmount();
	                }
	                double refundPackedAmount = 0D;
	                if (refundPackedItem != null) {
	                    refundPackedAmount = refundPackedItem.getAmount();
	                }
	                // 没有退菜的跳过
	                if (refundInAmount == 0 && refundPackedAmount == 0) {
	                    continue;
	                }
	                // 组装退菜待出餐打印
	                List<StoreOrderSubitem> storeOrderSubitems = storeOrderItem.getStoreOrderSubitems();
	                for (StoreOrderSubitem storeOrderSubitem : storeOrderSubitems) {
	                    double amount = storeOrderSubitem.getAmount();
	                    long productId = storeOrderSubitem.getProductId();
	                    String productName = storeOrderSubitem.getProductName();
	                    String unit = storeOrderSubitem.getUnit();
	                    String remark = storeOrderSubitem.getRemark();
	                    String key = chargeItemId + "_" + productId;
	                    long portId = productPortMap.getOrDefault(key, 0L);
	                    StoreMealCheckout storeMealCheckout = new StoreMealCheckout();
	                    storeMealCheckout.setOrderId(orderId);
	                    storeMealCheckout.setMerchantId(merchantId);
	                    storeMealCheckout.setStoreId(storeId);
	                    storeMealCheckout.setPortId(portId);
	                    storeMealCheckout.setRepastDate(repastDate);
	                    storeMealCheckout.setTimeBucketId(timeBucketId);
	                    storeMealCheckout.setTakeMode(takeMode);
	                    storeMealCheckout.setTakeSerialNumber(takeSerialNumber);
	                    storeMealCheckout.setSiteNumber(siteNumber);
	                    storeMealCheckout.setChargeItemId(chargeItemId);
	                    storeMealCheckout.setChargeItemName(chargeItemName);
	                    storeMealCheckout.setProductId(productId);
	                    storeMealCheckout.setProductName(productName);
	                    storeMealCheckout.setAmount(amount);
	                    storeMealCheckout.setUnit(unit);
	                    storeMealCheckout.setRemark(remark);
	                    storeMealCheckout.setUpdateTime(currentTime);
	                    storeMealCheckout.setCreateTime(currentTime);
	                    storeMealCheckout.setSpicyLevel(storeOrderItem.getSpicyLevel());
	                    storeMealCheckout.setPrinted(true);
	                    storeMealCheckout.setRefundMeal(true);
	                    storeMealCheckout.setPayAfter(payAfter);
	                    storeMealCheckout.setTableRecordId(tableRecordId);
	                    if (refundPackedAmount > 0) {
	                        // 打包部分
	                        storeMealCheckout.setPackaged(true);
	                        storeMealCheckout.setAmountCheckout(-refundPackedAmount);// 退库存，负数记录
	                        // 指定打包出餐口
	                        if (parkagePort != null && parkagePort.getPortId() > 0 && store5weiSetting.getPrintMode() == StorePrintModeEnum.NORMAL_PRINT.getValue()) {
	                            storeMealCheckout.setPortId(parkagePort.getPortId());
	                        }
	                        storeMealCheckoutItems.add(storeMealCheckout);
	                    }
	                    if (refundInAmount > 0) {
	                        StoreMealCheckout notPackaged = BeanUtil.copy(storeMealCheckout, StoreMealCheckout.class);
	                        notPackaged.setPortId(portId);
	                        notPackaged.setPackaged(false);
	                        notPackaged.setAmountCheckout(-refundInAmount);// 退库存，负数记录
	                        storeMealCheckoutItems.add(notPackaged);
	                    }
	                }
	                // 订单子项目end
	            }
	        }
	        // 订单end
	        storeMealCheckoutDao.batchCreate(storeMealCheckoutItems);
	        // 恢复固定库存
	        storeInventoryService.refundFixInventory(storeMealCheckoutItems);
	    }
		// 刷新周期库存
		for (StoreOrder storeOrder : storeOrders) {
			storeInventoryService.updateInventoryDateByOrder(storeOrder);
			this.updateOrderTradeFinish(storeOrder, "refund store meal");
		}
	}
	
	/**
	 * 此方法目前仅用于退菜和清台
	 * @param storeOrder
	 * @param remark
	 */
	private void updateOrderTradeFinish(StoreOrder storeOrder, String remark){
		if (storeOrder == null) {
			return;
		}
		boolean enableSlave = false;
		int merchantId = storeOrder.getMerchantId(); 
		long storeId = storeOrder.getStoreId();
		int count = storeMealTakeupDao.countStoreMealTakeupsByOrderId(merchantId, storeId, storeOrder.getOrderId(), enableSlave);
		int clientType = storeOrder.getClientType();
		int optType = StoreOrderOptlogTypeEnum.MEAL_CHECKOUT.getValue();
		if (count == 0) {
			if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
				storeOrderService.updateOrderPrepareMealFinish(merchantId, storeId, storeOrder);
				optType = StoreOrderOptlogTypeEnum.MEAL_CHECKOUT_COMPLETE.getValue();
			} else {
				storeOrderService.updateOrderTradeFinish(storeOrder);
				optType = StoreOrderOptlogTypeEnum.TRADE_FINISH.getValue();
			}
		}
		storeOrderOptlogDao.createOptlog(storeOrder, 0, clientType, optType, remark);
	}

	/**
	 * 将订单列表中包含全部订单变为已出餐，包含将收费项目从待出餐列表移到已出餐列表
	 * 
	 * @author chenkai
	 * @param storeOrders
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void clearStoreMeal(List<StoreOrder> storeOrders) throws T5weiException {
		if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		long currentTime = System.currentTimeMillis();
		Set<String> orderIds = new HashSet<String>();
		List<StoreMealCheckout> storeMealCheckoutItems = new ArrayList<StoreMealCheckout>();
		int merchantId = storeOrders.get(0).getMerchantId();
		long storeId = storeOrders.get(0).getStoreId();
		for (StoreOrder storeOrder : storeOrders) {
			String orderId = storeOrder.getOrderId();
			List<StoreMealTakeup> storeMealTakeUpItems = storeMealTakeupDao.getStoreMealsByOrderId(merchantId, storeId, orderId, true, false);
			if (storeMealTakeUpItems == null || storeMealTakeUpItems.isEmpty()) {
				continue;
			}
			// 出餐记录
			int takeSerialSeq = this._updateStoreMealCheckoutRecord(storeOrder, 0, false);
			// 待出餐变为已出餐
			for (StoreMealTakeup storeMealTakeup : storeMealTakeUpItems) {
				StoreMealCheckout storeMealCheckout = BeanUtil.copy(storeMealTakeup, StoreMealCheckout.class);
				storeMealCheckout.setAmountCheckout(storeMealTakeup.getRemainTakeup());
				storeMealCheckout.setTakeSerialSeq(takeSerialSeq);
				storeMealCheckout.setCheckoutType(StoreMealPortCheckoutTypeEnum.AUTO.getValue());
				storeMealCheckout.setPrinted(true);
				storeMealCheckout.setUpdateTime(currentTime);
				storeMealCheckout.setCreateTime(currentTime);
				storeMealCheckoutItems.add(storeMealCheckout);
				// 根据订单删除
				orderIds.add(storeMealTakeup.getOrderId());
			}
		}
		// 根据订单删除
		storeMealTakeupDao.clearTakeupRemainByOrderIds(merchantId, storeId, new ArrayList<String>(orderIds));
		// 出餐信息保存
		storeMealCheckoutDao.batchCreate(storeMealCheckoutItems);
		// 刷新周期库存，更新订单为交易完成
		for (StoreOrder storeOrder : storeOrders) {
			storeInventoryService.updateInventoryDateByOrder(storeOrder);
			storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.FINISH.getValue());
			storeOrder.setUpdateTime(System.currentTimeMillis());
			storeOrder.update();
		}
	}

	/**
	 * 更改桌台记录起菜状态(后付费-起菜)
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void changeStoreTableRecordSendType(ChangeStoreOrderSendTypeParam param) throws T5weiException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		long tableRecordId = param.getTableRecordId();
		int storeSendType = param.getStoreSendType();
		int clientType = param.getClientType();
		long staffId = param.getStaffId();
		//根据桌台记录ID和起菜状态获取桌台记录下的子订单
		List<StoreOrder> storeOrders = storeOrderDao.getSubStoreOrderByTableRecordIdAndSendType(merchantId,storeId,tableRecordId,null,StoreSendTypeEnum.WAIT.getValue(),true);
		List<StoreOrderOptlog> storeOrderOptlogs = new ArrayList<>();
		for(StoreOrder storeOrder : storeOrders){
			storeOrder.setSendType(StoreSendTypeEnum.START_TAKE_ORDER.getValue());
			storeOrder.setSendTime(System.currentTimeMillis());
			storeOrder.setUpdateTime(System.currentTimeMillis());
			//构造订单操作日志
			StoreOrderOptlog storeOrderOptlog = new StoreOrderOptlog();
			storeOrderOptlog.setOrderId(storeOrder.getOrderId());
			storeOrderOptlog.setMerchantId(merchantId);
			storeOrderOptlog.setStoreId(storeId);
			storeOrderOptlog.setStaffId(staffId);
			storeOrderOptlog.setClientType(clientType);
			storeOrderOptlog.setOptType(StoreOrderOptlogTypeEnum.CHANGE_SEND_TYPE.getValue());
			storeOrderOptlog.setRemark("set sendType to start START_TAKE_ORDER【2】");
			storeOrderOptlog.setCreateTime(System.currentTimeMillis());
			storeOrderOptlogs.add(storeOrderOptlog);
		}
		storeOrderDao.batchUpdate(storeOrders);
		//订单的操作日志批量新增
		storeOrderOptlogDao.batchCreate(storeOrderOptlogs);

		//订单在桌台列表中不再显示“叫起”的标记和桌台信息中不再显示“起菜”的按钮；
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId,storeId,tableRecordId,true);
		//根据桌台记录ID更改桌台起菜状态 StoreSendTypeEnum
		storeTableRecord.setSendType(StoreSendTypeEnum.START_TAKE_ORDER.getValue());
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		// 后厨各加工档口、传菜口(如果传菜口是用小票划菜，需打印起菜的的小票)打印起菜的的小票，
		i5weiSendPrinter.sendPrintMessages(storeTableRecord,storeOrders,storeTableRecord.getUpdateTime(),staffId);
	}
	
}
