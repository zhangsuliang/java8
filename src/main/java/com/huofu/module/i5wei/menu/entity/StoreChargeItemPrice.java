package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.menu.dbrouter.StoreChargeItemPriceDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.DateUtil;

/**
 * Auto created by i5weitools
 * 店铺收费项目价格版本
 */
@Table(name = "tb_store_charge_item_price", dalParser = StoreChargeItemPriceDbRouter.class)
public class StoreChargeItemPrice extends AbsEntity {

    /**
     * 价格id
     */
    @Id
    @Column("item_price_id")
    private long itemPriceId;

    /**
     * 收费项目id
     */
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
     * 价格
     */
    @Column("price")
    private long price;

    /**
     * 有效期开始时间
     */
    @Column("begin_time")
    private long beginTime;

    /**
     *
     */
    @Column("end_time")
    private long endTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * #bool 0:未删除 1:已删除
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
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

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getItemPriceId() {
        return itemPriceId;
    }

    public void setItemPriceId(long itemPriceId) {
        this.itemPriceId = itemPriceId;
    }

    public void initForCreate(long now) {
        this.deleted = false;
        this.setCreateTime(now);
        this.setUpdateTime(now);
    }

    /**
     * 检查未来的有效期设置，未来有效期必须最早是第二天开始
     *
     * @param time
     * @throws T5weiException
     */
    public void checkFutureBeginTime(long time) throws T5weiException {
        long today = DateUtil.getBeginTime(time, null);
        long beginDay = DateUtil.getBeginTime(this.beginTime, null);
        if (beginDay <= today) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_PRICE_EXPIRYDATE_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] itemPriceId[" + itemPriceId + "] beginDay[" + beginDay + "] must > today [" + today + "]");
        }
    }

    /**
     * 是否是指定时间有效的价格
     *
     * @param time 指定的时间
     * @return true/false
     */
    public boolean isSpecTimePrice(long time) {
        if (this.beginTime <= time && this.endTime > time) {
            return true;
        }
        return false;
    }

    /**
     * 是否是指定时间之后的未来价格
     *
     * @param time 指定的时间
     * @return true/false
     */
    public boolean isNextPrice(long time) {
        if (this.beginTime >= time) {
            return true;
        }
        return false;
    }

}