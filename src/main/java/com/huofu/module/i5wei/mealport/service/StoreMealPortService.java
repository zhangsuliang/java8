package com.huofu.module.i5wei.mealport.service;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.meal.StoreMealAutoPrintParam;
import huofucore.facade.i5wei.mealport.StoreAppTaskRelationParam;
import huofucore.facade.i5wei.mealport.StoreMealPortCheckoutTypeEnum;
import huofucore.facade.i5wei.mealport.StoreMealPortParam;
import huofucore.facade.i5wei.mealport.StoreMealPortRelationParam;
import huofucore.facade.i5wei.mealport.StoreMealPortTaskStatusEnum;
import huofucore.facade.i5wei.mealport.StorePortPrinterStatusEnum;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.meal.dao.StoreMealTakeupDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortPeripheralDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortSendDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealTaskDAO;
import com.huofu.module.i5wei.mealport.dao.StoreMealTaskLogDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortPeripheral;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.mealport.entity.StoreMealTask;
import com.huofu.module.i5wei.mealport.entity.StoreMealTaskLog;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.setting.service.StoreDefinedPrinterService;

@Service
public class StoreMealPortService {

    private static final Log log = LogFactory.getLog(StoreMealPortService.class);

    @Resource
    private StoreMealTakeupDAO storeMealTakeupDAO;

    @Resource
    private StoreMealPortDAO storeMealPortDAO;

    @Resource
    private StoreMealTaskDAO storeMealTaskDAO;

    @Resource
    private StoreChargeItemDAO storeChargeItemDAO;

    @Resource
    private StoreProductDAO storeProductDAO;

    @Resource
    private StoreMealTaskLogDAO storeMealTaskLogDAO;

    @Resource
    private StoreMealPortSendDAO storeMealPortSendDAO;

    @Resource
    private StoreDefinedPrinterService storeDefinedPrinterService;

    @Resource
    private Store5weiSettingService store5weiSettingService;

    @Resource
    private StoreMealPortPeripheralDAO storeMealPortPeripheralDAO;

