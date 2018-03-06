package com.huofu.module.i5wei.mealport.facade;

import com.huofu.module.i5wei.base.SnsPublish;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.eventtype.EventType;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealTaskDAO;
import huofucore.facade.i5wei.mealport.StoreAppTaskRelationParam;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.mealport.StoreMealPortFacade;
import huofucore.facade.i5wei.mealport.StoreMealPortParam;
import huofucore.facade.i5wei.mealport.StoreMealPortRelationParam;
import huofucore.facade.i5wei.mealport.StoreMealPortTaskStatusEnum;
import huofucore.facade.i5wei.mealport.StoreMealTaskDTO;
import huofucore.facade.i5wei.mealport.StoreMealTaskLogDTO;
import huofucore.facade.notify.*;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.sqs.SNSHelper;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.heartbeat.service.StoreHeartbeatService;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealTask;
import com.huofu.module.i5wei.mealport.entity.StoreMealTaskLog;
import com.huofu.module.i5wei.mealport.service.StoreMealPortService;

@ThriftServlet(name = "storeMealPortFacadeServlet", serviceClass = StoreMealPortFacade.class)
@Component
public class StoreMealPortFacadeImpl implements StoreMealPortFacade.Iface {

    private static final Log log = LogFactory.getLog(StoreMealPortFacadeImpl.class);

    @Autowired
    private StoreMealPortService storeMealPortService;

    @Autowired
    private StoreHeartbeatService storeHeartbeatService;

    @Autowired
    private StoreMealPortFacadeUtil storeMealPortFacadeUtil;

    @ThriftClient
    private NoticeFacade.Iface noticeFacade;

    @Autowired
    private StoreMealTaskDAO storeMealTaskDAO;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private SnsPublish snsPublish;

    @Override
    public StoreMealPortDTO getStoreMealPort(int merchantId, long storeId, long portId, boolean loadPeripheral) throws TException {
        StoreMealPort storeMealPort = this.storeMealPortService.loadStoreMealPort(merchantId, storeId, portId);
        return this.storeMealPortFacadeUtil.buildStoreMealPortDTO(storeMealPort, loadPeripheral);
    }

    @Override
    public boolean deleteStoreMealPort(int merchantId, long storeId, long portId) throws TException {
    	boolean result = this.storeMealPortService.deleteStoreMealPort(merchantId, storeId, portId);
    	storeHeartbeatService.updatePortLastUpdateTime(merchantId, storeId, System.currentTimeMillis());
        this.publishMealPort(merchantId,storeId,portId,EventType.MEAL_PORT_DELETE);
        return result;
    }

    @Override
    public StoreMealPortDTO saveStoreMealPort(StoreMealPortParam param) throws TException {
        StoreMealPort storeMealPort = storeMealPortService.saveStoreMealPort(param);
        storeHeartbeatService.updatePortLastUpdateTime(param.getMerchantId(), param.getStoreId(), System.currentTimeMillis());
        this.publishMealPort(param.getMerchantId(),param.getStoreId(),param.getPortId(),EventType.MEAL_PORT_UPDATE);
        return this.storeMealPortFacadeUtil.buildStoreMealPortDTO(storeMealPort, param.isLoadPeripheral());
    }

    @Override
    public List<StoreMealPortDTO> getStoreMealPortsIdle(int merchantId, long storeId, long appcopyId, boolean loadPeripheral) throws TException {
        List<StoreMealPort> idlePorts = storeMealPortService.getStoreMealPortsIdle(merchantId, storeId, true);
        if (idlePorts == null || idlePorts.isEmpty()) {
            storeHeartbeatService.changePortIdle(merchantId, storeId, false);
        } else {
            storeHeartbeatService.changePortIdle(merchantId, storeId, true);
        }
        return this.storeMealPortFacadeUtil.buildStoreMealPortDTOs(merchantId, idlePorts, loadPeripheral);
    }

    @Override
    public List<StoreMealPortDTO> registStoreAppTaskRelation(StoreAppTaskRelationParam storeAppTaskRelationParam) throws TException {
        int merchantId = storeAppTaskRelationParam.getMerchantId();
        long storeId = storeAppTaskRelationParam.getStoreId();
        long appcopyId = storeAppTaskRelationParam.getAppcopyId();
        List<StoreMealTask> updateStoreMealTasks = storeMealPortService.registStoreAppTaskRelation(storeAppTaskRelationParam);
        //打印机报警恢复
        this.batchUpdateNotice(merchantId,storeId,updateStoreMealTasks);
        List<StoreMealPort> idlePorts = storeMealPortService.getStoreMealPortsIdle(merchantId, storeId, false);
        boolean hasIdlePorts = true;
        if (idlePorts == null || idlePorts.isEmpty()) {
        	hasIdlePorts = false;
        } else {
        	hasIdlePorts = true;
        }
        storeHeartbeatService.changePortIdle(merchantId, storeId, hasIdlePorts);
        if (updateStoreMealTasks != null && !updateStoreMealTasks.isEmpty()) {
        	storeHeartbeatService.updatePortLastUpdateTime(merchantId, storeId, System.currentTimeMillis());
		}
        List<Long> registPortIds = new ArrayList<Long>();
        for (StoreMealPortRelationParam portRelationParam : storeAppTaskRelationParam.getMealPortRelations()) {
            long portId = portRelationParam.getPortId();
            int taskStatus = portRelationParam.getTaskStatus();
            if (taskStatus == StoreMealPortTaskStatusEnum.ON.getValue()) {
                registPortIds.add(portId);
            }
        }
        List<StoreMealPort> storeMealPorts = storeMealPortService.getStoreAppTaskRegistMealPorts(merchantId, storeId, appcopyId, registPortIds);
        return this.storeMealPortFacadeUtil.buildStoreMealPortDTOs(merchantId, storeMealPorts, storeAppTaskRelationParam.isLoadPeripheral());
    }

