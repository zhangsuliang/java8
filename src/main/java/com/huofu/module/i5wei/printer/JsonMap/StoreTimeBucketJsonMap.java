package com.huofu.module.i5wei.printer.JsonMap;

import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.order.entity.StoreOrder;
import huofuhelper.util.NumberUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiajin.nervous on 16/10/20.
 */
public class StoreTimeBucketJsonMap {

    public static Map<String, Object> toMap(StoreTimeBucket storeTimeBucket) {
        Map<String, Object> printMessage = new HashMap<String, Object>();
        printMessage.put("name", storeTimeBucket.getName());
        printMessage.put("time_bucket_id", storeTimeBucket.getTimeBucketId());
        printMessage.put("start_time", storeTimeBucket.getStartTime());
        printMessage.put("end_time", storeTimeBucket.getEndTime());
        printMessage.put("delivery_supported", NumberUtil.bool2Int(storeTimeBucket.isDeliverySupported()));
        printMessage.put("table_fee", storeTimeBucket.getTableFee());
        printMessage.put("tips", storeTimeBucket.getTips());
        return printMessage;
    }
}
