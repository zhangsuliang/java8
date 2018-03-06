package com.huofu.module.i5wei.delivery.facade;

import com.huofu.module.i5wei.delivery.dao.StoreDeliveryBuildingDAO;
import huofucore.facade.i5wei.delivery.UserDeliveryAddressParam;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.ValidateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by akwei on 5/8/15.
 */
@Component
public class UserDeliveryValidator {

    @Autowired
    private StoreDeliveryBuildingDAO storeDeliveryBuildingDAO;


    public void validateUserAddress(UserDeliveryAddressParam param) throws T5weiException {
        if (!ValidateUtil.testLength(param.getContactName(), 1, 100, false)) {
            throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_CONTACT_NAME.getValue(), "contact name[" + param.getContactName() + "] invalid");
        }
        if (!ValidateUtil.testLength(param.getContactPhone(), 1, 100, false)) {
            throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_CONTACT_NAME.getValue(), "contact phone[" + param.getContactPhone() + "] invalid");
        }
        if (!ValidateUtil.testLength(param.getAddress(), 1, 100, false)) {
            throw new T5weiException(T5weiErrorCodeType.USER_DELIVERY_CONTACT_NAME.getValue(), "user address[" + param.getAddress() + "] invalid");
        }
        if (param.getBuildingId() > 0) {
            this.storeDeliveryBuildingDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getBuildingId());
        }
    }
}
