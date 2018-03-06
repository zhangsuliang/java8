package com.huofu.module.i5wei.pickupsite.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteEnterpriseDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.dialog.resourcevisit.ResourceVisitParam;
import huofucore.facade.dialog.resourcevisit.UserResourceVisitDTO;
import huofucore.facade.dialog.resourcevisit.UserResourceVisitFacade;
import huofucore.facade.dialog.resourcevisit.UserResourceVisitType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StorePickupSiteDeliveryDTO;
import huofucore.facade.i5wei.pickupsite.*;
import huofucore.facade.merchant.preferential.MerchantEnterpriseFacade;
import huofucore.facade.merchant.preferential.MerchantEnterpriseResp;
import huofucore.facade.merchant.preferential.MerchantEnterpriseStaffFacade;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by taoming on 2016/12/8.
 */

@Service
public class StorePickupSiteHelpler {

	private Log log = LogFactory.getLog(StorePickupSiteHelpler.class);


	@ThriftClient
	private UserResourceVisitFacade.Iface userResourceVisitFacade;

	@Autowired
	private StorePickupSiteService storePickupSiteService;

	@Autowired
	private UserDefaultPickupSiteService userDefaultPickupSiteService;

	@Autowired
	private StoreTimeBucketService storeTimeBucketService;

	@Autowired
	private StorePickupSiteEnterpriseDAO storePickupSiteEnterpriseDAO;

	@ThriftClient
	private MerchantEnterpriseStaffFacade.Iface merchantEnterpriseStaffFacade;

	@ThriftClient
	private MerchantEnterpriseFacade.Iface merchantEnterpriseFacadeIface;

	@Autowired
	private Store5weiSettingService store5weiSettingService;

	@Autowired
	private StoreOrderService storeOrderService;

	@Autowired
	private StorePickupSiteTimeSettingService storePickupSiteTimeSettingService;


	public StorePickupSiteBaseDTO getStorePickupSiteDTOByEntity(StorePickupSite storePickupSite) {
		if (storePickupSite == null) {
			return new StorePickupSiteBaseDTO();
		}
		StorePickupSiteBaseDTO orderDTO = BeanUtil.copy(storePickupSite, StorePickupSiteBaseDTO.class);
		return orderDTO;
	}

