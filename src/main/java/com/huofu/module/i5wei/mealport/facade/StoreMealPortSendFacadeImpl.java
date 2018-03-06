package com.huofu.module.i5wei.mealport.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.mealportsend.StoreMealPortSendDTO;
import huofucore.facade.i5wei.mealportsend.StoreMealPortSendFacade;
import huofucore.facade.i5wei.mealportsend.StoreMealPortSendParam;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;
import huofucore.facade.i5wei.mealportsend.StoreMealSweepTypeEnum;
import huofucore.facade.i5wei.peripheral.I5weiPeripheralDTO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.huofu.module.i5wei.base.FacadeUtil;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortSend;
import com.huofu.module.i5wei.mealport.service.StoreMealPortSendService;

import huofuhelper.util.DataUtil;
import huofuhelper.util.ValidateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;

@Component
@ThriftServlet(name = "storeMealPortSendFacadeServlet", serviceClass = StoreMealPortSendFacade.class)
public class StoreMealPortSendFacadeImpl implements StoreMealPortSendFacade.Iface {
    
    @Autowired
    private StoreMealPortSendService storeMealPortSendService;
    
    @Autowired
    private FacadeUtil facadeUtil;
    
    private Log log = LogFactory.getLog(StoreMealPortSendFacadeImpl.class);
    
