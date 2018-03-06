package com.huofu.module.i5wei.mealport.dao;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.FillEmptyUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;

import halo.query.dal.DALStatus;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreMealPortSendDAO extends AbsQueryDAO<StoreMealPortSend> {
    
    @Autowired
    private IdMakerUtil idMakerUtil;
    
    private static final String StoreMealPortSendID = "tb_store_meal_port_send_seq";
    
    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
    
    @Override
    public void create(StoreMealPortSend storeMealPortSend) {
        this.addDbRouteInfo(storeMealPortSend.getMerchantId(), storeMealPortSend.getStoreId());
        FillEmptyUtil.fill(storeMealPortSend);
        storeMealPortSend.setSendPortId(this.nextId());
        super.create(storeMealPortSend);
    }

    @Override
    public List<StoreMealPortSend> batchCreate(List<StoreMealPortSend> list){
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        List<Long> ids = this.idMakerUtil.nextIds2(StoreMealPortSendID, list.size());
        int i = 0;
        for (StoreMealPortSend storeMealPortSend : list) {
            storeMealPortSend.setSendPortId(ids.get(i));
            i++;
        }
        return super.batchCreate(list);
    }
    
    private long nextId() {
        try {
            return this.idMakerUtil.nextId2(StoreMealPortSendID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public StoreMealPortSend getStoreMealPortSendById(int merchantId, long storeId, long mealSendId, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(StoreMealPortSend.class, mealSendId);
    }

    /**
     * 高级模式下，根据传菜口类型获取打包台或者外卖台
     */
    public StoreMealPortSend getStoreMealPortSend(int merchantId, long storeId, int sendPortType, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreMealPortSend.class, "where store_id = ? and send_port_type = ?", new Object[]{storeId,sendPortType});
    }

    public StoreMealPortSend getStoreMealPortSendByName(int merchantId, long storeId, long sendPortId, String sendPortName, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreMealPortSend.class, "where store_id = ? and send_port_id != ? and send_port_name = ? limit 1", new Object[]{storeId, sendPortId, sendPortName});
    }

    public StoreMealPortSend getStoreMealPortSendMaster(int merchantId, long storeId, boolean masterSendPort, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.obj(StoreMealPortSend.class, "where store_id = ? and master_send_port = ? limit 1", new Object[]{storeId, masterSendPort});
    }

    public List<StoreMealPortSend> getStoreMealPortSends(int merchantId, long storeId, int sendPortType, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreMealPortSend.class, "where store_id = ? and send_port_type = ? order by create_time asc", new Object[]{storeId,sendPortType});
    }

    /**
     * 获取店铺主传菜口
     */
    public StoreMealPortSend getMasterStoreMealPortSend(int merchantId, long storeId, boolean enableSlave) throws T5weiException {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        StoreMealPortSend storeMealPortSend =  this.query.obj(StoreMealPortSend.class, "where store_id = ? and send_port_type = ? and master_send_port=?", new Object[]{storeId, StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue(),true});
        if(storeMealPortSend == null){
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_NO_MASTER.getValue(),"store_meal_send_port no master");
        }
        return storeMealPortSend;
    }

    public Map<Long,StoreMealPortSend> getStoreMealPortSendMap(int merchantId, long storeId, int sendPortType, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        List<StoreMealPortSend> storeMealPortSends = this.query.list(StoreMealPortSend.class, "where store_id = ? and send_port_type = ? order by create_time asc", new Object[]{storeId,sendPortType});
        Map<Long,StoreMealPortSend> map = new HashMap<Long,StoreMealPortSend>();
        for(StoreMealPortSend storeMealPortSend : storeMealPortSends){
            map.put(storeMealPortSend.getSendPortId(),storeMealPortSend);
        }
        return map;
    }

    public int countStoreMealPortSend(int merchantId, long storeId, int sendPortType, boolean enableSlave) {
        if(enableSlave){
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.count(StoreMealPortSend.class, "where store_id = ? and send_port_type = ?", new Object[]{storeId,sendPortType});
    }
    
    public int deleteStoreMealPortSend(int merchantId, long storeId, long mealSendId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.deleteById(StoreMealPortSend.class, new Object[]{mealSendId});
    }

}
