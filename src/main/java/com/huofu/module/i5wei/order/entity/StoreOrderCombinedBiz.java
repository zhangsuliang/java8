package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.order.dbrouter.StoreOrderCombinedBizDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import org.apache.commons.lang.StringUtils;

/**
 * Created by wangxiaoyang on 16/8/19
 */
@Table(name = "tb_store_order_combined_biz", dalParser = StoreOrderCombinedBizDbRouter.class)
public class StoreOrderCombinedBiz extends BaseEntity {

    /**
     * 主键，自增ID，没有业务意义
     */
    @Id
    @Column("tid")
    private long tid;

    /**
     * 交易订单ID
     */
    @Column("order_id")
    private String orderId;

    /**
     * 组合业务id:目前只有买卡支付,这个id就是充值卡交易订单id
     */
    @Column("biz_id")
    private String bizId;

    /**
     * 组合业务类型 ${@link huofucore.facade.i5wei.order.StoreOrderCombinedBizType}
     */
    @Column("biz_type")
    private int bizType;

    /**
     * 商户id
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 店铺id
     */
    @Column("store_id")
    private long storeId;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 业务状态码 0表示没有错误
     */
    @Column("error_code")
    private int errorCode;

    /**
     * 业务错误信息
     */
    @Column("error_msg")
    private String errorMsg;

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

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public int getBizType() {
        return bizType;
    }

    public void setBizType(int bizType) {
        this.bizType = bizType;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        if (StringUtils.isNotEmpty(errorMsg) && errorMsg.length() > 100) {
            errorMsg = errorMsg.substring(0, 100);
        }
        this.errorMsg = errorMsg;
    }

    /**
     * 是否处理失败
     */
    public boolean isFail() {
        if (this.errorCode > 0 || StringUtils.isNotEmpty(this.errorMsg)) {
            return true;
        }
        return false;
    }

    public void init() {
        this.createTime = System.currentTimeMillis();
        this.errorCode = 0;
        this.errorMsg = "";
    }
}
