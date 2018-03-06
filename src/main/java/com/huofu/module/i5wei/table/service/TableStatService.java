package com.huofu.module.i5wei.table.service;

import com.huofu.module.i5wei.table.dao.StoreTableRecordDAO;
import huofucore.facade.i5wei.table.TableExpectIncomeDTO;
import huofucore.facade.i5wei.table.TableRecordStatusEnum;
import huofuhelper.util.ObjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dingmingyang on 16/12/16.
 */
@Service
public class TableStatService {

    @Autowired
    private StoreTableRecordDAO storeTableRecordDAO;

    /**
     * <pre>
     *     dmy 1216
     *     未结账订单预期收入统计
     * </pre>
     * @param storeId 店铺ID
     * @param timeBucketId 营业时段(为0则查询全天)
     * @param repastDate 就餐日期
     * @return TableExpectIncomeDTO
     */
    public TableExpectIncomeDTO getTableExpectIncome(int merchantId, long storeId, long timeBucketId, long repastDate){
        // SQL
        String tableName = storeTableRecordDAO.getRealName(merchantId, storeId);
        String sql4TimeBucket = (timeBucketId > 0 ? " and time_bucket_id = ? " : "");
        String sql = " select count(*) as orders, sum(pay_able_amount) as table_expect_income " +
                " from " + tableName +
                " where store_id = ? and repast_date = ? " + sql4TimeBucket +
                " and table_record_status not in (?, ?) and pay_able_amount > 0 and merge_table_record_id = 0 ";

        // 参数
        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(storeId); // 店铺ID
        sqlParams.add(repastDate); // 就餐日期
        if (timeBucketId > 0){
            sqlParams.add(timeBucketId); // 营业时段
        }
        sqlParams.add(TableRecordStatusEnum.SETTLEMENT.getValue()); // 4=已结账
        sqlParams.add(TableRecordStatusEnum.CLEAR_TABLE.getValue()); // 5=已清台

        // 返回值
        List<Map<String, Object>> resultMapList = storeTableRecordDAO.getResultMapList(merchantId, storeId, sql, sqlParams.toArray(), true);
        int orders = ObjectUtil.getInt(resultMapList.get(0).get("orders"), 0);
        long tableExpectIncome = ObjectUtil.getLong(resultMapList.get(0).get("table_expect_income"), 0L);
        TableExpectIncomeDTO tableExpectIncomeDTO = new TableExpectIncomeDTO();
        tableExpectIncomeDTO.setOrders(orders);
        tableExpectIncomeDTO.setExpectAmount(tableExpectIncome);
        return tableExpectIncomeDTO;
    }

}
