package com.huofu.module.i5wei.order.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huofu.module.i5wei.order.dbrouter.StoreOrderItemDbRouter;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_order_item", dalParser = StoreOrderItemDbRouter.class)
public class StoreOrderItem extends BaseEntity {

    /**
     * 主键，自增ID，没有业务意义
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
     * 是否支持协议企业折扣：0=支持，1=不支持
     */
    @Column("rebate_able")
    private boolean rebateAble;

    /**
     * 数量，支持半份＝0.5
     */
    @Column("amount")
    private double amount;

    /**
     * 规格：单位（份、碗、个。。。）
     */
    @Column("unit")
    private String unit;

    /**
     * 单价
     */
    @Column("price")
    private long price;

    /**
     * 打包数量，支持半份＝0.5
     */
    @Column("packed_amount")
    private double packedAmount;
    
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
     * 打包费单价
     */
    @Column("package_price")
    private long packagePrice;
    
    /**
     * 菜品辣度：1=微辣，2=中辣，3=辣，默认值为0（不辣）
     */
    @Column("spicy_level")
    private int spicyLevel;
    
    /**
     * 是否支持优惠券支付 1=是，0=否，默认为1
     */
    @Column("coupon_supported")
    private boolean couponSupported;
    
    /**
     * 是否计入客数
     */
    @Column("enable_customer_traffic")
    private boolean enableCustomerTraffic;
    
    /**
     * 会员价
     */
    @Column("member_price")
    private long memberPrice;
    
    /**
     * 赠菜免单份数
     */
    @Column("gratis_amount")
    private double gratisAmount;
    
    /**
     * 赠菜免单金额
     */
    @Column("gratis_derate")
    private long gratisDerate;
    
    /**
     * 菜品定价会员价折扣
     */
    @Column("member_derate")
    private long memberDerate;
    
    /**
     * 菜品定价网单折扣
     */
    @Column("internet_derate")
    private long internetDerate;
    
    /**
     * 菜品定价企业折扣
     */
    @Column("enterprise_derate")
    private long enterpriseDerate;
    
    /**
     * 菜品定价首份特价折扣
     */
    @Column("promotion_item_derate")
    private long promotionItemDerate;
    
    /**
     * 菜品定价折扣活动折扣
     */
    @Column("promotion_rebate_derate")
    private long promotionRebateDerate;
    
    /**
     * 菜品定价满减活动折扣
     */
    @Column("promotion_reduce_derate")
    private long promotionReduceDerate;
    
    /**
     * 菜品定价收银员折扣
     */
    @Column("staff_rebate_derate")
    private long staffRebateDerate;
    
    /**
     * 菜品定价优惠券折扣
     */
    @Column("coupon_derate")
    private long couponDerate;
    
    /**
     * 收费项目总减免金额
     */
    @Column("charge_item_derate")
    private long chargeItemDerate;

    /**
	 * 是否开启称重
	 */
	@Column("weight_enabled")
	private boolean weightEnabled;
    
    /**
     * 订单子项明细
     */
    private List<StoreOrderSubitem> storeOrderSubitems;
    
    /**
     * 订单退菜项目
     */
    private List<StoreOrderRefundItem> storeOrderRefundItems;
    
    /**
     * 已经退菜的数量(非打包)
     */
    private double refundChargeItemNumUnPacked;
    
    /**
     * 已经退菜的数量(打包)
     */
    private double refundChargeItemNumPacked;

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

    public boolean isRebateAble() {
        return rebateAble;
    }

