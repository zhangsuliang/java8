package com.huofu.module.i5wei.printer;

import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class StorePrinterHelper {

	private static final Log logger = LogFactory.getLog(StorePrinterHelper.class);

	@Autowired
	private StoreMealTakeupDAO storeMealTakeupDAO;
	
	@Autowired
	private StoreMealCheckoutDAO storeMealCheckoutDAO;

	@Autowired
	private Store5weiSettingService store5weiSettingService;

	
	public Map<Long, Set<StoreOrderRefundItem>> getPortStoreOrderRefundMap(List<StoreOrderRefundItem> storeOrderRefundItems, boolean enableSlave){
		// 按出餐口分拆打印信息
		Map<Long, Set<StoreOrderRefundItem>> portStoreOrderRefundMap = new HashMap<Long, Set<StoreOrderRefundItem>>();
		if (storeOrderRefundItems == null || storeOrderRefundItems.isEmpty()) {
			return portStoreOrderRefundMap;
		}
		int merchantId = storeOrderRefundItems.get(0).getMerchantId();
		long storeId = storeOrderRefundItems.get(0).getStoreId();
		Map<String, StoreOrderRefundItem> storeOrderRefundMap = new HashMap<String, StoreOrderRefundItem>();
		Set<String> orderIds = new HashSet<String>();
		for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems) {
			String orderId = storeOrderRefundItem.getOrderId();
			String key = orderId + "_" + storeOrderRefundItem.getChargeItemId() + "_" + storeOrderRefundItem.isPacked();
			orderIds.add(orderId);
			storeOrderRefundMap.put(key, storeOrderRefundItem);
		}
		Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId,storeId);
		int printMode = store5weiSetting.getPrintMode();
		boolean forUpdate = false;
		//相关待出餐信息
		List<StoreMealTakeup> storeMealTakeups = storeMealTakeupDAO.getStoreMealTakeupsByOrderIds(merchantId, storeId, new ArrayList<>(orderIds), false,forUpdate, enableSlave);
		// 相关已出餐信息
		List<StoreMealCheckout> storeMealCheckouts = storeMealCheckoutDAO.getStoreMealsHistoryAllByOrderIds(merchantId, storeId, new ArrayList<>(orderIds), forUpdate, enableSlave);
		if (storeMealTakeups != null) {
			for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
				String orderId = storeMealTakeup.getOrderId();
				String key = orderId + "_" + storeMealTakeup.getChargeItemId() + "_" + storeMealTakeup.isPackaged();
				StoreOrderRefundItem storeOrderRefundItem = storeOrderRefundMap.get(key);
				if (storeOrderRefundItem == null) {
					continue;
				}
				long portId = storeMealTakeup.getPortId();
				Set<StoreOrderRefundItem> storeOrderRefundItemSet = portStoreOrderRefundMap.get(portId);
				if (storeOrderRefundItemSet == null) {
					storeOrderRefundItemSet = new HashSet<StoreOrderRefundItem>();
				}
				//高级打印模式
				if(store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()){
					storeOrderRefundItem.setSendPortId(storeMealTakeup.getSendPortId());
				}
				storeOrderRefundItemSet.add(storeOrderRefundItem);
				portStoreOrderRefundMap.put(portId, storeOrderRefundItemSet);
			}
		}
		if (storeMealCheckouts != null) {
			for (StoreMealCheckout storeMealCheckout : storeMealCheckouts) {
				String orderId = storeMealCheckout.getOrderId();
				String key = orderId + "_" + storeMealCheckout.getChargeItemId() + "_" + storeMealCheckout.isPackaged();
				StoreOrderRefundItem storeOrderRefundItem = storeOrderRefundMap.get(key);
				if (storeOrderRefundItem == null) {
					continue;
				}
				long portId = storeMealCheckout.getPortId();
				Set<StoreOrderRefundItem> storeOrderRefundItemSet = portStoreOrderRefundMap.get(portId);
				if (storeOrderRefundItemSet == null) {
					storeOrderRefundItemSet = new HashSet<StoreOrderRefundItem>();
				}
				storeOrderRefundItemSet.add(storeOrderRefundItem);
				portStoreOrderRefundMap.put(portId, storeOrderRefundItemSet);
			}
		}
		return portStoreOrderRefundMap;
	}

	//组装每个加工档口需要打印的StoreMealDTO（高级模式）,只打印堂食的部分
	public Map<String,List<StoreMealDTO>> getStoreMealMap4AdvancePrinter(List<StoreMealDTO> storeMealDTOs, Map<Long,StoreMealPort> storeMealPortMap){
		Map<String,List<StoreMealDTO>> map = new HashMap<>();
		if(storeMealDTOs != null && !storeMealDTOs.isEmpty()){
			for(StoreMealDTO storeMealDTO : storeMealDTOs){
				if(storeMealDTO.isPackaged()){
					continue;
				}
				StoreMealPort storeMealPort = storeMealPortMap.get(storeMealDTO.getPortId());
				if (storeMealPort == null) {
					continue;
				}
				//加工档口ID__传菜口ID
				String key = storeMealDTO.getPortId() + "_" + storeMealPort.getSendPortId();
				List<StoreMealDTO> storeMealDTOList = map.get(key);
				//将同一个加工档口需要打印的东西合到一起
				storeMealDTOList = this.deal(storeMealDTOList,storeMealDTO);
				map.put(key,storeMealDTOList);
			}
		}
		return map;
	}

	//组装每个出餐口需要打印的StoreMealDTO(普通模式)
	public Map<Long,List<StoreMealDTO>> getStoreMealMap4NormalPrinter(List<StoreMealDTO> storeMealDTOs){
		Map<Long,List<StoreMealDTO>> map = new HashMap<>();
		if(storeMealDTOs != null && !storeMealDTOs.isEmpty()){
			for(StoreMealDTO storeMealDTO : storeMealDTOs){
				if(storeMealDTO.isPackaged()){
					continue;
				}
				List<StoreMealDTO> storeMealDTOList = map.get(storeMealDTO.getPortId());
				//将同一个出餐口需要打印的收费项目合到一起
				storeMealDTOList = this.deal(storeMealDTOList,storeMealDTO);
				map.put(storeMealDTO.getPortId(),storeMealDTOList);
			}
		}
		return map;
	}


	private List<StoreMealDTO> deal(List<StoreMealDTO> storeMealDTOList, StoreMealDTO storeMealDTO){
		if(storeMealDTOList == null || storeMealDTOList.isEmpty()){
			storeMealDTOList = new ArrayList<>();
			storeMealDTOList.add(storeMealDTO);
		}else{
			boolean isContain = false;
			for(StoreMealDTO dto : storeMealDTOList){
				if(dto.getPortId() == storeMealDTO.getPortId()){
					List<StoreMealChargeDTO> storeMealChargeDTOs = dto.getStoreMealChargeDTOs();
					if(storeMealChargeDTOs != null && !storeMealChargeDTOs.isEmpty()){
						storeMealChargeDTOs.addAll(storeMealDTO.getStoreMealChargeDTOs());
						dto.setStoreMealChargeDTOs(storeMealChargeDTOs);
						isContain = true;
					}
					break;
				}
			}
			if(!isContain){
				storeMealDTOList.add(storeMealDTO);
			}
		}
		return storeMealDTOList;
	}

}
