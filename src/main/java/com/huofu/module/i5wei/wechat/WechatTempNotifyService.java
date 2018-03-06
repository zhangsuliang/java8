package com.huofu.module.i5wei.wechat;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryInvset;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.OpDeliveryingResult;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.wechat.WechatTempDataHelper.TempContent;
import huofucore.facade.merchant.exception.TMerchantException;
import huofucore.facade.merchant.storealert.*;
import huofucore.facade.merchant.wechat.MerchantWechatFacade;
import huofucore.facade.merchant.wechat.WechatDTO;
import huofucore.facade.notify.NotifyFacade;
import huofucore.facade.notify.WechatNotifyTemplateParam;
import huofucore.facade.notify.WechatNotityTemplateType;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Service
public class WechatTempNotifyService {

	private static ResourceBundle resourceBundle = ResourceBundle.getBundle("i5weiconfig");
	
	@Autowired
	private WechatTempDataHelper wechatTempDataHelper;
	
	@Autowired
    private StoreTimeBucketService storeTimeBucketService;
	
	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@ThriftClient
	private StoreAlarmStaffSettingFacade.Iface storeAlarmStaffSettingFacade;
	
	@ThriftClient
	private NotifyFacade.Iface notifyFacade;

	@ThriftClient
	private MerchantWechatFacade.Iface merchantWechatFacadeIface;

	public String replaceParamsForURL(String key, List<String> params) {
		String result = resourceBundle.getString(key);
		if (CollectionUtils.isEmpty(params)) {
			return result;
		}
		for(int i = 0; i < params.size(); i ++) {
			result = result.replace("{" + i + "}", params.get(i));
		}
		return result;
	}

