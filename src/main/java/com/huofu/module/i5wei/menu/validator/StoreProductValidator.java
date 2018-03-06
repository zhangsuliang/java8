package com.huofu.module.i5wei.menu.validator;

import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreProductParam;
import huofuhelper.util.DataUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 产品验证
 * Created by wxy on 16/12/29.
 */
@Service
public class StoreProductValidator {

    @Resource
    private StoreMealPortDAO storeMealPortDAO;

    public void validateSaveStoreProduct(StoreProductParam param) throws T5weiException {
        if (param.getMerchantId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + param.getMerchantId() + "] invalid");
        }
        if (param.getStoreId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + param.getStoreId() + "] invalid");
        }
        if (param.getProductId() == 0 && DataUtil.isEmpty(param.getName())) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "name[" + param.getName() + "] invalid");
        }
        if (DataUtil.isNotEmpty(param.getName()) && param.getName().length() > 50){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "name[" + param.getName() + "] invalid");
        }
        if (DataUtil.isNotEmpty(param.getUnit()) && param.getUnit().length() > 50) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "unit[" + param.getUnit() + "] invalid");
        }
        this.validatePort(param);
    }

    private void validatePort(StoreProductParam param) throws T5weiException {
        if (param.getPortId() == 0) {
            return;
        }
        this.storeMealPortDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getPortId());
    }
}
