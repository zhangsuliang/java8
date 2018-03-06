package com.huofu.module.i5wei.delivery.dao;

import huofuhelper.util.AbsQueryDAO;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.huofu.module.i5wei.base.BaseMerchantDbRouter;
import com.huofu.module.i5wei.delivery.entity.MerchantOrderDeliveryLast;

import halo.query.dal.DALStatus;

/**
 * Auto created by i5weitools
 */
@Repository
public class MerchantOrderDeliveryLastDAO extends AbsQueryDAO<MerchantOrderDeliveryLast> {
	private void addDbRouteInfo(int merchantId, long storeId) {
        BaseMerchantDbRouter.addInfo(merchantId, storeId);
    }

	/**
	 * 用户在几天内某个商户的外送店铺
	 * @param merchantId
	 * @param storeId
	 * @param userId
	 * @param time
	 * @param enableSlave
	 * @return
	 */
	public List<MerchantOrderDeliveryLast> getMerchantOrderDeliveryLastsByInTime(int merchantId, long storeId, long userId, long time, boolean enableSlave) {
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(MerchantOrderDeliveryLast.class, "where merchant_id = ? and user_id = ? and last_delivery_time >= ? order by last_delivery_time desc", new Object[]{merchantId,userId,time});
	}

	/**
	 * 用户在几天前某个商户的某个外送店铺
	 * @param merchantId
	 * @param storeId
	 * @param userId
	 * @param time
	 * @param enableSlave
	 * @return
	 */
	public List<MerchantOrderDeliveryLast> getMerchantOrderDeliveryLastByOutTime(int merchantId, long storeId, long userId, long time, boolean enableSlave) {
		if(enableSlave){
			DALStatus.setSlaveMode();
		}
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(MerchantOrderDeliveryLast.class, "where merchant_id = ? and user_id = ? and last_delivery_time < ? order by last_delivery_time ", new Object[]{merchantId,userId,time});
	}
}
