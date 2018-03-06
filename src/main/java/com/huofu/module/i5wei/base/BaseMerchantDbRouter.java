package com.huofu.module.i5wei.base;

import java.util.Map;
import halo.query.dal.DALParser;
import halo.query.dal.DALStatus;
import halo.query.dal.ParsedInfo;

public abstract class BaseMerchantDbRouter implements DALParser {
	private static final int tableMode = 1024;

    private static final int dbMode = 64;

    protected static final String i5weiDbBaseName = "huofu_5wei";

    public abstract String getLogicName();

    protected TableBean build(Map<String, Object> map) {
        //long storeId = (Long) map.get("store_id");
    	int merchantId = (Integer) map.get("merchant_id");
        int tableId = (int) (merchantId % tableMode);
        int dbId = (int) (merchantId % dbMode);
        TableBean tableBean = new TableBean();
        tableBean.setTableId(tableId);
        tableBean.setDbId(dbId);
        return tableBean;
    }

    public static void addInfo(int merchantId, long storeId) {
        DALStatus.addParam("merchant_id", merchantId);
        DALStatus.addParam("store_id", storeId);
    }

    public ParsedInfo parse(Map<String, Object> map) {
        TableBean tableBean = this.build(map);
        ParsedInfo parsedInfo = new ParsedInfo();
        parsedInfo.setDsKey(i5weiDbBaseName + "_" + tableBean.getDbId());
        parsedInfo.setRealTableName(this.getLogicName() + "_" + tableBean.getTableId());
        return parsedInfo;
    }
}
