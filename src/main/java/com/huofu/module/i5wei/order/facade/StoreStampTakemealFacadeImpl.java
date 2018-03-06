package com.huofu.module.i5wei.order.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.order.entity.StoreStampTakemeal;
import com.huofu.module.i5wei.order.service.StoreStampTakemealService;
import huofucore.facade.i5wei.order.*;
import huofucore.facade.merchant.appcopy.AppcopyRelAutoPrinterCashierFacade;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by chengq on 16/4/11.
 * <p>
 * 需要打印的自动取餐单
 */
@ThriftServlet(name = "storeStampTakemealFacadeServlet", serviceClass = StoreStampTakemealFacade.class)
@Component
public class StoreStampTakemealFacadeImpl implements StoreStampTakemealFacade.Iface{

    @Resource
    private StoreStampTakemealService storeStampTakemealService;

    @ThriftClient
    private AppcopyRelAutoPrinterCashierFacade.Iface appcopyRelAutoPrinterCashierFacadeIface;

    @Override
    public void save(StoreStampTakemealParam param) throws TException {
        StoreStampTakemeal storeStampTakemeal = new StoreStampTakemeal();
        BeanUtil.copy(param,storeStampTakemeal);
        storeStampTakemealService.create(param.getMerchantId(),param.getStoreId(),storeStampTakemeal);
    }

    @Override
    public List<StoreStampTakemealDTO> getStoreStampTakemeals(int merchantId, long storeId, int count, List<StampTakemealReceiptParam> params) throws TException {
        //打印订单回执处理
        if(params != null && params.size() > 0){
            for (StampTakemealReceiptParam param : params) {
                StoreStampTakemeal storeStampTakemeal = storeStampTakemealService.getStoreStampTakemeal(merchantId, storeId, param.getStoreStampTakemealId());
                if(storeStampTakemeal != null && ( storeStampTakemeal.getStatus() == StatusEnum.PROCESS.getValue() ||
                        storeStampTakemeal.getStatus() == StatusEnum.FAIL.getValue() )){
                    if(param.getPrinted() == 1){
                        storeStampTakemeal.setUpdateTime(System.currentTimeMillis());
                        storeStampTakemeal.setStatus(StatusEnum.SUCCESS.getValue());
                        storeStampTakemealService.update(merchantId,storeId,storeStampTakemeal);
                    }else{
                        if(param.getPrinterStatus() != PrinterStatusEunm.NORMAL.getValue()){
                            //解除绑定
                            appcopyRelAutoPrinterCashierFacadeIface.relieveBinding(storeId,0);
                        }
                        storeStampTakemeal.setUpdateTime(System.currentTimeMillis());
                        storeStampTakemeal.setStatus(StatusEnum.FAIL.getValue());
                        storeStampTakemealService.update(merchantId,storeId,storeStampTakemeal);
                    }
                }
            }
        }else {
            //修复正在打印出餐单
            storeStampTakemealService.updateStoreStampTakemealStatus(merchantId, storeId);
        }
        //订单查询
        List<StoreStampTakemeal> storeStampTakemeals = storeStampTakemealService.getStoreStampTakemeals(merchantId, storeId, count);
        List<StoreStampTakemealDTO> list = Lists.newArrayList();
        for (StoreStampTakemeal storeStampTakemeal : storeStampTakemeals) {
            StoreStampTakemealDTO storeStampTakemealDTO = new StoreStampTakemealDTO();
            BeanUtil.copy(storeStampTakemeal,storeStampTakemealDTO);
            list.add(storeStampTakemealDTO);
        }
        return list;
    }

    @Override
    public boolean isStoreStampTakemeal(int merchantId, long storeId) throws TException {
        return storeStampTakemealService.isStoreStampTakemeal(merchantId,storeId);
    }
}
