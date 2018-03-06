package com.huofu.module.i5wei.menu.service;

import com.huofu.module.i5wei.menu.entity.StoreDateBizSetting;

/**
 * 菜单查询参数
 * Created by akwei on 10/10/15.
 */
public class StoreMenuQueryParam {

    private int clientType;

    /**
     * 商户id
     */
    private int merchantId;

    /**
     * 店铺id
     */
    private long storeId;

    /**
     * 选择的日期(每天的开始时间)
     */
    private long date;

    /**
     * 营业时段id
     */
    private long timeBucketId;

    /**
     * 日期的经营特殊设置
     */
    private StoreDateBizSetting storeDateBizSetting;

    /**
     * 是否只获取支持微信对话框的收费项目
     */
    private boolean forWechat;

    /**
     * 是否只获取外送的收费项目
     */
    private boolean forDelivery;

    /**
     * 是否只获取堂食的收费项目
     */
    private boolean forEatIn;

    /**
     * 是否只获取自取的收费项目
     */
    private boolean forUserTake;

    /**
     * 是否加载在有效期收费项目单品促销信息
     */
    private boolean loadAvailablePromotion;

    public boolean isForEatIn() {
        return forEatIn;
    }

    public void setForEatIn(boolean forEatIn) {
        this.forEatIn = forEatIn;
    }

    public boolean isForUserTake() {
        return forUserTake;
    }

    public void setForUserTake(boolean forUserTake) {
        this.forUserTake = forUserTake;
    }

    public boolean isLoadAvailablePromotion() {
        return loadAvailablePromotion;
    }

    public void setLoadAvailablePromotion(boolean loadAvailablePromotion) {
        this.loadAvailablePromotion = loadAvailablePromotion;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getClientType() {
        return clientType;
    }

    public void setClientType(int clientType) {
        this.clientType = clientType;
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

    public long getTimeBucketId() {
        return timeBucketId;
    }

    public void setTimeBucketId(long timeBucketId) {
        this.timeBucketId = timeBucketId;
    }

    public StoreDateBizSetting getStoreDateBizSetting() {
        return storeDateBizSetting;
    }

    public void setStoreDateBizSetting(StoreDateBizSetting storeDateBizSetting) {
        this.storeDateBizSetting = storeDateBizSetting;
    }

    public boolean isForWechat() {
        return forWechat;
    }

    public void setForWechat(boolean forWechat) {
        this.forWechat = forWechat;
    }

    public boolean isForDelivery() {
        return forDelivery;
    }

    public void setForDelivery(boolean forDelivery) {
        this.forDelivery = forDelivery;
    }
}
