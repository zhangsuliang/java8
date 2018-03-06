package com.huofu.module.i5wei.promotion.entity;

import java.util.List;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import com.huofu.module.i5wei.base.I5weiCachePrefix;
import com.huofu.module.i5wei.promotion.util.StorePromotionUtil;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.promotion.StorePromotionStatusEnum;
import huofucore.facade.merchant.staff.StaffDTO2;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.DateUtil;
import huofuhelper.util.cache.CacheDataCodecAble;
import huofuhelper.util.thrift.serialize.ThriftField;

/**
 * 买赠活动设置表
 * 
 * @author Suliang
 * @Date 2016-12-21
 */
@SuppressWarnings("all")
@CacheDataCodecAble(prefix = I5weiCachePrefix.STORE_PROMOTION_GRATIS)
@Table(name = "tb_store_promotion_gratis", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionGratis extends BaseEntity {
	public static int PROMOTION_GRATIS_4_AUTO=1;
	
	public static int PROMOTION_GRATIS_4_ARTIFICIAL=2;
	
	/**
	 * 买赠活动ID（全库唯一主键）
	 */
	@ThriftField(1)
	@Id
	@Column("promotion_gratis_id")
	private long promotionGratisId;

	/**
	 * 店铺ID
	 */
	@ThriftField(2)
	@Column("store_id")
	private long storeId;

	/**
	 * 商户ID
	 */
	@ThriftField(3)
	@Column("merchant_id")
	private int merchantId;

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}
     
	/**
	 * 活动开始时间
	 */
	@ThriftField(4)
	@Column("begin_time")
	private long beginTime;

	/**
	 * 活动结束时间
	 */
	@ThriftField(5)
	@Column("end_time")
	private long endTime;

	/**
	 * 买赠活动的标题
	 */
	@ThriftField(6)
	@Column("title")
	private String title;

	/**
	 * 购买数量
	 */
	@ThriftField(7)
	@Column("purchase_num")
	private int purchaseNum;

	/**
	 * 赠送数量
	 */
	@ThriftField(8)
	@Column("gratis_num")
	private double gratisNum;

	/**
	 * 取餐方式，支持多种取餐方式，并且以英文逗号隔开
	 */
	@ThriftField(9)
	@Column("take_mode_data")
	private String takeModeData;

	/**
	 * #bool 是否支持微信自助
	 */
	@ThriftField(10)
	@Column("wechat_only")
	private boolean wechatOnly;

	/**
	 * #bool 是否支持优惠券
	 */
	@ThriftField(11)
	@Column("coupon_support")
	private boolean couponSupport = false;

	/**
	 * #bool 是否与其他活动共享
	 */
	@ThriftField(12)
	@Column("shared")
	private boolean shared=false;

	/**
	 * #bool 是否支持预定
	 */
	@ThriftField(13)
	@Column("pre_order_support")
	private boolean preOrderSupport;

	/**
	 * #bool 是否自动（买免分为人工买免和自动买免）
	 */
	@ThriftField(14)
	@Column("privilege_way")
	private int privilegeWay;

	/**
	 * #bool 活动是否暂停
	 */
	@ThriftField(15)
	@Column("paused")
	private boolean paused;

	/**
	 * #bool 活动是否删除
	 */
	@ThriftField(16)
	@Column("deleted")
	private boolean deleted;

	/**
	 * 创建活动的员工的ID
	 */
	@ThriftField(17)
	@Column("staff_id")
	private long staffId;

	/**
	 * 创建时间
	 */
	@ThriftField(18)
	@Column("create_time")
	private long createTime;

	/**
	 * 更改时间
	 */
	@ThriftField(19)
	@Column("update_time")
	private long updateTime;

	/**
	 * #bool 收费项目是否参加活动
	 */
	@ThriftField(20)
	@Column("select_charge_item")
	private boolean selectChargeItem;

	/**
	 * 是否设置周期
	 */
	@ThriftField(21)
	@Column("select_period")
	private boolean selectPeriod=true;

	/**
	 * 参与买赠活动的收费项目的白名单
	 */
	private List<StorePromotionGratisChargeItem> storePromotionGratisChargeItems;

	/**
	 * 买赠周期营业时段设置
	 */
	private List<StorePromotionGratisPeriod> periods;

	private StaffDTO2 staffDTO2;

	public List<StorePromotionGratisChargeItem> getChargeItems() {
		return storePromotionGratisChargeItems;
	}

	public void setChargeItems(List<StorePromotionGratisChargeItem> storePromotionGratisChargeItems) {
		this.storePromotionGratisChargeItems = storePromotionGratisChargeItems;
	}

	public List<StorePromotionGratisPeriod> getPeriods() {
		return periods;
	}

	public void setPeriods(List<StorePromotionGratisPeriod> periods) {
		this.periods = periods;
	}

	public int getPurchaseNum() {
		return purchaseNum;
	}

	public void setPurchaseNum(int purchaseNum) {
		this.purchaseNum = purchaseNum;
	}

	public StaffDTO2 getStaffDTO2() {
		return staffDTO2;
	}

	public void setStaffDTO2(StaffDTO2 staffDTO2) {
		this.staffDTO2 = staffDTO2;
	}

	// 便于BeanUtil.copy方法自动赋值
	private List<Integer> takeModes;

	public long getPromotionGratisId() {
		return promotionGratisId;
	}

	public void setPromotionGratisId(long promotionGratisId) {
		this.promotionGratisId = promotionGratisId;
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

	public void setMechantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public boolean isSelectPeriod() {
		return selectPeriod;
	}

	public void setSelectPeriod(boolean selectPeriod) {
		this.selectPeriod = selectPeriod;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public double getGratisNum() {
		return gratisNum;
	}

	public void setGratisNum(double gratisNum) {
		this.gratisNum = gratisNum;
	}

	public String getTakeModelData() {
		return takeModeData;
	}

	public void setTakeModelData(String takeModeData) {
		this.takeModeData = takeModeData;
	}

	public boolean isWechatOnly() {
		return wechatOnly;
	}

	public void setWechatOnly(boolean wechatOnly) {
		this.wechatOnly = wechatOnly;
	}

	public boolean isCouponSupport() {
		return couponSupport;
	}

	public void setCouponSupport(boolean couponSupport) {
		this.couponSupport = couponSupport;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public boolean isPreOrderSupport() {
		return preOrderSupport;
	}

	public void setPreOrderSupport(boolean preOrderSupport) {
		this.preOrderSupport = preOrderSupport;
	}


	public int getPrivilegeWay() {
		return privilegeWay;
	}

	public void setPrivilegeWay(int privilegeWay) {
		this.privilegeWay = privilegeWay;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
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

	public String getTakeModeData() {
		return takeModeData;
	}

	public void setTakeModeData(String takeModeData) {
		this.takeModeData = takeModeData;
	}

	public boolean isSelectChargeItem() {
		return selectChargeItem;
	}

	public void setSelectChargeItem(boolean selectChargeItem) {
		this.selectChargeItem = selectChargeItem;
	}

	/**
	 * 支持的取餐方式
	 */
	public List<Integer> getTakeModes() {
		this.takeModes = StorePromotionUtil.getTakeModes(takeModeData);
		return this.takeModes;
	}

	public void setTakeModes(List<Integer> takeModes) {
		this.takeModes = takeModes;
		this.takeModeData = StorePromotionUtil.buildTakeMode(takeModes);
	}

	/**
	 * 活动状态：未开启，未开始、进行中、已暂停、已结束。根据当前时间进行判断
	 */
	public int getStatus() {
		return StorePromotionUtil.getStatus4Gratis(isPaused(), System.currentTimeMillis(), this.beginTime, this.endTime);
	}

	public int getStatus4Time(long time) {
		return StorePromotionUtil.getStatus(isPaused(), time, this.beginTime, this.endTime);
	}

	public boolean isNotBegin() {
		return StorePromotionUtil.isNotBegin(System.currentTimeMillis(), this.beginTime);
	}

	public boolean isDoing() {
		return StorePromotionUtil.isDoing(System.currentTimeMillis(), this.beginTime, this.endTime);
	}

	/**
	 * 是否不限制截止日期
	 *
	 * @return true:不限制
	 */
	// 自动赋值
	public boolean isUnlimit() {
		return this.endTime == Long.MAX_VALUE;
	}

	private boolean isCanUseTakeMode(StoreOrderTakeModeEnum storeOrderTakeModeEnum) {
		List<Integer> takeModeTypes = this.getTakeModes();
		for (Integer takeModeType : takeModeTypes) {
			if (takeModeType == storeOrderTakeModeEnum.getValue()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 收费项目id是否参加活动
	 *
	 * @param chargeItemId
	 *            收费项目id
	 * @return true/false
	 */
	public boolean containChargeItem(long chargeItemId) {
		if (!this.isSelectChargeItem()) {
			return true;
		}
		if (this.storePromotionGratisChargeItems == null) {
			return false;
		}
		for (StorePromotionGratisChargeItem gratisChargeItem : this.storePromotionGratisChargeItems) {
			if (gratisChargeItem.getChargeItemId() == chargeItemId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 指定星期[X]、取餐方式、营业时段、访问终端是否可以使用活动
	 *
	 * @param time
	 *            指定的时间，日期0时时间
	 * @param takeMode
	 *            取餐方式，参考{@link StoreOrderTakeModeEnum}
	 * @param timeBucketId
	 *            营业时段id
	 * @param wechatVisit
	 *            是否是维系访问
	 * @return true/false
	 */
	public boolean canUse(long time, int takeMode, long timeBucketId, boolean wechatVisit) {
		long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
		if (today != time && !this.preOrderSupport) {
			return false;
		}
		if (this.getStatus4Time(time) != StorePromotionStatusEnum.DOING.getValue()) {
			return false;
		}
		if (isWechatOnly() && !wechatVisit) {
			return false;
		}
		if (!this.isCanUseTakeMode(StoreOrderTakeModeEnum.findByValue(takeMode))) {
			return false;
		}
		if (this.periods == null) {
			return false;
		}
		int weekDay = DateUtil.getWeekDayByDate(time);
		for (StorePromotionGratisPeriod storePromotionGratisPeriod : this.periods) {
			if (storePromotionGratisPeriod.canUse(weekDay, timeBucketId)) {
				return true;
			}
		}
		return false;
	}

	/**
     * 指定星期[X]、营业时段是否可以使用活动
     *
     * @param time         指定的时间，日期0时时间
     * @param timeBucketId 营业时段id
     * @return true/false
     */
    public boolean canUse(long time, long timeBucketId) {
        long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (today != time && !this.preOrderSupport) {
            return false;
        }
        if (this.getStatus4Time(time) != StorePromotionStatusEnum.DOING.getValue()) {
            return false;
        }
        if (!this.isSelectPeriod()) {
            return true;
        }
        if (this.periods == null) {
            return false;
        }
        int weekDay = DateUtil.getWeekDayByDate(time);
        for (StorePromotionGratisPeriod obj : this.periods) {
            if (obj.canUse(weekDay, timeBucketId)) {
                return true;
            }
        }
        return false;
    }
	public boolean isEnded() {
		if (this.getStatus() == StorePromotionStatusEnum.ENDED.getValue()) {
			return true;
		}
		return false;
	}

	public void makeDeleted() {
		this.snapshot();
		this.deleted = true;
		this.updateTime = System.currentTimeMillis();
		this.update();
	}

	public void init4Create() {
		this.createTime = System.currentTimeMillis();
		this.updateTime = this.createTime;
	}

	public static List<Long> getIds(List<StorePromotionGratis> storePromotionGratisList) {
		List<Long> idList = Lists.newArrayList();
		for (StorePromotionGratis storePromotionGratis : storePromotionGratisList) {
			idList.add(storePromotionGratis.getPromotionGratisId());
		}
		return idList;
	}

	public static List<Long> getChargeitemIds(List<StorePromotionGratisChargeItem> storePromotionGratisChargeItems){
      List<Long> chargeItemIds=Lists.newArrayList();
		for(StorePromotionGratisChargeItem storePromotionGratisChargeItem :storePromotionGratisChargeItems){
            chargeItemIds.add(storePromotionGratisChargeItem.getChargeItemId());
		}
		return chargeItemIds;
	}

}
