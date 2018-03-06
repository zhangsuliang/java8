package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.order.dbrouter.StoreOrderInvoiceDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.merchant.invoice.MerchantInvoiceStatusEnum;
import huofuhelper.util.DataUtil;
import org.joda.time.MutableDateTime;

/**
 * Auto created by i5weitools
 * 订单发票
 */
@Table(name = "tb_store_order_invoice", dalParser = StoreOrderInvoiceDbRouter.class)
public class StoreOrderInvoice extends AbsEntity {

    /**
     * 订单ID
     */
	@Id
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
     * 服务员工ID
     */
    @Column("staff_id")
    private long staffId;

    /**
     * 下单用户ID
     */
    @Column("user_id")
    private long userId;

    /**
     * 就餐日期，即菜单所在日期
     */
    @Column("repast_date")
    private long repastDate;

    /**
     * 营业时间ID
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 订单折扣前的总金额
     */
    @Column("order_price")
    private long orderPrice;

    /**
     * 订单的实际支付金额，不包含各种折扣，不包含优惠
     */
    @Column("actual_price")
    private long actualPrice;

    /**
     * 发票金额
     */
    @Column("invoice_price")
    private long invoicePrice;

    /**
     * 发票抬头
     */
    @Column("invoice_title")
    private String invoiceTitle;

    /**
     * 发票号
     */
    @Column("invoice_no")
    private String invoiceNo;

    /**
     * 发票开具时间
     */
    @Column("invoice_time")
    private long invoiceTime;

    /**
     * 备注
     */
    @Column("remark")
    private String remark;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;
    
	/**
	 * 发票请求流水号
	 */
    @Column("request_id")
	private String requestId;
    
	/**
	 * 发票代码
	 */
    @Column("invoice_code")
	private String invoiceCode;
    
	/**
	 * 校验码
	 */
    @Column("check_code")
	private String checkCode;
	
	/**
	 * PDF下载地址
	 */
    @Column("invoice_pdf_url")
	private String invoicePDFUrl;

	/**
	 * 发票开具跳转地址
	 * @return
     */
	@Column("redirect_url")
	private String redirectUrl;

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
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

	public long getOrderPrice() {
		return orderPrice;
	}

	public void setOrderPrice(long orderPrice) {
		this.orderPrice = orderPrice;
	}
	
	public long getActualPrice() {
		return actualPrice;
	}

	public void setActualPrice(long actualPrice) {
		this.actualPrice = actualPrice;
	}

	public long getInvoicePrice() {
		return invoicePrice;
	}

	public void setInvoicePrice(long invoicePrice) {
		this.invoicePrice = invoicePrice;
	}

	public String getInvoiceTitle() {
		return invoiceTitle;
	}

	public void setInvoiceTitle(String invoiceTitle) {
		this.invoiceTitle = invoiceTitle;
	}

	public String getInvoiceNo() {
		return invoiceNo;
	}

	public void setInvoiceNo(String invoiceNo) {
		this.invoiceNo = invoiceNo;
	}

	public long getInvoiceTime() {
		return invoiceTime;
	}

	public void setInvoiceTime(long invoiceTime) {
		this.invoiceTime = invoiceTime;
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

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getInvoiceCode() {
		return invoiceCode;
	}

	public void setInvoiceCode(String invoiceCode) {
		this.invoiceCode = invoiceCode;
	}

	public String getCheckCode() {
		return checkCode;
	}

	public void setCheckCode(String checkCode) {
		this.checkCode = checkCode;
	}

	public String getInvoicePDFUrl() {
		return invoicePDFUrl;
	}

	public void setInvoicePDFUrl(String invoicePDFUrl) {
		this.invoicePDFUrl = invoicePDFUrl;
	}
	
	/**
     * 开发票状态：0=默认，1=处理中，2=处理超时，3=完成
     */
	public int getInvoiceStatus() {
		if (DataUtil.isNotEmpty(this.invoicePDFUrl) || DataUtil.isNotEmpty(this.redirectUrl)) {
			return MerchantInvoiceStatusEnum.FINISH.getValue();
		}

		if(this.createTime > 0 && DataUtil.isEmpty(this.invoicePDFUrl) && DataUtil.isEmpty(this.redirectUrl)) {
			// 设置超时时间
			MutableDateTime mdt = new MutableDateTime(System.currentTimeMillis());
			mdt.addMinutes(-2); 
			if (this.createTime >= mdt.getMillis()) {
				return MerchantInvoiceStatusEnum.PROCESSING.getValue();
			}else{
				return MerchantInvoiceStatusEnum.TIMEOUT.getValue();
			}
		}
		return MerchantInvoiceStatusEnum.NONE.getValue();
	}

}