package com.huofu.module.i5wei.delivery.dao;

import huofuhelper.util.AbsQueryDAO;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.base.BaseMerchantDbRouter;
import com.huofu.module.i5wei.delivery.entity.MerchantUserDeliveryAddress;
import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.delivery.MerchantUserDeliveryAddressParam;

/**
 * Auto created by i5weitools
 */
@Repository
public class MerchantUserDeliveryAddressDAO extends AbsQueryDAO<MerchantUserDeliveryAddress> {
	private void addDbRouteInfo(int merchantId, long storeId) {
        BaseMerchantDbRouter.addInfo(merchantId, storeId);
    }
	
	@Override
	public void create(MerchantUserDeliveryAddress merchantUserDeliveryAddress) {
		this.addDbRouteInfo(merchantUserDeliveryAddress.getMerchantId(), 0);
		super.create(merchantUserDeliveryAddress);
	}
	
	@Override
	public void update(MerchantUserDeliveryAddress merchantUserDeliveryAddress) {
		this.addDbRouteInfo(merchantUserDeliveryAddress.getMerchantId(), 0);
		super.update(merchantUserDeliveryAddress);
	}
	
	public MerchantUserDeliveryAddress getMerchantUserDeliveryAddressById(int merchantId, long storeId, long addressId, boolean enableSlave){
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.objById(MerchantUserDeliveryAddress.class, addressId);
	}

	public void deleteMerchantUserDeliveryAddress(int merchantId, long storeId, long userId, long addressId){
		this.addDbRouteInfo(merchantId, storeId);
		long currentTime = System.currentTimeMillis();
		this.query.update(MerchantUserDeliveryAddress.class, "set deleted = 1 , update_time = ? where address_id = ?", new Object[]{currentTime,addressId});
	}

	public List<MerchantUserDeliveryAddress> getMerchantUserDeliveryAddressByUserId(int merchantId, long storeId, long userId, boolean enableSlave) {
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(MerchantUserDeliveryAddress.class, "where merchant_id = ? and user_id = ? and deleted = 0", new Object[]{merchantId,userId});
	}
}
