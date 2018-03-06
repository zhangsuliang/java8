package com.huofu.module.i5wei.remark.service;

import com.huofu.module.i5wei.remark.dao.StoreProductRemarkDAO;
import com.huofu.module.i5wei.remark.entity.StoreProductRemark;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by akwei on 10/14/15.
 */
@Service
public class StoreProductRemarkService {

    @Autowired
    private StoreProductRemarkDAO storeProductRemarkDAO;

    public List<StoreProductRemark> getStoreProductRemarks(int merchantId,
                                                           long storeId,
                                                           int size) {
        return this.storeProductRemarkDAO.queryList(merchantId, storeId, size);
    }
}
