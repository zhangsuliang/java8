package com.huofu.module.i5wei.promotion.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.facade.MenuFacadeUtil;
import com.huofu.module.i5wei.order.service.StoreOrderPromotionStatService;
import com.huofu.module.i5wei.promotion.entity.StorePromotionRebate;
import com.huofu.module.i5wei.promotion.service.StorePromotionRebateService;
import com.huofu.module.i5wei.promotion.service.StorePromotionSummary;
import huofucore.facade.i5wei.exception.T5weiErrorCodeType;
import huofucore.facade.i5wei.exception.T5weiException;
import huofucore.facade.i5wei.order.StoreOrderPromotionTypeEnum;
import huofucore.facade.i5wei.promotion.*;
import huofuhelper.util.PageResult;
import huofuhelper.util.thrift.ThriftServlet;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Component
@ThriftServlet(name = "storePromotionRebateFacadeServlet", serviceClass = StorePromotionRebateFacade.class)
public class StorePromotionRebateFacadeImpl implements StorePromotionRebateFacade.Iface {

    @Resource
    private StorePromotionRebateService storePromotionRebateService;

    @Resource
    private StoreOrderPromotionStatService storeOrderPromotionStatService;

    @Resource
    private StorePromotionRebateFacadeValidator storePromotionRebateFacadeValidator;

    @Resource
    private MenuFacadeUtil menuFacadeUtil;

    @Override
    public StorePromotionRebateDTO saveStorePromotionRebate(StorePromotionRebateParam storePromotionRebateParam) throws TException {
        storePromotionRebateFacadeValidator.validate4Save(storePromotionRebateParam);
        StorePromotionRebate storePromotionRebate = this.storePromotionRebateService.saveStorePromotionRebate(storePromotionRebateParam);
        List<StorePromotionRebate> promotionList = Lists.newArrayList(storePromotionRebate);
        this.storePromotionRebateService.buildRebateInfo(storePromotionRebate.getMerchantId(), storePromotionRebate.getStoreId(), promotionList, true, true, false);
        return this.menuFacadeUtil.buildStorePromotionRebateDTO(storePromotionRebate, true);
    }

    @Override
    public StorePromotionRebateDTO getStorePromotionRebate(int merchantId, long storeId, long promotionRebateId) throws TException {
	    StorePromotionRebate promotionRebate =
			    this.storePromotionRebateService.getStorePromotionRebate(merchantId, storeId, promotionRebateId, true, true);
	    return this.menuFacadeUtil.buildStorePromotionRebateDTO(promotionRebate, true);
    }

    @Override
    public List<StoreOrderItemPromotionRebateStatDTO> getStorePromotionRebatesStatByRepastDate(int merchantId, long storeId, long repastDate) throws TException {
        if (storeId <= 0 || repastDate <= 0) {
            return Lists.newArrayList();
        }
        return storeOrderPromotionStatService.getStorePromotionRebatesStat(merchantId, storeId, repastDate);
    }

    @Override
    public StoreOrderItemPromotionRebateStatDTO getStorePromotionRebateStatByRepastDate(int merchantId, long storeId, long promotionRebateId, long repastDate) throws TException {
        if (storeId <= 0 || promotionRebateId <= 0 || repastDate <= 0) {
            return new StoreOrderItemPromotionRebateStatDTO();
        }
        return storeOrderPromotionStatService.getStorePromotionRebateStat(merchantId, storeId, promotionRebateId, repastDate);
    }

    @Override
    public StoreOrderItemPromotionRebateStatDTO getStorePromotionRebateStatByTimeRange(int merchantId, long storeId, long promotionRebateId, long startDate, long endDate) throws TException {
        if (storeId <= 0 || promotionRebateId <= 0 || startDate <= 0 || endDate <= 0) {
            return new StoreOrderItemPromotionRebateStatDTO();
        }
        return storeOrderPromotionStatService.getStorePromotionRebateStat(merchantId, storeId, promotionRebateId, startDate, startDate);
    }

