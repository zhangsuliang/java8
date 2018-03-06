package com.huofu.module.i5wei.pickupsite.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import halo.query.Query;
import halo.query.dal.DALContext;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by taoming on 2016/12/2.
 */
@SuppressWarnings("all")
@Repository
public class StorePickupSiteTimeSettingDAO extends AbsQueryDAO<StorePickupSiteTimeSetting> {

	private void addDbRouteInfo(int merchantId, long storeId) {
		BaseStoreDbRouter.addInfo(merchantId, storeId);
	}

	private DALContext buildDalContext(int merchantId, long storeId, boolean enableSlave) {
		DALContext dalContext = DALContext.create();
		dalContext.setEnableSlave(enableSlave);
		dalContext.addParam("merchant_id", merchantId);
		dalContext.addParam("store_id", storeId);
		return dalContext;
	}

	public String getRealName(int merchantId, long storeId) {
		this.addDbRouteInfo(merchantId, storeId);
		DALInfo dalInfo = Query.process(StorePickupSite.class);
		return dalInfo.getRealTable(StorePickupSite.class);
	}

	/**
	 * 获取自提点下所有开放时间
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteId
	 * @return
	 */
	public List<StorePickupSiteTimeSetting> getPickupSiteTimeSettingBypickupSiteId(int merchantId, long storeId, long pickupSiteId) {
		this.addDbRouteInfo(merchantId, storeId);
		DALStatus.setSlaveMode();
		return this.query.list(StorePickupSiteTimeSetting.class, "where pickup_site_id=? ", new Object[]{pickupSiteId});
	}

	/**
	 * 获取一个时间段下的所有自提点id
	 *
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 * @return
	 */
	public List<StorePickupSiteTimeSetting> getPickupSiteIdsByTimeBucketId(int merchantId, long storeId, long timeBucketId) {
		DALStatus.setSlaveMode();
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StorePickupSiteTimeSetting.class, "where time_bucket_id=? ", new Object[]{timeBucketId});
	}

	/**
	 * 获取一个自提点某一个时间段下的时间设置
	 *
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 * @return
	 */
	public List<StorePickupSiteTimeSetting> getPickupSiteIdsByPickupSiteIdAndTimeBucketId(int merchantId, long storeId, long pickupSiteId,
	                                                                                      long timeBucketId) {
		DALStatus.setSlaveMode();
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.list(StorePickupSiteTimeSetting.class, "where pickup_site_id=? and time_bucket_id=? ",
		                       new Object[]{pickupSiteId, timeBucketId});
	}

	/**
	 * 批量设置开放时段
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteId
	 * @param storePickupSiteTimeSettings
	 */
	public void createPickupSiteTimeSetting(int merchantId,
	                                        long storeId,
	                                        List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings) {
		for (StorePickupSiteTimeSetting storePickupSiteTimeSetting : storePickupSiteTimeSettings) {
			storePickupSiteTimeSetting.setMerchantId(merchantId);
			storePickupSiteTimeSetting.setStoreId(storeId);
			storePickupSiteTimeSetting.setCreateTime(System.currentTimeMillis());
			storePickupSiteTimeSetting.setUpdateTime(System.currentTimeMillis());
			this.addDbRouteInfo(merchantId, storeId);
			this.query.replace(storePickupSiteTimeSetting);
		}
	}
}
