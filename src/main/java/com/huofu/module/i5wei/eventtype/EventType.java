package com.huofu.module.i5wei.eventtype;

/**
 * Created by jiajin.nervous on 16/5/21.
 *
 */
public class EventType {

    /**
     * 营业时段变更事件类型
     */
    public static final int TIME_BUCKET = 1;

    /**
     * 餐牌号变更事件类型
     */
    public static final int SITE_NUMBER = 2;

    /**
     * 店铺桌台模式设置变更事件类型
     */
    public static final int STORE_TABLE_SETTING = 3;

    /**
     * 产品成本更新事件类型
     */
    public static final int PRODUCT_COST_UPDATE = 4;

    /**
     * 店铺设置变更
     */
    public static final int STORE_SETTING_UPDATE = 5;

	/**
	 * 删除收费项目变更
	 */
	public static final int CHARGEITEM_DELETE = 6;

	/**
	 * 修改出餐口（加工档口）
	 */
	public static final int MEAL_PORT_UPDATE = 7;

	/**
	 * 删除出餐口（加工档口）
	 */
	public static final int MEAL_PORT_DELETE = 8;
}
