package com.huofu.module.i5wei.menu.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.base.SnsPublish;
import com.huofu.module.i5wei.base.SysConfig;
import com.huofu.module.i5wei.eventtype.EventType;
import com.huofu.module.i5wei.mealport.dao.StoreMealPortDAO;
import com.huofu.module.i5wei.mealport.entity.StoreMealPort;
import com.huofu.module.i5wei.mealport.facade.StoreMealPortFacadeUtil;
import com.huofu.module.i5wei.menu.dao.StoreChargeItemDAO;
import com.huofu.module.i5wei.menu.entity.*;
import com.huofu.module.i5wei.menu.service.SaveStoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.menu.service.StoreProductService;
import com.huofu.module.i5wei.menu.service.StoreTimeBucketService;
import com.huofu.module.i5wei.menu.validator.StoreChargeItemValidator;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisChargeItem;
import com.huofu.module.i5wei.promotion.service.StoreChargeItemPromotionService;
import com.huofu.module.i5wei.promotion.service.StoreChargeItemPromotionStatResult;
import com.huofu.module.i5wei.promotion.service.StorePromotionGratisService;
import com.huofu.module.i5wei.setting.entity.Store5weiSetting;
import com.huofu.module.i5wei.setting.service.Store5weiSettingService;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.mealport.StoreMealPortDTO;
import huofucore.facade.i5wei.menu.*;
import huofucore.facade.image.*;
import huofuhelper.util.DataUtil;
import huofuhelper.util.PageUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@ThriftServlet(name = "storeChargeItemFacadeServlet", serviceClass = StoreChargeItemFacade.class)
@Component
public class StoreChargeItemFacadeImpl implements StoreChargeItemFacade.Iface {

    @Autowired
    private StoreChargeItemService storeChargeItemService;

    @Autowired
    private StoreProductService storeProductService;

    @Autowired
    private StoreTimeBucketService storeTimeBucketService;

    @Autowired
    private StoreChargeItemValidator storeChargeItemValidator;

    @Autowired
    private Store5weiSettingService store5weiSettingService;

    @Autowired
    private SaveStoreChargeItemService saveStoreChargeItemService;

    @Autowired
    private MenuFacadeUtil menuFacadeUtil;

    @Autowired
    private StoreMealPortFacadeUtil storeMealPortFacadeUtil;

    @Autowired
    private StoreMealPortDAO storeMealPortDAO;

    @Autowired
    private StoreChargeItemPromotionService storeChargeItemPromotionService;

    @Autowired
    private StoreChargeItemDAO storeChargeItemDAO;
    
    @Autowired
    private SnsPublish snsPublish;

    @ThriftClient
    private ImageFacade.Iface imageFacade;

	@Autowired
	private StorePromotionGratisService storePromotionGratisService;

    @Override
    public StoreChargeItemDTO saveStoreChargeItemForAll(SaveStoreChargeItemParam param) throws TException {

        StoreChargeItem storeChargeItem;
        this.storeChargeItemValidator.validateForSave(param);
        String headImg = null;
        if (param.getHeadImageData() != null && !param.isClearImg()) {
            ImageParam imageParam = new ImageParam();
            imageParam.setData(param.getHeadImageData());
            {
                ImageResizeParam imageResizeParam = new ImageResizeParam();
                imageResizeParam.setQuality(90);
                imageResizeParam.setMaxWidth(640);
                imageResizeParam.setFlag(StoreChargeItem.IMG_SUBFIX_BIG);
                imageParam.addToImageResizeParams(imageResizeParam);
            }
            {
                ImageResizeParam imageResizeParam = new ImageResizeParam();
                imageResizeParam.setQuality(95);
                imageResizeParam.setMaxWidth(80);
                imageResizeParam.setFlag(StoreChargeItem.IMG_SUBFIX_THUMBNAIL);
                imageParam.addToImageResizeParams(imageResizeParam);
            }
            try {
                headImg = this.imageFacade.uploadImage(ImageBizType.ITEM, imageParam);
            } catch (TImageException e) {
                throw new T5weiException(e.getErrorCode(), e.getMessage());
            }
        }
	    long now = System.currentTimeMillis();
	    StoreTimeBucket curOverDayTimeBucket = this.storeTimeBucketService.getCurOverDayTimeBucket(param.getMerchantId(), param.getStoreId(), now);
        if (param.getChargeItemId() > 0) {
            storeChargeItem = this.saveStoreChargeItemService.updateStoreChargeItem(param, headImg, now, curOverDayTimeBucket);
        } else {
            Store5weiSetting store5weiSetting = this.store5weiSettingService
                    .getStore5weiSettingByStoreId(param.getMerchantId(), param.getStoreId());
            storeChargeItem = this.saveStoreChargeItemService.createStoreChargeItem(param, headImg, store5weiSetting,now, curOverDayTimeBucket);
            if (storeChargeItem.getPortId() > 0) {
                storeChargeItem.setStoreMealPort(this.storeMealPortDAO
                        .getById(param.getMerchantId(), param.getStoreId(), storeChargeItem.getPortId()));
            }
        }
        return _buildStoreChargeItemDTOForDetail(storeChargeItem, true, true, false);
    }

