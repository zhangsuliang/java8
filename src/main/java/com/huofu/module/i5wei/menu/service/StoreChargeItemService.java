package com.huofu.module.i5wei.menu.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.menu.dao.*;
import com.huofu.module.i5wei.menu.entity.*;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.*;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import org.apache.thrift.TException;
import org.joda.time.MutableDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoreChargeItemService {

    public static final String key_allQuickTake = "allQuickTake";

    public static final String key_quickTrade = "quickTrade";

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;

    @Autowired
    private StoreChargeSubitemDAO storeChargeSubitemDAO;

    @Autowired
    private StoreChargeItemPriceDAO storeChargeItemPriceDAO;

    @Autowired
    private StoreProductDAO storeProductDAO;

    @Autowired
    private StoreChargeItemWeekDAO storeChargeItemWeekDAO;

    @Autowired
    private MenuServiceUtil menuServiceUtil;

    @Autowired
    private StoreDateTimeBucketSettingDAO storeDateTimeBucketSettingDAO;

    @Autowired
    private StoreDateBizSettingDAO storeDateBizSettingDAO;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private StoreMenuDisplayDAO storeMenuDisplayDAO;

    @Autowired
    private StoreChargeItemWeekBackDAO storeChargeItemWeekBackDAO;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreChargeItemPromotionDAO storeChargeItemPromotionDAO;


    /**
     * 获得指定日期营业时段的收费项目id
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param timeBucketId 营业时段id
     * @param time         日期时间戳
     * @return 收费项目id集合
     */
    public List<StoreChargeItem> getChargeItemForDate(int merchantId, long storeId, long timeBucketId, long time) {
        StoreDateBizSetting storeDateBizSetting = this.storeDateBizSettingDAO.getForSelectedDate(merchantId, storeId, time, true, true);
        if (StoreDateBizSetting.isPaused(storeDateBizSetting)) {
            return new ArrayList<>(0);
        }
        StoreDateTimeBucketSetting storeDateTimeBucketSetting = this.storeDateTimeBucketSettingDAO.getForDate(merchantId, storeId, time, timeBucketId, true, true);
        if (StoreDateTimeBucketSetting.isPaused(storeDateTimeBucketSetting)) {
            return new ArrayList<>(0);
        }

        int weekDay = StoreDateBizSetting.getWeekDay(storeDateBizSetting, time);
        List<Long> chargeItemIds = this.storeChargeItemWeekDAO.getChargeItemIdsForWeekDay(merchantId, storeId, timeBucketId, weekDay, time, true, true);
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, true, true);
        this.menuServiceUtil.buildSubitems(merchantId, storeId, storeChargeItems, chargeItemIds, true, true, true);
        return storeChargeItems;
    }

    public StoreChargeItem getStoreChargeItem(int merchantId, long storeId, long chargeItemId, boolean loadSubitems, boolean loadProduct, boolean loadAllAvailablesPrices, boolean loadAllItemWeeks, boolean loadTimeBucket, boolean loadMealPort, boolean loadChargeItemPromotion, long now) throws TException {

        StoreChargeItem storeChargeItem = this.storeChargeItemDAO.loadByIdForQuery(merchantId, storeId, chargeItemId, true, true);
        if (loadSubitems) {
            this.menuServiceUtil.buildSubitemsForItem(storeChargeItem, loadProduct, true, true);
        }
        if (loadAllAvailablesPrices) {
            this.menuServiceUtil.buildItemPrices(storeChargeItem, now);
        }
        if (loadAllItemWeeks) {
            this.menuServiceUtil.buildItemWeeks(storeChargeItem, loadTimeBucket, now, true, true);
        }
        if (loadMealPort) {
            StoreMealPort storeMealPort = this.storeMealPortDAO.getById
                    (merchantId, storeId, storeChargeItem.getPortId());
            storeChargeItem.setStoreMealPort(storeMealPort);
        }
        if (loadChargeItemPromotion){
			StoreChargeItemPromotion chargeItemPromotion = this.storeChargeItemPromotionDAO.getByChargeItemId
                    (storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(), now);
            storeChargeItem.setStoreChargeItemPromotion(chargeItemPromotion);
		}
        return storeChargeItem;
    }

    public StoreChargeItem getStoreChargeItemByName(int merchantId, long storeId, String name) throws T5weiException {
        List<StoreChargeItem> list = this.storeChargeItemDAO.getListByStoreId(merchantId, storeId,
                true, true);
        for (StoreChargeItem storeChargeItem : list) {
            if (storeChargeItem.getName().equals(name)) {
                return storeChargeItem;
            }
        }
        throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_INVALID.getValue(),
                "storeId[" + storeId + "] name[" + name + "] chargeItem invalid");
    }

    /**
     * 删除收费项目
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param chargeItemId 收费项目id
     * @throws T5weiException
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void deleteStoreChargeItem(int merchantId, long storeId, long chargeItemId) throws T5weiException {
        StoreChargeItem storeChargeItem = this.storeChargeItemDAO.loadById(merchantId, storeId, chargeItemId, false, true);
        if (storeChargeItem.isDeleted()) {
            return;
        }
        storeChargeItem.makeDeleted();
        this.storeChargeSubitemDAO.makeDeletedByChargeItemId(merchantId, storeId, chargeItemId, true);
        this.storeChargeItemPriceDAO.makeDeletedByChargeItemId(merchantId, storeId, chargeItemId, true);
        //周期创建时间在今天之前的,结束时间在今天之后的数据,需要把结束时间设置为昨天晚上最后1ms
        long now = System.currentTimeMillis();
        long todayBeginTime = DateUtil.getBeginTime(now, null);
        long todayEndTime = DateUtil.getEndTime(now, null);
        MutableDateTime mdt = new MutableDateTime(todayEndTime);
        mdt.addDays(-1);
        long yesEndTime = mdt.getMillis();
        //周期设置数据中，以前的生效数据有效期为 yesEndTime
        this.storeChargeItemWeekDAO.updateEndTimeForValid(merchantId, storeId, chargeItemId, todayBeginTime, now, yesEndTime);
        //删除 创建时间 >= 今天创建的本周生效的数据
        this.storeChargeItemWeekDAO.deleteForFuture(merchantId, storeId, chargeItemId, todayBeginTime);

        /////add by lizhijun  start
        //周期设置备份表中，将备份表中生效的数据有效期设置为 yesEndTime
        this.storeChargeItemWeekBackDAO.updateEndTimeForValid(merchantId, storeId, chargeItemId, todayBeginTime, now, yesEndTime);
        //删除 备份表中未生效的数据的周期设置
        this.storeChargeItemWeekBackDAO.deleteForFuture(merchantId, storeId, chargeItemId, todayBeginTime);
        /////add by lizhijun  end

        this.storeMenuDisplayDAO.deleteByChargeItemId(merchantId, storeId, chargeItemId);
    }

    public void deleteStoreChargeItemPrice(int merchantId, long storeId, long itemPriceId) throws T5weiException {
        long now = System.currentTimeMillis();
        StoreChargeItemPrice storeChargeItemPrice = this.storeChargeItemPriceDAO.loadById(merchantId, storeId, itemPriceId);
        if (storeChargeItemPrice.isSpecTimePrice(now)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_PRICE_CURRENT_CAN_NOT_BE_DELETED.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] itemPriceId[" + itemPriceId + "] can not be deleted");
        }
        //删除未来的
        storeChargeItemPrice.delete();
        //更新当前生效的数据有效期为永远
        this.storeChargeItemPriceDAO.updateEndTimeByChargeItemIdInExpiryDate(merchantId, storeId, storeChargeItemPrice.getChargeItemId(), now, Long.MAX_VALUE);
    }

    /**
     * 获得包含此产品的所有有效收费项目
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param prouctId   指定需要包含的产品id
     * @param time       指定日期，根据日期查询当前有效的价格和打折支持
     * @return 查询集合
     */
    public List<StoreChargeItem> getStoreChargeItemsContainProduct(int merchantId, long storeId, long prouctId, boolean loadSubitems, boolean loadProduct, boolean loadAllAvailablesPrices, boolean loadAllItemWeeks, boolean loadTimeBucket, long time) {
        //获得包含产品的收费项目
        List<Long> chargeItemIds = this.storeChargeSubitemDAO.getChargeItemIdsContainProduct(merchantId, storeId, prouctId);
        //获得收费项目列表
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, true, true);
        if (loadAllAvailablesPrices) {
            //获得有效的价格
            this.menuServiceUtil.buildPriceForCurAndNext(storeChargeItems, merchantId, storeId, chargeItemIds, time, true, true);
        }
        if (loadAllItemWeeks) {
            //获得收费项目的所有有效周期
            this.menuServiceUtil.buildWeek(merchantId, storeId, time, storeChargeItems, chargeItemIds, loadTimeBucket);
        }
        if (loadSubitems) {
            this.menuServiceUtil.buildSubitems(merchantId, storeId, storeChargeItems, chargeItemIds, loadProduct, true, true);
        }
        return storeChargeItems;
    }

    public List<StoreChargeItem> getStoreChargeItemsByStoreId(QueryStoreChargeItemListParam param, long time) {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        boolean loadSubitems = param.isLoadSubitems();
        boolean loadProduct = param.isLoadProduct();
        boolean loadAllAvailablesPrices = param.isLoadAllAvailablePrices();
        boolean loadAllItemWeeks = param.isLoadAllItemWeeks();
        boolean loadTimeBucket = param.isLoadTimeBucket();
        boolean loadMealPort = param.isLoadMealPort();
        //获得收费项目列表
        List<StoreChargeItem> storeChargeItems;
        if (param.getChargeItemIdsSize() > 0) {
            storeChargeItems = this.storeChargeItemDAO.getListInIds(merchantId, storeId, param.getChargeItemIds(), true, true);
        } else {
            storeChargeItems = this.storeChargeItemDAO.getListByStoreId(merchantId, storeId, true, true);
        }
        List<Long> chargeItemIds = Lists.newArrayList();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            if (chargeItemIds.contains(storeChargeItem.getChargeItemId())) {
                continue;
            }
            chargeItemIds.add(storeChargeItem.getChargeItemId());
        }
        if (loadAllAvailablesPrices) {
            //获得有效的价格
            this.menuServiceUtil.buildPriceForCurAndNext(storeChargeItems, merchantId, storeId, chargeItemIds, time, true, true);
        }
        if (loadAllItemWeeks) {
            //获得收费项目的所有有效周期
            this.menuServiceUtil.buildWeek(merchantId, storeId, time, storeChargeItems, chargeItemIds, loadTimeBucket);
        }
        if (loadSubitems) {
            this.menuServiceUtil.buildSubitems(merchantId, storeId, storeChargeItems, chargeItemIds, loadProduct, true, true);
        }
        if (loadMealPort) {
            List<Long> portIds = Lists.newArrayList();
            for (StoreChargeItem storeChargeItem : storeChargeItems) {
                if (storeChargeItem.getPortId() > 0) {
                    portIds.add(storeChargeItem.getPortId());
                }
            }
            if (!portIds.isEmpty()) {
                Map<Long, StoreMealPort> portMap = this.storeMealPortDAO.getMapInIds(merchantId, storeId, portIds, true);
                for (StoreChargeItem storeChargeItem : storeChargeItems) {
                    storeChargeItem.setStoreMealPort(portMap.get(storeChargeItem.getPortId()));
                }
            }
        }
        return storeChargeItems;
    }
    
    /**
     * 根据一组收费项目id，查询指定时间的收费项目数据，(价与时间有关)
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param chargeItemIds 收费项目id集合
     * @param time          指定日期时间戳，用来获取价格,=0时,表示不获取价格
     * @return 收费项目集合
     */
    public List<StoreChargeItem> getStoreChargeItemsInIds(int merchantId, long storeId, List<Long> chargeItemIds, long time) {
        return this.getStoreChargeItemsInIds(merchantId, storeId, chargeItemIds, time, false, false);
    }

    /**
     * 获取收费项目集合
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param chargeItemIds 收费项目id集合
     * @param time          大于0时，收费项目会组装价格信息
     * @param enableSlave   是否支持slave查询
     * @param enableCache   是否支持缓存查询
     * @return 收费项目list
     */
    public List<StoreChargeItem> getStoreChargeItemsInIds(int merchantId, long storeId, List<Long> chargeItemIds, long time, boolean enableSlave, boolean enableCache) {
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, enableSlave, enableCache);
        this.menuServiceUtil.buildSubitems(merchantId, storeId, storeChargeItems, chargeItemIds, true, enableSlave, enableCache);
        if (time > 0) {
            //获得有效的价格
            this.menuServiceUtil.buildPriceForCurAndNext(storeChargeItems, merchantId, storeId, chargeItemIds, time, enableSlave, enableCache);
        }
        return storeChargeItems;
    }

    public Map<Long, StoreChargeItem> getStoreChargeItemMapInIds(int merchantId, long storeId, List<Long> chargeItemIds, long time, boolean enableSlave, boolean enableCache) {
        List<StoreChargeItem> list = this.getStoreChargeItemsInIds(merchantId, storeId, chargeItemIds, time, enableSlave, enableCache);
        Map<Long, StoreChargeItem> map = Maps.newHashMap();
        for (StoreChargeItem chargeItem : list) {
            map.put(chargeItem.getChargeItemId(), chargeItem);
        }
        return map;
    }

    public List<StoreProduct> getStoreProducts(List<StoreChargeItem> storeChargeItems) {
        Map<Long, StoreProduct> storeProductMap = Maps.newHashMap();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            if (storeChargeItem.getStoreChargeSubitems() == null) {
                continue;
            }
            for (StoreChargeSubitem storeChargeSubitem : storeChargeItem.getStoreChargeSubitems()) {
                if (storeChargeSubitem.getStoreProduct() != null) {
                    storeProductMap.put(storeChargeSubitem.getProductId(), storeChargeSubitem.getStoreProduct());
                }
            }
        }
        List<StoreProduct> storeProducts = Lists.newArrayList();
        storeProducts.addAll(storeProductMap.values());
        return storeProducts;
    }

    /**
     * 获得包含指定产品的所有周期设置
     *
     * @param merchantId 商户id
     * @param storeId    店铺id
     * @param time       指定时间
     * @param productId  产品id
     * @return 查询集合
     */
    public List<StoreProductWeek> getStoreProductWeeksByProductIdForBiz(int merchantId, long storeId, long time, long productId) {
        return this.getStoreProductWeeksByProductId(merchantId, storeId, time, productId, false, false);
    }

    private List<StoreProductWeek> getStoreProductWeeksByProductId(int merchantId, long storeId, long time, long productId, boolean enableSlave, boolean enableCache) {
        List<Long> chargeItemIds = this.storeChargeSubitemDAO.getChargeItemIdsContainProduct(merchantId, storeId, productId);
        List<StoreChargeItemWeek> storeChargeItemWeeks = this.storeChargeItemWeekDAO.getListInChargeItemIds(merchantId, storeId, time, chargeItemIds);
        Map<String, StoreProductWeek> productWeekMap = Maps.newHashMap();
        for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
            boolean nextWeek = storeChargeItemWeek.isNextWeek(time);
            String key = storeChargeItemWeek.getTimeBucketId() + "_" + storeChargeItemWeek.getWeekDay() + "_" + nextWeek;
            if (productWeekMap.containsKey(key)) {
                continue;
            }
            StoreProductWeek storeProductWeek = new StoreProductWeek();
            storeProductWeek.setWeekDay(storeChargeItemWeek.getWeekDay());
            storeProductWeek.setTimeBucketId(storeChargeItemWeek.getTimeBucketId());
            storeProductWeek.setNextWeek(nextWeek);
            productWeekMap.put(key, storeProductWeek);
        }
        List<StoreProductWeek> list = Lists.newArrayList();
        list.addAll(productWeekMap.values());
        this.menuServiceUtil.buildTimeBucketForProductWeek(merchantId, storeId, list, enableSlave, enableCache);
        return list;
    }

    /**
     * 获得指定日期、营业时段的产品列表
     *
     * @param merchantId    商户id
     * @param storeId       店铺id
     * @param time          日期时间戳
     * @param timeBuceketId 营业时段id
     * @return 产品集合
     */
    public List<StoreProduct> getStoreProductsForDate(int merchantId, long storeId, long time, long timeBuceketId, boolean enableSlave, boolean enableCache) {
        List<Long> productIds = this.getStoreProductIdsForDate(merchantId, storeId, time, timeBuceketId, enableSlave, enableCache);
        return this.storeProductDAO.getListInIds(merchantId, storeId, productIds, enableSlave, enableCache);
    }

    /**
     * 获得指定日期、营业时段的产品id列表
     *
     * @param merchantId   商户id
     * @param storeId      店铺id
     * @param time         日期时间戳
     * @param timeBucketId 营业时段id
     * @return 产品id集合
     */
    public List<Long> getStoreProductIdsForDate(int merchantId, long storeId, long time, long timeBucketId, boolean enableSlave, boolean enableCache) {
        StoreDateBizSetting storeDateBizSetting = this.storeDateBizSettingDAO
                .getForSelectedDate(merchantId, storeId, time, enableSlave, enableCache);
        if (StoreDateBizSetting.isPaused(storeDateBizSetting)) {
            return new ArrayList<>(0);
        }
        if (storeDateBizSetting != null) {
            StoreDateTimeBucketSetting storeDateTimeBucketSetting = this.storeDateTimeBucketSettingDAO.getForDate(merchantId, storeId, time, timeBucketId, enableSlave, enableCache);
            if (StoreDateTimeBucketSetting.isPaused(storeDateTimeBucketSetting)) {
                return new ArrayList<>(0);
            }
        }
        int weekDay = StoreDateBizSetting.getWeekDay(storeDateBizSetting, time);
        List<Long> chargeItemIds = this.storeChargeItemWeekDAO.getChargeItemIdsForWeekDay(merchantId, storeId,
                timeBucketId, weekDay, time, enableSlave, enableCache);
        return this.storeChargeSubitemDAO.getProductIdsInChargeItemIds(merchantId, storeId, chargeItemIds);
    }

    /**
     * 集合中所有收费项目是否都支持快取
     *
     * @param storeChargeItems 收费项目集合
     * @return true:都支持
     */
    public boolean isAllQuickTake(List<StoreChargeItem> storeChargeItems) {
        //判断收费项目是否开启快取
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            if (!storeChargeItem.isQuickTake()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 查询收费项目和产品对应的出餐口
     *
     * @param merchantId
     * @param storeId
     * @param queryProductPortParams 请求参数
     * @return key=[chargeItemId]_[productId] value=portId
     */
    public Map<String, Long> getPortIdMap(int merchantId, long storeId, List<QueryProductPortParam>
            queryProductPortParams) {
        List<Long> chargeItemIds = Lists.newArrayList();
        List<Long> productIds = Lists.newArrayList();
        for (QueryProductPortParam param : queryProductPortParams) {
            chargeItemIds.add(param.getChargeItemId());
            if (param.getProductIds() != null) {
                productIds.addAll(param.getProductIds());
            }
        }
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, true, true);
        this.menuServiceUtil.buildSubitems(merchantId, storeId, storeChargeItems, chargeItemIds, true, true, true);
        Map<Long, StoreProduct> productMap = this.menuServiceUtil.getProductMap(storeChargeItems);
        //检查订单中的产品是否在收费项目中有变更
        List<Long> productIdsRemain = new ArrayList<>();
        productIdsRemain.addAll(productIds);
        Iterator<Long> it = productIdsRemain.iterator();
        while (it.hasNext()) {
            Long productId = it.next();
            if (!productMap.containsKey(productId)) {
                it.remove();
            }
        }
        Map<Long, StoreProduct> productRemainMap = this.storeProductDAO.getMapInIds(merchantId, storeId, productIdsRemain, true, true);
        productMap.putAll(productRemainMap);
        long defaultPortId = 0;
        StoreMealPort storeMealPortDefault = this.storeMealPortDAO.getFirst(merchantId, storeId, true);
        if (storeMealPortDefault != null) {
            defaultPortId = storeMealPortDefault.getPortId();
        }
        Map<Long, StoreChargeItem> itemMap = this.menuServiceUtil.toChargeItemMap(storeChargeItems);
        Map<String, Long> map = Maps.newHashMap();
        for (QueryProductPortParam param : queryProductPortParams) {
            StoreChargeItem item = itemMap.get(param.getChargeItemId());
            if (item == null) {
                if (param.getProductIds() == null || param.getProductIds().isEmpty()) {
                    continue;
                }
                //当获取不到收费项目时，使用产品的出餐口或者默认的出餐口(防御使用)
                for (Long productId : param.getProductIds()) {
                    StoreProduct storeProduct = productMap.get(productId);
                    String key = param.getChargeItemId() + "_" + productId;
                    long productPortId;
                    if (storeProduct == null) {
                        productPortId = defaultPortId;
                    } else {
                        productPortId = storeProduct.getPortId();
                        if (productPortId <= 0) {
                            productPortId = defaultPortId;
                        }
                    }
                    map.put(key, productPortId);
                }
            } else {
                long itemPortId = item.getPortId();
                for (Long productId : param.getProductIds()) {
                    StoreProduct storeProduct = productMap.get(productId);
                    String key = param.getChargeItemId() + "_" + productId;
                    long productPortId;
                    if (storeProduct == null) {
                        productPortId = defaultPortId;
                    } else {
                        productPortId = storeProduct.getPortId();
                        if (productPortId <= 0) {
                            productPortId = defaultPortId;
                        }
                    }
                    long portId;
                    if (itemPortId <= 0) {
                        portId = productPortId;
                    } else {
                        portId = itemPortId;
                    }
                    map.put(key, portId);
                }
            }
        }
        return map;
    }

    /**
     * Created by lixuwei on 2016-03-20.
     * <p>
     * 更新收费项目的 分类ID
     *
     * @param merchantId
     * @param storeId
     * @param categoryId    分类ID
     * @param chargeItemIds 需要进行更新的收费项目集合
     * @return
     */
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public List<StoreChargeItem> updateStoreChargeItemsCategory(int merchantId, long storeId, int categoryId, List<Long> chargeItemIds) throws T5weiException, TException {
        List<StoreChargeItem> chargeItems = storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, false, false);
//        for (StoreChargeItem storeChargeItem : chargeItems) {
//            storeChargeItem.setCategoryId(categoryId);
//        }
//        //批量更新收费项目的分类ID
//        batchUpdateCategory(merchantId, storeId, categoryId, chargeItems);

        //更新关联的产品
        List<Long> queryChargeItemIds = chargeItems.stream().map(StoreChargeItem::getChargeItemId).collect(Collectors.toList());
        List<Long> onlyChargeItemIds = storeChargeSubitemDAO.getChargeItemIdsByOnlyChargeItem(merchantId, storeId, queryChargeItemIds);
        List<Long> updateProductIds = storeChargeSubitemDAO.getProductIdsInChargeItemIds(merchantId, storeId, onlyChargeItemIds);

        //找到同一个产品ID关联的收费项目
        List<Long> glChargeItemIds = storeChargeSubitemDAO.getOnlyChargeItemIdsContainProductIds(merchantId, storeId, updateProductIds);
        List<StoreChargeItem> glChargeItems = storeChargeItemDAO.getListInIds(merchantId, storeId, glChargeItemIds, false, true);
        List<StoreChargeItem> updateChargeItems = mergeChargeItem(chargeItems, glChargeItems, categoryId);
        storeChargeItemDAO.batchUpdateCategory(merchantId, storeId, updateChargeItems);

        storeProductService.updateProductCategoryByChargeItem(merchantId, storeId, categoryId, updateProductIds);

        return chargeItems;
    }

    private List<StoreChargeItem> mergeChargeItem(List<StoreChargeItem> chargeItems, List<StoreChargeItem> glChargeItems, int categoryId) {
        List<StoreChargeItem> updateChargeItems = Lists.newArrayList();
        Map<Long, StoreChargeItem> chargeItemMap = Maps.newHashMap();
        for (StoreChargeItem chargeItem : chargeItems) {
            chargeItem.setCategoryId(categoryId);
            chargeItemMap.put(chargeItem.getChargeItemId(), chargeItem);
        }
        for (StoreChargeItem chargeItem : glChargeItems) {
            chargeItem.setCategoryId(categoryId);
            chargeItemMap.put(chargeItem.getChargeItemId(), chargeItem);
        }
        Collection<StoreChargeItem> values = chargeItemMap.values();
        updateChargeItems.addAll(values);
        return updateChargeItems;
    }

    /**
     * 查询店铺 收费项目和对应的分类
     *
     * @param merchantId
     * @param storeId
     * @return
     */
    public List<StoreChargeItemWithCategoryDTO> getStoreChargeItemWithCategory(int merchantId, long storeId) {

        List<StoreChargeItemWithCategoryDTO> dtos = Lists.newArrayList();

        List<Map<String, Object>> maps = storeChargeItemDAO.getChargeItemWithCategory(merchantId, storeId);
        for (Map<String, Object> map : maps) {

            Long chargeItemId = Long.valueOf(map.get("chargeItemId").toString());
            String chargeItemName = map.get("chargeItemName").toString();
            Integer categoryId = Integer.valueOf(map.get("categoryId").toString());
            Optional<Object> categoryNameOptional = Optional.ofNullable(map.get("categoryName"));
            String categoryName = categoryNameOptional.orElse("未分类").toString();

            StoreChargeItemWithCategoryDTO dto = new StoreChargeItemWithCategoryDTO();
            dto.setChargeItemId(chargeItemId);
            dto.setChargeItemName(chargeItemName);
            dto.setCategoryId(categoryId);
            dto.setCategoryName(categoryName);

            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 为收费项目 更新 '成本设置'的状态
     *
     * @param chargeItemds
     * @param productId
     */
    public void updateChargeItemPrimeCastSet(int merchantId, long storeId, long productId, List<Long> chargeItemds) {
        List<StoreChargeItem> storeChargeItems = storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemds, true, true);
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            //关联到 product
            //storeProductDAO.getStoreProductWithChargeItemId(merchantId, storeId, storeChargeItem);
            //subitem
            List<StoreChargeSubitem> subitems = storeChargeSubitemDAO.getListByChargeItemId(merchantId, storeId, storeChargeItem.getChargeItemId(), true, true);
            ArrayList<Long> productIds = new ArrayList<>();
            for (StoreChargeSubitem subitem : subitems) {
                productIds.add(subitem.getProductId());
            }
            if (subitems.size() == 0) {
                continue;
            }
            //product
            List<StoreProduct> products = storeProductDAO.getListInIds(merchantId, storeId, productIds);

            if (products.size() == 0) {
                continue;
            }
            int mark = 0;
            for (StoreProduct product : products) {
                if (product.isPrimeCostSet()) {
                    mark += 1;
                }
            }

            if (mark == 0) {
                if (storeChargeItem.getPrimeCostSet() != ChargeItemPrimeCostSetEnum.NO_SET.getValue()) {
                    storeChargeItem.snapshot();
                    storeChargeItem.setPrimeCostSet(ChargeItemPrimeCostSetEnum.NO_SET.getValue());
                    storeChargeItem.update();
                }
            } else if (products.size() == mark) {
                if (storeChargeItem.getPrimeCostSet() != ChargeItemPrimeCostSetEnum.ALL_SET.getValue()) {
                    storeChargeItem.snapshot();
                    storeChargeItem.setPrimeCostSet(ChargeItemPrimeCostSetEnum.ALL_SET.getValue());
                    storeChargeItem.update();
                }
            } else {
                if (storeChargeItem.getPrimeCostSet() != ChargeItemPrimeCostSetEnum.PART_SET.getValue()) {
                    storeChargeItem.snapshot();
                    storeChargeItem.setPrimeCostSet(ChargeItemPrimeCostSetEnum.PART_SET.getValue());
                    storeChargeItem.update();
                }
            }
        }

    }

    /**
     * 获得收费项目和包含的产品
     *
     * @param merchantId
     * @param storeId
     */
    @Deprecated
    public List<StoreChargeItemWithProductDTO> getStoreChargeItemsWithProduct(int merchantId, long storeId) {

        List<StoreChargeItemWithProductDTO> dtos = Lists.newArrayList();
        //获得所有的收费项目
        List<StoreChargeItem> storeChargeItems = storeChargeItemDAO.getStoreChargeItems(merchantId, storeId);
        //获得收费项目下的收费子项
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            List<StoreChargeSubitem> subitems = storeChargeSubitemDAO.getListByChargeItemId(merchantId, storeId, storeChargeItem.getChargeItemId(), true, true);
            //收费子项
            List<StoreChargeSubitemDTO> subitemDTOs = Lists.newArrayList();
            for (StoreChargeSubitem subitem : subitems) {
                StoreChargeSubitemDTO copy = BeanUtil.copy(subitem, StoreChargeSubitemDTO.class);
                subitemDTOs.add(copy);
            }

            List<Long> productIds = Lists.newArrayList();
            for (StoreChargeSubitem subitem : subitems) {
                productIds.add(subitem.getProductId());
            }
            //查找收费子项对应的产品
            List<StoreProduct> products = storeProductDAO.getListInIds(merchantId, storeId, productIds, true, true);
            List<StoreProductDTO> productDTOs = Lists.newArrayList();
            for (StoreProduct product : products) {
                StoreProductDTO copy = BeanUtil.copy(product, StoreProductDTO.class);
                productDTOs.add(copy);
            }

            StoreChargeItemWithProductDTO dto = new StoreChargeItemWithProductDTO();
            dto.setMerchantId(merchantId);
            dto.setStoreId(storeId);
            dto.setChargeItemId(storeChargeItem.getChargeItemId());
            dto.setChargeItemName(storeChargeItem.getName());
            dto.setCategoryId(storeChargeItem.getCategoryId());
            dto.setPrimeCostSet(storeChargeItem.getPrimeCostSet());
            dto.setProducts(productDTOs);
            dto.setSubitems(subitemDTOs);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * TODO 需要修改 性能太差
     * 根据chargeItemId集合 获得收费项目和包含的产品
     *
     * @param merchantId
     * @param storeId
     * @param chargeItemIds
     * @return
     */
    @Deprecated
    public List<StoreChargeItemWithProductDTO> getStoreChargeItemsWithProduct(int merchantId, long storeId, List<Long> chargeItemIds) {
        List<StoreChargeItemWithProductDTO> dtos = Lists.newArrayList();
        //获得所有的收费项目
        List<StoreChargeItem> storeChargeItems = storeChargeItemDAO.getStoreChargeItems(merchantId, storeId, chargeItemIds);
        Set<Long> itemIds = new HashSet<>();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            itemIds.add(storeChargeItem.getChargeItemId());
        }
        List<Long> queryChargeItemIds = Lists.newArrayList();
        queryChargeItemIds.addAll(itemIds);

        //获得当前查询日期有效的价格
        this.menuServiceUtil.buildPriceForCurAndNext(storeChargeItems, merchantId, storeId, queryChargeItemIds, System.currentTimeMillis(), true, true);
        //获得收费项目下的收费子项
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            List<StoreChargeSubitem> subitems = storeChargeSubitemDAO.getListByChargeItemId(merchantId, storeId, storeChargeItem.getChargeItemId(), true, true);
            //收费子项
            List<StoreChargeSubitemDTO> subitemDTOs = Lists.newArrayList();
            for (StoreChargeSubitem subitem : subitems) {
                StoreChargeSubitemDTO copy = BeanUtil.copy(subitem, StoreChargeSubitemDTO.class);
                subitemDTOs.add(copy);
            }

            List<Long> productIds = Lists.newArrayList();
            for (StoreChargeSubitem subitem : subitems) {
                productIds.add(subitem.getProductId());
            }
            //查找收费子项对应的产品
            List<StoreProduct> products = storeProductDAO.getListInIds(merchantId, storeId, productIds, true, true);

            List<StoreProductDTO> productDTOs = Lists.newArrayList();
            for (StoreProduct product : products) {
                StoreProductDTO copy = BeanUtil.copy(product, StoreProductDTO.class);
                productDTOs.add(copy);
            }

            StoreChargeItemWithProductDTO dto = new StoreChargeItemWithProductDTO();
            dto.setMerchantId(merchantId);
            dto.setStoreId(storeId);
            dto.setChargeItemId(storeChargeItem.getChargeItemId());
            dto.setChargeItemName(storeChargeItem.getName());
            dto.setChargeItemPrice(storeChargeItem.getCurPrice());
            dto.setCategoryId(storeChargeItem.getCategoryId());
            dto.setPrimeCostSet(storeChargeItem.getPrimeCostSet());
            dto.setProducts(productDTOs);
            dto.setSubitems(subitemDTOs);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * 根据产品ID 获得只包含一个产品(不关心数量)的收费项目(单品),并修改分类ID
     *
     * @param merchantId
     * @param storeId
     * @param categoryId
     * @param updateProductIds
     */
    public void updateChargeItemsCategoryByProductIds(int merchantId, long storeId, int categoryId, List<Long> updateProductIds) {
        List<Long> chargeItemIds = storeChargeSubitemDAO.getOnlyChargeItemIdsContainProductIds(merchantId, storeId, updateProductIds);
        if (chargeItemIds.size() <= 0) {
            return;
        }
        List<StoreChargeItem> storeChargeItems = storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, false, true);
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            storeChargeItem.setCategoryId(categoryId);
        }
        batchUpdateCategory(merchantId, storeId, categoryId, storeChargeItems);
    }

    /**
     * 批量更新收费项目的分类
     *
     * @param merchantId
     * @param storeId
     * @param categoryId
     * @param updateChargeItems
     */
    private void batchUpdateCategory(int merchantId, long storeId, int categoryId, List<StoreChargeItem> updateChargeItems) {
        if (updateChargeItems == null || updateChargeItems.size() <= 0) {
            return;
        }
        storeChargeItemDAO.batchUpdateCategory(merchantId, storeId, updateChargeItems);
    }

    public List<StoreChargeItem> getSimpleStoreChargeItems(int merchantId, long storeId, List<Long> chargeItemIds) {
        return this.storeChargeItemDAO.getListInIds(merchantId, storeId, chargeItemIds, false, false);
    }

    public boolean hasEnableSameTakeMode(int merchantId, long storeId, List<Long> chargeItemIds, int takeMode) throws T5weiException, TException {
        List<StoreChargeItem> items = this.getSimpleStoreChargeItems(merchantId, storeId, chargeItemIds);
        //是否支持快取
        if (takeMode == StoreOrderTakeModeEnum.QUICK_TAKE.getValue()) {
            for (StoreChargeItem item : items) {
                if (!item.isQuickTake()) {
                    return false;
                }
            }
            return true;
        }
        //是否支持自取
        if (takeMode == StoreOrderTakeModeEnum.TAKE_OUT.getValue()) {
            for (StoreChargeItem item : items) {
                if (!item.isEnableUserTake()) {
                    return false;
                }
            }
            return true;
        }
        //是否支持外送
        if (takeMode == StoreOrderTakeModeEnum.SEND_OUT.getValue()) {
            for (StoreChargeItem item : items) {
                if (!item.isEnableDelivery()) {
                    return false;
                }
            }
            return true;
        }
        //是否支持堂食
        if (takeMode == StoreOrderTakeModeEnum.DINE_IN.getValue()) {
            for (StoreChargeItem item : items) {
                if (!item.isEnableDineIn()) {
                    return false;
                }
            }
            return true;
        }
        throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "takeMode[" + takeMode + "] not supported");
    }

    // add by yangfei 2016-12-13 批量更新收费项目美团外卖开关
    public void batchUpdateChargeItemMeituanEnable(int merchantId, long storeId, List<Long> chargeItemIds, boolean enable) throws T5weiException {
        if (chargeItemIds == null || chargeItemIds.isEmpty()) {
            return;
        }
        storeChargeItemDAO.batchUpdateChargeItemMeituanEnable(merchantId, storeId, chargeItemIds, enable);
    }

    public void updateStoreChargeItemNotDelete(StoreChargeItem storeChargeItem) {
        storeChargeItem.snapshot();
        storeChargeItem.setDeleted(false);
        storeChargeItem.update();

        storeChargeItemPriceDAO.makeDeletedByChargeItemId(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(), false);
        storeChargeSubitemDAO.makeDeletedByChargeItemId(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), storeChargeItem.getChargeItemId(), false);
    }

    /**
     * 获取除指定收费项目外的其他收费项目
     * @param merchantId 商户id
     * @param storeId 店铺id
     * @param exceptedChargeItemId 需要排除的收费项目id
     * @return
     */
    public List<Long> getAllStoreChargeItemExceptId(int merchantId, long storeId, long exceptedChargeItemId) throws T5weiException {
        // 查询可用的收费项目,并验证指定收费项目是否有效
        List<StoreChargeItem> storeChargeItems = this.storeChargeItemDAO.getStoreChargeItems(merchantId, storeId);
        List<Long> storeChargeItemIds = StoreChargeItem.getIdList(storeChargeItems);
        if (!storeChargeItemIds.contains(exceptedChargeItemId)){
            throw new T5weiException(T5weiErrorCodeType.STORE_CHARGE_ITEM_INVALID.getValue(), StoreChargeItem.class.getName() + " merchantId[" + merchantId + "] storeId[" + storeId + "] chargeItemId[" + exceptedChargeItemId + "] invalid");
        }
        storeChargeItemIds.remove(exceptedChargeItemId);
        return storeChargeItemIds;
    }

}
