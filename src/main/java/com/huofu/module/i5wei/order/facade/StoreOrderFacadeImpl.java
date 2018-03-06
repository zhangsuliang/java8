package com.huofu.module.i5wei.order.facade;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.ModuleBizType;
import com.huofu.module.i5wei.base.StoreOrderCombinedTaskCons;
import com.huofu.module.i5wei.base.StoreOrderModuleConfig;
import com.huofu.module.i5wei.delivery.entity.MerchantUserDeliveryAddress;
import com.huofu.module.i5wei.delivery.entity.StoreDeliveryBuilding;
import com.huofu.module.i5wei.delivery.entity.StoreDeliverySetting;
import com.huofu.module.i5wei.delivery.entity.UserDeliveryAddress;
import com.huofu.module.i5wei.delivery.service.MerchantUserDeliveryAddressService;
import com.huofu.module.i5wei.delivery.service.StoreDeliveryBuildingService;
import com.huofu.module.i5wei.delivery.service.StoreDeliverySettingService;
import com.huofu.module.i5wei.delivery.service.UserDeliveryAddressService;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.StoreOrderDeliveryDAO;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.order.entity.StoreOrderCombinedBiz;
import com.huofu.module.i5wei.order.entity.StoreOrderDelivery;
import com.huofu.module.i5wei.order.service.*;
import com.huofu.module.i5wei.pickupsite.entity.StorePickupSiteTimeSetting;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteHelpler;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteOrderService;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteTimeSettingService;
import com.huofu.module.i5wei.pickupsite.service.UserPickupSiteService;
import com.huofu.module.i5wei.printer.*;
import com.huofu.module.i5wei.promotion.service.*;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.queue.SQSConfig;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.entity.StoreTableSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import com.huofu.module.i5wei.setting.service.StoreTableSettingService;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.service.OrderPayFinishResult;
import com.huofu.module.i5wei.table.service.StoreTableRecordService;
import com.huofu.module.i5wei.wechat.WechatNotifyService;
import com.huofu.module.i5wei.wechat.WechatTempNotifyService;

import huofucore.facade.combinedbiz.CombinedBizFacade;
import huofucore.facade.combinedbiz.dto.CombinedBizKeyRefParam;
import huofucore.facade.combinedbiz.dto.CombinedBizParam;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.config.currency.CurrencyEnum;
import huofucore.facade.coupon.BestAndInvalidCouponType;
import huofucore.facade.coupon.BestCouponTypeDTO;
import huofucore.facade.coupon.BestCouponTypeParam;
import huofucore.facade.coupon.Coupon4CommonFacade;
import huofucore.facade.dialog.tweet.TweetEventType;
import huofucore.facade.dialog.visit.StoreUserVisitFacade;
import huofucore.facade.dialog.visit.UserVisitType;
import huofucore.facade.i5wei.delivery.MerchantDeliveryModeEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteOrderQueryParam;
import huofucore.facade.merchant.info.MerchantDTO;
import huofucore.facade.merchant.info.query.MerchantQueryFacade;
import huofucore.facade.merchant.preferential.MerchantPreferentialFacade;
import huofucore.facade.merchant.preferential.MerchantPreferentialOfUserDTO;
import huofucore.facade.merchant.setting.MerchantSettingDTO;
import huofucore.facade.merchant.setting.MerchantSettingFacade;
import huofucore.facade.merchant.store.StoreAutoPrinterCashierFacade;
import huofucore.facade.merchant.store.StoreDTO;
import huofucore.facade.merchant.store.StoreFacade;
import huofucore.facade.merchant.store.query.StoreQueryFacade;
import huofucore.facade.pay.channel.MerchantChannelFacade;
import huofucore.facade.pay.payment.*;
import huofucore.facade.pay.payment.RefundRecordDTO;
import huofucore.facade.prepaidcard.*;
import huofucore.facade.user.account.UserAccountDTO;
import huofucore.facade.user.account.UserAccountFacade;
import huofucore.facade.user.info.UserDTO;
import huofucore.facade.user.info.UserFacade;
import huofucore.facade.user.info.exception.TUserException;
import huofucore.facade.user.invoice.UserInvoiceDTO;
import huofucore.facade.user.invoice.UserInvoiceFacade;
import huofucore.facade.waimai.meituan.order.SelfDeliveryMeituanOrderParam;
import huofucore.facade.waimai.meituan.order.StoreMeituanOrderFacade;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.module.combinedtask.CombinedTaskData;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.PageResult;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.*;

@ThriftServlet(name = "storeOrderFacadeServlet", serviceClass = StoreOrderFacade.class)
@Component
public class StoreOrderFacadeImpl implements StoreOrderFacade.Iface {

    private static final Log log = LogFactory.getLog(StoreOrderFacadeImpl.class);

    @Autowired
    private StoreOrderService storeOrderService;

    @Autowired
    private StoreOrderQueryService storeOrderQueryService;

    @Autowired
    private StoreOrderHelper storeOrderHelper;

    @Autowired
    private StoreInventoryService storeInventoryService;

    @Autowired
    private StoreTimeBucketService storeTimeBucketService;

    @Autowired
    private StoreOrderFacadeValidate storeOrderFacadeValidate;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @ThriftClient
    private MerchantPreferentialFacade.Iface merchantPreferentialFacade;

    @ThriftClient
    private PayFacade.Iface payFacade;

    @ThriftClient
    private RefundFacade.Iface refundFacade;

    @ThriftClient
    private ComPayFacade.Iface comPayFacade;

    @ThriftClient
    private MerchantChannelFacade.Iface merchantChannelFacade;

    @ThriftClient
    private PrepaidCardFacade.Iface prepaidCardFacade;

    @ThriftClient
    private MerchantQueryFacade.Iface merchantQueryFacade;

    @ThriftClient
    private StoreFacade.Iface storeFacade;

    @ThriftClient
    private StoreQueryFacade.Iface storeQueryFacade;

    @ThriftClient
    private UserAccountFacade.Iface userAccountFacade;

    @ThriftClient
    private StoreUserVisitFacade.Iface storeUserVisitFacade;

    @ThriftClient
    private UserInvoiceFacade.Iface userInvoiceFacadeIface;

    @ThriftClient
    private Coupon4CommonFacade.Iface coupon4CommonFacade;

    @ThriftClient
    private OrderFacade.Iface orderFacade;

	@ThriftClient
	private UserFacade.Iface userFacade;

	@ThriftClient
	private StoreMeituanOrderFacade.Iface storeMeituanOrderFacade;

    @Autowired
    private StoreDeliveryBuildingService storeDeliveryBuildingService;

    @Autowired
    private UserDeliveryAddressService userDeliveryAddressService;

    @Autowired
    private StoreOrderDeliveryService storeOrderDeliveryService;

    @Autowired
    private StoreDeliverySettingService storeDeliverySettingService;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private WechatNotifyService wechatNotifyService;

    @Autowired
    private WechatTempNotifyService wechatTempNotifyService;

    @ThriftClient
    private StoreAutoPrinterCashierFacade.Iface storeAutoPrinterCashierFacade;

    @Autowired
    private StoreTableRecordService storeTableRecordService;

    @Autowired
    private StoreOrderRefundService storeOrderRefundService;

    @ThriftClient
    private MerchantSettingFacade.Iface merchantSettingFacade;

    @Autowired
    private MerchantUserDeliveryAddressService merchantUserDeliveryAddressService;

    @ThriftClient
    private CombinedBizFacade.Iface combinedBizFacadeIface;

    @Autowired
    private StoreOrderCombinedBizService storeOrderCombinedBizService;

    @ThriftClient
    private PayQueryFacade.Iface payQueryFacade;

    @Autowired
	private StoreChargeItemPromotionService storeChargeItemPromotionService;

    @Autowired
    private StorePromotionRebateService storePromotionRebateService;

    @Autowired
    private StorePromotionReduceService storePromotionReduceService;

	@Autowired
	private StoreOrderDeliveryDAO storeOrderDeliveryDAO;

    @Autowired
    private I5weiOrderTakeCodePrinter i5weiOrderTakeCodePrinter;

    @Autowired
    private I5weiTakeAndSendOutPrinter i5weiTakeAndSendOutPrinter;

    @Autowired
    private I5weiTakeCodePrinter i5weiTakeCodePrinter;

    @Autowired
    private I5weiKitchenMealListPrinter i5weiKitchenMealListPrinter;

    @Autowired
    private StoreTableSettingService storeTableSettingService;

    @Autowired
    private StorePromotionGratisService storePromotionGratisService;

    @Autowired
    private I5weiSettlementPrinter i5weiSettlementPrinter;

    @Autowired
    private UserPickupSiteService userPickupSiteService;

    @Autowired
    private StorePickupSiteHelpler storePickupSiteHelpler;

    @Autowired
    private StorePickupSiteTimeSettingService storePickupSiteTimeSettingService;

	@Autowired
	private StorePickupSiteOrderService storePickupSiteOrderService;

