package com.huofu.module.i5wei.mealport.dao;

import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealPortDAO extends AbsQueryDAO<StoreMealPort> {

    private static final String PORTID_KEY = "tb_store_meal_port_seq";

    @Autowired
    private IdMakerUtil idMakerUtil;

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public List<StoreMealPort> batchCreate(List<StoreMealPort> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        List<Long> ids = this.idMakerUtil.nextIds2(PORTID_KEY, list.size());
        int i = 0;
        for (StoreMealPort storeMealPort : list) {
            storeMealPort.setPortId(ids.get(i));
            i++;
        }
        return super.batchCreate(list);
    }

    @Override
    public void create(StoreMealPort storeMealPort) {
        storeMealPort.setPortId(this.idMakerUtil.nextId2(PORTID_KEY));
        this.addDbRouteInfo(storeMealPort.getMerchantId(), storeMealPort.getStoreId());
        super.create(storeMealPort);
    }

    @Override
    public void update(StoreMealPort storeMealPort) {
        this.addDbRouteInfo(storeMealPort.getMerchantId(), storeMealPort.getStoreId());
        super.update(storeMealPort);
    }

    @Override
    public void update(StoreMealPort storeMealPort, StoreMealPort snapshot) {
        this.addDbRouteInfo(storeMealPort.getMerchantId(), storeMealPort.getStoreId());
        super.update(storeMealPort, snapshot);
    }

    @Override
    public void delete(StoreMealPort storeMealPort) {
        this.addDbRouteInfo(storeMealPort.getMerchantId(), storeMealPort.getStoreId());
        super.delete(storeMealPort);
    }

    @Override
    public void replace(StoreMealPort storeMealPort) {
        this.addDbRouteInfo(storeMealPort.getMerchantId(), storeMealPort.getStoreId());
        super.replace(storeMealPort);
    }

    public StoreMealPort loadById(int merchantId, long storeId, long portId) throws T5weiException {
        StoreMealPort storeMealPort = this.getById(merchantId, storeId, portId);
        if (storeMealPort == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_INVALID.getValue(), "storeId[" + storeId + "] portId[" + portId + "] invalid");
        }
        return storeMealPort;
    }

    public StoreMealPort getById(int merchantId, long storeId, long portId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(StoreMealPort.class, portId);
    }

    public StoreMealPort getById(int merchantId, long storeId, long portId, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(StoreMealPort.class, portId, forUpdate);
    }

    public List<StoreMealPort> getList(int merchantId, long storeId, boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealPort.class,
                " where store_id=? and deleted=0 order by create_time asc",
                new Object[]{storeId});
    }
    
    public Map<Long,StoreMealPort> getMap(int merchantId, long storeId, boolean enableSlave){
    	Map<Long,StoreMealPort> map = new HashMap<Long,StoreMealPort>();
    	List<StoreMealPort> list = this.getList(merchantId, storeId, enableSlave);
		if (list == null || list.isEmpty()) {
			return map;
		}
		for (StoreMealPort port : list) {
			map.put(port.getPortId(), port);
		}
    	return map;
    }

    public int getCount(int merchantId, long storeId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreMealPort.class,
                " where store_id=? and deleted=0 order by create_time asc",
                new Object[]{storeId});
    }

    public StoreMealPort getParkagePort(int merchantId, long storeId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreMealPort> list = this.query.list(StoreMealPort.class,
                " where store_id=? and has_pack=? and deleted=0 order by create_time asc limit 1", new Object[]{storeId, true});
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public StoreMealPort getDeliveryPort(int merchantId, long storeId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreMealPort> list = this.query.list(StoreMealPort.class,
                " where store_id=? and has_delivery=? and deleted=0 order by create_time asc limit 1", new Object[]{storeId, true});
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    public List<StoreMealPort> getByIds(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        if (portIds == null || portIds.isEmpty()) {
            return new ArrayList<StoreMealPort>();
        }
        if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.listInValues2(StoreMealPort.class,
                " where store_id=? and deleted=0 ", "port_id", Lists.newArrayList(storeId), portIds);
    }

    public Map<Long,StoreMealPort> getMapByIds(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        if (portIds == null || portIds.isEmpty()) {
            return new HashMap<>();
        }
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreMealPort> storeMealPorts = this.query.listInValues2(StoreMealPort.class,
                " where store_id=? and deleted=0 ", "port_id", Lists.newArrayList(storeId), portIds);
        Map<Long,StoreMealPort> map = new HashMap<>();
        for(StoreMealPort storeMealPort : storeMealPorts){
            map.put(storeMealPort.getPortId(),storeMealPort);
        }
        return map;

    }

    public StoreMealPort getByLetter(int merchantId, long storeId, String letter, long exceptPortId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreMealPort.class,
                " where store_id=? and letter=? and deleted=0 and port_id!=? limit 1", new Object[]{storeId, letter, exceptPortId});
    }

    public StoreMealPort getByName(int merchantId, long storeId, String name, long exceptPortId, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreMealPort.class,
                " where store_id=? and name=? and deleted=0 and port_id!=? limit 1", new Object[]{storeId, name, exceptPortId});
    }

    public int updateForUnPack(int merchantId, long storeId, long exceptPortId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreMealPort.class, "set has_pack=? where " +
                "store_id=? and port_id!=? and deleted=0", new
                Object[]{false, storeId, exceptPortId});
    }

    public int updateDeliveryPack(int merchantId, long storeId, long exceptPortId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreMealPort.class, "set has_delivery=? where store_id=? and port_id!=? and deleted=0", new
                Object[]{false, storeId, exceptPortId});
    }

    public StoreMealPort getFirst(int merchantId, long storeId, boolean enableSlave) {
        List<StoreMealPort> list = this.getList(merchantId, storeId, enableSlave);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public int clearPeripheral(int merchantId, long storeId, long peripheralId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreMealPort.class, "set " +
                "printer_peripheral_id=0, call_peripheral_id=0 where " +
                "store_id=? and (printer_peripheral_id=? or " +
                "call_peripheral_id=?)", new Object[]{storeId, peripheralId, peripheralId});
    }

    public Map<Long, StoreMealPort> getMapInIds(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
    	if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.map2(StoreMealPort.class, "where store_id=?", "port_id", Lists.newArrayList(storeId), portIds);
    }

    public void batchUpdateAutoShift(List<StoreMealPort> storeMealPorts){
        if(storeMealPorts == null || storeMealPorts.isEmpty()){
            return;
        }
        this.addDbRouteInfo(storeMealPorts.get(0).getMerchantId(), storeMealPorts.get(0).getStoreId());
        List<Object[]> valueList = new ArrayList<>();
        for(StoreMealPort storeMealPort : storeMealPorts){
            valueList.add(new Object[]{storeMealPort.isAutoShift(),storeMealPort.getStoreId(),storeMealPort.getPortId()});
        }
        this.query.batchUpdate(StoreMealPort.class,"set auto_shift = ? where store_id=? and port_id=?",valueList);
    }
    
    //加工档口相关//////////////////////////////////////////////////////////
    /**
     * 
     * @param merchantId
     * @param storeId
     * @param sendPortId
     * @param enableSlave
     * @return
     */
    public List<StoreMealPort> getStoreMealPortsBySendPortId(int merchantId, long storeId, long sendPortId, boolean enableSlave){
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        
        List<Long> params = new ArrayList<Long>();
        params.add(storeId);
        params.add(sendPortId);

        return this.query.list2(StoreMealPort.class, "where store_id = ? and send_port_id = ? and deleted = 0 order by create_time asc", params);
    }

    /**
     * 修改加工档口上的传菜口
     * @param merchantId
     * @param storeId
     * @param storeMealPortSendId
     * @param storeMealPortIds
     */
    public void batchUpdateStoreMealPorts(int merchantId, long storeId, long storeMealPortSendId, List<Long> storeMealPortIds){
        long updateTime = System.currentTimeMillis();
        this.addDbRouteInfo(merchantId, storeId);
        List<Object[]> params = new ArrayList<Object[]>();
        for (Long storeMealPortId : storeMealPortIds) {
            params.add(new Object[]{storeMealPortSendId, updateTime, storeMealPortId});
        }
        this.query.batchUpdate(StoreMealPort.class, "set send_port_id = ? , update_time = ? where port_id = ?", params);
    }
    
    /**
     * 修改加工档口上的传菜口
     * @param merchantId
     * @param storeId
     * @param storeMealPortSendId
     * @param
     */
    public void updateStoreMealPorts(int merchantId, long storeId, long storeMealPortSendId){
        long updateTime = System.currentTimeMillis();
        this.addDbRouteInfo(merchantId, storeId);
        this.query.update(StoreMealPort.class, "set send_port_id = ? , update_time = ? where send_port_id = ?", new Object[]{0,updateTime,storeMealPortSendId});
    }
}
