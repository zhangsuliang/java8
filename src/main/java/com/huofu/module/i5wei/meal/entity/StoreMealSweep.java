package com.huofu.module.i5wei.meal.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.meal.dbrouter.StoreMealSweepDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 
 */
@Table(name = "tb_store_meal_sweep", dalParser = StoreMealSweepDbRouter.class)
public class StoreMealSweep extends AbsEntity {

    /**
     * 划菜主键Id
     */
    @Id
    @Column("tid")
    private long tid;

    /**
     * 订单Id
     */
    @Column("order_id")
    private String orderId;

    /**
     * 商户Id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺Id
     */
    @Column("store_id")
    private long storeId;

    /**
     * 雇员Id
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 用户Id
     */
    @Column("user_id")
    private long userId;

    /**
     * 就餐日期
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 营业时段Id
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 加工档口Id
     */
    @Column("port_id")
    private long portId;

    /**
     * 传菜口Id
     */
    @Column("send_port_id")
    private long sendPortId;

    /**
     * appcopyId
     */
    @Column("appcopy_id")
    private long appcopyId;

    /**
     * 划菜类型
     */
    @Column("sweep_type")
    private int sweepType;

    /**
     * 流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;
    
    /**
     * 是否打包
     */
    @Column("packaged")
    private boolean packaged;
    
    /**
     * 规格：单位（份、碗、个。。。）
     */
    @Column("unit")
    private String unit;

    /**
     * 取餐方式
     */
    @Column("take_mode")
    private int takeMode;

    /**
     * 收费项Id
     */
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 收费项名称
     */
    @Column("charge_item_name")
    private String chargeItemName;

    /**
     * 产品Id
     */
    @Column("product_id")
    private long productId;

    /**
     * 产品名称
     */
    @Column("product_name")
    private String productName;
    
    /**
     * 点餐备注
     */
    @Column("remark")
    private String remark;

    /**
     * 规格数量
     */
    @Column("amount")
    private double amount;
    
    /**
     * 收费项目份数
     */
    @Column("amount_order")
    private double amountOrder;

    /**
     * 显示产品
     */
    @Column("show_products")
    private boolean showProducts;

    /**
     * 是否退菜
     */
    @Column("refund_meal")
    private boolean refundMeal;

    /**
     * 桌台记录Id
     */
    @Column("table_record_id")
    private long tableRecordId;

    /**
     * 划菜数量
     */
    @Column("sweep_meal_amount")
    private double sweepMealAmount;

    /**
     * 划菜时间
     */
    @Column("sweep_meal_time")
    private long sweepMealTime;

    /**
     * 修改时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;
    
    /**
     * 待出餐Id
     */
    @Column("takeup_id")
    private long takeupId;
    
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

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public long getPortId() {
        return portId;
    }

    public void setPortId(long portId) {
        this.portId = portId;
    }

    public long getSendPortId() {
        return sendPortId;
    }

    public void setSendPortId(long sendPortId) {
        this.sendPortId = sendPortId;
    }

    public long getAppcopyId() {
        return appcopyId;
    }

    public void setAppcopyId(long appcopyId) {
        this.appcopyId = appcopyId;
    }

    public int getSweepType() {
        return sweepType;
    }

    public void setSweepType(int sweepType) {
        this.sweepType = sweepType;
    }

    public int getTakeSerialNumber() {
        return takeSerialNumber;
    }

    public void setTakeSerialNumber(int takeSerialNumber) {
        this.takeSerialNumber = takeSerialNumber;
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

    public int getTakeMode() {
        return takeMode;
    }

    public void setTakeMode(int takeMode) {
        this.takeMode = takeMode;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    public boolean isShowProducts() {
        return showProducts;
    }

    public void setShowProducts(boolean showProducts) {
        this.showProducts = showProducts;
    }

    public boolean isRefundMeal() {
        return refundMeal;
    }

    public void setRefundMeal(boolean refundMeal) {
        this.refundMeal = refundMeal;
    }

    public long getTableRecordId() {
        return tableRecordId;
    }

    public void setTableRecordId(long tableRecordId) {
        this.tableRecordId = tableRecordId;
    }

    public double getSweepMealAmount() {
        return sweepMealAmount;
    }

    public void setSweepMealAmount(double sweepMealAmount) {
        this.sweepMealAmount = sweepMealAmount;
    }

    public long getSweepMealTime() {
        return sweepMealTime;
    }

    public void setSweepMealTime(long sweepMealTime) {
        this.sweepMealTime = sweepMealTime;
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

    public long getTakeupId() {
        return takeupId;
    }

    public void setTakeupId(long takeupId) {
        this.takeupId = takeupId;
    }
}