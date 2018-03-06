package com.huofu.module.i5wei.order.dao;

import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.ObjectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrderItemPromotion;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreOrderItemPromotionDAO extends AbsQueryDAO<StoreOrderItemPromotion> {
	
	private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
	
	public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreOrderItemPromotion.class);
        return dalInfo.getRealTable(StoreOrderItemPromotion.class);
    }
	
    @Override
    public void create(StoreOrderItemPromotion storeOrderItemPromotion) {
        this.addDbRouteInfo(storeOrderItemPromotion.getMerchantId(), storeOrderItemPromotion.getStoreId());
        super.create(storeOrderItemPromotion);
    }
    
    @Override
    public List<StoreOrderItemPromotion> batchCreate(List<StoreOrderItemPromotion> list) {
		if (list == null || list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreOrderItemPromotion storeOrderItemPromotion, StoreOrderItemPromotion snapshot) {
        this.addDbRouteInfo(storeOrderItemPromotion.getMerchantId(), storeOrderItemPromotion.getStoreId());
        super.update(storeOrderItemPromotion, snapshot);
    }

    @Override
    public void delete(StoreOrderItemPromotion storeOrderItemPromotion) {
        this.addDbRouteInfo(storeOrderItemPromotion.getMerchantId(), storeOrderItemPromotion.getStoreId());
        super.delete(storeOrderItemPromotion);
    }
    
    public void deleteByOrderId(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        query.delete(StoreOrderItemPromotion.class, "where store_id=? and order_id=? ", new Object[]{storeId, orderId});
    }
    
    public void updateTradeOrder(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreOrderItemPromotion.class, "set pay_order=1, trade_order=1 where store_id=? and order_id=? ", new Object[]{storeId, orderId});
    }
    
    public void updatePayOrder(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreOrderItemPromotion.class, "set pay_order=1 where store_id=? and order_id=? ", new Object[]{storeId, orderId});
    }
    
    public void updateCancelOrder(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreOrderItemPromotion.class, "set cancel_order=1 where store_id=? and order_id=? ", new Object[]{storeId, orderId});
    }
    
    public List<StoreOrderItemPromotion> getByOrderId(int merchantId, long storeId, String orderId, int promotionType) {
		if (storeId == 0 || orderId == null || orderId.isEmpty()) {
			return new ArrayList<StoreOrderItemPromotion>();
		}
        this.addDbRouteInfo(merchantId, storeId);
		List<StoreOrderItemPromotion> list = this.query.list(StoreOrderItemPromotion.class, " where store_id=? and order_id=? and promotion_type=? ", new Object[] { storeId, orderId, promotionType });
        return list;
    }
    
    /**
     * 订单的折扣活动促销额度统计 TODO
     * @return
     */
    public Map<Long, Long> getOrderPromotionsDerate(int merchantId, long storeId, List<String> orderIds, int promotionType, boolean enableSlave) {
		if (merchantId <= 0 || storeId <= 0 || orderIds == null || orderIds.isEmpty()){
            return Maps.newHashMap();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select promotion_id, sum(promotion_derate) as sum_promotion_derate from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and promotion_type=? ");
        sql.append(" and ").append(Query.createInSql("order_id", orderIds.size()));
        sql.append(" group by promotion_id ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<Object> params = new ArrayList<Object>();
        params.add(storeId);
        params.add(promotionType);
        params.addAll(orderIds);
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        if(mapList == null || mapList.size() == 0){
            return Maps.newHashMap();
        }
        Map<Long, Long> orderPromotions = Maps.newHashMap();
		for (Map<String, Object> map : mapList){
        	long promotionId = ObjectUtil.getLong(map, "promotion_id");
        	long promotionDerate = ObjectUtil.getLong(map, "sum_promotion_derate");
        	orderPromotions.put(promotionId, promotionDerate);
        }
        return orderPromotions;
    }
    
    public int countByChargeItem(int merchantId, long storeId, long chargeItemId, int promotionType) {
		if (chargeItemId <= 0) {
			return 0;
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.count(StoreOrderItemPromotion.class,
						" where store_id=? and charge_item_id=? and promotion_type=? and pay_order=1 and cancel_order=0 ", new Object[] { storeId, chargeItemId, promotionType });
    }
    
    public int countByChargeItemDate(int merchantId, long storeId, long chargeItemId, long repastDate, int promotionType) {
		if (chargeItemId <= 0) {
			return 0;
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.count(StoreOrderItemPromotion.class,
						" where store_id=? and charge_item_id=? and repast_date=? and promotion_type=? and pay_order=1 and cancel_order=0 ", new Object[] { storeId, chargeItemId, repastDate, promotionType });
    }
    
    public Map<Long, Integer> countByUser(int merchantId, long storeId, long userId, List<Long> chargeItemIds, int promotionType) {
    	if (userId <= 0 || chargeItemIds == null || chargeItemIds.isEmpty()) {
			return new HashMap<Long, Integer>();
		}
		this.addDbRouteInfo(merchantId, storeId);
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" select charge_item_id, count(*) as num from ");
		sql.append(this.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and user_id=? and promotion_type=? ");
		params.add(storeId);
		params.add(userId);
		params.add(promotionType);
		sql.append(" and ").append(Query.createInSql("charge_item_id", chargeItemIds.size()));
		params.addAll(chargeItemIds);
		sql.append(" and pay_order=1 and cancel_order=0 ");
		sql.append(" group by charge_item_id ");
		return this.getUserPromotions(sql.toString(), params);
    }
    
    private Map<Long, Integer> getUserPromotions(String sql, List<Object> params){
    	Map<Long, Integer> dtoMap = new HashMap<Long, Integer>();
		List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql, params.toArray(new Object[params.size()]));
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                long chargeItemId = ObjectUtil.getLong(map, "charge_item_id");
                int num = ObjectUtil.getInt(map, "num");
                dtoMap.put(chargeItemId, num);
            }
        }
		return dtoMap;
    }
    
    public Map<Long, Integer> countByUserRepastDate(int merchantId, long storeId, long userId, List<Long> chargeItemIds, long repastDate, int promotionType) {
    	if (userId <= 0 || chargeItemIds == null || chargeItemIds.isEmpty()) {
			return new HashMap<Long, Integer>();
		}
		this.addDbRouteInfo(merchantId, storeId);
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" select charge_item_id, count(*) as num from ");
		sql.append(this.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and user_id=? and repast_date=? and promotion_type=? ");
		params.add(storeId);
		params.add(userId);
		params.add(repastDate);
		params.add(promotionType);
		sql.append(" and ").append(Query.createInSql("charge_item_id", chargeItemIds.size()));
		params.addAll(chargeItemIds);
		sql.append(" and pay_order=1 and cancel_order=0 ");
		sql.append(" group by charge_item_id ");
		return this.getUserPromotions(sql.toString(), params);
    }
    
    public Map<Long, Integer> countByUserTimeBucketId(int merchantId, long storeId, long userId, List<Long> chargeItemIds, long repastDate, long timeBucketId, int promotionType) {
		if (userId <= 0 || chargeItemIds == null || chargeItemIds.isEmpty()) {
			return new HashMap<Long, Integer>();
		}
		this.addDbRouteInfo(merchantId, storeId);
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" select charge_item_id, count(*) as num from ");
		sql.append(this.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and user_id=? and repast_date=? and time_bucket_id=? and promotion_type=? ");
		params.add(storeId);
		params.add(userId);
		params.add(repastDate);
		params.add(timeBucketId);
		params.add(promotionType);
		sql.append(" and ").append(Query.createInSql("charge_item_id", chargeItemIds.size()));
		params.addAll(chargeItemIds);
		sql.append(" and pay_order=1 and cancel_order=0 ");
		sql.append(" group by charge_item_id ");
		return this.getUserPromotions(sql.toString(), params);
    }
    
    public Map<Long, Integer> countByUserTradeTime(int merchantId, long storeId, long userId, List<Long> chargeItemIds, long start, long end, int promotionType) {
    	if (userId <= 0 || chargeItemIds == null || chargeItemIds.isEmpty()) {
			return new HashMap<Long, Integer>();
		}
		this.addDbRouteInfo(merchantId, storeId);
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" select charge_item_id, count(*) as num from ");
		sql.append(this.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and user_id=? and promotion_type=? and repast_date between ? and ? ");
		params.add(storeId);
		params.add(userId);
		params.add(promotionType);
		params.add(start);
		params.add(end);
		sql.append(" and ").append(Query.createInSql("charge_item_id", chargeItemIds.size()));
		params.addAll(chargeItemIds);
		sql.append(" and pay_order=1 and cancel_order=0 ");
		sql.append(" group by charge_item_id ");
		return this.getUserPromotions(sql.toString(), params);
    }
    
    /**
     * 活动促销活动:有效时间内,参与人数
     * @return
     */
    public int countUser(int merchantId, long storeId, long chargeItemId, int promotionType){
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct(user_id)) as countUser from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and pay_order=1 and cancel_order=0");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType });
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getInt(mapList.get(0), "countUser");
    }

    /**
     * 活动促销活动:有效时间内,参与人数
     * @return
     */
    public int countUser(int merchantId, long storeId, long chargeItemId, long repastDate, int promotionType){
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || repastDate <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct(user_id)) as countUser from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and repast_date=? and promotion_type=? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, repastDate, promotionType });
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getInt(mapList.get(0), "countUser");
    }
    
    /**
     * 活动促销活动:有效时间内,参与人数
     * @return
     */
    public int countUser(int merchantId, long storeId, long chargeItemId, long startTime, long endTime, int promotionType){
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || startTime <= 0 || endTime <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select count(distinct(user_id)) as countUser from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType, startTime, endTime });
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getInt(mapList.get(0), "countUser");
    }
    
    /**
     * 本次活动促销订单总的消费额度(应付金额)
     * @return
     */
    public long countSumOrderPrice(int merchantId, long storeId, long chargeItemId, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(payable_price) as countSumOrderPrice from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType });
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getLong(mapList.get(0), "countSumOrderPrice");
    }
    
    /**
     * 本次活动促销订单总的消费额度(应付金额)
     * @return
     */
    public long countSumOrderPrice(int merchantId, long storeId, long chargeItemId, long repastDate, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || repastDate <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(payable_price) as countSumOrderPrice from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and repast_date=? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType , repastDate});
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getLong(mapList.get(0), "countSumOrderPrice");
    }
    
    /**
     * 本次活动促销订单总的消费额度(应付金额)
     * @return
     */
    public long countSumOrderPrice(int merchantId, long storeId, long chargeItemId, long startTime, long endTime, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || startTime <= 0 || endTime <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(payable_price) as countSumOrderPrice from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType, startTime, endTime });
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getLong(mapList.get(0), "countSumOrderPrice");
    }

    /**
     * 活动促销活动,优惠的价格,算总价
     * @return
     */
    public long countSumPromotionPrice(int merchantId, long storeId, long chargeItemId, int promotionType){
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(promotion_price) as countSumPayment from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType});
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getLong(mapList.get(0), "countSumPayment");
    }
    
    /**
     * 活动促销活动,优惠的价格,算总价
     * @return
     */
    public long countSumPromotionPrice(int merchantId, long storeId, long chargeItemId, long startTime, long endTime, int promotionType){
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || startTime <= 0 || endTime <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(promotion_price) as countSumPayment from " );
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType, startTime, endTime });
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getLong(mapList.get(0), "countSumPayment");
    }

    /**
     * 活动促销原价,总价格
     * @return
     */
    public long countSumPrice(int merchantId, long storeId, long chargeItemId, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(charge_item_price) as countSumPrice from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType});
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getLong(mapList.get(0), "countSumPrice");
    }

    /**
     * 活动促销原价,总价格
     * @return
     */
    public long countSumPrice(int merchantId, long storeId, long chargeItemId, long startTime, long endTime, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || startTime <= 0 || endTime <= 0){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(charge_item_price) as countSumPrice from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and charge_item_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 ");
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(sql.toString(), new Object[]{ storeId, chargeItemId, promotionType, startTime, endTime });
        if(mapList == null || mapList.size() == 0){
            return 0;
        }
        return ObjectUtil.getLong(mapList.get(0), "countSumPrice");
    }
    
    /**
     * 活动促销原价,总价格
     * @return
     */
    public long countPromotionCouponPrice(int merchantId, long storeId, long chargeItemId, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0){
            return 0;
        }
        long sumPrice = this.countSumPrice(merchantId, storeId, chargeItemId, promotionType);
        long sumPromotionPrice = this.countSumPromotionPrice(merchantId, storeId, chargeItemId, promotionType);
        long promotionCouponPrice = 0L;
		if (sumPromotionPrice < sumPrice) {
			promotionCouponPrice = sumPrice - sumPromotionPrice;
		}
        return promotionCouponPrice;
    }
    
    /**
     * 活动促销原价,总价格
     * @return
     */
    public long countPromotionCouponPrice(int merchantId, long storeId, long chargeItemId, long startTime, long endTime, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || startTime <= 0 || endTime <= 0){
            return 0;
        }
        long sumPrice = this.countSumPrice(merchantId, storeId, chargeItemId, startTime, endTime, promotionType);
        long sumPromotionPrice = this.countSumPromotionPrice(merchantId, storeId, chargeItemId, startTime, endTime, promotionType);
        long promotionCouponPrice = 0L;
		if (sumPromotionPrice < sumPrice) {
			promotionCouponPrice = sumPrice - sumPromotionPrice;
		}
        return promotionCouponPrice;
    }
    
    /**
     * 本次活动促销一共卖了多少单
     * @return
     */
    public int countOrder(int merchantId, long storeId, long chargeItemId, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0){
            return 0;
        }
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        return this.query.count(StoreOrderItemPromotion.class," where store_id=? and charge_item_id=? and promotion_type=? and pay_order=1 and cancel_order=0",new Object[]{ storeId, chargeItemId, promotionType});
    }
    
    /**
     * 本次活动促销一共卖了多少单
     * @return
     */
    public int countOrder(int merchantId, long storeId, long chargeItemId, long startTime, long endTime, int promotionType) {
        if(merchantId <= 0 || storeId <= 0 | chargeItemId <= 0 || startTime <= 0 || endTime <= 0){
            return 0;
        }
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        return this.query.count(StoreOrderItemPromotion.class," where store_id=? and charge_item_id=? and promotion_type=? and repast_date between ? and ? and pay_order=1 and cancel_order=0 ",
        		new Object[]{ storeId, chargeItemId, promotionType, startTime, endTime });
    }
    
    /**
     * 促销活动是否有人参与过
     * @return
     */
    public int countOrderPromotions(int merchantId, long storeId, long promotionId, int promotionType, boolean enableSlave) {
        if(merchantId <= 0 || storeId <= 0 | promotionId <= 0 ){
            return 0;
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        return this.query.count(StoreOrderItemPromotion.class," where store_id=? and promotion_id=? and promotion_type=? ", new Object[]{ storeId, promotionId, promotionType });
    }
    
    public Map<String, Object> getResultMap(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.getJdbcSupport().getMap(sql, params);
    }
    
    public List<Map<String, Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.getJdbcSupport().getMapList(sql, params);
    }

}
