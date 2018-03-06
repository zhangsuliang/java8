package com.huofu.module.i5wei.meal.service;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealAutoPrintParam;
import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealCheckoutItemParam;
import huofucore.facade.i5wei.meal.StoreMealCheckoutTypeEnum;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.meal.StoreMealHistoryQueryParam;
import huofucore.facade.i5wei.meal.StoreMealsCheckoutParam;
import huofucore.facade.i5wei.mealport.StoreMealPortCheckoutTypeEnum;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderOptlogTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.order.StoreOrderTradeStatusEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealCheckoutRecordDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckout;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckoutRecord;
import com.huofu.module.i5wei.meal.entity.StoreMealSweep;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.mealport.entity.StoreMealTask;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.service.QueryProductPortParam;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderOptlogDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderSubitem;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;

@Service
public class StoreMealMultiService {

    private static final Log log = LogFactory.getLog(StoreMealMultiService.class);

    @Autowired
    private StoreOrderDAO storeOrderDao;

    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDao;

    @Autowired
    private StoreMealCheckoutDAO storeMealCheckoutDao;

    @Autowired
    private StoreMealCheckoutRecordDAO storeMealCheckoutRecordDao;

    @Autowired
    private StoreProductDAO storeProductDao;
    
    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private StoreOrderOptlogDAO storeOrderOptlogDao;

    @Autowired
    private StoreChargeItemService storeChargeItemService;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreInventoryService storeInventoryService;

    @Autowired
    private StoreMealMultiHelper storeMealMultiHelper;
    
    @Autowired
    private StoreMealPortService storeMealPortService;
    
    @Autowired
    private StoreHeartbeatService storeHeartbeatService;
    
    @Autowired
    private StoreMealTakeupDAO StoreMealTakeupDao;
    
    @Autowired
    private StoreTableRecordService storeTableRecordService;
    
    @Autowired
    private Store5weiSettingService store5weiSettingService;
    
    @Autowired
    private StoreMealSweepService storeMealSweepService;
    
    @Autowired
    private StoreMealSweepRecordService storeMealSweepRecordService;
    
