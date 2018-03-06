package com.huofu.module.i5wei.mealport.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.FacadeUtil;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortPeripheral;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.mealport.StoreMealTaskDTO;
import huofucore.facade.i5wei.peripheral.I5weiPeripheralDTO;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StoreMealPortFacadeUtil {

    @Autowired
    private FacadeUtil facadeUtil;

    public Map<Long, StoreMealPortDTO> buildStoreMealPortDTOMap
            (int merchantId, List<StoreMealPort> storeMealPorts) throws TException {
        List<StoreMealPortDTO> storeMealPortDTOs = this.buildStoreMealPortDTOs
                (merchantId, storeMealPorts, true);
        Map<Long, StoreMealPortDTO> map = Maps.newHashMap();
        for (StoreMealPortDTO storeMealPortDTO : storeMealPortDTOs) {
            map.put(storeMealPortDTO.getPortId(), storeMealPortDTO);
        }
        return map;
    }

    public StoreMealPortDTO buildStoreMealPortDTO(StoreMealPort
                                                          storeMealPort,
                                                  boolean loadPeripheral)
            throws TException {
        Map<Long, I5weiPeripheralDTO> map = null;
        if (loadPeripheral) {
            List<Long> peripheralIdList = Lists.newArrayList(storeMealPort
                    .getCallPeripheralId(), storeMealPort
                    .getPrinterPeripheralId());
            if (storeMealPort.getCallMealPortPeripherals() != null) {
                for (StoreMealPortPeripheral storeMealPortPeripheral : storeMealPort.getCallMealPortPeripherals()) {
                    peripheralIdList.add(storeMealPortPeripheral.getPeripheralId());
                }
            }
            map = this.facadeUtil.buildI5weiPeripheralDTOMap(storeMealPort.getMerchantId(), peripheralIdList);
        }
        return this.buildStoreMealPortDTO(storeMealPort, map);
    }

    public List<StoreMealPortDTO> buildStoreMealPortDTOs(int merchantId,
                                                         List<StoreMealPort>
                                                                 storeMealPorts, boolean loadPeripheral) throws TException {
        Map<Long, I5weiPeripheralDTO> map = null;
        if (loadPeripheral) {
            List<Long> peripheralIds = Lists.newArrayList();
            for (StoreMealPort storeMealPort : storeMealPorts) {
                peripheralIds.add(storeMealPort.getCallPeripheralId());
                peripheralIds.add(storeMealPort.getPrinterPeripheralId());
                if (storeMealPort.getCallMealPortPeripherals() != null) {
                    for (StoreMealPortPeripheral storeMealPortPeripheral : storeMealPort.getCallMealPortPeripherals()) {
                        peripheralIds.add(storeMealPortPeripheral.getPeripheralId());
                    }
                }
            }
            map = this.facadeUtil.buildI5weiPeripheralDTOMap(merchantId, peripheralIds);
        }
        List<StoreMealPortDTO> list = Lists.newArrayList();
        for (StoreMealPort storeMealPort : storeMealPorts) {
            list.add(this.buildStoreMealPortDTO(storeMealPort, map));
        }
        return list;
    }

    public StoreMealPortDTO buildStoreMealPortDTO(StoreMealPort storeMealPort,
                                                  Map<Long, I5weiPeripheralDTO> i5weiPeripheralDTOMap) throws TException {
        StoreMealPortDTO dto = new StoreMealPortDTO();
        BeanUtil.copy(storeMealPort, dto);
        if (storeMealPort.getStoreMealTask() != null) {
            StoreMealTaskDTO storeMealTaskDTO = new StoreMealTaskDTO();
            BeanUtil.copy(storeMealPort.getStoreMealTask(), storeMealTaskDTO);
            dto.setStoreMealTaskDTO(storeMealTaskDTO);
        }
        if (i5weiPeripheralDTOMap != null) {
            dto.setCallPeripheralDTO(i5weiPeripheralDTOMap.get(storeMealPort.getCallPeripheralId()));
            dto.setPrinterPeripheralDTO(i5weiPeripheralDTOMap.get(storeMealPort.getPrinterPeripheralId()));
            if (storeMealPort.getCallMealPortPeripherals() != null) {
                List<I5weiPeripheralDTO> peripheralDTOS = Lists.newArrayList();
                for (StoreMealPortPeripheral storeMealPortPeripheral : storeMealPort.getCallMealPortPeripherals()) {
                    I5weiPeripheralDTO pdto = i5weiPeripheralDTOMap.get(storeMealPortPeripheral.getPeripheralId());
                    if (pdto != null) {
                        peripheralDTOS.add(pdto);
                    }
                }
                dto.setCallPeripheralDTOs(peripheralDTOS);
            }
        }
        return dto;
    }
}
