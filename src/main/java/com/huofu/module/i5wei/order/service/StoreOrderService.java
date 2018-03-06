package com.huofu.module.i5wei.order.service;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.Lists;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.meal.service.StoreMealService;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.dao.*;
import com.huofu.module.i5wei.order.entity.*;
import com.huofu.module.i5wei.order.facade.StoreOrderPay5weiParam;
import com.huofu.module.i5wei.promotion.service.StoreChargeItemPromotionService;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.request.service.Store5weiRequestParam;
import com.huofu.module.i5wei.request.service.Store5weiRequestService;
import com.huofu.module.i5wei.setting.dao.Store5weiSettingDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.service.OrderPayFinishResult;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;

import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.config.currency.CurrencyEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.merchant.info.MerchantDTO;
import huofucore.facade.merchant.info.MerchantFacade;
import huofucore.facade.pay.payment.DefineComPayResult;
import huofucore.facade.statistics.order.StoreOrderUpdateDTO;
import huofucore.facade.statistics.order.StoreOrderUpdateFacade;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.MoneyUtil;
import huofuhelper.util.RandomCode;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class StoreOrderService {

    private static final Log log = LogFactory.getLog(StoreOrderService.class);

    @Autowired
    private StoreOrderDAO storeOrderDAO;

    @Autowired
    private StoreOrderItemDAO storeOrderItemDAO;

    @Autowired
    private StoreOrderSubitemDAO storeOrderSubitemDAO;

    @Autowired
    private StoreOrderNumberDAO storeOrderNumberDAO;

    @Autowired
    private StoreOrderOptlogDAO storeOrderOptlogDAO;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    @Autowired
    private StoreOrderPriceHelper storeOrderPriceHelper;

    @Autowired
    private StoreChargeItemService storeChargeItemService;

    @Autowired
    private StoreInventoryService storeInventoryService;

    @Autowired
    private StoreMealService storeMealService;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @Autowired
    private Store5weiSettingDAO store5weiSettingDAO;

    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDAO;

    @Autowired
    private StoreMealCheckoutDAO storeMealCheckoutDAO;

    @Autowired
    private StoreOrderSwitchDAO storeOrderSwitchDAO;

    @Autowired
    private StoreOrderItemPromotionDAO storeOrderItemPromotionDAO;

    @Autowired
    private StoreChargeItemPromotionService storeChargeItemPromotionService;

    @ThriftClient
    private StoreOrderUpdateFacade.Iface storeOrderUpdateFacade;

    @Autowired
    private StoreTableRecordDAO storeTableRecordDAO;

    @ThriftClient
    private MerchantFacade.Iface merchantFacade;

    @Autowired
    private StoreOrderCombinedBizDao storeOrderCombinedBizDao;
    
    @Autowired
    private StoreTableRecordService storeTableRecordService;
    
    @Autowired
    private Store5weiRequestService store5weiRequestService;
    
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder placeStoreOrder(PlaceOrderParam placeOrderParam) throws TException {
        StoreOrderPlaceParam storeOrderPlaceParam = placeOrderParam.getStoreOrderPlaceParam();
        // 下单入参
        int currencyId = placeOrderParam.getCurrencyId();
        int merchantId = storeOrderPlaceParam.getMerchantId();
        long storeId = storeOrderPlaceParam.getStoreId();
        long timeBucketId = storeOrderPlaceParam.getTimeBucketId();
        long staffId = storeOrderPlaceParam.getStaffId();
        boolean backOrder = storeOrderPlaceParam.isBackOrder();// 收银台订单补录判断
        int takeMode = storeOrderPlaceParam.getTakeMode();
        int siteNumber = storeOrderPlaceParam.getSiteNumber();
        List<StoreOrderPlaceItemParam> orderPlaceItems = storeOrderPlaceParam.getChargeItems();
        boolean enableManualCustomerTraffic = storeOrderPlaceParam.isEnableManualCustomerTraffic();
        // 构造订单对象
        String inputOrderId = storeOrderPlaceParam.getOrderId();// 得到输入订单ID
        StoreOrder storeOrder;
        if (inputOrderId == null || inputOrderId.isEmpty()) {
            storeOrder = new StoreOrder();
        } else {
            storeOrder = storeOrderDAO.getById(merchantId, storeId, inputOrderId, true, true);
            if (storeOrder == null) {
                storeOrder = new StoreOrder();
            } else {
                String orderId = storeOrder.getOrderId();
                if (siteNumber <= 0) {
                    siteNumber = storeOrder.getSiteNumber();
                }
                if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_FINISH.getValue(), DataUtil.infoWithParams("store order pay finish, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
                }
                if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAYING.getValue(), DataUtil.infoWithParams("store order paying, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
                }
                if (storeOrder.getTakeSerialNumber() > 0) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_TAKE_CODE.getValue(), DataUtil.infoWithParams("store order has take code, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
                }
            }
        }
        // 根据订单的订单子项目信息得到详情
        long repastDate = DateUtil.getBeginTime(storeOrderPlaceParam.getRepastDate(), null);
        List<Long> orderChargeItemIds = storeOrderHelper.getChargeItemIdsOfStoreOrder(storeOrderPlaceParam);
        List<StoreChargeItem> orderChargeItems = storeChargeItemService.getStoreChargeItemsInIds(merchantId, storeId, orderChargeItemIds, repastDate);
        if (orderChargeItems == null || orderChargeItems.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ITEM_CHARGE_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order chargeitem not exist, storeId=#1, timeBucketId=#2, repastDate=#3, orderChargeItemIds=#4 ", new Object[]{storeId, timeBucketId,
                    storeOrderPlaceParam.getRepastDate(), JsonUtil.build(orderChargeItemIds)}));
        }
        // 校验下单库存
        if (this.isCheckInventory(placeOrderParam)) {
            Map<Long, Double> orderItemMap = storeOrderHelper.getChargeItemNumMapOfStoreOrder(storeOrderPlaceParam);
            storeInventoryService.checkInventoryForOrderPlace(merchantId, storeId, repastDate, timeBucketId, orderChargeItems, orderItemMap);
        }
        // 计算订单折扣&打包费
        StoreOrderPriceResult storeOrderPriceResult = storeOrderPriceHelper.getStoreOrderRebateResult(placeOrderParam, orderChargeItems);
        BeanUtil.copy(storeOrderPriceResult, storeOrder);
        // 计算外送费
        long deliveryFee = storeOrderPriceHelper.getStoreOrderDeliveryFee(placeOrderParam, orderChargeItems, storeOrderPriceResult);
        // 取餐方式
        storeOrder.setTakeMode(takeMode);
        StoreOrderTakeModeResult storeOrderTakeModeResult = storeOrderHelper.getStoreOrderTakeModeResult(placeOrderParam, orderChargeItems, storeOrder.isProducePackageFee());
        BeanUtil.copy(storeOrderTakeModeResult, storeOrder);
        // 订单赋值
        storeOrder.setMerchantId(merchantId);// 商户ID
        storeOrder.setStoreId(storeId);// 店铺ID
        storeOrder.setStaffId(staffId);// 服务员工ID（用户下单时，staff_id＝0）
        storeOrder.setUserId(storeOrderPlaceParam.getUserId());// 下单用户（员工下单时，有对应用户时user_id不等于0，无对应用户user_id＝0）
        if(storeOrderPlaceParam.isSetUserRemark()){
            storeOrder.setUserRemark(storeOrderPlaceParam.getUserRemark());
        }
        storeOrder.setTimeBucketId(timeBucketId);// 营业时间ID
        storeOrder.setClientType(storeOrderPlaceParam.getClientType());// 下单终端类型（需要定义客户端类型枚举类）
        storeOrder.setRepastDate(repastDate);// 就餐日期
        storeOrder.setDeliveryFee(deliveryFee);
        storeOrder.setOrderRemark(storeOrderPlaceParam.getOrderRemark());// 订单备注
        storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.NOT.getValue());
        storeOrder.setInvoiceDemand("");
        storeOrder.setBackOrder(backOrder);// 收银台订单补录判断
        storeOrder.setSiteNumber(siteNumber);// 设置桌牌号
        storeOrder.setEnableManualCustomerTraffic(enableManualCustomerTraffic);// 是否手动设置了入客数
        storeOrder.setTableRecordId(storeOrderPlaceParam.getTableRecordId());// 桌台记录ID
        storeOrder.setRequestId(storeOrderPlaceParam.getRequestId());// 下单请求ID，避免重复提交
        storeOrder.setCashierChannelId(storeOrderPlaceParam.getCashierChannelId()); // 收款线id
        // 桌台相关参数
        storeOrder.setPayAfter(storeOrderPlaceParam.isPayAfter());// 是否后付费
        // 订单信息入库
        int optType;
        String optRemark;
        List<StoreOrderItem> storeOrderItems;
        if (storeOrder.getOrderId() == null || storeOrder.getOrderId().isEmpty()) {
            // 创建订单
            this.createStoreOrder(storeOrder, currencyId);// 创建订单
            storeOrderItems = this.createStoreOrderItems(orderChargeItems, orderPlaceItems, storeOrderPriceResult, storeOrder);// 创建订单子项目&订单明细
            optType = StoreOrderOptlogTypeEnum.PLACE_ORDER_CREATE.getValue();
            optRemark = "place order, create";
        } else {
            // 更新订单
            this.deleteStoreOrderItemsById(merchantId, storeId, inputOrderId);// 删除历史订单子项目&历史订单明细
            storeOrder.setUpdateTime(System.currentTimeMillis());
            storeOrder.update();
            storeOrderItems = this.createStoreOrderItems(orderChargeItems, orderPlaceItems, storeOrderPriceResult, storeOrder);// 创建新的订单子项目&订单明细
            optType = StoreOrderOptlogTypeEnum.PLACE_ORDER_UPDATE.getValue();
            optRemark = "place order, update";
        }
        storeOrder.setStoreOrderItems(storeOrderItems);
        storeOrder.setPlaceOrderTime(System.currentTimeMillis());
        // 记录日志
        storeOrderOptlogDAO.createOptlog(storeOrder, staffId, storeOrderPlaceParam.getClientType(), optType, optRemark);
        return storeOrder;
    }
    
    /**
     * 是否校验库存
     * @param placeOrderParam
     * @return
     */
    private boolean isCheckInventory(PlaceOrderParam placeOrderParam){
    	boolean check = true;

        if(placeOrderParam.getStoreOrderPlaceParam().getClientType() == ClientTypeEnum.MINA.getValue()){
            check = false; //小程序订单不需要校验库存
        }

		if (placeOrderParam.isQuickTrade()) {
			check = false;// 自助餐不校验库存
		}
		if (placeOrderParam.getStoreOrderPlaceParam().isBackOrder()) {
			check = false;// 补录订单不校验库存
		}
    	return check;
    }
    
    public String createStoreOrder(StoreOrder storeOrder, int currencyId) throws T5weiException {
        storeOrder.setOrderLockStatus(StoreOrderLockStatusEnum.NOT.getValue()); // 下单时订单未锁定，支付时再根据设置决定是否锁定
        storeOrder.setOrderCurrencyId(currencyId);// 下单币种，商户默认币种
        storeOrder.setPayStatus(StoreOrderPayStatusEnum.NOT.getValue());// 订单的支付状态：1=待支付；
        storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.NOT.getValue());// 1＝未退款
        storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.NOT.getValue());// 交易状态是对基础交易订单的交易状态进行扩展：1＝尚未交易；
        storeOrder.setTakeupStatus(StoreOrderTakeupStatusEnum.NOT_RETAIN.getValue());// 库存保留状态：1＝非保留，占用5分钟；
        storeOrder.setTakeSerialNumber(0);// 取餐流水号
        storeOrder.setTakeCode("");// 取餐验证码
        storeOrder.setUpdateTime(System.currentTimeMillis());// 更新时间
        storeOrder.setCreateTime(System.currentTimeMillis());// 新增时间
        storeOrder.setPayOrderId("");
        storeOrder.create();
        // 下单请求唯一性约束
        Store5weiRequestParam param = storeOrderHelper.getStore5weiRequestParam(storeOrder);
        store5weiRequestService.save(param);
        return storeOrder.getOrderId();
    }

    /**
     * 根据子订单创建所属桌台记录对应主订单(桌台记录首次关联子订单时，需要同步创建一个空的主订单，用于获取主订单orderId)
     *
     * @param subStoreOrder 关联到支付记录上的第一个子订单
     * @param tableRecordId 支付记录id
     * @return masterStoreOrder
     * @throws T5weiException
     */
    public StoreOrder createMasterStoreOrder(StoreOrder subStoreOrder, long tableRecordId) throws T5weiException {
        int merchantId = subStoreOrder.getMerchantId();
        long storeId = subStoreOrder.getStoreId();
        long repastDate = subStoreOrder.getRepastDate();
        int takeSerialNumber = this.getTakeSerialNumber(merchantId, storeId, repastDate);
        String takeCode = this.getTakeCode(merchantId, storeId, repastDate);
        StoreOrder masterStoreOrder = new StoreOrder();
        masterStoreOrder.initMasterStoreOrder(subStoreOrder, tableRecordId, takeSerialNumber, takeCode);
        masterStoreOrder.create();
        return masterStoreOrder;
    }

    /**
     * 用于还需支付的桌台记录，结账时拼装主订单，之后通过主订单进行支付
     *
     * @param masterStoreOrder 原始主订单
     */
    public StoreOrder settleMasterStoreOrder(StoreOrder masterStoreOrder) {
        storeOrderPriceHelper.buildMasterOrderSettleInfo(masterStoreOrder);
        masterStoreOrder.update();
        return masterStoreOrder;
    }

    /**
     * 订单出餐完成，或外送订单送到
     *
     * @param tableRecordId
     * @param storeOrders
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateOrderTradeFinish(long tableRecordId, List<StoreOrder> storeOrders) {
        if (storeOrders == null || storeOrders.isEmpty()) {
            return;
        }
        StoreOrder masterStoreOrder = null;
        StringBuilder allOrderDescription = new StringBuilder();
        Set<Long> userIds = new HashSet<Long>();
        for (StoreOrder storeOrder : storeOrders) {
            this.updateOrderTradeFinish(storeOrder);
            if (!StringUtils.isNullOrEmpty(storeOrder.getOrderDescription())) {
            	allOrderDescription.append(storeOrder.getOrderDescription());
            }
            if (storeOrder.isTableRecordMasterOrder()) {
                masterStoreOrder = storeOrder;
            }
            if (storeOrder.getUserId() > 0) {
            	userIds.add(storeOrder.getUserId());
            }
        }
        if (masterStoreOrder != null) {
            masterStoreOrder.setOrderDescription(allOrderDescription.toString());
            StoreTableRecord storeTableRecord;
			try {
				storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(masterStoreOrder.getMerchantId(), masterStoreOrder.getStoreId(), tableRecordId, false);
				i5weiMessageProducer.sendMessageOfSendCouponAmount(storeTableRecord);
				if (!userIds.isEmpty()) {
					for (long userId : userIds) {
						masterStoreOrder.setUserId(userId);
						i5weiMessageProducer.sendMessageOfStoreOrderGrade(masterStoreOrder);
					}
				}
			} catch (T5weiException e) {
				e.printStackTrace();
			}
        }
    }

    /**
     * 订单出餐完成，或外送订单送到
     *
     * @param storeOrder
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateOrderTradeFinish(StoreOrder storeOrder) {
        if (storeOrder == null) {
            log.error("storeOrder is null");
        }
        boolean enableSlave = false;
        storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.FINISH.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());// 更新时间
        storeOrderDAO.update(storeOrder);
        storeOrderOptlogDAO.createOptlog(storeOrder, storeOrder.getStaffId(), storeOrder.getClientType(), StoreOrderOptlogTypeEnum.TRADE_FINISH.getValue(), "updateOrder TradeFinish");
        // 设置交易时间
        storeOrderHelper.setStoreOrderTimes(storeOrder, enableSlave);
        storeOrder.setTradeFinishTime(System.currentTimeMillis());
        // 获取订单详情
        if (storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
            storeOrderHelper.setStoreOrderDetail(storeOrder, enableSlave);
        }
        // 计算订单描述
        String orderDescription = "";
        if (!storeOrder.isTableRecordMasterOrder()) {
        	orderDescription = storeOrderHelper.getChargeItemStrOfStoreOrder(storeOrder);
        	storeOrder.setOrderDescription(orderDescription);
        }
        // 非桌台模式，非全额退款的进入评分邀请
        if (storeOrder.getTableRecordId() <= 0 && !storeOrder.isRefund4All()) {
            i5weiMessageProducer.sendMessageOfStoreOrderGrade(storeOrder);
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder cancelPayStoreOrder(int merchantId, long storeId, String orderId) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
            return storeOrder;
        }
        storeOrder.setPayStatus(StoreOrderPayStatusEnum.NOT.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());// 更新时间
        storeOrderDAO.update(storeOrder);
        storeOrderOptlogDAO.createOptlog(storeOrder, storeOrder.getStaffId(), storeOrder.getClientType(), StoreOrderOptlogTypeEnum.UNKNOWN.getValue(), "StoreOrder cancel pay");
        return storeOrder;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder storeOrderPaying(int merchantId, long storeId, String orderId) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue() || storeOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue()) {
            return storeOrder;
        }
        storeOrder.setPayStatus(StoreOrderPayStatusEnum.DOING.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());// 更新时间
        storeOrderDAO.update(storeOrder);
        storeOrderOptlogDAO.createOptlog(storeOrder, storeOrder.getStaffId(), storeOrder.getClientType(), StoreOrderOptlogTypeEnum.USER_PAYING_ORDER.getValue(), "StoreOrder paying");
        return storeOrder;
    }

    /**
     * 外送订单出餐完成
     *
     * @param merchantId
     * @param storeId
     * @param storeOrder
     * @return
     */
    public StoreOrder updateOrderPrepareMealFinish(int merchantId, long storeId, StoreOrder storeOrder) {
        storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());// 更新时间
        storeOrderDAO.update(storeOrder);
        storeOrderOptlogDAO.createOptlog(storeOrder, storeOrder.getStaffId(), storeOrder.getClientType(), StoreOrderOptlogTypeEnum.MEAL_CHECKOUT_COMPLETE.getValue(), "StoreOrder prepare meal finish");
        return storeOrder;
    }

    public List<StoreOrderItem> createStoreOrderItems(List<StoreChargeItem> orderChargeItems, List<StoreOrderPlaceItemParam> orderPlaceItems, StoreOrderPriceResult storeOrderPriceResult, StoreOrder storeOrder)
            throws T5weiException {
        if (orderChargeItems == null) {
            return null;
        }
		String orderId = storeOrder.getOrderId();
		long repastDate = storeOrder.getRepastDate();
		long timeBucketId = storeOrder.getTimeBucketId();
        List<StoreOrderItem> orderItemlist = storeOrderHelper.getStoreOrderItem(orderId, orderChargeItems, orderPlaceItems);
        List<StoreOrderSubitem> orderSubitemlist = storeOrderHelper.getStoreOrderSubItem(orderId, repastDate, timeBucketId, orderChargeItems, orderPlaceItems);
        storeOrderPriceHelper.amortizeStoreOrderItemRebatePrice(storeOrderPriceResult, storeOrder, orderItemlist);
        storeOrderItemDAO.batchCreate(orderItemlist);
        storeOrderSubitemDAO.batchCreate(orderSubitemlist);
        //edit by Jemon 20161201
        if(storeOrderPriceResult!=null){
           storeOrderItemPromotionDAO.batchCreate(storeOrderPriceResult.getStoreOrderItemPromotions());
        }
        // 构造对象返回
        for (StoreOrderItem item : orderItemlist) {
            List<StoreOrderSubitem> storeOrderSubitems = new ArrayList<StoreOrderSubitem>();
            for (StoreOrderSubitem subItem : orderSubitemlist) {
                if (item.getChargeItemId() == subItem.getChargeItemId()) {
                    storeOrderSubitems.add(subItem);
                }
            }
            item.setStoreOrderSubitems(storeOrderSubitems);
        }
        return orderItemlist;
    }

    public void deleteStoreOrderItemsById(int merchantId, long storeId, String orderId) {
        storeOrderItemDAO.deleteByOrderId(merchantId, storeId, orderId);
        storeOrderSubitemDAO.deleteByOrderId(merchantId, storeId, orderId);
        storeOrderItemPromotionDAO.deleteByOrderId(merchantId, storeId, orderId);
    }

    /**
     * 根据查询输入订单ID，得到订单详情（仅包括订单）
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws T5weiException
     */
    public StoreOrder getStoreOrderById(int merchantId, long storeId, String orderId) throws T5weiException {
        boolean enableSlave = false;
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, false, false);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        // 设置订单个状态的时间点
        storeOrderHelper.setStoreOrderTimes(storeOrder, enableSlave);
        return storeOrder;
    }

    /**
     * 根据查询输入订单ID，得到订单详情（仅包括订单，更新使用）
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws T5weiException
     */
    public StoreOrder getStoreOrderById4Update(int merchantId, long storeId, String orderId) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        return storeOrder;
    }

    /**
     * 根据查询输入订单ID，得到订单详情（订单、订单项、订单明细）
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws TException 
     */
    public StoreOrder getStoreOrderDetailById(int merchantId, long storeId, String orderId) throws TException {
        boolean enableSlave = false;
        StoreOrder storeOrder = storeOrderDAO.getStoreOrderById(merchantId, storeId, orderId, enableSlave);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        StoreOrderSwitch storeOrderSwitch = storeOrderSwitchDAO.getStoreOrderSwitchById(merchantId, storeId, orderId);
        if (storeOrderSwitch != null) {
            storeOrder.setStoreOrderSwitch(storeOrderSwitch);
        }
        storeOrderHelper.setStoreOrderDetail(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderTimes(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderDelivery(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderTableRecord(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderRefundItem(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderPromotion(storeOrder, enableSlave);
        if(storeOrder.isTableRecordSubOrder()){
        	StoreOrder masterOrder = storeOrderDAO.getStoreOrderById(merchantId, storeId, storeOrder.getParentOrderId(), enableSlave);
        	storeOrder.setOrdersDineIn(masterOrder.getOrdersDineIn());
			storeOrder.setOrdersInAndOut(masterOrder.getOrdersInAndOut());
			storeOrder.setOrdersSendOut(masterOrder.getOrdersSendOut());
			storeOrder.setOrdersTakeOut(masterOrder.getOrdersTakeOut());
			storeOrder.setOrdersTrade(masterOrder.getOrdersTrade());
        }
        return storeOrder;
    }

    /**
     * 根据查询输入订单ID，得到订单详情（订单、外卖信息）
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws T5weiException
     */
    public StoreOrder getStoreOrderDeliveryDetailById(int merchantId, long storeId, String orderId) throws T5weiException {
        boolean enableSlave = false;
        StoreOrder storeOrder = storeOrderDAO.getStoreOrderById(merchantId, storeId, orderId, enableSlave);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        storeOrderHelper.setStoreOrderTimeBucket(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderTimes(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderDelivery(storeOrder, enableSlave);
        storeOrderHelper.setStoreOrderTableRecord(storeOrder, enableSlave);
        return storeOrder;
    }

    public List<StoreOrder> getStoreOrders(int merchantId, long storeId, List<String> orderIds) {
        boolean enableSlave = true;
        List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrdersById(merchantId, storeId, orderIds, enableSlave);
        return storeOrders;
    }

    /**
     * 在调用账户付款之前，先调用餐饮行业订单（支付中）接口，将付款状态改为支付中
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param payOrderId
     * @param userId
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder toPayStoreOrder(int merchantId, long storeId, String orderId, String payOrderId, long userId) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2, payOrderId=#3", new Object[]{storeId, orderId, payOrderId}));
        }
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue() || storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
            return storeOrder;
        }
        // 根据支付时的userId更改交易订单归属
        if (userId > 0 && userId != storeOrder.getUserId()) {
            storeOrder.setUserId(userId);
        }
        storeOrder.setPayOrderId(payOrderId);
        storeOrder.setPayStatus(StoreOrderPayStatusEnum.DOING.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrderDAO.update(storeOrder);
        // 更新库存
        try {
            storeInventoryService.updateInventoryDateByOrder(storeOrder);
        } catch (Throwable e) {
            log.error("#### fail to updateInventoryByOrder ", e);
        }
        int clientType = storeOrder.getClientType();
        storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_PAYING_ORDER.getValue(), "to request m-pay, store order in paying");
        return storeOrder;
    }

    /**
     * 在调起微信支付时，不改变订单支付状态，仅记录支付订单ID相关信息
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param payOrderId
     * @param userId
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder recordWechatPayStoreOrder(int merchantId, long storeId, String orderId, String payOrderId, long userId) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2, payOrderId=#3", new Object[]{storeId, orderId, payOrderId}));
        }
        // 根据支付时的userId更改交易订单归属
        if (userId > 0 && userId != storeOrder.getUserId()) {
            storeOrder.setUserId(userId);
        }
        storeOrder.setPayOrderId(payOrderId);
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrderDAO.update(storeOrder);
        // 更新库存
        try {
            storeInventoryService.updateInventoryDateByOrder(storeOrder);
        } catch (Throwable e) {
            log.error("#### fail to updateInventoryByOrder ", e);
        }
        int clientType = storeOrder.getClientType();
        storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_PAYING_ORDER.getValue(), "to request m-pay, record wechat payOrderId ");
        return storeOrder;
    }

    /**
     * 调用餐饮行业订单（支付中）接口，支付失败状态回滚为“待支付”
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder toPayStoreOrderException(int merchantId, long storeId, String orderId, String errorMsg) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2", new Object[]{storeId, orderId}));
        }
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FAILURE.getValue()) {
            return storeOrder;
        }
        storeOrder.setPayStatus(StoreOrderPayStatusEnum.FAILURE.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrderDAO.update(storeOrder);
        int clientType = storeOrder.getClientType();
        if (errorMsg == null || errorMsg.isEmpty()) {
            errorMsg = "to request m-pay catch Exception, store order not pay";
        }
        storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_PAYING_ORDER.getValue(), errorMsg);
        return storeOrder;
    }

    /**
     * 在界面收到账户付款完成结果之后，调用餐饮行业订单（已支付）接口，状态改为已支付
     *
     * @param storeOrderPay5weiParam
     * @throws TException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder payStoreOrder(StoreOrderPay5weiParam storeOrderPay5weiParam) throws TException {
        // 检查输入参数（订单号）
        int merchantId = storeOrderPay5weiParam.getMerchantId();
        long storeId = storeOrderPay5weiParam.getStoreId();
        String orderId = storeOrderPay5weiParam.getOrderId();
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        storeOrder.snapshot();
        // 根据支付时的userId更改交易订单归属
        int clientType = storeOrder.getClientType();
        long userId = storeOrderPay5weiParam.getUserId();
        if (userId > 0 && userId != storeOrder.getUserId()) {
            storeOrder.setUserId(userId);
        }
        // 处理订单自动锁定状态
        Store5weiSetting store5weiSetting = storeOrderPay5weiParam.getStore5weiSetting();
        if (storeOrder.getUserId() > 0 && clientType != ClientTypeEnum.CASHIER.getValue() && store5weiSetting != null && store5weiSetting.isAutoLockOrder()) {
            if (storeOrder.getOrderLockStatus() == StoreOrderLockStatusEnum.NOT.getValue() && storeOrder.getChargeItemPrice() >= store5weiSetting.getAutoLockOrderAmount()) {
                MutableDateTime mdt = new MutableDateTime(System.currentTimeMillis());
                mdt.addMinutes(store5weiSetting.getAutoLockOrderDeadline());
                storeOrder.setOrderLockStatus(StoreOrderLockStatusEnum.AUTO.getValue());
                storeOrder.setAutoLockTime(mdt.getMillis());
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, storeOrder.getClientType(), StoreOrderOptlogTypeEnum.ORDER_LOCK.getValue(), "set order auto lock time");
            }
        }
        storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        // 优惠券支付部分在收费项目做分摊
		if (storeOrderPay5weiParam.hasCouponPayAmount()){
			long orderCouponDerate = storeOrderPay5weiParam.getPayResult().getCouponAmount();
			storeOrder.setOrderCouponDerate(orderCouponDerate);
			storeOrderPriceHelper.amortizeStoreOrderItemCouponRebatePrice(storeOrder);
        }
        // 已支付成功的返回
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
            storeOrder.setUpdateTime(System.currentTimeMillis());
            storeOrder.update();
            return storeOrder;
        }
        // 信息入库：支付状态=已支付、库存保留状态＝已扣减、取餐码=生成取餐码
        storeOrder.setPayStatus(StoreOrderPayStatusEnum.FINISH.getValue());
        if (storeOrder.getTableRecordId() > 0) {
            if (storeOrder.getTakeSerialNumber() == 0) { // 桌台模式先付费的子订单
                StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, storeOrder.getTableRecordId(), true);
                if (storeTableRecord != null) {
                    StoreOrder masterStoreOrder = storeOrderDAO.getMasterOrderByTableRecordId(merchantId, storeId, storeOrder.getTableRecordId(), false);
                    if (masterStoreOrder == null) {
                        masterStoreOrder = this.createMasterStoreOrder(storeOrder, storeTableRecord.getTableRecordId());
                        masterStoreOrder.setCustomerTraffic(storeTableRecord.getCustomerTraffic());
                        masterStoreOrder.setUpdateTime(System.currentTimeMillis());
                        masterStoreOrder.update();
                        storeTableRecord.setTakeSerialNumber(masterStoreOrder.getTakeSerialNumber());
                        storeTableRecord.setOrderTime(System.currentTimeMillis());
                        storeOrder.setEnableAddDishes(false);
                    } else {
                        storeOrder.setEnableAddDishes(true);
                    }
                    storeOrder.setParentOrderId(masterStoreOrder.getOrderId());
                    storeTableRecord.setOrderId(masterStoreOrder.getOrderId());
                    storeTableRecord.setUpdateTime(System.currentTimeMillis());
                    storeTableRecord.update();
                }
            }
        }
        List<StoreMealTakeup> storeMealTakeups = null;
        if (clientType == ClientTypeEnum.CASHIER.getValue() || storeOrder.isSkipTakeCode()) {
            // 收银员下单不用生成取餐码
            if (storeOrder.getTableRecordId() == 0 || storeOrder.getTakeSerialNumber() == 0) {
                storeMealTakeups = this.toTradeSkipTakeCode(storeOrder);// 进入出餐交易环节
            }
            storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.CASHIER_PAY_ORDER.getValue(), "pay finish");
        } else {
            // 生成取餐码
            if (storeOrder.getTakeCode() == null || storeOrder.getTakeCode().isEmpty()) {
                storeOrder.setTakeupStatus(StoreOrderTakeupStatusEnum.RETAIN.getValue());
                String takeCode = this.getTakeCode(merchantId, storeId, storeOrder.getRepastDate());
                storeOrder.setTakeCode(takeCode);
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_PAY_ORDER.getValue(), "user pay finish");
            }
        }
        String payOrderId = storeOrderPay5weiParam.getPayOrderId();
        if (payOrderId == null) {
            payOrderId = "";
        }
        storeOrder.setPayOrderId(payOrderId);// 支付订单ID
        int actualCurrencyId = storeOrderPay5weiParam.getActualCurrencyId();
        if (actualCurrencyId == 0) {
            actualCurrencyId = storeOrder.getOrderCurrencyId();
        }
        storeOrder.setActualCurrencyId(actualCurrencyId);// 实际支付币种，将来多币种实现时需要考虑
        long actualPrice = storeOrderPay5weiParam.getActualPrice();
        if (actualPrice == 0) {
            actualPrice = storeOrder.getPayablePrice();
        }
        storeOrder.setActualPrice(actualPrice);// 实际支付币种对应的金额
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrder.update();
        storeOrderItemPromotionDAO.updatePayOrder(merchantId, storeId, orderId);
        storeChargeItemPromotionService.updateSaleNum(merchantId, storeId, orderId);
        storeOrder.setStoreMealTakeups(storeMealTakeups);

        try {
            storeInventoryService.updateInventoryDateByOrder(storeOrder);
        } catch (Throwable e) {
            log.error("#### fail to updateInventoryByOrder ", e);
        }
        // 若为桌台后付费判断是否为主订单支付完成，用于结账
        OrderPayFinishResult orderPayFinishResult = storeTableRecordService.orderPayFinish(merchantId, storeId, orderId,userId);
        storeOrder.setOrderPayFinishResult(orderPayFinishResult);
        
        return storeOrder;
    }

    /**
     * 订单赊账
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws TException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder chargeStoreOrder(int merchantId, long storeId, String orderId, int creditType) throws TException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.NOT.getValue() && storeOrder.getCreditStatus() == StoreOrderCreditStatusEnum.NO_CREDIT.getValue()) {
            storeOrderOptlogDAO.createOptlog(storeOrder, 0, ClientTypeEnum.CASHIER.getValue(), StoreOrderOptlogTypeEnum.CASHIER_PAY_ORDER.getValue(), "cashier charge order");
            // 订单进入出餐交易环节
            this.toTradeSkipTakeCode(storeOrder);
            storeOrder.setCreditStatus(StoreOrderCreditStatusEnum.CHARGE.getValue());
            storeOrder.setCreditType(creditType);
            storeOrder.setActualCurrencyId(CurrencyEnum.RMB.getValue());
            storeOrder.setActualPrice(storeOrder.getPayablePrice());
            storeOrder.update();
            // 更新库存
            try {
                storeInventoryService.updateInventoryDateByOrder(storeOrder);
            } catch (Throwable e) {
                log.error("#### fail to updateInventoryByOrder ", e);
            }
        }
        return storeOrder;
    }

    /**
     * 订单进入出餐交易环节
     *
     * @param storeOrder
     * @throws TException
     */
    private List<StoreMealTakeup> toTradeSkipTakeCode(StoreOrder storeOrder) throws TException {
        List<StoreMealTakeup> storeMealTakeups = new ArrayList<>();
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), "storeOrder is null");
        }
        if (storeOrder.isTableRecordMasterOrder()) {
            return storeMealTakeups;
        }
        long storeId = storeOrder.getStoreId();
        String orderId = storeOrder.getOrderId();
        int clientType = ClientTypeEnum.CASHIER.getValue();
        if (storeOrder.getTakeSerialNumber() > 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_TAKE_CODE.getValue(), DataUtil.infoWithParams("store order has already in trade take code, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        if (storeOrder.isBackOrder()) {
            storeOrder.setTakeupStatus(StoreOrderTakeupStatusEnum.NOT_RETAIN.getValue());
        } else {
            storeOrder.setTakeupStatus(StoreOrderTakeupStatusEnum.DEDUCTED.getValue());
        }
        // 通知后厨出餐，将信息发给后厨界面
        storeOrder.setTakeClientType(clientType);
        storeOrder.setTakeSerialTime(System.currentTimeMillis());
        if (storeOrder.getTakeSerialNumber() == 0) {
            // 收银台直接生成订单流水号，进入后台出餐
            int takeSerialNumber = this.getTakeSerialNumber(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getRepastDate());
            storeOrder.setTakeSerialNumber(takeSerialNumber);
            if (storeOrder.isQuickTake() || storeOrder.isDisableKitchen() || storeOrder.isBackOrder()) {
                // 快取和补录的订单，直接进入已出餐环节
                storeOrder.setTakeSerialNumber(takeSerialNumber);
                storeMealService.storeMealQuickCheckout(storeOrder);
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue(), "skip take code");
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.MEAL_CHECKOUT.getValue(), "skip meal checkout");
                if(storeOrder.getClientType() != ClientTypeEnum.MINA.getValue()) { //小程序点餐不需要完成交易
                    this.updateOrderTradeFinish(storeOrder);//评分邀请
                }
            } else {
                // 后台出餐
                storeMealTakeups = storeMealService.storeOrderTakeCode(storeOrder, takeSerialNumber);
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue(), "skip take code");
            }
        }
        return storeMealTakeups;
    }

    /**
     * 赊账订单销账
     *
     * @param merchantId
     * @param storeId
     * @param payOrderId
     * @param orderIds
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreOrder> disChargeStoreOrder(int merchantId, long storeId, String payOrderId, List<String> orderIds) throws T5weiException {
        List<StoreOrder> storeOrders = storeOrderDAO.getListInIdsForUpdate(merchantId, storeId, orderIds);
        if (storeOrders == null || storeOrders.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), "merchantId=" + merchantId + ",storeId=" + storeId + ",orderIds=" + JsonUtil.build(orderIds));
        }
        if (payOrderId == null) {
            payOrderId = "";
        }
        for (StoreOrder storeOrder : storeOrders) {
            if (storeOrder.getCreditStatus() == StoreOrderCreditStatusEnum.CHARGE.getValue()) {
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, ClientTypeEnum.CASHIER.getValue(), StoreOrderOptlogTypeEnum.CASHIER_PAY_ORDER.getValue(), "cashier discharge order");
                storeOrder.setPayOrderId(payOrderId);
                storeOrder.setPayStatus(StoreOrderPayStatusEnum.FINISH.getValue());
                storeOrder.setCreditStatus(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
                storeOrder.setUpdateTime(System.currentTimeMillis());
                storeOrder.update();
            }
        }
        return storeOrders;
    }

    /**
     * 赊账订单取消
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder cancelCreditStoreOrder(int merchantId, long storeId, String orderId) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        if (storeOrder.getCreditStatus() == StoreOrderCreditStatusEnum.CHARGE.getValue()) {
            storeOrderOptlogDAO.createOptlog(storeOrder, 0, ClientTypeEnum.CASHIER.getValue(), StoreOrderOptlogTypeEnum.CASHIER_PAY_ORDER.getValue(), "cashier cancel charge order");
            storeOrder.setCreditStatus(StoreOrderCreditStatusEnum.CANCEL_CHARGE.getValue());
            storeOrder.setUpdateTime(System.currentTimeMillis());
            storeOrder.update();
        }
        return storeOrder;
    }

    /**
     * 生成取餐号
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @return
     * @throws T5weiException
     */
    private String getTakeCode(int merchantId, long storeId, long repastDate) throws T5weiException {
        boolean enableSlave = false;
        String takeCode = null;
        for (int i = 0; i < 10; i++) {
            String genTakeCode = RandomCode.getTakeCode(6);
            StoreOrder storeOrder = storeOrderDAO.getStoreOrderByTakeCode(merchantId, storeId, repastDate, genTakeCode, enableSlave);
            if (storeOrder == null) {
                takeCode = genTakeCode;
                break;
            }
        }
        if (takeCode == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "store order getTakeCode failure...");
        }
        return takeCode;
    }

    /**
     * 未交易订单，用户可以直接操作退款 已交易订单&大额订单可能被锁定，需要提示收银员操作退款 退款相关信息保存入库，更新库存
     *
     * @param storeOrderRefundParam
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder refundStoreOrder(long staffId, StoreOrderRefundParam storeOrderRefundParam, long refundRecordId) throws T5weiException {
        int merchantId = storeOrderRefundParam.getMerchantId();
        long storeId = storeOrderRefundParam.getStoreId();
        String orderId = storeOrderRefundParam.getOrderId();
        int clientType = storeOrderRefundParam.getClientType();
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        // 信息入库
        int optType = StoreOrderOptlogTypeEnum.UNKNOWN.getValue();
        String optRemark = "";
        if (clientType == ClientTypeEnum.CASHIER.getValue()) {
            storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
            optRemark = "merchant refund all";
            optType = StoreOrderOptlogTypeEnum.CASHIER_CANCEL_ORDER.getValue();
        } else {
            // 用户退款：用户退款状态=全额退款，库存保留状态＝无保留，用户退款状态=全额退款，支付状态=未支付
            storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.USER_ALL.getValue());
            optType = StoreOrderOptlogTypeEnum.USER_CANCEL_ORDER.getValue();
            optRemark = "user refund all";
        }
        if (storeOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() || storeOrder.getRefundStatus() == StoreOrderRefundStatusEnum.USER_ALL.getValue()) {
            // 没有交易过的全额退款，恢复项目数据
            if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue()) {
                storeOrderItemPromotionDAO.updateCancelOrder(merchantId, storeId, orderId);// 恢复单品促销
                storeChargeItemPromotionService.updateSaleNum(merchantId, storeId, orderId);
                storeOrder.setTakeupStatus(StoreOrderTakeupStatusEnum.NOT_RETAIN.getValue());// 恢复库存
            }
        }
        storeOrder.setCancelOrderType(storeOrderHelper.getCancelOrderType(staffId, storeOrder));
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrderDAO.update(storeOrder);
        // 更新库存
        try {
            storeInventoryService.updateInventoryDateByOrder(storeOrder);
        } catch (Throwable e) {
            log.error("#### fail to updateInventoryByOrder ", e);
        }
        storeOrderOptlogDAO.createOptlog(storeOrder, staffId, clientType, optType, optRemark);
        return storeOrder;
    }

    /**
     * 退款失败
     *
     * @param storeOrderRefundParam
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder refundStoreOrderFailure(long staffId, StoreOrderRefundParam storeOrderRefundParam, String errorMsg) throws T5weiException {
        int merchantId = storeOrderRefundParam.getMerchantId();
        long storeId = storeOrderRefundParam.getStoreId();
        String orderId = storeOrderRefundParam.getOrderId();
        int clientType = storeOrderRefundParam.getClientType();
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        // 信息入库
        int optType = StoreOrderOptlogTypeEnum.REFUND_FAILURE.getValue();
        storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.FAILURE.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrderDAO.update(storeOrder);
        storeOrderOptlogDAO.createOptlog(storeOrder, staffId, clientType, optType, errorMsg);
        return storeOrder;
    }

    /**
     * @param storeOrder
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class)
    public StoreOrder refundStoreOrder(StoreOrder storeOrder, long staffId, StoreOrderRefundStatusEnum merchantRefund, long refundRecordId) throws T5weiException {
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), "store order not exist");
        }
        // 尚未交易状态，往下走，其他状态，返回并提示给用户
        storeOrder.setRefundStatus(merchantRefund.getValue());
        // 信息入库
        int optType = StoreOrderOptlogTypeEnum.CASHIER_REFUND_ORDER.getValue();
        String optRemark = merchantRefund.toString();
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrderDAO.update(storeOrder);
        int clientType = storeOrder.getClientType();
        storeOrderOptlogDAO.createOptlog(storeOrder, staffId, clientType, optType, optRemark);
        return storeOrder;
    }

    /**
     * @param
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class)
    public StoreOrder saveStoreOrderSiteNumber(int merchantId, long storeId, String orderId, int siteNumber) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        List<StoreMealTakeup> storeMealTakeups = storeMealTakeupDAO.getStoreMealsByOrderId(merchantId, storeId, orderId, true, false);
        if (storeMealTakeups != null) {
            for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
                storeMealTakeup.snapshot();
                storeMealTakeup.setSiteNumber(siteNumber);
                storeMealTakeup.setUpdateTime(System.currentTimeMillis());
                storeMealTakeup.update();
            }
        }
        storeMealCheckoutDAO.updateSiteNumber(merchantId, storeId, orderId, siteNumber);
        // 信息入库
        storeOrder.setSiteNumber(siteNumber);
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrderDAO.update(storeOrder);
        int optType = StoreOrderOptlogTypeEnum.ORDER_SITE_NUMBER.getValue();
        storeOrderOptlogDAO.createOptlog(storeOrder, storeOrder.getStaffId(), storeOrder.getClientType(), optType, "save storeOrder site number");
        return storeOrder;
    }

    /**
     * 取号接口（仅做客户取号操作）
     *
     * @param storeOrderTakeCodeParam
     * @throws TException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder takeCodeStoreOrder(StoreOrderTakeCodeParam storeOrderTakeCodeParam) throws TException {
        int merchantId = storeOrderTakeCodeParam.getMerchantId();
        long storeId = storeOrderTakeCodeParam.getStoreId();
        String orderId = storeOrderTakeCodeParam.getOrderId();
        int siteNumber = storeOrderTakeCodeParam.getSiteNumber();
        int clientType = storeOrderTakeCodeParam.getClientType();
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        long timingTakeTime = storeOrderTakeCodeParam.getTimingTakeTime();
        if (timingTakeTime > 0 && storeOrder.getTimingTakeTime() > 0 && timingTakeTime != storeOrder.getTimingTakeTime()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TIMING_TAKE_TIME_CHANGED.getValue(), DataUtil.infoWithParams("store order timing take time changed, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        // 已生成流水号，说明已经进入出餐环节
        if (storeOrder.getTakeSerialNumber() > 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_TAKE_CODE.getValue(), DataUtil.infoWithParams("store order has already in trade take code, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        if (!storeOrder.isPayAfter()) {
            // 先付费，确认支付状态：
            // 未支付&支付中，返回并提示给用户，已支付，则往下进行
            int payStatus = storeOrder.getPayStatus();
            if (payStatus == StoreOrderPayStatusEnum.NOT.getValue()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_NOT.getValue(), DataUtil.infoWithParams("store order do not pay, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
            }
            if (payStatus == StoreOrderPayStatusEnum.DOING.getValue()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAYING.getValue(), DataUtil.infoWithParams("store order in paying, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
            }
            // 确认退款状态
            // 商户或用户全额退款，返回并提示给用户
            // 未退款&部分退款，往下走
            int refundStatus = storeOrder.getRefundStatus();
            if (refundStatus == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() || refundStatus == StoreOrderRefundStatusEnum.USER_ALL.getValue()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND.getValue(), DataUtil.infoWithParams("store order has already refund, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
            }
        }
        // 确认交易状态，尚未交易状态，则可以取号，往下走；其他状态，返回并提示给用户
        int tradeStatus = storeOrder.getTradeStatus();
        if (tradeStatus == StoreOrderTradeStatusEnum.SENTED.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_SEND_OUT.getValue(), DataUtil.infoWithParams("store order has already in trade send out, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        } else if (tradeStatus == StoreOrderTradeStatusEnum.CODE_TAKED.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_TAKE_CODE.getValue(), DataUtil.infoWithParams("store order has already in trade take code, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        } else if (tradeStatus == StoreOrderTradeStatusEnum.FINISH.getValue()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_FINISH.getValue(), DataUtil.infoWithParams("store order has already trade finish, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        if (siteNumber > 0) {
            storeOrder.setSiteNumber(siteNumber);
        }
        storeOrder.setTakeupStatus(StoreOrderTakeupStatusEnum.DEDUCTED.getValue());
        storeOrder.setUpdateTime(System.currentTimeMillis());
        if (storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
            storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        }
        // 通知后厨出餐，将信息发给后厨界面
        storeOrder.setTakeSerialTime(System.currentTimeMillis());
        if (storeOrder.getTakeSerialNumber() == 0) {
            // 生成订单流水号
            int takeSerialNumber = this.getTakeSerialNumber(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getRepastDate());
            storeOrder.setTakeSerialNumber(takeSerialNumber);
            storeOrder.setTakeClientType(clientType);
            int orderTakeMode = storeOrder.getTakeMode();
            // 不是快取，则以前端输入为准
            if (orderTakeMode != StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
                storeOrder.setTakeMode(storeOrderTakeCodeParam.getTakeMode().getValue());
            }
            if (orderTakeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
                // 快取
                storeOrder.setTakeSerialNumber(takeSerialNumber);
                storeMealService.storeMealQuickCheckout(storeOrder);
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue(), "quick take code");
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.MEAL_CHECKOUT.getValue(), "quick meal checkout");
                this.updateOrderTradeFinish(storeOrder);
            } else {
                // 信息入库：交易状态=已取号、流水号、取餐模式
                if (storeOrder.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue()) {
                    storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.WORKIN.getValue());
                } else {
                    storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.CODE_TAKED.getValue());
                }
                List<StoreMealTakeup> storeMealTakeups =  storeMealService.storeOrderTakeCode(storeOrder, takeSerialNumber);
                storeOrder.setStoreMealTakeups(storeMealTakeups);
                storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue(), "user take code");
            }
        }
        storeOrderDAO.update(storeOrder);
        // 更新库存
        try {
            storeInventoryService.updateInventoryDateByOrder(storeOrder);
        } catch (Throwable e) {
            log.error("#### fail to updateInventoryByOrder ", e);
        }
        return storeOrder;
    }

    /**
     * 定时取设置
     *
     * @param storeOrderTimingTakeCodeParam
     * @return
     * @throws T5weiException
     * @throws TException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder timingTakeCodeStoreOrder(StoreOrderTimingTakeCodeParam storeOrderTimingTakeCodeParam) throws T5weiException, TException {
        int merchantId = storeOrderTimingTakeCodeParam.getMerchantId();
        long storeId = storeOrderTimingTakeCodeParam.getStoreId();
        String orderId = storeOrderTimingTakeCodeParam.getOrderId();
        long timingTakeTime = storeOrderTimingTakeCodeParam.getTimingTakeTime();
        int clientType = storeOrderTimingTakeCodeParam.getClientType();
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        if (storeOrder.getTakeSerialNumber() > 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_PROCESS.getValue(), DataUtil.infoWithParams("store order has taked code, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        // 未取餐的订单可以设置定时取时间
        storeOrder.snapshot();
        storeOrder.setTimingTakeTime(timingTakeTime);
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrder.update();
        storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, StoreOrderOptlogTypeEnum.TIMING_TAKE.getValue(), "set timingTakeTime=" + timingTakeTime);
        return storeOrder;
    }

    /**
     * 流水号
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @return
     */
    public int getTakeSerialNumber(int merchantId, long storeId, long repastDate) {
        repastDate = DateUtil.getBeginTime(repastDate, null);
        StoreOrderNumber storeOrderNumber = storeOrderNumberDAO.getStoreOrderNumberById(merchantId, storeId, repastDate, true, true);
        if (storeOrderNumber == null) {
            int number = 1;// 出餐流水号起始值
            Store5weiSetting store5weiSetting = store5weiSettingDAO.getById(merchantId, storeId, false);
            if (store5weiSetting != null) {
                number = store5weiSetting.getSerialNumberStartBySiteNumber();
                if (number <= 0) {
                    number = 1;
                }
            }
            storeOrderNumber = new StoreOrderNumber();
            storeOrderNumber.setMerchantId(merchantId);
            storeOrderNumber.setStoreId(storeId);
            storeOrderNumber.setRepastDate(repastDate);
            storeOrderNumber.setTakeSerialNumber(number);
            storeOrderNumberDAO.create(storeOrderNumber);
            return number;
        } else {
            int number = storeOrderNumber.getTakeSerialNumber();
            number = number + 1;
            storeOrderNumber.setTakeSerialNumber(number);
            storeOrderNumberDAO.update(storeOrderNumber);
            return number;
        }
    }

    public void changeOrdersUser(int merchantId, long storeId, long srcUserId, long destUserId) throws TException {
        this.storeOrderDAO.changeOrdersUser(merchantId, storeId, srcUserId, destUserId);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateStoreOrderNumber(int merchantId, long storeId, int serialNumber) {
        long repastDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        StoreOrderNumber storeOrderNumber = storeOrderNumberDAO.getStoreOrderNumberById(merchantId, storeId, repastDate, true, true);
        if (storeOrderNumber == null) {
            return;
        }
        int takeSerialNumber = storeOrderNumber.getTakeSerialNumber();
        if (takeSerialNumber < serialNumber) {
            storeOrderNumber.setTakeSerialNumber(serialNumber);
            storeOrderNumber.update();
        }
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder changeStoreOrderTakeMode(int merchantId, long storeId, String orderId, int takeMode, int clientType) throws TException {
        if (takeMode == 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), DataUtil.infoWithParams("takeMode is invalid, takeMode=#1 ", new Object[]{takeMode}));
        }
        if (clientType == 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), DataUtil.infoWithParams("clientType is invalid, clientType=#1 ", new Object[]{clientType}));
        }
        StoreOrderSwitch storeOrderSwitch = storeOrderSwitchDAO.getStoreOrderSwitchById(merchantId, storeId, orderId);
        // 一个订单只能被更改一次取餐方式
        if (storeOrderSwitch != null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_SWITCH_ERROR.getValue(), DataUtil.infoWithParams("storeOrderSwitch already exists, merchantId=#1，storeId=#2, orderId=#3 ", new Object[]{merchantId, storeId, orderId}));
        }
        storeOrderSwitch = new StoreOrderSwitch();
        // 获取订单详情
        StoreOrder storeOrder = this.getStoreOrderDetailById(merchantId, storeId, orderId);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        StoreOrder storeOrderCount = BeanUtil.copy(storeOrder, StoreOrder.class);
        // 构造订单变更记录对象
        String orderSwitchId = StoreOrderSwitch.getOrderSwitchId(orderId);
        // 订单价格
        long prePrice = storeOrderCount.getPayablePrice();// 变更前的价格
        long afterPrice = 0;
        int switchType = StoreOrderSwitchTypeEnum.UNKNOWN.getValue();

        long packageFee = 0;
        if (takeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
            switchType = StoreOrderSwitchTypeEnum.SWITCH_TO_EATIN.getValue();
        } else if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
            switchType = StoreOrderSwitchTypeEnum.SWITCH_TO_PACKAGE.getValue();
            List<StoreOrderItem> storeOrderItems = storeOrderCount.getStoreOrderItems();
            double packageAmount = 0;
            for (StoreOrderItem storeOrderItem : storeOrderItems) {
                packageAmount = storeOrderItem.getAmount();
                long packagePrice = MoneyUtil.mul(storeOrderItem.getPackagePrice(), packageAmount);
                packageFee = MoneyUtil.add(packageFee, packagePrice);
            }
        }
        storeOrderCount.setTakeMode(takeMode);
        storeOrderCount.setPackageFee(packageFee);
        afterPrice = storeOrderCount.getPayablePrice();// 变更后的价格
        long diffPrice = MoneyUtil.sub(afterPrice, prePrice);
        int processType = 0;
        int payStatus = StoreOrderPayStatusEnum.UNKNOWN.getValue();
        int refundStatus = StoreOrderRefundStatusEnum.UNKNOWN.getValue();
        if (diffPrice < 0) {
            processType = StoreOrderSwitchProcessTypeEnum.REFUND.getValue();
            refundStatus = StoreOrderRefundStatusEnum.NOT.getValue();
        }
        if (diffPrice > 0) {
            processType = StoreOrderSwitchProcessTypeEnum.PAY.getValue();
            payStatus = StoreOrderPayStatusEnum.NOT.getValue();
        }
        int processStatus = StoreOrderSwitchProcessStatusEnum.NOT.getValue();
        String payOrderId = storeOrder.getPayOrderId();
        long createTime = System.currentTimeMillis();

        storeOrderSwitch.setOrderSwitchId(orderSwitchId);
        storeOrderSwitch.setMerchantId(merchantId);
        storeOrderSwitch.setStoreId(storeId);
        storeOrderSwitch.setOrderId(orderId);
        storeOrderSwitch.setOrderId(payOrderId);
        storeOrderSwitch.setSwitchType(switchType);
        storeOrderSwitch.setPrePrice(prePrice);
        storeOrderSwitch.setDiffPrice(diffPrice);
        storeOrderSwitch.setProcessType(processType);
        storeOrderSwitch.setProcessStatus(processStatus);
        storeOrderSwitch.setPayStatus(payStatus);
        storeOrderSwitch.setRefundStatus(refundStatus);
        storeOrderSwitch.setPayOrderId(payOrderId);
        storeOrderSwitch.setCreateTime(createTime);

        int optType = StoreOrderOptlogTypeEnum.ORDER_SWITCH_TAKE_MODE.getValue();
        String optRemark = "order switch to takeMode=" + takeMode;
        // 创建变更记录
        storeOrderSwitchDAO.create(storeOrderSwitch);
        // 变更取餐方式
        storeOrder.setTakeMode(takeMode);
        storeOrder.setUpdateTime(System.currentTimeMillis());
        storeOrder.update();
        // 创建变更订单取餐方式操作日志
        storeOrderOptlogDAO.createOptlog(storeOrder, storeOrder.getUserId(), clientType, optType, optRemark);
        storeOrder.setStoreOrderSwitch(storeOrderSwitch);
        return storeOrder;
    }

    /**
     * 订单更新后 需要修改统计库 stat
     *
     * @param storeOrder
     */
    public void orderUpdateEvent(StoreOrder storeOrder) {
        // 订单顾客或店员手动输入入客数
        List<StoreOrderUpdateDTO> storeOrderUpdateDTOs = Lists.newArrayList();
        StoreOrderUpdateDTO storeOrderUpdateDTO = new StoreOrderUpdateDTO();
        BeanUtil.copy(storeOrder, storeOrderUpdateDTO);
        storeOrderUpdateDTOs.add(storeOrderUpdateDTO);

        int updateTime = 1;
        boolean error = false;
        while (!error) {
            try {
                if (updateTime > 3) {
                    break;
                }
                storeOrderUpdateFacade.updateStoreOrders(storeOrderUpdateDTOs);
                break;
            } catch (TException e) {
                log.error("update order to m-stat db error , time = " + updateTime);
                updateTime += 1;
                e.printStackTrace();
            }
        }
    }

    /**
     * 统计客户指定营业时段在某餐台的就餐数量
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param userId
     * @param siteNumber
     * @return
     */
    public int countDishStoreOrder(int merchantId, long storeId, long repastDate, long timeBucketId, long userId, int siteNumber) {
        boolean enableSlave = true;
        return storeOrderDAO.countDishStoreOrder(merchantId, storeId, repastDate, timeBucketId, userId, enableSlave);
    }

    /**
     * 修改订单入客数
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param customerTraffic
     * @param staffId
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder updateStoreOrderCustomerTraffic(int merchantId, long storeId, String orderId, int customerTraffic, long staffId) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, true, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
        storeOrder.setCustomerTraffic(customerTraffic);
        storeOrder.setEnableManualCustomerTraffic(true);
        storeOrder.update();
        storeOrderOptlogDAO.createOptlog(storeOrder, staffId, ClientTypeEnum.CASHIER.getValue(), StoreOrderOptlogTypeEnum.ORDER_SITE_NUMBER.getValue(), "modify customerTraffic");
        // 更新统计库
        orderUpdateEvent(storeOrder);
        return storeOrder;
    }

    public void updateOrder4Pay(StoreOrderPayParam storeOrderPayParam) {
        this.storeOrderDAO.updateUserRemark(storeOrderPayParam.getMerchantId(), storeOrderPayParam.storeId, storeOrderPayParam.getOrderId(), storeOrderPayParam.getUserRemark());
    }

    /**
     * 变更库存回退数量
     *
     * @param merchantId 商编
     * @param storeId    店铺id
     */
    public void updateSubItemQuitAmount(int merchantId, long storeId, List<StoreOrderRefundItem> storeOrderRefundItems) {
        if (storeOrderRefundItems == null || storeOrderRefundItems.isEmpty()) {
            return;
        }
        List<StoreOrderSubitem> storeOrderSubitemList = Lists.newArrayList();
        for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems) {
            if (!storeOrderRefundItem.isRestoreInventory()) {
                continue;
            }
            String orderId = storeOrderRefundItem.getOrderId();
            long chargeItemId = storeOrderRefundItem.getChargeItemId();
            double amount = storeOrderRefundItem.getAmount();
            List<StoreOrderSubitem> subitemList = storeOrderSubitemDAO.getStoreOrderSubitemById(merchantId, storeId, orderId, chargeItemId);
            for (StoreOrderSubitem storeOrderSubitem : subitemList) {
                double refundAmount = storeOrderSubitem.getAmount();
                BigDecimal bDAmount = new BigDecimal(String.valueOf(amount));
                BigDecimal bDRefundAmount = new BigDecimal(String.valueOf(refundAmount));
                storeOrderSubitem.setInvQuitAmount(storeOrderSubitem.getInvQuitAmount() + bDAmount.multiply(bDRefundAmount).setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue());
            }
            storeOrderSubitemList.addAll(subitemList);
        }
        storeOrderSubitemDAO.batchUpdateInvQuitAmount(merchantId, storeId, storeOrderSubitemList);
    }

    /**
     * 组装充值卡交易信息
     */
    public String buildTransInfo4PrepaidCard(int merchantId) throws TException {
        MerchantDTO merchant = merchantFacade.getMerchant(merchantId);
        return merchant.getName() + "充值卡";
    }

    /**
     * 组装微信下单信息到Map中
     */
    public Map<String, String> buildWechatPayInfo(DefineComPayResult defineComPayResult, StoreOrderPayModeEnum otherPayMode) {
        Map<String, String> orderPayInfo = new HashMap<>();
        if (otherPayMode.equals(StoreOrderPayModeEnum.WECHART_PAY)) {
            orderPayInfo.put("prepay_id", defineComPayResult.getPrepayId());
            orderPayInfo.put("app_id", defineComPayResult.getAppid());
            orderPayInfo.put("timestamp", String.valueOf(defineComPayResult.getTimestamp()));
            orderPayInfo.put("nonce_str", defineComPayResult.getNonceStr());
            orderPayInfo.put("sign_type", defineComPayResult.getSignType());
            orderPayInfo.put("sign", defineComPayResult.getSign());
        }
        return orderPayInfo;
    }

    /**
     * 订单支付,买充值卡信息log
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder savePay4BuyPrepaidLog(int merchantId, long storeId, String orderId, int optType, String logInfo) throws T5weiException {
        StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, false, true);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2", new Object[]{storeId, orderId}));
        }
        int clientType = storeOrder.getClientType();
        storeOrderOptlogDAO.createOptlog(storeOrder, 0, clientType, optType, logInfo);
        return storeOrder;
    }

    /**
     * 处理买充值卡失败
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void processBuyPrepaidcardFail(StoreOrderCombinedBiz storeOrderCombinedBiz, StoreOrder storeOrder) throws T5weiException {
        //记录错误码和错误信息
        try {
            this.storeOrderCombinedBizDao.create(storeOrderCombinedBiz);
            //买卡失败,把订单修改成传统订单
            storeOrder.updateCombineBizType(StoreOrderCombinedBizType.TRADITION.getValue());
            //记录操作日志,买充值卡失败
            this.savePay4BuyPrepaidLog(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getOrderId(), StoreOrderOptlogTypeEnum.COMBINED_BUY_PREPAIDCARD_FAIL.getValue(), "buy prepaidcard fail");
        } catch (DuplicateKeyException e){}
    }

    /**
     * 处理买充值卡成功
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void processBuyPrepaidcardSuccess(StoreOrderCombinedBiz storeOrderCombinedBiz, int merchantId, long storeId, String orderId) throws T5weiException {
        storeOrderCombinedBiz.init();
        try {
            this.storeOrderCombinedBizDao.create(storeOrderCombinedBiz);
            //记录操作日志,买充值卡成功
            this.savePay4BuyPrepaidLog(merchantId, storeId, orderId, StoreOrderOptlogTypeEnum.COMBINED_BUY_PREPAIDCARD_SUCCESS.getValue(), "buy prepaidcard success");
        }catch (DuplicateKeyException e){}
    }

    public StoreOrderDelivery getStoreOrderDelivery(int merchant, long storeId, String orderId) {
        return this.storeOrderDAO.getDeliveryById(merchant, storeId, orderId, false, false);
    }
    
    /**
     * 更新订单用户备注
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param userRemark
     * @throws T5weiException 
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreOrder updateUserRemark (int merchantId, long storeId, String orderId, String userRemark) throws T5weiException {
    	StoreOrder storeOrder = this.getStoreOrderById4Update(merchantId, storeId, orderId);
    	storeOrder.setUserRemark(userRemark);
    	storeOrder.setUpdateTime(System.currentTimeMillis());
    	storeOrder.update();
    	return storeOrder;
    }

}
