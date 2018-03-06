package com.huofu.module.i5wei.delivery.facade;

import huofucore.facade.i5wei.delivery.StoreDeliveryBuildingParam;
import huofucore.facade.i5wei.delivery.StoreDeliverySettingDTO;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofuhelper.util.ValidateUtil;
import org.springframework.stereotype.Component;

/**
 * Created by akwei on 5/8/15.
 */
@Component
public class DeliveryValidator {

    public void validateDeliveryBuilding(StoreDeliveryBuildingParam param) throws T5weiException {
        if (!ValidateUtil.testLength(param.getName(), 1, 100, false)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_BUILDING_NAME_INVALID.getValue(), "building name[" + param.getName() + "] invalid");
        }
        if (!ValidateUtil.testLength(param.getAddress(), 1, 100, false)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_BUILDING_ADDRESS_INVALID.getValue(), "building address[" + param.getAddress() + "] invalid");
        }
    }

    public void validateDeliverySetting(StoreDeliverySettingDTO settingDTO) throws T5weiException {
        if(settingDTO.getMerchantId() <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "argument invalid");
        }
        if(settingDTO.getStoreId() <= 0){
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "argument invalid");
        }
        if (settingDTO.getAheadTime() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_SETTING_AHEAD_TIME_ERROR.getValue(), "ahead time lg zero");
        }
        if (settingDTO.getDeliveryFee() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_SETTING_DELIVERY_FEE_ERROR.getValue(), "delivery fee lg zero");
        }
        if (settingDTO.getMinOrderDeliveryAmount() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_MIN_ORDER_DELIVERY_AMOUNT_ERROR.getValue(), "min order delivery amount lg zero");
        }
        if (settingDTO.getMinOrderFreeDeliveryAmount() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_MIN_ORDER_FREE_DELIVERY_AMOUNT_ERROR.getValue(), "min order free delivery amount lg zero");
        }
        if (settingDTO.getManualNotifyHeadTime() < 0){
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_MANUAL_NOTIFY_HEAD_TIME_ERROR.getValue(), "manual notify head time lg zero");
        }
        if (settingDTO.getDeliveryScope() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_DELIVERY_SCOPE_ERROR.getValue(), "delivery scope lg zero");
        }
        if (settingDTO.getAutoPrepareMealPeriod() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_DELIVERY_AUTO_PREPARE_MEAL_PERIOD_ERROR.getValue(), "auto prepare meal period lg zero");
        }









    }
}
