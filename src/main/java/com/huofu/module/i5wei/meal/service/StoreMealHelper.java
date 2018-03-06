package com.huofu.module.i5wei.meal.service;

import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.meal.StoreMealProductDTO;
import huofucore.facade.i5wei.menu.StoreProductDTO;
import huofuhelper.util.bean.BeanUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.service.QueryProductPortParam;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderSubitem;

@Component
public class StoreMealHelper {
	
	public void sortStoreMealList(List<StoreMealDTO> storeMealList) {
		ComparatorStoreMeal comparator = new ComparatorStoreMeal();
		Collections.sort(storeMealList, comparator);
	}
	
	public void sortStoreMealListDesc(List<StoreMealDTO> storeMealList) {
		ComparatorStoreMealDesc comparator = new ComparatorStoreMealDesc();
		Collections.sort(storeMealList, comparator);
	}
	
	public class ComparatorStoreMeal implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreMealDTO meal0 = (StoreMealDTO) arg0;
			StoreMealDTO meal1 = (StoreMealDTO) arg1;
			int flag = Integer.valueOf(meal0.getTakeSerialNumber()).compareTo(meal1.getTakeSerialNumber());
			if (flag == 0) {
				return Integer.valueOf(meal0.getTakeSerialSeq()).compareTo(meal1.getTakeSerialSeq());
			} else {
				return flag;
			}
		}
	}
	
	public class ComparatorStoreMealDesc implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreMealDTO meal0 = (StoreMealDTO) arg0;
			StoreMealDTO meal1 = (StoreMealDTO) arg1;
			int flag = Integer.valueOf(meal1.getTakeSerialNumber()).compareTo(meal0.getTakeSerialNumber());
			if (flag == 0) {
				return Integer.valueOf(meal1.getTakeSerialSeq()).compareTo(meal0.getTakeSerialSeq());
			} else {
				return flag;
			}
		}
	}
	
	public List<String> getOrderIdsInStoreMealTakeups(List<StoreMealTakeup> storeMealTakeups){
		Set<String> orderSet = new HashSet<String>();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
			orderSet.add(storeMealTakeup.getOrderId());
		}
		return new ArrayList<String>(orderSet);
	}
	
	public List<String> getOrderIdsInStoreMealCheckouts(List<StoreMealCheckout> storeMealCheckouts){
		Set<String> orderSet = new HashSet<String>();
		for (StoreMealCheckout storeMealCheckout : storeMealCheckouts) {
			orderSet.add(storeMealCheckout.getOrderId());
		}
		return new ArrayList<String>(orderSet);
	}
	
	private StoreMealChargeDTO getStoreMealChargeDTOByChargeKey(StoreMealChargeDTO storeMealCharge, StoreMealDTO storeMealDTO){
		List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
		for (StoreMealChargeDTO storeMealChargeDTO : storeMealChargeDTOs) {
			if(storeMealChargeDTO.getChargeItemId()==storeMealCharge.getChargeItemId()){
				return storeMealChargeDTO;
			}
		}
		return null;
	}
	
	public List<StoreMealDTO> getStoreMealDTOByStoreMealTakeups(List<StoreMealTakeup> storeMealTakeups){
		if(storeMealTakeups==null||storeMealTakeups.isEmpty()){
			return new ArrayList<StoreMealDTO>();
		}
		Map<String,StoreMealDTO> storeMealMap = new HashMap<String,StoreMealDTO>();
		Map<String,StoreMealChargeDTO> storeChargeMap = new HashMap<String,StoreMealChargeDTO>();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
			String orderId = storeMealTakeup.getOrderId();
			int takeSerialNumber = storeMealTakeup.getTakeSerialNumber();
			long chargeItemId = storeMealTakeup.getChargeItemId();
			boolean packaged = storeMealTakeup.isPackaged();
			String mealKey = orderId + "_" + takeSerialNumber + "_" + packaged;
			String chargeKey = orderId + "_" + takeSerialNumber + "_" + packaged + "_" + chargeItemId;
			if(storeMealMap.containsKey(mealKey)){
				StoreMealDTO storeMealDTO = storeMealMap.get(mealKey);
				if(storeChargeMap.containsKey(chargeKey)){
					StoreMealChargeDTO storeMealChargeDTO = this.getStoreMealChargeDTOByChargeKey(storeChargeMap.get(chargeKey), storeMealDTO);
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
					List<StoreMealProductDTO> storeMealProductDTOs = storeMealChargeDTO.getStoreMealProductDTOs();
					storeMealProductDTOs.add(storeMealProductDTO);
				}else{
					StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealTakeup);
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
					
					List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
					storeMealProductDTOs.add(storeMealProductDTO);
					storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
					
					List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
					storeMealChargeDTOs.add(storeMealChargeDTO);
					
					storeChargeMap.put(chargeKey, storeMealChargeDTO);
				}
			}else{
				StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
				StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealTakeup);
				
				List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
				storeMealProductDTOs.add(storeMealProductDTO);
				
				List<StoreMealChargeDTO> storeMealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
				storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
				storeMealChargeDTOs.add(storeMealChargeDTO);
				
				StoreMealDTO storeMealDTO = this.newStoreMealDTO(storeMealTakeup);
				storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
				
				storeChargeMap.put(chargeKey, storeMealChargeDTO);
				storeMealMap.put(mealKey, storeMealDTO);
			}
		}
		return new ArrayList<StoreMealDTO>(storeMealMap.values());
	}
	
	public List<StoreMealDTO> getStoreMealDTOByStoreMealCheckouts(List<StoreMealCheckout> storeMealCheckouts){
		if(storeMealCheckouts==null||storeMealCheckouts.isEmpty()){
			return new ArrayList<StoreMealDTO>();
		}
		Map<String,StoreMealDTO> storeMealMap = new HashMap<String,StoreMealDTO>();
		Map<String,StoreMealChargeDTO> storeChargeMap = new HashMap<String,StoreMealChargeDTO>();
		for (StoreMealCheckout storeMealCheckout : storeMealCheckouts) {
			String orderId = storeMealCheckout.getOrderId();
			int takeSerialNumber = storeMealCheckout.getTakeSerialNumber();
			int takeSerialSeq = storeMealCheckout.getTakeSerialSeq();
			long chargeItemId = storeMealCheckout.getChargeItemId();
			boolean packaged = storeMealCheckout.isPackaged();
			String mealKey = orderId + "_" + takeSerialNumber  + "_" + takeSerialSeq + "_" + packaged;
			String chargeKey = orderId + "_" + takeSerialNumber  + "_" + takeSerialSeq + "_" + packaged + "_" + chargeItemId;
			if(storeMealMap.containsKey(mealKey)){
				StoreMealDTO storeMealDTO = storeMealMap.get(mealKey);
				
				if(storeChargeMap.containsKey(chargeKey)){
					StoreMealChargeDTO storeMealChargeDTO = this.getStoreMealChargeDTOByChargeKey(storeChargeMap.get(chargeKey), storeMealDTO);
					
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealCheckout);
					List<StoreMealProductDTO> storeMealProductDTOs = storeMealChargeDTO.getStoreMealProductDTOs();
					storeMealProductDTOs.add(storeMealProductDTO);
				}else{
					StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealCheckout);
					
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealCheckout);
					List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
					storeMealProductDTOs.add(storeMealProductDTO);
					
					List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
					storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
					storeMealChargeDTOs.add(storeMealChargeDTO);
					
					storeChargeMap.put(chargeKey, storeMealChargeDTO);
				}
			}else{
				StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealCheckout);
				StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealCheckout);
				
				List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
				storeMealProductDTOs.add(storeMealProductDTO);
				
				List<StoreMealChargeDTO> storeMealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
				storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
				storeMealChargeDTOs.add(storeMealChargeDTO);
				
				StoreMealDTO storeMealDTO = this.newStoreMealDTO(storeMealCheckout);
				storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
				
				storeChargeMap.put(chargeKey, storeMealChargeDTO);
				storeMealMap.put(mealKey, storeMealDTO);
			}
		}
		return new ArrayList<StoreMealDTO>(storeMealMap.values());
	}
	
	private StoreMealDTO newStoreMealDTO(StoreMealTakeup storeMealTakeup){
		String orderId = storeMealTakeup.getOrderId();
		long portId = storeMealTakeup.getPortId();
		int merchantId = storeMealTakeup.getMerchantId(); 
		long storeId = storeMealTakeup.getStoreId();
		long repastDate = storeMealTakeup.getRepastDate();
		long timeBucketId = storeMealTakeup.getTimeBucketId();
		int takeSerialNumber = storeMealTakeup.getTakeSerialNumber();
		int takeMode = storeMealTakeup.getTakeMode();
		boolean packaged = storeMealTakeup.isPackaged();
		long createTime = storeMealTakeup.getCreateTime();
		int siteNumber = storeMealTakeup.getSiteNumber();
		long updateTime = storeMealTakeup.getUpdateTime();
		long takeupTime = storeMealTakeup.getTakeupTime();
		StoreMealDTO storeMealDTO = new StoreMealDTO();
		storeMealDTO.setOrderId(orderId);
		storeMealDTO.setPortId(portId);
		storeMealDTO.setMerchantId(merchantId);
		storeMealDTO.setStoreId(storeId);
		storeMealDTO.setRepastDate(repastDate);
		storeMealDTO.setTimeBucketId(timeBucketId);
		storeMealDTO.setTakeSerialNumber(takeSerialNumber);
		storeMealDTO.setSiteNumber(siteNumber);
		storeMealDTO.setTakeMode(takeMode);
		storeMealDTO.setPackaged(packaged);
		storeMealDTO.setCreateTime(createTime);
		storeMealDTO.setUpdateTime(updateTime);
		storeMealDTO.setTakeupTime(takeupTime);
		return storeMealDTO;
	}
	
	private StoreMealChargeDTO newStoreMealChargeDTO(StoreMealTakeup storeMealTakeup){
		String orderId = storeMealTakeup.getOrderId();
		int merchantId = storeMealTakeup.getMerchantId();
		long storeId = storeMealTakeup.getStoreId();
		long chargeItemId = storeMealTakeup.getChargeItemId();
		String chargeItemName = storeMealTakeup.getChargeItemName();
		double remainTakeup = storeMealTakeup.getRemainTakeup();
		boolean packaged = storeMealTakeup.isPackaged();
		int spicyLevel = storeMealTakeup.getSpicyLevel();
		StoreMealChargeDTO storeMealChargeDTO = new StoreMealChargeDTO();
		storeMealChargeDTO.setOrderId(orderId);
		storeMealChargeDTO.setMerchantId(merchantId);
		storeMealChargeDTO.setStoreId(storeId);
		storeMealChargeDTO.setChargeItemId(chargeItemId);
		storeMealChargeDTO.setChargeItemName(chargeItemName);
		storeMealChargeDTO.setAmount(remainTakeup);
		storeMealChargeDTO.setPackaged(packaged);
		storeMealChargeDTO.setSpicyLevel(spicyLevel);
		return storeMealChargeDTO;
	}
	
	private StoreMealProductDTO newStoreMealProductDTO(StoreMealTakeup storeMealTakeup){
		String orderId = storeMealTakeup.getOrderId();
		int merchantId = storeMealTakeup.getMerchantId();
		long storeId = storeMealTakeup.getStoreId();
		long chargeItemId = storeMealTakeup.getChargeItemId();
		long productId = storeMealTakeup.getProductId();
		String productName = storeMealTakeup.getProductName();
		double amount = storeMealTakeup.getAmount();
		String unit = storeMealTakeup.getUnit();
		String remark = storeMealTakeup.getRemark();
		StoreMealProductDTO storeMealProductDTO = new StoreMealProductDTO();
		storeMealProductDTO.setOrderId(orderId);
		storeMealProductDTO.setMerchantId(merchantId);
		storeMealProductDTO.setStoreId(storeId);
		storeMealProductDTO.setChargeItemId(chargeItemId);
		storeMealProductDTO.setProductId(productId);
		storeMealProductDTO.setProductName(productName);
		storeMealProductDTO.setAmount(amount);
		storeMealProductDTO.setUnit(unit);
		storeMealProductDTO.setRemark(remark);
		return storeMealProductDTO;
	}
	
	private StoreMealDTO newStoreMealDTO(StoreMealCheckout storeMealCheckout){
		String orderId = storeMealCheckout.getOrderId();
		long portId = storeMealCheckout.getPortId();
		int merchantId = storeMealCheckout.getMerchantId(); 
		long storeId = storeMealCheckout.getStoreId();
		long repastDate = storeMealCheckout.getRepastDate();
		long timeBucketId = storeMealCheckout.getTimeBucketId();
		int takeSerialNumber = storeMealCheckout.getTakeSerialNumber();
		int takeSerialSeq = storeMealCheckout.getTakeSerialSeq();
		int siteNumber = storeMealCheckout.getSiteNumber();
		int takeMode = storeMealCheckout.getTakeMode();
		boolean packaged = storeMealCheckout.isPackaged();
		long appcopyId = storeMealCheckout.getAppcopyId();
		int checkoutType = storeMealCheckout.getCheckoutType();
		long createTime = storeMealCheckout.getCreateTime();
		boolean refundMeal = storeMealCheckout.isRefundMeal();
		long updateTime = storeMealCheckout.getUpdateTime();
		long takeupTime = storeMealCheckout.getTakeupTime();
		StoreMealDTO storeMealDTO = new StoreMealDTO();
		storeMealDTO.setOrderId(orderId);
		storeMealDTO.setPortId(portId);
		storeMealDTO.setMerchantId(merchantId);
		storeMealDTO.setStoreId(storeId);
		storeMealDTO.setRepastDate(repastDate);
		storeMealDTO.setTimeBucketId(timeBucketId);
		storeMealDTO.setTakeSerialNumber(takeSerialNumber);
		storeMealDTO.setTakeSerialSeq(takeSerialSeq);
		storeMealDTO.setSiteNumber(siteNumber);
		storeMealDTO.setTakeMode(takeMode);
		storeMealDTO.setPackaged(packaged);
		storeMealDTO.setAppcopyId(appcopyId);
		storeMealDTO.setCheckoutType(checkoutType);
		storeMealDTO.setCreateTime(createTime);
		storeMealDTO.setRefundMeal(refundMeal);
		storeMealDTO.setUpdateTime(updateTime);
		storeMealDTO.setTakeupTime(takeupTime);
		return storeMealDTO;
	}
	
	private StoreMealChargeDTO newStoreMealChargeDTO(StoreMealCheckout storeMealCheckout){
		String orderId = storeMealCheckout.getOrderId();
		int merchantId = storeMealCheckout.getMerchantId();
		long storeId = storeMealCheckout.getStoreId();
		long chargeItemId = storeMealCheckout.getChargeItemId();
		String chargeItemName = storeMealCheckout.getChargeItemName();
		double amountCheckout = storeMealCheckout.getAmountCheckout();
		boolean packaged = storeMealCheckout.isPackaged();
		int spicyLevel = storeMealCheckout.getSpicyLevel();
		StoreMealChargeDTO storeMealChargeDTO = new StoreMealChargeDTO();
		storeMealChargeDTO.setOrderId(orderId);
		storeMealChargeDTO.setMerchantId(merchantId);
		storeMealChargeDTO.setStoreId(storeId);
		storeMealChargeDTO.setChargeItemId(chargeItemId);
		storeMealChargeDTO.setChargeItemName(chargeItemName);
		storeMealChargeDTO.setAmount(amountCheckout);
		storeMealChargeDTO.setPackaged(packaged);
		storeMealChargeDTO.setSpicyLevel(spicyLevel);
		return storeMealChargeDTO;
	}
	
	private StoreMealProductDTO newStoreMealProductDTO(StoreMealCheckout storeMealCheckout){
		String orderId = storeMealCheckout.getOrderId();
		int merchantId = storeMealCheckout.getMerchantId();
		long storeId = storeMealCheckout.getStoreId();
		long chargeItemId = storeMealCheckout.getChargeItemId();
		long productId = storeMealCheckout.getProductId();
		String productName = storeMealCheckout.getProductName();
		double amount = storeMealCheckout.getAmount();
		String unit = storeMealCheckout.getUnit();
		String remark = storeMealCheckout.getRemark();
		StoreMealProductDTO storeMealProductDTO = new StoreMealProductDTO();
		storeMealProductDTO.setOrderId(orderId);
		storeMealProductDTO.setMerchantId(merchantId);
		storeMealProductDTO.setStoreId(storeId);
		storeMealProductDTO.setChargeItemId(chargeItemId);
		storeMealProductDTO.setProductId(productId);
		storeMealProductDTO.setProductName(productName);
		storeMealProductDTO.setAmount(amount);
		storeMealProductDTO.setUnit(unit);
		storeMealProductDTO.setRemark(remark);
		return storeMealProductDTO;
	}
	
	public List<StoreProductDTO> getStoreProductDTOs(List<StoreProduct> products){
		List<StoreProductDTO> productDTOs = new ArrayList<StoreProductDTO>();
		for (StoreProduct storeProduct : products) {
			StoreProductDTO storeProductDTO = BeanUtil.copy(storeProduct, StoreProductDTO.class);
			productDTOs.add(storeProductDTO);
		}
		return productDTOs;
	}
	
	public List<QueryProductPortParam> storeOrderToQueryProductPortParams(StoreOrder storeOrder){
		List<QueryProductPortParam> list = new ArrayList<QueryProductPortParam>();
		List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			long chargeItemId = storeOrderItem.getChargeItemId();
			List<StoreOrderSubitem> storeOrderSubitems = storeOrderItem.getStoreOrderSubitems();
			List<Long> productIds = new ArrayList<Long>();
            for (StoreOrderSubitem storeOrderSubitem : storeOrderSubitems) {
            	long productId = storeOrderSubitem.getProductId();
            	productIds.add(productId);
            }
            QueryProductPortParam param = new QueryProductPortParam();
            param.setChargeItemId(chargeItemId);
            param.setProductIds(productIds);
            list.add(param);
		}
		return list;
	}
	
}
