package com.huofu.module.i5wei.promotion.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

import java.util.List;
import java.util.Set;

/**
 * 满减活动设置表
 */
@CacheDataCodecAble(prefix = I5weiCachePrefix.STORE_PROMOTION_REDUCE)
@Table(name = "tb_store_promotion_reduce", dalParser = BaseDefaultStoreDbRouter.class)
public class StorePromotionReduce extends BaseEntity {

    /**
     * 满减活动ID
     */
    @ThriftField(1)
    @Id
    @Column("promotion_reduce_id")
    private long promotionReduceId;

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

    /**
     * 活动标题
     */
    @ThriftField(4)
    @Column
    private String title;

    /**
     * 创建服务员ID
     */
    @ThriftField(5)
    @Column("staff_id")
    private long staffId;

    /**
     * 开始时间
     */
    @ThriftField(6)
    @Column("begin_time")
    private long beginTime;

    /**
     * 结束时间
     */
    @ThriftField(7)
    @Column("end_time")
    private long endTime;

    /**
     * 支持的取餐方式，支持多种取餐方式以英文逗号分隔
     */
    @ThriftField(8)
    @Column("take_mode_data")
    private String takeModeData;

    /**
     * #bool 是否已暂停：0=未暂停、1=暂停
     */
    @ThriftField(9)
    @Column("paused")
    private boolean paused;

    /**
     * #bool 是否仅支持微信自助
     */
    @ThriftField(10)
    @Column("wechat_only")
    private boolean wechatOnly = true;

    /**
     * #bool 是否支持使用优惠券
     */
    @ThriftField(11)
    @Column("coupon_support")
    private boolean couponSupport = true;

    /**
     * #bool 是否指定日期
     */
    @ThriftField(12)
    @Column("select_period")
    private boolean selectPeriod;

    /**
     * 是否与其他活动共享
     */
    @ThriftField(13)
    @Column
    private boolean shared;

    /**
     * #bool 是否指定菜品
     */
    @ThriftField(14)
    @Column("select_charge_item")
    private boolean selectChargeItem;

    /**
     * 最后更新时间
     */
    @ThriftField(15)
    @Column("update_time")
    private long updateTime;

    /**
     * 创建时间
     */
    @ThriftField(16)
    @Column("create_time")
    private long createTime;

    /**
     * 是否删除
     */
    @ThriftField(17)
    @Column
    private boolean deleted;

	@ThriftField(18)
	@Column("pre_order_support")
	private boolean preOrderSupport;

    //存在的目的就是为了BeanUtil.copy自动赋值
    private List<Integer> takeModes;

    private StaffDTO2 staffDTO2;

	public boolean isPreOrderSupport() {
		return preOrderSupport;
	}

	public void setPreOrderSupport(boolean preOrderSupport) {
		this.preOrderSupport = preOrderSupport;
	}

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * 是否不限制截止日期
     *
     * @return true:不限制
     */
    //自动赋值
    public boolean isUnlimit() {
        return this.endTime == Long.MAX_VALUE;
    }

    public StaffDTO2 getStaffDTO2() {
        return staffDTO2;
    }

    public void setStaffDTO2(StaffDTO2 staffDTO2) {
        this.staffDTO2 = staffDTO2;
    }

    /**
     * 满减额度设置
     */
    private List<StorePromotionReduceQuota> quotas;

    /**
     * 参与满减活动的收费项目白名单
     */
    private List<StorePromotionReduceChargeItem> chargeItems;

    /**
     * 满减活动周期营业时段设置
     */
    private List<StorePromotionReducePeriod> periods;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public long getPromotionReduceId() {
        return promotionReduceId;
    }

