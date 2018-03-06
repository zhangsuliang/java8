package com.huofu.module.i5wei.meal.service;

import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.meal.dao.StoreMealSweepAmount;
import com.huofu.module.i5wei.meal.dao.StoreMealSweepDAO;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.meal.dao.StoreProductSweepAmount;
import com.huofu.module.i5wei.meal.entity.StoreMealSweep;
import com.huofu.module.i5wei.meal.entity.StoreMealSweepRecord;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderQueryService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.mealportsend.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class StoreMealSweepService {
    
    private static final Log log = LogFactory.getLog(StoreMealSweepService.class);
    
    @Autowired
    private StoreMealSweepDAO storeMealSweepDAO;
    
    @Autowired
    private StoreChargeItemService storeChargeItemService;

    @Autowired
    private StoreOrderQueryService storeOrderQueryService;

    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDao;
    
    @Autowired
    private StoreOrderService storeOrderService;
    
    @Autowired
    private StoreOrderDAO storeOrderDAO;
    
    @Autowired
    private StoreMealSweepHelper storeMealSweepHelper;
    
    @Autowired
    private StoreMealSweepRecordService storeMealSweepRecordService;
    
    @Autowired
    private StoreTableRecordService storeTableRecordService;
    
    @Autowired
    private StoreOrderHelper storeOrderHelper;
    
    @Autowired
    private StoreHeartbeatService storeHeartbeatService;
    
    /**
     * 统计等待上菜的已上菜品数量和全部菜品数量
     * @param merchantId
     * @param storeId
     * @param storeMealPortSend
     * @param enableSlave
     * @return
     * @throws TException
     */
    public List<StoreMealSweepAmount> countStoreMealSweepAmount(int merchantId, long storeId, long sendPortId, boolean enableSlave) throws TException {
        StoreMealSweptParam param = new StoreMealSweptParam();
        param.setMerchantId(merchantId);
        param.setStoreId(storeId);
        param.setSendPortId(sendPortId);
        List<StoreMealSweepAmount> storeMealSweepAmounts = this.storeMealSweepRecordService.countStoreMealSweepAmount(merchantId, storeId, sendPortId, enableSlave);
        return storeMealSweepAmounts;
    }
    
    public StoreMealTableSweepDTO getMealUnSweepOrdersDetail(StoreMealUnSweepParam param) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String orderId = param.getOrderId();
        long sendPortId = param.getSendPortId();
        long tableRecordId = param.getTableRecordId();
        boolean enableSlave = false;
        boolean forUpdate = true;
        List<StoreMealTakeup> storeMealSends = this.getStoreMealSends(merchantId, storeId, sendPortId, tableRecordId, orderId, enableSlave, forUpdate);
        List<StoreMealSweep> storeMealSweeps = this.getStoreMealSweeps(merchantId, storeId, sendPortId, tableRecordId, orderId, enableSlave, forUpdate);
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        StoreMealTableSweepDTO storeMealTableSweepDTO = this.storeMealSweepHelper.buildStoreMealTableSweepDTO(storeOrder, storeMealSends, storeMealSweeps, enableSlave);
        return storeMealTableSweepDTO;
    }
    
    @SuppressWarnings("unchecked")
    public List<StoreMealTableSweepDTO> getProductUnSweepOrdersDetail(StoreProductUnSweepParam param) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        long productId = param.getProductId();
        
        List<StoreMealTableSweepDTO> storeMealTableSweepDTOs = new ArrayList<StoreMealTableSweepDTO>();
        Map<String, Object> tableRecordIdsAndOrderIds = this.storeMealTakeupDao.getTableRecordIdsAndOrderIdsInProductId(merchantId, storeId, sendPortId, productId, false);//加分页，避免订单过多
        Set<String> orderIds = (Set<String>) tableRecordIdsAndOrderIds.get("orderIds");
        Set<Long> tableRecordIds = (Set<Long>) tableRecordIdsAndOrderIds.get("tableRecordIds");
        if((orderIds == null || orderIds.isEmpty()) && (tableRecordIds == null || tableRecordIds.isEmpty())){
            return storeMealTableSweepDTOs;
        }
        //1.包含product的收费项
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemService.getStoreChargeItemsContainProduct(merchantId, storeId, productId, false, false, false, false, false, 0);
        if(storeChargeItems.isEmpty()){
            return storeMealTableSweepDTOs;
        }
        List<Long> chargeItems = new ArrayList<Long>();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            chargeItems.add(storeChargeItem.getChargeItemId());
        }
        List<String> orderList = new ArrayList<String>(orderIds);
        List<Long> tableRecordIdList = new ArrayList<Long>(tableRecordIds);
        //2.使用订单包含的收费项StoreMealTakeup
        List<StoreMealTakeup> storeMealTakeups = this.storeMealTakeupDao.getStoreMealTakeupInChargeItemIds(merchantId, storeId, sendPortId, chargeItems, false);
        //3.按分单规则组装数据
        List<StoreMealDTO> sendMealDTOs = this.storeMealSweepHelper.buildStoreMealDTO4Send(merchantId, storeId, storeMealTakeups, false, true);
        //4.过滤不包含productId的分单
        this.storeMealSweepHelper.filterStoreMelDTOByProduct(productId, sendMealDTOs);
        storeMealTableSweepDTOs = this.storeMealSweepHelper.buildStoreMealTableSweepDTOs(merchantId, storeId, orderList, tableRecordIdList, sendMealDTOs, true);
        return storeMealTableSweepDTOs;
    }
    
    /**
     * 按桌台记录获取订单未划菜
     * @param merchantId
     * @param storeId
     * @param sendPortId
     * @param storeMealPorts
     * @param tableRecordId
     * @param enableSlave
     * @return
     */
    public List<StoreMealTakeup> getStoreMealUnSweepTableRecordId(int merchantId, long storeId, long sendPortId, long tableRecordId, boolean enableSlave) {
        return this.storeMealTakeupDao.getStoreMealSendsByTableRecordId(merchantId, storeId, sendPortId, tableRecordId, enableSlave, false);
    }

    /**
     * 划菜
     * @param param
     * @param param
     * @throws TException 
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreMealSweep> sweepStoreMeal(StoreMealSweepParam param) throws TException {
        boolean sweepOrder = param.isSweepAll();
        if (sweepOrder) {
            return this.sweepStoreMealOrderBySendPort(param);
        }
        List<StoreMealSweepParam> params = new ArrayList<StoreMealSweepParam>();
        params.add(param);
        return this.sweepStoreMealChargeBySendPort(params,false);
    }
    
    /**
     * 按收费项目划
     * @param params
     * @param refundMeal
     * @return
     * @throws TException 
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreMealSweep> sweepStoreMealChargeBySendPort(List<StoreMealSweepParam> params, boolean refundMeal) throws T5weiException{
        boolean enableSlave = false;
        long sweepMealTime = System.currentTimeMillis();
        List<StoreMealSweep> storeMealSweeps = new ArrayList<StoreMealSweep>();
        
        Set<String> orderIds = new HashSet<String>();
        for (StoreMealSweepParam storeMealSweepParam : params) {
            orderIds.add(storeMealSweepParam.getOrderId());
        }
        Map<String, StoreOrder> storeOrders = this.storeOrderDAO.getStoreOrderMapInIds(params.get(0).getMerchantId(), params.get(0).getStoreId(), new ArrayList<String>(orderIds), enableSlave);
        
        for (StoreMealSweepParam storeMealSweepParam : params) {
            List<StoreMealChargeItemSweepParam> storeMealChargeItemSweepParams = storeMealSweepParam.getStoreMealChargeItemSweepParams();
            int merchantId = storeMealSweepParam.getMerchantId();
            long storeId = storeMealSweepParam.getStoreId();
            String orderId = storeMealSweepParam.getOrderId();
            long appcopyId = storeMealSweepParam.getAppcopyId();
            long sendMealPortId = storeMealSweepParam.getSendMealPortId();
            long staffId = storeMealSweepParam.getStaffId();
            double totalProductAmount = 0;
            for (StoreMealChargeItemSweepParam chargeItemSweepParam : storeMealChargeItemSweepParams) {
                long chargeItemId = chargeItemSweepParam.getChargeItemId();
                List<Long> productIds = chargeItemSweepParam.getProductIds();
                boolean packaged = chargeItemSweepParam.isPacked();
                double sweepAmount = chargeItemSweepParam.getSweepAmount();
                List<StoreMealTakeup> storeMealTakeups = this.storeMealTakeupDao.getStoreMealTakeup4Sweep(merchantId, storeId, orderId, chargeItemId, productIds, packaged, enableSlave, true);
                if (storeMealTakeups.isEmpty() && !refundMeal) {
                    log.error(DataUtil.infoWithParams("菜已划完param=#1", new Object[] { storeMealSweepParam }));
                    throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_ALL_SWEEP_MEAL.getValue(), "store sweep all sweep meal");
                }
                for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
                    double remainMealAmount = storeMealTakeup.getRemainSend() - sweepAmount;
                    if (remainMealAmount < 0) {
                        if(refundMeal){
                            remainMealAmount = 0;
                            sweepAmount = storeMealTakeup.getRemainSend();
                        }else{
                            log.error(DataUtil.infoWithParams("划菜的数量超过了剩余数量param=#1", new Object[] { storeMealSweepParam }));
                            throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_MEAL_AMOUNT_OVER.getValue(), "store sweep meal amount over");
                        }
                    }
                    totalProductAmount = NumberUtil.add(totalProductAmount, NumberUtil.mul(sweepAmount, storeMealTakeup.getAmount()));
                    storeMealTakeup.setRemainSend(remainMealAmount);
                    storeMealTakeup.setSweepTime(sweepMealTime);
                    storeMealSweeps.add(this.buildStoreMealSweep(storeMealTakeup, sweepAmount, sweepMealTime, refundMeal, appcopyId, staffId));
                }
                this.storeMealTakeupDao.sweepStoreMeals(merchantId, storeId, storeMealTakeups);//划菜
            }
            this.storeMealSweepRecordService.saveMealSweepRecord4Sweep(merchantId, storeId, storeOrders.get(orderId), sendMealPortId, totalProductAmount, refundMeal);//更新划菜记录
        }
        this.storeMealSweepDAO.batchCreate(storeMealSweeps);// 创建划菜记录
        return storeMealSweeps;
    }
    
    /**
     * 整单划菜
     * @param param
     * @return
     * @throws T5weiException
     */
    private List<StoreMealSweep> sweepStoreMealOrderBySendPort(StoreMealSweepParam param) throws T5weiException{
        boolean enableSlave = false;
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendMealPortId();
        String orderId = param.getOrderId();
        long tableRecordId = param.getTableRecordId();
        long staffId = param.getStaffId();
        long appcopyId = param.getAppcopyId();
        
        List<StoreMealSweep> storeMealSweeps = new ArrayList<StoreMealSweep>();
        List<StoreMealTakeup> storeMealTakeups = this.getStoreMealSends(merchantId, storeId, sendPortId, tableRecordId, orderId, enableSlave, true);
        
        if (storeMealTakeups == null || storeMealTakeups.isEmpty()) {
            log.error(DataUtil.infoWithParams("菜已划完", new Object[] { param }));
            throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_ALL_SWEEP_MEAL.getValue(), "store sweep all sweep meal");
        }
        double tatalSweepAmountProduct = sweepMeal(merchantId, storeId, staffId, appcopyId, storeMealSweeps, storeMealTakeups);
        StoreOrder storeOrder = this.storeOrderQueryService.getStoreOrderById(merchantId, storeId, orderId);
        this.storeMealSweepRecordService.saveMealSweepRecord4Sweep(merchantId, storeId, storeOrder, sendPortId, tatalSweepAmountProduct, false);// 更新划菜记录
        return storeMealSweeps;
    }
    
    /**
     * 将桌台或订单上的菜品全部划菜
     * @param tableRecordId 如果为桌台：tableRecordId为桌台记录Id；如果为非桌台：tableRecordId为0；
     * @param orderId 如果为桌台：orderId为主订单Id；如果为非桌台：orderId为订单Id
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void sweepStoreMealByOrderId(int merchantId, long storeId, long tableRecordId, String orderId) {
        boolean enableSlave = false;
        boolean forUpdate = false;
        long sweepMealTime = System.currentTimeMillis();
        List<StoreMealTakeup> storeMealTakeups = this.getStoreMealSends(merchantId, storeId, 0, tableRecordId, orderId, enableSlave, forUpdate);
        if(storeMealTakeups.isEmpty()){
            return;
        }
        List<StoreMealSweep> storeMealSweeps = new ArrayList<StoreMealSweep>();
        sweepMeal(merchantId, storeId, 0, 0, storeMealSweeps, storeMealTakeups);
        this.storeMealSweepRecordService.saveMealSweepRecordByOrderId(merchantId, storeId, orderId, sweepMealTime, enableSlave);
        this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), true, null);//更新心跳
    }

    private double sweepMeal(int merchantId, long storeId, long staffId, long appcopyId, List<StoreMealSweep> storeMealSweeps,List<StoreMealTakeup> storeMealTakeups) {
        double tatalSweepAmountProduct = 0;
        long sweepMealTime = System.currentTimeMillis();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            double sweepMealAmount = storeMealTakeup.getRemainSend();
            storeMealTakeup.setRemainSend(0);
            storeMealTakeup.setSweepTime(sweepMealTime);
            tatalSweepAmountProduct = NumberUtil.add(NumberUtil.mul(storeMealTakeup.getAmount(), sweepMealAmount), tatalSweepAmountProduct);
            storeMealSweeps.add(this.buildStoreMealSweep(storeMealTakeup, sweepMealAmount, sweepMealTime, false, appcopyId, staffId));
        }
        this.storeMealTakeupDao.sweepStoreMeals(merchantId, storeId, storeMealTakeups);// 划菜
        this.storeMealSweepDAO.batchCreate(storeMealSweeps);// 创建划菜
        return tatalSweepAmountProduct;
    }
    
    @SuppressWarnings("unchecked")
    public StoreMealSweptPage getStoreMealSweptPage(StoreMealSweptParam param, boolean enableSlave) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        List<StoreMealTableSweepDTO> storeMealTableSweepDTOs = new ArrayList<StoreMealTableSweepDTO>();
        //1.查询已上齐的订单和桌台记录
        Map<String, Object> storeMealSweptRecordMap = this.storeMealSweepRecordService.getStoreMealSweptRecords(param, enableSlave);
        List<StoreMealSweepRecord> storeMealSweptRecords = (List<StoreMealSweepRecord>) storeMealSweptRecordMap.get("sweepRecords");
        int total = (int) storeMealSweptRecordMap.get("total");
        
        List<String> orderIds = new ArrayList<String>();
        List<Long> tableRecordIds = new ArrayList<Long>();
        for (StoreMealSweepRecord storeMealSweepRecord : storeMealSweptRecords) {
            if (storeMealSweepRecord.getTableRecordId() > 0) {
                tableRecordIds.add(storeMealSweepRecord.getTableRecordId());
            }
            orderIds.add(storeMealSweepRecord.getOrderId());
        }
        Map<String, StoreOrder> storeOrders = this.storeOrderDAO.getStoreOrderMapInIds(merchantId, storeId, orderIds, enableSlave);
        this.storeOrderHelper.setStoreOrderTimes(new ArrayList<StoreOrder>(storeOrders.values()), enableSlave);
        Map<Long, StoreTableRecord> storeTableRecords = this.storeTableRecordService.getStoreTableRecordMapByIds(merchantId, storeId, tableRecordIds, enableSlave);
        //2.构造返回数据
        for (StoreMealSweepRecord storeMealSweepRecord : storeMealSweptRecords) {
            StoreOrder storeOrder = storeOrders.get(storeMealSweepRecord.getOrderId());
            StoreTableRecord storeTableRecord = storeTableRecords.get(storeMealSweepRecord.getTableRecordId());
            StoreMealTableSweepDTO storeMealTableSweepDTO = this.storeMealSweepHelper.buildStoreMealTableSweepDTO(storeOrder, storeTableRecord, enableSlave);
            storeMealTableSweepDTO.setSweepMealTime(storeMealSweepRecord.getLastSweepMealTime());
            storeMealTableSweepDTOs.add(storeMealTableSweepDTO);
        }
        this.storeMealSweepHelper.sortStoreMealTableSweepDTOsBySweepMealTime(storeMealTableSweepDTOs);
        
        StoreMealSweptPage page = BeanUtil.copy(param, StoreMealSweptPage.class);
        page.setStoreMealSweepDTOs(storeMealTableSweepDTOs);
        page.setTotal(total);
        return page;
    }
    
    public List<StoreProductSweepAmount> countStoreProductSweepAmount(int merchantId, long storeId, long sendPortId, boolean enableSlave){
        return this.storeMealTakeupDao.countStoreProductSweepAmount(merchantId, storeId, sendPortId, enableSlave);
    }

    public List<StoreMealSweep> getStoreMealSweeps(StoreMealSweepHistoryQueryParam param, boolean enableSlave) {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        long repastDate = param.getRepastDate();
        String keyWord = param.getKeyWord();
        List<Long> tableRecordIds = new ArrayList<Long>();
        
        List<StoreTableRecord> storeTableRecords = this.storeTableRecordService.getStoreTableRecordByName(merchantId, storeId, keyWord, enableSlave);
        for (StoreTableRecord storeTableRecord : storeTableRecords) {
            tableRecordIds.add(storeTableRecord.getTableRecordId());
        }
        
        List<StoreMealSweep> storeMealSweeps = this.storeMealSweepDAO.getStoreMealSweepbyCond(param, tableRecordIds, enableSlave);
        if(storeMealSweeps.isEmpty()){
            return new ArrayList<StoreMealSweep>();
        }
        
        Set<Integer> takeSerialNumbers = new HashSet<Integer>();
        for (StoreMealSweep storeMealSweep : storeMealSweeps) {
            takeSerialNumbers.add(storeMealSweep.getTakeSerialNumber());
        }
        return this.storeMealSweepDAO.getStoreMealSweepbyTakeSerialNumber(merchantId, storeId, sendPortId, repastDate, new ArrayList<Integer>(takeSerialNumbers), enableSlave);
    }
    
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void cancelStoreMealSweep(StoreMealCancelSweepParam param) throws T5weiException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String orderId = param.getOrderId();
        long sendPortId = param.getSendPortId();
        boolean enableSlave = false;
        double totalSweepAmountProduct = 0;
        //1.获取需要取消的划菜记录
        List<StoreMealSweep> storeMealSweeps = this.storeMealSweepDAO.getStoreMealSweepByIds(merchantId, storeId, param.getSweepMealRecordIds(), enableSlave, true);
        if(storeMealSweeps.isEmpty()){
            log.error(DataUtil.infoWithParams("取消划菜的菜品已经被取消", new Object[] { param }));
            throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_CANCEL_MEAL.getValue(), "store sweep no cancel meal");
        }
        //2.获取已出餐列表中数据
        Set<Long> takeupIds = new HashSet<Long>();
        for (StoreMealSweep storeMealSweep : storeMealSweeps) {
            takeupIds.add(storeMealSweep.getTakeupId());
        }
        Map<Long, StoreMealTakeup> storeMealTakeupMap = this.storeMealTakeupDao.getStoreMealTakeupMapByTids(merchantId, storeId, new ArrayList<Long>(takeupIds), true);
        //3.将划菜记录的数据加回到已出餐列表中
        for (StoreMealSweep storeMealSweep : storeMealSweeps) {
            StoreMealTakeup storeMealTakeup = storeMealTakeupMap.get(storeMealSweep.getTakeupId());
            if(storeMealTakeup == null || storeMealTakeup == null){
                log.error(DataUtil.infoWithParams("取消划菜的菜品不存在", new Object[] { param }));
                throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_NO_CANCEL_MEAL.getValue(), "store sweep no cancel meal");
            }
            double cancelSweepMealAmount = storeMealSweep.getSweepMealAmount();
            double remainSend = storeMealTakeup.getRemainSend();
            remainSend = NumberUtil.add(remainSend, cancelSweepMealAmount);
            totalSweepAmountProduct = NumberUtil.add(totalSweepAmountProduct, NumberUtil.mul(cancelSweepMealAmount, storeMealTakeup.getAmount()));
            if(remainSend > storeMealTakeup.getAmountOrder()){
                log.error(DataUtil.infoWithParams("取消划菜的数量不能比购买的数量多", new Object[] { param }));
                throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_CANCEL_MEAL_AMOUNT_OVER.getValue(), "store sweep cancel meal amount over");
            }
            storeMealTakeup.setRemainSend(remainSend);
        }
        this.storeMealTakeupDao.sweepStoreMeals(merchantId, storeId, new ArrayList<StoreMealTakeup>(storeMealTakeupMap.values()));
        StoreOrder storeOrder = this.storeOrderQueryService.getStoreOrderById(merchantId, storeId, orderId);
        this.storeMealSweepRecordService.saveMealSweepRecord4Sweep(merchantId, storeId, storeOrder, sendPortId, NumberUtil.sub(0, totalSweepAmountProduct), false);
        //4.删除划菜记录
        this.storeMealSweepDAO.batchDelete(merchantId,storeId,storeMealSweeps);
    }
    
    public List<StoreMealTakeup> getStoreMealSends(int merchantId, long storeId, long sendPortId, long tableRecordId, String orderId, boolean enableSlave, boolean forUpdate){
        if(tableRecordId > 0){
            return this.storeMealTakeupDao.getStoreMealSendsByTableRecordId(merchantId, storeId, sendPortId, tableRecordId, enableSlave, forUpdate);
        }else{
            return this.storeMealTakeupDao.getStoreMealSendsByOrderId(merchantId, storeId, sendPortId, orderId, enableSlave, forUpdate);
        }
    }
    
    public List<StoreMealTakeup> getStoreMealSweeps(StoreMealSweepPrintParam param, boolean enableSlave){
        long tableRecordId = param.getTableRecordId();
        if(tableRecordId > 0){
            return this.storeMealTakeupDao.getStoreMealSweepsByTableRecordId(param, enableSlave);
        }else{
            return this.storeMealTakeupDao.getStoreMealSweepsByOrderId(param, enableSlave);
        }
    }
    
    public List<StoreMealSweep> getStoreMealSweeps(int merchantId, long storeId, long sendPortId, long tableRecordId, String orderId, boolean enableSlave, boolean forUpdate) {
        if(tableRecordId > 0){
            return this.storeMealSweepDAO.getStoreMealSweepsByTableRecordId(merchantId, storeId, sendPortId, tableRecordId, enableSlave, forUpdate);
        }else{
            return this.storeMealSweepDAO.getStoreMealSweepsByOrderId(merchantId, storeId, sendPortId, orderId, enableSlave, forUpdate);
        }
    }
    
    public void refundMeal4Sweep(int merchantId, long storeId, StoreOrderRefundItem refundItem) throws T5weiException{
        List<StoreMealTakeup> storeMealTakeups = this.storeMealTakeupDao.getStoreMealTakeups4Refund(merchantId, storeId, refundItem, false, true);
	    if (storeMealTakeups.isEmpty()) {
		    return;
	    }
        Map<String,StoreMealSweepParam> params = new HashMap<String,StoreMealSweepParam>();
        double refundAmount = refundItem.getAmount();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            String key = storeMealTakeup.getOrderId() + "_" + storeMealTakeup.getSendPortId() + "_" + storeMealTakeup.getChargeItemId() + "_" + storeMealTakeup.isPackaged();
            if(params.containsKey(key)){
                StoreMealSweepParam param = params.get(key);
                StoreMealChargeItemSweepParam chargeItemSweepParam = param.getStoreMealChargeItemSweepParams().get(0);
                chargeItemSweepParam.getProductIds().add(storeMealTakeup.getProductId());
                continue;
            }
            StoreMealSweepParam param = new StoreMealSweepParam();
            param.setMerchantId(storeMealTakeup.getMerchantId());
            param.setStoreId(storeMealTakeup.getStoreId());
            param.setSendMealPortId(storeMealTakeup.getSendPortId());
            param.setOrderId(storeMealTakeup.getOrderId());
            param.setTableRecordId(storeMealTakeup.getTableRecordId());
            param.setSweepAll(false);
            
            List<StoreMealChargeItemSweepParam> chargeItemSweepParams = new ArrayList<StoreMealChargeItemSweepParam>();
            StoreMealChargeItemSweepParam chargeItemSweepParam = new StoreMealChargeItemSweepParam();
            chargeItemSweepParam.setChargeItemId(storeMealTakeup.getChargeItemId());
            
            List<Long> productIds = new ArrayList<Long>();
            productIds.add(storeMealTakeup.getProductId());
            chargeItemSweepParam.setProductIds(productIds);
            
            chargeItemSweepParam.setPacked(storeMealTakeup.isPackaged());
            chargeItemSweepParam.setSweepAmount(refundAmount);
            chargeItemSweepParams.add(chargeItemSweepParam);
            param.setStoreMealChargeItemSweepParams(chargeItemSweepParams);
            params.put(key, param);
        }
        this.sweepStoreMealChargeBySendPort(new ArrayList<StoreMealSweepParam>(params.values()), true);
    }
    
    public StoreMealSweep buildStoreMealSweep(StoreMealTakeup storeMealTakeup, double sweepAmount, long sweepMealTime, boolean refundMeal, long appcopyId, long staffId){
        if(storeMealTakeup == null){
            return null;
        }
        StoreMealSweep storeMealSweep = new StoreMealSweep();
        storeMealSweep.setOrderId(storeMealTakeup.getOrderId());
        storeMealSweep.setMerchantId(storeMealTakeup.getMerchantId());
        storeMealSweep.setStoreId(storeMealTakeup.getStoreId());
        storeMealSweep.setStaffId(staffId);
        storeMealSweep.setRepastDate(storeMealTakeup.getRepastDate());
        storeMealSweep.setTimeBucketId(storeMealTakeup.getTimeBucketId());
        storeMealSweep.setPortId(storeMealTakeup.getPortId());
        storeMealSweep.setSendPortId(storeMealTakeup.getSendPortId());
        storeMealSweep.setAppcopyId(appcopyId);
        storeMealSweep.setAmount(storeMealTakeup.getAmount());
        storeMealSweep.setTakeSerialNumber(storeMealTakeup.getTakeSerialNumber());
        storeMealSweep.setPackaged(storeMealTakeup.isPackaged());
        storeMealSweep.setTakeMode(storeMealTakeup.getTakeMode());
        storeMealSweep.setChargeItemId(storeMealTakeup.getChargeItemId());
        storeMealSweep.setChargeItemName(storeMealTakeup.getChargeItemName());
        storeMealSweep.setProductId(storeMealTakeup.getProductId());
        storeMealSweep.setProductName(storeMealTakeup.getProductName());
        storeMealSweep.setUnit(storeMealTakeup.getUnit());
        storeMealSweep.setRemark(storeMealTakeup.getRemark());
        storeMealSweep.setAmountOrder(storeMealTakeup.getAmountOrder());
        storeMealSweep.setShowProducts(storeMealTakeup.isShowProducts());
        storeMealSweep.setTableRecordId(storeMealTakeup.getTableRecordId());
        storeMealSweep.setSweepMealAmount(sweepAmount);
        storeMealSweep.setSweepMealTime(sweepMealTime);
        storeMealSweep.setRefundMeal(refundMeal);
        storeMealSweep.setUpdateTime(System.currentTimeMillis());
        storeMealSweep.setCreateTime(System.currentTimeMillis());
        storeMealSweep.setTakeupId(storeMealTakeup.getTid());
        return storeMealSweep;
    }

    public void batchCreate(List<StoreMealSweep> storeMealSweeps) {
        this.storeMealSweepDAO.batchCreate(storeMealSweeps);
    }
    
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateTableRecordId(int merchantId, long storeId, long originalTableRecordId, long targetTableRecordId) {
        this.storeMealSweepDAO.updateTableRecordId(merchantId, storeId, originalTableRecordId, targetTableRecordId);
    }

    public List<StoreMealDTO> printStoreMealSweep(StoreMealSweepPrintParam param, boolean enableSlave) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        List<StoreMealTakeup> storeMealSends = this.getStoreMealSweeps(param, enableSlave);
        Set<String> orderIds = new HashSet<String>();
        for (StoreMealTakeup storeMealTakeup : storeMealSends) {
            orderIds.add(storeMealTakeup.getOrderId());
        }
        Map<String, StoreOrder> storeOrderMap = this.storeOrderDAO.getStoreOrderMapInIds(merchantId, storeId, new ArrayList<String>(orderIds), enableSlave);
        List<StoreMealDTO> storeMealDTOs = this.storeMealSweepHelper.buildStoreMealDTO4Send(merchantId, storeId, storeMealSends, true, enableSlave);
        this.storeMealSweepHelper.setStoreOrderDetail(storeOrderMap, enableSlave);
        this.storeMealSweepHelper.buildStoreMealDetail(storeMealDTOs, storeOrderMap);
        return storeMealDTOs;
    }
}
