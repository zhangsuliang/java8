package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.order.dbrouter.StoreOrderSubitemDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_order_subitem", dalParser = StoreOrderSubitemDbRouter.class)
public class StoreOrderSubitem {

    /**
     * 主键，订单快照自增ID，没有业务意义
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
     * 就餐日期
     */
    @Column("repast_date")
    private long repastDate;

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
     * 规格：单位（份、碗、个。。。）
     */
    @Column("unit")
    private String unit;

    /**
     * 订单子项目的对应产品组成数量：一份套餐对应（土豆丝*1，肉夹馍*2，例汤*1），对应产品肉夹馍的组成是2
     */
    @Column("amount")
    private double amount;

    /**
     * 订单子项目对应的产品数量，一份套餐对应（土豆丝*1，肉夹馍*2，例汤*1），子项目的预定数量是3，对应订单中子项目产品肉夹馍的数量是2*3=6
     */
    @Column("amount_order")
    private double amountOrder;
    
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
     * 产品成本
     */
    @Column("prime_cost")
    private long primeCost;
    
    /**
     * 库存回退数量
     */
    @Column("inv_quit_amount")
    private double invQuitAmount;
    
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

	public long getPrimeCost() {
		return primeCost;
	}

	public void setPrimeCost(long primeCost) {
		this.primeCost = primeCost;
	}

	public double getInvQuitAmount() {
		return invQuitAmount;
	}

	public void setInvQuitAmount(double invQuitAmount) {
		this.invQuitAmount = invQuitAmount;
	}

}