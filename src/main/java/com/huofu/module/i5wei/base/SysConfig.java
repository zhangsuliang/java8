package com.huofu.module.i5wei.base;

import java.util.ResourceBundle;

/**
 * Created by akwei on 9/30/14.
 */
public class SysConfig {

    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("sysconfig");

    private static final ResourceBundle rb = ResourceBundle.getBundle
            ("i5weiconfig");

    public static String getCahceKeyPrefix() {
        return BUNDLE.getString("cache_key_prefix");
    }

    public static boolean isEnableCache() {
        return Boolean.parseBoolean(BUNDLE.getString("memcached.enableCache"));
    }

    public static String getWechatNotifyServer() {
        return BUNDLE.getString("wechat.notify.server");
    }

    public static String getWechatNotifyServerBaseUrl() {
        return BUNDLE.getString("wechat.notify.server");
    }

    public static int getOfficalUserGzId() {
        return Integer.parseInt(rb.getString("user.gzId"));
    }

    public static int getOfficalMerchantGzId() {
        return Integer.parseInt(rb.getString("merchant.gzId"));
    }


    /**
     * 获取营业时段事件主题
     */
    public static String getStoreTimeBucketTopicArn() {
        return BUNDLE.getString("sns.storeTimeBucket");
    }

    /**
     * 获取餐牌号事件主题
     */
    public static String getSiteNumberTopicArn() {
        return BUNDLE.getString("sns.siteNumber");
    }

    /**
     * 获取桌台模式设置事件主题
     */
    public static String getStoreTableSettingTopicArn() {
        return BUNDLE.getString("sns.storeTableSetting");
    }

    /**
     * 获取产品成本变动的事件主题
     */
    public static String getProductEventTopicArn() {
        return BUNDLE.getString("sns.store_product");
    }

    /**
     * 获取店铺设置事件主题
     */
    public static String getStoreSettingTopicArn() {
        return BUNDLE.getString("sns.store_setting");
    }

    public static String getImageServerPath() {
        return rb.getString("img.server.path");
    }

	/**
	 * 删除收费项目事件主题
	 */
	public static String getDeleteChargeItemTopicArn() {
		return BUNDLE.getString("sns.delete_chargeItem");
	}

    /**
     * 获取出餐口主题
     */
    public static String getStoreMealPortTopicArn() {
        return BUNDLE.getString("sns.store_meal_port");
    }
}
