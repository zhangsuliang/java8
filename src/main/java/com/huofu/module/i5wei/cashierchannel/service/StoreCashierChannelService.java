package com.huofu.module.i5wei.cashierchannel.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.cashierchannel.dao.StoreCashierChannelChargeItemDAO;
import com.huofu.module.i5wei.cashierchannel.dao.StoreCashierChannelDAO;
import com.huofu.module.i5wei.cashierchannel.entity.StoreCashierChannel;
import com.huofu.module.i5wei.cashierchannel.entity.StoreCashierChannelChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.order.service.StoreOrderStatService;
import huofucore.facade.i5wei.cashierchannel.StoreCashierChannelParam;
import huofucore.facade.i5wei.cashierchannel.StoreCashierChannelTradeSummaryDTO;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderStatDTO;
import huofuhelper.util.DateUtil;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cherie on 2017/1/4.
 */
@Service
public class StoreCashierChannelService {

    @Resource
    private StoreCashierChannelDAO storeCashierChannelDAO;

    @Resource
    private StoreCashierChannelChargeItemDAO storeCashierChannelChargeItemDAO;

    @Resource
    private StoreChargeItemService storeChargeItemService;

    @Resource
    private StoreOrderStatService storeOrderStatService;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public StoreCashierChannel saveStoreCashierChannel(StoreCashierChannelParam param) throws T5weiException, TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long cashierId = param.getCashierId();
        long scanPeripheralId = param.getScanPeripheralId();
        long channelId = param.getChannelId();
        String name = param.getName();
        // 检查名称是否重复
        if (this.storeCashierChannelDAO.hasDuplicateNameInAvaliable(merchantId, storeId, cashierId, name, channelId)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CASHIER_CHANNEL_NAME_DUPLICATE.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] cashierId[" + cashierId + "] name[" + name + "] duplicate");
        }
        // 检查扫码台是否重复
        if (this.storeCashierChannelDAO.hasDuplicateScanPeripheralInavaliable(merchantId, storeId, cashierId, scanPeripheralId, channelId)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CASHIER_CHANNEL_SCAN_PERIPHERAL_DUPLICATE.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] cashierId[" + cashierId + "] scanPeripheralId[" + scanPeripheralId + "] duplicate");
        }
        StoreCashierChannel storeCashierChannel;
        if (channelId > 0) {
            storeCashierChannel = this.storeCashierChannelDAO.loadById(merchantId, storeId, channelId);
        } else {
            // 检查数量是否超出限制
            if (this.storeCashierChannelDAO.count4Avaliable(merchantId, storeId, cashierId) >= 4) {
                throw new T5weiException(T5weiErrorCodeType.STORE_CASHIER_CHANNEL_NUM_LIMIT.getValue(),
                        "merchantId[" + merchantId + "] storeId[" + storeId + "] cashierId[" + cashierId + "] cashierChannel num limit");
            }
            storeCashierChannel = new StoreCashierChannel();
        }
        long time = System.currentTimeMillis();
        storeCashierChannel.setMerchantId(merchantId);
        storeCashierChannel.setStoreId(storeId);
        storeCashierChannel.setCashierId(cashierId);
        storeCashierChannel.setScanPeripheralId(scanPeripheralId);
        storeCashierChannel.setName(name);
        storeCashierChannel.setUpdateTime(time);
        if (channelId > 0) {
            storeCashierChannel.update();
        } else {
            storeCashierChannel.setCreateTime(time);
            storeCashierChannel.create();
        }
        storeCashierChannel.setChargeItems(this._saveStoreCashierChannelChargeItems(merchantId, storeId, storeCashierChannel.getChannelId(), param.getChargeItemIds()));
        return storeCashierChannel;
    }

    public List<StoreCashierChannel> getStoreCashierChannelsByCashierId(int merchantId, long storeId, long cashierId) throws TException {
        List<StoreCashierChannel> cashierChannels = this.storeCashierChannelDAO.getListByCashierId(merchantId, storeId, cashierId);
        this.buildChannelInfo(merchantId, storeId, cashierChannels, true);
        return cashierChannels;
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteStoreCashierChannel(int merchantId, long storeId, long channelId) throws TException {
        this.storeCashierChannelDAO.deleteById(merchantId, storeId, channelId);
        this.storeCashierChannelChargeItemDAO.deleteByChannelId(merchantId, storeId, channelId);
    }

    private List<StoreCashierChannelChargeItem> _saveStoreCashierChannelChargeItems(
            int merchantId, long storeId, long channelId, List<Long> chargeItemIds) {
        this.storeCashierChannelChargeItemDAO.deleteByChannelId(merchantId, storeId, channelId);
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return null;
        }
        List<StoreCashierChannelChargeItem> list = Lists.newArrayList();
        List<Long> _list = this.filterDuplicateId(chargeItemIds);
        long createTime = System.currentTimeMillis();
        for (Long chargeItemId : _list) {
            StoreCashierChannelChargeItem item = new StoreCashierChannelChargeItem();
            item.setMerchantId(merchantId);
            item.setStoreId(storeId);
            item.setChannelId(channelId);
            item.setChargeItemId(chargeItemId);
            item.setCreateTime(createTime);
            list.add(item);
        }
        this.storeCashierChannelChargeItemDAO.batchCreate(list);
        return list;
    }

    private List<Long> filterDuplicateId(List<Long> ids) {
        if (ids == null) {
            return null;
        }
        if (ids.isEmpty()) {
            return ids;
        }
        Set<Long> idSet = Sets.newHashSet();
        List<Long> _list = Lists.newArrayList();
        for (Long id : ids) {
            if (idSet.contains(id)) {
                continue;
            }
            _list.add(id);
            idSet.add(id);
        }
        return _list;
    }

    public List<StoreCashierChannel> buildChannelInfo(int merchantId, long storeId, List<StoreCashierChannel> cashierChannels, boolean loadChannelRefInfo) {
        if (loadChannelRefInfo) {
            this.buildChannelRefInfo(merchantId, storeId, cashierChannels);
        }
        this.buildChargeItemInfo(cashierChannels);
        this.filterChargeItem4Delete(cashierChannels);
        return cashierChannels;
    }

    private void buildChannelRefInfo(int merchantId, long storeId, List<StoreCashierChannel> cashierChannels) {
        List<Long> ids = StoreCashierChannel.getIdList(cashierChannels);
        Map<Long, List<StoreCashierChannelChargeItem>> cashierChannelListMap = this.storeCashierChannelChargeItemDAO.getMapInCashierChannelIds(merchantId, storeId, ids);
        for (StoreCashierChannel cashierchannel : cashierChannels) {
            cashierchannel.setChargeItems(cashierChannelListMap.get(cashierchannel.getChannelId()));
        }
    }

    private void buildChargeItemInfo(List<StoreCashierChannel> storeCashierChannels) {
        if (storeCashierChannels == null || storeCashierChannels.isEmpty()) {
            return;
        }
        int merchantId = storeCashierChannels.get(0).getMerchantId();
        long storeId = storeCashierChannels.get(0).getStoreId();
        List<Long> chargeItemIds = Lists.newArrayList();
        for (StoreCashierChannel cashierChannel : storeCashierChannels) {
            if (cashierChannel.getChargeItems() != null) {
                for (StoreCashierChannelChargeItem channelChargeItem : cashierChannel.getChargeItems()) {
                    chargeItemIds.add(channelChargeItem.getChargeItemId());
                }
            }
        }
        long time = System.currentTimeMillis();
        Map<Long, StoreChargeItem> chargeItemMap = this.storeChargeItemService.getStoreChargeItemMapInIds(merchantId, storeId, chargeItemIds, time, true, true);
        for (StoreCashierChannel storeCashierChannel : storeCashierChannels) {
            if (storeCashierChannel.getChargeItems() != null) {
                for (StoreCashierChannelChargeItem channelChargeItem : storeCashierChannel.getChargeItems()) {
                    channelChargeItem.setStoreChargeItem(chargeItemMap.get(channelChargeItem.getChargeItemId()));
                }
            }
        }
    }

    private void filterChargeItem4Delete(List<StoreCashierChannel> storeCashierChannels) {
        if (storeCashierChannels == null || storeCashierChannels.isEmpty()) {
            return;
        }
        List<StoreCashierChannelChargeItem> list4Del = Lists.newArrayList();
        for (StoreCashierChannel storeCashierChannel : storeCashierChannels) {
            if (storeCashierChannel.getChargeItems() == null || storeCashierChannel.getChargeItems().isEmpty()) {
                continue;
            }
            Iterator<StoreCashierChannelChargeItem> it = storeCashierChannel.getChargeItems().iterator();
            while (it.hasNext()) {
                StoreCashierChannelChargeItem next = it.next();
                if (next.getStoreChargeItem() == null || next.getStoreChargeItem().isDeleted()) {
                    it.remove();
                    list4Del.add(next);
                }
            }
        }
        this.storeCashierChannelChargeItemDAO.deleteBatch(list4Del);
    }

    public List<StoreCashierChannelTradeSummaryDTO> getStoreCashierTradeSummary(int merchantId, long storeId, long timeBucketId, long cashierId, long repastDate) throws T5weiException, TException {
        List<StoreCashierChannelTradeSummaryDTO> resultDTOs = Lists.newArrayList();
        List<StoreCashierChannel> storeCashierChannels = this.storeCashierChannelDAO.getListByCashierId(merchantId, storeId, cashierId);
        List<Long> channelIds = StoreCashierChannel.getIdList(storeCashierChannels);
        if (channelIds == null || channelIds.isEmpty()) {
            return resultDTOs;
        }
        List<StoreOrderStatDTO> storeOrderStatDTOs = this.storeOrderStatService.getStoreOrderChannelTradeSummaryStat(merchantId, storeId, repastDate, timeBucketId, channelIds);

        for (StoreOrderStatDTO storeOrderStatDTO : storeOrderStatDTOs) {
            StoreCashierChannelTradeSummaryDTO dto = new StoreCashierChannelTradeSummaryDTO();
            dto.setChannelId(storeOrderStatDTO.getCashierChannelId());
            dto.setTotalPayment(storeOrderStatDTO.getTotalPayment());
            dto.setTotalOrders(storeOrderStatDTO.getTotalOrders());
            resultDTOs.add(dto);
        }
        return resultDTOs;
    }

    /**
     * 根据收银台id，删除收款线
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteStoreCashierChannelsByCashierId(int merchantId, long storeId, long cashierId) {
        List<StoreCashierChannel> cashierChannelList = this.storeCashierChannelDAO.getListByCashierId(merchantId, storeId, cashierId);
        List<Long> ids = StoreCashierChannel.getIdList(cashierChannelList);
        this.storeCashierChannelDAO.deleteByCashierId(merchantId, storeId, cashierId);
        this.storeCashierChannelChargeItemDAO.deleteBatchByChannelId(merchantId, storeId, ids);
    }
}
