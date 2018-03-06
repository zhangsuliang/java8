package com.huofu.module.i5wei.table.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.table.dbrouter.StoreTableRecordOptlogDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * 桌台记录操作日志
 * Created by lixuwei on 17/1/16.
 */
@Table(name = "tb_store_table_record_optlog", dalParser = StoreTableRecordOptlogDbRouter.class)
public class StoreTableRecordOptlog extends AbsEntity {

    @Id
    @Column("tid")
    private long tid;

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
     * 桌台记录ID
     */
    @Column("table_record_id")
    private long tableRecordId;
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
     * 客户端类型
     */
    @Column("client_type")
    private int clientType;
    /**
     * 操作类型
     */
    @Column("opt_type")
    private int optType;
    /**
     * 备注
     */
    @Column("remark")
    private String remark;
    /**
     * 业务时间
     */
    @Column("opt_time")
    private long optTime;
    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
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

    public long getTableRecordId() {
        return tableRecordId;
    }

    public void setTableRecordId(long tableRecordId) {
        this.tableRecordId = tableRecordId;
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

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
    }

    public int getOptType() {
        return optType;
    }

    public void setOptType(int optType) {
        this.optType = optType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getOptTime() {
        return optTime;
    }

    public void setOptTime(long optTime) {
        this.optTime = optTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
