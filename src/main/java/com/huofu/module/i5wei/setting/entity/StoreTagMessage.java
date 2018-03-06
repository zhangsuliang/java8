package com.huofu.module.i5wei.setting.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

@Table(name="tb_store_tag_message",dalParser=BaseDefaultStoreDbRouter.class)
public class StoreTagMessage extends AbsEntity {
	/**
     * 标签ID
     */
    @Id
    @Column("tag_id")
    private long tagId;

    /**
     * 店铺ID
     */
    @Column("store_id")
    private long storeId;

    /**
     * 商户ID
     */
    @Column("merchant_id")
    private int merchantId;

    /**
     * 标签信息
     */
    @Column("tag_message")
    private String tagMessage;
    
    /**
     * 标签类型，0：退款/退菜原因，1：整单备注
     */
    @Column("tag_type")
    private int tagType;
    
    /**
     * 标签顺序
     */
    @Column("tag_sort")
    private int tagSort;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 修改时间
     */
    @Column("update_time")
    private long updateTime;

	public long getTagId() {
		return tagId;
	}

	public void setTagId(long tagId) {
		this.tagId = tagId;
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

	public String getTagMessage() {
		return tagMessage;
	}

	public void setTagMessage(String tagMessage) {
		this.tagMessage = tagMessage;
	}

	public int getTagType() {
		return tagType;
	}

	public void setTagType(int tagType) {
		this.tagType = tagType;
	}

	public int getTagSort() {
		return tagSort;
	}

	public void setTagSort(int tagSort) {
		this.tagSort = tagSort;
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
}
