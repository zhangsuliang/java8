package com.huofu.module.i5wei.table.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.table.dbrouter.StoreTableRecordDbRouter;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.table.StoreTableErrorCodeEnum;
import huofucore.facade.i5wei.table.TableRecordStatusEnum;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 桌台记录
 * @author licheng7
 * 2016年4月27日 上午9:31:47
 */
@Repository
public class StoreTableRecordDAO extends AbsQueryDAO<StoreTableRecord> {
	
	@Autowired
    private IdMakerUtil idMakerUtil;
	
	private static final String TABLE_RECORDID_ID = "tb_table_recordid_seq";

	private void addDbRouteInfo(int merchantId, long storeId) {
        StoreTableRecordDbRouter.addInfo(merchantId, storeId);
    }
	
	/**
	 * 根据桌台id查询所属桌台未清台的桌台记录列表
	 * @param merchantId
	 * @param storeId
	 * @param tableId
	 * @param forUpdate
	 * @return
	 */
	public List<StoreTableRecord> getStoreTableRecordListByTableIdSeqDesc (int merchantId, long storeId, long tableId, boolean forUpdate) {
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where store_id=? and table_id=? and table_record_status!=? order by table_record_seq desc";
		if (forUpdate) {
			sql = sql + " for update";
		}
		return this.query.list(StoreTableRecord.class, sql, new Object[]{storeId, tableId, TableRecordStatusEnum.CLEAR_TABLE.getValue()});
	}
	
	public List<StoreTableRecord> getStoreTableRecordList4HeartBeat (int merchantId, long storeId, long areaId, long tableRecordId) {
		this.addDbRouteInfo(merchantId, storeId);
		List<Object> params = new ArrayList<Object>();
		StringBuilder sql = new StringBuilder(" where store_id=? and table_record_status!=?");
		params.add(storeId);
		params.add(TableRecordStatusEnum.CLEAR_TABLE.getValue());
		if (areaId > 0) {
			sql.append(" and area_id=?");
			params.add(areaId);
		}
		if (tableRecordId > 0) {
			sql.append(" and table_record_id=?");
			params.add(tableRecordId);
		}
		sql.append(" order by update_time desc");
		return this.query.list(StoreTableRecord.class, sql.toString(), params.toArray());
	}
	
	public StoreTableRecord getLastClearTimeClearTableRecord (int merchantId, long storeId) {
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.obj(StoreTableRecord.class, " where store_id=? and table_record_status=? order by clear_table_time desc limit 1", new Object[]{storeId, TableRecordStatusEnum.CLEAR_TABLE.getValue()});
	}
	
	/**
	 * 根据桌台id查询所属桌台未清台的桌台记录列表
	 * @param merchantId
	 * @param storeId
	 * @param tableId
	 * @param forUpdate
	 * @return
	 */
	public List<StoreTableRecord> getStoreTableRecordListByTableIdSeqAsc (int merchantId, long storeId, long tableId, boolean forUpdate) {
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where store_id=? and table_id=? and table_record_status!=? order by table_record_seq";
		if (forUpdate) {
			sql = sql + " for update";
		}
		return this.query.list(StoreTableRecord.class, sql, new Object[]{storeId, tableId, TableRecordStatusEnum.CLEAR_TABLE.getValue()});
	}
	
	/**
	 * 根据tableId和seq查询出桌台记录
	 * @param merchantId
	 * @param storeId
	 * @param tableId
	 * @param seq
	 * @param forUpdate
	 * @return
	 */
	public StoreTableRecord getStoreTableRecordListByTableIdAndSeq (int merchantId, long storeId, long tableId, int seq, boolean forUpdate) {
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where store_id=? and table_id=? and table_record_seq=? and table_record_status!=? order by create_time desc limit 1";
		if (forUpdate) {
			sql = sql + " for update";
		}
		return this.query.obj(StoreTableRecord.class, sql, new Object[]{storeId, tableId, seq, TableRecordStatusEnum.CLEAR_TABLE.getValue()});
	}
	
	/**
	 * 根据id获取桌台记录
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordId
	 * @return
	 * @throws T5weiException 
	 */
	public StoreTableRecord getStoreTableRecordById (int merchantId, long storeId, long tableRecordId, boolean forUpdate) throws T5weiException {
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where store_id=? and table_record_id=?";
		if (forUpdate) {
			sql = sql + " for update";
		}
		StoreTableRecord storeTableRecord = this.query.obj(StoreTableRecord.class, sql, new Object[]{storeId, tableRecordId});
		if (storeTableRecord == null) {
			throw new T5weiException(StoreTableErrorCodeEnum.PARAMS_INVALID.getValue(), "tableRecordId["+tableRecordId+"] invalid");
		}
		return storeTableRecord;
	}
	