    /**
     * 订单取餐，订单信息进入后厨出餐流程
     *
     * @param storeOrder
     * @param takeSerialNumber
     * @param takeMode
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreMealTakeup> storeOrderTakeCodeByPort(StoreOrder storeOrder, int takeSerialNumber) throws T5weiException {
    	boolean enableSlave = false;
        String orderId = storeOrder.getOrderId();
        int merchantId = storeOrder.getMerchantId();
        long storeId = storeOrder.getStoreId();
        long repastDate = storeOrder.getRepastDate();
        long timeBucketId = storeOrder.getTimeBucketId();
        StoreOrderTakeModeEnum takeMode = StoreOrderTakeModeEnum.findByValue(storeOrder.getTakeMode());
        int siteNumber = storeOrder.getSiteNumber();
        boolean payAfter = storeOrder.isPayAfter();
		long tableRecordId = storeOrder.getTableRecordId();
        int count = storeMealTakeupDao.countStoreMealTakeupsByOrderId(merchantId, storeId, orderId, enableSlave);
        if (count > 0) {
            return new ArrayList<StoreMealTakeup>();
        }
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId, enableSlave);
        int printMode = store5weiSetting.getPrintMode();
        List<QueryProductPortParam> queryProductPortParams = storeMealMultiHelper.storeOrderToQueryProductPortParams(storeOrder);
        Map<String, Long> productPortMap = storeChargeItemService.getPortIdMap(merchantId, storeId, queryProductPortParams);
        //查询加工档口关联的传菜间
        Map<Long, StoreMealPortSend> storeMealPorts = this.storeMealPortService.getStoreMealPorts4SendPort(merchantId, storeId, new ArrayList<Long>(productPortMap.values()), enableSlave);
        Set<Long> divChargeItems = storeMealMultiHelper.getDivChargeItemIds(productPortMap);
        StoreMealPort parkagePort = storeMealPortService.getParkagePort(merchantId, storeId);
        StoreMealPort deliveryPort = storeMealPortService.getDeliveryPort(merchantId, storeId);
        List<StoreMealTakeup> storeMealTakeUpItems = new ArrayList<StoreMealTakeup>();
        List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
        List<StoreMealSweep> storeMealSweeps = new ArrayList<StoreMealSweep>();
        for (StoreOrderItem storeOrderItem : storeOrderItems) {
            long chargeItemId = storeOrderItem.getChargeItemId();
            double orderAmount = storeOrderItem.getAmount();
            double packedAmount;
            if (takeMode == StoreOrderTakeModeEnum.DINE_IN) {
                packedAmount = Double.valueOf(0);
            } else if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT || takeMode == StoreOrderTakeModeEnum.SEND_OUT) {
                packedAmount = orderAmount;
            } else {
                packedAmount = storeOrderItem.getPackedAmount();
            }
            storeOrderItem.setPackedAmount(packedAmount);
            String chargeItemName = storeOrderItem.getChargeItemName();
            List<StoreOrderSubitem> storeOrderSubitems = storeOrderItem.getStoreOrderSubitems();
            for (StoreOrderSubitem storeOrderSubitem : storeOrderSubitems) {
                double amount = storeOrderSubitem.getAmount();
                long productId = storeOrderSubitem.getProductId();
                String productName = storeOrderSubitem.getProductName();
                String unit = storeOrderSubitem.getUnit();
                String remark = storeOrderSubitem.getRemark();
                String key = chargeItemId + "_" + productId;
                long portId = productPortMap.getOrDefault(key, 0L);
                StoreMealTakeup storeMealTakeup = new StoreMealTakeup();
                storeMealTakeup.setOrderId(orderId);
                storeMealTakeup.setMerchantId(merchantId);
                storeMealTakeup.setStoreId(storeId);
                storeMealTakeup.setPortId(portId);
                storeMealTakeup.setRepastDate(repastDate);
                storeMealTakeup.setTakeMode(takeMode.getValue());
                storeMealTakeup.setTakeSerialNumber(takeSerialNumber);
                storeMealTakeup.setSiteNumber(siteNumber);
                storeMealTakeup.setTimeBucketId(timeBucketId);
                storeMealTakeup.setChargeItemId(chargeItemId);
                storeMealTakeup.setChargeItemName(chargeItemName);
                storeMealTakeup.setProductId(productId);
                storeMealTakeup.setProductName(productName);
                storeMealTakeup.setAmount(amount);
                storeMealTakeup.setUnit(unit);
                storeMealTakeup.setRemark(remark);
                storeMealTakeup.setUpdateTime(System.currentTimeMillis());
                storeMealTakeup.setCreateTime(System.currentTimeMillis());
                storeMealTakeup.setSpicyLevel(storeOrderItem.getSpicyLevel());
                storeMealTakeup.setPayAfter(payAfter);
                storeMealTakeup.setTableRecordId(tableRecordId);
                storeMealTakeup.setWeightEnabled(storeOrderItem.isWeightEnabled());
                StoreMealTakeup notPackaged = null;
                boolean isPaperSweep = getPaperSweep(storeMealPorts, storeMealTakeup, printMode);
                if (packedAmount > 0) {
                    // 打包部分
                    storeMealTakeup.setPackaged(true);
                    storeMealTakeup.setAmountOrderTakeup(packedAmount, printMode, isPaperSweep);
                    // 指定打包出餐口
                    if (printMode == StorePrintModeEnum.NORMAL_PRINT.getValue() && parkagePort != null && parkagePort.getPortId() > 0) {
                        storeMealTakeup.setPortId(parkagePort.getPortId());
                    }
                    //外卖出餐口
                    if (printMode == StorePrintModeEnum.NORMAL_PRINT.getValue() && deliveryPort != null && deliveryPort.getPortId() > 0 && takeMode == StoreOrderTakeModeEnum.SEND_OUT) {
                        storeMealTakeup.setPortId(deliveryPort.getPortId());
                    }
                    storeMealTakeUpItems.add(storeMealTakeup);
                    // 堂食部分
                    double inAmount = NumberUtil.sub(orderAmount, packedAmount);
                    if (inAmount > 0) {
                        notPackaged = BeanUtil.copy(storeMealTakeup, StoreMealTakeup.class);
                        notPackaged.setPortId(portId);
                        notPackaged.setPackaged(false);
                        notPackaged.setAmountOrderTakeup(inAmount, printMode, isPaperSweep);
                        storeMealTakeUpItems.add(notPackaged);
                    }
                } else {
                    //全部为堂食
                    storeMealTakeup.setPortId(portId);
                    storeMealTakeup.setPackaged(false);
                    storeMealTakeup.setAmountOrderTakeup(orderAmount, printMode, isPaperSweep);
                    storeMealTakeUpItems.add(storeMealTakeup);
                }
                if (isPaperSweep) {
                    if(storeMealTakeup.getSendPortId() > 0){
                        StoreMealSweep storeMealSweep = this.storeMealSweepService.buildStoreMealSweep(storeMealTakeup, storeMealTakeup.getAmountOrder(), System.currentTimeMillis(), false, 0, 0);
                        storeMealSweeps.add(storeMealSweep);
                    }
                    if (notPackaged != null && notPackaged.getSendPortId() > 0) {
                        StoreMealSweep storeMealNotPackagedSweep = this.storeMealSweepService.buildStoreMealSweep(storeMealTakeup, storeMealTakeup.getAmountOrder(), System.currentTimeMillis(), false, 0, 0);
                        storeMealSweeps.add(storeMealNotPackagedSweep);
                    }
                }
            }
        }
        // 判断是否展现产品详情 & 非手动打印时不打印小票的出餐口直接变为已出餐
        Set<Long> checkoutPortIds = Sets.newHashSet();
        List<StoreMealCheckout> storeMealCheckoutItems = Lists.newArrayList();
        Map<Long, StoreMealPort> mealPortMap = storeMealPortService.getStoreMealPortMap(merchantId, storeId, true);
        Iterator<StoreMealTakeup> it = storeMealTakeUpItems.iterator();
        while(it.hasNext()){
        	StoreMealTakeup storeMealTakeup = it.next();
        	if (divChargeItems.contains(storeMealTakeup.getChargeItemId())) {
                storeMealTakeup.setShowProducts(true);
            }
            long portId = storeMealTakeup.getPortId();
            StoreMealPort storeMealPort = mealPortMap.get(portId);
			if (storeMealPort.getPrinterPeripheralId() <= 0) {
				StoreMealTask storeMealTask = storeMealPort.getStoreMealTask();
				if (this.isAutoMealCheckout(storeMealTask)){
					// 待出餐变为已出餐
					StoreMealCheckout storeMealCheckout = BeanUtil.copy(storeMealTakeup, StoreMealCheckout.class);
					storeMealCheckout.setPortId(portId);
		            storeMealCheckout.setAppcopyId(0);
		            storeMealCheckout.setCheckoutType(StoreMealCheckoutTypeEnum.AUTO.getValue());
		            storeMealCheckout.setStaffId(0);
		            storeMealCheckout.setAmountCheckout(storeMealTakeup.getRemainTakeup());
		            storeMealCheckout.setTakeSerialSeq(1);
		            storeMealCheckout.setPrinted(true);// 直接标为已打印
		            storeMealCheckout.setCreateTime(System.currentTimeMillis());
					storeMealCheckoutItems.add(storeMealCheckout);
					// 待出餐份数变为0
					storeMealTakeup.setRemainTakeup(0);
					// 已出餐加工档口
					checkoutPortIds.add(portId);
				}
            }
        }
        if (printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
            storeMealSweepService.batchCreate(storeMealSweeps);//创建划菜
            storeMealSweepRecordService.batchCreateMealSweepRecords(merchantId, storeId, storeOrder, storeMealTakeUpItems);//创建划菜记录
        }
        storeMealTakeupDao.batchCreate(storeMealTakeUpItems);
		if (!storeMealCheckoutItems.isEmpty()){
			storeMealCheckoutDao.batchCreate(storeMealCheckoutItems);
			for (long portId : checkoutPortIds){
				StoreMealCheckoutRecord storeMealCheckoutRecord = BeanUtil.copy(storeOrder, StoreMealCheckoutRecord.class);
				storeMealCheckoutRecord.setPortId(portId);
				storeMealCheckoutRecord.setCheckoutSeq(1);
				storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
				storeMealCheckoutRecord.setCreateTime(System.currentTimeMillis());
				storeMealCheckoutRecord.create();
			}
        }
		// 全部为已出餐则订单出餐完成
		if (storeMealTakeUpItems.size() == storeMealCheckoutItems.size()){
			this.updateOrderPrepareMealFinish(merchantId, storeId, 0, storeOrder, StoreMealCheckoutTypeEnum.AUTO.getValue());
		} else {
			storeOrder.setTradeStatus(StoreOrderTradeStatusEnum.CODE_TAKED.getValue());
			// 更新当前最大出餐序列号（心跳监控）
	        long currentRepastDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
			if (!storeMealTakeUpItems.isEmpty() && currentRepastDate == repastDate){
	            storeHeartbeatService.changeLastSerialNumber(merchantId, storeId, repastDate, takeSerialNumber);
	        }
		}
        return storeMealTakeUpItems;
    }

    private boolean isAutoMealCheckout(StoreMealTask storeMealTask){
    	if(storeMealTask == null){
    		return false;
    	}
    	if(storeMealTask.getCheckoutType() == StoreMealCheckoutTypeEnum.MANUAL.getValue()){
    		return false;
    	}
    	return true;
    }

    /**
     * 设置待出餐的传菜口
     * @param storeMealPorts
     * @param storeMealTakeup
     * @param printMode
     * @return
     */
    public boolean getPaperSweep(Map<Long, StoreMealPortSend> storeMealPorts, StoreMealTakeup storeMealTakeup, int printMode) {
        if (printMode == StorePrintModeEnum.NORMAL_PRINT.getValue()) {
            return false;
        }
        boolean isPaperSweep = false;
        StoreMealPortSend storeMealPortSend = storeMealPorts.get(storeMealTakeup.getPortId());
        if (storeMealPortSend == null || storeMealPortSend.getSweepType() == StoreMealSweepTypeEnum.PAPER_SWEEP.getValue()) {
            isPaperSweep = true;
            storeMealTakeup.setRemainSend(0);//加工档口没有关联传菜口或传菜口为纸划菜则自动划掉
            storeMealTakeup.setSweepTime(System.currentTimeMillis());
        }
        if (storeMealPortSend != null) {
            storeMealTakeup.setSendPortId(storeMealPortSend.getSendPortId());
        }
        return isPaperSweep;
    }

