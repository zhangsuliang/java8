package com.huofu.module.i5wei.delivery.entity;

import com.huofu.module.i5wei.delivery.dbrouter.NoDistributeDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

/**
 * Auto created by i5weitools 店铺外送设置
 */
@Table(name = "tb_store_delivery_setting", dalParser = NoDistributeDbRouter.class)
public class StoreDeliverySetting extends BaseEntity {

	/**
	 * 店铺id
	 */
	@Id
	@Column("store_id")
	private long storeId;

	/**
	 * 商户id
	 */
	@Column("merchant_id")
	private int merchantId;

	/**
	 * 是否支持外送 0:否 1:是 #bool
	 */
	@Column("delivery_supported")
	private boolean deliverySupported;

	/**
	 * 是否支持指定外送时间 0:否 1:是 #bool
	 */
	@Column("delivery_assign_time_supported")
	private boolean deliveryAssignTimeSupported;

	/**
	 * 外送的最小订单金额
	 */
	@Column("min_order_delivery_amount")
	private long minOrderDeliveryAmount;

	/**
	 * 外送费
	 */
	@Column("delivery_fee")
	private long deliveryFee;

	/**
	 * 订单最低免外送费金额
	 */
	@Column("min_order_free_delivery_amount")
	private long minOrderFreeDeliveryAmount;

	/**
	 * 外送提前时间(分钟数，存储用毫秒)
	 */
	@Column("ahead_time")
	private long aheadTime;

	/**
	 * 外送是否支持开发票
	 */
	@Column("invoice_supported")
	private boolean invoiceSupported;

	/**
	 * 是否仅外送员可送餐
	 */
	@Column("only_deliver_send")
	private boolean onlyDeliverSend;

	/**
	 * 手动设置的外送提醒提前时间(分钟数,存储用毫秒)
	 */
	@Column("manual_notify_head_time")
	private long manualNotifyHeadTime;

	/**
	 * 是否支持手动设置外卖提醒时间
	 */
	@Column("manual_notify_head_supported")
	private boolean manualNotifyHeadSupported;

	/**
	 * 外送模式下是否允许顾客整单备注
	 */
	@Column("enable_user_remark_for_delivery")
	private boolean enableUserRemarkForDelivery;

	/**
	 * 是否支持自动备餐0：不支持，1:支持
	 */
	@Column("auto_prepare_meal_supported")
	private boolean autoPrepareMealSupported;

	/**
	 * 自动备餐时间周期
	 */
	@Column("auto_prepare_meal_period")
	private int autoPrepareMealPeriod;

	/**
	 * 外送模式为：距离模式的外送范围，单位是米
	 */
	@Column("delivery_scope")
	private int deliveryScope;

	/**
	 * 是否开启了美团外卖 0:未开启 1:开启
	 */
	@Column("meituan_waimai_enabled")
	private boolean meituanWaimaiEnabled;

	/**
	 * 是否开启了百度外卖 0:未开启 1:开启
	 */
	@Column("baidu_waimai_enabled")
	private boolean baiduWaimaiEnabled;

	/**
	 * 是否开启了饿了么外卖 0:未开启 1:开启
	 */
	@Column("eleme_waimai_enabled")
	private boolean elemeWaimaiEnabled;

	/**
	 * #美团相关需要 true
	 * 是否开启了店铺外卖总开关
	 */
	@Column("take_out_enabled")
	private boolean takeOutEnabled = true;

	public boolean isEnableUserRemarkForDelivery() {
		return enableUserRemarkForDelivery;
	}

	public void setEnableUserRemarkForDelivery(boolean enableUserRemarkForDelivery) {
		this.enableUserRemarkForDelivery = enableUserRemarkForDelivery;
	}

	public long getManualNotifyHeadTime() {
		return manualNotifyHeadTime;
	}

	public void setManualNotifyHeadTime(long manualNotifyHeadTime) {
		this.manualNotifyHeadTime = manualNotifyHeadTime;
	}

	public boolean isManualNotifyHeadSupported() {
		return manualNotifyHeadSupported;
	}

	public void setManualNotifyHeadSupported(boolean manualNotifyHeadSupported) {
		this.manualNotifyHeadSupported = manualNotifyHeadSupported;
	}

	public boolean isInvoiceSupported() {
		return invoiceSupported;
	}

	public void setInvoiceSupported(boolean invoiceSupported) {
		this.invoiceSupported = invoiceSupported;
	}

