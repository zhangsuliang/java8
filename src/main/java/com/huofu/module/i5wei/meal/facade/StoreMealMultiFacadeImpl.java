package com.huofu.module.i5wei.meal.facade;

import com.huofu.module.i5wei.base.FacadeUtil;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealAutoCheckoutDTO;
import huofucore.facade.i5wei.meal.StoreMealAutoPrintParam;
import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealCheckoutTypeEnum;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.meal.StoreMealHistoryQueryParam;
import huofucore.facade.i5wei.meal.StoreMealMultiFacade;
import huofucore.facade.i5wei.meal.StoreMealTakeupDTO;
import huofucore.facade.i5wei.meal.StoreMealTakeupQueryParam;
import huofucore.facade.i5wei.meal.StoreMealTakeupResult;
import huofucore.facade.i5wei.meal.StoreMealsAutoCheckoutParam;
import huofucore.facade.i5wei.meal.StoreMealsCheckoutParam;
import huofucore.facade.i5wei.mealport.StoreAppTaskRelationParam;
import huofucore.facade.i5wei.mealport.StoreMealPortCheckoutTypeEnum;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.mealport.StoreMealPortRelationParam;
import huofucore.facade.i5wei.mealport.StoreMealPortTaskStatusEnum;
import huofucore.facade.i5wei.mealport.StorePortPrinterStatusEnum;
import huofucore.facade.i5wei.mealport.StorePortRelationChangeTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderDTO;
import huofucore.facade.i5wei.peripheral.I5weiPeripheralDTO;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.merchant.appcopyheartbeat.AppcopyHeartbeatFacade;
import huofucore.facade.merchant.info.MerchantFacade;
import huofucore.facade.merchant.store.StoreFacade;
import huofucore.facade.merchant.storealert.CheckoutAlertTypeEnum;
import huofucore.facade.merchant.storealert.StoreAlertFacade;
import huofucore.facade.notify.NoticeFacade;
import huofucore.facade.notify.NoticeParam;
import huofucore.facade.notify.NoticeTypeEnum;
import huofucore.facade.notify.PrinterAlarmReasonEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutRecordDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckoutRecord;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.meal.service.StoreMealMultiHelper;
import com.huofu.module.i5wei.meal.service.StoreMealMultiService;
import com.huofu.module.i5wei.mealport.dao.StoreMealTaskLogDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealTask;
import com.huofu.module.i5wei.mealport.facade.StoreMealPortFacadeUtil;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderQueryService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;

@ThriftServlet(name = "storeMealMultiFacadeServlet", serviceClass = StoreMealMultiFacade.class)
@Component
public class StoreMealMultiFacadeImpl implements StoreMealMultiFacade.Iface {
	
	private static final Log log = LogFactory.getLog(StoreMealMultiFacadeImpl.class);

    @Autowired
    private StoreOrderService storeOrderService;
    
    @Autowired
    private StoreOrderQueryService storeOrderQueryService;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    @Autowired
    private StoreMealMultiService storeMealMultiService;

    @Autowired
    private StoreMealMultiHelper storeMealMultiHelper;

    @Autowired
    private StoreMealPortService storeMealPortService;

    @Autowired
    private StoreHeartbeatService storeHeartbeatService;

    @ThriftClient
    private StoreAlertFacade.Iface storeAlertFacade;

    @ThriftClient
    private AppcopyHeartbeatFacade.Iface appcopyHeartbeatFacade;

    @Autowired
    private StoreMealPortFacadeUtil storeMealPortFacadeUtil;
    
    @Autowired
    private StoreMealCheckoutRecordDAO storeMealCheckoutRecordDao;
    
    @ThriftClient
    private MerchantFacade.Iface merchantFacade;

    @ThriftClient
    private StoreFacade.Iface storeFacade;
    
    @Autowired
    private StoreMealTaskLogDAO storeMealTaskLogDAO;
    
    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @ThriftClient
    private NoticeFacade.Iface noticeFacade;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private FacadeUtil facadeUtil;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @Override
    public StoreMealTakeupResult getStoreMealDTOsOfPortByNumber(StoreMealTakeupQueryParam storeMealTakeupQueryParam) throws T5weiException, TException {
        int merchantId = storeMealTakeupQueryParam.getMerchantId();
        long storeId = storeMealTakeupQueryParam.getStoreId();
        long portId = storeMealTakeupQueryParam.getPortId();
        int takeSerialNumber = storeMealTakeupQueryParam.getTakeSerialNumber();
        List<Long> portIds = new ArrayList<Long>();
        portIds.add(portId);
        List<StoreMealTakeup> storeMealTakeups = storeMealMultiService.getStoreMealTakeup(merchantId, storeId, portId, takeSerialNumber);
        //需要统计的产品
        StoreMealTakeupResult storeMealTakeupResult = new StoreMealTakeupResult();
        List<StoreProduct> products = storeMealMultiService.getStoreProductByMeals(storeMealTakeups);
        //本次出餐所有统计产品
        storeMealTakeupResult.setStatProductIds(storeMealMultiHelper.getStoreProductIds(products));
        Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> storeMeals = storeMealMultiHelper.getStoreMealDTOByStoreMealTakeups(storeMealTakeups, products, portMap, false);
        if (storeMeals == null || storeMeals.isEmpty()) {
            return storeMealTakeupResult;
        }
        List<String> orderIds = storeMealMultiHelper.getOrderIdsInStoreMealTakeups(storeMealTakeups);
        Map<String, StoreOrder> orderMap = storeOrderQueryService.getStoreOrderMapInIds(merchantId, storeId, orderIds);
        for (StoreMealDTO storeMealDTO : storeMeals) {
            StoreOrder storeOrder = orderMap.get(storeMealDTO.getOrderId());
            StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
            storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        }
        storeMealMultiHelper.sortStoreMealList(storeMeals);
        storeMealTakeupResult.setStoreMeals(storeMeals);
        return storeMealTakeupResult;
    }
    
