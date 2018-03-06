package com.huofu.module.i5wei.setting.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;

/**
 * Auto created by i5weitools
 * 店铺设置
 */
@Table(name = "tb_store_5wei_setting", dalParser = BaseDefaultStoreDbRouter.class)
public class Store5weiSetting extends AbsEntity {

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
     * 流水号起始值，不能小于桌牌号
     */
    @Column("serial_number_start")
    private int serialNumberStart;

    /**
     * 流水号起始值是否被锁定
     */
    @Column("serial_number_enable")
    private boolean serialNumberEnable;

    /**
     * 是否支持快取
     */
    @Column("quick_take_support")
    private boolean quickTakeSupport;

    /**
     * 是否支持堂食
     */
    @Column("enable_eatin")
    private boolean enableEatin;

    /**
     * 是否支持用户自取
     */
    @Column("enable_user_take")
    private boolean enableUserTake;

    /**
     * 是否支持自提点
     */
    @Column("enable_pickup_site")
    private boolean enablePickupSite = false;

    /**
     * 快取提示语
     */
    @Column("quick_take_tip")
    private String quickTakeTip;

    /**
     * 最大餐牌号
     */
    @Column("site_number_max")
    private int siteNumberMax;

    /**
     * 是否开启餐牌号
     */
    @Column("site_number_enable")
    private boolean siteNumberEnable;

    /**
     * 是否允许顾客自助领取餐牌号
     */
    @Column("site_number_self")
    private boolean siteNumberSelf;

    /**
     * 当顾客自助取餐后，提示设置餐牌的引导文字
     */
    @Column("site_number_tips")
    private String siteNumberTips;

    /**
     * 打包自取时是否使用餐牌号
     */
    @Column("site_number_for_take")
    private boolean siteNumberForTake;

    /**
     * 是否开启打包费
     */
    @Column("package_fee_enable")
    private boolean packageFeeEnable;

    /**
     * 是否开启排队人数
     */
    @Column("queue_number_enable")
    private boolean queueNumberEnable;

    /**
     * 客单价的计算方法：0=按订单统计，1=按入客数统计
     */
    @Column("customer_avg_payment_model")
    private int customerAvgPaymentModel;

    /**
     * 是否允许顾客手动设置了入客数
     */
    @Column("enable_customer_manual_customer_traffic")
    private boolean enableCustomerManualCustomerTraffic;

    /**
     * 是否允许现场点餐手动设置了入客数
     */
    @Column("enable_checkout_customer_traffic")
    private boolean enableCheckoutCustomerTraffic;

    /**
     * 是否收取台位费
     */
    @Column("enable_table_fee")
    private boolean enableTableFee;

    /**
     * 堂食点餐提示
     */
    @Column("eatin_tips")
    private String eatinTips;

    /**
     * 外送点餐提示
     */
    @Column("delivery_tips")
    private String deliveryTips;

    /**
     * 打包点餐提示
     */
    @Column("pack_tips")
    private String packTips;

    /**
     * 统一的点餐提示
     */
    @Column("unified_tips")
    private String unifiedTips; // required

    /**
     * 点餐提示方式, 参考 Store5weiSettingTipsTypeEnum
     */
    @Column("tips_type")
    private int tipsType; // required

    /**
     * 堂食模式下是否允许顾客整单备注
     */
    @Column("enable_user_remark_for_eating")
    public boolean enableUserRemarkForEatin;

    /**
     * 自取模式下是否允许顾客整单备注
     */
    @Column("enable_user_remark_for_take")
    public boolean enableUserRemarkForTake;

    /**
     * 备注显示文字,默认为空,表示显示:忌口备注
     */
    @Column("product_remark_label")
    private String productRemarkLabel = "忌口/备注";
    
    /**
     * #bool 是否设置过签到：0=否，1=是
     */
    @Column("sign_in_setted")
    private boolean signInSetted;

    /**
     * 免签到时间（分钟），默认30分钟
     */
    @Column("sign_in_free_time")
	private int signInFreeTime = 30;

