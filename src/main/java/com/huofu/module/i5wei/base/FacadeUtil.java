package com.huofu.module.i5wei.base;

import com.google.common.collect.Maps;
import huofucore.facade.i5wei.peripheral.I5weiPeripheralDTO;
import huofucore.facade.i5wei.peripheral.I5weiPeripheralIposDTO;
import huofucore.facade.merchant.peripheral.PeripheralDTO;
import huofucore.facade.merchant.peripheral.PeripheralFacade;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FacadeUtil {

    @ThriftClient
    private PeripheralFacade.Iface peripheralFacadeIface;

    public Map<Long, I5weiPeripheralDTO> buildI5weiPeripheralDTOMap(int
                                                                            merchantId,
                                                                    List<Long>
                                                                            peripheralIds) throws TException {
        List<PeripheralDTO> peripheralDTOs = this.peripheralFacadeIface
                .getPeripheralListInIds(merchantId, peripheralIds, true);
        Map<Long, I5weiPeripheralDTO> map = Maps.newHashMap();
        for (PeripheralDTO peripheralDTO : peripheralDTOs) {
            I5weiPeripheralDTO dto = this.buildI5weiPeripheralDTO(peripheralDTO);
            map.put(dto.getPeripheralId(), dto);
        }
        return map;
    }

    private I5weiPeripheralDTO buildI5weiPeripheralDTO(PeripheralDTO
                                                               peripheralDTO) {
        I5weiPeripheralDTO dto = new I5weiPeripheralDTO();
        BeanUtil.copy(peripheralDTO, dto);
        if (peripheralDTO.getPeripheralIposDTO() != null) {
            I5weiPeripheralIposDTO iposDTO = new I5weiPeripheralIposDTO();
            BeanUtil.copy(peripheralDTO.getPeripheralIposDTO(), iposDTO);
            dto.setI5weiPeripheralIposDTO(iposDTO);
        }
        return dto;
    }
}
