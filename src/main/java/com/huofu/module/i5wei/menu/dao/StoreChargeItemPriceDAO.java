package com.huofu.module.i5wei.menu.dao;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPrice;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.thrift.ThriftInvoker;
import huofuhelper.util.thrift.ThriftParam;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreChargeItemPriceDAO extends AbsQueryDAO<StoreChargeItemPrice> {

    @Autowired
    private IdMakerUtil idMakerUtil;

    private static final String id_key = "store_charge_item_price";

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    private long nextId() {
        return this.idMakerUtil.nextId(id_key);
    }

    @Override
    public void create(StoreChargeItemPrice storeChargeItemPrice) {
        storeChargeItemPrice.setItemPriceId(this.nextId());
        super.create(storeChargeItemPrice);
    }

    @Override
    public void update(StoreChargeItemPrice storeChargeItemPrice, StoreChargeItemPrice snapshot) {
        this.addDbRouteInfo(storeChargeItemPrice.getMerchantId(), storeChargeItemPrice.getStoreId());
        super.update(storeChargeItemPrice, snapshot);
    }

    public List<StoreChargeItemPrice> batchCreate(List<StoreChargeItemPrice> list, List<Long> ids) {
        if (list.isEmpty()) {
            return list;
        }
        int merchantId = list.get(0).getMerchantId();
        long storeId = list.get(0).getStoreId();
        this.addDbRouteInfo(merchantId, storeId);
        List<Long> idList = ids;
        if (idList == null) {
            idList = this.getIds(list.size(), null);
        }
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setItemPriceId(idList.get(i));
        }
        return super.batchCreate(list);
    }

    public List<Long> createIds(int size) {
        ThriftParam thriftParam = new ThriftParam();
        thriftParam.setSoTimeout(15000);
        return getIds(size, thriftParam);
    }

    private List<Long> getIds(int size, ThriftParam thriftParam) {
        try {
            List<Long> idList = Lists.newArrayList();
            int fixedSize = 500;
            if (size > fixedSize) {
                int sizea = size / fixedSize;
                int sizeb = size % fixedSize;
                for (int i = 0; i < sizea; i++) {
                    ThriftInvoker.invoke(thriftParam, () -> {
                        idList.addAll(idMakerUtil.nextIds(id_key, fixedSize));
                        return null;
                    });
                }
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.idMakerUtil.nextIds(id_key, sizeb));
                    return null;
                });
            } else {
                ThriftInvoker.invoke(thriftParam, () -> {
                    idList.addAll(this.idMakerUtil.nextIds(id_key, size));
                    return null;
                });
            }
            return idList;
        } catch (TException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获得指定日期中收费项目的有效的价格信息
     *
     * @return map key=chargeItmeId value=StoreChargeItemPrice
     */
    public Map<Long, List<StoreChargeItemPrice>> getMapGroupByChargeItemIdsForCurAndNext(int merchantId, long storeId, List<Long> chargeItemIds, long time, boolean enableSlave, boolean enableCache) {
        if (chargeItemIds.isEmpty()) {
            return new HashMap<>(0);
        }
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StoreChargeItemPrice> storeChargeItemPrices = this.query.listInValues2(StoreChargeItemPrice.class, "where store_id=? and end_time>?", "charge_item_id", Lists.newArrayList(storeId, time), chargeItemIds);
        Map<Long, List<StoreChargeItemPrice>> map = Maps.newHashMap();
        for (StoreChargeItemPrice price : storeChargeItemPrices) {
            List<StoreChargeItemPrice> list = map.get(price.getChargeItemId());
            if (list == null) {
                list = Lists.newArrayList();
                map.put(price.getChargeItemId(), list);
            }
            list.add(price);
        }
        return map;
    }

    public Map<Long, List<StoreChargeItemPrice>> getMapGroupByChargeItemIdsForCur(int merchantId, long storeId, List<Long> chargeItemIds, long time, boolean enableSlave, boolean enableCache) {
        if (chargeItemIds.isEmpty()) {
            return new HashMap<>(0);
        }
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        List<StoreChargeItemPrice> storeChargeItemPrices = this.query.listInValues2(StoreChargeItemPrice.class, "where store_id=? and begin_time<=? and end_time>?", "charge_item_id", Lists.newArrayList(storeId, time, time), chargeItemIds);
        Map<Long, List<StoreChargeItemPrice>> map = Maps.newHashMap();
        for (StoreChargeItemPrice price : storeChargeItemPrices) {
            List<StoreChargeItemPrice> list = map.get(price.getChargeItemId());
            if (list == null) {
                list = Lists.newArrayList();
                map.put(price.getChargeItemId(), list);
            }
            list.add(price);
        }
        return map;
    }

    public List<StoreChargeItemPrice> getList4Valid(int merchantId, long storeId, long chargeItemId, long time) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreChargeItemPrice.class, "where store_id=? and charge_item_id=? and begin_time<=? and end_time>=?", new Object[]{storeId, chargeItemId, time, time});
    }

    public StoreChargeItemPrice loadById(int merchantId, long storeId, long itemPriceId) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        StoreChargeItemPrice storeChargeItemPrice = this.query.objById(StoreChargeItemPrice.class, itemPriceId);
        if (storeChargeItemPrice == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_PRICE_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] itemPriceId[" + itemPriceId + "] invalid");
        }
        return storeChargeItemPrice;
    }

    public int updateEndTimeByChargeItemIdInExpiryDate(int merchantId, long storeId, long chargeItemId, long time, long newEndTime) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreChargeItemPrice.class, "set end_time=? where store_id=? and charge_item_id=? and begin_time<? and end_time>=?", new Object[]{newEndTime, storeId, chargeItemId, time, time});
    }

    public int updateEndTimeByChargeItemIdAndNotEqPriceInExpiryDate(int merchantId, long storeId, long chargeItemId, long price, long time, long newEndTime) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreChargeItemPrice.class, "set end_time=? where store_id=? and charge_item_id=? and price!=? and begin_time<? and end_time>=?", new Object[]{newEndTime, storeId, chargeItemId, price, time, time});
    }

    public int makeDeletedByChargeItemId(int merchantId, long storeId, long chargeItemId, boolean isDelete) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.update(StoreChargeItemPrice.class, "set deleted=? where store_id=? and charge_item_id=?", new Object[]{isDelete, storeId, chargeItemId});
    }

    /**
     * 删除未来生效的价格
     *
     * @param merchantId
     * @param storeId
     * @param chargeItemId
     * @param now
     * @return
     */
    public int deleteByChargeItemIdForFuture(int merchantId, long storeId, long chargeItemId, long now) {
        this.addDbRouteInfo(merchantId, storeId);
        MutableDateTime mdt = new MutableDateTime(now);
        mdt.addDays(1);
	    mdt.setMillisOfDay(0);
	    long time = mdt.getMillis();
        return this.query.delete(StoreChargeItemPrice.class, "where store_id=? and charge_item_id=? and begin_time>=?", new Object[]{storeId, chargeItemId, time});
    }

    /**
     * 删除指定收费项目和生效时间的数据
     *
     * @param merchantId
     * @param storeId
     * @param chargeItemId
     * @param beginTime
     * @return
     */
    public int deleteByChargeItemIdAndBeginTime(int merchantId, long storeId, long chargeItemId, long beginTime) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.delete(StoreChargeItemPrice.class, "where store_id=? and charge_item_id=? and begin_time=?", new Object[]{storeId, chargeItemId, beginTime});
    }

}
