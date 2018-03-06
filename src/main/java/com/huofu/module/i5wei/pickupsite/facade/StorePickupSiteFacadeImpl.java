package com.huofu.module.i5wei.pickupsite.facade;


import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteHelpler;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteSaveOrUpdateParam;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteService;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.pickupsite.*;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@ThriftServlet(name = "storePickupSiteFacadeServlet", serviceClass = StorePickupSiteFacade.class)
public class StorePickupSiteFacadeImpl implements StorePickupSiteFacade.Iface {

	private final static Log log = LogFactory.getLog(StorePickupSiteFacadeImpl.class);

	@Autowired
	private StorePickupSiteService storePickupSiteSevice;

	@Autowired
	private StorePickupSiteHelpler storePickupSiteHelpler;

	@Autowired
	private StorePickupSiteFacadeValidator storePickupSiteFacadeValidator;

	@Autowired
	private StoreTimeBucketService storeTimeBucketService;

	/**
	 * 创建(修改)自提点
	 *
	 * @param storePickupSiteCreatedOrUpdated 参数
	 * @return 自提点信息
	 * @throws T5weiException
	 */
	@Override
	public StorePickupSiteDTO createOrUpdateStorePickupSite(StorePickupSiteCreatedOrUpdatedParam storePickupSiteCreatedOrUpdated)
			throws T5weiException, TException {

		//参数校验
		storePickupSiteFacadeValidator.validatePickupSiteCreatedOrUpdateParam(storePickupSiteCreatedOrUpdated);
		List<StorePickupSiteTimeSettingParam> storePickupSiteTimeSettingParams = storePickupSiteCreatedOrUpdated.getStorePickupSiteTimeSettings();
		List<StorePickupSiteTimeSettingParam> updatePickupSiteTimeSettingParams = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(storePickupSiteTimeSettingParams)) {
			List<Long> timeBucketIds = Lists.newArrayList();
			for (StorePickupSiteTimeSettingParam storePickupSiteTimeSettingParam : storePickupSiteTimeSettingParams) {
				if(storePickupSiteTimeSettingParam.getPickupTime() > 0 || storePickupSiteTimeSettingParam.getOrderCutOffTime() > 0) {
					updatePickupSiteTimeSettingParams.add(storePickupSiteTimeSettingParam);
					timeBucketIds.add(storePickupSiteTimeSettingParam.getTimeBucketId());
				}
			}
			if (CollectionUtils.isNotEmpty(updatePickupSiteTimeSettingParams)) {
				Map<Long, StoreTimeBucket> storeTimeBucketMap = this.storeTimeBucketService.getStoreTimeBucketMapInIds(
						storePickupSiteCreatedOrUpdated.getMerchantId(), storePickupSiteCreatedOrUpdated.getStoreId(), timeBucketIds);
				storePickupSiteFacadeValidator.validatePickupSiteTimeSettingParams(storeTimeBucketMap, updatePickupSiteTimeSettingParams);
			}
			storePickupSiteCreatedOrUpdated.setStorePickupSiteTimeSettings(updatePickupSiteTimeSettingParams);
		}
		//参数转换
		StorePickupSiteSaveOrUpdateParam storePickupSiteSaveOrUpdate =
				storePickupSiteHelpler.getSaveOrUpdateParamFromThriftParam(storePickupSiteCreatedOrUpdated);
		return storePickupSiteSevice.saveOrUpdateStorePickupSite(storePickupSiteSaveOrUpdate);
	}

	/**
	 * 查询自提点详情
	 *
	 * @param merchantId
	 * @param storeId
	 * @param storePickupSiteId
	 */
	@Override
	public StorePickupSiteDTO getStorePickupSiteInfo(int merchantId, long storeId, long storePickupSiteId)
			throws T5weiException, TException {
		storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
		return this.storePickupSiteSevice.getStorePickupSiteInfo(merchantId, storeId, storePickupSiteId);
	}

	/**
	 * 获取店铺自提点列表
	 *
	 * @param merchantId
	 * @param storeId
	 */
	@Override
	public List<StorePickupSiteDTO> getStorePickupSiteBaseInfosByStoreId(int merchantId, long storeId)
			throws T5weiException, TException {
		storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
		List<StorePickupSite> storePickupSites = this.storePickupSiteSevice.getStorePickupSiteBaseInfosByStoreId(merchantId, storeId);
		List<StorePickupSiteDTO> pickupSiteDTOs = Lists.newArrayList();
		if (CollectionUtils.isNotEmpty(storePickupSites)) {
			for (StorePickupSite pickupSite : storePickupSites) {
				StorePickupSiteDTO pickupSiteDTO = this.storePickupSiteSevice.getStorePickupSiteInfo(merchantId, storeId, pickupSite.getStorePickupSiteId());
				if (pickupSiteDTO != null) {
					pickupSiteDTOs.add(pickupSiteDTO);
				}
			}
		}
		return pickupSiteDTOs;
	}

	/**
	 * 获取店铺下自提点的数量
	 *
	 * @param merchantId
	 * @param storeId
	 */
	@Override
	public StorePickupSiteCountInfoDTO getStorePickupSiteCount(int merchantId, long storeId) throws T5weiException, TException {
		storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
		return this.storePickupSiteSevice.getStorePickupSiteCount(merchantId, storeId);
	}

	/**
	 * 获取店铺自提点map
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteIds
	 */
	@Override
	public Map<Long, StorePickupSiteDTO> getStorePickupSiteMapByIds(int merchantId, long storeId, List<Long> pickupSiteIds)
			throws TException {
		return this.storePickupSiteSevice.getStorePickupSiteMapByIds(merchantId, storeId, pickupSiteIds);
	}
}
