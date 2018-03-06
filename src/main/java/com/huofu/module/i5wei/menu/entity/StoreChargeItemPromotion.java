package com.huofu.module.i5wei.menu.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionActiveStatus;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionLimitTypeEnum;
import huofuhelper.util.DateUtil;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.dbrouter.StoreChargeItemPromotionDbRouter;
import huofuhelper.util.SpringUtil;
import huofuhelper.util.cache.WengerCache;

/**
 * Auto created by i5weitools 店铺单品促销表
 */
@Table(name = "tb_store_charge_item_promotion", dalParser = StoreChargeItemPromotionDbRouter.class)
public class StoreChargeItemPromotion extends AbsEntity {

    public static final int EXPIRE_SEC = 3 * 24 * 60 * 60;

	/**
	 * 自增主键
	 */
	@Id
	@Column("promotion_id")
	private long promotionId;

	/**
	 * 商品ID
	 */
	@Column("charge_item_id")
	private long chargeItemId;

	/**
	 * 促销价格
	 */
	@Column("promotion_price")
	private long promotionPrice;

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
	 * 上线时间
	 */
	@Column("start_time")
	private long startTime;

	/**
	 * 下线时间
	 */
	@Column("end_time")
	private long endTime;

	/**
	 * 无限=0，优惠数量>0为限制数量
	 */
	@Column("limit_num")
	private int limitNum;

	/**
	 * 已促销数量
	 */
	@Column("sale_num")
	private int saleNum;

	/**
	 * 频率：0=每人一次，1=每人每时段一次，2=每人每天一次，3=每人每周一次，4=每人每月一次
	 */
	@Column("rate_type")
	private int rateType;

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
	 * 支持类型 0:不限, 1:仅支持堂食
	 */
	@Column("support_type")
	private int supportType;

	/**
	 * 创建优惠活动员工id
	 */
	@Column("staff_id")
	private long staffId;

	/**
	 * 是否开启
	 */
	@Column("enabled")
	private boolean enabled;

	/**
	 * 数量限制类型：0=按总量限制，1=按天限制
	 */
	@Column("limit_type")
	private int limitType;

	/**
	 * 单日数量限制
	 */
	@Column("limit_day_num")
	private int limitDayNum;

	/**
	 * 单日已销售数量
	 */
	@Column("sale_day_num")
	private int saleDayNum;

	public long getPromotionId() {
		return promotionId;
	}

	public void setPromotionId(long promotionId) {
		this.promotionId = promotionId;
	}

	public long getChargeItemId() {
		return chargeItemId;
	}

	public void setChargeItemId(long chargeItemId) {
		this.chargeItemId = chargeItemId;
	}

	public long getPromotionPrice() {
		return promotionPrice;
	}

	public void setPromotionPrice(long promotionPrice) {
		this.promotionPrice = promotionPrice;
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

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getLimitNum() {
		return limitNum;
	}

	public void setLimitNum(int limitNum) {
		this.limitNum = limitNum;
	}

	public int getRateType() {
		return rateType;
	}

	public void setRateType(int rateType) {
		this.rateType = rateType;
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

	public int getSupportType() {
		return supportType;
	}

	public void setSupportType(int supportType) {
		this.supportType = supportType;
	}

	public long getStaffId() {
		return staffId;
	}

	public void setStaffId(long staffId) {
		this.staffId = staffId;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getSaleNum() {
		return saleNum;
	}

	public void setSaleNum(int saleNum) {
		this.saleNum = saleNum;
	}

	public int getLimitType() {
		return limitType;
	}

	public void setLimitType(int limitType) {
		this.limitType = limitType;
	}

	public int getLimitDayNum() {
		return limitDayNum;
	}

	public void setLimitDayNum(int limitDayNum) {
		this.limitDayNum = limitDayNum;
	}

    public int getSaleDayNum() {
        return saleDayNum;
    }

    public int getSaleDayNum4Time(long time) {
        long timeDate = DateUtil.getBeginTime(time, null);
        long todayDate = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (timeDate != todayDate) {
            WengerCache wengerCache = (WengerCache) SpringUtil.instance().getBean(WengerCache.class);
            Integer num = wengerCache.get(getCacheKey(this.chargeItemId, timeDate), false);
            if (num == null) {
                return 0;
            }
            return num;
        }
        return saleDayNum;
    }

    public void setSaleDayNum(int saleDayNum) {
        this.saleDayNum = saleDayNum;
    }

    public boolean isInAvailable() {
        return this.isInAvailable4Time(System.currentTimeMillis());
    }

    /**
	 * 是否有效，开启的在指定的时间内没有超过促销数量
	 *
	 * @param time
	 *            指定的时间戳,单位: 毫秒
	 * @return boolean
	 */
	public boolean isInAvailable4Time(long time) {
		if (!this.enabled) {
			return false;
		}
		if (time < DateUtil.getBeginTime(this.startTime, null) || time > DateUtil.getEndTime(this.endTime, null)) {
			return false;
		}
		if (!hasRemain(time)) {
			return false;
		}
		return true;
	}

	public boolean hasRemain(long time) {
		if (this.limitType == StoreChargeItemPromotionLimitTypeEnum.ALL.getValue()) {
			// 按总量计算已售完
			if (this.limitNum <= this.saleNum) {
				return false;
			}
		} else if (this.limitType == StoreChargeItemPromotionLimitTypeEnum.DAY.getValue()) {
			// 按天销量计算已售完
			if (this.limitDayNum <= this.getSaleDayNum4Time(time)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 活动状态
	 *
	 * @return
	 */
	public int getActiveStatus() {
		return this.getActiveStatus4Time(System.currentTimeMillis());
	}

	/**
	 * 活动状态
	 *
	 * @return
	 */
	public int getActiveStatus4Time(long time) {
		if (time > DateUtil.getEndTime(this.endTime, null)) {
			// 当前时间大于结束时间,活动已经结束
			return StoreChargeItemPromotionActiveStatus.END_OFF.getValue();
		} else if (time < DateUtil.getBeginTime(this.startTime, null)) {
			// 当前时间小于开始时间,活动还没开始
			return StoreChargeItemPromotionActiveStatus.NOT_START.getValue();
		} else if (this.enabled) {
			// 活动处于开启状态
			if (!hasRemain(time)) {
				return StoreChargeItemPromotionActiveStatus.SOLD_OUT.getValue();
			}
			return StoreChargeItemPromotionActiveStatus.OK.getValue();
		} else {
			// 活动处于关闭状态
			return StoreChargeItemPromotionActiveStatus.CLOSE.getValue();
		}
	}

    public static String getCacheKey(long chargeItemId, long repastDate) {
        return String.valueOf(chargeItemId) + "_" + String.valueOf(repastDate);
    }

}