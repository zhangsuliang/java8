package com.huofu.module.i5wei.menu.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import com.huofu.module.i5wei.menu.dbrouter.StoreChargeItemCategoryDbRouter;

import java.util.List;

/**
 * Auto created by i5weitools
 * 收费项目分类管理
 */
@Table(name = "tb_store_charge_item_category", dalParser = StoreChargeItemCategoryDbRouter.class)
public class StoreChargeItemCategory {

    /**
     * 分类ID，全库唯一
     */
	@Id
    @Column("category_id")
    private int categoryId;

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
     * 分类名称
     */
    @Column("name")
    private String name;

    /**
     * 是否删除
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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


	/**
	 * 查询是否有重复的name
	 *
	 * @param name
	 * @param storeChargeItemCategories
	 * @return
	 */
	public boolean canAdd(String name, List<StoreChargeItemCategory> storeChargeItemCategories) {

		for (StoreChargeItemCategory storeChargeItemCategory : storeChargeItemCategories) {
			if (storeChargeItemCategory.getName().equals(name)) {
				return false;
			}
		}

		return true;
	}
}