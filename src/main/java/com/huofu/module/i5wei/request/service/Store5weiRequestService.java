package com.huofu.module.i5wei.request.service;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.request.dao.Store5weiRequestDAO;
import com.huofu.module.i5wei.request.entity.Store5weiRequest;

@Service
public class Store5weiRequestService {
	
	@Autowired
	private Store5weiRequestDAO store5weiRequestDAO;
	
	public void save(Store5weiRequestParam param) throws T5weiException {
		if (param == null) {
			return;
		}
		if (DataUtil.isEmpty(param.getRequestId())) {
			return;
		}
		Store5weiRequest store5weiRequest = BeanUtil.copy(param, Store5weiRequest.class);
		store5weiRequest.setCreateTime(System.currentTimeMillis());
		try {
			store5weiRequest.create();
		} catch (DuplicateKeyException e) {
			throw new T5weiException(T5weiErrorCodeType.STORE_5WEI_REQUEST_RESUBMIT.getValue(), "Store5weiRequest resubmit");
		}
	}
	
	public Store5weiRequest getById(int merchantId, long storeId, String requestId) throws T5weiException {
		return store5weiRequestDAO.loadById(merchantId, storeId, requestId);
	}

}