    /**
     * 得到已取号、未出餐订单产品信息
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param portId
     * @param takeSerialNumber
     * @return
     * @throws T5weiException
     */
    public List<StoreMealTakeup> getStoreMealTakeup(int merchantId, long storeId, long portId, int takeSerialNumber) throws T5weiException {
    	boolean enableSlave = true;
    	if (merchantId == 0 || storeId == 0 ) {
			return new ArrayList<StoreMealTakeup>();
		}
		//没有打包出餐口，portId不是打包出餐口
		List<Long> portIds = new ArrayList<Long>();
		if (portId > 0) {
			portIds.add(portId);
		}
		List<StoreMealTakeup> storeMealTakeups = storeMealTakeupDao.getStoreMealTakeups(merchantId, storeId, 0, 0, portIds, takeSerialNumber, enableSlave);
        return storeMealTakeups;
    }

    /**
     * 得到已取号、未出餐订单产品信息
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param timeBucketId
     * @param portIds
     * @param refreshTime
     * @return
     * @throws T5weiException
     */
    public List<StoreMealTakeup> getStoreMealTakeup(int merchantId, long storeId, List<Long> portIds) throws T5weiException {
    	boolean enableSlave = true;
		if (merchantId == 0 || storeId == 0 || portIds == null || portIds.isEmpty()) {
			return new ArrayList<StoreMealTakeup>();
		}
        return storeMealTakeupDao.getStoreMealTakeups(merchantId, storeId, 0, 0, portIds, enableSlave);
    }
    
