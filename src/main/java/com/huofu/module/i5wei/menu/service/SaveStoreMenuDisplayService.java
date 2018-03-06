package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemWeekDAO;
import com.huofu.module.i5wei.menu.dao.StoreMenuDisplayCatDAO;
import com.huofu.module.i5wei.menu.dao.StoreMenuDisplayDAO;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreMenuDisplay;
import com.huofu.module.i5wei.menu.entity.StoreMenuDisplayCat;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.StoreMenuDisplayParam;
import huofuhelper.util.DataUtil;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by akwei on 3/17/15.
 */
@Service
public class SaveStoreMenuDisplayService {

    @Autowired
    private StoreMenuDisplayDAO storeMenuDisplayDAO;

    @Autowired
    private StoreMenuDisplayCatDAO storeMenuDisplayCatDAO;

    @Autowired
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private MenuServiceUtil menuServiceUtil;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public SaveStoreMenuDisplayResult saveMenuDisplay(int merchantId, long storeId, long timeBucketId, List<StoreMenuDisplayParam> storeMenuDisplayParams) throws T5weiException {
        for (StoreMenuDisplayParam param : storeMenuDisplayParams) {
            if (DataUtil.isNotEmpty(param.getCatName()) && param.getCatName().length() > 50) {
                throw new T5weiException(T5weiErrorCodeType
                        .ARGUMENT_INVALID.getValue(), "storeId[" + storeId + "] [" + timeBucketId + "] catName[" + param.getCatName() + "] length must <=50");
            }
        }
        //去除多余unsorted,只保留最后一个unsorted
        List<StoreMenuDisplayParam> idxList = Lists.newArrayList();
        for (StoreMenuDisplayParam storeMenuDisplayParam : storeMenuDisplayParams) {
            if (storeMenuDisplayParam.isUnsorted()) {
                idxList.add(storeMenuDisplayParam);
            }
        }
        if (idxList.size() > 1) {
            idxList.remove(idxList.size() - 1);
            for (StoreMenuDisplayParam storeMenuDisplayParam : idxList) {
                storeMenuDisplayParam.setUnsorted(false);
            }
        }
        this.storeMenuDisplayDAO.deleteByTimeBucketId(merchantId, storeId, timeBucketId);
        long now = System.currentTimeMillis();
        List<StoreMenuDisplay> storeMenuDisplays = Lists.newArrayList();
        List<StoreMenuDisplayCat> cats = this.buildStoreMenuDisplayCats(merchantId, storeId, timeBucketId, storeMenuDisplayParams, now);
        int catIdx = 0;
        for (StoreMenuDisplayParam storeMenuDisplayParam : storeMenuDisplayParams) {
            StoreMenuDisplayCat storeMenuDisplayCat = cats.get(catIdx);
            int i = 1;
            if (storeMenuDisplayParam.getChargeItemIdsSize() > 0) {
                for (Long chargeItemId : storeMenuDisplayParam.getChargeItemIds()) {
                    StoreMenuDisplay storeMenuDisplay = new StoreMenuDisplay();
                    storeMenuDisplay.setMerchantId(merchantId);
                    storeMenuDisplay.setStoreId(storeId);
                    storeMenuDisplay.setChargeItemId(chargeItemId);
                    storeMenuDisplay.setTimeBucketId(timeBucketId);
                    storeMenuDisplay.setCreateTime(now);
                    if (storeMenuDisplayCat.getDisplayCatId() != 0) {
                        storeMenuDisplay.setDisplayCatId(storeMenuDisplayCat.getDisplayCatId());
                        storeMenuDisplay.setStoreMenuDisplayCat(storeMenuDisplayCat);
                    }
                    storeMenuDisplay.setSortFlag(i);
                    storeMenuDisplays.add(storeMenuDisplay);
                    i++;
                }
            }
            catIdx++;
        }

        //方式不同分类中含有同样的收费项目id，只保留第一个，其他的被去除
        Iterator<StoreMenuDisplay> it = storeMenuDisplays.iterator();
        Map<Long, Long> chargeItemId_catIdMap = Maps.newHashMap();
        while (it.hasNext()) {
            StoreMenuDisplay storeMenuDisplay = it.next();
            Long catId = chargeItemId_catIdMap.get(storeMenuDisplay.getChargeItemId());
            if (catId == null) {
                chargeItemId_catIdMap.put(storeMenuDisplay.getChargeItemId(), storeMenuDisplay.getDisplayCatId());
            } else {
                it.remove();
            }
        }
        this.storeMenuDisplayDAO.batchCreate(storeMenuDisplays);
        SaveStoreMenuDisplayResult result = new SaveStoreMenuDisplayResult();
        result.setStoreMenuDisplays(storeMenuDisplays);
        Iterator<StoreMenuDisplayCat> storeMenuDisplayCatIterator = cats.iterator();
        while (storeMenuDisplayCatIterator.hasNext()) {
            StoreMenuDisplayCat cat = storeMenuDisplayCatIterator.next();
            if (cat.getDisplayCatId() == 0) {
                storeMenuDisplayCatIterator.remove();
            }
        }
        result.setStoreMenuDisplayCats(cats);
        return result;
    }

