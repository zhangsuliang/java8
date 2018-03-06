package com.huofu.module.i5wei.promotion.facade;

import com.google.common.collect.Sets;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.promotion.StorePromotionPeriodParam;

import java.util.List;
import java.util.Set;

class BasePromotionValidator {

    void validateTakeMode(List<Integer> takeModes, String prefix) throws T5weiException {
        for (Integer takeMode : takeModes) {
            if (StoreOrderTakeModeEnum.findByValue(takeMode) == null) {
                throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " takeMode[" + takeMode + "] must in (" + StoreOrderTakeModeEnum.DINE_IN.getValue() + "," + StoreOrderTakeModeEnum.SEND_OUT.getValue() + "," + StoreOrderTakeModeEnum.TAKE_OUT.getValue() + " )");
            }
            if (takeMode != StoreOrderTakeModeEnum.DINE_IN.getValue() &&
                    takeMode != StoreOrderTakeModeEnum.SEND_OUT.getValue() &&
                    takeMode != StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
                throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " takeMode[" + takeMode + "] must in (" + StoreOrderTakeModeEnum.DINE_IN.getValue() + "," + StoreOrderTakeModeEnum.SEND_OUT.getValue() + "," + StoreOrderTakeModeEnum.TAKE_OUT.getValue() + " )");
            }
        }
    }

    void validatePeriod(List<StorePromotionPeriodParam> periodParams, String prefix) throws T5weiException {
        for (StorePromotionPeriodParam periodParam : periodParams) {
            Set<Integer> weekDaySet = Sets.newHashSet();
            weekDaySet.addAll(periodParam.getWeekDays());
            if (weekDaySet.size() != periodParam.getWeekDaysSize()) {
                throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " timeBucketId[" + periodParam.getTimeBucketId() + "] periodduplicate");
            }
        }
    }
}
