package com.huofu.module.i5wei.meal.facade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.meal.dao.StoreMealSweepAmount;
import com.huofu.module.i5wei.meal.dao.StoreProductSweepAmount;
import com.huofu.module.i5wei.meal.entity.StoreMealSweep;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.meal.service.StoreMealSweepHelper;
import com.huofu.module.i5wei.meal.service.StoreMealSweepService;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.mealport.service.StoreMealPortSendService;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderQueryService;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.service.StoreTableRecordQueryService;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealDTO;
import huofucore.facade.i5wei.meal.StoreSendTypeEnum;
import huofucore.facade.i5wei.mealportsend.StoreMealCancelSweepParam;
import huofucore.facade.i5wei.mealportsend.StoreMealChargeItemSweepParam;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepFacade;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepHistoryQueryParam;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepParam;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepPrintParam;
import huofucore.facade.i5wei.mealportsend.StoreMealSweptPage;
import huofucore.facade.i5wei.mealportsend.StoreMealSweptParam;
import huofucore.facade.i5wei.mealportsend.StoreMealTableSweepDTO;
import huofucore.facade.i5wei.mealportsend.StoreMealUnSweepParam;
import huofucore.facade.i5wei.mealportsend.StoreProductUnSweepDTO;
import huofucore.facade.i5wei.mealportsend.StoreProductUnSweepParam;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;

@ThriftServlet(name = "storeMealSweepFacadeServlet", serviceClass = StoreMealSweepFacade.class)
@Component
public class StoreMealSweepFacadeImpl implements StoreMealSweepFacade.Iface {
    
    private Log log = LogFactory.getLog(StoreMealSweepFacadeImpl.class);

    @Autowired
    private StoreMealPortSendService storeMealPortSendService;
    
    @Autowired
    private StoreMealSweepHelper storeMealSweepHelper;
    
    @Autowired
    private StoreTableRecordService storeTableRecordService;
    
    @Autowired
    private StoreMealSweepService storeMealSweepService;
    
    @Autowired
    private StoreOrderQueryService storeOrderQueryService;
    
    @Autowired
    private StoreHeartbeatService storeHeartbeatService;
    
    @Autowired
    private StoreOrderHelper storeOrderHelper;
    
