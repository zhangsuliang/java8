package com.huofu.module.i5wei.order.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.order.dao.*;
import com.huofu.module.i5wei.order.entity.*;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.prepaidcard.MerchantPrepaidCardOrderCreditTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StoreOrderQueryService {
	
	@Autowired
    private StoreOrderDAO storeOrderDAO;
	
	@Autowired
    private StoreOrderOptlogDAO storeOrderOptlogDAO;
	
	@Autowired
    private StoreOrderItemDAO storeOrderItemDAO;

    @Autowired
    private StoreOrderSubitemDAO storeOrderSubitemDAO;
    
    @Autowired
    private StoreOrderHelper storeOrderHelper;

	@Autowired
	private StoreOrderDeliveryDAO storeOrderDeliveryDAO;
	
	@Autowired
	private StoreOrderSwitchDAO storeOrderSwitchDAO;
	
	/**
	 * 根据查询输入订单ID，得到订单详情（仅包括订单）
	 *
	 * @param merchantId
	 * @param storeId
	 * @param orderId
	 * @return
	 * @throws T5weiException
	 */
	public StoreOrder getStoreOrderById(int merchantId, long storeId, String orderId) throws T5weiException {
		boolean enableSlave = false;
		StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, orderId, false, false);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[] { storeId, orderId }));
		}
		// 设置订单个状态的时间点
		storeOrderHelper.setStoreOrderTimes(storeOrder, enableSlave);
		storeOrderHelper.setStoreOrderDetail(storeOrder, enableSlave);
		return storeOrder;
	}

	/**
	 * 根据取餐码查询订单
	 *
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @param takeCode
	 * @return
	 * @throws T5weiException
	 */
	public StoreOrder getStoreOrderDetailByTakeCode(int merchantId, long storeId, long repastDate, String takeCode) throws T5weiException {
		boolean enableSlave = false;
		if (repastDate == 0) {
			repastDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		}
		StoreOrder storeOrder = storeOrderDAO.getStoreOrderByTakeCode(merchantId, storeId, repastDate, takeCode, enableSlave);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REPAST_DATE_CODE_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, repastDate=#2, takeCode=#3 ",
					new Object[] { storeId, repastDate, takeCode }));
		}
		storeOrderHelper.setStoreOrderDetail(storeOrder, enableSlave);
		return storeOrder;
	}

	public List<StoreOrder> getStoreOrders(int merchantId, long storeId, List<String> orderIds) {
		boolean enableSlave = true;
		List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrdersById(merchantId, storeId, orderIds, enableSlave);
		return storeOrders;
	}
	
	public Map<String, StoreOrder> getStoreOrderMapInIds(int merchantId, long storeId, List<String> orderIds) throws T5weiException {
		boolean enableSlave = false;
		List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrdersInIds(merchantId, storeId, orderIds, enableSlave);
		Map<String, StoreOrder> orderMap = new HashMap<String, StoreOrder>();
		if (storeOrders == null || storeOrders.isEmpty()) {
			return orderMap;
		}
		storeOrderHelper.setStoreOrderTimeBucket(storeOrders, enableSlave);
		storeOrderHelper.setStoreOrderTimes(storeOrders, enableSlave);
		storeOrderHelper.setStoreOrderDelivery(storeOrders, enableSlave);
		storeOrderHelper.setStoreOrderTableRecord(storeOrders, enableSlave);
		for (StoreOrder storeOrder : storeOrders) {
			orderMap.put(storeOrder.getOrderId(), storeOrder);
		}
		return orderMap;
	}
	
	public Map<String, StoreOrder> getStoreOrderSimpleMapInIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave){
	    List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrdersInIds(merchantId, storeId, orderIds, enableSlave);
        Map<String, StoreOrder> orderMap = new HashMap<String, StoreOrder>();
        if (storeOrders == null || storeOrders.isEmpty()) {
            return orderMap;
        }
        for (StoreOrder storeOrder : storeOrders) {
            orderMap.put(storeOrder.getOrderId(), storeOrder);
        }
        return orderMap;
	}
	
	/**
	 * 查询订单详情列表
	 *
	 * @param storeOrdersQueryParam
	 * @return
	 */
	public List<StoreOrder> getStoreOrders(StoreOrdersQueryParam storeOrdersQueryParam) {
		boolean enableSlave = true;
		List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrderByQueryDTO(storeOrdersQueryParam, enableSlave);
		if (storeOrders == null || storeOrders.isEmpty()) {
			return null;
		}
		storeOrderHelper.setStoreOrderDetail(storeOrders, enableSlave);
		storeOrderHelper.setStoreOrderTimes(storeOrders, enableSlave);
		storeOrderHelper.setStoreOrderTableRecord(storeOrders, enableSlave);
		return storeOrders;
	}
	
	/**
	 * 统计订单列表
	 *
	 * @param storeOrdersQueryParam
	 * @return
	 */
	public int countStoreOrders(StoreOrdersQueryParam storeOrdersQueryParam) {
		boolean enableSlave = true;
		return storeOrderDAO.countStoreOrderByQueryDTO(storeOrdersQueryParam, enableSlave);
	}

	/**
	 * 查询订单详情列表
	 *
	 * @param storeOrdersQueryByStatusParam
	 * @param loadDelivery
	 * @return
	 */
	public List<StoreOrder> getStoreOrders(StoreOrdersQueryByStatusParam storeOrdersQueryByStatusParam, boolean loadDelivery) {
		boolean enableSlave = true;
		List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrderByQueryDTO(storeOrdersQueryByStatusParam, enableSlave);
		if (storeOrders == null || storeOrders.isEmpty()) {
			return null;
		}
		storeOrderHelper.setStoreOrderDetail(storeOrders, enableSlave);
		storeOrderHelper.setStoreOrderTimes(storeOrders, enableSlave);
		storeOrderHelper.setStoreOrderTableRecord(storeOrders, enableSlave);
		if (loadDelivery) {
			storeOrderHelper.setStoreOrderDelivery(storeOrders, enableSlave);
		}
		return storeOrders;
	}
	
	/**
	 * 统计订单列表
	 *
	 * @param storeOrdersQueryByStatusParam
	 * @return
	 */
	public int countStoreOrders(StoreOrdersQueryByStatusParam storeOrdersQueryByStatusParam) {
		boolean enableSlave = true;
		return storeOrderDAO.countStoreOrderByQueryDTO(storeOrdersQueryByStatusParam, enableSlave);
	}

	
	public List<StoreOrder> getStoreOrdersRollback(int merchantId, long storeId) {
		boolean enableSlave = true;
		return storeOrderDAO.getStoreOrdersRollback(merchantId, storeId, enableSlave);
	}
    
    public List<StoreOrder> getStoreOrdersSearch(StoreOrdersSearchParam param) {
        boolean enableSlave = true;
        boolean limit = true;
        List<StoreOrder> storeOrders = storeOrderDAO.getStoreOrdersSearch(param, enableSlave, limit);
        if (storeOrders == null || storeOrders.isEmpty()) {
            return null;
        }
        storeOrderHelper.setStoreOrderTimes(storeOrders, enableSlave);
        return storeOrders;
    }
    
    public int countStoreOrdersSearch(StoreOrdersSearchParam param) {
        boolean enableSlave = true;
        boolean limit = false;
        return this.storeOrderDAO.countStoreOrdersSearch(param, enableSlave, limit);
    }
	
	public int countStoreOrders(StoreOrderOptQueryParam queryParam) throws TException {
		boolean enableSlave = true;
		return storeOrderDAO.countStoreOrders(queryParam, enableSlave);
	}
	
	public List<StoreOrder> getStoreOrders(StoreOrderOptQueryParam queryParam) throws TException {
		boolean enableSlave = true;
		return storeOrderDAO.getStoreOrders(queryParam, enableSlave);
	}
	
	public List<StoreOrderOptlog> getStoreOrderOptlogsByOrderId(int merchantId, long storeId, String orderId) throws TException {
		boolean enableSlave = true;
		return storeOrderOptlogDAO.getStoreOrderOptlogsByOrderId(merchantId, storeId, orderId, enableSlave);
	}
	
	public List<StoreOrderItem> getStoreOrderItems(int merchantId, long storeId, List<String> orderIds) throws TException {
		boolean enableSlave = true;
		return storeOrderItemDAO.getStoreOrderItemById(merchantId, storeId, orderIds, enableSlave);
	}
	
	public List<StoreOrder> getStoreOrdersForStat(StoreOrderStatQueryParam queryParam) throws TException {
		boolean enableSlave = true;
		return storeOrderDAO.getStoreOrdersForStat(queryParam, enableSlave);
	}
	
	/**
	 * 根据StoreOrderStatQueryParam查询订单总数和总金额（应付金额+外送费）</br>
	 * @param queryParam
	 * @return
	 * @throws TException
	 */
	public StoreOrderSummaryDTO getStoreOrdersSummary(StoreOrderStatQueryParam queryParam) throws TException {
		boolean enableSlave = true;
		return storeOrderDAO.getStoreOrdersSummary(queryParam,enableSlave);
	}

	/**
	 * 获取赊账类型订单总数和总金额(credit>0)
	 * @param queryParam
	 * @return Map<Integer, StoreOrderSummaryDTO>  map中的key：1-->未销账  2-->销账成功  3-->销账撤销 
	 */
	public Map<Integer, StoreOrderSummaryDTO> getStoreCreditOrdersSummary(StoreOrderStatQueryParam queryParam) throws TException {
		boolean enableSlave = true;
		return storeOrderDAO.getStoreCreditOrdersSummary(queryParam,enableSlave);
	}

	/**
	 * 获取赊账类型订单统计(credit>0)
	 * @param queryParam
	 * @return List<StoreOrder> 
	 */
	public List<StoreOrder> getStoreCreditOrdersForStat(StoreOrderStatQueryParam queryParam) throws TException {
		boolean enableSlave = true;
		return storeOrderDAO.getStoreCreditOrdersForStat(queryParam,enableSlave);
	}

	/**
	 * 获得店铺就餐日期内 交易完成的订单集合
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @return
	 */
	public List<StoreOrder> getStoreOrderByRepastDate(int merchantId, long storeId, long repastDate) {
		int payStatus = StoreOrderPayStatusEnum.FINISH.getValue();
		int creditType1 = MerchantPrepaidCardOrderCreditTypeEnum.ENTERPRISE.getValue();
		int creditType2 = MerchantPrepaidCardOrderCreditTypeEnum.PR_FEE.getValue();

		return storeOrderDAO.getStoreOrderByRepastDate(merchantId, storeId, repastDate, payStatus, creditType1, creditType2);
	}

	public long getStoreOrderItemCoutByRepastDate(int merchantId, long storeId, long repastDate) {

		List<StoreOrder> storeOrderList = getStoreOrderByRepastDate(merchantId, storeId, repastDate);
		List<String> orderIds = Lists.newArrayList();
		for (StoreOrder storeOrder : storeOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}

		Map<String, Object> orderItemNumOfDb5weiMap = storeOrderItemDAO.getStoreOrderItemByRepastDate(merchantId, storeId, orderIds);
		return Long.valueOf(orderItemNumOfDb5weiMap.getOrDefault("num",0).toString());

	}

	public long getStoreOrderSubitemCountByRepastDate(int merchantId, long storeId, long repastDate) {
		List<StoreOrder> storeOrderList = getStoreOrderByRepastDate(merchantId, storeId, repastDate);
		List<String> orderIds = Lists.newArrayList();
		for (StoreOrder storeOrder : storeOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}

		Map<String, Object> orderSubItemNumOfDb5weiMap = storeOrderSubitemDAO.getStoreOrderSubitemByRepastDate(merchantId, storeId, orderIds);
		return  Long.valueOf(orderSubItemNumOfDb5weiMap.getOrDefault("num",0).toString());
	}

	public List<StoreOrderItem> getStoreOrderItemByOrderIdList(int merchantId, long storeId, long repastDate) {
		List<StoreOrder> storeOrderList = getStoreOrderByRepastDate(merchantId, storeId, repastDate);
		List<String> orderIds = Lists.newArrayList();
		for (StoreOrder storeOrder : storeOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}
		return storeOrderItemDAO.getStoreOrderItemByOrderIdList(merchantId, storeId, orderIds);
	}

	public List<StoreOrderSubitem> getStoreOrderSubitemByOrderIdList(int merchantId, long storeId, long repastDate) {
		List<StoreOrder> storeOrderList = getStoreOrderByRepastDate(merchantId, storeId, repastDate);
		List<String> orderIds = Lists.newArrayList();
		for (StoreOrder storeOrder : storeOrderList) {
			orderIds.add(storeOrder.getOrderId());
		}
		return storeOrderSubitemDAO.getStoreOrderSubitemByOrderIdList(merchantId, storeId, orderIds);
	}

	public List<StoreOrderDelivery> getDeliveryInfoByOrderIds(int merchantId, long storeId, List<String> orderIds) {
		Map<String, StoreOrderDelivery> storeOrderDeliveryMap = storeOrderDeliveryDAO.getMapInIds(merchantId, storeId, orderIds, true);
		if (storeOrderDeliveryMap != null) {
			return storeOrderDeliveryMap.values().stream().collect(Collectors.toList());
		}
		return Lists.newArrayList();
	}

	public List<StoreOrder> getSubStoreOrderByTableRecordId(int merchantId, long storeId, String masterOrderId, long tableRecordId, boolean forUpdate) {
		return this.storeOrderDAO.getSubStoreOrderByTableRecordId(merchantId, storeId, tableRecordId, masterOrderId, forUpdate);
	}
	/**
	 * 根据取餐日期的时间范围 获得订单集合
	 * @param merchantId
	 * @param storeId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public List<StoreOrder> getStoreOrdersByTime(int merchantId, long storeId, long startTime, long endTime) {
		return storeOrderDAO.getStoreOrdersByTime(merchantId, storeId, startTime, endTime, true);
	}

	/**
	 * 根据取餐流水号查询订单
	 *
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @param takeSerialNumber
	 * @return
	 * @throws T5weiException
	 */
	public StoreOrder getStoreOrderDetailByTakeSerialNumber(int merchantId, long storeId, long repastDate, int takeSerialNumber) throws T5weiException {
		boolean enableSlave = true;
		if (repastDate == 0) {
			repastDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		}
		StoreOrder storeOrder = storeOrderDAO.getStoreOrderByTakeSerialNumber(merchantId, storeId, repastDate, takeSerialNumber, enableSlave);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REPAST_DATE_SERIALNUMBER_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order not exist, storeId=#1, repastDate=#2, takeSerialNumber=#3 ",
					new Object[] { storeId, repastDate, takeSerialNumber }));
		}
		storeOrderHelper.setStoreOrderDetail(storeOrder, enableSlave);
		return storeOrder;
	}
}
