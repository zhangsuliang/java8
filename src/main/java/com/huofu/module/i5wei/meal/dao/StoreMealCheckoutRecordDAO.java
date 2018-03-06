package com.huofu.module.i5wei.meal.dao;

import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealHistoryQueryParam;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.meal.entity.StoreMealCheckoutRecord;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealCheckoutRecordDAO extends AbsQueryDAO<StoreMealCheckoutRecord> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
    
    @Override
    public void create(StoreMealCheckoutRecord storeMealCheckoutRecord) {
        this.addDbRouteInfo(storeMealCheckoutRecord.getMerchantId(), storeMealCheckoutRecord.getStoreId());
        super.create(storeMealCheckoutRecord);
    }

    @Override
    public List<StoreMealCheckoutRecord> batchCreate(List<StoreMealCheckoutRecord> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreMealCheckoutRecord storeMealCheckoutRecord) {
        this.addDbRouteInfo(storeMealCheckoutRecord.getMerchantId(), storeMealCheckoutRecord.getStoreId());
        super.update(storeMealCheckoutRecord);
    }

    @Override
    public void delete(StoreMealCheckoutRecord storeMealCheckoutRecord) {
        this.addDbRouteInfo(storeMealCheckoutRecord.getMerchantId(), storeMealCheckoutRecord.getStoreId());
        super.delete(storeMealCheckoutRecord);
    }
    
    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreMealCheckoutRecord.class);
        return dalInfo.getRealTable(StoreMealCheckoutRecord.class);
    }
    
    public List<StoreMealCheckoutRecord> getStoreMealCheckoutRecordByOrderId(int merchantId, long storeId, String orderId, boolean forUpdate){
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where merchant_id=? and store_id=? and order_id=? ");
        if(forUpdate){
        	sql.append(" for update");
        }
        params.add(merchantId);
        params.add(storeId);
        params.add(orderId);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckoutRecord.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public StoreMealCheckoutRecord getStoreMealCheckoutRecordByOrderId(int merchantId, long storeId, String orderId, long portId, boolean forUpdate){
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where merchant_id=? and store_id=? and order_id=? and port_id=? ");
        if(forUpdate){
        	sql.append(" for update");
        }
        params.add(merchantId);
        params.add(storeId);
        params.add(orderId);
        params.add(portId);
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreMealCheckoutRecord.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public StoreMealCheckoutRecord getStoreMealCheckoutRecord(int merchantId, long storeId, long repastDate, int takeSerialNumber, boolean enableSlave) {
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where merchant_id=? and store_id=? and repast_date=? and take_serial_number=? order by update_time desc ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
        params.add(takeSerialNumber);
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreMealCheckoutRecord> list = this.query.list(StoreMealCheckoutRecord.class, sql.toString(), params.toArray(new Object[params.size()]));
		StoreMealCheckoutRecord storeMealCheckoutRecord = new StoreMealCheckoutRecord();
		if (list != null && !list.isEmpty()) {
			storeMealCheckoutRecord = list.get(0);
		}
        return storeMealCheckoutRecord;
    }
    
    /**
     * ＊通过就餐日期、取餐编号和取餐口查询出餐口取餐记录
     * @param merchantId
     * @param storeId
     * @param repastDate
     * @param takeSerialNumber
     * @param portId
     * @return
     */
	public StoreMealCheckoutRecord getStoreMealCheckoutRecord(int merchantId, long storeId, long repastDate, int takeSerialNumber, int portId, boolean enableSlave){
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		StoreMealCheckoutRecord storeMealCheckoutRecord = this.query.obj(StoreMealCheckoutRecord.class, " where store_id=? and repast_date=? and take_serial_number=? and port_id=?", new Object[]{storeId,repastDate,takeSerialNumber,portId});
		if(storeMealCheckoutRecord != null){
			return storeMealCheckoutRecord;
		}
		return new StoreMealCheckoutRecord();
	}
    
    /**
     * 根据订单列表查询出餐记录
     * @param merchantId
     * @param storeId
     * @param orderIds
     * @return Map<orderId_portId, StoreMealCheckoutRecord>
     * @throws T5weiException
     */
    public Map<String, StoreMealCheckoutRecord> getStoreMealCheckoutRecord(int merchantId, long storeId, long portId, List<String> orderIds, boolean enableSlave) {
    	Map<String, StoreMealCheckoutRecord> resultMap = new HashMap<String, StoreMealCheckoutRecord>();
		if (orderIds == null || orderIds.isEmpty()) {
			return resultMap;
		}
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where merchant_id=? and store_id=? ");
        params.add(merchantId);
        params.add(storeId);
		if (portId > 0) {
			sql.append(" and port_id in (?,0) ");
			params.add(portId);
		}
        String orderInSql = Query.createInSql("order_id", orderIds.size());
        sql.append(" and ").append(orderInSql);
        params.addAll(orderIds);
        if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreMealCheckoutRecord> list = this.query.list(StoreMealCheckoutRecord.class, sql.toString(), params.toArray(new Object[params.size()]));
        if (list != null && !list.isEmpty()) {
			for (StoreMealCheckoutRecord record : list) {
				resultMap.put(record.getOrderId(), record);
			}
		}
        return resultMap;
    }

	public int updateStoreMealNotifyTime(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        String sql = "update " + this.getRealName(merchantId, storeId) + " set notify_time=? where merchant_id=? and store_id=? and order_id=? ";
        Object[] args = new Object[]{System.currentTimeMillis(), merchantId, storeId, orderId};
        JdbcTemplate jdbcTemplate = this.query.getJdbcSupport().getJdbcTemplate();
        int num = jdbcTemplate.update(sql, args);
        return num;
    }
	
	public List<StoreMealCheckoutRecord> getStoreMealCheckoutRecord(StoreMealHistoryQueryParam storeMealHistoryQueryParam, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
    	int merchantId = storeMealHistoryQueryParam.getMerchantId();
    	long storeId = storeMealHistoryQueryParam.getStoreId();
        long repastDate = storeMealHistoryQueryParam.getRepastDate();
        long portId = storeMealHistoryQueryParam.getPortId();
        int startSerialNumber = storeMealHistoryQueryParam.getStartSerialNumber();
        int size = storeMealHistoryQueryParam.getSize();
        repastDate = DateUtil.getBeginTime(repastDate, null);
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer(" where merchant_id=? and store_id=? and repast_date=? ");
        params.add(merchantId);
        params.add(storeId);
        params.add(repastDate);
		if (portId > 0) {
			sql.append(" and port_id in (0,?) ");
			params.add(portId);
		}
		if (startSerialNumber > 0) {
			sql.append(" and take_serial_number<=? ");
			params.add(startSerialNumber);
		}
		sql.append(" order by take_serial_number desc");
		if (size > 0) {
			sql.append(" limit 0,? ");
			params.add(size);
		}
		DALStatus.setSlaveMode();
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealCheckoutRecord.class, sql.toString(), params.toArray(new Object[params.size()]));
    }
    
    public List<Map<String,Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        sql = sql.toLowerCase();
        return this.query.getJdbcSupport().getMapList(sql, params);
    }
    
}