    public void setPromotionReduceId(long promotionReduceId) {
        this.promotionReduceId = promotionReduceId;
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

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getTakeModeData() {
        return takeModeData;
    }

    public void setTakeModeData(String takeModeData) {
        this.takeModeData = takeModeData;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
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

    public boolean isSelectPeriod() {
        return selectPeriod;
    }

    public void setSelectPeriod(boolean selectPeriod) {
        this.selectPeriod = selectPeriod;
    }

    public boolean isSelectChargeItem() {
        return selectChargeItem;
    }

    public void setSelectChargeItem(boolean selectChargeItem) {
        this.selectChargeItem = selectChargeItem;
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

    public List<StorePromotionReduceQuota> getQuotas() {
        return quotas;
    }

    public void setQuotas(List<StorePromotionReduceQuota> quotas) {
        this.quotas = quotas;
    }

    public List<StorePromotionReduceChargeItem> getChargeItems() {
        return chargeItems;
    }

    public void setChargeItems(List<StorePromotionReduceChargeItem> chargeItems) {
        this.chargeItems = chargeItems;
    }

    public List<StorePromotionReducePeriod> getPeriods() {
        return periods;
    }

    public void setPeriods(List<StorePromotionReducePeriod> periods) {
        this.periods = periods;
    }

    public List<Integer> getTakeModes() {
        this.takeModes = StorePromotionUtil.getTakeModes(takeModeData);
        return this.takeModes;
    }

    public void setTakeModes(List<Integer> takeModes) {
        this.takeModes = takeModes;
        this.takeModeData = StorePromotionUtil.buildTakeMode(takeModes);
    }

    public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
    }

    /**
     * 活动状态：未开始、进行中、已暂停、已结束。根据当前时间进行判断
     */
    public int getStatus() {
	    return StorePromotionUtil.getStatus(isPaused(), System.currentTimeMillis(), this.beginTime, this.endTime);
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

    public boolean isEnded() {
        return StorePromotionUtil.isEnded(System.currentTimeMillis(), this.endTime);
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

    private Set<Long> _chargeItemIdSet;

    /**
     * 收费项目id是否参加活动
     *
     * @param chargeItemId 收费项目id
     * @return true/false
     */
    public boolean containChargeItem(long chargeItemId) {
        if (!this.isSelectChargeItem()) {
            return true;
        }
        if (this.chargeItems == null) {
            return false;
        }
        if (_chargeItemIdSet == null) {
            _chargeItemIdSet = Sets.newHashSet();
            for (StorePromotionReduceChargeItem reduceChargeItem : this.chargeItems) {
                _chargeItemIdSet.add(reduceChargeItem.getChargeItemId());
            }
        }
        return _chargeItemIdSet.contains(chargeItemId);
    }

    public List<Long> getContainChargeItemIds(List<Long> chargeItemIds) {
        List<Long> list = Lists.newArrayList();
        if (chargeItemIds == null) {
            return list;
        }
        for (Long chargeItemId : chargeItemIds) {
            if (this.containChargeItem(chargeItemId)) {
                list.add(chargeItemId);
            }
        }
        return list;
    }

    /**
     * 指定星期[X]、取餐方式、营业时段是否可以使用活动
     *
     * @param time         指定的时间，日期0时时间
     * @param takeMode     取餐方式，参考{@link StoreOrderTakeModeEnum}
     * @param timeBucketId 营业时段id
     * @param wechatVisit  是否是微信访问
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
        if (!this.isSelectPeriod()) {
            return true;
        }
        if (this.periods == null) {
            return false;
        }
	    int weekDay = DateUtil.getWeekDayByDate(time);
	    for (StorePromotionReducePeriod obj : this.periods) {
            if (obj.canUse(weekDay, timeBucketId)) {
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
        for (StorePromotionReducePeriod obj : this.periods) {
            if (obj.canUse(weekDay, timeBucketId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据额度获取最优的满减配置
     *
     * @param amount 消费金额
     * @return 满减后的金额。如果没有合适的满减配置，返回null。没有满减配置时，返回null。
     */
    public StorePromotionReduceQuota getBestReduceQuota(long amount) {
        if (this.quotas == null) {
            return null;
        }
        long minPrice = Long.MAX_VALUE;
        StorePromotionReduceQuota minQuota = null;
        for (StorePromotionReduceQuota quota : quotas) {
            if (quota.canUse(amount)) {
                long price = quota.getReduceAmount(amount);
                if (price <= minPrice) {
                    minPrice = price;
                    minQuota = quota;
                }
            }
        }
        return minQuota;
    }

    public static List<Long> getIdList(List<StorePromotionReduce> storePromotionReduces) {
        List<Long> idList = Lists.newArrayList();
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            idList.add(storePromotionReduce.getPromotionReduceId());
        }
        return idList;
    }

    public void makeDeleted() {
        this.snapshot();
        this.deleted = true;
        this.updateTime = System.currentTimeMillis();
        this.update();
    }

    public boolean equalsData(Object o) {
        if (this == o) return true;
        if (!(o instanceof StorePromotionReduce)) return false;

        StorePromotionReduce that = (StorePromotionReduce) o;

        if (getPromotionReduceId() != that.getPromotionReduceId()) return false;
        if (getStoreId() != that.getStoreId()) return false;
        if (getMerchantId() != that.getMerchantId()) return false;
        if (getStaffId() != that.getStaffId()) return false;
        if (getBeginTime() != that.getBeginTime()) return false;
        if (getEndTime() != that.getEndTime()) return false;
        if (isPaused() != that.isPaused()) return false;
        if (isWechatOnly() != that.isWechatOnly()) return false;
        if (isCouponSupport() != that.isCouponSupport()) return false;
        if (isSelectPeriod() != that.isSelectPeriod()) return false;
        if (isShared() != that.isShared()) return false;
        if (isSelectChargeItem() != that.isSelectChargeItem()) return false;
        if (getUpdateTime() != that.getUpdateTime()) return false;
        if (getCreateTime() != that.getCreateTime()) return false;
        if (isDeleted() != that.isDeleted()) return false;
        if (getTitle() != null ? !getTitle().equals(that.getTitle()) : that.getTitle() != null) return false;
        if (getTakeModeData() != null ? !getTakeModeData().equals(that.getTakeModeData()) : that.getTakeModeData() != null)
            return false;

        if (getQuotas() != null && that.getQuotas() != null) {
            if (getQuotas().size() != that.getQuotas().size()) {
                return false;
            }
            int i = 0;
            for (StorePromotionReduceQuota quota : getQuotas()) {
                if (!quota.equalsData(that.getQuotas().get(i))) {
                    return false;
                }
                i++;
            }
        }

        if (getChargeItems() != null && that.getChargeItems() != null) {
            if (getChargeItems().size() != that.getChargeItems().size()) {
                return false;
            }
            int i = 0;
            for (StorePromotionReduceChargeItem chargeItem : getChargeItems()) {
                if (!chargeItem.equalsData(that.getChargeItems().get(i))) {
                    return false;
                }
                i++;
            }
        } else {
            return false;
        }

        if (getPeriods() != null && that.getPeriods() != null) {
            if (this.getPeriods().size() != that.getPeriods().size()) {
                return false;
            }
            int i = 0;
            for (StorePromotionReducePeriod period : getPeriods()) {
                if (!period.equalsData(that.getPeriods().get(i))) {
                    return false;
                }
                i++;
            }
            return true;
        } else {
            return false;
        }

    }

    @Override
    public String toString() {
        return "StorePromotionReduce{" +
                "promotionReduceId=" + promotionReduceId +
                ", storeId=" + storeId +
                ", merchantId=" + merchantId +
                ", title='" + title + '\'' +
                ", staffId=" + staffId +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", takeModeData='" + takeModeData + '\'' +
                ", paused=" + paused +
                ", wechatOnly=" + wechatOnly +
                ", couponSupport=" + couponSupport +
                ", selectPeriod=" + selectPeriod +
                ", shared=" + shared +
                ", selectChargeItem=" + selectChargeItem +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", deleted=" + deleted +
                ", takeModes=" + takeModes +
                ", staffDTO2=" + staffDTO2 +
                ", quotas=" + quotas +
                ", chargeItems=" + chargeItems +
                ", periods=" + periods +
                ", _chargeItemIdSet=" + _chargeItemIdSet +
                '}';
    }
}