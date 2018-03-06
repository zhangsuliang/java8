package com.huofu.module.i5wei.batchproc.service;

import com.huofu.module.i5wei.batchproc.dao.StoreBatchProcDAO;
import com.huofu.module.i5wei.batchproc.entity.StoreBatchProc;
import huofucore.facade.i5wei.exception.T5weiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by akwei on 9/26/16.
 */
@Service
public class StoreBatchProcService {

    @Autowired
    private StoreBatchProcDAO storeBatchProcDAO;

    public StoreBatchProc getStoreBatchProc(int merchantId, long storeId, long batchId) throws T5weiException {
        return this.storeBatchProcDAO.loadById(merchantId, storeId, batchId);
    }
}