	public long getStoreId() {
		return storeId;
	}

	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}
    //外卖总开关关闭之后，不能进行接单 edit by Jemon 20170122
	public boolean isDeliverySupported() {
		
		if(deliverySupported && takeOutEnabled){
			return true;
		}else {
			return false;
		}
	}

	public void setDeliverySupported(boolean deliverySupported) {
		this.deliverySupported = deliverySupported;
	}

	public boolean isDeliveryAssignTimeSupported() {
		return deliveryAssignTimeSupported;
	}

	public void setDeliveryAssignTimeSupported(boolean deliveryAssignTimeSupported) {
		this.deliveryAssignTimeSupported = deliveryAssignTimeSupported;
	}

	public long getMinOrderDeliveryAmount() {
		return minOrderDeliveryAmount;
	}

	public void setMinOrderDeliveryAmount(long minOrderDeliveryAmount) {
		this.minOrderDeliveryAmount = minOrderDeliveryAmount;
	}

	public long getDeliveryFee() {
		return deliveryFee;
	}

	public void setDeliveryFee(long deliveryFee) {
		this.deliveryFee = deliveryFee;
	}

	public long getMinOrderFreeDeliveryAmount() {
		return minOrderFreeDeliveryAmount;
	}

	public void setMinOrderFreeDeliveryAmount(long minOrderFreeDeliveryAmount) {
		this.minOrderFreeDeliveryAmount = minOrderFreeDeliveryAmount;
	}

	public long getAheadTime() {
		return aheadTime;
	}

	public void setAheadTime(long aheadTime) {
		this.aheadTime = aheadTime;
	}

	public boolean isOnlyDeliverSend() {
		return onlyDeliverSend;
	}

	public void setOnlyDeliverSend(boolean onlyDeliverSend) {
		this.onlyDeliverSend = onlyDeliverSend;
	}

	public boolean isAutoPrepareMealSupported() {
		return autoPrepareMealSupported;
	}

	public void setAutoPrepareMealSupported(boolean autoPrepareMealSupported) {
		this.autoPrepareMealSupported = autoPrepareMealSupported;
	}

	public int getAutoPrepareMealPeriod() {
		return autoPrepareMealPeriod;
	}

	public void setAutoPrepareMealPeriod(int autoPrepareMealPeriod) {
		this.autoPrepareMealPeriod = autoPrepareMealPeriod;
	}

	public int getDeliveryScope() {
		return deliveryScope;
	}

	public void setDeliveryScope(int deliveryScope) {
		this.deliveryScope = deliveryScope;
	}

	public boolean isMeituanWaimaiEnabled() {
		return meituanWaimaiEnabled;
	}

	public void setMeituanWaimaiEnabled(boolean meituanWaimaiEnabled) {
		this.meituanWaimaiEnabled = meituanWaimaiEnabled;
	}

	public boolean isBaiduWaimaiEnabled() {
		return baiduWaimaiEnabled;
	}

	public void setBaiduWaimaiEnabled(boolean baiduWaimaiEnabled) {
		this.baiduWaimaiEnabled = baiduWaimaiEnabled;
	}

	public boolean isElemeWaimaiEnabled() {
		return elemeWaimaiEnabled;
	}

	public void setElemeWaimaiEnabled(boolean elemeWaimaiEnabled) {
		this.elemeWaimaiEnabled = elemeWaimaiEnabled;
	}

	public boolean isTakeOutEnabled() {
		return takeOutEnabled;
	}

	public void setTakeOutEnabled(boolean takeOutEnabled) {
		this.takeOutEnabled = takeOutEnabled;
	}

	/**
	 * 计算并返回外送费
	 *
	 * @param price 订单可支付价格
	 * @return 外送费用 0:面外送费 1:外送费用
	 */
	public long buildDeliveryFee(long price) {
		if (this.minOrderFreeDeliveryAmount <= 0) {
			return this.deliveryFee;
		}
		if (this.minOrderFreeDeliveryAmount <= price) {
			return 0;
		}
		return this.deliveryFee;
	}

	/**
	 * 订单价格是否支持外送
	 *
	 * @param price 订单可支付价格
	 * @return
	 */
	public boolean isDeliverySupportedForPrice(long price) {
		if (price >= this.minOrderDeliveryAmount) {
			return true;
		}
		return false;
	}

	public static StoreDeliverySetting createDefault(int merchantId, long storeId) {
		StoreDeliverySetting storeDeliverySetting = new StoreDeliverySetting();
		storeDeliverySetting.setMerchantId(merchantId);
		storeDeliverySetting.setStoreId(storeId);
		storeDeliverySetting.setDeliverySupported(false);
		storeDeliverySetting.setDeliveryAssignTimeSupported(false);
		storeDeliverySetting.setDeliveryFee(500);
		storeDeliverySetting.setMinOrderDeliveryAmount(900);
		storeDeliverySetting.setMinOrderFreeDeliveryAmount(0);
		storeDeliverySetting.setAheadTime(30 * 60 * 1000);
		storeDeliverySetting.setInvoiceSupported(true);
		storeDeliverySetting.setManualNotifyHeadSupported(false);
		storeDeliverySetting.setManualNotifyHeadTime(45 * 60 * 1000);
		storeDeliverySetting.setEnableUserRemarkForDelivery(true);
		storeDeliverySetting.setAutoPrepareMealSupported(false);
		storeDeliverySetting.setAutoPrepareMealPeriod(5 * 60 * 1000);
		storeDeliverySetting.setDeliveryScope(1000);
		return storeDeliverySetting;
	}

	public static long getMinNotifyTime(StoreDeliverySetting storeDeliverySetting) {
		if (storeDeliverySetting == null || !storeDeliverySetting.isDeliverySupported()) {
			return -1;
		}
		if (storeDeliverySetting.isManualNotifyHeadSupported() && storeDeliverySetting.getManualNotifyHeadTime() > 0) {
			return System.currentTimeMillis() + storeDeliverySetting.getManualNotifyHeadTime();
		}
		return (System.currentTimeMillis() + storeDeliverySetting.getAheadTime() + 5 * 60 * 1000);
	}

	/**
	 * 获取自动备餐的时间数（不包括外卖提醒时间），如果自动备餐关闭则不会自动备餐；-1表示不自动备餐
	 *
	 * @param storeDeliverySetting
	 * @return
	 */
	public static int getAutoPrepareMealTime(StoreDeliverySetting storeDeliverySetting) {
		if (storeDeliverySetting == null || !storeDeliverySetting.isDeliverySupported() ||
		    !storeDeliverySetting.isAutoPrepareMealSupported() || storeDeliverySetting.getAutoPrepareMealPeriod() <= 0) {
			return -1;
		}
		return storeDeliverySetting.getAutoPrepareMealPeriod();
	}
}