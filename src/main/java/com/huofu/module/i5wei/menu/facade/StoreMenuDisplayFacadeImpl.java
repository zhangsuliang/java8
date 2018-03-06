package com.huofu.module.i5wei.menu.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryDate;
import com.huofu.module.i5wei.inventory.service.StoreInventoryService;
import com.huofu.module.i5wei.menu.entity.*;
import com.huofu.module.i5wei.menu.service.*;
import com.huofu.module.i5wei.menu.validator.StoreMenuDisplayValidator;
import com.huofu.module.i5wei.setting.entity.StorePresellSetting;
import com.huofu.module.i5wei.setting.service.StorePresellService;
import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.menu.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.DateUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by akwei on 3/20/15.
 */
@ThriftServlet(name = "storeMenuDisplayFacadeServlet", serviceClass = StoreMenuDisplayFacade.class)
@Component
public class StoreMenuDisplayFacadeImpl implements StoreMenuDisplayFacade.Iface {

    private static Logger logger = Logger.getLogger(StoreMenuDisplayFacadeImpl.class);

    @Resource
    private SaveStoreMenuDisplayService saveStoreMenuDisplayService;

    @Resource
    private StoreMenuDisplayService storeMenuDisplayService;

    @Resource
    private StoreTimeBucketService storeTimeBucketService;

    @Resource
    private StoreMenuDisplayValidator storeMenuDisplayValidator;

    @Resource
    private StoreInventoryService storeInventoryService;


    @Resource
    private StorePresellService storePresellService;

    @Resource
    private StoreChargeItemService storeChargeItemService;

    @Resource
    private StoreMenuService storeMenuService;

    @Resource
    private MenuFacadeUtil menuFacadeUtil;

    @Override
    public StoreTimeBucketMenuDisplayDTO saveMenuDisplay(int merchantId, long storeId, long timeBucketId, List<StoreMenuDisplayParam> params) throws T5weiException, TException {
        this.storeMenuDisplayValidator.validateForSaveMenuDisplay(merchantId, storeId, timeBucketId, params);
        this.saveStoreMenuDisplayService.saveMenuDisplay(merchantId, storeId, timeBucketId, params);
        return this.getStoreTimeBucketMenuDisplayForSort(merchantId, storeId, timeBucketId);
    }

