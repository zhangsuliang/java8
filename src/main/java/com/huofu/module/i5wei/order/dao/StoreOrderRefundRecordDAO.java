package com.huofu.module.i5wei.order.dao;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundRecord;

@Repository
public class StoreOrderRefundRecordDAO extends AbsQueryDAO<StoreOrderRefundRecord> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreOrderRefundRecord storeOrderRefundRecord) {
        this.addDbRouteInfo(storeOrderRefundRecord.getMerchantId(), storeOrderRefundRecord.getStoreId());
        super.create(storeOrderRefundRecord);
    }

    @Override
    public void replace(StoreOrderRefundRecord storeOrderRefundRecord) {
        this.addDbRouteInfo(storeOrderRefundRecord.getMerchantId(), storeOrderRefundRecord.getStoreId());
        super.replace(storeOrderRefundRecord);
    }

    @Override
    public void update(StoreOrderRefundRecord storeOrderRefundRecord, StoreOrderRefundRecord snapshot) {
        this.addDbRouteInfo(storeOrderRefundRecord.getMerchantId(), storeOrderRefundRecord.getStoreId());
        super.update(storeOrderRefundRecord, snapshot);
    }

    @Override
    public void update(StoreOrderRefundRecord storeOrderRefundRecord) {
        this.addDbRouteInfo(storeOrderRefundRecord.getMerchantId(), storeOrderRefundRecord.getStoreId());
        super.update(storeOrderRefundRecord);
    }
    
    @Override
    public List<StoreOrderRefundRecord> batchCreate(List<StoreOrderRefundRecord> list) {
        if (list == null || list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void delete(StoreOrderRefundRecord storeOrderRefundRecord) {
        this.addDbRouteInfo(storeOrderRefundRecord.getMerchantId(), storeOrderRefundRecord.getStoreId());
        super.delete(storeOrderRefundRecord);
    }

    public StoreOrderRefundRecord getById(int merchantId, long storeId, long refundRecordId, boolean forUpdate) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(StoreOrderRefundRecord.class, refundRecordId, forUpdate);
    }


    public StoreOrderRefundRecord loadById(int merchantId, long storeId, long refundRecordId, boolean forUpdate) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        StoreOrderRefundRecord storeOrderRefundRecord = this.getById(merchantId, storeId, refundRecordId, forUpdate);
        if (storeOrderRefundRecord == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_RECORD_INVALID.getValue(),
                    "storeId[" + storeId + "] refundRecordId[" + refundRecordId + "] invalid");
        }
        return storeOrderRefundRecord;
    }
}
