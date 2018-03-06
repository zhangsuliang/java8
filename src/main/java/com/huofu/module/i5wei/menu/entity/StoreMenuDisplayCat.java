package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.dbrouter.StoreMenuDisplayCatDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 显示分类
 */
@Table(name = "tb_store_menu_display_cat", dalParser = StoreMenuDisplayCatDbRouter.class)
public class StoreMenuDisplayCat extends AbsEntity {

    /**
     * 分类id
     */
    @Id
    @Column("display_cat_id")
    private long displayCatId;

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
     * 分类名称
     */
    @Column("name")
    private String name;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 营业时段id
     */
    @Column("time_bucket_id")
    private long timeBucketId;

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getDisplayCatId() {
        return displayCatId;
    }

    public void setDisplayCatId(long displayCatId) {
        this.displayCatId = displayCatId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}