    public void setRebateAble(boolean rebateAble) {
        this.rebateAble = rebateAble;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public double getPackedAmount() {
        return packedAmount;
    }

    public void setPackedAmount(double packedAmount) {
        this.packedAmount = packedAmount;
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

	public long getMemberDerate() {
		return memberDerate;
	}

	public void setMemberDerate(long memberDerate) {
		this.memberDerate = memberDerate;
	}

	public double getGratisAmount() {
		return gratisAmount;
	}

	public void setGratisAmount(double gratisAmount) {
		this.gratisAmount = gratisAmount;
	}

	public long getGratisDerate() {
		return gratisDerate;
	}

	public void setGratisDerate(long gratisDerate) {
		this.gratisDerate = gratisDerate;
	}

	public long getInternetDerate() {
		return internetDerate;
	}

	public void setInternetDerate(long internetDerate) {
		this.internetDerate = internetDerate;
	}

	public long getEnterpriseDerate() {
		return enterpriseDerate;
	}

	public void setEnterpriseDerate(long enterpriseDerate) {
		this.enterpriseDerate = enterpriseDerate;
	}

	public long getPromotionItemDerate() {
		return promotionItemDerate;
	}

	public void setPromotionItemDerate(long promotionItemDerate) {
		this.promotionItemDerate = promotionItemDerate;
	}

	public long getPromotionRebateDerate() {
		return promotionRebateDerate;
	}

	public void setPromotionRebateDerate(long promotionRebateDerate) {
		this.promotionRebateDerate = promotionRebateDerate;
	}

	public long getPromotionReduceDerate() {
		return promotionReduceDerate;
	}

	public void setPromotionReduceDerate(long promotionReduceDerate) {
		this.promotionReduceDerate = promotionReduceDerate;
	}

	public long getStaffRebateDerate() {
		return staffRebateDerate;
	}

	public void setStaffRebateDerate(long staffRebateDerate) {
		this.staffRebateDerate = staffRebateDerate;
	}

	public long getChargeItemDerate() {
		return chargeItemDerate;
	}

	public long getCouponDerate() {
		return couponDerate;
	}

	public void setCouponDerate(long couponDerate) {
		this.couponDerate = couponDerate;
	}

	public void setChargeItemDerate(long chargeItemDerate) {
		this.chargeItemDerate = chargeItemDerate;
	}

    public boolean isWeightEnabled() {
        return weightEnabled;
    }

    public void setWeightEnabled(boolean weightEnabled) {
        this.weightEnabled = weightEnabled;
    }

    public List<StoreOrderSubitem> getStoreOrderSubitems() {
        return storeOrderSubitems;
    }

    public void setStoreOrderSubitems(List<StoreOrderSubitem> storeOrderSubitems) {
        this.storeOrderSubitems = storeOrderSubitems;
    }
    
    public Map<Long,StoreOrderSubitem> getProducts() {
    	Map<Long,StoreOrderSubitem> products = new HashMap<Long,StoreOrderSubitem>();
		if (storeOrderSubitems != null) {
			for (StoreOrderSubitem subitem : storeOrderSubitems) {
				products.put(subitem.getProductId(), subitem);
			}
		}
		return products;
	}
    
	public List<StoreOrderRefundItem> getStoreOrderRefundItems() {
		return storeOrderRefundItems;
	}

	public void setStoreOrderRefundItems(List<StoreOrderRefundItem> storeOrderRefundItems) {
		this.storeOrderRefundItems = storeOrderRefundItems;
	}

	public long getPackagePrice() {
		return packagePrice;
	}

	public void setPackagePrice(long packagePrice) {
		this.packagePrice = packagePrice;
	}

	public int getSpicyLevel() {
		return spicyLevel;
	}

	public void setSpicyLevel(int spicyLevel) {
		this.spicyLevel = spicyLevel;
	}

	public boolean isCouponSupported() {
		return couponSupported;
	}

	public void setCouponSupported(boolean couponSupported) {
		this.couponSupported = couponSupported;
	}

	public boolean isEnableCustomerTraffic() {
		return enableCustomerTraffic;
	}

	public void setEnableCustomerTraffic(boolean enableCustomerTraffic) {
		this.enableCustomerTraffic = enableCustomerTraffic;
	}

	public long getMemberPrice() {
		return memberPrice;
	}

	public void setMemberPrice(long memberPrice) {
		this.memberPrice = memberPrice;
	}

	public double getRefundChargeItemNumUnPacked() {
		return refundChargeItemNumUnPacked;
	}

	public void setRefundChargeItemNumUnPacked(double refundChargeItemNumUnPacked) {
		this.refundChargeItemNumUnPacked = refundChargeItemNumUnPacked;
	}

	public double getRefundChargeItemNumPacked() {
		return refundChargeItemNumPacked;
	}

	public void setRefundChargeItemNumPacked(double refundChargeItemNumPacked) {
		this.refundChargeItemNumPacked = refundChargeItemNumPacked;
	}

}