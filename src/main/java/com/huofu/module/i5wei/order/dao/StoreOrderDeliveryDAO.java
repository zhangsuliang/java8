package com.huofu.module.i5wei.order.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import halo.query.Query;
import halo.query.dal.DALContext;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteBaseDTO;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteOrderQueryParam;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteTimeSettingDTO;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.ObjectUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreOrderDeliveryDAO extends AbsQueryDAO<StoreOrderDelivery> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    private DALContext buildDalContext(int merchantId, long storeId, boolean
            enableSlave) {
        return BaseStoreDbRouter.buildDalContext(merchantId, storeId, enableSlave);
    }

    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreOrderDelivery.class);
        return dalInfo.getRealTable(StoreOrderDelivery.class);
    }

    @Override
    public void create(StoreOrderDelivery storeOrderDelivery) {
        this.addDbRouteInfo(storeOrderDelivery.getMerchantId(), storeOrderDelivery.getStoreId());
        super.create(storeOrderDelivery);
    }

    @Override
    public void update(StoreOrderDelivery storeOrderDelivery, StoreOrderDelivery snapshot) {
        this.addDbRouteInfo(storeOrderDelivery.getMerchantId(), storeOrderDelivery.getStoreId());
        super.update(storeOrderDelivery, snapshot);
    }

    @Override
    public void delete(StoreOrderDelivery storeOrderDelivery) {
        this.addDbRouteInfo(storeOrderDelivery.getMerchantId(), storeOrderDelivery.getStoreId());
        super.delete(storeOrderDelivery);
    }

    public StoreOrderDelivery getById(int merchantId, long storeId, String orderId, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.objById(StoreOrderDelivery.class, orderId);
    }

	public List<StoreOrderDelivery> getByIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
		this.addDbRouteInfo(merchantId, storeId);
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		return this.query.listInValues2(StoreOrderDelivery.class,
		                                null, "order_id", null, null, orderIds);
	}

	public StoreOrderDelivery getByWaimaiOrderId(int merchantId, long storeId, String waimaiOrderId, int waimaiType, boolean enableSlave) {
		this.addDbRouteInfo(merchantId, storeId);
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		return this.query
				.obj2(StoreOrderDelivery.class, "where waimai_order_id=? and waimai_type=?", Lists.newArrayList(waimaiOrderId, waimaiType));
	}

	public Map<String, StoreOrderDelivery> getMapInIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
        if (orderIds.isEmpty()) {
            return new HashMap<>(0);
        }
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.map2(StoreOrderDelivery.class, "where merchant_id=? and store_id=?", "order_id", Lists.newArrayList(merchantId, storeId), orderIds);
    }

    private Object[] buildQueryInfoForDelivery(int merchantId, long storeId, StoreOrderDeliveryQueryParam queryParam, boolean isContainPickupSiteOrder, boolean forData) {
        if (DataUtil.isEmpty(queryParam.getMobile()) && queryParam.getDeliveryStatus() == null) {
            throw new RuntimeException("mobile or queryStatus has one at least");
        }
        Object[] objs = new Object[2];
        List<Object> params = Lists.newArrayList();
        StringBuilder sb;
        sb = new StringBuilder("where tb_store_order_.order_id=tb_store_order_delivery_.order_id");
        sb.append(" and tb_store_order_.merchant_id=? and tb_store_order_.store_id=? and tb_store_order_.refund_status in (?,?) and tb_store_order_.take_mode=? and tb_store_order_delivery_.store_id=? and tb_store_order_.pay_status=?");
        if (!isContainPickupSiteOrder) {
            //排除自提点统计信息
            sb.append(" and tb_store_order_.waimai_type != ? ");
        }
        params.add(merchantId);
        params.add(storeId);
        params.add(StoreOrderRefundStatusEnum.NOT.getValue());
        params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
        params.add(StoreOrderTakeModeEnum.SEND_OUT.getValue());
        params.add(storeId);
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        if (!isContainPickupSiteOrder) {
            params.add(WaimaiTypeEnum.PICKUPSITE.getValue());
        }
        boolean orderByDeliveryFinish = false;
        if (queryParam.getDeliveryStaffId() > 0) {
            sb.append(" and tb_store_order_delivery_.delivery_staff_id=?");
            params.add(queryParam.getDeliveryStaffId());
        }
        if (DataUtil.isNotEmpty(queryParam.getMobile())) {
            sb.append(" and tb_store_order_delivery_.contact_phone=?");
            sb.append(" and (");
            sb.append(" (tb_store_order_.trade_status=? and tb_store_order_.create_time>=? and tb_store_order_.create_time<=?) or (tb_store_order_.trade_status!=?)");
            sb.append(")");
            params.add(queryParam.getMobile());
            params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
            long now = System.currentTimeMillis();
            long todayBegin = DateUtil.getBeginTime(now, null);
            long todayEnd = DateUtil.getEndTime(now, null);
            params.add(todayBegin);
            params.add(todayEnd);
            params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
        } else {
            sb.append(" and tb_store_order_.trade_status=?");
            if (queryParam.getDeliveryStatus().equals(StoreOrderDeliveryStatusEnum.WAIT_FOR_PREPARE)) {
                params.add(StoreOrderTradeStatusEnum.NOT.getValue());
            } else if (queryParam.getDeliveryStatus().equals(StoreOrderDeliveryStatusEnum.PREPARING)) {
                params.add(StoreOrderTradeStatusEnum.WORKIN.getValue());
            } else if (queryParam.getDeliveryStatus().equals(StoreOrderDeliveryStatusEnum.PREPARE_FINISH)) {
                params.add(StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue());
            } else if (queryParam.getDeliveryStatus().equals(StoreOrderDeliveryStatusEnum.DELIVERING)) {
                params.add(StoreOrderTradeStatusEnum.SENTED.getValue());
            } else if (queryParam.getDeliveryStatus().equals(StoreOrderDeliveryStatusEnum.DELIVERY_FINISH)) {
                sb.append(" and tb_store_order_delivery_.delivery_finish_time>=? and tb_store_order_delivery_.delivery_finish_time<=?");
                params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
                long now = System.currentTimeMillis();
                long todayBegin = DateUtil.getBeginTime(now, null);
                long todayEnd = DateUtil.getEndTime(now, null);
                params.add(todayBegin);
                params.add(todayEnd);
                orderByDeliveryFinish = true;
            } else {
                throw new RuntimeException("queryParam not supported");
            }
        }
        if (forData) {
            if (orderByDeliveryFinish) {
                sb.append(" order by tb_store_order_delivery_.delivery_finish_time desc");
            } else {
                sb.append(" order by tb_store_order_delivery_.delivery_assign_time asc");
            }
        }
        objs[0] = sb;
        objs[1] = params;
        return objs;
    }

    @SuppressWarnings("unchecked")
    public List<StoreOrder> getListForDelivery(int merchantId, long storeId, StoreOrderDeliveryQueryParam queryParam, int begin, int size, boolean isContainPickupSiteOrder, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        Object[] objs = this.buildQueryInfoForDelivery(merchantId, storeId, queryParam, isContainPickupSiteOrder, true);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.mysqlList2(new Class[]{StoreOrder.class, StoreOrderDelivery.class}, sb.toString(), begin, size, params, (rs, rowNum) -> {
            StoreOrder storeOrder = Query.getRowMapper(StoreOrder.class).mapRow(rs, rowNum);
            StoreOrderDelivery storeOrderDelivery = Query.getRowMapper(StoreOrderDelivery.class).mapRow(rs, rowNum);
            storeOrder.setStoreOrderDelivery(storeOrderDelivery);
            storeOrderDelivery.setDeliveryStatus(storeOrder.getDeliveryStatus());
            return storeOrder;
        });
    }

    @SuppressWarnings("unchecked")
    public int countForDelivery(int merchantId, long storeId, StoreOrderDeliveryQueryParam queryParam, boolean isContainPickupSiteOrder, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        Object[] objs = this.buildQueryInfoForDelivery(merchantId, storeId, queryParam, isContainPickupSiteOrder, false);
        StringBuilder sb = (StringBuilder) objs[0];
        List<Object> params = (List<Object>) objs[1];
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.count(new Class[]{StoreOrder.class, StoreOrderDelivery.class}, sb.toString(), params);
    }

    /**
     * 在订单预约送达时间之前进行提醒的最新id
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param minNotifyTime 最小提醒时间
     * @param enableSlave   是否走从库
     * @return 最新外送下单id
     */
    public String getLast4WaitPrepare(int merchantId, long storeId, long minNotifyTime, boolean
            enableSlave) {
        DALContext dalContext = this.buildDalContext(merchantId, storeId,
                enableSlave);
        List<StoreOrderDelivery> list = this.query.mysqlList(new Class[]{StoreOrder.class,
                StoreOrderDelivery
                        .class}, "where tb_store_order_.order_id=tb_store_order_delivery_.order_id " +
                "and tb_store_order_.merchant_id=? and tb_store_order_.store_id=? " +
                "and tb_store_order_.take_mode=? " +
                "and tb_store_order_.pay_status=? and tb_store_order_.refund_status in (?,?) " +
                "and tb_store_order_delivery_.delivery_assign_time<=? " +
                "order by tb_store_order_.update_time desc", 0, 1, new Object[]{merchantId, storeId,
                StoreOrderTakeModeEnum.SEND_OUT.getValue(), StoreOrderPayStatusEnum.FINISH.getValue(),
                StoreOrderRefundStatusEnum.NOT.getValue(), StoreOrderRefundStatusEnum.FAILURE.getValue(),
                minNotifyTime}, (rs, rowNum) -> {
            RowMapper<StoreOrderDelivery> mapper = Query.getRowMapper(StoreOrderDelivery.class);
            return mapper.mapRow(rs, rowNum);
        }, dalContext);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0).getOrderId();
    }

    public List<Map<String, Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.getJdbcSupport().getMapList(sql, params);
    }

    /**
     * 统计自提点订单信息，返回组装的自提点信息
     * @return
     */
    public List<StorePickupSiteDeliveryDTO> getPickupSiteDeliveryOrdersforDelivery(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        String storeOrderName = StoreOrderHelper.transToRealTableName(
                pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId(), StoreOrder.class);
        String storeOrderDeliveryName = StoreOrderHelper.transToRealTableName(
                pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId(), StoreOrderDelivery.class);
        StringBuilder sb = new StringBuilder("SELECT COUNT(1) AS total_order, tb_store_order_.time_bucket_id as time_bucket_id, tb_store_order_delivery_.pickup_site_id AS pickup_site_id, tb_store_order_delivery_.delivery_staff_id as delivery_staff_id, tb_store_order_delivery_.delivery_staff_user_id as delivery_staff_user_id FROM " + storeOrderName + " tb_store_order_, " + storeOrderDeliveryName + " tb_store_order_delivery_ ");
        Object[] objs = this.buildStorePickupSiteOrderQuerySqlAndParams(pickupSiteOrderQueryParam);
        String whereSql = (String) objs[0];
        List<Object> params = (List<Object>) objs[1];
        sb.append(whereSql);
        sb.append(" GROUP BY tb_store_order_delivery_.pickup_site_id, tb_store_order_.time_bucket_id, tb_store_order_delivery_.delivery_staff_id, tb_store_order_delivery_.delivery_staff_user_id");
        List<Map<String, Object>> resultList = this.getResultMapList(
                pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId(), sb.toString(), params.toArray(), true);
        List<StorePickupSiteDeliveryDTO> storePickupSites = new ArrayList<>(resultList.size());
        if(CollectionUtils.isNotEmpty(resultList)) {
            for (Map<String, Object> resultMap : resultList) {
                //组装统计信息
                StorePickupSiteDeliveryDTO pickupSiteDeliveryDTO = new StorePickupSiteDeliveryDTO();
                pickupSiteDeliveryDTO.setDeliveryCount(ObjectUtil.getInt(resultMap.get("total_order"), 0));
                pickupSiteDeliveryDTO.setDeliveryStaffId(ObjectUtil.getLong(resultMap.get("delivery_staff_id"), 0));
                pickupSiteDeliveryDTO.setDeliveryStaffUserId(ObjectUtil.getLong(resultMap.get("delivery_staff_user_id"), 0));

                long storePickupSiteId = ObjectUtil.getLong(resultMap.get("pickup_site_id"), 0);
                long timeBucketId = ObjectUtil.getLong(resultMap.get("time_bucket_id"), 0);

                //组装营业时段信息
                StorePickupSiteTimeSettingDTO pickupSiteTimeSetting = new StorePickupSiteTimeSettingDTO();
                pickupSiteTimeSetting.setStorePickupSiteId(storePickupSiteId);
                pickupSiteTimeSetting.setTimeBucketId(timeBucketId);

                //组装自提点基本信息
                StorePickupSiteBaseDTO storePickupSiteBase = new StorePickupSiteBaseDTO();
                storePickupSiteBase.setStorePickupSiteId(storePickupSiteId);
                storePickupSiteBase.setMerchantId(pickupSiteOrderQueryParam.getMerchantId());
                storePickupSiteBase.setStoreId(pickupSiteOrderQueryParam.getStoreId());
                storePickupSiteBase.setStorePickupSiteTimeSetting(pickupSiteTimeSetting);

                pickupSiteDeliveryDTO.setStorePickupSiteInfo(storePickupSiteBase);

                storePickupSites.add(pickupSiteDeliveryDTO);
            }
        }
        return storePickupSites;
    }

    public Object[] buildPickupSiteOrderQuerySqlContainDeliveryInfo(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        Object[] objs = new Object[2];
        List<Object> params = Lists.newArrayList();
        StringBuilder querySql = new StringBuilder(" WHERE tb_store_order_.order_id = tb_store_order_delivery_.order_id AND ");
        querySql.append(" tb_store_order_.merchant_id = tb_store_order_delivery_.merchant_id AND tb_store_order_.store_id = tb_store_order_delivery_.store_id ");
        querySql.append(" and tb_store_order_.merchant_id=? and tb_store_order_.store_id=? and tb_store_order_.refund_status in (?,?) ");
        querySql.append(" and tb_store_order_.take_mode=? and tb_store_order_.pay_status=? ");
        params.add(pickupSiteOrderQueryParam.getMerchantId());
        params.add(pickupSiteOrderQueryParam.getStoreId());
        params.add(StoreOrderRefundStatusEnum.NOT.getValue());
        params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
        params.add(StoreOrderTakeModeEnum.SEND_OUT.getValue());
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        if (pickupSiteOrderQueryParam.getStaffId() > 0) {
            querySql.append("and tb_store_order_delivery_.delivery_staff_id=?");
            params.add(pickupSiteOrderQueryParam.getStaffId());
        }
        if (pickupSiteOrderQueryParam.isSetWaimaiType()) {
            querySql.append(" and tb_store_order_.waimai_type = ?");
            params.add(pickupSiteOrderQueryParam.getWaimaiType());
        }
        if (!pickupSiteOrderQueryParam.isContainPickupSiteInfo()) {
            querySql.append(" and tb_store_order_.waimai_type != ?");
            params.add(WaimaiTypeEnum.PICKUPSITE.getValue());
        }
        if (CollectionUtils.isNotEmpty(pickupSiteOrderQueryParam.getPickupSiteIds())) {
            querySql.append(" and tb_store_order_delivery_.pickup_site_id in (");
            for (Long pickupSiteId : pickupSiteOrderQueryParam.getPickupSiteIds()) {
                querySql.append("?,");
                params.add(pickupSiteId);
            }
            if (querySql.toString().endsWith(",")) {
                querySql.replace(querySql.length() - 1, querySql.length(), ")");
            }
        }
        if(pickupSiteOrderQueryParam.getDeliveryStatus() > 0) {
            int deliveryStatus = pickupSiteOrderQueryParam.getDeliveryStatus();
            querySql.append(" and tb_store_order_.trade_status = ?");
            if (deliveryStatus == StoreOrderDeliveryStatusEnum.WAIT_FOR_PREPARE.getValue()) {
                params.add(StoreOrderTradeStatusEnum.NOT.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.PREPARING.getValue()) {
                params.add(StoreOrderTradeStatusEnum.WORKIN.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.PREPARE_FINISH.getValue()) {
                params.add(StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.DELIVERING.getValue()) {
                params.add(StoreOrderTradeStatusEnum.SENTED.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.DELIVERY_FINISH.getValue()) {
                querySql.append(" and tb_store_order_delivery_.delivery_finish_time>=? and tb_store_order_delivery_.delivery_finish_time<=?");
                params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
                long now = System.currentTimeMillis();
                long todayBegin = DateUtil.getBeginTime(now, null);
                long todayEnd = DateUtil.getEndTime(now, null);
                params.add(todayBegin);
                params.add(todayEnd);
            } else {
                throw new RuntimeException("queryParam not supported");
            }
        }
        if (pickupSiteOrderQueryParam.isNeedSort()) {
            if (pickupSiteOrderQueryParam.deliveryStatus == StoreOrderDeliveryStatusEnum.DELIVERY_FINISH.getValue()) {
                querySql.append(" order by tb_store_order_delivery_.delivery_finish_time desc");
            } else {
                querySql.append(" order by tb_store_order_delivery_.delivery_assign_time asc");
            }
        }
        objs[0] = querySql.toString();
        objs[1] = params;
        return objs;
    }

    public Object[] buildPickupSiteOrderQuerySqlNotContainDeliveryInfo(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        Object[] objs = new Object[2];
        StringBuilder querySql = new StringBuilder();
        List<Object> params = Lists.newArrayList();
        querySql.append(" where tb_store_order_.merchant_id = ? and tb_store_order_.store_id = ? ");
        querySql.append(" and tb_store_order_.refund_status in (?,?) and tb_store_order_.take_mode=? and tb_store_order_.pay_status=? ");
        params.add(pickupSiteOrderQueryParam.getMerchantId());
        params.add(pickupSiteOrderQueryParam.getStoreId());
        params.add(StoreOrderRefundStatusEnum.NOT.getValue());
        params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
        params.add(StoreOrderTakeModeEnum.SEND_OUT.getValue());
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        if (!pickupSiteOrderQueryParam.isContainPickupSiteInfo()) {
            querySql.append(" and tb_store_order_.waimai_type != ? ");
            params.add(WaimaiTypeEnum.PICKUPSITE.getValue());
        }
        int deliveryStatus = pickupSiteOrderQueryParam.getDeliveryStatus();
        if (deliveryStatus > 0) {
            querySql.append(" and tb_store_order_.trade_status=? ");
            if (deliveryStatus == StoreOrderDeliveryStatusEnum.WAIT_FOR_PREPARE.getValue()) {
                params.add(StoreOrderTradeStatusEnum.NOT.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.PREPARING.getValue()) {
                params.add(StoreOrderTradeStatusEnum.WORKIN.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.PREPARE_FINISH.getValue()) {
                params.add(StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.DELIVERING.getValue()) {
                params.add(StoreOrderTradeStatusEnum.SENTED.getValue());
            } else if (deliveryStatus == StoreOrderDeliveryStatusEnum.DELIVERY_FINISH.getValue()) {
                params.add(StoreOrderTradeStatusEnum.FINISH.getValue());
            } else {
                throw new RuntimeException("queryParam not supported");
            }
        }
        objs[0] = querySql.toString();
        objs[1] = params;
        return objs;
    }

    public Object[] buildStorePickupSiteOrderQuerySqlAndParams(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        Object[] objs;
        if (pickupSiteOrderQueryParam.isContainDeliveryInfo()) {
            objs = this.buildPickupSiteOrderQuerySqlContainDeliveryInfo(pickupSiteOrderQueryParam);
        } else {
            objs = this.buildPickupSiteOrderQuerySqlNotContainDeliveryInfo(pickupSiteOrderQueryParam);
        }
        return objs;
    }

    /**
     * 根据自提点ID获取订单信息
     * @return
     */
    public List<StoreOrder> getStorePickupSiteOrderByPickupSiteIds(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        this.addDbRouteInfo(pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId());
        Object[] objs = this.buildStorePickupSiteOrderQuerySqlAndParams(pickupSiteOrderQueryParam);
        String querySql = (String) objs[0];
        List<Object> params = (List<Object>) objs[1];
        return this.query.mysqlList2(new Class[]{StoreOrder.class, StoreOrderDelivery.class}, querySql, 0, 10000, params, (rs, rowNum) -> {
            StoreOrder storeOrder = Query.getRowMapper(StoreOrder.class).mapRow(rs, rowNum);
            StoreOrderDelivery storeOrderDelivery = Query.getRowMapper(StoreOrderDelivery.class).mapRow(rs, rowNum);
            storeOrder.setStoreOrderDelivery(storeOrderDelivery);
            storeOrderDelivery.setDeliveryStatus(storeOrder.getDeliveryStatus());
            return storeOrder;
        });
    }

    public int countStorePickupSiteOrderForDelivery(int merchantId, long storeId, long staffId, int tradeStatus) {
        this.addDbRouteInfo(merchantId, storeId);
        DALStatus.setSlaveMode();
        StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
        pickupSiteOrderQueryParam.setMerchantId(merchantId);
        pickupSiteOrderQueryParam.setStoreId(storeId);
        pickupSiteOrderQueryParam.setStaffId(staffId);
        pickupSiteOrderQueryParam.setDeliveryStatus(tradeStatus);
        pickupSiteOrderQueryParam.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
        pickupSiteOrderQueryParam.setContainDeliveryInfo(true);
        pickupSiteOrderQueryParam.setContainPickupSiteInfo(true);
        List<StorePickupSiteDeliveryDTO> storePickupSiteDeliveryDTOs = this.getPickupSiteDeliveryOrdersforDelivery(pickupSiteOrderQueryParam);
        if (CollectionUtils.isNotEmpty(storePickupSiteDeliveryDTOs)) {
            return storePickupSiteDeliveryDTOs.size();
        }
        return 0;
    }

    public List<StoreOrderDeliveryCountDTO> getStoreOrderDeliveryCountForThirdPart(int merchantId, long storeId, int deliveryStatus) {
        String storeOrderTableRealName = StoreOrderHelper.transToRealTableName(merchantId, storeId, StoreOrder.class);
        String storeOrderDeliveryTableRealName = StoreOrderHelper.transToRealTableName(merchantId, storeId, StoreOrderDelivery.class);
        StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
        pickupSiteOrderQueryParam.setMerchantId(merchantId);
        pickupSiteOrderQueryParam.setStoreId(storeId);
        pickupSiteOrderQueryParam.setDeliveryStatus(deliveryStatus);
        pickupSiteOrderQueryParam.setContainPickupSiteInfo(false);
        pickupSiteOrderQueryParam.setContainDeliveryInfo(true);
        StringBuilder querySqlSB = new StringBuilder("select count(tb_store_order_.waimai_type) waimai_total, tb_store_order_.waimai_type as waimai_type from ").append(storeOrderTableRealName).append(" tb_store_order_");
        querySqlSB.append(",").append(storeOrderDeliveryTableRealName).append(" tb_store_order_delivery_ ");
        Object[] objs = this.buildStorePickupSiteOrderQuerySqlAndParams(pickupSiteOrderQueryParam);
        querySqlSB.append(objs[0]);
        querySqlSB.append(" group by tb_store_order_.waimai_type order by null");
        List<Map<String, Object>> resultList = this.getResultMapList(merchantId, storeId, querySqlSB.toString(), ((List<Object>) objs[1]).toArray(), true);
        List<StoreOrderDeliveryCountDTO> storeOrderDeliveryCountDTOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(resultList)) {
            StoreOrderDeliveryCountDTO storeOrderDeliveryCountDTO;
            for (Map<String, Object> resultMap : resultList) {
                storeOrderDeliveryCountDTO = new StoreOrderDeliveryCountDTO();
                if (MapUtils.isNotEmpty(resultMap)) {
                    storeOrderDeliveryCountDTO.setWaimaiType(ObjectUtil.getInt(resultMap.get("waimai_type"), 0));
                    storeOrderDeliveryCountDTO.setCount(ObjectUtil.getInt(resultMap.get("waimai_total"), 0));
                    storeOrderDeliveryCountDTOs.add(storeOrderDeliveryCountDTO);
                }
            }
        }
        return storeOrderDeliveryCountDTOs;
    }

    public List<StoreOrder> getDeliveryOrderList(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam, boolean enableSlave) {
        this.addDbRouteInfo(pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId());
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        Object[] objs = this.buildStorePickupSiteOrderQuerySqlAndParams(pickupSiteOrderQueryParam);
        String querySql = (String) objs[0];
        List<Object> params = (List<Object>) objs[1];
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.mysqlList2(new Class[]{StoreOrder.class, StoreOrderDelivery.class}, querySql, 0, 10000, params, (rs, rowNum) -> {
            StoreOrder storeOrder = Query.getRowMapper(StoreOrder.class).mapRow(rs, rowNum);
            StoreOrderDelivery storeOrderDelivery = Query.getRowMapper(StoreOrderDelivery.class).mapRow(rs, rowNum);
            storeOrder.setStoreOrderDelivery(storeOrderDelivery);
            storeOrderDelivery.setDeliveryStatus(storeOrder.getDeliveryStatus());
            return storeOrder;
        });
    }

    /**
     * 根据自提点ID，获取相应状态的订单ID
     * @return
     */
    public List<String> getOrderIdsByPickupSiteIds(StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam) {
        String storeOrderRealTable = StoreOrderHelper.transToRealTableName(
                pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId(), StoreOrder.class);
        String storeOrderDeliveryTable = StoreOrderHelper.transToRealTableName(
                pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId(), StoreOrderDelivery.class);
        this.addDbRouteInfo(pickupSiteOrderQueryParam.getMerchantId(), pickupSiteOrderQueryParam.getStoreId());
        DALStatus.setSlaveMode();
        Object[] objs = this.buildStorePickupSiteOrderQuerySqlAndParams(pickupSiteOrderQueryParam);
        String whereSql = (String) objs[0];
        List<Object> params = (List<Object>) objs[1];
        StringBuilder querySql = new StringBuilder("select tb_store_order_.order_id as order_id from ").append(storeOrderRealTable).append(" tb_store_order_, ");
        querySql.append(storeOrderDeliveryTable).append(" tb_store_order_delivery_ ").append(whereSql);
        List<String> orderIds = this.query.getJdbcSupport().list(querySql.toString(), params.toArray(), (rs, rowNum) -> rs.getString("order_id"));
        if (CollectionUtils.isNotEmpty(orderIds)) {
           return orderIds;
        }
        return Collections.emptyList();
    }

    public StoreOrderDeliveryCountDTO getPickupSiteCountInfoByStoreId(int merchantId, long storeId, int deliveryStatus) {
        StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
        pickupSiteOrderQueryParam.setMerchantId(merchantId);
        pickupSiteOrderQueryParam.setStoreId(storeId);
        pickupSiteOrderQueryParam.setDeliveryStatus(deliveryStatus);
        pickupSiteOrderQueryParam.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
        pickupSiteOrderQueryParam.setContainDeliveryInfo(true);
        pickupSiteOrderQueryParam.setContainPickupSiteInfo(true);
        List<StorePickupSiteDeliveryDTO> pickupSiteDeliveryDTOs = this.getPickupSiteDeliveryOrdersforDelivery(pickupSiteOrderQueryParam);
        StoreOrderDeliveryCountDTO deliveryCountDTO = new StoreOrderDeliveryCountDTO();
        deliveryCountDTO.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
        if (CollectionUtils.isNotEmpty(pickupSiteDeliveryDTOs)) {
            deliveryCountDTO.setCount(pickupSiteDeliveryDTOs.size());
        } else {
            deliveryCountDTO.setCount(0);
        }
        return deliveryCountDTO;
    }
}