	@Override
    public StoreOrderDTO placeStoreOrder(StoreOrderPlaceParam storeOrderPlaceParam) throws TException {
        int merchantId = storeOrderPlaceParam.getMerchantId();
        long storeId = storeOrderPlaceParam.getStoreId();
        long repastDate = storeOrderPlaceParam.getRepastDate();
        long timeBucketId = storeOrderPlaceParam.getTimeBucketId();
        long userId = storeOrderPlaceParam.getUserId();
        int clientType = storeOrderPlaceParam.getClientType();
        boolean delivery = false;
        long tableRecordId = storeOrderPlaceParam.getTableRecordId();
        if (storeOrderPlaceParam.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
            delivery = true;
        }
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId, true);
        //参数校验
        if (delivery) {
            this.storeOrderFacadeValidate.validateDeliveryForPlaceOrder(storeOrderPlaceParam, storeTimeBucket);
        }
        storeOrderFacadeValidate.placeStoreOrderValidate(storeOrderPlaceParam, storeTimeBucket);
        //商户优惠信息
        MerchantPreferentialOfUserDTO rebateDto = new MerchantPreferentialOfUserDTO();
        if (userId > 0) {
            rebateDto = merchantPreferentialFacade.getMerchantPreferentialInfoOfUser(userId, merchantId, storeId, clientType);
        }
        MerchantDTO merchantDTO = merchantQueryFacade.getMerchant(merchantId);
        int currencyId = merchantDTO.getCurrency();
        if (currencyId == 0) {
            currencyId = CurrencyEnum.RMB.getValue();//如果没有则默认一个币种
        }
        //折扣活动&满减活动&买免活动
        StorePromotionQueryParam storePromotionQueryParam = new StorePromotionQueryParam(storeOrderPlaceParam);
        Map<Long, StoreChargeItemPromotion> chargeItemPromotionMap = storeChargeItemPromotionService.getStoreOrderItemPromotion(storeOrderPlaceParam);
        ChargeItemRebateInfo chargeItemRebateInfo = storePromotionRebateService.getBestStorePromotionRebateMap(storePromotionQueryParam);
        ChargeItemReduceInfo chargeItemReduceInfo = storePromotionReduceService.getBestStorePromotionReduce(storePromotionQueryParam);
        ChargeItemGratisInfo chargeItemGratisInfo = storePromotionGratisService.getBestStorePromotionGratis(storePromotionQueryParam);
        //商户店铺设置
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        StoreDeliverySetting storeDeliverySetting = null;
        if (delivery) {
            storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting(merchantId, storeId);
        }
        if (clientType == ClientTypeEnum.WECHAT.getValue()) {
            boolean enableAddDishes = this.isAddDishStoreOrder(merchantId, storeId, repastDate, timeBucketId, userId, 0);
            storeOrderPlaceParam.setEnableAddDishes(enableAddDishes);
        }
        PlaceOrderParam placeOrderParam = new PlaceOrderParam();
        placeOrderParam.setStoreOrderPlaceParam(storeOrderPlaceParam);
        placeOrderParam.setRebateDto(rebateDto);
        placeOrderParam.setCurrencyId(currencyId);
        placeOrderParam.setStore5weiSetting(store5weiSetting);
        placeOrderParam.setStoreDeliverySetting(storeDeliverySetting);
        placeOrderParam.setStoreTimeBucket(storeTimeBucket);
        placeOrderParam.setChargeItemPromotionMap(chargeItemPromotionMap);
        placeOrderParam.setChargeItemRebateInfo(chargeItemRebateInfo);
        placeOrderParam.setChargeItemReduceInfo(chargeItemReduceInfo);
        placeOrderParam.setChargeItemGratisInfo(chargeItemGratisInfo);
        StoreOrder placeOrder = storeOrderService.placeStoreOrder(placeOrderParam);
        // 关联桌台记录 add by licheng7
        StoreTableRecord storeTableRecord = storeTableRecordService.orderAddChargeItem(tableRecordId, placeOrder,storeOrderPlaceParam.getStoreSendType());
        if (storeTableRecord != null) {
            StoreOrder masterStoreOrder = storeTableRecord.getMasterStoreOrder();
            //客户取餐计入消费次数统计
            storeOrderHelper.accumulateStoreUserOrders(masterStoreOrder);
            //判断此订单是否为桌台子订单
            if(placeOrder.isTableRecordSubOrder()){
                //点菜宝下的订单，打印点（加）菜单
                if(storeOrderPlaceParam.isDiancaibaoPlaceOrder()){
                    placeOrder.setDiancaibaoPlaceOrder(storeOrderPlaceParam.isDiancaibaoPlaceOrder());
                    // 记录取餐票打印信息
                    storeOrderHelper.insertCashierPrintOrder(placeOrder);
                }
                // 点菜单
                i5weiTakeCodePrinter.sendPrintMessages(placeOrder);
                // 后厨清单
                List<String> orderIds = new ArrayList<>();
                orderIds.add(placeOrder.getOrderId());
                i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,tableRecordId,orderIds,storeTableRecord.getStoreMealTakeups());
                // 打包清单
                i5weiTakeAndSendOutPrinter.sendPrintMessages(placeOrder);
            }
        }
        //订单金额为0直接支付成功
        long payablePrice = placeOrder.getPayablePrice();
        if (payablePrice == 0) {
            StoreOrderPay5weiParam storeOrderPay5weiParam = new StoreOrderPay5weiParam();
            storeOrderPay5weiParam.setMerchantId(merchantId);
            storeOrderPay5weiParam.setStoreId(storeId);
            storeOrderPay5weiParam.setOrderId(placeOrder.getOrderId());
            StoreOrder payStoreOrder = storeOrderService.payStoreOrder(storeOrderPay5weiParam);
            payStoreOrder.setStoreTimeBucket(storeTimeBucket);
            payStoreOrder.setStoreTableRecord(storeTableRecord);
            StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(payStoreOrder);
            //设置用户余额
            this.setUserAccountRemainInfo(storeOrderDTO, 0);
            //支付完成微信通知客户
            wechatNotifyService.notifyOrderSuccessMsg(payStoreOrder);
            //用户下单记录
            i5weiMessageProducer.sendMessageOfStoreOrderVisit(merchantId, storeId, userId, UserVisitType.PLACE_ORDER);
            OrderPayFinishResult orderPayFinishResult = payStoreOrder.getOrderPayFinishResult();
            if (orderPayFinishResult != null) {
        		if (orderPayFinishResult.isSendTableRecordAddDishMsg()) {
            		try {
        				wechatNotifyService.notifyTableRecordAddDishMsg(orderPayFinishResult.getStoreTableRecord(), orderPayFinishResult.getUserId());
        			} catch (Exception e) {
        				log.warn("send storeTableRecord["+orderPayFinishResult.getStoreTableRecord().getTableRecordId()+"] add dish msg to user["+payStoreOrder.getUserId()+"] fail");
        			}
            	}
            	if (orderPayFinishResult.isSettleMent()) {
            		List<StoreOrder> storeOrders = orderPayFinishResult.getStoreOrders();
            		i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
            	}
        	}
            return storeOrderDTO;
        }
        // 调整支付总订单的金额
        try {
            // 如果已生成了对应的支付总订单，则调整支付总订单的金额
            orderFacade.updatePayOrderAmount(placeOrder.getOrderId(), PaySrcEnum.M_5WEI, placeOrder.getPayablePrice());
        } catch (TPayException e) {
            if (e.getErrorCode() == PayErrorCode.PAY_ORDER_INVALID.getValue()) {
                // 如果此时未生成对应的支付总订单则不必修改
            } else {
                log.error("amount of pay order update fail {orderId:" + placeOrder.getOrderId() + ", payOrderId:" + placeOrder.getPayOrderId() + ", amount:" + placeOrder.getPayablePrice() + "}", e);
                throw e;
            }
        }
        placeOrder.setStoreTableRecord(storeTableRecord);
        //返回：订单DTO+订单明细列表
        //设置用户余额
        placeOrder.setStoreTimeBucket(storeTimeBucket);
        StoreOrderDTO placeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(placeOrder);
        this.setUserAccountRemainInfo(placeOrderDTO, 0);
        if (placeOrderDTO.getOrderId() == null || placeOrderDTO.getOrderId().isEmpty()) {
            log.error(DataUtil.infoWithParams("placeStoreOrder, storeId=#1, userId=#2 , orderId=#3", new Object[]{placeOrderDTO.getStoreId(), placeOrderDTO.getUserId(), placeOrderDTO.getOrderId()}));
        }
        return placeOrderDTO;
    }

    @Override
    public StoreOrderPageDTO getStoreOrders(StoreOrdersQueryParam storeOrdersQueryParam) throws TException {
        long repastDate = storeOrdersQueryParam.getRepastDate();
        if (repastDate <= 0) {
            storeOrdersQueryParam.setRepastDate(DateUtil.getBeginTime(new Date(), null));
        }
        int total = storeOrderQueryService.countStoreOrders(storeOrdersQueryParam);
        StoreOrderPageDTO storeOrderPageDTO = new StoreOrderPageDTO();
        storeOrderPageDTO.setTotal(total);
        storeOrderPageDTO.setSize(storeOrdersQueryParam.getSize());
        storeOrderPageDTO.setPageNo(storeOrdersQueryParam.getPageNo());
        storeOrderPageDTO.setPageNum(PageUtil.getPageNum(total, storeOrdersQueryParam.getSize()));
        if (total == 0) {
            return storeOrderPageDTO;
        }
        List<StoreOrder> storeOrders = storeOrderQueryService.getStoreOrders(storeOrdersQueryParam);
        //返回：订单DTO+订单明细列表
        List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(storeOrders);
        storeOrderPageDTO.setDataList(storeOrderDTOs);
        return storeOrderPageDTO;
    }

    /**
     * 1=我的订单-待消费
     * 2=我的订单-已消费
     * 3=我的订单-已退款
     * 4=我的订单-可取餐
     * 5=我的订单-可退款
     */
    @Override
    public StoreOrderPageDTO getStoreOrdersByStatus(StoreOrdersQueryByStatusParam storeOrdersQueryByStatusParam) throws TException {
        StoreOrderPageDTO storeOrderPageDTO = new StoreOrderPageDTO();
        storeOrderPageDTO.setSize(storeOrdersQueryByStatusParam.getSize());
        storeOrderPageDTO.setPageNo(storeOrdersQueryByStatusParam.getPageNo());
        int merchantId = storeOrdersQueryByStatusParam.getMerchantId();
        long inputStoreId = storeOrdersQueryByStatusParam.getStoreId();
        long userId = storeOrdersQueryByStatusParam.getUserId();
        List<Long> visitStoreIds = new ArrayList<>();
        if (inputStoreId > 0) {
            visitStoreIds.add(inputStoreId);
        } else {
            visitStoreIds = storeUserVisitFacade.getStoreIdsByUserId(merchantId, userId);
        }
        if (visitStoreIds == null || visitStoreIds.isEmpty()) {
            return storeOrderPageDTO;
        }
        int maxTotal = 0;
        List<StoreOrder> allStoreOrders = new ArrayList<>();
        for (Long storeId : visitStoreIds) {
            storeOrdersQueryByStatusParam.setStoreId(storeId);
            StoreOrderStatusTypeEnum orderStatusType = storeOrdersQueryByStatusParam.getOrderStatusType();
            if (orderStatusType.equals(StoreOrderStatusTypeEnum.CAN_TAKE_MEAL)) {
                long currentTime = System.currentTimeMillis();
                long repastDate = DateUtil.getBeginTime(currentTime, null);
                StoreTimeBucket storeTimeBucket;
                try {
                    storeTimeBucket = storeTimeBucketService.getStoreTimeBucketForDate(merchantId, storeId, 0, repastDate);
                } catch (T5weiException e) {
                    if (inputStoreId > 0 && storeId == inputStoreId) {
                        throw e;
                    }
                    continue;
                }
                if (storeTimeBucket == null) {
                    continue;
                }
                long timeBucketId = storeTimeBucket.getTimeBucketId();
                storeOrdersQueryByStatusParam.setRepastDate(repastDate);
                storeOrdersQueryByStatusParam.setTimeBucketId(timeBucketId);
            }
            int total = storeOrderQueryService.countStoreOrders(storeOrdersQueryByStatusParam);
            if (total == 0) {
                continue;
            }
            if (maxTotal == 0) {
                maxTotal = total;
            }
            if (total > maxTotal) {
                maxTotal = total;
            }
            List<StoreOrder> storeOrders = storeOrderQueryService.getStoreOrders(storeOrdersQueryByStatusParam, true);
            if (storeOrders != null) {
                allStoreOrders.addAll(storeOrders);
            }
        }
        storeOrderPageDTO.setTotal(maxTotal);
        storeOrderPageDTO.setPageNum(PageUtil.getPageNum(maxTotal, storeOrdersQueryByStatusParam.getSize()));
        //返回：订单DTO+订单明细列表
        List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(allStoreOrders);
        Map<Long, Set<Long>> storeTimeBucketMap = storeOrderHelper.getOrderStoreTimeBucketIds(allStoreOrders);
        Set<Long> storeIds = storeTimeBucketMap.keySet();
        Map<Long, StoreDTO> storeDTOMap = storeFacade.getStoreMap(merchantId, new ArrayList<>(storeIds));
        Map<Long, StoreTimeBucket> timeBucketMap = storeTimeBucketService.getStoreTimeBucketMapInIds(merchantId, storeTimeBucketMap);
        for (StoreOrderDTO storeOrderDTO : storeOrderDTOs) {
            long timeBucketId = storeOrderDTO.getTimeBucketId();
            StoreDTO storeDTO = storeDTOMap.get(storeOrderDTO.getStoreId());
            StoreTimeBucket storeTimeBucket = timeBucketMap.get(timeBucketId);
            storeOrderDTO.setStoreName(storeDTO.getName());
            storeOrderDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
        }
        storeOrderPageDTO.setDataList(storeOrderDTOs);
        return storeOrderPageDTO;
    }

    @Override
    public StoreOrderDTO getStoreOrderById(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        return storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);

    }

    @Override
    public long getStoreOrderPriceById(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        return storeOrder.getPayablePrice();
    }

    @Override
    public StoreOrderDTO getStoreOrderWithDeliveryById(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        StoreOrderDTO orderDTO = BeanUtil.copy(storeOrder, StoreOrderDTO.class);
        StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryService.getStoreOrderDeliveryById(merchantId, storeId, orderId);
        if (storeOrderDelivery != null) {
            StoreOrderDeliveryDTO storeOrderDeliveryDTO = BeanUtil.copy(storeOrderDelivery, StoreOrderDeliveryDTO.class);
            orderDTO.setStoreOrderDeliveryDTO(storeOrderDeliveryDTO);
        }
        return orderDTO;
    }

    @Override
    public StoreOrderDTO getStoreOrderDetailById(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = storeOrderService.getStoreOrderDetailById(merchantId, storeId, orderId);
        StoreOrderDTO storeOrderDTO = buildStoreOrderDTO(merchantId, storeId, storeOrder);
        String payOrderId = storeOrderDTO.getPayOrderId();
        try {
            storeOrderHelper.setPayResultInfo(storeOrderDTO);
            storeOrderHelper.setRefundResultInfo(storeOrderDTO);
            this.setUserAccountRemainInfo(storeOrderDTO, 0);
            // 判断是不是充值卡组合业务
            if (storeOrder.isCombinedBuyPrepaidCard()) {
                this._setBuyPrepaidCardInfoDTO(merchantId, storeId, orderId, storeOrderDTO);
            }
            // 增加自提点返回信息
            if (storeOrderDTO.getOrderId() == null || storeOrderDTO.getOrderId().isEmpty()) {
                log.error(DataUtil.infoWithParams("placeStoreOrder, storeId=#1, userId=#2 , orderId=#3", new Object[]{storeOrderDTO.getStoreId(), storeOrderDTO.getUserId(), storeOrderDTO.getOrderId()}));
            }
            //设置默认自提点
            if(storeOrderDTO.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue() && storeOrderDTO.getPayStatus() == StoreOrderPayStatusEnum.NOT.getValue()) {
                storeOrderDTO
                        .setStorePickupSite(storePickupSiteOrderService.setOrderPickupSite(merchantId, storeId, storeOrderDTO.getUserId(), storeOrderDTO.getTimeBucketId(), storeOrder));
            }
            //设置订单使用自提点
            if (storeOrderDTO.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue() && storeOrderDTO.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue()) {
                storeOrderDTO.setStorePickupSite(this.storePickupSiteOrderService.getPickupSiteBaseDTOByOrderId(
                        merchantId, storeId, storeOrderDTO.getStoreOrderDeliveryDTO().getStorePickupSiteId(), storeOrderDTO.getTimeBucketId()));
            }
        } catch (Throwable e) {
            log.error("fail to setPayResultInfo, setUserAccountRemainInfo, payOrderId=" + payOrderId, e);
        }
        return storeOrderDTO;
    }

    @Override
    public List<StoreOrderDTO> getStoreOrdersWithTimeBucket(int merchantId, long storeId, List<String> orderIds) throws TException {
        List<StoreOrder> storeOrders = storeOrderQueryService.getStoreOrders(merchantId, storeId, orderIds);
        if (storeOrders == null || storeOrders.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> timeBucketIds = new HashSet<>();
        for (StoreOrder storeOrder : storeOrders) {
            long timeBucketId = storeOrder.getTimeBucketId();
            timeBucketIds.add(timeBucketId);
        }
        Map<Long, StoreTimeBucket> timeBucketMap = storeTimeBucketService.getStoreTimeBucketMapInIds(merchantId, storeId, new ArrayList<>(timeBucketIds));
        List<StoreOrderDTO> storeOrderDTOs = new ArrayList<>();
        for (StoreOrder storeOrder : storeOrders) {
            StoreOrderDTO storeOrderDTO = BeanUtil.copy(storeOrder, StoreOrderDTO.class);
            long timeBucketId = storeOrder.getTimeBucketId();
            StoreTimeBucket storeTimeBucket = timeBucketMap.get(timeBucketId);
            StoreTimeBucketDTO storeTimeBucketDTO = BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class);
            storeOrderDTO.setStoreTimeBucketDTO(storeTimeBucketDTO);
            storeOrderDTOs.add(storeOrderDTO);
        }
        return storeOrderDTOs;
    }

    @Override
    public StoreOrderDTO getStoreOrderDetailByTakeCode(int merchantId, long storeId, long repastDate, String takeCode) throws TException {
        long repastDateTime = DateUtil.getBeginTime(repastDate, null);
        StoreOrder storeOrder = storeOrderQueryService.getStoreOrderDetailByTakeCode(merchantId, storeId, repastDateTime, takeCode);
        return buildStoreOrderDTO(merchantId, storeId, storeOrder);
    }

    @Override
    public StoreUserRemainDTO getUserAccountRemainInfo(StoreUserRemainParam storeUserRemainParam) throws TException {
        StoreUserRemainDTO storeUserRemainDTO = new StoreUserRemainDTO();
        int merchantId = storeUserRemainParam.getMerchantId();
        long userId = storeUserRemainParam.getUserId();
        long storeId = storeUserRemainParam.getStoreId();
        //封装最优优惠券参数
        BestCouponTypeParam bestCouponTypeParam = new BestCouponTypeParam();
        bestCouponTypeParam.setMerchantId(merchantId);
        bestCouponTypeParam.setUserId(userId);
        bestCouponTypeParam.setStoreId(storeId);

        if (storeId <= 0 || userId <= 0) {
            return storeUserRemainDTO;
        }
        MerchantDTO merchantDTO = merchantQueryFacade.getMerchant(merchantId);
        int currencyId = merchantDTO.getCurrency();
        if (currencyId == 0) {
            currencyId = CurrencyEnum.RMB.getValue(); //如果没有则默认一个币种
        }
        long prepaidCardBalance = prepaidCardFacade.getUserBalance(userId, merchantId);
        UserAccountDTO userAccountInfo = userAccountFacade.getUserAccountForUser(userId, currencyId);
        //构造返回值
        storeUserRemainDTO.setMerchantId(merchantId);
        storeUserRemainDTO.setStoreId(storeId);
        storeUserRemainDTO.setUserId(userId);
        storeUserRemainDTO.setCurrencyId(currencyId);
        storeUserRemainDTO.setPrepaidCardBalance(prepaidCardBalance);
        storeUserRemainDTO.setUserAccountBalance(userAccountInfo.getBalance());
        //筛选出订单里支持用优惠券支付的收费项目的价格总和 
        StoreUserCouponDTO bestUserCouponDTO = new StoreUserCouponDTO();
        long couponPriceAmount = storeUserRemainParam.getOrderCouponPrice();
        if (storeUserRemainParam.isLoadBestCoupon() && couponPriceAmount > 0) {
            bestCouponTypeParam.setAmount(couponPriceAmount);
            bestCouponTypeParam.setSize(1);
            bestCouponTypeParam.setTimeBucketId(storeUserRemainParam.getTimeBucketId());
            bestCouponTypeParam.setRepastDate(storeUserRemainParam.getRepastDate());
            bestCouponTypeParam.setTakeMode(storeUserRemainParam.getTakeMode());//取餐类型
            BestAndInvalidCouponType bestAndInvalidCouponType = coupon4CommonFacade.getBestCouponType2(bestCouponTypeParam);
            if (bestAndInvalidCouponType != null
                    && bestAndInvalidCouponType.getBestCouponTypeDTOList() != null
                    && !bestAndInvalidCouponType.getBestCouponTypeDTOList().isEmpty()) {
                BestCouponTypeDTO bestCouponTypeDTO = bestAndInvalidCouponType.getBestCouponTypeDTOList().get(0);
                bestUserCouponDTO.setCouponTypeId(bestCouponTypeDTO.getCouponTypeId());
                bestUserCouponDTO.setCouponTypeName(bestCouponTypeDTO.getCouponTypeName());
                bestUserCouponDTO.setCouponDiscountAmount(bestCouponTypeDTO.getDiscountAmount());
            }
        }
        storeUserRemainDTO.setBestUserCouponDTO(bestUserCouponDTO);
        return storeUserRemainDTO;
    }

    /**
     * 在订单中设置用户余额（设计不合理将来要去掉，改用getUserAccountRemainInfo）
     */
    private void setUserAccountRemainInfo(StoreOrderDTO storeOrderDTO, int errorCode) throws TException {
        if (storeOrderDTO.getUserId() <= 0) {
            return;
        }
        StoreUserRemainParam storeUserRemainParam = new StoreUserRemainParam();
        storeUserRemainParam.setMerchantId(storeOrderDTO.getMerchantId());
        storeUserRemainParam.setStoreId(storeOrderDTO.getStoreId());
        storeUserRemainParam.setUserId(storeOrderDTO.getUserId());
        storeUserRemainParam.setTimeBucketId(storeOrderDTO.getTimeBucketId());
        storeUserRemainParam.setOrderCouponPrice(storeOrderDTO.getOrderCouponPrice());
        if (errorCode > 0) {
            storeUserRemainParam.setLoadBestCoupon(true);
        }
        StoreUserRemainDTO storeUserRemainDTO = this.getUserAccountRemainInfo(storeUserRemainParam);
        Map<String, String> userRemainInfo = new HashMap<>();
        userRemainInfo.put("prepaid_card_balance", storeUserRemainDTO.getPrepaidCardBalance() + "");
        userRemainInfo.put("user_account_balance", storeUserRemainDTO.getUserAccountBalance() + "");
        userRemainInfo.put("currency_id", storeUserRemainDTO.getCurrencyId() + "");
        userRemainInfo.put("coupon_type_id", storeUserRemainDTO.getBestUserCouponDTO().getCouponTypeId() + "");
        userRemainInfo.put("coupon_type_name", storeUserRemainDTO.getBestUserCouponDTO().getCouponTypeName() + "");
        userRemainInfo.put("coupon_discount_amount", storeUserRemainDTO.getBestUserCouponDTO().getCouponDiscountAmount() + "");
        userRemainInfo.put("error_code", errorCode + "");
        storeOrderDTO.setUserRemainInfo(userRemainInfo);
    }

    /**
     * 微信对话框支付
     */
    @Override
    public StoreOrderDTO toPayStoreOrder(int merchantId, long storeId, String orderId) throws TException {
        if (orderId == null || orderId.isEmpty()) {
            log.error(DataUtil.infoWithParams("toPayStoreOrder, storeId=#1, orderId=#2", new Object[]{storeId, orderId}));
        }
        StoreOrderPay5weiParam storeOrderPay5weiParam = new StoreOrderPay5weiParam();
        storeOrderPay5weiParam.setMerchantId(merchantId);
        storeOrderPay5weiParam.setStoreId(storeId);
        storeOrderPay5weiParam.setOrderId(orderId);
        //支付前查询余额
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        storeOrderHelper.setStoreOrderDetail(storeOrder, false);
        long userId = storeOrder.getUserId();
        int clientType = storeOrder.getClientType();
        if (clientType != ClientTypeEnum.CASHIER.getValue() && userId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), " user pay order , userId is null ");
        }
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue() || storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
            StoreOrderDTO resultDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
            //设置用户余额
            this.setUserAccountRemainInfo(resultDTO, 0);
            return resultDTO;
        }
        //校验参数，检查库存
        storeOrderFacadeValidate.payStoreOrderValidate(storeOrderPay5weiParam, storeOrder);
        //调用m-pay完成支付
        PayOrderDTO payOrderDTO;
        String payOrderId = "";
        try {
            String transOrderId = storeOrder.getOrderId();
            PaySrcEnum src = PaySrcEnum.M_5WEI;
            int currencyId = storeOrder.getOrderCurrencyId();
            long amount = storeOrder.getPayablePrice();
            //筛选出订单里支持用优惠券支付的收费项目（订单子项StoreOrderItem）的价格总和 
            long couponPriceAmount = storeOrder.getOrderCouponPrice();
            long repastDate = storeOrder.getRepastDate();
            long timeBucketId = storeOrder.getTimeBucketId();
            String transInfo = this.getTransInfo(merchantId, storeId, timeBucketId, repastDate);
            ComPayParam comPayParam = new ComPayParam();
            comPayParam.setMerchantId(merchantId);
            comPayParam.setStoreId(storeId);
            comPayParam.setTransOrderId(transOrderId);
            comPayParam.setUserId(userId);
            comPayParam.setSrc(src);
            comPayParam.setTransInfo(transInfo);
            comPayParam.setCurrencyId(currencyId);
            comPayParam.setAmount(amount);

            //封装最优优惠券入口参数
            BestCouponTypeParam bestCouponTypeParam = new BestCouponTypeParam();
            bestCouponTypeParam.setMerchantId(merchantId);
            bestCouponTypeParam.setUserId(userId);
            bestCouponTypeParam.setSize(1);
            bestCouponTypeParam.setStoreId(storeId);
            bestCouponTypeParam.setTimeBucketId(storeOrder.getTimeBucketId());
            bestCouponTypeParam.setRepastDate(storeOrder.getRepastDate());
            bestCouponTypeParam.setTakeMode(1);
            if (couponPriceAmount > 0) {
                bestCouponTypeParam.setAmount(couponPriceAmount);
                BestAndInvalidCouponType bestAndInvalidCouponType = coupon4CommonFacade.getBestCouponType2(bestCouponTypeParam);
                if (bestAndInvalidCouponType != null && bestAndInvalidCouponType.getBestCouponTypeDTOList() != null && !bestAndInvalidCouponType.getBestCouponTypeDTOList().isEmpty()) {
                    BestCouponTypeDTO bestCouponTypeDTO = bestAndInvalidCouponType.getBestCouponTypeDTOList().get(0);
                    comPayParam.setCouponTypeId(bestCouponTypeDTO.getCouponTypeId());
                    comPayParam.setCouponAmount(bestCouponTypeDTO.getDiscountAmount());
                }
            }
            payOrderDTO = comPayFacade.requestDefaultComPay(comPayParam);
            payOrderId = payOrderDTO.getPayOrderId();
        } catch (TPayException e) {
            if (e.getErrorCode() == PayErrorCode.USER_ACCOUNT_BALANCE_NOT_ENOUGH.getValue()
                    || e.getErrorCode() == PayErrorCode.AMOUNT_NOT_ENOUGH.getValue()
                    || e.getErrorCode() == PayErrorCode.BALANCE_USER_MERCHANTPREPAIDCARD_NOT_ENOUGH.getValue()
                    || e.getErrorCode() == PayErrorCode.BALANCE_USER_ACCOUNT_NOT_NOT_ENOUGH.getValue()
                    || e.getErrorCode() == PayErrorCode.BALANCE_ACCOUNT_WECHAT_NOT_ENOUGH.getValue()) {
                StoreOrderDTO resultDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
                //设置用户余额
                this.setUserAccountRemainInfo(resultDTO, PayErrorCode.USER_ACCOUNT_BALANCE_NOT_ENOUGH.getValue());
                return resultDTO;
            }
            String errorInfo = DataUtil.infoWithParams(e.getMessage() + ", storeId=#1, orderId=#2, userId=#3, payOrderId=#4", new Object[]{storeId, orderId, userId, payOrderId});
            log.error(errorInfo, e);
            throw new T5weiException(e.getErrorCode(), errorInfo);
        } catch (TException e) {
            String errorInfo = DataUtil.infoWithParams("storeId=#1, orderId=#2, userId=#3, payOrderId=#4 ", new Object[]{storeId, orderId, userId, payOrderId});
            log.error(errorInfo, e);
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_FAILURE.getValue(), errorInfo);
        }
        StoreOrder payStoreOrder;
        StoreOrderDTO resultDTO;
        if (payOrderDTO.getStatus().equals(PayOrderStatusDTO.PAYING)) {
            //支付中
            payStoreOrder = storeOrderService.toPayStoreOrder(merchantId, storeId, orderId, payOrderId, 0);
            resultDTO = storeOrderHelper.getStoreOrderDTOByEntity(payStoreOrder);
        } else {
            //支付成功，五味订单支付完成
            storeOrderPay5weiParam.setPayOrderId(payOrderId);
            payStoreOrder = storeOrderService.payStoreOrder(storeOrderPay5weiParam);
            // 后厨清单
            List<String> orderIds = new ArrayList<>();
            orderIds.add(payStoreOrder.getOrderId());
            i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,payStoreOrder.getTableRecordId(),orderIds,payStoreOrder.getStoreMealTakeups());
            resultDTO = storeOrderHelper.getStoreOrderDTOByEntity(payStoreOrder);
            //设置用户余额
            storeOrderHelper.setPayResultInfo(resultDTO);
            this.setUserAccountRemainInfo(resultDTO, 0);
        }
        return resultDTO;
    }


    private String getTransInfo(int merchantId, long storeId, long timeBucketId, long repastDate) throws TException {
        MerchantDTO merchant = merchantQueryFacade.getMerchant(merchantId);
        StoreDTO store = storeQueryFacade.getStore(merchantId, storeId);
        StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId, true);
        String time = DateUtil.formatDate("yyyy年MM月dd日", new Date(repastDate));
        return merchant.getName() + store.getName() + time + storeTimeBucket.getName();
    }

    /**
     * 网页支付（包括外卖和打包带走）
     */
    @Override
    public StoreOrderDTO payStoreOrder(StoreOrderPayParam storeOrderPayParam) throws TException {
        //校验参数，检查库存
        int merchantId = storeOrderPayParam.getMerchantId();
        long storeId = storeOrderPayParam.getStoreId();
        String orderId = storeOrderPayParam.getOrderId();
        long userId = storeOrderPayParam.getUserId();
	    int dynamicPayMethod = storeOrderPayParam.getDynamicPayMethod();
	    // 将桌台记录置为结账中状态
        storeTableRecordService.tableRecordToSettling(merchantId, storeId, orderId, true, ClientTypeEnum.WECHAT.getValue());
        //优惠券支付
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.DOING.getValue() || storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
            storeOrderHelper.setStoreOrderDetail(storeOrder, false);
            return storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        }
        int clientType = storeOrder.getClientType();
	    if (dynamicPayMethod == 0) {//非外卖类型 edit by Jemon
		    if (clientType != ClientTypeEnum.CASHIER.getValue() && userId <= 0) {
			    throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), " user pay order , userId is null ");
		    }
        }
        //参数校验
        StoreOrderPay5weiParam storeOrderPay5weiParam = new StoreOrderPay5weiParam();
        storeOrderPay5weiParam.setMerchantId(merchantId);
        storeOrderPay5weiParam.setStoreId(storeId);
        storeOrderPay5weiParam.setOrderId(orderId);
        storeOrderPay5weiParam.setUserId(userId);
        storeOrderPay5weiParam.setUserRemark(storeOrderPayParam.getUserRemark());
        storeOrderFacadeValidate.payStoreOrderValidate(storeOrderPay5weiParam, storeOrder);
        //更新用户下单备注
        if (DataUtil.isNotEmpty(storeOrderPayParam.getUserRemark())) {
            this.storeOrderService.updateOrder4Pay(storeOrderPayParam);
        }
        if (storeOrderPayParam.getPrepaidCardTypeId() > 0) {
            return payStoreOrderWithPrepaidCard(storeOrderPayParam);
        }
        return payStoreOrderTradition(storeOrderPayParam);
    }

    /**
     * 订单买卡支付第一步:购买充值卡
     */
    private StoreOrderDTO payStoreOrderWithPrepaidCard(StoreOrderPayParam storeOrderPayParam) throws TException {
        int merchantId = storeOrderPayParam.getMerchantId();
        long storeId = storeOrderPayParam.getStoreId();
        String orderId = storeOrderPayParam.getOrderId();
        long userId = storeOrderPayParam.getUserId();
        StoreOrderPayModeEnum otherPayMode = storeOrderPayParam.getOtherPayMode();

        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        //更新bizType
        storeOrder.updateCombineBizType(StoreOrderCombinedBizType.BUY_PREPAIDCARD_PAY_STORE_ORDER.getValue());
        //获得充值卡类型,计算需要支付的金额
        MerchantPrepaidCardTypeDTO prepaidCardTypeDTO = this.prepaidCardFacade.getPrepaidCardTypeById(storeOrderPayParam.getPrepaidCardTypeId(), merchantId);
        //用户支付金额 目前只购买一张卡
        long userPayPrice = prepaidCardTypeDTO.getPrice();
        //预付费卡实际购买面额 目前只购买一张卡
        long prepaidCardPrice = prepaidCardTypeDTO.getFaceValue();
        //默认创建的购买充值卡订单状态是“交易中”
        CreateMerchantPrepaidCardOrderParam param = new CreateMerchantPrepaidCardOrderParam();
        param.setPrepaidCardTypeId(storeOrderPayParam.getPrepaidCardTypeId());
        param.setFaceValue((int) prepaidCardPrice);
        param.setPrice(userPayPrice);
        param.setMerchantId(merchantId);
        param.setUserId(userId);
        param.setStoreId(storeId);
        param.setStaffId(storeOrder.getStaffId());
        param.setCombinedBizType(PrepaidCardCombinedBizType.BUY_PREPAIDCARD_PAY_STORE_ORDER.getValue());
        MerchantPrepaidCardOrderDTO prepaidCardOrder = this.prepaidCardFacade.createMerchantPrepaidCardOrderV2(param);

        String transOrderId = prepaidCardOrder.getPrepaidCardOrderId();

        Map<String, Object> dataMap = Maps.newHashMap();
        dataMap.put("merchantId", merchantId);
        dataMap.put("storeId", storeId);
        dataMap.put("orderId", orderId);
        dataMap.put("userId", userId);
        dataMap.put("couponTypeId", storeOrderPayParam.getCouponTypeId());
        dataMap.put("couponPayAmount", storeOrderPayParam.getCouponPayAmount());

        long payablePrice = storeOrder.getPayablePrice();
        long prepaidCardPayAmount = payablePrice - storeOrderPayParam.getCouponPayAmount() - storeOrderPayParam.getUserAccountPayAmount();
        dataMap.put("prepaidCardPayAmount", prepaidCardPayAmount); //充值卡应付金额

        dataMap.put("userAccountPayAmount", storeOrderPayParam.getUserAccountPayAmount());
        dataMap.put("otherPayAmount", storeOrderPayParam.getOtherPayAmount());
        dataMap.put("otherPayMode", storeOrderPayParam.getOtherPayMode());
        dataMap.put("prepaidCardOrderId", transOrderId);
        CombinedTaskData combinedTaskData = new CombinedTaskData();
        combinedTaskData.setSubject(StoreOrderCombinedTaskCons.TASKSUBJECT_BUYPREPAIDCARD_PAYSTOREORDER);
        combinedTaskData.setDataMap(dataMap);
        combinedTaskData.addTask(ModuleBizType.BUY_PREPAIDCARD.getValue());
        combinedTaskData.addTask(ModuleBizType.PAY_FOR_STORE_ORDER.getValue());
        String json = combinedTaskData.toJson();

        //记录任务
        CombinedBizParam combinedBizParam = new CombinedBizParam();
        combinedBizParam.setModuleId(StoreOrderModuleConfig.I5WEI_MODULE_ID);
        combinedBizParam.setSqsQueueName(SQSConfig.getI5weiCombinTaskQueue());
        combinedBizParam.setData(json);
        combinedBizParam.addToCombinedBizKeyRefParams(new CombinedBizKeyRefParam(StoreOrderModuleConfig.PREPAIDCARD_MODULE_ID, prepaidCardOrder.getPrepaidCardOrderId()));
        this.combinedBizFacadeIface.saveCombinedBiz(combinedBizParam);

        if (otherPayMode == null) {
            otherPayMode = StoreOrderPayModeEnum.UNKNOWN;
        }
        DefineComPayParam payParam = new DefineComPayParam();
        payParam.setMerchantId(merchantId);
        payParam.setStoreId(storeId);
        payParam.setTransOrderId(transOrderId);
        payParam.setUserId(userId);
        payParam.setSrc(PaySrcEnum.M_PREPAIDCARD_MERCHANT);
        payParam.setCurrencyId(storeOrder.getOrderCurrencyId());
        payParam.setTransInfo(this.storeOrderService.buildTransInfo4PrepaidCard(merchantId));
        this.buildDefineComPayParam(userPayPrice, storeOrder, storeOrderPayParam, payParam, true);
        Map<String, String> orderPayInfo;
        int payStatus;
        try {
            //目前没有异步请求微信支付下单
            DefineComPayResult defineComPayResult = payFacade.requestDefineComPay(payParam);
            payStatus = defineComPayResult.getStatus();

            if (payStatus == PayOrderStatusDTO.PAYING.getValue()) {
                //记录操作日志,买充值卡进行中
                this.storeOrderService.savePay4BuyPrepaidLog(merchantId, storeId, orderId, StoreOrderOptlogTypeEnum.COMBINED_BUY_PREPAIDCARD_ING.getValue(), "buy prepaidcard ing");
            }
            //微信支付信息
            orderPayInfo = this.storeOrderService.buildWechatPayInfo(defineComPayResult, otherPayMode);
        } catch (TPayException e) {
            String errorInfo = e.getMessage() + ", inputParams = " + storeOrderPayParam;
            log.error(errorInfo);
            throw new T5weiException(e.getErrorCode(), e.getMessage());
        } catch (TException e) {
            String errorInfo = e.getMessage() + ", inputParams = " + storeOrderPayParam;
            log.error("storeOrder pay failure:" + errorInfo);
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_FAILURE.getValue(), errorInfo);
        }

        // 第一步买卡的时候,不需要修改订单状态和库存,也不记录支付订单id,因为这个是买充值卡操作,并没有对订单进行支付
        // 在第二步对订单进行支付时候在修改
        StoreOrderDTO resultDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        if (!orderPayInfo.isEmpty()) {
            resultDTO.setOrderPayInfo(orderPayInfo);
        }
        return resultDTO;
    }

    /**
     * 传统订单支付
     */
    public StoreOrderDTO payStoreOrderTradition(StoreOrderPayParam storeOrderPayParam) throws TException {
        int merchantId = storeOrderPayParam.getMerchantId();
        long storeId = storeOrderPayParam.getStoreId();
        String orderId = storeOrderPayParam.getOrderId();
        long userId = storeOrderPayParam.getUserId();
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        StoreOrderPay5weiParam storeOrderPay5weiParam = new StoreOrderPay5weiParam();
        storeOrderPay5weiParam.setMerchantId(merchantId);
        storeOrderPay5weiParam.setStoreId(storeId);
        storeOrderPay5weiParam.setOrderId(orderId);
        storeOrderPay5weiParam.setUserId(userId);
        storeOrderPay5weiParam.setUserRemark(storeOrderPayParam.getUserRemark());

        StoreOrderPayModeEnum otherPayMode = storeOrderPayParam.getOtherPayMode();
        //优惠券支付
        long couponTypeId = storeOrderPayParam.getCouponTypeId();
        long couponPayAmount = storeOrderPayParam.getCouponPayAmount();

        //组合支付
        String transOrderId = orderId;
        PaySrcEnum src = PaySrcEnum.M_5WEI;
        int currencyId = storeOrder.getOrderCurrencyId();
        long repastDate = storeOrder.getRepastDate();
        long timeBucketId = storeOrder.getTimeBucketId();
        String transInfo = this.getTransInfo(merchantId, storeId, timeBucketId, repastDate);
        String payOrderId;
        long payAmount;
        int payStatus;
        Map<String, String> orderPayInfo = new HashMap<>();
        try {
            //其他支付方式
            if (otherPayMode == null) {
                otherPayMode = StoreOrderPayModeEnum.UNKNOWN;
            }
            if (otherPayMode.equals(StoreOrderPayModeEnum.AUTO_PAY)) {
                long amount = storeOrder.getPayablePrice();
                //自动充值支付
                ComPayParam comPayParam = new ComPayParam();
                comPayParam.setMerchantId(merchantId);
                comPayParam.setStoreId(storeId);
                comPayParam.setTransOrderId(transOrderId);
                comPayParam.setUserId(userId);
                comPayParam.setSrc(src);
                comPayParam.setTransInfo(transInfo);
                comPayParam.setCurrencyId(currencyId);
                comPayParam.setAmount(amount);
                if (couponPayAmount > 0) {
                    comPayParam.setCouponTypeId(couponTypeId);
                    comPayParam.setCouponAmount(couponPayAmount);
                }
                PayOrderDTO payOrderDTO = comPayFacade.requestDefaultComPay(comPayParam);
                payOrderId = payOrderDTO.getPayOrderId();
                payAmount = payOrderDTO.getPayAmount();
                payStatus = payOrderDTO.getStatus().getValue();
            } else {
                DefineComPayParam payParam = new DefineComPayParam();
                payParam.setMerchantId(storeOrderPayParam.getMerchantId());
                payParam.setStoreId(storeOrderPayParam.getStoreId());
                payParam.setTransOrderId(transOrderId);
                payParam.setUserId(userId);
                payParam.setSrc(src);
                payParam.setCurrencyId(currencyId);
                payParam.setTransInfo(transInfo);
                payParam.setClientType(storeOrderPayParam.getClientType());
                //外卖专用字段
                if (storeOrderPayParam.getDynamicPayMethod() > 0) {
                    List<StoreOrderVoucherPayParam> storeOrderVoucherPayParams = storeOrderPayParam.getStoreOrderVoucherPayParams();
                    if (storeOrderVoucherPayParams == null) {
                        throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "orderId[" + storeOrderPayParam.getOrderId() + "],waimai order must hava storeOrderVoucherPayParams");
                    }
                    payParam.setDynamicPayMethodEnum(DynamicPayMethodEnum.findByValue(storeOrderPayParam.getDynamicPayMethod()));
                    List<VoucherPayParam> voucherPayParams = BeanUtil.copyList(storeOrderVoucherPayParams, VoucherPayParam.class);
                    payParam.setVoucherPayParams(voucherPayParams);
                }
                this.buildDefineComPayParam(storeOrderPayParam.getOtherPayAmount(), storeOrder, storeOrderPayParam, payParam, false);
                //目前没有异步请求微信支付下单
                DefineComPayResult defineComPayResult = payFacade.requestDefineComPay(payParam);
                payOrderId = defineComPayResult.getPayOrderId();
                payAmount = defineComPayResult.getPayAmount();
                payStatus = defineComPayResult.getStatus();

                orderPayInfo = this.storeOrderService.buildWechatPayInfo(defineComPayResult, otherPayMode);
            }
        } catch (TPayException e) {
            String errorInfo = e.getMessage() + ", inputParams = " + storeOrderPayParam;
            log.error(errorInfo);
            throw new T5weiException(e.getErrorCode(), e.getMessage());
        } catch (TException e) {
            String errorInfo = e.getMessage() + ", inputParams = " + storeOrderPayParam;
            log.error("storeOrder pay failure:" + errorInfo);
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_FAILURE.getValue(), errorInfo);
        }
        storeOrderPay5weiParam.setPayOrderId(payOrderId);
        storeOrderPay5weiParam.setActualPrice(payAmount);
        StoreOrderDTO storeOrderDTO = this.buildStoreOrderDTO(payStatus, storeOrderPay5weiParam, otherPayMode, merchantId, storeId, orderId, payOrderId, userId, transOrderId, orderPayInfo);
        // 判断是不是充值卡组合业务
        if (storeOrder.isCombinedBuyPrepaidCard()) {
            this._setBuyPrepaidCardInfoDTO(merchantId, storeId, orderId, storeOrderDTO);
        }
        return storeOrderDTO;
    }

    /**
     * pad端请求m-pay，然后StoreOrderPayTask监听到m-pay的MQ消息后调用此接口，将5wei订单状态改为支付完成，完成库存扣减
     */
    @Override
    public StoreOrderDTO payStoreOrderFinish(StoreOrderPayFinishParam storeOrderPayFinishParam) throws TException {
        int merchantId = storeOrderPayFinishParam.getMerchantId();
        long storeId = storeOrderPayFinishParam.getStoreId();
        String orderId = storeOrderPayFinishParam.getOrderId();
        long userId = storeOrderPayFinishParam.getUserId();
        String payOrderId = storeOrderPayFinishParam.getPayOrderId();
        int actualCurrencyId = storeOrderPayFinishParam.getActualCurrencyId();
        long actualPrice = storeOrderPayFinishParam.getActualPrice();
        int payOrderStatus = storeOrderPayFinishParam.getPayOrderStatus();
        int errorCode = storeOrderPayFinishParam.getErrorCode();
        String errorMsg = storeOrderPayFinishParam.getErrorMsg();
        StoreOrderPay5weiParam storeOrderPay5weiParam = new StoreOrderPay5weiParam();
        storeOrderPay5weiParam.setMerchantId(merchantId);
        storeOrderPay5weiParam.setStoreId(storeId);
        storeOrderPay5weiParam.setOrderId(orderId);
        storeOrderPay5weiParam.setUserId(userId);
        storeOrderPay5weiParam.setPayOrderId(payOrderId);
        storeOrderPay5weiParam.setActualCurrencyId(actualCurrencyId);
        storeOrderPay5weiParam.setActualPrice(actualPrice);
        //支付完成
        StoreOrder payStoreOrder;
        if (payOrderStatus == PayOrderStatusDTO.FINISH.getValue()) {
        	// 支付结果
        	PayResultOfPayOrder payResult = payFacade.getPayResultOfPayOrder(payOrderId);
        	storeOrderPay5weiParam.setPayResult(payResult);
            // 处理订单支付结果
            Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
            storeOrderPay5weiParam.setStore5weiSetting(store5weiSetting);
            // 更新业务订单支付状态
            payStoreOrder = storeOrderService.payStoreOrder(storeOrderPay5weiParam);
            if (payStoreOrder.isSkipTakeCode()) {
                // 通知客户微信，已取餐
                wechatNotifyService.notifyOrderTakeCodeMsg(payStoreOrder);
                // 记录取餐票打印信息
                storeOrderHelper.insertCashierPrintOrder(payStoreOrder);
                // 客户取餐计入消费次数统计
                storeOrderHelper.accumulateStoreUserOrders(payStoreOrder);
                // 发送交互事件消息
                i5weiMessageProducer.sendMessageOfStoreOrderEvent(payStoreOrder, 0, TweetEventType.PAY_ORDER, "订单消费");
            } else {
                // 通知客户微信支付完成，请取餐
                wechatNotifyService.notifyOrderSuccessMsg(payStoreOrder);
            }
            //支付统计消息，用于创建支付明细
            i5weiMessageProducer.sendMessageOfStatStoreOrderPay(payStoreOrder, false);
            //用户下单记录
            i5weiMessageProducer.sendMessageOfStoreOrderVisit(merchantId, storeId, userId, UserVisitType.PLACE_ORDER);
            OrderPayFinishResult orderPayFinishResult = payStoreOrder.getOrderPayFinishResult();
            //小票打印
            this.printMessage(payStoreOrder);
        	if (orderPayFinishResult != null) {
        		if (orderPayFinishResult.isSendTableRecordAddDishMsg()) {
            		try {
        				wechatNotifyService.notifyTableRecordAddDishMsg(orderPayFinishResult.getStoreTableRecord(), orderPayFinishResult.getUserId());
        			} catch (Exception e) {
        				log.warn("send storeTableRecord["+orderPayFinishResult.getStoreTableRecord().getTableRecordId()+"] add dish msg to user["+payStoreOrder.getUserId()+"] fail");
        			}
            	}
            	if (orderPayFinishResult.isSettleMent()) {
            		List<StoreOrder> storeOrders = orderPayFinishResult.getStoreOrders();
                    //打印结账单
                    i5weiSettlementPrinter.sendPrintMessages(payStoreOrder,orderPayFinishResult.getStoreTableRecord());
            		i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
            	}
        	}
        	// 大单提醒
            wechatTempNotifyService.sendBigOrderAlarm(payStoreOrder);

            //支付完成之后，判断订单类型是否为自提点订单，若为自提点订单，发送消息队列信息，定时取号
            if (payStoreOrder.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue()) {
                this.sendPickupSiteOrderToAutoTakeCode(payStoreOrder);
            } else {
                // 定时取
                i5weiMessageProducer.sendTimingTakeCodeMessage(payStoreOrder, store5weiSetting.getTimingPrepareTime());
            }
        } else if (payOrderStatus == PayOrderStatusDTO.FAIL.getValue()) {
            String error = "errorCode=" + errorCode + ", errorMsg=" + errorMsg;
            payStoreOrder = storeOrderService.toPayStoreOrderException(merchantId, storeId, orderId, error);
            storeTableRecordService.orderPayFail(merchantId, storeId, orderId);
        } else {
            payStoreOrder = new StoreOrder();
        }
        if (payStoreOrder.getTableRecordId() <= 0) {
            if (payStoreOrder.getTradeStatus() > StoreOrderTradeStatusEnum.NOT.getValue()) {
                //发送入账信息
                i5weiMessageProducer.sendMessageOfMerchantAccounted(payStoreOrder);
                // 发送店铺统计消息
                i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(payStoreOrder);
            }
        } else {
            if (payStoreOrder.isPayFinish()) {
                // 发送入账信息
                i5weiMessageProducer.sendMessageOfMerchantAccounted(payStoreOrder);
                // 发送店铺统计消息
                i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(payStoreOrder);
            }
        }
        return storeOrderHelper.getStoreOrderDTOByEntity(payStoreOrder);
    }

    /**
     * 发送消息，设置自提点订单定时取号
     * @param storeOrder
     * @throws TException
     */
    private void sendPickupSiteOrderToAutoTakeCode(StoreOrder storeOrder) throws TException {
        StoreOrderTimingTakeCodeParam storeOrderTimingTakeCodeParam = new StoreOrderTimingTakeCodeParam();
        storeOrderTimingTakeCodeParam.setMerchantId(storeOrder.getMerchantId());
        storeOrderTimingTakeCodeParam.setStoreId(storeOrder.getStoreId());
        storeOrderTimingTakeCodeParam.setOrderId(storeOrder.getOrderId());
        storeOrderTimingTakeCodeParam.setClientType(storeOrder.getClientType());
        StoreOrderDelivery storeOrderDelivery = this.storeOrderService.getStoreOrderDelivery(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getOrderId());
        StorePickupSiteTimeSetting storePickupSiteTimeSetting = this.storePickupSiteTimeSettingService.getPickupSiteIdsByPickupSiteIdAndTimeBucketId(
                storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrderDelivery.getStorePickupSiteId(), storeOrder.getTimeBucketId());
        MutableDateTime mdt = new MutableDateTime(DateUtil.getBeginTime(storeOrder.getRepastDate(), null) + storePickupSiteTimeSetting.getOrderCutOffTime());
        mdt.addMinutes(5);
        storeOrderTimingTakeCodeParam.setTimingTakeTime(mdt.getMillis());
        this.timingTakeCodeStoreOrder(storeOrderTimingTakeCodeParam);
    }

    private void printMessage(StoreOrder payStoreOrder){
        int merchantId = payStoreOrder.getMerchantId();
        long storeId = payStoreOrder.getStoreId();
        String orderId = payStoreOrder.getOrderId();
        StoreTableSetting storeTableSetting = storeTableSettingService.getStoreTableSetting(storeId,merchantId,false);
        if (payStoreOrder.isSkipTakeCode()) {
            if(storeTableSetting.isEnableTableMode()){
                if(payStoreOrder.isTableRecordSubOrder()){
                    //: 自定义打印取餐单(桌台模式)
                    i5weiTakeCodePrinter.sendPrintMessages(payStoreOrder);
                }
            }else{
                //: 自定义打印取餐单(非桌台模式)
                i5weiOrderTakeCodePrinter.sendPrintMessages(payStoreOrder);
            }
            //打包清单
            i5weiTakeAndSendOutPrinter.sendPrintMessages(payStoreOrder);
            //后厨清单
            List<String> orderIds = new ArrayList<>();
            orderIds.add(orderId);
            i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,payStoreOrder.getTableRecordId(),orderIds,payStoreOrder.getStoreMealTakeups());
        }else{
            //非桌台模式
            if(!storeTableSetting.isEnableTableMode() && (payStoreOrder.getClientType() == ClientTypeEnum.DIAN_CAI_BAO.getValue() || payStoreOrder.getClientType() == ClientTypeEnum.CASHIER.getValue())){
                //自定义打印取餐单(非桌台模式)
                i5weiOrderTakeCodePrinter.sendPrintMessages(payStoreOrder);
                //打包清单
                i5weiTakeAndSendOutPrinter.sendPrintMessages(payStoreOrder);
                //:后厨清单
                List<String> orderIds = new ArrayList<>();
                orderIds.add(orderId);
                i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,payStoreOrder.getTableRecordId(),orderIds,payStoreOrder.getStoreMealTakeups());
            }
            //桌台模式
            if(storeTableSetting.isEnableTableMode()){
                //先付费
                if(!storeTableSetting.isEnablePayAfter()){
                    if(payStoreOrder.isTableRecordSubOrder()){
                        // 自定义打印点菜单（桌台模式）
                        i5weiTakeCodePrinter.sendPrintMessages(payStoreOrder);
                        // 后厨清单
                        List<String> orderIds = new ArrayList<>();
                        orderIds.add(orderId);
                        i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,payStoreOrder.getTableRecordId(),orderIds,payStoreOrder.getStoreMealTakeups());
                        // 打包清单
                        i5weiTakeAndSendOutPrinter.sendPrintMessages(payStoreOrder);
                    }
                }else{
                    //桌台模式后付费，顾客自助下单首单先付费
                    if(payStoreOrder.isTableRecordSubOrder()){
                        if(storeTableSetting.isEnableCustomerSelfOpenTablePayFirst() && !payStoreOrder.isEnableAddDishes()){
                            // 自定义打印点菜单（桌台模式）
                            i5weiTakeCodePrinter.sendPrintMessages(payStoreOrder);
                            // 后厨清单
                            List<String> orderIds = new ArrayList<>();
                            orderIds.add(orderId);
                            i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,payStoreOrder.getTableRecordId(),orderIds,payStoreOrder.getStoreMealTakeups());
                            // 打包清单
                            i5weiTakeAndSendOutPrinter.sendPrintMessages(payStoreOrder);
                        }
                    }
                }
                //非桌台记录子订单(外带下单)
                if(!payStoreOrder.isTableRecordSubOrder()){
                    if(payStoreOrder.getClientType() == ClientTypeEnum.CASHIER.getValue() || payStoreOrder.getClientType() == ClientTypeEnum.DIAN_CAI_BAO.getValue()){
                        // 打包清单
                        i5weiTakeAndSendOutPrinter.sendPrintMessages(payStoreOrder);
                    }
                }
            }
        }

    }

    @Override
    public StoreOrderDTO cancelPayStoreOrder(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = storeOrderService.cancelPayStoreOrder(merchantId, storeId, orderId);
        if (storeOrder.isTableRecordMasterOrder()) {
            try {
                storeTableRecordService.tableRecordSettleCancel(merchantId, storeId, storeOrder.getTableRecordId());
            } catch (Exception e) {
                log.warn("merchantId[" + merchantId + "], orderId[" + orderId + "] cancel fail", e);
            }
        }
        return BeanUtil.copy(storeOrder, StoreOrderDTO.class);
    }

    @Override
    public StoreOrderDTO storeOrderPaying(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = storeOrderService.storeOrderPaying(merchantId, storeId, orderId);
        return BeanUtil.copy(storeOrder, StoreOrderDTO.class);
    }

    @Override
    public StoreOrderDTO saveStoreOrderSiteNumber(int merchantId, long storeId, String orderId, int siteNumber) throws TException {
        if (orderId == null || orderId.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ID_CAN_NOT_NULL.getValue(), "store order_id can not null");
        }
        siteNumber = storeOrderFacadeValidate.validateSiteNumber(merchantId, storeId, orderId, siteNumber);
        StoreOrder storeOrder = storeOrderService.saveStoreOrderSiteNumber(merchantId, storeId, orderId, siteNumber);
        return BeanUtil.copy(storeOrder, StoreOrderDTO.class);
    }

    @Override
    public StoreOrderDTO takeCodeStoreOrder(StoreOrderTakeCodeParam storeOrderTakeCodeParam) throws TException {
        //返回：订单DTO+订单明细列表
        int merchantId = storeOrderTakeCodeParam.getMerchantId();
        long storeId = storeOrderTakeCodeParam.getStoreId();
        String orderId = storeOrderTakeCodeParam.getOrderId();
        StoreOrder storeOrder = storeOrderService.getStoreOrderDetailById(merchantId, storeId, orderId);
        if (storeOrderTakeCodeParam.getTakeMode() == null || storeOrderTakeCodeParam.getTakeMode().getValue() == 0) {
            if (storeOrder.getTakeMode() != StoreOrderTakeModeEnum.UNKNOWN.getValue()) {
                storeOrderTakeCodeParam.setTakeMode(StoreOrderTakeModeEnum.findByValue(storeOrder.getTakeMode()));
            } else {
                storeOrderTakeCodeParam.setTakeMode(StoreOrderTakeModeEnum.DINE_IN);// 未知取餐方式则默认为堂食
            }
        }
        // 判断营业时段
        long timeBucketId = storeOrder.getTimeBucketId();
        StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId, true);
        int clientType = storeOrderTakeCodeParam.getClientType();
        if (clientType != ClientTypeEnum.CASHIER.getValue()) {
            //是否限制取餐时间
            if (storeOrder.isLimitMealTime()) {
                //add by lizhijun
                storeOrderFacadeValidate.checkStoreOrderTimeBucket(merchantId, storeId, storeOrder.getRepastDate(), storeTimeBucket, storeOrder.getTakeMode());
            }
        }
        // 执行取餐
        StoreOrder takeCodeStoreOrder = storeOrderService.takeCodeStoreOrder(storeOrderTakeCodeParam);
        BeanUtil.copy(takeCodeStoreOrder, storeOrder);
        storeOrder.setPlaceOrderTime(takeCodeStoreOrder.getCreateTime());
        storeOrder.setTakeSerialTime(System.currentTimeMillis());
        // 记录取餐票打印信息
        if (clientType != ClientTypeEnum.CASHIER.getValue()) {
            storeOrderHelper.insertCashierPrintOrder(takeCodeStoreOrder);
        }
        //手机取餐，pad对预定订单取餐
        // (自定义)取餐票
        i5weiOrderTakeCodePrinter.sendPrintMessages(storeOrder);
        //（自定义 + 打包台）打包清单
        i5weiTakeAndSendOutPrinter.sendPrintMessages(storeOrder);
        // 后厨清单
        List<String> orderIds = new ArrayList<>();
        orderIds.add(orderId);
        i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,storeOrder.getTableRecordId(),orderIds,storeOrder.getStoreMealTakeups());
        // 客户取餐计入消费次数统计
        storeOrderHelper.accumulateStoreUserOrders(storeOrder);
        // 发送交互事件消息
        i5weiMessageProducer.sendMessageOfStoreOrderEvent(storeOrder, 0, TweetEventType.PAY_ORDER, "订单消费");
        // 更改统计信息为订单交易中
        i5weiMessageProducer.sendMessageOfStatStoreOrderPay(storeOrder, true);
        if (storeOrder.isPayFinish()) {
            // 发送入账通知
            i5weiMessageProducer.sendMessageOfMerchantAccounted(storeOrder);
            // 发送统计通知
            i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(storeOrder);
        }
	    // 给客户发微信取餐通知
	    wechatNotifyService.notifyOrderTakeCodeMsg(takeCodeStoreOrder);
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        storeOrderDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
        if (clientType == ClientTypeEnum.CASHIER.getValue()) {
            storeOrderHelper.setPayResultInfo(storeOrderDTO);
        }
        return storeOrderDTO;
    }

    @Override
    public StoreOrderDTO timingTakeCodeStoreOrder(StoreOrderTimingTakeCodeParam storeOrderTimingTakeCodeParam) throws T5weiException, TException {
        int merchantId = storeOrderTimingTakeCodeParam.getMerchantId();
        long storeId = storeOrderTimingTakeCodeParam.getStoreId();
        Store5weiSetting store5weiSetting = store5weiSettingService.getStore5weiSettingByStoreId(merchantId, storeId);
        StoreOrder diffNomalOrPickupSiteOrder = storeOrderService.getStoreOrderById(merchantId, storeId, storeOrderTimingTakeCodeParam.getOrderId());
        if (!store5weiSetting.isTimingTake() && !(diffNomalOrPickupSiteOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue() &&
                diffNomalOrPickupSiteOrder.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue())) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_TIMING_TAKE_NOT_OPENED.getValue(), DataUtil.infoWithParams("store order timing take not opened, merchantId=#1, storeId=#2", new Object[]{merchantId, storeId}));
        }
        StoreOrder storeOrder = storeOrderService.timingTakeCodeStoreOrder(storeOrderTimingTakeCodeParam);// 设置定时取时间
        i5weiMessageProducer.sendTimingTakeCodeMessage(storeOrder, store5weiSetting.getTimingPrepareTime());// 发送定时取消息
        return storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
    }

    /**
     * 这个接口目前使用场景:
     * 1.用户手机回0退款
     * 2.微信订单里面退款
     * 3.旧版ipad预订订单取消
     * 考虑到chenge需要这个接口的返回值,就没有把微信退款替换到StoreOrderRefundFacade.orderRefund()
     */
    @Override
    public StoreOrderDTO refundStoreOrder(StoreOrderRefundParam storeOrderRefundParam) throws TException {
        int merchantId = storeOrderRefundParam.getMerchantId();
        long storeId = storeOrderRefundParam.getStoreId();
        String orderId = storeOrderRefundParam.getOrderId();
        long staffId = storeOrderRefundParam.getStaffId();
        StoreOrder storeOrder = storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        //退款校验
        storeOrderFacadeValidate.refundStoreOrderValidate(storeOrderRefundParam, storeOrder);
        //未交易订单，用户可以直接操作退款
        if (storeOrder.getPayablePrice() == 0) {
            StoreOrder refundOrder = storeOrderService.refundStoreOrder(staffId, storeOrderRefundParam, 0);
            return storeOrderHelper.getStoreOrderDTOByEntity(refundOrder);
        }
        long userId = storeOrder.getUserId();
        String payOrderId = storeOrder.getPayOrderId();
        if (payOrderId == null || payOrderId.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                    DataUtil.infoWithParams("storeId=#1, orderId=#2, userId=#3, payOrderId is null ", new Object[]{storeId, orderId, userId}));
        }
        long refundRecordId = 0L;
        RefundResultDTO refundResultDTO = null;
        try {
            if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue()) {
                // 预定订单取消
                refundResultDTO = refundFacade.requestRefund4PreOrder(payOrderId, merchantId);
            } else {
                // 其他订单
                refundResultDTO = refundFacade.requestRefund4Default(payOrderId, merchantId);
            }
        } catch (TPayException e) {
            if (e.getErrorCode() == PayErrorCode.PAY_ORDER_REFUND.getValue()) {
                if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue()) {
                    // 向统计模块发送消费消息
                    i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(storeOrder);
                }
                refundRecordId = refundFacade.getRefundDetailByPayOrderId(payOrderId).getRefundRecordIds().get(0);
            } else {
                storeOrderService.refundStoreOrderFailure(staffId, storeOrderRefundParam, e.getMessage());
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                        "m-pay errorCode=" + e.getErrorCode() + "," + DataUtil.infoWithParams(e.getMessage() + ", storeId=#1, orderId=#2, userId=#3 ", new Object[]{storeId, orderId, userId}));
            }
        } catch (TException e) {
            storeOrderService.refundStoreOrderFailure(staffId, storeOrderRefundParam, e.getMessage());
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                    "m-pay," + DataUtil.infoWithParams(e.getMessage() + ", storeId=#1, orderId=#2, userId=#3 ", new Object[]{storeId, orderId, userId}));
        }
        //如果上述异常，不会执行下面的if代码
        if (refundResultDTO != null && refundResultDTO.getRefundRecord() != null) {
            RefundRecordDTO refundRecordDTO = refundResultDTO.getRefundRecord();
            refundRecordId = refundRecordDTO.getRefundRecordId();
        }
        //更新storeOrder信息，创建storeOrder操作日志
        StoreOrder refundOrder = this.storeOrderRefundService.refundStoreOrderWrapper(storeOrderRefundParam, refundResultDTO);
        // 更新库存
        try {
            storeInventoryService.updateInventoryDateByOrder(storeOrder);
        } catch (Throwable e) {
            log.error("#### fail to updateInventoryByOrder ", e);
        }
        // 事件消息
        int isAuto = 0;
        int clientType = storeOrderRefundParam.getClientType();
        if (clientType == ClientTypeEnum.CASHIER.getValue() && staffId > 0) {
            i5weiMessageProducer.sendMessageOfStoreOrderEvent(refundOrder, staffId, TweetEventType.CANCEL_ORDER, "取消订单");
        } else if (clientType == ClientTypeEnum.CASHIER.getValue() && staffId == 0) {
            isAuto = 1;
        }
        if (refundOrder.getUserId() > 0) {
            //不在发送原路退回链接,第三方支付原路退回
            wechatNotifyService.notifyAutoOrderRefundMsg(refundOrder, isAuto, StoreOrderRefundVersion.VERSION_REFUND_TO_ORICHANNEL.getValue());//通知用户
        }
        // 统计消息
        i5weiMessageProducer.sendMessageOfStatStoreOrderRefund(refundOrder, staffId, refundRecordId, true);
        if (storeOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue()) {
            // 如果当前订单为预定订单，在5wei的交易订单退款完成后向统计模块发送消费消息
            i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(storeOrder);
        }
        // 向店铺统计发送退款消息
        i5weiMessageProducer.sendRefundMessageOfStoreStatistics(refundRecordId);

        this.storeOrderRefundService.finishStoreOrderRefundRecord(storeOrderRefundParam.getMerchantId(), storeOrderRefundParam.getStoreId(), refundRecordId, false);

        // 返回：订单DTO+订单明细列表
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(refundOrder);
        this.setUserAccountRemainInfo(storeOrderDTO, 0);
        storeOrderHelper.setRefundResultInfo(storeOrderDTO, refundResultDTO);
        return storeOrderDTO;
    }

    /**
     * 自定义退款,该接口准备废弃,不在维护,关于自定义退款都使用StoreOrderRefundFacade <br>
     * 目前这个接口在新版pad中没有使用,旧版ipad如果没有更新版本,会继续使用该接口<br>
     * 新版ipad在订单管理里面对订单自定义退款,调用的退款接口都挪到了StoreOrderRefundFacade里面了
     */
    @Override
    public StoreOrderDTO refundStoreOrderMode(StoreOrderRefundModeParam storeOrderRefundModeParam) throws TException {
        int merchantId = storeOrderRefundModeParam.getMerchantId();
        long storeId = storeOrderRefundModeParam.getStoreId();
        String orderId = storeOrderRefundModeParam.getOrderId();
        long staffId = storeOrderRefundModeParam.getStaffId();
        StoreOrder storeOrder = storeOrderService.getStoreOrderById4Update(merchantId, storeId, orderId);
        //退款校验
        storeOrderFacadeValidate.refundPartStoreOrder(storeOrder);
        if (storeOrder.getPayablePrice() == 0) {
            StoreOrder refundOrder = storeOrderService.refundStoreOrder(storeOrder, staffId, StoreOrderRefundStatusEnum.MERCHANT_ALL, 0);
            return storeOrderHelper.getStoreOrderDTOByEntity(refundOrder);
        }
        String payOrderId = storeOrder.getPayOrderId();
        if (payOrderId == null || payOrderId.isEmpty()) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                    DataUtil.infoWithParams("storeId=#1, orderId=#2, payOrderId is null ", new Object[]{storeId, orderId}));
        }
        boolean voucherRefund = storeOrderRefundModeParam.isVoucherRefund();
        boolean couponRefund = storeOrderRefundModeParam.isCouponRefund();
        long prepaidcardRefund = storeOrderRefundModeParam.getPrepaidCardRefundAmount();
        long userAccountRefund = storeOrderRefundModeParam.getAccountRefundAmount();
        long cashRefund = storeOrderRefundModeParam.getCashierRefundAmount();

        // 请求m-pay进行退款
        RefundParam refundParam = new RefundParam();
        refundParam.setVoucherRefund(voucherRefund);
        refundParam.setCouponRefund(couponRefund);
        refundParam.setPrepaidcardRefund(prepaidcardRefund);
        refundParam.setUserAccountRefund(userAccountRefund);
        refundParam.setCashRefund(cashRefund);
        RefundResultDTO refundResultDTO;
        try {
            refundResultDTO = refundFacade.requestRefund4Define(payOrderId, merchantId, refundParam);
        } catch (TPayException e) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_REFUND_FAILURE.getValue(),
                    "m-pay errorCode=" + e.getErrorCode() + "," + DataUtil.infoWithParams(e.getMessage() + ", storeId=#1, orderId=#2", new Object[]{storeId, orderId}));
        }
        PayOrderDTO payOrderDTO = refundResultDTO.getPayOrder();
        RefundRecordDTO refundRecordDTO = refundResultDTO.getRefundRecord();

        //默认部分退款
        StoreOrderRefundStatusEnum merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_PART;
        //如果PayOrderDTO是全额退款，修改为商户全额退款
        if (payOrderDTO.getStatus().equals(PayOrderStatusDTO.FULL_REFUND)) {
            merchantRefund = StoreOrderRefundStatusEnum.MERCHANT_ALL;
        }

        long refundRecordId = refundRecordDTO.getRefundRecordId();
        long refundAmount = payOrderDTO.getRefundAmount();
        //生成订单操作日志
