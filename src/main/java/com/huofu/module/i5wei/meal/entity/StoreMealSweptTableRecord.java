package com.huofu.module.i5wei.meal.entity;

import java.util.List;

/**
 * 已划完菜的桌台记录和订单
 */
public class StoreMealSweptTableRecord {
    
    private long tableRecordId;
    
    private List<String> orderIds;

    public List<String> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
    }

    public long getTableRecordId() {
        return tableRecordId;
    }

    public void setTableRecordId(long tableRecordId) {
        this.tableRecordId = tableRecordId;
    }
}