	public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, List<String> orderIds, List<Long> portIds) {
		boolean enableSlave = false;
		return storeMealTakeupDao.getStoreMealTakeups(merchantId, storeId, orderIds, portIds, enableSlave);
	}
	
	public Map<String, Integer> getStoreMealTakeupOrderNum(int merchantId, long storeId, List<String> orderIds, List<Long> portIds) {
		boolean enableSlave = true;
		return storeMealTakeupDao.getStoreMealTakeupOrderNum(merchantId, storeId, orderIds, portIds, enableSlave);
	}
	
	public List<StoreMealTakeup> getStoreMealTakeups(int merchantId, long storeId, String orderId, Long portId, boolean enableSlave) {
		return storeMealTakeupDao.getStoreMealTakeups(merchantId, storeId, orderId, portId, enableSlave);
	}
	
	public List<String> getStoreMealTakeupOrders(int merchantId, long storeId, List<Long> portIds, int orderNum){
		boolean enableSlave = false;
		return storeMealTakeupDao.getStoreMealTakeupOrders(merchantId, storeId, portIds, orderNum, enableSlave);
	}
	
	public int countStoreMealTakeupOrders(int merchantId, long storeId, List<Long> portIds){
		boolean enableSlave = true;
		int count1 = storeMealTakeupDao.countStoreMealTakeups(merchantId, storeId, portIds, enableSlave);
		int count2 = storeMealCheckoutDao.countStoreMealsHistoryNotPrinted(merchantId, storeId, portIds, enableSlave);
		return count1 + count2;
	}
	
    /**
     * 后厨自动出餐订单产品信息
     *
     * @param storeMealCheckoutParam
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Map<String,Object> storeMealCheckoutByMealDTO(StoreMealDTO storeMealDTO, long appcopyId, int checkoutType) throws T5weiException {
        int merchantId = storeMealDTO.getMerchantId();
        long storeId = storeMealDTO.getStoreId();
        String orderId = storeMealDTO.getOrderId();
        long repastDate = storeMealDTO.getRepastDate();
        long staffId = 0L; //系统自动出，没有员工操作
        long portId = storeMealDTO.getPortId();
        if (merchantId == 0 || storeId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "store id can not null");
        }
        if (orderId == null || orderId.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ID_CAN_NOT_NULL.getValue(), "store order_id can not null");
        }
        if (repastDate == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_CAN_NOT_NULL.getValue(), "store repast date can not null");
        }
        //计算出餐数量
        List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
        Map<Long, Double> storeMealCheckoutItemMap = new HashMap<Long, Double>();
        for (StoreMealChargeDTO mealChargeDTO : storeMealChargeDTOs) {
        	long chargeItemId = mealChargeDTO.getChargeItemId();
            double amount = 0;
            if (storeMealCheckoutItemMap.containsKey(chargeItemId)) {
                amount = storeMealCheckoutItemMap.get(chargeItemId) + mealChargeDTO.getAmount();
            } else {
                amount = mealChargeDTO.getAmount();
            }
            storeMealCheckoutItemMap.put(chargeItemId, amount);
        }
        List<Long> takeupTids = storeMealDTO.getTakeupTids();
        repastDate = DateUtil.getBeginTime(repastDate, null);
        boolean packaged = storeMealDTO.isPackaged();
        List<StoreMealTakeup> storeMealTakeUpItems = storeMealTakeupDao.getStoreMealTakeupsByTids(merchantId, storeId, takeupTids, true);
        if (storeMealTakeUpItems == null || storeMealTakeUpItems.isEmpty()) {
        	if(log.isDebugEnabled()){
        		log.debug("####checkoutStoreMealsByPort storeMealTakeUpItems is null, input="+storeMealDTO);
        	}
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CHECKOUT_NUM_MORE_THEN_REMAIN_NUM.getValue(),
                    DataUtil.infoWithParams("there is no store meals, storeId=#1, orderId=#2, chargeItemId=#3 ", new Object[]{storeId, orderId}));
        }
        StoreMealsCheckoutParam storeMealsCheckoutParam = new StoreMealsCheckoutParam();
        storeMealsCheckoutParam.setMerchantId(merchantId);
        storeMealsCheckoutParam.setStoreId(storeId);
        storeMealsCheckoutParam.setOrderId(orderId);
        storeMealsCheckoutParam.setRepastDate(repastDate);
        storeMealsCheckoutParam.setPortId(portId);
        storeMealsCheckoutParam.setPackaged(packaged);
        storeMealsCheckoutParam.setStaffId(staffId);
        storeMealsCheckoutParam.setAppcopyId(appcopyId);
        storeMealsCheckoutParam.setCheckoutType(checkoutType);
        Map<String,Object> resultMap = this.storeMealCheckout(storeMealsCheckoutParam, storeMealCheckoutItemMap, storeMealTakeUpItems);
        resultMap.put("storeMeal", storeMealDTO);
        return resultMap;
    }

    /**
     * 后厨出餐订单产品信息
     *
     * @param storeMealCheckoutParam
     * @return
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public Map<String,Object> storeMealCheckout(StoreMealsCheckoutParam storeMealsCheckoutParam) throws T5weiException {
        int merchantId = storeMealsCheckoutParam.getMerchantId();
        long storeId = storeMealsCheckoutParam.getStoreId();
        String orderId = storeMealsCheckoutParam.getOrderId();
        long repastDate = storeMealsCheckoutParam.getRepastDate();
        long portId = storeMealsCheckoutParam.getPortId();
        if (merchantId == 0 || storeId == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "store id can not null");
        }
        if (orderId == null || orderId.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ID_CAN_NOT_NULL.getValue(), "store order_id can not null");
        }
        if (repastDate == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_REPAST_DATE_CAN_NOT_NULL.getValue(), "store repast date can not null");
        }
        List<StoreMealCheckoutItemParam> storeMealCheckoutItems = storeMealsCheckoutParam.getStoreMealCheckoutItems();
        if (storeMealCheckoutItems == null || storeMealCheckoutItems.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "store meal checkout items can not null");
        }
        //计算出餐数量
        Map<Long, Double> storeMealCheckoutItemMap = new HashMap<Long, Double>();
        for (StoreMealCheckoutItemParam storeMealCheckoutItemParam : storeMealCheckoutItems) {
            long chargeItemId = storeMealCheckoutItemParam.getChargeItemId();
            double amount = 0;
            if (storeMealCheckoutItemMap.containsKey(chargeItemId)) {
                amount = storeMealCheckoutItemMap.get(chargeItemId) + storeMealCheckoutItemParam.getAmount();
            } else {
                amount = storeMealCheckoutItemParam.getAmount();
            }
            storeMealCheckoutItemMap.put(chargeItemId, amount);
        }
        boolean packaged = storeMealsCheckoutParam.isPackaged();
        List<StoreMealTakeup> storeMealTakeUpItems = storeMealTakeupDao.getStoreMealTakeupsByOrderPackaged(merchantId, storeId, orderId, portId, packaged, true);
        if (storeMealTakeUpItems == null || storeMealTakeUpItems.isEmpty()) {
        	if(log.isDebugEnabled()){
        		log.debug("####checkoutStoreMealsByPort storeMealTakeUpItems is null, input="+storeMealsCheckoutParam);
        	}
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CHECKOUT_NUM_MORE_THEN_REMAIN_NUM.getValue(),
                    DataUtil.infoWithParams("there is no store meals, storeId=#1, orderId=#2, chargeItemId=#3 ", new Object[]{storeId, orderId}));
        }
        Map<String,Object> resultMap = this.storeMealCheckout(storeMealsCheckoutParam, storeMealCheckoutItemMap, storeMealTakeUpItems);
        return resultMap;
    }
    
    private Map<String,Object> storeMealCheckout(StoreMealsCheckoutParam storeMealsCheckoutParam, Map<Long, Double> storeMealCheckoutItemMap, List<StoreMealTakeup> storeMealTakeUpItems) throws T5weiException {
    	Map<String,Object> resultMap = new HashMap<String,Object>();
    	int merchantId = storeMealsCheckoutParam.getMerchantId();
        long storeId = storeMealsCheckoutParam.getStoreId();
        String orderId = storeMealsCheckoutParam.getOrderId();
        long staffId = storeMealsCheckoutParam.getStaffId();
        long portId = storeMealsCheckoutParam.getPortId();
        boolean packaged = storeMealsCheckoutParam.isPackaged();
        long appcopyId = storeMealsCheckoutParam.getAppcopyId();
        int checkoutType = storeMealsCheckoutParam.getCheckoutType();
        List<StoreMealCheckout> storeMealCheckouts = new ArrayList<StoreMealCheckout>();
    	//出餐分单记录
        StoreMealCheckoutRecord storeMealCheckoutRecord = storeMealCheckoutRecordDao.getStoreMealCheckoutRecordByOrderId(merchantId, storeId, orderId, portId, true);
        StoreOrder storeOrder = storeOrderDao.getById(merchantId, storeId, orderId, false, false);
        int takeSerialSeq = 0;
        if (storeMealCheckoutRecord != null) {
            takeSerialSeq = storeMealCheckoutRecord.getCheckoutSeq();
            takeSerialSeq = takeSerialSeq + 1;
            int packagedSeq = storeMealCheckoutRecord.getPackagedSeq();
            if (packaged) {
                packagedSeq = packagedSeq + 1;
            }
            storeMealCheckoutRecord.setCheckoutSeq(takeSerialSeq);
            storeMealCheckoutRecord.setPackagedSeq(packagedSeq);
            storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
        } else {
            takeSerialSeq = 1;
            int packagedSeq = 0;
            if (packaged) {
                packagedSeq = 1;
            }
            storeMealCheckoutRecord = new StoreMealCheckoutRecord();
            storeMealCheckoutRecord.setMerchantId(merchantId);
            storeMealCheckoutRecord.setStoreId(storeId);
            storeMealCheckoutRecord.setStaffId(staffId);
            storeMealCheckoutRecord.setOrderId(orderId);
            storeMealCheckoutRecord.setPortId(portId);
            storeMealCheckoutRecord.setUserId(storeOrder.getUserId());
            storeMealCheckoutRecord.setRepastDate(storeOrder.getRepastDate());
            storeMealCheckoutRecord.setTimeBucketId(storeOrder.getTimeBucketId());
            storeMealCheckoutRecord.setTakeSerialNumber(storeOrder.getTakeSerialNumber());
            storeMealCheckoutRecord.setCheckoutSeq(takeSerialSeq);
            storeMealCheckoutRecord.setPackagedSeq(packagedSeq);
            storeMealCheckoutRecord.setUpdateTime(System.currentTimeMillis());
            storeMealCheckoutRecord.setCreateTime(System.currentTimeMillis());
        }
        storeMealCheckoutRecordDao.replace(storeMealCheckoutRecord);
        //出餐记录
        long currentTime = System.currentTimeMillis();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeUpItems) {
            long chargeItemId = storeMealTakeup.getChargeItemId();
            double remainTakeup = storeMealTakeup.getRemainTakeup();
            Double checkoutAmount = storeMealCheckoutItemMap.get(chargeItemId);
            if (checkoutAmount == null) {
                continue;
            }
            if (remainTakeup < checkoutAmount) {
				if (remainTakeup > 0){
					log.error("####checkoutStoreMealsByPort storeMealTakeup = "+ JsonUtil.build(storeMealTakeup) + ", input="+storeMealsCheckoutParam);
            	}
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_CHECKOUT_NUM_MORE_THEN_REMAIN_NUM.getValue(),
                        DataUtil.infoWithParams("store checkout num more then order remain num, storeId=#1, orderId=#2, chargeItemId=#3 ", new Object[]{storeId, orderId, chargeItemId}));
            }
            // 更新待出餐记录
            double remainAmount = NumberUtil.sub(remainTakeup, checkoutAmount);
            storeMealTakeup.setRemainTakeup(remainAmount);
            storeMealTakeup.setUpdateTime(currentTime);
			storeMealTakeup.setCheckoutTime(currentTime);
            storeMealTakeupDao.update(storeMealTakeup);
            // 添加出餐记录
            StoreMealCheckout storeMealCheckout = BeanUtil.copy(storeMealTakeup, StoreMealCheckout.class);
            storeMealCheckout.setPortId(portId);
            storeMealCheckout.setAppcopyId(appcopyId);
            storeMealCheckout.setCheckoutType(checkoutType);
            storeMealCheckout.setStaffId(staffId);
            storeMealCheckout.setAmountCheckout(checkoutAmount);
            storeMealCheckout.setTakeSerialSeq(takeSerialSeq);
			if (checkoutType == StoreMealPortCheckoutTypeEnum.MANUAL.getValue()) {
				storeMealCheckout.setPrinted(true);// 手动出餐直接标为已打印
			} else {
				storeMealCheckout.setPrinted(false);
			}
            storeMealCheckout.setCreateTime(System.currentTimeMillis());
            storeMealCheckouts.add(storeMealCheckout);
        }
        storeMealCheckoutDao.batchCreate(storeMealCheckouts);
		// 更新库存
        try {
            storeInventoryService.updateInventoryDateByOrder(storeOrder);
        } catch (Throwable e) {
            log.error("#### fail to updateInventoryByOrder ", e);
        }
        resultMap.put("storeMealCheckoutRecord", storeMealCheckoutRecord);
        resultMap.put("storeMealCheckouts", storeMealCheckouts);
        return resultMap;
    }

    /**
     * 出餐完毕，修改订单状态为完成
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @throws T5weiException 
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public int updateOrderPrepareMealFinish(int merchantId, long storeId, long portId, String orderId, int checkoutType) throws T5weiException {
        StoreOrder storeOrder = storeOrderDao.getById(merchantId, storeId, orderId, true, true);
        return this.updateOrderPrepareMealFinish(merchantId, storeId, portId, storeOrder, checkoutType);
    }

    /**
     * 出餐完毕，修改订单状态为完成
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public int updateOrderPrepareMealFinish(int merchantId, long storeId, long portId, StoreOrder storeOrder, int checkoutType) throws T5weiException {
    	boolean enableSlave = false;
        int count = storeMealTakeupDao.countStoreMealTakeupsByOrderId(merchantId, storeId, storeOrder.getOrderId(), enableSlave);
        int clientType = storeOrder.getClientType();
        int optType = StoreOrderOptlogTypeEnum.MEAL_CHECKOUT.getValue();
        if (count == 0) {
            if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                storeOrderService.updateOrderPrepareMealFinish(merchantId, storeId, storeOrder);
                optType = StoreOrderOptlogTypeEnum.MEAL_CHECKOUT_COMPLETE.getValue();
            } else {
                storeOrderService.updateOrderTradeFinish(storeOrder);
                optType = StoreOrderOptlogTypeEnum.TRADE_FINISH.getValue();
            }
		} else {
        	count = storeMealTakeupDao.countStoreMealTakeupsByOrderId(merchantId, storeId, portId, storeOrder.getOrderId(), enableSlave);
        }
        storeOrderOptlogDao.createOptlog(storeOrder, 0, clientType, optType, "checkoutType=" + checkoutType);
        if (storeOrder.isHasDelivery()) {
            try {
                this.storeHeartbeatService.updateDeliveryPreparedNotifyTime(merchantId, storeId, System.currentTimeMillis());
            } catch (Exception e) {
                log.error("updateDeliveryPreparedNotifyTime err. " + e.getMessage());
            }
        }
        //桌台信息更新
        storeTableRecordService.updateTableRecordMealInfo(storeOrder);
        //返回信息
        return count;
    }

    /**
     * 日出餐历史
     *
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @return
     * @throws T5weiException
     */
    public List<StoreMealCheckout> getStoreMealCheckoutHistory(int merchantId, long storeId, long repastDate, long portId) throws T5weiException {
    	boolean enableSlave = true;
        repastDate = DateUtil.getBeginTime(repastDate, null);
        return storeMealCheckoutDao.getStoreMealCheckouts(merchantId, storeId, repastDate, portId, enableSlave);
    }
    
    /**
     * 日出餐历史
     *
     * @param storeMealHistoryQueryParam
     * @return
     * @throws T5weiException
     */
    public List<StoreMealCheckout> getStoreMealCheckoutHistory(StoreMealHistoryQueryParam storeMealHistoryQueryParam) throws T5weiException {
    	boolean enableSlave = true;
    	List<StoreMealCheckoutRecord> storeMealCheckoutRecords = storeMealCheckoutRecordDao.getStoreMealCheckoutRecord(storeMealHistoryQueryParam, enableSlave);
		if (storeMealCheckoutRecords == null || storeMealCheckoutRecords.isEmpty()) {
			return new ArrayList<StoreMealCheckout>();
		}
		List<Integer> takeSerialNumbers = new ArrayList<Integer>();
		for (StoreMealCheckoutRecord storeMealCheckoutRecord : storeMealCheckoutRecords) {
			takeSerialNumbers.add(storeMealCheckoutRecord.getTakeSerialNumber());
		}
        return storeMealCheckoutDao.getStoreMealCheckouts(storeMealHistoryQueryParam, takeSerialNumbers, enableSlave);
    }
	
	public List<StoreMealCheckout> getStoreMealsHistoryNotPrinted(int merchantId, long storeId, List<Long> portIds){
		boolean enableSlave = false;
		return storeMealCheckoutDao.getStoreMealsHistoryNotPrinted(merchantId, storeId, portIds, enableSlave);
	}
	
	public void batchUpdatePrinted(int merchantId, long storeId, List<StoreMealAutoPrintParam> storeMealAutoPrintParams) throws T5weiException {
		if (storeMealAutoPrintParams == null || storeMealAutoPrintParams.isEmpty()) {
			return;
		}
        storeMealCheckoutDao.batchUpdatePrinted(merchantId, storeId, storeMealAutoPrintParams);
    }
	
	public List<StoreProduct> getStoreProductByMeals(List<StoreMealTakeup> storeMealTakeups){
		if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
			return new ArrayList<StoreProduct>();
		}
		Set<Long> productIds = new HashSet<Long>();
		for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
			productIds.add(storeMealTakeup.getProductId());
		}
		int merchantId = storeMealTakeups.get(0).getMerchantId(); 
		long storeId = storeMealTakeups.get(0).getStoreId();
		List<StoreProduct> statProducts = storeProductDao.getListInIds(merchantId, storeId, new ArrayList<Long>(productIds), false, true);
		if (statProducts == null || statProducts.isEmpty()) {
			return new ArrayList<StoreProduct>();
		}
		return statProducts;
	}
	
	public List<StoreMealCheckout> getStoreMealCheckoutHistoryById(int merchantId, long storeId, String orderId, int takeSerialSeq) throws T5weiException, TException {
		if (storeId == 0 || orderId == null || orderId.isEmpty()) {
			return null;
		}
		boolean enableSlave = true;
		return storeMealCheckoutDao.getStoreMealCheckoutHistoryById(merchantId, storeId, orderId, takeSerialSeq, enableSlave);
	}
	
	/**
	 * 查询排队人数
	 * @throws T5weiException 
	 */
	public int getStoreOrderTakeLineNumber(int merchantId,long storeId,String orderId) throws T5weiException{
		boolean enableSlave = true;
		//查询订单是否存在
        StoreOrder storeOrder = storeOrderDao.getStoreOrderById(merchantId, storeId, orderId, enableSlave);
        if (storeOrder == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(),
                    DataUtil.infoWithParams("store order not exist, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
        }
		//走主库查询
		List<String> orderIds = StoreMealTakeupDao.getStoreMealTakeupOrderIds(merchantId, storeId, null, enableSlave);
		int num = 0;
		for(String id:orderIds){
			if(orderId.equals(id)){
				return num;
			}
			num = num + 1;
		}
		return 0;
	}
}
