package com.huofu.module.i5wei.printer.JsonMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huofu.module.i5wei.order.entity.StoreOrderSubitem;

/**
 * Auto created by i5weitools
 */
public class StoreOrderSubitemJsonMap {
	
	public static List<Map<String, Object>> toMapList(List<StoreOrderSubitem> storeOrderSubItems) {
		List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
		if (storeOrderSubItems == null || storeOrderSubItems.isEmpty()) {
			return mapList;
		}
		for(StoreOrderSubitem storeOrderSubitem:storeOrderSubItems){
			Map<String, Object> map = toMap(storeOrderSubitem);
			mapList.add(map);
		}
		return mapList;
	}

    public static Map<String, Object> toMap(StoreOrderSubitem storeOrderSubItem) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("charge_item_id",storeOrderSubItem.getChargeItemId());
        map.put("product_id",storeOrderSubItem.getProductId());
        map.put("amount",storeOrderSubItem.getAmount());
        map.put("remark",storeOrderSubItem.getRemark());
        map.put("product",getStoreProduct(storeOrderSubItem));
        return map;
    }
    
    private static Map<String, Object> getStoreProduct(StoreOrderSubitem storeOrderSubItem){
    	Map<String, Object> map = new HashMap<String, Object>();
    	map.put("product_id",storeOrderSubItem.getProductId());
        map.put("product_name",storeOrderSubItem.getProductName());
        map.put("unit",storeOrderSubItem.getUnit());
        return map;
    }
}