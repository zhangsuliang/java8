package com.huofu.module.i5wei.order.dao;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.service.StoreOrderInvTypeEnum;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteHelpler;
import halo.query.Query;
import halo.query.dal.DALContext;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteBaseDTO;
import huofucore.facade.idmaker.IdMakerFacade;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.module.base.IdGenerater;
import huofuhelper.util.*;
import huofuhelper.util.thrift.ThriftClient;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺订单DAO
 *
 * @author kaichen
 */
@SuppressWarnings("all")
@Repository
public class StoreOrderDAO extends AbsQueryDAO<StoreOrder> {

    @ThriftClient
    private IdMakerFacade.Iface idMakerFacadeIface;

    @Autowired
    private StoreOrderSubitemDAO storeOrderSubitemDao;

    @Autowired
    private StorePickupSiteDAO storePickupSiteDAO;

    @Autowired
    private StorePickupSiteHelpler storePickupSiteHelpler;

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    private DALContext buildDalContext(int merchantId, long storeId, boolean enableSlave) {
        DALContext dalContext = DALContext.create();
        dalContext.setEnableSlave(enableSlave);
        dalContext.addParam("merchant_id", merchantId);
        dalContext.addParam("store_id", storeId);
        return dalContext;
    }

    public String nextId(long storeId) {
        try {
            long orderSeq = this.idMakerFacadeIface.getNextId("store_order_5wei");
            return IdGenerater.get(storeId, orderSeq);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreOrder.class);
        return dalInfo.getRealTable(StoreOrder.class);
    }

    public void create(StoreOrder storeOrder) {
        this.addDbRouteInfo(storeOrder.getMerchantId(), storeOrder.getStoreId());
        storeOrder.setOrderId(this.nextId(storeOrder.getStoreId()));
        super.create(storeOrder);
    }

    @Override
    public List<StoreOrder> batchCreate(List<StoreOrder> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    public void batchUpdate(List<StoreOrder> list) {
        if (list.isEmpty()) {
            return;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        List<Object[]> param = new ArrayList<>();
        for (StoreOrder storeOrder : list) {
            param.add(new Object[]{storeOrder.getSendType(), storeOrder.getSendTime(), storeOrder.getOrderId()});
        }
        this.query.batchUpdate(StoreOrder.class, "set send_type = ?,send_time = ? where order_id = ?", param);
    }

    @Override
    public void update(StoreOrder storeOrder) {
        this.addDbRouteInfo(storeOrder.getMerchantId(), storeOrder.getStoreId());
        super.update(storeOrder);
    }

    @Override
    public void update(StoreOrder storeOrder, StoreOrder snapshot) {
        this.addDbRouteInfo(storeOrder.getMerchantId(), storeOrder.getStoreId());
        super.update(storeOrder, snapshot);
    }

    @Override
    public void delete(StoreOrder storeOrder) {
        this.addDbRouteInfo(storeOrder.getMerchantId(), storeOrder.getStoreId());
        super.delete(storeOrder);
    }

    /**
     * 根据orderId查询订单
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     */
    public StoreOrder getById(int merchantId, long storeId, String orderId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        StoreOrder storeOrder;
        if (forUpdate) {
            storeOrder = this.query.objByIdForUpdate(StoreOrder.class, orderId);
        } else {
            storeOrder = this.query.objById(StoreOrder.class, orderId);
        }
        if (storeOrder != null) {
            if (forSnapshot) {
                storeOrder.snapshot();
            }
        }
        return storeOrder;
    }

    public StoreOrderDelivery getDeliveryById(int merchant, long storeId, String orderId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchant, storeId);
        StoreOrderDelivery storeOrderDelivery;
        if (forUpdate) {
            storeOrderDelivery = this.query.objByIdForUpdate(StoreOrderDelivery.class, orderId);
        } else {
            storeOrderDelivery = this.query.objById(StoreOrderDelivery.class, orderId);
        }
        if (storeOrderDelivery != null) {
            if (forSnapshot) {
                storeOrderDelivery.snapshot();
            }
        }
        return storeOrderDelivery;
    }

    public List<StoreOrder> getStoreOrdersInIdsForUpdate(int merchantId, long storeId, List<String> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuilder sql = new StringBuilder();
        sql.append("where merchant_id=? and store_id=?");
        List<Object> values = new ArrayList<>();
        values.add(merchantId);
        values.add(storeId);
        String orderInSql = Query.createInSql("order_id", orderIds.size());
        values.addAll(orderIds);
        sql.append(" and ").append(orderInSql);
        sql.append(" order by create_time asc for update");
        return this.query.list2(StoreOrder.class, sql.toString(), values);
    }

    /**
     * 根据orderId查询订单
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     */
    public StoreOrder getStoreOrderById(int merchantId, long storeId, String orderId, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.objById(StoreOrder.class, orderId);
    }

    public List<StoreOrder> getStoreOrdersInIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> values = new ArrayList<>();
        values.add(merchantId);
        values.add(storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.listInValues2(StoreOrder.class, "where merchant_id=? and store_id=?", "order_id", values, orderIds);
    }

    public Map<String, StoreOrder> getStoreOrderMapInIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
        List<StoreOrder> orders = this.getStoreOrdersInIds(merchantId, storeId, orderIds, enableSlave);
        Map<String, StoreOrder> orderMap = new HashMap<String, StoreOrder>();
        if (orders == null || orders.isEmpty()) {
            return orderMap;
        }
        for (StoreOrder order : orders) {
            orderMap.put(order.getOrderId(), order);
        }
        return orderMap;
    }

