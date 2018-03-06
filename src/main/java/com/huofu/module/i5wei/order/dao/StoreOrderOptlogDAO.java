package com.huofu.module.i5wei.order.dao;

import com.google.common.collect.Lists;
import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderOptlog;

@Repository
public class StoreOrderOptlogDAO extends AbsQueryDAO<StoreOrderOptlog> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreOrderOptlog.class);
        return dalInfo.getRealTable(StoreOrderOptlog.class);
    }

    @Override
    public void create(StoreOrderOptlog storeOrderOptlog) {
        this.addDbRouteInfo(storeOrderOptlog.getMerchantId(), storeOrderOptlog.getStoreId());
        super.create(storeOrderOptlog);
    }

    @Override
    public List<StoreOrderOptlog> batchCreate(List<StoreOrderOptlog> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreOrderOptlog storeOrderOptlog) {
        this.addDbRouteInfo(storeOrderOptlog.getMerchantId(), storeOrderOptlog.getStoreId());
        super.update(storeOrderOptlog);
    }

    @Override
    public void delete(StoreOrderOptlog storeOrderOptlog) {
        this.addDbRouteInfo(storeOrderOptlog.getMerchantId(), storeOrderOptlog.getStoreId());
        super.delete(storeOrderOptlog);
    }

    public List<StoreOrderOptlog> getStoreOrderOptlogsByOrderId(int merchantId, long storeId, String orderId, boolean enableSlave) {
        if (orderId == null || orderId.isEmpty()) {
            return new ArrayList<StoreOrderOptlog>();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> values = new ArrayList<>();
        values.add(merchantId);
        values.add(storeId);
        values.add(orderId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StoreOrderOptlog> storeOrderOptlogs = this.query.list2(StoreOrderOptlog.class, "where merchant_id=? and store_id=? and order_id=? order by create_time asc ", values);
        if (storeOrderOptlogs == null || storeOrderOptlogs.isEmpty()) {
            return new ArrayList<StoreOrderOptlog>();
        }
        return storeOrderOptlogs;
    }

    public Map<String, List<StoreOrderOptlog>> getStoreOrderOptlogsInIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
        Map<String, List<StoreOrderOptlog>> orderOptLogMap = new HashMap<String, List<StoreOrderOptlog>>();
        if (orderIds == null || orderIds.isEmpty()) {
            return orderOptLogMap;
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> values = new ArrayList<>();
        values.add(merchantId);
        values.add(storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StoreOrderOptlog> storeOrderOptlogs = this.query.listInValues2(StoreOrderOptlog.class, "where merchant_id=? and store_id=?", "order_id", values, orderIds);
        if (storeOrderOptlogs == null || storeOrderOptlogs.isEmpty()) {
            return orderOptLogMap;
        }
        for (StoreOrderOptlog optlog : storeOrderOptlogs) {
            String orderId = optlog.getOrderId();
            List<StoreOrderOptlog> orderOptlogs = orderOptLogMap.get(orderId);
            if (orderOptlogs == null || orderOptlogs.isEmpty()) {
                orderOptlogs = new ArrayList<StoreOrderOptlog>();
                orderOptlogs.add(optlog);
                orderOptLogMap.put(orderId, orderOptlogs);
            } else {
                orderOptlogs.add(optlog);
            }
        }
        return orderOptLogMap;
    }

    public StoreOrderOptlog createOptlog(StoreOrder storeOrder, long staffId, int clientType, int optType, String remark) {
        StoreOrderOptlog storeOrderOptlog = new StoreOrderOptlog();
        storeOrderOptlog.setOrderId(storeOrder.getOrderId());
        storeOrderOptlog.setMerchantId(storeOrder.getMerchantId());
        storeOrderOptlog.setStoreId(storeOrder.getStoreId());
        storeOrderOptlog.setStaffId(staffId);
        storeOrderOptlog.setUserId(storeOrder.getUserId());
        storeOrderOptlog.setClientType(clientType);
        storeOrderOptlog.setOptType(optType);
        storeOrderOptlog.setRemark(remark);
        storeOrderOptlog.setCreateTime(System.currentTimeMillis());
        storeOrderOptlog.create();
        return storeOrderOptlog;
    }

    public List<Map<String, Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.getJdbcSupport().getMapList(sql, params);
    }

    public List<String> getOrderIdsByTime(int merchantId, long storeId, int orderOptlogType, long queryDateStart, long queryDateEnd) {

        String optLogTable = getRealName(merchantId, storeId);
        String sql = "select order_id as orderId FROM " + optLogTable +
                " where store_id=? and opt_type=? and create_time between ? and ? ";

        List<Object> args = Lists.newArrayList();
        args.add(storeId);
        args.add(orderOptlogType);
        args.add(queryDateStart);
        args.add(queryDateEnd);

        this.addDbRouteInfo(merchantId, storeId);
        List<String> orderIds = this.query.getJdbcSupport().getJdbcTemplate().query(sql, args.toArray(), new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int i) throws SQLException {
                return rs.getString("orderId");
            }
        });

        return orderIds;
    }
    
    /**
     * 获取某时间段内optType对应的订单（订单id）
     *
     * @param optTypeList
     * @param beginTime
     * @param endTime
     * @return
     */
    public List<StoreOrderOptlog> getOrderIdsByOptType(List<Integer> optTypeList, long beginTime, long endTime) {
        List<StoreOrderOptlog> storeOrderOptlogList = new ArrayList<StoreOrderOptlog>();

        StringBuffer whereOptType = new StringBuffer();
        for (int i = 0; i < optTypeList.size(); i++) {
            whereOptType.append(",?");
        }

        int dbSize = 64;
        int tableSize = 1024;
        String dbBaseName = "huofu_5wei";
        for (int i = 0; i < tableSize; i++) {
            String dbName = dbBaseName + "_" + (i % dbSize);
            String tname = "tb_store_order_optlog_" + (i % tableSize);

            DALInfo dalInfo = DALInfo.createForManual();
            dalInfo.setDsKey(dbName);
            DALStatus.setDalInfo(dalInfo);

            StringBuffer sql = new StringBuffer();
            sql.append("select order_id, merchant_id, store_id from ").append(tname)
               .append(" where opt_type in (").append(whereOptType.substring(1)).append(")")
               .append("   and create_time >= ?")
               .append("   and create_time < ?");

            List<Object> args = Lists.newArrayList();
            args.addAll(0, optTypeList);
            args.add(beginTime);
            args.add(endTime);

            List<StoreOrderOptlog> storeOrderOptlogs = this.query.getJdbcSupport().getJdbcTemplate().query(
                    sql.toString(), args.toArray(), new RowMapper<StoreOrderOptlog>() {
                @Override
                public StoreOrderOptlog mapRow(ResultSet rs, int i) throws SQLException {
                    StoreOrderOptlog storeOrderOptlog = new StoreOrderOptlog();
                    storeOrderOptlog.setOrderId(rs.getString("order_id"));
                    storeOrderOptlog.setMerchantId(rs.getInt("merchant_id"));
                    storeOrderOptlog.setStoreId(rs.getLong("store_id"));

                    return storeOrderOptlog;
                }
            });

            storeOrderOptlogList.addAll(storeOrderOptlogs);
        }

        return storeOrderOptlogList;
    }
}
