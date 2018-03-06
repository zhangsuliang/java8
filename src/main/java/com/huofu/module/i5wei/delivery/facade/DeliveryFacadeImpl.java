package com.huofu.module.i5wei.delivery.facade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import huofuhelper.util.DataUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.huofu.module.i5wei.delivery.entity.MerchantOrderDeliveryLast;
import com.huofu.module.i5wei.delivery.entity.StoreDeliveryBuilding;
import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.delivery.service.MerchantOrderDeliveryLastService;
import com.huofu.module.i5wei.delivery.service.StoreDeliveryBuildingService;
import com.huofu.module.i5wei.delivery.service.StoreDeliverySettingService;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucketUtil;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;

import huofucore.facade.i5wei.delivery.DeliveryFacade;
import huofucore.facade.i5wei.delivery.GpsDTO;
import huofucore.facade.i5wei.delivery.MerchantDeliveryModeEnum;
import huofucore.facade.i5wei.delivery.MerchantSingleMulitiEnum;
import huofucore.facade.i5wei.delivery.OrderDeliveryModelEnum;
import huofucore.facade.i5wei.delivery.StoreDeliveryBuildingDTO;
import huofucore.facade.i5wei.delivery.StoreDeliveryBuildingParam;
import huofucore.facade.i5wei.delivery.StoreDeliveryDTO;
import huofucore.facade.i5wei.delivery.StoreDeliveryInfoDTO;
import huofucore.facade.i5wei.delivery.StoreDeliveryPositionDTO;
import huofucore.facade.i5wei.delivery.StoreDeliveryPositionParam;
import huofucore.facade.i5wei.delivery.StoreDeliverySettingDTO;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.merchant.exception.TMerchantException;
import huofucore.facade.merchant.setting.MerchantSettingDTO;
import huofucore.facade.merchant.setting.MerchantSettingFacade;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.StoreFacade;
import huofuhelper.util.DateUtil;
import huofuhelper.util.GeoTool;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;

/**
 * Created by akwei on 5/6/15.
 */
@Component
@ThriftServlet(name = "deliveryFacadeServlet", serviceClass = DeliveryFacade.class)
public class DeliveryFacadeImpl implements DeliveryFacade.Iface {
	private static final Log log = LogFactory.getLog(DeliveryFacadeImpl.class);

	@Autowired
	private StoreDeliverySettingService storeDeliverySettingService;

	@Autowired
	private StoreDeliveryBuildingService storeDeliveryBuildingService;

	@Autowired
	private DeliveryValidator deliveryValidator;

	@ThriftClient
	private StoreFacade.Iface storeFacade;

	@ThriftClient
	private MerchantSettingFacade.Iface merchantSettingFacade;

	@Autowired
	private MerchantOrderDeliveryLastService merchantOrderDeliveryLastService;

	@Autowired
	private StoreTimeBucketService storeTimeBucketService;