	/**
	 * 返回组装类型
	 *
	 * @param createOrUpdatethriftParam
	 */
	public StorePickupSiteSaveOrUpdateParam getSaveOrUpdateParamFromThriftParam(
			StorePickupSiteCreatedOrUpdatedParam createOrUpdatethriftParam) {

		StorePickupSiteSaveOrUpdateParam storePickupSiteSaveOrUpdateParam = new StorePickupSiteSaveOrUpdateParam();

		BeanUtil.copy(createOrUpdatethriftParam, storePickupSiteSaveOrUpdateParam);

		//自提点名称赋值
		if (createOrUpdatethriftParam.isSetStorePickupSiteName() &&
		    StringUtils.isNotEmpty(createOrUpdatethriftParam.getStorePickupSiteName())) {
			storePickupSiteSaveOrUpdateParam.setStorePickupSiteName(createOrUpdatethriftParam.getStorePickupSiteName());
		}

		//自提点地址赋值
		if (createOrUpdatethriftParam.isSetStorePickupSiteAddress()
		    && StringUtils.isNotEmpty(createOrUpdatethriftParam.getStorePickupSiteAddress())) {
			storePickupSiteSaveOrUpdateParam.setStorePickupSiteAddress(createOrUpdatethriftParam.getStorePickupSiteAddress());
		} else {
			storePickupSiteSaveOrUpdateParam.setStorePickupSiteAddress("");
		}

		//企业信息赋值
		if (createOrUpdatethriftParam.isSetEnterpriseIds() && CollectionUtils.isNotEmpty(createOrUpdatethriftParam.getEnterpriseIds())) {
			storePickupSiteSaveOrUpdateParam.setEnterpriseIds(createOrUpdatethriftParam.getEnterpriseIds());
		}

		//设置开放类型
		if (createOrUpdatethriftParam.isSetStorePickupSiteOpenType()
		    && createOrUpdatethriftParam.getStorePickupSiteOpenType().getValue() > 0) {
			storePickupSiteSaveOrUpdateParam.setStorePickupSiteOpenType(createOrUpdatethriftParam.getStorePickupSiteOpenType());
		} else {
			storePickupSiteSaveOrUpdateParam.setStorePickupSiteOpenType(StorePickupSiteOpenTypeEnum.DEFAULT);
		}

		//设置自提点是否关闭
		if (createOrUpdatethriftParam.isSetEnabled()) {
			if (createOrUpdatethriftParam.isEnabled()) {
				storePickupSiteSaveOrUpdateParam.setDisabled(false);
			} else {
				storePickupSiteSaveOrUpdateParam.setDisabled(true);
			}
		}
		List<StorePickupSiteTimeSettingParam> storePickupSiteTimeSettings = createOrUpdatethriftParam.getStorePickupSiteTimeSettings();
		if (CollectionUtils.isNotEmpty(storePickupSiteTimeSettings)) {
			List<StorePickupSiteTimeSetting> storePickupSiteTimeSettingList = new ArrayList<>();
			StorePickupSiteTimeSetting storePickupSiteTimeSetting = null;
			for (StorePickupSiteTimeSettingParam storePickupSiteTimeSettingParam : storePickupSiteTimeSettings) {
				storePickupSiteTimeSetting = new StorePickupSiteTimeSetting();
				if (storePickupSiteTimeSettingParam.isSetDisabled()) {
					storePickupSiteTimeSetting.setDisabled(storePickupSiteTimeSettingParam.isDisabled());
				}
				if (storePickupSiteTimeSettingParam.isSetPickupTime()) {
					storePickupSiteTimeSetting.setPickupTime(storePickupSiteTimeSettingParam.getPickupTime());
				}
				if (storePickupSiteTimeSettingParam.isSetOrderCutOffTime()) {
					storePickupSiteTimeSetting.setOrderCutOffTime(storePickupSiteTimeSettingParam.getOrderCutOffTime());
				}
				storePickupSiteTimeSetting.setStorePickupSiteId(createOrUpdatethriftParam.getStorePickupSiteId());
				storePickupSiteTimeSetting.setTimeBucketId(storePickupSiteTimeSettingParam.getTimeBucketId());
				storePickupSiteTimeSettingList.add(storePickupSiteTimeSetting);
			}
			storePickupSiteSaveOrUpdateParam
					.setStorePickupSiteTimeSetting(storePickupSiteTimeSettingList);
		}
		return storePickupSiteSaveOrUpdateParam;
	}


