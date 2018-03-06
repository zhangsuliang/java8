package com.huofu.module.i5wei.menu.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.menu.dbrouter.StoreChargeItemDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.DataUtil;

import java.util.*;

/**
 * Auto created by i5weitools 店铺收费项目
 */
@Table(name = "tb_store_charge_item", dalParser = StoreChargeItemDbRouter.class)
public class StoreChargeItem extends AbsEntity {

    public static final String IMG_SUBFIX_ORIGIN = "o";

    public static final String IMG_SUBFIX_BIG = "b";

    public static final String IMG_SUBFIX_THUMBNAIL = "t";

    /**
     * id
     */
    @Id
    @Column("charge_item_id")
    private long chargeItemId;

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
     * 名称
     */
    @Column("name")
    private String name;

    /**
     * 单位
     */
    @Column("unit")
    private String unit;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 删除标识 #bool 0:未删除 1:已删除
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 下线提醒时间
     */
    @Column("offline_notify_time")
    private long offlineNotifyTime;

    /**
     * 是否开启网单折扣&企业折扣 #bool 0:未开启 1:开启
     */
    @Column("enable_rebate")
    private boolean enableRebate;

    /**
     * #bool 0:不在微信显示 1:在微信显示
     */
    @Column("enable_wechat")
    private boolean enableWechat;

    /**
     * 收费项目模式 1:一般模式 2:高级模式
     */
    @Column("item_mode")
    private int itemMode;

    /**
     * 是否支持快取
     */
    @Column("quick_take")
    private boolean quickTake;

    /**
     * 是否支持外送（默认开启）
     */
    @Column("enable_delivery")
    private boolean enableDelivery;

    /**
     * 是否支持用户自取
     */
    @Column("enable_user_take")
    private boolean enableUserTake = true;

    /**
     * #bool 是否支持堂食（默认开启）
     */
    @Column("enable_dine_in")
    private boolean enableDineIn = true;

    /**
     * 是否支持用户自助下单
     */
    @Column("enable_user_order")
    private boolean enableUserOrder;

    /**
     * 是否是无限库存，如果库存没有开启，也算作无限(只有在获取库存时，才存在有效值)
     */
    private boolean unlimit;

    /**
     * 计算后的库存剩余数量，如果库存无限，忽略剩余量的判断(只有在获取库存时，才存在有效值)
     */
    private double remain;

    /**
     * 出餐口id
     */
    @Column("port_id")
    private long portId;

    /**
     * 打包费(单价)
     */
    @Column("package_price")
    private long packagePrice;

    /**
     * 是否将菜品置为“推荐” 1=开启，0=不开启，默认值为0
     */
    @Column("recommended_enable")
    private boolean recommendedEnable;

    /**
     * 菜品辣度：1=微辣，2=中辣，3=辣，默认值为0（不辣）
     */
    @Column("spicy_level")
    private int spicyLevel;

    /**
     * 是否支持优惠券支付 1=是，0=否，默认为1
     */
    @Column("coupon_supported")
    private boolean couponSupported = true;

    /**
     * “新品”有效时间
     */
    @Column("new_dishes_end_time")
    private long newDishesEndTime;

    /**
     * "新品"开关，0=开启“新品”，1=关闭“新品” ，默认值为0；即联合“新品”有效时间判断的最终结果（也可能是开关在有效期七日内直接操作后的开关结果）
     */
    @Column("new_dishes_enable")
    private boolean newDishesEnable;

    /**
     * 分类ID
     */
    @Column("category_id")
    private int categoryId;

    /**
     * 是否计入客数
     */
    @Column("enable_customer_traffic")
    private boolean enableCustomerTraffic;

    /**
     * 收费项目上成本设置情况
     */
    @Column("prime_cost_set")
    private int primeCostSet;

    /**
     * 是否手动设置了入客数
     */
    @Column("enable_manual_customer_traffic")
    private boolean enableManualCustomerTraffic;

    /**
     * 入客数
     */
    @Column("customer_traffic")
    private int customerTraffic;

    /**
     * 头像地址
     */
    @Column("head_img")
    private String headImg;

