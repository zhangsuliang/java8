package com.huofu.module.i5wei.meal.service;

import huofucore.facade.i5wei.meal.StoreMealAutoPrintParam;
import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.meal.StoreMealProductDTO;
import huofucore.facade.i5wei.meal.StoreMealsAutoCheckoutParam;
import huofucore.facade.i5wei.mealport.StorePortPrinterStatusEnum;
import huofucore.facade.i5wei.menu.ProductDivRuleEnum;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.service.QueryProductPortParam;
import com.huofu.module.i5wei.order.dao.StoreOrderSubitemDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderSubitem;
import com.huofu.module.i5wei.wechat.WechatNotifyService;

@Component
public class StoreMealMultiHelper {
    
	@Autowired
    private StoreMealPortService storeMealPortService;
	
	@Autowired
    private WechatNotifyService wechatNotifyService;
	
	@Autowired
	private StoreChargeItemDAO storeChargeItemDAO;
	
	@Autowired
	private StoreOrderSubitemDAO storeOrderSubitemDAO;
	
	public void sortStoreMealList(List<StoreMealDTO> storeMealList) {
		ComparatorStoreMeal comparator = new ComparatorStoreMeal();
		Collections.sort(storeMealList, comparator);
	}
	
	public void sortStoreMealListDesc(List<StoreMealDTO> storeMealList) {
		ComparatorStoreMealDesc comparator = new ComparatorStoreMealDesc();
		Collections.sort(storeMealList, comparator);
	}
	
	public void sortStoreMealListTimeDesc(List<StoreMealDTO> storeMealList) {
		ComparatorStoreMealTimeDesc comparator = new ComparatorStoreMealTimeDesc();
		Collections.sort(storeMealList, comparator);
	}
	
