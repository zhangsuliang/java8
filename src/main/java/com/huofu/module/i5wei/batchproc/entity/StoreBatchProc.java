package com.huofu.module.i5wei.batchproc.entity;

import com.huofu.module.i5wei.batchproc.dbrouter.StoreBatchProcDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.batchproc.StoreBatchProcStatusEnum;
import huofuhelper.module.base.BaseEntity;

/**
 * 批量任务操作
 * Created by akwei on 9/26/16.
 */
@Table(name = "tb_store_batch_proc", dalParser = StoreBatchProcDbRouter.class)
public class StoreBatchProc extends BaseEntity {

    public static final String FIELD_SRC_STORE_ID = "f1";

    public static final String FIELD_TARGET_STORE_ID = "f2";

    @Id
    @Column("batch_id")
    private long batchId;

    @Column("store_id")
    private long storeId;

    @Column("merchant_id")
    private int merchantId;

    @Column
    private int type;

    @Column
    private int status;

    @Column("create_time")
    private long createTime;

    @Column("finish_time")
    private long finishTime;

    @Column
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getBatchId() {
        return batchId;
    }

    public void setBatchId(long batchId) {
        this.batchId = batchId;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public void init4Create() {
        this.status = StoreBatchProcStatusEnum.CREATED.getValue();
        this.finishTime = 0;
        this.createTime = System.currentTimeMillis();
    }

    public void makeFinish() {
        this.snapshot();
        this.status = StoreBatchProcStatusEnum.FINISH.getValue();
        this.finishTime = System.currentTimeMillis();
        this.update();
    }

    public void makeFail() {
        this.snapshot();
        this.status = StoreBatchProcStatusEnum.FAIL.getValue();
        this.update();
    }
}
