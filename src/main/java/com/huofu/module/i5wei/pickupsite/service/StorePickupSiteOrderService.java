package com.huofu.module.i5wei.pickupsite.service;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.service.StoreOrderDeliveryService;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.pickupsite.dao.StorePickupSiteDAO;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSite;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import com.huofu.module.i5wei.pickupsite.entity.UserDefaultPickupSite;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.account.accounted.PayMethodEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.pickupsite.AllocateStorePickupSiteParam;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteBaseDTO;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteDTO;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteTimeSettingDTO;
import huofucore.facade.user.info.UserDTO;
import huofucore.facade.user.info.UserFacade;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.thrift.TException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by taoming on 2016/12/8.
 */
@Service
public class StorePickupSiteOrderService {

	@Autowired
	private StorePickupSiteTimeSettingService storePickupSiteTimeSettingService;

	@Autowired
	private UserDefaultPickupSiteService userDefaultPickupSiteService;

	@Autowired
	private StorePickupSiteHelpler storePickupSiteHelpler;

	@Autowired
	private StoreOrderDeliveryService storeOrderDeliveryService;

	@Autowired
	private StorePickupSiteService storePickupSiteService;

	@Autowired
	private StoreOrderService storeOrderService;

	@Autowired
	private UserPickupSiteService userPickupSiteService;

	@Autowired
	private Store5weiSettingService store5weiSettingService;

	@ThriftClient
	private UserFacade.Iface userFacade;

	@Autowired
	private StorePickupSiteDAO storePickupSiteDAO;

	/**
	 * 用户选择自提点
	 *
	 * @param allocateStorePickupSiteParam
	 * @throws T5weiException
	 */
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public void userSelectStorePickupSite(AllocateStorePickupSiteParam allocateStorePickupSiteParam) throws TException {

		long userId = allocateStorePickupSiteParam.getUserId();
		long storeId = allocateStorePickupSiteParam.getStoreId();
		int merchantId = allocateStorePickupSiteParam.getMerchantId();
		boolean defaultMark = allocateStorePickupSiteParam.isDefaultMark();
		long timeBucketId = allocateStorePickupSiteParam.getTimeBucketId();
		long storePickupSiteId = allocateStorePickupSiteParam.getStorePickupSiteId();
		String orderId = allocateStorePickupSiteParam.getOrderId();

		if (storePickupSiteId > 0 && defaultMark) {//设置默认自提点
			UserDefaultPickupSite userDefaultPickupSite = new UserDefaultPickupSite();
			userDefaultPickupSite.setPickupSiteId(storePickupSiteId);
			userDefaultPickupSite.setTimeBucketId(timeBucketId);
			userDefaultPickupSite.setUserId(userId);
			userDefaultPickupSite.setCreateTime(System.currentTimeMillis());
			userDefaultPickupSiteService.setUserTimeBucketDefaultPickupSite(userDefaultPickupSite);
		}

		if (allocateStorePickupSiteParam.isSetOrderId() && orderId != null) {//设置订单自提点信息

			StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
			if (storePickupSiteId > 0) { //修改订单自提点
				Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
				if (store5weiSetting == null || !store5weiSetting.isEnablePickupSite()) {
					throw new T5weiException(T5weiErrorCodeType.STORE_PICKUP_SITE_NOT_BE_OPENED.getValue(),
					                         "store pickup function not be opened");
				}
				StorePickupSiteDTO storePickupSite = storePickupSiteService.getStorePickupSiteInfo(merchantId, storeId, storePickupSiteId);

				//更换自提点
				StorePickupSiteTimeSetting storePickupSiteTimeSetting =
						storePickupSiteTimeSettingService
								.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(merchantId, storeId, storePickupSiteId, timeBucketId);
				boolean isAvaliable = storePickupSiteHelpler.checkRepastDate(storeOrder.getRepastDate());
				boolean cutoffTimeExpiredMark = storePickupSiteHelpler.checkCutoffTimeOfPickupSite(storePickupSiteTimeSetting, isAvaliable);
				if (!cutoffTimeExpiredMark) {
					throw new T5weiException(T5weiErrorCodeType.STORE_PICKUP_SITE_NOT_IN_SERVICE.getValue(),
					                         "store pickup site not in service");
				}
				StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryService.getStoreOrderDeliveryById(merchantId, storeId, orderId);
				if (storeOrderDelivery == null) {
					//并且更新订单类型
					storeOrder.snapshot();
					storeOrder.setTakeMode(StoreOrderTakeModeEnum.SEND_OUT.getValue());
					storeOrder.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
					storeOrder.update();
					UserDTO userDTO =userFacade.getUserByUserId(storeOrder.getUserId());
					//创建order_delivery,
					storeOrderDelivery = new StoreOrderDelivery();
					storeOrderDelivery.setUserAddress(storePickupSite.getStorePickupSiteAddress());
					storeOrderDelivery.setStorePickupSiteId(storePickupSite.getStorePickupSiteId());
					storeOrderDelivery.setStorePickupName(storePickupSite.getStorePickupSiteName());
					storeOrderDelivery.setOrderId(orderId);
					storeOrderDelivery.setDeliveryAssignTime(DateUtil.getBeginTime(storeOrder.getRepastDate(), null) + storePickupSiteTimeSetting.getPickupTime());
					storeOrderDelivery.setMerchantId(merchantId);
					storeOrderDelivery.setStoreId(storeId);
                    storeOrderDelivery.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
					storeOrderDelivery.setStoreShipping(true);
					storeOrderDelivery.setUserAddress(storePickupSite.getStorePickupSiteAddress());
					storeOrderDelivery.setWaimaiPayType(PayMethodEnum.RMB_WECHAT.getValue());
					storeOrderDelivery.setContactName(userDTO.getName());
					storeOrderDelivery.setContactPhone(userDTO.getMobile());
					storeOrderDelivery.create();
				} else {
					storeOrderDelivery.setUserAddress(storePickupSite.getStorePickupSiteAddress());
					storeOrderDelivery.setStorePickupSiteId(storePickupSite.getStorePickupSiteId());
					storeOrderDelivery.setStorePickupName(storePickupSite.getStorePickupSiteName());
					storeOrderDelivery.update();
				}
			} else { //不使用自提点
				//不使用自提点(将order表设置为打包模式，同时删除order_delivery)
				storeOrderDeliveryService.changeTakeModel(merchantId, storeId, orderId);
			}
		}
	}

