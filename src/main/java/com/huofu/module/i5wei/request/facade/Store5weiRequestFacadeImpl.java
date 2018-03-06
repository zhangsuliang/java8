package com.huofu.module.i5wei.request.facade;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.request.Store5weiRequestDTO;
import huofucore.facade.i5wei.request.Store5weiRequestFacade;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.request.entity.Store5weiRequest;
import com.huofu.module.i5wei.request.service.Store5weiRequestService;

/**
 * 5wei请求唯一性服务
 * Created by chenkai on 2016-12-20
 */
@Component
@ThriftServlet(name = "store5weiRequestFacadeServlet", serviceClass = Store5weiRequestFacade.class)
public class Store5weiRequestFacadeImpl implements Store5weiRequestFacade.Iface {
	
	@Autowired
	private Store5weiRequestService store5weiRequestService;

	@Override
	public Store5weiRequestDTO getStore5weiRequestById(int merchantId, long storeId, String requestId) throws T5weiException, TException {
		Store5weiRequest store5weiRequest = store5weiRequestService.getById(merchantId, storeId, requestId);
		return BeanUtil.copy(store5weiRequest, Store5weiRequestDTO.class);
	}

}
