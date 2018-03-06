package com.huofu.module.i5wei.meal.service;

import halo.query.Query;
import huofucore.facade.i5wei.meal.StoreMeal4DistributeCountDTO;
import huofucore.facade.i5wei.meal.StoreMealOrderItemStatDTO;
import huofucore.facade.i5wei.meal.StoreMealProductStatDTO;
import huofucore.facade.i5wei.order.StoreOrderPayStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderRefundStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderTradeStatusEnum;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.ObjectUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutRecordDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;

@Service
public class StoreMealStatService {
	
	@Autowired
    private StoreOrderDAO storeOrderDAO;
	@Autowired
    private StoreOrderItemDAO storeOrderItemDAO;
    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDAO;
    @Autowired
    private StoreMealCheckoutDAO storeMealCheckoutDAO;
    @Autowired
    private StoreMealCheckoutRecordDAO storeMealCheckoutRecordDAO;
    @Autowired
	private StoreMealPortDAO storeMealPortDAO;
    @Autowired
    private StoreChargeItemService storeChargeItemService;
    
    /**
     * 预定收费项目数量统计
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
	public List<StoreMealOrderItemStatDTO> getRevenue5weiMealOrderItems(int merchantId, long storeId, long repastDate, long timeBucketId){
		List<StoreMealOrderItemStatDTO> result = new ArrayList<StoreMealOrderItemStatDTO>();
		if (merchantId == 0 || storeId == 0 || repastDate == 0) {
			return result;
		}
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT b.charge_item_id, b.charge_item_name, b.unit, sum(b.amount) as amount_order , sum(b.packed_amount) as amount_packaged ");
		sql.append(" FROM ").append(storeOrderDAO.getRealName(merchantId, storeId)).append(" a ") ;
		sql.append(" inner join ").append(storeOrderItemDAO.getRealName(merchantId, storeId)).append(" b on b.order_id=a.order_id ");
		sql.append(" where a.merchant_id=? and a.store_id=? and a.repast_date=? ");
		params.add(merchantId);
		params.add(storeId);
		params.add(repastDate);
		if (timeBucketId > 0) {
			sql.append(" and a.time_bucket_id=? ");
			params.add(timeBucketId);
		}
		sql.append(" and a.pay_status=? and refund_status=? and a.trade_status=? ");
		params.add(StoreOrderPayStatusEnum.FINISH.getValue());
		params.add(StoreOrderRefundStatusEnum.NOT.getValue());
		params.add(StoreOrderTradeStatusEnum.NOT.getValue());
		sql.append(" group by b.charge_item_id ");
		List<Map<String,Object>> list = storeMealTakeupDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(),true);
		if (list == null || list.isEmpty()) {
			return result;
		}
		for (Map<String, Object> obj : list) {
			Object charge_item_id = obj.get("charge_item_id");
			Object charge_item_name = obj.get("charge_item_name");
			Object unit = obj.get("unit");
			Object amount_order = obj.get("amount_order");
			Object amount_packaged = obj.get("amount_packaged");
			StoreMealOrderItemStatDTO itemVO = new StoreMealOrderItemStatDTO();
			itemVO.setMerchantId(merchantId);
			itemVO.setStoreId(storeId);
			itemVO.setRepastDate(repastDate);
			itemVO.setTimeBucketId(timeBucketId);
			itemVO.setChargeItemId(ObjectUtil.getLong(charge_item_id, 0));
			itemVO.setChargeItemName(charge_item_name.toString());
			if (unit == null || unit.toString().isEmpty()) {
				itemVO.setUnit("份");
			}else{
				itemVO.setUnit(unit.toString());
			}
			itemVO.setAmountOrder(ObjectUtil.getDouble(amount_order, 0));
			itemVO.setAmountPackaged(ObjectUtil.getDouble(amount_packaged, 0));
			result.add(itemVO);
		}
		return result;
	}
    
    /**
     * 已出餐产品数量统计
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
	public List<StoreMealProductStatDTO> getRevenue5weiMealTakeupProducts(int merchantId, long storeId, long repastDate, long timeBucketId){
		List<StoreMealProductStatDTO> result = new ArrayList<StoreMealProductStatDTO>();
		if (merchantId == 0 || storeId == 0 || repastDate == 0) {
			return result;
		}
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT product_id, product_name, unit, packaged, sum(remain_takeup*amount) as amount_order ");
		sql.append(" FROM ").append(storeMealTakeupDAO.getRealName(merchantId, storeId));
		sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
		params.add(merchantId);
		params.add(storeId);
		params.add(repastDate);
		if (timeBucketId > 0) {
			sql.append(" and time_bucket_id=? ");
			params.add(timeBucketId);
		}
		sql.append(" group by product_id, packaged having amount_order > 0");
		List<Map<String,Object>> list = storeMealTakeupDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(),true);
		return this.getListByResultMap(merchantId, storeId, repastDate, timeBucketId, list);
	}
	
	/**
	 * 已出餐产品数量统计
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @param timeBucketId
	 * @return
	 */
	public List<StoreMealProductStatDTO> getRevenue5weiMealCheckoutProducts(int merchantId, long storeId, long repastDate, long timeBucketId){
		List<StoreMealProductStatDTO> result = new ArrayList<StoreMealProductStatDTO>();
		if (merchantId == 0 || storeId == 0 || repastDate == 0) {
			return result;
		}
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT product_id, product_name, unit, packaged, sum(amount_checkout*amount) as amount_order ");
		sql.append(" FROM ").append(storeMealCheckoutDAO.getRealName(merchantId, storeId));
		sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
		params.add(merchantId);
		params.add(storeId);
		params.add(repastDate);
		if (timeBucketId > 0) {
			sql.append(" and time_bucket_id=? ");
			params.add(timeBucketId);
		}
		sql.append(" group by product_id ,packaged having amount_order > 0 ");
		List<Map<String,Object>> list = storeMealCheckoutDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(),true);
		return this.getListByResultMap(merchantId, storeId, repastDate, timeBucketId, list);
	}
	
	/**
     * 已出餐产品数量统计
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
	public List<StoreMealProductStatDTO> getStoreMealTakeupProductsByPorts(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> portIds){
		List<StoreMealProductStatDTO> result = new ArrayList<StoreMealProductStatDTO>();
		if (merchantId == 0 || storeId == 0 || repastDate == 0) {
			return result;
		}
		boolean enableSlave = true;
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT port_id, product_id, product_name, unit, packaged, sum(remain_takeup*amount) as amount_order ");
		sql.append(" FROM ").append(storeMealTakeupDAO.getRealName(merchantId, storeId));
		sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
		params.add(merchantId);
		params.add(storeId);
		params.add(repastDate);
		if (timeBucketId > 0) {
			sql.append(" and time_bucket_id=? ");
			params.add(timeBucketId);
		}
		if (portIds != null && !portIds.isEmpty()){
	        sql.append(" and ").append(Query.createInSql("port_id", portIds.size()));
	        params.addAll(portIds);
		}
		sql.append(" group by port_id, product_id, packaged having amount_order > 0 ");
		List<Map<String,Object>> list = storeMealTakeupDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(),enableSlave);
		result = this.getListByResultMap(merchantId, storeId, repastDate, timeBucketId, list);
		Map<Long, StoreMealPort> portMap = Maps.newHashMap();
		if (portIds != null && !portIds.isEmpty()) {
			portMap = storeMealPortDAO.getMapByIds(merchantId, storeId, portIds, enableSlave);
		} else {
			portMap = storeMealPortDAO.getMap(merchantId, storeId, enableSlave);
		}
		Set<Long> productIdsStat = Sets.newHashSet();
		for (StoreMealProductStatDTO dto : result) {
			long portId = dto.getPortId();
			if (portId > 0 && portMap.containsKey(portId)) {
				StoreMealPort storeMealPort = portMap.get(portId);
				dto.setPortName(storeMealPort.getName());
			}
			productIdsStat.add(dto.getProductId());
		}
		List<StoreProduct> productsInSell = storeChargeItemService.getStoreProductsForDate(merchantId, storeId, repastDate, timeBucketId, false, false);
		if (productsInSell != null){
			for (StoreProduct storeProduct : productsInSell){
				if (productIdsStat.contains(storeProduct.getProductId())) {
					continue;
				}
				StoreMealProductStatDTO itemVO = new StoreMealProductStatDTO();
				itemVO.setMerchantId(merchantId);
				itemVO.setStoreId(storeId);
				itemVO.setRepastDate(repastDate);
				itemVO.setTimeBucketId(timeBucketId);
				itemVO.setProductId(storeProduct.getProductId());
				itemVO.setProductName(storeProduct.getName());
				itemVO.setUnit(storeProduct.getUnit());
				long portId = storeProduct.getPortId();
				if (portId > 0 && portMap.containsKey(portId)) {
					StoreMealPort storeMealPort = portMap.get(portId);
					itemVO.setPortId(portId);
					itemVO.setPortName(storeMealPort.getName());
				}
				result.add(itemVO);
			}
		}
		return result;
	}
	
	private List<StoreMealProductStatDTO> getListByResultMap(int merchantId, long storeId, long repastDate, long timeBucketId, List<Map<String,Object>> list){
		List<StoreMealProductStatDTO> result = new ArrayList<StoreMealProductStatDTO>();
		if (list == null || list.isEmpty()) {
			return result;
		}
		Map<String, StoreMealProductStatDTO> resultMap = new HashMap<String, StoreMealProductStatDTO>();
		for (Map<String, Object> map : list) {
			long portId = ObjectUtil.getLong(map, "port_id");
			long productId = ObjectUtil.getLong(map, "product_id");
			String productName = ObjectUtil.getString(map, "product_name");
			double amountOrder = ObjectUtil.getDouble(map, "amount_order");
			String unit = ObjectUtil.getString(map, "unit");
			boolean packaged = ObjectUtil.getBoolean(map, "packaged");
			String key = String.valueOf(productId);
			if (portId > 0) {
				key = portId + "_" + productId;
			}
			StoreMealProductStatDTO itemVO = resultMap.get(key);
			if (itemVO == null) {
				itemVO = new StoreMealProductStatDTO();
				itemVO.setMerchantId(merchantId);
				itemVO.setStoreId(storeId);
				itemVO.setRepastDate(repastDate);
				itemVO.setTimeBucketId(timeBucketId);
				itemVO.setProductId(productId);
				itemVO.setProductName(productName);
				itemVO.setPortId(portId);
				itemVO.setUnit(unit.toString());
			}
			if(packaged){
				itemVO.setAmountPackaged(amountOrder);
			}else{
				itemVO.setAmountNotPackaged(amountOrder);
			}
			resultMap.put(key, itemVO);
		}
		result = new ArrayList<StoreMealProductStatDTO>(resultMap.values());
		for (StoreMealProductStatDTO dto : result) {
			double amountOrder = NumberUtil.add(dto.getAmountNotPackaged(), dto.getAmountPackaged());
			dto.setAmountOrder(amountOrder);
		}
		return result;
	}
	
	public void sortStoreMealProductListDesc(List<StoreMealProductStatDTO> list) {
		ComparatorStoreMealProductDesc comparator = new ComparatorStoreMealProductDesc();
		Collections.sort(list, comparator);
	}
	
	public class ComparatorStoreMealProductDesc implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreMealProductStatDTO obj0 = (StoreMealProductStatDTO) arg0;
			StoreMealProductStatDTO obj1 = (StoreMealProductStatDTO) arg1;
			int flag = Double.valueOf(obj1.getAmountOrder()).compareTo(obj0.getAmountOrder());
			return flag;
		}
	}
	
	public void sortStoreMealOrderItemListDesc(List<StoreMealOrderItemStatDTO> list) {
		ComparatorStoreMealOrderItemDesc comparator = new ComparatorStoreMealOrderItemDesc();
		Collections.sort(list, comparator);
	}
	
	public class ComparatorStoreMealOrderItemDesc implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			StoreMealOrderItemStatDTO obj0 = (StoreMealOrderItemStatDTO) arg0;
			StoreMealOrderItemStatDTO obj1 = (StoreMealOrderItemStatDTO) arg1;
			int flag = Double.valueOf(obj1.getAmountOrder()).compareTo(obj0.getAmountOrder());
			return flag;
		}
	}
	
	/**
	 * 出餐列表交班统计
	 * @param merchantId
	 * @param storeId
	 * @param countTimeBegin
	 * @param countTimeEnd
	 */
	public List<StoreMeal4DistributeCountDTO> getStoreMeal4DistributeCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd){
		if (merchantId == 0 || storeId == 0 || countTimeBegin == 0 || countTimeEnd == 0) {
			return new ArrayList<StoreMeal4DistributeCountDTO>();
		}
		List<StoreMeal4DistributeCountDTO> result = new ArrayList<StoreMeal4DistributeCountDTO>();
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" select staff_id, count(*) as total_order_count, sum(checkout_seq) as meal_distribute_order, sum(packaged_seq) as pack_distribute_order ");
		sql.append(" from ").append(storeMealCheckoutRecordDAO.getRealName(merchantId, storeId));
		sql.append(" where merchant_id=? and store_id=? and create_time between ? and ? ");
		params.add(merchantId);
		params.add(storeId);
		params.add(countTimeBegin);
		params.add(countTimeEnd);
		sql.append(" group by staff_id ");
		List<Map<String,Object>> list = storeMealCheckoutRecordDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(),true);
		if (list == null || list.isEmpty()) {
			return result;
		}
		for (Map<String, Object> obj : list) {
			Object staff_id = obj.get("staff_id");
			Object total_order_count = obj.get("total_order_count");
			Object meal_distribute_order = obj.get("meal_distribute_order");
			Object pack_distribute_order = obj.get("pack_distribute_order");
			StoreMeal4DistributeCountDTO itemVO = new StoreMeal4DistributeCountDTO();
			itemVO.setMerchantId(merchantId);
			itemVO.setStoreId(storeId);
			itemVO.setStaffId(ObjectUtil.getLong(staff_id, 0));
			itemVO.setTotalOrderCount(ObjectUtil.getInt(total_order_count, 0));
			itemVO.setMealDistributeOrder(ObjectUtil.getInt(meal_distribute_order, 0));
			itemVO.setPackDistributeOrder(ObjectUtil.getInt(pack_distribute_order, 0));
			result.add(itemVO);
		}
		return result;
	}

}