    public Map<Long, StoreChargeItem> getStoreChargeItemMapByTimeBucketIdForSort(int merchantId, long storeId, long timeBuceketId) {
        List<Long> chargeItemIds = this.storeChargeItemWeekDAO.getChargeItemIds4Sort(merchantId, storeId, timeBuceketId);
        Map<Long, StoreChargeItem> map = this.storeChargeItemDAO.getMapInIds(merchantId, storeId, chargeItemIds, false, false);
        this.menuServiceUtil.buildPriceForDate(new ArrayList<>(map.values()), merchantId, storeId, chargeItemIds, System.currentTimeMillis(), false, false);
        return map;
    }

    public List<StoreMenuDisplay> getMenuDisplay(int merchantId, long storeId, long timeBucketId) {
        List<StoreMenuDisplay> storeMenuDisplays = this.storeMenuDisplayDAO.getListByTimeBucketId(merchantId, storeId, timeBucketId, true, true);
        List<Long> catIds = Lists.newArrayList();
        for (StoreMenuDisplay storeMenuDisplay : storeMenuDisplays) {
            if (catIds.contains(storeMenuDisplay.getDisplayCatId())) {
                continue;
            }
            catIds.add(storeMenuDisplay.getDisplayCatId());
        }
        Map<Long, StoreMenuDisplayCat> catMap = this.storeMenuDisplayCatDAO.getMapInIds(merchantId, storeId, catIds, true, true);
        for (StoreMenuDisplay storeMenuDisplay : storeMenuDisplays) {
            storeMenuDisplay.setStoreMenuDisplayCat(catMap.get(storeMenuDisplay.getDisplayCatId()));
        }
        return storeMenuDisplays;
    }

    private List<StoreMenuDisplayCat> buildStoreMenuDisplayCats(int merchantId, long storeId, long timeBucketId, List<StoreMenuDisplayParam> storeMenuDisplayParams, long now) {
        this.storeMenuDisplayCatDAO.deleteByTimeBucketId(merchantId, storeId, timeBucketId);
        List<StoreMenuDisplayCat> storeMenuDisplayCats = Lists.newArrayList();
        List<StoreMenuDisplayCat> storeMenuDisplayCatsAll = Lists.newArrayList();
        for (StoreMenuDisplayParam storeMenuDisplayParam : storeMenuDisplayParams) {
            StoreMenuDisplayCat storeMenuDisplayCat = new StoreMenuDisplayCat();
            storeMenuDisplayCatsAll.add(storeMenuDisplayCat);
            if (storeMenuDisplayParam.isUnsorted()) {
                storeMenuDisplayCat.setDisplayCatId(0);
            } else {
                storeMenuDisplayCat.setMerchantId(merchantId);
                storeMenuDisplayCat.setStoreId(storeId);
                storeMenuDisplayCat.setName(storeMenuDisplayParam.getCatName());
                storeMenuDisplayCat.setTimeBucketId(timeBucketId);
                storeMenuDisplayCat.setCreateTime(now);
                storeMenuDisplayCats.add(storeMenuDisplayCat);
            }
        }
        if (!storeMenuDisplayCats.isEmpty()) {
            this.storeMenuDisplayCatDAO.batchCreate(storeMenuDisplayCats);
        }
        return storeMenuDisplayCatsAll;
    }

    @Transactional(rollbackFor = Exception.class)
    public void copyMenuDisplay(int merchantId, long storeId, long timeBucketId, long targetTimeBucketId) throws T5weiException, TException {
        List<StoreMenuDisplayCat> cats = this.storeMenuDisplayCatDAO.getListByTimeBucketId(merchantId, storeId, timeBucketId, false, false);
        List<StoreMenuDisplay> storeMenuDisplays = this.storeMenuDisplayDAO.getListByTimeBucketId(merchantId, storeId, timeBucketId, false, false);

        Map<Long, List<Long>> cat_itemIdsMap = Maps.newHashMap();
        for (StoreMenuDisplay storeMenuDisplay : storeMenuDisplays) {
            List<Long> list = cat_itemIdsMap.get(storeMenuDisplay.getDisplayCatId());
            if (list == null) {
                list = Lists.newArrayList();
                cat_itemIdsMap.put(storeMenuDisplay.getDisplayCatId(), list);
            }
            list.add(storeMenuDisplay.getChargeItemId());
        }

        List<StoreMenuDisplayParam> storeMenuDisplayParams = Lists.newArrayList();
        for (StoreMenuDisplayCat cat : cats) {
            StoreMenuDisplayParam storeMenuDisplayParam = new StoreMenuDisplayParam();
            storeMenuDisplayParam.setCatName(cat.getName());
            List<Long> itemIds = cat_itemIdsMap.get(cat.getDisplayCatId());
            storeMenuDisplayParam.setChargeItemIds(itemIds);
            storeMenuDisplayParams.add(storeMenuDisplayParam);
        }

        StoreMenuDisplayParam storeMenuDisplayParam = new StoreMenuDisplayParam();
        List<Long> itemIds = Lists.newArrayList();
        storeMenuDisplayParam.setChargeItemIds(itemIds);
        storeMenuDisplayParam.setUnsorted(true);
        storeMenuDisplayParams.add(storeMenuDisplayParam);

        this.saveMenuDisplay(merchantId, storeId, targetTimeBucketId, storeMenuDisplayParams);
    }
}
