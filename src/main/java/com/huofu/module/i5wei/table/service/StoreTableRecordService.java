package com.huofu.module.i5wei.table.service;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutRecordDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.meal.service.StoreMealService;
import com.huofu.module.i5wei.meal.service.StoreMealSweepRecordService;
import com.huofu.module.i5wei.meal.service.StoreMealSweepService;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemPriceDAO;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.*;
import com.huofu.module.i5wei.order.entity.*;
import com.huofu.module.i5wei.order.facade.StoreOrderFacadeImpl;
import com.huofu.module.i5wei.order.facade.StoreOrderFacadeValidate;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.printer.I5weiKitchenMealListPrinter;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.setting.dao.Store5weiSettingDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import com.huofu.module.i5wei.table.dao.*;
import com.huofu.module.i5wei.table.entity.*;
import com.huofu.module.i5wei.table.facade.StoreTableRecordFacadeValidate;
import com.huofu.module.i5wei.wechat.WechatNotifyService;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.credit.CreditFacade;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreSendTypeEnum;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.sharedto.I5weiUserDTO;
import huofucore.facade.i5wei.sharedto.StoreTableStaffDTO;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.i5wei.table.*;
import huofucore.facade.i5wei.table.RefundResultDTO;
import huofucore.facade.merchant.staff.StaffDTO;
import huofucore.facade.merchant.staff.StaffFacade;
import huofucore.facade.pay.payment.*;
import huofucore.facade.user.info.UserDTO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.MapObject;
import huofuhelper.util.MoneyUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 桌台记录业务服务类
 * @author licheng7
 * 2016年4月27日 上午10:15:27
 */
@Service
public class StoreTableRecordService {

	@ThriftClient
    private StaffFacade.Iface staffFacade;
	
	@ThriftClient
    private PayFacade.Iface payFacade;
	
	@ThriftClient
    private RefundFacade.Iface refundFacade;
	
	@ThriftClient
	private OrderFacade.Iface orderFacade;
	
	@ThriftClient
    private CreditFacade.Iface creditFacade;
	
	@Autowired
	private StoreTableRecordDAO storeTableRecordDAO;
	
	@Autowired
	private StoreOrderDAO storeOrderDAO;
	
	@Autowired
	private StoreTableRecordRefundDAO storeTableRecordRefundDAO;
	
	@Autowired
	private StoreMealTakeupDAO storeMealTakeupDAO;
	
	@Autowired
	private StoreMealCheckoutDAO storeMealCheckoutDAO;
	
	@Autowired
	private StoreChargeItemDAO storeChargeItemDAO;
	
	@Autowired
	private StoreOrderRefundItemDAO storeOrderRefundItemDAO;
	
	@Autowired
	private StoreTableSettingService storeTableSettingService;
	
	@Autowired
	private StoreOrderItemDAO storeOrderItemDAO;
	
	@Autowired
	private StoreChargeItemPriceDAO storeChargeItemPriceDAO;
	
	@Autowired
    private StoreOrderHelper storeOrderHelper;
	
	@Autowired
	private StoreOrderService storeOrderService;
	
	@Autowired
	private StoreOrderSubitemDAO storeOrderSubitemDAO;
	
	@Autowired
	private StoreAreaService storeAreaService;
	
	@Autowired
	private StoreTableService storeTableService;
	
	@Autowired
    private IdMakerUtil idMakerUtil;
	
	@Autowired
	private StoreMealService storeMealService;
	
	@Autowired
	private StoreOrderFacadeImpl storeOrderFacadeImpl;
	
	@Autowired
	private StoreTableRecordHelper storeTableRecordHelper;
	
	@Autowired
	private TableRecordRefundHelper tableRecordRefundHelper;
	
	@Autowired
	private StoreMealCheckoutRecordDAO storeMealCheckoutRecordDAO;
	
	@Autowired
	private StoreChargeItemService storeChargeItemService;
	
	@Autowired
	private StoreTimeBucketService storeTimeBucketService;
	
	@Autowired
	private Store5weiSettingService store5weiSettingService;
	
	@Autowired
	private StoreTableRecordFacadeValidate storeTableRecordFacadeValidate;
	
	@Autowired
	private TableRecordBatchRefundRecordDAO tableRecordBatchRefundRecordDAO;
	
	@Autowired
    private StoreOrderFacadeValidate storeOrderFacadeValidate;
	
	@Autowired
	private StoreOrderOptlogDAO storeOrderOptlogDAO;
	
	@Autowired
	private StoreAreaDAO storeAreaDAO;
	
	@Autowired
	private StoreTableDAO storeTableDAO;

	@Autowired
	private I5weiKitchenMealListPrinter i5weiKitchenMealListPrinter;
	
	@Autowired
	private StoreMealSweepService storeMealSweepService;
	
	@Autowired
	private StoreMealSweepRecordService storeMealSweepRecordService;
	
	@Autowired
	private Store5weiSettingDAO store5weiSettingDAO;
	
	@Autowired
    private WechatNotifyService wechatNotifyService;
	
	@Autowired
    private I5weiMessageProducer i5weiMessageProducer;

	@Autowired
	private StoreTableRecordOptlogService storeTableRecordOptlogService;
	

	private static final Log log = LogFactory.getLog(StoreTableRecordService.class);
	
	// 桌台后付费批量退款：结账退款
	private static final int SETTLEREFUND = 1;
	// 桌台后付费批量退款：订单管理退款
	private static final int ORDERMANAGERREFUND = 0;
	
