package com.huofu.module.i5wei.order.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrderCombinedBiz;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by wangxiaoyang on 16/8/20
 */
@Repository
public class StoreOrderCombinedBizDao extends AbsQueryDAO<StoreOrderCombinedBiz> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    public void create(StoreOrderCombinedBiz storeOrderCombinedBiz) {
        this.addDbRouteInfo(storeOrderCombinedBiz.getMerchantId(), storeOrderCombinedBiz.getStoreId());
        storeOrderCombinedBiz.setCreateTime(System.currentTimeMillis());
        super.create(storeOrderCombinedBiz);
    }

    public void update(StoreOrderCombinedBiz storeOrderCombinedBiz, StoreOrderCombinedBiz snapshot) {
        this.addDbRouteInfo(storeOrderCombinedBiz.getMerchantId(), storeOrderCombinedBiz.getStoreId());
        super.update(storeOrderCombinedBiz, snapshot);
    }

    public List<StoreOrderCombinedBiz> getByOrderIdAndType(int merchantId, long storeId, String orderId, int bizType, boolean enableSlave) {
        this.addDbRouteInfo(merchantId, storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        return this.query.list(StoreOrderCombinedBiz.class, "where order_id=? and biz_type=? order by create_time asc", new Object[]{orderId, bizType});
    }
}
