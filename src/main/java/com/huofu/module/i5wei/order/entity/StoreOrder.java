package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.order.dbrouter.StoreOrderDbRouter;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import com.huofu.module.i5wei.table.service.OrderPayFinishResult;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.meal.StoreSendTypeEnum;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import org.joda.time.MutableDateTime;

import java.util.List;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_order", dalParser = StoreOrderDbRouter.class)
public class StoreOrder extends AbsEntity {

	private static final long serialVersionUID = -1465396725918858230L;

	public static final int order_invalid_mins = 5;

    /**
     * 订单ID（主键，全库唯一）
     */
    @Id
    @Column("order_id")
    private String orderId;

    /**
     * 商户ID
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺ID
     */
    @Column("store_id")
    private long storeId;

    /**
     * 服务员工ID（用户下单时，staff_id＝0）
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 下单用户（员工下单时，有对应用户时user_id不等于0，无对应用户user_id＝0）
     */
    @Column("user_id")
    private long userId;

    /**
     * 营业时间ID
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 下单终端类型（需要定义客户端类型枚举类）
     */
    @Column("client_type")
    private int clientType;

    /**
     * 下单用户终端优惠金额，一天只有一单可以优惠
     */
    @Column("user_client_coupon")
    private long userClientCoupon;

    /**
     * 协议企业ID
     */
    @Column("enterprise_id")
    private int enterpriseId;

    /**
     * 协议企业折扣
     */
    @Column("enterprise_rebate")
    private double enterpriseRebate;

    /**
     * 协议企业折扣，订单中多少钱可以按协议企业打折，按订单详情中可以安协议企业打折的部分统计得到
     */
    @Column("enterprise_rebate_amount")
    private long enterpriseRebateAmount;

    /**
     * 协议企业折扣打折额度
     */
    @Column("enterprise_rebate_price")
    private long enterpriseRebatePrice;

    /**
     * 网单折扣
     */
    @Column("internet_rebate")
    private double internetRebate;

    /**
     * 网单折扣限额，订单中多少钱可以按网单打折，按订单详情中可以安网单打折的部分统计得到
     */
    @Column("internet_rebate_amount")
    private long internetRebateAmount;

    /**
     * 网单打折额度
     */
    @Column("internet_rebate_price")
    private long internetRebatePrice;
    
    /**
     * 折扣活动参与的金额
     */
    @Column("promotion_rebate_amount")
    private long promotionRebateAmount;

    /**
     * 折扣活动减免金额
     */
    @Column("promotion_rebate_price")
    private long promotionRebatePrice;
    
    /**
     * 满减活动ID
     */
    @Column("promotion_reduce_id")
    private long promotionReduceId;
    
    /**
     * 满减活动最低消费金额
     */
    @Column("promotion_reduce_quota")
    private long promotionReduceQuota;
    
    /**
     * 满减活动参与的金额
     */
    @Column("promotion_reduce_amount")
    private long promotionReduceAmount;
    
    /**
     * 满减活动减免金额
     */
    @Column("promotion_reduce_price")
    private long promotionReducePrice;
    
    /**
     * 赠菜免单金额
     */
    @Column("gratis_price")
    private long gratisPrice;

    /**
     * 整单折扣比例，可能是活动或者优惠，由店铺单独设置
     */
    @Column("total_rebate")
    private double totalRebate;

    /**
     * 整单减免金额，可能是活动或者优惠，由店铺单独设置
     */
    @Column("total_derate")
    private long totalDerate;

    /**
     * 整单折扣打折额度
     */
    @Column("total_rebate_price")
    private long totalRebatePrice;

    /**
     * 下单币种
     */
    @Column("order_currency_id")
    private int orderCurrencyId;

    /**
     * 原价（含订单项目金额原价+打包费+台位费...等，不含外送费）
     */
    @Column("order_price")
    private long orderPrice;

    /**
     * 总价（可以参与整单折扣的金额）
     */
    @Column("total_price")
    private long totalPrice;

    /**
     * 折后价（享受完各种折扣之后的金额，不含外送费）
     */
    @Column("favorable_price")
    private long favorablePrice;

    /**
     * 赊销状态：0=未赊账；1＝未销账；2＝销账成功；3＝赊账撤销
     */
    @Column("credit_status")
    private int creditStatus;

    /**
     * 赊账类型：0=未知，1＝协议企业，2＝公关费用
     */
    @Column("credit_type")
    private int creditType;

    /**
     * 支付订单ID
     */
    @Column("pay_order_id")
    private String payOrderId;

    /**
     * 实际支付币种
     */
    @Column("actual_currency_id")
    private int actualCurrencyId;

    /**
     * 实付，实际支付币种对应的金额
     */
    @Column("actual_price")
    private long actualPrice;

    /**
     * 订单的支付状态：1=待支付；2=支付中；3=支付完成；4=支付失败
     */
    @Column("pay_status")
    private int payStatus;

    /**
     * 1＝未退款，2＝用户全额退款：用户在支付完成之后，尚未发生实质交易时，发生的退款记录；3=商户全额退款：可能已发生交易，但由于异常情况需要做退款
     */
    @Column("refund_status")
    private int refundStatus;

