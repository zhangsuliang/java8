package com.huofu.module.i5wei.order.dao;

import halo.query.Query;
import halo.query.dal.DALContext;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderInvoice;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreOrderInvoiceDAO extends AbsQueryDAO<StoreOrderInvoice> {
	
	private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }

    private DALContext buildDalContext(int merchantId, long storeId, boolean enableSlave) {
        DALContext dalContext = DALContext.create();
        dalContext.setEnableSlave(enableSlave);
        dalContext.addParam("merchant_id", merchantId);
        dalContext.addParam("store_id", storeId);
        return dalContext;
    }

    public String getRealName(int merchantId, long storeId) {
        this.addDbRouteInfo(merchantId, storeId);
        DALInfo dalInfo = Query.process(StoreOrder.class);
        return dalInfo.getRealTable(StoreOrder.class);
    }

    public void create(StoreOrderInvoice storeOrderInvoice) {
        this.addDbRouteInfo(storeOrderInvoice.getMerchantId(), storeOrderInvoice.getStoreId());
        super.create(storeOrderInvoice);
    }

    @Override
    public List<StoreOrderInvoice> batchCreate(List<StoreOrderInvoice> list) {
        if (list.isEmpty()) {
            return list;
        }
        this.addDbRouteInfo(list.get(0).getMerchantId(), list.get(0).getStoreId());
        return super.batchCreate(list);
    }

    @Override
    public void update(StoreOrderInvoice storeOrderInvoice) {
        this.addDbRouteInfo(storeOrderInvoice.getMerchantId(), storeOrderInvoice.getStoreId());
        super.update(storeOrderInvoice);
    }

    @Override
    public void update(StoreOrderInvoice storeOrderInvoice, StoreOrderInvoice snapshot) {
        this.addDbRouteInfo(storeOrderInvoice.getMerchantId(), storeOrderInvoice.getStoreId());
        super.update(storeOrderInvoice, snapshot);
    }

    @Override
    public void delete(StoreOrderInvoice storeOrderInvoice) {
        this.addDbRouteInfo(storeOrderInvoice.getMerchantId(), storeOrderInvoice.getStoreId());
        super.delete(storeOrderInvoice);
    }
	
	/**
     * 根据orderId查询订单
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     */
    public StoreOrderInvoice getById(int merchantId, long storeId, String orderId, boolean forUpdate, boolean forSnapshot) {
        this.addDbRouteInfo(merchantId, storeId);
        StoreOrderInvoice storeOrderInvoice;
        if (forUpdate) {
        	storeOrderInvoice = this.query.objByIdForUpdate(StoreOrderInvoice.class, orderId);
        } else {
        	storeOrderInvoice = this.query.objById(StoreOrderInvoice.class, orderId);
        }
        if (storeOrderInvoice != null) {
            if (forSnapshot) {
            	storeOrderInvoice.snapshot();
            }
        }
        return storeOrderInvoice;
    }
    
    /**
     * 根据orderId查询订单
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     */
    public StoreOrderInvoice queryById(int merchantId, long storeId, String orderId) {
    	DALStatus.setSlaveMode();
        this.addDbRouteInfo(merchantId, storeId);
        StoreOrderInvoice storeOrderInvoice = this.query.objById(StoreOrderInvoice.class, orderId);
        return storeOrderInvoice;
    }
    
    /**
     * 根据orderId查询订单Map
     *
     * @param merchantId
     * @param storeId
     * @param orderId
     * @return
     */
    public Map<String, StoreOrderInvoice> getMapByOrderIds(int merchantId, long storeId, List<String> orderIds, boolean enableSlave) {
		if (orderIds == null || orderIds.isEmpty()) {
			return new HashMap<String, StoreOrderInvoice>();
		}
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
        this.addDbRouteInfo(merchantId, storeId);
        Map<String, StoreOrderInvoice> storeOrderInvoiceMap = this.query.map2(StoreOrderInvoice.class, null, "order_id", null, orderIds);
        return storeOrderInvoiceMap;
    }

}
