package com.huofu.module.i5wei.table.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.meal.entity.StoreMealTakeup;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.dbrouter.StoreTableRecordDbRouter;
import com.huofu.module.i5wei.table.service.TableRecordPayStatusResult;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.meal.StoreSendTypeEnum;
import huofucore.facade.i5wei.table.PayStatusEnum;
import huofucore.facade.i5wei.table.TableRecordStatusEnum;

import java.util.List;

/**
 * 桌台记录
 * @author licheng7
 * 2016年4月27日 上午9:26:20
 */
@Table(name = "tb_store_table_record", dalParser = StoreTableRecordDbRouter.class)
public class StoreTableRecord extends AbsEntity {

	private static final long serialVersionUID = 1L;

	/**
     * 桌台记录ID（主键全库唯一）
     */
	@Id
    @Column("table_record_id")
    private long tableRecordId;

    /**
     * 商编
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺id
     */
    @Column("store_id")
    private long storeId;

    /**
     * 所在桌台
     */
    @Column("table_id")
    private long tableId;

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
     * 主订单id
     */
    @Column("order_id")
    private String orderId;

    /**
     * 拼桌序号
     */
    @Column("table_record_seq")
    private int tableRecordSeq;

    /**
     * 开台时间
     */
    @Column("table_record_time")
    private long tableRecordTime;

    /**
     * 桌台记录状态:0=等待点餐,1=等待上菜,2=上菜中,3=菜上齐,4=已结账,5=已清台,6=结账中,7=结账失败
     */
    @Column("table_record_status")
    private int tableRecordStatus;

    /**
     * 付款状态:0=未付,1=部分支付,2=全额支付(付清),3=有退款
     */
    @Column("pay_status")
    private int payStatus;

    /**
     * 就餐人数
     */
    @Column("customer_traffic")
    private int customerTraffic;

    /**
     * 默认桌台服务员
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 开台服务员
     */
    @Column("create_table_staff_id")
    private long createTableStaffId;

    /**
     * 结账服务员
     */
    @Column("settle_staff_id")
    private long settleStaffId;

    /**
     * 清台服务员
     */
    @Column("clear_table_staff_id")
    private long clearTableStaffId;

    /**
     * 开台用户id
     */
    @Column("create_table_user_id")
    private long createTableUserId;

    /**
     * 结账用户id
     */
    @Column("settle_user_id")
    private long settleUserId;

    /**
     * 点餐开始时间
     */
    @Column("order_time")
    private long orderTime;

    /**
     * 首次上菜时间
     */
    @Column("first_up_time")
    private long firstUpTime;

    /**
     * 菜上齐时间
     */
    @Column("last_up_time")
    private long lastUpTime;

    /**
     * 清台时间
     */
    @Column("clear_table_time")
    private long clearTableTime;

    /**
     * 合台的目标开台记录ID
     */
    @Column("merge_table_record_id")
    private long mergeTableRecordId;

    /**
     * 合台时间
     */
    @Column("merge_table_time")
    private long mergeTableTime;

    /**
     * 桌台记录创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 区域id
     */
    @Column("area_id")
    private long areaId;

    /**
     * 结账时间
     */
    @Column("settle_time")
    private long settleTime;
    
    /**
     * 整单折扣比例
     */
    @Column("discount_pro")
    private double discountPro;
    
    /**
     * 整单减免金额
     */
    @Column("discount_amount")
    private long discountAmount;
    
    /**
     * 桌台名称
     */
    @Column("table_name")
    private String tableName;
    
    /**
     * 区域名称
     */
    @Column("area_name")
    private String areaName;
    
    /**
     * 取餐流水号
     */
    @Column("take_serial_number")
    private int takeSerialNumber;
    
    /**
     * 合台服务员
     */
    @Column("merge_staff_id")
    private long mergeStaffId;
    
    /**
     * 转台服务员
     */
    @Column("transfer_staff_id")
    private long transferStaffId;
    
    /**
     * 终端类型
     */
    @Column("client_type")
    private int clientType;
    
    /**
     * 桌台原价
     */
    @Column("table_price")
    private long tablePrice;

