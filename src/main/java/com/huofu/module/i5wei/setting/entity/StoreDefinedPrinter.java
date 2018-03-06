package com.huofu.module.i5wei.setting.entity;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.base.BaseDefaultStoreDbRouter;
import com.huofu.module.i5wei.setting.dbrouter.StoreDefinedPrinterDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

/**
 * Auto created by i5weitools
 * 店铺自定义打印
 */
@Table(name = "tb_store_defined_printer",dalParser=StoreDefinedPrinterDbRouter.class)
public class StoreDefinedPrinter extends AbsEntity {

    /**
     * 自定义打印ID，主键
     */
    @Id
    @Column("printer_id")
    private long printerId;

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
     * 自定义打印名称
     */
    @Column("printer_name")
    private String printerName;

    /**
     * 打印机外接设备ID
     */
    @Column("printer_peripheral_id")
    private long printerPeripheralId;

    /**
     * 7=点菜单，8=加工总单（需要按照档口分类），9=取餐单，10=打包清单，11=外卖清单
     */
    @Column("print_msg_type")
    private int printMsgType;

    /**
     * 打印范围，桌台模式下，如果print_msg_type = 1，print_scope=[档口ID1，档口ID2]；非桌台模式 print_type=9，print_scope=[区域ID1，区域ID2]
     */
    @Column("print_scope")
    private String printScope;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

    public long getPrinterId() {
        return printerId;
    }

    public void setPrinterId(long printerId) {
        this.printerId = printerId;
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

    public String getPrinterName() {
        return printerName;
    }

    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }

    public long getPrinterPeripheralId() {
        return printerPeripheralId;
    }

    public void setPrinterPeripheralId(long printerPeripheralId) {
        this.printerPeripheralId = printerPeripheralId;
    }

    public int getPrintMsgType() {
        return printMsgType;
    }

    public void setPrintMsgType(int printMsgType) {
        this.printMsgType = printMsgType;
    }

    public String getPrintScope() {
        return printScope;
    }

    public void setPrintScope(String printScope) {
        this.printScope = printScope;
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
}