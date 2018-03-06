package com.huofu.module.i5wei.inventory.dao;


import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofucore.facade.idmaker.IdMakerFacade;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DateUtil;
import huofuhelper.util.ObjectUtil;
import huofuhelper.util.thrift.ThriftClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryDate;
import com.huofu.module.i5wei.menu.entity.StoreProduct;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreInventoryDateDAO extends AbsQueryDAO<StoreInventoryDate> {

    @ThriftClient
    private IdMakerFacade.Iface idMakerFacadeIface;

    public long nextId() {
        try {
            return this.idMakerFacadeIface.getNextId("store_inventory_date");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreInventoryDate.class);
        return dalInfo.getRealTable(StoreInventoryDate.class);
    }

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    public void create(StoreInventoryDate storeInventoryDate) {
        storeInventoryDate.setInvDateId(this.nextId());
        this.addDbRouteInfo(storeInventoryDate.getMerchantId(), storeInventoryDate.getStoreId());
        super.create(storeInventoryDate);
    }
    
    public void replace(StoreInventoryDate storeInventoryDate) {
        storeInventoryDate.setInvDateId(this.nextId());
        this.addDbRouteInfo(storeInventoryDate.getMerchantId(), storeInventoryDate.getStoreId());
        this.query.replace(storeInventoryDate);
    }

    @Override
    public void update(StoreInventoryDate storeInventoryDate) {
        this.addDbRouteInfo(storeInventoryDate.getMerchantId(), storeInventoryDate.getStoreId());
        super.update(storeInventoryDate);
    }
    
    @Override
    public void update(StoreInventoryDate storeInventoryDate, StoreInventoryDate snapshot) {
        this.addDbRouteInfo(storeInventoryDate.getMerchantId(), storeInventoryDate.getStoreId());
        super.update(storeInventoryDate, snapshot);
    }
    
    @Override
    public void delete(StoreInventoryDate storeInventoryDate) {
        this.addDbRouteInfo(storeInventoryDate.getMerchantId(), storeInventoryDate.getStoreId());
        super.delete(storeInventoryDate);
    }

    /**
     * 周期导致指定日期库存联动修改
     * @param merchantId
     * @param storeId
     * @param timeBucketId
     * @param productId
     * @param amount
     * @param effectTime
     * @return
     */
    public int updateStoreInventoryDateByWeek(int merchantId, long storeId, long timeBucketId, long productId, double amount, long effectTime) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreInventoryDate.class);
        String tName = dalInfo.getRealTable(StoreInventoryDate.class);
        effectTime = DateUtil.getBeginTime(effectTime, null);
        JdbcTemplate jdbcTemplate = this.query.getJdbcSupport().getJdbcTemplate();
        String sql = "update " + tName + " set amount_plan=?, amount=?, update_time=? where merchant_id=? and store_id=? and time_bucket_id=? and product_id=? and modified in (0,1) and select_date>? ";
        Object[] args = new Object[]{amount,amount,System.currentTimeMillis(), merchantId, storeId, timeBucketId, productId, effectTime};
        int num = jdbcTemplate.update(sql, args);
        return num;
    }

    /**
     * 查询指定时间库存
     * 注意：周天库存和周营业时段库存同时被查询到，使用时需要过滤
     * @param merchantId
     * @param storeId
     * @param timeBucketId
     * @param selectDate
     * @return
     */
    public List<StoreInventoryDate> getStoreInventoryDateBySelectDate(int merchantId, long storeId, long timeBucketId, long selectDate, boolean enableSlave) {
    	if (enableSlave) {
        	DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        selectDate = DateUtil.getBeginTime(selectDate, null);
        return this.query.list(StoreInventoryDate.class,
                "where merchant_id=? and store_id=? and time_bucket_id in (?,0) and select_date=? ",
                new Object[]{merchantId, storeId, timeBucketId, selectDate});
    }
    
    /**
     * 查询库存总预定
     * @param merchantId
     * @param storeId
     * @param storeProduct
     * @return
     */
    public double getStoreInventoryProductOrderAmount(int merchantId, long storeId, StoreProduct storeProduct, boolean enableSlave) {
		if (storeProduct == null) {
			return 0D;
		}
        long selectDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT sum(amount_order) as amount_order ");
		sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
		sql.append(" where merchant_id=? and store_id=? and product_id=? and select_date>=? ");
		if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()) {
			sql.append(" and time_bucket_id=0 ");
		} else {
			sql.append(" and time_bucket_id>0 ");
		}
		params.add(merchantId);
		params.add(storeId);
		params.add(storeProduct.getProductId());
		params.add(selectDate);
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
		if (list == null || list.isEmpty()) {
			return 0;
		}
		double amountOrder = ObjectUtil.getDouble(list.get(0),"amount_order");
		return amountOrder;
    }
    
    /**
     * 查询库存总预定
     * @param merchantId
     * @param storeId
     * @param storeProducts
     * @return
     */
    public Map<Long,Double> getStoreInventoryProductOrderAmount(int merchantId, long storeId, List<StoreProduct> storeProducts, boolean enableSlave) {
		if (storeProducts == null || storeProducts.isEmpty()) {
			return new HashMap<Long, Double>();
		}
		Map<Long,Double> dataMap = new HashMap<Long, Double>();
		List<Long> dayProductIds = new ArrayList<Long>();
		List<Long> otherProductIds = new ArrayList<Long>();
		for (StoreProduct product : storeProducts) {
			if (product.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()){
				dayProductIds.add(product.getProductId());
			}else{
				otherProductIds.add(product.getProductId());
			}
		}
		Map<Long,Double> dayDataMap = this._getStoreInventoryProductOrderAmount(merchantId, storeId, dayProductIds, 0, enableSlave);
		Map<Long,Double> otherDataMap = this._getStoreInventoryProductOrderAmount(merchantId, storeId, otherProductIds, 1, enableSlave);
		dataMap.putAll(dayDataMap);
		dataMap.putAll(otherDataMap);
		return dataMap;
    }
    
    /**
     * 查询库存总预定
     */
    private Map<Long,Double> _getStoreInventoryProductOrderAmount(int merchantId, long storeId, List<Long> productIds, int timeType, boolean enableSlave) {
		if (storeId == 0 || productIds == null || productIds.isEmpty()) {
			return new HashMap<Long, Double>();
		}
        long selectDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT product_id, sum(amount_order) as amount_order ");
		sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
		sql.append(" where merchant_id=? and store_id=? and select_date>=? ");
		if (timeType == 0) {
			sql.append(" and time_bucket_id=0 ");
		} else {
			sql.append(" and time_bucket_id>0 ");
		}
		String productIdSql= Query.createInSql("product_id", productIds.size());
		sql.append(" and ").append(productIdSql);
		sql.append(" group by product_id ");
		params.add(merchantId);
		params.add(storeId);
		params.add(selectDate);
		params.addAll(productIds);
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
		List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
		if (list == null || list.isEmpty()) {
			return new HashMap<Long, Double>();
		}
		Map<Long,Double> dataMap = new HashMap<Long, Double>();
		for(Map<String, Object> map:list){
			long productId = ObjectUtil.getLong(map,"product_id");
			double amountOrder = ObjectUtil.getDouble(map,"amount_order");
			dataMap.put(productId, amountOrder);
		}
		return dataMap;
    }
    
    /**
     * 查询产品在指定时间库存
     * @param merchantId
     * @param storeId
     * @param timeBucketId 等于0则为全体库存
     * @param productId
     * @param selectDate
     * @return
     */
    public StoreInventoryDate getStoreInventoryDateByProductSelectDate(int merchantId, long storeId, long timeBucketId, long productId, long selectDate, boolean enableSlave) {
        selectDate = DateUtil.getBeginTime(selectDate, null);
        if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreInventoryDate.class,
                "where merchant_id=? and store_id=? and time_bucket_id=? and product_id=? and select_date=? ",
                new Object[]{merchantId, storeId, timeBucketId, productId, selectDate});
    }

    /**
     * 查询产品在指定时间库存
     */
    private List<StoreInventoryDate> _getStoreInventoryDateByProductSelectDate(int merchantId, long storeId, long timeBucketId, List<Long> productIds, long selectDate, boolean enableSlave) {
    	List<StoreInventoryDate> resultList = new ArrayList<StoreInventoryDate>();
        if (merchantId == 0 || storeId == 0 || selectDate == 0 || productIds == null || productIds.isEmpty()) {
			return resultList;
		}
        
        selectDate = DateUtil.getBeginTime(selectDate, null);
        List<Object> params = new ArrayList<>();
		StringBuilder sql = new StringBuilder();
		sql.append(" where merchant_id=? and store_id=? and select_date=? and time_bucket_id=?  ");
		params.add(merchantId);
		params.add(storeId);
		params.add(selectDate);
		params.add(timeBucketId);
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
		resultList = this.query.listInValues2(StoreInventoryDate.class, sql.toString(), "product_id", params, productIds);
        return resultList;
    }
    
    /**
     * 查询产品在指定时间库存
     * @param merchantId
     * @param storeId
     * @param timeBucketId
     * @param storeProducts
     * @param selectDate
     * @return
     */
    public List<StoreInventoryDate> getStoreInventoryDateByProductSelectDate(int merchantId, long storeId, long timeBucketId, List<StoreProduct> storeProducts, long selectDate, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        selectDate = DateUtil.getBeginTime(selectDate, null);
        List<StoreInventoryDate> resultList = new ArrayList<StoreInventoryDate>();
        if (merchantId == 0 || storeId == 0 || selectDate == 0 || storeProducts == null || storeProducts.isEmpty()) {
			return resultList;
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
		List<StoreInventoryDate> dayProductInvs = this._getStoreInventoryDateByProductSelectDate(merchantId, storeId, 0, dayProductIds, selectDate, enableSlave);
		resultList.addAll(dayProductInvs);
		if (timeBucketId > 0) {
			List<StoreInventoryDate> otherProductInvs = this._getStoreInventoryDateByProductSelectDate(merchantId, storeId, timeBucketId, otherProductIds, selectDate, enableSlave);
			resultList.addAll(otherProductInvs);
		}
		return resultList;
    }
    
    /**
     * 查询产品在指定时间库存
     * @param merchantId
     * @param storeId
     * @param timeBucketId
     * @param storeProducts
     * @param selectDate
     * @return
     */
    public Map<Long, StoreInventoryDate> getStoreInventoryDateMapByProductSelectDate(int merchantId, long storeId, long timeBucketId, List<StoreProduct> storeProducts, long selectDate, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        Map<Long, StoreInventoryDate> inventoryDateMap = new HashMap<>();
        List<StoreInventoryDate> list = this.getStoreInventoryDateByProductSelectDate(merchantId, storeId, timeBucketId, storeProducts, selectDate, enableSlave);
        if (list == null || list.isEmpty()) {
            return inventoryDateMap;
        }
        else {
            for (StoreInventoryDate storeInventoryDate : list) {
                inventoryDateMap.put(storeInventoryDate.getProductId(), storeInventoryDate);
            }
            return inventoryDateMap;
        }
    }
    
   /**
    * 更新库存估清状态
    * @param merchantId
    * @param storeId
    * @param timeBucketId
    * @param selectDate
    * @param productIds
    */
    public int updateStoreInventoryDateNothingness(int merchantId, long storeId, long selectDate, long timeBucketId, List<Long> productIds, boolean nothingness) {
		if (storeId <= 0 || selectDate <= 0 || timeBucketId <= 0 || productIds == null || productIds.isEmpty()) {
			return 0;
		}
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreInventoryDate.class);
        String tName = dalInfo.getRealTable(StoreInventoryDate.class);
        selectDate = DateUtil.getBeginTime(selectDate, null);
        JdbcTemplate jdbcTemplate = this.query.getJdbcSupport().getJdbcTemplate();
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(tName).append(" set nothingness=?, update_time=? where store_id=? and select_date=? ");
        params.add(nothingness);
		params.add(System.currentTimeMillis());
		params.add(storeId);
		params.add(selectDate);
		sql.append(" and time_bucket_id=? ");
		params.add(timeBucketId);
		sql.append(" and ").append(Query.createInSql("product_id", productIds.size()));
        params.addAll(productIds);
        int num = jdbcTemplate.update(sql.toString(), params.toArray());
        return num;
    }

}
