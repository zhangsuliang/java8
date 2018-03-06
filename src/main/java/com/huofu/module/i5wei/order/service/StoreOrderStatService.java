package com.huofu.module.i5wei.order.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderDeliveryDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderItemDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderOptlogDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import halo.query.Query;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.store5weisetting.StoreCustomerAvgPaymentEnum;
import huofuhelper.util.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StoreOrderStatService {

    @Autowired
    private StoreOrderDAO storeOrderDAO;

    @Autowired
    private StoreOrderItemDAO storeOrderItemDAO;

    @Autowired
    private StoreOrderDeliveryDAO storeOrderDeliveryDAO;

    @Autowired
    private StoreOrderOptlogDAO storeOrderOptlogDAO;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    /**
     * 经营报告--五味订单统计
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    public StoreOrderStatDTO getStoreOrderStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        StoreOrderStatDTO tradeOrder = this.getStoreOrderTakeModeStat(merchantId, storeId, repastDate, timeBucketId);
        StoreOrderStatDTO clientCoupon = this.getStoreOrderClientCouponStat(merchantId, storeId, repastDate, timeBucketId);
        StoreOrderStatDTO enterpriseRebate = this.getStoreOrderEnterpriseRebateStat(merchantId, storeId, repastDate, timeBucketId);
        StoreOrderStatDTO internetRebate = this.getStoreOrderInternetRebateStat(merchantId, storeId, repastDate, timeBucketId);
        StoreOrderStatDTO memberRebate = this.getStoreOrderMemberRebateStat(merchantId, storeId, repastDate, timeBucketId);
        StoreOrderStatDTO totalRebate = this.getStoreOrderTotalRebateStat(merchantId, storeId, repastDate, timeBucketId);
        StoreOrderStatDTO credit = this.getStoreOrderCreditStat(merchantId, storeId, repastDate, timeBucketId);
        StoreOrderStatDTO refund = this.getStoreOrderRefundStat(merchantId, storeId, repastDate, timeBucketId);

        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        result.setTotalOrders(tradeOrder.getTotalOrders());
        result.setTotalPayment(tradeOrder.getTotalPayment());
        result.setDineInOrders(tradeOrder.getDineInOrders());
        result.setTakeOutOrders(tradeOrder.getTakeOutOrders());
        result.setInAndOutOrders(tradeOrder.getInAndOutOrders());
        result.setSendOutOrders(tradeOrder.getSendOutOrders());
        result.setCashierOrders(tradeOrder.getCashierOrders());
        result.setCashierPayment(tradeOrder.getCashierPayment());
        result.setUserOrders(tradeOrder.getUserOrders());
        result.setUserPayment(tradeOrder.getUserPayment());
        result.setClientCouponOrders(clientCoupon.getClientCouponOrders());
        result.setClientCouponPayment(clientCoupon.getClientCouponPayment());
        result.setEnterpriseCouponOrders(enterpriseRebate.getEnterpriseCouponOrders());
        result.setEnterpriseCouponPayment(enterpriseRebate.getEnterpriseCouponPayment());
        result.setInternetCouponOrders(internetRebate.getInternetCouponOrders());
        result.setInternetCouponPayment(internetRebate.getInternetCouponPayment());
        result.setMemberCouponOrders(memberRebate.getMemberCouponOrders());
        result.setMemberCouponPayment(memberRebate.getMemberCouponPayment());
        result.setTotalCouponOrders(totalRebate.getTotalCouponOrders());
        result.setTotalCouponPayment(totalRebate.getTotalCouponPayment());
        result.setTotalCreditPayment(credit.getTotalCreditPayment());
        result.setTotalCreditOrders(credit.getTotalCreditOrders());
        result.setChargeCreditPayment(credit.getDischargeCreditPayment());
        result.setChargeCreditOrders(credit.getDischargeCreditOrders());
        result.setDischargeCreditPayment(credit.getDischargeCreditPayment());
        result.setDischargeCreditOrders(credit.getDischargeCreditOrders());
        result.setCancelCreditPayment(credit.getCancelCreditPayment());
        result.setCancelCreditOrders(credit.getCancelCreditOrders());
        result.setServeCreditPayment(credit.getServeCreditPayment());
        result.setServeCreditOrders(credit.getServeCreditOrders());
        result.setTotalRefundorders(refund.getTotalRefundorders());
        result.setPartRefundorders(refund.getPartRefundorders());
        result.setAllRefundorders(refund.getAllRefundorders());
        result.setFrontAllOrders(tradeOrder.getTotalOrders() - refund.getAllRefundorders());

        StoreOrderStatDTO customerStat = getStoreOrderCustomerStat(merchantId, storeId, repastDate, timeBucketId, result);
        result.setEnableCustomerTraffic(customerStat.isEnableCustomerTraffic());
        result.setCustomerAmount(customerStat.getCustomerAmount());
        return result;
    }

    /**
     * 增加客单价的统计
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    private StoreOrderStatDTO getStoreOrderCustomerStat(int merchantId, long storeId, long repastDate, long timeBucketId, StoreOrderStatDTO storeOrderStatDTO) {
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        //查询客单价计算规则
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        int customerAvgPaymentModel = store5weiSetting.getCustomerAvgPaymentModel();
        //统计标记入客数售卖的份数
        int customerAmount = 0;
        //是否按入客数统计
        boolean enableCustomerTraffic = false;
        if (customerAvgPaymentModel == StoreCustomerAvgPaymentEnum.ORDER.getValue()) {
            customerAmount = storeOrderStatDTO.getFrontAllOrders();
            enableCustomerTraffic = false;
        } else if (customerAvgPaymentModel == StoreCustomerAvgPaymentEnum.CUSTOMER_TRAFFIC.getValue()) {
            //直接统计订单上的入客数
            customerAmount = getCustomerAmountByOrder(merchantId, storeId, repastDate, timeBucketId);
            enableCustomerTraffic = true;
        }
        result.setCustomerAmount(customerAmount);
        result.setEnableCustomerTraffic(enableCustomerTraffic);
        return result;
    }

    /**
     * 统计订单上的入客数
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    private int getCustomerAmountByOrder(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select sum(customer_traffic) as customerTraffic ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and credit_status in (?,?,?) and refund_status not in (?,?) ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        params.add(StoreOrderCreditStatusEnum.CHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
        params.add(StoreOrderRefundStatusEnum.USER_ALL.getValue());
        params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
        Map<String, Object> resultMap = storeOrderDAO.getResultMap(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (resultMap != null) {
            Object customerTraffic = resultMap.get("customerTraffic");
            if (customerTraffic != null) {
                return Integer.valueOf(customerTraffic.toString());
            }
        }
        return 0;
    }

    /**
     * 根据 入客数标记 查询订单项, 得道销售的入客数
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    @Deprecated
    private int getCustomerAmount(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        List<String> orderIds = this.getStoreOrderIds4Customer(merchantId, storeId, repastDate, timeBucketId);
        if (orderIds == null || orderIds.size() <= 0) {
            return 0;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        String orderInSql = Query.createInSql("order_id", orderIds.size());
        sql.append(" SELECT store_id, charge_item_id, charge_item_name,amount, unit, enable_customer_traffic ");
        sql.append(" from ").append(storeOrderItemDAO.getRealName(merchantId, storeId));
        //设置查询条件是 设置enable_customer_traffic = 1
        sql.append(" where merchant_id=? and store_id=? and enable_customer_traffic = ? and ").append(orderInSql);
        params.add(merchantId);
        params.add(storeId);
        params.add(true);
        params.addAll(orderIds);
        List<Map<String, Object>> list = storeOrderItemDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        double amount = 0;
        for (Map<String, Object> map : list) {
            if (map.get("amount") != null) {
                amount += Double.valueOf(map.get("amount").toString());
            }
        }
        return Integer.parseInt(new java.text.DecimalFormat("0").format(amount));

    }

    public List<StoreOrderPaymentStatDTO> getStoreOrderPaymentDayStat(int merchantId, long storeId, long repastDate) {
        List<StoreOrderPaymentStatDTO> payList = this.getStoreOrderPayDayStat(merchantId, storeId, repastDate);
        if (payList == null || payList.isEmpty()) {
            return new ArrayList<StoreOrderPaymentStatDTO>();
        }
        List<StoreOrderPaymentStatDTO> refundList = this.getStoreOrderRefundStatDay(merchantId, storeId, repastDate);
        if (refundList == null || refundList.isEmpty()) {
            refundList = new ArrayList<StoreOrderPaymentStatDTO>();
        }
        Map<Long, StoreOrderPaymentStatDTO> refundMap = new HashMap<Long, StoreOrderPaymentStatDTO>();
        for (StoreOrderPaymentStatDTO refundDto : refundList) {
            refundMap.put(refundDto.getTimeBucketId(), refundDto);
        }
        for (StoreOrderPaymentStatDTO payDto : payList) {
            long timeBucketId = payDto.getTimeBucketId();
            if (refundMap.containsKey(timeBucketId)) {
                StoreOrderPaymentStatDTO refundDto = refundMap.get(timeBucketId);
                int allRefundOrders = refundDto.getAllRefundOrders();
                int allPayOrders = payDto.getOrders() - allRefundOrders;
                payDto.setAllPayOrders(allPayOrders);
                payDto.setAllRefundOrders(allRefundOrders);
            } else {
                payDto.setAllPayOrders(payDto.getOrders());
            }
        }
        return payList;
    }

    public List<StoreOrderPaymentStatDTO> getStoreOrderPayDayStat(int merchantId, long storeId, long repastDate) {
        boolean enableSlave = true;
        List<StoreOrderPaymentStatDTO> resultList = new ArrayList<StoreOrderPaymentStatDTO>();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return resultList;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select time_bucket_id,count(*) as orders, sum(actual_price) as actual_price ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? and credit_status in (?,?,?) ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        params.add(StoreOrderCreditStatusEnum.CHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
        sql.append(" and trade_status>? ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        sql.append(" group by time_bucket_id ");
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return resultList;
        }
        int totalOrders = 0;
        long totalPayment = 0;
        for (Map<String, Object> data : list) {
            StoreOrderPaymentStatDTO result = new StoreOrderPaymentStatDTO();
            long timeBucketId = ObjectUtil.getLong(data, "time_bucket_id");
            int orders = ObjectUtil.getInt(data, "orders");
            long actualPayment = ObjectUtil.getLong(data, "actual_price");
            result.setMerchantId(merchantId);
            result.setStoreId(storeId);
            result.setRepastDate(repastDate);
            result.setTimeBucketId(timeBucketId);
            result.setOrders(orders);
            result.setPayment(actualPayment);
            resultList.add(result);
            totalOrders = totalOrders + orders;
            totalPayment = totalPayment + actualPayment;
        }
        StoreOrderPaymentStatDTO result = new StoreOrderPaymentStatDTO();
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(0);
        result.setOrders(totalOrders);
        result.setPayment(totalPayment);
        resultList.add(result);
        return resultList;
    }

    /**
     * 退款订单列表交班统计
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     */
    public List<StoreOrderPaymentStatDTO> getStoreOrderRefundStatDay(int merchantId, long storeId, long repastDate) {
        boolean enableSlave = true;
        List<StoreOrderPaymentStatDTO> resultList = new ArrayList<StoreOrderPaymentStatDTO>();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return resultList;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select time_bucket_id, count(*) as orders ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        sql.append(" and refund_status = ? and trade_status>? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        sql.append(" group by time_bucket_id ");
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return resultList;
        }
        int totalRefundOrders = 0; // 全额退款--订单数
        for (Map<String, Object> data : list) {
            long timeBucketId = ObjectUtil.getInt(data, "time_bucket_id");
            int allRefundOrders = ObjectUtil.getInt(data, "orders");
            StoreOrderPaymentStatDTO result = new StoreOrderPaymentStatDTO();
            result.setMerchantId(merchantId);
            result.setStoreId(storeId);
            result.setRepastDate(repastDate);
            result.setTimeBucketId(timeBucketId);
            result.setAllRefundOrders(allRefundOrders);
            resultList.add(result);
            totalRefundOrders = totalRefundOrders + allRefundOrders;
        }
        StoreOrderPaymentStatDTO totalResult = new StoreOrderPaymentStatDTO();
        totalResult.setMerchantId(merchantId);
        totalResult.setStoreId(storeId);
        totalResult.setRepastDate(repastDate);
        totalResult.setTimeBucketId(0);
        totalResult.setAllRefundOrders(totalRefundOrders);
        resultList.add(totalResult);
        return resultList;
    }

    public Map<Long, Integer> getStoreOrdersDayStat(int merchantId, long storeId, long repastDate) {
        boolean enableSlave = true;
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return new HashMap<Long, Integer>();
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select time_bucket_id, count(*) as orders ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        sql.append(" and pay_status=? and refund_status in (?,?) and trade_status=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        params.add(StoreOrderRefundStatusEnum.NOT.getValue());
        params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        sql.append(" group by time_bucket_id ");
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return new HashMap<Long, Integer>();
        }
        Map<Long, Integer> resultMap = new HashMap<Long, Integer>();
        int totalOrders = 0; // 全天订单数
        for (Map<String, Object> data : list) {
            long timeBucketId = ObjectUtil.getInt(data, "time_bucket_id");
            int orders = ObjectUtil.getInt(data, "orders");
            resultMap.put(timeBucketId, orders);
            totalOrders = totalOrders + orders;
        }
        resultMap.put(0L, totalOrders);
        return resultMap;
    }

    /**
     * 支付订单列表--五味订单支付明细统计
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    public List<String> getStoreOrderIds4PayStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        List<String> result = new ArrayList<String>();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select order_id ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and pay_status=? and trade_status>? and credit_status=? ");
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> data : list) {
            Object order_id = data.get("order_id");
            result.add(order_id.toString());
        }
        return result;
    }

    /**
     * 退款订单列表--五味订单退款明细统计
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    public List<String> getStoreOrderIds4RefundStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return new ArrayList<String>();
        }
        List<String> result = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select order_id ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and refund_status in (?,?) and trade_status>? ");
        params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
        params.add(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> obj : list) {
            String orderId = obj.get("order_id").toString();
            result.add(orderId);
        }
        return result;
    }

    /**
     * 支付订单列表--五味订单支付明细统计（不包含全额退款）
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    @Deprecated
    public List<String> getStoreOrderIds4Customer(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        List<String> result = new ArrayList<String>();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select order_id "); //TODO 修改查询条件, 直接sum(customer_traffic) 得到入客数
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and credit_status in (?,?,?) and refund_status not in (?,?) ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        params.add(StoreOrderCreditStatusEnum.CHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
        params.add(StoreOrderRefundStatusEnum.USER_ALL.getValue());
        params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> data : list) {
            Object order_id = data.get("order_id");
            result.add(order_id.toString());
        }
        return result;
    }

    /**
     * 经营报告--五味订单取餐统计
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @return
     */
    public StoreOrderStatDTO getStoreOrderTakeModeStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select client_type,take_mode,count(*) as orders, sum(actual_price) as actual_price ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? and credit_status in (?,?,?) ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        params.add(StoreOrderCreditStatusEnum.CHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        sql.append(" group by client_type,take_mode ");
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        int totalOrders = 0;
        long totalPayment = 0;
        int dineInOrders = 0;
        int takeOutOrders = 0;
        int inAndOutOrders = 0;
        int sendOutOrders = 0;
        int cashierOrders = 0;
        long cashierPayment = 0;
        int userOrders = 0;
        long userPayment = 0;
        for (Map<String, Object> data : list) {
            int clientType = ObjectUtil.getInt(data.get("client_type"), 0);
            int takeMode = ObjectUtil.getInt(data.get("take_mode"), 0);
            int orders = ObjectUtil.getInt(data.get("orders"), 0);
            long actualPrice = ObjectUtil.getLong(data.get("actual_price"), 0);
            totalOrders = totalOrders + orders;
            totalPayment = totalPayment + actualPrice;
            if (clientType == ClientTypeEnum.CASHIER.getValue()) {
                cashierOrders = cashierOrders + orders;
                cashierPayment = cashierPayment + actualPrice;
            } else {
                userOrders = userOrders + orders;
                userPayment = userPayment + actualPrice;
            }
            if (takeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
                dineInOrders = dineInOrders + orders;
            } else if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
                //快取暂时放外带这儿
                takeOutOrders = takeOutOrders + orders;
            } else if (takeMode == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
                inAndOutOrders = inAndOutOrders + orders;
            } else if (takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                sendOutOrders = sendOutOrders + orders;
            }
        }
        result.setTotalOrders(totalOrders);
        result.setTotalPayment(totalPayment);
        result.setDineInOrders(dineInOrders);
        result.setTakeOutOrders(takeOutOrders);
        result.setInAndOutOrders(inAndOutOrders);
        result.setSendOutOrders(sendOutOrders);
        result.setCashierOrders(cashierOrders);
        result.setCashierPayment(cashierPayment);
        result.setUserOrders(userOrders);
        result.setUserPayment(userPayment);
        return result;
    }

    /**
     * 收银台收款线订单统计信息
     */
    public List<StoreOrderStatDTO> getStoreOrderChannelTradeSummaryStat(int merchantId, long storeId, long repastDate, long timeBucketId, List<Long> channelIds) {
        boolean enableSlave = true;
        List<StoreOrderStatDTO> result = Lists.newArrayList();
        if (merchantId == 0 || storeId == 0 || repastDate == 0 || channelIds == null) {
            return result;
        }
        List<Object> params = Lists.newArrayList();
        StringBuilder sql = new StringBuilder();
        sql.append(" select cashier_channel_id, count(*) as orders ,sum(actual_price) as actual_price ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=?  ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and take_serial_number > 0 ");
        sql.append(" and (");
        sql.append(" credit_status in (?,?) ");
        params.add(StoreOrderCreditStatusEnum.CHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
        sql.append(" or pay_status = ? ");
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        sql.append(" )");
        sql.append(" and refund_status in (?,?)");
        params.add(StoreOrderRefundStatusEnum.NOT.getValue());
        params.add(StoreOrderRefundStatusEnum.FAILURE.getValue());
        sql.append(" and cashier_channel_id in (");
        boolean isFirst = true;
        for (long channelId : channelIds) {
            if (isFirst) {
                sql.append("?");
                isFirst = false;
            } else {
                sql.append(",?");
            }
            params.add(channelId);
        }
        sql.append(") group by cashier_channel_id");

        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        int totalOrders = 0;
        long totalPayment = 0;
        Map<Long, StoreOrderStatDTO> map = Maps.newHashMap();
        for (Map<String, Object> data : list) {
            int orders = ObjectUtil.getInt(data.get("orders"), 0);
            long payment = ObjectUtil.getLong(data.get("actual_price"), 0);
            long cashierChannelId = ObjectUtil.getLong(data.get("cashier_channel_id"),0);
            StoreOrderStatDTO dto = new StoreOrderStatDTO();
            dto.setCashierChannelId(cashierChannelId);
            dto.setTotalOrders(orders);
            dto.setTotalPayment(payment);
            map.put(cashierChannelId,dto);
            totalOrders = totalOrders + orders;
            totalPayment = totalPayment + payment;
        }
        for (long channelId : channelIds){
            StoreOrderStatDTO dto = map.get(channelId);
            if (dto == null){
                dto = new StoreOrderStatDTO();
                dto.setCashierChannelId(channelId);
                dto.setTotalOrders(0);
                dto.setTotalPayment(0);
            }
            result.add(dto);
        }
        StoreOrderStatDTO dto = new StoreOrderStatDTO();
        dto.setCashierChannelId(0);
        dto.setTotalOrders(totalOrders);
        dto.setTotalPayment(totalPayment);
        result.add(dto);
        return result;
    }


    public StoreOrderStatDTO getStoreOrderClientCouponStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select count(*) as client_coupon_orders, sum(user_client_coupon) as client_coupon ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and user_client_coupon>0 ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        Map<String, Object> data = list.get(0);
        Object client_coupon_orders = data.get("client_coupon_orders");
        Object client_coupon = data.get("client_coupon");
        if (client_coupon_orders != null) {
            result.setClientCouponOrders(ObjectUtil.getInt(client_coupon_orders, 0));
        }
        if (client_coupon != null) {
            result.setClientCouponPayment(Long.valueOf(client_coupon.toString()));
        }
        return result;
    }

    public StoreOrderStatDTO getStoreOrderEnterpriseRebateStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select count(*) as enterprise_coupon_orders, sum(enterprise_rebate_price) as enterprise_coupon ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and enterprise_rebate_price>0 ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        Map<String, Object> data = list.get(0);
        Object enterprise_coupon_orders = data.get("enterprise_coupon_orders");
        Object enterprise_coupon = data.get("enterprise_coupon");
        if (enterprise_coupon_orders != null) {
            result.setEnterpriseCouponOrders(Integer.valueOf(enterprise_coupon_orders.toString()));
        }
        if (enterprise_coupon != null) {
            result.setEnterpriseCouponPayment(Long.valueOf(enterprise_coupon.toString()));
        }
        return result;
    }

    public StoreOrderStatDTO getStoreOrderInternetRebateStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select count(*) as internet_coupon_orders, sum(internet_rebate_price) as internet_coupon ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and internet_rebate_price>0 ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        Map<String, Object> data = list.get(0);
        Object internet_coupon_orders = data.get("internet_coupon_orders");
        Object internet_coupon = data.get("internet_coupon");
        if (internet_coupon_orders != null) {
            result.setInternetCouponOrders(Integer.valueOf(internet_coupon_orders.toString()));
        }
        if (internet_coupon != null) {
            result.setInternetCouponPayment(Long.valueOf(internet_coupon.toString()));
        }
        return result;
    }

    public StoreOrderStatDTO getStoreOrderTotalRebateStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select count(*) as total_coupon_orders, sum(total_rebate_price) as total_coupon ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and total_rebate_price>0 ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        Map<String, Object> data = list.get(0);
        Object total_coupon_orders = data.get("total_coupon_orders");
        Object total_coupon = data.get("total_coupon");
        if (total_coupon_orders != null) {
            result.setTotalCouponOrders(Integer.valueOf(total_coupon_orders.toString()));
        }
        if (total_coupon != null) {
            result.setTotalCouponPayment(Long.valueOf(total_coupon.toString()));
        }
        return result;
    }

    public StoreOrderStatDTO getStoreOrderMemberRebateStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select count(*) as member_coupon_orders, sum(member_rebate_price) as member_coupon ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and member_rebate_price>0 ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        Map<String, Object> data = list.get(0);
        Object member_coupon_orders = data.get("member_coupon_orders");
        Object member_coupon = data.get("member_coupon");
        if (member_coupon_orders != null) {
            result.setMemberCouponOrders(Integer.valueOf(member_coupon_orders.toString()));
        }
        if (member_coupon != null) {
            result.setMemberCouponPayment(Long.valueOf(member_coupon.toString()));
        }
        return result;
    }

    public StoreOrderStatDTO getStoreOrderCreditStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        result.setMerchantId(merchantId);
        result.setStoreId(storeId);
        result.setRepastDate(repastDate);
        result.setTimeBucketId(timeBucketId);
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select credit_status, credit_type, count(*) as orders, sum(actual_price) as actual_price ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and trade_status>? and credit_status in (?,?,?) ");
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        params.add(StoreOrderCreditStatusEnum.CHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.CANCEL_CHARGE.getValue());
        sql.append(" group by credit_status, credit_type ");
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        long totalCreditPayment = 0; // 赊账--总金额
        int totalCreditOrders = 0; // 赊账--订单总数
        long chargeCreditPayment = 0; // 未销账--金额
        int chargeCreditOrders = 0; // 未销账--订单数
        long dischargeCreditPayment = 0; // 销账--金额
        int dischargeCreditOrders = 0; // 销账--订单数
        long cancelCreditPayment = 0; // 赊账撤销--金额
        int cancelCreditOrders = 0; // 招待赊账--订单数
        long serveCreditPayment = 0; // 招待赊账--金额
        int serveCreditOrders = 0; // 赊账撤销--订单数
        for (Map<String, Object> data : list) {
            int creditStatus = ObjectUtil.getInt(data.get("credit_status"), 0);
            int creditType = ObjectUtil.getInt(data.get("credit_type"), 0);
            int orders = ObjectUtil.getInt(data.get("orders"), 0);
            long actualPrice = ObjectUtil.getLong(data.get("actual_price"), 0);
            if (creditStatus == StoreOrderCreditStatusEnum.CHARGE.getValue()) {
                chargeCreditOrders = chargeCreditOrders + orders;
                chargeCreditPayment = chargeCreditPayment + actualPrice;
            } else if (creditStatus == StoreOrderCreditStatusEnum.DISCHARGE.getValue()) {
                dischargeCreditOrders = dischargeCreditOrders + orders;
                dischargeCreditPayment = dischargeCreditPayment + actualPrice;
            } else if (creditStatus == StoreOrderCreditStatusEnum.CANCEL_CHARGE.getValue()) {
                cancelCreditOrders = cancelCreditOrders + orders;
                cancelCreditPayment = cancelCreditPayment + actualPrice;
            }
            if (creditType == 2 && creditStatus != StoreOrderCreditStatusEnum.CANCEL_CHARGE.getValue()) {
                //赊账类型：2＝公关费用
                serveCreditOrders = serveCreditOrders + orders;
                serveCreditPayment = serveCreditPayment + actualPrice;
            }
            //未取消赊账的计入总赊账
            if (creditStatus != StoreOrderCreditStatusEnum.CANCEL_CHARGE.getValue()) {
                totalCreditOrders = totalCreditOrders + orders;
                totalCreditPayment = totalCreditPayment + actualPrice;
            }
        }
        result.setTotalCreditPayment(totalCreditPayment);
        result.setTotalCreditOrders(totalCreditOrders);
        result.setChargeCreditPayment(dischargeCreditPayment);
        result.setChargeCreditOrders(dischargeCreditOrders);
        result.setDischargeCreditPayment(dischargeCreditPayment);
        result.setDischargeCreditOrders(dischargeCreditOrders);
        result.setCancelCreditPayment(cancelCreditPayment);
        result.setCancelCreditOrders(cancelCreditOrders);
        result.setServeCreditPayment(serveCreditPayment);
        result.setServeCreditOrders(serveCreditOrders);
        return result;
    }

    /**
     * 退款订单列表交班统计
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     */
    public StoreOrderStatDTO getStoreOrderRefundStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        StoreOrderStatDTO result = new StoreOrderStatDTO();
        if (merchantId == 0 || storeId == 0 || repastDate == 0) {
            return result;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select refund_status, count(*) as orders, sum(actual_price) as actual_price ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and repast_date=? and credit_status in (?,?,?) ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        params.add(StoreOrderCreditStatusEnum.CHARGE.getValue());
        params.add(StoreOrderCreditStatusEnum.DISCHARGE.getValue());
        if (timeBucketId > 0) {
            sql.append(" and time_bucket_id=? ");
            params.add(timeBucketId);
        }
        sql.append(" and refund_status in (?,?) and trade_status>? ");
        params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
        params.add(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        sql.append(" group by refund_status ");
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        int totalRefundorders = 0; // 退款--订单总数
        int partRefundorders = 0; // 部分退款--订单数
        int allRefundorders = 0; // 全额退款--订单数
        for (Map<String, Object> data : list) {
            int refundStatus = ObjectUtil.getInt(data.get("refund_status"), 0);
            int orders = ObjectUtil.getInt(data.get("orders"), 0);
            //long actualPrice = ObjectUtil.getLong(data.get("actual_price"), 0);
            if (refundStatus == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue()) {
                allRefundorders = allRefundorders + orders;
            } else if (refundStatus == StoreOrderRefundStatusEnum.MERCHANT_PART.getValue()) {
                partRefundorders = partRefundorders + orders;
            }
            totalRefundorders = totalRefundorders + orders;
        }
        result.setTotalRefundorders(totalRefundorders);
        result.setPartRefundorders(partRefundorders);
        result.setAllRefundorders(allRefundorders);
        return result;
    }

    /**
     * 外送统计交班统计
     *
     * @param merchantId
     * @param storeId
     * @param countTimeBegin
     * @param countTimeEnd
     */
    public List<StoreOrder4DeliveryCountDTO> getStoreOrder4DeliveryCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) {
        boolean enableSlave = true;
        List<StoreOrder4DeliveryCountDTO> result = new ArrayList<StoreOrder4DeliveryCountDTO>();
        if (merchantId == 0 || storeId == 0 || countTimeBegin == 0 || countTimeEnd == 0) {
            return new ArrayList<StoreOrder4DeliveryCountDTO>();
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select delivery_staff_id,count(*) as total_order_count, sum(a.favorable_price+a.delivery_fee) as total_order_price, sum(a.delivery_fee) as total_delivery_fee ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId)).append(" a inner join ").append(storeOrderDeliveryDAO.getRealName(merchantId, storeId)).append(" b on a.order_id=b.order_id and b.delivery_staff_id>0 ");
        sql.append(" where a.merchant_id=? and a.store_id=? and a.create_time between ? and ? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(countTimeBegin);
        params.add(countTimeEnd);
        sql.append(" group by b.delivery_staff_id ");
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> obj : list) {
            StoreOrder4DeliveryCountDTO countVO = new StoreOrder4DeliveryCountDTO();
            Object staff_id = obj.get("delivery_staff_id");
            Object total_delivery_fee = obj.get("total_delivery_fee");
            Object total_order_count = obj.get("total_order_count");
            Object total_order_price = obj.get("total_order_price");
            countVO.setMerchantId(merchantId);
            countVO.setStoreId(storeId);
            countVO.setStaffId(Long.valueOf(staff_id.toString()));
            countVO.setTotalDeliveryFee(Long.valueOf(total_delivery_fee.toString()));
            countVO.setTotalOrderCount(Integer.valueOf(total_order_count.toString()));
            countVO.setTotalOrderPrice(Long.valueOf(total_order_price.toString()));
            result.add(countVO);
        }
        return result;
    }

    /**
     * 取消订单交班统计
     *
     * @param merchantId
     * @param storeId
     * @param countTimeBegin
     * @param countTimeEnd
     * @return
     */
    public List<StoreOrder4CancelCountDTO> getStoreOrder4CancelCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) {
        boolean enableSlave = true;
        List<StoreOrder4CancelCountDTO> result = new ArrayList<StoreOrder4CancelCountDTO>();
        if (merchantId == 0 || storeId == 0 || countTimeBegin == 0 || countTimeEnd == 0) {
            return new ArrayList<StoreOrder4CancelCountDTO>();
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select staff_id, count(*) as cancel_count ");
        sql.append(" from ").append(storeOrderOptlogDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and create_time between ? and ?  and opt_type=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(countTimeBegin);
        params.add(countTimeEnd);
        params.add(StoreOrderOptlogTypeEnum.CASHIER_CANCEL_ORDER.getValue());
        sql.append(" group by staff_id ");
        List<Map<String, Object>> list = storeOrderOptlogDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> obj : list) {
            StoreOrder4CancelCountDTO countVO = new StoreOrder4CancelCountDTO();
            Object staff_id = obj.get("staff_id");
            Object cancel_count = obj.get("cancel_count");
            countVO.setMerchantId(merchantId);
            countVO.setStoreId(storeId);
            countVO.setStaffId(Long.valueOf(staff_id.toString()));
            countVO.setCancelOrderCount(Integer.valueOf(cancel_count.toString()));
            result.add(countVO);
        }
        return result;

    }

    /**
     * 现场收单订单列表交班统计
     *
     * @param merchantId
     * @param storeId
     * @param countTimeBegin
     * @param countTimeEnd
     */
    public List<String> getStoreOrderIds4CashierCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) {
        boolean enableSlave = true;
        if (merchantId == 0 || storeId == 0 || countTimeBegin == 0 || countTimeEnd == 0) {
            return new ArrayList<String>();
        }
        List<String> result = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select order_id ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and create_time between ? and ? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(countTimeBegin);
        params.add(countTimeEnd);
        sql.append(" and client_type=? and pay_status=? and trade_status>? and credit_status=? ");
        params.add(ClientTypeEnum.CASHIER.getValue());
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> obj : list) {
            String orderId = obj.get("order_id").toString();
            result.add(orderId);
        }
        return result;
    }

    /**
     * 营收订单列表交班统计
     *
     * @param merchantId
     * @param storeId
     * @param countTimeBegin
     * @param countTimeEnd
     */
    public List<String> getStoreOrderIds4RevenueCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) {
        boolean enableSlave = true;
        if (merchantId == 0 || storeId == 0 || countTimeBegin == 0 || countTimeEnd == 0) {
            return new ArrayList<String>();
        }
        List<String> result = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select order_id ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and create_time between ? and ? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(countTimeBegin);
        params.add(countTimeEnd);
        sql.append(" and pay_status=? and trade_status>? and credit_status=? ");
        params.add(StoreOrderPayStatusEnum.FINISH.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        params.add(StoreOrderCreditStatusEnum.NO_CREDIT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> obj : list) {
            String orderId = obj.get("order_id").toString();
            result.add(orderId);
        }
        return result;
    }

    /**
     * 退款订单列表交班统计
     *
     * @param merchantId
     * @param storeId
     * @param countTimeBegin
     * @param countTimeEnd
     */
    public List<String> getStoreOrderIds4RefundCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) {
        boolean enableSlave = true;
        if (merchantId == 0 || storeId == 0 || countTimeBegin == 0 || countTimeEnd == 0) {
            return new ArrayList<String>();
        }
        List<String> result = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append(" select order_id ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and create_time between ? and ? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(countTimeBegin);
        params.add(countTimeEnd);
        sql.append(" and refund_status in (?,?) and trade_status>? ");
        params.add(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
        params.add(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
        params.add(StoreOrderTradeStatusEnum.NOT.getValue());
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return result;
        }
        for (Map<String, Object> obj : list) {
            String orderId = obj.get("order_id").toString();
            result.add(orderId);
        }
        return result;
    }

    public List<StoreOrderItemStatDTO> getStoreOrderItemsCatStat(int merchantId, long storeId, long repastDate, long timeBucketId) {
        boolean enableSlave = true;
        List<StoreOrderItemStatDTO> resultList = new ArrayList<StoreOrderItemStatDTO>();
        List<String> orderIds = this.getStoreOrderIds4PayStat(merchantId, storeId, repastDate, timeBucketId);
        if (orderIds == null || orderIds.isEmpty()) {
            return resultList;
        }
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        String orderInSql = Query.createInSql("order_id", orderIds.size());
        sql.append(" SELECT store_id, charge_item_id, charge_item_name, unit, price as charge_item_price, spicy_level, rebate_able, sum(amount) as amount, sum(packed_amount) as packed_amount ");
        sql.append(" from ").append(storeOrderItemDAO.getRealName(merchantId, storeId));
        sql.append(" where merchant_id=? and store_id=? and ").append(orderInSql);
        sql.append(" group by charge_item_id ");
        params.add(merchantId);
        params.add(storeId);
        params.addAll(orderIds);
        List<Map<String, Object>> list = storeOrderItemDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), enableSlave);
        if (list == null || list.isEmpty()) {
            return resultList;
        }
        for (Map<String, Object> map : list) {
            StoreOrderItemStatDTO storeOrderItemStatDTO = new StoreOrderItemStatDTO();
            storeOrderItemStatDTO.setMerchantId(merchantId);
            storeOrderItemStatDTO.setStoreId(storeId);
            storeOrderItemStatDTO.setChargeItemId(ObjectUtil.getLong(map, "charge_item_id"));
            storeOrderItemStatDTO.setChargeItemName(ObjectUtil.getString(map, "charge_item_name"));
            storeOrderItemStatDTO.setChargeItemPrice(ObjectUtil.getLong(map, "charge_item_price"));
            storeOrderItemStatDTO.setUnit(ObjectUtil.getString(map, "unit"));
            storeOrderItemStatDTO.setSpicyLevel(ObjectUtil.getInt(map, "spicy_level"));
            storeOrderItemStatDTO.setRebateAble(ObjectUtil.getBoolean(map, "rebate_able"));
            storeOrderItemStatDTO.setAmount(ObjectUtil.getInt(map, "amount"));
            storeOrderItemStatDTO.setPackedAmount(ObjectUtil.getInt(map, "packed_amount"));
            resultList.add(storeOrderItemStatDTO);
        }
        return resultList;
    }

    /**
     * 按时间查询店铺的客单价
     *
     * @param merchantId
     * @param storeId
     * @param queryDateStart
     * @param queryDateEnd
     * @return
     */
    public CustomerAvgPaymentDTO getStoreOrderCustomerAvgPayment(int merchantId, long storeId, long queryDateStart, long queryDateEnd) {

        //根据订单的操作记录 查询订单集合
        List<String> optlogIds = getOrderIdsByOptlog(merchantId, storeId, queryDateStart, queryDateEnd);
        //根据订单集合 得到订单集合和实收的款项  去掉全额退款的订单和订单金额
        List<StoreOrder> orders = getOrderWithoutRefund(merchantId, storeId, optlogIds);

        List<String> orderIds = Lists.newArrayList();
        long totalPayment = 0;
        for (StoreOrder order : orders) {
            orderIds.add(order.getOrderId());
            totalPayment += order.getActualPrice();
        }
        //根据上一步筛选的订单集合 得到入客数
        //查询客单价计算规则
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        int customerAvgPaymentModel = store5weiSetting.getCustomerAvgPaymentModel();
        int customerAmount = getCustomerAmountByIds(merchantId, storeId, customerAvgPaymentModel, orderIds);
        //根据收入和入客数 得到客单价

        CustomerAvgPaymentDTO customerAvgPaymentDTO = new CustomerAvgPaymentDTO();
        customerAvgPaymentDTO.setMerchantId(merchantId);
        customerAvgPaymentDTO.setStoreId(storeId);
        customerAvgPaymentDTO.setCustomerAmount(customerAmount);
        if (customerAmount <= 0) {
            customerAvgPaymentDTO.setCustomerAvgPayment(0);
        } else {
            customerAvgPaymentDTO.setCustomerAvgPayment(totalPayment / customerAmount);
        }
        //组装dto
        return customerAvgPaymentDTO;
    }

    private int getCustomerAmountByIds(int merchantId, long storeId, int customerAvgPaymentModel, List<String> orderIds) {

        if (orderIds == null || orderIds.size() <= 0) {
            return 0;
        }
        if (customerAvgPaymentModel == StoreCustomerAvgPaymentEnum.ORDER.getValue()) {
            return orderIds.size();
        } else if (customerAvgPaymentModel == StoreCustomerAvgPaymentEnum.CUSTOMER_TRAFFIC.getValue()) {
            return getCustomerAmount(merchantId, storeId, orderIds);
        }
        return 0;
    }

    private int getCustomerAmount(int merchantId, long storeId, List<String> orderIds) {
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        String orderInSql = Query.createInSql("order_id", orderIds.size());
        sql.append(" SELECT store_id, charge_item_id, charge_item_name,amount, unit, enable_customer_traffic ");
        sql.append(" from ").append(storeOrderItemDAO.getRealName(merchantId, storeId));
        //设置查询条件是 设置enable_customer_traffic = 1
        sql.append(" where merchant_id=? and store_id=? and enable_customer_traffic = ? and ").append(orderInSql);
        params.add(merchantId);
        params.add(storeId);
        params.add(true);
        params.addAll(orderIds);
        List<Map<String, Object>> list = storeOrderItemDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), true);
        double amount = 0;
        for (Map<String, Object> map : list) {
            if (map.get("amount") != null) {
                amount += Double.valueOf(map.get("amount").toString());
            }
        }
        return Integer.parseInt(new java.text.DecimalFormat("0").format(amount));
    }

    /**
     * 查询订单集合 去除掉 全额退款
     *
     * @param merchantId
     * @param storeId
     * @param optlogIds
     */
    private List<StoreOrder> getOrderWithoutRefund(int merchantId, long storeId, List<String> optlogIds) {

        return storeOrderDAO.getStoreOrderWithoutRefund(merchantId, storeId, optlogIds,
                StoreOrderTradeStatusEnum.NOT.getValue(),
                StoreOrderCreditStatusEnum.NO_CREDIT.getValue(),
                StoreOrderCreditStatusEnum.CHARGE.getValue(),
                StoreOrderCreditStatusEnum.DISCHARGE.getValue(),
                StoreOrderRefundStatusEnum.USER_ALL.getValue(),
                StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
    }

    /**
     * 根据订单的操作记录 查询订单集合
     *
     * @param merchantId
     * @param storeId
     * @param queryDateStart
     * @param queryDateEnd
     */
    private List<String> getOrderIdsByOptlog(int merchantId, long storeId, long queryDateStart, long queryDateEnd) {

        return storeOrderOptlogDAO.getOrderIdsByTime(merchantId, storeId,
                StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue(), queryDateStart, queryDateEnd);
    }

    public List<StoreOrderRateOfUserDTO> countStoreOrderRateOfUser(int merchantId, long storeId) {
        Map<Long, StoreOrderRateOfUserDTO> resultMap = new HashMap<Long, StoreOrderRateOfUserDTO>();
        List<Object> params = new ArrayList<Object>();
        StringBuilder sql = new StringBuilder();
        sql.append("select user_id, take_mode, count(*) as num ");
        sql.append(" from ").append(storeOrderDAO.getRealName(merchantId, storeId));
        sql.append(" where store_id=? and user_id>0 and take_serial_number>0 and parent_order_id='' group by user_id,take_mode ");
        params.add(storeId);
        List<Map<String, Object>> list = storeOrderDAO.getResultMapList(merchantId, storeId, sql.toString(), params.toArray(), true);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        for (Map<String, Object> map : list) {
            long userId = ObjectUtil.getLong(map, "user_id");
            int takeMode = ObjectUtil.getInt(map, "take_mode");
            int num = ObjectUtil.getInt(map, "num");
            StoreOrderRateOfUserDTO storeUserVisit = resultMap.get(userId);
            if (storeUserVisit == null) {
                storeUserVisit = new StoreOrderRateOfUserDTO();
                storeUserVisit.setMerchantId(merchantId);
                storeUserVisit.setStoreId(storeId);
                storeUserVisit.setUserId(userId);
                resultMap.put(userId, storeUserVisit);
            }
            if (takeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
                storeUserVisit.setOrdersDineIn(storeUserVisit.getOrdersDineIn() + num);
            } else if (takeMode == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
                storeUserVisit.setOrdersInAndOut(storeUserVisit.getOrdersInAndOut() + num);
            } else if (takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                storeUserVisit.setOrdersSendOut(storeUserVisit.getOrdersSendOut() + num);
            } else if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()
                    || takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
                storeUserVisit.setOrdersTakeOut(storeUserVisit.getOrdersTakeOut() + num);
            }
            storeUserVisit.setOrdersTrade(storeUserVisit.getOrdersTrade() + num);
        }
        return new ArrayList<>(resultMap.values());
    }
}
