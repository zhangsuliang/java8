package com.huofu.module.i5wei.order.service;

import huofucore.facade.i5wei.order.StoreOrderPromotionTypeEnum;
import huofucore.facade.i5wei.promotion.StoreOrderItemPromotionGratisStatDTO;
import huofucore.facade.i5wei.promotion.StoreOrderItemPromotionRebateStatDTO;
import huofucore.facade.i5wei.promotion.StoreOrderItemPromotionReduceStatDTO;
import huofuhelper.util.ObjectUtil;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.order.dao.StoreOrderItemPromotionDAO;

@Service
public class StoreOrderPromotionStatService {

	@Autowired
	private StoreOrderItemPromotionDAO storeOrderItemPromotionDAO;
	
	/**
	 * 根据买免活动ID，获取其在指定时间范围内的信息
	 * @param merchantId 商户ID
	 * @param storeId  店铺ID
	 * @param gratisId  活动ID
	 * @param startDate  开始时间
	 * @param endDate  结束时间
	 * @return
	 */
	public StoreOrderItemPromotionGratisStatDTO getStoreOrderPromotionGratisDTOStatByTimeRange(int merchantId,
			long storeId, long gratisId, long startDate, long endDate) {
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT count(distinct order_id) as sale_orders,sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price from  ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 ");
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_GRATIS.getValue();
		Map<String, Object> resultMap = this.storeOrderItemPromotionDAO.getResultMap(merchantId, storeId, sql.toString(), new Object[]{storeId,gratisId,promotionType,startDate,endDate}, true);
		return this.getStorePromotionGratisStatDTO(resultMap);
	}

