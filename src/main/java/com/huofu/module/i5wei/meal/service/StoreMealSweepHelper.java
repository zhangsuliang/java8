package com.huofu.module.i5wei.meal.service;

import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.meal.StoreMealProductDTO;
import huofucore.facade.i5wei.mealportsend.StoreMealTableSweepDTO;
import huofucore.facade.i5wei.menu.ProductDivRuleEnum;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.i5wei.order.StoreOrderActualPayDTO;
import huofucore.facade.i5wei.order.StoreOrderDTO;
import huofucore.facade.i5wei.order.StoreOrderDeliveryDTO;
import huofucore.facade.i5wei.order.StoreOrderTableRecordDTO;
import huofuhelper.util.bean.BeanUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.huofu.module.i5wei.meal.dao.StoreMealSweepAmount;
import com.huofu.module.i5wei.meal.entity.StoreMealSweep;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;

@Component
public class StoreMealSweepHelper {
    
    @Autowired
    private StoreTableRecordDAO storeTableRecordDAO;
    
    @Autowired
    private StoreOrderHelper storeOrderHelper;
    
    @Autowired
    private StoreMealMultiHelper storeMealMultiHelper;
    
    @Autowired
    private StoreTableRecordService storeTableRecordService;
    
    @Autowired
    private StoreProductDAO storeProductDAO;
    
    @Autowired
    private StoreOrderDAO storeOrderDAO;
    
    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;
    
    private StoreMealDTO newStoreMealDTO(StoreMealSweep storeMealSweep) {
        String orderId = storeMealSweep.getOrderId();
        long portId = storeMealSweep.getPortId();
        int merchantId = storeMealSweep.getMerchantId(); 
        long storeId = storeMealSweep.getStoreId();
        long repastDate = storeMealSweep.getRepastDate();
        long timeBucketId = storeMealSweep.getTimeBucketId();
        int takeSerialNumber = storeMealSweep.getTakeSerialNumber();
        int takeMode = storeMealSweep.getTakeMode();
        boolean packaged = storeMealSweep.isPackaged();
        long appcopyId = storeMealSweep.getAppcopyId();
        long createTime = storeMealSweep.getCreateTime();
        boolean refundMeal = storeMealSweep.isRefundMeal();
        long updateTime = storeMealSweep.getUpdateTime();
        long sendPortId = storeMealSweep.getSendPortId();
        long tableRecordId = storeMealSweep.getTableRecordId();
        long sweepMealTime = storeMealSweep.getSweepMealTime();
        StoreMealDTO storeMealDTO = new StoreMealDTO();
        storeMealDTO.setOrderId(orderId);
        storeMealDTO.setPortId(portId);
        storeMealDTO.setMerchantId(merchantId);
        storeMealDTO.setStoreId(storeId);
        storeMealDTO.setRepastDate(repastDate);
        storeMealDTO.setTimeBucketId(timeBucketId);
        storeMealDTO.setTakeSerialNumber(takeSerialNumber);
        storeMealDTO.setTakeMode(takeMode);
        storeMealDTO.setPackaged(packaged);
        storeMealDTO.setAppcopyId(appcopyId);
        storeMealDTO.setCreateTime(createTime);
        storeMealDTO.setRefundMeal(refundMeal);
        storeMealDTO.setUpdateTime(updateTime);
        storeMealDTO.setSendPortId(sendPortId);
        storeMealDTO.setTableRecordId(tableRecordId);
        storeMealDTO.setSweepMealTime(sweepMealTime);
        return storeMealDTO;
    }

    private StoreMealChargeDTO newStoreMealChargeDTO(StoreMealSweep storeMealSweep) {
        StoreMealChargeDTO storeMealChargeDTO =BeanUtil.copy(storeMealSweep, StoreMealChargeDTO.class);
        storeMealChargeDTO.setAmount(storeMealSweep.getSweepMealAmount());//收费项的数量使用已划菜代替
        return storeMealChargeDTO;
    }

