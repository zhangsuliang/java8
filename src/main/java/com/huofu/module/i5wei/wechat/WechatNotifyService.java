package com.huofu.module.i5wei.wechat;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucketUtil;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.service.OpDeliveryingResult;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteDAO;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteTimeSettingDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderTradeStatusEnum;
import huofucore.facade.merchant.staff.StaffDTO2;
import huofucore.facade.merchant.staff.StaffFacade;
import huofucore.facade.merchant.store.FacadeStoreInvalidException;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.StoreFacade;
import huofucore.facade.merchant.store.query.StoreQueryFacade;
import huofucore.facade.notify.NotifyFacade;
import huofucore.facade.notify.TNotifyException;
import huofucore.facade.waimai.meituan.order.StoreMeituanOrderFacade;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.ObjectUtil;
import huofuhelper.util.http.HttpClient4Util;
import huofuhelper.util.http.HttpClientFactory;
import huofuhelper.util.http.HttpParameters;
import huofuhelper.util.http.HttpResp;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;
import org.apache.thrift.TEnum;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Service
public class WechatNotifyService {

	private static final Log log = LogFactory.getLog(WechatNotifyService.class);

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("sysconfig");

	private static final ResourceBundle i5weiConfig = ResourceBundle.getBundle("i5weiconfig");

	private static String requestUrl = null;

	private static String monitorUrl = null;

	private static int notifyAppid = 0;

	@Autowired
	private HttpClientFactory httpClientFactory;

	@ThriftClient
	private StoreFacade.Iface storeFacade;

    @ThriftClient
	private StoreQueryFacade.Iface storeQueryFacade;

    @ThriftClient
	private StaffFacade.Iface staffFacade;

	@Autowired
	private StoreTimeBucketDAO storeTimeBucketDAO;

	@Autowired
	private StoreOrderDAO storeOrderDAO;

	@Autowired
	private StorePickupSiteDAO storePickupSiteDAO;

	@Autowired
	private StorePickupSiteTimeSettingDAO storePickupSiteTimeSettingDAO;

	@ThriftClient
	private NotifyFacade.Iface notifyFacade;

	@ThriftClient
    private StoreMeituanOrderFacade.Iface storeMeituanOrderFacade;

	@Autowired
	private WechatTempNotifyService wechatTempNotifyService;

	private String getWechatMinaPayMessageUrl(){
		if (requestUrl == null) {
			String baseUrl = resourceBundle.getString("wechat.notify.server");
			requestUrl = baseUrl + "/api/send_template/refund";
		}
		return requestUrl;
	}

	private String getWechatNotifyUrl() {
		if (requestUrl == null) {
			String baseUrl = resourceBundle.getString("wechat.notify.server");
			requestUrl = baseUrl + "/notify/trigger";
		}
		return requestUrl;
	}

	private String getWechatMonitorUrl() {
		if (monitorUrl == null) {
			String baseUrl = resourceBundle.getString("wechat.notify.server");
			monitorUrl = baseUrl + "/qy/notify";
		}
		return monitorUrl;
	}

	private int getWechatNotifyAppid() {
		if (notifyAppid == 0) {
			String value = resourceBundle.getString("wechat.notify.appid");
			notifyAppid = ObjectUtil.getInt(value, 0);
		}
		return notifyAppid;
	}

