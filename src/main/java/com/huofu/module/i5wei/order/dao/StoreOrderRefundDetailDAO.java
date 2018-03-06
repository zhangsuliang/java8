package com.huofu.module.i5wei.order.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundDetail;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundRecord;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StoreOrderRefundDetailDAO extends AbsQueryDAO<StoreOrderRefundDetail> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreOrderRefundDetail storeOrderRefundDetail) {
        this.addDbRouteInfo(storeOrderRefundDetail.getMerchantId(), storeOrderRefundDetail.getStoreId());
        super.create(storeOrderRefundDetail);
    }

    @Override
    public void replace(StoreOrderRefundDetail storeOrderRefundDetail) {
        this.addDbRouteInfo(storeOrderRefundDetail.getMerchantId(), storeOrderRefundDetail.getStoreId());
        super.replace(storeOrderRefundDetail);
    }

    @Override
    public void update(StoreOrderRefundDetail storeOrderRefundDetail) {
        this.addDbRouteInfo(storeOrderRefundDetail.getMerchantId(), storeOrderRefundDetail.getStoreId());
        super.update(storeOrderRefundDetail);
    }

    @Override
    public void update(StoreOrderRefundDetail storeOrderRefundDetail, StoreOrderRefundDetail snapshot) {
        this.addDbRouteInfo(storeOrderRefundDetail.getMerchantId(), storeOrderRefundDetail.getStoreId());
        super.update(storeOrderRefundDetail, snapshot);
    }

    @Override
    public void delete(StoreOrderRefundDetail storeOrderRefundDetail) {
        this.addDbRouteInfo(storeOrderRefundDetail.getMerchantId(), storeOrderRefundDetail.getStoreId());
        super.delete(storeOrderRefundDetail);
    }

    @Override
    public List<StoreOrderRefundDetail> batchCreate(List<StoreOrderRefundDetail> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }
}
