package com.huofu.module.i5wei.table.service;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.table.dao.StoreTableRecordOptlogDAO;
import com.huofu.module.i5wei.table.entity.StoreTableRecordOptlog;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 桌台记录操作日志业务类
 * Created by lixuwei on 17/1/16.
 */
@Service
public class StoreTableRecordOptlogService {
    
    @Autowired
    private StoreTableRecordOptlogDAO storeTableRecordOptlogDAO;

    /**
     * 创建桌台记录操作日志
     * @param merchantId 商户ID
     * @param storeId 店铺ID
     * @param tableRecordId 桌台记录ID
     * @param staffId 服务员ID
     * @param userId 用户ID
     * @param clientType 客户端类型
     * @param optType 操作类型
     * @param remark 备注
     * @param optTime 操作时间
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public void createTableRecordOptlog(int merchantId, long storeId, long tableRecordId, long staffId, long userId,
                                        int clientType, int optType, String remark, long optTime) throws T5weiException {

        if (merchantId == 0 || storeId ==0 ) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId["+merchantId+"] storeId["+storeId+"]");
        }

        StoreTableRecordOptlog storeTableRecordOptlog = new StoreTableRecordOptlog();
        storeTableRecordOptlog.setMerchantId(merchantId);
        storeTableRecordOptlog.setStoreId(storeId);
        storeTableRecordOptlog.setTableRecordId(tableRecordId);
        storeTableRecordOptlog.setStaffId(staffId);
        storeTableRecordOptlog.setUserId(userId);
        storeTableRecordOptlog.setClientType(clientType);
        storeTableRecordOptlog.setOptType(optType);
        storeTableRecordOptlog.setRemark(remark);
        storeTableRecordOptlog.setOptTime(optTime);
        storeTableRecordOptlog.setCreateTime(System.currentTimeMillis());
        storeTableRecordOptlog.create();
    }

    /**
     * 查询桌台记录操作日志
     *
     * @param merchantId    商户ID
     * @param storeId       店铺ID
     * @param tableRecordId 桌台记录ID
     */
    public List<StoreTableRecordOptlog> queryStoreTableRecordOptlog(int merchantId, long storeId, long tableRecordId) {
        if (merchantId == 0 || storeId == 0 || tableRecordId == 0) {
            return Lists.newArrayList();
        }
        return storeTableRecordOptlogDAO.getByTableReordId(merchantId, storeId, tableRecordId, true);
    }

}
