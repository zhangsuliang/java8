package com.huofu.module.i5wei.delivery.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.huofu.module.i5wei.delivery.dao.MerchantUserDeliveryAddressDAO;
import com.huofu.module.i5wei.delivery.entity.MerchantUserDeliveryAddress;

import huofucore.facade.i5wei.delivery.MerchantUserDeliveryAddressParam;
import huofuhelper.util.bean.BeanUtil;

@Service
public class MerchantUserDeliveryAddressService {
	@Autowired
	private MerchantUserDeliveryAddressDAO merchantUserDeliveryAddressDAO;
	
	public MerchantUserDeliveryAddress getMerchantUserDeliveryAddressById(int merchantId, long storeId, long addressId, boolean enableSlave){
		return this.merchantUserDeliveryAddressDAO.getMerchantUserDeliveryAddressById(merchantId, storeId, addressId,enableSlave);
	}

	public MerchantUserDeliveryAddress saveMerchantUserDeliveryAddress(MerchantUserDeliveryAddressParam merchantUserDeliveryAddressParam) {
		int merchantId = merchantUserDeliveryAddressParam.getMerchantId();
		long storeId = merchantUserDeliveryAddressParam.getStoreId();
		long addressId = merchantUserDeliveryAddressParam.getAddressId();
		long currentTime = System.currentTimeMillis();
		MerchantUserDeliveryAddress merchantUserDeliveryAddress = this.getMerchantUserDeliveryAddressById(merchantId, storeId, addressId, true);
		if(merchantUserDeliveryAddress != null){//update
			BeanUtil.copy(merchantUserDeliveryAddressParam, merchantUserDeliveryAddress);
			merchantUserDeliveryAddress.setUpdateTime(currentTime);
			merchantUserDeliveryAddressDAO.update(merchantUserDeliveryAddress);
		}else{//add
			merchantUserDeliveryAddress = new MerchantUserDeliveryAddress();
			BeanUtil.copy(merchantUserDeliveryAddressParam, merchantUserDeliveryAddress);
			merchantUserDeliveryAddress.setCreateTime(currentTime);
			merchantUserDeliveryAddress.setUpdateTime(currentTime);
			merchantUserDeliveryAddressDAO.create(merchantUserDeliveryAddress);
		}
		return merchantUserDeliveryAddress;
	}
	
	public void deleteMerchantUserDeliveryAddress(int merchantId, long storeId, long userId, long addressId){
		this.merchantUserDeliveryAddressDAO.deleteMerchantUserDeliveryAddress(merchantId, storeId, userId, addressId);
	}
	
	public List<MerchantUserDeliveryAddress> getMerchantUserDeliveryAddressByUserId(int merchantId, long storeId, long userId, boolean enableSlave){
		return this.merchantUserDeliveryAddressDAO.getMerchantUserDeliveryAddressByUserId(merchantId, storeId, userId, enableSlave);
	}
}
