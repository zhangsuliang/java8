package com.huofu.module.i5wei.request.dao;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;

import org.springframework.stereotype.Repository;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.request.entity.Store5weiRequest;

/**
 * Auto created by i5weitools
 */
@Repository
public class Store5weiRequestDAO extends AbsQueryDAO<Store5weiRequest> {
	
	private void addDbRouteInfo(int merchantId, long storeId) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
    }
	
	@Override
    public void create(Store5weiRequest store5weiRequest) {
        this.addDbRouteInfo(store5weiRequest.getMerchantId(), store5weiRequest.getStoreId());
        super.create(store5weiRequest);
    }

    @Override
    public void update(Store5weiRequest store5weiRequest, Store5weiRequest snapshot) {
        this.addDbRouteInfo(store5weiRequest.getMerchantId(), store5weiRequest.getStoreId());
        super.update(store5weiRequest, snapshot);
    }

    @Override
    public void delete(Store5weiRequest store5weiRequest) {
        this.addDbRouteInfo(store5weiRequest.getMerchantId(), store5weiRequest.getStoreId());
        super.delete(store5weiRequest);
    }
    
    public Store5weiRequest loadById(int merchantId, long storeId, String requestId) throws T5weiException {
        this.addDbRouteInfo(merchantId, storeId);
        Store5weiRequest store5weiRequest = this.query.objById(Store5weiRequest.class, requestId);
		if (store5weiRequest == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_5WEI_REQUEST_NOT_EXIST.getValue(), "Store5weiRequest not exist");
		}
        return store5weiRequest;
    }

    public Store5weiRequest getById(int merchantId, long storeId, String requestId) {
        this.addDbRouteInfo(merchantId, storeId);
        return this.query.objById(Store5weiRequest.class, requestId);
    }

}
