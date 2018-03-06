package com.huofu.module.i5wei.pickupsite.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteDAO;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteEnterpriseDAO;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteTimeSettingDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteBaseDTO;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteCountInfoDTO;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteDTO;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteTimeSettingDTO;
import huofuhelper.util.DataUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StorePickupSiteService {

	private final static Log log = LogFactory.getLog(StorePickupSiteService.class);

	@Autowired
	private StorePickupSiteDAO storePickupSiteDAO;

	@Autowired
	private StorePickupSiteTimeSettingDAO storePickupSiteTimeSettingDAO;

	@Autowired
	private StorePickupSiteEnterpriseDAO storePickupSiteEnterpriseDAO;

	@Autowired
	private StorePickupSiteHelpler storePickupSiteHelpler;

	@Autowired
	private StorePickupSiteEnterpriseService storePickupSiteEnterpriseService;

	/**
	 * 创建或者更新自提点信息
	 *
	 * @param storePickupSiteSaveOrUpdate
	 * @return
	 * @throws TException
	 */
	public StorePickupSiteDTO saveOrUpdateStorePickupSite(StorePickupSiteSaveOrUpdateParam
			                                                      storePickupSiteSaveOrUpdate) throws TException {

		long pickupSiteId = storePickupSiteSaveOrUpdate.getStorePickupSiteId();
		long storeId = storePickupSiteSaveOrUpdate.getStoreId();
		int merchantId = storePickupSiteSaveOrUpdate.getMerchantId();

		if (pickupSiteId > 0) {

			StorePickupSite storePickupSite = storePickupSiteDAO.getStorePickupSiteById(merchantId, storeId, pickupSiteId);
			storePickupSite.snapshot();
			if (StringUtils.isNotEmpty(storePickupSiteSaveOrUpdate.getStorePickupSiteName())) {
				storePickupSite.setStorePickupSiteName(storePickupSiteSaveOrUpdate.getStorePickupSiteName());
			}
			if (StringUtils.isNotEmpty(storePickupSiteSaveOrUpdate.getStorePickupSiteAddress())) {
				storePickupSite.setStorePickupSiteAddress(storePickupSiteSaveOrUpdate.getStorePickupSiteAddress());
			}
			if (storePickupSiteSaveOrUpdate.getStorePickupSiteOpenType() != null &&
			    storePickupSiteSaveOrUpdate.getStorePickupSiteOpenType().getValue() > 0) {
				storePickupSite.setSiteOpenType(storePickupSiteSaveOrUpdate.getStorePickupSiteOpenType().getValue());
				if (storePickupSiteSaveOrUpdate.getStorePickupSiteOpenType().getValue() == 1) {
					storePickupSiteEnterpriseService
							.addEnterpriseOfPickupSite(merchantId, storeId, pickupSiteId, Lists.newArrayList());
				} else {
					storePickupSiteEnterpriseService
							.addEnterpriseOfPickupSite(merchantId, storeId, pickupSiteId, storePickupSiteSaveOrUpdate.getEnterpriseIds());
				}
			}
			if (storePickupSiteSaveOrUpdate.isDisabled() != null) {
				storePickupSite.setDisabled(storePickupSiteSaveOrUpdate.isDisabled());
			}
			storePickupSite.setUpdateTime(System.currentTimeMillis());
			storePickupSite.update();
			List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings = storePickupSiteSaveOrUpdate.getStorePickupSiteTimeSetting();
			if (CollectionUtils.isNotEmpty(storePickupSiteTimeSettings)) {
				this.storePickupSiteTimeSettingDAO.createPickupSiteTimeSetting(merchantId, storeId, storePickupSiteTimeSettings);
			}
		} else {
			StorePickupSite storePickupSite = new StorePickupSite();
			storePickupSite.setStorePickupSiteName(storePickupSiteSaveOrUpdate.getStorePickupSiteName());
			storePickupSite.setStorePickupSiteAddress(storePickupSiteSaveOrUpdate.getStorePickupSiteAddress());
			storePickupSite.setMerchantId(merchantId);
			storePickupSite.setStoreId(storeId);
			storePickupSite.setCreateTime(System.currentTimeMillis());
			storePickupSite.setUpdateTime(System.currentTimeMillis());
			this.storePickupSiteDAO.create(storePickupSite);
			pickupSiteId = storePickupSite.getStorePickupSiteId();
		}
		return getStorePickupSiteInfo(merchantId, storeId, pickupSiteId);
	}


	public StorePickupSiteCountInfoDTO getStorePickupSiteCount(int merchantId, long storeId) throws TException {
		StorePickupSiteCountInfoDTO storePickupSiteCountInfoDTO = new StorePickupSiteCountInfoDTO();
		storePickupSiteCountInfoDTO
				.setClosedCount(this.storePickupSiteDAO.getPickupSiteCountByStoreId(merchantId, storeId, NumberUtil.bool2Int(true)));
		storePickupSiteCountInfoDTO
				.setInServiceCount(this.storePickupSiteDAO.getPickupSiteCountByStoreId(merchantId, storeId, NumberUtil.bool2Int(false)));
		return storePickupSiteCountInfoDTO;
	}

	/**
	 * 查询自提点详情
	 *
	 * @param merchantId
	 * @param storeId
	 * @param storePickupSiteId
	 * @return
	 * @throws TException
	 */
	public StorePickupSiteDTO getStorePickupSiteInfo(int merchantId, long storeId, long storePickupSiteId) throws TException {

		StorePickupSite storePickupSite =
				this.storePickupSiteDAO.getStorePickupSiteById(merchantId, storeId, storePickupSiteId);

		if (storePickupSite == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_PICKUP_SITE_NOT_EXIST.getValue(),
			                         DataUtil.infoWithParams("store pickup site not exist, storeId=#1, pickupSiteId=#2 ",
			                                                 new Object[]{storeId, storePickupSiteId}));
		}

		List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings =
				this.storePickupSiteTimeSettingDAO.getPickupSiteTimeSettingBypickupSiteId(merchantId, storeId, storePickupSiteId);
		List<Long> enterpriseIds = this.storePickupSiteEnterpriseDAO.getEnterpricesByPickupSiteId(merchantId, storeId, storePickupSiteId);
		return storePickupSiteHelpler.buildStorePickupSiteDTO(storePickupSite, storePickupSiteTimeSettings, enterpriseIds);
	}

	/**
	 * 查询所有的自提点列表
	 *
	 * @param merchantId
	 * @param storeId
	 * @return
	 */
	public List<StorePickupSite> getStorePickupSiteBaseInfosByStoreId(int merchantId, long storeId) {
		return storePickupSiteDAO.getStorePickupSiteByStoreId(merchantId, storeId);
	}

	/**
	 * 查询店铺当前时段的下的自提点列表
	 *
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 * @return
	 * @throws T5weiException
	 * @throws TException
	 */
	public List<StorePickupSiteBaseDTO> getStorePickupSiteBaseInfosByTimeBucketId(int merchantId, long storeId, long timeBucketId)
			throws T5weiException, TException {

		List<StorePickupSiteBaseDTO> storePickupSiteBaseDTOS = new ArrayList<>();
		List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings
				= storePickupSiteTimeSettingDAO.getPickupSiteIdsByTimeBucketId(merchantId, storeId, timeBucketId);
		if (CollectionUtils.isNotEmpty(storePickupSiteTimeSettings)) {
			Map<String, StorePickupSiteTimeSetting> stringStorePickupSiteTimeSettingMap = new HashMap<>();
			List<Long> pickupSiteIds = new ArrayList<>();
			for (StorePickupSiteTimeSetting storePickupSiteTimeSetting : storePickupSiteTimeSettings) {
				pickupSiteIds.add(storePickupSiteTimeSetting.getStorePickupSiteId());
				stringStorePickupSiteTimeSettingMap
						.put(String.valueOf(storePickupSiteTimeSetting.getStorePickupSiteId()), storePickupSiteTimeSetting);
			}
			List<StorePickupSite> storePickupSites =
					this.storePickupSiteDAO.getStorePickupSiteByStoreId(merchantId, storeId, pickupSiteIds, false);
			if (CollectionUtils.isNotEmpty(storePickupSites)) {
				StorePickupSiteBaseDTO storePickupSiteBaseDTO = null;
				StorePickupSiteTimeSettingDTO storePickupSiteTimeSettingDTO = null;
				for (StorePickupSite storePickupSite : storePickupSites) {
					StorePickupSiteTimeSetting storePickupSiteTimeSetting = stringStorePickupSiteTimeSettingMap.get(
							String.valueOf(storePickupSite.getStorePickupSiteId()));
					storePickupSiteBaseDTO = new StorePickupSiteBaseDTO();
					storePickupSiteTimeSettingDTO = new StorePickupSiteTimeSettingDTO();
					BeanUtils.copyProperties(storePickupSite, storePickupSiteBaseDTO);
					if (storePickupSiteTimeSetting != null) {
						BeanUtils.copyProperties(storePickupSiteTimeSetting, storePickupSiteTimeSettingDTO);
						storePickupSiteBaseDTO.setStorePickupSiteTimeSetting(storePickupSiteTimeSettingDTO);
					}
					storePickupSiteBaseDTOS.add(storePickupSiteBaseDTO);
				}
			}
		}
		return storePickupSiteBaseDTOS;
	}

	/**
	 * 根据pickupIds集合查询list列表
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteIds
	 * @return
	 */
	public List<StorePickupSite> getStorePickupSitesByStoreIds(int merchantId, long storeId, List<Long> pickupSiteIds) {
		return storePickupSiteDAO.getStorePickupSiteByStoreId(merchantId, storeId, pickupSiteIds, true);
	}

	/**
	 * 根据pickupIds集合查询Map
	 *
	 * @param merchantId
	 * @param storeId
	 * @param pickupSiteIds
	 * @return
	 */
	public Map<Long, StorePickupSiteDTO> getStorePickupSiteMapByIds(int merchantId, long storeId, List<Long> pickupSiteIds) {
		Map<Long, StorePickupSiteDTO> storePickupSiteDTOMap = new HashMap<>();
		List<StorePickupSite> storePickupSites = this.getStorePickupSitesByStoreIds(merchantId, storeId, pickupSiteIds);
		if (CollectionUtils.isNotEmpty(storePickupSites)) {
			for (StorePickupSite storePickupSite : storePickupSites) {
				storePickupSiteDTOMap.put(storePickupSite.getStorePickupSiteId(), BeanUtil.copy(storePickupSite, StorePickupSiteDTO.class));
			}
		}
		return storePickupSiteDTOMap;
	}

}
