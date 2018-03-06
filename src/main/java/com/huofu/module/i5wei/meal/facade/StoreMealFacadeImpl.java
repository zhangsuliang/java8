package com.huofu.module.i5wei.meal.facade;

import com.huofu.module.i5wei.mealport.entity.StoreMealTask;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.*;
import huofucore.facade.i5wei.menu.StoreProductDTO;
import huofucore.facade.i5wei.order.StoreOrderDTO;
import huofucore.facade.notify.NoticeFacade;
import huofucore.facade.notify.NoticeStatusEnum;
import huofucore.facade.notify.NoticeTypeEnum;
import huofucore.facade.notify.NoticeUpdateParam;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckoutRecord;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.meal.service.StoreMealHelper;
import com.huofu.module.i5wei.meal.service.StoreMealMultiHelper;
import com.huofu.module.i5wei.meal.service.StoreMealMultiService;
import com.huofu.module.i5wei.meal.service.StoreMealService;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderQueryService;
import com.huofu.module.i5wei.order.service.StoreOrderService;

@ThriftServlet(name = "storeMealFacadeServlet", serviceClass = StoreMealFacade.class)
@Component
public class StoreMealFacadeImpl implements StoreMealFacade.Iface {
	
	private static final Log log = LogFactory.getLog(StoreMealFacadeImpl.class);
	
    @Autowired
    private StoreOrderService storeOrderService;
    
    @Autowired
    private StoreOrderQueryService storeOrderQueryService;

    @Autowired
    private StoreMealService storeMealService;
    
    @Autowired
    private StoreMealMultiService storeMealMultiService;
    
    @Autowired
    private StoreMealPortService storeMealPortService;
    
    @Autowired
	private StoreInventoryService storeInventoryService;
    
    @Autowired
    private StoreMealHelper storeMealHelper;

    @Autowired
    private StoreOrderHelper storeOrderHelper;
    
    @Autowired
    private StoreMealMultiHelper storeMealMultiHelper;
    
    @Autowired
    private StoreHeartbeatService storeHeartbeatService;

    @ThriftClient
    private NoticeFacade.Iface noticeFacade;


    @Override
    public List<StoreMealDTO> getStoreMealDTOs(int merchantId, long storeId, long repastDate, long timeBucketId, long refreshTime) throws T5weiException, TException {
        List<StoreMealTakeup> storeMealTakeups = storeMealService.getStoreMealTakeup(merchantId, storeId, repastDate, timeBucketId, refreshTime);
        List<StoreMealDTO> resultList = storeMealHelper.getStoreMealDTOByStoreMealTakeups(storeMealTakeups);
        if (resultList == null || resultList.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        List<String> orderIds = storeMealHelper.getOrderIdsInStoreMealTakeups(storeMealTakeups);
        Map<String, StoreOrder> orderMap = storeOrderQueryService.getStoreOrderMapInIds(merchantId, storeId, orderIds);
        for (StoreMealDTO storeMealDTO : resultList) {
            StoreOrder storeOrder = orderMap.get(storeMealDTO.getOrderId());
            StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
            storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        }
        storeMealHelper.sortStoreMealList(resultList);
        return resultList;
    }

    @Override
    public List<StoreMealTakeupDTO> getStoreMealTakeupDTOs(int merchantId, long storeId, long repastDate, long timeBucketId, long refreshTime) throws T5weiException, TException {
        List<StoreMealTakeup> storeMealTakeups = storeMealService.getStoreMealTakeup(merchantId, storeId, repastDate, timeBucketId, refreshTime);
        List<StoreMealTakeupDTO> storeMealTakeupDTOs = new ArrayList<StoreMealTakeupDTO>();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            StoreMealTakeupDTO storeMealTakeupDTO = BeanUtil.copy(storeMealTakeup, StoreMealTakeupDTO.class);
            storeMealTakeupDTOs.add(storeMealTakeupDTO);
        }
        return storeMealTakeupDTOs;
    }

