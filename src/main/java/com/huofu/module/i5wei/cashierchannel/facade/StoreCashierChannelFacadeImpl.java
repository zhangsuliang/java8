package com.huofu.module.i5wei.cashierchannel.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.FacadeUtil;
import com.huofu.module.i5wei.cashierchannel.entity.StoreCashierChannel;
import com.huofu.module.i5wei.cashierchannel.entity.StoreCashierChannelChargeItem;
import com.huofu.module.i5wei.cashierchannel.service.StoreCashierChannelService;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.entity.StoreMealPortPeripheral;
import huofucore.facade.i5wei.cashierchannel.*;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.mealport.StoreMealTaskDTO;
import huofucore.facade.i5wei.peripheral.I5weiPeripheralDTO;
import huofucore.facade.merchant.peripheral.PeripheralDTO;
import huofucore.facade.merchant.peripheral.PeripheralFacade;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.text.PlainDocument;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by cherie on 2017/1/4.
 */

@Component
@ThriftServlet(name = "storeCashierChannelFacadeServlet", serviceClass = StoreCashierChannelFacade.class)
public class StoreCashierChannelFacadeImpl implements StoreCashierChannelFacade.Iface {

    @Resource
    private StoreCashierChannelService storeCashierChannelService;

    @Autowired
    private FacadeUtil facadeUtil;

    @Override
    public StoreCashierChannelDTO saveStoreCashierChannel(StoreCashierChannelParam param) throws T5weiException, TException {
        this.validate4Save(param);
        StoreCashierChannel storeCashierChannel = this.storeCashierChannelService.saveStoreCashierChannel(param);
        List<StoreCashierChannel> list = Lists.newArrayList(storeCashierChannel);
        this.storeCashierChannelService.buildChannelInfo(param.getMerchantId(), param.getStoreId(), list, false);
        return this.buildStoreCashierChannelDTO(storeCashierChannel);
    }

    @Override
    public List<StoreCashierChannelDTO> getStoreCashierChannelsByCashierId(int merchantId, long storeId, long cashierId) throws TException {
        List<StoreCashierChannel> storeCashierChannels = this.storeCashierChannelService.getStoreCashierChannelsByCashierId(merchantId, storeId, cashierId);
        return this.buildStoreCashierChannelDTOs(storeCashierChannels);
    }

    @Override
    public void deleteStoreCashierChannel(int merchantId, long storeId, long channelId) throws TException {
        this.storeCashierChannelService.deleteStoreCashierChannel(merchantId, storeId, channelId);
    }

    /**
     * 获取扫码台信息
     */
    private Map<Long, I5weiPeripheralDTO> getI5weiPeripheralDTOMap(int merchantId, List<StoreCashierChannel> storeCashierChannels) throws TException {
        List<Long> peripheralIds = Lists.newArrayList();
        for (StoreCashierChannel storeCashierChannel : storeCashierChannels) {
            merchantId = storeCashierChannel.getMerchantId();
            if (storeCashierChannel.getScanPeripheralId() > 0) {
                peripheralIds.add(storeCashierChannel.getScanPeripheralId());
            }
        }
        return this.facadeUtil.buildI5weiPeripheralDTOMap(merchantId, peripheralIds);
    }

    private StoreCashierChannelDTO buildStoreCashierChannelDTO(StoreCashierChannel storeCashierChannel) throws TException {
        Map<Long, I5weiPeripheralDTO> i5weiPeripheralDTOMap = this.facadeUtil.buildI5weiPeripheralDTOMap(storeCashierChannel.getMerchantId(), Lists.newArrayList(storeCashierChannel.getScanPeripheralId()));
        return this.buildStoreCashierChannelDTO(storeCashierChannel, i5weiPeripheralDTOMap);
    }

    private StoreCashierChannelDTO buildStoreCashierChannelDTO(StoreCashierChannel storeCashierChannel, Map<Long, I5weiPeripheralDTO> i5weiPeripheralDTOMap) {
        StoreCashierChannelDTO dto = new StoreCashierChannelDTO();
        BeanUtil.copy(storeCashierChannel, dto);
        if (storeCashierChannel.getChargeItems() != null) {
            for (StoreCashierChannelChargeItem channelChargeItem : storeCashierChannel.getChargeItems()) {
                StoreCashierChannelChargeItemDTO channelChargeItemDTO = new StoreCashierChannelChargeItemDTO();
                BeanUtil.copy(channelChargeItem, channelChargeItemDTO);
                if (channelChargeItem.getStoreChargeItem() != null) {
                    channelChargeItemDTO.setChargeItemName(channelChargeItem.getStoreChargeItem().getName());
                    channelChargeItemDTO.setPrice(channelChargeItem.getStoreChargeItem().getCurPrice());
                }
                dto.addToChargeItemDTOs(channelChargeItemDTO);
            }
        }
        if (storeCashierChannel.getScanPeripheralId() > 0) {
            dto.setScanPeripheralDTO(i5weiPeripheralDTOMap.get(storeCashierChannel.getScanPeripheralId()));
        }
        return dto;
    }

    private List<StoreCashierChannelDTO> buildStoreCashierChannelDTOs(List<StoreCashierChannel> storeCashierChannels) throws TException {
        List<StoreCashierChannelDTO> dtos = Lists.newArrayList();
        if (storeCashierChannels == null || storeCashierChannels.isEmpty()) {
            return dtos;
        }
        Map<Long, I5weiPeripheralDTO> i5weiPeripheralDTOMap = this.getI5weiPeripheralDTOMap(storeCashierChannels.get(0).getMerchantId(), storeCashierChannels);
        for (StoreCashierChannel storeCashierChannel : storeCashierChannels) {
            dtos.add(this.buildStoreCashierChannelDTO(storeCashierChannel, i5weiPeripheralDTOMap));
        }
        return dtos;
    }

    @Override
    public List<StoreCashierChannelTradeSummaryDTO> getStoreCashierChannelTradeSummary(int merchantId, long storeId, long timeBucketId, long cashierId, long repastDate) throws T5weiException, TException {

        this.validate(merchantId, storeId, timeBucketId);
        List<StoreCashierChannelTradeSummaryDTO> dtos = this.storeCashierChannelService.getStoreCashierTradeSummary(merchantId, storeId, timeBucketId, cashierId, repastDate);
        return dtos;
    }

    private void validate(int merchantId, long storeId, long timeBucketId) throws T5weiException {
        if (merchantId == 0 || storeId == 0 || timeBucketId == 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + merchantId + "], storeId[" + storeId + "], timeBucketId[" + timeBucketId + " is invalid");
        }
    }

    private void validate4Save(StoreCashierChannelParam param) throws T5weiException {
        String prefix = "merchantId[" + param.getMerchantId() + "] storeId[" + param.getStoreId() + "]";
        if (DataUtil.isEmpty(param.getName())) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CASHIER_CHANNEL_NAME_INVALID.getValue(), prefix + " name[" + param.getName() + "] must be not empty");
        }
        if (param.getName().length() > 20) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CASHIER_CHANNEL_NAME_INVALID.getValue(), prefix + " name[" + param.getName() + "] length must <=20");
        }
    }

    @Override
    public void deleteStoreCashierChannelsByCashierId(int merchantId, long storeId, long cashierId) throws TException {
        this.storeCashierChannelService.deleteStoreCashierChannelsByCashierId(merchantId, storeId, cashierId);
    }
}
