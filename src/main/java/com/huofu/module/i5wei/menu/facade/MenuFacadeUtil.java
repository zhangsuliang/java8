package com.huofu.module.i5wei.menu.facade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huofu.module.i5wei.inventory.entity.StoreInventoryDate;
import com.huofu.module.i5wei.menu.entity.StoreChargeItem;
import com.huofu.module.i5wei.menu.entity.StoreChargeItemPromotion;
import com.huofu.module.i5wei.menu.entity.StoreChargeSubitem;
import com.huofu.module.i5wei.menu.entity.StoreMenuDisplay;
import com.huofu.module.i5wei.menu.entity.StoreMenuDisplayCat;
import com.huofu.module.i5wei.menu.entity.StoreProduct;
import com.huofu.module.i5wei.menu.entity.StoreTimeBucket;
import com.huofu.module.i5wei.menu.entity.StoreTvMenu;
import com.huofu.module.i5wei.menu.service.StoreTimeTvMenu;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratisPeriod;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebateChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebatePeriod;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceChargeItem;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReducePeriod;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduceQuota;
import com.huofu.module.i5wei.promotion.service.StorePromotionGratisService;
import com.huofu.module.i5wei.promotion.service.StorePromotionHelper;
import com.huofu.module.i5wei.promotion.service.StorePromotionRebateService;
import com.huofu.module.i5wei.promotion.service.StorePromotionReduceService;
import com.huofu.module.i5wei.queue.I5weiMessageProducer;

import huofucore.facade.config.client.ClientTypeEnum;
import huofucore.facade.dialog.resourcevisit.ResourceVisitParam;
import huofucore.facade.dialog.resourcevisit.UserResourceVisitDTO;
import huofucore.facade.dialog.resourcevisit.UserResourceVisitFacade;
import huofucore.facade.dialog.resourcevisit.UserResourceVisitType;
import huofucore.facade.dialog.visit.StoreUserVisitFacade;
import huofucore.facade.dialog.visit.UserVisitType;
import huofucore.facade.i5wei.menu.StoreChargeItemDTO;
import huofucore.facade.i5wei.menu.StoreChargeItemPriceDTO;
import huofucore.facade.i5wei.menu.StoreChargeItemPromotionDTO;
import huofucore.facade.i5wei.menu.StoreChargeSubitemDTO;
import huofucore.facade.i5wei.menu.StoreMenuDisplayCatDTO;
import huofucore.facade.i5wei.menu.StoreMenuDisplayDTO;
import huofucore.facade.i5wei.menu.StoreMenuPriceType;
import huofucore.facade.i5wei.menu.StoreProductDTO;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO;
import huofucore.facade.i5wei.menu.StoreTimeBucketDTO2;
import huofucore.facade.i5wei.menu.StoreTimeBucketMenuDisplayDTO;
import huofucore.facade.i5wei.menu.StoreTimeBucketMenuDisplayParam;
import huofucore.facade.i5wei.menu.StoreTimeTvMenuDTO;
import huofucore.facade.i5wei.menu.StoreTvMenuDTO;
import huofucore.facade.i5wei.order.StoreOrderTakeModeEnum;
import huofucore.facade.i5wei.promotion.StorePromotionChargeItemDTO;
import huofucore.facade.i5wei.promotion.StorePromotionGratisDTO;
import huofucore.facade.i5wei.promotion.StorePromotionPeriodDTO;
import huofucore.facade.i5wei.promotion.StorePromotionRebateDTO;
import huofucore.facade.i5wei.promotion.StorePromotionReduceDTO;
import huofucore.facade.i5wei.promotion.StorePromotionReduceQuotaDTO;
import huofucore.facade.i5wei.sharedto.I5weiStaffDTO;
import huofucore.facade.merchant.preferential.MerchantInternetRebateTypeEnum;
import huofucore.facade.merchant.preferential.MerchantPreferentialFacade;
import huofucore.facade.merchant.preferential.MerchantPreferentialOfUserDTO;
import huofucore.facade.merchant.staff.StaffDTO2;
import huofuhelper.util.MoneyUtil;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftClient;

/**
 * 菜单facade相关数据服务
 * Created by akwei on 8/30/15.
 */
@Component
public class MenuFacadeUtil {

    @ThriftClient
    private MerchantPreferentialFacade.Iface merchantPreferentialFacade;

    @ThriftClient
    private UserResourceVisitFacade.Iface userResourceVisitFacade;

    @Resource
    private StorePromotionRebateService storePromotionRebateService;

    @Resource
    private StorePromotionReduceService storePromotionReduceService;

    @ThriftClient
    private StoreUserVisitFacade.Iface storeUserVisitFacade;

    @Resource
    private I5weiMessageProducer i5weiMessageProducer;

    @Resource
    private StorePromotionGratisService storePromotionGratisService;

