package com.huofu.module.i5wei.menu.entity;

import halo.query.annotation.Column;
import halo.query.annotation.Id;
import halo.query.annotation.Table;
import huofuhelper.module.base.BaseEntity;

/**
 * Auto created by i5weitools
 * 产品项目单位
 */
@Table(name = "tb_item_unit")
public class ItemUnit extends BaseEntity {

    /**
     * 单位id
     */
    @Id
    @Column("item_unit_id")
    private int itemUnitId;

    /**
     * 名称
     */
    @Column("name")
    private String name;

    /**
     * 单位类型
     */
    @Column("unit_type")
    private int unitType;

    public int getItemUnitId() {
        return itemUnitId;
    }

    public void setItemUnitId(int itemUnitId) {
        this.itemUnitId = itemUnitId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnitType() {
        return unitType;
    }

    public void setUnitType(int unitType) {
        this.unitType = unitType;
    }
}