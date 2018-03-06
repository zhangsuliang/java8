package com.huofu.module.i5wei.meal.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.meal.dbrouter.StoreMealCheckoutRecordDbRouter;

/**
 * 已出餐记录
 */
@Table(name = "tb_store_meal_checkout_record", dalParser = StoreMealCheckoutRecordDbRouter.class)
public class StoreMealCheckoutRecord extends AbsEntity {
	
	private static final long serialVersionUID = -4495060555503722507L;

	/**
     * 自增序列号
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
     * 员工ID
     */
    @Column("staff_id")
    private long staffId;
    
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
     * 出餐口ID
     */
    @Column("port_id")
    private long portId;
    
    /**
     * 营业时段ID
     */
    @Column("time_bucket_id")
    private long timeBucketId;
    
    /**
     * 取餐流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;

    /**
     * 出餐分单
     */
    @Column("checkout_seq")
    private int checkoutSeq;
    
    /**
     * 打包分单
     */
    @Column("packaged_seq")
    private int packagedSeq;
    
    /**
     * 通知客户时间
     */
    @Column("notify_time")
    private long notifyTime;

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

	public long getTimeBucketId() {
		return timeBucketId;
	}

	public void setTimeBucketId(long timeBucketId) {
		this.timeBucketId = timeBucketId;
	}

	public int getTakeSerialNumber() {
		return takeSerialNumber;
	}

	public void setTakeSerialNumber(int takeSerialNumber) {
		this.takeSerialNumber = takeSerialNumber;
	}

	public int getCheckoutSeq() {
		return checkoutSeq;
	}

	public void setCheckoutSeq(int checkoutSeq) {
		this.checkoutSeq = checkoutSeq;
	}

	public int getPackagedSeq() {
		return packagedSeq;
	}

	public void setPackagedSeq(int packagedSeq) {
		this.packagedSeq = packagedSeq;
	}

	public long getNotifyTime() {
		return notifyTime;
	}

	public void setNotifyTime(long notifyTime) {
		this.notifyTime = notifyTime;
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

}
