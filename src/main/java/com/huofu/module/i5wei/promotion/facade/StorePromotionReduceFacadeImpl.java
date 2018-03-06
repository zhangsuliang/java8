package com.huofu.module.i5wei.promotion.facade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.huofu.module.i5wei.menu.facade.MenuFacadeUtil;
import com.huofu.module.i5wei.order.service.StoreOrderPromotionStatService;
import com.huofu.module.i5wei.promotion.entity.StorePromotionReduce;
import com.huofu.module.i5wei.promotion.service.StorePromotionReduceService;
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
@ThriftServlet(name = "storePromotionReduceFacadeServlet", serviceClass = StorePromotionReduceFacade.class)
public class StorePromotionReduceFacadeImpl implements StorePromotionReduceFacade.Iface {

    @Resource
    private StorePromotionReduceService storePromotionReduceService;

    @Resource
    private StoreOrderPromotionStatService storeOrderPromotionStatService;

    @Resource
    private StorePromotionReduceFacadeValidator storePromotionReduceFacadeValidator;

    @Resource
    private MenuFacadeUtil menuFacadeUtil;

    @Override
    public StorePromotionReduceDTO saveStorePromotionReduce(StorePromotionReduceParam storePromotionReduceParam) throws TException {
        this.storePromotionReduceFacadeValidator.validate4Save(storePromotionReduceParam);
        StorePromotionReduce storePromotionReduce = this.storePromotionReduceService.saveStorePromotionReduce(storePromotionReduceParam);
        List<StorePromotionReduce> promotionList = Lists.newArrayList(storePromotionReduce);
        this.storePromotionReduceService.buildReduceInfo(storePromotionReduce.getMerchantId(), storePromotionReduce.getStoreId(), promotionList, true, true, false);
        return this.menuFacadeUtil.buildStorePromotionReduceDTO(storePromotionReduce, true);
    }

    @Override
    public StorePromotionReduceDTO getStorePromotionReduce(int merchantId, long storeId, long promotionReduceId) throws TException {
        StorePromotionReduce storePromotionReduce = this.storePromotionReduceService.getStorePromotionReduce(merchantId, storeId, promotionReduceId, true, true, true);
        return this.menuFacadeUtil.buildStorePromotionReduceDTO(storePromotionReduce, true);
    }

    @Override
    public List<StoreOrderItemPromotionReduceStatDTO> getStorePromotionRebatesStatByRepastDate(int merchantId, long storeId, long repastDate) throws TException {
        if (storeId <= 0 || repastDate <= 0) {
            return Lists.newArrayList();
        }
        return storeOrderPromotionStatService.getStorePromotionReducesStat(merchantId, storeId, repastDate);
    }

    @Override
    public List<StoreOrderItemPromotionReduceStatDTO> getStorePromotionReduceStatByRepastDate(int merchantId, long storeId, long promotionReduceId, long repastDate) throws TException {
        if (storeId <= 0 || promotionReduceId <= 0 || repastDate <= 0) {
            return Lists.newArrayList();
        }
        return storeOrderPromotionStatService.getStorePromotionReduceStat(merchantId, storeId, promotionReduceId, repastDate);
    }

    @Override
    public List<StoreOrderItemPromotionReduceStatDTO> getStorePromotionReduceStatByTimeRange(int merchantId, long storeId, long promotionReduceId, long startDate, long endDate) throws TException {
        if (storeId <= 0 || promotionReduceId <= 0 || startDate <= 0 || endDate <= 0) {
            return Lists.newArrayList();
        }
        return storeOrderPromotionStatService.getStorePromotionReduceStat(merchantId, storeId, promotionReduceId, startDate, endDate);
    }

    @Override
    public StorePromotionReduceSummaryDTO getStorePromotionReduceSummary(int merchantId, long storeId) throws TException {
        StorePromotionSummary<StorePromotionReduce> summary = this.storePromotionReduceService.getStorePromotionReduceSummary(merchantId, storeId);
        StorePromotionReduceSummaryDTO summaryDTO = new StorePromotionReduceSummaryDTO();
        summaryDTO.setCount4NotBegin(summary.getCount4NotBegin());
        summaryDTO.setCount4Doing(summary.getCount4Doing());
        summaryDTO.setCount4Paused(summary.getCount4Paused());
        summaryDTO.setCount4Ended(summary.getCount4Ended());
        if (summary.getList4NotBegin() != null) {
            summaryDTO.setList4NotBegin(this.menuFacadeUtil.buildStorePromotionReduceDTOs(summary.getList4NotBegin(), true));
        }
        if (summary.getList4Doing() != null) {
            summaryDTO.setList4Doing(this.menuFacadeUtil.buildStorePromotionReduceDTOs(summary.getList4Doing(), true));
        }
        if (summary.getList4Paused() != null) {
            summaryDTO.setList4Paused(this.menuFacadeUtil.buildStorePromotionReduceDTOs(summary.getList4Paused(), true));
        }
        return summaryDTO;
    }

    @Override
    public StorePromotionReducePageDTO getStorePromotionReduces(int merchantId, long storeId, int status, int page, int size) throws TException {
        PageResult pageResult = this.storePromotionReduceService.getStorePromotionReduces(merchantId, storeId, status, page, size);
        StorePromotionReducePageDTO pageDTO = new StorePromotionReducePageDTO();
        pageDTO.setSize(size);
        pageDTO.setPageNo(page);
        pageDTO.setTotal(pageResult.getTotal());
        pageDTO.setPageNum(pageResult.getTotalPage());
        pageDTO.setDataList(this.menuFacadeUtil.buildStorePromotionReduceDTOs(pageResult.getList(), true));
        return pageDTO;
    }

    @Override
    public void changePromotionReducePaused(int merchantId, long storeId, long promotionReduceId, boolean paused) throws TException {
        this.storePromotionReduceService.changePromotionReducePaused(merchantId, storeId, promotionReduceId, paused);
    }

    @Override
    public void deleteStorePromotionReduce(int merchantId, long storeId, long promotionReduceId) throws T5weiException, TException {
        if (this.storeOrderPromotionStatService.hasStoreOrderPromotions(merchantId, storeId, promotionReduceId, StoreOrderPromotionTypeEnum.PROMOTION_REDUCE.getValue(), true)) {
            throw new T5weiException(T5weiErrorCodeType.STORE_PROMOTION_REDUCE_STAT_EXIST.getValue(), "merchantId[" + merchantId + "] storeId[" + storeId + "] reduceId[" + promotionReduceId + "] has stat data");
        }
        this.storePromotionReduceService.deleteStorePromotionReduce(merchantId, storeId, promotionReduceId);
    }

    @Override
    public Map<String, List<StorePromotionReduceDTO>> getStorePromotionReduceMapByTitle(int merchantId, long storeId, String title, int size) throws TException {
        Map<String, List<StorePromotionReduceDTO>> storePromotionDTOMap = Maps.newHashMap();
        Map<String, List<StorePromotionReduce>> storePromotionRebateMap = storePromotionReduceService.getStorePromotionReduceMapByTitle(merchantId, storeId, title, size);
        for (Map.Entry<String, List<StorePromotionReduce>> entry : storePromotionRebateMap.entrySet()) {
            List<StorePromotionReduce> list = entry.getValue();
            List<StorePromotionReduceDTO> valueDTO = this.menuFacadeUtil.buildStorePromotionReduceDTOs(list, true);
            storePromotionDTOMap.put(entry.getKey(), valueDTO);
        }
        return storePromotionDTOMap;
    }

}
