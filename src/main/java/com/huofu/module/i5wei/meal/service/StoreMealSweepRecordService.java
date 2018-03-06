package com.huofu.module.i5wei.meal.service;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealportsend.StoreMealSweptParam;
import huofuhelper.util.DataUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.huofu.module.i5wei.meal.dao.StoreMealSweepAmount;
import com.huofu.module.i5wei.meal.dao.StoreMealSweepRecordDAO;
import com.huofu.module.i5wei.meal.entity.StoreMealSweepRecord;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;

@Service
public class StoreMealSweepRecordService {
    
    private Log log = LogFactory.getLog(StoreMealSweepRecordService.class);
    
    @Autowired
    private StoreMealSweepRecordDAO storeMealSweepRecordDAO;
    
    @Autowired
    private StoreTableRecordService storeTableRecordService;
    
    @Autowired
    private StoreOrderDAO storeOrderDAO;
    
    /**
     * 批量更新划菜记录
     * @param merchantId
     * @param storeId
     * @param storeMealTakeups
     * @return
     * @throws T5weiException 
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreMealSweepRecord> batchCreateMealSweepRecords(int merchantId, long storeId, StoreOrder storeOrder, List<StoreMealTakeup> storeMealTakeups) throws T5weiException{
        if(storeMealTakeups.isEmpty()){
            return new ArrayList<StoreMealSweepRecord>();
        }
        String orderId = storeOrder.getOrderId();
        StoreOrder masterStoreOrder = null;
        if(storeOrder.getTableRecordId() > 0){
            orderId = storeOrder.getParentOrderId();//主订单
            masterStoreOrder = this.storeOrderDAO.getStoreOrderById(merchantId, storeId, orderId, false);
        }
        Set<Long> sendPortIds = new HashSet<Long>();
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            sendPortIds.add(storeMealTakeup.getSendPortId());
        }
        
        Map<String, StoreMealSweepRecord> storeMealSweepRecordMap = this.storeMealSweepRecordDAO.getStoreMealSweepRecords(merchantId, storeId, orderId, new ArrayList<Long>(sendPortIds), false, true);
        for (StoreMealTakeup storeMealTakeup : storeMealTakeups) {
            if (storeMealTakeup.getSendPortId() == 0) {//没有传菜口
                continue;
            }
            String key = orderId + "_" + storeMealTakeup.getSendPortId();
            if(storeMealSweepRecordMap.containsKey(key)){
                StoreMealSweepRecord storeMealSweepRecord = storeMealSweepRecordMap.get(key);
                double totalAmountProduct = NumberUtil.mul(storeMealTakeup.getAmountOrder(), storeMealTakeup.getAmount());
                storeMealSweepRecord.setAmountProduct(NumberUtil.add(totalAmountProduct, storeMealSweepRecord.getAmountProduct()));
                
                double sweepAmountProduct = NumberUtil.mul(NumberUtil.sub(storeMealTakeup.getAmountOrder(), storeMealTakeup.getRemainSend()), storeMealTakeup.getAmount());
                storeMealSweepRecord.setSweepMealAmount(NumberUtil.add(storeMealSweepRecord.getSweepMealAmount(), sweepAmountProduct));
                storeMealSweepRecord.setLastSweepMealTime(storeMealTakeup.getSweepTime());
                storeMealSweepRecord.setAllSweepStatus(storeMealSweepRecord.getSweepMealAmount() == storeMealSweepRecord.getAmountProduct() ? true : false);
                storeMealSweepRecord.setUpdateTime(System.currentTimeMillis());
            }else{
                StoreMealSweepRecord storeMealSweepRecord = buildStoreMealSweepRecord(masterStoreOrder, storeMealTakeup, orderId);
                storeMealSweepRecordMap.put(key, storeMealSweepRecord);
            }
        }
        List<StoreMealSweepRecord> storeMealSweepRecords = new ArrayList<StoreMealSweepRecord>(storeMealSweepRecordMap.values());
        batchSave(merchantId, storeId, storeMealSweepRecords);
        return storeMealSweepRecords;
    }
    
    /**
     * 划菜、取消划菜更新划菜记录
     * @param merchantId
     * @param storeId
     * @param storeOrder
     * @param sendPortId
     * @param totalProductAmount
     * @return
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreMealSweepRecord saveMealSweepRecord4Sweep(int merchantId, long storeId, StoreOrder storeOrder, long sendPortId, double totalProductAmount, boolean refundMeal){
        if(totalProductAmount == 0){
            return new StoreMealSweepRecord();
        }
        String orderId = storeOrder.getOrderId();
        if(storeOrder.getTableRecordId() > 0){//主订单
            if(DataUtil.isEmpty(storeOrder.getParentOrderId())){
                orderId = storeOrder.getOrderId();
            }else{
                orderId = storeOrder.getParentOrderId();
            }
        }
        StoreMealSweepRecord storeMealSweepRecord = this.storeMealSweepRecordDAO.getStoreMealSweepRecordbyIds(merchantId, storeId, orderId, sendPortId, false);
        if(refundMeal){
            if(storeMealSweepRecord.getAmountProduct() < totalProductAmount){
                totalProductAmount = storeMealSweepRecord.getAmountProduct();
            }
            storeMealSweepRecord.setAmountProduct(NumberUtil.sub(storeMealSweepRecord.getAmountProduct(), totalProductAmount));
        }else{
            storeMealSweepRecord.setSweepMealAmount(NumberUtil.add(storeMealSweepRecord.getSweepMealAmount(), totalProductAmount));
        }
        storeMealSweepRecord.setAllSweepStatus(storeMealSweepRecord.getSweepMealAmount() == storeMealSweepRecord.getAmountProduct() ? true : false);
        if(refundMeal || totalProductAmount > 0){//退菜和取消划菜不需要更新划菜记录时间
            storeMealSweepRecord.setLastSweepMealTime(System.currentTimeMillis());
        }
        storeMealSweepRecord.update();
        return storeMealSweepRecord;
    }

    /**
     * 保存划菜记录
     * @param merchantId
     * @param storeId
     * @param storeMealSweepRecords
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void batchSave(int merchantId, long storeId, List<StoreMealSweepRecord> storeMealSweepRecords){
        if(storeMealSweepRecords == null || storeMealSweepRecords.isEmpty()){
            return;
        }
        List<StoreMealSweepRecord> addSweepRecords = new ArrayList<StoreMealSweepRecord>();
        List<StoreMealSweepRecord> updateSweepRecords = new ArrayList<StoreMealSweepRecord>();
        for (StoreMealSweepRecord mealSweepRecord : storeMealSweepRecords) {
            if(mealSweepRecord.getTid() > 0){
                updateSweepRecords.add(mealSweepRecord);
            }else{
                addSweepRecords.add(mealSweepRecord);
            }
        }
        this.storeMealSweepRecordDAO.batchUpdate(merchantId, storeId, updateSweepRecords);
        this.storeMealSweepRecordDAO.batchCreate(addSweepRecords);
    }
    
    /**
     * 按订单保存划菜记录
     * @param merchantId
     * @param storeId
     * @param tableRecordId
     * @param orderId
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreMealSweepRecord> saveMealSweepRecordByOrderId(int merchantId, long storeId, String orderId, long sweepMealTime, boolean enableSlave) {
        List<StoreMealSweepRecord> storeMealSweepRecords = this.storeMealSweepRecordDAO.getStoreMealSweepRecords(merchantId, storeId, orderId, enableSlave, true);
        if(storeMealSweepRecords.isEmpty()){
            return storeMealSweepRecords;
        }
        for (StoreMealSweepRecord storeMealSweepRecord : storeMealSweepRecords) {
            storeMealSweepRecord.setSweepMealAmount(storeMealSweepRecord.getAmountProduct());
            storeMealSweepRecord.setLastSweepMealTime(sweepMealTime);
            storeMealSweepRecord.setAllSweepStatus(true);
            storeMealSweepRecord.setUpdateTime(System.currentTimeMillis());
        }
        this.storeMealSweepRecordDAO.batchUpdate(merchantId, storeId, storeMealSweepRecords);
        return storeMealSweepRecords;
    }
    
    public Map<String,Object> getStoreMealSweptRecords(StoreMealSweptParam param, boolean enableSlave){
        List<Long> tableRecordIds = new ArrayList<Long>();
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String keyWord = param.getKeyWord();
        List<StoreTableRecord> storeTableRecordIds = storeTableRecordService.getStoreTableRecordByName(merchantId, storeId, keyWord, enableSlave);
        for (StoreTableRecord storeTableRecord : storeTableRecordIds) {
            tableRecordIds.add(storeTableRecord.getTableRecordId());
        }
        return this.storeMealSweepRecordDAO.getStoreMealSweepByCond(param, tableRecordIds, enableSlave);
    }
    
    public List<StoreMealSweepAmount> countStoreMealSweepAmount(int merchantId, long storeId, long sendPortId, boolean enableSlave) {
        return this.storeMealSweepRecordDAO.countStoreMealSweepAmount(merchantId, storeId, sendPortId, enableSlave);
    }
    
    private StoreMealSweepRecord buildStoreMealSweepRecord(StoreOrder masterStoreOrder, StoreMealTakeup storeMealTakeup, String orderId) {
        StoreMealSweepRecord storeMealSweepRecord = new StoreMealSweepRecord();
        storeMealSweepRecord.setOrderId(orderId);
        storeMealSweepRecord.setTakeSerialNumber(storeMealTakeup.getTakeSerialNumber());
        if(masterStoreOrder != null){
            storeMealSweepRecord.setTakeSerialNumber(masterStoreOrder.getTakeSerialNumber());
        }
        storeMealSweepRecord.setMerchantId(storeMealTakeup.getMerchantId());
        storeMealSweepRecord.setStoreId(storeMealTakeup.getStoreId());
        storeMealSweepRecord.setRepastDate(storeMealTakeup.getRepastDate());
        storeMealSweepRecord.setTimeBucketId(storeMealTakeup.getTimeBucketId());
        storeMealSweepRecord.setPortId(storeMealTakeup.getPortId());
        storeMealSweepRecord.setSendPortId(storeMealTakeup.getSendPortId());
        
        double totalAmountProduct = NumberUtil.mul(storeMealTakeup.getAmountOrder(), storeMealTakeup.getAmount());
        storeMealSweepRecord.setAmountProduct(totalAmountProduct);
        storeMealSweepRecord.setTableRecordId(storeMealTakeup.getTableRecordId());
        
        double sweepAmountOrder = NumberUtil.sub(storeMealTakeup.getAmountOrder(), storeMealTakeup.getRemainSend());
        storeMealSweepRecord.setSweepMealAmount(NumberUtil.mul(sweepAmountOrder, storeMealTakeup.getAmount()));
        storeMealSweepRecord.setLastSweepMealTime(storeMealTakeup.getSweepTime());
        storeMealSweepRecord.setAllSweepStatus(storeMealSweepRecord.getSweepMealAmount() == storeMealSweepRecord.getAmountProduct() ? true : false);
        storeMealSweepRecord.setUpdateTime(System.currentTimeMillis());
        storeMealSweepRecord.setCreateTime(System.currentTimeMillis());
        return storeMealSweepRecord;
    }

    /**
     * 转台：将原桌台找到新桌台，记录需要跟着变化
     * @param merchantId
     * @param storeId
     * @param originalTableRecordId
     * @param targetTableRecordId
     * @param originalOrderId
     * @param targetOrderId
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateTableRecordId(int merchantId, long storeId, long originalTableRecordId, long targetTableRecordId, String originalOrderId, String targetOrderId) {
        boolean enableSlave = false;
        List<StoreMealSweepRecord> storeMealSweepRecords = new ArrayList<StoreMealSweepRecord>();
        Map<String,StoreMealSweepRecord> originalRecords = this.storeMealSweepRecordDAO.getStoreMealSweepRecords(merchantId, storeId, originalOrderId, null, enableSlave, true);
        Map<String,StoreMealSweepRecord> targetRecords = this.storeMealSweepRecordDAO.getStoreMealSweepRecords(merchantId, storeId, targetOrderId, null, enableSlave, true);
        
        Set<String> originalRecordKeys = originalRecords.keySet();
        Set<String> targetRecordKeys = targetRecords.keySet();
        for (String originalRecordKey : originalRecordKeys) {
            StoreMealSweepRecord originalSweepRecord = originalRecords.get(originalRecordKey);
            originalRecordKey = targetOrderId + "_" + originalSweepRecord.getSendPortId();
            if(targetRecordKeys.contains(originalRecordKey)){
                StoreMealSweepRecord targetSweepRecord = targetRecords.get(originalRecordKey);
                targetSweepRecord.setAmountProduct(NumberUtil.add(targetSweepRecord.getAmountProduct(), originalSweepRecord.getAmountProduct()));
                targetSweepRecord.setSweepMealAmount(NumberUtil.add(targetSweepRecord.getSweepMealAmount(), originalSweepRecord.getSweepMealAmount()));
                targetSweepRecord.setAllSweepStatus(targetSweepRecord.getAmountProduct() == targetSweepRecord.getSweepMealAmount() ? true : false);
                targetSweepRecord.setUpdateTime(System.currentTimeMillis());
                storeMealSweepRecords.add(targetSweepRecord);
            }else{
                StoreMealSweepRecord targetSweepRecord = BeanUtil.copy(originalSweepRecord, StoreMealSweepRecord.class);
                targetSweepRecord.setTid(0);
                targetSweepRecord.setTableRecordId(targetTableRecordId);
                targetSweepRecord.setOrderId(targetOrderId);
                targetSweepRecord.setUpdateTime(System.currentTimeMillis());
                storeMealSweepRecords.add(targetSweepRecord);
            }
        }
        this.storeMealSweepRecordDAO.deleteByOrderId(merchantId, storeId, originalOrderId);
        this.batchSave(merchantId, storeId, storeMealSweepRecords);
    }
}
