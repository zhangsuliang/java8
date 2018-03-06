package com.huofu.module.i5wei.setting.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.huofu.module.i5wei.setting.dao.StoreDefaultTagMessageDAO;
import com.huofu.module.i5wei.setting.entity.StoreDefaultTagMessage;
import huofucore.facade.i5wei.store5weisetting.StoreTagMessageEnum;

@Service
public class StoreDefaultTagMessageService {
	
	@Autowired
	private StoreDefaultTagMessageDAO storeDefaultTagMessageDAO;

	
	public List<StoreDefaultTagMessage> getStoreDefaultTagMessages(StoreTagMessageEnum storeTagMessageEnum, boolean enableSlave) {
		return this.storeDefaultTagMessageDAO.getStoreDefaultTagMessages(storeTagMessageEnum, enableSlave);
	}
	
	
	
}