    /**
     * 订单取消类型（结合refund_status已退款状态使用）：0=未取消（默认，订单取消时此项表示店员取消），1=用户取消，2=系统自动取消
     */
    @Column("cancel_order_type")
    private int cancelOrderType;

    /**
     * 交易状态是对基础交易订单的交易状态进行扩展：1＝尚未交易；2＝已备货【订单已收到，并备货，用户不可以随意退款，一般用于大额订单】；3＝已开始加工【外送接单进入加工环节，不可自主退款】；4＝已送出【专用于外卖，表示餐已送出】；5＝已取号【堂食、外带时，用户已经取了小票】；6＝交易完成【已取餐，或者已经送达】
     */
    @Column("trade_status")
    private int tradeStatus;

    /**
     * 开发票的状态：1＝不开发票；2＝有发票需求，尚未开发票；3＝已开发票
     */
    @Column("invoice_status")
    private int invoiceStatus;

    /**
     * 发票需求：如果订单有发票需求，需要设置发票需求信息，发票需求信息的数据格式参考“订单发票需求”
     */
    @Column("invoice_demand")
    private String invoiceDemand;

    /**
     * 是否限制取餐时间
     */
    @Column("limit_meal_time")
    private boolean limitMealTime;

    /**
     * 取餐流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;

    /**
     * 取餐验证码
     */
    @Column("take_code")
    private String takeCode;

    /**
     * 取餐模式，订单的取餐模式：1＝堂食；2＝外带；3＝堂食+外带；4＝外送；5＝快取
     */
    @Column("take_mode")
    private int takeMode;
    
    /**
     * 跳过后厨出餐，默认false
     */
    @Column("disable_kitchen")
    private boolean disableKitchen;

    /**
     * 库存保留状态，库存保留是在下单到支付期间的一段时间内，是否给用户保留库存：1＝无保留；2＝保留；3＝已扣减【支付成功，要正式扣减库存】
     */
    @Column("takeup_status")
    private int takeupStatus;

    /**
     * 取餐终端类型（客户端类型枚举类）
     */
    @Column("take_client_type")
    private int takeClientType;

    /**
     * 就餐日期，即菜单所在日期
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 新增时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 外送费
     */
    @Column("delivery_fee")
    private long deliveryFee;

    /**
     * 餐牌号，为0则表示没有餐牌号
     */
    @Column("site_number")
    private int siteNumber;

    /**
     * 订单打包费
     */
    @Column("package_fee")
    private long packageFee;

    /**
     * 是否会产生打包费
     */
    @Column("produce_package_fee")
    private boolean producePackageFee;

    /**
     * 折扣类型：1=企业折扣，2=网单折扣，3=整单折扣
     */
    @Column("rebate_type")
    private int rebateType;

    /**
     * 订单备注
     */
    @Column("order_remark")
    private String orderRemark;

    /**
     * 订单可使用优惠券金额
     */
    @Column("order_coupon_price")
    private long orderCouponPrice;
    
    /**
     * 订单优惠券减免金额
     */
    @Column("order_coupon_derate")
    private long orderCouponDerate;

    /**
     * 是否为补录：0=不是，1=是
     */
    @Column("back_order")
    private boolean backOrder;

    /**
     * 是否跳过取号环节：0=不是，1=是
     */
    @Column("skip_take_code")
    private boolean skipTakeCode;

    /**
     * 是否手动设置了入客数：0=不是，1=是
     */
    @Column("enable_manual_customer_traffic")
    private boolean enableManualCustomerTraffic;

    /**
     * 订单的入客数
     */
    @Column("customer_traffic")
    private int customerTraffic;

    /**
     * 是否加菜：0=不是，1=是
     */
    @Column("enable_add_dishes")
    private boolean enableAddDishes;

    /**
     * 台位费
     */
    @Column("table_fee")
    private long tableFee;

    /**
     * 会员价总共优惠
     */
    @Column("member_rebate_price")
    private long memberRebatePrice;

    /**
     * 用户点餐备注
     */
    @Column("user_remark")
    private String userRemark;
    
    /**
     * #bool 此订单是否后付费：0=否，1=是
     */
    @Column("pay_after")
    private boolean payAfter;
    
    /**
     * 桌台记录ID
     */
    @Column("table_record_id")
    private long tableRecordId;
    
    /**
     * 父订单id
     */
    @Column("parent_order_id")
    private String parentOrderId;

    /**
     * 业务类型 ${@link huofucore.facade.i5wei.order.StoreOrderCombinedBizType}
     */
    @Column("combined_biz_type")
    private int combinedBizType;
    
    /**
     * 单品折扣总共优惠
     */
    @Column("promotion_price")
    private long promotionPrice;
    
    /**
     * 0=未锁定，1=手动锁定，2=自动锁定
     */
    @Column("order_lock_status")
    private int orderLockStatus;
    
    /**
     * 订单自动锁定时间
     */
    @Column("auto_lock_time")
    private long autoLockTime;
    
    /**
     * 定时取时间
     */
    @Column("timing_take_time")
    private long timingTakeTime;

    /**
     * 堂食次数
     */
    @Column("orders_dine_in")
    public int ordersDineIn; 
    
