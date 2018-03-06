package com.huofu.module.i5wei.pickupsite.dao;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteEnterprise;
import halo.query.Query;
import halo.query.dal.DALContext;
import halo.query.dal.DALInfo;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.ObjectUtil;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by taoming on 2016/12/2.
 */
@SuppressWarnings("all")
@Repository
public class StorePickupSiteEnterpriseDAO extends AbsQueryDAO<StorePickupSiteEnterprise> {

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
		DALInfo dalInfo = Query.process(StorePickupSiteEnterprise.class);
		return dalInfo.getRealTable(StorePickupSiteEnterprise.class);
	}

	/**
	 * 获取一个自提点下的协议企业ID
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteId
	 * @return
	 */
	public List<Long> getEnterpricesByPickupSiteId(int merchantId, long storeId, long pickupSiteId) {

		this.addDbRouteInfo(merchantId, storeId);
		DALStatus.setSlaveMode();
		List<Object> params = new ArrayList<>();
		StringBuffer sql = new StringBuffer(" SELECT distinct enterprise_id ");
		sql.append(" FROM ").append(getRealName(merchantId, storeId)).append(" where pickup_site_id=? ");
		params.add(pickupSiteId);
		List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
		if (list == null || list.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		List<Long> enterpriseIds = new ArrayList<>();
		for (Map<String, Object> map : list) {
			String enterpriseId = ObjectUtil.getString(map.get("enterprise_id"));
			if (enterpriseId != null && enterpriseId.length() > 0) {
				enterpriseIds.add(Long.valueOf(enterpriseId));
			}
		}
		return enterpriseIds;
	}

	/**
	 * 创建自提点协议企业关联关系
	 * <p>
	 * enterpriseIds为空，则删除所有的自提单协议企业
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteId
	 * @param storePickupSiteEnterprises
	 */
	public void addEnterpriseOfPickupSite(int merchantId, long storeId, List<StorePickupSiteEnterprise> storePickupSiteEnterprises) {
		this.addDbRouteInfo(merchantId, storeId);
		this.query.batchInsert(storePickupSiteEnterprises);
	}

	public void deleteEnterpriseOfPickupSite(int merchantId, long storeId, long pickupSiteId) {
		this.addDbRouteInfo(merchantId, storeId);
		this.query.delete(StorePickupSiteEnterprise.class, " where pickup_site_id=? ", new Object[]{pickupSiteId});
	}
}
