package com.huofu.module.i5wei.cashierchannel.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.cashierchannel.entity.StoreCashierChannel;
import com.huofu.module.i5wei.cashierchannel.entity.StoreCashierChannelChargeItem;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cherie on 2017/1/4.
 */
@Repository
public class StoreCashierChannelChargeItemDAO extends AbsQueryDAO<StoreCashierChannelChargeItem> {

    public int deleteByChannelId(int merchantId, long storeId, long channelId) {
        return this.query.delete(StoreCashierChannelChargeItem.class,
                "where merchant_id=? and store_id=? and channel_id=?",
                new Object[]{merchantId, storeId, channelId});
    }

    public void deleteBatch(List<StoreCashierChannelChargeItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        List<Object[]> olist = Lists.newArrayList();
        Set<Long> channelIds = Sets.newHashSet();
        for (StoreCashierChannelChargeItem item : items) {
            olist.add(new Object[]{item.getMerchantId(), item.getStoreId(), item.getChannelId(), item.getChargeItemId()});
            channelIds.add(item.getChannelId());
        }
        this.query.batchDelete(StoreCashierChannelChargeItem.class, "where merchant_id=? and store_id=? and channel_id=? and charge_item_id=?", olist);
    }

    public Map<Long, List<StoreCashierChannelChargeItem>> getMapInCashierChannelIds(int merchantId, long storeId, List<Long> channelIds) {
        Map<Long, List<StoreCashierChannelChargeItem>> map = Maps.newHashMap();
        if (channelIds == null || channelIds.isEmpty()) {
            return map;
        }
        List<StoreCashierChannelChargeItem> list = this.query.listInValues2(StoreCashierChannelChargeItem.class,
                "where merchant_id=? and store_id=?", "channel_id", "order by tid asc", Lists.newArrayList(merchantId, storeId), channelIds);
        for (StoreCashierChannelChargeItem obj : list) {
            List<StoreCashierChannelChargeItem> sublist = map.get(obj.getChannelId());
            if (sublist == null) {
                sublist = Lists.newArrayList();
                map.put(obj.getChannelId(), sublist);
            }
            sublist.add(obj);
        }
        return map;
    }

    /**
     * 根据收款线id，批量删除收款线下的收费项目
     */
     public void deleteBatchByChannelId(int merchantId, long storeId, List<Long> channelIds) {
        if (channelIds == null || channelIds.isEmpty()) {
            return;
        }
        List<Object[]> olist = Lists.newArrayList();
        for (long channelId : channelIds) {
            olist.add(new Object[]{merchantId, storeId, channelId});
        }
        this.query.batchDelete(StoreCashierChannelChargeItem.class, "where merchant_id=? and store_id=? and channel_id=?", olist);
    }
}
