package com.huofu.module.i5wei.printer.JsonMap;

import java.util.HashMap;
import java.util.Map;

import com.huofu.module.i5wei.table.entity.StoreTable;

public class StoreTablePrinterJsonMap {
	
	public static Map<String, Object> toMap(StoreTable storeTable, String areaName) {
		Map<String, Object> printMessages = new HashMap<String, Object>();
		printMessages.put("area_name", areaName);
		printMessages.put("table_name", storeTable.getName());
		return printMessages;
	}

}
