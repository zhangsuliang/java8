package com.huofu.module.i5wei.base;

import halo.query.dal.DALParser;
import halo.query.dal.ParsedInfo;

import java.util.Map;

/**
 * Created by jiajin on 4/29/16.
 */
public class BaseDefaultStoreDbRouter implements DALParser {

    protected static final String i5weiDbBaseName = "huofu_5wei";

    public ParsedInfo parse(Map<String, Object> map) {
        ParsedInfo parsedInfo = new ParsedInfo();
        parsedInfo.setDsKey(i5weiDbBaseName);
        return parsedInfo;
    }
}