    /**
     * 打包自取次数（含快取）
     */
    @Column("orders_take_out")
    public int ordersTakeOut; 
    
    /**
     * 堂食+打包次数
     */
    @Column("orders_in_and_out")
    public int ordersInAndOut; 
    
    /**
     * 外卖次数（仅考虑通过公众号购买）
     */
    @Column("orders_send_out")
    public int ordersSendOut; 
    
    /**
     * 所有已消费次数（即：堂食+打包+外卖次数）
     */
    @Column("orders_trade")
    public int ordersTrade;

    /**
     * 起菜状态 {@link huofucore.facade.i5wei.meal.StoreSendTypeEnum}
     */
    @Column("send_type")
    private int sendType = StoreSendTypeEnum.TAKE_ORDER.getValue();

    /**
     * 起菜时间
     */
    @Column("send_time")
    private long sendTime;

    /**
     * 外卖类型
     * 0=公众号，1=美团，2=饿了么，3=百度
     */
    @Column("waimai_type")
    public int waimaiType;
    /*
     * 协议企业折扣类型{@link EnterpriseRebateType}
     */
    @Column("enterprise_rebate_type")
    private int enterpriseRebateType;
    
    /**
     * 收款线id
     */
    @Column("cashier_channel_id")
    private long cashierChannelId;
    /*
	 * 收银员减免（抹零）（只有后付费结账抹零时使用）
	 */
	@Column("staff_derate")
	private long staffDerate;

    /**
     * 订单子项
     */
    private List<StoreOrderItem> storeOrderItems;
    
    /**
     * 订单退菜项目
     */
    private List<StoreOrderRefundItem> storeOrderRefundItems;
    
    /**
     * 营业时段
     */
    private StoreTimeBucket storeTimeBucket;

    /**
     * 订单取餐方式变更记录
     */
    private StoreOrderSwitch storeOrderSwitch;
    
    /**
     * 订单取餐方式变更记录
     */
    private StoreTableRecord storeTableRecord;
    
    /**
     * 发票信息
     */
    private StoreOrderInvoice storeOrderInvoice;
    
    /**
     * 实际支付信息
     */
    private StoreOrderActualPayResult storeOrderActualPayResult;
    
    /**
     * 订单促销信息列表
     */
    private List<StoreOrderPromotion> storeOrderPromotions;
    
    /**
     * 满减活动标题
     */
    private String promotionReduceTitle;
    
    /**
     * 下单时间
     */
    private long placeOrderTime;

    /**
     * 付款时间
     */
    private long payOrderTime;

    /**
     * 退款时间
     */
    private long refundOrderTime;

    /**
     * 取餐时间
     */
    private long takeSerialTime;

    /**
     * 出餐时间
     */
    private long mealCheckoutTime;

    /**
     * 交易完成时间
     */
    private long tradeFinishTime;

    /**
     * 订单项目描述信息
     */
    private String orderDescription;

    /**
     * 订单的待出餐列表
     */
    private List<StoreMealTakeup> storeMealTakeups;

    /**
     * 是否是点菜宝下的订单
     */
    private boolean diancaibaoPlaceOrder;
    
    /**
     * 下单请求ID
     */
    private String requestId;

    private OrderPayFinishResult orderPayFinishResult;

    public boolean isDiancaibaoPlaceOrder() {
        return diancaibaoPlaceOrder;
    }

    public void setDiancaibaoPlaceOrder(boolean diancaibaoPlaceOrder) {
        this.diancaibaoPlaceOrder = diancaibaoPlaceOrder;
    }

    public List<StoreMealTakeup> getStoreMealTakeups() {
        return storeMealTakeups;
    }

    public void setStoreMealTakeups(List<StoreMealTakeup> storeMealTakeups) {
        this.storeMealTakeups = storeMealTakeups;
    }

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public int getSendType() {
        return sendType;
    }

    public void setSendType(int sendType) {
        this.sendType = sendType;
    }

    public int getDeliveryStatus() {
        if (this.payStatus == StoreOrderPayStatusEnum.FINISH.getValue() &&
                this.tradeStatus == StoreOrderTradeStatusEnum.NOT.getValue() &&
                this.isNotFinishRefund4All()) {
            return StoreOrderDeliveryStatusEnum.WAIT_FOR_PREPARE.getValue();
        }
        if (this.tradeStatus == StoreOrderTradeStatusEnum.WORKIN.getValue()) {
            return StoreOrderDeliveryStatusEnum.PREPARING.getValue();
        }
        if (this.tradeStatus == StoreOrderTradeStatusEnum.PREPARE_MEAL_FINISH.getValue()) {
            return StoreOrderDeliveryStatusEnum.PREPARE_FINISH.getValue();
        }
        if (this.tradeStatus == StoreOrderTradeStatusEnum.SENTED.getValue()) {
            return StoreOrderDeliveryStatusEnum.DELIVERING.getValue();
        }
        if (this.tradeStatus == StoreOrderTradeStatusEnum.FINISH.getValue()) {
            return StoreOrderDeliveryStatusEnum.DELIVERY_FINISH.getValue();
        }
        return 0;
    }

