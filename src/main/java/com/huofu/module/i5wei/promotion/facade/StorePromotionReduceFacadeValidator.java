package com.huofu.module.i5wei.promotion.facade;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.promotion.StorePromotionReduceParam;
import huofucore.facade.i5wei.promotion.StorePromotionReduceQuotaParam;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import org.springframework.stereotype.Component;

/**
 * 验证信息
 * Created by akwei on 11/9/16.
 */
@Component
public class StorePromotionReduceFacadeValidator extends BasePromotionValidator {

    void validate4Save(StorePromotionReduceParam param) throws T5weiException {
        String prefix = "merchantId[" + param.getMerchantId() + "] storeId[" + param.getStoreId() + "]";
        if (DataUtil.isEmpty(param.getTitle())) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " title[" + param.getTitle() + "] must be not empty");
        }
        if (param.getTitle().length() > 20) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " title[" + param.getTitle() + "] length must <=20");
        }
        long todayBegin = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        if (param.getPromotionReduceId() <= 0) {
            if (param.getBeginTime() < todayBegin) {
                throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " beginTime[" + param.getBeginTime() + "] must >= today");
            }
        }
        if (!param.isUnlimit() && param.getEndTime() < todayBegin) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " endTime[" + param.getEndTime() + "] must >= today");
        }

        if (param.getTakeModesSize() == 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " takeMode must be not empty");
        }
        this.validateTakeMode(param.getTakeModes(), prefix);
        if (param.getPeriodParams() != null) {
            this.validatePeriod(param.getPeriodParams(), prefix);
        }
        if (param.getQuotaParamsSize() == 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " reduce[" + param.getPromotionReduceId() + "] must be not empty");
        }
        for (StorePromotionReduceQuotaParam quotaParam : param.getQuotaParams()) {
            if (quotaParam.getReducePrice() > quotaParam.getQuotaPrice()) {
                throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " reduce[" + param.getPromotionReduceId() + "] must not null");
            }
            if (quotaParam.getReducePrice() < 0 || quotaParam.getQuotaPrice() < 0) {
                throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), prefix + " reduce[" + param.getPromotionReduceId() + "] reducePrice or quotaPrice must >= 0");
            }
        }
    }
}