    @Override
    public List<StoreMealTakeupDTO> getStoreMealTakeupDTOsOfPortByNumber(StoreMealTakeupQueryParam storeMealTakeupQueryParam) throws T5weiException, TException {
        int merchantId = storeMealTakeupQueryParam.getMerchantId();
        long storeId = storeMealTakeupQueryParam.getStoreId();
        long portId = storeMealTakeupQueryParam.getPortId();
        int takeSerialNumber = storeMealTakeupQueryParam.getTakeSerialNumber();
        List<StoreMealTakeup> storeMealTakeups = storeMealMultiService.getStoreMealTakeup(merchantId, storeId, portId, takeSerialNumber);
        List<StoreMealTakeupDTO> storeMealTakeupDTOs = new ArrayList<StoreMealTakeupDTO>();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            StoreMealTakeupDTO storeMealTakeupDTO = BeanUtil.copy(storeMealTakeup, StoreMealTakeupDTO.class);
            storeMealTakeupDTOs.add(storeMealTakeupDTO);
        }
        return storeMealTakeupDTOs;
    }

    @Override
    public StoreMealAutoCheckoutDTO autoCheckoutStoreMeals(StoreMealsAutoCheckoutParam storeMealsAutoCheckoutParam) throws T5weiException, TException {
        if(log.isDebugEnabled()){
    		log.debug("####autoCheckoutStoreMeals input=" + storeMealsAutoCheckoutParam);
    	}
		StoreMealAutoCheckoutDTO resultDTO = new StoreMealAutoCheckoutDTO();
        int merchantId = storeMealsAutoCheckoutParam.getMerchantId();
        long storeId = storeMealsAutoCheckoutParam.getStoreId();
        long appcopyId = storeMealsAutoCheckoutParam.getAppcopyId();
        int orderNum = storeMealsAutoCheckoutParam.getOrderNum();// 每次自动出餐单数
        int checkoutType = StoreMealCheckoutTypeEnum.AUTO.getValue();//此接口默认自动
        if (appcopyId <= 0) {
            throw new T5weiException(
                    T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "appcopyId can not be null ");
        }
        if (orderNum <= 0 || orderNum > 5) {
            throw new T5weiException(
                    T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "orderNum=" + orderNum + ", value must between 1~5 ");
        }
        List<StoreMealAutoPrintParam> storeMealAutoPrintParams = storeMealsAutoCheckoutParam.getStoreMealAutoPrintParams();
        //更新打印结果
        storeMealMultiService.batchUpdatePrinted(merchantId, storeId, storeMealAutoPrintParams);
        //更新打印机状态
        storeMealPortService.batchUpdatePrinterStatus(merchantId, storeId, storeMealAutoPrintParams);
        //不能正常打印的出餐口解除任务关系
        List<StoreMealPortRelationParam> mealPortRelations = new ArrayList<StoreMealPortRelationParam>();
        List<Long> portIdList = new ArrayList<>();
		for (StoreMealAutoPrintParam autoPrintParam : storeMealAutoPrintParams) {
			int printerStatus = autoPrintParam.getPrinterStatus();
			if (printerStatus != StorePortPrinterStatusEnum.ON.getValue()) {
                portIdList.add(autoPrintParam.getPortId());
				StoreMealPortRelationParam portRelationParam = new StoreMealPortRelationParam();
	            portRelationParam.setPortId(autoPrintParam.getPortId());
	            portRelationParam.setTaskStatus(StoreMealPortTaskStatusEnum.OFF.getValue());
	            portRelationParam.setPrinterStatus(printerStatus);
	            portRelationParam.setRelationChangeType(StorePortRelationChangeTypeEnum.SYSTEM_AUTO_NOT_PRINT.getValue());
	            portRelationParam.setCheckoutType(StoreMealCheckoutTypeEnum.AUTO.getValue());
	            mealPortRelations.add(portRelationParam);
			}
		}
        List<StoreMealPort> storeMealPortList = storeMealPortDAO.getByIds(merchantId, storeId, portIdList, true);
        // 打印机报警
        i5weiMessageProducer.sendPrintAlarmDelayMessage(merchantId,storeId,storeMealPortList);
		if (!mealPortRelations.isEmpty()) {
        	StoreAppTaskRelationParam storeAppTaskRelationParam = new StoreAppTaskRelationParam();
        	storeAppTaskRelationParam.setMerchantId(merchantId);
        	storeAppTaskRelationParam.setStoreId(storeId);
        	storeAppTaskRelationParam.setAppcopyId(appcopyId);
        	storeAppTaskRelationParam.setMealPortRelations(mealPortRelations);
        	storeMealPortService.registStoreAppTaskRelation(storeAppTaskRelationParam);
        	storeHeartbeatService.updatePortLastUpdateTime(merchantId, storeId, System.currentTimeMillis());
        	storeHeartbeatService.changePortIdle(merchantId, storeId, true);
        }
		//Pad所负责出餐口
        List<StoreMealPort> storeMealPorts = storeMealPortService.getStoreAppTaskMealPorts(merchantId, storeId, appcopyId, checkoutType);
        if (storeMealPorts == null || storeMealPorts.isEmpty()) {
            resultDTO.setStoreMealPorts(new ArrayList<>());
            resultDTO.setStoreMeals(new ArrayList<>());
            if(log.isDebugEnabled()){
            	log.debug("####autoCheckoutStoreMeals storeMealPorts=null");
            }
            return resultDTO;
        }
		//出餐口
        List<StoreMealPortDTO> storeMealPortDTOs = storeMealPortFacadeUtil.buildStoreMealPortDTOs(merchantId, storeMealPorts, true);
		List<Long> portIds = new ArrayList<Long>();
		Map<Long, StoreMealPort> portMap = new HashMap<Long, StoreMealPort>();
		for (StoreMealPort port : storeMealPorts) {
			portMap.put(port.getPortId(), port);
			portIds.add(port.getPortId());
		}
		if(log.isDebugEnabled()){
			log.debug("####autoCheckoutStoreMeals storeMealPorts=" + JsonUtil.build(storeMealPortDTOs));
		}
		//标记出餐时间
        storeMealPortService.updateTaskMealTime(merchantId, storeId, appcopyId, portIds);
        //查询未打印成功的已出餐订单
        List<StoreMealCheckout> storeMealCheckouts = storeMealMultiService.getStoreMealsHistoryNotPrinted(merchantId, storeId, portIds);
        if (storeMealCheckouts != null && !storeMealCheckouts.isEmpty()) {
            List<StoreMealDTO> storeMealDTOs = storeMealMultiHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts, portMap);
            List<String> orderIds = new ArrayList<String>();
            for (StoreMealDTO storeMealDTO : storeMealDTOs) {
                orderIds.add(storeMealDTO.getOrderId());
            }
            Map<String, StoreOrder> checkOrderMap = storeOrderQueryService.getStoreOrderMapInIds(merchantId, storeId, orderIds);
            //封装order信息返回
            for (StoreMealDTO storeMealDTO : storeMealDTOs) {
                StoreOrder storeOrder = checkOrderMap.get(storeMealDTO.getOrderId());
                StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
                storeMealDTO.setStoreOrderDTO(storeOrderDTO);
            }
            resultDTO.setStoreMealPorts(storeMealPortDTOs);
            resultDTO.setStoreMeals(storeMealDTOs);
            if(log.isDebugEnabled()){
            	log.debug("####autoCheckoutStoreMeals storeMealCheckouts  resultDTO=" + resultDTO);
            }
            return resultDTO;
        }
        //待出餐订单
        List<String> orderIds = storeMealMultiService.getStoreMealTakeupOrders(merchantId, storeId, portIds, orderNum);
        if (orderIds == null || orderIds.isEmpty()) {
            resultDTO.setStoreMealPorts(storeMealPortDTOs);
            resultDTO.setStoreMeals(new ArrayList<>());
            if(log.isDebugEnabled()){
            	log.debug("####autoCheckoutStoreMeals  orderIds=" + JsonUtil.build(orderIds));
            }
            return resultDTO;
        }
        //待出餐列表
        List<StoreMealTakeup> storeMealTakeups = storeMealMultiService.getStoreMealTakeups(merchantId, storeId, orderIds, portIds);
        if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
            resultDTO.setStoreMealPorts(storeMealPortDTOs);
            resultDTO.setStoreMeals(new ArrayList<>());
            if(log.isDebugEnabled()){
            	log.debug("####autoCheckoutStoreMeals storeMealTakeups=null ");
            }
            return resultDTO;
        }
        //需要统计的产品
        List<StoreProduct> products = storeMealMultiService.getStoreProductByMeals(storeMealTakeups);
        //自动出餐分单
        List<StoreMealDTO> storeMealDTOs = new ArrayList<StoreMealDTO>();
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        boolean isAuto = true;// 普通模式分单==自动出餐和打包不分单
        if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
            isAuto = false;// 高级模式分单
        }
		// 获取待出餐分单
        storeMealDTOs = storeMealMultiHelper.getStoreMealDTOByStoreMealTakeups(storeMealTakeups, products, portMap, isAuto);
        // 每次最大获取的分单数量
		int maxDivMealSize = 50; 
        if (storeMealDTOs.size() > maxDivMealSize) {
        	storeMealDTOs = storeMealDTOs.subList(0, maxDivMealSize);
		}
        // 根据分单自动出餐
        List<StoreMealDTO> resultList = new ArrayList<StoreMealDTO>();
        for (StoreMealDTO storeMealDTO : storeMealDTOs) {
            String orderId = storeMealDTO.getOrderId();
			if (orderId == null || orderId.isEmpty()) {
				continue;
			}
			List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
			if (storeMealChargeDTOs == null || storeMealChargeDTOs.isEmpty()) {
				continue;
			}
            Map<String,Object> resultMap = storeMealMultiService.storeMealCheckoutByMealDTO(storeMealDTO, appcopyId, checkoutType);
        	StoreMealCheckoutRecord storeMealCheckoutRecord = (StoreMealCheckoutRecord) resultMap.get("storeMealCheckoutRecord");
        	StoreMealDTO storeMeal = storeMealDTO.deepCopy();
        	storeMeal.setTakeSerialSeq(storeMealCheckoutRecord.getCheckoutSeq());
        	storeMeal.setCheckoutSeq(storeMealCheckoutRecord.getCheckoutSeq());
        	storeMeal.setPackagedSeq(storeMealCheckoutRecord.getPackagedSeq());
            long portId = storeMealDTO.getPortId();
            int count = storeMealMultiService.updateOrderPrepareMealFinish(merchantId, storeId, portId, orderId, StoreMealPortCheckoutTypeEnum.AUTO.getValue());
            storeMeal.setCount(count);
            resultList.add(storeMeal);
        }
        Map<String, StoreOrder> orderMap = storeOrderQueryService.getStoreOrderMapInIds(merchantId, storeId, orderIds);
        //封装order信息返回
        storeOrderHelper.setStoreOrderActualPayResult(orderMap);
        Map<String, StoreOrderDTO> storeOrderDTOMap = storeOrderHelper.getStoreOrderDTOMap(orderMap);
        for (StoreMealDTO storeMealDTO : resultList) {
            StoreOrderDTO storeOrderDTO = storeOrderDTOMap.get(storeMealDTO.getOrderId());
            storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        }
        resultDTO.setStoreMealPorts(storeMealPortDTOs);
        resultDTO.setStoreMeals(resultList);
        this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), false, store5weiSetting); //心跳更新
        if(log.isDebugEnabled()){
        	log.debug("####autoCheckoutStoreMeals storeMealTakeups  resultDTO=" + resultDTO);
        }
        return resultDTO;
    }


    @SuppressWarnings("unchecked")
	@Override
    public StoreMealDTO checkoutStoreMealsByPort(StoreMealsCheckoutParam storeMealsCheckoutParam) throws T5weiException, TException {
        int merchantId = storeMealsCheckoutParam.getMerchantId();
        long storeId = storeMealsCheckoutParam.getStoreId();
        String orderId = storeMealsCheckoutParam.getOrderId();
        long appcopyId = storeMealsCheckoutParam.getAppcopyId(); 
        int checkoutType = storeMealsCheckoutParam.getCheckoutType();
        long portId = storeMealsCheckoutParam.getPortId();
        
        Map<String,Object> resultMap = storeMealMultiService.storeMealCheckout(storeMealsCheckoutParam);
        StoreMealCheckoutRecord storeMealCheckoutRecord = (StoreMealCheckoutRecord) resultMap.get("storeMealCheckoutRecord");
        List<StoreMealCheckout> storeMealCheckouts = (List<StoreMealCheckout>) resultMap.get("storeMealCheckouts");
        int count = storeMealMultiService.updateOrderPrepareMealFinish(merchantId, storeId, portId, orderId, checkoutType);
        List<Long> portIds = new ArrayList<Long>();
        portIds.add(portId);
        //标记出餐时间
        storeMealPortService.updateTaskMealTime(merchantId, storeId, appcopyId, portIds);
        //构造返回结果
        Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> storeMealDTOs = storeMealMultiHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts, portMap);
        StoreOrder storeOrder = storeOrderService.getStoreOrderDeliveryDetailById(merchantId, storeId, orderId);
        StoreMealDTO storeMealDTO;
        if (storeMealDTOs == null || storeMealDTOs.isEmpty()) {
        	if(log.isDebugEnabled()){
        		log.debug("####checkoutStoreMealsByPort storeMealDTOs is null, input="+storeMealsCheckoutParam+", output="+JsonUtil.build(storeMealCheckouts));
        	}
        	throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CHECKOUT_NUM_MORE_THEN_REMAIN_NUM.getValue(),
                    DataUtil.infoWithParams("store checkout num more then order remain num, storeId=#1, orderId=#2, storeMealDTOs=null ", new Object[]{storeId, orderId}));
        } else {
            storeMealDTO = storeMealDTOs.get(0);
        }
        storeOrder.setMealCheckoutTime(System.currentTimeMillis());
        storeOrderHelper.setStoreOrderActualPayResult(storeOrder);
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        storeMealDTO.setTakeSerialSeq(storeMealCheckoutRecord.getCheckoutSeq());
        storeMealDTO.setCheckoutSeq(storeMealCheckoutRecord.getCheckoutSeq());
        storeMealDTO.setPackagedSeq(storeMealCheckoutRecord.getPackagedSeq());
        storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        storeMealDTO.setCount(count);
        return storeMealDTO;
    }
    
    @Override
	public List<StoreMealDTO> checkoutStoreOrderMealsByPort(StoreMealsCheckoutParam storeMealsCheckoutParam) throws T5weiException, TException {
    	int merchantId = storeMealsCheckoutParam.getMerchantId();
        long storeId = storeMealsCheckoutParam.getStoreId();
        String orderId = storeMealsCheckoutParam.getOrderId();
        long appcopyId = storeMealsCheckoutParam.getAppcopyId(); 
        int checkoutType = storeMealsCheckoutParam.getCheckoutType();
        long portId = storeMealsCheckoutParam.getPortId();
        List<StoreMealTakeup> storeMealTakeups = storeMealMultiService.getStoreMealTakeups(merchantId, storeId, orderId, portId, false);
        List<Long> portIds = new ArrayList<Long>();
		portIds.add(portId);
		//标记出餐时间
        storeMealPortService.updateTaskMealTime(merchantId, storeId, appcopyId, portIds);
        //构造返回结果
        Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> storeMealDTOs = storeMealMultiHelper.getStoreMealDTOByStoreMealTakeups(storeMealTakeups, null, portMap, false);
        StoreOrder storeOrder = storeOrderService.getStoreOrderDeliveryDetailById(merchantId, storeId, orderId);
        storeOrder.setMealCheckoutTime(System.currentTimeMillis());
        storeOrderHelper.setStoreOrderActualPayResult(storeOrder);
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        List<StoreMealDTO> resultList = new ArrayList<StoreMealDTO>();
        for (StoreMealDTO storeMealDTO : storeMealDTOs) {
        	orderId = storeMealDTO.getOrderId();
        	if (orderId == null || orderId.isEmpty()) {
				continue;
			}
			List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
			if (storeMealChargeDTOs == null || storeMealChargeDTOs.isEmpty()) {
				continue;
			}
        	Map<String,Object> resultMap = storeMealMultiService.storeMealCheckoutByMealDTO(storeMealDTO, appcopyId, checkoutType);
        	StoreMealCheckoutRecord storeMealCheckoutRecord = (StoreMealCheckoutRecord) resultMap.get("storeMealCheckoutRecord");
        	StoreMealDTO storeMeal = storeMealDTO.deepCopy();
        	storeMeal.setTakeSerialSeq(storeMealCheckoutRecord.getCheckoutSeq());
        	storeMeal.setCheckoutSeq(storeMealCheckoutRecord.getCheckoutSeq());
        	storeMeal.setPackagedSeq(storeMealCheckoutRecord.getPackagedSeq());
            int count = storeMealMultiService.updateOrderPrepareMealFinish(merchantId, storeId, portId, orderId, checkoutType);
            storeMeal.setCount(count);
            storeMeal.setStoreOrderDTO(storeOrderDTO);
            resultList.add(storeMeal);
        }
        return resultList;
    }

    @Override
    public int regularCheckStorePort(int merchantId, long storeId) throws T5weiException, TException {
        List<StoreMealPort> ports = storeMealPortService.getStoreMealPorts(merchantId, storeId);
        if (ports == null || ports.isEmpty()) {
            //没有出餐口
            storeHeartbeatService.changePortIdle(merchantId, storeId, false);
            return 0;
        }
        List<StoreMealTask> tasks = storeMealPortService.getStoreMealTasks(merchantId, storeId);
        Map<Long, StoreMealTask> taskMap = new HashMap<Long, StoreMealTask>();
        if (tasks != null && !tasks.isEmpty()) {
            for (StoreMealTask task : tasks) {
                taskMap.put(task.getPortId(), task);
            }
        }
		// 1.判断是否有空闲的出餐口
		int minutes = 2;// 未出餐超时时间
        long nowTime = System.currentTimeMillis();
        int _min_millis = DateUtil.getMillisInMinutes(minutes);
        List<StoreMealPortRelationParam> mealPortRelations = new ArrayList<StoreMealPortRelationParam>();
        for (StoreMealPort port : ports) {
			// 1.1.未被分配出餐pad的出餐口
            StoreMealTask mealTask = taskMap.get(port.getPortId());
            if (mealTask == null) {
                storeMealPortService.initPrinterStatus(merchantId, storeId, port.getPortId());//默认为手动，未建立任务关系
                continue;
            }
            // 1.2.死掉的终端解除任务关系
            long appcopyId = mealTask.getAppcopyId();
			if (appcopyId > 0) {
            	boolean isAppcopyAlive = appcopyHeartbeatFacade.isAppcopyAlive(merchantId, storeId, appcopyId);
                if (!isAppcopyAlive) {
                	if (mealTask.getCheckoutType() == StoreMealPortCheckoutTypeEnum.AUTO.getValue() || port.isAutoShift()) {
                		StoreMealPortRelationParam portRelationParam = new StoreMealPortRelationParam();
                        portRelationParam.setPortId(port.getPortId());
                        portRelationParam.setTaskStatus(StoreMealPortTaskStatusEnum.OFF.getValue());
                        portRelationParam.setRelationChangeType(StorePortRelationChangeTypeEnum.SYSTEM_AUTO_NOT_HREATBEAT.getValue());
                        mealPortRelations.add(portRelationParam);
                        if (log.isDebugEnabled()) {
                        	log.debug("####StoreMealTask App not alive , merchantId=" + merchantId + ", storeId=" + storeId + ", appcopyId=" + appcopyId+ ", portId=" + port.getPortId()+",isAppcopyAlive="+isAppcopyAlive);
                        }
                		continue;
                	}
                }
            }
            List<StoreMealTakeup> storeMealTakeups = storeMealMultiService.getStoreMealTakeup(merchantId, storeId, port.getPortId(), 0);
            //有出餐任务的时候做如下切换
            if (storeMealTakeups != null && storeMealTakeups.size() > 0) {
    			// 1.3.不能连接打印机的出餐口，自动打印或智能切换时需要重新分配任务
                int printerStatus = mealTask.getPrinterStatus();
                if (printerStatus != StorePortPrinterStatusEnum.ON.getValue()) {
    				if (mealTask.getCheckoutType() == StoreMealPortCheckoutTypeEnum.AUTO.getValue() || port.isAutoShift()) {
    					if(mealTask.getTaskStatus()==StoreMealPortTaskStatusEnum.ON.getValue()){
    						StoreMealPortRelationParam portRelationParam = new StoreMealPortRelationParam();
    	                    portRelationParam.setPortId(port.getPortId());
    	                    portRelationParam.setTaskStatus(StoreMealPortTaskStatusEnum.OFF.getValue());
    	                    portRelationParam.setPrinterStatus(StorePortPrinterStatusEnum.CAN_NOT.getValue());
    	                    portRelationParam.setRelationChangeType(StorePortRelationChangeTypeEnum.SYSTEM_AUTO_NOT_PRINT.getValue());
    	                    mealPortRelations.add(portRelationParam);
    	                    if (log.isDebugEnabled()) {
    	                    	log.debug("####StoreMealTask can not print, merchantId=" + merchantId + ", storeId=" + storeId + ", appcopyId=" + appcopyId+ ", portId=" + port.getPortId()+",printerStatus="+printerStatus);
    	                    }
                		}
    					continue;
                	}
                }
    			// 1.4.在指定时间内自动出餐口有出餐任务，但是一直不出餐，自动打印或智能切换时需要重新分配任务
                long mealTime = mealTask.getMealTime();
                long outOffTime = mealTime + _min_millis;
    			if (outOffTime < nowTime) {
    				if (mealTask.getCheckoutType() == StoreMealPortCheckoutTypeEnum.AUTO.getValue() || port.isAutoShift()) {
    					if(mealTask.getTaskStatus()==StoreMealPortTaskStatusEnum.ON.getValue()){
    						StoreMealPortRelationParam portRelationParam = new StoreMealPortRelationParam();
    		                portRelationParam.setPortId(port.getPortId());
    		                portRelationParam.setTaskStatus(StoreMealPortTaskStatusEnum.OFF.getValue());
    		                portRelationParam.setRelationChangeType(StorePortRelationChangeTypeEnum.SYSTEM_AUTO_TIMEOUT.getValue());
    		                mealPortRelations.add(portRelationParam);
    		                if (log.isDebugEnabled()) {
    		                	log.debug("####StoreMealTask outoff time, merchantId=" + merchantId + ", storeId=" + storeId + ", appcopyId=" + appcopyId+ ", portId=" + port.getPortId()+",lastMealTime="+DateUtil.formatDate(DateUtil.format_ms, new Date(mealTime)));
    		                }
                		}
    					continue;
    				}
                }
            }
        }
        //2.1.更新出餐口状态
		if (!mealPortRelations.isEmpty()) {
        	StoreAppTaskRelationParam storeAppTaskRelationParam = new StoreAppTaskRelationParam();
        	storeAppTaskRelationParam.setMerchantId(merchantId);
        	storeAppTaskRelationParam.setStoreId(storeId);
        	storeAppTaskRelationParam.setAppcopyId(0);
        	storeAppTaskRelationParam.setMealPortRelations(mealPortRelations);
        	storeMealPortService.registStoreAppTaskRelation(storeAppTaskRelationParam);
        	storeHeartbeatService.updatePortLastUpdateTime(merchantId, storeId, System.currentTimeMillis());
        	if (log.isDebugEnabled()) {
        		log.debug("####StoreMealTask update registStoreAppTaskRelation, merchantId=" + merchantId + ", storeId=" + storeId);
        	}
        }
        //2.2.空闲出餐口检查
        List<StoreMealPort> idlePorts = storeMealPortService.getStoreMealPortsIdle(merchantId, storeId, false);
        boolean hasIdlePorts = true;
        if (idlePorts == null || idlePorts.isEmpty()) {
        	hasIdlePorts = false;
        } else {
        	hasIdlePorts = true;
        }
        storeHeartbeatService.changePortIdle(merchantId, storeId, hasIdlePorts);
		if (idlePorts == null || idlePorts.isEmpty()) {
			return 0;
		}
        //2.3指定时间内仍然没有执行自动出餐任务，根据空闲原因发送报警
        StringBuffer monitorContent = new StringBuffer();
        monitorContent.append("merchantId=").append(merchantId).append(", storeId=").append(storeId);
        CheckoutAlertTypeEnum alertType = null;
        List<Long> idlePortIds = new ArrayList<Long>(); //空闲出餐口Id列表
        List<StoreMealPort> storeMealPorts = new ArrayList<>();
        for (StoreMealPort idlePort : idlePorts) {
        	idlePortIds.add(idlePort.getPortId());
            StoreMealTask mealTask = taskMap.get(idlePort.getPortId());
            if (mealTask == null) {
                continue;
            }
            int printerStatus = mealTask.getPrinterStatus();
            if (printerStatus == StorePortPrinterStatusEnum.OFF.getValue()) {
                alertType = CheckoutAlertTypeEnum.PRINTER_ERROR;//打印机无法正常连接
                monitorContent.append(" [出餐口").append(idlePort.getPortId()).append("打印机无法正常连接] ");
                idlePort.setPrintAlarm(PrinterAlarmReasonEnum.NOT_CONNECT.getValue());
                storeMealPorts.add(idlePort);
            } else if (printerStatus == StorePortPrinterStatusEnum.CAN_NOT.getValue()) {
                alertType = CheckoutAlertTypeEnum.PRINTER_ERROR;//打印机无法正常打印
                monitorContent.append(" [出餐口").append(idlePort.getPortId()).append("打印机无法正常打印] ");
                idlePort.setPrintAlarm(PrinterAlarmReasonEnum.NOT_PRINT.getValue());
                storeMealPorts.add(idlePort);
            } else {
                alertType = CheckoutAlertTypeEnum.NO_CLIENT_ACCEPT_AUTO_CHECKOUT;//指定时间内未自动出餐
                monitorContent.append(" [出餐口").append(idlePort.getPortId()).append("指定时间内未自动出餐] ");
                idlePort.setPrintAlarm(PrinterAlarmReasonEnum.NO_CLIENT_ACCEPT_AUTO_CHECKOUT.getValue());
                storeMealPorts.add(idlePort);
            }
        }
        if (alertType != null) {
			// 有待出餐列表才需要报警
        	List<StoreMealTakeup> storeMealTakeups = storeMealMultiService.getStoreMealTakeup(merchantId, storeId, idlePortIds);
			if (storeMealTakeups != null && storeMealTakeups.size() > 0) {
                long mealTime = storeMealTakeups.get(0).getCreateTime();
                long outOffTime = mealTime + _min_millis;
                if (outOffTime < nowTime) {
                    storeMealTaskLogDAO.deleteHistoryTaskLogs(merchantId, storeId);// 删除历史mealTaskLog
                }
                // 打印机报警
                this.sendPrinterAlarm(merchantId,storeId,storeMealPorts);
            }
        }
        return 1;
    }

    private void sendPrinterAlarm(int merchantId,long storeId,List<StoreMealPort> storeMealPorts){
        List<Long> peripheralIds = new ArrayList<>();
        for(StoreMealPort storeMealPort : storeMealPorts){
            peripheralIds.add(storeMealPort.getPrinterPeripheralId());
        }
        List<NoticeParam> noticeParams = new ArrayList<>();
        Map<Long, I5weiPeripheralDTO> map = null;
        try {
            map = facadeUtil.buildI5weiPeripheralDTOMap(merchantId, peripheralIds);
        } catch (TException e) {
            log.error("merchantId[" + merchantId + "],storeId[" + storeId + "],peripheralIds[" + JsonUtil.build(peripheralIds) + "],getI5weiPeripheralDTOMap failed");
        }
        for(StoreMealPort storeMealPort : storeMealPorts){
            if(storeMealPort.getPrinterPeripheralId() <= 0){
                continue;
            }
            int printAlarmReason = storeMealPort.getPrintAlarm();
            NoticeParam param = new NoticeParam();
            param.setMerchantId(merchantId);
            param.setStoreId(storeId);
            param.setNoticeTypeId(NoticeTypeEnum.PRINTER_ALARM.getValue());
            List<String> params = new ArrayList<>();
            I5weiPeripheralDTO i5weiPeripheralDTO = map.get(storeMealPort.getPrinterPeripheralId());
            if(i5weiPeripheralDTO == null){
                continue;
            }
            params.add(storeMealPort.getName());
            params.add(i5weiPeripheralDTO.getName());
            if(printAlarmReason == PrinterAlarmReasonEnum.NOT_PRINT.getValue()){
                params.add("无法打印");
            }
            if(printAlarmReason == PrinterAlarmReasonEnum.NOT_CONNECT.getValue()){
                params.add("无法连接");
            }
            if(printAlarmReason == PrinterAlarmReasonEnum.NO_CLIENT_ACCEPT_AUTO_CHECKOUT.getValue()){
                 params.add("无法连接");
            }
            param.setSrcId(String.valueOf(storeMealPort.getPrinterPeripheralId()));
            param.setParams(params);
            param.setReplaceParam(String.valueOf(storeMealPort.getPortId()));
            noticeParams.add(param);
        }
        try {
            if(noticeParams != null && !noticeParams.isEmpty()){
                noticeFacade.batchSendNotice(noticeParams);
            }
        } catch (Exception e) {
            log.error("autoCheckOut  merchantId[" + merchantId + "],storeId[" + storeId + "],noticeParams[" + JsonUtil.build(noticeParams) + "],send notice failed,,,," + e.getMessage());
        }
    }

    @Override
    public List<StoreMealDTO> getStoreMealCheckoutHistoryByPort(int merchantId, long storeId, long repastDate, long portId) throws T5weiException, TException {
        List<StoreMealCheckout> storeMealCheckouts = storeMealMultiService.getStoreMealCheckoutHistory(merchantId, storeId, repastDate, portId);
        if (storeMealCheckouts == null || storeMealCheckouts.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        List<Long> portIds = new ArrayList<Long>();
        portIds.add(portId);
        Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> resultList = storeMealMultiHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts, portMap);//按出餐口出餐
        if (resultList == null || resultList.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        List<String> orderIds = storeMealMultiHelper.getOrderIdsInStoreMealCheckouts(storeMealCheckouts);
        Map<String, StoreOrder> orderMap = storeOrderQueryService.getStoreOrderMapInIds(merchantId, storeId, orderIds);
        for (StoreMealDTO storeMealDTO : resultList) {
            StoreOrder storeOrder = orderMap.get(storeMealDTO.getOrderId());
            StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
            storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        }
        storeMealMultiHelper.sortStoreMealListTimeDesc(resultList);
        return resultList;
    }

    @Override
    public List<StoreMealDTO> getStoreMealCheckoutHistoryByParam(StoreMealHistoryQueryParam storeMealHistoryQueryParam) throws T5weiException, TException {
        List<StoreMealCheckout> storeMealCheckouts = storeMealMultiService.getStoreMealCheckoutHistory(storeMealHistoryQueryParam);
        if (storeMealCheckouts == null || storeMealCheckouts.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        int merchantId = storeMealHistoryQueryParam.getMerchantId();
        long storeId = storeMealHistoryQueryParam.getStoreId();
        long portId = storeMealHistoryQueryParam.getPortId();
        List<Long> portIds = new ArrayList<Long>();
        portIds.add(portId);
        Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> resultList = storeMealMultiHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts, portMap);//按出餐口出餐
        if (resultList == null || resultList.isEmpty()) {
            return new ArrayList<StoreMealDTO>();
        }
        List<String> orderIds = storeMealMultiHelper.getOrderIdsInStoreMealCheckouts(storeMealCheckouts);
        Map<String, StoreMealCheckoutRecord> checkoutRecordMap = storeMealCheckoutRecordDao.getStoreMealCheckoutRecord(merchantId, storeId, portId, orderIds, true);
        Map<String, Integer> takeupOrderMap = storeMealMultiService.getStoreMealTakeupOrderNum(merchantId, storeId, orderIds, portIds);
		for (StoreMealDTO storeMealDTO : resultList) {
			int checkoutSeq = 0;
			StoreMealCheckoutRecord storeMealCheckoutRecord = checkoutRecordMap.get(storeMealDTO.getOrderId());
			if (storeMealCheckoutRecord != null) {
				checkoutSeq = storeMealCheckoutRecord.getCheckoutSeq();
			}
			Integer takeupOrderNum = takeupOrderMap.get(storeMealDTO.getOrderId());
			if (takeupOrderNum != null && takeupOrderNum > 0) {
				checkoutSeq++;
			}
	        storeMealDTO.setCheckoutSeq(checkoutSeq);
		}
        Map<String, StoreOrder> orderMap = storeOrderQueryService.getStoreOrderMapInIds(merchantId, storeId, orderIds);
        for (StoreMealDTO storeMealDTO : resultList) {
            StoreOrder storeOrder = orderMap.get(storeMealDTO.getOrderId());
            StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
            storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        }
        storeMealMultiHelper.sortStoreMealListTimeDesc(resultList);
        return resultList;
    }

	@Override
	public StoreMealDTO getStoreMealCheckoutHistoryById(int merchantId, long storeId, String orderId, int takeSerialSeq) throws T5weiException, TException {
		List<StoreMealCheckout> storeMealCheckouts = storeMealMultiService.getStoreMealCheckoutHistoryById(merchantId, storeId, orderId, takeSerialSeq);
        if (storeMealCheckouts == null || storeMealCheckouts.isEmpty()) {
            return new StoreMealDTO();
        }
        long portId = storeMealCheckouts.get(0).getPortId();
        List<Long> portIds = new ArrayList<Long>();
        portIds.add(portId);
        Map<Long, StoreMealPort> portMap = storeMealPortService.getStoreMealPortMapInIds(merchantId, storeId, portIds);
        List<StoreMealDTO> storeMealDTOs = storeMealMultiHelper.getStoreMealDTOByStoreMealCheckouts(storeMealCheckouts, portMap);//按出餐口出餐
		if (storeMealDTOs == null || storeMealDTOs.isEmpty()) {
			return new StoreMealDTO();
		}
        StoreMealDTO storeMealDTO = storeMealDTOs.get(0); 
        List<StoreMealTakeup> storeMealTakeups = storeMealMultiService.getStoreMealTakeups(merchantId, storeId, orderId, portId, true);
        StoreMealCheckoutRecord storeMealCheckoutRecord = storeMealCheckoutRecordDao.getStoreMealCheckoutRecordByOrderId(merchantId, storeId, orderId, portId, false);
		if (storeMealCheckoutRecord != null) {
			int checkoutSeq = storeMealCheckoutRecord.getCheckoutSeq();
			storeMealDTO.setPackagedSeq(storeMealCheckoutRecord.getPackagedSeq());
			if (storeMealTakeups != null && storeMealTakeups.size() > 0) {
				checkoutSeq++;
			}
			storeMealDTO.setCheckoutSeq(checkoutSeq);
		}
        List<String> orderIds = storeMealMultiHelper.getOrderIdsInStoreMealCheckouts(storeMealCheckouts);
        Map<String, StoreOrder> orderMap = storeOrderQueryService.getStoreOrderMapInIds(merchantId, storeId, orderIds);
        StoreOrder storeOrder = orderMap.get(storeMealDTO.getOrderId());
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        storeMealDTO.setStoreOrderDTO(storeOrderDTO);
		return storeMealDTO;
	}

    /**
     * 查询待打印出餐单数量
     * @param merchantId
     * @param storeId
     * @return
     */
    @Override
    public int countStoreMealTakeupOrders(int merchantId, long storeId, long appcopyId) throws TException {
        //Pad所负责出餐口
        int checkoutType = StoreMealPortCheckoutTypeEnum.AUTO.getValue();
        List<StoreMealPort> storeMealPorts = storeMealPortService.getStoreAppTaskMealPorts(merchantId, storeId, appcopyId, checkoutType);
        if (storeMealPorts == null || storeMealPorts.isEmpty()) {
           return 0;
        }
        List<Long> portIds = new ArrayList<Long>();
            for (StoreMealPort port : storeMealPorts) {
                portIds.add(port.getPortId());
		 }
		return storeMealMultiService.countStoreMealTakeupOrders(merchantId, storeId, portIds);
	}
    
}