    /**
     * 签到提示语
     */
    @Column("sign_in_slogan")
    private String signInSlogan;
    
    /**
     * #bool 订单自动锁定开关：0=关闭，1=开启
     */
    @Column("auto_lock_order")
    private boolean autoLockOrder;

    /**
     * 订单自动锁定金额
     */
    @Column("auto_lock_order_amount")
    private long autoLockOrderAmount;

    /**
     * 订单自动锁定时间（分钟）
     */
    @Column("auto_lock_order_deadline")
    private int autoLockOrderDeadline;
    
    /**
     * 定时取开关：0=关闭，1=开启
     */
    @Column("timing_take")
    private boolean timingTake;

    /**
     * 预留给店铺备餐的时间，默认30分钟
     */
    @Column("timing_prepare_time")
    private int timingPrepareTime;

    /**
     * 打印模式 1=普通打印，2=高级打印
     * {@link StorePrintModeEnum}
     */
    @Column("print_mode")
    private int printMode;

    /**
     * 是否手动切换过 0=未手动切换过 1=手动切换过
     */
    @Column("print_mode_defined")
    private boolean printModeDefined;

    /**
     * 是否是网络叫号
     */
    @Column("net_call_order")
    private boolean netCallOrder;

    /**
     * 是否去餐后标记叫号信息已取餐
     */
    @Column("call_order_taked")
    private boolean callOrderTaked;

    private int errorCode;

    public boolean isNetCallOrder() {
        return netCallOrder;
    }

    public void setNetCallOrder(boolean netCallOrder) {
        this.netCallOrder = netCallOrder;
    }

    public boolean isCallOrderTaked() {
        return callOrderTaked;
    }

    public void setCallOrderTaked(boolean callOrderTaked) {
        this.callOrderTaked = callOrderTaked;
    }

    public int getPrintMode() {
        return printMode;
    }

    public void setPrintMode(int printMode) {
        this.printMode = printMode;
    }

    public boolean isPrintModeDefined() {
        return printModeDefined;
    }

    public void setPrintModeDefined(boolean printModeDefined) {
        this.printModeDefined = printModeDefined;
    }

    public String getProductRemarkLabel() {
        return productRemarkLabel;
    }

    public void setProductRemarkLabel(String productRemarkLabel) {
        this.productRemarkLabel = productRemarkLabel;
    }

    public boolean isEnableUserRemarkForEatin() {
        return enableUserRemarkForEatin;
    }

    public void setEnableUserRemarkForEatin(boolean enableUserRemarkForEatin) {
        this.enableUserRemarkForEatin = enableUserRemarkForEatin;
    }

    public boolean isEnableUserRemarkForTake() {
        return enableUserRemarkForTake;
    }

    public void setEnableUserRemarkForTake(boolean enableUserRemarkForTake) {
        this.enableUserRemarkForTake = enableUserRemarkForTake;
    }

    public String getUnifiedTips() {
        return unifiedTips;
    }

    public void setUnifiedTips(String unifiedTips) {
        this.unifiedTips = unifiedTips;
    }

    public int getTipsType() {
        return tipsType;
    }

    public void setTipsType(int tipsType) {
        this.tipsType = tipsType;
    }

    public String getEatinTips() {
        return eatinTips;
    }

    public void setEatinTips(String eatinTips) {
        this.eatinTips = eatinTips;
    }

    public String getDeliveryTips() {
        return deliveryTips;
    }

    public void setDeliveryTips(String deliveryTips) {
        this.deliveryTips = deliveryTips;
    }

    public String getPackTips() {
        return packTips;
    }

    public void setPackTips(String packTips) {
        this.packTips = packTips;
    }

    public boolean isEnableEatin() {
        return enableEatin;
    }

    public void setEnableEatin(boolean enableEatin) {
        this.enableEatin = enableEatin;
    }

    public boolean isEnableUserTake() {
        return enableUserTake;
    }

    public void setEnableUserTake(boolean enableUserTake) {
        this.enableUserTake = enableUserTake;
    }