    @Override
    public StoreTimeBucketMenuDisplayDTO getStoreTimeBucketMenuDisplayForSort(int merchantId, long storeId, long timeBucketId) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucket(merchantId, storeId, timeBucketId);
        List<StoreMenuDisplayCat> storeMenuDisplayCats = this.storeMenuDisplayService.getStoreMenuDisplayCatsByTimeBucketId(merchantId, storeId, timeBucketId);
        List<StoreMenuDisplay> storeMenuDisplays = this.saveStoreMenuDisplayService.getMenuDisplay(merchantId, storeId, timeBucketId);
        Map<Long, StoreChargeItem> storeChargeItemMap = this.saveStoreMenuDisplayService.getStoreChargeItemMapByTimeBucketIdForSort(merchantId, storeId, timeBucketId);
        return menuFacadeUtil.buildStoreTimeBucketMenuDisplayDTO
                (storeMenuDisplayCats, storeMenuDisplays, storeChargeItemMap,
                        storeTimeBucket);
    }

    @Override
    public StoreTimeBucketMenuDisplayDTO getStoreTimeBucketMenuDisplay(int merchantId, long storeId, long timeBucketId, long time, boolean needInventory, boolean onlyWechat, long userId) throws T5weiException, TException {
        StoreTimeBucketMenuDisplayParam param = new StoreTimeBucketMenuDisplayParam();
        param.setMerchantId(merchantId);
        param.setStoreId(storeId);
        param.setTimeBucketId(timeBucketId);
        param.setDate(time);
        param.setNeedInventory(needInventory);
        param.setForWechat(onlyWechat);
        param.setUserId(userId);
        return this.getStoreTimeBucketMenuDisplayV2(param);
    }

    @Override
    public List<StoreDateTimeBucketDTO> getStoreDateTimeBucketByDate(int merchantId, long storeId, long date, int addDays) throws TException {
        long _date = DateUtil.getBeginTime(date, null);
        StorePresellSetting storePresellSetting = this.storePresellService.getStorePresellSetting(merchantId, storeId);
        boolean presellEnable = false;
        if (storePresellSetting != null) {
            presellEnable = storePresellSetting.isEnabled();
        }
        int _addDays = addDays;
        if (!presellEnable) {
            _addDays = 0;
        }
        List<StoreDateTimeBucket> storeDateTimeBuckets = this
                .storeMenuDisplayService.getStoreDateTimeBucketByDate
                        (merchantId, storeId, _date, _addDays, presellEnable);
        List<StoreDateTimeBucketDTO> storeDateTimeBucketDTOs = new ArrayList<>();
        for (StoreDateTimeBucket o : storeDateTimeBuckets) {
            StoreDateTimeBucketDTO storeDateTimeBucketDTO = new StoreDateTimeBucketDTO();
            storeDateTimeBucketDTO.setDate(o.getTime());
            List<StoreTimeBucketDTO> storeTimeBucketDTOs = BeanUtil.copyList(o
                    .getStoreTimeBuckets(), StoreTimeBucketDTO.class);
            storeDateTimeBucketDTO.setStoreTimeBucketDTOs(storeTimeBucketDTOs);
            storeDateTimeBucketDTOs.add(storeDateTimeBucketDTO);
        }
        return storeDateTimeBucketDTOs;
    }

    @Override
    public List<SimpleStoreChargeItemInventoryDTO> getSimpleStoreChargeItemInventorys(int merchantId, long storeId, long timeBucketId, long time) throws T5weiException, TException {
        QueryInventorysParam param = new QueryInventorysParam();
        param.setMerchantId(merchantId);
        param.setStoreId(storeId);
        param.setTimeBucketId(timeBucketId);
        param.setTime(time);
        return this.getSimpleStoreChargeItemInventorysV2(param);
    }

    @Override
    public List<SimpleStoreChargeItemInventoryDTO> getSimpleStoreChargeItemInventorysV2(QueryInventorysParam param) throws T5weiException, TException {
        long _time = DateUtil.getBeginTime(param.getTime(), null);
        List<StoreChargeItem> storeChargeItems;
        long begin1 = System.currentTimeMillis();
        if (param.getChargeItemIdsSize() > 0) {
            storeChargeItems = this.storeChargeItemService.getStoreChargeItemsInIds(param.getMerchantId(), param.getStoreId(), param.getChargeItemIds(), 0);
        } else {
            storeChargeItems = this.storeChargeItemService.getChargeItemForDate(param.getMerchantId(), param.getStoreId(), param.getTimeBucketId(), _time);
        }
        long end1 = System.currentTimeMillis();

        long begin2 = System.currentTimeMillis();
        List<SimpleStoreChargeItemInventoryDTO> inventorys = this._buildInventorys(param.getMerchantId(), param.getStoreId(), param.getTimeBucketId(), _time, storeChargeItems);
        long end2 = System.currentTimeMillis();
        long t1 = end1 - begin1;
        long t2 = end2 - begin2;
        long t = 2000;
        if (t1 >= t || t2 >= t) {
            logger.error("getSimpleStoreChargeItemInventorysV2 menutime[" + t1 + "] inventorys[" + t2 + "]");
        }
        return inventorys;
    }

    private List<SimpleStoreChargeItemInventoryDTO> _buildInventorys(int merchantId, long storeId, long timeBucketId, long time, List<StoreChargeItem> storeChargeItems) {
        List<StoreProduct> storeProducts = this.storeChargeItemService.getStoreProducts(storeChargeItems);
        List<StoreInventoryDate> storeInventoryDates = this.storeInventoryService.getInventoryDate(merchantId, storeId, time, timeBucketId, storeProducts, false);
        Map<Long, StoreChargeItem> storeChargeItemMap = StoreChargeItem.listToMap(storeChargeItems);
        menuFacadeUtil.buildInventory(storeInventoryDates, storeChargeItemMap);
        List<SimpleStoreChargeItemInventoryDTO> simpleStoreChargeItemInventoryDTOs = Lists.newArrayList();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            SimpleStoreChargeItemInventoryDTO simpleStoreChargeItemInventoryDTO = new SimpleStoreChargeItemInventoryDTO();
            simpleStoreChargeItemInventoryDTO.setChargeItemId(storeChargeItem.getChargeItemId());
            simpleStoreChargeItemInventoryDTO.setChargeItemName(storeChargeItem.getName());
            simpleStoreChargeItemInventoryDTO.setUnlimit(storeChargeItem.isUnlimit());
            simpleStoreChargeItemInventoryDTO.setRemain(storeChargeItem.getRemain());
            simpleStoreChargeItemInventoryDTO.setDeleted(storeChargeItem.isDeleted());
            simpleStoreChargeItemInventoryDTOs.add(simpleStoreChargeItemInventoryDTO);
        }
        return simpleStoreChargeItemInventoryDTOs;
    }

    @Override
    public StoreTimeBucketMenuDisplayDTO getStoreTimeBucketMenuDisplayV2(StoreTimeBucketMenuDisplayParam param) throws T5weiException, TException {

        int merchantId = param.getMerchantId();

        long storeId = param.getStoreId();
        long _time = param.getDate();
        if (_time <= 0) {
            _time = System.currentTimeMillis();
        }
        long beginDate = DateUtil.getBeginTime(_time, null);
        StoreDateBizSetting storeDateBizSetting = this.storeMenuService.getStoreDateBizSettingForSelectedDate(merchantId, storeId, beginDate, true, true);
        if (StoreDateBizSetting.isPaused(storeDateBizSetting)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_TIME_BUCKET_CURRENT_NOT_SUPPORTED.getValue(),"storeId[" + storeId + "] date[" + new DateTime(beginDate) + "] is paused");
        }
        long timeBucketId = param.getTimeBucketId();
        boolean needInventory = param.isNeedInventory();

        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucketForDate(merchantId, storeId, timeBucketId, beginDate);
        StoreMenuQueryParam storeMenuQueryParam = new StoreMenuQueryParam();
        BeanUtil.copy(param, storeMenuQueryParam);
        storeMenuQueryParam.setDate(beginDate);
        storeMenuQueryParam.setTimeBucketId(storeTimeBucket.getTimeBucketId());
        storeMenuQueryParam.setStoreDateBizSetting(storeDateBizSetting);
        StoreMenuDisplayQueryResult storeMenuDisplayQueryResult = this.storeMenuDisplayService.getStoreMenu(storeMenuQueryParam);
        if (needInventory) {
            List<StoreInventoryDate> storeInventoryDates = this.storeInventoryService.getInventoryDate(merchantId, storeId, beginDate, storeTimeBucket.getTimeBucketId(), storeMenuDisplayQueryResult.getStoreProducts(), false);
            menuFacadeUtil.buildInventory(storeInventoryDates, storeMenuDisplayQueryResult.getStoreChargeItemMap());
        }
        StoreTimeBucketMenuDisplayDTO storeTimeBucketMenuDisplayDTO =
                menuFacadeUtil.buildStoreTimeBucketMenuDisplayDTO(storeMenuDisplayQueryResult.getStoreMenuDisplayCats(), storeMenuDisplayQueryResult.getStoreMenuDisplays(), storeMenuDisplayQueryResult.getStoreChargeItemMap(), storeTimeBucket);

        ClientTypeEnum clientTypeEnum = ClientTypeEnum.findByValue(param.getClientType());
        if (clientTypeEnum == null) {
            clientTypeEnum = ClientTypeEnum.CASHIER;
        }
	    this.menuFacadeUtil.buildPriceInfoAndPromotionInfoAndSendVisitInfo(param, clientTypeEnum,
	                                                                       storeMenuDisplayQueryResult.getStoreChargeItemMap().values(),
	                                                                       storeTimeBucket.getTimeBucketId(), _time, storeTimeBucketMenuDisplayDTO);
        return storeTimeBucketMenuDisplayDTO;
    }

    @Override
    public List<StoreMenuDTO> getStoreMenus(StoreMenusQueryParam param) throws TException {
        long _date = DateUtil.getBeginTime(param.getDate(), null);
        StorePresellSetting storePresellSetting = this.storePresellService.getStorePresellSetting(param.getMerchantId(), param.getStoreId());
        boolean presellEnable = false;
        if (storePresellSetting != null) {
            presellEnable = storePresellSetting.isEnabled();
        }
        MutableDateTime endDate = new MutableDateTime();
        endDate.setMillis(_date);
        int _addDays;
        if (presellEnable) {
            if (param.getAddDays() <= 0) {
                _addDays = 0;
            } else if (param.getAddDays() > 7) {
                _addDays = 7;
            } else {
                _addDays = param.getAddDays();
            }
        } else {
            _addDays = 0;
        }
        if (_addDays > 0) {
            endDate.addDays(_addDays);
        }
        int moreAddDays = 2;//由于周六周日可能正常休假，因此需要多获取2天数据
        endDate.addDays(moreAddDays);
        List<DateBizCal> dateBizCals = this.storeMenuService
                .getDateBizCalsForDateRange(param.getMerchantId(), param
                        .getStoreId(), _date, endDate.getMillis(), true);
        this.filterTimeBucketExpired(dateBizCals);
        int limit = _addDays + 1;//表示获取几天的数据
        dateBizCals = this.filterPausedDateBizCall(dateBizCals, param
                .isIngorePaused(), limit);
        List<StoreMenuDTO> list = Lists.newArrayList();
        list.addAll(dateBizCals.stream().map(this::buildStoreMenuDTO).collect(Collectors.toList()));
        return list;
    }

    private List<DateBizCal> filterPausedDateBizCall(List<DateBizCal> dateBizCals, boolean
            ignorePaused, int limit) {
        List<DateBizCal> list = Lists.newArrayList();
        if (ignorePaused) {
            for (DateBizCal dateBizCal : dateBizCals) {
                if (dateBizCal.isPaused()) {
                    continue;
                }
                Iterator<TimeBucketMenuCal> tcit = dateBizCal
                        .getTimeBucketMenuCals().iterator();
                while (tcit.hasNext()) {
                    TimeBucketMenuCal timeBucketMenuCal = tcit.next();
                    if (timeBucketMenuCal.isPaused() || timeBucketMenuCal
                            .getChargeItemAmount() <= 0) {
                        logger.info("filterpaused-" + new MutableDateTime(dateBizCal.getDate()) + "-" + timeBucketMenuCal.getTimeBucketId());
                        tcit.remove();
                    }
                }
                //表示所有营业时段都是暂停营业
                if (dateBizCal.getTimeBucketMenuCals().isEmpty()) {
                    continue;
                }
                list.add(dateBizCal);
                if (list.size() == limit) {
                    break;
                }
            }
            return list;
        }
        return DataUtil.subList(dateBizCals, 0, limit);
    }

    private void filterTimeBucketExpired(List<DateBizCal> dateBizCals) {
//        long today = DateUtil.getBeginTime(System.currentTimeMillis(), null);
        Iterator<DateBizCal> it = dateBizCals.iterator();
        while (it.hasNext()) {
            DateBizCal dateBizCal = it.next();
            Iterator<TimeBucketMenuCal> tcit = dateBizCal
                    .getTimeBucketMenuCals().iterator();
            while (tcit.hasNext()) {
                TimeBucketMenuCal timeBucketMenuCal = tcit.next();
                //当天营业时段都已过，只判断当天
                long now = System.currentTimeMillis();
                if (timeBucketMenuCal.getStoreTimeBucket().isAfterBizTime(dateBizCal.getDate(), now)) {
//                    logger.info("filter-remove-" + timeBucketMenuCal.getStoreTimeBucket().getName() + " - " + new MutableDateTime(+dateBizCal.getDate()) + " , " + now);
                    tcit.remove();
                } else {
//                    logger.info(timeBucketMenuCal.getStoreTimeBucket().getName() + " - " + new MutableDateTime(dateBizCal.getDate()) + " , " + now);
                }
            }

            if (dateBizCal.getTimeBucketMenuCals().isEmpty()) {
                it.remove();
            }
        }
    }

    private StoreMenuDTO buildStoreMenuDTO(DateBizCal dateBizCal) {
        StoreMenuDTO storeMenuDTO = new StoreMenuDTO();
        storeMenuDTO.setDate(dateBizCal.getDate());
        storeMenuDTO.setPaused(dateBizCal.isPaused());
        storeMenuDTO.setMenuTimeBucketDTOs(this.buildMenuTimeBucketDTOs
                (dateBizCal.getTimeBucketMenuCals()));
        return storeMenuDTO;
    }

    private List<MenuTimeBucketDTO> buildMenuTimeBucketDTOs(List<TimeBucketMenuCal> timeBucketMenuCals) {
        List<MenuTimeBucketDTO> list = Lists.newArrayList();
        list.addAll(timeBucketMenuCals.stream().map(this::buildMenuTimeBucketDTO).collect(Collectors.toList()));
        return list;
    }

    private MenuTimeBucketDTO buildMenuTimeBucketDTO(TimeBucketMenuCal
                                                             timeBucketMenuCal) {
        MenuTimeBucketDTO dto = new MenuTimeBucketDTO();
        dto.setPaused(timeBucketMenuCal.isPaused());
        dto.setInBizTime(timeBucketMenuCal.getStoreTimeBucket().isInBizTime());
        dto.setStoreTimeBucketDTO2(this.menuFacadeUtil.buildStoreTimeBucketDTO2(timeBucketMenuCal.getStoreTimeBucket()));
        return dto;
    }

    @Override
    public List<StoreMenuDisplayDTO> getStoreMenuDisplays(int merchantId, long storeId, long timeBucketId) throws TException {
        List<StoreMenuDisplay> storeMenuDisplays = storeMenuDisplayService
                .getStoreMenuDisplays(merchantId, storeId, timeBucketId);
        return this.menuFacadeUtil.buildStoreMenuDisplayDTOs(storeMenuDisplays);
    }

    @Override
    public List<StoreMenuDisplayCatDTO> getStoreMenuDisplayCats(int merchantId, long storeId, long timeBucketId) throws TException {
        List<StoreMenuDisplayCat> storeMenuDisplayCats = this
                .storeMenuDisplayService.getStoreMenuDisplayCatsByTimeBucketId(merchantId, storeId, timeBucketId);
        return this.menuFacadeUtil.buildStoreMenuDisplayCatDTOs
                (storeMenuDisplayCats);
    }

    @Override
    public DefMenuInfo4OrderDTO getDefMenuInfo4Order(int merchantId, long storeId, String timeBucketName, String chargeItemName) throws T5weiException, TException {
        StoreTimeBucket storeTimeBucket = this.storeTimeBucketService.getStoreTimeBucketByName
                (merchantId, storeId, timeBucketName);
        StoreChargeItem storeChargeItem = this.storeChargeItemService.getStoreChargeItemByName
                (merchantId, storeId, chargeItemName);
        DefMenuInfo4OrderDTO defMenuInfo4OrderDTO = new DefMenuInfo4OrderDTO();
        defMenuInfo4OrderDTO.setChargeItemId(storeChargeItem.getChargeItemId());
        defMenuInfo4OrderDTO.setTimeBucketId(storeTimeBucket.getTimeBucketId());
        return defMenuInfo4OrderDTO;
    }

    @Override
    public void copyMenuDisplay(int merchantId, long storeId, long timeBucketId, long targetTimeBucketId) throws T5weiException, TException {
        this.saveStoreMenuDisplayService.copyMenuDisplay(merchantId, storeId, timeBucketId, targetTimeBucketId);
    }

    @Override
    public StoreTimeBucketMenuDTO getStoreTimeBucketMenu(StoreTimeBucketMenuParam param) throws TException {


        StoreTimeBucketMenuDisplayParam menuDisplayParam = new StoreTimeBucketMenuDisplayParam();
        menuDisplayParam.setMerchantId(param.getMerchantId());
        menuDisplayParam.setStoreId(param.getStoreId());
        menuDisplayParam.setDate(param.getDay());
        menuDisplayParam.setTimeBucketId(param.getTimeBucketId());
        menuDisplayParam.setForWechat(false);
        menuDisplayParam.setForDelivery(false);
        menuDisplayParam.setLoadAvailablePromotion(false);
        menuDisplayParam.setClientType(ClientTypeEnum.CASHIER.getValue());//如果是收银台类型, 查询菜单时就不会过滤

        StoreTimeBucketMenuDisplayDTO menuDisplayDTO = this.getStoreTimeBucketMenuDisplayV2(menuDisplayParam);

        //得到营业时段和收费项目集合
        StoreTimeBucketDTO timeBucketDTO = menuDisplayDTO.getStoreTimeBucketDTO();
        Map<Long, List<StoreChargeItemDTO>> storeChargeItemDTOsMap = menuDisplayDTO.getStoreChargeItemDTOsMap();
        List<StoreChargeItemDTO> chargeItemDTOs = Lists.newArrayList();
        Collection<List<StoreChargeItemDTO>> values = storeChargeItemDTOsMap.values();
        for (List<StoreChargeItemDTO> value : values) {
            chargeItemDTOs.addAll(value);
        }

        Map<Long, StoreProductDTO> productDTOMap = new HashMap<>();
        for (StoreChargeItemDTO chargeItemDTO : chargeItemDTOs) {
            List<StoreChargeSubitemDTO> storeChargeSubitemDTOs = chargeItemDTO.getStoreChargeSubitemDTOs();
            if (storeChargeSubitemDTOs != null) {
                for (StoreChargeSubitemDTO storeChargeSubitemDTO : storeChargeSubitemDTOs) {
                    StoreProductDTO storeProductDTO = storeChargeSubitemDTO.getStoreProductDTO();
                    if (storeProductDTO != null) {
                        productDTOMap.put(storeProductDTO.getProductId(), storeProductDTO);
                    }
                }
            }
        }
        List<StoreProductDTO> productDTOs = Lists.newArrayList();
        productDTOs.addAll(productDTOMap.values());
        //组装DTO
        StoreTimeBucketMenuDTO storeTimeBucketMenuDTO = new StoreTimeBucketMenuDTO();
        storeTimeBucketMenuDTO.setDay(param.getDay());
        storeTimeBucketMenuDTO.setStoreChargeItems(chargeItemDTOs);
        storeTimeBucketMenuDTO.setStoreTimeBucketDTO(timeBucketDTO);
        storeTimeBucketMenuDTO.setStoreProducts(productDTOs);

        return storeTimeBucketMenuDTO;
    }

    public List<StoreTimeBucketMenuDTO> getStoreTimeBucketMenus(int merchantId, long storeId, List<Long> days, long timeBucketId) throws TException {

        List<StoreTimeBucketMenuDTO> menuDTOs = Lists.newArrayList();

        for (Long day : days) {

            if (timeBucketId > 0) {
                StoreTimeBucketMenuParam param = new StoreTimeBucketMenuParam();
                param.setMerchantId(merchantId);
                param.setStoreId(storeId);
                param.setDay(day);
                param.setTimeBucketId(timeBucketId);

                try {
                    StoreTimeBucketMenuDTO storeTimeBucketMenuDTO = getStoreTimeBucketMenu(param);
                    menuDTOs.add(storeTimeBucketMenuDTO);
                } catch (TException e) {
                    e.printStackTrace();
                }
            } else {
                List<StoreTimeBucket> timeBuckets = storeTimeBucketService.getStoreTimeBucketsInStoreForTime(merchantId, storeId, day);
                for (StoreTimeBucket timeBucket : timeBuckets) {
                    StoreTimeBucketMenuParam param = new StoreTimeBucketMenuParam();
                    param.setMerchantId(merchantId);
                    param.setStoreId(storeId);
                    param.setDay(day);
                    param.setTimeBucketId(timeBucket.getTimeBucketId());

                    try {
                        StoreTimeBucketMenuDTO storeTimeBucketMenuDTO = getStoreTimeBucketMenu(param);
                        menuDTOs.add(storeTimeBucketMenuDTO);
                    } catch (TException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return menuDTOs;
    }

    /**
     * 获取查询日期内(菜单上的收费项目)有效的收费项目
     *
     * @param merchantId
     * @param storeId
     * @param days         查询的日期集合
     * @param timeBucketId 营业时段ID
     */
    public List<StoreChargeItemDTO> getChargeItemsByMenuOfDays(int merchantId, long storeId, List<Long> days, long timeBucketId) throws TException {

        Map<Long, StoreChargeItemDTO> chargeItemDTOMap = new HashMap<>();

        List<StoreTimeBucketMenuDTO> storeTimeBucketMenus = getStoreTimeBucketMenus(merchantId, storeId, days, timeBucketId);
        for (StoreTimeBucketMenuDTO storeTimeBucketMenu : storeTimeBucketMenus) {
            List<StoreChargeItemDTO> storeChargeItems = storeTimeBucketMenu.getStoreChargeItems();
            for (StoreChargeItemDTO storeChargeItem : storeChargeItems) {
                chargeItemDTOMap.put(storeChargeItem.getChargeItemId(), storeChargeItem);
            }
        }

        return chargeItemDTOMap.values().stream().collect(Collectors.toList());
    }

    /**
     * 获取查询日期内(菜单上的产品)有效的产品
     *
     * @param merchantId
     * @param storeId
     * @param days         查询的日期集合
     * @param timeBucketId 营业时段ID
     */
    public List<StoreProductDTO> getProductsByMenuOfDays(int merchantId, long storeId, List<Long> days, long timeBucketId) throws TException {

        Map<Long, StoreProductDTO> productDTOMap = new HashMap<>();

        List<StoreTimeBucketMenuDTO> storeTimeBucketMenus = getStoreTimeBucketMenus(merchantId, storeId, days, timeBucketId);
        for (StoreTimeBucketMenuDTO storeTimeBucketMenu : storeTimeBucketMenus) {
            List<StoreProductDTO> storeChargeItems = storeTimeBucketMenu.getStoreProducts();
            for (StoreProductDTO storeProductDTO : storeChargeItems) {
                productDTOMap.put(storeProductDTO.getProductId(), storeProductDTO);
            }
        }

        return productDTOMap.values().stream().collect(Collectors.toList());
    }
}