    @Override
    public StoreChargeItemDTO saveStoreChargeItemSimple(SaveStoreChargeItemSimpleParam param) throws T5weiException, TException {
        if (DataUtil.isNotEmpty(param.getName()) && param.getName().length() > 50) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "name[" + param.getName() + "] invalid");
        }
        StoreChargeItem storeChargeItem = this.saveStoreChargeItemService.saveStoreChargeItemSimple
                (param);
        return this._buildStoreChargeItemDTOForDetail(storeChargeItem, false, false, false);
    }

    @Override
    public void deleteStoreChargeItemPrice(int merchantId, long storeId, long itemPriceId) throws TException {
        this.storeChargeItemService.deleteStoreChargeItemPrice(merchantId, storeId, itemPriceId);
    }

    @Override
    public void deleteStoreChargeItem(int merchantId, long storeId, long chargeItemId) throws TException {
        this.storeChargeItemService.deleteStoreChargeItem(merchantId, storeId, chargeItemId);
        // add by yangfei 2016-12-07 如果被删除的收费项目开启了美团外卖开关，删除有关美团菜品信息
        StoreChargeItem storeChargeItem = this.storeChargeItemDAO.loadById(merchantId, storeId, chargeItemId, false, true);
        if(storeChargeItem.isMeituanWaimaiEnabled()){
        	this.publish(merchantId, storeId, chargeItemId);
        }
    }
    
    // add by yangfei 2016-12-07 如果被删除的收费项目开启了美团外卖开关，删除有关美团菜品信息
    public void publish(int merchantId, long storeId, long chargeItemId) {
        //SNS发布事件
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("merchantId", merchantId);
        dataMap.put("storeId", storeId);
        dataMap.put("chargeItemId", chargeItemId);
        String deleteChargeItemTopicArn = SysConfig.getDeleteChargeItemTopicArn();
        snsPublish.publish(dataMap, EventType.CHARGEITEM_DELETE, deleteChargeItemTopicArn);
    }

    @Override
    public List<StoreChargeItemDTO> getStoreChargeItemsByQueryParam(QueryStoreChargeItemListParam param) throws TException {
        List<StoreChargeItem> list;
        if (param.getProductId() > 0) {
        	list = this.storeChargeItemService.getStoreChargeItemsContainProduct(param.getMerchantId(), param.getStoreId(), param.getProductId(), param.isLoadSubitems(), param.isLoadProduct(), param.isLoadAllAvailablePrices(), param.isLoadAllItemWeeks(), param.isLoadTimeBucket(), System.currentTimeMillis());
        } else {
        	list = this.storeChargeItemService.getStoreChargeItemsByStoreId(param, System.currentTimeMillis());
        }
		// 兼容单店铺 start
		boolean isFilterMeituan = false;
		if (param.isSetFilterMeituan()) {
			isFilterMeituan = param.isFilterMeituan();
		}
		if (isFilterMeituan) {
			Iterator<StoreChargeItem> it = list.iterator();
			while (it.hasNext()) {
				StoreChargeItem chargeItem = it.next();
				if ((isFilterMeituan && chargeItem.isMeituanWaimaiEnabled())) {
					it.remove();
				}
			}
		}
		// 兼容单店铺 end
        return this._buildStoreChargeItemDTOs(param.getMerchantId(), list, false, false);
    }

    @Override
    public List<StoreChargeItemDTO> getStoreProChargeItemsByQueryParam(QueryStoreChargeItemListParam param) throws TException {
        List<StoreChargeItem> list;
        if (param.getProductId() > 0) {
            list = this.storeChargeItemService.getStoreChargeItemsContainProduct(param.getMerchantId(), param.getStoreId(), param.getProductId(), param.isLoadSubitems(), param.isLoadProduct(), param.isLoadAllAvailablePrices(), param.isLoadAllItemWeeks(), param.isLoadTimeBucket(), System.currentTimeMillis());
        } else {
            list = this.storeChargeItemService.getStoreChargeItemsByStoreId(param, System.currentTimeMillis());
        }
        List<StoreChargeItemDTO> storeChargeItemDTOs = this._buildStoreChargeItemDTOs(param.getMerchantId(), list, false, false);
        if (storeChargeItemDTOs == null || storeChargeItemDTOs.size() == 0) {
            return new ArrayList<>();
        }

        List<StoreChargeItemPromotion> storeChargeItemPromotions = storeChargeItemPromotionService.getStoreChargeItemPromotions(param.getMerchantId(), param.getStoreId());
        if (storeChargeItemPromotions == null || storeChargeItemPromotions.size() == 0) {
            return storeChargeItemDTOs;
        }
        List<StoreChargeItemDTO> storeChargeItemDTOdels = new ArrayList<>();
        for (StoreChargeItemPromotion itemPromotion : storeChargeItemPromotions) {
            for (StoreChargeItemDTO storeChargeItemDTO : storeChargeItemDTOs) {
                if (itemPromotion.getChargeItemId() == storeChargeItemDTO.getChargeItemId()) {
                    storeChargeItemDTOdels.add(storeChargeItemDTO);
                    break;
                }
            }
        }
        if (storeChargeItemDTOdels != null && storeChargeItemDTOdels.size() > 0) {
            for (StoreChargeItemDTO storeChargeItemDTOdel : storeChargeItemDTOdels) {
                storeChargeItemDTOs.remove(storeChargeItemDTOdel);
            }
        }
	    // 将参与了自动买赠活动的去掉
	    List<StorePromotionGratis> gratisList = this.storePromotionGratisService.getStorePromotionGratisListByPrivilegeWay
			    (param.getMerchantId(), param.getStoreId(), StorePromotionGratis.PROMOTION_GRATIS_4_AUTO, System.currentTimeMillis());
        if (CollectionUtils.isNotEmpty(gratisList)){
            Set<Long> chargeItemIds = Sets.newHashSet();
            for (StorePromotionGratis item : gratisList){
                if (CollectionUtils.isNotEmpty(item.getChargeItems())){
                    List<Long> ids = StorePromotionGratisChargeItem.getIds(item.getChargeItems());
                    chargeItemIds.addAll(ids);
                }
            }
            if (CollectionUtils.isNotEmpty(chargeItemIds)){
                Iterator<StoreChargeItemDTO> it = storeChargeItemDTOs.iterator();
                while (it.hasNext()){
                    StoreChargeItemDTO next = it.next();
                    if (chargeItemIds.contains(next.getChargeItemId())){
                        it.remove();
                    }
                }
            }
        }

        return storeChargeItemDTOs;
    }

    @Override
    public StoreChargeItemDTO getStoreChargeItemByChargeItemId(QueryStoreChargeItemParam param) throws TException {
        StoreChargeItem storeChargeItem = this.storeChargeItemService
                .getStoreChargeItem(param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), param.isLoadSubitems(),
                        param.isLoadProduct(), param.isLoadAllAvailablePrices(), param.isLoadAllItemWeeks(), true,
                        param.isLoadMealPort(),param.isLoadChargeItemPromotion(), System.currentTimeMillis());
        return _buildStoreChargeItemDTOForDetail(storeChargeItem, false, false, param.isLoadChargeItemPromotion());
    }

    private Map<String, StoreChargeItemPriceDTO> _buildStoreChargeItemPriceDTOMap(StoreChargeItem storeChargeItem) {
        Map<String, StoreChargeItemPriceDTO> map = Maps.newHashMap();
        if (storeChargeItem.getCurStoreChargeItemPrice() != null) {
            StoreChargeItemPriceDTO storeChargeItemPriceDTO = new StoreChargeItemPriceDTO();
            BeanUtil.copy(storeChargeItem.getCurStoreChargeItemPrice(), storeChargeItemPriceDTO);
            map.put("cur", storeChargeItemPriceDTO);
        }
        if (storeChargeItem.getNextStoreChargeItemPrice() != null) {
            StoreChargeItemPriceDTO storeChargeItemPriceDTO = new StoreChargeItemPriceDTO();
            BeanUtil.copy(storeChargeItem.getNextStoreChargeItemPrice(), storeChargeItemPriceDTO);
            map.put("next", storeChargeItemPriceDTO);
        }
        return map;
    }

    @Override
    public StoreChargeItemDTO getStoreChargeItemByName(int merchantId, long storeId, String name) throws T5weiException, TException {
        StoreChargeItem storeChargeItem = this.storeChargeItemService.getStoreChargeItemByName
                (merchantId, storeId, name);
        return this._buildStoreChargeItemDTO(storeChargeItem);
    }

    private Map<Long, StoreTimeBucket> getStoreTimeBucketMap(StoreChargeItem storeChargeItem) {
        List<Long> timeBucketIds = Lists.newArrayList();
        if (storeChargeItem.getCurStoreChargeItemWeeks() != null) {
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItem.getCurStoreChargeItemWeeks()) {
                if (timeBucketIds.contains(storeChargeItemWeek.getTimeBucketId())) {
                    continue;
                }
                timeBucketIds.add(storeChargeItemWeek.getTimeBucketId());
            }
        }
        if (storeChargeItem.getNextWeekStoreChargeItemWeeks() != null) {
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItem.getNextWeekStoreChargeItemWeeks()) {
                if (timeBucketIds.contains(storeChargeItemWeek.getTimeBucketId())) {
                    continue;
                }
                timeBucketIds.add(storeChargeItemWeek.getTimeBucketId());
            }
        }
        return this.storeTimeBucketService.getStoreTimeBucketMapInIds(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), timeBucketIds);
    }

    private List<StoreChargeItemWeekDTO> _buildStoreChargeItemWeekDTOs(List<StoreChargeItemWeek> storeChargeItemWeeks, Map<Long, StoreTimeBucket> storeTimeBucketMap) {
        List<StoreChargeItemWeekDTO> list = Lists.newArrayList();
        if (storeChargeItemWeeks != null) {
            for (StoreChargeItemWeek storeChargeItemWeek : storeChargeItemWeeks) {
                StoreChargeItemWeekDTO storeChargeItemWeekDTO = new StoreChargeItemWeekDTO();
                BeanUtil.copy(storeChargeItemWeek, storeChargeItemWeekDTO);
                if (storeChargeItemWeek.getStoreTimeBucket() == null) {
                    if (storeTimeBucketMap != null) {
                        storeChargeItemWeekDTO.setStoreTimeBucketDTO(this._buildStoreTimeBucketDTO(storeTimeBucketMap.get(storeChargeItemWeek.getTimeBucketId())));
                    }
                } else {
                    storeChargeItemWeekDTO.setStoreTimeBucketDTO(this._buildStoreTimeBucketDTO(storeChargeItemWeek.getStoreTimeBucket()));
                }
                list.add(storeChargeItemWeekDTO);
            }
        }
        return list;
    }

    private List<StoreChargeSubitemDTO> _buildStoreChargeSubitemDTOs(StoreChargeItem storeChargeItem, boolean loadProductMap) {
        List<StoreChargeSubitemDTO> storeChargeSubitemDTOs = Lists.newArrayList();
        Map<Long, StoreProduct> productMap = null;
        if (loadProductMap) {
            List<Long> productIds = Lists.newArrayList();
            if (storeChargeItem.getStoreChargeSubitems() != null) {
                for (StoreChargeSubitem storeChargeSubitem : storeChargeItem.getStoreChargeSubitems()) {
                    if (productIds.contains(storeChargeSubitem.getProductId())) {
                        continue;
                    }
                    productIds.add(storeChargeSubitem.getProductId());
                }
            }
            productMap = this.storeProductService.getStoreProductMapInIds(storeChargeItem.getMerchantId(), storeChargeItem.getStoreId(), productIds);
        }
        if (storeChargeItem.getStoreChargeSubitems() != null) {
            for (StoreChargeSubitem storeChargeSubitem : storeChargeItem.getStoreChargeSubitems()) {
                StoreChargeSubitemDTO storeChargeSubitemDTO = new StoreChargeSubitemDTO();
                BeanUtil.copy(storeChargeSubitem, storeChargeSubitemDTO);
                if (storeChargeSubitem.getStoreProduct() == null) {
                    if (loadProductMap) {
                        storeChargeSubitemDTO.setStoreProductDTO(this._buildStoreProductDTO(productMap.get(storeChargeSubitemDTO.getProductId())));
                    }
                } else {
                    storeChargeSubitemDTO.setStoreProductDTO(this._buildStoreProductDTO(storeChargeSubitem.getStoreProduct()));
                }
                storeChargeSubitemDTOs.add(storeChargeSubitemDTO);
            }
        }
        return storeChargeSubitemDTOs;
    }

    private StoreTimeBucketDTO _buildStoreTimeBucketDTO(StoreTimeBucket storeTimeBucket) {
        if (storeTimeBucket == null) {
            return null;
        }
        return this.menuFacadeUtil.buildStoreTimeBucketDTO(storeTimeBucket);
    }

    private StoreProductDTO _buildStoreProductDTO(StoreProduct storeProduct) {
        if (storeProduct == null) {
            return null;
        }
        StoreProductDTO storeProductDTO = new StoreProductDTO();
        BeanUtil.copy(storeProduct, storeProductDTO);
        return storeProductDTO;
    }

    private List<StoreChargeItemDTO> _buildStoreChargeItemDTOs(int merchantId,
                                                               List<StoreChargeItem> storeChargeItems,
                                                               boolean loadProductMap,
                                                               boolean loadTimeBucketMap) throws TException {
        List<StoreMealPort> storeMealPorts = Lists.newArrayList();
        storeMealPorts.addAll(storeChargeItems.stream().filter(storeChargeItem -> storeChargeItem.getStoreMealPort() != null).map(StoreChargeItem::getStoreMealPort).collect(Collectors.toList()));
        Map<Long, StoreMealPortDTO> portDTOMap = null;
        if (!storeMealPorts.isEmpty()) {
            portDTOMap = this.storeMealPortFacadeUtil
                    .buildStoreMealPortDTOMap(merchantId, storeMealPorts);
        }
        List<StoreChargeItemDTO> storeChargeItemDTOs = Lists.newArrayList();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            StoreChargeItemDTO storeChargeItemDTO = this
                    ._buildStoreChargeItemDTOForDetail(storeChargeItem, portDTOMap, loadProductMap, loadTimeBucketMap);
            storeChargeItemDTOs.add(storeChargeItemDTO);
        }
        return storeChargeItemDTOs;
    }

    private StoreChargeItemDTO _buildStoreChargeItemDTO(StoreChargeItem
                                                                storeChargeItem) throws TException {
        List<StoreMealPort> storeMealPorts = Lists.newArrayList();
        Map<Long, StoreMealPortDTO> portDTOMap = null;
        if (storeChargeItem.getStoreMealPort() != null) {
            storeMealPorts.add(storeChargeItem.getStoreMealPort());
            portDTOMap = this.storeMealPortFacadeUtil
                    .buildStoreMealPortDTOMap(storeChargeItem.getMerchantId(),
                            storeMealPorts);
        }
        return this._buildStoreChargeItemDTO(storeChargeItem, portDTOMap);
    }

    private StoreChargeItemDTO _buildStoreChargeItemDTO(StoreChargeItem
                                                                storeChargeItem,
                                                        Map<Long, StoreMealPortDTO> portDTOMap) throws TException {
        StoreChargeItemDTO storeChargeItemDTO = new StoreChargeItemDTO();
        BeanUtil.copy(storeChargeItem, storeChargeItemDTO);
        if (storeChargeItem.getStoreMealPort() != null) {
            if (portDTOMap != null) {
                storeChargeItemDTO.setStoreMealPortDTO(portDTOMap.get
                        (storeChargeItemDTO.getPortId()));
            }
        }
        return storeChargeItemDTO;
    }

    private StoreChargeItemDTO _buildStoreChargeItemDTOForDetail(StoreChargeItem storeChargeItem, boolean loadProductMap, boolean loadTimeBucketMap,boolean loadPromotion) throws TException {
        StoreChargeItemDTO storeChargeItemDTO = this._buildStoreChargeItemDTO(storeChargeItem);
        storeChargeItemDTO.setStoreChargeSubitemDTOs(this._buildStoreChargeSubitemDTOs(storeChargeItem, loadProductMap));
        Map<Long, StoreTimeBucket> storeTimeBucketMap = null;
        if (loadTimeBucketMap) {
            storeTimeBucketMap = this.getStoreTimeBucketMap(storeChargeItem);
        }
        storeChargeItemDTO.setCurStoreChargeItemWeekDTOs(this._buildStoreChargeItemWeekDTOs(storeChargeItem.getCurStoreChargeItemWeeks(), storeTimeBucketMap));
        storeChargeItemDTO.setNextWeekStoreChargeItemWeekDTOs(this._buildStoreChargeItemWeekDTOs(storeChargeItem.getNextWeekStoreChargeItemWeeks(), storeTimeBucketMap));
        Map<String, StoreChargeItemPriceDTO> priceDTOMap = this._buildStoreChargeItemPriceDTOMap(storeChargeItem);
        storeChargeItemDTO.setCurStoreChargeItemPriceDTO(priceDTOMap.get("cur"));
        storeChargeItemDTO.setNextStoreChargeItemPriceDTO(priceDTOMap.get("next"));
        // 加载首份特价信息
        if (loadPromotion) {
            StoreChargeItemPromotion chargeItemPromotion = storeChargeItem.getStoreChargeItemPromotion();
            if (chargeItemPromotion != null) {
                StoreChargeItemPromotionDTO chargeItemPromotionDTO = BeanUtil.copy(chargeItemPromotion, StoreChargeItemPromotionDTO.class);
                storeChargeItemDTO.setStoreChargeItemPromotionDTO(chargeItemPromotionDTO);
            }
        }
        return storeChargeItemDTO;
    }

    private StoreChargeItemDTO _buildStoreChargeItemDTOForDetail(StoreChargeItem storeChargeItem, Map<Long, StoreMealPortDTO> portDTOMap, boolean loadProductMap, boolean loadTimeBucketMap) throws TException {
        StoreChargeItemDTO storeChargeItemDTO = this._buildStoreChargeItemDTO
                (storeChargeItem, portDTOMap);
        storeChargeItemDTO.setStoreChargeSubitemDTOs(this._buildStoreChargeSubitemDTOs(storeChargeItem, loadProductMap));
        Map<Long, StoreTimeBucket> storeTimeBucketMap = null;
        if (loadTimeBucketMap) {
            storeTimeBucketMap = this.getStoreTimeBucketMap(storeChargeItem);
        }
        storeChargeItemDTO.setCurStoreChargeItemWeekDTOs(this._buildStoreChargeItemWeekDTOs(storeChargeItem.getCurStoreChargeItemWeeks(), storeTimeBucketMap));
        storeChargeItemDTO.setNextWeekStoreChargeItemWeekDTOs(this._buildStoreChargeItemWeekDTOs(storeChargeItem.getNextWeekStoreChargeItemWeeks(), storeTimeBucketMap));
        Map<String, StoreChargeItemPriceDTO> priceDTOMap = this._buildStoreChargeItemPriceDTOMap(storeChargeItem);
        storeChargeItemDTO.setCurStoreChargeItemPriceDTO(priceDTOMap.get("cur"));
        storeChargeItemDTO.setNextStoreChargeItemPriceDTO(priceDTOMap.get("next"));
        return storeChargeItemDTO;
    }

    @Override
    public StoreChargeItemPromotionDTO saveStoreChargeItemPromotion(StoreChargeItemPromotionParam param) throws T5weiException, TException {
        if (param == null) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "param can not null");
        }
        storeChargeItemPromotionService.saveStoreChargeItemPromotion(param);
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long chargeItemId = param.getChargeItemId();
        return this.getStoreChargeItemPromotion(merchantId, storeId, chargeItemId);
    }

    @Override
    public void deleteStoreChargeItemPromotion(int merchantId, long storeId, long chargeItemId) throws T5weiException, TException {
        storeChargeItemPromotionService.deleteStoreChargeItemPromotion(merchantId, storeId, chargeItemId);
    }

    @Override
    public StoreChargeItemPromotionDTO getStoreChargeItemPromotion(int merchantId, long storeId, long chargeItemId) throws T5weiException, TException {
        StoreChargeItemPromotion storeChargeItemPromotion = storeChargeItemPromotionService.getStoreChargeItemPromotion(merchantId, storeId, chargeItemId);
        if (storeChargeItemPromotion == null) {
            return new StoreChargeItemPromotionDTO();
        }
        StoreChargeItemPromotionStatResult promotionStat = storeChargeItemPromotionService.getStoreChargeItemPromotionStat(merchantId, storeId, chargeItemId);
        StoreChargeItemPromotionDTO itemPromotionDTO = BeanUtil.copy(storeChargeItemPromotion, StoreChargeItemPromotionDTO.class);
        BeanUtil.copy(promotionStat, itemPromotionDTO);
        return itemPromotionDTO;
    }

    @Override
    public StoreChargeItemPromotionPageDTO getStoreChargeItemPromotions(StoreChargeItemPromotionQueryParam Param) throws T5weiException, TException {
        int countChargeItemPromotion = storeChargeItemPromotionService.countStoreChargeItemPromotions(Param);
        StoreChargeItemPromotionPageDTO pageDTO = new StoreChargeItemPromotionPageDTO();
        pageDTO.setTotal(countChargeItemPromotion);
        pageDTO.setPageNum(PageUtil.getPageNum(countChargeItemPromotion, Param.getSize()));
        pageDTO.setSize(Param.getSize());
        pageDTO.setPageNo(Param.getPageNo());
        if (countChargeItemPromotion <= 0) {
            return pageDTO;
        }
        List<StoreChargeItemPromotion> storeChargeItemPromotions = storeChargeItemPromotionService.getStoreChargeItemPromotions(Param);
        List<Long> chargeItemIds = new ArrayList<Long>();
        for (StoreChargeItemPromotion storeChargeItemPromotion : storeChargeItemPromotions) {
            chargeItemIds.add(storeChargeItemPromotion.getChargeItemId());
        }
        int merchantId = Param.getMerchantId();
        long storeId = Param.getStoreId();
        List<StoreChargeItem> storeChargeItems = storeChargeItemService.getStoreChargeItemsInIds(merchantId, storeId, chargeItemIds, System.currentTimeMillis());
        Map<Long, StoreChargeItem> storeChargeItemMap = new HashMap<>();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            storeChargeItemMap.put(storeChargeItem.getChargeItemId(), storeChargeItem);
        }
        List<StoreChargeItemPromotionDTO> storeChargeItemPromotionDTOs = new ArrayList<>();
        for (StoreChargeItemPromotion storeChargeItemPromotion : storeChargeItemPromotions) {
            StoreChargeItemPromotionDTO itemPromotionDTO = BeanUtil.copy(storeChargeItemPromotion, StoreChargeItemPromotionDTO.class);
            StoreChargeItem storeChargeItem = storeChargeItemMap.get(storeChargeItemPromotion.getChargeItemId());
            if (storeChargeItem == null) {
                continue;
            }
            boolean chargeItemDelete = storeChargeItem.isDeleted();
            long price = storeChargeItem.getCurPrice();
            String chargeItemName = storeChargeItem.getName();
            itemPromotionDTO.setChargeItemName(chargeItemName);
            itemPromotionDTO.setPrice(price);
            itemPromotionDTO.setChargeItemDelete(chargeItemDelete);
            storeChargeItemPromotionDTOs.add(itemPromotionDTO);
        }
        pageDTO.setDataList(storeChargeItemPromotionDTOs);
        return pageDTO;
    }

    @Override
    public List<StoreChargeItemDTO> updateStoreChargeItemsCategory(int merchantId, long storeId, int categoryId, List<Long> chargeItemIds) throws T5weiException, TException {

        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }
        if (categoryId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.ARGUMENT_INVALID.getValue(), "categoryId[" + categoryId + "] must > 0");
        }

        List<StoreChargeItem> storeChargeItems = storeChargeItemService.updateStoreChargeItemsCategory(merchantId, storeId, categoryId, chargeItemIds);

        List<StoreChargeItemDTO> storeChargeItemDTOs = Lists.newArrayList();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            StoreChargeItemDTO storeChargeItemDTO = BeanUtil.copy(storeChargeItem, StoreChargeItemDTO.class);
            storeChargeItemDTOs.add(storeChargeItemDTO);
        }

        return storeChargeItemDTOs;
    }

    @Deprecated
    @Override
    public StoreChargeItemDTO updateStoreChargeItemCustomerTraffic(int merchantId, long storeId, long chargeItemId, boolean enableCustomerTraffic) throws T5weiException, TException {

        return null;
    }

    @Deprecated
    @Override
    public List<StoreChargeItemDTO> getStoreChargeItemsByCustomerTraffic(int merchantId, long storeId, int customerTrafficSelectMode) throws TException {

        return null;
    }

    @Override
    public List<StoreChargeItemWithCategoryDTO> getStoreChargeItemsWithCategory(int merchantId, long storeId) throws TException {

        //validate
        if (merchantId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_MERCHANT_ID_CAN_NOT_NULL.getValue(), "merchantId[" + merchantId + "] invalid");
        }
        if (storeId <= 0) {
            throw new T5weiException(T5weiErrorCodeType.STORE_ID_CAN_NOT_NULL.getValue(), "storeId[" + storeId + "] invalid");
        }

        List<StoreChargeItemWithCategoryDTO> dtos = storeChargeItemService.getStoreChargeItemWithCategory(merchantId, storeId);
        return dtos;
    }

    @Override
    @Deprecated
    public List<StoreChargeItemWithProductDTO> getStoreChargeItemsWithProduct(int merchantId, long storeId) throws TException {

        return storeChargeItemService.getStoreChargeItemsWithProduct(merchantId, storeId);
    }

    @Override
    @Deprecated
    public List<StoreChargeItemWithProductDTO> getProductsByChargeItemIds(int merchantId, long storeId, List<Long> chargeItemIds) throws TException {
        return storeChargeItemService.getStoreChargeItemsWithProduct(merchantId, storeId, chargeItemIds);
    }
	
    @Override
    public boolean hasEnableSameTakeMode(int merchantId, long storeId, List<Long> chargeItemIds, int takeMode) throws T5weiException, TException {
        return storeChargeItemService.hasEnableSameTakeMode(merchantId, storeId, chargeItemIds, takeMode);
    }
	
    @Override
    public void batchUpdateChargeItemMeituanEnable(int merchantId, long storeId, List<Long> chargeItemIds, boolean enable) throws T5weiException, TException {
        storeChargeItemService.batchUpdateChargeItemMeituanEnable(merchantId, storeId, chargeItemIds, enable);
    }
	
	@Override
	public List<StoreChargeItemPromotionDTO> getStoreChargeItemPromotions4NotEnd(int merchantId, long storeId) throws TException {
		List<StoreChargeItemPromotionDTO> dto = Lists.newArrayList();
		long time = System.currentTimeMillis();
		List<StoreChargeItemPromotion> chargeItemPromotionList = this.storeChargeItemPromotionService.getList4NotEnd(merchantId, storeId, time);
		StoreChargeItemPromotionDTO promotionDTO;
		for (StoreChargeItemPromotion item : chargeItemPromotionList){
			promotionDTO = BeanUtil.copy(item,StoreChargeItemPromotionDTO.class);
			dto.add(promotionDTO);
		}
		return dto;
	}
	
}
