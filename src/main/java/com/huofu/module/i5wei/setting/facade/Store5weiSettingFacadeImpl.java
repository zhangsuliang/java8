package com.huofu.module.i5wei.setting.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.FacadeUtil;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.facade.StoreMealPortFacadeUtil;
import com.huofu.module.i5wei.setting.dao.Store5weiSettingDAO;
import com.huofu.module.i5wei.setting.dao.StoreDefinedPrinterDAO;
import com.huofu.module.i5wei.setting.dao.StoreTableSettingDAO;
import com.huofu.module.i5wei.setting.entity.StoreDefinedPrinter;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.service.StoreDefinedPrinterService;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import com.huofu.module.i5wei.table.dao.StoreAreaDAO;
import com.huofu.module.i5wei.table.entity.StoreArea;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.peripheral.I5weiPeripheralDTO;
import huofucore.facade.i5wei.store5weisetting.*;
import huofucore.facade.i5wei.table.StoreAreaDTO;
import huofucore.facade.merchant.peripheral.PeripheralDTO;
import huofucore.facade.merchant.peripheral.PeripheralFacade;
import huofucore.facade.merchant.printer.PrintMsgTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
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

import com.huofu.module.i5wei.base.SnsPublish;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.eventtype.EventType;
import com.huofu.module.i5wei.heartbeat.service.StoreAppcopyRefreshService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingSaveResult;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;

/**
 * Created by akwei on 5/29/15.
 */
@Component
@ThriftServlet(name = "store5weiSettingFacadeServlet", serviceClass = Store5weiSettingFacade.class)
public class Store5weiSettingFacadeImpl implements Store5weiSettingFacade.Iface {

    private final static Log log = LogFactory.getLog(Store5weiSettingFacadeImpl.class);

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private SnsPublish snsPublish;

    @Autowired
    private StoreAppcopyRefreshService storeAppcopyRefreshService;

    @Autowired
    private StoreDefinedPrinterDAO storeDefinedPrinterDAO;

    @Autowired
    private StoreDefinedPrinterService storeDefinedPrinterService;

    @Autowired
    private Store5weiSettingDAO store5weiSettingDAO;

    @Autowired
    private StoreTableSettingDAO storeTableSettingDAO;

    @Autowired
    private StoreAreaDAO storeAreaDAO;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private FacadeUtil facadeUtil;

    @Autowired
    private StoreMealPortFacadeUtil storeMealPortFacadeUtil;

    @ThriftClient
    private PeripheralFacade.Iface peripheralFacade;

    @Override
    public Store5weiSettingDTO saveStore5weiSetting(Store5weiSettingParam param) throws T5weiException, TException {
        Store5weiSettingSaveResult store5weiSettingSave = this.store5weiSettingService.saveStore5weiSetting(param);
        sendStoreSettingUpdateEvent(store5weiSettingSave);
        return BeanUtil.copy(store5weiSettingSave.getStore5weiSetting(), Store5weiSettingDTO.class);
    }

    /**
     * 发送店铺设置变更事件
     *
     * @param store5weiSettingSave
     */
    private void sendStoreSettingUpdateEvent(Store5weiSettingSaveResult store5weiSettingSave) {
        Store5weiSetting store5weiSetting = store5weiSettingSave.getStore5weiSetting();
        Map<String, Object> eventDataMap = new HashMap<>();
        eventDataMap.put("merchantId", store5weiSetting.getMerchantId());
        eventDataMap.put("storeId", store5weiSetting.getStoreId());
        //如果台位费变更,需要在发消息的时候进行设置
        if (store5weiSettingSave.isTableFeeUpdate()) {
            eventDataMap.put("enableTableFee", 1);
        }
        eventDataMap.put("updateTime", System.currentTimeMillis()); //更新时间
        String topic = SysConfig.getStoreSettingTopicArn();
        snsPublish.publish(eventDataMap, EventType.STORE_SETTING_UPDATE, topic);
    }

    @Override
    public Store5weiSettingDTO saveStoreSerialNumberStart(StoreSerialNumberSettingParam param) throws T5weiException, TException {
        Store5weiSetting store5weiSetting = store5weiSettingService.saveStoreSerialNumberStart(param);
        storeOrderService.updateStoreOrderNumber(store5weiSetting.getMerchantId(), store5weiSetting.getStoreId(), store5weiSetting.getSerialNumberStartBySiteNumber());
        Store5weiSettingDTO store5weiSettingDTO = BeanUtil.copy(store5weiSetting, Store5weiSettingDTO.class);
        return store5weiSettingDTO;
    }

