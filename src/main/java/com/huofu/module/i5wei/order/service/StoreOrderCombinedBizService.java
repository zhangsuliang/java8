package com.huofu.module.i5wei.order.service;

import com.huofu.module.i5wei.order.dao.StoreOrderCombinedBizDao;
import com.huofu.module.i5wei.order.entity.StoreOrderCombinedBiz;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by wangxiaoyang on 16/8/20
 */
@Service
public class StoreOrderCombinedBizService {

    @Autowired
    private StoreOrderCombinedBizDao storeOrderCombinedBizDao;

    public StoreOrderCombinedBiz getByOrderIdAndType(int merchantId, long storeId, String orderId, int bizType, boolean enableSlave) {
        List<StoreOrderCombinedBiz> storeOrderCombinedBizs = this.storeOrderCombinedBizDao.getByOrderIdAndType(merchantId, storeId, orderId, bizType, enableSlave);
        if (storeOrderCombinedBizs.isEmpty()) {
            return null;
        }
        return storeOrderCombinedBizs.get(0);
    }
}