    /**
     * 是否没有进行全部退款或者未退款
     */
    private boolean isNotFinishRefund4All() {
        if (this.refundStatus != StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() &&
                this.refundStatus != StoreOrderRefundStatusEnum.USER_ALL.getValue()
                ) {
            return true;
        }
        return false;
    }

    /**
     * 是否是全部退款
     */
    public boolean isRefund4All() {
        if (this.refundStatus == StoreOrderRefundStatusEnum.MERCHANT_ALL.getValue() ||
                this.refundStatus == StoreOrderRefundStatusEnum.USER_ALL.getValue()
                ) {
            return true;
        }
        return false;
    }

    private StoreOrderDelivery storeOrderDelivery;

    public StoreOrderDelivery getStoreOrderDelivery() {
        return storeOrderDelivery;
    }

    public void setStoreOrderDelivery(StoreOrderDelivery storeOrderDelivery) {
        this.storeOrderDelivery = storeOrderDelivery;
    }

    public long getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(long deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public int getSiteNumber() {
        return siteNumber;
    }

    public void setSiteNumber(int siteNumber) {
        this.siteNumber = siteNumber;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public long getUserClientCoupon() {
        return userClientCoupon;
    }

    public void setUserClientCoupon(long userClientCoupon) {
        this.userClientCoupon = userClientCoupon;
    }

    public int getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(int enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public double getEnterpriseRebate() {
        return enterpriseRebate;
    }

    public void setEnterpriseRebate(double enterpriseRebate) {
        this.enterpriseRebate = enterpriseRebate;
    }

    public long getEnterpriseRebateAmount() {
        return enterpriseRebateAmount;
    }

    public void setEnterpriseRebateAmount(long enterpriseRebateAmount) {
        this.enterpriseRebateAmount = enterpriseRebateAmount;
    }

    public long getEnterpriseRebatePrice() {
        return enterpriseRebatePrice;
    }

    public void setEnterpriseRebatePrice(long enterpriseRebatePrice) {
        this.enterpriseRebatePrice = enterpriseRebatePrice;
    }

    public double getInternetRebate() {
        return internetRebate;
    }

    public void setInternetRebate(double internetRebate) {
        this.internetRebate = internetRebate;
    }

    public long getInternetRebateAmount() {
        return internetRebateAmount;
    }

    public void setInternetRebateAmount(long internetRebateAmount) {
        this.internetRebateAmount = internetRebateAmount;
    }

    public long getInternetRebatePrice() {
        return internetRebatePrice;
    }

    public void setInternetRebatePrice(long internetRebatePrice) {
        this.internetRebatePrice = internetRebatePrice;
    }
    
	public long getPromotionRebateAmount() {
		return promotionRebateAmount;
	}
	
	public void setPromotionRebateAmount(long promotionRebateAmount) {
		this.promotionRebateAmount = promotionRebateAmount;
	}

	public long getPromotionRebatePrice() {
		return promotionRebatePrice;
	}

	public void setPromotionRebatePrice(long promotionRebatePrice) {
		this.promotionRebatePrice = promotionRebatePrice;
	}
	
	public long getPromotionReduceId() {
		return promotionReduceId;
	}

	public void setPromotionReduceId(long promotionReduceId) {
		this.promotionReduceId = promotionReduceId;
	}
	
	public long getPromotionReduceQuota() {
		return promotionReduceQuota;
	}
	
	public void setPromotionReduceQuota(long promotionReduceQuota) {
		this.promotionReduceQuota = promotionReduceQuota;
	}

	public long getPromotionReduceAmount() {
		return promotionReduceAmount;
	}

	public void setPromotionReduceAmount(long promotionReduceAmount) {
		this.promotionReduceAmount = promotionReduceAmount;
	}

	public long getPromotionReducePrice() {
		return promotionReducePrice;
	}

	public void setPromotionReducePrice(long promotionReducePrice) {
		this.promotionReducePrice = promotionReducePrice;
	}

	public long getGratisPrice() {
		return gratisPrice;
	}

	public void setGratisPrice(long gratisPrice) {
		this.gratisPrice = gratisPrice;
	}

	public double getTotalRebate() {
        return totalRebate;
    }

    public void setTotalRebate(double totalRebate) {
        this.totalRebate = totalRebate;
    }

    public long getTotalDerate() {
        return totalDerate;
    }

    public void setTotalDerate(long totalDerate) {
        this.totalDerate = totalDerate;
    }

    public long getTotalRebatePrice() {
        return totalRebatePrice;
    }

    public void setTotalRebatePrice(long totalRebatePrice) {
        this.totalRebatePrice = totalRebatePrice;
    }

    public int getOrderCurrencyId() {
        return orderCurrencyId;
    }

    public void setOrderCurrencyId(int orderCurrencyId) {
        this.orderCurrencyId = orderCurrencyId;
    }

    public long getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(long orderPrice) {
        this.orderPrice = orderPrice;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    /**
	 * 订单菜品原价
	 * @return
	 */
	public long getChargeItemPrice(){
		return this.orderPrice - this.packageFee - this.tableFee;
	}
    
    /**
	 * 订单总原价
	 * @return
	 */
	public long getOriginalPrice(){
		return this.orderPrice + this.deliveryFee;
	}

	/**
	 * 应付金额
	 * @return
	 */
    public long getPayablePrice() {
        return this.favorablePrice + this.deliveryFee;
    }

    public int getCreditStatus() {
        return creditStatus;
    }

    public void setCreditStatus(int creditStatus) {
        this.creditStatus = creditStatus;
    }

    public int getCreditType() {
        return creditType;
    }

    public void setCreditType(int creditType) {
        this.creditType = creditType;
    }

    public String getPayOrderId() {
        return payOrderId;
    }

    public void setPayOrderId(String payOrderId) {
        this.payOrderId = payOrderId;
    }

    public int getActualCurrencyId() {
        return actualCurrencyId;
    }

    public void setActualCurrencyId(int actualCurrencyId) {
        this.actualCurrencyId = actualCurrencyId;
    }

    public long getActualPrice() {
        return actualPrice;
    }

    public void setActualPrice(long actualPrice) {
        this.actualPrice = actualPrice;
    }

    public int getPayStatus() {
        return payStatus;
    }

    public void setPayStatus(int payStatus) {
        this.payStatus = payStatus;
    }

    public int getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(int refundStatus) {
        this.refundStatus = refundStatus;
    }

    public int getCancelOrderType() {
        return cancelOrderType;
    }

    public void setCancelOrderType(int cancelOrderType) {
        this.cancelOrderType = cancelOrderType;
    }

    public int getTradeStatus() {
        return tradeStatus;
    }

    public void setTradeStatus(int tradeStatus) {
        this.tradeStatus = tradeStatus;
    }
    
    public boolean isSended(){
		if (this.tradeStatus == StoreOrderTradeStatusEnum.SENTED.getValue()
				&& this.takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()){
    		return true;
    	}
    	return false;
    }

    public int getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(int invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getInvoiceDemand() {
        return invoiceDemand;
    }

    public void setInvoiceDemand(String invoiceDemand) {
        this.invoiceDemand = invoiceDemand;
    }

    public int getTakeSerialNumber() {
        return takeSerialNumber;
    }

    public void setTakeSerialNumber(int takeSerialNumber) {
        this.takeSerialNumber = takeSerialNumber;
    }

    public String getTakeCode() {
        return takeCode;
    }

    public void setTakeCode(String takeCode) {
        this.takeCode = takeCode;
    }

    public int getTakeMode() {
        return takeMode;
    }

    public void setTakeMode(int takeMode) {
        this.takeMode = takeMode;
    }

	public boolean isDisableKitchen() {
		return disableKitchen;
	}

	public void setDisableKitchen(boolean disableKitchen) {
		this.disableKitchen = disableKitchen;
	}

	public int getTakeupStatus() {
        return takeupStatus;
    }

    public void setTakeupStatus(int takeupStatus) {
        this.takeupStatus = takeupStatus;
    }

    public int getTakeClientType() {
        return takeClientType;
    }

    public void setTakeClientType(int takeClientType) {
        this.takeClientType = takeClientType;
    }

    public long getRepastDate() {
        return repastDate;
    }

    public void setRepastDate(long repastDate) {
        this.repastDate = repastDate;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isLimitMealTime() {
        return limitMealTime;
    }

    public void setLimitMealTime(boolean limitMealTime) {
        this.limitMealTime = limitMealTime;
    }

    public List<StoreOrderItem> getStoreOrderItems() {
        return storeOrderItems;
    }

    public void setStoreOrderItems(List<StoreOrderItem> storeOrderItems) {
        this.storeOrderItems = storeOrderItems;
    }
    
    public List<StoreOrderRefundItem> getStoreOrderRefundItems() {
		return storeOrderRefundItems;
	}

	public void setStoreOrderRefundItems(List<StoreOrderRefundItem> storeOrderRefundItems) {
		this.storeOrderRefundItems = storeOrderRefundItems;
	}

	public StoreTimeBucket getStoreTimeBucket() {
        return storeTimeBucket;
    }

    public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
        this.storeTimeBucket = storeTimeBucket;
    }

    public StoreOrderSwitch getStoreOrderSwitch() {
        return storeOrderSwitch;
    }

    public void setStoreOrderSwitch(StoreOrderSwitch storeOrderSwitch) {
        this.storeOrderSwitch = storeOrderSwitch;
    }
    
    public StoreTableRecord getStoreTableRecord() {
		return storeTableRecord;
	}

	public void setStoreTableRecord(StoreTableRecord storeTableRecord) {
		this.storeTableRecord = storeTableRecord;
	}

	public StoreOrderInvoice getStoreOrderInvoice() {
		return storeOrderInvoice;
	}
	
	public void setStoreOrderInvoice(StoreOrderInvoice storeOrderInvoice) {
		this.storeOrderInvoice = storeOrderInvoice;
	}

	public StoreOrderActualPayResult getStoreOrderActualPayResult() {
		return storeOrderActualPayResult;
	}

	public void setStoreOrderActualPayResult(StoreOrderActualPayResult storeOrderActualPayResult) {
		this.storeOrderActualPayResult = storeOrderActualPayResult;
	}

	public List<StoreOrderPromotion> getStoreOrderPromotions() {
		return storeOrderPromotions;
	}

	public void setStoreOrderPromotions(List<StoreOrderPromotion> storeOrderPromotions) {
		this.storeOrderPromotions = storeOrderPromotions;
	}

	public String getPromotionReduceTitle() {
		return promotionReduceTitle;
	}

	public void setPromotionReduceTitle(String promotionReduceTitle) {
		this.promotionReduceTitle = promotionReduceTitle;
	}

	public long getPlaceOrderTime() {
        return placeOrderTime;
    }

    public void setPlaceOrderTime(long placeOrderTime) {
        this.placeOrderTime = placeOrderTime;
    }

    public long getPayOrderTime() {
        return payOrderTime;
    }

    public void setPayOrderTime(long payOrderTime) {
        this.payOrderTime = payOrderTime;
    }

    public long getRefundOrderTime() {
        return refundOrderTime;
    }

    public void setRefundOrderTime(long refundOrderTime) {
        this.refundOrderTime = refundOrderTime;
    }

    public long getTakeSerialTime() {
        return takeSerialTime;
    }

    public void setTakeSerialTime(long takeSerialTime) {
        this.takeSerialTime = takeSerialTime;
    }

    public long getMealCheckoutTime() {
        return mealCheckoutTime;
    }

    public void setMealCheckoutTime(long mealCheckoutTime) {
        this.mealCheckoutTime = mealCheckoutTime;
    }

    public long getTradeFinishTime() {
        return tradeFinishTime;
    }

    public void setTradeFinishTime(long tradeFinishTime) {
        this.tradeFinishTime = tradeFinishTime;
    }

    public String getOrderDescription() {
        return orderDescription;
    }

    public void setOrderDescription(String orderDescription) {
        this.orderDescription = orderDescription;
    }

    public long getFavorablePrice() {
        return favorablePrice;
    }

    public void setFavorablePrice(long favorablePrice) {
        this.favorablePrice = favorablePrice;
    }

    public long getPackageFee() {
        return packageFee;
    }

    public void setPackageFee(long packageFee) {
        this.packageFee = packageFee;
    }

    public boolean isProducePackageFee() {
        return producePackageFee;
    }

    public void setProducePackageFee(boolean producePackageFee) {
        this.producePackageFee = producePackageFee;
    }

    public int getRebateType() {
        return rebateType;
    }

    public void setRebateType(int rebateType) {
        this.rebateType = rebateType;
    }

    public String getOrderRemark() {
        return orderRemark;
    }

    public void setOrderRemark(String orderRemark) {
        this.orderRemark = orderRemark;
    }

    public long getOrderCouponPrice() {
		return orderCouponPrice;
	}

	public void setOrderCouponPrice(long orderCouponPrice) {
		this.orderCouponPrice = orderCouponPrice;
	}

	public long getOrderCouponDerate() {
		return orderCouponDerate;
	}

	public void setOrderCouponDerate(long orderCouponDerate) {
		this.orderCouponDerate = orderCouponDerate;
	}

	public boolean isBackOrder() {
		return backOrder;
	}

	public void setBackOrder(boolean backOrder) {
		this.backOrder = backOrder;
	}

	public boolean isSkipTakeCode() {
		return skipTakeCode;
	}

	public void setSkipTakeCode(boolean skipTakeCode) {
		this.skipTakeCode = skipTakeCode;
	}
	
	public boolean isEnableManualCustomerTraffic() {
		return enableManualCustomerTraffic;
	}

	public void setEnableManualCustomerTraffic(boolean enableManualCustomerTraffic) {
		this.enableManualCustomerTraffic = enableManualCustomerTraffic;
	}

	public int getCustomerTraffic() {
		return customerTraffic;
	}

	public void setCustomerTraffic(int customerTraffic) {
		this.customerTraffic = customerTraffic;
	}

	public boolean isEnableAddDishes() {
		return enableAddDishes;
	}

	public void setEnableAddDishes(boolean enableAddDishes) {
		this.enableAddDishes = enableAddDishes;
	}

	public long getTableFee() {
		return tableFee;
	}

	public void setTableFee(long tableFee) {
		this.tableFee = tableFee;
	}
	
	public long getMemberRebatePrice() {
        return memberRebatePrice;
    }

    public void setMemberRebatePrice(long memberRebatePrice) {
        this.memberRebatePrice = memberRebatePrice;
    }

    public String getUserRemark() {
        return userRemark;
    }

    public void setUserRemark(String userRemark) {
        this.userRemark = userRemark;
    }

	public boolean isPayAfter() {
		return payAfter;
	}

	public void setPayAfter(boolean payAfter) {
		this.payAfter = payAfter;
	}

	public long getTableRecordId() {
		return tableRecordId;
	}

	public void setTableRecordId(long tableRecordId) {
		this.tableRecordId = tableRecordId;
	}

	public String getParentOrderId() {
		return parentOrderId;
	}

	public void setParentOrderId(String parentOrderId) {
		this.parentOrderId = parentOrderId;
	}
	
    public long getPromotionPrice() {
		return promotionPrice;
	}

    public int getCombinedBizType() {
        return combinedBizType;
    }

    public void setCombinedBizType(int combinedBizType) {
        this.combinedBizType = combinedBizType;
    }

	public void setPromotionPrice(long promotionPrice) {
		this.promotionPrice = promotionPrice;
	}

	public int getOrderLockStatus() {
		return orderLockStatus;
	}

	public void setOrderLockStatus(int orderLockStatus) {
		this.orderLockStatus = orderLockStatus;
	}

	public long getAutoLockTime() {
		return autoLockTime;
	}

	public void setAutoLockTime(long autoLockTime) {
		this.autoLockTime = autoLockTime;
	}

	public long getTimingTakeTime() {
		return timingTakeTime;
	}

	public void setTimingTakeTime(long timingTakeTime) {
		this.timingTakeTime = timingTakeTime;
	}
	
	public int getOrdersDineIn() {
		return ordersDineIn;
	}

	public void setOrdersDineIn(int ordersDineIn) {
		this.ordersDineIn = ordersDineIn;
	}

	public int getOrdersTakeOut() {
		return ordersTakeOut;
	}

	public void setOrdersTakeOut(int ordersTakeOut) {
		this.ordersTakeOut = ordersTakeOut;
	}

	public int getOrdersInAndOut() {
		return ordersInAndOut;
	}

	public void setOrdersInAndOut(int ordersInAndOut) {
		this.ordersInAndOut = ordersInAndOut;
	}

	public int getOrdersSendOut() {
		return ordersSendOut;
	}

	public void setOrdersSendOut(int ordersSendOut) {
		this.ordersSendOut = ordersSendOut;
	}

	public int getOrdersTrade() {
		return ordersTrade;
	}

	public void setOrdersTrade(int ordersTrade) {
		this.ordersTrade = ordersTrade;
	}

	public int getWaimaiType() {
		return waimaiType;
	}

	public void setWaimaiType(int waimaiType) {
		this.waimaiType = waimaiType;
	}

	public long getCashierChannelId() {
        return cashierChannelId;
    }

    public void setCashierChannelId(long cashierChannelId) {
        this.cashierChannelId = cashierChannelId;
    }

	public long getStaffDerate() {
		return staffDerate;
	}

	public void setStaffDerate(long staffDerate) {
		this.staffDerate = staffDerate;
	}

	public boolean isWaimaiOrder() {
		if (this.waimaiType == WaimaiTypeEnum.MEITUAN.getValue()) {
			return true;
		} else if (this.waimaiType == WaimaiTypeEnum.ELEME.getValue()) {
			return true;
		} else if (this.waimaiType == WaimaiTypeEnum.BAIDU.getValue()) {
			return true;
		}
		return false;
	}
	
    public int getEnterpriseRebateType() {
        return enterpriseRebateType;
    }

    public void setEnterpriseRebateType(int enterpriseRebateType) {
        this.enterpriseRebateType = enterpriseRebateType;
    }
    
    public OrderPayFinishResult getOrderPayFinishResult() {
		return orderPayFinishResult;
	}

	public void setOrderPayFinishResult(OrderPayFinishResult orderPayFinishResult) {
		this.orderPayFinishResult = orderPayFinishResult;
	}

	public long getTimingTakeEndTime() {
		if (timingTakeTime == 0) {
			return 0;
		}
		MutableDateTime mdt = new MutableDateTime(timingTakeTime);
		mdt.setMinuteOfHour(0);
		mdt.setSecondOfMinute(0);
		for (int i = 0; i < 4; i++) {
			if (mdt.getMillis() < timingTakeTime) {
				mdt.addMinutes(15); // 15分钟一个间隔，所以最多只需要4次就能算出定时取结束时间
			} else {
				break;
			}
		}
		return mdt.getMillis();
	}
	
	/**
	 * 是否订单锁定
	 */
	public boolean isOrderLocked() {
		boolean orderLocked = false;
		if (orderLockStatus == StoreOrderLockStatusEnum.MANUAL.getValue()) {
			orderLocked = true;// 手动锁定直接返回true
		} else if (orderLockStatus == StoreOrderLockStatusEnum.AUTO.getValue()) {
			if (autoLockTime <= System.currentTimeMillis()) {
				orderLocked = true;// 自动锁定需要根据订单自动锁定时间和当前时间进行判断
			}
		}
		return orderLocked;
	}
	
	public boolean isQuickTake() {
		if (this.takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
			return true;
		}
		return false;
	}

	/**
     * 是否有外送订单
     *
     * @return true/false
     */
    public boolean isHasDelivery() {
        if (this.takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
            return true;
        }
        return false;
    }
    
    public String getTakeModeName(){
		if (this.takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
			return "外送";
		} else if (this.takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue() || this.takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
			return "打包";
		} else if (this.takeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
			return "堂食";
		} else if (this.takeMode == StoreOrderTakeModeEnum.IN_AND_OUT.getValue()) {
			return "堂食&打包";
		} else {
			return "堂食";
		}
    }

    /**
     * 是否为赊账订单
     */
    public boolean isCreditOrder() {
        if (this.creditStatus != StoreOrderCreditStatusEnum.NO_CREDIT.getValue()) {
            return true;
        }
        return false;
    }

    /**
     * 是否支付完成
     *
     * @return true 完成；fasle 未完成
     */
    public boolean isPayFinish() {
        if (this.payStatus == StoreOrderPayStatusEnum.FINISH.getValue()) {
            return true;
        }
        return false;
    }
    
    /**
     * 是否交易未开始处理
     *
     * @return
     */
    public boolean isTradeNot() {
        if (this.tradeStatus == StoreOrderTradeStatusEnum.NOT.getValue()) {
            return true;
        }
        return false;
    }

    /**
     * 是否是预定订单
     */
    public boolean isPreOrder() {
        if (this.isPayFinish() && this.isTradeNot()) {
            return true;
        }
        return false;
    }
    
    /**
     * 是否为桌台主订单(不区分订单是否为桌台模式)
     * @return true 是主订单；fasle 不是主订单
     */
    public boolean isMasterOrder() {
		if (tableRecordId > 0) {
			if(this.takeSerialNumber > 0 && this.parentOrderId.isEmpty()){
				return true;
			}
        }else{
        	return true;
        }
        return false;
    }
    
    /**
     * 桌台模式订单
     * @return
     */
    public boolean isTableRecordOrder () {
    	if (this.tableRecordId > 0) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * 判断是否为桌台记录主订单
     * @return
     */
    public boolean isTableRecordMasterOrder () {
    	if (this.tableRecordId > 0 && this.takeSerialNumber > 0 && this.parentOrderId.isEmpty()) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * 判断是否为桌台记录子订单
     * @return
     */
    public boolean isTableRecordSubOrder () {
    	if (this.tableRecordId > 0 && this.takeSerialNumber > 0 && !this.parentOrderId.isEmpty()) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * 根据子订单创建所属桌台记录对应主订单(桌台记录首次关联子订单时，需要同步创建一个空的主订单，用于获取主订单orderId)
     * @param subStoreOrder 关联到支付记录上的第一个子订单
     * @param tableRecordId 支付记录id
     * @param takeSerialNumber 取餐序列号
     * @param takeCode 取餐码
     */
    public void initMasterStoreOrder(StoreOrder subStoreOrder, long tableRecordId, int takeSerialNumber, String takeCode) {
		this.setMerchantId(subStoreOrder.getMerchantId());
		this.setStoreId(subStoreOrder.getStoreId());
		this.setStaffId(subStoreOrder.getStaffId()); 
		this.setUserId(subStoreOrder.getUserId()); 
		//this.setUserId(0L); 
		this.setTimeBucketId(subStoreOrder.getTimeBucketId());
		this.setClientType(subStoreOrder.getClientType()); // 主订单默认和子订单相同的终端类型
		this.setOrderCurrencyId(subStoreOrder.getOrderCurrencyId());
		this.setOrderPrice(0L);
		this.setTotalPrice(0L);
		this.setFavorablePrice(0L);
		this.setPayOrderId("");
		this.setActualCurrencyId(subStoreOrder.getActualCurrencyId());
		this.setActualPrice(0L);
		this.setTotalRebate(100);
		this.setTotalDerate(0L);
		this.setEnterpriseRebate(100);
		this.setInternetRebate(100);
		this.setPayStatus(StoreOrderPayStatusEnum.NOT.getValue());
		this.setRefundStatus(StoreOrderPayStatusEnum.NOT.getValue());
		this.setTradeStatus(StoreOrderTradeStatusEnum.NOT.getValue());
		this.setTakeSerialNumber(takeSerialNumber); 
		this.setTakeCode(takeCode); 
		this.setTakeMode(StoreOrderTakeModeEnum.DINE_IN.getValue()); 
		this.setTakeClientType(subStoreOrder.getTakeClientType()); // 首单取餐终端类型
		this.setRepastDate(subStoreOrder.getRepastDate());
		this.setCreateTime(System.currentTimeMillis());
		this.setUpdateTime(System.currentTimeMillis());
		this.setSiteNumber(subStoreOrder.getSiteNumber()); // 记录首单餐牌号
		this.setPackageFee(0L); // 记录合计打包费,主订单不存在打包费，置为0
		this.setTableFee(0L); // 台位费
		this.setPayAfter(subStoreOrder.isPayAfter());
		this.setTableRecordId(tableRecordId);
		this.setInvoiceDemand("");
		this.setOrderRemark("");
		this.setUserRemark("");
		this.setParentOrderId("");
		this.setSkipTakeCode(false);
		this.setEnableManualCustomerTraffic(true);
    }


    /**
     * 更新StoreOrder的bizType
     */
    public void updateCombineBizType(int combinedBizType) {
        this.snapshot();
        this.combinedBizType = combinedBizType;
        this.update();
    }

    /**
     * 是否是买充值卡支付的组合业务
     */
    public boolean isCombinedBuyPrepaidCard(){
        return this.combinedBizType == StoreOrderCombinedBizType.BUY_PREPAIDCARD_PAY_STORE_ORDER.getValue();
    }
    
    /**
     * 是否支付失败
     */
    public boolean isPayFail(){
        return this.payStatus == StoreOrderPayStatusEnum.FAILURE.getValue();
    }
}