    StoreTimeBucketMenuDisplayDTO buildStoreTimeBucketMenuDisplayDTO(List<StoreMenuDisplayCat> storeMenuDisplayCats, List<StoreMenuDisplay> storeMenuDisplays, Map<Long, StoreChargeItem> storeChargeItemMap, StoreTimeBucket storeTimeBucket) {
        StoreTimeBucketMenuDisplayDTO storeTimeBucketMenuDisplayDTO = new StoreTimeBucketMenuDisplayDTO();

        StoreTimeBucketDTO storeTimeBucketDTO = this.buildStoreTimeBucketDTO(storeTimeBucket);
        long now = System.currentTimeMillis();
        storeTimeBucketDTO.setInBizTime(storeTimeBucket.isInTime(now));
        storeTimeBucketMenuDisplayDTO.setStoreTimeBucketDTO(storeTimeBucketDTO);


        //sort cat
        Collections.sort(storeMenuDisplayCats, (o1, o2) -> {
            if (o1.getDisplayCatId() > o2.getDisplayCatId()) {
                return 1;
            }
            if (o1.getDisplayCatId() < o2.getDisplayCatId()) {
                return -1;
            }
            return 0;
        });
        storeTimeBucketMenuDisplayDTO.setStoreMenuDisplayCatDTOs(BeanUtil.copyList(storeMenuDisplayCats, StoreMenuDisplayCatDTO.class));

        //group by cat
        Map<Long, List<StoreMenuDisplay>> storeMenuDisplaysMap = Maps.newHashMap();
        for (StoreMenuDisplay storeMenuDisplay : storeMenuDisplays) {
            List<StoreMenuDisplay> list = storeMenuDisplaysMap.get(storeMenuDisplay.getDisplayCatId());
            if (list == null) {
                list = Lists.newArrayList();
                storeMenuDisplaysMap.put(storeMenuDisplay.getDisplayCatId(), list);
            }
            list.add(storeMenuDisplay);
        }

        //sort by sortFlag in cat
        Collection<List<StoreMenuDisplay>> values = storeMenuDisplaysMap.values();
        for (List<StoreMenuDisplay> list : values) {
            Collections.sort(list, (o1, o2) -> {
                if (o1.getSortFlag() > o2.getSortFlag()) {
                    return 1;
                }
                if (o1.getSortFlag() < o2.getSortFlag()) {
                    return -1;
                }
                return 0;
            });
        }

        Set<Map.Entry<Long, List<StoreMenuDisplay>>> set = storeMenuDisplaysMap.entrySet();
        Map<Long, List<StoreChargeItemDTO>> storeChargeItemDTOsMap = Maps.newHashMap();
        for (Map.Entry<Long, List<StoreMenuDisplay>> e : set) {
            List<StoreMenuDisplay> list = e.getValue();
            List<StoreChargeItemDTO> storeChargeItemDTOs = Lists.newArrayList();
            for (StoreMenuDisplay storeMenuDisplay : list) {
                StoreChargeItem storeChargeItem = storeChargeItemMap.get(storeMenuDisplay.getChargeItemId());
                if (storeChargeItem != null) {
                    storeChargeItemDTOs.add(buildStoreChargeItemDTO(storeChargeItem));
                }
            }
            storeChargeItemDTOsMap.put(e.getKey(), storeChargeItemDTOs);
        }
        storeTimeBucketMenuDisplayDTO.setStoreChargeItemDTOsMap(storeChargeItemDTOsMap);

        //process unsorted 存储未进行排序的数据map
        Map<Long, StoreChargeItem> _storeChargeItemMap = Maps.newHashMap();
        _storeChargeItemMap.putAll(storeChargeItemMap);
        //排除所有已经进行排序的，包括(其他:catId=0的)
        for (StoreMenuDisplay storeMenuDisplay : storeMenuDisplays) {
            _storeChargeItemMap.remove(storeMenuDisplay.getChargeItemId());
        }

        //存储未进行排序的数据list
        List<StoreChargeItem> unSortedStoreChargeItems = Lists.newArrayList();
        unSortedStoreChargeItems.addAll(_storeChargeItemMap.values());

        //sorted for unsorted，对未排序的数据按照id排序
        Collections.sort(unSortedStoreChargeItems, (o1, o2) -> {
            if (o1.getChargeItemId() > o2.getChargeItemId()) {
                return 1;
            }
            if (o1.getChargeItemId() < o2.getChargeItemId()) {
                return -1;
            }
            return 0;
        });

        List<StoreChargeItemDTO> unSortedStoreChargeItemDTOs = Lists.newArrayList();
        for (StoreChargeItem storeChargeItem : unSortedStoreChargeItems) {
            unSortedStoreChargeItemDTOs.add(buildStoreChargeItemDTO(storeChargeItem));
        }

        List<StoreChargeItemDTO> chargeItemDTOs = storeChargeItemDTOsMap.get(0L);
        if (chargeItemDTOs == null) {
            chargeItemDTOs = Lists.newArrayList();
            storeChargeItemDTOsMap.put(0L, chargeItemDTOs);
        }
        chargeItemDTOs.addAll(unSortedStoreChargeItemDTOs);
        return storeTimeBucketMenuDisplayDTO;
    }

    private StoreChargeItemDTO buildStoreChargeItemDTO(StoreChargeItem storeChargeItem) {
        StoreChargeItemDTO storeChargeItemDTO = new StoreChargeItemDTO();
        BeanUtil.copy(storeChargeItem, storeChargeItemDTO);
        Map<String, StoreChargeItemPriceDTO> priceDTOMap = _buildStoreChargeItemPriceDTOMap(storeChargeItem);
        storeChargeItemDTO.setCurStoreChargeItemPriceDTO(priceDTOMap.get("cur"));
        storeChargeItemDTO.setNextStoreChargeItemPriceDTO(priceDTOMap.get("next"));
        storeChargeItemDTO.setStoreChargeSubitemDTOs(buildStoreChargeSubitemDTOs(storeChargeItem.getStoreChargeSubitems()));
        if (storeChargeItem.getStoreChargeItemPromotion() != null) {
            storeChargeItemDTO.setStoreChargeItemPromotionDTO(this.buildStoreChargeItemPromotionDTO(storeChargeItem.
                    getStoreChargeItemPromotion()));
        }
        return storeChargeItemDTO;
    }

    private StoreChargeItemPromotionDTO buildStoreChargeItemPromotionDTO(StoreChargeItemPromotion promotion) {
        StoreChargeItemPromotionDTO dto = new StoreChargeItemPromotionDTO();
        BeanUtil.copy(promotion, dto);
        return dto;
    }

