package com.huofu.module.i5wei.delivery.dbrouter;

import halo.query.dal.DALParser;
import halo.query.dal.ParsedInfo;

import java.util.Map;

/**
 * Created by akwei on 5/16/15.
 */
public class NoDistributeDbRouter implements DALParser {

    private static final String dsKey = "huofu_5wei";

    @Override
    public ParsedInfo parse(Map<String, Object> paramMap) {
        ParsedInfo parsedInfo = new ParsedInfo();
        parsedInfo.setDsKey(dsKey);
        return parsedInfo;
    }
}
