package com.huofu.module.i5wei.menu.facade;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.menu.entity.StoreDateBizSetting;
import com.huofu.module.i5wei.menu.entity.StoreDateTimeBucketSetting;
import com.huofu.module.i5wei.menu.service.StoreMenuService;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreDateBizSettingDTO;
import huofucore.facade.i5wei.menu.StoreDateBizSettingParam;
import huofucore.facade.i5wei.menu.StoreDateTimeBucketSettingDTO;
import huofucore.facade.i5wei.menu.StoreDateTimeBucketSettingParam;
import huofucore.facade.i5wei.menu.StoreMenuFacade;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;

/**
 * Created by akwei on 8/28/15.
 */
@Component
@ThriftServlet(name = "storeMenuFacadeServlet", serviceClass = StoreMenuFacade.class)
public class StoreMenuFacadeImpl implements StoreMenuFacade.Iface {

    @Autowired
    private StoreMenuService storeMenuService;

    @Override
    public StoreDateBizSettingDTO saveStoreDateBizSetting(StoreDateBizSettingParam param) throws T5weiException, TException {
        StoreDateBizSetting storeDateBizSetting = this.storeMenuService
                .saveStoreDateBizSetting(param);
        return this.buildStoreDateBizSettingDTO(storeDateBizSetting);
    }

    @Override
    public StoreDateTimeBucketSettingDTO saveStoreDateTimeBucketSetting(StoreDateTimeBucketSettingParam param) throws T5weiException, TException {
        StoreDateTimeBucketSetting storeDateTimeBucketSetting = this
                .storeMenuService.saveStoreDateTimeBucketSetting(param);
        return this.buildStoreDateTimeBucketSettingDTO
                (storeDateTimeBucketSetting);
    }

    @Override
    public StoreDateBizSettingDTO getStoreDateBizSetting(int merchantId, long storeId, long selectedDate) throws T5weiException, TException {
        StoreDateBizSetting storeDateBizSetting = this.storeMenuService.loadStoreDateBizSettingForSelectedDate(merchantId, storeId, selectedDate);
        return this.buildStoreDateBizSettingDTO(storeDateBizSetting);
    }

    @Override
    public StoreDateTimeBucketSettingDTO getStoreDateTimeBucketSetting(int merchantId, long storeId, long selectedDate, long timeBucketId) throws T5weiException, TException {
        StoreDateTimeBucketSetting setting = this.storeMenuService.loadStoreDateTimeBucketSettingForSelecteDate(merchantId, storeId, selectedDate, timeBucketId);
        return this.buildStoreDateTimeBucketSettingDTO(setting);
    }

    @Override
    public List<StoreDateTimeBucketSettingDTO> getStoreDateTimeBucketSettings(int merchantId, long storeId, long selectedDate) throws TException {
        List<StoreDateTimeBucketSetting> list = this.storeMenuService.getStoreDateTimeBucketSettingsForSelectedDate(merchantId, storeId,
                selectedDate);
        return list.stream().map(this::buildStoreDateTimeBucketSettingDTO).collect(Collectors.toList());
    }

    @Override
    public List<StoreDateBizSettingDTO> getStoreDateBizSettings(int merchantId, long storeId, long minDate, long maxDate) throws TException {
        List<StoreDateBizSetting> list = this.storeMenuService
                .getStoreDateBizSettings(merchantId, storeId, minDate, maxDate);
        return list.stream().map(this::buildStoreDateBizSettingDTO).collect
                (Collectors.toList());
    }

    private StoreDateBizSettingDTO buildStoreDateBizSettingDTO(StoreDateBizSetting storeDateBizSetting) {
        StoreDateBizSettingDTO storeDateBizSettingDTO = new
                StoreDateBizSettingDTO();
        BeanUtil.copy(storeDateBizSetting, storeDateBizSettingDTO);
        return storeDateBizSettingDTO;
    }

    private StoreDateTimeBucketSettingDTO buildStoreDateTimeBucketSettingDTO(StoreDateTimeBucketSetting storeDateTimeBucketSetting) {
        StoreDateTimeBucketSettingDTO storeDateTimeBucketSettingDTO = new
                StoreDateTimeBucketSettingDTO();
        BeanUtil.copy(storeDateTimeBucketSetting, storeDateTimeBucketSettingDTO);
        return storeDateTimeBucketSettingDTO;
    }
}