    private StoreMealProductDTO newStoreMealProductDTO(StoreMealSweep storeMealSweep) {
        String orderId = storeMealSweep.getOrderId();
        int merchantId = storeMealSweep.getMerchantId();
        long storeId = storeMealSweep.getStoreId();
        long chargeItemId = storeMealSweep.getChargeItemId();
        long productId = storeMealSweep.getProductId();
        String productName = storeMealSweep.getProductName();
        String unit = storeMealSweep.getUnit();
        double amount = storeMealSweep.getAmount();
        long storeMealSweepId = storeMealSweep.getTid();
        StoreMealProductDTO storeMealProductDTO = new StoreMealProductDTO();
        storeMealProductDTO.setOrderId(orderId);
        storeMealProductDTO.setMerchantId(merchantId);
        storeMealProductDTO.setStoreId(storeId);
        storeMealProductDTO.setChargeItemId(chargeItemId);
        storeMealProductDTO.setProductId(productId);
        storeMealProductDTO.setProductName(productName);
        storeMealProductDTO.setUnit(unit);
        storeMealProductDTO.setAmount(amount);
        storeMealProductDTO.setSweepMealRecordId(storeMealSweepId);
        return storeMealProductDTO;
    }

    private Map<String,Object> getStoreMealSendDetailOrders(int merchantId, long storeId, List<StoreMealTakeup> storeMealSends, boolean enableSlave) throws TException{
        Map<String,Object> result = new HashMap<String,Object>();
        Set<Long> productIds = new HashSet<Long>();
        Set<Long> chargeItemIds = new HashSet<Long>();
        for (StoreMealTakeup storeMealSend : storeMealSends) {
            productIds.add(storeMealSend.getProductId());
            chargeItemIds.add(storeMealSend.getChargeItemId());
        }
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemDAO.getListInIds(merchantId, storeId, new ArrayList<Long>(chargeItemIds), enableSlave, true);
        List<StoreProduct> storeProducts = this.storeProductDAO.getListInIds(merchantId, storeId, new ArrayList<Long>(productIds), enableSlave, true);
        
        result.put("chargeItems", storeChargeItems);
        result.put("products", storeProducts);
        return result;
    }

    /**
     * 划菜记录分单
     * @param storeMealSweeps
     * @throws TException 
     */
    public List<StoreMealDTO> getStoreMealSweepHistory(int merchantId, long storeId, List<StoreMealSweep> storeMealSweeps, boolean enableSlave) throws TException {
        if(storeMealSweeps.isEmpty()){
            return new ArrayList<StoreMealDTO>();
        }
        List<StoreMealDTO> storeMealDTOs = this.buildStoreMealDTO4Sweep(merchantId, storeId, storeMealSweeps, enableSlave);
        
        Set<String> orderIds = new HashSet<String>();
        Set<Long> tableRecordIds = new HashSet<Long>();
        for(StoreMealSweep storeMealSweep : storeMealSweeps){
            orderIds.add(storeMealSweep.getOrderId());
            tableRecordIds.add(storeMealSweep.getTableRecordId());
        }
        Map<String, StoreOrder> storeOrderMap = this.storeOrderDAO.getStoreOrderMapInIds(merchantId, storeId, new ArrayList<String>(orderIds), enableSlave);
        this.setStoreOrderDetail(storeOrderMap, enableSlave);
        this.buildStoreMealDetail(storeMealDTOs, storeOrderMap);
        return storeMealDTOs;
    }
    
