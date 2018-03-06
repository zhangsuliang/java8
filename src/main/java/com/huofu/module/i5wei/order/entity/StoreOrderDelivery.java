package com.huofu.module.i5wei.order.entity;

import com.huofu.module.i5wei.order.dbrouter.StoreOrderDeliveryDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.waimai.setting.WaimaiTypeEnum;
import huofuhelper.module.base.BaseEntity;

/**
 * Auto created by i5weitools
 * 订单外送信息
 */
@Table(name = "tb_store_order_delivery", dalParser = StoreOrderDeliveryDbRouter.class)
public class StoreOrderDelivery extends BaseEntity {

    /**
     * 订单id
     */
    @Id
    @Column("order_id")
    private String orderId;

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
     * 联系人名称
     */
    @Column("contact_name")
    private String contactName;

    /**
     * 联系人电话
     */
    @Column("contact_phone")
    private String contactPhone;

    /**
     * 送餐员id
     */
    @Column("delivery_staff_id")
    private long deliveryStaffId;

    /**
     * 送餐员 user_id
     */
    @Column("delivery_staff_user_id")
    private long deliveryStaffUserId;

    /**
     * 送达时间
     */
    @Column("delivery_finish_time")
    private long deliveryFinishTime;

    /**
     * 派送开始时间
     */
    @Column("delivery_start_time")
    private long deliveryStartTime;

    /**
     * 指定送达时间
     */
    @Column("delivery_assign_time")
    private long deliveryAssignTime;

    /**
     * 送餐楼宇id
     */
    @Column("delivery_building_id")
    private long deliveryBuildingId;

    /**
     * 送餐楼宇名称
     */
    @Column("delivery_building_name")
    private String deliveryBuildingName;

    /**
     * 送餐楼宇地址
     */
    @Column("delivery_building_address")
    private String deliveryBuildingAddress;

    /**
     * 用户地址
     */
    @Column("user_address")
    private String userAddress;

    /**
     * 备餐开始时间
     */
    @Column("prepare_begin_time")
    private long prepareBeginTime;

	/**
	 * #bool 是否店铺自配送
	 */
	@Column("store_shipping")
	private boolean storeShipping = true;

	/**
	 * 外卖平台订单ID
	 */
	@Column("waimai_order_id")
	private String waimaiOrderId;

	/**
	 * 外卖订单预计送达时间
	 */
	@Column("waimai_delivery_time")
	private long waimaiDeliveryTime;

	/**
	 * 是否第三方配送
	 */
	@Column("waimai_third_shipping")
	private boolean waimaiThirdShipping;


	/**
	 * 外卖平台类型：0=公众号配送（此接口不支持）、1=美团、2=饿了么、3=百度外卖
	 */
	@Column("waimai_type")
	private int waimaiType;

	/**
	 * 外卖订单每日序列号
	 */
	@Column("waimai_day_seq")
	private int waimaiDaySeq;

	/**
	 * 外卖订单支付类型：0=未知、1=货到付款、2=在线支付
	 */
	@Column("waimai_pay_type")
	private int waimaiPayType;

	/**
	 * #bool 用户是否第一次在此门店点餐
	 */
	@Column("waimai_first_order")
	private boolean waimaiFirstOrder;

	/**
	 * #bool 用户是否收藏此门店
	 */
	@Column("waimai_favorites")
	private boolean waimaiFavorites;

	/**
	 * 骑手电话
	 */
	@Column("shipper_phone")
	private String shipperPhone;

	/**
	 * 实际送餐地址纬度
	 */
	@Column("latitude")
	private double latitude;

    /**
     * 实际送餐地址经度
     */
    @Column("longitude")
    private double longitude;
    

    /**
     * 外卖订单退款类型
     */
    @Column("waimai_refund_type")
    private int waimaiRefundType;
    

    /**
     * 自提点id
     */
    @Column("pickup_site_id")
    private long storePickupSiteId;

    /**
     * 自提点名称
     */
    @Column("pickup_site_name")
    private String storePickupName;

    private int deliveryStatus;
    
    /**
     * 商家承担优惠费用
     */
    @Column("poi_money_cent")
    private long poiMoneyCent;
    
    /**
     * 美团承担优惠费用
     */
    @Column("mt_money_cent")
    private long mtMoneyCent;
    
    /**
     * 餐盒费
     */
    @Column("box_price")
    private long boxPrice;
    
    /**
     * 延时接单时间
     */
    @Column("delay_receive_order_minute")
    private int delayReceiveOrderMinute;
    
    /**
     * 接单类型
     */
    @Column("receive_order_type")
    private int receiveOrderType;
    
    /**
     * 备注
     */
    @Column("remarks")
    private String remarks;


    public long getStorePickupSiteId() {
        return storePickupSiteId;
    }

    public void setStorePickupSiteId(long storePickupSiteId) {
        this.storePickupSiteId = storePickupSiteId;
    }

    public String getStorePickupName() {
        return storePickupName;
    }

    public void setStorePickupName(String storePickupName) {
        this.storePickupName = storePickupName;
    }

	/**
	 * 非数据库存储，需要通过订单状态计算得到
	 */
	public int getDeliveryStatus() {
		return deliveryStatus;
	}

