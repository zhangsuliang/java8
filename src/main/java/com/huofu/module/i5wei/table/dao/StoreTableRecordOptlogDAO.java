package com.huofu.module.i5wei.table.dao;

import com.huofu.module.i5wei.table.dbrouter.StoreTableRecordOptlogDbRouter;
import com.huofu.module.i5wei.table.entity.StoreTableRecordOptlog;
import halo.query.dal.DALStatus;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 桌台记录操作日志
 * Created by lixuwei on 17/1/16.
 */
@Repository
public class StoreTableRecordOptlogDAO extends AbsQueryDAO<StoreTableRecordOptlog> {

    private void addDbRouteInfo(int merchantId, long storeId) {
        StoreTableRecordOptlogDbRouter.addInfo(merchantId, storeId);
    }

    @Override
    public void create(StoreTableRecordOptlog storeTableRecordOptlog) {
        this.addDbRouteInfo(storeTableRecordOptlog.getMerchantId(), storeTableRecordOptlog.getStoreId());
        super.create(storeTableRecordOptlog);
    }

    /**
     * 根据桌台记录ID 获取桌台记录的操作日志
     *
     * @param merchantId
     * @param storeId
     * @param tableRecordId
     * @return
     */
    public List<StoreTableRecordOptlog> getByTableReordId(int merchantId, long storeId, long tableRecordId, boolean enableSlave) {
        if (enableSlave) {
            DALStatus.setSlaveMode();
        }
        this.addDbRouteInfo(merchantId, storeId);
        String sql = " where merchant_id = ? and store_id = ? and table_record_id = ?";
        return this.query.list(StoreTableRecordOptlog.class, sql, new Object[]{merchantId, storeId, tableRecordId});
    }
}
