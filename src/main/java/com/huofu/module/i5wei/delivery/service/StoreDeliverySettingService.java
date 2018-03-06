package com.huofu.module.i5wei.delivery.service;

import com.huofu.module.i5wei.delivery.dao.StoreDeliverySettingDAO;
import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import huofucore.facade.i5wei.delivery.StoreDeliverySettingDTO;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.bean.BeanUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * Created by akwei on 5/6/15.
 */
@Service
public class StoreDeliverySettingService {

	@Autowired
	private StoreDeliverySettingDAO storeDeliverySettingDAO;

	public StoreDeliverySetting saveStoreDeliverySetting(StoreDeliverySettingDTO param) {
		try {
			return this._saveStoreDeliverySetting(param);
		} catch (DuplicateKeyException e) {
			return this._saveStoreDeliverySetting(param);
		}
	}

	public StoreDeliverySetting _saveStoreDeliverySetting(StoreDeliverySettingDTO param) {
		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingDAO.getById(param.getMerchantId(), param.getStoreId());
		if (storeDeliverySetting == null) {
			storeDeliverySetting = StoreDeliverySetting.createDefault(param.getMerchantId(), param.getStoreId());

			BeanUtil.copy(param, storeDeliverySetting, true);
			if (storeDeliverySetting.getManualNotifyHeadTime() <= 0) {
				storeDeliverySetting.setManualNotifyHeadTime(45 * 60 * 1000);
			}
			storeDeliverySetting.create();
		} else {
			storeDeliverySetting.snapshot();
			BeanUtil.copy(param, storeDeliverySetting, true);
			if (storeDeliverySetting.getManualNotifyHeadTime() <= 0) {
				storeDeliverySetting.setManualNotifyHeadTime(45 * 60 * 1000);
			}
			storeDeliverySetting.update();
		}
		return storeDeliverySetting;
	}

	public StoreDeliverySetting getStoreDeliverySetting(int merchantId, long storeId) {
		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingDAO.getById(merchantId, storeId);
		if (storeDeliverySetting == null) {
			storeDeliverySetting = StoreDeliverySetting.createDefault(merchantId, storeId);
		}
		return storeDeliverySetting;
	}

	public Map<Long, StoreDeliverySetting> getStoreDeliverySettingByMerchantId(int merchantId, boolean enableSlave) {
		List<StoreDeliverySetting> storeDeliverySettings = this.storeDeliverySettingDAO.getStoreDeliverySettingByMerchantId(merchantId, enableSlave);
		Map<Long, StoreDeliverySetting> storeDeliverySettingMap = new HashMap<Long, StoreDeliverySetting>();
		for (StoreDeliverySetting storeDeliverySetting : storeDeliverySettings) {
			storeDeliverySettingMap.put(storeDeliverySetting.getStoreId(), storeDeliverySetting);
		}
		return storeDeliverySettingMap;
	}

	public StoreDeliverySetting getStoreDeliverySetting4Read(int merchantId, long storeId) {
		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingDAO.getById(merchantId, storeId, true, true);
		if (storeDeliverySetting == null) {
			storeDeliverySetting = StoreDeliverySetting.createDefault(merchantId, storeId);
		}
		return storeDeliverySetting;
	}
	
	//兼容单店铺
	public void updateStoreDeliveryWaimaiEnableByWaimaiType(int merchantId, long storeId, int waimaiType, boolean enabled, boolean enableSlave) {
		storeDeliverySettingDAO.updateStoreDeliveryWaimaiEnableByWaimaiType(merchantId, storeId, waimaiType, enabled, enableSlave);
	}
}