	/**
	 * 创建桌台记录(使用取餐码进行关联)
	 * @param createTableRecordWithTakeCodesParam 取餐码开台参数
	 * @return StoreTableRecord 桌台记录
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public OpenTableRecordResult createTableRecordWithTakeCodes (CreateTableRecordWithTakeCodesParam createTableRecordWithTakeCodesParam) throws TException {
		int merchantId = createTableRecordWithTakeCodesParam.getMerchantId(); // 商编
		long storeId = createTableRecordWithTakeCodesParam.getStoreId(); // 店铺id
		long repastDate = createTableRecordWithTakeCodesParam.getRepastDate(); // 就餐日期
		long timeBucketId = createTableRecordWithTakeCodesParam.getTimeBucketId();
		long tableId = createTableRecordWithTakeCodesParam.getTableId();
		int customerTraffic = createTableRecordWithTakeCodesParam.getCustomerTraffic();
		long defaultStaffId = createTableRecordWithTakeCodesParam.getStaffId(); // 桌台默认服务员id
		long createTableRecordStaffId = createTableRecordWithTakeCodesParam.getCreateTableRecordStaffId(); // 开台服务员id
		long userId = createTableRecordWithTakeCodesParam.getUserId(); // 自助开台用户id
		List<String> takeCodes = createTableRecordWithTakeCodesParam.getTakeCodes(); // 取餐码
		ClientTypeEnum clientType = ClientTypeEnum.CASHIER;
		repastDate = DateUtil.getBeginTime(repastDate, null);
		// 根据takeCodes查询出待关联到桌台记录的订单列表
		List<StoreOrder> orderList = new ArrayList<StoreOrder>();
		if (takeCodes != null && !takeCodes.isEmpty()) {
			for (String takeCode : takeCodes) {
				// 根据取餐码查询订单
				StoreOrder storeOrder = this.storeOrderDAO.getStoreOrderByTakeCode(merchantId, storeId, repastDate, takeCode, false);
				if (storeOrder != null) {
					orderList.add(storeOrder);
				} else { // 不存在的订单直接忽略
					log.warn("takeCode["+takeCode+"] invalid");
					throw new T5weiException(StoreTableErrorCodeEnum.TAKE_CODE_ERROR.getValue(), "takeCode["+takeCode+"] invalid");
				}
			}
		}
		// 开台处理
		OpenTableRecordRequestParam openTableRecordRequestParam = new OpenTableRecordRequestParam();
		openTableRecordRequestParam.setMerchantId(merchantId);
		openTableRecordRequestParam.setStoreId(storeId);
		openTableRecordRequestParam.setRepastDate(repastDate);
		openTableRecordRequestParam.setTimeBucketId(timeBucketId);
		openTableRecordRequestParam.setTableId(tableId);
		openTableRecordRequestParam.setCustomerTraffic(customerTraffic);
		openTableRecordRequestParam.setCreateTableRecordStaffId(createTableRecordStaffId);
		openTableRecordRequestParam.setUserId(userId);
		openTableRecordRequestParam.setOrderList(orderList);
		openTableRecordRequestParam.setClientType(clientType.getValue());
		openTableRecordRequestParam.setDefaultStaffId(defaultStaffId);
		OpenTableRecordResult openTableRecordResult = this.openTableRecord(openTableRecordRequestParam);
		return openTableRecordResult;
	}
	
	/**
	 * 创建桌台记录(使用订单号进行关联)
	 * @param createTableRecordWithOrderIdsParam 开台参数
	 * @return StoreTableRecord 桌台记录
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public OpenTableRecordResult createTableRecordWithOrderIds (CreateTableRecordWithOrderIdsParam createTableRecordWithOrderIdsParam) throws TException {
		int merchantId = createTableRecordWithOrderIdsParam.getMerchantId(); // 商编
		long storeId = createTableRecordWithOrderIdsParam.getStoreId(); // 店铺id
		long repastDate = createTableRecordWithOrderIdsParam.getRepastDate(); // 就餐日期
		long timeBucketId = createTableRecordWithOrderIdsParam.getTimeBucketId();
		long tableId = createTableRecordWithOrderIdsParam.getTableId();
		int customerTraffic = createTableRecordWithOrderIdsParam.getCustomerTraffic();
		long defaultStaffId = createTableRecordWithOrderIdsParam.getStaffId(); // 桌台默认服务员id
		long createTableRecordStaffId = createTableRecordWithOrderIdsParam.getCreateTableRecordStaffId(); // 开台服务员id
		long userId = createTableRecordWithOrderIdsParam.getUserId(); // 自助开台用户id
		List<String> orderIds = createTableRecordWithOrderIdsParam.getOrderIds(); // 待关联到桌台记录的订单列表
		ClientTypeEnum clientType = ClientTypeEnum.CASHIER;
		repastDate = DateUtil.getBeginTime(repastDate, null);
		// 获取待关联到桌台记录的订单列表
		List<StoreOrder> orderList = storeOrderDAO.getStoreOrdersInIdsForUpdate(merchantId, storeId, orderIds);
		if (orderIds != null && !orderIds.isEmpty()) {
			if (orderList == null || orderList.isEmpty()) {
				log.warn("orderIds = "+JsonUtil.build(orderIds)+" invalid");
				throw new T5weiException(StoreTableErrorCodeEnum.ORDER_ID_ERROR.getValue(), "orderIds = "+JsonUtil.build(orderIds)+" invalid");
			}
		}
		// 开台处理
		OpenTableRecordRequestParam openTableRecordRequestParam = new OpenTableRecordRequestParam();
		openTableRecordRequestParam.setMerchantId(merchantId);
		openTableRecordRequestParam.setStoreId(storeId);
		openTableRecordRequestParam.setRepastDate(repastDate);
		openTableRecordRequestParam.setTimeBucketId(timeBucketId);
		openTableRecordRequestParam.setTableId(tableId);
		openTableRecordRequestParam.setCustomerTraffic(customerTraffic);
		openTableRecordRequestParam.setCreateTableRecordStaffId(createTableRecordStaffId);
		openTableRecordRequestParam.setUserId(userId);
		openTableRecordRequestParam.setOrderList(orderList);
		openTableRecordRequestParam.setClientType(clientType.getValue());
		openTableRecordRequestParam.setDefaultStaffId(defaultStaffId);
		OpenTableRecordResult openTableRecordResult = this.openTableRecord(openTableRecordRequestParam);
		return openTableRecordResult;
	}
	
	/**
	 * 将订单关联到桌台记录
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @param orderIds 订单id集合
	 * @return StoreTableRecord 桌台记录
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public RelateSubOrderToTableRecordResult relateSubOrderToTableRecord (int merchantId, long storeId, long tableRecordId, List<String> orderIds) throws TException {
		List<StoreMealTakeup> storeMealTakeups = new ArrayList<>();
		// 获取店铺桌台设置
		StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
		if (!storeTableSetting.isEnableTableMode()) {
			// 非桌台模式不允许加菜
			log.warn("un-tableMode merchantId["+merchantId+"], storeId["+storeId+"] can not relate order to tableRecord");
			throw new T5weiException(StoreTableErrorCodeEnum.UN_ABLE_TABLE_MODE.getValue(), "un-tableMode merchantId["+merchantId+"], storeId["+storeId+"] can not relate order to tableRecord");
		}
		// 查询桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			log.warn("tableRecordId["+tableRecordId+"] invalid");
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		storeTableRecord = this.getMergeTableRecord(storeTableRecord);
		tableRecordId = storeTableRecord.getTableRecordId();
		RelateSubOrderToTableRecordResult relateSubOrderToTableRecordResult = new RelateSubOrderToTableRecordResult();
		List<StoreOrder> storeOrders = new ArrayList<StoreOrder>();
		// 已经结账或者清台的桌台记录如果想关联订单需要重新开台
		if (storeTableRecord.isClearTable() || storeTableRecord.isSettleMent()) {
			CreateTableRecordWithOrderIdsParam createTableRecordWithOrderIdsParam = new CreateTableRecordWithOrderIdsParam();
			createTableRecordWithOrderIdsParam.setTableId(storeTableRecord.getTableId());
			createTableRecordWithOrderIdsParam.setStaffId(storeTableRecord.getStaffId());
			createTableRecordWithOrderIdsParam.setCustomerTraffic(storeTableRecord.getCustomerTraffic());
			createTableRecordWithOrderIdsParam.setMerchantId(storeTableRecord.getMerchantId());
			createTableRecordWithOrderIdsParam.setStoreId(storeTableRecord.getStoreId());
			createTableRecordWithOrderIdsParam.setRepastDate(storeTableRecord.getRepastDate());
			createTableRecordWithOrderIdsParam.setTimeBucketId(storeTableRecord.getTimeBucketId());
			createTableRecordWithOrderIdsParam.setOrderIds(orderIds);
			createTableRecordWithOrderIdsParam.setUserId(storeTableRecord.getCreateTableUserId());
			createTableRecordWithOrderIdsParam.setCreateTableRecordStaffId(storeTableRecord.getCreateTableStaffId());
			OpenTableRecordResult openTableRecordResult = this.createTableRecordWithOrderIds(createTableRecordWithOrderIdsParam);
			relateSubOrderToTableRecordResult.setStoreOrders(openTableRecordResult.getStoreOrders());
			relateSubOrderToTableRecordResult.setStoreTableRecord(openTableRecordResult.getStoreTableRecord());
			relateSubOrderToTableRecordResult.setMasterOrder(openTableRecordResult.getMasterOrder());
			return relateSubOrderToTableRecordResult;
		}
		// 结账中或结账失败状态的桌台记录不允许关联子订单
		if (storeTableRecord.isSettling() || storeTableRecord.isSettleFail()) {
			log.warn("tableRecordId["+tableRecordId+"] tableRecordStatus is SETTLING or SETTLE_FAIL, can not relate order");
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLING_OR_FAIL_CAN_NOT_RELATE_SUB_ORDER.getValue(), "tableRecordId["+tableRecordId+"] tableRecordStatus is SETTLING or SETTLE_FAIL, can not relate order");
		}
		StoreOrder masterStoreOrder = null;
		// 标记，如果子订单被关联桌台记录，桌台记录需要重新获取状态
		boolean needUpdateTableRecord = false; 
		long userId = storeTableRecord.getCreateTableUserId();
		for (String orderId : orderIds) {
			// 查询子订单
			StoreOrder subStoreOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, false);
			if (subStoreOrder == null) {
				log.warn("orderId["+orderId+"] invalid");
				throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "orderId["+orderId+"] invalid");
			}
			// 如果已经是桌台记录子订单，忽略
			if (subStoreOrder.isTableRecordSubOrder()) {
				continue;
			}
			if (subStoreOrder.isTableRecordMasterOrder()) {
				continue;
			}
			// 设置子订单是否为后付费
			if (!storeTableSetting.isEnablePayAfter()) {
				// 若非后付费模式，需要校验子订单是否已经支付完成
				if (!subStoreOrder.isPayFinish()) {
					log.warn("store["+storeId+"] setting is unable payAfter, order["+orderId+"] payStatus can not be ["+StoreOrderPayStatusEnum.findByValue(subStoreOrder.getPayStatus())+"]");
					throw new T5weiException(StoreTableErrorCodeEnum.PAY_BEFORE_CAN_NOT_UNPAY.getValue(), "store["+storeId+"] setting is unable payAfter, order["+orderId+"] payStatus can not be ["+StoreOrderPayStatusEnum.findByValue(subStoreOrder.getPayStatus())+"]");
				}
				subStoreOrder.setPayAfter(false);
			} else {
				subStoreOrder.setPayAfter(true);
			}
			// 绑定桌台的订单不允许是外送和快取模式
			storeTableRecordFacadeValidate.checkSubOrderTakeMode(subStoreOrder);
			// 订单状态校验:桌台记录在结账前，子订单不允许赊账和退款
			storeTableRecordFacadeValidate.checkSubOrderStatus(storeTableRecord, subStoreOrder);
			// 查询主订单
			masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, false);
			// 主订单为空时需要创建主订单，并设置订单是否为加菜
			if (masterStoreOrder == null) {
				masterStoreOrder = storeOrderService.createMasterStoreOrder(subStoreOrder, tableRecordId);
				masterStoreOrder.setCustomerTraffic(storeTableRecord.getCustomerTraffic());
				masterStoreOrder.setUpdateTime(System.currentTimeMillis());
				masterStoreOrder.update();
				storeTableRecord.setTakeSerialNumber(masterStoreOrder.getTakeSerialNumber());
				storeTableRecord.setOrderTime(System.currentTimeMillis());
				//桌台记录操作日志 TODO 缺少员工ID
				storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, tableRecordId, 0, subStoreOrder.getUserId(),
						ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_MASTER_CREATE.getValue(), "桌台记录的主订单创建", masterStoreOrder.getCreateTime());
			} else {
				boolean isUpdate = false;
				if (masterStoreOrder.getUserId() == 0 && subStoreOrder.getUserId() > 0) {
					masterStoreOrder.setUserId(subStoreOrder.getUserId());
					masterStoreOrder.setUpdateTime(System.currentTimeMillis());
					isUpdate = true;
				}
				if (masterStoreOrder.getClientType() == ClientTypeEnum.CASHIER.getValue() && subStoreOrder.getClientType() != ClientTypeEnum.CASHIER.getValue()) {
					masterStoreOrder.setClientType(subStoreOrder.getClientType());
					masterStoreOrder.setUpdateTime(System.currentTimeMillis());
					isUpdate = true;
				}
				if (isUpdate) {
					masterStoreOrder.update();
				}
			}
			// 校验桌台记录主订单id和主订单orderId是否一致
			storeTableRecordFacadeValidate.checkMasterOrderId(storeTableRecord, masterStoreOrder);
			// 更新子订单状态
			subStoreOrder.setTableRecordId(storeTableRecord.getTableRecordId());
			subStoreOrder.setParentOrderId(masterStoreOrder.getOrderId());
			subStoreOrder.setUpdateTime(System.currentTimeMillis());
			
			List<StoreOrder> subOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrder.getOrderId(), false);
			if (subOrderList != null && !subOrderList.isEmpty()) {
				if (subOrderList.get(0).getOrderId().equals(subStoreOrder.getOrderId())) {
					subStoreOrder.setEnableAddDishes(false);
				} else {
					subStoreOrder.setEnableAddDishes(true);
				}
			} else {
				subStoreOrder.setEnableAddDishes(false);
			}
			subStoreOrder.setEnableManualCustomerTraffic(true);
			subStoreOrder.update();
			// 取餐
			if (subStoreOrder.getTakeSerialNumber() == 0) { // takeSerialNumber>0认为是已取餐，跳过取餐环节
				StoreOrderTakeCodeParam storeOrderTakeCodeParam = new StoreOrderTakeCodeParam();
				storeOrderTakeCodeParam.setMerchantId(merchantId);
				storeOrderTakeCodeParam.setStoreId(storeId);
				storeOrderTakeCodeParam.setOrderId(subStoreOrder.getOrderId());
				storeOrderTakeCodeParam.setClientType(ClientTypeEnum.CASHIER.getValue());
				storeOrderTakeCodeParam.setTakeMode(StoreOrderTakeModeEnum.findByValue(subStoreOrder.getTakeMode()));
				subStoreOrder = storeOrderService.takeCodeStoreOrder(storeOrderTakeCodeParam);
				storeMealTakeups.addAll(subStoreOrder.getStoreMealTakeups());
				storeOrders.add(subStoreOrder);
			}
			needUpdateTableRecord = true;
			if (userId == 0 && subStoreOrder.getUserId() > 0) {
				userId = subStoreOrder.getUserId();
			}
		}
		if (needUpdateTableRecord) {
			// 更新桌台记录信息
			this.refreshTableRecord(merchantId, storeId, storeTableRecord);
			storeTableRecord.setUpdateTime(System.currentTimeMillis());
			storeTableRecord.setOrderId(masterStoreOrder.getOrderId());
			storeTableRecord.update();
		}
		relateSubOrderToTableRecordResult.setStoreTableRecord(storeTableRecord);
		relateSubOrderToTableRecordResult.setUserId(userId);
		relateSubOrderToTableRecordResult.setStoreOrders(storeOrders);
		relateSubOrderToTableRecordResult.setMasterOrder(masterStoreOrder);
		relateSubOrderToTableRecordResult.setStoreMealTakeups(storeMealTakeups);
		return relateSubOrderToTableRecordResult;
	}
	
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public ClearTableRecordResult clearTableRecord(ClearTableRecordParam clearTableRecordParam, Store5weiSetting store5weiSetting) throws TException {
		ClearTableRecordResult clearTableRecordResult = new ClearTableRecordResult();
		int merchantId = clearTableRecordParam.getMerchantId(); // 商编
		long storeId = clearTableRecordParam.getStoreId(); // 店铺id
		long staffId = clearTableRecordParam.getStaffId(); // 清台员工id
		long tableRecordId = clearTableRecordParam.getTableRecordId(); // 桌台记录id
		// 查询出桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		clearTableRecordResult.setPreClearTableRecordStatus(TableRecordStatusEnum.findByValue(storeTableRecord.getTableRecordStatus()));
		// 桌台记录如果为已经清台，不允许重复操作
		if (storeTableRecord.isClearTable()) {
			clearTableRecordResult.setStoreTableRecord(storeTableRecord);
			return clearTableRecordResult;
		}
		List<StoreOrder> subStoreOrderList = null;
		// 桌台记录为已经结账或等待点餐，可以直接清台
		if (!storeTableRecord.isSettleMent() && !storeTableRecord.isWaitMeal()) {
			// 结账中的桌台记录不允许直接清台
			if (storeTableRecord.isSettling()) {
				log.warn("tableRecord["+tableRecordId+"] status is SETTLING, can not clear table");
				throw new T5weiException(StoreTableErrorCodeEnum.SETTLING_CAN_NOT_CLEAR_TABLE.getValue(), "tableRecord["+tableRecordId+"] status is SETTLING, can not clear table");
			}
			long waitSettleAmount = storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount());
			if (waitSettleAmount != 0) {
				log.warn("tableRecord["+tableRecordId+"] pay status["+PayStatusEnum.findByValue(storeTableRecord.getPayStatus())+"] is not pay finish, can not clear tableRecord");
				throw new T5weiException(StoreTableErrorCodeEnum.UN_ALL_PAY_CAN_NOT_CLEAR_TABLE_RECORD.getValue(), "tableRecord["+tableRecordId+"] pay status["+PayStatusEnum.findByValue(storeTableRecord.getPayStatus())+"] is not pay finish, can not clear tableRecord");
			}
			// 查询桌台记录关联主订单
			StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, true);
			if (masterStoreOrder == null) {
				log.warn("tableRecord["+tableRecordId+"] request clearTable can not find masterStoreOrder");
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"] request clearTable can not find masterStoreOrder");
			}
			List<StoreOrder> toTradeFinishOrders = new ArrayList<StoreOrder>();
			// 将主订单交易状态修改为已完成
			if (masterStoreOrder.getTradeStatus() != StoreOrderTradeStatusEnum.FINISH.getValue()) {
				toTradeFinishOrders.add(masterStoreOrder);
			}
			// 查询桌台记录关联全部子订单
			subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrder.getOrderId(), false);
			if (subStoreOrderList.isEmpty()) {
				// 主订单应该跟随第一个子订单一起生成，出现有子订单没有主订单的情况抛异常
				log.warn("tableRecord["+tableRecordId+"],masterOrder["+masterStoreOrder.getOrderId()+"] subStoreOrderList is empty");
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"],masterOrder["+masterStoreOrder.getOrderId()+"] subStoreOrderList is empty");
			}
			// 将子订单全部置为已交易完成状态
			for (StoreOrder subStoreOrder : subStoreOrderList) {
				if (subStoreOrder.getTradeStatus() != StoreOrderTradeStatusEnum.FINISH.getValue()) {
					toTradeFinishOrders.add(subStoreOrder);
				}
			}
			storeOrderService.updateOrderTradeFinish(tableRecordId, toTradeFinishOrders);
		}
		if (subStoreOrderList == null) {
			subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, storeTableRecord.getOrderId(), false);
		}
		// 检验出餐状态，如果存在未出餐状态的餐，将状态改为已出餐
		if (clearTableRecordParam.isMealComplete()) {
            // 将桌台记录包含全部子订单变为已出餐
            /*storeMealService.clearStoreMeal(subStoreOrderList);
            if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
                // 将桌台记录包含全部子订单变为已划菜
                storeMealSweepService.sweepStoreMealByOrderId(merchantId, storeId, tableRecordId, storeTableRecord.getOrderId());
            }*/
			this.clearStoreMeal(merchantId, storeId, tableRecordId, storeTableRecord.getOrderId(), subStoreOrderList, store5weiSetting);
		}
		if (!storeTableRecord.isSettleMent()) {
			// 更新桌台记录
			this.refreshTableRecord(merchantId, storeId, storeTableRecord);
		}
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.CLEAR_TABLE.getValue());
		storeTableRecord.setClearTableStaffId(staffId);
		storeTableRecord.setClearTableTime(System.currentTimeMillis());
		storeTableRecord.update();
		//记录清台日志
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, tableRecordId, staffId, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_CLEAR.getValue(),
				"桌台记录清台", storeTableRecord.getClearTableTime());
		clearTableRecordResult.setStoreTableRecord(storeTableRecord);
		return clearTableRecordResult;
	}

	private int getSendType(StoreTableRecord originalTableRecord,StoreTableRecord targetTableRecord){
		int originalTableSendType = originalTableRecord.getSendType();
		int targetTableSendType = targetTableRecord.getSendType();
		int resultType = StoreSendTypeEnum.TAKE_ORDER.getValue();
		if(originalTableSendType == StoreSendTypeEnum.URGENT.getValue() || targetTableSendType == StoreSendTypeEnum.URGENT.getValue()){
			resultType = StoreSendTypeEnum.URGENT.getValue();
		}
		//优先叫起，次之加急，最后即起
		if(originalTableSendType == StoreSendTypeEnum.WAIT.getValue() || targetTableSendType == StoreSendTypeEnum.WAIT.getValue()){
			resultType = StoreSendTypeEnum.WAIT.getValue();
		}
		return resultType;
	}

	/**
	 * 合台
	 * @param mergeTableRecordParam 合台请求参数
	 * @return StoreTableRecord 桌台记录
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public MergeTableRecordResult mergeTableRecord (MergeTableRecordParam mergeTableRecordParam, Store5weiSetting store5weiSetting) throws TException {
		int merchantId = mergeTableRecordParam.getMerchantId(); // 商编
		long storeId = mergeTableRecordParam.getStoreId(); // 店铺id
		long staffId = mergeTableRecordParam.getStaffId(); // 合台员工id
		long originalTableRecordId = mergeTableRecordParam.getOriginalTableRecordId(); // 原始桌台记录id
		long targetTableRecordId = mergeTableRecordParam.getTargetTableRecordId(); // 目标桌台记录id
		MergeTableRecordResult mergeTableRecordResult = new MergeTableRecordResult();
		// 自己不能和自己合
		if (originalTableRecordId == targetTableRecordId) {
			log.warn("originalTableRecordId["+originalTableRecordId+"] and targetTableRecordId["+targetTableRecordId+"] must be different");
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "originalTableRecordId["+originalTableRecordId+"] and targetTableRecordId["+targetTableRecordId+"] must be different");
		}
		// 查询原始桌台记录
		StoreTableRecord originalTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, originalTableRecordId, true);
		if (originalTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "originalTableRecordId["+originalTableRecordId+"] invalid");
		}
		mergeTableRecordResult.setOriginalTableRecord(originalTableRecord);
		// 查询目标桌台记录
		StoreTableRecord targetTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, targetTableRecordId, true);
		//根据原始桌台和目标桌台的起菜状态，得出最终合完台的起菜状态
		int sendType = this.getSendType(originalTableRecord,targetTableRecord);

		if (targetTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "targetTableRecordId["+targetTableRecordId+"] invalid");
		}
		// 判断原始桌台桌台状态
		if (originalTableRecord.isClearTable() || originalTableRecord.isSettleMent() || originalTableRecord.isSettling() || originalTableRecord.isSettleFail()) {
			// 原始桌台已清台 || 已结账 || 结账中不许合台
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_MERGE.getValue(), "originalTableRecord["+originalTableRecord.getTableRecordId()+"] status is CLEAR_TABLE or SETTLEMENT, can not merge table record");
		}
		// 判断目标桌台桌台状态
		if (targetTableRecord.isClearTable() || targetTableRecord.isSettleMent() || targetTableRecord.isSettling() || targetTableRecord.isSettleFail()) {
			// 目标桌台已清台 || 已结账 || 结账中不许合台
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_MERGE.getValue(), "targetTableRecord["+targetTableRecord.getTableRecordId()+"] status is CLEAR_TABLE or SETTLEMENT, can not merge table record");
		}
		// 查询原始桌台记录的主订单
		String originalMasterOrderId = originalTableRecord.getOrderId();
		StoreOrder originalMasterOrder = null;
		if (!StringUtils.isNullOrEmpty(originalMasterOrderId)) {
			originalMasterOrder = storeOrderDAO.getById(merchantId, storeId, originalMasterOrderId, false, false);
			// 合台时两个桌台记录主订单均不允许已支付
			if (originalMasterOrder.getPayStatus() != StoreOrderPayStatusEnum.NOT.getValue()) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "originalMasterOrder["+originalMasterOrder.getOrderId()+"] pay status is not NOT(UN_PAY), can not merge table record");
			}
		}
		// 查询目标桌台记录的主订单
		String targetMasterOrderId = targetTableRecord.getOrderId();
		StoreOrder targetMasterOrder = null;
		if (!StringUtils.isNullOrEmpty(targetMasterOrderId)) {
			targetMasterOrder = storeOrderDAO.getById(merchantId, storeId, targetMasterOrderId, false, false);
			// 合台时两个桌台记录主订单均不允许已支付
			if (targetMasterOrder.getPayStatus() != StoreOrderPayStatusEnum.NOT.getValue()) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "targetMasterOrder["+targetMasterOrder.getOrderId()+"] pay status is not NOT(UN_PAY), can not merge table record");
			}
		}
		// 查询原始桌台记录关联的子订单列表
		List<StoreOrder> oriSubOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, originalTableRecordId, originalMasterOrderId, true);
		mergeTableRecordResult.setSubOrderList(oriSubOrderList);
		// 如果原始桌台记录关联的子订单列表不为空，则需要判断目标桌台是否已经有主订单，如果没有需要生成
		if (!oriSubOrderList.isEmpty() && targetMasterOrder == null) {
			targetMasterOrder = storeOrderService.createMasterStoreOrder(oriSubOrderList.get(0), targetTableRecordId);
			targetTableRecord.setTakeSerialNumber(targetMasterOrder.getTakeSerialNumber());
			targetTableRecord.setOrderId(targetMasterOrder.getOrderId());
			targetTableRecord.setOrderTime(System.currentTimeMillis());
		}
		// 将原始桌台记录关联的子订单转移到目标桌台记录下
		for (StoreOrder subStoreOrder : oriSubOrderList) {
			subStoreOrder.setParentOrderId(targetMasterOrder.getOrderId()); // 设置父订单id为目标桌台对应的主订单
			subStoreOrder.setTableRecordId(targetTableRecordId); // 设置桌台记录id为目标桌台记录
			subStoreOrder.setUpdateTime(System.currentTimeMillis());
			subStoreOrder.update();
		}
		// 将原始桌台记录待出餐、已出餐table_record_id修改成目标桌台记录id
		storeMealCheckoutDAO.updateTableRecordId(merchantId, storeId, originalTableRecordId, targetTableRecordId);
		storeMealTakeupDAO.updateTableRecordId(merchantId, storeId, originalTableRecordId, targetTableRecordId);
		if(store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
		    storeMealSweepService.updateTableRecordId(merchantId, storeId, originalTableRecordId, targetTableRecordId);
		    storeMealSweepRecordService.updateTableRecordId(merchantId, storeId, originalTableRecordId, targetTableRecordId, originalMasterOrderId, targetMasterOrderId);
		}
		// 查询出原始桌台记录的退菜列表
		List<StoreTableRecordRefund> originalStoreTableRecordRefundList = storeTableRecordRefundDAO.getStoreTableRecordRefundsByTableRecordId(merchantId, storeId, originalTableRecordId);
		// 将原始桌台的退菜记录挂到目标桌台记录上
		for (StoreTableRecordRefund storeTableRecordRefund : originalStoreTableRecordRefundList) {
			storeTableRecordRefund.setTableRecordId(targetTableRecordId);
			storeTableRecordRefund.setUpdateTime(System.currentTimeMillis());
			storeTableRecordRefund.update();
		}
		// 查询出原始桌台订单退菜详情
		List<StoreOrderRefundItem> originalStoreOrderRefundItemList = storeOrderRefundItemDAO.getStoreOrderRefundItems(merchantId, storeId, originalTableRecordId);
		for (StoreOrderRefundItem storeOrderRefundItem : originalStoreOrderRefundItemList) {
			storeOrderRefundItem.setTableRecordId(targetTableRecordId);
			storeOrderRefundItem.setUpdateTime(System.currentTimeMillis());
			storeOrderRefundItem.update();
		}
		// 删除原始桌台主订单
		if (originalMasterOrder != null) {
			originalMasterOrder.delete();
		}
		// 再将原始桌台记录置为清台状态
		originalTableRecord.setUpdateTime(System.currentTimeMillis());
		originalTableRecord.setTableRecordStatus(TableRecordStatusEnum.CLEAR_TABLE.getValue());
		originalTableRecord.setMergeTableTime(System.currentTimeMillis());
		originalTableRecord.setClearTableTime(System.currentTimeMillis());
		originalTableRecord.setMergeTableRecordId(targetTableRecordId);
		originalTableRecord.update();
		//记录原始桌台的清台记录 TODO 和台的入参没有客户端类型
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, originalTableRecordId, staffId, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_CLEAR.getValue(),
				"和台操作,原始桌台记录[" + originalTableRecord.getTableRecordId() + "]和台到目标桌台记录[" + targetTableRecord.getTableRecordId() + "],最后原始桌台记录执行清台",
				originalTableRecord.getClearTableTime());
		// 修改目标桌台对应的桌台记录
		targetTableRecord.setMergeStaffId(staffId);
		targetTableRecord.setCustomerTraffic(targetTableRecord.getCustomerTraffic() + originalTableRecord.getCustomerTraffic());
        targetTableRecord.setReductionTableFee(targetTableRecord.getReductionTableFee() + originalTableRecord.getReductionTableFee());
		this.refreshTableRecord(merchantId, storeId, targetTableRecord);
		this.updateTargetTableRecordTimes(targetTableRecord, originalTableRecord);
		StoreOrderPlaceParam storeOrderPlaceParam = new StoreOrderPlaceParam();
		storeOrderPlaceParam.setMerchantId(merchantId);
		storeOrderPlaceParam.setStoreId(storeId);
		storeOrderPlaceParam.setTimeBucketId(targetTableRecord.getTimeBucketId());
		storeOrderPlaceParam.setTakeMode(StoreOrderTakeModeEnum.DINE_IN.getValue());
		storeOrderPlaceParam.setEnableAddDishes(false);
		long payAbleTableFee = this.getTableFee(storeOrderPlaceParam, targetTableRecord.getCustomerTraffic());
		targetTableRecord.setPayAbleTableFee(payAbleTableFee);
		//起菜状态
		targetTableRecord.setSendType(sendType);
		targetTableRecord.setUpdateTime(System.currentTimeMillis());
		targetTableRecord.update();
		//记录原始桌台的清台记录 TODO 和台的入参没有客户端类型 记得时间是原始桌台的和台操作的时间
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, targetTableRecordId, staffId, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_MERGE.getValue(),
				"和台操作,原始桌台记录[" + originalTableRecord.getTableRecordId() + "]和台到目标桌台记录[" + targetTableRecord.getTableRecordId() + "]",
				originalTableRecord.getMergeTableTime());
		mergeTableRecordResult.setTargetTableRecord(targetTableRecord);
		if (targetMasterOrder != null) {
			targetMasterOrder.setCustomerTraffic(targetTableRecord.getCustomerTraffic());
			targetMasterOrder.setUpdateTime(System.currentTimeMillis());
			targetMasterOrder.update();
		}
		return mergeTableRecordResult;
	}
	
	/**
	 * 转台
	 * @param transferTableRecordParam 转台请求参数
	 * @return StoreTableRecord 桌台记录
	 * @throws T5weiException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public TransferTableRecordResult transferTableRecord(TransferTableRecordParam transferTableRecordParam) throws T5weiException {
		int merchantId = transferTableRecordParam.getMerchantId(); // 商户id
		long storeId = transferTableRecordParam.getStoreId(); // 店铺id
		long originalTableRecordId = transferTableRecordParam.getOriginalTableRecordId(); // 原桌台记录id
		long targetTableId = transferTableRecordParam.getTargetTableId(); // 目标桌台id
		long staffId = transferTableRecordParam.getStaffId();
		TransferTableRecordResult transferTableRecordResult = new TransferTableRecordResult();
		// 根据originalTableRecordId查询出原桌台记录
		StoreTableRecord originalTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, originalTableRecordId, true);
		if (originalTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "originalTableRecordId["+originalTableRecordId+"] invalid");
		}
		StoreTableRecord originalTableRecordCopy = new StoreTableRecord();
		BeanUtil.copy(originalTableRecord, originalTableRecordCopy);
		transferTableRecordResult.setOriginalTableRecord(originalTableRecordCopy);
		// 比较桌台，不允许往当前桌台上转
		if (originalTableRecord.getTableId() == targetTableId) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "originalTableRecord["+originalTableRecord+"] request transferTableRecord, targetTableId["+targetTableId+"] and oriTableId["+originalTableRecord.getTableId()+"] can not be same");
		}
		// 获取目标桌台
		StoreTable storeTable = storeTableRecordHelper.getStoreTable(merchantId, storeId, targetTableId);

		if (storeTable == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "targetTableId["+targetTableId+"] invalid");
		}
		StoreArea storeArea = storeTableRecordHelper.getStoreArea(merchantId, storeId, storeTable.getAreaId());
		if (storeArea == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "targetAreaId["+storeTable.getAreaId()+"] invalid");
		}
		transferTableRecordResult.setTargetTable(storeTable);
		// 查询原始桌台记录关联的子订单列表
		List<StoreOrder> oriSubOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, originalTableRecordId, originalTableRecord.getOrderId(), true);
		transferTableRecordResult.setSubOrderList(oriSubOrderList);
		// 拼桌序号
		int tableRecordSeq = 0;
		// 查找桌台id下是否还挂有其他桌台记录
		List<StoreTableRecord> storeTableRecordList = storeTableRecordDAO.getStoreTableRecordListByTableIdSeqDesc (merchantId, storeId, targetTableId, true);
		// 如果table下的桌台记录非空，说明是拼桌
		if (!storeTableRecordList.isEmpty()) {
			// 查询桌台记录列表中最大的拼桌序号
			int minTableRecordSeq = storeTableRecordList.get(storeTableRecordList.size()-1).getTableRecordSeq();
			if (minTableRecordSeq == 0) {
				int maxTableRecordSeq = storeTableRecordList.get(0).getTableRecordSeq();
				tableRecordSeq = maxTableRecordSeq + 1;
			}
		}
		// 修改桌台id
		originalTableRecord.setTableId(targetTableId);
		originalTableRecord.setAreaId(storeArea.getAreaId());
		originalTableRecord.setAreaName(storeArea.getAreaName());
		originalTableRecord.setTableName(storeTable.getName());
		originalTableRecord.setTableRecordSeq(tableRecordSeq);
		originalTableRecord.setTransferStaffId(staffId);
		originalTableRecord.setUpdateTime(System.currentTimeMillis());
		originalTableRecord.update();
		//记录桌台记录的清台
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, originalTableRecord.getTableRecordId(), staffId, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_TRANSFER.getValue(),
				"转台操作,桌台记录[" + originalTableRecord.getTableRecordId() + "] 从" + originalTableRecordCopy.getTableName()
						+ "[" + originalTableRecordCopy.getTableId() + "] 转到了 " + originalTableRecord.getTableName() + "[" + originalTableRecord.getTableId() + "]",
				originalTableRecord.getUpdateTime());

		this.isAllTakeOut(merchantId, storeId, originalTableRecord);
		transferTableRecordResult.setTableRecord(originalTableRecord);
		return transferTableRecordResult;
	}
	
	/**
	 * 点菜/加菜
	 * @param tableRecordId 加菜桌台记录id
	 * @param storeOrder 加菜订单
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreTableRecord orderAddChargeItem (long tableRecordId, StoreOrder storeOrder,int storeSendType) throws TException {
		int merchantId = storeOrder.getMerchantId(); // 商编
		long storeId = storeOrder.getStoreId(); // 店铺id
		// 桌台记录id为0，非桌台加菜，只是普通订单，直接返回
		if (tableRecordId == 0) {
			//预定订单默认为 即起
			storeOrder.setSendType(StoreSendTypeEnum.TAKE_ORDER.getValue());
			storeOrder.update();
			return null;
		} 
		// 加菜订单取餐模式不能为未知、快取、外送
		if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.UNKNOWN.getValue() || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.QUICK_TAKE.getValue() || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
			return null;
		}
		// 获取店铺桌台配置
		StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
		// 订单关联桌台记录
		if (!storeTableSetting.isEnableTableMode()) {
			// 非桌台模式不允许加菜
			// 非桌台模式起菜状态默认为 即起
			storeOrder.setSendType(StoreSendTypeEnum.TAKE_ORDER.getValue());
			return null;
		}
		if (storeOrder.isTableRecordSubOrder()) {
			return null;
		}
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		//设置桌台记录和订单的起菜状态
		this._setSendType(storeOrder,storeTableRecord,storeSendType);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		storeOrder.setTableRecordId(tableRecordId);
		storeOrder.setPayAfter(storeTableSetting.isEnablePayAfter());
		if (storeOrder.getPayablePrice() > 0) {
			if (!storeTableSetting.isEnablePayAfter() && storeOrder.getPayStatus() != StoreOrderPayStatusEnum.FINISH.getValue()) {
				// 非后付费，订单支付前不允许加菜，但是要将tableRecordId记录到订单中
				storeOrder.setUpdateTime(System.currentTimeMillis());
				storeOrder.update();
				return null;
			}
		}
		// 判断桌台记录状态
		int tableStatus = storeTableRecord.getTableRecordStatus();
		if (storeTableRecord.isSettling() || storeTableRecord.isSettleFail()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLING_OR_FAIL_CAN_NOT_RELATE_SUB_ORDER.getValue(), "tableRecord["+tableRecordId+"] tableStatus is "+TableRecordStatusEnum.findByValue(tableStatus)+" can not add item");
		}
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
			// 已经结账、已经清台的桌台记录不允许加菜
			throw new T5weiException(StoreTableErrorCodeEnum.TABLE_RECORD_STATUS_ERROR.getValue(), "tableRecord["+tableRecordId+"] tableStatus is "+TableRecordStatusEnum.findByValue(tableStatus)+" can not add item");
		}
		// 查询桌台记录关联主订单
		StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, false);
		// 主订单不存在则创建
		if (masterStoreOrder == null) {
			if (!StringUtils.isNullOrEmpty(storeTableRecord.getOrderId())) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "system exception, tableRecord["+storeTableRecord.getTableRecordId()+"], masterOrderId=["+storeTableRecord.getOrderId()+"], masterOrder=null");
			}
			// 创建主订单
			masterStoreOrder = storeOrderService.createMasterStoreOrder(storeOrder, tableRecordId);
			masterStoreOrder.setCustomerTraffic(storeTableRecord.getCustomerTraffic());
			masterStoreOrder.setUpdateTime(System.currentTimeMillis());
			masterStoreOrder.update();
			storeTableRecord.setTakeSerialNumber(masterStoreOrder.getTakeSerialNumber());
			storeTableRecord.setOrderId(masterStoreOrder.getOrderId());
			storeTableRecord.setOrderTime(System.currentTimeMillis());
			//桌台记录操作日志
			storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, tableRecordId, storeOrder.getStaffId(), storeOrder.getUserId(),
					storeOrder.getClientType(), TableRecordOptlogTypeEnum.TABLE_MASTER_CREATE.getValue(), "桌台记录的主订单创建", masterStoreOrder.getCreateTime());
		} else {
			if (masterStoreOrder.getClientType() == ClientTypeEnum.CASHIER.getValue() && storeOrder.getClientType() != ClientTypeEnum.CASHIER.getValue()) {
				masterStoreOrder.setClientType(storeOrder.getClientType());
				masterStoreOrder.setUpdateTime(System.currentTimeMillis());
				masterStoreOrder.update();
			}
		}
		
		List<StoreOrder> subOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrder.getOrderId(), false);
		if (subOrderList != null && !subOrderList.isEmpty()) {
			if (subOrderList.get(0).getOrderId().equals(storeOrder.getOrderId())) {
				storeOrder.setEnableAddDishes(false);
			} else {
				storeOrder.setEnableAddDishes(true);
			}
		} else {
			storeOrder.setEnableAddDishes(false);
		}
		
		// 更新订单桌台记录相关的信息
		storeTableRecordHelper.checkPayAfter(storeId, storeTableSetting, storeOrder);
		storeOrder.setParentOrderId(masterStoreOrder.getOrderId());
		storeOrder.setUpdateTime(System.currentTimeMillis());
		storeOrder.setEnableManualCustomerTraffic(true);
		storeOrder.update();
		// 执行取餐
		if (storeOrder.getTakeSerialNumber() == 0) { // takeSerialNumber>0认为是已取餐，跳过取餐环节
			StoreOrderTakeCodeParam storeOrderTakeCodeParam = new StoreOrderTakeCodeParam();
			storeOrderTakeCodeParam.setMerchantId(merchantId);
			storeOrderTakeCodeParam.setStoreId(storeId);
			storeOrderTakeCodeParam.setOrderId(storeOrder.getOrderId());
			storeOrderTakeCodeParam.setClientType(ClientTypeEnum.CASHIER.getValue());
			storeOrderTakeCodeParam.setTakeMode(StoreOrderTakeModeEnum.findByValue(storeOrder.getTakeMode()));
//			storeOrder = storeOrderService.takeCodeStoreOrder(storeOrderTakeCodeParam);
			StoreOrder takeCodeStoreOrder = storeOrderService.takeCodeStoreOrder(storeOrderTakeCodeParam);
			storeTableRecord.setStoreMealTakeups(takeCodeStoreOrder.getStoreMealTakeups());
			if (takeCodeStoreOrder != null) {
				storeOrder.setTakeSerialNumber(takeCodeStoreOrder.getTakeSerialNumber());
			}
		}

		// 更新桌台记录信息
		this.refreshTableRecord(merchantId, storeId, storeTableRecord);
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		storeTableRecord.setMasterStoreOrder(masterStoreOrder);
		return storeTableRecord;
	}

	private void _setSendType(StoreOrder storeOrder,StoreTableRecord storeTableRecord,int storeSendType){
		//快取直接将子订单和桌台记录设置为“即起”
		if(storeOrder.getTakeMode() == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()){
			storeTableRecord.setSendType(StoreSendTypeEnum.TAKE_ORDER.getValue());
			storeOrder.setSendType(StoreSendTypeEnum.TAKE_ORDER.getValue());
			return;
		}
		if(storeSendType == StoreSendTypeEnum.TAKE_ORDER.getValue() || storeSendType == 0){
			storeTableRecord.setSendType(StoreSendTypeEnum.TAKE_ORDER.getValue());
			storeOrder.setSendType(StoreSendTypeEnum.TAKE_ORDER.getValue());
		}else{
			//对前端的起菜状态参数做校验
			if(storeTableRecord.getSendType() == StoreSendTypeEnum.WAIT.getValue() || storeTableRecord.getSendType() == StoreSendTypeEnum.URGENT.getValue()){
				//桌台记录如果之前就是加急或者叫起的状态，则对前端传的起菜状态忽略
			}else{
				storeTableRecord.setSendType(storeSendType);
			}
		}
		//如果之前桌台为叫起或者加急状态，则该桌台接下来的子订单都只能为叫起或者加急状态
		if(storeTableRecord.getSendType() == StoreSendTypeEnum.WAIT.getValue()){
			storeOrder.setSendType(StoreSendTypeEnum.WAIT.getValue());
		}
		if(storeTableRecord.getSendType() == StoreSendTypeEnum.URGENT.getValue()){
			storeOrder.setSendType(StoreSendTypeEnum.URGENT.getValue());
		}

		//记录订单操作日志
		StoreOrderOptlog storeOrderOptlog = new StoreOrderOptlog();
		storeOrderOptlog.setOrderId(storeOrder.getOrderId());
		storeOrderOptlog.setMerchantId(storeOrder.getMerchantId());
		storeOrderOptlog.setStoreId(storeOrder.getStoreId());
		storeOrderOptlog.setStaffId(storeOrder.getStaffId());
		storeOrderOptlog.setClientType(storeOrder.getClientType());
		storeOrderOptlog.setOptType(StoreOrderOptlogTypeEnum.CHANGE_SEND_TYPE.getValue());
		storeOrderOptlog.setCreateTime(System.currentTimeMillis());
		if(storeTableRecord.getSendType() == StoreSendTypeEnum.TAKE_ORDER.getValue()){
			storeOrderOptlog.setRemark("set sendType to TAKE_ORDER[3]");
		}
		if(storeTableRecord.getSendType() == StoreSendTypeEnum.URGENT.getValue()){
			storeOrderOptlog.setRemark("set sendType to URGENT[1]");
		}
		if(storeTableRecord.getSendType() == StoreSendTypeEnum.WAIT.getValue()){
			storeOrderOptlog.setRemark("set sendType to WAIT[4]");
		}
		storeOrderOptlogDAO.create(storeOrderOptlog);
	}

	public long caculateChargeItemAvePrice (int merchantId, long storeId, long tableRecordId, long chargeItemId, boolean packed) throws T5weiException {
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, false);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		// 校验桌台记录状态
		int tableRecordStatus = storeTableRecord.getTableRecordStatus();
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable() || storeTableRecord.isSettling() || storeTableRecord.isSettleFail()) {
			// 退菜只能发生在结账和清台前
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_REFUND_ITEM.getValue(), "storeTableRecord["+tableRecordId+"] tableStatus is "+TableRecordStatusEnum.findByValue(tableRecordStatus)+" can not refund chargeItem");
		}
		// 没有主订单的桌台记录不允许退菜
		if (StringUtils.isNullOrEmpty(storeTableRecord.getOrderId())) {
			throw new T5weiException(StoreTableErrorCodeEnum.NO_PLACE_ORDER_CAN_NOT_REFUND_ITEM.getValue(), "tableRecord["+tableRecordId+"] masterOrderId is null or empty, can not refund charge item");
		}
		// 查询桌台记录关联的子订单
		List <StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(), false);
		if (subStoreOrderList.isEmpty()) {
			// 未下单的桌台不允许退菜
			throw new T5weiException(StoreTableErrorCodeEnum.NO_PLACE_ORDER_CAN_NOT_REFUND_ITEM.getValue(), "storeTableRecord["+tableRecordId+"] does not exist subOrders, can not refund chargeItem");
		}
		List<String> orderIds = new ArrayList<String>();
		for (StoreOrder storeOrder : subStoreOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}
		List<StoreOrderItem> storeOrderItems = storeOrderItemDAO.getStoreOrderItemByOrderIdAndChargeItemId(merchantId, storeId, chargeItemId, orderIds, true);
		if (storeOrderItems == null || storeOrderItems.isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.REQUEST_REFUND_NUM_OVER_LIMIT.getValue(), "storeTableRecord["+tableRecordId+"] does not exist chargeItemId["+chargeItemId+"]");
		}
		long totalPrice = 0L;
		double totalAmount = 0D;
		long packagePrice = 0L;
		double packedAmount = 0D;
		
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			long _totalPrice = BigDecimal.valueOf(storeOrderItem.getAmount()).multiply(BigDecimal.valueOf(storeOrderItem.getPrice())).longValue()-storeOrderItem.getChargeItemDerate();
			double _totalAmount = storeOrderItem.getAmount();
			long _packagePrice = BigDecimal.valueOf(storeOrderItem.getPackagePrice()).multiply(BigDecimal.valueOf(storeOrderItem.getPackedAmount())).longValue();
			double _packedAmount = storeOrderItem.getPackedAmount();
			totalPrice += _totalPrice;
			totalAmount += _totalAmount;
			packagePrice += _packagePrice;
			packedAmount += _packedAmount;
		}
		
		long aveUnPackedPrice = 0L;
		if (totalAmount > 0) {
			aveUnPackedPrice = BigDecimal.valueOf(totalPrice).divide(BigDecimal.valueOf(totalAmount), 0, BigDecimal.ROUND_HALF_UP).longValue();
		}
		long avePackedPrice = 0L;
		if (packedAmount > 0) {
			avePackedPrice = BigDecimal.valueOf(packagePrice).divide(BigDecimal.valueOf(packedAmount), 0, BigDecimal.ROUND_HALF_UP).longValue();
		}
		long avePrice = aveUnPackedPrice;
		if (packed) {
			avePrice += avePackedPrice;
		} 
		return avePrice;
	}
	
	/**
	 * 退菜
	 * @param refundTableItemParam 退款请求参数
	 * @return StoreTableRecord 桌台记录
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public RefundChargeItemResult refundChargeItem(RefundTableItemParam refundTableItemParam) throws TException {
		int merchantId = refundTableItemParam.getMerchantId(); // 商编
		long storeId = refundTableItemParam.getStoreId(); // 店铺id
		long tableRecordId = refundTableItemParam.getTableRecordId(); // 桌台记录id
		long staffId = refundTableItemParam.getStaffId(); // 员工id
		ClientTypeEnum clientType = ClientTypeEnum.CASHIER; // 终端类型
		List<RefundOrderItemParam> refundOrderItems = refundTableItemParam.getRefundOrderItems(); // 退菜列表
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		// 校验桌台记录状态
		int tableRecordStatus = storeTableRecord.getTableRecordStatus();
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable() || storeTableRecord.isSettling() || storeTableRecord.isSettleFail()) {
			// 退菜只能发生在结账和清台前
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_REFUND_ITEM.getValue(), "storeTableRecord["+tableRecordId+"] tableStatus is "+TableRecordStatusEnum.findByValue(tableRecordStatus)+" can not refund chargeItem");
		}
		// 没有主订单的桌台记录不允许退菜
		if (StringUtils.isNullOrEmpty(storeTableRecord.getOrderId())) {
			throw new T5weiException(StoreTableErrorCodeEnum.NO_PLACE_ORDER_CAN_NOT_REFUND_ITEM.getValue(), "tableRecord["+tableRecordId+"] masterOrderId is null or empty, can not refund charge item");
		}
		// 查询桌台记录关联的子订单
		List <StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(), false);
		if (subStoreOrderList.isEmpty()) {
			// 未下单的桌台不允许退菜
			throw new T5weiException(StoreTableErrorCodeEnum.NO_PLACE_ORDER_CAN_NOT_REFUND_ITEM.getValue(), "storeTableRecord["+tableRecordId+"] does not exist subOrders, can not refund chargeItem");
		}
		List<String> orderIds = new ArrayList<String>();
		for (StoreOrder storeOrder : subStoreOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}
		long totalRequestRefundAmount = 0L; // 请求退菜金额合计
		for (RefundOrderItemParam refundOrderItem : refundOrderItems) {
			totalRequestRefundAmount += refundOrderItem.getRefundAmount();
		}
		TableRecordAmountsResult tableRecordAmounts = this.calculateTableRecordAmounts(storeTableRecord, totalRequestRefundAmount);
		// 等待结账的金额（waitSettleAmount>0表示用户还需要支付；waitSettleAmount=0表示已经付清；waitSettleAmount<0表示应该给用户退款）
		long waitSettleAmount = tableRecordAmounts.getWaitSettleAmount();
		// 已付金额
		long paidAmount = tableRecordAmounts.getPaidAmount();
		// 需要退给用户的钱不允许超过用户已经支付的钱，否则结账时不够退
		if (waitSettleAmount + paidAmount < 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.REFUND_AMOUNT_TOO_MORE.getValue(), "tableRecord["+tableRecordId+"] request refund amount["+totalRequestRefundAmount+"] too more");
		}
		// 退菜列表
		List<StoreOrderRefundItem> storeOrderRefundItems = new ArrayList<StoreOrderRefundItem>();
		// 计算收费项目单价
		List<Long> chargeItemIds = new ArrayList<Long>();
		for (RefundOrderItemParam refundOrderItemParam : refundOrderItems) {
			chargeItemIds.add(refundOrderItemParam.getChargeItemId());
		}
		// 已退数量
		List<StoreTableRecordRefund> storeTableRecordRefunds = storeTableRecordRefundDAO.getStoreTableRecordRefundsByTableRecordId(merchantId, storeId, tableRecordId);
		Map<String, Double> storeTableRecordRefundMap = new HashMap<String, Double>();
		for (StoreTableRecordRefund storeTableRecordRefund : storeTableRecordRefunds) {
			long chargeItemId = storeTableRecordRefund.getChargeItemId();
			boolean isPacked = storeTableRecordRefund.isPacked();
			String isPackedStr = String.valueOf("-"+isPacked);
			if (storeTableRecordRefundMap.containsKey(chargeItemId+isPackedStr)) {
				double amount = storeTableRecordRefundMap.get(chargeItemId+isPackedStr);
				storeTableRecordRefundMap.put(chargeItemId+isPackedStr, amount + storeTableRecordRefund.getAmount());
			} else {
				storeTableRecordRefundMap.put(chargeItemId+isPackedStr, storeTableRecordRefund.getAmount());
			}
		}
		// 查询桌台各个chargeItem所点数量
		List<StoreOrderItem> storeOrderItems = storeOrderItemDAO.getStoreOrderItemById(merchantId, storeId, orderIds, true);
		Map<String, Double> storeOrderItemMap = new HashMap<String, Double>();
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			long chargeItemId = storeOrderItem.getChargeItemId();
			double totalAmount = storeOrderItem.getAmount();
			double packedAmount = storeOrderItem.getPackedAmount();
			double unPackedAmount = totalAmount - packedAmount;
			if (packedAmount > 0) {
				if (storeOrderItemMap.containsKey(chargeItemId+String.valueOf("-"+true))) {
					storeOrderItemMap.put(chargeItemId+String.valueOf("-"+true), packedAmount + storeOrderItemMap.get(chargeItemId+String.valueOf("-"+true)));
				} else {
					storeOrderItemMap.put(chargeItemId+String.valueOf("-"+true), packedAmount);
				}
			}
			if (unPackedAmount > 0) {
				if (storeOrderItemMap.containsKey(chargeItemId+String.valueOf("-"+false))) {
					storeOrderItemMap.put(chargeItemId+String.valueOf("-"+false), unPackedAmount + storeOrderItemMap.get(chargeItemId+String.valueOf("-"+false)));
				} else {
					storeOrderItemMap.put(chargeItemId+String.valueOf("-"+false), unPackedAmount);
				}
			}
		}
		for (RefundOrderItemParam refundOrderItemParam : refundOrderItems) {
			// 请求退菜数量不能为0
			long chargeItemId = refundOrderItemParam.getChargeItemId();
			boolean isPacked = refundOrderItemParam.isPacked();
			String isPackedStr = String.valueOf("-"+isPacked);
			double requestRefundNum = refundOrderItemParam.getRefundNum();
			if (requestRefundNum == 0) {
				throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecord["+tableRecordId+"] refundNum[chargeItemId="+refundOrderItemParam.getChargeItemId()+"] can not be 0");
			}
			if (storeTableRecordRefundMap.containsKey(chargeItemId+isPackedStr)) {
				if (!storeOrderItemMap.containsKey(chargeItemId+isPackedStr)) {
					throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "can not find key["+chargeItemId+isPackedStr+"] from storeOrderItemMap");
				}
				double canRefundNum = MoneyUtil.sub(storeOrderItemMap.get(chargeItemId+isPackedStr), storeTableRecordRefundMap.get(chargeItemId+isPackedStr));
				//double canRefundNum = storeOrderItemMap.get(chargeItemId+isPackedStr) - storeTableRecordRefundMap.get(chargeItemId+isPackedStr);
				if (requestRefundNum > canRefundNum) {
					log.warn("tableRecord["+tableRecordId+"] refundNum[chargeItemId="+refundOrderItemParam.getChargeItemId()+"] over limit");
					throw new T5weiException(StoreTableErrorCodeEnum.REQUEST_REFUND_NUM_OVER_LIMIT.getValue(), "tableRecord["+tableRecordId+"] refundNum[chargeItemId="+refundOrderItemParam.getChargeItemId()+"] over limit");
				}
			}
			//storeTableRecordRefundDAO.getStoreTableRecordRefundsByTableRecordId(merchantId, storeId, tableRecordId);
		}
		Map<Long, StoreOrderItem> refundStoreOrderItemMap = storeOrderItemDAO.getStoreOrderItemMapByIds(merchantId, storeId, orderIds, chargeItemIds);
		for (RefundOrderItemParam refundOrderItemParam : refundOrderItems) {
			// 请求退菜数量不能为0
			if (refundOrderItemParam.getRefundNum() == 0) {
				throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecord["+tableRecordId+"] refundNum[chargeItemId="+refundOrderItemParam.getChargeItemId()+"] can not be 0");
			}
			if (refundStoreOrderItemMap == null || !refundStoreOrderItemMap.containsKey(refundOrderItemParam.getChargeItemId())) {
				throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "chargeItemId["+refundOrderItemParam.getChargeItemId()+"] invalid");
			}
			StoreOrderItem storeOrderItem = refundStoreOrderItemMap.get(refundOrderItemParam.getChargeItemId());
			long refundItemPrice = storeOrderItem.getPrice();
			long packagePrice = storeOrderItem.getPackagePrice();
			if (refundOrderItemParam.isPacked()) {
				refundItemPrice = refundItemPrice + packagePrice;
			}
			// 请求退款金额不能大于菜的价格
			long chargeItemprice = MoneyUtil.mul(refundItemPrice, refundOrderItemParam.getRefundNum());
			if (refundOrderItemParam.getRefundAmount() > chargeItemprice) {
				throw new T5weiException(StoreTableErrorCodeEnum.REFUND_AMOUNT_CAN_NOT_MORE_TAHN_ITEM_PRICE.getValue(), "tableRecord["+tableRecordId+"] request refund chargeItemId["+refundOrderItemParam.getChargeItemId()+"], amount["+refundOrderItemParam.getRefundAmount()+"] can not be more than chargeItem price["+chargeItemprice+"]");
			}
			// 创建退菜记录
			StoreTableRecordRefund storeTableRecordRefund = tableRecordRefundHelper.createStoreTableRecordRefund(
					merchantId, storeId, tableRecordId, staffId, clientType.getValue(), refundOrderItemParam, storeOrderItem.getChargeItemName(), storeOrderItem.getUnit());
			double refundedItemNum = 0; // 记录已退数量
			// 先从未支付订单中退菜
			for (StoreOrder subStoreOrder : subStoreOrderList) {
				if (!subStoreOrder.isPayFinish()) {
					if (refundedItemNum == refundOrderItemParam.getRefundNum()) {
						break;
					} else if (refundedItemNum > refundOrderItemParam.getRefundNum()) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"] refund item error, refundItemNum["+refundedItemNum+"] can not more than request num["+refundOrderItemParam.getRefundNum()+"]");
					} else {
						if (subStoreOrder.getPayStatus() == StoreOrderPayStatusEnum.NOT.getValue()) {
							refundedItemNum += this.createRefundItemRecord(storeTableRecord, subStoreOrder, refundOrderItemParam, staffId, storeOrderItem, storeTableRecordRefund, refundItemPrice, refundedItemNum, storeOrderRefundItems);
						} 
					}
				}
			}
			// 再从已支付订单中退菜
			for (StoreOrder subStoreOrder : subStoreOrderList) {
				if (subStoreOrder.isPayFinish()) {
					if (refundedItemNum == refundOrderItemParam.getRefundNum()) {
						break;
					} else if (refundedItemNum > refundOrderItemParam.getRefundNum()) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"] refund item error, refundItemNum["+refundedItemNum+"] can not more than request num["+refundOrderItemParam.getRefundNum()+"]");
					} else {
						if (subStoreOrder.getPayStatus() != StoreOrderPayStatusEnum.NOT.getValue()) {
							refundedItemNum += this.createRefundItemRecord(storeTableRecord, subStoreOrder, refundOrderItemParam, staffId, storeOrderItem, storeTableRecordRefund, refundItemPrice, refundedItemNum, storeOrderRefundItems);
						}
					}
				}
			}
		}
		// 出餐口退菜 
		storeMealService.refundStoreMeal(storeOrderRefundItems);
		// 更新桌台记录
		this.refreshTableRecord(merchantId, storeId, storeTableRecord);
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		//记录桌台记录退菜日志 TODO 缺少clientType
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(),
				staffId, 0, ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_REFUND_ITEM.getValue(),
				"桌台记录退菜操作", storeTableRecord.getUpdateTime());
		RefundChargeItemResult refundChargeItemResult = new RefundChargeItemResult();
		refundChargeItemResult.setStoreTableRecord(storeTableRecord);
		refundChargeItemResult.setStoreOrderRefundItems(storeOrderRefundItems);
		return refundChargeItemResult;
	}
	
	
	/**
	 * 获取结账记录
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param storeTableRecord 桌台记录
	 * @return TableRecordPayStatusResult
	 * @throws TException 
	 */
	//@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public TableRecordPayStatusResult getSettleRecord (int merchantId, long storeId, StoreTableRecord storeTableRecord) throws TException {
		// 获取桌台记录支付详情&各项金额详情
		TableRecordPayStatusResult tableRecordPayStatusInfo = this.getTableRecordPayStatusInfo(merchantId, storeId, storeTableRecord, true);
		return tableRecordPayStatusInfo;
	}
	
	/**
	 * 更新桌台记录出餐信息(在待出餐、已出餐发生变化时调用此方法)
	 * @param storeOrder
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void updateTableRecordMealInfo (StoreOrder storeOrder) throws T5weiException {
		if (storeOrder == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeOrder invalid");
		}
		int merchantId = storeOrder.getMerchantId(); 
		long storeId = storeOrder.getStoreId();
		long tableRecordId = storeOrder.getTableRecordId();
		if (tableRecordId > 0) {
			StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
			if (storeTableRecord == null) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecordId["+tableRecordId+"] invalid");
			}
			// 重新计算桌台记录的出餐信息
			TableStatusResult tableStatusResult = this.getTableRecordStatus(merchantId, storeId, storeTableRecord);
			TableRecordStatusEnum TableRecordStatusEnum = tableStatusResult.getTableRecordStatusEnum();
			storeTableRecord.snapshot();
			storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.getValue());
			this.isAllTakeOut(tableStatusResult, storeTableRecord);
			long currentTime = System.currentTimeMillis();
			long firstUpTime = storeTableRecord.getFirstUpTime();
			if (firstUpTime == 0) {
				storeTableRecord.setFirstUpTime(currentTime);
			}
			storeTableRecord.setLastUpTime(currentTime);
			storeTableRecord.setUpdateTime(currentTime);
			storeTableRecord.update();
		}
	}
	
	/**
	 * 获取桌台记录详情信息（不包含主订单，子订单列表（包含定价和子项目），退菜记录列表，桌台信息，区域信息）
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @return StoreTableRecord 桌台记录
	 * @throws T5weiException 
	 * @throws TException 
	 */
	public StoreTableRecord getTableRecordDetail (int merchantId, long storeId, long tableRecordId) throws T5weiException {
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, false);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		storeTableRecord = this.getMergeTableRecord(storeTableRecord);
		int mealTakeupNum = storeTableRecord.getMealTakeupNum();
		int mealCheckoutNum = storeTableRecord.getMealCheckoutNum();
		this.setAllTakeOut(storeTableRecord, mealTakeupNum, mealCheckoutNum);
		return storeTableRecord;
	}
	
	/**
	 * 结账-付款
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @param staffId 结账服务员id
	 * @param userId 自助结账用户id
	 * @param clientType 终端类型
	 * @return StoreOrder 用于结账使用的主订单
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreOrder settleTableRecordPay (int merchantId, long storeId, long tableRecordId, long staffId, long userId, int clientType, long staffDerate) 
			throws TException {
		// 查询出桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		storeTableRecord = this.getMergeTableRecord(storeTableRecord);
		tableRecordId = storeTableRecord.getTableRecordId();
		// 如果桌台记录已经为结账或清台或结账中状态 不允许结账
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_SETTLE.getValue(), "tableRecord["+tableRecordId+"] status ["+TableRecordStatusEnum.findByValue(storeTableRecord.getTableRecordStatus())+"] can not settle");
		}
		// 查询出主订单
		StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, false);
		if (masterStoreOrder == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"] masterStoreOrder is null, can not settle");
		}
		// 如果主订单支付状态不等于未支付，不允许结账
		if (masterStoreOrder.getPayStatus() != StoreOrderPayStatusEnum.NOT.getValue()) {
			try {
				boolean cancelResult = this.tableRecordSettleCancel(storeTableRecord);
				if (cancelResult) {
					masterStoreOrder.setPayStatus(StoreOrderPayStatusEnum.NOT.getValue());
					masterStoreOrder.setUpdateTime(System.currentTimeMillis());
					masterStoreOrder.update();
				} else {
					throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"] masterStoreOrder payStatus is not NOT, can not settle");
				}
			} catch (Exception e) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"] masterStoreOrder payStatus is not NOT, can not settle");
			}
		}
		// 每次请求结账都将历史的抹零清零
		long _staffDerate = storeTableRecord.getStaffDerate();
		storeTableRecord.setStaffDerate(0L);
		storeTableRecord.setPayAbleAmount(storeTableRecord.getPayAbleAmount() + _staffDerate);
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		// 查询各项付款金额
		long waitSettleAmount = storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount());
		if (waitSettleAmount <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.CAN_NOT_SETTLE_PAY.getValue(), "tableRecord["+tableRecordId+"] waitSettleAmount["+waitSettleAmount+"], can not settle-pay");
		}
		if (waitSettleAmount - staffDerate <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.WAIT_SETTLE_AMOUNT_CHANGE.getValue(), "tableRecord["+tableRecordId+"] waitSettleAmount - staffDerate["+(waitSettleAmount - staffDerate)+"], can not settle-pay");
		}
		if (staffDerate > 0) {
			if (!this.checkStaffDerate(waitSettleAmount - staffDerate)) {
				throw new T5weiException(StoreTableErrorCodeEnum.WAIT_SETTLE_AMOUNT_CHANGE.getValue(), "tableRecord["+tableRecordId+"] waitSettleAmount - staffDerate["+(waitSettleAmount - staffDerate)+"], can not settle-pay");
			}
		}
		storeTableRecord.update();
		// 更新主订单
		masterStoreOrder.snapshot();
		masterStoreOrder.setOrderPrice(waitSettleAmount - staffDerate);
		masterStoreOrder.setTotalRebate(100D);
		masterStoreOrder.setTotalDerate(0L);
		masterStoreOrder.setStaffId(staffId);
		masterStoreOrder.setUserId(userId);
		masterStoreOrder.setStaffDerate(staffDerate);
		storeOrderService.settleMasterStoreOrder(masterStoreOrder);
		// 调整支付总订单的金额
        try {
            // 如果已生成了对应的支付总订单，则调整支付总订单的金额
            orderFacade.updatePayOrderAmount(masterStoreOrder.getOrderId(), PaySrcEnum.M_5WEI, masterStoreOrder.getPayablePrice());
        } catch (TPayException e) {
            if (e.getErrorCode() == PayErrorCode.PAY_ORDER_INVALID.getValue()) {
                // 如果此时未生成对应的支付总订单则不必修改
            } else {
                log.error("amount of pay order update fail {orderId:" + masterStoreOrder.getOrderId() + ", payOrderId:" + masterStoreOrder.getPayOrderId() + ", amount:" + masterStoreOrder.getPayablePrice() + "}", e);
                throw e;
            }
        }
		masterStoreOrder.setTotalPrice(waitSettleAmount);
		//记录桌台操作日志
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, tableRecordId, staffId, userId,
				clientType, TableRecordOptlogTypeEnum.TABLE_START_SETTLE.getValue(),
				"桌台记录结账支付开始,使用主订单金额进行支付", storeTableRecord.getUpdateTime());
		return masterStoreOrder;
	}
	
	/**
	 * 获取结账退款详情
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @return
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public List<StoreOrder> getRefundDetail (int merchantId, long storeId, long tableRecordId) throws TException {
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		storeTableRecord = this.getMergeTableRecord(storeTableRecord);
		tableRecordId = storeTableRecord.getTableRecordId();
		// 获取主订单
		StoreOrder masterStoreOrder = null;
		if (!StringUtils.isNullOrEmpty(storeTableRecord.getOrderId())) {
			masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, false);
		}
		if (masterStoreOrder == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord["+tableRecordId+"] masterOrder is null");
		}
		// 获取子订单
		List<StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrder.getOrderId(), false);
		List<StoreOrder> paidStoreOrderList = new ArrayList<StoreOrder>();
		if (masterStoreOrder.isPayFinish()) {
			if (masterStoreOrder.getRefundStatus() != StoreOrderRefundStatusEnum.USER_ALL.getValue() && masterStoreOrder.getRefundStatus() != StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue()) {
				paidStoreOrderList.add(masterStoreOrder);
			}
		}
		// 获取所有已经支付的子订单
		for (StoreOrder storeOrder : subStoreOrderList) {
			if (storeOrder.isPayFinish()) {
				if (storeOrder.getRefundStatus() != StoreOrderRefundStatusEnum.USER_ALL.getValue() && storeOrder.getRefundStatus() != StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue()) {
					paidStoreOrderList.add(storeOrder);
				}
			}
		}
		return paidStoreOrderList;
	}
	
	/**
	 * 设置里开启\关闭台位费时调用此方法,用于修改桌台记录台位费
	 * @param merchantId
	 * @param storeId
	 * @throws TException
	 */
	public void changeTableFee (int merchantId, long storeId) throws TException {
		boolean enableSlave = false;
		StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, enableSlave);
		if (!storeTableSetting.isEnableTableMode()) {
			return;
		}
		// 获取店铺下所有未清台的桌台记录
		List<StoreTableRecord> storeTableRecordList = storeTableRecordDAO.getStoreTableRecordByStoreId(merchantId, storeId, enableSlave);
		for (StoreTableRecord storeTableRecord : storeTableRecordList) {
			try {
				StoreOrderPlaceParam storeOrderPlaceParam = new StoreOrderPlaceParam();
				storeOrderPlaceParam.setMerchantId(merchantId);
				storeOrderPlaceParam.setStoreId(storeId);
				storeOrderPlaceParam.setTimeBucketId(storeTableRecord.getTimeBucketId());
				storeOrderPlaceParam.setTakeMode(StoreOrderTakeModeEnum.DINE_IN.getValue());
				storeOrderPlaceParam.setEnableAddDishes(false);
                storeTableRecord.setReductionTableFee(0);
				long payAbleTableFee = this.getTableFee(storeOrderPlaceParam, storeTableRecord.getCustomerTraffic());
				storeTableRecord.setPayAbleTableFee(payAbleTableFee);
				storeTableRecord.setUpdateTime(System.currentTimeMillis());
				this.refreshTableRecord(merchantId, storeId, storeTableRecord);
				storeTableRecord.update();
			} catch (Exception e) {
				log.error("changeTableFee error, merchantId["+merchantId+"] storeId["+storeId+"]", e);
			}
		}
	}
	
	/**
	 * 设置桌台记录为结账失败状态
	 * @param merchantId
	 * @param storeId
	 * @param orderId
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void orderPayFail (int merchantId, long storeId, String orderId) throws T5weiException {
		StoreOrder payStoreOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
		if (payStoreOrder.isTableRecordMasterOrder()) { // 主订单
			// 获取桌台记录
			StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(payStoreOrder.getMerchantId(), payStoreOrder.getStoreId(), payStoreOrder.getTableRecordId(), true);
			if (storeTableRecord == null) {
				log.error("storeOrder["+payStoreOrder.getOrderId()+"] tableRecordId=["+payStoreOrder.getTableRecordId()+"], can not find tableRecord");
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeOrder["+payStoreOrder.getOrderId()+"] tableRecordId=["+payStoreOrder.getTableRecordId()+"], can not find tableRecord");
			}
			log.warn("storeTableRecord["+storeTableRecord.getTableRecordId()+"] master order pay fail");
			// 更新桌台记录状态
			TableStatusResult tableStatusResult = this.getTableRecordStatus(merchantId, storeId, storeTableRecord);
			TableRecordStatusEnum TableRecordStatusEnum = tableStatusResult.getTableRecordStatusEnum();
			storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.getValue());
			storeTableRecord.setUpdateTime(System.currentTimeMillis());
			storeTableRecord.update();
			//记录桌台记录的操作日志
			storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), 0, 0,
					0, TableRecordOptlogTypeEnum.TABLE_SETTLE_FAIL.getValue(),
					"桌台记录支付结账失败,当前桌台的状态["+storeTableRecord.getTableRecordStatus()+"]", storeTableRecord.getUpdateTime());
			log.error("storeTableRecord["+storeTableRecord.getTableRecordId()+"] settle fail");
		}
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public OrderPayFinishResult creditSettle (StoreOrder storeOrder) throws TException {
		OrderPayFinishResult orderPayFinishResult = new OrderPayFinishResult();
		if (storeOrder.getTableRecordId() > 0) {
			StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getTableRecordId(), true);
			if (storeTableRecord == null) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeOrder["+storeOrder.getOrderId()+"] tableRecordId=["+storeOrder.getTableRecordId()+"], can not find tableRecord");
			}
			orderPayFinishResult.setStoreTableRecord(storeTableRecord);
			// 已经清台或结账或结账中的桌台记录不处理订单 
			if (storeTableRecord.isClearTable() || storeTableRecord.isSettleMent()) {
				log.error("storeTableRecord["+storeTableRecord.getTableRecordId()+"] tableRecordStatus["+storeTableRecord.getTableRecordStatus()+"] can not relate subOrder["+storeOrder.getOrderId()+"]");
				return orderPayFinishResult;
			}
			if (storeOrder.isTableRecordMasterOrder()) { // 主订单
				if (storeOrder.getCreditStatus() > 0) {
					if (storeOrder.getStaffDerate() > 0) {
						storeTableRecord.setStaffDerate(storeOrder.getStaffDerate());
						storeTableRecord.setDiscountAmount(storeTableRecord.getDiscountAmount() + storeOrder.getStaffDerate());
						storeTableRecord.setPayAbleAmount(storeTableRecord.getPayAbleAmount() - storeOrder.getStaffDerate());
					}
					List<StoreOrder> toTradeFinishOrders = new ArrayList<StoreOrder>();
					// 将主订单、子订单状态设置为完成
					toTradeFinishOrders.add(storeOrder);
					List<StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getTableRecordId(), storeOrder.getOrderId(), true);
					for (StoreOrder subStoreOrder : subStoreOrderList) {
						toTradeFinishOrders.add(subStoreOrder);
					}
					storeOrderService.updateOrderTradeFinish(storeTableRecord.getTableRecordId(), toTradeFinishOrders);
					// 更新桌台记录状态
					storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.SETTLEMENT.getValue());
					/*// 获取店铺桌台设置
					StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeOrder.getStoreId(), storeOrder.getMerchantId(), false);
					// 如果开启了结账后自动清台，则设置桌台记录为清台状态
					if (storeTableSetting.isEnableTableMode() && storeTableSetting.isEnableTableAutoClear()) {
						storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.CLEAR_TABLE.getValue());
						storeTableRecord.setClearTableTime(System.currentTimeMillis());
					}*/ 
					this.autoClearTable(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeTableRecord, null);
					StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeOrder.getStoreId(), storeOrder.getMerchantId(), false);
					if (storeTableSetting.isEnableTableMode() && storeTableSetting.isEnableTableAutoClear()) {
						this.clearStoreMeal(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(), subStoreOrderList, null);
					}
					this.refreshTableRecord(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeTableRecord);
					storeTableRecord.setUpdateTime(System.currentTimeMillis());
					storeTableRecord.setSettleStaffId(storeOrder.getStaffId());
					storeTableRecord.setSettleTime(System.currentTimeMillis());
					storeTableRecord.update();
					//记录桌台记录的操作日志
					storeTableRecordOptlogService.createTableRecordOptlog(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeTableRecord.getTableRecordId(), 0, 0,
							0, TableRecordOptlogTypeEnum.TABLE_SETTLEMENT.getValue(),
							"桌台记录主订单赊账成功", storeTableRecord.getSettleTime());
					orderPayFinishResult.setSettleMent(true);
					// 将桌台记录下所有订单发消息给统计
					orderPayFinishResult.setStoreOrders(toTradeFinishOrders);
				}
			}
		}
		return orderPayFinishResult;
	}
	
	/**
	 * 设订单支付完成后续处理
	 * @param merchantId 商户ID
	 * @param storeId 店铺ID
	 * @param orderId 支付成功交易订单
	 * @throws TException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public OrderPayFinishResult orderPayFinish (int merchantId, long storeId, String orderId,long settleUserId) throws TException {
		OrderPayFinishResult orderPayFinishResult = new OrderPayFinishResult();
		StoreOrder payStoreOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
		if (payStoreOrder.getTableRecordId() > 0) {
			// 获取桌台记录
			StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(payStoreOrder.getMerchantId(), payStoreOrder.getStoreId(), payStoreOrder.getTableRecordId(), true);
			if (storeTableRecord == null) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeOrder["+payStoreOrder.getOrderId()+"] tableRecordId=["+payStoreOrder.getTableRecordId()+"], can not find tableRecord");
			}
			orderPayFinishResult.setStoreTableRecord(storeTableRecord);
			// 已经清台或结账或结账中的桌台记录不处理订单 
			if (storeTableRecord.isClearTable() || storeTableRecord.isSettleMent()) {
				return orderPayFinishResult;
			}
			if (payStoreOrder.isMasterOrder()) { // 主订单
				if (payStoreOrder.getStaffDerate() > 0) {
					storeTableRecord.setStaffDerate(payStoreOrder.getStaffDerate());
					storeTableRecord.setDiscountAmount(storeTableRecord.getDiscountAmount() + payStoreOrder.getStaffDerate());
					storeTableRecord.setPayAbleAmount(storeTableRecord.getPayAbleAmount() - payStoreOrder.getStaffDerate());
				}
				PayStatusEnum payStatusEnum = this.getPayStatus(payStoreOrder.getMerchantId(), payStoreOrder.getStoreId(), storeTableRecord);
                log.info("===="+payStatusEnum.getValue());
				if (payStatusEnum.getValue() != PayStatusEnum.ALL_PAY.getValue()) {
					throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "masterOrder["+payStoreOrder.getOrderId()+"] pay finish, but tableRecord["+storeTableRecord.getTableRecordId()+"] payStatus is not ALL_PAY");
				}
				List<StoreOrder> toTradeFinishOrders = new ArrayList<StoreOrder>();
				// 将主订单、子订单状态设置为完成
				toTradeFinishOrders.add(payStoreOrder);
				List<StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, payStoreOrder.getTableRecordId(), payStoreOrder.getOrderId(), true);
				for (StoreOrder subStoreOrder : subStoreOrderList) {
					toTradeFinishOrders.add(subStoreOrder);
				}
				storeOrderService.updateOrderTradeFinish(storeTableRecord.getTableRecordId(), toTradeFinishOrders);
				// 更新桌台记录状态
				storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.SETTLEMENT.getValue());
				/*// 获取店铺桌台设置
				StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
				// 如果开启了结账后自动清台，则设置桌台记录为清台状态
				if (storeTableSetting.isEnableTableMode() && storeTableSetting.isEnableTableAutoClear()) {
					storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.CLEAR_TABLE.getValue());
					storeTableRecord.setClearTableTime(System.currentTimeMillis());
				} */
				this.autoClearTable(merchantId, storeId, storeTableRecord, null);
				StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
				if (storeTableSetting.isEnableTableMode() && storeTableSetting.isEnableTableAutoClear()) {
					this.clearStoreMeal(merchantId, storeId, storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(), subStoreOrderList, null);
				}
				this.refreshTableRecord(merchantId, storeId, storeTableRecord);
				if (storeTableRecord.getPayStatus() != PayStatusEnum.ALL_PAY.getValue()) {
					log.error("tableRecord["+storeTableRecord.getTableRecordId()+"], masterOrder["+payStoreOrder.getOrderId()+"] pay finish, but table pay status is noy all_pay");
					throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"], masterOrder["+payStoreOrder.getOrderId()+"] pay finish, but table pay status is noy all_pay");
				}
				storeTableRecord.setUpdateTime(System.currentTimeMillis());
				storeTableRecord.setSettleStaffId(payStoreOrder.getStaffId());
				storeTableRecord.setSettleUserId(settleUserId);
				storeTableRecord.setSettleTime(System.currentTimeMillis());
				storeTableRecord.update();

				//记录桌台记录操作日志
				storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), 0, 0,
						0, TableRecordOptlogTypeEnum.TABLE_SETTLEMENT.getValue(),
						"桌台记录主订单付款结账回调成功", storeTableRecord.getSettleTime());
				
				orderPayFinishResult.setSettleMent(true);
				// 将桌台记录下所有订单发消息给统计
				orderPayFinishResult.setStoreOrders(toTradeFinishOrders);
			} else { // 子订单
				if (storeTableRecord.isSettling()) {
					// 结账中状态的桌台记录不允许关联订单
					throw new T5weiException(StoreTableErrorCodeEnum.SETTLING_OR_FAIL_CAN_NOT_RELATE_SUB_ORDER.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"] status is SETTLING, can not add subOrder");
				}
				// 获取店铺桌台设置
				StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
				// 非桌台模式不允许加菜
				if (!storeTableSetting.isEnableTableMode()) {
					return orderPayFinishResult;
				}
				StoreOrder masterStoreOrder = null;
				// 查询子订单
				StoreOrder subStoreOrder = payStoreOrder;
				// 判断当前订单是否已经为桌台记录子订单，如果是，说明该订单已经通过relate接口关联桌台记录
				if (!subStoreOrder.isTableRecordSubOrder()) {
					orderPayFinishResult.setSendTableRecordAddDishMsg(true);
				}
				// 将支付完成的订单与桌台记录关联时发现桌台记录已经结账或清台，重新开台处理
				if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
					CreateTableRecordWithOrderIdsParam createTableRecordWithOrderIdsParam = new CreateTableRecordWithOrderIdsParam();
					createTableRecordWithOrderIdsParam.setTableId(storeTableRecord.getTableId());
					createTableRecordWithOrderIdsParam.setStaffId(storeTableRecord.getStaffId());
					createTableRecordWithOrderIdsParam.setCustomerTraffic(storeTableRecord.getCustomerTraffic());
					createTableRecordWithOrderIdsParam.setMerchantId(storeTableRecord.getMerchantId());
					createTableRecordWithOrderIdsParam.setStoreId(storeTableRecord.getStoreId());
					createTableRecordWithOrderIdsParam.setRepastDate(storeTableRecord.getRepastDate());
					createTableRecordWithOrderIdsParam.setTimeBucketId(storeTableRecord.getTimeBucketId());
					List<String> orderIds = new ArrayList<String>();
					orderIds.add(subStoreOrder.getOrderId());
					createTableRecordWithOrderIdsParam.setOrderIds(orderIds);
					createTableRecordWithOrderIdsParam.setUserId(storeTableRecord.getCreateTableUserId());
					createTableRecordWithOrderIdsParam.setCreateTableRecordStaffId(storeTableRecord.getCreateTableStaffId());
					this.createTableRecordWithOrderIds(createTableRecordWithOrderIdsParam);
					return orderPayFinishResult;
				}
				// 订单状态校验:桌台记录在结账前，子订单不允许赊账和退款
				storeTableRecordFacadeValidate.checkSubOrderStatus(storeTableRecord, subStoreOrder);
				// 查询主订单
				masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), false);
				// 主订单为空时需要创建主订单，并设置订单是否为加菜
				if (masterStoreOrder == null) {
					masterStoreOrder = storeOrderService.createMasterStoreOrder(subStoreOrder, storeTableRecord.getTableRecordId());
					masterStoreOrder.setCustomerTraffic(storeTableRecord.getCustomerTraffic());
					masterStoreOrder.setUpdateTime(System.currentTimeMillis());
					masterStoreOrder.update();
					storeTableRecord.setTakeSerialNumber(masterStoreOrder.getTakeSerialNumber());
					storeTableRecord.setOrderTime(System.currentTimeMillis());
				} else {
					if (masterStoreOrder.getClientType() == ClientTypeEnum.CASHIER.getValue() && subStoreOrder.getClientType() != ClientTypeEnum.CASHIER.getValue()) {
						masterStoreOrder.setClientType(subStoreOrder.getClientType());
						masterStoreOrder.setUpdateTime(System.currentTimeMillis());
						masterStoreOrder.update();
					}
				}
				List<StoreOrder> subOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), masterStoreOrder.getOrderId(), false);
				if (subOrderList != null && !subOrderList.isEmpty()) {
					if (subOrderList.get(0).getOrderId().equals(subStoreOrder.getOrderId())) {
						subStoreOrder.setEnableAddDishes(false);
					} else {
						subStoreOrder.setEnableAddDishes(true);
					}
				} else {
					subStoreOrder.setEnableAddDishes(false);
				}
				// 校验桌台记录主订单id和主订单orderId是否一致
				storeTableRecordFacadeValidate.checkMasterOrderId(storeTableRecord, masterStoreOrder);
				storeTableRecord.setOrderId(masterStoreOrder.getOrderId());
				// 更新子订单状态
				subStoreOrder.setTableRecordId(storeTableRecord.getTableRecordId());
				subStoreOrder.setParentOrderId(masterStoreOrder.getOrderId());
				subStoreOrder.setUpdateTime(System.currentTimeMillis());
				subStoreOrder.update();
				if (subStoreOrder.getTakeSerialNumber() == 0) { // takeSerialNumber>0认为是已取餐，跳过取餐环节
					StoreOrderTakeCodeParam storeOrderTakeCodeParam = new StoreOrderTakeCodeParam();
					storeOrderTakeCodeParam.setMerchantId(merchantId);
					storeOrderTakeCodeParam.setStoreId(storeId);
					storeOrderTakeCodeParam.setOrderId(subStoreOrder.getOrderId());
					storeOrderTakeCodeParam.setClientType(ClientTypeEnum.CASHIER.getValue());
					storeOrderTakeCodeParam.setTakeMode(StoreOrderTakeModeEnum.findByValue(subStoreOrder.getTakeMode()));
					subStoreOrder = storeOrderService.takeCodeStoreOrder(storeOrderTakeCodeParam);

				}
				// 更新桌台记录信息
				this.refreshTableRecord(merchantId, storeId, storeTableRecord);
				//设置桌台记录的起菜状态
				storeTableRecord.setSendType(payStoreOrder.getSendType());
				storeTableRecord.setUpdateTime(System.currentTimeMillis());
				storeTableRecord.update();
				orderPayFinishResult.setUserId(payStoreOrder.getUserId());
				return orderPayFinishResult;
			}
		}
		return orderPayFinishResult;
	}
	
	/**
	 * 支付前先经过后付费判断，如果是后付费主订单结账，需要判断请求结账金额与桌台记录待支付金额是否相等，没问题则将桌台记录置为结账中状态，
	 * @param merchantId
	 * @param storeId
	 * @param orderId
	 * @param isToSettling
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreTableRecord tableRecordToSettling (int merchantId, long storeId, String orderId, boolean isToSettling, int clientType) throws T5weiException {
		// 查询订单
		StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, false);
		if (storeOrder == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "orderId["+orderId+"] invalid");
		}
		if (storeOrder.isTableRecordMasterOrder()) { 
			long tableRecordId = storeOrder.getTableRecordId();
			StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
			if (storeTableRecord == null) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "can not query storeTableRecord by id["+tableRecordId+"]");
			}
			if (!storeTableRecord.isSettleMent() && !storeTableRecord.isClearTable() && !storeTableRecord.isSettling()) {
				// 校验主订单金额和当前应付金额是否已经发生变化
				long waitSettleAmount = storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount()) - storeOrder.getStaffDerate();
				if (waitSettleAmount <= 0) {
					throw new T5weiException(StoreTableErrorCodeEnum.WAIT_SETTLE_AMOUNT_ERROR.getValue(), "waitSettleAmount["+waitSettleAmount+"] less 0, can not settle-pay");
				} else {
					if (waitSettleAmount != storeOrder.getPayablePrice()) {
						throw new T5weiException(StoreTableErrorCodeEnum.PAY_AMOUNT_ERROR.getValue(), "masterStoreOrder["+orderId+"] payAblePrice["+storeOrder.getPayablePrice()+"] != waitSettleAmount["+waitSettleAmount+"]");
					} else {
						if (isToSettling) {
							storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.SETTLING.getValue());
							storeTableRecord.setUpdateTime(System.currentTimeMillis());
							storeTableRecord.update();
							//记录桌台记录操作日志 //TODO 缺少服务员ID 和 客户端类型
							storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), 0, 0,
									clientType, TableRecordOptlogTypeEnum.TABLE_SETTLING.getValue(),
									"桌台记录结账中", storeTableRecord.getUpdateTime());
						} else { // 目前只有赊账结账时，该参数为false，赊账结账前需要判断，如果桌台子订单已经支付过，则不允许使用赊账进行结账
							if (storeTableRecord.getPaidAmount() > 0) {
								throw new T5weiException(StoreTableErrorCodeEnum.SUBORDER_PAID_CAN_NOT_CREDIT_SETTLE.getValue(), "tableRecord["+tableRecordId+"] subOrder paid, can not credit settle");
							}
						}
					}
				}
			} else if (storeTableRecord.isSettling()) {
				log.warn("storeTableRecord["+tableRecordId+"] status already has SETTLING");
				return null;
			} else {
				log.error("storeTableRecord["+tableRecordId+"] status[SETTLE, CLEARTABLE, SETTLING] can not to SETTLING");
				throw new T5weiException(StoreTableErrorCodeEnum.TABLE_RECORD_STATUS_ERROR.getValue(), "storeTableRecord["+tableRecordId+"] status[SETTLE, CLEARTABLE, SETTLING] can not to SETTLING");
			}
			return storeTableRecord;
		} else {
			return null;
		}
	}
	
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void tableRecordSettlingCancelMsgProcessor (int merchantId, long storeId, long tableRecordId) throws T5weiException {
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "can not query storeTableRecord by id["+tableRecordId+"]");
		}
		if (!storeTableRecord.isSettling()) {
			return;
		}
		// 调用撤销结账方法
		this.tableRecordSettleCancel(storeTableRecord);
	}
	
	/**
	 * 撤销桌台记录结账中状态
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @throws TException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public boolean tableRecordSettleCancel (int merchantId, long storeId, long tableRecordId) throws TException {
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "can not query storeTableRecord by id["+tableRecordId+"]");
		}
		return this.tableRecordSettleCancel(storeTableRecord);
	}
	
	private boolean tableRecordSettleCancel (StoreTableRecord storeTableRecord) throws T5weiException {
		int merchantId = storeTableRecord.getMerchantId();
		long storeId = storeTableRecord.getStoreId();
		long tableRecordId = storeTableRecord.getTableRecordId();
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) { // 已经结账或清台的桌台记录不允许撤销结账
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLEMENT_OR_CLEAR_CAN_NOT_CANCEL_SETTLE.getValue(), "storeTableRecord["+tableRecordId+"] SETTLEMENT or CLEARTABLE can not cancel settle"); 
		} 
		if (storeTableRecord.isSettleFail()) { // 结账失败的桌台记录暂时不允许撤销结账
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_FAIL.getValue(), "storeTableRecord["+tableRecordId+"] settle fail"); 
		}
		if (!storeTableRecord.isSettling()) { // 桌台记录非结账中无需撤销
			log.warn("storeTableRecord["+tableRecordId+"] status is not SETTLING can not to cancel settling");
			//throw new T5weiException(StoreTableErrorCodeEnum.TABLE_RECORD_STATUS_ERROR.getValue(), "storeTableRecord["+tableRecordId+"] status is not SETTLING can not to cancel settling");
			return true;
		}
		// 判断桌台记录当前处于支付还是退款
		long waitSettleAmount = storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount());
		if (waitSettleAmount > 0) { // 需要用户付款，需要判断主订单状态
			// 获取主订单
			StoreOrder masterOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, true);
			if (masterOrder == null) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord["+tableRecordId+"] masterOrder is null");
			}
			if (masterOrder.isPayFinish()) { // 主订单已经支付成功，等待桌台记录置为结账完成，不允许撤销
				throw new T5weiException(StoreTableErrorCodeEnum.MASTER_ORDER_PAY_FINISH_CAN_NOT_CANCEL_SETTLE.getValue(), "storeTableRecord["+tableRecordId+"] master order pay finish, can not cancel settle"); 
			} 
			/*else if (masterOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue()) { // 主订单支付中不允许撤销结账
				throw new T5weiException(StoreTableErrorCodeEnum.MASTER_ORDER_PAYING_CAN_NOT_CANCEL_SETTLE.getValue(), "storeTableRecord["+tableRecordId+"] master order paying, can not cancel settle"); 
			}*/ 
			else {
				// 请求m-pay支付撤销
				try {
					orderFacade.cancelPayOrder(masterOrder.getOrderId(), PaySrcEnum.M_5WEI.getValue());
				} catch (TPayException e) {
					if (e.getErrorCode() == PayErrorCode.ARGUMENT_INVALID.getValue()) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "orderId["+masterOrder.getOrderId()+"] invalid");
					} else if (e.getErrorCode() == PayErrorCode.PAY_ORDER_PAY_SUCCESS.getValue()) {
						throw new T5weiException(StoreTableErrorCodeEnum.MASTER_ORDER_PAY_FINISH_CAN_NOT_CANCEL_SETTLE.getValue(), "storeTableRecord["+tableRecordId+"] master order pay finish, can not cancel settle"); 
					} else if (e.getErrorCode() == PayErrorCode.NOT_EXISTS_CAN_CANCEL_PAY_RECORD.getValue()) {
						
					} else if (e.getErrorCode() == PayErrorCode.EXISTS_LEGAL_PAY_RECORD.getValue()) {
						throw new T5weiException(StoreTableErrorCodeEnum.MASTER_ORDER_PAYING_CAN_NOT_CANCEL_SETTLE.getValue(), "storeTableRecord["+tableRecordId+"] master order paying, can not cancel settle"); 
					} else if (e.getErrorCode() == PayErrorCode.EXISTS_IPOS_PAYING_RECORD.getValue()) {
						throw new T5weiException(StoreTableErrorCodeEnum.MASTER_ORDER_PAYING_CAN_NOT_CANCEL_SETTLE.getValue(), "storeTableRecord["+tableRecordId+"] master order paying, can not cancel settle");
					} else if (e.getErrorCode() == PayErrorCode.BUSIN_ORDER_INVALID.getValue()) {
					
					} else {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "call m-pay cancel settle, unknow errorCode["+e.getErrorCode()+"]");
					}
				} catch (TException e) {
					throw new T5weiException(StoreTableErrorCodeEnum.REQUEST_CANCEL_SETTLE_FAIL.getValue(), "storeTableRecord["+tableRecordId+"] request cancel settle fail");
				}
			}
			masterOrder.setPayStatus(StoreOrderPayStatusEnum.NOT.getValue());
			masterOrder.setUpdateTime(System.currentTimeMillis());
			masterOrder.update();
		} else if (waitSettleAmount < 0) { // 需要退款，需要判断桌台批量退款记录
			// 查询批量退款记录表
			List<TableRecordBatchRefundRecord> tableRecordBatchRefundRecords = tableRecordBatchRefundRecordDAO.getSettleTableRecordBatchRefund(tableRecordId);
			if (tableRecordBatchRefundRecords == null || tableRecordBatchRefundRecords.isEmpty()) {
				log.error("storeTableRecord["+tableRecordId+"] status is SETTLING, waitSettleAmount["+waitSettleAmount+"]<0 can not find tableRecordBatchRefundRecord");
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord["+tableRecordId+"] status is SETTLING, waitSettleAmount["+waitSettleAmount+"]<0 can not find tableRecordBatchRefundRecord");
			}
			for (TableRecordBatchRefundRecord tableRecordBatchRefundRecord : tableRecordBatchRefundRecords) {
				if (tableRecordBatchRefundRecord.getStatus() != BatchRefundStatusEnum.UNSTART.getValue() && tableRecordBatchRefundRecord.getStatus() != BatchRefundStatusEnum.FAIL.getValue()) {
					throw new T5weiException(StoreTableErrorCodeEnum.TABLE_RECORD_SETTLE_REFUNDING_CAN_NOT_CANCEL_SETTLE.getValue(), "storeTableRecord["+tableRecordId+"] SELLTE REFUNDING, can not cancel settle");
				}
			}
		} else { // 待结账金额为0时，不应该存在结账中状态
			log.error("storeTableRecord["+tableRecordId+"] status is SETTLING, waitSettleAmount=0");
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord["+tableRecordId+"] status is SETTLING, waitSettleAmount=0");
		}
		
		TableStatusResult tableStatusResult = this.getTableRecordStatus(merchantId, storeId, storeTableRecord);
		TableRecordStatusEnum TableRecordStatusEnum = tableStatusResult.getTableRecordStatusEnum();
		storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.getValue());
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		//记录桌台记录操作日志
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, tableRecordId, 0, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_SETTLE_CANCEL.getValue(),
				"桌台记录撤销结账操作,当前桌台记录的状态为[" + storeTableRecord.getTableRecordStatus() + "]", storeTableRecord.getUpdateTime());
		return true;
	}
	
	
	/**
	 * 结账-退款 需要改造，做到调用结账-退款后，一定退款成功
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @param orderRefundList 退菜请求列表
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	@Deprecated
	public List<StoreOrderDTO> settleTableRecordRefund (int merchantId, long storeId, long tableRecordId, List<StoreOrderRefundModeParam> orderRefundList)
			throws T5weiException, TException {
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		// 只有未结账状态的桌台记录才能退款
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_SETTLE_REFUND.getValue(), "tableRecord["+tableRecordId+"], tableStatus["+TableRecordStatusEnum.findByValue(storeTableRecord.getTableRecordStatus())+"], can not settle-record");
		}
		// 查询各项付款金额
		long waitSettleAmount = storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount());
		if (waitSettleAmount >= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.CAN_NOT_SETTLE_REFUND.getValue(), "tableRecord["+tableRecordId+"] waitSettleAmount["+waitSettleAmount+"]>=0, can not settle-refund");
		}
		List<StoreOrderDTO> storeOrderDTOList = new ArrayList<StoreOrderDTO>();
		for (StoreOrderRefundModeParam storeOrderRefundModeParam : orderRefundList) {
			if (storeOrderRefundModeParam.getAccountRefundAmount() + storeOrderRefundModeParam.getCashierRefundAmount() + storeOrderRefundModeParam.getPrepaidCardRefundAmount() == 0 && !storeOrderRefundModeParam.isCouponRefund() && !storeOrderRefundModeParam.isVoucherRefund() && storeOrderRefundModeParam.getVoucherRefundPayRecordIdsSize() == 0) {
				continue;
			}
			StoreOrderDTO storeOrderDTO = storeOrderFacadeImpl.refundStoreOrderMode(storeOrderRefundModeParam);
			storeOrderDTOList.add(storeOrderDTO);
		}
		this.refreshTableRecord(merchantId, storeId, storeTableRecord);
		if (storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount()) == 0) {
			StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, true);
			List<StoreOrder> toTradeFinishOrders = new ArrayList<StoreOrder>();
			toTradeFinishOrders.add(masterStoreOrder);
			List<StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrder.getOrderId(), true);
			for (StoreOrder subStoreOrder : subStoreOrderList) {
				toTradeFinishOrders.add(subStoreOrder);
			}
			storeOrderService.updateOrderTradeFinish(tableRecordId, toTradeFinishOrders);
			storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.SETTLEMENT.getValue());
			storeTableRecord.setSettleStaffId(orderRefundList.get(0).getStaffId());
			storeTableRecord.setUpdateTime(System.currentTimeMillis());
			storeTableRecord.setSettleTime(System.currentTimeMillis());
			storeTableRecordDAO.update(storeTableRecord);
		} else {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+tableRecordId+"] settle refund fail, waitSettleAmount["+waitSettleAmount+"] != 0");
		}
		return storeOrderDTOList;
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public long tableRecordRefund (int merchantId, long storeId, long tableRecordId, List<StoreOrderRefundModeParam> orderRefundList, int refundVersion) throws TPayException, TException {
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (!storeTableRecord.isSettleMent() && !storeTableRecord.isClearTable()) {
			throw new T5weiException(StoreTableErrorCodeEnum.UN_SETTLEMENT_AND_CLEAR_CAN_NOT_REFUND.getValue(), "tableRecordId["+tableRecordId+"] un-settlement and un-clear, can not refund");
		}
		long requestRefundAmount = 0L; // 请求退款总金额
		for (StoreOrderRefundModeParam storeOrderRefundModeParam : orderRefundList) {
			String orderId = storeOrderRefundModeParam.getOrderId();
			StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, false, false);
			storeOrderFacadeValidate.refundPartStoreOrder(storeOrder);
			String payOrderId = storeOrder.getPayOrderId();
			if (payOrderId == null || payOrderId.isEmpty()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
						DataUtil.infoWithParams("storeId=#1, orderId=#2, payOrderId is null ", new Object[]{storeId, orderId}));
			}
			long couponAmount = 0L;
			long dynamicPayAmount = 0L;
			long voucherRefundAmount4Single = 0;
			if (storeOrderRefundModeParam.isCouponRefund() || storeOrderRefundModeParam.isVoucherRefund() || storeOrderRefundModeParam.getVoucherRefundPayRecordIdsSize()>0) {
				// 获取订单支付详情
				PayResultOfPayOrder payResultOfPayOrder = payFacade.getPayResultOfPayOrder(payOrderId);
				couponAmount = payResultOfPayOrder.getCouponAmount();
				List<String> voucherRefundPayRecordIds = storeOrderRefundModeParam.getVoucherRefundPayRecordIds(); //请求退款的第三方券支付记录ID集合
				List<PayResultOfDynamicPayMethod> payResultOfDynamicPayMethods = payResultOfPayOrder.getPayResultOfDynamicPayMethodList();
				if (payResultOfDynamicPayMethods!=null && payResultOfDynamicPayMethods.size() > 0) {
					if(storeOrderRefundModeParam.isVoucherRefund()){
						voucherRefundAmount4Single = payResultOfDynamicPayMethods.get(0).getAmount();
					}
					//add by wxy 20161111
					for(PayResultOfDynamicPayMethod payResultOfDynamicPayMethod : payResultOfDynamicPayMethods){
						if(voucherRefundPayRecordIds != null && voucherRefundPayRecordIds.contains(payResultOfDynamicPayMethod.getPayRecordId())){
							dynamicPayAmount += payResultOfDynamicPayMethod.getAmount();
						}
					}
				}
			}
			requestRefundAmount += storeOrderRefundModeParam.getAccountRefundAmount() + storeOrderRefundModeParam.getCashierRefundAmount()
					+ storeOrderRefundModeParam.getPrepaidCardRefundAmount() + storeOrderRefundModeParam.getOriRefundAmount();
			if (storeOrderRefundModeParam.isCouponRefund()) {
				requestRefundAmount += couponAmount;
			}
			if (storeOrderRefundModeParam.isVoucherRefund() || storeOrderRefundModeParam.getVoucherRefundPayRecordIdsSize()>0) {
				if(dynamicPayAmount > 0){
					requestRefundAmount += dynamicPayAmount; //收银台组合支付多张券
				}else{
					requestRefundAmount += voucherRefundAmount4Single; //兼容旧版单张券
				}
			}
		}
		if (requestRefundAmount <= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.REQUEST_REFUND_AMOUNT_ERROR.getValue(), "requestRefundAmount["+requestRefundAmount+"]");
		}
		// 拼装参数请求批量退款接口
		BatchCreateRefundRecordParam batchCreateRefundRecordParam = new BatchCreateRefundRecordParam();
		batchCreateRefundRecordParam.setMerchantId(merchantId);
		batchCreateRefundRecordParam.setStoreId(storeId);
		batchCreateRefundRecordParam.setRefundVersion(refundVersion);
		List<CreateRefundRecordParam> createRefundRecordParams = new ArrayList<CreateRefundRecordParam>();
		for (StoreOrderRefundModeParam storeOrderRefundModeParam : orderRefundList) {
			if (storeOrderRefundModeParam.getAccountRefundAmount() + storeOrderRefundModeParam.getCashierRefundAmount() + storeOrderRefundModeParam.getPrepaidCardRefundAmount() + storeOrderRefundModeParam.getOriRefundAmount() == 0
					&& !storeOrderRefundModeParam.isCouponRefund() && !storeOrderRefundModeParam.isVoucherRefund() && storeOrderRefundModeParam.getVoucherRefundPayRecordIdsSize() == 0) {
				continue;
			}
			StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, storeOrderRefundModeParam.getOrderId(), false, false);
			CreateRefundRecordParam createRefundRecordParam = new CreateRefundRecordParam();
			createRefundRecordParam.setPayOrderId(storeOrder.getPayOrderId());
			createRefundRecordParam.setPreOrder(false);
			createRefundRecordParam.setRefundBusinId(storeOrderRefundModeParam.getOrderId());
			createRefundRecordParam.setPaySrc(PaySrcEnum.M_5WEI.getValue());
			RefundParam refundParam = new RefundParam();
			refundParam.setCouponRefund(storeOrderRefundModeParam.isCouponRefund());
			refundParam.setPrepaidcardRefund(storeOrderRefundModeParam.getPrepaidCardRefundAmount());
			refundParam.setUserAccountRefund(storeOrderRefundModeParam.getAccountRefundAmount());
			refundParam.setCashRefund(storeOrderRefundModeParam.getCashierRefundAmount());
			refundParam.setVoucherRefund(storeOrderRefundModeParam.isVoucherRefund());
			refundParam.setOriRefund(storeOrderRefundModeParam.getOriRefundAmount());
			refundParam.setVoucherRefundPayRecordIds(storeOrderRefundModeParam.getVoucherRefundPayRecordIds()); //请求退款的券支付记录ID add by wxy 20161111
			createRefundRecordParam.setRefundParam(refundParam);
			createRefundRecordParams.add(createRefundRecordParam);
		}
		batchCreateRefundRecordParam.setCreateRefundRecordParams(createRefundRecordParams);
		BatchCreateRefundRecordResult batchCreateRefundRecordResult = refundFacade.batchCreateRefundRecord(batchCreateRefundRecordParam);
		long batchRefundId = batchCreateRefundRecordResult.getBatchRefundId();
		// 创建桌台记录批量退款记录
		TableRecordBatchRefundRecord tableRecordBatchRefundRecord = new TableRecordBatchRefundRecord();
		tableRecordBatchRefundRecord.setBatchRefundId(batchRefundId);
		tableRecordBatchRefundRecord.setMerchantId(merchantId);
		tableRecordBatchRefundRecord.setStatus(TableRecordBatchRefundStatusEnum.REFUNDING.getValue());
		tableRecordBatchRefundRecord.setStoreId(storeId);
		tableRecordBatchRefundRecord.setTableRecordId(tableRecordId);
		tableRecordBatchRefundRecord.setCreateTime(System.currentTimeMillis());
		tableRecordBatchRefundRecord.setUpdateTime(System.currentTimeMillis());
		long staffId = 0;
		if (orderRefundList!=null && !orderRefundList.isEmpty()) {
			staffId = orderRefundList.get(0).getStaffId();
		}
		tableRecordBatchRefundRecord.setStaffId(staffId);
		tableRecordBatchRefundRecord.setRefundAmount(requestRefundAmount);
		tableRecordBatchRefundRecord.setType(ORDERMANAGERREFUND);
		List<RefundRecordDBDTO> refundRecordDBDTOs = batchCreateRefundRecordResult.getRefundRecordDBDTOs();
		StringBuilder refundRecordIds  = new StringBuilder();
		for (int i=0; i<refundRecordDBDTOs.size(); i++) {
			if (i > 0) {
				refundRecordIds.append(",");
			}
			refundRecordIds.append(refundRecordDBDTOs.get(i).getRefundRecordId());
		}
		tableRecordBatchRefundRecord.setRefundRecordIds(refundRecordIds.toString());
		tableRecordBatchRefundRecord.setClientType(ClientTypeEnum.CASHIER.getValue());
		tableRecordBatchRefundRecord.create();
		//桌台记录操作日志
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, tableRecordId, staffId, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_REFUND.getValue(),
				"桌台记录订单管理退款",tableRecordBatchRefundRecord.getCreateTime());

		refundFacade.requestBatchRefund(batchRefundId, refundVersion);
		return batchRefundId;
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public long settleTableRecordRefund2 (int merchantId, long storeId, long tableRecordId, List<StoreOrderRefundModeParam> orderRefundList, int refundVersion)
			throws T5weiException, TException {
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		// 只有未结账状态的桌台记录才能退款
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_SETTLE_REFUND.getValue(), "tableRecord["+tableRecordId+"], tableStatus["+TableRecordStatusEnum.findByValue(storeTableRecord.getTableRecordStatus())+"], can not settle-record");
		}
		// 查询各项付款金额
		long waitSettleAmount = storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount());
		if (waitSettleAmount >= 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.CAN_NOT_SETTLE_REFUND.getValue(), "tableRecord["+tableRecordId+"] waitSettleAmount["+waitSettleAmount+"]>=0, can not settle-refund");
		}
		long requestRefundAmount = 0L; // 请求退款总金额
		for (StoreOrderRefundModeParam storeOrderRefundModeParam : orderRefundList) {
			String orderId = storeOrderRefundModeParam.getOrderId();
			StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, false, false);
			storeOrderFacadeValidate.refundPartStoreOrder(storeOrder);
			String payOrderId = storeOrder.getPayOrderId();
			if (payOrderId == null || payOrderId.isEmpty()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
						DataUtil.infoWithParams("storeId=#1, orderId=#2, payOrderId is null ", new Object[]{storeId, orderId}));
			}
			long couponAmount = 0L;
			long dynamicPayAmount = 0L;
			long voucherRefundAmount4Single = 0;
			if (storeOrderRefundModeParam.isCouponRefund() || storeOrderRefundModeParam.isVoucherRefund() || storeOrderRefundModeParam.getVoucherRefundPayRecordIdsSize()>0) {
				// 获取订单支付详情
				PayResultOfPayOrder payResultOfPayOrder = payFacade.getPayResultOfPayOrder(payOrderId);
				couponAmount = payResultOfPayOrder.getCouponAmount();
				List<String> voucherRefundPayRecordIds = storeOrderRefundModeParam.getVoucherRefundPayRecordIds(); //请求的第三方券支付记录ID集合
				List<PayResultOfDynamicPayMethod> payResultOfDynamicPayMethods = payResultOfPayOrder.getPayResultOfDynamicPayMethodList();
				if (payResultOfDynamicPayMethods!=null && payResultOfDynamicPayMethods.size() > 0) {
					if(storeOrderRefundModeParam.isVoucherRefund()){
						voucherRefundAmount4Single = payResultOfDynamicPayMethods.get(0).getAmount();
					}
					// add by wxy 20161111
					for(PayResultOfDynamicPayMethod payResultOfDynamicPayMethod : payResultOfDynamicPayMethods){
						if(voucherRefundPayRecordIds != null && voucherRefundPayRecordIds.contains(payResultOfDynamicPayMethod.getPayRecordId())){
							dynamicPayAmount += payResultOfDynamicPayMethod.getAmount();
						}
					}
//					PayResultOfDynamicPayMethod payResultOfDynamicPayMethod = payResultOfDynamicPayMethods.get(0);
//					dynamicPayAmount = payResultOfDynamicPayMethod.getAmount();
				}
			}
			requestRefundAmount += storeOrderRefundModeParam.getAccountRefundAmount() + storeOrderRefundModeParam.getCashierRefundAmount()
					+ storeOrderRefundModeParam.getPrepaidCardRefundAmount() + storeOrderRefundModeParam.getOriRefundAmount();
			if (storeOrderRefundModeParam.isCouponRefund()) {
				requestRefundAmount += couponAmount;
			}
			if (storeOrderRefundModeParam.isVoucherRefund() || storeOrderRefundModeParam.getVoucherRefundPayRecordIdsSize()>0) {
				if(dynamicPayAmount > 0){
					requestRefundAmount += dynamicPayAmount; //收银台组合支付多张券
				}else{
					requestRefundAmount += voucherRefundAmount4Single; //兼容旧版单张券
				}
			}
		}
		if (requestRefundAmount + waitSettleAmount != 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.REQUEST_REFUND_AMOUNT_ERROR.getValue(), "requestRefundAmount["+requestRefundAmount+"] != waitSettleAmount["+waitSettleAmount+"]");
		}
		// 拼装参数请求批量退款接口
		BatchCreateRefundRecordParam batchCreateRefundRecordParam = new BatchCreateRefundRecordParam();
		batchCreateRefundRecordParam.setMerchantId(merchantId);
		batchCreateRefundRecordParam.setStoreId(storeId);
		batchCreateRefundRecordParam.setRefundVersion(refundVersion);
		List<CreateRefundRecordParam> createRefundRecordParams = new ArrayList<CreateRefundRecordParam>();
		for (StoreOrderRefundModeParam storeOrderRefundModeParam : orderRefundList) {
			if (storeOrderRefundModeParam.getAccountRefundAmount() + storeOrderRefundModeParam.getCashierRefundAmount() + storeOrderRefundModeParam.getPrepaidCardRefundAmount() + storeOrderRefundModeParam.getOriRefundAmount() == 0
					&& !storeOrderRefundModeParam.isCouponRefund() && !storeOrderRefundModeParam.isVoucherRefund() && storeOrderRefundModeParam.getVoucherRefundPayRecordIdsSize() == 0) {
				continue;
			}
			StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, storeOrderRefundModeParam.getOrderId(), false, false);
			CreateRefundRecordParam createRefundRecordParam = new CreateRefundRecordParam();
			createRefundRecordParam.setPayOrderId(storeOrder.getPayOrderId());
			createRefundRecordParam.setPreOrder(false);
			createRefundRecordParam.setRefundBusinId(storeOrderRefundModeParam.getOrderId());
			createRefundRecordParam.setPaySrc(PaySrcEnum.M_5WEI.getValue());
			RefundParam refundParam = new RefundParam();
			refundParam.setCouponRefund(storeOrderRefundModeParam.isCouponRefund());
			refundParam.setPrepaidcardRefund(storeOrderRefundModeParam.getPrepaidCardRefundAmount());
			refundParam.setUserAccountRefund(storeOrderRefundModeParam.getAccountRefundAmount());
			refundParam.setCashRefund(storeOrderRefundModeParam.getCashierRefundAmount());
			refundParam.setVoucherRefund(storeOrderRefundModeParam.isVoucherRefund());
			refundParam.setOriRefund(storeOrderRefundModeParam.getOriRefundAmount());
			refundParam.setVoucherRefundPayRecordIds(storeOrderRefundModeParam.getVoucherRefundPayRecordIds()); //请求退款的券支付记录ID add by wxy 20161111
			createRefundRecordParam.setRefundParam(refundParam);
			createRefundRecordParams.add(createRefundRecordParam);
		}
		batchCreateRefundRecordParam.setCreateRefundRecordParams(createRefundRecordParams);
		BatchCreateRefundRecordResult batchCreateRefundRecordResult = refundFacade.batchCreateRefundRecord(batchCreateRefundRecordParam);
		long batchRefundId = batchCreateRefundRecordResult.getBatchRefundId();
		// 创建桌台记录批量退款记录
		TableRecordBatchRefundRecord tableRecordBatchRefundRecord = new TableRecordBatchRefundRecord();
		tableRecordBatchRefundRecord.setBatchRefundId(batchRefundId);
		tableRecordBatchRefundRecord.setMerchantId(merchantId);
		tableRecordBatchRefundRecord.setStatus(TableRecordBatchRefundStatusEnum.REFUNDING.getValue());
		tableRecordBatchRefundRecord.setStoreId(storeId);
		tableRecordBatchRefundRecord.setTableRecordId(tableRecordId);
		tableRecordBatchRefundRecord.setCreateTime(System.currentTimeMillis());
		tableRecordBatchRefundRecord.setUpdateTime(System.currentTimeMillis());
		long staffId = 0;
		if (orderRefundList!=null && !orderRefundList.isEmpty()) {
			staffId = orderRefundList.get(0).getStaffId();
		}
		tableRecordBatchRefundRecord.setStaffId(staffId);
		tableRecordBatchRefundRecord.setRefundAmount(requestRefundAmount);
		tableRecordBatchRefundRecord.setType(SETTLEREFUND);
		List<RefundRecordDBDTO> refundRecordDBDTOs = batchCreateRefundRecordResult.getRefundRecordDBDTOs();
		StringBuilder refundRecordIds  = new StringBuilder();
		for (int i=0; i<refundRecordDBDTOs.size(); i++) {
			if (i > 0) {
				refundRecordIds.append(",");
			}
			refundRecordIds.append(refundRecordDBDTOs.get(i).getRefundRecordId());
		}
		tableRecordBatchRefundRecord.setRefundRecordIds(refundRecordIds.toString());
		tableRecordBatchRefundRecord.create();
		storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.SETTLING.getValue());
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		//记录桌台记录操作日志
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), staffId, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_SETTLING.getValue(),
				"桌台记录退款结账", storeTableRecord.getUpdateTime());

		refundFacade.requestBatchRefund(batchRefundId, refundVersion);
		return batchRefundId;
	}
	
	/**
	 * 接收批量退款回调通知
	 * @param batchRefundId
	 * @param refundResults
	 * @param status
	 * @param type
	 * @param errorCode
	 * @param errorMsg
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public ConfirmRefundResult confirmRefund (long batchRefundId, List<RefundResultDTO> refundResults, int status, int type, String errorCode, String errorMsg) throws TException {
		ConfirmRefundResult confirmRefundResult = new ConfirmRefundResult();
		confirmRefundResult.setRefundSuccess(false);
		if (type == QueueRefundType.BATCH_REFUND.getValue()) {
			// 获取桌台批量退款记录
			TableRecordBatchRefundRecord tableRecordBatchRefundRecord = tableRecordBatchRefundRecordDAO.getTableRecordBatchRefundRecordById(batchRefundId);
			if (tableRecordBatchRefundRecord == null) {
				log.error("batchRefundId["+batchRefundId+"] invalid");
				return confirmRefundResult;
			}
			long tableRecordId = tableRecordBatchRefundRecord.getTableRecordId();
			int merchantId = tableRecordBatchRefundRecord.getMerchantId();
			long storeId = tableRecordBatchRefundRecord.getStoreId();
			// 获取桌台记录
			StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
			if (storeTableRecord == null) {
				log.error("tableRecordId["+tableRecordId+"] invalid");
				return confirmRefundResult;
			}
			int refundType = tableRecordBatchRefundRecord.getType();
			if (status == BatchRefundStatusEnum.SUCCESS.getValue()) { // 退款成功
				if (tableRecordBatchRefundRecord.getStatus() == BatchRefundStatusEnum.SUCCESS.getValue()) {
					return confirmRefundResult;
				}
				Map<String, StoreOrder> storeOrderMap = new HashMap<String, StoreOrder>();
				List<Long> refundRecordIds = Lists.newArrayList();
				for (RefundResultDTO refundResultDTO : refundResults) {
					refundRecordIds.add(refundResultDTO.getRefundRecordId());
					String orderId = refundResultDTO.getBusinId();
					StoreOrderRefundStatusEnum merchantRefund;
					if (refundResultDTO.getPayOrderStatus() == PayOrderStatusDTO.FULL_REFUND.getValue()) {
						merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_ALL;
					} else {
						merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_PART;
					}
					StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, false);
					if (storeOrder == null) {
						throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), "store order not exist");
					}
					storeOrderMap.put(orderId, storeOrder);
					// 尚未交易状态，往下走，其他状态，返回并提示给用户
					storeOrder.setRefundStatus(merchantRefund.getValue());
					// 信息入库
					int optType = StoreOrderOptlogTypeEnum.CASHIER_REFUND_ORDER.getValue();
					String optRemark = merchantRefund.toString();
					storeOrder.setUpdateTime(System.currentTimeMillis());
					storeOrderDAO.update(storeOrder);
					int clientType = storeOrder.getClientType();
					storeOrderOptlogDAO.createOptlog(storeOrder, tableRecordBatchRefundRecord.getStaffId(), clientType, optType, optRemark);
				}
				// 更新桌台批量退款记录
				List<RefundRecordDBDTO> refundRecordDBDTOs = refundFacade.getRefundRecordDBDTOs(refundRecordIds);
				confirmRefundResult.setRefundRecordDBDTOs(refundRecordDBDTOs);
				long actualRefundAmount = this.getActualRefundAmount(refundRecordDBDTOs);
				tableRecordBatchRefundRecord.setActualRefundAmount(actualRefundAmount);
				tableRecordBatchRefundRecord.setUpdateTime(System.currentTimeMillis());
				tableRecordBatchRefundRecord.setStatus(TableRecordBatchRefundStatusEnum.FINISH.getValue());
				tableRecordBatchRefundRecord.update();
				this.refreshTableRecord(merchantId, storeId, storeTableRecord);
				if (refundType == SETTLEREFUND) {
					long waitSettleAmount = storeTableRecord.getPayAbleAmount()-(storeTableRecord.getPaidAmount()-storeTableRecord.getRefundAmount());
					if (waitSettleAmount == 0) {
						StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, tableRecordId, true);
						List<StoreOrder> toTradeFinishOrders = new ArrayList<StoreOrder>();
						toTradeFinishOrders.add(masterStoreOrder);
						List<StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterStoreOrder.getOrderId(), true);
						for (StoreOrder subStoreOrder : subStoreOrderList) {
							toTradeFinishOrders.add(subStoreOrder);
						}
						storeOrderService.updateOrderTradeFinish(tableRecordId, toTradeFinishOrders);
						storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.SETTLEMENT.getValue());
						storeTableRecord.setSettleStaffId(tableRecordBatchRefundRecord.getStaffId());
						storeTableRecord.setSettleTime(System.currentTimeMillis());
						/*// 获取店铺桌台设置
						StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
						// 如果开启了结账后自动清台，则设置桌台记录为清台状态
						if (storeTableSetting.isEnableTableMode() && storeTableSetting.isEnableTableAutoClear()) {
							storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.CLEAR_TABLE.getValue());
							storeTableRecord.setClearTableTime(System.currentTimeMillis());
						}*/ 
						StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
						this.autoClearTable(merchantId, storeId, storeTableRecord, storeTableSetting);
						if (storeTableSetting.isEnableTableMode() && storeTableSetting.isEnableTableAutoClear()) {
							this.clearStoreMeal(merchantId, storeId, tableRecordId, storeTableRecord.getOrderId(), subStoreOrderList, null);
						}
						confirmRefundResult.setSettleRefund(true);
					} else {
						log.error("tableRecord["+tableRecordId+"] settle refund fail, waitSettleAmount["+waitSettleAmount+"] != 0");
						return null;
					}
				}
				storeTableRecord.setUpdateTime(System.currentTimeMillis());
				storeTableRecordDAO.update(storeTableRecord);
				if (refundType == SETTLEREFUND) {
					//记录操作日志
					storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), 0, 0,
							0, TableRecordOptlogTypeEnum.TABLE_SETTLEMENT.getValue(),
							"桌台记录退款结账成功", storeTableRecord.getSettleTime());
				} else {
					//记录操作日志
					storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), 0, 0,
							0, TableRecordOptlogTypeEnum.TABLE_REFUND_SUCCESS.getValue(),
							"桌台记录订单管理退款成功", storeTableRecord.getSettleTime());
				}

				confirmRefundResult.setTableRecordBatchRefundRecord(tableRecordBatchRefundRecord);
				confirmRefundResult.setRefundSuccess(true);
				confirmRefundResult.setStoreOrderMap(storeOrderMap);
				return confirmRefundResult;
			} else if (status == BatchRefundStatusEnum.FAIL.getValue()) { // 退款失败
				log.error("tableRecord["+tableRecordId+"] settle refund fail");
				if (refundType == SETTLEREFUND) {
					// 结账失败
					storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.SETTLE_FAIL.getValue());
					storeTableRecord.setUpdateTime(System.currentTimeMillis());
					storeTableRecordDAO.update(storeTableRecord);
				}
				// 更新桌台批量退款记录
				tableRecordBatchRefundRecord.setUpdateTime(System.currentTimeMillis());
				tableRecordBatchRefundRecord.setStatus(TableRecordBatchRefundStatusEnum.FAIL.getValue());
				tableRecordBatchRefundRecord.setErrorCode(Integer.parseInt(errorCode));
				tableRecordBatchRefundRecord.setErrorMsg(errorMsg);
				tableRecordBatchRefundRecord.update();

				if (refundType == SETTLEREFUND) {
					//记录操作日志
					storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), 0, 0,
							0, TableRecordOptlogTypeEnum.TABLE_SETTLE_FAIL.getValue(),
							"桌台记录退款结账失败", storeTableRecord.getSettleTime());
				} else {
					//记录操作日志
					storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(), 0, 0,
							0, TableRecordOptlogTypeEnum.TABLE_REFUND_FAIL.getValue(),
							"桌台记录退款结账失败", storeTableRecord.getSettleTime());
				}
				return confirmRefundResult;
			} else {
				log.error("batchRefundId["+batchRefundId+"] status=["+status+"]");
				return confirmRefundResult;
			}
		} else {
			return confirmRefundResult;
		}
	}
	
	/**
	 * 查询桌台记录批量退款记录
	 * @param batchRefundId
	 * @return
	 * @throws T5weiException
	 */
	public TableRecordBatchRefundRecord queryTableRecordBatchRefundResult (long batchRefundId) throws T5weiException {
		// 查询桌台记录批量退款记录
		TableRecordBatchRefundRecord tableRecordBatchRefundRecord = tableRecordBatchRefundRecordDAO.getTableRecordBatchRefundRecordById(batchRefundId);
		if (tableRecordBatchRefundRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "batchRefundId["+batchRefundId+"] invalid");
		}
		return tableRecordBatchRefundRecord;
	}
	
	/**
	 * 为桌台记录设置整单折扣比例和整单折扣金额
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @param totalRebate 整单折扣
	 * @param totalDerate 整单减免
	 * @return
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreTableRecord setDerate4TableRecord (int merchantId, long storeId, long tableRecordId, double totalRebate, long totalDerate) 
			throws TException {
		// 查询桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		// 桌台记录已经结账或清台，不允许修改整单折扣和减免
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable() || storeTableRecord.isSettling()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_CHANGE_DERATE.getValue(), "tableRecord["+tableRecordId+"] settle or clearTable, can not change derate info");
		}
		/*long tableRecordPayAbleAmount = storeTableRecord.getPayAbleAmount();
		if (totalDerate > tableRecordPayAbleAmount) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecord["+tableRecordId+"] tableRecordPayAbleAmount["+tableRecordPayAbleAmount+"] must more than request totalDerate["+totalDerate+"]");
		}*/

		boolean isTotalRebateUpdate = false;
		boolean isTotalDerateUpdate = false;
		if (storeTableRecord.getDiscountPro() != totalRebate) {
			isTotalRebateUpdate = true;
		}
		if (storeTableRecord.getDiscountAmount() != totalDerate) {
			isTotalDerateUpdate = true;
		}
		storeTableRecord.setDiscountPro(totalRebate);
		storeTableRecord.setDiscountAmount(totalDerate);
		this.refreshTableRecord(merchantId, storeId, storeTableRecord);
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		//记录整单折扣和整单减免的日志 TODO 缺少员工ID 和 客户端类型
		if (isTotalRebateUpdate) {
			storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(),
					0, 0, ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_TOTAL_REBATE_UPDATE.getValue(),
					"整单折扣操作", storeTableRecord.getUpdateTime());
		}
		if (isTotalDerateUpdate) {
			storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId(),
					0, 0, ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_TOTAL_DERATE_UPDATE.getValue(),
					"整单减免操作", storeTableRecord.getUpdateTime());
		}
		return storeTableRecord;
	}
	
	/**
	 * 获取物理桌台记录列表
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param areaId 区域id
	 * @throws TException 
	 */
	public List<TableInfoDTO> getRealStoreTableRecords (int merchantId, long storeId, long areaId) throws TException {
		// 返回结果
		List<TableInfoDTO> tableInfoDTOList = new ArrayList<TableInfoDTO>();
		// 先查询出桌台信息
		List<StoreTable> storeTableList = storeTableService.getValidStoreTables(merchantId, storeId, areaId);
		for (StoreTable storeTable : storeTableList) {
			long tableId = storeTable.getTableId();
			List<StoreTableRecord> storeTableRecordList = storeTableRecordDAO.getStoreTableRecordListByTableIdSeqDesc(merchantId, storeId, tableId, false);
			TableInfoDTO tableInfoDTO = new TableInfoDTO();
			String name = storeTable.getName();
			tableInfoDTO.setName(name);
			tableInfoDTO.setStoreTableDTO(BeanUtil.copy(storeTable, StoreTableDTO.class));
			tableInfoDTO.setStoreTableRecordDTO(null);
			tableInfoDTO.setStoreTableStaffDTO(storeTable.getStoreTableStaffDTO());
			if (storeTableRecordList.isEmpty()) { // 空闲
				tableInfoDTO.setTableAndRecordStatus(TableAndRecordStatusEnum.FREE);
			} else {
				tableInfoDTO.setTableAndRecordStatus(TableAndRecordStatusEnum.SCRAPE_TOGETHER_TABLE);
			}
			tableInfoDTOList.add(tableInfoDTO);
		}
		return tableInfoDTOList;
	}
	
	/**
	 * 获取桌台记录列表
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param areaId 区域id
	 * @throws TException 
	 */
	public List<TableInfoDTO> getStoreTableRecords (int merchantId, long storeId, long areaId) throws TException {
		// 返回结果
		List<TableInfoDTO> tableInfoDTOList = new ArrayList<TableInfoDTO>();
		// 先查询出桌台信息
		List<StoreTable> storeTableList = storeTableService.getValidStoreTables(merchantId, storeId, areaId);
		// 循环遍历桌台查询出对应的桌台记录
		for (StoreTable storeTable : storeTableList) {
			long tableId = storeTable.getTableId();
			List<StoreTableRecord> storeTableRecordList = storeTableRecordDAO.getStoreTableRecordListByTableIdSeqAsc(merchantId, storeId, tableId, false);
			if (storeTableRecordList.isEmpty()) { // 空闲
				TableInfoDTO tableInfoDTO = new TableInfoDTO();
				String name = storeTable.getName();
				tableInfoDTO.setName(name);
				tableInfoDTO.setStoreTableDTO(BeanUtil.copy(storeTable, StoreTableDTO.class));
				tableInfoDTO.setStoreTableRecordDTO(null);
				tableInfoDTO.setTableAndRecordStatus(TableAndRecordStatusEnum.FREE);
				tableInfoDTO.setStaffId(storeTable.getStaffId());
				tableInfoDTOList.add(tableInfoDTO);
			} else { // 非空闲
				String name = storeTable.getName();
				// 先设置一个本桌桌台的
				TableInfoDTO tableInfoDTOOri = new TableInfoDTO();
				tableInfoDTOOri.setName(name);
				tableInfoDTOOri.setStoreTableDTO(BeanUtil.copy(storeTable, StoreTableDTO.class));
				StoreTableRecord storeTableRecordOri = storeTableRecordDAO.getStoreTableRecordListByTableIdAndSeq(merchantId, storeId, tableId, 0, false);
				if (storeTableRecordOri == null) {
					// 有拼台
					tableInfoDTOOri.setTableAndRecordStatus(TableAndRecordStatusEnum.SCRAPE_TOGETHER_TABLE);
					tableInfoDTOOri.setStaffId(storeTable.getStaffId());
					tableInfoDTOOri.setStoreTableRecordDTO(null);
				} else  {
					tableInfoDTOOri.setTableAndRecordStatus(this.tableStatucTransfer(TableRecordStatusEnum.findByValue(storeTableRecordOri.getTableRecordStatus())));
					tableInfoDTOOri.setStaffId(storeTableRecordOri.getStaffId());
					tableInfoDTOOri.setStoreTableRecordDTO(BeanUtil.copy(storeTableRecordOri, StoreTableRecordDTO.class));
				}
				tableInfoDTOList.add(tableInfoDTOOri);
				for (StoreTableRecord storeTableRecord : storeTableRecordList) {
					int seq = storeTableRecord.getTableRecordSeq();
					if (seq > 0) {
						TableInfoDTO tableInfoDTO = new TableInfoDTO();
						name = storeTable.getName() + "(+"+seq+")";
						tableInfoDTO.setName(name);
						tableInfoDTO.setStoreTableDTO(BeanUtil.copy(storeTable, StoreTableDTO.class));
						tableInfoDTO.setStoreTableRecordDTO(BeanUtil.copy(storeTableRecord, StoreTableRecordDTO.class));
						tableInfoDTO.setTableAndRecordStatus(this.tableStatucTransfer(TableRecordStatusEnum.findByValue(storeTableRecord.getTableRecordStatus())));
						tableInfoDTO.setStaffId(storeTableRecord.getStaffId());
						tableInfoDTOList.add(tableInfoDTO);
					}
				}
			}
		}
		List<Long> staffIds = new ArrayList<Long>();
		for (TableInfoDTO tableInfoDTO : tableInfoDTOList) {
			if (tableInfoDTO.getStaffId() > 0){
				staffIds.add(tableInfoDTO.getStaffId());
			}
		}
		if (!staffIds.isEmpty()) {
			Map<Long, StaffDTO> staffDTOMap = staffFacade.getStaffMapInIds(merchantId, staffIds, true);
			for (TableInfoDTO tableInfoDTO : tableInfoDTOList) {
				if (staffDTOMap.containsKey(tableInfoDTO.getStaffId())){
					StaffDTO staffDTO = staffDTOMap.get(tableInfoDTO.getStaffId());
					tableInfoDTO.setStoreTableStaffDTO(this.getStoreTableStaffDTO(staffDTO));
				}
			}
		}
		return tableInfoDTOList;
	}
	
	private TableAndRecordStatusEnum tableStatucTransfer (TableRecordStatusEnum tableRecordStatus) {
		if (tableRecordStatus.getValue() == TableRecordStatusEnum.WAIT_MEAL.getValue()) {
			return TableAndRecordStatusEnum.WAIT_MEAL;
		} else if (tableRecordStatus.getValue() == TableRecordStatusEnum.WAIT_SERVE.getValue()) {
			return TableAndRecordStatusEnum.WAIT_SERVE;
		} else if (tableRecordStatus.getValue() == TableRecordStatusEnum.SERVING.getValue()) {
			return TableAndRecordStatusEnum.SERVING;
		} else if (tableRecordStatus.getValue() == TableRecordStatusEnum.COMPLETE_SERVING.getValue()) {
			return TableAndRecordStatusEnum.COMPLETE_SERVING;
		} else if (tableRecordStatus.getValue() == TableRecordStatusEnum.SETTLEMENT.getValue()) {
			return TableAndRecordStatusEnum.SETTLEMENT;
		} else if (tableRecordStatus.getValue() == TableRecordStatusEnum.CLEAR_TABLE.getValue()) {
			return TableAndRecordStatusEnum.FREE;
		} else if (tableRecordStatus.getValue() == TableRecordStatusEnum.SETTLING.getValue()) {
			return TableAndRecordStatusEnum.SETTLING;
		} else if (tableRecordStatus.getValue() == TableRecordStatusEnum.SETTLE_FAIL.getValue()) {
			return TableAndRecordStatusEnum.SETTLING;
		} else {
			return TableAndRecordStatusEnum.SETTLING;
		}
	}
	
	/**
	 * 修改桌台记录
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @param serviceStaffId 服务员id
	 * @return StoreTableRecord 桌台记录
	 * @throws TException 
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)	
	public StoreTableRecord changeTableRecord (int merchantId, long storeId, long tableRecordId, long serviceStaffId, int customerTraffic) 
			throws TException {
		// 查询出桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable() || storeTableRecord.isSettling()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SETTLE_OR_CLEAR_CAN_NOT_CHANGE_STAFF_OR_CUSTOMERTRAFFIC.getValue(), "tableRecord["+tableRecordId+"] settle or clear_table, can not change staff or customerTraffic");
		}
		//桌台入客数是否修改
		boolean isTableCustomerTrafficUpdate = false;
		storeTableRecord.snapshot();
		if (customerTraffic == -1 && serviceStaffId == -1) {
			return storeTableRecord;
		}
		if (customerTraffic > -1) {
		    if(customerTraffic != storeTableRecord.getCustomerTraffic()){//修改入客数时,减免桌台费变为0
                storeTableRecord.setReductionTableFee(0);
            }
			if (storeTableRecord.getCustomerTraffic() != customerTraffic) {
				isTableCustomerTrafficUpdate = true;
			}
			storeTableRecord.setCustomerTraffic(customerTraffic);
			this.refreshTableRecord(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord);
			StoreOrderPlaceParam storeOrderPlaceParam = new StoreOrderPlaceParam();
			storeOrderPlaceParam.setMerchantId(merchantId);
			storeOrderPlaceParam.setStoreId(storeId);
			storeOrderPlaceParam.setTimeBucketId(storeTableRecord.getTimeBucketId());
			storeOrderPlaceParam.setTakeMode(StoreOrderTakeModeEnum.DINE_IN.getValue());
			storeOrderPlaceParam.setEnableAddDishes(false);
			long payAbleTableFee = this.getTableFee(storeOrderPlaceParam, customerTraffic);
			storeTableRecord.setPayAbleTableFee(payAbleTableFee);
			// 同步更新就餐人数给主订单
			if (!StringUtils.isNullOrEmpty(storeTableRecord.getOrderId())) {
				StoreOrder masterStoreOrder = storeOrderDAO.getById(merchantId, storeId, storeTableRecord.getOrderId(), false, false);
				masterStoreOrder.setCustomerTraffic(customerTraffic);
				masterStoreOrder.setUpdateTime(System.currentTimeMillis());
				masterStoreOrder.update();
			}
		}
		if (serviceStaffId > -1) {
			storeTableRecord.setStaffId(serviceStaffId);
		}
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		if (isTableCustomerTrafficUpdate) {
			//记录桌台记录操作日志 TODO 缺少了服务员和客户端类型
			storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, storeTableRecord.getTableRecordId() , 0, 0,
					ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_CUSTOMER_TRAFFIC_UPDATE.getValue(),
					"桌台记录入客数更新操作", storeTableRecord.getUpdateTime());
		}
		return storeTableRecord;
	}
	
	/**
	 * 结账前检查桌台记录是否允许结账（目前只检查是否全部出餐完毕）
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param tableRecordId 桌台记录id
	 * @return boolean（可以结账时返回true，否则报出异常，根据异常码判断禁止结账的原因）
	 * @throws T5weiException
	 */
	public boolean checkSettleCondition (int merchantId, long storeId, long tableRecordId) throws T5weiException {
		// 获取桌台记录
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, false);
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		storeTableRecord = this.getMergeTableRecord(storeTableRecord);
		tableRecordId = storeTableRecord.getTableRecordId();
		// 子订单
		List <StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(), false);		
		if (subStoreOrderList.isEmpty()) {
			return true;
		}
		// 总共待出餐数量
		int totalStoreMealTakeupNum = 0; 
		for (StoreOrder subStoreOrder : subStoreOrderList) {
			// 待出餐数量
			int storeMealTakeupNum = storeMealTakeupDAO.countStoreMealTakeupsByOrderId(merchantId, storeId, subStoreOrder.getOrderId(), false);
			// 待出餐数量累加
			totalStoreMealTakeupNum += storeMealTakeupNum;
			if (totalStoreMealTakeupNum > 0) {
				break;
			}
		}
		if (totalStoreMealTakeupNum == 0) {
			return true;
		} else {
			throw new T5weiException(StoreTableErrorCodeEnum.UN_ALL_TAKE_OUT.getValue(), "meal take up num more than 0");
		}
	}
	
	public Map<Long, AreaAndTableRecordUpdateTimeDTO> getTableRecordUpdateTime (int merchantId, long storeId) {
		Map<Long, AreaAndTableRecordUpdateTimeDTO> result = new HashMap<Long, AreaAndTableRecordUpdateTimeDTO>();
		// 查询区域
		List<StoreArea> storeAreas = storeAreaDAO.getStoreAreasOrderByUpdateTime(storeId, true);
		if (storeAreas == null || storeAreas.isEmpty()) {
			return result;
		} else {
			List<Map<String, Object>> realTableUpdateTimeOfAreaList = storeTableDAO.getStoreTablesLastUpdateTimeGroupByAreaId(merchantId, storeId, true);
			Map<Long, Long> realTableUpdateTimeOfAreaMap = new HashMap<Long, Long>();
			if (realTableUpdateTimeOfAreaList != null && !realTableUpdateTimeOfAreaList.isEmpty()) {
				for (Map<String, Object> map : realTableUpdateTimeOfAreaList) {
					MapObject mapObject = new MapObject(map);
					long areaId = mapObject.getLLong("area_id");
					long maxUpdateTime = mapObject.getLLong("max_update_time");
					realTableUpdateTimeOfAreaMap.put(areaId, maxUpdateTime);
				}
			}
			List<StoreTableRecord> storeTableRecordList = storeTableRecordDAO.getStoreTableRecordList4HeartBeat(merchantId, storeId, 0, 0);
			for (StoreArea storeArea : storeAreas) {
				AreaAndTableRecordUpdateTimeDTO areaAndTableRecordUpdateTimeDTO = new AreaAndTableRecordUpdateTimeDTO();
				long areaId = storeArea.getAreaId();
				areaAndTableRecordUpdateTimeDTO.setAreaId(areaId);
				areaAndTableRecordUpdateTimeDTO.setStoreAreaUpdateTime(storeArea.getUpdateTime());
				long realTableUpdateTime = 0L;
				if (realTableUpdateTimeOfAreaMap.containsKey(areaId)) {
					realTableUpdateTime = realTableUpdateTimeOfAreaMap.get(areaId);
				} 
				areaAndTableRecordUpdateTimeDTO.setRealTableUpdateTime(realTableUpdateTime);
				areaAndTableRecordUpdateTimeDTO.setTableRecordUpdateTimes(new HashMap<Long, Long>());
				result.put(areaId, areaAndTableRecordUpdateTimeDTO);
			}
			for (StoreTableRecord storeTableRecord : storeTableRecordList) {
				long areaId = storeTableRecord.getAreaId();
				if (!result.containsKey(areaId)) {
					continue;
				}
				AreaAndTableRecordUpdateTimeDTO areaAndTableRecordUpdateTimeDTO = result.get(areaId);
				areaAndTableRecordUpdateTimeDTO.getTableRecordUpdateTimes().put(storeTableRecord.getTableRecordId(), storeTableRecord.getUpdateTime());
			}
		}
		return result;
	}
	
	/**
	 * 心跳查询桌台记录的最后更新时间
	 * @param merchantId
	 * @param storeId
	 * @param areaId
	 * @param tableRecordId
	 * @return
	 * @throws TException 
	 */
	public AreaAndTableRecordUpdateTimeDTO getTableRecordUpdateTime (int merchantId, long storeId, long areaId, long tableRecordId) throws TException {
		AreaAndTableRecordUpdateTimeDTO areaAndTableRecordUpdateTimeDTO = new AreaAndTableRecordUpdateTimeDTO();
		areaAndTableRecordUpdateTimeDTO.setAreaId(areaId);
		// 查询区域
		List<StoreArea> storeAreas = storeAreaDAO.getStoreAreasOrderByUpdateTime(storeId, true);
		if (storeAreas == null || storeAreas.isEmpty()) {
			areaAndTableRecordUpdateTimeDTO.setStoreAreaUpdateTime(0L);
		} else {
			Set<Long> storeAreaIds = new HashSet<Long>();
			for (StoreArea storeArea : storeAreas) {
				long storeAreaId = storeArea.getAreaId();
				storeAreaIds.add(storeAreaId);
			}
			if (!storeAreaIds.contains(areaId)) {
				throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "areaId["+areaId+"] invalid");
			}
			StoreArea storeArea = storeAreas.get(0);
			areaAndTableRecordUpdateTimeDTO.setStoreAreaUpdateTime(storeArea.getUpdateTime());
		}
		// 查询物理桌台
		List<StoreTable> storeTables = storeTableDAO.getStoreAreaTablesOrderByUpdateTime(merchantId, storeId, areaId, true);
		if (storeTables == null || storeTables.isEmpty()) {
			areaAndTableRecordUpdateTimeDTO.setRealTableUpdateTime(0L);
		} else {
			StoreTable storeTable = storeTables.get(0);
			areaAndTableRecordUpdateTimeDTO.setRealTableUpdateTime(storeTable.getUpdateTime());
		}
		// 查询桌台记录
		Map<Long, Long> tableRecordUpdateTimes = new HashMap<Long, Long>();
		List<StoreTableRecord> storeTableRecords = storeTableRecordDAO.getStoreTableRecordList4HeartBeat(merchantId, storeId, areaId, tableRecordId);
		StoreTableRecord storeTableRecord = storeTableRecordDAO.getLastClearTimeClearTableRecord(merchantId, storeId);
		long lastClearTime = 0L;
		if (storeTableRecord != null) {
			lastClearTime = storeTableRecord.getClearTableTime();
		}
		if (storeTableRecords == null || storeTableRecords.isEmpty()) {
			areaAndTableRecordUpdateTimeDTO.setLastTableRecordUpdateTime(lastClearTime);
		} else {
			for (StoreTableRecord str : storeTableRecords) {
				tableRecordUpdateTimes.put(str.getTableRecordId(), str.getUpdateTime());
			}
			long lastTableRecordUpdateTime = storeTableRecords.get(0).getUpdateTime();
			if (lastTableRecordUpdateTime > lastClearTime) {
				areaAndTableRecordUpdateTimeDTO.setLastTableRecordUpdateTime(lastTableRecordUpdateTime);
			} else {
				areaAndTableRecordUpdateTimeDTO.setLastTableRecordUpdateTime(lastClearTime);
			}
		}
		areaAndTableRecordUpdateTimeDTO.setTableRecordUpdateTimes(tableRecordUpdateTimes);
		return areaAndTableRecordUpdateTimeDTO;
	}
	
	/**
	 * 计算桌台各项金额 退菜调用
	 * @param storeTableRecord
	 * @param totalRequestRefundAmount
	 * @return
	 * @throws TException 
	 */
	public TableRecordAmountsResult calculateTableRecordAmounts (StoreTableRecord storeTableRecord, long totalRequestRefundAmount) throws TException {
		int merchantId = storeTableRecord.getMerchantId();
		long storeId = storeTableRecord.getStoreId();
		TableRecordPayStatusResult tableRecordPayStatusInfo = this.getTableRecordPayStatusInfo(merchantId, storeId, storeTableRecord, false);
		return this.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, totalRequestRefundAmount);
	}
	
	/**
	 * 计算桌台各项金额
	 * @param tableRecordPayStatusInfo
	 * @param storeTableRecord
	 * @param totalRequestRefundAmount 请求退菜金额，只用于请求退菜时，计算桌台记录的最大可退金额，其他时候传0
	 * @throws T5weiException 
	 */
	public TableRecordAmountsResult calculateTableRecordAmounts (TableRecordPayStatusResult tableRecordPayStatusInfo, StoreTableRecord storeTableRecord, long totalRequestRefundAmount) throws T5weiException {
		// 计算桌台实际应收台位费
		StoreOrderPlaceParam storeOrderPlaceParam = new StoreOrderPlaceParam();
		storeOrderPlaceParam.setMerchantId(storeTableRecord.getMerchantId());
		storeOrderPlaceParam.setStoreId(storeTableRecord.getStoreId());
		storeOrderPlaceParam.setTimeBucketId(storeTableRecord.getTimeBucketId());
		storeOrderPlaceParam.setTakeMode(StoreOrderTakeModeEnum.DINE_IN.getValue());
		storeOrderPlaceParam.setEnableAddDishes(false);
        long reductionTableFee = storeTableRecord.getReductionTableFee();
		long payAbleTableFee = this.getTableFee(storeOrderPlaceParam, storeTableRecord.getCustomerTraffic());
		if (storeTableRecord.isClearTable() || storeTableRecord.isSettleMent()) {
			payAbleTableFee = storeTableRecord.getPayAbleTableFee();
        }
		
		// 合计 = 子订单原价-子订单台位费+桌台记录台位费-退菜金额
		long totalAmount = tableRecordPayStatusInfo.getSubOrderPriceTotalAmount() + 
				tableRecordPayStatusInfo.getSubOrderDeliveryFeeTotalAmount() - 
					tableRecordPayStatusInfo.getTableFee() + 
						payAbleTableFee -
							tableRecordPayStatusInfo.getRefundChargeItemPriceTotalAmount() -
								totalRequestRefundAmount - reductionTableFee;
		// 整单折扣
		double discountPro = storeTableRecord.getDiscountPro();
		// 整单减免
		long discountAmount = storeTableRecord.getDiscountAmount();
		//long discountAmount = storeTableRecord.getDiscountAmount() + storeTableRecord.getStaffDerate();
		// 计算整单折扣和整单减免的基数（所有子订单的）
		long tableRecordRadix = tableRecordPayStatusInfo.getSubOrderFavorablePriceTotalAmount() +
			tableRecordPayStatusInfo.getSubOrderTotalDerateTotalAmount() +
				tableRecordPayStatusInfo.getSubOrderTotalRebatePriceTotalAmount();

		// 后付费折扣活动，腾龙说计算折扣金额得改
		// 最新的桌台应付金额需要减去两次折扣，首先减去所有已付订单除整单折扣和整单减免外所有折扣，再减去收银员设置的整单减免和整单折扣
		long alreadyDiscountAmount = tableRecordPayStatusInfo.getSubOrderPriceTotalAmount() - 
				tableRecordRadix;
		
		// 整单折扣折合成金额
		long discountProAmount = MoneyUtil.mul((totalAmount - alreadyDiscountAmount), storeTableRecordHelper.div((100-discountPro), 100, 4));
		// 已付
		long paidAmount = tableRecordPayStatusInfo.getSubOrderActualPricePayAmount() +
				tableRecordPayStatusInfo.getMasterActualPricePayAmount();
		// 应付 = 合计 * 整单折扣 - 整单减免
		long payAbleAmount = totalAmount - discountProAmount - discountAmount - alreadyDiscountAmount;
		// 等待结账金额（大于0表示还需要支付；小于0表示需要退款；等于0表示已经付清 ）=应付-(已付-退款)
		long waitSettleAmount = payAbleAmount-(paidAmount-tableRecordPayStatusInfo.getTableRecordSettleRefundAmount());
		// 桌台各项金额返回结果
		TableRecordAmountsResult tableRecordAmounts = new TableRecordAmountsResult();
		tableRecordAmounts.setTotalAmount(totalAmount);
		tableRecordAmounts.setDiscountPro(discountPro);
		tableRecordAmounts.setDiscountAmount(discountAmount);
		tableRecordAmounts.setAlreadyDiscountAmount(alreadyDiscountAmount);
		tableRecordAmounts.setPaidAmount(paidAmount);
		tableRecordAmounts.setPayAbleAmount(payAbleAmount);
		tableRecordAmounts.setWaitSettleAmount(waitSettleAmount);
		tableRecordAmounts.setDiscountProAmount(discountProAmount);
		return tableRecordAmounts;
	}
	
	/**
	 * 计算折扣和应付（目前仅提供给ios使用）
	 * @param price
	 * @param discountPro
	 * @param discountAmount
	 * @return
	 */
	public Map<String, Long> caculateDiscountAmount (long price, double discountPro, long discountAmount) {
		// 整单折扣折合成金额
		long discountProAmount = MoneyUtil.mul(price, storeTableRecordHelper.div((100-discountPro), 100, 4));
		// 应付 = 合计 - 整单折扣 - 整单减免 
		long payAbleAmount = price - discountProAmount - discountAmount;
		// 封装返回结果
		Map<String, Long> result = new HashMap<String, Long>();
		result.put("price", price);
		result.put("discount_pro_amount", discountProAmount);
		result.put("discount_amount", discountAmount);
		result.put("pay_able_amount", payAbleAmount);
		return result;
	}
	
	/**
	 * 计算桌台记录过程各项金额&支付详情
	 * @param merchantId
	 * @param storeId
	 * @param storeTableRecord
	 * @param queryPayResult
	 * @return
	 * @throws T5weiException 
	 * @throws TException 
	 */	
	public TableRecordPayStatusResult getTableRecordPayStatusInfo (int merchantId, long storeId, StoreTableRecord storeTableRecord, boolean queryPayResult)
			throws TException {
		// 主订单已支付金额
		long masterOrderPricePayAmount = 0L;
		// 退款记录
		List<RefundDetailDTO> refundResultList = new ArrayList<RefundDetailDTO>();
		// 桌台享受的各项优惠记录
		List<TableRecordPayDetailResult> payDetailResulttList = new ArrayList<TableRecordPayDetailResult>();
		// 子订单总价合计（子订单order_price之和）
		long subOrderPriceTotalAmount = 0L;
		// 子订单打包费合计（子订单package_fee之和）
		long subOrderPackageFeeTotalAmount = 0L;
		// 子订单外送费合计（子订单delivery_fee之和）
		long subOrderDeliveryFeeTotalAmount = 0L;
		// 已支付子订单
		long subOrderPricePayAmount = 0L;
		// 已支付子订单打包费合计（已支付子订单package_fee之和）
		long subOrderPackageFeePayAmount = 0L;
		// 已支付子订单外送费合计（已支付子订单delivery_fee之和）
		long subOrderDeliveryFeePayAmount = 0L;
		// 子订单实付合计（子订单actual_price之和）
		long subOrderActualPriceTotalAmount = 0L;
		// 子订单已付金额（所有支付完成子订单actual_price之和）
		long subOrderActualPricePayAmount = 0L;
		// 退菜金额合计
		long refundChargeItemPriceTotalAmount = 0L;
		// 主订单订单金额合计
		long masterOrderPriceTotalAmount = 0L;
		// 主订单实际支付金额合计
		long masterActualPriceTotalAmount = 0L;
		// 已支付主订单实付金额
		long masterActualPricePayAmount = 0L;
		// 桌台记录已退款总金额
		long tableRecordRefundAmount = 0L;
		// 已收取台位费
		long paidTableFee = 0L;
		// 子订单中已经计算在orderPrice中的台位费
		long tableFee = 0L;
		// 子订单折后价（享受完各种折扣之后的金额，不含外送费）合计
		long subOrderFavorablePriceTotalAmount = 0L;
		// 子订单整单减免金额合计
		long subOrderTotalDerateTotalAmount = 0L;
		// 子订单整单折扣打折额度合计
		long subOrderTotalRebatePriceTotalAmount = 0L;
		
		// 查询storeTableRecord关联的主订单
		StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), false);
		if (masterStoreOrder != null) {
			masterOrderPriceTotalAmount = masterStoreOrder.getOrderPrice();
			masterActualPriceTotalAmount = masterStoreOrder.getFavorablePrice();
			// 计算主订单已支付金额
			if (masterStoreOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
				// 已支付主订单订单金额
				masterOrderPricePayAmount = masterStoreOrder.getOrderPrice();
				// 主订单已支付金额
				masterActualPricePayAmount = masterStoreOrder.getFavorablePrice();
				if (masterStoreOrder.getRefundStatus() == StoreOrderRefundStatusEnum.USER_ALL.getValue() || masterStoreOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() || masterStoreOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_PART.getValue()) {
					RefundDetailDTO refundDetailDTO = null;
					try {
						refundDetailDTO = refundFacade.getRefundDetailByPayOrderId(masterStoreOrder.getPayOrderId());
					} catch (Exception e) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), e.getLocalizedMessage());
					}
					if (refundDetailDTO == null) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"], masterOrder[payOrderId="+masterStoreOrder.getPayOrderId()+"], get refundDetailDTO result is null");
					}
					tableRecordRefundAmount += refundDetailDTO.getAmountRefund();
					refundResultList.add(refundDetailDTO);
				}
			} else {
				if (masterStoreOrder.getCreditStatus() > 0) {
					// 已支付主订单订单金额
					masterOrderPricePayAmount = masterStoreOrder.getOrderPrice();
					// 主订单已支付金额
					masterActualPricePayAmount = masterStoreOrder.getFavorablePrice();
				}
			}
			if (queryPayResult) {
				PayResultOfPayOrder masterOrderPayResult = null;
				String payOrderId = masterStoreOrder.getPayOrderId();
				if (masterStoreOrder.isPayFinish() && !StringUtils.isNullOrEmpty(payOrderId)) {
					try {
						masterOrderPayResult = payFacade.getPayResultOfPayOrder(payOrderId);
					} catch (Exception e) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "payOrderId["+payOrderId+"] get payResult error");
					} 
					if (masterOrderPayResult == null) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "payOrderId["+payOrderId+"] get payResult error");
					}
				}
				TableRecordPayDetailResult tableRecordFavourableAmountsResult = this.calculateFavourableAmounts(masterStoreOrder, masterOrderPayResult);
				payDetailResulttList.add(tableRecordFavourableAmountsResult);
			}
		}
		// 查询storeTableRecord关联的子订单
		List <StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(), false);
		if (subStoreOrderList != null && !subStoreOrderList.isEmpty()) {
			// 遍历子订单列表，计算桌台记录各项金额
			for (StoreOrder subStoreOrder : subStoreOrderList) {
				// 桌台后付费不允许赊账
				if (subStoreOrder.getCreditStatus() != StoreOrderCreditStatusEnum.NO_CREDIT.getValue()) {
					throw new T5weiException(StoreTableErrorCodeEnum.SUB_ORDER_CAN_NOT_CREDIT.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"] subOrder["+subStoreOrder.getOrderId()+"] can not credit");
				}
				// 计算原价
				subOrderPriceTotalAmount += subStoreOrder.getOrderPrice();
				// 计算打包费
				subOrderPackageFeeTotalAmount += subStoreOrder.getPackageFee();
				// 计算外送费
				subOrderDeliveryFeeTotalAmount += subStoreOrder.getDeliveryFee();
				// 计算实付
				subOrderActualPriceTotalAmount += subStoreOrder.getFavorablePrice();
				// 计算子订单已经包含在orderPrice中的台位费
				tableFee += subStoreOrder.getTableFee();
				
				subOrderFavorablePriceTotalAmount += subStoreOrder.getFavorablePrice();
				subOrderTotalDerateTotalAmount += subStoreOrder.getTotalDerate();
				subOrderTotalRebatePriceTotalAmount += subStoreOrder.getTotalRebatePrice();
				
				// 已经支付的订单需要查询支付详情给前端组装结果进行显示
				if (subStoreOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
					// 已支付子订单原价之和
					subOrderPricePayAmount += subStoreOrder.getOrderPrice();
					// 已支付子订单打包费合计
					subOrderPackageFeePayAmount += subStoreOrder.getPackageFee();
					// 已支付子订单外送费合计
					subOrderDeliveryFeePayAmount += subStoreOrder.getDeliveryFee();
					// 已支付子订单实付合计
					subOrderActualPricePayAmount += subStoreOrder.getFavorablePrice();
					// 计算桌台已经支付过的台位费
					paidTableFee += subStoreOrder.getTableFee();
					if (subStoreOrder.getRefundStatus() == StoreOrderRefundStatusEnum.USER_ALL.getValue() ||
							subStoreOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() ||
									subStoreOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_PART.getValue()) {
						RefundDetailDTO refundDetailDTO = null;
						try {
							refundDetailDTO = refundFacade.getRefundDetailByPayOrderId(subStoreOrder.getPayOrderId());
						} catch (Exception e) {
							throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), e.getLocalizedMessage());
						}
						if (refundDetailDTO == null) {
							throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"], subStoreOrder[payOrderId="+subStoreOrder.getPayOrderId()+"], get refundDetailDTO result is null");
						}
						tableRecordRefundAmount += refundDetailDTO.getAmountRefund();
						refundResultList.add(refundDetailDTO);
					}
				}
				if (queryPayResult) {
					String payOrderId = subStoreOrder.getPayOrderId();
					PayResultOfPayOrder subPayResult = null;
					if (subStoreOrder.isPayFinish() && !StringUtils.isNullOrEmpty(payOrderId)) {
						//throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeOrder["+subStoreOrder.getOrderId()+"] payStatus finish, payOrderId is null or empty");
						try {
							subPayResult = payFacade.getPayResultOfPayOrder(payOrderId);
						} catch (Exception e) {
							throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "payOrderId["+payOrderId+"] get payResult error");
						} 
					}
					/*if (subPayResult == null) {
						throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "payOrderId["+payOrderId+"] get payResult error");
					}*/
					TableRecordPayDetailResult tableRecordFavourableAmountsResult = this.calculateFavourableAmounts(subStoreOrder, subPayResult);
					payDetailResulttList.add(tableRecordFavourableAmountsResult);
				}
			}
		}
		// 查询storeTableRecord关联的所有退菜记录
		List<StoreTableRecordRefund> storeTableRecordRefundList = 
				storeTableRecordRefundDAO.getStoreTableRecordRefundsByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId());
		// 循环退菜记录，计算退菜金额
		for (StoreTableRecordRefund storeTableRecordRefund : storeTableRecordRefundList) {
			refundChargeItemPriceTotalAmount += storeTableRecordRefund.getRefundPrice();
		}
		
		long tableRecordSettleRefundAmount = 0L;
		long tableRecordUnSettleRefundAmount = 0L;
		// 获取桌台批量退款记录
		List<TableRecordBatchRefundRecord> tableRecordBatchRefundRecords = tableRecordBatchRefundRecordDAO.getSuccessTableRecordBatchRefundByTableRecordId(storeTableRecord.getTableRecordId());
		for (TableRecordBatchRefundRecord tableRecordBatchRefundRecord : tableRecordBatchRefundRecords) {
			if (tableRecordBatchRefundRecord.getType() == SETTLEREFUND) {
				tableRecordSettleRefundAmount += tableRecordBatchRefundRecord.getRefundAmount();
			} else {
				tableRecordUnSettleRefundAmount += tableRecordBatchRefundRecord.getRefundAmount();
			}
		}
		
		// 封装并返回计算结果
		TableRecordPayStatusResult tableRecordPayStatusInfo = new TableRecordPayStatusResult();
		tableRecordPayStatusInfo.setSubOrderPriceTotalAmount(subOrderPriceTotalAmount);
		tableRecordPayStatusInfo.setSubOrderPricePayAmount(subOrderPricePayAmount);
		tableRecordPayStatusInfo.setSubOrderPackageFeeTotalAmount(subOrderPackageFeeTotalAmount);
		tableRecordPayStatusInfo.setSubOrderPackageFeePayAmount(subOrderPackageFeePayAmount);
		tableRecordPayStatusInfo.setSubOrderDeliveryFeeTotalAmount(subOrderDeliveryFeeTotalAmount);
		tableRecordPayStatusInfo.setSubOrderDeliveryFeePayAmount(subOrderDeliveryFeePayAmount);
		tableRecordPayStatusInfo.setSubOrderActualPriceTotalAmount(subOrderActualPriceTotalAmount);
		tableRecordPayStatusInfo.setSubOrderActualPricePayAmount(subOrderActualPricePayAmount);
		tableRecordPayStatusInfo.setRefundChargeItemPriceTotalAmount(refundChargeItemPriceTotalAmount);
		tableRecordPayStatusInfo.setMasterOrderPriceTotalAmount(masterOrderPriceTotalAmount);
		tableRecordPayStatusInfo.setMasterOrderPricePayAmount(masterOrderPricePayAmount);
		tableRecordPayStatusInfo.setMasterActualPriceTotalAmount(masterActualPriceTotalAmount);
		tableRecordPayStatusInfo.setMasterActualPricePayAmount(masterActualPricePayAmount);
		tableRecordPayStatusInfo.setRefundResultList(refundResultList);
		tableRecordPayStatusInfo.setPayDetailResulttList(payDetailResulttList);
		tableRecordPayStatusInfo.setPaidTableFee(paidTableFee);
		tableRecordPayStatusInfo.setTableFee(tableFee);
		tableRecordPayStatusInfo.setTableRecordSettleRefundAmount(tableRecordSettleRefundAmount);
		tableRecordPayStatusInfo.setTableRecordUnSettleRefundAmount(tableRecordUnSettleRefundAmount);
		tableRecordPayStatusInfo.setSubOrderFavorablePriceTotalAmount(subOrderFavorablePriceTotalAmount);
		tableRecordPayStatusInfo.setSubOrderTotalDerateTotalAmount(subOrderTotalDerateTotalAmount);
		tableRecordPayStatusInfo.setSubOrderTotalRebatePriceTotalAmount(subOrderTotalRebatePriceTotalAmount);
		return tableRecordPayStatusInfo;
	}
	
	public Map<Long,StoreTableRecord> getStoreTableRecordMapByIds (int merchantId, long storeId, List<Long> tableRecordIds, boolean enableSlave) {
	    return this.storeTableRecordDAO.getStoreTableRecordMapByIds(merchantId, storeId, tableRecordIds, enableSlave);
	}
	
	/**
	 * 获取桌台记录的支付状态
	 * @param merchantId
	 * @param storeId
	 * @param storeTableRecord
	 * @param tableRecordPayStatusInfo
	 * @param tableRecordAmountsResult
	 * @return
	 * @throws T5weiException
	 */
	private PayStatusEnum getPayStatus (int merchantId, long storeId, StoreTableRecord storeTableRecord, TableRecordPayStatusResult tableRecordPayStatusInfo, TableRecordAmountsResult tableRecordAmountsResult) throws T5weiException {
		long waitSettleAmount = tableRecordAmountsResult.getWaitSettleAmount();
		long paidAmount = tableRecordAmountsResult.getPaidAmount();
		// 如果桌台记录状态已经是结账或清台，等待结账金额仍然不为0，说明结账出问题，抛异常
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
			if (waitSettleAmount != 0) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"] tableStatus["+TableRecordStatusEnum.findByValue(storeTableRecord.getTableRecordStatus())+"], waitSettleAmount["+waitSettleAmount+"] must be 0");
			}
		}
		// 如果等待退款金额比已付金额还大，报错
		if (waitSettleAmount + paidAmount < 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.WAIT_REFUND_AMOUNT_CAN_NOT_MORE_THAN_PAID_AMOUNT.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"] waitSettleAmount["+waitSettleAmount+"]+paidAmount["+paidAmount+"] can not more than 0");
		}
		if (waitSettleAmount == 0L) { // 全部支付完成
			if (paidAmount > 0) {
				return PayStatusEnum.ALL_PAY;
			} else {
				return PayStatusEnum.UN_PAY;
			}
		} else if (waitSettleAmount > 0L) { // 尚未支付完成,需要判断是否支付过
			if (paidAmount > 0) {
				return PayStatusEnum.PART_PAY;
			} else {
				return PayStatusEnum.UN_PAY;
			}
		} else { // 等待结账金额小于0表示需要退款
			return PayStatusEnum.REFUND;
		}
	}
	
	/**
	 * 获取桌台记录的支付状态
	 * @param merchantId
	 * @param storeId
	 * @param storeTableRecord
	 * @return
	 * @throws TException 
	 */
	private PayStatusEnum getPayStatus (int merchantId, long storeId, StoreTableRecord storeTableRecord) throws TException {
		// 获取桌台记录支付状态详情
		TableRecordPayStatusResult tableRecordPayStatusInfo = this.getTableRecordPayStatusInfo(merchantId, storeId, storeTableRecord, false);
		TableRecordAmountsResult tableRecordAmountsResult = this.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, 0L);
		long waitSettleAmount = tableRecordAmountsResult.getWaitSettleAmount();
		long paidAmount = tableRecordAmountsResult.getPaidAmount();
		// 如果桌台记录状态已经是结账或清台，等待结账金额仍然不为0，说明结账出问题，抛异常
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
			if (waitSettleAmount != 0) {
				throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"] tableStatus["+TableRecordStatusEnum.findByValue(storeTableRecord.getTableRecordStatus())+"], waitSettleAmount["+waitSettleAmount+"] must be 0");
			}
		}
		// 如果等待退款金额比已付金额还大，报错
		if (waitSettleAmount + paidAmount < 0) {
			throw new T5weiException(StoreTableErrorCodeEnum.WAIT_REFUND_AMOUNT_CAN_NOT_MORE_THAN_PAID_AMOUNT.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"] waitSettleAmount["+waitSettleAmount+"]+paidAmount["+paidAmount+"] can not more than 0");
		}
		// 根据waitSettleAmount和paidAmount返回桌台记录的支付状态
		if (waitSettleAmount == 0L) {
			// 全部支付完成
			return PayStatusEnum.ALL_PAY;
		} else if (waitSettleAmount > 0L) {
			// 尚未支付完成,需要判断是否支付过
			if (paidAmount > 0) {
				return PayStatusEnum.PART_PAY;
			} else {
				return PayStatusEnum.UN_PAY;
			}
		} else {
			// 等待结账金额小于0表示需要退款
			return PayStatusEnum.REFUND;
		}
	}
	
	/**
	 * 处理具体的开台逻辑
	 * <br/> merchantId 商编
	 * <br/> storeId 店铺id
	 * <br/> repastDate 就餐日期
	 * <br/> timeBucketId 营业时段
	 * <br/> tableId 桌台id
	 * <br/> customerTraffic 入客数
	 * <br/> staffId 开台员工id
	 * <br/> userId 自助开台用户id
	 * <br/> orderList 待绑定桌台订单列表
	 * @return StoreTableRecord 桌台记录
	 * @throws TException
	 */
	private OpenTableRecordResult openTableRecord (OpenTableRecordRequestParam openTableRecordRequestParam) throws TException {
		int merchantId = openTableRecordRequestParam.getMerchantId();
		long storeId = openTableRecordRequestParam.getStoreId();
		long repastDate = openTableRecordRequestParam.getRepastDate();
		long timeBucketId = openTableRecordRequestParam.getTimeBucketId();
		long tableId = openTableRecordRequestParam.getTableId();
		int customerTraffic = openTableRecordRequestParam.getCustomerTraffic();
		long staffId = openTableRecordRequestParam.getCreateTableRecordStaffId();
		long userId = openTableRecordRequestParam.getUserId();
		List<StoreOrder> orderList = openTableRecordRequestParam.getOrderList();
		int clientType = openTableRecordRequestParam.getClientType();
		long defaultStaffId = openTableRecordRequestParam.getDefaultStaffId();
		OpenTableRecordResult openTableRecordResult = new OpenTableRecordResult();
		List<StoreMealTakeup> storeMealTakeups = new ArrayList<>();
		// 获取店铺桌台设置
		StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
		// 非桌台模式不允许开台
		if (!storeTableSetting.isEnableTableMode()) {
			log.warn("un-tableMode merchantId["+merchantId+"], storeId["+storeId+"] can not create tableRecord");
			throw new T5weiException(StoreTableErrorCodeEnum.UN_ABLE_TABLE_MODE.getValue(), "un-tableMode merchantId["+merchantId+"], storeId["+storeId+"] can not create tableRecord");
		} 
		// 获取桌台信息
		StoreTable storeTable = storeTableRecordHelper.getStoreTable(merchantId, storeId, tableId);
		// 查找桌台id下是否还挂有其他桌台记录
		List<StoreTableRecord> storeTableRecordList = storeTableRecordDAO.getStoreTableRecordListByTableIdSeqDesc (merchantId, storeId, tableId, false);
		// 拼桌序号
		int tableRecordSeq = 0;
		// 如果table下的桌台记录非空，说明是拼桌
		if (!storeTableRecordList.isEmpty()) {
			// 查询桌台记录列表中最大的拼桌序号
			int minTableRecordSeq = storeTableRecordList.get(storeTableRecordList.size()-1).getTableRecordSeq();
			if (minTableRecordSeq == 0) {
				int maxTableRecordSeq = storeTableRecordList.get(0).getTableRecordSeq();
				tableRecordSeq = maxTableRecordSeq + 1;
			}
		}
		// 创建桌台记录
		CreateStoreTableRecordParam createStoreTableRecordParam = new CreateStoreTableRecordParam();
		createStoreTableRecordParam.setMerchantId(merchantId);
		createStoreTableRecordParam.setStoreId(storeId);
		createStoreTableRecordParam.setRepastDate(repastDate);
		createStoreTableRecordParam.setTimeBucketId(timeBucketId);
		createStoreTableRecordParam.setStoreTable(storeTable);
		createStoreTableRecordParam.setTableRecordSeq(tableRecordSeq);
		createStoreTableRecordParam.setCustomerTraffic(customerTraffic);
		createStoreTableRecordParam.setCreateTableRecordStaffId(staffId);
		createStoreTableRecordParam.setCreateTableRecordUserId(userId);
		createStoreTableRecordParam.setClientType(clientType);
		createStoreTableRecordParam.setDefaultStaffId(defaultStaffId);
		StoreTableRecord storeTableRecord = storeTableRecordHelper.createStoreTableRecord(createStoreTableRecordParam);
		storeTableRecordDAO.create(storeTableRecord);
		//记录开台日志
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId,
				storeTableRecord.getTableRecordId(), staffId, userId, storeTableRecord.getClientType(),
				TableRecordOptlogTypeEnum.TABLE_OPEN.getValue(), "开台操作", storeTableRecord.getCreateTime());
		// 循环遍历子订单列表，将订单与桌台记录进行关联
		List<StoreOrder> storeOrders = new ArrayList<StoreOrder>();
		List<String> orderIds = new ArrayList<>();
		if (orderList != null && !orderList.isEmpty()) {
			String masterOrderId = null; // 主订单id
			StoreOrder masterOrder = null; // 主订单
			for (StoreOrder storeOrder : orderList) {
				orderIds.add(storeOrder.getOrderId());
				// 校验订单取餐方式（绑定桌台的订单不允许是外送和快取模式）
				storeTableRecordFacadeValidate.checkSubOrderTakeMode(storeOrder);
				// 已经关联过桌台记录的订单不允许关联其他桌台记录
				storeTableRecordFacadeValidate.checkSubOrderTableRecordId(storeOrder);
				// 订单状态校验:桌台记录在结账前，子订单不允许赊账和退款
				storeTableRecordFacadeValidate.checkSubOrderStatus(storeTableRecord, storeOrder);
			}
			for (StoreOrder storeOrder : orderList) {
				// 桌台设置是否为后付费模式
				if (!storeTableSetting.isEnablePayAfter()) {
					// 如果不支持后付费，要判断订单支付状态
					if (!storeOrder.isPayFinish()) {
						// 如果尚未支付成功，抛异常
						throw new T5weiException(StoreTableErrorCodeEnum.PAY_BEFORE_CAN_NOT_UNPAY.getValue(), "store["+storeId+"] not support payAfter mode, storeOrder["+storeOrder.getOrderId()+"] payStatus must be payFinish");
					}
					storeOrder.setPayAfter(false);
				} else {
					storeOrder.setPayAfter(true);
				}
				// 获取主订单
				masterOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), false);
				if (masterOrder == null) { 
					// 主订单不存在则创建
					masterOrder = storeOrderService.createMasterStoreOrder(storeOrder, storeTableRecord.getTableRecordId());
					masterOrder.setCustomerTraffic(customerTraffic);
					masterOrder.setUpdateTime(System.currentTimeMillis());
					masterOrder.update();
					storeTableRecord.setTakeSerialNumber(masterOrder.getTakeSerialNumber());
					storeTableRecord.setOrderTime(System.currentTimeMillis());
					//记录开台日志
					storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId,
							storeTableRecord.getTableRecordId(), staffId, userId, storeOrder.getClientType(),//主订单的clientType和子订单的一致
							TableRecordOptlogTypeEnum.TABLE_MASTER_CREATE.getValue(), "桌台记录的主订单创建", masterOrder.getCreateTime());
				} 
				masterOrderId = masterOrder.getOrderId();
				List<StoreOrder> subOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), masterOrderId, false);
				if (subOrderList != null && !subOrderList.isEmpty()) {
					if (subOrderList.get(0).getOrderId().equals(storeOrder.getOrderId())) {
						storeOrder.setEnableAddDishes(false);
					} else {
						storeOrder.setEnableAddDishes(true);
					}
				} else {
					storeOrder.setEnableAddDishes(false);
				}
				// 子订单绑定桌台记录
				storeOrder.setParentOrderId(masterOrderId);
				storeOrder.setTableRecordId(storeTableRecord.getTableRecordId());
				storeOrder.setUpdateTime(System.currentTimeMillis());
				// 将订单设置为手动设置入客数
				storeOrder.setEnableManualCustomerTraffic(true);
				storeOrder.update();
				// 如果是桌台后付费，绑定桌台后立即执行取餐&改变库存
				if (storeOrder.getTakeSerialNumber() == 0) { // takeSerialNumber>0认为是已取餐，跳过取餐环节
					StoreOrderTakeCodeParam storeOrderTakeCodeParam = new StoreOrderTakeCodeParam();
					storeOrderTakeCodeParam.setMerchantId(merchantId);
					storeOrderTakeCodeParam.setStoreId(storeId);
					storeOrderTakeCodeParam.setOrderId(storeOrder.getOrderId());
					storeOrderTakeCodeParam.setClientType(ClientTypeEnum.CASHIER.getValue());
					storeOrderTakeCodeParam.setTakeMode(StoreOrderTakeModeEnum.findByValue(storeOrder.getTakeMode()));
					storeOrder = storeOrderService.takeCodeStoreOrder(storeOrderTakeCodeParam);
					storeOrders.add(storeOrder);
					storeMealTakeups.addAll(storeOrder.getStoreMealTakeups());
				}
			}
			// 后厨清单
			i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,storeTableRecord.getTableRecordId(),orderIds,storeMealTakeups);
			/*// 更新桌台记录
			this.refreshTableRecord(merchantId, storeId, storeTableRecord);*/
			storeTableRecord.setOrderId(masterOrderId);
			openTableRecordResult.setMasterOrder(masterOrder);
		}
		// 更新桌台记录应付台位费
		StoreOrderPlaceParam storeOrderPlaceParam = new StoreOrderPlaceParam();
		storeOrderPlaceParam.setMerchantId(merchantId);
		storeOrderPlaceParam.setStoreId(storeId);
		storeOrderPlaceParam.setTimeBucketId(timeBucketId);
		storeOrderPlaceParam.setTakeMode(StoreOrderTakeModeEnum.DINE_IN.getValue());
		storeOrderPlaceParam.setEnableAddDishes(false);
		long payAbleTableFee = this.getTableFee(storeOrderPlaceParam, customerTraffic);
		storeTableRecord.setPayAbleTableFee(payAbleTableFee);
		// 更新桌台记录
		this.refreshTableRecord(merchantId, storeId, storeTableRecord);
		storeTableRecord.setUpdateTime(System.currentTimeMillis());
		storeTableRecord.update();
		openTableRecordResult.setStoreTableRecord(storeTableRecord);
		openTableRecordResult.setStoreOrders(storeOrders);
		return openTableRecordResult;
	}
	
	/**
	 * 更新桌台记录
	 * @param merchantId
	 * @param storeId
	 * @param storeTableRecord
	 * @return
	 * @throws TException
	 */
	private void refreshTableRecord (int merchantId, long storeId, StoreTableRecord storeTableRecord) throws TException {
		// 查询桌台状态、待出餐、已出餐数量
		TableStatusResult tableStatusResult = this.getTableRecordStatus(merchantId, storeId, storeTableRecord);
		TableRecordStatusEnum TableRecordStatusEnum = tableStatusResult.getTableRecordStatusEnum();
		storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.getValue());
		this.isAllTakeOut(tableStatusResult, storeTableRecord);
		// 查询桌台记录各项金额
		TableRecordPayStatusResult tableRecordPayStatusInfo = this.getTableRecordPayStatusInfo(merchantId, storeId, storeTableRecord, false);

		long tableRecordSettleRefundAmount = tableRecordPayStatusInfo.getTableRecordSettleRefundAmount();
		long tableRecordUnSettleRefundAmount = tableRecordPayStatusInfo.getTableRecordUnSettleRefundAmount();
		
		storeTableRecord.setTotalRefundAmount(tableRecordSettleRefundAmount + tableRecordUnSettleRefundAmount);
		storeTableRecord.setRefundAmount(tableRecordSettleRefundAmount);
		tableRecordPayStatusInfo.setTableRecordRefundAmount(tableRecordSettleRefundAmount);
		
		TableRecordAmountsResult tableRecordAmountsResult = this.calculateTableRecordAmounts(tableRecordPayStatusInfo, storeTableRecord, 0L);
		storeTableRecord.setTableRecordPayStatusInfo(tableRecordPayStatusInfo);
		storeTableRecord.setTablePrice(tableRecordPayStatusInfo.getSubOrderPriceTotalAmount()); // 桌台原价
		storeTableRecord.setTableFee(tableRecordPayStatusInfo.getTableFee());
		storeTableRecord.setAlreadyDiscountAmount(tableRecordAmountsResult.getAlreadyDiscountAmount());
		storeTableRecord.setRefundChargeItemPrice(tableRecordPayStatusInfo.getRefundChargeItemPriceTotalAmount());
		storeTableRecord.setPayAbleAmount(tableRecordAmountsResult.getPayAbleAmount());
		storeTableRecord.setPaidAmount(tableRecordAmountsResult.getPaidAmount());
		
		// 更新桌台、支付状态
		// 重新判断桌台记录的支付状态
		PayStatusEnum payStatusEnum = this.getPayStatus(merchantId, storeId, storeTableRecord, tableRecordPayStatusInfo, tableRecordAmountsResult);
		storeTableRecord.setPayStatus(payStatusEnum.getValue());
	}
	
	/**
	 * 获取桌台记录待出餐、已出餐数量以及是否菜上齐
	 * @param merchantId 商编
	 * @param storeId 店铺id
	 * @param storeTableRecord 桌台记录
	 */
	private StoreTableRecord isAllTakeOut (int merchantId, long storeId, StoreTableRecord storeTableRecord) {
		// 子订单
		List <StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(), false);
		// 总共待出餐数量
		int totalStoreMealTakeupNum = 0; 
		// 总共已出餐数量
		int totalStoreMealCheckoutNum = 0; 
		for (StoreOrder subStoreOrder : subStoreOrderList) {
			// 待出餐数量
			int storeMealTakeupNum = storeMealTakeupDAO.countStoreMealTakeupsByOrderId(merchantId, storeId, subStoreOrder.getOrderId(), false);
			// 已出餐数量
			int storeMealCheckoutNum = storeMealCheckoutDAO.countStoreMealsHistoryByOrderId(merchantId, storeId, subStoreOrder.getOrderId(), false);
			// 待出餐数量累加
			totalStoreMealTakeupNum += storeMealTakeupNum;
			// 已出餐数量累加
			totalStoreMealCheckoutNum += storeMealCheckoutNum;
		}
		storeTableRecord.setMealCheckoutNum(totalStoreMealCheckoutNum);
		storeTableRecord.setMealTakeupNum(totalStoreMealTakeupNum);
		storeTableRecord = this.setAllTakeOut(storeTableRecord, totalStoreMealTakeupNum, totalStoreMealCheckoutNum);
		return storeTableRecord;
	}
	
	/**
	 * 获取桌台记录待出餐、已出餐数量以及是否菜上齐
	 * @param tableStatusResult
	 * @param storeTableRecord
	 * @return
	 */
	private StoreTableRecord isAllTakeOut (TableStatusResult tableStatusResult, StoreTableRecord storeTableRecord) {
		int totalStoreMealTakeupNum = tableStatusResult.getTotalStoreMealTakeupNum();
		int totalStoreMealCheckoutNum = tableStatusResult.getTotalStoreMealCheckoutNum();
		storeTableRecord.setMealCheckoutNum(totalStoreMealCheckoutNum);
		storeTableRecord.setMealTakeupNum(totalStoreMealTakeupNum);
		storeTableRecord = this.setAllTakeOut(storeTableRecord, totalStoreMealTakeupNum, totalStoreMealCheckoutNum);
		return storeTableRecord;
	}
	
	/**
	 * 获取桌台记录待出餐、已出餐数量以及是否菜上齐
	 * @param storeTableRecord
	 * @param mealTakeupNum
	 * @param mealCheckoutNum
	 * @return
	 */
	private StoreTableRecord setAllTakeOut (StoreTableRecord storeTableRecord, int mealTakeupNum, int mealCheckoutNum) {
		if (mealTakeupNum == 0) {
			if (mealCheckoutNum == 0) {
				if (storeTableRecord.getOrderTime() > 0) {
					storeTableRecord.setAllTakeOut(true);
				} else {
					storeTableRecord.setAllTakeOut(false);
				}
			} else {
				storeTableRecord.setAllTakeOut(true);
			}
		} else {
			storeTableRecord.setAllTakeOut(false);
		}
		return storeTableRecord;
	}
	
	/**
	 * 获取桌台记录状态
	 * @param merchantId
	 * @param storeId
	 * @param storeTableRecord
	 * @return
	 * @throws T5weiException
	 */
	private TableStatusResult getTableRecordStatus (int merchantId, long storeId, StoreTableRecord storeTableRecord) 
			throws T5weiException {
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord can not be null");
		}
		TableStatusResult tableStatusResult = new TableStatusResult();
		// 查询主订单
		StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), false);
		if (masterStoreOrder == null) {
			// 尚未生成出订单，说明桌台记录尚未下单
			tableStatusResult.setTableRecordStatusEnum(TableRecordStatusEnum.WAIT_MEAL);
			return tableStatusResult;
		}
		// 查询子订单
		List <StoreOrder> subStoreOrderList = storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, storeTableRecord.getTableRecordId(), storeTableRecord.getOrderId(),false);
		if (subStoreOrderList.isEmpty()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord["+storeTableRecord.getTableRecordId()+"], masterOrder["+masterStoreOrder.getOrderId()+"] subStoreOrder can not be null or empty");
		}
		// 总共待出餐数量
		int totalStoreMealTakeupNum = 0; 
		// 总共已出餐数量
		int totalStoreMealCheckoutNum = 0; 
		// 遍历子订单列表，计算待出餐、已出餐数量
		for (StoreOrder subStoreOrder : subStoreOrderList) {
			// 桌台后付费不允许赊账
			if (subStoreOrder.getCreditStatus() != StoreOrderCreditStatusEnum.NO_CREDIT.getValue()) {
				throw new T5weiException(StoreTableErrorCodeEnum.SUB_ORDER_CAN_NOT_CREDIT.getValue(), "tableRecord["+storeTableRecord.getTableRecordId()+"] subOrder["+subStoreOrder.getOrderId()+"] can not refund");
			}
			// 待出餐数量
            int storeMealTakeupNum = storeMealTakeupDAO.countStoreMealTakeupsByOrderId(merchantId, storeId, subStoreOrder.getOrderId(), false);
            // 已出餐数量
            int storeMealCheckoutNum = storeMealCheckoutDAO.countStoreMealsHistoryByOrderId(merchantId, storeId, subStoreOrder.getOrderId(), false);
			// 待出餐数量累加
			totalStoreMealTakeupNum += storeMealTakeupNum;
			// 已出餐数量累加
			totalStoreMealCheckoutNum += storeMealCheckoutNum;
		}
		tableStatusResult.setTotalStoreMealCheckoutNum(totalStoreMealCheckoutNum);
		tableStatusResult.setTotalStoreMealTakeupNum(totalStoreMealTakeupNum);
		// 当前桌台记录的状态
		int tableRecordStatus = storeTableRecord.getTableRecordStatus();
		// 已结账或清台状态直接返回
		if (storeTableRecord.isSettleMent() || storeTableRecord.isClearTable()) {
			tableStatusResult.setTableRecordStatusEnum(TableRecordStatusEnum.findByValue(tableRecordStatus));
			return tableStatusResult;
		}
		// 主订单已支付或者已退款，说明桌台记录应该已经结账，不应该走到这里，报错
		if (masterStoreOrder.getPayStatus() != StoreOrderPayStatusEnum.NOT.getValue() || masterStoreOrder.getRefundStatus() != StoreOrderRefundStatusEnum.NOT.getValue()) {
			throw new T5weiException(StoreTableErrorCodeEnum.SYSTEM_ERROR.getValue(), "storeTableRecord["+storeTableRecord.getTableRecordId()+"] tableStatus["+tableRecordStatus+"] is not SETTLEMENT or CLEAR_TABLE, masterOrder can not pay or refund");
		}
		if (totalStoreMealTakeupNum > 0 && totalStoreMealCheckoutNum > 0) { 
			// 上菜中
			tableStatusResult.setTableRecordStatusEnum(TableRecordStatusEnum.SERVING);
		} else if (totalStoreMealTakeupNum > 0 && totalStoreMealCheckoutNum == 0) {
			// 等待上菜
			tableStatusResult.setTableRecordStatusEnum(TableRecordStatusEnum.WAIT_SERVE);
		} else if (totalStoreMealTakeupNum == 0 && totalStoreMealCheckoutNum == 0) {
			if (storeTableRecord.getOrderTime() > 0) {
				// 说明已经点过餐，又全部退菜
				tableStatusResult.setTableRecordStatusEnum(TableRecordStatusEnum.COMPLETE_SERVING);
			} else {
				// 等待点餐
				tableStatusResult.setTableRecordStatusEnum(TableRecordStatusEnum.WAIT_MEAL);
			}
		} else {
			// 已上齐
			tableStatusResult.setTableRecordStatusEnum(TableRecordStatusEnum.COMPLETE_SERVING);
		}
		return tableStatusResult;
	}
	
	/**
	 * 创建退菜详情
	 * @param storeTableRecord
	 * @param storeOrder
	 * @param refundOrderItemParam
	 * @param staffId
	 * @param storeOrderItem
	 * @param storeTableRecordRefund
	 * @param refundItemPrice
	 * @param refundedItemNum
	 * @param storeOrderRefundItems
	 */
	private double createRefundItemRecord (StoreTableRecord storeTableRecord, StoreOrder storeOrder, RefundOrderItemParam refundOrderItemParam, long staffId, StoreOrderItem storeOrderItem, StoreTableRecordRefund storeTableRecordRefund, long refundItemPrice, double refundedItemNum, List<StoreOrderRefundItem> storeOrderRefundItems) {
		StoreOrderRefundItem storeOrderRefundItem = tableRecordRefundHelper.createRefundItemRecord(
			storeOrder, refundOrderItemParam, storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getTableRecordId(), staffId, storeTableRecordRefund.getTableRecordRefundId(), refundItemPrice, refundedItemNum);
		if (storeOrderRefundItem != null) {
			storeOrderRefundItems.add(storeOrderRefundItem);
			// 是否恢复数据
			if (refundOrderItemParam.isRecoveryStock()) { 
				List<StoreOrderSubitem> storeOrderSubitemList = storeOrderSubitemDAO.getStoreOrderSubitemById(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeOrder.getOrderId(), storeOrderItem.getChargeItemId());
				for (StoreOrderSubitem storeOrderSubitem : storeOrderSubitemList) {
					double amount = storeOrderSubitem.getAmount();
					double refundAmount = storeOrderRefundItem.getAmount();
					BigDecimal bDAmount = new BigDecimal(String.valueOf(amount)); 
					BigDecimal bDRefundAmount = new BigDecimal(String.valueOf(refundAmount)); 
					storeOrderSubitem.setInvQuitAmount(storeOrderSubitem.getInvQuitAmount() + bDAmount.multiply(bDRefundAmount).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
					storeOrderSubitem.setUpdateTime(System.currentTimeMillis());
					storeOrderSubitemDAO.update(storeOrderSubitem);
				}
			}
			return storeOrderRefundItem.getAmount();
		}
		return 0;
	}
	
	/**
	 * 计算桌台各个订单优惠信息
	 * @param storeOrder
	 * @param payResultOfPayOrder
	 * @return TableRecordFavourableAmountsResult
	 */
	public TableRecordPayDetailResult calculateFavourableAmounts(StoreOrder storeOrder, PayResultOfPayOrder payResultOfPayOrder) {
		long orderPrice = storeOrder.getOrderPrice();//订单原价
        long tableFee = storeOrder.getTableFee();//台位费
        
        long internetRebatePrice = storeOrder.getInternetRebatePrice();// 自助下单折扣
        long enterpriseRebatePrice = storeOrder.getEnterpriseRebatePrice();// 企业折扣
        long memberRebatePrice = storeOrder.getMemberRebatePrice();//会员价折扣
        long promotionRebatePrice = storeOrder.getPromotionRebatePrice();//折扣活动减免
        long promotionReducePrice = storeOrder.getPromotionReducePrice();//满减活动减免
        long gratisPrice = storeOrder.getGratisPrice();//赠菜免单金额
        long promotionPrice = storeOrder.getPromotionPrice();//单品折扣总共优惠
        TableRecordPayDetailResult tableRecordFavourableAmountsResult = new TableRecordPayDetailResult();
		tableRecordFavourableAmountsResult.setOrderId(storeOrder.getOrderId());
		if (payResultOfPayOrder != null) {
			tableRecordFavourableAmountsResult.setPayOrderId(payResultOfPayOrder.getPayOrderId());
			tableRecordFavourableAmountsResult.setCashAmount(payResultOfPayOrder.getCashAmount());
			tableRecordFavourableAmountsResult.setCashReceivedAmount(payResultOfPayOrder.getCashReceivedAmount());
			tableRecordFavourableAmountsResult.setCouponAmount(payResultOfPayOrder.getCouponAmount());
			tableRecordFavourableAmountsResult.setPrepaidcardAmount(payResultOfPayOrder.getPrepaidcardAmount());
			tableRecordFavourableAmountsResult.setUserAccountAmount(payResultOfPayOrder.getUserAccountAmount());
			tableRecordFavourableAmountsResult.setYjpayAmount(payResultOfPayOrder.getYjpayAmount());
			tableRecordFavourableAmountsResult.setWechatAmount(payResultOfPayOrder.getWechatAmount());
			tableRecordFavourableAmountsResult.setIposAmount(payResultOfPayOrder.getIposAmount());
			tableRecordFavourableAmountsResult.setPosAmount(payResultOfPayOrder.getPosAmount());
			if (storeOrder.getCreditStatus() == StoreOrderCreditStatusEnum.CHARGE.getValue() ||
					storeOrder.getCreditStatus() == StoreOrderCreditStatusEnum.DISCHARGE.getValue()) {
				tableRecordFavourableAmountsResult.setCreditAmount(storeOrder.getActualPrice());
			}
			tableRecordFavourableAmountsResult.setPublicTransferAmount(payResultOfPayOrder.getPublicTransferAmount());
			if (payResultOfPayOrder.getPayResultOfDynamicPayMethodList() != null &&
					!payResultOfPayOrder.getPayResultOfDynamicPayMethodList().isEmpty()) {
				// 需要考虑多张券 add by wxy 20161111
				List<StoreOrderPayResultOfDynamicPayMethod> dynamicPayMethods = BeanUtil.copyList(payResultOfPayOrder.getPayResultOfDynamicPayMethodList(), StoreOrderPayResultOfDynamicPayMethod.class);
				tableRecordFavourableAmountsResult.setStoreOrderPayResultOfDynamicPayMethodList(dynamicPayMethods);
				tableRecordFavourableAmountsResult.setDynamicPayDerate(tableRecordFavourableAmountsResult.getDynamicPayAllDerate()); //自定义券支付优惠

			}
			tableRecordFavourableAmountsResult.setAliPayAmount(payResultOfPayOrder.getAliPayAmount());
			tableRecordFavourableAmountsResult.setIboxPayAmount(payResultOfPayOrder.getIboxPayAmount());
		}
		tableRecordFavourableAmountsResult.setOrderPrice(orderPrice);
		tableRecordFavourableAmountsResult.setTableFee(tableFee);
		tableRecordFavourableAmountsResult.setInternetRebatePrice(internetRebatePrice);
		tableRecordFavourableAmountsResult.setEnterpriseRebatePrice(enterpriseRebatePrice);
		tableRecordFavourableAmountsResult.setMemberRebatePrice(memberRebatePrice);
		//tableRecordFavourableAmountsResult.setTotalDerate(totalDerate);
		tableRecordFavourableAmountsResult.setPromotionRebatePrice(promotionRebatePrice);
		tableRecordFavourableAmountsResult.setPromotionReducePrice(promotionReducePrice);
		tableRecordFavourableAmountsResult.setGratisPrice(gratisPrice);
		tableRecordFavourableAmountsResult.setPromotionPrice(promotionPrice);
		
		return tableRecordFavourableAmountsResult;
    }
	
	/**
     * 计算台位费,计算应付台位费金额时减免台位费的金额为0
     * @param storeOrderPlaceParam
     * @param customerTraffic
     * @return
     * @throws T5weiException
     */
    private long getTableFee(StoreOrderPlaceParam storeOrderPlaceParam, int customerTraffic) throws T5weiException{
    	int merchantId = storeOrderPlaceParam.getMerchantId();
    	long storeId = storeOrderPlaceParam.getStoreId();
    	long timeBucketId = storeOrderPlaceParam.getTimeBucketId();
    	int takeMode = storeOrderPlaceParam.getTakeMode();
    	boolean enableAddDishes = storeOrderPlaceParam.isEnableAddDishes();
    	Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId, true);
		if (!store5weiSetting.isEnableTableFee()) {
			return 0;
		}
    	// 入客数为0不计台位费
    	if (customerTraffic <= 0) {
    		return 0;
    	}
        // 加菜的订单不重复收取台位费
        if (enableAddDishes) {
        	return 0;
        }
        // 快取、外送、外带不收台位费
		if (takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue() || takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue() || takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
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
     * 拼装员工返回对象
     * @param staffDTO
     * @return
     */
    public StoreTableStaffDTO getStoreTableStaffDTO (StaffDTO staffDTO) {
		StoreTableStaffDTO storeTableStaffDTO = new StoreTableStaffDTO();
		storeTableStaffDTO.setUserId(staffDTO.getUserId());
		storeTableStaffDTO.setStaffId(staffDTO.getStaffId());
		storeTableStaffDTO.setAliasName(staffDTO.getAliasName());
		storeTableStaffDTO.setMerchantId(staffDTO.getMerchantId());
		storeTableStaffDTO.setStatus(staffDTO.getStatus());
		storeTableStaffDTO.setCreateTime(staffDTO.getCreateTime());
		storeTableStaffDTO.setUpdateTime(staffDTO.getUpdateTime());
		storeTableStaffDTO.setPwd(staffDTO.getPwd());
		storeTableStaffDTO.setPostQuantity(staffDTO.getPostQuantity());
		storeTableStaffDTO.setShowName(staffDTO.getShowName());
		UserDTO userDTO = staffDTO.getUserDTO();
		if (userDTO != null) {
			I5weiUserDTO i5weiUserDTO = BeanUtil.copy(userDTO, I5weiUserDTO.class);
			storeTableStaffDTO.setI5weiUserDTO(i5weiUserDTO);
		}
		return storeTableStaffDTO;
	}
    
    /**
	 * 合台操作时更新目标桌台各项时间
	 * @param targetTableRecord
	 * @param originalTableRecord
	 */
	private void updateTargetTableRecordTimes (StoreTableRecord targetTableRecord, StoreTableRecord originalTableRecord) {
		// 更新开台时间
		if (targetTableRecord.getTableRecordTime() == 0) {
			if (originalTableRecord.getTableRecordTime() > 0) {
				targetTableRecord.setTableRecordTime(originalTableRecord.getTableRecordTime());
			}
		} else {
			if (originalTableRecord.getTableRecordTime() != 0 && originalTableRecord.getTableRecordTime() < targetTableRecord.getTableRecordTime()) {
				targetTableRecord.setTableRecordTime(originalTableRecord.getTableRecordTime());
			}
		}
		// 更新首次点菜时间
		if (targetTableRecord.getOrderTime() == 0) {
			if (originalTableRecord.getOrderTime() > 0) {
				targetTableRecord.setOrderTime(originalTableRecord.getOrderTime());
			}
		} else {
			if (originalTableRecord.getOrderTime() != 0 && originalTableRecord.getOrderTime() < targetTableRecord.getOrderTime()) {
				targetTableRecord.setOrderTime(originalTableRecord.getOrderTime());
			}
		}
		// 更新首次上菜时间
		if (targetTableRecord.getFirstUpTime() == 0) {
			if (originalTableRecord.getFirstUpTime() > 0) {
				targetTableRecord.setFirstUpTime(originalTableRecord.getFirstUpTime());
			}
		} else {
			if (originalTableRecord.getFirstUpTime() != 0 && originalTableRecord.getFirstUpTime() < targetTableRecord.getFirstUpTime()) {
				targetTableRecord.setFirstUpTime(originalTableRecord.getFirstUpTime());
			}
		}
		// 更新最后上菜时间
		if (originalTableRecord.getLastUpTime() > targetTableRecord.getLastUpTime()) {
			targetTableRecord.setLastUpTime(originalTableRecord.getLastUpTime());
		}
	}

	/**
	 * 查看是否合过台,如果合过返回和台的目标桌台记录
	 * @param storeTableRecord
	 * @return
	 * @throws T5weiException
	 */
	public StoreTableRecord getMergeTableRecord (StoreTableRecord storeTableRecord) throws T5weiException {
		StoreTableRecord mergeStoreTableRecord = null;
		while (storeTableRecord.getMergeTableRecordId() > 0) {
			storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId(), storeTableRecord.getMergeTableRecordId(), true);
			if (storeTableRecord == null) {
				throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+storeTableRecord.getMergeTableRecordId()+"] invalid");
			}
		}
		if (mergeStoreTableRecord == null) {
			mergeStoreTableRecord = storeTableRecord;
		}
		return mergeStoreTableRecord;
	}
	/**
	 * 获取实际退款金额
	 */
	public long getActualRefundAmount(List<RefundRecordDBDTO> refundRecordDBDTOs) throws TException {
		if (refundRecordDBDTOs == null || refundRecordDBDTOs.isEmpty()) {
			return 0;
		}
		long actualRefundAmount = 0;
		for (RefundRecordDBDTO refundRecordDBDTO : refundRecordDBDTOs) {
			List<RefundDetailDBDTO> successRefundDetails = refundRecordDBDTO.getSuccessRefundDetails();
			if (successRefundDetails == null || successRefundDetails.isEmpty()) {
				continue;
			}
			for (RefundDetailDBDTO refundDetailDBDTO : successRefundDetails) {
				actualRefundAmount += refundDetailDBDTO.getAmount();
			}
		}
		return actualRefundAmount;
	}

	public List<StoreTableRecord> getStoreTableRecordByName(int merchantId, long storeId, String tableName, boolean enableSlave){
	    if(DataUtil.isEmpty(tableName)){
	        return new ArrayList<StoreTableRecord>();
	    }
	    return this.storeTableRecordDAO.getStoreTableRecordByName(merchantId, storeId, tableName, enableSlave);
	}
	
	private boolean checkStaffDerate (long amount) {
		if (amount % 100 == 0) {
			return true;
		}
		return false;
	}

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreTableRecord reduceTableRecordTableFee(int merchantId, long storeId, long tableRecordId, long reductionTableFee) throws TException {
        StoreTableRecord storeTableRecord = this.storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, true);
        if (storeTableRecord.getTableRecordStatus() == TableRecordStatusEnum.CLEAR_TABLE.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_RECORD_CLRER_REDUCE_TABLE_FEE.getValue(), " store table record clrer reduce table fee ");
        }
        if (storeTableRecord.getTableRecordStatus() == TableRecordStatusEnum.SETTLEMENT.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_RECORD_SETTLEMENT_REDUCE_TABLE_FEE.getValue(), "store table record settlement reduce table fee");
        }
        if (storeTableRecord.getTableRecordStatus() == TableRecordStatusEnum.SETTLING.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_RECORD_SETTLING_REDUCE_TABLE_FEE.getValue(), " store table record settling reduce table fee ");
        }
        if (reductionTableFee > storeTableRecord.getPayAbleTableFee()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_REDUCE_TABLE_FEE_GT_PAYABLE_TABLE_FEE.getValue(), " store table reduce table fee gt payable table fee ");
        }
        long updateTime = System.currentTimeMillis();
        storeTableRecord.setReductionTableFee(reductionTableFee);
        storeTableRecord.setUpdateTime(updateTime);
        this.refreshTableRecord(merchantId, storeId, storeTableRecord);
        storeTableRecord.update();
		//记录桌台记录操作日志, TODO 缺少收银员和客户端类型
		storeTableRecordOptlogService.createTableRecordOptlog(merchantId, storeId, tableRecordId, 0, 0,
				ClientTypeEnum.CASHIER.getValue(), TableRecordOptlogTypeEnum.TABLE_FEE_DERATE.getValue(),
				"桌台记录台位费减免操作", storeTableRecord.getUpdateTime());
        return storeTableRecord;
    }

	/**
	 * 结账完成自动清台
	 * @param merchantId
	 * @param storeId
	 * @param storeTableRecord
	 * @param storeTableSetting
	 */
	private void autoClearTable (int merchantId, long storeId, StoreTableRecord storeTableRecord, StoreTableSetting storeTableSetting) {
		StoreTableSetting _storeTableSetting;
		if (storeTableSetting == null) {
			// 获取店铺桌台设置
			_storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, false);
		} else {
			_storeTableSetting = storeTableSetting;
		}
		// 如果开启了结账后自动清台，则设置桌台记录为清台状态
		if (_storeTableSetting.isEnableTableMode() && _storeTableSetting.isEnableTableAutoClear()) {
			storeTableRecord.setTableRecordStatus(TableRecordStatusEnum.CLEAR_TABLE.getValue());
			storeTableRecord.setClearTableTime(System.currentTimeMillis());
		} 
	}
	
	/**
	 * 清台时将待出餐变为已出餐
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @param subStoreOrderList
	 * @param store5weiSetting
	 * @throws T5weiException
	 */
	private void clearStoreMeal (int merchantId, long storeId, long tableRecordId, String masterOrderId, List<StoreOrder> subStoreOrderList, Store5weiSetting store5weiSetting) throws T5weiException {
		Store5weiSetting _store5weiSetting;
		if (store5weiSetting == null) {
			_store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId, false);
		} else {
			_store5weiSetting = store5weiSetting;
		}
		// 将桌台记录包含全部子订单变为已出餐
        storeMealService.clearStoreMeal(subStoreOrderList);
        if (_store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
            // 将桌台记录包含全部子订单变为已划菜
            storeMealSweepService.sweepStoreMealByOrderId(merchantId, storeId, tableRecordId, masterOrderId);
        }
	}


	public int getStoreTableRecordsCount(int merchantId, long storeId, long tableRecordId, long repastDate, long staffId) {
		return storeTableRecordDAO.getStoreTableRecordsCount(merchantId, storeId, tableRecordId, repastDate, staffId, true);
	}

	public List<StoreTableRecord> getStoreTableRecords(int merchantId, long storeId, long tableRecordId, long repastDate, long staffId, int pageNo, int size) {
		return storeTableRecordDAO.getStoreTableRecords(merchantId, storeId, tableRecordId, repastDate, staffId, pageNo, size, true);
	}
}




