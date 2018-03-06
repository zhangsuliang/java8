package com.huofu.module.i5wei.mealport.entity;

import java.util.List;
import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.mealport.dbrouter.StoreMealPortSendDBRouter;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.mealportsend.StoreMealSendPortEnum;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_meal_port_send" , dalParser = StoreMealPortSendDBRouter.class)
public class StoreMealPortSend extends AbsEntity {
    /**
     * 主键，自增ID，传菜口ID
     */
	@Id
    @Column("send_port_id")
    private long sendPortId;

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
     * 传菜口名称
     */
    @Column("send_port_name")
    private String sendPortName;

    /**
     * 划菜类型：纸划菜=0（默认），IPad划菜=1
     */
    @Column("sweep_type")
    private int sweepType;

    /**
     * 是否按桌划菜，否=0，是=1（默认）
     */
    @Column("table_sweep")
    private boolean tableSweep;

    /**
     * 打印机ID
     */
    @Column("printer_peripheral_id")
    private long printerPeripheralId;

    /**
     * 是否打印出菜分单
     */
    @Column("print_div_item")
    private boolean printDivItem;

    /**
     * 叫号设备ID
     */
    @Column("call_peripheral_id")
    private long callPeripheralId;

    /**
     * 叫号规则：0=自动叫号：每个打印的小票自动叫号；（默认）；1=不自动叫号：必须由店员手动叫号；2=尾单叫号：只有出最后一个小票才叫号
     */
    @Column("call_type")
    private int callType;

    /**
     * 叫号信息
     */
    @Column("call_message")
    private String callMessage;

    /**
     * 是否打包传菜口
     */
    @Column("has_pack")
    private boolean hasPack;

    /**
     * 是否外卖传菜口
     */
    @Column("has_delivery")
    private boolean hasDelivery;
    
    /**
     *  是否为主传菜口,否=0（默认），是=1
     */
    @Column("master_send_port")
    private boolean masterSendPort;

    /**
     * 传菜口类型：传菜口=0（默认），打包台=1，外卖台=2
     */
    @Column("send_port_type")
    private int sendPortType;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 修改时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 加工档口
     */
    private List<StoreMealPort> storeMealPorts;

    public long getSendPortId() {
        return sendPortId;
    }

    public void setSendPortId(long sendPortId) {
        this.sendPortId = sendPortId;
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

    public String getSendPortName() {
        return sendPortName;
    }

    public void setSendPortName(String sendPortName) {
        this.sendPortName = sendPortName;
    }

    public int getSweepType() {
        return sweepType;
    }

    public void setSweepType(int sweepType) {
        this.sweepType = sweepType;
    }

    public boolean isTableSweep() {
        return tableSweep;
    }

    public void setTableSweep(boolean tableSweep) {
        this.tableSweep = tableSweep;
    }

    public long getPrinterPeripheralId() {
        return printerPeripheralId;
    }

    public void setPrinterPeripheralId(long printerPeripheralId) {
        this.printerPeripheralId = printerPeripheralId;
    }

    public boolean isPrintDivItem() {
        return printDivItem;
    }

    public void setPrintDivItem(boolean printDivItem) {
        this.printDivItem = printDivItem;
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

    public String getCallMessage() {
        return callMessage;
    }

    public void setCallMessage(String callMessage) {
        this.callMessage = callMessage;
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

    public boolean isMasterSendPort() {
        return masterSendPort;
    }

    public void setMasterSendPort(boolean masterSendPort) {
        this.masterSendPort = masterSendPort;
    }

    public int getSendPortType() {
        return sendPortType;
    }

    public void setSendPortType(int sendPortType) {
        this.sendPortType = sendPortType;
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

    public List<StoreMealPort> getStoreMealPorts() {
        return storeMealPorts;
    }

    public void setStoreMealPorts(List<StoreMealPort> storeMealPorts) {
        this.storeMealPorts = storeMealPorts;
    }
    
    /**
     * 打包台和外卖台没有设置打印机，则默认使用主传菜口的打印机
     * @param masterStoreMealPortSend
     */
    public void setPrinterPeripheralId(StoreMealPortSend masterStoreMealPortSend){
        if(masterStoreMealPortSend != null && (this.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_PACKAGE_PORT.getValue() || this.getSendPortType() == StoreMealSendPortEnum.STORE_MEAL_DELIVERY_PORT.getValue())
                && masterStoreMealPortSend.isMasterSendPort()){
            if(this.getPrinterPeripheralId() <= 0){
                this.setPrinterPeripheralId(masterStoreMealPortSend.getPrinterPeripheralId());
            }
        }
    }
}