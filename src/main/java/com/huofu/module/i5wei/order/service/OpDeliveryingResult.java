package com.huofu.module.i5wei.order.service;

import com.huofu.module.i5wei.order.entity.StoreOrder;

import java.util.List;

public class OpDeliveryingResult {

    /**
     * 派送的员工userId
     */
    private long deliverStaffUserId;

    /**
     * 新添加的派送数量
     */
    private int newAdd;

    /**
     * 总共派送数量
     */
    private int total;

    private List<StoreOrder> storeOrders;

    public List<StoreOrder> getStoreOrders() {
        return storeOrders;
    }

    public void setStoreOrders(List<StoreOrder> storeOrders) {
        this.storeOrders = storeOrders;
    }

    public long getDeliverStaffUserId() {
        return deliverStaffUserId;
    }

    public void setDeliverStaffUserId(long deliverStaffUserId) {
        this.deliverStaffUserId = deliverStaffUserId;
    }

    public int getNewAdd() {
        return newAdd;
    }

    public void setNewAdd(int newAdd) {
        this.newAdd = newAdd;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
