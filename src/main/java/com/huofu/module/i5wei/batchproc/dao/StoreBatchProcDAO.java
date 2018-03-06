package com.huofu.module.i5wei.batchproc.dao;

import com.huofu.module.i5wei.batchproc.entity.StoreBatchProc;

import halo.query.dal.DALContext;
//import halo.query.dal.DALContext;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.AbsQueryDAO;
import org.springframework.stereotype.Repository;

@Repository
public class StoreBatchProcDAO extends AbsQueryDAO<StoreBatchProc> {

    private DALContext createDalContext(int merchantId, long storeId) {
        DALContext dalContext = DALContext.create();
        dalContext.addParam("merchant_id", merchantId);
        dalContext.addParam("store_id", storeId);
        return dalContext;
    }

    @Override
    public void create(StoreBatchProc storeBatchProc) {
        this.query.insertForNumber(storeBatchProc, this.createDalContext(storeBatchProc.getMerchantId(), storeBatchProc.getStoreId()));
    }

    @Override
    public void update(StoreBatchProc storeBatchProc, StoreBatchProc snapshot) {
        this.query.update(storeBatchProc, snapshot, this.createDalContext(storeBatchProc.getMerchantId(), storeBatchProc.getStoreId()));
    }

    public StoreBatchProc loadById(int merchantId, long storeId, long batchId) throws T5weiException {
        StoreBatchProc obj = this.query.objById(StoreBatchProc.class, batchId, this.createDalContext(merchantId, storeId));
        if (obj == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_BATCH_PROC_INVALID.getValue(), "storeId[" + storeId + "] storeBatchProcId[" + batchId + "] invalid");
        }
        return obj;
    }
}
