package com.huofu.module.i5wei.queue;

import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import huofucore.facade.account.accounted.AccountedSrcEnum;
import huofucore.facade.account.accounted.TradeTypeEnum;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.dialog.tweet.StoreTweetParam;
import huofucore.facade.dialog.tweet.TweetEventType;
import huofucore.facade.dialog.tweet.TweetMsgType;
import huofucore.facade.dialog.tweet.TweetType;
import huofucore.facade.dialog.visit.UserVisitType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderFacade;
import huofucore.facade.i5wei.order.StoreOrderTakeCodeParam;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.merchant.printer.PrintMessageParam;
import huofucore.facade.notify.PrinterAlarmSourceEnum;
import huofucore.facade.pay.payment.PaySrcEnum;
import huofucore.facade.statistics.storestatistics.StoreStatisticsFlag;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.DateUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.sqs.SQSHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class I5weiMessageProducer {

	private static final Log logger = LogFactory.getLog(I5weiMessageProducer.class);

	@Autowired
	private StoreTimeBucketService storeTimeBucketService;

	@Autowired
	private SQSHelper sqsHelper;

    @Autowired
    private StoreTableRecordDAO storeTableRecordDAO;

    @Autowired
    private StoreOrderFacade.Iface storeOrderFacade;

	private String uuid() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replaceAll("-", "");
		return uuid;
	}

	/**
	 * 订单支付事件消息
	 *
	 * @param storeOrder
	 * @throws T5weiException
	 */
	public void sendMessageOfStoreOrderEvent(StoreOrder storeOrder, long staffId, TweetEventType tweetEventType, String msg)
			throws T5weiException {
		if (storeOrder == null) {
			return;
		}
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		long userId = storeOrder.getUserId();
		String orderId = storeOrder.getOrderId();
		if (merchantId == 0 || storeId == 0 || userId == 0 || orderId == null || orderId.isEmpty()) {
			return;
		}
		StoreTimeBucket storeTimeBucket = storeOrder.getStoreTimeBucket();
		if (storeTimeBucket == null) {
			long timeBucketId = storeOrder.getTimeBucketId();
			storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId);
		}
		StringBuilder eventMsg = new StringBuilder();
		String date = DateUtil.formatDate("yyyy年MM月dd日", new Date(storeOrder.getRepastDate()));
		eventMsg.append(date);
		eventMsg.append("-");
		eventMsg.append(storeTimeBucket.getName());
		if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
			eventMsg.append("-");
			eventMsg.append("堂食");
		} else if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
			eventMsg.append("-");
			eventMsg.append("外带");
		} else if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
			eventMsg.append("-");
			eventMsg.append("堂食");
		} else if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
			eventMsg.append("-");
			eventMsg.append("外送");
		}
		// 订单消费事件记录
		StoreTweetParam storeTweetParam = new StoreTweetParam();
		storeTweetParam.setMerchantId(merchantId);
		storeTweetParam.setStoreId(storeId);
		storeTweetParam.setUserId(userId);
		storeTweetParam.setClientType(storeOrder.getClientType());
		storeTweetParam.setStaffId(staffId);
		storeTweetParam.setTweetType(TweetType.EVENT);
		storeTweetParam.setEventType(tweetEventType);
		storeTweetParam.setEventSrc(orderId);
		storeTweetParam.setTweet(eventMsg.toString());
		storeTweetParam.setMsgType(TweetMsgType.UNKNOWN);
		this.sendMessageOfStoreTweet(storeTweetParam);
	}

	public void sendMessageOfSendCouponAmount(StoreTableRecord storeTableRecord) {
		int merchantId = storeTableRecord.getMerchantId();
		long storeId = storeTableRecord.getStoreId();
		long tableRecordId = storeTableRecord.getTableRecordId();
		int customerTraffic = storeTableRecord.getCustomerTraffic();
		long tablePrice = storeTableRecord.getTablePrice();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("merchantId", merchantId);
		params.put("storeId", storeId);
		params.put("tableRecordId", tableRecordId);
		params.put("customerTraffic", customerTraffic);
		params.put("tablePrice", tablePrice);
		sqsHelper.sendMessage(SQSConfig.getSendCouponAmountQueue(), JsonUtil.build(params));
	}


    /**
     * 订单评分初始化，待评分
     *
     * @param storeOrder 交易订单
     */
    public void sendMessageOfStoreOrderGrade(StoreOrder storeOrder) {

        //小程序订单不需要走评分流程
        if(storeOrder.getClientType() == ClientTypeEnum.MINA.getValue()){
            return;
        }
        int merchantId = storeOrder.getMerchantId();
        long storeId = storeOrder.getStoreId();
        long repastDate = storeOrder.getRepastDate();
        long timeBucketId = storeOrder.getTimeBucketId();
        long userId = storeOrder.getUserId();
        String orderId = storeOrder.getOrderId();
        String orderDescription = storeOrder.getOrderDescription();
        int maxLength = 400;
        if (orderDescription.length() > maxLength) {
            orderDescription = orderDescription.substring(0, maxLength) + "...";
        }
        long orderPrice = storeOrder.getOrderPrice();
        if (storeOrder.isTableRecordMasterOrder()) {
            long tableRecordId = storeOrder.getTableRecordId();
            try {
                StoreTableRecord storeTableRecord = storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, tableRecordId, false);
                long payAbleAmount = storeTableRecord.getPayAbleAmount();
                orderPrice = payAbleAmount;
            } catch (T5weiException e) {
            }
        }
        long takeSerialTime = storeOrder.getTakeSerialTime();
        if (takeSerialTime == 0) {
            takeSerialTime = System.currentTimeMillis();
        }
        long tradeFinishTime = storeOrder.getTradeFinishTime();
        if (tradeFinishTime == 0) {
            tradeFinishTime = System.currentTimeMillis();
        }
        int takeMode = storeOrder.getTakeMode();
        if (merchantId == 0 || storeId == 0 || timeBucketId == 0 || userId == 0 || orderId == null || orderId.isEmpty()) {
            return;
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("merchantId", merchantId);
        params.put("storeId", storeId);
        params.put("repastDate", repastDate);
        params.put("timeBucketId", timeBucketId);
        params.put("userId", userId);
        params.put("orderId", orderId);
        params.put("src", PaySrcEnum.M_5WEI.getValue());
        params.put("couponId", 0);
        params.put("orderDescription", orderDescription);
        params.put("takeSerialTime", takeSerialTime);
        params.put("tradeFinishTime", tradeFinishTime);
        params.put("takeMode", takeMode);
        params.put("orderPrice", orderPrice);
        params.put("tableRecordId", storeOrder.getTableRecordId());
        sqsHelper.sendMessage(SQSConfig.getDialogGradeQueue(), JsonUtil.build(params));
    }

	/**
	 * 访问记录
	 */
	public void sendMessageOfStoreOrderVisit(int merchantId, long storeId, long userId, UserVisitType userVisitType) {
		this.sendMessageOfStoreOrderVisit(merchantId, storeId, userId, userVisitType, null, null, null);
	}

    public void sendMessageOfStoreOrderVisit(int merchantId, long storeId, long userId, UserVisitType userVisitType, List<Long> promotionRebateIds4Visit, List<Long> promotionReduceIds4Visit, List<Long> promotiongGratisIds4Visit) {
        if (merchantId == 0 || storeId == 0 || userId == 0) {
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("merchantId", merchantId);
        params.put("storeId", storeId);
        params.put("userId", userId);
        params.put("userVisitType", userVisitType.getValue());
        if (promotionRebateIds4Visit != null) {
            params.put("promotionRebateIds4Visit", promotionRebateIds4Visit);
        }
        if (promotionReduceIds4Visit != null) {
            params.put("promotionReduceIds4Visit", promotionReduceIds4Visit);
        }
        if (promotiongGratisIds4Visit != null){
            params.put("promotiongGratisIds4Visit", promotiongGratisIds4Visit);
        }
        sqsHelper.sendMessage(SQSConfig.getDialogVisitQueue(), JsonUtil.build(params));
    }

	/**
	 * 消息&事件记录
	 *
	 * @param storeTweetParam
	 */
	public void sendMessageOfStoreTweet(StoreTweetParam storeTweetParam) {
		int merchantId = storeTweetParam.getMerchantId();
		long storeId = storeTweetParam.getStoreId();
		long userId = storeTweetParam.getUserId();
		long staffId = storeTweetParam.getStaffId();
		if (merchantId == 0 || storeId == 0) {
			return;
		}
		if (userId == 0 && staffId == 0) {
			return;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("merchantId", storeTweetParam.getMerchantId());
		params.put("storeId", storeTweetParam.getStoreId());
		params.put("userId", storeTweetParam.getUserId());
		params.put("clientType", storeTweetParam.getClientType());
		params.put("staffId", storeTweetParam.getStaffId());
		params.put("tweetType", storeTweetParam.getTweetType().getValue());
		params.put("eventType", storeTweetParam.getEventType().getValue());
		params.put("eventSrc", storeTweetParam.getEventSrc());
		params.put("msgType", storeTweetParam.getMsgType().getValue());
		params.put("tweet", storeTweetParam.getTweet());
		sqsHelper.sendMessage(SQSConfig.getDialogTweetQueue(), JsonUtil.build(params));
	}

	/*public void sendMessageOfStatStoreOrderPay(StoreOrder storeOrder) {
	    this.sendMessageOfStatStoreOrderPay(storeOrder, false);
	}*/

	/**
	 * 支付统计消息
	 *
	 * @param storeOrder
	 */
	public void sendMessageOfStatStoreOrderPay(StoreOrder storeOrder, boolean tradeOrder) {
		if (storeOrder.isTableRecordOrder()) { // 如果是桌台订单，先不发消息，等结账或清台时统一发送消息
			return;
		}
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		String payOrderId = storeOrder.getPayOrderId();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("merchantId", merchantId);
		params.put("storeId", storeId);
		params.put("orderId", orderId);
		params.put("payOrderId", payOrderId);
		params.put("tradeOrder", tradeOrder);
		params.put("createTime", System.currentTimeMillis());
		logger.info("send smg:" + JsonUtil.build(params) + " to " + SQSConfig.getStat5weiOrderPayQueue());
		sqsHelper.sendMessage(SQSConfig.getStat5weiOrderPayQueue(), JsonUtil.build(params));
	}

	public void sendMessageOfStatTableRecordOrder(List<StoreOrder> storeOrders) {
		if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		for (StoreOrder storeOrder : storeOrders) {
			int merchantId = storeOrder.getMerchantId();
			long storeId = storeOrder.getStoreId();
			String orderId = storeOrder.getOrderId();
			String payOrderId = storeOrder.getPayOrderId();
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("merchantId", merchantId);
			params.put("storeId", storeId);
			params.put("orderId", orderId);
			params.put("payOrderId", payOrderId);
			params.put("tradeOrder", false);
			params.put("createTime", System.currentTimeMillis());
			logger.info("send smg:" + JsonUtil.build(params) + " to " + SQSConfig.getStat5weiOrderPayQueue());
			sqsHelper.sendMessage(SQSConfig.getStat5weiOrderPayQueue(), JsonUtil.build(params));
		}
	}

	/**
	 * 退款统计消息
	 *
	 * @param storeOrder
	 * @param staffId
	 * @param refundRecordId
	 */
	public void sendMessageOfStatStoreOrderRefund(StoreOrder storeOrder, long staffId, long refundRecordId, boolean cancelOrder) {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		String payOrderId = storeOrder.getPayOrderId();
		long repastDate = storeOrder.getRepastDate();
		long timeBucketId = storeOrder.getTimeBucketId();
		long userId = storeOrder.getUserId();
		int refundStatus = storeOrder.getRefundStatus();
		long tableRecordId = storeOrder.getTableRecordId();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("merchantId", merchantId);
		params.put("storeId", storeId);
		params.put("orderId", orderId);
		params.put("payOrderId", payOrderId);
		params.put("staffId", staffId);
		params.put("refundRecordId", refundRecordId);
		params.put("repastDate", repastDate);
		params.put("timeBucketId", timeBucketId);
		params.put("userId", userId);
		params.put("refundStatus", refundStatus);
		params.put("createTime", System.currentTimeMillis());
		params.put("cancelOrder", cancelOrder);
		params.put("tableRecordId", tableRecordId);
		logger.info("========params:" + JsonUtil.build(params));
		sqsHelper.sendMessage(SQSConfig.getStat5weiOrderRefundQueue(), JsonUtil.build(params));
	}

	/**
	 * 商户入账消息
	 *
	 * @param storeOrder 交易订单
	 */
	public void sendMessageOfMerchantAccounted(StoreOrder storeOrder) {
		if (StringUtils.isEmpty(storeOrder.getPayOrderId())) {
			return;
		}
		Map<String, Object> msgOf5wei = new HashMap<String, Object>();
		msgOf5wei.put("accountedSrc", AccountedSrcEnum.M_5WEI.getValue());
		msgOf5wei.put("tradeType", TradeTypeEnum.CONSUME.getValue());
		msgOf5wei.put("payOrderId", storeOrder.getPayOrderId());
		logger.info("send message " + JsonUtil.build(msgOf5wei) + " to " + SQSConfig.getMerchantAccountedQueue());
		sqsHelper.sendMessage(SQSConfig.getMerchantAccountedQueue(), JsonUtil.build(msgOf5wei));
	}

	/**
	 * 店铺统计消息（消费）
	 *
	 * @param storeOrder 交易订单
	 */
	public void sendConsumeMessageOfStoreStatistics(StoreOrder storeOrder) {
		if (StringUtils.isEmpty(storeOrder.getPayOrderId())) {
			return;
		}
		Map<String, Object> msgOfStoreStatistics = new HashMap<String, Object>();
		msgOfStoreStatistics.put("payOrderId", storeOrder.getPayOrderId());
		msgOfStoreStatistics.put("flag", StoreStatisticsFlag.CONSUME.getValue());
		logger.info("send message " + JsonUtil.build(msgOfStoreStatistics) + " to " + SQSConfig.getStoreStatisticsQueue());
		sqsHelper.sendMessage(SQSConfig.getStoreStatisticsQueue(), JsonUtil.build(msgOfStoreStatistics));
	}

	/**
	 * 店铺统计消息（退款）
	 *
	 * @param refundRecordId 退款记录id
	 */
	public void sendRefundMessageOfStoreStatistics(long refundRecordId) {
		Map<String, Object> msgOfStoreStatistics = new HashMap<String, Object>();
		msgOfStoreStatistics.put("refundRecordId", refundRecordId);
		msgOfStoreStatistics.put("flag", StoreStatisticsFlag.REFUND.getValue());
		logger.info("send message " + JsonUtil.build(msgOfStoreStatistics) + " to " + SQSConfig.getStoreStatisticsQueue());
		sqsHelper.sendMessage(SQSConfig.getStoreStatisticsQueue(), JsonUtil.build(msgOfStoreStatistics));
	}

	/**
	 * 自动打印消息通知
	 *
	 * @param printMessageParam
	 */
	public void sendPrintMessage(PrintMessageParam printMessageParam) {
		if (true) {
			logger.debug("####printMessageParam=" + printMessageParam);
		}
		if (printMessageParam == null) {
			return;
		}
		long storeId = printMessageParam.getStoreId();
		int merchantId = printMessageParam.getMerchantId();
		long printerPeripheralId = printMessageParam.getPrinterPeripheralId();
		int printSrc = printMessageParam.getPrintSrc();
		String printSrcId = printMessageParam.getPrintSrcId();
		long userId = printMessageParam.getUserId();
		long staffId = printMessageParam.getStaffId();
		int msgType = printMessageParam.getMsgType();
		String msgContent = printMessageParam.getMsgContent();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("uuid", uuid());
		params.put("storeId", storeId);
		params.put("merchantId", merchantId);
		params.put("printerPeripheralId", printerPeripheralId);
		params.put("printSrc", printSrc);
		params.put("printSrcId", printSrcId);
		params.put("userId", userId);
		params.put("staffId", staffId);
		params.put("msgType", msgType);
		params.put("userId", userId);
		params.put("msgContent", msgContent);
		params.put("createTime", System.currentTimeMillis());
		sqsHelper.sendMessage(SQSConfig.getPrintMessageNotifyQueue(), JsonUtil.build(params));
	}

	//消息不去重
	public void sendMultiPrintMessages(List<PrintMessageParam> printMessages, long staffId) {
		if (printMessages == null || printMessages.isEmpty()) {
			return;
		}
		for (PrintMessageParam printMessageParam : printMessages) {
			printMessageParam.setStaffId(staffId);
			this.sendPrintMessage(printMessageParam);
		}
	}

	public void sendPrintMessages(List<PrintMessageParam> printMessages, long staffId) {
		if (printMessages == null || printMessages.isEmpty()) {
			return;
		}
		// 去除重复消息
		Map<String, PrintMessageParam> dataMap = new HashMap<String, PrintMessageParam>();
		for (PrintMessageParam messageParam : printMessages) {
			String key = messageParam.getPrintSrcId() + "_" + messageParam.getPrinterPeripheralId() + "_" + messageParam.getMsgContent();
			dataMap.put(key, messageParam);
		}
		printMessages = new ArrayList<PrintMessageParam>(dataMap.values());
		// 发送消息
		for (PrintMessageParam printMessageParam : printMessages) {
			printMessageParam.setStaffId(staffId);
			this.sendPrintMessage(printMessageParam);
		}
	}

	public void sendTableRecordSettlingCancelMessages(StoreTableRecord storeTableRecord) {
		if (storeTableRecord == null) {
			return;
		}
		if (!storeTableRecord.isSettling()) {
			return;
		}
		int merchantId = storeTableRecord.getMerchantId();
		long storeId = storeTableRecord.getStoreId();
		long tableRecordId = storeTableRecord.getTableRecordId();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("merchantId", merchantId);
		params.put("storeId", storeId);
		params.put("tableRecordId", tableRecordId);
		params.put("requestBeginTime", System.currentTimeMillis());
		sqsHelper.sendMessage(SQSConfig.getTableRecordSettlingCancelMessageQueue(), JsonUtil.build(params), 120, 3);
	}

	public void sendPrintAlarmDelayMessage(int merchantId, long storeId, List<StoreMealPort> storeMealPorts) {
        List<Long> storeMealPortIds = new ArrayList<>();
        for(StoreMealPort storeMealPort : storeMealPorts){
            storeMealPortIds.add(storeMealPort.getPortId());
        }
		Map<String, Object> params = new HashMap<String, Object>();
		//0=自动出餐  1=通用自动打印
		params.put("printAlarmSource", PrinterAlarmSourceEnum.AUTO_CHECK_OUT.getValue());
		params.put("merchantId", merchantId);
		params.put("storeId", storeId);
		params.put("storeMealPortIds", storeMealPortIds);
		long recvTime = System.currentTimeMillis() + 2 * 60 * 1000;
		try {
			if(!storeMealPortIds.isEmpty()){
				sqsHelper.sendMessageDelayToTime(SQSConfig.getPrintAlarmQueue(), JsonUtil.build(params), recvTime, 3);
			}
		}catch (Exception e){
			logger.error("autoCheckOut  sendPrintAlarmDelayMessage [params] == " + JsonUtil.build(params) + ",,," + e.getMessage());
		}

	}


	public void sendTimingTakeCodeMessage(StoreOrder storeOrder, int timingPrepareTime) {
		if (!storeOrder.isPayFinish()) {
			return;
		}
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		int takeMode = storeOrder.getTakeMode();
		int clientType = storeOrder.getClientType();
		long timingTakeTime = storeOrder.getTimingTakeTime();
		if (timingTakeTime <= 0) {
			return;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("merchantId", merchantId);
		params.put("storeId", storeId);
		params.put("orderId", orderId);
		params.put("takeMode", takeMode);
		params.put("clientType", clientType);
		params.put("timingTakeTime", timingTakeTime);
		if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue() &&
				storeOrder.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue()) {
			sqsHelper.sendMessageDelayToTime(SQSConfig.getTimingTakeCodeQueue(), JsonUtil.build(params), timingTakeTime, 3);
		} else {
			MutableDateTime mdt = new MutableDateTime(timingTakeTime);
			mdt.addMinutes(-timingPrepareTime);
			if (mdt.getMillis() <= System.currentTimeMillis()) {
				StoreOrderTakeCodeParam storeOrderTakeCodeParam = new StoreOrderTakeCodeParam();
				storeOrderTakeCodeParam.setMerchantId(merchantId);
				storeOrderTakeCodeParam.setStoreId(storeId);
				storeOrderTakeCodeParam.setOrderId(storeOrder.getOrderId());
				storeOrderTakeCodeParam.setClientType(storeOrder.getClientType());
				storeOrderTakeCodeParam.setTakeMode(StoreOrderTakeModeEnum.findByValue(storeOrder.getTakeMode()));
				try {
					storeOrderFacade.takeCodeStoreOrder(storeOrderTakeCodeParam);
				} catch (TException e) {
					logger.error("sendTimingTakeCodeMessage, takeCodeStoreOrder=" + storeOrderTakeCodeParam, e);
				} catch (Exception e) {
					logger.error("sendTimingTakeCodeMessage, takeCodeStoreOrder=" + storeOrderTakeCodeParam, e);
				}
			} else {
				sqsHelper.sendMessageDelayToTime(SQSConfig.getTimingTakeCodeQueue(), JsonUtil.build(params), mdt.getMillis(), 3);
			}
		}
	}

}
