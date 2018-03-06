package com.huofu.module.i5wei.batchproc.facade;

import com.huofu.module.i5wei.batchproc.entity.StoreBatchProc;
import com.huofu.module.i5wei.batchproc.service.StoreBatchProcService;
import huofucore.facade.i5wei.batchproc.StoreBatchProcFacade;
import huofucore.facade.i5wei.batchproc.StoreBatchProcResultDTO;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 批量任务服务
 * Created by akwei on 9/26/16.
 */
@Component
@ThriftServlet(name = "storeBatchProcFacadeServlet", serviceClass = StoreBatchProcFacade.class)
public class StoreBatchProcFacadeImpl implements StoreBatchProcFacade.Iface {

    @Autowired
    private StoreBatchProcService storeBatchProcService;

    @Override
    public StoreBatchProcResultDTO checkResult(int merchantId, long storeId, long batchId) throws TException {
        StoreBatchProc proc = this.storeBatchProcService.getStoreBatchProc(merchantId, storeId, batchId);
        StoreBatchProcResultDTO dto = new StoreBatchProcResultDTO();
        BeanUtil.copy(proc, dto);
        return dto;
    }
}
