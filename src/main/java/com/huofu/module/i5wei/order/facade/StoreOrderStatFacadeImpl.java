package com.huofu.module.i5wei.order.facade;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofuhelper.util.thrift.ThriftServlet;

import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.order.service.StoreOrderStatService;

@ThriftServlet(name = "storeOrderStatFacadeServlet", serviceClass = StoreOrderStatFacade.class)
@Component
public class StoreOrderStatFacadeImpl implements StoreOrderStatFacade.Iface {
	
    @Autowired
    private StoreOrderStatService storeOrderStatService;

    @Override
    public StoreOrderStatDTO getStoreOrderStat(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrderStat(merchantId, storeId, repastDate, timeBucketId);
    }

    @Override
    public List<StoreOrderPaymentStatDTO> getStoreOrderPaymentDayStat(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrderPaymentDayStat(merchantId, storeId, repastDate);
    }

    @Override
    public Map<Long, Integer> getStoreOrdersDayStat(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrdersDayStat(merchantId, storeId, repastDate);
    }

    @Override
    public List<String> getStoreOrderIds4PayStat(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrderIds4PayStat(merchantId, storeId, repastDate, timeBucketId);
    }

    @Override
    public List<String> getStoreOrderIds4RefundStat(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrderIds4RefundStat(merchantId, storeId, repastDate, timeBucketId);
    }

    @Override
    public List<StoreOrder4DeliveryCountDTO> getStoreOrder4DeliveryCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrder4DeliveryCount(merchantId, storeId, countTimeBegin, countTimeEnd);
    }

    @Override
    public List<StoreOrder4CancelCountDTO> getStoreOrder4CancelCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrder4CancelCount(merchantId, storeId, countTimeBegin, countTimeEnd);
    }

    @Override
    public List<String> getStoreOrderIds4CashierCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrderIds4CashierCount(merchantId, storeId, countTimeBegin, countTimeEnd);
    }

    @Override
    public List<String> getStoreOrderIds4RevenueCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrderIds4CashierCount(merchantId, storeId, countTimeBegin, countTimeEnd);
    }

    @Override
    public List<String> getStoreOrderIds4RefundCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) throws T5weiException, TException {
        return storeOrderStatService.getStoreOrderIds4RefundCount(merchantId, storeId, countTimeBegin, countTimeEnd);
    }

	@Override
	public List<StoreOrderItemStatDTO> getStoreOrderItemsCatStat(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
		return storeOrderStatService.getStoreOrderItemsCatStat(merchantId, storeId, repastDate, timeBucketId);
	}

    @Override
    public CustomerAvgPaymentDTO getCustomerAvgPayment(CustomerAvgPaymentQueryParam param) throws TException {

        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long queryDateStart = param.getQueryDateStart();
        long queryDateEnd = param.getQueryDateEnd();

        return storeOrderStatService.getStoreOrderCustomerAvgPayment(merchantId, storeId, queryDateStart, queryDateEnd);
    }

	@Override
	public List<StoreOrderRateOfUserDTO> countStoreOrderRateOfUser(int merchantId, long storeId) throws TException {
		return storeOrderStatService.countStoreOrderRateOfUser(merchantId, storeId);
	}

}
