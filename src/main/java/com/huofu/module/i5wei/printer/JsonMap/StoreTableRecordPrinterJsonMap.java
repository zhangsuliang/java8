package com.huofu.module.i5wei.printer.JsonMap;

import java.util.HashMap;
import java.util.Map;

import com.huofu.module.i5wei.table.entity.StoreTableRecord;

public class StoreTableRecordPrinterJsonMap {

	public static Map<String, Object> toMap(StoreTableRecord storeTableRecord) {
		Map<String, Object> printMessage = new HashMap<String, Object>();
		printMessage.put("area_name", storeTableRecord.getAreaName());
		printMessage.put("table_name", storeTableRecord.getTableName());
		printMessage.put("table_record_seq", storeTableRecord.getTableRecordSeq());
		return printMessage;
	}
	
}