    /**
     * 会员价
     */
    @Column("member_price")
    private long memberPrice;

    /**
     * 点餐提示
     */
    @Column("tips")
    private String tips;

    /**
     * 收费项分单规则
     */
    @Column("div_rule")
    private int divRule;

    /**
     * 是否开启分单规则
     */
    @Column("open_div_rule")
    private boolean openDivRule;

	/**
	 * 是否开启了美团外卖
	 */
	@Column("meituan_waimai_enabled")
	private boolean meituanWaimaiEnabled;

	/**
	 * 是否开启了百度外卖
	 */
	@Column("baidu_waimai_enabled")
	private boolean baiduWaimaiEnabled;

	/**
	 * 是否开启了饿了么外卖
	 */
	@Column("eleme_waimai_enabled")
	private boolean elemeWaimaiEnabled;

	/**
	 * 是否开启称重
	 */
	@Column("weight_enabled")
	private boolean weightEnabled;

	/**
	 * 称重单位
	 */
	@Column("weight_unit")
	private int weightUnit;

	public boolean isMeituanWaimaiEnabled() {
		return meituanWaimaiEnabled;
	}

	public void setMeituanWaimaiEnabled(boolean meituanWaimaiEnabled) {
		this.meituanWaimaiEnabled = meituanWaimaiEnabled;
	}

	public boolean isBaiduWaimaiEnabled() {
		return baiduWaimaiEnabled;
	}

	public void setBaiduWaimaiEnabled(boolean baiduWaimaiEnabled) {
		this.baiduWaimaiEnabled = baiduWaimaiEnabled;
	}

	public boolean isElemeWaimaiEnabled() {
		return elemeWaimaiEnabled;
	}

	public void setElemeWaimaiEnabled(boolean elemeWaimaiEnabled) {
		this.elemeWaimaiEnabled = elemeWaimaiEnabled;
	}


    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public long getMemberPrice() {
        return memberPrice;
    }

    public void setMemberPrice(long memberPrice) {
        this.memberPrice = memberPrice;
    }

    private StoreMealPort storeMealPort;

    private StoreChargeItemPromotion storeChargeItemPromotion;

    public StoreChargeItemPromotion getStoreChargeItemPromotion() {
        return storeChargeItemPromotion;
    }

    public void setStoreChargeItemPromotion(StoreChargeItemPromotion storeChargeItemPromotion) {
        this.storeChargeItemPromotion = storeChargeItemPromotion;
    }

    public StoreMealPort getStoreMealPort() {
        return storeMealPort;
    }

    public void setStoreMealPort(StoreMealPort storeMealPort) {
        this.storeMealPort = storeMealPort;
    }

    public boolean isEnableDelivery() {
        return enableDelivery;
    }

    public void setEnableDelivery(boolean enableDelivery) {
        this.enableDelivery = enableDelivery;
    }


	public boolean isEnableUserTake() {
		return enableUserTake;
	}

	public void setEnableUserTake(boolean enableUserTake) {
		this.enableUserTake = enableUserTake;
	}

	public boolean isEnableDineIn() {
		return enableDineIn;
	}

	public void setEnableDineIn(boolean enableDineIn) {
		this.enableDineIn = enableDineIn;
	}

	public boolean isEnableUserOrder() {
		return enableUserOrder;
	}

	public void setEnableUserOrder(boolean enableUserOrder) {
		this.enableUserOrder = enableUserOrder;
	}

	public long getPortId() {
		return portId;
	}

	public void setPortId(long portId) {
		this.portId = portId;
	}

	public boolean isQuickTake() {
		return quickTake;
	}

	public void setQuickTake(boolean quickTake) {
		this.quickTake = quickTake;
	}

	public boolean isUnlimit() {
		return unlimit;
	}

	public void setUnlimit(boolean unlimit) {
		this.unlimit = unlimit;
	}

	public double getRemain() {
		return remain;
	}

	public void setRemain(double remain) {
		this.remain = remain;
	}

	public int getItemMode() {
		return itemMode;
	}

	public void setItemMode(int itemMode) {
		this.itemMode = itemMode;
	}

	public boolean isEnableWechat() {
		return enableWechat;
	}

