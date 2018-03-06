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

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrderSubitem;

/**
 * Auto created by i5weitools
 *
 * @author kaichen
 */
@Repository
public class StoreOrderSubitemDAO extends AbsQueryDAO<StoreOrderSubitem> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreOrderSubitem storeOrderSubitem) {
        this.addDbRouteInfo(storeOrderSubitem.getMerchantId(), storeOrderSubitem.getStoreId());
        super.create(storeOrderSubitem);
    }

    @Override
    public List<StoreOrderSubitem> batchCreate(List<StoreOrderSubitem> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreOrderSubitem storeOrderSubitem) {
        this.addDbRouteInfo(storeOrderSubitem.getMerchantId(), storeOrderSubitem.getStoreId());
        super.update(storeOrderSubitem);
    }
    
    @Override
    public void delete(StoreOrderSubitem storeOrderSubitem) {
        this.addDbRouteInfo(storeOrderSubitem.getMerchantId(), storeOrderSubitem.getStoreId());
        super.delete(storeOrderSubitem);
    }
    
    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreOrderSubitem.class);
        return dalInfo.getRealTable(StoreOrderSubitem.class);
    }
    
    public void deleteByOrderId(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        query.delete(StoreOrderSubitem.class, "where store_id=? and order_id=?", new Object[]{storeId, orderId});
    }
    
    /**
     * 更新恢复库存数量
     *
     * @param merchantId
     * @param storeId
     * @param storeMealAutoPrintParams
     */
    public void batchUpdateInvQuitAmount(int merchantId, long storeId, List<StoreOrderSubitem> storeOrderSubitems) {
    	if (storeOrderSubitems == null || storeOrderSubitems.isEmpty()) {
			return;
		}
    	long currentTime = System.currentTimeMillis();
        List<Object[]> params = Lists.newArrayList();
		for (StoreOrderSubitem param : storeOrderSubitems) {
			params.add(new Object[] { param.getInvQuitAmount(), currentTime, param.getOrderId(), param.getChargeItemId(), param.getProductId()});
		}
        this.addDbRouteInfo(merchantId, storeId);
        this.query.batchUpdate(StoreOrderSubitem.class, "set inv_quit_amount=?, update_time=? where order_id=? and charge_item_id=? and product_id=? ", params);
    }

    public List<StoreOrderSubitem> getStoreOrderSubitemById(int merchantId, long storeId, String orderId, long chargeItemid) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreOrderSubitem.class, "where store_id=? and order_id=? and charge_item_id=? order by tid asc ",
                new Object[]{storeId, orderId, chargeItemid});
    }

    public Map<Long, List<StoreOrderSubitem>> getStoreOrderSubitemMapById(int merchantId, long storeId, String orderId, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreOrderSubitem> list = this.query.list(StoreOrderSubitem.class, "where store_id=? and order_id=?", new Object[]{storeId, orderId});
        Map<Long, List<StoreOrderSubitem>> map = new HashMap<Long, List<StoreOrderSubitem>>();
        if (list == null || list.isEmpty()) {
            return map;
        }
        for (StoreOrderSubitem subitem : list) {
            long chargeItemId = subitem.getChargeItemId();
            if (map.containsKey(chargeItemId)) {
                List<StoreOrderSubitem> sublist = map.get(chargeItemId);
                sublist.add(subitem);
                map.put(subitem.getChargeItemId(), sublist);
            }
            else {
                List<StoreOrderSubitem> sublist = new ArrayList<StoreOrderSubitem>();
                sublist.add(subitem);
                map.put(subitem.getChargeItemId(), sublist);
            }
        }
        return map;
    }

    public List<StoreOrderSubitem> getStoreOrderSubitemByOrderIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues(StoreOrderSubitem.class, " where store_id=? ", "order_id",
                new Object[]{storeId}, orderIds.toArray(new String[orderIds.size()]));
    }

    public List<StoreOrderSubitem> getStoreOrderSubitemProductByOrderIds(int merchantId, long storeId, long productId, List<String> orderIds) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues(StoreOrderSubitem.class, " where store_id=? and product_id=?", "order_id",
                new Object[]{storeId, productId}, orderIds.toArray(new String[orderIds.size()]));
    }
    
    public List<Map<String,Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.getJdbcSupport().getMapList(sql, params);
    }


    public void updateInvQuitAmount (int merchantId, long storeId, long tid) {
    }

    /**
     * 得到5wei库中订单子项的数量
     * @param merchantId
     * @param storeId
     * @return
     */
    public Map<String, Object> getStoreOrderSubitemByRepastDate(int merchantId, long storeId, List<String> orderIds) {

        if (orderIds == null || orderIds.isEmpty()) {
            return new HashMap<>();
        }
        String realName = getRealName(merchantId, storeId);
        //in语句
        String orderIdInSql = Query.createInSql("order_id", orderIds.size());

        String sql = "SELECT count(tid) as num FROM " + realName + " where " + orderIdInSql;

        List<Object> args = Lists.newArrayList();
        args.addAll(orderIds);
        return this.query.getJdbcSupport().getMap(sql, args.toArray());
    }

    /**
     * 统统订单Id集合 得到全部的订单子项
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @return
     */
    public List<StoreOrderSubitem> getStoreOrderSubitemByOrderIdList(int merchantId, long storeId, List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Lists.newArrayList();
        }
        //in语句
        String orderIdInSql = Query.createInSql("order_id", orderIds.size());
        String sql = " where " + orderIdInSql;
        List<Object> args = Lists.newArrayList();
        args.addAll(orderIds);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list2(StoreOrderSubitem.class, sql, args);
    }
    
    /**
     * 查询订单Id指定收费项目的订单子项
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param chargeItemIds
     * @return
     */
    public List<StoreOrderSubitem> getStoreOrderSubitemByOrderChargeItemIds(int merchantId, long storeId, String orderId, List<Long> chargeItemIds) {
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return Lists.newArrayList();
        }
        String sql = " where store_id=? and order_id=? and " + Query.createInSql("charge_item_id", chargeItemIds.size());
        List<Object> args = Lists.newArrayList();
        args.add(storeId);
        args.add(orderId);
        args.addAll(chargeItemIds);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list2(StoreOrderSubitem.class, sql, args);
    }
    
    /**
     * 统计订单上的收费项包含的收费子项的数量
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @param enableSlave
     * @return
     */
    public Map<String,Integer> countStoreOrderSubitem(int merchantId, long storeId, List<String> orderIds, boolean enableSlave){
        Map<String, Integer> result = new HashMap<String, Integer>();
        this.addDbRouteInfo(merchantId, storeId);
        if(orderIds == null || orderIds.isEmpty()){
            return result;
        }
        StringBuffer sql = new StringBuffer();
        sql.append("select order_id, charge_item_id, count(product_id) product_num from ").append(this.getRealName(merchantId, storeId));
        sql.append(" where ").append(Query.createInSql("order_id", orderIds.size()));
        sql.append(" group by order_id, charge_item_id");
        List<Map<String, Object>> resultMapList = this.getResultMapList(merchantId, storeId, sql.toString(), orderIds.toArray(), enableSlave);
        for (Map<String, Object> map : resultMapList) {
            String orderId = ObjectUtil.getString(map, "order_id");
            long chargeItemId = ObjectUtil.getLong(map, "charge_item_id");
            int productNum = ObjectUtil.getInt(map, "product_num");
            result.put(orderId + "_" + chargeItemId, productNum);
        }
        return result;
    }
    
}
