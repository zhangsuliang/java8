package com.huofu.module.i5wei.table.facade;

import com.huofu.module.i5wei.table.service.TableStatService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.table.*;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by dingmingyang on 16/12/16.
 */
@Component
@ThriftServlet(name = "tableStatFacadeServlet", serviceClass = TableStatFacade.class)
public class TableStatFacadeImpl implements TableStatFacade.Iface{

    @Autowired
    private TableStatService tableStatService;

    /**
     * 桌台预期收入统计
     *
     * @param param
     */
    @Override
    public TableExpectIncomeDTO getTableExpectIncomeReport(TableExpectIncomeParam param) throws T5weiException, TException {
        // 参数校验
        if (param == null) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), " param is null ");
        }
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long timeBucketId = param.getTimeBucketId();
        long repastDate = param.getRepastDate();
        if (storeId == 0 || repastDate == 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), " param is error ");
        }

        // 返回值
        TableExpectIncomeDTO tableExpectIncome = tableStatService.getTableExpectIncome(merchantId, storeId, timeBucketId, repastDate);
        return tableExpectIncome;
    }
}