    public String getQuickTakeTip() {
        return quickTakeTip;
    }

    public void setQuickTakeTip(String quickTakeTip) {
        this.quickTakeTip = quickTakeTip;
    }

    public boolean isQuickTakeSupport() {
        return quickTakeSupport;
    }

    public void setQuickTakeSupport(boolean quickTakeSupport) {
        this.quickTakeSupport = quickTakeSupport;
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

    public boolean isSerialNumberEnable() {
        return serialNumberEnable;
    }

    public void setSerialNumberEnable(boolean serialNumberEnable) {
        this.serialNumberEnable = serialNumberEnable;
    }

    public int getSerialNumberStart() {
        return serialNumberStart;
    }

    public void setSerialNumberStart(int serialNumberStart) {
        this.serialNumberStart = serialNumberStart;
    }

    public int getSiteNumberMax() {
        return siteNumberMax;
    }

    public void setSiteNumberMax(int siteNumberMax) {
        this.siteNumberMax = siteNumberMax;
    }

    public boolean isSiteNumberEnable() {
        return siteNumberEnable;
    }

    public void setSiteNumberEnable(boolean siteNumberEnable) {
        this.siteNumberEnable = siteNumberEnable;
    }

    public boolean isSiteNumberSelf() {
        return siteNumberSelf;
    }

    public void setSiteNumberSelf(boolean siteNumberSelf) {
        this.siteNumberSelf = siteNumberSelf;
    }

    public String getSiteNumberTips() {
        return siteNumberTips;
    }

    public void setSiteNumberTips(String siteNumberTips) {
        this.siteNumberTips = siteNumberTips;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public boolean isSiteNumberForTake() {
        return siteNumberForTake;
    }

    public void setSiteNumberForTake(boolean siteNumberForTake) {
        this.siteNumberForTake = siteNumberForTake;
    }

    public boolean isPackageFeeEnable() {
        return packageFeeEnable;
    }

    public void setPackageFeeEnable(boolean packageFeeEnable) {
        this.packageFeeEnable = packageFeeEnable;
    }

    public boolean isQueueNumberEnable() {
        return queueNumberEnable;
    }

    public void setQueueNumberEnable(boolean queueNumberEnable) {
        this.queueNumberEnable = queueNumberEnable;
    }

    public int getCustomerAvgPaymentModel() {
        return customerAvgPaymentModel;
    }

    public void setCustomerAvgPaymentModel(int customerAvgPaymentModel) {
        this.customerAvgPaymentModel = customerAvgPaymentModel;
    }

    public boolean isEnableCustomerManualCustomerTraffic() {
        return enableCustomerManualCustomerTraffic;
    }

    public void setEnableCustomerManualCustomerTraffic(boolean enableCustomerManualCustomerTraffic) {
        this.enableCustomerManualCustomerTraffic = enableCustomerManualCustomerTraffic;
    }

    public boolean isEnableCheckoutCustomerTraffic() {
        return enableCheckoutCustomerTraffic;
    }

    public void setEnableCheckoutCustomerTraffic(boolean enableCheckoutCustomerTraffic) {
        this.enableCheckoutCustomerTraffic = enableCheckoutCustomerTraffic;
    }

    public boolean isEnableTableFee() {
        return enableTableFee;
    }

    public void setEnableTableFee(boolean enableTableFee) {
        this.enableTableFee = enableTableFee;
    }
    
	public boolean isSignInSetted() {
		return signInSetted;
	}

	public void setSignInSetted(boolean signInSetted) {
		this.signInSetted = signInSetted;
	}

	public int getSignInFreeTime() {
		return signInFreeTime;
	}

	public void setSignInFreeTime(int signInFreeTime) {
		this.signInFreeTime = signInFreeTime;
	}

	public String getSignInSlogan() {
		return signInSlogan;
	}

	public void setSignInSlogan(String signInSlogan) {
		this.signInSlogan = signInSlogan;
	}
	
	public boolean isAutoLockOrder() {
		return autoLockOrder;
	}

	public void setAutoLockOrder(boolean autoLockOrder) {
		this.autoLockOrder = autoLockOrder;
	}

	public long getAutoLockOrderAmount() {
		return autoLockOrderAmount;
	}

	public void setAutoLockOrderAmount(long autoLockOrderAmount) {
		this.autoLockOrderAmount = autoLockOrderAmount;
	}

	public int getAutoLockOrderDeadline() {
		return autoLockOrderDeadline;
	}

	public void setAutoLockOrderDeadline(int autoLockOrderDeadline) {
		this.autoLockOrderDeadline = autoLockOrderDeadline;
	}

	public boolean isTimingTake() {
		return timingTake;
	}

	public void setTimingTake(boolean timingTake) {
		this.timingTake = timingTake;
	}

	public int getTimingPrepareTime() {
		return timingPrepareTime;
	}

	public void setTimingPrepareTime(int timingPrepareTime) {
		this.timingPrepareTime = timingPrepareTime;
	}

	public int getSerialNumberStartBySiteNumber() {
		if (this.serialNumberStart <= 0) {
			this.serialNumberStart = 1;
		}
		int i = 0;
		if ((this.siteNumberMax % 10) > 0) {
			i = 1;
		}
		int siteNumberStartIndex = (this.siteNumberMax / 10 + i) * 10 + 1;
		if (this.siteNumberEnable) {
			return Math.max(siteNumberStartIndex, this.serialNumberStart);
		}
		return this.serialNumberStart;
	}

    public boolean isEnablePickupSite() {
        return enablePickupSite;
    }

    public void setEnablePickupSite(boolean enablePickupSite) {
        this.enablePickupSite = enablePickupSite;
    }

    /**
     * 是否仅支持快取
     *
     * @return true:是
     */
    public boolean isOnlyQuickTakeSupport() {
        //目前只有快取支持，因此只需要判断一个
        return this.isQuickTakeSupport();
    }

    public static Store5weiSetting createDefault(int merchantId, long storeId) {
        Store5weiSetting store5weiSetting = new Store5weiSetting();
        store5weiSetting.setMerchantId(merchantId);
        store5weiSetting.setStoreId(storeId);
        store5weiSetting.setSerialNumberStart(1);
        store5weiSetting.setQuickTakeSupport(true);
        store5weiSetting.setEnableEatin(true);
        store5weiSetting.setEnableUserTake(true);
	    store5weiSetting.setEnablePickupSite(false);
	    store5weiSetting.setQuickTakeTip("");
        store5weiSetting.setSiteNumberMax(30);
        store5weiSetting.setSiteNumberEnable(false);
        store5weiSetting.setSiteNumberSelf(false);
        store5weiSetting.setSiteNumberTips("");
        store5weiSetting.setEnableUserRemarkForTake(false);
        store5weiSetting.setEnableUserRemarkForEatin(false);
        store5weiSetting.setAutoLockOrder(true);
        store5weiSetting.setAutoLockOrderAmount(50000);
        store5weiSetting.setAutoLockOrderDeadline(30);
        store5weiSetting.setTimingTake(false);
        store5weiSetting.setTimingPrepareTime(30);
        store5weiSetting.setPrintMode(StorePrintModeEnum.NORMAL_PRINT.getValue());
        store5weiSetting.setPrintModeDefined(false);
        return store5weiSetting;
    }
    
    public static void main(String[] args) {
        Store5weiSetting store5weiSetting = new Store5weiSetting();
        int siteNumberMax = 156;
        boolean siteNumberEnable = true;
        int serialNumberStart = 100;
        store5weiSetting.setSiteNumberMax(siteNumberMax);
        store5weiSetting.setSiteNumberEnable(siteNumberEnable);
        store5weiSetting.setSerialNumberStart(serialNumberStart);
        int startIndex = store5weiSetting.getSerialNumberStartBySiteNumber();
        System.out.println("#####startIndex=" + startIndex);
    }
    
}
