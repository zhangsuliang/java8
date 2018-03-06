package com.huofu.module.i5wei.meal.dao;

import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DataUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.meal.entity.StoreMealSweep;
import halo.query.Query;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepHistoryQueryParam;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealSweepDAO extends AbsQueryDAO<StoreMealSweep> {
    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
    
    @Override
    public List<StoreMealSweep> batchCreate(List<StoreMealSweep> storeMealSweeps) {
        if(storeMealSweeps.isEmpty()){
            return new ArrayList<StoreMealSweep>();
        }
        this.addDbRouteInfo(storeMealSweeps.get(0).getMerchantId(), storeMealSweeps.get(0).getStoreId());
        return super.batchCreate(storeMealSweeps);
    }

    public List<StoreMealSweep> getStoreMealSweepsByOrderId(int merchantId, long storeId, long sendPortId, String orderId, boolean enableSlave, boolean forUpdate) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuffer sql = new StringBuffer(" where order_id = ? and send_port_id = ? and refund_meal = ? ");
        if(forUpdate){
            sql.append(" for update ");
        }
        return this.query.list(StoreMealSweep.class, sql.toString(), new Object[]{orderId, sendPortId, false });
    }
    
    public List<StoreMealSweep> getStoreMealSweepsByTableRecordId(int merchantId, long storeId, long sendPortId, long tableRecordId, boolean enableSlave, boolean forUpdate) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuffer sql = new StringBuffer(" where table_record_id = ? and send_port_id = ? and refund_meal = ? ");
        if(forUpdate){
            sql.append(" for update ");
        }
        return this.query.list(StoreMealSweep.class, sql.toString(), new Object[]{tableRecordId, sendPortId, false });
    }
    
    public List<StoreMealSweep> getStoreMealSweepByIds(int merchantId, long storeId, List<Long> sweepIds, boolean enableSlave, boolean forUpdate) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        if(sweepIds == null || sweepIds.isEmpty()){
            return new ArrayList<StoreMealSweep>();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuffer sql = new StringBuffer();
        sql.append("where ").append(Query.createInSql("tid", sweepIds.size()));
        if(forUpdate){
            sql.append(" for update ");
        }
        return this.query.list2(StoreMealSweep.class, sql.toString(), sweepIds);
    }
    
    public List<StoreMealSweep> getStoreMealSweepbyCond(StoreMealSweepHistoryQueryParam param, List<Long> tableRecordIds, boolean enableSlave){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        long repastDate = param.getRepastDate();
        String keyWord = param.getKeyWord();
        int startSerialNumber = param.getStartSerialNumber();
        int serialNumberSize = param.getSerialNumberSize();

        this.addDbRouteInfo(merchantId, storeId);
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" where send_port_id = ? and repast_date = ? and refund_meal = ? ");
        params.add(sendPortId);
        params.add(repastDate);
        params.add(false);
        if (DataUtil.isNotEmpty(keyWord)) {
            sql.append(" and ( ");
            sql.append(" take_serial_number like ? ");
            params.add("%" + keyWord + "%");
            if (!tableRecordIds.isEmpty()) {
                sql.append(" or ").append(Query.createInSql(" table_record_id ", tableRecordIds.size()));
                params.addAll(tableRecordIds);
            }
            sql.append(" ) ");
        }
        if (startSerialNumber > 0) {//按流水号分页
            sql.append(" and take_serial_number > ? ");
            params.add(startSerialNumber);
        }
        sql.append(" order by take_serial_number desc ");
        sql.append(" limit 0 , ? ");
        params.add(serialNumberSize);
        return this.query.list2(StoreMealSweep.class, sql.toString(), params);
    }

    public List<StoreMealSweep> getStoreMealSweepbyTakeSerialNumber(int merchantId, long storeId, long sendPortId, long repastDate, ArrayList<Integer> takeSerialNumbers, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append(" where send_port_id = ? and repast_date = ? and refund_meal = ? ");
        params.add(sendPortId);
        params.add(repastDate);
        params.add(false);
        if(takeSerialNumbers.size() > 0){
            sql.append(" and ");
            sql.append(Query.createInSql(" take_serial_number ", takeSerialNumbers.size()));
            params.addAll(takeSerialNumbers);
        }
        return this.query.list2(StoreMealSweep.class, sql.toString(), params);
    }

    /**
     * 批量删除划菜记录
     * @param merchantId
     * @param storeId
     * @param values
     * @return
     */
    public int batchDelete(int merchantId, long storeId, Collection<StoreMealSweep> values) {
        List<Long> storeMealSweepIds = new ArrayList<Long>();
        for (StoreMealSweep storeMealSweep : values) {
            storeMealSweepIds.add(storeMealSweep.getTid());
        }
        if(storeMealSweepIds.isEmpty()){
            return 0;
        }
        this.addDbRouteInfo(merchantId, storeId);
        StringBuffer sql = new StringBuffer();
        sql.append(" where ");
        sql.append(Query.createInSql(" tid ", storeMealSweepIds.size()));
        return this.query.delete2(StoreMealSweep.class, sql.toString(), storeMealSweepIds);
    }

    public void updateTableRecordId(int merchantId, long storeId, long originalTableRecordId, long targetTableRecordId) {
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreMealSweep.class,
                " set table_record_id=?, update_time=? where table_record_id=? ", 
                new Object[]{targetTableRecordId, System.currentTimeMillis(), originalTableRecordId});
    }
}
