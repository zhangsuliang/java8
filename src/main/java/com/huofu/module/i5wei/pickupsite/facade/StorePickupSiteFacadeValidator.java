package com.huofu.module.i5wei.pickupsite.facade;

import com.google.common.base.Strings;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import huofucore.facade.i5wei.exception.*;
import huofucore.facade.i5wei.pickupsite.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StorePickupSiteFacadeValidator {

	/**
	 * 参数校验并且返回组装类型
	 *
	 * @param createOrUpdatethriftParam
	 * @throws T5weiException
	 */
	public void validatePickupSiteCreatedOrUpdateParam(
			StorePickupSiteCreatedOrUpdatedParam createOrUpdatethriftParam) throws PickupSiteCutOffTimeGreaterThanPickupTimeException, TException {

		//校验商户和店铺ID
		checkMerchantIdAndStoreIdParam(createOrUpdatethriftParam.getMerchantId(), createOrUpdatethriftParam.getStoreId());

		//校验自提单名称过长
		if (!Strings.isNullOrEmpty(createOrUpdatethriftParam.getStorePickupSiteName())
		    && createOrUpdatethriftParam.getStorePickupSiteName().length() > 100) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "pickupSiteName too long");
		}

		//校验自提点地址过长
		if (!Strings.isNullOrEmpty(createOrUpdatethriftParam.getStorePickupSiteAddress())
		    && createOrUpdatethriftParam.getStorePickupSiteAddress().length() > 200) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "pickupSiteAddress too long");
		}

		//新建模式下，名称不能为空
		if (createOrUpdatethriftParam.getStorePickupSiteId() <= 0
		    && Strings.isNullOrEmpty(createOrUpdatethriftParam.getStorePickupSiteName())) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "create model: pickupSiteName not be null");
		}

		//校验店铺取餐已送餐时段
		if (CollectionUtils.isNotEmpty(createOrUpdatethriftParam.getStorePickupSiteTimeSettings())) {
			for(StorePickupSiteTimeSettingParam storePickupSiteTimeSettingParam : createOrUpdatethriftParam.getStorePickupSiteTimeSettings()) {
                if (storePickupSiteTimeSettingParam.getOrderCutOffTime() != 0 && storePickupSiteTimeSettingParam.getPickupTime() != 0 &&
						storePickupSiteTimeSettingParam.isDisabled() == false) {
                    if (storePickupSiteTimeSettingParam.getOrderCutOffTime() > storePickupSiteTimeSettingParam.getPickupTime()) {
                        throw new PickupSiteCutOffTimeGreaterThanPickupTimeException("orderCutOffTime must less than pickupTime");
                    }
                }
			}
		}
	}

	public void validatePickupSiteTimeSettingParams(Map<Long, StoreTimeBucket> storeTimeBucketMap, List<StorePickupSiteTimeSettingParam> pickupSiteTimeSettingParams)
			throws PickupSiteCutOffTimeGreaterThanTimeBucketEndTimeException, PickupSitePickupTimeLessThanTimeBucketStartTimeException, TException {
        for (StorePickupSiteTimeSettingParam pickupSiteTimeSettingParam : pickupSiteTimeSettingParams) {

            StoreTimeBucket storeTimeBucket = storeTimeBucketMap.get(pickupSiteTimeSettingParam.getTimeBucketId());
			if (pickupSiteTimeSettingParam.isDisabled() == false) {
				if (pickupSiteTimeSettingParam.getOrderCutOffTime() > 0 && pickupSiteTimeSettingParam.getOrderCutOffTime() >= storeTimeBucket.getEndTime()) {
					throw new PickupSiteCutOffTimeGreaterThanTimeBucketEndTimeException();
				}

				if (pickupSiteTimeSettingParam.getPickupTime() > 0 && pickupSiteTimeSettingParam.getPickupTime() <= storeTimeBucket.getStartTime()) {
					throw new PickupSitePickupTimeLessThanTimeBucketStartTimeException();
				}
			}
        }
    }

	/**
	 * 店铺id和商户id参数校验
	 *
	 * @param merchantId
	 * @param storeId
	 * @throws T5weiException
	 */
	public void checkMerchantIdAndStoreIdParam(int merchantId, long storeId) throws T5weiException {
		if (merchantId <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId error");
		}
		if (storeId <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId error");
		}
	}
}
