package com.huofu.module.i5wei.order.facade;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderDeliveryDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.service.PlaceOrderWaimaiParam;
import com.huofu.module.i5wei.order.service.StoreOrderHelper;
import com.huofu.module.i5wei.order.service.StoreOrderWaimaiService;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import huofucore.facade.config.currency.CurrencyEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@ThriftServlet(name = "storeOrderWaimaiFacadeServlet", serviceClass = StoreOrderWaimaiFacade.class)
@Component
public class StoreOrderWaimaiFacadeImpl implements StoreOrderWaimaiFacade.Iface {

	@Autowired
	private StoreOrderWaimaiService storeOrderWaimaiService;

	@Autowired
	private StoreOrderWaimaiFacadeValidate storeOrderWaimaiFacadeValidate;

	@Autowired
	private StoreTimeBucketService storeTimeBucketService;

	@Autowired
	private I5weiMessageProducer i5weiMessageProducer;

	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@Autowired
	private StoreOrderDeliveryDAO storeOrderDeliveryDAO;

	@Autowired
	private StoreOrderDAO storeOrderDAO;


	@Override
	public StoreOrderDTO placeStoreOrderDelivery(StoreOrderWaimaiPlaceParam storeOrderWaimaiPlaceParam) throws T5weiException, TException {
		//参数校验
		storeOrderWaimaiFacadeValidate.placeStoreOrderValidate(storeOrderWaimaiPlaceParam);
		//获取可用的营业时段
		int merchantId = storeOrderWaimaiPlaceParam.getMerchantId();
		long storeId = storeOrderWaimaiPlaceParam.getStoreId();
		long deliveryTime = storeOrderWaimaiPlaceParam.getDeliveryTime();
		if (deliveryTime <= 0) {
			deliveryTime = System.currentTimeMillis();
		}
		long repastDate = DateUtil.getBeginTime(deliveryTime, null);
		StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket4Waimai(merchantId, storeId, repastDate);
		//外卖平台默认人民币
		int currencyId = CurrencyEnum.RMB.getValue();
		//下单
		PlaceOrderWaimaiParam placeOrderWaimaiParam = new PlaceOrderWaimaiParam();
		placeOrderWaimaiParam.setStoreOrderWaimaiPlaceParam(storeOrderWaimaiPlaceParam);
		placeOrderWaimaiParam.setCurrencyId(currencyId);
		placeOrderWaimaiParam.setRepastDate(repastDate);
		placeOrderWaimaiParam.setStoreTimeBucket(storeTimeBucket);
		StoreOrder placeOrder = storeOrderWaimaiService.placeStoreOrderDelivery(placeOrderWaimaiParam);
		StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(placeOrder);
		return storeOrderDTO;
	}

	@Override
	public StoreOrderDeliveryDTO updateStoreOrderDelivery(StoreOrderWaimaiUpdateParam storeOrderWaimaiUpdateParam)
			throws T5weiException, TException {
		StoreOrderDelivery storeOrderDelivery = storeOrderWaimaiService.updateStoreOrderDelivery(storeOrderWaimaiUpdateParam);
		return BeanUtil.copy(storeOrderDelivery, StoreOrderDeliveryDTO.class);
	}

	@Override
	public StoreOrderDTO getStoreOrderByMeituanOrderId(int merchantId, long storeId, String meituanOrderId)
			throws T5weiException, TException {
		//查询外卖订单
		StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryDAO.getByWaimaiOrderId(merchantId, storeId, meituanOrderId, 1, false);
		if (storeOrderDelivery == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_PARAM_ERROR.getValue(),
			                         "get storeOrderDelivery by meituanOrderId is null");
		}
		//查询订单
		StoreOrder storeOrder = storeOrderDAO.getById(merchantId, storeId, storeOrderDelivery.getOrderId(), true, true);
		if (storeOrder == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_NOT_EXIST.getValue(), "get storeOrder by orderId is null");
		}
		StoreOrderDTO storeOrderDTO = new StoreOrderDTO();
		BeanUtil.copy(storeOrder, storeOrderDTO);
		return storeOrderDTO;
	}

	@Override
	public String getMeituanOrderIdByOrderId(int merchantId, long storeId, String businOrderId)
			throws T5weiException, TException {
		//查询外卖订单
		StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryDAO.getById(merchantId, storeId, businOrderId, false);
		if (storeOrderDelivery == null) {
			throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_PARAM_ERROR.getValue(), "get storeOrderDelivery by orderId is null");
		}
		return storeOrderDelivery.getWaimaiOrderId();
	}
}
