package com.huofu.module.i5wei.table.dbrouter;

import halo.query.dal.ParsedInfo;

import java.util.Map;

import com.huofu.module.i5wei.base.BaseStoreDbRouter;
import com.huofu.module.i5wei.base.TableBean;

/**
 * 桌台记录router
 * @author licheng7
 * 2016年5月9日 下午5:20:03
 */
public class TableRecordBatchRefundRecordDbRouter extends BaseStoreDbRouter {

    private static final String baseName = "tb_table_record_batch_refund_record";

    @Override
    public String getLogicName() {
        return baseName;
    }
    
    public ParsedInfo parse(Map<String, Object> map) {
        ParsedInfo parsedInfo = new ParsedInfo();
        parsedInfo.setDsKey(i5weiDbBaseName);
        parsedInfo.setRealTableName(this.getLogicName());
        return parsedInfo;
    }
}
