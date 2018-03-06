package com.huofu.module.i5wei.printer;

import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreSendTypeEnum;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.printer.JsonMap.StoreTablePrinterJsonMap;
import com.huofu.module.i5wei.printer.JsonMap.StoreTableRecordPrinterJsonMap;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.table.dao.StoreAreaDAO;
import com.huofu.module.i5wei.table.entity.StoreArea;
import com.huofu.module.i5wei.table.entity.StoreTable;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;

/**
 * 转台后厨打印单
 * 
 * @author chenkai on 2016-06-17
 */
@Service
public class I5weiTransTablePrinter {
	
	private static final Log logger = LogFactory.getLog(I5weiTransTablePrinter.class);

	@Autowired
	private StoreAreaDAO storeAreaDAO;

	@Autowired
	private StoreMealTakeupDAO storeMealTakeupDAO;

	@Autowired
	private StoreMealCheckoutDAO storeMealCheckoutDAO;

	@Autowired
	private StoreMealPortDAO storeMealPortDAO;
	
	@Autowired
	private I5weiMessageProducer i5weiMessageProducer;

	@Autowired
	private Store5weiSettingService store5weiSettingService;

	@Autowired
	private StoreMealPortSendDAO storeMealPortSendDAO;

	/**
	 * 已点菜的桌台记录，都应该发送转台通知单
	 */
	public List<PrintMessageParam> getPrintMessages(StoreTableRecord srcTableRecord, StoreTable targetTable, List<String> orderIds) {
		boolean enableSlave = true;
		List<PrintMessageParam> printMessages = new ArrayList<PrintMessageParam>();
		if (srcTableRecord == null || targetTable == null) {
			return printMessages;
		}
		String targetAreaName = "";
		if (targetTable.getAreaId() == srcTableRecord.getAreaId()) {
			targetAreaName = srcTableRecord.getAreaName();
		} else {
			StoreArea storeArea = storeAreaDAO.getById(targetTable.getAreaId(), enableSlave);
			if (storeArea != null) {
				targetAreaName = storeArea.getAreaName();
			}
		}
		Map<String, Object> printMessage = new HashMap<String, Object>();
		// 原桌台记录信息
		printMessage.put("src_table_record", StoreTableRecordPrinterJsonMap.toMap(srcTableRecord));
		// 目标桌台信息
		printMessage.put("target_table", StoreTablePrinterJsonMap.toMap(targetTable, targetAreaName));
		// 需要通知的出餐口
		int merchantId = srcTableRecord.getMerchantId();
		long storeId = srcTableRecord.getStoreId();
		boolean forUpdate = false;
		List<StoreMealTakeup> storeMealTakeups = storeMealTakeupDAO.getStoreMealsByOrderIds(merchantId, storeId, orderIds, forUpdate, enableSlave);
		List<StoreMealCheckout> storeMealCheckouts = storeMealCheckoutDAO.getStoreMealsHistoryByOrderIds(merchantId, storeId, orderIds, forUpdate, enableSlave);
		Set<Long> portIds = new HashSet<Long>();
		if (storeMealTakeups != null) {
			for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
				portIds.add(storeMealTakeup.getPortId());
			}
		}
		if (storeMealCheckouts != null) {
			for (StoreMealCheckout storeMealCheckout : storeMealCheckouts) {
				portIds.add(storeMealCheckout.getPortId());
			}
		}
		if (portIds.isEmpty()) {
			return printMessages;
		}
		List<StoreMealPort> storeMealPorts = storeMealPortDAO.getByIds(merchantId, storeId, new ArrayList<>(portIds), true);
		//获取店铺打印模式
		Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId,storeId);
		int printMode = store5weiSetting.getPrintMode();
		Set<Long> storeMealSendPortIds = new HashSet<>();
		// 构造加工档口（出餐口）通知信息
		for (StoreMealPort storeMealPort : storeMealPorts) {
			storeMealSendPortIds.add(storeMealPort.getSendPortId());
			long printerPeripheralId = storeMealPort.getPrinterPeripheralId();
			if (printerPeripheralId <= 0) {
				continue;
			}
			PrintMessageParam param = this.getPrintMessageParam(merchantId,storeId,printerPeripheralId,srcTableRecord,printMessage);
			printMessages.add(param);
		}
		//构造高级模式传菜间的打印信息
		//高级模式,传菜口打印转台单
		if(printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
			//高级模式获取传菜口
			Map<Long,StoreMealPortSend> storeMealPortSendMap = storeMealPortSendDAO.getStoreMealPortSendMap(merchantId,storeId, StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue(),true);
			for(Long sendPortId : storeMealSendPortIds){
				StoreMealPortSend storeMealPortSend = storeMealPortSendMap.get(sendPortId);
				if(storeMealPortSend == null){
					continue;
				}
				if(storeMealPortSend.getPrinterPeripheralId() > 0){
						PrintMessageParam sendPortparam = this.getPrintMessageParam(merchantId,storeId,storeMealPortSend.getPrinterPeripheralId(),srcTableRecord,printMessage);
						sendPortparam.setPrinterPeripheralId(storeMealPortSend.getPrinterPeripheralId());
						printMessages.add(sendPortparam);
					}
				}
		}
		return printMessages;
	}

	public PrintMessageParam getPrintMessageParam(int merchantId,long storeId,long printerPeripheralId,StoreTableRecord srcTableRecord,Map<String, Object> printMessage){
		PrintMessageParam param = new PrintMessageParam();
		param.setMerchantId(merchantId);
		param.setStoreId(storeId);
		param.setPrinterPeripheralId(printerPeripheralId);
		param.setPrintSrc(PrintSrcEnum.I5WEI_TABLE_RECORD.getValue());
		param.setPrintSrcId(srcTableRecord.getTableRecordId() + "");
		param.setUserId(srcTableRecord.getCreateTableUserId());
		param.setStaffId(srcTableRecord.getStaffId());
		param.setMsgType(PrintMsgTypeEnum.I5WEI_TRANS_TABLE.getValue());
		param.setMsgContent(JsonUtil.build(printMessage));
		return param;
	}

	public void sendPrintMessages(long staffId, StoreTableRecord srcTableRecord, StoreTable targetTable, List<String> orderIds) {
		if(logger.isDebugEnabled()){
			logger.debug("####srcTableRecord="+JsonUtil.build(srcTableRecord)+", targetTable="+JsonUtil.build(targetTable));
		}
		List<PrintMessageParam> printMessages = this.getPrintMessages(srcTableRecord, targetTable, orderIds);
		i5weiMessageProducer.sendMultiPrintMessages(printMessages, staffId);
	}

}