    private Map<String, StoreChargeItemPriceDTO>
    _buildStoreChargeItemPriceDTOMap(StoreChargeItem storeChargeItem) {
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


    private List<StoreChargeSubitemDTO> buildStoreChargeSubitemDTOs
            (List<StoreChargeSubitem> storeChargeSubitems) {
        if (storeChargeSubitems == null) {
            return null;
        }
        List<StoreChargeSubitemDTO> storeChargeSubitemDTOs = Lists.newArrayList();
        for (StoreChargeSubitem subitem : storeChargeSubitems) {
            StoreChargeSubitemDTO storeChargeSubitemDTO = new StoreChargeSubitemDTO();
            BeanUtil.copy(subitem, storeChargeSubitemDTO);
            storeChargeSubitemDTO.setStoreProductDTO(buildStoreProductDTO(subitem.getStoreProduct()));
            storeChargeSubitemDTOs.add(storeChargeSubitemDTO);
        }
        return storeChargeSubitemDTOs;
    }

    private StoreProductDTO buildStoreProductDTO(StoreProduct storeProduct) {
        if (storeProduct == null) {
            return null;
        }
        StoreProductDTO storeProductDTO = new StoreProductDTO();
        BeanUtil.copy(storeProduct, storeProductDTO);
        return storeProductDTO;
    }

    /**
     * 组装用户网单折扣价格，对企业折扣、会员价格比较
     */
    private Map<Long, Long> buildUserDatePrice(Collection<StoreChargeItem> storeChargeItems, int merchantId, long storeId, long userId, ClientTypeEnum clientTypeEnum, Map<Long, StoreMenuPriceType> menuPriceTypeMap) {
        double rebate = 100;
        StoreMenuPriceType storeMenuPriceType4EntOrInternet = StoreMenuPriceType.ORIGIN;
        boolean enableMemberPrice = false;
        if (userId > 0) {
            try {
                MerchantPreferentialOfUserDTO merchantPreferentialOfUserDTO = this.merchantPreferentialFacade.getMerchantPreferentialInfoOfUser(userId, merchantId, storeId, clientTypeEnum.getValue());
                if (merchantPreferentialOfUserDTO.getInternetRebateType() == MerchantInternetRebateTypeEnum.MEMBER_PRICE.getValue()) {
                    enableMemberPrice = true;
                    if (merchantPreferentialOfUserDTO.getEnterpriseRebate() < 100) {
                        storeMenuPriceType4EntOrInternet = StoreMenuPriceType.ENT;
                        rebate = merchantPreferentialOfUserDTO.getEnterpriseRebate();
                    }
                } else if (merchantPreferentialOfUserDTO.getInternetRebateType() == MerchantInternetRebateTypeEnum.REBATE.getValue()) {
                    if (merchantPreferentialOfUserDTO.getEnterpriseRebate() >= merchantPreferentialOfUserDTO.getInternetRebate()) {
                        storeMenuPriceType4EntOrInternet = StoreMenuPriceType.INTERNET;
                        rebate = merchantPreferentialOfUserDTO.getInternetRebate();
                    } else {
                        storeMenuPriceType4EntOrInternet = StoreMenuPriceType.ENT;
                        rebate = merchantPreferentialOfUserDTO.getEnterpriseRebate();
                    }
                } else {
                    if (merchantPreferentialOfUserDTO.getEnterpriseRebate() < 100) {
                        storeMenuPriceType4EntOrInternet = StoreMenuPriceType.ENT;
                        rebate = merchantPreferentialOfUserDTO.getEnterpriseRebate();
                    }
                }
            } catch (TException ignored) {
            }
        }
        Map<Long, Long> priceMap = Maps.newHashMap();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            if (storeChargeItem.getCurStoreChargeItemPrice() != null) {
                long realPrice;
                if (storeChargeItem.isEnableRebate()) {
                    realPrice = MoneyUtil.getRebatePrice(rebate, 0, storeChargeItem.getCurPrice());
                    if (realPrice <= storeChargeItem.getCurPrice()) {
                        if (enableMemberPrice && storeChargeItem.getMemberPrice() > 0 && storeChargeItem.getMemberPrice() < realPrice) {
                            realPrice = storeChargeItem.getMemberPrice();
                            menuPriceTypeMap.put(storeChargeItem.getChargeItemId(), StoreMenuPriceType.MEMBER);
                        } else {
                            menuPriceTypeMap.put(storeChargeItem.getChargeItemId(), storeMenuPriceType4EntOrInternet);
                        }
                    } else {
                        menuPriceTypeMap.put(storeChargeItem.getChargeItemId(), StoreMenuPriceType.ORIGIN);
                    }
                } else {
                    realPrice = storeChargeItem.getCurPrice();
                    if (enableMemberPrice && storeChargeItem.getMemberPrice() > 0 && storeChargeItem.getMemberPrice() < realPrice) {
                        realPrice = storeChargeItem.getMemberPrice();
                        menuPriceTypeMap.put(storeChargeItem.getChargeItemId(), StoreMenuPriceType.MEMBER);
                    } else {
                        menuPriceTypeMap.put(storeChargeItem.getChargeItemId(), StoreMenuPriceType.ORIGIN);
                    }
                }
                priceMap.put(storeChargeItem.getChargeItemId(), realPrice);
            } else {
                //当价格数据不存在时进行高价展示，避免0价格购买
                priceMap.put(storeChargeItem.getChargeItemId(), 9999999L);
                menuPriceTypeMap.put(storeChargeItem.getChargeItemId(), StoreMenuPriceType.ORIGIN);
            }
        }
        return priceMap;
    }

