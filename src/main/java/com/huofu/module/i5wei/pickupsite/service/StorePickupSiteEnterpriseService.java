package com.huofu.module.i5wei.pickupsite.service;

import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteEnterpriseDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteEnterprise;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by taoming on 2016/12/21.
 */
@Service
public class StorePickupSiteEnterpriseService {

	@Autowired
	private StorePickupSiteEnterpriseDAO storePickupSiteEnterpriseDAO;


	/**
	 * 创建自提点协议企业关联关系
	 * <p>
	 * enterpriseIds为空，则删除所有的自提单协议企业
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteId
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void addEnterpriseOfPickupSite(int merchantId, long storeId, long pickupSiteId, List<Long> enterpriseIds) {
		if (enterpriseIds == null) {
			return;
		}
		this.storePickupSiteEnterpriseDAO.deleteEnterpriseOfPickupSite(merchantId, storeId, pickupSiteId);
		if (CollectionUtils.isNotEmpty(enterpriseIds)) {
			List<StorePickupSiteEnterprise> storePickupSiteEnterprises = new ArrayList<StorePickupSiteEnterprise>();
			StorePickupSiteEnterprise storePickupSiteEnterprise = null;
			for (Long enterpriseId : enterpriseIds) {
				storePickupSiteEnterprise = new StorePickupSiteEnterprise();
				storePickupSiteEnterprise.setPickupSiteId(pickupSiteId);
				storePickupSiteEnterprise.setMerchantId(merchantId);
				storePickupSiteEnterprise.setStoreId(storeId);
				storePickupSiteEnterprise.setEnterpriseId(enterpriseId);
				storePickupSiteEnterprise.setCreateTime(System.currentTimeMillis());
				storePickupSiteEnterprises.add(storePickupSiteEnterprise);
			}
			this.storePickupSiteEnterpriseDAO.addEnterpriseOfPickupSite(merchantId, storeId, storePickupSiteEnterprises);
		}
	}

}
