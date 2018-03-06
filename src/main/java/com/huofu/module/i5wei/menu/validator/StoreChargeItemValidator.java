package com.huofu.module.i5wei.menu.validator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreProductDAO;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.ChargeItemModeEnum;
import huofucore.facade.i5wei.menu.SaveStoreChargeItemParam;
import huofucore.facade.i5wei.menu.SaveStoreChargeSubitemParam;
import huofucore.facade.i5wei.menu.StoreChargeItemWeekParam;
import huofuhelper.module.base.WeekDayEnum;
import huofuhelper.util.DataUtil;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by akwei on 3/18/15.
 */
@Component
public class StoreChargeItemValidator {

    @Autowired
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private StoreProductDAO storeProductDAO;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    /**
     * 收费项目输入参数进行校验
     *
     * @param param 输入参数
     * @throws T5weiException 校验异常
     */
    public void validateForSave(SaveStoreChargeItemParam param) throws T5weiException {
        if (param.getChargeItemId() > 0) {
            this.storeChargeItemDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), false, false);
        }
        long now = System.currentTimeMillis();
        if (param.getMerchantId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "merchantId[" + param.getMerchantId() + "] invalid");
        }
        if (param.getStoreId() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "storeId[" + param.getStoreId() + "] invalid");
        }
        if (DataUtil.isEmpty(param.getName())) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_NAME_INVALID.getValue(), "name invalid");
        }
        if (param.getName().length() > 50){
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_NAME_INVALID.getValue(), "name[" + param.getName() + "] invalid");
        }
        if (DataUtil.isNotEmpty(param.getTips()) && param.getTips().length() > 50) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "name invalid");
        }
        if (param.getSubitemParamsSize() == 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_SUBITEM_MUST_BE_NOT_EMPTY.getValue(), "subitem must be not empty");
        }
