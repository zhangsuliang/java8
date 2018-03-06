package com.huofu.module.i5wei.remark.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.remark.entity.StoreProductRemark;

import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreProductRemarkDAO extends AbsQueryDAO<StoreProductRemark> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreProductRemark storeProductRemark) {
        this.addDbRouteInfo(storeProductRemark.getMerchantId(),
                storeProductRemark.getStoreId());
        super.create(storeProductRemark);
    }

    @Override
    public void update(StoreProductRemark storeProductRemark, StoreProductRemark snapshot) {
        this.addDbRouteInfo(storeProductRemark.getMerchantId(),
                storeProductRemark.getStoreId());
        super.update(storeProductRemark, snapshot);
    }

    @Override
    public void delete(StoreProductRemark storeProductRemark) {
        this.addDbRouteInfo(storeProductRemark.getMerchantId(),
                storeProductRemark.getStoreId());
        super.delete(storeProductRemark);
    }

    public StoreProductRemark getById(int merchantId, long storeId, String remark) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objByIds(StoreProductRemark.class, new
                Object[]{storeId, remark});
    }

    public List<StoreProductRemark> queryList(int merchantId, long storeId, int size) {
    	DALStatus.setSlaveMode();
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.list(StoreProductRemark.class, "where " +
                "store_id=? order by update_time desc limit ?", new
                Object[]{storeId, size});
    }

}
