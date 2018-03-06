package com.huofu.module.i5wei.inventory.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;

import com.huofu.module.i5wei.base.AbsEntity;
import com.huofu.module.i5wei.inventory.dbrouter.StoreInventoryDbRouter;
import com.huofu.module.i5wei.menu.entity.StoreProduct;

/**
 * Auto created by i5weitools
 */
@Table(name = "tb_store_inventory", dalParser = StoreInventoryDbRouter.class)
public class StoreInventory extends AbsEntity {

    /**
     * 店铺
     */
    @Id
    @Column("inv_id")
    private long invId;

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
     * 产品固定库存ID，自增
     */
    @Column("product_id")
    private long productId;

    /**
     * 剩余可销售数量（支付时扣减库存的剩余）
     */
    @Column("amount")
    private double amount;

    /**
     * 更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 新增时间
     */
    @Column("create_time")
    private long createTime;
    
    /**
     * 非周期库存报警开关：0=关闭，1=开启
     */
    @Column("enable_inv_alarm")
    private boolean enableInvAlarm;
    
    /**
     * 预警数量
     */
    @Column("alarm_amount")
    private double alarmAmount;
    
    /**
     * 产品
     */
    private StoreProduct storeProduct;
    
    public long getInvId() {
        return invId;
    }

    public void setInvId(long invId) {
        this.invId = invId;
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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
	
	public StoreProduct getStoreProduct() {
		return storeProduct;
	}

	public void setStoreProduct(StoreProduct storeProduct) {
		this.storeProduct = storeProduct;
		if (storeProduct != null) {
			storeProduct.setAmount(this.getAmount());
		}
	}

	public void construct(StoreProduct storeProduct){
		if (storeProduct == null) {
			return;
		}
		long currentTime = System.currentTimeMillis();
		this.productId = storeProduct.getProductId();
		this.merchantId = storeProduct.getMerchantId();
		this.storeId = storeProduct.getStoreId();
		this.createTime = currentTime;
		this.updateTime = currentTime;
		this.storeProduct = storeProduct;
	}

}