    @Override
    public StoreMealDTO checkoutStoreMeals(StoreMealsCheckoutParam storeMealsCheckoutParam) throws T5weiException, TException {
        int merchantId = storeMealsCheckoutParam.getMerchantId();
        long storeId = storeMealsCheckoutParam.getStoreId();
        String orderId = storeMealsCheckoutParam.getOrderId();
        int checkoutType = storeMealsCheckoutParam.getCheckoutType();
        boolean refundMeal = false;
        List<StoreMealCheckout> storeMealCheckouts = storeMealService.storeMealCheckout(storeMealsCheckoutParam, refundMeal);
        int count = storeMealService.updateOrderPrepareMealFinish(merchantId, storeId, orderId, checkoutType);
        List<StoreMealDTO> storeMealDTOs = storeMealHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts);
        StoreOrder storeOrder = storeOrderService.getStoreOrderDeliveryDetailById(merchantId, storeId, orderId);
        // 更新库存
 		try {
 			storeInventoryService.updateInventoryDateByOrder(storeOrder);
 		} catch (Throwable e) {
 			log.error("#### fail to updateInventoryByOrder ", e);
 		}
        StoreMealDTO storeMealDTO;
        if (storeMealDTOs == null || storeMealDTOs.isEmpty()) {
            storeMealDTO = new StoreMealDTO();
        } else {
            storeMealDTO = storeMealDTOs.get(0);
        }
        storeOrder.setMealCheckoutTime(System.currentTimeMillis());
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        storeMealDTO.setCount(count);
        return storeMealDTO;
    }

    @Override
    public List<StoreProductDTO> getStoreMealStatProducts(int merchantId, long storeId, long repastDate, long timeBucketId) throws T5weiException, TException {
        List<StoreProduct> products = storeMealService.getStoreMealStatProducts(merchantId, storeId, repastDate, timeBucketId);
        return storeMealHelper.getStoreProductDTOs(products);
    }
    
    @Override
	public List<StoreProductDTO> getStoreMealStatProductDTOs(StoreMealStatProductsQueryParam storeMealStatProductsQueryParam) throws T5weiException, TException {
    	List<StoreProduct> products = storeMealService.getStoreMealStatProducts(storeMealStatProductsQueryParam);
        return storeMealHelper.getStoreProductDTOs(products);
	}
    
    @Override
    public List<StoreProductDTO> storeMealStatProductSetup(int merchantId, long storeId, List<StoreMealStatProductParam> storeMealStatProducts) throws T5weiException, TException {
        List<StoreProduct> products = storeMealService.storeMealStatProductSetup(merchantId, storeId, storeMealStatProducts);
        return storeMealHelper.getStoreProductDTOs(products);
    }

    @Override
    public List<StoreProductDTO> storeDivRuleProductSetup(int merchantId, long storeId, List<StoreDivRuleProductParam> storeDivRuleProducts) throws T5weiException, TException {
        List<StoreProduct> products = storeMealService.storeDivRuleProductSetup(merchantId, storeId, storeDivRuleProducts);
        return storeMealHelper.getStoreProductDTOs(products);
    }

    @Override
    public List<StoreMealDTO> getStoreMealCheckoutHistory(int merchantId, long storeId, long repastDate) throws T5weiException, TException {
        List<StoreMealCheckout> storeMealCheckouts = storeMealService.getStoreMealCheckoutHistory(merchantId, storeId, repastDate);
        if (storeMealCheckouts == null || storeMealCheckouts.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        List<StoreMealDTO> resultList = storeMealHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts);
        if (resultList == null || resultList.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        storeMealHelper.sortStoreMealListDesc(resultList);
        return resultList;
    }

    @Override
    public List<StoreMealDTO> getStoreMealsByOrderId(int merchantId, long storeId, String orderId) throws T5weiException, TException {
        List<StoreMealTakeup> storeMealTakeups = storeMealService.getStoreMealsByOrderId(merchantId, storeId, orderId);
        if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
		Set<Long> portIdSet = new HashSet<Long>();
		for (StoreMealTakeup meal : storeMealTakeups) {
			portIdSet.add(meal.getPortId());
		}
		List<Long> portIds = new ArrayList<Long>(portIdSet);
		Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> resultList = storeMealMultiHelper.getStoreMealDTOByStoreMealTakeups(storeMealTakeups, null, portMap, false);
        storeMealHelper.sortStoreMealList(resultList);
        return resultList;
    }

    @Override
    public List<StoreMealDTO> getStoreMealsHistoryByOrderId(int merchantId, long storeId, String orderId) throws T5weiException, TException {
        List<StoreMealCheckout> storeMealCheckouts = storeMealService.getStoreMealsHistoryByOrderId(merchantId, storeId, orderId);
        if (storeMealCheckouts == null || storeMealCheckouts.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        Set<Long> portIdSet = new HashSet<Long>();
		for (StoreMealCheckout meal : storeMealCheckouts) {
			portIdSet.add(meal.getPortId());
		}
		List<Long> portIds = new ArrayList<Long>(portIdSet);
		Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> resultList = storeMealMultiHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts,portMap);
        storeMealHelper.sortStoreMealList(resultList);
        return resultList;
    }

	@Override
	public StoreMealCheckoutRecordDTO getStoreMealCheckoutRecord(int merchantId, long storeId, long repastDate, int takeSerialNumber) throws T5weiException, TException {
		StoreMealCheckoutRecord storeMealCheckoutRecord = storeMealService.getStoreMealCheckoutRecord(merchantId, storeId, repastDate, takeSerialNumber);
		return BeanUtil.copy(storeMealCheckoutRecord, StoreMealCheckoutRecordDTO.class);
	}
	
	@Override
	public StoreMealCheckoutRecordDTO getStoreMealCheckoutRecordByPortId(int merchantId, long storeId, long repastDate, int takeSerialNumber, int portId) throws T5weiException, TException {
		StoreMealCheckoutRecord storeMealCheckoutRecord = storeMealService.getStoreMealCheckoutRecord(merchantId, storeId, repastDate, takeSerialNumber, portId);
		return BeanUtil.copy(storeMealCheckoutRecord, StoreMealCheckoutRecordDTO.class);
	}

	@Override
	public int updateStoreMealNotifyTime(int merchantId, long storeId, String orderId) throws T5weiException, TException {
		return storeMealService.updateStoreMealNotifyTime(merchantId, storeId, orderId);
	}

	@Override
	public int updateStoreMealsNotifyTime(int merchantId, long storeId, String orderId, int portId) throws T5weiException, TException {
		return storeMealService.updateStoreMealsNotifyTime(merchantId, storeId, orderId, portId);
	}

	@Override
	public List<StoreMealDTO> getStoreMealsByTableRecordId(int merchantId, long storeId, long tableRecordId) throws T5weiException, TException {
        List<StoreMealTakeup> storeMealTakeups = storeMealService.getStoreMealsByTableRecordId(merchantId, storeId, tableRecordId);
        if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
		Set<Long> portIdSet = new HashSet<Long>();
		for (StoreMealTakeup meal : storeMealTakeups) {
			portIdSet.add(meal.getPortId());
		}
		List<Long> portIds = new ArrayList<Long>(portIdSet);
		Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
		//需要统计的产品
		List<StoreProduct> products = storeMealMultiService.getStoreProductByMeals(storeMealTakeups);
        List<StoreMealDTO> resultList = storeMealMultiHelper.getStoreMealDTOByStoreMealTakeups(storeMealTakeups, products, portMap, false);
        storeMealHelper.sortStoreMealList(resultList);
        return resultList;
    }

	@Override
	public List<StoreMealDTO> getStoreMealsHistoryByTableRecordId( int merchantId, long storeId, long tableRecordId) throws T5weiException, TException {
        List<StoreMealCheckout> storeMealCheckouts = storeMealService.getStoreMealsHistoryByTableRecordId(merchantId, storeId, tableRecordId);
        if (storeMealCheckouts == null || storeMealCheckouts.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        Set<Long> portIdSet = new HashSet<Long>();
		for (StoreMealCheckout meal : storeMealCheckouts) {
			portIdSet.add(meal.getPortId());
		}
		List<Long> portIds = new ArrayList<Long>(portIdSet);
		Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> resultList = storeMealMultiHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts,portMap);
        storeMealHelper.sortStoreMealList(resultList);
        return resultList;
    }

	@Override
	public void clearStoreMealsOverdue(int merchantId, long storeId) throws T5weiException, TException {
		if (merchantId <= 0 || storeId <= 0) {
			return;
		}
		// 当天的起始时间
		long beginTime = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		// 减一天得到过期时间
		MutableDateTime mdt = new MutableDateTime(beginTime);
		mdt.addDays(-1);
		long expireTime = mdt.getMillis();
		// 清理过期时间之前的待出餐订单
		storeMealService.clearStoreMealsOverdue(merchantId, storeId, expireTime);
        //批量恢复打印机报警
        List<StoreMealPort> storeMealPorts = storeMealPortService.getStoreMealPorts(merchantId, storeId);
        this.batchUpdateNotice(merchantId,storeId,storeMealPorts);
	}


    @Override
    public void changeStoreTableRecordSendType(ChangeStoreOrderSendTypeParam param) throws T5weiException, TException {
        if(param.getMerchantId() <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"merchantId[" + param.getMerchantId() + "] is invalid!");
        }
        if(param.getStoreId() <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(),"storeId[" + param.getStoreId() + "] is invalid!");
        }
        storeMealService.changeStoreTableRecordSendType(param);
        this.storeHeartbeatService.updateSweepLastUpdateTime(param.getMerchantId(), param.getStoreId(), System.currentTimeMillis(), false, null);
    }

   private void batchUpdateNotice(int merchantId,long storeId,List<StoreMealPort> storeMealPorts){
        List<NoticeUpdateParam> noticeUpdateParams = new ArrayList<>();
        for(StoreMealPort storeMealPort : storeMealPorts){
            NoticeUpdateParam param = new NoticeUpdateParam();
            param.setMerchantId(merchantId);
            param.setStoreId(storeId);
            param.setNoticeTypeId(NoticeTypeEnum.PRINTER_ALARM.getValue());
            param.setNoticeStatus(NoticeStatusEnum.PROCESSED.getValue());
            param.setReplaceParam(String.valueOf(storeMealPort.getPortId()));
            noticeUpdateParams.add(param);
        }
        try {
            noticeFacade.batchUpdateNoticeStatus(noticeUpdateParams);
        } catch (TException e) {
            log.error("autoCheckOut merchantId[" + merchantId + "],storeId[" + storeId + "],noticeUpdateParams[" + JsonUtil.build(noticeUpdateParams) + "],notice batch update notice failed");
        }
    }

}
