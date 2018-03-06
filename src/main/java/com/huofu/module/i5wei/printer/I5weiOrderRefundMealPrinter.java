package com.huofu.module.i5wei.printer;

import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepTypeEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.merchant.printer.PrintMessageParam;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofucore.facade.merchant.printer.PrintSrcEnum;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.printer.JsonMap.StoreOrderPrinterJsonMap;
import com.huofu.module.i5wei.printer.JsonMap.StoreOrderRefundItemJsonMap;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;

@Service
public class I5weiOrderRefundMealPrinter {

	private static final Log logger = LogFactory.getLog(I5weiOrderRefundMealPrinter.class);

	@Autowired
	private StoreMealPortDAO storeMealPortDAO;

	@Autowired
	private StoreOrderDAO storeOrderDAO;
	
	@Autowired
	private StorePrinterHelper storePrinterHelper;
	
	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@Autowired
	private I5weiMessageProducer i5weiMessageProducer;

	@Autowired
	private Store5weiSettingService store5weiSettingService;

	@Autowired
	private StoreMealPortSendDAO storeMealPortSendDAO;

	@Autowired
	private StoreMealTakeupDAO storeMealTakeupDAO;

	public List<PrintMessageParam> getPrintMessages(List<StoreOrderRefundItem> storeOrderRefundItems){
		boolean enableSlave = false;
		List<PrintMessageParam> printMessages = new ArrayList<PrintMessageParam>();
		if (storeOrderRefundItems == null || storeOrderRefundItems.isEmpty()) {
			return printMessages;
		}
		int merchantId = storeOrderRefundItems.get(0).getMerchantId();
		long storeId = storeOrderRefundItems.get(0).getStoreId();
		// 退菜信息分出餐口
		Map<Long, Set<StoreOrderRefundItem>> portStoreOrderRefundMap = storePrinterHelper.getPortStoreOrderRefundMap(storeOrderRefundItems, enableSlave);
		// 相关订单
		String orderId = storeOrderRefundItems.get(0).getOrderId();
		StoreOrder storeOrder = storeOrderDAO.getStoreOrderById(merchantId, storeId, orderId, enableSlave);
		storeOrderHelper.setStoreOrderDetail(storeOrder, enableSlave);
		storeOrderHelper.setStoreOrderRefundItem(storeOrder, enableSlave);
		List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
		if (storeOrderItems == null || storeOrderItems.isEmpty()) {
			return printMessages;
		}
		Map<Long, StoreOrderItem> refundItemMap = new HashMap<Long, StoreOrderItem>();
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			refundItemMap.put(storeOrderItem.getChargeItemId(), storeOrderItem);
		}
		// 出餐口信息
		Map<Long, StoreMealPort> mealPortMap = storeMealPortDAO.getMap(merchantId, storeId, enableSlave);
		//获取店铺打印模式
		Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId,storeId);
		int printMode = store5weiSetting.getPrintMode();
		//高级模式获取传菜口
		Map<Long,StoreMealPortSend> storeMealPortSendMap = new HashMap<>();
		if(printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
			storeMealPortSendMap = storeMealPortSendDAO.getStoreMealPortSendMap(merchantId,storeId, StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue(),true);
		}
		//传菜间打印信息
		Map<String,Map<String, Object>> sendPortRefundItemsMap = new HashMap<>();
		// 构造打印信息
		for (long portId : portStoreOrderRefundMap.keySet()) {
			Set<StoreOrderRefundItem> refundItems = portStoreOrderRefundMap.get(portId);
			StoreMealPort storeMealPort = mealPortMap.get(portId);
			if (storeMealPort == null) {
				continue;
			}
			long printerPeripheralId = storeMealPort.getPrinterPeripheralId();
			if (printerPeripheralId <= 0) {
				continue;
			}
			Map<String, Object> printMessage = new HashMap<String, Object>();
			// 相关信息
			int printSrc = 0;
			String printSrcId = "";
			long userId = 0;
			long staffId = 0;
			if (storeOrder != null) {
				printMessage.put("store_order", StoreOrderPrinterJsonMap.toMap(storeOrder));
				printMessage.put("refund_reason", storeOrderRefundItems.get(0).getRefundReason());
				printSrc = PrintSrcEnum.I5WEI_ORDER.getValue();
				printSrcId = storeOrder.getOrderId();
				userId = storeOrder.getUserId();
			}
			List<Map<String, Object>> refundItemMessages = new ArrayList<Map<String, Object>>();
			for (StoreOrderRefundItem refundItem : refundItems) {
				staffId = refundItem.getStaffId();
				long chargeItemId = refundItem.getChargeItemId();
				StoreOrderItem storeOrderItem = refundItemMap.get(chargeItemId);
				if (storeOrderItem == null) {
					continue;
				}
				Map<String, Object> refundItemMessage = StoreOrderRefundItemJsonMap.toMap(refundItem, storeOrderItem);
				refundItemMessages.add(refundItemMessage);
			}
			printMessage.put("refund_item_messages", refundItemMessages);
			//没有退菜信息则直接返回
			if(refundItemMessages.isEmpty()){
				return printMessages;
			}
			this.addPrintMessages(merchantId,storeId,printerPeripheralId,printSrc,printSrcId,userId,staffId,printMessage,printMessages);
			//构造传菜间打印信息
			sendPortRefundItemsMap.put(storeMealPort.getSendPortId() + "_" + staffId,printMessage);
		}
		//高级打印模式
		if(printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
			for(String key : sendPortRefundItemsMap.keySet()){
				String[] keys = key.split("_");
				long sendPortId = Long.valueOf(keys[0]);
				long staffId = Long.valueOf(keys[1]);
				//获取传菜口
				StoreMealPortSend storeMealPortSend = storeMealPortSendMap.get(sendPortId);
				Map<String,Object> printMessage = sendPortRefundItemsMap.get(key);
				if(storeMealPortSend == null || printMessage == null || printMessage.isEmpty()){
					continue;
				}
				//纸划菜
				if(storeMealPortSend.getSweepType() == StoreMealSweepTypeEnum.PAPER_SWEEP.getValue()){
					if(storeMealPortSend.getPrinterPeripheralId() > 0){
						this.addPrintMessages(merchantId,storeId,storeMealPortSend.getPrinterPeripheralId(),PrintSrcEnum.I5WEI_ORDER.getValue(),storeOrder.getOrderId(),storeOrder.getUserId(),staffId,printMessage,printMessages);
					}
				}
			}
		}
		return printMessages;
	}

	public void addPrintMessages(int merchantId,long storeId,long printerPeripheralId,int printSrc,String printSrcId,long userId,long staffId,Map<String,Object> printMessage,List<PrintMessageParam> printMessages){
		PrintMessageParam param = new PrintMessageParam();
		param.setMerchantId(merchantId);
		param.setStoreId(storeId);
		param.setPrinterPeripheralId(printerPeripheralId);
		param.setPrintSrc(printSrc);
		param.setPrintSrcId(printSrcId);
		param.setUserId(userId);
		param.setStaffId(staffId);
		param.setMsgType(PrintMsgTypeEnum.I5WEI_ORDER_REFUND_MEAL.getValue());
		param.setMsgContent(JsonUtil.build(printMessage));
		param.setCreateTime(System.currentTimeMillis());
		printMessages.add(param);
	}

	public void sendPrintMessages(long staffId, List<StoreOrderRefundItem> storeOrderRefundItems) throws T5weiException {
		if (logger.isDebugEnabled()) {
			logger.debug("####storeOrderRefundItems=" + JsonUtil.build(storeOrderRefundItems));
		}
		List<PrintMessageParam> printMessages = this.getPrintMessages(storeOrderRefundItems);
		// 发送消息
		i5weiMessageProducer.sendMultiPrintMessages(printMessages,staffId);
	}

}
