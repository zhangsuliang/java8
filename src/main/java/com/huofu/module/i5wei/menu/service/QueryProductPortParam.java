package com.huofu.module.i5wei.menu.service;

import java.util.List;

/**
 * Created by akwei on 10/14/15.
 */
public class QueryProductPortParam {

    private long chargeItemId;

    private List<Long> productIds;

    public long getChargeItemId() {
        return chargeItemId;
    }

    public void setChargeItemId(long chargeItemId) {
        this.chargeItemId = chargeItemId;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }
}
