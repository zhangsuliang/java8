package com.huofu.module.i5wei.mealport.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import java.util.List;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.mealport.dbrouter.StoreMealPortDbRouter;

/**
 * 出餐台设置表
 */
@Table(name = "tb_store_meal_port", dalParser = StoreMealPortDbRouter.class)
public class StoreMealPort extends AbsEntity {

    /**
     * 出餐口ID
     */
    @Id
    @Column("port_id")
    private long portId;

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
     * 出餐口名称
     */
    @Column("name")
    private String name;

    /**
     * 字母标识
     */
    @Column("letter")
    private String letter;

    /**
     * 打印机外接设备ID
     */
    @Column("printer_peripheral_id")
    private long printerPeripheralId;

    /**
     * 叫号电视外接设备ID
     */
    @Column("call_peripheral_id")
    private long callPeripheralId;

    /**
     * 叫号规则：0=自动叫号：每个打印的小票自动叫号；（默认）；1=不自动叫号：必须由店员手动叫号；2=尾单叫号：只有出最后一个小票才叫号
     */
    @Column("call_type")
    private int callType;

    /**
     * #bool 是否是打包台(每个店只有一个打包台)
     */
    @Column("has_pack")
    private boolean hasPack;

    /**
     * #bool 是否是外卖台(每个店只有一个外卖台)
     */
    @Column("has_delivery")
    private boolean hasDelivery;

    /**
     * #bool 手动出餐指定时间不出餐，智能切换为自动出餐
     */
    @Column("auto_shift")
    private boolean autoShift;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 是否删除 #bool
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 出餐口叫号信息
     */
    @Column("meal_port_message")
    private String mealPortMessage;

    /**
     * 传菜口Id
     */
    @Column("send_port_id")
    private long sendPortId;

    private StoreMealTask storeMealTask;

    private List<StoreMealPortPeripheral> callMealPortPeripherals;

    private int printAlarm;

    public int getPrintAlarm() {
        return printAlarm;
    }

    public void setPrintAlarm(int printAlarm) {
        this.printAlarm = printAlarm;
    }

    public List<StoreMealPortPeripheral> getCallMealPortPeripherals() {
        return callMealPortPeripherals;
    }

    public void setCallMealPortPeripherals(List<StoreMealPortPeripheral> callMealPortPeripherals) {
        this.callMealPortPeripherals = callMealPortPeripherals;
    }

    public StoreMealTask getStoreMealTask() {
        return storeMealTask;
    }

    public void setStoreMealTask(StoreMealTask storeMealTask) {
        this.storeMealTask = storeMealTask;
    }

    public long getPortId() {
        return portId;
    }

    public void setPortId(long portId) {
        this.portId = portId;
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

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public long getPrinterPeripheralId() {
        return printerPeripheralId;
    }

    public void setPrinterPeripheralId(long printerPeripheralId) {
        this.printerPeripheralId = printerPeripheralId;
    }

    public long getCallPeripheralId() {
        return callPeripheralId;
    }

    public void setCallPeripheralId(long callPeripheralId) {
        this.callPeripheralId = callPeripheralId;
    }

    public int getCallType() {
        return callType;
    }

    public void setCallType(int callType) {
        this.callType = callType;
    }


    public boolean isHasPack() {
        return hasPack;
    }

    public void setHasPack(boolean hasPack) {
        this.hasPack = hasPack;
    }

    public boolean isHasDelivery() {
        return hasDelivery;
    }

    public void setHasDelivery(boolean hasDelivery) {
        this.hasDelivery = hasDelivery;
    }

    public boolean isAutoShift() {
		if (printerPeripheralId <= 0) {
			// 出餐口打印机设置为0，默认不智能切换自动出餐
			return false;
		}
        return autoShift;
    }

    public void setAutoShift(boolean autoShift) {
        this.autoShift = autoShift;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getMealPortMessage() {
        return mealPortMessage;
    }

    public void setMealPortMessage(String mealPortMessage) {
        this.mealPortMessage = mealPortMessage;
    }

    public long getSendPortId() {
        return sendPortId;
    }

    public void setSendPortId(long sendPortId) {
        this.sendPortId = sendPortId;
    }

    public void makeDeleted() {
        this.snapshot();
        this.deleted = true;
        this.updateTime = System.currentTimeMillis();
        this.update();
    }

    public void cancelPack() {
        this.snapshot();
        this.hasPack = false;
        this.updateTime = System.currentTimeMillis();
        this.update();
    }

    public void init4Create() {
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
    }
}