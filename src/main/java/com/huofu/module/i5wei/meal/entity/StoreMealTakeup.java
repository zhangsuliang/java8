package com.huofu.module.i5wei.meal.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.meal.dbrouter.StoreMealTakeupDbRouter;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.menu.ProductDivRuleEnum;
import huofucore.facade.i5wei.store5weisetting.StorePrintModeEnum;

/**
 * 已取号
 */
@Table(name = "tb_store_meal_takeup", dalParser = StoreMealTakeupDbRouter.class)
public class StoreMealTakeup extends AbsEntity {

	private static final long serialVersionUID = 561691072134085054L;

	/**
     * 自增ID
     */
    @Id
    @Column("tid")
    private int tid;

    /**
     * 订单ID
     */
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
     * 就餐日期
     */
    @Column("repast_date")
    private long repastDate;
    
    /**
     * 出餐口ID
     */
    @Column("port_id")
    private long portId;

    /**
     * 取餐流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;
    
    /**
     * 餐牌号，为0则表示没有餐牌号
     */
    @Column("site_number")
    private int siteNumber;

    /**
     * 营业时段ID
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 收费项目ID
     */
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 收费项目名称
     */
    @Column("charge_item_name")
    private String chargeItemName;

    /**
     * 产品ID
     */
    @Column("product_id")
    private long productId;

    /**
     * 产品名称
     */
    @Column("product_name")
    private String productName;

    /**
     * 规格：数量，支持半份＝0.5
     */
    @Column("amount")
    private double amount;

    /**
     * 收费项目份数
     */
    @Column("amount_order")
    private double amountOrder;

    /**
     * 0=堂食，1=打包
     */
    @Column("packaged")
    private boolean packaged;

    /**
     * 规格：单位（份、碗、个。。。）
     */
    @Column("unit")
    private String unit;
    
    /**
     * 取餐模式，订单的取餐模式：1＝堂食；2＝外带；3＝堂食+外带；4＝外送
     */
    @Column("take_mode")
    private int takeMode;
    
    /**
     * 是否显示产品：0=不显示，1=显示
     */
    @Column("show_products")
    private boolean showProducts;
    
    /**
     * 点餐备注
     */
    @Column("remark")
    private String remark;

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
     * 菜品辣度：1=微辣，2=中辣，3=辣，默认值为0（不辣）
     */
    @Column("spicy_level")
    private int spicyLevel;
    
    /**
     * 此订单是否后付费
     */
    @Column("pay_after")
    private boolean payAfter;
    
    /**
     * 桌台记录ID
     */
    @Column("table_record_id")
    private long tableRecordId;
    
    /**
     * 剩余待出餐数量
     */
    @Column("remain_takeup")
    private double remainTakeup;
    
    /**
     * 剩余传菜数量
     */
    @Column("remain_send")
    private double remainSend;
    
    /**
     * 出餐时间
     */
    @Column("checkout_time")
    private long checkoutTime;
    
    /**
     * 划菜时间
     */
    @Column("sweep_time")
    private long sweepTime;

    /**
     * 传菜口Id
     */
    @Column("send_port_id")
    private long sendPortId;

    /**
     * 传菜口Id
     */
    @Column("weight_enabled")
    private boolean weightEnabled;

    /**
     * 待出餐分单规则
     */
    private int divRule;
    
    /**
     * 是否是产品的分单规则
     */
    private boolean productDivRule;
    
    /**
     * 收费项目的分单规则是否开启
     */
    private boolean chargeItemEnable;
    
    public boolean isNumDiv(){
        if (divRule == ProductDivRuleEnum.NUM.getValue()){
            return true;
        }
        return false;
    }
    
    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
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

    public long getRepastDate() {
        return repastDate;
    }

    public void setRepastDate(long repastDate) {
        this.repastDate = repastDate;
    }

    public long getPortId() {
		return portId;
	}

	public void setPortId(long portId) {
		this.portId = portId;
	}

	public int getTakeSerialNumber() {
        return takeSerialNumber;
    }

    public void setTakeSerialNumber(int takeSerialNumber) {
        this.takeSerialNumber = takeSerialNumber;
    }
    
    public int getSiteNumber() {
		return siteNumber;
	}

	public void setSiteNumber(int siteNumber) {
		this.siteNumber = siteNumber;
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

    public String getChargeItemName() {
        return chargeItemName;
    }

    public void setChargeItemName(String chargeItemName) {
        this.chargeItemName = chargeItemName;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmountOrder() {
        return amountOrder;
    }

    public void setAmountOrder(double amountOrder) {
        this.amountOrder = amountOrder;
    }
    
    public void setAmountOrderTakeup(double amountOrder, int printMode, boolean isPaperSweep) {
        this.amountOrder = amountOrder;
        this.remainTakeup = amountOrder;
        if(printMode == StorePrintModeEnum.ADVANCE_PRINT.getValue() && !isPaperSweep){
            this.remainSend = amountOrder;
        }else{
            this.remainSend = 0;
        }
    }

    public boolean isPackaged() {
        return packaged;
    }

    public void setPackaged(boolean packaged) {
        this.packaged = packaged;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public boolean isShowProducts() {
		return showProducts;
	}

	public void setShowProducts(boolean showProducts) {
		this.showProducts = showProducts;
	}
	
	public int getTakeMode() {
		return takeMode;
	}

	public void setTakeMode(int takeMode) {
		this.takeMode = takeMode;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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
    
    public long getTakeupTime() {
		return createTime;
	}

	public int getSpicyLevel() {
		return spicyLevel;
	}

	public void setSpicyLevel(int spicyLevel) {
		this.spicyLevel = spicyLevel;
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

    public double getRemainTakeup() {
        return remainTakeup;
    }

    public void setRemainTakeup(double remainTakeup) {
        this.remainTakeup = remainTakeup;
    }

    public double getRemainSend() {
        return remainSend;
    }

    public void setRemainSend(double remainSend) {
        this.remainSend = remainSend;
    }

    public long getCheckoutTime() {
        return checkoutTime;
    }

    public void setCheckoutTime(long checkoutTime) {
        this.checkoutTime = checkoutTime;
    }

    public long getSweepTime() {
        return sweepTime;
    }

    public void setSweepTime(long sweepTime) {
        this.sweepTime = sweepTime;
    }

    public long getSendPortId() {
        return sendPortId;
    }

    public void setSendPortId(long sendPortId) {
        this.sendPortId = sendPortId;
    }

    public int getDivRule() {
        return divRule;
    }

    public void setDivRule(int divRule) {
        this.divRule = divRule;
    }

    public boolean isProductDivRule() {
        return productDivRule;
    }

    public void setProductDivRule(boolean productDivRule) {
        this.productDivRule = productDivRule;
    }

    public boolean isChargeItemEnable() {
        return chargeItemEnable;
    }

    public void setChargeItemEnable(boolean chargeItemEnable) {
        this.chargeItemEnable = chargeItemEnable;
    }

    public boolean isWeightEnabled() {
        return weightEnabled;
    }

    public void setWeightEnabled(boolean weightEnabled) {
        this.weightEnabled = weightEnabled;
    }

    public boolean isSameName(){
    	return this.chargeItemName.equalsIgnoreCase(this.productName);
    }
    
}