	public class ComparatorStoreMeal implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreMealDTO meal0 = (StoreMealDTO) arg0;
			StoreMealDTO meal1 = (StoreMealDTO) arg1;
			int flag = Integer.valueOf(meal0.getTakeSerialNumber()).compareTo(meal1.getTakeSerialNumber());
	        if(flag != 0)
	            return flag;
	        flag = Integer.valueOf(meal0.getTakeSerialSeq()).compareTo(meal1.getTakeSerialSeq());
	        return flag;
		}
	}
	
	public class ComparatorStoreMealDesc implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreMealDTO meal0 = (StoreMealDTO) arg0;
			StoreMealDTO meal1 = (StoreMealDTO) arg1;
			int flag = Integer.valueOf(meal1.getTakeSerialNumber()).compareTo(meal0.getTakeSerialNumber());
	        if(flag != 0)
	            return flag;
	        flag = Integer.valueOf(meal1.getTakeSerialSeq()).compareTo(meal0.getTakeSerialSeq());
	        return flag;
		}
	}
	
	public class ComparatorStoreMealTimeDesc implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreMealDTO meal0 = (StoreMealDTO) arg0;
			StoreMealDTO meal1 = (StoreMealDTO) arg1;
			int flag = Long.valueOf(meal1.getCreateTime()).compareTo(meal0.getCreateTime());
	        return flag;
		}
	}
	
	public List<String> getOrderIdsInStoreMealTakeups(List<StoreMealTakeup> storeMealTakeups){
		Set<String> orderSet = new HashSet<String>();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
			orderSet.add(storeMealTakeup.getOrderId());
		}
		return new ArrayList<String>(orderSet);
	}
	
	
	public StoreMealChargeDTO getStoreMealChargeDTOByChargeKey(StoreMealChargeDTO storeMealCharge, List<StoreMealChargeDTO> storeMealChargeDTOs){
		for (StoreMealChargeDTO storeMealChargeDTO : storeMealChargeDTOs) {
			if(storeMealChargeDTO.getChargeItemId()==storeMealCharge.getChargeItemId() && storeMealChargeDTO.isPackaged() == storeMealCharge.isPackaged()){
				return storeMealChargeDTO;
			}
		}
		return storeMealCharge;
	}
	
	/**
     * 计算分单规则
     *      特殊规则:称重的收费项,如果按数量分单则需要改为按种类分单
     * @param storeMealTakeups
     * @param products
     * @param isAuto
     * @return
     */
    public void calculateDivRule(List<StoreMealTakeup> storeMealTakeups, List<StoreChargeItem> storeChargeItems, List<StoreProduct> products, boolean isAuto){
        Map<Long, StoreChargeItem> chargeItemMap = new HashMap<Long, StoreChargeItem>();
        if(storeChargeItems != null){
            for (StoreChargeItem storeChargeItem : storeChargeItems){
                chargeItemMap.put(storeChargeItem.getChargeItemId(), storeChargeItem);
            }
        }
        Map<Long, StoreProduct> productMap = new HashMap<Long, StoreProduct>();
        if (products != null) {
            for (StoreProduct product : products) {
                productMap.put(product.getProductId(), product);
            }
        }
        Map<Long,Integer> chargeItemDivRules = Maps.newHashMap();
        Map<Long,Integer> productDivRules = Maps.newHashMap();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            long chargeItemId = storeMealTakeup.getChargeItemId();
            long productId = storeMealTakeup.getProductId();
            boolean packaged = storeMealTakeup.isPackaged();
            boolean weightEnabled = storeMealTakeup.isWeightEnabled();
            boolean enableDivRule = false;
            if (isAuto) {
                //自动出餐打包不分单，只有非打包部分按基本规则分单
                if (!packaged && productMap.keySet().contains(productId)) {
                    enableDivRule = true;
                }
            } else {
                //按基本规则分单
                enableDivRule = true;
            }
            if (enableDivRule) {
                StoreChargeItem chargeItem = chargeItemMap.get(chargeItemId);
                boolean chargeItemEnable = true;
                if (chargeItemMap.containsKey(chargeItemId)) {
                    chargeItemEnable = chargeItem.isOpenDivRule();
                    storeMealTakeup.setChargeItemEnable(chargeItemEnable);
                    // 计算收费项目的分单规则
                    int chargeItemDivRule = ProductDivRuleEnum.NOT.getValue();
                    if (chargeItemEnable) {
                        chargeItemDivRule = chargeItem.getDivRule();
                        if(chargeItemDivRule == ProductDivRuleEnum.NUM.getValue() && weightEnabled){
                            chargeItemDivRule = ProductDivRuleEnum.ITEM.getValue();
                        }
                    }
                    if (chargeItemDivRule > ProductDivRuleEnum.NOT.getValue()) {
                        storeMealTakeup.setDivRule(chargeItemDivRule);
                        storeMealTakeup.setChargeItemEnable(true);
                        chargeItemDivRules.put(chargeItemId, chargeItemDivRule);
                        continue;
                    }
                }
                // 产品的分单规则
                int productDivRule = ProductDivRuleEnum.NOT.getValue();
                if (productMap.containsKey(productId)) {
                    productDivRule = productMap.get(productId).getDivRule();
                    if(productDivRule == ProductDivRuleEnum.NUM.getValue() && weightEnabled){
                        productDivRule = ProductDivRuleEnum.ITEM.getValue();
                    }
                }
                if (chargeItemEnable) {
                    // 按收费项目分单，计算产品的分单规则优先级
                    if (productDivRule > ProductDivRuleEnum.NOT.getValue()) {
                        storeMealTakeup.setDivRule(productDivRule);
                        storeMealTakeup.setChargeItemEnable(true);
                        // 计算产品分单规则优先级
                        int _productDivRule = productDivRules.getOrDefault(chargeItemId, ProductDivRuleEnum.NOT.getValue());
                        if (productDivRule < _productDivRule) {
                            productDivRule = _productDivRule;
                        }
                        productDivRules.put(chargeItemId, productDivRule);
                    }
                } else {
                    // 按产品的分单规则，不区分优先级
                    storeMealTakeup.setProductDivRule(true);
                    storeMealTakeup.setDivRule(productDivRule);
                    // 但是收费项目和产品名字不一样，则出餐单显示产品信息
                    if (!storeMealTakeup.isSameName()) {
    					storeMealTakeup.setShowProducts(true);
    				}
                }
            }
        }
        if (!productDivRules.isEmpty()) {
            for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
                // 不干扰已经按收费项目分单规则分单的部分
                if (chargeItemDivRules.containsKey(storeMealTakeup.getChargeItemId())) {
                    continue;
                }
                // 按收费项目分单，如果是计算产品的分单规则优先级，则让按收费项目分单的规则保持一致
                int divRule = productDivRules.getOrDefault(storeMealTakeup.getChargeItemId(), ProductDivRuleEnum.NOT.getValue());
                if (divRule > ProductDivRuleEnum.NOT.getValue()) {
                    storeMealTakeup.setDivRule(divRule);
                }
            }
        }
    }
	
	public List<Long> getStoreProductIds(List<StoreProduct> products){
		List<Long> list = new ArrayList<Long>();
		if (products == null || products.isEmpty()) {
			return list;
		}
		for (StoreProduct product : products) {
			list.add(product.getProductId());
		}
		return list;
	}
	
	/**
	 * 基本规则 
	 * 	1、按不同取餐模式的产品，分不同的单； 2、按不同出餐口的产品，分不同的单； 3、按照需要统计的产品分单 
	 * 自动出餐特殊规则
	 * 	1、对于打包的部分（外送或者自取的），只按照出餐口进行分单，不按照需要统计的产品进行分单，而且打印也是按照“打包清单”的规则打印，不是按照出餐单的模板规则打印；
	 * @author chenkai
	 * @since 2015-10-14
	 * @param storeMealTakeups
	 * @param products
	 * @param portMap
	 * @return List<StoreMealDTO>
	 */
	public List<StoreMealDTO> getStoreMealDTOByStoreMealTakeups(List<StoreMealTakeup> storeMealTakeups, List<StoreProduct> products, Map<Long, StoreMealPort> portMap, boolean isAuto){
		if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
			return new ArrayList<StoreMealDTO>();
		}
		int merchantId = storeMealTakeups.get(0).getMerchantId(); 
		long storeId = storeMealTakeups.get(0).getStoreId(); 
		//非打包，需要按产品统计做分单处理的收费项目ID
		List<StoreChargeItem> storeChargeItems = this.getStoreChargeItemByMeals(storeMealTakeups);
		this.calculateDivRule(storeMealTakeups, storeChargeItems, products, isAuto);
		//出餐分单
		int portCount = storeMealPortService.countStoreMealPorts(merchantId, storeId);
		Map<String,StoreMealDTO> storeMealMap = new HashMap<String,StoreMealDTO>();
		Map<String,StoreMealChargeDTO> storeChargeMap = new HashMap<String,StoreMealChargeDTO>();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
			long tid = storeMealTakeup.getTid();
			String orderId = storeMealTakeup.getOrderId();
			long portId = storeMealTakeup.getPortId();
			int takeSerialNumber = storeMealTakeup.getTakeSerialNumber();
			long chargeItemId = storeMealTakeup.getChargeItemId();
			long productId = storeMealTakeup.getProductId();
			boolean packaged = storeMealTakeup.isPackaged();
			StoreMealPort storeMealPort = portMap.get(portId);
			String portLetter = "";
			if (storeMealPort != null) {
				portLetter = storeMealPort.getLetter();
			}
			//分单规则
			String mealKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + packaged ;
			String chargeKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + chargeItemId + "_" + packaged;
			String productKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + chargeItemId + "_" + productId + "_" + packaged;
			//分单主键(默认按订单)
			boolean numDiv = storeMealTakeup.isNumDiv();
			String mkey = mealKey; //StoreMealDTO
			String ckey = chargeKey; //StoreMealChargeDTO
			// 分单显示项目
            if (isAuto && packaged) {
                // 自动出餐特殊规则：对于打包的部分（外送或者自取的），只按照出餐口进行分单，不按照需要统计的产品进行分单
            } else {
                if (storeMealTakeup.isProductDivRule()) {
                    if (storeMealTakeup.getDivRule() > ProductDivRuleEnum.NOT.getValue()) {
                        mkey = productKey;
                        ckey = productKey;
                    }
                } else {
                    if (storeMealTakeup.getDivRule() > ProductDivRuleEnum.NOT.getValue()) {
                        mkey = chargeKey;
                        ckey = chargeKey;
                    }
                }
            }
			if (storeMealMap.containsKey(mkey)) {
				StoreMealDTO storeMealDTO = storeMealMap.get(mkey);
				if(storeChargeMap.containsKey(ckey)){
					StoreMealChargeDTO storeMealChargeDTO = this.getStoreMealChargeDTOByChargeKey(storeChargeMap.get(ckey), storeMealDTO.getStoreMealChargeDTOs());
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
					List<StoreMealProductDTO> storeMealProductDTOs = storeMealChargeDTO.getStoreMealProductDTOs();
					storeMealProductDTOs.add(storeMealProductDTO);
				}else{
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
					StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealTakeup);
					
					List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
					storeMealProductDTOs.add(storeMealProductDTO);
					storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
					
					List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
					storeMealChargeDTOs.add(storeMealChargeDTO);
					
					storeChargeMap.put(ckey, storeMealChargeDTO);
				}
				List<Long> takeupTids = storeMealDTO.getTakeupTids();
				takeupTids.add(tid);
			} else {
				StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
				StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealTakeup);
				
				List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
				storeMealProductDTOs.add(storeMealProductDTO);
				
				List<StoreMealChargeDTO> storeMealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
				storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
				storeMealChargeDTOs.add(storeMealChargeDTO);
				
				StoreMealDTO storeMealDTO = this.newStoreMealDTO(storeMealTakeup);
				storeMealDTO.setPortLetter(portLetter);
				storeMealDTO.setPortCount(portCount);
				storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
				storeMealDTO.setNumDiv(numDiv);
				List<Long> takeupTids = new ArrayList<Long>();
				takeupTids.add(tid);
				storeMealDTO.setTakeupTids(takeupTids);
				
				storeMealMap.put(mkey, storeMealDTO);
				storeChargeMap.put(ckey, storeMealChargeDTO);
			}
		}
		List<StoreMealDTO> storeMealDTOs = new ArrayList<StoreMealDTO>(storeMealMap.values());
		List<StoreMealDTO> divStoreMealDTOs = new ArrayList<StoreMealDTO>();
		// 分单的收费项目按个数拆分
        for (StoreMealDTO storeMealDTO : storeMealDTOs) {
            boolean packaged = storeMealDTO.isPackaged();
            List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
            if (storeMealChargeDTOs.size() != 1) {
                continue;// 不是按菜品分单的跳过
            }
            StoreMealChargeDTO storeMealChargeDTO = storeMealChargeDTOs.get(0);
            double amount = storeMealChargeDTO.getAmount();
            // 分单显示项目
            if (isAuto && packaged) {
                // 自动出餐特殊规则：对于打包的部分（外送或者自取的），只按照出餐口进行分单，不按照需要统计的产品进行分单
            } else {
                if (storeMealDTO.isNumDiv()) {
                    if (amount > 1) {
                        // 分单的收费项目按个数拆分
                        storeMealChargeDTO.setAmount(1);
                        storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
                        for (int i = 1; i < amount; i++) {
                            StoreMealDTO divStoreMealDTO = storeMealDTO.deepCopy();
                            divStoreMealDTOs.add(divStoreMealDTO);
                        }
                    }
                }
			}
		}
		storeMealDTOs.addAll(divStoreMealDTOs);
		this.setStoreMealChargeDTO(merchantId, storeId, storeMealDTOs);
		return storeMealDTOs;
	}

    /**
     * 给后厨清单临时提供按订单分单,上班之后考虑和getStoreMealDTOByStoreMealTakeups合并到一块
     * @param storeMealTakeups
     * @param products
     * @param portMap
     * @param isAuto
     * @return
     */
	public List<StoreMealDTO> getStoreMealDTOByKitchenTakeups(List<StoreMealTakeup> storeMealTakeups, List<StoreProduct> products, Map<Long, StoreMealPort> portMap, boolean isAuto){
        if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
			return new ArrayList<StoreMealDTO>();
		}
		int merchantId = storeMealTakeups.get(0).getMerchantId();
		long storeId = storeMealTakeups.get(0).getStoreId();
		List<StoreChargeItem> storeChargeItems = this.getStoreChargeItemByMeals(storeMealTakeups);
		this.calculateDivRule(storeMealTakeups, storeChargeItems, products, isAuto);
		//出餐分单
		int portCount = storeMealPortService.countStoreMealPorts(merchantId, storeId);
		Map<String,StoreMealDTO> storeMealMap = new HashMap<String,StoreMealDTO>();
		Map<String,StoreMealChargeDTO> storeChargeMap = new HashMap<String,StoreMealChargeDTO>();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
			long tid = storeMealTakeup.getTid();
			String orderId = storeMealTakeup.getOrderId();
			long portId = storeMealTakeup.getPortId();
			int takeSerialNumber = storeMealTakeup.getTakeSerialNumber();
			long chargeItemId = storeMealTakeup.getChargeItemId();
			long productId = storeMealTakeup.getProductId();
			boolean packaged = storeMealTakeup.isPackaged();
			StoreMealPort storeMealPort = portMap.get(portId);
			String portLetter = "";
			if (storeMealPort != null) {
				portLetter = storeMealPort.getLetter();
			}
			//分单规则
			String mealKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + packaged ;
			String chargeKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + chargeItemId + "_" + packaged;

			if (storeMealMap.containsKey(mealKey)) {
				StoreMealDTO storeMealDTO = storeMealMap.get(mealKey);
				if(storeChargeMap.containsKey(chargeKey)){
					StoreMealChargeDTO storeMealChargeDTO = this.getStoreMealChargeDTOByChargeKey(storeChargeMap.get(chargeKey), storeMealDTO.getStoreMealChargeDTOs());
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
					List<StoreMealProductDTO> storeMealProductDTOs = storeMealChargeDTO.getStoreMealProductDTOs();
					storeMealProductDTOs.add(storeMealProductDTO);
				}else{
					StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
					StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealTakeup);

					List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
					storeMealProductDTOs.add(storeMealProductDTO);
					storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);

					List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
					storeMealChargeDTOs.add(storeMealChargeDTO);

					storeChargeMap.put(chargeKey, storeMealChargeDTO);
				}
				List<Long> takeupTids = storeMealDTO.getTakeupTids();
				takeupTids.add(tid);
			} else {
				StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealTakeup);
				StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealTakeup);

				List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
				storeMealProductDTOs.add(storeMealProductDTO);

				List<StoreMealChargeDTO> storeMealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
				storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
				storeMealChargeDTOs.add(storeMealChargeDTO);

				StoreMealDTO storeMealDTO = this.newStoreMealDTO(storeMealTakeup);
				storeMealDTO.setPortLetter(portLetter);
				storeMealDTO.setPortCount(portCount);
				storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
				List<Long> takeupTids = new ArrayList<Long>();
				takeupTids.add(tid);
				storeMealDTO.setTakeupTids(takeupTids);

				storeMealMap.put(mealKey, storeMealDTO);
				storeChargeMap.put(chargeKey, storeMealChargeDTO);
			}
		}
	    return new ArrayList<StoreMealDTO>(storeMealMap.values());
    }

	/**
	 * 基本规则：1、按不同取餐模式的产品，分不同的单； 2、分不同出餐口
	 * @author chenkai
	 * @param storeMealCheckouts
	 * @return List<StoreMealDTO>
	 */
	public List<StoreMealDTO> getStoreMealDTOByStoreMealCheckouts(List<StoreMealCheckout> storeMealCheckouts, Map<Long, StoreMealPort> portMap){
		if(storeMealCheckouts==null||storeMealCheckouts.isEmpty()){
			return new ArrayList<StoreMealDTO>();
		}
		int merchantId = storeMealCheckouts.get(0).getMerchantId(); 
		long storeId = storeMealCheckouts.get(0).getStoreId(); 
		int portCount = storeMealPortService.countStoreMealPorts(merchantId, storeId);
		Map<String,StoreMealDTO> storeMealMap = new HashMap<String,StoreMealDTO>();
		Map<String,StoreMealChargeDTO> storeChargeMap = new HashMap<String,StoreMealChargeDTO>();
		for (StoreMealCheckout storeMealCheckout : storeMealCheckouts) {
			String orderId = storeMealCheckout.getOrderId();
			long portId = storeMealCheckout.getPortId();
			int takeSerialNumber = storeMealCheckout.getTakeSerialNumber();
			int takeSerialSeq = storeMealCheckout.getTakeSerialSeq();
			long chargeItemId = storeMealCheckout.getChargeItemId();
			boolean packaged = storeMealCheckout.isPackaged();
			boolean refundMeal = storeMealCheckout.isRefundMeal();
			StoreMealPort storeMealPort = portMap.get(portId);
			String portLetter = "";
			if (storeMealPort != null) {
				portLetter = storeMealPort.getLetter();
			}
			String mealKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + takeSerialSeq + "_" + packaged + "_" + refundMeal;
			String chargeKey = orderId + "_" + portId + "_" + takeSerialNumber  + "_" + takeSerialSeq + "_" + chargeItemId + "_" + packaged + "_" + refundMeal;
			
			if(storeMealMap.containsKey(mealKey)){
				StoreMealDTO storeMealDTO = storeMealMap.get(mealKey);
				if(storeChargeMap.containsKey(chargeKey)){
					StoreMealChargeDTO storeMealChargeDTO = this.getStoreMealChargeDTOByChargeKey(storeChargeMap.get(chargeKey), storeMealDTO.getStoreMealChargeDTOs());
					
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
				storeMealDTO.setPortLetter(portLetter);
				storeMealDTO.setPortCount(portCount);
				storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
				storeMealMap.put(mealKey, storeMealDTO);
				storeChargeMap.put(chargeKey, storeMealChargeDTO);
			}
		}
		List<StoreMealDTO> storeMealDTOs = new ArrayList<StoreMealDTO>(storeMealMap.values());
		this.setStoreMealChargeDTO(merchantId, storeId, storeMealDTOs);
		return storeMealDTOs;
	}
	
	public StoreMealDTO newStoreMealDTO(StoreMealTakeup storeMealTakeup){
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
		long sendPortId = storeMealTakeup.getSendPortId();
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
		storeMealDTO.setSendPortId(sendPortId);
		return storeMealDTO;
	}
	
	public StoreMealChargeDTO newStoreMealChargeDTO(StoreMealTakeup storeMealTakeup){
		String orderId = storeMealTakeup.getOrderId();
		int merchantId = storeMealTakeup.getMerchantId();
		long storeId = storeMealTakeup.getStoreId();
		long chargeItemId = storeMealTakeup.getChargeItemId();
		String chargeItemName = storeMealTakeup.getChargeItemName();
		double remainTakeup = storeMealTakeup.getRemainTakeup();
		boolean packaged = storeMealTakeup.isPackaged();
		boolean showProducts = storeMealTakeup.isShowProducts();
		int spicyLevel = storeMealTakeup.getSpicyLevel();
		double remainSend = storeMealTakeup.getRemainSend();
		long sweepTime = storeMealTakeup.getSweepTime();
		StoreMealChargeDTO storeMealChargeDTO = new StoreMealChargeDTO();
		storeMealChargeDTO.setOrderId(orderId);
		storeMealChargeDTO.setMerchantId(merchantId);
		storeMealChargeDTO.setStoreId(storeId);
		storeMealChargeDTO.setChargeItemId(chargeItemId);
		storeMealChargeDTO.setChargeItemName(chargeItemName);
		storeMealChargeDTO.setAmount(remainTakeup);
		storeMealChargeDTO.setPackaged(packaged);
		storeMealChargeDTO.setShowProducts(showProducts);
		storeMealChargeDTO.setSpicyLevel(spicyLevel);
        storeMealChargeDTO.setSweepMealTime(sweepTime);
        storeMealChargeDTO.setRemainMealAmount(remainSend);
		return storeMealChargeDTO;
	}
	
	public StoreMealProductDTO newStoreMealProductDTO(StoreMealTakeup storeMealTakeup){
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
		long tableRecordId = storeMealCheckout.getTableRecordId();
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
		storeMealDTO.setTableRecordId(tableRecordId);
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
		boolean showProducts = storeMealCheckout.isShowProducts();
		int spicyLevel = storeMealCheckout.getSpicyLevel();
		long portId = storeMealCheckout.getPortId();
		StoreMealChargeDTO storeMealChargeDTO = new StoreMealChargeDTO();
		storeMealChargeDTO.setOrderId(orderId);
		storeMealChargeDTO.setMerchantId(merchantId);
		storeMealChargeDTO.setStoreId(storeId);
		storeMealChargeDTO.setChargeItemId(chargeItemId);
		storeMealChargeDTO.setChargeItemName(chargeItemName);
		storeMealChargeDTO.setAmount(amountCheckout);
		storeMealChargeDTO.setPackaged(packaged);
		storeMealChargeDTO.setShowProducts(showProducts);
		storeMealChargeDTO.setSpicyLevel(spicyLevel);
		storeMealChargeDTO.setPortId(portId);
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
	
	public List<QueryProductPortParam> storeOrderToQueryProductPortParams(List<StoreOrder> storeOrders){
		Map<Long, QueryProductPortParam> dataMap = new HashMap<Long, QueryProductPortParam>();
		for (StoreOrder storeOrder : storeOrders) {
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
	            dataMap.put(chargeItemId, param);
			}
		}
		return new ArrayList<QueryProductPortParam>(dataMap.values());
	}
	
	public Set<Long> getDivChargeItemIds(Map<String, Long> productPortMap) {
		Map<Long, Set<Long>> chargeItemPortMap = new HashMap<Long, Set<Long>>();
		for (String key : productPortMap.keySet()) {
			String[] array = key.split("_");
			long chargeItemId = Long.valueOf(array[0]);
			long portId = productPortMap.get(key);
			Set<Long> portIds = chargeItemPortMap.get(chargeItemId);
			if (portIds == null) {
				portIds = new HashSet<Long>();
			}
			portIds.add(portId);
			chargeItemPortMap.put(chargeItemId, portIds);
		}
		Set<Long> divChargeItems = new HashSet<Long>();
		for (Long chargeItemId : chargeItemPortMap.keySet()) {
			Set<Long> portIds = chargeItemPortMap.get(chargeItemId);
			if (portIds.size() > 1) {
				divChargeItems.add(chargeItemId);
			}
		} 
		return divChargeItems;
	}
	
	public void autoCheckoutStoreMealMonitor(StoreMealsAutoCheckoutParam storeMealsAutoCheckoutParam){
    	int merchantId = storeMealsAutoCheckoutParam.getMerchantId();
        long storeId = storeMealsAutoCheckoutParam.getStoreId();
        long appcopyId = storeMealsAutoCheckoutParam.getAppcopyId();
        List<StoreMealAutoPrintParam> storeMealAutoPrintParams = storeMealsAutoCheckoutParam.getStoreMealAutoPrintParams();
		if (storeMealAutoPrintParams == null || storeMealAutoPrintParams.isEmpty()) {
			return;
		}
		boolean monitor = false;
		StringBuffer content = new StringBuffer();
		content.append("merchantId="+merchantId).append(",");
		content.append("storeId="+storeId).append(",");
		content.append("appcopyId="+appcopyId).append("：");
		for (StoreMealAutoPrintParam param : storeMealAutoPrintParams) {
			if(param.getPrinterStatus() != StorePortPrinterStatusEnum.ON.getValue()){
				monitor = true;
				content.append("[出餐口").append(param.getPortId());
				if(param.getPrinterStatus()==StorePortPrinterStatusEnum.OFF.getValue()){
					content.append("无法连接] ");
				}else if(param.getPrinterStatus()==StorePortPrinterStatusEnum.CAN_NOT.getValue()){
					content.append("无法打印] ");
				}
			}
		}
		if (monitor) {
        	wechatNotifyService.monitorMsg(content.toString());
        }
    }
	
	public List<StoreChargeItem> getStoreChargeItemByMeals(List<StoreMealTakeup> storeMealTakeups) {
        List<StoreChargeItem> storeChargeItems = new ArrayList<StoreChargeItem>();
        if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
            return storeChargeItems;
        }
        int merchantId = storeMealTakeups.get(0).getMerchantId(); 
        long storeId = storeMealTakeups.get(0).getStoreId();
        Set<Long> storeChargeItemIds = new HashSet<Long>();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            storeChargeItemIds.add(storeMealTakeup.getChargeItemId());
        }
        return storeChargeItemDAO.getStoreChargeItems(merchantId, storeId, new ArrayList<Long>(storeChargeItemIds));
    }
	
	public List<Long> getMealTakeupTableRecordIds(List<StoreMealTakeup> storeMealTakeups) {
		Set<Long> tableRecordIds = new HashSet<Long>();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
			long tableRecordId = storeMealTakeup.getTableRecordId();
			if (tableRecordId > 0) {
				tableRecordIds.add(tableRecordId);
			}
		}
		return new ArrayList<Long>(tableRecordIds);
	}
	
	public List<Long> getMealCheckoutTableRecordIds(List<StoreMealCheckout> storeMealCheckouts){
		Set<Long> tableRecordIds = new HashSet<Long>();
		for (StoreMealCheckout storeMealCheckout : storeMealCheckouts) {
			long tableRecordId = storeMealCheckout.getTableRecordId();
			if (tableRecordId > 0) {
				tableRecordIds.add(tableRecordId);
			}
		}
		return new ArrayList<Long>(tableRecordIds);
	}
	
	public List<String> getOrderIdsInStoreMealCheckouts(List<StoreMealCheckout> storeMealCheckouts){
        Set<String> orderSet = new HashSet<String>();
        for (StoreMealCheckout storeMealCheckout : storeMealCheckouts) {
            orderSet.add(storeMealCheckout.getOrderId());
        }
        return new ArrayList<String>(orderSet);
    }
	
	public void setStoreMealChargeDTO(int merchantId, long storeId, List<StoreMealDTO> storeMealDTOs){
	    Set<String> orderIds = new HashSet<String>();
	    if(storeMealDTOs.isEmpty()){
	        return;
	    }
	    for (StoreMealDTO storeMealDTO : storeMealDTOs) {
	        orderIds.add(storeMealDTO.getOrderId());
        }
	    Map<String, Integer> storeOrderSubitem = this.storeOrderSubitemDAO.countStoreOrderSubitem(merchantId, storeId, new ArrayList<String>(orderIds), true);
	    for (StoreMealDTO storeMealDTO : storeMealDTOs) {
	        List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
	        if(storeMealChargeDTOs == null){
	            continue;
	        }
	        for (StoreMealChargeDTO charge : storeMealChargeDTOs) {
	            Integer subitemNum = storeOrderSubitem.get(charge.getOrderId() + "_" + charge.getChargeItemId());
	            if(subitemNum == null){
	                continue;
	            }
	            charge.setStoreOrderSubItemSize(subitemNum);
	            if(subitemNum == 1){
	                if(charge.getChargeItemName().equals(charge.getStoreMealProductDTOs().get(0).getProductName())){
	                    charge.setSameSubItemName(true);
	                }
	            }
            }
	    }
	}
}