	/**
    * 根据买免活动ID，获取其在指定日期下的信息
    * @param merchantId 商户ID
    * @param storeId  店铺ID
    * @param gratisId  活动ID
    * @param repastDate 更新日期
    * @return
    */
	public StoreOrderItemPromotionGratisStatDTO getStoreOrderPromotionGratisDTOStatByRepastDate(int merchantId, long storeId,long gratisId, long repastDate) {
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT count(distinct order_id) as sale_orders,sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price from");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_type=? and promotion_id=?  and repast_date=? and pay_order=1 and cancel_order=0 ");
		int promtionType = StoreOrderPromotionTypeEnum.PROMOTION_GRATIS.getValue();
		Map<String, Object> resultMap = this.storeOrderItemPromotionDAO.getResultMap(merchantId, storeId, sql.toString(), new Object[]{storeId,promtionType,gratisId,repastDate}, true);
		return this.getStorePromotionGratisStatDTO(resultMap);
	}
   
	/**
	 * 根据指定日期进行统计收费项目参加买免活动的情况(包含订单数量，原价，减免金额)
	 * @param merchantId  商户ID
	 * @param storeId  店铺ID 
	 * @param repastDate  就餐日期（指定日期）
	 * @return
	 */
	public List<StoreOrderItemPromotionGratisStatDTO> getStorePromotionGratisListStat(int merchantId, long storeId, long repastDate) {
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT promotion_id,count(distinct order_id) as sale_orders,sum(promotion_derate) as derate_price,sum(charge_item_price*amount) as sale_item_price FROM ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_type=? and repast_date=? and pay_order=1 and cancel_order=0 group by promotion_id");
		int promotionType=StoreOrderPromotionTypeEnum.PROMOTION_GRATIS.getValue();
		List<Map<String,Object>> resultMapList = storeOrderItemPromotionDAO.getResultMapList(merchantId, storeId, sql.toString(), new Object[]{storeId,promotionType,repastDate},true);
		if(resultMapList==null || resultMapList.isEmpty()){
			return Lists.newArrayList();
		}
		List<StoreOrderItemPromotionGratisStatDTO> resultList=Lists.newArrayList();
		for (Map<String,Object> map : resultMapList) {
			StoreOrderItemPromotionGratisStatDTO storeOrderItemPromotionGratisStatDTO=this.getStorePromotionGratisStatDTO(map);
			resultList.add(storeOrderItemPromotionGratisStatDTO);
		}
		return resultList;
	}

	
	private StoreOrderItemPromotionGratisStatDTO getStorePromotionGratisStatDTO(Map<String, Object> map) {
		StoreOrderItemPromotionGratisStatDTO storeOrderItemPromotionGratisStatDTO=new StoreOrderItemPromotionGratisStatDTO();
		long gratisId = ObjectUtil.getLong(map, "promotion_id");
		int saleOrders = ObjectUtil.getInt(map, "sale_orders");
		long deratePrice = ObjectUtil.getLong(map, "derate_price");
		long saleItemPrice = ObjectUtil.getLong(map, "sale_item_price");
		storeOrderItemPromotionGratisStatDTO.setPromotionGratisId(gratisId);
		storeOrderItemPromotionGratisStatDTO.setSaleOrders(saleOrders);
		storeOrderItemPromotionGratisStatDTO.setDeratePrice(deratePrice);
		storeOrderItemPromotionGratisStatDTO.setSaleItemPrice(saleItemPrice);
		return storeOrderItemPromotionGratisStatDTO;
	}


	public List<StoreOrderItemPromotionRebateStatDTO> getStorePromotionRebatesStat(int merchantId, long storeId, long repastDate){
		boolean enableSlave = true;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT promotion_id, count(distinct order_id) as sale_orders, sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price FROM ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_type=? and repast_date=? and pay_order=1 and cancel_order=0 group by promotion_id ");
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue();
		List<Map<String, Object>> dataMaps = storeOrderItemPromotionDAO.getResultMapList(merchantId, storeId, sql.toString(), new Object[]{storeId, promotionType, repastDate}, enableSlave);
		if (dataMaps == null || dataMaps.isEmpty()){
			return Lists.newArrayList();
		}
		List<StoreOrderItemPromotionRebateStatDTO> resultList = Lists.newArrayList();
		for (Map<String, Object> dataMap : dataMaps){
			StoreOrderItemPromotionRebateStatDTO storeOrderItemPromotionRebateStatDTO = this.getStorePromotionRebateDTO(dataMap);
			resultList.add(storeOrderItemPromotionRebateStatDTO);
		}
		return resultList;
	}

	public StoreOrderItemPromotionRebateStatDTO getStorePromotionRebateStat(int merchantId, long storeId, long promotionRebateId, long repastDate){
		boolean enableSlave = true;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT count(distinct order_id) as sale_orders, sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price FROM ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_id=? and promotion_type=? and repast_date=? and pay_order=1 and cancel_order=0 ");
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue();
		Map<String, Object> dataMap = storeOrderItemPromotionDAO.getResultMap(merchantId, storeId, sql.toString(), new Object[]{storeId, promotionRebateId, promotionType, repastDate}, enableSlave);
		return this.getStorePromotionRebateDTO(dataMap);
	}

	public StoreOrderItemPromotionRebateStatDTO getStorePromotionRebateStat(int merchantId, long storeId, long promotionRebateId, long startDate, long endDate){
		boolean enableSlave = true;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT count(distinct order_id) as sale_orders, sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price FROM ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 ");
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue();
		Map<String, Object> dataMap = storeOrderItemPromotionDAO.getResultMap(merchantId, storeId, sql.toString(), new Object[]{storeId, promotionRebateId, promotionType, startDate, endDate}, enableSlave);
		return this.getStorePromotionRebateDTO(dataMap);
	}

	private StoreOrderItemPromotionRebateStatDTO getStorePromotionRebateDTO(Map<String, Object> dataMap){
		if (dataMap == null) {
			return null;
		}
		StoreOrderItemPromotionRebateStatDTO storeOrderItemPromotionRebateStatDTO = new StoreOrderItemPromotionRebateStatDTO();
		long promotionId = ObjectUtil.getLong(dataMap, "promotion_id");
		int saleOrders = ObjectUtil.getInt(dataMap, "sale_orders");
		long deratePrice = ObjectUtil.getLong(dataMap, "derate_price");
		long saleItemPrice = ObjectUtil.getLong(dataMap, "sale_item_price");
		storeOrderItemPromotionRebateStatDTO.setPromotionRebateId(promotionId);
		storeOrderItemPromotionRebateStatDTO.setSaleOrders(saleOrders);
		storeOrderItemPromotionRebateStatDTO.setSaleItemPrice(saleItemPrice);
		storeOrderItemPromotionRebateStatDTO.setDeratePrice(deratePrice);
		return storeOrderItemPromotionRebateStatDTO;
	}

	public List<StoreOrderItemPromotionReduceStatDTO> getStorePromotionReducesStat(int merchantId, long storeId, long repastDate){
		boolean enableSlave = true;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT promotion_quota, promotion_reduce, count(distinct order_id) as sale_orders, sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price FROM ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_type=? and repast_date=? and pay_order=1 and cancel_order=0 group by promotion_quota, promotion_reduce");
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_REDUCE.getValue();
		List<Map<String, Object>> dataMaps = storeOrderItemPromotionDAO.getResultMapList(merchantId, storeId, sql.toString(), new Object[]{storeId, promotionType, repastDate}, enableSlave);
		return this.getStorePromotionRebateDTOs(dataMaps);
	}

	public List<StoreOrderItemPromotionReduceStatDTO> getStorePromotionReduceStat(int merchantId, long storeId, long promotionReduceId, long repastDate){
		boolean enableSlave = true;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT promotion_id, promotion_quota, promotion_reduce, count(distinct order_id) as sale_orders, sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price FROM ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_id=? and promotion_type=? and repast_date=? and pay_order=1 and cancel_order=0 group by promotion_quota, promotion_reduce");
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_REDUCE.getValue();
		List<Map<String, Object>> dataMaps = storeOrderItemPromotionDAO.getResultMapList(merchantId, storeId, sql.toString(), new Object[]{storeId, promotionReduceId, promotionType, repastDate}, enableSlave);
		return this.getStorePromotionRebateDTOs(dataMaps);
	}

	public List<StoreOrderItemPromotionReduceStatDTO> getStorePromotionReduceStat(int merchantId, long storeId, long promotionReduceId, long startDate, long endDate){
		boolean enableSlave = true;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT promotion_id, promotion_quota, promotion_reduce, count(distinct order_id) as sale_orders, sum(promotion_derate) as derate_price, sum(charge_item_price*amount) as sale_item_price FROM ");
		sql.append(storeOrderItemPromotionDAO.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and promotion_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 group by promotion_quota, promotion_reduce");
		int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_REDUCE.getValue();
		List<Map<String, Object>> dataMaps = storeOrderItemPromotionDAO.getResultMapList(merchantId, storeId, sql.toString(), new Object[]{storeId, promotionReduceId, promotionType, startDate, endDate}, enableSlave);
		return this.getStorePromotionRebateDTOs(dataMaps);
	}

	private List<StoreOrderItemPromotionReduceStatDTO> getStorePromotionRebateDTOs(List<Map<String, Object>> dataMaps){
		if (dataMaps == null || dataMaps.isEmpty()) {
			return Lists.newArrayList();
		}
		List<StoreOrderItemPromotionReduceStatDTO> resultList = Lists.newArrayList();
		for (Map<String, Object> dataMap : dataMaps){
			StoreOrderItemPromotionReduceStatDTO storeOrderItemPromotionReduceStatDTO = new StoreOrderItemPromotionReduceStatDTO();
			long promotionId = ObjectUtil.getLong(dataMap, "promotion_id");
			int saleOrders = ObjectUtil.getInt(dataMap, "sale_orders");
			long deratePrice = ObjectUtil.getLong(dataMap, "derate_price");
			long saleItemPrice = ObjectUtil.getLong(dataMap, "sale_item_price");
			storeOrderItemPromotionReduceStatDTO.setPromotionReduceId(promotionId);
			storeOrderItemPromotionReduceStatDTO.setSaleOrders(saleOrders);
			storeOrderItemPromotionReduceStatDTO.setSaleItemPrice(saleItemPrice);
			storeOrderItemPromotionReduceStatDTO.setDeratePrice(deratePrice);
			resultList.add(storeOrderItemPromotionReduceStatDTO);
		}
		return resultList;
	}

	/**
	 * 促销活动是否有被参与过
	 *
	 * @param merchantId
	 * @param storeId
	 * @param promotionId
	 * @param promotionType {@link StoreOrderPromotionTypeEnum}
	 * @param enableSlave
	 * @return
	 */
	public boolean hasStoreOrderPromotions(int merchantId, long storeId, long promotionId, int promotionType, boolean enableSlave){
		int count = storeOrderItemPromotionDAO.countOrderPromotions(merchantId, storeId, promotionId, promotionType, enableSlave);
		if (count <= 0) {
			return false;
		} else {
			return true;
		}
	}

}
