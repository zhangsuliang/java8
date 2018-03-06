package com.huofu.module.i5wei.order.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.util.MoneyUtil;
import huofuhelper.util.json.JsonUtil;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.order.dbrouter.StoreOrderItemPromotionDbRouter;

/**
 * Auto created by i5weitools 店铺单品促销订单表
 */
@Table(name = "tb_store_order_item_promotion", dalParser = StoreOrderItemPromotionDbRouter.class)
public class StoreOrderItemPromotion extends AbsEntity {

	/**
	 * 自增主键
	 */
	@Id
	@Column("tid")
	private long tid;

	/**
	 * 订单ID
	 */
	@Column("order_id")
	private String orderId;

	/**
	 * #bool 支付订单：0=未支付，1=已支付
	 */
	@Column("pay_order")
	private boolean payOrder;

	/**
	 * #bool 取消订单：0=未取消，1=已取消
	 */
	@Column("cancel_order")
	private boolean cancelOrder;

	/**
	 * #bool 订单是否已交易：0=未交易，1=已交易
	 */
	@Column("trade_order")
	private boolean tradeOrder;
	
	/**
	 * 应付金额 tb_store_order.favorablePrice + tb_store_order.deliveryFee
	 */
	@Column("payable_price")
	private long payablePrice;
	
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
	 * 用户ID
	 */
	@Column("user_id")
	private long userId;

	/**
	 * 就餐日期
	 */
	@Column("repast_date")
	private long repastDate;

	/**
	 * 营业时间ID
	 */
	@Column("time_bucket_id")
	private long timeBucketId;

	/**
	 * 商品ID
	 */
	@Column("charge_item_id")
	private long chargeItemId;
	
	/**
	 * 收费项目原价
	 */
	@Column("charge_item_price")
	private long chargeItemPrice;

	/**
	 * 根据promotion_type确定具体所指的促销活动ID
	 */
	@Column("promotion_id")
	private long promotionId;

	/**
	 * 促销活动类型 {@link StoreOrderPromotionTypeEnum}
	 */
	@Column("promotion_type")
	private int promotionType;

	/**
	 * 促销价格
	 */
	@Column("promotion_price")
	private long promotionPrice;
	
	/**
	 * 活动限额
	 */
	@Column("promotion_quota")
	private long promotionQuota;
	
	/**
	 * 活动减免额度
	 */
	@Column("promotion_reduce")
	private long promotionReduce;
	
	/**
	 * 活动减免
	 */
	@Column("promotion_derate")
	private long promotionDerate;
	
	/**
	 * 参与活动的份数
	 */
	@Column("amount")
	private double amount;

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

	public StoreOrderItemPromotion(){
	}
	
	public StoreOrderItemPromotion(StoreChargeItem storeChargeItem){
		this.storeId = storeChargeItem.getStoreId();
		this.merchantId = storeChargeItem.getMerchantId();
		this.chargeItemId = storeChargeItem.getChargeItemId();
		this.chargeItemPrice = storeChargeItem.getCurPrice();
	}
	
	public long getTid() {
		return tid;
	}

	public void setTid(long tid) {
		this.tid = tid;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public boolean isPayOrder() {
		return payOrder;
	}

	public void setPayOrder(boolean payOrder) {
		this.payOrder = payOrder;
	}

	public boolean isCancelOrder() {
		return cancelOrder;
	}

	public void setCancelOrder(boolean cancelOrder) {
		this.cancelOrder = cancelOrder;
	}

	public boolean isTradeOrder() {
		return tradeOrder;
	}

	public void setTradeOrder(boolean tradeOrder) {
		this.tradeOrder = tradeOrder;
	}

	public long getPayablePrice() {
		return payablePrice;
	}

	public void setPayablePrice(long payablePrice) {
		this.payablePrice = payablePrice;
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

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public long getRepastDate() {
		return repastDate;
	}

	public void setRepastDate(long repastDate) {
		this.repastDate = repastDate;
	}

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}

	public long getChargeItemId() {
		return chargeItemId;
	}

	public void setChargeItemId(long chargeItemId) {
		this.chargeItemId = chargeItemId;
	}

	public long getChargeItemPrice() {
		return chargeItemPrice;
	}

	public void setChargeItemPrice(long chargeItemPrice) {
		this.chargeItemPrice = chargeItemPrice;
	}

	public long getPromotionId() {
		return promotionId;
	}

	public void setPromotionId(long promotionId) {
		this.promotionId = promotionId;
	}

	public int getPromotionType() {
		return promotionType;
	}

	public void setPromotionType(int promotionType) {
		this.promotionType = promotionType;
	}

	public long getPromotionPrice() {
		return promotionPrice;
	}

	public void setPromotionPrice(long promotionPrice) {
		this.promotionPrice = promotionPrice;
	}

	public long getPromotionQuota() {
		return promotionQuota;
	}

	public void setPromotionQuota(long promotionQuota) {
		this.promotionQuota = promotionQuota;
	}

	public long getPromotionReduce() {
		return promotionReduce;
	}

	public void setPromotionReduce(long promotionReduce) {
		this.promotionReduce = promotionReduce;
	}

	public long getPromotionDerate() {
		return promotionDerate;
	}

	public void setPromotionDerate(long promotionDerate) {
		this.promotionDerate = promotionDerate;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
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

	@Override
	public String toString() {
		return JsonUtil.build(this);
	}
	
	public void setStoreOrderInfo(StoreOrder storeOrder) {
		long currentTime = System.currentTimeMillis();
		this.orderId = storeOrder.getOrderId();
		this.userId = storeOrder.getUserId();
		this.repastDate = storeOrder.getRepastDate();
		this.timeBucketId = storeOrder.getTimeBucketId();
		this.payablePrice = storeOrder.getPayablePrice();
		this.createTime = currentTime;
		this.updateTime = currentTime;
	}
	
}