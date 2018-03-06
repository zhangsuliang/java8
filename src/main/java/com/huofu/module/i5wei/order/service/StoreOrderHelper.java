package com.huofu.module.i5wei.order.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.menu.entity.StoreChargeSubitem;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.order.dao.*;
import com.huofu.module.i5wei.order.entity.*;
import com.huofu.module.i5wei.pickupsite.service.StorePickupSiteOrderService;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import com.huofu.module.i5wei.promotion.service.StorePromotionGratisService;
import com.huofu.module.i5wei.promotion.service.StorePromotionRebateService;
import com.huofu.module.i5wei.promotion.service.StorePromotionReduceService;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;
import com.huofu.module.i5wei.request.entity.Store5weiRequestBizType;
import com.huofu.module.i5wei.request.service.Store5weiRequestParam;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import com.huofu.module.i5wei.table.dao.TableRecordBatchRefundRecordDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.entity.TableRecordBatchRefundRecord;
import com.huofu.module.i5wei.table.service.StoreAreaService;
import com.huofu.module.i5wei.table.service.StoreTableService;
import com.huofu.module.i5wei.wechat.WechatNotifyService;
import halo.query.dal.DALInfo;
import halo.query.dal.DALParserUtil;
import halo.query.mapping.EntityTableInfo;
import halo.query.mapping.EntityTableInfoFactory;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.coupon.Coupon4CommonFacade;
import huofucore.facade.dialog.tweet.TweetEventType;
import huofucore.facade.dialog.visit.StoreUserVisitDTO;
import huofucore.facade.dialog.visit.StoreUserVisitFacade;
import huofucore.facade.dialog.visit.StoreUserVisitOrderParam;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.i5wei.pickupsite.StorePickupSiteBaseDTO;
import huofucore.facade.i5wei.sharedto.I5weiUserDTO;
import huofucore.facade.i5wei.sharedto.StoreTableStaffDTO;
import huofucore.facade.merchant.cashier.StoreCashierAutoPrintParam;
import huofucore.facade.merchant.cashier.StoreCashierDTO;
import huofucore.facade.merchant.cashier.StoreCashierFacade;
import huofucore.facade.merchant.staff.StaffDTO;
import huofucore.facade.merchant.staff.StaffFacade;
import huofucore.facade.merchant.store.StoreAutoPrinterCashierFacade;
import huofucore.facade.pay.payment.*;
import huofucore.facade.prepaidcard.PrepaidCardFacade;
import huofucore.facade.user.account.UserAccountFacade;
import huofucore.facade.user.info.UserDTO;
import huofucore.facade.user.info.UserFacade;
import huofucore.facade.waimai.meituan.order.StoreMeituanOrderFacade;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.MoneyUtil;
import huofuhelper.util.NumberUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thrift.ThriftClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class StoreOrderHelper {

    private static final Log log = LogFactory.getLog(StoreOrderHelper.class);
    
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("sysconfig");
    
    @Autowired
	private StoreOrderItemDAO storeOrderItemDAO;
    
    @Autowired
	private StoreOrderItemPromotionDAO storeOrderItemPromotionDAO;
    
	@Autowired
	private StoreOrderSubitemDAO storeOrderSubitemDAO;
	
	@Autowired
	private StoreOrderOptlogDAO storeOrderOptlogDAO;
	
	@Autowired
	private StoreOrderDeliveryDAO storeOrderDeliveryDAO;
	
	@Autowired
	private StoreOrderRefundItemDAO storeOrderRefundItemDAO;
	
	@Autowired
	private StoreTableRecordDAO storeTableRecordDAO;
	
	@Autowired
	private StoreOrderInvoiceDAO storeOrderInvoiceDAO;
	
	@Autowired
	private StoreTimeBucketService storeTimeBucketService;
    
    @ThriftClient
	private StaffFacade.Iface staffFacade;
    
	@ThriftClient
	private UserFacade.Iface userFacade;

    @Autowired
    private StoreChargeItemService storeChargeItemService;

    @Autowired
    private StoreTableService storeTableService;
    
    @Autowired
    private StoreAreaService storeAreaService;
    
    @Autowired
    private StoreStampTakemealService storeStampTakemealService;

    @ThriftClient
    private StoreAutoPrinterCashierFacade.Iface storeAutoPrinterCashierFacade;

    @ThriftClient
    private PrepaidCardFacade.Iface prepaidCardFacade;

    @ThriftClient
    private UserAccountFacade.Iface userAccountFacade;
    
    @ThriftClient
    private PayFacade.Iface payFacade;
    
    @ThriftClient
    private RefundFacade.Iface refundFacade;

    @ThriftClient
    private Coupon4CommonFacade.Iface coupon4CommonFacade;

    @Autowired
    private StoreInventoryService storeInventoryService;

    @Autowired
    private I5weiMessageProducer i5weiMessageProducer;

    @Autowired
    private WechatNotifyService wechatNotifyService;
    
    @Autowired
    private StoreOrderPriceHelper storeOrderPriceHelper;
    
    @ThriftClient
    private OriChannelRefundFacade.Iface oriChannelRefundFacade;
    
    @ThriftClient
    private StoreUserVisitFacade.Iface storeUserVisitFacade;

    @Resource
    private StoreOrderDAO storeOrderDAO;

    @Resource
    private TableRecordBatchRefundRecordDAO tableRecordBatchRefundRecordDAO;
    
    @Autowired
    private StorePromotionRebateService storePromotionRebateService;
    
    @Autowired
    private StorePromotionReduceService storePromotionReduceService;
    
    @ThriftClient
    private StoreMeituanOrderFacade.Iface storeMeituanOrderFacade;

    @ThriftClient
    private StoreCashierFacade.Iface storeCashierFacade;

    @Autowired
    private StorePickupSiteOrderService storePickupSiteOrderService;

    @Autowired
    private StorePromotionGratisService storePromotionGratisService;

    public Store5weiRequestParam getStore5weiRequestParam(StoreOrder storeOrder){
        if (DataUtil.isEmpty(storeOrder.getRequestId())) {

            return null;
		}
    	Store5weiRequestParam param = new Store5weiRequestParam();
    	param.setRequestId(storeOrder.getRequestId());
    	param.setMerchantId(storeOrder.getMerchantId());
    	param.setStoreId(storeOrder.getStoreId());
    	param.setI5weiBizType(Store5weiRequestBizType.ORDER.getValue());
    	param.setI5weiBizId(storeOrder.getOrderId());
    	return param;
    }
    
	/**
     * 设置用户订单支付信息
     *
     * @param storeOrderDTO
     * @throws org.apache.thrift.TException
     */
    public void setPayResultInfo(StoreOrderDTO storeOrderDTO) throws TException {
    	PayResultOfPayOrder payResult = null;
		if (storeOrderDTO.getCreditStatus() == StoreOrderCreditStatusEnum.NO_CREDIT.getValue()){
			String payOrderId = storeOrderDTO.getPayOrderId();
			if (DataUtil.isNotEmpty(payOrderId)) {
	        	payResult = payFacade.getPayResultOfPayOrder(payOrderId);
		        long cashReceivedAmount = payResult.getCashReceivedAmount();
		        long cashAmount = payResult.getCashAmount();
		        long couponAmount = payResult.getCouponAmount();
		        long prepaidcardAmount = payResult.getPrepaidcardAmount();
		        long userAccountAmount = payResult.getUserAccountAmount();
		        long aliPayAmount = payResult.getAliPayAmount();
		        long wechatAmount = payResult.getWechatAmount();
		        long iposAmount = payResult.getIposAmount();
		        long yjpayAmount = payResult.getYjpayAmount();
		        long posAmount = payResult.getPosAmount();
		        long publicTransferAmount = payResult.getPublicTransferAmount();
		        long iBoxPayAmount = payResult.getIboxPayAmount();
		        Map<String, String> orderPayInfo = new HashMap<String, String>();
		        orderPayInfo.put("cash_received_amount", cashReceivedAmount + "");
		        orderPayInfo.put("cash_amount", cashAmount + "");
		        orderPayInfo.put("coupon_amount", couponAmount + "");
		        orderPayInfo.put("prepaid_card_amount", prepaidcardAmount + "");
		        orderPayInfo.put("user_account_amount", userAccountAmount + "");
		        orderPayInfo.put("alipay_amount", aliPayAmount + "");
		        orderPayInfo.put("wechat_amount", wechatAmount + "");
		        orderPayInfo.put("ipos_amount", iposAmount + "");
		        orderPayInfo.put("yjpay_amount", yjpayAmount + "");
		        orderPayInfo.put("pos_amount", posAmount + "");
		        orderPayInfo.put("public_transfer_amount", publicTransferAmount + "");
		        orderPayInfo.put("iboxpay_amount", iBoxPayAmount + "");
		        List<PayResultOfDynamicPayMethod> payResultOfDynamicPayMethodList = payResult.getPayResultOfDynamicPayMethodList();
		        if (payResultOfDynamicPayMethodList != null) {
		            PayResultOfDynamicPayMethod dynamicPayMethod = payResultOfDynamicPayMethodList.get(0);
		            if (dynamicPayMethod != null) {
		                orderPayInfo.put("dynamic_pay_method_id", dynamicPayMethod.getDynamicPayMethodId() + "");
		                orderPayInfo.put("dynamic_pay_method_name", dynamicPayMethod.getDynamicPayMethodName());
		                orderPayInfo.put("dynamic_pay_amount", dynamicPayMethod.getAmount() + "");
		                orderPayInfo.put("dynamic_pay_actual_amount", dynamicPayMethod.getActualAmount() + "");
		            }
                    //多张自定义券,收银台组合支付适用 为了兼容旧版ipad,上面的代码保留 add by wxy 20161111
                    orderPayInfo.put("pay_result_of_dynamic", JsonUtil.build(buildVoucherPayMap(payResultOfDynamicPayMethodList)));
		        }
                storeOrderDTO.setOrderPayInfo(orderPayInfo);
	        }
		} else if (storeOrderDTO.getCreditStatus() == StoreOrderCreditStatusEnum.DISCHARGE.getValue()){
			Map<String, String> orderPayInfo = new HashMap<>();
			orderPayInfo.put("cash_received_amount", storeOrderDTO.getPayablePrice() + "");
			orderPayInfo.put("cash_amount", storeOrderDTO.getPayablePrice() + "");
			storeOrderDTO.setOrderPayInfo(orderPayInfo);
		}
		this.setStoreOrderActualPayInfo(storeOrderDTO, payResult);
    }

    private static List<Map<String, Object>> buildVoucherPayMap(List<PayResultOfDynamicPayMethod> payResultOfDynamicPayMethodList) {
        List<Map<String,Object>> voucherPayList = Lists.newArrayList();
        for(PayResultOfDynamicPayMethod payResultOfDynamicPayMethod : payResultOfDynamicPayMethodList){
            Map<String,Object> voucherPayInfoMap = Maps.newHashMap();
            voucherPayInfoMap.put("dynamic_pay_method_id", payResultOfDynamicPayMethod.getDynamicPayMethodId());
            voucherPayInfoMap.put("dynamic_pay_method_name", payResultOfDynamicPayMethod.getDynamicPayMethodName());
            voucherPayInfoMap.put("dynamic_pay_amount", payResultOfDynamicPayMethod.getAmount());
            voucherPayInfoMap.put("dynamic_pay_actual_amount", payResultOfDynamicPayMethod.getActualAmount());
            voucherPayList.add(voucherPayInfoMap);
        }
        return voucherPayList;
    }
    
    /**
	 * 计算订单的实际支付信息
	 *
	 * @param payResult
	 * @return
	 */
	public void setStoreOrderActualPayInfo(StoreOrderDTO storeOrderDTO, PayResultOfPayOrder payResult) {
		StoreOrder storeOrder = BeanUtil.copy(storeOrderDTO, StoreOrder.class);
		StoreOrderActualPayResult storeOrderActualPay = storeOrderPriceHelper.getStoreOrderActualPayInfo(storeOrder, payResult);
		StoreOrderActualPayDTO storeOrderActualPayDTO = BeanUtil.copy(storeOrderActualPay, StoreOrderActualPayDTO.class);
        storeOrderActualPayDTO.setStoreOrderPayResultOfDynamicPayMethods(storeOrderActualPay.getStoreOrderPayResultOfDynamicPayMethodList());
		storeOrderDTO.setStoreOrderActualPayDTO(storeOrderActualPayDTO);
	}
	
	public void setStoreOrderActualPayResult(StoreOrder storeOrder) throws TException{
		if (storeOrder == null) {
			return;
		}
		String payOrderId = storeOrder.getPayOrderId();
        if (payOrderId == null || payOrderId.isEmpty()) {
            return;
        }
        PayResultOfPayOrder payResult = payFacade.getPayResultOfPayOrder(payOrderId);
        StoreOrderActualPayResult storeOrderActualPay = storeOrderPriceHelper.getStoreOrderActualPayInfo(storeOrder, payResult);
        storeOrder.setStoreOrderActualPayResult(storeOrderActualPay);
	}
	
	public void setStoreOrderActualPayResult(List<StoreOrder> storeOrders) throws TException{
		if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		for (StoreOrder storeOrder : storeOrders){
			this.setStoreOrderActualPayResult(storeOrder);
		}
	}
	
	public void setStoreOrderActualPayResult(Map<String, StoreOrder> storeOrderMap) throws TException{
		if (storeOrderMap == null || storeOrderMap.isEmpty()) {
			return;
		}
		for (StoreOrder storeOrder : storeOrderMap.values()){
			this.setStoreOrderActualPayResult(storeOrder);
		}
	}
	
	public Map<String, StoreOrderDTO> getStoreOrderDTOMap(Map<String, StoreOrder> storeOrderMap) throws TException{
		Map<String, StoreOrderDTO> storeOrderDTOMap = Maps.newHashMap();
		if (storeOrderMap == null || storeOrderMap.isEmpty()) {
			return storeOrderDTOMap;
		}
		for (String orderId : storeOrderMap.keySet()){
			StoreOrder storeOrder = storeOrderMap.get(orderId);
			StoreOrderDTO storeOrderDTO = this.getStoreOrderDTOByEntity(storeOrder);
			storeOrderDTOMap.put(orderId, storeOrderDTO);
		}
		return storeOrderDTOMap;
	}
    
    /**
     * 设置用户订单退款信息
     *
     * @param storeOrderDTO
     * @throws org.apache.thrift.TException
     */
    public void setRefundResultInfo(StoreOrderDTO storeOrderDTO) throws TPayException, TException {
        if (storeOrderDTO.getRefundStatus() == 1) {
            return;
        }
        String payOrderId = storeOrderDTO.getPayOrderId();
        if (payOrderId == null || payOrderId.isEmpty()) {
            return;
        }
        RefundDetailDTO refundDetailDTO = refundFacade.getRefundDetailByPayOrderId(payOrderId);
        if (refundDetailDTO == null) {
            return;
        }
        Map<String, String> orderRefundInfo = this.getOrderRefundInfoMap(refundDetailDTO);
        storeOrderDTO.setOrderRefundInfo(orderRefundInfo);
    }

    /**
     * 设置退款信息
     *
     * @param storeOrderDTO
     * @throws org.apache.thrift.TException
     */
    public void setRefundResultInfo(StoreOrderDTO storeOrderDTO, RefundResultDTO refundResultDTO) throws TException {
        if (refundResultDTO == null) {
            return;
        }
        long refundRecordId = refundResultDTO.getRefundRecord().getRefundRecordId();
        if (refundRecordId <= 0) {
            return;
        }
        RefundDetailDTO refundDetailDTO = refundFacade.getRefundDetailByRefundRecordId(refundRecordId);
        if (refundDetailDTO == null) {
            return;
        }
        Map<String, String> orderRefundInfo = this.getOrderRefundInfoMap(refundDetailDTO);
        storeOrderDTO.setOrderRefundInfo(orderRefundInfo);
    }

    public Map<String, String> getOrderRefundInfoMap(RefundDetailDTO refundDetailDTO) {
        long amountRefund = refundDetailDTO.getAmountRefund();
        long cashRefund = refundDetailDTO.getCashRefund();
        long accountRefund = refundDetailDTO.getAccountRefund();
        long prepaidRefund = refundDetailDTO.getPrepaidRefund();
        long alipayRefund = refundDetailDTO.getAlipayRefund();
        long wechatRefund = refundDetailDTO.getWechatRefund();
        long yjRefund = refundDetailDTO.getYjRefund();
        long iposRefund = refundDetailDTO.getIposRefund();
        long couponRefund = refundDetailDTO.getCouponRefund();
        long voucherAmount = refundDetailDTO.getVoucherAmount();
        long voucherFaceValue = refundDetailDTO.getVoucherFaceValue();
        long voucherRefund = refundDetailDTO.getVoucherRefund();
        String voucherName = "";
        if (refundDetailDTO.getVoucherName() != null) {
            voucherName = refundDetailDTO.getVoucherName();
        }
        Map<String, String> orderRefundInfo = new HashMap<String, String>();
        orderRefundInfo.put("amount_refund", amountRefund + "");
        orderRefundInfo.put("cash_refund", cashRefund + "");
        orderRefundInfo.put("user_account_refund", accountRefund + "");
        orderRefundInfo.put("prepaid_card_refund", prepaidRefund + "");
        orderRefundInfo.put("alipay_refund", alipayRefund + "");
        orderRefundInfo.put("wechat_refund", wechatRefund + "");
        orderRefundInfo.put("yj_refund", yjRefund + "");
        orderRefundInfo.put("ipos_refund", iposRefund + "");
        orderRefundInfo.put("coupon_refund", couponRefund + "");
        orderRefundInfo.put("voucher_amount", voucherAmount + "");
        orderRefundInfo.put("voucher_face_value", voucherFaceValue + "");
        orderRefundInfo.put("voucher_refund", voucherRefund + "");
        orderRefundInfo.put("voucher_name", voucherName);
        //多张自定义券的处理
        orderRefundInfo.put("dynamic_pay_method_refund", JsonUtil.build(buildVoucherRefundMap(refundDetailDTO.getDynamicPayMethodRefundInfos()))); //add by wxy 20161111
        return orderRefundInfo;
    }

    private static List<Map<String, Object>> buildVoucherRefundMap(List<DynamicPayMethodRefundInfo> dynamicPayMethodRefundInfos) {
        List<Map<String, Object>> list = Lists.newArrayList();
        if (dynamicPayMethodRefundInfos != null) {
            for (DynamicPayMethodRefundInfo dynamicPayMethodRefundInfo : dynamicPayMethodRefundInfos) {
                Map<String, Object> map = Maps.newHashMap();
                map.put("actual_amount", dynamicPayMethodRefundInfo.getVoucherAmount());
                map.put("amount", dynamicPayMethodRefundInfo.getVoucherFaceValue());
                map.put("dynamic_refund_amount", dynamicPayMethodRefundInfo.getVoucherRefund());
                map.put("dynamic_pay_method_name", dynamicPayMethodRefundInfo.getVoucherName());
                list.add(map);
            }
        }
        return list;
    }


    public List<StoreOrderItem> getStoreOrderItem(String orderId, List<StoreChargeItem> orderChargeItems, List<StoreOrderPlaceItemParam> orderPlaceItems) throws T5weiException {
        if (orderChargeItems == null || orderChargeItems.isEmpty()) {
            return null;
        }
        if (orderPlaceItems == null || orderPlaceItems.isEmpty()) {
            return null;
        }
        Map<Long, StoreChargeItem> orderItemMap = this.getStoreChargeItemsMapOfStoreOrder(orderChargeItems);
        List<StoreOrderItem> list = new ArrayList<StoreOrderItem>();
        for (StoreOrderPlaceItemParam storeOrderPlaceItem : orderPlaceItems) {
            StoreChargeItem storeChargeItem = orderItemMap.get(storeOrderPlaceItem.getChargeItemId());
            if (storeOrderPlaceItem.getAmount() < storeOrderPlaceItem.getPackedAmount()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_ORDER_ITEM_PACKAGE_CAN_NOT_BIGGER_THEN_AMOUNT.getValue(),
                        DataUtil.infoWithParams("store order_item package can not bigger then amount, storeId=#1, orderId=#2, chargeItemId＝#3 ",
                                new Object[]{storeChargeItem.getStoreId(), orderId, storeChargeItem.getChargeItemId()}));
            }
            StoreOrderItem item = new StoreOrderItem();
            item.setOrderId(orderId);
            item.setMerchantId(storeChargeItem.getMerchantId());
            item.setStoreId(storeChargeItem.getStoreId());
            item.setChargeItemId(storeChargeItem.getChargeItemId());
            item.setChargeItemName(storeChargeItem.getName());
            item.setRebateAble(storeChargeItem.isEnableRebate());
            item.setAmount(storeOrderPlaceItem.getAmount());
            item.setGratisAmount(storeOrderPlaceItem.getGratisAmount());
            item.setPackedAmount(storeOrderPlaceItem.getPackedAmount());
            //增加打包费
            item.setPackagePrice(storeChargeItem.getPackagePrice());
            //是否支持优惠券支付
            item.setCouponSupported(storeChargeItem.isCouponSupported());
            //辣度
            item.setSpicyLevel(storeChargeItem.getSpicyLevel());
            //入客数
            item.setEnableCustomerTraffic(storeChargeItem.isEnableCustomerTraffic());
            String unit = storeChargeItem.getUnit();
            if (unit == null || unit.isEmpty()) {
                item.setUnit("份");
            } else {
                item.setUnit(storeChargeItem.getUnit());
            }
            item.setPrice(storeChargeItem.getCurPrice());
            item.setMemberPrice(storeChargeItem.getMemberPrice());
            item.setUpdateTime(Calendar.getInstance().getTimeInMillis());
            item.setCreateTime(Calendar.getInstance().getTimeInMillis());
            //设置了外卖平台参数则认为是外卖订单
            if(storeOrderPlaceItem.isSetWaimaiPlaceItemParam()){
            	StoreOrderWaimaiPlaceItemParam waimaiPlaceItemParam = storeOrderPlaceItem.getWaimaiPlaceItemParam();
            	item.setPrice(waimaiPlaceItemParam.getPrice());
            	//FIXME chenkai 餐盒费是否需要保存？
            }
            item.setWeightEnabled(storeChargeItem.isWeightEnabled());
            list.add(item);
            if (log.isDebugEnabled()) {
                log.debug(storeChargeItem.getStoreId() + "-" + orderId + "-" + storeChargeItem.getChargeItemId());
            }
        }
        return list;
    }

    public List<StoreOrderSubitem> getStoreOrderSubItem(String orderId, long repastDatetime, long timeBucketId, List<StoreChargeItem> orderChargeItems, List<StoreOrderPlaceItemParam> orderPlaceItems) {
        if (orderChargeItems == null || orderChargeItems.isEmpty()) {
            return null;
        }
        Map<Long, StoreOrderPlaceItemParam> orderItemMap = this.getChargeItemsMapOfStoreOrder(orderPlaceItems);
        List<StoreOrderSubitem> list = new ArrayList<StoreOrderSubitem>();
        for (StoreChargeItem storeChargeItem : orderChargeItems) {
            if (storeChargeItem == null) {
                continue;
            }
            List<StoreChargeSubitem> storeChargeSubitemList = storeChargeItem.getStoreChargeSubitems();
            if (storeChargeSubitemList == null || storeChargeSubitemList.isEmpty()) {
                continue;
            }
            StoreOrderPlaceItemParam storeOrderPlaceItemParam = orderItemMap.get(storeChargeItem.getChargeItemId());
            String waimaiFoodProperty = null;
            if(storeOrderPlaceItemParam.isSetWaimaiPlaceItemParam()){
            	//外卖平台菜品备注
            	waimaiFoodProperty = storeOrderPlaceItemParam.getWaimaiPlaceItemParam().getFoodProperty();
            }
            double orderItemNum = storeOrderPlaceItemParam.getAmount();
            Map<Long, String> productRemarkMap = storeOrderPlaceItemParam.getSubItemRemark();
            if (productRemarkMap == null) {
                productRemarkMap = new HashMap<Long, String>();
            }
            for (StoreChargeSubitem storeChargeSubitem : storeChargeSubitemList) {
                if (storeChargeSubitem == null) {
                    continue;
                }
                StoreOrderSubitem item = new StoreOrderSubitem();
                item.setOrderId(orderId);
                item.setMerchantId(storeChargeSubitem.getMerchantId());
                item.setStoreId(storeChargeSubitem.getStoreId());
                item.setRepastDate(repastDatetime);
                item.setTimeBucketId(timeBucketId);
                item.setChargeItemId(storeChargeSubitem.getChargeItemId());
                item.setProductId(storeChargeSubitem.getProductId());
                item.setAmount(storeChargeSubitem.getAmount());
                item.setAmountOrder(NumberUtil.mul(storeChargeSubitem.getAmount(), orderItemNum));
                item.setProductName(storeChargeSubitem.getStoreProduct().getName());
                if (storeChargeItem.isWeightEnabled()) {
                    item.setUnit(storeChargeItem.getUnit());
                } else {
                    item.setUnit(storeChargeSubitem.getStoreProduct().getUnit());
                }
                item.setRemark(productRemarkMap.getOrDefault(storeChargeSubitem.getProductId(), ""));
				if (waimaiFoodProperty != null){
					item.setRemark(waimaiFoodProperty);//外卖平台菜品备注
                }
                item.setPrimeCost(storeChargeSubitem.getStoreProduct().getPrimeCost());
                item.setUpdateTime(Calendar.getInstance().getTimeInMillis());
                item.setCreateTime(Calendar.getInstance().getTimeInMillis());
                list.add(item);
                if (log.isDebugEnabled()) {
                    log.debug(storeChargeItem.getStoreId() + "-" + orderId + "-" + storeChargeItem.getChargeItemId() + "-" + storeChargeSubitem.getProductId());
                }
            }
        }
        return list;
    }

    public List<StoreOrderItemPromotion> getStoreChargeItemPromotion(Map<Long, StoreChargeItemPromotion> chargeItemPromotionMap, StoreOrder storeOrder, List<StoreOrderItem> orderItemlist) {
        if (chargeItemPromotionMap == null || chargeItemPromotionMap.isEmpty()) {
            return null;
        }
        //只有用户下单才能享受单品促销折扣
        if (storeOrder.getUserId() <= 0) {
            return null;
        }
        List<StoreOrderItemPromotion> orderItemPromotions = new ArrayList<StoreOrderItemPromotion>();
        long currentTime = System.currentTimeMillis();
        for (StoreChargeItemPromotion chargeItemPromotion : chargeItemPromotionMap.values()) {
            StoreOrderItemPromotion orderItemPromotion = new StoreOrderItemPromotion();
            orderItemPromotion.setOrderId(storeOrder.getOrderId());
            orderItemPromotion.setTradeOrder(false);
            orderItemPromotion.setUserId(storeOrder.getUserId());
            orderItemPromotion.setRepastDate(storeOrder.getRepastDate());
            orderItemPromotion.setTimeBucketId(storeOrder.getTimeBucketId());
            orderItemPromotion.setAmount(1);
            orderItemPromotion.setPromotionId(chargeItemPromotion.getPromotionId());
            orderItemPromotion.setMerchantId(chargeItemPromotion.getMerchantId());
            orderItemPromotion.setStoreId(chargeItemPromotion.getStoreId());
            orderItemPromotion.setChargeItemId(chargeItemPromotion.getChargeItemId());
            orderItemPromotion.setPromotionPrice(chargeItemPromotion.getPromotionPrice());
            orderItemPromotion.setUpdateTime(currentTime);
            orderItemPromotion.setCreateTime(currentTime);
            if (orderItemlist != null && orderItemlist.size() > 0) {
                for (StoreOrderItem storeOrderItem : orderItemlist) {
                    if (storeOrderItem.getChargeItemId() == chargeItemPromotion.getChargeItemId()) {
                        orderItemPromotion.setChargeItemPrice(storeOrderItem.getPrice());
                    }
                }
            }
            orderItemPromotion.setPayablePrice(storeOrder.getPayablePrice());
			if (storeOrder.isPayAfter()) {
            	orderItemPromotion.setPayOrder(true);
            }
            orderItemPromotions.add(orderItemPromotion);
        }
        return orderItemPromotions;
    }
    
    public Map<Long, StoreOrderPlaceItemParam> getChargeItemsMapOfStoreOrder(List<StoreOrderPlaceItemParam> orderPlaceItems) {
        Map<Long, StoreOrderPlaceItemParam> orderItemMap = new HashMap<Long, StoreOrderPlaceItemParam>();
        if (orderPlaceItems == null || orderPlaceItems.isEmpty()) {
            return orderItemMap;
        }
        for (StoreOrderPlaceItemParam orderItem : orderPlaceItems) {
            orderItemMap.put(orderItem.getChargeItemId(), orderItem);
        }
        return orderItemMap;
    }

    public Map<Long, StoreOrderPlaceItemParam> getChargeItemsMapOfStoreOrder(StoreOrderPlaceParam storeOrderPlaceParam) {
        return this.getChargeItemsMapOfStoreOrder(storeOrderPlaceParam.getChargeItems());
    }

    public Map<Long, StoreChargeItem> getStoreChargeItemsMapOfStoreOrder(List<StoreChargeItem> orderChargeItems) {
        if (orderChargeItems == null || orderChargeItems.isEmpty()) {
            return null;
        }
        Map<Long, StoreChargeItem> orderItemMap = new HashMap<Long, StoreChargeItem>();
        for (StoreChargeItem orderItem : orderChargeItems) {
            orderItemMap.put(orderItem.getChargeItemId(), orderItem);
        }
        return orderItemMap;
    }

    public Map<Long, Double> getChargeItemNumMapOfStoreOrder(StoreOrderPlaceParam storeOrderPlaceParam) {
        List<StoreOrderPlaceItemParam> chargeItems = storeOrderPlaceParam.getChargeItems();
        if (chargeItems == null || chargeItems.isEmpty()) {
            return null;
        }
        Map<Long, Double> orderItemMap = new HashMap<Long, Double>();
        for (StoreOrderPlaceItemParam orderItem : chargeItems) {
            orderItemMap.put(orderItem.getChargeItemId(), orderItem.getAmount());

        }
        return orderItemMap;
    }

    public List<Long> getChargeItemIdsOfStoreOrder(StoreOrderPlaceParam storeOrderPlaceParam) {
        return this.getChargeItemIdsOfStoreOrder(storeOrderPlaceParam.getChargeItems());
    }
    
    public List<Long> getChargeItemIdsOfStoreOrder(List<StoreOrderPlaceItemParam> orderPlaceItems) {
        if (orderPlaceItems == null || orderPlaceItems.isEmpty()) {
            return null;
        }
        List<Long> orderItemIds = new ArrayList<Long>();
        for (StoreOrderPlaceItemParam orderItem : orderPlaceItems) {
            orderItemIds.add(orderItem.getChargeItemId());
        }
        return orderItemIds;
    }

    public List<Long> getChargeItemIdsOfStoreOrder(StoreOrder storeOrder) {
        List<StoreOrderItem> chargeItems = storeOrder.getStoreOrderItems();
        if (chargeItems == null || chargeItems.isEmpty()) {
            return null;
        }
        List<Long> orderChargeItemIds = new ArrayList<Long>();
        for (StoreOrderItem orderItem : chargeItems) {
            orderChargeItemIds.add(orderItem.getChargeItemId());
        }
        return orderChargeItemIds;
    }

    public String getChargeItemStrOfStoreOrder(StoreOrder storeOrder) {
        List<StoreOrderItem> chargeItems = storeOrder.getStoreOrderItems();
        if (chargeItems == null || chargeItems.isEmpty()) {
            return null;
        }
        StringBuilder itemStr = new StringBuilder();
        for (StoreOrderItem orderItem : chargeItems) {
            double amount = orderItem.getAmount();
            if (amount == 1) {
                itemStr.append(orderItem.getChargeItemName());
            } else {
                String num = String.valueOf(orderItem.getAmount());
                num = num.replace(".0", "");
                itemStr.append(orderItem.getChargeItemName()).append("x").append(num);
            }
            itemStr.append("；");
        }
        return itemStr.toString();
    }

    public Map<Long, Double> getChargeItemNumOfStoreOrder(StoreOrder storeOrder) {
        List<StoreOrderItem> chargeItems = storeOrder.getStoreOrderItems();
        if (chargeItems == null || chargeItems.isEmpty()) {
            return null;
        }
        Map<Long, Double> orderChargeItemMap = new HashMap<Long, Double>();
        for (StoreOrderItem orderItem : chargeItems) {
            orderChargeItemMap.put(orderItem.getChargeItemId(), orderItem.getAmount());
        }
        return orderChargeItemMap;
    }

    public Map<Long, Double> getProductNumOfStoreOrder(StoreOrder storeOrder) {
        Map<Long, Double> orderProductNumMap = new HashMap<Long, Double>();
        List<StoreOrderItem> itemList = storeOrder.getStoreOrderItems();
		if (itemList == null || itemList.isEmpty()) {
			return orderProductNumMap;
		}
        for (StoreOrderItem item : itemList) {
            double num = item.getAmount();
            List<StoreOrderSubitem> subItemList = item.getStoreOrderSubitems();
            for (StoreOrderSubitem subitem : subItemList) {
                long productId = subitem.getProductId();
                double subAmount = subitem.getAmount();
                Double amount = orderProductNumMap.get(productId);
                if (amount == null) {
                    amount = Double.valueOf(0);
                }
                double itemAmount =  NumberUtil.mul(subAmount, num);
                amount =  NumberUtil.add(amount, itemAmount);
                orderProductNumMap.put(productId, amount);
            }
        }
        return orderProductNumMap;
    }
    
    public List<Long> getStoreOrderItemIds(int merchantId, long storeId, String orderId, boolean enableSlave) {
    	List<Long> itemIds = new ArrayList<Long>();
		List<StoreOrderItem> storeOrderItems = storeOrderItemDAO.getStoreOrderItemByOrderId(merchantId, storeId, orderId, enableSlave);
		if (storeOrderItems == null || storeOrderItems.isEmpty()) {
			return itemIds;
		}
		for (StoreOrderItem item : storeOrderItems) {
			itemIds.add(item.getChargeItemId());
		}
		return itemIds;
    }
    
    public void setStoreOrderDetail(StoreOrder storeOrder, boolean enableSlave) {
		if (storeOrder == null) {
			return;
		}
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
		List<StoreOrderItem> storeOrderItems = storeOrderItemDAO.getStoreOrderItemByOrderId(merchantId, storeId, orderId, enableSlave);
		if (storeOrderItems == null || storeOrderItems.isEmpty()) {
			return;
		}
		Map<Long, List<StoreOrderSubitem>> map = storeOrderSubitemDAO.getStoreOrderSubitemMapById(merchantId, storeId, orderId, enableSlave);
		for (StoreOrderItem item : storeOrderItems) {
			long chargeItemid = item.getChargeItemId();
			List<StoreOrderSubitem> storeOrderSubitems = map.get(chargeItemid);
			if (storeOrderSubitems != null && storeOrderSubitems.size() > 0) {
				item.setStoreOrderSubitems(storeOrderSubitems);
			}
		}
		storeOrder.setStoreOrderItems(storeOrderItems);
	}

    /**
     * 设置多个订单的订单详情
     * @param storeOrders
     * @param enableSlave
     */
    public void setStoreOrderDetail(List<StoreOrder> storeOrders, boolean enableSlave) {
    	if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
        StoreOrder storeOrder = storeOrders.get(0);
        int merchantId = storeOrder.getMerchantId();
        long storeId = storeOrder.getStoreId();
        List<String> orderIds = storeOrders.stream().map(StoreOrder::getOrderId).collect(Collectors.toList());
        //找到订单项
        List<StoreOrderItem> storeOrderItemByOrderIds = storeOrderItemDAO.getStoreOrderItemById(merchantId, storeId, orderIds, enableSlave);
        ArrayListMultimap<String, StoreOrderItem> storeOrderItemMap = ArrayListMultimap.create();
        for (StoreOrderItem storeOrderItem : storeOrderItemByOrderIds) {
            storeOrderItemMap.put(storeOrderItem.getOrderId(), storeOrderItem);
        }
        //找到订单子项
        List<StoreOrderSubitem> storeOrderSubitemByOrderIds = storeOrderSubitemDAO.getStoreOrderSubitemByOrderIds(merchantId, storeId, orderIds, enableSlave);
        ArrayListMultimap<String, StoreOrderSubitem> storeOrderSubitemMap = ArrayListMultimap.create();
        for (StoreOrderSubitem storeOrderSubitem : storeOrderSubitemByOrderIds) {
            storeOrderSubitemMap.put(storeOrderSubitem.getOrderId(), storeOrderSubitem);
        }
        //组装订单详情
        for (StoreOrder order : storeOrders) {
            String orderId = order.getOrderId();
            //找出订单项
            List<StoreOrderItem> storeOrderItems = storeOrderItemMap.get(orderId);
            if (storeOrderItems != null && !storeOrderItems.isEmpty()) {
                //找出订单子项
                List<StoreOrderSubitem> storeOrderSubitems = storeOrderSubitemMap.get(orderId);
                ArrayListMultimap<Long, StoreOrderSubitem> storeOrderSubitemOfItemMap = ArrayListMultimap.create();
                if (storeOrderSubitems != null && !storeOrderSubitems.isEmpty()) {
                    for (StoreOrderSubitem storeOrderSubitem : storeOrderSubitems) {
                        storeOrderSubitemOfItemMap.put(storeOrderSubitem.getChargeItemId(), storeOrderSubitem);
                    }
                }
                //设置订单子项
                for (StoreOrderItem storeOrderItem : storeOrderItems) {
                    long chargeItemId = storeOrderItem.getChargeItemId();
                    List<StoreOrderSubitem> storeOrderSubitemsOfItem = storeOrderSubitemOfItemMap.get(chargeItemId);
                    if (storeOrderSubitemsOfItem != null && !storeOrderSubitemsOfItem.isEmpty()) {
                        storeOrderItem.setStoreOrderSubitems(storeOrderSubitemsOfItem);
                    } else {
                        storeOrderItem.setStoreOrderSubitems(Lists.newArrayList());
                    }
                }
                //设置订单项
                order.setStoreOrderItems(storeOrderItems);
            } else {
                order.setStoreOrderItems(Lists.newArrayList());
            }
        }
    }
    
    public void setStoreOrderTimes(StoreOrder storeOrder, boolean enableSlave) {
		if (storeOrder == null) {
			return;
		}
		List<StoreOrder> storeOrders = Lists.newArrayList();
		storeOrders.add(storeOrder);
		this.setStoreOrderTimes(storeOrders, enableSlave);
	}
    
    public void setStoreOrderTimes(List<StoreOrder> storeOrders, boolean enableSlave) {
    	if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		int merchantId = storeOrders.get(0).getMerchantId();
		long storeId = storeOrders.get(0).getStoreId();
		List<String> orderIds = this.getStoreOrderIds(storeOrders);
		Map<String, List<StoreOrderOptlog>> orderOptLogMap = storeOrderOptlogDAO.getStoreOrderOptlogsInIds(merchantId, storeId, orderIds, enableSlave);
		for (StoreOrder storeOrder : storeOrders) {
			List<StoreOrderOptlog> optLogs = orderOptLogMap.get(storeOrder.getOrderId());
			this.setStoreOrderTimes(storeOrder, optLogs);
		}
	}

    private void setStoreOrderTimes(StoreOrder storeOrder, List<StoreOrderOptlog> optLogs) {
        Map<Integer, StoreOrderOptlog> optlogMap = this.getOrderOptlogMapByOptType(optLogs);
        StoreOrderOptlog placeOrderOptLog = optlogMap.get(StoreOrderOptlogTypeEnum.PLACE_ORDER_CREATE.getValue());
        if (placeOrderOptLog != null) {
            storeOrder.setPlaceOrderTime(placeOrderOptLog.getCreateTime());
        }
        StoreOrderOptlog payOrderOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.USER_PAY_ORDER.getValue());
        if (payOrderOptlog == null) {
            payOrderOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.CASHIER_PAY_ORDER.getValue());
        }
        if (payOrderOptlog != null) {
            storeOrder.setPayOrderTime(payOrderOptlog.getCreateTime());
        }
        StoreOrderOptlog refundOrderOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.USER_REFUND_ORDER.getValue());
        if (refundOrderOptlog == null) {
            refundOrderOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.CASHIER_REFUND_ORDER.getValue());
        }
        if (refundOrderOptlog == null) {
            refundOrderOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.USER_CANCEL_ORDER.getValue());
        }
        if (refundOrderOptlog == null) {
            refundOrderOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.CASHIER_CANCEL_ORDER.getValue());
        }
        if (refundOrderOptlog != null) {
            storeOrder.setRefundOrderTime(refundOrderOptlog.getCreateTime());
        }
        StoreOrderOptlog takeCodeOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.USER_TAKE_CODE.getValue());
        if (takeCodeOptlog != null) {
            storeOrder.setTakeSerialTime(takeCodeOptlog.getCreateTime());
        }
        StoreOrderOptlog mealCheckoutOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.MEAL_CHECKOUT.getValue());
        if (mealCheckoutOptlog == null) {
            mealCheckoutOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.MEAL_CHECKOUT_COMPLETE.getValue());
        }
        if (mealCheckoutOptlog == null) {
            mealCheckoutOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.TRADE_FINISH.getValue());
        }
        if (mealCheckoutOptlog != null) {
            storeOrder.setMealCheckoutTime(mealCheckoutOptlog.getCreateTime());
        }
        StoreOrderOptlog tradeFinishOptlog = optlogMap.get(StoreOrderOptlogTypeEnum.TRADE_FINISH.getValue());
        if (tradeFinishOptlog != null) {
            storeOrder.setTradeFinishTime(tradeFinishOptlog.getCreateTime());
        }
    }
    
    public void setStoreOrderDelivery(StoreOrder storeOrder, boolean enableSlave) {
		if (storeOrder == null) {
			return;
		}
		List<StoreOrder> storeOrders = Lists.newArrayList();
		storeOrders.add(storeOrder);
		this.setStoreOrderDelivery(storeOrders, enableSlave);
	}
    
    public void setStoreOrderDelivery(List<StoreOrder> storeOrders, boolean enableSlave) {
		if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		int merchantId = storeOrders.get(0).getMerchantId();
		long storeId = storeOrders.get(0).getStoreId();
		List<String> deliveryOrderIds = Lists.newArrayList();
		for (StoreOrder storeOrder : storeOrders) {
			if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
				deliveryOrderIds.add(storeOrder.getOrderId());
			}
		}
		Map<String, StoreOrderDelivery> storeOrderDeliveryMap = this.storeOrderDeliveryDAO.getMapInIds(merchantId, storeId, deliveryOrderIds, enableSlave);
		for (StoreOrder storeOrder : storeOrders) {
			StoreOrderDelivery storeOrderDelivery = storeOrderDeliveryMap.get(storeOrder.getOrderId());
			if (storeOrderDelivery != null) {
				storeOrderDelivery.setDeliveryStatus(storeOrder.getDeliveryStatus());
				storeOrder.setStoreOrderDelivery(storeOrderDelivery);
			}
		}
    }
    
    public void setStoreOrderTableRecord(StoreOrder storeOrder, boolean enableSlave) {
		if (storeOrder == null) {
			return;
		}
		List<StoreOrder> storeOrders = Lists.newArrayList();
		storeOrders.add(storeOrder);
		this.setStoreOrderTableRecord(storeOrders, enableSlave);
	}
    
    public void setStoreOrderTableRecord(List<StoreOrder> storeOrders, boolean enableSlave) {
		if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		List<Long> tableRecordIds = this.getStoreOrderTableRecordIds(storeOrders);
		if (tableRecordIds == null || tableRecordIds.isEmpty()) {
			return;
		}
		int merchantId = storeOrders.get(0).getMerchantId();
		long storeId = storeOrders.get(0).getStoreId();
		Map<Long, StoreTableRecord> tableRecordMap = new HashMap<Long, StoreTableRecord>();
		if (!tableRecordIds.isEmpty()) {
			tableRecordMap = storeTableRecordDAO.getStoreTableRecordMapByIds(merchantId, storeId, tableRecordIds, enableSlave);
		}
		for (StoreOrder storeOrder : storeOrders) {
			// 桌台记录信息
			long tableRecordId = storeOrder.getTableRecordId();
			if (tableRecordId > 0) {
				StoreTableRecord storeTableRecord = tableRecordMap.get(tableRecordId);
				if (storeTableRecord != null) {
					storeOrder.setStoreTableRecord(storeTableRecord);
					storeOrder.setTakeSerialNumber(storeTableRecord.getTakeSerialNumber());
					// 如果订单为主订单，则根据桌台记录计算退款状态
					if (storeOrder.isTableRecordMasterOrder()) {
						if (storeTableRecord.getTotalRefundAmount() > 0) {
//							if (storeTableRecord.getTotalRefundAmount() == storeTableRecord.getPaidAmount()) {
//								storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
//							} else {
//								storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
//							}
                            List<StoreOrder> subStoreOrders = this.storeOrderDAO.getSubStoreOrderByTableRecordId(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getTableRecordId(), storeOrder.getOrderId(), false);
                            long tableActuralPayAmount = storeTableRecord.getStoreTableActualPrice(subStoreOrders, storeOrder);
                            List<TableRecordBatchRefundRecord> tableRecordBatchRefundRecords = tableRecordBatchRefundRecordDAO.getSuccessTableRecordBatchRefundByTableRecordId(storeOrder.getTableRecordId());
                            long actualRefundAmount = storeTableRecord.getStoreTableActualRefundAmount(tableRecordBatchRefundRecords);
                            if (actualRefundAmount == tableActuralPayAmount) {
                                storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
                            } else if (actualRefundAmount < tableActuralPayAmount) {
                                storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_PART.getValue());
                            } else {
                                log.warn("storeOrder[" + storeOrder.getOrderId() + "], merchantId[" + storeOrder.getMerchantId() + "],storeId[" + storeOrder.getStoreId() + "] refundAmount["+actualRefundAmount+"] > actualPayAmount["+tableActuralPayAmount+"]");
                                storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue());
                            }

                        } else {
							storeOrder.setRefundStatus(StoreOrderRefundStatusEnum.NOT.getValue());
						}
						
					}
				}
			}
		}
    }
    
    public void setStoreOrderRefundItem(StoreOrder storeOrder, boolean enableSlave) {
		if (storeOrder == null) {
			return;
		}
		List<StoreOrder> storeOrders = Lists.newArrayList();
		storeOrders.add(storeOrder);
		this.setStoreOrderRefundItem(storeOrders, enableSlave);
	}
    
    public void setStoreOrderRefundItem(List<StoreOrder> storeOrders, boolean enableSlave) {
    	if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		int merchantId = storeOrders.get(0).getMerchantId();
		long storeId = storeOrders.get(0).getStoreId();
		List<String> orderIds = this.getStoreOrderIds(storeOrders);
		List<StoreOrderRefundItem> storeOrderRefundItems = storeOrderRefundItemDAO.getStoreOrderRefundItemsByOrderIds(merchantId, storeId, orderIds, enableSlave);
		if (storeOrderRefundItems == null || storeOrderRefundItems.isEmpty()) {
			return;
		}
		for (StoreOrder storeOrder : storeOrders) {
			List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
			if (storeOrderItems == null || storeOrderItems.isEmpty()) {
				this.setStoreOrderDetail(storeOrder, enableSlave);
				storeOrderItems = storeOrder.getStoreOrderItems();
			}
			List<StoreOrderRefundItem> orderRefundItems = this.getStoreOrderRefundItems(storeOrderRefundItems, storeOrder.getOrderId());
			if (orderRefundItems == null || orderRefundItems.isEmpty()) {
				continue;
			}
			storeOrder.setStoreOrderRefundItems(orderRefundItems);
			for (StoreOrderItem storeOrderItem : storeOrderItems) {
				double refundChargeItemNumUnPacked = 0;
				double refundChargeItemNumPacked = 0;
				List<StoreOrderRefundItem> orderItemRefundItemList = this.getStoreOrderRefundItems(storeOrderRefundItems, storeOrder.getOrderId(), storeOrderItem.getChargeItemId());
				if (orderItemRefundItemList == null || orderItemRefundItemList.isEmpty()) {
					continue;
				}
				storeOrderItem.setStoreOrderRefundItems(orderItemRefundItemList);
				for (StoreOrderRefundItem storeOrderRefundItem : orderItemRefundItemList) {
					if (storeOrderRefundItem.isPacked()) {
						//refundChargeItemNumPacked += storeOrderRefundItem.getAmount();
						refundChargeItemNumPacked = MoneyUtil.add(refundChargeItemNumPacked, storeOrderRefundItem.getAmount());
					} else {
						//refundChargeItemNumUnPacked += storeOrderRefundItem.getAmount();
						refundChargeItemNumUnPacked = MoneyUtil.add(refundChargeItemNumUnPacked, storeOrderRefundItem.getAmount());
					}
				}
				storeOrderItem.setRefundChargeItemNumPacked(refundChargeItemNumPacked);
				storeOrderItem.setRefundChargeItemNumUnPacked(refundChargeItemNumUnPacked);
			}
		}
    }
    
    private List<StoreOrderRefundItem> getStoreOrderRefundItems(List<StoreOrderRefundItem> storeOrderRefundItems, String orderId){
		if (storeOrderRefundItems == null || storeOrderRefundItems.isEmpty() || orderId == null || orderId.isEmpty()) {
			return storeOrderRefundItems;
		}
    	List<StoreOrderRefundItem> resultOrderRefundItems = Lists.newArrayList();
		for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems){
			if (orderId.equalsIgnoreCase(storeOrderRefundItem.getOrderId())) {
				resultOrderRefundItems.add(storeOrderRefundItem);
			}
    	}
    	return resultOrderRefundItems;
    }
    
	private List<StoreOrderRefundItem> getStoreOrderRefundItems(List<StoreOrderRefundItem> storeOrderRefundItems, String orderId, long chargeItemId){
		if (storeOrderRefundItems == null || storeOrderRefundItems.isEmpty() || orderId == null || orderId.isEmpty()||chargeItemId==0) {
			return storeOrderRefundItems;
		}
    	List<StoreOrderRefundItem> resultOrderRefundItems = Lists.newArrayList();
		for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems){
			if (orderId.equalsIgnoreCase(storeOrderRefundItem.getOrderId()) && chargeItemId == storeOrderRefundItem.getChargeItemId()) {
				resultOrderRefundItems.add(storeOrderRefundItem);
			}
    	}
    	return resultOrderRefundItems;
    }
    
    public void setStoreOrderTimeBucket(StoreOrder storeOrder, boolean enableSlave) {
		if (storeOrder == null) {
			return;
		}
		List<StoreOrder> storeOrders = Lists.newArrayList();
		storeOrders.add(storeOrder);
		this.setStoreOrderTimeBucket(storeOrders, enableSlave);
	}
    
    public void setStoreOrderTimeBucket(List<StoreOrder> storeOrders, boolean enableSlave) {
		if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		int merchantId = storeOrders.get(0).getMerchantId();
		long storeId = storeOrders.get(0).getStoreId();
		List<Long> timeBucketIds = new ArrayList<Long>();
		for (StoreOrder storeOrder : storeOrders) {
			timeBucketIds.add(storeOrder.getTimeBucketId());
		}
		// 营业时段
		Map<Long, StoreTimeBucket> timeBucketMap = storeTimeBucketService.getStoreTimeBucketMapInIds(merchantId, storeId, timeBucketIds);
		for (StoreOrder storeOrder : storeOrders) {
			StoreTimeBucket storeTimeBucket = timeBucketMap.get(storeOrder.getTimeBucketId());
			storeOrder.setStoreTimeBucket(storeTimeBucket);
		}
    }
    
    public void setStoreOrderInvoice(StoreOrder storeOrder, boolean enableSlave) {
		if (storeOrder == null) {
			return;
		}
		List<StoreOrder> storeOrders = Lists.newArrayList();
		storeOrders.add(storeOrder);
		this.setStoreOrderInvoice(storeOrders, enableSlave);
	}
    
    public void setStoreOrderInvoice(List<StoreOrder> storeOrders, boolean enableSlave) {
		if (storeOrders == null || storeOrders.isEmpty()) {
			return;
		}
		int merchantId = storeOrders.get(0).getMerchantId();
		long storeId = storeOrders.get(0).getStoreId();
		List<String> orderIds = new ArrayList<String>();
		for (StoreOrder storeOrder : storeOrders) {
			if (storeOrder.getInvoiceStatus() == StoreOrderInvoiceStatusEnum.FINISH.getValue()){
				orderIds.add(storeOrder.getOrderId());
			}
		}
		// 发票信息
		Map<String, StoreOrderInvoice> storeOrderInvoiceMap = storeOrderInvoiceDAO.getMapByOrderIds(merchantId, storeId, orderIds, enableSlave);
		for (StoreOrder storeOrder : storeOrders) {
			StoreOrderInvoice storeOrderInvoice = storeOrderInvoiceMap.get(storeOrder.getOrderId());
			storeOrder.setStoreOrderInvoice(storeOrderInvoice);
		}
    }
    
    public void setStoreOrderPromotion(StoreOrder storeOrder, boolean enableSlave) throws TException{
		if (storeOrder == null) {
			return;
		}
		List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
		if (storeOrderItems == null || storeOrderItems.isEmpty()){
			return;
		}
		int merchantId = storeOrder.getMerchantId();
		long storeId = storeOrder.getStoreId();
		String orderId = storeOrder.getOrderId();
	    List<StoreOrderPromotion> storeOrderPromotionList = new ArrayList<>();
		if (storeOrder.getPromotionRebatePrice() > 0) {
			int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue();
			List<StoreOrderPromotion> storeOrderPromotions = this.getStoreOrderPromotionsByOrderIds(merchantId, storeId, Lists.newArrayList(orderId), promotionType, enableSlave);
			storeOrderPromotionList.addAll(storeOrderPromotions);
		}

		if(storeOrder.getGratisPrice() > 0){
			int promotionType = StoreOrderPromotionTypeEnum.PROMOTION_GRATIS.getValue();
			List<StoreOrderPromotion> storeOrderPromotions = this.getStoreOrderPromotionsByOrderIds(merchantId, storeId, Lists.newArrayList(orderId), promotionType, enableSlave);
			storeOrderPromotionList.addAll(storeOrderPromotions);
		}
	    storeOrder.setStoreOrderPromotions(storeOrderPromotionList);
		long promotionReduceId = storeOrder.getPromotionReduceId();
		if (promotionReduceId > 0){
			StorePromotionReduce storePromotionReduce = storePromotionReduceService.getStorePromotionReduce(merchantId, storeId, promotionReduceId, false, enableSlave, true);
			if (storePromotionReduce != null){
				String promotionReduceTitle = storePromotionReduce.getTitle();
				storeOrder.setPromotionReduceTitle(promotionReduceTitle);
			}
		}else{
			storeOrder.setPromotionReduceTitle("");
		}
    }
    
    public List<StoreOrderPromotion> getStoreOrderPromotionsByOrderIds(int merchantId, long storeId, List<String> orderIds, int promotionType, boolean enableSlave){
		if (storeId <= 0 || orderIds == null || orderIds.isEmpty()){
    		return Lists.newArrayList();
    	}
		Map<Long, Long> orderPromotionRebates = storeOrderItemPromotionDAO.getOrderPromotionsDerate(merchantId, storeId, orderIds, promotionType, enableSlave);
		if (orderPromotionRebates.isEmpty()) {
			return Lists.newArrayList();
		}
		List<Long> promotionRebateIds = Lists.newArrayList(orderPromotionRebates.keySet());
		Map<Long, StorePromotionRebate> storePromotionRebates = Maps.newHashMap();
		if (promotionType == StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue()){
			storePromotionRebates = storePromotionRebateService.getStorePromotionRebateMap(merchantId, storeId, promotionRebateIds);
		}
		Map<Long,StorePromotionGratis> storePromotionGratisMap = Maps.newHashMap();
		if(promotionType == StoreOrderPromotionTypeEnum.PROMOTION_GRATIS.getValue()){
			storePromotionGratisMap = storePromotionGratisService.getStorePromotionGratisMap(merchantId, storeId, promotionRebateIds);
		}
		List<StoreOrderPromotion> storeOrderPromotions = Lists.newArrayList();
		for (long promotionRebateId : orderPromotionRebates.keySet()) {
			StoreOrderPromotion storeOrderPromotion = new StoreOrderPromotion();
			storeOrderPromotion.setMerchantId(merchantId);
			storeOrderPromotion.setStoreId(storeId);
			storeOrderPromotion.setPromotionId(promotionRebateId);
			storeOrderPromotion.setPromotionType(promotionType);
			Long promotionDerate = orderPromotionRebates.get(promotionRebateId);
			if (promotionDerate != null) {
				storeOrderPromotion.setPromotionDerate(promotionDerate);
			}
			if (orderIds.size() == 1){
				storeOrderPromotion.setOrderId(orderIds.get(0));
			}
			if (promotionType == StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue()){
				// 其他折扣类型的名称暂不获取
				StorePromotionRebate storePromotionRebate = storePromotionRebates.get(promotionRebateId);
				if (storePromotionRebate != null){
					storeOrderPromotion.setPromotionTitle(storePromotionRebate.getTitle());
				}else{
					storeOrderPromotion.setPromotionTitle("");
				}
			}
			if(promotionType == StoreOrderPromotionTypeEnum.PROMOTION_GRATIS.getValue()){
				StorePromotionGratis storePromotionGratis = storePromotionGratisMap.get(promotionRebateId);
				if(storePromotionGratis != null){
					storeOrderPromotion.setPromotionTitle(storePromotionGratis.getTitle());
				}else{
					storeOrderPromotion.setPromotionTitle("");
				}
			}
			storeOrderPromotions.add(storeOrderPromotion);
		}
    	return storeOrderPromotions;
    }
    
    public List<StoreOrderDTO> getStoreOrderDTOByEntity(List<StoreOrder> storeOrders) {
        List<StoreOrderDTO> orders = new ArrayList<>();
        if (storeOrders == null || storeOrders.isEmpty()) {
            return orders;
        }
       return getStoreOrderDTOsByEntity(storeOrders, true);
    }

    /**
     * 转换订单集合 到orderDTO集合
     * @param storeOrders 需要转换的order集合
     * @param batchLoad 订单中加载的部分数据,是否批量加载
     */
    private List<StoreOrderDTO> getStoreOrderDTOsByEntity(List<StoreOrder> storeOrders, boolean batchLoad) {

        List<StoreOrderDTO> orderDTOs = Lists.newArrayList();

        if (storeOrders == null || storeOrders.isEmpty()) {
            return orderDTOs;
        }

        int merchantId = storeOrders.get(0).getMerchantId();
        //记录用户ID 和 服务员ID 进行批量查询
        Set<Long> userIds = new HashSet<>();
        Set<Long> staffIds = new HashSet<>();
        for (StoreOrder storeOrder : storeOrders) {
            StoreOrderDTO orderDTO = BeanUtil.copy(storeOrder, StoreOrderDTO.class);
            int takeMode = orderDTO.getTakeMode();
            List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
            if (storeOrderItems != null && !storeOrderItems.isEmpty()) {
                List<StoreOrderItemDTO> orderItemDTOs = new ArrayList<StoreOrderItemDTO>();
                for (StoreOrderItem storeOrderItem : storeOrderItems) {
                    StoreOrderItemDTO orderItemDTO = BeanUtil.copy(storeOrderItem, StoreOrderItemDTO.class);
                    if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                        orderItemDTO.setPackedAmount(orderItemDTO.getAmount());
                    }
                    List<StoreOrderSubitem> storeOrderSubitems = storeOrderItem.getStoreOrderSubitems();
                    List<StoreOrderSubItemDTO> orderSubItemDTOs = new ArrayList<StoreOrderSubItemDTO>();
                    if (storeOrderSubitems != null && !storeOrderSubitems.isEmpty()) {
                        orderSubItemDTOs = BeanUtil.copyList(storeOrderSubitems, StoreOrderSubItemDTO.class);
                    }
                    orderItemDTO.setStoreOrderSubItemSize(orderSubItemDTOs.size());
                    if(orderSubItemDTOs.size() == 1){
                        orderItemDTO.setSameSubItemName(storeOrderItem.getChargeItemName().equals(orderSubItemDTOs.get(0).getProductName()));
                    }
                    orderItemDTO.setStoreOrderSubItemDTOs(orderSubItemDTOs);
                    orderItemDTOs.add(orderItemDTO);
                }
                orderDTO.setStoreOrderItemDTOs(orderItemDTOs);
            }
            if (storeOrder.getStoreTimeBucket() != null) {
                StoreTimeBucketDTO storeTimeBucketDTO = BeanUtil.copy(storeOrder.getStoreTimeBucket(), StoreTimeBucketDTO.class);
                orderDTO.setStoreTimeBucketDTO(storeTimeBucketDTO);
            }
            if (storeOrder.getStoreOrderDelivery() != null) {
                StoreOrderDeliveryDTO storeOrderDeliveryDTO = BeanUtil.copy(storeOrder.getStoreOrderDelivery(), StoreOrderDeliveryDTO.class);
                orderDTO.setStoreOrderDeliveryDTO(storeOrderDeliveryDTO);
            }

            if (storeOrder.getStoreOrderSwitch() != null) {
                StoreOrderSwitchDTO storeOrderSwitchDTO = BeanUtil.copy(storeOrder.getStoreOrderSwitch(), StoreOrderSwitchDTO.class);
                orderDTO.setStoreOrderSwitchDTO(storeOrderSwitchDTO);
            }
            StoreTableRecord storeTableRecord = storeOrder.getStoreTableRecord();
            if (storeTableRecord != null){
                StoreOrderTableRecordDTO storeOrderTableRecordDTO = BeanUtil.copy(storeTableRecord, StoreOrderTableRecordDTO.class);
                orderDTO.setStoreOrderTableRecordDTO(storeOrderTableRecordDTO);
            }
            List<StoreOrderRefundItem> storeOrderRefundItems = storeOrder.getStoreOrderRefundItems();
            if (storeOrderRefundItems != null && !storeOrderRefundItems.isEmpty()){
                List<StoreOrderRefundItemDTO> storeOrderRefundItemDTOs = BeanUtil.copyList(storeOrderRefundItems, StoreOrderRefundItemDTO.class);
                orderDTO.setStoreOrderRefundItemDTOs(storeOrderRefundItemDTOs);
            }
            StoreOrderInvoice storeOrderInvoice = storeOrder.getStoreOrderInvoice();
            if (storeOrderInvoice != null){
                StoreOrderInvoiceDTO storeOrderInvoiceDTO = BeanUtil.copy(storeOrderInvoice, StoreOrderInvoiceDTO.class);
                orderDTO.setStoreOrderInvoiceDTO(storeOrderInvoiceDTO);
            }
            StoreOrderActualPayResult storeOrderActualPayResult = storeOrder.getStoreOrderActualPayResult();
            if (storeOrderActualPayResult != null){
                StoreOrderActualPayDTO storeOrderActualPayDTO = BeanUtil.copy(storeOrderActualPayResult, StoreOrderActualPayDTO.class);
                storeOrderActualPayDTO.setStoreOrderPayResultOfDynamicPayMethods(storeOrderActualPayResult.getStoreOrderPayResultOfDynamicPayMethodList());
                orderDTO.setStoreOrderActualPayDTO(storeOrderActualPayDTO);
            }
            List<StoreOrderPromotion> storeOrderPromotions = storeOrder.getStoreOrderPromotions();
            if (storeOrderPromotions != null && !storeOrderPromotions.isEmpty()){
                List<StoreOrderPromotionDTO> storeOrderPromotionDTOs = BeanUtil.copyList(storeOrderPromotions, StoreOrderPromotionDTO.class);
                orderDTO.setStoreOrderPromotionDTOs(storeOrderPromotionDTOs);
            }
            if (storeOrder.getStaffId() > 0) {
                staffIds.add(storeOrder.getStaffId());
                if (!batchLoad) {
                    try {
                        List<Long> queryStaffIds = new ArrayList<Long>();
                        queryStaffIds.add(storeOrder.getStaffId());
                        Map<Long, StaffDTO> staffDTOMap = staffFacade.getStaffMapInIds(storeOrder.getMerchantId(), queryStaffIds, true);
                        StaffDTO staffDTO = staffDTOMap.get(storeOrder.getStaffId());
                        StoreTableStaffDTO storeTableStaffDTO = this.getStoreTableStaffDTO(staffDTO);
                        orderDTO.setStaffDTO(storeTableStaffDTO);
                    } catch (TException e) {
                        log.error("staffId["+storeOrder.getStaffId()+"] invalid");
                    }
                }
            }
            if (storeOrder.getUserId() > 0) {
                userIds.add(storeOrder.getUserId());
                if (!batchLoad) {
                    try {
                        UserDTO userDTO = userFacade.getUserByUserId(storeOrder.getUserId());
                        I5weiUserDTO i5weiUserDTO = BeanUtil.copy(userDTO, I5weiUserDTO.class);
                        orderDTO.setUserDTO(i5weiUserDTO);
                    } catch (TException e) {
                        log.warn("userId["+storeOrder.getUserId()+"] invalid");
                    }
                }
            }

            if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue() &&
                    storeOrder.getWaimaiType() == WaimaiTypeEnum.PICKUPSITE.getValue() &&
                    storeOrder.getStoreOrderDelivery() != null) {
                StorePickupSiteBaseDTO storePickupSiteBaseDTO = this.storePickupSiteOrderService.getPickupSiteBaseDTOByOrderId(
                        storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getStoreOrderDelivery().getStorePickupSiteId(), storeOrder.getTimeBucketId());
                if (storePickupSiteBaseDTO != null) {
                    orderDTO.setStorePickupSite(storePickupSiteBaseDTO);
                }
            }
            orderDTOs.add(orderDTO);
        }
        //订单中的部分数据进行批量加载
        if (batchLoad) {
            Map<Long, UserDTO> userMapByIds = Maps.newHashMap();
            if (!userIds.isEmpty()) {
                List<Long> queryUserIds = userIds.stream().collect(Collectors.toList());
                try {
                    userMapByIds = userFacade.getUserMapByIds(queryUserIds);
                } catch (TException e) {
                    log.warn("userIds[" + queryUserIds.toString() + "] invalid");
                }
                //设置用户DTO
                for (StoreOrderDTO orderDTO : orderDTOs) {
                    long userId = orderDTO.getUserId();
                    if (userId > 0) {
                        UserDTO userDTO = userMapByIds.get(userId);
                        if (userDTO != null) {
                            I5weiUserDTO i5weiUserDTO = BeanUtil.copy(userDTO, I5weiUserDTO.class);
                            orderDTO.setUserDTO(i5weiUserDTO);
                        }
                    }
                }
            }

            if (!staffIds.isEmpty()) {
                List<Long> queryStaffIds = staffIds.stream().collect(Collectors.toList());
                Map<Long, StaffDTO> staffMapInIds = Maps.newHashMap();
                try {
                    staffMapInIds = staffFacade.getStaffMapInIds(merchantId, queryStaffIds, true);
                } catch (TException e) {
                    log.warn("staffIds[" + staffMapInIds.toString() + "] invalid");
                }
                //设置服务员DTO
                for (StoreOrderDTO orderDTO : orderDTOs) {
                    long staffId = orderDTO.getStaffId();
                    if (staffId > 0) {
                        StaffDTO staffDTO = staffMapInIds.get(staffId);
                        if (staffDTO != null) {
                            StoreTableStaffDTO storeTableStaffDTO = this.getStoreTableStaffDTO(staffDTO);
                            orderDTO.setStaffDTO(storeTableStaffDTO);
                        }
                    }
                }
            }
        }
        return orderDTOs;
    }

    public StoreOrderDTO getStoreOrderDTOSimpleByEntity(StoreOrder storeOrder) {
        if (storeOrder == null) {
            return new StoreOrderDTO();
        }
        StoreOrderDTO orderDTO = BeanUtil.copy(storeOrder, StoreOrderDTO.class);
        int takeMode = orderDTO.getTakeMode();
        List<StoreOrderItem> storeOrderItems = storeOrder.getStoreOrderItems();
        if (storeOrderItems != null && !storeOrderItems.isEmpty()) {
            List<StoreOrderItemDTO> orderItemDTOs = new ArrayList<StoreOrderItemDTO>();
            for (StoreOrderItem storeOrderItem : storeOrderItems) {
                StoreOrderItemDTO orderItemDTO = BeanUtil.copy(storeOrderItem, StoreOrderItemDTO.class);
                if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
                    orderItemDTO.setPackedAmount(orderItemDTO.getAmount());
                }
                List<StoreOrderSubitem> storeOrderSubitems = storeOrderItem.getStoreOrderSubitems();
                List<StoreOrderSubItemDTO> orderSubItemDTOs = new ArrayList<StoreOrderSubItemDTO>();
				if (storeOrderSubitems != null && !storeOrderSubitems.isEmpty()) {
					orderSubItemDTOs = BeanUtil.copyList(storeOrderSubitems, StoreOrderSubItemDTO.class);
				}
                orderItemDTO.setStoreOrderSubItemSize(orderSubItemDTOs.size());
                if(orderSubItemDTOs.size() == 1){
                    orderItemDTO.setSameSubItemName(storeOrderItem.getChargeItemName().equals(orderSubItemDTOs.get(0).getProductName()));
                }
                orderItemDTO.setStoreOrderSubItemDTOs(orderSubItemDTOs);
                orderItemDTOs.add(orderItemDTO);
            }
            orderDTO.setStoreOrderItemDTOs(orderItemDTOs);
        }
        return orderDTO;
    }
    
    public StoreOrderDTO getStoreOrderDTOByEntity(StoreOrder storeOrder) {
        if (storeOrder == null) {
            return new StoreOrderDTO();
        }
        List<StoreOrder> storeOrders = Lists.newArrayList();
        storeOrders.add(storeOrder);
        List<StoreOrderDTO> storeOrderDTOsByEntity = getStoreOrderDTOsByEntity(storeOrders, false);
        return storeOrderDTOsByEntity.get(0);
    }
    
    public List<String> getStoreOrderIds(List<StoreOrder> storeOrders) {
        List<String> orderIds = new ArrayList<String>();
        if (storeOrders == null || storeOrders.isEmpty()) {
            return orderIds;
        }
        for (StoreOrder order : storeOrders) {
            orderIds.add(order.getOrderId());
        }
        return orderIds;
    }
    
    public List<Long> getStoreOrderTableRecordIds(List<StoreOrder> storeOrders) {
        List<Long> tableRecordIds = new ArrayList<Long>();
        if (storeOrders == null || storeOrders.isEmpty()) {
            return tableRecordIds;
        }
        for (StoreOrder order : storeOrders) {
        	long tableRecordId = order.getTableRecordId();
			if (tableRecordId > 0) {
				tableRecordIds.add(order.getTableRecordId());
			}
        }
        return tableRecordIds;
    }

    public Map<Integer, StoreOrderOptlog> getOrderOptlogMapByOptType(List<StoreOrderOptlog> optLogs) {
        Map<Integer, StoreOrderOptlog> optlogMap = new HashMap<Integer, StoreOrderOptlog>();
		if (optLogs == null || optLogs.isEmpty()) {
			return optlogMap;
		}
        for (StoreOrderOptlog orderOptlog : optLogs) {
            optlogMap.put(orderOptlog.getOptType(), orderOptlog);
        }
        return optlogMap;
    }

    public Map<Long, Set<Long>> getOrderStoreTimeBucketIds(List<StoreOrder> storeOrders) {
        Map<Long, Set<Long>> storeTimeBucketMap = new HashMap<Long, Set<Long>>();
        for (StoreOrder storeOrder : storeOrders) {
            long storeId = storeOrder.getStoreId();
            long timeBucketId = storeOrder.getTimeBucketId();
            Set<Long> storeTimeBucketIds;
            if (storeTimeBucketMap.containsKey(storeId)) {
                storeTimeBucketIds = storeTimeBucketMap.get(storeId);
            } else {
                storeTimeBucketIds = new HashSet<Long>();
            }
            storeTimeBucketIds.add(timeBucketId);
            storeTimeBucketMap.put(storeId, storeTimeBucketIds);
        }
        return storeTimeBucketMap;
    }

    public StoreOrderTakeModeResult getStoreOrderTakeModeResult(PlaceOrderParam placeOrderParam, List<StoreChargeItem> orderChargeItems, boolean producePackageFee) {
        StoreOrderPlaceParam storeOrderPlaceParam = placeOrderParam.getStoreOrderPlaceParam();
        Store5weiSetting store5weiSetting = placeOrderParam.getStore5weiSetting();
        int siteNumber = storeOrderPlaceParam.getSiteNumber();
        int takeMode = storeOrderPlaceParam.getTakeMode();
        StoreOrderTakeModeResult storeOrderTakeModeResult = new StoreOrderTakeModeResult();
        //餐牌号
        if (siteNumber > 0) {
            storeOrderTakeModeResult.setSiteNumber(siteNumber);
        }
        //快取模式计算
        boolean isFastGet = false;
        if (storeOrderPlaceParam.getTakeMode() == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
            storeOrderTakeModeResult.setTakeMode(storeOrderPlaceParam.getTakeMode());
        } else {
			if (storeOrderPlaceParam.getTableRecordId() > 0) {
				isFastGet = false; // 关联到桌台的订单不支持快取
			}else if (store5weiSetting.isQuickTakeSupport()) {
                isFastGet = storeChargeItemService.isAllQuickTake(orderChargeItems);
                //如果从堂食进来，如果订单中得收费项目支持“快取”且含有打包费则不走“快取”模式，如果都没有打包费，则继续走“快取”模式
                //如果从打包进来，如果订单中得收费项目支持“快取”模式，则走“快取”模式
                if (takeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
                    if (producePackageFee) {
                        isFastGet = false;
                    }
					if (siteNumber > 0) {
                        isFastGet = false;
                    }
                }
            }
            if (isFastGet) {
                storeOrderTakeModeResult.setTakeMode(StoreOrderTakeModeEnum.QUICK_TAKE.getValue()); //快取；
            } else {
                storeOrderTakeModeResult.setTakeMode(storeOrderPlaceParam.getTakeMode()); //下单取餐方式
            }
        }
        // 是否限制取餐时间
        storeOrderTakeModeResult.setLimitMealTime(true);
        // 是否加菜
        storeOrderTakeModeResult.setEnableAddDishes(storeOrderPlaceParam.isEnableAddDishes());
        // 是否跳过自助取餐
        storeOrderTakeModeResult.setSkipTakeCode(storeOrderPlaceParam.isSkipTakeCode());
        // 取餐流程设置
		if (storeOrderPlaceParam.getUserId() > 0 && takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
        	storeOrderTakeModeResult.setSkipTakeCode(false); // 外送不涉及跳过取餐环节
        	storeOrderTakeModeResult.setDisableKitchen(false); // 外送后厨备餐
			storeOrderTakeModeResult.setEnableAddDishes(false); // 外送订单不算加菜
        } else {
        	int clientType = storeOrderPlaceParam.getClientType();
			if (clientType == ClientTypeEnum.CASHIER.getValue() || clientType == ClientTypeEnum.DIAN_CAI_BAO.getValue()){
        		storeOrderTakeModeResult.setSkipTakeCode(true); // 收银台跳过取餐环节
        		storeOrderTakeModeResult.setLimitMealTime(false); // 收银台不限制取餐时间
        	}
			if (isFastGet || placeOrderParam.isQuickTrade() || storeOrderPlaceParam.isBackOrder()) {
				storeOrderTakeModeResult.setSkipTakeCode(true); // 跳过取餐环节
				storeOrderTakeModeResult.setDisableKitchen(true); // 后厨不出餐
				storeOrderTakeModeResult.setLimitMealTime(false); // 不限制取餐时间
			}
			if (placeOrderParam.isQuickTrade()){
				storeOrderTakeModeResult.setTakeMode(StoreOrderTakeModeEnum.DINE_IN.getValue()); //自助餐都是堂食；
			}
			//小程序订单快速取餐
			if(clientType == ClientTypeEnum.MINA.getValue()){
				storeOrderTakeModeResult.setSkipTakeCode(true); // 跳过取餐环节
				storeOrderTakeModeResult.setDisableKitchen(true); // 后厨不出餐
				storeOrderTakeModeResult.setLimitMealTime(false); // 不限制取餐时间
			}
		}

        return storeOrderTakeModeResult;
    }

    public int getCancelOrderType(long staffId, StoreOrder storeOrder) {
        int cancelOrderType = 0;
        if (storeOrder == null) {
            return cancelOrderType;
        }
        int payStatus = storeOrder.getPayStatus();
        int refundStatus = storeOrder.getRefundStatus();
        int tradeStatus = storeOrder.getTradeStatus();
        if (payStatus == StoreOrderPayStatusEnum.FINISH.getValue()
                && refundStatus == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue()
                && tradeStatus == StoreOrderTradeStatusEnum.NOT.getValue()) {
            if (staffId <= 0) {
                cancelOrderType = StoreOrderCancelTypeEnum.SYSTEM.getValue();
            } else {
                cancelOrderType = StoreOrderCancelTypeEnum.DEFAULT_OR_STAFF.getValue();
            }
        } else if (payStatus == StoreOrderPayStatusEnum.FINISH.getValue()
                && refundStatus == StoreOrderRefundStatusEnum.USER_ALL.getValue()
                && tradeStatus == StoreOrderTradeStatusEnum.NOT.getValue()) {
            cancelOrderType = StoreOrderCancelTypeEnum.USER.getValue();
        }
        return cancelOrderType;
    }
        
    
    private StoreTableStaffDTO getStoreTableStaffDTO (StaffDTO staffDTO) {
		StoreTableStaffDTO storeTableStaffDTO = new StoreTableStaffDTO();
		storeTableStaffDTO.setUserId(staffDTO.getUserId());
		storeTableStaffDTO.setStaffId(staffDTO.getStaffId());
		storeTableStaffDTO.setAliasName(staffDTO.getAliasName());
		storeTableStaffDTO.setMerchantId(staffDTO.getMerchantId());
		storeTableStaffDTO.setStatus(staffDTO.getStatus());
		storeTableStaffDTO.setCreateTime(staffDTO.getCreateTime());
		storeTableStaffDTO.setUpdateTime(staffDTO.getUpdateTime());
		storeTableStaffDTO.setPwd(staffDTO.getPwd());
		storeTableStaffDTO.setPostQuantity(staffDTO.getPostQuantity());
		storeTableStaffDTO.setShowName(staffDTO.getShowName());
		UserDTO userDTO = staffDTO.getUserDTO();
		if (userDTO != null) {
			I5weiUserDTO i5weiUserDTO = BeanUtil.copy(userDTO, I5weiUserDTO.class);
			storeTableStaffDTO.setI5weiUserDTO(i5weiUserDTO);
		}
		return storeTableStaffDTO;
	}

    public void insertCashierPrintOrder(StoreOrder storeOrder) {
        if (storeOrder.getClientType() == ClientTypeEnum.CASHIER.getValue() && !storeOrder.isDiancaibaoPlaceOrder()) {
            return;
        }
        int merchantId = storeOrder.getMerchantId();
        long storeId = storeOrder.getStoreId();
        String orderId = storeOrder.getOrderId();
        //出错也要保证出餐正常
        try {
            //取餐方式:1＝堂食；2＝外带；3＝堂食+外带 才会打印
            if (storeOrder.getTakeMode() == StoreOrderTakeModeEnum.DINE_IN.getValue()
                    || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.TAKE_OUT.getValue()
                    || storeOrder.getTakeMode() == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {


                //判断是否需要自动打印出餐单
                if (storeAutoPrinterCashierFacade.isStoreAutoPrinterCashier(storeId, 0)) {
                    StoreStampTakemeal storeStampTakemeal = new StoreStampTakemeal();
                    storeStampTakemeal.setCreateTime(System.currentTimeMillis());
                    storeStampTakemeal.setMerchantId(merchantId);
                    storeStampTakemeal.setStoreId(storeId);
                    storeStampTakemeal.setStatus(StatusEnum.WAIT.getValue());//待打印
                    storeStampTakemeal.setUpdateTime(System.currentTimeMillis());
                    storeStampTakemeal.setOrderId(orderId);
                    storeStampTakemeal.setRepastDate(storeOrder.getRepastDate());
                    storeStampTakemeal.setTimeBuchetId(storeOrder.getTimeBucketId());
                    storeStampTakemealService.create(merchantId, storeId, storeStampTakemeal);
                }
            }
        } catch (Throwable e) {
            log.error("insertCashierPrintOrder fail", e);
        }
    }
    
    /**
     * 客户取餐计入消费次数统计
     */
    public void accumulateStoreUserOrders(StoreOrder storeOrder) {
		if (storeOrder == null || storeOrder.getUserId() <= 0) {
			return;
		}
		if (storeOrder.getOrdersTrade() > 0 ){
    		return;
    	}
		if (!storeOrder.isMasterOrder()){
    		return;
    	}
		try {
			StoreUserVisitOrderParam storeUserVisitOrderParam = BeanUtil.copy(storeOrder, StoreUserVisitOrderParam.class);
			StoreUserVisitDTO storeUserVisitDTO = storeUserVisitFacade.accumulateStoreUserOrders(storeUserVisitOrderParam);
			storeOrder.snapshot();
			storeOrder.setOrdersDineIn(storeUserVisitDTO.getOrdersDineIn());
			storeOrder.setOrdersInAndOut(storeUserVisitDTO.getOrdersInAndOut());
			storeOrder.setOrdersSendOut(storeUserVisitDTO.getOrdersSendOut());
			storeOrder.setOrdersTakeOut(storeUserVisitDTO.getOrdersTakeOut());
			storeOrder.setOrdersTrade(storeUserVisitDTO.getOrdersTrade());
			storeOrder.update();
		} catch (Throwable e) {
            log.error("accumulateStoreUserOrders fail", e);
        }
    }

    //add by akwei
    public void afterRefundSuccess4DefineRefund(RefundCallbackResult refundCallbackResult,int refundVersion) throws TException {
        StoreOrder storeOrder = refundCallbackResult.getStoreOrder();
        StoreOrderRefundRecord storeOrderRefundRecord = refundCallbackResult.getStoreOrderRefundRecord();
        long refundRecordId = storeOrderRefundRecord.getRefundRecordId();
		try {
			// 如果为美团订单，调用美团取消订单接口 edit by Jemon 20161116
			if (storeOrder.getWaimaiType() == 1) {
				int merchantId = storeOrder.getMerchantId();
				long storeId = storeOrder.getStoreId();
				String orderId = storeOrder.getOrderId();
				StoreOrderDelivery storeOrderDelivery = this.storeOrderDeliveryDAO.getById(merchantId, storeId, orderId, false);
				long meituanOrderId = Long.parseLong(storeOrderDelivery.getWaimaiOrderId());
				if (storeOrderDelivery.getWaimaiRefundType() == 0) {
					this.storeMeituanOrderFacade.cancelOrder(merchantId, storeId, meituanOrderId, storeOrder.getRepastDate(), "商家取消订单");
				}
			}
		} catch (Throwable t) {
			log.error("iPad define refund order for meituan fail.");
		}
        if(storeOrder.getUserId() > 0){
            //发送通知消息
            String tweet = getRefundTweetInfo(storeOrder, refundRecordId, refundVersion);
            this.wechatNotifyService.sendWechatMessage(storeOrder.getMerchantId(), storeOrder.getStoreId(), storeOrder.getUserId(), tweet);
        }
        
        // 统计消息
        i5weiMessageProducer.sendMessageOfStatStoreOrderRefund(storeOrder,
                storeOrderRefundRecord.getStaffId(),
                storeOrderRefundRecord.getRefundRecordId(), false);

        // 事件消息
        i5weiMessageProducer.sendMessageOfStoreOrderEvent(storeOrder,
                storeOrderRefundRecord.getStaffId(),
                TweetEventType.REFUND_ORDER, "订单退款");

        // 退款完成向店铺统计发送退款消息
        i5weiMessageProducer.sendRefundMessageOfStoreStatistics(storeOrderRefundRecord.getRefundRecordId());
    }

    public void afterRefundSuccess4PreOrderOrAllRefund(RefundCallbackResult refundCallbackResult,int refundVersion) throws TException {
        StoreOrder refundOrder = refundCallbackResult.getStoreOrder();
        StoreOrderRefundRecord storeOrderRefundRecord = refundCallbackResult.getStoreOrderRefundRecord();
        long refundRecordId = storeOrderRefundRecord.getRefundRecordId();
        long storeId = refundOrder.getStoreId();
        String orderId = refundOrder.getOrderId();
        long staffId = storeOrderRefundRecord.getStaffId();
		try {
			// 如果为美团订单，调用美团取消订单接口 edit by Jemon 20161116
			if (refundOrder.getWaimaiType() == 1) {
				int merchantId = refundOrder.getMerchantId();
				StoreOrderDelivery storeOrderDelivery = this.storeOrderDeliveryDAO.getById(merchantId, storeId, orderId, false);
				long meituanOrderId = Long.parseLong(storeOrderDelivery.getWaimaiOrderId());
				if (storeOrderDelivery.getWaimaiRefundType() == 0) {
					this.storeMeituanOrderFacade.cancelOrder(merchantId, storeId, meituanOrderId, refundOrder.getRepastDate(), "商家取消订单");
				}
			}
		} catch (Throwable t) {
			log.error("iPad refund preOrder for meituan fail.");
		}
        // 更新库存
        try {
            storeInventoryService.updateInventoryDateByOrder(refundOrder);
        } catch (Throwable e) {
            log.error("storeId[" + storeId + "] orderId[" + orderId + "] fail to updateInventoryByOrder ", e);
        }
        // 事件消息
        int isAuto = 0;
        int clientType = storeOrderRefundRecord.getClientType();
        if (clientType == ClientTypeEnum.CASHIER.getValue() && staffId > 0) {
            i5weiMessageProducer.sendMessageOfStoreOrderEvent(refundOrder, staffId, TweetEventType.CANCEL_ORDER, "取消订单");
        } else if (clientType == ClientTypeEnum.CASHIER.getValue() && staffId == 0) {
            isAuto = 1;
        }
        if (refundOrder.getUserId() > 0) {
            wechatNotifyService.notifyAutoOrderRefundMsg(refundOrder, isAuto, refundVersion);//通知用户
        }
        // 统计消息
        i5weiMessageProducer.sendMessageOfStatStoreOrderRefund(refundOrder, staffId, refundRecordId, true);

        if (refundOrder.getTradeStatus() == StoreOrderTradeStatusEnum.NOT.getValue()) {
            // 如果当前订单为预定订单，在5wei的交易订单退款完成后向统计模块发送消费消息
            i5weiMessageProducer.sendConsumeMessageOfStoreStatistics(refundOrder);
        }

        // 向店铺统计发送退款消息
        i5weiMessageProducer.sendRefundMessageOfStoreStatistics(refundRecordId);
    }
    
    public String getRefundTweetInfo(StoreOrder storeOrder,long refundRecordId,int refundVersion) throws TException {
        String date = DateUtil.formatDate("yyyy年MM月dd日", new Date(storeOrder.getRepastDate()));
        if (refundRecordId == 0) {
            return null;
        }
        RefundDetailDTO refundDetailDTO = refundFacade.getRefundDetailByRefundRecordId(refundRecordId);
        long amountRefund = refundDetailDTO.getAmountRefund();
        long cashRefund = refundDetailDTO.getCashRefund();
        long accountRefund = refundDetailDTO.getAccountRefund();
        long prepaidRefund = refundDetailDTO.getPrepaidRefund();
        long couponRefund = refundDetailDTO.getCouponRefund();
        StringBuilder tweet = new StringBuilder();
        tweet.append("尊敬的客户您好！");
        tweet.append("\n您的" + date + "订单[" + storeOrder.getOrderId() + "]退款，");
        tweet.append("\n本次退款金额" + MoneyUtil.getMoney(amountRefund) + "元，其中：");
        if (cashRefund > 0) {
            tweet.append("\n现金退" + MoneyUtil.getMoney(cashRefund) + "元");
        }
        if (accountRefund > 0) {
            tweet.append("\n火付账户退" + MoneyUtil.getMoney(accountRefund) + "元");
        }
        if (prepaidRefund > 0) {
            tweet.append("\n充值卡退" + MoneyUtil.getMoney(prepaidRefund) + "元");
        }
        if (couponRefund > 0) {
            tweet.append("\n优惠券退" + MoneyUtil.getMoney(couponRefund) + "元");
        }
        if(refundVersion == StoreOrderRefundVersion.VERSION_REFUND_TO_ACCOUNT.getValue()){
            if (accountRefund > 0) {
                String payOrderId = storeOrder.getPayOrderId();
                boolean canRefund = oriChannelRefundFacade.canOriChannelRefund(payOrderId);
                if (canRefund) {
                    tweet.append("\n >> <a href=\"" + this.getWechatRefundUrl(payOrderId) + "\">原路退回</a>");
                }
            }
        }

        // 第三方支付原路退回,不需要在发送"原路退回链接"
        if(refundVersion == StoreOrderRefundVersion.VERSION_REFUND_TO_ORICHANNEL.getValue()){
            long wechatRefund = refundDetailDTO.getWechatRefund();
            long alipayRefund = refundDetailDTO.getAlipayRefund();
            long yjpayRefund = refundDetailDTO.getYjRefund();
            if(wechatRefund > 0){
                tweet.append("\n微信退" + MoneyUtil.getMoney(wechatRefund) + "元");
            }
            if(alipayRefund > 0){
                tweet.append("\n支付宝退" + MoneyUtil.getMoney(alipayRefund) + "元");
            }
            if(yjpayRefund > 0){
                tweet.append("\n一键支付退" + MoneyUtil.getMoney(yjpayRefund) + "元");
            }
        }
        return tweet.toString();
    }

    private String getWechatRefundUrl(String payOrderId) {
        StringBuilder sb = new StringBuilder();
        sb.append(resourceBundle.getString("wechat.web.server"));
        sb.append("/weixin/meal/order/origin_refund?pay_order_id=");
        sb.append(payOrderId);
        return sb.toString();
    }
    
	public String getRefundItemMessages(List<RefundOrderItemParam> refundOrderItems) {
		StringBuilder refundItemMessages = new StringBuilder("");
		if (refundOrderItems == null || refundOrderItems.isEmpty()) {
			return refundItemMessages.toString();
		}
		int i = 1;
		for (RefundOrderItemParam itemParam : refundOrderItems) {
			refundItemMessages.append(itemParam.getChargeItemId()).append(",");
			refundItemMessages.append(itemParam.getRefundNum()).append(",");
			refundItemMessages.append(NumberUtil.bool2Int(itemParam.isRecoveryStock())).append(",");
			refundItemMessages.append(NumberUtil.bool2Int(itemParam.isPacked()));
			if (i < refundOrderItems.size()) {
				refundItemMessages.append(";");
			}
			i++;
		}
		return refundItemMessages.toString();
	}
	
	public List<StoreOrderRefundItem> getStoreOrderRefundItemsByRefundRecord(StoreOrderRefundRecord storeOrderRefundRecord, StoreOrder refundOrder){
		List<StoreOrderRefundItem> storeOrderRefundItems = new ArrayList<StoreOrderRefundItem>();
		String refundItemMessages = storeOrderRefundRecord.getRefundItemMessages();
		if (refundItemMessages == null || refundItemMessages.trim().isEmpty()) {
			return storeOrderRefundItems;
		}
		int merchantId = storeOrderRefundRecord.getMerchantId();
		long storeId = storeOrderRefundRecord.getStoreId();
		long staffId = storeOrderRefundRecord.getStaffId();
		String orderId = storeOrderRefundRecord.getOrderId();
		long repastDate = refundOrder.getRepastDate();
		long timeBucketId = refundOrder.getTimeBucketId();
		List<String> orderIds = new ArrayList<String>();
		orderIds.add(orderId);
		List<Long> chargeItemIds = new ArrayList<Long>();
		long currentTime = System.currentTimeMillis();
		String refundReason = storeOrderRefundRecord.getRefundReason();
		String[] refundItemMessageArray = refundItemMessages.split(";");
		for (String refundItemMessage : refundItemMessageArray) {
			StoreOrderRefundItem storeOrderRefundItem = new StoreOrderRefundItem();
			String[] refundItemParams = refundItemMessage.split(",");
			long chargeItemId = Long.valueOf(refundItemParams[0]);
			double refundNum = Double.valueOf(refundItemParams[1]);
			boolean recoveryStock = NumberUtil.int2Bool(Integer.valueOf(refundItemParams[2]));
			boolean packed = NumberUtil.int2Bool(Integer.valueOf(refundItemParams[3]));
			storeOrderRefundItem.setMerchantId(merchantId);
			storeOrderRefundItem.setStoreId(storeId);
			storeOrderRefundItem.setStaffId(staffId);
			storeOrderRefundItem.setOrderId(orderId);
			storeOrderRefundItem.setRepastDate(repastDate);
			storeOrderRefundItem.setTimeBucketId(timeBucketId);
			storeOrderRefundItem.setChargeItemId(chargeItemId);
			storeOrderRefundItem.setAmount(refundNum);
			storeOrderRefundItem.setRestoreInventory(recoveryStock);
			storeOrderRefundItem.setPacked(packed);
			storeOrderRefundItem.setRefundReason(refundReason);
			storeOrderRefundItem.setUpdateTime(currentTime);
			storeOrderRefundItem.setCreateTime(currentTime);
			storeOrderRefundItem.setRefundRecordId(storeOrderRefundRecord.getRefundRecordId());
			storeOrderRefundItems.add(storeOrderRefundItem);
			chargeItemIds.add(chargeItemId);
		}
		Map<Long, StoreOrderItem> refundStoreOrderItemMap = storeOrderItemDAO.getStoreOrderItemMapByIds(merchantId, storeId, orderIds, chargeItemIds);
		long originalPrice = 0;
		for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems) {
			long chargeItemId = storeOrderRefundItem.getChargeItemId();
			StoreOrderItem storeOrderItem = refundStoreOrderItemMap.get(chargeItemId);
			storeOrderRefundItem.setChargeItemName(storeOrderItem.getChargeItemName());
			storeOrderRefundItem.setUnit(storeOrderItem.getUnit());
			storeOrderRefundItem.setPrice(storeOrderItem.getPrice());
			originalPrice = originalPrice + storeOrderRefundItem.getOriginalPrice(storeOrderItem.getPackagePrice());
		}
		long refundAmount = storeOrderRefundRecord.getRefundAmount();
		if (refundAmount <= 0) {
			return storeOrderRefundItems;
		}
		int itemIndex = 1;
		long lastRefundPrice = refundAmount;
		for (StoreOrderRefundItem storeOrderRefundItem : storeOrderRefundItems) {
			BigDecimal b1 = new BigDecimal(storeOrderRefundItem.getOriginalPrice());
	        BigDecimal b2 = new BigDecimal(originalPrice);
	        BigDecimal rate = b1.divide(b2, 5, BigDecimal.ROUND_HALF_UP);
			long refundPrice = MoneyUtil.mul(refundAmount, rate.doubleValue());
			lastRefundPrice = lastRefundPrice - refundPrice;
			if (itemIndex < storeOrderRefundItems.size()){
				storeOrderRefundItem.setRefundPrice(refundPrice);
			}else{
				storeOrderRefundItem.setRefundPrice(lastRefundPrice);
			}
			itemIndex++;
		}
		return storeOrderRefundItems;
	}


    public StoreCashierDTO getStoreAutoPrintCashiers(int merchantId, long storeId, boolean autoPrintTake, boolean autoPrintSettle){
        StoreCashierAutoPrintParam param = new StoreCashierAutoPrintParam();
        param.setMerchantId(merchantId);
        param.setStoreId(storeId);
        param.setAutoPrintSettle(true);
        List<StoreCashierDTO> storeCashierDTOs = null;
        StoreCashierDTO storeCashierDTO = null;
        try {
            storeCashierDTOs = storeCashierFacade.getStoreAutoPrintCashier(param);
        } catch (TException e) {
            log.error("storeId[" + storeId + "],get auto print take storeCashier failed");
        }
        if(storeCashierDTOs.isEmpty()){
            return null;
        }else{
            if(storeCashierDTOs.size() == 1){
                storeCashierDTO = storeCashierDTOs.get(0);
            }else{
                return null;
            }
        }

        return storeCashierDTO;
    }

    public static <T> String transToRealTableName(int merchantId, long storeId, Class<T> classRef) {
        BaseStoreDbRouter.addInfo(merchantId, storeId);
        // 获取数据库名和表名
        EntityTableInfo<T> tableInfo0 = EntityTableInfoFactory.getEntityTableInfo(classRef);
        DALInfo dalInfo = DALParserUtil.process(classRef, tableInfo0.getDalParser());
        return dalInfo.getRealTable(classRef);
    }
}
