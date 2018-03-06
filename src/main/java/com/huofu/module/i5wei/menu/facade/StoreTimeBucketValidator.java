package com.huofu.module.i5wei.menu.facade;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreTimeBucketParam;
import huofuhelper.util.DataUtil;
import org.springframework.stereotype.Component;

@Component
public class StoreTimeBucketValidator {

    public void checkSaveStoreTimeBucketEmpty(StoreTimeBucketParam param) throws T5weiException {
        if (DataUtil.isEmpty(param.getName())) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "timebucket storeId[" + param.getStoreId() + "] name[" + param.getName() + "] must not empty or length must <=20");
        }
    }

    public void checkSaveStoreTimeBucketParamLength(StoreTimeBucketParam param) throws T5weiException {
        if (DataUtil.isNotEmpty(param.getName()) && param.getName().length() > 20) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "timebucket storeId[" + param.getStoreId() + "] name[" + param.getName() + "] must not empty or length must <=20");
        }
        if (DataUtil.isNotEmpty(param.getTips()) && param.getTips().length() > 50) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "timebucket storeId[" + param.getStoreId() + "] tips[" + param.getTips() + "] must not empty or length must <=50");
        }
    }
}
