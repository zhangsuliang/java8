package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.dao.*;
import com.huofu.module.i5wei.menu.entity.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by akwei on 3/21/15.
 */
@Component
public class MenuServiceUtil {

    @Resource
    private StoreChargeSubitemDAO storeChargeSubitemDAO;

    @Resource
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Resource
    private StoreTimeBucketDAO storeTimeBucketDAO;

    @Resource
    private StoreChargeItemPriceDAO storeChargeItemPriceDAO;

    @Resource
    private StoreProductDAO storeProductDAO;

    public void buildSubitems(int merchantId, long storeId, List<StoreChargeItem> storeChargeItems, List<Long> chargeItemIds, boolean loadProduct, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StoreChargeSubitem>> listMap = this.storeChargeSubitemDAO.getMapForChargeItemIds(merchantId, storeId, chargeItemIds, enableSlave, enableCache);
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            storeChargeItem.setStoreChargeSubitems(listMap.get(storeChargeItem.getChargeItemId()));
        }
        if (loadProduct) {
            this.buildProductForChargeSubitemMap(merchantId, storeId, listMap, enableSlave, enableCache);
        }
    }

    public Map<Long, StoreProduct> getProductMap(List<StoreChargeItem> storeChargeItems) {
        Map<Long, StoreProduct> productMap = Maps.newHashMap();
        for (StoreChargeItem item : storeChargeItems) {
            if (item.getStoreChargeSubitems() == null) {
                continue;
            }
            for (StoreChargeSubitem subitem : item.getStoreChargeSubitems()) {
                if (productMap.containsKey(subitem.getProductId())) {
                    continue;
                }
                if (subitem.getStoreProduct() == null) {
                    continue;
                }
                productMap.put(subitem.getProductId(), subitem.getStoreProduct());
            }
        }
        return productMap;
    }

    public Map<Long, StoreChargeItem> toChargeItemMap(List<StoreChargeItem> storeChargeItems) {
        Map<Long, StoreChargeItem> map = Maps.newHashMap();
        for (StoreChargeItem item : storeChargeItems) {
            map.put(item.getChargeItemId(), item);
        }
        return map;
    }

    public void buildWeek(int merchantId, long storeId, long time, List<StoreChargeItem> storeChargeItems, List<Long> chargeItemIds, boolean loadTimeBucket) {
        boolean enableSlave = true;
        List<StoreChargeItemWeek> storeChargeItemWeeks = this.storeChargeItemWeekDAO.getListInChargeItemIds(merchantId, storeId, time, chargeItemIds);
        Map<Long, List<StoreChargeItemWeek>> listMap = Maps.newHashMap();
        for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
            List<StoreChargeItemWeek> list = listMap.get(storeChargeItemWeek.getChargeItemId());
            if (list == null) {
                list = Lists.newArrayList();
                listMap.put(storeChargeItemWeek.getChargeItemId(), list);
            }
            list.add(storeChargeItemWeek);
        }
        if (loadTimeBucket) {
            List<Long> timeBucketIds = Lists.newArrayList();
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
                if (timeBucketIds.contains(storeChargeItemWeek.getTimeBucketId())) {
                    continue;
                }
                timeBucketIds.add(storeChargeItemWeek.getTimeBucketId());
            }
            Map<Long, StoreTimeBucket> storeTimeBucketMap = this.storeTimeBucketDAO.getMapInIds(merchantId, storeId, timeBucketIds, enableSlave);
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
                storeChargeItemWeek.setStoreTimeBucket(storeTimeBucketMap.get(storeChargeItemWeek.getTimeBucketId()));
            }
        }

        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            storeChargeItem.parseStoreChargeItemWeeks(listMap.get(storeChargeItem.getChargeItemId()));
        }
    }

    /**
     * build当前有效和未来的价格数据
     *
     * @param storeChargeItems
     * @param merchantId
     * @param storeId
     * @param chargeItemIds
     * @param time
     */
    public void buildPriceForCurAndNext(List<StoreChargeItem> storeChargeItems, int merchantId, long storeId, List<Long> chargeItemIds, long time, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StoreChargeItemPrice>> map = this.storeChargeItemPriceDAO.getMapGroupByChargeItemIdsForCurAndNext(merchantId, storeId, chargeItemIds, time, enableSlave, enableCache);
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            storeChargeItem.parseStoreChargeItemPrices(map.get(storeChargeItem.getChargeItemId()), time);
        }
    }

    /**
     * build当前有效和未来的价格数据
     *
     * @param storeChargeItems
     * @param merchantId
     * @param storeId
     * @param chargeItemIds
     * @param time
     */
    public void buildPriceForDate(List<StoreChargeItem> storeChargeItems, int merchantId, long storeId, List<Long> chargeItemIds, long time, boolean enableSlave, boolean enableCache) {
        Map<Long, List<StoreChargeItemPrice>> map = this.storeChargeItemPriceDAO.getMapGroupByChargeItemIdsForCur(merchantId, storeId, chargeItemIds, time, enableSlave, enableCache);
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            storeChargeItem.parseStoreChargeItemPrices(map.get(storeChargeItem.getChargeItemId()), time);
        }
    }

    public void buildItemPrices(StoreChargeItem storeChargeItem, long now) {
        List<StoreChargeItemPrice> storeChargeItemPrices = this.storeChargeItemPriceDAO.getList4Valid(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(), now);
        storeChargeItem.parseStoreChargeItemPrices(storeChargeItemPrices, now);

    }

    public void buildItemWeeks(StoreChargeItem storeChargeItem, boolean loadTimeBucket, long time, boolean enableSlave, boolean enableCache) {
        List<StoreChargeItemWeek> storeChargeItemWeeks = this.storeChargeItemWeekDAO.getListByChargeItemId(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(), time);
        storeChargeItem.parseStoreChargeItemWeeks(storeChargeItemWeeks);
        if (loadTimeBucket) {
            List<Long> timeBucketIds = Lists.newArrayList();
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
                if (timeBucketIds.contains(storeChargeItemWeek.getTimeBucketId())) {
                    continue;
                }
                timeBucketIds.add(storeChargeItemWeek.getTimeBucketId());
            }
            Map<Long, StoreTimeBucket> storeTimeBucketMap = this.storeTimeBucketDAO.getMapInIds(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), timeBucketIds, enableSlave, enableCache);
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
                storeChargeItemWeek.setStoreTimeBucket(storeTimeBucketMap.get(storeChargeItemWeek.getTimeBucketId()));
            }
        }
        storeChargeItem.parseStoreChargeItemWeeks(storeChargeItemWeeks);
    }

    public void buildSubitemsForItem(StoreChargeItem storeChargeItem, boolean loadProduct, boolean enableSlave, boolean enableCache) {
        List<StoreChargeSubitem> storeChargeSubitems = this.storeChargeSubitemDAO.getListByChargeItemId(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(), enableSlave, enableCache);
        storeChargeItem.setStoreChargeSubitems(storeChargeSubitems);
        if (loadProduct) {
            List<Long> productIds = Lists.newArrayList();
            for (StoreChargeSubitem storeChargeSubitem : storeChargeSubitems) {
                if (productIds.contains(storeChargeSubitem.getProductId())) {
                    continue;
                }
                productIds.add(storeChargeSubitem.getProductId());
            }
            Map<Long, StoreProduct> productMap = this.storeProductDAO.getMapInIds(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), productIds, enableSlave, enableCache);
            for (StoreChargeSubitem storeChargeSubitem : storeChargeSubitems) {
                storeChargeSubitem.setStoreProduct(productMap.get(storeChargeSubitem.getProductId()));
            }
        }
    }

    public void buildProductForChargeSubitemMap(int merchantId, long storeId, Map<Long, List<StoreChargeSubitem>> subitemMap, boolean enableSlave, boolean enableCache) {
        List<Long> productIds = Lists.newArrayList();
        Collection<List<StoreChargeSubitem>> storeChargeSubitems = subitemMap.values();
        for (List<StoreChargeSubitem> list : storeChargeSubitems) {
            for (StoreChargeSubitem storeChargeSubitem : list) {
                productIds.add(storeChargeSubitem.getProductId());
            }
        }
        Map<Long, StoreProduct> productMap = this.storeProductDAO.getMapInIds(merchantId, storeId, productIds, enableSlave, enableCache);
        for (List<StoreChargeSubitem> list : storeChargeSubitems) {
            for (StoreChargeSubitem storeChargeSubitem : list) {
                storeChargeSubitem.setStoreProduct(productMap.get(storeChargeSubitem.getProductId()));
            }
        }
    }

    public void checkSubitem(List<StoreChargeSubitem> storeChargeSubitems) {
        Map<Long, StoreChargeSubitem> productIdMap = Maps.newLinkedHashMap();
        for (StoreChargeSubitem obj : storeChargeSubitems) {
            productIdMap.put(obj.getProductId(), obj);
        }
        storeChargeSubitems.clear();
        storeChargeSubitems.addAll(productIdMap.values());
        if (storeChargeSubitems.size() > 0) {
            storeChargeSubitems.get(0).setMainFlag(true);
        }
    }

    public void buildTimeBucketForProductWeek(int merchantId, long storeId, List<StoreProductWeek> storeProductWeeks, boolean enableSlave, boolean enableCache) {
        List<Long> timeBucketIds = Lists.newArrayList();
        for (StoreProductWeek storeProductWeek : storeProductWeeks) {
            if (timeBucketIds.contains(storeProductWeek.getTimeBucketId())) {
                continue;
            }
            timeBucketIds.add(storeProductWeek.getTimeBucketId());
        }
        Map<Long, StoreTimeBucket> storeTimeBucketMap = this.storeTimeBucketDAO.getMapInIds(merchantId, storeId, timeBucketIds, enableSlave, enableCache);
        for (StoreProductWeek storeProductWeek : storeProductWeeks) {
            storeProductWeek.setStoreTimeBucket(storeTimeBucketMap.get(storeProductWeek.getTimeBucketId()));
        }
    }
}
