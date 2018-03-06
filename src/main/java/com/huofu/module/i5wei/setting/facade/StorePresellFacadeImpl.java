package com.huofu.module.i5wei.setting.facade;

import com.huofu.module.i5wei.setting.entity.StorePresellSetting;
import com.huofu.module.i5wei.setting.service.StorePresellService;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.presell.StorePresellFacade;
import huofucore.facade.i5wei.presell.StorePresellSettingDTO;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ThriftServlet(name = "storePresellFacadeServlet", serviceClass = StorePresellFacade.class)
@Component
public class StorePresellFacadeImpl implements StorePresellFacade.Iface {

    @Autowired
    private StorePresellService storePresellService;

    @Override
    public StorePresellSettingDTO getStorePresellSetting(int merchantId, long storeId) throws T5weiException, TException {
        if (merchantId == 0 || storeId == 0) {
            return new StorePresellSettingDTO();
        }
        StorePresellSetting storePresellSetting = storePresellService.getStorePresellSetting(merchantId, storeId);
        if (storePresellSetting == null) {
            return new StorePresellSettingDTO();
        }
        return BeanUtil.copy(storePresellSetting, StorePresellSettingDTO.class);
    }

    @Override
    public StorePresellSettingDTO updateStorePresellSetting(StorePresellSettingDTO storePresellSettingDTO) throws T5weiException, TException {
        storePresellService.updateStorePresellSetting(storePresellSettingDTO);
        return this.getStorePresellSetting(storePresellSettingDTO.getMerchantId(), storePresellSettingDTO.getStoreId());
    }

}
