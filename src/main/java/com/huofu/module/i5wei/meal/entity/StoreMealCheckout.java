package com.huofu.module.i5wei.meal.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.meal.dbrouter.StoreMealCheckoutDbRouter;

/**
 * 已出餐，为避免tid与待出餐表冲突此对象不加tid
 */
@Table(name = "tb_store_meal_checkout", dalParser = StoreMealCheckoutDbRouter.class)
public class StoreMealCheckout extends AbsEntity {
	
	private static final long serialVersionUID = -8578039016583749138L;

	@Id
	@Column("tid")
	private long checkoutTid;//add by lizhijun 为防止与待出餐列表中的tid冲突，采用不用的命名方式
	
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
     * 员工ID
     */
    @Column("staff_id")
    private long staffId;

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
     * 出餐副本ID
     */
    @Column("appcopy_id")
    private long appcopyId;
    
    /**
     * 出餐方式：0=手动，1=自动出餐
     */
    @Column("checkout_type")
    private int checkoutType;

    /**
     * 取餐流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;

    /**
     * 取餐流水号，序号（1、2、3。。。）
     */
    @Column("take_serial_seq")
    private int takeSerialSeq;
    
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
     * 出餐数量
     */
    @Column("amount_checkout")
    private double amountCheckout;
    
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
     * 0=未打印，1=已打印
     */
    @Column("printed")
    private boolean printed;

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
     * 是否退菜：0=否，1=是
     */
    @Column("refund_meal")
    private boolean refundMeal;
    
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
     * 待出餐创建时间
     */
    @Column("takeup_time")
    private long takeupTime;
    
    /**
     * 传菜口Id
     */
    @Column("send_port_id")
    private long sendPortId;
    
	public long getCheckoutTid() {
        return checkoutTid;
    }

    public void setCheckoutTid(long checkoutTid) {
        this.checkoutTid = checkoutTid;
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

    public long getAppcopyId() {
		return appcopyId;
	}

	public void setAppcopyId(long appcopyId) {
		this.appcopyId = appcopyId;
	}

	public int getCheckoutType() {
		return checkoutType;
	}

	public void setCheckoutType(int checkoutType) {
		this.checkoutType = checkoutType;
	}

	public int getTakeSerialNumber() {
        return takeSerialNumber;
    }

    public void setTakeSerialNumber(int takeSerialNumber) {
        this.takeSerialNumber = takeSerialNumber;
    }

    public int getTakeSerialSeq() {
        return takeSerialSeq;
    }

    public void setTakeSerialSeq(int takeSerialSeq) {
        this.takeSerialSeq = takeSerialSeq;
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
    
    public double getAmountCheckout() {
		return amountCheckout;
	}
    
	public void setAmountCheckout(double amountCheckout) {
		this.amountCheckout = amountCheckout;
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

	public boolean isPrinted() {
		return printed;
	}

	public void setPrinted(boolean printed) {
		this.printed = printed;
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
    
	public int getSpicyLevel() {
		return spicyLevel;
	}

	public void setSpicyLevel(int spicyLevel) {
		this.spicyLevel = spicyLevel;
	}

	public boolean isRefundMeal() {
		return refundMeal;
	}

	public void setRefundMeal(boolean refundMeal) {
		this.refundMeal = refundMeal;
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

	public long getTakeupTime() {
		return takeupTime;
	}

	public void setTakeupTime(long takeupTime) {
		this.takeupTime = takeupTime;
	}

    public long getSendPortId() {
        return sendPortId;
    }

    public void setSendPortId(long sendPortId) {
        this.sendPortId = sendPortId;
    }

}