    /**
     * 桌台已收取台位费
     */
    @Column("table_fee")
    private long tableFee;

    /**
     * 桌台已享受折扣金额
     */
    @Column("already_discount_amount")
    private long alreadyDiscountAmount;

    /**
     * 已退菜金额
     */
    @Column("refund_charge_item_price")
    private long refundChargeItemPrice;

    /**
     * 应付金额
     */
    @Column("pay_able_amount")
    private long payAbleAmount;

    /**
     * 已付金额
     */
    @Column("paid_amount")
    private long paidAmount;

    /**
     * 以退款金额
     */
    @Column("refund_amount")
    private long refundAmount;

    /**
     * 待出餐数量
     */
    @Column("meal_takeup_num")
    private int mealTakeupNum;

    /**
     * 已出餐数量
     */
    @Column("meal_checkout_num")
    private int mealCheckoutNum;
    
    /**
     * 是否菜上齐
     */
    private boolean allTakeOut;
    
    /**
     * 应付台位费
     */
    @Column("payable_table_fee")
    private long payAbleTableFee;
    
    /**
     * 总退款金额
     */
    @Column("total_refund_amount")
    private long totalRefundAmount;

	/**
	 * 起菜状态
	 */
	@Column("send_type")
	private int sendType = StoreSendTypeEnum.TAKE_ORDER.getValue();
	
	/**
	 * 收银员减免（抹零）
	 */
	@Column("staff_derate")
	private long staffDerate;

	/**
	 * 减免台位费
	 */
	@Column("reduction_table_fee")
	private long reductionTableFee;

    private TableRecordPayStatusResult tableRecordPayStatusInfo;
    
	/**
	 * 桌台记录主订单
	 */
    private StoreOrder masterStoreOrder;

	/**
	 * 桌台记录子订单的待出餐列表
	 */
	private List<StoreMealTakeup> storeMealTakeups;

	public List<StoreMealTakeup> getStoreMealTakeups() {
		return storeMealTakeups;
	}

	public void setStoreMealTakeups(List<StoreMealTakeup> storeMealTakeups) {
		this.storeMealTakeups = storeMealTakeups;
	}

	public int getSendType() {
		return sendType;
	}

	public void setSendType(int sendType) {
		this.sendType = sendType;
	}

	public long getTableRecordId() {
		return tableRecordId;
	}

