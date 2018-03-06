package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.menu.dbrouter.StoreMenuDisplayDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 菜单显示方案
 */
@Table(name = "tb_store_menu_display", dalParser = StoreMenuDisplayDbRouter.class)
public class StoreMenuDisplay {

    /**
     * 自增id
     */
    @Id
    @Column("tid")
    private long tid;

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
     * 营业时段id
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    /**
     * 收费项目id
     */
    @Column("charge_item_id")
    private long chargeItemId;

    /**
     * 排序标识
     */
    @Column("sort_flag")
    private int sortFlag;

    /**
     * 显示分类id
     */
    @Column("display_cat_id")
    private long displayCatId;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    private StoreMenuDisplayCat storeMenuDisplayCat;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public StoreMenuDisplayCat getStoreMenuDisplayCat() {
        return storeMenuDisplayCat;
    }

    public void setStoreMenuDisplayCat(StoreMenuDisplayCat storeMenuDisplayCat) {
        this.storeMenuDisplayCat = storeMenuDisplayCat;
    }

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

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(long chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public int getSortFlag() {
        return sortFlag;
    }

    public void setSortFlag(int sortFlag) {
        this.sortFlag = sortFlag;
    }

    public long getDisplayCatId() {
        return displayCatId;
    }

    public void setDisplayCatId(long displayCatId) {
        this.displayCatId = displayCatId;
    }
}