    /**
     * 组装价格信息
     *
     * @param storeChargeItems        收费项目list
     * @param userRebateMap           用户折扣价格
     * @param storePromotionRebateMap 收费项目参加的活动
     * @param menuPriceTypeMap        价格类型map
     * @return 价格信息
     */
    private Collection<ChargeItemRebatePrice> matchMinPrice(Collection<StoreChargeItem> storeChargeItems, Map<Long, Long> userRebateMap, Map<Long, StorePromotionRebate> storePromotionRebateMap, Map<Long, StoreMenuPriceType> menuPriceTypeMap) {
        List<ChargeItemRebatePrice> list = Lists.newArrayList();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            long chargeItemId = storeChargeItem.getChargeItemId();
            long userPrice = userRebateMap.get(chargeItemId);
            ChargeItemRebatePrice obj = new ChargeItemRebatePrice();
            list.add(obj);
            obj.setStoreChargeItem(storeChargeItem);
            StorePromotionRebate storePromotionRebate = storePromotionRebateMap.get(chargeItemId);
            if (storePromotionRebate != null) {
                long rebatePrice = storePromotionRebate.getRebatePrice(storeChargeItem.getCurPrice());
                if (userPrice <= rebatePrice) {
                    obj.setPrice(userPrice);
                } else {
                    obj.setPrice(rebatePrice);
                    obj.setStorePromotionRebate(storePromotionRebate);
                    if (menuPriceTypeMap != null) {
                        menuPriceTypeMap.put(chargeItemId, StoreMenuPriceType.REBATE);
                    }
                }
            } else {
                obj.setPrice(userPrice);
            }
        }
        return list;
    }

    void buildPriceInfoAndPromotionInfoAndSendVisitInfo(StoreTimeBucketMenuDisplayParam param, ClientTypeEnum clientTypeEnum,
                                                        Collection<StoreChargeItem> storeChargeItems,
                                                        long timeBucketId, long time,
                                                        StoreTimeBucketMenuDisplayDTO storeTimeBucketMenuDisplayDTO
    ) throws TException {
        int merchantId = param.getMerchantId();
        long storeId = param.getStoreId();
        long userId = param.getUserId();
        Map<Long, StoreMenuPriceType> menuPriceTypeMap = Maps.newHashMap();
        Map<Long, StoreMenuPriceType> menuPriceTypeMap4TakeOut = null;
        storeTimeBucketMenuDisplayDTO.setMenuPriceTypeMap(menuPriceTypeMap);
        //计算企业折扣后价格
        Map<Long, Long> userRebatePriceMap = buildUserDatePrice(storeChargeItems, merchantId, storeId, userId, clientTypeEnum, menuPriceTypeMap);
        if (!StorePromotionHelper.isWechatVisist(clientTypeEnum.getValue())) {
            menuPriceTypeMap4TakeOut = Maps.newHashMap();
            menuPriceTypeMap4TakeOut.putAll(menuPriceTypeMap);
        }
        List<StorePromotionRebate> storePromotionRebates4Match = this.storePromotionRebateService.getStorePromotionRebates4Match(merchantId, storeId, time);
        List<StorePromotionReduce> storePromotionReduces4Match = this.storePromotionReduceService.getStorePromotionReduces4Match(merchantId, storeId, time);
        List<StorePromotionGratis> storePromotionGratises4Match = this.storePromotionGratisService.getStorePromotionGratisList4Match(merchantId, storeId, time);
        if (storePromotionRebates4Match.isEmpty() && storePromotionReduces4Match.isEmpty() && storePromotionGratises4Match.isEmpty()) {
            //没有活动时，只需要组装价格数据
            storeTimeBucketMenuDisplayDTO.setChargeItemCurPriceMap(userRebatePriceMap);
            return;
        }

        int takeMode4Rebate = StoreOrderTakeModeEnum.DINE_IN.getValue();
        if (param.isForDelivery()) {
            takeMode4Rebate = StoreOrderTakeModeEnum.SEND_OUT.getValue();
        } else if (param.isForEatIn()) {
            takeMode4Rebate = StoreOrderTakeModeEnum.DINE_IN.getValue();
        } else if (param.isForUserTake()) {
            takeMode4Rebate = StoreOrderTakeModeEnum.TAKE_OUT.getValue();
        }
        List<StorePromotionRebate> storePromotionRebates = null;
        List<StorePromotionReduce> storePromotionReduces = null;
        List<StorePromotionGratis> storePromotionGratises = null;
        if (storePromotionRebates4Match.isEmpty()) {
            storeTimeBucketMenuDisplayDTO.setChargeItemCurPriceMap(userRebatePriceMap);
        } else {
            Set<Long> rebateIdSet = Sets.newHashSet();
            Map<Long, StorePromotionRebate> storePromotionRebateMap = this.storePromotionRebateService.getBestStorePromotionRebateMap(timeBucketId, time, clientTypeEnum.getValue(), takeMode4Rebate, storeChargeItems, storePromotionRebates4Match);
            Collection<ChargeItemRebatePrice> chargeItemRebatePrices = this.matchMinPrice(storeChargeItems, userRebatePriceMap, storePromotionRebateMap, menuPriceTypeMap);
            Map<Long, Long> matchRebateMap = this.matchRebateMap(chargeItemRebatePrices);
            rebateIdSet.addAll(matchRebateMap.values());
            storeTimeBucketMenuDisplayDTO.setChargeItemPromotionRebateIdMap(matchRebateMap);
            storeTimeBucketMenuDisplayDTO.setChargeItemCurPriceMap(this.buildPriceMap(chargeItemRebatePrices));

            //收银台访问，再次获取打包的活动信息。默认收银台不会指定打包条件
            if (!StorePromotionHelper.isWechatVisist(clientTypeEnum.getValue())) {
                Map<Long, StorePromotionRebate> storePromotionRebateMap4TakeOut = this.storePromotionRebateService.getBestStorePromotionRebateMap(timeBucketId, time, clientTypeEnum.getValue(), StoreOrderTakeModeEnum.TAKE_OUT.getValue(), storeChargeItems, storePromotionRebates4Match);
                Collection<ChargeItemRebatePrice> chargeItemRebatePrices4TakeOut = this.matchMinPrice(storeChargeItems, userRebatePriceMap, storePromotionRebateMap4TakeOut, menuPriceTypeMap4TakeOut);
                Map<Long, Long> matchRebateMap4TakeOut = this.matchRebateMap(chargeItemRebatePrices4TakeOut);
                if (!matchRebateMap4TakeOut.isEmpty()) {
                    storeTimeBucketMenuDisplayDTO.setChargeItemPromotionRebateIdMap4TakeOut(matchRebateMap4TakeOut);
                    rebateIdSet.addAll(matchRebateMap4TakeOut.values());
                    storeTimeBucketMenuDisplayDTO.setChargeItemCurPriceMap4TakeOut(this.buildPriceMap(chargeItemRebatePrices4TakeOut));
                }
            }

            //获得所有可使用的活动
            storePromotionRebates = Lists.newArrayList();
            for (StorePromotionRebate storePromotionRebate : storePromotionRebates4Match) {
                if (rebateIdSet.contains(storePromotionRebate.getPromotionRebateId())) {
                    storePromotionRebates.add(storePromotionRebate);
                }
            }
            storeTimeBucketMenuDisplayDTO.setStorePromotionRebateDTOs(this.buildStorePromotionRebateDTOs(storePromotionRebates, false));
        }

        if (!storePromotionReduces4Match.isEmpty()) {
            StorePromotionReduce bestStorePromotionReduce = this.storePromotionReduceService.getBestStorePromotionReduce(time, timeBucketId, clientTypeEnum.getValue(), takeMode4Rebate, storePromotionReduces4Match);
            storePromotionReduces = Lists.newArrayList();
            if (bestStorePromotionReduce != null) {
                Map<Long, Long> reduceMap = this.storePromotionReduceService.getBestStorePromotionReduceMap(storeChargeItems, bestStorePromotionReduce);
                this.filterNotSharedReduce(reduceMap, bestStorePromotionReduce, menuPriceTypeMap);
                if (!reduceMap.isEmpty()) {
                    storePromotionReduces.add(bestStorePromotionReduce);
                    storeTimeBucketMenuDisplayDTO.setChargeItemPromotionReduceIdMap(reduceMap);
                }
            }

            if (!StorePromotionHelper.isWechatVisist(clientTypeEnum.getValue())) {
                StorePromotionReduce bestStorePromotionReduce4TakeOut = this.storePromotionReduceService.getBestStorePromotionReduce(time, timeBucketId, clientTypeEnum.getValue(), StoreOrderTakeModeEnum.TAKE_OUT.getValue(), storePromotionReduces4Match);
                if (bestStorePromotionReduce4TakeOut != null) {
                    Map<Long, Long> reduceMap4TakeOut = this.storePromotionReduceService.getBestStorePromotionReduceMap(storeChargeItems, bestStorePromotionReduce4TakeOut);
                    this.filterNotSharedReduce(reduceMap4TakeOut, bestStorePromotionReduce4TakeOut, menuPriceTypeMap4TakeOut);
                    if (!reduceMap4TakeOut.isEmpty()) {
                        storePromotionReduces.add(bestStorePromotionReduce4TakeOut);
                        storeTimeBucketMenuDisplayDTO.setChargeItemPromotionReduceIdMap4TakeOut(reduceMap4TakeOut);
                    }
                }
            }
            storeTimeBucketMenuDisplayDTO.setStorePromotionReduceDTOs(this.buildStorePromotionReduceDTOs(storePromotionReduces, false));
        }

        if (!storePromotionGratises4Match.isEmpty()) {
            Set<Long> gratisIdSet = Sets.newHashSet();
            Map<Long, List<StorePromotionGratis>> storePromotionGratisMap = this.storePromotionGratisService.getBestStorePromotionGratisMap(timeBucketId, time, clientTypeEnum.getValue(), takeMode4Rebate, storeChargeItems, storePromotionGratises4Match);
            Map<Long, List<Long>> matchGratisMap = this.matchGratisMap(storeChargeItems, storePromotionGratisMap);
            if (matchGratisMap != null) {
                Collection<List<Long>> gratisIds = matchGratisMap.values();
                for (List<Long> idList : gratisIds) {
                    gratisIdSet.addAll(idList);
                }
                storeTimeBucketMenuDisplayDTO.setChargeItemPromotionGratisIdMap(matchGratisMap);
            }

            //收银台访问，再次获取打包的活动信息。默认收银台不会指定打包条件
            if (!StorePromotionHelper.isWechatVisist(clientTypeEnum.getValue())) {
                Map<Long, List<StorePromotionGratis>> storePromotionGratisMap4TakeOut = this.storePromotionGratisService.getBestStorePromotionGratisMap(timeBucketId, time, clientTypeEnum.getValue(), StoreOrderTakeModeEnum.TAKE_OUT.getValue(), storeChargeItems, storePromotionGratises4Match);
                if (storePromotionGratisMap4TakeOut != null) {
                    Map<Long, List<Long>> matchGratisMap4TakeOut = this.matchGratisMap(storeChargeItems, storePromotionGratisMap4TakeOut);
                    if (matchGratisMap4TakeOut != null) {
                        Collection<List<Long>> gratisIds = matchGratisMap4TakeOut.values();
                        for (List<Long> idList : gratisIds) {
                            gratisIdSet.addAll(idList);
                        }
                        storeTimeBucketMenuDisplayDTO.setChargeItemPromotionGratisIdMap4TakeOut(matchGratisMap4TakeOut);
                    }
                }
            }
            //获得所有可使用的活动
            storePromotionGratises = Lists.newArrayList();
            for (StorePromotionGratis storePromotionGratis : storePromotionGratises4Match) {
                if (gratisIdSet.contains(storePromotionGratis.getPromotionGratisId())) {
                    storePromotionGratises.add(storePromotionGratis);
                }
            }
            storeTimeBucketMenuDisplayDTO.setStorePromotionGratisDTOs(this.buildStorePromotionGratisDTOs(storePromotionGratises, false));
        }

        //进行通知数据处理
        if (userId > 0 && StorePromotionHelper.isWechatVisist(clientTypeEnum.getValue())) {
            this.buildPromotionNotify(userId, storePromotionRebates, storePromotionReduces, storePromotionGratises, storeTimeBucketMenuDisplayDTO);
        }
        //记录最近访问
        List<Long> reduceIds = Lists.newArrayList();
        List<Long> rebateIds = Lists.newArrayList();
        List<Long> gratisIds = Lists.newArrayList();
        if (storePromotionRebates != null) {
            for (StorePromotionRebate promotionRebate : storePromotionRebates) {
                rebateIds.add(promotionRebate.getPromotionRebateId());
            }
        }
        if (storePromotionReduces != null) {
            for (StorePromotionReduce promotionReduce : storePromotionReduces) {
                reduceIds.add(promotionReduce.getPromotionReduceId());
            }
        }
        if (storePromotionGratises != null) {
            for (StorePromotionGratis promotionGratis : storePromotionGratises) {
                gratisIds.add(promotionGratis.getPromotionGratisId());
            }
        }
        this.i5weiMessageProducer.sendMessageOfStoreOrderVisit(merchantId, storeId, userId, UserVisitType.BROWSE, rebateIds, reduceIds, gratisIds);
    }

    public Map<Long, List<Long>> matchGratisMap(Collection<StoreChargeItem> storeChargeItems, Map<Long, List<StorePromotionGratis>> storePromotionGratisMap) {
        Map<Long, List<Long>> map = Maps.newHashMap();
        for (StoreChargeItem storeChargeItem : storeChargeItems) {
            long storeChargeItemId = storeChargeItem.getChargeItemId();
            List<StorePromotionGratis> storePromotionGratisList = storePromotionGratisMap.get(storeChargeItemId);
            if (storePromotionGratisList == null) {
                continue;
            }
            for (StorePromotionGratis item : storePromotionGratisList) {
                List<Long> gratisIds = map.get(storeChargeItemId);
                if (gratisIds == null) {
                    gratisIds = Lists.newArrayList();
                    map.put(storeChargeItemId, gratisIds);
                }
                gratisIds.add(item.getPromotionGratisId());
            }
        }
        return map;
    }

    private void buildPromotionNotify(long userId, List<StorePromotionRebate> storePromotionRebates, List<StorePromotionReduce> storePromotionReduces, List<StorePromotionGratis> storePromotionGratises, StoreTimeBucketMenuDisplayDTO storeTimeBucketMenuDisplayDTO) {
        List<ResourceVisitParam> visitParams = Lists.newArrayList();
        if (storePromotionRebates != null && !storePromotionRebates.isEmpty()) {
            ResourceVisitParam visitParam = new ResourceVisitParam();
            visitParam.setResourceType(UserResourceVisitType.PROMOTION_REBATE.getValue());
            for (StorePromotionRebate promotionRebate : storePromotionRebates) {
                visitParam.addToResourceIds(promotionRebate.getPromotionRebateId());
            }
            visitParams.add(visitParam);
        }
        if (storePromotionReduces != null && !storePromotionReduces.isEmpty()) {
            ResourceVisitParam visitParam = new ResourceVisitParam();
            visitParam.setResourceType(UserResourceVisitType.PROMOTION_REDUCE.getValue());
            for (StorePromotionReduce promotionReduce : storePromotionReduces) {
                visitParam.addToResourceIds(promotionReduce.getPromotionReduceId());
            }
            visitParams.add(visitParam);
        }
        if (storePromotionGratises != null && !storePromotionGratises.isEmpty()) {
            ResourceVisitParam visitParam = new ResourceVisitParam();
            visitParam.setResourceType(UserResourceVisitType.PROMOTION_GRATIS.getValue());
            for (StorePromotionGratis storePromotionGratis : storePromotionGratises) {
                visitParam.addToResourceIds(storePromotionGratis.getPromotionGratisId());
            }
            visitParams.add(visitParam);
        }

        Map<Integer, List<UserResourceVisitDTO>> visitMap;
        try {
            visitMap = this.userResourceVisitFacade.getUserResourceVisitMap(userId, visitParams);
        } catch (TException ignored) {
            visitMap = new HashMap<>(0);
        }
        storeTimeBucketMenuDisplayDTO.setStorePromotionRebateIds4Notify(this.buildPromotionRebateIds4Notify(visitMap.get(UserResourceVisitType.PROMOTION_REBATE.getValue()), storePromotionRebates));
        storeTimeBucketMenuDisplayDTO.setStorePromotionReduceIds4Notify(this.buildPromotionReduceIds4Notify(visitMap.get(UserResourceVisitType.PROMOTION_REDUCE.getValue()), storePromotionReduces));
        storeTimeBucketMenuDisplayDTO.setStorePromotionGratisIds4Notify(this.buildPromotionGratisIds4Notify(visitMap.get(UserResourceVisitType.PROMOTION_GRATIS.getValue()), storePromotionGratises));
    }

    /**
     * 当满减不支持共享时，过滤掉不支持共享的满减相关
     */
    private void filterNotSharedReduce(Map<Long, Long> reduceMap, StorePromotionReduce bestStorePromotionReduce, Map<Long, StoreMenuPriceType> menuPriceTypeMap) {
        if (menuPriceTypeMap == null) {
            return;
        }
        if (bestStorePromotionReduce == null) {
            return;
        }
        if (bestStorePromotionReduce.isShared()) {
            return;
        }
        Iterator<Map.Entry<Long, Long>> it = reduceMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Long> e = it.next();
            long chargeItemId = e.getKey();
            StoreMenuPriceType priceType = menuPriceTypeMap.get(chargeItemId);
            if (!priceType.equals(StoreMenuPriceType.ORIGIN)) {
                //如果不是原价，表示使用的是某个折扣价格，满减不共享时，就不参与共享活动
                it.remove();
            }
        }
    }

    /**
     * 获得参加活动的map
     */
    public Map<Long, Long> matchRebateMap(Collection<ChargeItemRebatePrice> chargeItemRebatePrices) {
        Map<Long, Long> map = Maps.newHashMap();
        for (ChargeItemRebatePrice chargeItemRebatePrice : chargeItemRebatePrices) {
            long chargeItemId = chargeItemRebatePrice.getStoreChargeItem().getChargeItemId();
            if (chargeItemRebatePrice.getStorePromotionRebate() != null) {
                StorePromotionRebate storePromotionRebate = chargeItemRebatePrice.getStorePromotionRebate();
                map.put(chargeItemId, storePromotionRebate.getPromotionRebateId());
            }
        }
        return map;
    }

    public Map<Long, Long> buildPriceMap(Collection<ChargeItemRebatePrice> chargeItemRebatePrices) {
        Map<Long, Long> map = Maps.newHashMap();
        for (ChargeItemRebatePrice chargeItemRebatePrice : chargeItemRebatePrices) {
            map.put(chargeItemRebatePrice.getStoreChargeItem().getChargeItemId(), chargeItemRebatePrice.getPrice());
        }
        return map;
    }

    private List<Long> buildPromotionReduceIds4Notify(List<UserResourceVisitDTO> userResourceVisitDTOs,
                                                      Collection<StorePromotionReduce> storePromotionReduces) {
        List<Long> ids = Lists.newArrayList();
        if (storePromotionReduces == null || storePromotionReduces.isEmpty()) {
            return ids;
        }
        if (userResourceVisitDTOs == null || userResourceVisitDTOs.isEmpty()) {
            for (StorePromotionReduce promotionReduce : storePromotionReduces) {
                ids.add(promotionReduce.getPromotionReduceId());
            }
            return ids;
        }
        Map<Long, UserResourceVisitDTO> map = Maps.newHashMap();
        for (UserResourceVisitDTO visitDTO : userResourceVisitDTOs) {
            map.put(visitDTO.getResourceId(), visitDTO);
        }

        for (StorePromotionReduce promotionReduce : storePromotionReduces) {
            UserResourceVisitDTO visitDTO = map.get(promotionReduce.getPromotionReduceId());
            if (visitDTO == null) {
                ids.add(promotionReduce.getPromotionReduceId());
            }
        }
        return ids;
    }

    /**
     * 获得可以提醒用户的活动id list
     */
    private List<Long> buildPromotionRebateIds4Notify(List<UserResourceVisitDTO> userResourceVisitDTOs,
                                                      Collection<StorePromotionRebate> storePromotionRebates) {
        List<Long> ids = Lists.newArrayList();
        if (storePromotionRebates == null || storePromotionRebates.isEmpty()) {
            return ids;
        }
        if (userResourceVisitDTOs == null || userResourceVisitDTOs.isEmpty()) {
            for (StorePromotionRebate promotionRebate : storePromotionRebates) {
                ids.add(promotionRebate.getPromotionRebateId());
            }
            return ids;
        }
        Map<Long, UserResourceVisitDTO> map = Maps.newHashMap();
        for (UserResourceVisitDTO visitDTO : userResourceVisitDTOs) {
            map.put(visitDTO.getResourceId(), visitDTO);
        }

        for (StorePromotionRebate promotionRebate : storePromotionRebates) {
            UserResourceVisitDTO visitDTO = map.get(promotionRebate.getPromotionRebateId());
            if (visitDTO == null) {
                ids.add(promotionRebate.getPromotionRebateId());
            }
        }
        return ids;
    }

    /**
     * 获得可以提醒用户的活动id list
     */
    private List<Long> buildPromotionGratisIds4Notify(List<UserResourceVisitDTO> userResourceVisitDTOs, Collection<StorePromotionGratis> storePromotionGratises) {
        List<Long> ids = Lists.newArrayList();
        if (storePromotionGratises == null || storePromotionGratises.isEmpty()) {
            return ids;
        }
        if (userResourceVisitDTOs == null || userResourceVisitDTOs.isEmpty()) {
            for (StorePromotionGratis promotionGratis : storePromotionGratises) {
                ids.add(promotionGratis.getPromotionGratisId());
            }
            return ids;
        }
        Map<Long, UserResourceVisitDTO> map = Maps.newHashMap();
        for (UserResourceVisitDTO visitDTO : userResourceVisitDTOs) {
            map.put(visitDTO.getResourceId(), visitDTO);
        }

        for (StorePromotionGratis promotionGratis : storePromotionGratises) {
            UserResourceVisitDTO visitDTO = map.get(promotionGratis.getPromotionGratisId());
            if (visitDTO == null) {
                ids.add(promotionGratis.getPromotionGratisId());
            }
        }
        return ids;
    }

    void buildInventory(List<StoreInventoryDate> storeInventoryDates, Map<Long, StoreChargeItem> storeChargeItemMap) {
        Map<Long, StoreInventoryDate> inventoryDateMap = Maps.newHashMap();
        for (StoreInventoryDate obj : storeInventoryDates) {
            inventoryDateMap.put(obj.getProductId(), obj);
        }

        Set<Map.Entry<Long, StoreChargeItem>> set = storeChargeItemMap.entrySet();
        for (Map.Entry<Long, StoreChargeItem> e : set) {
            StoreChargeItem storeChargeItem = e.getValue();
            this.buildChargeItemInventory(storeChargeItem, inventoryDateMap);
        }
    }

    private void buildChargeItemInventory(StoreChargeItem storeChargeItem,
                                          Map<Long, StoreInventoryDate> inventoryDateMap) {
        if (storeChargeItem.getStoreChargeSubitems() != null) {
            double min = -1;
            for (StoreChargeSubitem subitem : storeChargeItem.getStoreChargeSubitems()) {
                if (subitem.getAmount() <= 0) {
                    continue;
                }
                StoreInventoryDate storeInventoryDate = inventoryDateMap.get(subitem.getProductId());
                if (storeInventoryDate != null) {
                    if (storeInventoryDate.getStoreProduct().isInvEnabled()) {
                        //如果产品开启了库存
                        double remain = storeInventoryDate.getAmountCanSell();
                        BigDecimal b1 = new BigDecimal(Double.toString(remain));
                        BigDecimal b2 = new BigDecimal(Double.toString(subitem.getAmount()));
                        double remainItem = b1.divide(b2, 1, RoundingMode.DOWN).doubleValue();
                        if (min == -1) {
                            min = remainItem;
                        } else {
                            min = Math.min(min, remainItem);
                        }
                    } else {
                        //如果未开启库存并且已经估清
                        if (storeInventoryDate.isNothingness()) {
                            min = 0;
                        }
                    }
                }
            }
            if (min <= -1) {
                storeChargeItem.setUnlimit(true);
            } else {
                double _min = new BigDecimal(min).setScale(0, RoundingMode.DOWN).doubleValue();
                storeChargeItem.setUnlimit(false);
                storeChargeItem.setRemain(_min);
            }
        }
    }

    public StoreTimeBucketDTO buildStoreTimeBucketDTO(StoreTimeBucket storeTimeBucket) {
        StoreTimeBucketDTO storeTimeBucketDTO = new StoreTimeBucketDTO();
        BeanUtil.copy(storeTimeBucket, storeTimeBucketDTO);
        return storeTimeBucketDTO;
    }

    StoreTimeBucketDTO2 buildStoreTimeBucketDTO2(StoreTimeBucket
                                                         storeTimeBucket) {
        StoreTimeBucketDTO2 storeTimeBucketDTO2 = new StoreTimeBucketDTO2();
        BeanUtil.copy(storeTimeBucket, storeTimeBucketDTO2);
        return storeTimeBucketDTO2;
    }

    List<StoreTimeBucketDTO> buildStoreTimeBucketDTOs
            (List<StoreTimeBucket> storeTimeBuckets) {
        List<StoreTimeBucketDTO> list = new ArrayList<>();
        for (StoreTimeBucket storeTimeBucket : storeTimeBuckets) {
            list.add(buildStoreTimeBucketDTO(storeTimeBucket));
        }
        return list;
    }

    List<StoreMenuDisplayCatDTO> buildStoreMenuDisplayCatDTOs
            (List<StoreMenuDisplayCat> storeMenuDisplayCats) {
        List<StoreMenuDisplayCatDTO> list = Lists.newArrayList();
        for (StoreMenuDisplayCat cat : storeMenuDisplayCats) {
            StoreMenuDisplayCatDTO dto = new StoreMenuDisplayCatDTO();
            BeanUtil.copy(cat, dto);
            list.add(dto);
        }
        return list;
    }

    List<StoreMenuDisplayDTO> buildStoreMenuDisplayDTOs
            (List<StoreMenuDisplay> storeMenuDisplays) {
        List<StoreMenuDisplayDTO> list = Lists.newArrayList();
        for (StoreMenuDisplay obj : storeMenuDisplays) {
            StoreMenuDisplayDTO dto = new StoreMenuDisplayDTO();
            BeanUtil.copy(obj, dto);
            list.add(dto);
        }
        return list;
    }

    StoreTvMenuDTO buildStoreTvMenuDTO(StoreTvMenu storeTvMenu) {
        StoreTvMenuDTO storeTvMenuDTO = new StoreTvMenuDTO();
        BeanUtil.copy(storeTvMenu, storeTvMenuDTO);
        return storeTvMenuDTO;
    }

    List<StoreTvMenuDTO> buildStoreTvMenuDTOs(List<StoreTvMenu> storeTvMenus) {
        List<StoreTvMenuDTO> list = Lists.newArrayList();
        for (StoreTvMenu storeTvMenu : storeTvMenus) {
            list.add(this.buildStoreTvMenuDTO(storeTvMenu));
        }
        return list;
    }

    private StoreTimeTvMenuDTO buildStoreTimeTvMenuDTO(StoreTimeTvMenu storeTimeTvMenu) {
        StoreTimeTvMenuDTO dto = new StoreTimeTvMenuDTO();
        dto.setRecommendable(storeTimeTvMenu.isRecommendable());
        dto.setStoreTvMenuDTO(this.buildStoreTvMenuDTO(storeTimeTvMenu.getStoreTvMenu()));
        return dto;
    }

    List<StoreTimeTvMenuDTO> buildStoreTimeTvMenuDTOs(List<StoreTimeTvMenu> storeTimeTvMenus) {
        List<StoreTimeTvMenuDTO> list = Lists.newArrayList();
        for (StoreTimeTvMenu storeTimeTvMenu : storeTimeTvMenus) {
            list.add(this.buildStoreTimeTvMenuDTO(storeTimeTvMenu));
        }
        return list;
    }

    public StorePromotionRebateDTO buildStorePromotionRebateDTO(StorePromotionRebate storePromotionRebate, boolean buildRefInfo) {
        StorePromotionRebateDTO dto = new StorePromotionRebateDTO();
        BeanUtil.copy(storePromotionRebate, dto);
        if (!buildRefInfo) {
            return dto;
        }
        if (storePromotionRebate.getChargeItems() != null) {
            for (StorePromotionRebateChargeItem rebateChargeItem : storePromotionRebate.getChargeItems()) {
                StorePromotionChargeItemDTO rebateChargeItemDTO = new StorePromotionChargeItemDTO();
                BeanUtil.copy(rebateChargeItem, rebateChargeItemDTO);
                if (rebateChargeItem.getStoreChargeItem() != null) {
                    rebateChargeItemDTO.setChargeItemName(rebateChargeItem.getStoreChargeItem().getName());
                    if (rebateChargeItem.getStoreChargeItem().isDeleted()) {
                        rebateChargeItemDTO.setChargeItemDeleted(true);
                    }
                }
                dto.addToChargeItemDTOs(rebateChargeItemDTO);
            }
        }
        if (storePromotionRebate.getPeriods() != null) {
            Map<Long, List<Integer>> tbWeekDaysMap = Maps.newHashMap();
            Map<Long, StoreTimeBucket> tbMap = Maps.newHashMap();
            List<Long> timeBucketIds = Lists.newArrayList();
            for (StorePromotionRebatePeriod rebatePeriod : storePromotionRebate.getPeriods()) {
                List<Integer> weekDays = tbWeekDaysMap.get(rebatePeriod.getTimeBucketId());
                if (rebatePeriod.getStoreTimeBucket() != null) {
                    tbMap.put(rebatePeriod.getTimeBucketId(), rebatePeriod.getStoreTimeBucket());
                }
                if (weekDays == null) {
                    timeBucketIds.add(rebatePeriod.getTimeBucketId());
                    weekDays = Lists.newArrayList();
                    tbWeekDaysMap.put(rebatePeriod.getTimeBucketId(), weekDays);
                }
                weekDays.add(rebatePeriod.getWeekDay());
            }
            dto.setPeriodDTOs(this.buildStorePromotionPeriodDTOs(timeBucketIds, tbMap, tbWeekDaysMap));
        }
        if (storePromotionRebate.getStaffDTO2() != null) {
            dto.setStaffDTO(this.buildStaffDTO(storePromotionRebate.getStaffDTO2()));
        }
        return dto;
    }

    private List<StorePromotionPeriodDTO> buildStorePromotionPeriodDTOs(List<Long> timeBucketIds, Map<Long, StoreTimeBucket> tbMap, Map<Long, List<Integer>> tbWeekDaysMap) {
        List<StorePromotionPeriodDTO> dtos = Lists.newArrayList();
        for (Long timeBucketId : timeBucketIds) {
            StorePromotionPeriodDTO periodDTO = new StorePromotionPeriodDTO();
            periodDTO.setTimeBucketId(timeBucketId);
            if (tbMap.containsKey(timeBucketId)) {
                periodDTO.setTimeBucketName(tbMap.get(timeBucketId).getName());
            }
            periodDTO.setWeekDays(tbWeekDaysMap.get(timeBucketId));
            dtos.add(periodDTO);
        }
        return dtos;
    }

    public List<StorePromotionRebateDTO> buildStorePromotionRebateDTOs(Collection<StorePromotionRebate> storePromotionRebates, boolean buildRefInfo) {
        List<StorePromotionRebateDTO> dtos = Lists.newArrayList();
        for (StorePromotionRebate storePromotionRebate : storePromotionRebates) {
            dtos.add(this.buildStorePromotionRebateDTO(storePromotionRebate, buildRefInfo));
        }
        return dtos;
    }

    public StorePromotionReduceDTO buildStorePromotionReduceDTO(StorePromotionReduce storePromotionReduce, boolean buildRefInfo) {
        StorePromotionReduceDTO dto = new StorePromotionReduceDTO();
        BeanUtil.copy(storePromotionReduce, dto);
        if (storePromotionReduce.getQuotas() != null) {
            for (StorePromotionReduceQuota quota : storePromotionReduce.getQuotas()) {
                StorePromotionReduceQuotaDTO quotaDTO = new StorePromotionReduceQuotaDTO();
                BeanUtil.copy(quota, quotaDTO);
                dto.addToQuotaDTOs(quotaDTO);
            }
        }
        if (!buildRefInfo) {
            return dto;
        }
        if (storePromotionReduce.getChargeItems() != null) {
            for (StorePromotionReduceChargeItem reduceChargeItem : storePromotionReduce.getChargeItems()) {
                StorePromotionChargeItemDTO reduceChargeItemDTO = new StorePromotionChargeItemDTO();
                BeanUtil.copy(reduceChargeItem, reduceChargeItemDTO);
                dto.addToChargeItemDTOs(reduceChargeItemDTO);
            }
        }
        if (storePromotionReduce.getPeriods() != null) {
            Map<Long, List<Integer>> tbWeekDaysMap = Maps.newHashMap();
            Map<Long, StoreTimeBucket> tbMap = Maps.newHashMap();
            List<Long> timeBucketIds = Lists.newArrayList();
            for (StorePromotionReducePeriod reducePeriod : storePromotionReduce.getPeriods()) {
                List<Integer> weekDays = tbWeekDaysMap.get(reducePeriod.getTimeBucketId());
                if (reducePeriod.getStoreTimeBucket() != null) {
                    tbMap.put(reducePeriod.getTimeBucketId(), reducePeriod.getStoreTimeBucket());
                }
                if (weekDays == null) {
                    timeBucketIds.add(reducePeriod.getTimeBucketId());
                    weekDays = Lists.newArrayList();
                    tbWeekDaysMap.put(reducePeriod.getTimeBucketId(), weekDays);
                }
                weekDays.add(reducePeriod.getWeekDay());
            }
            dto.setPeriodDTOs(this.buildStorePromotionPeriodDTOs(timeBucketIds, tbMap, tbWeekDaysMap));
        }
        if (storePromotionReduce.getStaffDTO2() != null) {
            dto.setStaffDTO(this.buildStaffDTO(storePromotionReduce.getStaffDTO2()));
        }
        return dto;
    }

    public List<StorePromotionReduceDTO> buildStorePromotionReduceDTOs(Collection<StorePromotionReduce> storePromotionReduces, boolean buildRefInfo) {
        List<StorePromotionReduceDTO> dtos = Lists.newArrayList();
        for (StorePromotionReduce storePromotionReduce : storePromotionReduces) {
            dtos.add(this.buildStorePromotionReduceDTO(storePromotionReduce, buildRefInfo));
        }
        return dtos;
    }


    private I5weiStaffDTO buildStaffDTO(StaffDTO2 staffDTO) {
        I5weiStaffDTO dto = new I5weiStaffDTO();
        BeanUtil.copy(staffDTO, dto);
        dto.setHead(staffDTO.getMerchantUserDTO().getHead());
        return dto;
    }

    public StorePromotionGratisDTO buildStorePromotionGratisDTO(StorePromotionGratis storePromotionGratis, boolean buildRefInfo) {
        StorePromotionGratisDTO dto = new StorePromotionGratisDTO();
        BeanUtil.copy(storePromotionGratis, dto);
        if (!buildRefInfo) {
            return dto;
        }
        if (storePromotionGratis.getChargeItems() != null) {
            for (StorePromotionGratisChargeItem item : storePromotionGratis.getChargeItems()) {
                StorePromotionChargeItemDTO storePromotionChargeItemDTO = new StorePromotionChargeItemDTO();
                storePromotionChargeItemDTO.setChargeItemId(item.getChargeItemId());
                if (item.getStoreChargeItem() != null) {
                    storePromotionChargeItemDTO.setChargeItemName(item.getStoreChargeItem().getName());
                    if (item.getStoreChargeItem().getCurStoreChargeItemPrice() != null) {
                        storePromotionChargeItemDTO.setChargeItemPrice(item.getStoreChargeItem().getCurStoreChargeItemPrice().getPrice());
                    }
                    if (item.getStoreChargeItem().isDeleted()) {
                        storePromotionChargeItemDTO.setChargeItemDeleted(item.getStoreChargeItem().isDeleted());
                    }
                }
                dto.addToChargeItemDTOs(storePromotionChargeItemDTO);
            }
        }
        if (storePromotionGratis.getStaffDTO2() != null) {
            dto.setStaffDTO(this.buildStaffDTO(storePromotionGratis.getStaffDTO2()));
        }
        if (storePromotionGratis.getPeriods() != null) {
            Map<Long, List<Integer>> tbWeekDaysMap = Maps.newHashMap();
            Map<Long, StoreTimeBucket> tbMap = Maps.newHashMap();
            List<Long> timeBucketIds = Lists.newArrayList();
            for (StorePromotionGratisPeriod gratisPeriod : storePromotionGratis.getPeriods()) {
                List<Integer> weekDays = tbWeekDaysMap.get(gratisPeriod.getTimeBucketId());
                if (gratisPeriod.getStoreTimeBucket() != null) {
                    tbMap.put(gratisPeriod.getTimeBucketId(), gratisPeriod.getStoreTimeBucket());
                }
                if (weekDays == null) {
                    timeBucketIds.add(gratisPeriod.getTimeBucketId());
                    weekDays = Lists.newArrayList();
                    tbWeekDaysMap.put(gratisPeriod.getTimeBucketId(), weekDays);
                }
                weekDays.add(gratisPeriod.getWeekDay());
            }
            dto.setPeriodDTOs(this.buildStorePromotionPeriodDTOs(timeBucketIds, tbMap, tbWeekDaysMap));
        }
        return dto;
    }

    /**
     * 将实体对象装换成DTO对象
     *
     * @param promotionGratises
     * @param buildRefInfo      是否构建相关联的对象
     * @return
     */
    public List<StorePromotionGratisDTO> buildStorePromotionGratisDTOs(Collection<StorePromotionGratis> promotionGratises, boolean buildRefInfo) {
        List<StorePromotionGratisDTO> gratisDTOList = Lists.newArrayList();
        for (StorePromotionGratis storePromotionGratis : promotionGratises) {
            gratisDTOList.add(this.buildStorePromotionGratisDTO(storePromotionGratis, buildRefInfo));
        }
        return gratisDTOList;
    }

}
