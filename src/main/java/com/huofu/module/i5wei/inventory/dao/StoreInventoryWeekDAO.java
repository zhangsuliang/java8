package com.huofu.module.i5wei.inventory.dao;

import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofucore.facade.idmaker.IdMakerFacade;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DateUtil;
import huofuhelper.util.thrift.ThriftClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryWeek;
import com.huofu.module.i5wei.menu.entity.StoreProduct;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreInventoryWeekDAO extends AbsQueryDAO<StoreInventoryWeek> {

    @ThriftClient
    private IdMakerFacade.Iface idMakerFacadeIface;

    public long nextId() {
        try {
            return this.idMakerFacadeIface.getNextId("store_inventory_week");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
    
    @Override
    public void create(StoreInventoryWeek storeInventoryWeek) {
        this.addDbRouteInfo(storeInventoryWeek.getMerchantId(), storeInventoryWeek.getStoreId());
        storeInventoryWeek.setInvWeekId(this.nextId());
        super.create(storeInventoryWeek);
    }

    @Override
    public List<StoreInventoryWeek> batchCreate(List<StoreInventoryWeek> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }
    
    @Override
    public void replace(StoreInventoryWeek storeInventoryWeek) {
    	storeInventoryWeek.setInvWeekId(this.nextId());
        this.addDbRouteInfo(storeInventoryWeek.getMerchantId(), storeInventoryWeek.getStoreId());
        this.query.replace(storeInventoryWeek);
    }

    @Override
    public void update(StoreInventoryWeek storeInventoryWeek) {
        this.addDbRouteInfo(storeInventoryWeek.getMerchantId(), storeInventoryWeek.getStoreId());
        super.update(storeInventoryWeek);
    }

    @Override
    public void delete(StoreInventoryWeek storeInventoryWeek) {
        this.addDbRouteInfo(storeInventoryWeek.getMerchantId(), storeInventoryWeek.getStoreId());
        super.delete(storeInventoryWeek);
    }

    public StoreInventoryWeek getStoreInventoryWeekByProductTime(int merchantId, long storeId, long productId, int weekDay, long timeBucketId, long datetime) {
        long instanceDateTime = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (instanceDateTime == datetime) {
            datetime = System.currentTimeMillis();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreInventoryWeek> list = this.query.list(StoreInventoryWeek.class,
                "where merchant_id=? and store_id=? and product_id=? and week_day=? and time_bucket_id=? and begin_time<=? and end_time>=? order by update_time desc",
                new Object[]{merchantId, storeId, productId, weekDay, timeBucketId, datetime, datetime});
		if (list == null || list.isEmpty()) {
			return null;
		}
        return list.get(0);
    }
    
    public List<StoreInventoryWeek> getStoreInventoryWeekByProductTime(int merchantId, long storeId, List<StoreProduct> storeProducts, int weekDay, long timeBucketId, long datetime, boolean enableSlave) {
        long instanceDateTime = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (instanceDateTime == datetime) {
            datetime = System.currentTimeMillis();
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
		List<StoreInventoryWeek> resultList = new ArrayList<StoreInventoryWeek>();
		List<StoreInventoryWeek> daylist = this._getStoreInventoryWeekByProductTime(merchantId, storeId, dayProductIds, weekDay, 0, instanceDateTime, enableSlave);
		resultList.addAll(daylist);
		if (timeBucketId > 0) {
			List<StoreInventoryWeek> otherlist = this._getStoreInventoryWeekByProductTime(merchantId, storeId, otherProductIds, weekDay, timeBucketId, instanceDateTime, enableSlave);
			resultList.addAll(otherlist);
		}
		return resultList;
    }
    
    private List<StoreInventoryWeek> _getStoreInventoryWeekByProductTime(int merchantId, long storeId, List<Long> productIds, int weekDay, long timeBucketId, long datetime, boolean enableSlave) {
		if (storeId == 0 || weekDay == 0 || productIds == null || productIds.isEmpty()) {
			return new ArrayList<StoreInventoryWeek>();
		}
        long instanceDateTime = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (instanceDateTime == datetime) {
            datetime = System.currentTimeMillis();
        }
        List<Object> params = new ArrayList<Object>();
        params.add(merchantId);
        params.add(storeId);
        params.add(weekDay);
        params.add(timeBucketId);
        params.add(datetime);
        params.add(datetime);
        if (enableSlave) {
        	DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreInventoryWeek.class,
                "where merchant_id=? and store_id=? and week_day=? and time_bucket_id =? and begin_time<=? and end_time>=?", "product_id", params, productIds);
    }

    public Map<Long, StoreInventoryWeek> getStoreInventoryWeekMapByProductTime(int merchantId, long storeId, List<StoreProduct> storeProducts, int weekDay, long timeBucketId, long datetime, boolean enableSlave) {
        Map<Long, StoreInventoryWeek> inventoryWeekMap = new HashMap<>();
        List<StoreInventoryWeek> list = this.getStoreInventoryWeekByProductTime(merchantId, storeId, storeProducts, weekDay, timeBucketId, datetime, enableSlave);
        if (list == null || list.isEmpty()) {
            return inventoryWeekMap;
        }
        else {
            for (StoreInventoryWeek storeInventoryWeek : list) {
                inventoryWeekMap.put(storeInventoryWeek.getProductId(), storeInventoryWeek);
            }
            return inventoryWeekMap;
        }
    }

    public List<StoreInventoryWeek> getStoreInventoryWeekByTime(int merchantId, long storeId, StoreProduct storeProduct, long datetime, boolean enableSlave) {
		if (merchantId == 0 || storeId == 0 || storeProduct == null || datetime == 0) {
			return new ArrayList<StoreInventoryWeek>();
		}
        long instanceDateTime = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (instanceDateTime == datetime) {
            datetime = System.currentTimeMillis();
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" where merchant_id=? and store_id=? and product_id=? and begin_time<=? and end_time>=? ");
        if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()) {
            sql.append(" and time_bucket_id=0 ");
        } else if(storeProduct.getInvType() == ProductInvTypeEnum.WEEK.getValue()){
            sql.append(" and time_bucket_id>0 ");
        } else {
            return new ArrayList<StoreInventoryWeek>();
        }
        if (enableSlave) {
        	DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreInventoryWeek.class, sql.toString(),
                new Object[]{merchantId, storeId, storeProduct.getProductId(), datetime, datetime});
    }
    
    public Map<String, StoreInventoryWeek> getStoreInventoryWeekMapByTime(int merchantId, long storeId, StoreProduct storeProduct, long datetime, boolean enableSlave) {
        Map<String, StoreInventoryWeek> map = new HashMap<>();
        List<StoreInventoryWeek> list = this.getStoreInventoryWeekByTime(merchantId, storeId, storeProduct, datetime, enableSlave);
        if (list == null) {
            return map;
        }
        
        for (StoreInventoryWeek storeInventoryWeek : list) {
        	long timeBucketId = storeInventoryWeek.getTimeBucketId();
            if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()) {
    			timeBucketId = 0;
    		}
        	String key = timeBucketId + "_" + storeInventoryWeek.getWeekDay();
        	map.put(key, storeInventoryWeek);
        }
        return map;
    }

}
