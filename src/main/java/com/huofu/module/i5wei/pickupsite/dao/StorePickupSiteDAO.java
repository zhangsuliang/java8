package com.huofu.module.i5wei.pickupsite.dao;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.IdMakerUtil;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import halo.query.Query;
import halo.query.dal.DALContext;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by taoming on 2016/12/2.
 */
@SuppressWarnings("all")
@Repository
public class StorePickupSiteDAO extends AbsQueryDAO<StorePickupSite> {

	private static final String PICKUP_SITE_KEY = "tb_store_pickup_site_seq";

	@Autowired
	private IdMakerUtil idMakerUtil;


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

	public void create(StorePickupSite storePickupSite) {
		this.addDbRouteInfo(storePickupSite.getMerchantId(), storePickupSite.getStoreId());
		storePickupSite.setStorePickupSiteId(this.idMakerUtil.nextId2(PICKUP_SITE_KEY));
		super.create(storePickupSite);
	}

	public void update(StorePickupSite storePickupSite, StorePickupSite storePickupSiteSnapshot) {
		this.addDbRouteInfo(storePickupSite.getMerchantId(), storePickupSite.getStoreId());
		super.update(storePickupSite, storePickupSiteSnapshot);
	}


	/**
	 * 根据店铺id获取自提点列表
	 *
	 * @param merchantId
	 * @param storeId
	 * @return
	 */
	public List<StorePickupSite> getStorePickupSiteByStoreId(int merchantId, long storeId) {
		this.addDbRouteInfo(merchantId, storeId);
		DALStatus.setSlaveMode();
		return this.query.list(StorePickupSite.class, "where store_id=?", new Object[]{storeId});
	}

	/**
	 * 根据店铺id获取自提点数目
	 *
	 * @param merchantId
	 * @param storeId
	 * @param enableSlave
	 * @return
	 */
	public int getPickupSiteCountByStoreId(int merchantId, long storeId, int disabled) {
		DALStatus.setSlaveMode();
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.count(StorePickupSite.class, "where store_id=? and disabled=? ", new Object[]{storeId, disabled});
	}

	/**
	 * 根据pickupids获取自提点集合
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteIds
	 * @return
	 */
	public List<StorePickupSite> getStorePickupSiteByStoreId(int merchantId, long storeId, List<Long> pickupSiteIds, boolean isContainDisabled) {
		DALStatus.setSlaveMode();
		this.addDbRouteInfo(merchantId, storeId);
		if (isContainDisabled) {
			return this.query.listInValues2(StorePickupSite.class, "where 1=1 ", "pickup_site_id", Lists.newArrayList(), pickupSiteIds);
		} else {
			return this.query.list(StorePickupSite.class, "where store_id=? and disabled = ?", new Object[]{storeId, 0});
		}
	}

	/**
	 * 根据参数id获取自提点
	 *
	 * @param merchantId
	 * @param storeId
	 * @param storePickupSiteId
	 * @return
	 */
	public StorePickupSite getStorePickupSiteById(int merchantId, long storeId, long storePickupSiteId) {
		this.addDbRouteInfo(merchantId, storeId);
		return this.query.obj(StorePickupSite.class, "where pickup_site_id=? ", new Object[]{storePickupSiteId});
	}
}
