package com.huofu.module.i5wei.setting.facade;

import java.util.List;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.setting.entity.StoreDefaultTagMessage;
import com.huofu.module.i5wei.setting.service.StoreDefaultTagMessageService;

import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.store5weisetting.StoreDefaultTagMessageDTO;
import huofucore.facade.i5wei.store5weisetting.StoreDefaultTagMessageFacade;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageEnum;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;

@Component
@ThriftServlet(name = "storeDefaultTagMessageFacadeServlet", serviceClass = StoreDefaultTagMessageFacade.class)
public class StoreDefaultTagMessageFacadeImpl implements StoreDefaultTagMessageFacade.Iface {

	@Autowired
	private StoreDefaultTagMessageService storeDefaultTagMessageService;
	
	@Override
	public List<StoreDefaultTagMessageDTO> getStoreDefaultTagMessageDTOs(StoreTagMessageEnum storeTagMessageEnum) throws T5weiException, TException {
		List<StoreDefaultTagMessage> storeDefaultTagMessages = this.storeDefaultTagMessageService.getStoreDefaultTagMessages(storeTagMessageEnum, true);
		return BeanUtil.copyList(storeDefaultTagMessages, StoreDefaultTagMessageDTO.class);
	}
}