/**
 * 开台请求参数
 * @author licheng7
 * 2016年6月17日 上午11:10:31
 */
class OpenTableRecordRequestParam {
	/**
	 * 商编
	 */
	private int merchantId; 
	/**
	 * 店铺id
	 */
	private long storeId; 
	/**
	 * 就餐日期
	 */
	private long repastDate;
	/**
	 * 营业时段
	 */
	private long timeBucketId;
	/**
	 * 桌台id
	 */
	private long tableId;
	/**
	 * 就餐人数
	 */
	private int customerTraffic;
	/**
	 * 开台服务员
	 */
	private long createTableRecordStaffId;
	/**
	 * 用户id
	 */
	private long userId;
	/**
	 * 开台订单列表
	 */
	private List<StoreOrder> orderList;
	/**
	 * 终端类型
	 */
	private int clientType;
	/**
	 * 默认桌台服务员
	 */
	private long defaultStaffId;
	
	public int getMerchantId() {
		return merchantId;
	}
	
	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}
	
	public long getStoreId() {
		return storeId;
	}
	
	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}
	
	public long getRepastDate() {
		return repastDate;
	}
	
	public void setRepastDate(long repastDate) {
		this.repastDate = repastDate;
	}
	
	public long getTimeBucketId() {
		return timeBucketId;
	}
	
	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}
	
	public long getTableId() {
		return tableId;
	}
	
	public void setTableId(long tableId) {
		this.tableId = tableId;
	}
	
	public int getCustomerTraffic() {
		return customerTraffic;
	}
	
	public void setCustomerTraffic(int customerTraffic) {
		this.customerTraffic = customerTraffic;
	}
	
	public long getCreateTableRecordStaffId() {
		return createTableRecordStaffId;
	}

	public void setCreateTableRecordStaffId(long createTableRecordStaffId) {
		this.createTableRecordStaffId = createTableRecordStaffId;
	}

	public long getUserId() {
		return userId;
	}
	
	public void setUserId(long userId) {
		this.userId = userId;
	}
	
	public List<StoreOrder> getOrderList() {
		return orderList;
	}
	
	public void setOrderList(List<StoreOrder> orderList) {
		this.orderList = orderList;
	}
	
	public int getClientType() {
		return clientType;
	}
	
	public void setClientType(int clientType) {
		this.clientType = clientType;
	}
	
	public long getDefaultStaffId() {
		return defaultStaffId;
	}
	
	public void setDefaultStaffId(long defaultStaffId) {
		this.defaultStaffId = defaultStaffId;
	}
}
