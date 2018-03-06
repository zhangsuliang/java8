package com.huofu.module.i5wei.menu.entity;

import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.menu.dbrouter.StoreProductDbRouter;
import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofucore.facade.i5wei.menu.ProductInvTypeEnum;
import huofuhelper.module.base.BaseEntity;
import huofuhelper.util.json.JsonUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto created by i5weitools
 * 店铺产品
 */
@SuppressWarnings("unchecked")
@Table(name = "tb_store_product", dalParser = StoreProductDbRouter.class)
public class StoreProduct extends BaseEntity {

    /**
     * 产品id
     */
    @Id
    @Column("product_id")
    private long productId;

    /**
     * 名称
     */
    @Column("name")
    private String name;

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
     * 单元
     */
    @Column("unit")
    private String unit;

    /**
     * 0=不开启库存，1=开启库存，#bool
     */
    @Column("inv_enabled")
    private boolean invEnabled;

    /**
     * 产品库存类型：0=未开启，1=周营业时段计划库存，2=固定库存，3=周天计划库存
     */
    @Column("inv_type")
    private int invType;

    /**
     * 0=不需要，1=需要统计，#bool
     */
    @Column("meal_stat")
    private boolean mealStat;

    /**
     * 产品周期默认库存
     */
    @Column("week_amount")
    private double weekAmount;

    /**
     * 0:未删除 1:已删除 #bool
     */
    @Column("deleted")
    private boolean deleted;

    /**
     * 创建时间
     */
    @Column("create_time")
    private long createTime;

    /**
     * 最后更新时间
     */
    @Column("update_time")
    private long updateTime;

    /**
     * 产品备注，内容为json
     */
    @Column("remark_data")
    private String remarkData;

    /**
     * 固定库存剩余
     */
    private double amount;

    /**
     * 出餐口id
     */
    @Column("port_id")
    private long portId;

    /**
     * #bool 是否已设置产品成本：0=未设置，1=已设置
     */
    @Column("prime_cost_set")
    private boolean primeCostSet;

    /**
     * 产品成本(分)
     */
    @Column("prime_cost")
    private long primeCost;

    /**
     * 是否设置了入客数
     */
    @Column("enable_customer_traffic")
    private boolean enableCustomerTraffic;

    /**
     * 入客数
     */
    @Column("customer_traffic")
    private int customerTraffic = 1;

    /**
     * 分单规则：0=不分单（默认），1=按品项分单，2=按份数分单
     */
    @Column("div_rule")
    private int divRule;

    /**
     * 备注是否是必选
     */
    @Column("remark_must_selected")
    private boolean remarkMustSelected;

    /**
     * 备注是否支持多选
     */
    @Column("enable_remark_mutli_selected")
    private boolean enableRemarkMutliSelected = true;

    /**
     * 只是为了进行BeanUtil.copy,不要删除
     */
    @SuppressWarnings("unused")
    private List<String> remarks;

    private StoreMealPort storeMealPort;

    /**
     * 分类ID
     */
    @Column("category_id")
    private int categoryId;

    public boolean isRemarkMustSelected() {
        return remarkMustSelected;
    }

    public void setRemarkMustSelected(boolean remarkMustSelected) {
        this.remarkMustSelected = remarkMustSelected;
    }

    public boolean isEnableRemarkMutliSelected() {
        return enableRemarkMutliSelected;
    }

    public void setEnableRemarkMutliSelected(boolean enableRemarkMutliSelected) {
        this.enableRemarkMutliSelected = enableRemarkMutliSelected;
    }

    public StoreMealPort getStoreMealPort() {
        return storeMealPort;
    }

    public void setStoreMealPort(StoreMealPort storeMealPort) {
        this.storeMealPort = storeMealPort;
    }

    public long getPortId() {
        return portId;
    }

    public void setPortId(long portId) {
        this.portId = portId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isInvEnabled() {
        return invEnabled;
    }

    public void setInvEnabled(boolean invEnabled) {
        this.invEnabled = invEnabled;
    }

    public int getInvType() {
        if (invType == 0) {
            //默认周营业时段计划库存
            invType = ProductInvTypeEnum.WEEK.getValue();
        }
        return invType;
    }

    public void setInvType(int invType) {
        this.invType = invType;
    }

    public boolean isMealStat() {
        return mealStat;
    }

    public void setMealStat(boolean mealStat) {
        this.mealStat = mealStat;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public double getWeekAmount() {
        return weekAmount;
    }

    public void setWeekAmount(double weekAmount) {
        this.weekAmount = weekAmount;
    }

    public String getRemarkData() {
        return remarkData;
    }

    public void setRemarkData(String remarkData) {
        this.remarkData = remarkData;
    }

    public List<String> getRemarks() {
        if (this.remarkData == null) {
            return new ArrayList<>(0);
        }
        try {
            List<String> list = (List<String>) JsonUtil.parse(this.remarkData, List.class);
            if (list == null) {
                list = new ArrayList<>(0);
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>(0);
        }
    }

    public void setRemarks(List<String> remarks) {
        try {
            this.remarkData = JsonUtil.build(remarks);
        } catch (Exception e) {
            this.remarkData = "";
        }
    }

    public void makeDeleted(long now) {
        this.snapshot();
        this.setDeleted(true);
        this.updateTime = now;
        this.update();
    }

    public void initForCreate(long now) {
        this.deleted = false;
        this.setCreateTime(now);
        this.setUpdateTime(now);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean isPrimeCostSet() {
        return primeCostSet;
    }

    public void setPrimeCostSet(boolean primeCostSet) {
        this.primeCostSet = primeCostSet;
    }

    public long getPrimeCost() {
        return primeCost;
    }

    public void setPrimeCost(long primeCost) {
        this.primeCost = primeCost;
    }

    public boolean isEnableCustomerTraffic() {
        return enableCustomerTraffic;
    }

    public void setEnableCustomerTraffic(boolean enableCustomerTraffic) {
        this.enableCustomerTraffic = enableCustomerTraffic;
    }

    public int getCustomerTraffic() {
        return customerTraffic;
    }

    public void setCustomerTraffic(int customerTraffic) {
        this.customerTraffic = customerTraffic;
    }

    public int getDivRule() {
        return divRule;
    }

    public void setDivRule(int divRule) {
        this.divRule = divRule;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
}