	public void setEnableWechat(boolean enableWechat) {
		this.enableWechat = enableWechat;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public int getDivRule() {
		return divRule;
	}

	public void setDivRule(int divRule) {
		this.divRule = divRule;
	}

	public boolean isOpenDivRule() {
		return openDivRule;
	}

	public void setOpenDivRule(boolean openDivRule) {
		this.openDivRule = openDivRule;
	}

	public boolean isEnableRebate() {
		return enableRebate;
	}

	public void setEnableRebate(boolean enableRebate) {
		this.enableRebate = enableRebate;
	}

	public long getOfflineNotifyTime() {
		return offlineNotifyTime;
	}

	public void setOfflineNotifyTime(long offlineNotifyTime) {
		this.offlineNotifyTime = offlineNotifyTime;
	}

	public void initForCreate(long now) {
		this.createTime = now;
		this.updateTime = now;
	}

	public long getCurPrice() {
		return this.curStoreChargeItemPrice.getPrice();
	}

	private StoreChargeItemPrice curStoreChargeItemPrice;

	private StoreChargeItemPrice nextStoreChargeItemPrice;

	private List<StoreChargeItemWeek> curStoreChargeItemWeeks;

	private List<StoreChargeItemWeek> nextWeekStoreChargeItemWeeks;

	private List<StoreChargeSubitem> storeChargeSubitems;

	public StoreChargeItemPrice getCurStoreChargeItemPrice() {
		return curStoreChargeItemPrice;
	}

	public void setCurStoreChargeItemPrice(StoreChargeItemPrice curStoreChargeItemPrice) {
		this.curStoreChargeItemPrice = curStoreChargeItemPrice;
	}

	public StoreChargeItemPrice getNextStoreChargeItemPrice() {
		return nextStoreChargeItemPrice;
	}

	public void setNextStoreChargeItemPrice(StoreChargeItemPrice nextStoreChargeItemPrice) {
		this.nextStoreChargeItemPrice = nextStoreChargeItemPrice;
	}

	public List<StoreChargeItemWeek> getCurStoreChargeItemWeeks() {
		return curStoreChargeItemWeeks;
	}

	public void setCurStoreChargeItemWeeks(List<StoreChargeItemWeek> curStoreChargeItemWeeks) {
		this.curStoreChargeItemWeeks = curStoreChargeItemWeeks;
	}

	public List<StoreChargeItemWeek> getNextWeekStoreChargeItemWeeks() {
		return nextWeekStoreChargeItemWeeks;
	}

	public void setNextWeekStoreChargeItemWeeks(List<StoreChargeItemWeek> nextWeekStoreChargeItemWeeks) {
		this.nextWeekStoreChargeItemWeeks = nextWeekStoreChargeItemWeeks;
	}

	public List<StoreChargeSubitem> getStoreChargeSubitems() {
		return storeChargeSubitems;
	}

	public void setStoreChargeSubitems(List<StoreChargeSubitem> storeChargeSubitems) {
		this.storeChargeSubitems = storeChargeSubitems;
	}

	public int getPrimeCostSet() {
		return primeCostSet;
	}

	public void setPrimeCostSet(int primeCostSet) {
		this.primeCostSet = primeCostSet;
	}

	public long getPackagePrice() {
		return packagePrice;
	}

	public void setPackagePrice(long packagePrice) {
		this.packagePrice = packagePrice;
	}

	public boolean isRecommendedEnable() {
		return recommendedEnable;
	}

	public void setRecommendedEnable(boolean recommendedEnable) {
		this.recommendedEnable = recommendedEnable;
	}

	public int getSpicyLevel() {
		return spicyLevel;
	}

	public void setSpicyLevel(int spicyLevel) {
		this.spicyLevel = spicyLevel;
	}

	public boolean isCouponSupported() {
		return couponSupported;
	}

	public void setCouponSupported(boolean couponSupported) {
		this.couponSupported = couponSupported;
	}

	public long getNewDishesEndTime() {
		return newDishesEndTime;
	}

	public void setNewDishesEndTime(long newDishesEndTime) {
		this.newDishesEndTime = newDishesEndTime;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public boolean isEnableCustomerTraffic() {
		return enableCustomerTraffic;
	}

	public void setEnableCustomerTraffic(boolean enableCustomerTraffic) {
		this.enableCustomerTraffic = enableCustomerTraffic;
	}

	public boolean isEnableManualCustomerTraffic() {
		return enableManualCustomerTraffic;
	}

	public void setEnableManualCustomerTraffic(boolean enableManualCustomerTraffic) {
		this.enableManualCustomerTraffic = enableManualCustomerTraffic;
	}

	public int getCustomerTraffic() {
		return customerTraffic;
	}

    public void setCustomerTraffic(int customerTraffic) {
        this.customerTraffic = customerTraffic;
    }

    public boolean isWeightEnabled() {
        return weightEnabled;
    }

    public void setWeightEnabled(boolean weightEnabled) {
        this.weightEnabled = weightEnabled;
    }

    public int getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(int weightUnit) {
        this.weightUnit = weightUnit;
    }

    /**
     * 用“新品”有效期截止时间判断其开关的最终状态用于copy到对应DTO返回给前端，告诉其开关的最终状态。
     */
    public boolean isNewDishesEnable() {
        long currentTime = System.currentTimeMillis();
        if (this.newDishesEndTime >= currentTime) {
            return true;
        }
        return false;
    }

    public void setNewDishesEnable(boolean newDishesEnable) {
        this.newDishesEnable = newDishesEnable;
    }

    public Map<Long, StoreChargeSubitem> getProducts() {
        Map<Long, StoreChargeSubitem> products = new HashMap<Long, StoreChargeSubitem>();
        if (storeChargeSubitems != null) {
            for (StoreChargeSubitem subitem : storeChargeSubitems) {
                products.put(subitem.getProductId(), subitem);
            }
        }
        return products;
    }

    public void parseStoreChargeItemWeeks(List<StoreChargeItemWeek> storeChargeItemWeeks) {
        if (storeChargeItemWeeks == null) {
            return;
        }
        this.curStoreChargeItemWeeks = Lists.newArrayList();
        this.nextWeekStoreChargeItemWeeks = Lists.newArrayList();
        long now = System.currentTimeMillis();
        for (StoreChargeItemWeek week : storeChargeItemWeeks) {
            if (week.isCurrentWeek(now)) {
                this.curStoreChargeItemWeeks.add(week);
            } else if (week.isNextWeek(now)) {
                this.nextWeekStoreChargeItemWeeks.add(week);
            }
        }
        this.sortItemWeeks();
    }

    public void sortItemWeeks() {
        this.sortByWeekDay(this.curStoreChargeItemWeeks);
        this.sortByWeekDay(this.nextWeekStoreChargeItemWeeks);
    }

    private void sortByWeekDay(List<StoreChargeItemWeek> storeChargeItemWeeks) {
        if (storeChargeItemWeeks == null) {
            return;
        }
        Collections.sort(storeChargeItemWeeks, (o1, o2) -> {
            if (o1.getWeekDay() > o2.getWeekDay()) {
                return 1;
            }
            if (o1.getWeekDay() < o2.getWeekDay()) {
                return -1;
            }
            return 0;
        });
    }

    public void parseStoreChargeItemPrices(List<StoreChargeItemPrice> storeChargeItemPrices, long time) {
        if (storeChargeItemPrices == null) {
            return;
        }
        for (StoreChargeItemPrice price : storeChargeItemPrices) {
            if (price.isSpecTimePrice(time)) {
                this.curStoreChargeItemPrice = price;
            } else if (price.isNextPrice(time)) {
                this.nextStoreChargeItemPrice = price;
            }
        }
    }

    public void checkOfflineNotifyTime(long time, long oldOfflineTime) throws T5weiException {
        if (this.offlineNotifyTime == oldOfflineTime) {
            return;
        } else {
            if (this.offlineNotifyTime <= time && this.offlineNotifyTime > 0) {
                throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_OFFLINE_NOTIFY_TIME_INVALID.getValue(), "merchantId[" + this.merchantId + "] storeId[" + this.storeId + "] chargeItemId[" + this.chargeItemId + "] offlineNotifyTime[" + this.offlineNotifyTime + "] invalid");
            }
        }
    }

    public void makeDeleted() {
        this.deleted = true;
        this.updateTime = System.currentTimeMillis();
        this.update();
    }

    public static Map<Long, StoreChargeItem> listToMap(List<StoreChargeItem> storeChargeItems) {
        Map<Long, StoreChargeItem> storeChargeItemMap = Maps.newHashMap();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            storeChargeItemMap.put(storeChargeItem.getChargeItemId(), storeChargeItem);
        }
        return storeChargeItemMap;
    }

