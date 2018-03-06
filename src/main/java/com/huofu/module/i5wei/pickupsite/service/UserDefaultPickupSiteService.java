package com.huofu.module.i5wei.pickupsite.service;

import com.huofu.module.i5wei.pickupsite.dao.UserDefaultPickupSiteDAO;
import com.huofu.module.i5wei.pickupsite.entity.UserDefaultPickupSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by taoming on 2016/12/7.
 */
@Service
public class UserDefaultPickupSiteService {

	@Autowired
	private UserDefaultPickupSiteDAO userDefaultPickupSiteDAO;

	/**
	 * 获取默认的自提点
	 *
	 * @param userId
	 * @param timeBucketId
	 * @return
	 */
	public long getUserTimeBucketDefaultPickupSite(long userId, long timeBucketId) {
		return this.userDefaultPickupSiteDAO.getUserTimeBucketDefaultPickupSite(userId, timeBucketId);
	}

	/**
	 * 设置默认自提点
	 *
	 * @param userDefaultPickupSite
	 */
	public void setUserTimeBucketDefaultPickupSite(UserDefaultPickupSite userDefaultPickupSite) {
		this.userDefaultPickupSiteDAO.createUserTimeBucketDefaultPickupSite(userDefaultPickupSite);
	}

	/**
	 * 删除默认自提点
	 *
	 * @param userId
	 * @param timeBucketId
	 * @param pickupSiteId
	 */
	public void concelUserTimeBucketDefaultPickupSite(long userId, long pickupSiteId, long timeBucketId) {
		this.userDefaultPickupSiteDAO.concelUserTimeBucketDefaultPickupSite(userId, pickupSiteId, timeBucketId);
	}
}