	public void setTableRecordId(long tableRecordId) {
		this.tableRecordId = tableRecordId;
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

	public long getTableId() {
		return tableId;
	}

	public void setTableId(long tableId) {
		this.tableId = tableId;
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

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public int getTableRecordSeq() {
		return tableRecordSeq;
	}

	public void setTableRecordSeq(int tableRecordSeq) {
		this.tableRecordSeq = tableRecordSeq;
	}

	public long getTableRecordTime() {
		return tableRecordTime;
	}

	public void setTableRecordTime(long tableRecordTime) {
		this.tableRecordTime = tableRecordTime;
	}

	public int getTableRecordStatus() {
		return tableRecordStatus;
	}

	public void setTableRecordStatus(int tableRecordStatus) {
		this.tableRecordStatus = tableRecordStatus;
	}

	public int getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(int payStatus) {
		this.payStatus = payStatus;
	}

	public int getCustomerTraffic() {
		return customerTraffic;
	}

	public void setCustomerTraffic(int customerTraffic) {
		this.customerTraffic = customerTraffic;
	}

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
	}

	public long getCreateTableStaffId() {
		return createTableStaffId;
	}

	public void setCreateTableStaffId(long createTableStaffId) {
		this.createTableStaffId = createTableStaffId;
	}

	public long getSettleStaffId() {
		return settleStaffId;
	}

	public void setSettleStaffId(long settleStaffId) {
		this.settleStaffId = settleStaffId;
	}

	public long getClearTableStaffId() {
		return clearTableStaffId;
	}

	public void setClearTableStaffId(long clearTableStaffId) {
		this.clearTableStaffId = clearTableStaffId;
	}

	public long getCreateTableUserId() {
		return createTableUserId;
	}

	public void setCreateTableUserId(long createTableUserId) {
		this.createTableUserId = createTableUserId;
	}

	public long getSettleUserId() {
		return settleUserId;
	}

	public void setSettleUserId(long settleUserId) {
		this.settleUserId = settleUserId;
	}

	public long getOrderTime() {
		return orderTime;
	}

	public void setOrderTime(long orderTime) {
		this.orderTime = orderTime;
	}

	public long getFirstUpTime() {
		return firstUpTime;
	}

	public void setFirstUpTime(long firstUpTime) {
		this.firstUpTime = firstUpTime;
	}

	public long getLastUpTime() {
		return lastUpTime;
	}

	public void setLastUpTime(long lastUpTime) {
		this.lastUpTime = lastUpTime;
	}

	public long getClearTableTime() {
		return clearTableTime;
	}

	public void setClearTableTime(long clearTableTime) {
		this.clearTableTime = clearTableTime;
	}

	public long getMergeTableRecordId() {
		return mergeTableRecordId;
	}

	public void setMergeTableRecordId(long mergeTableRecordId) {
		this.mergeTableRecordId = mergeTableRecordId;
	}

	public long getMergeTableTime() {
		return mergeTableTime;
	}

	public void setMergeTableTime(long mergeTableTime) {
		this.mergeTableTime = mergeTableTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public long getAreaId() {
		return areaId;
	}

	public void setAreaId(long areaId) {
		this.areaId = areaId;
	}

	public long getSettleTime() {
		return settleTime;
	}

	public void setSettleTime(long settleTime) {
		this.settleTime = settleTime;
	}

	public double getDiscountPro() {
		return discountPro;
	}

	public void setDiscountPro(double discountPro) {
		this.discountPro = discountPro;
	}

	public long getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(long discountAmount) {
		this.discountAmount = discountAmount;
	}
	
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public boolean isAllTakeOut() {
		return allTakeOut;
	}

	public void setAllTakeOut(boolean allTakeOut) {
		this.allTakeOut = allTakeOut;
	}

	public int getTakeSerialNumber() {
		return takeSerialNumber;
	}

	public void setTakeSerialNumber(int takeSerialNumber) {
		this.takeSerialNumber = takeSerialNumber;
	}

	public long getMergeStaffId() {
		return mergeStaffId;
	}

	public void setMergeStaffId(long mergeStaffId) {
		this.mergeStaffId = mergeStaffId;
	}

	public long getTransferStaffId() {
		return transferStaffId;
	}

	public void setTransferStaffId(long transferStaffId) {
		this.transferStaffId = transferStaffId;
	}

	public int getClientType() {
		return clientType;
	}

	public void setClientType(int clientType) {
		this.clientType = clientType;
	}

	public long getTablePrice() {
		return tablePrice;
	}

	public void setTablePrice(long tablePrice) {
		this.tablePrice = tablePrice;
	}

	public long getTableFee() {
		return tableFee;
	}

	public void setTableFee(long tableFee) {
		this.tableFee = tableFee;
	}

	public long getAlreadyDiscountAmount() {
		return alreadyDiscountAmount;
	}

	public void setAlreadyDiscountAmount(long alreadyDiscountAmount) {
		this.alreadyDiscountAmount = alreadyDiscountAmount;
	}

	public long getRefundChargeItemPrice() {
		return refundChargeItemPrice;
	}

	public void setRefundChargeItemPrice(long refundChargeItemPrice) {
		this.refundChargeItemPrice = refundChargeItemPrice;
	}

	public long getPayAbleAmount() {
		return payAbleAmount;
	}

	public void setPayAbleAmount(long payAbleAmount) {
		this.payAbleAmount = payAbleAmount;
	}

	public long getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(long paidAmount) {
		this.paidAmount = paidAmount;
	}

	public long getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(long refundAmount) {
		this.refundAmount = refundAmount;
	}

	public int getMealTakeupNum() {
		return mealTakeupNum;
	}

	public void setMealTakeupNum(int mealTakeupNum) {
		this.mealTakeupNum = mealTakeupNum;
	}

	public int getMealCheckoutNum() {
		return mealCheckoutNum;
	}

	public void setMealCheckoutNum(int mealCheckoutNum) {
		this.mealCheckoutNum = mealCheckoutNum;
	}

	public TableRecordPayStatusResult getTableRecordPayStatusInfo() {
		return tableRecordPayStatusInfo;
	}

	public void setTableRecordPayStatusInfo(
			TableRecordPayStatusResult tableRecordPayStatusInfo) {
		this.tableRecordPayStatusInfo = tableRecordPayStatusInfo;
	}

	public long getPayAbleTableFee() {
		return payAbleTableFee;
	}

	public void setPayAbleTableFee(long payAbleTableFee) {
		this.payAbleTableFee = payAbleTableFee;
	}
	
	public long getStaffDerate() {
		return staffDerate;
	}

	public void setStaffDerate(long staffDerate) {
		this.staffDerate = staffDerate;
	}

    public long getReductionTableFee() {
        return reductionTableFee;
    }

    public void setReductionTableFee(long reductionTableFee) {
        this.reductionTableFee = reductionTableFee;
    }

    public boolean isClearTable () {
		if (this.tableRecordStatus == TableRecordStatusEnum.CLEAR_TABLE.getValue()) {
			return true;
		}
		return false;
	}
	
	public boolean isSettleMent () {
		if (this.tableRecordStatus == TableRecordStatusEnum.SETTLEMENT.getValue()) {
			return true;
		}
		return false;
	}
	
	public boolean isSettling () {
		if (this.tableRecordStatus == TableRecordStatusEnum.SETTLING.getValue()) {
			return true;
		}
		return false;
	}
	
	public boolean isSettleFail () {
		if (this.tableRecordStatus == TableRecordStatusEnum.SETTLE_FAIL.getValue()) {
			return true;
		}
		return false;
	}
	
	public boolean isWaitMeal () {
		if (this.tableRecordStatus == TableRecordStatusEnum.WAIT_MEAL.getValue()) {
			return true;
		}
		return false;
	}
	
	public boolean isAllPay () {
		if (this.payStatus == PayStatusEnum.ALL_PAY.getValue()) {
			return true;
		}
		return false;
	}

	public long getTotalRefundAmount() {
		return totalRefundAmount;
	}

	public void setTotalRefundAmount(long totalRefundAmount) {
		this.totalRefundAmount = totalRefundAmount;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public StoreOrder getMasterStoreOrder() {
		return masterStoreOrder;
	}

	public void setMasterStoreOrder(StoreOrder masterStoreOrder) {
		this.masterStoreOrder = masterStoreOrder;
	}

	/**
	 * 获取桌台记录的实际支付金额
	 */
	public long getStoreTableActualPrice(List<StoreOrder> subStoreOrders, StoreOrder mainStoreOrder){
		if(subStoreOrders == null || subStoreOrders.isEmpty() ){
			return mainStoreOrder.getActualPrice();
		}
		long acturalPrice = 0;
		for(StoreOrder storeOrder : subStoreOrders){
			acturalPrice += storeOrder.getActualPrice();
		}
		acturalPrice += mainStoreOrder.getActualPrice();
		return acturalPrice;
	}

	/**
	 * 获取桌台记录的实际退款金额
	 */
	public long getStoreTableActualRefundAmount(List<TableRecordBatchRefundRecord> tableRecordBatchRefundRecords){
		if(tableRecordBatchRefundRecords == null || tableRecordBatchRefundRecords.isEmpty()){
			return 0;
		}
		long actualRefundAmount = 0;
		for(TableRecordBatchRefundRecord tableRecordBatchRefundRecord : tableRecordBatchRefundRecords){
			actualRefundAmount += tableRecordBatchRefundRecord.getActualRefundAmount();
		}
		return actualRefundAmount;
	}

    /**
     * 实际支付的台位费
     * @return
     */
    public long getRealPayTableFee() {
        long realTableFee = this.payAbleTableFee - this.reductionTableFee;
        if (realTableFee < 0) {
            return 0;
        }
        return realTableFee;
    }
}
