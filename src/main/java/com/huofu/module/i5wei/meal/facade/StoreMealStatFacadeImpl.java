package com.huofu.module.i5wei.meal.facade;

import com.huofu.module.i5wei.meal.service.StoreMealStatService;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMeal4DistributeCountDTO;
import huofucore.facade.i5wei.meal.StoreMealOrderItemStatDTO;
import huofucore.facade.i5wei.meal.StoreMealProductStatDTO;
import huofucore.facade.i5wei.meal.StoreMealStatFacade;
import huofuhelper.util.DateUtil;
import huofuhelper.util.thrift.ThriftServlet;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@ThriftServlet(name = "storeMealStatFacadeServlet", serviceClass = StoreMealStatFacade.class)
@Component
public class StoreMealStatFacadeImpl implements StoreMealStatFacade.Iface {

    @Autowired
    private StoreMealStatService storeMealStatService;
    
    @Autowired
    private StoreTimeBucketService storeTimeBucketService;

    @Override
    public List<StoreMealOrderItemStatDTO> getRevenue5weiMealOrderItems(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
    	List<StoreMealOrderItemStatDTO> list = storeMealStatService.getRevenue5weiMealOrderItems(merchantId, storeId, repastDate, timeBucketId);
    	storeMealStatService.sortStoreMealOrderItemListDesc(list);
        return list; 
    }
    
    @Override
    public List<StoreMealProductStatDTO> getRevenue5weiMealTakeupProducts(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
    	List<StoreMealProductStatDTO> list = storeMealStatService.getRevenue5weiMealTakeupProducts(merchantId, storeId, repastDate, timeBucketId);
    	storeMealStatService.sortStoreMealProductListDesc(list);
        return list;
    }
    
    @Override
    public List<StoreMealProductStatDTO> getRevenue5weiMealCheckoutProducts(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
    	List<StoreMealProductStatDTO> list = storeMealStatService.getRevenue5weiMealCheckoutProducts(merchantId, storeId, repastDate, timeBucketId);
    	storeMealStatService.sortStoreMealProductListDesc(list);
        return list; 
    }

    @Override
    public List<StoreMeal4DistributeCountDTO> getStoreMeal4DistributeCount(int merchantId, long storeId, long countTimeBegin, long countTimeEnd) throws T5weiException, TException {
        return storeMealStatService.getStoreMeal4DistributeCount(merchantId, storeId, countTimeBegin, countTimeEnd);
    }

	@Override
	public List<StoreMealProductStatDTO> getStoreMealTakeupProductsByPorts(int merchantId, long storeId, List<Long> portIds) throws T5weiException, TException {
		long repastDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucketForDate(merchantId, storeId, 0, System.currentTimeMillis());
        long timeBucketId = storeTimeBucket.getTimeBucketId();
        List<StoreMealProductStatDTO> list = storeMealStatService.getStoreMealTakeupProductsByPorts(merchantId, storeId, repastDate, timeBucketId, portIds);
        storeMealStatService.sortStoreMealProductListDesc(list);
		return list;
	}

}
