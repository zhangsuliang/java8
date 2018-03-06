package com.huofu.module.i5wei.wechat;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryInvset;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.service.OpDeliveryingResult;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteService;
import huofucore.facade.i5wei.order.StoreOrderPayStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.merchant.exception.TMerchantException;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.StoreFacade;
import huofucore.facade.merchant.store.query.StoreQueryFacade;
import huofucore.facade.user.info.UserDTO;
import huofucore.facade.user.info.UserFacade;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.MoneyUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WechatTempDataHelper {

	@ThriftClient
	private StoreQueryFacade.Iface storeQueryFacade;

	@ThriftClient
	private UserFacade.Iface userFacade;

	@Autowired
	private StorePickupSiteService pickupSiteService;

	@ThriftClient
	private StoreFacade.Iface storeFacadeIface;

	public class TempContent {

		private String value;
		private String color;

		public TempContent(String value, String color) {
			this.value = value;
			this.color = color;
		}

		public TempContent(String value) {
			this.value = value;
			// color为空默认黑色
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public String getColor() {
			return color;
		}

		public void setColor(String color) {
			this.color = color;
		}
	}

	/**
	 * 大单提醒
	 * 
	 * @param storeOrder
	 * @return 
	 * @throws TException
	 */
	public Map<String, TempContent> buildBigOrderAlarmData(StoreOrder storeOrder) throws TException {
		Map<String, TempContent> resultData = new HashMap<String, TempContent>();
		if (storeOrder.getPayStatus() != StoreOrderPayStatusEnum.FINISH.getValue()) {
			return resultData;
		}
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String timeBucketName = "";
		if (storeOrder.getStoreTimeBucket() != null) {
			timeBucketName = storeOrder.getStoreTimeBucket().getName();
		}
		StoreDTO storeDTO = storeQueryFacade.getStore(merchantId, storeId);
		String createTime = DateUtil.formatDate("MM月dd日", new Date(storeOrder.getCreateTime()));
		String repastDate = DateUtil.formatDate("MM月dd日", new Date(storeOrder.getRepastDate()));
		String takeModeName = storeOrder.getTakeModeName();
		// 备注内容构造
		StringBuilder remark = new StringBuilder();
		remark.append("下单店铺：").append(storeDTO.getName()).append("\n");
		remark.append("就餐日期：").append(repastDate).append(timeBucketName).append("\n");
		remark.append("就餐方式：").append(takeModeName).append("\n");
		if (storeOrder.getUserId() > 0) {
			UserDTO userDTO = userFacade.getUserByUserId(storeOrder.getUserId());
			remark.append("下单用户：").append(userDTO.getName()).append("\n");
			remark.append("用户手机：");
			if (DataUtil.isEmpty(userDTO.getMobile())) {
				remark.append("未绑定");
			} else {
				remark.append(userDTO.getMobile());
			}
			remark.append("\n");
		}
		remark.append("点单内容：");
		int i = 1;
		for (StoreOrderItem storeOrderItem : storeOrder.getStoreOrderItems()) {
			String num = String.valueOf(storeOrderItem.getAmount());
			num = num.replace(".0", "");
			remark.append(storeOrderItem.getChargeItemName()).append("x").append(num);
			if (i < storeOrder.getStoreOrderItems().size()) {
				remark.append("、");
			}
			i++;
		}
		// 构造模板内容
		resultData.put("first", new TempContent(storeDTO.getName() + createTime + "有大额订单，菜品总价"+MoneyUtil.getMoney(storeOrder.getChargeItemPrice())+"元，请关注!"));
		resultData.put("OrderSn", new TempContent(storeOrder.getOrderId()));
		resultData.put("OrderStatus", new TempContent("已支付"));
		resultData.put("remark", new TempContent(remark.toString()));
		return resultData;
	}

	/**
	 * 非周期性库存报警
	 * 
	 * @param storeInventoryInvsets
	 * @return 
	 * @throws TException
	 * @throws TMerchantException
	 */
	public Map<String, TempContent> buildInventoryNotEnoughData(StoreOrder storeOrder, List<StoreInventoryInvset> storeInventoryInvsets) throws TException {
		Map<String, TempContent> resultData = new HashMap<String, TempContent>();
		if (storeInventoryInvsets == null || storeInventoryInvsets.isEmpty()) {
			return resultData;
		}
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		StoreDTO storeDTO = storeQueryFacade.getStore(merchantId, storeId);
		// 表单页眉
		StringBuilder first = new StringBuilder();
		if (storeInventoryInvsets.size() == 1) {
			StoreInventoryInvset inventoryInvset = storeInventoryInvsets.get(0);
			String num = String.valueOf(inventoryInvset.getAlarmAmount());
			num = num.replace(".0", "");
			first.append("“" + inventoryInvset.getName() + "”").append("当前数量不足").append(num).append(inventoryInvset.getUnit()).append("，请及时补货");
		} else {
			int i = 1;
			for (StoreInventoryInvset inventoryInvset : storeInventoryInvsets) {
				first.append(inventoryInvset.getName());
				if (i < storeInventoryInvsets.size()) {
					first.append("、");
				}
				i++;
			}
			first.append("当前库存数量不足，请及时补货");
		}
		//店铺名称
		StringBuilder keyword1 = new StringBuilder();
		keyword1.append(storeDTO.getName());
		//产品名称
		StringBuilder keyword2 = new StringBuilder();
		if (storeInventoryInvsets.size() == 1) {
			StoreInventoryInvset inventoryInvset = storeInventoryInvsets.get(0);
			keyword2.append(inventoryInvset.getName());
		} else {
			int i = 1;
			for (StoreInventoryInvset inventoryInvset : storeInventoryInvsets) {
				keyword2.append(inventoryInvset.getName());
				if (i < storeInventoryInvsets.size()) {
					keyword2.append("、");
				}
				i++;
			}
		}
		//剩余数量
		StringBuilder keyword3 = new StringBuilder();
		if (storeInventoryInvsets.size() == 1) {
			StoreInventoryInvset inventoryInvset = storeInventoryInvsets.get(0);
			String num = String.valueOf(inventoryInvset.getRemain());
			num = num.replace(".0", "");
			keyword3.append(inventoryInvset.getName());
			keyword3.append(num).append(inventoryInvset.getUnit());
		} else {
			int i = 1;
			for (StoreInventoryInvset inventoryInvset : storeInventoryInvsets) {
				String num = String.valueOf(inventoryInvset.getRemain());
				num = num.replace(".0", "");
				keyword3.append(inventoryInvset.getName());
				keyword3.append(num).append(inventoryInvset.getUnit());
				if (i < storeInventoryInvsets.size()) {
					keyword3.append("、");
				}
				i++;
			}
		}
		// 构造模板内容
		resultData.put("first", new TempContent(first.toString()));
		resultData.put("keyword1", new TempContent(keyword1.toString()));
		resultData.put("keyword2", new TempContent(keyword2.toString()));
		resultData.put("keyword3", new TempContent(keyword3.toString()));
		resultData.put("remark", new TempContent("补货后，请通过 “库存盘点” 修改新的库存数量"));
		return resultData;
	}

	public Map<String, TempContent> buildDeliveringRemindData(OpDeliveryingResult result) {
		Map<String, TempContent> resultData = new HashMap<>();
		StringBuilder first = new StringBuilder("您有").append(result.getNewAdd()).append("个新配送任务\n");
		String keyword1 = null;
		List<StoreOrder> storeOrders = result.getStoreOrders();
		if (CollectionUtils.isNotEmpty(storeOrders)) {
			keyword1 = buildOrderSerialNumber(storeOrders);
		}
		if (StringUtils.isEmpty(keyword1)) {
			return resultData;
		}
		if (keyword1.endsWith(",")) {
			keyword1 = keyword1.substring(0, keyword1.lastIndexOf(","));
		}
		StringBuilder totalNotDeliveried = new StringBuilder("待配送总量：").append(result.getTotal()).append("个配送任务待完成\n");
		//构造模板内容
		resultData.put("first", new TempContent(first.toString()));
		resultData.put("keyword1", new TempContent(new StringBuilder("订单号：").append(keyword1).append("\n").toString()));
		resultData.put("keyword2", new TempContent(totalNotDeliveried.toString()));
		return resultData;
	}

	public String buildOrderSerialNumber(List<StoreOrder> storeOrders) {
		StringBuilder orderNumber = new StringBuilder("");
		List<Integer> publicNumber = Lists.newArrayList();
		List<Integer> meituanNumbers = Lists.newArrayList();
		List<Integer> elmNumbers = Lists.newArrayList();
		List<Integer> baiduNumbers = Lists.newArrayList();
		List<Long> pickupSiteIds = Lists.newArrayList();
		for (StoreOrder storeOrder : storeOrders) {
			if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
				switch (storeOrder.getWaimaiType()) {
					case 0 :
						publicNumber.add(storeOrder.getTakeSerialNumber());
						break;
					case 1:
						meituanNumbers.add(storeOrder.getTakeSerialNumber());
						break;
					case 2:
						elmNumbers.add(storeOrder.getTakeSerialNumber());
						break;
					case 3:
						baiduNumbers.add(storeOrder.getTakeSerialNumber());
						break;
					case 4:
						if (storeOrder.getStoreOrderDelivery() != null &&
								!pickupSiteIds.contains(storeOrder.getStoreOrderDelivery().getStorePickupSiteId())) {
							pickupSiteIds.add(storeOrder.getStoreOrderDelivery().getStorePickupSiteId());
						}
						break;
				}
			}
		}

		//构造公众号订单信息
		if (CollectionUtils.isNotEmpty(publicNumber)) {
			for(int serialNo : publicNumber) {
				orderNumber.append(serialNo).append("号,");
			}
		}

		//构造美团订单信息
		if (CollectionUtils.isNotEmpty(meituanNumbers)) {
			for(int serialNo : meituanNumbers) {
				orderNumber.append("美团").append(serialNo).append("号,");
			}
		}

		//构造饿了么订单信息
		if (CollectionUtils.isNotEmpty(elmNumbers)) {
			for(int serialNo : elmNumbers) {
				orderNumber.append("饿了么").append(serialNo).append("号,");
			}
		}

		//构造百度外卖订单信息
		if (CollectionUtils.isNotEmpty(baiduNumbers)) {
			for(int serialNo : baiduNumbers) {
				orderNumber.append("百度").append(serialNo).append("号,");
			}
		}

		//构造自提点
		if (CollectionUtils.isNotEmpty(pickupSiteIds)) {
			List<StorePickupSite> storePickupSites = pickupSiteService.getStorePickupSitesByStoreIds(
					storeOrders.get(0).getMerchantId(), storeOrders.get(0).getStoreId(), pickupSiteIds);
			if (CollectionUtils.isNotEmpty(storePickupSites)) {
				for(StorePickupSite pickupSite : storePickupSites) {
					orderNumber.append(pickupSite.getStorePickupSiteName()).append("订单,");
				}
			}
		}
		return orderNumber.toString();
	}

	public Map<String, TempContent> buildMsgForTakeCode(StoreOrder storeOrder) throws TException {
		Map<String, TempContent> resultData = new HashMap<>();
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String first = "下单成功，稍候即可用餐";
		StringBuilder keyword1 = new StringBuilder();
		if (storeOrder.getSiteNumber() > 0) {
			keyword1.append("餐牌号").append(storeOrder.getSiteNumber()).append("（");
		} else {
			keyword1.append(storeOrder.getTakeSerialNumber()).append("号（");
		}
		if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
			keyword1.append("堂食）");
		} else if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
			keyword1.append("打包）");
		} else if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
			keyword1.append("堂食加打包）");
		} else {
			return resultData;
		}
		StringBuilder keyword2 = new StringBuilder();
		StoreDTO storeDTO = storeFacadeIface.getStore(merchantId, storeId);
		if (storeDTO != null) {
			keyword2.append(storeDTO.getName());
		}
		StringBuilder keyword3 = new StringBuilder();
		keyword3.append(DateUtil.formatDate(DateUtil.format_ms, new Date(storeOrder.getCreateTime())));
		StringBuilder keyword4 = new StringBuilder();
		if (CollectionUtils.isNotEmpty(storeOrder.getStoreOrderItems())) {
			String replaceStr = "";
			for (StoreOrderItem storeOrderItem : storeOrder.getStoreOrderItems()) {
				if (keyword4.length() >= 30) {
					replaceStr = "...";
					break;
				}
				keyword4.append(storeOrderItem.getChargeItemName()).append("x").append((int)(storeOrderItem.getAmount())).append(",");
			}
			if(keyword4.toString().endsWith(",")) {
				keyword4.replace(keyword4.lastIndexOf(","), keyword4.length(), replaceStr);
			}
		}
		StringBuilder keyword5 = new StringBuilder();
		keyword5.append(MoneyUtil.getMoney(storeOrder.getActualPrice())).append("元");
		resultData.put("first", new TempContent(first));
		resultData.put("keyword1", new TempContent(keyword1.toString()));
		resultData.put("keyword2", new TempContent(keyword2.toString()));
		resultData.put("keyword3", new TempContent(keyword3.toString()));
		resultData.put("keyword4", new TempContent(keyword4.toString()));
		resultData.put("keyword5", new TempContent(keyword5.toString()));
		resultData.put("remark", new TempContent("点击查看取餐单"));
		return resultData;
	}
}