	/**
	 * 自提点entity转换为自提点基础dto
	 *
	 * @param storePickupSites
	 * @return
	 */
	public List<StorePickupSiteBaseDTO> convertStorePickupSiteBaseDTOs(List<StorePickupSite> storePickupSites) {
		List<StorePickupSiteBaseDTO> storePickupSiteBaseDTOS = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(storePickupSites)) {
			StorePickupSiteBaseDTO storePickupSiteBaseDTO = null;
			for (StorePickupSite storePickupSite : storePickupSites) {
				storePickupSiteBaseDTO = new StorePickupSiteBaseDTO();
				storePickupSiteBaseDTO.setStoreId(storePickupSite.getStoreId());
				storePickupSiteBaseDTO.setMerchantId(storePickupSite.getMerchantId());
				storePickupSiteBaseDTO.setStorePickupSiteId(storePickupSite.getStorePickupSiteId());
				storePickupSiteBaseDTO.setStorePickupSiteAddress(storePickupSite.getStorePickupSiteAddress());
				storePickupSiteBaseDTO.setStorePickupSiteName(storePickupSite.getStorePickupSiteName());
				storePickupSiteBaseDTO.setDisabled(storePickupSite.isDisabled());
				storePickupSiteBaseDTOS.add(storePickupSiteBaseDTO);
			}
		}
		return storePickupSiteBaseDTOS;
	}


	/**
	 * 获取有效的用户自提点(有效的和无效分组)
	 *
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 * @param userId
	 * @return
	 */
	public StorePickupSiteForSelectDTO getStorePickupSiteForSelectDTO(int merchantId,
	                                                                  long storeId,
	                                                                  long timeBucketId,
	                                                                  long userId,
																	  String orderId) throws TException {

		StorePickupSiteForSelectDTO storePickupSiteForSelectDTO = null;

		//获取店面某一时段下开启的自提点
		List<StorePickupSiteBaseDTO> storePickupSiteBaseDTOS
				= this.storePickupSiteService.getStorePickupSiteBaseInfosByTimeBucketId(merchantId, storeId, timeBucketId);

		//有效无效分组
		//1、有效是指在对用户提供服务，无效是指无法对用户提供服务
		//2、无效有两种类型：时段不符合开放时间约定，不是其面向的企业用户
		if (CollectionUtils.isNotEmpty(storePickupSiteBaseDTOS)) {
            storePickupSiteForSelectDTO = new StorePickupSiteForSelectDTO();
			StoreOrder storeOrder = this.storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
			List<StorePickupSiteStatusDTO> availableStorePickupsites = new ArrayList<>();
			List<StorePickupSiteStatusDTO> notAvailableStorePickupsites = new ArrayList<>();

			long userEnterpriseId = this.getUserEnterprises(userId, merchantId);

			//首先判断就餐日期是否为当天
			boolean isTodayToRepast = checkRepastDate(storeOrder.getRepastDate());

			StorePickupSiteStatusDTO storePickupSiteStatusDTO = null;
			for (StorePickupSiteBaseDTO storePickupSiteBaseDTO : storePickupSiteBaseDTOS) {
				storePickupSiteStatusDTO = new StorePickupSiteStatusDTO();
				storePickupSiteStatusDTO.setStorePickupSite(storePickupSiteBaseDTO);
				StorePickupSiteTimeSettingDTO storePickupSiteTimeSettingDTO = storePickupSiteBaseDTO.getStorePickupSiteTimeSetting();
				//判断下单截止时间是否过期
				if (storePickupSiteTimeSettingDTO == null) {
					continue;
				}
				boolean cutoffTimeExpiredMark =
						checkCutoffTimeOfPickupSite(BeanUtil.copy(storePickupSiteTimeSettingDTO, StorePickupSiteTimeSetting.class), isTodayToRepast);
				if (cutoffTimeExpiredMark) {
					if (storePickupSiteBaseDTO.getSiteOpenType() == StorePickupSiteOpenTypeEnum.ALL_ORIENTED.getValue()) {
						availableStorePickupsites.add(storePickupSiteStatusDTO);
					} else {
						//判断是否加入了自提点协议企业
						boolean hasJoinMark =
								verifyUserHasJoinEnterprise(merchantId, storeId, storePickupSiteBaseDTO.getStorePickupSiteId(),
								                            userEnterpriseId);
						if (hasJoinMark) {
							availableStorePickupsites.add(storePickupSiteStatusDTO);
						}
					}
				} else {
					notAvailableStorePickupsites.add(storePickupSiteStatusDTO);
				}
			}
			storePickupSiteForSelectDTO.setAvailableStorePickupsites(availableStorePickupsites);
			storePickupSiteForSelectDTO.setNotAvailableStorePickupsites(notAvailableStorePickupsites);
		}
		return storePickupSiteForSelectDTO;
	}

	/**
	 * 判断截止时间是否有效
	 *
	 * @param storePickupSiteTimeSetting
	 * @return
	 */
	public boolean checkCutoffTimeOfPickupSite(StorePickupSiteTimeSetting storePickupSiteTimeSetting, boolean flag) throws T5weiException {

		long millisecondOfNow = System.currentTimeMillis();
		long millisecondOfDayStart = DateUtil.getBeginTime(millisecondOfNow, null);
		boolean isAvailable = false;
		if (storePickupSiteTimeSetting != null && !storePickupSiteTimeSetting.isDisabled()) {
			if (!flag) {
				isAvailable = true;
			} else {
				isAvailable = millisecondOfDayStart + storePickupSiteTimeSetting.getOrderCutOffTime() > millisecondOfNow;
			}
		}
		return isAvailable;
	}

	/**
	 * 判断就餐日期是否为今天
	 * @return
	 */
	public boolean checkRepastDate(long repastDateMills) {
		MutableDateTime nowDate = new MutableDateTime(System.currentTimeMillis());
		MutableDateTime repastDate = new MutableDateTime(repastDateMills);
		if(repastDate.getYear() > nowDate.getYear()) {
			return false;
		}
		if(repastDate.getDayOfYear() > nowDate.getDayOfYear()) {
			return false;
		}
		return true;
	}

	/**
	 * 获取用户在某一商户下的协议企业ID
	 *
	 * @param userId
	 * @param merchantId
	 * @return
	 * @throws TException
	 */
	public long getUserEnterprises(long userId, int merchantId) throws TException {
		MerchantEnterpriseResp merchantEnterpriseResp = merchantEnterpriseStaffFacade.getMerchanterprise4User(userId, merchantId);
		if (merchantEnterpriseResp == null || merchantEnterpriseResp.getMerchantEnterpriseDTO() == null) {
			return 0;
		}
		return merchantEnterpriseResp.getMerchantEnterpriseDTO().getEnterpriseId();
	}

	/**
	 * 判断用户是否关联了协议企业
	 *
	 * @param merchantId
	 */
	public boolean verifyUserHasJoinEnterprise(int merchantId, long storeId, long pickupSiteId, long userEnterpriseId) {
		List<Long> storePickupSiteEnterprises =
				this.storePickupSiteEnterpriseDAO.getEnterpricesByPickupSiteId(merchantId, storeId, pickupSiteId);
		if (CollectionUtils.isEmpty(storePickupSiteEnterprises)) {
			return false;
		}
		return storePickupSiteEnterprises.contains(userEnterpriseId);
	}

	/**
	 * 包装自StorePickupSiteForSelectDTO对象，构造newmark和default属性
	 *
	 * @param userId
	 * @param storePickupSiteForSelectDTO
	 * @return
	 */
	public StorePickupSiteForSelectDTO wrapperStorePickupSiteForSelectDTO(long userId, long timeBucketId,
	                                                                      StorePickupSiteForSelectDTO storePickupSiteForSelectDTO)
			throws TException {

		//查询用户默认自提点
		long userDefaultPickupSite = this.userDefaultPickupSiteService.getUserTimeBucketDefaultPickupSite(userId, timeBucketId);

		//查询最后访问时间
		long visitLastTime = getUserResourceVisitDTO(userId, timeBucketId, UserResourceVisitType.TIME_BUCKET);
		wrapperStorePickupSiteStatusDTOs(userDefaultPickupSite, visitLastTime, storePickupSiteForSelectDTO.getAvailableStorePickupsites());
		wrapperStorePickupSiteStatusDTOs(userDefaultPickupSite, visitLastTime,
		                                 storePickupSiteForSelectDTO.getNotAvailableStorePickupsites());
		return storePickupSiteForSelectDTO;
	}

	/**
	 * 包装StorePickupSiteStatusDTO，构造newmark和default属性
	 *
	 * @param storePickupSiteStatusDTOS
	 * @return
	 */
	public List<StorePickupSiteStatusDTO> wrapperStorePickupSiteStatusDTOs(long userDefaultPickupSite, long visitLastTime,
	                                                                       List<StorePickupSiteStatusDTO> storePickupSiteStatusDTOS)
			throws TException {

		if (CollectionUtils.isNotEmpty(storePickupSiteStatusDTOS)) {
			for (StorePickupSiteStatusDTO storePickupSiteStatusDTO : storePickupSiteStatusDTOS) {
				if (visitLastTime > 0 && storePickupSiteStatusDTO.storePickupSite.getCreateTime() > visitLastTime) {
					storePickupSiteStatusDTO.setNewMark(true);
				} else {
					storePickupSiteStatusDTO.setNewMark(false);
				}
				if (storePickupSiteStatusDTO.storePickupSite.getStorePickupSiteId() == userDefaultPickupSite) {
					storePickupSiteStatusDTO.setDefaultMark(true);
				} else {
					storePickupSiteStatusDTO.setDefaultMark(false);
				}
			}
			return storePickupSiteStatusDTOS;
		}
		return Collections.EMPTY_LIST;
	}


	/**
	 * 判断是否要弹窗提示用户
	 *
	 * @param userId
	 * @param timeBucketId
	 * @param storePickupSiteForSelectDTO
	 * @return
	 * @throws TException
	 */
	public StorePickupSiteAlertCheckDTO getStorePickupSiteAlertCheckDTO(long userId, long timeBucketId,
	                                                                    StorePickupSiteForSelectDTO storePickupSiteForSelectDTO)
			throws TException {

		StorePickupSiteAlertCheckDTO storePickupSiteAlertCheckDTO = new StorePickupSiteAlertCheckDTO();
		storePickupSiteAlertCheckDTO.setEssentialMark(false);
		storePickupSiteAlertCheckDTO.setUserMark(false);
		storePickupSiteAlertCheckDTO.setNewPickupSiteMark(false);

		//查询用户默认自提点
		long userDefaultPickupSite = this.userDefaultPickupSiteService.getUserTimeBucketDefaultPickupSite(userId, timeBucketId);

		//查询最后访问时间
		long visitLastTime = getUserResourceVisitDTO(userId, timeBucketId, UserResourceVisitType.TIME_BUCKET);
		if (visitLastTime > 0) {
			storePickupSiteAlertCheckDTO.setUserMark(false);
		} else {
			storePickupSiteAlertCheckDTO.setUserMark(true);
		}

        storePickupSiteAlertCheckDTO.setNotAvailableStorePickupsites(storePickupSiteForSelectDTO.getNotAvailableStorePickupsites());
        storePickupSiteAlertCheckDTO.setAvailableStorePickupsites(storePickupSiteForSelectDTO.getAvailableStorePickupsites());

		//新用户并且有可用的自提点，弹窗提示
		if (visitLastTime == 0 && CollectionUtils.isNotEmpty(storePickupSiteForSelectDTO.getAvailableStorePickupsites())) {
            storePickupSiteAlertCheckDTO.setEssentialMark(true);
        } else if(visitLastTime == 0 && CollectionUtils.isNotEmpty(storePickupSiteAlertCheckDTO.getNotAvailableStorePickupsites())) {
            storePickupSiteAlertCheckDTO.setNewPickupSiteMark(true);
        }

		/**
		 * 老用户情况判断：
		 * 		1：如果用户没有默认自提点
		 * 			1）如果有新的可用自提点，则弹窗提示
		 * 			2）没有新的可用自提点，判断是否有新的不可用自提点，如果有，则文字提示
		 * 		2：如果用户有默认自提点
		 * 			1）如果有新的可用自提点，则文字提示
		 * 			2）如果没有新的可用自提点，判断是否有新的不可用自提点，如果有，则文字提示
		 */
		if (visitLastTime > 0) {
			boolean essentialMark = false;
			boolean newPickupSiteMark = false;
			for (StorePickupSiteStatusDTO storePickupSiteStatusDTO : storePickupSiteForSelectDTO.getAvailableStorePickupsites()) {
				if (storePickupSiteStatusDTO.isNewMark()) {
					essentialMark = true;
					break;
				}
			}
			if (essentialMark) {
				if (userDefaultPickupSite <= 0) {
					storePickupSiteAlertCheckDTO.setEssentialMark(true);
				} else {
					storePickupSiteAlertCheckDTO.setNewPickupSiteMark(true);
				}
			} else {
				for (StorePickupSiteStatusDTO storePickupSiteStatusDTO : storePickupSiteForSelectDTO.getNotAvailableStorePickupsites()) {
					if (storePickupSiteStatusDTO.isNewMark()) {
						newPickupSiteMark = true;
						break;
					}
				}
				if(newPickupSiteMark) {
					storePickupSiteAlertCheckDTO.setNewPickupSiteMark(true);
				}
			}
		}
		setUserVisitMark(userId, timeBucketId);
		return storePickupSiteAlertCheckDTO;
	}

	//设置老用户
	public void setUserVisitMark(long userId, long timeBucketId) throws TException {
		ResourceVisitParam resourceVisitParam = new ResourceVisitParam();
		resourceVisitParam.setResourceType(UserResourceVisitType.TIME_BUCKET.getValue());
		resourceVisitParam.setResourceIds(Lists.newArrayList(timeBucketId));
		userResourceVisitFacade.saveUserResourceVisits(userId, Lists.newArrayList(resourceVisitParam));
	}

	/**
	 * 获取用户最后访问时间
	 *
	 * @param userId
	 * @param resourceValue
	 * @param userResourceVisitType
	 * @return
	 * @throws TException
	 */
	public long getUserResourceVisitDTO(long userId, long resourceValue, UserResourceVisitType userResourceVisitType) throws TException {
		ResourceVisitParam resourceVisitParam = new ResourceVisitParam();
		resourceVisitParam.setResourceType(userResourceVisitType.getValue());
		resourceVisitParam.setResourceIds(Lists.newArrayList(resourceValue));
		Map<Integer, List<UserResourceVisitDTO>> resourceVisitMap =
				userResourceVisitFacade.getUserResourceVisitMap(userId, Lists.newArrayList(resourceVisitParam));
		if (resourceVisitMap != null) {
			List<UserResourceVisitDTO> resourceVisitDTOS = resourceVisitMap.get(userResourceVisitType.getValue());
			if (CollectionUtils.isNotEmpty(resourceVisitDTOS)) {
				return resourceVisitDTOS.get(0).getVisitTime();
			}
		}
		return 0L;
	}

	public StorePickupSiteDTO buildStorePickupSiteDTO(StorePickupSite storePickupSite,
	                                                  List<StorePickupSiteTimeSetting> storePickupSiteTimeSettings,
	                                                  List<Long> enterpriseIds) throws TException {
		List<StorePickupSiteTimeSettingDTO> storePickupSiteTimeSettingsForSet = new ArrayList<>();
		List<StoreTimeBucket> storeTimeBuckets = storeTimeBucketService.getStoreTimeBucketListForStore(storePickupSite.getMerchantId(),
		                                                                                               storePickupSite.getStoreId());
		Map<String, StorePickupSiteTimeSetting> storePickupSiteTimeSettingMap = new HashMap<>();

		if (CollectionUtils.isNotEmpty(storeTimeBuckets)) {
			for (StorePickupSiteTimeSetting storePickupSiteTimeSetting : storePickupSiteTimeSettings) {
				storePickupSiteTimeSettingMap.put(String.valueOf(storePickupSiteTimeSetting.getTimeBucketId()), storePickupSiteTimeSetting);
			}
			for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
				StorePickupSiteTimeSetting storePickupSiteTimeSetting =
						storePickupSiteTimeSettingMap.get(String.valueOf(storeTimeBucket.getTimeBucketId()));
				StorePickupSiteTimeSettingDTO storePickupSiteTimeSettingDTO = new StorePickupSiteTimeSettingDTO();
				if (storePickupSiteTimeSetting != null) {
					storePickupSiteTimeSettingDTO.setTimeBucketName(storeTimeBucket.getName());
					storePickupSiteTimeSettingDTO.setStorePickupSiteId(storePickupSite.getStorePickupSiteId());
					BeanUtil.copy(storePickupSiteTimeSetting,storePickupSiteTimeSettingDTO);
				} else {
					storePickupSiteTimeSettingDTO.setTimeBucketName(storeTimeBucket.getName());
					storePickupSiteTimeSettingDTO.setStorePickupSiteId(storePickupSite.getStorePickupSiteId());
					storePickupSiteTimeSettingDTO.setTimeBucketId(storeTimeBucket.getTimeBucketId());
					storePickupSiteTimeSettingDTO.setDisabled(true);
				}
				storePickupSiteTimeSettingDTO.setStartTime(storeTimeBucket.getStartTime());
				storePickupSiteTimeSettingDTO.setEndTime(storeTimeBucket.getEndTime());
				storePickupSiteTimeSettingsForSet.add(storePickupSiteTimeSettingDTO);
			}
		}
		StorePickupSiteDTO storePickupSiteDTO = new StorePickupSiteDTO();
		BeanUtil.copy(storePickupSite,storePickupSiteDTO);
		storePickupSiteDTO.setStorePickupSiteOpenType(StorePickupSiteOpenTypeEnum.findByValue(storePickupSite.getSiteOpenType()));
		storePickupSiteDTO.setEnterpriseIds(enterpriseIds);
		storePickupSiteDTO.setStorePickupSiteTimeSetting(storePickupSiteTimeSettingsForSet);
		return storePickupSiteDTO;
	}

	/**
	 * 组装自提点相关信息
	 * @param storePickupSiteDeliverys
	 * @param merchantId
	 */
	public void addPickupSiteInformation(List<StorePickupSiteDeliveryDTO> storePickupSiteDeliverys, int merchantId) throws TException {
		for(StorePickupSiteDeliveryDTO storePickupSiteDelivery : storePickupSiteDeliverys) {
			StorePickupSiteBaseDTO storePickupSiteBase = storePickupSiteDelivery.getStorePickupSiteInfo();
			long storePickupSiteId = storePickupSiteBase.getStorePickupSiteId();
			if(storePickupSiteId > 0) {
				Map<Long, StorePickupSiteDTO> infoMap = this.storePickupSiteService.getStorePickupSiteMapByIds(
						merchantId, storePickupSiteBase.getStoreId(), Lists.newArrayList(storePickupSiteId));
				StorePickupSiteDTO storePickupSiteDTO = infoMap.get(storePickupSiteId);
				if (storePickupSiteDTO != null) {
					BeanUtils.copyProperties(storePickupSiteDTO, storePickupSiteBase);
				}
			}
		}
	}

	/**
	 * 组装营业时段相关信息
	 * @param storePickupSiteDeliverys
	 * @param merchantId
	 */
	public void addPickupSiteTimeBucketInfo(List<StorePickupSiteDeliveryDTO> storePickupSiteDeliverys, int merchantId) throws TException {
		Long storeId;
		long storePickupSiteId;
		long pickupSiteTimeSettingId;
		for(StorePickupSiteDeliveryDTO storePickupSiteDelivery : storePickupSiteDeliverys) {
			StorePickupSiteTimeSettingDTO pickupSiteTimeSettingDTO = storePickupSiteDelivery.getStorePickupSiteInfo().getStorePickupSiteTimeSetting();
			storeId = storePickupSiteDelivery.getStorePickupSiteInfo().getStoreId();
			storePickupSiteId = storePickupSiteDelivery.getStorePickupSiteInfo().getStorePickupSiteId();
			pickupSiteTimeSettingId = pickupSiteTimeSettingDTO.getTimeBucketId();
			if(storePickupSiteId > 0 && pickupSiteTimeSettingId > 0) {
				StorePickupSiteTimeSetting storePickupSiteTimeSetting = this.storePickupSiteTimeSettingService.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(
						merchantId, storeId, storePickupSiteId, pickupSiteTimeSettingId);
				if(storePickupSiteTimeSetting != null) {
					pickupSiteTimeSettingDTO.setOrderCutOffTime(storePickupSiteTimeSetting.getOrderCutOffTime());
					pickupSiteTimeSettingDTO.setPickupTime(storePickupSiteTimeSetting.getPickupTime());
				}
			}
		}
	}

}
