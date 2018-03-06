package com.huofu.module.i5wei.promotion.facade;

import com.google.common.collect.Lists;
import com.huofu.module.i5wei.menu.facade.MenuFacadeUtil;
import com.huofu.module.i5wei.menu.service.StoreChargeItemService;
import com.huofu.module.i5wei.promotion.entity.StorePromotionGratis;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import com.huofu.module.i5wei.promotion.service.*;
import huofucore.facade.i5wei.promotion.*;
import huofuhelper.util.bean.BeanUtil;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 菜单活动服务
 * Created by akwei on 11/14/16.
 */
@Component
@ThriftServlet(name = "storeMenuPromotionFacadeServlet", serviceClass = StoreMenuPromotionFacade.class)
public class StoreMenuPromotionFacadeImpl implements StoreMenuPromotionFacade.Iface {

    @Resource
    private StorePromotionRebateService storePromotionRebateService;

    @Resource
    private StorePromotionReduceService storePromotionReduceService;

    @Resource
    private StorePromotionGratisService storePromotionGratisService;

    @Resource
    private MenuFacadeUtil menuFacadeUtil;

    @Resource
    private StoreChargeItemPromotionService storeChargeItemPromotionService;

    @Resource
    private StoreChargeItemService storeChargeItemService;

    @Override
    public StoreMenuPromotionDTO getStoreMenuPromotion(StoreMenuPromotionParam param) throws TException {
        StoreMenuPromotionDTO dto = new StoreMenuPromotionDTO();
        boolean loadChargeItem = param.isLoadChargeItem();
        Collection<StorePromotionRebate> promotionRebates = this.storePromotionRebateService.getBestStorePromotionRebates(param.getMerchantId(), param.getStoreId(), param.getTimeBucketId(), param.getTime(), param.getClientType(), param.getTakeMode(), param.isLoadChargeItem());
        StorePromotionReduce promotionReduce = this.storePromotionReduceService.getBestStorePromotionReduce(param.getMerchantId(), param.getStoreId(), param.getTime(), param.getTimeBucketId(), param.getClientType(), param.getTakeMode(), param.isLoadChargeItem());
        Collection<StorePromotionGratis> promotionGratises = this.storePromotionGratisService.getBestStorePromotionGratis(param.getMerchantId(), param.getStoreId(), param.getTime(), param.getTimeBucketId(), param.getClientType(), param.getTakeMode(), loadChargeItem);

        if (promotionReduce != null) {
            List<StorePromotionReduce> promotionReduces = Lists.newArrayList(promotionReduce);
            dto.setStorePromotionReduceDTOs(this.menuFacadeUtil.buildStorePromotionReduceDTOs(promotionReduces, param.isLoadChargeItem()));
        } else {
            List<StorePromotionReduceDTO> list = Lists.newArrayList();
            dto.setStorePromotionReduceDTOs(list);
        }
        dto.setStorePromotionRebateDTOs(this.menuFacadeUtil.buildStorePromotionRebateDTOs(promotionRebates, param.isLoadChargeItem()));
        dto.setStorePromotionGratisDTOs(this.menuFacadeUtil.buildStorePromotionGratisDTOs(promotionGratises, param.isLoadChargeItem()));
        return dto;
    }

    @Override
    public Map<Long, Integer> getStoreMenuChargeItemPromotionNumMap(StoreMenuChargeItemPromotionParam param) throws TException {
        return storeChargeItemPromotionService.getStoreMenuChargeItemPromotionNumMap(param);
    }

