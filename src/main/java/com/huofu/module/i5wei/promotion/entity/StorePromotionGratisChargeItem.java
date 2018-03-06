package com.huofu.module.i5wei.promotion.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPrice;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.thrift.serialize.ThriftField;

/**
 * 买赠活动收费项目白名单设置表
 * 
 * @author Suliang
 * @Date 2016-12-21
 */
@SuppressWarnings("all")
@Table(name = "tb_store_promotion_gratis_charge_item", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionGratisChargeItem extends BaseEntity {

	/**
	 * 买赠活动的ID：在创建索引时，promotion_gratis_id和charge_item_id作为联合主键做索引
	 */
	@ThriftField(1)
    @Id
	@Column("promotion_gratis_id")
	private long promotionGratisId;

	/**
	 * 收费项目ID
	 */
	@ThriftField(2)
    @Id(1)
	@Column("charge_item_id")
	private long chargeItemId;

	/**
	 * 商户ID
	 */
	@ThriftField(3)
	@Column("merchant_id")
	private int merchantId;

	/**
	 * 店铺ID
	 */
	@ThriftField(4)
	@Column("store_id")
	private long storeId;

	/**
	 * 创建时间
	 */
	@ThriftField(5)
	@Column("create_time")
	private long createTime;

	private StoreChargeItem storeChargeItem;
	
	private  StoreChargeItemPrice  storeChargeItemPrice;
	
	public void setStoreChargeItemPrice(StoreChargeItemPrice storeChargeItemPrice) {
		this.storeChargeItemPrice = storeChargeItemPrice;
	}

	public StoreChargeItemPrice getStoreChargeItemPrice() {
		return storeChargeItemPrice;
	}

	public void setStoreChargeItem(StoreChargeItem storeChargeItem) {
		this.storeChargeItem = storeChargeItem;
	}

	public StoreChargeItem getStoreChargeItem() {
		return storeChargeItem;
	}

	public long getPromotionGratisId() {
		return promotionGratisId;
	}

	public void setPromotionGratisId(long promotionGratisId) {
		this.promotionGratisId = promotionGratisId;
	}

	public long getChargeItemId() {
		return chargeItemId;
	}

	public void setChargeItemId(long chargeItemId) {
		this.chargeItemId = chargeItemId;
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
	public static List<Long> getIds(List<StorePromotionGratisChargeItem> storePromotionGratisChargeItemList) {
		List<Long> idList = Lists.newArrayList();
		for (StorePromotionGratisChargeItem storePromotionGratisChargeItem : storePromotionGratisChargeItemList) {
			idList.add(storePromotionGratisChargeItem.getChargeItemId());
		}
		return idList;
	}
	
}
