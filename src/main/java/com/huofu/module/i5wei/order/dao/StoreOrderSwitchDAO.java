package com.huofu.module.i5wei.order.dao;

import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrderSwitch;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreOrderSwitchDAO extends AbsQueryDAO<StoreOrderSwitch> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
    
    @Override
    public void create(StoreOrderSwitch storeOrderSwitch) {
        this.addDbRouteInfo(storeOrderSwitch.getMerchantId(), storeOrderSwitch.getStoreId());
        super.create(storeOrderSwitch);
    }

    @Override
    public void update(StoreOrderSwitch storeOrderSwitch, StoreOrderSwitch snapshot) {
        this.addDbRouteInfo(storeOrderSwitch.getMerchantId(), storeOrderSwitch.getStoreId());
        super.update(storeOrderSwitch, snapshot);
    }

    @Override
    public void delete(StoreOrderSwitch storeOrderSwitch) {
        this.addDbRouteInfo(storeOrderSwitch.getMerchantId(), storeOrderSwitch.getStoreId());
        super.delete(storeOrderSwitch);
    }
	
	public StoreOrderSwitch getStoreOrderSwitchById (int merchantId,long storeId,String orderId){
		this.addDbRouteInfo(merchantId, storeId);
		String orderSwitchId = StoreOrderSwitch.getOrderSwitchId(orderId);
		return this.query.objById(StoreOrderSwitch.class, orderSwitchId);
	}
	
	public void deleteByOrderId(int merchantId, long storeId, String orderId) {
        this.addDbRouteInfo(merchantId, storeId);
        query.delete(StoreOrderSwitch.class, "where merchant_id=? and store_id=? and order_id=? ", new Object[]{merchantId, storeId, orderId});
    }
	
}
