package com.huofu.module.i5wei.order.dao;

import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;

/**
 * Auto created by i5weitools
 *
 * @author kaichen
 */
@Repository
public class StoreOrderItemDAO extends AbsQueryDAO<StoreOrderItem> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreOrderItem storeOrderItem) {
        this.addDbRouteInfo(storeOrderItem.getMerchantId(), storeOrderItem.getStoreId());
        super.create(storeOrderItem);
    }

    @Override
    public List<StoreOrderItem> batchCreate(List<StoreOrderItem> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreOrderItem storeOrderItem) {
        this.addDbRouteInfo(storeOrderItem.getMerchantId(), storeOrderItem.getStoreId());
        super.update(storeOrderItem);
    }

    @Override
    public void delete(StoreOrderItem storeOrderItem) {
        this.addDbRouteInfo(storeOrderItem.getMerchantId(), storeOrderItem.getStoreId());
        super.delete(storeOrderItem);
    }

    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreOrderItem.class);
        return dalInfo.getRealTable(StoreOrderItem.class);
    }

    public void deleteByOrderId(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        query.delete(StoreOrderItem.class, "where store_id=? and order_id=? ", new Object[]{storeId, orderId});
    }

    /**
     * 根据订单id查询订单包含的收费项目
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     */
    public List<StoreOrderItem> getStoreOrderItemByOrderId(int merchantId, long storeId, String orderId, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreOrderItem.class, "where store_id=? and order_id=? order by tid asc ",  new Object[]{storeId, orderId});
    }
    
    public List<StoreOrderItem> getStoreOrderItemById(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<Object>();
        params.add(storeId);
        return this.query.listInValues2(StoreOrderItem.class, "where store_id=?", "order_id", params, orderIds);
    }
    
    public List<StoreOrderItem> getStoreOrderItemByOrderIdAndChargeItemId(int merchantId, long storeId, long chargeItemId, List<String> orderIds, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<Object>();
        params.add(storeId);
        params.add(chargeItemId);
        return this.query.listInValues2(StoreOrderItem.class, "where store_id=? and charge_item_id=?", "order_id", params, orderIds);
    }

    public int updatePackagedStatus(int merchantId, long storeId, String orderId, boolean isPackaged) {
        this.addDbRouteInfo(merchantId, storeId);
        String tName = this.getRealName(merchantId, storeId);
        JdbcTemplate jdbcTemplate = this.query.getJdbcSupport().getJdbcTemplate();
        String packedAmount = "";
        if (isPackaged) {
            packedAmount = "packed_amount=amount";
        } else {
            packedAmount = "packed_amount=0";
        }
        String sql = "update " + tName + " set " + packedAmount + ", update_time=? where store_id=? and order_id=? ";
        Object[] args = new Object[]{System.currentTimeMillis(), storeId, orderId};
        int num = jdbcTemplate.update(sql, args);
        return num;
    }

    public List<Map<String, Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.getJdbcSupport().getMapList(sql, params);
    }

    public Map<String, List<StoreOrderItem>> getMapsInOrderIds
            (int merchantId, long storeId, List<String> orderIds) {
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreOrderItem> storeOrderItems = this.query.listInValues2
                (StoreOrderItem.class, "where store_id=?", "order_id", "order by" +
                        " tid asc", Lists.newArrayList(storeId), orderIds);
        Map<String, List<StoreOrderItem>> map = new HashMap<>();
        for (StoreOrderItem storeOrderItem : storeOrderItems) {
            List<StoreOrderItem> items = map.get(storeOrderItem.getOrderId());
            if (items == null) {
                items = new ArrayList<>();
                map.put(storeOrderItem.getOrderId(), items);
            }
            items.add(storeOrderItem);
        }
        return map;
    }

    /**
     * 获得5wei库中的订单项数量
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public Map<String, Object> getStoreOrderItemByRepastDate(int merchantId, long storeId, List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new HashMap<>();
        }
        //in语句
        String orderIdInSql = Query.createInSql("order_id", orderIds.size());

        String realName = getRealName(merchantId, storeId);
        String sql = "SELECT count(tid) as num FROM " + realName + " where " + orderIdInSql;

        List<Object> args = Lists.newArrayList();
        args.addAll(orderIds);
        return this.query.getJdbcSupport().getMap(sql, args.toArray());
    }

    /**
     * 订单集合获取订单项
     *
     * @param merchantId
     * @param storeId
     * @param orderIds
     */
    public List<StoreOrderItem> getStoreOrderItemByOrderIdList(int merchantId, long storeId, List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Lists.newArrayList();
        }
        //in语句
        String orderIdInSql = Query.createInSql("order_id", orderIds.size());
        String sql = " where " + orderIdInSql;
        List<Object> args = Lists.newArrayList();
        args.addAll(orderIds);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list2(StoreOrderItem.class, sql, args);
    }
    
    /**
     * 不严格的订单项目查询，主要为获取订单相关订单项目基本信息（价格、打包费、名称等...）
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @param chargeItemIds
     * @return
     */
    public Map<Long, StoreOrderItem> getStoreOrderItemMapByIds(int merchantId, long storeId, List<String> orderIds, List<Long> chargeItemIds) {
		Map<Long, StoreOrderItem> storeOrderItemMap = new HashMap<Long, StoreOrderItem>();
		if (orderIds == null || orderIds.isEmpty() || chargeItemIds == null || chargeItemIds.isEmpty()) {
			return storeOrderItemMap;
		}
        //order in语句
        String orderIdInSql = Query.createInSql("order_id", orderIds.size());
        String sql = " where " + orderIdInSql;
        List<Object> params = Lists.newArrayList();
        params.addAll(orderIds);
        //chargeItem in语句
        String chargeItemInSql = Query.createInSql("charge_item_id", chargeItemIds.size());
        sql = sql + " and " + chargeItemInSql;
        params.addAll(chargeItemIds);
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreOrderItem> storeOrderItems = this.query.list2(StoreOrderItem.class, sql, params);
		if (storeOrderItems == null || storeOrderItems.isEmpty()) {
			return storeOrderItemMap;
		}
		for (StoreOrderItem storeOrderItem : storeOrderItems) {
			long chargeItemId = storeOrderItem.getChargeItemId();
			StoreOrderItem item = storeOrderItemMap.get(chargeItemId);
			if (item != null) {
				if (item.getPrice() < storeOrderItem.getPrice()) {
					continue; //返回价格较小的
				}
			}
			storeOrderItemMap.put(chargeItemId, storeOrderItem);
		}
		return storeOrderItemMap;
	}
    
}
