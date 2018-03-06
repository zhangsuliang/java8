package com.huofu.module.i5wei.meal.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.meal.entity.StoreMealSweepRecord;
import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.mealportsend.StoreMealSweptParam;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.ObjectUtil;
import huofuhelper.util.PageUtil;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealSweepRecordDAO extends AbsQueryDAO<StoreMealSweepRecord> {
    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
    
    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreMealSweepRecord.class);
        return dalInfo.getRealTable(StoreMealSweepRecord.class);
    }
    
    @Override
    public List<StoreMealSweepRecord> batchCreate(List<StoreMealSweepRecord> storeMealSweepRecords) {
        if(storeMealSweepRecords == null || storeMealSweepRecords.isEmpty()){
            return new ArrayList<StoreMealSweepRecord>();
        }
        this.addDbRouteInfo(storeMealSweepRecords.get(0).getMerchantId(), storeMealSweepRecords.get(0).getStoreId());
        return super.batchCreate(storeMealSweepRecords);
    }
    
    public void batchUpdate(int merchantId, long storeId, List<StoreMealSweepRecord> updateSweepRecords) {
        this.addDbRouteInfo(merchantId, storeId);
        if(updateSweepRecords == null || updateSweepRecords.isEmpty()){
            return;
        }
        List<Object[]> params = new ArrayList<Object[]>();
        
        for (StoreMealSweepRecord storeMealSweepRecord : updateSweepRecords) {
            Object[] obj = new Object[] { storeMealSweepRecord.getAmountProduct(), storeMealSweepRecord.getSweepMealAmount(), 
                    storeMealSweepRecord.getLastSweepMealTime(), storeMealSweepRecord.isAllSweepStatus(), storeMealSweepRecord.getUpdateTime(),
                    storeMealSweepRecord.getTid() };
            params.add(obj);
        }
        
        this.query.batchUpdate(StoreMealSweepRecord.class, 
                " set amount_product = ? , sweep_meal_amount = ? , last_sweep_meal_time = ? , all_sweep_status = ? , update_time = ? where tid= ?", 
                params);
    }
    
    public void deleteByOrderId(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.delete(StoreMealSweepRecord.class, " where order_id = ? ", new Object[]{orderId});
    }
    
    public Map<String, StoreMealSweepRecord> getStoreMealSweepRecords(int merchantId, long storeId, String orderId, List<Long> sendPortIds, boolean enableSlave, boolean forUpdate){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" where order_id = ? ");
        params.add(orderId);
        Map<String, StoreMealSweepRecord> result = new HashMap<String, StoreMealSweepRecord>();
        
        if(sendPortIds != null && !sendPortIds.isEmpty()){
            sql.append(" and ").append(Query.createInSql(" send_port_id  ", sendPortIds.size()));
            params.addAll(sendPortIds);
        }
        
        if(forUpdate){
            sql.append(" for update ");
        }
        
        List<StoreMealSweepRecord> storeMealSweepRecords = this.query.list2(StoreMealSweepRecord.class, sql.toString(), params);
        for (StoreMealSweepRecord storeMealSweepRecord : storeMealSweepRecords) {
            result.put(storeMealSweepRecord.getOrderId() + "_" + storeMealSweepRecord.getSendPortId(), storeMealSweepRecord);
        }
        return result;
    }
    
    public StoreMealSweepRecord getStoreMealSweepRecordbyIds(int merchantId, long storeId, String orderId, long sendPortId, boolean enableSlave){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreMealSweepRecord.class, " where order_id = ? and send_port_id = ? ", new Object[]{orderId,sendPortId});
    }
    
    /**
     * 统计订单或桌台上产品未上齐数和总数
     * @param merchantId
     * @param storeId
     * @param sendPortId
     * @param enableSlave
     * @return
     */
    public List<StoreMealSweepAmount> countStoreMealSweepAmount(int merchantId, long storeId, long sendPortId, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        StringBuffer sql = new StringBuffer();
        sql.append(" select order_id, table_record_id, amount_product, sweep_meal_amount from ");
        sql.append(this.getRealName(merchantId, storeId));
        sql.append(" where send_port_id = ? and all_sweep_status = ?");
        
        List<Map<String, Object>> mapList = this.query.getJdbcSupport().getMapList(
                sql.toString(), 
                new Object[]{sendPortId,false});
        List<StoreMealSweepAmount> storeMealSweepAmounts = new ArrayList<StoreMealSweepAmount>();
        
        for (Map<String, Object> map : mapList) {
            String orderId = ObjectUtil.getString(map, "order_id");
            long tableRecordId  = ObjectUtil.getLong(map, "table_record_id");
            double amountProduct = ObjectUtil.getDouble(map, "amount_product");
            double sweepMealAmount = ObjectUtil.getDouble(map, "sweep_meal_amount");
            
            StoreMealSweepAmount storeMealSweepAmount = new StoreMealSweepAmount();
            storeMealSweepAmount.setOrderId(orderId);
            storeMealSweepAmount.setTableRecordId(tableRecordId);
            storeMealSweepAmount.setAmountProduct(amountProduct);
            storeMealSweepAmount.setSweepAmount(sweepMealAmount);
            
            storeMealSweepAmounts.add(storeMealSweepAmount);
        }
        return storeMealSweepAmounts;
    }
    
    /**
     * 获取已上齐的记录
     * @param merchantId
     * @param storeId
     * @param sendPortId
     * @param keyWord
     * @param enableSlave
     * @return
     */
    public Map<String,Object> getStoreMealSweepByCond(StoreMealSweptParam param, List<Long> tableRecordIds, boolean enableSlave){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        long repastDate = param.getRepastDate();
        String keyWord = param.getKeyWord();
        int pageNo = param.getPageNo();
        int pageSize = param.getPageSize();
        int beginIndex = PageUtil.getBeginIndex(pageNo, pageSize);

        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" where send_port_id = ? and all_sweep_status = 1 and repast_date = ? and amount_product > 0");
        params.add(sendPortId);
        params.add(repastDate);

        if (DataUtil.isNotEmpty(keyWord)) {
            sql.append(" and ( ");
            sql.append(" take_serial_number like ? ");
            params.add("%" + keyWord + "%");
            if (!tableRecordIds.isEmpty()) {
                sql.append(" or ");
                sql.append(Query.createInSql(" table_record_id ", tableRecordIds.size()));
                params.addAll(tableRecordIds);
            }
            sql.append(" ) ");
        }
        sql.append(" order by last_sweep_meal_time desc ");
        
        int total = this.query.count2(StoreMealSweepRecord.class, sql.toString(), params);//总数量
        
        sql.append(" limit ? , ?");
        params.add(beginIndex);
        params.add(pageSize);
        
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreMealSweepRecord> storeMealSweepRecords = this.query.list2(StoreMealSweepRecord.class, sql.toString(), params);
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("total", total);
        result.put("sweepRecords", storeMealSweepRecords);
        return result;
    }

    public List<StoreMealSweepRecord> getStoreMealSweepRecords(int merchantId, long storeId, String orderId, boolean enableSlave, boolean forUpdate) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer(" where order_id = ? and all_sweep_status = ? ");
        if(forUpdate){
            sql.append(" for update ");
        }
        params.add(orderId);
        params.add(false);
        return this.query.list2(StoreMealSweepRecord.class, sql.toString(), params); 
    }
}