    @Autowired
    private StoreTableRecordQueryService storeTableRecordQueryService;
    
   
    @Override
    public List<StoreMealTableSweepDTO> getMealUnSweepOrders(int merchantId, long storeId, long sendPortId) throws T5weiException, TException {
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0){
            log.error(DataUtil.infoWithParams("invalid argument params: merchantId=#1,storeId=#2,sendPortId=#3", new Object[]{merchantId,storeId,sendPortId}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        boolean enableSlave = false;
        //计算菜品已上的菜品和全部的菜品数量
        List<StoreMealSweepAmount> storeMealSweepAmounts = this.storeMealSweepService.countStoreMealSweepAmount(merchantId,storeId,sendPortId,enableSlave);
        return this.storeMealSweepHelper.buildStoreMealTableSweepDTOs(merchantId, storeId, storeMealSweepAmounts);
    }
    
    @Override
    public List<StoreProductUnSweepDTO> getProductUnSweepOrders(int merchantId, long storeId, long sendPortId) throws T5weiException, TException {
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0){
            log.error(DataUtil.infoWithParams("invalid argument params: merchantId=#1,storeId=#2,sendPortId=#3", new Object[]{merchantId,storeId,sendPortId}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        boolean enableSlave = false;
        List<StoreProductSweepAmount> storeProductSweepAmount = this.storeMealSweepService.countStoreProductSweepAmount(merchantId, storeId, sendPortId, enableSlave);
        List<StoreProductUnSweepDTO> storeProductUnSweepDTOs = BeanUtil.copyList(storeProductSweepAmount, StoreProductUnSweepDTO.class);
        return storeProductUnSweepDTOs;
    }
    
    @Override
    public StoreMealTableSweepDTO getMealUnSweepOrdersDetail(StoreMealUnSweepParam param) throws TException{
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        String orderId = param.getOrderId();
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0 || DataUtil.isEmpty(orderId)){
            log.error(DataUtil.infoWithParams("invalid argument params: merchantId=#1,storeId=#2,sendPortId=#3,orderId=#4", new Object[]{merchantId,storeId,sendPortId,orderId}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        StoreMealTableSweepDTO storeMealTableSweepDTO = this.storeMealSweepService.getMealUnSweepOrdersDetail(param);
        return storeMealTableSweepDTO;
    }
    
    @Override
    public List<StoreMealTableSweepDTO> getProductUnSweepOrdersDetail(StoreProductUnSweepParam param) throws T5weiException, TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        long productId = param.getProductId();
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0 || productId<=0){
            log.error(DataUtil.infoWithParams("invalid argument params=#1", new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        List<StoreMealTableSweepDTO> storeMealTableSweepDTOs = this.storeMealSweepService.getProductUnSweepOrdersDetail(param);
        return storeMealTableSweepDTOs;
    }

    @Override
    public StoreMealTableSweepDTO sweepStoreMeal(StoreMealSweepParam param) throws T5weiException, TException {
        if(param.getSendMealPortId() <= 0){
            log.error(DataUtil.infoWithParams("invalid argument params=#1", new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        if(DataUtil.isEmpty(param.getOrderId())){
            log.error(DataUtil.infoWithParams("订单号不能为空param=#1", new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_NO_ORDER_ID.getValue(), "store sweep no order id");
        }
        if(!param.isSweepAll()){
            List<StoreMealChargeItemSweepParam> storeMealChargeItemSweepParams = param.getStoreMealChargeItemSweepParams();
            if(storeMealChargeItemSweepParams == null){
                log.error(DataUtil.infoWithParams("收费项不能为空param=#1", new Object[]{param}));
                throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_NO_CHARGE_ITEM.getValue(), "store sweep no charge item");
            }
            for (StoreMealChargeItemSweepParam storeMealChargeItemSweepParam : storeMealChargeItemSweepParams) {
                if(storeMealChargeItemSweepParam.getChargeItemId() <= 0){
                    log.error(DataUtil.infoWithParams("收费项不能为空param=#1", new Object[]{param}));
                    throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_NO_CHARGE_ITEM.getValue(), "store sweep no charge item");
                }
                if(storeMealChargeItemSweepParam.getSweepAmount() <= 0){
                    log.error(DataUtil.infoWithParams("划菜数量不能小于等于零param=#1", new Object[]{param}));
                    throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_AMOUNT_INVALID.getValue(), "store sweep amount invalid");
                }
                if(storeMealChargeItemSweepParam.getProductIds() == null || storeMealChargeItemSweepParam.getProductIds().isEmpty()){
                    log.error(DataUtil.infoWithParams("收费项目中不包括产品param=#1", new Object[]{param}));
                    throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_NO_PRODUCT.getValue(), "store sweep no product");
                }
            }
        }
        boolean enableSlave = false;
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        String orderId = param.getOrderId();
        long sendPortId = param.getSendMealPortId();
        
        StoreOrder storeOrder = this.storeOrderQueryService.getStoreOrderById(merchantId, storeId, orderId);
        if(storeOrder.getTableRecordId() > 0){
            StoreTableRecord storeTableRecord = this.storeTableRecordQueryService.getStoreTableRecordById(merchantId, storeId, storeOrder.getTableRecordId());
            if(storeTableRecord.getSendType() == StoreSendTypeEnum.WAIT.getValue()){
                log.error(DataUtil.infoWithParams("桌台为叫起状态不能划菜param=#1", new Object[]{param}));
                throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_TABLE_SEND_WAIT.getValue(), "table is waitting , not sweep ");
            }
        } else {
            if(storeOrder.getSendType() == StoreSendTypeEnum.WAIT.getValue()){
                log.error(DataUtil.infoWithParams("订单为叫起状态不能划菜param=#1", new Object[]{param}));
                throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_ORDER_SEND_WAIT.getValue(), "order is waitting , not sweep");
            }
        }
        List<StoreMealSweep> storeMealSweeps = this.storeMealSweepService.sweepStoreMeal(param);//划菜
        //划菜成功，但是查询超时。
        List<StoreMealTakeup> storeMealTakeups = this.storeMealSweepService.getStoreMealSends(merchantId, storeId, sendPortId, storeOrder.getTableRecordId(), orderId, enableSlave, false);
        StoreMealTableSweepDTO storeMealTableSweepDTO = this.storeMealSweepHelper.buildStoreMealTableSweepDTO(storeOrder, storeMealTakeups, storeMealSweeps, true);
        this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), true, null);//更新心跳
        return storeMealTableSweepDTO;
    }
    
    @Override
    public List<StoreMealDTO> sweepStoreMealTables(List<StoreMealSweepParam> params) throws T5weiException, TException {
        boolean enableSlave = false;
        if(params.isEmpty()){
            log.error(DataUtil.infoWithParams("invalid argument params=#1",new Object[]{params}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        int merchantId = params.get(0).getMerchantId();
        long storeId = params.get(0).getStoreId();
        long sendPortId = params.get(0).getSendMealPortId();
        StoreMealPortSend storeMealPortSend = this.storeMealPortSendService.getStoreMealPortSendById(merchantId, storeId, sendPortId, false, true);
        
        Set<Long> tableRecordId = new HashSet<Long>();
        Set<String> orderIds = new HashSet<String>();
        if (storeMealPortSend.isTableSweep()) {
            for (StoreMealSweepParam storeMealSweepParam : params) {
                if (storeMealSweepParam.getTableRecordId() > 0) {
                    tableRecordId.add(storeMealSweepParam.getTableRecordId());
                } else {
                    orderIds.add(storeMealSweepParam.getOrderId());
                }
            }
        } else {
            for (StoreMealSweepParam storeMealSweepParam : params) {
                orderIds.add(storeMealSweepParam.getOrderId());
            }
        }
        List<StoreOrder> storeOrders = this.storeOrderQueryService.getStoreOrders(merchantId, storeId, new ArrayList<String>(orderIds));
        for (StoreOrder storeOrder : storeOrders) {
            if(StoreSendTypeEnum.WAIT.getValue() == storeOrder.getSendType()){
                log.error(DataUtil.infoWithParams("订单为起菜状态不能划菜param=#1", new Object[]{params}));
                throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_ORDER_SEND_WAIT.getValue(), "order is waitting, not sweep");
            }
        }
        Map<Long, StoreTableRecord> storeTableRecordMapByIds = this.storeTableRecordService.getStoreTableRecordMapByIds(merchantId, storeId, new ArrayList<Long>(tableRecordId), true);
        for(Entry<Long, StoreTableRecord> storeTableRecord : storeTableRecordMapByIds.entrySet()){
            if(StoreSendTypeEnum.WAIT.getValue() == storeTableRecord.getValue().getSendType()){
                log.error(DataUtil.infoWithParams("桌台为起菜状态不能划菜param=#1", new Object[]{params}));
                throw new T5weiException(T5weiErrorCodeType.STORE_SWEEP_TABLE_SEND_WAIT.getValue(), "table is waitting, not sweep");
            }
        }
        List<StoreMealSweep> storeMealSweeps = this.storeMealSweepService.sweepStoreMealChargeBySendPort(params, false);
        List<StoreMealDTO> storeMealDTOs = this.storeMealSweepHelper.getStoreMealSweepHistory(merchantId, storeId, storeMealSweeps, enableSlave);
        this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), true, null);//更新心跳
        return storeMealDTOs;
    }
    
    @Override
    public StoreMealSweptPage getMealSweptOrders(StoreMealSweptParam param) throws T5weiException, TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        int pageSize = param.getPageSize();
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0 || pageSize<=0){
            log.error(DataUtil.infoWithParams("invalid argument params=#1",new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        boolean enableSlave = true;
        return this.storeMealSweepService.getStoreMealSweptPage(param, enableSlave);
    }

    @Override
    public List<StoreMealDTO> getStoreMealSweepHistory(StoreMealSweepHistoryQueryParam param) throws T5weiException, TException {
        boolean enableSlave = true;
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        long repastDate = param.getRepastDate();
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0 || repastDate <= 0){
            log.error(DataUtil.infoWithParams("invalid argument params=#1",new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        List<StoreMealSweep> storeMealSweeps = this.storeMealSweepService.getStoreMealSweeps(param, enableSlave);
        List<StoreMealDTO> storeMealDTOs = this.storeMealSweepHelper.buildStoreMealDTO4Sweep(merchantId, storeId, storeMealSweeps, enableSlave);
        Set<String> orderIds = new HashSet<String>();
        for (StoreMealDTO storeMealDTO : storeMealDTOs) {
            orderIds.add(storeMealDTO.getOrderId());
        }
        Map<String, StoreOrder> storeOrderMap = this.storeOrderQueryService.getStoreOrderSimpleMapInIds(merchantId, storeId, new ArrayList<String>(orderIds), enableSlave);
        this.storeOrderHelper.setStoreOrderTableRecord(new ArrayList<StoreOrder>(storeOrderMap.values()), enableSlave);
        this.storeMealSweepHelper.buildStoreMealDetail(storeMealDTOs, storeOrderMap);
        return storeMealDTOs;
    }
    
    @Override
    public StoreMealTableSweepDTO cancelStoreMealSweep(StoreMealCancelSweepParam param) throws T5weiException, TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId =param.getSendPortId();
        String orderId = param.getOrderId();
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0 || DataUtil.isEmpty(orderId)){
            log.error(DataUtil.infoWithParams("invalid argument params=#1",new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        boolean enableSlave = false;
        //取消划菜
        this.storeMealSweepService.cancelStoreMealSweep(param);
        //查询数据返回
        StoreOrder storeOrder = this.storeOrderQueryService.getStoreOrderById(merchantId, storeId, orderId);
        List<StoreMealTakeup> storeMealSends = this.storeMealSweepService.getStoreMealSends(merchantId, storeId, sendPortId, storeOrder.getTableRecordId(), orderId, enableSlave, false);
        List<StoreMealSweep> storeMealSweeps = storeMealSweepService.getStoreMealSweeps(merchantId, storeId, sendPortId, storeOrder.getTableRecordId(), orderId, enableSlave, false);
        
        StoreMealTableSweepDTO storeMealTableSweepDTO = this.storeMealSweepHelper.buildStoreMealTableSweepDTO(storeOrder, storeMealSends, storeMealSweeps,  true);
        this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), true, null);//更新心跳
        return storeMealTableSweepDTO;
    }

    @Override
    public List<StoreMealDTO> printStoreMealSweep(StoreMealSweepPrintParam param) throws T5weiException, TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId =param.getSendPortId();
        String orderId = param.getOrderId();
        if(merchantId <= 0 || storeId <= 0 || sendPortId <= 0 || DataUtil.isEmpty(orderId)){
            log.error(DataUtil.infoWithParams("invalid argument params=#1",new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "invalid argument");
        }
        boolean enableSlave = true;
        return this.storeMealSweepService.printStoreMealSweep(param, enableSlave);
    }
}
