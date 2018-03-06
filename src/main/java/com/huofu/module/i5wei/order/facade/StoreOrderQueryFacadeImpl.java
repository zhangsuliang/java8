package com.huofu.module.i5wei.order.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.meal.service.StoreMealMultiService;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderOptlogDAO;
import com.huofu.module.i5wei.order.entity.*;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderQueryService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.promotion.service.StoreChargeItemPromotionService;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.i5wei.order.*;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ThriftServlet(name = "storeOrderQueryFacadeServlet", serviceClass = StoreOrderQueryFacade.class)
@Component
public class StoreOrderQueryFacadeImpl implements StoreOrderQueryFacade.Iface {

    @Resource
    private StoreOrderDAO storeOrderDAO;

    @Resource
    private StoreOrderOptlogDAO storeOrderOptlogDAO;

	@Autowired
	private StoreOrderQueryService storeOrderQueryService;
	
	@Autowired
    private StoreOrderService storeOrderService;
	
	@Autowired
    private StoreTimeBucketService storeTimeBucketService;
	
	@Autowired
    private StoreOrderHelper storeOrderHelper;
	
	@Autowired
	private StoreChargeItemPromotionService storeChargeItemPromotionService;
	
	@Autowired
	private StoreMealMultiService storeMealMultiService;
	@Override
	public StoreOrderPageDTO getStoreOrdersForOpt(StoreOrderOptQueryParam queryParam) throws TException {
		int total = storeOrderQueryService.countStoreOrders(queryParam);
		StoreOrderPageDTO storeOrderPageDTO = new StoreOrderPageDTO();
        storeOrderPageDTO.setSize(queryParam.getSize());
        storeOrderPageDTO.setPageNo(queryParam.getPageNo());
        storeOrderPageDTO.setPageNum(PageUtil.getPageNum(total, queryParam.getSize()));
        storeOrderPageDTO.setTotal(total);
        if (total == 0) {
            return storeOrderPageDTO;
        }
		List<StoreOrder> dataList = storeOrderQueryService.getStoreOrders(queryParam);
		List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(dataList);
        storeOrderPageDTO.setDataList(storeOrderDTOs);
        return storeOrderPageDTO;
	}
	
	@Override
	public List<StoreOrderOptlogDTO> getStoreOrderOptlogsByOrderId(int merchantId, long storeId, String orderId) throws TException {
		List<StoreOrderOptlog> optlogs = storeOrderQueryService.getStoreOrderOptlogsByOrderId(merchantId, storeId, orderId);
		if (optlogs == null || optlogs.isEmpty()) {
			return new ArrayList<StoreOrderOptlogDTO>();
		}
		return BeanUtil.copyList(optlogs, StoreOrderOptlogDTO.class);
	}

	@Override
	public StoreOrderDTO getStoreOrderById(int merchantId, long storeId, String orderId) throws T5weiException, TException {
        StoreOrder storeOrder = storeOrderQueryService.getStoreOrderById(merchantId, storeId, orderId);
		return storeOrderHelper.getStoreOrderDTOSimpleByEntity(storeOrder);
	}
	
