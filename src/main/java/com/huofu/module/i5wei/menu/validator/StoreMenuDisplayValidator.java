package com.huofu.module.i5wei.menu.validator;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreTimeBucketDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreMenuDisplayParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by akwei on 3/27/15.
 */
@Component
public class StoreMenuDisplayValidator {

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private StoreTimeBucketDAO storeTimeBucketDAO;

    public void validateForSaveMenuDisplay(int merchantId, long storeId, long timeBucketId, List<StoreMenuDisplayParam> params) throws T5weiException {
        if (timeBucketId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.TIMEBUCKET_INVALID.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] timeBucketId[" + timeBucketId + "] invalid");
        }
        this.storeTimeBucketDAO.loadById(merchantId, storeId, timeBucketId, false, false);
        List<Long> chargeItemIds = Lists.newArrayList();
        for (StoreMenuDisplayParam storeMenuDisplayParam : params) {
            if (storeMenuDisplayParam.getChargeItemIdsSize() > 0) {
                for (Long id : storeMenuDisplayParam.getChargeItemIds()) {
                    if (!chargeItemIds.contains(id)) {
                        chargeItemIds.add(id);
                    }
                }
            }
        }
        Map<Long, StoreChargeItem> storeChargeItemMap = this.storeChargeItemDAO.getMapInIds(merchantId, storeId, chargeItemIds, false, false);
        for (StoreMenuDisplayParam storeMenuDisplayParam : params) {
            Iterator<Long> it = storeMenuDisplayParam.getChargeItemIdsIterator();
            if (it != null) {
                while (it.hasNext()) {
                    long id = it.next();
                    StoreChargeItem storeChargeItem = storeChargeItemMap.get(id);
                    if (storeChargeItem == null || storeChargeItem.isDeleted()) {
                        it.remove();
                    }
                }
            }
        }
    }
}
