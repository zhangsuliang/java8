package com.huofu.module.i5wei.printer.JsonMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.huofu.module.i5wei.order.entity.StoreOrder;
import com.huofu.module.i5wei.table.entity.StoreTableRecord;
import huofucore.facade.i5wei.meal.StoreMealChargeDTO;
import huofucore.facade.i5wei.meal.StoreMealDTO;

public class StoreOrderPrinterJsonMap {
	
	public static Map<String, Object> toMap(StoreOrder storeOrder) {
		Map<String, Object> printMessage = new HashMap<String, Object>();
		printMessage.put("order_id", storeOrder.getOrderId());
		printMessage.put("take_serial_number", storeOrder.getTakeSerialNumber());
		printMessage.put("site_number", storeOrder.getSiteNumber());
		printMessage.put("order_remark",storeOrder.getOrderRemark());
		printMessage.put("create_time",storeOrder.getCreateTime());
		printMessage.put("take_serial_time",storeOrder.getTakeSerialTime());
		return printMessage;
	}

	public static Map<String, Object> toMap(StoreOrder storeOrder, StoreTableRecord storeTableRecord) {
		Map<String, Object> printMessage = new HashMap<String, Object>();
		printMessage.put("order_id", storeOrder.getOrderId());
		printMessage.put("take_serial_number", storeOrder.getTakeSerialNumber());
		printMessage.put("table_record_id", storeTableRecord.getTableRecordId());
		printMessage.put("table_name", storeTableRecord.getTableName());
		printMessage.put("area_name", storeTableRecord.getAreaName());
		printMessage.put("order_remark",storeOrder.getOrderRemark());
		printMessage.put("create_time",storeOrder.getCreateTime());
		printMessage.put("take_serial_time",storeOrder.getTakeSerialTime());
		return printMessage;
	}

	public static Map<String, Object> toMap(StoreTableRecord storeTableRecord,long sendTime,long staffId) {
		Map<String, Object> printMessage = new HashMap<String, Object>();
		printMessage.put("table_record_id", storeTableRecord.getTableRecordId());
		printMessage.put("table_name", storeTableRecord.getTableName());
		printMessage.put("area_name", storeTableRecord.getAreaName());
		printMessage.put("send_time", sendTime);
		printMessage.put("table_record_seq",storeTableRecord.getTableRecordSeq());
		return printMessage;
	}


	public static Map<String, Object> toMap(StoreOrder storeOrder,List<Map<String,Object>> storeMealPrinterMapList) {
		Map<String, Object> printMessage = toMap(storeOrder);
		printMessage.put("store_meal_list",storeMealPrinterMapList);
		return printMessage;
	}


	public static Map<String, Object> toMap(StoreOrder storeOrder, StoreTableRecord storeTableRecord,List<Map<String,Object>> storeMealPrinterMapList) {
		Map<String, Object> printMessage = toMap(storeOrder,storeTableRecord);
		printMessage.put("store_meal_list",storeMealPrinterMapList);
		return printMessage;
	}

}