    /**
     * 通过{@link huofuhelper.util.bean.BeanUtil#copy(Object, Object)} 进行赋值
     *
     * @return
     */
    public String getHeadImgBig() {
        if (DataUtil.isEmpty(this.headImg)) {
            return null;
        }
        return getFullHeadImgUrl(this.headImg, IMG_SUBFIX_BIG);
    }

    public String getHeadImageThumbnail() {
        if (DataUtil.isEmpty(this.headImg)) {
            return null;
        }
        return getFullHeadImgUrl(this.headImg, IMG_SUBFIX_THUMBNAIL);
    }

    public static String getFullHeadImgUrl(String headImg, String subfix) {
        StringBuilder sb = new StringBuilder();
        sb.append(SysConfig.getImageServerPath());
        sb.append(headImg);
        sb.append("_");
        sb.append(subfix);
        return sb.toString();
    }

    public static List<Long> getIdList(Collection<StoreChargeItem> storeChargeItems) {
        List<Long> list = Lists.newArrayList();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            list.add(storeChargeItem.getChargeItemId());
        }
        return list;
    }

	@Override
	public String toString() {
		return "StoreChargeItem [chargeItemId=" + chargeItemId + ", merchantId=" + merchantId + ", storeId=" + storeId
				+ ", name=" + name + ", unit=" + unit + ", createTime=" + createTime + ", updateTime=" + updateTime
				+ ", deleted=" + deleted + ", offlineNotifyTime=" + offlineNotifyTime + ", enableRebate=" + enableRebate
				+ ", enableWechat=" + enableWechat + ", itemMode=" + itemMode + ", quickTake=" + quickTake
				+ ", enableDelivery=" + enableDelivery + ", enableUserTake=" + enableUserTake + ", enableDineIn="
				+ enableDineIn + ", enableUserOrder=" + enableUserOrder + ", unlimit=" + unlimit + ", remain=" + remain
				+ ", portId=" + portId + ", packagePrice=" + packagePrice + ", recommendedEnable=" + recommendedEnable
				+ ", spicyLevel=" + spicyLevel + ", couponSupported=" + couponSupported + ", newDishesEndTime="
				+ newDishesEndTime + ", newDishesEnable=" + newDishesEnable + ", categoryId=" + categoryId
				+ ", enableCustomerTraffic=" + enableCustomerTraffic + ", primeCostSet=" + primeCostSet
				+ ", enableManualCustomerTraffic=" + enableManualCustomerTraffic + ", customerTraffic="
				+ customerTraffic + ", headImg=" + headImg + ", memberPrice=" + memberPrice + ", tips=" + tips
				+ ", divRule=" + divRule + ", openDivRule=" + openDivRule + ", meituanWaimaiEnabled="
				+ meituanWaimaiEnabled + ", baiduWaimaiEnabled=" + baiduWaimaiEnabled + ", elemeWaimaiEnabled="
				+ elemeWaimaiEnabled + ", weightEnabled=" + weightEnabled + ", weightUnit=" + weightUnit
				+ ", storeMealPort=" + storeMealPort + ", storeChargeItemPromotion=" + storeChargeItemPromotion
				+ ", curStoreChargeItemPrice=" + curStoreChargeItemPrice + ", nextStoreChargeItemPrice="
				+ nextStoreChargeItemPrice + ", curStoreChargeItemWeeks=" + curStoreChargeItemWeeks
				+ ", nextWeekStoreChargeItemWeeks=" + nextWeekStoreChargeItemWeeks + ", storeChargeSubitems="
				+ storeChargeSubitems + "]";
	}

}