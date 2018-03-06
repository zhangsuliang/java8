package com.huofu.module.i5wei.table.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.table.dbrouter.StoreTableDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.sharedto.StoreTableStaffDTO;
import huofuhelper.util.DataUtil;

/**
 * Auto created by i5weitools
 * 店铺桌台表
 */
@Table(name = "tb_store_table",dalParser = StoreTableDbRouter.class)
public class StoreTable extends AbsEntity {

    /**
     * 桌台ID
     */
    @Id
    @Column("table_id")
    private long tableId;

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
     * 区域ID
     */
    @Column("area_id")
    private long areaId;

    /**
     * 桌台名称
     */
    @Column("name")
    private String name;

    /**
     * 餐牌号
     */
    @Column("site_number")
    private int siteNumber;

    /**
     * 座位数
     */
    @Column("seat_num")
    private int seatNum;

    /**
     * 默认负责服务员ID
     */
    @Column("staff_id")
    private long staffId;

    /**
     * #bool 是否删除
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 排序号
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

    /**
     * #bool 桌台状态：false=空闲，true=有客（默认false）
     */
    public boolean idle;
    /**
     * 相关有效桌台记录数，大于1则为有拼桌（默认0）
     */
    public long recordNum;
    /**
     * 已落座客人数（默认0）
     */
    public long customerNum;

    /**
     * 桌台二维码
     */
    public String markUrl;

    /**
     * 桌台是否可以被删除
     */
    public boolean tableEnableDeleted;

    public boolean isTableEnableDeleted() {
        return tableEnableDeleted;
    }

    public void setTableEnableDeleted(boolean tableEnableDeleted) {
        this.tableEnableDeleted = tableEnableDeleted;
    }

    public String getMarkUrl() {
        return markUrl;
    }

    public void setMarkUrl(String markUrl) {
        this.markUrl = markUrl;
    }

    private StoreTableStaffDTO storeTableStaffDTO;

    public boolean isIdle() {
        return idle;
    }

    public void setIdle(boolean idle) {
        this.idle = idle;
    }

    public long getRecordNum() {
        return recordNum;
    }

    public void setRecordNum(long recordNum) {
        this.recordNum = recordNum;
    }

    public long getCustomerNum() {
        return customerNum;
    }

    public void setCustomerNum(long customerNum) {
        this.customerNum = customerNum;
    }

    public StoreTableStaffDTO getStoreTableStaffDTO() {
        return storeTableStaffDTO;
    }

    public void setStoreTableStaffDTO(StoreTableStaffDTO storeTableStaffDTO) {
        this.storeTableStaffDTO = storeTableStaffDTO;
    }

    public long getTableId() {
        return tableId;
    }

    public void setTableId(long tableId) {
        this.tableId = tableId;
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

    public long getAreaId() {
        return areaId;
    }

    public void setAreaId(long areaId) {
        this.areaId = areaId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSiteNumber() {
        return siteNumber;
    }

    public void setSiteNumber(int siteNumber) {
        this.siteNumber = siteNumber;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
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

    public void setStoreTableName(){
        if(this.getSiteNumber() > 0 && DataUtil.isEmpty(this.getName())){
            this.setName(this.getSiteNumber() + "号桌");
        }
    }
}