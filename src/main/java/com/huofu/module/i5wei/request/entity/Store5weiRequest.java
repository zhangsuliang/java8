package com.huofu.module.i5wei.request.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.request.dbrouter.Store5weiRequestDbRouter;

/**
 * 请求唯一性表
 */
@Table(name = "tb_store_5wei_request", dalParser = Store5weiRequestDbRouter.class)
public class Store5weiRequest extends AbsEntity {

    /**
     * 唯一主键，请求ID
     */
	@Id
    @Column("request_id")
    private String requestId;

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
     * 业务类型{@link Store5weiRequestBizType}
     */
    @Column("i5wei_biz_type")
    private int i5weiBizType;

    /**
     * 业务ID
     */
    @Column("i5wei_biz_id")
    private String i5weiBizId;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
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

	public int getI5weiBizType() {
		return i5weiBizType;
	}

	public void setI5weiBizType(int i5weiBizType) {
		this.i5weiBizType = i5weiBizType;
	}

	public String getI5weiBizId() {
		return i5weiBizId;
	}

	public void setI5weiBizId(String i5weiBizId) {
		this.i5weiBizId = i5weiBizId;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

}