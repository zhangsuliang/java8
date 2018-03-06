package com.huofu.module.i5wei.cashierchannel.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.cashierchannel.entity.StoreCashierChannel;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebateChargeItem;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by cherie on 2017/1/4.
 */
@Repository
public class StoreCashierChannelDAO extends AbsQueryDAO<StoreCashierChannel> {

    public int count4Avaliable(int merchantId, long storeId, long cashierId) {
        return this.query.count(StoreCashierChannel.class,
                "where merchant_id=? and store_id=? and cashier_id=?",
                new Object[]{merchantId, storeId, cashierId});
    }

    public StoreCashierChannel loadById(int merchantId, long storeId, long channelId) throws T5weiException {
        StoreCashierChannel obj = this.query.objById(StoreCashierChannel.class, channelId);
        if (obj == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CASHIER_CHANNEL_INVALID.getValue(),
                    "merchantId[" + merchantId + "] storeId[" + storeId + "] channelId[" + channelId + "] invalid");
        }
        return obj;
    }

    /**
     * 查找名称重复的收款线
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param cashierId  收银台id
     * @param name       名称
     * @param exceptedId 忽略的扫码线id
     * @return true:有标题重名的扫码线
     */
    public boolean hasDuplicateNameInAvaliable(int merchantId, long storeId, long cashierId, String name, long exceptedId) {
        int val = this.query.count(StoreCashierChannel.class,
                "where merchant_id=? and store_id=? and cashier_id=? and name=? and channel_id!=?",
                new Object[]{merchantId, storeId, cashierId, name, exceptedId});
        return val > 0;
    }

    /**
     * 查找扫码台重复的收款线
     *
     * @param merchantId       商户id
     * @param storeId          店铺id
     * @param cashierId        收银台id
     * @param scanPeripheralId 扫码台id
     * @param exceptedId       忽略的扫码下id
     * @return true:有扫码台重复的收款线
     */
    public boolean hasDuplicateScanPeripheralInavaliable(int merchantId, long storeId, long cashierId, long scanPeripheralId, long exceptedId) {
        int val = this.query.count(StoreCashierChannel.class,
                "where merchant_id=? and store_id=? and cashier_id=? and scan_peripheral_id=? and channel_id!=?",
                new Object[]{merchantId, storeId, cashierId, scanPeripheralId, exceptedId});
        return val > 0;
    }

    public int deleteById(int merchantId, long storeId, long channelId) {
        return this.query.delete(StoreCashierChannel.class,
                "where merchant_id=? and store_id=? and channel_id=?",
                new Object[]{merchantId, storeId, channelId});
    }

    public List<StoreCashierChannel> getListByCashierId(int merchantId, long storeId, long cashierId) {
        return this.query.list(StoreCashierChannel.class, "where merchant_id=? and store_id=? and cashier_id=? order by channel_id asc",
                new Object[]{merchantId, storeId, cashierId});
    }
    
    /**
     * 根据收银台id，删除收款线
     */
    public int deleteByCashierId(int merchantId, long storeId, long cashierId){
        return this.query.delete(StoreCashierChannel.class,
                "where merchant_id=? and store_id=? and cashier_id=?",
                new Object[]{merchantId, storeId, cashierId});
    }
    
}
