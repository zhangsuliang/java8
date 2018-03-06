package com.huofu.module.i5wei.delivery.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.huofu.module.i5wei.delivery.dao.MerchantOrderDeliveryLastDAO;
import com.huofu.module.i5wei.delivery.entity.MerchantOrderDeliveryLast;

@Service
public class MerchantOrderDeliveryLastService {

	@Autowired
	private MerchantOrderDeliveryLastDAO merchantOrderDeliveryLastDAO;
	
	public List<MerchantOrderDeliveryLast> getMerchantOrderDeliveryLastsByInTime(int merchantId, long storeId, long userId, long time, boolean enableSlave){
		return this.merchantOrderDeliveryLastDAO.getMerchantOrderDeliveryLastsByInTime(merchantId,storeId,userId,time,enableSlave);
	}
	
	public List<MerchantOrderDeliveryLast> getMerchantOrderDeliveryLastByOutTime(int merchantId, long storeId, long userId, long time, boolean enableSlave){
		return this.merchantOrderDeliveryLastDAO.getMerchantOrderDeliveryLastByOutTime(merchantId,storeId,userId,time,enableSlave);
	}
}