	/**
	 * 根据id查询桌台记录集合
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordIds
	 * @return
	 */
	public List<StoreTableRecord> getStoreTableRecordsByIds (int merchantId, long storeId, List<Long> tableRecordIds, boolean enableSlave) {
		if (enableSlave) {
            DALStatus.setSlaveMode();
        }
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.listInValues(StoreTableRecord.class, " where store_id=? ", " table_record_id ", new Object[]{storeId}, tableRecordIds.toArray());
	}
	
	/**
	 * 根据id查询桌台记录集合，以map形式返回
	 * @param merchantId
	 * @param storeId
	 * @param tableRecordIds
	 * @return
	 */
	public Map<Long,StoreTableRecord> getStoreTableRecordMapByIds (int merchantId, long storeId, List<Long> tableRecordIds, boolean enableSlave) {
		this.addDbRouteInfo(merchantId, storeId);
		Map<Long,StoreTableRecord> dataMap = new HashMap<Long,StoreTableRecord>();
		List<StoreTableRecord> dataList = this.getStoreTableRecordsByIds(merchantId, storeId, tableRecordIds, enableSlave);
		if (dataList == null || dataList.isEmpty()) {
			return dataMap;
		}
		for (StoreTableRecord data : dataList) {
			dataMap.put(data.getTableRecordId(), data);
		}
		return dataMap;
	}
	
	public List<StoreTableRecord> getStoreTableRecordByStoreId (int merchantId, long storeId, boolean enableSlave) {
		if (enableSlave) {
            DALStatus.setSlaveMode();
        }
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StoreTableRecord.class, " where store_id=? and table_record_status not in (?,?,?,?)", 
				new Object[]{storeId, TableRecordStatusEnum.CLEAR_TABLE.getValue(), TableRecordStatusEnum.SETTLEMENT.getValue(), TableRecordStatusEnum.SETTLING.getValue(), TableRecordStatusEnum.SETTLE_FAIL.getValue()});
	}
	
	/**
	 * 更新桌台记录
	 */
	public void update (StoreTableRecord storeTableRecord) {
		this.addDbRouteInfo(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId());
		super.update(storeTableRecord);
	}
	
	@Override
	public void update(StoreTableRecord storeTableRecord, StoreTableRecord snapshot) {
		this.addDbRouteInfo(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId());
		super.update(storeTableRecord);
    }
	
	public List<StoreTableRecord> getStoreTableRecordByName(int merchantId, long storeId, String tableName, boolean enableSlave){
	    if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreTableRecord.class, " where store_id = ? and table_name like ? ", new Object[]{storeId,"%"+tableName+"%"});
	}
	
	/**
	 * 创建桌台记录
	 */
	public void create (StoreTableRecord storeTableRecord) {
		this.addDbRouteInfo(storeTableRecord.getMerchantId(), storeTableRecord.getStoreId());
		long tableRecordId = this.nextId();
		storeTableRecord.setTableRecordId(tableRecordId);
		super.create(storeTableRecord);
	}
	
	private long nextId() {
		try {
        	return this.idMakerUtil.nextId2(TABLE_RECORDID_ID);
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }

	public String getRealName(int merchantId, long storeId) {
		this.addDbRouteInfo(merchantId, storeId);
		DALInfo dalInfo = Query.process(StoreTableRecord.class);
		return dalInfo.getRealTable(StoreTableRecord.class);
	}

	public List<Map<String,Object>> getResultMapList(int merchantId, long storeId, String sql, Object[] params, boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		sql = sql.toLowerCase();
		return this.query.getJdbcSupport().getMapList(sql, params);
	}

	public int getStoreTableRecordsCount(int merchantId, long storeId, long tableRecordId, long repastDate, long staffId, boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where merchant_id = ? and store_id = ? ";
		List<Object> params = Lists.newArrayList();
		params.add(merchantId);
		params.add(storeId);
		if (tableRecordId > 0) {
			sql += " and table_record_id = ? ";
			params.add(tableRecordId);
		}
		if (repastDate > 0 ){
			sql += " and repast_date = ? ";
			params.add(repastDate);
		}
		if (staffId > 0) {
			sql += " and staff_id = ? ";
			params.add(staffId);
		}
		return this.query.count(StoreTableRecord.class, sql, params.toArray());
	}

	public List<StoreTableRecord> getStoreTableRecords(int merchantId, long storeId, long tableRecordId, long repastDate, long staffId, int pageNo, int size, boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		String sql = " where merchant_id = ? and store_id = ? ";
		List<Object> params = Lists.newArrayList();
		params.add(merchantId);
		params.add(storeId);
		if (tableRecordId > 0) {
			sql += " and table_record_id = ? ";
			params.add(tableRecordId);
		}
		if (repastDate > 0 ){
			sql += " and repast_date = ? ";
			params.add(repastDate);
		}
		if (staffId > 0) {
			sql += " and staff_id = ? ";
			params.add(staffId);
		}
		sql += " order by create_time desc limit ?,? ";
		int startIndex = (pageNo - 1) * size;
		params.add(startIndex);
		params.add(size);
		return this.query.list(StoreTableRecord.class, sql, params.toArray());
	}
}