    @Override
    public StoreMealPortSendDTO saveStoreMealPortSendDTO(StoreMealPortSendParam param) throws T5weiException, TException {
        if(param.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_SEND_PORT.getValue()){
            if(!ValidateUtil.testLength(param.getSendPortName(), 0, 50, false)){//传菜口名称必填
                log.error(DataUtil.infoWithParams("传菜口名称错误:param=#1", new Object[]{param}));
                throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_NAME_ERROR.getValue(), "store_meal_send_port_name is error");
            }
        } else {
            if(!ValidateUtil.testLength(param.getSendPortName(), 0, 50, true)){//打包台和外卖台名称非必填
                log.error(DataUtil.infoWithParams("打包台和外卖台名称错误:param=#1",new Object[]{param}));
                throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PACKAGED_DELIVERY_NAME_ERROR.getValue(), "store_meal_send_packaged_delivery name is error");
            }
        }
        if(param.getSweepType() == StoreMealSweepTypeEnum.PAPER_SWEEP.getValue()){//纸划菜必须设置打印机
            if(param.getPrinterPeripheralId() <= 0){
                log.error(DataUtil.infoWithParams("用小票划菜必须设置小票打印机:param=#1",new Object[]{param}));
                throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_NO_SET_PRINTTER.getValue(), "store_meal_send_port no set printter");
            }
        }
        if(param.getSweepType() == StoreMealSweepTypeEnum.PAD_SWEEP.getValue() && param.isPrintDivItem()){
            if(param.getPrinterPeripheralId() <= 0){
                log.error(DataUtil.infoWithParams("用iPad划菜,开启打印传菜分单必须设置打印机:param=#1",new Object[]{param}));
                throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_NO_SET_PRINTTER.getValue(), "store_meal_send_port no set printter");
            }
        }
        if(!ValidateUtil.testLength(param.getCallMessage(), 0, 50, true)){
            log.error(DataUtil.infoWithParams("叫号信息错误:param=#1",new Object[]{param}));
            throw new T5weiException(T5weiErrorCodeType.STORE_MEAL_SEND_PORT_CALL_MESSAGE_ERROR.getValue(), "store_meal_send_port call message error");
        }
        StoreMealPortSend storeMealPortSend = this.storeMealPortSendService.saveStoreMealPortSend(param);
        return BeanUtil.copy(storeMealPortSend, StoreMealPortSendDTO.class);
    }

    @Override
    public StoreMealPortSendDTO getStoreMealPortSendDTO(int merchantId, long storeId, long mealSendId, boolean loadStoreMealPortDTO, boolean loadPeripheralDTO) throws T5weiException, TException {
        StoreMealPortSend storeMealPortSend = this.storeMealPortSendService.getStoreMealPortSendById(merchantId, storeId, mealSendId, loadStoreMealPortDTO, true);
        List<StoreMealPortSend> storeMealPortSends = new ArrayList<StoreMealPortSend>();
        storeMealPortSends.add(storeMealPortSend);
        
        List<StoreMealPortSendDTO> storeMealPortSendDTOs = this.buildI5weiPeripheralDTOs(merchantId, storeMealPortSends, loadPeripheralDTO);
        StoreMealPortSendDTO storeMealPortSendDTO = storeMealPortSendDTOs.get(0);
        
        return storeMealPortSendDTO;
    }

    @Override
    public List<StoreMealPortSendDTO> getStoreMealPortSendDTOs(int merchantId, long storeId, int sendPortType, boolean loadStoreMealPortDTO, boolean loadPeripheralDTO) throws T5weiException, TException {
        List<StoreMealPortSend> storeMealPortSends = this.storeMealPortSendService.getStoreMealPortSends(merchantId, storeId, sendPortType, loadStoreMealPortDTO, false);
        List<StoreMealPortSendDTO> storeMealPortSendDTOs = this.buildI5weiPeripheralDTOs(merchantId, storeMealPortSends, loadPeripheralDTO);
        return storeMealPortSendDTOs;
    }

    @Override
    public void deleteStoreMealPortSend(int merchantId, long storeId, long mealSendPortId) throws T5weiException, TException {
        this.storeMealPortSendService.deleteStoreMealPortSend(merchantId, storeId, mealSendPortId);
    }

    @Override
    public void initStoreMealPortSendDTO(int merchantId, long storeId) throws T5weiException, TException {
        this.storeMealPortSendService.initStoreMealPortSendDTO(merchantId, storeId);
    }

    /**
     * 构造外部设备I5weiPeripheralDTO
     * @param merchantId
     * @param storeMealPortSends
     * @param loadPeripheralDTO
     * @return 
     * @throws TException 
     */
    private List<StoreMealPortSendDTO> buildI5weiPeripheralDTOs(int merchantId, List<StoreMealPortSend> storeMealPortSends, boolean loadPeripheralDTO) throws TException{
        List<StoreMealPortSendDTO> storeMealPortSendDTOs = BeanUtil.copyList(storeMealPortSends, StoreMealPortSendDTO.class);
        Map<Long, StoreMealPortSendDTO> storeMealPortSendDTOMap = new HashMap<Long, StoreMealPortSendDTO>();
        for (StoreMealPortSendDTO storeMealPortSendDTO : storeMealPortSendDTOs) {
            storeMealPortSendDTOMap.put(storeMealPortSendDTO.getSendPortId(), storeMealPortSendDTO);
        }
        for (StoreMealPortSend storeMealPortSend : storeMealPortSends) {
            if(storeMealPortSend.getStoreMealPorts() != null && !storeMealPortSend.getStoreMealPorts().isEmpty()){
                List<StoreMealPortDTO> storeMealPortDTOs = BeanUtil.copyList(storeMealPortSend.getStoreMealPorts(), StoreMealPortDTO.class);
                storeMealPortSendDTOMap.get(storeMealPortSend.getSendPortId()).setStoreMealPortDTOs(storeMealPortDTOs);
            }
        }
        if(loadPeripheralDTO){
            Set<Long> peripheralIdSet = new HashSet<Long>();
            for (StoreMealPortSend storeMealPortSend : storeMealPortSends) {
                peripheralIdSet.add(storeMealPortSend.getPrinterPeripheralId());
                peripheralIdSet.add(storeMealPortSend.getCallPeripheralId());
            }
            List<Long> peripheralIdList = new ArrayList<Long>(peripheralIdSet);
            for (StoreMealPortSendDTO storeMealPortSendDTO : storeMealPortSendDTOs) {
                Map<Long, I5weiPeripheralDTO> peripheralDTOMap = this.facadeUtil.buildI5weiPeripheralDTOMap(merchantId, peripheralIdList);
                storeMealPortSendDTO.setPrinterPeripheralDTO(peripheralDTOMap.get(storeMealPortSendDTO.getPrinterPeripheralId()));
                storeMealPortSendDTO.setCallPeripheralDTO(peripheralDTOMap.get(storeMealPortSendDTO.getCallPeripheralId()));
            }
        }
        return storeMealPortSendDTOs;
    }
}