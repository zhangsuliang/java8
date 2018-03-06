package com.huofu.module.i5wei.table.dao;

import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.table.StoreAreaSaveParam;
import huofuhelper.util.AbsQueryDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.table.entity.StoreArea;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreAreaDAO extends AbsQueryDAO<StoreArea> {

    private final static Log log = LogFactory.getLog(StoreAreaDAO.class);

    /**
     * 根据区域ID查询店铺区域（包括删除过的区域和有效的区域）
     */
    public StoreArea getById(long areaId, boolean enableSlave){
    	if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        StoreArea storeArea =this.query.obj(StoreArea.class,"where area_id=?",new Object[]{areaId});
        return storeArea;
    }
    
    /**
     * 根据区域ID查询有效区域
     */
    public StoreArea loadValidStoreAreaById(long areaId, boolean enableSlave) throws T5weiException {
        StoreArea storeArea =this.query.obj(StoreArea.class,"where area_id=? and deleted=?",new Object[]{areaId,false});
        if(storeArea == null){
            throw new T5weiException(T5weiErrorCodeType.STORE_AREA_ID_INVALID.getValue(),"areaId[" + areaId + "] is invalid!");
        }
        return storeArea;
    }

    /**
     * 根据条件查询有效的区域列表
     */
    public List<StoreArea> getValidStoreAreas(long storeId, boolean enableSlave){
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        return this.query.list(StoreArea.class,"where store_id=? and deleted=? order by sort_no,create_time asc",new Object[]{storeId,false});
    }

    /**
     * 根据条件查询区域列表（包含删除过的和未删除的）
     */
    public List<StoreArea> getStoreAreas(long storeId, boolean enableSlave){
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        return this.query.list(StoreArea.class,"where store_id=? order by sort_no,create_time asc",new Object[]{storeId});
    }
    
    public List<StoreArea> getStoreAreasOrderByUpdateTime(long storeId, boolean enableSlave){
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        return this.query.list(StoreArea.class,"where store_id=? order by update_time desc",new Object[]{storeId});
    }

    /**
     * 店铺区域名称重复校验
     */
    public void checkValidStoreAreaNameDuplicate(StoreAreaSaveParam param) throws T5weiException {
        int count  = this.query.count(StoreArea.class,"where store_id=? and area_name=? and deleted=?",new Object[]{param.getStoreId(),param.getAreaName(),false});
        if(count > 0){
            throw new T5weiException(T5weiErrorCodeType.STORE_AREA_NAME_DUPLICATE.getValue(),"storeId[" + param.getStoreId() + "] ,merchantId[" +
                  param.getMerchantId() + "],areaName[" + param.getAreaName() + "] is duplicate");
        }
    }

    /**
     * 根据区域名称获取区域
     */
    public StoreArea getStoreAreaByName(String areaName,long storeId, boolean enableSlave){
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        StoreArea storeArea = this.query.obj(StoreArea.class,"where area_name = ? and store_id = ? and deleted = ?",new Object[]{areaName,storeId,true});
        return storeArea;
    }
    
    /**
     * 根据区域ID获取区域列表
     */
    public List<StoreArea> getStoreAreaByIds(int merchantId,long storeId, List<Long> areaIds, boolean enableSlave){
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        List<StoreArea> storeAreas = this.query.listInValues(StoreArea.class, "where store_id=? ", "area_id", new Object[]{storeId}, areaIds.toArray());
        return storeAreas;
    }
    
    /**
     * 根据区域ID列表获取区域列表
     */
    public Map<Long,StoreArea> getStoreAreaMapByIds(int merchantId,long storeId, List<Long> areaIds, boolean enableSlave){
    	Map<Long,StoreArea> resultMap = new HashMap<Long,StoreArea>();
        List<StoreArea> storeAreas = this.getStoreAreaByIds(merchantId, storeId, areaIds, enableSlave);
		if (storeAreas == null || storeAreas.isEmpty()){
			return resultMap;
        }
		for (StoreArea storeArea : storeAreas) {
			resultMap.put(storeArea.getAreaId(), storeArea);
		}
        return resultMap;
    }

    /**
     * 根据storeId获取店铺下的区域Map
     * @param storeId
     * @return
     */
    public Map<Long,StoreArea> getStoreAreaMapByStoreId(long storeId, boolean enableSlave){
        Map<Long,StoreArea> resultMap = new HashMap<Long,StoreArea>();
        List<StoreArea> storeAreas = this.getStoreAreas(storeId, enableSlave);
        if (storeAreas == null || storeAreas.isEmpty()){
            return resultMap;
        }
        for (StoreArea storeArea : storeAreas) {
            resultMap.put(storeArea.getAreaId(), storeArea);
        }
        return resultMap;
    }

}
