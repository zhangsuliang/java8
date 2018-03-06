package com.huofu.module.i5wei.order.facade;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderCreditFacade;
import huofucore.facade.i5wei.order.StoreOrderDTO;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.table.service.OrderPayFinishResult;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;

@ThriftServlet(name = "storeOrderCreditFacadeServlet", serviceClass = StoreOrderCreditFacade.class)
@Component
public class StoreOrderCreditFacadeImpl implements StoreOrderCreditFacade.Iface {
	
    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderHelper storeOrderHelper;
    
    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;
    
    @Autowired
    private StoreTableRecordService storeTableRecordService;

	@Override
	@Deprecated
	public StoreOrderDTO chargeStoreOrder(int merchantId, long storeId, String orderId) throws T5weiException, TException {
		StoreOrder storeOrder = storeOrderService.chargeStoreOrder(merchantId, storeId, orderId, 0);
		if (storeOrder.isTableRecordMasterOrder()) {
			OrderPayFinishResult orderPayFinishResult = storeTableRecordService.creditSettle(storeOrder);
            if (orderPayFinishResult != null) {
            	if (orderPayFinishResult.isSettleMent()) {
            		List<StoreOrder> storeOrders = orderPayFinishResult.getStoreOrders();
	        		i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
            	}
        	}
		} else {
			i5weiMessageProducer.sendMessageOfStatStoreOrderPay(storeOrder, false);
		}
		return BeanUtil.copy(storeOrder, StoreOrderDTO.class);
	}
	
	@Override
	public StoreOrderDTO chargeStoreOrderV2(int merchantId, long storeId, String orderId, int creditType) throws T5weiException, TException {
		StoreOrder storeOrder = storeOrderService.chargeStoreOrder(merchantId, storeId, orderId, creditType);
		if (storeOrder.isTableRecordMasterOrder()) {
			OrderPayFinishResult orderPayFinishResult = storeTableRecordService.creditSettle(storeOrder);
            if (orderPayFinishResult != null) {
            	if (orderPayFinishResult.isSettleMent()) {
            		List<StoreOrder> storeOrders = orderPayFinishResult.getStoreOrders();
            		i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
            	}
        	}
		} else {
			i5weiMessageProducer.sendMessageOfStatStoreOrderPay(storeOrder, false);
		}
		return BeanUtil.copy(storeOrder, StoreOrderDTO.class);
	}

	@Override
	public List<StoreOrderDTO> disChargeStoreOrder(int merchantId, long storeId, String payOrderId, List<String> orderIds) throws T5weiException, TException {
		List<StoreOrder> storeOrders = storeOrderService.disChargeStoreOrder(merchantId, storeId, payOrderId, orderIds);
		for (StoreOrder storeOrder : storeOrders) {
			i5weiMessageProducer.sendMessageOfStatStoreOrderPay(storeOrder, false);
		}
		return BeanUtil.copyList(storeOrders, StoreOrderDTO.class);
	}

	@Override
	public StoreOrderDTO cancelCreditStoreOrder(int merchantId, long storeId, String orderId) throws T5weiException, TException {
		StoreOrder storeOrder = storeOrderService.cancelCreditStoreOrder(merchantId, storeId, orderId);
		if (storeOrder.isTableRecordMasterOrder()) {
			List<StoreOrder> storeOrders = new ArrayList<StoreOrder>();
			storeOrders.add(storeOrder);
			i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
		} else {
			i5weiMessageProducer.sendMessageOfStatStoreOrderPay(storeOrder, false);
		}
		return BeanUtil.copy(storeOrder, StoreOrderDTO.class);
	}

}