	@Override
	public StoreDeliverySettingDTO saveStoreDeliverySetting(StoreDeliverySettingDTO param) throws TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
        this.deliveryValidator.validateDeliverySetting(param);
		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.saveStoreDeliverySetting(param);
		StoreDeliverySettingDTO storeDeliverySettingDTO = this.buildStoreDeliverySettingDTO(storeDeliverySetting);
		storeDeliverySettingDTO.setEarthPositionStatus(true);
		StoreDTO store = storeFacade.getStore(merchantId, storeId);
		if (store.getLatitude() == 0.0 && store.getLongitude() == 0.0) {
			storeDeliverySettingDTO.setEarthPositionStatus(false);
		}
		MerchantSettingDTO merchantSettingDTO = merchantSettingFacade.getMerchantSetting(merchantId);
		MerchantDeliveryModeEnum merchantDeliveryMode = MerchantDeliveryModeEnum.findByValue(merchantSettingDTO.getDeliveryMode().getValue());
		if (merchantDeliveryMode == null) {
			throw new T5weiException(T5weiErrorCodeType.MERCHANT_NO_DELIVERY_MODE.getValue(), "merchant no set delivery mode");
		}
		storeDeliverySettingDTO.setDeliveryMode(merchantDeliveryMode);
		return storeDeliverySettingDTO;
	}

	@Override
	public StoreDeliverySettingDTO getStoreDeliverySetting(int merchantId, long storeId) throws TException {
        if (merchantId <= 0 || storeId <= 0) {
            log.error("argument invalid merchantId:" + merchantId + ",storeId:" + storeId);
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId:" + merchantId + "," + "storeId:" + storeId);
        }

		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting(merchantId, storeId);
		StoreDeliverySettingDTO storeDeliverySettingDTO = this.buildStoreDeliverySettingDTO(storeDeliverySetting);
		storeDeliverySettingDTO.setEarthPositionStatus(true);
		StoreDTO store = storeFacade.getStore(merchantId, storeId);
		if (store.getLatitude() == 0.0 && store.getLongitude() == 0.0) {
			storeDeliverySettingDTO.setEarthPositionStatus(false);
		}
		MerchantSettingDTO merchantSettingDTO = merchantSettingFacade.getMerchantSetting(merchantId);
		MerchantDeliveryModeEnum merchantDeliveryMode = MerchantDeliveryModeEnum.findByValue(merchantSettingDTO.getDeliveryMode().getValue());
		if (merchantDeliveryMode == null) {
			throw new T5weiException(T5weiErrorCodeType.MERCHANT_NO_DELIVERY_MODE.getValue(), "merchant no set delivery mode");
		}
		storeDeliverySettingDTO.setDeliveryMode(merchantDeliveryMode);
		return storeDeliverySettingDTO;
	}

	@Override
	public StoreDeliveryBuildingDTO saveStoreDeliveryBuilding(StoreDeliveryBuildingParam param) throws T5weiException, TException {
		this.deliveryValidator.validateDeliveryBuilding(param);
		StoreDeliveryBuilding storeDeliveryBuilding = this.storeDeliveryBuildingService.saveStoreDeliveryBuilding(param);
		StoreDeliveryBuildingDTO storeDeliveryBuildingDTO = new StoreDeliveryBuildingDTO();
		BeanUtil.copy(storeDeliveryBuilding, storeDeliveryBuildingDTO);
		return storeDeliveryBuildingDTO;
	}

	@Override
	public List<StoreDeliveryBuildingDTO> getStoreDeliveryBuildings(int merchantId, long storeId) throws TException {
		List<StoreDeliveryBuilding> storeDeliveryBuildings = this.storeDeliveryBuildingService.getStoreDeliveryBuildings(merchantId, storeId, 100);
		return BeanUtil.copyList(storeDeliveryBuildings, StoreDeliveryBuildingDTO.class);
	}

	@Override
	public void deleteStoreDeliveryBuilding(int merchantId, long storeId, long buildingId) throws TException {
		this.storeDeliveryBuildingService.deleteStoreDeliveryBuilding(merchantId, storeId, buildingId);
	}

	private StoreDeliverySettingDTO buildStoreDeliverySettingDTO(StoreDeliverySetting storeDeliverySetting) {
		StoreDeliverySettingDTO storeDeliverySettingDTO = new StoreDeliverySettingDTO();
		BeanUtil.copy(storeDeliverySetting, storeDeliverySettingDTO);
		return storeDeliverySettingDTO;
	}

	@Override
	public StoreDeliveryPositionDTO getStoreDeliveryPosition(StoreDeliveryPositionParam param) throws T5weiException, TException {
		int merchantId = param.getMerchantId();
		List<StoreDTO> storeDTOs = storeFacade.getStoresByMerchantId(merchantId);
		Map<Long, StoreDTO> storeMaps = this.getStoreDTOMap(storeDTOs);

		MerchantSettingDTO merchantSetting = merchantSettingFacade.getMerchantSetting(merchantId);
		MerchantDeliveryModeEnum deliveryMode = MerchantDeliveryModeEnum.findByValue(merchantSetting.getDeliveryMode().getValue());

		StoreDeliveryPositionDTO storeDeliveryPositionDTO = new StoreDeliveryPositionDTO();
		if (param.getStoreId() > 0 || storeDTOs.size() == 1) {// 单店店铺
			return this.getSingleDeliveryStoreDTO(param, storeDTOs, storeMaps, merchantSetting);
		}

		if (deliveryMode == null || deliveryMode == MerchantDeliveryModeEnum.NO_DISTANCE) {// 非距离模式
			return this.getNoDistanceDeliveryStoreDTO(param, storeDTOs, storeMaps);
		}

		if (deliveryMode == MerchantDeliveryModeEnum.DISTINCE) {// 距离模式
			return this.getDistanceDeliveryStoreDTO(param, storeDTOs, storeMaps);
		}
		return storeDeliveryPositionDTO;
	}

	@Override
	public StoreDeliveryInfoDTO getStoreDeliveryInfo(int merchantId, long storeId) throws T5weiException, TException {
		long currentTime = System.currentTimeMillis();
		MerchantSettingDTO merchantSetting = merchantSettingFacade.getMerchantSetting(merchantId);
		StoreDTO storeDTO = storeFacade.getStore(merchantId, storeId);

		MerchantDeliveryModeEnum deliveryMode = MerchantDeliveryModeEnum.findByValue(merchantSetting.getDeliveryMode().getValue());
		StoreDeliverySetting storeDeliverySetting = storeDeliverySettingService.getStoreDeliverySetting(merchantId, storeId);

		StoreDeliveryInfoDTO storeDeliveryInfoDTO = new StoreDeliveryInfoDTO();
		if (deliveryMode == null || deliveryMode == MerchantDeliveryModeEnum.NO_DISTANCE) {
			// 楼宇列表
			List<StoreDeliveryBuilding> storeDeliveryBuildings = storeDeliveryBuildingService.getStoreDeliveryBuildings(merchantId, storeId, 100); // TODO:需要分页
			if (storeDeliveryBuildings != null) {
				List<StoreDeliveryBuildingDTO> storeDeliveryBuildingDTOs = BeanUtil.copyList(storeDeliveryBuildings, StoreDeliveryBuildingDTO.class);
				storeDeliveryInfoDTO.setStoreDeliveryBuildingDTOs(storeDeliveryBuildingDTOs);
			}
		}

		// 营业时段
		List<StoreTimeBucket> storeTimeBuckets = storeTimeBucketService.getStoreTimeBucketsInStoreForTime(merchantId, storeId, DateUtil.getBeginTime(currentTime, null));
		for (Iterator<StoreTimeBucket> iterator = storeTimeBuckets.iterator(); iterator.hasNext();) {
			StoreTimeBucket storeTimeBucket = (StoreTimeBucket) iterator.next();
			if (!storeTimeBucket.isDeliverySupported()) {
				iterator.remove();
			}
		}
		if (storeTimeBuckets != null) {
			List<StoreTimeBucketDTO> storeTimeBucketDTOs = BeanUtil.copyList(storeTimeBuckets, StoreTimeBucketDTO.class);
			storeDeliveryInfoDTO.setStoreTimeBucketDTOs(storeTimeBucketDTOs);
		}

		BeanUtil.copy(storeDTO, storeDeliveryInfoDTO);
		storeDeliveryInfoDTO.setMinOrderDeliveryAmount(storeDeliverySetting.getMinOrderDeliveryAmount());
		storeDeliveryInfoDTO.setDeliveryFee(storeDeliverySetting.getDeliveryFee());
		storeDeliveryInfoDTO.setMinOrderFreeDeliveryAmount(storeDeliverySetting.getMinOrderFreeDeliveryAmount());
		storeDeliveryInfoDTO.setDeliveryScope(storeDeliverySetting.getDeliveryScope());
		storeDeliveryInfoDTO.setAheadTime(storeDeliverySetting.getAheadTime());
		storeDeliveryInfoDTO.setDeliveryMode(deliveryMode);

		return storeDeliveryInfoDTO;
	}

	@Override
	public List<StoreDeliveryDTO> getStoreForDistanceDeliverys(int merchantId, long userId, double userLongitude, double userLatitude, int userScope) throws T5weiException, TException {
		Map<Long, StoreDeliverySetting> storeDeliverySettings = storeDeliverySettingService.getStoreDeliverySettingByMerchantId(merchantId, true);
		List<StoreDTO> storeDTOs = this.storeFacade.getStoresByMerchantId(merchantId);
		// 支持外送的店铺
		List<Long> storeIds = this.getStoreDeliveryIds(storeDTOs, storeDeliverySettings);
		// 在userScope范围内的店铺
		for (Iterator<Long> iterator = storeIds.iterator(); iterator.hasNext();) {
			Long storeId = iterator.next();
			for (StoreDTO storeDTO : storeDTOs) {
				if (storeDTO.getStoreId() == storeId && userLatitude != 0.0 && userLongitude != 0.0) {
					// 人与店铺的距离小于等于userScope + deliveryScope
					double userAndStoreDistance = GeoTool.getPointDistance(storeDTO.getLongitude(), storeDTO.getLatitude(), userLongitude, userLatitude);
					double deliveryScope = storeDeliverySettings.get(storeDTO.getStoreId()).getDeliveryScope();
					if (userAndStoreDistance > userScope + deliveryScope) {// 两个圆相离
						iterator.remove();
					}
					break;
				}
			}
		}

		// 存在外送营业时段的店铺
		Map<Long, List<StoreTimeBucket>> timeBucketMap = this.getStoreTimeBucketByMerchantId(merchantId, storeIds);
		Set<Long> storeDeliverys = timeBucketMap.keySet();

		for (Iterator<StoreDTO> iterator = storeDTOs.iterator(); iterator.hasNext();) {
			StoreDTO storeDTO = iterator.next();
			if (!storeDeliverys.contains(storeDTO.getStoreId())) {
				iterator.remove();
			}
		}
		return this.buildStoreDeliveryDTOs(storeDTOs, storeDeliverySettings);
	}

	@Override
	public List<StoreDeliveryDTO> getStoreForNoDistanceDeliverys(int merchantId) throws TMerchantException, TException {
		Map<Long, StoreDeliverySetting> storeDeliverySettings = storeDeliverySettingService.getStoreDeliverySettingByMerchantId(merchantId, true);
		List<StoreDTO> storeDTOs = this.storeFacade.getStoresByMerchantId(merchantId);
		List<Long> storeIds = this.getStoreDeliveryIds(storeDTOs, storeDeliverySettings);

		Map<Long, List<StoreTimeBucket>> timeBucketMap = this.getStoreTimeBucketByMerchantId(merchantId, storeIds);
		Set<Long> storeDeliverys = timeBucketMap.keySet();
		for (Iterator<StoreDTO> iterator = storeDTOs.iterator(); iterator.hasNext();) {
			StoreDTO storeDTO = iterator.next();
			if (!storeDeliverys.contains(storeDTO.getStoreId())) {
				iterator.remove();
			}
		}
		return this.buildStoreDeliveryDTOs(storeDTOs, storeDeliverySettings);
	}

	/**
	 * 支持外送的店铺Id集合
	 * 
	 * @param storeDTOs
	 * @param storeDeliverySettings
	 * @return
	 */
	private List<Long> getStoreDeliveryIds(List<StoreDTO> storeDTOs, Map<Long, StoreDeliverySetting> storeDeliverySettings) {
		List<Long> storeIds = new ArrayList<Long>();
		for (StoreDTO storeDTO : storeDTOs) {// 支持外送的店铺
			if (storeDeliverySettings.get(storeDTO.getStoreId()) != null && storeDeliverySettings.get(storeDTO.getStoreId()).isDeliverySupported()) {
				storeIds.add(storeDTO.getStoreId());
			}
		}
		return storeIds;
	}

	private long getStoreDeliveryId(int merchantId, List<Long> storeIds, Map<Long, List<StoreTimeBucket>> timeBucketMap) {
		for (Long storeId : storeIds) {
			if (timeBucketMap.get(storeId) != null && !timeBucketMap.get(storeId).isEmpty()) {
				return storeId;
			}
		}
		return 0L;
	}

	/**
	 * 获取非距离模式外送店铺
	 * 
	 * @param param
	 * @return
	 * @throws TMerchantException
	 * @throws TException
	 */
	private StoreDeliveryPositionDTO getNoDistanceDeliveryStoreDTO(StoreDeliveryPositionParam param, List<StoreDTO> stores, Map<Long, StoreDTO> storeMaps) throws TMerchantException, TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		long userId = param.getUserId();
		long currentTime = System.currentTimeMillis();
		long tenDaysBeforeTime = DateUtil.getBeginTime(currentTime, null) - 10 * 24 * 60 * 60 * 1000;

		Map<Long, StoreDeliverySetting> storeDeliverySettings = storeDeliverySettingService.getStoreDeliverySettingByMerchantId(merchantId, true);

		StoreDeliveryPositionDTO storeDeliveryPositionDTO = null;
		List<Long> storeIdsList = new ArrayList<Long>();
		for (StoreDTO storeDTO : stores) {
			StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDTO.getStoreId());
			if (storeDeliverySetting != null && storeDeliverySetting.isDeliverySupported()) {
				storeIdsList.add(storeDTO.getStoreId());
			}
		}

		Map<Long, List<StoreTimeBucket>> timeBucketMap = this.getStoreTimeBucketByMerchantId(merchantId, storeIdsList);
		Set<Long> storeIdsDeliverySet = timeBucketMap.keySet();// 支持外送的店铺

		boolean exitStoreDelivery = false;
		// 该商户下的店铺都不支持外卖 1.店铺都不支持外送；2.当前时间不支持外送
		if (storeIdsDeliverySet.size() == 0) {
			exitStoreDelivery = true;
			storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.NO_GPS_NO_STORE, MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, 0.0, 0.0, userId, 0);
		}

		// 十天内有消费过
		if (!exitStoreDelivery) {
			// 最近十天是否消费过，并且当天支持外卖的店铺
			List<Long> storeIdsDeliveryList = new ArrayList<Long>();
			List<MerchantOrderDeliveryLast> merchantOrderDeliveryLastsInTime = this.merchantOrderDeliveryLastService.getMerchantOrderDeliveryLastsByInTime(merchantId, storeId, userId,
					tenDaysBeforeTime, true);
			if (merchantOrderDeliveryLastsInTime != null && !merchantOrderDeliveryLastsInTime.isEmpty()) {
				for (Iterator<MerchantOrderDeliveryLast> iterator = merchantOrderDeliveryLastsInTime.iterator(); iterator.hasNext();) {
					MerchantOrderDeliveryLast merchantOrderDeliveryLast = iterator.next();
					if (storeIdsDeliverySet.contains(merchantOrderDeliveryLast.getStoreId())) {
						storeIdsDeliveryList.add(merchantOrderDeliveryLast.getStoreId());
					}
				}
			}
			if (storeIdsDeliveryList.size() > 0) {
				// 判断消费过的店铺是否支持外送
				long storeDeliveryId = this.getStoreDeliveryId(merchantId, storeIdsDeliveryList, timeBucketMap);
				if (storeDeliveryId > 0) {
					exitStoreDelivery = true;
					StoreDTO storeDelivery = storeMaps.get(storeDeliveryId);
					long timeBucketId = timeBucketMap.get(storeDeliveryId).get(0).getTimeBucketId();
					StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDeliveryId);
					storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(storeDelivery, storeDeliverySetting, OrderDeliveryModelEnum.NO_DISTANCE_STORE,
							MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, storeDelivery.getLongitude(), storeDelivery.getLatitude(), userId, timeBucketId);
				}
			}
		}

		// 十天内不支持外送或10天内没有消费过，取附近5公里内支持外送的店铺
		// 进入最近的支持外送的店铺
		if (!exitStoreDelivery && param.getUserLongitude() != 0.0 && param.getUserLatitude() != 0.0) {
			Map<Long, Double> storeIdsMap = new HashMap<Long, Double>();
			for (StoreDTO store : stores) {
				if (store.getLongitude() != 0.0 && store.getLatitude() != 0.0) {
					StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(store.getStoreId());
					if (storeDeliverySetting != null && storeDeliverySetting.isDeliverySupported()) {
						double userAndSroreDiatance = GeoTool.getPointDistance(store.getLongitude(), store.getLatitude(), param.getUserLongitude(), param.getUserLatitude());
						if (userAndSroreDiatance <= storeDeliverySetting.getDeliveryScope() + 5000) {// 外切
							storeIdsMap.put(store.getStoreId(), userAndSroreDiatance);
						}
					}
				}
			}
			// 对5公里内的店铺做升序排序
			List<Map.Entry<Long, Double>> list = new ArrayList<Map.Entry<Long, Double>>(storeIdsMap.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<Long, Double>>() {
				@Override
				public int compare(Entry<Long, Double> o1, Entry<Long, Double> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			});

			long storeDeliveryId = 0;
			for (Entry<Long, Double> entry : list) {
				List<StoreTimeBucket> timeBuckets = timeBucketMap.get(entry.getKey());
				if (timeBuckets != null && !timeBuckets.isEmpty()) {
					exitStoreDelivery = true;
					storeDeliveryId = entry.getKey();
					break;
				}
			}
			if (exitStoreDelivery) {
				StoreDTO storeDelivery = storeMaps.get(storeDeliveryId);
				StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDeliveryId);
				long timeBucketId = timeBucketMap.get(storeDeliveryId).get(0).getTimeBucketId();
				storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(storeDelivery, storeDeliverySetting, OrderDeliveryModelEnum.NO_DISTANCE_STORE,
						MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, storeDelivery.getLongitude(), storeDelivery.getLatitude(), userId, timeBucketId);
			}
		}

		// 没有找到外送店铺，需要显示店铺列表
		if (!exitStoreDelivery) {
			storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.NO_DISTANCE_NO_STORE, MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, 0.0, 0.0, userId, 0);
		}
		return storeDeliveryPositionDTO;
	}

	/**
	 * 获取距离模式的外送店铺
	 * 
	 * @param param
	 * @return
	 * @throws TMerchantException
	 * @throws TException
	 */
	private StoreDeliveryPositionDTO getDistanceDeliveryStoreDTO(StoreDeliveryPositionParam param, List<StoreDTO> stores, Map<Long, StoreDTO> storeMaps) throws TMerchantException, TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		long userId = param.getUserId();
		long currentTime = System.currentTimeMillis();
		long tenDaysBeforeTime = DateUtil.getBeginTime(currentTime, null) - 10 * 24 * 60 * 60 * 1000;

		Map<Long, StoreDeliverySetting> storeDeliverySettings = storeDeliverySettingService.getStoreDeliverySettingByMerchantId(merchantId, true);
		StoreDeliveryPositionDTO storeDeliveryPositionDTO = null;

		// 无用户GPS信息
		if (param.getUserLongitude() == 0.0 && param.getUserLatitude() == 0.0) {
			List<Long> sIds = new ArrayList<Long>();
			for (StoreDTO storeDTO : stores) {
				if (storeDTO.getLongitude() != 0.0 || storeDTO.getLatitude() != 0.0) {
					StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDTO.getStoreId());
					if (storeDeliverySetting != null && storeDeliverySetting.isDeliverySupported()) {
						sIds.add(storeDTO.getStoreId());
					}
				}
			}
			Map<Long, List<StoreTimeBucket>> timeBucketMap = this.getStoreTimeBucketByMerchantId(merchantId, sIds);

			// 10天内有外卖订单
			boolean exitStoreDelivery = false;
			List<MerchantOrderDeliveryLast> merchantOrderDeliveryLastsInTime = this.merchantOrderDeliveryLastService.getMerchantOrderDeliveryLastsByInTime(merchantId, storeId, userId,
					tenDaysBeforeTime, true);
			if (merchantOrderDeliveryLastsInTime != null && !merchantOrderDeliveryLastsInTime.isEmpty()) {
				List<Long> storeIds = new ArrayList<Long>();
				for (MerchantOrderDeliveryLast merchantOrderDeliveryLast : merchantOrderDeliveryLastsInTime) {
					StoreDTO storeDTO = storeMaps.get(merchantOrderDeliveryLast.getStoreId());
					if (storeDTO.getLongitude() != 0.0 || storeDTO.getLatitude() != 0.0) {
						storeIds.add(merchantOrderDeliveryLast.getStoreId());
					}
				}

				long storeDeliveryId = this.getStoreDeliveryId(merchantId, storeIds, timeBucketMap);
				if (storeDeliveryId > 0) {
					exitStoreDelivery = true;
					StoreDTO storeDelivery = storeMaps.get(storeDeliveryId);
					long timeBucketId = timeBucketMap.get(storeDeliveryId).get(0).getTimeBucketId();
					StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDeliveryId);
					storeDeliveryPositionDTO = buildStoreDeliveryPositionDTO(storeDelivery, storeDeliverySetting, OrderDeliveryModelEnum.NO_GPS_TEN_ORDER,
							MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, storeDelivery.getLongitude(), storeDelivery.getLatitude(), userId, timeBucketId);
				}
			}

			// 10天内无外卖订单，但是下过外卖订单
			if (!exitStoreDelivery) {
				List<MerchantOrderDeliveryLast> merchantOrderDeliveryLastOutTimes = this.merchantOrderDeliveryLastService.getMerchantOrderDeliveryLastByOutTime(merchantId, storeId, userId,
						tenDaysBeforeTime, true);
				if (merchantOrderDeliveryLastOutTimes != null && !merchantOrderDeliveryLastOutTimes.isEmpty()) {
					List<Long> storeIds = new ArrayList<Long>();
					for (MerchantOrderDeliveryLast merchantOrderDeliveryLast : merchantOrderDeliveryLastOutTimes) {
						if (merchantOrderDeliveryLast.getUserAddressLongitude() != 0.0 || merchantOrderDeliveryLast.getUserAddressLatitude() != 0.0) {
							storeIds.add(merchantOrderDeliveryLast.getStoreId());
						}
					}

					long storeDeliveryId = this.getStoreDeliveryId(merchantId, storeIds, timeBucketMap);
					if (storeDeliveryId > 0) {// 距离当前时间最近的订单上边的外送地址
						for (MerchantOrderDeliveryLast merchantOrderDeliveryLast : merchantOrderDeliveryLastOutTimes) {
							if (storeDeliveryId == merchantOrderDeliveryLast.getStoreId()) {
								if (merchantOrderDeliveryLast.getUserAddressLongitude() != 0.0 && merchantOrderDeliveryLast.getUserAddressLatitude() != 0.0) {
									exitStoreDelivery = true;
									storeDeliveryPositionDTO = buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.NO_GPS_NO_TEN_ORDER, MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE,
											merchantOrderDeliveryLast.getUserAddressLongitude(), merchantOrderDeliveryLast.getUserAddressLatitude(), userId, 0);
									break;
								}
							}
						}
					}
				}
			}

			// 该商户第一家支持外送的店铺
			if (!exitStoreDelivery) {
				List<Long> storeIds = new ArrayList<Long>();
				for (StoreDTO store : stores) {
					if (store.getLongitude() != 0.0 || store.getLatitude() != 0.0) {
						storeIds.add(store.getStoreId());
					}
				}

				long storeDeliveryId = this.getStoreDeliveryId(merchantId, storeIds, timeBucketMap);
				if (storeDeliveryId > 0) {
					exitStoreDelivery = true;
					StoreDTO storeDelivery = storeMaps.get(storeDeliveryId);
					long timeBucketId = timeBucketMap.get(storeDeliveryId).get(0).getTimeBucketId();
					StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDeliveryId);
					storeDeliveryPositionDTO = buildStoreDeliveryPositionDTO(storeDelivery, storeDeliverySetting, OrderDeliveryModelEnum.NO_GPS_NO_ORDER,
							MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, storeDelivery.getLongitude(), storeDelivery.getLatitude(), userId, timeBucketId);
				}
			}

			// 该商户无支持外送的店铺
			if (!exitStoreDelivery) {
				storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.NO_GPS_NO_STORE, MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, 0.0, 0.0, userId, 0);
			}
		} else {
			Map<Long, Double> storeIdsMap = new HashMap<Long, Double>();
			List<Long> storeIds = new ArrayList<Long>();
			for (StoreDTO store : stores) {
				StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(store.getStoreId());
				if (storeDeliverySetting != null && storeDeliverySetting.isDeliverySupported()) {
					double distance = GeoTool.getPointDistance(store.getLongitude(), store.getLatitude(), param.getUserLongitude(), param.getUserLatitude());// 用户当前位置和店铺的距离
					if (distance <= storeDeliverySetting.getDeliveryScope()) {// 用户处在该店铺的外送范围内
						storeIdsMap.put(store.getStoreId(), distance);
						storeIds.add(store.getStoreId());
					}
				}
			}

			if (storeIdsMap.size() > 0) {
				List<Map.Entry<Long, Double>> list = new ArrayList<Map.Entry<Long, Double>>(storeIdsMap.entrySet());
				// 对距离店铺的做升序排序
				Collections.sort(list, new Comparator<Map.Entry<Long, Double>>() {
					@Override
					public int compare(Entry<Long, Double> o1, Entry<Long, Double> o2) {
						return o1.getValue().compareTo(o2.getValue());
					}
				});
				Map<Long, List<StoreTimeBucket>> timeBucketMap = this.getStoreTimeBucketByMerchantId(merchantId, storeIds);

				boolean exitStoreDelivery = false;
				long storeDeliveryId = 0;
				for (Entry<Long, Double> entry : list) {
					List<StoreTimeBucket> timeBuckets = timeBucketMap.get(entry.getKey());
					if (timeBuckets != null && !timeBuckets.isEmpty()) {
						exitStoreDelivery = true;
						storeDeliveryId = entry.getKey();
						break;
					}
				}
				if (exitStoreDelivery) {
					// 用户GSP信息,用户附近有店铺
					StoreDTO storeDelivery = storeMaps.get(storeDeliveryId);
					long timeBucketId = timeBucketMap.get(storeDeliveryId).get(0).getTimeBucketId();
					StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDeliveryId);
					storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(storeDelivery, storeDeliverySetting, OrderDeliveryModelEnum.GPS_STORE, MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE,
							storeDelivery.getLongitude(), storeDelivery.getLatitude(), userId, timeBucketId);
				} else {
					// 有用户GSP信息，用户附近无店铺
					storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.GPS_NO_STORE, MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE,
							param.getUserLongitude(), param.getUserLatitude(), userId, 0);
				}
			} else {
				// 有用户GSP信息，用户附近无店铺
				storeDeliveryPositionDTO = this.buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.GPS_NO_STORE, MerchantSingleMulitiEnum.MERCHANT_MULITI_STORE, param.getUserLongitude(),
						param.getUserLatitude(), userId, 0);
			}
		}
		return storeDeliveryPositionDTO;
	}

	/**
	 * 获取单店铺形式的外送店铺
	 * 
	 * @param param
	 * @return
	 * @throws TMerchantException
	 * @throws TException
	 */
	private StoreDeliveryPositionDTO getSingleDeliveryStoreDTO(StoreDeliveryPositionParam param, List<StoreDTO> storeDTOs, Map<Long, StoreDTO> storeMaps, MerchantSettingDTO merchantSetting)
			throws TMerchantException, TException {
		int merchantId = param.getMerchantId();
		long storeId = param.getStoreId();
		long userId = param.getUserId();
		MerchantDeliveryModeEnum deliveryMode = MerchantDeliveryModeEnum.findByValue(merchantSetting.getDeliveryMode().getValue());

		StoreDTO storeDTO = null;
		if (storeId > 0) {
			storeDTO = storeMaps.get(storeId);
		}
		if (storeDTOs.size() == 1) {
			storeDTO = storeDTOs.get(0);
		}
		StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting(merchantId, storeDTO.getStoreId());
		if (storeDeliverySetting == null || !storeDeliverySetting.isDeliverySupported()) {
			return this.buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.MERCHANT_SINGLE_STORE_NO_DELIVERY, MerchantSingleMulitiEnum.MERCHANT_SINGLE_STORE, 0.0, 0.0, userId, 0);
		}

		List<Long> storeIds = new ArrayList<Long>();
		storeIds.add(storeDTO.getStoreId());
		Map<Long, List<StoreTimeBucket>> timeBucketMap = this.getStoreTimeBucketByMerchantId(merchantId, storeIds);
		long storeDeliveryId = this.getStoreDeliveryId(merchantId, storeIds, timeBucketMap);

		if (storeDeliveryId == 0 || (deliveryMode == MerchantDeliveryModeEnum.DISTINCE && storeDTO.getLongitude() == 0.0 && storeDTO.getLatitude() == 0.0)) {// 外送模式下，店铺的GPS信息不允许为空
			return this.buildStoreDeliveryPositionDTO(null, null, OrderDeliveryModelEnum.MERCHANT_SINGLE_STORE_NO_DELIVERY, MerchantSingleMulitiEnum.MERCHANT_SINGLE_STORE, 0.0, 0.0, userId, 0);
		} else {
			long timeBucketId = timeBucketMap.get(storeDeliveryId).get(0).getTimeBucketId();
			return this.buildStoreDeliveryPositionDTO(storeDTO, storeDeliverySetting, OrderDeliveryModelEnum.MERCHANT_SINGLE_STORE_DELIVERY, MerchantSingleMulitiEnum.MERCHANT_SINGLE_STORE,
					storeDTO.getLongitude(), storeDTO.getLatitude(), userId, timeBucketId);
		}
	}

	/**
	 * 获取Map结构的店铺
	 * 
	 * @param storeDTOs
	 * @return
	 */
	private Map<Long, StoreDTO> getStoreDTOMap(List<StoreDTO> storeDTOs) {
		Map<Long, StoreDTO> storeDTOMap = new HashMap<Long, StoreDTO>();
		if (storeDTOs == null || storeDTOs.isEmpty()) {
			return storeDTOMap;
		}
		for (StoreDTO storeDTO : storeDTOs) {
			storeDTOMap.put(storeDTO.getStoreId(), storeDTO);
		}
		return storeDTOMap;
	}

	/**
	 * 创建 StoreDeliveryPositionDTO
	 * 
	 * @param storeDTO
	 * @param storeDeliverySetting
	 * @param orderDeliveryModelEnum
	 * @param merchantSingleMulitiEnum
	 * @param longitude
	 * @param latitud
	 * @param userId
	 * @return
	 */
	private StoreDeliveryPositionDTO buildStoreDeliveryPositionDTO(StoreDTO storeDTO, StoreDeliverySetting storeDeliverySetting, OrderDeliveryModelEnum orderDeliveryModelEnum,
			MerchantSingleMulitiEnum merchantSingleMulitiEnum, double longitude, double latitud, long userId, long timeBucketId) {
		StoreDeliveryPositionDTO storeDeliveryPositionDTO = new StoreDeliveryPositionDTO();
		if (storeDTO != null) {
			storeDeliveryPositionDTO.setMerchantId(storeDTO.getMerchantId());
			storeDeliveryPositionDTO.setStoreId(storeDTO.getStoreId());
		}
		storeDeliveryPositionDTO.setUserId(userId);
		if (storeDeliverySetting != null) {
			storeDeliveryPositionDTO.setMinOrderDeliveryAmount(storeDeliverySetting.getMinOrderDeliveryAmount());
			storeDeliveryPositionDTO.setDeliveryFee(storeDeliverySetting.getDeliveryFee());
		}
		storeDeliveryPositionDTO.setOrderDeliveryModelEnum(orderDeliveryModelEnum);
		storeDeliveryPositionDTO.setTimeBucketId(timeBucketId);
		// 1.GPS_STORE 3.NO_GPS_TEN_ORDER
		if (orderDeliveryModelEnum == OrderDeliveryModelEnum.GPS_STORE || orderDeliveryModelEnum == OrderDeliveryModelEnum.NO_GPS_TEN_ORDER) {
			GpsDTO storeGps = new GpsDTO(longitude, latitud);
			storeDeliveryPositionDTO.setStoreGps(storeGps);
		}
		// 2.GPS_NO_STORE
		// 4.NO_GPS_NO_TEN_ORDER
		if (orderDeliveryModelEnum == OrderDeliveryModelEnum.NO_GPS_NO_TEN_ORDER) {
			GpsDTO takeWayAddressGps = new GpsDTO(longitude, latitud);
			storeDeliveryPositionDTO.setFirstStoreGps(takeWayAddressGps);
		}
		// 5.NO_GPS_NO_ORDER
		if (orderDeliveryModelEnum == OrderDeliveryModelEnum.NO_GPS_NO_ORDER) {
			GpsDTO firstStoreGps = new GpsDTO(longitude, latitud);
			storeDeliveryPositionDTO.setFirstStoreGps(firstStoreGps);
		}
		// 6.NO_GPS_NO_STORE
		// 7.NO_DISTANCE_STORE
		if (orderDeliveryModelEnum == OrderDeliveryModelEnum.NO_DISTANCE_STORE) {
			GpsDTO storeGps = new GpsDTO(longitude, latitud);
			storeDeliveryPositionDTO.setStoreGps(storeGps);
		}
		// 8.NO_DISTANCE_NO_TODAY_STORE
		// 9.NO_DISTANCE_NO_STORE
		// 10.MERCHANT_SINGLE_STORE_NO_DELIVERY
		// 11.MERCHANT_SINGLE_STORE_DELIVERY
		if (orderDeliveryModelEnum == OrderDeliveryModelEnum.MERCHANT_SINGLE_STORE_DELIVERY) {
			GpsDTO storeGps = new GpsDTO(longitude, latitud);
			storeDeliveryPositionDTO.setStoreGps(storeGps);
		}
		storeDeliveryPositionDTO.setMerchantSingleMulitiEnum(merchantSingleMulitiEnum);
		return storeDeliveryPositionDTO;
	}

	/**
	 * 创建外送店铺列表
	 * 
	 * @param storeDTOs
	 * @param storeDeliverySettings
	 * @return
	 */
	private List<StoreDeliveryDTO> buildStoreDeliveryDTOs(List<StoreDTO> storeDTOs, Map<Long, StoreDeliverySetting> storeDeliverySettings) {
		List<StoreDeliveryDTO> storeDeliveryDTOs = new ArrayList<StoreDeliveryDTO>();
		for (StoreDTO storeDTO : storeDTOs) {
			StoreDeliveryDTO storeDeliveryDTO = new StoreDeliveryDTO();
			StoreDeliverySetting storeDeliverySetting = storeDeliverySettings.get(storeDTO.getStoreId());
			BeanUtil.copy(storeDTO, storeDeliveryDTO);
			storeDeliveryDTO.setDeliveryScope(storeDeliverySetting.getDeliveryScope());
			storeDeliveryDTO.setMinOrderDeliveryAmount(storeDeliverySetting.getMinOrderDeliveryAmount());
			storeDeliveryDTO.setDeliveryFee(storeDeliverySetting.getDeliveryFee());
			storeDeliveryDTOs.add(storeDeliveryDTO);
		}
		return storeDeliveryDTOs;
	}

	/**
	 * 获取外送营业时段，除去删除、不支持外送和不在外送时段的送餐结束之间内的营业时段
	 * 
	 * @param merchantId
	 * @param storeId
	 * @return
	 */
	private List<StoreTimeBucket> getStoreBucketTimeForDelivery(int merchantId, long storeId) {
		long currentTime = System.currentTimeMillis();
		long selectedDate = DateUtil.getBeginTime(currentTime, null);
		long time = currentTime - DateUtil.getBeginTime(currentTime, null);

		List<StoreTimeBucket> storeTimeBuckets = this.storeTimeBucketService.getStoreTimeBucketsInStoreForTime(merchantId, storeId, selectedDate);
		for (Iterator<StoreTimeBucket> iterator = storeTimeBuckets.iterator(); iterator.hasNext();) {
			StoreTimeBucket storeTimeBucket = iterator.next();
			if (storeTimeBucket.isDeleted() || !storeTimeBucket.isDeliverySupported() || time > storeTimeBucket.getDeliveryEndTimeForBiz()) {
				iterator.remove();
			}
		}
		StoreTimeBucketUtil.soreTimeBucketsByDeliveryEndTime(storeTimeBuckets);
		return storeTimeBuckets;
	}

	/**
	 * 获取该商户下店铺的外送的营业时段
	 * 
	 * @param merchantId
	 * @param storeIds
	 * @return
	 */
	private Map<Long, List<StoreTimeBucket>> getStoreTimeBucketByMerchantId(int merchantId, List<Long> storeIds) {
		Map<Long, List<StoreTimeBucket>> map = new HashMap<Long, List<StoreTimeBucket>>();
		if (storeIds == null || storeIds.isEmpty()) {
			return map;
		}
		for (Long storeId : storeIds) {
			List<StoreTimeBucket> storeBucketTimeForDeliverys = this.getStoreBucketTimeForDelivery(merchantId, storeId);
			if (storeBucketTimeForDeliverys != null && !storeBucketTimeForDeliverys.isEmpty()) {
				map.put(storeId, this.getStoreBucketTimeForDelivery(merchantId, storeId));
			}
		}
		return map;
	}
	
	//兼容单店铺
	@Override
	public void updateStoreDeliveryWaimaiEnableByWaimaiType(int merchantId, long storeId, int waimaiType, boolean enabled) throws T5weiException, TException {
		storeDeliverySettingService.updateStoreDeliveryWaimaiEnableByWaimaiType(merchantId, storeId, waimaiType, enabled, false);
	}
}