	/**
	 * 打包订单设置默认自提点
	 *
	 * @param merchantId
	 * @param storeId
	 * @param userId
	 * @param timeBucketId
	 * @param placeOrder
	 * @return
	 */
	public StorePickupSiteBaseDTO setOrderPickupSite(int merchantId, long storeId, long userId, long timeBucketId, StoreOrder placeOrder)
			throws T5weiException {
		Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
		if (store5weiSetting == null || !store5weiSetting.isEnablePickupSite()) {
			return null;
		}
		//设置自提点
		long storePickupSiteId = userPickupSiteService.getUserTimeBucketDefaultPickupSite(userId, timeBucketId);
		if (storePickupSiteId != 0) {
			StorePickupSite storePickupSite = userPickupSiteService.getStorePickupSiteById(merchantId, storeId, storePickupSiteId);
			if (storePickupSite != null && storePickupSite.isDisabled() == false) {
				StorePickupSiteTimeSetting storePickupSiteTimeSetting =
						storePickupSiteTimeSettingService
								.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(merchantId, storeId, storePickupSiteId, timeBucketId);
				boolean isTodayToRepast = storePickupSiteHelpler.checkRepastDate(placeOrder.getRepastDate());
				boolean cutoffTimeNotExpiredMark = storePickupSiteHelpler.checkCutoffTimeOfPickupSite(storePickupSiteTimeSetting, isTodayToRepast);
				StorePickupSiteBaseDTO storePickupSiteBaseDTO = storePickupSiteHelpler.getStorePickupSiteDTOByEntity(storePickupSite);
				if(storePickupSiteTimeSetting != null) {
					StorePickupSiteTimeSettingDTO storePickupSiteTimeSettingDTO = BeanUtil.copy(storePickupSiteTimeSetting, StorePickupSiteTimeSettingDTO.class);
					storePickupSiteBaseDTO.setStorePickupSiteTimeSetting(storePickupSiteTimeSettingDTO);
				}
				if (cutoffTimeNotExpiredMark) {
					return storePickupSiteBaseDTO;
				} else {
					return storePickupSiteBaseDTO.setExpired(true);
				}
			}
		}
		return null;
	}

	public StorePickupSiteBaseDTO getPickupSiteBaseDTOByOrderId(int merchantId, long storeId, long pickupSiteId, long timeBucketId) {
		StorePickupSite storePickupSite = this.storePickupSiteDAO.getStorePickupSiteById(merchantId, storeId, pickupSiteId);
		StorePickupSiteTimeSetting storePickupSiteTimeSetting = this.storePickupSiteTimeSettingService.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(merchantId, storeId, pickupSiteId, timeBucketId);
		StorePickupSiteBaseDTO storePickupSiteBaseDTO = new StorePickupSiteBaseDTO();
		StorePickupSiteTimeSettingDTO storePickupSiteTimeSettingDTO;
		if(storePickupSite != null && storePickupSiteTimeSetting != null) {
			storePickupSiteTimeSettingDTO = new StorePickupSiteTimeSettingDTO();
			BeanUtils.copyProperties(storePickupSite, storePickupSiteBaseDTO);
			BeanUtils.copyProperties(storePickupSiteTimeSetting, storePickupSiteTimeSettingDTO);
			storePickupSiteBaseDTO.setStorePickupSiteTimeSetting(storePickupSiteTimeSettingDTO);
		}
		return storePickupSiteBaseDTO;
	}
}