    @Override
    public StoreAllPromotionChargeItemDTO getStoreAllPromotionChargeItem(int merchantId, long storeId, List<Long> chargeItemIds) throws TException {
        StoreAllPromotionChargeItemDTO dto = new StoreAllPromotionChargeItemDTO();
        long time = System.currentTimeMillis();
        //获取未结束的折扣、满减、买免活动的id集合
        //1. 折扣活动
        ChargeItemPromotionRebates chargeItemPromotionRebateList = this.storePromotionRebateService.getStorePromotionRebate4ChargeItemList(merchantId, storeId, time, chargeItemIds);
        List<StorePromotionRebate> rebateList = chargeItemPromotionRebateList.getStorePromotionRebateList();
        if (CollectionUtils.isNotEmpty(rebateList)) {
            List<StorePromotionRebateDTO> storePromotionRebateDTOs = Lists.newArrayList();
            StorePromotionRebateDTO rebateDTO;
            for (StorePromotionRebate item : rebateList) {
                rebateDTO = BeanUtil.copy(item, StorePromotionRebateDTO.class);
                storePromotionRebateDTOs.add(rebateDTO);
            }
            dto.setStorePromotionRebateDTOs(storePromotionRebateDTOs);
            dto.setStorePromotionRebateIdMap(chargeItemPromotionRebateList.getPromotionRebateIdMap());
        }
        //2. 满减活动
        ChargeItemPromotionReduces chargeItemPromotionReduceList = this.storePromotionReduceService.getStorePromotionReduce4ChargeItemList(merchantId, storeId, time, chargeItemIds);
        List<StorePromotionReduce> reduceList = chargeItemPromotionReduceList.getStorePromotionReduceList();
        if (CollectionUtils.isNotEmpty(reduceList)) {
            List<StorePromotionReduceDTO> storePromotionReduceDTOs = Lists.newArrayList();
            StorePromotionReduceDTO reduceDTO;
            for (StorePromotionReduce item : reduceList) {
                reduceDTO = BeanUtil.copy(item, StorePromotionReduceDTO.class);
                storePromotionReduceDTOs.add(reduceDTO);
            }
            dto.setStorePromotionReduceDTOs(storePromotionReduceDTOs);
            dto.setStorePromotionReduceIdMap(chargeItemPromotionReduceList.getPromotionReduceIdMap());
        }
        //3. 买免活动
        ChargeItemPromotionGratises chargeItemPromotionGratisList = this.storePromotionGratisService.getStorePromotionGratis4ChargeItemList(merchantId, storeId, time, chargeItemIds);
        List<StorePromotionGratis> gratisList = chargeItemPromotionGratisList.getStorePromotionGratisList();
        if (CollectionUtils.isNotEmpty(gratisList)) {
            List<StorePromotionGratisDTO> storePromotionGratisDTOs = Lists.newArrayList();
            StorePromotionGratisDTO gratisDTO;
            for (StorePromotionGratis item : gratisList) {
                gratisDTO = BeanUtil.copy(item, StorePromotionGratisDTO.class);
                storePromotionGratisDTOs.add(gratisDTO);
            }
            dto.setStorePromotionGratisDTOs(storePromotionGratisDTOs);
            dto.setStorePromotionGratisIdMap(chargeItemPromotionGratisList.getPromotionGratisIdMap());
        }
        return dto;
    }

    @Override
    public StorePromotion4ChargeItemDTO getStorePromotion4ChargeItem(int merchantId, long storeId, long chargeItemId) throws TException {
        Long time = System.currentTimeMillis();
        // 获取指定收费项目所参与的活动
        StoreRebate4ChargeItemInfo rebate4ChargeItemInfo = this.storePromotionRebateService.getStorePromotionRebate4ChargeItem
                (merchantId, storeId, chargeItemId, time);
        StoreReduce4ChargeItemInfo reduce4ChargeItemInfo = this.storePromotionReduceService.getStorePromotionReduce4ChargeItem
                (merchantId, storeId, chargeItemId, time);
        StoreGratis4ChargeItemInfo gratis4ChargeItemInfo = this.storePromotionGratisService.getStorePromotionGratis4ChargeItem
                (merchantId, storeId, chargeItemId, time);

        return this.buildStorePromotion4ChargeItem(rebate4ChargeItemInfo, reduce4ChargeItemInfo, gratis4ChargeItemInfo);
    }

