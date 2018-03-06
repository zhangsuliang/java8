package com.huofu.module.i5wei.order.facade;

import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.delivery.service.StoreDeliverySettingService;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.dao.StoreOrderDeliveryDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.setting.service.StorePresellService;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.coupon.Coupon4CommonFacade;
import huofucore.facade.coupon.CouponTypeDTO;
import huofucore.facade.coupon.ErrorCodeEnum;
import huofucore.facade.coupon.TCouponException;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.merchant.store.*;
import huofucore.facade.merchant.store.query.StoreQueryFacade;
import huofucore.facade.user.info.FacadeUserInvalidException;
import huofucore.facade.user.info.UserDTO;
import huofucore.facade.user.info.UserFacade;
import huofucore.facade.user.info.UserTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class StoreOrderFacadeValidate {

	private static final int mills_in_day = 24 * 60 * 60 * 1000;

	@Autowired
	private StoreOrderService storeOrderService;
	
	@Autowired
	private StoreInventoryService storeInventoryService;
	
	@Autowired
    private StoreChargeItemService storeChargeItemService;

	@Autowired
	private StoreDeliverySettingService storeDeliverySettingService;

	@Autowired
	private StoreTimeBucketDAO storeTimeBucketDAO;

	@Autowired
	private StoreProductDAO storeProductDAO;

	@Autowired
	private StoreOrderDeliveryDAO storeOrderDeliveryDAO;

	@Autowired
	private Store5weiSettingService store5weiSettingService;

	@ThriftClient
	private StoreQueryFacade.Iface storeQueryFacade;

	@Autowired
	private StorePresellService storePresellService;

	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@ThriftClient
	private StoreSettingFacade.Iface storeSettingFacade;
	
	@ThriftClient
	private UserFacade.Iface userFacade;
	
	@ThriftClient
    private Coupon4CommonFacade.Iface coupon4CommonFacade;
	
    /**
     * 在支持用户指定预约时间的情况下，检测用户指定的预约时间是否是在外送提前时间之外
     *
     * @param timeBucketStartTime    营业时段的开始时间
     * @param timeBucketEndTime      营业时段的结束时间
     * @param userDeliveryAssignTime 用户指定的预约时间
     * @param aheadTime              预约提前时间量
     * @return true:时间合法(只要指定的预约时间超过当前时间+外送提前时间就可以)
     */
    public boolean testDeliveryAssignTime(long timeBucketStartTime, long timeBucketEndTime, long userDeliveryAssignTime, long aheadTime) {
        long now = System.currentTimeMillis();
        //预约时间 < 营业开始时间
        if (userDeliveryAssignTime < timeBucketStartTime) {
            return false;
        }
        //判断下单时间与预约时间的时间差必须是外送提前时间(预约时间 - 下单时间 < 外送提前时间)
        if (now + aheadTime > userDeliveryAssignTime) {
            return false;
        }
        //最晚的预约时间必须是营业时段结束时间和外送提前时间的和
        if (userDeliveryAssignTime > timeBucketEndTime + aheadTime) {
            return false;
        }
        return true;
    }
    
    public void placeStoreOrderValidate(StoreOrderPlaceParam storeOrderPlaceParam, StoreTimeBucket storeTimeBucket) throws TException {
        List<String> errorMsgs = new ArrayList<String>();
        if (storeOrderPlaceParam.getStoreId() == 0) {
            errorMsgs.add("StoreId is null");
        }
        if (storeOrderPlaceParam.getRepastDate() == 0) {
            errorMsgs.add("RepastDate is null");
        }
        if (storeOrderPlaceParam.getTimeBucketId() == 0) {
            errorMsgs.add("TimeBucketId is null");
        }
        if (storeOrderPlaceParam.getStaffId() == 0 && storeOrderPlaceParam.getUserId() == 0) {
            errorMsgs.add("StaffId or UserId at least have one");
        }
        if (storeOrderPlaceParam.getChargeItems() == null || storeOrderPlaceParam.getChargeItems().isEmpty()) {
            errorMsgs.add("StoreOrder ChargeItems is null");
        }
        if (storeOrderPlaceParam.getTotalDerate() < 0) {
            errorMsgs.add("StoreOrder totalDerate can not less 0");
        }
        if (DataUtil.isNotEmpty(storeOrderPlaceParam.getUserRemark()) && storeOrderPlaceParam.getUserRemark().length() > 50) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "userRemark[" + storeOrderPlaceParam.getUserRemark() + "] length must <=50");
        }
        try {
            StoreDTO storeDTO = this.storeQueryFacade.getStore(storeOrderPlaceParam.getMerchantId(), storeOrderPlaceParam.getStoreId());
            if (storeDTO.getStatus() == StoreStatusDTO.CLOSE.getValue()) {
                errorMsgs.add("store is close status");
            }
        } catch (FacadeStoreInvalidException e) {
            errorMsgs.add("store invlid");
        }
        int clientType = storeOrderPlaceParam.getClientType();
        int takeMode = storeOrderPlaceParam.getTakeMode();
        int merchantId = storeOrderPlaceParam.getMerchantId();
        long storeId = storeOrderPlaceParam.getStoreId();
        if (clientType != ClientTypeEnum.CASHIER.getValue() && storeOrderPlaceParam.getUserId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), " user place order , userId is null ");
        }
        //就餐日期&营业时段与预售日期判断
        if (clientType != ClientTypeEnum.CASHIER.getValue()) {
            this.checkStoreOrderTimeBucket(merchantId, storeId, storeOrderPlaceParam.getRepastDate(), storeTimeBucket, takeMode);
        }
        // 点餐备注大小限制
        String orderRemark = storeOrderPlaceParam.getOrderRemark();
        if (orderRemark == null) {
            orderRemark = "";
        }
        if (orderRemark.length() > 200) {
            orderRemark = orderRemark.substring(0, 200);// 超过200个字符做截断
        }
        storeOrderPlaceParam.setOrderRemark(orderRemark);
        Double allAmount = 0D;
        Double allPackedAmount = 0D;
        Set<Long> chargeItemIdSet = new HashSet<Long>();
        if (storeOrderPlaceParam.getChargeItems() != null && !storeOrderPlaceParam.getChargeItems().isEmpty()) {
            for (StoreOrderPlaceItemParam item : storeOrderPlaceParam.getChargeItems()) {
                StringBuffer str = new StringBuffer();
                if (item.getChargeItemId() == 0) {
                    str.append("ChargeItemId is null ");
                }
                if (item.getAmount() == 0) {
                    str.append("ChargeItemId=" + item.getChargeItemId() + ", amount = 0 ");
                }
                if (!str.toString().isEmpty()) {
                    errorMsgs.add(str.toString());
                }
                //处理用户自助点餐的打包份数
                if (clientType != ClientTypeEnum.CASHIER.getValue()) {
                    //客户端点餐的打包和外送打包数量处理
                    if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                        item.setPackedAmount(item.getAmount());
                    }
                }
                chargeItemIdSet.add(item.getChargeItemId());
                //计算下单份数
                
                allAmount = NumberUtil.add(allAmount, item.getAmount());
				allPackedAmount = NumberUtil.add(allPackedAmount, item.getPackedAmount());
			}
		}
		if (chargeItemIdSet.size() != storeOrderPlaceParam.getChargeItems().size()) {
			errorMsgs.add("chargeItemId Repeat");
		}
		// 取餐方式未知的计算
		if (takeMode == StoreOrderTakeModeEnum.UNKNOWN.getValue()) {
			if (allPackedAmount.compareTo(0D) == 0) {
				takeMode = StoreOrderTakeModeEnum.DINE_IN.getValue();
			} else if (allPackedAmount.compareTo(allAmount) == 0) {
				takeMode = StoreOrderTakeModeEnum.TAKE_OUT.getValue();
			} else {
				takeMode = StoreOrderTakeModeEnum.IN_AND_OUT.getValue();
			}
			storeOrderPlaceParam.setTakeMode(takeMode);
		}
		int siteNumber = storeOrderPlaceParam.getSiteNumber();
		siteNumber = this.validateSiteNumber(storeOrderPlaceParam.getMerchantId(), storeOrderPlaceParam.getStoreId(), storeOrderPlaceParam.getOrderId(), siteNumber);
		storeOrderPlaceParam.setSiteNumber(siteNumber);
		if (!errorMsgs.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), JsonUtil.build(errorMsgs));
        }
	}
    
	public void validateDelivery(StoreOrderDeliveryParam storeOrderDeliveryParam, StoreOrder storeOrder) throws T5weiException {
		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting(storeOrderDeliveryParam.getMerchantId(), storeOrderDeliveryParam.getStoreId());
		// 如果订单是外送，但是店铺不支持外送
		long repastDate = storeOrder.getRepastDate();
		if (!storeDeliverySetting.isDeliverySupported()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_NOT_SUPPORTED.getValue(), "storeId[" + storeOrderDeliveryParam.getStoreId() + "] delivery not be supported");
		}
		long timeBucketId = storeOrder.getTimeBucketId();
		StoreTimeBucket storeTimeBucket = this.storeTimeBucketDAO.loadById(storeOrderDeliveryParam.getMerchantId(), storeOrderDeliveryParam.getStoreId(), timeBucketId, false, false);
		if (!storeTimeBucket.isDeliverySupported()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_TIMEBUCKET_DELIVERY_NOT_SUPPORTED.getValue(), "storeId[" + storeTimeBucket.getStoreId() + "] timeBucketId[" + storeTimeBucket.getTimeBucketId() + "] delivery not supported");
		}
		if (!storeDeliverySetting.isDeliveryAssignTimeSupported()) {
			long time = Math.max(storeTimeBucket.getDeliveryStartTimeForDate(repastDate), System.currentTimeMillis() + storeDeliverySetting.getAheadTime());
			storeOrderDeliveryParam.setDeliveryAssignTime(time);
		}
		if (!testDeliveryAssignTime(storeTimeBucket.getDeliveryStartTimeForDate(repastDate), storeTimeBucket.getDeliveryEndTimeForDate(repastDate), storeOrderDeliveryParam.getDeliveryAssignTime(), storeDeliverySetting.getAheadTime())) {
			throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_ASSIGN_TIME_INVALID.getValue(), "deliveryAssignTime[" + storeOrderDeliveryParam.getDeliveryAssignTime() + "] invalid 0");
		}
	}

	public void validateDeliveryForPlaceOrder(StoreOrderPlaceParam storeOrderPlaceParam, StoreTimeBucket storeTimeBucket) throws T5weiException {

		int merchantId = storeOrderPlaceParam.getMerchantId();
		long storeId = storeOrderPlaceParam.getStoreId();
		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting(merchantId, storeId);
		// 如果订单是外送，但是店铺不支持外送
		if (!storeDeliverySetting.isDeliverySupported()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_NOT_SUPPORTED.getValue(), "storeId[" + storeId + "] delivery not be supported");
		}
		if (!storeTimeBucket.isDeliverySupported()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_TIMEBUCKET_DELIVERY_NOT_SUPPORTED.getValue(), "storeId[" + storeTimeBucket.getStoreId() + "] timeBucketId[" + storeTimeBucket.getTimeBucketId() + "] delivery not supported");
		}
		//按外送的结束时间判断用户是否能够点餐
		long now = System.currentTimeMillis();
        long repastDateTimeMillis = DateUtil.getBeginTime(storeOrderPlaceParam.getRepastDate(), null);
        long currentTimeBeginMillis = DateUtil.getBeginTime(now, null);
    	long deliveryEndTime = storeTimeBucket.getDeliveryEndTimeForBiz();//当天
    	if (repastDateTimeMillis == currentTimeBeginMillis) {
    		if(now - repastDateTimeMillis > deliveryEndTime){
    			throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_DAY_BIGGER_THEN_PRESELL_DAY.getValue(), 
    					DataUtil.infoWithParams("store delivery repastDateTimeMillis < currentTimeBeginMillis, storeId=#1, repastDate=#2", new Object[]{storeId, repastDateTimeMillis}));//超过外送时间
    		}
    	}else if (repastDateTimeMillis < currentTimeBeginMillis) {//跨天
    		MutableDateTime mdt = new MutableDateTime(now);
    		if(storeTimeBucket.getDeliveryEndTimeForBiz() > mills_in_day){
    			MutableDateTime mdt2 = new MutableDateTime(repastDateTimeMillis + storeTimeBucket.getDeliveryEndTimeForBiz());
    			mdt.add(-mdt2.getMinuteOfDay());
    		}
    		long timeBeginMillis = DateUtil.getBeginTime(mdt.getMillis(), null);
    		if(repastDateTimeMillis == timeBeginMillis && (now - repastDateTimeMillis) <= storeTimeBucket.getDeliveryEndTimeForBiz()){
    		}else{
    			//小于当日的，不能点餐
    			throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_DAY_BIGGER_THEN_PRESELL_DAY.getValue(),
    					DataUtil.infoWithParams("store delivery repastDateTimeMillis < currentTimeBeginMillis, storeId=#1, repastDate=#2", new Object[]{storeId, repastDateTimeMillis}));
    		}
    	}else{//不能超过预售日期
    		long presellTimeMillis = storePresellService.getStorePresellTimeMillis(merchantId, storeId);
    		if (repastDateTimeMillis > presellTimeMillis) {
    			//大于当日的，不能超过预售日期
    			throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_DAY_BIGGER_THEN_PRESELL_DAY.getValue(),
    					DataUtil.infoWithParams("store repastDateTimeMillis > presellTimeMillis, storeId=#1, repastDate=#2", new Object[]{storeId, repastDateTimeMillis}));
    		}
    	}
	}

    /**
     * 检查营业时段
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param storeTimeBucket
     * @throws T5weiException
     */
    public void checkStoreOrderTimeBucket(int merchantId, long storeId, long repastDate, StoreTimeBucket storeTimeBucket, int takeMode) throws T5weiException {
        long now = System.currentTimeMillis();
        long repastDateTimeMillis = DateUtil.getBeginTime(repastDate, null);
        long currentTimeBeginMillis = DateUtil.getBeginTime(now, null);
        if(takeMode != StoreOrderTakeModeEnum.SEND_OUT.getValue()){
	    	//客户端点餐需要限制时间
	    	if (repastDateTimeMillis == currentTimeBeginMillis) {
	    		if (storeTimeBucket.compareTo(now) < 0) {
	    			//当日的，点餐不能点过去的营业时段
	    			 throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_DAY_BIGGER_THEN_PRESELL_DAY.getValue(),
	    					DataUtil.infoWithParams("store storeTimeBucket.compareTo(now) < 0, storeId=#1, repastDate=#2", new Object[]{storeId, repastDateTimeMillis}));
	    		}
	    	} else if (repastDateTimeMillis < currentTimeBeginMillis) {
	    		MutableDateTime mdt = new MutableDateTime(now);
	    		if (storeTimeBucket.getEndTime() > mills_in_day) {
	    			MutableDateTime mdt2 = new MutableDateTime(repastDateTimeMillis + storeTimeBucket.getEndTime());
	    			mdt.addMinutes(-mdt2.getMinuteOfDay());
	    		}
	    		long timeBeginMillis = DateUtil.getBeginTime(mdt.getMillis(), null);
	    		if (repastDateTimeMillis == timeBeginMillis && storeTimeBucket.compareTo(now) == 0) {
	    			//减去跨天的小时数后与就餐日期相等且在营业时段的可以下单
	    		} else {
	    			//小于当日的，不能点餐
	    			throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_DAY_BIGGER_THEN_PRESELL_DAY.getValue(),
	    					DataUtil.infoWithParams("store repastDateTimeMillis < currentTimeBeginMillis, storeId=#1, repastDate=#2", new Object[]{storeId, repastDateTimeMillis}));
	    		}
	    	} else {
	    		long presellTimeMillis = storePresellService.getStorePresellTimeMillis(merchantId, storeId);
	    		if (repastDateTimeMillis > presellTimeMillis) {
	    			//大于当日的，不能超过预售日期
	    			throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_DAY_BIGGER_THEN_PRESELL_DAY.getValue(),
	    					DataUtil.infoWithParams("store repastDateTimeMillis > presellTimeMillis, storeId=#1, repastDate=#2", new Object[]{storeId, repastDateTimeMillis}));
	    		}
	    	}
        }
    }

	public StoreOrder payStoreOrderValidate(StoreOrderPay5weiParam storeOrderPay5weiParam, StoreOrder storeOrder) throws T5weiException {
		// 检查输入参数（订单号）
		int merchantId = storeOrderPay5weiParam.getMerchantId();
		long storeId = storeOrderPay5weiParam.getStoreId();
		String orderId = storeOrderPay5weiParam.getOrderId();
		if (merchantId == 0 || storeId == 0) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "store_id can not null");
		}
		if (orderId == null || orderId.isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ID_CAN_NOT_NULL.getValue(), "store order_id can not null");
		}
		if (DataUtil.isNotEmpty(storeOrderPay5weiParam.getUserRemark()) && storeOrderPay5weiParam.getUserRemark().length() > 50) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "userRemark[" + storeOrderPay5weiParam.getUserRemark() + "] length must <=50");
		}
		// 确认支付状态：
		// 已支付，返回并提示给界面
		if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_FINISH.getValue(), DataUtil.infoWithParams("store order already pay finish, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAYING.getValue(), DataUtil.infoWithParams("store order paying, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 确认退款状态
		// 商户或用户全额退款，返回并提示给用户
		// 未退款&部分退款，往下走
		if (storeOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() || storeOrder.getRefundStatus() == StoreOrderRefundStatusEnum.USER_ALL.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND.getValue(), DataUtil.infoWithParams("store order already refund finish, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if (storeOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_PART.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_PART.getValue(), DataUtil.infoWithParams("store order already refund part, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
			boolean enableSlave = false;
			StoreOrderDelivery storeOrderDelivery = this.storeOrderDeliveryDAO.getById(merchantId, storeId, orderId, enableSlave);
			if (storeOrderDelivery == null) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), "storeId[" + storeId + "] orderId[" + orderId + "] delivery invalid");
			}
		}
		// 确认库存保留状态
		//判断是否为外卖订单,0不是外卖订单，大于0说明是外卖订单，不进行库存判断，edit by Jemon 20161109
		if (storeOrder.getWaimaiType() == 0) {
			storeInventoryService.checkInventoryForOrderPlace(merchantId, storeId, storeOrder);
		}
		return storeOrder;
	}

	public StoreOrder refundStoreOrderValidate(StoreOrderRefundParam storeOrderRefundParam, StoreOrder storeOrder) throws T5weiException {
		int merchantId = storeOrderRefundParam.getMerchantId();
		long storeId = storeOrderRefundParam.getStoreId();
		String orderId = storeOrderRefundParam.getOrderId();
		int clientType = storeOrderRefundParam.getClientType();
		if (merchantId == 0 || storeId == 0) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "store id can not null");
		}
		if (orderId == null || orderId.isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ID_CAN_NOT_NULL.getValue(), "store order_id can not null");
		}
		// 确认支付状态：
		// 已支付，则往下进行
		// 未支付&支付中不允许退款，返回并提示给界面
		int payStatus = storeOrder.getPayStatus();
		if (payStatus == StoreOrderPayStatusEnum.NOT.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_NOT.getValue(), DataUtil.infoWithParams("store order do not pay, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if (payStatus == StoreOrderPayStatusEnum.DOING.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAYING.getValue(), DataUtil.infoWithParams("store order in paying, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if(storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue() && storeOrder.isOrderLocked()){
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_LOCKED.getValue(), DataUtil.infoWithParams("store order locked, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 确认退款状态
		// 商户或用户全额退款，返回并提示给用户
		// 未退款&部分退款，往下走
		int refundStatus = storeOrder.getRefundStatus();
		if (refundStatus == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() || refundStatus == StoreOrderRefundStatusEnum.USER_ALL.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND.getValue(), DataUtil.infoWithParams("store order has already refund, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 尚未交易状态，往下走，其他状态，返回并提示给用户
		int tradeStatus = storeOrder.getTradeStatus();
		if (clientType != ClientTypeEnum.CASHIER.getValue()) {
			if (tradeStatus == StoreOrderTradeStatusEnum.READY.getValue()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_STOCK_UP.getValue(), DataUtil.infoWithParams("store order has already in trade stock_up, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			} else if (tradeStatus == StoreOrderTradeStatusEnum.WORKIN.getValue()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_PROCESS.getValue(), DataUtil.infoWithParams("store order has already in trade process, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			} else if (tradeStatus == StoreOrderTradeStatusEnum.SENTED.getValue()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_SEND_OUT.getValue(), DataUtil.infoWithParams("store order has already in trade send out, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			} else if (tradeStatus == StoreOrderTradeStatusEnum.CODE_TAKED.getValue()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_TAKE_CODE.getValue(), DataUtil.infoWithParams("store order has already in trade take code, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			} else if (tradeStatus == StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_PROCESS.getValue(), DataUtil.infoWithParams("store order meal has already prepare finish, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			}
			if (tradeStatus == StoreOrderTradeStatusEnum.FINISH.getValue()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_FINISH.getValue(), DataUtil.infoWithParams("store order has already trade finish, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			}
		}
		return storeOrder;
	}

	public void refundStoreOrderItemValidate(List<RefundOrderItemParam> refundOrderItems, StoreOrder storeOrder) throws T5weiException {
		boolean enableSlave = false;
		if (refundOrderItems == null || refundOrderItems.isEmpty()) {
			return;
		}
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		storeOrderHelper.setStoreOrderDetail(storeOrder, enableSlave);
		storeOrderHelper.setStoreOrderRefundItem(storeOrder, enableSlave);
		List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
		if (storeOrderItems == null || storeOrderItems.isEmpty()) {
			return;
		}
		Map<Long, StoreOrderItem> refundItemMap = new HashMap<Long, StoreOrderItem>();
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			refundItemMap.put(storeOrderItem.getChargeItemId(), storeOrderItem);
		}
		for (RefundOrderItemParam refundOrderItem : refundOrderItems) {
			long chargeItemId = refundOrderItem.getChargeItemId();
			StoreOrderItem storeOrderItem = refundItemMap.get(chargeItemId);
			if (storeOrderItem == null) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CAN_REFUND_AMOUNT_OUTOFF.getValue(), DataUtil.infoWithParams("store order storeOrderItem is null, storeId=#1, orderId=#2, chargeItemId=#3 ", new Object[] { storeId, orderId, chargeItemId }));
			}
			Double canRefundNum;
			if (refundOrderItem.isPacked()) {
				double packedAmount = storeOrderItem.getPackedAmount();
				double refundChargeItemNumPacked = storeOrderItem.getRefundChargeItemNumPacked();
				canRefundNum = NumberUtil.sub(packedAmount, refundChargeItemNumPacked);

			} else {
				double unPackedAmount = NumberUtil.sub(storeOrderItem.getAmount(), storeOrderItem.getPackedAmount());
				double refundChargeItemNumUnPacked = storeOrderItem.getRefundChargeItemNumUnPacked();
				canRefundNum = NumberUtil.sub(unPackedAmount, refundChargeItemNumUnPacked);
			}
			double refundNum = refundOrderItem.getRefundNum();
			if (canRefundNum.compareTo(refundNum) < 0) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CAN_REFUND_AMOUNT_OUTOFF.getValue(), DataUtil.infoWithParams("store order storeOrderItem is null, storeId=#1, orderId=#2, chargeItemId=#3 ", new Object[] { storeId, orderId, chargeItemId }));
			}
		}
	}

	public StoreOrder refundPartStoreOrder(StoreOrder storeOrder) throws T5weiException {
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		// 确认支付状态：
		// 未支付，则往下进行
		// 已支付&支付中，返回并提示给界面
		int payStatus = storeOrder.getPayStatus();
		if (payStatus == StoreOrderPayStatusEnum.NOT.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_NOT.getValue(), DataUtil.infoWithParams("store order do not pay, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if (payStatus == StoreOrderPayStatusEnum.DOING.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAYING.getValue(), DataUtil.infoWithParams("store order in paying, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue() && storeOrder.isOrderLocked()){
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_LOCKED.getValue(), DataUtil.infoWithParams("store order locked, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 确认退款状态
		// 商户或用户全额退款，返回并提示给用户
		// 未退款&部分退款，往下走
		int refundStatus = storeOrder.getRefundStatus();
		if (refundStatus == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() || refundStatus == StoreOrderRefundStatusEnum.USER_ALL.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND.getValue(), DataUtil.infoWithParams("store order has already refund, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		return storeOrder;
	}

	public int validateSiteNumber(int merchantId, long storeId, String orderId, int siteNumber) throws T5weiException {
		if (siteNumber > 0) {
			Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
			if (store5weiSetting == null || !store5weiSetting.isSiteNumberEnable()) {
				return 0;
			}
			if (store5weiSetting.isSiteNumberEnable()) {
				if (siteNumber > store5weiSetting.getSiteNumberMax()) {
					throw new T5weiException(T5weiErrorCodeType.STORE_SITE_NUMBER_SCOPE_INCORRECT.getValue(), DataUtil.infoWithParams("store site number scope incorrect, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
				}
			}
		}
		if (siteNumber < 0) {
			throw new T5weiException(T5weiErrorCodeType.STORE_SITE_NUMBER_SCOPE_INCORRECT.getValue(), DataUtil.infoWithParams("store site number scope incorrect, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		return siteNumber;
	}
	
	public StoreOrder validateCreateUserOrderInvoice(UserOrderInvoiceParam param) throws TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		String orderId = param.getOrderId();
		long requestUserId = param.getRequestUserId();
		StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
		// 没有支付完成的订单不允许开发票
		if (storeOrder.getPayStatus() != StoreOrderPayStatusEnum.FINISH.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_NOT.getValue(), DataUtil.infoWithParams("store order do not pay, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 未交易的订单不允许开发票
		if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_TRADE.getValue(), DataUtil.infoWithParams("store order do not trade, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 全额退款的订单不允许开发票
		if (storeOrder.getInvoiceStatus() != StoreOrderInvoiceStatusEnum.FINISH.getValue()) {
			if (storeOrder.getRefundStatus() == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() || storeOrder.getRefundStatus() == StoreOrderRefundStatusEnum.USER_ALL.getValue()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND.getValue(), DataUtil.infoWithParams("store order has refund all, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			}
		} else if (storeOrder.getInvoiceStatus() == StoreOrderInvoiceStatusEnum.FINISH.getValue()) {
			// 已经开过发票
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_INVOICE_HAS_OPENED.getValue(), DataUtil.infoWithParams("this store order has opened invoice, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		
		// 得到店铺发票设置
		StoreInvoiceSettingDTO storeInvoiceSetting = storeSettingFacade.getStoreInvoiceSetting(merchantId, storeId);
		if (!storeInvoiceSetting.isEnabledTax()) {
			// 电子发票未开启
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_INVOICE_TAX_NOT_ENABLE.getValue(), DataUtil.infoWithParams(" this store do not enabled elecInvoice , storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 检查用户权限
		this.validateCreateUserOrderInvoice(storeOrder, storeInvoiceSetting, requestUserId);
		// 已经超过开具发票期限，不允许再修改发票信息
		MutableDateTime mdt = new MutableDateTime(System.currentTimeMillis());
		mdt.addDays(-storeInvoiceSetting.getDeadline());
		if (storeOrder.getUpdateTime() < mdt.getMillis()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_INVOICE_OVER_TIME.getValue(), DataUtil.infoWithParams("store order invoice over time, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		return storeOrder;
	}
	
	public void validateCreateUserOrderInvoice(StoreOrder storeOrder, StoreInvoiceSettingDTO storeInvoiceSetting, long requestUserId) throws FacadeUserInvalidException, TException{
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		if (storeInvoiceSetting.isLockUser() && storeOrder.getUserId() > 0) {
			UserDTO userDTO = userFacade.getUserByUserId(storeOrder.getUserId());
			if (userDTO.getType() == UserTypeEnum.CARD.getValue()) {
				return;
			}
			// 不是此用户的订单，无权开此发票
			if ((storeOrder.getUserId() != requestUserId) || requestUserId == 0) {
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_INVOICE_USER_NO_PERMISSION.getValue(), DataUtil.infoWithParams(" this user has no permission to visit the order invoice, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
			}
		}
	}
	
	public void validateChangeStoreOrderTakeMode(int merchantId, long storeId, String orderId, int toTakeMode) throws T5weiException, TException {
    	Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
    	//切换成快取，但是店铺设置不支持快取
		if (toTakeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
			if (!store5weiSetting.isQuickTakeSupport()) {
				throw new T5weiException(T5weiErrorCodeType.STORE_CAN_NOT_QUICK_TAKE.getValue(), "切换成快取，但是店铺设置不支持快取");
			}
		}
		//切换成堂食，但是店铺设置不支持堂食
		if (toTakeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()){
			if(!store5weiSetting.isEnableEatin()){
				throw new T5weiException(T5weiErrorCodeType.STORE_CAN_NOT_DINE_IN.getValue(), "切换成堂食，但是店铺设置不支持堂食");
	    	}
    	}
		//切换成自取，但是店铺设置不支持自取
		if (toTakeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()){
			if(!store5weiSetting.isEnableUserTake()){
				throw new T5weiException(T5weiErrorCodeType.STORE_CAN_NOT_TAKE_OUT.getValue(), "切换成自取，但是店铺设置不支持自取");
	    	}
    	}
		//堂食自取不能切换成外送
		if (toTakeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()){
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CAN_NOT_CHANGE_TO_SEND_OUT.getValue(), "堂食自取不能切换成外送");
    	}
		//已支付订单产生打包费会导致价格变更，不能切换
		StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
		if(storeOrder.isProducePackageFee()){
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PRODUCE_PACKAGE_FEE.getValue(), "已支付订单产生打包费会导致价格变更，不能切换");
		}
		//收费项目是否支持切换
		List<Long> chargeItemIds = storeOrderHelper.getStoreOrderItemIds(merchantId, storeId, orderId, false);
    	boolean hasEnableSameTakeMode = storeChargeItemService.hasEnableSameTakeMode(merchantId, storeId, chargeItemIds, toTakeMode);
		if (!hasEnableSameTakeMode) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ITEM_CAN_NOT_CHANGE_TAKE_MODE.getValue(), "收费项目不支持取餐方式切换");
		}
		//优惠券是否支持切换
		String payOrderId = storeOrder.getPayOrderId();
		if(DataUtil.isEmpty(payOrderId)){
			return;
		}
		//支付记录相关的优惠券是否支持切换
		try{
			CouponTypeDTO couponTypeDTO = coupon4CommonFacade.getCouponTypeBypayOrderId(merchantId, payOrderId);
			if (couponTypeDTO == null){
				return;
			}
	    	// 切换成堂食，但优惠券不支持堂食
			if (toTakeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()){
				if (!couponTypeDTO.isDinein()){
					throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_COUPON_CAN_NOT_DINE_IN.getValue(), "切换成堂食，但优惠券不支持堂食");
		    	}
	    	}
			// 切换成自取，但优惠券不支持自取
			if (toTakeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()){
				if (!couponTypeDTO.isTakeout()){
					throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_COUPON_CAN_NOT_TAKE_OUT.getValue(), "切换成自取，但优惠券不支持自取");
		    	}
	    	}
		} catch (TCouponException e) {
			if (e.getErrorCode() == ErrorCodeEnum.GET_COUPON_TYPE_ID_ERROR.getValue()){
				// 根据payOrderid，获取不到优惠券类型Id
				return;
			}else{
				throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ITEM_CAN_NOT_CHANGE_TAKE_MODE.getValue(), "收费项目不支持取餐方式切换");
			}
		}
    }
	
}
