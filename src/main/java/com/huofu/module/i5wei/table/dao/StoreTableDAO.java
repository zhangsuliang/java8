package com.huofu.module.i5wei.table.dao;

import halo.query.Query;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.table.dbrouter.StoreTableDbRouter;
import com.huofu.module.i5wei.table.entity.StoreTable;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreTableDAO extends AbsQueryDAO<StoreTable> {

    private static final Log log = LogFactory.getLog(StoreTableDAO.class);

    private void addDbRouteInfo(int merchantId, long storeId) {
        StoreTableDbRouter.addInfo(merchantId, storeId);
    }
    
    private String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreTable.class);
        return dalInfo.getRealTable(StoreTable.class);
    }

    /**
     * 查询店铺有效区域下的有效桌台列表
     */
    public List<StoreTable> getValidStoreAreaTables(int merchantId, long storeId, long areaId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreTable.class, "where store_id=? and area_id=? and deleted=? order by sort_no,create_time asc", new Object[]{storeId, areaId, false});
    }

    /**
     * 查询店铺区域下的所有桌台列表（可能是无效区域下的所有桌台列表，也可能是有效区域下的所有桌台列表）
     */
    public List<StoreTable> getStoreAreaTables(int merchantId, long storeId, long areaId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreTable.class, "where store_id=? and area_id=? order by sort_no,create_time asc", new Object[]{storeId, areaId});
    }
    
    public List<StoreTable> getStoreAreaTablesOrderByUpdateTime(int merchantId, long storeId, long areaId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreTable.class, "where store_id=? and area_id=? order by update_time desc", new Object[]{storeId, areaId});
    }
    
    public List<Map<String, Object>> getStoreTablesLastUpdateTimeGroupByAreaId (int merchantId, long storeId, boolean enableSlave) {
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        String sql = "select area_id, max(update_time) as max_update_time from "+this.getRealName(merchantId, storeId)+" where store_id=? group by area_id";
        return this.query.getJdbcSupport().getMapList(sql, new Object[]{storeId});
    }

    /**
     * 查询有效桌台
     */
    public StoreTable getValidStoreTableById(int merchantId, long storeId, long tableId, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        if (forUpdate) {
            return this.query.obj(StoreTable.class, "where table_id=? and deleted=? for update", new Object[]{tableId, false});
        } else {
            return this.query.obj(StoreTable.class, "where table_id=? and deleted=?", new Object[]{tableId, false});
        }
    }

    /**
     * 查询桌台（查询到的结果可能是未删除过的，也可能是删除过的）
     */
    public StoreTable getById(int merchantId, long storeId, long tableId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreTable.class, "where table_id=?", new Object[]{tableId});
    }

    /**
     * 统计店铺区域下总共有效的桌台数
     */
    public int countValidStoreTableNums(int merchantId, long storeId, long areaId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreTable.class, "where store_id=? and area_id=? and deleted=?", new Object[]{storeId, areaId, false});
    }

    /**
     * 查询site_number为最大的有效桌台,获取site_number
     */
    public int getValidMaxSiteNumber(int merchantId, long storeId, long areaId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreTable> storeTables = this.query.list(StoreTable.class, "where store_id=? and area_id=? and deleted=? order by site_number desc", new Object[]{storeId, areaId, false});
        if (storeTables != null && storeTables.size() != 0) {
            return storeTables.get(0).getSiteNumber();
        }
        return 0;
    }


    /**
     * 查询并获取最小的siteNumber
     */
    public int getMinSiteNumber(int merchantId, long storeId, long areaId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreTable> storeTables = this.query.list(StoreTable.class, "where store_id=? and area_id=? order by site_number asc", new Object[]{storeId, areaId});
        if (storeTables != null && storeTables.size() != 0) {
            return storeTables.get(0).getSiteNumber();
        }
        return 0;
    }

    public void checkValidTableNameDuplicate(int merchantId, long storeId, long areaId, String name) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        int count = this.query.count(StoreTable.class, "where store_id=? and area_id=? and name=? and deleted=?", new Object[]{storeId, areaId, name, false});
        if (count > 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TABLE_NAME_DUPLICATE.getValue(), "merchantId[" + merchantId + "] ,store_id[" +
                    storeId + "],areaId[" + areaId + "],tableName [" + name + "] is duplicate");
        }
    }

    /**
     * 查询店铺下所有的有效桌台列表
     */
    public List<StoreTable> getValidStoreTables(int merchantId, long storeId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreTable.class, "where store_id=? and deleted=? order by sort_no,create_time asc", new Object[]{storeId, false});
    }


    @Override
    public void update(StoreTable storeTable, StoreTable snapshot) {
        this.addDbRouteInfo(storeTable.getMerchantId(), storeTable.getStoreId());
        super.update(storeTable, snapshot);
    }

    @Override
    public void update(StoreTable storeTable) {
        this.addDbRouteInfo(storeTable.getMerchantId(), storeTable.getStoreId());
        super.update(storeTable);
    }

    public void create(StoreTable storeTable) {
        this.addDbRouteInfo(storeTable.getMerchantId(), storeTable.getStoreId());
        super.create(storeTable);
    }

    //根据siteNumber查询有效桌台
    public StoreTable getBySiteNumber(int merchantId, long storeId, long areaId, int siteNumber, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreTable.class, "where store_id=? and area_id=? and siteNumber=? and deleted=?", new Object[]{storeId, areaId, siteNumber, false});
    }

    public StoreTable getStoreTableByName(String tableName, long storeId, long areaId, int merchantId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StoreTable storeTable = this.query.obj(StoreTable.class, "where store_id=? and area_id =? and name=? and deleted =?", new Object[]{storeId, areaId, tableName, true});
        return storeTable;
    }

    public List<StoreTable> getStoreTablesBySiteNumber(int merchantId, long storeId, int siteNumber, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreTable> storeTables = this.query.list(StoreTable.class, "where store_id=? and site_number=? and deleted =? order by create_time asc", new Object[]{storeId, siteNumber, false});
        return storeTables;
    }

    /**
     * 根据ID查询店铺下的桌台列表
     */
    public List<StoreTable> getStoreTableByIds(int merchantId, long storeId, List<Long> tableIds, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreTable> storeTables = this.query.listInValues(StoreTable.class, " where store_id=? ", "table_id", new Object[]{storeId}, tableIds.toArray());
        return storeTables;
    }

    /**
     * 根据ID查询店铺下的桌台列表
     */
    public Map<Long, StoreTable> getStoreTableMapByIds(int merchantId, long storeId, List<Long> tableIds, boolean enableSlave) {
        Map<Long, StoreTable> resultMap = new HashMap<Long, StoreTable>();
        List<StoreTable> storeTables = this.getStoreTableByIds(merchantId, storeId, tableIds, enableSlave);
        if (storeTables == null || storeTables.isEmpty()) {
            return resultMap;
        }
        for (StoreTable storeTable : storeTables) {
            resultMap.put(storeTable.getTableId(), storeTable);
        }
        return resultMap;
    }

    public void dealStoreTable(int merchantId, long storeId, List<StoreTable> storeTables) {
        List<Object[]> params = Lists.newArrayList();
        for (StoreTable storeTable : storeTables) {
            params.add(new Object[]{storeTable.getUpdateTime(), storeTable.getTableId()});
        }
        if (!storeTables.isEmpty()) {
            this.addDbRouteInfo(merchantId, storeId);
            this.query.batchUpdate(StoreTable.class, "set staff_id = 0,update_time=? where table_id=?", params);
        }
    }

    public List<StoreTable> getDealStoreTables(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreTable.class, "where store_id = ? and staff_id > 0 and deleted = 0", new Object[]{storeId});
    }

}