    @Override
    public Store5weiSettingDTO saveStoreSiteNumber(StoreSiteNumberSettingParam param) throws T5weiException, TException {
        Store5weiSetting store5weiSetting = store5weiSettingService.saveStoreSiteNumber(param);
        storeOrderService.updateStoreOrderNumber(store5weiSetting.getMerchantId(), store5weiSetting.getStoreId(), store5weiSetting.getSerialNumberStartBySiteNumber());
        Store5weiSettingDTO store5weiSettingDTO = BeanUtil.copy(store5weiSetting, Store5weiSettingDTO.class);
        this.publish(store5weiSetting.getMerchantId(), store5weiSetting.getStoreId(), store5weiSetting.isSiteNumberEnable());
        return store5weiSettingDTO;
    }

    @Override
    public Store5weiSettingDTO getStore5weiSetting(int merchantId, long storeId) throws TException {
        Store5weiSetting store5weiSetting = this.store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        Store5weiSettingDTO store5weiSettingDTO = BeanUtil.copy(store5weiSetting, Store5weiSettingDTO.class);
        return store5weiSettingDTO;
    }

    @Override
    public Store5weiSettingDTO saveStorePackageSetting(StorePackageSettingParam param) throws T5weiException, TException {
        Store5weiSetting store5weiSetting = store5weiSettingService.saveStorePackage(param);
        Store5weiSettingDTO store5weiSettingDTO = BeanUtil.copy(store5weiSetting, Store5weiSettingDTO.class);
        return store5weiSettingDTO;
    }

    @Override
    public Store5weiSettingDTO saveStoreQueueNumberSetting(StoreQueueNumberSettingParam param) throws T5weiException, TException {
        Store5weiSetting store5weiSetting = store5weiSettingService.saveStoreQueueNumber(param);
        Store5weiSettingDTO store5weiSettingDTO = BeanUtil.copy(store5weiSetting, Store5weiSettingDTO.class);
        return store5weiSettingDTO;
    }

