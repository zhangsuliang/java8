package com.huofu.module.i5wei.inventory.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import com.huofu.module.i5wei.inventory.dbrouter.StoreInventoryInvsetDbRouter;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_inventory_invset", dalParser = StoreInventoryInvsetDbRouter.class)
public class StoreInventoryInvset {

    /**
     * 店铺进销存ID，自增主键
     */
    @Id
    @Column("inv_invest_id")
    private long invInvestId;

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
     * 产品ID
     */
    @Column("product_id")
    private long productId;

    /**
     * 订单ID
     */
    @Column("order_id")
    private String orderId;

    /**
     * 原剩余可销售库存量
     */
    @Column("inventory")
    private double inventory;

    /**
     * 变更数量：正数表示增加，负数表示减少
     */
    @Column("amount")
    private double amount;

    /**
     * 剩余可销售库存量
     */
    @Column("remain")
    private double remain;

    /**
     * 备注，记录数量改变原因
     */
    @Column("remark")
    private String remark;

    /**
     * 新增时间
     */
    @Column("create_time")
    private long createTime;
    
    /**
     * 名称
     */
    private String name;

    /**
     * 单元
     */
    private String unit;
    
    /**
     * 非周期库存报警开关：0=关闭，1=开启
     */
    private boolean enableInvAlarm;
    
    /**
     * 预警数量
     */
    private double alarmAmount;
    
    public long getInvInvestId() {
        return invInvestId;
    }

    public void setInvInvestId(long invInvestId) {
        this.invInvestId = invInvestId;
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

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getInventory() {
        return inventory;
    }

    public void setInventory(double inventory) {
        this.inventory = inventory;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getRemain() {
        return remain;
    }

    public void setRemain(double remain) {
        this.remain = remain;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

	public boolean isEnableInvAlarm() {
		return enableInvAlarm;
	}

	public void setEnableInvAlarm(boolean enableInvAlarm) {
		this.enableInvAlarm = enableInvAlarm;
	}

	public double getAlarmAmount() {
		return alarmAmount;
	}

	public void setAlarmAmount(double alarmAmount) {
		this.alarmAmount = alarmAmount;
	}

	/**
     * 库存是否报警
     */
	public boolean isInvAlarm() {
		if (!enableInvAlarm) {
			return false;
		}
		if (alarmAmount > 0) {
			// 库存报警开启，库存从非报警数量变为小于等于报警数量
			if (inventory >= alarmAmount && remain < alarmAmount) {
				return true;
			}
		}
		return false;
	}

}