//        if (param.getOfflineNotifyTime() > 0 && param.getOfflineNotifyTime() < System.currentTimeMillis()) {
//            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_OFFLINE_NOTIFY_TIME_INVALID.getValue(), "offlineNotifyTime[" + param.getOfflineNotifyTime() + "] invalid");
//        }
        if (param.getNextPriceBeginTime() > 0 && param.getNextPriceBeginTime() <= now || param.getNextPriceBeginTime() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_PRICE_EXPIRYDATE_INVALID.getValue(), "nextPriceBeginTime[" + param.getNextPriceBeginTime() + "] must > now [" + now + "]");
        }
        if (ChargeItemModeEnum.findByValue(param.getItemMode()) == null) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_MODE_INVALID.getValue(), "merchantId[" + param.getMerchantId() + "] storeId[" + param.getStoreId() + "] itemMode[" + param.getItemMode() + "] invalid");
        }
        int mainFlagCount = 0;
        if (param.getSubitemParamsSize() <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_SUBITEM_INVALID.getValue(), "chargeItem[" + param.getChargeItemId() + "] has empty subitems");
        }
        for (SaveStoreChargeSubitemParam subitemParam : param.getSubitemParams()) {
            if (subitemParam.getProductId() <= 0) {
                throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INVALID.getValue(), "productId[" + subitemParam.getProductId() + "] invalid");
            }
            if (subitemParam.getAmount() <= 0) {
                throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_SUBITEM_AMOUNT_INVALID.getValue(), "subitem amount [" + subitemParam.getAmount() + "] invalid");
            }
            if (subitemParam.isMainFlag()) {
                mainFlagCount = mainFlagCount + 1;
            }
        }
        if (mainFlagCount > 1) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_SUBITEM_ONLY_HAS_ONE_MAIN.getValue(), "subitem must only has one main product [" + mainFlagCount + "]");
        }
        if (param.getCurPrice() < 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_PRICE_INVALID.getValue(), "curPrice [" + param.getCurPrice() + "] invalid");
        }
        if (param.getNextPrice() >= 0) {//时间必须大于今天1天
            MutableDateTime mdt = new MutableDateTime(now);
            mdt.addDays(1);
            mdt.setHourOfDay(0);
            mdt.setMinuteOfHour(0);
            mdt.setSecondOfMinute(0);
            mdt.setMillisOfSecond(0);
            if (param.getNextPriceBeginTime() < mdt.getMillis()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_PRICE_EXPIRYDATE_INVALID.getValue(), "nextBeginTime[" + param.getNextPriceBeginTime() + "] invalid");
            }
        }
        if (param.isWeightEnabled()) {
            if (param.getWeightUnit() <= 0) {
                throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_WEIGHT_UNIT_NULL.getValue(), " charge item weight unit null");
            }
            if (param.isEnableUserOrder()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_WEIGHT_DISABLE_USER_ORDER.getValue(), " charge item weight disable user order");
            }
            if (param.getSubitemParams().size() != 1) {
                throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_WEIGHT_NOT_ONE_PRODUCT.getValue(), " charge item weight not one product");
            }
            if (param.getSubitemParams().get(0).getAmount() != 1) {//
                throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_WEIGHT_PRODUCT_NUM_UNEQ_ONE.getValue(), " charge item weight product num uneq one");
            }
        }
        this.checkWeekDay(param);
        this.checkProduct(param);
        this.checkTimeBucket(param);
        this.checkPort(param);
    }

    private void checkProduct(SaveStoreChargeItemParam param) throws T5weiException {
        List<Long> productIds = Lists.newArrayList();
        for (SaveStoreChargeSubitemParam subitemParam : param.getSubitemParams()) {
            productIds.add(subitemParam.getProductId());
        }
        Map<Long, StoreProduct> storeProductMap = this.storeProductDAO.getMapInIds(param.getMerchantId(), param.getStoreId(), productIds, false, false);
        for (SaveStoreChargeSubitemParam subitemParam : param.getSubitemParams()) {
            StoreProduct storeProduct = storeProductMap.get(subitemParam.getProductId());
            if (storeProduct == null || storeProduct.isDeleted()) {
                throw new T5weiException(T5weiErrorCodeType.STORE_PRODUCT_INVALID.getValue(), "productId[" + subitemParam.getProductId() + "] invalid or deleted");
            }
        }
    }

    private void checkWeekDay(SaveStoreChargeItemParam param) throws T5weiException {
        if (param.getCurItemWeekParams() != null) {
            for (StoreChargeItemWeekParam storeChargeItemWeekParam : param.getCurItemWeekParams()) {
                if (WeekDayEnum.findByValue(storeChargeItemWeekParam.getWeekDay()) == null) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_WEEK_INVALID.getValue(), "item week[" + storeChargeItemWeekParam.getWeekDay() + "] invalid");
                }
                if (storeChargeItemWeekParam.getTimeBucketId() <= 0) {
                    throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_INVALID.getValue(), "timeBucketId[" + storeChargeItemWeekParam.getTimeBucketId() + "] invalid");
                }
            }
        }
        if (param.getItemMode() == ChargeItemModeEnum.MODE_SUPER.getValue() && param.getNextWeekItemWeekParams() != null) {
            for (StoreChargeItemWeekParam storeChargeItemWeekParam : param.getNextWeekItemWeekParams()) {
                if (WeekDayEnum.findByValue(storeChargeItemWeekParam.getWeekDay()) == null) {
                    throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_WEEK_INVALID.getValue(), "item week[" + storeChargeItemWeekParam.getWeekDay() + "] invalid");
                }
                if (storeChargeItemWeekParam.getTimeBucketId() <= 0) {
                    throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_INVALID.getValue(), "timeBucketId[" + storeChargeItemWeekParam.getTimeBucketId() + "] invalid");
                }
            }
        }
    }

    private void checkTimeBucket(SaveStoreChargeItemParam param) {
        boolean enableSlave = false;
        List<Long> timeBucketIds = Lists.newArrayList();
        if (param.getCurItemWeekParams() != null) {
            for (StoreChargeItemWeekParam storeChargeItemWeekParam : param.getCurItemWeekParams()) {
                timeBucketIds.add(storeChargeItemWeekParam.getTimeBucketId());
            }
        }
        if (param.getNextWeekItemWeekParams() != null) {
            for (StoreChargeItemWeekParam storeChargeItemWeekParam : param.getNextWeekItemWeekParams()) {
                timeBucketIds.add(storeChargeItemWeekParam.getTimeBucketId());
            }
        }
        Map<Long, StoreTimeBucket> storeTimeBucketMap = this.storeTimeBucketDAO.getMapInIds(param.getMerchantId(), param.getStoreId(), timeBucketIds, enableSlave);
        Set<Long> deletedTimeBucketIds = Sets.newHashSet();
        if (param.getCurItemWeekParams() != null) {
            for (StoreChargeItemWeekParam storeChargeItemWeekParam : param.getCurItemWeekParams()) {
                StoreTimeBucket storeTimeBucket = storeTimeBucketMap.get(storeChargeItemWeekParam.getTimeBucketId());
                if (storeTimeBucket == null || storeTimeBucket.isDeleted()) {
                    deletedTimeBucketIds.add(storeChargeItemWeekParam.getTimeBucketId());
                }
            }
        }

        //去除无效的timeBucketId
        if (param.getCurItemWeekParams() != null) {
            Iterator<StoreChargeItemWeekParam> it = param.getCurItemWeekParamsIterator();
            while (it.hasNext()) {
                StoreChargeItemWeekParam storeChargeItemWeekParam = it.next();
                if (deletedTimeBucketIds.contains(storeChargeItemWeekParam.getTimeBucketId())) {
                    it.remove();
                }
            }
        }
        if (param.getNextWeekItemWeekParams() != null) {
            Iterator<StoreChargeItemWeekParam> it = param.getNextWeekItemWeekParamsIterator();
            while (it.hasNext()) {
                StoreChargeItemWeekParam storeChargeItemWeekParam = it.next();
                if (deletedTimeBucketIds.contains(storeChargeItemWeekParam.getTimeBucketId())) {
                    it.remove();
                }
            }
        }
    }

    private void checkPort(SaveStoreChargeItemParam param) throws T5weiException {
        if (param.getPortId() == 0) {
            return;
        }
        this.storeMealPortDAO.loadById(param.getMerchantId(), param.getStoreId(), param.getPortId());
    }
}
