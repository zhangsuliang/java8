package com.huofu.module.i5wei.pickupsite.service;

import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteTimeSettingDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorePickupSiteTimeSettingService {

	@Autowired
	private StorePickupSiteTimeSettingDAO storePickupSiteTimeSettingDAO;

	public List<StorePickupSiteTimeSetting> getPickupSiteIdsByTimeBucketId(int merchantId, long storeId, long timeBucketId) {
		return storePickupSiteTimeSettingDAO.getPickupSiteIdsByTimeBucketId(merchantId, storeId, timeBucketId);
	}

	/**
	 * 获取一个自提点某一个时间段下的时间设置
	 *
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 * @return
	 */
	public StorePickupSiteTimeSetting getPickupSiteIdsByPickupSiteIdAndTimeBucketId(int merchantId,
	                                                                                long storeId,
	                                                                                long pickupSiteId,
	                                                                                long timeBucketId) {
		List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings =
				this.storePickupSiteTimeSettingDAO.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(
						                                                                                                 merchantId,
						                                                                                                 storeId,
						                                                                                                 pickupSiteId,
						                                                                                                 timeBucketId);
		if (CollectionUtils.isNotEmpty(storePickupSiteTimeSettings)) {
			return storePickupSiteTimeSettings.get(0);
		}
		return null;
	}

}