	public void sendWechatMinaPayMessage(StoreOrder storeOrder,String prepayId) throws FacadeStoreInvalidException, TException {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		long userId = storeOrder.getUserId();
		String orderId = storeOrder.getOrderId();
		if (userId <= 0 || StringUtils.isEmpty(prepayId)) {
			return;// 不发送消息
		}
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", merchantId + "");
		params.add("store_id", storeId + "");
		params.add("to_user_id", userId + "");
		params.add("form_id", prepayId + "");
		params.add("order_id", orderId + "");
		for (int i = 0; i < 3; i++) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("#### Mina Pay Message Request url=" + this.getWechatMinaPayMessageUrl() + ",storeId=" + storeId + ",userId=" + userId);
				}
				HttpClient4Util.doPost(httpClient, this.getWechatMinaPayMessageUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatMinaPayMessageUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	public void notifyOrderSuccessMsg(StoreOrder storeOrder) throws FacadeStoreInvalidException, TException {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		long userId = storeOrder.getUserId();
		if (userId <= 0) {
			return;
		}
		if (storeOrder.getTableRecordId() > 0) {
			return;// 桌台模式不发
		}
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", merchantId + "");
		params.add("store_id", storeId + "");
		params.add("user_id", userId + "");
		params.add("status", WechatNotifyStatus.ORDER_SUCCESS.getDescption());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("order_id", storeOrder.getOrderId() + "");
		params.add("data", JsonUtil.build(data));
		for (int i = 0; i < 3; i++) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("#### Request url=" + this.getWechatNotifyUrl() + ",storeId=" + storeId + ",userId=" + userId);
				}
				HttpClient4Util.doPost(httpClient, this.getWechatNotifyUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatNotifyUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	public void notifyOpenTableRecordMsg(StoreTableRecord storeTableRecord, long userId) {
		int merchantId = storeTableRecord.getMerchantId();
		long storeId = storeTableRecord.getStoreId();
		if (userId <= 0) {
			return;
		}
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", merchantId + "");
		params.add("store_id", storeId + "");
		params.add("user_id", userId + "");
		params.add("status", WechatNotifyStatus.OPEN_TABLE_RECORD.getDescption());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("table_record_id", storeTableRecord.getTableRecordId() + "");
		params.add("data", JsonUtil.build(data));
		for (int i = 0; i < 3; i++) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("#### Request url=" + this.getWechatNotifyUrl() + ",storeId=" + storeId + ",userId=" + userId);
				}
				HttpClient4Util.doPost(httpClient, this.getWechatNotifyUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatNotifyUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	public void notifyTableRecordAddDishMsg(StoreTableRecord storeTableRecord, long userId) {
		int merchantId = storeTableRecord.getMerchantId();
		long storeId = storeTableRecord.getStoreId();
		if (userId <= 0) {
			return;
		}
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", merchantId + "");
		params.add("store_id", storeId + "");
		params.add("user_id", userId + "");
		params.add("status", WechatNotifyStatus.ADD_DISH.getDescption());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("table_record_id", storeTableRecord.getTableRecordId() + "");
		params.add("data", JsonUtil.build(data));
		for (int i = 0; i < 3; i++) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("#### Request url=" + this.getWechatNotifyUrl() + ",storeId=" + storeId + ",userId=" + userId);
				}
				HttpClient4Util.doPost(httpClient, this.getWechatNotifyUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatNotifyUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	public void notifyOrderTakeCodeMsg(StoreOrder storeOrder) throws FacadeStoreInvalidException, TException {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		long userId = storeOrder.getUserId();
		if (userId <= 0) {
			return;// 没指定用户不发
		}
		if (storeOrder.getTableRecordId() > 0) {
			return;// 桌台模式不发
		}
		List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
		if (storeOrderItems != null && !storeOrderItems.isEmpty()){
			if (storeOrder.getCashierChannelId()>0){
				return;// 自助餐不发
			}
		}
		//小程序接单不发
		if (storeOrder.getClientType() == ClientTypeEnum.MINA.getValue()) {
			return;
		}
		//店内取餐发送模板消息,仅中味餐厅发送模板消息
		if(storeOrder.isSkipTakeCode() && storeOrder.getMerchantId() == 1307) {
			wechatTempNotifyService.sendWechatMsgForTakeCode(storeOrder);
			return;
		}
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", merchantId + "");
		params.add("store_id", storeId + "");
		params.add("user_id", userId + "");
		params.add("status", WechatNotifyStatus.TAKE_CODE.getDescption());
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("order_id", storeOrder.getOrderId() + "");
		params.add("data", JsonUtil.build(data));
		for (int i = 0; i < 3; i++) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("#### NotifyOrderTakeCodeMsg Request url=" + this.getWechatNotifyUrl() + ",storeId=" + storeId + ",userId=" + userId);
				}
				HttpClient4Util.doPost(httpClient, this.getWechatNotifyUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatNotifyUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	public void notifyAutoOrderRefundMsg(StoreOrder storeOrder, int isAuto, int refundVersion) throws FacadeStoreInvalidException, TException {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		long userId = storeOrder.getUserId();
		if (userId <= 0) {
			return;
		}
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", merchantId + "");
		params.add("store_id", storeId + "");
		params.add("user_id", userId + "");
		params.add("status", WechatNotifyStatus.ORDER_CANCEL.getDescption());
        Map<String, Object> data = new HashMap<>();
		data.put("merchant_id", merchantId + "");
		data.put("store_id", storeId + "");
		data.put("order_id", storeOrder.getOrderId() + "");
		data.put("auto", isAuto + "");
		// 增加一个标识,区分是退款是新版本还是旧版本
		//0 : 发送"原路退回"链接
		//1 : 不发送"原路退回"链接
		data.put("original", String.valueOf(refundVersion));
		params.add("data", JsonUtil.build(data));
		for (int i = 0; i < 3; i++) {
			try {
				if (log.isDebugEnabled()) {
					log.debug("#### Request url=" + this.getWechatNotifyUrl() + ",storeId=" + storeId + ",userId=" + userId);
				}
				HttpClient4Util.doPost(httpClient, this.getWechatNotifyUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatNotifyUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	class ReturnMsg {

		private String msg;

		private String error_code;

		public String getMsg() {
			return msg;
		}

		public void setMsg(String msg) {
			this.msg = msg;
		}

		public String getError_code() {
			return error_code;
		}

		public void setError_code(String error_code) {
			this.error_code = error_code;
		}
	}

	public enum WechatNotifyStatus implements TEnum {
		/**
		 * 订单评分通知
		 */
		GRADE_INVITE(0),

		/**
		 * 支付成功通知
		 */
		ORDER_SUCCESS(1),

		/**
		 * 取消订单通知
		 */
		ORDER_CANCEL(2),

		/**
		 * 取消订单通知
		 */
		TAKE_CODE(3),

		/**
		 * 开台通知
		 */
		OPEN_TABLE_RECORD(4),

		/**
		 * 加菜
		 */
		ADD_DISH(5);

		private final String[] status = new String[] { "5wei_grade_invite", "5wei_order_success", "5wei_order_refundInfo", "5wei_order_fetched", "5wei_table_seating", "5wei_table_addDish" };

		private final int value;

		private WechatNotifyStatus(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public String getDescption() {
			return status[value];
		}

		public static WechatNotifyStatus findByValue(int value) {
			switch (value) {
			case 0:
				return GRADE_INVITE;
			case 1:
				return ORDER_SUCCESS;
			case 2:
				return ORDER_CANCEL;
			case 3:
				return TAKE_CODE;
			case 4:
				return OPEN_TABLE_RECORD;
			case 5:
				return ADD_DISH;
			default:
				return null;
			}
		}
	}

	public void sendWecaht4Delivery(int merchantId, long storeId, long staffId, List<StoreOrder> storeOrders, boolean forDelivery) throws TException {
		if (storeOrders == null) {
			return;
		}
		List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketDAO.getListForStore(merchantId, storeId, false, false);
		Map<Long, StoreTimeBucket> bucketMap = StoreTimeBucketUtil.buildMap(storeTimeBuckets);
        StoreDTO storeDTO = null;
        StaffDTO2 staffDTO2 = null;
        if (forDelivery) {
            storeDTO = this.storeQueryFacade.getStore(merchantId, storeId);
            staffDTO2 = this.staffFacade.getStaffByStaffIdV2(merchantId, staffId);
        }
        for (StoreOrder storeOrder : storeOrders) {
			if (storeOrder.getUserId() <= 0) {
				continue;
			}
			StoreTimeBucket bucket = bucketMap.get(storeOrder.getTimeBucketId());
			try {
				String content = null;
				if (storeOrder.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue()) {
					if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.SENTED.getValue()) {
						content = this.buildContentForPickupSiteStore(storeOrder, bucket);
						this.notifyFacade.wechatSendSimple(merchantId, storeId, storeOrder.getUserId(), content, 0);
					} else if(storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.WORKIN.getValue() ||
							storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.CODE_TAKED.getValue()) {
						notifyOrderTakeCodeMsg(storeOrder);
					}
				} else {
					//普通外卖订单提醒内容
					content = this.buildContent4PreparingAndDelivering(storeDTO, storeOrder, staffDTO2, bucket, forDelivery);
					this.notifyFacade.wechatSendSimple(merchantId, storeId, storeOrder.getUserId(), content, 0);
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}

	public void sendMsgToStaffForDelivering(OpDeliveryingResult opDeliveryingResult) {
		long staffUserId = opDeliveryingResult.getDeliverStaffUserId();
		int newAdd = opDeliveryingResult.getNewAdd();
		int total = opDeliveryingResult.getTotal();
		try {
			int gzId = SysConfig.getOfficalMerchantGzId();
			HttpClient httpClient = this.httpClientFactory.getHttpClient();
			HttpParameters httpParameters = new HttpParameters();
			httpParameters.add("gzid", String.valueOf(gzId));
			httpParameters.add("user_id", String.valueOf(staffUserId));
			httpParameters.add("status", "manage_delivery_push");
			Map<String, Object> dataMap = Maps.newHashMap();
			dataMap.put("t_count", newAdd);
			dataMap.put("count", total);
			httpParameters.add("data", JsonUtil.build(dataMap));
			HttpResp httpResp = HttpClient4Util.doPost(httpClient, this.getWechatNotifyUrl(), httpParameters, "utf-8");
			String respText = httpResp.getText("utf-8");
			Map<String, Object> map = (Map<String, Object>) JsonUtil.parse(respText, Map.class);
			if (map != null) {
				Integer errorCode = (Integer) map.get("error_code");
				if (errorCode != null && errorCode != 0) {
					log.warn(respText);
				}
			}
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
	}

	public void monitorMsg(String content) {
		if (content == null || content.isEmpty()) {
			return;
		}
		int appid = this.getWechatNotifyAppid();
		HttpClient httpClient = httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("agentid", appid + "");// 应用ID
		params.add("totag", "7");
		params.add("msgtype", "text");
		params.add("content", content);
		for (int i = 0; i < 3; i++) {
			try {
				HttpClient4Util.doPost(httpClient, this.getWechatMonitorUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatMonitorUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	private String buildContent4PreparingAndDelivering(StoreDTO storeDTO, StoreOrder storeOrder, StaffDTO2 staffDTO2, StoreTimeBucket bucket, boolean forDelivery) {
		if (storeOrder.getUserId() <= 0) {
			return null;
		}
		String userName = "";//外送员姓名
        String userMobile = "";//外送员电话
        if (staffDTO2 != null) {
            if (staffDTO2.getMerchantUserDTO() != null) {
                userMobile = staffDTO2.getMerchantUserDTO().getMobile();
            }
            userName = staffDTO2.getShowName();
        }
        String storeTel = "";//店铺电话
        if (storeDTO != null) {
            storeTel = storeDTO.getTel();
        }
        MutableDateTime mdtCreateTime = new MutableDateTime(storeOrder.getCreateTime());
		MutableDateTime mdtRepastDate = new MutableDateTime(storeOrder.getRepastDate());
		MutableDateTime mdtToday = new MutableDateTime();
		String createTimeStr = null;
		String repastTimeStr = null;
		if (mdtCreateTime.getDayOfMonth() == mdtToday.getDayOfMonth()) {
			createTimeStr = "今天";
		}
		if (mdtRepastDate.getDayOfMonth() == mdtToday.getDayOfMonth()) {
			repastTimeStr = "今天";
		}

		if (createTimeStr == null) {
			createTimeStr = DateUtil.formatDate("yyyy-MM-dd HH:mm", mdtCreateTime.toDate());
		}
		if (repastTimeStr == null) {
			repastTimeStr = DateUtil.formatDate("yyyy-MM-dd HH:mm", mdtRepastDate.toDate());
		}

		String content;
		if (mdtCreateTime.getDayOfMonth() == mdtToday.getDayOfMonth() && mdtRepastDate.getDayOfMonth() == mdtToday.getDayOfMonth()) {
			if (forDelivery) {
			    if(DataUtil.isNotEmpty(storeTel)){
                    return MessageFormat.format(i5weiConfig.getString("msg.delivery.delivering2"), bucket.getName(), userName, userMobile, storeTel);
                }
                return MessageFormat.format(i5weiConfig.getString("msg.delivery.delivering0"), bucket.getName(), userName, userMobile);
			} else {
				return MessageFormat.format(i5weiConfig.getString("msg.delivery.preparing0"), bucket.getName());
			}
		} else {
            if (forDelivery) {
                if (DataUtil.isNotEmpty(storeTel)) {
                    return MessageFormat.format(i5weiConfig.getString("msg.delivery.delivering3"), createTimeStr, repastTimeStr, bucket.getName(), userName, userMobile, storeTel);
                }
                return MessageFormat.format(i5weiConfig.getString("msg.delivery.delivering1"), createTimeStr, repastTimeStr, bucket.getName(), userName, userMobile);
            } else {
                return MessageFormat.format(i5weiConfig.getString("msg.delivery.preparing1"), createTimeStr, repastTimeStr, bucket.getName());
            }
        }
	}

	public String buildContentForPickupSiteStore(StoreOrder storeOrder, StoreTimeBucket bucket) {
		boolean isCompleted = false;
		StoreOrderDelivery storeOrderDelivery = this.storeOrderDAO.getDeliveryById(
				storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getOrderId(), false, false);
		StringBuilder content = new StringBuilder("尊敬的顾客，配送员带着您的自提点订单，已经飞奔在路上了，稍后即可用餐")
				.append("\n-------");
		if(storeOrderDelivery != null) {
			List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings = this.storePickupSiteTimeSettingDAO.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(
					storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrderDelivery.getStorePickupSiteId(), storeOrder.getTimeBucketId());
			StorePickupSite storePickupSite = this.storePickupSiteDAO.getStorePickupSiteById(
					storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrderDelivery.getStorePickupSiteId());
			if (!CollectionUtils.isEmpty(storePickupSiteTimeSettings) && storePickupSite != null) {
				StorePickupSiteTimeSetting storePickupSiteTimeSetting = storePickupSiteTimeSettings.get(0);
				content.append("\n自提点：" + storeOrderDelivery.getStorePickupName());
				content.append("\n地址：" + storePickupSite.getStorePickupSiteAddress());
				MutableDateTime mdt = new MutableDateTime(DateUtil.getBeginTime(System.currentTimeMillis(), null) + storePickupSiteTimeSetting.getPickupTime());
				String takeOrderTimeStr = DateUtil.formatDate("HH:mm", mdt.toDate());
				content.append("\n取餐时间：" + takeOrderTimeStr + "以后");
				isCompleted = true;
			}
		}
		if (isCompleted) {
			return content.toString();
		} else {
			return null;
		}
	}

	public void sendWechatMessage(int merchantId, long storeId, long userId, String tweet) {
		if (merchantId == 0 || storeId == 0 || userId == 0 || tweet == null || tweet.isEmpty()) {
			return;
		}
		String notifyMessage = tweet;
		try {
			notifyFacade.wechatSendSimple(merchantId, storeId, userId, notifyMessage, 1);
		} catch (TNotifyException e1) {
			log.error("给用户发送微信消息失败:merchantId[" + merchantId + "], storeId[" + storeId + "], userId[" + userId + "],notifyMessage[" + notifyMessage + "];" + e1.getMessage());
		} catch (TException e) {
			log.error("给用户发送微信消息失败:merchantId[" + merchantId + "], storeId[" + storeId + "], userId[" + userId + "],notifyMessage[" + notifyMessage + "];" + e.getMessage());
		}
	}

	/**
	 * 发送订单锁定信息
<<<<<<< HEAD
	 * 
	 * @param storeOrder
=======
	 *
>>>>>>> master
	 */
	public void sendOrderLockMsg(StoreOrder storeOrder) {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		boolean locked = storeOrder.isOrderLocked();
		long userId = storeOrder.getUserId();
		HttpClient httpClient = this.httpClientFactory.getHttpClient();
		HttpParameters params = new HttpParameters();
		params.add("merchant_id", String.valueOf(merchantId));
		params.add("store_id", String.valueOf(storeId));
		params.add("user_id", String.valueOf(userId));
		if (locked) {
			params.add("status", "5wei_order_lock");
		} else {
			params.add("status", "5wei_order_unlock");
		}
		Map<String, Object> dataMap = Maps.newHashMap();
		dataMap.put("order_id", orderId);
		params.add("data", JsonUtil.build(dataMap));
		for (int i = 0; i < 3; i++) {
			try {
				HttpClient4Util.doPost(httpClient, this.getWechatNotifyUrl(), params, "UTF-8");
				break;
			} catch (Throwable e) {
				log.error(this.getWechatNotifyUrl() + ",params=" + JsonUtil.build(params) + ",ErrorMessage=" + e.getMessage());
			}
		}
	}

	private String buildContentForUserToTake(StoreOrder storeOrder, StorePickupSite storePickupSite){
		StringBuilder sb = new StringBuilder("");
		if(storeOrder != null && storePickupSite != null) {
			sb.append(storeOrder.getTakeSerialNumber()).append("号取餐通知")
					.append("\n------").append("\n请到").append(storePickupSite.getStorePickupSiteAddress()).append("取餐");
		}
		return sb.toString();
	}

	/**
	 * 自提点订单为一个自提点是一个大订单
	 * @param merchantId
	 * @param storeId
     * @param storeOrders
	 */
	public void notifyWechatForPickupSiteOrders(int merchantId, long storeId, List<StoreOrder> storeOrders){
		//取其中一个订单ID获取自提点信息
		StoreOrderDelivery storeOrderDelivery = this.storeOrderDAO.getDeliveryById(merchantId, storeId, storeOrders.get(0).getOrderId(), false, false);
		if(storeOrderDelivery != null) {
			StorePickupSite storePickupSite = this.storePickupSiteDAO.getStorePickupSiteById(merchantId, storeId, storeOrderDelivery.getStorePickupSiteId());
            for (StoreOrder storeOrder : storeOrders) {
                String content = this.buildContentForUserToTake(storeOrder, storePickupSite);
				if (storeOrder.getStoreOrderDelivery() == null) {
					continue;
				}
                if(StringUtils.isNotEmpty(content)) {
                    try {
                        this.notifyFacade.wechatSendSimple(merchantId, storeId, storeOrder.getUserId(), content, 0);
                    } catch(TException e) {
                        log.error("store pickup site notify wechat message failed: " + e.getMessage());
                    }
                }
            }
		}
	}

}
