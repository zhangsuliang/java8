package com.huofu.module.i5wei.base;

import halo.query.dal.DALContext;
import halo.query.dal.DALParser;
import halo.query.dal.DALStatus;
import halo.query.dal.ParsedInfo;

import java.util.Map;

/**
 * Created by akwei on 2/16/15.
 */
public abstract class BaseStoreDbRouter implements DALParser {


    private static final int tableMode = 1024;

    private static final int dbMode = 64;

    public static final String i5weiDbBaseName = "huofu_5wei";

    public abstract String getLogicName();

    protected TableBean build(Map<String, Object> map) {
        long storeId = (Long) map.get("store_id");
        int last4 = last4Number(storeId);
        int tableId = last4 % tableMode;
        int dbId = last4 % dbMode;
        TableBean tableBean = new TableBean();
        tableBean.setTableId(tableId);
        tableBean.setDbId(dbId);
        return tableBean;
    }

    public static TableBean build0(long storeId) {
        int last4 = last4Number(storeId);
        int tableId = last4 % tableMode;
        int dbId = last4 % dbMode;
        TableBean tableBean = new TableBean();
        tableBean.setTableId(tableId);
        tableBean.setDbId(dbId);
        return tableBean;
    }

    public static int last4Number(long storeId) {
        String str_storeId = String.valueOf(storeId);
        String last;
        int len = str_storeId.length();
        if (len > 4) {
            last = str_storeId.substring(len - 4, len);
        } else if (len == 4) {
            last = str_storeId;
        } else {
            if (len == 3) {
                last = "0" + str_storeId;
            } else if (len == 2) {
                last = "00" + str_storeId;
            } else if (len == 1) {
                last = "000" + str_storeId;
            } else {
                throw new RuntimeException("storeId len=" + len + ". parse error");
            }
        }
        return Integer.parseInt(last);
    }

    public static void addInfo(int merchantId, long storeId) {
        addInfo(merchantId, storeId, false);
    }

    public static void addInfo(int merchantId, long storeId, boolean enableSlave) {
        DALStatus.addParam("merchant_id", merchantId);
        DALStatus.addParam("store_id", storeId);
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
    }

    public ParsedInfo parse(Map<String, Object> map) {
        TableBean tableBean = this.build(map);
        ParsedInfo parsedInfo = new ParsedInfo();
        parsedInfo.setDsKey(i5weiDbBaseName + "_" + tableBean.getDbId());
        parsedInfo.setRealTableName(this.getLogicName() + "_" + tableBean.getTableId());
        return parsedInfo;
    }

    public static DALContext buildDalContext(int merchantId, long storeId, boolean
            enableSlave) {
        DALContext dalContext = DALContext.create();
        dalContext.setEnableSlave(enableSlave);
        dalContext.addParam("merchant_id", merchantId);
        dalContext.addParam("store_id", storeId);
        return dalContext;
    }


    public static void main(String args[]) {
        long storeId = 16959;
        int last4 = last4Number(storeId);
        int tableId = last4 % tableMode;
        int dbId = last4 % dbMode;
        System.out.println("dbId:"+dbId);
        System.out.println("tableId" + tableId);
    }
}