    public StoreMealPort loadStoreMealPort(int merchantId, long storeId, long portId) throws T5weiException {
        StoreMealPort storeMealPort = this.storeMealPortDAO.loadById
                (merchantId, storeId, portId);
        StoreMealTask storeMealTask = this.storeMealTaskDAO.getById(merchantId,
                storeId, portId, false);
        storeMealPort.setStoreMealTask(storeMealTask);
        storeMealPort.setCallMealPortPeripherals(this.storeMealPortPeripheralDAO.getListByPortId(merchantId, storeId, portId));
        return storeMealPort;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public boolean deleteStoreMealPort(int merchantId, long storeId, long portId) throws T5weiException {
        boolean enableSlave = false;
        //只剩一个出餐口则不允许删除
        List<StoreMealPort> storeMealPorts = storeMealPortDAO.getList(merchantId, storeId, enableSlave);
        if (storeMealPorts.isEmpty()) {
            return false;
        }
        if (storeMealPorts.size() == 1) {
            return false;
        }

        //高级打印模式下加工档口上的菜必须划完
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId, enableSlave);
        if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
            if (!this.storeMealTakeupDAO.isStoreMealPortAllSweep(merchantId, storeId, portId, enableSlave)) {
                log.error(DataUtil.infoWithParams("store meal port not deleted , port no sweep, merchantId=#1,store=#2,portId=#3", new Object[]{merchantId, storeId, portId}));
                throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_NO_DELETED_NO_SWEEP.getValue(), " store meal port not deleted , port no sweep ");
            }
        }

        //解除产品与此出餐口任务关系
        this.storeProductDAO.clearPort(merchantId, storeId, portId);
        //解除收费项目与此出餐口任务关系
        this.storeChargeItemDAO.clearPort(merchantId, storeId, portId);
        //更改待出餐产品与此出餐口关系
        long newPortId = 0;
        for (StoreMealPort port : storeMealPorts) {
            if (port.getPortId() != portId) {
                newPortId = port.getPortId();
            }
        }
        storeMealTakeupDAO.updatePort(merchantId, storeId, portId, newPortId);
        //解除Pad与此出餐口任务关系
        StoreMealTask storeMealTask = storeMealTaskDAO.getById(merchantId, storeId, portId, true);
        if (storeMealTask != null) {
            storeMealTask.delete();
        }
        StoreMealPort storeMealPort = storeMealPortDAO.loadById(merchantId, storeId, portId);
        storeMealPort.makeDeleted();
        //高级模式下，删除自定义打印中的打印范围含有该加工档口
        storeDefinedPrinterService.filterStoreDefinedPrinter(merchantId, storeId, portId, PrintMsgTypeEnum.I5WEI_KITCHEN_MEAL_LIST.getValue());

        this.storeMealPortPeripheralDAO.deleteByPortId(merchantId, storeId, portId);
        return true;
    }


    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreMealPort saveStoreMealPort(StoreMealPortParam param) throws T5weiException {
        boolean enableSlave = false;
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long portId = param.getPortId();
        String name = param.getName();
        String letter = param.getLetter();
        if (storeMealPortDAO.getByName(merchantId, storeId, name, portId, enableSlave) != null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_NAME_DUPLICATE.getValue(), "storeId[" + storeId + "] portId[" + portId + "] name[" + param.getName() + "] duplicate");
        }
        if (storeMealPortDAO.getByLetter(merchantId, storeId, letter, portId, enableSlave) != null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_PORT_LETTER_DUPLICATE.getValue(), "storeId[" + storeId + "] portId[" + portId + "] letter[" + param.getLetter() + "] duplicate");
        }
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId, enableSlave);
        boolean autoShiftChanged = false;
        StoreMealPort storeMealPort;
        if (param.getPortId() == 0) {
            storeMealPort = new StoreMealPort();
            BeanUtil.copy(param, storeMealPort);
            storeMealPort.init4Create();
        } else {
            storeMealPort = this.storeMealPortDAO.loadById(merchantId, storeId, portId);
            if (param.isAutoShift() != storeMealPort.isAutoShift()) {
                autoShiftChanged = true;
            }
            storeMealPort.snapshot();
            BeanUtil.copy(param, storeMealPort);
            storeMealPort.setUpdateTime(System.currentTimeMillis());
        }
        if (portId == 0) {
            if (store5weiSetting.getPrintMode() == StorePrintModeEnum.ADVANCE_PRINT.getValue()) {
                StoreMealPortSend masterStoreMealPortSend = this.storeMealPortSendDAO.getStoreMealPortSendMaster(merchantId, storeId, true, enableSlave);
                storeMealPort.setSendPortId(masterStoreMealPortSend.getSendPortId());
            }
            storeMealPort.create();
            this.initPrinterStatus(merchantId, storeId, storeMealPort.getPortId());
        } else {
            storeMealPort.update();
            StoreMealPortCheckoutTypeEnum checkoutType;
            if (storeMealPort.isAutoShift()) {
                checkoutType = StoreMealPortCheckoutTypeEnum.AUTO;
            } else {
                checkoutType = StoreMealPortCheckoutTypeEnum.MANUAL;
            }
            this.updateMealPortCheckoutType(merchantId, storeId, portId, autoShiftChanged, checkoutType);
        }
        portId = storeMealPort.getPortId();
        if (storeMealPort.isHasPack()) {
            this.storeMealPortDAO.updateForUnPack(merchantId, storeId, portId);
            this.storeMealTakeupDAO.updatePackagePort(merchantId, storeId, portId);
        }

        if (storeMealPort.isHasDelivery()) {
            this.storeMealPortDAO.updateDeliveryPack(merchantId, storeId, portId);
            this.storeMealTakeupDAO.updateDeliveryPort(merchantId, storeId, portId);
        }

        storeMealTakeupDAO.updatePort0(merchantId, storeId, portId);//待出餐信息portId=0则设置为此portId
        StoreMealTask storeMealTask = this.storeMealTaskDAO.getById(merchantId, storeId, portId, false);
        storeMealPort.setStoreMealTask(storeMealTask);

        if (param.isUpdateCallPeripherals()) {
            this.storeMealPortPeripheralDAO.deleteByPortId(merchantId, storeId, portId);
            if (param.getCallPeripheralIdsSize() > 0) {
                List<StoreMealPortPeripheral> pplist = Lists.newArrayList();
                for (Long peripheralId : param.getCallPeripheralIds()) {
                    StoreMealPortPeripheral obj = new StoreMealPortPeripheral();
                    obj.setMerchantId(merchantId);
                    obj.setStoreId(storeId);
                    obj.setPeripheralId(peripheralId);
                    obj.setPortId(portId);
                    obj.setCreateTime(System.currentTimeMillis());
                    pplist.add(obj);
                }
                this.storeMealPortPeripheralDAO.batchCreate(pplist);
                storeMealPort.setCallMealPortPeripherals(pplist);
            }
        }
        return storeMealPort;
    }

    public void batchUpdatePrinterStatus(int merchantId, long storeId, List<StoreMealAutoPrintParam> storeMealAutoPrintParams) {
        if (storeMealAutoPrintParams == null || storeMealAutoPrintParams.isEmpty()) {
            return;
        }
        Map<Long, Integer> printerParams = new HashMap<>();
        for (StoreMealAutoPrintParam param : storeMealAutoPrintParams) {
            printerParams.put(param.getPortId(), param.getPrinterStatus());
        }
        storeMealTaskDAO.batchUpdatePrinterStatus(merchantId, storeId, printerParams);
    }

    public void initPrinterStatus(int merchantId, long storeId, long portId) {
        StoreMealTask storeMealTask = new StoreMealTask();
        storeMealTask.setPortId(portId);
        storeMealTask.setMerchantId(merchantId);
        storeMealTask.setStoreId(storeId);
        storeMealTask.setAppcopyId(0);
        storeMealTask.setCheckoutType(StoreMealPortCheckoutTypeEnum.MANUAL.getValue());
        storeMealTask.setPrinterStatus(StorePortPrinterStatusEnum.OFF.getValue());
        storeMealTask.setTaskStatus(StoreMealPortTaskStatusEnum.OFF.getValue());
        storeMealTask.setUpdateTime(System.currentTimeMillis());
        storeMealTask.replace();
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateMealPortCheckoutType(int merchantId, long storeId, long portId, boolean autoShiftChanged, StoreMealPortCheckoutTypeEnum checkoutType) {
        StoreMealTask storeMealTask = this.storeMealTaskDAO.getById(merchantId, storeId, portId, false);
        if (storeMealTask == null) {
            this.initPrinterStatus(merchantId, storeId, portId);
        } else {
            if (autoShiftChanged) {
                storeMealTask.setTaskStatus(StoreMealPortTaskStatusEnum.OFF.getValue());
                storeMealTask.setCheckoutType(checkoutType.getValue());
                storeMealTask.setUpdateTime(System.currentTimeMillis());
                storeMealTask.update();
            }
        }
    }

    public List<StoreMealPort> getStoreMealPortsIdle(int merchantId, long storeId, boolean enableSlave) throws T5weiException {
        List<StoreMealTask> storeMealTasks = storeMealTaskDAO.getList(merchantId, storeId, enableSlave);
        if (storeMealTasks.isEmpty()) {
            List<StoreMealPort> storeMealPorts = storeMealPortDAO.getList(merchantId, storeId, enableSlave);
            return storeMealPorts;
        }
        List<Long> portIds = new ArrayList<>();
        for (StoreMealTask storeMealTask : storeMealTasks) {
            if (storeMealTask.getTaskStatus() == StoreMealPortTaskStatusEnum.OFF.getValue()) {
                portIds.add(storeMealTask.getPortId());
            }
        }
        if (portIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<StoreMealPort> storeMealPorts = storeMealPortDAO.getByIds(merchantId, storeId, portIds, enableSlave);
        this.buildStoreMealPorts(storeMealPorts, storeMealTasks);
        return storeMealPorts;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreMealTask> registStoreAppTaskRelation(StoreAppTaskRelationParam storeAppTaskRelationParam) throws T5weiException {
        int merchantId = storeAppTaskRelationParam.getMerchantId();
        long storeId = storeAppTaskRelationParam.getStoreId();
        long appcopyId = storeAppTaskRelationParam.getAppcopyId();
        List<StoreMealTask> updateStoreMealTasks = new ArrayList<StoreMealTask>();
        List<StoreMealPortRelationParam> mealPortRelations = storeAppTaskRelationParam.getMealPortRelations();
        if (mealPortRelations == null || mealPortRelations.isEmpty()) {
            return updateStoreMealTasks;
        }
        for (StoreMealPortRelationParam param : mealPortRelations) {
            long portId = param.getPortId();
            int taskStatus = param.getTaskStatus(); // 任务关系状态：0=解除任务关系，1=建立任务关系
            int printerStatus = param.getPrinterStatus(); // 打印机连接状态：0=未连接，1=正常连接，2=无法打印
            int checkoutType = param.getCheckoutType(); // 出餐方式：0=手动，1=自动
            long portAppcopyId = appcopyId;
            if (taskStatus == StoreMealPortTaskStatusEnum.OFF.getValue() || appcopyId <= 0) {
                checkoutType = StoreMealPortCheckoutTypeEnum.AUTO.getValue();// 任务关系解除，则出餐方式设置为自动
            }
            long currentTime = System.currentTimeMillis();
            StoreMealTask storeMealTask = storeMealTaskDAO.getById(merchantId, storeId, portId, true);
            if (storeMealTask == null) {
                StoreMealPort storeMealPort = storeMealPortDAO.getById(merchantId, storeId, portId);
                if (storeMealPort == null) {
                    continue;
                }
                storeMealTask = new StoreMealTask();
                storeMealTask.setPortId(portId);
                storeMealTask.setMerchantId(merchantId);
                storeMealTask.setStoreId(storeId);
                storeMealTask.setAppcopyId(portAppcopyId);
                storeMealTask.setCheckoutType(checkoutType);
                storeMealTask.setPrinterStatus(printerStatus);
                storeMealTask.setTaskStatus(taskStatus);
                storeMealTask.setUpdateTime(currentTime);
                storeMealTask.replace();
                updateStoreMealTasks.add(storeMealTask);
                // 记录任务变更日志
                StoreMealTaskLog storeMealTaskLog = BeanUtil.copy(storeMealTask, StoreMealTaskLog.class);
                storeMealTaskLog.setInputParams(DataUtil.subString(storeAppTaskRelationParam.toString(), 500));
                storeMealTaskLogDAO.create(storeMealTaskLog);
            } else {
                //portAppcopyId=0的为后台任务逻辑自动请求解除任务关系，storeMealTask.printerPeripheralId > 0表示设置了打印机
				if (taskStatus == StoreMealPortTaskStatusEnum.OFF.getValue() && portAppcopyId > 0 && storeMealTask.getPrinterPeripheralId() > 0) {
                    //portAppcopyId>0时为前端请求解除任务关系，storeMealTask.appcopyId=0则可以被接管不忽略，portAppcopyId不相等则为不属于它的任务要忽略
                    if (storeMealTask.getAppcopyId() > 0 && storeMealTask.getAppcopyId() != portAppcopyId) {
                        continue;
                    }
                }
                storeMealTask.setAppcopyId(portAppcopyId);
                storeMealTask.setCheckoutType(checkoutType);
                storeMealTask.setPrinterStatus(printerStatus);
                storeMealTask.setTaskStatus(taskStatus);
                storeMealTask.setUpdateTime(System.currentTimeMillis());
                storeMealTask.update();
                if (log.isDebugEnabled()) {
                    log.debug("####StoreMealTask changed, merchantId=" + merchantId + ",storeId=" + storeId
                            + ", portAppcopyId=" + portAppcopyId + ", portId=" + portId + ",taskStatus=" + taskStatus + ",checkoutType=" + checkoutType + ",printerStatus=" + printerStatus);
                }
                updateStoreMealTasks.add(storeMealTask);
                // 记录任务变更日志
                StoreMealTaskLog storeMealTaskLog = BeanUtil.copy(storeMealTask, StoreMealTaskLog.class);
                storeMealTaskLog.setInputParams(DataUtil.subString(storeAppTaskRelationParam.toString(), 500));
                storeMealTaskLogDAO.create(storeMealTaskLog);
            }
        }
        return updateStoreMealTasks;
    }

    public List<StoreMealPort> getStoreAppTaskMealPorts(int merchantId, long storeId, long appcopyId) throws T5weiException {
        boolean enableSlave = true;
        List<StoreMealTask> storeMealTasks = storeMealTaskDAO.getStoreAppMealTasks(merchantId, storeId, appcopyId, enableSlave);
        return this.getStoreMealPorts(merchantId, storeId, storeMealTasks);
    }

    public List<StoreMealPort> getStoreAppTaskMealPorts(int merchantId, long storeId, long appcopyId, long checkoutType) throws T5weiException {
        boolean enableSlave = true;
        List<StoreMealTask> storeMealTasks = storeMealTaskDAO.getStoreAppMealTasks(merchantId, storeId, appcopyId, checkoutType, enableSlave);
        return this.getStoreMealPorts(merchantId, storeId, storeMealTasks);
    }

    public List<StoreMealPort> getStoreAppTaskRegistMealPorts(int merchantId, long storeId, long appcopyId, List<Long> registPortIds) throws T5weiException {
        boolean enableSlave = true;
        if (appcopyId == 0 || registPortIds == null || registPortIds.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<StoreMealTask> storeMealTasks = storeMealTaskDAO.getStoreAppTaskRegistMealPorts(merchantId, storeId, appcopyId, registPortIds, enableSlave);
        return this.getStoreMealPorts(merchantId, storeId, storeMealTasks);
    }

    private List<StoreMealPort> getStoreMealPorts(int merchantId, long storeId, List<StoreMealTask> storeMealTasks) {
        boolean enableSlave = true;
        if (storeMealTasks.isEmpty()) {
            return new ArrayList<>(0);
        }
        Map<Long, Integer> portCheckoutTypeMap = new HashMap<>();
        for (StoreMealTask storeMealTask : storeMealTasks) {
            portCheckoutTypeMap.put(storeMealTask.getPortId(), storeMealTask.getCheckoutType());
        }
        List<Long> portIds = new ArrayList<>(portCheckoutTypeMap.keySet());
        List<StoreMealPort> storeMealPorts = storeMealPortDAO.getByIds(merchantId, storeId, portIds, enableSlave);
        this.buildStoreMealPorts(storeMealPorts, storeMealTasks);
        return storeMealPorts;
    }

    public StoreMealPort getParkagePort(int merchantId, long storeId) {
        boolean enableSlave = true;
        return storeMealPortDAO.getParkagePort(merchantId, storeId, enableSlave);
    }

    public StoreMealPort getDeliveryPort(int merchantId, long storeId) {
        boolean enableSlave = true;
        return storeMealPortDAO.getDeliveryPort(merchantId, storeId, enableSlave);
    }

    public List<StoreMealPort> getStoreMealPorts(int merchantId, long storeId) {
        boolean enableSlave = true;
        return this.storeMealPortDAO.getList(merchantId, storeId, enableSlave);
    }

    public int countStoreMealPorts(int merchantId, long storeId) {
        boolean enableSlave = true;
        return this.storeMealPortDAO.getCount(merchantId, storeId, enableSlave);
    }

    public Map<Long, StoreMealPort> getStoreMealPortMap(int merchantId, long storeId, boolean loadEx) {
        Map<Long, StoreMealPort> mealPortMap = Maps.newHashMap();
        List<StoreMealPort> storeMealPorts = this.getStoreMealPorts(merchantId, storeId, loadEx);
        if (storeMealPorts != null && !storeMealPorts.isEmpty()) {
            for (StoreMealPort storeMealPort : storeMealPorts) {
                mealPortMap.put(storeMealPort.getPortId(), storeMealPort);
            }
        }
        return mealPortMap;
    }

    public List<StoreMealPort> getStoreMealPorts(int merchantId, long storeId, boolean loadEx) {
        boolean enableSlave = true;
        List<StoreMealPort> storeMealPorts = this.storeMealPortDAO.getList(merchantId, storeId, enableSlave);
        if (loadEx) {
            this.buildStoreMealPorts(storeMealPorts);
        }
        return storeMealPorts;
    }

    public List<StoreMealTask> getStoreMealTasks(int merchantId, long storeId) {
        boolean enableSlave = true;
        return this.storeMealTaskDAO.getList(merchantId, storeId, enableSlave);
    }

    public void clearPeripheral(int merchantId, long storeId, long peripheralId) {
        this.storeMealPortDAO.clearPeripheral(merchantId, storeId, peripheralId);
    }

    public Map<Long, StoreMealPort> getStoreMealPortMapInIds(int merchantId,
                                                             long storeId,
                                                             List<Long> portIds) {
        boolean enableSlave = true;
        return this.storeMealPortDAO.getMapInIds(merchantId, storeId, portIds, enableSlave);
    }

    private Map<Long, StoreMealTask> toStoreMealTaskMap(List<StoreMealTask>
                                                                storeMealTasks) {
        Map<Long, StoreMealTask> map = Maps.newHashMap();
        for (StoreMealTask storeMealTask : storeMealTasks) {
            map.put(storeMealTask.getPortId(), storeMealTask);
        }
        return map;
    }

    private void buildStoreMealPorts(List<StoreMealPort> storeMealPorts,
                                     List<StoreMealTask> storeMealTasks) {
        Map<Long, StoreMealTask> map = this.toStoreMealTaskMap(storeMealTasks);
        for (StoreMealPort storeMealPort : storeMealPorts) {
            storeMealPort.setStoreMealTask(map.get(storeMealPort.getPortId()));
        }
    }

    private void buildStoreMealPorts(List<StoreMealPort> storeMealPorts, Map<Long, StoreMealTask> storeMealTaskMap,
                                     Map<Long, List<StoreMealPortPeripheral>> peripheralsMap) {
        for (StoreMealPort storeMealPort : storeMealPorts) {
            storeMealPort.setStoreMealTask(storeMealTaskMap.get(storeMealPort.getPortId()));
        }
        if (peripheralsMap != null) {
            for (StoreMealPort storeMealPort : storeMealPorts) {
                storeMealPort.setCallMealPortPeripherals(peripheralsMap.get(storeMealPort.getPortId()));
            }
        }
    }

    private void buildStoreMealPorts(List<StoreMealPort> storeMealPorts) {
        boolean enableSlave = true;
        if (storeMealPorts.isEmpty()) {
            return;
        }
        int merchantId = storeMealPorts.get(0).getMerchantId();
        long storeId = storeMealPorts.get(0).getStoreId();
        List<Long> portIds = Lists.newArrayList();
        for (StoreMealPort storeMealPort : storeMealPorts) {
            portIds.add(storeMealPort.getPortId());
        }
        Map<Long, StoreMealTask> map = this.storeMealTaskDAO.getMapInIds(merchantId, storeId, portIds, enableSlave);
        Map<Long, List<StoreMealPortPeripheral>> listMap = this.storeMealPortPeripheralDAO.getListMapInPortIds(merchantId, storeId, portIds);
        this.buildStoreMealPorts(storeMealPorts, map, listMap);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateTaskMealTime(int merchantId, long storeId, long appcopyId, List<Long> registPortIds) {
        boolean enableSlave = false;
        List<StoreMealTask> storeMealTasks = storeMealTaskDAO.getStoreAppTaskRegistMealPorts(merchantId, storeId, appcopyId, registPortIds, enableSlave);
        if (storeMealTasks == null || storeMealTasks.isEmpty()) {
            return;
        }
        for (StoreMealTask storeMealTask : storeMealTasks) {
            storeMealTask.setMealTime(System.currentTimeMillis());
            storeMealTask.update();
        }
    }

    public List<StoreMealTaskLog> getTaskLogs(int merchantId, long storeId, int size) {
        return storeMealTaskLogDAO.getTaskLogs(merchantId, storeId, size);
    }


    //加工档口相关//////////////////////////////////////////////////////////

    /**
     * 根据传菜口Id获取加工档口
     *
     * @param merchantId
     * @param storeId
     * @param sendPortId
     * @param enableSlave
     * @return
     */
    public List<StoreMealPort> getStoreMealPortsBySendPortId(int merchantId, long storeId, long sendPortId, boolean enableSlave) {
        return storeMealPortDAO.getStoreMealPortsBySendPortId(merchantId, storeId, sendPortId, enableSlave);
    }

    /**
     * 设置传菜口上关联的加工档口
     *
     * @param merchantId
     * @param storeId
     * @param storeMealPortSendId
     * @param storeMealPortIds
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void setStoreMealPortSend(int merchantId, long storeId, long storeMealPortSendId, List<Long> storeMealPortIds) {
        if (storeMealPortIds == null || storeMealPortIds.isEmpty() || storeMealPortSendId <= 0) {
            return;
        }
        this.storeMealPortDAO.batchUpdateStoreMealPorts(merchantId, storeId, storeMealPortSendId, storeMealPortIds);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void unSetStoreMealPortSend(int merchantId, long storeId, long storeMealPortSendId) {
        if (storeMealPortSendId <= 0) {
            return;
        }
        this.storeMealPortDAO.updateStoreMealPorts(merchantId, storeId, storeMealPortSendId);
    }

    public Map<Long, StoreMealPortSend> getStoreMealPorts4SendPort(int merchantId, long storeId, List<Long> portIds, boolean enableSlave) {
        List<StoreMealPort> storeMealPorts = new ArrayList<StoreMealPort>();
        if (portIds == null || portIds.isEmpty()) {
            storeMealPorts = this.storeMealPortDAO.getList(merchantId, storeId, enableSlave);
        } else {
            storeMealPorts = this.storeMealPortDAO.getByIds(merchantId, storeId, portIds, enableSlave);
        }
        Map<Long, StoreMealPortSend> storeMealPortSendMap = this.storeMealPortSendDAO.getStoreMealPortSendMap(merchantId, storeId, StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue(), enableSlave);

        Map<Long, StoreMealPortSend> storeMealPortMap = new HashMap<Long, StoreMealPortSend>();
        for (StoreMealPort storeMealPort : storeMealPorts) {
            if (storeMealPort.getSendPortId() > 0 && storeMealPortSendMap.get(storeMealPort.getSendPortId()) != null) {
                storeMealPortMap.put(storeMealPort.getPortId(), storeMealPortSendMap.get(storeMealPort.getSendPortId()));
            }
        }
        return storeMealPortMap;
    }
}
