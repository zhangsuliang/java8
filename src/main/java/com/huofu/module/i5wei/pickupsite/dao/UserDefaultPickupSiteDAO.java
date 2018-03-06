package com.huofu.module.i5wei.pickupsite.dao;

import com.huofu.module.i5wei.pickupsite.entity.UserDefaultPickupSite;
import huofuhelper.util.AbsQueryDAO;
import huofuhelper.util.ObjectUtil;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by taoming on 2016/12/2.
 */
@Repository
public class UserDefaultPickupSiteDAO extends AbsQueryDAO<UserDefaultPickupSite> {

	/**
	 * 获取用户某一营业时段下的默认自提点
	 *
	 * @param userId
	 * @param timeBucketId
	 * @return
	 */
	public long getUserTimeBucketDefaultPickupSite(long userId, long timeBucketId) {

		long userDefaultPickupSiteId = 0L;
		List<Object> params = new ArrayList<>();
		StringBuffer sql = new StringBuffer(" SELECT distinct pickup_site_id ");
		sql.append(" FROM ").append("tb_store_pickup_site_user_default").append(" where user_id = ? and time_bucket_id=? ");
		params.add(userId);
		params.add(timeBucketId);
		List<Map<String, Object>> list = this.query.getJdbcSupport().getMapList(sql.toString(), params.toArray());
		if (list == null || list.isEmpty()) {
			return userDefaultPickupSiteId;
		}
		for (Map<String, Object> map : list) {
			String pickupSiteId = ObjectUtil.getString(map.get("pickup_site_id"));
			if (pickupSiteId != null && pickupSiteId.length() > 0) {
				userDefaultPickupSiteId = Integer.valueOf(pickupSiteId);
				break;
			}
		}
		return userDefaultPickupSiteId;
	}

	/**
	 * 增加用户某一营业时段的默认自提点
	 */
	public void createUserTimeBucketDefaultPickupSite(UserDefaultPickupSite userDefaultPickupSite) {
		this.query.replace(userDefaultPickupSite);
	}

	/**
	 * 取消用户一时段的默认自提点
	 *
	 * @param userId
	 * @param timeBucketId
	 * @param pickupSiteId
	 */
	public void concelUserTimeBucketDefaultPickupSite(long userId, long pickupSiteId, long timeBucketId) {

		this.query.delete(UserDefaultPickupSite.class, " where user_id = ? and pickup_site_id=? and time_bucket_id=?",
		                  new Object[]{userId, pickupSiteId, timeBucketId});
	}
}