    /**
     * 根据parentOrderId查询订单
     *
     * @param merchantId
     * @param storeId
     * @param parentOrderId
     * @return
     */
    public List<StoreOrder> getStoreOrderByParentOrderId(int merchantId, long storeId, long tableRecordId, String parentOrderId, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreOrder.class, "where store_id=? and table_record_id=? and parent_order_id=?", new Object[]{storeId, tableRecordId, parentOrderId});
    }

    /**
     * 查询userId今日（已支付、未退款）订单
     *
     * @param merchantId
     * @param storeId
     * @param userId
     * @return
     */
    public List<StoreOrder> getStoreOrdersPlaceByRepastDate(int merchantId, long storeId, long userId, long repastDate, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        repastDate = DateUtil.getBeginTime(repastDate, null);
        return this.query.list(StoreOrder.class, "where merchant_id=? and store_id=? and user_id=? and pay_status=? and refund_status in (?,?) and repast_date=? ", new Object[]{merchantId, storeId, userId, StoreOrderPayStatusEnum.FINISH.getValue(), StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue(), repastDate});
    }

    /**
     * 查询userId今日享受终端优惠的订单（已支付、未退款）订单
     *
     * @param merchantId
     * @param storeId
     * @param userId
     * @return
     */
    public List<StoreOrder> getStoreOrdersClientCouponByRepastDate(int merchantId, long storeId, long userId, long repastDate, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        repastDate = DateUtil.getBeginTime(repastDate, null);
        return this.query.list(StoreOrder.class, "where merchant_id=? and store_id=? and user_id=? and pay_status=? and refund_status in (?,?) and repast_date=? and user_client_coupon>0 ", new Object[]{merchantId, storeId, userId, StoreOrderPayStatusEnum.FINISH.getValue(), StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue(), repastDate});
    }

    /**
     * 根据订单ID列表查询订单
     *
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @return
     */
    public List<StoreOrder> getStoreOrdersById(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
        List<Object> params = new ArrayList<Object>();
        params.add(merchantId);
        params.add(storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreOrder.class, "where merchant_id=? and store_id=?", "order_id", params, orderIds);
    }

    /**
     * 查询店铺需要回滚的订单列表
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreOrder> getStoreOrdersRollback(int merchantId, long storeId, boolean enableSlave) {
        String sql = "where merchant_id=? and store_id=? and repast_date<? and repast_date>=? and pay_status=? and refund_status in (?,?) and user_id>0 and take_serial_number=0 and (favorable_price+delivery_fee)>0";
        long now = System.currentTimeMillis();
        MutableDateTime mdt = new MutableDateTime(now);
        mdt.addDays(-2);
        long today = DateUtil.getBeginTime(now, null);
        long _2day = DateUtil.getBeginTime(mdt.getMillis(), null);
        List<Object> params = new ArrayList<Object>();
        params.add(merchantId);
        params.add(storeId);
        params.add(today);
        params.add(_2day);
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        params.add(StoreOrderRefundStatusEnum.NOT.getValue());
        params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list2(StoreOrder.class, sql, params);
    }

    /**
     * 根据取餐码查询订单
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param takeCode
     * @return
     */
    public StoreOrder getStoreOrderByTakeCode(int merchantId, long storeId, long repastDate, String takeCode, boolean enableSlave) {
        if (repastDate == 0 || takeCode == null || takeCode.isEmpty()) {
            return null;
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreOrder.class, "where merchant_id=? and store_id=? and repast_date=? and take_code=? ", new Object[]{merchantId, storeId, repastDate, takeCode});
    }

    /**
     * 统计客户指定营业时段在某餐台的就餐数量
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param userId
     * @return
     */
    public int countDishStoreOrder(int merchantId, long storeId, long repastDate, long timeBucketId, long userId, boolean enableSlave) {
        if (storeId == 0 || repastDate == 0 || timeBucketId == 0 || userId == 0) {
            return 0;
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreOrder.class, "where merchant_id=? and store_id=? and repast_date=? and time_bucket_id=? and user_id=? and pay_status=? and take_mode in (?,?) and refund_status not in (?,?) ", new Object[]{merchantId, storeId, repastDate, timeBucketId, userId, StoreOrderPayStatusEnum.FINISH.getValue(), StoreOrderTakeModeEnum.DINE_IN.getValue(), StoreOrderTakeModeEnum.IN_AND_OUT.getValue(), StoreOrderRefundStatusEnum.USER_ALL.getValue(), StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue()});
    }

    public Map<Long, Double> countStoreOrderSubitemAmountMap(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> productIds, StoreOrderInvTypeEnum orderType, boolean enableSlave) {
        Map<Long, Double> amountMap = new HashMap<Long, Double>();
        if (merchantId == 0 || storeId == 0 || repastDate == 0 || productIds == null || productIds.isEmpty()) {
            return amountMap;
        }
        if (orderType == null || StoreOrderInvTypeEnum.UNKNOWN.equals(orderType)) {
            return amountMap;
        }
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT b.product_id, SUM(b.amount_order-b.inv_quit_amount) as amount_order FROM ").append(this.getRealName(merchantId, storeId)).append(" a ");
        sql.append(" LEFT JOIN ").append(storeOrderSubitemDao.getRealName(merchantId, storeId)).append(" b on a.order_id=b.order_id ");
        sql.append(" WHERE a.merchant_id=? and a.store_id=? and a.repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and a.time_bucket_id=? ");
            params.add(timeBucketId);
        }
        if (StoreOrderInvTypeEnum.BOOK_ORDER.equals(orderType)) {
            sql.append(" and a.refund_status in (?,?) and a.pay_status=? and a.takeup_status=? ");
            params.add(StoreOrderRefundStatusEnum.NOT.getValue());
            params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
            params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            params.add(StoreOrderTakeupStatusEnum.RETAIN.getValue());
        }
        if (StoreOrderInvTypeEnum.TAKED_CODE.equals(orderType)) {
            sql.append(" and a.take_serial_number>0 and a.takeup_status=? ");
            params.add(StoreOrderTakeupStatusEnum.DEDUCTED.getValue());
        }
        if (StoreOrderInvTypeEnum.TAKEUP_INVENTORY.equals(orderType)) {
            sql.append(" and ( (a.refund_status in (?,?) and a.pay_status=?) or a.take_serial_number>0 ) and a.takeup_status in (?,?) ");
            params.add(StoreOrderRefundStatusEnum.NOT.getValue());
            params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
            params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            params.add(StoreOrderTakeupStatusEnum.RETAIN.getValue());
            params.add(StoreOrderTakeupStatusEnum.DEDUCTED.getValue());
        }
        String productInSql = Query.createInSql("product_id", productIds.size());
        sql.append(" and ").append(productInSql);
        params.addAll(productIds);
        sql.append(" group by product_id ");
        List<Map<String, Object>> list = this.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return amountMap;
        }
        for (Map<String, Object> map : list) {
            long productId = ObjectUtil.getLong(map, "product_id");
            double amountOrder = ObjectUtil.getDouble(map, "amount_order");
            amountMap.put(productId, amountOrder);
        }
        return amountMap;
    }


    /**
     * 根据storeQueryDTO查询订单列表
     *
     * @param storeQueryDTO
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<StoreOrder> getStoreOrderByQueryDTO(StoreOrdersQueryParam storeQueryDTO, boolean enableSlave) {
        Map<String, Object> paramMap = this.getStoreOrderByQueryDTOParam(storeQueryDTO);
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }
        this.addDbRouteInfo(storeQueryDTO.getMerchantId(), storeQueryDTO.getStoreId());
        List<Object> params = (List<Object>) paramMap.get("whereParams");
        StringBuffer sql = (StringBuffer) paramMap.get("whereSql");
        int size = storeQueryDTO.getSize();
        int pageNo = storeQueryDTO.getPageNo();
        if (size > 0 && pageNo > 0) {
            int start = PageUtil.getBeginIndex(pageNo, size);
            sql.append(" limit ?, ? ");
            params.add(start);
            params.add(size);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    /**
     * 根据storeQueryDTO查询订单列表
     *
     * @param storeQueryDTO
     * @return
     */
    @SuppressWarnings("unchecked")
    public int countStoreOrderByQueryDTO(StoreOrdersQueryParam storeQueryDTO, boolean enableSlave) {
        Map<String, Object> paramMap = this.getStoreOrderByQueryDTOParam(storeQueryDTO);
        if (paramMap == null || paramMap.isEmpty()) {
            return 0;
        }
        this.addDbRouteInfo(storeQueryDTO.getMerchantId(), storeQueryDTO.getStoreId());
        List<Object> params = (List<Object>) paramMap.get("whereParams");
        StringBuffer sql = (StringBuffer) paramMap.get("whereSql");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.count(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    private Map<String, Object> getStoreOrderByQueryDTOParam(StoreOrdersQueryParam storeQueryDTO) {
        Map<String, Object> paramMap = new HashMap<>();
        int merchantId = storeQueryDTO.getMerchantId();
        long storeId = storeQueryDTO.getStoreId();
        StoreOrderQueryTypeEnum orderQueryType = storeQueryDTO.getOrderQueryType();
        if (merchantId == 0 || storeId == 0 || orderQueryType == null) {
            return paramMap;
        }
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("where merchant_id=? ");
        params.add(merchantId);
        if (storeId > 0) {
            sql.append(" and store_id=? ");
            params.add(storeId);
        }
        long repastDate = storeQueryDTO.getRepastDate();
        if (repastDate > 0) {
            if(storeQueryDTO.isSetClientType()) { //如果设置了终端来源
                if (storeQueryDTO.getClientType() == ClientTypeEnum.MINA.getValue()) {
                    sql.append(" and repast_date>? ");
                    params.add(repastDate);
                }else{
                    sql.append(" and repast_date=? ");
                    params.add(repastDate);
                }
            }else{
                sql.append(" and repast_date=? ");
                params.add(repastDate);
            }
        }
        long timeBucketId = storeQueryDTO.getTimeBucketId();
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        long userId = storeQueryDTO.getUserId();
        if (userId > 0) {
            sql.append(" and user_id=? ");
            params.add(userId);
        }
        int takeSerialNumber = storeQueryDTO.getTakeSerialNumber();
        if (takeSerialNumber > 0) {
            sql.append(" and (take_serial_number=? or site_number=?) ");
            params.add(takeSerialNumber);
            int siteNumber = takeSerialNumber;//桌牌号查询
            params.add(siteNumber);
        }
        String takeCode = storeQueryDTO.getTakeCode();
        if (takeCode != null && !takeCode.isEmpty()) {
            sql.append(" and take_code=? ");
            params.add(takeCode);
        }

        if(storeQueryDTO.isSetClientType()) { //如果设置了终端来源
            int clientType = storeQueryDTO.getClientType();
            if (clientType > 0) {
                sql.append(" and client_type=? ");
                params.add(clientType);
            }
        }

        //如果是小程序订单，得加入状态查询
        if(storeQueryDTO.isSetMinaOrderStatus()){
            StoreMinaOrderStatusTypeEnum minaOrderStatus = storeQueryDTO.getMinaOrderStatus();
            //已处理
            if(minaOrderStatus == StoreMinaOrderStatusTypeEnum.PROCESSED){
                sql.append(" and (trade_status <> 1 or refund_status=2 ) ");
            }else{//未处理
                sql.append(" and trade_status=1 and refund_status<> 2");
            }
        }

        if (StoreOrderQueryTypeEnum.MEAL_ORDER.equals(orderQueryType)) {
            sql.append(" and take_serial_number > 0 and back_order=? ");
            params.add(false);
            sql.append(" order by take_serial_number desc ");
        } else if (StoreOrderQueryTypeEnum.BOOK_ORDER.equals(orderQueryType)) {
	        sql.append(" and pay_status=? and refund_status in (?,?) and trade_status=? ");//edit by Jemon 去掉 and user_id>0 满足外卖订单
	        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            params.add(StoreOrderRefundStatusEnum.NOT.getValue());
            params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
            params.add(StoreOrderTradeStatusEnum.NOT.getValue());
            sql.append(" order by create_time desc ");
        } else if (StoreOrderQueryTypeEnum.BACK_ORDER.equals(orderQueryType)) {
            sql.append(" and pay_status=? and back_order=? ");
            params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            params.add(true);
            sql.append(" order by create_time desc ");
        } else if (StoreOrderQueryTypeEnum.DINE_IN_ORDER.equals(orderQueryType)) {
            //堂食订单
            sql.append(" and take_serial_number > 0 and take_mode in (?,?) and back_order=? and parent_order_id='' ");
            params.add(StoreOrderTakeModeEnum.DINE_IN.getValue());
            params.add(StoreOrderTakeModeEnum.IN_AND_OUT.getValue());
            params.add(false);
            sql.append(" order by create_time desc ");
        } else if (StoreOrderQueryTypeEnum.TAKE_OUT_ORDER.equals(orderQueryType)) {
            sql.append(" and take_serial_number > 0 and take_mode in (?,?) and back_order=? and table_record_id=0");
            params.add(StoreOrderTakeModeEnum.TAKE_OUT.getValue());
            params.add(StoreOrderTakeModeEnum.QUICK_TAKE.getValue());
            params.add(false);
            sql.append(" order by create_time desc ");
        } else if (StoreOrderQueryTypeEnum.SEND_OUT_ORDER.equals(orderQueryType)) {
            sql.append(" and take_serial_number > 0 and take_mode=? and back_order=? ");
            params.add(StoreOrderTakeModeEnum.SEND_OUT.getValue());
            params.add(false);
            sql.append(" order by create_time desc ");
        }
        paramMap.put("whereSql", sql);
        paramMap.put("whereParams", params);
        return paramMap;
    }

    /**
     * 根据storeQueryDTO查询订单列表
     *
     * @param storeQueryDTO
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<StoreOrder> getStoreOrderByQueryDTO(StoreOrdersQueryByStatusParam storeQueryDTO, boolean enableSlave) {
        Map<String, Object> paramMap = this.getStoreOrderByQueryDTOParam(storeQueryDTO);
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }
        this.addDbRouteInfo(storeQueryDTO.getMerchantId(), storeQueryDTO.getStoreId());
        List<Object> params = (List<Object>) paramMap.get("whereParams");
        StringBuffer sql = (StringBuffer) paramMap.get("whereSql");
        int size = storeQueryDTO.getSize();
        int pageNo = storeQueryDTO.getPageNo();
        if (size > 0 && pageNo > 0) {
            int start = PageUtil.getBeginIndex(pageNo, size);
            sql.append(" limit ?, ? ");
            params.add(start);
            params.add(size);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    /**
     * 根据storeQueryDTO查询订单列表
     *
     * @param storeQueryDTO
     * @return
     */
    @SuppressWarnings("unchecked")
    public int countStoreOrderByQueryDTO(StoreOrdersQueryByStatusParam storeQueryDTO, boolean enableSlave) {
        Map<String, Object> paramMap = this.getStoreOrderByQueryDTOParam(storeQueryDTO);
        if (paramMap == null || paramMap.isEmpty()) {
            return 0;
        }
        this.addDbRouteInfo(storeQueryDTO.getMerchantId(), storeQueryDTO.getStoreId());
        List<Object> params = (List<Object>) paramMap.get("whereParams");
        StringBuffer sql = (StringBuffer) paramMap.get("whereSql");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.count(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    private Map<String, Object> getStoreOrderByQueryDTOParam(StoreOrdersQueryByStatusParam storeQueryDTO) {
        Map<String, Object> paramMap = new HashMap<>();
        //是否正序输出数据
        boolean asc = storeQueryDTO.isAsc();
        int merchantId = storeQueryDTO.getMerchantId();
        long storeId = storeQueryDTO.getStoreId();
        if (merchantId == 0 || storeId == 0) {
            return paramMap;
        }
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("where merchant_id=? ");
        params.add(merchantId);
        if (storeId > 0) {
            sql.append(" and store_id=? ");
            params.add(storeId);
        }
        long repastDate = storeQueryDTO.getRepastDate();
        if (repastDate > 0) {
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        } else {
            if (storeQueryDTO.getOrderStatusType().equals(StoreOrderStatusTypeEnum.CAN_TAKE_MEAL)) {
                sql.append(" and repast_date=? ");
                params.add(DateUtil.getBeginTime(System.currentTimeMillis(), null));
            }
        }
        long timeBucketId = storeQueryDTO.getTimeBucketId();
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        long userId = storeQueryDTO.getUserId();
        if (userId > 0) {
            sql.append(" and user_id=? ");
            params.add(userId);
        }
        StoreOrderStatusTypeEnum orderStatusType = storeQueryDTO.getOrderStatusType();
        if (orderStatusType.equals(StoreOrderStatusTypeEnum.TO_BE_CONSUME)) {
            sql.append(" and pay_status=? ");
            params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            sql.append(" and refund_status in (?,?) ");
            params.add(StoreOrderRefundStatusEnum.NOT.getValue());
            params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
            sql.append(" and trade_status!=? ");
            params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
        } else if (orderStatusType.equals(StoreOrderStatusTypeEnum.HAS_BEAN_CONSUME)) {
            sql.append(" and pay_status=? ");
            params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            sql.append(" and trade_status=? ");
            params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
        } else if (orderStatusType.equals(StoreOrderStatusTypeEnum.HAS_BEAN_REFUND)) {
            sql.append(" and refund_status in (?,?,?) ");
            params.add(StoreOrderRefundStatusEnum.USER_ALL.getValue());
            params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
            params.add(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
        } else if (orderStatusType.equals(StoreOrderStatusTypeEnum.CAN_TAKE_MEAL)) {
            sql.append(" and pay_status=? ");
            params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            sql.append(" and refund_status in (?,?) ");
            params.add(StoreOrderRefundStatusEnum.NOT.getValue());
            params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
            sql.append(" and take_serial_number=0 ");
        } else if (orderStatusType.equals(StoreOrderStatusTypeEnum.CAN_REFUND)) {
            sql.append(" and pay_status=? ");
            params.add(StoreOrderPayStatusEnum.FINISH.getValue());
            sql.append(" and refund_status in (?,?) ");
            params.add(StoreOrderRefundStatusEnum.NOT.getValue());
            params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
            sql.append(" and trade_status=? ");
            params.add(StoreOrderTradeStatusEnum.NOT.getValue());
            sql.append(" and (order_lock_status=? or (order_lock_status=? and auto_lock_time>?)) ");
            params.add(StoreOrderLockStatusEnum.NOT.getValue());
            params.add(StoreOrderLockStatusEnum.AUTO.getValue());
            params.add(System.currentTimeMillis());
        }
        if (asc) {
            sql.append(" order by create_time asc ");
        } else {
            sql.append(" order by create_time desc ");
        }
        paramMap.put("whereSql", sql);
        paramMap.put("whereParams", params);
        return paramMap;
    }

    public List<StoreOrder> getListInIdsForUpdate(int merchantId, long storeId, List<String> orderIds) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreOrder.class, "where merchant_id=? and store_id=?", "order_id", " for update", Lists.newArrayList(merchantId, storeId), orderIds);
    }

    public int countForDeliveryWaitForPrepare(int merchantId, long storeId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreOrder.class, "where merchant_id=? and store_id=? and take_mode=? and trade_status=? and pay_status=? and refund_status in (?,?)", new Object[]{merchantId, storeId, StoreOrderTakeModeEnum.SEND_OUT.getValue(), StoreOrderTradeStatusEnum.NOT.getValue(), StoreOrderPayStatusEnum.FINISH.getValue(), StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue()});
    }

    /**
     * 在订单预约送达时间之前进行提醒的数据量获取
     *
     * @param merchantId  商户id
     * @param storeId     店铺id
     * @param enableSlave 是否走从库
     * @return
     */
    public int countForDeliveryWaitForPrepareForNotify(int merchantId, long storeId, long minNotifyTime, boolean enableSlave) {
        DALContext dalContext = this.buildDalContext(merchantId, storeId, enableSlave);
        return this.query.count(new Class[]{StoreOrder.class, StoreOrderDelivery.class}, "where tb_store_order_.order_id=tb_store_order_delivery_.order_id and tb_store_order_.merchant_id=? and tb_store_order_.store_id=? and tb_store_order_.take_mode=? and tb_store_order_.trade_status=? and tb_store_order_.pay_status=? and tb_store_order_.refund_status in (?,?) and tb_store_order_delivery_.delivery_assign_time<=?", new Object[]{merchantId, storeId, StoreOrderTakeModeEnum.SEND_OUT.getValue(), StoreOrderTradeStatusEnum.NOT.getValue(), StoreOrderPayStatusEnum.FINISH.getValue(), StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue(), minNotifyTime}, dalContext);
    }

    public int countForDeliveryTradeStatus(int merchantId, long storeId, StoreOrderTradeStatusEnum statusEnum, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        if (statusEnum.getValue() == StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue() ||
                statusEnum.getValue() == StoreOrderTradeStatusEnum.SENTED.getValue()) {
            return this.query.count(StoreOrder.class, "where merchant_id=? and store_id=? and take_mode=? and pay_status=? and trade_status=? and refund_status in (?,?) and waimai_type != ?", new Object[]{merchantId, storeId, StoreOrderTakeModeEnum.SEND_OUT.getValue(), StoreOrderPayStatusEnum.FINISH.getValue(), statusEnum.getValue(), StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue(), WaimaiTypeEnum.PICKUPSITE.getValue()});
        }
        return this.query.count(StoreOrder.class, "where merchant_id=? and store_id=? and take_mode=? and pay_status=? and trade_status=? and refund_status in (?,?)", new Object[]{merchantId, storeId, StoreOrderTakeModeEnum.SEND_OUT.getValue(), StoreOrderPayStatusEnum.FINISH.getValue(), statusEnum.getValue(), StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue()});
    }

    public void changeOrdersUser(int merchantId, long storeId, long srcUserId, long destUserId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreOrder.class, "set user_id=? where store_id=? and user_id=?", new Object[]{destUserId, storeId, srcUserId});
    }

    public List<Map<String, Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql, params);
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<StoreOrder> getList(QueryOrderParam queryOrderParam, int begin, int size, boolean enableSlave) {
        Object[] objs = this.buildForGetStoreOrders(queryOrderParam, true, enableSlave);
        String sql = (String) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.mysqlList2(StoreOrder.class, sql, begin, size, params);
    }

    @SuppressWarnings("unchecked")
    public int count(QueryOrderParam queryOrderParam, boolean enableSlave) {

        Object[] objs = this.buildForGetStoreOrders(queryOrderParam, false, enableSlave);
        String sql = (String) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.count2(StoreOrder.class, sql, params);
    }

    public Object[] buildForGetStoreOrders(QueryOrderParam queryOrderParam, boolean needOrder, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(queryOrderParam.getMerchantId(), queryOrderParam.getStoreId());
        Object[] objs = new Object[2];
        StringBuilder sb = new StringBuilder();
        List<Object> params = new ArrayList<>();
        sb.append("where merchant_id=? and store_id=?");
        params.add(queryOrderParam.getMerchantId());
        params.add(queryOrderParam.getStoreId());
        if (DataUtil.isNotEmpty(queryOrderParam.getOrderId())) {
            sb.append(" and order_id=?");
            params.add(queryOrderParam.getOrderId());
        }
        if (queryOrderParam.getUserId() > 0) {
            sb.append(" and user_id=?");
            params.add(queryOrderParam.getUserId());
        }
        if (queryOrderParam.getPayStatus() != null) {
            sb.append(" and pay_status=?");
            params.add(queryOrderParam.getPayStatus());
        }
        if (queryOrderParam.getRefundStatus() != null) {
            sb.append(" and refund_status=?");
            params.add(queryOrderParam.getRefundStatus());
        }
        if (queryOrderParam.getTakeSerialNumber() > 0) {
            sb.append(" and take_serial_number=?");
            params.add(queryOrderParam.getTakeSerialNumber());
        }
        if (queryOrderParam.getMinCreateTime() > 0) {
            sb.append(" and create_time>=?");
            params.add(queryOrderParam.getMinCreateTime());
        }
        if (queryOrderParam.getMaxCreateTime() > 0) {
            sb.append(" and create_time<=?");
            params.add(queryOrderParam.getMaxCreateTime());
        }

        if (needOrder) {
            if (queryOrderParam.getOrderByType().equals(QueryOrderOrderByTypeEnum.CREATE_TIME_ASC)) {
                sb.append(" order by create_time asc");
            } else if (queryOrderParam.getOrderByType().equals(QueryOrderOrderByTypeEnum.CREATE_TIME_DESC)) {
                sb.append(" order by create_time desc");
            }
        }
        objs[0] = sb.toString();
        objs[1] = params;
        return objs;
    }

    /**
     * 根据storeQueryDTO查询订单列表
     */
    @SuppressWarnings("unchecked")
    public List<StoreOrder> getStoreOrders(StoreOrderOptQueryParam queryParam, boolean enableSlave) {
        Map<String, Object> paramMap = this.getStoreOrderByQueryDTOParam(queryParam);
        if (paramMap == null || paramMap.isEmpty()) {
            return null;
        }
        this.addDbRouteInfo(queryParam.getMerchantId(), queryParam.getStoreId());
        List<Object> params = (List<Object>) paramMap.get("whereParams");
        StringBuffer sql = (StringBuffer) paramMap.get("whereSql");
        int size = queryParam.getSize();
        int pageNo = queryParam.getPageNo();
        if (size > 0 && pageNo > 0) {
            int start = PageUtil.getBeginIndex(pageNo, size);
            sql.append(" order by create_time desc limit ?, ? ");
            params.add(start);
            params.add(size);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    /**
     * 根据storeQueryDTO查询订单列表
     *
     * @param queryParam
     * @return
     */
    @SuppressWarnings("unchecked")
    public int countStoreOrders(StoreOrderOptQueryParam queryParam, boolean enableSlave) {
        Map<String, Object> paramMap = this.getStoreOrderByQueryDTOParam(queryParam);
        if (paramMap == null || paramMap.isEmpty()) {
            return 0;
        }
        this.addDbRouteInfo(queryParam.getMerchantId(), queryParam.getStoreId());
        List<Object> params = (List<Object>) paramMap.get("whereParams");
        StringBuffer sql = (StringBuffer) paramMap.get("whereSql");
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.count(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    private Map<String, Object> getStoreOrderByQueryDTOParam(StoreOrderOptQueryParam queryParam) {
        Map<String, Object> paramMap = new HashMap<>();
        int merchantId = queryParam.getMerchantId();
        long storeId = queryParam.getStoreId();
        if (merchantId == 0 || storeId == 0) {
            return paramMap;
        }
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("where merchant_id=? ");
        params.add(merchantId);
        sql.append(" and store_id=? ");
        params.add(storeId);
        String orderId = queryParam.getOrderId();
        if (orderId != null && !orderId.isEmpty()) {
            sql.append(" and order_id=? ");
            params.add(orderId);
        }
        String payOrderId = queryParam.getPayOrderId();
        if (payOrderId != null && !payOrderId.isEmpty()) {
            sql.append(" and pay_order_id=? ");
            params.add(payOrderId);
        }
        long repastDate = queryParam.getRepastDate();
        if (repastDate > 0) {
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }
        long userId = queryParam.getUserId();
        if (userId > 0) {
            sql.append(" and user_id=? ");
            params.add(userId);
        }
        int takeSerialNumber = queryParam.getTakeSerialNumber();
        if (takeSerialNumber > 0) {
            sql.append(" and take_serial_number=? ");
            params.add(takeSerialNumber);
        }
        String takeCode = queryParam.getTakeCode();
        if (takeCode != null && !takeCode.isEmpty()) {
            sql.append(" and take_code=? ");
            params.add(takeCode);
        }
        StoreOrderPayStatusEnum payStatus = queryParam.getPayStatus();
        if (payStatus != null && payStatus.getValue() > 0) {
            sql.append(" and pay_status=? ");
            params.add(payStatus.getValue());
        }
        StoreOrderRefundStatusEnum refundStatus = queryParam.getRefundStatus();
        if (refundStatus != null && refundStatus.getValue() > 0) {
            sql.append(" and refund_status=? ");
            params.add(refundStatus.getValue());
        }
        StoreOrderTradeStatusEnum tradeStatus = queryParam.getTradeStatus();
        if (tradeStatus != null && tradeStatus.getValue() > 0) {
            sql.append(" and trade_status=? ");
            params.add(tradeStatus.getValue());
        }
        long minCreateTime = queryParam.getMinCreateTime(); // 最小创建时间
        if (minCreateTime > 0) {
            sql.append(" and create_time>=? ");
            params.add(minCreateTime);
        }
        long maxCreateTime = queryParam.getMaxCreateTime(); // 最大创建时间
        if (maxCreateTime > 0) {
            sql.append(" and create_time<=? ");
            params.add(maxCreateTime);
        }
        paramMap.put("whereSql", sql);
        paramMap.put("whereParams", params);
        return paramMap;
    }


    /**
     * 根据StoreOrderStatQueryParam查询订单列表
     */
    public List<StoreOrder> getStoreOrdersForStat(StoreOrderStatQueryParam queryParam, boolean enableSlave) {
        int merchantId = queryParam.getMerchantId();
        long storeId = queryParam.getStoreId();
        this.addDbRouteInfo(queryParam.getMerchantId(), queryParam.getStoreId());
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("where merchant_id=? ");
        params.add(merchantId);
        sql.append(" and store_id=? ");
        params.add(storeId);
        String orderId = queryParam.getOrderId();
        if (orderId != null && !orderId.isEmpty()) {
            sql.append(" and order_id=? ");
            params.add(orderId);
        }
        long repastDate = queryParam.getRepastDate();
        if (repastDate > 0) {
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }
        StoreOrderStatQueryTypeEnum statQueryType = queryParam.getStatQueryType();
        if (statQueryType != null) {
            if (statQueryType.equals(StoreOrderStatQueryTypeEnum.TRADE_ORDER)) {
                //支付完成，交易>1
                sql.append(" and pay_status=3 and trade_status>1 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CANCEL_ORDER)) {
                //支付完成，用户全额退款，尚未交易
                sql.append(" and pay_status=3 and refund_status=2 and trade_status=1 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CREDIT_ORDER)) {
                sql.append(" and credit_type>0 and credit_status>0 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.TRADE_PAY_ORDER)) {
                sql.append(" and pay_status=3 and trade_status>1 and refund_status in (1,5) ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.TRADE_REFUND_ORDER)) {
                sql.append(" and pay_status=3 and trade_status>1 and refund_status in (3,4) ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.OTHER_PAY_ORDER)) {
                sql.append(" and (trade_status>1 or credit_type>0) ");
            } else {
                sql.append(" and (trade_status>1 or credit_type>0) ");
            }
        } else {
            sql.append(" and (trade_status>1 or credit_type>0) ");
        }
        long minCreateTime = queryParam.getMinCreateTime(); // 最小创建时间
        if (minCreateTime > 0) {
            sql.append(" and create_time>=? ");
            params.add(minCreateTime);
        }
        long maxCreateTime = queryParam.getMaxCreateTime(); // 最大创建时间
        if (maxCreateTime > 0) {
            sql.append(" and create_time<=? ");
            params.add(maxCreateTime);
        }
        return this.query.list(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }


    public List<StoreOrder> getStoreOrderWithoutRefund(int merchantId, long storeId, List<String> optlogIds, int tradeStatus_not, int creditStatus_no_credit, int creditStatus_charge, int creditStatus_discharge, int refundStatus_user_all, int refundStatus_merchant_all) {

        List<Object> args = Lists.newArrayList();
        args.add(merchantId);
        args.add(storeId);
        args.add(tradeStatus_not);
        args.add(creditStatus_no_credit);
        args.add(creditStatus_charge);
        args.add(creditStatus_discharge);
        args.add(refundStatus_user_all);
        args.add(refundStatus_merchant_all);

        String sql = "where merchant_id=? and store_id=? and trade_status>? and credit_status in (?,?,?) and refund_status not in (?,?) ";
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreOrder.class, sql, "order_id", args, optlogIds);
    }

    /**
     * 统计订单上的入客数
     *
     * @param merchantId
     * @param storeId
     * @param sql
     * @param params
     * @return
     */
    public Map<String, Object> getResultMap(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.getJdbcSupport().getMap(sql, params);
    }

    public List<StoreOrder> getStoreOrdersSearch(StoreOrdersSearchParam param, boolean enableSlave, boolean limit) {
        //开始写查询sql
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();

        Map<String, Object> paramMap = this._storeOrdersSearchParam(param, limit);
        String sql = paramMap.get("sql").toString();
        List<Object> params = (List<Object>) paramMap.get("params");

        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreOrder.class, sql, params.toArray());
    }

    public int countStoreOrdersSearch(StoreOrdersSearchParam param, boolean enableSlave, boolean limit) {
        //开始写查询sql
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();

        Map<String, Object> paramMap = this._storeOrdersSearchParam(param, limit);
        String sql = paramMap.get("sql").toString();
        List<Object> params = (List<Object>) paramMap.get("params");

        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreOrder.class, sql, params.toArray());
    }

    private Map<String, Object> _storeOrdersSearchParam(StoreOrdersSearchParam param, boolean limit) {
        StringBuffer sqlbuffer = new StringBuffer();
        List<Object> params = Lists.newArrayList();
        sqlbuffer.append(" where merchant_id=? and store_id=? ");
        params.add(param.getMerchantId());
        params.add(param.getStoreId());
        long repastDate = param.getRepastDate();
        if (repastDate > 0) {
            sqlbuffer.append(" and repast_date=? ");
            params.add(repastDate);
        }
        long timeBucketId = param.getTimeBucketId();
        if (timeBucketId > 0) {
            sqlbuffer.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        //已支付，或者后付费
        sqlbuffer.append(" and (pay_status=? or pay_after=?) ");
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        params.add(true);
        //取餐方式枚举
        StoreOrderSearchTakeEnum storeOrderSearchTakeEnum = param.getStoreOrderSearchTakeEnum();
        //预约取消
        StoreOrderSearchCanceEnum storeOrderSearchCanceEnum = param.getStoreOrderSearchCanceEnum();
        //按退款
        StoreOrderSearchRefundEnum storeOrderSearchRefundEnum = param.getStoreOrderSearchRefundEnum();
        //取餐方式进行判断
        if (storeOrderSearchTakeEnum != null) {
            switch (storeOrderSearchTakeEnum) {
                case DINE_IN: {//堂食  包含(堂食加外带)
                    sqlbuffer.append(" and take_mode in (?,?) ");
                    params.add(StoreOrderTakeModeEnum.DINE_IN.getValue());
                    params.add(StoreOrderTakeModeEnum.IN_AND_OUT.getValue());
                    break;
                }
                case TAKE_OUT: {//外带
                    sqlbuffer.append(" and take_mode=? ");
                    params.add(StoreOrderTakeModeEnum.TAKE_OUT.getValue());
                    break;
                }
                case IN_AND_OUT: {//堂食+外带
                    sqlbuffer.append(" and take_mode=? ");
                    params.add(StoreOrderTakeModeEnum.IN_AND_OUT.getValue());
                    break;
                }
                case SEND_OUT: {//外送
                    sqlbuffer.append(" and take_mode=? ");
                    params.add(StoreOrderTakeModeEnum.SEND_OUT.getValue());
                    break;
                }
                case QUICK_TAKE: {//快取
                    sqlbuffer.append(" and take_mode=? ");
                    params.add(StoreOrderTakeModeEnum.QUICK_TAKE.getValue());
                    break;
                }
                case TAKE_CODE: {//已取号但没有出餐完成的订单
                    sqlbuffer.append(" and take_serial_number>0 and trade_status not in (?,?,?) ");
                    params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
                    params.add(StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue());
                    params.add(StoreOrderTradeStatusEnum.SENTED.getValue());
                    break;
                }
                default: {//不限
                }
            }
        }
        //预约取消进行判断
        if (storeOrderSearchCanceEnum != null) {
            switch (storeOrderSearchCanceEnum) {
                case ALL: { //所有取消的订单
                    sqlbuffer.append(" and refund_status in (?,?) and trade_status=? and user_id>0  ");
                    params.add(StoreOrderRefundStatusEnum.USER_ALL.getValue());//用户全额退款
                    params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());//商户全额退款
                    params.add(StoreOrderTradeStatusEnum.NOT.getValue());
                    break;
                }
                case CLERK_CANCE: { //店员取消的订单
                    sqlbuffer.append(" and refund_status=? and trade_status=? and cancel_order_type=? and user_id>0 ");
                    params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());//商户全额退款
                    params.add(StoreOrderTradeStatusEnum.NOT.getValue());
                    params.add(CancelOrderTypeEnum.STAFF_CANCEL_ORDER.getValue());
                    break;
                }
                case SYSTEM_CANCE: { //系统取消的订单
                    sqlbuffer.append(" and refund_status=? and trade_status=? and cancel_order_type=? and user_id>0 ");
                    params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());//用户全额退款
                    params.add(StoreOrderTradeStatusEnum.NOT.getValue());
                    params.add(CancelOrderTypeEnum.SYSTEM_CANCEL_ORDER.getValue());
                    break;
                }
                case CUSTOMER_CANCE: { //顾客取消的订单
                    sqlbuffer.append(" and refund_status=? and trade_status=? and user_id>0 ");
                    params.add(StoreOrderRefundStatusEnum.USER_ALL.getValue());//用户全额退款
                    params.add(StoreOrderTradeStatusEnum.NOT.getValue());
                    break;
                }
                default: {//不限
                }
            }
        }
        //退款进行判断
        if (storeOrderSearchRefundEnum != null) {
            switch (storeOrderSearchRefundEnum) {
                case ALL: { //所有退款的订单
                    sqlbuffer.append(" and refund_status in (?,?,?) ");
                    params.add(StoreOrderRefundStatusEnum.USER_ALL.getValue());//用户全额退款
                    params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());//商户全额退款
                    params.add(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());//商户部分退款
                    break;
                }
                case FULL: { //全额退款的订单(用户全额退款,商户全额退款)
                    sqlbuffer.append(" and refund_status in (?,?) ");
                    params.add(StoreOrderRefundStatusEnum.USER_ALL.getValue());//用户全额退款
                    params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());//商户全额退款
                    break;
                }
                case PART: { //部分退款的订单(商户部分退款)
                    sqlbuffer.append(" and refund_status=? ");
                    params.add(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());//商户部分退款
                    break;
                }
                default: {//不限
                }
            }
        }
        if (limit) {
            //排序
            sqlbuffer.append(" order by create_time desc ");
            //分页
            int size = param.getSize();
            int pageNo = param.getPageNo();
            int start = 0;
            if (size > 0 && pageNo > 0) {
                start = PageUtil.getBeginIndex(pageNo, size);
                sqlbuffer.append(" limit ?, ? ");
                params.add(start);
                params.add(size);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sqlbuffer.toString());
        map.put("params", params);
        return map;
    }

    /**
     * 根据StoreOrderStatQueryParam查询订单总数和总金额（应付金额+外送费）</br>
     *
     * @param StoreOrderStatQueryParam
     * @return StoreOrderSummaryDTO
     */
    public StoreOrderSummaryDTO getStoreOrdersSummary(StoreOrderStatQueryParam queryParam, boolean enableSlave) {
        int merchantId = queryParam.getMerchantId();
        long storeId = queryParam.getStoreId();
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append(" select count(*) as count_orders ,sum(favorable_price+delivery_fee) as sum_price ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? ");
        params.add(merchantId);
        sql.append(" and store_id=? ");
        params.add(storeId);
        String orderId = queryParam.getOrderId();
        if (orderId != null && !orderId.isEmpty()) {
            sql.append(" and order_id=? ");
            params.add(orderId);
        }
        long repastDate = queryParam.getRepastDate();
        if (repastDate > 0) {
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }
        StoreOrderStatQueryTypeEnum statQueryType = queryParam.getStatQueryType();
        if (statQueryType != null) {
            if (statQueryType.equals(StoreOrderStatQueryTypeEnum.TRADE_ORDER)) {
                sql.append(" and pay_status=3 and trade_status>1 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CANCEL_ORDER)) {
                sql.append(" and pay_status=3 and refund_status=2 and trade_status=1 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CREDIT_ORDER)) {
                sql.append(" and credit_type>0 and credit_status>0 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.TRADE_PAY_ORDER)) {
                sql.append(" and pay_status=3 and trade_status>1 and refund_status in (1,5) ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.TRADE_REFUND_ORDER)) { //退款
                sql.append(" and pay_status=3 and trade_status>1 and refund_status in (3,4) ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.OTHER_PAY_ORDER)) {
                sql.append(" and (trade_status>1 or credit_type>0) ");
            } else {
                sql.append(" and (trade_status>1 or credit_type>0) ");
            }
        } else {
            sql.append(" and (trade_status>1 or credit_type>0) ");
        }
        long minCreateTime = queryParam.getMinCreateTime(); // 最小创建时间
        if (minCreateTime > 0) {
            sql.append(" and create_time>=? ");
            params.add(minCreateTime);
        }
        long maxCreateTime = queryParam.getMaxCreateTime(); // 最大创建时间
        if (maxCreateTime > 0) {
            sql.append(" and create_time<=? ");
            params.add(maxCreateTime);
        }
        List<StoreOrderSummaryDTO> list = this.query.getJdbcSupport().getJdbcTemplate().query(sql.toString(), params.toArray(),
                new RowMapper() {
                    @Override
                    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
                        StoreOrderSummaryDTO dto = new StoreOrderSummaryDTO();
                        dto.setTotalOrder(rs.getInt("count_orders"));
                        dto.setPayablePrice(rs.getLong("sum_price"));
                        return dto;
                    }
                }
        );
        return list.get(0);
    }

    /**
     * 查询销账订单类型（credit_status>0）的总数和总金额
     *
     * @param queryParam
     * @return map中的key：1-->未销账  2-->销账成功  3-->销账撤销
     */
    public Map<Integer, StoreOrderSummaryDTO> getStoreCreditOrdersSummary(StoreOrderStatQueryParam queryParam,
                                                                          boolean enableSlave) {
        int merchantId = queryParam.getMerchantId();
        long storeId = queryParam.getStoreId();
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append(" select credit_status as status, count(*) as count_orders ,sum(favorable_price+delivery_fee) as sum_price ");
        sql.append(" FROM ").append(this.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? ");
        params.add(merchantId);
        sql.append(" and store_id=? ");
        params.add(storeId);
        String orderId = queryParam.getOrderId();
        if (orderId != null && !orderId.isEmpty()) {
            sql.append(" and order_id=? ");
            params.add(orderId);
        }
        long repastDate = queryParam.getRepastDate();
        if (repastDate > 0) {
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }

        long minCreateTime = queryParam.getMinCreateTime(); // 最小创建时间
        if (minCreateTime > 0) {
            sql.append(" and create_time>=? ");
            params.add(minCreateTime);
        }
        long maxCreateTime = queryParam.getMaxCreateTime(); // 最大创建时间
        if (maxCreateTime > 0) {
            sql.append(" and create_time<=? ");
            params.add(maxCreateTime);
        }

        StoreOrderStatQueryTypeEnum statQueryType = queryParam.getStatQueryType();
        if (statQueryType != null) {
            if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CREDIT_ORDER)) {
                sql.append(" and credit_status>0 group by credit_status ");
            }
        }

        Map<Integer, StoreOrderSummaryDTO> dtoMap = new HashMap<Integer, StoreOrderSummaryDTO>();
        DALStatus.setSlaveMode();
        List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray(new Object[params.size()]));
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> map = list.get(i);
                StoreOrderSummaryDTO dto = new StoreOrderSummaryDTO();
                int status = ObjectUtil.getInt(map, "status");
                dto.setTotalOrder(ObjectUtil.getInt(map, "count_orders"));
                dto.setPayablePrice(ObjectUtil.getLong(map, "sum_price"));
                dtoMap.put(status, dto);
            }
        }
        return dtoMap;
    }

    /**
     * 获取赊账类型订单统计
     *
     * @param queryParam
     * @param enableSlave
     * @return List<StoreOrder>
     */
    public List<StoreOrder> getStoreCreditOrdersForStat(StoreOrderStatQueryParam queryParam, boolean enableSlave) {
        int merchantId = queryParam.getMerchantId();
        long storeId = queryParam.getStoreId();
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("where merchant_id=? ");
        params.add(merchantId);
        sql.append(" and store_id=? ");
        params.add(storeId);
        String orderId = queryParam.getOrderId();
        if (orderId != null && !orderId.isEmpty()) {
            sql.append(" and order_id=? ");
            params.add(orderId);
        }
        long repastDate = queryParam.getRepastDate();
        if (repastDate > 0) {
            sql.append(" and repast_date=? ");
            params.add(repastDate);
        }
        StoreOrderStatQueryTypeEnum statQueryType = queryParam.getStatQueryType();
        if (statQueryType != null) {
            if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CREDIT_CHARGE_ORDER)) {  //未销账credit_status=1
                sql.append(" and credit_type>0 and credit_status=1 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CREDIT_DISCHARGE_ORDER)) {  //销账成功credit_status=2
                sql.append(" and credit_type>0 and credit_status=2 ");
            } else if (statQueryType.equals(StoreOrderStatQueryTypeEnum.CREDIT_CANCEL_CHARGE_ORDER)) { //销账撤销credit_status=3
                sql.append(" and credit_type>0 and credit_status=3 ");
            }
        } else {
            sql.append(" and (trade_status>1 or credit_type>0) ");
        }
        long minCreateTime = queryParam.getMinCreateTime(); // 最小创建时间
        if (minCreateTime > 0) {
            sql.append(" and create_time>=? ");
            params.add(minCreateTime);
        }
        long maxCreateTime = queryParam.getMaxCreateTime(); // 最大创建时间
        if (maxCreateTime > 0) {
            sql.append(" and create_time<=? ");
            params.add(maxCreateTime);
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreOrder.class, sql.toString(), params.toArray(new Object[params.size()]));
    }

    /**
     * 根据桌台记录id查询相关联子订单
     *
     * @param merchantId    商编
     * @param stroreId      订单id
     * @param tableRecordId 桌台记录id
     * @return
     */
    public List<StoreOrder> getSubStoreOrderByTableRecordId(int merchantId, long storeId, long tableRecordId, String masterOrderId, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        if (!StringUtils.isNullOrEmpty(masterOrderId)) {
            String sql = " where table_record_id=? and parent_order_id=? and take_serial_number>0 order by create_time";
            if (forUpdate) {
                sql = sql + " for update";
            }
            return this.query.list(StoreOrder.class, sql, new Object[]{tableRecordId, masterOrderId});
        } else {
            String sql = " where table_record_id=? and parent_order_id!=? and take_serial_number>0 order by create_time";
            if (forUpdate) {
                sql = sql + " for update";
            }
            return this.query.list(StoreOrder.class, sql, new Object[]{tableRecordId, ""});
        }
    }

    /**
     * 根据桌台记录id和叫起状态查询相关联子订单
     *
     * @param merchantId    商编
     * @param stroreId      订单id
     * @param tableRecordId 桌台记录id
     * @return
     */
    public List<StoreOrder> getSubStoreOrderByTableRecordIdAndSendType(int merchantId, long storeId, long tableRecordId, String masterOrderId, int sendType, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        if (!StringUtils.isNullOrEmpty(masterOrderId)) {
            String sql = " where table_record_id=? and parent_order_id=? and take_serial_number>0 and send_type=? order by create_time";
            if (forUpdate) {
                sql = sql + " for update";
            }
            return this.query.list(StoreOrder.class, sql, new Object[]{tableRecordId, masterOrderId, sendType});
        } else {
            String sql = " where table_record_id=? and parent_order_id!=? and take_serial_number>0 and send_type=? order by create_time";
            if (forUpdate) {
                sql = sql + " for update";
            }
            return this.query.list(StoreOrder.class, sql, new Object[]{tableRecordId, "", sendType});
        }
    }

    /**
     * 根据桌台记录id查询相关联主订单
     *
     * @param merchantId
     * @param storeId
     * @param tableRecordId
     * @return
     * @throws T5weiException
     */
    public StoreOrder getMasterOrderByTableRecordId(int merchantId, long storeId, long tableRecordId, boolean forUpdate) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where table_record_id=? and parent_order_id=? and take_serial_number>0";
        if (forUpdate) {
            sql = sql + " for update";
        }
        StoreOrder masterOrder = this.query.obj(StoreOrder.class, sql, new Object[]{tableRecordId, ""});
        return masterOrder;
    }


    public void updateUserRemark(int merchantId, long storeId, String orderId, String userRemark) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreOrder.class, "set user_remark=? where order_id=?", new Object[]{userRemark, orderId});
    }

    /**
     * 获得5wei 库中的订单
     *
     * @param merchantId
     * @param storeId
     * @param day
     * @param payStatus
     * @param creditType1
     * @param creditType2 @return
     */
    public List<StoreOrder> getStoreOrderByRepastDate(int merchantId, long storeId, Long day, int payStatus, int creditType1, int creditType2) {
        //pay_status 3, 支付完成 or 赊账类型(1:协议企业 2:公关费用)
        String sql = " where store_id = ? and repast_date = ? and (pay_status = ? or credit_type in (?,?)) and order_price != 0 ";
        List<Object> args = Lists.newArrayList();
        args.add(storeId);
        args.add(day);
        args.add(payStatus);
        args.add(creditType1);
        args.add(creditType2);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreOrder.class, sql, args.toArray());
    }

    /**
     * 获取未备餐的订单
     *
     * @param merchantId
     * @param storeId
     * @param enableSlave
     * @return
     */
    public List<String> getWaitPrepareOrderIds(int merchantId, long storeId, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<Map<String, Object>> resultMapList = this.getResultMapList(merchantId, storeId, "select order_id from " + this.getRealName(merchantId, storeId) + " where store_id=? and trade_status=? and take_mode=? and pay_status=? and refund_status in (?,?)",
                new Object[]{storeId, StoreOrderTradeStatusEnum.NOT.getValue(), StoreOrderTakeModeEnum.SEND_OUT.getValue(), StoreOrderPayStatusEnum.FINISH.getValue(), StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue()}, true);
        List<String> storeOrderIds = new ArrayList<String>();
        if (resultMapList != null && !resultMapList.isEmpty()) {
            for (Map<String, Object> map : resultMapList) {
                String orderId = ObjectUtil.getString(map, "order_id");
                storeOrderIds.add(orderId);
            }
        }
        return storeOrderIds;
    }

    /**
     * 得到查询日期的所有订单
     *
     * @param merchantId 商户
     * @param storeId    店铺
     * @param repastDate 就餐日期
     * @return
     */
    public List<StoreOrder> getStoreOrdersByRepastDate(int merchantId, long storeId, long repastDate, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        String sql = " where store_id = ? and repast_date = ? ";
        List<Object> params = Lists.newArrayList();
        params.add(storeId);
        params.add(repastDate);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list2(StoreOrder.class, sql, params);
    }

    /**
     * 根据取餐日期的时间范围 获得订单集合
     *
     * @param merchantId
     * @param storeId
     * @param startTime
     * @param endTime
     * @return
     */
    public List<StoreOrder> getStoreOrdersByTime(int merchantId, long storeId, long startTime, long endTime, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        String sql = " where merchant_id = ? and store_id = ? and repast_date >= ? and repast_date <= ? ";
        return this.query.list(StoreOrder.class, sql, new Object[]{merchantId, storeId, startTime, endTime});
    }

    /**
     * 根据取餐流水号得到订单
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param takeSerialNumber
     * @param enableSlave
     * @return
     */
    public StoreOrder getStoreOrderByTakeSerialNumber(int merchantId, long storeId, long repastDate, int takeSerialNumber, boolean enableSlave) {
        if (repastDate == 0 || takeSerialNumber == 0) {
            return null;
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreOrder.class, "where merchant_id=? and store_id=? and repast_date=? and take_serial_number=? ", new Object[]{merchantId, storeId, repastDate, takeSerialNumber});
    }
    public StorePickupSiteBaseDTO getStorePickupSiteBase(int merchantId, long storeId, long storePickupSiteId, boolean enableSlabe) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlabe) {
            DALStatus.setSlaveMode();
        }
        StorePickupSite storePickupSite = this.storePickupSiteDAO.getStorePickupSiteById(merchantId, storeId, storePickupSiteId);
        StorePickupSiteBaseDTO storePickupSiteBaseDTO = null;
        if (storePickupSite != null) {
            List<StorePickupSiteBaseDTO> storePickupSiteBaseDTOs = storePickupSiteHelpler.convertStorePickupSiteBaseDTOs(Lists.newArrayList(storePickupSite));
            storePickupSiteBaseDTO = storePickupSiteBaseDTOs.get(0);
        }

        return storePickupSiteBaseDTO;
    }
}
