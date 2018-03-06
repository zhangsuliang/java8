package com.huofu.module.i5wei.delivery.dao;

import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;

import halo.query.dal.DALStatus;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.AbsQueryDAO;

import org.apache.thrift.TException;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Auto created by i5weitools
 */
@Repository
public class StoreDeliverySettingDAO extends AbsQueryDAO<StoreDeliverySetting> {

	public StoreDeliverySetting getById(int merchantId, long storeId, boolean enableSlave, boolean enableCache) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		return this.query.objById(StoreDeliverySetting.class, storeId);
	}

	public StoreDeliverySetting getById(int merchantId, long storeId) {
		return this.getById(merchantId, storeId, false, false);
	}

	public List<StoreDeliverySetting> getStoreDeliverySettingsAutoPrepareMeal(boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		return this.query.list(StoreDeliverySetting.class, " where auto_prepare_meal_supported = 1 and auto_prepare_meal_supported > 0", null);
	}

	public List<StoreDeliverySetting> getStoreDeliverySettingByMerchantId(int merchantId, boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		return this.query.list(StoreDeliverySetting.class, " where merchant_id = ? and delivery_supported = 1", new Object[] { merchantId });
	}
	/**
	 * 开启/关闭5wei外卖设置外卖平台开关（兼容单店铺）
	 * 
	 * @param merchantId
	 * @param storeId
	 * @param waimaiType
	 * @param enabled
	 * @throws T5weiException
	 * @throws TException
	 */
	public void updateStoreDeliveryWaimaiEnableByWaimaiType(int merchantId, long storeId, int waimaiType, boolean enabled,
	                                                        boolean enableSlave) {
		if (enableSlave) {
			DALStatus.setSlaveMode();
		}
		String waimaiField = "";
		if (waimaiType == WaimaiTypeEnum.MEITUAN.getValue()) {
			waimaiField = "meituan_waimai_enabled";
		} else if (waimaiType == WaimaiTypeEnum.BAIDU.getValue()) {
			waimaiField = "baidu_waimai_enabled";
		} else if (waimaiType == WaimaiTypeEnum.ELEME.getValue()) {
			waimaiField = "eleme_waimai_enabled";
		}
		this.query.update(StoreDeliverySetting.class, "set " + waimaiField + " =? where store_id=? and merchant_id=?",
		                  new Object[]{enabled, storeId, merchantId});
	}
}
