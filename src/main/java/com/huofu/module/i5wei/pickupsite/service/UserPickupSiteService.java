package com.huofu.module.i5wei.pickupsite.service;

import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteDAO;
import com.huofu.module.i5wei.pickupsite.dao.UserDefaultPickupSiteDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.entity.UserDefaultPickupSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPickupSiteService {
	@Autowired
	private UserDefaultPickupSiteDAO userDefaultPickupSiteDAO;

	@Autowired
	private StorePickupSiteDAO storePickupSiteDAO;

	public void createUserTimeBucketDefaultPickupSite(UserDefaultPickupSite userDefaultPickupSite) {
		userDefaultPickupSiteDAO.createUserTimeBucketDefaultPickupSite(userDefaultPickupSite);
	}

	public long getUserTimeBucketDefaultPickupSite(long userId, long timeBucketId) {
		return userDefaultPickupSiteDAO.getUserTimeBucketDefaultPickupSite(userId, timeBucketId);
	}

	public StorePickupSite getStorePickupSiteById(int merchantId, long storeId, long storePickupSiteId) {

		return storePickupSiteDAO.getStorePickupSiteById(merchantId, storeId, storePickupSiteId);
	}

}