    @Override
    public Store5weiSettingDTO saveStoreCustomerAvgPaymentModelSetting(StoreCustomerAvgPaymentModelSettingParam param)
            throws T5weiException, TException {

        //validate
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long staffId = param.getStaffId();

        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }

        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }

        if (staffId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + " ]  staffId[" + storeId + "] invalid");
        }

        Store5weiSetting store5weiSetting = store5weiSettingService.saveStoreCustomerAvgPaymentModelSetting(param);
        Store5weiSettingDTO store5weiSettingDTO = BeanUtil.copy(store5weiSetting, Store5weiSettingDTO.class);
        return store5weiSettingDTO;
    }

    public void publish(int merchantId, long storeId, boolean siteNumberEnable) {
        //SNS发布事件
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("merchantId", merchantId);
        dataMap.put("storeId", storeId);
        dataMap.put("siteNumberEnable", siteNumberEnable);
        dataMap.put("updateTime", System.currentTimeMillis());
        String siteNumberTopicArn = SysConfig.getSiteNumberTopicArn();
        int eventType = EventType.SITE_NUMBER;
        snsPublish.publish(dataMap, eventType, siteNumberTopicArn);
    }

    @Override
    public boolean isStore5weiSettingSignIn(int merchantId, long storeId) throws TException {
        boolean enableSignIn;
        Map<Long, Long> appcopyIds = storeAppcopyRefreshService.getSignInAppcopys(merchantId, storeId);
        if (appcopyIds.isEmpty()) {
            enableSignIn = false;
        } else {
            enableSignIn = true;
        }
        return enableSignIn;
    }

    @Override
    public StoreDefinedPrinterDTO saveStoreDefinedPrinter(StoreDefinedPrinterParam param) throws T5weiException, TException {
        if (param.getMerchantId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + param.getMerchantId() + "] is invalid!");
        }
        if (param.getStoreId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + param.getStoreId() + "] is invalid!");
        }
        if (DataUtil.isEmpty(param.getPrinterName())) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "printerName[" + param.getPrinterName() + "] is invalid!");
        }
        if (param.getPrintMsgType() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "printMsgType[" + param.getPrintMsgType() + "] is invalid!");
        }
        if (param.getPrinterPeripheralId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "printerPeripheralId[" + param.getPrinterPeripheralId() + "] is invalid!");
        }
        StoreDefinedPrinter storeDefinedPrinter = storeDefinedPrinterService.saveStoreDefinedPrinter(param);
        return this._buildStoreDefinedPrinterDTO(storeDefinedPrinter);
    }

    @Override
    public StoreDefinedPrinterDTO getStoreDefinedPrinter(int merchantId, long storeId, long printerId) throws T5weiException, TException {
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] is invalid!");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] is invalid!");
        }
        if (printerId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "printerId[" + printerId + "] is invalid!");
        }
        StoreDefinedPrinter storeDefinedPrinter = storeDefinedPrinterService.getStoreDefinedPrinterById(merchantId, storeId, printerId);
        return this._buildStoreDefinedPrinterDTO(storeDefinedPrinter);
    }

    @Override
    public List<StoreDefinedPrinterDTO> getStoreDefinedPrinterList(int merchantId, long storeId) throws T5weiException, TException {
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] is invalid!");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] is invalid!");
        }
        List<StoreDefinedPrinter> storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterList(merchantId, storeId, true);
        return this._buildStoreDefinedPrinterDTOList(merchantId, storeId, storeDefinedPrinters);
    }

    @Override
    public List<StoreDefinedPrinterDTO> getStoreDefinedPrinterListByType(int merchantId, long storeId, int printMsgType) throws T5weiException, TException {
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] is invalid!");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] is invalid!");
        }
        List<StoreDefinedPrinter> storeDefinedPrinters = storeDefinedPrinterDAO.getStoreDefinedPrinterListByType(merchantId, storeId, printMsgType, true);
        return this._buildStoreDefinedPrinterDTOList(merchantId, storeId, storeDefinedPrinters);
    }

    @Override
    public void deleteStoreDefinedPrinter(int merchantId, long storeId, long printerId) throws T5weiException, TException {
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "] is invalid!");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] is invalid!");
        }
        if (printerId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "printerId[" + printerId + "] is invalid!");
        }
        storeDefinedPrinterService.deleteStoreDefinedPrinter(merchantId, storeId, printerId);
    }

    private StoreDefinedPrinterDTO _buildStoreDefinedPrinterDTO(StoreDefinedPrinter storeDefinedPrinter) throws TException {
        int merchantId = storeDefinedPrinter.getMerchantId();
        long storeId = storeDefinedPrinter.getStoreId();
        StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, true);
        List<Long> peripheralIds = Lists.newArrayList();
        peripheralIds.add(storeDefinedPrinter.getPrinterPeripheralId());
        Map<Long, I5weiPeripheralDTO> map = this.facadeUtil.buildI5weiPeripheralDTOMap(merchantId, peripheralIds);
        Map<Long, StoreMealPort> storeMealPortMap = storeMealPortDAO.getMap(merchantId, storeId, true);
        Map<Long, StoreArea> storeAreaMap = storeAreaDAO.getStoreAreaMapByStoreId(storeId, true);
        return this.buildStoreDefinedPrinterDTO(storeDefinedPrinter, storeTableSetting, map, storeMealPortMap, storeAreaMap);
    }


    private StoreDefinedPrinterDTO buildStoreDefinedPrinterDTO(StoreDefinedPrinter storeDefinedPrinter, StoreTableSetting storeTableSetting, Map<Long, I5weiPeripheralDTO> map, Map<Long, StoreMealPort> storeMealPortMap, Map<Long, StoreArea> storeAreaMap) throws TException {
        String printScope = storeDefinedPrinter.getPrintScope();
        int merchantId = storeDefinedPrinter.getMerchantId();
        long storeId = storeDefinedPrinter.getStoreId();
        List<StoreMealPortDTO> storeMealPortDTOs = new ArrayList<>();
        List<StoreAreaDTO> storeAreaDTOs = new ArrayList<>();
        if (DataUtil.isNotEmpty(printScope)) {
            List<Integer> scopes = JsonUtil.parse(printScope, List.class);
            //加工档口(出餐口)
            if (DataUtil.isNotEmpty(printScope)) {
                for (Integer scope : scopes) {
                    StoreMealPort storeMealPort = storeMealPortMap.get(Long.valueOf(String.valueOf(scope)));
                    if (storeMealPort != null) {
                        StoreMealPortDTO storeMealPortDTO = storeMealPortFacadeUtil.buildStoreMealPortDTO(storeMealPort, false);
                        storeMealPortDTOs.add(storeMealPortDTO);
                    }
                }
            }
            if (storeTableSetting.isEnableTableMode()) {
                //区域的点菜单
                if (storeDefinedPrinter.getPrintMsgType() == PrintMsgTypeEnum.I5WEI_ORDER_MEAL.getValue()) {
                    for (Integer scope : scopes) {
                        StoreArea storeArea = storeAreaMap.get(Long.valueOf(String.valueOf(scope)));
                        if (storeArea != null) {
                            StoreAreaDTO storeAreaDTO = new StoreAreaDTO();
                            BeanUtil.copy(storeArea, storeAreaDTO);
                            storeAreaDTOs.add(storeAreaDTO);
                        }
                    }
                }
            }

        }

        StoreDefinedPrinterDTO storeDefinedPrinterDTO = new StoreDefinedPrinterDTO();
        storeDefinedPrinterDTO.setPrinterId(storeDefinedPrinter.getPrinterId());
        storeDefinedPrinterDTO.setMerchantId(merchantId);
        storeDefinedPrinterDTO.setStoreId(storeId);
        storeDefinedPrinterDTO.setPrinterName(storeDefinedPrinter.getPrinterName());
        storeDefinedPrinterDTO.setPrinterPeripheralId(storeDefinedPrinter.getPrinterPeripheralId());
        if (map.get(storeDefinedPrinter.getPrinterPeripheralId()) != null) {
            storeDefinedPrinterDTO.setI5weiPeripheralDTO(map.get(storeDefinedPrinter.getPrinterPeripheralId()));
        }
        storeDefinedPrinterDTO.setPrintMsgType(storeDefinedPrinter.getPrintMsgType());
        List<Integer> printScopeInt = JsonUtil.parse(printScope, List.class);
        List<Long> printScopes = new ArrayList<>();
        for (Integer scope : printScopeInt) {
            printScopes.add(Long.valueOf(String.valueOf(scope)));
        }
        storeDefinedPrinterDTO.setPrintScope(printScopes);
        storeDefinedPrinterDTO.setStoreAreaDTOs(storeAreaDTOs);
        storeDefinedPrinterDTO.setStoreMealPortDTOs(storeMealPortDTOs);
        storeDefinedPrinterDTO.setCreateTime(storeDefinedPrinter.getCreateTime());
        storeDefinedPrinterDTO.setUpdateTime(storeDefinedPrinter.getUpdateTime());
        return storeDefinedPrinterDTO;
    }


    private List<StoreDefinedPrinterDTO> _buildStoreDefinedPrinterDTOList(int merchantId, long storeId, List<StoreDefinedPrinter> storeDefinedPrinters) throws TException {
        //根据打印模式，和 printMsgType 来判定 printerScope是区域还是加工档口的id来组装 printerScopeNames
        Store5weiSetting store5weiSetting = store5weiSettingDAO.getById(merchantId, storeId, true);
        StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId, merchantId, true);
        Map<Long, StoreMealPort> storeMealPortMap = storeMealPortDAO.getMap(merchantId, storeId, true);
        Map<Long, StoreArea> storeAreaMap = storeAreaDAO.getStoreAreaMapByStoreId(storeId, true);
        List<Long> peripheralIds = Lists.newArrayList();
        for (StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters) {
            if (storeDefinedPrinter.getPrinterPeripheralId() > 0) {
                peripheralIds.add(storeDefinedPrinter.getPrinterPeripheralId());
            }
        }
        Map<Long, I5weiPeripheralDTO> map = this.facadeUtil.buildI5weiPeripheralDTOMap(merchantId, peripheralIds);
        List<StoreDefinedPrinterDTO> list = Lists.newArrayList();
        for (StoreDefinedPrinter storeDefinedPrinter : storeDefinedPrinters) {
            StoreDefinedPrinterDTO storeDefinedPrinterDTO = this.buildStoreDefinedPrinterDTO(storeDefinedPrinter, storeTableSetting, map, storeMealPortMap, storeAreaMap);
            list.add(storeDefinedPrinterDTO);
        }
        return list;
    }
}
