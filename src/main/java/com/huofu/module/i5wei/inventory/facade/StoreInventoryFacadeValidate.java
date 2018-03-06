package com.huofu.module.i5wei.inventory.facade;

import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.inventory.StoreInventoryDateAmountUpdateParam;
import huofucore.facade.i5wei.inventory.StoreInventoryDateNothingnessUpdateParam;
import huofucore.facade.i5wei.inventory.StoreInventoryWeekUpdateParam;

import org.springframework.stereotype.Component;

@Component
public class StoreInventoryFacadeValidate {
	
	public void validateStoreInventoryWeekUpdateParam(StoreInventoryWeekUpdateParam storeInventoryWeekUpdateParam) throws T5weiException{
		if (storeInventoryWeekUpdateParam.getMerchantId() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId can not null");
		}
		if (storeInventoryWeekUpdateParam.getStoreId() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId can not null");
		}
		if (storeInventoryWeekUpdateParam.getProductId() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "productId can not null");
		}
		if (storeInventoryWeekUpdateParam.getStoreInventoryWeekItemParams() == null || storeInventoryWeekUpdateParam.getStoreInventoryWeekItemParams().isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_AMOUNT_OUT_OFF_RANGE.getValue(), "StoreInventoryWeekItemParams can not null");
		}
	}
	
	public void validateStoreInventoryDateAmountUpdateParam(StoreInventoryDateAmountUpdateParam storeInventoryDateAmountUpdateParam) throws T5weiException{
		if (storeInventoryDateAmountUpdateParam.getMerchantId() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId can not null");
		}
		if (storeInventoryDateAmountUpdateParam.getStoreId() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId can not null");
		}
		if (storeInventoryDateAmountUpdateParam.getRepastDate() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "repastDate can not null");
		}
		if (storeInventoryDateAmountUpdateParam.getAmount() < 0 || storeInventoryDateAmountUpdateParam.getAmount() > 99999999) {
			throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INV_AMOUNT_OUT_OFF_RANGE.getValue(), "repastDate can not null");
		}
	}
	
	public void ValidateStoreInventoryDateNothingnessUpdateParam(StoreInventoryDateNothingnessUpdateParam storeInventoryDateNothingnessUpdateParam) throws T5weiException{
		if (storeInventoryDateNothingnessUpdateParam.getMerchantId() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId can not null");
		}
		if (storeInventoryDateNothingnessUpdateParam.getStoreId() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId can not null");
		}
		if (storeInventoryDateNothingnessUpdateParam.getRepastDate() <= 0) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "repastDate can not null");
		}
		if (storeInventoryDateNothingnessUpdateParam.getProductIds()==null||storeInventoryDateNothingnessUpdateParam.getProductIds().isEmpty()) {
			throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "productIds can not be empty");
		}
	}

}