    @Override
    public StorePromotion4ChargeItemDTO saveStorePromotion4ChargeItem(SaveStorePromotionParam param) throws TException {
        Long time = System.currentTimeMillis();
        // 查询可用的收费项目,并验证指定收费项目是否有效
        List<Long> storeChargeItemIds = this.storeChargeItemService.getAllStoreChargeItemExceptId(param.getMerchantId(), param.getStoreId(), param.getChargeItemId());
        // 更新该收费项目所参与的活动
        StoreRebate4ChargeItemInfo rebate4ChargeItemInfo = this.storePromotionRebateService.saveStorePromotionRebate4ChargeItem
                (param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), param.getPromotionRebateIds(), time, storeChargeItemIds);
        StoreReduce4ChargeItemInfo reduce4ChargeItemInfo = this.storePromotionReduceService.saveStorePromotionReduce4ChargeItem
                (param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), param.getPromotionReduceIds(), time, storeChargeItemIds);
        StoreGratis4ChargeItemInfo gratis4ChargeItemInfo = this.storePromotionGratisService.saveStorePromotionGratis4ChargeItem
                (param.getMerchantId(), param.getStoreId(), param.getChargeItemId(), param.getPromotionGratisIds(), time, storeChargeItemIds);

        return this.buildStorePromotion4ChargeItem(rebate4ChargeItemInfo, reduce4ChargeItemInfo, gratis4ChargeItemInfo);
    }

    /**
     * 将指定收费项目参加的活动转化为dto
     */
    private StorePromotion4ChargeItemDTO buildStorePromotion4ChargeItem(StoreRebate4ChargeItemInfo rebate4ChargeItemInfo,
                                                                        StoreReduce4ChargeItemInfo reduce4ChargeItemInfo,
                                                                        StoreGratis4ChargeItemInfo gratis4ChargeItemInfo) {
        List<StorePromotionRebate> rebateList4Use = rebate4ChargeItemInfo.getStorePromotionRebateList4Use();
        List<StorePromotionRebateDTO> rebateDTOs4Use = Lists.newArrayList();
        StorePromotionRebateDTO rebateDTO;
        for (StorePromotionRebate item : rebateList4Use) {
            rebateDTO = new StorePromotionRebateDTO();
            BeanUtil.copy(item, rebateDTO);
            rebateDTOs4Use.add(rebateDTO);
        }
        List<StorePromotionRebate> rebateList4NoUse = rebate4ChargeItemInfo.getStorePromotionRebateList4NoUse();
        List<StorePromotionRebateDTO> rebateDTOs4NoUse = Lists.newArrayList();
        for (StorePromotionRebate item : rebateList4NoUse) {
            rebateDTO = new StorePromotionRebateDTO();
            BeanUtil.copy(item, rebateDTO);
            rebateDTOs4NoUse.add(rebateDTO);
        }

        List<StorePromotionReduce> reduceList4Use = reduce4ChargeItemInfo.getStorePromotionReduceList4Use();
        List<StorePromotionReduceDTO> reduceDTOs4Use = Lists.newArrayList();
        StorePromotionReduceDTO reduceDTO;
        for (StorePromotionReduce item : reduceList4Use) {
            reduceDTO = new StorePromotionReduceDTO();
            BeanUtil.copy(item, reduceDTO);
            reduceDTOs4Use.add(reduceDTO);
        }
        List<StorePromotionReduce> reduceList4NoUse = reduce4ChargeItemInfo.getStorePromotionReduceList4NoUse();
        List<StorePromotionReduceDTO> reduceDTOs4NoUse = Lists.newArrayList();
        for (StorePromotionReduce item : reduceList4NoUse) {
            reduceDTO = new StorePromotionReduceDTO();
            BeanUtil.copy(item, reduceDTO);
            reduceDTOs4NoUse.add(reduceDTO);
        }

        List<StorePromotionGratis> gratisList4Use = gratis4ChargeItemInfo.getStorePromotionGratisList4Use();
        List<StorePromotionGratisDTO> gratisDTOs4Use = Lists.newArrayList();
        StorePromotionGratisDTO gratisDTO;
        for (StorePromotionGratis item : gratisList4Use) {
            gratisDTO = new StorePromotionGratisDTO();
            BeanUtil.copy(item, gratisDTO);
            gratisDTOs4Use.add(gratisDTO);
        }
        List<StorePromotionGratis> gratisList4NoUse = gratis4ChargeItemInfo.getStorePromotionGratisList4NoUse();
        List<StorePromotionGratisDTO> gratisDTOs4NoUse = Lists.newArrayList();
        for (StorePromotionGratis item : gratisList4NoUse) {
            gratisDTO = new StorePromotionGratisDTO();
            BeanUtil.copy(item, gratisDTO);
            gratisDTOs4NoUse.add(gratisDTO);
        }

        StorePromotion4ChargeItemDTO dto = new StorePromotion4ChargeItemDTO();
        StorePromotionDTO promotionDTO4Use = new StorePromotionDTO();
        promotionDTO4Use.setStorePromotionRebateDTOs(rebateDTOs4Use);
        promotionDTO4Use.setStorePromotionReduceDTOs(reduceDTOs4Use);
        promotionDTO4Use.setStorePromotionGratisDTOs(gratisDTOs4Use);
        dto.setStoreAllPromotion4Use(promotionDTO4Use);

        StorePromotionDTO promotionDTO4NoUse = new StorePromotionDTO();
        promotionDTO4NoUse.setStorePromotionRebateDTOs(rebateDTOs4NoUse);
        promotionDTO4NoUse.setStorePromotionReduceDTOs(reduceDTOs4NoUse);
        promotionDTO4NoUse.setStorePromotionGratisDTOs(gratisDTOs4NoUse);
        dto.setStoreAllPromotion4NoUse(promotionDTO4NoUse);
        return dto;
    }
}
