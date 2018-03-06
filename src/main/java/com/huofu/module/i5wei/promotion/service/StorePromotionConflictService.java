package com.huofu.module.i5wei.promotion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisChargeItemDAO;
import com.huofu.module.i5wei.promotion.dao.StorePromotionGratisDAO;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisChargeItem;

import huofucore.facade.i5wei.order.StoreOrderFacade.AsyncProcessor.makeDeliveryOrderDelivering;
import huofucore.facade.i5wei.promotion.StorePromotionConflictDTO;
import huofucore.facade.i5wei.promotion.StorePromotionConflictMapDTO;
import huofucore.facade.i5wei.promotion.StorePromotionGratisParam;

@Service
public class StorePromotionConflictService {
	@Autowired
	private StorePromotionGratisDAO storePromotionGratisDAO;
	
	@Autowired
	private StoreChargeItemPromotionService storeChargeItemPromotionService;
	
	@Autowired
	private StorePromotionGratisChargeItemDAO storePromotionGratisChargeItemDAO;
	
	 public Map<Long,List<StorePromotionGratis>>  getStorePromotionConflictMap(StorePromotionGratisParam param){
	         //获取首单特价
		 List<StoreChargeItemPromotion> chargeItemPromotions = this.storeChargeItemPromotionService.getListByIds(param.getMerchantId(), param.getStoreId(), param.getChargeItemIds(), System.currentTimeMillis());
		 List<StorePromotionGratis> gratisList = this.storePromotionGratisDAO.getStorePromotionGratisIntersectTime(param.getMerchantId(),  param.getStoreId(),param.getPrivilegeWay(), param.getBeginTime(), param.getEndTime());
     	 if(gratisList!=null){
     		Map<Long, List<StorePromotionGratisChargeItem>> chargeItemMap = this.storePromotionGratisChargeItemDAO.getMapInStorePromotionGratisIds(param.getMerchantId(), param.getStoreId(), StorePromotionGratis.getIds(gratisList), false, false);
     		 for (Entry<Long, List<StorePromotionGratisChargeItem>> entry : chargeItemMap.entrySet()) {
     			 Map<Long, Set<Long>> gratisMap = this.getGratisMap(entry.getValue());
     			 
     		 }
     	}
	        return null;
	    }
	
	 
	 public  Map<Long, List<StoreChargeItemPromotion>>  getChargeItemPromotionMap(List<StoreChargeItemPromotion> storeChargeItemPromotions){
		 Map<Long, List<StoreChargeItemPromotion>> map=new HashMap<>();
		 for (StoreChargeItemPromotion storeChargeItemPromotion : storeChargeItemPromotions) {
			 List<StoreChargeItemPromotion> list=new ArrayList<>();
			 list.add(storeChargeItemPromotion);
			 map.put(storeChargeItemPromotion.getChargeItemId(),list);
		}
		 return map;
	 }
	 
	 public  Map<Long, Set<Long>>  getGratisMap(List<StorePromotionGratisChargeItem> chargeItemList){
		 Map<Long, Set<Long>> map=new HashMap<>();
		for (StorePromotionGratisChargeItem chargeItem1 : chargeItemList) {
			Set<Long> set=new HashSet<>();
			for (StorePromotionGratisChargeItem  chargeItem2: chargeItemList) {
				if(chargeItem1.getChargeItemId()==chargeItem2.getChargeItemId()){
					set.add(chargeItem2.getPromotionGratisId());
				}
			}
			map.put(chargeItem1.getChargeItemId(), set);
		}
		 return null;
	 }
	/**
	 * 获取冲突的买免活动信息
	 * 
	 * @param gratisMap
	 * @return
	 */
	public <E> List<StorePromotionConflictMapDTO> buildStorePriomotionConflictDTOs(Map<Long, List<E>> map) {
		List<StorePromotionConflictMapDTO> conflictMapDTOs = Lists.newArrayList();
		Iterator<Map.Entry<Long, List<E>>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			StorePromotionConflictMapDTO storePromotionConflictMapDTO = new StorePromotionConflictMapDTO();
			List<StorePromotionConflictDTO> conflictDTOs = Lists.newArrayList();
			Map.Entry<Long, List<E>> entry = it.next();
		    conflictDTOs.addAll(this.buildStorePromotionConflictDTO(entry.getValue()));
			storePromotionConflictMapDTO.setChargeItemId(entry.getKey());
			storePromotionConflictMapDTO.setConflictIds(conflictDTOs);
			conflictMapDTOs.add(storePromotionConflictMapDTO);
		}
		return conflictMapDTOs;
	}

	/**
	 * 将买免实体/首单特价实体对象装换成StorePromotionConflict对象
	 * 
	 * @param gratisList
	 * @return
	 */
	public static <E> List<StorePromotionConflictDTO> buildStorePromotionConflictDTO(List<E> objs) {
		List<StorePromotionConflictDTO> conflictDTOList =new ArrayList<>();
		Object obj = objs.get(0);
		if (obj instanceof StorePromotionGratis) {
			for (Object objTemp : objs) {
				StorePromotionGratis storePromotionGratis = (StorePromotionGratis) objTemp;
				StorePromotionConflictDTO storePromotionConflictDTO = new StorePromotionConflictDTO();
				storePromotionConflictDTO.setPromotionId(storePromotionGratis.getPromotionGratisId());
				storePromotionConflictDTO.setTitle(storePromotionGratis.getTitle());
				storePromotionConflictDTO.setType(2);
				conflictDTOList.add(storePromotionConflictDTO);
			}
		}
		if (obj instanceof StoreChargeItemPromotion) {
			for (Object objTemp : objs) {
				StoreChargeItemPromotion storeChargeItemPromotion = (StoreChargeItemPromotion) objTemp;
				StorePromotionConflictDTO storePromotionConflictDTO = new StorePromotionConflictDTO();
				storePromotionConflictDTO.setPromotionId(storeChargeItemPromotion.getPromotionId());
				storePromotionConflictDTO.setTitle("首单特价");
				storePromotionConflictDTO.setType(1);
				conflictDTOList.add(storePromotionConflictDTO);
			}
		}
		return conflictDTOList;
	}


}
