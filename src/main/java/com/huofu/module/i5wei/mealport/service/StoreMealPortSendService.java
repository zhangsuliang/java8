package com.huofu.module.i5wei.mealport.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealportsend.StoreMealPortSendParam;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;

@Service
public class StoreMealPortSendService {
    private Log log = LogFactory.getLog(StoreMealPortSendService.class);

    @Autowired
    private StoreMealPortSendDAO storeMealPortSendDAO;
    
    @Autowired
    private StoreMealPortService storeMealPortService;
    
    @Autowired
    private StoreHeartbeatService storeHeartbeatService;
    
    @Autowired
    private StoreMealTakeupDAO storeMealTakeupDAO;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreMealPortSend saveStoreMealPortSend(StoreMealPortSendParam param) throws T5weiException {
        long currentTime = System.currentTimeMillis();
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long sendPortId = param.getSendPortId();
        StoreMealPortSend storeMealPortSend = null;
        if(this.storeMealPortSendDAO.getStoreMealPortSendByName(merchantId, storeId, sendPortId, param.getSendPortName(), false) != null){
            log.error(DataUtil.infoWithParams("传菜口名称重复 merchantId=#1,storeId=#2,sendPortId=#3,sendPortName=#4", new Object[]{merchantId,storeId,sendPortId,param.getSendPortName()}));
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_DUPLICATE_NAME.getValue(), "store_meal_send_port name is duplicate");
        }
        if(param.getSendPortId() > 0){//修改
            storeMealPortSend = this.getStoreMealPortSendById(merchantId, storeId, sendPortId, true, false);
            storeMealPortSend.snapshot();
            int sweepType = storeMealPortSend.getSweepType();
            BeanUtil.copy(param, storeMealPortSend, true);
            if(storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue()){
                if(!this.checkStoreMealPort(merchantId, storeId, storeMealPortSend.getStoreMealPorts(), param.getStoreMealPortIds())){//验证加工档口上时候有未划完的菜
                    log.error(DataUtil.infoWithParams("加工档口上时候有未划完的菜，请划完后进行修改 param=#1", new Object[]{param}));
                    throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_NO_SWEEP.getValue(), "port meal is unsweep , don't update send port");
                }
                //解除加工档口上的传菜口
                this.storeMealPortService.unSetStoreMealPortSend(merchantId, storeId, sendPortId);
                //传菜口上边关联的加工档口
                this.storeMealPortService.setStoreMealPortSend(merchantId, storeId, sendPortId, param.getStoreMealPortIds());
            }
            if(sweepType != storeMealPortSend.getSweepType()){
                this.storeHeartbeatService.updateSweepLastUpdateTime(merchantId, storeId, System.currentTimeMillis(), true, null);
            }
            storeMealPortSend.setUpdateTime(currentTime);
            storeMealPortSend.update();
        } else {//添加
            storeMealPortSend = new StoreMealPortSend();
            BeanUtil.copy(param, storeMealPortSend, true);
            storeMealPortSend.setCreateTime(currentTime);
            storeMealPortSend.setUpdateTime(currentTime);
            if(storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue()){
                int storeMealPortSendSize = this.storeMealPortSendDAO.countStoreMealPortSend(merchantId, storeId, storeMealPortSend.getSendPortType(), false);
                //需要设置主传菜口
                storeMealPortSend.setMasterSendPort(storeMealPortSendSize > 0 ? false : true);//店铺中不存在传菜口，则该传菜间为主传菜口
                this.storeMealPortSendDAO.create(storeMealPortSend);
                //传菜口上边关联的加工档口
                this.storeMealPortService.setStoreMealPortSend(merchantId, storeId, storeMealPortSend.getSendPortId(), param.getStoreMealPortIds());
            } else {
                this.storeMealPortSendDAO.create(storeMealPortSend);
            }
        }
        return storeMealPortSend;
    }

    public StoreMealPortSend getStoreMealPortSendById(int merchantId, long storeId, long mealSendId, boolean loadStoreMealPort, boolean enableSlave) throws T5weiException {
        StoreMealPortSend storeMealPortSend = this.storeMealPortSendDAO.getStoreMealPortSendById(merchantId, storeId, mealSendId, enableSlave);
        if(storeMealPortSend == null){
            log.error(DataUtil.infoWithParams("传菜口不存在 merchantId=#1,storeId=#2,sendPortId=#3", new Object[]{merchantId,storeId,mealSendId}));
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_NO.getValue(), "store_meal_send_port is no");
        }
        if(loadStoreMealPort && storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue()){
            List<StoreMealPort> storeMealPorts = this.storeMealPortService.getStoreMealPortsBySendPortId(merchantId, storeId, mealSendId, enableSlave);
            storeMealPortSend.setStoreMealPorts(storeMealPorts);
        }
        if(storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue() || storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue()){
            storeMealPortSend.setPrinterPeripheralId(this.storeMealPortSendDAO.getStoreMealPortSendMaster(merchantId, storeId, true, enableSlave));
        }
        return storeMealPortSend;
    }

    public List<StoreMealPortSend> getStoreMealPortSends(int merchantId, long storeId, int sendPortType, boolean loadStoreMealPort, boolean enableSlave) throws T5weiException {
        List<StoreMealPortSend> storeMealPortSends = this.storeMealPortSendDAO.getStoreMealPortSends(merchantId, storeId, sendPortType, enableSlave);
        if (loadStoreMealPort && sendPortType == StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue()) {
            List<StoreMealPort> storeMealPorts = this.storeMealPortService.getStoreMealPorts(merchantId, storeId, false);
            for (StoreMealPortSend storeMealPortSend : storeMealPortSends) {
                List<StoreMealPort> mealPorts = new ArrayList<>();
                for (StoreMealPort storeMealPort : storeMealPorts) {
                    if(storeMealPort.getSendPortId() == storeMealPortSend.getSendPortId()){
                        mealPorts.add(storeMealPort);
                        continue;
                    }
                }
                storeMealPortSend.setStoreMealPorts(mealPorts);
            }
        }
        if(sendPortType == StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue() || sendPortType == StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue()){
            StoreMealPortSend storeMealPortSendMaster = this.storeMealPortSendDAO.getStoreMealPortSendMaster(merchantId, storeId, true, enableSlave);
            for (StoreMealPortSend storeMealPortSend : storeMealPortSends) {
                storeMealPortSend.setPrinterPeripheralId(storeMealPortSendMaster);
            }
        }
        return storeMealPortSends;
    }

    public StoreMealPortSend getStoreMealPortSendPackageOrDelivery(int merchantId, long storeId, int sendPortType, boolean enableSlave) throws T5weiException {
        StoreMealPortSend storeMealPortSendMaster = this.storeMealPortSendDAO.getStoreMealPortSendMaster(merchantId, storeId, true, enableSlave);
        StoreMealPortSend storeMealPortSend = null;
        if(sendPortType == StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue()){
            storeMealPortSend = storeMealPortSendDAO.getStoreMealPortSend(merchantId,storeId, StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue(),true);
            storeMealPortSend.setPrinterPeripheralId(storeMealPortSendMaster);
        }
        if(sendPortType == StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue()){
            storeMealPortSend = storeMealPortSendDAO.getStoreMealPortSend(merchantId,storeId, StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue(),true);
            storeMealPortSend.setPrinterPeripheralId(storeMealPortSendMaster);
        }
        if(storeMealPortSend == null){
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_INVALID.getValue(),"打包台或者外卖台不存在");
        }else{
            return storeMealPortSend;
        }
    }

    
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public int deleteStoreMealPortSend(int merchantId, long storeId, long mealSendId) throws T5weiException {
        long currentTime = System.currentTimeMillis();
        boolean enableSlave = false;
        StoreMealPortSend storeMealPortSend = this.storeMealPortSendDAO.getStoreMealPortSendById(merchantId, storeId, mealSendId, enableSlave);
        if(storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue()){
            log.error(DataUtil.infoWithParams("打包台不能被删除merchantId=#1,storeId,=#2,mealSendId=#3", new Object[]{merchantId,storeId,mealSendId}));
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_SEND_PACKAGED_NO_DELETED.getValue(), "store meal port send packaged no deleted");
        }
        if(storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue()){
            log.error(DataUtil.infoWithParams("外卖台不能被删除merchantId=#1,storeId,=#2,mealSendId=#3", new Object[]{merchantId,storeId,mealSendId}));
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_SEND_DELIVERY_NO_DELETED.getValue(), "store meal port send delivery no deleted");
        }
        boolean storeMealAllSweep = this.storeMealTakeupDAO.isStoreMealPortSendAllSweep(merchantId, storeId, mealSendId, enableSlave);
        if(!storeMealAllSweep){
            log.error(DataUtil.infoWithParams("该传菜间有未划完的菜，不能删除该传菜间merchantId=#1,storeId,=#2,mealSendId=#3", new Object[]{merchantId,storeId,mealSendId}));
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_SEND_NO_SWEPT_NO_DELETED.getValue(), "store meal port send no swept no deleted");
        }
        if(storeMealPortSend.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue()){//传菜口
            List<StoreMealPortSend> storeMealPortSends = this.storeMealPortSendDAO.getStoreMealPortSends(merchantId, storeId, storeMealPortSend.getSendPortType(), enableSlave);
            if(storeMealPortSends.size() <= 1){
                log.error(DataUtil.infoWithParams("传菜口不予许全部删除 merchantId=#1,storeId=#2,sendPortId=#3", new Object[]{merchantId,storeId,mealSendId}));
                throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_NO_DELETED.getValue(), "store_meal_send_port no deleted");
            }
            //解绑传菜口关联的加工档口
            this.storeMealPortService.unSetStoreMealPortSend(merchantId, storeId, mealSendId);

            if(storeMealPortSends.get(0).getSendPortId() == mealSendId){
                storeMealPortSends.get(1).setMasterSendPort(true);
                storeMealPortSends.get(1).setUpdateTime(currentTime);
                storeMealPortSends.get(1).update();
            }
        }
        return this.storeMealPortSendDAO.deleteStoreMealPortSend(merchantId, storeId, mealSendId);
    }

    public void initStoreMealPortSendDTO(int merchantId, long storeId) throws T5weiException, TException {
        //新创建的店铺和（老数据）普通模式切换成高级模式时需要添加打包台和外卖台
        boolean enableSlave = false;
        List<StoreMealPortSend> mealPortSends = this.getStoreMealPortSends(merchantId, storeId, StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue(), false, enableSlave);
        StoreMealPortSendParam mealPortSendParam = new StoreMealPortSendParam();
        mealPortSendParam.setMerchantId(merchantId);
        mealPortSendParam.setStoreId(storeId);
        //打包台
        List<StoreMealPortSend> mealPortSendPackageds = this.getStoreMealPortSends(merchantId, storeId, StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue(), false, enableSlave);
        if(mealPortSendPackageds == null || mealPortSendPackageds.isEmpty()){
            mealPortSendParam.setSendPortName("打包台");
            mealPortSendParam.setSendPortType(StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue());
            this.saveStoreMealPortSend(mealPortSendParam);
        }
        //外卖台
        List<StoreMealPortSend> mealPortSendDeliverys = this.getStoreMealPortSends(merchantId, storeId, StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue(), false, enableSlave);
        if(mealPortSendDeliverys == null || mealPortSendDeliverys.isEmpty()){
            mealPortSendParam.setSendPortName("外卖台");
            mealPortSendParam.setSendPortType(StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue());
            this.saveStoreMealPortSend(mealPortSendParam);
        }
        if(mealPortSends == null || mealPortSends.isEmpty()){
            mealPortSendParam.setSendPortName("主传菜口");
            mealPortSendParam.setSendPortType(StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue());
            this.saveStoreMealPortSend(mealPortSendParam);
        }
    }
    
    private boolean checkStoreMealPort(int merchantId, long storeId, List<StoreMealPort> storeMealPorts, List<Long> storeMealPortIds){
        Set<Long> mealPortIdSet = new HashSet<Long>();
        for(StoreMealPort storeMealPort : storeMealPorts){
            mealPortIdSet.add(storeMealPort.getPortId());
        }
        //判断传菜口上的加工档口是否修改
        Set<Long> difftStoreMealPortIds = new HashSet<Long>();
        Set<Long> storeMealPortIdSet = new HashSet<Long>(storeMealPortIds);
        for (Long mealPortId : storeMealPortIdSet) {
            if(!mealPortIdSet.contains(mealPortId)){
                difftStoreMealPortIds.add(mealPortId);
            }
        }
        for (Long mealPortId : mealPortIdSet) {
            if(!storeMealPortIdSet.contains(mealPortId)){
                difftStoreMealPortIds.add(mealPortId);
            }
        }
        //验证加工档口上是否有未划完的菜
        return this.storeMealTakeupDAO.isStoreMealPortsAllSweep(merchantId, storeId, new ArrayList<Long>(difftStoreMealPortIds), false);
    }
}