	@Override
	public StoreOrderDTO getStoreOrderDetailById(int merchantId, long storeId, String orderId) throws T5weiException, TException {
        StoreOrder storeOrder = storeOrderService.getStoreOrderDetailById(merchantId, storeId, orderId);
        long timeBucketId = storeOrder.getTimeBucketId();
        StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId, true);
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        storeOrderDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
        return storeOrderDTO;
    }

	@Override
	public List<StoreOrderDTO> getStoreOrders(StoreOrderOptQueryParam queryParam) throws TException {
		int total = storeOrderQueryService.countStoreOrders(queryParam);
		queryParam.setPageNo(1);
        if (total == 0) {
            return new ArrayList<StoreOrderDTO>();
        }
        queryParam.setSize(total);
		List<StoreOrder> dataList = storeOrderQueryService.getStoreOrders(queryParam);
		List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(dataList);
        return storeOrderDTOs;
	}
	
	@Override
	public List<StoreOrderItemDTO> getStoreOrderItems(int merchantId, long storeId, List<String> orderIds) throws TException {
		List<StoreOrderItem> storeOrderItems = storeOrderQueryService.getStoreOrderItems(merchantId, storeId, orderIds);
		if (storeOrderItems == null || storeOrderItems.isEmpty()) {
			return new ArrayList<StoreOrderItemDTO>();
		}
		return BeanUtil.copyList(storeOrderItems, StoreOrderItemDTO.class);
	}

	@Override
	public int getStoreOrderTakeLineNumber(int merchantId, long storeId,String orderId) throws T5weiException, TException {
		return storeMealMultiService.getStoreOrderTakeLineNumber(merchantId, storeId, orderId);
	}

	@Override
	public List<StoreOrderDTO> getStoreOrdersForStat(StoreOrderStatQueryParam queryParam) throws TException {
		List<StoreOrder> list = storeOrderQueryService.getStoreOrdersForStat(queryParam);
		return BeanUtil.copyList(list, StoreOrderDTO.class);
	}

	@Override
	public int getStoreOrderItemPromotionNum(int merchantId, long storeId, long userId, long chargeItemId, long repastDate, long timeBucketId) throws TException {
		List<Long> chargeItemIds = new ArrayList<Long>();
		chargeItemIds.add(chargeItemId);
		Map<Long, Integer> promotionNum = storeChargeItemPromotionService.getStoreOrderItemPromotionNum(merchantId, storeId, userId, chargeItemIds, repastDate, timeBucketId);
		return promotionNum.getOrDefault(chargeItemId, 0);
	}
	
	@Override
	public Map<Long, Integer> getStoreOrderItemPromotionNumMap(int merchantId, long storeId, long userId, List<Long> chargeItemIds, long repastDate, long timeBucketId) throws TException {
		if (chargeItemIds == null || chargeItemIds.isEmpty()) {
			return new HashMap<Long, Integer>();
		}
		return storeChargeItemPromotionService.getStoreOrderItemPromotionNum(merchantId, storeId, userId, chargeItemIds, repastDate, timeBucketId);
	}
	
	/**
	 * 根据StoreOrderStatQueryParam查询订单总数和总金额（应付金额+外送费）</br>
	 */
	@Override
	public StoreOrderSummaryDTO getStoreOrdersSummary(StoreOrderStatQueryParam queryParam) 
			throws TException {
		return this.storeOrderQueryService.getStoreOrdersSummary(queryParam);
	}

	/**
	 * 获取赊账订单类型（credit_status>0）的订单总数和总应付金额
	 */
	@Override
	public Map<Integer, StoreOrderSummaryDTO> getStoreCreditOrdersSummary(StoreOrderStatQueryParam queryParam)
			throws TException {
		return this.storeOrderQueryService.getStoreCreditOrdersSummary(queryParam);
	}

	@Override
	public List<StoreOrderDTO> getStoreCreditOrdersForStat(StoreOrderStatQueryParam queryParam) 
			throws TException {
		List<StoreOrder> list = this.storeOrderQueryService.getStoreCreditOrdersForStat(queryParam);
		return BeanUtil.copyList(list, StoreOrderDTO.class);
	}

    /**
     * 获取需要入账的交易订单
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     *
     * @return 交易订单
     */
    public List<StoreOrderDTO> getStoreOrderNeedAccounted(long beginTime, long endTime) {
        List<StoreOrderDTO> storeOrderList = new ArrayList<StoreOrderDTO>();

        List<Integer> optTypeList1 = new ArrayList<Integer>();
        optTypeList1.add(StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue());
        List<StoreOrderOptlog> storeOrderOptlogList1 = storeOrderOptlogDAO.getOrderIdsByOptType(optTypeList1, beginTime, endTime);
        for (StoreOrderOptlog storeOrderOptlog : storeOrderOptlogList1) {
            StoreOrder storeOrder = storeOrderDAO.getById(storeOrderOptlog.getMerchantId(), storeOrderOptlog.getStoreId(), storeOrderOptlog.getOrderId(), false, false);
            if (storeOrder.isPayFinish() && !storeOrder.isCreditOrder()) {
                StoreOrderDTO storeOrderDTO = new StoreOrderDTO();
                BeanUtil.copy(storeOrder, storeOrderDTO);
                storeOrderList.add(storeOrderDTO);
            }
        }

        List<Integer> optTypeList2 = new ArrayList<Integer>();
        optTypeList2.add(StoreOrderOptlogTypeEnum.USER_CANCEL_ORDER.getValue());
        optTypeList2.add(StoreOrderOptlogTypeEnum.CASHIER_CANCEL_ORDER.getValue());
        List<StoreOrderOptlog> storeOrderOptlogList2 = storeOrderOptlogDAO.getOrderIdsByOptType(optTypeList2, beginTime, endTime);
        for (StoreOrderOptlog storeOrderOptlog : storeOrderOptlogList2) {
            StoreOrder storeOrder = storeOrderDAO.getById(storeOrderOptlog.getMerchantId(), storeOrderOptlog.getStoreId(), storeOrderOptlog.getOrderId(), false, false);
            if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue()) {
                StoreOrderDTO storeOrderDTO = new StoreOrderDTO();
                BeanUtil.copy(storeOrder, storeOrderDTO);
                storeOrderList.add(storeOrderDTO);
            }
        }

        return storeOrderList;
    }

	/**
	 * 获得店铺就餐日期内 交易完成的订单数量
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	@Override
	public int getStoreOrderCountByRepastDate(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
		List<StoreOrder> storeOrderList = storeOrderQueryService.getStoreOrderByRepastDate(merchantId, storeId, repastDate);
		return storeOrderList.size();
	}

	/**
	 * 获得店铺就餐日期内 交易完成的订单
	 *
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	@Override
	public List<StoreOrderDTO> getStoreOrderByRepastDate(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
		List<StoreOrder> storeOrderList = storeOrderQueryService.getStoreOrderByRepastDate(merchantId, storeId, repastDate);
		return BeanUtil.copyList(storeOrderList, StoreOrderDTO.class);
	}

	/**
	 * 获得店铺就餐日期内 交易完成的订单项数量
	 *
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	@Override
	public long getStoreOrderItemCoutByRepastDate(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
		return storeOrderQueryService.getStoreOrderItemCoutByRepastDate(merchantId, storeId, repastDate);
	}

	/**
	 * 获得店铺就餐日期内 交易完成的订单子项数量
	 * @param merchantId
	 * @param storeId
	 * @param repastDate
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	@Override
	public long getStoreOrderSubitemCountByRepastDate(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
		return storeOrderQueryService.getStoreOrderSubitemCountByRepastDate(merchantId, storeId, repastDate);
	}

	@Override
	public List<StoreOrderItemDTO> getStoreOrderItemByOrderIdList(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
		List<StoreOrderItem> storeOrderItemByOrderIdList = storeOrderQueryService.getStoreOrderItemByOrderIdList(merchantId, storeId, repastDate);
		return BeanUtil.copyList(storeOrderItemByOrderIdList, StoreOrderItemDTO.class);
	}

	@Override
	public List<StoreOrderSubItemDTO> getStoreOrderSubitemByOrderIdList(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
		List<StoreOrderSubitem> storeOrderItemByOrderIdList = storeOrderQueryService.getStoreOrderSubitemByOrderIdList(merchantId, storeId, repastDate);
		return BeanUtil.copyList(storeOrderItemByOrderIdList, StoreOrderSubItemDTO.class);
	}

	@Override
	public StoreOrderPageDTO getStoreOrdersSearch(StoreOrdersSearchParam param) throws T5weiException, TException {
        int total = storeOrderQueryService.countStoreOrdersSearch(param);;
        StoreOrderPageDTO storeOrderPageDTO = new StoreOrderPageDTO();
        storeOrderPageDTO.setTotal(total);
        storeOrderPageDTO.setSize(param.getSize());
        storeOrderPageDTO.setPageNo(param.getPageNo());
        storeOrderPageDTO.setPageNum(PageUtil.getPageNum(total, param.getSize()));
        if (total == 0) {
            return storeOrderPageDTO;
        }
        List<StoreOrder> storeOrders = storeOrderQueryService.getStoreOrdersSearch(param);
        //返回：订单DTO+订单明细列表
        List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(storeOrders);
        storeOrderPageDTO.setDataList(storeOrderDTOs);
        return storeOrderPageDTO;
    }

	@Override
	public List<StoreOrderDeliveryDTO> getDeliveryInfoByOrderIds(int merchantId, long storeId, List<String> orderIds) throws T5weiException, TException {
		List<StoreOrderDelivery> deliveryInfoByOrderIds = storeOrderQueryService.getDeliveryInfoByOrderIds(merchantId, storeId, orderIds);
		List<StoreOrderDeliveryDTO> dtos = Lists.newArrayList();
		for (StoreOrderDelivery deliveryInfo : deliveryInfoByOrderIds) {
			StoreOrderDeliveryDTO dto = new StoreOrderDeliveryDTO();
			BeanUtil.copy(deliveryInfo, dto);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<StoreOrderDTO> getStoreOrderByTime(QueryStoreOrderByTimeParam param) throws TException {
		long storeId =  param.getStoreId();
		int merchantId = param.getMerchantId();
		long startTime = param.getStartTime();
		long endTime = param.getEndTime();
		List<StoreOrder> storeOrderList = storeOrderQueryService.getStoreOrdersByTime(merchantId, storeId, startTime, endTime);
		return BeanUtil.copyList(storeOrderList, StoreOrderDTO.class);
	}
}
