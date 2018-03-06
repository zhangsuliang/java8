package com.huofu.module.i5wei.order.service;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderInvoiceStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderOptlogTypeEnum;
import huofucore.facade.i5wei.order.StoreOrderPayStatusEnum;
import huofucore.facade.i5wei.order.StoreOrderPlaceItemParam;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.order.StoreOrderWaimaiPlaceParam;
import huofucore.facade.i5wei.order.StoreOrderWaimaiUpdateParam;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderDeliveryDAO;
import com.huofu.module.i5wei.order.dao.StoreOrderOptlogDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StoreOrderWaimaiService {

	@Autowired
	private StoreOrderDAO storeOrderDAO;

	@Autowired
	private StoreOrderDeliveryDAO storeOrderDeliveryDAO;

	@Autowired
	private StoreChargeItemService storeChargeItemService;

	@Autowired
	private StoreTimeBucketService storeTimeBucketService;

	@Autowired
	private StoreOrderService storeOrderService;

	@Autowired
	private StoreOrderOptlogDAO storeOrderOptlogDAO;

	@Autowired
	private StoreOrderHelper storeOrderHelper;

	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreOrder placeStoreOrderDelivery(PlaceOrderWaimaiParam placeOrderWaimaiParam) throws T5weiException {
		boolean enableSlave = false;
		//参数
		long repastDate = placeOrderWaimaiParam.getRepastDate();
		StoreTimeBucket storeTimeBucket = placeOrderWaimaiParam.getStoreTimeBucket();
		long timeBucketId = storeTimeBucket.getTimeBucketId();
		int currencyId = placeOrderWaimaiParam.getCurrencyId();
		StoreOrderWaimaiPlaceParam storeOrderWaimaiPlaceParam = placeOrderWaimaiParam.getStoreOrderWaimaiPlaceParam();
		int merchantId = storeOrderWaimaiPlaceParam.getMerchantId();
		long storeId = storeOrderWaimaiPlaceParam.getStoreId();
		long staffId = storeOrderWaimaiPlaceParam.getStaffId();
		String waimaiOrderId = storeOrderWaimaiPlaceParam.getWaimaiOrderId();
		int waimaiType = storeOrderWaimaiPlaceParam.getWaimaiType();
		int clientType = storeOrderWaimaiPlaceParam.getClientType();
		long currentTime = System.currentTimeMillis();
		List<StoreOrderPlaceItemParam> orderPlaceItems = storeOrderWaimaiPlaceParam.getChargeItems();
		//检查外卖订单
		StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryDAO.getByWaimaiOrderId(merchantId, storeId, waimaiOrderId, waimaiType, enableSlave);
		String inputOrderId = null;
		if (storeOrderDelivery == null){
			storeOrderDelivery = new StoreOrderDelivery();
		}else{
			inputOrderId = storeOrderDelivery.getOrderId();
		}
		//获取订单
		StoreOrder storeOrder;
        if (inputOrderId == null || inputOrderId.isEmpty()) {
            storeOrder = new StoreOrder();
        } else {
            storeOrder = storeOrderDAO.getById(merchantId, storeId, inputOrderId, true, true);
            if (storeOrder == null) {
                storeOrder = new StoreOrder();
            } else {
                String orderId = storeOrder.getOrderId();
                if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_FINISH.getValue(), DataUtil.infoWithParams("store order pay finish, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
                }
                if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAYING.getValue(), DataUtil.infoWithParams("store order paying, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
                }
                if (storeOrder.getTakeSerialNumber() > 0) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TRADE_TAKE_CODE.getValue(), DataUtil.infoWithParams("store order has take code, storeId=#1, orderId=#2 ", new Object[]{storeId, orderId}));
                }
            }
        }
        //组装订单
        storeOrder.setMerchantId(merchantId);
        storeOrder.setStoreId(storeId);
        storeOrder.setRepastDate(repastDate);
        storeOrder.setTimeBucketId(timeBucketId);
        storeOrder.setClientType(clientType);
        if(storeOrderWaimaiPlaceParam.isHasInvoiced()){
        	storeOrder.setInvoiceStatus(StoreOrderInvoiceStatusEnum.NEED.getValue());
        }
        storeOrder.setInvoiceDemand(storeOrderWaimaiPlaceParam.getInvoiceTitle());
        // 组装价格，请参考订单小票打印数据：StoreOrderPriceHelper.getStoreOrderActualPayInfo
		long shippingFee = storeOrderWaimaiPlaceParam.getShippingFee();
		long originalPrice = storeOrderWaimaiPlaceParam.getOriginalPrice();
		long actualPrice = storeOrderWaimaiPlaceParam.getTotalPrice();// 顾客实际支付
		long packageFee = storeOrderWaimaiPlaceParam.getBoxPrice();
		long orderPrice = originalPrice - shippingFee;
		if (orderPrice < 0) {
			orderPrice = 0;
		}
		long favorablePrice = actualPrice - shippingFee;
		if (favorablePrice < 0) {
			favorablePrice = 0;
		}
        storeOrder.setPackageFee(packageFee);
		storeOrder.setOrderPrice(orderPrice);
		storeOrder.setTotalPrice(orderPrice);
		storeOrder.setFavorablePrice(favorablePrice);
        storeOrder.setDeliveryFee(shippingFee);
        storeOrder.setActualPrice(actualPrice);
        storeOrder.setTakeMode(StoreOrderTakeModeEnum.SEND_OUT.getValue());
        storeOrder.setWaimaiType(storeOrderWaimaiPlaceParam.getWaimaiType());
        //组装外卖订单
        storeOrderDelivery.setMerchantId(merchantId);
        storeOrderDelivery.setStoreId(storeId);
        storeOrderDelivery.setContactName(storeOrderWaimaiPlaceParam.getRecipientName());
        storeOrderDelivery.setContactPhone(storeOrderWaimaiPlaceParam.getRecipientPhone());;
        storeOrderDelivery.setUserAddress(storeOrderWaimaiPlaceParam.getRecipientAddress());
        storeOrderDelivery.setDeliveryAssignTime(storeOrderWaimaiPlaceParam.getDeliveryTime());
        storeOrderDelivery.setWaimaiDeliveryTime(storeOrderWaimaiPlaceParam.getDeliveryTime());
        storeOrderDelivery.setBoxPrice(storeOrderWaimaiPlaceParam.getBoxPrice());
        storeOrderDelivery.setMtMoneyCent(storeOrderWaimaiPlaceParam.getMtMoneyCent());
        storeOrderDelivery.setPoiMoneyCent(storeOrderWaimaiPlaceParam.getMtMoneyCent());
        storeOrderDelivery.setReceiveOrderType(storeOrderWaimaiPlaceParam.getReceiveOrderType());
        storeOrderDelivery.setDelayReceiveOrderMinute(storeOrderWaimaiPlaceParam.getDelayReceiveOrderMinute());
        storeOrderDelivery.setRemarks(storeOrderWaimaiPlaceParam.getRemarks());
        //美团传递的是否是第三方配送，外送表存的是是否商家自配送，恰好相反
        storeOrderDelivery.setStoreShipping(storeOrderWaimaiPlaceParam.isStoreShipping()==true?false:true);
        storeOrderDelivery.setWaimaiOrderId(waimaiOrderId);
        storeOrderDelivery.setWaimaiType(waimaiType);
        storeOrderDelivery.setWaimaiDaySeq(storeOrderWaimaiPlaceParam.getWaimaiDaySeq());
        storeOrderDelivery.setWaimaiPayType(storeOrderWaimaiPlaceParam.getWaimaiPayType());
        storeOrderDelivery.setWaimaiFirstOrder(storeOrderWaimaiPlaceParam.isFirstOrder());
        storeOrderDelivery.setWaimaiFavorites(storeOrderWaimaiPlaceParam.isFavorites());
        storeOrderDelivery.setShipperPhone(storeOrderWaimaiPlaceParam.getShipperPhone());
        storeOrderDelivery.setLatitude(storeOrderWaimaiPlaceParam.getLatitude());
        storeOrderDelivery.setLongitude(storeOrderWaimaiPlaceParam.getLongitude());
        //构造收费项目
        List<Long> orderChargeItemIds = storeOrderHelper.getChargeItemIdsOfStoreOrder(orderPlaceItems);
        List<StoreChargeItem> orderChargeItems = storeChargeItemService.getStoreChargeItemsInIds(merchantId, storeId, orderChargeItemIds, repastDate);
        if (orderChargeItems == null || orderChargeItems.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ITEM_CHARGE_NOT_EXIST.getValue(), DataUtil.infoWithParams("store order chargeitem not exist, storeId=#1, timeBucketId=#2, repastDate=#3, orderChargeItemIds=#4 ", new Object[]{storeId, timeBucketId,
            		repastDate, JsonUtil.build(orderChargeItemIds)}));
        }
        int optType;
        String optRemark;
		List<StoreOrderItem> storeOrderItems;
        if (storeOrder.getOrderId() == null || storeOrder.getOrderId().isEmpty()) {
            // 创建订单
            String orderId = storeOrderService.createStoreOrder(storeOrder, currencyId);// 创建订单
            storeOrderItems = storeOrderService.createStoreOrderItems(orderChargeItems, orderPlaceItems, null, storeOrder);// 创建订单子项目&订单明细
            optType = StoreOrderOptlogTypeEnum.PLACE_ORDER_CREATE.getValue();
            optRemark = "place waimai order, create";
            //创建订单外送信息
            storeOrderDelivery.setOrderId(orderId);
            storeOrderDelivery.create();
        } else {
            // 更新订单
        	storeOrderService.deleteStoreOrderItemsById(merchantId, storeId, inputOrderId);// 删除历史订单子项目&历史订单明细
            storeOrder.setUpdateTime(currentTime);
            storeOrder.update();
            storeOrderItems = storeOrderService.createStoreOrderItems(orderChargeItems, orderPlaceItems, null, storeOrder);// 创建新的订单子项目&订单明细
            optType = StoreOrderOptlogTypeEnum.PLACE_ORDER_UPDATE.getValue();
            optRemark = "place waimai order, update";
            //更新订单外送信息
            storeOrderDelivery.update();
        }
        // 记录日志
        storeOrderOptlogDAO.createOptlog(storeOrder, staffId, clientType, optType, optRemark);
        // 返回订单
        storeOrder.setStoreOrderItems(storeOrderItems);
		return storeOrder;
	}
	@Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
	public StoreOrderDelivery updateStoreOrderDelivery(StoreOrderWaimaiUpdateParam storeOrderWaimaiUpdateParam) throws T5weiException {
		boolean enableSlave = false;
		int merchantId = storeOrderWaimaiUpdateParam.getMerchantId();
		long storeId = storeOrderWaimaiUpdateParam.getStoreId();
		String waimaiOrderId = storeOrderWaimaiUpdateParam.getWaimaiOrderId();
		int waimaiType = storeOrderWaimaiUpdateParam.getWaimaiType();
		StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryDAO.getByWaimaiOrderId(merchantId, storeId, waimaiOrderId, waimaiType, enableSlave);
		if (storeOrderDelivery == null) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), DataUtil.infoWithParams("store waimai order not exist, storeId=#1, waimaiOrderId=#2 ", new Object[]{storeId, waimaiOrderId}));
		}
		storeOrderDelivery.snapshot();
		BeanUtil.copy(storeOrderWaimaiUpdateParam, storeOrderDelivery, true);
		storeOrderDelivery.update();
		return storeOrderDelivery;
	}
	
}