    public void setDeliveryStatus(int deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public long getDeliveryStaffId() {
		return deliveryStaffId;
	}

	public void setDeliveryStaffId(long deliveryStaffId) {
		this.deliveryStaffId = deliveryStaffId;
	}

	public long getDeliveryStaffUserId() {
		return deliveryStaffUserId;
	}

	public void setDeliveryStaffUserId(long deliveryStaffUserId) {
		this.deliveryStaffUserId = deliveryStaffUserId;
	}

	public long getDeliveryFinishTime() {
		return deliveryFinishTime;
	}

	public void setDeliveryFinishTime(long deliveryFinishTime) {
		this.deliveryFinishTime = deliveryFinishTime;
	}

	public long getDeliveryStartTime() {
		return deliveryStartTime;
	}

	public void setDeliveryStartTime(long deliveryStartTime) {
		this.deliveryStartTime = deliveryStartTime;
	}

	public long getDeliveryAssignTime() {
		return deliveryAssignTime;
	}

	public void setDeliveryAssignTime(long deliveryAssignTime) {
		this.deliveryAssignTime = deliveryAssignTime;
	}

	public long getDeliveryBuildingId() {
		return deliveryBuildingId;
	}

	public void setDeliveryBuildingId(long deliveryBuildingId) {
		this.deliveryBuildingId = deliveryBuildingId;
	}

	public String getDeliveryBuildingName() {
		return deliveryBuildingName;
	}

	public void setDeliveryBuildingName(String deliveryBuildingName) {
		this.deliveryBuildingName = deliveryBuildingName;
	}

	public String getDeliveryBuildingAddress() {
		return deliveryBuildingAddress;
	}

	public void setDeliveryBuildingAddress(String deliveryBuildingAddress) {
		this.deliveryBuildingAddress = deliveryBuildingAddress;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public long getPrepareBeginTime() {
		return prepareBeginTime;
	}

	public void setPrepareBeginTime(long prepareBeginTime) {
		this.prepareBeginTime = prepareBeginTime;
	}

	public boolean isStoreShipping() {
		return storeShipping;
	}

	public void setStoreShipping(boolean storeShipping) {
		this.storeShipping = storeShipping;
	}

	public String getWaimaiOrderId() {
		return waimaiOrderId;
	}

	public void setWaimaiOrderId(String waimaiOrderId) {
		this.waimaiOrderId = waimaiOrderId;
	}

	public long getWaimaiDeliveryTime() {
		return waimaiDeliveryTime;
	}

	public void setWaimaiDeliveryTime(long waimaiDeliveryTime) {
		this.waimaiDeliveryTime = waimaiDeliveryTime;
	}

	public boolean isWaimaiThirdShipping() {
		return waimaiThirdShipping;
	}

	public void setWaimaiThirdShipping(boolean waimaiThirdShipping) {
		this.waimaiThirdShipping = waimaiThirdShipping;
	}

	public int getWaimaiType() {
		return waimaiType;
	}

	public void setWaimaiType(int waimaiType) {
		this.waimaiType = waimaiType;
	}

	public boolean isWaimaiOrder() {
		if (this.waimaiType == WaimaiTypeEnum.MEITUAN.getValue()) {
			return true;
		} else if (this.waimaiType == WaimaiTypeEnum.ELEME.getValue()) {
			return true;
		} else if (this.waimaiType == WaimaiTypeEnum.BAIDU.getValue()) {
			return true;
		}
		return false;
	}

	public int getWaimaiDaySeq() {
		return waimaiDaySeq;
	}

	public void setWaimaiDaySeq(int waimaiDaySeq) {
		this.waimaiDaySeq = waimaiDaySeq;
	}

	public int getWaimaiPayType() {
		return waimaiPayType;
	}

	public void setWaimaiPayType(int waimaiPayType) {
		this.waimaiPayType = waimaiPayType;
	}

	public boolean isWaimaiFirstOrder() {
		return waimaiFirstOrder;
	}

	public void setWaimaiFirstOrder(boolean waimaiFirstOrder) {
		this.waimaiFirstOrder = waimaiFirstOrder;
	}

	public boolean isWaimaiFavorites() {
		return waimaiFavorites;
	}

	public void setWaimaiFavorites(boolean waimaiFavorites) {
		this.waimaiFavorites = waimaiFavorites;
	}

	public String getShipperPhone() {
		return shipperPhone;
	}

	public void setShipperPhone(String shipperPhone) {
		this.shipperPhone = shipperPhone;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


	public int getWaimaiRefundType() {
		return waimaiRefundType;
	}

	public void setWaimaiRefundType(int waimaiRefundType) {
		this.waimaiRefundType = waimaiRefundType;
	}

	public long getPoiMoneyCent() {
		return poiMoneyCent;
	}

	public void setPoiMoneyCent(long poiMoneyCent) {
		this.poiMoneyCent = poiMoneyCent;
	}

	public long getMtMoneyCent() {
		return mtMoneyCent;
	}

	public void setMtMoneyCent(long mtMoneyCent) {
		this.mtMoneyCent = mtMoneyCent;
	}

	public long getBoxPrice() {
		return boxPrice;
	}

	public void setBoxPrice(long boxPrice) {
		this.boxPrice = boxPrice;
	}


	public int getDelayReceiveOrderMinute() {
		return delayReceiveOrderMinute;
	}

	public void setDelayReceiveOrderMinute(int delayReceiveOrderMinute) {
		this.delayReceiveOrderMinute = delayReceiveOrderMinute;
	}

	public int getReceiveOrderType() {
		return receiveOrderType;
	}

	public void setReceiveOrderType(int receiveOrderType) {
		this.receiveOrderType = receiveOrderType;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}


}