	/**
	 * 大单提醒
	 * 
	 * @param storeOrder
	 * @throws TMerchantException
	 * @throws TException
	 */
	public void sendBigOrderAlarm(StoreOrder storeOrder) throws TException {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		if (storeOrder.getUserId() <= 0 || storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
			return;
		}
		boolean bigOrderRemind = false;
		StoreAlarmSettingDTO storeAlarmSettingDTO = storeAlarmStaffSettingFacade.getStoreAlarmSetting(merchantId, storeId);
		if (storeAlarmSettingDTO.isEnableBigOrderRemind()) {
			if (storeOrder.getChargeItemPrice() >= storeAlarmSettingDTO.getBigOrderAmount()) {
				bigOrderRemind = true;
			}
		}
		if (!bigOrderRemind) {
			return;
		}
		if (storeOrder.getStoreTimeBucket() == null) {
			StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, storeOrder.getTimeBucketId(), true);
			storeOrder.setStoreTimeBucket(storeTimeBucket);
		}
		if (storeOrder.getStoreOrderItems() == null || storeOrder.getStoreOrderItems().isEmpty()) {
			storeOrderHelper.setStoreOrderDetail(storeOrder, true);
		}
		Map<String, TempContent> data = wechatTempDataHelper.buildBigOrderAlarmData(storeOrder);
		this.sendWechatTemplateNotify(merchantId, storeId, StoreBusinAlarmTypeEnum.BIG_ORDER_REMIND, data, null);
	}

	/**
	 * 非周期性库存报警
	 * 
	 * @param storeInventoryInvsets
	 * @throws TMerchantException
	 * @throws TException
	 */
	public void sendInventoryNotEnoughAlarm(StoreOrder storeOrder, List<StoreInventoryInvset> storeInventoryInvsets) throws TException {
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		if (storeInventoryInvsets == null || storeInventoryInvsets.isEmpty()) {
			return;
		}
		List<StoreInventoryInvset> inventoryInvsets = new ArrayList<StoreInventoryInvset>();
		for (StoreInventoryInvset storeInventoryInvset : storeInventoryInvsets) {
			if (storeInventoryInvset.isInvAlarm()) {
				inventoryInvsets.add(storeInventoryInvset);
			}
		}
		if (inventoryInvsets == null || inventoryInvsets.isEmpty()) {
			return;
		}
		boolean fixInventoryAlarm = false;
		StoreAlarmSettingDTO storeAlarmSettingDTO = storeAlarmStaffSettingFacade.getStoreAlarmSetting(merchantId, storeId);
		if (storeAlarmSettingDTO.isEnableInventoryNotEnough()) {
			fixInventoryAlarm = true;
		}
		if (!fixInventoryAlarm) {
			return;
		}
		Map<String, TempContent> data = wechatTempDataHelper.buildInventoryNotEnoughData(storeOrder, inventoryInvsets);
		this.sendWechatTemplateNotify(merchantId, storeId, StoreBusinAlarmTypeEnum.INVENTORY_NOT_ENOUGH, data, null);
	}

	/**
	 * 配送员配送提醒
	 * @param merchantId
	 * @param storeId
	 * @param result
	 * @throws TException
	 */
	public void remindDeliveryStaffDelivery(int merchantId, long storeId, OpDeliveryingResult result) throws TException {
		if (result == null || merchantId == 0 || storeId == 0 || result.getDeliverStaffUserId() == 0 || result.getStoreOrders() == null) {
			return;
		}
		Map<String, TempContent> data = wechatTempDataHelper.buildDeliveringRemindData(result);
		WechatNotifyTemplateParam wechatNotifyTemplateParam = new WechatNotifyTemplateParam();
		wechatNotifyTemplateParam.setData(JsonUtil.build(data));
		wechatNotifyTemplateParam.setUserIds(Lists.newArrayList(result.getDeliverStaffUserId()));
		wechatNotifyTemplateParam.setTemplateType(WechatNotityTemplateType.DELIVERY_ORDER_REMIND);
		wechatNotifyTemplateParam.setUrl(replaceParamsForURL("deliverystaff.delivery.url", Lists.newArrayList()));
		notifyFacade.wechatNotifyTemplateSend(wechatNotifyTemplateParam);
	}

	private String getTakeCodeURL(StoreOrder storeOrder) throws TException {
		List<String> params = Lists.newArrayList();
		params.add(String.valueOf(storeOrder.getMerchantId()));
		params.add(String.valueOf(storeOrder.getStoreId()));
		params.add(String.valueOf(storeOrder.getOrderId()));
		WechatDTO wechatDTO = merchantWechatFacadeIface.getMerchantWechatListByMerchantId(storeOrder.getMerchantId(), storeOrder.getStoreId());
		if(wechatDTO == null) {
			throw new TException("this store does not have gzid");
		}
		params.add(String.valueOf(wechatDTO.getGzId()));
		return this.replaceParamsForURL("takecode.remind.url", params);
	}

	public void sendWechatMsgForTakeCode(StoreOrder storeOrder) throws TException {
		Map<String, TempContent> result = wechatTempDataHelper.buildMsgForTakeCode(storeOrder);
		WechatNotifyTemplateParam wechatNotifyTemplateParam = new WechatNotifyTemplateParam();
		wechatNotifyTemplateParam.setData(JsonUtil.build(result));
		wechatNotifyTemplateParam.setUserIds(Lists.newArrayList(storeOrder.getUserId()));
		wechatNotifyTemplateParam.setTemplateType(WechatNotityTemplateType.TAKE_CODE_REMIND);
		wechatNotifyTemplateParam.setUrl(getTakeCodeURL(storeOrder));
		notifyFacade.wechatNotifyTemplateSend(wechatNotifyTemplateParam);
	}

	private void sendWechatTemplateNotify(int merchantId, long storeId, StoreBusinAlarmTypeEnum storeBusinAlarmType, Map<String, TempContent> data, String url) throws TException{
		if (merchantId == 0 || storeId == 0 || storeBusinAlarmType == null || data == null || data.isEmpty()) {
			return;
		}
		StoreAlarmStaffParam storeAlarmStaffParam = new StoreAlarmStaffParam();
		storeAlarmStaffParam.setMerchantId(merchantId);
		storeAlarmStaffParam.setStoreId(storeId);
		storeAlarmStaffParam.setBusinAlarmType(storeBusinAlarmType.getValue());
		storeAlarmStaffParam.setEnableAlarm(true);
		storeAlarmStaffParam.setLoadUser(true);
		List<StoreAlarmDTO> storeAlarmDTOs = storeAlarmStaffSettingFacade.getStoreAlarmStaffList(storeAlarmStaffParam);
		if (storeAlarmDTOs == null || storeAlarmDTOs.isEmpty()) {
			return;
		}
		List<Long> userIds = new ArrayList<Long>();
		for (StoreAlarmDTO storeAlarmDTO : storeAlarmDTOs) {
			userIds.add(storeAlarmDTO.getStoreAlarmStaffDTO().getUserId());
		}
		WechatNotifyTemplateParam wechatNotifyTemplateParam = new WechatNotifyTemplateParam();
		wechatNotifyTemplateParam.setData(JsonUtil.build(data));
		wechatNotifyTemplateParam.setUserIds(userIds);
		if (url != null) {
			wechatNotifyTemplateParam.setUrl(url);
		}
		if(storeBusinAlarmType.equals(StoreBusinAlarmTypeEnum.BIG_ORDER_REMIND)){
			wechatNotifyTemplateParam.setTemplateType(WechatNotityTemplateType.BIG_ORDER);
		}else if(storeBusinAlarmType.equals(StoreBusinAlarmTypeEnum.INVENTORY_NOT_ENOUGH)){
			wechatNotifyTemplateParam.setTemplateType(WechatNotityTemplateType.INVENTORY_NOT_ENOUGH);
		}else{
			return;
		}
		notifyFacade.wechatNotifyTemplateSend(wechatNotifyTemplateParam);
	}
	

}
