package com.huofu.module.i5wei.meal.dao;

import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealAutoPrintParam;
import huofucore.facade.i5wei.meal.StoreMealHistoryQueryParam;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.ObjectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.menu.entity.StoreProduct;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealCheckoutDAO extends AbsQueryDAO<StoreMealCheckout> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
    
    @Override
    public void create(StoreMealCheckout storeMealCheckout) {
        this.addDbRouteInfo(storeMealCheckout.getMerchantId(), storeMealCheckout.getStoreId());
        super.create(storeMealCheckout);
    }

    @Override
    public List<StoreMealCheckout> batchCreate(List<StoreMealCheckout> list) {
		if (list == null || list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreMealCheckout storeMealCheckout) {
        throw new RuntimeException("StoreMealCheckout Entity dose not has tid ");
    }

    @Override
    public void delete(StoreMealCheckout storeMealCheckout) {
    	throw new RuntimeException("StoreMealCheckout Entity dose not has tid ");
    }
    
    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreMealCheckout.class);
        return dalInfo.getRealTable(StoreMealCheckout.class);
    }
    
    public List<StoreMealCheckout> getStoreMealCheckouts(int merchantId, long storeId, long repastDate, boolean enableSlave) {
        repastDate = DateUtil.getBeginTime(repastDate, null);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? and repast_date=? ");
        params.add(storeId);
        params.add(repastDate);
        sql.append(" and refund_meal=? ");
        params.add(false);
		if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public List<StoreMealCheckout> getStoreMealCheckouts(int merchantId, long storeId, long repastDate, long portId, boolean enableSlave) {
        repastDate = DateUtil.getBeginTime(repastDate, null);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? and repast_date=? ");
        params.add(storeId);
        params.add(repastDate);
		if (portId > 0) {
			sql.append(" and port_id in (0,?) ");
			params.add(portId);
		}
		sql.append(" and refund_meal=? ");
        params.add(false);
		if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealCheckout> getStoreMealCheckouts(StoreMealHistoryQueryParam storeMealHistoryQueryParam, List<Integer> takeSerialNumbers, boolean enableSlave) {
    	int merchantId = storeMealHistoryQueryParam.getMerchantId();
    	long storeId = storeMealHistoryQueryParam.getStoreId();
        long repastDate = storeMealHistoryQueryParam.getRepastDate();
        long portId = storeMealHistoryQueryParam.getPortId();
        repastDate = DateUtil.getBeginTime(repastDate, null);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? and repast_date=? ");
        params.add(storeId);
        params.add(repastDate);
		if (portId > 0) {
			sql.append(" and port_id in (0,?) ");
			params.add(portId);
		}
		String takeSerialNumberInSql = Query.createInSql("take_serial_number", takeSerialNumbers.size());
        sql.append(" and ").append(takeSerialNumberInSql);
        params.addAll(takeSerialNumbers);
        sql.append(" and refund_meal=? ");
        params.add(false);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
    	this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealCheckout> getStoreMealsHistoryByOrderId(int merchantId, long storeId, String orderId, boolean forUpdate, boolean enableSlave){
    	StringBuffer sql = new StringBuffer(" where order_id=? and refund_meal = ? ");
        List<Object> params = new ArrayList<>();
        params.add(orderId);
        params.add(false);
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealCheckout> getStoreMealsHistoryByOrderIds(int merchantId, long storeId, List<String> orderIds, boolean forUpdate, boolean enableSlave){
    	StringBuffer sql = new StringBuffer(" where store_id=? ");
        List<Object> params = new ArrayList<>();
        params.add(storeId);
        if (orderIds != null && !orderIds.isEmpty()) {
			String orderInSql = Query.createInSql("order_id", orderIds.size());
			sql.append(" and ").append(orderInSql);
			params.addAll(orderIds);
		}
        sql.append(" and refund_meal = ? ");
        params.add(false);
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealCheckout> getStoreMealsHistoryAllByOrderIds(int merchantId, long storeId, List<String> orderIds, boolean forUpdate, boolean enableSlave){
    	StringBuffer sql = new StringBuffer(" where store_id=? ");
        List<Object> params = new ArrayList<>();
        params.add(storeId);
        if (orderIds != null && !orderIds.isEmpty()) {
			String orderInSql = Query.createInSql("order_id", orderIds.size());
			sql.append(" and ").append(orderInSql);
			params.addAll(orderIds);
		}
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealCheckout> getStoreMealsHistoryByTableRecordId(int merchantId, long storeId, long tableRecordId, boolean forUpdate, boolean enableSlave){
    	StringBuffer sql = new StringBuffer(" where store_id=? and table_record_id=?");
        List<Object> params = new ArrayList<>();
        params.add(storeId);
        params.add(tableRecordId);
        sql.append(" and refund_meal=? ");
        params.add(false);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public int countStoreMealsHistoryByOrderId(int merchantId, long storeId, String orderId, boolean enableSlave){
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where order_id=?");
        params.add(orderId);
        sql.append(" and refund_meal=? ");
        params.add(false);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public Map<Long,Double> getStoreMealCheckoutProducts(int merchantId, long storeId, long repastDate, long timeBucketId, boolean enableSlave){
    	Map<Long,Double> productAmount = new HashMap<Long,Double>();
    	if (merchantId == 0 || storeId == 0 || repastDate == 0) {
			return productAmount;
		}
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT product_id, sum(amount_checkout*amount) as amount ");
		sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and repast_date=? ");
		params.add(storeId);
		params.add(repastDate);
		if (timeBucketId > 0) {
			sql.append(" and time_bucket_id=? ");
			params.add(timeBucketId);
		}
		sql.append(" group by product_id ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
		List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
		if (list == null || list.isEmpty()) {
			return productAmount;
		}
		for (Map<String, Object> obj : list) {
			long productId = ObjectUtil.getLong(obj.get("product_id"), 0);
			double takeupAmount = ObjectUtil.getDouble(obj.get("amount"), 0);
			productAmount.put(productId, takeupAmount);
		}
		return productAmount;
    }
    
    public Map<Long,Double> getStoreMealCheckoutProducts(int merchantId, long storeId, long repastDate, long timeBucketId, List<StoreProduct> storeProducts, boolean enableSlave){
    	this.addDbRouteInfo(merchantId, storeId);
    	Map<Long,Double> productAmount = new HashMap<Long,Double>();
		if (merchantId == 0 || storeId == 0 || repastDate == 0 || storeProducts == null || storeProducts.isEmpty()) {
			return productAmount;
		}
		List<Long> dayProductIds = new ArrayList<Long>();
		List<Long> otherProductIds = new ArrayList<Long>();
		for (StoreProduct product : storeProducts) {
			if (product.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
				dayProductIds.add(product.getProductId());
			}else{
				otherProductIds.add(product.getProductId());
			}
		}
		Map<Long,Double> dayProductAmount = this.getStoreMealCheckoutProductIds(merchantId, storeId, repastDate, 0, dayProductIds, enableSlave);
		productAmount.putAll(dayProductAmount);
		if (timeBucketId > 0) {
			Map<Long,Double> otherProductAmount = this.getStoreMealCheckoutProductIds(merchantId, storeId, repastDate, timeBucketId, otherProductIds, enableSlave);
			productAmount.putAll(otherProductAmount);
		}
		return productAmount;
    }
    
    public Map<Long,Double> getStoreMealCheckoutProductIds(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> productIds, boolean enableSlave){
    	Map<Long,Double> productAmount = new HashMap<Long,Double>();
		if (merchantId == 0 || storeId == 0 || repastDate == 0 || productIds == null || productIds.isEmpty()) {
			return productAmount;
		}
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT product_id, sum(amount_checkout*amount) as amount ");
		sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
		sql.append(" where store_id=? and repast_date=? ");
		params.add(storeId);
		params.add(repastDate);
		if (timeBucketId > 0) {
			sql.append(" and time_bucket_id=? ");
			params.add(timeBucketId);
		}
		String productInSql = Query.createInSql("product_id", productIds.size());
		sql.append(" and ").append(productInSql);
		params.addAll(productIds);
		sql.append(" group by product_id ");
		if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
		List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
		if (list == null || list.isEmpty()) {
			return productAmount;
		}
		for (Map<String, Object> obj : list) {
			long productId = ObjectUtil.getLong(obj.get("product_id"), 0);
			double takeupAmount = ObjectUtil.getDouble(obj.get("amount"), 0);
			productAmount.put(productId, takeupAmount);
		}
		return productAmount;
    }
    
    public double getStoreMealCheckoutProduct(int merchantId, long storeId, long repastDate, long timeBucketId, long productId, boolean enableSlave){
    	List<Long> productIds = new ArrayList<Long>();
    	productIds.add(productId);
    	Map<Long,Double> productAmount = this.getStoreMealCheckoutProductIds(merchantId, storeId, repastDate, timeBucketId, productIds, enableSlave);
		return productAmount.getOrDefault(productId, 0D);
    }
    
    /**
     * 查询未打印历史出餐（不区分是否退菜）
     * @param merchantId
     * @param storeId
     * @param portIds
     * @param enableSlave
     * @return
     */
    public List<StoreMealCheckout> getStoreMealsHistoryNotPrinted(int merchantId, long storeId, List<Long> portIds, boolean enableSlave){
		if (portIds == null || portIds.isEmpty()) {
			return new ArrayList<StoreMealCheckout>();
		}
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? ");
        params.add(storeId);
        String portInSql= Query.createInSql("port_id", portIds.size());
    	sql.append(" and ").append(portInSql);
    	params.addAll(portIds);
    	sql.append(" and printed=? ");
        params.add(false);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public int countStoreMealsHistoryNotPrinted(int merchantId, long storeId, List<Long> portIds, boolean enableSlave){
		if (portIds == null || portIds.isEmpty()) {
			return 0;
		}
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? ");
        params.add(storeId);
        String portInSql= Query.createInSql("port_id", portIds.size());
    	sql.append(" and ").append(portInSql);
    	params.addAll(portIds);
    	sql.append(" and printed=? ");
        params.add(false);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealCheckout> getStoreMealCheckoutHistoryById(int merchantId, long storeId, String orderId, int takeSerialSeq, boolean enableSlave) throws T5weiException, TException {
    	if (storeId == 0 || orderId == null || orderId.isEmpty()) {
			return null;
		}
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where order_id=? ");
        params.add(orderId);
        sql.append(" and take_serial_seq=? ");
        params.add(takeSerialSeq);
        sql.append(" and refund_meal=? ");
        params.add(false);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckout.class, sql.toString(), params.toArray(new Object[params.size()]));
	}
    
    /**
     * 更新产品打印状态
     *
     * @param merchantId
     * @param storeId
     * @param storeMealAutoPrintParams
     */
    public void batchUpdatePrinted(int merchantId, long storeId, List<StoreMealAutoPrintParam> storeMealAutoPrintParams) {
    	List<Object[]> orderParams = Lists.newArrayList();
        List<Object[]> serialNumberParams = Lists.newArrayList();
		for (StoreMealAutoPrintParam param : storeMealAutoPrintParams) {
			if(param.isPrinted()){
				if (DataUtil.isNotEmpty(param.getOrderId())) {
					orderParams.add(new Object[] { true, System.currentTimeMillis(), param.getOrderId(), param.getTakeSerialSeq()});
				}else{
					serialNumberParams.add(new Object[] { true, System.currentTimeMillis(), storeId, param.getPortId(), param.getRepastDate(), param.getTakeSerialNumber(), param.getTakeSerialSeq()});
				}
			}
		}
		if (!orderParams.isEmpty()) {
			this.addDbRouteInfo(merchantId, storeId);
			this.query.batchUpdate(StoreMealCheckout.class, "set printed=?, update_time=? where order_id=? and take_serial_seq=? ", orderParams);
		}
		if (!serialNumberParams.isEmpty()) {
			this.addDbRouteInfo(merchantId, storeId);
			this.query.batchUpdate(StoreMealCheckout.class, "set printed=?, update_time=? where store_id=? and port_id=? and repast_date=? and take_serial_number=? and take_serial_seq=? ", serialNumberParams);
		}
    }
    
    /**
     * 更新历史出餐桌牌号
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param siteNumber
     */
    public void updateSiteNumber(int merchantId, long storeId, String orderId, int siteNumber) {
		if (orderId == null || orderId.isEmpty()) {
			return;
		}
        this.addDbRouteInfo(merchantId, storeId);
		this.query.update(StoreMealCheckout.class, " set site_number=?, update_time=? where order_id=? ", new Object[] { siteNumber, System.currentTimeMillis(), orderId });
    }
    
    /**
     * 更新桌台记录ID
     * @param merchantId
     * @param storeId
     * @param originalTableRecordId
     * @param targetTableRecordId
     */
    public void updateTableRecordId(int merchantId, long storeId, long originalTableRecordId, long targetTableRecordId) {
		if (originalTableRecordId <= 0 || targetTableRecordId <= 0) {
			return;
		}
        this.addDbRouteInfo(merchantId, storeId);
		this.query.update(StoreMealCheckout.class, " set table_record_id=?, update_time=? where store_id=? and table_record_id=? ", new Object[] { targetTableRecordId, System.currentTimeMillis(), storeId, originalTableRecordId });
    }
    
    public List<Map<String,Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        return this.query.getJdbcSupport().getMapList(sql, params);
    }
}