    @Override
    public StorePromotionRebateSummaryDTO getStorePromotionRebateSummary(int merchantId, long storeId) throws TException {
        StorePromotionSummary<StorePromotionRebate> summary = this.storePromotionRebateService.getStorePromotionRebateSummary(merchantId, storeId);
        StorePromotionRebateSummaryDTO summaryDTO = new StorePromotionRebateSummaryDTO();
        summaryDTO.setCount4NotBegin(summary.getCount4NotBegin());
        summaryDTO.setCount4Doing(summary.getCount4Doing());
        summaryDTO.setCount4Paused(summary.getCount4Paused());
        summaryDTO.setCount4Ended(summary.getCount4Ended());
        if (summary.getList4NotBegin() != null) {
            summaryDTO.setList4NotBegin(this.menuFacadeUtil.buildStorePromotionRebateDTOs(summary.getList4NotBegin(), true));
        }
        if (summary.getList4Doing() != null) {
            summaryDTO.setList4Doing(this.menuFacadeUtil.buildStorePromotionRebateDTOs(summary.getList4Doing(), true));
        }
        if (summary.getList4Paused() != null) {
            summaryDTO.setList4Paused(this.menuFacadeUtil.buildStorePromotionRebateDTOs(summary.getList4Paused(), true));
        }
        return summaryDTO;
    }

    @Override 
    public StorePromotionRebatePageDTO getStorePromotionRebates(int merchantId, long storeId, int status, int page, int size) throws TException {
        PageResult pageResult = this.storePromotionRebateService.getStorePromotionRebates(merchantId, storeId, status, page, size);
        StorePromotionRebatePageDTO pageDTO = new StorePromotionRebatePageDTO();
        pageDTO.setSize(size);
        pageDTO.setPageNo(page);
        pageDTO.setTotal(pageResult.getTotal());
        pageDTO.setPageNum(pageResult.getTotalPage());
        pageDTO.setDataList(this.menuFacadeUtil.buildStorePromotionRebateDTOs(pageResult.getList(), true));
        return pageDTO;
    }

    @Override 
    public void changePromotionRebatePaused(int merchantId, long storeId, long promotionRebateId, boolean paused) throws TException {
        this.storePromotionRebateService.changePromotionRebatePaused(merchantId, storeId, promotionRebateId, paused);
    }

    @Override
    public void deleteStorePromotionRebate(int merchantId, long storeId, long promotionRebateId) throws TException {
        if (this.storeOrderPromotionStatService.hasStoreOrderPromotions(merchantId, storeId, promotionRebateId, StoreOrderPromotionTypeEnum.PROMOTION_REBATE.getValue(), true)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REBATE_STAT_EXIST.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] rebateId[" + promotionRebateId + "] has stat data");
        }
        this.storePromotionRebateService.deleteStorePromotionRebate(merchantId, storeId, promotionRebateId);
    }

    @Override
    public Map<String, List<StorePromotionRebateDTO>> getStorePromotionRebateMapByTitle(int merchantId, long storeId, String title, int size) throws TException {
        Map<String, List<StorePromotionRebateDTO>> storePromotionDTOMap = Maps.newHashMap();
        Map<String, List<StorePromotionRebate>> storePromotionRebateMap = storePromotionRebateService.getStorePromotionRebateMapByTitle(merchantId, storeId, title, size);
        for (Map.Entry<String, List<StorePromotionRebate>> entry : storePromotionRebateMap.entrySet()) {
            List<StorePromotionRebate> list = entry.getValue();
            List<StorePromotionRebateDTO> valueDTO = this.menuFacadeUtil.buildStorePromotionRebateDTOs(list, true);
            storePromotionDTOMap.put(entry.getKey(), valueDTO);
        }
        return storePromotionDTOMap;
    }
}
