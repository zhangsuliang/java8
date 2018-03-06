package com.huofu.module.i5wei.table.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import java.util.List;

/**
 * Auto created by i5weitools
 * 店铺区域表
 */
@Table(name = "tb_store_area",dalParser = BaseDefaultStoreDbRouter.class)
public class StoreArea extends AbsEntity {

    /**
     * 区域ID
     */
    @Id
    @Column("area_id")
    private long areaId;

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
     * 区域名称
     */
    @Column("area_name")
    private String areaName;

    /**
     * #bool 是否删除
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 排序
     */
    @Column("sort_no")
    private int sortNo;

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

    private List<StoreTable> storeTableList;

    public List<StoreTable> getStoreTableList() {
        return storeTableList;
    }

    public void setStoreTableList(List<StoreTable> storeTableList) {
        this.storeTableList = storeTableList;
    }

    public long getAreaId() {
        return areaId;
    }

    public void setAreaId(long areaId) {
        this.areaId = areaId;
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

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public int getSortNo() {
        return sortNo;
    }

    public void setSortNo(int sortNo) {
        this.sortNo = sortNo;
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

    public static StoreArea createDefault(int merchantId,long storeId){
        StoreArea storeArea = new StoreArea();
        storeArea.setMerchantId(merchantId);
        storeArea.setStoreId(storeId);
        storeArea.setAreaName("大厅");
        storeArea.setDeleted(false);
        storeArea.setCreateTime(System.currentTimeMillis());
        storeArea.setUpdateTime(System.currentTimeMillis());
        storeArea.setSortNo(0);
        return storeArea;
    }

    public static StoreArea _create(int merchantId,long storeId){
        StoreArea storeArea = new StoreArea();
        storeArea.setMerchantId(merchantId);
        storeArea.setStoreId(storeId);
        storeArea.setDeleted(false);
        storeArea.setCreateTime(System.currentTimeMillis());
        storeArea.setUpdateTime(System.currentTimeMillis());
        storeArea.setSortNo(0);
        return storeArea;
    }

}