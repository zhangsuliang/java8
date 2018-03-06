package com.huofu.module.i5wei.printer.JsonMap;

import com.huofu.module.i5wei.order.entity.StoreOrderItem;
import com.huofu.module.i5wei.order.entity.StoreOrderRefundItem;
import huofuhelper.util.NumberUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jiajin.nervous on 16/10/20.
 */
public class StoreOrderItemJsonMap {

    public static Map<String, Object> toMap(StoreOrderItem storeOrderItem) {
        Map<String, Object> printMessage = new HashMap<String, Object>();
        printMessage.put("charge_item_id", storeOrderItem.getChargeItemId());
        printMessage.put("charge_item_name", storeOrderItem.getChargeItemName());
        printMessage.put("price", storeOrderItem.getPrice());
        printMessage.put("amount", storeOrderItem.getAmount());
        printMessage.put("unit", storeOrderItem.getUnit());
        return printMessage;
    }

    public static List<Map<String, Object>> toMapList(List<StoreOrderItem> storeOrderItemList) {
        List<Map<String, Object>> mapList = new ArrayList<>();
        for(StoreOrderItem storeOrderItem : storeOrderItemList){
            mapList.add(toMap(storeOrderItem));
        }
        return mapList;
    }
}
