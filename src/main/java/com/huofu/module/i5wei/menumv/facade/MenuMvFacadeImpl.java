package com.huofu.module.i5wei.menumv.facade;

import com.huofu.module.i5wei.batchproc.entity.StoreBatchProc;
import com.huofu.module.i5wei.batchproc.service.StoreBatchProcService;
import com.huofu.module.i5wei.menumv.service.MenuMvService;
import huofucore.facade.i5wei.menumv.CopyMenuParam;
import huofucore.facade.i5wei.menumv.MenuMvFacade;
import huofuhelper.util.MapObject;
import huofuhelper.util.json.JsonUtil;
import huofuhelper.util.thread.WengerExecutorService;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by akwei on 3/9/16.
 */
@SuppressWarnings("unchecked")
@Component
@ThriftServlet(name = "menuMvFacadeServlet", serviceClass = MenuMvFacade.class)
public class MenuMvFacadeImpl implements MenuMvFacade.Iface {

    private static Logger logger = Logger.getLogger(MenuMvFacadeImpl.class);

    @Autowired
    private MenuMvService menuMvService;

    @Autowired
    private WengerExecutorService wengerExecutorService;

    @Autowired
    private StoreBatchProcService storeBatchProcService;

    @Override
    public void mvToStore(int merchantId, long srcStoreId, long targetStoreId) throws TException {
    }

    @Override
    public void copyTvMenu2Store(int merchantId, long storeId, long targetStoreId) throws TException {
        this.menuMvService.copyTvMenu2Store(merchantId, storeId, targetStoreId);
    }

    @Override
    public long createCopyMenu2StoreBatchProc(CopyMenuParam param) throws TException {
        StoreBatchProc proc = this.menuMvService.createCopyStoreBatchProc(param);
        return proc.getBatchId();
    }

    @Override
    public void copyMenu2StoreBatch(int merchantId, long storeId, long batchId) throws TException {
        StoreBatchProc storeBatchProc = this.storeBatchProcService.getStoreBatchProc(merchantId, storeId, batchId);
        Map<String, Object> dataMap = JsonUtil.parse(storeBatchProc.getData(), Map.class);
        MapObject mo = new MapObject(dataMap);
        long srcStoreId = mo.getLong(StoreBatchProc.FIELD_SRC_STORE_ID, 0);
        long targetStoreId = mo.getLong(StoreBatchProc.FIELD_TARGET_STORE_ID, 0);
        Map<String, Object> map = this.menuMvService.getData4MoveToStore(merchantId, srcStoreId);
        this.wengerExecutorService.getExecutorService().submit(() -> {
            try {
                this.menuMvService.moveToStore(merchantId, targetStoreId, map, storeBatchProc);
            } catch (Exception e) {
                logger.error("copy error.", e);
                this.menuMvService.copyMenu2StoreFail(storeBatchProc);
            }
        });
    }
}