    private void batchUpdateNotice(int merchantId,long storeId,List<StoreMealTask> updateStoreMealTasks){
        List<NoticeUpdateParam> noticeUpdateParams = new ArrayList<>();
        for(StoreMealTask storeMealTask : updateStoreMealTasks){
            NoticeUpdateParam param = new NoticeUpdateParam();
            param.setMerchantId(merchantId);
            param.setStoreId(storeId);
            param.setNoticeTypeId(NoticeTypeEnum.PRINTER_ALARM.getValue());
            param.setNoticeStatus(NoticeStatusEnum.PROCESSED.getValue());
            param.setReplaceParam(String.valueOf(storeMealTask.getPortId()));
            noticeUpdateParams.add(param);
        }
        try {
            noticeFacade.batchUpdateNoticeStatus(noticeUpdateParams);
        } catch (TException e) {
            log.error("autoCheckOut merchantId[" + merchantId + "],storeId[" + storeId + "],noticeUpdateParams[" + JsonUtil.build(noticeUpdateParams) + "],notice batch update notice failed");
        }
    }

    @Override
    public List<StoreMealPortDTO> getStoreMealPorts(int merchantId, long storeId, boolean loadPeripheral) throws TException {
        List<StoreMealPort> storeMealPorts = storeMealPortService.getStoreMealPorts(merchantId, storeId, true);
        return this.storeMealPortFacadeUtil.buildStoreMealPortDTOs(merchantId, storeMealPorts, loadPeripheral);
    }

    @Override
    public List<StoreMealPortDTO> getStoreMealPortsByPortIds(int merchantId, long storeId, List<Long> portIds, boolean loadPeripheral) throws TException {
        List<StoreMealPort> storeMealPorts = storeMealPortDAO.getByIds(merchantId, storeId, portIds, true);
        return this.storeMealPortFacadeUtil.buildStoreMealPortDTOs(merchantId, storeMealPorts, loadPeripheral);
    }

    @Override
    public List<StoreMealPortDTO> getStoreAppTaskMealPorts(int merchantId, long storeId, long appcopyId, boolean loadPeripheral) throws TException {
        List<StoreMealPort> storeMealPorts = storeMealPortService.getStoreAppTaskMealPorts(merchantId, storeId, appcopyId);
        return this.storeMealPortFacadeUtil.buildStoreMealPortDTOs(merchantId, storeMealPorts, loadPeripheral);
    }

    @Override
    public void clearPeripheral(int merchantId, long storeId, long peripheralId) throws TException {
        this.storeMealPortService.clearPeripheral(merchantId, storeId, peripheralId);
    }

	@Override
	public List<StoreMealTaskDTO> getStoreAppTaskRelations(int merchantId, long storeId) throws TException {
		List<StoreMealTask> storeMealTasks = storeMealPortService.getStoreMealTasks(merchantId, storeId);
		if (storeMealTasks == null || storeMealTasks.isEmpty()) {
			return new ArrayList<StoreMealTaskDTO>();
		}
		return BeanUtil.copyList(storeMealTasks, StoreMealTaskDTO.class);
	}

    @Override
    public List<StoreMealTaskDTO> getStoreAppTaskRelationsByPortIds(int merchantId, long storeId, List<Long> portIds) throws TException {
        List<StoreMealTask> storeMealTasks = storeMealTaskDAO.getByIds(merchantId,storeId,portIds,true);
        return BeanUtil.copyList(storeMealTasks, StoreMealTaskDTO.class);
    }

    @Override
	public List<StoreMealTaskLogDTO> getStoreMealTaskLogs(int merchantId, long storeId, int size) throws TException {
		if (merchantId == 0 || storeId == 0) {
			return new ArrayList<StoreMealTaskLogDTO>();
		}
		if (size == 0) {
			size = 10;// 默认返回10条
		}
		List<StoreMealTaskLog> storeMealTaskLogs = storeMealPortService.getTaskLogs(merchantId, storeId, size);
		if (storeMealTaskLogs == null || storeMealTaskLogs.isEmpty()) {
			return new ArrayList<StoreMealTaskLogDTO>();
		}
		return BeanUtil.copyList(storeMealTaskLogs, StoreMealTaskLogDTO.class);
	}

    public void publishMealPort(int merchantId, long storeId, long portId,int eventType){
        //SNS发布事件
        Map<String,Object> dataMap = new HashMap<String,Object>();
        dataMap.put("merchantId",merchantId);
        dataMap.put("storeId",storeId);
        dataMap.put("portId",portId);
        dataMap.put("printAlarmSource",PrinterAlarmSourceEnum.AUTO_CHECK_OUT.getValue());
        String storeTableSettingTopicArn = SysConfig.getStoreMealPortTopicArn();
        snsPublish.publish(dataMap,eventType,storeTableSettingTopicArn);
    }

}
