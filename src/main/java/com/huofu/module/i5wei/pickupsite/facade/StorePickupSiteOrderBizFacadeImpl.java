package com.huofu.module.i5wei.pickupsite.facade;

import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderService;
import com.huofu.module.i5wei.pickupsite.service.*;
import huofucore.facade.dialog.resourcevisit.UserResourceVisitFacade;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.pickupsite.*;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by taoming on 2016/12/6.
 */
@SuppressWarnings("all")
@ThriftServlet(name = "storePickupSiteOrderBizFacadeServlet", serviceClass = StorePickupSiteOrderBizFacade.class)
@Component
public class StorePickupSiteOrderBizFacadeImpl implements StorePickupSiteOrderBizFacade.Iface {

	@Autowired
	private StoreOrderService storeOrderService;

	@Autowired
	private UserPickupSiteService userPickupSiteService;

	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@Autowired
	private StorePickupSiteHelpler storePickupSiteHelpler;

	@Autowired
	private StorePickupSiteService storePickupSiteService;

	@Autowired
	private StorePickupSiteTimeSettingService storePickupSiteTimeSettingService;

	@Autowired
	private UserDefaultPickupSiteService userDefaultPickupSiteService;

	@ThriftClient
	private UserResourceVisitFacade.Iface userResourceVisitFacade;

	@Autowired
	private StorePickupSiteFacadeValidator storePickupSiteFacadeValidator;

	@Autowired
	private StorePickupSiteOrderService storePickupSiteOrderService;


	/**
	 * 获取店铺时段下的自提点列表
	 *
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 */
	@Override
	public StorePickupSiteForSelectDTO getStorePickupSiteBaseInfosByBucketId(int merchantId, long storeId, long timeBucketId, long userId, String orderId)
			throws T5weiException, TException {
		storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
		//获取有效与无效自提点分组列表
		StorePickupSiteForSelectDTO storePickupSiteForSelectDTO =
				storePickupSiteHelpler.getStorePickupSiteForSelectDTO(merchantId, storeId, timeBucketId, userId, orderId);
		return storePickupSiteHelpler.wrapperStorePickupSiteForSelectDTO(userId, timeBucketId, storePickupSiteForSelectDTO);
	}

	/**
	 * 用户取消默认自提点
	 *
	 * @param storePickupSiteConcelDefaultParam
	 */
	@Override
	public void concelDefaultPickupSite(long userId, long storePickupSiteId, long timeBucketId) throws T5weiException, TException {
		this.userDefaultPickupSiteService.concelUserTimeBucketDefaultPickupSite(userId, storePickupSiteId, timeBucketId);
	}


	/**
	 * 检测是否需要弹窗
	 *
	 * @param merchantId
	 * @param storeId
	 * @param timeBucketId
	 * @param userId
	 */
	@Override
	public StorePickupSiteAlertCheckDTO pickupSiteAlertCheck(int merchantId, long storeId, long timeBucketId, long userId, String orderId)
			throws T5weiException, TException {
		storePickupSiteFacadeValidator.checkMerchantIdAndStoreIdParam(merchantId, storeId);
		//获取有效与无效自提点分组列表
		StorePickupSiteForSelectDTO storePickupSiteForSelectDTO =
				storePickupSiteHelpler.getStorePickupSiteForSelectDTO(merchantId, storeId, timeBucketId, userId, orderId);
		if(storePickupSiteForSelectDTO != null) {
			storePickupSiteHelpler.wrapperStorePickupSiteForSelectDTO(userId, timeBucketId, storePickupSiteForSelectDTO);
			return storePickupSiteHelpler.getStorePickupSiteAlertCheckDTO(userId, timeBucketId, storePickupSiteForSelectDTO);
		}
		return new StorePickupSiteAlertCheckDTO();
	}


	/**
	 * 选择自提单点
	 *
	 * @param allocateStorePickupSiteParam
	 */
	@Override
	public boolean allocatePickupSite(AllocateStorePickupSiteParam allocateStorePickupSiteParam) throws T5weiException, TException {
		storePickupSiteOrderService.userSelectStorePickupSite(allocateStorePickupSiteParam);
		return true;
	}

	/**
	 * 根据订单ID获取自提点基本信息
	 * @param merchantId
	 * @param storeIds
	 * @param orderId
	 * @param timeBucketId
	 * @return
	 * @throws TException
	 */
	@Override
	public StorePickupSiteBaseDTO getPickupSiteBaseDTOByOrderId(int merchantId, long storeId, String orderId, long timeBucketId) throws T5weiException, TException {
		StoreOrderDelivery storeOrderDelivery = this.storeOrderService.getStoreOrderDelivery(merchantId, storeId, orderId);
		if (storeOrderDelivery == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_PICKUP_SITE_NOT_EXIST.getValue(), "pickup site not exist");
		}
		return this.storePickupSiteOrderService.getPickupSiteBaseDTOByOrderId(merchantId, storeId, storeOrderDelivery.getStorePickupSiteId(), timeBucketId);
	}
}
