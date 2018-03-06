package com.huofu.module.i5wei.inventory.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofuhelper.util.NumberUtil;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.inventory.dbrouter.StoreInventoryDateDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_inventory_date", dalParser = StoreInventoryDateDbRouter.class)
public class StoreInventoryDate extends AbsEntity {

	/**
	 * 指定日期库存ID，全库唯一主键
	 */
	@Id
	@Column("inv_date_id")
	private long invDateId;

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
	 * 日常周营业时间ID，按周天库存则为0
	 */
	@Column("time_bucket_id")
	private long timeBucketId;

	/**
	 * 指定日期
	 */
	@Column("select_date")
	private long selectDate;

	/**
	 * 产品周期营业库存ID
	 */
	@Column("inv_week_id")
	private long invWeekId;

	/**
	 * 0=未知，1=周期联动，2=手动库存
	 */
	@Column("modified")
	private int modified;

	/**
	 * 产品ID
	 */
	@Column("product_id")
	private long productId;

	/**
	 * 计划数量
	 */
	@Column("amount_plan")
	private double amountPlan;

	/**
	 * 常规库存数量（周期库存实际供应量，或固定库存实际剩余量）
	 */
	@Column("amount")
	private double amount;

	/**
	 * 已预定数量
	 */
	@Column("amount_order")
	private double amountOrder;

	/**
	 * 已取餐数量
	 */
	@Column("amount_take")
	private double amountTake;

	/**
	 * 待出餐个数
	 */
	@Column("amount_takeup")
	private double amountTakeup;

	/**
	 * 已出餐个数
	 */
	@Column("amount_checkout")
	private double amountCheckout;
	
	/**
	 * 本时段是否估清：0=未估清，1=已估清
	 */
	@Column("nothingness")
	private boolean nothingness;

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
	 * 全部预定数量
	 */
	private double amountOrderTotal;

	/**
	 * 产品
	 */
	private StoreProduct storeProduct;

	/**
	 * 营业时段
	 */
	private StoreTimeBucket storeTimeBucket;

	/**
	 * 产品是否在销售中，默认未false
	 */
	private boolean inSell = false;

	public long getInvDateId() {
		return invDateId;
	}

	public void setInvDateId(long invDateId) {
		this.invDateId = invDateId;
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

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}

	public long getSelectDate() {
		return selectDate;
	}

	public void setSelectDate(long selectDate) {
		this.selectDate = selectDate;
	}

	public long getInvWeekId() {
		return invWeekId;
	}

	public void setInvWeekId(long invWeekId) {
		this.invWeekId = invWeekId;
	}

	public void setInvWeekId(StoreInventoryWeek storeInventoryWeek) {
		if (storeInventoryWeek == null) {
			this.invWeekId = 0;
			return;
		}
		if (storeInventoryWeek.getParentWeekId() > 0) {
			this.invWeekId = storeInventoryWeek.getParentWeekId();
		} else {
			this.invWeekId = storeInventoryWeek.getInvWeekId();
		}
	}

	public int getModified() {
		return modified;
	}

	public void setModified(int modified) {
		this.modified = modified;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public double getAmountPlan() {
		return amountPlan;
	}

	public void setAmountPlan(double amountPlan) {
		if (amountPlan < 0) {
			amountPlan = 0D;
		}
		this.amountPlan = amountPlan;
	}

	public double getAmount() {
		if (storeProduct == null) {
			return amount;
		}
		if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK.getValue() || storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()) {
			// 周期库存的供应量不能少于已出餐数量
			if (amount < this.getAmountCheckout()) {
				amount = this.getAmountCheckout();
			}
		}
		return amount;
	}

	public void setAmount(double amount) {
		if (amount < 0) {
			amount = 0D;
		}
		this.amount = amount;
	}

	public double getAmountOrder() {
		return amountOrder;
	}

	public void setAmountOrder(double amountOrder) {
		if (amountOrder < 0) {
			amountOrder = 0D;
		}
		this.amountOrder = amountOrder;
	}

	public double getAmountTake() {
		return amountTake;
	}

	public void setAmountTake(double amountTake) {
		if (amountTake < 0) {
			amountTake = 0D;
		}
		this.amountTake = amountTake;
	}

	public double getAmountTakeup() {
		return amountTakeup;
	}

	public void setAmountTakeup(double amountTakeup) {
		if (amountTakeup < 0) {
			amountTakeup = 0D;
		}
		this.amountTakeup = amountTakeup;
	}

	public double getAmountCheckout() {
		return amountCheckout;
	}

	public void setAmountCheckout(double amountCheckout) {
		if (amountCheckout < 0) {
			amountCheckout = 0D;
		}
		this.amountCheckout = amountCheckout;
	}

	public boolean isNothingness() {
		return nothingness;
	}

	public void setNothingness(boolean nothingness) {
		this.nothingness = nothingness;
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

	public double getAmountOrderTotal() {
		return amountOrderTotal;
	}

	public void setAmountOrderTotal(double amountOrderTotal) {
		this.amountOrderTotal = amountOrderTotal;
	}

	public double getAmountRemain() {
		double amountRemain = 0D;
		if (storeProduct == null) {
			return amountRemain;
		}
		if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK.getValue() || storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()) {
			amountRemain = NumberUtil.sub(this.getAmount(), this.getAmountCheckout());// 周期库存实际供应量-已出餐
		} else if (storeProduct.getInvType() == ProductInvTypeEnum.FIXED.getValue()) {
			amountRemain = NumberUtil.add(this.getAmount(), this.getAmountTakeup());// 固定库存实际剩余+待出餐
		}
		if (amountRemain < 0) {
			amountRemain = 0;
		}
		return amountRemain;
	}

	/**
	 * 剩余可销售数量
	 */
	public double getAmountCanSell() {
		double canSell = 0;
		if (storeProduct == null) {
			return canSell;
		}
		if (storeProduct.getInvType() == ProductInvTypeEnum.WEEK.getValue() || storeProduct.getInvType() == ProductInvTypeEnum.WEEK_DAY.getValue()) {
			double remain = NumberUtil.sub(this.getAmountRemain(), this.getAmountTakeup());// 实际剩余量=后厨剩余量-待出餐数量
			canSell = NumberUtil.sub(remain, this.getAmountOrder());// 可售卖量=实际剩余量-当前预定
		} else if (storeProduct.getInvType() == ProductInvTypeEnum.FIXED.getValue()) {
			canSell = NumberUtil.sub(this.getAmount(), this.getAmountOrderTotal());// 可售卖量=实际剩余量-总预定
		}
		if (canSell < 0) {
			canSell = 0;
		}
		return canSell;
	}

	public StoreProduct getStoreProduct() {
		return storeProduct;
	}

	public void setStoreProduct(StoreProduct storeProduct) {
		this.storeProduct = storeProduct;
	}

	public StoreTimeBucket getStoreTimeBucket() {
		return storeTimeBucket;
	}

	public void setStoreTimeBucket(StoreTimeBucket storeTimeBucket) {
		this.storeTimeBucket = storeTimeBucket;
	}

	public boolean isInSell() {
		return inSell;
	}

	public void setInSell(boolean inSell) {
		this.inSell = inSell;
	}

}