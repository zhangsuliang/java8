package com.huofu.module.i5wei.remark.facade;

import com.huofu.module.i5wei.remark.entity.StoreProductRemark;
import com.huofu.module.i5wei.remark.service.StoreProductRemarkService;
import huofucore.facade.i5wei.remark.StoreProductRemarkDTO;
import huofucore.facade.i5wei.remark.StoreProductRemarkFacade;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akwei on 10/14/15.
 */
@Component
@ThriftServlet(name = "storeProductRemarkFacadeServlet", serviceClass
        = StoreProductRemarkFacade.class)
public class StoreProductRemarkFacadeImpl implements StoreProductRemarkFacade
        .Iface {

    @Autowired
    private StoreProductRemarkService storeProductRemarkService;

    @Override
    public List<StoreProductRemarkDTO> getStoreProductRemarks(int merchantId, long storeId, int size) throws TException {
        List<StoreProductRemark> storeProductRemarks = this
                .storeProductRemarkService.getStoreProductRemarks(merchantId,
                        storeId, size);
        List<StoreProductRemarkDTO> dtos = new ArrayList<>();
        for (StoreProductRemark storeProductRemark : storeProductRemarks) {
            StoreProductRemarkDTO dto = new StoreProductRemarkDTO();
            BeanUtil.copy(storeProductRemark, dto);
            dtos.add(dto);
        }
        return dtos;
    }
}
