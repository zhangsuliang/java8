
package com.huofu.module.i5wei.meal.dao;

import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepPrintParam;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.ObjectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealTakeupDAO extends AbsQueryDAO<StoreMealTakeup> {
	
    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreMealTakeup storeMealTakeup) {
        this.addDbRouteInfo(storeMealTakeup.getMerchantId(), storeMealTakeup.getStoreId());
        super.create(storeMealTakeup);
    }

    @Override
    public List<StoreMealTakeup> batchCreate(List<StoreMealTakeup> list) {
		if (list == null || list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreMealTakeup storeMealTakeup) {
        this.addDbRouteInfo(storeMealTakeup.getMerchantId(), storeMealTakeup.getStoreId());
        super.update(storeMealTakeup);
    }

    @Override
    public void delete(StoreMealTakeup storeMealTakeup) {
        this.addDbRouteInfo(storeMealTakeup.getMerchantId(), storeMealTakeup.getStoreId());
        super.delete(storeMealTakeup);
    }

    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreMealTakeup.class);
        return dalInfo.getRealTable(StoreMealTakeup.class);
    }
    
    public int clearTakeupRemainByOrderIds(int merchantId, long storeId, List<String> orderIds){
		if (orderIds == null || orderIds.isEmpty()) {
			return 0;
		}
    	this.addDbRouteInfo(merchantId, storeId);
    	List<Object> params = new ArrayList<>();
    	StringBuffer sql = new StringBuffer(" set remain_takeup=0 where store_id=? ");
    	params.add(storeId);
    	if (orderIds != null && !orderIds.isEmpty()) {
    		String orderInSql = Query.createInSql("order_id", orderIds.size());
    		sql.append(" and ").append(orderInSql);
    		params.addAll(orderIds);
    	}
        sql.append(" and remain_takeup>0 ");
    	return this.query.update2(StoreMealTakeup.class, sql.toString(), params);
    }

    public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, long repastDate, long timeBucketId, long refreshTime, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? ");
        params.add(storeId);
        if (repastDate > 0) {
            repastDate = DateUtil.getBeginTime(repastDate, null);
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        if (refreshTime > 0) {
            sql.append(" and create_time>=? ");
            params.add(refreshTime);
        }
        sql.append(" and remain_takeup>0 ");
        sql.append(" order by tid asc limit 0,600 ");
        List<StoreMealTakeup> list = this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
        return list;
    }
    
    public List<String> getStoreMealTakeupOrderIdsByExpireTime(int merchantId, long storeId, long expireTime, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" SELECT distinct order_id ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId)).append(" where store_id=? and create_time<=? and remain_takeup>0 ");
        params.add(storeId);
        params.add(expireTime);
        List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        if (list == null || list.isEmpty()) {
            return new ArrayList<String>();
        }
        List<String> orderIds = new ArrayList<String>();
        for (Map<String, Object> map : list) {
            String orderId = ObjectUtil.getString(map.get("order_id"));
            orderIds.add(orderId);
        }
        return orderIds;
    }
    
    public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> portIds, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? ");
        params.add(storeId);
        if (repastDate > 0) {
            repastDate = DateUtil.getBeginTime(repastDate, null);
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        sql.append(" order by tid asc limit 0,600 ");
        List<StoreMealTakeup> list = this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
        return list;
    }
    
    public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> portIds, int takeSerialNumber, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? ");
        params.add(storeId);
        if (repastDate > 0) {
            repastDate = DateUtil.getBeginTime(repastDate, null);
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        if (takeSerialNumber > 0) {
            sql.append(" and take_serial_number>=? ");
            params.add(takeSerialNumber);
        }
        sql.append(" order by tid asc limit 0,600 ");
        List<StoreMealTakeup> list = this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
        return list;
    }

    public List<StoreMealTakeup> getStoreMealTakeupsPackaged(int merchantId, long storeId, long portId, long refreshTime, boolean packaged, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? and packaged=? ");
        params.add(storeId);
        params.add(packaged);
        sql.append(" and remain_takeup>0 ");
        if (portId > 0) {
            sql.append(" and port_id=? ");
            params.add(portId);
        }
        if (refreshTime > 0) {
            sql.append(" and create_time>=? ");
            params.add(refreshTime);
        }
        sql.append(" order by tid asc ");
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public List<StoreMealTakeup> getStoreMealTakeupsByOrderPackaged(int merchantId, long storeId, String orderId, boolean packaged, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? and order_id=? and packaged=? and remain_takeup>0 ");
        params.add(storeId);
        params.add(orderId);
        params.add(packaged);
        if (forUpdate) {
            sql.append(" for update ");
        }
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public List<StoreMealTakeup> getStoreMealTakeupsByOrderPackaged(int merchantId, long storeId, String orderId, long portId, boolean packaged, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? and order_id=? and packaged=? and remain_takeup>0 and port_id=? ");
        params.add(storeId);
        params.add(orderId);
        params.add(packaged);
        params.add(portId);
        if (forUpdate) {
            sql.append(" for update ");
        }
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealTakeup> getStoreMealTakeupsByOrderIds(int merchantId, long storeId, List<String> orderIds,boolean isRemainTakeUp, boolean forUpdate, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? ");
        params.add(storeId);
        if (orderIds != null && !orderIds.isEmpty()) {
			String orderInSql = Query.createInSql("order_id", orderIds.size());
			sql.append(" and ").append(orderInSql);
			params.addAll(orderIds);
		}
        if(isRemainTakeUp){
            sql.append(" and remain_takeup>0 ");
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public List<StoreMealTakeup> getStoreMealTakeupsByOrderIdsForRemainSend(int merchantId, long storeId, List<String> orderIds,boolean isRemainTakeUp, boolean forUpdate, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? ");
        params.add(storeId);
        if (orderIds != null && !orderIds.isEmpty()) {
            String orderInSql = Query.createInSql("order_id", orderIds.size());
            sql.append(" and ").append(orderInSql);
            params.addAll(orderIds);
        }
        if(isRemainTakeUp){
            sql.append(" and remain_send>0 ");
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
            if(forUpdate){
                sql.append(" for update ");
            }
        }
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealTakeup> getStoreMealTakeupsByOrderId(int merchantId, long storeId, String orderId, long portId, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? and order_id=? and remain_takeup>0 and port_id=? ");
        params.add(storeId);
        params.add(orderId);
        params.add(portId);
        if (forUpdate) {
            sql.append(" for update ");
        }
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public List<StoreMealTakeup> getStoreMealTakeupsByTids(int merchantId, long storeId, List<Long> tids, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? ");
        params.add(storeId);
        if (tids != null && !tids.isEmpty()) {
            String tidSqls = Query.createInSql("tid", tids.size());
            sql.append(" and ").append(tidSqls);
            params.addAll(tids);
        }
        if (forUpdate) {
            sql.append(" for update ");
        }
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public int countStoreMealTakeupsByOrderPackaged(int merchantId, long storeId, String orderId, boolean packaged, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? and order_id=? and packaged=? and remain_takeup>0 ");
        params.add(storeId);
        params.add(orderId);
        params.add(packaged);
        return this.query.count(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public int countStoreMealTakeupsByOrderId(int merchantId, long storeId, String orderId, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? and order_id=? and remain_takeup>0 ");
        params.add(storeId);
        params.add(orderId);
        return this.query.count(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public int countStoreMealTakeupsByOrderId(int merchantId, long storeId, long portId, String orderId, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(" where store_id=? and port_id=? and order_id=? and remain_takeup>0 ");
        params.add(storeId);
        params.add(portId);
        params.add(orderId);
        return this.query.count(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    public List<StoreMealTakeup> getStoreMealsByOrderId(int merchantId, long storeId, String orderId, boolean forUpdate, boolean enableSlave) {
    	List<Object> params = new ArrayList<>();
    	StringBuilder sql = new StringBuilder(" where store_id=? and order_id=? and remain_takeup>0 ");
        params.add(storeId);
        params.add(orderId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }


    public List<StoreMealTakeup> getStoreMealsByOrderIds(int merchantId, long storeId, List<String> orderIds, boolean forUpdate, boolean enableSlave) {
    	List<Object> params = new ArrayList<>();
    	StringBuilder sql = new StringBuilder(" where store_id=? ");
        params.add(storeId);
		if (orderIds != null && !orderIds.isEmpty()) {
			String orderInSql = Query.createInSql("order_id", orderIds.size());
			sql.append(" and ").append(orderInSql);
			params.addAll(orderIds);
		}
		sql.append(" and remain_takeup>0 ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<StoreMealTakeup> getStoreMealsByTableRecordId(int merchantId, long storeId, long tableRecordId, boolean forUpdate, boolean enableSlave) {
    	List<Object> params = new ArrayList<>();
    	StringBuilder sql = new StringBuilder(" where store_id=? and table_record_id=? and remain_takeup>0 ");
        params.add(storeId);
        params.add(tableRecordId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }else{
        	if(forUpdate){
        		sql.append(" for update ");
        	}
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public Map<Long, Double> getStoreMealTakeupProducts(int merchantId, long storeId, long repastDate, long timeBucketId, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        Map<Long, Double> productAmount = new HashMap<Long, Double>();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return productAmount;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT product_id, sum(remain_takeup*amount) as amount ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and repast_date=? ");
        params.add(storeId);
        params.add(repastDate);
		if (timeBucketId > 0) {
			sql.append(" and time_bucket_id=? ");
			params.add(timeBucketId);
		}
		sql.append(" and remain_takeup>0 ");
        sql.append(" group by product_id ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
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
    
    public Map<Long, Double> getStoreMealTakeupProducts(int merchantId, long storeId, long repastDate, long timeBucketId, List<StoreProduct> storeProducts, boolean enableSlave) {
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
		Map<Long,Double> dayProductAmount = this.getStoreMealTakeupProductIds(merchantId, storeId, repastDate, 0, dayProductIds, enableSlave);
		productAmount.putAll(dayProductAmount);
		if (timeBucketId > 0) {
			Map<Long,Double> otherProductAmount = this.getStoreMealTakeupProductIds(merchantId, storeId, repastDate, timeBucketId, otherProductIds, enableSlave);
			productAmount.putAll(otherProductAmount);
		}
		return productAmount;
    }
    
    public Map<Long, Double> getStoreMealTakeupProductIds(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> productIds, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        Map<Long, Double> productAmount = new HashMap<Long, Double>();
        if (merchantId == 0 || storeId == 0 || repastDate == 0 || productIds == null || productIds.isEmpty()) {
            return productAmount;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT product_id, sum(remain_takeup*amount) as amount ");
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
		sql.append(" and remain_takeup>0 ");
        sql.append(" group by product_id ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
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

    public double getStoreMealTakeupProduct(int merchantId, long storeId, long repastDate, long timeBucketId, long productId, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (merchantId == 0 || storeId == 0 || repastDate == 0 || productId == 0) {
            return 0;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT sum(remain_takeup*amount) as amount ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and repast_date=? and product_id=? ");
        params.add(storeId);
        params.add(repastDate);
        params.add(productId);
        if (timeBucketId > 0) {
			sql.append(" and time_bucket_id=? ");
			params.add(timeBucketId);
		}
        sql.append(" and remain_takeup>0 ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        if (list == null || list.isEmpty()) {
            return 0;
        }
        return ObjectUtil.getDouble(list.get(0).get("amount"), 0);
    }

    /**
     * 查询指定数量的待出餐订单ID
     * @param merchantId
     * @param storeId
     * @param portIds
     * @param num
     * @param enableSlave
     * @return
     */
    public List<String> getStoreMealTakeupOrders(int merchantId, long storeId, List<Long> portIds, int num, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
		if (merchantId == 0 || storeId == 0 || num == 0) {
			return new ArrayList<String>();
		}
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT * FROM (SELECT distinct order_id, create_time ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId)).append(" where store_id=? ");
        params.add(storeId);
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        sql.append(" ) a order by a.create_time asc limit 0,? ");
        params.add(num);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        if (list == null || list.isEmpty()) {
            return new ArrayList<String>();
        }
        List<String> orderIds = new ArrayList<String>();
        for (Map<String, Object> map : list) {
            String orderId = ObjectUtil.getString(map.get("order_id"));
            orderIds.add(orderId);
        }
        return orderIds;
    }
    
    /**
     * 统计待出餐订单数（不区分是否退菜）
     * @param merchantId
     * @param storeId
     * @param portIds
     * @param enableSlave
     * @return
     */
    public int countStoreMealTakeups(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
		if (merchantId == 0 || storeId == 0) {
			return 0;
		}
        List<Object> params = new ArrayList<Object>();
        params.add(merchantId);
        params.add(storeId);
        StringBuilder sql = new StringBuilder(" where merchant_id=? and store_id=? ");
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        return this.query.count2(StoreMealTakeup.class, sql.toString(), params);
    }
    
    public List<String> getStoreMealTakeupOrderIds(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<String> orderIds = new ArrayList<String>();
        if (merchantId == 0 || storeId == 0) {
			return orderIds;
		}
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT distinct order_id, create_time ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId)).append(" where store_id=? ");
        params.add(storeId);
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        sql.append("order by create_time asc");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        if (list == null || list.isEmpty()) {
            return orderIds;
        }
		for (Map<String, Object> map : list) {
			String orderId = ObjectUtil.getString(map, "order_id");
			orderIds.add(orderId);
		}
        return orderIds;
    }
    
    public Map<String, Integer> getStoreMealTakeupOrderNum(int merchantId, long storeId, List<String> orderIds, List<Long> portIds, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        Map<String, Integer> resultMap = new HashMap<String, Integer>();
		if (merchantId == 0 || storeId == 0) {
			return resultMap;
		}
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT order_id, count(*) as num, create_time ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId)).append(" where store_id=? ");
        params.add(storeId);
        if (orderIds != null && !orderIds.isEmpty()) {
            String orderInSql = Query.createInSql("order_id", orderIds.size());
            sql.append(" and ").append(orderInSql);
            params.addAll(orderIds);
        } else {
            return resultMap;
        }
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
			portIds.add(0L);
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        sql.append(" group by order_id ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
        if (list == null || list.isEmpty()) {
            return resultMap;
        }
        for (Map<String, Object> map : list) {
            String orderId = ObjectUtil.getString(map.get("order_id"));
            int num = ObjectUtil.getInt(map, "num");
            resultMap.put(orderId, num);
        }
        return resultMap;
    }
    
    public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, String orderId, long portId, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? and order_id=? and remain_takeup>0 and port_id=? ");
        params.add(storeId);
        params.add(orderId);
        params.add(portId);
        sql.append(" order by tid asc ");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StoreMealTakeup> list = this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
        return list;
    }

    /**
     * 根据订单ID列表查询出餐口的待出餐（不区分是否退菜）
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @param portIds
     * @param enableSlave
     * @return
     */
    public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, List<String> orderIds, List<Long> portIds, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where store_id=? ");
        params.add(storeId);
        if (orderIds != null && !orderIds.isEmpty()) {
            String orderInSql = Query.createInSql("order_id", orderIds.size());
            sql.append(" and ").append(orderInSql);
            params.addAll(orderIds);
        } else {
            return new ArrayList<StoreMealTakeup>();
        }
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StoreMealTakeup> list = this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
        return list;
    }
    
    public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where merchant_id=? and store_id=? ");
        params.add(merchantId);
        params.add(storeId);
        sql.append(" and remain_takeup>0 ");
        if (portIds != null && !portIds.isEmpty()) {
            String portInSql = Query.createInSql("port_id", portIds.size());
            sql.append(" and ").append(portInSql);
            params.addAll(portIds);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StoreMealTakeup> list = this.query.list(StoreMealTakeup.class, sql.toString(), params.toArray(new Object[params.size()]));
        return list;
    }

    public int updatePort(int merchantId, long storeId, long portId, long newPortId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreMealTakeup.class, "set port_id=? where store_id=? and remain_takeup>0 and port_id=? ", new Object[]{newPortId, storeId, portId});
    }
    
    public int updatePort0(int merchantId, long storeId, long portId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreMealTakeup.class, "set port_id=? where store_id=? and remain_takeup>0 and port_id=0 ", new Object[]{portId, storeId});
    }

    /**
     * 更新出餐口信息
     *
     * @param merchantId              商户id
     * @param storeId                 店铺id
     * @param updateProductPortParams 条件参数
     * @param newPortId               新的出餐口id
     * @param hasPackagePort          是否是打包出餐口，如果店铺有打包出餐口，就设置为true
     * @return batchUpdate 结果
     */
    public int[] updateProductPort(int merchantId, long storeId, List<UpdateProductPortParam> updateProductPortParams, long newPortId, boolean hasPackagePort) {
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder("set port_id=? where store_id=? and charge_item_id=? and product_id=? ");
        List<Object[]> list = Lists.newArrayList();
        int argSize = 4;
        if (hasPackagePort) {
            argSize = 5;
            sql.append(" and packaged=?");
        }
        sql.append(" and remain_takeup>0 ");
        for (UpdateProductPortParam updateProductPortParam : updateProductPortParams) {
            Object[] args = new Object[argSize];
            args[0] = newPortId;
            args[1] = storeId;
            args[2] = updateProductPortParam.getChargeItemId();
            args[3] = updateProductPortParam.getProductId();
            if (hasPackagePort) {
                args[4] = false;
            }
            list.add(args);
        }
        return this.query.batchUpdate(StoreMealTakeup.class, sql.toString(), list);
    }

    /**
     * 更新出餐口信息
     *
     * @param merchantId     商户id
     * @param storeId        店铺id
     * @param chargeItemId   收费项目id
     * @param newPortId      新的出餐口id
     * @param hasPackagePort 是否是打包出餐口，如果店铺有打包出餐口，就设置为true
     * @return update 结果
     */
    public int updateProductPort(int merchantId, long storeId, long chargeItemId, long newPortId, boolean hasPackagePort) {
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder("set port_id=? where store_id=? and charge_item_id=? ");
        int argSize = 3;
        if (hasPackagePort) {
            argSize = 4;
            sql.append(" and packaged=?");
        }
        sql.append(" and remain_takeup>0 ");
        Object[] args = new Object[argSize];
        args[0] = newPortId;
        args[1] = storeId;
        args[2] = chargeItemId;
        if (hasPackagePort) {
            args[3] = false;
        }
        return this.query.update(StoreMealTakeup.class, sql.toString(), args);
    }

    /**
     * 更改打包出餐口
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param packagePortId 打包出餐口id
     * @return update 结果
     */
    public int updatePackagePort(int merchantId, long storeId, long packagePortId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreMealTakeup.class, " set port_id=? where store_id=? and packaged=? and remain_takeup>0 ", new Object[]{packagePortId, storeId, true});
    }
    
    /**
     * 更改外送出餐口
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param deliveryPortId 外送出餐口id
     * @return update 结果
     */
    public int updateDeliveryPort(int merchantId, long storeId, long deliveryPortId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreMealTakeup.class, " set port_id=? where store_id=? and take_mode=? and remain_takeup>0 ", new Object[]{deliveryPortId, storeId, StoreOrderTakeModeEnum.SEND_OUT.getValue()});
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
		this.query.update(StoreMealTakeup.class, " set table_record_id=?, update_time=? where store_id=? and table_record_id=? ", new Object[] { targetTableRecordId, System.currentTimeMillis(), storeId, originalTableRecordId });
    }

    public List<Map<String, Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        return this.query.getJdbcSupport().getMapList(sql, params);
    }

    //获取待出餐列表
    public List<StoreMealTakeup> getStoreMealTakeupsByStoreId(int merchantId,long storeId,boolean enableSlave){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealTakeup.class,"where store_id = ? and remain_takeup>0 ",new Object[]{storeId});
    }

    //获取待划菜列表
    public List<StoreMealTakeup> getRemainSendStoreMealTakeupsByStoreId(int merchantId,long storeId,boolean enableSlave){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealTakeup.class,"where store_id = ? and remain_send>0 ",new Object[]{storeId});
    }

    //根据订单ID获取待出餐列表
    public List<StoreMealTakeup> getStoreMealTakeupsByOrderId(int merchantId,long storeId,String orderId,boolean enableSlave){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealTakeup.class,"where store_id=? and order_id=? and remain_takeup>0 ",new Object[]{storeId,orderId});
    }
    
    //划菜相关start
    /**
     * 计算未划完菜品剩余堂食和剩余打包的数量
     * @param merchantId
     * @param storeId
     * @param sendPortId
     * @param enableSlave
     * @return
     */
    public List<StoreProductSweepAmount> countStoreProductSweepAmount(int merchantId, long storeId, long sendPortId, boolean enableSlave){
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        List<StoreProductSweepAmount> storeProductSweepAmounts = new ArrayList<StoreProductSweepAmount>();
        StringBuffer sql = new StringBuffer();
        sql.append(" select product_id, product_name, sum(if(packaged = 1, remain_send*amount, 0)) packaged_remain_send, sum(if(packaged=0, remain_send*amount, 0)) dine_in_remain_send from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where remain_send > 0 and send_port_id = ? group by product_id order by product_id");
        
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(
                sql.toString(),
                new Object[]{sendPortId});
        
        for (Map<String, Object> map : mapList) {
            StoreProductSweepAmount storeProductSweepAmount = new StoreProductSweepAmount();
            storeProductSweepAmount.setProductId(ObjectUtil.getLong(map, "product_id"));
            storeProductSweepAmount.setProductName(ObjectUtil.getString(map, "product_name"));
            storeProductSweepAmount.setDineInRemainSend(ObjectUtil.getDouble(map, "dine_in_remain_send"));
            storeProductSweepAmount.setPackagedRemainSend(ObjectUtil.getDouble(map, "packaged_remain_send"));
            storeProductSweepAmounts.add(storeProductSweepAmount);
        }
        return storeProductSweepAmounts;
    }

    public List<StoreMealTakeup> getStoreMealSendsByTableRecordId(int merchantId, long storeId, long sendPortId, long tableRecordId, boolean enableSlave, boolean forUpdate) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" where remain_send > 0 ");
        if(sendPortId > 0){
            sql.append(" and send_port_id = ? ");
            params.add(sendPortId);
        }
        if(tableRecordId > 0){
            sql.append(" and table_record_id = ? ");
            params.add(tableRecordId);
        }
        if(forUpdate){
            sql.append(" for update");
        }
        return this.query.list2(StoreMealTakeup.class, sql.toString(), params);
    }

    public List<StoreMealTakeup> getStoreMealSendsByOrderId(int merchantId, long storeId, long sendPortId, String orderId, boolean enableSlave, boolean forUpdate) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" where remain_send > 0 ");
        if(sendPortId > 0){
            sql.append(" and send_port_id = ? ");
            params.add(sendPortId);
        }
        if(!DataUtil.isEmpty(orderId)){
            sql.append(" and order_id = ? ");
            params.add(orderId);
        }
        if(forUpdate){
            sql.append(" for update");
        }
        return this.query.list2(StoreMealTakeup.class, sql.toString(), params);
    }
    
    public Map<String, Object> getTableRecordIdsAndOrderIdsInProductId(int merchantId, long storeId, long sendPortId, long productId, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        StringBuffer sql = new StringBuffer();
        sql.append(" select table_record_id, order_id from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where remain_send > 0 and send_port_id = ? and product_id = ? limit 800 ");
        Set<Long> tableRecordIds = new HashSet<Long>();
        Set<String> orderIds = new HashSet<String>();
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(
                sql.toString(), 
                new Object[]{sendPortId,productId});
        Map<String, Object> result = new HashMap<String, Object>();
        for (Map<String, Object> map : mapList) {
            tableRecordIds.add(ObjectUtil.getLong(map, "table_record_id"));
            orderIds.add(ObjectUtil.getString(map, "order_id"));
        }
        result.put("tableRecordIds", tableRecordIds);
        result.put("orderIds", orderIds);
        return result;
    }
    
    /**
     * 获取订单、收费项获取未划完的数据
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @param chargeItemIds
     * @param enableSlave
     * @return
     */
    public List<StoreMealTakeup> getStoreMealTakeupInChargeItemIds(int merchantId, long storeId, long sendPortId, List<Long> chargeItemIds, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuffer sql = new StringBuffer();
        List<Object> param = new ArrayList<Object>();
        
        sql.append(" where remain_send > 0 and send_port_id = ? ");
        param.add(sendPortId);
        if(chargeItemIds.size() > 0){
            String chargeItemIdSql = Query.createInSql("charge_item_id", chargeItemIds.size());
            sql.append(" and ").append(chargeItemIdSql);
            param.addAll(chargeItemIds);
        }
        return this.query.list2(StoreMealTakeup.class, sql.toString(), param);
    }
    
    /**
     * 划菜
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param sendPortId
     */
    public void sweepStoreMeals(int merchantId, long storeId, List<StoreMealTakeup> storeMealTakeups){
        this.addDbRouteInfo(merchantId, storeId);
        List<Object[]> params = new ArrayList<Object[]>();
        if(storeMealTakeups.isEmpty()){
            return;
        }
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            Object[] obj = new Object[]{storeMealTakeup.getRemainSend(),storeMealTakeup.getSweepTime(),System.currentTimeMillis(),storeMealTakeup.getTid()};
            params.add(obj);
        }
        
        this.query.batchUpdate(
                StoreMealTakeup.class,
                " set remain_send = ? , sweep_time = ? , update_time = ? where tid = ? ",
                params);
    }
    
    /**
     * 根据收费项和产品数组获取需要划菜的数据
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param chargeItemId
     * @param productIds
     * @param packaged
     * @param enableSlave
     * @param forUpdate
     * @return
     */
    public List<StoreMealTakeup> getStoreMealTakeup4Sweep(int merchantId, long storeId, String orderId, long chargeItemId, List<Long> productIds, boolean packaged, boolean enableSlave, boolean forUpdate) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        if(DataUtil.isEmpty(orderId) || chargeItemId <= 0 || productIds == null || productIds.isEmpty()){
            return new ArrayList<StoreMealTakeup>();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<Object>();
        
        StringBuffer sql = new StringBuffer();
        sql.append(" where order_id = ? and charge_item_id = ? ");
        params.add(orderId);
        params.add(chargeItemId);
        sql.append(" and ").append(Query.createInSql(" product_id ", productIds.size()));
        params.addAll(productIds);
        sql.append(" and packaged = ? and remain_send > 0 ");
        params.add(packaged);
        if(forUpdate){
            sql.append(" for update");
        }
        return this.query.list2(StoreMealTakeup.class, sql.toString(), params);
    }
    
    public Map<Long, StoreMealTakeup> getStoreMealTakeupMapByTids(int merchantId, long storeId, List<Long> tids, boolean forUpdate) {
        Map<Long, StoreMealTakeup> storeMealTakeupMap = new HashMap<Long, StoreMealTakeup>();
        if(tids == null || tids.isEmpty()){
            return storeMealTakeupMap;
        }
        List<StoreMealTakeup> storeMealTakeups = this.getStoreMealTakeupsByTids(merchantId, storeId, tids, forUpdate);
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            storeMealTakeupMap.put(Long.valueOf(storeMealTakeup.getTid()), storeMealTakeup);
        }
        return storeMealTakeupMap;
    }
    
    public List<StoreMealTakeup> getStoreMealTakeups4Refund(int merchantId, long storeId, StoreOrderRefundItem refundItem, boolean enableSlave, boolean forUpdate) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuffer sql = new StringBuffer(" where order_id = ? and charge_item_id = ? and packaged = ? and remain_send > 0 ");
        if(forUpdate){
            sql.append(" for update ");
        }
        return this.query.list(StoreMealTakeup.class, 
                sql.toString(), 
                new Object[]{refundItem.getOrderId(),refundItem.getChargeItemId(),refundItem.isPacked()});
    }

    public List<StoreMealTakeup> getStoreMealSweepsByTableRecordId(StoreMealSweepPrintParam param, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        long tableRecordId = param.getTableRecordId();
        boolean packed = param.isPacked();
        boolean printOrder = param.isPrintOrder();
        this.addDbRouteInfo(merchantId, storeId);
        
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" where table_record_id = ? and send_port_id = ? ");
        params.add(tableRecordId);
        params.add(sendPortId);
        if(!printOrder){
            sql.append(" and packaged = ? ");
            params.add(packed);
        }
        return this.query.list2(StoreMealTakeup.class, sql.toString(), params);
    }

    public List<StoreMealTakeup> getStoreMealSweepsByOrderId(StoreMealSweepPrintParam param, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        String orderId = param.getOrderId();
        boolean packed = param.isPacked();
        boolean printOrder = param.isPrintOrder();
        this.addDbRouteInfo(merchantId, storeId);
        
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" where order_id = ? and send_port_id = ? ");
        params.add(orderId);
        params.add(sendPortId);
        if(!printOrder){
            sql.append(" and packaged = ? ");
            params.add(packed);
        }
        return this.query.list2(StoreMealTakeup.class, sql.toString(), params);
    }
    
    /**
     * 传菜口是否全部划完
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param enableSlave
     * @return
     */
    public boolean isStoreMealPortSendAllSweep(int merchantId, long storeId, long sendPortId, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        int count = this.query.count(StoreMealTakeup.class,
                " where remain_send > 0 and send_port_id = ? ",
                new Object[]{sendPortId});
        return count > 0 ? false : true ;
    }
    
    /**
     * 多个加工档口是否全部划完
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param enableSlave
     * @return
     */
    public boolean isStoreMealPortsAllSweep(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        if(portIds == null || portIds.isEmpty()){
            return true;
        }
        
        this.addDbRouteInfo(merchantId, storeId);
        StringBuffer sql = new StringBuffer();
        List<Object> params = new ArrayList<Object>();
        sql.append(" where remain_send > 0 ");
        sql.append(" and ").append(Query.createInSql(" port_id ", portIds.size()));
        params.addAll(portIds);
        int count = this.query.count2(StoreMealTakeup.class, sql.toString(), params);
        return count > 0 ? false : true ;
    }
    
    /**
     * 单个加工档口是否全部划完
     * @param merchantId
     * @param storeId
     * @param portId
     * @param enableSlave
     * @return
     */
    public boolean isStoreMealPortAllSweep(int merchantId, long storeId, long portId, boolean enableSlave) {
        List<Long> portIds = new ArrayList<Long>();
        portIds.add(portId);
        return isStoreMealPortsAllSweep(merchantId, storeId, portIds, enableSlave);
    }
    //划菜相关end
}