    /**
     * 获取划菜分单
     * @param merchantId
     * @param storeId
     * @param storeMealSends
     * @param enableSlave 
     * @throws TException
     */
    @SuppressWarnings("unchecked")
    public List<StoreMealDTO> buildStoreMealDTO4Send(int merchantId, long storeId, List<StoreMealTakeup> storeMealSends, boolean isPrint, boolean enableSlave) throws TException{
        Map<String,Object> detailOrders = this.getStoreMealSendDetailOrders(merchantId, storeId, storeMealSends, enableSlave);
        List<StoreProduct> storeProducts = (List<StoreProduct>) detailOrders.get("products");
        List<StoreChargeItem> storeChargeItems = (List<StoreChargeItem>) detailOrders.get("chargeItems");
        this.storeMealMultiHelper.calculateDivRule(storeMealSends, storeChargeItems, storeProducts, false);
        
        Map<String, StoreMealDTO> storeMealMap = new HashMap<String,StoreMealDTO>();
        Map<String,StoreMealChargeDTO> storeChargeMap = new HashMap<String,StoreMealChargeDTO>();
        //待划菜数据
        for (StoreMealTakeup storeMealSend : storeMealSends) {
            String orderId = storeMealSend.getOrderId();
            long portId = storeMealSend.getPortId();//加工档口
            long sendPortId = storeMealSend.getSendPortId();//传菜口
            int takeSerialNumber = storeMealSend.getTakeSerialNumber();
            long chargeItemId = storeMealSend.getChargeItemId();
            long productId = storeMealSend.getProductId();
            boolean packaged = storeMealSend.isPackaged();
            long sweepMealTime = storeMealSend.getSweepTime();
            boolean numDiv = storeMealSend.isNumDiv();
            //分单规则
            String mealKey = orderId + "_" + sendPortId + "_" + takeSerialNumber;
            String chargeKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + chargeItemId + "_" + packaged;
            String productKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + chargeItemId + "_" + productId + "_" + packaged;
            //分单主键
            String ckey = chargeKey;
            // 分单显示项目
            if (storeMealSend.isProductDivRule()) {
                if (storeMealSend.getDivRule() > ProductDivRuleEnum.NOT.getValue()) {
                    ckey = productKey;//按产品分单
                }
            } else {
                if (storeMealSend.getDivRule() > ProductDivRuleEnum.NOT.getValue()) {
                    ckey = chargeKey;//按收费项目分单
                }
            }

            if (storeMealMap.containsKey(mealKey)) {
                StoreMealDTO storeMealDTO = storeMealMap.get(mealKey);
                if(storeChargeMap.containsKey(ckey)){
                    StoreMealChargeDTO storeMealChargeDTO = storeChargeMap.get(ckey);
                    StoreMealProductDTO storeMealProductDTO = this.storeMealMultiHelper.newStoreMealProductDTO(storeMealSend);
                    List<StoreMealProductDTO> storeMealProductDTOs = storeMealChargeDTO.getStoreMealProductDTOs();
                    storeMealProductDTOs.add(storeMealProductDTO);
                }else{
                    StoreMealProductDTO storeMealProductDTO = this.storeMealMultiHelper.newStoreMealProductDTO(storeMealSend);
                    StoreMealChargeDTO storeMealChargeDTO = this.storeMealMultiHelper.newStoreMealChargeDTO(storeMealSend);
                    List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
                    storeMealProductDTOs.add(storeMealProductDTO);
                    storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
                    storeMealChargeDTO.setAmount(storeMealSend.getRemainSend());
                    storeMealChargeDTO.setNumDiv(numDiv);
                    if(isPrint){
                        storeMealChargeDTO.setAmount(storeMealSend.getAmountOrder());
                    }
                    List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
                    storeMealChargeDTOs.add(storeMealChargeDTO);
                    storeChargeMap.put(ckey, storeMealChargeDTO);
                }
                if(sweepMealTime > storeMealDTO.getSweepMealTime()){//更新订单的划菜时间
                    storeMealDTO.setSweepMealTime(sweepMealTime);
                }
            } else {
                StoreMealProductDTO storeMealProductDTO = this.storeMealMultiHelper.newStoreMealProductDTO(storeMealSend);
                StoreMealChargeDTO storeMealChargeDTO = this.storeMealMultiHelper.newStoreMealChargeDTO(storeMealSend);
                List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
                storeMealProductDTOs.add(storeMealProductDTO);
                List<StoreMealChargeDTO> storeMealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
                storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
                storeMealChargeDTO.setAmount(storeMealSend.getRemainSend());
                storeMealChargeDTO.setNumDiv(numDiv);
                if(isPrint){
                    storeMealChargeDTO.setAmount(storeMealSend.getAmountOrder());
                }
                storeMealChargeDTOs.add(storeMealChargeDTO);
                StoreMealDTO storeMealDTO = this.storeMealMultiHelper.newStoreMealDTO(storeMealSend);
                storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
                storeMealMap.put(mealKey, storeMealDTO);
                storeChargeMap.put(ckey, storeMealChargeDTO);
            }
        }
        List<StoreMealDTO> storeMealDTOs = new ArrayList<StoreMealDTO>(storeMealMap.values());
        for (StoreMealDTO storeMealDTO : storeMealDTOs) {
            List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
            List<StoreMealChargeDTO> mealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
            for (StoreMealChargeDTO storeMealChargeDTO : storeMealChargeDTOs) {
                double amount = storeMealChargeDTO.getAmount();
                // 分单显示项目
                if(storeMealChargeDTO.isNumDiv()){
                    if(amount > 1){
                        //分单的收费项目按个数拆分
                        storeMealChargeDTO.setAmount(1);
                        for (int i = 1; i < amount; i++) {
                            StoreMealChargeDTO mealChargeDTO = storeMealChargeDTO.deepCopy();
                            mealChargeDTOs.add(mealChargeDTO);
                        }
                    }
                }
            }
            storeMealChargeDTOs.addAll(mealChargeDTOs);
        }
        this.sortStoreMealDTOsBySendTakeupTime(storeMealDTOs);
        this.storeMealMultiHelper.setStoreMealChargeDTO(merchantId, storeId, storeMealDTOs);
        return storeMealDTOs;
    }
    