//        StoreOrder refundOrder = storeOrderService.refundStoreOrder(storeOrder, staffId, merchantRefund, refundRecordId);
        StoreOrder refundOrder = this.storeOrderRefundService.refundStoreOrderWrapper4Mode(storeOrderRefundModeParam, storeOrder, merchantRefund, refundResultDTO);

        // 统计消息
        i5weiMessageProducer.sendMessageOfStatStoreOrderRefund(refundOrder, staffId, refundRecordId, false);

        // 事件消息
        i5weiMessageProducer.sendMessageOfStoreOrderEvent(refundOrder, staffId, TweetEventType.REFUND_ORDER, "订单退款");

        // 退款完成向店铺统计发送退款消息
        i5weiMessageProducer.sendRefundMessageOfStoreStatistics(refundRecordId);

        // 完成该次退款业务
        this.storeOrderRefundService.finishStoreOrderRefundRecord(storeOrderRefundModeParam.getMerchantId(), storeOrderRefundModeParam.getStoreId(), refundRecordId, false);

        StoreOrderDTO dto = storeOrderHelper.getStoreOrderDTOByEntity(refundOrder);
        Map<String, String> orderRefundInfo = new HashMap<>();
        orderRefundInfo.put("refund_record_id", refundRecordId + "");
        orderRefundInfo.put("refund_amount", refundAmount + "");
        dto.setOrderRefundInfo(orderRefundInfo);
        return dto;
    }

    @Override
    public void makeDeliveryOrderPreparing(int merchantId, long storeId, List<String> orderIds) throws TException {
        List<StoreOrder> list = this.storeOrderDeliveryService.makeDeliveryOrderPreparing(merchantId, storeId, orderIds);
        this.wechatNotifyService.sendWecaht4Delivery(merchantId, storeId, 0L, list, false);
        for (StoreOrder storeOrder : list) {
            // 更改统计信息为订单交易中
            i5weiMessageProducer.sendMessageOfStatStoreOrderPay(storeOrder, true);
            // 已交易订单发送商户入账通知
            i5weiMessageProducer.sendMessageOfMerchantAccounted(storeOrder);
            // 已交易订单发送店铺统计通知
            i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(storeOrder);
            // 记录取餐票打印信息
            storeOrderHelper.accumulateStoreUserOrders(storeOrder);
            // 高级模式需要打印外卖清单和自定义打印
            i5weiTakeAndSendOutPrinter.sendPrintMessages(storeOrder);
        }
    }

    @Override
    public void makeDeliveryOrderDelivering(int merchantId, long storeId, List<String> orderIds, long staffId, long staffUserId) throws TException {
        OpDeliveryingResult opDeliveryingResult = this.storeOrderDeliveryService.makeDeliveryOrderDelivering(merchantId, storeId, orderIds, staffId, staffUserId);
        List<StoreOrder> storeOrders = opDeliveryingResult.getStoreOrders();
        //如果是美团外卖订单，edit by Jemon 20161027
        Map<String, StoreOrderDelivery> storeOrderDeliveryMap = this.storeOrderDeliveryDAO.getMapInIds(merchantId, storeId, orderIds, false);
        List<Long> meituanOrderIds = new ArrayList<Long>();
        for (StoreOrder storeOrder : storeOrders) {
        	if(storeOrder.isWaimaiOrder()){//外卖订单
	        	StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryMap.get(storeOrder.getOrderId());
	        	if (storeOrderDelivery != null && storeOrderDelivery.isStoreShipping()) {
					meituanOrderIds.add(Long.parseLong(storeOrderDelivery.getWaimaiOrderId()));
	        	}
        	}
        }
        if(!meituanOrderIds.isEmpty()){
        	UserDTO userDTO = this.userFacade.getUserByUserId(opDeliveryingResult.getDeliverStaffUserId());
        	try {
	        	SelfDeliveryMeituanOrderParam selfDeliveryParam = new SelfDeliveryMeituanOrderParam();
				selfDeliveryParam.setMerchantId(merchantId);
				selfDeliveryParam.setStoreId(storeId);
				selfDeliveryParam.setShipperName(userDTO.getName());
				selfDeliveryParam.setShipperPhone(userDTO.getMobile());
				selfDeliveryParam.setMeituanOrderIds(meituanOrderIds);
				this.storeMeituanOrderFacade.selfDelivery(selfDeliveryParam);

        	} catch (Throwable t) {
				log.error("shangjia self begin delivering , tell meituan fail.");
			}
        }
        //配送数量增加自提点订单数量
        int pickupSiteOrderTotal = this.storeOrderDeliveryService.countPickupSiteOrderInDelivering(merchantId, storeId, staffId);
        opDeliveryingResult.setTotal(pickupSiteOrderTotal + opDeliveryingResult.getTotal());
        //以上是处理自配送
        if(!storeOrders.get(0).isWaimaiOrder()){//非外卖类型 edit by Jemon
	        wechatNotifyService.sendWecaht4Delivery(merchantId, storeId, staffId, opDeliveryingResult.getStoreOrders(), true);
	        wechatNotifyService.sendMsgToStaffForDelivering(opDeliveryingResult);
        } else if(storeOrders.get(0).getStoreOrderDelivery() != null) {
            StoreOrderDelivery storeOrderDelivery = storeOrders.get(0).getStoreOrderDelivery();
            if (storeOrderDelivery.isStoreShipping()) {
                wechatNotifyService.sendMsgToStaffForDelivering(opDeliveryingResult);
            }
        }
    }

    /**
     * 更改自提点订单状态为配送中
     * @param merchantId
     * @param storeId
     * @param pickupSiteIds
     * @param staffId
     * @param staffUserId
     * @throws T5weiException
     * @throws TException
     */
    @Override
    public void makePickupSiteOrderDelivering(int merchantId, long storeId, List<Long> pickupSiteIds, long staffId, long staffUserId) throws T5weiException, TException {
        StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
        pickupSiteOrderQueryParam.setMerchantId(merchantId);
        pickupSiteOrderQueryParam.setStoreId(storeId);
        pickupSiteOrderQueryParam.setPickupSiteIds(pickupSiteIds);
        pickupSiteOrderQueryParam.setDeliveryStatus(StoreOrderDeliveryStatusEnum.PREPARE_FINISH.getValue());
        pickupSiteOrderQueryParam.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
        pickupSiteOrderQueryParam.setContainPickupSiteInfo(true);
        pickupSiteOrderQueryParam.setContainDeliveryInfo(true);

        List<String> orderIds = this.storeOrderDeliveryService.getOrderIdsByPickupSiteIds(pickupSiteOrderQueryParam);

        if (CollectionUtils.isNotEmpty(orderIds)) {
            OpDeliveryingResult opDeliveryingResult = this.storeOrderDeliveryService.makeDeliveryOrderDelivering(merchantId, storeId, orderIds, staffId, staffUserId);
            if(opDeliveryingResult != null) {
                //发送微信消息到用户，提醒用户，订单已配送
                wechatNotifyService.sendWecaht4Delivery(merchantId, storeId, 0, opDeliveryingResult.getStoreOrders(), false);
                int pickupSiteOrderTotal = this.storeOrderDeliveryService.countPickupSiteOrderInDelivering(merchantId, storeId, staffId);
                opDeliveryingResult.setTotal(opDeliveryingResult.getTotal() + pickupSiteOrderTotal);
                opDeliveryingResult.setNewAdd(pickupSiteIds.size());
                //发送微信消息提醒配送员配送订单
                wechatNotifyService.sendMsgToStaffForDelivering(opDeliveryingResult);
            }
        }
    }

    @Override
    public void makeDeliveryOrderDeliveryFinish(int merchantId, long storeId, List<String> orderIds) throws TException {
        List<StoreOrder> list = this.storeOrderDeliveryService.makeDeliveryOrderDeliveryFinish(merchantId, storeId, orderIds);
        for(StoreOrder storeOrder:list){
        	StoreOrderDelivery storeOrderDelivery = storeOrder.getStoreOrderDelivery();
			if (storeOrderDelivery == null) {
				continue;
			}
			try {
				// 如果是美团外卖订单,并且是自配送 edit by Jemon 20161027
				if (storeOrderDelivery.getWaimaiType() > 0 && storeOrderDelivery.isStoreShipping()) {
						long meituanOrderId = Long.parseLong(storeOrderDelivery.getWaimaiOrderId());
						this.storeMeituanOrderFacade.finishOrder(merchantId, storeId, meituanOrderId);
				 }
			} catch (Throwable t) {
				log.error("shangjia self delivery finish ,tell meituan finish fail.");
			}
        }
    }

    @Override
    public void makePickupSiteOrderFinished(int merchantId, long storeId, List<Long> pickupSiteIds) throws T5weiException, TException {
        StorePickupSiteOrderQueryParam pickupSiteOrderQueryParam = new StorePickupSiteOrderQueryParam();
        pickupSiteOrderQueryParam.setMerchantId(merchantId);
        pickupSiteOrderQueryParam.setStoreId(storeId);
        pickupSiteOrderQueryParam.setPickupSiteIds(pickupSiteIds);
        pickupSiteOrderQueryParam.setDeliveryStatus(StoreOrderDeliveryStatusEnum.DELIVERING.getValue());
        pickupSiteOrderQueryParam.setWaimaiType(WaimaiTypeEnum.PICKUPSITE.getValue());
        pickupSiteOrderQueryParam.setContainDeliveryInfo(true);
        pickupSiteOrderQueryParam.setContainPickupSiteInfo(true);
        List<String> orderIds = this.storeOrderDeliveryService.getOrderIdsByPickupSiteIds(pickupSiteOrderQueryParam);
        if (CollectionUtils.isNotEmpty(orderIds)) {
            this.storeOrderDeliveryService.makeDeliveryOrderDeliveryFinish(merchantId, storeId, orderIds);
        }
    }

    @Override
    public StoreOrderDeliveryInfoDTO getStoreOrderDeliveryInfo(int merchantId, long storeId) throws TException {
        StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting4Read(merchantId, storeId);
        return this.storeOrderDeliveryService.getStoreOrderDeliveryInfo(merchantId, storeId, storeDeliverySetting);
    }

    @Override
    public StoreOrderPageDTO getStoreDeliveryOrders(int merchantId, long storeId, StoreOrderDeliveryQueryParam queryParam, int page, int size) throws TException {
        PageResult pageResult = this.storeOrderDeliveryService.getStoreDeliveryOrders(merchantId,
                storeId, queryParam, page, size, true);
        StoreOrderPageDTO storeOrderPageDTO = new StoreOrderPageDTO();
        storeOrderPageDTO.setSize(size);
        storeOrderPageDTO.setPageNo(page);
        storeOrderPageDTO.setTotal(pageResult.getTotal());
        storeOrderPageDTO.setPageNum(pageResult.getTotalPage());
        List<StoreOrder> storeOrders = pageResult.getList();
        List<StoreOrderDTO> storeOrderDTOs = storeOrderHelper.getStoreOrderDTOByEntity(storeOrders);
        //分类封装，edit by Jemon 20161027
        List<StoreOrderDTO> gzhStoreOrderDTOs = new ArrayList<StoreOrderDTO>();
        List<StoreOrderDTO> meituanStoreOrderDTOs = new ArrayList<StoreOrderDTO>();
        List<StoreOrderDTO> elmeStoreOrderDTOs = new ArrayList<StoreOrderDTO>();
        List<StoreOrderDTO> baiduStoreOrderDTOs = new ArrayList<StoreOrderDTO>();
        for (StoreOrderDTO storeOrderDTO : storeOrderDTOs) {
            int waimaiType = storeOrderDTO.getWaimaiType();
            if (waimaiType == 0) {
                gzhStoreOrderDTOs.add(storeOrderDTO);
            } else if (waimaiType == 1) {
                meituanStoreOrderDTOs.add(storeOrderDTO);
            } else if (waimaiType == 2) {
                elmeStoreOrderDTOs.add(storeOrderDTO);
            } else if (waimaiType == 3) {
                baiduStoreOrderDTOs.add(storeOrderDTO);
            }
        }
        storeOrderPageDTO.setDataList(storeOrderDTOs);//仍然存放总的外卖订单，供非等待配送状态使用
        storeOrderPageDTO.setMeituanDataList(meituanStoreOrderDTOs);
        storeOrderPageDTO.setElmeDataList(elmeStoreOrderDTOs);
        storeOrderPageDTO.setBaiduDataList(baiduStoreOrderDTOs);
        return storeOrderPageDTO;
    }

    @Override
    public void updateOrderDelivery(StoreOrderDeliveryParam storeOrderDeliveryParam) throws TException {
        int merchantId = storeOrderDeliveryParam.getMerchantId();
        long storeId = storeOrderDeliveryParam.getStoreId();
        StoreOrder storeOrder = this.storeOrderService.getStoreOrderById(merchantId, storeId, storeOrderDeliveryParam.getOrderId());
        //验证参数的完整性
        this.storeOrderFacadeValidate.validateDelivery(storeOrderDeliveryParam, storeOrder);
        UserInvoiceDTO userInvoiceDTO = null;
        if (storeOrderDeliveryParam.getInvoiceId() > 0) {
            try {
                userInvoiceDTO = this.userInvoiceFacadeIface.getUserInvoiceByInvoiceId(storeOrder.getUserId(), storeOrderDeliveryParam.getInvoiceId());
            } catch (TUserException e) {
                storeOrderDeliveryParam.setInvoiceId(0);
            }
            try {
                this.userInvoiceFacadeIface.updateUserInvoiceLastUsedTime(storeOrder.getUserId(), storeOrderDeliveryParam.getInvoiceId());
            } catch (TException e) {
            }
        }

        StoreDeliverySetting storeDeliverySetting = this.storeDeliverySettingService.getStoreDeliverySetting(merchantId, storeId);

        MerchantSettingDTO merchantSetting = this.merchantSettingFacade.getMerchantSetting(merchantId);
        MerchantDeliveryModeEnum deliveryMode = MerchantDeliveryModeEnum.findByValue(merchantSetting.getDeliveryMode().getValue());
        //楼宇模式
        if (deliveryMode == null || deliveryMode == MerchantDeliveryModeEnum.NO_DISTANCE) {//为了保证兼容性
            StoreDeliveryBuilding storeDeliveryBuilding = this.storeDeliveryBuildingService.loadStoreDeliveryBuilding(merchantId, storeId, storeOrderDeliveryParam.getBuildingId());
            UserDeliveryAddress userDeliveryAddress = this.userDeliveryAddressService.loadUserDeliveryAddressByAddressId(storeOrder.getUserId(), storeOrderDeliveryParam.getAddressId());
            this.userDeliveryAddressService.updateUsrDeliveryAddressLastUsedTime(storeOrder.getUserId(), storeOrderDeliveryParam.getAddressId());
            try {
                this.storeOrderDeliveryService.updateOrderForNoDistanceDelivery(storeOrder, storeOrderDeliveryParam, userInvoiceDTO, storeDeliveryBuilding, userDeliveryAddress, storeDeliverySetting);
            } catch (DuplicateKeyException e) {
                this.storeOrderDeliveryService.updateOrderForNoDistanceDelivery(storeOrder, storeOrderDeliveryParam, userInvoiceDTO, storeDeliveryBuilding, userDeliveryAddress, storeDeliverySetting);
            }
        }

        //距离模式
        if (deliveryMode == MerchantDeliveryModeEnum.DISTINCE) {
            if (storeOrderDeliveryParam.getAddressId() <= 0) {
                throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_ADDRESS_INVALID.getValue(), "user's delivery user address is null");
            }
            MerchantUserDeliveryAddress merchantUserDeliveryAddress = this.merchantUserDeliveryAddressService.getMerchantUserDeliveryAddressById(merchantId, storeId, storeOrderDeliveryParam.getAddressId(), true);
            if (merchantUserDeliveryAddress == null) {
                throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_ADDRESS_INVALID.getValue(), "user's delivery user address is null");
            }
            try {
                this.storeOrderDeliveryService.updateOrderForDistanceDelivery(storeOrder, storeOrderDeliveryParam, userInvoiceDTO, storeDeliverySetting, merchantUserDeliveryAddress);
            } catch (DuplicateKeyException e) {
                this.storeOrderDeliveryService.updateOrderForDistanceDelivery(storeOrder, storeOrderDeliveryParam, userInvoiceDTO, storeDeliverySetting, merchantUserDeliveryAddress);
            }
        }
    }

    @Override
    public StoreOrderDTO getStoreOrderStatusById(int merchantId, long storeId, String orderId) throws TException {
        StoreOrder storeOrder = this.storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        if (storeOrder.getPayStatus() == StoreOrderPayStatusEnum.FINISH.getValue()) {
            StoreOrderDTO storeOrderDTO = new StoreOrderDTO();
            BeanUtil.copy(storeOrder, storeOrderDTO);
            return storeOrderDTO;
        }
        if (storeOrder.getPayStatus() != StoreOrderPayStatusEnum.FINISH.getValue()) {
            //查组合任务
            StoreOrderCombinedBiz storeOrderCombinedBiz = storeOrderCombinedBizService.getByOrderIdAndType(merchantId, storeId, orderId, StoreOrderCombinedBizType.BUY_PREPAIDCARD_PAY_STORE_ORDER.getValue(), false);
            if (storeOrderCombinedBiz != null && storeOrderCombinedBiz.isFail()) {
                //充值卡交易失败
                StoreOrderDTO storeOrderDTO = new StoreOrderDTO();
                BeanUtil.copy(storeOrder, storeOrderDTO);
                Map<String, String> orderPayInfo = new HashMap<>();
                orderPayInfo.put("errorCode", String.valueOf(storeOrderCombinedBiz.getErrorCode()));
                orderPayInfo.put("errorMsg", storeOrderCombinedBiz.getErrorMsg());
                storeOrderDTO.setOrderPayInfo(orderPayInfo);
                //设置交易订单的支付状态是:支付失败
                storeOrderDTO.setPayStatus(StoreOrderPayStatusEnum.FAILURE.getValue());
                return storeOrderDTO;
            }
            String payOrderId = storeOrder.getPayOrderId();
            if (StringUtils.isNotEmpty(payOrderId)) {
                PayOrderDTO payOrderDTO = payQueryFacade.getPayOrderByTransOrderId(orderId, PaySrcEnum.M_5WEI.getValue());
                int status = payOrderDTO.getStatus().getValue();
                if (status == PayOrderStatusDTO.FAIL.getValue()) {
                    storeOrder = storeOrderService.toPayStoreOrderException(merchantId, storeId, orderId, "");
                    StoreOrderDTO storeOrderDTO = BeanUtil.copy(storeOrder, StoreOrderDTO.class);
                    Map<String, String> orderPayInfo = new HashMap<>();
                    orderPayInfo.put("errorCode", String.valueOf(payOrderDTO.getErrorCode()));
                    orderPayInfo.put("errorMsg", payOrderDTO.getErrorMsg());
                    storeOrderDTO.setOrderPayInfo(orderPayInfo);
                    return storeOrderDTO;
                } else if (status == PayOrderStatusDTO.FINISH.getValue()) {
                    StoreOrderPay5weiParam storeOrderPay5weiParam = new StoreOrderPay5weiParam();
                    storeOrderPay5weiParam.setMerchantId(merchantId);
                    storeOrderPay5weiParam.setStoreId(storeId);
                    storeOrderPay5weiParam.setOrderId(orderId);
                    storeOrderPay5weiParam.setPayOrderId(payOrderDTO.getPayOrderId());
                    storeOrderPay5weiParam.setUserId(payOrderDTO.getUserId());
                    storeOrder = storeOrderService.payStoreOrder(storeOrderPay5weiParam);
                    // 若为桌台后付费判断是否为主订单支付完成，用于结账
                    //OrderPayFinishResult orderPayFinishResult = storeTableRecordService.orderPayFinish(merchantId, storeId, orderId);
                    OrderPayFinishResult orderPayFinishResult = storeOrder.getOrderPayFinishResult();
                    if (orderPayFinishResult != null) {
                        if (orderPayFinishResult.isSendTableRecordAddDishMsg()) {
                            try {
                                wechatNotifyService.notifyTableRecordAddDishMsg(orderPayFinishResult.getStoreTableRecord(), orderPayFinishResult.getUserId());
                            } catch (Exception e) {
                                log.warn("send storeTableRecord["+orderPayFinishResult.getStoreTableRecord().getTableRecordId()+"] add dish msg to user["+storeOrder.getUserId()+"] fail");
                            }
                        }
                        if (orderPayFinishResult.isSettleMent()) {
                            List<StoreOrder> storeOrders = orderPayFinishResult.getStoreOrders();
                            i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
                            //打印结账单
                            i5weiSettlementPrinter.sendPrintMessages(storeOrder, orderPayFinishResult.getStoreTableRecord());
                        }
                    }
                    //后厨清单
                    List<String> orderIds = new ArrayList<>();
                    orderIds.add(storeOrder.getOrderId());
                    i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,storeOrder.getTableRecordId(),orderIds,storeOrder.getStoreMealTakeups());
                }
            }
        }
        return BeanUtil.copy(storeOrder, StoreOrderDTO.class);
    }

    @Override
    public List<StoreOrderDTO> getStoreOrdersRollback(int merchantId, long storeId) throws TException {
        List<StoreOrder> orders = storeOrderQueryService.getStoreOrdersRollback(merchantId, storeId);
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        return BeanUtil.copyList(orders, StoreOrderDTO.class);
    }

    @Override
    public void changeOrdersUser(int merchantId, long storeId, long srcUserId, long destUserId) throws TException {
        this.storeOrderService.changeOrdersUser(merchantId, storeId, srcUserId, destUserId);
    }

    @Override
    public void checkChangeStoreOrderTakeMode(int merchantId, long storeId, String orderId, int toTakeMode) throws TException {
        storeOrderFacadeValidate.validateChangeStoreOrderTakeMode(merchantId, storeId, orderId, toTakeMode);
    }

    @Override
    public StoreOrderDTO changeStoreOrderTakeMode(int merchantId, long storeId, String orderId, int takeMode, int clientType) throws TException {
        StoreOrder storeOrder;
        try {
            storeOrder = storeOrderService.changeStoreOrderTakeMode(merchantId, storeId, orderId, takeMode, clientType);
        } catch (DuplicateKeyException e) {
            storeOrder = storeOrderService.changeStoreOrderTakeMode(merchantId, storeId, orderId, takeMode, clientType);
        }
        return storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
    }

    @Override
    public boolean isAddDishStoreOrder(int merchantId, long storeId, long repastDate, long timeBucketId, long userId, int siteNumber) throws TException {
        int dishStoreOrders = storeOrderService.countDishStoreOrder(merchantId, storeId, repastDate, timeBucketId, userId, siteNumber);
        return dishStoreOrders > 0;
    }

    @Override
    public StoreOrderDTO updateStoreOrderCustomerTraffic(int merchantId, long storeId, String orderId, int customerTraffic, long staffId) throws TException {
        StoreOrder storeOrder = storeOrderService.updateStoreOrderCustomerTraffic(merchantId, storeId, orderId, customerTraffic, staffId);
        return storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
    }

    @Override
    public void makeDeliveryOrderPreparingAuto() throws TException {
        List<StoreOrderDeliveryPreparingResult> deliveryPreparingResults = storeOrderDeliveryService.makeDeliveryOrderPreparingAuto();
        for (StoreOrderDeliveryPreparingResult storeOrderDeliveryPreparingResult : deliveryPreparingResults) {
            int merchantId = storeOrderDeliveryPreparingResult.getMerchantId();
            long storeId = storeOrderDeliveryPreparingResult.getStoreId();
            List<String> deliveryOrderIds = storeOrderDeliveryPreparingResult.getDeliveryOrderIds();
            this.makeDeliveryOrderPreparing(merchantId, storeId, deliveryOrderIds);
        }
    }

    /**
     * 组建StoreOrderDTO
     */
    private StoreOrderDTO buildStoreOrderDTO(int payStatus, StoreOrderPay5weiParam storeOrderPay5weiParam,
                                             StoreOrderPayModeEnum otherPayMode, int merchantId, long storeId, String orderId,
                                             String payOrderId, long userId, String transOrderId, Map<String, String> orderPayInfo) throws TException {
        StoreOrder payStoreOrder;
        if (PayOrderStatusDTO.PAYING.getValue() == payStatus) {
            //支付中
            if (!otherPayMode.equals(StoreOrderPayModeEnum.WECHART_PAY)) {
                payStoreOrder = storeOrderService.toPayStoreOrder(merchantId, storeId, orderId, payOrderId, userId);
            } else {
                payStoreOrder = storeOrderService.recordWechatPayStoreOrder(merchantId, storeId, transOrderId, payOrderId, userId);
            }
        } else {
            //支付完成
            payStoreOrder = storeOrderService.payStoreOrder(storeOrderPay5weiParam);
            OrderPayFinishResult orderPayFinishResult = payStoreOrder.getOrderPayFinishResult();
            if (orderPayFinishResult != null) {
        		if (orderPayFinishResult.isSendTableRecordAddDishMsg()) {
            		try {
        				wechatNotifyService.notifyTableRecordAddDishMsg(orderPayFinishResult.getStoreTableRecord(), orderPayFinishResult.getUserId());
        			} catch (Exception e) {
        				log.warn("send storeTableRecord["+orderPayFinishResult.getStoreTableRecord().getTableRecordId()+"] add dish msg to user["+payStoreOrder.getUserId()+"] fail");
        			}
            	}
            	if (orderPayFinishResult.isSettleMent()) {
            		List<StoreOrder> storeOrders = orderPayFinishResult.getStoreOrders();
            		i5weiMessageProducer.sendMessageOfStatTableRecordOrder(storeOrders);
            		//打印结账单
                    i5weiSettlementPrinter.sendPrintMessages(payStoreOrder,orderPayFinishResult.getStoreTableRecord());
            	}
        	}
            //后厨清单
            List<String> orderIds = new ArrayList<>();
            orderIds.add(payStoreOrder.getOrderId());
            i5weiKitchenMealListPrinter.sendPrintMessages(merchantId,storeId,payStoreOrder.getTableRecordId(),orderIds,payStoreOrder.getStoreMealTakeups());

        }
        StoreOrderDTO resultDTO = storeOrderHelper.getStoreOrderDTOByEntity(payStoreOrder);
        if (!orderPayInfo.isEmpty()) {
            resultDTO.setOrderPayInfo(orderPayInfo);
        }
        return resultDTO;
    }

    /**
     * 设置买卡信息
     */
    private void _setBuyPrepaidCardInfoDTO(int merchantId, long storeId, String orderId, StoreOrderDTO storeOrderDTO) throws TException {
        StoreOrderCombinedBiz storeOrderCombinedBiz = storeOrderCombinedBizService.getByOrderIdAndType(merchantId, storeId, orderId, StoreOrderCombinedBizType.BUY_PREPAIDCARD_PAY_STORE_ORDER.getValue(), false);
        if (storeOrderCombinedBiz != null) {
            MerchantPrepaidCardOrderDTO merchantPrepaidCardOrder = this.prepaidCardFacade.getMerchantPrepaidCardOrder(storeOrderCombinedBiz.getBizId(), storeOrderCombinedBiz.getMerchantId());
            BuyPrepaidCardInfoDTO buyPrepaidCardInfoDTO = new BuyPrepaidCardInfoDTO();
            buyPrepaidCardInfoDTO.setFaceValue(merchantPrepaidCardOrder.getFaceValue());
            buyPrepaidCardInfoDTO.setPrice(merchantPrepaidCardOrder.getPrice());
            storeOrderDTO.setBuyPrepaidCardInfoDTO(buyPrepaidCardInfoDTO);
        }
    }

    /**
     * 组建自定义组合支付参数
     */
    private void buildDefineComPayParam(long otherPayAmount, StoreOrder storeOrder, StoreOrderPayParam storeOrderPayParam, DefineComPayParam payParam, boolean isCombinedTask) throws T5weiException {
        StoreOrderPayModeEnum otherPayMode = storeOrderPayParam.getOtherPayMode();
        long payablePrice = storeOrder.getPayablePrice();

        long prepaidCardPayAmount = 0;
        long userAccountPayAmount = 0;
        long couponPayAmount = 0;
        long voucherPayAmount = 0;
        //买充值卡支付,不需要设置预付费卡支付,用户账户支付,优惠券支付参数
        if (!isCombinedTask) {
            //预付费卡支付
            prepaidCardPayAmount = storeOrderPayParam.getPrepaidCardPayAmount();
            if (prepaidCardPayAmount > 0) {
                PrepaidCardPayParam prepaidCardPayParam = new PrepaidCardPayParam();
                prepaidCardPayParam.setAmount(Integer.valueOf(prepaidCardPayAmount + ""));
                payParam.setPrepaidCardPayParam(prepaidCardPayParam);
            }
            //用户账户支付
            userAccountPayAmount = storeOrderPayParam.getUserAccountPayAmount();
            if (userAccountPayAmount > 0) {
                UserAccountPayParam userAccountPayParam = new UserAccountPayParam();
                userAccountPayParam.setAmount(Integer.valueOf(userAccountPayAmount + ""));
                payParam.setUserAccountPayParam(userAccountPayParam);
            }

            //第三方自定义券支付
            if(storeOrderPayParam.getStoreOrderVoucherPayParams() != null){
                for(StoreOrderVoucherPayParam param : storeOrderPayParam.getStoreOrderVoucherPayParams()){
                    voucherPayAmount += param.getAmount();
                }
            }

            //优惠券支付
            couponPayAmount = storeOrderPayParam.getCouponPayAmount();
            long couponTypeId = storeOrderPayParam.getCouponTypeId();
            if (couponPayAmount > 0) {
                long couponAmount = payablePrice - (prepaidCardPayAmount + userAccountPayAmount + otherPayAmount + voucherPayAmount);
                if (couponPayAmount > couponAmount) {
                    couponPayAmount = couponAmount;
                }
                CouponPayParam couponPayParam = new CouponPayParam();
                couponPayParam.setCouponTypeId(couponTypeId);
                couponPayParam.setValue(Integer.valueOf(couponPayAmount + ""));
                payParam.setCouponPayParam(couponPayParam);
            }
        }

        //其他支付方式
        if (otherPayAmount > 0) {
            Map<String, String> otherPayParams = storeOrderPayParam.getOtherPayParams();
            if (otherPayParams == null) {
                throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "输入参数不完整");
            }
            if (otherPayMode.equals(StoreOrderPayModeEnum.BOUND_CARD_PAY)) {
                //绑卡支付
                String bindId = otherPayParams.get("bindId");
                if (bindId == null || bindId.isEmpty()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "输入参数不完整");
                }
                BindPayParam bindPayParam = new BindPayParam();
                bindPayParam.setBindId(bindId);
                bindPayParam.setAmount(Integer.valueOf(otherPayAmount + ""));
                payParam.setBindPayParam(bindPayParam);
            } else if (otherPayMode.equals(StoreOrderPayModeEnum.YJ_CREDIT_CARD_PAY)) {
                //YJPay信用卡支付
                String cardNo = otherPayParams.get("cardNo");
                String cvv2 = otherPayParams.get("cvv2");
                String validthru = otherPayParams.get("validthru");
                String mobile = otherPayParams.get("mobile");
                String smsCode = otherPayParams.get("smsCode");
                if (cardNo == null || cardNo.isEmpty() || cvv2 == null || cvv2.isEmpty()
                        || validthru == null || validthru.isEmpty() || mobile == null || mobile.isEmpty() || smsCode == null || smsCode.isEmpty()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "输入参数不完整");
                }
                CreditCardPayParam creditCardPayParam = new CreditCardPayParam();
                creditCardPayParam.setCardNo(cardNo);
                creditCardPayParam.setCvv2(cvv2);
                creditCardPayParam.setValidthru(validthru);
                creditCardPayParam.setMobile(mobile);
                creditCardPayParam.setAmount(Integer.valueOf(otherPayAmount + ""));
                creditCardPayParam.setSmsCode(smsCode);
                payParam.setCreditCardPayParam(creditCardPayParam);
            } else if (otherPayMode.equals(StoreOrderPayModeEnum.YJ_DEBIT_CARD_PAY)) {
                //YJPay借记卡支付
                String cardNo = otherPayParams.get("cardNo");
                String owner = otherPayParams.get("owner");
                String idCard = otherPayParams.get("idCard");
                String mobile = otherPayParams.get("mobile");
                String smsCode = otherPayParams.get("smsCode");
                if (cardNo == null || cardNo.isEmpty() || owner == null || owner.isEmpty()
                        || idCard == null || idCard.isEmpty() || mobile == null || mobile.isEmpty() || smsCode == null || smsCode.isEmpty()) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "输入参数不完整");
                }
                DebitCardPayParam debitCardPayParam = new DebitCardPayParam();
                debitCardPayParam.setCardNo(cardNo);
                debitCardPayParam.setOwner(owner);
                debitCardPayParam.setIdCard(idCard);
                debitCardPayParam.setMobile(mobile);
                debitCardPayParam.setSmsCode(smsCode);
                debitCardPayParam.setAmount(Integer.valueOf(otherPayAmount + ""));
                payParam.setDebitCardPayParam(debitCardPayParam);
            } else if (otherPayMode.equals(StoreOrderPayModeEnum.WECHART_PAY)) {
                //微信统一下单支付
                String productId = otherPayParams.get("productId");
                Integer tradeType = Integer.valueOf(otherPayParams.get("tradeType"));
                WechatTradeTypeDTO wechatTradeType = WechatTradeTypeDTO.findByValue(tradeType);
                if (tradeType == 0 || wechatTradeType == null) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_INPUT_PARAM_INCOMPLETE.getValue(), "输入参数不完整");
                }
                UnifiedorderPayParam unifiedorderPayParam = new UnifiedorderPayParam();
                if (productId != null) {
                    unifiedorderPayParam.setProductId(productId);
                }
                unifiedorderPayParam.setTradeType(wechatTradeType);
                unifiedorderPayParam.setAmount(Integer.valueOf(otherPayAmount + ""));
                payParam.setUnifiedorderPayParam(unifiedorderPayParam);
            } else {
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_PAY_FAILURE.getValue(),
                        DataUtil.infoWithParams("pay storeOrder input param incomplete, otherPayMode unknown," + JsonUtil.build(otherPayParams) + ",storeId=#1, orderId=#2, userId=#3 ", new Object[]{storeOrderPayParam.getStoreId(), storeOrderPayParam.getOrderId(), storeOrderPayParam.getUserId()}));
            }
        }

        if (!isCombinedTask) {
            long payParamPrice = prepaidCardPayAmount + userAccountPayAmount + couponPayAmount + otherPayAmount + voucherPayAmount;
            if (payablePrice != payParamPrice) {
                throw new T5weiException(
                        T5weiErrorCodeType.STORE_ORDER_PRICE_NOT_EQUAL_INPUT.getValue(), DataUtil.infoWithParams(
                        "storeId=#1, orderId=#2, userId=#3 : orderPayablePrice[" + payablePrice + "]!= payParamPrice[" + payParamPrice + "]", new Object[]{storeOrderPayParam.getStoreId(), storeOrderPayParam.getOrderId(), storeOrderPayParam.getUserId()}));
            }
        }
    }

    @Override
    public StoreOrderDTO getStoreOrderDetailByTakeSerialNumber(int merchantId, long storeId, long repastDate, int takeSerialNumber) throws TException {
        long repastDateTime = DateUtil.getBeginTime(repastDate, null);
        StoreOrder storeOrder = storeOrderQueryService.getStoreOrderDetailByTakeSerialNumber(merchantId, storeId, repastDateTime, takeSerialNumber);
        return buildStoreOrderDTO(merchantId, storeId, storeOrder);
    }

    /**
     * 小程序接单接口
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param orderId    订单id
     *                   <p>
     *                   throw T5weiException
     *                   T5weiErrorCodeType.STORE_ORDER_NOT_EXIST <br>
     *                   T5weiErrorCodeType.STORE_ORDER_ClIENT_TYPE_ERROR <br>
     */
    @Override
    public void makeOrderTradeStatusFinish(int merchantId, long storeId, String orderId) throws T5weiException, TException {
        StoreOrder storeOrder = this.storeOrderService.getStoreOrderById(merchantId, storeId, orderId);
        if(storeOrder.getClientType() != ClientTypeEnum.MINA.getValue()){
            throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ClIENT_TYPE_ERROR.getValue(), " order client type error ");
        }
        this.storeOrderService.updateOrderTradeFinish(storeOrder);
    }

    /**
     * 修改用户备注
     * @param merchantId
     * @param storeId
     * @param orderId
     * @param userRemark
     * @return
     * @throws T5weiException
     */
    @Override
    public StoreOrderDTO updateUserRemark (int merchantId, long storeId, String orderId, String userRemark) throws T5weiException {
    	if (merchantId <= 0) {
    		throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "param [merchantId="+merchantId+"] invalid");
    	}
    	if (storeId <= 0) {
    		throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "param [storeId="+storeId+"] invalid");
    	}
    	if (StringUtils.isEmpty(orderId)) {
    		throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ID_CAN_NOT_NULL.getValue(), "param [orderId="+orderId+"] invalid");
    	}
    	if (StringUtils.isEmpty(userRemark) || userRemark.length()>150) {
    		throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "param [userRemark="+userRemark+"] invalid");
    	}
    	StoreOrder storeOrder = this.storeOrderService.updateUserRemark(merchantId, storeId, orderId, userRemark);
    	return buildStoreOrderDTO(merchantId, storeId, storeOrder);
    }

    private StoreOrderDTO buildStoreOrderDTO(int merchantId, long storeId, StoreOrder storeOrder) throws T5weiException {
        long timeBucketId = storeOrder.getTimeBucketId();
        StoreTimeBucket storeTimeBucket = storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId, true);
        StoreOrderDTO storeOrderDTO = storeOrderHelper.getStoreOrderDTOByEntity(storeOrder);
        storeOrderDTO.setStoreTimeBucketDTO(BeanUtil.copy(storeTimeBucket, StoreTimeBucketDTO.class));
        return storeOrderDTO;
    }
}