    /**
     * 已划菜分单
     * @param merchantId
     * @param storeId
     * @param storeMealSweeps
     * @throws TException
     */
    public List<StoreMealDTO> buildStoreMealDTO4Sweep(int merchantId, long storeId, List<StoreMealSweep> storeMealSweeps, boolean enableSlave) throws TException{
        Map<String, StoreMealDTO> storeMealMap = new HashMap<String, StoreMealDTO>();
        Map<String, StoreMealChargeDTO> storeMealChargeMap = new HashMap<String, StoreMealChargeDTO>();
        for (StoreMealSweep storeMealSweep : storeMealSweeps) {
            String orderId = storeMealSweep.getOrderId();
            long portId = storeMealSweep.getPortId();// 加工档口
            long sendPortId = storeMealSweep.getSendPortId();// 传菜口
            int takeSerialNumber = storeMealSweep.getTakeSerialNumber();
            long chargeItemId = storeMealSweep.getChargeItemId();
            boolean packaged = storeMealSweep.isPackaged();
            long sweepMealTime = storeMealSweep.getSweepMealTime();
            // 分单规则
            String mealKey = orderId + "_" + sendPortId + "_" + takeSerialNumber + "_" + sweepMealTime + "_" + packaged;
            String chargeKey = orderId + "_" + portId + "_" + takeSerialNumber + "_" + chargeItemId + "_" + sweepMealTime + "_" + packaged;
            // 分单主键
            String ckey = chargeKey;
            if (storeMealMap.containsKey(mealKey)) {
                StoreMealDTO storeMealDTO = storeMealMap.get(mealKey);
                if (storeMealChargeMap.containsKey(ckey)) {
                    StoreMealChargeDTO storeMealChargeDTO = storeMealChargeMap.get(ckey);//this.storeMealMultiHelper.getStoreMealChargeDTOByChargeKey(storeMealChargeMap.get(ckey), storeMealDTO.getStoreMealChargeDTOs());
                    StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealSweep);
                    List<StoreMealProductDTO> storeMealProductDTOs = storeMealChargeDTO.getStoreMealProductDTOs();
                    storeMealProductDTOs.add(storeMealProductDTO);
                } else {
                    StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealSweep);
                    StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealSweep);
                    List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
                    storeMealProductDTOs.add(storeMealProductDTO);
                    storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
                    List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
                    if (storeMealChargeDTOs == null || storeMealChargeDTOs.isEmpty()) {
                        storeMealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
                        storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
                    }
                    storeMealChargeDTOs.add(storeMealChargeDTO);
                    storeMealChargeMap.put(ckey, storeMealChargeDTO);
                }
            } else {
                StoreMealProductDTO storeMealProductDTO = this.newStoreMealProductDTO(storeMealSweep);
                StoreMealChargeDTO storeMealChargeDTO = this.newStoreMealChargeDTO(storeMealSweep);
                List<StoreMealProductDTO> storeMealProductDTOs = new ArrayList<StoreMealProductDTO>();
                storeMealProductDTOs.add(storeMealProductDTO);
                List<StoreMealChargeDTO> storeMealChargeDTOs = new ArrayList<StoreMealChargeDTO>();
                storeMealChargeDTO.setStoreMealProductDTOs(storeMealProductDTOs);
                storeMealChargeDTOs.add(storeMealChargeDTO);
                StoreMealDTO storeMealDTO = this.newStoreMealDTO(storeMealSweep);
                storeMealDTO.setStoreMealChargeDTOs(storeMealChargeDTOs);
                storeMealMap.put(mealKey, storeMealDTO);
                storeMealChargeMap.put(ckey, storeMealChargeDTO);
            }
        }
        List<StoreMealDTO> storeMealDTOs = new ArrayList<StoreMealDTO>(storeMealMap.values());
        this.storeMealMultiHelper.setStoreMealChargeDTO(merchantId, storeId, storeMealDTOs);
        this.sortStoreMealDTOsBySweepMealTime(storeMealDTOs);
        return storeMealDTOs;
    }

    /**
     * 按桌台显示页面构造返回值
     * @param merchantId
     * @param storeId
     * @param storeMealSweepAmounts
     * @return
     * @throws TException
     */
    public List<StoreMealTableSweepDTO> buildStoreMealTableSweepDTOs(int merchantId, long storeId, List<StoreMealSweepAmount> storeMealSweepAmounts) throws TException {
        boolean enableSlave = true;
        List<StoreMealTableSweepDTO> storeMealTableSweepDTOs = new ArrayList<StoreMealTableSweepDTO>();
        if(storeMealSweepAmounts.isEmpty()){
            return storeMealTableSweepDTOs;
        }
        Set<Long> tableRecordIds = new HashSet<Long>();
        Set<String> orderIds = new HashSet<String>();
        for (StoreMealSweepAmount storeMealSweepAmount : storeMealSweepAmounts) {
            if(storeMealSweepAmount.getTableRecordId() > 0){
                tableRecordIds.add(storeMealSweepAmount.getTableRecordId());
            }
            orderIds.add(storeMealSweepAmount.getOrderId());
        }
        
        Map<String, StoreOrder> storeOrders = storeOrderDAO.getStoreOrderMapInIds(merchantId, storeId, new ArrayList<String>(orderIds), enableSlave);
        this.storeOrderHelper.setStoreOrderTimes(new ArrayList<StoreOrder>(storeOrders.values()), enableSlave);
        
        Map<Long, StoreTableRecord> storeTableRecords = this.storeTableRecordService.getStoreTableRecordMapByIds(merchantId, storeId, new ArrayList<Long>(tableRecordIds), enableSlave);
        for (StoreMealSweepAmount storeMealSweepAmount : storeMealSweepAmounts) {
            String orderId = storeMealSweepAmount.getOrderId();
            StoreOrder storeOrder = storeOrders.get(orderId);
            if(storeOrder == null){//正常情况不会出现
                continue;
            }
            StoreTableRecord storeTableRecord = storeTableRecords.get(storeOrder.getTableRecordId());
            StoreMealTableSweepDTO tableSweepDTO = this.buildStoreMealTableSweepDTO(storeOrder, storeTableRecord, enableSlave);
            tableSweepDTO.setAmountProduct(storeMealSweepAmount.getAmountProduct());
            tableSweepDTO.setSweepAmount(storeMealSweepAmount.getSweepAmount());
            tableSweepDTO.setAllSweepMeal(false);
            storeMealTableSweepDTOs.add(tableSweepDTO);
        }
        this.sortStoreMealTableSweepDTOs(storeMealTableSweepDTOs);
        return storeMealTableSweepDTOs;
    }
    
    public StoreMealTableSweepDTO buildStoreMealTableSweepDTO(StoreOrder storeOrder, StoreTableRecord storeTableRecord, boolean enableSlave){
        StoreMealTableSweepDTO tableSweepDTO = new StoreMealTableSweepDTO();
        if(storeTableRecord != null){
            tableSweepDTO.setTableRecordId(storeTableRecord.getTableRecordId());
            
            tableSweepDTO.setTableRecordName(storeTableRecord.getTableName());
            if(storeTableRecord.getTableRecordSeq() > 0){
                tableSweepDTO.setTableRecordName(storeTableRecord.getTableName() + "(+" + storeTableRecord.getTableRecordSeq() +")");//桌台名称(+n)拼台序号
            }
            tableSweepDTO.setTableAreaId(storeTableRecord.getAreaId());
            tableSweepDTO.setTableAreaName(storeTableRecord.getAreaName());
            tableSweepDTO.setOrderId(storeTableRecord.getOrderId());
            tableSweepDTO.setSendType(storeTableRecord.getSendType());
            tableSweepDTO.setTakeSerialTime(storeTableRecord.getTableRecordTime());//开台时间
        }else{
            tableSweepDTO.setOrderId(storeOrder.getOrderId());
            tableSweepDTO.setSendType(storeOrder.getSendType());
            tableSweepDTO.setTakeSerialTime(storeOrder.getTakeSerialTime());//取号时间
        }
        tableSweepDTO.setTakeSerialNumber(storeOrder.getTakeSerialNumber());
        tableSweepDTO.setTakeMode(storeOrder.getTakeMode());
        tableSweepDTO.setAllSweepMeal(false);
        return tableSweepDTO;
    }
    
    public StoreMealTableSweepDTO buildStoreMealTableSweepDTO(StoreOrder storeOrder, List<StoreMealTakeup> storeMealSends, List<StoreMealSweep> storeMealSweeps, boolean enableSlave) throws TException{
        int merchantId = storeOrder.getMerchantId();
        long storeId = storeOrder.getStoreId();
        
        List<StoreMealDTO> mealDTOMap4Send = this.buildStoreMealDTO4Send(merchantId, storeId, storeMealSends, false, enableSlave);
        List<StoreMealDTO> mealDTOMap4Sweep = this.buildStoreMealDTO4Sweep(merchantId, storeId, storeMealSweeps, enableSlave);
        Set<String> orderIds = new HashSet<String>();
        for (StoreMealDTO storeMealDTO : mealDTOMap4Send) {
            orderIds.add(storeMealDTO.getOrderId());
        }
        for (StoreMealDTO storeMealDTO : mealDTOMap4Sweep) {
            orderIds.add(storeMealDTO.getOrderId());
        }
        Map<String, StoreOrder> storeOrderMap = this.storeOrderDAO.getStoreOrderMapInIds(merchantId, storeId, new ArrayList<String>(orderIds), enableSlave);
        this.setStoreOrderDetail(storeOrderMap, enableSlave);
        
        this.buildStoreMealDetail(mealDTOMap4Send, storeOrderMap);
        this.buildStoreMealDetail(mealDTOMap4Sweep, storeOrderMap);
        
        StoreTableRecord storeTableRecord = null;
        if(storeOrder.getTableRecordId() > 0){
            storeTableRecord = this.storeTableRecordDAO.getStoreTableRecordById(merchantId, storeId, storeOrder.getTableRecordId(), false);
        }
        StoreMealTableSweepDTO storeMealTableSweepDTO = this.buildStoreMealTableSweepDTO(storeOrder, storeTableRecord, enableSlave);
        if(storeMealSends.isEmpty()){
            storeMealTableSweepDTO.setAllSweepMeal(true);
        }
        storeMealTableSweepDTO.setSendMealDTOs(mealDTOMap4Send);
        storeMealTableSweepDTO.setSweepMealDTOs(mealDTOMap4Sweep);
        return storeMealTableSweepDTO;
    }

    public void setStoreOrderDetail(Map<String, StoreOrder> storeOrderMap, boolean enableSlave) throws TException {
        List<StoreOrder> storeOrders = new ArrayList<StoreOrder>(storeOrderMap.values());
        this.storeOrderHelper.setStoreOrderTimes(storeOrders, enableSlave);
        this.storeOrderHelper.setStoreOrderDelivery(storeOrders, enableSlave);
        this.storeOrderHelper.setStoreOrderTimeBucket(storeOrders, enableSlave);
        this.storeOrderHelper.setStoreOrderTableRecord(storeOrders, enableSlave);
        this.storeOrderHelper.setStoreOrderActualPayResult(storeOrders);
    }

    public void buildStoreMealDetail(List<StoreMealDTO> storeMealDTOs, Map<String, StoreOrder> storeOrderMap) {
        for (StoreMealDTO storeMealDTO : storeMealDTOs) {
            StoreOrder order = storeOrderMap.get(storeMealDTO.getOrderId());
            StoreOrderDTO storeOrderDTO = BeanUtil.copy(order, StoreOrderDTO.class);
            if(order.getStoreOrderDelivery() != null){
                StoreOrderDeliveryDTO storeOrderDeliveryDTO = BeanUtil.copy(order.getStoreOrderDelivery(), StoreOrderDeliveryDTO.class);
                storeOrderDTO.setStoreOrderDeliveryDTO(storeOrderDeliveryDTO);
            }
            if(order.getStoreTimeBucket() != null){
                StoreTimeBucketDTO storeTimeBucketDTO = BeanUtil.copy(order.getStoreTimeBucket(), StoreTimeBucketDTO.class);
                storeOrderDTO.setStoreTimeBucketDTO(storeTimeBucketDTO);
            }
            if(order.getStoreOrderActualPayResult() != null){
                StoreOrderActualPayDTO storeOrderActualPayDTO = BeanUtil.copy(order.getStoreOrderActualPayResult(), StoreOrderActualPayDTO.class);
                storeOrderDTO.setStoreOrderActualPayDTO(storeOrderActualPayDTO);
            }
            if(order.getStoreTableRecord() != null){
                storeOrderDTO.setStoreOrderTableRecordDTO(BeanUtil.copy(order.getStoreTableRecord(), StoreOrderTableRecordDTO.class));
            }
            storeMealDTO.setStoreOrderDTO(storeOrderDTO);
        }
    }
    
    public List<StoreMealTableSweepDTO> buildStoreMealTableSweepDTOs(int merchantId, long storeId, List<String> orderIds, List<Long> tableRecordIds, List<StoreMealDTO> storeMealDTOs, boolean enableSlave) throws TException {
        List<StoreMealTableSweepDTO> storeMealTableSweepDTOs = new ArrayList<StoreMealTableSweepDTO>();
        Map<String, StoreOrder> storeOrderMap = this.storeOrderDAO.getStoreOrderMapInIds(merchantId, storeId, orderIds, enableSlave);
        
        this.setStoreOrderDetail(storeOrderMap, enableSlave);
        this.buildStoreMealDetail(storeMealDTOs, storeOrderMap);
        
        Map<String, StoreMealTableSweepDTO> tableSweepDTOMaps = new HashMap<String, StoreMealTableSweepDTO>();
        for (StoreMealDTO storeMealDTO : storeMealDTOs) {
            String orderId = storeMealDTO.getOrderId();
            StoreOrder storeOrder = storeOrderMap.get(orderId);
            StoreTableRecord storeTableRecord = storeOrder.getStoreTableRecord();
            StoreMealTableSweepDTO tableSweepDTO = this.buildStoreMealTableSweepDTO(storeOrder,storeTableRecord,enableSlave);
            if (storeOrder.getTakeSerialTime() > tableSweepDTO.getTakeSerialTime()) {
                tableSweepDTO.setTakeSerialTime(storeOrder.getTakeSerialTime());// 取号时间
            }
            if (storeMealDTO.getSweepMealTime() > tableSweepDTO.getSweepMealTime()) {// 取订单上最近划菜时间作为桌台的划菜时间
                tableSweepDTO.setSweepMealTime(storeMealDTO.getSweepMealTime());
            }
            if (tableSweepDTOMaps.get(orderId) != null) {
                tableSweepDTOMaps.get(orderId).getSendMealDTOs().add(storeMealDTO);
            } else {
                List<StoreMealDTO> mealDTOs = new ArrayList<StoreMealDTO>();
                mealDTOs.add(storeMealDTO);
                tableSweepDTO.setSendMealDTOs(mealDTOs);
                tableSweepDTO.setAllSweepMeal(false);
                tableSweepDTOMaps.put(orderId, tableSweepDTO);
            }
        }
        storeMealTableSweepDTOs.addAll(tableSweepDTOMaps.values());
        this.sortStoreMealTableSweepDTOs(storeMealTableSweepDTOs);
        return storeMealTableSweepDTOs;
    }

    public void filterStoreMelDTOByProduct(long productId, List<StoreMealDTO> sendMealDTOs) {
        if(sendMealDTOs != null){
            for (Iterator<StoreMealDTO> orderIterator = sendMealDTOs.iterator(); orderIterator.hasNext();) {
                StoreMealDTO storeMealDTO = orderIterator.next();
                List<StoreMealChargeDTO> storeMealChargeDTOs = storeMealDTO.getStoreMealChargeDTOs();
                if(storeMealChargeDTOs != null){
                    for (Iterator<StoreMealChargeDTO> chargeIterator = storeMealChargeDTOs.iterator(); chargeIterator.hasNext();) {
                        StoreMealChargeDTO storeMealChargeDTO = chargeIterator.next();
                        boolean exitProduct = false;
                        List<StoreMealProductDTO> storeMealProductDTOs = storeMealChargeDTO.getStoreMealProductDTOs();
                        if(storeMealProductDTOs != null){
                            for (Iterator<StoreMealProductDTO> productIterator = storeMealProductDTOs.iterator(); productIterator.hasNext();) {
                                StoreMealProductDTO storeMealProductDTO = productIterator.next();
                                if(storeMealProductDTO.getProductId() == productId){
                                    exitProduct = true;
                                    break;
                                }
                            }
                        }
                        if(!exitProduct){
                            chargeIterator.remove();
                        }
                    }
                }
                if(storeMealChargeDTOs.size() == 0){
                    orderIterator.remove();
                }
            }
        }
    }
    
    public void sortStoreMealDTOsBySendTakeupTime(List<StoreMealDTO> storeMealDTOs){
        Collections.sort(storeMealDTOs, new Comparator<StoreMealDTO>() {
            @Override
            public int compare(StoreMealDTO o1, StoreMealDTO o2) {
                if(o1.getTakeupTime() > o2.getTakeupTime()){
                    return 1;
                }
                if(o1.getTakeupTime() < o2.getTakeupTime()){
                    return -1;
                }
                return 0;
            }
        });
    }
    
    public void sortStoreMealDTOsBySweepMealTime(List<StoreMealDTO> storeMealDTOs){
        Collections.sort(storeMealDTOs, new Comparator<StoreMealDTO>() {
            @Override
            public int compare(StoreMealDTO o1, StoreMealDTO o2) {
                if(o1.getSweepMealTime() < o2.getSweepMealTime()){
                    return 1;
                }
                if(o1.getSweepMealTime() > o2.getSweepMealTime()){
                    return -1;
                }
                return 0;
            }
        });
    }
    
    /**
     * 桌台按起菜状态和取号时间排序
     * @param storeMealTableSweepDTOs
     */
    public void sortStoreMealTableSweepDTOs(List<StoreMealTableSweepDTO> storeMealTableSweepDTOs){
        Collections.sort(storeMealTableSweepDTOs, new Comparator<StoreMealTableSweepDTO>() {
            @Override
            public int compare(StoreMealTableSweepDTO o1, StoreMealTableSweepDTO o2) {
                //起菜状态升序
                if(o1.getSendType() > o2.getSendType()){
                    return 1;
                }
                if(o1.getSendType() < o2.getSendType()){
                    return -1;
                }
                //取号时间降序
                if(o1.getTakeSerialTime() < o2.getTakeSerialTime()){
                    return -1;
                }
                if(o1.getTakeSerialTime() > o2.getTakeSerialTime()){
                    return 1;
                }
                return 0;
            }
        });
    }
    
    /**
     * 桌台按划菜时间排序
     * @param storeMealTableSweepDTOs
     */
    public void sortStoreMealTableSweepDTOsBySweepMealTime(List<StoreMealTableSweepDTO> storeMealTableSweepDTOs){
        Collections.sort(storeMealTableSweepDTOs, new Comparator<StoreMealTableSweepDTO>() {
            @Override
            public int compare(StoreMealTableSweepDTO o1, StoreMealTableSweepDTO o2) {
                if(o1.getSweepMealTime() < o2.getSweepMealTime()){
                    return 1;
                }
                if(o1.getTakeSerialTime() > o2.getTakeSerialTime()){
                    return -1;
                }
                return 0;
            }
        });
    }
}
